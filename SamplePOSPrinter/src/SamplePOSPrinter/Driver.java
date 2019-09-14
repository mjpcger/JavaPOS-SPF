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
import jpos.config.JposEntry;
import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.cashdrawer.*;
import de.gmxhome.conrad.jpos.jpos_base.posprinter.*;
import org.apache.log4j.Level;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.util.*;
import java.util.List;


/**
 * Implementation of a JposDevice based implementation of a sample printer that becomes
 * JavaPOS CashDrawer and POSPrinter device services in combination with the CashDrawerServer
 * and POSPrinterServer classes.
 */
public class Driver extends JposDevice{
    private UniqueIOProcessor OutStream;
    private Thread StateWatcher;
    private boolean ToBeFinished;

    // Byte arrays for printer commands.
    private final static byte LineFeed = '\12';
    private final static byte[] CmdCut = {'\14'};
    private byte[] CmdBold = {'\33', 'b'};
    private byte[] CmdColor = {'\33', 'c'};
    private final static byte[] CmdNormalize = {'\33', 'b', '0', '\33', 'c', '0', '\33', 'o', 'l', '\33', 'u', '0'};
    private final static byte[] CmdDrawerOpen = {'\33','d'};
    private byte[] CmdFont = {'\33', 'f'};
    private byte[] CmdOrientation = {'\33', 'o'};
    private byte[] CmdCodepage = {'\33', 'p'};
    private final static byte[] CmdStatusRequest = {'\33','s'};
    private byte[] CmdUnderline = {'\33', 'u'};

    private static final String CharSetList = "997,998,999,1250,1251,1252,1253,1254,1257";
    private static final int CharSetListVals[] = {POSPrinterConst.PTR_CS_UNICODE,POSPrinterConst.PTR_CS_ASCII, POSPrinterConst.PTR_CS_ANSI,1250,1251,1252,1253,1254,1257};
    private static final String LineCharsList = "42,56";
    private static final int LineCharsListVals[] = {42, 56};
    private static final byte[] CodePages = { '6', '0', '6', '0', '1', '2', '3', '4', '5' };    // Default encoding of Java is UFT-8
    private static final byte[] Fonts = { 'A', 'B' };
    private static final int[] CharWidths = { 12, 9 };
    private static final int[] LineHeights = { 24, 18 };
    private static final int[] LineSpacings = { 30, 23 };
    private int CurrentFontIndex = 0;
    private int LastUsedFontIndex = -1;
    private int CurrentCodePageIndex = 0;
    private int LastUsedCodePageIndex = -1;
    private static final int LineWidth = 512;
    private static final int KnifeOffset = 50;
    private final int[][] FactorMatrix = new int[][]{
            new int[]{POSPrinterConst.PTR_MM_DOTS, LineWidth},  // LineWidth dots per line
            new int[]{POSPrinterConst.PTR_MM_METRIC, 8000},     // 8000/100 mm per line
            new int[]{POSPrinterConst.PTR_MM_ENGLISH, 3150},    // 3150/1000 inch per line
            new int[]{POSPrinterConst.PTR_MM_TWIPS, 4535},      // 4535/1440 inch per line
    };

    // Constants for status response byte. Naming: "Resp" + printer state word + drawer state word
    private final static byte RespOk = '0';
    private final static byte RespNearEnd = '2';
    private final static byte RespEnd = '4';
    private final static byte RespCover = '6';
    private final static byte RespError = '8';
    private final static byte RespDrawerBit = 1;
    private int Baudrate = SerialIOProcessor.BAUDRATE_9600;
    private int Databits = SerialIOProcessor.DATABITS_8;
    private int Stopbits = SerialIOProcessor.STOPBITS_2;
    private int Parity = SerialIOProcessor.PARITY_NONE;
    private int OwnPort = 0;
    private int LoggingType = UniqueIOProcessor.LoggingTypeEscapeString;
    private int RequestTimeout = 500;
    private int PollDelay = 1000;
    private int MaxRetry = 2;
    private boolean UsbToSerial = false;
    private boolean MapCharacterSet = true;
    private boolean AdjustLineChars = true;
    private boolean InIOError = true;
    private boolean CoverOpen = false;
    private boolean PrinterError = false;
    private boolean Online = false;
    private boolean DrawerOpen = false;
    private int PaperState = PaperOk;
    private static final int PaperOk = 0;
    private static final int PaperNearEnd = 1;
    private static final int PaperEnd = 2;
    private final Integer[] OpenCount = new Integer[1];
    private long LastPollTick;
    private SyncObject StartWaiter;
    private List<SyncObject> StatusWaitList = new ArrayList<SyncObject>();

    // For testing:

    private int AsyncProcessingCommandDelay = 0;

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
        props.RecLineCharsListDef = LineCharsList;
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

    private SyncObject PollWaiter = new SyncObject();

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

    private void updateCommonStates(JposCommonProperties dev, boolean enable) {
        if (enable) {
            if (dev.PowerNotify == JposConst.JPOS_PN_ENABLED) {
                dev.PowerState = !Online ? JposConst.JPOS_PS_OFF : (PrinterError ? JposConst.JPOS_PS_OFFLINE : JposConst.JPOS_PS_ONLINE);
            } else
                dev.PowerState = JposConst.JPOS_PS_UNKNOWN;
        }
    }

    private int startCommunication(int timeout) {
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

    private int stopCommunication() {
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
        return new SamplePrinterPrinterAccessor(index);
    }

    private class SamplePrinterPrinterAccessor extends POSPrinterProperties {
        public SamplePrinterPrinterAccessor(int index) {
            super(index);
        }

        @Override
        public void claim(int timeout) throws JposException {
            startCommunication(timeout);
            super.claim(timeout);
            setCurrentValues();
        }

        private void setCurrentValues() {
            int index;
            for (index = 0; index < CharSetListVals.length; index++) {
                if (CharacterSet == CharSetListVals[index]) {
                    CurrentCodePageIndex = index;
                    break;
                }
            }
            for (index = 0; index < LineCharsListVals.length; index++) {
                if (RecLineChars > LineCharsListVals[index]) {
                    CurrentFontIndex = index;
                    break;
                }
            }
        }


        @Override
        public void release() throws JposException {
            super.release();
            stopCommunication();
        }

        @Override
        public void deviceEnabled(boolean enable) throws JposException {
            SyncObject obj = StartWaiter;
            if (obj != null)
                obj.suspend(SyncObject.INFINITE);
            super.deviceEnabled(enable);
            updateStates(enable);
        }

        private void updateStates(boolean enable) {
            updateCommonStates(this, enable);
            if (enable) {
                CoverOpen = CoverOpen;
                RecEmpty = PaperState == PaperEnd;
                RecNearEnd = PaperState > PaperOk;
            }
        }

        private class PrinterState {
            int CharsetIndex = CurrentFontIndex;
            int CodeIndex = CurrentCodePageIndex;
        }

        @Override
        public void checkHealth(int level) throws JposException {
            CheckHealthText = "Interactive CheckHealth: ";
            switch (level) {
                case JposConst.JPOS_CH_INTERNAL:
                    CheckHealthText += "Internal CheckHealth: OK.";
                    break;
                case JposConst.JPOS_CH_EXTERNAL:
                    CheckHealthText = "External CheckHealth: ";
                case JposConst.JPOS_CH_INTERACTIVE:
                    try {
                        ((POSPrinterService) EventSource).printImmediate(POSPrinterConst.PTR_S_RECEIPT, "\12\33|cA" + CheckHealthText + "OK.\12\33|fP");
                        CheckHealthText += "OK.";
                    } catch (JposException e) {
                        CheckHealthText += "Error: " + e.getMessage() + ".";
                    }
            }
            log(Level.DEBUG, LogicalName + ": CheckHealthText <- " + CheckHealthText);
            super.checkHealth(level);
        }

        @Override
        public void mapMode(int i) throws JposException {
            if (i != MapMode) {
                super.mapMode(i);
                RecLineSpacing = fromDotScale(LineSpacings[CurrentFontIndex]);
                RecLineWidth = fromDotScale((LineWidth / CharWidths[CurrentFontIndex]) * RecLineChars);
                RecLineHeight = fromDotScale(LineHeights[CurrentFontIndex]);
                log(Level.DEBUG, LogicalName + ": RecLineSpacing <- " + RecLineSpacing);
                log(Level.DEBUG, LogicalName + ": RecLineWidth <- " + RecLineWidth);
                log(Level.DEBUG, LogicalName + ": RecLineHeight <- " + RecLineHeight);
            }
        }

        private int fromDotScale(int dotval, int mapmode) {
            for (int[] vector : FactorMatrix) {
                if (vector[0] == mapmode) {
                    return dotval * vector[1] / LineWidth;
                }
            }
            return dotval;
        }

        private int fromDotScale(int dotval) {
            return fromDotScale(dotval, MapMode);
        }

        private int toDotScale(int mapval, int mapmode) {
            for (int[] vector : FactorMatrix) {
                if (vector[0] == mapmode) {
                    return mapval * LineWidth / vector[1];
                }
            }
            return mapval;
        }

        @Override
        public void characterSet(int value) throws JposException {
            int newindex;
            for (newindex = 0; newindex < CharSetListVals.length; newindex++) {
                if (value == CharSetListVals[newindex]) {
                    CurrentCodePageIndex = newindex;
                    break;
                }
            }
            super.characterSet(value);
        }

        @Override
        public void recLineChars(int value) throws JposException {
            int newindex;
            for (newindex = 0; newindex < LineCharsListVals.length; newindex++) {
                if (LineCharsListVals[newindex] >= value)
                    break;
            }
            if (newindex == LineCharsListVals.length)
                throw new JposException(JposConst.JPOS_E_ILLEGAL, "RecLineChars too high: " + value + ", maximum: " + LineCharsListVals[newindex - 1]);
            if (newindex != CurrentFontIndex) {
                RecLineSpacing = fromDotScale(LineSpacings[CurrentFontIndex = newindex]);
                RecLineWidth = fromDotScale((LineWidth / CharWidths[CurrentFontIndex]) * RecLineChars);
                RecLineHeight = fromDotScale(LineHeights[CurrentFontIndex]);
                RecLinesToPaperCut = (KnifeOffset + LineSpacings[CurrentFontIndex] - 1) / LineSpacings[CurrentFontIndex];
                log(Level.DEBUG, LogicalName + ": RecLineSpacing <- " + RecLineSpacing);
                log(Level.DEBUG, LogicalName + ": RecLineWidth <- " + RecLineWidth);
                log(Level.DEBUG, LogicalName + ": RecLineHeight <- " + RecLineHeight);
                log(Level.DEBUG, LogicalName + ": RecLinesToPaperCut <- " + RecLinesToPaperCut);
            }
            super.recLineChars(AdjustLineChars ? LineCharsListVals[CurrentFontIndex] : value);
        }

        @Override
        public void recLineHeight(int i) throws JposException {
            super.recLineHeight(fromDotScale(LineHeights[CurrentFontIndex]));
        }

        @Override
        public void recLineSpacing(int i) throws JposException {
            super.recLineSpacing(fromDotScale(LineSpacings[CurrentFontIndex]));
        }

        private POSPrinterService.PrintDataPart[] TopLogoData;
        private POSPrinterService.PrintDataPart[] BottomLogoData;

        @Override
        public POSPrinterService.PrintDataPart[] getLogoData(boolean top) {
            return top ? TopLogoData : BottomLogoData;
        }

        @Override
        public void setLogo(int location, String data) throws JposException {
            try {
                ((POSPrinterService)EventSource).validateData(POSPrinterConst.PTR_S_RECEIPT, data);
            } catch (JposException e) {
                if (e.getErrorCode() == JposConst.JPOS_E_FAILURE)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, e.getMessage(), e);
            }
            List<POSPrinterService.PrintDataPart> dataparts = ((POSPrinterService)EventSource).outputDataParts(data);
            Object o = null;
            for (int i = 0; i < dataparts.size(); i++) {
                o = dataparts.get(i);
                if (o instanceof POSPrinterService.EscLogo)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Data contains logo escape sequence");
            }
            if (location == POSPrinterConst.PTR_L_TOP) {
                TopLogoData = dataparts.toArray(new POSPrinterService.PrintDataPart[0]);
            }
            else {
                BottomLogoData = dataparts.toArray(new POSPrinterService.PrintDataPart[0]);
            }
        }

        @Override
        public void validateData(int station, POSPrinterService.PrintData data) throws JposException {
            checkNextMustFeed();
            super.validateData(station, data);
            if (data.getServiceIsMapping() && data.getCharacterSet() != POSPrinterConst.PTR_CS_UNICODE) {
                Charset charset = data.getCharacterSet() == POSPrinterConst.PTR_CS_ANSI ? Charset.defaultCharset() : Charset.forName(getCharsetString(data));
                CharsetEncoder encoder = charset.newEncoder();
                encoder.onMalformedInput(CodingErrorAction.REPORT);
                encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
                try {
                    encoder.encode(CharBuffer.wrap(data.getPrintData()));
                } catch (UnmappableCharacterException e) {
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Unmappable character", e);
                } catch (CharacterCodingException e) {
                    throw new JposException(JposConst.JPOS_E_FAILURE, "Malformed input", e);
                }
            }
        }

        private void checkNextMustFeed() throws JposException {
            if (NextMustFeed) {
                NextMustFeed = false;
                throw new JposException(JposConst.JPOS_E_FAILURE, "Carriage return without line feed not supported");
            }
            LastHasFed = false;
        }

        private String getCharsetString(POSPrinterService.PrintData data) {
            return data.getCharacterSet() == POSPrinterConst.PTR_CS_ASCII ? "ASCII" : (data.getCharacterSet() == POSPrinterConst.PTR_CS_UNICODE ? "UTF-8" : "cp" + data.getCharacterSet());
        }

        private boolean LastHasFed = false;
        private boolean NextMustFeed = false;

        @Override
        public void validateData(int station, POSPrinterService.ControlChar ctrl) throws JposException {
            if (!LastHasFed) {
                if (ctrl.getControlCharacter() == '\15')
                    NextMustFeed = true;
                else
                    LastHasFed = true;
            }
        }

        @Override
        public void validateData(int station, POSPrinterService.EscLogo esc) throws JposException {
            POSPrinterService.PrintDataPart[] logo = esc.getLogoData();
            int i;
            for (i = 0; i < logo.length - i; i++) {
                validateData(station, logo[i]);
            }
            POSPrinterService.PrintDataPart o;
            if (i < logo.length && (!((o = logo[i]) instanceof POSPrinterService.PrintData) || ((POSPrinterService.PrintData) o).getPrintData().length() > 0)) {
                validateData(station, o);
            }
        }

        @Override
        public void validateData(int station, POSPrinterService.EscFeed esc) throws JposException {
            NextMustFeed = false;
            if (esc.getReverse()) {
                LastHasFed = false;
                throw new JposException(JposConst.JPOS_E_FAILURE, "Reverse feeding not supported");
            }
            int count = esc.getCount();
            if (esc.getUnits()) {
                count = toDotScale(esc.getCount(), esc.getMapMode());
                if (++count % LineSpacings[CurrentFontIndex] > 2) {
                    LastHasFed = false;
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Unit feed not supported, feed to nearest line");
                }
                count /= LineSpacings[CurrentFontIndex];
            }
            if (count > 0)
                LastHasFed = true;
            else
                NextMustFeed = true;
        }

        @Override
        public void validateData(int station, POSPrinterService.EscEmbedded esc) throws JposException {
            checkNextMustFeed();
        }

        @Override
        public void validateData(int station, POSPrinterService.EscCut esc) throws JposException {
            super.validateData(station,esc);
        }

        private void checkInError() throws JposException {
            check(InIOError, JposConst.JPOS_E_FAILURE, "No connection to device");
            check(PrinterError, JposConst.JPOS_E_FAILURE, "Printer not operational");
            checkext(CoverOpen, POSPrinterConst.JPOS_EPTR_COVER_OPEN, "Printer cover open");
            checkext(PaperState == PaperEnd, POSPrinterConst.JPOS_EPTR_REC_EMPTY, "Paper end");
        }

        @Override
        public void cutPaper(CutPaper request) throws JposException {
            checkInError();
            new SyncObject().suspend(request.EndSync == null ? AsyncProcessingCommandDelay : 0);      // for testing
            sendCommand(CmdCut);
        }

        @Override
        public PrintNormal printNormal(int station, String data) throws JposException {
            PrintNormal request = super.printNormal(station, data);
            request.AdditionalData = new PrinterState();
            return request;
        }

        @Override
        public PrintImmediate printImmediate(int station, String data) throws JposException {
            PrintImmediate request = super.printImmediate(station, data);
            request.AdditionalData = new PrinterState();
            return request;
        }

        @Override
        public void printNormal(PrintNormal request) throws JposException {
            new SyncObject().suspend(request.EndSync == null ? AsyncProcessingCommandDelay : 0);      // for testing
            checkInError();
            List<POSPrinterService.PrintDataPart> dataparts = request.getData();
            byte[] binarydata = getBytes(dataparts, (PrinterState)request.AdditionalData);
            if (binarydata.length > 0) {
                if (Arrays.equals(CmdNormalize, Arrays.copyOf(binarydata, CmdNormalize.length)) || binarydata[0] == LineFeed) {
                    sendCommand(binarydata);
                }
                else {
                    byte[] tobesent = Arrays.copyOf(CmdNormalize, CmdNormalize.length + binarydata.length);
                    System.arraycopy(binarydata, 0, tobesent, CmdNormalize.length, binarydata.length);
                    sendCommand(tobesent);
                }
            }
        }

        private byte[] getBytes(List<POSPrinterService.PrintDataPart> dataparts, PrinterState statusData) throws JposException {
            ByteBuffer[] parts = new ByteBuffer[dataparts.size()];
            int totalsize = 0;
            for (int i = 0; i < parts.length; i++) {
                POSPrinterService.PrintDataPart data = dataparts.get(i);
                if (data instanceof POSPrinterService.PrintData) {
                    if ((parts[i] = getPrintData((POSPrinterService.PrintData) data, statusData)) != null)
                        totalsize += parts[i].limit();
                }
                else if (data instanceof POSPrinterService.ControlChar) {
                    if ((parts[i] = getNewline()) != null)
                        totalsize += parts[i].limit();
                }
                else if (data instanceof POSPrinterService.EscCut) {
                    if ((parts[i] = getCut((POSPrinterService.EscCut) data)) != null)
                        totalsize += parts[i].limit();
                }
                else if (data instanceof POSPrinterService.EscNormalize) {
                    if ((parts[i] = getNormalize()) != null)
                        totalsize += parts[i].limit();
                }
                else if (data instanceof POSPrinterService.EscLogo) {
                    if ((parts[i] = getLogo((POSPrinterService.EscLogo) data, statusData)) != null)
                        totalsize += parts[i].limit();
                }
                else if (data instanceof POSPrinterService.EscFeed) {
                    if ((parts[i] = getFeed((POSPrinterService.EscFeed) data)) != null)
                        totalsize += parts[i].limit();
                }
                else if (data instanceof POSPrinterService.EscEmbedded) {
                    if ((parts[i] = getEmbeddedBytes((POSPrinterService.EscEmbedded) data)) != null)
                        totalsize += parts[i].limit();
                }
                else if (data instanceof POSPrinterService.EscAlignment) {
                    if ((parts[i] = getAlignment((POSPrinterService.EscAlignment) data)) != null)
                        totalsize += parts[i].limit();
                }
                else if (data instanceof POSPrinterService.EscSimple) {
                    if ((parts[i] = getSimpleAttribute((POSPrinterService.EscSimple) data)) != null)
                        totalsize += parts[i].limit();
                }
                else if (data instanceof POSPrinterService.EscLine) {
                    if ((parts[i] = getUnderline((POSPrinterService.EscLine) data)) != null)
                        totalsize += parts[i].limit();
                }
                else if (data instanceof POSPrinterService.EscColor) {
                    if ((parts[i] = getColor((POSPrinterService.EscColor) data)) != null)
                        totalsize += parts[i].limit();
                }
            }
            byte[] binarydata = new byte[totalsize];
            int pos = 0;
            for (ByteBuffer buffer : parts) {
                if (buffer != null) {
                    System.arraycopy(buffer.array(), 0, binarydata, pos, buffer.limit());
                    pos += buffer.limit();
                }
            }
            return binarydata;
        }

        private ByteBuffer getPrintData(POSPrinterService.PrintData data, PrinterState statusData) {
            ByteBuffer databuffer;
            if (data.getServiceIsMapping()) {
                Charset charset = data.getCharacterSet() == POSPrinterConst.PTR_CS_ANSI ? Charset.defaultCharset() : Charset.forName(getCharsetString(data));
                CharsetEncoder encoder = charset.newEncoder();
                encoder.onMalformedInput(CodingErrorAction.IGNORE);
                encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
                encoder.replaceWith(new byte[]{' '});
                try {
                    databuffer = encoder.encode(CharBuffer.wrap(data.getPrintData()));
                } catch (CharacterCodingException e) {
                    databuffer = ByteBuffer.allocate(0);
                }
            }
            else {
                byte[] adddata = new byte[data.getPrintData().length()];
                for (int i = data.getPrintData().length() - 1; i >= 0; i--)
                    adddata[i] = (byte)data.getPrintData().charAt(i);
                databuffer = ByteBuffer.wrap(adddata);
            }
            if (statusData.CodeIndex != LastUsedCodePageIndex || statusData.CharsetIndex != LastUsedFontIndex) {
                ByteBuffer newbuffer = ByteBuffer.allocate(databuffer.position(0).limit()
                        + (statusData.CodeIndex != LastUsedCodePageIndex ? CmdCodepage.length + 1 : 0)
                        + (statusData.CharsetIndex != LastUsedFontIndex ? CmdFont.length + 1 : 0));
                if (statusData.CodeIndex != LastUsedCodePageIndex)
                    newbuffer.put(CmdCodepage).put(CodePages[LastUsedCodePageIndex = statusData.CodeIndex]);
                if (statusData.CharsetIndex != LastUsedFontIndex)
                    newbuffer.put(CmdFont).put(Fonts[LastUsedFontIndex = statusData.CharsetIndex]);
                databuffer = newbuffer.put(databuffer);
            }
            return databuffer;
        }

        private ByteBuffer getNewline() {
            return ByteBuffer.allocate(CmdNormalize.length + 1).put(LineFeed).put(CmdNormalize);
        }

        private ByteBuffer getCut(POSPrinterService.EscCut data) {
            POSPrinterService.EscCut cut = data;
            if (cut.getFeed() || cut.getStamp()) {
                ByteBuffer retbuffer = ByteBuffer.allocate(RecLinesToPaperCut + 1 + CmdNormalize.length);
                Arrays.fill(retbuffer.array(), 0, RecLinesToPaperCut, LineFeed);
                retbuffer.position(RecLinesToPaperCut);
                return retbuffer.put(CmdCut).put(CmdNormalize);
            }
            return ByteBuffer.allocate(1 + CmdNormalize.length).put(CmdCut).put(CmdNormalize);
        }

        private ByteBuffer getNormalize() {
            return ByteBuffer.wrap(CmdNormalize);
        }

        private ByteBuffer getLogo(POSPrinterService.EscLogo data, PrinterState status) throws JposException {
            List<POSPrinterService.PrintDataPart> logodata = new ArrayList<POSPrinterService.PrintDataPart>();
            POSPrinterService.PrintDataPart[] source = data.getLogoData();
            for (POSPrinterService.PrintDataPart part : source)
                logodata.add(part);
            return ByteBuffer.wrap(getBytes(logodata, status));
        }

        private ByteBuffer getFeed(POSPrinterService.EscFeed data) {
            if (!data.getReverse()) {
                int count = data.getCount();
                if (data.getUnits()) {
                    count = (toDotScale(count, data.getMapMode()) + LineSpacings[CurrentFontIndex] / 2) / LineSpacings[CurrentFontIndex];
                }
                if (count == 0)
                    count++;
                ByteBuffer retbuffer = ByteBuffer.allocate(count + CmdNormalize.length);
                Arrays.fill(retbuffer.array(), 0, count, LineFeed);
                retbuffer.position(count);
                return retbuffer.put(CmdNormalize);
            }
            return null;
        }

        private ByteBuffer getEmbeddedBytes(POSPrinterService.EscEmbedded data) {
            ByteBuffer newdata = ByteBuffer.allocate(data.getData().length());
            for (int i = data.getData().length() - 1; i >= 0; --i)
                newdata.put((byte) data.getData().charAt(i));
            return newdata;
        }

        private ByteBuffer getAlignment(POSPrinterService.EscAlignment data) {
            return ByteBuffer.allocate(CmdOrientation.length + 1).put(CmdOrientation).put((byte)(data.getAlignment() == POSPrinterConst.PTR_BC_LEFT ? 'l' : (data.getAlignment() == POSPrinterConst.PTR_BC_CENTER ? 'c' : 'r')));
        }

        private ByteBuffer getSimpleAttribute(POSPrinterService.EscSimple data) {
            if (data.getBold()) {
                return ByteBuffer.allocate(CmdBold.length + 1).put(CmdBold).put((byte)(data.getActivate() ? '1' : '0'));
            }
            return null;
        }

        private ByteBuffer getUnderline(POSPrinterService.EscLine data) {
            if (data.getUnderline()) {
                return ByteBuffer.allocate(CmdUnderline.length + 1).put(CmdUnderline).put((byte)(data.getThickness() != 0 ? '1' : '0'));
            }
            return null;
        }

        private ByteBuffer getColor(POSPrinterService.EscColor data) {
            if (!data.getRgb()) {
                return ByteBuffer.allocate(CmdColor.length + 1).put(CmdColor).put((byte)(data.getColor() != POSPrinterConst.PTR_COLOR_PRIMARY ? '1' : '0'));
            }
            return null;
        }

        @Override
        public void transactionPrint(TransactionPrint request) throws JposException {
            if (request.getControl() == POSPrinterConst.PTR_TP_NORMAL) {
                SyncObject obj = StartWaiter = new SyncObject();
                PollWaiter.signal();
                obj.suspend(SyncObject.INFINITE);
                check(!Online, JposConst.JPOS_E_FAILURE, "Device off");
                check(PrinterError, JposConst.JPOS_E_FAILURE, "Device offline");
                checkext(CoverOpen, POSPrinterConst.JPOS_EPTR_COVER_OPEN, "Cover open");
                checkext(PaperState == PaperEnd, POSPrinterConst.JPOS_EPTR_REC_EMPTY, "Cover open");

            }
        }
    }

    @Override
    public CashDrawerProperties getCashDrawerProperties(int index) {
        return new SamplePrinterDrawerAccessor(index);
    }

    private class SamplePrinterDrawerAccessor extends CashDrawerProperties {
        SamplePrinterDrawerAccessor(int index) {
            super(index);
        }

        @Override
        public void deviceEnabled(boolean enable) throws JposException {
            super.deviceEnabled(enable);
            updateStates(enable);
        }

        private void updateStates(boolean enable) {
            updateCommonStates(this, enable);
            if (enable) {
                DrawerOpened = DrawerOpen;
            }
        }

        @Override
        public void open() throws JposException {
            initOnOpen();
            startCommunication((int)SyncObject.INFINITE);
            State = JposConst.JPOS_S_IDLE;
        }

        @Override
        public void close() throws JposException {
            super.close();
            stopCommunication();
        }

        @Override
        public void checkHealth(int level) throws JposException {
            String healthError = "";

            if (level == JposConst.JPOS_CH_INTERACTIVE) {
                synchronizedMessageBox("Press OK to start health test.", "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
            }
            try {
                if (level != JposConst.JPOS_CH_INTERNAL) {
                    if (level == JposConst.JPOS_CH_INTERACTIVE) {
                        healthError = "Interactive CheckHealth: ";
                        ((CashDrawerService)EventSource).openDrawer();
                        if (DrawerOpened) {
                            healthError += "Opened ";
                            synchronizedMessageBox("Close drawer and Press OK to stop health test.", "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
                            healthError += DrawerOpened ? "Failed " : "Closed ";
                        }
                        else
                            healthError += "Failed ";
                    } else {
                        healthError = "External CheckHealth: ";
                        ((CashDrawerService)EventSource).openDrawer();
                        healthError += "Opened ";
                        ((CashDrawerService)EventSource).waitForDrawerClose(5000, 500, 450, 200);
                        healthError += "Closed ";
                    }
                }
                else
                    healthError = "Internal CheckHealth: ";
            } catch (JposException e) {
                healthError += "Failed ";
            }
            CheckHealthText = healthError + (healthError.matches(".*Fail.*") ? "ERROR." : "OK.");
            EventSource.logSet("CheckHealthText");
            super.checkHealth(level);
        }


        @Override
        public void openDrawer() throws JposException {
            if (!Online)
                throw new JposException(JposConst.JPOS_E_ILLEGAL, "Drawer not accessible");
            sendCommand(CmdDrawerOpen);
            PollWaiter.signal();
            waitStatusChange(true, RequestTimeout);
            DrawerOpened = DrawerOpen;
            super.openDrawer();
        }

        @Override
        public void waitForDrawerClose(int beepTimeout, int beepFrequency, int beepDuration, int beepDelay) throws JposException {
            int timeout = beepTimeout;
            boolean beeping = false;

            if (DrawerOpen) {
                while (!waitStatusChange(false, timeout)) {
                    if (beeping) {
                        timeout = beepDelay;
                    } else {
                        timeout = beepDuration;
                    }
                    if (beeping = !beeping) {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }
            super.waitForDrawerClose(beepTimeout, beepFrequency, beepDuration, beepDelay);
        }
    }
}
