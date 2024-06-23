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

package de.gmxhome.conrad.jpos.jpos_base.voicerecognition;

import jpos.JposException;

import static jpos.VoiceRecognitionConst.*;

/**
 * Input request executor for VoiceRecognition method StartHearingSentence.
 */
public class StartHearingSentence extends StartHearingWord {
    /**
     * Comma-separated list of sentence pattern method StartHearingSentence shall be waiting for. See UPOS specification
     * for details.
     */
    public final String PatternList;
    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props    Property set of device service.
     * @param language Language to be used for recognition.
     * @param wordList Comma-separated list of words to be waiting for.
     * @param patternList Comma-separated list of sentence pattern to be waiting for. See UPOS specification for details.
     */
    public StartHearingSentence(VoiceRecognitionProperties props, String language, String wordList, String patternList) {
        super(props, language, wordList);
        PatternList = patternList;
    }

    @Override
    public void invoke() throws JposException {
        if (((VoiceRecognitionProperties) Props).HearingStatus != VRCG_HSTATUS_SENTENCE)
            Device.handleEvent(new VoiceRecognitionStatusUpdateEvent(Props.EventSource, VRCG_SUE_START_HEARING_SENTENCE));
        ((VoiceRecognitionService)Props.EventSource).VoiceRecognition.startHearingSentence(this);
        Device.handleEvent(new VoiceRecognitionStatusUpdateEvent(Props.EventSource, VRCG_SUE_STOP_HEARING));
    }
}
