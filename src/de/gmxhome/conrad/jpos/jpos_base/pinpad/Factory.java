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

package de.gmxhome.conrad.jpos.jpos_base.pinpad;

import de.gmxhome.conrad.jpos.jpos_base.JposDevice;
import de.gmxhome.conrad.jpos.jpos_base.JposDeviceFactory;
import jpos.*;

/**
 * General part of PINPad factory for JPOS devices using this framework.
 */
public class Factory extends JposDeviceFactory {
    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index PINPad  property set index.
     * @param dev PINPad implementation instance derived from JposDevice to be used by the service.
     * @return PINPadService object.
     * @throws JposException If property set could not be retrieved.
     */
    public PINPadService addDevice(int index, JposDevice dev) throws JposException {
        PINPadService service;
        PINPadProperties props = dev.getPINPadProperties(index);
        dev.check(props == null, JposConst.JPOS_E_FAILURE, "Missing implementation of getPINPadProperties()");
        service = (PINPadService) (props.EventSource = new PINPadService(props, dev));
        props.Device = dev;
        props.addProperties(dev.PINPads);
        props.Claiming = dev.ClaimedPINPad;
        dev.changeDefaults(props);
        service.DeviceInterface = service.PINPad = props;
        return service;
    }
}
