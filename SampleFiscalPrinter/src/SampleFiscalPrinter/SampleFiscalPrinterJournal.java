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
import de.gmxhome.conrad.jpos.jpos_base.electronicjournal.*;
import jpos.ElectronicJournalConst;
import jpos.FiscalPrinterConst;
import jpos.JposConst;
import jpos.JposException;

import javax.swing.*;
import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

import static SampleFiscalPrinter.SampleFiscalPrinter.*;

/**
 * Class implementing the ElectronicJournalInterface for the sample fiscal printer.
 */
class SampleFiscalPrinterJournal extends ElectronicJournalProperties implements SampleFiscalPrinterStatusUpdater {
    private SampleFiscalPrinter Dev;

    /**
     * Constructor. Gets instance of SampleFiscalPrinter to be used as communication object. Device index
     * for sample is always 0.
     * @param dev Instance of SampleFiscalPrinter this object belongs to.
     */
    SampleFiscalPrinterJournal(SampleFiscalPrinter dev) {
        super(0);
        Dev = dev;
    }
    @Override
    public void deviceEnabled(boolean enable) throws JposException {
        super.deviceEnabled(enable);
        Dev.updateStates(this, enable);
    }

    @Override
    public void claim(int timeout) throws JposException {
        Dev.startPolling();
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
            String[] marker = new String[1];
            ((ElectronicJournalService) EventSource).retrieveCurrentMarker(ElectronicJournalConst.EJ_MT_DOCUMENT, marker);
            CheckHealthText += "OK, Last marker: " + marker;
        } catch (JposException e) {
            CheckHealthText += "Failed, " + e.getMessage();
        }
        super.checkHealth(level);
    }

    @Override
    public PrintContent printContent(String fromMarker, String toMarker) throws JposException {
        long fromvalue = "".equals(fromMarker) ? 1 : fullCheckMarker(fromMarker, "fromMarker");
        long tovalue = "".equals(toMarker) ? MAXPERIOD * 1000000000L : fullCheckMarker(toMarker, "toMarker");
        Dev.check(fromvalue > tovalue, JposConst.JPOS_E_ILLEGAL, "fromMarker newer than toMarker");
        return super.printContent(fromMarker,toMarker);
    }

    private long fullCheckMarker(String marker, String markerName) throws JposException {
        String[] parts = checkMarker(marker);
        int max = Integer.parseInt(parts[1]);
        Dev.check(max == 0 || max > getMaxDocumentNumber(parts[0]), JposConst.JPOS_E_ILLEGAL, "Invalid " + markerName + ": " + marker);
        return Long.parseLong(parts[0])* 1000000000 + Long.parseLong(parts[1]);
    }

    private int getMaxDocumentNumber(String period) throws JposException {
        String[][] cmd = new String[][]{null, null};
        if (Integer.parseInt(period) == Dev.CurrentPeriod)
            cmd[0] = new String[]{"get", "Total", "Fiscal", "Normal"};
        else
            cmd[0] = new String[]{"get", "Memory", period, "Fiscal", "Normal"};
        Dev.check(Dev.executeCommands(0, cmd) != 1, JposConst.JPOS_E_FAILURE, "Cannot retrieve session data");
        Dev.check(cmd[1].length < 3 || cmd[1][1].matches(".*[^0-9].*") || cmd[1][2].matches(".*[^0-9].*") || cmd[1][1].length() > 8 || cmd[1][2].length() > 8, JposConst.JPOS_E_FAILURE, "Bad data format");
        return Integer.parseInt(cmd[1][1]) + Integer.parseInt(cmd[1][2]);
    }

    @Override
    public void printContent(PrintContent request) throws JposException {
        String from = "".equals(request.getFromMarker()) ? "0-1" : request.getFromMarker();
        int tosession = Dev.CurrentPeriod + 1;
        int toticket = 0;
        if (!"".equals(request.getToMarker())) {
            String[] toparts = checkMarker(request.getToMarker());
            tosession = Integer.parseInt(toparts[0]);
            toticket = Integer.parseInt(toparts[1]);
        }
        int[]addData = (int[])request.AdditionalData;
        if (addData == null) {
            String[] fromparts = checkMarker(from);
            request.AdditionalData = addData = new int[]{Integer.parseInt(fromparts[0]), Integer.parseInt(fromparts[1])};
        }
        int maxfrom = getMaxDocumentNumber(Integer.toString(addData[0]));
        while (addData[0] <= tosession && addData[0] <= Dev.CurrentPeriod) {
            String ticketstr = Integer.toString(addData[1]);
            String[][]cmd = new String[][]{new String[]{"printJournal", Integer.toString(addData[0]), ticketstr, ticketstr}, null};
            if (Dev.executeCommands(0, cmd) != 1) {
                commandErrorException(cmd, new long[]{CLOSED}, true);
            }
            if (++addData[1] > (addData[0] == tosession ? toticket : maxfrom)) {
                if (++addData[0] < tosession)
                    maxfrom = getMaxDocumentNumber(Integer.toString(addData[0]));
                else
                    maxfrom = toticket;
                if (maxfrom == 0)
                    break;
                addData[1] = 1;
            }
        }
    }

    @Override
    public QueryContent queryContent(String fileName, String fromMarker, String toMarker) throws JposException {
        Dev.check(new File(fileName).exists(), JposConst.JPOS_E_EXISTS, "File exists: " + fileName);
        long fromvalue = "".equals(fromMarker) ? 1 : fullCheckMarker(fromMarker, "fromMarker");
        long tovalue = "".equals(toMarker) ? MAXPERIOD * 1000000000L : fullCheckMarker(toMarker, "toMarker");
        Dev.check(fromvalue > tovalue, JposConst.JPOS_E_ILLEGAL, "fromMarker newer than toMarker");
        return super.queryContent(fileName, fromMarker, toMarker);
    }

    @Override
    public void queryContent(QueryContent request) throws JposException {
        try {
            String from = "".equals(request.getFromMarker()) ? "0-1" : request.getFromMarker();
            int tosession = Dev.CurrentPeriod + 1;
            int toticket = 0;
            RandomAccessFile target = new RandomAccessFile(request.getFileName(), "rw");
            if (!"".equals(request.getToMarker())) {
                String[] toparts = checkMarker(request.getToMarker());
                tosession = Integer.parseInt(toparts[0]);
                toticket = Integer.parseInt(toparts[1]);
            }
            int[]addData = (int[])request.AdditionalData;
            if (addData == null) {
                String[] fromparts = checkMarker(from);
                request.AdditionalData = addData = new int[]{Integer.parseInt(fromparts[0]), Integer.parseInt(fromparts[1])};
            }
            else {
                target.seek(target.length());
            }
            int maxfrom = getMaxDocumentNumber(Integer.toString(addData[0]));
            while (addData[0] <= tosession) {
                String ticketstr = Integer.toString(addData[1]);
                String[][] cmd = new String[][]{new String[]{"retrieveJournal", Integer.toString(addData[0]), ticketstr, ticketstr, "1"}, null};
                if (Dev.executeCommands(0, cmd) != 1 || cmd[1].length != 3 || !"1".equals(cmd[1][1])) {
                    target.close();
                    throw new JposException(JposConst.JPOS_E_FAILURE, "Cannot retrieve ticket data for marker " + addData[0] + "-" + addData[1]);
                }
                byte[] frame = cmd[1][2].getBytes("UTF8");
                target.write(frame,0,frame.length);
                target.write(new byte[]{FF}, 0, 1);
                if (++addData[1] > (addData[0] == tosession ? toticket : maxfrom)) {
                    if (++addData[0] < tosession)
                        maxfrom = getMaxDocumentNumber(Integer.toString(addData[0]));
                    else
                        maxfrom = toticket;
                    if (maxfrom == 0)
                        break;
                    addData[1] = 1;
                }
            }
            target.close();
            Dev.handleEvent(new JposDataEvent(EventSource, 0));
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_FAILURE, e.getMessage(), e);
        }
    }

    @Override
    public void retrieveCurrentMarker(int markerType, String[] marker) throws JposException {
        Dev.check(Dev.CurrentPeriod == 0, JposConst.JPOS_E_NOEXIST, "Fiscal printer not yet fiscalized");
        String[][][] cmds = Dev.addCommand(new String[0][][], new String[]{"get", "Total", "Fiscal", "Normal"});
        cmds = Dev.addCommand(cmds, new String[]{"get", "Memory", Long.toString(Dev.CurrentPeriod - 1), "Fiscal", "Normal"});
        if (Dev.executeCommands(0, cmds) < cmds.length) {
            for (String[][]cmd : cmds)
                commandErrorException(cmd, null, false);
        }
        buildMarkerFromResponses(markerType, marker, cmds);
    }

    private void buildMarkerFromResponses(int markerType, String[] marker, String[][][] cmds) throws JposException {
        int storedlastcurrent;
        int storedlastprevious;
        int session = Dev.CurrentPeriod;
        try {
            storedlastprevious = Integer.parseInt(cmds[1][1][1]) + Integer.parseInt(cmds[1][1][2]);
            storedlastcurrent = Integer.parseInt(cmds[0][1][1]) + Integer.parseInt(cmds[0][1][2]);
            if (storedlastcurrent == 0) {
                session--;
                storedlastcurrent = storedlastprevious;
            }
            switch (markerType) {
                case ElectronicJournalConst.EJ_MT_SESSION_BEG:
                    marker[0] = session + "-1";
                    break;
                case ElectronicJournalConst.EJ_MT_SESSION_END:
                    marker[0] = (session == Dev.CurrentPeriod ? session - 1 : session) + "-" + storedlastprevious;
                    break;
                case ElectronicJournalConst.EJ_MT_DOCUMENT:
                case ElectronicJournalConst.EJ_MT_TAIL:
                    marker[0] = session + "-" + storedlastcurrent;
                    break;
                case ElectronicJournalConst.EJ_MT_HEAD:
                    marker[0] = "0-1";
            }
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_FAILURE, "Bad data structure: " + e.getMessage(), e);
        }
    }

    @Override
    public void retrieveMarker(int markerType, int sessionNumber, int documentNumber, String[] marker) throws JposException {
        Dev.check(Dev.CurrentPeriod == 0, JposConst.JPOS_E_NOEXIST, "Fiscal printer not yet fiscalized");
        char[] state = Dev.getCurrentState();
        Dev.check(sessionNumber < 0 || sessionNumber > Dev.CurrentPeriod || (sessionNumber == Dev.CurrentPeriod && markerType == ElectronicJournalConst.EJ_MT_SESSION_END), JposConst.JPOS_E_NOEXIST, "Invalid session: " + sessionNumber);
        String[][] cmd = new String[][]{(sessionNumber < Dev.CurrentPeriod
                ? new String[]{"get", "Memory", Long.toString(sessionNumber), "Fiscal", "Normal"}
                : new String[]{"get", "Total", "Fiscal", "Normal"}
        ), null};
        if (Dev.executeCommands(0, cmd) != 1)
            commandErrorException(cmd, null, false);
        int documentcount;
        try {
            documentcount = Integer.parseInt(cmd[1][1]) + Integer.parseInt(cmd[1][2]);
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_FAILURE, "Bad data structure: " + e.getMessage(), e);
        }
        if (markerType == ElectronicJournalConst.EJ_MT_SESSION_BEG) {
            Dev.check(documentcount == 0, JposConst.JPOS_E_NOEXIST, "No documents in session " + sessionNumber);
            marker[0] = sessionNumber + "-1";
        } else if (markerType == ElectronicJournalConst.EJ_MT_DOCUMENT) {
            Dev.check(documentcount < documentNumber || documentNumber < 1, JposConst.JPOS_E_NOEXIST, "Invalid document: " + documentNumber);
            marker[0] = sessionNumber + "-" + documentNumber;
        } else {
            marker[0] = sessionNumber + "-" + documentcount;
        }
    }

    @Override
    public void retrieveMarkersDateTime(String marker, String[] dateTime) throws JposException {
        String[] parts = checkMarker(marker);
        String[][]cmd = new String[][]{new String[]{"retrieveJournal", parts[0], parts[1], parts[1], "0"}, null};
        Dev.check (Dev.executeCommands(0, cmd) != 1 || cmd[1].length != 3 || !"1".equals(cmd[1][1]), JposConst.JPOS_E_NOEXIST, "Marker does not exist: " + marker);
        dateTime[0] = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(Long.parseLong(cmd[1][2]) * 1000));
    }

    private String[] checkMarker(String marker) throws JposException {
        String[] parts = marker.split("-");
        Dev.check(parts.length != 2, JposConst.JPOS_E_ILLEGAL, "Not a valid marker: " + marker);
        Dev.check(parts[0].matches(".*[^0-9].*") || parts[1].matches(".*[^0-9].*"), JposConst.JPOS_E_ILLEGAL, "Not a valid marker: " + marker);
        Dev.check(parts[0].length() > 5 || Integer.parseInt(parts[0]) > Dev.CurrentPeriod || parts[1].length() > 9, JposConst.JPOS_E_ILLEGAL, "Not a valid marker: " + marker);
        return parts;
    }

    private void commandErrorException(String[][] cmd, long[] allowed, boolean checkpaper) throws JposException {
        if (allowed == null)
            allowed = new long[]{CLOSED, ITEMIZING, PAYING, FINALIZING, NONFISCAL, BLOCKED};
        Dev.check(cmd[1] == null || cmd[1].length < 1 || cmd[1][0] == null || cmd[1][0].length() < DRAWER + 2, JposConst.JPOS_E_FAILURE, "Communication error");
        if (cmd[1][0].charAt(0) == SUCCESS)
            return;
        char[] state = cmd[1][0].substring(1).toCharArray();
        Dev.check(checkpaper && state[PRINTER] >= NEAREND, FiscalPrinterConst.JPOS_EFPTR_REC_EMPTY, "Change paper");
        Dev.check(!Dev.member(state[RECEIPT], allowed), FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Bad printer state");
        if (cmd[1][0].charAt(0) != SUCCESS && Dev.member(cmd[1].length, new long[]{2, 3})) {
            String cmdstr = cmd[0][0];
            for (int i = 1; i < cmd[0].length; i++)
                cmdstr = cmdstr + " ETB " + cmd[0][i];
            cmdstr = "Internal command [" + cmdstr + "] failed: ";
            Dev.check(cmd[1].length == 2 && cmd[1][1].equals("0"), JposConst.JPOS_E_FAILURE, cmdstr + "Invalid in current state [" + state + "]");
            Dev.check(cmd[1].length == 2, JposConst.JPOS_E_FAILURE, cmdstr + "Bad parameter " + cmd[1][1]);
            Dev.check(cmd[1].length == 3 , JposConst.JPOS_E_FAILURE, cmdstr + cmd[1][1] + " - " + cmd[1][2]);
        }
        throw new JposException(JposConst.JPOS_E_FAILURE, "Unknown error");
    }

    @Override
    public void updateState(boolean notused) {
        char[] state = Dev.getCurrentState();
        if (PowerNotify == JposConst.JPOS_PN_ENABLED) {
            int value = state.length <= DRAWER ? JposConst.JPOS_PS_OFF_OFFLINE : JposConst.JPOS_PS_ONLINE;
            new JposStatusUpdateEvent(EventSource, value).setAndCheckStatusProperties();
        }
        if (state.length > DRAWER) {
            long free = (Dev.MaxJournalSize > Dev.CurrentJournalSize ? Dev.MaxJournalSize - Dev.CurrentJournalSize : 0) * CURRENCYFACTOR;
            if (MediumFreeSpace > free) {
                MediumFreeSpace = free;
                EventSource.logSet("MediumFreeSpace");
            }
        }
    }
}
