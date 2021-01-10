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

package de.gmxhome.conrad.jpos.jpos_base.cashchanger;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.services.*;

/**
 * CashChanger service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class CashChangerService extends JposBase implements CashChangerService114 {
    /**
     * Instance of a class implementing the CashChangerInterface for cash drawer specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public CashChangerInterface CashChangerInterface;

    private CashChangerProperties Data;

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public CashChangerService(CashChangerProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public int getAsyncResultCode() throws JposException {
        checkEnabled();
        logGet("AsyncResultCode");
        return Data.AsyncResultCode;
    }

    @Override
    public int getAsyncResultCodeExtended() throws JposException {
        checkEnabled();
        logGet("AsyncResultCodeExtended");
        return Data.AsyncResultCodeExtended;
    }

    @Override
    public boolean getCapDeposit() throws JposException {
        checkOpened();
        logGet("CapDeposit");
        return Data.CapDeposit;
    }

    @Override
    public boolean getCapDepositDataEvent() throws JposException {
        checkOpened();
        logGet("CapDepositDataEvent");
        return Data.CapDepositDataEvent;
    }

    @Override
    public boolean getCapDiscrepancy() throws JposException {
        checkOpened();
        logGet("CapDiscrepancy");
        return Data.CapDiscrepancy;
    }

    @Override
    public boolean getCapEmptySensor() throws JposException {
        checkOpened();
        logGet("CapEmptySensor");
        return Data.CapEmptySensor;
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
    public boolean getCapNearEmptySensor() throws JposException {
        checkOpened();
        logGet("CapNearEmptySensor");
        return Data.CapNearEmptySensor;
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
    public boolean getCapRepayDeposit() throws JposException {
        checkOpened();
        logGet("CapRepayDeposit");
        return Data.CapRepayDeposit;
    }

    @Override
    public boolean getCapRealTimeData() throws JposException {
        checkOpened();
        logGet("CapRealTimeData");
        return Data.CapRealTimeData;
    }

    @Override
    public String getCurrencyCashList() throws JposException {
        checkOpened();
        logGet("CurrencyCashList");
        return Data.CurrencyCashList;
    }

    @Override
    public String getCurrencyCode() throws JposException {
        checkOpened();
        logGet("CurrencyCode");
        return Data.CurrencyCode;
    }

    @Override
    public String getCurrencyCodeList() throws JposException {
        checkOpened();
        logGet("CurrencyCodeList");
        return Data.CurrencyCodeList;
    }

    @Override
    public int getCurrentExit() throws JposException {
        checkOpened();
        logGet("CurrentExit");
        return Data.CurrentExit;
    }

    @Override
    public int getCurrentService() throws JposException {
        checkOpened();
        logGet("CurrentService");
        return Data.CurrentService;
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
        Device.check(Data.DepositStatus == null, JposConst.JPOS_E_FAILURE, "Not initialized: DepositStatus");
        logGet("DepositStatus");
        return Data.DepositStatus;
    }

    @Override
    public int getDeviceExits() throws JposException {
        checkOpened();
        logGet("DeviceExits");
        return Data.DeviceExits;
    }

    @Override
    public int getDeviceStatus() throws JposException {
        checkEnabled();
        Device.check(Data.DeviceStatus == null, JposConst.JPOS_E_FAILURE, "Not initialized: DeviceStatus");
        logGet("DeviceStatus");
        return Data.DeviceStatus;
    }

    @Override
    public String getExitCashList() throws JposException {
        checkOpened();
        logGet("ExitCashList");
        return Data.ExitCashList;
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
    public int getServiceCount() throws JposException {
        checkOpened();
        logGet("ServiceCount");
        return Data.ServiceCount;
    }

    @Override
    public int getServiceIndex() throws JposException {
        checkOpened();
        logGet("ServiceIndex");
        return Data.ServiceIndex;
    }

    @Override
    public void setCurrencyCode(String s) throws JposException {
        logPreSet("CurrencyCode");
        checkOpened();
        String[] allowed = Data.CurrencyCodeList.split(",");
        Device.check(!JposDevice.member(s, allowed), JposConst.JPOS_E_ILLEGAL, "Currency code " + s + " not in { " + Data.CurrencyCodeList + "}");
        CashChangerInterface.currencyCode(s);
        logSet("CurrencyCode");
    }

    @Override
    public void setCurrentExit(int i) throws JposException {
        logPreSet("CurrentExit");
        checkEnabled();
        Device.check(i < 1 || i > Data.DeviceExits, JposConst.JPOS_E_ILLEGAL, "CurrentExit out of range: " + i);
        CashChangerInterface.currentExit(i);
        logSet("CurrentExit");
    }

    @Override
    public void setCurrentService(int i) throws JposException {
        logPreSet("CurrentService");
        checkEnabled();
        Device.check(i < 0 || i > Data.ServiceCount, JposConst.JPOS_E_ILLEGAL, "CurrentService out of range: " + i);
        long[] allowed = {Data.ServiceIndex & 0xff, (Data.ServiceIndex >> 8) & 0xff, (Data.ServiceIndex >> 16) & 0xff, (Data.ServiceIndex >> 24) & 0xff};
        Device.check(i > 0 && !Device.member(i, allowed), JposConst.JPOS_E_ILLEGAL, "Unsupported service index: " + i);
        CashChangerInterface.currentService(i);
        logSet("CurrentService");
    }

    @Override
    public void setRealTimeDataEnabled(boolean b) throws JposException {
        logPreSet("RealTimeDataEnabled");
        checkEnabled();
        Device.check(!Data.CapRealTimeData && b, JposConst.JPOS_E_ILLEGAL, "Device does not support RealTimeData");
        CashChangerInterface.realTimeDataEnabled(b);
        logSet("RealTimeDataEnabled");
    }

    @Override
    public void adjustCashCounts(String cashCounts) throws JposException {
        logPreCall("AdjustCashCounts", cashCounts == null ? "null" : "" + cashCounts);
        checkEnabled();
        Device.check(cashCounts == null, JposConst.JPOS_E_ILLEGAL, "Cash counts null");
        String cashCountPart[] = cashCounts.split(";");
        Device.check(((cashCountPart.length - 1) & ~1) != 0, JposConst.JPOS_E_ILLEGAL, "Bad format of cash counts");
        boolean nocounts = true;
        for (String parts : cashCountPart) {
            if (!parts.equals("")) {
                nocounts = false;
                String cashCount[] = parts.substring(1).split(",");
                for (String entry : cashCount) {
                    String values[] = entry.split(":");
                    Device.check(values.length != 2, JposConst.JPOS_E_ILLEGAL, "Bad format of cash count");
                    try {
                        Device.check(Integer.parseInt(values[0]) <= 0 || Integer.parseInt(values[1]) < 0, JposConst.JPOS_E_ILLEGAL, "Bad format of cash count");
                    } catch (NumberFormatException e) {
                        throw new JposException(JposConst.JPOS_E_ILLEGAL, "Non-integer cash count component", e);
                    }
                }
            }
        }
        Device.check(nocounts, JposConst.JPOS_E_ILLEGAL, "No cash counts");
        CashChangerInterface.adjustCashCounts(cashCounts);
        logCall("AdjustCashCounts");
    }

    @Override
    public void beginDeposit() throws JposException {
        logPreCall("BeginDeposit");
        checkEnabled();
        CashChangerInterface.beginDeposit();
        logCall("BeginDeposit");
    }

    @Override
    public void endDeposit(int success) throws JposException {
        logPreCall("EndDeposit", "" + success);
        checkEnabled();
        Device.check(success != BillAcceptorConst.BACC_DEPOSIT_COMPLETE, JposConst.JPOS_E_ILLEGAL, "Invalid success code: " + success);
        CashChangerInterface.endDeposit(success);
        logCall("EndDeposit");
    }

    @Override
    public void fixDeposit() throws JposException {
        logPreCall("FixDeposit");
        checkEnabled();
        CashChangerInterface.fixDeposit();
        logCall("FixDeposit");
    }

    @Override
    public void pauseDeposit(int control) throws JposException {
        logPreCall("PauseDeposit");
        checkEnabled();
        Device.check(!Data.CapPauseDeposit, JposConst.JPOS_E_ILLEGAL, "PauseDeposit not supported");
        long allowed[] = {BillAcceptorConst.BACC_DEPOSIT_PAUSE, BillAcceptorConst.BACC_DEPOSIT_RESTART };
        Device.checkMember(control, allowed, JposConst.JPOS_E_ILLEGAL, "Illegal parameter value");
        CashChangerInterface.pauseDeposit(control);
        logCall("PauseDeposit");
    }

    @Override
    public void dispenseCash(String cashCounts) throws JposException {
        logPreCall("DispenseCash", cashCounts == null ? "null" : "" + cashCounts);
        checkEnabled();
        Device.check(cashCounts == null, JposConst.JPOS_E_ILLEGAL, "Cash counts null");
        String cashCountPart[] = cashCounts.split(";");
        Device.check(((cashCountPart.length - 1) & ~1) != 0, JposConst.JPOS_E_ILLEGAL, "Bad format of cash counts");
        boolean nocounts = true;
        for (String parts : cashCountPart) {
            if (!parts.equals("")) {
                nocounts = false;
                String cashCount[] = parts.substring(1).split(",");
                for (String entry : cashCount) {
                    String values[] = entry.split(":");
                    Device.check(values.length != 2, JposConst.JPOS_E_ILLEGAL, "Bad format of cash count");
                    try {
                        Device.check(Integer.parseInt(values[0]) <= 0 || Integer.parseInt(values[1]) < 0, JposConst.JPOS_E_ILLEGAL, "Bad format of cash count");
                    } catch (NumberFormatException e) {
                        throw new JposException(JposConst.JPOS_E_ILLEGAL, "Non-integer cash count component", e);
                    }
                }
            }
        }
        Device.check(nocounts, JposConst.JPOS_E_ILLEGAL, "No cash counts");
        if (callNowOrLater(CashChangerInterface.dispenseCash(cashCounts)))
            logAsyncCall("DispenseCash");
        else
            logCall("DispenseCash");
  }

    @Override
    public void dispenseChange(int amount) throws JposException {
        logPreCall("DispenseChange", "" + amount);
        checkEnabled();
        Device.check(amount <= 0, JposConst.JPOS_E_ILLEGAL, "Invalid amount");
        if (callNowOrLater(CashChangerInterface.dispenseChange(amount)))
            logAsyncCall("DispenseChange");
        else
            logCall("DispenseChange");
    }

    @Override
    public void readCashCounts(String[] cashCounts, boolean[] discrepancy) throws JposException {
        logPreCall("ReadCashCounts");
        checkEnabled();
        Device.check(cashCounts == null, JposConst.JPOS_E_ILLEGAL, "cashCounts null");
        Device.check(cashCounts.length != 1, JposConst.JPOS_E_ILLEGAL, "cashCounts: Invalid array size");
        Device.check(discrepancy == null, JposConst.JPOS_E_ILLEGAL, "discrepancy null");
        Device.check(discrepancy.length != 1, JposConst.JPOS_E_ILLEGAL, "discrepancy: Invalid array size");
        CashChangerInterface.readCashCounts(cashCounts, discrepancy);
        try {
            logCall("ReadCashCounts", "{ " + cashCounts[0] + " }, " + discrepancy[0]);
        } catch (NullPointerException e) {
            throw new JposException(JposConst.JPOS_E_FAILURE, "Invalid result for " + (cashCounts[0] == null ? "cashCounts" : "discrepancy"), e);
        }
    }
}
