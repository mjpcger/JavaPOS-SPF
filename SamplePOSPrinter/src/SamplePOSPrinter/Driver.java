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

package SamplePOSPrinter;

import jpos.*;
import jpos.config.*;
import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.cashdrawer.*;
import de.gmxhome.conrad.jpos.jpos_base.posprinter.*;
import org.apache.log4j.Level;

import java.io.*;
import java.util.*;


/**
 * Implementation of a JposDevice based implementation of a sample printer that becomes
 * JavaPOS CashDrawer and POSPrinter device services in combination with the CashDrawerServer
 * and POSPrinterServer classes.
 */
public class Driver extends JposDevice{
    private UniqueIOProcessor OutStream;
    private boolean ToBeFinished;
    private static final String CharSetList = "997,998,999,1250,1251,1252,1253,1254,1257";
    private static final String LineCharsList = "42,56";
    // Constants for status response byte. Naming: "Resp" + printer state word + drawer state word
    private final static byte RespOk = '0';
    private final static byte RespNearEnd = '2';
    private final static byte RespEnd = '4';
    private final static byte RespCover = '6';
    private final static byte RespError = '8';
    private final static byte RespDrawerBit = 1;
    // Jpos.xml properties
    private int Baudrate = SerialIOProcessor.BAUDRATE_9600;
    private int Databits = SerialIOProcessor.DATABITS_8;
    private int Stopbits = SerialIOProcessor.STOPBITS_2;
    private int Parity = SerialIOProcessor.PARITY_NONE;
    private int OwnPort = 0;
    private int LoggingType = UniqueIOProcessor.LoggingTypeEscapeString;
    private int PollDelay = 1000;
    private int MaxRetry = 2;
    private boolean UsbToSerial = false;

    private boolean MapCharacterSet = true;
    private boolean CoverOpen = false;
    private final Integer[] OpenCount = new Integer[1];
    private List<SyncObject> StatusWaitList = new ArrayList<SyncObject>();

    /**
     * Byte arrays for printer normalization command.
     */
    final static byte[] CmdNormalize = {'\33', 'b', '0', '\33', 'c', '0', '\33', 'o', 'l', '\33', 'u', '0'};

    /**
     * Byte array for drawer open command.
     */
    final static byte[] CmdDrawerOpen = {'\33','d'};

    /**
     * Byte array for status request command.
     */
    final static byte[] CmdStatusRequest = {'\33','s'};

    /**
     * List of supported character sets.
     */
    static final int CharSetListVals[] = {POSPrinterConst.PTR_CS_UNICODE,POSPrinterConst.PTR_CS_ASCII, POSPrinterConst.PTR_CS_ANSI,1250,1251,1252,1253,1254,1257};

    /**
     * Character width for font A and B in dots.
     */
    static final int[] CharWidths = { 12, 9 };

    /**
     * Character height for fontt A and B in dots.
     */
    static final int[] LineHeights = { 24, 18 };

    /**
     * Line spacing in dots.
     */
    static final int[] LineSpacings = { 30, 23 };

    /**
     * Index of current font. Valid values: 0 (font A) and 1 (font B).
     */
    int CurrentFontIndex = 0;

    /**
     * Index of last font used or -1 if unknown.
     */
    int LastUsedFontIndex = -1;

    /**
     * Index of last character set used, -1 if not known.
     */
    int LastUsedCodePageIndex = -1;

    /**
     * Line width in dots.
     */
    static final int LineWidth = 512;

    /**
     * Knife offset from print head in dots.
     */
    static final int KnifeOffset = 50;

    /**
     * Timeout for response after status request.
     */
    int RequestTimeout = 500;

    /**
     * Jpos.xml property. Specifies whether setting RecLineChars sets RecLineChars to the given value (false) or to the
     * maximum value allowed for the largest font that can print the specified number of characters per line.
     */
    boolean AdjustLineChars = true;

    /**
     * Flag, set to true in case of a communication error.
     */
    boolean InIOError = true;

    /**
     * Flag, set to true if the printer indicates a printer error.
     */
    boolean PrinterError = false;

    /**
     * Flag, set to true if the printer is online
     */
    boolean Online = false;

    /**
     * Flag, set to true if the cash drawer is open.
     */
    boolean DrawerOpen = false;

    /**
     * Paper state, one of PaperOk, PaperNearEnd, PaperEnd
     */
    int PaperState = PaperOk;

    /**
     * Constant for printer state (printer working).
     */
    static final int PaperOk = 0;

    /**
     * Constant for printer state (printer working, but paper is near end).
     */
    static final int PaperNearEnd = 1;

    /**
     * Constant for printer state (printer not working (paper end).
     */
    static final int PaperEnd = 2;

    /**
     * Synchronization object to be signalled after next status response.
     */
    SyncObject StartWaiter;

    /**
     * Synchronization object used by status poll thread. The status poll thread uses this object to wait up to
     * PollDelay milliseconds after the last poll. Can be signalled to force an immediate status request.
     * this object
     */
    SyncObject PollWaiter = new SyncObject();

    /**
     * Jpos.xml property. Can be set to a delay in milliseconds between asynchronous commands. May be helpful for testing.
     */
    int AsyncProcessingCommandDelay = 0;

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
        pOSPrinterInit(1);
        cashDrawerInit(1);
        setCapPowerReportingDef();
        PhysicalDeviceDescription = "Sample printer simulator for virtual COM ports or TCP";
        PhysicalDeviceName = "Sample Printer Simulator";
        OpenCount[0] = 0;
    }

    private void setCapPowerReportingDef() {
        try {
            new TcpClientIOProcessor(this, ID);
        } catch (JposException e) {
            TcpType = false;
        }
        CapPowerReporting = JposConst.JPOS_PR_ADVANCED;
    }

    /**
     * Specifies whether the device is connected via tcp. If false, connection is via COM port.
     */
    private boolean TcpType = true;

    @Override
    public void checkProperties(JposEntry entry) throws JposException {
        super.checkProperties(entry);
        try {
            Object o;
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
            if ((o = entry.getPropertyValue("PollDelay")) != null)
                PollDelay = Integer.parseInt(o.toString());
            if ((o = entry.getPropertyValue("MaxRetry")) != null)
                MaxRetry = Integer.parseInt(o.toString());
            if ((o = entry.getPropertyValue("UsbToSerial")) != null) {
                UsbToSerial = Boolean.parseBoolean(o.toString());
                if (TcpType)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property: UsbToSerial");
            }
            if ((o = entry.getPropertyValue("MapCharacterSet")) != null) {
                MapCharacterSet = Boolean.parseBoolean(o.toString());
            }
            if ((o = entry.getPropertyValue("AdjustLineChars")) != null) {
                AdjustLineChars = Boolean.parseBoolean(o.toString());
            }
            if ((o = entry.getPropertyValue("AsyncProcessingCommandDelay")) != null) {
                AsyncProcessingCommandDelay = Integer.parseInt(o.toString());
            }
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property", e);
        }
    }

    @Override
    public void changeDefaults(POSPrinterProperties props) {
        props.DeviceServiceDescription = "POSPrinter service for printer simulator";
        props.DeviceServiceVersion = 1014001;
        props.CapCharacterSet = POSPrinterConst.PTR_CCS_UNICODE;
        props.CapCoverSensor = true;
        props.CapRec2Color = true;
        props.CapRecBold = true;
        props.CapRecColor = POSPrinterConst.PTR_COLOR_PRIMARY|POSPrinterConst.PTR_COLOR_CUSTOM1;
        props.CapRecItalic = false;
        props.CapTransaction = true;
        props.CharacterSetDef = CharSetListVals[0];
        props.MapCharacterSet = MapCharacterSet;
        props.CharacterSetList = CharSetList;
        props.RecLineCharsList = LineCharsList;
        props.RecLineHeightDef = LineHeights[CurrentFontIndex];
        props.RecLineWidthDef = (LineWidth / CharWidths[CurrentFontIndex]) * CharWidths[CurrentFontIndex];
        props.RecLineSpacingDef = LineSpacings[CurrentFontIndex];
        props.RecLinesToPaperCutDef = (KnifeOffset + LineSpacings[CurrentFontIndex] - 1) / LineSpacings[CurrentFontIndex];
    }

    @Override
    public void changeDefaults(CashDrawerProperties props) {
        props.DeviceServiceDescription = "Drawer service for combined device simulator";
        props.DeviceServiceVersion = 1014001;
    }

    private final Byte SocketSync = new Byte((byte)0);

    /**
     * Method to write data synchronized.
     * @param request Data to be sent.
     * @throws  JposException if sending data fails due to missing or loosing output device.
     */
    protected void sendCommand(byte[] request) throws JposException
    {
        synchronized(SocketSync) {
            if (!TcpType && UsbToSerial && !((SerialIOProcessor) OutStream).exists())
                throw new JposException(0, "Device plugged off");
            if (OutStream == null) {
                JposException e = initPort();
                if (e != null) {
                    throw e;
                }
            }
            try {
                OutStream.write(request);
            } catch (JposException e) {}
        }
    }

    /**
     * Method to wait for a specific drawer state.
     * @param opened true to wait for drawer open, false to wait for drawer closed.
     * @param timeout Maximum waiting time.
     * @return true if status changed to the status specified by opened, false in case of timeout or offline
     */
    protected boolean waitStatusChange(boolean opened, int timeout) {
        boolean ret = false;
        long starttime = System.currentTimeMillis();
        SyncObject obj = new SyncObject();
        do {
            synchronized (this) {
                if (DrawerOpen == opened || !Online)
                    return Online;
                StatusWaitList.add(obj);
            }
            long currenttime = System.currentTimeMillis();
            long tio = timeout == SyncObject.INFINITE ? timeout : currenttime - starttime >= timeout ? 1 : timeout + starttime - currenttime;
            if (!obj.suspend(tio)) {
                synchronized (this) {
                    if (DrawerOpen == opened || !Online) {
                        if (StatusWaitList.contains(obj))
                            StatusWaitList.remove(obj);
                        return Online;
                    }
                }
                return false;
            }
        } while (true);
    }

    private void handleCommunicationError(String msg) {
        synchronized(SocketSync) {
            closePort(false);
            InIOError = true;
        }
        log(Level.TRACE, ID + msg);
        LastUsedCodePageIndex = LastUsedFontIndex = -1;
        Online = false;
    }

    private void setStatus(byte val) {
        boolean oldstate = DrawerOpen;
        synchronized (this) {
            DrawerOpen = (val & RespDrawerBit) == RespDrawerBit;
            if (oldstate != DrawerOpen) {
                while (StatusWaitList.size() > 0) {
                    StatusWaitList.get(0).signal();
                    StatusWaitList.remove(0);
                }
            }
        }
        val &= ~RespDrawerBit;
        if (!(PrinterError = val == RespError)) {
            if (val == RespCover) {
                CoverOpen = true;
                PaperState = PaperEnd;
            }
            else {
                CoverOpen = false;
                PaperState = val == RespOk ? PaperOk : (val == RespNearEnd ? PaperNearEnd : PaperEnd);
            }
        }
    }

    /**
     * Status request thread. Tries to send a status request every PollDelay milliseconds. Starts StatusHandler thread
     * after first request, even if successful. Stops StatusHandler before stopping itself.
     */
    class RequestSender extends Thread {
        /**
         * Flag to be set to force thread termination.
         */
        boolean ToBeFinished = false;
        private StatusHandler Handler = null;

        /**
         * Constructor. Sets thread name.
         */
        RequestSender() {
            super("RequestSender");
        }

        @Override
        public void run() {
            while (!ToBeFinished) {
                try {
                    sendCommand(Online ? CmdStatusRequest : getInitialSequence(CmdStatusRequest));
                } catch (JposException e) {}
                if (Handler == null) {
                    Handler = new StatusHandler();
                    Handler.start();
                }
                PollWaiter.suspend(PollDelay);
            }
            Handler.ToBeFinished = true;
            OfflineWaiter.signal();
            closePort(false);
            SendTask.ToBeFinished = true;
            waitThreadFinished(Handler);
        }
    }

    private void waitThreadFinished(Thread th) {
        while (true) {
            try {
                th.join();
                break;
            } catch (Exception e) {}
        }
    }

    private RequestSender SendTask;
    private SyncObject OfflineWaiter = new SyncObject();

    /**
     * Status handler thread, used for status check loop while device is opened.
     */
    class StatusHandler extends Thread {
        /**
         * Flag to be set to force thread termination.
         */

        boolean ToBeFinished = false;

        /**
         * Constructor. Sets thread name.
         */
        StatusHandler() {
            super("StatusHandler");
        }

        @Override
        public void run() {
            boolean firsttime = true;
            while (!ToBeFinished) {
                boolean offline = Online;
                boolean draweropen = DrawerOpen;
                boolean coveropen = CoverOpen;
                boolean inerror = PrinterError;
                int paperstate = PaperState;
                recvState(firsttime);
                handleStatusChange(offline, inerror, draweropen, coveropen, paperstate);
                if (StartWaiter != null) {
                    SyncObject obj = StartWaiter;
                    StartWaiter = null;
                    obj.signal();
                }
                firsttime = false;
            }
            closePort(true);
        }

        private void recvState(boolean firsttime) {
            UniqueIOProcessor out;
            synchronized (SocketSync) {
                out = OutStream;
            }
            int timeout = PollDelay * MaxRetry + MaxRetry + RequestTimeout;
            if (out == null) {
                OfflineWaiter.suspend(firsttime ? timeout : SyncObject.INFINITE);
                synchronized (SocketSync) {
                    out = OutStream;
                }
                if (out == null)
                    return;
            }
            String msg;
            try {
                int count = out.available();
                out.setTimeout(timeout);
                byte[] data = count > 0 ? out.read(count) : out.read(1);
                if (data != null && data.length > 0) {
                    Online = true;
                    setStatus(data[data.length - 1]);
                    return;
                }
                msg = ": No response from sample printer";
            } catch (JposException e) {
                msg = ": IO error: " + e.getMessage();
            }
            handleCommunicationError(msg);
        }
    }


    private byte[] getInitialSequence(byte[] data) {
        byte[] retdata = Arrays.copyOf(CmdNormalize, CmdNormalize.length + data.length);
        System.arraycopy(data, 0, retdata, CmdNormalize.length, data.length);
        return retdata;
    }

    private void handleStatusChange(boolean offline, boolean inerror, boolean draweropen, boolean coveropen, int state) {
        JposCommonProperties props = getPropertySetInstance(CashDrawers, 0, 0);
        if (offline != Online || inerror != PrinterError) {
            if (props != null) {
                try {
                    handleEvent(new CashDrawerStatusUpdateEvent(props.EventSource, !Online ? JposConst.JPOS_SUE_POWER_OFF : (PrinterError ? JposConst.JPOS_SUE_POWER_OFFLINE : JposConst.JPOS_SUE_POWER_ONLINE)));
                } catch (JposException e) {}
            }
            if ((props = getClaimingInstance(ClaimedPOSPrinter, 0)) != null) {
                try {
                    handleEvent(new POSPrinterStatusUpdateEvent(props.EventSource, !Online ? JposConst.JPOS_SUE_POWER_OFF : (PrinterError ? JposConst.JPOS_SUE_POWER_OFFLINE : JposConst.JPOS_SUE_POWER_ONLINE)));
                } catch (JposException e) {}
            }
        }
        if (Online && !PrinterError) {
            if (draweropen != DrawerOpen) {
                if ((props = getPropertySetInstance(CashDrawers, 0, 0)) != null) {
                    try {
                        handleEvent(new CashDrawerStatusUpdateEvent(props.EventSource, DrawerOpen ? CashDrawerConst.CASH_SUE_DRAWEROPEN : CashDrawerConst.CASH_SUE_DRAWERCLOSED));
                    } catch (JposException e) {}
                }
            }
            if (coveropen != CoverOpen) {
                if ((props = getClaimingInstance(ClaimedPOSPrinter, 0)) != null) {
                    try {
                        handleEvent(new POSPrinterStatusUpdateEvent(props.EventSource, CoverOpen ? POSPrinterConst.PTR_SUE_COVER_OPEN : POSPrinterConst.PTR_SUE_COVER_OK));
                    } catch (JposException e) {}
                }
            }
            if (state != PaperState) {
                if ((props = getClaimingInstance(ClaimedPOSPrinter, 0)) != null) {
                    try {
                        handleEvent(new POSPrinterStatusUpdateEvent(props.EventSource, PaperState == PaperOk ? POSPrinterConst.PTR_SUE_REC_PAPEROK : (PaperState == PaperEnd ? POSPrinterConst.PTR_SUE_REC_EMPTY : POSPrinterConst.PTR_SUE_REC_NEAREMPTY)));
                    } catch (JposException e) {}
                }
            }
        }
    }

    /**
     * Closes the port
     * @param doFlush Specifies whether the output stream shall be flushed befor close.
     * @return In case of an IO error, the corresponding exception. Otherwise null
     */
    private JposException closePort(boolean doFlush) {
        JposException e = null;
        if (OutStream != null) {
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
                } catch (JposException ee) {
                    e = ee;
                }
            }
            OutStream = null;
        }
        return e;
    }

    private void stopSendTask() {
        SendTask.ToBeFinished = true;
        PollWaiter.signal();
        waitThreadFinished(SendTask);
    }

    /**
     * Port initialization.
     * @return In case of initialization error, the exception. Otherwise null.
     */
    private JposException initPort() {
        try {
            if (!TcpType) {
                SerialIOProcessor ser;
                OutStream = ser = new SerialIOProcessor(this, ID);
                ser.setParameters(Baudrate, Databits, Stopbits, Parity);
            }
            else {
                TcpClientIOProcessor tcp;
                OutStream = tcp = new TcpClientIOProcessor(this, ID);
                tcp.setParam(OwnPort);
            }
            OutStream.setLoggingType(LoggingType);
            OutStream.open(InIOError);
            InIOError = false;
            OfflineWaiter.signal();
        } catch (JposException e) {
            OutStream = null;
            return e;
        }
        return null;
    }

    /**
     * Performs update of PowerState property from device internal status if DeviceEnabled has been set.
     * @param dev Property set to be updated.
     * @param enable New state for DeviceEnabled.
     */
    void updateCommonStates(JposCommonProperties dev, boolean enable) {
        if (enable) {
            if (dev.PowerNotify == JposConst.JPOS_PN_ENABLED) {
                dev.PowerState = !Online ? JposConst.JPOS_PS_OFF : (PrinterError ? JposConst.JPOS_PS_OFFLINE : JposConst.JPOS_PS_ONLINE);
            } else
                dev.PowerState = JposConst.JPOS_PS_UNKNOWN;
        }
    }

    /**
     * Increments OpenCount. If OpenCount = 1, communication with sample printer simulator will be started.
     * @param timeout Maximum time for communication startup
     * @return New value of OpenCount.
     */
    int startCommunication(int timeout) {
        synchronized (OpenCount) {
            if (OpenCount[0] == 0) {
                ToBeFinished = false;
                SyncObject obj = StartWaiter = new SyncObject();
                (SendTask = new RequestSender()).start();
                obj.suspend(timeout);
                OpenCount[0] = 1;
            }
            else
                OpenCount[0] = OpenCount[0] + 1;
            return OpenCount[0];
        }
    }

    /**
     * If OpenCount = 1, stops comminucation with sample printer. If OpenCount &gt; 0, decrements OpenCount.
     * @return New value of OpenCount.
     */
    int stopCommunication() {
        synchronized(OpenCount) {
            if (OpenCount[0] == 1) {
                SendTask.ToBeFinished = true;
                PollWaiter.signal();
                stopSendTask();
            }
            if (OpenCount[0] > 0)
                OpenCount[0] = OpenCount[0] - 1;
            return OpenCount[0];
        }
    }

    @Override
    public POSPrinterProperties getPOSPrinterProperties(int index) {
        return new Printer(this);
    }

    @Override
    public CashDrawerProperties getCashDrawerProperties(int index) {
        return new Drawer(this);
    }
}
