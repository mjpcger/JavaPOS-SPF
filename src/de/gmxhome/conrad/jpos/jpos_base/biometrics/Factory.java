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

/**
 * General part of Biometrics factory for JPOS devices using this framework.
 */
public class Factory extends JposDeviceFactory {
    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index Biometrics  property set index.
     * @param dev Biometrics implementation instance derived from JposDevice to be used by the service.
     * @return BiometricsService object.
     * @throws JposException If property set could not be retrieved.
     */
    public BiometricsService addDevice(int index, JposDevice dev) throws JposException {
        BiometricsService service;
        BiometricsProperties props = dev.getBiometricsProperties(index);
        JposDevice.check(props == null, JposConst.JPOS_E_FAILURE, "Missing implementation of getBiometricsProperties()");
        service = (BiometricsService) (props.EventSource = new BiometricsService(props, dev));
        props.Device = dev;
        props.Claiming = dev.ClaimedBiometrics;
        dev.changeDefaults(props);
        JposDevice.check(props.SensorBPP == null, JposConst.JPOS_E_FAILURE, "Missing initialization of SensorBPP property");
        JposDevice.check(props.SensorHeight == null, JposConst.JPOS_E_FAILURE, "Missing initialization of SensorHeight property");
        JposDevice.check(props.SensorWidth == null, JposConst.JPOS_E_FAILURE, "Missing initialization of SensorWidth property");
        JposDevice.check(!props.validateSensorColor(props.SensorColor), JposConst.JPOS_E_FAILURE, "SensorColor mismatch");
        props.addProperties(dev.Biometricss);
        service.DeviceInterface = service.Biometrics = props;
        return service;
    }
}
