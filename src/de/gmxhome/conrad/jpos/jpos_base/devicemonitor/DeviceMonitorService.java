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
import jpos.JposException;
import jpos.services.DeviceMonitorService116;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.DeviceMonitorConst.*;
import static jpos.JposConst.*;

/**
 * DeviceMonitor service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class DeviceMonitorService extends JposBase implements DeviceMonitorService116 {
    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public DeviceMonitorService(DeviceMonitorProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    /**
     * Instance of a class implementing the DeviceMonitorInterface for device monitor specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public DeviceMonitorInterface DeviceMonitor;

    private final DeviceMonitorProperties Data;

    @Override
    public String getDeviceData() throws JposException {
        checkEnabled();
        logGet("DeviceData");
        return Data.DeviceData;
    }

    @Override
    public String getDeviceList() throws JposException {
        checkOpened();
        logGet("DeviceList");
        return Data.DeviceList;
    }

    @Override
    public String getMonitoringDeviceList() throws JposException {
        checkEnabled();
        logGet("MonitoringDeviceList");
        return Data.MonitoringDeviceList;
    }

    @Override
    public void addMonitoringDevice(String deviceID, int monitoringMode, int boundary, int subBoundary, int intervalTime) throws JposException {
        long[] valid = {
                DMON_MMODE_UPDATE, DMON_MMODE_STRADDLED, DMON_MMODE_HIGH, DMON_MMODE_LOW,
                DMON_MMODE_WITHIN, DMON_MMODE_OUTSIDE, DMON_MMODE_POLLING
        };
        long[] important = { DMON_MMODE_WITHIN, DMON_MMODE_OUTSIDE };
        logPreCall("AddMonitoringDevice", removeOuterArraySpecifier(new Object[]{deviceID, monitoringMode, boundary, subBoundary, intervalTime}, Device.MaxArrayStringElements));
        checkEnabled();
        String[] alldevs = Data.DeviceList.split(",");
        for (String anydev : alldevs) {
            String[] anycomponents = anydev.split(":");
            if (anycomponents.length > 0 && anycomponents[0].equals(deviceID)) {
                String[] mondevs = Data.MonitoringDeviceList.split(",");
                int index;
                for (index = mondevs.length - 1; index >= 0; --index) {
                    String[] moncomponents = mondevs[index].split(":");
                    if (moncomponents[0].equals(deviceID))
                        break;
                }
                checkMember(monitoringMode, valid, JPOS_E_ILLEGAL, "Illegal monitoringMode: " + monitoringMode);
                check(member(monitoringMode, important) && boundary <= subBoundary,
                        JPOS_E_ILLEGAL, "Invalid boundaries: " + boundary + " <= " + subBoundary + "!");
                check(intervalTime <= 0, JPOS_E_ILLEGAL, "Invalid interval time: " + intervalTime);
                DeviceMonitor.addMonitoringDevice(deviceID, monitoringMode, boundary, subBoundary, intervalTime);
                if (index < 0) {
                    Data.MonitoringDeviceList = Data.MonitoringDeviceList + "," + deviceID + ":" + monitoringMode + ":" + boundary + ":" + subBoundary + ":" + intervalTime;
                } else {
                    mondevs[index] = deviceID + ":" + monitoringMode + ":" + boundary + ":" + subBoundary + ":" + intervalTime;
                    Data.MonitoringDeviceList = String.join(",", mondevs);
                }
                logSet("MonitoringDeviceList");
                logCall("AddMonitoringDevice");
                return;
            }
        }
        throw new JposException(JPOS_E_ILLEGAL, "Invalid device id: " + (deviceID == null ? "(null)" : deviceID));
    }

    @Override
    public void clearMonitoringDevices() throws JposException {
        logPreCall("ClearMonitoringDevices");
        checkEnabled();
        DeviceMonitor.clearMonitoringDevices();
        if (Data.MonitoringDeviceList.length() > 0) {
            Data.MonitoringDeviceList = "";
            logSet("MonitoringDeviceList");
        }
        logCall("ClearMonitoringDevices");
    }

    @Override
    public void deleteMonitoringDevice(String deviceID) throws JposException {
        if (deviceID == null)
            deviceID = "";
        logPreCall("DeleteMonitoringDevice", deviceID);
        checkEnabled();
        String[] devs = Data.MonitoringDeviceList.split(",");
        String newmonlist = "";
        for (int index = 0; index < devs.length; index++) {
            String[] components = devs[index].split(":");
            if (components[0].equals(deviceID)) {
                DeviceMonitor.deleteMonitoringDevice(deviceID);
                while (++index < devs.length)
                    newmonlist = (newmonlist.length() > 0) ? newmonlist + "," + devs[index] : devs[index];
                Data.MonitoringDeviceList = newmonlist;
                logSet("MonitoringDeviceList");
                logCall("DeleteMonitoringDevice");
                return;
            } else
                newmonlist = (newmonlist.length() > 0) ? newmonlist + "," + devs[index] : devs[index];
        }
        throw new JposException(JPOS_E_ILLEGAL, "Invalid device id: " + deviceID);
    }

    @Override
    public void getDeviceValue(String deviceID, int[] pValue) throws JposException {
        if (deviceID == null)
            deviceID = "";
        logPreCall("GetDeviceValue", deviceID);
        checkEnabled();
        String[] devs = Data.DeviceList.split(",");
        for (String dev : devs) {
            String[] components = dev.split(":");
            if (components.length > 0 && components[0].equals(deviceID)) {
                check(pValue == null || pValue.length != 1, JPOS_E_ILLEGAL, "pValue must be int[1]");
                DeviceMonitor.getDeviceValue(deviceID, pValue);
                logSet("MonitoringDeviceList");
                logCall("GetDeviceValue", removeOuterArraySpecifier(new Object[]{deviceID, pValue[0]}, Device.MaxArrayStringElements));
                return;
            }
        }
        throw new JposException(JPOS_E_ILLEGAL, "Invalid device id: " + deviceID);
    }
}
