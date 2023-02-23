/*
 * Copyright 2021 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.hardtotals;

import de.gmxhome.conrad.jpos.jpos_base.JposDevice;
import de.gmxhome.conrad.jpos.jpos_base.JposDeviceFactory;
import jpos.*;

import java.util.HashMap;
import java.util.Map;

/**
 * General part of HardTotals factory for JPOS devices using this framework.
 */
public class Factory extends JposDeviceFactory {
    /**
     * Hashmap containing array of list of claiming device instances per hard total file per HardTotals device.
     * Contains one array per JposDevice instance.
     * <br>This map is structured as follows:
     * <ul>
     *     <li>For each instance of a HardTotals service implemented via JavaPOS-SPF, one array of
     *     Map&lt;Integer, HardTotalsProperties&gt; will be put to ClaimedHardTotals, with the service object as
     *     key.</li>
     *     <li>The dimension of the array will be the number of HardTotals devices the service implementation supports.
     *     </li>
     *     <li>Every HardTotals instance has a property set with an Index property that specifies which HardTotals
     *     device it supports. This is also the index of the corresponding map to be used to claim files.</li>
     *     <li>For every claimed file, the property set of the claiming instance will be put into that map, with
     *     the file handle as key.</li>
     * </ul>
     * Therefore, from within an instance of HardTotalsProperties, you can find the property set of a file referenced
     * by a file handle <i>handle</i> as follows:<br>
     *     Factory.ClaimedHardTotals.get(Device)[Index].get(<i>handle</i>)
     */
    static Map<JposDevice, Map<Integer,HardTotalsProperties>[]> ClaimedHardTotals = new HashMap();

    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index HardTotals  property set index.
     * @param dev HardTotals implementation instance derived from JposDevice to be used by the service.
     * @return HardTotalsService object.
     * @throws JposException If property set could not be retrieved.
     */
    public HardTotalsService addDevice(int index, JposDevice dev) throws JposException {
        HardTotalsService service;
        HardTotalsProperties props = dev.getHardTotalsProperties(index);
        dev.check(props == null, JposConst.JPOS_E_FAILURE, "Missing implementation of getHardTotalsProperties()");
        service = (HardTotalsService) (props.EventSource = new HardTotalsService(props, dev));
        props.Device = dev;
        props.addProperties(dev.HardTotalss);
        props.Claiming = dev.ClaimedHardTotals;
        synchronized (ClaimedHardTotals) {
            if (!ClaimedHardTotals.containsKey(dev)) {
                HashMap<Integer, HardTotalsProperties>[] map = new HashMap[dev.HardTotalss.length];
                for (int i = 0; i < map.length; i++)
                    map[i] = new HashMap<Integer, HardTotalsProperties>();
                ClaimedHardTotals.put(dev, map);
            }
        }
        dev.changeDefaults(props);
        service.DeviceInterface = service.HardTotals = props;
        return service;
    }
}
