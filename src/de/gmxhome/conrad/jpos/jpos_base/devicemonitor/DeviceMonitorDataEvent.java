/*
 * Copyright 2023 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.devicemonitor;

import de.gmxhome.conrad.jpos.jpos_base.*;

/**
 * Data event implementation for DeviceMonitor devices.
 */
@SuppressWarnings("unused")
public class DeviceMonitorDataEvent extends JposDataEvent {
    /**
     * DeviceData contents belonging to this data event.
     * @return DeviceData in the form of "deviceID: value" as specified in the constructor.
     */
    public String getDeviceData() {
        return DeviceData;
    }

    /**
     * Device data as specified within the UPOS specification.
     */
    private final String DeviceData;

    /**
     * Constructor. Parameters passed to base class unchanged.
     *
     * @param source   Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state    Status, see UPOS specification.
     * @param deviceID Device id of the device value belongs to.
     * @param value    Value retrieved from device specified by deviceID.
     */
    public DeviceMonitorDataEvent(JposBase source, int state, String deviceID, int value) {
        super(source, state);
        DeviceData = deviceID + ":" + value;
    }

    @Override
    public void setDataProperties() {
        super.setDataProperties();
        ((DeviceMonitorProperties)(((DeviceMonitorService)getSource()).Props)).DeviceData = DeviceData;
    }
}
