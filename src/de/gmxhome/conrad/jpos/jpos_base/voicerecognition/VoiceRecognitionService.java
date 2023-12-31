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

package de.gmxhome.conrad.jpos.jpos_base.voicerecognition;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.JposException;
import jpos.services.VoiceRecognitionService116;

/**
 * VoiceRecognition service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class VoiceRecognitionService extends JposBase implements VoiceRecognitionService116 {
    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public VoiceRecognitionService(JposCommonProperties props, JposDevice device) {
        super(props, device);
    }

    /**
     * Instance of a class implementing the VoiceRecognitionInterface for voice recognition specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public VoiceRecognitionInterface VoiceRecognition;

    @Override
    public boolean getCapLanguage() throws JposException {
        return false;
    }

    @Override
    public String getHearingDataPattern() throws JposException {
        return null;
    }

    @Override
    public String getHearingDataWord() throws JposException {
        return null;
    }

    @Override
    public String getHearingDataWordList() throws JposException {
        return null;
    }

    @Override
    public int getHearingResult() throws JposException {
        return 0;
    }

    @Override
    public int getHearingStatus() throws JposException {
        return 0;
    }

    @Override
    public String getLanguageList() throws JposException {
        return null;
    }

    @Override
    public void startHearingFree(String s) throws JposException {

    }

    @Override
    public void startHearingSentence(String s, String s1, String s2) throws JposException {

    }

    @Override
    public void startHearingWord(String s, String s1) throws JposException {

    }

    @Override
    public void startHearingYesNo(String s) throws JposException {

    }

    @Override
    public void stopHearing() throws JposException {

    }
}
