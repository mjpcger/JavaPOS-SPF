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
import jpos.*;

import java.math.*;
import java.text.*;
import java.util.Date;
import java.util.HashMap;

import static de.gmxhome.conrad.jpos.jpos_base.SyncObject.INFINITE;
import static java.math.RoundingMode.*;
import static javax.swing.JOptionPane.*;
import static jpos.FiscalPrinterConst.*;
import static jpos.JposConst.*;
import static SampleFiscalPrinter.Device.*;

/**
 * Class implementing the JposFiscalPrinterInterface for the sample fiscal printer.
 */
class FiscalPrinter extends FiscalPrinterProperties implements StatusUpdater {
    private final SampleFiscalPrinter.Device Dev;

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
        if (level == JPOS_CH_INTERNAL) {
            CheckHealthText = "Internal CheckHealth: OK";
            return;
        }
        if (level == JPOS_CH_INTERACTIVE) {
            synchronizedMessageBox("Press OK to start health test.", "CheckHealth", INFORMATION_MESSAGE);
        }
        CheckHealthText = (level == JPOS_CH_EXTERNAL ? "Externel" : "Interactive") + " CheckHealth: ";
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
            check((data[0] & ~1) != 0, JPOS_E_ILLEGAL, "Invalid data, must be 0 or 1");
            Dev.NonFiscalReceiptWithTrailer = data[0] == 0 ? "0" : "1";
            break;
        case SAMPLEFISCALPRINTERDIO_FISCALIZE:
            EventSource.checkEnabled();
            check("".equals(Dev.SerialNumber), JPOS_E_ILLEGAL, "Serial number not set");
            check(Dev.CurrentPeriod > 0, JPOS_E_ILLEGAL, "Printer just fiscalized");
            String[][] cmd = {new String[]{"fiscalize"}, null};
            if (Dev.executeCommands(0, cmd) != 1) {
                commandErrorException(cmd, new long[]{CLOSED});
            }
            attachWaiter();
            Dev.PollWaiter.signal();
            waitWaiter(INFINITE);
            releaseWaiter();
        }
        return null;
    }

    @Override
    public void additionalHeader(String header) throws JposException {
        check(header.length() > MAXFISCALPRINTLINE, JPOS_E_ILLEGAL, "Line too long: " + header);
        check(!removeControlCharacters(header, false).equals(header), JPOS_E_ILLEGAL, "Header must not contain control characters: " + header);
        super.additionalHeader(header);
    }

    @Override
    public void additionalTrailer(String trailer) throws JposException {
        check(trailer.length() > MAXFISCALPRINTLINE, JPOS_E_ILLEGAL, "Line too long: " + trailer);
        check(!removeControlCharacters(trailer, false).equals(trailer), JPOS_E_ILLEGAL, "Text must not contain control characters: " + trailer);
        super.additionalTrailer(trailer);
    }

    @Override
    public void dateType(int type) throws JposException {
        long[] allowed = {
                FPTR_DT_EOD,
                FPTR_DT_RTC,
                FPTR_DT_START,
                FPTR_DT_CONF
        };
        checkMember(type, allowed, JPOS_E_ILLEGAL, "Unsupported date type: " + type);
        super.dateType(type);
    }

    @Override
    public void messageType(int type) throws JposException {
        long[] allowedtypes = {
                FPTR_MT_FREE_TEXT,
                FPTR_MT_CASHIER
        };
        checkMember(type, allowedtypes, JPOS_E_ILLEGAL, "Unsupported message type: " + type);
        super.messageType(type);
    }

    @Override
    public void postLine(String text) throws JposException {
        check(text.length() > MAXFISCALPRINTLINE, JPOS_E_ILLEGAL, "Line too long: " + text);
        check(!removeControlCharacters(text, false).equals(text), JPOS_E_ILLEGAL, "Text must not contain control characters: " + text);
        super.postLine(text);
    }

    @Override
    public void preLine(String text) throws JposException {
        check(text.length() > MAXFISCALPRINTLINE, JPOS_E_ILLEGAL, "Line too long: " + text);
        check(!removeControlCharacters(text, false).equals(text), JPOS_E_ILLEGAL, "Text must not contain control characters: " + text);
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
        check(state[RECEIPT] != CLOSED, JPOS_E_ILLEGAL, "Printer in receipt");
        if (!TrainingModeActive) {
            Object e = processCommand(new String[]{"setTrainingMode", "1"});
            if (e instanceof JposException)
                throw new JposException (JPOS_E_FAILURE, "Setting training mode failed: " + ((JposException)e).getMessage(), (JposException) e);
            attachWaiter();
            Dev.PollWaiter.signal();
            waitWaiter(INFINITE);
            releaseWaiter();
            check(!TrainingModeActive, JPOS_E_FAILURE, "Could not activate training mode");
        }
    }

    @Override
    public void endTraining() throws JposException {
        char[] state = Dev.getCurrentState();
        checkOperational(state);
        check(state[RECEIPT] != CLOSED, JPOS_E_ILLEGAL, "Printer in receipt");
        if (TrainingModeActive) {
            Object e = processCommand(new String[]{"setTrainingMode", "0"});
            if (e instanceof JposException)
                throw new JposException (JPOS_E_FAILURE, "Resetting training mode failed: " + ((JposException)e).getMessage(), (JposException) e);
            attachWaiter();
            Dev.PollWaiter.signal();
            waitWaiter(INFINITE);
            releaseWaiter();
            check(TrainingModeActive, JPOS_E_FAILURE, "Could not deactivate training mode");
        }
    }

    @Override
    public void beginFixedOutput(int station, int documentType) throws JposException {
        checkMember(documentType, Dev.AllowedDocumentLineTable[0], JPOS_E_ILLEGAL, "Invalid document type: " + documentType);
        beginNonFiscalOutput(documentType, "fixed output", FPTR_PS_FIXED_OUTPUT);
    }

    @Override
    public void beginNonFiscal() throws JposException {
        beginNonFiscalOutput(0, "non-fiscal", FPTR_PS_NONFISCAL);
    }

    /**
     * Checks whether a status array represents an operational device.
     * @param state State of the device.
     * @throws JposException    If the device is not in an operational state.
     */
    private void checkOperational(char[] state) throws JposException {
        check(state.length <= DRAWER, JPOS_E_FAILURE, "Printer not online");
        check(state[FISCAL] == NOTFISCALIZED, JPOS_E_ILLEGAL, "Printer not fiscalized");
        check(state[FISCAL] == FISCALBLOCK, JPOS_E_ILLEGAL, "Printer blocked");
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
            return new JposException(JPOS_E_FAILURE, status, reason + ": Command timed out");
        if (resp.length == 0)
            return new JposException(JPOS_E_FAILURE, status, reason + ": Invalid frame contents");
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
                    return new JposException(JPOS_E_ILLEGAL, status, reason + ": Illegal parameter " + index + ": " + cmd[(int)index]);
                return new JposException(JPOS_E_FAILURE, status, reason + ": Check status (" + status + " - " + index + ")");
            }
            else if (resp.length == 3)
                return new JposException(JPOS_E_ILLEGAL, status, reason + ": " + resp[2]);
        }
        return resp;
    }

    private void beginNonFiscalOutput(int documentType, String receiptType, int targetstate) throws JposException {
        char[] state = Dev.getCurrentState();
        checkOperational(state);
        check(state[RECEIPT] != CLOSED, JPOS_E_ILLEGAL, "Printer in receipt");
        Dev.FixedOutputDocument = documentType;
        Dev.NonFiscalMinLineNo = documentType > 0 ? SAMPLEFISCALPRINTERFXO_HEAD : 0;
        Dev.PrintBuffer = "";
        String[][][] cmds = {};
        cmds = Dev.addCommand(cmds, new String[]{"setHeaderLine", Long.toString(MAXHEADERLINES + 1), ""});
        cmds = Dev.addCommand(cmds, new String[]{"nfStart"});
        if (Dev.executeCommands(0, cmds) != cmds.length){
            for (String[][]cmd : cmds)
                commandErrorException(cmd, new long[]{NONFISCAL});
        }
        attachWaiter();
        Dev.PollWaiter.signal();
        waitWaiter(INFINITE);
        releaseWaiter();
        check(PrinterState != targetstate, JPOS_E_FAILURE, "Could not enter " + receiptType + " receipt");
    }

    @Override
    public void endFixedOutput() throws JposException {
        endNonFiscalOutput(FPTR_PS_FIXED_OUTPUT, "fixed output");
    }

    @Override
    public void endNonFiscal() throws JposException {
        endNonFiscalOutput(FPTR_PS_NONFISCAL, "non-fiscal");
    }

    private void endNonFiscalOutput(int startState, String receiptType) throws JposException {
        char[] state = Dev.getCurrentState();
        checkOperational(state);
        check(PrinterState != startState, JPOS_E_ILLEGAL, "Printer not in " + receiptType + " receipt");
        String[][][] cmds = {};
        waitAsyncFinished();
        checkext(Dev.NonFiscalMinLineNo > 0, JPOS_EFPTR_BAD_LENGTH, "Last line not been reached");
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
        waitWaiter(INFINITE);
        releaseWaiter();
        check(PrinterState != FPTR_PS_MONITOR, JPOS_E_FAILURE, "Could not leave " + receiptType + " receipt");
    }

    private void waitAsyncFinished() {
        SyncObject idlewaiter = OutputRequest.setIdleWaiter(Dev);
        if (idlewaiter != null)
            idlewaiter.suspend(INFINITE);
    }

    @Override
    public void beginFiscalReceipt(boolean printHeader) throws JposException {
        char[] state = Dev.getCurrentState();
        checkOperational(state);
        check(state[RECEIPT] != CLOSED, JPOS_E_ILLEGAL, "Printer in receipt");
        String[][][] cmds = {
                new String[][]{new String[]{"setHeaderLine", Long.toString(MAXHEADERLINES + 1), AdditionalHeader}, null},
                new String[][]{new String[]{"fStart"}, null}
        };
        if (Dev.executeCommands(0, cmds) != cmds.length) {
            for (String[][]cmd : cmds)
                commandErrorException(cmd, new long[]{CLOSED});
        }
        attachWaiter();
        Dev.PollWaiter.signal();
        waitWaiter(INFINITE);
        releaseWaiter();
        check(PrinterState != FPTR_PS_FISCAL_RECEIPT, JPOS_E_FAILURE, "Could not enter fiscal receipt");
        Dev.VoidOnEndFiscal = true;
        Dev.SubtotalAdjustment = null;
    }


    @Override
    public void endFiscalReceipt(boolean printHeader) throws JposException {
        char[] state = Dev.getCurrentState();
        checkOperational(state);
        String[][][] cmds = {};
        waitAsyncFinished();
        if (Dev.VoidOnEndFiscal) {
            cmds = Dev.addCommand(cmds, new String[]{"fPrintTotal"});
        }
        else
            check(Dev.getCurrentState()[RECEIPT] != FINALIZING, JPOS_E_ILLEGAL, "Printer not in fiscal receipt ending state");
        cmds = Dev.addCommand(cmds, new String[]{"setTrailerLine", Long.toString(MAXTRAILERLINES + 1), AdditionalTrailer});
        cmds = Dev.addCommand(cmds, new String[]{"fEnd"});
        if (Dev.executeCommands(0, cmds) != cmds.length) {
            for (String[][] cmd : cmds) {
                commandErrorException(cmd, new long[]{CLOSED, FINALIZING});
            }
        }
        attachWaiter();
        Dev.PollWaiter.signal();
        waitWaiter(INFINITE);
        releaseWaiter();
        check(PrinterState != FPTR_PS_MONITOR, JPOS_E_FAILURE, "Could not leave fiscal receipt");
        Dev.SubtotalAdjustment = null;
    }

    @Override
    public void getData(int dataItem, int[] optArgs, String[] data) throws JposException {
        if (dataItem == FPTR_GD_PRINTER_ID) {
            data[0] = Dev.SerialNumber;
            return;
        }
        throw new JposException(JPOS_E_ILLEGAL, "Unsupported dataItem: " + dataItem);
    }

    @Override
    public void getData(int dataItem, int[] optArgs, int[] data) throws JposException {
        switch (dataItem) {
        case FPTR_GD_RECEIPT_NUMBER:
            getCount(new String[]{"get", "Total", "Fiscal"}, data);
            return;
        case FPTR_GD_Z_REPORT:
            data[0] = Dev.CurrentPeriod;
            return;
        case FPTR_GD_DESCRIPTION_LENGTH:
            getDescriptionLength(optArgs[0], data);
            return;
        }
        throw new JposException(JPOS_E_ILLEGAL, "Unsupported dataItem: " + dataItem);
    }

    @Override
    public void getData(int dataItem, int[] optArgs, long[] data) throws JposException {
        switch (dataItem) {
        case FPTR_GD_CURRENT_TOTAL:
            String[][] cmd = {new String[]{"getCurrentTotal", "0"}, null};
            if (Dev.executeCommands(0, cmd) != 1 || !returnValuePresent(cmd)) {
                commandErrorException(cmd, new long[]{ITEMIZING, PAYING, FINALIZING});
                throw new JposException(JPOS_E_FAILURE, "Total not available");
            }
            data[0] = new BigDecimal(cmd[1][1]).multiply(new BigDecimal(CURRENCYFACTOR)).longValueExact();
            break;
        case FPTR_GD_DAILY_TOTAL:
            getTotal(new String[]{"get", "Total", "All"}, data, "daily total");
            break;
        case FPTR_GD_GRAND_TOTAL:
            getTotal(new String[]{"get", "GrandTotal"}, data, "grand total");
            break;
        case FPTR_GD_NOT_PAID:
            getTotal(new String[]{"get", "Total", "Aborts"}, data, "aborted receipt total");
            break;
        case FPTR_GD_REFUND:
            getTotal(new String[]{"get", "Total", "Refunds"}, data, "refund total");
            break;
        default:
            throw new JposException(JPOS_E_ILLEGAL, "Unsupported dataItem: " + dataItem);
        }
    }

    private void getCount(String[] cmd, int[] data) throws JposException {
        String[][] command = {cmd, null};
        if (Dev.executeCommands(0, command) == 1 && returnValuePresent(command)) {
            try {
                data[0] = Integer.parseInt(command[1][1]);
                return;
            } catch (NumberFormatException e) {
                throw new JposException(JPOS_E_FAILURE, "Could not retrieve valid fiscal receipt number" + ": " + e.getMessage(), e);
            }
        }
        throw new JposException(JPOS_E_FAILURE, "Could not retrieve " + "fiscal receipt number");
    }

    private void getTotal(String[] cmd, long[] data, String what) throws JposException {
        String[][] command = {cmd, null};
        if (Dev.executeCommands(0, command) == 1 && returnValuePresent(command)) {
            try {
                data[0] = new BigDecimal(command[1][1]).multiply(new BigDecimal(CURRENCYFACTOR)).longValueExact();
            } catch (NumberFormatException | ArithmeticException e) {
                throw new JposException(JPOS_E_FAILURE, "Could not retrieve valid " + what + ": " + e.getMessage(), e);
            }
            return;
        }
        throw new JposException(JPOS_E_FAILURE, "Could not retrieve " + what);
    }

    private void getDescriptionLength(int optArg, int[] data) throws JposException {
        switch (optArg) {
        case FPTR_DL_ITEM:
        case FPTR_DL_ITEM_ADJUSTMENT:
        case FPTR_DL_REFUND:
        case FPTR_DL_REFUND_VOID:
        case FPTR_DL_SUBTOTAL_ADJUSTMENT:
        case FPTR_DL_TOTAL:
        case FPTR_DL_VOID_ITEM:
            data[0] = MAXDESCRIPTIONLENGTH;
            break;
        case FPTR_DL_VOID:
            data[0] = MAXFISCALPRINTLINE;
            break;
        default:
            throw new JposException(JPOS_E_ILLEGAL, "Unsupported optArgs for description length: " + optArg);
        }
    }

    @Override
    public void getDate(String[] date) throws JposException {
        String[][]command;
        int result;
        switch (DateType) {
        case FPTR_DT_RTC:
            result = Dev.executeCommands(0, command = new String[][]{ { "getDate"}, null });
            check(result != 1 || !returnValuePresent(command), JPOS_E_ILLEGAL, "RTC currently not available");
            String rtc = command[1][1];
            date[0] = rtc.substring(6, 8) + rtc.substring(4, 6) + rtc.substring(0, 4) + rtc.substring(8, 12);
            break;
        case FPTR_DT_START:
            result = Dev.executeCommands(0, command = new String[][]{
                    DayOpened ? new String[]{"get", "Total", "Date"} : new String[]{"get", "Memory", Long.toString(Dev.CurrentPeriod - 1), "Date"},
                    null
            });
            check(result != 1, JPOS_E_ILLEGAL, "Start date currently not available");
            date[0] = new SimpleDateFormat("ddMMyyyyHHmm").format(new Date(Long.parseLong(command[1][1]) * 1000));
            break;
        case FPTR_DT_CONF:
            getLastTicketDateOfPeriod(date, 1, "CONF");
            break;
        case FPTR_DT_EOD:
            getLastTicketDateOfPeriod(date, Dev.CurrentPeriod, "EOD");
        }
    }

    private void getLastTicketDateOfPeriod(String[] date, int period, String what) throws JposException {
        check(period == 0, JPOS_E_ILLEGAL, "No " + what + " date available");
        try {
            date[0] = new SimpleDateFormat("ddMMyyyyHHmm").format(getLastTicketDate(period - 1));
        }
        catch (Exception e) {
            throw new JposException(JPOS_E_ILLEGAL, what + " date not available", e);
        }
    }

    private Date getLastTicketDate(int period) throws JposException {
        String[][] command;
        Dev.executeCommands(0, command = new String[][]{
                {"get", "Memory", Long.toString(period), "Normal", "Fiscal"}, null
        });
        String recno = Long.toString(Long.parseLong(command[1][1]) + Long.parseLong(command[1][2]));
        Dev.executeCommands(0, command = new String[][]{
                {"retrieveJournal", Long.toString(period), recno, recno, "0"}, null
        });
        return new Date(Long.parseLong(command[1][2]) * 1000L);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void getTotalizer(int vatID, int optArgs, String[] data) throws JposException {
        throw new JposException(JPOS_E_ILLEGAL, "Totalizer not supported: " + optArgs);
    }

    @Override
    public void getVatEntry(int vatID, int optArgs, int[] vatRate) throws JposException {
        check(vatID < 1 || vatID > MAXVATINDEX, JPOS_E_ILLEGAL, "Invalid vatID: " + vatID);
        Object e = processCommand(new String[]{"get", "VAT", Integer.toString(vatID)});
        if (e instanceof String[]) {
            String[] resp = (String[]) e;
            try {
                if (resp[0].charAt(0) == SUCCESS && resp.length == 2) {
                    int rate = new BigDecimal(resp[1]).multiply(new BigDecimal(CURRENCYFACTOR)).intValueExact();
                    vatRate[0] = rate < 0 ? -1 : rate;
                    return;
                }
            } catch (Exception ee) {
                e = ee;
            }
        }
        if (e instanceof Exception)
            throw new JposException(JPOS_E_FAILURE, "Error getting VAT rate " + vatID + ": " + ((Exception)e).getMessage(), (Exception) e);
        throw new JposException(JPOS_E_FAILURE, "Error getting VAT rate " + vatID);
    }

    @Override
    public void printPeriodicTotalsReport(String date1, String date2) throws JposException {
        int start = getStartPeriod(date1, 1);
        check(start == 0, JPOS_E_ILLEGAL, "No period starting after " + date1 + " found");
        int end = getEndPeriod(date2, start);
        check(end == 0, JPOS_E_ILLEGAL, "No period ending before " + date2 + " found");
        check(start > end, JPOS_E_ILLEGAL, "No period starting before " + date1 + " and ending after " + date2 + " found");
        Object e = processCommand(new String[]{"report", Long.toString(start), Long.toString(end), "1"});
        if (e instanceof JposException) {
            char[] state = Integer.toString(((JposException) e).getErrorCodeExtended()).toCharArray();
            if (state[0] == '9' || state.length  != DRAWER + 2)
                throw new JposException(JPOS_E_FAILURE, "Communication error", (Exception) e);
            checkext(state[RECEIPT + 1] != CLOSED, JPOS_EFPTR_WRONG_STATE, "Bad printer state");
            checkext(state[PRINTER + 1] == EMPTY, JPOS_EFPTR_REC_EMPTY, "Paper end");
            checkext(state[PRINTER + 1] == NEAREND, JPOS_EFPTR_REC_EMPTY, "Paper near end");
            check(state[PRINTER + 1] == COVERORERROR, JPOS_E_FAILURE, "Print station error");
            throw new JposException(JPOS_E_FAILURE, "Mysterious error: " + ((JposException) e).getMessage(), (Exception)e);
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
            for (Date dateval = format.parse(date, new ParsePosition(0)); dateval.compareTo(getLastTicketDate(index)) < 0; )
                --index;
        }
        catch (Exception e) {
            return 0;
        }
        return index;
    }

    @Override
    public void printReport(int reportType, String startNum, String endNum) throws JposException {
        int start, end;
        if (reportType == FPTR_RT_DATE) {
            start = getStartPeriod(startNum, 1);
            check(start == 0, JPOS_E_ILLEGAL, "No period starting after " + startNum + " found");
            end = getEndPeriod(endNum, start);
            check(end == 0, JPOS_E_ILLEGAL, "No period ending before " + endNum + " found");
            check(start > end, JPOS_E_ILLEGAL, "No period starting before " + startNum + " and ending after " + endNum + " found");
            startNum = Long.toString(start);
            endNum = Long.toString(end);
        }
        else {
            if (Integer.parseInt(endNum) == 0)
                endNum = startNum;
            check(Integer.parseInt(endNum) >= Dev.CurrentPeriod, JPOS_E_ILLEGAL, "Invalid period: " + endNum);
        }
        Object e = processCommand(new String[]{"report", startNum, endNum, "0"});
        if (e instanceof JposException) {
            char[] state = Integer.toString(((JposException) e).getErrorCodeExtended()).toCharArray();
            if (state[0] == '9' || state.length  != DRAWER + 2)
                throw new JposException(JPOS_E_FAILURE, "Communication error", (Exception) e);
            checkext(state[RECEIPT + 1] != CLOSED, JPOS_EFPTR_WRONG_STATE, "Bad printer state");
            checkext(state[PRINTER + 1] == EMPTY, JPOS_EFPTR_REC_EMPTY, "Paper end");
            checkext(state[PRINTER + 1] == NEAREND, JPOS_EFPTR_REC_EMPTY, "Paper near end");
            check(state[PRINTER + 1] == COVERORERROR, JPOS_E_FAILURE, "Print station error");
            throw new JposException(JPOS_E_FAILURE, "Mysterious error: " + ((JposException) e).getMessage(), (Exception)e);
        }
    }

    @Override
    public void printXReport() throws JposException {
        Object e = processCommand(new String[]{"xReport"});
        if (e instanceof JposException) {
            char[] state = Integer.toString(((JposException) e).getErrorCodeExtended()).toCharArray();
            if (state[0] == '9' || state.length  != DRAWER + 2)
                throw new JposException(JPOS_E_FAILURE, "Communication error", (Exception) e);
            checkext(state[RECEIPT + 1] != CLOSED, JPOS_EFPTR_WRONG_STATE, "Bad printer state");
            checkext(state[PRINTER + 1] == EMPTY, JPOS_EFPTR_REC_EMPTY, "Paper end");
            checkext(state[PRINTER + 1] == NEAREND, JPOS_EFPTR_REC_EMPTY, "Paper near end");
            check(state[PRINTER + 1] == COVERORERROR, JPOS_E_FAILURE, "Print station error");
            throw new JposException(JPOS_E_FAILURE, "Mysterious error: " + ((JposException) e).getMessage(), (Exception)e);
        }
    }

    @Override
    public void printZReport() throws JposException {
        checkext(!DayOpened, JPOS_EFPTR_WRONG_STATE, "Fiscal day has not been started");
        Object e = processCommand(new String[]{"zReport"});
        if (e instanceof JposException) {
            char[] state = Integer.toString(((JposException) e).getErrorCodeExtended()).toCharArray();
            if (state[0] == '9' || state.length  != DRAWER + 2)
                throw new JposException(JPOS_E_FAILURE, "Communication error", (Exception) e);
            checkext(state[RECEIPT + 1] != CLOSED, JPOS_EFPTR_WRONG_STATE, "Bad printer state");
            checkext(state[PRINTER + 1] == EMPTY, JPOS_EFPTR_REC_EMPTY, "Paper end");
            checkext(state[PRINTER + 1] == NEAREND, JPOS_EFPTR_REC_EMPTY, "Paper near end");
            check(state[PRINTER + 1] == COVERORERROR, JPOS_E_FAILURE, "Print station error");
            throw new JposException(JPOS_E_FAILURE, "Mysterious error: " + ((JposException) e).getMessage(), (Exception)e);
        }
        attachWaiter();
        Dev.PollWaiter.signal();
        waitWaiter(INFINITE);
        releaseWaiter();
        check(PrinterState != FPTR_PS_MONITOR || DayOpened, JPOS_E_FAILURE, "Could not close fiscal day");
    }

    @Override
    public void resetPrinter() throws JposException {
        attachWaiter();
        Dev.PollWaiter.signal();
        waitWaiter(INFINITE);
        releaseWaiter();
        synchronized (Dev) {
            String[][][]  commands = {};
            char[] state = Dev.getCurrentState();
            check(PrinterState == FPTR_PS_LOCKED, JPOS_E_FAILURE, "Communication error");
            check(state[FISCAL] == FISCALBLOCK, JPOS_E_FAILURE, "Fiscal block");
            check(member(state[PRINTER], new long[]{EMPTY, NEAREND}), JPOS_E_FAILURE, "Change Paper");
            check(state[RECEIPT] == BLOCKED, JPOS_E_FAILURE, "Printer blocked");
            if (state[TRAININGMODE] == ON) {
                commands = Dev.addCommand(commands, new String[]{"setTrainingMode", "0"});
            }
            switch (state[RECEIPT]) {
            case NONFISCAL:
                String text = Dev.centeredLine(Dev.PrinterResetText);
                commands = Dev.addCommand(commands, new String[]{"nfPrint", "\n" + text});
                commands = Dev.addCommand(commands, new String[]{"nfEnd", Dev.NonFiscalReceiptWithTrailer});
                break;
            case ITEMIZING:
            case PAYING:
            case FINALIZING:
                commands = Dev.addCommand(commands, new String[]{"fAbort", Dev.PrinterResetText});
                commands = Dev.addCommand(commands, new String[]{"fEnd"});
            }
            Dev.executeCommands(0, commands);
        }
        attachWaiter();
        Dev.PollWaiter.signal();
        waitWaiter(INFINITE);
        releaseWaiter();
        check(PrinterState != FPTR_PS_MONITOR || TrainingModeActive, JPOS_E_FAILURE, "Printer reset failed");
    }

    @Override
    public void setHeaderLine(int lineNumber, String text, boolean doubleWidth) throws JposException {
        text = expandText(removeControlCharacters(text, false), doubleWidth);
        checkext(text.length() > MAXPRINTLINELENGTH, JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Text too long");
        String[][] cmd = {new String[]{"setHeaderLine", Integer.toString(lineNumber), text}, null};
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
                    text = text.substring(0, i) + (c = '\n') + text.substring(i + 1);
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
            check(posid < 0 || posid > MAXPOSID, JPOS_E_ILLEGAL, "POSID must be positive < 100000");
            check(cashier < 0 || cashier > MAXCASHIER, JPOS_E_ILLEGAL, "POSID must be positive < 100000");
            String[][][] commands = {
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
            throw new JposException(JPOS_E_ILLEGAL, "POSID and cashierID must be numerical values", e);
        }
    }

    @Override
    public void setStoreFiscalID(String ID) throws JposException {
        ID = removeControlCharacters(ID, false);
        check(ID.length() > MAXDESCRIPTIONLENGTH, JPOS_E_ILLEGAL, "Text too long");
        String[][] cmd = {new String[]{"setTaxPayerID", ID}, null};
        if (Dev.executeCommands(0, cmd) != 1) {
            commandErrorException(cmd, new long[]{CLOSED});
        }
    }

    @Override
    public void setTrailerLine(int lineNumber, String text, boolean doubleWidth) throws JposException {
        text = expandText(removeControlCharacters(text, false), doubleWidth);
        checkext(text.length() > MAXPRINTLINELENGTH, JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Text too long");
        String[][] cmd = {new String[]{"setTrailerLine", Integer.toString(lineNumber), text}, null};
        if (Dev.executeCommands(0, cmd) != 1) {
            commandErrorException(cmd, new long[]{CLOSED});
        }
    }

    private void commandErrorException(String[][] cmd, long[] allowed) throws JposException {
        check(cmd[1] == null || cmd[1].length < 1 || cmd[1][0] == null || cmd[1][0].length() < DRAWER + 2, JPOS_E_FAILURE, "Communication error");
        if (cmd[1][0].charAt(0) == SUCCESS)
            return;
        char[] state = cmd[1][0].substring(1).toCharArray();
        check(state[RECEIPT] == BLOCKED, JPOS_E_FAILURE, "Print station error");
        checkext(!member(state[RECEIPT], allowed), JPOS_EFPTR_WRONG_STATE, "Bad printer state");
        checkext(state[PRINTER] >= NEAREND, JPOS_EFPTR_REC_EMPTY, "Change paper");
        check(state[DRAWER] == OPENED, JPOS_E_FAILURE, "Close cash drawer");
        if (cmd[1][0].charAt(0) != SUCCESS && member(cmd[1].length, new long[]{2, 3})) {
            StringBuilder cmdstr = new StringBuilder(cmd[0][0]);
            for (int i = 1; i < cmd[0].length; i++)
                cmdstr.append(" ETB ").append(cmd[0][i]);
            cmdstr = new StringBuilder("Internal command [" + cmdstr + "] failed: ");
            check(cmd[1].length == 2 && cmd[1][1].equals("0"), JPOS_E_ILLEGAL, cmdstr + "Check state [" + new String(state) + "]");
            check(cmd[1].length == 2, JPOS_E_FAILURE, cmdstr + "Bad parameter " + cmd[1][1]);
            check(cmd[1].length == 3 , JPOS_E_FAILURE, cmdstr + cmd[1][1] + " - " + cmd[1][2]);
        }
        throw new JposException(JPOS_E_FAILURE, "Unknown error");
    }

    private boolean returnValuePresent(String[][] cmd) {
        return (cmd[1].length == 2 && cmd[1][1] != null);
    }

    @Override
    public void setVatTable() throws JposException {
        for (int i = 1; i < MAXVATINDEX; ++i) {
            check(Dev.NewVatTable[i - 1] < Dev.NewVatTable[i], JPOS_E_ILLEGAL, "VAT rates must be in decending order");
        }
        try {
            String[][][] commands = {};
            for (int i = 0; i < MAXVATINDEX; i++) {
                if ((Dev.NewVatTable[i] >= 0 || Dev.VatTable[i] >= 0) && Dev.VatTable[i] != Dev.NewVatTable[i]) {
                    commands = Dev.addCommand(commands, new String[]{"setVatRate", Long.toString(i + 1), new BigDecimal(Dev.NewVatTable[i]).divide(new BigDecimal(CURRENCYFACTOR), HALF_UP).toString()});
                }
            }
            int count = Dev.executeCommands(0, commands);
            for (String[][] strings : commands) {
                if (strings[1] != null && strings[1].length > 0 && strings[1][0].charAt(0) == SUCCESS) {
                    int idx = Integer.parseInt(strings[0][1]) - 1;
                    Dev.VatTable[idx] = Dev.NewVatTable[idx];
                }
            }
            if (count != commands.length) {
                for (String[][] command : commands) {
                    commandErrorException(command, new long[]{CLOSED});
                }
            }
        } catch (ClassCastException e) {
            throw new JposException(JPOS_E_FAILURE, "Communication error get VAT rates", e);
        } catch (NumberFormatException e) {
            throw new JposException(JPOS_E_FAILURE, "Invalid printer VAT rate", e);
        }
    }

    @Override
    public void setDate(String date) throws JposException {
        SimpleDateFormat format = new SimpleDateFormat("ddMMyyyyHHmm");
        String nowstr = format.format(new Date());
        checkext(!nowstr.equals(date), JPOS_EFPTR_BAD_DATE, "Invalid date");
    }

    @Override
    public void setVatValue(int vatID, long vatValue) throws JposException {
        check(vatID <= 0 || vatID > MAXVATINDEX, JPOS_E_ILLEGAL, "Invalid VatD: " + vatID);
        Dev.NewVatTable[vatID - 1] = vatValue;
    }

    /**
     * Flags for fixed output document lines. One int[] per document, one entry per line:
     * <br>0: Mandatory line, empty data (first and last line),
     * <br>1: Mandatory line, cashier number (numeric, 1 - 99999),
     * <br>-2: Optional line, secret (any string no longer than MAXDESCRIPTIONLENGTH
     */
    private final int[][] FixedOutputDataFlags = {
            null,       // Not used, but possible values are 0 (mandatory, data empty), 1 (mandatory, cashier no), -2 (optional, secret)
            new int[]{-2, 1, 0},         // Sign on
            new int[]{-2, 1, -2, 0}      // Sign off
    };

    @Override
    public PrintFixedOutput printFixedOutput(int documentType, int lineNumber, String data) throws JposException {
        check(documentType != Dev.FixedOutputDocument, JPOS_E_ILLEGAL, "Bad document type: " + documentType);
        boolean allowedLine = (member(lineNumber, Dev.AllowedDocumentLineTable[documentType]) && lineNumber >= Dev.NonFiscalMinLineNo) || lineNumber == SAMPLEFISCALPRINTERFXO_HEAD;
        check(!allowedLine, JPOS_E_ILLEGAL, "Invalid line number: " + lineNumber);
        check(FixedOutputDataFlags[documentType][lineNumber - SAMPLEFISCALPRINTERFXO_HEAD] == 0 && data.length() != 0, JPOS_E_ILLEGAL, "Invalid data for line " + lineNumber + ": Must be empty");
        if (FixedOutputDataFlags[documentType][lineNumber - SAMPLEFISCALPRINTERFXO_HEAD] == 1) {
            try {
                int cashier = Integer.parseInt(data);
                check(cashier <= 0 || 100000 <= cashier, JPOS_E_ILLEGAL, "Cashier out of range: " + cashier);
            } catch (NumberFormatException e) {
                throw new JposException(JPOS_E_ILLEGAL, "Cashier not numeric: " + data, e);
            }
        }
        check(FixedOutputDataFlags[documentType][lineNumber - SAMPLEFISCALPRINTERFXO_HEAD] == -2 && data.length() > MAXDESCRIPTIONLENGTH, JPOS_E_ILLEGAL, "Secret too long: " + data.length());
        return super.printFixedOutput(documentType, lineNumber, data);
    }

    @Override
    public void printFixedOutput(PrintFixedOutput request) throws JposException {
        check(Dev.NonFiscalMinLineNo == -1, JPOS_E_ILLEGAL, "Invalid output after last line");
        for (int i = Dev.NonFiscalMinLineNo; i < request.getLineNumber(); i++)
            check(FixedOutputDataFlags[request.getDocumentType()][i - SAMPLEFISCALPRINTERFXO_HEAD] >= 0, JPOS_E_ILLEGAL, "Mandatory line " + i + " missing");
        String[] lines;
        String[][][] cmds = {};
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
                String[][] cmd = {new String[]{"nfPrint", Dev.PrintBuffer.substring(0, MAXPRINTLINELENGTH)}, null};
                if (Dev.executeCommands(0, cmd) != 1)
                    commandErrorException(cmd, new long[]{NONFISCAL});
                Dev.PrintBuffer = Dev.PrintBuffer.substring(MAXPRINTLINELENGTH);
                continue;
            }
            if (nlindex >= 0) {
                String[][] cmd = {new String[]{"nfPrint", Dev.PrintBuffer.substring(0, nlindex)}, null};
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
        checkext(!removeControlCharacters(description, false).equals(description), JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description contains invalid characters");
        checkext(description.length() > MAXDESCRIPTIONLENGTH, JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description too long");
        checkext(vatInfo < 1 || vatInfo > MAXVATINDEX || Dev.VatTable[vatInfo - 1] < 0, JPOS_EFPTR_BAD_VAT, "Invalid vatInfo: " + vatInfo);
        checkext(!unitName.equals(removeControlCharacters(unitName, false)), JPOS_EFPTR_BAD_ITEM_QUANTITY, "unitName contains invalid characters");
        checkext(unitName.length() > MAXDIMENSIONLENGTH, JPOS_EFPTR_BAD_ITEM_QUANTITY, "UnitName too long: " + unitName);
        return super.printRecItem(description, price, quantity, vatInfo, unitPrice, unitName);
    }

    @Override
    public void printRecItem(PrintRecItem request) throws JposException {
        // fPrintItem text vatrate price quantity dimension quantitydecimals amount ""|Voids|Refunds
        String[][][]cmds = {};
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
                    new BigDecimal(price).divide(new BigDecimal(CURRENCYFACTOR), HALF_UP).toString(),
                    new BigDecimal(quantity).divide(new BigDecimal(QUANTITYFACTOR), HALF_UP).toString(),
                    request.getUnitName(),
                    Integer.toString(qdec),
                    new BigDecimal(request.getAmount()).divide(new BigDecimal(CURRENCYFACTOR), HALF_UP).toString(),
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
        for (int i = QUANTITYFACTOR; true; i /= 10) {
            if (quantity % i == 0)
                break;
            qdec++;
        }
        return qdec;
    }

    @Override
    public PrintRecItemAdjustment printRecItemAdjustment(int adjustmentType, String description, long amount, int vatInfo) throws JposException {
        checkext(description.length() > MAXDESCRIPTIONLENGTH, JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description too long");
        checkext(vatInfo < 1 || vatInfo > MAXVATINDEX || Dev.VatTable[vatInfo - 1] < 0, JPOS_EFPTR_BAD_VAT, "Invalid vatInfo: " + vatInfo);
        return super.printRecItemAdjustment(adjustmentType, description, amount, vatInfo);
    }

    @Override
    public void printRecItemAdjustment(PrintRecItemAdjustment request) throws JposException {
        // fPrintAdjustment text amount percent
        String[][][]cmds = {};
        if (request.AdditionalData == null) {
            // Initial call
            cmds = Dev.addCommand(cmds, new String[]{"get", "Position", "VAT", "Amount"});
            if (Dev.executeCommands(0, cmds) != 1 || cmds[0][1].length < 3) {
                commandErrorException(cmds[0], new long[]{ITEMIZING});
                throw new JposException(JPOS_E_FAILURE, "Cannot retrieve VAT rate and amount of current position");
            }
            long amount = new BigDecimal(cmds[0][1][2]).multiply(new BigDecimal(CURRENCYFACTOR)).longValueExact();
            int vatidx = Integer.parseInt(cmds[0][1][1]);
            checkext(vatidx != request.getVatInfo(), JPOS_EFPTR_BAD_VAT, "vatInfo does not match VAT rate of current position");
            cmds = new String[0][][];
            if (!request.getPreLine().equals(""))
                cmds = Dev.addCommand(cmds, new String[]{"fPrint", request.getPreLine()});
            boolean discount = member(request.getAdjustmentType(), new long[]{
                    FPTR_AT_COUPON_PERCENTAGE_DISCOUNT,
                    FPTR_AT_COUPON_AMOUNT_DISCOUNT,
                    FPTR_AT_PERCENTAGE_DISCOUNT,
                    FPTR_AT_AMOUNT_DISCOUNT
            });
            boolean percent = member(request.getAdjustmentType(), new long[]{
                    FPTR_AT_PERCENTAGE_SURCHARGE,
                    FPTR_AT_PERCENTAGE_DISCOUNT,
                    FPTR_AT_COUPON_PERCENTAGE_DISCOUNT
            });
            check(!percent && discount && amount <= request.getAmount(), JPOS_E_ILLEGAL, "Discount > amount");
            cmds = Dev.addCommand(cmds, new String[]{
                    "fPrintAdjustment",
                    request.getDescription(),
                    new BigDecimal(request.getAmount() * (discount ? -1 : 1)).divide(new BigDecimal(CURRENCYFACTOR), HALF_UP).toString(),
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
        VoidAdjustmentDescription.remove(request.getAdjustmentType());
        VoidAdjustmentDescription.put(request.getAdjustmentType(), request.getDescription());
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
        checkext(description.length() > MAXDESCRIPTIONLENGTH, JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description too long");
        checkext(vatInfo < 1 || vatInfo > MAXVATINDEX || Dev.VatTable[vatInfo - 1] < 0, JPOS_EFPTR_BAD_VAT, "Invalid vatInfo: " + vatInfo);
        return super.printRecItemAdjustmentVoid(adjustmentType, description, amount, vatInfo);
    }

    @Override
    public void printRecItemAdjustmentVoid(PrintRecItemAdjustmentVoid request) throws JposException {
        // fPrintAdjustment text amount percent
        String[][][]cmds = {};
        if (request.AdditionalData == null) {
            // Initial call
            cmds = Dev.addCommand(cmds, new String[]{"get", "Position", "VAT", "Amount"});
            if (Dev.executeCommands(0, cmds) != 1 || cmds[0][1].length < 3) {
                commandErrorException(cmds[0], new long[]{ITEMIZING});
                throw new JposException(JPOS_E_FAILURE, "Cannot retrieve VAT rate and amount of current position");
            }
            long amount = new BigDecimal(cmds[0][1][2]).multiply(new BigDecimal(CURRENCYFACTOR)).longValueExact();
            int vatidx = Integer.parseInt(cmds[0][1][1]);
            checkext(vatidx != request.getVatInfo(), JPOS_EFPTR_BAD_VAT, "vatInfo does not match VAT rate of current position");
            cmds = new String[0][][];
            if (!request.getPreLine().equals(""))
                cmds = Dev.addCommand(cmds, new String[]{"fPrint", request.getPreLine()});
            cmds = Dev.addCommand(cmds, new String[]{"fPrint", Dev.centeredLine(Dev.AdjustmentVoidText)});
            boolean discount = member(request.getAdjustmentType(), new long[]{
                    FPTR_AT_COUPON_PERCENTAGE_DISCOUNT,
                    FPTR_AT_COUPON_AMOUNT_DISCOUNT,
                    FPTR_AT_PERCENTAGE_DISCOUNT,
                    FPTR_AT_AMOUNT_DISCOUNT
            });
            boolean percent = member(request.getAdjustmentType(), new long[]{
                    FPTR_AT_PERCENTAGE_SURCHARGE,
                    FPTR_AT_PERCENTAGE_DISCOUNT,
                    FPTR_AT_COUPON_PERCENTAGE_DISCOUNT
            });
            check(!percent && !discount && amount <= request.getAmount(), JPOS_E_ILLEGAL, "Voided surcharge > amount");
            cmds = Dev.addCommand(cmds, new String[]{
                    "fPrintAdjustment",
                    request.getDescription(),
                    new BigDecimal(request.getAmount() * (discount ? 1 : -1)).divide(new BigDecimal(CURRENCYFACTOR), HALF_UP).toString(),
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
        checkext(!removeControlCharacters(description, false).equals(description), JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description contains invalid characters");
        checkext(description.length() > MAXDESCRIPTIONLENGTH, JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description too long");
        checkext(vatInfo < 1 || vatInfo > MAXVATINDEX || Dev.VatTable[vatInfo - 1] < 0, JPOS_EFPTR_BAD_VAT, "Invalid vatInfo: " + vatInfo);
        checkext(!unitName.equals(removeControlCharacters(unitName, false)), JPOS_EFPTR_BAD_ITEM_QUANTITY, "unitName contains invalid characters");
        checkext(unitName.length() > MAXDIMENSIONLENGTH, JPOS_EFPTR_BAD_ITEM_QUANTITY, "UnitName too long: " + unitName);
        return super.printRecItemVoid(description, price, quantity, vatInfo, unitPrice, unitName);
    }

    @Override
    public void printRecItemVoid(PrintRecItemVoid request) throws JposException {
        // fPrintItem text vatrate price quantity dimension quantitydecimals amount ""|Voids|Refunds
        String[][][]cmds = {};
        if (request.AdditionalData == null) {
            // Initial call
            cmds = Dev.addCommand(cmds, new String[]{"fPrintSubtotal", "0"});
            cmds = Dev.addCommand(cmds, new String[]{"get", "CurrentTotal", "VAT" + request.getVatInfo()});
            if (Dev.executeCommands(0, cmds) != cmds.length || cmds[1][1].length < 2) {
                for (String[][]cmd : cmds)
                    commandErrorException(cmd, new long[]{ITEMIZING});
                throw new JposException(JPOS_E_FAILURE, "Cannot retrieve current total for VAT " + cmds[0][0][2]);
            }
            long amount = new BigDecimal(cmds[1][1][1]).multiply(new BigDecimal(CURRENCYFACTOR)).longValueExact();
            checkext(amount < request.getAmount(), JPOS_EFPTR_BAD_ITEM_AMOUNT, "Amount on VAT rate " + request.getVatInfo() + " negative");
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
                    new BigDecimal(price).divide(new BigDecimal(CURRENCYFACTOR), HALF_UP).toString(),
                    new BigDecimal(quantity).divide(new BigDecimal(QUANTITYFACTOR), HALF_UP).toString(),
                    request.getUnitName(),
                    Integer.toString(qdec),
                    new BigDecimal(request.getAmount()).divide(new BigDecimal(CURRENCYFACTOR), HALF_UP).toString(),
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
        checkext(!removeControlCharacters(description, false).equals(description), JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description contains invalid characters");
        checkext(description.length() > MAXDESCRIPTIONLENGTH, JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description too long");
        checkext(vatInfo < 1 || vatInfo > MAXVATINDEX || Dev.VatTable[vatInfo - 1] < 0, JPOS_EFPTR_BAD_VAT, "Invalid vatInfo: " + vatInfo);
        checkext(!unitName.equals(removeControlCharacters(unitName, false)), JPOS_EFPTR_BAD_ITEM_QUANTITY, "unitName contains invalid characters");
        checkext(unitName.length() > MAXDIMENSIONLENGTH, JPOS_EFPTR_BAD_ITEM_QUANTITY, "UnitName too long: " + unitName);
        return super.printRecItemRefund(description, amount, quantity, vatInfo, unitAmount, unitName);
    }

    @Override
    public void printRecItemRefund(PrintRecItemRefund request) throws JposException {
        // fPrintItem text vatrate price quantity dimension quantitydecimals amount ""|Voids|Refunds
        String[][][]cmds = {};
        if (request.AdditionalData == null) {
            // Initial call
            cmds = Dev.addCommand(cmds, new String[]{"fPrintSubtotal", "0"});
            cmds = Dev.addCommand(cmds, new String[]{"get", "CurrentTotal", "VAT" + request.getVatInfo()});
            if (Dev.executeCommands(0, cmds) != cmds.length || cmds[1][1].length < 2) {
                for (String[][]cmd : cmds)
                    commandErrorException(cmd, new long[]{ITEMIZING});
                throw new JposException(JPOS_E_FAILURE, "Cannot retrieve current total for VAT " + cmds[0][0][2]);
            }
            long amount = new BigDecimal(cmds[1][1][1]).multiply(new BigDecimal(CURRENCYFACTOR)).longValueExact();
            checkext(amount < request.getAmount(), JPOS_EFPTR_BAD_ITEM_AMOUNT, "Amount on VAT rate " + request.getVatInfo() + " negative");
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
                    new BigDecimal(price).divide(new BigDecimal(CURRENCYFACTOR), HALF_UP).toString(),
                    new BigDecimal(quantity).divide(new BigDecimal(QUANTITYFACTOR), HALF_UP).toString(),
                    request.getUnitName(),
                    Integer.toString(qdec),
                    new BigDecimal(request.getAmount()).divide(new BigDecimal(CURRENCYFACTOR), HALF_UP).toString(),
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
        String[][][] cmds = {};
        if (!request.getPreLine().equals(""))
            cmds = Dev.addCommand(cmds, new String[]{"fPrint", request.getPreLine()});
        cmds = Dev.addCommand(cmds, new String[]{"fPrint", Dev.centeredLine(Dev.RefundVoidText)});
        if (Dev.executeCommands(0, cmds) != cmds.length) {
            for (String[][] cmd : cmds)
                commandErrorException(cmd, new long[]{ITEMIZING});
        }
        printRecItem(req);
        if (!request.getPostLine().equals("")) {
            String[][] cmd = {new String[]{"fPrint", request.getPostLine()}, null};
            if (Dev.executeCommands(0, cmd) != 1)
                commandErrorException(cmd, new long[]{ITEMIZING});
        }
    }

    @Override
    public PrintRecMessage printRecMessage(String message) throws JposException {
        checkext(!removeControlCharacters(message, false).equals(message), JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description contains invalid characters");
        if (MessageType == FPTR_MT_CASHIER) {
            try {
                long i = Long.parseLong(message);
                checkext(i < 0 || i > MAXCASHIER, JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Cashier must be between 0 and " + MAXCASHIER);
            } catch (NumberFormatException e) {
                throw new JposException(JPOS_E_EXTENDED, JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Cashier must be numeric", e);
            }
        }
        checkext(message.length() > MAXFISCALPRINTLINE, JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description too long");
        return super.printRecMessage(message);
    }

    @Override
    public void printRecMessage(PrintRecMessage request) throws JposException {
        String[][] cmd = {new String[]{null, request.getMessage()}, null};
        cmd[0][0] = request.getMessageType() == FPTR_MT_CASHIER ? "setCashier" : "fPrint";
        checkext(MessageType == FPTR_MT_CASHIER && PrinterState != FPTR_PS_FISCAL_RECEIPT_ENDING, JPOS_EFPTR_WRONG_STATE, "Print cashier only at receipt ending");
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
        String[][][]cmds = {};
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
        checkext(!removeControlCharacters(description, false).equals(description), JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description contains invalid characters");
        checkext(description.length() > MAXDESCRIPTIONLENGTH, JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description too long");
        return super.printRecSubtotalAdjustment(adjustmentType, description, amount);
    }

    @Override
    public void printRecSubtotalAdjustment(PrintRecSubtotalAdjustment request) throws JposException {
        // fPrintSubtotal print
        // fPrintAdjustment text amount percent
        String[][][]cmds = {};
        if (request.AdditionalData == null) {
            // Initial call
            cmds = Dev.addCommand(cmds, new String[]{"fPrintSubtotal", "1"});
            boolean discount = member(request.getAdjustmentType(), new long[]{
                    FPTR_AT_COUPON_PERCENTAGE_DISCOUNT,
                    FPTR_AT_COUPON_AMOUNT_DISCOUNT,
                    FPTR_AT_PERCENTAGE_DISCOUNT,
                    FPTR_AT_AMOUNT_DISCOUNT
            });
            boolean percent = member(request.getAdjustmentType(), new long[]{
                    FPTR_AT_PERCENTAGE_SURCHARGE,
                    FPTR_AT_PERCENTAGE_DISCOUNT,
                    FPTR_AT_COUPON_PERCENTAGE_DISCOUNT
            });
            if (!request.getPreLine().equals(""))
                cmds = Dev.addCommand(cmds, new String[]{"fPrint", request.getPreLine()});
            cmds = Dev.addCommand(cmds, new String[]{
                    "fPrintAdjustment",
                    request.getDescription(),
                    new BigDecimal(request.getAmount() * (discount ? -1 : 1)).divide(new BigDecimal(CURRENCYFACTOR), HALF_UP).toString(),
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
        String[][][]cmds = {};
        if (request.AdditionalData == null) {
            // Initial call
            boolean discount = member(request.getAdjustmentType(), new long[]{
                    FPTR_AT_COUPON_PERCENTAGE_DISCOUNT,
                    FPTR_AT_COUPON_AMOUNT_DISCOUNT,
                    FPTR_AT_PERCENTAGE_DISCOUNT,
                    FPTR_AT_AMOUNT_DISCOUNT
            });
            boolean percent = member(request.getAdjustmentType(), new long[]{
                    FPTR_AT_PERCENTAGE_SURCHARGE,
                    FPTR_AT_PERCENTAGE_DISCOUNT,
                    FPTR_AT_COUPON_PERCENTAGE_DISCOUNT
            });
            if (!request.getPreLine().equals(""))
                cmds = Dev.addCommand(cmds, new String[]{"fPrint", request.getPreLine()});
            cmds = Dev.addCommand(cmds, new String[]{"fPrint", Dev.centeredLine(Dev.AdjustmentVoidText)});
            cmds = Dev.addCommand(cmds, new String[]{
                    "fPrintAdjustment",
                    Dev.SubtotalAdjustment.getDescription(),
                    new BigDecimal(request.getAmount() * (discount ? 1 : -1)).divide(new BigDecimal(CURRENCYFACTOR), HALF_UP).toString(),
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
            checkext(Dev.SubtotalAdjustment == null, JPOS_EFPTR_WRONG_STATE, "No previous subtotal adjustment");
            checkext(Dev.SubtotalAdjustment.getAmount() != amount, JPOS_EFPTR_BAD_ITEM_AMOUNT, "Amount does not match previous adjustment");
            checkext(Dev.SubtotalAdjustment.getAdjustmentType() != adjustmentType, JPOS_EFPTR_BAD_ITEM_AMOUNT, "Adjustment type does not match previous adjustment type");
        }
    }

    @Override
    public PrintRecTaxID printRecTaxID(String id) throws JposException {
        throw new JposException(JPOS_E_ILLEGAL, "Printing tax ID not supported");
    }

    @Override
    public PrintRecTotal printRecTotal(long total, long payment, String description) throws JposException {
        checkext(!removeControlCharacters(description, false).equals(description), JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description contains invalid characters");
        checkext(description.length() > MAXDESCRIPTIONLENGTH, JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description too long");
        return super.printRecTotal(total, payment, description);
    }

    @Override
    public void printRecTotal(PrintRecTotal request) throws JposException {
        // fPrintTotal
        // fPrintPayment text amount
        String[][][]cmds = {};
        long[]allowedStates = {
                FPTR_PS_FISCAL_RECEIPT_TOTAL,
                FPTR_PS_FISCAL_RECEIPT_ENDING
        };
        if (request.AdditionalData == null) {
            // Initial call
            if (Dev.getCurrentState()[RECEIPT] == ITEMIZING)
                cmds = Dev.addCommand(cmds, new String[]{"fPrintTotal"});
            if (request.getPayment() > 0) {
                cmds = Dev.addCommand(cmds, new String[]{
                        "fPrintPayment",
                        request.getDescription(),
                        new BigDecimal(request.getPayment()).divide(new BigDecimal(CURRENCYFACTOR), HALF_UP).toString(),
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
        waitWaiter(INFINITE);
        releaseWaiter();
        checkMember(PrinterState, allowedStates, JPOS_E_FAILURE, "Could not leave itemizing state");
    }

    @Override
    public PrintRecVoid printRecVoid(String description) throws JposException {
        checkext(!removeControlCharacters(description, false).equals(description), JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description contains invalid characters");
        checkext(description.length() > MAXFISCALPRINTLINE, JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Description too long");
        return super.printRecVoid(description);
    }

    @Override
    public void printRecVoid(PrintRecVoid request) throws JposException {
        // fAbort text
        long[] allowed = {FPTR_PS_FISCAL_RECEIPT, FPTR_PS_FISCAL_RECEIPT_TOTAL};
        checkext(!member(PrinterState, allowed), JPOS_EFPTR_WRONG_STATE, "Wrong printer state: " + PrinterState);
        String[][]cmd = {new String[]{"fAbort", request.getDescription()}, null};
        if (Dev.executeCommands(0, cmd) != 1)
            commandErrorException(cmd, new long[]{ITEMIZING, PAYING, FINALIZING});
        attachWaiter();
        Dev.PollWaiter.signal();
        waitWaiter(INFINITE);
        releaseWaiter();
        check(PrinterState != FPTR_PS_FISCAL_RECEIPT_ENDING, JPOS_E_FAILURE, "Could not enter receipt ending state");
        Dev.VoidOnEndFiscal = false;
    }

    private final HashMap<Integer,String> VoidAdjustmentDescription = new HashMap<>();

    @Override
    public void printRecVoidItem(PrintRecVoidItem request) throws JposException {
        PrintRecItemVoid voidItem = printRecItemVoid(request.getDescription(), request.getAmount(), request.getQuantity(), request.getVatInfo(), 0, "");
        if (request.getAdjustment() != 0) {
            String desc = VoidAdjustmentDescription.get(request.getAdjustmentType());
            check(desc == null, JPOS_E_ILLEGAL, "Unused Adjustment type: " + request.getAdjustmentType() + ", Void Item not possible");
            printRecItemAdjustmentVoid(printRecItemAdjustmentVoid(request.getAdjustmentType(), desc, request.getAdjustment(), request.getVatInfo()));
        }
        printRecItemVoid(voidItem);
    }

    @Override
    public void updateState(boolean fromDeviceEnabled) {
        char[] state = Dev.getCurrentState();
        if (PowerNotify == JPOS_PN_ENABLED) {
            int value = state.length <= DRAWER ? JPOS_PS_OFF_OFFLINE : JPOS_PS_ONLINE;
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
            if (PrinterState != FPTR_PS_LOCKED) {
                PrinterState = FPTR_PS_LOCKED;
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
            if (fromDeviceEnabled && PowerState != JPOS_PS_OFF) {
                PowerState = JPOS_PS_OFF;
                EventSource.logSet("PowerState");
            }
        }
    }

    private void updateReceiptState(char recState, boolean fromDeviceEnabled) {
        if (    (PrinterState == FPTR_PS_MONITOR) != (recState == CLOSED) ||
                (PrinterState == FPTR_PS_FISCAL_RECEIPT) != (recState == ITEMIZING) ||
                (PrinterState == FPTR_PS_FISCAL_RECEIPT_TOTAL) != (recState == PAYING) ||
                (PrinterState == FPTR_PS_FISCAL_RECEIPT_ENDING) != (recState == FINALIZING) ||
                (PrinterState == FPTR_PS_NONFISCAL || PrinterState == FPTR_PS_FIXED_OUTPUT) != (recState == NONFISCAL))
        {
            if (fromDeviceEnabled) {
                PowerState = JPOS_PS_OFF;
                EventSource.logSet("PowerState");
            }
            switch (recState) {
            case CLOSED:
                PrinterState = FPTR_PS_MONITOR;
                break;
            case ITEMIZING:
                PrinterState = FPTR_PS_FISCAL_RECEIPT;
                break;
            case PAYING:
                PrinterState = FPTR_PS_FISCAL_RECEIPT_TOTAL;
                break;
            case FINALIZING:
                PrinterState = FPTR_PS_FISCAL_RECEIPT_ENDING;
                break;
            case NONFISCAL:
                PrinterState = Dev.NonFiscalMinLineNo > 0 ? FPTR_PS_FIXED_OUTPUT : FPTR_PS_NONFISCAL;
            }
            EventSource.logSet("PrinterState");
        }
    }

    private void updatePaperStates(char c, boolean fromDeviceEnabled) {
        FiscalPrinterStatusUpdateEvent ev = null;
        switch (c) {
        case OK:
            if (RecEmpty || RecNearEnd)
                ev = new FiscalPrinterStatusUpdateEvent(EventSource, FPTR_SUE_REC_PAPEROK);
            break;
        case NEAREND:
            if (RecEmpty || !RecNearEnd)
                ev = new FiscalPrinterStatusUpdateEvent(EventSource, FPTR_SUE_REC_NEAREMPTY);
            break;
        default:
            if (!RecEmpty)
                ev = new FiscalPrinterStatusUpdateEvent(EventSource, FPTR_SUE_REC_EMPTY);
        }
        if (ev != null) {
            if (fromDeviceEnabled)
                ev.setAndCheckStatusProperties();
            else {
                try {
                    Dev.handleEvent(ev);
                } catch (JposException ignored) {}
            }
        }
    }
}
