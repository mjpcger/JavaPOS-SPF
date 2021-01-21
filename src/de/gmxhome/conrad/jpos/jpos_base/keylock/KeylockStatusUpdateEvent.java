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

import java.util.Arrays;

/**
 * Status update event implementation for Keylock devices.
 */
public class KeylockStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Value to be stored in ElectronicKey property when firing the event..
     */
    byte[] ElectronicKeyValue;

    /**
     * Constructor, see JposStatusUpdateEvent
     *
     * @param source Source, for services implemented with this framework, the (keylock.)KeylockService object.
     * @param state  New status value, see UPOS specification, chapter Keylock - Events - StatusUpdateEvent.
     * @param ekey   Electronic key value, if Status = 0
     */
    public KeylockStatusUpdateEvent(JposBase source, int state, byte[] ekey) {
        super(source, state);
        ElectronicKeyValue = ekey;
    }

    /**
     * Constructor, used by copyEvent to create a copy of this with changed source only.
     * @param source    New event source.
     * @param ev        Event to be copied.
     */
    public KeylockStatusUpdateEvent(JposBase source, KeylockStatusUpdateEvent ev) {
        super(source, ev.getStatus());
        if (ev.ElectronicKeyValue != null)
            ElectronicKeyValue = Arrays.copyOf(ev.ElectronicKeyValue, ev.ElectronicKeyValue.length);
    }

    @Override
    public JposStatusUpdateEvent copyEvent(JposBase o) {
        return new KeylockStatusUpdateEvent(o, this);
    }

    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        KeylockProperties props = (KeylockProperties) getPropertySet();
        int state = getStatus();
        switch (state) {
            case KeylockConst.LOCK_KP_ELECTRONIC:
                props.KeyPosition = KeylockConst.LOCK_KP_ANY;
                props.ElectronicKeyValue = (ElectronicKeyValue != null) ? ElectronicKeyValue : props.ElectronicKeyValueDef;
                props.signalWaiter();
                return true;
            default:
                if (state > 0 && state <= props.PositionCount) {
                    props.KeyPosition = state;
                    props.signalWaiter();
                    return true;
                }
        }
        return false;
    }

    @Override
    public boolean checkStatusCorresponds() {
        if (super.checkStatusCorresponds())
            return true;
        KeylockProperties props = (KeylockProperties) getPropertySet();
        int state = getStatus();
        switch (state) {
            case KeylockConst.LOCK_KP_ELECTRONIC:
                return props.KeyPosition == KeylockConst.LOCK_KP_ANY && props.ElectronicKeyValue == (ElectronicKeyValue != null ? ElectronicKeyValue : props.ElectronicKeyValueDef);
            default:
                if (state > 0 && state <= props.PositionCount)
                    return props.KeyPosition == state;
        }
        return false;
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        String[] propnames = {
                "KeyPosition",
                "ElectronicKeyValue"
        };
        Object[] oldvals = getPropertyValues(propnames);
        if (super.setAndCheckStatusProperties())
            return true;
        return propertiesHaveBeenChanged(propnames, oldvals);
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        if (ret.length() > 0)
            return ret;
        switch (getStatus()) {
            case KeylockConst.LOCK_KP_ELECTRONIC:
                try {
                    return ElectronicKeyValue == null ? "Lock Position Undefined" : "Electronic Key Changed To " + getPropertySet().EventSource.getPropertyString(this, "ElectronicKeyValue");
                } catch (Exception e) {
                    return "Electronic Key Changed To [Error: " + e.getMessage() + "]";
                }
            case KeylockConst.LOCK_KP_LOCK:
                return "Key Position: Locked";
            case KeylockConst.LOCK_KP_NORM:
                return "Key Position: Normal";
            case KeylockConst.LOCK_KP_SUPR:
                return "Key Position: Supervisor";
        }
        return "Key position: " + getStatus();
    }
}
