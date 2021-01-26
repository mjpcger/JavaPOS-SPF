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
import de.gmxhome.conrad.jpos.jpos_base.belt.*;
import de.gmxhome.conrad.jpos.jpos_base.billacceptor.*;
import de.gmxhome.conrad.jpos.jpos_base.billdispenser.*;
import de.gmxhome.conrad.jpos.jpos_base.cashchanger.*;
import de.gmxhome.conrad.jpos.jpos_base.cashdrawer.*;
import de.gmxhome.conrad.jpos.jpos_base.coinacceptor.*;
import de.gmxhome.conrad.jpos.jpos_base.coindispenser.*;
import jpos.*;
import jpos.config.JposEntry;
import org.apache.log4j.Level;

import javax.swing.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
    // General purpose objects
    private boolean ToBeFinished = false;
    private int OwnPort = 0;
    private int PollDelay = 200;
    private boolean UseClientIO = true;
    private UniqueIOProcessor Target = null;
    private int[] OpenCount = { 0 };
    private JposCommonProperties StartPollingWaiter = null;
    private Thread StateWatcher;

    /**
     * Offline flag, simple boolean that shows whether the last request has been responded correctly (false) or not (true).
     */
    boolean Offline = true;

    /**
     * Synchronization object for internal thread synchronization.
     */
    SyncObject PollWaiter = new SyncObject();

    /**
     * Timeout in milliseconds for a response from the device. Default 500 milliseconds.
     */
    int RequestTimeout = 500;

    /**
     * Maximum number of retrys in case of a timeout after sending a request to the device. Default 2.
     */
    int MaxRetry = 2;

    // Drawer specific objects, for CashDrawer implementation.
    private boolean DrawerOpen = false;

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

    // Cash box specific objects, for implementation of BillAcceptor, BillDispender, CashChanger, CoinAcceptor and
    // CoinDispenser.

    /**
     * Synchronization object for cash deposit operations.
     */
    int[] CashDepositSync = {0};

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
    int[][] CashSlots = copySlots(CashInitialSlots);

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
    JposCommonProperties[] CashInstances = {null, null, null};

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
    Map<Integer, Integer> CashSlotIndex = new HashMap<Integer, Integer>();

    /**
     * Constructor. id specifies the server to be connected in format host:port.
     *
     * @param id IPv4 target address and port
     */
    protected Device(String id) {
        super(id);
        cashDrawerInit(1);
        beltInit(1);
        billAcceptorInit(1);
        billDispenserInit(1);
        cashChangerInit(1);
        coinAcceptorInit(1);
        coinDispenserInit(1);
        PhysicalDeviceDescription = "UDP device simulator";
        PhysicalDeviceName = "UDP device Simulator";
        CapPowerReporting = JposConst.JPOS_PR_STANDARD;
        for (int i = 0; i < CashSlots.length; i++) {
            CashSlotIndex.put(CashSlots[i][0], i);
        }
    }

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

    @Override
    public void changeDefaults(BillAcceptorProperties props) {
        props.DeviceServiceDescription = "BillAcceptor service for sample UDP device simulator";
        props.DeviceServiceVersion = 1014001;
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
        props.DeviceServiceDescription = "BillDispenser service for sample UDP device simulator";
        props.DeviceServiceVersion = 1014001;
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
        props.DeviceServiceDescription = "CashChanger service for sample UDP device simulator";
        props.DeviceServiceVersion = 1014001;
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
        props.DeviceServiceDescription = "CoinAcceptor service for sample UDP device simulator";
        props.DeviceServiceVersion = 1014001;
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
        props.DeviceServiceDescription = "CoinDispenser service for sample UDP device simulator";
        props.DeviceServiceVersion = 1014001;
        props.CapEmptySensor = true;
        props.CapJamSensor = true;
        props.CapNearEmptySensor = true;
    }

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

        public int checkValidCommand(String command, String response, String[] opportunities) {
            int clen = command.length();
            int rlen = response.length();
            for (String opportunity : opportunities) {
                int olen = opportunity.length();
                if (olen <= clen && olen <= rlen) {
                    if (opportunity.equals(command.substring(0, olen)) && opportunity.equals(response.substring(0, olen)))
                        return olen;
                }
            }
            return 0;
        }
    }

    private class DrawerChecker extends CommonChecker {
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
    }

    private class BeltChecker extends CommonChecker {
        @Override
        public int check(String[] command, String[] response) {
            int rc = super.check(command, response);
            if (0 == rc) {
                if (0 == rc && command[0].equals("BELT")) {
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
    }

    private class CashChecker extends CommonChecker {
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
                        if (values.length == CashSlots.length) {
                            for (int i = 0; i < values.length; i++) {
                                if (values[i][0] != CashSlots[i][0])
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
    }

    private ReturnValueChecker[] checker = {
            new DrawerChecker(),
            new BeltChecker(),
            new CashChecker()
    };

    /**
     * Sends a single command to the device and returns the response on success. In case of a recoverable error,
     * maximum MaxRetry to send the command will be made.
     *
     * @param command Command to be sent to the device.
     * @return        Return parameters of the response on success, otherwise null.
     */
    String sendResp(String command) {
        String[] result = sendResp(new String[]{command});
        return result == null ? null : result[0];
    }

    /**
     * Sends multiple commands to the device and returns the responses on success. In case of a recoverable error,
     * maximum MaxRetry to send the commands will be made.<br>
     * On success, the response values will be returned within a String array of the same length as the array of
     * commands passed as method parameter.
     *
     * @param commandParts Array of commands to be sent to the device.
     * @return             Array of return parameters of the corresponding responses on success, otherwise null.
     */
    synchronized String[] sendResp(String[] commandParts) {
        if (connectionOffline())
            return null;
        try {
            String commands = "";
            String[] result = new String[commandParts.length];
            for (String part : commandParts) {
                commands += "," + part;
            }
            byte[] responses;
            byte[] request = commands.substring(1).getBytes();
            Target.flush();
            long starttime = System.currentTimeMillis();
            long acttime = starttime;
            for (int count = 0; count < MaxRetry; count++) {
                Target.write(request);
                do {
                    Target.setTimeout((int)(RequestTimeout - (acttime - starttime)));
                    responses = Target.read(500);
                    if (UseClientIO || responses.length == 0 || Target.getSource().equals(Target.getTarget())) {
                        String[] responseParts = new String(responses).split(",");
                        if (commandParts.length == responseParts.length) {
                            int checkresult = 1;
                            for (int index = 0; index < commandParts.length && checkresult == 1; index++) {
                                String[] respparts = responseParts[index].split(":");
                                String[] cmdparts = commandParts[index].split(":");
                                for (ReturnValueChecker check : checker) {
                                    if ((checkresult = check.check(cmdparts, respparts)) != 0) {
                                        if (checkresult == 1)
                                            result[index] = respparts[1];
                                        break;
                                    }
                                }
                                result[index] = respparts[1];
                            }
                            if (checkresult == 1)
                                return result;
                        }
                    }
                } while ((acttime = System.currentTimeMillis()) - starttime < RequestTimeout);
            }
        } catch (JposException e) {
            log(Level.TRACE, ID + ": IO error: " + e.getMessage());
        }
        Offline = true;
        closePort();
        return null;
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

    private boolean compareSlots(int[][] slot1, int[][] slot2) {
        if (slot1 == null || slot2 == null)
            return slot1 == slot2;
        if (slot1.length != slot2.length)
            return false;
        for (int i = slot1.length - 1; i >= 0; --i) {
            if (!Arrays.equals(slot1[i], slot2[i]))
                return false;
        }
        return true;
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
            result = copySlots(CashSlots);
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
            check(cashtypestr[1 - validIndices[what][0]].length() > 0, JposConst.JPOS_E_ILLEGAL, cashCounts + " contains invalid cashCount");
        for (int type : validIndices[what]) {
            if (type < cashtypestr.length && cashtypestr[type].length() > 0) {
                String[] countsstr = cashtypestr[type].split(",");
                for (int i = 0; i < countsstr.length; i++) {
                    String[] parts = countsstr[i].split(":");
                    int value = Integer.parseInt(parts[0]);
                    check(!CashSlotIndex.containsKey(value), JposConst.JPOS_E_ILLEGAL, "Invalid cashCount component: " + countsstr[i]);
                    value = CashSlotIndex.get(value);
                    check(value < boundaries[type][0] || boundaries[type][1] <= value, JposConst.JPOS_E_ILLEGAL,
                            "Invalid cashCount type component: " + countsstr[i]);
                    slots[value][1] += Integer.parseInt(parts[1]);
                }
            }
        }
        return slots;
    }

    @Override
    public void run() {
        try {
            while (!ToBeFinished) {
                boolean oldoffline = Offline;
                boolean oldDrawerState = DrawerOpen;
                char[] oldBeltState = BeltState;
                char[] oldBillState = CashBillState;
                char[] oldCoinState = CashCoinState;
                char[] cashState;
                int[][] oldSlots = CashSlots;
                prepareSignalStatusWaits(CashDrawers[0]);
                prepareSignalStatusWaits(BillAcceptors[0]);
                prepareSignalStatusWaits(BillDispensers[0]);
                prepareSignalStatusWaits(CashChangers[0]);
                prepareSignalStatusWaits(CoinAcceptors[0]);
                prepareSignalStatusWaits(CoinDispensers[0]);
                String[] resps = sendResp(new String[]{"DRAWER:GetState",
                        "BELT:GetState",
                        "CASHBOX:GetState",
                        "CASHBOX:GetSlots"});
                if (resps != null) {
                    DrawerOpen = resps[0].equals("1");
                    BeltState = resps[1].toCharArray();
                    cashState = resps[2].toCharArray();
                    synchronized (CashSlots) {
                        CashSlots = string2Slots(resps[3]);
                        setCashStates(cashState);
                    }
                }
                statusUpdateEventProcessing(oldoffline, oldDrawerState, oldBeltState, oldCoinState, oldBillState, oldSlots);
                if (StartPollingWaiter != null) {
                    StartPollingWaiter.signalWaiter();
                    StartPollingWaiter = null;
                }
                signalStatusWaits(BillAcceptors[0]);
                signalStatusWaits(CoinAcceptors[0]);
                signalStatusWaits(BillDispensers[0]);
                signalStatusWaits(CoinDispensers[0]);
                signalStatusWaits(CashChangers[0]);
                PollWaiter.suspend(PollDelay);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void setCashStates(char[] cashState) {
        CashCoinState = Arrays.copyOf(cashState, cashState.length);
        CashBillState = cashState;
        setCashState(CashCoinState, cashState, Arrays.copyOf(CashSlots, CashMinBillIndex));
        setCashState(CashBillState, cashState, Arrays.copyOfRange(CashSlots, CashMinBillIndex, CashSlots.length));
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

    private void statusUpdateEventProcessing(boolean oldoffline, boolean oldDrawerState, char[] oldBeltState, char[] oldCoinState, char[] oldBillState, int[][] oldSlots) {
        if (oldoffline && oldoffline != Offline) {
            oldoffline = statusPowerOnlineProcessing();
        }
        if (!Offline && oldDrawerState != DrawerOpen) {
            statusDrawerProcessing();
        }
        if (!Offline && !Arrays.equals(oldBeltState, BeltState)) {
            statusBeltProcessing(oldBeltState);
        }
        if (!Offline &&
                (!Arrays.equals(getCashStates(oldBillState, oldCoinState), getCashStates(CashBillState, CashCoinState))
                        || !compareSlots(oldSlots, CashSlots)
                )
        ) {
            statusCashProcessing(oldBillState, oldCoinState, oldSlots);
        }
        if (Offline && oldoffline != Offline) {
            statusPowerOfflineProcessing();
        }
    }

    private void statusCashProcessing(char[] oldBillState, char[] oldCoinState, int[][] oldSlots) {
        sendCashFullEvents(oldBillState, oldCoinState);
        sendCashEmptyEvents(oldBillState, oldCoinState);
        if ((oldBillState[CashOperationState] > CashFinishInput) != (CashBillState[CashOperationState] > CashFinishInput)) {
            sendCashJamEvent(CashBillState[CashOperationState] > CashFinishInput);
        }
        if (!compareSlots(oldSlots, CashSlots)) {
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
        } catch (JposException e) {
        }
    }

    private void sendCashJamEvent(boolean b) {
        try {
            int[][] states = {
                    {BillAcceptorConst.BACC_STATUS_JAM, BillAcceptorConst.BACC_STATUS_JAMOK},
                    {BillDispenserConst.BDSP_STATUS_JAM, BillDispenserConst.BDSP_STATUS_JAMOK},
                    {CashChangerConst.CHAN_STATUS_JAM, CashChangerConst.CHAN_STATUS_JAMOK},
                    {CoinAcceptorConst.CACC_STATUS_JAM, CoinAcceptorConst.CACC_STATUS_JAMOK},
                    {CoinDispenserConst.COIN_STATUS_JAM, CoinDispenserConst.COIN_STATUS_OK}
            };
            int i = b ? 0 : 1;
            int j = CashBillState[CashEmptyState] - '0';
            JposCommonProperties props = getClaimingInstance(ClaimedBillAcceptor, 0);
            if (props != null) {
                handleEvent(new SampleUdpDeviceBillAcceptorStatusUpdateEvent(props.EventSource, states[0][i]));
            }
            props = getClaimingInstance(ClaimedBillDispenser, 0);
            if (props != null) {
                int[] devstate = {BillDispenserConst.BDSP_STATUS_EMPTY, BillDispenserConst.BDSP_STATUS_NEAREMPTY, BillDispenserConst.BDSP_STATUS_OK};
                if (b)
                    devstate[j] = BillDispenserConst.BDSP_STATUS_JAM;
                handleEvent(new SampleUdpDeviceBillDispenserStatusUpdateEvent(props.EventSource, states[0][i], devstate[j]));
            }
            props = getClaimingInstance(ClaimedCashChanger, 0);
            if (props != null) {
                int[] devstate = {CashChangerConst.CHAN_STATUS_EMPTY, CashChangerConst.CHAN_STATUS_NEAREMPTY, CashChangerConst.CHAN_STATUS_OK};
                if (((SampleUdpDeviceCashChangerProperties) props).DepositStatus != CashChangerConst.CHAN_STATUS_DEPOSIT_END) {
                    devstate[j] = CashChangerConst.CHAN_STATUS_DEPOSIT_END;
                }
                handleEvent(new SampleUdpDeviceCashChangerStatusUpdateEvent(props.EventSource, states[0][i], devstate[j]));
            }
            props = getClaimingInstance(ClaimedCoinAcceptor, 0);
            if (props != null) {
                handleEvent(new SampleUdpDeviceCoinAcceptorStatusUpdateEvent(props.EventSource, states[0][i]));
            }
            props = getClaimingInstance(ClaimedCoinDispenser, 0);
            if (props != null) {
                int[] devstate = {CoinDispenserConst.COIN_STATUS_EMPTY, CoinDispenserConst.COIN_STATUS_NEAREMPTY, CoinDispenserConst.COIN_STATUS_OK};
                if (b)
                    devstate[j] = CoinDispenserConst.COIN_STATUS_JAM;
                handleEvent(new SampleUdpDeviceCoinDispenserStatusUpdateEvent(props.EventSource, devstate[j]));
            }
        } catch (JposException e) {
        }
    }

    private void sendCashEmptyEvents(char[] oldBillState, char[] oldCoinState) {
        try {
            int[][] states = {
                    {BillDispenserConst.BDSP_STATUS_EMPTY, BillDispenserConst.BDSP_STATUS_NEAREMPTY, BillDispenserConst.BDSP_STATUS_EMPTYOK},
                    {CashChangerConst.CHAN_STATUS_EMPTY, CashChangerConst.CHAN_STATUS_NEAREMPTY, CashChangerConst.CHAN_STATUS_EMPTYOK},
                    {CoinDispenserConst.COIN_STATUS_EMPTY, CoinDispenserConst.COIN_STATUS_NEAREMPTY, CoinDispenserConst.COIN_STATUS_OK}
            };
            char[] cashstate = getCashStates(CashBillState, CashCoinState);
            JposCommonProperties props = getClaimingInstance(ClaimedBillDispenser, 0);
            if (props != null && CashBillState[CashEmptyState] != oldBillState[CashEmptyState]) {
                int i = CashBillState[CashEmptyState] - '0';
                int[] devstate = {BillDispenserConst.BDSP_STATUS_EMPTY, BillDispenserConst.BDSP_STATUS_NEAREMPTY, BillDispenserConst.BDSP_STATUS_OK};
                if (CashBillState[CashOperationState] > CashInput)
                    devstate[i] = BillDispenserConst.BDSP_STATUS_JAM;
                handleEvent(new SampleUdpDeviceBillDispenserStatusUpdateEvent(props.EventSource, states[0][i], devstate[i]));
            }
            props = getClaimingInstance(ClaimedCashChanger, 0);
            if (props != null && cashstate[CashEmptyState] != getCashStates(oldBillState, oldCoinState)[CashEmptyState]) {
                int i = cashstate[CashEmptyState] - '0';
                int[] devstate = {CashChangerConst.CHAN_STATUS_EMPTY, CashChangerConst.CHAN_STATUS_NEAREMPTY, CashChangerConst.CHAN_STATUS_OK};
                if (((SampleUdpDeviceCashChangerProperties)props).DepositStatus == CashChangerConst.CHAN_STATUS_DEPOSIT_END &&
                        CashBillState[CashOperationState] > CashInput)
                    devstate[i] = CashChangerConst.CHAN_STATUS_JAM;
                handleEvent(new SampleUdpDeviceCashChangerStatusUpdateEvent(props.EventSource, states[0][i], devstate[i]));
            }
            props = getClaimingInstance(ClaimedCoinDispenser, 0);
            if (props != null && CashCoinState[CashEmptyState] != oldCoinState[CashEmptyState]) {
                int i = CashCoinState[CashEmptyState] - '0';
                int[] devstate = {CoinDispenserConst.COIN_STATUS_EMPTY, CoinDispenserConst.COIN_STATUS_NEAREMPTY, CoinDispenserConst.COIN_STATUS_OK};
                if (CashCoinState[CashOperationState] > CashInput)
                    devstate[i] = CoinDispenserConst.COIN_STATUS_JAM;
                handleEvent(new SampleUdpDeviceCoinDispenserStatusUpdateEvent(props.EventSource, devstate[i]));
            }
        } catch (JposException e) {
        }
    }

    private void sendCashFullEvents(char[] oldBillState, char[] oldCoinState) {
        try {
            int[][] states = {
                    {BillAcceptorConst.BACC_STATUS_FULL, BillAcceptorConst.BACC_STATUS_NEARFULL, BillAcceptorConst.BACC_STATUS_FULLOK},
                    {CashChangerConst.CHAN_STATUS_FULL, CashChangerConst.CHAN_STATUS_NEARFULL, CashChangerConst.CHAN_STATUS_FULLOK},
                    {CoinAcceptorConst.CACC_STATUS_FULL, CoinAcceptorConst.CACC_STATUS_NEARFULL, CoinAcceptorConst.CACC_STATUS_FULLOK}
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
        } catch (JposException e) {
        }
    }

    private void statusBeltProcessing(char[] oldBeltState) {
        if (sendBeltStateEvents(oldBeltState)) {
            sendResp("BELT:Speed0");
        }
    }

    private void statusDrawerProcessing() {
        sendDrawerStateEvent();
        signalStatusWaits(CashDrawers[0]);
    }

    private boolean statusPowerOnlineProcessing() {
        boolean oldoffline;
        sendPowerStateEvents();
        oldoffline = Offline;
        return oldoffline;
    }

    private void statusPowerOfflineProcessing() {
        sendPowerStateEvents();
        signalStatusWaits(CashDrawers[0]);
        signalStatusWaits(Belts[0]);
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
            if ((props = getClaimingInstance(ClaimedBillAcceptor, 0)) != null)
                handleEvent(new SampleUdpDeviceBillAcceptorStatusUpdateEvent(props.EventSource, state));
            if ((props = getClaimingInstance(ClaimedBillDispenser, 0)) != null)
                handleEvent(new SampleUdpDeviceBillDispenserStatusUpdateEvent(props.EventSource, state));
            if ((props = getClaimingInstance(ClaimedCashChanger, 0)) != null)
                handleEvent(new SampleUdpDeviceCashChangerStatusUpdateEvent(props.EventSource, state));
            if ((props = getClaimingInstance(ClaimedCoinDispenser, 0)) != null)
                handleEvent(new SampleUdpDeviceCoinDispenserStatusUpdateEvent(props.EventSource, state));
            if ((props = getClaimingInstance(ClaimedCoinAcceptor, 0)) != null)
                handleEvent(new SampleUdpDeviceCoinAcceptorStatusUpdateEvent(props.EventSource, state));
        } catch (JposException e) {}
    }

    /**
     * Method to start status updating thread if started for the first time.
     * @param props Property set of the device that starts polling
     * @return      Start count, 1 if updating thread has been started and a value &gt; 1 if the thread is just running.
     */
    int startPolling(JposCommonProperties props) {
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

    /**
     * Method to stop status updating thread if stopped as many times as previously started.
     * @return      Start count, 0 if updating thread has been stopped and a value &gt; 1 if the thread is just running.
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
            if (!DrawerOpened && !Offline) {
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
            sendResp(cmd[speed - 1]);
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
            sendResp("BELT:Speed0");
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
            sendResp("BELT:Speed0");
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
            props.waitWaiter(RequestTimeout * MaxRetry);
            props.releaseWaiter();
            synchronized(CashSlots) {
                state = CashBillState[CashOperationState];
                check(Offline || (state == CashInput || state == CashFinishInput), JposConst.JPOS_E_FAILURE, "Unable to cancel deposit operation");
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
        Integer amount = 0;
        String counts = "";
        for (int i = from; i < to; i++) {
            amount += slots[i][0] * slots[i][1];
            counts += "," + slots[i][0] + ":" + slots[i][1];
        }
        return new Object[]{ amount, (from == 0 ? "" : ";") + counts.substring(1) };
    }

    class SampleUdpDeviceBillAcceptorStatusUpdateEvent extends BillAcceptorStatusUpdateEvent {
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

    class SampleUdpDeviceBillAcceptorProcessor extends BillAcceptorProperties {
        protected SampleUdpDeviceBillAcceptorProcessor() {
            super(0);
        }

        @Override
        public void handlePowerStateOnEnable() throws JposException {
            handleEvent(new JposStatusUpdateEvent(EventSource, Offline ? JposConst.JPOS_SUE_POWER_OFF_OFFLINE : JposConst.JPOS_SUE_POWER_ONLINE));
        }

        @Override
        public void initOnEnable(boolean enable) {
            if (enable) {
                synchronized (CashSlots) {
                    char[] state = getCashStates();
                    char opstate = state[CashOperationState];
                    char lowstate = state[CashEmptyState];
                    char histate = state[CashFullState];
                    if (Offline || opstate == CashJam || opstate == CashOpened) {
                        DepositStatusDef = BillAcceptorConst.BACC_STATUS_DEPOSIT_JAM;
                        FullStatusDef = BillAcceptorConst.BACC_STATUS_FULL;
                    } else {
                        FullStatusDef = histate == CashFull ? BillAcceptorConst.BACC_STATUS_FULL
                                : (histate == CashNearlyFull ? BillAcceptorConst.BACC_STATUS_NEARFULL : BillAcceptorConst.BACC_STATUS_OK);
                        DepositStatusDef = opstate == CashIdle ? BillAcceptorConst.BACC_STATUS_DEPOSIT_END
                                : (opstate == CashInput ? BillAcceptorConst.BACC_STATUS_DEPOSIT_START : BillAcceptorConst.BACC_STATUS_DEPOSIT_COUNT);
                    }
                }
            }
            super.initOnEnable(enable);
        }

        @Override
        public void claim(int timeout) throws JposException {
            synchronized (CashInstances) {
                check(CashInstances[CashAcceptInstance] != null && !(CashInstances[CashAcceptInstance] instanceof BillAcceptorProperties),
                        JposConst.JPOS_E_CLAIMED, "Device claimed by other cash accepting instance");
                CashInstances[CashAcceptInstance] = this;
            }
            startPolling(this);
            if (Offline && PowerNotify == JposConst.JPOS_PN_DISABLED) {
                stopPolling();
                synchronized(CashInstances) {
                    CashInstances[CashAcceptInstance] = null;
                }
                throw new JposException(JposConst.JPOS_E_OFFLINE, "Communication with device disrupted");
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
                        String[] counts = {""};
                        boolean[] diff = {false};
                        readCashCounts(counts, diff);
                    } catch (JposException e) {
                        CheckHealthText = how + "Checkhealth: Error: " + e.getMessage();
                    }
                    if (interactive)
                        synchronizedMessageBox("CheckHealth result:\n" + CheckHealthText, "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }

        @Override
        public void adjustCashCounts(String cashCounts) throws JposException {
            synchronized (CashSlots) {
                check(CashDepositStartSlots != null, JposConst.JPOS_E_ILLEGAL, "Cash acceptance in progress");
            }
            int[][] slots = cashCounts2ints(cashCounts, 2);
            boolean doit = false;
            synchronized (CashSlots) {
                for (int i = CashMinBillIndex; i < slots.length; i++) {
                    if ((slots[i][1] -= CashSlots[i][1]) != 0)
                        doit = true;
                }
            }
            if (doit) {
                String list = "";
                for (int i = CashMinBillIndex; i < slots.length; i++) {
                    if (slots[i][1] != 0)
                        list += " " + slots[i][0] + " " + slots[i][1];
                }
                String result = sendResp("CASHBOX:AddSlots" + list.substring(1));
                check(result == null || Offline, JposConst.JPOS_E_FAILURE, "Communication error");
            }
        }

        @Override
        public void beginDeposit() throws JposException {
            check(DepositStatus != BillAcceptorConst.BACC_STATUS_DEPOSIT_END, JposConst.JPOS_E_ILLEGAL, "Bad deposit state");
            attachWaiter();
            String[] result = sendResp(new String[]{"CASHBOX:StartInput2", "CASHBOX:GetSlots"});
            Object[] deposit;
            if (result != null) {
                PollWaiter.signal();
                waitWaiter(RequestTimeout * MaxRetry);
            }
            releaseWaiter();
            check(result == null, JposConst.JPOS_E_OFFLINE, "Could not start deposit operation");
            synchronized (CashSlots) {
                CashDepositStartSlots = string2Slots(result[1]);
                deposit = getCountsAmount(depositDelta(), CashMinBillIndex, CashSlots.length);
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
            check(depositstr == null, JposConst.JPOS_E_FAILURE, "Cannot stop cash input");
            PollWaiter.signal();
            waitWaiter(RequestTimeout * MaxRetry);
            releaseWaiter();
            Object[] deposit;
            synchronized(CashSlots) {
                deposit = getCountsAmount(depositDelta(), CashMinBillIndex, CashSlots.length);
                check(Integer.parseInt(depositstr) != (Integer)deposit[0], JposConst.JPOS_E_FAILURE, "Deposit amount mismatch");
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
                deposit = getCountsAmount(delta, CashMinBillIndex, CashSlots.length);
                CashDepositStartSlots = null;
            }
            synchronized (CashDepositSync) {
                DepositAmount = (Integer)deposit[0];
                DepositCounts = (String) deposit[1];
                if (DepositStatus != BillAcceptorConst.BACC_STATUS_DEPOSIT_JAM)
                    DepositStatus = BillAcceptorConst.BACC_STATUS_DEPOSIT_END;
            }
        }

        @Override
        public void endDeposit(int success) throws JposException {
            String depositstr = sendResp("CASHBOX:EndInput");
            synchronized (CashDepositSync) {
                check(depositstr == null, JposConst.JPOS_E_FAILURE, "Deposit end failure");
            }
            check(Integer.parseInt(depositstr) != DepositAmount, JposConst.JPOS_E_FAILURE, "Deposit amount mismatch");
            synchronized (CashSlots) {
                CashDepositStartSlots = null;
            }
            synchronized (CashDepositSync) {
                if (DepositStatus != BillAcceptorConst.BACC_STATUS_DEPOSIT_JAM)
                    super.endDeposit(success);
            }
        }

        @Override
        public void readCashCounts(String[] cashCounts, boolean[] discrepancy) throws JposException {
            check(Offline, JposConst.JPOS_E_OFFLINE, "Device is offline");
            attachWaiter();
            PollWaiter.signal();
            waitWaiter(RequestTimeout * MaxRetry);
            releaseWaiter();
            check(Offline, JposConst.JPOS_E_OFFLINE, "Device is offline");
            cashCounts[0] = "";
            int[][] slots;
            synchronized (CashSlots) {
                slots = CashDepositStartSlots == null ? CashSlots : CashDepositStartSlots;
                cashCounts[0] = (String) (getCountsAmount(slots, CashMinBillIndex, slots.length)[1]);
            }
            discrepancy[0] = false;
        }
    }

    @Override
    public BillAcceptorProperties getBillAcceptorProperties(int index) {
        return new SampleUdpDeviceBillAcceptorProcessor();
    }

    class SampleUdpDeviceCoinAcceptorStatusUpdateEvent extends CoinAcceptorStatusUpdateEvent {
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

    class SampleUdpDeviceCoinAcceptorProcessor extends CoinAcceptorProperties {
        protected SampleUdpDeviceCoinAcceptorProcessor() {
            super(0);
        }

        @Override
        public void handlePowerStateOnEnable() throws JposException {
            handleEvent(new JposStatusUpdateEvent(EventSource, Offline ? JposConst.JPOS_SUE_POWER_OFF_OFFLINE : JposConst.JPOS_SUE_POWER_ONLINE));
        }

        @Override
        public void initOnEnable(boolean enable) {
            if (enable) {
                synchronized (CashSlots) {
                    char[] state = getCashStates();
                    char opstate = state[CashOperationState];
                    char lowstate = state[CashEmptyState];
                    char histate = state[CashFullState];
                    if (Offline || opstate == CashJam || opstate == CashOpened) {
                        DepositStatusDef = CoinAcceptorConst.CACC_STATUS_DEPOSIT_JAM;
                        FullStatusDef = CoinAcceptorConst.CACC_STATUS_FULL;
                    } else {
                        FullStatusDef = histate == CashFull ? CoinAcceptorConst.CACC_STATUS_FULL
                                : (histate == CashNearlyFull ? CoinAcceptorConst.CACC_STATUS_NEARFULL : CoinAcceptorConst.CACC_STATUS_OK);
                        DepositStatusDef = opstate == CashIdle ? CoinAcceptorConst.CACC_STATUS_DEPOSIT_END
                                : (opstate == CashInput ? CoinAcceptorConst.CACC_STATUS_DEPOSIT_START : CoinAcceptorConst.CACC_STATUS_DEPOSIT_COUNT);
                    }
                }
            }
            super.initOnEnable(enable);
        }

        @Override
        public void claim(int timeout) throws JposException {
            synchronized (CashInstances) {
                check(CashInstances[CashAcceptInstance] != null && !(CashInstances[CashAcceptInstance] instanceof CoinAcceptorProperties),
                        JposConst.JPOS_E_CLAIMED, "Device claimed by other cash accepting instance");
                CashInstances[CashAcceptInstance] = this;
            }
            startPolling(this);
            if (Offline && PowerNotify == JposConst.JPOS_PN_DISABLED) {
                stopPolling();
                synchronized (CashInstances) {
                    CashInstances[CashAcceptInstance] = null;
                }
                throw new JposException(JposConst.JPOS_E_OFFLINE, "Communication with device disrupted");
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
                        String[] counts = {""};
                        boolean[] diff = {false};
                        readCashCounts(counts, diff);
                    } catch (JposException e) {
                        CheckHealthText = how + "Checkhealth: Error: " + e.getMessage();
                    }
                    if (interactive)
                        synchronizedMessageBox("CheckHealth result:\n" + CheckHealthText, "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }

        @Override
        public void adjustCashCounts(String cashCounts) throws JposException {
            synchronized (CashSlots) {
                check(CashDepositStartSlots != null, JposConst.JPOS_E_ILLEGAL, "Cash acceptance in progress");
            }
            int[][] slots = cashCounts2ints(cashCounts, 1);
            boolean doit = false;
            synchronized (CashSlots) {
                for (int i = 0; i < CashMinBillIndex; i++) {
                    if ((slots[i][1] -= CashSlots[i][1]) != 0)
                        doit = true;
                }
            }
            if (doit) {
                String list = "";
                for (int i = 0; i < CashMinBillIndex; i++) {
                    if (slots[i][1] != 0)
                        list += " " + slots[i][0] + " " + slots[i][1];
                }
                String result = sendResp("CASHBOX:AddSlots" + list.substring(1));
                check(result == null || Offline, JposConst.JPOS_E_FAILURE, "Communication error");
            }
        }

        @Override
        public void beginDeposit() throws JposException {
            check(DepositStatus != CoinAcceptorConst.CACC_STATUS_DEPOSIT_END, JposConst.JPOS_E_ILLEGAL, "Bad deposit state");
            attachWaiter();
            String[] result = sendResp(new String[]{"CASHBOX:StartInput1", "CASHBOX:GetSlots"});
            Object[] deposit;
            if (result != null) {
                PollWaiter.signal();
                waitWaiter(RequestTimeout * MaxRetry);
            }
            releaseWaiter();
            check(result == null, JposConst.JPOS_E_OFFLINE, "Could not start deposit operation");
            synchronized (CashSlots) {
                CashDepositStartSlots = string2Slots(result[1]);
                deposit = getCountsAmount(depositDelta(), 0, CashMinBillIndex);
            }
            synchronized (CashDepositSync) {
                check(DepositStatus == CoinAcceptorConst.CACC_STATUS_DEPOSIT_JAM, JposConst.JPOS_E_FAILURE, "JAM condition");
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
            check(depositstr == null, JposConst.JPOS_E_FAILURE, "Cannot stop cash input");
            PollWaiter.signal();
            waitWaiter(RequestTimeout * MaxRetry);
            releaseWaiter();
            Object[] deposit;
            synchronized(CashSlots) {
                deposit = getCountsAmount(depositDelta(), 0, CashMinBillIndex);
                check(Integer.parseInt(depositstr) != (Integer)deposit[0], JposConst.JPOS_E_FAILURE, "Deposit amount mismatch");
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
                if (DepositStatus != CoinAcceptorConst.CACC_STATUS_DEPOSIT_JAM)
                    DepositStatus = CoinAcceptorConst.CACC_STATUS_DEPOSIT_END;
            }
        }

        @Override
        public void endDeposit(int success) throws JposException {
            String depositstr = sendResp("CASHBOX:EndInput");
            synchronized (CashDepositSync) {
                check(depositstr == null, JposConst.JPOS_E_FAILURE, "Deposit end failure");
            }
            check(Integer.parseInt(depositstr) != DepositAmount, JposConst.JPOS_E_FAILURE, "Deposit amount mismatch");
            synchronized (CashSlots) {
                CashDepositStartSlots = null;
            }
            synchronized (CashDepositSync) {
                if (DepositStatus != CoinAcceptorConst.CACC_STATUS_DEPOSIT_JAM)
                    super.endDeposit(success);
            }
        }

        @Override
        public void readCashCounts(String[] cashCounts, boolean[] discrepancy) throws JposException {
            check(Offline, JposConst.JPOS_E_OFFLINE, "Device is offline");
            attachWaiter();
            PollWaiter.signal();
            waitWaiter(RequestTimeout * MaxRetry);
            releaseWaiter();
            check(Offline, JposConst.JPOS_E_OFFLINE, "Device is offline");
            cashCounts[0] = "";
            int[][] slots;
            synchronized (CashSlots) {
                slots = CashDepositStartSlots == null ? CashSlots : CashDepositStartSlots;
                cashCounts[0] = (String) (getCountsAmount(slots, 0, CashMinBillIndex)[1]);
            }
            discrepancy[0] = false;
        }
    }

    @Override
    public CoinAcceptorProperties getCoinAcceptorProperties(int index) {
        return new SampleUdpDeviceCoinAcceptorProcessor();
    }

    class SampleUdpDeviceCashChangerStatusUpdateEvent extends CashChangerStatusUpdateEvent {
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

    class SampleUdpDeviceBillDispenserStatusUpdateEvent extends BillDispenserStatusUpdateEvent {
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

    class SampleUdpDeviceCoinDispenserStatusUpdateEvent extends CoinDispenserStatusUpdateEvent {
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

    class SampleUdpDeviceCoinDispenserProcessor extends CoinDispenserProperties {
        protected SampleUdpDeviceCoinDispenserProcessor() {
            super(0);
        }

        @Override
        public void handlePowerStateOnEnable() throws JposException {
            handleEvent(new JposStatusUpdateEvent(EventSource, Offline ? JposConst.JPOS_SUE_POWER_OFF_OFFLINE : JposConst.JPOS_SUE_POWER_ONLINE));
        }

        @Override
        public void initOnEnable(boolean enable) {
            if (enable) {
                synchronized (CashSlots) {
                    char[] state = getCashStates();
                    char opstate = state[CashOperationState];
                    char lowstate = state[CashEmptyState];
                    char histate = state[CashFullState];
                    if (Offline || opstate == CashJam || opstate == CashOpened) {
                        DispenserStatus = CoinDispenserConst.COIN_STATUS_JAM;
                    } else {
                        DispenserStatus = lowstate == CashEmpty ? CoinDispenserConst.COIN_STATUS_EMPTY
                                : (lowstate == CashNearEmpty ? CoinDispenserConst.COIN_STATUS_NEAREMPTY : CoinDispenserConst.COIN_STATUS_OK);
                    }
                }
            }
            super.initOnEnable(enable);
        }

        @Override
        public void claim(int timeout) throws JposException {
            synchronized (CashInstances) {
                check(CashInstances[CashCoinInstance] != null && !(CashInstances[CashCoinInstance] instanceof SampleUdpDeviceCashChangerProperties),
                        JposConst.JPOS_E_CLAIMED, "Device claimed by other coin dispensing instance");
                CashInstances[CashCoinInstance] = this;
            }
            startPolling(this);
            if (Offline && PowerNotify == JposConst.JPOS_PN_DISABLED) {
                stopPolling();
                synchronized (CashInstances) {
                    CashInstances[CashCoinInstance] = null;
                }
                throw new JposException(JposConst.JPOS_E_OFFLINE, "Communication with device disrupted");
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
                        String[] counts = {""};
                        boolean[] diff = {false};
                        readCashCounts(counts, diff);
                    } catch (JposException e) {
                        CheckHealthText = how + "Checkhealth: Error: " + e.getMessage();
                    }
                    if (interactive)
                        synchronizedMessageBox("CheckHealth result:\n" + CheckHealthText, "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }

        @Override
        public void adjustCashCounts(String cashCounts) throws JposException {
            synchronized (CashSlots) {
                check(CashDepositStartSlots != null, JposConst.JPOS_E_ILLEGAL, "Cash acceptance in progress");
            }
            int[][] slots = cashCounts2ints(cashCounts, 1);
            boolean doit = false;
            synchronized (CashSlots) {
                for (int i = 0; i < CashMinBillIndex; i++) {
                    if ((slots[i][1] -= CashSlots[i][1]) != 0)
                        doit = true;
                }
            }
            if (doit) {
                String list = "";
                for (int i = 0; i < CashMinBillIndex; i++) {
                    if (slots[i][1] != 0)
                        list += " " + slots[i][0] + " " + slots[i][1];
                }
                String result = sendResp("CASHBOX:AddSlots" + list.substring(1));
                check(result == null || Offline, JposConst.JPOS_E_FAILURE, "Communication error");
            }
        }

        @Override
        public void readCashCounts(String[] cashCounts, boolean[] discrepancy) throws JposException {
            check(Offline, JposConst.JPOS_E_OFFLINE, "Device is offline");
            attachWaiter();
            PollWaiter.signal();
            waitWaiter(RequestTimeout * MaxRetry);
            releaseWaiter();
            check(Offline, JposConst.JPOS_E_OFFLINE, "Device is offline");
            cashCounts[0] = "";
            int[][] slots;
            synchronized (CashSlots) {
                slots = Arrays.copyOf(CashDepositStartSlots == null ? CashSlots : CashDepositStartSlots, CashSlots.length);
            }
            cashCounts[0] = (String) (getCountsAmount(slots, 0, CashMinBillIndex)[1]);
            discrepancy[0] = false;
        }

        @Override
        public void dispenseChange(int dispenseAmount) throws JposException {
            char[] status;
            int[][] currentslots;
            synchronized(CashSlots) {
                check(CashDepositStartSlots != null, JposConst.JPOS_E_ILLEGAL, "Cash acceptance in progress");
                status = Arrays.copyOf(CashCoinState, CashCoinState.length);
                currentslots = copySlots(CashSlots);
            }
            check(status[CashOperationState] != CashIdle, JposConst.JPOS_E_FAILURE, "CoinDispenser not operational");
            int amount = dispenseAmount;
            for (int i = CashMinBillIndex; i >= 0; --i) {
                if (currentslots[i][0] <= amount) {
                    if (amount / currentslots[i][0] <= currentslots[i][1])
                        amount = amount % currentslots[i][0];
                    else
                        amount -= currentslots[i][0] * currentslots[i][1];
                }
            }
            check(amount > 0, JposConst.JPOS_E_ILLEGAL, "Cannot dispense " + dispenseAmount + " with coins");
            String dispensed = sendResp("CASHBOX:OutputC" + dispenseAmount);
            check (dispensed == null || Offline, JposConst.JPOS_E_FAILURE, "Dispenser communication error");
            checkext(Integer.parseInt(dispensed) != dispenseAmount, CashChangerConst.JPOS_ECHAN_OVERDISPENSE,
                    "Dispenser difference: " + (dispenseAmount - Integer.parseInt(dispensed)));
        }
    }

    @Override
    public CoinDispenserProperties getCoinDispenserProperties(int index) {
        return new SampleUdpDeviceCoinDispenserProcessor();
    }
}
