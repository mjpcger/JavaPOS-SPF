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

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import jpos.JposException;
import jpos.SmartCardRWConst;

/**
 * Class containing the smart card reader / writer specific properties, their default values and default implementations of
 * SmartCardRWInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Smart Card Reader / Writer.
 * This is a base implementation only. The current implementor of JavaPOS-SPF has no experience with SmartCardRW and therefore
 * has no idea about meaningful defaults for most SmartCardRD properties, transmission protocols and interface modes.
 * <br>Especially, the author has no deeper knowledge about APDU structure or the ARTS XML standard for SCR/W functionality.
 * As result, currently the common service does not perform any APDU or XML data checks, this must be done by the device
 * specific implementation instead.
 */
public class SmartCardRWProperties extends JposCommonProperties implements SmartCardRWInterface {
    /**
     * UPOS property CapCardErrorDetection. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapCardErrorDetection = false;

    /**
     * UPOS property CapInterfaceMode. Default: CMODE_TRANS. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int CapInterfaceMode = SmartCardRWConst.SC_CMODE_TRANS;

    /**
     * UPOS property CapIsoEmvMode. Default: 0. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method, should be overwritten if CapInterfaceMode contains CMODE_APDU.
     */
    public int CapIsoEmvMode = 0;

    /**
     * UPOS property CapSCPresentSensor. Default: 0. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int CapSCPresentSensor = 0;

    /**
     * UPOS property CapSCSlots. Default: 1.
     */
    public int CapSCSlots = 1;

    /**
     * UPOS property CapTransmissionProtocol. Default: 0. Must be overwritten by objects derived from JposDevice within the
     * changeDefaults method to CTRANS_PROTOCOL_T0 or CTRANS_PROTOCOL_T1.
     */
    public int CapTransmissionProtocol = 0;

    /**
     * UPOS property InterfaceMode. Default: MODE_TRANS. Must be overwritten until first enabled if CapInterfaceMode
     * does not contain CMODE_TRANS.
     */
    public int InterfaceMode = SmartCardRWConst.SC_MODE_TRANS;

    /**
     * UPOS property IsoEmvMode. Default: 0.
     */
    public int IsoEmvMode = 0;

    /**
     * UPOS property SCPresentSensor. Default: 0.
     */
    public int SCPresentSensor = 0;

    /**
     * UPOS property SCSlot. Default: 1 (Slot 0).
     */
    public int SCSlot = 1;

    /**
     * UPOS property TransactionInProgress. Default: false.
     */
    public boolean TransactionInProgress = false;

    /**
     * UPOS property TransmissionProtocol. Default: 0. Must be overwritten until first enabled to match the transmission
     * protocols specified in CapTransmissionProtocol.
     */
    public int TransmissionProtocol = 0;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected SmartCardRWProperties(int dev) {
        super(dev);
        ExclusiveUse = ExclusiveYes;
    }

    @Override
    public void interfaceMode(int mode) throws JposException {
        InterfaceMode = mode;
    }

    @Override
    public void isoEmvMode(int mode) throws JposException {
        IsoEmvMode = mode;
    }

    @Override
    public void sCSlot(int slot) throws JposException {
        SCSlot = slot;
    }

    @Override
    public void beginInsertion(int timeout) throws JposException {
    }

    @Override
    public void beginRemoval(int timeout) throws JposException {
    }

    @Override
    public void endInsertion() throws JposException {
    }

    @Override
    public void endRemoval() throws JposException {
    }

    @Override
    public void readData(int action, int[] count, String[] data) throws JposException {
    }

    @Override
    public WriteData writeData(int action, int count, String data) throws JposException {
        return new WriteData(this, action, count, data);
    }

    @Override
    public void writeData(WriteData request) throws JposException {
    }
}
