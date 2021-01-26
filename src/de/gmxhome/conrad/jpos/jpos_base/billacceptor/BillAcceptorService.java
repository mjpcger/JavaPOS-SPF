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

package de.gmxhome.conrad.jpos.jpos_base.billacceptor;

import de.gmxhome.conrad.jpos.jpos_base.JposBase;
import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import de.gmxhome.conrad.jpos.jpos_base.JposDevice;
import jpos.BillAcceptor;
import jpos.BillAcceptorConst;
import jpos.JposConst;
import jpos.JposException;
import jpos.services.BillAcceptorService114;

/**
 * BillAcceptor service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class BillAcceptorService extends JposBase implements BillAcceptorService114 {
    /**
     * Instance of a class implementing the BillAcceptorInterface for cash drawer specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public BillAcceptorInterface BillAcceptorInterface;

    private BillAcceptorProperties Data;

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public BillAcceptorService(BillAcceptorProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public boolean getCapDiscrepancy() throws JposException {
        checkOpened();
        logGet("CapDiscrepancy");
        return Data.CapDiscrepancy;
    }

    @Override
    public boolean getCapFullSensor() throws JposException {
        checkOpened();
        logGet("CapFullSensor");
        return Data.CapFullSensor;
    }

    @Override
    public boolean getCapJamSensor() throws JposException {
        checkOpened();
        logGet("CapJamSensor");
        return Data.CapJamSensor;
    }

    @Override
    public boolean getCapNearFullSensor() throws JposException {
        checkOpened();
        logGet("CapNearFullSensor");
        return Data.CapNearFullSensor;
    }

    @Override
    public boolean getCapPauseDeposit() throws JposException {
        checkOpened();
        logGet("CapPauseDeposit");
        return Data.CapPauseDeposit;
    }

    @Override
    public boolean getCapRealTimeData() throws JposException {
        checkOpened();
        logGet("CapRealTimeData");
        return Data.CapRealTimeData;
    }

    @Override
    public String getCurrencyCode() throws JposException {
        checkOpened();
        logGet("CurrencyCode");
        return Data.CurrencyCode;
    }

    @Override
    public int getDepositAmount() throws JposException {
        checkOpened();
        logGet("DepositAmount");
        return Data.DepositAmount;
    }

    @Override
    public String getDepositCashList() throws JposException {
        checkOpened();
        logGet("DepositCashList");
        return Data.DepositCashList;
    }

    @Override
    public String getDepositCodeList() throws JposException {
        checkOpened();
        logGet("DepositCodeList");
        return Data.DepositCodeList;
    }

    @Override
    public String getDepositCounts() throws JposException {
        checkOpened();
        logGet("DepositCounts");
        return Data.DepositCounts;
    }

    @Override
    public int getDepositStatus() throws JposException {
        checkEnabled();
        logGet("DepositStatus");
        return Data.DepositStatus;
    }

    @Override
    public int getFullStatus() throws JposException {
        checkEnabled();
        Device.check(Data.FullStatus == null, JposConst.JPOS_E_FAILURE, "Not initialized: FullStatus");
        logGet("FullStatus");
        return Data.FullStatus;
    }

    @Override
    public boolean getRealTimeDataEnabled() throws JposException {
        checkEnabled();
        logGet("RealTimeDataEnabled");
        return Data.RealTimeDataEnabled;
    }

    @Override
    public void setCurrencyCode(String s) throws JposException {
        logPreSet("CurrencyCode");
        checkOpened();
        String[] allowed = Data.DepositCodeList.split(",");
        Device.check(!JposDevice.member(s, allowed), JposConst.JPOS_E_ILLEGAL, "Currency code " + s + " not in { " + Data.DepositCodeList + "}");
        BillAcceptorInterface.currencyCode(s);
        logSet("CurrencyCode");
    }

    @Override
    public void setRealTimeDataEnabled(boolean b) throws JposException {
        logPreSet("RealTimeDataEnabled");
        checkEnabled();
        Device.check(!Data.CapRealTimeData && b, JposConst.JPOS_E_ILLEGAL, "Device does not support RealTimeData");
        BillAcceptorInterface.realTimeDataEnabled(b);
        logSet("RealTimeDataEnabled");
    }

    @Override
    public void adjustCashCounts(String cashCounts) throws JposException {
        logPreCall("AdjustCashCounts", cashCounts == null ? "null" : "" + cashCounts);
        checkEnabled();
        Device.check(cashCounts == null, JposConst.JPOS_E_ILLEGAL, "Cash counts null");
        Device.check(cashCounts.length() == 0 || cashCounts.charAt(0) != ';', JposConst.JPOS_E_ILLEGAL, "Bad format of cash count");
        String cashCount[] = cashCounts.substring(1).split(",");
        Device.check(cashCount.length == 0, JposConst.JPOS_E_ILLEGAL, "No cash counts");
        for (String entry : cashCount) {
            String values[] = entry.split(":");
            Device.check(values.length != 2, JposConst.JPOS_E_ILLEGAL, "Bad format of cash count");
            try {
                Device.check(Integer.parseInt(values[0]) <= 0 || Integer.parseInt(values[1]) < 0, JposConst.JPOS_E_ILLEGAL, "Bad format of cash count");
            }
            catch (NumberFormatException e) {
                throw new JposException(JposConst.JPOS_E_ILLEGAL, "Non-integer cash count component", e);
            }
        }
        BillAcceptorInterface.adjustCashCounts(cashCounts);
        logCall("AdjustCashCounts");
    }

    @Override
    public void beginDeposit() throws JposException {
        logPreCall("BeginDeposit");
        checkEnabled();
        Device.check(Data.DepositStatus != BillAcceptorConst.BACC_STATUS_DEPOSIT_END, JposConst.JPOS_E_ILLEGAL,
                (Data.DepositStatus == BillAcceptorConst.BACC_STATUS_DEPOSIT_JAM) ? "Jam condition" : "Just in deposit operation");
        BillAcceptorInterface.beginDeposit();
        logCall("BeginDeposit");
    }

    @Override
    public void endDeposit(int success) throws JposException {
        logPreCall("EndDeposit", "" + success);
        checkEnabled();
        Device.check(Data.DepositStatus != BillAcceptorConst.BACC_STATUS_DEPOSIT_COUNT, JposConst.JPOS_E_ILLEGAL,
                (Data.DepositStatus == BillAcceptorConst.BACC_STATUS_DEPOSIT_JAM) ? "Jam condition" : "Operation not fixed");
        Device.check(success != BillAcceptorConst.BACC_DEPOSIT_COMPLETE, JposConst.JPOS_E_ILLEGAL, "Invalid success code: " + success);
        BillAcceptorInterface.endDeposit(success);
        logCall("EndDeposit");
    }

    @Override
    public void fixDeposit() throws JposException {
        logPreCall("FixDeposit");
        checkEnabled();
        Device.check(Data.DepositStatus != BillAcceptorConst.BACC_STATUS_DEPOSIT_START, JposConst.JPOS_E_ILLEGAL,
                (Data.DepositStatus == BillAcceptorConst.BACC_STATUS_DEPOSIT_JAM ? "Jam condition" :
                        (Data.DepositStatus == BillAcceptorConst.BACC_STATUS_DEPOSIT_END ? "Operation not started" : "Operation just fixed")));
        BillAcceptorInterface.fixDeposit();
        logCall("FixDeposit");
    }

    @Override
    public void pauseDeposit(int control) throws JposException {
        logPreCall("PauseDeposit");
        checkEnabled();
        Device.check(!Data.CapPauseDeposit, JposConst.JPOS_E_ILLEGAL, "PauseDeposit not supported");
        Device.check(control == BillAcceptorConst.BACC_DEPOSIT_PAUSE && Data.DepositStatus == BillAcceptorConst.BACC_STATUS_DEPOSIT_END,
                JposConst.JPOS_E_ILLEGAL, "No pending deposit operation");
        long allowed[] = {BillAcceptorConst.BACC_DEPOSIT_PAUSE, BillAcceptorConst.BACC_DEPOSIT_RESTART };
        Device.checkMember(control, allowed, JposConst.JPOS_E_ILLEGAL, "Illegal parameter value");
        BillAcceptorInterface.pauseDeposit(control);
        logCall("PauseDeposit");
    }

    @Override
    public void readCashCounts(String[] cashCounts, boolean[] discrepancy) throws JposException {
        logPreCall("ReadCashCounts");
        checkEnabled();
        Device.check(cashCounts == null, JposConst.JPOS_E_ILLEGAL, "cashCounts null");
        Device.check(cashCounts.length != 1, JposConst.JPOS_E_ILLEGAL, "cashCounts: Invalid array size");
        Device.check(discrepancy == null, JposConst.JPOS_E_ILLEGAL, "discrepancy null");
        Device.check(discrepancy.length != 1, JposConst.JPOS_E_ILLEGAL, "discrepancy: Invalid array size");
        BillAcceptorInterface.readCashCounts(cashCounts, discrepancy);
        try {
            logCall("ReadCashCounts", "{ " + cashCounts[0] + " }, " + discrepancy[0]);
        } catch (NullPointerException e) {
            throw new JposException(JposConst.JPOS_E_FAILURE, "Invalid result for " + (cashCounts[0] == null ? "cashCounts" : "discrepancy"), e);
        }
    }
}
