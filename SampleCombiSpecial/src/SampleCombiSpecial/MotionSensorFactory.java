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

package SampleCombiSpecial;

import de.gmxhome.conrad.jpos.jpos_base.JposDevice;
import de.gmxhome.conrad.jpos.jpos_base.motionsensor.Factory;
import jpos.JposException;
import jpos.config.JposEntry;
import jpos.loader.JposServiceInstance;
import jpos.loader.JposServiceInstanceFactory;
import static jpos.JposConst.*;

/**
 * Factory class for MotionSensor Device of sample implementation.
 */
public class MotionSensorFactory extends Factory implements JposServiceInstanceFactory {
    @Override
    public JposServiceInstance createInstance(String s, JposEntry jposEntry) throws JposException {
        try {
            String deviceClass = jposEntry.getPropertyValue("deviceCategory").toString();
            String port = jposEntry.getPropertyValue("Port").toString();

            synchronized(Devices) {
                if (deviceClass.equals("MotionSensor")) {
                    JposDevice any = getDevice(port);
                    Device dev;
                    boolean created = any != null;
                    if (!created) {
                        dev = new Device(port);
                    } else if (!(any instanceof Device))
                        throw new JposException(JPOS_E_NOSERVICE, "Port " + port + " used by " + any.getClass().getName());
                    else {
                        dev = (Device) any;
                    }
                    dev.checkProperties(jposEntry);
                    JposServiceInstance srv = addDevice(0, dev, jposEntry);
                    if (!created)
                        putDevice(port, dev);
                    return srv;
                }
            }
            throw new JposException(JPOS_E_NOSERVICE, "Bad device category " + deviceClass);
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            throw new JposException(JPOS_E_NOSERVICE, "Invalid or missing JPOS property", e);
        }
    }
}
