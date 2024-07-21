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
import de.gmxhome.conrad.jpos.jpos_base.JposOutputRequest;
import de.gmxhome.conrad.jpos.jpos_base.speechsynthesis.Speak;
import de.gmxhome.conrad.jpos.jpos_base.speechsynthesis.SpeechSynthesisProperties;
import jpos.JposException;
import jpos.config.JposEntry;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static de.gmxhome.conrad.jpos.jpos_base.SyncObject.INFINITE;
import static jpos.JposConst.*;

/**
 * JposDevice based implementation for JavaPOS SpeechSynthesis, based on the espeak command line tool for Windows.<br>
 * No hardware access, uses espeak instead for speech synthesis. You can download espeak for Windows
 * <a href="https://espeak.sourceforge.net/">here</a>.<br>
 * This service does not support error handling: ErrorEvent events will not be generated.<br>
 * The following properties can be set via jpos.xml:
 * <ul>
 *     <li>ESpeakPath: Full path of espeak executable, normally %ProgramFiles(x86)%\eSpeak\command_line\espeak.exe.</li>
 *     <li>NormalSpeakSpeed: Speed in words per minute, default: 175. Valid range: 80 - 450.</li>
 * </ul>
 * Keep in mind: Even if espeak supports many languages and voices, this sample provides only support for two languages
 * and voices. To add further languages or voices, change the source code.
 */
@SuppressWarnings("unused")
public class ESpeakDevice extends JposDevice {
    /**
     * Constructor. Initialize ID. Derived classes must allocate list of list of property sets
     * for all supported device types.
     *
     * @param id Device ID, in this case path to espeak executable.
     */
    protected ESpeakDevice(String id) {
        super(id);
        speechSynthesisInit(1);
        PhysicalDeviceDescription = "SpeechSynthesis base on espeak executable";
        PhysicalDeviceName = "ESpeak SpeechSynthesis Service";
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
        props.DeviceServiceDescription = "Speech Synthesis service based on espeak executable";
        props.CapPitch = props.CapSpeed = props.CapVolume = props.CapLanguage = props.CapVoice = true;
    }

    private static final String[] Languages = {
            "en-UK",
            "de-DE"
            // Add further RFC 4646 language codes to be supported here
    };

    private static final String[] InternalLanguages = {
            "en",
            "de"
            // Add the corresponding espeak voice codes here
    };

    private static final String[] Voices = {
            "John",
            "Jane"
            // Add further names here
    };

    private static final String[] InternalModifiers = {
            "m1",
            "f3"
            // Add further voice modifiers here. See espeak documentation for details.
    };

    private class MyProperties extends SpeechSynthesisProperties {
        private int NormalSpeakSpeed = 175;
        private String InternalLanguage;
        private String InternalVoice;
        /**
         * Constructor.
         */
        protected MyProperties() {
            super(0);
            LanguageList = String.join(",", Languages);
            Language = Languages[0];
            InternalLanguage = InternalLanguages[0];
            VoiceList = String.join(",", Voices);
            Voice = Voices[0];
            InternalVoice = InternalModifiers[0];
        }
        @Override
        public void checkProperties(JposEntry entries) throws JposException {
            super.checkProperties(entries);
            Object o = entries.getPropertyValue("NormalSpeakSpeed");
            if (o instanceof Integer speed) {
                check(speed < 80 || speed > 450, JPOS_E_NOSERVICE, "Invalid speak speed: " + speed);
                NormalSpeakSpeed = speed;
            }
            else
                throw new JposException(JPOS_E_NOSERVICE, "NormalSpeakSpeed must have type Integer");
        }

        @Override
        public void language(String code) throws JposException {
            super.language(code);
            for (int i = 0; i < Languages.length; i++) {
                if (Languages[i].equalsIgnoreCase(code)) {
                    InternalLanguage = InternalLanguages[i];
                    break;
                }
            }
        }

        @Override
        public void voice(String voice) throws JposException {
            super.voice(voice);
            for (int i = 0; i < Voices.length; i++) {
                if (Voices[i].equalsIgnoreCase(voice)) {
                    InternalVoice = InternalModifiers[i];
                }
            }
        }

        @Override
        public Speak speak(List<TextPart> parsedText) throws JposException {
            for (TextPart part : parsedText) {
                check(part.Text != null && part.Text.contains("\""), JPOS_E_ILLEGAL, "Text contains illegal character");
            }
            return super.speak(parsedText);
        }

        @Override
        public void speak(Speak req) throws JposException {
            int pitch = req.Pitch, rate = req.Speed, volume = req.Volume;
            StringBuffer text = appendProsody(new StringBuffer(), pitch, rate, volume);
            fillTextFromParsedText(req, pitch, rate, volume, text);
            try {
                ESpeak = Runtime.getRuntime().exec(new String[]{
                        new File(ID).getCanonicalPath(), "-s", Integer.toString(NormalSpeakSpeed), "-g", "0",
                        "-m", "-v", InternalLanguage + "+" + InternalVoice, text.toString()
                });
                final JposException[] exception = { null };
                Thread thread = new Thread(() -> {
                    exception[0] = espeakController(req);
                }, "ESpeakWatchdog");
                thread.start();
                while (true) {
                    req.Waiting.suspend(INFINITE);
                    synchronized (this) {
                        if (ESpeak != null) {
                            ESpeak.destroy();
                            continue;
                        }
                    }
                    break;
                }
                if (exception[0] != null)
                    throw exception[0];
            } catch (IOException e) {
                throw new JposException(JPOS_E_NOSERVICE, e.getMessage(), e);
            }
        }

        private void fillTextFromParsedText(Speak req, int pitch, int rate, int volume, StringBuffer text) {
            for (TextPart part : req.ParsedText) {
                if (part.Text != null)
                    text.append(part.Text);
                else {
                    if (part.Pause != null)
                        text.append("<break time='").append(part.Pause).append("'/>");
                    if (part.Reset) {
                        if (pitch != 100 || rate != 100 || volume != 50) {
                            text.append("</prosody>");
                            appendProsody(text, pitch = 100, rate = 100, volume = 50);
                        }
                    } else if ((part.Pitch != null && part.Pitch != pitch) ||
                            (part.Speed != null && part.Speed != rate) ||
                            (part.Volume != null && part.Volume != volume)) {
                        if (part.Pitch != null)
                            pitch = part.Pitch;
                        if (part.Speed != null)
                            rate = part.Speed;
                        if (part.Volume != null)
                            volume = part.Volume;
                        text.append("</prosody>");
                        appendProsody(text, pitch, rate, volume);
                    }
                }
            }
            text.append("</prosody>");
        }

        private Process ESpeak;

        private StringBuffer appendProsody(StringBuffer text, int pitch, int rate, int volume) {
            text.append("<prosody pitch='").append(pitch).append("%'");
            text.append(" rate='").append(rate).append("%'");
            text.append(" volume='").append(volume).append("'>");
            return text;
        }

        private JposException espeakController(Speak req) {
            JposException ret = null;
            while (true) {
                try {
                    int result = ESpeak.waitFor();
                    if (result != 0 && req.Abort == null)
                        ret = new JposException(JPOS_E_ILLEGAL, "ESpeak exited abnormally: " + result);
                } catch (InterruptedException e) {
                    continue;
                } catch (Exception e) {
                    ret = new JposException(JPOS_E_FAILURE, e.getMessage(), e);
                } catch (Throwable e) {
                    e.printStackTrace();
                    ret = new JposException(JPOS_E_FAILURE, e.getMessage());
                }
                synchronized (this) {
                    ESpeak = null;
                }
                req.Waiting.signal();
                return ret;
            }
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
                if (CurrentCommand instanceof Speak speak && speak.OutputID == outputID)
                    speak.abortCommand(true);
                else {
                    for (JposOutputRequest req : PendingCommands) {
                        if (req instanceof Speak speak && speak.OutputID == outputID) {
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
