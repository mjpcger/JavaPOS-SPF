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

package de.gmxhome.conrad.jpos.jpos_base.speechsynthesis;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.JposException;
import jpos.services.SpeechSynthesisService116;

import java.util.List;

import static de.gmxhome.conrad.jpos.jpos_base.JposBaseDevice.*;
import static jpos.JposConst.*;

/**
 * SpeechSynthesis service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class SpeechSynthesisService extends JposBase implements SpeechSynthesisService116 {
    private final SpeechSynthesisProperties Data;
    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public SpeechSynthesisService(SpeechSynthesisProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    /**
     * Instance of a class implementing the SpeechSynthesisInterface for speech synthesis specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public SpeechSynthesisInterface SpeechSynthesis;

    @Override
    public boolean getCapLanguage() throws JposException {
        logGet("CapLanguage");
        checkOpened();
        return Data.CapLanguage;
    }

    @Override
    public boolean getCapPitch() throws JposException {
        logGet("CapPitch");
        checkOpened();
        return Data.CapPitch;
    }

    @Override
    public boolean getCapSpeed() throws JposException {
        logGet("CapSpeed");
        checkOpened();
        return Data.CapSpeed;
    }

    @Override
    public boolean getCapVoice() throws JposException {
        logGet("CapVoice");
        checkOpened();
        return Data.CapVoice;
    }

    @Override
    public boolean getCapVolume() throws JposException {
        logGet("CapVolume");
        checkOpened();
        return Data.CapVolume;
    }

    @Override
    public String getLanguage() throws JposException {
        logGet("Language");
        checkEnabled();
        return Data.Language;
    }

    @Override
    public void setLanguage(String language) throws JposException {
        logPreSet("Language");
        checkEnabled();
        check(!Data.CapLanguage && !Data.Language.equals(language), JPOS_E_ILLEGAL, "Changing Language not supported");
        check(!member(language, Data.LanguageList.split(",")), JPOS_E_ILLEGAL, "Invalid language, must be one of " + Data.LanguageList);
        SpeechSynthesis.language(language);
        logSet("Language");
    }

    @Override
    public String getLanguageList() throws JposException {
        logGet("LanguageList");
        checkOpened();
        return Data.LanguageList;
    }

    @Override
    public String getOutputIDList() throws JposException {
        logGet("OutputIDList");
        checkEnabled();
        synchronized (Data.OutputIdListSync) {
            return Data.OutputIDList;
        }
    }

    @Override
    public int getPitch() throws JposException {
        logGet("Pitch");
        checkEnabled();
        return Data.Pitch;
    }

    @Override
    public void setPitch(int pitch) throws JposException {
        logPreSet("Pitch");
        checkEnabled();
        check(!Data.CapPitch && Data.Pitch != pitch, JPOS_E_ILLEGAL, "Changing Pitch not supported");
        check(pitch < 50 || pitch > 200, JPOS_E_ILLEGAL, "Illegal Pitch: " + pitch);
        SpeechSynthesis.pitch(pitch);
        logSet("Pitch");
    }

    @Override
    public int getSpeed() throws JposException {
        logGet("Speed");
        checkEnabled();
        return Data.Speed;
    }

    @Override
    public void setSpeed(int speed) throws JposException {
        logPreSet("Speed");
        checkEnabled();
        check(!Data.CapSpeed && Data.Speed != speed, JPOS_E_ILLEGAL, "Changing Speed not supported");
        check(speed < 50 || speed > 200, JPOS_E_ILLEGAL, "Illegal speed: " + speed);
        SpeechSynthesis.speed(speed);
        logSet("Speed");
    }

    @Override
    public String getVoice() throws JposException {
        logGet("Voice");
        checkEnabled();
        return Data.Voice;
    }

    @Override
    public void setVoice(String voice) throws JposException {
        logPreSet("Voice");
        checkEnabled();
        check(!Data.CapVoice && !Data.Voice.equals(voice), JPOS_E_ILLEGAL, "Changing Voice not supported");
        check(!member(voice, Data.VoiceList.split(",")), JPOS_E_ILLEGAL, "Invalid voice, must be one of " + Data.VoiceList);
        SpeechSynthesis.voice(voice);
        logSet("Voice");
    }

    @Override
    public String getVoiceList() throws JposException {
        logGet("VoiceList");
        checkOpened();
        return Data.VoiceList;
    }

    @Override
    public int getVolume() throws JposException {
        logGet("Volume");
        checkEnabled();
        return Data.Volume;
    }

    @Override
    public void setVolume(int volume) throws JposException {
        logPreSet("Volume");
        checkEnabled();
        check(!Data.CapVolume && volume != Data.Volume, JPOS_E_ILLEGAL, "Changing Volume not supported");
        check(volume < 0 || volume > 100, JPOS_E_ILLEGAL, "Volume must be between 0 and 100");
        SpeechSynthesis.volume(volume);
        logSet("Volume");
    }

    @Override
    public void speak(String text) throws JposException {
        logPreCall("Speak", removeOuterArraySpecifier(new Object[]{text}, Device.MaxArrayStringElements));
        checkEnabled();
        List<SpeechSynthesisProperties.TextPart> parsedText = SpeechSynthesisProperties.TextPart.parse(text);
        JposOutputRequest request = SpeechSynthesis.speak(parsedText);
        if (request != null)
            request.enqueue();
        synchronized (Data.OutputIdListSync) {
            if (Data.OutputIDList.length() == 0)
                Data.OutputIDList = String.valueOf(Data.OutputID);
            else
                Data.OutputIDList += "," + Data.OutputID;
            logSet("OutputIDList");
        }
        logAsyncCall("Speak");
    }

    @Override
    public void speakImmediate(String text) throws JposException {
        logPreCall("SpeakImmediate", removeOuterArraySpecifier(new Object[]{text}, Device.MaxArrayStringElements));
        checkEnabled();
        if (Data.State != JPOS_S_IDLE) {
            SpeechSynthesis.clearOutput();
            if (Data.OutputIDList.length() > 0) {
                Data.OutputIDList = "";
                logSet("OutputIDList");
            }
        }
        List<SpeechSynthesisProperties.TextPart> parsedText = SpeechSynthesisProperties.TextPart.parse(text);
        JposOutputRequest request = SpeechSynthesis.speak(parsedText);
        if (request != null)
            request.enqueue();
        synchronized (Data.OutputIdListSync) {
            if (Data.OutputIDList.length() == 0)
                Data.OutputIDList = String.valueOf(Data.OutputID);
            else
                Data.OutputIDList += "," + Data.OutputID;
            logSet("OutputIDList");
        }
        logAsyncCall("SpeakImmediate");
    }

    @Override
    public void stopCurrentSpeaking() throws JposException {
        logPreCall("StopCurrentSpeaking");
        checkEnabled();
        check(Data.State == JPOS_S_IDLE, JPOS_E_ILLEGAL, "Output not active");
        SpeechSynthesis.stopCurrentSpeaking();
        logCall("StopCurrentSpeaking");
    }

    @Override
    public void stopSpeaking(int outputID) throws JposException {
        logPreCall("StopSpeaking", removeOuterArraySpecifier(new Object[]{outputID}, Device.MaxArrayStringElements));
        synchronized (Data.OutputIdListSync) {
            String[] supported = Data.OutputIDList.split(",");
            checkEnabled();
            check(!member(Integer.toString(outputID), supported),
                    JPOS_E_ILLEGAL, "outputID not one of {" + String.join(",", supported) + "}");
        }
        SpeechSynthesis.stopSpeaking(outputID);
        logCall("StopSpeaking");
    }

    @Override
    public void clearOutput() throws JposException {
        super.clearOutput();
        if (Data.OutputIDList.length() > 0) {
            Data.OutputIDList = "";
            logSet("OutputIDList");
        }
    }
}
