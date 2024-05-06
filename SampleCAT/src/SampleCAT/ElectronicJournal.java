/*
 * Copyright 2018 Martin Conrad
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
import de.gmxhome.conrad.jpos.jpos_base.electronicjournal.*;
import jpos.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import static SampleCAT.Device.*;
import static SampleCAT.Device.TicketOutput.*;
import static jpos.ElectronicJournalConst.*;
import static jpos.JposConst.*;

/**
 * Sample device specific accessor class, bound to the credit authorization terminal service.
 * This implementation provides up to two electronic journal devices, depending on the CAT device
 * configuration:
 * <ul>
 *     <li>If a path prefix has been specified in jpos.xml (entry <b>JournalPath</b>), ElectronicJournal
 * services are available. They use the device indices 0 (for customer tickets) and 1 (for merchant tickets).</li>
 *     <li>Otherwise, ticket data will be passed to the application via DirectIOEvents, fired by the CAT service.</li>
 * </ul>
 * If available, the ElectronicJournal devices work as follows:
 * <ul>
 *     <li>The ElectronicJournal will be written automatically whenever ticket data are received from the terminal.</li>
 *     <li>The transaction time stamp will be used as markers for the corresponding tickets.</li>
 *     <li>All ticket data will be written to electronic journal wih index 0.</li>
 *     <li>All ticket data that shall be printed twice will be written to electronic journal with index 1 as well.</li>
 *     <li>The application can retrieve ticket data with the QueryContent method. Print methods are not available
 *         because the sample device has no printer.</li>
 *     <li>To be able to use QueryContent, methods RetrieveCurrentMarker and RetrieveMarkerByDateTime can be used. In
 *         addition, the contents of the CAT property SlipNumber can be used as marker.</li>
 *     <li>Each electronic journal can be cleared with the EraseMedium method. This method must be called whenever
 *         property MediumFreeSpace is 0.</li>
 * </ul>
 */
public class ElectronicJournal extends ElectronicJournalProperties {
    static private class TicketViaDirectIO extends Device.TicketOutput {
        private final Device Dev;
        private TicketViaDirectIO(Device dev) {
            super();
            Dev = dev;
        }
        @Override
        void setTicket(int count, String contents) {
            super.setTicket(count, contents);
            try {
                Dev.handleEvent(new JposDirectIOEvent(Dev.getClaimingInstance(Dev.ClaimedCAT, 0).EventSource, CAT_CMD_TICKET, count == 1 ? CAT_DATA_CUSTOMER : CAT_DATA_BOTH, contents));
            } catch (JposException e) {
                e.printStackTrace();
            }
        }
    }

    static private class TicketViaEJ extends Device.TicketOutput {
        private final Device Dev;
        private static final int MARKSIZE = 14; // YYYYmmddHHMMSS
        private int FRAMESIZE = 0;
        private String Contents;
        private int Count;
        private long MediumSize;
        private final RandomAccessFile[] DataFile = {null, null};
        private final String[][] LastMarkers = {{null, null}, {null, null}};
        private final long[][] LastPos = {{0, 0}, {0, 0}};
        private TicketViaEJ(Device dev) {
            super();
            Dev = dev;
        }

        @Override
        void prepare() throws JposException {
            FRAMESIZE = JRN_MAX_LINE_COUNT * Dev.JournalWidth;
            MediumSize = (Dev.JournalMaxSize * Dev.JournalWidth * JRN_MAX_LINE_COUNT + HEADSIZE) * CURRENCYFACTOR;
        }

        @Override
        synchronized void init() throws JposException {
            try {
                check(MediumSize <= validateFile(0) || MediumSize <= validateFile(1), JPOS_E_FAILURE, "Insufficient space on journal medium");
            } catch (IOException e) {
                throw new JposException(JPOS_E_FAILURE, "Journal file access error: " + e.getMessage(), e);
            }
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
                    byte[] frame = Arrays.copyOf(TransactionDate.getBytes(StandardCharsets.UTF_8), FRAMESIZE);
                    byte[] ticket = Contents.getBytes(StandardCharsets.UTF_8);
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
            ElectronicJournalProperties props = (ElectronicJournalProperties)Dev.getClaimingInstance(Dev.ClaimedElectronicJournal, index);
            if (props != null && props.DeviceEnabled) {
                int oldstate = Dev.JournalState[index];
                if ((length += frame.length) * CURRENCYFACTOR >= props.MediumSize) {
                    props.MediumFreeSpace = 0;
                    Dev.JournalState[index] = EJ_SUE_MEDIUM_FULL;
                } else {
                    props.MediumFreeSpace = props.MediumSize - length * CURRENCYFACTOR;
                    if (props.MediumFreeSpace <= FRAMESIZE * CURRENCYFACTOR * Dev.JournalLowSize) {
                        Dev.JournalState[index] = EJ_SUE_MEDIUM_NEAR_FULL;
                    }
                }
                if (oldstate != Dev.JournalState[index]) {
                    try {
                        Dev.handleEvent(new ElectronicJournalStatusUpdateEvent(props.EventSource, Dev.JournalState[index]));
                    } catch (JposException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @SuppressWarnings("resource")
        private long validateFile(int index) throws IOException {
            final String[] namesuffix = {".customer.tickets", ".merchant.tickets"};
            try {
                DataFile[index] = new RandomAccessFile(Dev.JournalPath + namesuffix[index], "rwd");
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
        private synchronized String retrieveMarker(int index, int type) {
            try {
                byte[] mark = new byte[MARKSIZE];
                long length = validateFile(index);
                if (length > HEADSIZE) {
                    long pos = 0;
                    switch (type) {
                        default -> {
                            return "";
                        }
                        case EJ_MT_DOCUMENT, EJ_MT_TAIL -> DataFile[index].seek(pos = length - FRAMESIZE);
                        case EJ_MT_HEAD -> DataFile[index].seek(pos = HEADSIZE);
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

        private synchronized String retrieveMarker(int index, int count, String date, long[] pos) {
            String retval = "";
            try {
                long dateval = Long.parseLong(date);
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
                    if (Long.parseLong(new String(buffer, 0, date.length())) == dateval) {
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
                return Long.parseLong(new String(buffer));
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
        private synchronized void getTickets(int index, String from, String to, String filename) throws JposException {
            if (new File(filename).exists())
                throw new JposException(JPOS_E_EXISTS, "File exists: " + filename);
            RandomAccessFile target = null;
            try {
                byte[] frame = new byte[FRAMESIZE];
                long[] pos = {HEADSIZE};
                if (!from.equals("")) {
                    if ((pos[0] = getLastMarkerPosition(index, from)) == 0 && retrieveMarker(index, 1, from, pos).equals(""))
                        throw new JposException(JPOS_E_ILLEGAL, "Invalid from marker");
                }
                long[] filereadend = {validateFile(index)};
                if (!to.equals("")) {
                    filereadend[0] = getLastMarkerPosition(index, to);
                    if (filereadend[0] == 0 && retrieveMarker(index, 1, to, filereadend).equals(""))
                        throw new JposException(JPOS_E_ILLEGAL, "Invalid to marker");
                    filereadend[0] += FRAMESIZE;
                }
                if (pos[0] >= filereadend[0])
                    throw new JposException(JPOS_E_ILLEGAL, "To marker before from marker");
                DataFile[index].seek(pos[0]);
                for (long currentpos = pos[0]; currentpos < filereadend[0]; currentpos += frame.length) {
                    check(DataFile[index].read(frame) < frame.length, JPOS_E_FAILURE, "Journal corrupted");
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
                                    throw new JposException(JPOS_E_FAILURE, "Data file error: " + e.getMessage(), e);
                                }
                            }
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                throw new JposException(JPOS_E_FAILURE, "Journal file error: " + e.getMessage(), e);
            } catch (NumberFormatException e) {
                throw new JposException(JPOS_E_NOEXIST, "Invalid end marker", e);
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

    private final Device Dev;

    /**
     * Constructor.
     *
     * @param index Property set used by this accessor.
     * @param dev   Device communication handler object.
     */
    public ElectronicJournal(int index, Device dev) {
        super(index);
        Dev = dev;
    }

    /**
     * Class factory for TicketOutput objects that fit the configuration. Relevant configuration detail is the
     * Jpos.xml entry JournalPath: If not set or empty, an instance of class TicketViaDirectIO will be returned,
     * otherwise an instance of class TicketViaEJ.
     *
     * @param dev Device implementation instance to be initialized.
     * @return    TicketOutput object to be used during CAT transactions.
     */
    static Device.TicketOutput getTicketOutput(Device dev) {
        return dev.JournalPath.equals("") ? new TicketViaDirectIO(dev) : new TicketViaEJ(dev);
    }

    @Override
    public void checkHealth(int level) throws JposException {
        CheckHealthText = "Internal CheckHealth: OK";
        EventSource.logSet("CheckHealthText");
    }

    @Override
    public void claim(int timeout) throws JposException {
        check(Dev.Ticket == null || !(Dev.Ticket instanceof TicketViaEJ), JPOS_E_NOHARDWARE, "No electronic journal present");
        super.claim(timeout);
    }

    @Override
    @SuppressWarnings("SynchronizeOnNonFinalField")
    public void deviceEnabled(boolean enable) throws JposException {
        super.deviceEnabled(enable);
        if (enable) {
            final TicketViaEJ ej = (TicketViaEJ) Dev.Ticket;
            synchronized(Dev.Ticket) {
                try {
                    long length = ej.validateFile(Index) * CURRENCYFACTOR;
                    MediumFreeSpace = length >= MediumSize ? 0 : MediumSize - length;
                    Dev.JournalState[Index] = 0;
                    if (MediumFreeSpace == 0) {
                        Dev.JournalState[Index] = EJ_SUE_MEDIUM_FULL;
                    } else if (MediumFreeSpace <= Dev.JournalLowSize * Dev.JournalWidth * JRN_MAX_LINE_COUNT * CURRENCYFACTOR) {
                        Dev.JournalState[Index] = EJ_SUE_MEDIUM_NEAR_FULL;
                    }
                    if (Dev.JournalState[Index] != 0) {
                        Dev.handleEvent(new ElectronicJournalStatusUpdateEvent(EventSource, Dev.JournalState[Index]));
                    }
                } catch (IOException e) {
                    throw new JposException(JPOS_E_FAILURE, "Journal fault: " + e.getMessage(), e);
                } finally {
                    ej.closefile(Index);
                }
            }
        }
    }

    @Override
    public void station(int station) throws JposException {
        check(station != EJ_S_RECEIPT, JPOS_E_ILLEGAL, "Journal deactivation not allowed");
    }

    @Override
    public void handlePowerStateOnEnable() throws JposException {
        PowerState = JPOS_PS_ONLINE;
        super.handlePowerStateOnEnable();
    }

    @Override
    @SuppressWarnings("SynchronizeOnNonFinalField")
    public void eraseMedium(EraseMedium request) throws JposException {
        TicketViaEJ ej = (TicketViaEJ) Dev.Ticket;
        synchronized(Dev.Ticket) {
            try {
                ej.validateFile(Index);
                ej.DataFile[Index].setLength(HEADSIZE);
                MediumFreeSpace = MediumSize - HEADSIZE * CURRENCYFACTOR;
                Dev.JournalState[Index] = 0;
                ej.eraseLastMarkers(Index);
            } catch (IOException e) {
                throw new JposException(JPOS_E_FAILURE, "Journal access error: " + e.getMessage(), e);
            } finally {
                ej.closefile(Index);
            }
        }
    }

    @Override
    public void queryContent(QueryContent request) throws JposException {
        TicketViaEJ ej = (TicketViaEJ) Dev.Ticket;
        ej.getTickets(Index, request.getFromMarker(), request.getToMarker(), request.getFileName());
        Dev.handleEvent(new JposDataEvent(EventSource, 0));
    }

    @Override
    public void retrieveCurrentMarker(int type, String[] marker) throws JposException {
        TicketViaEJ ej = (TicketViaEJ) Dev.Ticket;
        String mark = "";
        check((mark = ej.retrieveMarker(Index, type)).equals(""), JPOS_E_NOEXIST, "Marker not found");
        marker[0] = mark;
    }

    @Override
    public void retrieveMarkerByDateTime(int type, String date, String count, String[] marker) throws JposException {
        TicketViaEJ ej = (TicketViaEJ) Dev.Ticket;
        String mark;
        check(type != EJ_MT_DOCUMENT, JPOS_E_NOEXIST, "Unsupported marker type: " + type);
        check((mark = ej.retrieveMarker(Index, Integer.parseInt(count), date, new long[1])).equals(""), JPOS_E_NOEXIST, "Marker not found");
        marker[0] = mark;
    }

    @Override
    public void retrieveMarkersDateTime(String marker, String[] date) throws JposException {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        format.setLenient(false);
        check(format.parse(marker, new ParsePosition(0)) == null, JPOS_E_ILLEGAL, "Bad marker format");
        check((((TicketViaEJ)Dev.Ticket).retrieveMarker(Index, Integer.parseInt("1"), marker, new long[1])).equals(""), JPOS_E_NOEXIST, "Marker not found");
        date[0] = marker;
    }
}
