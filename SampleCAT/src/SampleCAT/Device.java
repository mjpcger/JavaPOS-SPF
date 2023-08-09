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
import jpos.events.*;
import net.bplaced.conrad.log4jpos.Level;

import javax.swing.*;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Base of a JposDevice based implementation of JavaPOS CAT and ElectronicJournal device service implementations for the
 * sample device implemented in SampleCAT.tcl. The sample device simulates EFT payment transactions without ticket
 * printing (the simulator sends ticket and display data to its target).<br>
 * Supported features are:
 * <ul>
 *     <li>Sale, refund and void.</li>
 *     <li>Start transaction before amount to approve is known.</li>
 *     <li>Confirmation in case of signature based approval.</li>
 *     <li>Lock / unlock terminal.</li>
 *     <li>Card issuer, transaction number, transaction date, tip, approved amount, approval result, terminal result,
 *         masked card number (last 4 digit readable), expiration date sent in case of successful operation.</li>
 *     <li>Display output will be sent to LineDisplay or to the application via DirectIOEvent during CAT operations.</li>
 *     <li>Ticket layout will be send to a POS printer, to the application via DirectIOEvent or passed to
 *         ElectronicJournal devices.</li>
 * </ul>
 * DisplayName is a mandatory property in Jpos.xml. If not specified or not empty, it must specify the name of a
 * LineDisplay device that shall be used to echo the contents of the terminal display. It will be working best if it
 * has 4 x 40 characters or more, a minimum of 2 x 20 is recommended.<br>
 * Minimum requirements: It must support code mapping or ASCII, ANSI or Unicode encoding.<br>
 * The advantages of processing display data via DirectIOEvent:
 * <ul>
 *     <li>The application has full control over display contents.</li>
 *     <li>No hardware limits for number of display lines and columns (sample CAT uses 4 lines with 40 characters).</li>
 *     <li>No limitations for test formatting, character sets and usable fonts.</li>
 *     <li>Neither need to release the cash register's LineDisplay before CAT operations nor need to re-claim and
 *         re-enable the LineDisplay after CAT operation has been finished.</li>
 * </ul>
 * The advantages of sending display data to LineDisplay device:
 * <ul>
 *     <li>No need for vendor-specific code to process display data.</li>
 *     <li>No need to change the application if it is designed to share its line display during CAT operations:
 *         <ul>
 *             <li>If the application releases the LineDisplay before CAT operations start.</li>
 *             <li>If the application re-claims and re-enables LineDisplay after CAT operations finish.</li>
 *         </ul></li>
 * </ul>
 * JournalPath is a mandatory property in Jpos.xml. It can specify the name of a POSPrinter device that
 * shall be used to print ticket data, a prefix for customer and merchant journals or it can be empty.<br>
 * If JournalPath specifies a POSPrinter device name, the printer will be claimed and enabled at CAT operation start.
 * If the printer reports an error (not online, paper near end, cover open), the CAT operation will not be started. If
 * printing ticket data fails due to head cleaning, printing will be repeated automatically, otherwise the sample
 * waits for pressing the OK button on a message box (OK, that's a bad solution, but this is only a sample). After
 * printing finished, the POSPrinter will be released.<br>
 * Minimum requirements to the POSPrinter device:
 * <ul>
 *     <li>Power Reporting,</li>
 *     <li>Receipt print station,</li>
 *     <li>Receipt near end sensor,</li>
 *     <li>Maximum receipt width &ge; JournalWidth (Jpos.xml) property. Default for JournalWidth is 32.</li>
 * </ul>
 * If JournalPath specifies a path prefix (the service appends either ".merchant.tickets" or ".customer.tickets"),
 * ticket data will be written to one or two of two virtual ElectronicJournal devices: The device with Jpos.xml property
 * DevIndex = 0 for customer tickets, the device with DevIndex = 1 for merchant tickets. After each sales transaction,
 * the application cat query the full layouted tickets for customer and (if present) for merchant from the corresponding
 * ElectronicJournal devices as described in the UPOS manual, merge them into the sales receipt or print them separately
 * and optionally clear the journals after processing, once a day of whenever nearly full.
 * If JournalPath is empty, ticket data will be passed to the application via DirectIOEvent. Even if the way the
 * application gets the tickets for customer and merchant is different, processing ticket data can be similar to the
 * ElectronicJournal scenario.<br>
 * Ticket data of the sample CAT do not contain any control characters except newline (0ah).<br>
 * The advantages of processing ticket data via DirectIOEvent:
 * <ul>
 *     <li>Printing ticket data is completely under control of the application, including error handling and error
 *         recovery.</li>
 *     <li>The application knows the ticket data and can print copies if requested.</li>
 *     <li>Ticket data come directly from the service to the application, no need to query data from ElectronicJournal
 *         devices.</li>
 *     <li>No need to release the POSPrinter before each CAT operation and to re-claim and re-enable after each
 *         CAT operation has been finished.</li>
 * </ul>
 * The advantages of processing data via ElectronicJournal devices:
 * <ul>
 *     <li>Printing ticket data is completely under control of the application, including error handling and error
 *         recovery.</li>
 *     <li>The application knows the ticket data and can print copies if requested.</li>
 *     <li>The ElectronicJournal interface is a well-defined standard, no vendor specific implementation needed (if
 *         usage of ElectronicJournal devices has been implemented for such usage cases).</li>
 *     <li>No need to release the POSPrinter before each CAT operation and to re-claim and re-enable after each
 *         CAT operation has been finished.</li>
 * </ul>
 * The advantages of sending ticket data directly to POSPrinter:
 * <ul>
 *     <li>No need for vendor-specific code to process ticket data.</li>
 *     <li>No need to change the application if it is designed to share its pos printer during CAT operations:
 *         <ul>
 *             <li>If the application releases the POSPrinter before CAT operations start.</li>
 *             <li>If the application re-claims and re-enables POSPrinter after CAT operations finish.</li>
 *         </ul>
 *         This implies that the application uses only subsequent receipt printing because the sales receipt cannot
 *         be finished before payment operations have been finished (CAT operations are payment operations) and ticket
 *         printing would destroy the sales receipt otherwise.
 *         receipt.</li>
 * </ul><br>
 * Here a full list of all device specific properties that can be changed via jpos.xml:
 * <ul>
 *     <li>CharacterTimeout: Positive integer value, specifying the maximum delay between bytes that belong to the same
 *     frame. Default value: 50 milliseconds.</li>
 *     <li>ClientPort: Integer value between 0 and 65535 specifying the TCP port used for communication with the device
 *     simulator. Default: 0 (for random port number selected by operating system).</li>
 *     <li>DisplayLines: Number of display lines the application or the attached JavaPOS display can support. Must be
 *     2, 3 or 4. Default: 4</li>
 *     <li>DisplayName: See description above: Must be a LineDisplay device name or empty. Default: empty.</li>
 *     <li>DisplayWidth: Maximum  length of a single display line the application or the attached LineDisplay can
 *     support. Must be an integer value between 20 and 40 (both inclusive). Default: 40.</li>
 *     <li>JournalLowSize: Maximum free space (in tickets) to report nearly full. Must be a positive value between
 *     0 and the maximum size of the journal, in tickets. Default: 10.</li>
 *     <li>JournalMaxSize: Maximum size of of electronic journal (in tickets). Must be a positive value. Default: 1000.</li>
 *     <li>JournalPath: See description above: Path of electronic journal files or empty to force usage of DirectIoEvent
 *     objects to pass CAT tickets to the application. Default: empty (to force DirectIOEvent usage).</li>
 *     <li>JournalWidth: Length of one ticket line stored in the journal or sent vie DirectIOEvent. Must be a value
 *     between 28 and 99 (inclusive). Default: 32.</li>
 *     <li>MinClaimTimeout: Minimum timeout in milliseconds used by method Claim to ensure correct working. Must be a
 *     positive value. If this value is too small, Claim might throw a JposException even if everything is OK if the
 *     specified timeout is less than or equal to MinClaimTimeout. Default: 100.</li>
 *     <li>Port: The IPv4 address of the device. Must always be specified and not empty. Notation: address:port, where
 *     address is a IPv4 address and port the TCP port of the device.</li>
 *     <li>RequestTimeout: Maximum time the service object waits for the reception of a response frame after sending a
 *     request to the target, in milliseconds. Default: 1000.</li>
 * </ul>
 */
public class Device extends JposDevice implements Runnable{
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
     * Data value of DirectIOEvent with EventNumber CAT_CMD_TICKET for merchant and customer tickets. Such a ticket should
     * be printed twice, one to be handed over to the customer and one to be kept by the merchant, in most cases with
     * customer signature.
     */
    static public final int CAT_DATA_BOTH = 3;      // ticket data for merchant and customer. No control characters except LF.

    /**
     * Maximum time between request and response.
     */
    int RequestTimeout = 1000;

    /**
     * Minimum claim timeout. To be used whenever timeout in claim() method is too small to
     * guarantee correct work of service, even if everything is OK.
     */
    int MinClaimTimeout = 100;

    /**
     * Length of print line.
     */
    int JournalWidth = 32;

    /**
     * Path of electronic journal files. If empty use DirectIOEvent instead.
     */
    String JournalPath = "";

    /**
     * Maximum free space (in tickets) to report nearly full.
     */
    long JournalLowSize = 10;

    /**
     * Status watcher instance.
     */
    Thread StateWatcher = null;

    /**
     * Flag showing that status watcher shall be stopped.
     */
    boolean ToBeFinished;

    /**
     * Flag showing the current communication state.
     */
    boolean InIOError = false;

    /**
     * Display output object. Used to process display output data from terminal.
     */
    DisplayOutput Display = null;

    /**
     * Ticket output object. Used to process ticket data provided by the terminal.
     */
    TicketOutput  Ticket = null;

    /**
     * Status of customer and merchant journal. May contain the following values:
     * <ul>
     *     <li>0: There is enough free space available for the journal.</li>
     *     <li>SUE_MEDIUM_NEAR_FULL: There is still enough free space available for the journal, but free space is low.</li>
     *     <li>SUE_MEDIUM_FULL: There is no free space available for the journal. Some space must be freed.</li>
     * </ul>
     */
    int[] JournalState = new int[]{0, 0};

    /**
     * Maximum size of of electronic journal (in tickets).
     */
    long JournalMaxSize = 1000;

    /**
     * Maximum number of lines per ticket used by sample terminal. If the device generates more lines, this constant
     * must be adjusted.
     */
    static final int JRN_MAX_LINE_COUNT = 20;

    /**
     * Long representation of a CURRENCY value of 1. Since a CURRENCY is specified as a long with implicit 4 decimals,
     * this value is always 10000.
     */
    static final long CURRENCYFACTOR = 10000;

    static private final int MinJournalWidth = 28;      // Minimum print line length.
    static private final int MinDisplayWidth = 20;      // Minimum display line length.
    static private final int MinDisplayLines = 2;       // Minimum display line length.
    static private final int MaxJournalWidth = 99;      // Maximum print line length.
    static private final int MaxDisplayWidth = 40;      // Maximum display line length.
    static private final int MaxDisplayLines = 4;       // Maximum display line length.

    private String JournalConversion = "";  // Default conversion for ticket data on POSPrinter (Unicode, ansi or ascii).
    private String DisplayConversion = "";  // Default conversion for display data on LineDisplay (Unicode, ansi or ascii).
    private int DisplayWidth = 40;          // Length of display line.
    private int DisplayLines = 4;           // Number of display lines.
    private String DisplayName = "";        // Display name, if empty, use DirectIOEvents instead
    private int ClientPort = 0;                // Default: OS generated random port
    private int CharacterTimeout = 50;      // Default: Service specific value for maximum delay between bytes belonging
                                            // to the same frame
    /**
     * Communication object.
     */
    TcpClientIOProcessor OutStream = null;

    private SyncObject WaitInitialized;

    /**
     * Control characters for form feed
     */
    static final byte FF = 12;

    // Further control characters used by the terminal simulator

    static private final byte STX = 2;
    static private final byte ETX = 3;
    static private final byte LF = 10;

    /**
     * Class for processing terminal display information.
     */
    static class DisplayOutput {
        private String[] Line = new String[]{"", "", "", ""};
        private boolean[] PutOut = new boolean[]{true, true, true, true};

        /**
         * Specifies whether the display output object is currently in use. This is the case between ini() and release().
         */
        boolean Active = false;

        /**
         * The constructor. No further action.
         */
        DisplayOutput() {}

        /**
         * Initializes the display output processor for output operation. Updates display contents, if changed since
         * last release.
         * @throws JposException If initialization fails or display output device is not in an operable status.
         */
        void init() throws JposException {
            Active = true;
            for (int i = 0; i < Line.length; i++) {
                if (PutOut[i])
                    setLine(i, Line[i]);
            }
        }

        /**
         * Releases the display. Further display output will be stored internally.
         */
        void release() {
            Active = false;
        }

        /**
         * Fills contents into the line specified by lineno. lineno must be between 0 and 3.
         * @param lineno    Line number to be set.
         * @param contents  New contents of the line.
         */
        void setLine(int lineno, String contents) {
            Line[lineno] = contents;
        }

        /**
         * Frees all resources when the object will no longer be used.
         */
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

    /**
     * Class to process ticket data provided by the terminal to be printed on an attached printer.
     */
    static class TicketOutput {
        /**
         * Additional size per ticket for journal media, if ElectronicJournal is used for ticket output. This is the
         * difference between the size per ticket and the product of (maximum lines per ticket) * (characters per line).
         */
        static final int HEADSIZE = 10;

        /**
         * Date of CAT transaction corresponding to the ticket in format YYYYmmddHHMMSS. Must be set before releasing
         * the object.
         */
        String TransactionDate = null;

        /**
         * The constructor. No further operation.
         */
        TicketOutput() {}

        /**
         * Prepares object for later use. Called immediately after the first call of checkProperties.
         * @throws JposException If object would not work correctly with the configuration in jpos.xml.
         */
        void prepare() throws JposException {}
        /**
         * Initializes the object for ticket output operation.
         * @throws JposException If ticket output would not work.
         */
        void init() throws JposException {}

        /**
         * Locks object for further ticket output operation.
         */
        void release() {}

        /**
         * Frees all resources hold by the object.
         */
        void cleanup() {}

        /**
         * Processes ticket data. Currently, three methods of processing ticket data are available:
         * <ul>
         *     <li>Ticket data will be passed to the application via firing a DirectIOEvent object.</li>
         *     <li>Ticket data will be written to customer or merchant journal.</li>
         *     <li>Ticket data will be printed by the pos printer.</li>
         * </ul>
         *  After the current CAT operation has been finished, the application should merge the tickets with the sales
         *  receipt transaction and (optionally) print tickets that fulfill the needs of customer and merchant.
         *  <br>This method must not be called more than once per transaction. If called more than once, the contents
         *  of previous calls might be overwritten. This is no restriction because the sample implementation sends
         *  no more than one ticket per CAT transaction.
         *
         * @param count     Number of tickets (1 or 2). If 1, only one ticket must be printed. If
         *                  two, merchant and customer ticket must be printed.
         * @param contents  Contents of ticket. May contain printable characters and new line characters.
         */
        void setTicket(int count, String contents) {}
    }

    // Implementation for POSPrinter based operation.

    private class TicketViaPrt extends Device.TicketOutput implements StatusUpdateListener {
        private POSPrinter Printer;
        private String Conversion = new String("");
        private String Contents;
        private int Count;
        private boolean HasCartridgeSensor;
        private SyncObject CleaningEndWaiter = null;
        TicketViaPrt(Device device) throws JposException {
            super();
            Printer = new POSPrinter();
            Printer.addStatusUpdateListener(this);
            try {
                Printer.open(JournalPath);
            } catch (JposException e) {
                Printer = null;
                throw e;
            }
        }

        @Override
        void prepare() throws JposException {
            boolean fitsMinimumRequirements = Printer.getCapPowerReporting() != JposConst.JPOS_PR_NONE &&
                    (Printer.getCapRecColor() & POSPrinterConst.PTR_COLOR_PRIMARY) != 0 &&
                    Printer.getCapRecPresent() &&
                    Printer.getCapRecPapercut() &&
                    Printer.getCapRecNearEndSensor();
            check(!fitsMinimumRequirements, JposConst.JPOS_E_ILLEGAL, "Printer " + JournalPath + " does not fit minimum requirements");
            HasCartridgeSensor = (Printer.getCapRecCartridgeSensor() & POSPrinterConst.PTR_CART_CLEANING) != 0;
            long[] allowedLineChars = stringArrayToLongArray(Printer.getRecLineCharsList().split(","));
            for (long chars : allowedLineChars) {
                if (chars >= JournalWidth)
                    return;
            }
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Configured Print line (JournalWidth) not supported by printer");
        }

        @Override
        void init() throws JposException {
            if(Printer != null) {
                try {
                    Printer.claim(100);
                    if (HasCartridgeSensor)
                        Printer.setCartridgeNotify(POSPrinterConst.PTR_CN_ENABLED);
                    Printer.setPowerNotify(JposConst.JPOS_PN_ENABLED);
                    Printer.setDeviceEnabled(true);
                    if (HasCartridgeSensor)
                        Printer.setRecCurrentCartridge(POSPrinterConst.PTR_COLOR_PRIMARY);
                    if (Printer.getCapMapCharacterSet()) {
                        Printer.setMapCharacterSet(true);
                    }
                    else if (member(String.valueOf(POSPrinterConst.PTR_CS_UNICODE), Printer.getCharacterSetList().split(","))) {
                        Printer.setCharacterSet(POSPrinterConst.PTR_CS_UNICODE);
                    }
                    else if (!Printer.getMapCharacterSet()) {
                        if (member(String.valueOf(POSPrinterConst.PTR_CS_ANSI), Printer.getCharacterSetList().split(","))) {
                            Printer.setCharacterSet(POSPrinterConst.PTR_CS_ANSI);
                            Conversion = ANSI;
                        }
                        else {
                            Printer.setCharacterSet(POSPrinterConst.PTR_CS_ASCII);
                            Conversion = ASCII;
                        }
                    }
                    Printer.setRecLineChars(JournalWidth);
                    check(Printer.getRecNearEnd(), JposConst.JPOS_E_FAILURE, "Paper near end");
                    check(Printer.getCapCoverSensor() &&Printer.getCoverOpen(), JposConst.JPOS_E_FAILURE, "Cover open");
                    check(Printer.getPowerState() != JposConst.JPOS_SUE_POWER_ONLINE, JposConst.JPOS_E_FAILURE, "Printer off or offline");
                } catch (JposException e) {
                    throw new JposException(JposConst.JPOS_E_FAILURE, "Printer access error: " + e.getMessage(), e);
                }
            }
            super.init();
        }

        @Override
        void release() {
            if (Printer != null) {
                try {
                    if (Printer.getClaimed())
                        Printer.release();
                } catch (JposException e) {
                    e.printStackTrace();
                }
            }
            super.release();
        }

        @Override
        void cleanup() {
            if (Printer != null) {
                try {
                    Printer.close();
                } catch (JposException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        void setTicket(int count, String contents) {
            try {
                Count = count;
                Contents = contents;
                if (Conversion.length() > 0) {
                    try {
                        byte[] source = Contents.getBytes(Conversion);
                        char[] target = new char[source.length];
                        for (int i = 0; i < source.length; i++) {
                            target[i] = (char) source[i];
                        }
                        Contents = new String(target);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                Contents = Contents + "\33|fP";
                while(true) {
                    SyncObject waiter = new SyncObject();
                    synchronized (this) {
                        (CleaningEndWaiter = waiter).suspend(0);
                    }
                    try {
                        Printer.printNormal(POSPrinterConst.PTR_S_RECEIPT, "\33|N" + (count == 1 ? Contents : Contents + Contents));
                        break;
                    } catch (JposException e) {
                        if (e.getErrorCode() == JposConst.JPOS_E_EXTENDED && e.getErrorCodeExtended() == POSPrinterConst.JPOS_EPTR_REC_HEAD_CLEANING)
                            waiter.suspend(SyncObject.INFINITE);
                        else {
                            synchronized (this) {
                                CleaningEndWaiter = null;
                            }
                            String message = "Check Printer";
                            if (e.getErrorCode() != JposConst.JPOS_E_EXTENDED) {
                                switch (e.getErrorCodeExtended()) {
                                    case POSPrinterConst.JPOS_EPTR_COVER_OPEN:
                                        message = "Close Printer Cover";
                                        break;
                                    case POSPrinterConst.JPOS_EPTR_REC_CARTRIDGE_EMPTY:
                                        message = "Change Color Cartridge";
                                        break;
                                    case POSPrinterConst.JPOS_EPTR_REC_CARTRIDGE_REMOVED:
                                        message = "Insert Color Cartridge";
                                        break;
                                    case POSPrinterConst.JPOS_EPTR_REC_EMPTY:
                                        message = "Insert Paper";
                                        break;
                                }
                            }
                            synchronizedMessageBox(message, "Printer Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void statusUpdateOccurred(StatusUpdateEvent statusUpdateEvent) {
            switch (statusUpdateEvent.getStatus()) {
                case POSPrinterConst.PTR_SUE_REC_CARTDRIGE_OK:
                case POSPrinterConst.PTR_SUE_REC_CARTRIDGE_EMPTY:
                case POSPrinterConst.PTR_SUE_REC_CARTRIDGE_NEAREMPTY:
                    synchronized (this) {
                        if (CleaningEndWaiter != null) {
                            CleaningEndWaiter.signal();
                            CleaningEndWaiter = null;
                        }
                    }
            }
        }
    }

    /**
     * String used for conversions from and to UTF-8 character set.
     */
    static final String UTF8 = "UTF-8";

    static private final String ANSI = "windows-1252";    // For conversion to ANSI (Windows code page 1252) character set.
    static private final String ASCII = "US-ASCII";     // for connversion to ASCII character set.

    // Implementation for LineDisplay based operation.

    private class DisplayViaLD extends DisplayOutput {
        private LineDisplay Display;
        private String Conversion = new String("");
        DisplayViaLD() throws JposException {
            super();
            Display = new LineDisplay();
            try {
                Display.open(DisplayName);
            } catch (JposException e) {
                Display = null;
                throw e;
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

    /**
     * Constructor. ID is the network address of the credit authorisation terminal (CAT).
     * displayName specifies how display information of the CAT will be performed. journalPath specifies how ticket
     * data provided by the CAT will be handled.
     *
     * @param id    Network address of the display controller.
     * @param displayName Value of Jpos.xml property DisplayName (default: null).
     * @param journalPath Value of Jpos.xml property JournalPath (default: null).
     */
    protected Device(String id, Object displayName, Object journalPath) {
        super(id);
        DisplayName = displayName == null ? "" : displayName.toString();
        JournalPath = journalPath == null ? "" : journalPath.toString();
        try {
            Display = DisplayName.equals("") ? new DisplayViaDirectIO() : new DisplayViaLD();
        } catch (JposException e) {
            Display = null;
        }
        try {
            Ticket = new TicketViaPrt(this);
        } catch (Exception e) {
            Ticket = ElectronicJournal.getTicketOutput(this);
        }
        cATInit(1);
        electronicJournalInit(JournalPath.length() == 0 || Ticket instanceof TicketViaPrt ? 0 : 2);
        PhysicalDeviceDescription = "CAT simulator for TCP";
        PhysicalDeviceName = "CAT Simulator";
        CapPowerReporting = JposConst.JPOS_PR_STANDARD;
    }

    @Override
    public void checkProperties(JposEntry entry) throws JposException {
        check(Display == null, JposConst.JPOS_E_ILLEGAL, "Invalid DisplayName property: " + DisplayName);
        check(Ticket == null, JposConst.JPOS_E_ILLEGAL, "Invalid JournalPath property: " + JournalPath);
        super.checkProperties(entry);
        try {
            Object o;
            int value;
            if ((o = entry.getPropertyValue("ClientPort")) != null && (value = Integer.parseInt(o.toString())) >= 0 && value <= 0xffff)
                ClientPort = value;
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
                check(!JournalPath.equals(o.toString()), JposConst.JPOS_E_ILLEGAL, "Inconsistent JournalPath properties: \"" + JournalPath + "\" - \"" + o.toString() + "\"");
            if ((o = entry.getPropertyValue("JournalMaxSize")) != null && (value = Integer.parseInt(o.toString())) >= 0)
                JournalMaxSize = value;
            if ((o = entry.getPropertyValue("JournalLowSize")) != null && (value = Integer.parseInt(o.toString())) >= 0 && JournalMaxSize <= value)
                JournalLowSize = value;
            if ((o = entry.getPropertyValue("DisplayName")) != null)
                check(!DisplayName.equals(o.toString()), JposConst.JPOS_E_ILLEGAL, "Inconsistent DisplayName properties: \"" + DisplayName + "\" - \"" + o.toString() + "\"");
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property", e);
        }
        Ticket.prepare();
    }

    @Override
    public void changeDefaults(CATProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "CAT service for sample CAT";
        props.CapAuthorizeRefund = true;
        props.CapAuthorizeVoid = true;
        props.CapCenterResultCode = true;
        props.CapAdditionalSecurityInformation = true;
    }

    @Override
    public void changeDefaults(ElectronicJournalProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "ElectronicJournal service for sample CAT";
        props.CapErasableMedium = true;
        props.CapRetrieveCurrentMarker = true;
        props.CapRetrieveMarkerByDateTime = true;
        props.CapRetrieveMarkersDateTime = true;
        props.MediumSizeDef = (JournalMaxSize * JournalWidth * JRN_MAX_LINE_COUNT + Ticket.HEADSIZE) * CURRENCYFACTOR;
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
                    e.printStackTrace();
                }
            }
            ReadThread = null;
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

    private StreamReader ReadThread;

    /**
     * Closes the port
     * @return In case of an IO error, the corresponding exception. Otherwise null
     */
    JposException closePort() {
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
            OutStream.setParam(ClientPort);
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
     * @return JposException in case of communication error, String containing the response otherwise.
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
     */
    void setPrintWidth(int width){
        Object o = sendRecv(String.format("p%d", width), '\0', 0);
    }

    /**
     * Internal command to lock or unlock the terminal.
     * @param lock      If set, lock the terminal. Otherwise, unlock it.
     * @param timeout   Timeout for answer-back.
     * @throws JposException    Communication error occurred.
     */
    void lock(boolean lock, int timeout) throws JposException {
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
    void beginAuthorization (int timeout) throws JposException {
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
    void abort() throws JposException {
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
    void confirm(int transno, boolean committed, int timeout) throws JposException {
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
    void sale(boolean refund, long amount, int timeout) throws JposException {
        Object o = sendRecv((refund ? "r" : "s") + Double.toString((double)amount / 10000.0), 'E', timeout);
        if (o instanceof JposException)
            throw (JposException) o;
        String resp = (String) o;
        if (resp.length() == 0)
            throw new JposException(JposConst.JPOS_E_TIMEOUT, 0, "No valid response within " + timeout + " milliseconds");
        int codeEndIndex = resp.indexOf(STX);
        int code = Integer.parseInt(resp.substring(1, codeEndIndex > 0 ? codeEndIndex : resp.length()));
        switch (code) {
            case 4: // Device locked:
                throw new JposException(JposConst.JPOS_E_EXTENDED, CATConst.JPOS_ECAT_RESET, "Terminal locked");
            case 5: // Not in transaction: Ignore.
                throw new JposException(JposConst.JPOS_E_EXTENDED, CATConst.JPOS_ECAT_COMMANDERROR, "Not in transaction");
            case 6: // Waiting for commit.
                throw new JposException(JposConst.JPOS_E_EXTENDED, CATConst.JPOS_ECAT_COMMANDERROR, "Waiting for commit");
            case 7: // No confirmation requested.
                throw new JposException(JposConst.JPOS_E_EXTENDED, CATConst.JPOS_ECAT_COMMANDERROR, "Authorization active");
            case 3: // Operation abort confirmed
                throw new JposException(JposConst.JPOS_E_EXTENDED, CATConst.JPOS_ECAT_COMMANDERROR, "Authorization aborted");
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
    void rollback(int transno, int timeout) throws JposException {
        Object o = sendRecv(String.format("v%d", transno), 'E', timeout);
        if (o instanceof JposException)
            throw (JposException) o;
        String resp = (String) o;
        if (resp.length() == 0)
            throw new JposException(JposConst.JPOS_E_TIMEOUT, 0, "No valid response within " + timeout + " milliseconds");
        int codeEndIndex = resp.indexOf(STX);
        int code = Integer.parseInt(resp.substring(1, codeEndIndex > 0 ? codeEndIndex : resp.length()));
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
        UniqueIOProcessor oldstream = null;
        UniqueIOProcessor currentStream = null;
        try {
            oldstream = new UniqueIOProcessor(this, "");
        } catch (JposException e) {}
        JposCommonProperties props;
        while (!ToBeFinished) {
            StreamReader reader;
            synchronized (this) {
                if (OutStream == null)
                    initPort();
                currentStream = OutStream;
                reader = ReadThread;
            }
            if (oldstream != currentStream) {
                oldstream = currentStream;
                if ((props = getClaimingInstance(ClaimedCAT, 0)) != null) {
                    try {
                        handleEvent(new JposStatusUpdateEvent(props.EventSource, oldstream == null ? JposConst.JPOS_SUE_POWER_OFF_OFFLINE : JposConst.JPOS_SUE_POWER_ONLINE));
                    } catch (JposException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (reader != null) {
                setPrintWidth(JournalWidth);
                try {
                    reader.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    reader = null;
                }
            }
            if (reader == null) {
                new SyncObject().suspend(CharacterTimeout);
            }
        }
    }

    @Override
    public CATProperties getCATProperties(int index) {
        return new CAT(this);
    }

    @Override
    public ElectronicJournalProperties getElectronicJournalProperties(int index) {
        return new ElectronicJournal(index, this);
    }
}
