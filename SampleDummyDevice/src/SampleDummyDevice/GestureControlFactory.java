/*
 * Copyright 2024 Martin Conrad
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

package SampleDummyDevice;

import de.gmxhome.conrad.jpos.jpos_base.JposDevice;
import de.gmxhome.conrad.jpos.jpos_base.gesturecontrol.GestureControlService;
import de.gmxhome.conrad.jpos.jpos_base.gesturecontrol.Factory;
import jpos.JposException;
import jpos.config.JposEntry;
import jpos.loader.JposServiceInstance;
import jpos.loader.JposServiceInstanceFactory;

import static jpos.JposConst.JPOS_E_NOSERVICE;

/**
 * Factory class for sample GestureControl device implementation
 */
@SuppressWarnings("unused")
public class GestureControlFactory extends Factory implements JposServiceInstanceFactory {
    @Override
    public JposServiceInstance createInstance(String s, JposEntry jposEntry) throws JposException {
         try {
            String deviceClass = jposEntry.getPropertyValue("deviceCategory").toString();

            synchronized(Devices) {
                if (deviceClass.equals("GestureControl")) {
                    JposDevice any = getDevice("SampleGestureControl");
                    GestureControlDevice dev;
                    boolean create = any == null;
                    if (create) {
                        dev = new GestureControlDevice("SampleGestureControl");
                    } else if (!(any instanceof GestureControlDevice))
                        throw new JposException(JPOS_E_NOSERVICE, "Different devices on same port: SampleGraphicDisplay");
                    else {
                        dev = (GestureControlDevice) any;
                    }
                    dev.checkProperties(jposEntry);
                    GestureControlService srv = addDevice(0, dev, jposEntry);
                    if (create)
                        putDevice("SampleGestureControl", dev);
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
