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

import jpos.events.*;

import java.lang.reflect.*;
import java.util.*;

import static jpos.JposConst.*;

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
     * Creates copy of given event for a different property set. Needed for shareable devices only.
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
        case JPOS_SUE_POWER_OFF:
            props.PowerState = JPOS_PS_OFF;
            break;
        case JPOS_SUE_POWER_OFF_OFFLINE:
            props.PowerState = JPOS_PS_OFF_OFFLINE;
            break;
        case JPOS_SUE_POWER_OFFLINE:
            props.PowerState = JPOS_PS_OFFLINE;
            break;
        case JPOS_SUE_POWER_ONLINE:
            props.PowerState = JPOS_PS_ONLINE;
            break;
        case JPOS_SUE_UF_COMPLETE:
        case JPOS_SUE_UF_COMPLETE_DEV_NOT_RESTORED:
        case JPOS_SUE_UF_FAILED_DEV_NEEDS_FIRMWARE:
        case JPOS_SUE_UF_FAILED_DEV_OK:
        case JPOS_SUE_UF_FAILED_DEV_UNKNOWN:
        case JPOS_SUE_UF_FAILED_DEV_UNRECOVERABLE:
            return true;
        default:
            if (state > JPOS_SUE_UF_PROGRESS && state <= JPOS_SUE_UF_PROGRESS + 100)
                return true;
            if (Objects.equals(state, props.FlagWhenIdleStatusValue)) {
                props.FlagWhenIdle = false;
                return true;
            }
            return false;
        }
        return true;
    }

    /**
     * Sets status properties and checks whether status properties have been changed. If so, logs status change.
     * @return  true if state might have been really changed, false otherwise.
     */
    public boolean setAndCheckStatusProperties() {
        String[] properties = {"PowerState", "FlagWhenIdle"};
        Object[] oldproperties = getPropertyValues(properties);
        if (!setStatusProperties())
            return false;
        if (propertiesHaveBeenChanged(properties, oldproperties))
            return true;
        int state = getStatus();
        switch (state) {
        case JPOS_SUE_UF_COMPLETE:
        case JPOS_SUE_UF_COMPLETE_DEV_NOT_RESTORED:
        case JPOS_SUE_UF_FAILED_DEV_NEEDS_FIRMWARE:
        case JPOS_SUE_UF_FAILED_DEV_OK:
        case JPOS_SUE_UF_FAILED_DEV_UNKNOWN:
        case JPOS_SUE_UF_FAILED_DEV_UNRECOVERABLE:
            return true;
        }
        return state > JPOS_SUE_UF_PROGRESS && state <= JPOS_SUE_UF_PROGRESS + 100;
    }

    /**
     * Checks whether properties specified by name have been changed. Works for all basic type properties (String,
     * int, long) and array type properties (byte[]).
     * @param properties    Array of property names.
     * @param oldproperties Array of the corresponding property values before possible change.
     * @return true if at least one of the given property values differs from its corresponding current value.
     */
    public boolean propertiesHaveBeenChanged(String[] properties, Object[] oldproperties) {
        JposCommonProperties props = getPropertySet();
        Object[] newproperties = getPropertyValues(properties);
        boolean result = false;
        for (int i = 0; i < newproperties.length; i++) {
            if (!compare(oldproperties[i], newproperties[i])) {
                props.EventSource.logSet(properties[i]);
                result = true;
            }
        }
        return result;
    }

    private boolean compare(Object first, Object second) {
        if (first == null || second == null)
            return first == second;
        Class<?> firstclass = first.getClass();
        if (!firstclass.isArray())
            return first.equals(second);
        int count;
        if (!second.getClass().equals(firstclass) || (count = Array.getLength(first)) != Array.getLength(second))
            return false;
        if (--count >= 0) {
            if (first instanceof Object[])
                return Arrays.equals((Object[]) first, (Object[]) second);
            try {
                return arrayEquals(firstclass, first, second);
            } catch (Exception e) {
                do {
                    if (!Array.get(first, count).equals(Array.get(second, count)))
                        return false;
                } while (--count >= 0);
            }
        }
        return true;
    }

    private static final Map<Class<?>,Method> arrayComparators = new HashMap<>();

    private boolean arrayEquals(Class<?> theClass, Object first, Object second) throws Exception {
        Method comparator = arrayComparators.get(theClass);
        boolean result;
        if (comparator == null) {
            comparator = Arrays.class.getMethod("equals", theClass, theClass);
            result = (Boolean) comparator.invoke(null, first, second);
            arrayComparators.put(theClass, comparator);
        } else
            result = (Boolean) comparator.invoke(null, first, second);
        return result;
    }

    /**
     * Retrieves the values of a given set of properties. The names of the properties to be retrieved will be passed
     * as an array of String. The corresponding values will be returned in an array of Object with the same size (or
     * size 0 if properties is null), in the same order. For example, if properties is {"Status", "Result"}, the returned
     * array is { <i>value of Status</i>, <i>value of Result</i>}. If a property does not exist, "[error]" will be
     * stored in the corresponding place within the returned array.
     * @param properties Array of names of properties to be retrieved.
     * @return Array of property values.
     */
    public Object[] getPropertyValues(String[] properties) {
        JposCommonProperties props = getPropertySet();
        Object[] result = new Object[properties == null ? 0 : properties.length];
        for (int i = 0; i < result.length; i++) {
            try {
                result[i] = props.EventSource.getClass().getMethod("get" + properties[i]).invoke(props.EventSource);
            } catch (Exception e) {
                result[i] = "[error]";
            }
        }
        return result;
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
        case JPOS_SUE_POWER_OFF:
            return props.PowerState == JPOS_PS_OFF;
        case JPOS_SUE_POWER_OFF_OFFLINE:
            return props.PowerState == JPOS_PS_OFF_OFFLINE;
        case JPOS_SUE_POWER_OFFLINE:
            return props.PowerState == JPOS_PS_OFFLINE;
        case JPOS_SUE_POWER_ONLINE:
            return props.PowerState == JPOS_PS_ONLINE;
        case JPOS_SUE_UF_COMPLETE:
        case JPOS_SUE_UF_COMPLETE_DEV_NOT_RESTORED:
        case JPOS_SUE_UF_FAILED_DEV_NEEDS_FIRMWARE:
        case JPOS_SUE_UF_FAILED_DEV_OK:
        case JPOS_SUE_UF_FAILED_DEV_UNKNOWN:
        case JPOS_SUE_UF_FAILED_DEV_UNRECOVERABLE:
            return true;
        }
        return state > JPOS_SUE_UF_PROGRESS && state <= JPOS_SUE_UF_PROGRESS + 100;
    }

    /**
     * Checks whether event must be blocked due to missing or delayed status support.
     * Currently power status update events will be blocked if PowerNotify
     * is disabled.
     * @return true if event must be blocked.
     */
    public boolean block() {
        JposCommonProperties props = getPropertySet();
        if (props.PowerNotify == JPOS_PN_DISABLED) {
            switch (getStatus()) {
            case JPOS_SUE_POWER_OFF:
            case JPOS_SUE_POWER_OFF_OFFLINE:
            case JPOS_SUE_POWER_OFFLINE:
            case JPOS_SUE_POWER_ONLINE:
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
        case JPOS_SUE_POWER_OFF:
            return "Device Off";
        case JPOS_SUE_POWER_OFF_OFFLINE:
            return "Device Off or Offline";
        case JPOS_SUE_POWER_OFFLINE:
            return "Device Offline";
        case JPOS_SUE_POWER_ONLINE:
            return "Device Online";
        case JPOS_SUE_UF_COMPLETE:
            return "Firmware Update Complete, Device Usable";
        case JPOS_SUE_UF_COMPLETE_DEV_NOT_RESTORED:
            return "Firmware update Complete, Device Reset";
        case JPOS_SUE_UF_FAILED_DEV_NEEDS_FIRMWARE:
            return "Device Needs Firmware Update";
        case JPOS_SUE_UF_FAILED_DEV_OK:
            return "Firmware Update Failed, Device Remains Usable";
        case JPOS_SUE_UF_FAILED_DEV_UNKNOWN:
            return "Firmware Update Failed, Device Status Unknown";
        case JPOS_SUE_UF_FAILED_DEV_UNRECOVERABLE:
            return "Firmware Update Failed, Device Not Working";
        default:
            if (state > JPOS_SUE_UF_PROGRESS && state <= JPOS_SUE_UF_PROGRESS + 100)
                return "Firmware Update (" + (state - JPOS_SUE_UF_PROGRESS) + "%)";
            if (Objects.equals(state, getPropertySet().FlagWhenIdleStatusValue))
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
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}
