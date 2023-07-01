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

package de.gmxhome.conrad.jpos.jpos_base.cashdrawer;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.config.JposEntry;

/**
 * General part of CashDrawer factory for JPOS devices using this framework.
 */
public class Factory extends JposDeviceFactory {
    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults. Returns CashDrawerService object.
     * @param index CashDrawer property set index.
     * @param dev   CashDrawer implementation instance derived from JposDevice to be used by the service.
     * @return CashDrawerService object.
     * @throws JposException If property set could not be retrieved.
     */
    @Deprecated
    public CashDrawerService addDevice(int index, JposDevice dev) throws JposException {
        CashDrawerProperties drw = dev.getCashDrawerProperties(index);
        CashDrawerService service;
        JposDevice.check(drw == null, JposConst.JPOS_E_FAILURE, "Missing implementation of getCashDrawerProperties()");
        service = (CashDrawerService) (drw.EventSource = new CashDrawerService(drw, dev));
        drw.Device = dev;
        drw.Claiming = dev.ClaimedCashDrawer;
        dev.changeDefaults(drw);
        drw.addProperties(dev.CashDrawers);
        service.DeviceInterface = service.CashDrawerInterface = drw;
        return service;
    }

    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults. Returns CashDrawerService object.
     * @param index CashDrawer property set index.
     * @param dev   CashDrawer implementation instance derived from JposDevice to be used by the service.
     * @param entry Property list from jpos configuration.
     * @return CashDrawerService object.
     * @throws JposException If property set could not be retrieved.
     */
    public CashDrawerService addDevice(int index, JposDevice dev, JposEntry entry) throws JposException {
        // As long as DrawerBeepVolume has not been removed, entry will not be used (works via dev.DrawerBeepVolume).
        return addDevice(index, dev);
    }
}
