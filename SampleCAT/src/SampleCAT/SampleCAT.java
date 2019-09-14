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
 *
 */

package SampleCAT;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.cat.*;
import de.gmxhome.conrad.jpos.jpos_base.electronicjournal.*;
import jpos.*;
import jpos.config.JposEntry;
import jpos.events.DirectIOEvent;
import jpos.events.DirectIOListener;
import jpos.events.StatusUpdateEvent;
import jpos.events.StatusUpdateListener;
import org.apache.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Implementation of CAT based for the sample implemented in SampleCAT.tcl.
 * Supported features are:
 * <br>- Sale, refund and void.
 * <br>- Start transaction before amount to approve is known.
 * <br>- Confirmation in case of signature based approval.
 * <br>- lock / unlock terminal.
 * <br>- All display texts will be sent to application while terminal is connected.
 * <br>- Ticket layout will be send after transaction (before optional confirmation).
 * <br>- Card issuer, transaction number, transaction date, tip, approved amount, approval result, terminal result,
 *       masked card number (last 4 digit readable), expiration date sent in case of successful operation.
 * <br>- Ticket data will either be provided to application via ElectronicJournal devices or via DirectIOEvents.<br>
 *       In the first case, QueryContent can be used to retrieve the tickets after authorization finished. Use SlipNumber
 *       as marker to retrieve ticket data for customer ticket (ElectronicJournal with Index = 0) or merchant ticket
 *       (ElectronicJournal with Index = 1) via QueryContent method and use EraseMedium to clear the journals afterwards.
 *       However, using EraseMedium after a transaction is optional as long as the journal medium is not full.<br>
 *       In the latter case, ticket data and count will be passed via DirectIOEvent. Event property Data contains the
 *       ticket count (1 or 2), Oj contains the ticket data [as one String, only control characters are LF ('\n')]. If
 *       ticket count is 1, only a customer ticket shall be printed. If count is 2, ticket data must be printed on a
 *       customer ticket as well as on a merchant ticket.
 */
public class SampleCAT extends JposDevice implements Runnable{
    /**
     * EventNumber of DirectIOEvent for display data. Property Data contains line number (starting from 1), Obj contains
     * String object with the display contents.
     */
    static public final int CAT_CMD_DISPLAY = 200;

    /**
     * EventNumber of DirectIOEvent for ticket data. Property Data contains the target (merchant, customer or both),
     * Obj contains String object with the ticket. The ticket contains no control characters except LF (0Ah, line feed).
     */
    static public final int CAT_CMD_TICKET = 201;

    /**
     * Data value of DirectIOEvent with EventNumber CAT_CMD_TICKET for customer tickets. Such a ticket should be printed
     * and handed over to the customer.
     */
    static public final int CAT_DATA_CUSTOMER = 1;

    /**
     * Data value of DirectIOEvent with EventNumber CAT_CMD_TICKET for merchant tickets. Such a ticket should be kept by
     * the merchant, in most cases with a customer signature.
     */
    static public final int CAT_DATA_MERCHANT = 2;

    /**
     * Data value of DirectIOEvent with EventNumber CAT_CMD_TICKET for merchant and customer tickets. Such a ticket should
     * be printed twice, one to be handed over to the customer and one to be kept by the merchant, in most cases with
     * customer signature.
     */
    static public final int CAT_DATA_BOTH = 3;      // ticket data for merchant and customer. No control characters except LF.

    private int OwnPort = 0;                // Default: OS generated random port
    private int RequestTimeout = 1000;      // Default: Service specific value
    private int CharacterTimeout = 50;      // Default: Service specific value for maximum delay between bytes belonging
                                            // to the same frame
    private int MinClaimTimeout = 100;      // Minimum claim timeout, to be used whenever timeout in claim() method is
                                            // too small to guarantee correct working of service, even if everything
                                            // is OK.
    private int JournalWidth = 32;          // Length of print line.
    private int DisplayWidth = 40;          // Length of display line.
    private int DisplayLines = 4;           // Number of display lines.
    private String DisplayName = "";        // Display name, if empty, use DirectIOEvents instead
    private String JournalPath = "";        // Path of electronic journal files. If empty use DirectIOEvent instead.
    private long JournalMaxSize = 1000;     // Maximum size of of electronic journal (in tickets).
    private long JournalLowSize = 10;       // Maximum free space (in tickets) to report nearly full.

    static private final int MinJournalWidth = 28;      // Minimum print line length.
    static private final int MinDisplayWidth = 20;      // Minimum display line length.
    static private final int MinDisplayLines = 2;       // Minimum display line length.
    static private final int MaxJournalWidth = 99;      // Maximum print line length.
    static private final int MaxDisplayWidth = 40;      // Maximum display line length.
    static private final int MaxDisplayLines = 4;       // Maximum display line length.

    // Communication properties
    private TcpClientIOProcessor OutStream = null;
    private Thread StateWatcher;
    private SyncObject WaitInitialized;
    private boolean ToBeFinished;
    private boolean InIOError = false;
    private int Online = 0;
    static private final byte STX = 2;
    static private final byte ETX = 3;
    static private final byte LF = 10;
    static private final byte FF = 12;

    private DisplayOutput Display = null;
    private TicketOutput  Ticket = null;
    private int[] JournalState = new int[]{0, 0};

    static private final long CURRENCYFACTOR = 10000;   // long value represents currency value / CURRENCYFACTOR, with 4 decimals.

    private class DisplayOutput {
        boolean Active;
        String[] Line = new String[]{"", "", "", ""};
        boolean[] PutOut = new boolean[]{true, true, true, true};
        DisplayOutput(){ Active = false;}
        void init() throws JposException {
            Active = true;
            for (int i = 0; i < Line.length; i++) {
                if (PutOut[i])
                    setLine(i, Line[i]);
            }
        }
        void release() { Active = false; }
        void setLine(int lineno, String contents) { Line[lineno] = contents; }
        void cleanup() {}

        /**
         * Line number of the sample terminal will be passed whenever changed. Since the sample terminal works with 4
         * lines with 40 columns each, the amount of lines and characters per line must be reduced if the application
         * supports less lines and / or less columns.
         * @param line      Line no. specified by terminal (in). Line no to be used during output (out).
         * @param data      Line contents to be used during output (out).
         * @param rows      No. of line to be used for output.
         * @param columns   Maximum columns per line to be used during output.
         * @return          true if output lines have been changed, else false.
         */
        boolean setLineData(int[] line, String[] data, int rows, int columns) {
            if (rows == 4) {
                data[0] = Line[line[0]];
                return true;
            }
            else if (rows == 3) {
                PutOut[3] = false;
                if (line[0] < 2) {
                    data[0] = Line[line[0]];
                }
                else {
                    line[0] = 2;
                    data[0] = Line[Line[2].equals("") ? 3 : 2];
                }
                return true;
            }
            if (rows == 2) {
                PutOut[1] = PutOut[3] = false;
                if (line[0] < 2) {
                    line[0] = 0;
                    int len = columns - Line[1].length() - 1;
                    data[0] = String.format("%" + -len + "s", Line[0]).substring(0, len) + " " + Line[1];
                }
                else {
                    line[0] = 1;
                    data[0] = Line[Line[2].equals("") ? 3 : 2];
                }
                return true;
            }
            else if (rows == 1) {
                PutOut[0] = PutOut[1] = PutOut[3] = false;
                if (line[0] >= 2){
                    line[0] = 0;
                    data[0] = Line[Line[2].equals("") ? 3 : 2];
                    return true;
                }
            }
            return false;
        }
    }

    // Ticket printer class
    private class TicketOutput {
        String TransactionDate = null;
        TicketOutput() {}
        void init() {}
        void release() {}
        void cleanup() {}
        void setTicket(int count, String contents) {}
    }

    // Implementation for DirectIO based operation.
    private class DisplayViaDirectIO extends DisplayOutput {
        DisplayViaDirectIO() { super(); }
        @Override
        void setLine(int number, String contents) {
            super.setLine(number, contents);
            if (Active) {
                int[] row = new int[]{number};
                String[] data = new String[1];
                if (setLineData(row, data, DisplayLines, DisplayWidth)) {
                    try {
                        handleEvent(new JposDirectIOEvent(getClaimingInstance(ClaimedCAT, 0).EventSource, CAT_CMD_DISPLAY, row[0], data[0]));
                    } catch (JposException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class TicketViaDirectIO extends TicketOutput {
        TicketViaDirectIO() { super(); }
        @Override
        void setTicket(int count, String contents) {
            super.setTicket(count, contents);
            try {
                handleEvent(new JposDirectIOEvent(getClaimingInstance(ClaimedCAT, 0).EventSource, CAT_CMD_TICKET, count == 1 ? CAT_DATA_CUSTOMER : CAT_DATA_BOTH, contents));
            } catch (JposException e) {
                e.printStackTrace();
            }
        }
    }

    // Implementation for use with LineDisplay

    static private final String UTF8 = "UTF-8";
    static private final String ANSI = "ISO-8859-1";
    static private final String ASCII = "US-ASCII";

    private class DisplayViaLD extends DisplayOutput implements StatusUpdateListener, DirectIOListener {
        LineDisplay Display;
        String Conversion = new String("");
        DisplayViaLD() throws JposException {
            super();
            Display = new LineDisplay();
            Display.addStatusUpdateListener(this);
            Display.addDirectIOListener(this);
            try {
                Display.open(DisplayName);
            } catch (JposException e) {
                e.printStackTrace();
                Display = null;
                throw new JposException(JposConst.JPOS_E_FAILURE, "Cannot access display " + DisplayName + ": " + e.getMessage(), e);
            }
        }

        @Override
        void cleanup() {
            if (Display != null) {
                try {
                    Display.close();
                } catch (JposException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        synchronized void init() throws JposException {
            if(Display != null) {
                try {
                    Display.claim(100);
                    Display.setDeviceEnabled(true);
                    if (Display.getCapMapCharacterSet()) {
                        Display.setMapCharacterSet(true);
                    }
                    else if (member(String.valueOf(LineDisplayConst.DISP_CS_UNICODE), Display.getCharacterSetList().split(","))) {
                        Display.setCharacterSet(LineDisplayConst.DISP_CS_UNICODE);
                    }
                    else if (!Display.getMapCharacterSet()) {
                        if (member(String.valueOf(LineDisplayConst.DISP_CS_ANSI), Display.getCharacterSetList().split(","))) {
                            Display.setCharacterSet(LineDisplayConst.DISP_CS_ANSI);
                            Conversion = ANSI;
                        }
                        else {
                            Display.setCharacterSet(LineDisplayConst.DISP_CS_ASCII);
                            Conversion = ASCII;
                        }
                    }
                } catch (JposException e) {
                    throw new JposException(JposConst.JPOS_E_FAILURE, "Display access error: " + e.getMessage(), e);
                }
            }
            super.init();
        }

        @Override
        synchronized void setLine(int number, String contents) {
            super.setLine(number, contents);
            if (Active) {
                if (Display != null) {
                    int[] row = new int[]{number};
                    String[] data = new String[1];
                    try {
                        if (setLineData(row, data, Display.getDeviceRows(), Display.getDeviceColumns())) {
                            displayTextLine(row[0], data[0]);
                        }
                    } catch (JposException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private void displayTextLine(int number, String contents) throws JposException {
            int columns = Display.getDeviceColumns();
            String s = String.format("%" + -columns + "s", contents).substring(0, columns);
            if (Conversion.length() > 0) {
                try {
                    byte[] source = s.getBytes(Conversion);
                    char[] target = new char[source.length];
                    for (int i = 0; i < source.length; i++) {
                        target[i] = (char) source[i];
                    }
                    s = new String(target);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            if (Display.getState() != JposConst.JPOS_S_CLOSED && Display.getDeviceEnabled())
                Display.displayTextAt(number, 0, s, LineDisplayConst.DISP_DT_NORMAL);
        }

        @Override
        synchronized void release() {
            if (Display != null) {
                try {
                    if (Display.getClaimed())
                        Display.release();
                } catch (JposException e) {
                    e.printStackTrace();
                }
            }
            super.release();
        }

        @Override
        public void statusUpdateOccurred(StatusUpdateEvent statusUpdateEvent) {}

        @Override
        public void directIOOccurred(DirectIOEvent directIOEvent) {}
    }

    // Implementation for use with electronic journal

    static final private int JRN_MAX_LINE_COUNT = 20;

    private class TicketViaEJ extends TicketOutput {
        static final int MARKSIZE = 14; // YYYYmmddHHMMSS
        static final int HEADSIZE = 10;
        final int FRAMESIZE = JRN_MAX_LINE_COUNT * JournalWidth;
        String Contents;
        int Count;
        RandomAccessFile[] DataFile = new RandomAccessFile[]{null, null};
        String[][] LastMarkers = new String[][]{new String[]{null, null}, new String[]{null, null}};
        long[][] LastPos = new long[][]{new long[]{0, 0}, new long[]{0, 0}};
        TicketViaEJ() {
            super();
        }
        @Override
        synchronized void init() {
            Count = 0;
            super.init();
        }

        @Override
        synchronized void setTicket(int count, String contents) {
            Count = count;
            Contents = contents;
            super.setTicket(count, contents);
        }

        @Override
        synchronized void release() {
            if (Count != 0) {
                try {
                    byte[] frame = Arrays.copyOf(TransactionDate.getBytes(UTF8), FRAMESIZE);
                    byte[] ticket = Contents.getBytes(UTF8);
                    long length;
                    System.arraycopy(ticket, 0, frame, MARKSIZE, ticket.length);
                    switch (Count) {
                        case 2:
                            writeTicket(1, frame);
                        case 1:
                            writeTicket(0, frame);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Count = 0;
                    closefile(0);
                    closefile(1);
                }
            }
            super.release();
        }

        private void writeTicket(int index, byte[] frame) throws IOException {
            long length;
            length = validateFile(index);
            DataFile[index].seek(length);
            DataFile[index].write(frame);
            ElectronicJournalProperties props = (ElectronicJournalProperties)getClaimingInstance(ClaimedElectronicJournal, index);
            if (props != null && props.DeviceEnabled) {
                int oldstate = JournalState[index];
                if ((length += frame.length) * CURRENCYFACTOR >= props.MediumSize) {
                    props.MediumFreeSpace = 0;
                    JournalState[index] = ElectronicJournalConst.EJ_SUE_MEDIUM_FULL;
                } else {
                    props.MediumFreeSpace = props.MediumSize - length * CURRENCYFACTOR;
                    if (props.MediumFreeSpace <= FRAMESIZE * CURRENCYFACTOR * JournalLowSize) {
                        JournalState[index] = ElectronicJournalConst.EJ_SUE_MEDIUM_NEAR_FULL;
                    }
                }
                if (oldstate != JournalState[index]) {
                    try {
                        handleEvent(new ElectronicJournalStatusUpdateEvent(props.EventSource, JournalState[index]));
                    } catch (JposException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private long validateFile(int index) throws IOException {
            final String[] namesuffix = new String[]{".customer.tickets", ".merchant.tickets"};
            try {
                DataFile[index] = new RandomAccessFile(JournalPath + namesuffix[index], "rwd");
                byte[] framesize = Arrays.copyOf(String.valueOf(FRAMESIZE).getBytes(), HEADSIZE);
                while (true) {
                    long len = DataFile[index].length();
                    if (len < HEADSIZE || len % FRAMESIZE != HEADSIZE) {
                        DataFile[index].seek(0);
                        DataFile[index].write(framesize);
                        DataFile[index].setLength(len = framesize.length);
                        return len;
                    } else {
                        byte[] storedsize = new byte[HEADSIZE];
                        DataFile[index].seek(0);
                        if (DataFile[index].read(storedsize) == storedsize.length && Arrays.equals(storedsize, framesize))
                            return len;
                        DataFile[index].setLength(0);
                    }
                }
            } catch (IOException e) {
                closefile(index);
                throw e;
            }
        }

        private void closefile(int index) {
            if (DataFile[index] != null) {
                try {
                    DataFile[index].close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    DataFile[index] = null;
                }
            }
        }

        /**
         * Retrieve marker for ticket access via electronic journal interface
         * @param index Journal index (0: customer tickets, 1: journal tickets)
         * @param type  marker type. Session specific marker are not supported.
         * @return marker if present, an empty string otherwise.
         */
        synchronized String retrieveMarker(int index, int type) {
            try {
                byte[] mark = new byte[MARKSIZE];
                long length = validateFile(index);
                if (length > HEADSIZE) {
                    long pos = 0;
                    switch (type) {
                        default:
                            return "";
                        case ElectronicJournalConst.EJ_MT_DOCUMENT:
                        case ElectronicJournalConst.EJ_MT_TAIL:
                            DataFile[index].seek(pos = length - FRAMESIZE);
                            break;
                        case ElectronicJournalConst.EJ_MT_HEAD:
                            DataFile[index].seek(pos = HEADSIZE);
                    }
                    DataFile[index].read(mark);
                    return storeLastMarker(index, pos, new String(mark));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                closefile(index);
            }
            return "";
        }

        synchronized String retrieveMarker(int index, int count, String date, long[] pos) {
            String retval = "";
            try {
                long dateval = Long.valueOf(date);
                byte[] buffer = new byte[date.length()];
                long length = validateFile(index);
                if (length == HEADSIZE)
                    return "";
                long from;
                long to = length / FRAMESIZE - 1;
                long currentval = readvalue(index, HEADSIZE, buffer);
                if (dateval < currentval)
                    return "";
                if (currentval == dateval) {
                    from = to = 0;
                }
                else {
                    if (readvalue(index, HEADSIZE + to * FRAMESIZE, buffer) < dateval)
                        return "";
                    from = 1;
                }
                while (from < to) {
                    long current = from + (to - from) / 2;
                    if (readvalue(index, HEADSIZE + current * FRAMESIZE, buffer) < dateval)
                        from = current + 1;
                    else
                        to = current;
                }
                buffer = new byte[MARKSIZE];
                if ((pos[0] = HEADSIZE + (from + count - 1) * FRAMESIZE) < length) {
                    readvalue(index, pos[0], buffer);
                    if (Long.valueOf(new String(buffer, 0, date.length())) == dateval) {
                        retval = storeLastMarker(index, pos[0], new String(buffer));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                closefile(index);
            }
            return retval;
        }

        synchronized private String storeLastMarker(int index, long pos, String marker) {
            for (int i = 0; i < LastMarkers[index].length; i++) {
                if (marker.equals(LastMarkers[index][i])) {
                    LastPos[index][i] = pos;
                    return marker;
                }
            }
            LastPos[index][1] = LastPos[index][0];
            LastMarkers[index][1] = LastMarkers[index][0];
            LastPos[index][0] = pos;
            return LastMarkers[index][0] = marker;
        }

        synchronized private void eraseLastMarkers(int index) {
            LastMarkers[index][0] = LastMarkers[index][1] = null;
        }

        synchronized private long getLastMarkerPosition(int index, String mark) {
            for(int i = LastMarkers[index].length - 1; i >= 0; --i) {
                if (mark.equals(LastMarkers[index][i])) {
                    return LastPos[index][i];
                }
            }
            return 0;
        }

        private long readvalue(int index, long pos, byte[] buffer) throws IOException {
            DataFile[index].seek(pos);
            if (DataFile[index].read(buffer) < buffer.length)
                throw new IOException("Insufficient data");
            try {
                return Long.valueOf(new String(buffer));
            } catch (NumberFormatException e) {
                throw new IOException("Bad data format");
            }
        }

        /**
         * Write the tickets within the specified marker range into the specified file
         * @param index     Journal index (0: customer tickets, 1: journal tickets).
         * @param from      Marker of the first ticket to be printed, "" to print from beginning.
         * @param to        Marker of the last ticket to be printed, "" to print to end.
         * @param filename  Name of target file. Tickets are separated with control character FF, line separator is LF.
         *                  No further control characters will be used.
         */
        synchronized void getTickets(int index, String from, String to, String filename) throws JposException {
            if (new File(filename).exists())
                throw new JposException(JposConst.JPOS_E_EXISTS, "File exists: " + filename);
            RandomAccessFile target = null;
            try {
                byte[] frame = new byte[FRAMESIZE];
                long[] pos = new long[]{HEADSIZE};
                if (!from.equals("")) {
                    if ((pos[0] = getLastMarkerPosition(index, from)) == 0 && retrieveMarker(index, 1, from, pos).equals(""))
                        throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid from marker");
                }
                long[] filereadend = new long[]{validateFile(index)};
                if (!to.equals("")) {
                    filereadend[0] = getLastMarkerPosition(index, to);
                    if (filereadend[0] == 0 && retrieveMarker(index, 1, to, filereadend).equals(""))
                        throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid to marker");
                    filereadend[0] += FRAMESIZE;
                }
                if (pos[0] >= filereadend[0])
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "To marker before from marker");
                DataFile[index].seek(pos[0]);
                for (long currentpos = pos[0]; currentpos < filereadend[0]; currentpos += frame.length) {
                    check(DataFile[index].read(frame) < frame.length, JposConst.JPOS_E_FAILURE, "Journal corrupted");
                    for (int i = 0; i < frame.length - MARKSIZE; i++) {
                        if (frame[i + MARKSIZE] == 0) {
                            if (i > 0) {
                                try {
                                    if (target == null) {
                                        (target = new RandomAccessFile(filename, "rw")).write(frame, MARKSIZE, i);
                                    } else {
                                        frame[MARKSIZE - 1] = FF;
                                        target.write(frame, MARKSIZE - 1, i + 1);
                                    }
                                } catch (IOException e) {
                                    throw new JposException(JposConst.JPOS_E_FAILURE, "Data file error: " + e.getMessage(), e);
                                }
                            }
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                throw new JposException(JposConst.JPOS_E_FAILURE, "Journal file error: " + e.getMessage(), e);
            } catch (NumberFormatException e) {
                throw new JposException(JposConst.JPOS_E_NOEXIST, "Invalid end marker", e);
            } finally {
                try {
                    if (target != null)
                        target.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                closefile(index);
            }
        }
    }

    /**
     * Constructor. ID is the network address of the credit authorisation terminal (CAT).
     *
     * @param id    Network address of the display controller.
     */
    protected SampleCAT(String id) {
        super(id);
        cATInit(1);
        electronicJournalInit(2);
        PhysicalDeviceDescription = "CAT simulator for TCP";
        PhysicalDeviceName = "CAT Simulator";
        CapPowerReporting = JposConst.JPOS_PR_STANDARD;
    }

    @Override
    public void checkProperties(JposEntry entry) throws JposException {
        super.checkProperties(entry);
        try {
            Object o;
            int value;
            if ((o = entry.getPropertyValue("ClientPort")) != null && (value = Integer.parseInt(o.toString())) >= 0 && value <= 0xffff)
                OwnPort = value;
            if ((o = entry.getPropertyValue("RequestTimeout")) != null && (value = Integer.parseInt(o.toString())) > 0)
                RequestTimeout = value;
            if ((o = entry.getPropertyValue("CharacterTimeout")) != null && (value = Integer.parseInt(o.toString())) > 0)
                CharacterTimeout = value;
            if ((o = entry.getPropertyValue("MinClaimTimeout")) != null && (value = Integer.parseInt(o.toString())) >= 0)
                MinClaimTimeout = value;
            if ((o = entry.getPropertyValue("JournalWidth")) != null && (value = Integer.parseInt(o.toString())) >= 0 && MinJournalWidth <= value && value <= MaxJournalWidth)
                JournalWidth = value;
            if ((o = entry.getPropertyValue("DisplayWidth")) != null && (value = Integer.parseInt(o.toString())) >= 0 && MinDisplayWidth <= value && value <= MaxDisplayWidth)
                DisplayWidth = value;
            if ((o = entry.getPropertyValue("DisplayLines")) != null && (value = Integer.parseInt(o.toString())) >= 0 && MinDisplayLines <= value && value <= MaxDisplayLines)
                DisplayLines = value;
            if ((o = entry.getPropertyValue("JournalPath")) != null)
                JournalPath = o.toString();
            if ((o = entry.getPropertyValue("JournalMaxSize")) != null && (value = Integer.parseInt(o.toString())) >= 0 && MinDisplayWidth <= value && value <= MaxDisplayWidth)
                JournalMaxSize = value;
            if ((o = entry.getPropertyValue("JournalLowSize")) != null && (value = Integer.parseInt(o.toString())) >= 0 && MinDisplayWidth <= value && value <= MaxDisplayWidth)
                JournalLowSize = value;
            if ((o = entry.getPropertyValue("DisplayName")) != null)
                DisplayName = o.toString();
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property", e);
        }
        if (Display == null)
            Display = DisplayName.equals("") ? new DisplayViaDirectIO() : new DisplayViaLD();
        if (Ticket == null)
            Ticket = JournalPath.equals("") ? new TicketViaDirectIO() : new TicketViaEJ();
    }

    @Override
    public void changeDefaults(CATProperties props) {
        props.DeviceServiceDescription = "CAT service for sample CAT";
        CapPowerReporting = JposConst.JPOS_PR_STANDARD;
        props.DeviceServiceVersion = 1014001;
        props.CapAuthorizeRefund = true;
        props.CapAuthorizeVoid = true;
        props.CapCenterResultCode = true;
        props.CapAdditionalSecurityInformation = true;
    }

    @Override
    public void changeDefaults(ElectronicJournalProperties props) {
        props.DeviceServiceDescription = "ElectronicJournal service for sample CAT";
        props.DeviceServiceVersion = 1014001;
        props.CapErasableMedium = true;
        props.CapRetrieveCurrentMarker = true;
        props.CapRetrieveMarkerByDateTime = true;
        props.MediumSizeDef = (JournalMaxSize * JournalWidth * JRN_MAX_LINE_COUNT + TicketViaEJ.HEADSIZE) * CURRENCYFACTOR;
    }

    @Override
    public boolean removePropertySet(JposCommonProperties props) throws JposException {
        if (super.removePropertySet(props)) {
            if (Display != null) {
                Display.cleanup();
                Display = null;
            }
            if (Ticket != null) {
                Ticket.cleanup();
                Ticket = null;
            }
            return true;
        }
        return false;
    }

    private class StreamReader extends Thread {
        /**
         * If not 0, the first byte of the frame we are waiting for.
         */
        char ResultHead = 0;

        private SyncObject FrameReader = new SyncObject();
        private String TheFrame = null;

        final private int BIGTIMEOUT = 1000000000;  // One billion milliseconds

        StreamReader(String name) {
            super(name);
            start();
        }

        @Override
        public void run() {
            TcpClientIOProcessor stream = OutStream;
            byte[] frame = new byte[0];
            while (stream != null) {
                try {
                    stream.setTimeout(frame.length == 0 ? BIGTIMEOUT : CharacterTimeout);
                    byte[] part1 = stream.read(1);
                    if (part1.length > 0) {
                        int count = stream.available();
                        byte[] part2 = count > 0 ? stream.read(count) : new byte[0];
                        byte[] data = Arrays.copyOf(frame, frame.length + part1.length + part2.length);
                        System.arraycopy(part1, 0, data, frame.length, part1.length);
                        System.arraycopy(part2, 0, data, frame.length + part1.length, part2.length);
                        for (int i = frame.length; i < data.length; i++) {
                            if (data[i] == ETX) {
                                processFrame(new String(Arrays.copyOf(data, i), UTF8));
                                data = Arrays.copyOfRange(data, i + 1, data.length);
                                i = -1;
                            }
                        }
                        frame = data;
                    }
                    else if (frame.length > 0) {
                        log(Level.DEBUG, ID + ": Incomplete frame discarded.");
                        frame = new byte[0];
                    }
                } catch (JposException e) {
                    closePort();
                    FrameReader.signal();
                    break;
                } catch (Exception e) {
                    e.printStackTrace();;
                }
            }
        }

        private void processFrame(String frame) {
            synchronized(this) {
                if (frame.charAt(0) == ResultHead) {
                    TheFrame = frame;
                    FrameReader.signal();
                    return;
                }
            }
            String logtext = null;
            switch (frame.charAt(0)) {
                case 'D':   // Display text
                {
                    String[] parts = frame.split("\2");
                    String data = parts.length >= 2 ? parts[1] : "";
                    int line = Integer.parseInt(parts[0].substring(1));
                    if (Display != null)
                        Display.setLine(line, data);
                    else
                        System.out.println("Display line " + line + ": " + data);
                    break;
                }
                case 'P':   // Ticket data
                {
                    String[] parts = frame.split("\2");
                    String data = parts.length >= 2 ? parts[1] : "";
                    int count = Integer.parseInt(parts[0].substring(1));
                    if (Ticket != null)
                        Ticket.setTicket(count, data);
                    else
                        System.out.println("Print ticket " + count + " times:\n" + data + "\n");
                    break;
                }
                case 'L':   // Lock response
                    logtext = "lock";
                case 'U':   // Unlock response
                    if (logtext == null)
                        logtext = "unlock";
                case 'B':   // Begin transaction response
                    if (logtext == null)
                        logtext = "transaction commit";
                case 'E':   // Command end sequence
                    if (logtext == null)
                        logtext = "authorization end";
                    System.out.println("Unexpected " + logtext + " response, will be ignored.");
            }
        }

        /**
         * Sets response frame type for later reading. Should be set immediately before the corresponding command
         * will be sent.
         * @param frametype Header byte of expected command
         */
        void prepareReadResponse(char frametype) {
            ResultHead = frametype;
        }

        /**
         * Retrieves response read by the StreamReader. On timeout, send cancel command.
         * @return             The response or empty string in case of timeout or error.
         */
        String readFrame(int timeout) {
            if (ResultHead == 0)
                return "";
            TcpClientIOProcessor stream;
            if (!FrameReader.suspend(timeout) && (stream = OutStream) != null) {
                try {
                    stream.write(new byte[]{'a', ETX});
                } catch (JposException e) {}
            }
            synchronized(this) {
                String response = TheFrame;
                TheFrame = null;
                ResultHead = 0;
                return response == null ? "" : response;
            }
        }
    }

    StreamReader ReadThread;

    /**
     * Closes the port
     * @return In case of an IO error, the corresponding exception. Otherwise null
     */
    private JposException closePort() {
        JposException e = null;
        if (OutStream != null) {
            try {
                OutStream.close();
            } catch (JposException ee) {
                e = ee;
            }
            OutStream = null;
        }
        return e;
    }

    /**
     * Port initialization.
     * @return In case of initialization error, the exception. Otherwise null.
     */
    private JposException initPort() {
        try {
            OutStream = new TcpClientIOProcessor(this, ID);
            OutStream.setParam(OwnPort);
            OutStream.open(InIOError);
            InIOError = false;
            ReadThread = new StreamReader("StreamReader_" + ID);
        } catch (JposException e) {
            OutStream = null;
            return e;
        }
        return null;
    }

    /**
     * Method to perform any command, Keep in mind that commands normally generate no response.
     * @param command Command data to be sent, without final ETX.
     * @param resptype Header byte of expected response. If no response is expected: 0.
     * @param timeout  Timeout for whole operation.
     * @return JposException in error case, null if no error occurred.
     */
    public Object sendRecv(String command, char resptype, int timeout) {
        TcpClientIOProcessor stream;
        StreamReader reader;
        synchronized(this) {
            if (OutStream == null) {
                JposException e = initPort();
                if (e != null) {
                    return e;
                }
            }
            stream = OutStream;
            reader = ReadThread;
        }
        try {
            byte[] request = (command + "\3").getBytes(UTF8);
            reader.prepareReadResponse(resptype);
            stream.write(request);
            return resptype == 0 ? "" : reader.readFrame(timeout);
        } catch (Exception e) {
            log(Level.TRACE, ID + ": IO error: " + e.getMessage());
            closePort();
            InIOError = true;
            return e instanceof JposException ? (JposException)e : new JposException(JposConst.JPOS_E_ILLEGAL, UniqueIOProcessor.IOProcessorError, e.getMessage());
        }
    }

    /**
     * Method to send an abort command without waiting for the response.
     * @return JposException in error case, null if no error occurred.
     */
    public Object sendAbort() {
        TcpClientIOProcessor stream;
        synchronized(this) {
            if (OutStream == null) {
                JposException e = initPort();
                if (e != null) {
                    return e;
                }
            }
            stream = OutStream;
        }
        try {
            byte[] request = ("a\3").getBytes(UTF8);
            stream.write(request);
            return "";
        } catch (Exception e) {
            log(Level.TRACE, ID + ": IO error: " + e.getMessage());
            closePort();
            InIOError = true;
            return e instanceof JposException ? (JposException)e : new JposException(JposConst.JPOS_E_ILLEGAL, UniqueIOProcessor.IOProcessorError, e.getMessage());
        }
    }

    /**
     * Internal command to set ticket line width.
     * @param width Width to be used for printing. Must be between 28 and 99.
     * @throws JposException, if a communication error occurs.
     */
    private void setPrintWidth(int width){
        Object o = sendRecv(String.format("p%d", width), '\0', 0);
    }

    /**
     * Internal command to lock or unlock the terminal.
     * @param lock      If set, lock the terminal. Otherwise, unlock it.
     * @param timeout   Timeout for answer-back.
     * @throws JposException    Communication error occurred.
     */
    private void lock(boolean lock, int timeout) throws JposException {
        Object o = sendRecv(lock ? "l1" : "l0", lock ? 'L' : 'U', timeout);
        if (o instanceof JposException)
            throw (JposException) o;
        String resp = (String) o;
        if (resp.length() == 0)
            throw new JposException(JposConst.JPOS_E_TIMEOUT, 0, "No valid response within " + timeout + " milliseconds");
        // This function fails only if the terminal is just in the requested state. Therefore, errors will be ignored.
    }

    /**
     * Begin transaction. Should be called after starting receipt to allow card swipe for fast payment. Amount and
     * transaction type (sale, refund or void) will be sent when authorization will be required. Alternatively, the
     * terminal can be unlocked or transaction can be cancelled.
     * @param timeout   Timeout for answer-back.
     * @throws JposException    Communication error or terminal in wrong state.
     */
    private void beginAuthorization (int timeout) throws JposException {
        Object o = sendRecv("b", 'B', timeout);
        if (o instanceof JposException)
            throw (JposException) o;
        String resp = (String) o;
        if (resp.length() == 0)
            throw new JposException(JposConst.JPOS_E_TIMEOUT, 0, "No valid response within " + timeout + " milliseconds");
        int code = Integer.parseInt(resp.substring(1));
        switch (code) {
            case 4:
                throw new JposException(JposConst.JPOS_E_EXTENDED, CATConst.JPOS_ECAT_RESET, "Terminal locked");
            case 6:
                throw new JposException(JposConst.JPOS_E_EXTENDED, CATConst.JPOS_ECAT_COMMANDERROR, "Confirmation requested");
            case 7:
                throw new JposException(JposConst.JPOS_E_EXTENDED, CATConst.JPOS_ECAT_COMMANDERROR, "Authorization just activated");
        }
    }

    /**
     * Abort a transaction.
     * @throws JposException    Communication error.
     */
    private void abort() throws JposException {
        Object o = sendAbort();
        if (o instanceof JposException)
            throw (JposException) o;
    }

    /**
     * Transaction confirmation. Will be requested after signature-based sale authorization.
     * @param transno   Transaction number of corresponding sale transaction.
     * @param committed True if confirmation was successful, false if confirmation failed
     * @param timeout   Timeout for answer-back.
     * @throws JposException    Communication error or terminal in wrong state.
     */
    private void confirm(int transno, boolean committed, int timeout) throws JposException {
        Object o = sendRecv(String.format("c%d\2%d", transno, committed ? 1 : 0), 'E', timeout);
        if (o instanceof JposException)
            throw (JposException) o;
        String resp = (String) o;
        if (resp.length() == 0)
            throw new JposException(JposConst.JPOS_E_TIMEOUT, 0, "No valid response within " + timeout + " milliseconds");
        int code = Integer.parseInt(resp.substring(1));
        switch (code) {
            case 4: // Device locked:
                throw new JposException(JposConst.JPOS_E_EXTENDED, CATConst.JPOS_ECAT_RESET, "Terminal locked");
            case 5: // Not in transaction: Ignore.
                throw new JposException(JposConst.JPOS_E_EXTENDED, CATConst.JPOS_ECAT_COMMANDERROR, "Not in transaction");
            case 7: // No confirmation requested.
                throw new JposException(JposConst.JPOS_E_EXTENDED, CATConst.JPOS_ECAT_COMMANDERROR, "No confirmation requested");
            case 8: // Invalid transaction number:
                throw new JposException(JposConst.JPOS_E_ILLEGAL, code, "Invalid transaction number: " + transno);
        }
    }

    /**
     * Sale or refund operation. Allowed after successful transaction start.
     * @param refund    True for refund operation, false for normal sale.
     * @param amount    Sale or refund amount (positive value).
     * @param timeout   Timeout for answer-back. Should be long enough (about 90 - 300 seconds).
     * @throws JposException    Communication error or terminal in wrong state.
     */
    private void sale(boolean refund, long amount, int timeout) throws JposException {
        Object o = sendRecv(String.format("%c%f", refund ? 'r' : 's', (double)amount / 10000.0), 'E', timeout);
        if (o instanceof JposException)
            throw (JposException) o;
        String resp = (String) o;
        if (resp.length() == 0)
            throw new JposException(JposConst.JPOS_E_TIMEOUT, 0, "No valid response within " + timeout + " milliseconds");
        int code = Integer.parseInt(resp.substring(1, resp.indexOf(STX)));
        switch (code) {
            case 4: // Device locked:
                throw new JposException(JposConst.JPOS_E_EXTENDED, CATConst.JPOS_ECAT_RESET, "Terminal locked");
            case 5: // Not in transaction: Ignore.
                throw new JposException(JposConst.JPOS_E_EXTENDED, CATConst.JPOS_ECAT_COMMANDERROR, "Not in transaction");
            case 6: // Waiting for commit.
                throw new JposException(JposConst.JPOS_E_EXTENDED, CATConst.JPOS_ECAT_COMMANDERROR, "Waiting for commit");
            case 7: // No confirmation requested.
                throw new JposException(JposConst.JPOS_E_EXTENDED, CATConst.JPOS_ECAT_COMMANDERROR, "Authorization active");
            case 0: // Transaction OK
            case 1: // Wait for commit
            case 2: // Authorization failure
                setTransactionProperties(refund ? 1 : 0, resp);
        }
    }

    /**
     * Void operation. Allowed after successful transaction start.
     * @param transno   Transaction number of transaction to be voided.
     * @param timeout   Timeout for answer-back.
     * @throws JposException    Communication error or terminal in wrong state.
     */
    private void rollback(int transno, int timeout) throws JposException {
        Object o = sendRecv(String.format("v%d", transno), 'E', timeout);
        if (o instanceof JposException)
            throw (JposException) o;
        String resp = (String) o;
        if (resp.length() == 0)
            throw new JposException(JposConst.JPOS_E_TIMEOUT, 0, "No valid response within " + timeout + " milliseconds");
        int code = Integer.parseInt(resp.substring(1, resp.indexOf(STX)));
        switch (code) {
            case 4: // Device locked:
                throw new JposException(JposConst.JPOS_E_EXTENDED, CATConst.JPOS_ECAT_RESET, "Terminal locked");
            case 5: // Not in transaction: Ignore.
                throw new JposException(JposConst.JPOS_E_EXTENDED, CATConst.JPOS_ECAT_COMMANDERROR, "Not in transaction");
            case 6: // Waiting for commit.
                throw new JposException(JposConst.JPOS_E_EXTENDED, CATConst.JPOS_ECAT_COMMANDERROR, "Waiting for commit");
            case 7: // No confirmation requested.
                throw new JposException(JposConst.JPOS_E_EXTENDED, CATConst.JPOS_ECAT_COMMANDERROR, "Authorization active");
            case 8: // No confirmation requested.
                throw new JposException(JposConst.JPOS_E_ILLEGAL, code, "Invalid transaction: " + transno);
            case 0: // Transaction OK
            case 1: // Wait for commit
            case 2: // Authorization failure
                setTransactionProperties(2, resp);
        }
    }

    /**
     * Sets CAT properties to the appropriate values returned by the device. Use the following mapping:
     * <br> - CenterResultCode: approval result,
     * <br> - Balance: Amount (inclusive tip, if added),
     * <br> - CardCompanyID: Card issuer,
     * <br> - AccountNumber: Card number (4 last digits left unchanged),
     * <br> - ApprovalCode: Expiration date,
     * <br> - AdditionalSecurityInformation: transaction number,
     * <br> - SlipNumber: Transaction date / time.
     * <br>If property AdditionalSecurityInformation will not be changed by the application, any void
     * operation will use the transaction number of the previously performed operation.
     * @param what  Flag: 0: sale, 1: refund, 2: void
     * @param resp  Response string as received by sendRecv().
     * @throws JposException If response doesnot contain the expected fields or non-numeric amount.
     */
    private void setTransactionProperties(int what, String resp) throws JposException {
        String[] params = resp.substring(resp.indexOf('\2') + 1).split("\2");
        if (params.length >= 9) {
            CATProperties props = (CATProperties)getClaimingInstance(ClaimedCAT, 0);
            props.CenterResultCode = params[1];
            try {
                props.Balance = (long) (Double.parseDouble(params[2]) * 10000);
            } catch (Exception e) {
                throw new JposException(JposConst.JPOS_E_ILLEGAL, 0, "Illegal balance (" + params[2] + "): " + e.getMessage());
            }
            props.CardCompanyID = params[4];
            props.AccountNumber = params[5];
            props.ApprovalCode = params[6];
            props.AdditionalSecurityInformation = params[7];
            props.SlipNumber = params[8].substring(0, 8) + params[8].substring(9);
            props.PaymentMedia = CATConst.CAT_MEDIA_CREDIT;
            props.PaymentCondition = CATConst.CAT_PAYMENT_DEBIT;
            switch (what) {
                case 0: // Sale transaction
                    props.TransactionType = CATConst.CAT_TRANSACTION_SALES;
                    break;
                case 1: // Refund operation
                    props.TransactionType = CATConst.CAT_TRANSACTION_REFUND;
                    break;
                case 2: // Void operation
                    props.TransactionType = CATConst.CAT_TRANSACTION_VOID;
            }
            switch (Integer.parseInt(params[0])) {
                case 100:   // Abort by user
                    throw new JposException(JposConst.JPOS_E_EXTENDED, CATConst.JPOS_ECAT_RESET, "Aborted by customer");
                case 101:   // Card locked
                    throw new JposException(JposConst.JPOS_E_EXTENDED, CATConst.JPOS_ECAT_CENTERERROR, "Card locked");
                case 102:   // Retain card
                    throw new JposException(JposConst.JPOS_E_EXTENDED, CATConst.JPOS_ECAT_CENTERERROR, "Retain card");
                case 103:   // Card error
                    throw new JposException(JposConst.JPOS_E_EXTENDED, CATConst.JPOS_ECAT_CENTERERROR, "Card error");
                case 104:   // Approval error
                    throw new JposException(JposConst.JPOS_E_EXTENDED, CATConst.JPOS_ECAT_CENTERERROR, "Approval error");
            }
        }
        else
            throw new JposException(JposConst.JPOS_E_EXTENDED, CATConst.JPOS_ECAT_COMMANDERROR, "Invalid response: " + resp);
    }

    @Override
    public void run() {
        boolean oldInError = InIOError;
        JposCommonProperties props;
        while (!ToBeFinished) {
            try {
                StreamReader reader = ReadThread;
                if (reader != null) {
                    reader.join();
                }
                else {
                    new SyncObject().suspend(CharacterTimeout);
                }
                setPrintWidth(JournalWidth);
                if (oldInError != InIOError) {
                    oldInError = !oldInError;
                    if ((props = getClaimingInstance(ClaimedCAT, 0)) != null)
                        handleEvent(new JposStatusUpdateEvent(props.EventSource, oldInError ? JposConst.JPOS_SUE_POWER_OFF_OFFLINE : JposConst.JPOS_SUE_POWER_ONLINE));
                }
            } catch(Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public CATProperties getCATProperties(int index) {
        return new SampleCATAccessor(index);
    }
    /**
     * Sample device specific accessor class. The device uses the following commands:
     *<br>-		p%d\3			Set print line width. Parameters: Line width (must be &ge; 28).
     *<br>-		l%d\3			Lock or unlock terminal. Parameters: 0: unlock, 1: lock
     *<br>-		b\3				Begin transaction. s, v or r must follow.
     *<br>-		s%f\3			Set sale amount. Parameters: Amount.
     *<br>-		c%d\2%d\3		Commit operation. Parameters: No. of transaction to be committed, result (0: Verification
          						error, 1: Signature verified). Mandatory after sign-based sale operations.
     *<br>-		r%f\3			Set return amount. Parameters: Amount.
     *<br>-		v%d				Void transaction. Parameters: No. of transaction to be voided.
     *<br>-		a\3				Abort operation.
     *<br>In addition, the device sends the following responses:
     *<br>-	L%d\3										Lock terminal. Parameters: Result code (0: OK, 4: just locked).
     *<br>-	U%d\3										Unlock terminal. Parameters: Result code (0: OK, 4: just unlocked).
     *<br>-	B%d\3										Begin transaction. Parameters: Result code (0: OK, 4: just locked,
                                                        6: waiting for commit, 7: authorization active).
     *<br>-	E%d\3										End. Parameters: Result code (0: OK, 3: Abort, 4: locked, 5: no
     	    											transaction, 6: wait for commit, 7: other operation active,
        												8: invalid transaction).
     *<br>-	E%d\2%s\2%s\2%s\2%s\2%s\2%s\2%s\2%s\2%s\3 	End processing. Parameters: Result code (0: OK, 1: wait for commit,
     			    									2: Error), Result code (0: no error), approval result (0: OK, 1111:
     		    										check, else error), balance, tip (included in balance), card issuer
     	    											(abbreviation, see IssuerList), card no (last 4 digits), expiration
        												date, transaction number, transaction time (format YYYYmmddTHHMMSS).
     *<br>The device sends the following status messages:
     *<br> 	D%d\2%s\3					Display line. Parameters: line no (0-3), contents (UTF-8).
     *<br>	P%d\2%s\3					Print ticket. Parameters: count (1-2), ticket data (UTF-8), may contain line feeds.
     *
     * <br>- Connect via TCP
     */
    class SampleCATAccessor extends CATProperties {
        /**
         * Constructor.
         * @param index Property set index used by this accessor.
         */
        public SampleCATAccessor(int index) {
            super(index);
        }

        @Override
        public void claim(int timeout) throws JposException {
            if (timeout < MinClaimTimeout)
                timeout = MinClaimTimeout;
            super.claim(timeout);
            setPrintWidth(JournalWidth);
            if (InIOError) {
                release();
                throw new JposException(JposConst.JPOS_E_NOHARDWARE, "CAT not detected");
            }
        }

        @Override
        public void release() throws JposException {
            ToBeFinished = true;
            synchronized(SampleCAT.this) {
                closePort();
            }
            while (ToBeFinished) {
                try {
                    StateWatcher.join();
                } catch (Exception e) {}
                break;
            }
            StateWatcher = null;
            Online = 0;
            PowerState = JposConst.JPOS_PS_UNKNOWN;
            EventSource.logSet("PowerState");
            InIOError = false;
            super.release();
        }

        @Override
        public void checkHealth(int level) throws JposException {
            CheckHealthText = InIOError ? "Internal CheckHealth: OFFLINE" : "Internal CheckHealth: OK";
            EventSource.logSet("CheckHealthText");
        }

        @Override
        public void deviceEnabled(boolean enable) throws JposException {
            lock(!enable, RequestTimeout);
            super.deviceEnabled(enable);
        }

        @Override
        public void authorizeSales(AuthorizeSales request) throws JposException  {
            Display.init();
            Ticket.init();
            try {
                long starttime = System.currentTimeMillis();
                beginAuthorization(request.getTimeout());
                long deltatime = System.currentTimeMillis() - starttime;
                if (deltatime < request.getTimeout()) {
                    sale(false, request.getAmount() + request.getTaxOthers(), (int) (request.getTimeout() - deltatime));
                    deltatime = System.currentTimeMillis() - starttime;
                    if (Integer.parseInt(CenterResultCode) != 0 && deltatime < request.getTimeout()) {
                        confirm(Integer.parseInt(AdditionalSecurityInformation), true, (int) (request.getTimeout() - deltatime));
                    }
                    SequenceNumber = request.getSequenceNumber();
                }
            } finally {
                Ticket.TransactionDate = SlipNumber;
                Ticket.release();
                Display.release();
            }
        }

        @Override
        public AuthorizeVoid authorizeVoid(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
            // AdditionalSecurityInformation must be an integer value, containing the transaction number generated by
            // the device.
            try {
                Integer.parseInt(AdditionalSecurityInformation);
            }
            catch (Exception e) {
                check(true, JposConst.JPOS_E_ILLEGAL, "AdditionalSecurityInformation (device transaction number) invalid");
            }
            return super.authorizeVoid(sequenceNumber, amount, taxOthers, timeout);
        }

        @Override
        public void authorizeVoid(AuthorizeVoid request) throws JposException  {
            Display.init();
            Ticket.init();
            try {
                long starttime = System.currentTimeMillis();
                beginAuthorization(request.getTimeout());
                long deltatime = System.currentTimeMillis() - starttime;
                if (deltatime < request.getTimeout()) {
                    rollback(Integer.parseInt(request.getAdditionalSecurityInformation()), (int) (request.getTimeout() - deltatime));
                    SequenceNumber = request.getSequenceNumber();
                }
            } finally {
                Ticket.TransactionDate = SlipNumber;
                Ticket.release();
                Display.release();
            }
        }

        @Override
        public void authorizeRefund(AuthorizeRefund request) throws JposException  {
            Display.init();
            Ticket.init();
            try {
                long starttime = System.currentTimeMillis();
                beginAuthorization(request.getTimeout());
                long deltatime = System.currentTimeMillis() - starttime;
                if (deltatime < request.getTimeout()) {
                    sale(true, request.getAmount() + request.getTaxOthers(), (int) (request.getTimeout() - deltatime));
                    SequenceNumber = request.getSequenceNumber();
                }
            } finally {
                Ticket.TransactionDate = SlipNumber;
                Ticket.release();
                Display.release();
            }
        }

        @Override
        public void clearOutput() throws JposException {
            abort();
            super.clearOutput();
        }
    }

    @Override
    public ElectronicJournalProperties getElectronicJournalProperties(int index) {
        return new SampleElectronicJournalAccessor(index);
    }
    /**
     * Sample device specific accessor class, bound to the credit authorization terminal service.
     * This implementation provides up to two electronic journal devices, depending on the CAT device
     * configuration:
     * <br>- If a path prefix has been specified in jpos.xml (entry <b>JournalPath</b>), ElectronicJournal
     * services are available. They use the device indices 0 (for customer tickets) and 1 (for merchant tickets).
     * <br>- Otherwise, ticket data will be passed to the application via DirectIOEvents, fired by the CAT service.
     * <br>If available, the ElectronicJournal devices work as follows:
     * <br>- The ElectronicJournal will be written automatically whenever ticket data are received from the terminal.
     * <br>- The transaction time stamp will be used as markers for the corresponding tickets.
     * <br>- All ticket data will be written to electronic journal wih index 0.
     * <br>- All ticket data that shall be printed twice will be written to electronic journal with index 1 as well.
     * <br>- The application can retrieve ticket data with the QueryContent method. Print methods are not available
     * because the sample device has no printer.
     * <br>- To be able to use QueryContent, methods RetrieveCurrentMarker and RetrieveMarkerByDateTime can be used. In
     * addition, the contents of the CAT property SlipNumber can be used as marker.
     * <br>- Each electronic journal can be cleared with the EraseMedium method. This method must be called whenever
     * property MediumFreeSpace is 0.
     */
    class SampleElectronicJournalAccessor extends ElectronicJournalProperties {
        /**
         * Constructor.
         *
         * @param index Property set used by this accessor.
         */
        public SampleElectronicJournalAccessor(int index) {
            super(index);
        }

        @Override
        public void checkHealth(int level) throws JposException {
            CheckHealthText = "Internal CheckHealth: OK";
            EventSource.logSet("CheckHealthText");
        }

        @Override
        public void claim(int timeout) throws JposException {
            check(Ticket == null || !(Ticket instanceof TicketViaEJ), JposConst.JPOS_E_NOHARDWARE, "No electronic journal present");
            super.claim(timeout);
        }

        @Override
        public void deviceEnabled(boolean enable) throws JposException {
            super.deviceEnabled(enable);
            if (enable) {
                TicketViaEJ ej = (TicketViaEJ) Ticket;
                synchronized(ej) {
                    try {
                        long length = ej.validateFile(Index) * CURRENCYFACTOR;
                        MediumFreeSpace = length >= MediumSize ? 0 : MediumSize - length;
                        JournalState[Index] = 0;
                        if (MediumFreeSpace == 0) {
                            JournalState[Index] = ElectronicJournalConst.EJ_SUE_MEDIUM_FULL;
                        } else if (MediumFreeSpace <= JournalLowSize * JournalWidth * JRN_MAX_LINE_COUNT * CURRENCYFACTOR) {
                            JournalState[Index] = ElectronicJournalConst.EJ_SUE_MEDIUM_NEAR_FULL;
                        }
                        if (JournalState[Index] != 0) {
                            handleEvent(new ElectronicJournalStatusUpdateEvent(EventSource, JournalState[Index]));
                        }
                    } catch (IOException e) {
                        throw new JposException(JposConst.JPOS_E_FAILURE, "Journal fault: " + e.getMessage(), e);
                    } finally {
                        ej.closefile(Index);
                    }
                }
            }
        }

        @Override
        public void eraseMedium(EraseMedium request) throws JposException {
            TicketViaEJ ej = (TicketViaEJ) Ticket;
            synchronized(ej) {
                try {
                    ej.validateFile(Index);
                    ej.DataFile[Index].setLength(ej.HEADSIZE);
                    MediumFreeSpace = MediumSize - ej.HEADSIZE * CURRENCYFACTOR;
                    JournalState[Index] = 0;
                    ej.eraseLastMarkers(Index);
                } catch (IOException e) {
                    throw new JposException(JposConst.JPOS_E_FAILURE, "Journal access error: " + e.getMessage(), e);
                } finally {
                    ej.closefile(Index);
                }
            }
        }

        @Override
        public void queryContent(QueryContent request) throws JposException {
            TicketViaEJ ej = (TicketViaEJ) Ticket;
            ej.getTickets(Index, request.getFromMarker(), request.getToMarker(), request.getFileName());
            handleEvent(new JposDataEvent(EventSource, 0));
        }

        @Override
        public void retrieveCurrentMarker(int type, String[] marker) throws JposException {
            TicketViaEJ ej = (TicketViaEJ) Ticket;
            String mark = "";
            check((mark = ej.retrieveMarker(Index, type)).equals(""), JposConst.JPOS_E_NOEXIST, "Marker not found");
            marker[0] = mark;
        }
        @Override
        public void retrieveMarkerByDateTime(int type, String date, String count, String[] marker) throws JposException {
            TicketViaEJ ej = (TicketViaEJ) Ticket;
            String mark;
            check(type != ElectronicJournalConst.EJ_MT_DOCUMENT, JposConst.JPOS_E_NOEXIST, "Unsupported marker type: " + type);
            check((mark = ej.retrieveMarker(Index, Integer.parseInt(count), date, new long[1])).equals(""), JposConst.JPOS_E_NOEXIST, "Marker not found");
            marker[0] = mark;
        }
    }
}
