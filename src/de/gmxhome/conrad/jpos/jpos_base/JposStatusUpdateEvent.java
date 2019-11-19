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

package de.gmxhome.conrad.jpos.jpos_base;

import jpos.JposConst;
import jpos.events.StatusUpdateEvent;

import java.lang.reflect.Field;

/**
 * Status update event with method to fill status properties.
 * The default implementation for event handling does not handle instances of StatusUpdateEvent, it handles only
 * instances of JposStatusUpdateEvent instead.
 */
public class JposStatusUpdateEvent extends StatusUpdateEvent {
    /**
     * Constructor, Parameters passed to base class unchanged.
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state Status, see UPOS specification.
     */
    public JposStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
    }

    /**
     * Creates copy of given event for a different property set. Needed for shareable devices.
     * @param source    Source (JposDevice) of the event
     * @return Copy of the event to be handled by the given source.
     */
    public JposStatusUpdateEvent copyEvent(JposBase source) {
        return new JposStatusUpdateEvent(source, getStatus());
    }

    /**
     * Set status properties to reflect the given status. These properties must be set immediately before the event
     * is buffered into the event queue.
     * @return true, if properties have been updated, false otherwise
     */
    public boolean setStatusProperties() {
        JposCommonProperties props = getPropertySet();
        int state = getStatus();
        switch (state) {
            case JposConst.JPOS_SUE_POWER_OFF:
                props.PowerState = JposConst.JPOS_PS_OFF;
                return true;
            case JposConst.JPOS_SUE_POWER_OFF_OFFLINE:
                props.PowerState = JposConst.JPOS_PS_OFF_OFFLINE;
                return true;
            case JposConst.JPOS_SUE_POWER_OFFLINE:
                props.PowerState = JposConst.JPOS_PS_OFFLINE;
                return true;
            case JposConst.JPOS_SUE_POWER_ONLINE:
                props.PowerState = JposConst.JPOS_PS_ONLINE;
                return true;
            case JposConst.JPOS_SUE_UF_COMPLETE:
            case JposConst.JPOS_SUE_UF_COMPLETE_DEV_NOT_RESTORED:
            case JposConst.JPOS_SUE_UF_FAILED_DEV_NEEDS_FIRMWARE:
            case JposConst.JPOS_SUE_UF_FAILED_DEV_OK:
            case JposConst.JPOS_SUE_UF_FAILED_DEV_UNKNOWN:
            case JposConst.JPOS_SUE_UF_FAILED_DEV_UNRECOVERABLE:
                return true;
            default:
                if (state > JposConst.JPOS_SUE_UF_PROGRESS && state <= JposConst.JPOS_SUE_UF_PROGRESS + 100)
                    return true;
        }
        return false;
    }

    /**
     * Sets status properties and checks whether status properties have been changed. If so, logs status change.
     * @return  true if state might have been really changed, false otherwise.
     */
    public boolean setAndCheckStatusProperties() {
        JposCommonProperties props = getPropertySet();
        int state = props.PowerState;
        if (!setStatusProperties())
            return false;
        if (state != props.PowerState) {
            props.EventSource.logSet("PowerState");
            return true;
        }
        switch (getStatus()) {
            case JposConst.JPOS_SUE_UF_COMPLETE:
            case JposConst.JPOS_SUE_UF_COMPLETE_DEV_NOT_RESTORED:
            case JposConst.JPOS_SUE_UF_FAILED_DEV_NEEDS_FIRMWARE:
            case JposConst.JPOS_SUE_UF_FAILED_DEV_OK:
            case JposConst.JPOS_SUE_UF_FAILED_DEV_UNKNOWN:
            case JposConst.JPOS_SUE_UF_FAILED_DEV_UNRECOVERABLE:
                return true;
            default:
                if (state > JposConst.JPOS_SUE_UF_PROGRESS && state <= JposConst.JPOS_SUE_UF_PROGRESS + 100)
                    return true;
        }
        return false;
    }

    /**
     * Sets those properties that shall be set immediately before the event will be fired to the application. Should be
     * used for properties with a relationship to the event. For pure status values, use method setStatusProperties
     * instead.
     */
    public void setLateProperties() {
    }

    /**
     * Checks if the device state corresponds to the event status.
     * @return  true if device state corresponds to event state, false otherwise.
     */
    public boolean checkStatusCorresponds() {
        JposCommonProperties props = getPropertySet();
        int state = getStatus();
        switch (state) {
            case JposConst.JPOS_SUE_POWER_OFF:
                return props.PowerState == JposConst.JPOS_PS_OFF;
            case JposConst.JPOS_SUE_POWER_OFF_OFFLINE:
                return props.PowerState == JposConst.JPOS_PS_OFF_OFFLINE;
            case JposConst.JPOS_SUE_POWER_OFFLINE:
                return props.PowerState == JposConst.JPOS_PS_OFFLINE;
            case JposConst.JPOS_SUE_POWER_ONLINE:
                return props.PowerState == JposConst.JPOS_PS_ONLINE;
            case JposConst.JPOS_SUE_UF_COMPLETE:
            case JposConst.JPOS_SUE_UF_COMPLETE_DEV_NOT_RESTORED:
            case JposConst.JPOS_SUE_UF_FAILED_DEV_NEEDS_FIRMWARE:
            case JposConst.JPOS_SUE_UF_FAILED_DEV_OK:
            case JposConst.JPOS_SUE_UF_FAILED_DEV_UNKNOWN:
            case JposConst.JPOS_SUE_UF_FAILED_DEV_UNRECOVERABLE:
                return true;
            default:
                if (state > JposConst.JPOS_SUE_UF_PROGRESS && state <= JposConst.JPOS_SUE_UF_PROGRESS + 100)
                    return true;
        }
        return false;
    }

    /**
     * Checks whether event must be blocked due to missing status support or .
     * Currently only power status update events will be blocked if PowerNotify
     * is disabled.
     * @return true if event must be blocked.
     */
    public boolean block() {
        JposCommonProperties props = getPropertySet();
        if (props.PowerNotify == JposConst.JPOS_PN_DISABLED) {
            switch (getStatus()) {
                case JposConst.JPOS_SUE_POWER_OFF:
                case JposConst.JPOS_SUE_POWER_OFF_OFFLINE:
                case JposConst.JPOS_SUE_POWER_OFFLINE:
                case JposConst.JPOS_SUE_POWER_ONLINE:
                    return true;
            }
        }
        return false;
    }

    /**
     * Generates string describing the data event for logging purposes.
     * @return Describing string.
     */
    public String toLogString() {
        int state = getStatus();
        switch (state) {
            case JposConst.JPOS_SUE_POWER_OFF:
                return "Device Off";
            case JposConst.JPOS_SUE_POWER_OFF_OFFLINE:
                return "Device Off or Offline";
            case JposConst.JPOS_SUE_POWER_OFFLINE:
                return "Device Offline";
            case JposConst.JPOS_SUE_POWER_ONLINE:
                return "Device Online";
            case JposConst.JPOS_SUE_UF_COMPLETE:
                return "Firmware Update Complete, Device Usable";
            case JposConst.JPOS_SUE_UF_COMPLETE_DEV_NOT_RESTORED:
                return "Firmware update Complete, Device Reset";
            case JposConst.JPOS_SUE_UF_FAILED_DEV_NEEDS_FIRMWARE:
                return "Device Needs Firmware Update";
            case JposConst.JPOS_SUE_UF_FAILED_DEV_OK:
                return "Firmware Update Failed, Device Remains Usable";
            case JposConst.JPOS_SUE_UF_FAILED_DEV_UNKNOWN:
                return "Firmware Update Failed, Device Status Unknown";
            case JposConst.JPOS_SUE_UF_FAILED_DEV_UNRECOVERABLE:
                return "Firmware Update Failed, Device Not Working";
            default:
                if (state > JposConst.JPOS_SUE_UF_PROGRESS && state <= JposConst.JPOS_SUE_UF_PROGRESS + 100)
                    return "Firmware Update (" + (state - JposConst.JPOS_SUE_UF_PROGRESS) + "%)";
                if (state == getPropertySet().FlagWhenIdleStatusValue && state != 0)
                    return "Device idle";
        }
        return "";
    }

    /**
     * Returns property set bound to the event source.
     * @return Property set.
     */
    public JposCommonProperties getPropertySet() {
        return ((JposBase) getSource()).Props;
    }

    /**
     * Sets Status property to the given value. ATTENTION: This implementation might fail if jpos114.jar is replaced by
     * a newer version when the name of the protected status field changes.
     * @param newstate New status value.
     */
    public void setStatus(int newstate) {
        try {
            Field field = this.getClass().getSuperclass().getDeclaredField("status");
            field.setAccessible(true);
            field.setInt(this, newstate);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
