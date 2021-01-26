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
import jpos.CashChangerConst;
import jpos.JposConst;
import jpos.JposException;
import javax.swing.*;
import java.util.Arrays;

/**
 * Class implementing the CashChanger Interface for the sample udp device.
 */
class SampleUdpDeviceCashChangerProperties extends CashChangerProperties {
    private Device Dev;

    private String getCashSlotList(int index) {
        String[] typeList = {"", ""};
        for (int i = 0; i < Dev.CashMinBillIndex; i++) {
            typeList[0] += "," + Dev.CashInitialSlots[i][0];
        }
        if (index == 2)
            return typeList[0].substring(1);
        for (int i = Dev.CashMinBillIndex; i < Dev.CashInitialSlots.length; i++) {
            typeList[1] += "," + Dev.CashInitialSlots[i][0];
        }
        if (index == 3)
            return ";" + typeList[1].substring(1);
        else if (index == 1)
            return typeList[0].substring(1) + ";" + typeList[1].substring(1);
        return "";
    }

    protected SampleUdpDeviceCashChangerProperties(Device dev) {
        super(0);
        Dev = dev;
    }

    @Override
    public void initOnEnable(boolean enable) {
        if (enable) {
            synchronized (Dev.CashSlots) {
                char[] state = Dev.getCashStates();
                char opstate = state[Dev.CashOperationState];
                char lowstate = state[Dev.CashEmptyState];
                char histate = state[Dev.CashFullState];
                if (Dev.Offline) {
                    DepositStatusDef = CashChangerConst.CHAN_STATUS_DEPOSIT_JAM;
                    DeviceStatusDef = CashChangerConst.CHAN_STATUS_JAM;
                    FullStatusDef = CashChangerConst.CHAN_STATUS_FULL;
                } else {
                    FullStatusDef = histate == Dev.CashFull ? CashChangerConst.CHAN_STATUS_FULL
                            : (histate == Dev.CashNearlyFull ? CashChangerConst.CHAN_STATUS_NEARFULL : CashChangerConst.CHAN_STATUS_OK);
                    if (opstate > Dev.CashFinishInput) {
                        DeviceStatusDef = CashChangerConst.CHAN_STATUS_JAM;
                        DepositStatusDef = CashChangerConst.CHAN_STATUS_DEPOSIT_END;
                    }
                    else{
                        DeviceStatusDef = lowstate == Dev.CashEmpty ? CashChangerConst.CHAN_STATUS_EMPTY
                                : (lowstate == Dev.CashNearEmpty ? CashChangerConst.CHAN_STATUS_NEAREMPTY : CashChangerConst.CHAN_STATUS_OK);
                        DepositStatusDef = opstate == Dev.CashIdle ? CashChangerConst.CHAN_STATUS_DEPOSIT_END
                                : (opstate == Dev.CashInput ? CashChangerConst.CHAN_STATUS_DEPOSIT_START : CashChangerConst.CHAN_STATUS_DEPOSIT_COUNT);
                    }
                }
            }
        }
        super.initOnEnable(enable);
    }

    @Override
    public void handlePowerStateOnEnable() throws JposException {
        Device.handleEvent(new JposStatusUpdateEvent(EventSource, Dev.Offline ? JposConst.JPOS_SUE_POWER_OFF_OFFLINE : JposConst.JPOS_SUE_POWER_ONLINE));
    }

    @Override
    public void claim(int timeout) throws JposException {
        synchronized (Dev.CashInstances) {
            Dev.check(Dev.CashInstances[Dev.CashAcceptInstance] != null &&
                            !(Dev.CashInstances[Dev.CashAcceptInstance] instanceof SampleUdpDeviceCashChangerProperties),
                    JposConst.JPOS_E_CLAIMED, "Device claimed by other cash accepting instance");
            Dev.check(Dev.CashInstances[Dev.CashCoinInstance] != null &&
                            !(Dev.CashInstances[Dev.CashCoinInstance] instanceof SampleUdpDeviceCashChangerProperties),
                    JposConst.JPOS_E_CLAIMED, "Device claimed by other coin dispensing instance");
            Dev.check(Dev.CashInstances[Dev.CashBillInstance] != null &&
                            !(Dev.CashInstances[Dev.CashBillInstance] instanceof SampleUdpDeviceCashChangerProperties),
                    JposConst.JPOS_E_CLAIMED, "Device claimed by other bill dispensing instance");
            Dev.CashInstances[Dev.CashAcceptInstance]
                    = Dev.CashInstances[Dev.CashCoinInstance]
                    = Dev.CashInstances[Dev.CashBillInstance] = this;
        }
        Dev.startPolling(this);
        if (Dev.Offline && PowerNotify == JposConst.JPOS_PN_DISABLED) {
            Dev.stopPolling();
            synchronized (Dev.CashInstances) {
                Dev.CashInstances[Dev.CashAcceptInstance]
                        = Dev.CashInstances[Dev.CashCoinInstance]
                        = Dev.CashInstances[Dev.CashBillInstance] = null;
            }
            throw new JposException(JposConst.JPOS_E_OFFLINE, "Communication with device disrupted");
        }
        super.claim(timeout);
    }

    @Override
    public void release() throws JposException {
        super.release();
        Dev.stopPolling();
        synchronized (Dev.CashInstances) {
            Dev.CashInstances[Dev.CashAcceptInstance]
                    = Dev.CashInstances[Dev.CashCoinInstance]
                    = Dev.CashInstances[Dev.CashBillInstance] = null;
        }
    }

    @Override
    public void checkHealth(int level) throws JposException {
        String how = level == JposConst.JPOS_CH_INTERNAL ? "Internal" : (level == JposConst.JPOS_CH_EXTERNAL ? "External" : "Interactive");
        if (Dev.Offline)
            CheckHealthText = how + " Checkhealth: Offline";
        else {
            CheckHealthText = how + " Checkhealth: OK";
            if (level != JposConst.JPOS_CH_INTERNAL) {
                boolean interactive;
                if (interactive = (level == JposConst.JPOS_CH_INTERACTIVE))
                    Dev.synchronizedMessageBox("Press OK to start health test.", "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
                try {
                    String[] counts = {""};
                    boolean[] diff = {false};
                    readCashCounts(counts, diff);
                } catch (JposException e) {
                    CheckHealthText = how + "Checkhealth: Error: " + e.getMessage();
                }
                if (interactive)
                    Dev.synchronizedMessageBox("CheckHealth result:\n" + CheckHealthText, "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    @Override
    public void adjustCashCounts(String cashCounts) throws JposException {
        synchronized (Dev.CashSlots) {
            Dev.check(Dev.CashDepositStartSlots != null, JposConst.JPOS_E_ILLEGAL, "Cash acceptance in progress");
        }
        int[][] slots = Dev.cashCounts2ints(cashCounts, 0);
        boolean doit = false;
        synchronized (Dev.CashSlots) {
            for (int i = 0; i < slots.length; i++) {
                if ((slots[i][1] -= Dev.CashSlots[i][1]) != 0)
                    doit = true;
            }
        }
        if (doit) {
            String list = "";
            for (int[] pair : slots) {
                if (pair[1] != 0)
                    list += " " + pair[0] + " " + pair[1];
            }
            String result = Dev.sendResp("CASHBOX:AddSlots" + list.substring(1));
            Dev.check(result == null || Dev.Offline, JposConst.JPOS_E_FAILURE, "Communication error");
        }
    }

    @Override
    public void beginDeposit() throws JposException {
        Dev.check(DepositStatus != CashChangerConst.CHAN_STATUS_DEPOSIT_END, JposConst.JPOS_E_ILLEGAL, "Bad deposit state");
        attachWaiter();
        String[] result = Dev.sendResp(new String[]{"CASHBOX:StartInput3", "CASHBOX:GetSlots"});
        if (result != null) {
            Dev.PollWaiter.signal();
            waitWaiter(Dev.RequestTimeout * Dev.MaxRetry);
        }
        releaseWaiter();
        Dev.check(result == null, JposConst.JPOS_E_OFFLINE, "Could not start deposit operation");
        Object[] depositcoins;
        Object[] depositbills;
        synchronized (Dev.CashSlots) {
            Dev.CashDepositStartSlots = Dev.string2Slots(result[1]);
            int[][] delta = Dev.depositDelta();
            depositcoins = Dev.getCountsAmount(delta, 0, Dev.CashMinBillIndex);
            depositbills = Dev.getCountsAmount(delta, Dev.CashMinBillIndex, delta.length);
        }
        synchronized (Dev.CashDepositSync) {
            Dev.check(DepositStatus == CashChangerConst.CHAN_STATUS_DEPOSIT_JAM, JposConst.JPOS_E_FAILURE, "JAM condition");
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
        Dev.check(depositstr == null, JposConst.JPOS_E_FAILURE, "Cannot stop cash input");
        Dev.PollWaiter.signal();
        waitWaiter(Dev.RequestTimeout * Dev.MaxRetry);
        releaseWaiter();
        Object[] depositcoins;
        Object[] depositbills;
        synchronized (Dev.CashSlots) {
            int[][] delta = Dev.depositDelta();
            depositcoins = Dev.getCountsAmount(delta, 0, Dev.CashMinBillIndex);
            depositbills = Dev.getCountsAmount(delta, Dev.CashMinBillIndex, delta.length);
            Dev.check(Integer.parseInt(depositstr) != (Integer) depositcoins[0] + (Integer) depositbills[0], JposConst.JPOS_E_FAILURE, "Deposit amount mismatch");
        }
        synchronized (Dev.CashDepositSync) {
            Dev.CashCreateDataEvents = false;
            DepositAmount = (Integer) depositcoins[0] + (Integer) depositbills[0];
            DepositCounts = (String) depositcoins[1] + (String) depositbills[1];
            super.fixDeposit();
        }
    }

    @Override
    public void clearInput() throws JposException {
        Dev.cancelCashInput(Dev.getCashStates()[Dev.CashOperationState]);
        super.clearInput();
        Object[] depositcoins;
        Object[] depositbills;
        synchronized (Dev.CashSlots) {
            int[][] delta = Dev.depositDelta();
            if (delta == null)
                delta = Dev.CashInitialSlots;
            depositcoins = Dev.getCountsAmount(delta, 0, Dev.CashMinBillIndex);
            depositbills = Dev.getCountsAmount(delta, Dev.CashMinBillIndex, delta.length);
            Dev.CashDepositStartSlots = null;
        }
        synchronized (Dev.CashDepositSync) {
            DepositAmount = (Integer) depositcoins[0] + (Integer) depositbills[0];
            DepositCounts = (String) depositcoins[1] + (String) depositbills[1];
            if (DepositStatus != CashChangerConst.CHAN_STATUS_DEPOSIT_JAM)
                DepositStatus = CashChangerConst.CHAN_STATUS_DEPOSIT_END;
        }
    }

    @Override
    public void endDeposit(int success) throws JposException {
        if (success == CashChangerConst.CHAN_DEPOSIT_REPAY) {
            clearInput();
            return;
        }
        String depositstr = Dev.sendResp("CASHBOX:EndInput");
        synchronized (Dev.CashDepositSync) {
            Dev.check(depositstr == null, JposConst.JPOS_E_FAILURE, "Deposit end failure");
        }
        Dev.check(Integer.parseInt(depositstr) != DepositAmount, JposConst.JPOS_E_FAILURE, "Deposit amount mismatch");
        synchronized (Dev.CashSlots) {
            Dev.CashDepositStartSlots = null;
        }
        synchronized (Dev.CashDepositSync) {
            if (DepositStatus != CashChangerConst.CHAN_STATUS_DEPOSIT_JAM)
                super.endDeposit(success);
        }
    }

    @Override
    public void readCashCounts(String[] cashCounts, boolean[] discrepancy) throws JposException {
        Dev.check(Dev.Offline, JposConst.JPOS_E_OFFLINE, "Device is offline");
        attachWaiter();
        Dev.PollWaiter.signal();
        waitWaiter(Dev.RequestTimeout * Dev.MaxRetry);
        releaseWaiter();
        Dev.check(Dev.Offline, JposConst.JPOS_E_OFFLINE, "Device is offline");
        cashCounts[0] = "";
        int[][] slots;
        synchronized (Dev.CashSlots) {
            slots = Dev.CashDepositStartSlots == null ? Dev.CashSlots : Dev.CashDepositStartSlots;
            cashCounts[0] = (String) (Dev.getCountsAmount(slots, 0, Dev.CashMinBillIndex)[1])
                    + (String) (Dev.getCountsAmount(slots, Dev.CashMinBillIndex, slots.length)[1]);
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
            Dev.check(Dev.CashDepositStartSlots != null, JposConst.JPOS_E_ILLEGAL, "Cash acceptance in progress");
            currentslots = Dev.copySlots(Dev.CashSlots);
        }
        synchronized (Dev.CashDepositSync) {
            statuses = new char[][]{
                    Dev.getCashStates(),
                    Arrays.copyOf(Dev.CashCoinState, Dev.CashCoinState.length),
                    Arrays.copyOf(Dev.CashBillState, Dev.CashBillState.length)
            };
        }
        Dev.check(statuses[request.getCurrentExit() - 1][Dev.CashOperationState] != Dev.CashIdle, JposConst.JPOS_E_FAILURE, "CashChanger not operational");
        int[][] dispenseSlots = (int[][]) request.AdditionalData;
        for (int i = dispenseSlots.length - 1; i >= 0; --i) {
            Dev.checkext(currentslots[i][1] < dispenseSlots[i][1], CashChangerConst.JPOS_ECHAN_OVERDISPENSE, "Not enough cash units " + currentslots[i][0]);
        }
        boolean again;
        do {
            String command = "";
            again = false;
            for (int i = 0; i < dispenseSlots.length; i++) {
                if (dispenseSlots[i][1] > 0) {
                    int max = dispenseSlots[i][1];
                    if (i != dispenseSlots.length - 1 && i != Dev.CashMinBillIndex - 1) {
                        max = (dispenseSlots[i + 1][0] - 1) / dispenseSlots[i][0];
                        if (max > dispenseSlots[i][1])
                            max = dispenseSlots[i][1];
                    }
                    command += ",CASHBOX:Output" + (i < Dev.CashMinBillIndex ? "C" : "B") + dispenseSlots[i][0] * max;
                    if ((dispenseSlots[i][1] -= max) > 0)
                        again = true;
                }
            }
            if (command.length() > 0) {
                String[] result = Dev.sendResp(command.substring(1).split(","));
                Dev.check(result == null || Dev.Offline, JposConst.JPOS_E_FAILURE, "Dispense failure");
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
            Dev.check(Dev.CashDepositStartSlots != null, JposConst.JPOS_E_ILLEGAL, "Cash acceptance in progress");
            statuses = new char[][]{
                    Dev.getCashStates(),
                    Arrays.copyOf(Dev.CashCoinState, Dev.CashCoinState.length),
                    Arrays.copyOf(Dev.CashBillState, Dev.CashBillState.length)
            };
            currentslots = Dev.copySlots(Dev.CashSlots);
        }
        int exitindex = request.getCurrentExit() - 1;
        Dev.check(statuses[exitindex][Dev.CashOperationState] != Dev.CashIdle, JposConst.JPOS_E_FAILURE, "CashChanger not operational");
        int amount = request.getAmount();
        int[][] boundaries = {{0, currentslots.length}, {0, Dev.CashMinBillIndex}, {Dev.CashMinBillIndex, currentslots.length}};
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
        Dev.check(amount > 0, JposConst.JPOS_E_ILLEGAL, "Cannot dispense " + request.getAmount() + " with " + what[exitindex]);
        String dispensed = Dev.sendResp("CASHBOX:Output" + command[exitindex] + request.getAmount());
        Dev.check(dispensed == null || Dev.Offline, JposConst.JPOS_E_FAILURE, "Dispenser communication error");
        Dev.checkext(Integer.parseInt(dispensed) != request.getAmount(), CashChangerConst.JPOS_ECHAN_OVERDISPENSE,
                "Dispenser difference: " + (request.getAmount() - Integer.parseInt(dispensed)));
    }
}
