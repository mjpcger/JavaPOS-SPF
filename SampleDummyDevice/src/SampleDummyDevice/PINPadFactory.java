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
import de.gmxhome.conrad.jpos.jpos_base.pinpad.*;
import jpos.*;
import jpos.config.*;
import jpos.loader.*;

/**
 * Factory class for sample PINPad device implementation
 */
public class PINPadFactory extends Factory implements JposServiceInstanceFactory {
    @Override
    public JposServiceInstance createInstance(String s, JposEntry jposEntry) throws JposException {
        try {
            String deviceClass = jposEntry.getPropertyValue("deviceCategory").toString();

            synchronized(Devices) {
                if (deviceClass.equals("PINPad")) {
                    JposDevice any = getDevice("SamplePINPad");
                    PINPadDevice dev;
                    boolean create = any == null;
                    if (create) {
                        dev = new PINPadDevice("SamplePINPad");
                    } else if (!(any instanceof PINPadDevice))
                        throw new JposException(JposConst.JPOS_E_NOSERVICE, "Different devices on same port: SampleRFIDScanner");
                    else {
                        dev = (PINPadDevice) any;
                    }
                    dev.checkProperties(jposEntry);
                    PINPadService srv = addDevice(0, dev);
                    if (create)
                        putDevice("SamplePINPad", dev);
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
