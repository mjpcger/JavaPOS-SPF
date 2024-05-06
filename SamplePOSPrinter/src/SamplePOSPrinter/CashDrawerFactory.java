/**
 * Copyright 2017 Martin Conrad
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
 */

package SamplePOSPrinter;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.cashdrawer.*;
import jpos.*;
import jpos.config.JposEntry;
import jpos.loader.*;

import static jpos.JposConst.*;

/**
 * Factory class for sample printer device CashDrawer implementation
 */
public class CashDrawerFactory extends Factory implements JposServiceInstanceFactory {
    @Override
    public JposServiceInstance createInstance(String s, JposEntry jposEntry) throws JposException {
        try {
            String deviceClass = jposEntry.getPropertyValue("deviceCategory").toString();
            String port = jposEntry.getPropertyValue("Port").toString();

            synchronized(Devices) {
                if (deviceClass.equals("CashDrawer")) {
                    JposDevice any = getDevice(port);
                    Device dev;
                    boolean create = any == null;
                    if (create) {
                        dev = new Device(port);
                    } else if (!(any instanceof Device))
                        throw new JposException(JPOS_E_NOSERVICE, "Different devices on same port: " + port);
                    else {
                        dev = (Device) any;
                    }
                    dev.checkProperties(jposEntry);
                    JposServiceInstance drw = addDevice(0, dev, jposEntry);
                    if (create) {
                        putDevice(port, dev);
                    }
                    return drw;
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
