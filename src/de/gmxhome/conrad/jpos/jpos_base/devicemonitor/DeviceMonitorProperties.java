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

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import jpos.JposException;

/**
 * Class containing the device monitor specific properties, their default values and default implementations of
 * DeviceMonitorInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Device Monitor.
 */
public class DeviceMonitorProperties extends JposCommonProperties implements DeviceMonitorInterface {
    /**
     * Default value of DeviceData property. Default: "".
     */
    public String DeviceData = "";

    /**
     * Default value of DeviceList property. Default: null. Must be overwritten
     * by objects derived from JposDevice within the changeDefaults method at the latest.
     */
    public String DeviceList = null;

    /**
     * Default value of MonitoringDeviceList property. Default: "". Will be controlled by DeviceMonitorService and must
     * not be changed by specific DeviceMonitor implementations.
     */
    public String MonitoringDeviceList = "";

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected DeviceMonitorProperties(int dev) {
        super(dev);
        DeviceServiceVersion = 1016000;
    }

    @Override
    public void addMonitoringDevice(String deviceID, int monitoringMode, int boundary, int subBoundary, int intervalTime) throws JposException {
    }

    @Override
    public void clearMonitoringDevices() throws JposException {
    }

    @Override
    public void deleteMonitoringDevice(String deviceID) throws JposException {
    }

    @Override
    public void getDeviceValue(String deviceID, int[] pValue) throws JposException {
    }
}
