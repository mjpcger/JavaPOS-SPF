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

package SampleUdpDevice;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.cashdrawer.*;
import jpos.*;
import jpos.config.*;
import jpos.loader.*;

import static jpos.JposConst.*;

/**
 * Factory class for CashDrawer device class of sample UDP service implementation.
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
                    BeltCashboxDrawer dev;
                    boolean created = any != null;
                    if (!created) {
                        dev = new BeltCashboxDrawer(port);
                    } else if (!(any instanceof Device))
                        throw new JposException(JPOS_E_NOSERVICE, "Port " + port + " used by " + any.getClass().getName());
                    else {
                        dev = (BeltCashboxDrawer) any;
                    }
                    dev.checkProperties(jposEntry);
                    JposServiceInstance service = addDevice(0, dev, jposEntry);
                    if (!created)
                        putDevice(port, dev);
                    return service;
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
