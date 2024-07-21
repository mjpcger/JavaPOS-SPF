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

package SampleDummyDevice;

import de.gmxhome.conrad.jpos.jpos_base.JposDevice;
import de.gmxhome.conrad.jpos.jpos_base.JposOutputRequest;
import de.gmxhome.conrad.jpos.jpos_base.speechsynthesis.Speak;
import de.gmxhome.conrad.jpos.jpos_base.speechsynthesis.SpeechSynthesisProperties;
import jpos.JposException;
import jpos.config.JposEntry;

import static jpos.JposConst.JPOS_E_NOSERVICE;
import static jpos.JposConst.JPOS_PR_NONE;

/**
 * JposDevice based dummy implementation for JavaPOS SpeechSynthesis device service implementation.
 * No real hardware. All read data with dummy values, output to console, char by char.<br>
 * Supported configuration values for SpeechSynthesis in jpos.xml can be used to set the corresponding property values:
 * <ul>
 *     <li>CharacterTimeout: Speech speed in milliseconds per character. Must be specified as integer property. Default: 100.</li>
 *     <li>LanguageList: Value of LanguageList property. Default: en-US. Can be set to another value if other languages
 *     shall be supported.</li>
 *     <li>VoiceList: Value of VoiceList property. Default: An empty string. Can be set if changing voices shall be
 *     simulated.</li>
 * </ul>
 */
public class SpeechSynthesis extends JposDevice {
    private int CharacterTimeout = 100;

    /**
     * The device implementation. See parent for further details.
     * @param id  Device ID, not used by implementation.
     */
    public SpeechSynthesis(String id) {
        super(id);
        speechSynthesisInit(1);
        PhysicalDeviceDescription = "Dummy SpeechSynthesis simulator";
        PhysicalDeviceName = "Dummy SpeechSynthesis Simulator";
        CapPowerReporting = JPOS_PR_NONE;
    }

    @Override
    public SpeechSynthesisProperties getSpeechSynthesisProperties(int index) {
        return new MyProperties();
    }

    @Override
    public void changeDefaults(SpeechSynthesisProperties p) {
        MyProperties props = (MyProperties) p;
        super.changeDefaults(p);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "Speech Synthesis service for sample dummy device";
        props.CapPitch = props.CapSpeed = props.CapVolume = true;
    }

    private class MyProperties extends SpeechSynthesisProperties {
        protected MyProperties() {
            super(0);
        }
        @Override
        public void checkProperties(JposEntry entries) throws JposException {
            super.checkProperties(entries);
            Object o = entries.getPropertyValue("LanguageList");
            if (o != null) {
                String[] languages = o.toString().split(",");
                for (int i = 0; i < languages.length; i++) {
                    String language = languages[i];
                    String[] languageCountry = language.split("-", 2);
                    boolean ok = languageCountry[0].matches("[a-z]{2,3}") && (languageCountry[1].matches("[A-Z\\-]{2,3}") || languageCountry[1].matches("[0-9]{3}"));
                    check(!ok, JPOS_E_NOSERVICE, "Invalid LanguageList: " + o);
                    for (int j = 0; j < i; j++)
                        check(language.equalsIgnoreCase(languages[j]), JPOS_E_NOSERVICE, "Duplicate language in LanguageList: " + o);
                }
                LanguageList = o.toString();
            } else {
                this.LanguageList = "en_US";
            }
            o = entries.getPropertyValue("CharacterTimeout");
            if (o instanceof Integer value)
                CharacterTimeout = value;
            o = entries.getPropertyValue("VoiceList");
            if (o != null) {
                String[] voices = o.toString().split(",");
                for (int i = 0; i < voices.length; i++) {
                    check(!voices[i].matches("[a-zA-Z0-9]*"), JPOS_E_NOSERVICE, "Invalid voices, must be alphanumeric: " + o);
                    for (int j = 0; j < i; j++)
                        check(voices[j].equalsIgnoreCase(voices[i]), JPOS_E_NOSERVICE, "Duplicate voice in VoiceList: " + o);
                }
            }
        }

        @Override
        public void speak(Speak req) throws JposException {
            int factor = req.Speed;
            m:
            for (int i = 0; i < req.ParsedText.size(); i++){
                TextPart part = req.ParsedText.get(i);
                if (part.Text != null) {
                    for (int j = 0; j < part.Text.length(); j++) {
                        System.out.print(part.Text.charAt(j));
                        int tio = (int)(CharacterTimeout * 100L / factor);
                        if (req.Waiting.suspend(tio)) {
                            System.out.print(" ABORT");
                            break m;
                        }
                    }
                }
                else {
                    if (part.Speed != null)
                        factor = part.Speed;
                    if (part.Pause != null && req.Waiting.suspend(part.Pause)) {
                        System.out.print(" ABORT");
                        break;
                    }
                    if (part.Reset)
                        factor = 100;
                }
            }
            System.out.println();
        }

        @Override
        public void stopCurrentSpeaking() throws JposException {
            synchronized (AsyncProcessorRunning) {
                if (CurrentCommand != null && CurrentCommand instanceof Speak speak) {
                    speak.abortCommand(true);
                }
            }
        }

        @Override
        public void stopSpeaking(int outputID) throws JposException {
            Speak request = null;
            synchronized (AsyncProcessorRunning) {
                if (CurrentCommand != null && CurrentCommand.OutputID == outputID)
                    CurrentCommand.abortCommand(true);
                else {
                    for (JposOutputRequest req : PendingCommands) {
                        if (req instanceof Speak speak && req.OutputID == outputID) {
                            PendingCommands.remove(request = speak);
                            break;
                        }
                    }
                }
            }
            if (request != null)
                request.removeFromOutputIDList();
        }
    }
}
