/*
 * Copyright 2019 Martin Conrad
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package SampleFiscalPrinter;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.fiscalprinter.*;
import jpos.FiscalPrinterConst;
import jpos.JposConst;
import jpos.JposException;

import javax.swing.*;
import java.math.BigDecimal;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import static SampleFiscalPrinter.Device.*;

/**
 * Class implementing the JposFiscalPrinterInterface for the sample fiscal printer.
 */
class FiscalPrinter extends FiscalPrinterProperties implements StatusUpdater {
    private SampleFiscalPrinter.Device Dev;

    /**
     * Constructor. Gets instance of Device to be used as communication object. Device
     * index for sample is always 0.
     * @param dev Instance of Device this object belongs to.
     */
    FiscalPrinter(SampleFiscalPrinter.Device dev) {
        super(0);
        Dev = dev;
    }

    @Override
    public void deviceEnabled(boolean enable) throws JposException {
        super.deviceEnabled(enable);
        synchronized (Dev) {
            Dev.updateStates(this, enable);
        }
    }

    @Override
    public void claim(int timeout) throws JposException {
        Dev.InitializeVatTable = true;
        Dev.startPolling(this);
        super.claim(timeout);
    }

    @Override
    public void release() throws JposException {
        super.release();
        Dev.stopPolling();
    }

    @Override
    public void checkHealth(int level) throws JposException {
        if (level == JposConst.JPOS_CH_INTERNAL) {
            CheckHealthText = "Internal CheckHealth: OK";
            return;
        }
        if (level == JposConst.JPOS_CH_INTERACTIVE) {
            Dev.synchronizedMessageBox("Press OK to start health test.", "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
        }
        CheckHealthText = (level == JposConst.JPOS_CH_EXTERNAL ? "Externel" : "Interactive") + " CheckHealth: ";
        try {
            ((FiscalPrinterService) EventSource).resetPrinter();
            CheckHealthText += "OK";
        } catch (JposException e) {
            CheckHealthText += "Failed, " + e.getMessage();
        }
        super.checkHealth(level);
    }

    /**
     * DirectIO implements the following command:<ul>
     * <li>SAMPLEFISCALPRINTERDIO_EXECCOMMAND<br>
     *     Executes one or several generic commands.</li>
     * <li>SAMPLEFISCALPRINTERDIO_NONFISCALRECEIPTTRAILER<br>
     *     Sets trailer flag for non-fiscal receipt to data[0], data[0] must be 0 or 1. obj won't be used.</li>
     * <li>SAMPLEFISCALPRINTERDIO_FISCALIZE<br>
     *     Performs fiscalization. At least, VAT rate 1, one header line and tax payer ID have to be set previously,
     *     The printer serial number must be set directly in the simulator. data and obj won't be used.</li>
     * </ul>
     * @param command   SAMPLEFISCALPRINTERDIO_EXECCOMMAND
     * @param data      If obj is String[], data[0] is the count of strings used as command, on return the count
     *                  of strings returned. If obj is String[][] or String[][][], data[0] becomes the count of
     *                  commands that have been executed successfully.
     * @param obj       String[] containing the command and its arguments, on return the received arguments, or
     *                  String[2][] containing the command strings in obj[0], on return the result strings in obj[1],
     *                  or String[][2][] containing several commands, on return the corresponding result strings.
     * @throws JposException If device nor claimed or in case of communication errors.
     */
    @Override
    public DirectIO directIO(int command, int[] data, Object obj) throws JposException {
        switch (command) {
            case SAMPLEFISCALPRINTERDIO_EXECCOMMAND:
                EventSource.checkClaimed();
                data[0] = Dev.executeCommands(data[0], obj);
                break;
            case SAMPLEFISCALPRINTERDIO_NONFISCALRECEIPTTRAILER:
                Dev.check((data[0] & ~1) != 0, JposConst.JPOS_E_ILLEGAL, "Invalid data, must be 0 or 1");
                Dev.NonFiscalReceiptWithTrailer = data[0] == 0 ? "0" : "1";
                break;
            case SAMPLEFISCALPRINTERDIO_FISCALIZE:
                EventSource.checkEnabled();
                Dev.check("".equals(Dev.SerialNumber), JposConst.JPOS_E_ILLEGAL, "Serial number not set");
                Dev.check(Dev.CurrentPeriod > 0, JposConst.JPOS_E_ILLEGAL, "Printer just fiscalized");
                String[][] cmd = new String[][]{new String[]{"fiscalize"}, null};
                if (Dev.executeCommands(0, cmd) != 1) {
                    commandErrorException(cmd, new long[]{CLOSED});
                }
                attachWaiter();
                Dev.PollWaiter.signal();
                waitWaiter(SyncObject.INFINITE);
                releaseWaiter();
                break;
        }
        return null;
    }

    @Override
    public void additionalHeader(String header) throws JposException {
        Dev.check(header.length() > MAXFISCALPRINTLINE, JposConst.JPOS_E_ILLEGAL, "Line too long: " + header);
        Dev.check(!removeControlCharacters(header, false).equals(header), JposConst.JPOS_E_ILLEGAL, "Header must not contain control characters: " + header);
        super.additionalHeader(header);
    }

    @Override
    public void additionalTrailer(String trailer) throws JposException {
        Dev.check(trailer.length() > MAXFISCALPRINTLINE, JposConst.JPOS_E_ILLEGAL, "Line too long: " + trailer);
        Dev.check(!removeControlCharacters(trailer, false).equals(trailer), JposConst.JPOS_E_ILLEGAL, "Text must not contain control characters: " + trailer);
        super.additionalTrailer(trailer);
    }

    @Override
    public void dateType(int type) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_DT_EOD,
                FiscalPrinterConst.FPTR_DT_RTC,
                FiscalPrinterConst.FPTR_DT_START,
                FiscalPrinterConst.FPTR_DT_CONF
        };
        Dev.checkMember(type, allowed, JposConst.JPOS_E_ILLEGAL, "Unsupported date type: " + type);
        super.dateType(type);
    }

    @Override
    public void messageType(int type) throws JposException {
        long[] allowedtypes = new long[]{
                FiscalPrinterConst.FPTR_MT_FREE_TEXT,
                FiscalPrinterConst.FPTR_MT_CASHIER
        };
        Dev.checkMember(type, allowedtypes, JposConst.JPOS_E_ILLEGAL, "Unsupported message type: " + type);
        super.messageType(type);
    }

    @Override
    public void postLine(String text) throws JposException {
        Dev.check(text.length() > MAXFISCALPRINTLINE, JposConst.JPOS_E_ILLEGAL, "Line too long: " + text);
        Dev.check(!removeControlCharacters(text, false).equals(text), JposConst.JPOS_E_ILLEGAL, "Text must not contain control characters: " + text);
        super.postLine(text);
    }

    @Override
    public void preLine(String text) throws JposException {
        Dev.check(text.length() > MAXFISCALPRINTLINE, JposConst.JPOS_E_ILLEGAL, "Line too long: " + text);
        Dev.check(!removeControlCharacters(text, false).equals(text), JposConst.JPOS_E_ILLEGAL, "Text must not contain control characters: " + text);
        super.preLine(text);
    }

    @Override
    public void clearError() throws JposException {
        super.clearError();
    }

    @Override
    public void beginTraining() throws JposException {
        char[] state = Dev.getCurrentState();
        checkOperational(state);
        Dev.check(state[RECEIPT] != CLOSED, JposConst.JPOS_E_ILLEGAL, "Printer in receipt");
        if (!TrainingModeActive) {
            Object e = processCommand(new String[]{"setTrainingMode", "1"});
            if (e instanceof JposException)
                throw new JposException (JposConst.JPOS_E_FAILURE, "Setting training mode failed: " + ((JposException)e).getMessage(), (JposException) e);
            attachWaiter();
            Dev.PollWaiter.signal();
            waitWaiter(SyncObject.INFINITE);
            releaseWaiter();
            Dev.check(!TrainingModeActive, JposConst.JPOS_E_FAILURE, "Could not activate training mode");
        }
    }

    @Override
    public void endTraining() throws JposException {
        char[] state = Dev.getCurrentState();
        checkOperational(state);
        Dev.check(state[RECEIPT] != CLOSED, JposConst.JPOS_E_ILLEGAL, "Printer in receipt");
        if (TrainingModeActive) {
            Object e = processCommand(new String[]{"setTrainingMode", "0"});
            if (e instanceof JposException)
                throw new JposException (JposConst.JPOS_E_FAILURE, "Resetting training mode failed: " + ((JposException)e).getMessage(), (JposException) e);
            attachWaiter();
            Dev.PollWaiter.signal();
            waitWaiter(SyncObject.INFINITE);
            releaseWaiter();
            Dev.check(TrainingModeActive, JposConst.JPOS_E_FAILURE, "Could not deactivate training mode");
        }
    }

    @Override
    public void beginFixedOutput(int station, int documentType) throws JposException {
        Dev.checkMember(documentType, Dev.AllowedDocumentLineTable[0], JposConst.JPOS_E_ILLEGAL, "Invalid document type: " + documentType);
        beginNonFiscalOutput(documentType, "fixed output", FiscalPrinterConst.FPTR_PS_FIXED_OUTPUT);
    }

    @Override
    public void beginNonFiscal() throws JposException {
        beginNonFiscalOutput(0, "non-fiscal", FiscalPrinterConst.FPTR_PS_NONFISCAL);
    }

    /**
     * Checks whether a status array represents an operational device.
     * @param state State of the device.
     * @throws JposException    If the device is not in an operational state.
     */
    private void checkOperational(char[] state) throws JposException {
        Dev.check(state.length <= DRAWER, JposConst.JPOS_E_FAILURE, "Printer not online");
        Dev.check(state[FISCAL] == NOTFISCALIZED, JposConst.JPOS_E_ILLEGAL, "Printer not fiscalized");
        Dev.check(state[FISCAL] == FISCALBLOCK, JposConst.JPOS_E_ILLEGAL, "Printer blocked");
    }

    /**
     * Process a printer command. On success, a String array containing the components of the response will be returned.
     * In case of an error, a JposException describing the reason for the failure will be returned.
     * @param cmd Components of the command.
     * @return Response components on success, JposException otherwise.
     */
    private Object processCommand(String[] cmd) {
        String[] resp = Dev.sendrecv(cmd);
        String reason = "Cannot process internal command " + cmd[0];
        int status = 99999999;
        if (resp == null)
            return new JposException(JposConst.JPOS_E_FAILURE, status, reason + ": Command timed out");
        if (resp.length == 0)
            return new JposException(JposConst.JPOS_E_FAILURE, status, reason + ": Invalid frame contents");
        if (resp[0].charAt(0) != SUCCESS) {
            if (resp.length == 2) {
                long index;
                try {
                    status = Integer.parseInt(resp[0]) + 10000000;
                    index = Long.parseLong(resp[1]);
                } catch (Exception e) {
                    index = cmd.length;
                }
                if (index > 0 && index < cmd.length)
                    return new JposException(JposConst.JPOS_E_ILLEGAL, status, reason + ": Illegal parameter " + index + ": " + cmd[(int)index]);
                return new JposException(JposConst.JPOS_E_FAILURE, status, reason + ": Check status (" + status + " - " + index + ")");
            }
            else if (resp.length == 3)
                return new JposException(JposConst.JPOS_E_ILLEGAL, status, reason + ": " + resp[2]);
        }
        return resp;
    }

    private void beginNonFiscalOutput(int documentType, String receiptType, int targetstate) throws JposException {
        char[] state = Dev.getCurrentState();
        checkOperational(state);
        Dev.check(state[RECEIPT] != CLOSED, JposConst.JPOS_E_ILLEGAL, "Printer in receipt");
        Dev.FixedOutputDocument = documentType;
        Dev.NonFiscalMinLineNo = documentType > 0 ? SAMPLEFISCALPRINTERFXO_HEAD : 0;
        Dev.PrintBuffer = "";
        String[][][] cmds = new String[0][][];
        cmds = Dev.addCommand(cmds, new String[]{"setHeaderLine", Long.toString(MAXHEADERLINES + 1), ""});
        cmds = Dev.addCommand(cmds, new String[]{"nfStart"});
        if (Dev.executeCommands(0, cmds) != cmds.length){
            for (String[][]cmd : cmds)
                commandErrorException(cmd, new long[]{NONFISCAL});
        }
        attachWaiter();
        Dev.PollWaiter.signal();
        waitWaiter(SyncObject.INFINITE);
        releaseWaiter();
        Dev.check(PrinterState != targetstate, JposConst.JPOS_E_FAILURE, "Could not enter " + receiptType + " receipt");
    }

    @Override
    public void endFixedOutput() throws JposException {
        endNonFiscalOutput(FiscalPrinterConst.FPTR_PS_FIXED_OUTPUT, "fixed output");
    }

    @Override
    public void endNonFiscal() throws JposException {
        endNonFiscalOutput(FiscalPrinterConst.FPTR_PS_NONFISCAL, "non-fiscal");
    }

    private void endNonFiscalOutput(int startState, String receiptType) throws JposException {
        char[] state = Dev.getCurrentState();
        checkOperational(state);
        Dev.check(PrinterState != startState, JposConst.JPOS_E_ILLEGAL, "Printer not in " + receiptType + " receipt");
        String[][][] cmds = new String[0][][];
        waitAsyncFinished();
        Dev.checkext(Dev.NonFiscalMinLineNo > 0, FiscalPrinterConst.JPOS_EFPTR_BAD_LENGTH, "Last line not been reached");
        if (Dev.PrintBuffer.length() > 0)
            cmds = Dev.addCommand(cmds, new String[]{"nfPrint", Dev.PrintBuffer});
        cmds = Dev.addCommand(cmds, new String[]{"setTrailerLine", Long.toString(MAXTRAILERLINES + 1), ""});
        cmds = Dev.addCommand(cmds, new String[]{"nfEnd", Dev.NonFiscalReceiptWithTrailer});
        if (Dev.executeCommands(0, cmds) != cmds.length){
            for (String[][]cmd : cmds)
                commandErrorException(cmd, new long[]{NONFISCAL});
        }
        attachWaiter();
        Dev.PollWaiter.signal();
        waitWaiter(SyncObject.INFINITE);
        releaseWaiter();
        Dev.check(PrinterState != FiscalPrinterConst.FPTR_PS_MONITOR, JposConst.JPOS_E_FAILURE, "Could not leave " + receiptType + " receipt");
    }

    private void waitAsyncFinished() {
        SyncObject idlewaiter = OutputRequest.setIdleWaiter(Dev);
        if (idlewaiter != null)
            idlewaiter.suspend(SyncObject.INFINITE);
    }

    @Override
    public void beginFiscalReceipt(boolean printHeader) throws JposException {
        char[] state = Dev.getCurrentState();
        checkOperational(state);
        Dev.check(state[RECEIPT] != CLOSED, JposConst.JPOS_E_ILLEGAL, "Printer in receipt");
        String[][][] cmds = new String[][][]{
                new String[][]{new String[]{"setHeaderLine", Long.toString(MAXHEADERLINES + 1), AdditionalHeader}, null},
                new String[][]{new String[]{"fStart"}, null}
        };
        if (Dev.executeCommands(0, cmds) != cmds.length) {
            for (String[][]cmd : cmds)
                commandErrorException(cmd, new long[]{CLOSED});
        }
        attachWaiter();
        Dev.PollWaiter.signal();
        waitWaiter(SyncObject.INFINITE);
        releaseWaiter();
        Dev.check(PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT, JposConst.JPOS_E_FAILURE, "Could not enter fiscal receipt");
        Dev.VoidOnEndFiscal = true;
        Dev.SubtotalAdjustment = null;
    }


    @Override
    public void endFiscalReceipt(boolean printHeader) throws JposException {
        char[] state = Dev.getCurrentState();
        checkOperational(state);
        String[][][] cmds = new String[0][][];
        waitAsyncFinished();
        if (Dev.VoidOnEndFiscal) {
            cmds = Dev.addCommand(cmds, new String[]{"fPrintTotal"});
        }
        else
            Dev.check(Dev.getCurrentState()[RECEIPT] != FINALIZING, JposConst.JPOS_E_ILLEGAL, "Printer not in fiscal receipt ending state");
        cmds = Dev.addCommand(cmds, new String[]{"setTrailerLine", Long.toString(MAXTRAILERLINES + 1), AdditionalTrailer});
        cmds = Dev.addCommand(cmds, new String[]{"fEnd"});
        if (Dev.executeCommands(0, cmds) != cmds.length) {
            for (String[][] cmd : cmds) {
                commandErrorException(cmd, new long[]{CLOSED, FINALIZING});
            }
        }
        attachWaiter();
        Dev.PollWaiter.signal();
        waitWaiter(SyncObject.INFINITE);
        releaseWaiter();
        Dev.check(PrinterState != FiscalPrinterConst.FPTR_PS_MONITOR, JposConst.JPOS_E_FAILURE, "Could not leave fiscal receipt");
        Dev.SubtotalAdjustment = null;
    }

    @Override
    public void getData(int dataItem, int[] optArgs, String[] data) throws JposException {
        switch (dataItem) {
            case FiscalPrinterConst.FPTR_GD_PRINTER_ID:
                data[0] = Dev.SerialNumber;
                return;
            case FiscalPrinterConst.FPTR_GD_CURRENT_TOTAL:
                String[][]cmd = new String[][]{new String[]{"getCurrentTotal", "0"}, null};
                if (Dev.executeCommands(0, cmd) != 1 || !returnValuePresent(cmd)) {
                    commandErrorException(cmd, new long[]{ITEMIZING, PAYING, FINALIZING});
                    throw new JposException(JposConst.JPOS_E_FAILURE, "Total not available");
                }
                if (Dev.CurrencyStringAsLong)
                    data[0] = Long.toString(new BigDecimal(cmd[1][1]).multiply(new BigDecimal(CURRENCYFACTOR)).longValue());
                else
                    data[0] = cmd[1][1];
                return;
            case FiscalPrinterConst.FPTR_GD_DAILY_TOTAL:
                getTotalOrCount(new String[]{"get", "Total", "All"}, data, "daily total", true);
                return;
            case FiscalPrinterConst.FPTR_GD_GRAND_TOTAL:
                getTotalOrCount(new String[]{"get", "GrandTotal"}, data, "grand total", true);
                return;
            case FiscalPrinterConst.FPTR_GD_NOT_PAID:
                getTotalOrCount(new String[]{"get", "Total", "Aborts"}, data, "aborted receipt total", true);
                return;
            case FiscalPrinterConst.FPTR_GD_RECEIPT_NUMBER:
            case FiscalPrinterConst.FPTR_GD_FISCAL_REC:
                getTotalOrCount(new String[]{"get", "Total", "Fiscal"}, data, "fiscal receipt number", false);
                return;
            case FiscalPrinterConst.FPTR_GD_REFUND:
                getTotalOrCount(new String[]{"get", "Total", "Refunds"}, data, "refund total", true);
                return;
            case FiscalPrinterConst.FPTR_GD_NONFISCAL_REC:
                getTotalOrCount(new String[]{"get", "Total", "Normal"}, data, "non-fiscal receipt number", false);
                return;
            case FiscalPrinterConst.FPTR_GD_Z_REPORT:
                data[0] = Long.toString(Dev.CurrentPeriod);
                return;
            case FiscalPrinterConst.FPTR_GD_DESCRIPTION_LENGTH:
                getDescriptionLength(optArgs[0], data);
                return;
        }
        throw new JposException(JposConst.JPOS_E_ILLEGAL, "Unsupported dataItem: " + dataItem);
    }

    private void getTotalOrCount(String[] cmd, String[] data, String what, boolean currency) throws JposException {
        String[][] command = new String[][]{cmd, null};
        if (Dev.executeCommands(0, command) == 1 && returnValuePresent(command)) {
            data[0] = command[1][1];
            if (Dev.CurrencyStringAsLong) {
                try {
                    data[0] = Long.toString(new BigDecimal(data[0]).multiply(new BigDecimal(CURRENCYFACTOR)).longValue());
                    return;
                } catch (NumberFormatException e) {
                    throw new JposException(JposConst.JPOS_E_FAILURE, "Could not retrieve valid " + what + ": " + e.getMessage(), e);
                }
            }
            return;
        }
        throw new JposException(JposConst.JPOS_E_FAILURE, "Could not retrieve " + what);
    }

    private void getDescriptionLength(int optArg, String[] data) throws JposException {
        switch(optArg) {
            case FiscalPrinterConst.FPTR_DL_ITEM:
            case FiscalPrinterConst.FPTR_DL_ITEM_ADJUSTMENT:
            case FiscalPrinterConst.FPTR_DL_REFUND:
            case FiscalPrinterConst.FPTR_DL_REFUND_VOID:
            case FiscalPrinterConst.FPTR_DL_SUBTOTAL_ADJUSTMENT:
            case FiscalPrinterConst.FPTR_DL_TOTAL:
            case FiscalPrinterConst.FPTR_DL_VOID_ITEM:
                data[0] = Integer.toString(MAXDESCRIPTIONLENGTH);
                return;
            case FiscalPrinterConst.FPTR_DL_VOID:
                data[0] = Long.toString(MAXFISCALPRINTLINE);
                return;
        }
        throw new JposException(JposConst.JPOS_E_ILLEGAL, "Unsupported optArgs for description length: " + optArg);
    }

    @Override
    public void getDate(String[] date) throws JposException {
        String[][]command;
        int result;
        switch (DateType) {
            case FiscalPrinterConst.FPTR_DT_RTC:
                result = Dev.executeCommands(0, command = new String[][]{
                        new String[]{"getDate"}, null
                });
                Dev.check(result != 1 || !returnValuePresent(command), JposConst.JPOS_E_ILLEGAL, "RTC currently not available");
                String rtc = command[1][1];
                date[0] = rtc.substring(6, 8) + rtc.substring(4, 6) + rtc.substring(0, 4) + rtc.substring(8, 12);
                break;
            case FiscalPrinterConst.FPTR_DT_START:
                result = Dev.executeCommands(0, command = new String[][]{
                        DayOpened ? new String[]{"get", "Total", "Date"} : new String[]{"get", "Memory", Long.toString(Dev.CurrentPeriod - 1), "Date"},
                        null
                });
                Dev.check(result != 1, JposConst.JPOS_E_ILLEGAL, "Start date currently not available");
                date[0] = new SimpleDateFormat("ddMMyyyyHHmm").format(new Date(Long.parseLong(command[1][1]) * 1000));
                break;
            case FiscalPrinterConst.FPTR_DT_CONF:
                getLastTicketDateOfPeriod(date, 1, "CONF");
                break;
            case FiscalPrinterConst.FPTR_DT_EOD:
                getLastTicketDateOfPeriod(date, Dev.CurrentPeriod, "EOD");
        }
    }

    private void getLastTicketDateOfPeriod(String[] date, int period, String what) throws JposException {
        Dev.check(period == 0, JposConst.JPOS_E_ILLEGAL, "No " + what + " date available");
        try {
            date[0] = new SimpleDateFormat("ddMMyyyyHHmm").format(getLastTicketDate(period - 1));
        }
        catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, what + " date not available", e);
        }
    }

    private Date getLastTicketDate(int period) throws JposException {
        String[][] command;
        int result = Dev.executeCommands(0, command = new String[][]{
                new String[]{"get", "Memory", Long.toString(period), "Normal", "Fiscal"}, null
        });
        String recno = Long.toString(Long.parseLong(command[1][1]) + Long.parseLong(command[1][2]));
        result = Dev.executeCommands(0, command = new String[][]{
                new String[]{"retrieveJournal", Long.toString(period), recno, recno, "0"}, null
        });
        return new Date(Long.parseLong(command[1][2]) * 1000L);
    }

    @Override
    public void getTotalizer(int vatID, int optArgs, String[] data) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL, "Totalizer not supported: " + optArgs);
    }

    @Override
    public void getVatEntry(int vatID, int optArgs, int[] vatRate) throws JposException {
        Dev.check(vatID < 1 || vatID > MAXVATINDEX, JposConst.JPOS_E_ILLEGAL, "Invalid vatID: " + vatID);
        Object e = processCommand(new String[]{"get", "VAT", Integer.toString(vatID)});
        try {
            String[] resp = (String[]) e;
            if (resp[0].charAt(0) == SUCCESS && resp.length == 2) {
                int rate = new BigDecimal(resp[1]).multiply(new BigDecimal(CURRENCYFACTOR)).intValue();
                vatRate[0] = rate < 0 ? -1 : rate;
                return;
            }
        } catch (Exception ee) {
            if (!(e instanceof JposException))
                e = ee;
        }
        Exception ex = (Exception) e;
        throw new JposException(JposConst.JPOS_E_FAILURE, "Error getting VAT rate " + vatID + ": " + ex.getMessage(), ex);
    }

    @Override
    public void printPeriodicTotalsReport(String date1, String date2) throws JposException {
        int start = getStartPeriod(date1, 1);
        Dev.check(start == 0, JposConst.JPOS_E_ILLEGAL, "No period starting after " + date1 + " found");
        int end = getEndPeriod(date2, start);
        Dev.check(end == 0, JposConst.JPOS_E_ILLEGAL, "No period ending before " + date2 + " found");
        Dev.check(start > end, JposConst.JPOS_E_ILLEGAL, "No period starting before " + date1 + " and ending after " + date2 + " found");
        Object e = processCommand(new String[]{"report", Long.toString(start), Long.toString(end), "1"});
        if (e instanceof JposException) {
            char[] state = Integer.toString(((JposException) e).getErrorCodeExtended()).toCharArray();
            if (state[0] == '9' || state.length  != DRAWER + 2)
                throw new JposException(JposConst.JPOS_E_FAILURE, "Communication error", (Exception) e);
            Dev.checkext(state[RECEIPT + 1] != CLOSED, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Bad printer state");
            Dev.checkext(state[PRINTER + 1] == EMPTY, FiscalPrinterConst.JPOS_EFPTR_REC_EMPTY, "Paper end");
            Dev.checkext(state[PRINTER + 1] == NEAREND, FiscalPrinterConst.JPOS_EFPTR_REC_EMPTY, "Paper near end");
            Dev.check(state[PRINTER + 1] == COVERORERROR, JposConst.JPOS_E_FAILURE, "Print station error");
            throw new JposException(JposConst.JPOS_E_FAILURE, "Mysterious error: " + ((JposException) e).getMessage(), (Exception)e);
        }
    }

    private int getStartPeriod (String date, int lower) {
        SimpleDateFormat format = new SimpleDateFormat("ddMMyyyyHHmm");
        format.setLenient(false);
        Date dateval = format.parse(date, new ParsePosition(0));
        Date testval = new Date(0L);
        int upper = Dev.CurrentPeriod - 1;
        if (upper < lower)
            return 0;
        while (lower < upper) {
            int index = (lower + upper) / 2;
            try {
                String[] resp = (String[]) processCommand(new String[]{"get", "Memory", Integer.toString(index), "Date", "Normal", "Fiscal"});
                testval.setTime(Long.parseLong(resp[1]) * 1000);
            } catch (Exception e) {
                return 0;
            }
            int relation = dateval.compareTo(testval);
            if (relation == 0)
                return index;
            if (relation > 0)
                lower = index + 1;
            else
                upper = index;
        }
        return lower;
    }

    private int getEndPeriod (String date, int lower) {
        int index = getStartPeriod(date, lower);
        if (index <= 1)
            return 0;
        SimpleDateFormat format = new SimpleDateFormat("ddMMyyyyHHmm");
        try {
            format.setLenient(false);
            for (Date dateval = format.parse(date, new ParsePosition(0)); dateval.compareTo(getLastTicketDate(index)) < 0; --index) {}
        }
        catch (Exception e) {
            return 0;
        }
        return index;
    }

    @Override
    public void printReport(int reportType, String startNum, String endNum) throws JposException {
        int start, end;
        if (reportType == FiscalPrinterConst.FPTR_RT_DATE) {
            start = getStartPeriod(startNum, 1);
            Dev.check(start == 0, JposConst.JPOS_E_ILLEGAL, "No period starting after " + startNum + " found");
            end = getEndPeriod(endNum, start);
            Dev.check(end == 0, JposConst.JPOS_E_ILLEGAL, "No period ending before " + endNum + " found");
            Dev.check(start > end, JposConst.JPOS_E_ILLEGAL, "No period starting before " + startNum + " and ending after " + endNum + " found");
            startNum = Long.toString(start);
            endNum = Long.toString(end);
        }
        else {
            if (Integer.parseInt(endNum) == 0)
                endNum = startNum;
            Dev.check(Integer.parseInt(endNum) >= Dev.CurrentPeriod, JposConst.JPOS_E_ILLEGAL, "Invalid period: " + endNum);
        }
        Object e = processCommand(new String[]{"report", startNum, endNum, "0"});
        if (e instanceof JposException) {
            char[] state = Integer.toString(((JposException) e).getErrorCodeExtended()).toCharArray();
            if (state[0] == '9' || state.length  != DRAWER + 2)
                throw new JposException(JposConst.JPOS_E_FAILURE, "Communication error", (Exception) e);
            Dev.checkext(state[RECEIPT + 1] != CLOSED, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Bad printer state");
            Dev.checkext(state[PRINTER + 1] == EMPTY, FiscalPrinterConst.JPOS_EFPTR_REC_EMPTY, "Paper end");
            Dev.checkext(state[PRINTER + 1] == NEAREND, FiscalPrinterConst.JPOS_EFPTR_REC_EMPTY, "Paper near end");
            Dev.check(state[PRINTER + 1] == COVERORERROR, JposConst.JPOS_E_FAILURE, "Print station error");
            throw new JposException(JposConst.JPOS_E_FAILURE, "Mysterious error: " + ((JposException) e).getMessage(), (Exception)e);
        }
    }

    @Override
    public void printXReport() throws JposException {
        Object e = processCommand(new String[]{"xReport"});
        if (e instanceof JposException) {
            char[] state = Integer.toString(((JposException) e).getErrorCodeExtended()).toCharArray();
            if (state[0] == '9' || state.length  != DRAWER + 2)
                throw new JposException(JposConst.JPOS_E_FAILURE, "Communication error", (Exception) e);
            Dev.checkext(state[RECEIPT + 1] != CLOSED, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Bad printer state");
            Dev.checkext(state[PRINTER + 1] == EMPTY, FiscalPrinterConst.JPOS_EFPTR_REC_EMPTY, "Paper end");
            Dev.checkext(state[PRINTER + 1] == NEAREND, FiscalPrinterConst.JPOS_EFPTR_REC_EMPTY, "Paper near end");
            Dev.check(state[PRINTER + 1] == COVERORERROR, JposConst.JPOS_E_FAILURE, "Print station error");
            throw new JposException(JposConst.JPOS_E_FAILURE, "Mysterious error: " + ((JposException) e).getMessage(), (Exception)e);
        }
    }

    @Override
    public void printZReport() throws JposException {
        Dev.checkext(!DayOpened, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Fiscal day has not been started");
        Object e = processCommand(new String[]{"zReport"});
        if (e instanceof JposException) {
            char[] state = Integer.toString(((JposException) e).getErrorCodeExtended()).toCharArray();
            if (state[0] == '9' || state.length  != DRAWER + 2)
                throw new JposException(JposConst.JPOS_E_FAILURE, "Communication error", (Exception) e);
            Dev.checkext(state[RECEIPT + 1] != CLOSED, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Bad printer state");
            Dev.checkext(state[PRINTER + 1] == EMPTY, FiscalPrinterConst.JPOS_EFPTR_REC_EMPTY, "Paper end");
            Dev.checkext(state[PRINTER + 1] == NEAREND, FiscalPrinterConst.JPOS_EFPTR_REC_EMPTY, "Paper near end");
            Dev.check(state[PRINTER + 1] == COVERORERROR, JposConst.JPOS_E_FAILURE, "Print station error");
            throw new JposException(JposConst.JPOS_E_FAILURE, "Mysterious error: " + ((JposException) e).getMessage(), (Exception)e);
        }
        attachWaiter();
        Dev.PollWaiter.signal();
        waitWaiter(SyncObject.INFINITE);
        releaseWaiter();
        Dev.check(PrinterState != FiscalPrinterConst.FPTR_PS_MONITOR || DayOpened, JposConst.JPOS_E_FAILURE, "Could not close fiscal day");
    }

    @Override
    public void resetPrinter() throws JposException {
        attachWaiter();
        Dev.PollWaiter.signal();
        waitWaiter(SyncObject.INFINITE);
        releaseWaiter();
        synchronized (Dev) {
            String[][][]  commands = new String[0][][];
            char[] state = Dev.getCurrentState();
            Dev.check(PrinterState == FiscalPrinterConst.FPTR_PS_LOCKED, JposConst.JPOS_E_FAILURE, "Communication error");
            Dev.check(state[FISCAL] == FISCALBLOCK, JposConst.JPOS_E_FAILURE, "Fiscal block");
            Dev.check(Dev.member(state[PRINTER], new long[]{EMPTY, NEAREND}), JposConst.JPOS_E_FAILURE, "Change Paper");
            Dev.check(state[RECEIPT] == BLOCKED, JposConst.JPOS_E_FAILURE, "Printer blocked");
            if (state[TRAININGMODE] == ON) {
                commands = Dev.addCommand(commands, new String[]{"setTrainingMode", "0"});
            }
            switch (state[RECEIPT]) {
                case  NONFISCAL: {
                    String text = Dev.centeredLine(Dev.PrinterResetText);
                    commands = Dev.addCommand(commands, new String[]{"nfPrint", "\n" + text});
                    commands = Dev.addCommand(commands, new String[]{"nfEnd", Dev.NonFiscalReceiptWithTrailer});
                    break;
                }
                case ITEMIZING:
                case PAYING:
                case FINALIZING: {
                    commands = Dev.addCommand(commands, new String[]{"fAbort", Dev.PrinterResetText});
                    commands = Dev.addCommand(commands, new String[]{"fEnd"});
                }
            }
            Dev.executeCommands(0, commands);
        }
        attachWaiter();
        Dev.PollWaiter.signal();
        waitWaiter(SyncObject.INFINITE);
        releaseWaiter();
        Dev.check(PrinterState != FiscalPrinterConst.FPTR_PS_MONITOR || TrainingModeActive, JposConst.JPOS_E_FAILURE, "Printer reset failed");
    }

    @Override
    public void setHeaderLine(int lineNumber, String text, boolean doubleWidth) throws JposException {
        text = expandText(removeControlCharacters(text, false), doubleWidth);
        Dev.checkext(text.length() > MAXPRINTLINELENGTH, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Text too long");
        String[][] cmd = new String[][]{new String[]{"setHeaderLine", Integer.toString(lineNumber), text}, null};
        if (Dev.executeCommands(0, cmd) != 1) {
            commandErrorException(cmd, new long[]{CLOSED});
        }
    }

    private String removeControlCharacters(String text, boolean holdNL) {
        for(int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (holdNL && c == '\r') {
                char prev = i == 0 ? 0 : text.charAt(i - 1);
                char next = i < text.length() - 1 ? text.charAt(i + 1) : 0;
                if (prev != '\n' && next != '\n') {
                    text = text.substring(0, i) + new char[]{c = '\n'} + text.substring(i + 1);
                }
            }
            if (c < ' ' && (!holdNL || c != '\n')) {
                text = text.substring(0, i) + text.substring(i + 1);
                --i;
            }
        }
        return text;
    }

    private String expandText(String text, boolean doubleWidth) {
        if (doubleWidth) {
            char[] result = new char[text.length() * 2 - 1];
            for (int i = text.length() - 1; true; --i) {
                result[2 * i] = text.charAt(i);
                if (i == 0)
                    break;
                result[2 * i - 1] = ' ';
            }
            return new String(result);
        }
        return text;
    }

    @Override
    public void setPOSID(String POSID, String cashierID) throws JposException {
        try {
            int posid = Integer.parseInt(POSID);
            int cashier = Integer.parseInt(cashierID);
            Dev.check(posid < 0 || posid > MAXPOSID, JposConst.JPOS_E_ILLEGAL, "POSID must be positive < 100000");
            Dev.check(cashier < 0 || cashier > MAXCASHIER, JposConst.JPOS_E_ILLEGAL, "POSID must be positive < 100000");
            String[][][] commands = new String[][][]{
                    new String[][]{new String[]{"setTill", POSID}, null},
                    new String[][]{new String[]{"setCashier", cashierID}, null}
            };
            if (Dev.executeCommands(0, commands) < commands.length) {
                for (String[][] command : commands) {
                    long[] allowed = command[0][0].equals("setTill") ? new long[]{CLOSED} : new long[]{CLOSED, ITEMIZING, PAYING, FINALIZING, NONFISCAL};
                    commandErrorException(command, allowed);
                }
            }
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "POSID and cashierID must be numerical values", e);
        }
    }

    @Override
    public void setStoreFiscalID(String ID) throws JposException {
        ID = removeControlCharacters(ID, false);
        Dev.check(ID.length() > MAXDESCRIPTIONLENGTH, JposConst.JPOS_E_ILLEGAL, "Text too long");
        String[][] cmd = new String[][]{new String[]{"setTaxPayerID", ID}, null};
        if (Dev.executeCommands(0, cmd) != 1) {
            commandErrorException(cmd, new long[]{CLOSED});
        }
    }

    @Override
    public void setTrailerLine(int lineNumber, String text, boolean doubleWidth) throws JposException {
        text = expandText(removeControlCharacters(text, false), doubleWidth);
        Dev.checkext(text.length() > MAXPRINTLINELENGTH, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Text too long");
        String[][] cmd = new String[][]{new String[]{"setTrailerLine", Integer.toString(lineNumber), text}, null};
        if (Dev.executeCommands(0, cmd) != 1) {
            commandErrorException(cmd, new long[]{CLOSED});
        }
    }

    private void commandErrorException(String[][] cmd, long[] allowed) throws JposException {
        Dev.check(cmd[1] == null || cmd[1].length < 1 || cmd[1][0] == null || cmd[1][0].length() < DRAWER + 2, JposConst.JPOS_E_FAILURE, "Communication error");
        if (cmd[1][0].charAt(0) == SUCCESS)
            return;
        char[] state = cmd[1][0].substring(1).toCharArray();
        Dev.check(state[RECEIPT] == BLOCKED, JposConst.JPOS_E_FAILURE, "Print station error");
        Dev.checkext(!Dev.member(state[RECEIPT], allowed), FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Bad printer state");
        Dev.checkext(state[PRINTER] >= NEAREND, FiscalPrinterConst.JPOS_EFPTR_REC_EMPTY, "Change paper");
        Dev.check(state[DRAWER] == OPENED, JposConst.JPOS_E_FAILURE, "Close cash drawer");
        if (cmd[1][0].charAt(0) != SUCCESS && Dev.member(cmd[1].length, new long[]{2, 3})) {
            String cmdstr = cmd[0][0];
            for (int i = 1; i < cmd[0].length; i++)
                cmdstr = cmdstr + " ETB " + cmd[0][i];
            cmdstr = "Internal command [" + cmdstr + "] failed: ";
            Dev.check(cmd[1].length == 2 && cmd[1][1].equals("0"), JposConst.JPOS_E_ILLEGAL, cmdstr + "Check state [" + new String(state) + "]");
            Dev.check(cmd[1].length == 2, JposConst.JPOS_E_FAILURE, cmdstr + "Bad parameter " + cmd[1][1]);
            Dev.check(cmd[1].length == 3 , JposConst.JPOS_E_FAILURE, cmdstr + cmd[1][1] + " - " + cmd[1][2]);
        }
        throw new JposException(JposConst.JPOS_E_FAILURE, "Unknown error");
    }

    private boolean returnValuePresent(String[][] cmd) {
        return (cmd[1].length == 2 && cmd[1][1] != null);
    }

    @Override
    public void setVatTable() throws JposException {
        for (int i = 1; i < MAXVATINDEX; ++i) {
            Dev.check(Dev.NewVatTable[i - 1] < Dev.NewVatTable[i], JposConst.JPOS_E_ILLEGAL, "VAT rates must be in decending order");
        }
        try {
            String[][][] commands = new String[0][][];
            for (int i = 0; i < MAXVATINDEX; i++) {
                if ((Dev.NewVatTable[i] >= 0 || Dev.VatTable[i] >= 0) && Dev.VatTable[i] != Dev.NewVatTable[i]) {
                    commands = Dev.addCommand(commands, new String[]{"setVatRate", Long.toString(i + 1), new BigDecimal(Dev.NewVatTable[i]).divide(new BigDecimal(CURRENCYFACTOR)).toString()});
                }
            }
            int count = Dev.executeCommands(0, commands);
            for (int i = 0; i < commands.length; i++) {
                if (commands[i][1] != null && commands[i][1].length > 0 && commands[i][1][0].charAt(0) == SUCCESS) {
                    int idx = Integer.parseInt(commands[i][0][1]) - 1;
                    Dev.VatTable[idx] = Dev.NewVatTable[idx];
                }
            }
            if (count != commands.length) {
                for (String[][] command : commands) {
                    commandErrorException(command, new long[]{CLOSED});
                }
            }
        } catch (ClassCastException e) {
            throw new JposException(JposConst.JPOS_E_FAILURE, "Communication error get VAT rates", e);
        } catch (NumberFormatException e) {
            throw new JposException(JposConst.JPOS_E_FAILURE, "Invalid printer VAT rate", e);
        }
    }

    @Override
    public void setDate(String date) throws JposException {
        SimpleDateFormat format = new SimpleDateFormat("ddMMyyyyHHmm");
        String nowstr = format.format(new Date());
        checkext(!nowstr.equals(date), FiscalPrinterConst.JPOS_EFPTR_BAD_DATE, "Invalid date");
    }

    @Override
    public void setVatValue(int vatID, long vatValue) throws JposException {
        Dev.check(vatID <= 0 || vatID > MAXVATINDEX, JposConst.JPOS_E_ILLEGAL, "Invalid VatD: " + vatID);
        Dev.NewVatTable[vatID - 1] = vatValue;
    }

    /**
     * Flags for fixed output document lines. One int[] per document, one entry per line:
     * <br>0: Mandatory line, empty data (first and last line),
     * <br>1: Mandatory line, cashier number (numeric, 1 - 99999),
     * <br>-2: Optional line, secret (any string no longer than MAXDESCRIPTIONLENGTH
     */
    private int[][] FixedOutputDataFlags = new int[][]{
            null,       // Not used, but possible values are 0 (mandatory, data empty), 1 (mandatory, cashier no), -2 (optional, secret)
            new int[]{-2, 1, 0},         // Sign on
            new int[]{-2, 1, -2, 0}      // Sign off
    };

    @Override
    public PrintFixedOutput printFixedOutput(int documentType, int lineNumber, String data) throws JposException {
        Dev.check(documentType != Dev.FixedOutputDocument, JposConst.JPOS_E_ILLEGAL, "Bad document type: " + documentType);
        boolean allowedLine = (Dev.member(lineNumber, Dev.AllowedDocumentLineTable[documentType]) && lineNumber >= Dev.NonFiscalMinLineNo) || lineNumber == SAMPLEFISCALPRINTERFXO_HEAD;
        Dev.check(!allowedLine, JposConst.JPOS_E_ILLEGAL, "Invalid line number: " + lineNumber);
        Dev.check(FixedOutputDataFlags[documentType][lineNumber - SAMPLEFISCALPRINTERFXO_HEAD] == 0 && data.length() != 0, JposConst.JPOS_E_ILLEGAL, "Invalid data for line " + lineNumber + ": Must be empty");
        if (FixedOutputDataFlags[documentType][lineNumber - SAMPLEFISCALPRINTERFXO_HEAD] == 1) {
            try {
                int cashier = Integer.parseInt(data);
                Dev.check(cashier <= 0 || 100000 <= cashier, JposConst.JPOS_E_ILLEGAL, "Cashier out of range: " + cashier);
            } catch (NumberFormatException e) {
                throw new JposException(JposConst.JPOS_E_ILLEGAL, "Cashier not numeric: " + data, e);
            }
        }
        Dev.check(FixedOutputDataFlags[documentType][lineNumber - SAMPLEFISCALPRINTERFXO_HEAD] == -2 && data.length() > MAXDESCRIPTIONLENGTH, JposConst.JPOS_E_ILLEGAL, "Secret too long: " + data.length());
        return super.printFixedOutput(documentType, lineNumber, data);
    }

    @Override
    public void printFixedOutput(PrintFixedOutput request) throws JposException {
        Dev.check(Dev.NonFiscalMinLineNo == -1, JposConst.JPOS_E_ILLEGAL, "Invalid output after last line");
        for (int i = Dev.NonFiscalMinLineNo; i < request.getLineNumber(); i++)
            Dev.check(FixedOutputDataFlags[request.getDocumentType()][i - SAMPLEFISCALPRINTERFXO_HEAD] >= 0, JposConst.JPOS_E_ILLEGAL, "Mandatory line " + i + " missing");
        String[] lines;
        String[][][] cmds = new String[0][][];
        switch (request.getDocumentType() * 10 + request.getLineNumber()) {
            case SAMPLEFISCALPRINTERFXO_SIGNON * 10 + SAMPLEFISCALPRINTERFXO_HEAD:
            case SAMPLEFISCALPRINTERFXO_SIGNOFF * 10 + SAMPLEFISCALPRINTERFXO_HEAD:
                lines = new String[request.getData().length() > 0 ? 3 : 2];
                lines[0] = Dev.centeredLine(new String[]{"", Dev.SignOnHeader, Dev.SignOffHeader}[request.getDocumentType()]);
                lines[1] = "";
                if (lines.length > 2)
                    lines[2] = String.format("%" + MAXFIXEDTEXTLENGTH + "s: %s", Dev.CashierName, request.getData());
                break;
            case SAMPLEFISCALPRINTERFXO_SIGNON * 10 + SAMPLEFISCALPRINTERFXO_ON_CASHIER:
            case SAMPLEFISCALPRINTERFXO_SIGNOFF * 10 + SAMPLEFISCALPRINTERFXO_OFF_CASHIER:
                lines = new String[]{String.format("%" + MAXFIXEDTEXTLENGTH + "s: %s", Dev.CashierID, request.getData())};
                cmds = Dev.addCommand(cmds, new String[]{"setCashier", request.getDocumentType() == SAMPLEFISCALPRINTERFXO_SIGNON ? request.getData() : "0"});
                break;
            case SAMPLEFISCALPRINTERFXO_SIGNOFF * 10 + SAMPLEFISCALPRINTERFXO_OFF_SECRET:
                lines = new String[]{String.format("%" + MAXFIXEDTEXTLENGTH + "s: %c%s%c", Dev.SecretText, SO, request.getData(), SI)};
                break;
            case SAMPLEFISCALPRINTERFXO_SIGNON * 10 + SAMPLEFISCALPRINTERFXO_ON_END:
            case SAMPLEFISCALPRINTERFXO_SIGNOFF * 10 + SAMPLEFISCALPRINTERFXO_OFF_END:
            default:
                lines = new String[0];
        }
        for (String line : lines) {
            cmds = Dev.addCommand(cmds, new String[]{"nfPrint", line});
        }
        if (Dev.executeCommands(0, cmds) != 1) {
            for (String[][] cmd : cmds) {
                commandErrorException(cmd, new long[]{NONFISCAL});
            }
        }
        Dev.NonFiscalMinLineNo = (int) Dev.AllowedDocumentLineTable[request.getDocumentType()][request.getLineNumber() - SAMPLEFISCALPRINTERFXO_HEAD];
    }

    @Override
    public void printNormal(PrintNormal request) throws JposException {
        Dev.PrintBuffer += removeControlCharacters(request.getData(), true);
        int nlindex;
        while (true) {
            nlindex = Dev.PrintBuffer.indexOf('\n');
            if (nlindex > MAXPRINTLINELENGTH || (nlindex < 0 && Dev.PrintBuffer.length() > MAXPRINTLINELENGTH)) {
                String[][] cmd = new String[][]{new String[]{"nfPrint", Dev.PrintBuffer.substring(0, MAXPRINTLINELENGTH)}, null};
                if (Dev.executeCommands(0, cmd) != 1)
                    commandErrorException(cmd, new long[]{NONFISCAL});
                Dev.PrintBuffer = Dev.PrintBuffer.substring(MAXPRINTLINELENGTH);
                continue;
            }
            if (nlindex >= 0) {
                String[][] cmd = new String[][]{new String[]{"nfPrint", Dev.PrintBuffer.substring(0, nlindex)}, null};
                if (Dev.executeCommands(0, cmd) != 1)
                    commandErrorException(cmd, new long[]{NONFISCAL});
                Dev.PrintBuffer = Dev.PrintBuffer.substring(nlindex + 1);
                continue;
            }
            break;
        }
    }

    @Override
    public PrintRecItem printRecItem(String description, long price, int quantity, int vatInfo, long unitPrice, String unitName) throws JposException {
        Dev.checkext(!removeControlCharacters(description, false).equals(description), FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description contains invalid characters");
        Dev.checkext(description.length() > MAXDESCRIPTIONLENGTH, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description too long");
        Dev.checkext(vatInfo < 1 || vatInfo > MAXVATINDEX || Dev.VatTable[vatInfo - 1] < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_VAT, "Invalid vatInfo: " + vatInfo);
        Dev.checkext(!unitName.equals(removeControlCharacters(unitName, false)), FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_QUANTITY, "unitName contains invalid characters");
        Dev.checkext(unitName.length() > MAXDIMENSIONLENGTH, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_QUANTITY, "UnitName too long: " + unitName);
        return super.printRecItem(description, price, quantity, vatInfo, unitPrice, unitName);
    }

    @Override
    public void printRecItem(PrintRecItem request) throws JposException {
        // fPrintItem text vatrate price quantity dimension quantitydecimals amount ""|Voids|Refunds
        String[][][]cmds = new String[0][][];
        if (request.AdditionalData == null) {
            // Initial call
            if (!request.getPreLine().equals(""))
                cmds = Dev.addCommand(cmds, new String[]{"fPrint", request.getPreLine()});
            int quantity = request.getQuantity() == 0 ? QUANTITYFACTOR : request.getQuantity();
            long price = request.getUnitPrice() == 0 ? request.getAmount() * quantity / QUANTITYFACTOR : request.getUnitPrice();
            int qdec = computeQuantityDecimals(quantity);
            cmds = Dev.addCommand(cmds, new String[]{
                    "fPrintItem",
                    request.getDescription(),
                    Integer.toString(request.getVatInfo()),
                    new BigDecimal(price).divide(new BigDecimal(CURRENCYFACTOR)).toString(),
                    new BigDecimal(quantity).divide(new BigDecimal(QUANTITYFACTOR)).toString(),
                    request.getUnitName(),
                    Integer.toString(qdec),
                    new BigDecimal(request.getAmount()).divide(new BigDecimal(CURRENCYFACTOR)).toString(),
                    ""
            });
            if (!request.getPostLine().equals(""))
                cmds = Dev.addCommand(cmds, new String[]{"fPrint", request.getPostLine()});
            request.AdditionalData = cmds;
        }
        else {
            cmds = removeSuccessfulCommands(request);
        }
        if (Dev.executeCommands(0, cmds) < cmds.length) {
            for (String[][]cmd : cmds)
                commandErrorException(cmd, new long[]{ITEMIZING});

        }
        Dev.VoidOnEndFiscal = false;
        Dev.SubtotalAdjustment = null;
    }

    private int computeQuantityDecimals(int quantity) {
        int qdec = 0;
        for (int i = QUANTITYFACTOR; i > 0; i /= 10) {
            if (quantity % i == 0)
                break;
            qdec++;
        }
        return qdec;
    }

    @Override
    public PrintRecItemAdjustment printRecItemAdjustment(int adjustmentType, String description, long amount, int vatInfo) throws JposException {
        Dev.checkext(description.length() > MAXDESCRIPTIONLENGTH, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description too long");
        Dev.checkext(vatInfo < 1 || vatInfo > MAXVATINDEX || Dev.VatTable[vatInfo - 1] < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_VAT, "Invalid vatInfo: " + vatInfo);
        return super.printRecItemAdjustment(adjustmentType, description, amount, vatInfo);
    }

    @Override
    public void printRecItemAdjustment(PrintRecItemAdjustment request) throws JposException {
        // fPrintAdjustment text amount percent
        String[][][]cmds = new String[0][][];
        if (request.AdditionalData == null) {
            // Initial call
            cmds = Dev.addCommand(cmds, new String[]{"get", "Position", "VAT", "Amount"});
            if (Dev.executeCommands(0, cmds) != 1 || cmds[0][1].length < 3) {
                commandErrorException(cmds[0], new long[]{ITEMIZING});
                throw new JposException(JposConst.JPOS_E_FAILURE, "Cannot retrieve VAT rate and amount of current position");
            }
            long amount = new BigDecimal(cmds[0][1][2]).multiply(new BigDecimal(CURRENCYFACTOR)).longValue();
            int vatidx = Integer.parseInt(cmds[0][1][1]);
            Dev.checkext(vatidx != request.getVatInfo(), FiscalPrinterConst.JPOS_EFPTR_BAD_VAT, "vatInfo does not match VAT rate of current position");
            cmds = new String[0][][];
            if (!request.getPreLine().equals(""))
                cmds = Dev.addCommand(cmds, new String[]{"fPrint", request.getPreLine()});
            boolean discount = Dev.member(request.getAdjustmentType(), new long[]{
                    FiscalPrinterConst.FPTR_AT_COUPON_PERCENTAGE_DISCOUNT,
                    FiscalPrinterConst.FPTR_AT_COUPON_AMOUNT_DISCOUNT,
                    FiscalPrinterConst.FPTR_AT_PERCENTAGE_DISCOUNT,
                    FiscalPrinterConst.FPTR_AT_AMOUNT_DISCOUNT
            });
            boolean percent = Dev.member(request.getAdjustmentType(), new long[]{
                    FiscalPrinterConst.FPTR_AT_PERCENTAGE_SURCHARGE,
                    FiscalPrinterConst.FPTR_AT_PERCENTAGE_DISCOUNT,
                    FiscalPrinterConst.FPTR_AT_COUPON_PERCENTAGE_DISCOUNT
            });
            Dev.check(!percent && discount && amount <= request.getAmount(), JposConst.JPOS_E_ILLEGAL, "Discount > amount");
            cmds = Dev.addCommand(cmds, new String[]{
                    "fPrintAdjustment",
                    request.getDescription(),
                    new BigDecimal(request.getAmount() * (discount ? -1 : 1)).divide(new BigDecimal(CURRENCYFACTOR)).toString(),
                    percent ? "1" : "0"
            });
            request.AdditionalData = cmds;
        }
        else {
            cmds = removeSuccessfulCommands(request);
        }
        if (Dev.executeCommands(0, cmds) < cmds.length) {
            for (String[][]cmd : cmds)
                commandErrorException(cmd, new long[]{ITEMIZING});
        }
    }

    private String[][][] removeSuccessfulCommands(JposOutputRequest request) {
        String[][][] cmds = (String[][][]) request.AdditionalData;
        request.AdditionalData = null;
        for (int i = 0; i < cmds.length; i++) {
            if (cmds[i][1] != null && cmds[i][1].length == 1 && cmds[i][1][0] != null && cmds[i][1][0].length() > DRAWER + 1 && cmds[i][1][0].charAt(0) == SUCCESS) {
                continue;
            }
            String[][][] newcmds = new String[cmds.length - i][][];
            System.arraycopy(cmds, i, newcmds, 0, newcmds.length);
            request.AdditionalData = newcmds;
            break;
        }
        return (String[][][]) request.AdditionalData;
    }

    @Override
    public PrintRecItemAdjustmentVoid printRecItemAdjustmentVoid(int adjustmentType, String description, long amount, int vatInfo) throws JposException {
        Dev.checkext(description.length() > MAXDESCRIPTIONLENGTH, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description too long");
        Dev.checkext(vatInfo < 1 || vatInfo > MAXVATINDEX || Dev.VatTable[vatInfo - 1] < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_VAT, "Invalid vatInfo: " + vatInfo);
        return super.printRecItemAdjustmentVoid(adjustmentType, description, amount, vatInfo);
    }

    @Override
    public void printRecItemAdjustmentVoid(PrintRecItemAdjustmentVoid request) throws JposException {
        // fPrintAdjustment text amount percent
        String[][][]cmds = new String[0][][];
        if (request.AdditionalData == null) {
            // Initial call
            cmds = Dev.addCommand(cmds, new String[]{"get", "Position", "VAT", "Amount"});
            if (Dev.executeCommands(0, cmds) != 1 || cmds[0][1].length < 3) {
                commandErrorException(cmds[0], new long[]{ITEMIZING});
                throw new JposException(JposConst.JPOS_E_FAILURE, "Cannot retrieve VAT rate and amount of current position");
            }
            long amount = new BigDecimal(cmds[0][1][2]).multiply(new BigDecimal(CURRENCYFACTOR)).longValue();
            int vatidx = Integer.parseInt(cmds[0][1][1]);
            Dev.checkext(vatidx != request.getVatInfo(), FiscalPrinterConst.JPOS_EFPTR_BAD_VAT, "vatInfo does not match VAT rate of current position");
            cmds = new String[0][][];
            if (!request.getPreLine().equals(""))
                cmds = Dev.addCommand(cmds, new String[]{"fPrint", request.getPreLine()});
            cmds = Dev.addCommand(cmds, new String[]{"fPrint", Dev.centeredLine(Dev.AdjustmentVoidText)});
            boolean discount = Dev.member(request.getAdjustmentType(), new long[]{
                    FiscalPrinterConst.FPTR_AT_COUPON_PERCENTAGE_DISCOUNT,
                    FiscalPrinterConst.FPTR_AT_COUPON_AMOUNT_DISCOUNT,
                    FiscalPrinterConst.FPTR_AT_PERCENTAGE_DISCOUNT,
                    FiscalPrinterConst.FPTR_AT_AMOUNT_DISCOUNT
            });
            boolean percent = Dev.member(request.getAdjustmentType(), new long[]{
                    FiscalPrinterConst.FPTR_AT_PERCENTAGE_SURCHARGE,
                    FiscalPrinterConst.FPTR_AT_PERCENTAGE_DISCOUNT,
                    FiscalPrinterConst.FPTR_AT_COUPON_PERCENTAGE_DISCOUNT
            });
            Dev.check(!percent && !discount && amount <= request.getAmount(), JposConst.JPOS_E_ILLEGAL, "Voided surcharge > amount");
            cmds = Dev.addCommand(cmds, new String[]{
                    "fPrintAdjustment",
                    request.getDescription(),
                    new BigDecimal(request.getAmount() * (discount ? 1 : -1)).divide(new BigDecimal(CURRENCYFACTOR)).toString(),
                    percent ? "2" : "0"
            });
            request.AdditionalData = cmds;
        }
        else {
            cmds = removeSuccessfulCommands(request);
        }
        if (Dev.executeCommands(0, cmds) < cmds.length) {
            for (String[][]cmd : cmds)
                commandErrorException(cmd, new long[]{ITEMIZING});
        }
    }

    @Override
    public PrintRecItemVoid printRecItemVoid(String description, long price, int quantity, int vatInfo, long unitPrice, String unitName) throws JposException {
        Dev.checkext(!removeControlCharacters(description, false).equals(description), FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description contains invalid characters");
        Dev.checkext(description.length() > MAXDESCRIPTIONLENGTH, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description too long");
        Dev.checkext(vatInfo < 1 || vatInfo > MAXVATINDEX || Dev.VatTable[vatInfo - 1] < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_VAT, "Invalid vatInfo: " + vatInfo);
        Dev.checkext(!unitName.equals(removeControlCharacters(unitName, false)), FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_QUANTITY, "unitName contains invalid characters");
        Dev.checkext(unitName.length() > MAXDIMENSIONLENGTH, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_QUANTITY, "UnitName too long: " + unitName);
        return super.printRecItemVoid(description, price, quantity, vatInfo, unitPrice, unitName);
    }

    @Override
    public void printRecItemVoid(PrintRecItemVoid request) throws JposException {
        // fPrintItem text vatrate price quantity dimension quantitydecimals amount ""|Voids|Refunds
        String[][][]cmds = new String[0][][];
        if (request.AdditionalData == null) {
            // Initial call
            cmds = Dev.addCommand(cmds, new String[]{"fPrintSubtotal", "0"});
            cmds = Dev.addCommand(cmds, new String[]{"get", "CurrentTotal", "VAT" + request.getVatInfo()});
            if (Dev.executeCommands(0, cmds) != cmds.length || cmds[1][1].length < 2) {
                for (String[][]cmd : cmds)
                    commandErrorException(cmd, new long[]{ITEMIZING});
                throw new JposException(JposConst.JPOS_E_FAILURE, "Cannot retrieve current total for VAT " + cmds[0][0][2]);
            }
            long amount = new BigDecimal(cmds[1][1][1]).multiply(new BigDecimal(CURRENCYFACTOR)).longValue();
            Dev.checkext(amount < request.getAmount(), FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_AMOUNT, "Amount on VAT rate " + request.getVatInfo() + " negative");
            cmds = new String[0][][];
            if (!request.getPreLine().equals(""))
                cmds = Dev.addCommand(cmds, new String[]{"fPrint", request.getPreLine()});
            int quantity = request.getQuantity() == 0 ? QUANTITYFACTOR : request.getQuantity();
            long price = request.getUnitPrice() == 0 ? request.getAmount() * quantity / QUANTITYFACTOR : request.getUnitPrice();
            int qdec = computeQuantityDecimals(quantity);
            cmds = Dev.addCommand(cmds, new String[]{
                    "fPrintItem",
                    request.getDescription(),
                    Integer.toString(request.getVatInfo()),
                    new BigDecimal(price).divide(new BigDecimal(CURRENCYFACTOR)).toString(),
                    new BigDecimal(quantity).divide(new BigDecimal(QUANTITYFACTOR)).toString(),
                    request.getUnitName(),
                    Integer.toString(qdec),
                    new BigDecimal(request.getAmount()).divide(new BigDecimal(CURRENCYFACTOR)).toString(),
                    "Voids"
            });
            if (!request.getPostLine().equals(""))
                cmds = Dev.addCommand(cmds, new String[]{"fPrint", request.getPostLine()});
            request.AdditionalData = cmds;
        }
        else {
            cmds = removeSuccessfulCommands(request);
        }
        if (Dev.executeCommands(0, cmds) < cmds.length) {
            for (String[][]cmd : cmds)
                commandErrorException(cmd, new long[]{ITEMIZING});

        }
    }

    @Override
    public PrintRecItemRefund printRecItemRefund(String description, long amount, int quantity, int vatInfo, long unitAmount, String unitName) throws JposException {
        Dev.checkext(!removeControlCharacters(description, false).equals(description), FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description contains invalid characters");
        Dev.checkext(description.length() > MAXDESCRIPTIONLENGTH, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description too long");
        Dev.checkext(vatInfo < 1 || vatInfo > MAXVATINDEX || Dev.VatTable[vatInfo - 1] < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_VAT, "Invalid vatInfo: " + vatInfo);
        Dev.checkext(!unitName.equals(removeControlCharacters(unitName, false)), FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_QUANTITY, "unitName contains invalid characters");
        Dev.checkext(unitName.length() > MAXDIMENSIONLENGTH, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_QUANTITY, "UnitName too long: " + unitName);
        return super.printRecItemRefund(description, amount, quantity, vatInfo, unitAmount, unitName);
    }

    @Override
    public void printRecItemRefund(PrintRecItemRefund request) throws JposException {
        // fPrintItem text vatrate price quantity dimension quantitydecimals amount ""|Voids|Refunds
        String[][][]cmds = new String[0][][];
        if (request.AdditionalData == null) {
            // Initial call
            cmds = Dev.addCommand(cmds, new String[]{"fPrintSubtotal", "0"});
            cmds = Dev.addCommand(cmds, new String[]{"get", "CurrentTotal", "VAT" + request.getVatInfo()});
            if (Dev.executeCommands(0, cmds) != cmds.length || cmds[1][1].length < 2) {
                for (String[][]cmd : cmds)
                    commandErrorException(cmd, new long[]{ITEMIZING});
                throw new JposException(JposConst.JPOS_E_FAILURE, "Cannot retrieve current total for VAT " + cmds[0][0][2]);
            }
            long amount = new BigDecimal(cmds[1][1][1]).multiply(new BigDecimal(CURRENCYFACTOR)).longValue();
            Dev.checkext(amount < request.getAmount(), FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_AMOUNT, "Amount on VAT rate " + request.getVatInfo() + " negative");
            cmds = new String[0][][];
            if (!request.getPreLine().equals(""))
                cmds = Dev.addCommand(cmds, new String[]{"fPrint", request.getPreLine()});
            int quantity = request.getQuantity() == 0 ? QUANTITYFACTOR : request.getQuantity();
            long price = request.getUnitPrice() == 0 ? request.getAmount() * quantity / QUANTITYFACTOR : request.getUnitPrice();
            int qdec = computeQuantityDecimals(quantity);
            cmds = Dev.addCommand(cmds, new String[]{
                    "fPrintItem",
                    request.getDescription(),
                    Integer.toString(request.getVatInfo()),
                    new BigDecimal(price).divide(new BigDecimal(CURRENCYFACTOR)).toString(),
                    new BigDecimal(quantity).divide(new BigDecimal(QUANTITYFACTOR)).toString(),
                    request.getUnitName(),
                    Integer.toString(qdec),
                    new BigDecimal(request.getAmount()).divide(new BigDecimal(CURRENCYFACTOR)).toString(),
                    "Refunds"
            });
            if (!request.getPostLine().equals(""))
                cmds = Dev.addCommand(cmds, new String[]{"fPrint", request.getPostLine()});
            request.AdditionalData = cmds;
        }
        else {
            cmds = removeSuccessfulCommands(request);
        }
        if (Dev.executeCommands(0, cmds) < cmds.length) {
            for (String[][]cmd : cmds)
                commandErrorException(cmd, new long[]{ITEMIZING});
        }
    }

    @Override
    public PrintRecItemRefundVoid printRecItemRefundVoid(String description, long amount, int quantity, int vatInfo, long unitAmount, String unitName) throws JposException {
        // No refund void, perform normal sale as workaround
        PrintRecItemRefundVoid req = new PrintRecItemRefundVoid(this, description, amount, 0, vatInfo, 0, "");
        req.AdditionalData = printRecItem(description, amount, quantity, vatInfo, unitAmount, unitName);
        return req;
    }

    @Override
    public void printRecItemRefundVoid(PrintRecItemRefundVoid request) throws JposException {
        // No refund void, perform normal sale as workaround
        PrintRecItem req = (PrintRecItem)request.AdditionalData;
        String[][][] cmds = new String[0][][];
        if (!request.getPreLine().equals(""))
            cmds = Dev.addCommand(cmds, new String[]{"fPrint", request.getPreLine()});
        cmds = Dev.addCommand(cmds, new String[]{"fPrint", Dev.centeredLine(Dev.RefundVoidText)});
        if (Dev.executeCommands(0, cmds) != cmds.length) {
            for (String[][] cmd : cmds)
                commandErrorException(cmd, new long[]{ITEMIZING});
        }
        printRecItem(req);
        if (!request.getPostLine().equals("")) {
            String[][] cmd = new String[][]{new String[]{"fPrint", request.getPostLine()}, null};
            if (Dev.executeCommands(0, cmd) != 1)
                commandErrorException(cmd, new long[]{ITEMIZING});
        }
    }

    @Override
    public PrintRecMessage printRecMessage(String message) throws JposException {
        Dev.checkext(!removeControlCharacters(message, false).equals(message), FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description contains invalid characters");
        if (MessageType == FiscalPrinterConst.FPTR_MT_CASHIER) {
            try {
                long i = Long.parseLong(message);
                Dev.checkext(i < 0 || i > MAXCASHIER, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Cashier must be between 0 and " + MAXCASHIER);
            } catch (NumberFormatException e) {
                throw new JposException(JposConst.JPOS_E_EXTENDED, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Cashier must be numeric", e);
            }
        }
        Dev.checkext(message.length() > MAXFISCALPRINTLINE, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description too long");
        return super.printRecMessage(message);
    }

    @Override
    public void printRecMessage(PrintRecMessage request) throws JposException {
        String[][] cmd = new String[][]{new String[]{null, request.getMessage()}, null};
        cmd[0][0] = request.getMessageType() == FiscalPrinterConst.FPTR_MT_CASHIER ? "setCashier" : "fPrint";
        Dev.checkext(MessageType == FiscalPrinterConst.FPTR_MT_CASHIER && PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT_ENDING, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Print cashier only at receipt ending");
        if (Dev.executeCommands(0, cmd) != 1) {
            commandErrorException(cmd, new long[]{ITEMIZING});
        }
        Dev.VoidOnEndFiscal = false;
    }

    @Override
    public PrintRecRefund printRecRefund(String description, long amount, int vatInfo) throws JposException {
        PrintRecRefund request = new PrintRecRefund(this, "", 0, 0);
        request.AdditionalData = printRecItemRefund(description, amount, 0, vatInfo, 0, "");
        return request;
    }

    @Override
    public void printRecRefund(PrintRecRefund request) throws JposException {
        PrintRecItemRefund req = (PrintRecItemRefund) request.AdditionalData;
        printRecItemRefund(req);
    }

    @Override
    public PrintRecRefundVoid printRecRefundVoid(String description, long amount, int vatInfo) throws JposException {
        PrintRecRefundVoid request = new PrintRecRefundVoid(this, "", 0, 0);
        request.AdditionalData = printRecItemRefundVoid(description, amount, 0, vatInfo, 0, "");
        return request;
    }

    @Override
    public void printRecRefundVoid(PrintRecRefundVoid request) throws JposException {
        PrintRecItemRefundVoid req = (PrintRecItemRefundVoid) request.AdditionalData;
        printRecItemRefundVoid(req);
    }

    @Override
    public void printRecSubtotal(PrintRecSubtotal request) throws JposException {
        // fPrintSubtotal print
        String[][][]cmds = new String[0][][];
        if (request.AdditionalData == null) {
            // Initial call
            cmds = Dev.addCommand(cmds, new String[]{
                    "fPrintSubtotal",
                    "0"
            });
            if (!request.getPostLine().equals(""))
                cmds = Dev.addCommand(cmds, new String[]{"fPrint", request.getPostLine()});
            request.AdditionalData = cmds;
        }
        else {
            cmds = removeSuccessfulCommands(request);
        }
        if (Dev.executeCommands(0, cmds) < cmds.length) {
            for (String[][]cmd : cmds)
                commandErrorException(cmd, new long[]{ITEMIZING});
        }
    }

    @Override
    public PrintRecSubtotalAdjustment printRecSubtotalAdjustment(int adjustmentType, String description, long amount) throws JposException {
        Dev.checkext(!removeControlCharacters(description, false).equals(description), FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description contains invalid characters");
        Dev.checkext(description.length() > MAXDESCRIPTIONLENGTH, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description too long");
        return super.printRecSubtotalAdjustment(adjustmentType, description, amount);
    }

    @Override
    public void printRecSubtotalAdjustment(PrintRecSubtotalAdjustment request) throws JposException {
        // fPrintSubtotal print
        // fPrintAdjustment text amount percent
        String[][][]cmds = new String[0][][];
        if (request.AdditionalData == null) {
            // Initial call
            cmds = Dev.addCommand(cmds, new String[]{"fPrintSubtotal", "1"});
            boolean discount = Dev.member(request.getAdjustmentType(), new long[]{
                    FiscalPrinterConst.FPTR_AT_COUPON_PERCENTAGE_DISCOUNT,
                    FiscalPrinterConst.FPTR_AT_COUPON_AMOUNT_DISCOUNT,
                    FiscalPrinterConst.FPTR_AT_PERCENTAGE_DISCOUNT,
                    FiscalPrinterConst.FPTR_AT_AMOUNT_DISCOUNT
            });
            boolean percent = Dev.member(request.getAdjustmentType(), new long[]{
                    FiscalPrinterConst.FPTR_AT_PERCENTAGE_SURCHARGE,
                    FiscalPrinterConst.FPTR_AT_PERCENTAGE_DISCOUNT,
                    FiscalPrinterConst.FPTR_AT_COUPON_PERCENTAGE_DISCOUNT
            });
            if (!request.getPreLine().equals(""))
                cmds = Dev.addCommand(cmds, new String[]{"fPrint", request.getPreLine()});
            cmds = Dev.addCommand(cmds, new String[]{
                    "fPrintAdjustment",
                    request.getDescription(),
                    new BigDecimal(request.getAmount() * (discount ? -1 : 1)).divide(new BigDecimal(CURRENCYFACTOR)).toString(),
                    percent ? "1" : "0"
            });
            request.AdditionalData = cmds;
        }
        else {
            cmds = removeSuccessfulCommands(request);
        }
        if (Dev.executeCommands(0, cmds) < cmds.length) {
            for (String[][]cmd : cmds)
                commandErrorException(cmd, new long[]{ITEMIZING});
        }
        Dev.SubtotalAdjustment = request;
    }

    @Override
    public PrintRecSubtotalAdjustVoid printRecSubtotalAdjustVoid(int adjustmentType, long amount) throws JposException {
        checkPreviousSubtotalAdjustment(!AsyncMode, adjustmentType, amount);
        return super.printRecSubtotalAdjustVoid(adjustmentType, amount);
    }

    @Override
    public void printRecSubtotalAdjustVoid(PrintRecSubtotalAdjustVoid request) throws JposException {
        // fPrintSubtotal print
        // fPrintAdjustment text amount percent
        checkPreviousSubtotalAdjustment(AsyncMode, request.getAdjustmentType(), request.getAmount());
        String[][][]cmds = new String[0][][];
        if (request.AdditionalData == null) {
            // Initial call
            boolean discount = Dev.member(request.getAdjustmentType(), new long[]{
                    FiscalPrinterConst.FPTR_AT_COUPON_PERCENTAGE_DISCOUNT,
                    FiscalPrinterConst.FPTR_AT_COUPON_AMOUNT_DISCOUNT,
                    FiscalPrinterConst.FPTR_AT_PERCENTAGE_DISCOUNT,
                    FiscalPrinterConst.FPTR_AT_AMOUNT_DISCOUNT
            });
            boolean percent = Dev.member(request.getAdjustmentType(), new long[]{
                    FiscalPrinterConst.FPTR_AT_PERCENTAGE_SURCHARGE,
                    FiscalPrinterConst.FPTR_AT_PERCENTAGE_DISCOUNT,
                    FiscalPrinterConst.FPTR_AT_COUPON_PERCENTAGE_DISCOUNT
            });
            if (!request.getPreLine().equals(""))
                cmds = Dev.addCommand(cmds, new String[]{"fPrint", request.getPreLine()});
            cmds = Dev.addCommand(cmds, new String[]{"fPrint", Dev.centeredLine(Dev.AdjustmentVoidText)});
            cmds = Dev.addCommand(cmds, new String[]{
                    "fPrintAdjustment",
                    Dev.SubtotalAdjustment.getDescription(),
                    new BigDecimal(request.getAmount() * (discount ? 1 : -1)).divide(new BigDecimal(CURRENCYFACTOR)).toString(),
                    percent ? "2" : "0"
            });
            request.AdditionalData = cmds;
        }
        else {
            cmds = removeSuccessfulCommands(request);
        }
        if (Dev.executeCommands(0, cmds) < cmds.length) {
            for (String[][]cmd : cmds)
                commandErrorException(cmd, new long[]{ITEMIZING});
        }
    }

    private void checkPreviousSubtotalAdjustment(boolean checkNow, int adjustmentType, long amount) throws JposException {
        if (checkNow) {
            Dev.checkext(Dev.SubtotalAdjustment == null, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "No previous subtotal adjustment");
            Dev.checkext(Dev.SubtotalAdjustment.getAmount() != amount, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_AMOUNT, "Amount does not match previous adjustment");
            Dev.checkext(Dev.SubtotalAdjustment.getAdjustmentType() != adjustmentType, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_AMOUNT, "Adjustment type does not match previous adjustment type");
        }
    }

    @Override
    public PrintRecTaxID printRecTaxID(String id) throws JposException {
        throw new JposException(JposConst.JPOS_E_ILLEGAL, "Printing tax ID not supported");
    }

    @Override
    public PrintRecTotal printRecTotal(long total, long payment, String description) throws JposException {
        Dev.checkext(!removeControlCharacters(description, false).equals(description), FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description contains invalid characters");
        Dev.checkext(description.length() > MAXDESCRIPTIONLENGTH, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description too long");
        return super.printRecTotal(total, payment, description);
    }

    @Override
    public void printRecTotal(PrintRecTotal request) throws JposException {
        // fPrintTotal
        // fPrintPayment text amount
        String[][][]cmds = new String[0][][];
        long[]allowedStates = new long[]{
                FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT_TOTAL,
                FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT_ENDING
        };
        if (request.AdditionalData == null) {
            // Initial call
            if (Dev.getCurrentState()[RECEIPT] == ITEMIZING)
                cmds = Dev.addCommand(cmds, new String[]{"fPrintTotal"});
            if (request.getPayment() > 0) {
                cmds = Dev.addCommand(cmds, new String[]{
                        "fPrintPayment",
                        request.getDescription(),
                        new BigDecimal(request.getPayment()).divide(new BigDecimal(CURRENCYFACTOR)).toString(),
                });
            }
            if (!request.getPostLine().equals(""))
                cmds = Dev.addCommand(cmds, new String[]{"fPrint", request.getPostLine()});
            request.AdditionalData = cmds;
        }
        else {
            cmds = removeSuccessfulCommands(request);
        }
        if (Dev.executeCommands(0, cmds) < cmds.length) {
            for (String[][]cmd : cmds)
                commandErrorException(cmd, new long[]{ITEMIZING});
        }
        attachWaiter();
        Dev.PollWaiter.signal();
        waitWaiter(SyncObject.INFINITE);
        releaseWaiter();
        Dev.checkMember(PrinterState, allowedStates, JposConst.JPOS_E_FAILURE, "Could not leave itemizing state");
    }

    @Override
    public PrintRecVoid printRecVoid(String description) throws JposException {
        Dev.checkext(!removeControlCharacters(description, false).equals(description), FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description contains invalid characters");
        Dev.checkext(description.length() > MAXFISCALPRINTLINE, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description too long");
        return super.printRecVoid(description);
    }

    @Override
    public void printRecVoid(PrintRecVoid request) throws JposException {
        // fAbort text
        long[] allowed = new long[]{FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT, FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT_TOTAL};
        Dev.checkext(!Dev.member(PrinterState, allowed), FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Wrong printer state: " + PrinterState);
        String[][]cmd = new String[][]{new String[]{"fAbort", request.getDescription()}, null};
        if (Dev.executeCommands(0, cmd) != 1)
            commandErrorException(cmd, new long[]{ITEMIZING, PAYING, FINALIZING});
        attachWaiter();
        Dev.PollWaiter.signal();
        waitWaiter(SyncObject.INFINITE);
        releaseWaiter();
        Dev.check(PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT_ENDING, JposConst.JPOS_E_FAILURE, "Could not enter receipt ending state");
        Dev.VoidOnEndFiscal = false;
    }

    @Override
    public void updateState(boolean fromDeviceEnabled) {
        char[] state = Dev.getCurrentState();
        if (PowerNotify == JposConst.JPOS_PN_ENABLED) {
            int value = state.length <= DRAWER ? JposConst.JPOS_PS_OFF_OFFLINE : JposConst.JPOS_PS_ONLINE;
            new JposStatusUpdateEvent(EventSource, value).setAndCheckStatusProperties();
        }
        if (state.length > DRAWER) {
            updatePaperStates(state[PRINTER], fromDeviceEnabled);
            updatePeriodStates((state[DAY] == OPENED) != DayOpened);
            updateTrainingMode(state, fromDeviceEnabled);
            if (fromDeviceEnabled) {
                System.arraycopy(Dev.VatTable, 0, Dev.NewVatTable, 0, Dev.NewVatTable.length);
            }
        }
        else {
            if (PrinterState != FiscalPrinterConst.FPTR_PS_LOCKED) {
                PrinterState = FiscalPrinterConst.FPTR_PS_LOCKED;
                EventSource.logSet("PrinterState");
            }
        }
    }

    private void updatePeriodStates(boolean dayOpenChanged) {
        if (dayOpenChanged) {
            DayOpened = !DayOpened;
            EventSource.logSet("DayOpened");
        }
        if (MAXPERIOD - Dev.CurrentPeriod != RemainingFiscalMemory) {
            RemainingFiscalMemory = MAXPERIOD - Dev.CurrentPeriod;
            EventSource.logSet("RemainingFiscalMemory");
        }
    }

    private void updateTrainingMode(char[] state, boolean fromDeviceEnabled) {
        updateReceiptState(state[RECEIPT], fromDeviceEnabled && state[RECEIPT] != CLOSED);
        if (TrainingModeActive != (state[TRAININGMODE] == ON)) {
            TrainingModeActive = !TrainingModeActive;
            EventSource.logSet("TrainingModeActive");
            if (fromDeviceEnabled && PowerState != JposConst.JPOS_PS_OFF) {
                PowerState = JposConst.JPOS_PS_OFF;
                EventSource.logSet("PowerState");
            }
        }
    }

    private void updateReceiptState(char recState, boolean fromDeviceEnabled) {
        if (    (PrinterState == FiscalPrinterConst.FPTR_PS_MONITOR) != (recState == CLOSED) ||
                (PrinterState == FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT) != (recState == ITEMIZING) ||
                (PrinterState == FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT_TOTAL) != (recState == PAYING) ||
                (PrinterState == FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT_ENDING) != (recState == FINALIZING) ||
                (PrinterState == FiscalPrinterConst.FPTR_PS_NONFISCAL || PrinterState == FiscalPrinterConst.FPTR_PS_FIXED_OUTPUT) != (recState == NONFISCAL))
        {
            if (fromDeviceEnabled) {
                PowerState = JposConst.JPOS_PS_OFF;
                EventSource.logSet("PowerState");
            }
            switch (recState) {
                case CLOSED:
                    PrinterState = FiscalPrinterConst.FPTR_PS_MONITOR;
                    break;
                case ITEMIZING:
                    PrinterState = FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT;
                    break;
                case PAYING:
                    PrinterState = FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT_TOTAL;
                    break;
                case FINALIZING:
                    PrinterState = FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT_ENDING;
                    break;
                case NONFISCAL:
                    PrinterState = Dev.NonFiscalMinLineNo > 0 ? FiscalPrinterConst.FPTR_PS_FIXED_OUTPUT : FiscalPrinterConst.FPTR_PS_NONFISCAL;
            }
            EventSource.logSet("PrinterState");
        }
    }

    private void updatePaperStates(char c, boolean fromDeviceEnabled) {
        FiscalPrinterStatusUpdateEvent ev = null;
        switch (c) {
            case OK:
                if (RecEmpty || RecNearEnd)
                    ev = new FiscalPrinterStatusUpdateEvent(EventSource, FiscalPrinterConst.FPTR_SUE_REC_PAPEROK);
                break;
            case NEAREND:
                if (RecEmpty || !RecNearEnd)
                    ev = new FiscalPrinterStatusUpdateEvent(EventSource, FiscalPrinterConst.FPTR_SUE_REC_NEAREMPTY);
                break;
            default:
                if (!RecEmpty)
                    ev = new FiscalPrinterStatusUpdateEvent(EventSource, FiscalPrinterConst.FPTR_SUE_REC_EMPTY);
        }
        if (ev != null) {
            if (fromDeviceEnabled)
                ev.setAndCheckStatusProperties();
            else {
                try {
                    Dev.handleEvent(ev);
                } catch (JposException e) {}
            }
        }
    }
}
