/*
 * Copyright 2019 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.coindispenser;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.services.CoinDispenserService114;

/**
 * CoinDispenser service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class CoinDispenserService extends JposBase implements CoinDispenserService114 {
    /**
     * Instance of a class implementing the CoinDispenserInterface for coin dispenser specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public CoinDispenserInterface CoinDispenserInterface;

    private CoinDispenserProperties Data;

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public CoinDispenserService(CoinDispenserProperties props, JposDevice device) {
        super(props, device);
        Data = props;
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
    public int getDispenserStatus() throws JposException {
        checkEnabled();
        logGet("DispenserStatus");
        return Data.DispenserStatus;
    }

    @Override
    public void adjustCashCounts(String cashCounts) throws JposException {
        logPreCall("AdjustCashCounts", cashCounts == null ? "null" : "" + cashCounts);
        checkEnabled();
        Device.check(cashCounts == null, JposConst.JPOS_E_ILLEGAL, "Cash counts null");
        String cashCount[] = cashCounts.split(",");
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
        CoinDispenserInterface.adjustCashCounts(cashCounts);
        logCall("AdjustCashCounts");
    }

    private String generateArgString(String[] cashCounts, boolean[] discrepancy) {
        String res = "";
        if (cashCounts == null)
            res = "null, ";
        else {
            String prefix = "";
            res = "{ ";
            for (String arg : cashCounts) {
                res += prefix + (arg == null ? "null" : arg);
                prefix = ", ";
            }
            res += " }, ";
        }
        if (discrepancy == null)
            return res + "null";
        String prefix = "";
        res = "{ ";
        for (boolean arg : discrepancy) {
            res += prefix + arg;
            prefix = ", ";
        }
        return res + " }";
    }

    @Override
    public void readCashCounts(String[] cashCounts, boolean[] discrepancy) throws JposException {
        logPreCall("ReadCashCounts", generateArgString(cashCounts, discrepancy));
        checkEnabled();
        Device.check(cashCounts == null || cashCounts.length != 1, JposConst.JPOS_E_ILLEGAL, "Bad dimension of cashCounts");
        Device.check(discrepancy == null || discrepancy.length != 1, JposConst.JPOS_E_ILLEGAL, "Bad dimension of discrepancy");
        CoinDispenserInterface.readCashCounts(cashCounts, discrepancy);
        logCall("ReadCashCounts", generateArgString(cashCounts, discrepancy));
    }

    @Override
    public void dispenseChange(int amount) throws JposException {
        logPreCall("DispenseChange", "" + amount);
        checkEnabled();
        Device.check(amount <= 0 , JposConst.JPOS_E_ILLEGAL, "Amount negative or zero");
        CoinDispenserInterface.dispenseChange(amount);
        logCall("DispenseChange");
    }
}
