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

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import de.gmxhome.conrad.jpos.jpos_base.JposStatusUpdateEvent;
import jpos.JposException;
import jpos.config.JposEntry;

import static de.gmxhome.conrad.jpos.jpos_base.JposBaseDevice.check;
import static jpos.JposConst.JPOS_E_NOSERVICE;
import static jpos.VoiceRecognitionConst.VRCG_HSTATUS_NONE;

/**
 * Class containing the voice recognition specific properties, their default values and default implementations of
 * VoiceRecognitionInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Voice Recognition.
 */
public class VoiceRecognitionProperties extends JposCommonProperties implements VoiceRecognitionInterface {
    /**
     * UPOS property CapLanguage. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapLanguage = false;

    /**
     * UPOS property HearingDataPattern. Default: An empty string. Valid values will be set via VoiceRecognitionDataEvent.
     */
    public String HearingDataPattern = "";

    /**
     * UPOS property HearingDataWord. Default: An empty string. Valid values will be set via VoiceRecognitionDataEvent.
     */
    public String HearingDataWord = "";

    /**
     * UPOS property HearingDataWordList. Default: An empty string. Valid values will be set via VoiceRecognitionDataEvent.
     */
    public String HearingDataWordList;

    /**
     * UPOS property HearingResult. Default: null. Valid values will be set via VoiceRecognitionDataEvent.
     */
    public Integer HearingResult = null;

    /**
     * UPOS property HearingStatus. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int HearingStatus = VRCG_HSTATUS_NONE;

    /**
     * UPOS property LanguageList. Default: An empty string. Must be overwritten within method checkProperties or by
     * objects derived from JposDevice within their changeDefaults method.
     */
    public String LanguageList;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected VoiceRecognitionProperties(int dev) {
        super(dev);
    }

    @Override
    public void initOnEnable(boolean enable) {
        super.initOnEnable(enable);
        if (HearingStatus != VRCG_HSTATUS_NONE) {
            HearingStatus = VRCG_HSTATUS_NONE;
            EventSource.logSet("HearingStatus");
        }
    }

    @Override
    public void clearDataProperties() {
        JposStatusUpdateEvent helper = new JposStatusUpdateEvent(EventSource, 0);
        String[] names = { "HearingDataWord", "HearingDataWordList", "HearingDataPattern", "HearingResult" };
        Object[] previousValues = helper.getPropertyValues(names);
        HearingDataWord = HearingDataWordList = HearingDataPattern = "";
        HearingResult = null;
        helper.propertiesHaveBeenChanged(names, previousValues);
    }

    @Override
    public void clearInput() throws JposException {
        super.clearInput();
        if (HearingStatus != VRCG_HSTATUS_NONE) {
            HearingStatus = VRCG_HSTATUS_NONE;
            EventSource.logSet("HearingStatus");
        }
    }

    @Override
    public StartHearingFree startHearingFree(String language) throws JposException {
        return new StartHearingFree(this, language);
    }

    @Override
    public void startHearingFree(StartHearingFree request) throws JposException {
    }

    @Override
    public StartHearingSentence startHearingSentence(String language, String wordList, String patternList) throws JposException {
        return new StartHearingSentence(this, language, wordList, patternList);
    }

    @Override
    public void startHearingSentence(StartHearingSentence request) throws JposException {
    }

    @Override
    public StartHearingWord startHearingWord(String language, String wordList) throws JposException {
        return new StartHearingWord(this, language, wordList);
    }

    @Override
    public void startHearingWord(StartHearingWord request) throws JposException {
    }

    @Override
    public StartHearingYesNo startHearingYesNo(String language) throws JposException {
        return new StartHearingYesNo(this, language);
    }

    @Override
    public void startHearingYesNo(StartHearingYesNo request) throws JposException {
    }

    @Override
    public void stopHearing() throws JposException {
    }
}
