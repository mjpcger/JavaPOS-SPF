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

import java.util.Arrays;

import static jpos.KeylockConst.*;

/**
 * Status update event implementation for Keylock devices.
 */
public class KeylockStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Value to be stored in ElectronicKey property when firing the event..
     */
    final byte[] ElectronicKeyValue;

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
        ElectronicKeyValue = ev.ElectronicKeyValue == null ? null : Arrays.copyOf(ev.ElectronicKeyValue, ev.ElectronicKeyValue.length);
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
        if (state == LOCK_KP_ELECTRONIC) {
            props.KeyPosition = LOCK_KP_ANY;
            props.ElectronicKeyValue = (ElectronicKeyValue != null) ? ElectronicKeyValue : props.ElectronicKeyValueDef;
        } else {
            if (state > 0 && state <= props.PositionCount)
                props.KeyPosition = state;
            else
                return false;
        }
        props.signalWaiter();
        return true;
    }

    @Override
    public boolean checkStatusCorresponds() {
        if (super.checkStatusCorresponds())
            return true;
        KeylockProperties props = (KeylockProperties) getPropertySet();
        int state = getStatus();
        return state == LOCK_KP_ELECTRONIC ? (props.KeyPosition == LOCK_KP_ANY && props.ElectronicKeyValue == (ElectronicKeyValue != null ?
                ElectronicKeyValue : props.ElectronicKeyValueDef)) :
                state > 0 && state <= props.PositionCount && props.KeyPosition == state;
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        String[] propnames = { "KeyPosition", "ElectronicKeyValue" };
        Object[] oldvals = getPropertyValues(propnames);
        return super.setAndCheckStatusProperties() || propertiesHaveBeenChanged(propnames, oldvals);
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        if (ret.length() > 0)
            return ret;
        try {
            switch (getStatus()) {
            case LOCK_KP_ELECTRONIC:
                return ElectronicKeyValue == null ?
                    "Lock Position Undefined" :
                    "Electronic Key Changed To " + getPropertySet().EventSource.getPropertyString(this, "ElectronicKeyValue");
            case LOCK_KP_LOCK:
                return "Key Position: Locked";
            case LOCK_KP_NORM:
                return "Key Position: Normal";
            case LOCK_KP_SUPR:
                return "Key Position: Supervisor";
            default:
                return "Key position: " + getStatus();
            }
        } catch (Exception e) {
            return "Electronic Key Changed To [Error: " + e.getMessage() + "]";
        }
    }
}
