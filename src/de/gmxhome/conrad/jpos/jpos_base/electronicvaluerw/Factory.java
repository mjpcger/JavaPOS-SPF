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
import jpos.config.JposEntry;

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
    @Deprecated
    public ElectronicValueRWService addDevice(int index, JposDevice dev) throws JposException {
        ElectronicValueRWProperties props = dev.getElectronicValueRWProperties(index);
        JposDevice.check(props == null, JposConst.JPOS_E_FAILURE, "Missing implementation of getElectronicValueRWProperties()");
        ElectronicValueRWService service = (ElectronicValueRWService) (props.EventSource = new ElectronicValueRWService(props, dev));
        props.Device = dev;
        props.Claiming = dev.ClaimedElectronicValueRW;
        dev.changeDefaults(props);
        props.addProperties(dev.ElectronicValueRWs);
        service.DeviceInterface = service.ElectronicValueRW = props;
        return service;
    }

    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index ElectronicValueRW  property set index.
     * @param dev ElectronicValueRW implementation instance derived from JposDevice to be used by the service.
     * @param entry Property list from jpos configuration.
     * @return ElectronicValueRWService object.
     * @throws JposException If property set could not be retrieved.
     */
    public ElectronicValueRWService addDevice(int index, JposDevice dev, JposEntry entry) throws JposException {
        ElectronicValueRWProperties props = dev.getElectronicValueRWProperties(index);
        Object o = entry.getPropertyValue("UseEnumeratedValues");
        boolean useValues = o != null ? Boolean.parseBoolean(o.toString()) : true;
        o = entry.getPropertyValue("StrongEnumerationCheck");
        boolean strong = o != null ? Boolean.parseBoolean(o.toString()) : true;
        JposDevice.check(props == null, JposConst.JPOS_E_FAILURE, "Missing implementation of getElectronicValueRWProperties()");
        ElectronicValueRWService service = (ElectronicValueRWService) (props.EventSource = new ElectronicValueRWService(props, dev, useValues, strong));
        props.Device = dev;
        props.Claiming = dev.ClaimedElectronicValueRW;
        dev.changeDefaults(props);
        props.addProperties(dev.ElectronicValueRWs);
        service.DeviceInterface = service.ElectronicValueRW = props;
        return service;
    }
}
