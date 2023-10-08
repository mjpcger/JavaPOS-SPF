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

/**
 * SpeechSynthesis service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class SpeechSynthesisService extends JposBase implements SpeechSynthesisService116 {
    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public SpeechSynthesisService(JposCommonProperties props, JposDevice device) {
        super(props, device);
    }

    /**
     * Instance of a class implementing the SpeechSynthesisInterface for tone indicator specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public SpeechSynthesisInterface SpeechSynthesis;

    @Override
    public boolean getCapLanguage() throws JposException {
        return false;
    }

    @Override
    public boolean getCapPitch() throws JposException {
        return false;
    }

    @Override
    public boolean getCapSpeed() throws JposException {
        return false;
    }

    @Override
    public boolean getCapVoice() throws JposException {
        return false;
    }

    @Override
    public boolean getCapVolume() throws JposException {
        return false;
    }

    @Override
    public String getLanguage() throws JposException {
        return null;
    }

    @Override
    public void setLanguage(String s) throws JposException {

    }

    @Override
    public String getLanguageList() throws JposException {
        return null;
    }

    @Override
    public String getOutputIDList() throws JposException {
        return null;
    }

    @Override
    public int getPitch() throws JposException {
        return 0;
    }

    @Override
    public void setPitch(int i) throws JposException {

    }

    @Override
    public int getSpeed() throws JposException {
        return 0;
    }

    @Override
    public void setSpeed(int i) throws JposException {

    }

    @Override
    public String getVoice() throws JposException {
        return null;
    }

    @Override
    public void setVoice(String s) throws JposException {

    }

    @Override
    public String getVoiceList() throws JposException {
        return null;
    }

    @Override
    public int getVolume() throws JposException {
        return 0;
    }

    @Override
    public void setVolume(int i) throws JposException {

    }

    @Override
    public void speak(String s) throws JposException {

    }

    @Override
    public void speakImmediate(String s) throws JposException {

    }

    @Override
    public void stopCurrentSpeaking() throws JposException {

    }

    @Override
    public void stopSpeaking(int i) throws JposException {

    }
}
