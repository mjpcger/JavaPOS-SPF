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
import de.gmxhome.conrad.jpos.jpos_base.billdispenser.*;
import jpos.BillDispenser;
import jpos.BillDispenserConst;
import jpos.JposConst;
import jpos.JposException;

import javax.swing.*;
import java.util.Arrays;

/**
 * Class implementing the BillDispenser Interface for the sample udp device.
 */
class SampleUdpDeviceBillDispenserProperties extends BillDispenserProperties {
    private Device Dev;

    protected SampleUdpDeviceBillDispenserProperties(Device dev) {
        super(0);
        Dev = dev;
    }

    @Override
    public void handlePowerStateOnEnable() throws JposException {
        Dev.handleEvent(new JposStatusUpdateEvent(EventSource, Dev.Offline ? JposConst.JPOS_SUE_POWER_OFF_OFFLINE : JposConst.JPOS_SUE_POWER_ONLINE));
    }

    @Override
    public void initOnEnable(boolean enable) {
        if (enable) {
            synchronized (Dev.CashSlots) {
                char[] state = Dev.getCashStates();
                char opstate = state[Dev.CashOperationState];
                char lowstate = state[Dev.CashEmptyState];
                char histate = state[Dev.CashFullState];
                if (Dev.Offline || opstate == Dev.CashJam || opstate == Dev.CashOpened) {
                    DeviceStatusDef = BillDispenserConst.BDSP_STATUS_JAM;
                } else {
                    DeviceStatusDef = lowstate == Dev.CashEmpty ? BillDispenserConst.BDSP_STATUS_EMPTY
                            : (lowstate == Dev.CashNearEmpty ? BillDispenserConst.BDSP_STATUS_NEAREMPTY : BillDispenserConst.BDSP_STATUS_OK);
                }
            }
        }
        super.initOnEnable(enable);
    }

    @Override
    public void claim(int timeout) throws JposException {
        synchronized (Dev.CashInstances) {
            Dev.check(Dev.CashInstances[Dev.CashBillInstance] != null && !(Dev.CashInstances[Dev.CashBillInstance] instanceof SampleUdpDeviceCashChangerProperties),
                    JposConst.JPOS_E_CLAIMED, "Device claimed by other bill dispensing instance");
            Dev.CashInstances[Dev.CashBillInstance] = this;
        }
        Dev.startPolling(this);
        if (Dev.Offline && PowerNotify == JposConst.JPOS_PN_DISABLED) {
            Dev.stopPolling();
            synchronized (Dev.CashInstances) {
                Dev.CashInstances[Dev.CashBillInstance] = null;
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
            Dev.CashInstances[Dev.CashBillInstance] = null;
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
            cashCounts[0] = (String) (Dev.getCountsAmount(slots, Dev.CashMinBillIndex, slots.length)[1]);
        }
        discrepancy[0] = false;
    }

    @Override
    public DispenseCash dispenseCash(String cashCounts) throws JposException {
        String[] cashtypestr = cashCounts.split(";");
        int[][] dispenseSlots = Dev.copySlots(Dev.CashInitialSlots);
        if (cashtypestr[1].length() > 0) {
            String[] countsstr = cashtypestr[1].split(",");
            for (int i = 0; i < countsstr.length; i++) {
                String[] parts = countsstr[i].split(":");
                int value = Integer.parseInt(parts[0]);
                Dev.check(!Dev.CashSlotIndex.containsKey(value), JposConst.JPOS_E_ILLEGAL, "Invalid cashCount component: " + countsstr[i]);
                value = Dev.CashSlotIndex.get(value);
                Dev.check(value < Dev.CashMinBillIndex || dispenseSlots.length <= value, JposConst.JPOS_E_ILLEGAL,
                        "Invalid cashCount type component: " + countsstr[i]);
                dispenseSlots[value][1] += Integer.parseInt(parts[1]);
            }
        }
        DispenseCash ret = super.dispenseCash(cashCounts);
        ret.AdditionalData = dispenseSlots;
        return ret;
    }

    @Override
    public void dispenseCash(DispenseCash request) throws JposException {
        char[] status;
        int[][] currentslots;
        synchronized(Dev.CashSlots) {
            Dev.check(Dev.CashDepositStartSlots != null, JposConst.JPOS_E_ILLEGAL, "Cash acceptance in progress");
            status = Arrays.copyOf(Dev.CashBillState, Dev.CashBillState.length);
            currentslots = Dev.copySlots(Dev.CashSlots);
        }
        Dev.check(status[Dev.CashOperationState] != Dev.CashIdle, JposConst.JPOS_E_FAILURE, "BillDispenser not operational");
        int[][] dispenseSlots = (int[][])request.AdditionalData;
        for (int i = 0; i < dispenseSlots.length; i++) {
            Dev.checkext(currentslots[i][1] < dispenseSlots[i][1], BillDispenserConst.JPOS_EBDSP_OVERDISPENSE, "Not enough cash units " + currentslots[i][0]);
        }
        boolean again;
        do {
            String command = "";
            again = false;
            for (int i = 0; i < dispenseSlots.length; i++) {
                if (dispenseSlots[i][1] > 0) {
                    command += ",CASHBOX:OutputB" + dispenseSlots[i][0];
                    if (--dispenseSlots[i][1] > 0)
                        again = true;
                }
            }
            if (command.length() > 0) {
                String[] result = Dev.sendResp(command.split(","));
                Dev.check(result == null || Dev.Offline, JposConst.JPOS_E_FAILURE, "Dispense failure");
            }
        } while (again);
    }
}
