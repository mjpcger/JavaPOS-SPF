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
import de.gmxhome.conrad.jpos.jpos_base.pointcardrw.*;
import jpos.*;
import jpos.config.JposEntry;
import jpos.loader.*;

import static jpos.JposConst.*;

/**
 * Factory class for sample PointCardRW device implementation
 */
public class PointCardRWFactory extends Factory implements JposServiceInstanceFactory {
    @Override
    public JposServiceInstance createInstance(String s, JposEntry jposEntry) throws JposException {
        try {
            String deviceClass = jposEntry.getPropertyValue("deviceCategory").toString();

            synchronized(Devices) {
                if (deviceClass.equals("PointCardRW")) {
                    JposDevice any = getDevice("SamplePointCardRW");
                    PointCardRWDevice dev;
                    boolean create = any == null;
                    if (create) {
                        dev = new PointCardRWDevice("SamplePointCardRW");
                    } else if (!(any instanceof PointCardRWDevice))
                        throw new JposException(JPOS_E_NOSERVICE, "Different devices on same port: SamplePointCardRW");
                    else {
                        dev = (PointCardRWDevice) any;
                    }
                    dev.checkProperties(jposEntry);
                    PointCardRWService srv = addDevice(0, dev, jposEntry);
                    if (create)
                        putDevice("SamplePointCardRW", dev);
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
