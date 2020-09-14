/*
 * Copyright 2020 Martin Conrad
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

package SampleCombiSpecial;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.gate.*;
import de.gmxhome.conrad.jpos.jpos_base.itemdispenser.*;
import de.gmxhome.conrad.jpos.jpos_base.lights.*;
import de.gmxhome.conrad.jpos.jpos_base.motionsensor.*;
import de.gmxhome.conrad.jpos.jpos_base.signaturecapture.*;
import jpos.*;
import jpos.config.JposEntry;
import org.apache.log4j.Level;

import javax.swing.*;
import java.awt.Point;
import java.io.IOException;
import java.util.Arrays;

/**
 * Implementation of a JposDevice based implementation of a combined driver that becomes
 * several JavaPOS device services in combination with the JposXxxx class in jpos_base
 */
public class Device extends JposDevice implements Runnable {
    private int Baudrate = SerialIOProcessor.BAUDRATE_9600;
    private int Databits = SerialIOProcessor.DATABITS_8;
    private int Stopbits = SerialIOProcessor.STOPBITS_2;
    private int Parity = SerialIOProcessor.PARITY_NONE;
    private Integer OwnPort = null;
    private int LoggingType = UniqueIOProcessor.LoggingTypeEscapeString;
    private int RequestTimeout = 500;
    private int CharacterTimeout = 10;
    private int PollDelay = 300;
    private int MaxRetry = 2;
    private boolean UsbToSerial = false;
    private int MotionActivityTimeout = 5000;
    private int DispenserNearEndCount = 2;
    private boolean Off = false;

    /**
     * Constructor, Stores communication target. Communication target can be a COM port or a TCP
     * target. Valid COM port specifiers differ between operating systems, e.g. on Windows, COM1
     * can be a valid communication target while on Linux, /dev/ttyS0 might specify the same target.
     * Format of TCP targets is <i>IpAddress</i>:<i>Port</i>, e.g. 10.11.12.13:45678.
     * @param id COM port or TCP target.
     * @throws JposException if Communication target invalid.
     */
    public Device(String id) throws JposException {
        super(id);
        lightsInit(1);
        motionSensorInit(1);
        gateInit(1);
        itemDispenserInit(1);
        signatureCaptureInit(1);
        PhysicalDeviceDescription = "Combined special device simulator for virtual COM ports and TCP";
        PhysicalDeviceName = "Combined Special Device Simulator";
        CapPowerReporting = JposConst.JPOS_PR_ADVANCED;
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
            if ((o = entry.getPropertyValue("DispenserNearEndCount")) != null)
                DispenserNearEndCount = Integer.parseInt(o.toString());
            if ((o = entry.getPropertyValue("MotionActivityTimeout")) != null)
                MotionActivityTimeout = Integer.parseInt(o.toString());
            if ((o = entry.getPropertyValue("UsbToSerial")) != null) {
                if (OwnPort != null)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property: UsbToSerial");
                UsbToSerial = Boolean.parseBoolean(o.toString());
            }
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property", e);
        }
    }

    private int[] ItemDispenserStates = new int[10];

    private void prepareStatusWaitingObjects() {
        prepareSignalStatusWaits(MotionSensors[0]);
        prepareSignalStatusWaits(Gates[0]);
    }

    /**
     * Device control thread, polls device states and handles status changes.
     */
    @Override
    public void run() {
        String[] commands = {"idR", "gS", "msS"};   // Status requests for item dispenser, gate and motion sensor
        int newgatestate = 0, newmotionsensorstate = 0, newitemdispenderstate[] = new int[10];
        prepareStatusWaitingObjects();
        int retry = 0;
        for (int index = 0; !ToBeFinished; index = (index + 1) % commands.length) {
            retry++;
            try {
                String resp = sendrecv(commands[index]);
                if (resp == null) {
                    index--;
                    synchronized (this) {
                        if (UsbToSerial && !new SerialIOProcessor(this, ID).exists()) {
                            closePort();
                            InIOError = true;
                            Off = true;
                        }
                    }
                }
                else {
                    if (index == 0) {           // ItemDispenser
                        int[] itemcounts = new int[10];
                        if (resp.length() == 23) {
                            for (int i = 0; i < 10; i++) {
                                itemcounts[i] = Integer.parseInt(resp.substring(3 + 2 * i, 5 + 2 * i), 10);
                            }
                            newitemdispenderstate = itemcounts;
                            retry = 0;
                        }
                        else
                            index--;
                    } else if (index == 1) {    // Gate
                        int gatestate = 0;
                        if (resp.length() == 3 && (gatestate = "COco".indexOf(resp.charAt(2))) >= 0) {
                            newgatestate = gatestate;
                            retry = 0;
                        }
                        else
                            index--;
                    } else {                    // MotionSensor
                        int motionstate = 0;
                        if (resp.length() == 3 && (motionstate = "sS".indexOf(resp.charAt(2))) >= 0) {
                            newmotionsensorstate = motionstate;
                            retry = 0;
                        }
                        else
                            index--;
                    }
                }
                if (index == commands.length - 1) { // New status is complete
                    handleStates(newgatestate, newmotionsensorstate, newitemdispenderstate);
                    if (StartPollingWaiter != null) {
                        StartPollingWaiter.signalWaiter();
                        StartPollingWaiter = null;
                    }
                    PollWaiter.suspend(PollDelay);
                    prepareStatusWaitingObjects();
                }
            } catch (Exception e) {
                e.printStackTrace();
                index--;
            }
            try {
                if (retry == MaxRetry) {
                    retry = 0;
                    handleStates(-1, -1, null);
                    if (StartPollingWaiter != null) {
                        StartPollingWaiter.signalWaiter();
                        StartPollingWaiter = null;
                    }
                    PollWaiter.suspend(PollDelay);
                    prepareStatusWaitingObjects();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private int PowerOff = JposConst.JPOS_PS_UNKNOWN;

    private void handleStates(int newgatestate, int newmotionsensorstate, int[] newitemdispenderstate) {
        if (newgatestate == -1) { // offline
            if (PowerOff != JposConst.JPOS_PS_OFF_OFFLINE) {
                PowerOff = UsbToSerial ? (Off ? JposConst.JPOS_PS_OFF : JposConst.JPOS_PS_OFFLINE) : JposConst.JPOS_PS_OFF_OFFLINE;
                GateState = MotionSensorState = -1;
                ItemDispenserStates = new int[0];
                firePowerStateEvent(JposConst.JPOS_SUE_POWER_OFF_OFFLINE);
            }
        }
        else {
            if (PowerOff != JposConst.JPOS_PS_ONLINE) {
                PowerOff = JposConst.JPOS_PS_ONLINE;
                firePowerStateEvent(JposConst.JPOS_SUE_POWER_ONLINE);
            }
            fireGateStateEvent(newgatestate);
            fireMotionSensorState(newmotionsensorstate);
            fireItemDispenserState(newitemdispenderstate);
        }
    }

    private int DispenserState = JposConst.JPOS_SUE_POWER_OFF_OFFLINE;

    private void fireItemDispenserState(int[] newitemdispenserstate) {
        boolean changed = newitemdispenserstate.length != ItemDispenserStates.length;
        for (int i = 0; !changed && i < ItemDispenserStates.length; i++)
            changed = newitemdispenserstate[i] != ItemDispenserStates[i];
        if (changed) {
            ItemDispenserStates = newitemdispenserstate;
            int minimum = 20;
            for(int val : ItemDispenserStates) {
                if (val < minimum)
                    minimum = val;
            }
            int state = minimum == 0 ? ItemDispenserConst.ITEM_SUE_EMPTY :
                    (minimum < DispenserNearEndCount ? ItemDispenserConst.ITEM_SUE_NEAREMPTY : ItemDispenserConst.ITEM_SUE_OK);
            if (state != DispenserState) {
                DispenserState = state;
                JposCommonProperties props = getClaimingInstance(ClaimedItemDispenser, 0);
                if (props != null) {
                    try {
                        handleEvent(new ItemDispenserStatusUpdateEvent(props.EventSource, state));
                    } catch (JposException e) {}
                }
            }
        }
    }

    private class SampleMotionSensorStatusUpdateEvent extends MotionSensorStatusUpdateEvent {
        SampleMotionSensorStatusUpdateEvent(JposBase source, int state) {
            super(source, state);
        }

        @Override
        public long handleDelay() {
            // Let us assume the device sends inactivity message after MotionActivityTimeout milliseconds. Therefore,
            // a delay is only necessary if Timeout above 5000.
            long ret = super.handleDelay();
            if (ret > 0) {
                if (ret > MotionActivityTimeout)
                    ret -= MotionActivityTimeout;
                else
                    ret = 0;
            }
            return ret;
        }
    }

    private void fireMotionSensorState(int newmotionsensorstate) {
        if (newmotionsensorstate != MotionSensorState) {
            MotionSensorState = newmotionsensorstate;
            JposCommonProperties props = getPropertySetInstance(MotionSensors, 0, 0);
            if (props != null) {
                try {
                    int[] states = {MotionSensorConst.MOTION_M_ABSENT, MotionSensorConst.MOTION_M_PRESENT};
                    handleEvent(new SampleMotionSensorStatusUpdateEvent(props.EventSource, states[MotionSensorState]));
                } catch (JposException e) {}
                if (MotionSensorState == 1)
                    signalStatusWaits(MotionSensors[0]);
            }
        }
    }

    private void fireGateStateEvent(int newgatestate) {
        if (newgatestate > 2)
            newgatestate--;
        if (newgatestate != GateState) {
            GateState = newgatestate;
            JposCommonProperties props = getPropertySetInstance(Gates, 0, 0);
            if (props != null) {
                try {
                    int[] states = {GateConst.GATE_SUE_CLOSED, GateConst.GATE_SUE_OPEN, GateConst.GATE_SUE_BLOCKED};
                    handleEvent(new GateStatusUpdateEvent(props.EventSource, states[GateState]));
                } catch (JposException e) {}
                signalStatusWaits(Gates[0]);
            }
        }
    }

    private void firePowerStateEvent(int state) {
        JposCommonProperties[] props = {
                getPropertySetInstance(Gates, 0, 0),
                getClaimingInstance(ClaimedItemDispenser, 0),
                getClaimingInstance(ClaimedLights, 0),
                getPropertySetInstance(MotionSensors, 0, 0),
                getClaimingInstance(ClaimedSignatureCapture, 0),
        };
        for (JposCommonProperties set : props) {
            if (set != null) {
                try {
                    handleEvent(new JposStatusUpdateEvent(set.EventSource, state));
                } catch (JposException e) {}
                signalStatusWaits(MotionSensors[0]);
                signalStatusWaits(Gates[0]);
            }
        }
    }

    /*
     * Device control thread ready, Device initialization follows
     */

    @Override
    public void changeDefaults(LightsProperties props) {
        props.DeviceServiceDescription = "Lights service for combined special device simulator";
        props.DeviceServiceVersion = 1014001;
        props.CapColor = LightsConst.LGT_COLOR_PRIMARY|LightsConst.LGT_COLOR_CUSTOM1|LightsConst.LGT_COLOR_CUSTOM2|
                LightsConst.LGT_COLOR_CUSTOM3|LightsConst.LGT_COLOR_CUSTOM4|LightsConst.LGT_COLOR_CUSTOM5;
        props.MaxLights = 5;
    }

    @Override
    public void changeDefaults(GateProperties props) {
        props.DeviceServiceDescription = "Gate service for combined special device simulator";
        props.DeviceServiceVersion = 1014001;
        props.CapGateStatus = true;
    }

    @Override
    public void changeDefaults(MotionSensorProperties props) {
        props.DeviceServiceDescription = "MotionSensor service for combined special device simulator";
        props.DeviceServiceVersion = 1014001;
    }

    @Override
    public void changeDefaults(SignatureCaptureProperties props) {
        props.DeviceServiceDescription = "SignatureCapture service for combined special device simulator";
        props.DeviceServiceVersion = 1014001;
        props.CapUserTerminated = true;
        props.MaximumX = 500;
        props.MaximumY = 200;
    }

    @Override
    public void changeDefaults(ItemDispenserProperties props) {
        props.DeviceServiceDescription = "ItemDispenser service for combined special device simulator";
        props.DeviceServiceVersion = 1014001;
        props.CapIndividualSlotStatus = true;
        props.CapEmptySensor = true;
        props.MaxSlots = 10;
    }

    private UniqueIOProcessor Stream = null;
    private boolean InIOError = false;
    private boolean ToBeFinished = false;
    private SyncObject PollWaiter;
    private Thread StateWatcher;
    private JposCommonProperties StartPollingWaiter = null;

    // Method to start communication
    private int startPolling(JposCommonProperties props) {
        synchronized (OpenCount) {
            if (OpenCount[0] == 0) {
                ToBeFinished = false;
                PollWaiter = new SyncObject();
                (StartPollingWaiter = props).attachWaiter();
                (StateWatcher = new Thread(this)).start();
                StateWatcher.setName(ID + "/StatusUpdater");
                OpenCount[0] = 1;
                props.waitWaiter(MaxRetry * RequestTimeout * 3);
                props.releaseWaiter();
            }
            else
                OpenCount[0] = OpenCount[0] + 1;
            return OpenCount[0];
        }
    }

    // Method to stop communication
    private int stopPolling() {
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

    private int[] OpenCount = { 0 };

    private synchronized int changeOpenCount(int value) {
        OpenCount[0] += value;
        return OpenCount[0];
    }

    /*
     * Basic communication follows.
     */
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
            if (OwnPort == null) {
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
            Off = false;
        } catch (JposException e) {
            Stream = null;
            return e;
        }
        return null;
    }

    /**
     * Send a command to the device and wait for a response. Incoming status messages will be processed by the way.
     * @param command Command to be executed.
     * @return Response from device, null in error case
     */
    private synchronized String sendrecv(String command) {
        if (Stream == null) {
            JposException e = initPort();
            if (e != null) {
                return null;
            }
        }
        try {
            String response = sendFrameRetrieveResponse(command);
            if (response != null) return response;
        } catch (Exception e) {
            log(Level.TRACE, ID + ": IO error: " + e.getMessage());
            JposException ee = closePort();
            InIOError = true;
        }
        return null;
    }

    private String SignatureState = null;

    // Communication method, sends command, retrieves response (and asynchronously incoming status messages)
    private String sendFrameRetrieveResponse(String command) throws JposException {
        Stream.write((command + "\n").getBytes());
        while (true) {
            String resp = getLine();
            if (resp == null)
                break;
            if (getResponseType(command).equals(getResponseType(resp)))
                return resp;
            handleState(resp);
        }
        return null;
    }

    private String[] ResponseTypes = {"scA", "scS", "scX", "scB", "lS", "gO", "gS", "ms", "idA", "idD", "idR"};

    // Valid response must start with returned byte sequence. Returns null for unknown command
    private String getResponseType(String command) {
        String type = null;
        for (String resptype : ResponseTypes) {
            if (resptype.length() <= command.length() && command.substring(0, resptype.length()).equals(resptype)) {
                type = resptype;
                break;
            }
        }
        return type;
    }

    // Retrieve a frame from device (data up to NL), on timeout null
    String getLine() throws JposException {
        Stream.setTimeout(RequestTimeout);
        byte[] resp = new byte[500];
        int count = 0;
        while (true) {
            byte[] data = Stream.read(1);
            if (data.length == 0)
                return null;
            if (data[0] == '\n')
                break;
            if (count < resp.length) {
                resp[count++] = data[0];
            }
            Stream.setTimeout(CharacterTimeout);
        }
        return count > 0 ? new String(Arrays.copyOf(resp, count)) : null;
    }

    private int GateState = 0;
    private int MotionSensorState = 0;
    private byte[] RawData = new byte[0];
    private Point[] PointArray = new Point[0];
    private int RetryCount = 0;

    // Handle status information from device
    private void handleState(String status) throws JposException {
        String type = getResponseType(status);
        if ("scX".equals(type)) {
            // We have signature data
            SignatureCaptureProperties props = (SignatureCaptureProperties)getClaimingInstance(ClaimedSignatureCapture, 0);
            if (status.equals(type) && props != null) {
                if (retrieveSignatureData()) {
                    handleEvent(new SignatureCaptureDataEvent(props.EventSource, RawData.length, RawData, PointArray));
                }
            }
        }
        else if ("gS".equals(type)) {
            // We have a gate state
            int state;
            if (status.length() == 3 && (state = "COco".indexOf(status.charAt(2))) >= 0) {
                RetryCount = 0;
                fireGateStateEvent(state);
                return;
            }
        }
        else if ("ms".equals(type)) {
            // We have a motion sensor status change
            int state;
            if (status.length() == 3 && (state = "sS".indexOf(status.charAt(2))) >= 0) {
                RetryCount = 0;
                fireMotionSensorState(state);
                return;
            }
        }
        if (RetryCount++ < MaxRetry)
            Stream.write(new byte[]{'?', '\n'});
    }

    // Retrieve signature raw data
    private synchronized boolean retrieveSignatureData() throws JposException {
        String rawdata = "";
        Point[] pointarray = new Point[0];
        int retry = 0;
        boolean ended = true;
        for (int block = 1; true; block++) {
            for (retry = 0; retry < MaxRetry; retry++) {
                String req = String.format("scB%05d", block);
                String resp = sendrecv(req);
                try {
                    if (req.equals(resp.substring(0, req.length())) && (resp.length() - req.length()) % 6 == 0) {
                        Point[] blockpoints = new Point[(resp.length() - req.length()) / 6];
                        for (int i = 0, basepos = req.length(); i < blockpoints.length; i++, basepos += 6) {
                            blockpoints[i] = new Point(Integer.parseInt(resp.substring(basepos, basepos + 3), 10),
                                    Integer.parseInt(resp.substring(basepos + 3, basepos + 6), 10));
                            if (ended = (blockpoints[i].x == -1 && blockpoints[i].y == -1))
                                blockpoints[i].x = blockpoints[i].y = 0xffff;
                        }
                        rawdata += resp.substring(req.length());
                        pointarray = Arrays.copyOf(pointarray, pointarray.length + blockpoints.length);
                        System.arraycopy(blockpoints, 0, pointarray, pointarray.length - blockpoints.length, blockpoints.length);
                        if (blockpoints.length != 50 && ended) {
                            RawData = rawdata.getBytes();
                            PointArray = pointarray;
                            return true;
                        }
                        break;  // next block
                    }
                } catch (Exception e) {}
            }
            if (retry == MaxRetry)
                break;
        }
        return false;
    }

    // Common CheckHealth implementation: props: property set, level:  from specific CheckHealth, request: request to
    //      be sent, expectedResponseLen: expected length of valid response
    private void commonCheckHealth(JposCommonProperties props, int level, String request, int expectedResponseLen) {
        String howstr = level == JposConst.JPOS_CH_INTERNAL ? "Internal" : (level == JposConst.JPOS_CH_EXTERNAL ? "External" : "Interactive");
        if (ItemDispenserStates.length == 0) {
            props.CheckHealthText = howstr + " CheckHealth: FAILED: Communication error";
            return;
        }
        boolean interactive;
        if (interactive = (level == JposConst.JPOS_CH_INTERACTIVE))
            synchronizedMessageBox("Press OK to start health test.", "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
        String resp = sendrecv(request);
        if (resp != null && resp.length() == expectedResponseLen) {
            props.CheckHealthText = howstr + " CheckHealth: OK";
        } else if (resp == null) {
            props.CheckHealthText = howstr + " CheckHealth: FAILED: No valid response";
        } else if (resp.length() > request.length()) {
            props.CheckHealthText = howstr + " CheckHealth: FAILED: Error from device: " + resp.substring(request.length());
        } else {
            props.CheckHealthText = howstr + " CheckHealth: FAILED: Unspecific error from device";
        }
        if (interactive)
            synchronizedMessageBox("CheckHealth result:\n" + props.CheckHealthText, "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
    }

    // Common method for setting power state on enable in a specific property set
    private void setPowerStateOnEnable(JposCommonProperties props) throws JposException {
        handleEvent(new JposStatusUpdateEvent(props.EventSource, ItemDispenserStates.length == 0 ? JposConst.JPOS_SUE_POWER_OFF_OFFLINE : JposConst.JPOS_SUE_POWER_ONLINE));
    }

    /*
     * Specific device implementations for Gate follow
     */

    public GateProperties getGateProperties(int index) {
        return new SampleGateProperties();
    }

    class SampleGateProperties extends GateProperties {
        public SampleGateProperties() {
            super(0);
        }

        @Override
        public void initOnOpen() {
            super.initOnOpen();
            CapPowerReporting = UsbToSerial ? JposConst.JPOS_PR_ADVANCED : JposConst.JPOS_PR_STANDARD;
        }

        @Override
        public void initOnEnable(boolean enable) {
            if (enable) {
                if (ItemDispenserStates.length == 0)
                    GateStatus = GateConst.GATE_GS_MALFUNCTION;
                else
                    GateStatus = new int[]{GateConst.GATE_GS_CLOSED, GateConst.GATE_GS_OPEN, GateConst.GATE_GS_BLOCKED, GateConst.GATE_GS_BLOCKED}[GateState];
            }
        }

        @Override
        public void handlePowerStateOnEnable() throws JposException {
            setPowerStateOnEnable(this);
        }

        @Override
        public void deviceEnabled(boolean enable) throws JposException {
            if (enable) {
                if (!Claimed) {
                    startPolling(this);
                    if (ItemDispenserStates.length == 0 && PowerNotify == JposConst.JPOS_PN_DISABLED) {
                        stopPolling();
                        throw new JposException(InIOError ? JposConst.JPOS_E_OFFLINE : JposConst.JPOS_E_FAILURE, "Communication with device disrupted");
                    }
                }
            } else {
                if (!Claimed)
                    stopPolling();
                signalWaiter();
            }
            super.deviceEnabled(enable);
        }

        @Override
        public void claim(int timeout) throws JposException {
            startPolling(this);
            if (ItemDispenserStates.length == 0 && PowerNotify == JposConst.JPOS_PN_DISABLED) {
                stopPolling();
                throw new JposException(InIOError ? JposConst.JPOS_E_OFFLINE : JposConst.JPOS_E_FAILURE, "Communication with device disrupted");
            }
            super.claim(timeout);
        }

        @Override
        public void release() throws JposException {
            super.release();
            stopPolling();
        }

        @Override
        public void checkHealth(int level) throws JposException {
            commonCheckHealth(this, level, "lS17", 4);
        }

        @Override
        public void openGate() throws JposException {
            String resp = sendrecv("gO");
            if (!"gO".equals(resp)) {
                check(resp == null || resp.length() < 2 || !resp.substring(0, 2).equals("gO"), JposConst.JPOS_E_OFFLINE, "Communication error");
                throw new JposException(JposConst.JPOS_E_FAILURE, "Gate error: " + resp.substring(2));
            }
            attachWaiter();
            PollWaiter.signal();
            waitWaiter((RequestTimeout + CharacterTimeout) * MaxRetry);
            releaseWaiter();
            check(GateStatus != GateConst.GATE_GS_OPEN, JposConst.JPOS_E_FAILURE, "Open gate failed");
        }

        @Override
        public void waitForGateClose(int timeout) throws JposException {
            attachWaiter();
            long starttime = System.currentTimeMillis();
            while (GateStatus == GateConst.GATE_GS_OPEN && ItemDispenserStates.length > 0) {
                long realtimeout;
                if (timeout == JposConst.JPOS_FOREVER)
                    realtimeout = SyncObject.INFINITE;
                else if ((realtimeout = timeout - System.currentTimeMillis() + starttime) < 0)
                    break;
                waitWaiter(realtimeout);
            }
            releaseWaiter();
            if (GateStatus == GateConst.GATE_GS_CLOSED) {
                super.waitForGateClose(timeout);
                return;
            }
            check(timeout != JposConst.JPOS_FOREVER && System.currentTimeMillis() - starttime > timeout, JposConst.JPOS_E_TIMEOUT, "WaitForGateClose timed out");
            check(ItemDispenserStates.length == 0, JposConst.JPOS_E_OFFLINE, "Device is off or offline");
            check(GateStatus != GateConst.GATE_GS_OPEN, JposConst.JPOS_E_FAILURE, "Gate is blocked");
        }
    }

    /*
     * Specific device implementations for ItemDispenser follow
     */

    public ItemDispenserProperties getItemDispenserProperties(int index) {
        return new SampleItemDispenserProperties();
    }

    class SampleItemDispenserProperties extends ItemDispenserProperties {
        public SampleItemDispenserProperties() {
            super(0);
        }

        @Override
        public void claim(int timeout) throws JposException {
            startPolling(this);
            if (ItemDispenserStates.length == 0 && PowerNotify == JposConst.JPOS_PN_DISABLED) {
                stopPolling();
                throw new JposException(InIOError ? JposConst.JPOS_E_OFFLINE : JposConst.JPOS_E_FAILURE, "Communication with device disrupted");
            }
            super.claim(timeout);
        }

        @Override
        public void release() throws JposException {
            super.release();
            stopPolling();
        }

        @Override
        public void initOnOpen() {
            super.initOnOpen();
            CapNearEmptySensor = DispenserNearEndCount > 1;
            CapPowerReporting = UsbToSerial ? JposConst.JPOS_PR_ADVANCED : JposConst.JPOS_PR_STANDARD;
        }

        @Override
        public void initOnEnable(boolean enable) {
            if (enable) {
                new ItemDispenserStatusUpdateEvent(EventSource, DispenserState).setAndCheckStatusProperties();
            }
        }

        @Override
        public void handlePowerStateOnEnable() throws JposException {
            setPowerStateOnEnable(this);
        }

        @Override
        public void checkHealth(int level) throws JposException {
            commonCheckHealth(this, level, "lS17", 4);
        }

        @Override
        public void adjustItemCount(int itemCount, int slotNumber) throws JposException {
            check(ItemDispenserStates.length != 10, JposConst.JPOS_E_OFFLINE, "Device not accessible");
            check(itemCount < ItemDispenserStates[slotNumber - 1], JposConst.JPOS_E_ILLEGAL, "Add negative item count not supported");
            int add = itemCount - ItemDispenserStates[slotNumber - 1];
            if (add > 0) {
                String request = String.format("idA%1d%02d", slotNumber - 1, add);
                String resp = sendrecv(request);
                check(resp == null || resp.length() < request.length(), JposConst.JPOS_E_NOSERVICE, "Communication error");
                check(!resp.equals(request) && resp.substring(0, request.length()).equals(request), JposConst.JPOS_E_ILLEGAL, "Error from device: " + resp.substring(request.length()));
                check(!resp.equals(request), JposConst.JPOS_E_FAILURE, "Communication error");
            }
        }

        @Override
        public void dispenseItem(int[] numItem, int slotNumber) throws JposException {
            int currentItems = ItemDispenserStates[slotNumber - 1];
            check(ItemDispenserStates.length != 10, JposConst.JPOS_E_OFFLINE, "Device not accessible");
            check(numItem[0] > currentItems, JposConst.JPOS_E_ILLEGAL, "Not enough items in slot " + slotNumber);
            String request = String.format("idD%1d%02d", slotNumber - 1, numItem[0]);
            String resp = sendrecv(request);
            check(resp == null || resp.length() < request.length(), JposConst.JPOS_E_NOSERVICE, "Communication error");
            check(!resp.equals(request) && resp.substring(0, request.length()).equals(request), JposConst.JPOS_E_ILLEGAL, "Error from device: " + resp.substring(request.length()));
            check(!resp.equals(request), JposConst.JPOS_E_FAILURE, "Communication error");
            numItem[0] = currentItems - numItem[0];
        }

        @Override
        public void readItemCount(int[] itemCount, int slotNumber) throws JposException {
            String request = "idR";
            String resp = sendrecv(request);
            check (resp == null || resp.length() != request.length() + 20, JposConst.JPOS_E_NOSERVICE, "Communication error");
            int slotindex = request.length() + (slotNumber - 1) * 2;
            try {
                itemCount[0] = Integer.parseInt(resp.substring(slotindex, slotindex + 2));
            } catch (NumberFormatException e) {
                throw new JposException(JposConst.JPOS_E_FAILURE, "Bad count for slot " + slotNumber + ": " + resp.substring(slotindex, slotindex + 2), e);
            }
        }
    }

    /*
     * Specific device implementations for Lights follow
     */

    public LightsProperties getLightsProperties(int index) {
        return new SampleLightsProperties();
    }

    class SampleLightsProperties extends LightsProperties {
        protected SampleLightsProperties() {
            super(0);
        }

        @Override
        public void initOnOpen() {
            super.initOnOpen();
            CapPowerReporting = UsbToSerial ? JposConst.JPOS_PR_ADVANCED : JposConst.JPOS_PR_STANDARD;
        }

        @Override
        public void claim(int timeout) throws JposException {
            startPolling(this);
            if (ItemDispenserStates.length == 0 && PowerNotify == JposConst.JPOS_PN_DISABLED) {
                stopPolling();
                throw new JposException(InIOError ? JposConst.JPOS_E_OFFLINE : JposConst.JPOS_E_FAILURE, "Communication with device disrupted");
            }
            super.claim(timeout);
        }

        @Override
        public void release() throws JposException {
            super.release();
            stopPolling();
        }

        @Override
        public void handlePowerStateOnEnable() throws JposException {
            setPowerStateOnEnable(this);
        }

        @Override
        public void checkHealth(int level) throws JposException {
            commonCheckHealth(this, level, "lS17", 4);
        }

        @Override
        public void switchOff(int lightNumber) throws JposException {
            String request = "lS" + lightNumber + "0";
            String resp = sendrecv(request);
            check(!request.equals(resp), JposConst.JPOS_E_FAILURE, "Communication error");
        }

        @Override
        public void switchOn(int lightNumber, int blinkOnCycle, int blinkOffCycle, int color, int alarm) throws JposException {
            int[] colors = {
                    LightsConst.LGT_COLOR_CUSTOM1, LightsConst.LGT_COLOR_PRIMARY, LightsConst.LGT_ALARM_CUSTOM2,
                    LightsConst.LGT_COLOR_CUSTOM3, LightsConst.LGT_COLOR_CUSTOM5, LightsConst.LGT_COLOR_CUSTOM4
            };
            for (int i = 0; i < colors.length; i++) {
                if (color == colors[i]) {
                    String request = "lS" + lightNumber + (i + 1);
                    String resp = sendrecv(request);
                    check(!request.equals(resp), JposConst.JPOS_E_FAILURE, "Communication error");
                    return;
                }
            }
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid color: " + color);
        }
    }

    /*
     * Specific device implementations for MotionSensor follow
     */

    public MotionSensorProperties getMotionSensorProperties(int index) {
        return new SampleMotionSensorProperties();
    }

    class SampleMotionSensorProperties extends MotionSensorProperties {
        public SampleMotionSensorProperties() {
            super(0);
        }

        @Override
        public void initOnOpen() {
            super.initOnOpen();
            CapPowerReporting = UsbToSerial ? JposConst.JPOS_PR_ADVANCED : JposConst.JPOS_PR_STANDARD;
        }

        @Override
        public void initOnEnable(boolean enable) {
            if (enable)
                Motion = MotionSensorState != 0;
        }

        @Override
        public void handlePowerStateOnEnable() throws JposException {
            setPowerStateOnEnable(this);
        }

        @Override
        public void deviceEnabled(boolean enable) throws JposException {
            if (enable) {
                startPolling(this);
                if (ItemDispenserStates.length == 0 && PowerNotify == JposConst.JPOS_PN_DISABLED) {
                    stopPolling();
                    throw new JposException(InIOError ? JposConst.JPOS_E_OFFLINE : JposConst.JPOS_E_FAILURE, "Communication with device disrupted");
                }
            } else {
                stopPolling();
                signalWaiter();
            }
            super.deviceEnabled(enable);
        }

        @Override
        public void checkHealth(int level) throws JposException {
            commonCheckHealth(this, level, "lS17", 4);
        }

        @Override
        public void waitForMotion(int timeout) throws JposException {
            attachWaiter();
            long starttime = System.currentTimeMillis();
            while (!Motion && ItemDispenserStates.length > 0) {
                long realtimeout;
                if (timeout == JposConst.JPOS_FOREVER)
                    realtimeout = SyncObject.INFINITE;
                else if ((realtimeout = timeout - System.currentTimeMillis() + starttime) < 0)
                    break;
                waitWaiter(realtimeout);
            }
            releaseWaiter();
            if (Motion) {
                super.waitForMotion(timeout);
                return;
            }
            check(timeout != JposConst.JPOS_FOREVER && System.currentTimeMillis() - starttime > timeout, JposConst.JPOS_E_TIMEOUT, "WaitForMotion timed out");
            check(ItemDispenserStates.length == 0, JposConst.JPOS_E_OFFLINE, "Device is off or offline");
        }
    }

    /*
     * Specific device implementations for SignatureCapture follow
     */

    public SignatureCaptureProperties getSignatureCaptureProperties(int index) {
        return new SampleSignatureCaptureProperties();
    }

    class SampleSignatureCaptureProperties extends SignatureCaptureProperties {
        public SampleSignatureCaptureProperties() {
            super(0);
        }

        @Override
        public void initOnOpen() {
            super.initOnOpen();
            CapPowerReporting = UsbToSerial ? JposConst.JPOS_PR_ADVANCED : JposConst.JPOS_PR_STANDARD;
        }

        @Override
        public void claim(int timeout) throws JposException {
            startPolling(this);
            if (ItemDispenserStates.length == 0 && PowerNotify == JposConst.JPOS_PN_DISABLED) {
                stopPolling();
                throw new JposException(InIOError ? JposConst.JPOS_E_OFFLINE : JposConst.JPOS_E_FAILURE, "Communication with device disrupted");
            }
            super.claim(timeout);
        }

        @Override
        public void release() throws JposException {
            super.release();
            stopPolling();
        }

        @Override
        public void initOnEnable(boolean enable) {
            if (enable) {
                RawData = new byte[0];
                PointArray = new Point[0];
            }
        }

        @Override
        public void handlePowerStateOnEnable() throws JposException {
            setPowerStateOnEnable(this);
        }

        @Override
        public void deviceEnabled(boolean enable) throws JposException {
            if (enable) {
                String req = "scA";
                String resp = sendrecv(req);
                if (!req.equals(resp) && PowerNotify == JposConst.JPOS_PN_DISABLED) {
                    check(InIOError, JposConst.JPOS_E_OFFLINE, "Device not online");
                    check(resp == null || resp.length() < req.length() || !resp.substring(0, req.length()).equals(req), JposConst.JPOS_E_NOSERVICE, "Communication with device disrupted");
                    throw new JposException(JposConst.JPOS_E_FAILURE, "Device error: " + resp.substring(req.length()));
                }
            }
            super.deviceEnabled(enable);
        }

        @Override
        public void checkHealth(int level) throws JposException {
            commonCheckHealth(this, level, "lS17", 4);
        }

        @Override
        public void clearDataProperties() {
            super.clearDataProperties();
            RawData = new byte[0];
            PointArray = new Point[0];
        }

        @Override
        public void beginCapture(String formName) throws JposException {
            String req = "scS";
            String resp = sendrecv(req);
            if (!req.equals(resp) && PowerNotify == JposConst.JPOS_PN_DISABLED) {
                check(InIOError, JposConst.JPOS_E_OFFLINE, "Device not online");
                check(resp == null || resp.length() < req.length() || !resp.substring(0, req.length()).equals(req), JposConst.JPOS_E_NOSERVICE, "Communication with device disrupted");
                check(resp.substring(req.length()).equals("InProgress"), JposConst.JPOS_E_ILLEGAL, "Signature capture in progress");
                throw new JposException(JposConst.JPOS_E_FAILURE, "Device error: " + resp.substring(req.length()));
            }
        }

        @Override
        public void endCapture() throws JposException {
            String req = "scX";
            String resp = sendrecv(req);
            if ((resp.equals(req) || resp.equals(req + "Disabled")) && retrieveSignatureData()) {
                RawData = Device.this.RawData;
                PointArray = Device.this.PointArray;
                return;
            }
            check(resp.equals(req + "Disabled"), JposConst.JPOS_E_ILLEGAL, "Signature capture not in progress");
            check(InIOError || resp == null, JposConst.JPOS_E_OFFLINE, "Device not online");
            check(resp.substring(0, req.length()).equals(req), JposConst.JPOS_E_FAILURE, "Device error: " + resp.substring(req.length()));
            check(!resp.substring(0, req.length()).equals(req), JposConst.JPOS_E_NOSERVICE, "Communication with device disrupted");
        }
    }
}
