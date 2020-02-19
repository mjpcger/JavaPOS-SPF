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

package SampleRemoteOrderDisplay;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.remoteorderdisplay.*;
import jpos.JposConst;
import jpos.JposException;
import jpos.RemoteOrderDisplayConst;
import jpos.config.JposEntry;
import org.apache.log4j.Level;

import java.util.Arrays;

import static SampleRemoteOrderDisplay.DisplayContents.*;

/**
 * Implementation of RemoteOrderDisplay based for the sample implemented in Device.tcl.
 * Supported features are:
 * <br>- One clock per display unit.
 * <br>- One buffer per display unit.
 * <br>- No tone, no character set selection, only one (default) video mode.
 * <br>- clock overlays text, text changes do not overwrite the clock as long as clock has not been stopped.
 * <br>- text does not overwrite drawn lines. Drawn lines can only be removed by clearVideo, clearVideoRegion or
 * resetVideo.
 * <br>- A box around the clock can become part of the clock if it was drawn before starting the clock with the
 * clock specific dimensions (height 1, width 5, 5 or eight, depending on clock type). In that case, the box remains
 * part of the clock, even if it will be cleared by clearVideoRegion.
 * <br>- If the simulator performs a clock update cycle between reading out the current time and restarting the clock
 * at the new position in method controlClock with function CLK_MOVE, the time will not be updated.
 * <br>For each display unit, the whole contents of the display will be buffered in the service. For each character,
 * a foreground and background color and a character value will be stored. In addition, if a a line has been drawn above,
 * below, on the left or right side of the character, the line color will be stored as well. This is necessary to be
 * able to implement save, restore, copy and move instructions, for normal text as well as for the clock.
 */
public class Device extends JposDevice implements Runnable {
    /**
     * Extended error code that has not yet defined within the JavaPOS framework. The value (200) must be replaced by
     * RemoteOrderDisplayConst.JPOS_EROD_NOUNITS when this constant will be defined in future.
     */
    public final static int JPOS_EROD_NOUNITS = 200;

    private int OwnPort = 0;                // Default: OS generated random port
    private int RequestTimeout = 1000;      // Default: Service specific value
    private int CharacterTimeout = 50;      // Default: Service specific value for maximum delay between bytes belonging
    // to the same frame
    private int MinClaimTimeout = 100;      // Minimum claim timeout, to be used whenever timeout in claim() method is
    // too small to guarantee correct working of service, even if everything
    // is OK.
    RemoteOrderDisplayService Service;          // UPOS service instance

    private final static int MAX_UNITS = 5;     // Simulator supports up to 5 displays
    private final static int MAX_LINES = 20;    // Simulator supports up to 20 lines per unit
    private final static int MAX_COLUMNS = 25;  // Simulator supports up to 25 characters per line for each unit
    private DisplayContents Display = new DisplayContents();
    // colors are the same as specified by UPOS, plus intensity, resulting in 16 colors, where only 8 colors are allowd
    // for background (configurable intensity).
    private int BackgroundIntensity = RemoteOrderDisplayConst.ROD_ATTR_INTENSITY;
    private int CursorColor = RemoteOrderDisplayConst.ROD_ATTR_FG_BLACK;
    // Communication properties
    private TcpClientIOProcessor OutStream = null;
    private Thread StateWatcher;
    private SyncObject WaitInitialized;
    private SyncObject WaitClockData = new SyncObject();
    private boolean ToBeFinished;
    private boolean InIOError = false;
    private int Online = 0;
    private int UnitOnline = 0;
    // Frame layout constants
    private final static byte StateCmd = 'O';
    private final static byte UpCmd = 'U';
    private final static byte DownCmd = 'D';
    private final static byte ClockCmd = 'T';
    private final static byte ETX = 3;
    private final static int StateLen = 5;      // <unit>O<onoff>ETX, where <unit> = 01 ... 05 and <onoff> 0 or 1
    private final static int TouchLen = 8;      // <unit><UorD><row><column>ETX, where <unit> = 00 ... 05, <UorD> U or D, <row> and <column> 2-digit decimal numbers (00 - 99)

    /**
     * Constructor. ID is the network address of the remote order display controller.
     * The controller is the instance that controls the communication between the application and up to 32 displays, it
     * is some kind of hub or so. In case of the sample device, up to 5 displays of identical types are supported. Other
     * examples might consist of a controller that controls several completely different devices, some might support
     * touch events, some might support tone output, all might support completely different dimensions.
     * @param id    Network address of the display controller.
     */
    protected Device(String id) {
        super(id);
        remoteOrderDisplayInit(1);
        PhysicalDeviceDescription = "Coin Dispenser Simulator for TCP";
        PhysicalDeviceName = "Coin Dispenser Simulator";
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
            if ((o = entry.getPropertyValue("BackgroundIntensity")) != null)
                BackgroundIntensity = Boolean.parseBoolean(o.toString()) ? RemoteOrderDisplayConst.ROD_ATTR_INTENSITY : 0;
            if ((o = entry.getPropertyValue("CursorColor")) != null && (value = Integer.parseInt(o.toString())) >= 0 && value <= 0xf)
                CursorColor = value;
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property", e);
        }
    }

    @Override
    public void changeDefaults(RemoteOrderDisplayProperties props) {
        props.DeviceServiceDescription = "RemoteOrderDisplay service for remote order display simulator";
        props.CapTone = false;
        props.CharacterSetDef = RemoteOrderDisplayConst.ROD_CS_UNICODE;
        props.SystemClocksDef = UNITCOUNT;
        props.SystemVideoSaveBuffersDef = UNITCOUNT * BUFFERIDLIMIT;
        props.VideoModeDef = 1;
        props.VideoModesListDef = "1:20x25x16C";
        props.EventTypeDef = RemoteOrderDisplayConst.ROD_DE_TOUCH_UP|RemoteOrderDisplayConst.ROD_DE_TOUCH_DOWN;
        props.TimeoutDef = RequestTimeout;
        props.DeviceServiceVersion = 1014001;
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
        } catch (JposException e) {
            OutStream = null;
            return e;
        }
        return null;
    }

    /**
     * Method to perform any command, Keep in mind that commands normally generate no response.
     * @param command Command data to be sent, without final ETX.
     * @return JposException in error case, null if no error occurred.
     */
    synchronized protected JposException sendCommand(String command) {
        if (OutStream == null) {
            JposException e = initPort();
            if (e != null) {
                return e;
            }
        }
        byte[] request = (command + "\3").getBytes();
        try {
            OutStream.write(request);
            return null;
        } catch (JposException e) {
            log(Level.TRACE, ID + ": IO error: " + e.getMessage());
            closePort(false);
            InIOError = true;
            return e;
        }
    }

    /**
     * Main thread, reads frames from simulator and generates the corresponding events (coordinates or power state for
     * specific display unit)
     */
    @Override
    public void run() {
        while (!ToBeFinished) {
            TcpClientIOProcessor instream;
            synchronized(this) {
                instream = getDisplayControllerStream();
            }
            if (instream != null) {
                try {
                    instream.setTimeout(Integer.MAX_VALUE);
                    byte[] data = instream.read(StateLen);
                    if (data.length == StateLen) {
                        int unitidx = Integer.parseInt(new String(data).substring(0, 2)) - 1;
                        int unit = 1 << (unitidx);
                        if (unit != 0) {
                            if (data[2] == UpCmd || data[2] == DownCmd) {
                                data = handleClickMessage(instream, data, unit);
                            } else if (data[2] == ClockCmd) {
                                // Clock data handling
                                data = handleClockMessage(instream, data, Display.Unit[unitidx]);
                            } else if (data[2] == StateCmd && data[4] == ETX) {
                                data = handleStatusMessage(data[3], unitidx, unit);
                            }
                        }
                    }
                    if (data != null)
                        log(Level.DEBUG, getClaimingInstance(ClaimedRemoteOrderDisplay, 0).LogicalName + ": Insufficient input: " + new String(data));
                } catch (Exception e) {
                    e.printStackTrace();
                    log(Level.TRACE, ID + ": IO error: " + e.getMessage());
                    handeSocketError();
                }
            }
        }
    }

    private void handeSocketError() {
        closePort(false);
        InIOError = true;
        RemoteOrderDisplayProperties disp = (RemoteOrderDisplayProperties)getClaimingInstance(ClaimedRemoteOrderDisplay, 0);
        if (disp.UnitsOnline != 0) {
            for (int i = 0; disp.UnitsOnline != 0; i++) {
                int unit = 1 << i;
                if ((disp.UnitsOnline & unit) != 0) {
                    Display.Unit[i].CursorActive = false;
                    disp.UnitsOnline &= ~unit;
                }
            }
            disp.EventSource.logSet("UnitsOnline");
        }
        try {
            handleEvent(new RemoteOrderDisplayStatusUpdateEvent(disp.EventSource, JposConst.JPOS_PS_OFF_OFFLINE, UnitOnline));
        } catch (JposException e1) {
            e1.printStackTrace();
        } finally {
            UnitOnline = 0;
        }
    }

    private TcpClientIOProcessor getDisplayControllerStream() {
        TcpClientIOProcessor instream;
        if ((instream = OutStream) == null) {
            instream = initPort() == null ? OutStream : null;
        }
        else if (WaitInitialized != null) {
            SyncObject waiter = WaitInitialized;
            WaitInitialized = null;
            waiter.signal();
        }
        return instream;
    }

    private byte[] handleClickMessage(TcpClientIOProcessor instream, byte[] data, int unit) throws JposException {
        instream.setTimeout(CharacterTimeout);
        byte[] rest = instream.read(TouchLen - data.length);
        if (rest.length == TouchLen - data.length && rest[rest.length - 1] == ETX) {
            data = Arrays.copyOf(data, data.length + rest.length);
            System.arraycopy(rest, 0, data, data.length - rest.length, rest.length);
            // Generate data event
            int col = Integer.parseInt(new String(data).substring(3, 5)) - 1;
            int row = Integer.parseInt(new String(data).substring(5, 7)) - 1;
            int type = data[2] == UpCmd ? RemoteOrderDisplayConst.ROD_DE_TOUCH_UP : RemoteOrderDisplayConst.ROD_DE_TOUCH_DOWN;
            RemoteOrderDisplayProperties disp = (RemoteOrderDisplayProperties)getClaimingInstance(ClaimedRemoteOrderDisplay, 0);
            if ((disp.EventType & RemoteOrderDisplayConst.ROD_DE_TOUCH_UP) != 0 && data[2] == UpCmd ||
                    (disp.EventType & RemoteOrderDisplayConst.ROD_DE_TOUCH_DOWN) != 0 && data[2] == DownCmd) {
                handleEvent(new RemoteOrderDisplayDataEvent(disp.EventSource, (((row << 8) + col) << 16) + type, unit));
            }
            data = null;
        }
        return data;
    }

    private byte[] handleClockMessage(TcpClientIOProcessor instream, byte[] data, DisplayUnit displayUnit) throws JposException {
        instream.setTimeout(CharacterTimeout);
        int partlen = data[4] == ETX ? 0 : (data[4] == ':' ? 3 : 4);
        if (partlen > 0) {
            byte[] part = instream.read(partlen);
            data = Arrays.copyOf(data, data.length + part.length);
            System.arraycopy(part, 0, data, data.length - part.length, part.length);
            if (part.length == partlen && part[partlen - 1] == ':') {
                part = instream.read(partlen = 3);
                data = Arrays.copyOf(data, data.length + part.length);
                System.arraycopy(part, 0, data, data.length - part.length, part.length);
            }
            if (data[data.length - 1] == ETX && displayUnit.SaveBuffer[0] != null && data.length == displayUnit.SaveBuffer[0][0].length + 4) {
                for (int i = 3; data[i] != ETX; i++) {
                    displayUnit.SaveBuffer[0][0][i - 3].Value = (char)data[i];
                }
                WaitClockData.signal();
                data = null;
            }
        }
        else {
            displayUnit.SaveBuffer[0] = null;
            data = null;
        }
        return data;
    }

    private byte[] handleStatusMessage(byte datum, int unitidx, int unit) throws JposException {
        // Generate status update event
        RemoteOrderDisplayProperties disp = (RemoteOrderDisplayProperties)getClaimingInstance(ClaimedRemoteOrderDisplay, 0);
        if (datum == '1') {
            UnitOnline |= unit;
            disp.Unit[unitidx].Clocks = 1;
            disp.Unit[unitidx].CapTouch = true;
            disp.Unit[unitidx].VideoModesList = disp.VideoModesListDef;
            disp.Unit[unitidx].CharacterSetList = String.valueOf(disp.CharacterSetDef);
            disp.Unit[unitidx].VideoSaveBuffers = BUFFERIDLIMIT;
            if (disp.CurrentUnitID == unit) {
                disp.copyIn();
            }
        } else
            UnitOnline &= ~unit;
        if (disp.DeviceEnabled) {
            int status = JposConst.JPOS_PS_OFF_OFFLINE;
            if (datum == '1') {
                disp.UnitsOnline |= unit;
                status = JposConst.JPOS_PS_ONLINE;
            } else {
                Display.Unit[unitidx].CursorActive = false;
                disp.UnitsOnline &= ~unit;
            }
            disp.EventSource.logSet("UnitsOnline");
            handleEvent(new RemoteOrderDisplayStatusUpdateEvent(disp.EventSource, status, unit));
        }
        return null;
    }

    @Override
    public RemoteOrderDisplayProperties getRemoteOrderDisplayProperties(int index) {
        return new SampleRODisplayAccessor();
    }

    /**
     * Sample device specific accessor class. The device is very simple:
     * <br>- Connect via TCP
     * <br>- Status (online / offline) comes automatically when changed and after connect.
     * <br>- Touch up / down events come automatically whenever a display has been touched.
     * <br>Commands are simple as well: After they have been sent, they will be executed (if possible) without any
     * response. Following features are not supported:
     * <br>- No tone (CapTone is always false).
     * <br>- No character set selection (CapSetCharacterSet always false).
     * <br>- No character mapping (device supports only unicode and the simulator supports UFT-8).
     * <br>- No clocks support (SystemClocks and Clocks are zero).
     * <br>- No video buffers supported (SystemVideoSaveBuffers and VideoSaveBuffers are zero).
     * <br>- Only one video mode supported (1:20x25x16C).
     * <br>- No cursor (cursor always off).
     * <br>Copy video region is restricted: Box border lines will be copied only if the box is completely within the
     * range to be copied.
     */
    class SampleRODisplayAccessor extends RemoteOrderDisplayProperties {
        /**
         * Constructor. Uses device index 0 implicitly because sample implementation supports only one base station.
         */
        public SampleRODisplayAccessor() {
            super(0);
        }

        @Override
        public void open() throws JposException {
            Service = (RemoteOrderDisplayService) EventSource;
            super.open();
        }

        @Override
        public void claim(int timeout) throws JposException {
            if (timeout < MinClaimTimeout)
                timeout = MinClaimTimeout;
            super.claim(timeout);
            SyncObject initsync = new SyncObject();
            ToBeFinished = false;
            WaitInitialized = initsync;
            UnitOnline = 0;
            (StateWatcher = new Thread(SampleRemoteOrderDisplay.Device.this)).start();
            initsync.suspend(timeout);
            if (WaitInitialized != null || InIOError) {
                release();
                throw new JposException(JposConst.JPOS_E_NOHARDWARE, "Remote order display controller not detected");
            }
        }

        @Override
        public void deviceEnabled(boolean enable) throws JposException {
            if (enable != DeviceEnabled) {
                super.deviceEnabled(enable);
                if (enable) {
                    UnitsOnline = UnitOnline;
                    EventSource.logSet("UnitsOnline");
                    if (UnitOnline != 0) {
                        handleEvent(new RemoteOrderDisplayStatusUpdateEvent(EventSource, JposConst.JPOS_PS_ONLINE, UnitOnline));
                    }
                }
            }
        }

        @Override
        public void handlePowerStateOnEnable() throws JposException {
            int nowoffline = UnitsOnline & ~UnitOnline;
            int nowonline = ~UnitsOnline & UnitOnline;
            UnitsOnline = UnitOnline;
            if (nowoffline != 0)
                handleEvent(new RemoteOrderDisplayStatusUpdateEvent(EventSource, JposConst.JPOS_SUE_POWER_OFF_OFFLINE, nowoffline));
            if (nowonline != 0)
                handleEvent(new RemoteOrderDisplayStatusUpdateEvent(EventSource, JposConst.JPOS_SUE_POWER_ONLINE, nowonline));
        }

        @Override
        public void release() throws JposException {
            ToBeFinished = true;
            synchronized (SampleRemoteOrderDisplay.Device.this) {
                closePort(false);
            }
            while (ToBeFinished) {
                try {
                    StateWatcher.join();
                } catch (Exception e) {
                }
                break;
            }
            StateWatcher = null;
            Online = UnitOnline = 0;
            PowerState = JposConst.JPOS_PS_UNKNOWN;
            EventSource.logSet("PowerState");
            UnitsOnline = 0;
            EventSource.logSet("UnitsOnline");
            InIOError = false;
            super.release();
        }

        @Override
        public void checkHealth(int level) throws JposException {
            String what = level == JposConst.JPOS_CH_INTERNAL ? "Internal" : (level == JposConst.JPOS_CH_EXTERNAL ? "External" : "Interactive");
            Service.check(CurrentUnitID == 0, 0, JposConst.JPOS_E_EXTENDED, JPOS_EROD_NOUNITS, "Unit ID not set");
            Service.check((CurrentUnitID & UnitsOnline) == 0, CurrentUnitID, JposConst.JPOS_E_FAILURE, 0, "Unit " + CurrentUnitID + " offline");
            try {
                if (level != JposConst.JPOS_CH_INTERNAL) {
                    Service.displayData(CurrentUnitID, 0, 0, RemoteOrderDisplayConst.ROD_ATTR_BG_GRAY | RemoteOrderDisplayConst.ROD_ATTR_FG_BLACK, "CheckHealth OK");
                    CheckHealthText = what + " CheckHealth: OK";
                } else
                    CheckHealthText = "Internal CheckHealth: OK";
            } catch (JposException e) {
                CheckHealthText = what + " Checkhealth: Error: " + e.getMessage();
            }
            EventSource.logSet("CheckHealthText");
        }

        @Override
        public void clearVideo(ClearVideo request) throws JposException {
            int color = ((request.getAttributes() >> 4) & 7) | BackgroundIntensity;
            int units = request.getUnits();
            for (int i = 0; i < Display.Unit.length; i++) {
                if ((units & (1 << i)) != 0) {
                    sendCheckCommand(request, String.format("%02dRC%02d%02d%02d%02d%02d%1d", i + 1, 1, 1, Display.Unit[i].Attribute[1].length, Display.Unit[i].Attribute.length, color, 1));
                    for (int l = 0; l < Display.Unit[i].Attribute.length; l++) {
                        for (int c = 0; c < Display.Unit[i].Attribute[l].length; c++) {
                            Display.Unit[i].Attribute[l][c].BackgroundColor = color;
                            Display.Unit[i].Attribute[l][c].ForegroundColor = color;
                            Display.Unit[i].Attribute[l][c].Value = ' ';
                            Display.Unit[i].Attribute[l][c].LeftBorderColor = Display.NOT_PRESENT;
                            Display.Unit[i].Attribute[l][c].TopBorderColor = Display.NOT_PRESENT;
                            Display.Unit[i].Attribute[l][c].RightBorderColor = Display.NOT_PRESENT;
                            Display.Unit[i].Attribute[l][c].BottomBorderColor = Display.NOT_PRESENT;
                        }
                    }
                    Display.Unit[i].SaveBuffer[0] = null;
                }
            }
        }

        @Override
        public ClearVideoRegion clearVideoRegion(int units, int row, int column, int height, int width, int attribute) throws JposException {
            Service.check(column + width > COLUMNCOUNT || row + height > LINECOUNT, units, JposConst.JPOS_E_ILLEGAL, 0, "Region out of range");
            return super.clearVideoRegion(units, row, column, height, width, attribute);
        }

        @Override
        public void clearVideoRegion(ClearVideoRegion request) throws JposException {
            int color = ((request.getAttributes() >> 4) & 7) | BackgroundIntensity;
            int units = request.getUnits();
            for (int i = 0; i < Display.Unit.length; i++) {
                if ((units & (1 << i)) != 0) {
                    boolean clockInRegion = Display.Unit[i].SaveBuffer[0] != null && request.getRow() <= Display.Unit[i].ClockRow && Display.Unit[i].ClockRow < request.getRow() + request.getHeight();
                    if (clockInRegion && (request.getColumn() + request.getWidth() <= Display.Unit[i].ClockColumn || Display.Unit[i].ClockColumn + Display.Unit[i].SaveBuffer[0][0].length <= request.getColumn()))
                        clockInRegion = false;
                    if (clockInRegion && Display.Unit[i].ClockSuspendTick == 0) {
                        while (WaitClockData.suspend(0)) ;
                        sendCheckCommand(units, String.format("%02dST%02d%02d%1d%d", i + 1, 0, 0, 0, 0));
                        WaitClockData.suspend(RequestTimeout);
                    }
                    sendCheckCommand(request, String.format("%02dRC%02d%02d%02d%02d%02d%1d", i + 1, request.getRow() + 1, request.getColumn() + 1, request.getWidth(), request.getHeight(), color, 1));
                    if (clockInRegion) {
                        updateRegion(request, i, Display.Unit[i].SaveBuffer[0], Display.Unit[i].ClockRow, Display.Unit[i].ClockColumn, true);
                        sendCheckCommand(units, String.format("%02dST%02d%02d%1d%d", i + 1, Display.Unit[i].ClockRow + 1, Display.Unit[i].ClockColumn + 1, Display.Unit[i].ClockType, 1));
                    }
                    for (int l = request.getRow(); l < request.getRow() + request.getHeight(); l++) {
                        for (int c = request.getColumn(); c < request.getColumn() + request.getWidth(); c++) {
                            Display.Unit[i].Attribute[l][c].BackgroundColor = color;
                            Display.Unit[i].Attribute[l][c].ForegroundColor = color;
                            Display.Unit[i].Attribute[l][c].Value = ' ';
                            Display.Unit[i].Attribute[l][c].LeftBorderColor = Display.NOT_PRESENT;
                            Display.Unit[i].Attribute[l][c].TopBorderColor = Display.NOT_PRESENT;
                            Display.Unit[i].Attribute[l][c].RightBorderColor = Display.NOT_PRESENT;
                            Display.Unit[i].Attribute[l][c].BottomBorderColor = Display.NOT_PRESENT;
                        }
                    }
                }
            }
        }

        @Override
        public void freeVideoRegion(int units, int bufferId) throws JposException {
            Service.check(bufferId > BUFFERIDLIMIT, units, JposConst.JPOS_E_ILLEGAL, 0, "Invalid buffer id " + bufferId);
            for (int i = 0; i < Display.Unit.length; i++) {
                if ((units & (1 << i)) != 0) {
                    synchronized (Display.Unit[i].SaveBuffer) {
                        Display.Unit[i].SaveBuffer[bufferId] = null;
                    }
                }
            }
        }

        @Override
        public void restoreVideoRegion(RestoreVideoRegion request) throws JposException {
            int noregion = 0;
            int outofrange = 0;
            for (int i = 0; i < Display.Unit.length; i++) {
                if ((request.getUnits() & (1 << i)) != 0) {
                    CharAttributes[][] range;
                    synchronized (Display.Unit[i].SaveBuffer) {
                        range = Display.Unit[i].SaveBuffer[request.getBufferId()];
                    }
                    if (range == null) {
                        noregion |= 1 << i;
                        continue;
                    }
                    int width = range[0].length;
                    int height = range.length;
                    if (request.getTargetColumn() + width > COLUMNCOUNT || request.getTargetRow() + height > LINECOUNT) {
                        outofrange |= 1 << i;
                    }
                }
            }
            Service.check(noregion != 0, noregion, JposConst.JPOS_E_EXTENDED, RemoteOrderDisplayConst.JPOS_EROD_NOREGION, "No region saved for buffer ID " + request.getBufferId() + " for units " + Integer.toString(noregion, 0x10));
            Service.check(outofrange != 0, outofrange, JposConst.JPOS_E_ILLEGAL, 0, "Target Region out of range for units " + Integer.toString(outofrange, 0x10));
            for (int i = 0; i < Display.Unit.length; i++) {
                if ((request.getUnits() & (1 << i)) != 0) {
                    CharAttributes[][] range;
                    synchronized (Display.Unit[i].SaveBuffer) {
                        range = Display.Unit[i].SaveBuffer[request.getBufferId()];
                    }
                    Service.check(range == null, 1 << i, JposConst.JPOS_E_EXTENDED, RemoteOrderDisplayConst.JPOS_EROD_NOREGION, "No region saved for buffer ID " + request.getBufferId() + " for unit " + Integer.toString(1 << i, 0x10));
                    updateRegion(request, i, range, request.getTargetRow(), request.getTargetColumn(), false);
                }
            }
        }

        @Override
        public SaveVideoRegion saveVideoRegion(int units, int row, int column, int height, int width, int bufferId) throws JposException {
            Service.check(column + width > COLUMNCOUNT || row + height > LINECOUNT, units, JposConst.JPOS_E_ILLEGAL, 0, "Source Region out of range");
            Service.check(bufferId > BUFFERIDLIMIT, units, JposConst.JPOS_E_ILLEGAL, 0, "Invalid buffer id " + bufferId);
            return super.saveVideoRegion(units, row, column, height, width, bufferId);
        }

        @Override
        public void saveVideoRegion(SaveVideoRegion request) throws JposException {
            int units = request.getUnits();
            for (int u = 0; u < Display.Unit.length; u++) {
                if ((units & (1 << u)) != 0) {
                    Display.Unit[u].SaveBuffer[request.getBufferId()] = getRange(u, request.getRow(), request.getColumn(), request.getWidth(), request.getHeight());
                }
            }
        }

        @Override
        public CopyVideoRegion copyVideoRegion(int units, int row, int column, int height, int width, int targetRow, int targetColumn) throws JposException {
            Service.check(column + width > COLUMNCOUNT || row + height > LINECOUNT, units, JposConst.JPOS_E_ILLEGAL, 0, "Source Region out of range");
            Service.check(targetColumn + width > COLUMNCOUNT || targetRow + height > LINECOUNT, units, JposConst.JPOS_E_ILLEGAL, 0, "Target Region out of range");
            return super.copyVideoRegion(units, row, column, height, width, targetRow, targetColumn);
        }

        @Override
        public void copyVideoRegion(CopyVideoRegion request) throws JposException {
            int units = request.getUnits();
            for (int u = 0; u < Display.Unit.length; u++) {
                if ((units & (1 << u)) != 0) {
                    DisplayContents.CharAttributes[][] range = getRange(u, request.getRow(), request.getColumn(), request.getWidth(), request.getHeight());
                    updateRegion(request, u, range, request.getTargetRow(), request.getTargetColumn(), false);
                }
            }
        }

        private void updateRegion(OutputRequest request, int u, DisplayContents.CharAttributes[][] range, int targetRow, int targetColumn, boolean clock) throws JposException {
            for (int l = 0; l < range.length; l++) {
                for (int c = 0; c < range[l].length; c++) {
                    String s = "";
                    for (int cc = c; cc < range[l].length; cc++) {
                        if (range[l][c].ForegroundColor != range[l][cc].ForegroundColor ||
                                range[l][c].BackgroundColor != range[l][cc].BackgroundColor ||
                                range[l][c].TopBorderColor != range[l][cc].TopBorderColor ||
                                range[l][c].BottomBorderColor != range[l][cc].BottomBorderColor) {
                            break;
                        }
                        drawLeftLinePart(request, u, range[l][cc], targetRow, targetColumn, l, cc, clock);
                        drawRightLinePart(request, u, range[l][cc], targetRow, targetColumn, l, cc, clock);
                        s += String.valueOf(range[l][cc].Value);
                    }
                    displayTextPart(request, u, range[l][c], targetRow + l, targetColumn + c, s, clock);
                    drawTopLinePart(request, u, range[l][c], targetRow, targetColumn, l, c, s, clock);
                    drawBottomLinePart(request, u, range[l][c], targetRow, targetColumn, l, c, s, clock);
                    c += s.length() - 1;
                }
            }
        }

        private void drawLeftLinePart(OutputRequest request, int u, CharAttributes charAttributes, int targetRow, int targetColumn, int l, int c, boolean clock) throws JposException {
            if (charAttributes.LeftBorderColor != NOT_PRESENT) {
                int color = charAttributes.LeftBorderColor % BLINKING;
                int blinking = charAttributes.LeftBorderColor / BLINKING;
                sendCheckCommand(request, String.format("%02dBL%02d%02d%1d%02d%02d%1d", u + 1, l + targetRow + 1, c + targetColumn + 1, 0, 1, color, blinking));
                if (!clock)
                    Display.Unit[u].Attribute[l + targetRow][c + targetColumn].LeftBorderColor = charAttributes.LeftBorderColor;
            }
        }

        private void drawRightLinePart(OutputRequest request, int u, CharAttributes charAttributes, int targetRow, int targetColumn, int l, int c, boolean clock) throws JposException {
            if (charAttributes.RightBorderColor != NOT_PRESENT) {
                int color = charAttributes.RightBorderColor % BLINKING;
                int blinking = charAttributes.RightBorderColor / BLINKING;
                sendCheckCommand(request, String.format("%02dBL%02d%02d%1d%02d%02d%1d", u + 1, l + targetRow + 1, c + targetColumn + 1, 2, 1, color, blinking));
                if (!clock)
                    Display.Unit[u].Attribute[l + targetRow][c + targetColumn].RightBorderColor = charAttributes.RightBorderColor;
            }
        }

        private void displayTextPart(OutputRequest request, int unit, CharAttributes attributes, int row, int column, String text, boolean clock) throws JposException {
            int fgcolor = attributes.ForegroundColor % BLINKING;
            int bgcolor = attributes.BackgroundColor;
            int blinking = attributes.ForegroundColor / BLINKING;
            if (clock || Display.Unit[unit].SaveBuffer[0] == null || row != Display.Unit[unit].ClockRow || column + text.length() <= Display.Unit[unit].ClockColumn || column >= Display.Unit[unit].ClockColumn + Display.Unit[unit].SaveBuffer[0][0].length) {
                sendCheckCommand(request, String.format("%02dDT%02d%02d%02d%02d%1d%s", unit + 1, row + 1, column + 1, fgcolor, bgcolor, blinking, text));
            } else {
                if (column < Display.Unit[unit].ClockColumn) {
                    String subtext = text.substring(0, Display.Unit[unit].ClockColumn - column);
                    sendCheckCommand(request, String.format("%02dDT%02d%02d%02d%02d%1d%s", unit + 1, row + 1, column + 1, fgcolor, bgcolor, blinking, subtext));
                }
                if (column + text.length() > Display.Unit[unit].ClockColumn + Display.Unit[unit].SaveBuffer[0][0].length) {
                    String subtext = text.substring(Display.Unit[unit].ClockColumn + Display.Unit[unit].SaveBuffer[0][0].length - column);
                    sendCheckCommand(request, String.format("%02dDT%02d%02d%02d%02d%1d%s", unit + 1, row + 1, Display.Unit[unit].ClockColumn + Display.Unit[unit].SaveBuffer[0][0].length + 1, fgcolor, bgcolor, blinking, subtext));
                }
            }
            if (!clock) {
                for (int i = 0; i < text.length(); i++) {
                    Display.Unit[unit].Attribute[row][column + i].Value = text.charAt(i);
                    Display.Unit[unit].Attribute[row][column + i].ForegroundColor = attributes.ForegroundColor;
                    Display.Unit[unit].Attribute[row][column + i].BackgroundColor = attributes.BackgroundColor;
                }
            }
        }

        private void drawTopLinePart(OutputRequest request, int u, CharAttributes charAttributes, int targetRow, int targetColumn, int l, int c, String s, boolean clock) throws JposException {
            int fgcolor;
            int blinking;
            if (charAttributes.TopBorderColor != NOT_PRESENT) {
                fgcolor = charAttributes.TopBorderColor % BLINKING;
                blinking = charAttributes.TopBorderColor / BLINKING;
                sendCheckCommand(request, String.format("%02dBL%02d%02d%1d%02d%02d%1d", u + 1, targetRow + l + 1, targetColumn + c + 1, 1, s.length(), fgcolor, blinking));
                if (!clock) {
                    for (int i = 0; i < s.length(); i++) {
                        Display.Unit[u].Attribute[l + targetRow][c + targetColumn + i].TopBorderColor = charAttributes.TopBorderColor;
                    }
                }
            }
        }

        private void drawBottomLinePart(OutputRequest request, int u, CharAttributes charAttributes, int targetRow, int targetColumn, int l, int c, String s, boolean clock) throws JposException {
            int fgcolor;
            int blinking;
            if (charAttributes.BottomBorderColor != NOT_PRESENT) {
                fgcolor = charAttributes.BottomBorderColor % BLINKING;
                blinking = charAttributes.BottomBorderColor / BLINKING;
                sendCheckCommand(request, String.format("%02dBL%02d%02d%1d%02d%02d%1d", u + 1, targetRow + l + 1, targetColumn + c + 1, 3, s.length(), fgcolor, blinking));
                if (!clock) {
                    for (int i = 0; i < s.length(); i++) {
                        Display.Unit[u].Attribute[l + targetRow][c + targetColumn + i].BottomBorderColor = charAttributes.BottomBorderColor;
                    }
                }
            }
        }

        private DisplayContents.CharAttributes[][] getRange(int unit, int row, int column, int width, int height) {
            DisplayContents.CharAttributes[][] result = new DisplayContents.CharAttributes[height][];
            for (int l = 0; l < result.length; l++) {
                result[l] = new DisplayContents.CharAttributes[width];
                for (int c = 0; c < result[l].length; c++) {
                    result[l][c] = new DisplayContents.CharAttributes();
                    result[l][c].Value = Display.Unit[unit].Attribute[row + l][column + c].Value;
                    result[l][c].ForegroundColor = Display.Unit[unit].Attribute[row + l][column + c].ForegroundColor;
                    result[l][c].BackgroundColor = Display.Unit[unit].Attribute[row + l][column + c].BackgroundColor;
                    result[l][c].LeftBorderColor = Display.Unit[unit].Attribute[row + l][column + c].LeftBorderColor;
                    result[l][c].TopBorderColor = Display.Unit[unit].Attribute[row + l][column + c].TopBorderColor;
                    result[l][c].RightBorderColor = Display.Unit[unit].Attribute[row + l][column + c].RightBorderColor;
                    result[l][c].BottomBorderColor = Display.Unit[unit].Attribute[row + l][column + c].BottomBorderColor;
                }
            }
            return result;
        }

        @Override
        public void displayData(DisplayData request) throws JposException {
            CharAttributes attrs = new CharAttributes();
            int bgcolor = ((request.getAttributes() >> 4) & 7) | BackgroundIntensity;
            int fgcolor = request.getAttributes() & 15;
            int blinking = (request.getAttributes() >> 7) & 1;
            attrs.BackgroundColor = bgcolor;
            attrs.ForegroundColor = fgcolor | (blinking == 0 ? 0 : BLINKING);
            String text = request.getData();
            int units = request.getUnits();
            if (request.getColumn() + text.length() > COLUMNCOUNT) {
                text = text.substring(0, COLUMNCOUNT - request.getColumn());
            }
            for (int i = 0; i < Display.Unit.length; i++) {
                if ((units & (1 << i)) != 0) {
                    displayTextPart(request, i, attrs, request.getRow(), request.getColumn(), text, false);
                }
            }
        }

        @Override
        public DrawBox drawBox(int units, int row, int column, int height, int width, int attribute, int bordertype) throws JposException {
            Service.check(column + width > COLUMNCOUNT || row + height > LINECOUNT, units, JposConst.JPOS_E_ILLEGAL, 0, "Region out of range");
            return super.drawBox(units, row, column, height, width, attribute, bordertype);
        }

        @Override
        public void drawBox(DrawBox request) throws JposException {
            int color = request.getAttributes() & 15;
            int blinking = (request.getAttributes() >> 7) & 1;
            int units = request.getUnits();
            for (int i = 0; i < Display.Unit.length; i++) {
                if ((units & (1 << i)) != 0) {
                    sendCheckCommand(request, String.format("%02dDB%02d%02d%02d%02d%02d%1d", i + 1, request.getRow() + 1, request.getColumn() + 1, request.getWidth(), request.getHeight(), color, blinking));
                    for (int l = request.getRow(); l < request.getRow() + request.getHeight(); l++) {
                        Display.Unit[i].Attribute[l][request.getColumn()].LeftBorderColor = color | (blinking == 0 ? 0 : BLINKING);
                        Display.Unit[i].Attribute[l][request.getColumn() + request.getWidth() - 1].RightBorderColor = color | (blinking == 0 ? 0 : BLINKING);
                    }
                    for (int c = request.getColumn(); c < request.getColumn() + request.getWidth(); c++) {
                        Display.Unit[i].Attribute[request.getRow()][c].TopBorderColor = color | (blinking == 0 ? 0 : BLINKING);
                        Display.Unit[i].Attribute[request.getRow() + request.getHeight() - 1][c].BottomBorderColor = color | (blinking == 0 ? 0 : BLINKING);
                    }
                }
            }
        }

        @Override
        public UpdateVideoRegionAttribute updateVideoRegionAttribute(int units, int function, int row, int column, int height, int width, int attribute) throws JposException {
            Service.check(column + width > COLUMNCOUNT || row + height > LINECOUNT, units, JposConst.JPOS_E_ILLEGAL, 0, "Region out of range");
            return super.updateVideoRegionAttribute(units, function, row, column, height, width, attribute);
        }

        @Override
        public void updateVideoRegionAttribute(UpdateVideoRegionAttribute request) throws JposException {
            int units = request.getUnits();
            for (int u = 0; u < Display.Unit.length; u++) {
                if ((units & (1 << u)) != 0) {
                    DisplayContents.CharAttributes[][] range = getRange(u, request.getRow(), request.getColumn(), request.getWidth(), request.getHeight());
                    switch (request.getFunction()) {
                        case RemoteOrderDisplayConst.ROD_UA_SET: {
                            setAttributes(request, range);
                            break;
                        }
                        case RemoteOrderDisplayConst.ROD_UA_INTENSITY_ON: {
                            switchIntensityOn(range);
                            break;
                        }
                        case RemoteOrderDisplayConst.ROD_UA_INTENSITY_OFF: {
                            switchIntensityOff(range);
                            break;
                        }
                        case RemoteOrderDisplayConst.ROD_UA_REVERSE_OFF:
                        case RemoteOrderDisplayConst.ROD_UA_REVERSE_ON: {
                            doReverseVideo(range);
                            break;
                        }
                        case RemoteOrderDisplayConst.ROD_UA_BLINK_ON: {
                            switchBlinkingOn(range);
                            break;
                        }
                        case RemoteOrderDisplayConst.ROD_UA_BLINK_OFF: {
                            swichtBlinkingOff(range);
                            break;
                        }
                    }
                    updateRegion(request, u, range, request.getRow(), request.getColumn(), false);
                }
            }
        }

        private void setAttributes(UpdateVideoRegionAttribute request, DisplayContents.CharAttributes[][] range) {
            int fgcolor = (request.getAttributes() & 0xf) | ((request.getAttributes() & 0x80) == 0 ? 0 : BLINKING);
            int bgcolor = ((request.getAttributes() >> 4) & 7) | BackgroundIntensity;
            for (DisplayContents.CharAttributes[] line : range) {
                for (DisplayContents.CharAttributes column : line) {
                    column.ForegroundColor = fgcolor;
                    column.BackgroundColor = bgcolor;
                    if (column.LeftBorderColor != NOT_PRESENT)
                        column.LeftBorderColor = fgcolor;
                    if (column.TopBorderColor != NOT_PRESENT)
                        column.TopBorderColor = fgcolor;
                    if (column.RightBorderColor != NOT_PRESENT)
                        column.RightBorderColor = fgcolor;
                    if (column.BottomBorderColor != NOT_PRESENT)
                        column.BottomBorderColor = fgcolor;
                }
            }
        }

        private void switchIntensityOn(DisplayContents.CharAttributes[][] range) {
            for (DisplayContents.CharAttributes[] line : range) {
                for (DisplayContents.CharAttributes column : line) {
                    column.ForegroundColor |= RemoteOrderDisplayConst.ROD_ATTR_INTENSITY;
                    if (column.LeftBorderColor != NOT_PRESENT)
                        column.LeftBorderColor |= RemoteOrderDisplayConst.ROD_ATTR_INTENSITY;
                    if (column.TopBorderColor != NOT_PRESENT)
                        column.TopBorderColor |= RemoteOrderDisplayConst.ROD_ATTR_INTENSITY;
                    if (column.RightBorderColor != NOT_PRESENT)
                        column.RightBorderColor |= RemoteOrderDisplayConst.ROD_ATTR_INTENSITY;
                    if (column.BottomBorderColor != NOT_PRESENT)
                        column.BottomBorderColor |= RemoteOrderDisplayConst.ROD_ATTR_INTENSITY;
                }
            }
        }

        private void switchIntensityOff(DisplayContents.CharAttributes[][] range) {
            for (DisplayContents.CharAttributes[] line : range) {
                for (DisplayContents.CharAttributes column : line) {
                    column.ForegroundColor &= ~RemoteOrderDisplayConst.ROD_ATTR_INTENSITY;
                    if (column.LeftBorderColor != NOT_PRESENT)
                        column.LeftBorderColor &= ~RemoteOrderDisplayConst.ROD_ATTR_INTENSITY;
                    if (column.TopBorderColor != NOT_PRESENT)
                        column.TopBorderColor &= ~RemoteOrderDisplayConst.ROD_ATTR_INTENSITY;
                    if (column.RightBorderColor != NOT_PRESENT)
                        column.RightBorderColor &= ~RemoteOrderDisplayConst.ROD_ATTR_INTENSITY;
                    if (column.BottomBorderColor != NOT_PRESENT)
                        column.BottomBorderColor &= ~RemoteOrderDisplayConst.ROD_ATTR_INTENSITY;
                }
            }
        }

        private void doReverseVideo(DisplayContents.CharAttributes[][] range) {
            for (DisplayContents.CharAttributes[] line : range) {
                for (DisplayContents.CharAttributes column : line) {
                    int color = column.ForegroundColor % BLINKING;
                    column.ForegroundColor &= BLINKING;
                    column.ForegroundColor |= column.BackgroundColor;
                    column.BackgroundColor = color;
                    if (column.LeftBorderColor != NOT_PRESENT)
                        column.LeftBorderColor = (column.LeftBorderColor & BLINKING) | (15 - column.LeftBorderColor % BLINKING);
                    if (column.TopBorderColor != NOT_PRESENT)
                        column.TopBorderColor = (column.TopBorderColor & BLINKING) | (15 - column.TopBorderColor % BLINKING);
                    if (column.RightBorderColor != NOT_PRESENT)
                        column.RightBorderColor = (column.RightBorderColor & BLINKING) | (15 - column.RightBorderColor % BLINKING);
                    if (column.BottomBorderColor != NOT_PRESENT)
                        column.BottomBorderColor = (column.BottomBorderColor & BLINKING) | (15 - column.BottomBorderColor % BLINKING);
                }
            }
        }

        private void switchBlinkingOn(DisplayContents.CharAttributes[][] range) {
            for (DisplayContents.CharAttributes[] line : range) {
                for (DisplayContents.CharAttributes column : line) {
                    column.ForegroundColor |= BLINKING;
                    if (column.LeftBorderColor != NOT_PRESENT)
                        column.LeftBorderColor |= BLINKING;
                    if (column.TopBorderColor != NOT_PRESENT)
                        column.TopBorderColor |= BLINKING;
                    if (column.RightBorderColor != NOT_PRESENT)
                        column.RightBorderColor |= BLINKING;
                    if (column.BottomBorderColor != NOT_PRESENT)
                        column.BottomBorderColor |= BLINKING;
                }
            }
        }

        private void swichtBlinkingOff(DisplayContents.CharAttributes[][] range) {
            for (DisplayContents.CharAttributes[] line : range) {
                for (DisplayContents.CharAttributes column : line) {
                    column.ForegroundColor &= ~BLINKING;
                    if (column.LeftBorderColor != NOT_PRESENT)
                        column.LeftBorderColor &= ~BLINKING;
                    if (column.TopBorderColor != NOT_PRESENT)
                        column.TopBorderColor &= ~BLINKING;
                    if (column.RightBorderColor != NOT_PRESENT)
                        column.RightBorderColor &= ~BLINKING;
                    if (column.BottomBorderColor != NOT_PRESENT)
                        column.BottomBorderColor &= ~BLINKING;
                }
            }
        }

        private void sendCheckCommand(OutputRequest request, String command) throws JposException {
            JposException e = sendCommand(command);
            if (e != null) {
                RemoteOrderDisplayProperties data = (RemoteOrderDisplayProperties) request.Props;
                data.ErrorUnits = request.getUnits();
                data.EventSource.logSet("ErrorUnits");
                data.ErrorString = e.getMessage();
                data.EventSource.logSet("ErrorString");
                throw e;
            }
        }

        @Override
        public void controlClock(int units, int function, int clockid, int hour, int minute, int second, int row, int column, int attribute, int mode) throws JposException {
            switch (function) {
                case RemoteOrderDisplayConst.ROD_CLK_START: {
                    startClock(units, hour, minute, second, row, column, attribute, mode);
                    break;
                }
                case RemoteOrderDisplayConst.ROD_CLK_STOP: {
                    stopClock(units);
                    break;
                }
                case RemoteOrderDisplayConst.ROD_CLK_PAUSE: {
                    pauseClock(units);
                    break;
                }
                case RemoteOrderDisplayConst.ROD_CLK_RESUME: {
                    resumeClock(units);
                    break;
                }
                case RemoteOrderDisplayConst.ROD_CLK_MOVE: {
                    moveClock(units, row, column);
                    break;
                }
            }
        }

        private void startClock(int units, int hour, int minute, int second, int row, int column, int attribute, int mode) throws JposException {
            checkClockActive(units);
            int type = 0;
            String time = "";
            switch (mode) {
                case RemoteOrderDisplayConst.ROD_CLK_SHORT:
                    time = String.format("%1d:%02d", minute, second);
                    break;
                case RemoteOrderDisplayConst.ROD_CLK_NORMAL:
                    time = String.format("%02d:%02d", minute, second);
                    type = 1;
                    break;
                case RemoteOrderDisplayConst.ROD_CLK_24_LONG:
                    type = 1;
                case RemoteOrderDisplayConst.ROD_CLK_12_LONG:
                    type += 2;
                    time = String.format("%02d:%02d:%02d", hour, minute, second);
                    break;
            }
            Service.check(time.length() + column > COLUMNCOUNT, units, JposConst.JPOS_E_ILLEGAL, 0, "Clock out of range");
            int fgcolor = attribute & 0xf;
            int bgcolor = ((attribute >> 4) & 7) | BackgroundIntensity;
            int blinking = attribute >> 7;
            for (int u = 0; u < Display.Unit.length; u++) {
                if ((units & (1 << u)) != 0) {
                    Display.Unit[u].SaveBuffer[0] = getRange(u, row, column, time.length(), 1);
                    for (int i = 0; i < time.length(); i++) {
                        Display.Unit[u].SaveBuffer[0][0][i].Value = time.charAt(i);
                        Display.Unit[u].SaveBuffer[0][0][i].ForegroundColor = fgcolor;
                        Display.Unit[u].SaveBuffer[0][0][i].BackgroundColor = bgcolor | (blinking == 0 ? 0 : BLINKING);
                    }
                    sendCheckCommand(units, String.format("%02dDT%02d%02d%02d%02d%1d%s", u + 1, row + 1, column + 1, fgcolor, bgcolor, blinking, time));
                    sendCheckCommand(units, String.format("%02dST%02d%02d%1d%d", u + 1, row + 1, column + 1, type, 1));
                    Display.Unit[u].ClockRow = row;
                    Display.Unit[u].ClockColumn = column;
                    Display.Unit[u].ClockType = type;
                }
            }
        }

        private void checkClockActive(int units) throws JposException {
            for (int u = 0; u < Display.Unit.length; u++) {
                if ((units & (1 << u)) != 0) {
                    Service.check(Display.Unit[u].SaveBuffer[0] != null, units, JposConst.JPOS_E_BUSY, 0, "Clock running");
                }
            }
        }

        private void checkClockInactive(int units) throws JposException {
            for (int u = 0; u < Display.Unit.length; u++) {
                if ((units & (1 << u)) != 0) {
                    Service.check(Display.Unit[u].SaveBuffer[0] == null, units, JposConst.JPOS_E_EXTENDED, RemoteOrderDisplayConst.JPOS_EROD_BADCLK, "Clock not started");
                }
            }
        }

        private void stopClock(int units) throws JposException {
            checkClockInactive(units);
            for (int u = 0; u < Display.Unit.length; u++) {
                if ((units & (1 << u)) != 0) {
                    if (Display.Unit[u].ClockSuspendTick == 0) {
                        while (WaitClockData.suspend(0)) ;
                        sendCheckCommand(units, String.format("%02dST%02d%02d%1d%d", u + 1, 0, 0, 0, 0));
                        WaitClockData.suspend(RequestTimeout);
                    }
                    for (int i = 0; i < Display.Unit[u].SaveBuffer.length; i++) {
                        Display.Unit[u].Attribute[Display.Unit[u].ClockRow][Display.Unit[u].ClockColumn + i] = Display.Unit[u].SaveBuffer[0][0][i];
                    }
                    Display.Unit[u].ClockSuspendTick = 0;
                    Display.Unit[u].ClockType = -1;
                    Display.Unit[u].SaveBuffer[0] = null;
                }
            }
        }

        private void pauseClock(int units) throws JposException {
            checkClockInactive(units);
            for (int u = 0; u < Display.Unit.length; u++) {
                if ((units & (1 << u)) != 0) {
                    if (Display.Unit[u].ClockSuspendTick == 0) {
                        while (WaitClockData.suspend(0)) ;
                        sendCheckCommand(units, String.format("%02dST%02d%02d%1d%d", u + 1, 0, 0, 0, 0));
                        WaitClockData.suspend(RequestTimeout);
                    }
                    Display.Unit[u].ClockSuspendTick = System.currentTimeMillis();
                }
            }
        }

        private void resumeClock(int units) throws JposException {
            checkClockInactive(units);
            for (int u = 0; u < Display.Unit.length; u++) {
                if ((units & (1 << u)) != 0) {
                    if (Display.Unit[u].ClockSuspendTick != 0) {
                        // If ticks since pause start shall be added, use add instead of 1 below:
                        // int add = (int)((System.currentTimeMillis() - Display.Unit[u].ClockSuspendTick + 500) / 1000 + 1);
                        sendCheckCommand(units, String.format("%02dST%02d%02d%1d%d", u + 1, Display.Unit[u].ClockRow + 1, Display.Unit[u].ClockColumn + 1, Display.Unit[u].ClockType, 1));
                    }
                    Display.Unit[u].ClockSuspendTick = 0;
                }
            }
        }

        private void moveClock(int units, int row, int column) throws JposException {
            for (int u = 0; u < Display.Unit.length; u++) {
                if ((units & (1 << u)) != 0) {
                    Service.check(Display.Unit[u].SaveBuffer[0] == null, units, JposConst.JPOS_E_EXTENDED, RemoteOrderDisplayConst.JPOS_EROD_BADCLK, "Clock not started");
                    Service.check(column + Display.Unit[u].SaveBuffer[0][0].length > COLUMNCOUNT, units, JposConst.JPOS_E_ILLEGAL, 0, "Target position out of range");
                }
            }
            for (int u = 0; u < Display.Unit.length; u++) {
                if ((units & (1 << u)) != 0) {
                    if (Display.Unit[u].ClockSuspendTick == 0) {
                        while (WaitClockData.suspend(0)) ;
                        sendCheckCommand(units, String.format("%02dST%02d%02d%1d%d", u + 1, 0, 0, 0, 0));
                        WaitClockData.suspend(RequestTimeout);
                    }
                    int sourceRow = Display.Unit[u].ClockRow;
                    int sourceColumn = Display.Unit[u].ClockColumn;
                    CharAttributes[][] clock = Display.Unit[u].SaveBuffer[0];
                    CharAttributes[][] underclock = getRange(u, sourceRow, sourceColumn, clock[0].length, 1);
                    Display.Unit[u].ClockRow = row;
                    Display.Unit[u].ClockColumn = column;
                    OutputRequest request = new OutputRequest(this, 1 << u);
                    updateRegion(request, u, underclock, sourceRow, sourceColumn, true);
                    updateRegion(request, u, clock, row, column, true);
                    if (Display.Unit[u].ClockSuspendTick == 0) {
                        sendCheckCommand(units, String.format("%02dST%02d%02d%1d%d", u + 1, row, column, Display.Unit[u].ClockType, 1));
                    }
                }
            }
        }

        @Override
        public void resetVideo(int units) throws JposException {
            Service.check((units & ~UnitOnline) != 0, units & ~UnitOnline, JposConst.JPOS_E_OFFLINE, 0, "Units offline");
            for (int i = 0; i < Display.Unit.length; i++) {
                if ((units & (1 << i)) != 0) {
                    sendCheckCommand(units, String.format("%02dVC", i + 1));
                    for (int l = 0; l < Display.Unit[i].Attribute.length; l++) {
                        for (int c = 0; c < Display.Unit[i].Attribute[l].length; c++) {
                            Display.Unit[i].Attribute[l][c] = new DisplayContents.CharAttributes();
                        }
                    }
                    Display.Unit[i].SaveBuffer[0] = null;
                }
            }
        }

        private void sendCheckCommand(int units, String command) throws JposException {
            JposException e = sendCommand(command);
            if (e != null) {
                ErrorUnits = units;
                EventSource.logSet("ErrorUnits");
                ErrorString = e.getMessage();
                EventSource.logSet("ErrorString");
                throw e;
            }
        }

        @Override
        public void controlCursor(int units, int function) throws JposException {
            for (int i = 0; i < Display.Unit.length; i++) {
                if ((units & (1 << i)) != 0) {
                    if (Display.Unit[i].CursorActive) {
                        if (function == RemoteOrderDisplayConst.ROD_CRS_OFF) {
                            sendCheckCommand(1 << i, String.format("%02dSC%02d%02d%02d", i + 1, 0, 0, CursorColor));
                            Display.Unit[i].CursorActive = false;
                        }
                    } else {
                        if (function != RemoteOrderDisplayConst.ROD_CRS_OFF) {
                            sendCheckCommand(1 << i, String.format("%02dSC%02d%02d%02d", i + 1, Display.Unit[i].CursorRow + 1, Display.Unit[i].CursorColumn + 1, CursorColor));
                            Display.Unit[i].CursorActive = true;
                        }
                    }
                }
            }
        }

        @Override
        public void setCursor(int units, int row, int column) throws JposException {
            for (int i = 0; i < Display.Unit.length; i++) {
                if ((units & (1 << i)) != 0) {
                    if (Display.Unit[i].CursorActive)
                        sendCheckCommand(1 << i, String.format("%02dSC%02d%02d%02d", i + 1, row + 1, column + 1, CursorColor));
                    Display.Unit[i].CursorRow = row;
                    Display.Unit[i].CursorColumn = column;
                }
            }
        }
    }
}
