/*
 * Copyright 2019 Martin Conrad
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

package SampleCAT;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.cat.*;
import jpos.JposConst;
import jpos.JposException;
import jpos.config.JposEntry;
import jpos.loader.JposServiceInstance;
import jpos.loader.JposServiceInstanceFactory;

import static de.gmxhome.conrad.jpos.jpos_base.JposDeviceFactory.getDevice;
import static de.gmxhome.conrad.jpos.jpos_base.JposDeviceFactory.putDevice;

/**
 * Factory class for CAT sample service implementation
 */
public class SampleCATFactory extends Factory implements JposServiceInstanceFactory {
    @Override
    public JposServiceInstance createInstance(String s, JposEntry jposEntry) throws JposException {
        try {
            String deviceClass = jposEntry.getPropertyValue("deviceCategory").toString();
            String port = jposEntry.getPropertyValue("Port").toString();

            synchronized(Devices) {
                if (deviceClass.equals("CAT")) {
                    JposDevice any = getDevice(port);
                    SampleCAT dev;
                    boolean created = any != null;
                    if (!created) {
                        dev = new SampleCAT(port, jposEntry.getPropertyValue("DisplayName"), jposEntry.getPropertyValue("JournalPath"));
                    } else if (!(any instanceof SampleCAT))
                        throw new JposException(JposConst.JPOS_E_ILLEGAL, "Port " + port + " used by " + any.getClass().getName());
                    else {
                        dev = (SampleCAT) any;
                    }
                    dev.checkProperties(jposEntry);
                    JposServiceInstance srv = addDevice(0, dev);
                    if (!created)
                        putDevice(port, dev);
                    return srv;
                }
            }
            throw new JposException(JposConst.JPOS_E_NOSERVICE, "Bad device category " + deviceClass);
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid or missing JPOS property", e);
        }
    }
}
