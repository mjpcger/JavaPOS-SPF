/*
 * Copyright 2018 Martin Conrad
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

package SampleCoinDispenser;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.coindispenser.*;
import jpos.CoinDispenserConst;
import jpos.JposConst;
import jpos.JposException;

import javax.swing.*;

/**
 * Class implementing the CoinDispenserInterface for the sample coin dispenser. Since UPOS does not explicitly supports
 * multiple cash counts per denomination, this service does not support cash count adjustments. When reading cash counts,
 * counts for coins with the same value will be added. Inside dispense operation, the service prioritises the slots,
 * givin the slot with the higher count a higher priority.
 */
class CoinDispenser extends CoinDispenserProperties {
    private Device Dev;

    /**
     * Constructor. Gets instance of Device to be used as communication object. Device index for sample is
     * always 0.
     * @param dev   Instance of Device this object belongs to.
     */
    CoinDispenser(Device dev) {
        super(0);
        Dev = dev;
    }

    @Override
    public void claim(int timeout) throws JposException {
        if (timeout < Dev.MinClaimTimeout)
            timeout = Dev.MinClaimTimeout;
        super.claim(timeout);
        SyncObject initsync = new SyncObject();
        Dev.ToBeFinished = false;
        Dev.WaitInitialized = initsync;
        (Dev.StateWatcher = new Thread(Dev)).start();
        initsync.suspend(timeout);
        if (Dev.WaitInitialized != null || Dev.InIOError) {
            release();
            throw new JposException(JposConst.JPOS_E_NOHARDWARE, "No coin dispenser detected");
        }
    }

    @Override
    public void release() throws JposException {
        Dev.ToBeFinished = true;
        Dev.WaitObj.signal();
        while (Dev.ToBeFinished) {
            try {
                Dev.StateWatcher.join();
            } catch (Exception e) {}
            break;
        }
        JposException e = Dev.closePort(true);
        Dev.Offline = JposConst.JPOS_PS_UNKNOWN;
        Dev.InIOError = false;
        super.release();
    }

    @Override
    public void deviceEnabled(boolean enable) throws JposException {
        super.deviceEnabled(enable);
        if (enable) {
            DispenserStatus = Dev.DispenserState;
        }
    }

    @Override
    public void handlePowerStateOnEnable() throws JposException {
        PowerState = Dev.InIOError ? JposConst.JPOS_PS_OFF_OFFLINE : JposConst.JPOS_PS_ONLINE;
        super.handlePowerStateOnEnable();
    }

    @Override
    public void adjustCashCounts(String cashCounts) throws JposException {
        // UPOS spec: Call will be ignored because sample coin dispenser can detect the exact amount of cash in it.
        super.adjustCashCounts(cashCounts);
    }

    @Override
    public void readCashCounts(String[] cashCounts, boolean[] discrepancy) throws JposException {
        super.readCashCounts(cashCounts, discrepancy);
        if (Dev.Offline != JposConst.JPOS_PS_ONLINE)
            throw new JposException(JposConst.JPOS_E_OFFLINE, "Coin dispenser not connected");
        if (Dev.DispenserState == CoinDispenserConst.COIN_STATUS_JAM)
            throw new JposException(JposConst.JPOS_E_FAILURE, "Coin dispenser not ready");
        if (!Dev.ReadArgumentCheck || cashCounts[0] == null)
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
            for (j = Dev.SlotCoinValues.length - 1; j >= 0; --j) {
                if (Dev.SlotCoinValues[j] == values[i][0]) {
                    values[i][1] = Dev.SlotCount[j];
                    break;
                }
            }
            Dev.check(values[i][1] < 0, JposConst.JPOS_E_ILLEGAL, "Invalid coin value");
            res = valueStrings[0] + (res == null ? ":" + values[i][1] : ":" + values[i][1] + "," + res);
        }
        discrepancy[0] = checkDiscrepancy(values);
        cashCounts[0] = res;
    }

    private boolean checkDiscrepancy(int[][] values) {
        int sum1 = 0, sum2 = 0;
        for (int[] value : values)
            sum1 += value[1];
        for (int value : Dev.SlotCount)
            sum2 += value;
        return sum1 != sum2;
    }

    @Override
    public void dispenseChange(int amount) throws JposException {
        Dev.check (amount > 610, JposConst.JPOS_E_ILLEGAL, "Amount too big");
        int val;
        int[] array = new int[Dev.HWSlotCount.length];
        if ((val = amount / 200) == 2)
            array[Dev.HWSlot200a] = array[Dev.HWSlot200b] = 1;
        else if (val == 1)
            array[Dev.HWSlotCount[Dev.HWSlot200a] > Dev.HWSlotCount[Dev.HWSlot200b] ? Dev.HWSlot200a : Dev.HWSlot200b] = 1;
        array[Dev.HWSlot100] = (amount %= 200) / 100;
        array[Dev.HWSlot50] = (amount %= 100) / 50;
        if ((val = (amount %= 50) / 20) == 2)
            array[Dev.HWSlot20a] = array[Dev.HWSlot20b] = 1;
        else if (val == 1)
            array[Dev.HWSlotCount[Dev.HWSlot20a] > Dev.HWSlotCount[Dev.HWSlot20b] ? Dev.HWSlot20a : Dev.HWSlot20b] = 1;
        array[Dev.HWSlot10] = (amount %= 20) / 10;
        array[Dev.HWSlot5] = (amount %= 10) / 5;
        if ((val = (amount %= 5) / 2) == 2)
            array[Dev.HWSlot2a] = array[Dev.HWSlot2b] = 1;
        else if (val == 1)
            array[Dev.HWSlotCount[Dev.HWSlot2a] > Dev.HWSlotCount[Dev.HWSlot2b] ? Dev.HWSlot2a : Dev.HWSlot2b] = 1;
        array[Dev.HWSlot1] = amount % 2;
        String command = "";
        for (int i = 1; i < array.length; i++) {
            command += array[i] > 0 ? " 1" : " 0";
        }
        int result;
        synchronized (Dev.SlotCount) {
            int offline = Dev.Offline;
            Dev.handleStatusChanges(result = Dev.handleResponse(Dev.sendCommand("O" + command)), offline);
        }
        if (result == CoinDispenserConst.COIN_STATUS_JAM) {
            if (Dev.Offline == JposConst.JPOS_PS_ONLINE)
                throw new JposException(JposConst.JPOS_E_FAILURE, "Coin dispenser error, check slots");
            else
                throw new JposException(JposConst.JPOS_E_OFFLINE, "Coin dispenser offline");
        }
    }

    @Override
    public void checkHealth(int level) throws JposException {
        String healthError = "";

        if (level == JposConst.JPOS_CH_INTERACTIVE) {
            Dev.synchronizedMessageBox("Press OK to start health test.", "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
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
                result = Dev.Offline != JposConst.JPOS_PS_ONLINE ? "Offline" : (Dev.DispenserState == CoinDispenserConst.COIN_STATUS_JAM ? "Jam" : "Missing Coins");
                healthError += " CheckHealth: " + result + ": ";
            }
            if (level == JposConst.JPOS_CH_INTERACTIVE) {
                Dev.synchronizedMessageBox("Interactive check health result: " + result, "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
            }
        } else
            healthError = "Internal CheckHealth: ";
        CheckHealthText = healthError + (CheckHealthText.matches(".*Fail.*") ? "ERROR." : "OK.");
        EventSource.logSet("CheckHealthText");
    }
}
