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

package de.gmxhome.conrad.jpos.jpos_base.belt;

import de.gmxhome.conrad.jpos.jpos_base.JposDevice;
import de.gmxhome.conrad.jpos.jpos_base.JposDeviceFactory;
import jpos.*;
import jpos.config.JposEntry;

import static jpos.JposConst.*;
import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;

/**
 * General part of Belt factory for JPOS devices using this framework.
 */
public class Factory extends JposDeviceFactory {
    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index Belt  property set index.
     * @param dev Belt implementation instance derived from JposDevice to be used by the service.
     * @return BeltService object.
     * @throws JposException If property set could not be retrieved.
     */
    @Deprecated
    public BeltService addDevice(int index, JposDevice dev) throws JposException {
        return addDevice(index, dev, CurrentEntry);
    }

    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index Belt  property set index.
     * @param dev Belt implementation instance derived from JposDevice to be used by the service.
     * @param entry Property list from jpos configuration.
     * @return BeltService object.
     * @throws JposException If property set could not be retrieved.
     */
    public BeltService addDevice(int index, JposDevice dev, JposEntry entry) throws JposException {
        BeltProperties props = dev.getBeltProperties(index);
        validateJposConfiguration(props, dev, dev.ClaimedBelt, entry);
        BeltService service = (BeltService) (props.EventSource = new BeltService(props, dev));
        dev.changeDefaults(props);
        props.addProperties(dev.Belts);
        service.DeviceInterface = service.BeltInterface = props;
        return service;
    }
}
