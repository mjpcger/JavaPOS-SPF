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
 * Input request executor for VoiceRecognition method StartHearingWord.
 */
public class StartHearingWord extends StartHearingFree {
    /**
     * Comma-separated list of word candidates StartHearingWord shall be waiting for.
     */
    public final String WordList;
    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props    Property set of device service.
     * @param language Language to be used for recognition.
     * @param wordList Comma-separated list of words to be waiting for.
     */
    public StartHearingWord(VoiceRecognitionProperties props, String language, String wordList) {
        super(props, language);
        WordList = wordList;
    }

    @Override
    public void invoke() throws JposException {
        if (((VoiceRecognitionProperties) Props).HearingStatus != VRCG_HSTATUS_WORD)
            Device.handleEvent(new VoiceRecognitionStatusUpdateEvent(Props.EventSource, VRCG_SUE_START_HEARING_WORD));
        ((VoiceRecognitionService)Props.EventSource).VoiceRecognition.startHearingWord(this);
        Device.handleEvent(new VoiceRecognitionStatusUpdateEvent(Props.EventSource, VRCG_SUE_STOP_HEARING));
    }
}
