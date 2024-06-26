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

package SampleDeviceMonitor;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.devicemonitor.*;
import jpos.*;

import java.math.*;
import java.nio.file.*;
import java.util.*;

import static de.gmxhome.conrad.jpos.jpos_base.SyncObject.INFINITE;
import static java.math.RoundingMode.*;
import static jpos.DeviceMonitorConst.*;
import static jpos.JposConst.*;

/**
 * JposDevice based implementation of JavaPOS DeviceMonitor device service implementation for the mass storage devices.
 */
public class Device extends JposDevice {
    /**
     * The device implementation. See parent for further details.
     * @param id  Device ID, not used by implementation.
     */
    protected Device(String id) {
        super(id);
        deviceMonitorInit(1);
        PhysicalDeviceDescription = "Device Monitor For Mass Storage Devices";
        PhysicalDeviceName = "Mass Storage Device Monitor";
        CapPowerReporting = JPOS_PR_NONE;
    }

    @Override
    public void changeDefaults(DeviceMonitorProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "Device Monitor For Mass Storage Devices";
    }

    @Override
    public DeviceMonitorProperties getDeviceMonitorProperties(int index) {
        return new MyProperties();
    }

    private class MyProperties extends DeviceMonitorProperties {
        Map<String, FileStore> Stores = new HashMap<>();
        MyProperties() {
            super(0);
            StringBuilder list = new StringBuilder();
            for (FileStore store : FileSystems.getDefault().getFileStores()) {
                String name = store.name().replaceAll("[:,]", "");
                if (name.length() > 0) {
                    for (int i = 1; ; i++) {
                        String key = i == 1 ? name : name + i;
                        if (Stores.containsKey(key))
                            continue;
                        Stores.put(name = key, store);
                        break;
                    }
                    list.append(",").append(name).append(":Mass Storage:Percent Free:10000000");
                }
            }
            DeviceList = list.length() > 1 ? list.substring(1) : "";
        }

        @Override
        public void getDeviceValue(String deviceID, int[] pValue) throws JposException {
            FileStore fs = Stores.get(deviceID);
            check(fs == null, JPOS_E_ILLEGAL, "Invalid device ID: " + deviceID);
            try {
                BigDecimal capacity = new BigDecimal(fs.getTotalSpace());
                BigDecimal free = new BigDecimal(fs.getUsableSpace());
                pValue[0] = free.movePointRight(9).divide(capacity, HALF_EVEN).intValue();
            } catch (Exception e) {
                throw new JposException(JPOS_E_FAILURE, e.getMessage(), e);
            }
        }

        private class DeviceWatcher extends Thread {
            int Mode, Upper, Lower, Time;
            String ID;
            FileStore Storage;
            SyncObject Waiting = new SyncObject();
            long WaitingTime;
            DeviceWatcher(String id, int mode, int upper, int lower, int time, FileStore fs) {
                setName("DeviceWatcher-" + (ID = id));
                Mode = mode;
                Upper = upper;
                Lower = lower;
                WaitingTime = Time = time;
                Storage = fs;
            }

            @Override
            public void run() {
                Integer oldvalue = null;
                long nextcheck = System.currentTimeMillis();
                while(true) {
                    int mode, upper, lower;
                    SyncObject waitobj;
                    long waittime, now;
                    synchronized (Watchers) {
                        mode = Mode;
                        upper = Upper;
                        lower = Lower;
                        waittime = WaitingTime;
                        if ((waitobj = Waiting) == null)
                            break;      // Termination condition
                    }
                    now = System.currentTimeMillis();
                    if (waittime != INFINITE) {
                        if (nextcheck - now <= 0) {
                            try {
                                BigDecimal capacity = new BigDecimal(Storage.getTotalSpace());
                                BigDecimal free = new BigDecimal(Storage.getUsableSpace());
                                int value = free.movePointRight(9).divide(capacity, HALF_EVEN).intValue();
                                JposDataEvent ev = new DeviceMonitorDataEvent(EventSource, 0, ID, value);
                                switch (mode) {
                                    case DMON_MMODE_UPDATE -> {
                                        if (oldvalue == null || oldvalue != value)
                                            handleEvent(ev);
                                    }
                                    case DMON_MMODE_STRADDLED -> {
                                        if (oldvalue != null) {
                                            if ((oldvalue > upper) != (value > upper))
                                                handleEvent(ev);
                                        }
                                    }
                                    case DMON_MMODE_HIGH -> {
                                        if (value >= upper)
                                            handleEvent(ev);
                                    }
                                    case DMON_MMODE_LOW -> {
                                        if (value <= upper)
                                            handleEvent(ev);
                                    }
                                    case DMON_MMODE_WITHIN -> {
                                        if (value <= upper && value >= lower)
                                            handleEvent(ev);
                                    }
                                    case DMON_MMODE_OUTSIDE -> {
                                        if (value > upper || value < lower)
                                            handleEvent(ev);
                                    }
                                    case DMON_MMODE_POLLING -> handleEvent(ev);
                                }
                                oldvalue = value;
                            } catch (Exception ignored) {}
                            nextcheck = System.currentTimeMillis() + waittime;
                        } else {
                            waittime = nextcheck - now;
                        }
                    }
                    waitobj.suspend(waittime);
                }
            }
        }

        private final Map<String, DeviceWatcher> Watchers = new HashMap<>();

        @Override
        public void addMonitoringDevice(String deviceID, int monitoringMode, int boundary, int subBoundary, int intervalTime) throws JposException {
            long[] checkvals = { DMON_MMODE_UPDATE, DMON_MMODE_POLLING };
            check(member(monitoringMode, checkvals) && boundary > 1000000000, JPOS_E_ILLEGAL, "Boundary too high: " + boundary);
            boolean starting = false;
            synchronized (Watchers) {
                DeviceWatcher current;
                if ((current = Watchers.get(deviceID)) != null) {
                    current.Mode = monitoringMode;
                    current.Upper = boundary;
                    current.Lower = subBoundary;
                    current.WaitingTime = current.Time = intervalTime;
                    current.Waiting.signal();
                } else {
                    current = new DeviceWatcher(deviceID, monitoringMode, boundary, subBoundary, intervalTime, Stores.get(deviceID));
                    current.start();
                    Watchers.put(deviceID, current);
                    if (Watchers.size() == 1)
                        starting = true;
                }
            }
            if (starting)
                handleEvent(new DeviceMonitorStatusUpdateEvent(EventSource, DMON_SUE_START_MONITORING));
        }

        @Override
        public void clearMonitoringDevices() throws JposException {
            synchronized (Watchers) {
                for (DeviceWatcher current : Watchers.values()) {
                    current.Waiting.signal();
                    current.Waiting = null;
                }
                Watchers.clear();
            }
            handleEvent(new DeviceMonitorStatusUpdateEvent(EventSource, DMON_SUE_STOP_MONITORING));
        }

        @Override
        public void deleteMonitoringDevice(String deviceID) throws JposException {
            boolean last = false;
            synchronized (Watchers) {
                DeviceWatcher current = Watchers.get(deviceID);
                if (current != null) {
                    Watchers.remove(deviceID);
                    current.Waiting.signal();
                    current.Waiting = null;
                    last = Watchers.size() == 0;
                }
            }
            if (last)
                handleEvent(new DeviceMonitorStatusUpdateEvent(EventSource, DMON_SUE_STOP_MONITORING));
        }

        @Override
        public void deviceEnabled(boolean flag) throws JposException {
            if (!flag) {
                synchronized (Watchers) {
                    for (DeviceWatcher current : Watchers.values()) {
                        current.WaitingTime = INFINITE;
                        current.Waiting.signal();
                    }
                }
            }
            super.deviceEnabled(flag);
            if (flag) {
                synchronized (Watchers) {
                    for (DeviceWatcher current : Watchers.values()) {
                        current.WaitingTime = current.Time;
                        current.Waiting.signal();
                    }
                }
            }
        }

        @Override
        public void release() throws JposException {
            synchronized (Watchers) {
                for (DeviceWatcher current : Watchers.values()) {
                    current.Waiting.signal();
                    current.Waiting = null;
                }
                Watchers.clear();
            }
            super.release();
        }

        @Override
        public void clearDataProperties() {
            DeviceData = "";
        }
    }
}
