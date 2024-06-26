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
import de.gmxhome.conrad.jpos.jpos_base.cashchanger.*;
import jpos.*;
import java.util.Arrays;

import static SampleUdpDevice.BeltCashboxDrawer.*;
import static javax.swing.JOptionPane.*;
import static jpos.CashChangerConst.*;
import static jpos.JposConst.*;

/**
 * Class implementing the CashChanger Interface for the sample udp device.
 */
class SampleUdpDeviceCashChangerProperties extends CashChangerProperties {
    private final BeltCashboxDrawer Dev;

    private String getCashSlotList(int index) {
        String[] typeList = {"", ""};
        for (int i = 0; i < CashMinBillIndex; i++) {
            typeList[0] += "," + CashInitialSlots[i][0];
        }
        if (index == 2)
            return typeList[0].substring(1);
        for (int i = CashMinBillIndex; i < CashInitialSlots.length; i++) {
            typeList[1] += "," + CashInitialSlots[i][0];
        }
        if (index == 3)
            return ";" + typeList[1].substring(1);
        else if (index == 1)
            return typeList[0].substring(1) + ";" + typeList[1].substring(1);
        return "";
    }

    /**
     * Constructor. Gets instance of Device to be used as communication object. Device index for
     * sample is always 0.
     * @param dev Instance of Device this object belongs to.
     */
    protected SampleUdpDeviceCashChangerProperties(BeltCashboxDrawer dev) {
        super(0);
        Dev = dev;
    }

    @Override
    public void initOnEnable(boolean enable) {
        if (enable) {
            synchronized (Dev.CashSlots) {
                char[] state = Dev.getCashStates();
                char opstate = state[CashOperationState];
                char lowstate = state[CashEmptyState];
                char histate = state[CashFullState];
                if (Dev.Offline) {
                    DepositStatusDef = CHAN_STATUS_DEPOSIT_JAM;
                    DeviceStatusDef = CHAN_STATUS_JAM;
                    FullStatusDef = CHAN_STATUS_FULL;
                } else {
                    FullStatusDef = histate == CashFull ? CHAN_STATUS_FULL
                            : (histate == CashNearlyFull ? CHAN_STATUS_NEARFULL : CHAN_STATUS_OK);
                    if (opstate > CashFinishInput) {
                        DeviceStatusDef = CHAN_STATUS_JAM;
                        DepositStatusDef = CHAN_STATUS_DEPOSIT_END;
                    }
                    else{
                        DeviceStatusDef = lowstate == CashEmpty ? CHAN_STATUS_EMPTY
                                : (lowstate == CashNearEmpty ? CHAN_STATUS_NEAREMPTY : CHAN_STATUS_OK);
                        DepositStatusDef = opstate == CashIdle ? CHAN_STATUS_DEPOSIT_END
                                : (opstate == CashInput ? CHAN_STATUS_DEPOSIT_START : CHAN_STATUS_DEPOSIT_COUNT);
                    }
                }
            }
        }
        super.initOnEnable(enable);
    }

    @Override
    public void handlePowerStateOnEnable() throws JposException {
        Device.handleEvent(new JposStatusUpdateEvent(EventSource, Dev.Offline ? JPOS_SUE_POWER_OFF_OFFLINE : JPOS_SUE_POWER_ONLINE));
    }

    @Override
    public void claim(int timeout) throws JposException {
        synchronized (Dev.CashInstances) {
            check(Dev.CashInstances[CashAcceptInstance] != null &&
                            !(Dev.CashInstances[CashAcceptInstance] instanceof SampleUdpDeviceCashChangerProperties),
                    JPOS_E_CLAIMED, "Device claimed by other cash accepting instance");
            check(Dev.CashInstances[CashCoinInstance] != null &&
                            !(Dev.CashInstances[CashCoinInstance] instanceof SampleUdpDeviceCashChangerProperties),
                    JPOS_E_CLAIMED, "Device claimed by other coin dispensing instance");
            check(Dev.CashInstances[CashBillInstance] != null &&
                            !(Dev.CashInstances[CashBillInstance] instanceof SampleUdpDeviceCashChangerProperties),
                    JPOS_E_CLAIMED, "Device claimed by other bill dispensing instance");
            Dev.CashInstances[CashAcceptInstance]
                    = Dev.CashInstances[CashCoinInstance]
                    = Dev.CashInstances[CashBillInstance] = this;
        }
        Dev.startPolling(this);
        if (Dev.Offline && PowerNotify == JPOS_PN_DISABLED) {
            Dev.stopPolling();
            synchronized (Dev.CashInstances) {
                Dev.CashInstances[CashAcceptInstance]
                        = Dev.CashInstances[CashCoinInstance]
                        = Dev.CashInstances[CashBillInstance] = null;
            }
            throw new JposException(JPOS_E_OFFLINE, "Communication with device disrupted");
        }
        super.claim(timeout);
    }

    @Override
    public void release() throws JposException {
        super.release();
        Dev.stopPolling();
        synchronized (Dev.CashInstances) {
            Dev.CashInstances[CashAcceptInstance]
                    = Dev.CashInstances[CashCoinInstance]
                    = Dev.CashInstances[CashBillInstance] = null;
        }
    }

    @Override
    @SuppressWarnings("AssignmentUsedAsCondition")
    public void checkHealth(int level) throws JposException {
        String how = level == JPOS_CH_INTERNAL ? "Internal" : (level == JPOS_CH_EXTERNAL ? "External" : "Interactive");
        if (Dev.Offline)
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
        synchronized (Dev.CashSlots) {
            check(Dev.CashDepositStartSlots != null, JPOS_E_ILLEGAL, "Cash acceptance in progress");
        }
        int[][] slots = Dev.cashCounts2ints(cashCounts, 0);
        boolean doit = false;
        synchronized (Dev.CashSlots) {
            for (int i = 0; i < slots.length; i++) {
                if ((slots[i][1] -= Dev.CashSlots[0][i][1]) != 0)
                    doit = true;
            }
        }
        if (doit) {
            StringBuilder list = new StringBuilder();
            for (int[] pair : slots) {
                if (pair[1] != 0)
                    list.append(" ").append(pair[0]).append(" ").append(pair[1]);
            }
            String result = Dev.sendResp("CASHBOX:AddSlots" + list.substring(1));
            check(result == null || Dev.Offline, JPOS_E_FAILURE, "Communication error");
        }
    }

    @Override
    public void beginDeposit() throws JposException {
        check(DepositStatus != CHAN_STATUS_DEPOSIT_END, JPOS_E_ILLEGAL, "Bad deposit state");
        attachWaiter();
        String[] result = Dev.sendResp(new String[]{"CASHBOX:StartInput3", "CASHBOX:GetSlots"});
        if (result != null) {
            Dev.PollWaiter.signal();
            waitWaiter((long)Dev.RequestTimeout * Dev.MaxRetry);
        }
        releaseWaiter();
        check(result == null, JPOS_E_OFFLINE, "Could not start deposit operation");
        Object[] depositcoins;
        Object[] depositbills;
        synchronized (Dev.CashSlots) {
            Dev.CashDepositStartSlots = Dev.string2Slots(result[1]);
            int[][] delta = Dev.depositDelta();
            depositcoins = Dev.getCountsAmount(delta, 0, CashMinBillIndex);
            depositbills = Dev.getCountsAmount(delta, CashMinBillIndex, delta.length);
        }
        synchronized (Dev.CashDepositSync) {
            check(DepositStatus == CHAN_STATUS_DEPOSIT_JAM, JPOS_E_FAILURE, "JAM condition");
            DepositAmount = (Integer) depositcoins[0] + (Integer) depositbills[0];
            DepositCounts = (String) depositcoins[1] + (String) depositbills[1];
            super.beginDeposit();
            Dev.CashCreateDataEvents = RealTimeDataEnabled;
        }
    }

    @Override
    public void fixDeposit() throws JposException {
        attachWaiter();
        String depositstr = Dev.sendResp("CASHBOX:StopInput");
        check(depositstr == null, JPOS_E_FAILURE, "Cannot stop cash input");
        Dev.PollWaiter.signal();
        waitWaiter((long)Dev.RequestTimeout * Dev.MaxRetry);
        releaseWaiter();
        Object[] depositcoins;
        Object[] depositbills;
        synchronized (Dev.CashSlots) {
            int[][] delta = Dev.depositDelta();
            depositcoins = Dev.getCountsAmount(delta, 0, CashMinBillIndex);
            depositbills = Dev.getCountsAmount(delta, CashMinBillIndex, delta.length);
            check(Integer.parseInt(depositstr) != (Integer) depositcoins[0] + (Integer) depositbills[0], JPOS_E_FAILURE, "Deposit amount mismatch");
        }
        synchronized (Dev.CashDepositSync) {
            Dev.CashCreateDataEvents = false;
            DepositAmount = (Integer) depositcoins[0] + (Integer) depositbills[0];
            DepositCounts = depositcoins[1] + (String) depositbills[1];
            super.fixDeposit();
        }
    }

    @Override
    public void clearInput() throws JposException {
        Dev.cancelCashInput(Dev.getCashStates()[CashOperationState]);
        super.clearInput();
        Object[] depositcoins;
        Object[] depositbills;
        synchronized (Dev.CashSlots) {
            int[][] delta = Dev.depositDelta();
            if (delta == null)
                delta = CashInitialSlots;
            depositcoins = Dev.getCountsAmount(delta, 0, CashMinBillIndex);
            depositbills = Dev.getCountsAmount(delta, CashMinBillIndex, delta.length);
            Dev.CashDepositStartSlots = null;
        }
        synchronized (Dev.CashDepositSync) {
            DepositAmount = (Integer) depositcoins[0] + (Integer) depositbills[0];
            DepositCounts = (String) depositcoins[1] + (String) depositbills[1];
            if (DepositStatus != CHAN_STATUS_DEPOSIT_JAM)
                DepositStatus = CHAN_STATUS_DEPOSIT_END;
        }
    }

    @Override
    public void endDeposit(int success) throws JposException {
        if (success == CHAN_DEPOSIT_REPAY) {
            clearInput();
            return;
        }
        String depositstr = Dev.sendResp("CASHBOX:EndInput");
        synchronized (Dev.CashDepositSync) {
            check(depositstr == null, JPOS_E_FAILURE, "Deposit end failure");
        }
        check(Integer.parseInt(depositstr) != DepositAmount, JPOS_E_FAILURE, "Deposit amount mismatch");
        synchronized (Dev.CashSlots) {
            Dev.CashDepositStartSlots = null;
        }
        synchronized (Dev.CashDepositSync) {
            if (DepositStatus != CHAN_STATUS_DEPOSIT_JAM)
                super.endDeposit(success);
        }
    }

    @Override
    public void readCashCounts(String[] cashCounts, boolean[] discrepancy) throws JposException {
        check(Dev.Offline, JPOS_E_OFFLINE, "Device is offline");
        attachWaiter();
        Dev.PollWaiter.signal();
        waitWaiter((long)Dev.RequestTimeout * Dev.MaxRetry);
        releaseWaiter();
        check(Dev.Offline, JPOS_E_OFFLINE, "Device is offline");
        cashCounts[0] = "";
        int[][] slots;
        synchronized (Dev.CashSlots) {
            slots = Dev.CashDepositStartSlots == null ? Dev.CashSlots[0] : Dev.CashDepositStartSlots;
            cashCounts[0] = (String) (Dev.getCountsAmount(slots, 0, CashMinBillIndex)[1])
                    + (String) (Dev.getCountsAmount(slots, CashMinBillIndex, slots.length)[1]);
        }
        discrepancy[0] = false;
    }

    @Override
    public void currentExit(int exit) throws JposException {
        synchronized (Dev.CashSlots) {
            super.currentExit(exit);
            ExitCashList = getCashSlotList(exit);
        }
    }

    @Override
    public DispenseCash dispenseCash(String cashCounts) throws JposException {
        DispenseCash ret = super.dispenseCash(cashCounts);
        synchronized (Dev.CashSlots) {
            ret.AdditionalData = Dev.cashCounts2ints(cashCounts, CurrentExit - 1);
        }
        return ret;
    }

    @Override
    public void dispenseCash(DispenseCash request) throws JposException {
        char[][] statuses;
        int[][] currentslots;
        synchronized (Dev.CashSlots) {
            check(Dev.CashDepositStartSlots != null, JPOS_E_ILLEGAL, "Cash acceptance in progress");
            currentslots = Dev.copySlots(Dev.CashSlots[0]);
        }
        synchronized (Dev.CashDepositSync) {
            statuses = new char[][]{
                    Dev.getCashStates(),
                    Arrays.copyOf(Dev.CashCoinState, Dev.CashCoinState.length),
                    Arrays.copyOf(Dev.CashBillState, Dev.CashBillState.length)
            };
        }
        check(statuses[request.getCurrentExit() - 1][CashOperationState] != CashIdle, JPOS_E_FAILURE, "CashChanger not operational");
        int[][] dispenseSlots = (int[][]) request.AdditionalData;
        for (int i = dispenseSlots.length - 1; i >= 0; --i) {
            checkext(currentslots[i][1] < dispenseSlots[i][1], JPOS_ECHAN_OVERDISPENSE, "Not enough cash units " + currentslots[i][0]);
        }
        boolean again;
        do {
            StringBuilder command = new StringBuilder();
            again = false;
            for (int i = 0; i < dispenseSlots.length; i++) {
                if (dispenseSlots[i][1] > 0) {
                    int max = dispenseSlots[i][1];
                    if (i != dispenseSlots.length - 1 && i != CashMinBillIndex - 1) {
                        max = (dispenseSlots[i + 1][0] - 1) / dispenseSlots[i][0];
                        if (max > dispenseSlots[i][1])
                            max = dispenseSlots[i][1];
                    }
                    command.append(",CASHBOX:Output").append(i < CashMinBillIndex ? "C" : "B").append(dispenseSlots[i][0] * max);
                    if ((dispenseSlots[i][1] -= max) > 0)
                        again = true;
                }
            }
            if (command.length() > 0) {
                String[] result = Dev.sendResp(command.substring(1).split(","));
                check(result == null || Dev.Offline, JPOS_E_FAILURE, "Dispense failure");
            }
        } while (again);
    }

    @Override
    public DispenseChange dispenseChange(int amount) throws JposException {
        return super.dispenseChange(amount);
    }

    @Override
    public void dispenseChange(DispenseChange request) throws JposException {
        char[][] statuses;
        int[][] currentslots;
        synchronized (Dev.CashSlots) {
            check(Dev.CashDepositStartSlots != null, JPOS_E_ILLEGAL, "Cash acceptance in progress");
            statuses = new char[][]{
                    Dev.getCashStates(),
                    Arrays.copyOf(Dev.CashCoinState, Dev.CashCoinState.length),
                    Arrays.copyOf(Dev.CashBillState, Dev.CashBillState.length)
            };
            currentslots = Dev.copySlots(Dev.CashSlots[0]);
        }
        int exitindex = request.getCurrentExit() - 1;
        check(statuses[exitindex][CashOperationState] != CashIdle, JPOS_E_FAILURE, "CashChanger not operational");
        int amount = request.getAmount();
        int[][] boundaries = {{0, currentslots.length}, {0, CashMinBillIndex}, {CashMinBillIndex, currentslots.length}};
        int[] bounds = boundaries[exitindex];
        for (int i = bounds[1] - 1; i >= bounds[0]; --i) {
            if (currentslots[i][0] <= amount) {
                if (amount / currentslots[i][0] <= currentslots[i][1])
                    amount = amount % currentslots[i][0];
                else
                    amount -= currentslots[i][0] * currentslots[i][1];
            }
        }
        String[] what = {"cash", "coins", "bills"};
        String[] command = {"A", "C", "B"};
        check(amount > 0, JPOS_E_ILLEGAL, "Cannot dispense " + request.getAmount() + " with " + what[exitindex]);
        String dispensed = Dev.sendResp("CASHBOX:Output" + command[exitindex] + request.getAmount());
        check(dispensed == null || Dev.Offline, JPOS_E_FAILURE, "Dispenser communication error");
        checkext(Integer.parseInt(dispensed) != request.getAmount(), JPOS_ECHAN_OVERDISPENSE,
                "Dispenser difference: " + (request.getAmount() - Integer.parseInt(dispensed)));
    }
}
