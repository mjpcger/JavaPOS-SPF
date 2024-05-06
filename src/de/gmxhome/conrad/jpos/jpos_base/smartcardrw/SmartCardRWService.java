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

package de.gmxhome.conrad.jpos.jpos_base.smartcardrw;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.services.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;
import static jpos.SmartCardRWConst.*;

/**
 * SmartCardRW service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class SmartCardRWService extends JposBase implements SmartCardRWService116 {
    /**
     * Instance of a class implementing the SmartCardRWInterface for smart card reader / writer specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public SmartCardRWInterface SmartCardRW;

    private final SmartCardRWProperties Data;
    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public SmartCardRWService(SmartCardRWProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public boolean getCapCardErrorDetection() throws JposException {
        logGet("CapCardErrorDetection");
        checkOpened();
        return Data.CapCardErrorDetection;
    }

    @Override
    public int getCapInterfaceMode() throws JposException {
        logGet("CapInterfaceMode");
        checkOpened();
        return Data.CapInterfaceMode;
    }

    @Override
    public int getCapIsoEmvMode() throws JposException {
        logGet("CapIsoEmvMode");
        checkOpened();
        return Data.CapIsoEmvMode;
    }

    @Override
    public int getCapSCPresentSensor() throws JposException {
        logGet("CapSCPresentSensor");
        checkOpened();
        return Data.CapSCPresentSensor;
    }

    @Override
    public int getCapSCSlots() throws JposException {
        logGet("CapSCSlots");
        checkOpened();
        return Data.CapSCSlots;
    }

    @Override
    public int getCapTransmissionProtocol() throws JposException {
        logGet("CapTransmissionProtocol");
        checkOpened();
        return Data.CapTransmissionProtocol;
    }

    @Override
    public int getInterfaceMode() throws JposException {
        logGet("InterfaceMode");
        checkEnabled();
        return Data.InterfaceMode;
    }

    @Override
    public int getIsoEmvMode() throws JposException {
        logGet("IsoEmvMode");
        checkEnabled();
        return Data.IsoEmvMode;
    }

    @Override
    public int getSCPresentSensor() throws JposException {
        logGet("SCPresentSensor");
        checkEnabled();
        return Data.SCPresentSensor;
    }

    @Override
    public int getSCSlot() throws JposException {
        logGet("SCSlot");
        checkEnabled();
        return Data.SCSlot;
    }

    @Override
    public boolean getTransactionInProgress() throws JposException {
        logGet("TransactionInProgress");
        checkOpened();
        return Data.TransactionInProgress;
    }

    @Override
    public int getTransmissionProtocol() throws JposException {
        logGet("TransmissionProtocol");
        checkOpened();
        return Data.TransmissionProtocol;
    }

    @Override
    public void setInterfaceMode(int mode) throws JposException {
        int[][] validCombinations = {
                {SC_MODE_TRANS, SC_CMODE_TRANS}, {SC_MODE_BLOCK, SC_CMODE_BLOCK},
                {SC_MODE_APDU, SC_CMODE_APDU}, {SC_MODE_XML, SC_CMODE_XML}
        };
        logPreSet("InterfaceMode");
        checkEnabled();
        for (int[] pair : validCombinations) {
            if (mode == pair[0]) {
                check((Data.CapInterfaceMode & pair[1]) == 0, JPOS_E_ILLEGAL, "Unsupported InterfaceMode: " + mode);
                SmartCardRW.interfaceMode(mode);
                logSet("InterfaceMode");
                return;
            }
        }
        throw new JposException(JPOS_E_ILLEGAL, "Invalid InterfaceMode: " + mode);
    }

    @Override
    public void setIsoEmvMode(int mode) throws JposException {
        int[][] validCombinations = { {SC_MODE_ISO, SC_CMODE_ISO}, {SC_MODE_EMV, SC_CMODE_EMV} };
        logPreSet("IsoEmvMode");
        checkEnabled();
        for (int[] pair : validCombinations) {
            if (mode == pair[0]) {
                check((Data.CapIsoEmvMode & pair[1]) == 0, JPOS_E_ILLEGAL, "Unsupported IsoEmvMode: " + mode);
                SmartCardRW.isoEmvMode(mode);
                logSet("IsoEmvMode");
                return;
            }
        }
        throw new JposException(JPOS_E_ILLEGAL, "Invalid IsoEmvMode: " + mode);
    }

    @Override
    public void setSCSlot(int slots) throws JposException {
        logPreSet("SCSlot");
        checkEnabled();
        int count = 0;
        for (int i = 0; i < Integer.SIZE; i++) {
            if (((1 << i) & slots) != 0)
                count++;
        }
        int badslots = ~Data.CapSCSlots & slots;
        check(count != 1, JPOS_E_ILLEGAL, "Invalid slot selected: " + Integer.toHexString(slots));
        check(badslots != 0, JPOS_E_ILLEGAL, "Unsupported slot selected: " + Integer.toHexString(badslots));
        SmartCardRW.sCSlot(slots);
        logSet("SCSlot");
    }

    @Override
    public void beginInsertion(int timeout) throws JposException {
        logPreCall("BeginInsertion", removeOuterArraySpecifier(new Object[]{timeout}, Device.MaxArrayStringElements));
        checkEnabled();
        check(timeout != JPOS_FOREVER && timeout < 0, JPOS_E_ILLEGAL, "Invalid timeout value");
        SmartCardRW.beginInsertion(timeout);
        logCall("BeginInsertion");
    }

    @Override
    public void beginRemoval(int timeout) throws JposException {
        logPreCall("BeginRemoval", removeOuterArraySpecifier(new Object[]{timeout}, Device.MaxArrayStringElements));
        checkEnabled();
        check(timeout != JPOS_FOREVER && timeout < 0, JPOS_E_ILLEGAL, "Invalid timeout value");
        SmartCardRW.beginRemoval(timeout);
        logCall("BeginRemoval");
    }

    @Override
    public void endInsertion() throws JposException {
        logPreCall("EndInsertion", "");
        checkEnabled();
        SmartCardRW.endInsertion();
        logCall("EndInsertion");
    }

    @Override
    public void endRemoval() throws JposException {
        logPreCall("EndRemoval", "");
        checkEnabled();
        SmartCardRW.endRemoval();
        logCall("EndRemoval");
    }

    @Override
    public void readData(int action, int[] count, String[] data) throws JposException {
        logPreCall("ReadData", removeOuterArraySpecifier(new Object[]{action, "..."}, Device.MaxArrayStringElements));
        long[] valid = { SC_READ_DATA, SC_READ_PROGRAM, SC_EXECUTE_AND_READ_DATA, SC_XML_READ_BLOCK_DATA };
        check(count == null || count.length != 1, JPOS_E_ILLEGAL, "Count is not array with length 1");
        check(data == null || data.length != 1, JPOS_E_ILLEGAL, "Data is not array with length 1");
        checkEnabled();
        checkMember(action, valid, JPOS_E_ILLEGAL, "Invalid read action: " + action);
        SmartCardRW.readData(action, count, data);
        logCall("ReadData", removeOuterArraySpecifier(new Object[]{"...", count[0], data[0]}, Device.MaxArrayStringElements));
    }

    @Override
    public void writeData(int action, int count, String data) throws JposException {
        logPreCall("WriteData", removeOuterArraySpecifier(new Object[]{action, count, data}, Device.MaxArrayStringElements));
        long[] valid = { SC_STORE_DATA, SC_STORE_PROGRAM, SC_EXECUTE_DATA, SC_XML_BLOCK_DATA, SC_SECURITY_FUSE, SC_RESET };
        if (data == null)
            data = "";
        checkEnabled();
        checkMember(action, valid, JPOS_E_ILLEGAL, "Invalid write action: " + action);
        check(count <= 0, JPOS_E_ILLEGAL, "Invalid byte count: " + count);
        callNowOrLater(SmartCardRW.writeData(action, count, data));
        logAsyncCall("WriteData");
    }
}
