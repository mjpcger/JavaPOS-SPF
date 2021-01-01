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
 */

package de.gmxhome.conrad.jpos.jpos_base.bumpbar;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * General part of Bump Bar factory for JPOS devices using this framework.
 */
public class Factory extends JposDeviceFactory {
    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index BumpBar  property set index.
     * @param dev BumpBar implementation instance derived from JposDevice to be used by the service.
     * @return BumpBarService object.
     * @throws JposException If property set could not be retrieved.
     */
    public BumpBarService addDevice(int index, JposDevice dev) throws JposException {
        BumpBarService service;
        BumpBarProperties props = dev.getBumpBarProperties(index);
        dev.check(props == null, JposConst.JPOS_E_FAILURE, "Missing implementation of getBumpBarProperties()");
        service = (BumpBarService) (props.EventSource = new BumpBarService(props, dev));
        props.Device = dev;
        props.addProperties(dev.BumpBars);
        props.Claiming = dev.ClaimedBumpBar;
        dev.changeDefaults(props);
        service.DeviceInterface = service.BumpBarInterface = props;
        return service;
    }
}
