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

package SampleCheckScanner;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.checkscanner.*;
import jpos.*;
import jpos.config.*;
import jpos.loader.*;

/**
 * Factory class for sample CheckScanner device implementation
 */
public class CheckScannerFactory extends Factory implements JposServiceInstanceFactory {
    @Override
    public JposServiceInstance createInstance(String s, JposEntry jposEntry) throws JposException {
        try {
            String deviceClass = jposEntry.getPropertyValue("deviceCategory").toString();

            synchronized(Devices) {
                if (deviceClass.equals("CheckScanner")) {
                    JposDevice any = getDevice("SampleCheckScanner");
                    Device dev;
                    boolean create = any == null;
                    if (create) {
                        dev = new Device("SampleCheckScanner");
                    } else if (!(any instanceof Device))
                        throw new JposException(JposConst.JPOS_E_NOSERVICE, "Different devices on same port: SampleCheckScanner");
                    else {
                        dev = (Device) any;
                    }
                    dev.checkProperties(jposEntry);
                    CheckScannerService srv = addDevice(0, dev);
                    srv.CheckScanner.checkMandatoryProperties();
                    if (create) {
                        putDevice("SampleCheckScanner", dev);
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
