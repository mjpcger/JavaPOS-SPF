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

package SampleScale;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.scale.*;
import jpos.JposConst;
import jpos.JposException;
import jpos.config.JposEntry;
import jpos.loader.JposServiceInstance;
import jpos.loader.JposServiceInstanceFactory;

import java.util.*;
/**
 * Factory class for SampleScale sample implementation
 */
public class SampleScaleFactory extends Factory implements JposServiceInstanceFactory {
    @Override
    public JposServiceInstance createInstance(String s, JposEntry jposEntry) throws JposException {
        try {
            int index = Integer.parseInt(jposEntry.getPropertyValue("DevIndex").toString());
            String deviceClass = jposEntry.getPropertyValue("deviceCategory").toString();
            String port = jposEntry.getPropertyValue("Target").toString();

            if (deviceClass.equals("Scale")) {
                JposDevice any = getDevice(port);
                SampleScale dev;
                boolean created = any != null;
                if (!created) {
                    dev = new SampleScale(port);
                }
                else if (!(any instanceof SampleScale))
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Port " + port + " used by " + any.getClass().getName());
                else {
                    dev = (SampleScale) any;
                }
                dev.checkRange(index, 0, dev.Scales.length - 1, JposConst.JPOS_E_ILLEGAL, "Scales index out of range");
                dev.checkProperties(jposEntry);
                JposServiceInstance srv = addDevice(index, dev);
                if (!created)
                    putDevice(port, dev);
                return srv;
            }
            throw new JposException(JposConst.JPOS_E_NOSERVICE, "Bad device category " + deviceClass);
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid or missing JPOS property", e);
        }
    }
}
