/*
 * Copyright 2024 Martin Conrad
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

package SampleVideoAudioDevice;

import de.gmxhome.conrad.jpos.jpos_base.JposDevice;
import de.gmxhome.conrad.jpos.jpos_base.speechsynthesis.Factory;
import de.gmxhome.conrad.jpos.jpos_base.speechsynthesis.SpeechSynthesisService;
import jpos.JposException;
import jpos.config.JposEntry;
import jpos.loader.JposServiceInstance;
import jpos.loader.JposServiceInstanceFactory;

import java.io.File;

import static de.gmxhome.conrad.jpos.jpos_base.JposBaseDevice.check;
import static jpos.JposConst.JPOS_E_NOSERVICE;

/**
 * Factory class for espeak based SpeechSynthesis device implementation
 */
@SuppressWarnings("unused")
public class ESpeakSpeechSynthesisFactory extends Factory implements JposServiceInstanceFactory {
    @Override
    public JposServiceInstance createInstance(String s, JposEntry jposEntry) throws JposException {
        try {
            String deviceClass = jposEntry.getPropertyValue("deviceCategory").toString();
            String espeakPath = jposEntry.getPropertyValue("ESpeakPath").toString();
            check(!espeakPath.contains("espeak") || !new File(espeakPath).canExecute(), JPOS_E_NOSERVICE, "Cannot execute espeak");

            synchronized(Devices) {
                if (deviceClass.equals("SpeechSynthesis")) {
                    JposDevice any = getDevice(espeakPath);
                    ESpeakDevice dev;
                    boolean create = any == null;
                    if (create) {
                        dev = new ESpeakDevice(espeakPath);
                    } else if (!(any instanceof ESpeakDevice))
                        throw new JposException(JPOS_E_NOSERVICE, "Different devices on same port: Any");
                    else {
                        dev = (ESpeakDevice) any;
                    }
                    dev.checkProperties(jposEntry);
                    SpeechSynthesisService srv = addDevice(0, dev, jposEntry);
                    if (create)
                        putDevice(espeakPath, dev);
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
