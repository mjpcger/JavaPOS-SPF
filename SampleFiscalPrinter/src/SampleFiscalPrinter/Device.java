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
import de.gmxhome.conrad.jpos.jpos_base.linedisplay.*;
import de.gmxhome.conrad.jpos.jpos_base.cashdrawer.*;
import de.gmxhome.conrad.jpos.jpos_base.electronicjournal.*;
import jpos.*;
import jpos.config.JposEntry;
import net.bplaced.conrad.log4jpos.Level;
import java.io.*;
import java.util.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties.ExclusiveAllowed;

/**
 * Base of a JposDevice based implementation of JavaPOS CashDrawer, ElectronicJournal, FiscalPrinter and LineDisplay
 * device service implementations for the sample device implemented in SampleFiscalPrinter.tcl.
 * <p>Supported features are:
 * <br>FiscalPrinter:
 * <ul>
 *     <li>Basic functionality,</li>
 *     <li>Set header, trailer, vat rates, POS ID</li>
 *     <li>Additional lines, additional header and trailer lines, pre and post lines, tax ID</li>
 *     <li>Item and subtotal adjustments (amount, percent, surcharge),</li>
 *     <li>Status handling for receipt (neither journal nor slip station),</li>
 *     <li>Subtotal,</li>
 *     <li>Training Mode,</li>
 *     <li>X report,</li>
 *     <li>Totalizers (only GD_PRINTER_ID, GD_CURRENT_TOTAL, GD_DAILY_TOTAL, GD_GRAND_TOTAL, GD_NOT_PAID, GD_RECEIPT_NUMBER,
 * GD_FISCAL_REC, GD_REFUND, GD_NONFISCAL_REC, GD_Z_REPORT, GD_DESCRIPTION_LENGTH),</li>
 *     <li>Report from counter to counter,</li>
 *     <li>non-fiscal reports.</li>
 * </ul>
 * ElectronicJournal (markers constructed from sessionNo and documentNo using format mask "%d-%d"). Session-No.
 * corresponds to Z count of fiscal printer and document no. to receipt number of the fiscal printer. Supported
 * features:
 * <ul>
 *     <li>Basic electronic journal capabilities,</li>
 *     <li>Print stored data.</li>
 * </ul>
 * LineDisplay:
 * <ul>
 *     <li>Basic LineDisplay capabilities,</li>
 *     <li>2x20 lines.</li>
 * </ul>
 * CashDrawer:
 * <ul>
 *     <li>Basic cash drawer capabilities,</li>
 *     <li>Drawer status reporting.</li>
 * </ul>
 * <p>Here a full list of all device specific properties that can be changed via jpos.xml:
 * <ul>
 *     <li>AdjustmentVoidText: Additional text line for adjustment void. Default "*** ADJUSTMENT VOID ***".</li>
 *     <li>Baudrate: Baud rate of the communication device. Must be one of the baud rate constants specified in the
 *     SerialIOProcessor class. Default: 9600 (SerialIOProcessor.BAUDRATE_9600).
 *     <br>This property may only be set if the communication with the device shall be made via serial port.</li>
 *     <li>CashierID: Cashier id prefix for signon or signoff receipt. Default: "Cashier ID".</li>
 *     <li>CashierName: Cashier name prefix for signon or signoff receipt. Default: "Cashier Name".</li>
 *     <li>CharacterTimeout: Positive integer value, specifying the maximum delay between bytes that belong to the same
 *     frame. Default value: 50 milliseconds.</li>
 *     <li>CurrencyStringAsLong: Specifies how currency values shall be stored in strings. If true, a value of 1.50 will
 *     be stored as "15000" (four implicit decimals). Otherwise, 1.50 will be stored as "1.50". Default false.</li>
 *     <li>Databits: Number of data bits per data unit. Must be 7 or 8. Default: 8. It is strictly recommended to let
 *     this value unchanged.
 *     <br>This property may only be set if the communication with the device shall be made via serial port.</li>
 *     <li>JournalSizeNearFull: Cumulated size of all journal files to generate journal near full status update event.
 *     Default 99% of <i>MaxJournalSize</i>.</li>
 *     <li>MaxJournalSize: Maximum size of all journal files of the device simulator. Default fits for 100 periods,
 *     100000 receipts per period, 10000 characters per receipt.</li>
 *     <li>MaxRetry: Specifies the maximum number of retries. Should be &gt; 0 only for RS232 (real COM ports)
 *     where characters can become lost or corrupted on the communication line. Default: 2.</li>
 *     <li>NonFiscalReceiptWithTrailer: Flag specifying whether non-fiscal receipts shall be printed with (true) or
 *     without (false) trailer lines. Default is false (without trailer lines).</li>
 *     <li>OwnPort: Integer value between 0 and 65535 specifying the TCP port used for communication with the device
 *     simulator. Default: 0 (for random port number selected by operating system).
 *     <br>This property may only be set if the communication with the device shall be made via TCP.</li>
 *     <li>Parity: Parity of each data unit. Must be one of the parity constants specified in the
 *     SerialIOProcessor class. Default: 0 (SerialIOProcessor.PARITY_NONE).
 *     <br>This property may only be set if the communication with the device shall be made via serial port.</li>
 *     <li>PollDelay: Minimum time between status requests, in milliseconds. Status requests will be used to monitor the
 *     device state. Default: 500.</li>
 *     <li>Port: Operating system specific name of the serial communication port (e.g. RS232, Usb2Serial,
 *     Bluetooth...) or the TCP address to be used for
 *     communication with the device simulator. In case of RS232, names look typically like COM2 or /dev/ttyS1. In
 *     case of TCP, names are of the form IPv4:port, where IPv4 is the IP address of the device and port its TCP port.</li>
 *     <li>PrinterResetText: Text to be printed during printer reset. Default "*** RESET PRINTER ***".</li>
 *     <li>RefundVoidText: Additional text line for refund void. Default "*** REFUND VOID ***".</li>
 *     <li>RequestTimeout: Maximum time, in milliseconds, between sending a command to the simulator and getting the
 *     first byte of its response. Default: 2000.</li>
 *     <li>SecretText: Secret code prefix for signon or signoff receipt. Default: "Secret".</li>
 *     <li>SerialNumber: Fiscal printer serial number. If not set, serial no. of the currently connected printer will be
 *     retrieved. Otherwise, serial number must match the serial number stored in the printer. Default is not set.</li>
 *     <li>SignOffHeader: Header text for signoff receipt. Default: "S I G N O F F".</li>
 *     <li>SignOnHeader: Header text for signon receipt. Default: "S I G N O N".</li>
 *     <li>Stopbits: Number of stop bits per data unit. Must be 1 or 2. Default: 2.
 *     <br>This property may only be set if the communication with the device shall be made via serial port.</li>
 *     <li>UsbToSerial: Specifies whether the specified port is a virtual port that will be removed by the operating
 *     system when the device is not connected. Default: false.
 *     <br>This property may only be set if the communication with the device shall be made via serial port.</li>
 * </ul>
 */
public class Device extends JposDevice implements Runnable {
    protected Device(String id) {
        super(id);
        fiscalPrinterInit(1);
        lineDisplayInit(1);
        electronicJournalInit(1);
        cashDrawerInit(1);
        PhysicalDeviceDescription = "Fiscal printer simulator for TCP and COM port";
        PhysicalDeviceName = "Fiscal printer Simulator";
        CapPowerReporting = JposConst.JPOS_PR_ADVANCED;
    }

    @Override
    public void checkProperties(JposEntry entry) throws JposException {
        super.checkProperties(entry);
        try {
            new TcpClientIOProcessor(this, ID);
            TcpType = true;
        } catch (JposException e) {
            TcpType = false;
        }
        try {
            Object o;
            int value;
            if ((o = entry.getPropertyValue("Baudrate")) != null) {
                Baudrate = Integer.parseInt(o.toString());
                if (TcpType)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property: Baudrate");
            }
            if ((o = entry.getPropertyValue("Databits")) != null) {
                Databits = Integer.parseInt(o.toString());
                if (TcpType)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property: Databits");
            }
            if ((o = entry.getPropertyValue("Stopbits")) != null) {
                Stopbits = Integer.parseInt(o.toString());
                if (TcpType)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property: Stopbits");
            }
            if ((o = entry.getPropertyValue("Parity")) != null) {
                Parity = Integer.parseInt(o.toString());
                if (TcpType)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property: Parity");
            }
            if ((o = entry.getPropertyValue("OwnPort")) != null) {
                int port = Integer.parseInt(o.toString());
                if (port < 0 || port > 0xffff)
                    throw new IOException("Invalid TCP port: " + o.toString());
                OwnPort = port;
                if (!TcpType)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property: OwnPort");
            }
            if ((o = entry.getPropertyValue("RequestTimeout")) != null && (value = Integer.parseInt(o.toString())) > 0)
                RequestTimeout = value;
            if ((o = entry.getPropertyValue("CharacterTimeout")) != null && (value = Integer.parseInt(o.toString())) > 0)
                CharacterTimeout = value;
            if ((o = entry.getPropertyValue("MaxRetry")) != null)
                MaxRetry = Integer.parseInt(o.toString());
            if ((o = entry.getPropertyValue("PollDelay")) != null)
                PollDelay = Integer.parseInt(o.toString());
            if ((o = entry.getPropertyValue("UsbToSerial")) != null)
                UsbToSerial = Boolean.parseBoolean(o.toString());
            if ((o = entry.getPropertyValue("MaxJournalSize")) != null)
                MaxJournalSize = Long.parseLong(o.toString());
            if ((o = entry.getPropertyValue("JournalSizeNearFull")) != null)
                JournalSizeNearFull = Long.parseLong(o.toString());
            if ((o = entry.getPropertyValue("SerialNumber")) != null)
                SerialNumber = o.toString();
            if ((o = entry.getPropertyValue("NonFiscalReceiptWithTrailer")) != null) {
                NonFiscalReceiptWithTrailer = Boolean.parseBoolean(o.toString()) ? "1" : "0";
            }
            if ((o = entry.getPropertyValue("PrinterResetText")) != null)
                PrinterResetText = o.toString();
            if ((o = entry.getPropertyValue("CurrencyStringAsLong")) != null)
                CurrencyStringAsLong = Boolean.parseBoolean(o.toString());
            if ((o = entry.getPropertyValue("SignOnHeader")) != null)
                SignOnHeader = o.toString();
            if ((o = entry.getPropertyValue("SignOffHeader")) != null)
                SignOffHeader = o.toString();
            if ((o = entry.getPropertyValue("CashierName")) != null)
                CashierName = o.toString();
            if ((o = entry.getPropertyValue("CashierID")) != null)
                CashierID = o.toString();
            if ((o = entry.getPropertyValue("SecretText")) != null)
                SecretText = o.toString();
            if ((o = entry.getPropertyValue("AdjustmentVoidText")) != null)
                AdjustmentVoidText = o.toString();
            if ((o = entry.getPropertyValue("RefundVoidText")) != null)
                RefundVoidText = o.toString();
            FiscalPrinterService srv = (FiscalPrinterService) getPropertySetInstance(FiscalPrinters, 0, 0).EventSource;
            if ((o = entry.getPropertyValue("AllowItemAdjustmentTypesInPackageAdjustment")) != null)
                srv.AllowItemAdjustmentTypesInPackageAdjustment = Boolean.parseBoolean(o.toString());
            check(SignOffHeader.length() > MAXPRINTLINELENGTH, JposConst.JPOS_E_ILLEGAL, "Signoff header too long");
            check(SignOnHeader.length() > MAXPRINTLINELENGTH, JposConst.JPOS_E_ILLEGAL, "Signon header too long");
            check(CashierName.length() > MAXFIXEDTEXTLENGTH, JposConst.JPOS_E_ILLEGAL, "Cashier prefix too long");
            check(CashierID.length() > MAXFIXEDTEXTLENGTH, JposConst.JPOS_E_ILLEGAL, "Cashier prefix too long");
            check(SecretText.length() > MAXFIXEDTEXTLENGTH, JposConst.JPOS_E_ILLEGAL, "Secret prefix too long");
            check(PrinterResetText.length() > MAXFISCALPRINTLINE, JposConst.JPOS_E_ILLEGAL, "Invalid PrinterResetText: " + PrinterResetText);
            check(centeredLine(AdjustmentVoidText).length() > MAXFISCALPRINTLINE, JposConst.JPOS_E_ILLEGAL, "Adjustment void text too long");
            check(centeredLine(RefundVoidText).length() > MAXFISCALPRINTLINE, JposConst.JPOS_E_ILLEGAL, "Refund void text too long");
            check(MaxJournalSize - JournalSizeNearFull < 10000, JposConst.JPOS_E_ILLEGAL, "JournalSizeNearFull too big: " + JournalSizeNearFull);
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, e.getMessage(), e);
        }
    }

    /**
     * Returns a String starting with as many space characters as necessary to place the given text centered (in
     * the middle) on a print line.
     * @param text  Test to be centered.
     * @return  See description.
     */
    String centeredLine(String text) {
        String format = "%" + ((MAXPRINTLINELENGTH + text.length()) / 2) + "s";
        return String.format(format, text);
    }

    /**
     * Specifies whether the device is connected via tcp. If false, connection is via COM port.
     */
    private Boolean TcpType = null;

    /*
     * Internally used control characters.
     */
    private final static byte ETX = 0x03;
    private final static byte ETB = 0x17;
    private final static byte ACK = 0x06;
    private final static byte NAK = 0x15;

    /**
     * Pseudo control character used to concatenate receipts.
     */
    final static byte FF = 0x0c;

    /**
     * Pseudo control character used to mark the beginning of secret values. Secret values are only printed but not
     * stored in journal nor printed on receipt copies.
     */
    final static byte SO = 0x0e;

    /**
     * Pseudo control character used to mark the ending of secret values. Secret values are only printed but not
     * stored in journal nor printed on receipt copies.
     */
    final static byte SI = 0x0f;

    /**
     * Maximum print line length (42).
     */
    final static int MAXPRINTLINELENGTH = 42;

    /**
     * Maximum print line length in fiscal receipts (40).
     */
    final static int MAXFISCALPRINTLINE = 40;

    /**
     * Maximum display line length (20).
     */
    final static int MAXDISPLAYLINELENGTH = 20;

    /**
     * Maximum number of display lines (2).
     */
    final static int MAXDISPLAYLINE = 2;

    /**
     * Maximum length of any description text (20).
     */
    final static int MAXDESCRIPTIONLENGTH = 20;

    /**
     * Maximum length for variable fixed text parts in fixed output printing commands (18).
     */
    final static int MAXFIXEDTEXTLENGTH = 18;

    /**
     * Maximum length of a quantity dimension (6). Valid dimensions are strings like "mm", "kg", "ltr"...
     */
    final static int MAXDIMENSIONLENGTH = 6;

    /**
     * Limit for VAT rate index (4).
     */
    final static int MAXVATINDEX = 4;

    /**
     * Maximum number of header lines.
     */
    final static int MAXHEADERLINES = 4;

    /**
     * Maximum number of trailer lines.
     */
    final static int MAXTRAILERLINES = 4;

    /**
     * Factor to be used to compute between CURRENCY and long (10000).
     */
    final static int CURRENCYFACTOR = 10000;

    /**
     * Factor to be used to compute between int and quantity (1000).
     */
    final static int QUANTITYFACTOR = 1000;

    /**
     * Maximum value for cashier id. For this implementation, only numerical values between 1 and MAXCASHIER are allowed.
     * A value of 0 is allowed to unset the cashier id.
     */
    final static int MAXCASHIER = 99999;

    /**
     * Maximum value for pos id. For this implementation, only numerical values between 1 and MAXPOSID are allowed.
     * A value of 0 is allowed to unset the pos id.
     */
    final static int MAXPOSID = 99999;

    /**
     * Maximum period (100). Capacity of the fiscal memory.
     */
    static final int MAXPERIOD = 100;       // Maximum period of simulator (real fiscal printers have higher values)

    private final static int Version = 1014001;
    private final static int FiscalPrinterVersion = 1015001;    // FiscalPrinter conforms to UPOS version 1.15

    /**
     * Maximum time between acknowledge and response frame. Default: 2000. Can be set via jpos.xml.
     */
    public  int RequestTimeout = 2000;
    private int CharacterTimeout = 50;      // Maximum delay between two characters of the same frame.
    private int AckTimeout = 1000;          // Maximum time between a command and the corresponding response.
    private int PollDelay = 500;            // Delay between status poll cycles
    private int OwnPort = 0;                // Own port, used in case of TCP connection.
                                            // The following four values will only be used in case of a COM port.
    private int Baudrate = SerialIOProcessor.BAUDRATE_9600;
    private int Databits = SerialIOProcessor.DATABITS_8;
    private int Stopbits = SerialIOProcessor.STOPBITS_2;
    private int Parity = SerialIOProcessor.PARITY_NONE;

    private UniqueIOProcessor Stream = null;
    private Thread StateWatcher = null;
    private boolean ToBeFinished;

    private boolean InIOError = false;

    private char[] CurrentState = new char[0];      // Updated when receiving a response

    /**
     * Maximum number of retries. Default: 2. Can be set via jpos.xml.
     */
    int MaxRetry = 2;
    private boolean UsbToSerial = false;
    private int[] OpenCount = new int[]{0};

    /**
     * Maximum size of all journal files of the device simulator. Default fits for 100 periods, 100000 receipts
     * per period, 10000 characters per receipt. Can be set via Jpos.xml.
     */
    long MaxJournalSize = 100000000000L;

    /**
     * Cumulated size of all journal files to generate journal near full status update event. Default 99% of
     * <i>MaxJournalSize</i>. Can be set via Jpos.xml.
     */
    long JournalSizeNearFull = 99000000000L;

    /**
     * Current cumulated size of all journal files of the simulator. Retrieved by polling thread. Initialized to zero.
     */
    long CurrentJournalSize = 0L;

    /**
     * Object used to wait until next poll has been finished. Used to wait until status has been updated after commands
     * that should result in a status change.
     */
    SyncObject PollWaiter;

    /**
     * Fiscal printer serial number. If not set, serial no. of the currently connected printer will be retrieved.
     * Otherwise, serial number must match the serial number stored in the printer. Default is not set. Can be set
     * via Jpos.xml.
     */
    String SerialNumber = "";

    /**
     * Specifies how currency values shall be stored in strings. If true, a value of 1.50 will be stored as "15000" (four
     * implicit decimals). Otherwise, 1.50 will be stored as "1.50". Default false. Can be set via Jpos.xml.
     */
    boolean CurrencyStringAsLong = false;

    /**
     * Flag specifying whether non-fiscal receipts shall be printed with ("1") or without ("0") trailer lines.
     * Default is "0" (without trailer lines). Can be modified via Jpos.xml.
     */
    String NonFiscalReceiptWithTrailer = "0";

    /**
     * Text to be printed during printer reset. Default "*** RESET PRINTER ***". Can be modified via Jpos.xml.
     */
    String PrinterResetText = "*** RESET PRINTER ***";

    /**
     * Current period number as retrieved from printer. Zero before fiscalization, 100 when fiscal memory is full.
     */
    int  CurrentPeriod = 0;

    /**
     * Command processing result flag. The first byte of any response. "1" for successful command processing.
     */
    static final char SUCCESS = '1';

    /**
     * Index for fiscal state of printer (0).
     */
    static final int FISCAL = 0;

    /**
     * Index for fiscal state of training mode (1).
     */
    static final int TRAININGMODE = 1;

    /**
     * Index for fiscal state of fiscal memory (2).
     */
    static final int MEMORY = 2;

    /**
     * Index for state of fiscal day (3).
     */
    static final int DAY = 3;

    /**
     * Index for receipt part of state (4).
     */
    static final int RECEIPT = 4;

    /**
     * Index of printer part of state (5).
     */
    static final int PRINTER = 5;

    /**
     * Index for drawer part of state (6).
     */
    static final int DRAWER = 6;

    /**
     * State[FISCAL] when printer has not been fiscalized ('0').
     */
    static final char NOTFISCALIZED = '0';

    /**
     * State[FISCAL] when printer has been fiscalized ('1').
     */
    static final char FISCALIZED = '1';

    /**
     * State[FISCAL] when fiscal memory is full ('2').
     */
    static final char FISCALBLOCK = '2';

    /**
     * State[TRAININGMODE] whenever training mode is inactive ('0').
     */
    static final char OFF = '0';

    /**
     * State[TRAININGMODE] whenever training mode is active ('1').
     */
    static final char ON = '1';

    /**
     * State[MEMORY] whenever fiscal memory is ok ('0'). as well as
     * State[PRINTER] whenever the printer is able to print.
     */
    static final char OK = '0';

    /**
     * State[MEMORY] whenever fiscal memory is low ('1').
     */
    static final char NEARFULL = '1';

    /**
     * State[MEMORY] whenever fiscal memory is full ('2').
     */
    static final char FULL = '2';

    /**
     * Status[DAY] value that represents day not opened state. as well as
     * Status[RECEIPT] value that represents receipt not started and
     * Status[DRAWER] value that represents drawer has been closed.
     */
    static final char CLOSED = '0';

    /**
     * Status[DAY] value that represents day opened state aswell as
     * Status[DRAWER] value that represents drawer is open.
     */
    static final char OPENED = '1';

    /**
     * State[RECEIPT] after starting fiscal receipt before payment has been started.
     */
    static final char ITEMIZING = '1';

    /**
     * State[RECEIPT] after payment has been started but not finished.
     */
    static final char PAYING = '2';

    /**
     * State[RECEIPT] after payment has been finished.
     */
    static final char FINALIZING = '3';

    /**
     * State[RECEIPT] value for non-fiscal receipts.
     */
    static final char NONFISCAL = '4';

    /**
     * State[RECEIPT] when printer is blocked ('5'). Set whenever printing is not possible, e.g. due to missing paper.
     */
    static final char BLOCKED = '5';

    /**
     * State[PRINTER] when paper is near end ('1').
     */
    static final char NEAREND = '1';

    /**
     * State[PRINTER] when paper is empty ('2').
     */
    static final char EMPTY = '2';

    /**
     * State[PRINTER] when cover is open or printer is not working ('3').
     */
    static final char COVERORERROR = '3';

    /**
     * Current VAT table.
     */
    long[] VatTable = new long[MAXVATINDEX];

    /**
     * New VAT table. Values must be in descending order.
     */
    long[] NewVatTable = new long[MAXVATINDEX];

    /**
     * Flag showing whether the VAT table must be initialized (true) or not. Initial value is true.
     */
    boolean InitializeVatTable = true;

    /**
     * Command for DirectIO: Send native command(s) and retrieve the response(s).
     */
    public static final int SAMPLEFISCALPRINTERDIO_EXECCOMMAND = 100;

    /**
     * DirectIO: Sets flag for non-fiscal receipt printing (with / without trailer lines).
     */
    public static final int SAMPLEFISCALPRINTERDIO_NONFISCALRECEIPTTRAILER = 101;

    /**
     * DirectIO: Perform fiscalization. In addition to VAT table, header and trailer lines, store fiscal ID and
     * POS ID, the serial number must be set as well. This can be done in the simulator (debug window, "set SerialNo [number]",
     * where [number] should be replaced by any unique number).
     */
    public static final int SAMPLEFISCALPRINTERDIO_FISCALIZE = 102;

    /**
     * BeginFixedOutput: documentType for sign on. It can be used to set the cashier number after day has been opened to
     * a non-zero value.
     */
    public static final int SAMPLEFISCALPRINTERFXO_SIGNON = 1;

    /**
     * BeginFixedOutput: documentType for sign off. It can be used to set the cashier number after day has been opened to
     * zero.
     */
    public static final int SAMPLEFISCALPRINTERFXO_SIGNOFF = 2;

    /**
     * Header of fixed output receipts. Data must be an empty string.
     */
    public static final int SAMPLEFISCALPRINTERFXO_HEAD = 1;

    /**
     * First line no. of SIGNON receipt. Data must be the number of the cashier to be signed on.
     */
    public static final int SAMPLEFISCALPRINTERFXO_ON_CASHIER = 2;

    /**
     * Last line of a SIGNON. Data must be an empty string.
     */
    public static final int SAMPLEFISCALPRINTERFXO_ON_END = 3;

    /**
     * First line no. of SIGNOFF receipt. Data must be the number of the cashier to be signed off.
     */
    public static final int SAMPLEFISCALPRINTERFXO_OFF_CASHIER = 2;

    /**
     * Optional second line of a SIGNOFF receipt. Data must be a secret number used for later sign on operations.
     */
    public static final int SAMPLEFISCALPRINTERFXO_OFF_SECRET = 3;

    /**
     * Last line of a SIGNOFF. Data must be an empty string.
     */
    public static final int SAMPLEFISCALPRINTERFXO_OFF_END = 4;

    /**
     * Fixed output document number. Zero for non-fiscal receipts.
     */
    int FixedOutputDocument = 0;

    /**
     * Mininum line number of next fixed output line.
     */
    int NonFiscalMinLineNo = 0;

    /**
     * Contains fixed output layouts. First element contains a list of all allowed document types.
     * Further lines contain an array of allowed minimum line values: AllowedDocumentLineTable[i][j] contains
     * the minimum line number after line (j-1) has been printed or -1 for the last line.
     */
    long[][] AllowedDocumentLineTable = new long[][]{
            new long[]{SAMPLEFISCALPRINTERFXO_SIGNON, SAMPLEFISCALPRINTERFXO_SIGNOFF},
            new long[]{SAMPLEFISCALPRINTERFXO_ON_CASHIER, SAMPLEFISCALPRINTERFXO_ON_END, -1},
            new long[]{SAMPLEFISCALPRINTERFXO_OFF_CASHIER, SAMPLEFISCALPRINTERFXO_OFF_SECRET, SAMPLEFISCALPRINTERFXO_OFF_END, -1}
    };

    /**
     * Header text for signon receipt. Default: "S I G N O N". Can be changed via Jpos.xml.
     */
    String SignOnHeader = "S I G N O N";

    /**
     * Header text for signoff receipt. Default: "S I G N O F F". Can be changed via Jpos.xml.
     */
    String SignOffHeader = "S I G N O F F";

    /**
     * Cashier name prefix for signon or signoff receipt. Default: "Cashier Name". Can be changed via Jpos.xml.
     */
    String CashierName = "Cashier Name";

    /**
     * Cashier id prefix for signon or signoff receipt. Default: "Cashier ID". Can be changed via Jpos.xml.
     */
    String CashierID = "Cashier ID";

    /**
     * Secret code prefix for signon or signoff receipt. Default: "Secret". Can be changed via Jpos.xml.
     */
    String SecretText = "Secret";

    /**
     * Additional text line for adjustment void. Default "*** ADJUSTMENT VOID ***". Can be changed via Jpos.xml.
     */
    String AdjustmentVoidText = "*** ADJUSTMENT VOID ***";

    /**
     * Additional text line for refund void. Default "*** REFUND VOID ***". Can be changed via Jpos.xml.
     */
    String RefundVoidText = "*** REFUND VOID ***";

    /**
     * Holds all characters not yet printed due to outstanding LF
     * character.
     */
    String PrintBuffer = "";

    /**
     * Flag for void handling. Will be set in BeginFiscalReceipt and cleared in first PrintRec... method.
     */
    boolean VoidOnEndFiscal = true;

    /**
     * Last request for subtotal adjustment. Necessary to perform the corresponding void method. Initialized and
     * reset to <i>null</i> whenever subtotal adjustments are not allowed.
     */
    PrintRecSubtotalAdjustment SubtotalAdjustment = null;

    @Override
    public void changeDefaults(FiscalPrinterProperties props) {
        props.DeviceServiceDescription = "Fiscal printer service for sample fiscal printer";
        props.DeviceServiceVersion = FiscalPrinterVersion;
        props.ActualCurrencyDef = FiscalPrinterConst.FPTR_AC_EUR;
        props.CapAdditionalHeader = true;
        props.CapAdditionalLines = true;
        props.CapAdditionalTrailer = true;
        props.CapAmountAdjustment = true;
        props.CapHasVatTable = true;
        props.CapFixedOutput = true;
        props.CapFiscalReceiptStation = true;
        props.CapNonFiscalMode = true;
        props.CapPercentAdjustment = true;
        props.CapPositiveAdjustment = true;
        props.CapPositiveSubtotalAdjustment = true;
        props.CapPostPreLine = true;
        props.CapRecEmptySensor = true;
        props.CapRecNearEndSensor = true;
        props.CapRecPresent = true;
        props.CapRemainingFiscalMemory = true;
        props.CapSetHeader = true;
        props.CapSetPOSID = true;
        props.CapSetStoreFiscalID = true;
        props.CapSetTrailer = true;
        props.CapSetVatTable = true;
        props.CapSubAmountAdjustment = true;
        props.CapSubPercentAdjustment = true;
        props.CapSubtotal = true;
        props.CapTrainingMode = true;
        props.CapXReport = true;
        props.MessageLengthDef = 30;
        props.NumHeaderLines = MAXHEADERLINES;
        props.NumTrailerLines = MAXTRAILERLINES;
        props.NumVatRates = MAXVATINDEX;
        props.QuantityDecimalPlacesDef = 3;
        props.QuantityLengthDef = 9;
        // props.CoverOpenDef, props.DayOpenedDef, props.PrinterStateDef, props.RecEmptyDef, props.RecNearEndDef,
        // props.RemainingFiscalMemoryDef and props.TrainingModeActiveDef must be set before enabling the device.
        // They can only be set to meaningful values after the device has been claimed and some successful communication
        // with the device has been made.
    }

    @Override
    public void changeDefaults(ElectronicJournalProperties props) {
        props.DeviceServiceDescription = "Electronic journal service for sample fiscal printer";
        props.DeviceServiceVersion = Version;
        props.CapPrintContent = true;
        props.CapRetrieveCurrentMarker = true;
        props.CapRetrieveMarker = true;
        props.CapRetrieveMarkersDateTime = true;
        // props.MediumFreeSpaceDef and props.MediumSizeDef must be set before first enable, but need
        // communication with the device. Therefore, they cannot be set to meaningful values before the
        // device has been claimed successfully.
    }

    @Override
    public void changeDefaults(LineDisplayProperties props) {
        props.DeviceServiceDescription = "Line display service for sample fiscal printer";
        props.DeviceServiceVersion = Version;
        // Defaults are good for plain 2x20 character display. Changes of defaults only for Unicode support:
        props.CapCharacterSet = LineDisplayConst.DISP_CCS_UNICODE;
        props.CharacterSetDef = LineDisplayConst.DISP_CS_UNICODE;
        props.CharacterSetList = "997";
        props.DeviceRows = MAXDISPLAYLINE;
        props.DeviceColumns = MAXDISPLAYLINELENGTH;
        props.ScreenModeList = MAXDISPLAYLINE + "x" + MAXDISPLAYLINELENGTH;
    }

    @Override
    public void changeDefaults(CashDrawerProperties props) {
        props.DeviceServiceDescription = "Drawer service for sample fiscal printer";
        props.DeviceServiceVersion = Version;
        // All other defaults match the abilities of this service.
    }

    private JposException closePort() {
        JposException e = null;
        if (Stream != null) {
            try {
                Stream.close();
            } catch (JposException ee) {
                e = ee;
            }
            Stream = null;
        }
        return e;
    }

    private JposException initPort() {
        try {
            if (!TcpType) {
                SerialIOProcessor ser;
                Stream = ser = new SerialIOProcessor(this, ID);
                ser.setParameters(Baudrate, Databits, Stopbits, Parity);
            }
            else {
                TcpClientIOProcessor tcp;
                Stream = tcp = new TcpClientIOProcessor(this, ID);
                tcp.setParam(OwnPort);
            }
            Stream.open(InIOError);
            InIOError = false;
        } catch (JposException e) {
            Stream = null;
            return e;
        }
        return null;
    }

    /**
     * Basic method used to establish a connection (if not established), send a command and retrieve a response.
     * @param args Array of command components,
     * @return Array of response components, <i>null</i> on IO error, zero length array on timeout.
     */
    synchronized String[] sendrecv(String[] args) {
        if (Stream == null) {
            JposException e = initPort();
            if (e != null) {
                return null;
            }
        }
        try {
            String[] response = sendFrameRetrieveResponse(args);
            if (response != null) return response;
        } catch (Exception e) {
            log(Level.TRACE, ID + ": IO error: " + e.getMessage());
            JposException ee = closePort();
            InIOError = true;
            return null;
        }
        return new String[0];
    }

    private String[] sendFrameRetrieveResponse(String[] args) throws JposException, UnsupportedEncodingException {
        for (int sendretry = 0; sendretry < MaxRetry; sendretry++) {
            Stream.flush();
            Stream.write(getFrame(args));
            if (retrieveAcknowledge())
                continue;   // On timeout or NAK, response must be sent again.
            byte[] acknowledge;
            byte[] response = new byte[0];
            Stream.setTimeout(RequestTimeout);
            while ((acknowledge = Stream.read(1)).length == 1) {
                response = retrieveResponse(acknowledge, response);
                if (response[response.length - 1] == ETX && response[response.length - 3] == ETB && getChecksum(response) % 96 == 31) {
                    // The frame is valid: Truncate ETB SUM ETX from frame, convert to string, split at ETB
                    return new String(Arrays.copyOf(response, response.length - 3), "UTF8").split(new String(new char[]{ETB}), -1);
                }
                Stream.setTimeout(CharacterTimeout);
            }
            // Invalid frame: Don't retry original frame, retrieve last response instead.
            args = new String[]{"lastResponse"};
            checkPortPresent(acknowledge);
        }
        return null;
    }

    private void checkPortPresent(byte[] acknowledge) throws JposException {
        check(!TcpType && acknowledge.length == 0 && !((SerialIOProcessor)Stream).exists(), JposConst.JPOS_E_FAILURE, "Port missing");
    }

    private byte[] retrieveResponse(byte[] acknowledge, byte[] response) throws JposException {
        int partlen = Stream.available();
        int resplen = response.length;
        byte[] part = null;
        if (partlen > 0) {
            partlen = (part = Stream.read(partlen)).length;
        }
        response = Arrays.copyOf(response, resplen + 1 + partlen);
        response[resplen] = acknowledge[0];
        if (partlen > 0)
            System.arraycopy(part, 0, response, resplen + 1, partlen);
        return response;
    }

    private boolean retrieveAcknowledge() throws JposException {
        Stream.setTimeout(AckTimeout);
        byte[] acknowledge = Stream.read(1);
        if (acknowledge.length == 0 || acknowledge[0] == NAK) {
            checkPortPresent(acknowledge);
            return true;
        }
        return false;
    }

    private byte[] getFrame(String[] args) throws UnsupportedEncodingException {
        String command = "";
        for (int i = 0; i < args.length; i++) {
            command = command + args[i] + new String(new byte[]{ETB});
        }
        byte[] frame = command.getBytes("UTF8");
        byte[] ret = Arrays.copyOf(frame, frame.length + 2);
        ret[frame.length] = (byte) (127 - getChecksum(frame) % 96);
        ret[frame.length + 1] = ETX;
        return ret;
    }

    private int getChecksum(byte[] frame) {
        int sum = 0;
        for (int i = 0; i < frame.length; i++) {
            if (frame[i] == ETX)
                break;
            sum += (int) frame[i] & 0xff;
        }
        return sum;
    }

    @Override
    public void run() {
        String[][] commands = new String[][]{new String[]{"getJournalUsed"}, new String[]{"get", "SerialNo", "Period"}};
        char[] newstate;
        String newserno = SerialNumber;
        long newjournalsize = CurrentJournalSize;
        prepareStatusWaitingObjects();
        for (int index = 0; !ToBeFinished; index = 1 - index) {
            try {
                String[] result = sendrecv(commands[index]);
                if (result == null || result.length < 1 || result[0].length() < DRAWER + 2) {
                    index = 1;
                    newstate = new char[0];
                } else {
                    newstate = result[0].substring(1).toCharArray();
                    if (index == 0) {
                        newjournalsize = Long.parseLong(result[1]);
                    } else {
                        if (SerialNumber.equals(""))
                            SerialNumber = result[1];
                        newserno = result[1];
                        if (result.length > 2) {
                            CurrentPeriod = Integer.parseInt(result[2]);
                        }
                    }
                }
                if (index == 1) {
                    handleStates(newstate, newserno, newjournalsize);
                    signalStatusWaits(FiscalPrinters[0]);
                    StartPollingWaiter.signalWaiter();
                    PollWaiter.suspend(PollDelay);
                    prepareStatusWaitingObjects();
                }
            } catch (Exception e) {
                index = 1 - index;      // try it again
            }
        }
    }

    private void prepareStatusWaitingObjects() {
        prepareSignalStatusWaits(CashDrawers[0]);
        prepareSignalStatusWaits(ElectronicJournals[0]);
        prepareSignalStatusWaits(FiscalPrinters[0]);
        prepareSignalStatusWaits(LineDisplays[0]);
    }

    private void handleStates(char[] newstate, String newserno, long newjournalsize) {
        char[] oldstate = CurrentState;
        if ((newstate.length <= DRAWER) != (oldstate.length <= DRAWER) || InitializeVatTable) {
            newstate = fillVatTable(newstate);
            if ((newstate.length <= DRAWER) != (oldstate.length <= DRAWER)) {
                handlePowerState(newstate);
                signalStatusWaits(CashDrawers[0]);
            }
            if (oldstate.length <= DRAWER)
                oldstate = "9999999".toCharArray();
        }
        if (!newserno.equals(SerialNumber) && !SerialNumber.equals("")) {
            log(Level.ERROR, ID + ": Serial number does not match the expected number: " + SerialNumber + " - " + newserno);
            if (newstate.length > RECEIPT) {
                newstate[RECEIPT] = BLOCKED;
            }
        }
        synchronized (CurrentState) {
            CurrentState = newstate;
        }
        if (newstate.length > DRAWER) {
            if (newjournalsize > CurrentJournalSize) {
                handleEJState(newjournalsize);
            }
            if (newstate[DRAWER] != oldstate[DRAWER]) {
                handleDrawerState(newstate[DRAWER]);
                signalStatusWaits(CashDrawers[0]);
            }
            handlePrinterState();
        }
    }

    /**
     * Retrieves a copy of the current device state.
     * @return See description.
     */
    char[] getCurrentState() {
        char[] actstate;
        synchronized(CurrentState) {
            actstate = Arrays.copyOf(CurrentState, CurrentState.length);
        }
        return actstate;
    }

    private void handlePrinterState() {
        JposCommonProperties props = getClaimingInstance(ClaimedFiscalPrinter, 0);
        if (props != null && props.DeviceEnabled) {
            ((StatusUpdater)props).updateState(false);
        }
    }

    private JposException handleDrawerState(char c) {
        try {
            JposCommonProperties props = getPropertySetInstance(CashDrawers, 0, 0);
            if (props != null) {
                int state = c == OPENED ? CashDrawerConst.CASH_SUE_DRAWEROPEN : CashDrawerConst.CASH_SUE_DRAWERCLOSED;
                handleEvent(new CashDrawerStatusUpdateEvent(props.EventSource, state));
            }
        } catch (JposException e) {
            return e;
        } catch (IndexOutOfBoundsException e) {}
        return null;
    }


    private JposException handleEJState(long newjournalsize) {
        long oldsize = CurrentJournalSize;
        CurrentJournalSize = newjournalsize;
        try {
            ElectronicJournalProperties jrn = (ElectronicJournalProperties)getClaimingInstance(ClaimedElectronicJournal, 0);
            if (newjournalsize > oldsize) {
                if (jrn != null) {
                    if (jrn.DeviceEnabled)
                        jrn.MediumFreeSpace = (newjournalsize > MaxJournalSize ? 0 : MaxJournalSize - newjournalsize) * 10000;
                    else
                        jrn.MediumFreeSpaceDef = (newjournalsize > MaxJournalSize ? 0 : MaxJournalSize - newjournalsize) * 10000;
                }
            }
            if (oldsize <= JournalSizeNearFull && newjournalsize > JournalSizeNearFull) {
                handleEvent(new ElectronicJournalStatusUpdateEvent(jrn.EventSource, ElectronicJournalConst.EJ_SUE_MEDIUM_NEAR_FULL));
            }
            else if (newjournalsize > MaxJournalSize) {
                if (oldsize <= MaxJournalSize) {
                    handleEvent(new ElectronicJournalStatusUpdateEvent(jrn.EventSource, ElectronicJournalConst.EJ_SUE_MEDIUM_FULL));
                }
                synchronized (CurrentState) {
                    CurrentState[FISCAL] = FISCALBLOCK;
                }
            }
        } catch (JposException e) {
            return e;
        }
        return null;
    }

    private void handlePowerState(char[] newstate) {
        int state;
        try {
            JposCommonProperties props = getClaimingInstance(ClaimedFiscalPrinter, 0);
            FiscalPrinterProperties prt = (FiscalPrinterProperties) props;
            if (newstate.length > DRAWER) {
                state = JposConst.JPOS_SUE_POWER_ONLINE;
            }
            else {
                state = JposConst.JPOS_SUE_POWER_OFF_OFFLINE;
                if (prt != null && prt.DeviceEnabled && prt.PrinterState != FiscalPrinterConst.FPTR_PS_LOCKED) {
                    prt.PrinterState = FiscalPrinterConst.FPTR_PS_LOCKED;
                    prt.EventSource.logSet("PrinterState");
                }
            }
            if ((props = getPropertySetInstance(CashDrawers, 0, 0)) != null) {
                handleEvent(new CashDrawerStatusUpdateEvent(props.EventSource, state));
            }
            if ((props = getClaimingInstance(ClaimedLineDisplay, 0)) != null) {
                handleEvent(new JposStatusUpdateEvent(props.EventSource, state));
            }
            if ((props = getClaimingInstance(ClaimedElectronicJournal, 0)) != null) {
                handleEvent(new ElectronicJournalStatusUpdateEvent(props.EventSource, state));
            }
            if ((prt) != null) {
                handleEvent(new FiscalPrinterStatusUpdateEvent(prt.EventSource, state));
            }
        } catch (JposException e) {}
    }

    private char[] fillVatTable(char[] newstate) {
        JposCommonProperties props = getClaimingInstance(ClaimedFiscalPrinter, 0);
        if (props != null) {
            FiscalPrinterService prt = (FiscalPrinterService) props.EventSource;
            String[] cmd = new String[MAXVATINDEX + 2];
            cmd[0] = "get";
            cmd[1] = "VAT";
            for (int i = 1; i <= MAXVATINDEX; i++) {
                cmd[i + 1] = Long.toString(i);
            }
            String resp[] = sendrecv(cmd);
            if (resp == null || resp.length != MAXVATINDEX + 1 || resp[0].length() != newstate.length + 1 || resp[0].charAt(0) != SUCCESS) {
                return new char[0];
            }
            // Initialize VAT table here.
            for (int i = 0; i < VatTable.length; i++) {
                try {
                    VatTable[i] = prt.stringToCurrency(resp[i + 1], "VAT " + (i + 1));
                } catch (JposException e) {}
            }
            if (InitializeVatTable) {
                System.arraycopy(VatTable, 0, NewVatTable, 0, NewVatTable.length);
                InitializeVatTable = false;
            }
            return resp[0].substring(1).toCharArray();
        }
        return newstate;
    }

    /**
     * Update status properties on enable. If enabled, the common status properties will be updated to the current
     * statu. Depending on the type of the given property set, drawer, electronic journal or fiscal printer status
     * values will be updated as well.
     * @param dev       Property set to be updated.
     * @param enable    If true, update will be made. If false, no action will be performed.
     */
    void updateStates(JposCommonProperties dev, boolean enable) {
        if (enable) {
            ((StatusUpdater) dev).updateState(true);
        }
    }

    /**
     * Major part of device initialization. Increments open count. If open count was zero, starts polling
     * the device state. Depending on the device sharing of the device type, it will be performed during
     * enable (shareable devices) or during claim (exclusive use devices).
     * @param props  Property set of device to be opened.
     * @return The new open count.
     */
    int startPolling(JposCommonProperties props) {
        synchronized (OpenCount) {
            if (OpenCount[0] == 0) {
                ToBeFinished = false;
                PollWaiter = new SyncObject();
                StartPollingWaiter = props;
                (StateWatcher = new Thread(this)).start();
                StateWatcher.setName("StatusUpdater");
                OpenCount[0] = 1;
                props.attachWaiter();
                props.waitWaiter(MaxRetry * (RequestTimeout + AckTimeout));
                props.releaseWaiter();
            }
            else
                OpenCount[0] = OpenCount[0] + 1;
            return OpenCount[0];
        }
    }

    private JposCommonProperties StartPollingWaiter = null;

    /**
     * Major part of device release operation. Decrements open count. If open count becomes zero, stops atatus
     * polling. This method is the inverse operation to startPolling().
     * @return The new open count.
     */
    int stopPolling() {
        synchronized(OpenCount) {
            if (OpenCount[0] == 1) {
                ToBeFinished = true;
                PollWaiter.signal();
                while (true) {
                    try {
                        StateWatcher.join();
                        break;
                    } catch (InterruptedException e) {}
                }
                StartPollingWaiter = null;
                closePort();
            }
            if (OpenCount[0] > 0)
                OpenCount[0] = OpenCount[0] - 1;
            return OpenCount[0];
        }
    }

    /**
     * Execute one or more printer specific commands. The given object may be an array of String, a 2-dimensional array
     * of arrays of String on a non-zero length array of 2-dimensional arrays of arrays of String.<br>
     * In the first case, the first <i>data</i> array consists of the components of one command. On return, the array
     * components will be overwritten by the components of the command response and the number of response components
     * will be returned. If the array length is lower than the number of response components, the array length will be
     * returned and the remaining components will be lost.<br>
     * In the other cases, the first element of each 2-dimensional array contains the components of a printer specific
     * command. The second component will be filled with the corresponding response components and the number of
     * successfully called commands will be returned.
     * @param data  In the first case, number of string components. Otherise ignored.
     * @param obj   Data components, see description.
     * @return See description.
     * @throws JposException If obj does not match the specified needs.
     */
    int executeCommands(int data, Object obj) throws JposException {
        check(!(obj instanceof String[][][] || obj instanceof String[][] || obj instanceof String[]), JposConst.JPOS_E_ILLEGAL, "Unsupported object type for obj");
        check(obj instanceof String[] && data > ((String[])obj).length, JposConst.JPOS_E_ILLEGAL, "data[0] > obj.length");
        if (obj instanceof String[]) {
            String[] cmd = (String[])obj;
            String[] resp = sendrecv(Arrays.copyOf(cmd, data));
            if ((data = resp == null ? 0 : resp.length) > 0)
                System.arraycopy(resp, 0, cmd, 0, resp.length < cmd.length ? resp.length : cmd.length);
        }
        else if (obj instanceof String[][] && ((String[][])obj).length == 2) {
            String[][] arg = (String[][])obj;
            arg[1] = sendrecv(arg[0]);
            data = arg[1] == null || arg[1][0] == null || arg[1][0].length() < DRAWER + 2 || arg[1][0].charAt(0) != SUCCESS ? 0 : 1;
        }
        else if (obj instanceof String[][][]) {
            data = 0;
            for (String[][] arg : (String[][][])obj) {
                check(arg.length != 2, JposConst.JPOS_E_ILLEGAL, "One element stored in obj is not String[2][]");
                arg[1] = sendrecv(arg[0]);
                data += arg[1] == null || arg[1][0] == null || arg[1][0].length() < DRAWER + 2 || arg[1][0].charAt(0) != SUCCESS ? 0 : 1;
            }
        }
        else
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Unsupported format of obj");
        return data;
    }

    /**
     * Adds the components of a command to an array of 2-dimensional arrays of String arrays where each first component
     * contains the components of one command. Each second component contains a place holder for the corresponding
     * command response components.
     * @param commands  The array of command / response pairs for later execution.
     * @param command   The components of the command to be added
     * @return New array of command / response pairs, containing <i>command</i> as the latest command and an empty response.
     */
    String[][][] addCommand(String[][][] commands, String[] command) {
        int index = commands.length;
        commands = Arrays.copyOf(commands, index + 1);
        commands[index] = new String[][]{command, null};
        return commands;
    }

    @Override
    public CashDrawerProperties getCashDrawerProperties(int index) {
        return new CashDrawer(this);
    }

    @Override
    public LineDisplayProperties getLineDisplayProperties(int index) {
        return new LineDisplay(this);
    }

    @Override
    public FiscalPrinterProperties getFiscalPrinterProperties(int index) {
        return new FiscalPrinter(this);
    }

    @Override
    public ElectronicJournalProperties getElectronicJournalProperties(int index) {
        return new ElectronicJournal(this);
    }
}
