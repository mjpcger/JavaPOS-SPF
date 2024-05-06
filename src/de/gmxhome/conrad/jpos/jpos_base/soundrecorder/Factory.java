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

package de.gmxhome.conrad.jpos.jpos_base.soundrecorder;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.config.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;

/**
 * General part of SoundRecorder factory for JPOS devices using this framework.
 */
public class Factory extends JposDeviceFactory {
    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index SoundRecorder  property set index.
     * @param dev SoundRecorder implementation instance derived from JposDevice to be used by the service.
     * @param entry Property list from jpos configuration.
     * @return SoundRecorderService object.
     * @throws JposException If property set could not be retrieved.
     */
    public SoundRecorderService addDevice(int index, JposDevice dev, JposEntry entry) throws JposException {
        SoundRecorderProperties props = dev.getSoundRecorderProperties(index);
        validateJposConfiguration(props, dev, dev.ClaimedSoundRecorder, entry);
        SoundRecorderService service = (SoundRecorderService) (props.EventSource = new SoundRecorderService(props, dev));
        dev.changeDefaults(props);
        props.addProperties(dev.SoundRecorders);
        service.DeviceInterface = service.SoundRecorder = props;
        return service;
    }
}
