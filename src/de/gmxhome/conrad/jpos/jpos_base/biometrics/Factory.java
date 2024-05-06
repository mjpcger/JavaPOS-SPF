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

package de.gmxhome.conrad.jpos.jpos_base.biometrics;

import de.gmxhome.conrad.jpos.jpos_base.JposDevice;
import de.gmxhome.conrad.jpos.jpos_base.JposDeviceFactory;
import jpos.*;
import jpos.config.JposEntry;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;

/**
 * General part of Biometrics factory for JPOS devices using this framework.
 */
public class Factory extends JposDeviceFactory {
    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index Biometrics  property set index.
     * @param dev Biometrics implementation instance derived from JposDevice to be used by the service.
     * @param entry Property list from jpos configuration.
     * @return BiometricsService object.
     * @throws JposException If property set could not be retrieved.
     */
    public BiometricsService addDevice(int index, JposDevice dev, JposEntry entry) throws JposException {
        BiometricsProperties props = dev.getBiometricsProperties(index);
        validateJposConfiguration(props, dev, dev.ClaimedBiometrics, entry);
        BiometricsService service = (BiometricsService) (props.EventSource = new BiometricsService(props, dev));
        dev.changeDefaults(props);
        check(props.SensorBPP == null, JPOS_E_NOSERVICE, "Missing initialization of SensorBPP property");
        check(props.SensorHeight == null, JPOS_E_NOSERVICE, "Missing initialization of SensorHeight property");
        check(props.SensorWidth == null, JPOS_E_NOSERVICE, "Missing initialization of SensorWidth property");
        check(props.validateSensorColor(props.SensorColor), JPOS_E_NOSERVICE, "SensorColor mismatch");
        props.addProperties(dev.Biometricss);
        service.DeviceInterface = service.Biometrics = props;
        return service;
    }

    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index Biometrics  property set index.
     * @param dev Biometrics implementation instance derived from JposDevice to be used by the service.
     * @return BiometricsService object.
     * @throws JposException If property set could not be retrieved.
     */
    @Deprecated
    public BiometricsService addDevice(int index, JposDevice dev) throws JposException {
        return addDevice(index, dev, CurrentEntry);
    }
}
