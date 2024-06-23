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

import java.util.ArrayList;

import static de.gmxhome.conrad.jpos.jpos_base.JposBaseDevice.member;
import static jpos.JposConst.JPOS_E_BUSY;
import static jpos.JposConst.JPOS_E_ILLEGAL;

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
    public VoiceRecognitionService(VoiceRecognitionProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    /**
     * Instance of a class implementing the VoiceRecognitionInterface for voice recognition specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public VoiceRecognitionInterface VoiceRecognition;
    private final VoiceRecognitionProperties Data;

    @Override
    public boolean getCapLanguage() throws JposException {
        logGet("CapLanguage");
        checkOpened();
        return Data.CapLanguage;
    }

    @Override
    public String getHearingDataPattern() throws JposException {
        logGet("HearingDataPattern");
        checkEnabled();
        return Data.HearingDataPattern;
    }

    @Override
    public String getHearingDataWord() throws JposException {
        logGet("HearingDataWord");
        checkEnabled();
        return Data.HearingDataWord;
    }

    @Override
    public String getHearingDataWordList() throws JposException {
        logGet("HearingDataWordList");
        checkEnabled();
        return Data.HearingDataWordList;
    }

    @Override
    public int getHearingResult() throws JposException {
        logGet("HearingResult");
        checkEnabled();
        check(Data.HearingResult == null, JPOS_E_ILLEGAL, "HearingResult not present");
        return Data.HearingResult;
    }

    @Override
    public int getHearingStatus() throws JposException {
        logGet("HearingStatus");
        checkEnabled();
        return Data.HearingStatus;
    }

    @Override
    public String getLanguageList() throws JposException {
        logGet("LanguageList");
        checkOpened();
        return Data.LanguageList;
    }

    @Override
    public void startHearingFree(String language) throws JposException {
        logPreCall("StartHearingFree", removeOuterArraySpecifier(new Object[]{language}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!member(language, Data.LanguageList.split(",")), JPOS_E_ILLEGAL, "Invalid language: " + language);
        synchronized (Device.AsyncProcessorRunning) {
            check(Props.AsyncInputActive, JPOS_E_BUSY, "Hearing something is active");
            StartHearingFree request = VoiceRecognition.startHearingFree(language);
            if (request != null)
                request.enqueue();
        }
        logCall("StartHearingFree");
    }

    @Override
    public void startHearingSentence(String language, String wordList, String patternList) throws JposException {
        logPreCall("StartHearingSentence", removeOuterArraySpecifier(new Object[]{language, wordList, patternList}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!member(language, Data.LanguageList.split(",")), JPOS_E_ILLEGAL, "Invalid language: " + language);
        String[] words = (wordList == null ? "" : wordList).split(",");
        ArrayList<String> ids = new ArrayList<>();
        for (String word : words) {
            String[] idCandidates = word.split(":");
            check(idCandidates.length < 2 || ids.contains(idCandidates[0]), JPOS_E_ILLEGAL, "Invalid word list: " + wordList);
            for (String part : idCandidates) {
                check(part.length() == 0, JPOS_E_ILLEGAL, "Invalid word list component: " + word);
            }
            ids.add(idCandidates[0]);
        }
        String[] patterns = (patternList == null ? "" : patternList).split(",");
        ArrayList<String> pids = new ArrayList<>();
        for (String pattern : patterns) {
            String[] idSentence = pattern.split(":");
            check(idSentence.length != 2, JPOS_E_ILLEGAL, "Invalid pattern:" + pattern);
            check(idSentence[0].length() == 0 || pids.contains(idSentence[0]), JPOS_E_ILLEGAL, "Duplicate or empty pattern id: " + idSentence[0]);
            pids.add(idSentence[0]);
            for (int i = idSentence[1].indexOf('['); ++i > 0; i = idSentence[1].indexOf('[')) {
                int j = idSentence[1].indexOf(']');
                check(j < i, JPOS_E_ILLEGAL, "Invalid pattern: " + pattern);
                check(!ids.contains(idSentence[1].substring(i, j)), JPOS_E_ILLEGAL, "Invalid wordGroupId in pattern: " + pattern);
                idSentence[1] = idSentence[1].substring(j + 1);
            }
        }
        synchronized (Device.AsyncProcessorRunning) {
            check(Props.AsyncInputActive, JPOS_E_BUSY, "Hearing something is active\"");
            StartHearingSentence request = VoiceRecognition.startHearingSentence(language, wordList, patternList);
            if (request != null)
                request.enqueue();
        }
        logCall("StartHearingSentence");
    }

    @Override
    public void startHearingWord(String language, String wordList) throws JposException {
        logPreCall("StartHearingWord", removeOuterArraySpecifier(new Object[]{language, wordList}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!member(language, Data.LanguageList.split(",")), JPOS_E_ILLEGAL, "Invalid language: " + language);
        String[] words = (wordList == null ? "" : wordList).split(",");
        for (String word : words) {
            check(word.length() == 0, JPOS_E_ILLEGAL, "Empty or duplicate word: " + wordList);
        }
        synchronized (Device.AsyncProcessorRunning) {
            check(Props.AsyncInputActive, JPOS_E_BUSY, "Hearing something is active\"");
            StartHearingWord request = VoiceRecognition.startHearingWord(language, wordList);
            if (request != null)
                request.enqueue();
        }
        logCall("StartHearingWord");
    }

    @Override
    public void startHearingYesNo(String language) throws JposException {
        logPreCall("StartHearingYesNo", removeOuterArraySpecifier(new Object[]{language}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!member(language, Data.LanguageList.split(",")), JPOS_E_ILLEGAL, "Invalid language: " + language);
        synchronized (Device.AsyncProcessorRunning) {
            check(Props.AsyncInputActive, JPOS_E_BUSY, "Hearing something is active\"");
            StartHearingYesNo request = VoiceRecognition.startHearingYesNo(language);
            if (request != null)
                request.enqueue();
        }
        logCall("StartHearingYesNo");
    }

    @Override
    public void stopHearing() throws JposException {
        logPreCall("StopHearing");
        checkEnabled();
        synchronized (Device.AsyncProcessorRunning) {
            check(!Props.AsyncInputActive, JPOS_E_ILLEGAL, "Hearing something has not been started");
            VoiceRecognition.stopHearing();
        }
        logCall("StartHearingYesNo");
    }
}
