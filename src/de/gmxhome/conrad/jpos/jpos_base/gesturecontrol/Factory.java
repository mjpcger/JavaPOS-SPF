/*
 * Copyright 2023 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.gesturecontrol;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.config.JposEntry;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;

/**
 * General part of GestureControl factory for JPOS devices using this framework.
 */
public class Factory extends JposDeviceFactory {
    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index GestureControl  property set index.
     * @param dev GestureControl implementation instance derived from JposDevice to be used by the service.
     * @param entry Property list from jpos configuration.
     * @return GestureControlService object.
     * @throws JposException If property set could not be retrieved.
     */
    public GestureControlService addDevice(int index, JposDevice dev, JposEntry entry) throws JposException {
        GestureControlProperties props = dev.getGestureControlProperties(index);
        validateJposConfiguration(props, dev, dev.ClaimedGestureControl, entry);
        GestureControlService service = (GestureControlService) (props.EventSource = new GestureControlService(props, dev));
        dev.changeDefaults(props);
        props.addProperties(dev.GestureControls);
        service.DeviceInterface = service.GestureControl = props;
        return service;
    }
}
