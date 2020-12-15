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

package SampleUdpDevice;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.belt.BeltProperties;
import de.gmxhome.conrad.jpos.jpos_base.belt.BeltStatusUpdateEvent;
import de.gmxhome.conrad.jpos.jpos_base.cashdrawer.*;
import jpos.BeltConst;
import jpos.CashDrawerConst;
import jpos.JposConst;
import jpos.JposException;
import jpos.config.JposEntry;
import org.apache.log4j.Level;

import javax.swing.*;
import java.util.Arrays;

/**
 * JposDevice based implementation of a JavaPOS CashDrawer device service implementation for the
 * sample device implemented in SampleUdpDevice.tcl. It becomes the following JavaPOS
 * device services in combination with the corresponding <i>DeviceClass</i>Server classes:
 * Belt and CashDrawer.
 * <p>The simulator supports the following commands:<ul>
 *   <li>CashDrawer:
 *     <ul>
 *       <li> 'DRAWER:GetState': Status request. Sends back 'DRAWER:0' if drawer is closed, otherwise 'DRAWER:1'.</li>
 *       <li> 'DRAWER:Open': Opens the drawer. Sends back the new status which is always 'DRAWER:1'.</li>
 *     </ul>
 *   </li>
 *   <li>Belt:
 *     <ul>
 *       <li> 'BELT:GetState': Status request. Sends back 'BELT:<i>WXYZ</i>' with the following values for <i>W, X, Y</i>
 *       and <i>Z</i>:
 *         <ul>
 *           <li><i>W</i>: The current belt speed, 0 (stopped), 1 (slow) or 2 (faster),</li>
 *           <li><i>X</i>: The current motor state, 0 (ok), 1 (overheated) or 2 (defective),</li>
 *           <li><i>Y</i>: The current security flap state, 0 (closed) or 1 (open) and</li>
 *           <li><i>Z</i>: The current light barrier state, 0 (free) or 1 (interrupted).</li>
 *         </ul>
 *       </li>
 *       <li> 'BELT:Speed<i>X</i>': Sets the belt speed. Valid values for <i>X</i> are 0 (stopped), 1 (slow) and
 *         2 (faster). Sends back the belt status in the same format as for command BELT:GetState.
 *       </li>
 *     </ul>
 *   </li>
 * </ul>
 * All frames have variable length. Communication will be made with UDP sockets.
 * <br> Multiple commands can be sent in a single frame, separated by comma (,). The corresponding responses will
 * be sent in one frame, separated by comma as well.<p>
 * Here a full list of all device specific properties that can be changed via jpos.xml:
 * <ul>
 *     <li>MaxRetry: Specifies the maximum number of retries. Default: 2.</li>
 *     <li>OwnPort: Integer value between 0 and 65535 specifying the UDP port used for communication with the device
 *     simulator. Default: 0 (for random port number selected by operating system).</li>
 *     <li>PollDelay: Minimum time between status requests, in milliseconds. Status requests will be used to monitor the
 *     device state. Default: 200.</li>
 *     <li>Port: Operating system specific name of the UDP address to be used for
 *     communication with the device simulator. Names are of the form IPv4:port, where IPv4 is the IP address of the
 *     device and port its UDP port.</li>
 *     <li>RequestTimeout: Maximum time, in milliseconds, between sending a command to the simulator and getting the
 *     first byte of its response. Default: 200.</li>
 *     <li>UseClientIO: Specifies whether UdpClientIOProcessor or UdpIOProcessor shall be used for communication. If
 *     true, UdpClientIOProcessor will be used, otherwise UdpIOProcessor. Default: true.</li>
 * </ul>
 */
public class Device extends JposDevice implements Runnable {
    /**
     * Constructor. id specifies the server to be connected in format host:port.
     *
     * @param id IPv4 target address and port
     */
    protected Device(String id) {
        super(id);
        cashDrawerInit(1);
        beltInit(1);
        PhysicalDeviceDescription = "UDP device simulator";
        PhysicalDeviceName = "UDP device Simulator";
        CapPowerReporting = JposConst.JPOS_PR_STANDARD;
    }

    private int OwnPort = 0;
    private int PollDelay = 200;
    private int RequestTimeout = 200;
    private int MaxRetry = 2;
    private boolean UseClientIO = true;

    @Override
    public void checkProperties(JposEntry entry) throws JposException {
        super.checkProperties(entry);
        try {
            Object o;
            if ((o = entry.getPropertyValue("OwnPort")) != null) {
                if ((OwnPort = Integer.parseInt(o.toString())) < 0 || OwnPort >= 0xffff)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid source port.");
            }
            if ((o = entry.getPropertyValue("PollDelay")) != null) {
                if ((PollDelay = Integer.parseInt(o.toString())) <= 0)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid poll delay: " + PollDelay);
            }
            if ((o = entry.getPropertyValue("RequestTimeout")) != null) {
                if ((RequestTimeout = Integer.parseInt(o.toString())) <= 0)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid request timeout: " + RequestTimeout);
            }
            if ((o = entry.getPropertyValue("MaxRetry")) != null) {
                if ((MaxRetry = Integer.parseInt(o.toString())) <= 0)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid maximum retry count: " + MaxRetry);
            }
            if ((o = entry.getPropertyValue("UseClientIO")) != null) {
                UseClientIO = Boolean.parseBoolean(o.toString());
            }
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property", e);
        }
    }

    @Override
    public void changeDefaults(CashDrawerProperties props) {
        props.DeviceServiceDescription = "CashDrawer service for sample UDP device simulator";
        props.DeviceServiceVersion = 1014001;
    }

    @Override
    public void changeDefaults(BeltProperties props) {
        props.DeviceServiceDescription = "Belt service for sample UDP device simulator";
        props.DeviceServiceVersion = 1014001;
        props.CapSpeedStepsForward = 2;
    }

    private UniqueIOProcessor Target = null;
    private boolean Offline = true;

    private JposException initPort() {
        try {
            if (UseClientIO)
                ((UdpClientIOProcessor) (Target = new UdpClientIOProcessor(this, ID))).setParam(OwnPort);
            else
                ((UdpIOProcessor) (Target = new UdpIOProcessor(this, OwnPort))).setTarget(ID);
            Target.open(Offline);
            Offline = false;
        } catch (JposException e) {
            Target = null;
            return e;
        }
        return null;
    }

    private JposException closePort() {
        JposException e = null;
        if (Target != null) {
            for (int i = 0; i < 2; i++) {
                try {
                    Target.close();
                } catch (JposException ee) {
                    e = ee;
                }
            }
            Target = null;
        }
        return e;
    }

    private boolean connectionOffline() {
        if (Target == null) {
            JposException e = initPort();
            if (e != null)
                return Offline = true;
        }
        return false;
    }

    private interface ReturnValueChecker {
        /**
         * Checks whether response is a valid response for command
         * @param command Both parts of a command.
         * @param response Both parts of a response, first parts of command and response must be equal.
         * @return Return values specify the result of validation. The following values are supported:
         * <ul>
         *     <li>1: The response is a valid response.</li>
         *     <li>0: This ReturnValueChecker cannot validate this command.</li>
         *     <li>-1: The response is an invalid or unknown value.</li>
         *     <li>-2: The command is invalid.</li>
         * </ul>
         */
        public int check(String[] command, String[] response);
    }

    private class CommonChecker implements ReturnValueChecker {
        @Override
        public int check(String[] command, String[] response) {
            if (response.length != 2)
                return -1;
            if (!response[0].equals(command[0]))
                return response[0].equals("INVALID") ? -2 : -1;
            return response[1].matches("Inv!.*") ? -2 : 0;
        }
    }

    private class DrawerChecker extends CommonChecker {
        @Override
        public int check(String[] command, String[] response) {
            int rc = super.check(command, response);
            if (0 == rc && command[0].equals("DRAWER")) {
                rc = response[1].matches("[01]") ? 1 : -1;
            }
            return rc;
        }
    }

    private class BeltChecker extends CommonChecker {
        @Override
        public int check(String[] command, String[] response) {
            int rc = super.check(command, response);
            if (0 == rc) {
                if (0 == rc && command[0].equals("BELT")) {
                    char[] state = response[1].toCharArray();
                    if (state.length != 4)
                        rc = -1;
                    else if (state[0] < '0' || state[0] > '2')
                        rc = -1;
                    else if (state[1] < '0' || state[1] > '2')
                        rc = -1;
                    else if (state[2] < '0' || state[2] > '1')
                        rc = -1;
                    else if (state[3] < '0' || state[3] > '1')
                        rc = -1;
                    else
                        rc = 1;
                }
            }
            return rc;
        }
    }

    private ReturnValueChecker[] checker = {
            new DrawerChecker(),
            new BeltChecker()
    };

    private synchronized String sendResp(String commands) {
        if (connectionOffline())
            return null;
        try {
            byte[] responses;
            byte[] request = commands.getBytes();
            String[] commandParts = commands.split(",");
            Target.flush();
            long starttime = System.currentTimeMillis();
            long acttime = starttime;
            for (int count = 0; count < MaxRetry; count++) {
                Target.write(request);
                do {
                    Target.setTimeout((int)(RequestTimeout - (acttime - starttime)));
                    responses = Target.read(100);
                    if (UseClientIO || responses.length == 0 || Target.getSource().equals(Target.getTarget()))
                        break;
                } while ((acttime = System.currentTimeMillis()) - starttime < RequestTimeout);
                String[] responseParts = new String(responses).split(",");
                if (commandParts.length == responseParts.length) {
                    int checkresult = 1;
                    for (int index = 0; index < commandParts.length && checkresult == 1; index++) {
                        String[] respparts = responseParts[index].split(":");
                        String[] cmdparts = commandParts[index].split(":");
                        for (ReturnValueChecker check : checker) {
                            if ((checkresult = check.check(cmdparts, respparts)) != 0)
                                break;
                        }
                    }
                    if (checkresult == 1)
                        return new String(responses);
                    else if (checkresult == -2)
                        count = MaxRetry;   // Invalid command: Force abort
                }
            }
        } catch (JposException e) {
            log(Level.TRACE, ID + ": IO error: " + e.getMessage());
        }
        Offline = true;
        closePort();
        return null;
    }

    private boolean ToBeFinished = false;
    private SyncObject PollWaiter = new SyncObject();

    private boolean DrawerOpen = false;

    private char[] BeltState = {BeltOff, BeltMotorOK, BeltSecurityFlapClosed, BeltLightBarrierFree};
    private final static int BeltSpeed = 0;
    private final static int BeltMotor = 1;
    private final static int BeltSecurityFlap = 2;
    private final static int BeltLightBarrier = 3;
    private final static char BeltOff = '0';
    private final static char BeltSlow = '1';
    private final static char BeltFast = '2';
    private final static char BeltMotorOK = '0';
    private final static char BeltMotorOverheat = '1';
    private final static char BeltMotorKO = '2';
    private final static char BeltLightBarrierFree = '0';
    private final static char BeltLightBarrierInterrupted = '1';
    private final static char BeltSecurityFlapClosed = '0';
    private final static char BeltSecurityFlapOpen = '1';

    @Override
    public void run() {
        while (!ToBeFinished) {
            boolean oldoffline = Offline;
            boolean oldDrawerState = DrawerOpen;
            char[] oldBeltState = BeltState;
            prepareSignalStatusWaits(CashDrawers[0]);
            String resp = sendResp("DRAWER:GetState,BELT:GetState");
            if (resp != null) {
                String[] resps = resp.split(",");
                DrawerOpen = resps[0].split(":")[1].equals("1");
                BeltState = resps[1].split(":")[1].toCharArray();
            }
            if (oldoffline && oldoffline != Offline) {
                sendPowerStateEvents();
                oldoffline = Offline;
            }
            if (!Offline && oldDrawerState != DrawerOpen) {
                sendDrawerStateEvent();
                signalStatusWaits(CashDrawers[0]);
            }
            if (!Offline && !Arrays.equals(oldBeltState, BeltState)) {
                if (sendBeltStateEvents(oldBeltState)) {
                    sendResp("BELT:Speed0");
                }
            }
            if (Offline && oldoffline != Offline) {
                sendPowerStateEvents();
                signalStatusWaits(CashDrawers[0]);
                signalStatusWaits(Belts[0]);
            }
            if (StartPollingWaiter != null) {
                StartPollingWaiter.signalWaiter();
                StartPollingWaiter = null;
            }
            PollWaiter.suspend(PollDelay);
        }
    }

    private boolean sendBeltStateEvents(char[] oldstates) {
        try {
            BeltProperties belt = (BeltProperties)getClaimingInstance(ClaimedBelt, 0);
            if (belt != null) {
                if (oldstates[BeltLightBarrier] != BeltState[BeltLightBarrier]) {
                    sendLightBarrierEvent(belt);
                }
                if (oldstates[BeltSecurityFlap] != BeltState[BeltSecurityFlap]) {
                    sendSecurityFlapEvent(belt);
                }
                if (oldstates[BeltMotor] != BeltState[BeltMotor]) {
                    sendMotorHealthEvent(belt);
                }
                else if (oldstates[BeltSpeed] != BeltOff && BeltState[BeltSpeed] == BeltOff) {
                    int state = belt.AutoStopForward ? BeltConst.BELT_SUE_AUTO_STOP : BeltConst.BELT_SUE_TIMEOUT_STOP;
                    handleEvent(new BeltStatusUpdateEvent(belt.EventSource, state));
                    return BeltState[BeltMotor] == BeltMotorOK && BeltState[BeltSecurityFlap] == BeltSecurityFlapClosed &&
                            BeltState[BeltLightBarrier] == BeltLightBarrierInterrupted;
                }
            }
        } catch (JposException e) {
        }
        return false;
    }

    private void sendMotorHealthEvent(BeltProperties belt) throws JposException {
        int state;
        if (BeltState[BeltMotor] != BeltMotorOK) {
            state = BeltState[BeltMotor] == BeltMotorOverheat ?
                    BeltConst.BELT_SUE_MOTOR_OVERHEATING :
                    BeltConst.BELT_SUE_MOTOR_FUSE_DEFECT;
        }
        else {
            state = BeltState[BeltSecurityFlap] == BeltSecurityFlapOpen ?
                    BeltConst.BELT_SUE_EMERGENCY_STOP :
                    BeltConst.BELT_SUE_SAFETY_STOP;
        }
        handleEvent(new BeltStatusUpdateEvent(belt.EventSource, state));
        signalStatusWaits(Belts[0]);
    }

    private void sendSecurityFlapEvent(BeltProperties belt) throws JposException {
        int state = BeltState[BeltSecurityFlap] == BeltSecurityFlapOpen ?
                BeltConst.BELT_SUE_SECURITY_FLAP_FORWARD_OPENED :
                BeltConst.BELT_SUE_SECURITY_FLAP_FORWARD_CLOSED;
        handleEvent(new BeltStatusUpdateEvent(belt.EventSource, state));
    }

    private void sendLightBarrierEvent(BeltProperties belt) throws JposException {
        int state = BeltState[BeltLightBarrier] == BeltLightBarrierInterrupted ?
                BeltConst.BELT_SUE_LIGHT_BARRIER_FORWARD_INTERRUPTED :
                BeltConst.BELT_SUE_LIGHT_BARRIER_FORWARD_OK;
        handleEvent(new BeltStatusUpdateEvent(belt.EventSource, state));
    }

    private void sendDrawerStateEvent() {
        try {
            JposCommonProperties props = getPropertySetInstance(CashDrawers, 0, 0);
            if (props != null) {
                int state = DrawerOpen ? CashDrawerConst.CASH_SUE_DRAWEROPEN : CashDrawerConst.CASH_SUE_DRAWERCLOSED;
                handleEvent(new CashDrawerStatusUpdateEvent(props.EventSource, state));
            }
        } catch (JposException e) {
        }
    }

    private void sendPowerStateEvents() {
        int state = Offline ? JposConst.JPOS_SUE_POWER_OFF_OFFLINE : JposConst.JPOS_SUE_POWER_ONLINE;
        try {
            JposCommonProperties props = getPropertySetInstance(CashDrawers, 0, 0);
            if (props != null)
                handleEvent(new CashDrawerStatusUpdateEvent(props.EventSource, state));
            if ((props = getClaimingInstance(ClaimedBelt, 0)) != null)
                handleEvent(new BeltStatusUpdateEvent(props.EventSource, state));
        } catch (JposException e) {}
    }

    private int[] OpenCount = { 0 };

    private synchronized int changeOpenCount(int value) {
        OpenCount[0] += value;
        return OpenCount[0];
    }

    private JposCommonProperties StartPollingWaiter = null;
    private Thread StateWatcher;

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

    class SampleUdpDeviceCashDrawerAccessor extends CashDrawerProperties {
        /**
         * Constructor. Uses device index 0 implicitly because sample implementation supports only one cash drawer.
         */
        public SampleUdpDeviceCashDrawerAccessor() {
            super(0);
        }

        @Override
        public void initOnEnable(boolean enable) {
            if (enable) {
                DrawerOpened = DrawerOpen;
            }
        }

        @Override
        public void handlePowerStateOnEnable() throws JposException {
            handleEvent(new JposStatusUpdateEvent(EventSource, Offline ? JposConst.JPOS_SUE_POWER_OFF_OFFLINE : JposConst.JPOS_SUE_POWER_ONLINE));
        }

        @Override
        public void deviceEnabled(boolean enable) throws JposException {
            if (enable) {
                if (!Claimed) {
                    startPolling(this);
                    if (Offline && PowerNotify == JposConst.JPOS_PN_DISABLED) {
                        stopPolling();
                        throw new JposException(JposConst.JPOS_E_OFFLINE, "Communication with device disrupted");
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
            if (Offline && PowerNotify == JposConst.JPOS_PN_DISABLED) {
                stopPolling();
                throw new JposException(JposConst.JPOS_E_OFFLINE, "Communication with device disrupted");
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
            String how = level == JposConst.JPOS_CH_INTERNAL ? "Internal" : (level == JposConst.JPOS_CH_EXTERNAL ? "External" : "Interactive");
            if (Offline)
                CheckHealthText = how + " Checkhealth: Offline";
            else {
                CheckHealthText = how + " Checkhealth: OK";
                if (level != JposConst.JPOS_CH_INTERNAL) {
                    boolean interactive;
                    if (interactive = (level == JposConst.JPOS_CH_INTERACTIVE))
                        synchronizedMessageBox("Press OK to start health test.", "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
                    try {
                        openDrawer();
                        if (!DrawerOpened)
                            CheckHealthText = how + "Checkhealth: Drawer open failed";
                    } catch (JposException e) {
                        CheckHealthText = how + "Checkhealth: Offline";
                    }
                    if (interactive)
                        synchronizedMessageBox("CheckHealth result:\n" + CheckHealthText, "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }

        @Override
        public void openDrawer() throws JposException {
            attachWaiter();
            String state = sendResp("DRAWER:Open");
            if (!DrawerOpened) {
                PollWaiter.signal();
                waitWaiter(RequestTimeout * MaxRetry);
            }
            releaseWaiter();
            check(state == null, JposConst.JPOS_E_OFFLINE, "Communication failure");
            super.openDrawer();
        }

        @Override
        public void waitForDrawerClose() throws JposException {
            attachWaiter();
            while (DrawerOpen && !Offline && DeviceEnabled)
                waitWaiter(SyncObject.INFINITE);
            releaseWaiter();
            check(Offline, JposConst.JPOS_E_OFFLINE, "Device offline");
            check(!DeviceEnabled, JposConst.JPOS_E_ILLEGAL, "Device not enabled");
            super.waitForDrawerClose();
        }
    }

    @Override
    public CashDrawerProperties getCashDrawerProperties(int index) {
        return new SampleUdpDeviceCashDrawerAccessor();
    }

    /**
     * This sample implementation does not fully implement what a Belt service should implement.
     * Since the simulator performs always an AutoStop immediately after the light barrier has been interrupted and the
     * time delay between interruption of the light barrier and stopping the motor cannot be configured,
     * the following restrictions have been made:
     * <ul>
     *     <li>Any value set in AutoStopForwardDelayTime will be ignored,</li>
     *     <li>If AutoStopForward if false and the device stops the belt automatically, a StatusUpdateEvent with
     *         status value SUE_TIMEOUT_STOP will be fired. If AutoStopForward is true, the status value is
     *         SUE_AUTO_STOP.
     *     </li>
     * </ul>
     */
    class SampleUdpDeviceBeltAccessor extends BeltProperties {
        /**
         * Constructor. Uses device index 0 implicitly because sample implementation supports only one belt.
         */
        protected SampleUdpDeviceBeltAccessor() {
            super(0);
        }

        @Override
        public void initOnEnable(boolean enable) {
            if (enable) {
                if (BeltState != null) {
                    LightBarrierForwardInterrupted = BeltState[BeltLightBarrier] == BeltLightBarrierInterrupted;
                    SecurityFlapForwardOpened = BeltState[BeltSecurityFlap] == BeltSecurityFlapOpen;
                    if (BeltState[BeltSpeed] != BeltOff)
                        MotionStatus = BeltConst.BELT_MT_FORWARD;
                    else if (BeltState[BeltMotor] != BeltMotorOK)
                        MotionStatus = BeltConst.BELT_MT_MOTOR_FAULT;
                    else if (BeltState[BeltSecurityFlap] == BeltSecurityFlapOpen)
                        MotionStatus = BeltConst.BELT_MT_EMERGENCY;
                    else
                        MotionStatus = BeltConst.BELT_MT_STOPPED;
                }
            }
        }

        @Override
        public void handlePowerStateOnEnable() throws JposException {
            handleEvent(new JposStatusUpdateEvent(EventSource, Offline ? JposConst.JPOS_SUE_POWER_OFF_OFFLINE : JposConst.JPOS_SUE_POWER_ONLINE));
        }

        @Override
        public void claim(int timeout) throws JposException {
            startPolling(this);
            if (Offline && PowerNotify == JposConst.JPOS_PN_DISABLED) {
                stopPolling();
                throw new JposException(JposConst.JPOS_E_OFFLINE, "Communication with device disrupted");
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
            String how = level == JposConst.JPOS_CH_INTERNAL ? "Internal" : (level == JposConst.JPOS_CH_EXTERNAL ? "External" : "Interactive");
            if (Offline)
                CheckHealthText = how + " Checkhealth: Offline";
            else {
                CheckHealthText = how + " Checkhealth: OK";
                if (level != JposConst.JPOS_CH_INTERNAL) {
                    boolean interactive;
                    if (interactive = (level == JposConst.JPOS_CH_INTERACTIVE))
                        synchronizedMessageBox("Press OK to start health test.", "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
                    try {
                        moveForward(CapSpeedStepsForward);
                        if (MotionStatus != BeltConst.BELT_MT_FORWARD)
                            CheckHealthText = how + "Checkhealth: Starting Belt failed";
                    } catch (JposException e) {
                        CheckHealthText = how + "Checkhealth: Error: " + e.getMessage();
                    }
                    if (interactive)
                        synchronizedMessageBox("CheckHealth result:\n" + CheckHealthText, "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }

        @Override
        public void moveForward(int speed) throws JposException {
            Device.check(BeltState[BeltMotor] != BeltMotorOK || BeltState[BeltSecurityFlap] == BeltSecurityFlapOpen, JposConst.JPOS_E_FAILURE, "Hardware not in operational state");
            Device.check(BeltState[BeltLightBarrier] != BeltLightBarrierFree, JposConst.JPOS_E_ILLEGAL, "Not allowed in current state");
            String[] cmd = {"BELT:Speed1", "BELT:Speed2"};
            attachWaiter();
            char[] prevState = BeltState;
            String state = sendResp(cmd[speed - 1]);
            if ((prevState[BeltSpeed] == BeltFast) != (speed == CapSpeedStepsForward)) {
                PollWaiter.signal();
                waitWaiter(RequestTimeout * MaxRetry);
            }
            releaseWaiter();
            prevState = BeltState;
            check(prevState == null , JposConst.JPOS_E_OFFLINE, "Communication failure");
            check((prevState[BeltSpeed] == BeltFast) != (speed == CapSpeedStepsForward), JposConst.JPOS_E_FAILURE, "Speed " + speed + " could not be set");
        }

        @Override
        public void resetBelt() throws JposException {
            Device.check(BeltState[BeltMotor] != BeltMotorOK || BeltState[BeltSecurityFlap] == BeltSecurityFlapOpen, JposConst.JPOS_E_FAILURE, "Hardware not in operational state");
            attachWaiter();
            char[] prevState = BeltState;
            String state = sendResp("BELT:Speed0");
            if (prevState[BeltSpeed] != BeltOff) {
                PollWaiter.signal();
                waitWaiter(RequestTimeout * MaxRetry);
            }
            releaseWaiter();
            prevState = BeltState;
            check(prevState == null , JposConst.JPOS_E_OFFLINE, "Communication failure");
            check(prevState[BeltSpeed] != BeltOff, JposConst.JPOS_E_FAILURE, "Belt could not be stopped");
        }

        @Override
        public void stopBelt() throws JposException {
            Device.check(BeltState[BeltMotor] != BeltMotorOK || BeltState[BeltSecurityFlap] == BeltSecurityFlapOpen, JposConst.JPOS_E_FAILURE, "Hardware not in operational state");
            attachWaiter();
            char[] prevState = BeltState;
            String state = sendResp("BELT:Speed0");
            if (prevState[BeltSpeed] != BeltOff) {
                PollWaiter.signal();
                waitWaiter(RequestTimeout * MaxRetry);
            }
            releaseWaiter();
            prevState = BeltState;
            check(prevState == null , JposConst.JPOS_E_OFFLINE, "Communication failure");
            check(prevState[BeltSpeed] != BeltOff, JposConst.JPOS_E_FAILURE, "Belt could not be stopped");
        }
    }

    @Override
    public BeltProperties getBeltProperties(int index) {
        return new SampleUdpDeviceBeltAccessor();
    }
}
