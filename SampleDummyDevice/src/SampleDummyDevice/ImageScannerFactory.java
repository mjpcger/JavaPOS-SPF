/*
 * Copyright 2021 Martin Conrad
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
import de.gmxhome.conrad.jpos.jpos_base.imagescanner.*;
import jpos.*;
import jpos.config.JposEntry;
import jpos.loader.*;

import static jpos.JposConst.*;

/**
 * Factory class for sample ImageScanner device implementation
 */
public class ImageScannerFactory extends Factory implements JposServiceInstanceFactory {
    @Override
    public JposServiceInstance createInstance(String s, JposEntry jposEntry) throws JposException {
        try {
            String deviceClass = jposEntry.getPropertyValue("deviceCategory").toString();

            synchronized(Devices) {
                if (deviceClass.equals("ImageScanner")) {
                    JposDevice any = getDevice("SampleImageScanner");
                    Device dev;
                    boolean create = any == null;
                    if (create) {
                        dev = new Device("SampleImageScanner");
                    } else if (!(any instanceof Device))
                        throw new JposException(JPOS_E_NOSERVICE, "Different devices on same port: SampleImageScanner");
                    else {
                        dev = (Device) any;
                    }
                    dev.checkProperties(jposEntry);
                    ImageScannerService srv = addDevice(0, dev, jposEntry);
                    if (create)
                        putDevice("SampleImageScanner", dev);
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
