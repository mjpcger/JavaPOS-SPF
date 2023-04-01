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

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * General part of POSPower factory for JPOS devices using this framework.
 */
public class Factory extends JposDeviceFactory {
    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults. Returns POSPowerService object.
     * @param index POSPower property set index.
     * @param dev   POSPower implementation instance derived from JposDevice to be used by the service.
     * @return POSPowerService object.
     * @throws JposException If property set could not be retrieved.
     */
    public POSPowerService addDevice(int index, JposDevice dev) throws JposException {
        POSPowerProperties drw = dev.getPOSPowerProperties(index);
        POSPowerService service;
        JposDevice.check(drw == null, JposConst.JPOS_E_FAILURE, "Missing implementation of getPOSPowerProperties()");
        service = (POSPowerService) (drw.EventSource = new POSPowerService(drw, dev));
        drw.Device = dev;
        drw.Claiming = dev.ClaimedPOSPower;
        dev.changeDefaults(drw);
        drw.addProperties(dev.POSPowers);
        service.DeviceInterface = service.POSPowerInterface = drw;
        return service;
    }
}
