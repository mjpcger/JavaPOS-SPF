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

package de.gmxhome.conrad.jpos.jpos_base.imagescanner;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.config.JposEntry;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;

/**
 * General part of ImageScanner factory for JPOS devices using this framework.
 */
public class Factory extends JposDeviceFactory {
    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index ImageScanner  property set index.
     * @param dev ImageScanner implementation instance derived from JposDevice to be used by the service.
     * @param entry Property list from jpos configuration.
     * @return ImageScannerService object.
     * @throws JposException If property set could not be retrieved.
     */
    public ImageScannerService addDevice(int index, JposDevice dev, JposEntry entry) throws JposException {
        ImageScannerProperties props = dev.getImageScannerProperties(index);
        validateJposConfiguration(props, dev, dev.ClaimedImageScanner, entry);
        ImageScannerService service = (ImageScannerService) (props.EventSource = new ImageScannerService(props, dev));
        dev.changeDefaults(props);
        check(!props.CapVideoData && !props.CapImageData && !props.CapDecodeData, JPOS_E_NOSERVICE, "Missing video, image or decode capability");
        props.addProperties(dev.ImageScanners);
        service.DeviceInterface = service.ImageScanner = props;
        return service;
    }

    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index ImageScanner  property set index.
     * @param dev ImageScanner implementation instance derived from JposDevice to be used by the service.
     * @return ImageScannerService object.
     * @throws JposException If property set could not be retrieved.
     */
    @Deprecated
    public ImageScannerService addDevice(int index, JposDevice dev) throws JposException {
        return addDevice(index, dev, CurrentEntry);
    }
}
