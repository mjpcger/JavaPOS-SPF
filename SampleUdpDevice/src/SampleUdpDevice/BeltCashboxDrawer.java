/*
 * Copyright 2021 Martin Conrad
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
import de.gmxhome.conrad.jpos.jpos_base.belt.*;
import de.gmxhome.conrad.jpos.jpos_base.billacceptor.*;
import de.gmxhome.conrad.jpos.jpos_base.billdispenser.*;
import de.gmxhome.conrad.jpos.jpos_base.cashchanger.*;
import de.gmxhome.conrad.jpos.jpos_base.cashdrawer.*;
import de.gmxhome.conrad.jpos.jpos_base.coinacceptor.*;
import de.gmxhome.conrad.jpos.jpos_base.coindispenser.*;
import jpos.*;

import java.util.*;


import static de.gmxhome.conrad.jpos.jpos_base.SyncObject.INFINITE;
import static javax.swing.JOptionPane.*;
import static jpos.BeltConst.*;
import static jpos.BillAcceptorConst.*;
import static jpos.BillDispenserConst.*;
import static jpos.CashChangerConst.*;
import static jpos.CashDrawerConst.*;
import static jpos.CoinAcceptorConst.*;
import static jpos.CoinDispenserConst.*;
import static jpos.JposConst.*;

/**
 * JposDevice based implementation of some JavaPOS device service implementations for the
 * sample device implemented in SampleUdpDevice.tcl. It becomes the following JavaPOS
 * device services in combination with the corresponding <i>DeviceClass</i>Server classes:
 * Belt, BillAcceptor, BillDispenser, CashChanger, CashDrawer, CoinAcceptor and CoinDispenser.
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
 *   <li>Cashbox:
 *     <ul>
 *       <li> 'CASHBOX:GetState':  Command to retrieve the current state: Sends back 'GetStateNMO' where
 *       <ul>
 *         <li>N is 0: (one slot)Empty, 1: (one slot) Low, 2: OK,</li>
 *         <li>M is 0: (one slot) Full, 1: (one slot) High, 2: OK and</li>
 *         <li>O is 0: Operational, 1: Waiting for input, 2: Input stopped, 3: Jam, 4: Opened</li>
 *       </ul>
 *       </li>
 *       <li> 'GetInput': Command to retrieve current input amount in minimum units. Sends back 'GetInputN' where N is
 *       current input in minimum units.</li>
 *       <li> 'OutputXN': Command to output N minimum X-units, where X =C for coins, =B for bills and =A for coins or bills.
 *       Sends back 'OutputM' where M is the minimum units that could be output (If M &lt; N, at least one slot is empty)</li>
 *       <li> 'StartInputX': Command to start cash input. Enables the '+' buttons. X specifies whether coins (1),
 *       bills (2) or both (3) shall be accepted. Sends back 'StartInputN' where N is the current input (normally 0) in
 *       minimum units.</li>
 *       <li> 'StopInput': Command to stop cash input. Disables enabled '+' buttons. Sends back 'StopInputN' where N is
 *       the final input in minimum units.</li>
 *       <li> 'EndInput': Command to finish cash input. Disables enabled '+' buttons. Sends back 'EndInputN' where N is
 *       the final input in minimum units.</li>
 *       <li> 'CancelInput': Command to cancel input. Disables enabled '+' buttons and restores previous slot values.
 *       Sends back 'CancelInputN' where N is the cancelled input (normally 0) in minimum units.</li>
 *       <li> 'GetSlots': Command to retrieve the current slot amounts. Sends back 'GetSlotsL', where L is a list of
 *       value pairs separated by spaces where the first value of each pair specifies the cash value and the second
 *       value the amount of cash units in a slot.</li>
 *       <li> 'AddSlotsK': Command to add cash units to the corresponding slots. K is a list of value pairs separated
 *       by spaces, two values per slot, where the first value of each pair specifies the cash value and the second
 *       value the amount of cash units to be added. Sends back 'AddSlotsL', where L is a list of value pairs separated
 *       by spaces where the first value of each pair specifies the cash value and the second value the amount of cash
 *       units in a slot.</li>
 *     </ul>
 *   </li>
 * </ul>
 * All frames have variable length. Communication will be made with UDP sockets.
 * <br> Multiple commands can be sent in a single frame, separated by comma (,). The corresponding responses will
 * be sent in one frame, separated by comma as well.
 */
@SuppressWarnings("unused")
public class BeltCashboxDrawer extends Device {
    /**
     * Constructor. id specifies the server to be connected in format host:port.
     *
     * @param id IPv4 target address and port
     */
    protected BeltCashboxDrawer(String id) {
        super(id);
        cashDrawerInit(1);
        beltInit(1);
        billAcceptorInit(1);
        billDispenserInit(1);
        cashChangerInit(1);
        coinAcceptorInit(1);
        coinDispenserInit(1);
        for (int i = 0; i < CashSlots[0].length; i++) {
            CashSlotIndex.put(CashSlots[0][i][0], i);
        }
        Toolsets = new CommonSubDeviceToolset[]{
                new DrawerSubDeviceToolset(),
                new BeltSubDeviceToolset(),
                new CashSubDeviceToolset()
        };
    }

    // Drawer specific objects, for CashDrawer implementation.
    private boolean DrawerOpen = false;

    @Override
    public void changeDefaults(CashDrawerProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "CashDrawer service for sample UDP device simulator";
    }

    private class DrawerSubDeviceToolset extends CommonSubDeviceToolset {
        @Override
        public int check(String[] command, String[] response) {
            int rc = super.check(command, response);
            if (0 == rc && command[0].equals("DRAWER")) {
                String[] validcommands = {
                        "GetState",
                        "Open"
                };
                int prefix = checkValidCommand(command[1], response[1], validcommands);
                rc = prefix == 0 ? -2 : ((response[1] = response[1].substring(prefix)).matches("[01]") ? 1 : -1);
            }
            return rc;
        }

        private boolean OldDrawerState;
        private int FirstCommand;

        @Override
        public void saveCurrentStatusInformation(String[][] commands) {
            super.saveCurrentStatusInformation(null);
            OldDrawerState = DrawerOpen;
            prepareSignalStatusWaits(CashDrawers[0]);
            commands[0] = Arrays.copyOf(commands[0], (FirstCommand = commands[0].length) + 1);
            commands[0][FirstCommand] = "DRAWER:GetState";
        }

        @Override
        public void setNewStatusInformation(String[] resps) {
            super.setNewStatusInformation(resps);
            DrawerOpen = resps[FirstCommand].equals("1");
        }

        @Override
        public void statusUpdateProcessing() {
            if (OldDrawerState != DrawerOpen) {
                try {
                    JposCommonProperties props = getPropertySetInstance(CashDrawers, 0, 0);
                    if (props != null) {
                        int state = DrawerOpen ? CASH_SUE_DRAWEROPEN : CASH_SUE_DRAWERCLOSED;
                        handleEvent(new CashDrawerStatusUpdateEvent(props.EventSource, state));
                    }
                } catch (JposException ignored) {}
                signalStatusWaits(CashDrawers[0]);
            }
        }

        @Override
        public boolean statusPowerOnlineProcessing() {
            JposCommonProperties props = getPropertySetInstance(CashDrawers, 0, 0);
            if (props != null) {
                try {
                    handleEvent(new CashDrawerStatusUpdateEvent(props.EventSource, JPOS_SUE_POWER_ONLINE));
                } catch (JposException ignored) {}
            }
            return super.statusPowerOnlineProcessing();
        }

        @Override
        public void statusPowerOfflineProcessing() {
            JposCommonProperties props = getPropertySetInstance(CashDrawers, 0, 0);
            if (props != null) {
                try {
                    handleEvent(new CashDrawerStatusUpdateEvent(props.EventSource, JPOS_SUE_POWER_OFF_OFFLINE));
                } catch (JposException ignored) {}
            }
            signalStatusWaits(CashDrawers[0]);
        }
    }

    private class SampleUdpDeviceCashDrawerAccessor extends CashDrawerProperties {
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
            handleEvent(new JposStatusUpdateEvent(EventSource, Offline ? JPOS_SUE_POWER_OFF_OFFLINE : JPOS_SUE_POWER_ONLINE));
        }

        @Override
        public void deviceEnabled(boolean enable) throws JposException {
            if (enable) {
                if (!Claimed) {
                    startPolling(this);
                    if (Offline && PowerNotify == JPOS_PN_DISABLED) {
                        stopPolling();
                        throw new JposException(JPOS_E_OFFLINE, "Communication with device disrupted");
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
            if (Offline && PowerNotify == JPOS_PN_DISABLED) {
                stopPolling();
                throw new JposException(JPOS_E_OFFLINE, "Communication with device disrupted");
            }
            super.claim(timeout);
        }

        @Override
        public void release() throws JposException {
            super.release();
            stopPolling();
        }

        @Override
        @SuppressWarnings("AssignmentUsedAsCondition")
        public void checkHealth(int level) throws JposException {
            String how = level == JPOS_CH_INTERNAL ? "Internal" : (level == JPOS_CH_EXTERNAL ? "External" : "Interactive");
            if (Offline)
                CheckHealthText = how + " Checkhealth: Offline";
            else {
                CheckHealthText = how + " Checkhealth: OK";
                if (level != JPOS_CH_INTERNAL) {
                    boolean interactive;
                    if (interactive = (level == JPOS_CH_INTERACTIVE))
                        synchronizedMessageBox("Press OK to start health test.", "CheckHealth", INFORMATION_MESSAGE);
                    try {
                        openDrawer();
                        if (!DrawerOpened)
                            CheckHealthText = how + "Checkhealth: Drawer open failed";
                    } catch (JposException e) {
                        CheckHealthText = how + "Checkhealth: Offline";
                    }
                    if (interactive)
                        synchronizedMessageBox("CheckHealth result:\n" + CheckHealthText, "CheckHealth", INFORMATION_MESSAGE);
                }
            }
        }

        @Override
        public void openDrawer() throws JposException {
            attachWaiter();
            String state = sendResp("DRAWER:Open");
            if (!DrawerOpened && !Offline) {
                PollWaiter.signal();
                waitWaiter((long)RequestTimeout * MaxRetry);
            }
            releaseWaiter();
            check(state == null, JPOS_E_OFFLINE, "Communication failure");
            super.openDrawer();
        }

        @Override
        public void waitForDrawerClose() throws JposException {
            attachWaiter();
            while (DrawerOpen && !Offline && DeviceEnabled)
                waitWaiter(INFINITE);
            releaseWaiter();
            check(Offline, JPOS_E_OFFLINE, "Device offline");
            check(!DeviceEnabled, JPOS_E_ILLEGAL, "Device not enabled");
            super.waitForDrawerClose();
        }
    }

    @Override
    public CashDrawerProperties getCashDrawerProperties(int index) {
        return new SampleUdpDeviceCashDrawerAccessor();
    }

    // Belt specific objects, for Belt implementation.
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
    public void changeDefaults(BeltProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "Belt service for sample UDP device simulator";
        props.CapSpeedStepsForward = 2;
    }

    private class BeltSubDeviceToolset extends CommonSubDeviceToolset {
        @Override
        public int check(String[] command, String[] response) {
            int rc = super.check(command, response);
            if (0 == rc) {
                if (command[0].equals("BELT")) {
                    String[] validcommands = {
                            "GetState",
                            "Speed"
                    };
                    int prefix = checkValidCommand(command[1], response[1], validcommands);
                    if (prefix != 0) {
                        char[] state = (response[1] = response[1].substring(prefix)).toCharArray();
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
                    } else
                        rc = -2;
                }
            }
            return rc;
        }

        private char[] OldBeltState;

        @Override
        public void saveCurrentStatusInformation(String[][] commands) {
            super.saveCurrentStatusInformation(null);
            OldBeltState = BeltState;
            int firstCommand = commands[0].length;
            commands[0] = Arrays.copyOf(commands[0], firstCommand + 1);
            commands[0][firstCommand] = "BELT:GetState";
        }

        @Override
        public void setNewStatusInformation(String[] resps) {
            super.setNewStatusInformation(resps);
            BeltState = resps[1].toCharArray();
        }

        @Override
        public void statusUpdateProcessing() {
            if (!Arrays.equals(OldBeltState, BeltState)) {
                try {
                    BeltProperties belt = (BeltProperties)getClaimingInstance(ClaimedBelt, 0);
                    if (belt != null) {
                        if (OldBeltState[BeltLightBarrier] != BeltState[BeltLightBarrier]) {
                            sendLightBarrierEvent(belt);
                        }
                        if (OldBeltState[BeltSecurityFlap] != BeltState[BeltSecurityFlap]) {
                            sendSecurityFlapEvent(belt);
                        }
                        if (OldBeltState[BeltMotor] != BeltState[BeltMotor]) {
                            sendMotorHealthEvent(belt);
                        }
                        else if (OldBeltState[BeltSpeed] != BeltOff && BeltState[BeltSpeed] == BeltOff) {
                            int state = belt.AutoStopForward ? BELT_SUE_AUTO_STOP : BELT_SUE_TIMEOUT_STOP;
                            handleEvent(new BeltStatusUpdateEvent(belt.EventSource, state));
                            if (BeltState[BeltMotor] == BeltMotorOK && BeltState[BeltSecurityFlap] == BeltSecurityFlapClosed &&
                                    BeltState[BeltLightBarrier] == BeltLightBarrierInterrupted)
                                sendResp("BELT:Speed0");
                        }
                    }
                } catch (JposException ignored) {}
            }
        }

        private void sendMotorHealthEvent(BeltProperties belt) throws JposException {
            int state;
            if (BeltState[BeltMotor] != BeltMotorOK) {
                state = BeltState[BeltMotor] == BeltMotorOverheat ?
                        BELT_SUE_MOTOR_OVERHEATING :
                        BELT_SUE_MOTOR_FUSE_DEFECT;
            }
            else {
                state = BeltState[BeltSecurityFlap] == BeltSecurityFlapOpen ?
                        BELT_SUE_EMERGENCY_STOP :
                        BELT_SUE_SAFETY_STOP;
            }
            handleEvent(new BeltStatusUpdateEvent(belt.EventSource, state));
            signalStatusWaits(Belts[0]);
        }

        private void sendSecurityFlapEvent(BeltProperties belt) throws JposException {
            int state = BeltState[BeltSecurityFlap] == BeltSecurityFlapOpen ?
                    BELT_SUE_SECURITY_FLAP_FORWARD_OPENED :
                    BELT_SUE_SECURITY_FLAP_FORWARD_CLOSED;
            handleEvent(new BeltStatusUpdateEvent(belt.EventSource, state));
        }

        private void sendLightBarrierEvent(BeltProperties belt) throws JposException {
            int state = BeltState[BeltLightBarrier] == BeltLightBarrierInterrupted ?
                    BELT_SUE_LIGHT_BARRIER_FORWARD_INTERRUPTED :
                    BELT_SUE_LIGHT_BARRIER_FORWARD_OK;
            handleEvent(new BeltStatusUpdateEvent(belt.EventSource, state));
        }

        @Override
        public boolean statusPowerOnlineProcessing() {
            JposCommonProperties props = getClaimingInstance(ClaimedBelt, 0);
            if (props != null) {
                try {
                    handleEvent(new CashDrawerStatusUpdateEvent(props.EventSource, JPOS_SUE_POWER_ONLINE));
                } catch (JposException ignored) {}
            }
            return super.statusPowerOnlineProcessing();
        }

        @Override
        public void statusPowerOfflineProcessing() {
            JposCommonProperties props = getClaimingInstance(ClaimedBelt, 0);
            if (props != null) {
                try {
                    handleEvent(new CashDrawerStatusUpdateEvent(props.EventSource, JPOS_SUE_POWER_OFF_OFFLINE));
                } catch (JposException ignored) {}
            }
            signalStatusWaits(Belts[0]);
        }
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
                        MotionStatus = BELT_MT_FORWARD;
                    else if (BeltState[BeltMotor] != BeltMotorOK)
                        MotionStatus = BELT_MT_MOTOR_FAULT;
                    else if (BeltState[BeltSecurityFlap] == BeltSecurityFlapOpen)
                        MotionStatus = BELT_MT_EMERGENCY;
                    else
                        MotionStatus = BELT_MT_STOPPED;
                }
            }
        }

        @Override
        public void handlePowerStateOnEnable() throws JposException {
            handleEvent(new JposStatusUpdateEvent(EventSource, Offline ? JPOS_SUE_POWER_OFF_OFFLINE : JPOS_SUE_POWER_ONLINE));
        }

        @Override
        public void claim(int timeout) throws JposException {
            startPolling(this);
            if (Offline && PowerNotify == JPOS_PN_DISABLED) {
                stopPolling();
                throw new JposException(JPOS_E_OFFLINE, "Communication with device disrupted");
            }
            super.claim(timeout);
        }

        @Override
        public void release() throws JposException {
            super.release();
            stopPolling();
        }

        @Override
        @SuppressWarnings("AssignmentUsedAsCondition")
        public void checkHealth(int level) throws JposException {
            String how = level == JPOS_CH_INTERNAL ? "Internal" : (level == JPOS_CH_EXTERNAL ? "External" : "Interactive");
            if (Offline)
                CheckHealthText = how + " Checkhealth: Offline";
            else {
                CheckHealthText = how + " Checkhealth: OK";
                if (level != JPOS_CH_INTERNAL) {
                    boolean interactive;
                    if (interactive = (level == JPOS_CH_INTERACTIVE))
                        synchronizedMessageBox("Press OK to start health test.", "CheckHealth", INFORMATION_MESSAGE);
                    try {
                        moveForward(CapSpeedStepsForward);
                        if (MotionStatus != BELT_MT_FORWARD)
                            CheckHealthText = how + "Checkhealth: Starting Belt failed";
                    } catch (JposException e) {
                        CheckHealthText = how + "Checkhealth: Error: " + e.getMessage();
                    }
                    if (interactive)
                        synchronizedMessageBox("CheckHealth result:\n" + CheckHealthText, "CheckHealth", INFORMATION_MESSAGE);
                }
            }
        }

        @Override
        public void moveForward(int speed) throws JposException {
            check(BeltState[BeltMotor] != BeltMotorOK || BeltState[BeltSecurityFlap] == BeltSecurityFlapOpen, JPOS_E_FAILURE, "Hardware not in operational state");
            check(BeltState[BeltLightBarrier] != BeltLightBarrierFree, JPOS_E_ILLEGAL, "Not allowed in current state");
            String[] cmd = {"BELT:Speed1", "BELT:Speed2"};
            attachWaiter();
            char[] prevState = BeltState;
            sendResp(cmd[speed - 1]);
            if ((prevState[BeltSpeed] == BeltFast) != (speed == CapSpeedStepsForward)) {
                PollWaiter.signal();
                waitWaiter((long)RequestTimeout * MaxRetry);
            }
            releaseWaiter();
            prevState = BeltState;
            check(prevState == null , JPOS_E_OFFLINE, "Communication failure");
            check((prevState[BeltSpeed] == BeltFast) != (speed == CapSpeedStepsForward), JPOS_E_FAILURE, "Speed " + speed + " could not be set");
        }

        @Override
        public void resetBelt() throws JposException {
            check(BeltState[BeltMotor] != BeltMotorOK || BeltState[BeltSecurityFlap] == BeltSecurityFlapOpen, JPOS_E_FAILURE, "Hardware not in operational state");
            attachWaiter();
            char[] prevState = BeltState;
            sendResp("BELT:Speed0");
            if (prevState[BeltSpeed] != BeltOff) {
                PollWaiter.signal();
                waitWaiter((long)RequestTimeout * MaxRetry);
            }
            releaseWaiter();
            prevState = BeltState;
            check(prevState == null , JPOS_E_OFFLINE, "Communication failure");
            check(prevState[BeltSpeed] != BeltOff, JPOS_E_FAILURE, "Belt could not be stopped");
        }

        @Override
        public void stopBelt() throws JposException {
            check(BeltState[BeltMotor] != BeltMotorOK || BeltState[BeltSecurityFlap] == BeltSecurityFlapOpen, JPOS_E_FAILURE, "Hardware not in operational state");
            attachWaiter();
            char[] prevState = BeltState;
            sendResp("BELT:Speed0");
            if (prevState[BeltSpeed] != BeltOff) {
                PollWaiter.signal();
                waitWaiter((long)RequestTimeout * MaxRetry);
            }
            releaseWaiter();
            prevState = BeltState;
            check(prevState == null , JPOS_E_OFFLINE, "Communication failure");
            check(prevState[BeltSpeed] != BeltOff, JPOS_E_FAILURE, "Belt could not be stopped");
        }
    }

    @Override
    public BeltProperties getBeltProperties(int index) {
        return new SampleUdpDeviceBeltAccessor();
    }

    // Cash box specific objects, for implementation of BillAcceptor, BillDispender, CashChanger, CoinAcceptor and
    // CoinDispenser.

    /**
     * Synchronization object for cash deposit operations.
     */
    final int[] CashDepositSync = {0};

    /**
     * Device state for bill slots.
     */
    char[] CashBillState = { CashOK, CashOK, CashOpened };

    /**
     * Device state for coin slots.
     */
    char[] CashCoinState = { CashOK, CashOK, CashOpened };

    /**
     * Index of status character that specifies the empty sensor status (0).
     */
    final static int CashEmptyState = 0;

    /**
     * Status value indicating one slot as empty
     */
    final static char CashEmpty = '0';

    /**
     * Status value indicating one slot as nearly empty.
     */
    final static char CashNearEmpty = '1';

    /**
     * Status value indicating all slots are in good working state.
     */
    final static char CashOK = '2';

    /**
     * Index of status character that specifies the full sensor status (1).
     */
    final static int CashFullState = 1;

    /**
     * Status value indicating one slot as nearly full.
     */
    final static char CashNearlyFull = '1';

    /**
     * Status value indicating one slot as full.
     */
    final static char CashFull = '0';

    /**
     * Index of status character that specifies the operational status (2).
     */
    final static int CashOperationState = 2;

    /**
     * Status character of operational status that specifies cash device as operational ('0').
     */
    final static char CashIdle = '0';

    /**
     * Status value indicating cash input is pending ('1').
     */
    final static char CashInput = '1';

    /**
     * Status value indicating cash input is pending ('1').
     */
    final static char CashFinishInput = '2';

    /**
     * Status value indicating jam in one slot ('3').
     */
    final static char CashJam = '3';

    /**
     * Status value indicating device has been opened ('4')
     */
    final static char CashOpened = '4';

    /**
     * Current cash slot values. Each array element consists of a two-dimensional integer array that
     * contains the value of each cash piece in the slot in minimum cash units in the first element
     * and the count of cash pieces in the second element. The two-dimensional arrays are sorted in
     * ascending order by minimum cash unit element.
     */
    final int[][][] CashSlots = {copySlots(CashInitialSlots)};

    /**
     * Initial cash slot values. Same structure as property CashSlots, but the count for all cash pieces
     * is zero.
     */
    static int [][] CashInitialSlots = {
            {1, 0}, {2, 0}, {5, 0}, {10, 0}, {20, 0}, {50, 0}, {100, 0}, {200, 0},              // Coins
            {500, 0},  {1000, 0}, {2000, 0}, {5000, 0}, {10000, 0}, {20000, 0}, {50000, 0}      // Bills
    };

    /**
     * Index of first element in property CashSlots that contains a value pair for bills. All lower indices
     * contain value pairs for coins and all upper indices for bills.
     */
    final static int CashMinBillIndex = 8;

    /**
     * Copy of property CashSlots that contains the slot counts at the time when a deposit operation has been
     * started. Outside a deposit operation, this property is null.
     */
    int[][] CashDepositStartSlots = null;

    /**
     * Copy of Jpos property RealTimeDataEnabled at the time where a deposit operation has been started. Keep in
     * mind that data events will only be fired when CashCreateDataEvents is true.
     */
    boolean CashCreateDataEvents = false;

    /**
     * Property sets of the currently claimed cash devices. Contains maximum three non-null elements:<ul>
     *     <li>An accepting device property set (for BillAcceptor, CashChanger or CoinAcceptor),</li>
     *     <li>a coin dispensing device property set (for CashChanger or CoinDispenser) and(</li>
     *     <li>a bill dispensing device property set (for CashChanger or BillDispenser).</li>
     * </ul>
     */
    final JposCommonProperties[] CashInstances = {null, null, null};

    /**
     * Index of cash accepting device property set in property CashInstances (0).
     */
    static final int CashAcceptInstance = 0;

    /**
     * Index of coin dispensing device property set in property CashInstances (1).
     */
    static final int CashCoinInstance = 1;

    /**
     * Index of bill dispensing device property set in property CashInstances (2).
     */
    static final int CashBillInstance = 2;

    /**
     * Map that contains the slot index for any cash piece's value in minimum units. As result, for any value x,
     * CashSlotIndex.containsKey(x) is false or CashSlot[CashSlotIndex.get(x)][0] equals x.
     */
    Map<Integer, Integer> CashSlotIndex = new HashMap<>();

    @Override
    public void changeDefaults(BillAcceptorProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "BillAcceptor service for sample UDP device simulator";
        props.CapRealTimeData = true;
        props.CapNearFullSensor = true;
        props.CapFullSensor = true;
        props.CapJamSensor = true;
        props.CurrencyCode = "EUR";
        props.DepositCashList = ";500,1000,2000,5000,10000,20000,50000";
        props.DepositCodeList = "EUR";
        props.DepositCounts = ";500:0,1000:0,2000:0,5000:0,10000:0,20000:0,50000:0";
    }

    @Override
    public void changeDefaults(BillDispenserProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "BillDispenser service for sample UDP device simulator";
        props.CapEmptySensor = true;
        props.CapNearEmptySensor = true;
        props.CapJamSensor = true;
        props.CurrencyCashList = ";500,1000,2000,5000,10000,20000,50000";
        props.CurrencyCodeList = "EUR";
        props.CurrencyCode = "EUR";
        props.ExitCashList = ";500,1000,2000,5000,10000,20000,50000";
    }

    @Override
    public void changeDefaults(CashChangerProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "CashChanger service for sample UDP device simulator";
        props.CapDeposit = true;
        props.CapRealTimeData = true;
        props.CapDepositDataEvent = true;
        props.CapEmptySensor = true;
        props.CapNearEmptySensor = true;
        props.CapNearFullSensor = true;
        props.CapFullSensor = true;
        props.CapJamSensor = true;
        props.CapRepayDeposit = true;
        props.CurrencyCashList = "1,2,5,10,20,50,100,200;500,1000,2000,5000,10000,20000,50000";
        props.CurrencyCodeList = "EUR";
        props.CurrencyCode = "EUR";
        props.DepositCashList = "1,2,5,10,20,50,100,200;500,1000,2000,5000,10000,20000,50000";
        props.DepositCodeList = "EUR";
        props.ExitCashList = "1,2,5,10,20,50,100,200;500,1000,2000,5000,10000,20000,50000";
        props.DeviceExits = 3;
        props.DepositCounts = "1:0,2:0,5:0,10:0,20:0,50:0,100:0,200:0;500:0,1000:0,2000:0,5000:0,10000:0,20000:0,50000:0";
        props.RealTimeDataEnabled = true;
    }

    @Override
    public void changeDefaults(CoinAcceptorProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "CoinAcceptor service for sample UDP device simulator";
        props.CapRealTimeData = true;
        props.CapFullSensor = true;
        props.CapNearFullSensor = true;
        props.CapJamSensor = true;
        props.CurrencyCode = "EUR";
        props.DepositCodeList = "EUR";
        props.DepositCashList = "1,2,5,10,20,50,100,200";
        props.DepositCounts = "1:0,2:0,5:0,10:0,20:0,50:0,100:0,200:0";
        props.RealTimeDataEnabled = true;
    }

    @Override
    public void changeDefaults(CoinDispenserProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "CoinDispenser service for sample UDP device simulator";
        props.CapEmptySensor = true;
        props.CapJamSensor = true;
        props.CapNearEmptySensor = true;
    }

    private class CashSubDeviceToolset extends CommonSubDeviceToolset {
        @Override
        public int check(String[] command, String[] response) {
            int rc = super.check(command, response);
            if (0 == rc && command[0].equals("CASHBOX")) {
                String[] validcommands = {
                        "GetState",
                        "GetInput",
                        "GetSlots",
                        "StartInput",
                        "EndInput",
                        "CancelInput",
                        "StopInput",
                        "AddSlots",
                        "Output",
                };
                int prefix = checkValidCommand(command[1], response[1], validcommands);
                if (prefix != 0) {
                    response[1] = response[1].substring(prefix);
                    String current = command[1].substring(0, prefix);
                    if (current.equals("GetSlots") || current.equals("AddSlots")) {
                        int[][] values = string2Slots(response[1]);
                        if (values.length == CashSlots[0].length) {
                            for (int i = 0; i < values.length; i++) {
                                if (values[i][0] != CashSlots[0][i][0])
                                    return -1;
                            }
                            return 1;
                        }
                        return -1;
                    }
                    int[][] values = string2Slots(response[1] + " 0");
                    if (values.length != 1)
                        return -1;
                    if (values[0][0] < 0)
                        return -1;
                    return current.equals("GetState") && !response[1].matches("[012][012][01234]") ? -1 : 1;
                } else
                    rc = -2;
            }
            return rc;
        }

        private int FirstCommand;
        private char[]OldBillState;
        private char[]OldCoinState;
        private int[][]OldSlots;

        @Override
        public void saveCurrentStatusInformation(String[][] commands) {
            super.saveCurrentStatusInformation(null);
            commands[0] = Arrays.copyOf(commands[0], (FirstCommand = commands[0].length) + 2);
            commands[0][FirstCommand] = "CASHBOX:GetState";
            commands[0][FirstCommand + 1] = "CASHBOX:GetSlots";
            OldBillState = CashBillState;
            OldCoinState = CashCoinState;
            OldSlots = CashSlots[0];
            prepareSignalStatusWaits(BillAcceptors[0]);
            prepareSignalStatusWaits(BillDispensers[0]);
            prepareSignalStatusWaits(CashChangers[0]);
            prepareSignalStatusWaits(CoinAcceptors[0]);
            prepareSignalStatusWaits(CoinDispensers[0]);
        }

        @Override
        public void setNewStatusInformation(String[] resps) {
            super.setNewStatusInformation(resps);
            char[]cashState = resps[FirstCommand].toCharArray();
            synchronized (CashSlots) {
                CashSlots[0] = string2Slots(resps[FirstCommand + 1]);
                CashCoinState = Arrays.copyOf(cashState, cashState.length);
                CashBillState = cashState;
                setCashState(CashCoinState, cashState, Arrays.copyOf(CashSlots[0], CashMinBillIndex));
                setCashState(CashBillState, cashState, Arrays.copyOfRange(CashSlots[0], CashMinBillIndex, CashSlots[0].length));
            }
        }

        private void setCashState(char[] state, char[] cashState, int[][] slots) {
            if (cashState[CashEmptyState] != CashOK || cashState[CashFullState] != CashOK) {
                state[CashFullState] = state[CashEmptyState] = CashOK;
                for (int[] slot : slots) {
                    if (slot[1] <= 2 && state[CashEmptyState] != CashEmpty) {
                        state[CashEmptyState] = slot[1] == 0 ? CashEmpty : CashNearEmpty;
                    }
                    if (slot[1] >= 98 && state[CashFullState] != CashFull) {
                        state[CashFullState] = slot[1] == 100 ? CashFull : CashNearlyFull;
                    }
                }
            }
        }

        @Override
        public void statusUpdateProcessing() {
            if (!Arrays.equals(getCashStates(OldBillState, OldCoinState), getCashStates(CashBillState, CashCoinState))
                    || slotsDifferent(OldSlots, CashSlots[0])
            ) {
                statusCashProcessing(OldBillState, OldCoinState, OldSlots);
            }
            signalStatusWaiters();
        }

        private void signalStatusWaiters() {
            signalStatusWaits(BillAcceptors[0]);
            signalStatusWaits(BillDispensers[0]);
            signalStatusWaits(CashChangers[0]);
            signalStatusWaits(CoinAcceptors[0]);
            signalStatusWaits(CoinDispensers[0]);
        }

        private void statusCashProcessing(char[] oldBillState, char[] oldCoinState, int[][] oldSlots) {
            sendCashFullEvents(oldBillState, oldCoinState);
            sendCashEmptyEvents(oldBillState, oldCoinState);
            if ((oldBillState[CashOperationState] > CashFinishInput) != (CashBillState[CashOperationState] > CashFinishInput)) {
                sendCashJamEvent(CashBillState[CashOperationState] > CashFinishInput);
            }
            if (slotsDifferent(oldSlots, CashSlots[0])) {
                sendDataEvents();
            }
        }

        private void sendDataEvents() {
            int[][] slots;
            synchronized (CashSlots) {
                if (!CashCreateDataEvents)
                    return;
                slots = depositDelta();
            }
            String coincounts = "";
            String billcounts = "";
            int coinamount = 0;
            int billamount = 0;
            Object[] deposit = getCountsAmount(slots, 0, CashMinBillIndex);
            coinamount = (Integer) deposit[0];
            coincounts = (String) deposit[1];
            deposit = getCountsAmount(slots, CashMinBillIndex, slots.length);
            billamount = (Integer) deposit[0];
            billcounts = (String) deposit[1];
            JposCommonProperties props;
            synchronized (CashInstances) {
                props = CashInstances[CashAcceptInstance];
            }
            synchronized (CashDepositSync) {
                if (!CashCreateDataEvents)
                    return;
            }
            try {
                if (props instanceof CoinAcceptorProperties) {
                    handleEvent(new CoinAcceptorDataEvent(props.EventSource, 0, coinamount, coincounts));
                } else if (props instanceof BillAcceptorProperties) {
                    handleEvent(new BillAcceptorDataEvent(props.EventSource, 0, billamount, billcounts));
                } else if (props instanceof SampleUdpDeviceCashChangerProperties) {
                    handleEvent(new CashChangerDataEvent(props.EventSource, 0, coinamount + billamount, coincounts + billcounts));
                }
            } catch (JposException ignored) {}
        }

        private void sendCashJamEvent(boolean b) {
            try {
                int[][] states = {
                        {BACC_STATUS_JAM, BACC_STATUS_JAMOK},
                        {BDSP_STATUS_JAM, BDSP_STATUS_JAMOK},
                        {CHAN_STATUS_JAM, CHAN_STATUS_JAMOK},
                        {CACC_STATUS_JAM, CACC_STATUS_JAMOK},
                        {COIN_STATUS_JAM, COIN_STATUS_OK}
                };
                int i = b ? 0 : 1;
                int j = CashBillState[CashEmptyState] - '0';
                JposCommonProperties props = getClaimingInstance(ClaimedBillAcceptor, 0);
                if (props != null) {
                    handleEvent(new SampleUdpDeviceBillAcceptorStatusUpdateEvent(props.EventSource, states[0][i]));
                }
                props = getClaimingInstance(ClaimedBillDispenser, 0);
                if (props != null) {
                    int[] devstate = {BDSP_STATUS_EMPTY, BDSP_STATUS_NEAREMPTY, BDSP_STATUS_OK};
                    if (b)
                        devstate[j] = BDSP_STATUS_JAM;
                    handleEvent(new SampleUdpDeviceBillDispenserStatusUpdateEvent(props.EventSource, states[0][i], devstate[j]));
                }
                props = getClaimingInstance(ClaimedCashChanger, 0);
                if (props != null) {
                    int[] devstate = {CHAN_STATUS_EMPTY, CHAN_STATUS_NEAREMPTY, CHAN_STATUS_OK};
                    if (((SampleUdpDeviceCashChangerProperties) props).DepositStatus != CHAN_STATUS_DEPOSIT_END) {
                        devstate[j] = CHAN_STATUS_DEPOSIT_END;
                    }
                    handleEvent(new SampleUdpDeviceCashChangerStatusUpdateEvent(props.EventSource, states[0][i], devstate[j]));
                }
                props = getClaimingInstance(ClaimedCoinAcceptor, 0);
                if (props != null) {
                    handleEvent(new SampleUdpDeviceCoinAcceptorStatusUpdateEvent(props.EventSource, states[0][i]));
                }
                props = getClaimingInstance(ClaimedCoinDispenser, 0);
                if (props != null) {
                    int[] devstate = {COIN_STATUS_EMPTY, COIN_STATUS_NEAREMPTY, COIN_STATUS_OK};
                    if (b)
                        devstate[j] = COIN_STATUS_JAM;
                    handleEvent(new SampleUdpDeviceCoinDispenserStatusUpdateEvent(props.EventSource, devstate[j]));
                }
            } catch (JposException ignored) {}
        }

        private void sendCashEmptyEvents(char[] oldBillState, char[] oldCoinState) {
            try {
                int[][] states = {
                        {BDSP_STATUS_EMPTY, BDSP_STATUS_NEAREMPTY, BDSP_STATUS_EMPTYOK},
                        {CHAN_STATUS_EMPTY, CHAN_STATUS_NEAREMPTY, CHAN_STATUS_EMPTYOK},
                        {COIN_STATUS_EMPTY, COIN_STATUS_NEAREMPTY, COIN_STATUS_OK}
                };
                char[] cashstate = getCashStates(CashBillState, CashCoinState);
                JposCommonProperties props = getClaimingInstance(ClaimedBillDispenser, 0);
                if (props != null && CashBillState[CashEmptyState] != oldBillState[CashEmptyState]) {
                    int i = CashBillState[CashEmptyState] - '0';
                    int[] devstate = {BDSP_STATUS_EMPTY, BDSP_STATUS_NEAREMPTY, BDSP_STATUS_OK};
                    if (CashBillState[CashOperationState] > CashInput)
                        devstate[i] = BDSP_STATUS_JAM;
                    handleEvent(new SampleUdpDeviceBillDispenserStatusUpdateEvent(props.EventSource, states[0][i], devstate[i]));
                }
                props = getClaimingInstance(ClaimedCashChanger, 0);
                if (props != null && cashstate[CashEmptyState] != getCashStates(oldBillState, oldCoinState)[CashEmptyState]) {
                    int i = cashstate[CashEmptyState] - '0';
                    int[] devstate = {CHAN_STATUS_EMPTY, CHAN_STATUS_NEAREMPTY, CHAN_STATUS_OK};
                    if (((SampleUdpDeviceCashChangerProperties)props).DepositStatus == CHAN_STATUS_DEPOSIT_END &&
                            CashBillState[CashOperationState] > CashInput)
                        devstate[i] = CHAN_STATUS_JAM;
                    handleEvent(new SampleUdpDeviceCashChangerStatusUpdateEvent(props.EventSource, states[0][i], devstate[i]));
                }
                props = getClaimingInstance(ClaimedCoinDispenser, 0);
                if (props != null && CashCoinState[CashEmptyState] != oldCoinState[CashEmptyState]) {
                    int i = CashCoinState[CashEmptyState] - '0';
                    int[] devstate = {COIN_STATUS_EMPTY, COIN_STATUS_NEAREMPTY, COIN_STATUS_OK};
                    if (CashCoinState[CashOperationState] > CashInput)
                        devstate[i] = COIN_STATUS_JAM;
                    handleEvent(new SampleUdpDeviceCoinDispenserStatusUpdateEvent(props.EventSource, devstate[i]));
                }
            } catch (JposException ignored) {}
        }

        private void sendCashFullEvents(char[] oldBillState, char[] oldCoinState) {
            try {
                int[][] states = {
                        {BACC_STATUS_FULL, BACC_STATUS_NEARFULL, BACC_STATUS_FULLOK},
                        {CHAN_STATUS_FULL, CHAN_STATUS_NEARFULL, CHAN_STATUS_FULLOK},
                        {CACC_STATUS_FULL, CACC_STATUS_NEARFULL, CACC_STATUS_FULLOK}
                };
                JposCommonProperties props = getClaimingInstance(ClaimedBillAcceptor, 0);
                char[] cashstate = getCashStates(CashBillState, CashCoinState);
                if (props != null && CashBillState[CashFullState] != oldBillState[CashFullState]) {
                    int i = CashBillState[CashFullState] - '0';
                    handleEvent(new SampleUdpDeviceBillAcceptorStatusUpdateEvent(props.EventSource, states[0][i]));
                }
                props = getClaimingInstance(ClaimedCashChanger, 0);
                if (props != null && cashstate[CashFullState] != getCashStates(oldBillState, oldCoinState)[CashFullState]) {
                    int i = cashstate[CashFullState] - '0';
                    handleEvent(new SampleUdpDeviceCashChangerStatusUpdateEvent(props.EventSource, states[1][i]));
                }
                props = getClaimingInstance(ClaimedCoinAcceptor, 0);
                if (props != null && CashCoinState[CashFullState] != oldCoinState[CashFullState]) {
                    int i = CashCoinState[CashFullState] - '0';
                    handleEvent(new SampleUdpDeviceCoinAcceptorStatusUpdateEvent(props.EventSource, states[2][i]));
                }
            } catch (JposException ignored) {}
        }

        @Override
        public boolean statusPowerOnlineProcessing() {
            try {
                JposCommonProperties props = getClaimingInstance(ClaimedBillAcceptor, 0);
                if ((props) != null)
                    handleEvent(new SampleUdpDeviceBillAcceptorStatusUpdateEvent(props.EventSource, JPOS_SUE_POWER_ONLINE));
                if ((props = getClaimingInstance(ClaimedBillDispenser, 0)) != null)
                    handleEvent(new SampleUdpDeviceBillDispenserStatusUpdateEvent(props.EventSource, JPOS_SUE_POWER_ONLINE));
                if ((props = getClaimingInstance(ClaimedCashChanger, 0)) != null)
                    handleEvent(new SampleUdpDeviceCashChangerStatusUpdateEvent(props.EventSource, JPOS_SUE_POWER_ONLINE));
                if ((props = getClaimingInstance(ClaimedCoinDispenser, 0)) != null)
                    handleEvent(new SampleUdpDeviceCoinDispenserStatusUpdateEvent(props.EventSource, JPOS_SUE_POWER_ONLINE));
                if ((props = getClaimingInstance(ClaimedCoinAcceptor, 0)) != null)
                    handleEvent(new SampleUdpDeviceCoinAcceptorStatusUpdateEvent(props.EventSource, JPOS_SUE_POWER_ONLINE));
            } catch (JposException ignored) {}
            return super.statusPowerOnlineProcessing();
        }

        @Override
        public void statusPowerOfflineProcessing() {
            try {
                JposCommonProperties props = getClaimingInstance(ClaimedBillAcceptor, 0);
                if ((props) != null)
                    handleEvent(new SampleUdpDeviceBillAcceptorStatusUpdateEvent(props.EventSource, JPOS_SUE_POWER_OFF_OFFLINE));
                if ((props = getClaimingInstance(ClaimedBillDispenser, 0)) != null)
                    handleEvent(new SampleUdpDeviceBillDispenserStatusUpdateEvent(props.EventSource, JPOS_SUE_POWER_OFF_OFFLINE));
                if ((props = getClaimingInstance(ClaimedCashChanger, 0)) != null)
                    handleEvent(new SampleUdpDeviceCashChangerStatusUpdateEvent(props.EventSource, JPOS_SUE_POWER_OFF_OFFLINE));
                if ((props = getClaimingInstance(ClaimedCoinDispenser, 0)) != null)
                    handleEvent(new SampleUdpDeviceCoinDispenserStatusUpdateEvent(props.EventSource, JPOS_SUE_POWER_OFF_OFFLINE));
                if ((props = getClaimingInstance(ClaimedCoinAcceptor, 0)) != null)
                    handleEvent(new SampleUdpDeviceCoinAcceptorStatusUpdateEvent(props.EventSource, JPOS_SUE_POWER_OFF_OFFLINE));
            } catch (JposException ignored) {}
            signalStatusWaiters();
        }
    }

    /**
     * Creates a copy of a two-dimensional array of int.
     * @param slots Two-dimensional array to be copied.
     * @return      A copy of the given array, where each element of the array is a copy of the corresponding
     *              element in the source array.
     */
    int[][] copySlots(int[][] slots) {
        int[][] result = new int[slots.length][];
        for (int i = slots.length - 1; i >= 0; --i) {
            result[i] = Arrays.copyOf(slots[i], slots[i].length);
        }
        return result;
    }

    private boolean slotsDifferent(int[][] slot1, int[][] slot2) {
        if (slot1 == null || slot2 == null)
            return slot1 != slot2;
        if (slot1.length != slot2.length)
            return true;
        for (int i = slot1.length - 1; i >= 0; --i) {
            if (!Arrays.equals(slot1[i], slot2[i]))
                return true;
        }
        return false;
    }

    /**
     * Creates a two-dimensional int array from the return parameters of specific device commands. Can be used
     * to create new values for the properties CashSlots and CashDepositStartSlots.
     * @param slotstr String that contains an even number of space separated integer numbers.
     * @return        An int[][] representation of the given String.
     */
    int[][] string2Slots(String slotstr) {
        String[] slotparts = slotstr.split(" ");
        int[][] slots = new int[slotparts.length / 2][];
        int i = 0;
        try {
            while (i < slots.length) {
                slots[i] = new int[2];
                slots[i][0] = Integer.parseInt(slotparts[i + i]);
                slots[i][1] = Integer.parseInt(slotparts[i + i + 1]);
                ++i;
            }
        } catch (Exception e) {
            if (i > 0)
                slots = Arrays.copyOf(slots, i);
        }
        return slots;
    }

    /**
     * Computes an object with the same structure as the CashSlots property that contains count elements which are
     * computed as the difference between the corresponding counts of the properties CashSlots and CashDepositStartSlots.
     * This object can be used to compute the amount that has been payed-in, in cash pieces or as value.
     * @return The computed value or null, if CashDepositStartSlots is null. This is the case outside of deposit operations.
     */
    int[][] depositDelta() {
        int[][] result = null;
        if (CashDepositStartSlots != null) {
            result = copySlots(CashSlots[0]);
            for (int i = 0; i < result.length; i++) {
                result[i][1] -= CashDepositStartSlots[i][1];
            }
        }
        return result;
    }

    /**
     * Converts a cashCounts string into an int[][]. Throws a JposException if a component of the string is invalid.
     * @param cashCounts String to be converted. Format: Array of Pairs of int values separated by ":", separated by
     *                   comma (",") or maximum one semicolon (";").
     * @param what       specifies which values are valid. 0: all pairs ";", 1: Only pairs for coins, 2: only pairs for bills.
     * @return           cashCounts representation in the same format as CashSlots.
     * @throws JposException  If any pair of int contains a first component that is not a valid coin or bill value.
     */
    int[][] cashCounts2ints(String cashCounts, int what) throws JposException {
        String[] cashtypestr = Arrays.copyOf((cashCounts + ";").split(";", -1), 2);
        int[][] slots = copySlots(CashInitialSlots);
        int[][] boundaries = {{0, CashMinBillIndex}, {CashMinBillIndex, slots.length}};
        int[][] validIndices = {{0, 1}, {0}, {1}};
        if (validIndices[what].length == 1)
            check(cashtypestr[1 - validIndices[what][0]].length() > 0, JPOS_E_ILLEGAL, cashCounts + " contains invalid cashCount");
        for (int type : validIndices[what]) {
            if (type < cashtypestr.length && cashtypestr[type].length() > 0) {
                String[] countsstr = cashtypestr[type].split(",");
                for (String s : countsstr) {
                    String[] parts = s.split(":");
                    int value = Integer.parseInt(parts[0]);
                    check(!CashSlotIndex.containsKey(value), JPOS_E_ILLEGAL, "Invalid cashCount component: " + s);
                    value = CashSlotIndex.get(value);
                    check(value < boundaries[type][0] || boundaries[type][1] <= value, JPOS_E_ILLEGAL,
                            "Invalid cashCount type component: " + s);
                    slots[value][1] += Integer.parseInt(parts[1]);
                }
            }
        }
        return slots;
    }

    /**
     * Computes a common cash status from properties CashCoinState and CashBillState.
     * @return Combined common status for coins and bills.
     */
    char[] getCashStates() {
        return getCashStates(CashCoinState, CashBillState);
    }

    private char[] getCashStates(char[] state1, char[] state2) {
        char[] result = Arrays.copyOf(state1, state1.length);
        if (result[CashEmptyState] > state2[CashEmptyState])
            result[CashEmptyState] = state2[CashEmptyState];
        if (result[CashFullState] > state2[CashFullState])
            result[CashFullState] = state2[CashFullState];
        return result;
    }

    /**
     * Cancel a pending cash input operation.
     * @param state Cash operation status of input operation, must be CashInput to send cancel command.
     * @throws JposException If cancel operation could not be cancelled or in case of a communication error.
     */
    void cancelCashInput(char state) throws JposException {
        if (!Offline && (state == CashInput || state == CashFinishInput)) {
            JposCommonProperties props;
            synchronized(CashInstances) {
                props = CashInstances[CashAcceptInstance];
            }
            props.attachWaiter();
            sendResp("CASHBOX:CancelInput");
            PollWaiter.signal();
            props.waitWaiter((long)RequestTimeout * MaxRetry);
            props.releaseWaiter();
            synchronized(CashSlots) {
                state = CashBillState[CashOperationState];
                check(Offline || (state == CashInput || state == CashFinishInput), JPOS_E_FAILURE, "Unable to cancel deposit operation");
            }
        }
    }

    /**
     * Computes UPOS conforming cash counts string and an amount value in minimum units from CashSlots or an object
     * with a CashSlots compatible format.
     * @param slots CashSlots or an int[][] array with only different element counts.
     * @param from  First index for computation, usually 0 or CashMinBillIndex.
     * @param to    Upper border for computation, usually CashMinBillIndex or CashSlots.length.
     * @return      Array of two Object elements, where the first object is an Integer containing the amount value and
     *              the second object is a String in a format specified for cashCounts method parameters in the UPOS
     *              specification.
     */
    Object[] getCountsAmount(int[][] slots, int from, int to) {
        int amount = 0;
        StringBuilder counts = new StringBuilder();
        for (int i = from; i < to; i++) {
            amount += slots[i][0] * slots[i][1];
            counts.append(",").append(slots[i][0]).append(":").append(slots[i][1]);
        }
        return new Object[]{ amount, (from == 0 ? "" : ";") + counts.substring(1) };
    }

    private class SampleUdpDeviceBillAcceptorStatusUpdateEvent extends BillAcceptorStatusUpdateEvent {
        public SampleUdpDeviceBillAcceptorStatusUpdateEvent(JposBase source, int state) {
            super(source, state);
        }

        @Override
        public boolean setStatusProperties() {
            synchronized(CashDepositSync) {
                return super.setStatusProperties();
            }
        }
    }

    private class SampleUdpDeviceBillAcceptorProcessor extends BillAcceptorProperties {
        protected SampleUdpDeviceBillAcceptorProcessor() {
            super(0);
        }

        @Override
        public void handlePowerStateOnEnable() throws JposException {
            handleEvent(new JposStatusUpdateEvent(EventSource, Offline ? JPOS_SUE_POWER_OFF_OFFLINE : JPOS_SUE_POWER_ONLINE));
        }

        @Override
        public void initOnEnable(boolean enable) {
            if (enable) {
                synchronized (CashSlots) {
                    char[] state = getCashStates();
                    char opstate = state[CashOperationState];
                    char histate = state[CashFullState];
                    if (Offline || opstate == CashJam || opstate == CashOpened) {
                        DepositStatusDef = BACC_STATUS_DEPOSIT_JAM;
                        FullStatusDef = BACC_STATUS_FULL;
                    } else {
                        FullStatusDef = histate == CashFull ? BACC_STATUS_FULL
                                : (histate == CashNearlyFull ? BACC_STATUS_NEARFULL : BACC_STATUS_OK);
                        DepositStatusDef = opstate == CashIdle ? BACC_STATUS_DEPOSIT_END
                                : (opstate == CashInput ? BACC_STATUS_DEPOSIT_START : BACC_STATUS_DEPOSIT_COUNT);
                    }
                }
            }
            super.initOnEnable(enable);
        }

        @Override
        public void claim(int timeout) throws JposException {
            synchronized (CashInstances) {
                check(CashInstances[CashAcceptInstance] != null && !(CashInstances[CashAcceptInstance] instanceof BillAcceptorProperties),
                        JPOS_E_CLAIMED, "Device claimed by other cash accepting instance");
                CashInstances[CashAcceptInstance] = this;
            }
            startPolling(this);
            if (Offline && PowerNotify == JPOS_PN_DISABLED) {
                stopPolling();
                synchronized(CashInstances) {
                    CashInstances[CashAcceptInstance] = null;
                }
                throw new JposException(JPOS_E_OFFLINE, "Communication with device disrupted");
            }
            super.claim(timeout);
        }

        @Override
        public void release() throws JposException {
            super.release();
            stopPolling();
            synchronized(CashInstances) {
                CashInstances[CashAcceptInstance] = null;
            }
        }

        @Override
        @SuppressWarnings("AssignmentUsedAsCondition")
        public void checkHealth(int level) throws JposException {
            String how = level == JPOS_CH_INTERNAL ? "Internal" : (level == JPOS_CH_EXTERNAL ? "External" : "Interactive");
            if (Offline)
                CheckHealthText = how + " Checkhealth: Offline";
            else {
                CheckHealthText = how + " Checkhealth: OK";
                if (level != JPOS_CH_INTERNAL) {
                    boolean interactive;
                    if (interactive = (level == JPOS_CH_INTERACTIVE))
                        synchronizedMessageBox("Press OK to start health test.", "CheckHealth", INFORMATION_MESSAGE);
                    try {
                        String[] counts = {""};
                        boolean[] diff = {false};
                        readCashCounts(counts, diff);
                    } catch (JposException e) {
                        CheckHealthText = how + "Checkhealth: Error: " + e.getMessage();
                    }
                    if (interactive)
                        synchronizedMessageBox("CheckHealth result:\n" + CheckHealthText, "CheckHealth", INFORMATION_MESSAGE);
                }
            }
        }

        @Override
        public void adjustCashCounts(String cashCounts) throws JposException {
            synchronized (CashSlots) {
                check(CashDepositStartSlots != null, JPOS_E_ILLEGAL, "Cash acceptance in progress");
            }
            int[][] slots = cashCounts2ints(cashCounts, 2);
            boolean doit = false;
            synchronized (CashSlots) {
                for (int i = CashMinBillIndex; i < slots.length; i++) {
                    if ((slots[i][1] -= CashSlots[0][i][1]) != 0)
                        doit = true;
                }
            }
            if (doit) {
                StringBuilder list = new StringBuilder();
                for (int i = CashMinBillIndex; i < slots.length; i++) {
                    if (slots[i][1] != 0)
                        list.append(" ").append(slots[i][0]).append(" ").append(slots[i][1]);
                }
                String result = sendResp("CASHBOX:AddSlots" + list.substring(1));
                check(result == null || Offline, JPOS_E_FAILURE, "Communication error");
            }
        }

        @Override
        public void beginDeposit() throws JposException {
            check(DepositStatus != BACC_STATUS_DEPOSIT_END, JPOS_E_ILLEGAL, "Bad deposit state");
            attachWaiter();
            String[] result = sendResp(new String[]{"CASHBOX:StartInput2", "CASHBOX:GetSlots"});
            Object[] deposit;
            if (result != null) {
                PollWaiter.signal();
                waitWaiter((long)RequestTimeout * MaxRetry);
            }
            releaseWaiter();
            check(result == null, JPOS_E_OFFLINE, "Could not start deposit operation");
            synchronized (CashSlots) {
                CashDepositStartSlots = string2Slots(result[1]);
                deposit = getCountsAmount(depositDelta(), CashMinBillIndex, CashSlots[0].length);
            }
            synchronized (CashDepositSync) {
                DepositAmount = (Integer) deposit[0];
                DepositCounts = (String) deposit[1];
                super.beginDeposit();
                CashCreateDataEvents = RealTimeDataEnabled;
            }
        }

        @Override
        public void fixDeposit() throws JposException {
            attachWaiter();
            String depositstr = sendResp("CASHBOX:StopInput");
            check(depositstr == null, JPOS_E_FAILURE, "Cannot stop cash input");
            PollWaiter.signal();
            waitWaiter((long)RequestTimeout * MaxRetry);
            releaseWaiter();
            Object[] deposit;
            synchronized(CashSlots) {
                deposit = getCountsAmount(depositDelta(), CashMinBillIndex, CashSlots[0].length);
                check(Integer.parseInt(depositstr) != (Integer)deposit[0], JPOS_E_FAILURE, "Deposit amount mismatch");
            }
            synchronized (CashDepositSync) {
                CashCreateDataEvents = false;
                DepositAmount = (Integer)deposit[0];
                DepositCounts = (String) deposit[1];
                super.fixDeposit();
            }
        }

        @Override
        public void clearInput() throws JposException {
            cancelCashInput(CashBillState[CashOperationState]);
            super.clearInput();
            Object[] deposit;
            synchronized(CashSlots) {
                int[][] delta = depositDelta();
                if (delta == null)
                    delta = CashInitialSlots;
                deposit = getCountsAmount(delta, CashMinBillIndex, CashSlots[0].length);
                CashDepositStartSlots = null;
            }
            synchronized (CashDepositSync) {
                DepositAmount = (Integer)deposit[0];
                DepositCounts = (String) deposit[1];
                if (DepositStatus != BACC_STATUS_DEPOSIT_JAM)
                    DepositStatus = BACC_STATUS_DEPOSIT_END;
            }
        }

        @Override
        public void endDeposit(int success) throws JposException {
            String depositstr = sendResp("CASHBOX:EndInput");
            synchronized (CashDepositSync) {
                check(depositstr == null, JPOS_E_FAILURE, "Deposit end failure");
            }
            check(Integer.parseInt(depositstr) != DepositAmount, JPOS_E_FAILURE, "Deposit amount mismatch");
            synchronized (CashSlots) {
                CashDepositStartSlots = null;
            }
            synchronized (CashDepositSync) {
                if (DepositStatus != BACC_STATUS_DEPOSIT_JAM)
                    super.endDeposit(success);
            }
        }

        @Override
        public void readCashCounts(String[] cashCounts, boolean[] discrepancy) throws JposException {
            check(Offline, JPOS_E_OFFLINE, "Device is offline");
            attachWaiter();
            PollWaiter.signal();
            waitWaiter((long)RequestTimeout * MaxRetry);
            releaseWaiter();
            check(Offline, JPOS_E_OFFLINE, "Device is offline");
            cashCounts[0] = "";
            int[][] slots;
            synchronized (CashSlots) {
                slots = CashDepositStartSlots == null ? CashSlots[0] : CashDepositStartSlots;
                cashCounts[0] = (String) (getCountsAmount(slots, CashMinBillIndex, slots.length)[1]);
            }
            discrepancy[0] = false;
        }
    }

    @Override
    public BillAcceptorProperties getBillAcceptorProperties(int index) {
        return new SampleUdpDeviceBillAcceptorProcessor();
    }

    private class SampleUdpDeviceCoinAcceptorStatusUpdateEvent extends CoinAcceptorStatusUpdateEvent {
        public SampleUdpDeviceCoinAcceptorStatusUpdateEvent(JposBase source, int state) {
            super(source, state);
        }

        @Override
        public boolean setStatusProperties() {
            synchronized(CashDepositSync) {
                return super.setStatusProperties();
            }
        }
    }

    private class SampleUdpDeviceCoinAcceptorProcessor extends CoinAcceptorProperties {
        protected SampleUdpDeviceCoinAcceptorProcessor() {
            super(0);
        }

        @Override
        public void handlePowerStateOnEnable() throws JposException {
            handleEvent(new JposStatusUpdateEvent(EventSource, Offline ? JPOS_SUE_POWER_OFF_OFFLINE : JPOS_SUE_POWER_ONLINE));
        }

        @Override
        public void initOnEnable(boolean enable) {
            if (enable) {
                synchronized (CashSlots) {
                    char[] state = getCashStates();
                    char opstate = state[CashOperationState];
                    char histate = state[CashFullState];
                    if (Offline || opstate == CashJam || opstate == CashOpened) {
                        DepositStatusDef = CACC_STATUS_DEPOSIT_JAM;
                        FullStatusDef = CACC_STATUS_FULL;
                    } else {
                        FullStatusDef = histate == CashFull ? CACC_STATUS_FULL
                                : (histate == CashNearlyFull ? CACC_STATUS_NEARFULL : CACC_STATUS_OK);
                        DepositStatusDef = opstate == CashIdle ? CACC_STATUS_DEPOSIT_END
                                : (opstate == CashInput ? CACC_STATUS_DEPOSIT_START : CACC_STATUS_DEPOSIT_COUNT);
                    }
                }
            }
            super.initOnEnable(enable);
        }

        @Override
        public void claim(int timeout) throws JposException {
            synchronized (CashInstances) {
                check(CashInstances[CashAcceptInstance] != null && !(CashInstances[CashAcceptInstance] instanceof CoinAcceptorProperties),
                        JPOS_E_CLAIMED, "Device claimed by other cash accepting instance");
                CashInstances[CashAcceptInstance] = this;
            }
            startPolling(this);
            if (Offline && PowerNotify == JPOS_PN_DISABLED) {
                stopPolling();
                synchronized (CashInstances) {
                    CashInstances[CashAcceptInstance] = null;
                }
                throw new JposException(JPOS_E_OFFLINE, "Communication with device disrupted");
            }
            super.claim(timeout);
        }

        @Override
        public void release() throws JposException {
            super.release();
            stopPolling();
            synchronized (CashInstances) {
                CashInstances[CashAcceptInstance] = null;
            }
        }

        @Override
        @SuppressWarnings("AssignmentUsedAsCondition")
        public void checkHealth(int level) throws JposException {
            String how = level == JPOS_CH_INTERNAL ? "Internal" : (level == JPOS_CH_EXTERNAL ? "External" : "Interactive");
            if (Offline)
                CheckHealthText = how + " Checkhealth: Offline";
            else {
                CheckHealthText = how + " Checkhealth: OK";
                if (level != JPOS_CH_INTERNAL) {
                    boolean interactive;
                    if (interactive = (level == JPOS_CH_INTERACTIVE))
                        synchronizedMessageBox("Press OK to start health test.", "CheckHealth", INFORMATION_MESSAGE);
                    try {
                        String[] counts = {""};
                        boolean[] diff = {false};
                        readCashCounts(counts, diff);
                    } catch (JposException e) {
                        CheckHealthText = how + "Checkhealth: Error: " + e.getMessage();
                    }
                    if (interactive)
                        synchronizedMessageBox("CheckHealth result:\n" + CheckHealthText, "CheckHealth", INFORMATION_MESSAGE);
                }
            }
        }

        @Override
        public void adjustCashCounts(String cashCounts) throws JposException {
            synchronized (CashSlots) {
                check(CashDepositStartSlots != null, JPOS_E_ILLEGAL, "Cash acceptance in progress");
            }
            int[][] slots = cashCounts2ints(cashCounts, 1);
            boolean doit = false;
            synchronized (CashSlots) {
                for (int i = 0; i < CashMinBillIndex; i++) {
                    if ((slots[i][1] -= CashSlots[0][i][1]) != 0)
                        doit = true;
                }
            }
            if (doit) {
                StringBuilder list = new StringBuilder();
                for (int i = 0; i < CashMinBillIndex; i++) {
                    if (slots[i][1] != 0)
                        list.append(" ").append(slots[i][0]).append(" ").append(slots[i][1]);
                }
                String result = sendResp("CASHBOX:AddSlots" + list.substring(1));
                check(result == null || Offline, JPOS_E_FAILURE, "Communication error");
            }
        }

        @Override
        public void beginDeposit() throws JposException {
            check(DepositStatus != CACC_STATUS_DEPOSIT_END, JPOS_E_ILLEGAL, "Bad deposit state");
            attachWaiter();
            String[] result = sendResp(new String[]{"CASHBOX:StartInput1", "CASHBOX:GetSlots"});
            Object[] deposit;
            if (result != null) {
                PollWaiter.signal();
                waitWaiter((long)RequestTimeout * MaxRetry);
            }
            releaseWaiter();
            check(result == null, JPOS_E_OFFLINE, "Could not start deposit operation");
            synchronized (CashSlots) {
                CashDepositStartSlots = string2Slots(result[1]);
                deposit = getCountsAmount(depositDelta(), 0, CashMinBillIndex);
            }
            synchronized (CashDepositSync) {
                check(DepositStatus == CACC_STATUS_DEPOSIT_JAM, JPOS_E_FAILURE, "JAM condition");
                DepositAmount = (Integer) deposit[0];
                DepositCounts = (String) deposit[1];
                super.beginDeposit();
                CashCreateDataEvents = RealTimeDataEnabled;
            }
        }

        @Override
        public void fixDeposit() throws JposException {
            attachWaiter();
            String depositstr = sendResp("CASHBOX:StopInput");
            check(depositstr == null, JPOS_E_FAILURE, "Cannot stop cash input");
            PollWaiter.signal();
            waitWaiter((long)RequestTimeout * MaxRetry);
            releaseWaiter();
            Object[] deposit;
            synchronized(CashSlots) {
                deposit = getCountsAmount(depositDelta(), 0, CashMinBillIndex);
                check(Integer.parseInt(depositstr) != (Integer)deposit[0], JPOS_E_FAILURE, "Deposit amount mismatch");
            }
            synchronized (CashDepositSync) {
                CashCreateDataEvents = false;
                DepositAmount = (Integer)deposit[0];
                DepositCounts = (String) deposit[1];
                super.fixDeposit();
            }
        }

        @Override
        public void clearInput() throws JposException {
            cancelCashInput(CashCoinState[CashOperationState]);
            super.clearInput();
            Object[] deposit;
            synchronized(CashSlots) {
                int[][] delta = depositDelta();
                if (delta == null)
                    delta = CashInitialSlots;
                deposit = getCountsAmount(delta, 0, CashMinBillIndex);
                CashDepositStartSlots = null;
            }
            synchronized (CashDepositSync) {
                DepositAmount = (Integer)deposit[0];
                DepositCounts = (String) deposit[1];
                if (DepositStatus != CACC_STATUS_DEPOSIT_JAM)
                    DepositStatus = CACC_STATUS_DEPOSIT_END;
            }
        }

        @Override
        public void endDeposit(int success) throws JposException {
            String depositstr = sendResp("CASHBOX:EndInput");
            synchronized (CashDepositSync) {
                check(depositstr == null, JPOS_E_FAILURE, "Deposit end failure");
            }
            check(Integer.parseInt(depositstr) != DepositAmount, JPOS_E_FAILURE, "Deposit amount mismatch");
            synchronized (CashSlots) {
                CashDepositStartSlots = null;
            }
            synchronized (CashDepositSync) {
                if (DepositStatus != CACC_STATUS_DEPOSIT_JAM)
                    super.endDeposit(success);
            }
        }

        @Override
        public void readCashCounts(String[] cashCounts, boolean[] discrepancy) throws JposException {
            check(Offline, JPOS_E_OFFLINE, "Device is offline");
            attachWaiter();
            PollWaiter.signal();
            waitWaiter((long)RequestTimeout * MaxRetry);
            releaseWaiter();
            check(Offline, JPOS_E_OFFLINE, "Device is offline");
            cashCounts[0] = "";
            int[][] slots;
            synchronized (CashSlots) {
                slots = CashDepositStartSlots == null ? CashSlots[0] : CashDepositStartSlots;
                cashCounts[0] = (String) (getCountsAmount(slots, 0, CashMinBillIndex)[1]);
            }
            discrepancy[0] = false;
        }
    }

    @Override
    public CoinAcceptorProperties getCoinAcceptorProperties(int index) {
        return new SampleUdpDeviceCoinAcceptorProcessor();
    }

    private class SampleUdpDeviceCashChangerStatusUpdateEvent extends CashChangerStatusUpdateEvent {
        public SampleUdpDeviceCashChangerStatusUpdateEvent(JposBase source, int state, int devstate) {
            super(source, state, devstate);
        }

        public SampleUdpDeviceCashChangerStatusUpdateEvent(JposBase source, int state) {
            super(source, state);
        }

        @Override
        public boolean setStatusProperties() {
            synchronized(CashDepositSync) {
                return super.setStatusProperties();
            }
        }
    }

    @Override
    public CashChangerProperties getCashChangerProperties(int index) {
        return new SampleUdpDeviceCashChangerProperties(this);
    }

    private class SampleUdpDeviceBillDispenserStatusUpdateEvent extends BillDispenserStatusUpdateEvent {
        public SampleUdpDeviceBillDispenserStatusUpdateEvent(JposBase source, int state, int devstate) {
            super(source, state, devstate);
        }

        public SampleUdpDeviceBillDispenserStatusUpdateEvent(JposBase source, int state) {
            super(source, state);
        }

        @Override
        public boolean setStatusProperties() {
            synchronized(CashDepositSync) {
                return super.setStatusProperties();
            }
        }
    }

    @Override
    public BillDispenserProperties getBillDispenserProperties(int index) {
        return new SampleUdpDeviceBillDispenserProperties(this);
    }

    private class SampleUdpDeviceCoinDispenserStatusUpdateEvent extends CoinDispenserStatusUpdateEvent {
        public SampleUdpDeviceCoinDispenserStatusUpdateEvent(JposBase source, int state) {
            super(source, state);
        }

        @Override
        public boolean setStatusProperties() {
            synchronized(CashDepositSync) {
                return super.setStatusProperties();
            }
        }
    }

    private class SampleUdpDeviceCoinDispenserProcessor extends CoinDispenserProperties {
        protected SampleUdpDeviceCoinDispenserProcessor() {
            super(0);
        }

        @Override
        public void handlePowerStateOnEnable() throws JposException {
            handleEvent(new JposStatusUpdateEvent(EventSource, Offline ? JPOS_SUE_POWER_OFF_OFFLINE : JPOS_SUE_POWER_ONLINE));
        }

        @Override
        public void initOnEnable(boolean enable) {
            if (enable) {
                synchronized (CashSlots) {
                    char[] state = getCashStates();
                    char opstate = state[CashOperationState];
                    char lowstate = state[CashEmptyState];
                    if (Offline || opstate == CashJam || opstate == CashOpened) {
                        DispenserStatus = COIN_STATUS_JAM;
                    } else {
                        DispenserStatus = lowstate == CashEmpty ? COIN_STATUS_EMPTY
                                : (lowstate == CashNearEmpty ? COIN_STATUS_NEAREMPTY : COIN_STATUS_OK);
                    }
                }
            }
            super.initOnEnable(enable);
        }

        @Override
        public void claim(int timeout) throws JposException {
            synchronized (CashInstances) {
                check(CashInstances[CashCoinInstance] != null && !(CashInstances[CashCoinInstance] instanceof SampleUdpDeviceCashChangerProperties),
                        JPOS_E_CLAIMED, "Device claimed by other coin dispensing instance");
                CashInstances[CashCoinInstance] = this;
            }
            startPolling(this);
            if (Offline && PowerNotify == JPOS_PN_DISABLED) {
                stopPolling();
                synchronized (CashInstances) {
                    CashInstances[CashCoinInstance] = null;
                }
                throw new JposException(JPOS_E_OFFLINE, "Communication with device disrupted");
            }
            super.claim(timeout);
        }

        @Override
        public void release() throws JposException {
            super.release();
            stopPolling();
            synchronized (CashInstances) {
                CashInstances[CashCoinInstance] = null;
            }
        }

        @Override
        @SuppressWarnings("AssignmentUsedAsCondition")
        public void checkHealth(int level) throws JposException {
            String how = level == JPOS_CH_INTERNAL ? "Internal" : (level == JPOS_CH_EXTERNAL ? "External" : "Interactive");
            if (Offline)
                CheckHealthText = how + " Checkhealth: Offline";
            else {
                CheckHealthText = how + " Checkhealth: OK";
                if (level != JPOS_CH_INTERNAL) {
                    boolean interactive;
                    if (interactive = (level == JPOS_CH_INTERACTIVE))
                        synchronizedMessageBox("Press OK to start health test.", "CheckHealth", INFORMATION_MESSAGE);
                    try {
                        String[] counts = {""};
                        boolean[] diff = {false};
                        readCashCounts(counts, diff);
                    } catch (JposException e) {
                        CheckHealthText = how + "Checkhealth: Error: " + e.getMessage();
                    }
                    if (interactive)
                        synchronizedMessageBox("CheckHealth result:\n" + CheckHealthText, "CheckHealth", INFORMATION_MESSAGE);
                }
            }
        }

        @Override
        public void adjustCashCounts(String cashCounts) throws JposException {
            synchronized (CashSlots) {
                check(CashDepositStartSlots != null, JPOS_E_ILLEGAL, "Cash acceptance in progress");
            }
            int[][] slots = cashCounts2ints(cashCounts, 1);
            boolean doit = false;
            synchronized (CashSlots) {
                for (int i = 0; i < CashMinBillIndex; i++) {
                    if ((slots[i][1] -= CashSlots[0][i][1]) != 0)
                        doit = true;
                }
            }
            if (doit) {
                StringBuilder list = new StringBuilder();
                for (int i = 0; i < CashMinBillIndex; i++) {
                    if (slots[i][1] != 0)
                        list.append(" ").append(slots[i][0]).append(" ").append(slots[i][1]);
                }
                String result = sendResp("CASHBOX:AddSlots" + list.substring(1));
                check(result == null || Offline, JPOS_E_FAILURE, "Communication error");
            }
        }

        @Override
        public void readCashCounts(String[] cashCounts, boolean[] discrepancy) throws JposException {
            check(Offline, JPOS_E_OFFLINE, "Device is offline");
            attachWaiter();
            PollWaiter.signal();
            waitWaiter((long)RequestTimeout * MaxRetry);
            releaseWaiter();
            check(Offline, JPOS_E_OFFLINE, "Device is offline");
            cashCounts[0] = "";
            int[][] slots;
            synchronized (CashSlots) {
                slots = Arrays.copyOf(CashDepositStartSlots == null ? CashSlots[0] : CashDepositStartSlots, CashSlots[0].length);
            }
            cashCounts[0] = (String) (getCountsAmount(slots, 0, CashMinBillIndex)[1]);
            discrepancy[0] = false;
        }

        @Override
        public void dispenseChange(int dispenseAmount) throws JposException {
            char[] status;
            int[][] currentslots;
            synchronized(CashSlots) {
                check(CashDepositStartSlots != null, JPOS_E_ILLEGAL, "Cash acceptance in progress");
                status = Arrays.copyOf(CashCoinState, CashCoinState.length);
                currentslots = copySlots(CashSlots[0]);
            }
            check(status[CashOperationState] != CashIdle, JPOS_E_FAILURE, "CoinDispenser not operational");
            int amount = dispenseAmount;
            for (int i = CashMinBillIndex; i >= 0; --i) {
                if (currentslots[i][0] <= amount) {
                    if (amount / currentslots[i][0] <= currentslots[i][1])
                        amount = amount % currentslots[i][0];
                    else
                        amount -= currentslots[i][0] * currentslots[i][1];
                }
            }
            check(amount > 0, JPOS_E_ILLEGAL, "Cannot dispense " + dispenseAmount + " with coins");
            String dispensed = sendResp("CASHBOX:OutputC" + dispenseAmount);
            check (dispensed == null || Offline, JPOS_E_FAILURE, "Dispenser communication error");
            checkext(Integer.parseInt(dispensed) != dispenseAmount, JPOS_ECHAN_OVERDISPENSE,
                    "Dispenser difference: " + (dispenseAmount - Integer.parseInt(dispensed)));
        }
    }

    @Override
    public CoinDispenserProperties getCoinDispenserProperties(int index) {
        return new SampleUdpDeviceCoinDispenserProcessor();
    }
}
