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

package de.gmxhome.conrad.jpos.jpos_base.coindispenser;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * General part of CoinDispenser factory for JPOS devices using this framework.
 */
public class Factory extends JposDeviceFactory {
    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index CoinDispenser property set index.
     * @param dev   CoinDispenser implementation instance derived from JposDevice to be used by the service.
     * @return CoinDispenserService object.
     * @throws JposException If property set could not be retrieved.
     */
    public CoinDispenserService addDevice(int index, JposDevice dev) throws JposException {
        CoinDispenserService service;
        CoinDispenserProperties props = dev.getCoinDispenserProperties(index);
        JposDevice.check(props == null, JposConst.JPOS_E_FAILURE, "Missing implementation of getCoinDispenserProperties()");
        service = (CoinDispenserService) (props.EventSource = new CoinDispenserService(props, dev));
        props.Device = dev;
        props.Claiming = dev.ClaimedCoinDispenser;
        dev.changeDefaults(props);
        props.addProperties(dev.CoinDispensers);
        service.DeviceInterface = service.CoinDispenserInterface = props;
        return service;
    }
}
