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

package de.gmxhome.conrad.jpos.jpos_base.keylock;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.services.*;

import java.util.Arrays;

/**
 * Keylock service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class KeylockService extends JposBase implements KeylockService115 {
    /**
     * Instance of a class implementing the KeylockInterface for keylock specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public KeylockInterface KeylockInterface;

    private KeylockProperties Data;

    /**
     * Constructor. Stores property set and device driver implementation
     *
     * @param props  Device service property set.
     * @param device Device driver implementation.
     */
    public KeylockService(KeylockProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public int getCapKeylockType() throws JposException {
        checkOpened();
        logGet("CapKeylockType");
        return Data.CapKeylockType;
    }

    @Override
    public byte[] getElectronicKeyValue() throws JposException {
        checkEnabled();
        logGet("ElectronicKeyValue");
        return Arrays.copyOf(Data.ElectronicKeyValue, Data.ElectronicKeyValue.length);
    }

    @Override
    public int getKeyPosition() throws JposException {
        checkEnabled();
        logGet("KeyPosition");
        return Data.KeyPosition;
    }

    @Override
    public int getPositionCount() throws JposException {
        checkOpened();
        logGet("PositionCount");
        return Data.PositionCount;
    }

    @Override
    public void waitForKeylockChange(int i, int i1) throws JposException {
        logPreCall("WaitForKeylockChange", "" + i + ", " + i1);
        checkEnabled();
        Device.check(i < KeylockConst.LOCK_KP_ANY || i > Data.PositionCount, JposConst.JPOS_E_ILLEGAL, "Key position out of range: "+ i);
        Device.check(i1 < 0 && i1 != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + i1);
        KeylockInterface.waitForKeylockChange(i, i1);
        logCall("WaitForKeylockChange");
    }
}
