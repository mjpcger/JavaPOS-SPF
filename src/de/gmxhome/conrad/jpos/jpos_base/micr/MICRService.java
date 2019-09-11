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

package de.gmxhome.conrad.jpos.jpos_base.micr;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.services.MICRService114;

/**
 * MICR service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class MICRService extends JposBase implements MICRService114 {
    /**
     * Instance of a class implementing the MICRInterface for magnetic ink character recognition reader specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public MICRInterface MICRInterface;

    private MICRProperties Data;

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public MICRService(MICRProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public boolean getCapValidationDevice() throws JposException {
        checkOpened();
        logGet("CapValidationDevice");
        return Data.CapValidationDevice;
    }

    @Override
    public String getAccountNumber() throws JposException {
        checkOpened();
        logGet("AccountNumber");
        return Data.AccountNumber;
    }

    @Override
    public String getAmount() throws JposException {
        checkOpened();
        logGet("Amount");
        return Data.Amount;
    }

    @Override
    public String getBankNumber() throws JposException {
        checkOpened();
        logGet("BankNumber");
        return Data.BankNumber;
    }

    @Override
    public int getCheckType() throws JposException {
        checkOpened();
        MICRInterface.checkNoData();
        logGet("CheckType");
        return Data.CheckType;
    }

    @Override
    public int getCountryCode() throws JposException {
        checkOpened();
        MICRInterface.checkNoData();
        logGet("CountryCode");
        return Data.CountryCode;
    }

    @Override
    public String getEPC() throws JposException {
        checkOpened();
        logGet("EPC");
        return Data.EPC;
    }

    @Override
    public String getRawData() throws JposException {
        checkOpened();
        logGet("RawData");
        return Data.RawData;
    }

    @Override
    public String getSerialNumber() throws JposException {
        checkOpened();
        logGet("SerialNumber");
        return Data.SerialNumber;
    }

    @Override
    public String getTransitNumber() throws JposException {
        checkOpened();
        logGet("TransitNumber");
        return Data.TransitNumber;
    }

    @Override
    public void beginInsertion(int timeout) throws JposException {
        logPreCall("BeginInsertion", "" + timeout);
        checkEnabled();
        MICRInterface.checkBusy();
        Device.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout value: " + timeout);
        MICRInterface.beginInsertion(timeout);
        Data.InsertionMode = true;
        logCall("BeginInsertion");
    }

    @Override
    public void beginRemoval(int timeout) throws JposException {
        logPreCall("BeginRemoval", "" + timeout);
        checkEnabled();
        MICRInterface.checkBusy();
        Device.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout value: " + timeout);
        MICRInterface.beginRemoval(timeout);
        Data.RemovalMode = true;
        logCall("BeginRemoval");
    }

    @Override
    public void endInsertion() throws JposException {
        logPreCall("EndInsertion");
        checkEnabled();
        Device.check(!Data.InsertionMode, JposConst.JPOS_E_ILLEGAL, "Not in insertion mode");
        Device.check(Props.State != JposConst.JPOS_S_IDLE, JposConst.JPOS_E_BUSY, "Output in progress or error detected");
        Data.InsertionMode = false;
        MICRInterface.endInsertion();
        logCall("EndInsertion");
    }

    @Override
    public void endRemoval() throws JposException {
        logPreCall("EndRemoval");
        checkEnabled();
        Device.check(!Data.RemovalMode, JposConst.JPOS_E_ILLEGAL, "Not in removal mode");
        Device.check(Props.State != JposConst.JPOS_S_IDLE, JposConst.JPOS_E_BUSY, "Output in progress or error detected");
        Data.RemovalMode = false;
        MICRInterface.endRemoval();
        logCall("EndRemoval");
    }
}
