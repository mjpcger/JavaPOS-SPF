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

package SampleDummyDevice;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.rfidscanner.*;
import jpos.*;
import jpos.config.*;
import jpos.loader.*;

import static jpos.JposConst.*;

/**
 * Factory class for sample RFIDScanner device implementation
 */
public class RFIDScannerFactory extends Factory implements JposServiceInstanceFactory {
    @Override
    public JposServiceInstance createInstance(String s, JposEntry jposEntry) throws JposException {
        try {
            String deviceClass = jposEntry.getPropertyValue("deviceCategory").toString();
            String file = jposEntry.getPropertyValue("RFIDScannerFileName").toString();

            synchronized(Devices) {
                if (deviceClass.equals("RFIDScanner")) {
                    JposDevice any = getDevice(file);
                    RFIDDevice dev;
                    boolean create = any == null;
                    if (create) {
                        dev = new RFIDDevice(file);
                    } else if (!(any instanceof RFIDDevice))
                        throw new JposException(JPOS_E_NOSERVICE, "Different devices on same port: " + file);
                    else {
                        dev = (RFIDDevice) any;
                    }
                    dev.checkProperties(jposEntry);
                    RFIDScannerService srv = addDevice(0, dev, jposEntry);
                    if (create)
                        putDevice(file, dev);
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
