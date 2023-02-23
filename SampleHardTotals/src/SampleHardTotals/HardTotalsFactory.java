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

package SampleHardTotals;

import de.gmxhome.conrad.jpos.jpos_base.JposDevice;
import de.gmxhome.conrad.jpos.jpos_base.hardtotals.Factory;
import jpos.JposConst;
import jpos.JposException;
import jpos.config.JposEntry;
import jpos.loader.JposServiceInstance;
import jpos.loader.JposServiceInstanceFactory;

/**
 * Factory class for sample HardTotals device implementation
 */
public class HardTotalsFactory extends Factory implements JposServiceInstanceFactory {
    @Override
    public JposServiceInstance createInstance(String s, JposEntry jposEntry) throws JposException {
        try {
            String deviceClass = jposEntry.getPropertyValue("deviceCategory").toString();
            String file = jposEntry.getPropertyValue("HardTotalsFileName").toString();
            Object o = jposEntry.getPropertyValue("DevIndex");
            int index = o == null ? 0 : Integer.parseInt(o.toString());

            synchronized(Devices) {
                if (deviceClass.equals("HardTotals")) {
                    JposDevice any = getDevice(file);
                    Device dev;
                    boolean create = any == null;
                    if (create) {
                        dev = new Device(file);
                    } else if (!(any instanceof Device))
                        throw new JposException(JposConst.JPOS_E_NOSERVICE, "Different devices on same file: " + file);
                    else {
                        dev = (Device) any;
                    }
                    dev.checkProperties(jposEntry);
                    dev.checkRange(index, 0, dev.HardTotalss.length - 1, JposConst.JPOS_E_ILLEGAL, "HardTotals index out of range");
                    JposServiceInstance srv = addDevice(index, dev);
                    if (create) {
                        putDevice(file, dev);
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