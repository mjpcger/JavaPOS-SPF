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

package de.gmxhome.conrad.jpos.jpos_base.videocapture;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.config.JposEntry;

/**
 * General part of VideoCapture factory for JPOS devices using this framework.
 */
public class Factory extends JposDeviceFactory {
    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index VideoCapture  property set index.
     * @param dev VideoCapture implementation instance derived from JposDevice to be used by the service.
     * @param entry Property list from jpos configuration.
     * @return VideoCaptureService object.
     * @throws JposException If property set could not be retrieved.
     */
    public VideoCaptureService addDevice(int index, JposDevice dev, JposEntry entry) throws JposException {
        VideoCaptureProperties props = dev.getVideoCaptureProperties(index);
        JposDevice.check(props == null, JposConst.JPOS_E_NOSERVICE, "Missing implementation of getVideoCaptureProperties()");
        VideoCaptureService service = (VideoCaptureService) (props.EventSource = new VideoCaptureService(props, dev));
        props.Device = dev;
        props.Claiming = dev.ClaimedVideoCapture;
        dev.changeDefaults(props);
        JposDevice.check(!props.CapPhoto && !props.CapVideo, JposConst.JPOS_E_NOSERVICE, "Either video or photo support is mandatory for the device class");
        props.addProperties(dev.VideoCaptures);
        service.DeviceInterface = service.VideoCapture = props;
        return service;
    }
}
