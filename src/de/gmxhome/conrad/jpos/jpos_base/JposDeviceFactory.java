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
import jpos.JposException;

import java.util.*;

/**
 * General part of JposDevice factory for every JPOS device using this framework. Every
 * device factory tries to get a JposDevice using the specified ID.
 * If this fails, a new object derived from JposDevice will be generated and put into a
 * JposDevice map using putDevice. After closing the last service based on JposBase that
 * holds a specific JposDevice, it uses deleteDevice to remove the JposDevice object from
 * the device map.
 */
public class JposDeviceFactory {
    /**
     * Holds all JposDevice objects currentls created. The ID specified within the
     * constructor of a JposDevice will be used as index into this map.
     */
    static public Map<String,JposDevice> Devices = new HashMap<String,JposDevice>();

    /**
     * Retrieves driver implementation derived from JposDevice.
     * @param key Key, e.g. COM port
     * @return JposDevice or null
     */
    public static JposDevice getDevice(String key) {
        synchronized(Devices) {
            return Devices.containsKey(key) ? Devices.get(key) : null;
        }
    }

    /**
     * Stores driver implementation derived from JposDevice in map Devices
     * @param key Key, e.g. COM port
     * @param dev JposDevice
     * @throws JposException if Devices contains just an entry with the given key
     */
    public static void putDevice(String key, JposDevice dev) throws JposException {
        synchronized(Devices) {
            if (Devices.containsKey(key))
                throw new JposException(JposConst.JPOS_E_ILLEGAL, "Duplicate device");
            Devices.put(key, dev);
        }
    }

    /**
     * Deletes driver implementation derived from JposDevice ou of map Devices.
     * Must be done only if the JposDevice has no property set in any of its
     * lists of lists of property set lists.
     * @param key Key, e.g. COM port
     * @throws JposException if Devices does not contain an entry with the given key
     */
    public static void deleteDevice (String key) throws JposException {
        synchronized(Devices) {
            if (!Devices.containsKey(key))
                throw new JposException(JposConst.JPOS_E_NOEXIST, "Device not present");
            Devices.remove(key);
        }
    }
}
