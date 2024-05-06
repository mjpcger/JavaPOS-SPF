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

package de.gmxhome.conrad.jpos.jpos_base.coinacceptor;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.services.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.CoinAcceptorConst.*;
import static jpos.JposConst.*;

/**
 * CoinAcceptor service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class CoinAcceptorService extends JposBase implements CoinAcceptorService116 {
    /**
     * Instance of a class implementing the CoinAcceptorInterface for coin acceptor specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public CoinAcceptorInterface CoinAcceptorInterface;

    private final CoinAcceptorProperties Data;

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public CoinAcceptorService(CoinAcceptorProperties props, JposDevice device) {
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
        check(Data.FullStatus == null, JPOS_E_FAILURE, "Not initialized: FullStatus");
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
        if (s == null)
            s = "";
        checkOpened();
        String[] allowed = Data.DepositCodeList.split(",");
        check(!member(s, allowed), JPOS_E_ILLEGAL, "Currency code " + s + " not in { " + Data.DepositCodeList + "}");
        checkNoChangedOrClaimed(Data.CurrencyCode, s);
        CoinAcceptorInterface.currencyCode(s);
        logSet("CurrencyCode");
    }

    @Override
    public void setRealTimeDataEnabled(boolean b) throws JposException {
        logPreSet("RealTimeDataEnabled");
        checkEnabled();
        check(!Data.CapRealTimeData && b, JPOS_E_ILLEGAL, "Device does not support RealTimeData");
        CoinAcceptorInterface.realTimeDataEnabled(b);
        logSet("RealTimeDataEnabled");
    }

    @Override
    public void adjustCashCounts(String cashCounts) throws JposException {
        logPreCall("AdjustCashCounts", removeOuterArraySpecifier(new Object[]{cashCounts}, Device.MaxArrayStringElements));
        checkEnabled();
        check(cashCounts == null, JPOS_E_ILLEGAL, "Cash counts null");
        String[] cashCount = cashCounts.split(",");
        check(cashCount.length == 0, JPOS_E_ILLEGAL, "No cash counts");
        for (String entry : cashCount) {
            String[] values = entry.split(":");
            check(values.length != 2, JPOS_E_ILLEGAL, "Bad format of cash count");
            try {
                check(Integer.parseInt(values[0]) <= 0 || Integer.parseInt(values[1]) < 0, JPOS_E_ILLEGAL, "Bad format of cash count");
            }
            catch (NumberFormatException e) {
                throw new JposException(JPOS_E_ILLEGAL, "Non-integer cash count component", e);
            }
        }
        CoinAcceptorInterface.adjustCashCounts(cashCounts);
        logCall("AdjustCashCounts");
    }

    @Override
    public void beginDeposit() throws JposException {
        logPreCall("BeginDeposit");
        checkEnabled();
        check(Data.DepositStatus != CACC_STATUS_DEPOSIT_END, JPOS_E_ILLEGAL,
                (Data.DepositStatus == CACC_STATUS_DEPOSIT_JAM) ? "Jam condition" : "Just in deposit operation");
        CoinAcceptorInterface.beginDeposit();
        logCall("BeginDeposit");
    }

    @Override
    public void endDeposit(int success) throws JposException {
        logPreCall("EndDeposit", removeOuterArraySpecifier(new Object[]{success}, Device.MaxArrayStringElements));
        checkEnabled();
        check(Data.DepositStatus != CACC_STATUS_DEPOSIT_COUNT, JPOS_E_ILLEGAL,
                (Data.DepositStatus == CACC_STATUS_DEPOSIT_JAM) ? "Jam condition" : "Operation not fixed");
        check(success != CACC_DEPOSIT_COMPLETE, JPOS_E_ILLEGAL, "Invalid success code: " + success);
        CoinAcceptorInterface.endDeposit(success);
        logCall("EndDeposit");
    }

    @Override
    public void fixDeposit() throws JposException {
        logPreCall("FixDeposit");
        checkEnabled();
        check(Data.DepositStatus != CACC_STATUS_DEPOSIT_START, JPOS_E_ILLEGAL,
                (Data.DepositStatus == CACC_STATUS_DEPOSIT_JAM ? "Jam condition" :
                        (Data.DepositStatus == CACC_STATUS_DEPOSIT_END ? "Operation not started" : "Operation just fixed")));
        CoinAcceptorInterface.fixDeposit();
        logCall("FixDeposit");
    }

    @Override
    public void pauseDeposit(int control) throws JposException {
        logPreCall("PauseDeposit", removeOuterArraySpecifier(new Object[]{control}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapPauseDeposit, JPOS_E_ILLEGAL, "PauseDeposit not supported");
        check(control == CACC_DEPOSIT_PAUSE && Data.DepositStatus == CACC_STATUS_DEPOSIT_END,
                JPOS_E_ILLEGAL, "No pending deposit operation");
        long[] allowed = { CACC_DEPOSIT_PAUSE, CACC_DEPOSIT_RESTART };
        checkMember(control, allowed, JPOS_E_ILLEGAL, "Illegal parameter value");
        CoinAcceptorInterface.pauseDeposit(control);
        logCall("PauseDeposit");
    }

    @Override
    public void readCashCounts(String[] cashCounts, boolean[] discrepancy) throws JposException {
        logPreCall("ReadCashCounts");
        checkEnabled();
        check(cashCounts == null, JPOS_E_ILLEGAL, "cashCounts null");
        check(cashCounts.length != 1, JPOS_E_ILLEGAL, "cashCounts: Invalid array size");
        check(discrepancy == null, JPOS_E_ILLEGAL, "discrepancy null");
        check(discrepancy.length != 1, JPOS_E_ILLEGAL, "discrepancy: Invalid array size");
        CoinAcceptorInterface.readCashCounts(cashCounts, discrepancy);
        logCall("ReadCashCounts", removeOuterArraySpecifier(new Object[]{cashCounts[0], discrepancy[0]}, Device.MaxArrayStringElements));
    }
}
