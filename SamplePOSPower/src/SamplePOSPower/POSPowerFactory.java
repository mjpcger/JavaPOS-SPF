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

package SamplePOSPower;

import de.gmxhome.conrad.jpos.jpos_base.JposDevice;
import de.gmxhome.conrad.jpos.jpos_base.pospower.Factory;
import jpos.JposConst;
import jpos.JposException;
import jpos.config.JposEntry;
import jpos.loader.JposServiceInstance;
import jpos.loader.JposServiceInstanceFactory;

/**
 * Factory class for sample POSPower device implementation
 */
public class POSPowerFactory extends Factory implements JposServiceInstanceFactory {
    @Override
    public JposServiceInstance createInstance(String s, JposEntry jposEntry) throws JposException {
        try {
            String deviceClass = jposEntry.getPropertyValue("deviceCategory").toString();

            synchronized(Devices) {
                if (deviceClass.equals("POSPower")) {
                    JposDevice any = getDevice("SamplePOSPower");
                    Device dev;
                    boolean create = any == null;
                    if (create) {
                        dev = new Device("SamplePOSPower");
                    } else if (!(any instanceof Device))
                        throw new JposException(JposConst.JPOS_E_NOSERVICE, "Different devices on same port: SamplePOSPower");
                    else {
                        dev = (Device) any;
                    }
                    if (Device.Kernel32Lib == null)
                        throw new JposException(JposConst.JPOS_E_NOSERVICE, "JNA or Kernel32.dll not available");
                    dev.checkProperties(jposEntry);
                    JposServiceInstance srv = addDevice(0, dev, jposEntry);
                    if (create) {
                        putDevice("SamplePOSPower", dev);
                    }
                    return srv;
                }
            }
            throw new JposException(JposConst.JPOS_E_NOSERVICE, "Bad device category " + deviceClass);
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid or missing JPOS property", e);
        }
    }
}
