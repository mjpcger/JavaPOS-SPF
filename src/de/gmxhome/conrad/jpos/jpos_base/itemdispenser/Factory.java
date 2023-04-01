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

package de.gmxhome.conrad.jpos.jpos_base.itemdispenser;

import de.gmxhome.conrad.jpos.jpos_base.JposDevice;
import de.gmxhome.conrad.jpos.jpos_base.JposDeviceFactory;
import jpos.JposConst;
import jpos.JposException;

/**
 * General part of ItemDispenser factory for JPOS devices using this framework.
 */
public class Factory extends JposDeviceFactory {
    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults. Returns ItemDispenserService object.
     * @param index ItemDispenser property set index.
     * @param dev   ItemDispenser implementation instance derived from JposDevice to be used by the service.
     * @return ItemDispenserService object.
     * @throws jpos.JposException If property set could not be retrieved.
     */
    public ItemDispenserService addDevice(int index, JposDevice dev) throws JposException {
        ItemDispenserProperties drw = dev.getItemDispenserProperties(index);
        ItemDispenserService service;
        JposDevice.check(drw == null, JposConst.JPOS_E_FAILURE, "Missing implementation of getItemDispenserProperties()");
        service = (ItemDispenserService) (drw.EventSource = new ItemDispenserService(drw, dev));
        drw.Device = dev;
        drw.Claiming = dev.ClaimedItemDispenser;
        dev.changeDefaults(drw);
        drw.addProperties(dev.ItemDispensers);
        service.DeviceInterface = service.ItemDispenserInterface = drw;
        return service;
    }
}
