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

import de.gmxhome.conrad.jpos.jpos_base.JposBase;
import de.gmxhome.conrad.jpos.jpos_base.JposDataEvent;
import jpos.JposException;

import java.util.Objects;

import static de.gmxhome.conrad.jpos.jpos_base.JposBaseDevice.check;
import static jpos.JposConst.JPOS_E_ILLEGAL;

/**
 * Data event implementation for VoiceRecognition devices.
 */
@SuppressWarnings("unused")
public class VoiceRecognitionDataEvent extends JposDataEvent {
    /**
     * Constructor. Parameters passed to base class unchanged.
     * In case of sentence processing, the word list must be passed as first data element, the pattern as second element.
     * In all other cases, only the word data element must be passed. Further data elements will be ignored. At least
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     * @param result Result value to be stored in property HearingResult.
     * @param word   Words to be stored in HearingDataWord if pattern has not been specified, otherwise word information
     *               as specified in UPOS specification for property HearingDataWordList.
     * @param pattern If specified, pattern to be stored in HearingDataPattern.
     *
     */
    public VoiceRecognitionDataEvent(JposBase source, int state, int result, String word, String... pattern) throws JposException {
        super(source, state);
        check(pattern.length > 1, JPOS_E_ILLEGAL, "Too many parameters");
        HearingResult = result;
        HearingDataWordOrWordList = word;
        HearingDataPattern = pattern.length == 0 ? null : pattern[0];
    }

    /**
     * Data to be stored in HearingResult before event delivery. See UPOS specification for details.
     */
    public final int HearingResult;

    /**
     * Data to be stored in HearingDataWord or HearingDataWordList before event delivery. See UPOS specification for details.
     */
    public final String HearingDataWordOrWordList;

    /**
     * Data to be stored in HearingDataPattern before event delivery. See UPOS specification for details.
     */
    public final String HearingDataPattern;

    @Override
    public void setDataProperties() {
        VoiceRecognitionProperties props = (VoiceRecognitionProperties)getPropertySet();
        if (!Objects.equals(HearingResult,props.HearingResult)) {
            props.HearingResult = HearingResult;
            props.EventSource.logSet("HearingResult");
        }
        if (HearingDataPattern != null) {
            if (!Objects.equals(props.HearingDataPattern, HearingDataPattern)) {
                props.HearingDataPattern = HearingDataPattern;
                props.EventSource.logSet("HearingDataPattern");
            }
            if (!Objects.equals(props.HearingDataWordList, HearingDataWordOrWordList)) {
                props.HearingDataWordList = HearingDataWordOrWordList;
                props.EventSource.logSet("HearingDataWordList");
            }
        } else if (!Objects.equals(props.HearingDataWord, HearingDataWordOrWordList)) {
            props.HearingDataWord = HearingDataWordOrWordList;
            props.EventSource.logSet("HearingDataWord");
        }
    }
}
