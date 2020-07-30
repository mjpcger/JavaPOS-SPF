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
import jpos.POSPowerConst;

/**
 * Status update event implementation for POSPower devices.
 */
public class POSPowerStatusUpdateEvent extends JposStatusUpdateEvent {
    private int AdditionalData;

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

    @Override
    public JposStatusUpdateEvent copyEvent(JposBase o) {
        return new POSPowerStatusUpdateEvent(o, getStatus(), AdditionalData);
    }

    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        POSPowerProperties props = (POSPowerProperties)getPropertySet();
        switch (getStatus()) {
            case POSPowerConst.PWR_SUE_UPS_FULL:
                props.UPSChargeState = POSPowerConst.PWR_UPS_FULL;
                return true;
            case POSPowerConst.PWR_SUE_UPS_WARNING:
                props.UPSChargeState = POSPowerConst.PWR_UPS_WARNING;
                return true;
            case POSPowerConst.PWR_SUE_UPS_LOW:
                props.UPSChargeState = POSPowerConst.PWR_UPS_LOW;
                return true;
            case POSPowerConst.PWR_SUE_UPS_CRITICAL:
                props.UPSChargeState = POSPowerConst.PWR_UPS_CRITICAL;
                return true;
            case POSPowerConst.PWR_SUE_BAT_CAPACITY_REMAINING:
                props.BatteryCapacityRemaining = AdditionalData;
                return true;
            case POSPowerConst.PWR_SUE_PWR_SOURCE:
                props.PowerSource = AdditionalData;
                return true;
            case POSPowerConst.PWR_SUE_FAN_STOPPED:
            case POSPowerConst.PWR_SUE_FAN_RUNNING:
            case POSPowerConst.PWR_SUE_TEMPERATURE_HIGH:
            case POSPowerConst.PWR_SUE_TEMPERATURE_OK:
            case POSPowerConst.PWR_SUE_SHUTDOWN:
            case POSPowerConst.PWR_SUE_BAT_LOW:
            case POSPowerConst.PWR_SUE_BAT_CRITICAL:
            case POSPowerConst.PWR_SUE_RESTART:
            case POSPowerConst.PWR_SUE_STANDBY:
            case POSPowerConst.PWR_SUE_USER_STANDBY:
            case POSPowerConst.PWR_SUE_SUSPEND:
            case POSPowerConst.PWR_SUE_USER_SUSPEND:
                return true;
        }
        return false;
    }

    @Override
    public boolean checkStatusCorresponds() {
        if (super.checkStatusCorresponds())
            return true;
        POSPowerProperties props = (POSPowerProperties)getPropertySet();
        switch (getStatus()) {
            case POSPowerConst.PWR_SUE_UPS_FULL:
                return props.UPSChargeState == POSPowerConst.PWR_UPS_FULL;
            case POSPowerConst.PWR_SUE_UPS_WARNING:
                return props.UPSChargeState == POSPowerConst.PWR_UPS_WARNING;
            case POSPowerConst.PWR_SUE_UPS_LOW:
                return props.UPSChargeState == POSPowerConst.PWR_UPS_LOW;
            case POSPowerConst.PWR_SUE_UPS_CRITICAL:
                return props.UPSChargeState == POSPowerConst.PWR_UPS_CRITICAL;
            case POSPowerConst.PWR_SUE_BAT_CAPACITY_REMAINING:
                return props.BatteryCapacityRemaining == AdditionalData;
            case POSPowerConst.PWR_SUE_PWR_SOURCE:
                return props.PowerSource == AdditionalData;
        }
        return false;
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        POSPowerProperties props = (POSPowerProperties)getPropertySet();
        int status = props.UPSChargeState;
        int capacity = props.BatteryCapacityRemaining;
        int source = props.PowerSource;
        if (super.setAndCheckStatusProperties())
            return true;
        if (status != props.UPSChargeState) {
            props.EventSource.logSet("UPSChargeState");
            return true;
        }
        else if (capacity != props.BatteryCapacityRemaining) {
            props.EventSource.logSet("BatteryCapacityRemaining");
            return true;
        }
        else if (source != props.PowerSource) {
            props.EventSource.logSet("PowerSource");
            return true;
        }
        return false;
    }

    /**
     * Blocks event if derived class would block it and if status is BAT_CRITICAL or BAT_LOW and the corresponding
     * property, BatteryCriticallyLowThreshold or BatteryLowThreshold is 0.
     * @return true If event shall be blocked, otherwise false.
     */
    @Override
    public boolean block() {
        return super.block() ||
                (getStatus() == POSPowerConst.PWR_SUE_BAT_CRITICAL &&
                        ((POSPowerProperties)getPropertySet()).BatteryCriticallyLowThreshold == 0) ||
                (getStatus() == POSPowerConst.PWR_SUE_BAT_LOW &&
                        ((POSPowerProperties)getPropertySet()).BatteryLowThreshold == 0);
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        if (ret.length() > 0)
            return ret;
        switch (getStatus()) {
            case POSPowerConst.PWR_SUE_UPS_FULL:
                return "UPS full";
            case POSPowerConst.PWR_SUE_UPS_WARNING:
                return "UPS at 50%";
            case POSPowerConst.PWR_SUE_UPS_LOW:
                return "UPS low";
            case POSPowerConst.PWR_SUE_UPS_CRITICAL:
                return "UPS critical";
            case POSPowerConst.PWR_SUE_BAT_CAPACITY_REMAINING:
                return "Battery capacity at" + AdditionalData + "%";
            case POSPowerConst.PWR_SUE_PWR_SOURCE:
                switch (AdditionalData) {
                    case POSPowerConst.PWR_SOURCE_NA:
                        return "Power source not available";
                    case POSPowerConst.PWR_SOURCE_AC:
                        return "Power source is AC line";
                    case POSPowerConst.PWR_SOURCE_BATTERY:
                        return "Power source is system battery";
                    case POSPowerConst.PWR_SOURCE_BACKUP:
                        return "Power source is backup";
                }
                return "Unknown power source: " + AdditionalData;
            case POSPowerConst.PWR_SUE_FAN_STOPPED:
                return "Fan stopped";
            case POSPowerConst.PWR_SUE_FAN_RUNNING:
                return "Fan running";
            case POSPowerConst.PWR_SUE_TEMPERATURE_HIGH:
                return "Temperature high";
            case POSPowerConst.PWR_SUE_TEMPERATURE_OK:
                return "Temperature OK";
            case POSPowerConst.PWR_SUE_SHUTDOWN:
                return "Shutdown started";
            case POSPowerConst.PWR_SUE_BAT_LOW:
                return "Battery low";
            case POSPowerConst.PWR_SUE_BAT_CRITICAL:
                return "Battery critical";
            case POSPowerConst.PWR_SUE_RESTART:
                return "System restart";
            case POSPowerConst.PWR_SUE_STANDBY:
                return "System standby";
            case POSPowerConst.PWR_SUE_USER_STANDBY:
                return "User-initiated system standby";
            case POSPowerConst.PWR_SUE_SUSPEND:
                return "System suspend";
            case POSPowerConst.PWR_SUE_USER_SUSPEND:
                return "User-initiated system suspend";
        }
        return "Unknown Status Change: " + getStatus();
    }
}
