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
import org.apache.log4j.Level;

import javax.swing.*;
import java.io.*;
import java.util.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties.ExclusiveAllowed;
import static de.gmxhome.conrad.jpos.jpos_base.UniqueIOProcessor.IOProcessorError;


/**
 * Implementation of a JposDevice based implementation of a combined driver that becomes
 * several JavaPOS device service in combination with the JposXxxx class in jpos_base
 */
public class Driver extends JposDevice implements Runnable{
    private UniqueIOProcessor OutStream;
    private Thread StateWatcher;
    private SyncObject WaitObj;
    private boolean ToBeFinished;
    private Properties PropertySet;
    private static byte[] CmdScannerEnable = {'R','E'};
    private static byte[] CmdScannerDisable = {'R','D'};
    private static byte[] CmdStatusRequest = {'S','R'};
    private static byte[] CmdDrawerOpen = {'D','O'};
    private static byte[] CmdBeepOn = {'B', '1'};
    private static byte[] CmdBeepOff = {'B', '0'};
    private static final byte NoResponse = 0;
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
    private static final byte RespFromStatus = 'S';
    private static final byte StatusLen = 15;
    private static final byte StatusDrawerPos = 1;
    private static final byte StatusLockPos = 2;
    private static final byte StatusEKeyPos = 3;
    private static final byte CmdTextOutPrefix = 'T';
    private static final int TextLinePos = 1;
    private static final int TextLengthPos = 2;
    private static final int TextStartPos = 4;
    private static int LockIndex = 0;
    private static int EKeyIndex = 1;
    private int Baudrate = SerialIOProcessor.BAUDRATE_9600;
    private int Databits = SerialIOProcessor.DATABITS_8;
    private int Stopbits = SerialIOProcessor.STOPBITS_2;
    private int Parity = SerialIOProcessor.PARITY_NONE;
    private int OwnPort = 0;
    private int LoggingType = UniqueIOProcessor.LoggingTypeEscapeString;
    private int RequestTimeout = 500;
    private int CharacterTimeout = 10;
    private int PollDelay = 50;
    private int MaxRetry = 2;
    private boolean UsbToSerial = false;
    private boolean InIOError = true;
    private boolean DrawerIsOpen = false;
    private boolean DeviceIsOffline = true;
    private byte LockPosition = DefaultLockPos;
    private Map<Integer, Integer> LockMapping = new HashMap<Integer, Integer>();
    private byte[] EKeyValue = DefaultEKeyPos;
    private char[][] DisplayContents = new char[2][20];
    private char[][] DisplayAttributes = new char[2][20];
    private final char NormalChar = 'n';
    private final char ReverseChar = 'r';
    private final char BlinkChar = 'b';
    private final char BlinkReverseChar = 'a';
    private int[][] KeyValueTable = new int[10][16];
    private int OpenCount = 0;
    private List<CommandHelper> Commands = new ArrayList<CommandHelper>();
    private long LastPollTick;
    private SyncObject StartWaiter = new SyncObject();

    class CommandHelper {
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
            Command = Driver.CmdStatusRequest;
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
     * tagret. Valid COM port specifiers differ between operating systems, e.g. on Windows, COM1
     * can be a valid communication target while on Linux, /dev/ttyS0 might specify the same target.
     * Format of TCP targets is <i>IpAddress</i>:<i>Port</i>, e.g. 10.11.12.13:45678.
     * @param port COM port or TCP target.
     * @throws JposException if Communication target invalid.
     */
    public Driver(String port) throws JposException {
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
        PhysicalDeviceName = "Combined Driver Simulator";
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

    /**
     * The device type. &gt;0: COM port type, &lt;0: TCP service type.
     */
    private int DeviceType = 0;

    @Override
    public void checkProperties(JposEntry entry) throws JposException {
        super.checkProperties(entry);
        try {
            Object o;
            if ((o = entry.getPropertyValue("Baudrate")) != null) {
                Baudrate = Integer.parseInt(o.toString());
                DeviceType = 1;
            }
            if ((o = entry.getPropertyValue("Databits")) != null) {
                Databits = Integer.parseInt(o.toString());
                DeviceType = 1;
            }
            if ((o = entry.getPropertyValue("Stopbits")) != null) {
                Stopbits = Integer.parseInt(o.toString());
                DeviceType = 1;
            }
            if ((o = entry.getPropertyValue("Parity")) != null) {
                Parity = Integer.parseInt(o.toString());
                DeviceType = 1;
            }
            if ((o = entry.getPropertyValue("OwnPort")) != null) {
                int port = Integer.parseInt(o.toString());
                if (port < 0 || port > 0xffff)
                    throw new IOException("Invalid TCP port: " + o.toString());
                OwnPort = port;
                DeviceType = -1;
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
            if ((o = entry.getPropertyValue("UsbToSerial")) != null) {
                UsbToSerial = Boolean.parseBoolean(o.toString());
                DeviceType = 1;
            }
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property", e);
        }
    }

    @Override
    public void changeDefaults(LineDisplayProperties props) {
        props.DeviceServiceDescription = "Display service for combined device simulator";
        props.DeviceServiceVersion = 1014001;
        props.CapCharacterSet = LineDisplayConst.DISP_CCS_UNICODE;
        props.CharacterSetDef = LineDisplayConst.DISP_CS_UNICODE;
        props.CharacterSetList = "997";
        props.CapBlink = LineDisplayConst.DISP_CB_BLINKEACH;
        props.CapReverse = LineDisplayConst.DISP_CR_REVERSEEACH;
        props.CapICharWait = true;
    }

    @Override
    public void changeDefaults(CashDrawerProperties props) {
        props.DeviceServiceDescription = "Drawer service for combined device simulator";
        props.ExclusiveUse = ExclusiveAllowed;
        props.DeviceServiceVersion = 1014001;
    }

    @Override
    public void changeDefaults(POSKeyboardProperties props) {
        props.DeviceServiceDescription = "Keyboard service for combined device simulator";
        props.DeviceServiceVersion = 1014001;
    }

    @Override
    public void changeDefaults(KeylockProperties props) {
        props.DeviceServiceDescription = "Keylock service for combined device simulator";
        props.DeviceServiceVersion = 1014001;
        if (props.Index == EKeyIndex) {
            props.CapKeylockType = KeylockConst.LOCK_KT_ELECTRONIC;
        }
        else {
            props.PositionCount = 7;
        }
    }

    @Override
    public void changeDefaults(MSRProperties props) {
        props.DeviceServiceDescription = "MSR service for combined device simulator";
        props.DeviceServiceVersion = 1014001;
    }

    @Override
    public void changeDefaults(ScannerProperties props) {
        props.DeviceServiceDescription = "Scanner service for combined device simulator";
        props.DeviceServiceVersion = 1014001;
    }

    @Override
    public void changeDefaults(ToneIndicatorProperties props) {
        props.DeviceServiceDescription = "Tone indicator service for combined device simulator";
        props.DeviceServiceVersion = 1014001;
    }

    /**
     * Reads properties from property file and stores property values in corresponding class variables
     * @throws JposException if <i>classname</i>.properties does not exist.
     */
    private void ProcessProperties() throws JposException {
        PropertySet = new Properties();
        File propertyFile = new File(getClass().getName() + ".properties");
        if (propertyFile.exists()) {
            try {
                BufferedInputStream istream = new BufferedInputStream(new FileInputStream(propertyFile));
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
        else
            retry[0] = 0;
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
            handlePowerStateEvent(JposConst.JPOS_SUE_POWER_OFFLINE);
            DeviceIsOffline = true;
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
                    DeviceIsOffline = InIOError = false;
                    handlePowerStateEvent(JposConst.JPOS_SUE_POWER_ONLINE);
                } catch (JposException e1) {
                    closePort(false);
                    return e1;
                }
            }
            else {
                DeviceIsOffline = InIOError = false;
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

    private Object respFromStatus(byte[] next, int offset, byte[] head) throws JposException {
        if (readData(next, offset, StatusLen)) {
            Exception e = handleDrawerChange(next[StatusDrawerPos] == DrawerOpenVal);
            if (e == null)
                e = handleLockChange(next[StatusLockPos]);
            if (e == null)
                e = handleEKeyChange(Arrays.copyOfRange(next, StatusEKeyPos, EKeyValueLen + StatusEKeyPos));
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
                if (claimer != null)
                    handleEvent(new ScannerDataEvent(claimer.EventSource, 0, data, label, labelType));
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
            byte[][] tracks = new byte[][]{new byte[0], new byte[0], new byte[0]};
            boolean success = readData(next, 0, trackTotal);
            if (props != null) {
                if (success && next[0] == '1') {
                    Exception e = extractTracks(next, props, trackTotal, tracks);
                    if (e != null)
                        return e;
                }
                if (props.ErrorReportingType == MSRConst.MSR_ERT_TRACK) {
                    try {
                        handleEvent(new MSRErrorEvent(props.EventSource, JposConst.JPOS_E_FAILURE, 0, new TrackData(tracks)));
                    } catch (JposException e) {
                        return e;
                    }
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
                if (props != null && start == trackTotal) {
                    try {
                        int status = (((tracks[2].length << 8) + tracks[1].length) << 8) + tracks[0].length;
                        handleEvent(new MSRDataEvent(props.EventSource, status, new TrackData(tracks)));
                    }
                    catch (Exception e) {
                        return e;
                    }
                }
            }
        }
        return null;
    }

    private int extractTrack(int index, byte startChar, byte[] next, int trackTotal, byte[][] tracks, int start) {
        int end = start + 1;
        if (start < trackTotal && next[start] == startChar) {
            while (end < trackTotal && next[end] != '?')
                end++;
            if (end < trackTotal) {
                MSRProperties props = (MSRProperties)getClaimingInstance(ClaimedMSR, 0);
                int trackbit = new int[]{MSRConst.MSR_TR_1, MSRConst.MSR_TR_2, MSRConst.MSR_TR_3}[index];
                if (props != null && (trackbit & props.TracksToRead) != 0)
                    tracks[index] = Arrays.copyOfRange(next, start, end + 1);
                return end + 1;
            }
            else
                return start - 1;
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
                }
            }
        }
        return null;
    }

    private Exception handleEKeyChange(byte[] newEkeyValue) {
        if (!Arrays.equals(newEkeyValue, EKeyValue)) {
            EKeyValue = newEkeyValue;
            JposCommonProperties props = getPropertySetInstance(Keylocks, EKeyIndex, 0);
            if (props != null) {
                try {
                    handleEvent(new KeylockStatusUpdateEvent(props.EventSource, KeylockConst.LOCK_KP_ELECTRONIC, EKeyValue));
                } catch (JposException e) {
                    return e;
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
                    handleEvent(new KeylockStatusUpdateEvent(props.EventSource, LockMapping.get(new Integer(LockPosition)), new byte[0]));
                } catch (JposException e) {
                    return e;
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
        }
        return null;
    }

    /**
     * Method to perform any command
     * @param dev property set to be used for logging, if enabled.
     * @param request Data to be sent
     * @param responseType type of response frame (first byte)
     * @return null on timeout, Byte(responseType) on success
     * @throws JposException in error case
     */
    protected Byte sendCommand(JposCommonProperties dev, byte[] request, byte responseType) throws JposException {
        if (InIOError || DeviceIsOffline) {
            throw  new JposException(JposConst.JPOS_E_ILLEGAL, "Driver not available");
        }
        CommandHelper helper = new CommandHelper(request, responseType);
        enterCommand(helper);
        WaitObj.signal();
        helper.Signalizer.suspend(SyncObject.INFINITE);
        if (helper.Response != responseType)
            return null;
        return new Byte(responseType);
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
            if (DeviceType == 0) {
                try {
                    OutStream = new TcpClientIOProcessor(this, ID);
                } catch (JposException e) {}
            }
            else
                OutStream = null;
            if (DeviceType >= 0 && OutStream == null) {
                SerialIOProcessor ser;
                OutStream = ser = new SerialIOProcessor(this, ID);
                ser.setParameters(Baudrate, Databits, Stopbits, Parity);
            }
            else {
                TcpClientIOProcessor tcp = OutStream != null ? (TcpClientIOProcessor) OutStream : new TcpClientIOProcessor(this, ID);
                tcp.setParam(OwnPort);
                OutStream = tcp;
            }
            OutStream.open(false);
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    synchronized int changeOpenCount(int value) {
        OpenCount += value;
        return OpenCount;
    }

    private void updateKeylockStates(JposCommonProperties dev, boolean enable) throws JposException {
        KeylockProperties props = (KeylockProperties) dev;
        if (enable) {
            if (dev.Index == EKeyIndex) {
                props.ElectronicKeyValue = EKeyValue;
                props.KeyPosition = 0;
            }
            else {
                Integer i = LockMapping.get(new Integer(LockPosition));
                if(i != null)
                    props.KeyPosition = i;
                else
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Internal error: Cannot translate key position");
            }
        }
    }

    private void updateDrawerStates(CashDrawerProperties dev, boolean enable) {
        if (enable) {
            dev.DrawerOpened = DrawerIsOpen;
        }
    }

    private void updateCommonStates(JposCommonProperties dev, boolean enable) {
        if (enable) {
            if (dev.PowerNotify == JposConst.JPOS_PN_ENABLED) {
                dev.PowerState = InIOError ? JposConst.JPOS_PS_OFF : (DeviceIsOffline ? JposConst.JPOS_PS_OFFLINE : JposConst.JPOS_PS_ONLINE);
            } else
                dev.PowerState = JposConst.JPOS_PS_UNKNOWN;
        }
    }

    private void startCommunication() {
        if (changeOpenCount(1) == 1) {
            ToBeFinished = false;
            (StateWatcher = new Thread(this)).start();
            StartWaiter.suspend((MaxRetry + 2) * RequestTimeout);
        }
    }

    private void stopCommunication() {
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
        return new CombinedDrawerAccessor(index);
    }

    private class CombinedDrawerAccessor extends CashDrawerProperties {
        CombinedDrawerAccessor(int index) {
            super(index);
        }

        @Override
        public void deviceEnabled(boolean enable) throws JposException {
            super.deviceEnabled(enable);
            updateCommonStates(this, enable);
            updateDrawerStates(this, enable);
        }

        @Override
        public void open() throws JposException {
            startCommunication();
            super.open();
        }

        @Override
        public void close() throws JposException {
            super.close();
            stopCommunication();
        }

        @Override
        public void checkHealth(int level) throws JposException {
            if (internalCheckHealth(this, level, true))
                return;
            try {
                do {
                    if (level == JposConst.JPOS_CH_EXTERNAL) {
                        CheckHealthText = "External check: Error";
                        ((CashDrawerService)EventSource).openDrawer();
                        new SyncObject().suspend(200);
                        ((CashDrawerService)EventSource).waitForDrawerClose(5000, 500, 300, 2000);
                        CheckHealthText = "External check: OK";
                        break;
                    }
                    if (drawerCheckHealthInteractive())
                        break;
                } while (false);
            } catch (JposException e) {}
            super.checkHealth(level);
        }

        private boolean drawerCheckHealthInteractive() throws JposException {
            SyncObject waiter = new SyncObject();
            CheckHealthText = "Interactive check: Error";
            synchronizedMessageBox("Press OK to open the drawer", "CheckHealth Drawer", JOptionPane.INFORMATION_MESSAGE);
            ((CashDrawerService)EventSource).openDrawer();
            for (int i = 0; i > 10 && !DrawerOpened; i++) {
                waiter.suspend(200);
                if (DrawerIsOpen)
                    break;
            }
            if (!DrawerIsOpen) {
                synchronizedMessageBox("Drawer could not be opened", "CheckHealth Drawer", JOptionPane.ERROR_MESSAGE);
                return true;
            }
            synchronizedMessageBox("Close drawer to finish drawer check", "CheckHealth Drawer", JOptionPane.INFORMATION_MESSAGE);
            waiter.suspend(200);
            if (DrawerIsOpen) {
                synchronizedMessageBox("Drawer could not be closed", "CheckHealth Drawer", JOptionPane.ERROR_MESSAGE);
                return true;
            }
            synchronizedMessageBox("Drawer check finished successfully", "CheckHealth Drawer", JOptionPane.INFORMATION_MESSAGE);
            CheckHealthText = "Interactive check: OK";
            return false;
        }

        @Override
        public void openDrawer() throws JposException {
            int retry = 0;
            do {
                sendCommand(this, CmdDrawerOpen, NoResponse);
                if (sendCommand(this, CmdStatusRequest, RespFromStatus) == 0)
                    continue;
                super.openDrawer();
                return;
            } while (++retry < MaxRetry);
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "No response on drawer open request");
        }

        @Override
        public void waitForDrawerClose(int beepTimeout, int beepFrequency, int beepDuration, int beepDelay) throws JposException {
            long timeout = beepTimeout;
            boolean beeping = false;

            attachWaiter();
            if (DrawerOpened) {
                while (!waitWaiter(timeout)) {
                    if (beeping) {
                        timeout = beepDelay;
                        sendCommand(this, CmdBeepOff, NoResponse);
                    } else {
                        timeout = beepDuration;
                        sendCommand(this, CmdBeepOn, NoResponse);
                    }
                    beeping = !beeping;
                }
                if (beeping)
                    sendCommand(this, CmdBeepOff, NoResponse);
            }
            releaseWaiter();
            super.waitForDrawerClose(beepTimeout, beepFrequency, beepDuration, beepDelay);
        }
    }

    @Override
    public KeylockProperties getKeylockProperties(int index) {
        return new CombinedKeylockAccessor(index);
    }

    private class CombinedKeylockAccessor extends KeylockProperties {
        CombinedKeylockAccessor(int index) {
            super(index);
        }

        @Override
        public void deviceEnabled(boolean enable) throws JposException {
            super.deviceEnabled(enable);
            updateCommonStates(this, enable);
            updateKeylockStates(this, enable);
        }

        @Override
        public void open() throws JposException {
            startCommunication();
            super.open();
        }

        @Override
        public void close() throws JposException {
            super.close();
            stopCommunication();
        }

        @Override
        public void checkHealth(int level) throws JposException {
            if (internalCheckHealth(this, level, true))
                return;
            // TOBEIMPLEMENTED
            super.checkHealth(level);
        }

        @Override
        public void waitForKeylockChange(int pos, int timeout) throws JposException {
            long startTime = System.currentTimeMillis();
            long occurredTime = 0;
            long tio = timeoutToLong(timeout);
            if (pos != KeyPosition || pos == KeylockConst.LOCK_KP_ANY) {
                attachWaiter();
                while (occurredTime < tio && waitWaiter(tio - occurredTime)) {
                    if (pos == KeylockConst.LOCK_KP_ANY || pos == KeyPosition) {
                        occurredTime = tio - 1;
                        break;
                    }
                    occurredTime = System.currentTimeMillis() - startTime;
                }
                releaseWaiter();
                if (occurredTime == tio)
                    throw new JposException(JposConst.JPOS_E_TIMEOUT, "No keylock change");
            }
            super.waitForKeylockChange(pos, timeout);
        }
    }

    @Override
    public ToneIndicatorProperties getToneIndicatorProperties(int index) {
        return new CombinedToneIndicatorAccessor(index);
    }

    private class CombinedToneIndicatorAccessor extends ToneIndicatorProperties {
        CombinedToneIndicatorAccessor(int index) {
            super(index);
        }

        @Override
        public void deviceEnabled(boolean enable) throws JposException {
            super.deviceEnabled(enable);
            updateCommonStates(this, enable);
        }

        @Override
        public void open() throws JposException {
            startCommunication();
            super.open();
        }

        @Override
        public void close() throws JposException {
            super.close();
            stopCommunication();
        }

        @Override
        public void checkHealth(int level) throws JposException {
            if (internalCheckHealth(this, level, true))
                return;
            super.checkHealth(level);
        }

        @Override
        public void sound(Sound request) throws JposException {
            if ((Tone1Duration | Tone2Duration) != 0) {
                while (request.Count == JposConst.JPOS_FOREVER || request.Count-- > 0) {
                    if (soundAndDelay(request, Tone1Duration, InterToneWait))
                        break;
                    if (soundAndDelay(request, Tone2Duration, request.Delay))
                        break;
                }
            }
        }

        private boolean soundAndDelay(JposOutputRequest request, int duration, int delay) throws JposException {
            ToneIndicatorProperties props = (ToneIndicatorProperties)request.Props;
            if (request.Abort != null) {
                JposCommonProperties claimer = props.getClaimingInstance();
                if (request.EndSync != null && claimer != null && claimer != props)
                    throw new JposException(JposConst.JPOS_E_CLAIMED, "Claimed by other instance");
                return true;
            }
            if (duration > 0) {
                sendCommand(request.Props, CmdBeepOn, NoResponse);
                request.Waiting.suspend(duration);
                if (request.Abort != null)
                    return true;
                sendCommand(request.Props, CmdBeepOff, NoResponse);
                if (request.Abort != null)
                    return true;
            }
            if (delay > 0) {
                request.Waiting.suspend(delay);
            }
            return false;
        }
    }

    @Override
    public LineDisplayProperties getLineDisplayProperties(int index) {
        return new CombinedDisplayAccessor(index);
    }

    static private class DisplayCoordinates {
        boolean Update;
        int Line;
        int Column;
        DisplayCoordinates(int row, int column, boolean update) {
            Line = row;
            Column = column;
            Update = update;
        }
    }

    private class CombinedDisplayAccessor extends LineDisplayProperties {
        CombinedDisplayAccessor(int index) {
            super(index);
        }

        @Override
        public void deviceEnabled(boolean enable) throws JposException {
            super.deviceEnabled(enable);
            updateCommonStates(this, enable);
        }

        @Override
        public void claim(int timeout) throws JposException {
            startCommunication();

            super.claim(timeout);
        }

        @Override
        public void release() throws JposException {
            super.release();
            stopCommunication();
        }

        @Override
        public void checkHealth(int level) throws JposException {
            if (internalCheckHealth(this, level, true))
                return;
            CheckHealthText = (level == JposConst.JPOS_CH_EXTERNAL ? "Externel" : "Internal") + " CheckHealth: ";
            try {
                ((LineDisplayService) EventSource).clearText();
                ((LineDisplayService) EventSource).displayTextAt(1, 3, "CheckHealth: OK!", LineDisplayConst.DISP_DT_NORMAL);
                CheckHealthText += "OK";
            } catch (JposException e) {
                CheckHealthText += "Failed, " + e.getMessage();
            }
            super.checkHealth(level);
        }

        @Override
        public void clearText() throws JposException {
            int row, column;
            for (row = 0; row < DisplayContents.length; row++) {
                for (column = 0; column < DisplayContents[row].length; column++) {
                    DisplayAttributes[row][column] = NormalChar;
                    DisplayContents[row][column] = ' ';
                }
            }
            CursorRow = CursorColumn = 0;
            refreshWindow(0);
            super.clearText();
        }

        @Override
        public void deviceBrightness(int b) throws JposException {
            super.deviceBrightness(b);
            refreshWindow(0);
        }

        @Override
        public void scrollText(int direction, int units) throws JposException {
            boolean otherDirection = false;
            switch (direction) {
                case LineDisplayConst.DISP_ST_DOWN:
                    otherDirection = true;
                case LineDisplayConst.DISP_ST_UP:
                    scrollVertical(units, otherDirection);
                    break;
                case LineDisplayConst.DISP_ST_LEFT:
                    otherDirection = true;
                case LineDisplayConst.DISP_ST_RIGHT:
                    scrollHorizontal(units, otherDirection);
                    break;
            }
            super.scrollText(direction, units);
        }

        @Override
        public DisplayText displayText(String text, int attribute) throws JposException {
            DisplayText request = super.displayText(text, attribute);
            LineDisplayService.DisplayDataPart[] data = request.getData();
            request.AdditionalData = new DisplayCoordinates(CursorRow, CursorColumn, CursorUpdate);
            if (CursorUpdate && InterCharacterWait > 0 && MarqueeType == LineDisplayConst.DISP_MT_NONE) {
                // We update the coordinates here
                ((DisplayCoordinates)request.AdditionalData).Update = false;
                for (Object o : data) {
                    if (o instanceof LineDisplayService.DisplayData) {
                        LineDisplayService.DisplayData dd = (LineDisplayService.DisplayData)o;
                        CursorColumn += dd.getData().length();
                        while (CursorColumn > Columns) {
                            CursorColumn -= Columns;
                            if (CursorRow < Rows - 1)
                                CursorRow++;
                        }
                    }
                    else if (o instanceof LineDisplayService.ControlChar) {
                        CursorColumn = 0;
                        if (CursorRow < Rows - 1 && ((LineDisplayService.ControlChar) o).getControlCharacter() == '\n')
                            CursorRow++;
                    }
                }
            }
            return request;
        }

        private SyncObject InterCharacterWaiter = new SyncObject();

        @Override
        public void interCharacterWait(int b) throws JposException {
            int prev = InterCharacterWait;
            super.interCharacterWait(b);
            if (prev != b && b == 0)
                InterCharacterWaiter.signal();
        }

        @Override
        public void displayText(DisplayText request) throws  JposException {
            char attribute = NormalChar;
            DisplayCoordinates coordinates = (DisplayCoordinates)request.AdditionalData;
            InterCharacterWaiter = new SyncObject();
            for(Object o : request.getData()) {
                if (o instanceof LineDisplayService.DisplayData){
                    String data = ((LineDisplayService.DisplayData) o).getData();
                    for (int i = 0; i < data.length(); i++) {
                        processChar(coordinates, data.charAt(i), attribute);
                        if (InterCharacterWait > 0 && MarqueeType == LineDisplayConst.DISP_MT_NONE) {
                            refreshWindow(0);
                            InterCharacterWaiter.suspend(InterCharacterWait);
                        }
                    }
                }
                else if (o instanceof LineDisplayService.ControlChar)
                    processChar(coordinates, ((LineDisplayService.ControlChar) o).getControlCharacter(), attribute);
                else if (o instanceof LineDisplayService.EscNormalize)
                    attribute = NormalChar;
                else if (o instanceof LineDisplayService.EscSimple && attribute != BlinkReverseChar) {
                    LineDisplayService.EscSimple esc = (LineDisplayService.EscSimple) o;
                    if ((esc.getBlinking() && (esc.getReverse() || attribute == ReverseChar)) || (esc.getReverse() && attribute == BlinkChar))
                        attribute = BlinkReverseChar;
                    else
                        attribute = esc.getReverse() ? ReverseChar : BlinkChar;
                }
            }
            if (coordinates.Update) {
                CursorRow = coordinates.Line;
                CursorColumn = coordinates.Column;
            }
            refreshWindow(0);
        }

        @Override
        public void refreshWindow(int index) throws JposException {
            byte[][] lines = new byte[2][];
            if (DeviceBrightness < 50) {
                sendTextLine(this, " ", '0');
                sendTextLine(this, " ", '1');
            } else {
                sendTextLine(this, new String(DisplayContents[0]) + new String(DisplayAttributes[0]), '0');
                sendTextLine(this, new String(DisplayContents[1]) + new String(DisplayAttributes[1]), '1');
            }
            super.refreshWindow(index);
        }

        private void scrollHorizontal(int units, boolean otherDirection) throws JposException {
            if (units >= 20) {
                clearText();
                return;
            }
            if (units > 0) {
                int i;
                for (i = 0; i < 20 - units; i++) {
                    if (otherDirection) {
                        DisplayContents[0][i] = DisplayContents[0][i + units];
                        DisplayContents[1][i] = DisplayContents[1][i + units];
                        DisplayAttributes[0][i] = DisplayAttributes[0][i + units];
                        DisplayAttributes[1][i] = DisplayAttributes[1][i + units];
                    } else {
                        DisplayContents[0][19 - i] = DisplayContents[0][19 - i - units];
                        DisplayContents[0][19 - i] = DisplayContents[0][19 - i - units];
                        DisplayAttributes[0][19 - i] = DisplayAttributes[0][19 - i - units];
                        DisplayAttributes[0][19 - i] = DisplayAttributes[0][19 - i - units];
                    }
                }
                while (i < 20) {
                    if (otherDirection) {
                        DisplayContents[0][i] = DisplayContents[1][i] = ' ';
                        DisplayAttributes[0][i] = DisplayAttributes[1][i] = NormalChar;
                    }
                    else {
                        DisplayContents[0][19 - i] = DisplayContents[1][19 - i] = ' ';
                        DisplayAttributes[0][19 - i] = DisplayAttributes[1][19 - i] = NormalChar;
                    }
                }
                refreshWindow(0);
            }
        }

        private void scrollVertical(int units, boolean otherDirection) throws JposException {
            if (units >= 2) {
                clearText();
                return;
            }
            if (units == 1) {
                for (int i = 0; i < 20; i++) {
                    if (otherDirection) {
                        DisplayContents[1][i] = DisplayContents[0][i];
                        DisplayContents[0][i] = ' ';
                        DisplayAttributes[1][i] = DisplayAttributes[0][i];
                        DisplayAttributes[0][i] = NormalChar;
                    } else {
                        DisplayContents[0][i] = DisplayContents[1][i];
                        DisplayContents[1][i] = ' ';
                        DisplayAttributes[0][i] = DisplayAttributes[1][i];
                        DisplayAttributes[1][i] = NormalChar;
                    }
                }
                refreshWindow(0);
            }
        }
    }

    private void processChar(DisplayCoordinates coordinates, char c, char attribute) {
        if (c == '\r') {
            coordinates.Column = 0;
            return;
        }
        if (c == '\n' || coordinates.Column == 20) {
            coordinates.Column = 0;
            if (coordinates.Line == 0) {
                coordinates.Line++;
            } else {
                for (int i = 0; i < 20; i++) {
                    DisplayContents[0][i] = DisplayContents[1][i];
                    DisplayContents[1][i] = ' ';
                    DisplayAttributes[0][i] = DisplayAttributes[1][i];
                    DisplayAttributes[1][i] = NormalChar;
                }
            }
            if (c == '\n')
                return;
        }
        DisplayContents[coordinates.Line][coordinates.Column] = c;
        DisplayAttributes[coordinates.Line][coordinates.Column] = attribute;
        coordinates.Column++;
    }

    private void sendTextLine(LineDisplayProperties props, String linestr, char row) throws JposException {
        byte[] line;
        try {
            line = linestr.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, e.getMessage(), e);
        }
        byte[] buffer = Arrays.copyOf(new byte[]{CmdTextOutPrefix}, line.length + TextStartPos);
        System.arraycopy(line,0, buffer, TextStartPos, line.length);
        buffer[TextLinePos] = (byte)row;
        for (int i = TextStartPos, len = linestr.length(); --i >= TextLengthPos; len /= 10)
            buffer[i] = (byte)(len % 10 + '0');
        sendCommand(props, buffer, NoResponse);
    }

    @Override
    public POSKeyboardProperties getPOSKeyboardProperties(int index) {
        return new CombinedKeyboardAccessor(index);
    }

    private class CombinedKeyboardAccessor extends POSKeyboardProperties {
        CombinedKeyboardAccessor(int index) {
            super(index);
        }

        @Override
        public void claim(int timeout) throws JposException {
            startCommunication();
            super.claim(timeout);
        }

        @Override
        public void release() throws JposException {
            super.release();
            stopCommunication();
        }

        @Override
        public void checkHealth(int level) throws JposException {
            if (internalCheckHealth(this, level, true))
                return;
            // TOBEIMPLEMENTED
            super.checkHealth(level);
        }
    }

    private static class TrackData {
        byte[][] Tracks;
        TrackData(byte[][] tracks) {
            Tracks = tracks;
        }
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
        return new CombinedMSRAccessor(index);
    }

    private class CombinedMSRAccessor extends MSRProperties {
        CombinedMSRAccessor(int index) {
            super(index);
        }

        @Override
        public void claim(int timeout) throws JposException {
            startCommunication();
            super.claim(timeout);
        }

        @Override
        public void release() throws JposException {
            super.release();
            stopCommunication();
        }

        @Override
        public void checkHealth(int level) throws JposException {
            if (internalCheckHealth(this, level, true))
                return;
            // TOBEIMPLEMENTED
            super.checkHealth(level);
        }

        @Override
        public void setDataProperties(Object o) {
            if (o instanceof TrackData) {
                byte[][] tracks = ((TrackData) o).Tracks;
                if (tracks.length == 3) {
                    Track1Data = storeData(tracks[0], 0x20, 0x3f);
                    Track2Data = storeData(tracks[1], 0x30, 0xf);
                    Track3Data = storeData(tracks[2], 0x30, 0xf);
                }
            }
        }
        private byte[] storeData(byte[] source, int subtractor, int maxvalue) {
            if (!TransmitSentinels && source.length > 1)
                source = Arrays.copyOfRange(source, 1, source.length);
            if (DecodeData)
                return source;
            byte[] target = new byte[source.length];
            int i = target.length;
            while (--i >= 0) {
                if ((target[i] = (byte)(source[i] - subtractor)) < 0 || target[i] > maxvalue)
                    break;
            }
            return i < 0 ? target : source;
        }
    }

    @Override
    public ScannerProperties getScannerProperties(int index) {
        return new CombinedScannerAccessor(index);
    }

    private class CombinedScannerAccessor extends ScannerProperties {
        CombinedScannerAccessor(int index) {
            super(index);
        }

        @Override
        public void claim(int timeout) throws JposException {
            startCommunication();
            super.claim(timeout);
        }

        @Override
        public void release() throws JposException {
            super.release();
            stopCommunication();
        }

        @Override
        public void checkHealth(int level) throws JposException {
            if (internalCheckHealth(this, level, true))
                return;
            super.checkHealth(level);
        }
    }

    private boolean internalCheckHealth(JposCommonProperties dev, int level, boolean claimed) {
        if (level == JposConst.JPOS_CH_INTERNAL) {
            try {
                dev.CheckHealthText = "Internal CheckHealth: ";
                if (!claimed) {
                    dev.EventSource.claim(RequestTimeout * (MaxRetry + 2));
                    dev.CheckHealthText += "Claimed ";
                }
                dev.CheckHealthText += InIOError || DeviceIsOffline ? "Failed" : "OK";
                if (!claimed) {
                    dev.EventSource.release();
                    dev.CheckHealthText += " Released";
                }
            } catch (JposException e) {
                if (!claimed && dev.Claimed) {
                    dev.CheckHealthText += "Failed";
                    try {
                        dev.EventSource.release();
                        dev.CheckHealthText += " Released";
                    } catch (JposException ee) {}
                }
            }
            dev.CheckHealthText += ".";
            log(Level.DEBUG, dev.LogicalName + ": CheckHealthText <- " + dev.CheckHealthText);
            return true;
        }
        return false;
    }

    private long timeoutToLong(int timeout) {
        long subtractor = Integer.MAX_VALUE;
        if (timeout == JposConst.JPOS_FOREVER)
            return -1l;
        if (timeout > 0)
            return timeout;
        return subtractor + subtractor + 2 + timeout;
    }
}
