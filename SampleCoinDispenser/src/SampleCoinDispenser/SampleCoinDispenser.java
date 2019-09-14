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

package SampleCoinDispenser;

import jpos.CoinDispenserConst;
import jpos.JposConst;
import jpos.JposException;
import jpos.config.JposEntry;
import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.coindispenser.*;
import org.apache.log4j.Level;;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

import static de.gmxhome.conrad.jpos.jpos_base.UniqueIOProcessor.IOProcessorError;

/**
 * Implementation of a JposDevice based implementation of a coin dispenser driver that becomes
 * a JavaPOS CoinDispenser service in combination with the JposCoinDispenser class
 */
public class SampleCoinDispenser extends JposDevice implements Runnable{
    /**
     * Server port of sample CoinDispenser.
     */
    String Port = "127.0.0.1:56789";

    /**
     * Own client port. If not specified, random port will be allocated dynamically by operating system. This
     * is good for normal operation but may be a problem in some firewall configurations.
     */
    int OwnPort = 0;

    /**
     * Minimum claim timeout. If a timeout value less than MinClaimTimeout will be used in claim call,
     * claim will use MinClaimTimeout instead of the given timeout to wait for successful operation.
     */
    int MinClaimTimeout = 100;

    /**
     * Maximum time, in milliseconds, between sending a command to the simulator and getting the first byte of its
     * response.
     */
    int RequestTimeout = 200;

    /**
     * Maximum time, in milliseconds, between characters belonging to the same command response.
     */
    int CharacterTimeout = 50;

    /**
     * Minimum time between status requests. Status requests will be used to monitor the coin dispenser state.
     */
    int PollTimeout = 500;

    /**
     * Minimum amount of coins in each slot. If one slot contains a lower number of coins, status will
     * be reported as near empty. However, an empty status will only be reported if all slots are empty.
     */
    int NearLimit = 2;

    /**
     * The maximum number of coins that one single hardware slot can hold.
     */
    int SlotCapacity = 999;

    /**
     * Specifies whether the cashCounts argument of readCashCounts shall be used as a template for the result or
     * be overwritten by a string that contains cashCount values for all slots.
     */
    boolean ReadArgumentCheck = false;

    /**
     * The TCP strean processor.
     */
    private TcpClientIOProcessor OutStream = null;

    /**
     * The status watcher thread.
     */
    private Thread StateWatcher;

    /**
     * A synchronization object used for timing.
     */
    private SyncObject WaitObj;

    /**
     * A synchronization object used to wait until startup has been finished.
     */
    private SyncObject WaitInitialized;

    /**
     * Termination flag, set when state watcher shall finish.
     */
    private boolean ToBeFinished;

    /**
     * Coin counts per virtual slot.
     */
    private int SlotCount[] = new int[8];                   // ( slots (1, 2, 5, 10, 20, 50, 100 and 200 cent)

    /**
     * Coin values of hardware slots
     */
    private int HWSlotCount[] = new int[12];
    /**
     * Maximum response length.
     */
    static private final int MaxRespLen = 2 + 11 * 4 + 1;   // "OK nnn nnn nnn nnn nnn nnn nnn nnn nnn nnn nnn" NL

    /**
     * Dispenser state, valid if coin dispenser is online.
     */
    private int DispenserState = CoinDispenserConst.COIN_STATUS_OK;

    /**
     * Offline state, true if dispenser is offline.
     */
    private int Offline = JposConst.JPOS_PS_UNKNOWN;

    /**
     * Indicates whether we are in error recovery state
     */
    boolean InIOError = false;
    /*
     Slot indices
     */
    static private final int Slot1 = 0;
    static private final int Slot2 = 1;
    static private final int Slot5 = 2;
    static private final int Slot10 = 3;
    static private final int Slot20 = 4;
    static private final int Slot50 = 5;
    static private final int Slot100 = 6;
    static private final int Slot200 = 7;
    /*
     Coin values for indexed virtual slots
     */
    static private final long[] SlotCoinValues = new long[]{1, 2, 5, 10, 20, 50, 100, 200};
    /*
     Hardware slot indices
     */
    static private final int HWSlot1 = 1;
    static private final int HWSlot2a = 2;
    static private final int HWSlot2b = 3;
    static private final int HWSlot10 = 4;
    static private final int HWSlot5 = 5;
    static private final int HWSlot20a = 6;
    static private final int HWSlot20b = 7;
    static private final int HWSlot100 = 8;
    static private final int HWSlot50 = 9;
    static private final int HWSlot200a = 10;
    static private final int HWSlot200b = 11;

    /**
     * Constructor. Stores port and baud rate
     * @param port COM port
     * @throws JposException If COM port is invalid
     */
    public SampleCoinDispenser(String port) throws JposException {
        super(port);
        coinDispenserInit(1);
        PhysicalDeviceDescription = "Coin Dispenser Simulator for TCP";
        PhysicalDeviceName = "Coin Dispenser Simulator";
        CapPowerReporting = JposConst.JPOS_PR_STANDARD;
        WaitObj = new SyncObject();
        WaitInitialized = null;
    }

    @Override
    public void checkProperties(JposEntry entry) throws JposException {
        super.checkProperties(entry);
        try {
            Object o;
            if ((o = entry.getPropertyValue("Port")) != null)
                Port = o.toString();
            if ((o = entry.getPropertyValue("ClientPort")) != null)
                OwnPort = Integer.parseInt(o.toString());
            if ((o = entry.getPropertyValue("RequestTimeout")) != null)
                RequestTimeout = Integer.parseInt(o.toString());
            if ((o = entry.getPropertyValue("CharacterTimeout")) != null)
                CharacterTimeout = Integer.parseInt(o.toString());
            if ((o = entry.getPropertyValue("PollTimeout")) != null)
                PollTimeout = Integer.parseInt(o.toString());
            if ((o = entry.getPropertyValue("NearLimit")) != null)
                NearLimit = Integer.parseInt(o.toString());
            if ((o = entry.getPropertyValue("SlotCapacity")) != null)
                SlotCapacity = Integer.parseInt(o.toString());
            if ((o = entry.getPropertyValue("ReadArgumentCheck")) != null)
                ReadArgumentCheck = Boolean.parseBoolean(o.toString());
            if ((o = entry.getPropertyValue("MinClaimTimeout")) != null)
                MinClaimTimeout = Integer.parseInt(o.toString());
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property", e);
        }
    }

    @Override
    public void changeDefaults(CoinDispenserProperties props) {
        props.DeviceServiceDescription = "CoinDispenser service for coin dispenser simulator";
        props.CapEmptySensor = true;
        props.CapJamSensor = true;
        props.CapNearEmptySensor = true;
        CapPowerReporting = JposConst.JPOS_PR_STANDARD;
        props.DeviceServiceVersion = 1014001;
    }

    /**
     * Thread main, used for status check loop while device is enabled.
     */
    public void run() {
        int offline = Offline;
        while (!ToBeFinished) {
            String[] response;
            synchronized (SlotCount) {
                try {
                    response = sendCommand("R");
                } catch (Exception e) {
                    response = null;
                }
                offline = handleStatusChanges(handleResponse(response), offline);
            }
            if (WaitInitialized != null) {
                SyncObject obj = WaitInitialized;
                WaitInitialized = null;
                obj.signal();
            }
            WaitObj.suspend(PollTimeout);
        }
    }

    private int handleStatusChanges(int dispenserState, int offline) {
        try {
            JposCommonProperties props = getClaimingInstance(ClaimedCoinDispenser, 0);
            if (props != null && (dispenserState != DispenserState || Offline != offline)) {
                if (offline != Offline && Offline == JposConst.JPOS_PS_ONLINE) {
                    handleEvent(new JposStatusUpdateEvent(props.EventSource, JposConst.JPOS_SUE_POWER_ONLINE));
                    offline = Offline;
                }
                if (dispenserState != DispenserState) {
                    DispenserState = dispenserState;
                    handleEvent(new CoinDispenserStatusUpdateEvent(props.EventSource, DispenserState));
                }
                if (offline != Offline) {
                    handleEvent(new JposStatusUpdateEvent(props.EventSource, JposConst.JPOS_SUE_POWER_OFF_OFFLINE));
                    offline = Offline;
                }
            }
        } catch (JposException e) {}
        return offline;
    }

    private int handleResponse(String[] response) {
        int state = CoinDispenserConst.COIN_STATUS_JAM;
        if (response != null && response.length == 12 && response[0].equals("OK")) {
            Offline = JposConst.JPOS_PS_ONLINE;
            state = CoinDispenserConst.COIN_STATUS_OK;
            SlotCount[Slot1] = HWSlotCount[HWSlot1] = Short.parseShort(response[HWSlot1]);
            SlotCount[Slot2] = (HWSlotCount[HWSlot2a] = Short.parseShort(response[HWSlot2a])) + (HWSlotCount[HWSlot2b] = Short.parseShort(response[HWSlot2b]));
            SlotCount[Slot5] = HWSlotCount[HWSlot5] = Short.parseShort(response[HWSlot5]);
            SlotCount[Slot10] = HWSlotCount[HWSlot10] = Short.parseShort(response[HWSlot10]);
            SlotCount[Slot20] = (HWSlotCount[HWSlot20a] = Short.parseShort(response[HWSlot20a])) + (HWSlotCount[HWSlot20b] = Short.parseShort(response[HWSlot20b]));
            SlotCount[Slot50] = HWSlotCount[HWSlot50] = Short.parseShort(response[HWSlot50]);
            SlotCount[Slot100] = HWSlotCount[HWSlot100] = Short.parseShort(response[HWSlot100]);
            SlotCount[Slot200] = (HWSlotCount[HWSlot200a] = Short.parseShort(response[HWSlot200a])) + (HWSlotCount[HWSlot200b] = Short.parseShort(response[HWSlot200b]));
            for (int i = HWSlotCount.length - 1; i > 0; --i) {
                if (HWSlotCount[i] == 0) {
                    state = CoinDispenserConst.COIN_STATUS_EMPTY;
                    break;
                }
                else if (HWSlotCount[i] <= NearLimit) {
                    state = CoinDispenserConst.COIN_STATUS_NEAREMPTY;
                }
            }
        }
        else if(response != null && response.length == 1 && response[0].equals("KO"))
            Offline = JposConst.JPOS_PS_ONLINE;
        else
            Offline = JposConst.JPOS_PS_OFF_OFFLINE;
        return state;
    }

    /**
     * Method to perform any command
     * @param command Command data to be sent
     * @return Array of string components of response, null in error case.
     */
    protected String[] sendCommand(String command) {
        if (OutStream == null) {
            JposException e = initPort();
            if (e != null) {
                return null;
            }
        }
        byte[] request = (command + "\n").getBytes();
        try {
            OutStream.write(request);
            byte[] response = new byte[MaxRespLen];
            byte[] nextbyte;
            int i;
            OutStream.setTimeout(RequestTimeout);
            for (i = 0; i < MaxRespLen && (nextbyte = OutStream.read(1)).length == 1; i++) {
                if ((response[i] = nextbyte[0]) == '\n')
                    break;
                OutStream.setTimeout(CharacterTimeout);
            }
            if (i < MaxRespLen && i >= 2 && response[i] == '\n') {
                String result = new String(response,0, i);
                return result.split(" ");
            }
            log(Level.TRACE, getClaimingInstance(ClaimedCoinDispenser, 0).LogicalName + ": No coin dispenser response");
        } catch (JposException e) {
            log(Level.TRACE, getClaimingInstance(ClaimedCoinDispenser, 0).LogicalName + ": IO error: " + e.getMessage());
        }
        JposException ee = closePort(false);
        InIOError = true;
        return null;
    }

    @Override
    public CoinDispenserProperties getCoinDispenserProperties(int index) {
        return new SampleCoinDispenserAccessor(index);
    }
    public class SampleCoinDispenserAccessor extends CoinDispenserProperties {
        SampleCoinDispenserAccessor(int index) {
            super(index);
        }

        @Override
        public void claim(int timeout) throws JposException {
            if (timeout < MinClaimTimeout)
                timeout = MinClaimTimeout;
            super.claim(timeout);
            SyncObject initsync = new SyncObject();
            ToBeFinished = false;
            WaitInitialized = initsync;
            (StateWatcher = new Thread(SampleCoinDispenser.this)).start();
            initsync.suspend(timeout);
            if (WaitInitialized != null || InIOError) {
                release();
                throw new JposException(JposConst.JPOS_E_NOHARDWARE, "No coin dispenser detected");
            }
        }

        @Override
        public void release() throws JposException {
            ToBeFinished = true;
            WaitObj.signal();
            while (ToBeFinished) {
                try {
                    StateWatcher.join();
                } catch (Exception e) {}
                break;
            }
            JposException e = closePort(true);
            Offline = JposConst.JPOS_PS_UNKNOWN;
            InIOError = false;
            super.release();
        }

        @Override
        public void deviceEnabled(boolean enable) throws JposException {
            super.deviceEnabled(enable);
            if (enable) {
                PowerState = InIOError ? JposConst.JPOS_PS_OFF_OFFLINE : JposConst.JPOS_PS_ONLINE;
                DispenserStatus = DispenserState;
            }
        }
        @Override
        public void adjustCashCounts(String cashCounts) throws JposException {
            // UPOS spec: Call will be ignored because sample coin dispenser can detect the exact amount of cash in it.
            super.adjustCashCounts(cashCounts);
        }

        @Override
        public void readCashCounts(String[] cashCounts, boolean[] discrepancy) throws JposException {
            super.readCashCounts(cashCounts, discrepancy);
            if (Offline != JposConst.JPOS_PS_ONLINE)
                throw new JposException(JposConst.JPOS_E_OFFLINE, "Coin dispenser not connected");
            if (DispenserState == CoinDispenserConst.COIN_STATUS_JAM)
                throw new JposException(JposConst.JPOS_E_FAILURE, "Coin dispenser not ready");
            if (!ReadArgumentCheck || cashCounts[0] == null)
                cashCounts[0] = "1:0,2:0,5:0,10:0,20:0,50:0,100:0,200:0";
            String[] counts = cashCounts[0].split(",");
            String res = null;
            int[][] values = new int[counts.length][];
            for (int i = counts.length - 1; i >= 0; --i) {
                String[] valueStrings = counts[i].split(":");
                try {
                    values[i] = new int[]{Integer.parseInt(valueStrings[0]), -1};
                }
                catch (NumberFormatException e) {
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Bad cash count format");
                }
                int j;
                for (j = SlotCoinValues.length - 1; j >= 0; --j) {
                    if (SlotCoinValues[j] == values[i][0]) {
                        values[i][1] = SlotCount[j];
                        break;
                    }
                }
                check(values[i][1] < 0, JposConst.JPOS_E_ILLEGAL, "Invalid coin value");
                res = valueStrings[0] + (res == null ? ":" + values[i][1] : ":" + values[i][1] + "," + res);
            }
            discrepancy[0] = checkDiscrepancy(values);
            cashCounts[0] = res;
        }

        private boolean checkDiscrepancy(int[][] values) {
            int sum1 = 0, sum2 = 0;
            for (int[] value : values)
                sum1 += value[1];
            for (int value : SlotCount)
                sum2 += value;
            return sum1 != sum2;
        }

        @Override
        public void dispenseChange(int amount) throws JposException {
            check (amount > 610, JposConst.JPOS_E_ILLEGAL, "Amount too big");
            int val;
            int[] array = new int[HWSlotCount.length];
            if ((val = amount / 200) == 2)
                array[HWSlot200a] = array[HWSlot200b] = 1;
            else if (val == 1)
                array[HWSlotCount[HWSlot200a] > HWSlotCount[HWSlot200b] ? HWSlot200a : HWSlot200b] = 1;
            array[HWSlot100] = (amount %= 200) / 100;
            array[HWSlot50] = (amount %= 100) / 50;
            if ((val = (amount %= 50) / 20) == 2)
                array[HWSlot20a] = array[HWSlot20b] = 1;
            else if (val == 1)
                array[HWSlotCount[HWSlot20a] > HWSlotCount[HWSlot20b] ? HWSlot20a : HWSlot20b] = 1;
            array[HWSlot10] = (amount %= 20) / 10;
            array[HWSlot5] = (amount %= 10) / 5;
            if ((val = (amount %= 5) / 2) == 2)
                array[HWSlot2a] = array[HWSlot2b] = 1;
            else if (val == 1)
                array[HWSlotCount[HWSlot2a] > HWSlotCount[HWSlot2b] ? HWSlot2a : HWSlot2b] = 1;
            array[HWSlot1] = amount % 2;
            String command = "";
            for (int i = 1; i < array.length; i++) {
                command += array[i] > 0 ? " 1" : " 0";
            }
            int result;
            synchronized (SlotCount) {
                int offline = Offline;
                handleStatusChanges(result = handleResponse(sendCommand("O" + command)), offline);
            }
            if (result == CoinDispenserConst.COIN_STATUS_JAM) {
                if (Offline == JposConst.JPOS_PS_ONLINE)
                    throw new JposException(JposConst.JPOS_E_FAILURE, "Coin dispenser error, check slots");
                else
                    throw new JposException(JposConst.JPOS_E_OFFLINE, "Coin dispenser offline");
            }
        }

        @Override
        public void checkHealth(int level) throws JposException {
            String healthError = "";

            if (level == JposConst.JPOS_CH_INTERACTIVE) {
                synchronizedMessageBox("Press OK to start health test.", "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
            }
            if (level != JposConst.JPOS_CH_INTERNAL) {
                healthError = (level == JposConst.JPOS_CH_INTERACTIVE) ? "Interactive CheckHealth: " : "External CheckHealth: ";
                String result = "";
                try {
                    ((CoinDispenserService) EventSource).dispenseChange(499);
                    result = "OK";
                    if (DispenserStatus != CoinDispenserConst.COIN_STATUS_OK) {
                        result = DispenserStatus == CoinDispenserConst.COIN_STATUS_NEAREMPTY ? "Nearly Empty" : "Empty";
                        healthError += result + ": ";
                    }
                } catch (JposException e) {
                    result = Offline != JposConst.JPOS_PS_ONLINE ? "Offline" : (DispenserState == CoinDispenserConst.COIN_STATUS_JAM ? "Jam" : "Missing Coins");
                    healthError += " CheckHealth: " + result + ": ";
                }
                if (level == JposConst.JPOS_CH_INTERACTIVE) {
                    synchronizedMessageBox("Interactive check health result: " + result, "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
                }
            } else
                healthError = "Internal CheckHealth: ";
            CheckHealthText = healthError + (CheckHealthText.matches(".*Fail.*") ? "ERROR." : "OK.");
            EventSource.logSet("CheckHealthText");
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

}
