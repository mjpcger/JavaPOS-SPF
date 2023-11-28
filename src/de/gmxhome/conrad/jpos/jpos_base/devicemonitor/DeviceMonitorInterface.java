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

import de.gmxhome.conrad.jpos.jpos_base.JposBaseInterface;
import jpos.JposException;

/**
 * Interface for methods that implement property setter and method calls for the DeviceMonitor device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Device Monitor.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface DeviceMonitorInterface extends JposBaseInterface {
    /**
     * Final part of AddMonitoringDevice method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>deviceID is one of the device ids specified in property DeviceList,</li>
     *     <li>monitoringMode is one of MMODE_UPDATE, MMODE_STRADDLED, MMODE_HIGH, MMODE_LOW, MMODE_WITHIN,
     *     MMODE_OUTSIDE or MMODE_POLLING,</li>
     *     <li>If monitorMode is MMODE_WITHIN or MMODE_OUTSIDE, boundary is greater than subBoundary,</li>
     *     <li>intervalTime is greater than zero.</li>
     * </ul>
     * If successful, DeviceMonitorService will update property MonitoringDeviceList to match the specified values.
     *
     * @param deviceID       The deviceID of the monitored device.
     * @param monitoringMode The monitoring mode.
     * @param boundary       (Upper) boundary value to be monitored, if necessary for the specified monitoring mode.
     * @param subBoundary    Lower boundary value to be monitored, if necessary for the specified monitoring mode.
     * @param intervalTime   Monitoring interval in milliseconds.
     * @throws JposException    If an error occurs.
     */
    void addMonitoringDevice(String deviceID, int monitoringMode, int boundary, int subBoundary, int intervalTime) throws JposException;

    /**
     * Final part of ClearMonitoringDevices method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     * If successful, DeviceMonitorService will be reset to "".
     *
     * @throws JposException    If an error occurs.
     */
    void clearMonitoringDevices() throws JposException;

    /**
     * Final part of DeleteMonitoringDevice method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>deviceID is one of the device ids specified in property MonitoringDeviceList.</li>
     * </ul>
     * If successful, DeviceMonitorService will update property MonitoringDeviceList.
     *
     * @param deviceID       The deviceID of the monitored device.
     * @throws JposException    If an error occurs.
     */
    void deleteMonitoringDevice(String deviceID) throws JposException;

    /**
     * Final part of AddMarker method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>deviceID is one of the device ids specified in property DeviceList,</li>
     *     <li>pValue is a reference to an int[1].</li>
     * </ul>
     *
     * @param deviceID  The deviceID of the device.
     * @param pValue    Pointer that stores measurement value.
     * @throws JposException    If an error occurs.
     */
    void getDeviceValue(String deviceID, int[] pValue) throws JposException;
}
