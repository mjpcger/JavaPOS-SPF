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

package de.gmxhome.conrad.jpos.jpos_base.electronicvaluerw;

import de.gmxhome.conrad.jpos.jpos_base.JposDevice;
import de.gmxhome.conrad.jpos.jpos_base.JposDeviceFactory;
import jpos.*;

/**
 * General part of ElectronicValueRW factory for JPOS devices using this framework.
 */
public class Factory extends JposDeviceFactory {
    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index ElectronicValueRW  property set index.
     * @param dev ElectronicValueRW implementation instance derived from JposDevice to be used by the service.
     * @return ElectronicValueRWService object.
     * @throws JposException If property set could not be retrieved.
     */
    public ElectronicValueRWService addDevice(int index, JposDevice dev) throws JposException {
        ElectronicValueRWService service;
        ElectronicValueRWProperties props = dev.getElectronicValueRWProperties(index);
        dev.check(props == null, JposConst.JPOS_E_FAILURE, "Missing implementation of getElectronicValueRWProperties()");
        service = (ElectronicValueRWService) (props.EventSource = new ElectronicValueRWService(props, dev));
        props.Device = dev;
        props.addProperties(dev.ElectronicValueRWs);
        props.Claiming = dev.ClaimedElectronicValueRW;
        dev.changeDefaults(props);
        service.DeviceInterface = service.ElectronicValueRW = props;
        return service;
    }
}