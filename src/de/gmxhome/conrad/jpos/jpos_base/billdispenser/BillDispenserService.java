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

package de.gmxhome.conrad.jpos.jpos_base.billdispenser;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.services.*;

/**
 * BillDispenser service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class BillDispenserService extends JposBase implements BillDispenserService115 {
    /**
     * Instance of a class implementing the BillDispenserInterface for cash drawer specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public BillDispenserInterface BillDispenserInterface;

    private BillDispenserProperties Data;

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public BillDispenserService(BillDispenserProperties props, JposDevice device) {
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
    public void setCurrencyCode(String s) throws JposException {
        logPreSet("CurrencyCode");
        if (s == null)
            s = "";
        checkOpened();
        String[] allowed = Data.CurrencyCodeList.split(",");
        Device.check(!JposDevice.member(s, allowed), JposConst.JPOS_E_ILLEGAL, "Currency code " + s + " not in { " + Data.CurrencyCodeList + "}");
        checkNoChangedOrClaimed(Data.CurrencyCode, s);
        BillDispenserInterface.currencyCode(s);
        logSet("CurrencyCode");
    }

    @Override
    public void setCurrentExit(int i) throws JposException {
        logPreSet("CurrentExit");
        checkEnabled();
        Device.check(i < 1 || i > Data.DeviceExits, JposConst.JPOS_E_ILLEGAL, "CurrentExit out of range: " + i);
        BillDispenserInterface.currentExit(i);
        logSet("CurrentExit");
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
        BillDispenserInterface.adjustCashCounts(cashCounts);
        logCall("AdjustCashCounts");
    }

    @Override
    public void dispenseCash(String cashCounts) throws JposException {
        logPreCall("DispenseCash", cashCounts == null ? "null" : "" + cashCounts);
        checkEnabled();
        Device.check(Props.State == JposConst.JPOS_S_BUSY, JposConst.JPOS_E_BUSY, "Device is busy");
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
        if (callNowOrLater(BillDispenserInterface.dispenseCash(cashCounts)))
            logAsyncCall("DispenseCash");
        else
            logCall("DispenseCash");
    }

    @Override
    public void readCashCounts(String[] cashCounts, boolean[] discrepancy) throws JposException {
        logPreCall("ReadCashCounts");
        checkEnabled();
        Device.check(cashCounts == null, JposConst.JPOS_E_ILLEGAL, "cashCounts null");
        Device.check(cashCounts.length != 1, JposConst.JPOS_E_ILLEGAL, "cashCounts: Invalid array size");
        Device.check(discrepancy == null, JposConst.JPOS_E_ILLEGAL, "discrepancy null");
        Device.check(discrepancy.length != 1, JposConst.JPOS_E_ILLEGAL, "discrepancy: Invalid array size");
        BillDispenserInterface.readCashCounts(cashCounts, discrepancy);
        try {
            logCall("ReadCashCounts", "{ " + cashCounts[0] + " }, " + discrepancy[0]);
        } catch (NullPointerException e) {
            throw new JposException(JposConst.JPOS_E_FAILURE, "Invalid result for " + (cashCounts[0] == null ? "cashCounts" : "discrepancy"), e);
        }
    }
}
