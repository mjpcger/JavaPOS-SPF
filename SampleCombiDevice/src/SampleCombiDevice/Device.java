/**
 * Copyright 2017 Martin Conrad
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

package SampleCombiDevice;

import jpos.*;
import jpos.config.JposEntry;
import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.cashdrawer.*;
import de.gmxhome.conrad.jpos.jpos_base.keylock.*;
import de.gmxhome.conrad.jpos.jpos_base.linedisplay.*;
import de.gmxhome.conrad.jpos.jpos_base.msr.*;
import de.gmxhome.conrad.jpos.jpos_base.poskeyboard.*;
import de.gmxhome.conrad.jpos.jpos_base.scanner.*;
import de.gmxhome.conrad.jpos.jpos_base.toneindicator.*;
import net.bplaced.conrad.log4jpos.Level;

import java.io.*;
import java.util.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties.ExclusiveAllowed;
import static de.gmxhome.conrad.jpos.jpos_base.UniqueIOProcessor.IOProcessorError;

/**
 * Base of a JposDevice based implementation of JavaPOS CashDrawer, Keylock, LineDisplay,MSR, POSKeyboard, Scanner and
 * ToneIndicator device service implementations for the sample device implemented in CombiSim.tcl.<br>
 * For a complete list of possible commands and responses, look at the comments at the beginning of the device simulator
 * script.
 * <p>Here a full list of all device specific properties that can be changed via jpos.xml:
 * <ul>
 *     <li>Baudrate: Baud rate of the communication device. Must be one of the baud rate constants specified in the
 *     SerialIOProcessor class. Default: 9600 (SerialIOProcessor.BAUDRATE_9600).
 *     <br>This property may only be set if the communication with the device shall be made via serial port.</li>
 *     <li>BinaryEKey: This property specifies whether the service shall convert electronic key values. If false,
 *     electronic key values will be passed to the application as received from the simulator: as a series of ASCII
 *     codes representing hexadecimal values ('0' - '9', 'A' - 'F'). If true, electronic key values will be converted
 *     to byte values before passing them to the application. Default: true.</li>
 *     <li>CharacterTimeout: Positive integer value, specifying the maximum delay between bytes that belong to the same
 *     frame. Default value: 10 milliseconds.</li>
 *     <li>ComPort: Operating system specific name of the serial communication port (e.g. RS232, Usb2Serial,
 *     Bluetooth...) or the TCP address to be used for
 *     communication with the device simulator. In case of RS232, names look typically like COM2 or /dev/ttyS1. In
 *     case of TCP, names are of the form IPv4:port, where IPv4 is the IP address of the device and port its TCP port.</li>
 *     <li>Databits: Number of data bits per data unit. Must be 7 or 8. Default: 8. It is strictly recommended to let
 *     this value unchanged.
 *     <br>This property may only be set if the communication with the device shall be made via serial port.</li>
 *     <li>LoggingType: Specifies the logging format used by the IO processor. Must be one of the logging type values
 *     specified in the UniqueIOProcessor class. Default: 1 (UniqueIOProcessor.LoggingTypeEscapeString).</li>
 *     <li>MaxRetry: Specifies the maximum number of retries. Should be &gt; 0 only for RS232 (real COM ports)
 *     where characters can become lost or corrupted on the communication line. Default: 2.</li>
 *     <li>OwnPort: Integer value between 0 and 65535 specifying the TCP port used for communication with the device
 *     simulator. Default: 0 (for random port number selected by operating system).
 *     <br>This property may only be set if the communication with the device shall be made via TCP.</li>
 *     <li>Parity: Parity of each data unit. Must be one of the parity constants specified in the
 *     SerialIOProcessor class. Default: 0 (SerialIOProcessor.PARITY_NONE).
 *     <br>This property may only be set if the communication with the device shall be made via serial port.</li>
 *     <li>PollDelay: Minimum time between status requests, in milliseconds. Status requests will be used to monitor the
 *     device state. Default: 50.</li>
 *     <li>RequestTimeout: Maximum time, in milliseconds, between sending a command to the simulator and getting the
 *     first byte of its response. Default: 500.</li>
 *     <li>Stopbits: Number of stop bits per data unit. Must be 1 or 2. Default: 2.
 *     <br>This property may only be set if the communication with the device shall be made via serial port.</li>
 * </ul>
 * <p>
 * The key values of the keyboard must be configured within the SampleCombiDevice.Device.properties file. It must
 * contain an entry of the form <b>Key</b><i>row</i><b>-</b><i>column</i><b>Value = </b><i>keyvalue</i> for every
 * supported key, where
 * <i>row</i> must be the two-digit key row (01 - 10),
 * <i>column</i> must be the two-digit key column (01 - 16) and
 * <i>keyvalue</i> must be the application-specific integer value that specifies that key.
 */
public class Device extends JposDevice implements Runnable{
    private UniqueIOProcessor OutStream;
    private Thread StateWatcher;
    private SyncObject WaitObj;
    private boolean ToBeFinished;
    private Properties PropertySet;

    /**
     * Status request command.
     */
    static byte[] CmdStatusRequest = {'S','R'};

    /**
     * Beep activation command.
     */
    static byte[] CmdBeepOn = {'B', '1'};

    /**
     * Beep deactivation command.
     */
    static byte[] CmdBeepOff = {'B', '0'};

    /**
     * Dummy character value, replacing first byte of response for commands which will not be responded.
     */
    static final byte NoResponse = 0;
    private static final byte RespFromDrawer = 'D';
    private static final int DrawerStatePos = 1;
    private static final int DrawerStateLen = 1;
    private static final byte DrawerOpenVal = 'O';
    private static final byte RespFromLock = 'L';
    private static final int LockLockPos = 1;
    private static final int LockLockLen = 1;
    private static final int DefaultLockPos = '-';
    private static final byte RespFromEKey = 'E';
    private static final int EKeyValuePos = 1;
    private static final int EKeyValueLen = 12;
    private static final byte[] DefaultEKeyPos = new byte[0];
    private static final byte RespFromKeyboard = 'B';
    private static final int KeybRowPos = 1;
    private static final int KeybColumnPos = 2;
    private static final int KeybPositionLen = 2;
    private static final byte RespFromMsr = 'M';
    private static final int MsrLenPos = 1;
    private static final int MsrLenLen = 3;
    private static final byte RespFromScanner = 'R';
    private static final int LabelPos = 1;
    private static final int LabelLen = 1;
    private static final int LabelUpcA = 'A';
    private static final int LabelUpcE = 'E';
    private static final int LabelEan = 'F';
    private static final int UpcALen = 12;
    private static final int UpcELen = 7;
    private static final int LabelEan8Flag = 'F';
    private static final int Ean8Len = 9;
    private static final int Ean13Len = 13;

    /**
     * First byte of response on code page change
     */
    static final byte RespFromDisplay = 'C';
    private static final int DisplayStatePos = 1;
    private static final int DisplayStateLen = 2;
    private static final int DisplayStateSuccess = '1';

    /**
     * First byte of response on status request
     */
    static final byte RespFromStatus = 'S';
    private static final byte StatusLen = 15;
    private static final byte StatusDrawerPos = 1;
    private static final byte StatusLockPos = 2;
    private static final byte StatusEKeyPos = 3;
    private static int LockIndex = 0;
    private static int EKeyIndex = 1;
    private int Baudrate = SerialIOProcessor.BAUDRATE_9600;
    private int Databits = SerialIOProcessor.DATABITS_8;
    private int Stopbits = SerialIOProcessor.STOPBITS_2;
    private int Parity = SerialIOProcessor.PARITY_NONE;
    private Integer OwnPort = null;
    private int LoggingType = UniqueIOProcessor.LoggingTypeEscapeString;
    private int RequestTimeout = 500;
    private int CharacterTimeout = 10;
    private int PollDelay = 50;

    /**
     * Jpos.xml property MaxRetry specifies the maximum number of retries. Should be &gt; 0 only for RS232 (real COM ports)
     * where characters can become lost or corrupted on the communication line.
     */
    int MaxRetry = 2;

    /**
     * Flag showing the driver is in I/O error state.
     */
    boolean InIOError = true;

    /**
     * Flag showing the current drawer state (true = drawer is open).
     */
    boolean DrawerIsOpen = false;

    /**
     * Flag showing the current device state (true = offline, false = online).
     */
    boolean DeviceIsOffline = true;

    private byte LockPosition = DefaultLockPos;
    private Map<Integer, Integer> LockMapping = new HashMap<Integer, Integer>();
    private byte[] EKeyValue = DefaultEKeyPos;
    private boolean BinaryEKey = true;

    /**
     * Contents of LineDisplay.
     */
    char[][] DisplayContents = new char[2][20];

    /**
     * Attributes of characters on LineDisplay.
     */
    char[][] DisplayAttributes = new char[2][20];

    /**
     * Attribute value for normal display output.
     */
    final char NormalChar = 'n';
    private int[][] KeyValueTable = new int[10][16];
    private int OpenCount = 0;
    private List<CommandHelper> Commands = new ArrayList<CommandHelper>();
    private long LastPollTick;
    private SyncObject StartWaiter = new SyncObject();

    private class CommandHelper {
        byte[] Command;
        byte Response;
        SyncObject Signalizer;

        /**
         * Constructor for synchronized processing
         * @param command Byte array that contains a command sequence.
         * @param responseType Response type specifier
         */
        CommandHelper(byte[] command, byte responseType) {
            Signalizer = null;
            Command = command;
            Response = responseType;
        }

        /**
         * Constructor for internal use within communication handler (without symchronization)
         */
        CommandHelper() {
            Command = Device.CmdStatusRequest;
            Response = 0;
            Signalizer = null;
        }
    }

    synchronized private void enterCommand(CommandHelper command) {
        command.Signalizer = new SyncObject();
        Commands.add(command);
    }

    synchronized private CommandHelper retrieveCommand() {
        if (Commands.size() > 0) {
            CommandHelper ret = Commands.get(0);
            Commands.remove(0);
            return ret;
        }
        return null;
    }

    /**
     * Constructor, Stores communication target. Communication target can be a COM port or a TCP
     * target. Valid COM port specifiers differ between operating systems, e.g. on Windows, COM1
     * can be a valid communication target while on Linux, /dev/ttyS0 might specify the same target.
     * Format of TCP targets is <i>IpAddress</i>:<i>Port</i>, e.g. 10.11.12.13:45678.
     * @param port COM port or TCP target.
     * @throws JposException if Communication target invalid.
     */
    public Device(String port) throws JposException {
        super(port);
        lineDisplayInit(1);
        cashDrawerInit(1);
        pOSKeyboardInit(1);
        keylockInit(2);
        mSRInit(1);
        scannerInit(1);
        toneIndicatorInit(1);
        ProcessProperties();
        PhysicalDeviceDescription = "Combined device simulator for virtual COM ports";
        PhysicalDeviceName = "Combined Device Simulator";
        CapPowerReporting = JposConst.JPOS_PR_ADVANCED;
        WaitObj = new SyncObject();
        // Set property defaults, can be overwritten in every jpos.xml entry
        for (char[] line : DisplayContents) {
            for (char c : line)
                c = ' ';
        }
        for (char[] line : DisplayAttributes) {
            for (char c : line)
                c = NormalChar;
        }
        for (int[] row : KeyValueTable) {
            for (int val : row)
                val = 0;    // 0: suppress key entry
        }
        int[] positions = new int[]{'-', '0', '1', '2', 'X', 'Z', 'P', 'T'};
        for (int i = 0; i < positions.length; i++) {
            LockMapping.put(positions[i], i);
        }
    }

    @Override
    public void checkProperties(JposEntry entry) throws JposException {
        super.checkProperties(entry);
        try {
            new TcpClientIOProcessor(this, ID);
            OwnPort = 0;
        } catch (JposException e) {}
        try {
            Object o;
            if ((o = entry.getPropertyValue("Baudrate")) != null) {
                if (OwnPort != null)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property: Baudrate");
                Baudrate = Integer.parseInt(o.toString());
            }
            if ((o = entry.getPropertyValue("Databits")) != null) {
                if (OwnPort != null)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property: Databits");
                Databits = Integer.parseInt(o.toString());
            }
            if ((o = entry.getPropertyValue("Stopbits")) != null) {
                if (OwnPort != null)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property: Stopbits");
                Stopbits = Integer.parseInt(o.toString());
            }
            if ((o = entry.getPropertyValue("Parity")) != null) {
                if (OwnPort != null)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property: Parity");
                Parity = Integer.parseInt(o.toString());
            }
            if ((o = entry.getPropertyValue("OwnPort")) != null) {
                if (OwnPort == null)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property: OwnPort");
                int port = Integer.parseInt(o.toString());
                if (port < 0 || port > 0xffff)
                    throw new IOException("Invalid TCP port: " + o.toString());
                OwnPort = port;
            }
            if ((o = entry.getPropertyValue("LoggingType")) != null) {
                int type = Integer.parseInt(o.toString());
                switch (type) {
                    default:
                        throw new IOException("Unsupported logging type: " + o.toString());
                    case UniqueIOProcessor.LoggingTypeEscapeString:
                    case UniqueIOProcessor.LoggingTypeHexString:
                    case UniqueIOProcessor.LoggingTypeNoLogging:
                        LoggingType = type;
                }
            }
            if ((o = entry.getPropertyValue("RequestTimeout")) != null)
                RequestTimeout = Integer.parseInt(o.toString());
            if ((o = entry.getPropertyValue("CharacterTimeout")) != null)
                CharacterTimeout = Integer.parseInt(o.toString());
            if ((o = entry.getPropertyValue("PollDelay")) != null)
                PollDelay = Integer.parseInt(o.toString());
            if ((o = entry.getPropertyValue("MaxRetry")) != null)
                MaxRetry = Integer.parseInt(o.toString());
            if ((o = entry.getPropertyValue("BinaryEKey")) != null) {
                BinaryEKey = Boolean.parseBoolean(o.toString());
            }
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property", e);
        }
    }

    @Override
    public void changeDefaults(LineDisplayProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "Display service for combined device simulator";
        props.CapCharacterSet = LineDisplayConst.DISP_CCS_UNICODE;
        props.CharacterSetDef = LineDisplayConst.DISP_CS_UNICODE;
        props.CharacterSetList = "437,997,998,1252";
        props.CapBlink = LineDisplayConst.DISP_CB_BLINKEACH;
        props.CapReverse = LineDisplayConst.DISP_CR_REVERSEEACH;
        props.CapICharWait = true;
    }

    @Override
    public void changeDefaults(CashDrawerProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "Drawer service for combined device simulator";
    }

    @Override
    public void changeDefaults(POSKeyboardProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "Keyboard service for combined device simulator";
    }

    @Override
    public void changeDefaults(KeylockProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "Keylock service for combined device simulator";
        if (props.Index == EKeyIndex) {
            props.CapKeylockType = KeylockConst.LOCK_KT_ELECTRONIC;
        }
        else {
            props.PositionCount = 7;
        }
    }

    @Override
    public void changeDefaults(MSRProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "MSR service for combined device simulator";
    }

    @Override
    public void changeDefaults(ScannerProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "Scanner service for combined device simulator";
    }

    @Override
    public void changeDefaults(ToneIndicatorProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "Tone indicator service for combined device simulator";
    }

    /**
     * Reads properties from property file and stores property values in corresponding class variables
     * @throws JposException if <i>classname</i>.properties does not exist.
     */
    private void ProcessProperties() throws JposException {
        PropertySet = new Properties();
        File propertyFile = new File(getClass().getName() + ".properties");
        if (propertyFile.exists()) {
            try (BufferedInputStream istream = new BufferedInputStream(new FileInputStream(propertyFile))) {
                PropertySet.load(istream);
            } catch (Exception e) {
                throw new JposException(JposConst.JPOS_E_ILLEGAL, "Property file missing", e);
            }
            KeyValueTable[0][0] = Integer.parseInt(PropertySet.getProperty("Key01-01Value", "0"));
            KeyValueTable[0][1] = Integer.parseInt(PropertySet.getProperty("Key01-02Value", "0"));
            KeyValueTable[0][2] = Integer.parseInt(PropertySet.getProperty("Key01-03Value", "0"));
            KeyValueTable[0][3] = Integer.parseInt(PropertySet.getProperty("Key01-04Value", "0"));
            KeyValueTable[0][4] = Integer.parseInt(PropertySet.getProperty("Key01-05Value", "0"));
            KeyValueTable[0][5] = Integer.parseInt(PropertySet.getProperty("Key01-06Value", "0"));
            KeyValueTable[0][6] = Integer.parseInt(PropertySet.getProperty("Key01-07Value", "0"));
            KeyValueTable[0][7] = Integer.parseInt(PropertySet.getProperty("Key01-08Value", "0"));
            KeyValueTable[0][8] = Integer.parseInt(PropertySet.getProperty("Key01-09Value", "0"));
            KeyValueTable[0][9] = Integer.parseInt(PropertySet.getProperty("Key01-10Value", "0"));
            KeyValueTable[0][10] = Integer.parseInt(PropertySet.getProperty("Key01-11Value", "0"));
            KeyValueTable[0][11] = Integer.parseInt(PropertySet.getProperty("Key01-12Value", "0"));
            KeyValueTable[0][12] = Integer.parseInt(PropertySet.getProperty("Key01-13Value", "0"));
            KeyValueTable[0][13] = Integer.parseInt(PropertySet.getProperty("Key01-14Value", "0"));
            KeyValueTable[0][14] = Integer.parseInt(PropertySet.getProperty("Key01-15Value", "0"));
            KeyValueTable[0][15] = Integer.parseInt(PropertySet.getProperty("Key01-16Value", "0"));
            KeyValueTable[1][0] = Integer.parseInt(PropertySet.getProperty("Key02-01Value", "0"));
            KeyValueTable[1][1] = Integer.parseInt(PropertySet.getProperty("Key02-02Value", "0"));
            KeyValueTable[1][2] = Integer.parseInt(PropertySet.getProperty("Key02-03Value", "0"));
            KeyValueTable[1][3] = Integer.parseInt(PropertySet.getProperty("Key02-04Value", "0"));
            KeyValueTable[1][4] = Integer.parseInt(PropertySet.getProperty("Key02-05Value", "0"));
            KeyValueTable[1][5] = Integer.parseInt(PropertySet.getProperty("Key02-06Value", "0"));
            KeyValueTable[1][6] = Integer.parseInt(PropertySet.getProperty("Key02-07Value", "0"));
            KeyValueTable[1][7] = Integer.parseInt(PropertySet.getProperty("Key02-08Value", "0"));
            KeyValueTable[1][8] = Integer.parseInt(PropertySet.getProperty("Key02-09Value", "0"));
            KeyValueTable[1][9] = Integer.parseInt(PropertySet.getProperty("Key02-10Value", "0"));
            KeyValueTable[1][10] = Integer.parseInt(PropertySet.getProperty("Key02-11Value", "0"));
            KeyValueTable[1][11] = Integer.parseInt(PropertySet.getProperty("Key02-12Value", "0"));
            KeyValueTable[1][12] = Integer.parseInt(PropertySet.getProperty("Key02-13Value", "0"));
            KeyValueTable[1][13] = Integer.parseInt(PropertySet.getProperty("Key02-14Value", "0"));
            KeyValueTable[1][14] = Integer.parseInt(PropertySet.getProperty("Key02-15Value", "0"));
            KeyValueTable[1][15] = Integer.parseInt(PropertySet.getProperty("Key02-16Value", "0"));
            KeyValueTable[2][0] = Integer.parseInt(PropertySet.getProperty("Key03-01Value", "0"));
            KeyValueTable[2][1] = Integer.parseInt(PropertySet.getProperty("Key03-02Value", "0"));
            KeyValueTable[2][2] = Integer.parseInt(PropertySet.getProperty("Key03-03Value", "0"));
            KeyValueTable[2][3] = Integer.parseInt(PropertySet.getProperty("Key03-04Value", "0"));
            KeyValueTable[2][4] = Integer.parseInt(PropertySet.getProperty("Key03-05Value", "0"));
            KeyValueTable[2][5] = Integer.parseInt(PropertySet.getProperty("Key03-06Value", "0"));
            KeyValueTable[2][6] = Integer.parseInt(PropertySet.getProperty("Key03-07Value", "0"));
            KeyValueTable[2][7] = Integer.parseInt(PropertySet.getProperty("Key03-08Value", "0"));
            KeyValueTable[2][8] = Integer.parseInt(PropertySet.getProperty("Key03-09Value", "0"));
            KeyValueTable[2][9] = Integer.parseInt(PropertySet.getProperty("Key03-10Value", "0"));
            KeyValueTable[2][10] = Integer.parseInt(PropertySet.getProperty("Key03-11Value", "0"));
            KeyValueTable[2][11] = Integer.parseInt(PropertySet.getProperty("Key03-12Value", "0"));
            KeyValueTable[2][12] = Integer.parseInt(PropertySet.getProperty("Key03-13Value", "0"));
            KeyValueTable[2][13] = Integer.parseInt(PropertySet.getProperty("Key03-14Value", "0"));
            KeyValueTable[2][14] = Integer.parseInt(PropertySet.getProperty("Key03-15Value", "0"));
            KeyValueTable[2][15] = Integer.parseInt(PropertySet.getProperty("Key03-16Value", "0"));
            KeyValueTable[3][0] = Integer.parseInt(PropertySet.getProperty("Key04-01Value", "0"));
            KeyValueTable[3][1] = Integer.parseInt(PropertySet.getProperty("Key04-02Value", "0"));
            KeyValueTable[3][2] = Integer.parseInt(PropertySet.getProperty("Key04-03Value", "0"));
            KeyValueTable[3][3] = Integer.parseInt(PropertySet.getProperty("Key04-04Value", "0"));
            KeyValueTable[3][4] = Integer.parseInt(PropertySet.getProperty("Key04-05Value", "0"));
            KeyValueTable[3][5] = Integer.parseInt(PropertySet.getProperty("Key04-06Value", "0"));
            KeyValueTable[3][6] = Integer.parseInt(PropertySet.getProperty("Key04-07Value", "0"));
            KeyValueTable[3][7] = Integer.parseInt(PropertySet.getProperty("Key04-08Value", "0"));
            KeyValueTable[3][8] = Integer.parseInt(PropertySet.getProperty("Key04-09Value", "0"));
            KeyValueTable[3][9] = Integer.parseInt(PropertySet.getProperty("Key04-10Value", "0"));
            KeyValueTable[3][10] = Integer.parseInt(PropertySet.getProperty("Key04-11Value", "0"));
            KeyValueTable[3][11] = Integer.parseInt(PropertySet.getProperty("Key04-12Value", "0"));
            KeyValueTable[3][12] = Integer.parseInt(PropertySet.getProperty("Key04-13Value", "0"));
            KeyValueTable[3][13] = Integer.parseInt(PropertySet.getProperty("Key04-14Value", "0"));
            KeyValueTable[3][14] = Integer.parseInt(PropertySet.getProperty("Key04-15Value", "0"));
            KeyValueTable[3][15] = Integer.parseInt(PropertySet.getProperty("Key04-16Value", "0"));
            KeyValueTable[4][0] = Integer.parseInt(PropertySet.getProperty("Key05-01Value", "0"));
            KeyValueTable[4][1] = Integer.parseInt(PropertySet.getProperty("Key05-02Value", "0"));
            KeyValueTable[4][2] = Integer.parseInt(PropertySet.getProperty("Key05-03Value", "0"));
            KeyValueTable[4][3] = Integer.parseInt(PropertySet.getProperty("Key05-04Value", "0"));
            KeyValueTable[4][4] = Integer.parseInt(PropertySet.getProperty("Key05-05Value", "0"));
            KeyValueTable[4][5] = Integer.parseInt(PropertySet.getProperty("Key05-06Value", "0"));
            KeyValueTable[4][6] = Integer.parseInt(PropertySet.getProperty("Key05-07Value", "0"));
            KeyValueTable[4][7] = Integer.parseInt(PropertySet.getProperty("Key05-08Value", "0"));
            KeyValueTable[4][8] = Integer.parseInt(PropertySet.getProperty("Key05-09Value", "0"));
            KeyValueTable[4][9] = Integer.parseInt(PropertySet.getProperty("Key05-10Value", "0"));
            KeyValueTable[4][10] = Integer.parseInt(PropertySet.getProperty("Key05-11Value", "0"));
            KeyValueTable[4][11] = Integer.parseInt(PropertySet.getProperty("Key05-12Value", "0"));
            KeyValueTable[4][12] = Integer.parseInt(PropertySet.getProperty("Key05-13Value", "0"));
            KeyValueTable[4][13] = Integer.parseInt(PropertySet.getProperty("Key05-14Value", "0"));
            KeyValueTable[4][14] = Integer.parseInt(PropertySet.getProperty("Key05-15Value", "0"));
            KeyValueTable[4][15] = Integer.parseInt(PropertySet.getProperty("Key05-16Value", "0"));
            KeyValueTable[5][0] = Integer.parseInt(PropertySet.getProperty("Key06-01Value", "0"));
            KeyValueTable[5][1] = Integer.parseInt(PropertySet.getProperty("Key06-02Value", "0"));
            KeyValueTable[5][2] = Integer.parseInt(PropertySet.getProperty("Key06-03Value", "0"));
            KeyValueTable[5][3] = Integer.parseInt(PropertySet.getProperty("Key06-04Value", "0"));
            KeyValueTable[5][4] = Integer.parseInt(PropertySet.getProperty("Key06-05Value", "0"));
            KeyValueTable[5][5] = Integer.parseInt(PropertySet.getProperty("Key06-06Value", "0"));
            KeyValueTable[5][6] = Integer.parseInt(PropertySet.getProperty("Key06-07Value", "0"));
            KeyValueTable[5][7] = Integer.parseInt(PropertySet.getProperty("Key06-08Value", "0"));
            KeyValueTable[5][8] = Integer.parseInt(PropertySet.getProperty("Key06-09Value", "0"));
            KeyValueTable[5][9] = Integer.parseInt(PropertySet.getProperty("Key06-10Value", "0"));
            KeyValueTable[5][10] = Integer.parseInt(PropertySet.getProperty("Key06-11Value", "0"));
            KeyValueTable[5][11] = Integer.parseInt(PropertySet.getProperty("Key06-12Value", "0"));
            KeyValueTable[5][12] = Integer.parseInt(PropertySet.getProperty("Key06-13Value", "0"));
            KeyValueTable[5][13] = Integer.parseInt(PropertySet.getProperty("Key06-14Value", "0"));
            KeyValueTable[5][14] = Integer.parseInt(PropertySet.getProperty("Key06-15Value", "0"));
            KeyValueTable[5][15] = Integer.parseInt(PropertySet.getProperty("Key06-16Value", "0"));
            KeyValueTable[6][0] = Integer.parseInt(PropertySet.getProperty("Key07-01Value", "0"));
            KeyValueTable[6][1] = Integer.parseInt(PropertySet.getProperty("Key07-02Value", "0"));
            KeyValueTable[6][2] = Integer.parseInt(PropertySet.getProperty("Key07-03Value", "0"));
            KeyValueTable[6][3] = Integer.parseInt(PropertySet.getProperty("Key07-04Value", "0"));
            KeyValueTable[6][4] = Integer.parseInt(PropertySet.getProperty("Key07-05Value", "0"));
            KeyValueTable[6][5] = Integer.parseInt(PropertySet.getProperty("Key07-06Value", "0"));
            KeyValueTable[6][6] = Integer.parseInt(PropertySet.getProperty("Key07-07Value", "0"));
            KeyValueTable[6][7] = Integer.parseInt(PropertySet.getProperty("Key07-08Value", "0"));
            KeyValueTable[6][8] = Integer.parseInt(PropertySet.getProperty("Key07-09Value", "0"));
            KeyValueTable[6][9] = Integer.parseInt(PropertySet.getProperty("Key07-10Value", "0"));
            KeyValueTable[6][10] = Integer.parseInt(PropertySet.getProperty("Key07-11Value", "0"));
            KeyValueTable[6][11] = Integer.parseInt(PropertySet.getProperty("Key07-12Value", "0"));
            KeyValueTable[6][12] = Integer.parseInt(PropertySet.getProperty("Key07-13Value", "0"));
            KeyValueTable[6][13] = Integer.parseInt(PropertySet.getProperty("Key07-14Value", "0"));
            KeyValueTable[6][14] = Integer.parseInt(PropertySet.getProperty("Key07-15Value", "0"));
            KeyValueTable[6][15] = Integer.parseInt(PropertySet.getProperty("Key07-16Value", "0"));
            KeyValueTable[7][0] = Integer.parseInt(PropertySet.getProperty("Key08-01Value", "0"));
            KeyValueTable[7][1] = Integer.parseInt(PropertySet.getProperty("Key08-02Value", "0"));
            KeyValueTable[7][2] = Integer.parseInt(PropertySet.getProperty("Key08-03Value", "0"));
            KeyValueTable[7][3] = Integer.parseInt(PropertySet.getProperty("Key08-04Value", "0"));
            KeyValueTable[7][4] = Integer.parseInt(PropertySet.getProperty("Key08-05Value", "0"));
            KeyValueTable[7][5] = Integer.parseInt(PropertySet.getProperty("Key08-06Value", "0"));
            KeyValueTable[7][6] = Integer.parseInt(PropertySet.getProperty("Key08-07Value", "0"));
            KeyValueTable[7][7] = Integer.parseInt(PropertySet.getProperty("Key08-08Value", "0"));
            KeyValueTable[7][8] = Integer.parseInt(PropertySet.getProperty("Key08-09Value", "0"));
            KeyValueTable[7][9] = Integer.parseInt(PropertySet.getProperty("Key08-10Value", "0"));
            KeyValueTable[7][10] = Integer.parseInt(PropertySet.getProperty("Key08-11Value", "0"));
            KeyValueTable[7][11] = Integer.parseInt(PropertySet.getProperty("Key08-12Value", "0"));
            KeyValueTable[7][12] = Integer.parseInt(PropertySet.getProperty("Key08-13Value", "0"));
            KeyValueTable[7][13] = Integer.parseInt(PropertySet.getProperty("Key08-14Value", "0"));
            KeyValueTable[7][14] = Integer.parseInt(PropertySet.getProperty("Key08-15Value", "0"));
            KeyValueTable[7][15] = Integer.parseInt(PropertySet.getProperty("Key08-16Value", "0"));
            KeyValueTable[8][0] = Integer.parseInt(PropertySet.getProperty("Key09-01Value", "0"));
            KeyValueTable[8][1] = Integer.parseInt(PropertySet.getProperty("Key09-02Value", "0"));
            KeyValueTable[8][2] = Integer.parseInt(PropertySet.getProperty("Key09-03Value", "0"));
            KeyValueTable[8][3] = Integer.parseInt(PropertySet.getProperty("Key09-04Value", "0"));
            KeyValueTable[8][4] = Integer.parseInt(PropertySet.getProperty("Key09-05Value", "0"));
            KeyValueTable[8][5] = Integer.parseInt(PropertySet.getProperty("Key09-06Value", "0"));
            KeyValueTable[8][6] = Integer.parseInt(PropertySet.getProperty("Key09-07Value", "0"));
            KeyValueTable[8][7] = Integer.parseInt(PropertySet.getProperty("Key09-08Value", "0"));
            KeyValueTable[8][8] = Integer.parseInt(PropertySet.getProperty("Key09-09Value", "0"));
            KeyValueTable[8][9] = Integer.parseInt(PropertySet.getProperty("Key09-10Value", "0"));
            KeyValueTable[8][10] = Integer.parseInt(PropertySet.getProperty("Key09-11Value", "0"));
            KeyValueTable[8][11] = Integer.parseInt(PropertySet.getProperty("Key09-12Value", "0"));
            KeyValueTable[8][12] = Integer.parseInt(PropertySet.getProperty("Key09-13Value", "0"));
            KeyValueTable[8][13] = Integer.parseInt(PropertySet.getProperty("Key09-14Value", "0"));
            KeyValueTable[8][14] = Integer.parseInt(PropertySet.getProperty("Key09-15Value", "0"));
            KeyValueTable[8][15] = Integer.parseInt(PropertySet.getProperty("Key09-16Value", "0"));
            KeyValueTable[9][0] = Integer.parseInt(PropertySet.getProperty("Key10-01Value", "0"));
            KeyValueTable[9][1] = Integer.parseInt(PropertySet.getProperty("Key10-02Value", "0"));
            KeyValueTable[9][2] = Integer.parseInt(PropertySet.getProperty("Key10-03Value", "0"));
            KeyValueTable[9][3] = Integer.parseInt(PropertySet.getProperty("Key10-04Value", "0"));
            KeyValueTable[9][4] = Integer.parseInt(PropertySet.getProperty("Key10-05Value", "0"));
            KeyValueTable[9][5] = Integer.parseInt(PropertySet.getProperty("Key10-06Value", "0"));
            KeyValueTable[9][6] = Integer.parseInt(PropertySet.getProperty("Key10-07Value", "0"));
            KeyValueTable[9][7] = Integer.parseInt(PropertySet.getProperty("Key10-08Value", "0"));
            KeyValueTable[9][8] = Integer.parseInt(PropertySet.getProperty("Key10-09Value", "0"));
            KeyValueTable[9][9] = Integer.parseInt(PropertySet.getProperty("Key10-10Value", "0"));
            KeyValueTable[9][10] = Integer.parseInt(PropertySet.getProperty("Key10-11Value", "0"));
            KeyValueTable[9][11] = Integer.parseInt(PropertySet.getProperty("Key10-12Value", "0"));
            KeyValueTable[9][12] = Integer.parseInt(PropertySet.getProperty("Key10-13Value", "0"));
            KeyValueTable[9][13] = Integer.parseInt(PropertySet.getProperty("Key10-14Value", "0"));
            KeyValueTable[9][14] = Integer.parseInt(PropertySet.getProperty("Key10-15Value", "0"));
            KeyValueTable[9][15] = Integer.parseInt(PropertySet.getProperty("Key10-16Value", "0"));
        }
    }

    /**
     * Thread main, used for status check loop while device is opened.
     */
    public void run() {
        CommandHelper[] cmd = new CommandHelper[1];
        cmd[0] = null;
        LastPollTick = System.currentTimeMillis() - PollDelay;
        SyncObject startWaiter = StartWaiter;
        int[] retry = new int[]{0};
        while (!ToBeFinished) {
            try {
                Object e;

                if (InIOError) {
                    if (handleIOErrorState(true) != null)
                        continue;
                }
                if ((e = readData()) != null) {
                    if (startWaiter != null) {
                        startWaiter.signal();
                        startWaiter = null;
                    }
                    if (e instanceof JposException) {
                        if (((JposException) e).getErrorCodeExtended() != IOProcessorError)
                        abortAllCommands(cmd);
                        handleIOExceptionState();
                        continue;
                    }
                    if ((e instanceof Exception || e == null) && !DeviceIsOffline) {
                        if (e != null)
                            ((Exception) e).printStackTrace();
                        DeviceIsOffline = true;
                        abortAllCommands(cmd);
                        handlePowerStateEvent(JposConst.JPOS_SUE_POWER_OFFLINE);
                    }
                    if (e instanceof byte[]) {
                        handleNoErrorState((byte[]) e, retry, cmd);
                    }
                }
            } catch (Exception e) {
            }
        }
        closePort(InIOError = true);
    }

    private void handleNoErrorState(byte[] e, int[] retry, CommandHelper[] cmd) throws Exception {
        byte[] head = e;

        if (cmd[0] != null && cmd[0].Response != NoResponse) {
            if (cmd[0].Response == head[0]) {
                signalCommandOK(cmd);
            }
            else
                return;
        }
        if (head[0] == 0) {
            if (retry[0]++ >= MaxRetry) {
                processMaxRetryReached(retry);
            }
            else {
                OutStream.write(cmd[0] == null ? CmdStatusRequest : cmd[0].Command);
                return;
            }
        }
        else if (DeviceIsOffline) {
            DeviceIsOffline = false;
            handlePowerStateEvent(JposConst.JPOS_SUE_POWER_ONLINE);
            retry[0] = 0;
        }
        if (DeviceIsOffline) {
            abortAllCommands(cmd);
        }
        if (OutStream.available() == 0) {
            processResponselessCommands(cmd);
            if (cmd[0] == null) {
                performPollDelay();
                processResponselessCommands(cmd);
            }
            OutStream.write(cmd[0] == null ? CmdStatusRequest : cmd[0].Command);
        }
    }

    private void processMaxRetryReached(int[] retry) {
        if (!DeviceIsOffline) {
            DeviceIsOffline = true;
            handlePowerStateEvent(JposConst.JPOS_SUE_POWER_OFFLINE);
        }
        retry[0] = 0;
    }

    private void signalCommandOK(CommandHelper[] cmd) {
        if (cmd[0].Signalizer != null) {
            cmd[0].Signalizer.signal();
            cmd[0] = null;
        }
    }

    private void performPollDelay() {
        long currentTick = System.currentTimeMillis();
        if (currentTick < LastPollTick + PollDelay) {
            WaitObj.suspend(LastPollTick + PollDelay - currentTick);
            LastPollTick += PollDelay;
        }
        else
            LastPollTick = currentTick;
    }

    private void processResponselessCommands(CommandHelper[] cmd) throws JposException {
        while ((cmd[0] = retrieveCommand()) != null && cmd[0].Response == NoResponse) {
            OutStream.write(cmd[0].Command);
            if (cmd[0].Signalizer != null)
                cmd[0].Signalizer.signal();
        }
    }

    private void abortAllCommands(CommandHelper[] cmd) {
        if (cmd[0] == null)
            cmd[0] = retrieveCommand();
        while (cmd[0] != null) {
            cmd[0].Response = 0;
            if (cmd[0].Signalizer != null)
                cmd[0].Signalizer.signal();
            cmd[0] = retrieveCommand();
        }
    }

    private void handleIOExceptionState() {
        InIOError = true;
        handlePowerStateEvent(JposConst.JPOS_SUE_POWER_OFF);
        closePort(false);
        try {
            Thread.sleep(PollDelay);
        } catch (InterruptedException e1) {
        }
        return;
    }

    /**
     * Handle IO error
     * @param requestState If true,
     * @return Exception in case or error condition, null otherwise.
     */
    private Exception handleIOErrorState(boolean requestState) {
        Exception e;
        e = initPort();
        if (e == null) {
            if (requestState) {
                try {
                    OutStream.write(CmdStatusRequest);
                    DeviceIsOffline = InIOError = CpChanged = false;
                    handlePowerStateEvent(JposConst.JPOS_SUE_POWER_ONLINE);
                } catch (JposException e1) {
                    closePort(false);
                    return e1;
                }
            }
            else {
                DeviceIsOffline = InIOError = CpChanged = false;
                handlePowerStateEvent(JposConst.JPOS_SUE_POWER_ONLINE);
            }
        }
        else {
            try {
                Thread.sleep(RequestTimeout);
            } catch (InterruptedException e1) {}
            return e;
        }
        return null;
    }

    /**
     * Read and process a frame
     * @return Exception, if occurred, byte[1] containing 1st byte of frame on success, null framing error
     */
    private Object readData() {
        byte[] next = new byte[250];
        int offset = 0;
        byte[] head = new byte[1];
        try {
            OutStream.setTimeout(RequestTimeout);
            byte[] charIn = OutStream.read(1);
            Exception e;
            if (charIn.length == 1) {
                next[0] = charIn[0];
                offset = charIn.length;
                switch (head[0] = next[0]) {
                    case RespFromDisplay:
                        Object e0 = respFromDisplay(next, offset, head);
                        if (e0 != null) return e0;
                        break;
                    case RespFromDrawer:
                        Object e1 = respFromDrawer(next, offset, head);
                        if (e1 != null) return e1;
                        break;
                    case RespFromEKey:
                        Object e2 = respFromEKey(next, offset, head);
                        if (e2 != null) return e2;
                        break;
                    case RespFromLock:
                        Object e3 = respFromLock(next, offset, head);
                        if (e3 != null) return e3;
                        break;
                    case RespFromKeyboard:
                        Object e4 = respFromKeyboard(next, offset, head);
                        if (e4 != null) return e4;
                        break;
                    case RespFromMsr:
                        Object e5 = respFromMsr(next, offset, head);
                        if (e5 != null) return e5;
                        break;
                    case RespFromScanner:
                        if (respFromScanner(next, offset, head)) return head;
                        break;
                    case RespFromStatus:
                        Object e6 = respFromStatus(next, offset, head);
                        if (e6 != null) return e6;
                }
            }
            else {
                head[0] = 0;
                return head;
            }
        }
        catch (Exception e) {
            return e;
        }
        return null;
    }

    /**
     * Flag to check whether the code üage has been modified.
     */
    boolean CpChanged = false;

    private Object respFromDisplay(byte[] next, int offset, byte[] head) throws JposException {
        if (readData(next, offset, DisplayStateLen)) {
            CpChanged = next[DisplayStatePos] == DisplayStateSuccess;
            return head;
        }
        return null;
    }

    private Object respFromStatus(byte[] next, int offset, byte[] head) throws JposException {
        if (readData(next, offset, StatusLen)) {
            Exception e = handleDrawerChange(next[StatusDrawerPos] == DrawerOpenVal);
            if (e == null)
                e = handleLockChange(next[StatusLockPos]);
            if (e == null)
                e =  handleEKeyChange(Arrays.copyOfRange(next, StatusEKeyPos, EKeyValueLen + StatusEKeyPos));
            return e == null ? head : e;
        }
        return null;
    }

    private boolean respFromScanner(byte[] next, int offset, byte[] head) throws JposException {
        if (readData(next, offset, LabelPos + LabelLen)) {
            int targetOffset = offset = LabelPos + LabelLen;
            byte[] label;
            byte[] data;
            int labelType = ScannerConst.SCAN_SDT_UNKNOWN;
            int labelOffset = LabelPos + LabelLen;
            switch (next[LabelPos]) {
                case LabelUpcA:
                    targetOffset += UpcALen;
                    labelType = ScannerConst.SCAN_SDT_UPCA;
                    break;
                case LabelUpcE:
                    targetOffset += UpcELen;
                    labelType = ScannerConst.SCAN_SDT_UPCE;
                    break;
                case LabelEan:
                    if (readData(next, offset, offset + 1)) {
                        offset++;
                        if (next[LabelPos + LabelLen] == LabelEan8Flag) {
                            targetOffset += Ean8Len;
                            labelType = ScannerConst.SCAN_SDT_EAN8;
                            labelOffset++;
                        }
                        else {
                            targetOffset += Ean13Len;
                            labelType = ScannerConst.SCAN_SDT_EAN13;
                        }
                    }
            }
            if (targetOffset > offset && readData(next, offset, targetOffset)) {
                data = Arrays.copyOfRange(next, LabelPos, targetOffset);
                label = Arrays.copyOfRange(next, labelOffset, targetOffset);
                ScannerProperties claimer = (ScannerProperties)getClaimingInstance(ClaimedScanner, 0);
                if (claimer != null) {
                    if (claimer.DecodeData)
                        handleEvent(new ScannerDataEvent(claimer.EventSource, 0, data, label, labelType));
                    else
                        handleEvent(new ScannerDataEvent(claimer.EventSource, 0, data, new byte[0], ScannerConst.SCAN_SDT_UNKNOWN));
                }
                return true;
            }
        }
        return false;
    }

    private Object respFromMsr(byte[] next, int offset, byte[] head) throws JposException {
        MSRProperties props;
        props = (MSRProperties)getClaimingInstance(ClaimedMSR, 0);
        if (readData(next, offset, MsrLenPos + MsrLenLen)) {
            int trackTotal = Integer.parseInt(new String(Arrays.copyOfRange(next, MsrLenPos, MsrLenPos + MsrLenLen)));
            byte[][] tracks = new byte[][]{new byte[0], new byte[0], new byte[0], new byte[3]};
            boolean success = readData(next, 0, trackTotal);
            if (props != null) {
                if (success && next[0] == '1') {
                    Exception e = extractTracks(next, props, trackTotal, tracks);
                    if (e != null)
                        return e;
                }
            }
            return head;
        }
        return null;
    }

    private Object respFromKeyboard(byte[] next, int offset, byte[] head) throws JposException {
        if (readData(next, offset, KeybRowPos + KeybPositionLen)) {
            int row = next[KeybRowPos] - '0';
            int column = next[KeybColumnPos] - 'A';
            POSKeyboardProperties claimer = (POSKeyboardProperties)getClaimingInstance(ClaimedPOSKeyboard, 0);
            if (claimer != null && KeyValueTable[row][column] != 0) {
                try {
                    handleEvent(new POSKeyboardDataEvent(claimer.EventSource, 0, KeyValueTable[row][column], POSKeyboardConst.KBD_KET_KEYDOWN));
                } catch (JposException e) {
                    return e;
                }
            }
            return head;
        }
        return null;
    }

    private Object respFromLock(byte[] next, int offset, byte[] head) throws JposException {
        Exception e;
        if (readData(next, offset, LockLockPos + LockLockLen)) {
            e = handleLockChange(next[LockLockPos]);
            return e == null ? head : e;
        }
        return null;
    }

    private Object respFromEKey(byte[] next, int offset, byte[] head) throws JposException {
        Exception e;
        if (readData(next, offset, EKeyValuePos + EKeyValueLen)) {
            e = handleEKeyChange(Arrays.copyOfRange(next, EKeyValuePos, EKeyValueLen + EKeyValuePos));
            return e == null ? head : e;
        }
        return null;
    }

    private Object respFromDrawer(byte[] next, int offset, byte[] head) throws JposException {
        Exception e;
        if (readData(next, offset, DrawerStatePos + DrawerStateLen)) {
            e = handleDrawerChange(next[DrawerStatePos] == DrawerOpenVal);
            return e == null ? head : e;
        }
        return null;
    }

    private boolean readData(byte[] buffer, int offset, int targetOffset) throws JposException {
        byte[] data;
        OutStream.setTimeout(CharacterTimeout);
        do {
            data = OutStream.read(targetOffset - offset);
            if (data.length == 0)
                return offset == targetOffset;
            for (byte c : data) {
                buffer[offset++] = c;
            }
        } while (offset < targetOffset);
        return true;
    }

    private Exception extractTracks(byte[] next, MSRProperties props, int trackTotal, byte[][] tracks) {
        int start = extractTrack(0, (byte) '&', next, trackTotal, tracks, 1);
        if (start < trackTotal && next[start] == '2') {
            start = extractTrack(1, (byte) ';', next, trackTotal, tracks, ++start);
            if (start < trackTotal && next[start] == '3') {
                start = extractTrack(2, (byte) ';', next, trackTotal, tracks, ++start);
                if (props != null) {
                    try {
                        if (tracks[3][0] == JposConst.JPOS_SUCCESS && tracks[3][1] == JposConst.JPOS_SUCCESS && tracks[3][2] == JposConst.JPOS_SUCCESS && start == trackTotal) {
                            int status = (((tracks[2].length << 8) + tracks[1].length) << 8) + tracks[0].length;
                            handleEvent(new MSRDataEvent(props.EventSource, status, new TrackData(tracks)));
                        } else {
                            MSRErrorEvent ev;
                            if (props.ErrorReportingType == MSRConst.MSR_ERT_CARD || start != trackTotal)
                                ev = new MSRErrorEvent(props.EventSource, JposConst.JPOS_E_FAILURE, 0, new TrackData(tracks));
                            else {
                                int exterr = ((((tracks[3][2] & 0xff) << 8) + (tracks[3][1] & 0xff)) << 8) + (tracks[3][0] & 0xff);
                                ev = new MSRErrorEvent(props.EventSource, JposConst.JPOS_E_EXTENDED, exterr, new TrackData(tracks));
                            }
                            handleEvent(ev);
                        }
                    } catch(Exception e){
                        return e;
                    }
                }
            }
        }
        return null;
    }

    private int extractTrack(int index, byte startChar, byte[] next, int trackTotal, byte[][] tracks, int start) {
        int end = start + 1;
        tracks[3][index] = JposConst.JPOS_SUCCESS;
        if (start < trackTotal && next[start] == startChar) {
            while (end < trackTotal && next[end] != '?') {
                if(next[end] == startChar)
                    tracks[3][index] = (byte) MSRConst.JPOS_EMSR_START;
                end++;
            }
            if (end < trackTotal) {
                MSRProperties props = (MSRProperties)getClaimingInstance(ClaimedMSR, 0);
                int trackbit = new int[]{MSRConst.MSR_TR_1, MSRConst.MSR_TR_2, MSRConst.MSR_TR_3}[index];
                if (props != null && (trackbit & props.TracksToRead) != 0 && tracks[3][index] == JposConst.JPOS_SUCCESS) {
                    tracks[index] = Arrays.copyOfRange(next, start, end + 1);
                }
                return end + 1;
            }
            else {
                tracks[3][index] = (byte)MSRConst.JPOS_EMSR_END;
                return start - 1;
            }
        }
        return start;
    }

    private Exception handleDrawerChange(boolean newDrawerState) {
        if (newDrawerState != DrawerIsOpen) {
            DrawerIsOpen = newDrawerState;
            JposCommonProperties props = getPropertySetInstance(CashDrawers, 0, 0);
            if (props != null) {
                try {
                    handleEvent(new CashDrawerStatusUpdateEvent(props.EventSource, DrawerIsOpen ? CashDrawerConst.CASH_SUE_DRAWEROPEN : CashDrawerConst.CASH_SUE_DRAWERCLOSED));
                } catch (JposException e) {
                    return e;
                } finally {
                    signalStatusWaits(CashDrawers[0]);
                }
            }
        }
        return null;
    }

    private Exception handleEKeyChange(byte[] newEkeyValue) {
        if (BinaryEKey) {
            byte[] binaryEKey = new byte[newEkeyValue.length / 2];
            for (int i = 0; i < binaryEKey.length; i++) {
                binaryEKey[i] = (byte)("0123456789ABCDEF".indexOf(newEkeyValue[i + i] & 0xff) * 0x10
                        + (byte)"0123456789ABCDEF".indexOf(newEkeyValue[i + i + 1] & 0xff));
            }
            newEkeyValue = binaryEKey;
        }
        if (!Arrays.equals(newEkeyValue, EKeyValue)) {
            EKeyValue = newEkeyValue;
            JposCommonProperties props = getPropertySetInstance(Keylocks, EKeyIndex, 0);
            if (props != null) {
                try {
                    handleEvent(new KeylockStatusUpdateEvent(props.EventSource, KeylockConst.LOCK_KP_ELECTRONIC, EKeyValue));
                } catch (JposException e) {
                    return e;
                } finally {
                    signalStatusWaits(Keylocks[EKeyIndex]);
                }
            }
        }
        return null;
    }

    private Exception handleLockChange(byte newLockPos) {
        if (newLockPos != LockPosition) {
            LockPosition = newLockPos;
            JposCommonProperties props = getPropertySetInstance(Keylocks, LockIndex, 0);
            if (props != null) {
                try {
                    handleEvent(new KeylockStatusUpdateEvent(props.EventSource, LockMapping.get((int)LockPosition), new byte[0]));
                } catch (JposException e) {
                    return e;
                } finally {
                    signalStatusWaits(Keylocks[LockIndex]);
                }
            }
        }
        return null;
    }

    private Exception handlePowerStateEvent(int status) {
        try {
            JposCommonProperties props;
            if ((props = getClaimingInstance(ClaimedLineDisplay, 0)) != null)
                handleEvent(new JposStatusUpdateEvent(props.EventSource, status));
            if ((props = getPropertySetInstance(CashDrawers, 0, 0)) != null)
                handleEvent(new JposStatusUpdateEvent(props.EventSource, status));
            if ((props = getClaimingInstance(ClaimedPOSKeyboard, 0)) != null)
                handleEvent(new JposStatusUpdateEvent(props.EventSource, status));
            if ((props = getPropertySetInstance(Keylocks, 0, 0)) != null)
                handleEvent(new JposStatusUpdateEvent(props.EventSource, status));
            if ((props = getPropertySetInstance(Keylocks, 1, 0)) != null)
                handleEvent(new JposStatusUpdateEvent(props.EventSource, status));
            if ((props = getClaimingInstance(ClaimedMSR, 0)) != null)
                handleEvent(new JposStatusUpdateEvent(props.EventSource, status));
            if ((props = getClaimingInstance(ClaimedScanner, 0)) != null)
                handleEvent(new JposStatusUpdateEvent(props.EventSource, status));
            if ((props = getPropertySetInstance(ToneIndicators, 0, 0)) != null)
                handleEvent(new JposStatusUpdateEvent(props.EventSource, status));
        }
        catch (JposException e) {
            return e;
        } finally {
            if (status != JposConst.JPOS_SUE_POWER_ONLINE) {
                signalStatusWaits(CashDrawers[0]);
                signalStatusWaits(Keylocks[0]);
                signalStatusWaits(Keylocks[1]);
            }
        }
        return null;
    }

    /**
     * Method to perform any command
     * @param request Data to be sent
     * @param responseType type of response frame (first byte)
     * @return null on timeout, Byte(responseType) on success
     * @throws JposException in error case
     */
    protected Byte sendCommand(byte[] request, byte responseType) throws JposException {
        if (InIOError || DeviceIsOffline) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Device not available");
        }
        CommandHelper helper = new CommandHelper(request, responseType);
        enterCommand(helper);
        WaitObj.signal();
        helper.Signalizer.suspend(SyncObject.INFINITE);
        if (helper.Response != responseType)
            return null;
        return responseType;
    }

    /**
     * Closes the port
     * @param doFlush Specifies whether the output stream shall be flushed befor close.
     * @return In case of an IO error, the corresponding exception. Otherwise null
     */
    private Exception closePort(boolean doFlush) {
        Exception e = null;
        for (int i = 0; i < 2; i++) {
            try {
                switch (i) {
                    case 0:
                        if (doFlush)
                            OutStream.flush();
                        i++;
                    case 1:
                        OutStream.close();
                }
            } catch (Exception ee) {
                e = ee;
            }
        }
        OutStream = null;
        return e;
    }

    /**
     * Port initialization.
     * @return In case of initialization error, the exception. Otherwise null.
     */
    private Exception initPort() {
        try {
            if (OwnPort != null) {
                try {
                    OutStream = new TcpClientIOProcessor(this, ID);
                } catch (JposException e) {}
            }
            else
                OutStream = null;
            if (OwnPort == null && OutStream == null) {
                SerialIOProcessor ser;
                OutStream = ser = new SerialIOProcessor(this, ID);
                ser.setParameters(Baudrate, Databits, Stopbits, Parity);
            }
            else {
                TcpClientIOProcessor tcp = OutStream != null ? (TcpClientIOProcessor) OutStream : new TcpClientIOProcessor(this, ID);
                tcp.setParam(OwnPort);
                OutStream = tcp;
            }
            OutStream.setLoggingType(LoggingType);
            OutStream.open(false);
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    private synchronized int changeOpenCount(int value) {
        OpenCount += value;
        return OpenCount;
    }

    /**
     * Updates keyposition  or electronic key value, depending on the lock type: The first Keylock is a
     * central lock with positions 0, I, II, X, Z, P and T, the second Keylock is for electronic keys with
     * 48-bit values (all bits 0 means no key present).
     *
     * @param dev   Property set
     * @param enable True if DeviceEnabled will be set.
     * @throws JposException If current Keylock position is invalid.
     */
    void updateKeylockStates(JposCommonProperties dev, boolean enable) throws JposException {
        KeylockProperties props = (KeylockProperties) dev;
        if (enable) {
            if (dev.Index == EKeyIndex) {
                props.ElectronicKeyValue = EKeyValue;
                props.KeyPosition = 0;
            }
            else {
                Integer i = LockMapping.get((int)LockPosition);
                if(i != null)
                    props.KeyPosition = i;
                else
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Internal error: Cannot translate key position");
            }
        }
    }

    /**
     * Updates DrawerOpen property while setting DeviceEnabled.
     * @param dev   Property set
     * @param enable    True if DeviceEnabled will be set.
     */
    void updateDrawerStates(CashDrawerProperties dev, boolean enable) {
        if (enable) {
            dev.DrawerOpened = DrawerIsOpen;
        }
    }

    /**
     * Updates PowerState property if power notifications are enabled while setting DeviceEnabled to true.
     * @param dev       Property set
     * @param enable    True if DeviceEnabled will be set.
     */
    void updateCommonStates(JposCommonProperties dev, boolean enable) {
        if (enable) {
            if (dev.PowerNotify == JposConst.JPOS_PN_ENABLED) {
                dev.PowerState = InIOError ? JposConst.JPOS_PS_OFF : (DeviceIsOffline ? JposConst.JPOS_PS_OFFLINE : JposConst.JPOS_PS_ONLINE);
            } else
                dev.PowerState = JposConst.JPOS_PS_UNKNOWN;
        }
    }

    /**
     * Increments open count. If incremented to 1, starts communication handler.
     */
    void startCommunication() {
        if (changeOpenCount(1) == 1) {
            ToBeFinished = false;
            (StateWatcher = new Thread(this)).start();
            StartWaiter.suspend((MaxRetry + 2) * RequestTimeout);
        }
    }

    /**
     * Decrements open count. If decremented to 0, stops communication handler.
     */
    void stopCommunication() {
        if (changeOpenCount(-1) == 0) {
            ToBeFinished = true;
            while (ToBeFinished) {
                try {
                    StateWatcher.join();
                } catch (Exception e) {}
                break;
            }
        }
    }

    @Override
    public CashDrawerProperties getCashDrawerProperties(int index) {
        return new CashDrawer(this);
    }

    @Override
    public KeylockProperties getKeylockProperties(int index) {
        return new Keylock(index, this);
    }

    @Override
    public ToneIndicatorProperties getToneIndicatorProperties(int index) {
        return new ToneIndicator(this);
    }

    @Override
    public LineDisplayProperties getLineDisplayProperties(int index) {
        return new LineDisplay(this);
    }

    /**
     * Helper class for AdditionalData property of DisplayText class used for synchronous or asynchronous processing.
     * Holds volatile parameters.
     */
    static class DisplayCoordinates {
        /**
         * Contents of CursorUpdate property when the method request has been enqueued for processing.
         */
        boolean Update;

        /**
         * Contents of CursorRow property when the method request has been enqueued for processing.
         */
        int Line;

        /**
         * Contents of CursorColumn property when the method request has been enqueued for processing.
         */
        int Column;

        /**
         * Constructor. Sets properties to given values.
         * @param row       Value for property Line.
         * @param column    Value for property Column.
         * @param update    Value for property Update.
         */
        DisplayCoordinates(int row, int column, boolean update) {
            Line = row;
            Column = column;
            Update = update;
        }
    }

    @Override
    public POSKeyboardProperties getPOSKeyboardProperties(int index) {
        return new POSKeyboard(this);
    }

    /**
     * Helper class that stores MSR track data.
     */
    static class TrackData {
        /**
         * Contents of currently valid tracks. Up to 3 tracks are supported by the sample device.
         */
        byte[][] Tracks;

        /**
         * Constructor.
         * @param tracks    Initial value for tracks read before.
         */
        TrackData(byte[][] tracks) {
            Tracks = Arrays.copyOf(tracks, tracks.length > 3 ? 3 : tracks.length);
        }

        /**
         * Special toString method, used to translate the track values into readable character strings.
         * @return String representing the contents of the tracks.
         */
        @Override
        public String toString() {
            String data = "";
            for (int i = 0; i < Tracks.length; i++) {
                if (Tracks[i].length > 0)
                    data += "] / " + i + ": [" + new String(Tracks[i]);
            }
            return data.length() > 0 ? data.substring(4) + "]" : "No data";
        }
    }

    @Override
    public MSRProperties getMSRProperties(int index) {
        return new MSR(this);
    }

    @Override
    public ScannerProperties getScannerProperties(int index) {
        return new Scanner(this);
    }

    /**
     * Performs internal CheckHealth, equal processing for all device classes.
     * @param dev   Property set to be used.
     * @param level CheckHealth level parameter.
     * @return true if level = CH_INTERNAL.
     */
    boolean internalCheckHealth(JposCommonProperties dev, int level) {
        if (level == JposConst.JPOS_CH_INTERNAL) {
            dev.CheckHealthText = "Internal CheckHealth: ";
            dev.CheckHealthText += InIOError || DeviceIsOffline ? "Failed" : "OK";
            dev.CheckHealthText += ".";
            log(Level.DEBUG, dev.LogicalName + ": CheckHealthText <- " + dev.CheckHealthText);
            return true;
        }
        return false;
    }

    /**
     * Converts timeout value to long.
     * @param timeout Timeout as integer value.
     * @return For positive values, timeout. For JPOS_FOREVER -1. Otherwise 2 * (Integer.MAX_VALUE + 1) + timeout.
     */
    long timeoutToLong(int timeout) {
        long subtractor = Integer.MAX_VALUE;
        if (timeout == JposConst.JPOS_FOREVER)
            return -1l;
        if (timeout > 0)
            return timeout;
        return subtractor + subtractor + 2 + timeout;
    }
}
