/*
 * Copyright 2020 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.pospower;

import de.gmxhome.conrad.jpos.jpos_base.JposBase;
import de.gmxhome.conrad.jpos.jpos_base.JposStatusUpdateEvent;

import static jpos.POSPowerConst.*;

/**
 * Status update event implementation for POSPower devices.
 */
public class POSPowerStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Value for property AdditionalData
     */
    private final int AdditionalData;

    /**
     * Constructor, Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     */
    public POSPowerStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
        AdditionalData = 0;
    }

    /**
     * Constructor, Parameters source and state passed to base class unchanged. Parameter add consists of additional
     * information about the status change, e.g. power source or current battery capacity.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     * @param add    Additional data specifying detail of status change.
     */
    public POSPowerStatusUpdateEvent(JposBase source, int state, int add) {
        super(source, state);
        AdditionalData = add;
    }

    /**
     * Constructor, used by copyEvent to create a copy of this with changed source only.
     * @param source    New event source.
     * @param ev        Event to be copied.
     */
    public POSPowerStatusUpdateEvent(JposBase source, POSPowerStatusUpdateEvent ev) {
        super(source, ev.getStatus());
        AdditionalData = ev.AdditionalData;
    }

    @Override
    public JposStatusUpdateEvent copyEvent(JposBase o) {
        return new POSPowerStatusUpdateEvent(o, this);
    }

    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        POSPowerProperties props = (POSPowerProperties)getPropertySet();
        switch (getStatus()) {
            case PWR_SUE_UPS_FULL -> props.UPSChargeState = PWR_UPS_FULL;
            case PWR_SUE_UPS_WARNING -> props.UPSChargeState = PWR_UPS_WARNING;
            case PWR_SUE_UPS_LOW -> props.UPSChargeState = PWR_UPS_LOW;
            case PWR_SUE_UPS_CRITICAL -> props.UPSChargeState = PWR_UPS_CRITICAL;
            case PWR_SUE_BAT_CAPACITY_REMAINING -> props.BatteryCapacityRemaining = AdditionalData;
            case PWR_SUE_PWR_SOURCE -> props.PowerSource = AdditionalData;
            case PWR_SUE_BAT_CAPACITY_REMAINING_IN_SECONDS -> props.BatteryCapacityRemainingInSeconds = AdditionalData;
            case PWR_SUE_FAN_STOPPED, PWR_SUE_FAN_RUNNING, PWR_SUE_TEMPERATURE_HIGH, PWR_SUE_TEMPERATURE_OK, PWR_SUE_SHUTDOWN,
                    PWR_SUE_BAT_LOW, PWR_SUE_BAT_CRITICAL, PWR_SUE_RESTART, PWR_SUE_STANDBY, PWR_SUE_USER_STANDBY, PWR_SUE_SUSPEND, PWR_SUE_USER_SUSPEND -> {}
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean checkStatusCorresponds() {
        POSPowerProperties props = (POSPowerProperties)getPropertySet();
        return super.checkStatusCorresponds() || switch (getStatus()) {
            case PWR_SUE_UPS_FULL -> props.UPSChargeState == PWR_UPS_FULL;
            case PWR_SUE_UPS_WARNING -> props.UPSChargeState == PWR_UPS_WARNING;
            case PWR_SUE_UPS_LOW -> props.UPSChargeState == PWR_UPS_LOW;
            case PWR_SUE_UPS_CRITICAL -> props.UPSChargeState == PWR_UPS_CRITICAL;
            case PWR_SUE_BAT_CAPACITY_REMAINING -> props.BatteryCapacityRemaining == AdditionalData;
            case PWR_SUE_PWR_SOURCE -> props.PowerSource == AdditionalData;
            case PWR_SUE_BAT_CAPACITY_REMAINING_IN_SECONDS ->
                    props.BatteryCapacityRemainingInSeconds == AdditionalData;
            default -> false;
        };
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        String[] propnames = { "UPSChargeState", "BatteryCapacityRemaining", "PowerSource" };
        Object[] oldvals = getPropertyValues(propnames);
        if (super.setAndCheckStatusProperties())
            return true;
        return propertiesHaveBeenChanged(propnames, oldvals);
    }

    /**
     * Blocks event if derived class would block it and if status is BAT_CRITICAL or BAT_LOW and the corresponding
     * property, BatteryCriticallyLowThreshold or BatteryLowThreshold is 0.
     * @return true If event shall be blocked, otherwise false.
     */
    @Override
    public boolean block() {
        return super.block() ||
                (getStatus() == PWR_SUE_BAT_CRITICAL &&
                        ((POSPowerProperties)getPropertySet()).BatteryCriticallyLowThreshold == 0) ||
                (getStatus() == PWR_SUE_BAT_LOW &&
                        ((POSPowerProperties)getPropertySet()).BatteryLowThreshold == 0);
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        return ret.length() > 0 ? ret : switch (getStatus()) {
            case PWR_SUE_UPS_FULL -> "UPS full";
            case PWR_SUE_UPS_WARNING -> "UPS at 50%";
            case PWR_SUE_UPS_LOW -> "UPS low";
            case PWR_SUE_UPS_CRITICAL -> "UPS critical";
            case PWR_SUE_BAT_CAPACITY_REMAINING -> "Battery capacity at" + AdditionalData + "%";
            case PWR_SUE_PWR_SOURCE -> switch (AdditionalData) {
                case PWR_SOURCE_NA -> "Power source not available";
                case PWR_SOURCE_AC -> "Power source is AC line";
                case PWR_SOURCE_BATTERY -> "Power source is system battery";
                case PWR_SOURCE_BACKUP -> "Power source is backup";
                default -> "Unknown power source: " + AdditionalData;
            };
            case PWR_SUE_FAN_STOPPED -> "Fan stopped";
            case PWR_SUE_FAN_RUNNING -> "Fan running";
            case PWR_SUE_TEMPERATURE_HIGH -> "Temperature high";
            case PWR_SUE_TEMPERATURE_OK -> "Temperature OK";
            case PWR_SUE_SHUTDOWN -> "Shutdown started";
            case PWR_SUE_BAT_LOW -> "Battery low";
            case PWR_SUE_BAT_CRITICAL -> "Battery critical";
            case PWR_SUE_RESTART -> "System restart";
            case PWR_SUE_STANDBY -> "System standby";
            case PWR_SUE_USER_STANDBY -> "User-initiated system standby";
            case PWR_SUE_SUSPEND -> "System suspend";
            case PWR_SUE_USER_SUSPEND -> "User-initiated system suspend";
            case PWR_SUE_BAT_CAPACITY_REMAINING_IN_SECONDS -> "Battery empty in " + AdditionalData + " seconds";
            default -> "Unknown Status Change: " + getStatus();
        };
    }
}
