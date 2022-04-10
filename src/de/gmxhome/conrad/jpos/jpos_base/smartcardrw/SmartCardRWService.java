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

/**
 * SmartCardRW service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class SmartCardRWService extends JposBase implements SmartCardRWService115 {
    /**
     * Instance of a class implementing the SmartCardRWInterface for smart card reader / writer specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public SmartCardRWInterface SmartCardRW;

    private SmartCardRWProperties Data;
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
        return false;
    }

    @Override
    public int getCapInterfaceMode() throws JposException {
        return 0;
    }

    @Override
    public int getCapIsoEmvMode() throws JposException {
        return 0;
    }

    @Override
    public int getCapSCPresentSensor() throws JposException {
        return 0;
    }

    @Override
    public int getCapSCSlots() throws JposException {
        return 0;
    }

    @Override
    public int getCapTransmissionProtocol() throws JposException {
        return 0;
    }

    @Override
    public int getInterfaceMode() throws JposException {
        return 0;
    }

    @Override
    public void setInterfaceMode(int i) throws JposException {

    }

    @Override
    public int getIsoEmvMode() throws JposException {
        return 0;
    }

    @Override
    public void setIsoEmvMode(int i) throws JposException {

    }

    @Override
    public int getSCPresentSensor() throws JposException {
        return 0;
    }

    @Override
    public int getSCSlot() throws JposException {
        return 0;
    }

    @Override
    public void setSCSlot(int i) throws JposException {

    }

    @Override
    public boolean getTransactionInProgress() throws JposException {
        return false;
    }

    @Override
    public int getTransmissionProtocol() throws JposException {
        return 0;
    }

    @Override
    public void beginInsertion(int i) throws JposException {

    }

    @Override
    public void beginRemoval(int i) throws JposException {

    }

    @Override
    public void endInsertion() throws JposException {

    }

    @Override
    public void endRemoval() throws JposException {

    }

    @Override
    public void readData(int i, int[] ints, String[] strings) throws JposException {

    }

    @Override
    public void writeData(int i, int i1, String s) throws JposException {

    }
}
