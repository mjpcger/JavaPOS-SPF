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
import de.gmxhome.conrad.jpos.jpos_base.JposStatusUpdateEvent;

import static jpos.VoiceRecognitionConst.*;

/**
 * Status update event implementation for VoiceRecognition devices.
 */
public class VoiceRecognitionStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Constructor, Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     */
    public VoiceRecognitionStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
    }

    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        VoiceRecognitionProperties props = (VoiceRecognitionProperties) getPropertySet();
        Integer status = getHearingStatus(props);
        if (status == null) return false;
        props.HearingStatus = status;
        return true;
    }

    private Integer getHearingStatus(VoiceRecognitionProperties props) {
        switch (getStatus()) {
            case VRCG_SUE_START_HEARING_FREE -> status = VRCG_HSTATUS_FREE;
            case VRCG_SUE_START_HEARING_SENTENCE -> status = VRCG_HSTATUS_SENTENCE;
            case VRCG_SUE_START_HEARING_WORD -> status = VRCG_HSTATUS_WORD;
            case VRCG_SUE_START_HEARING_YESNO -> status = VRCG_HSTATUS_YESNO;
            case VRCG_SUE_STOP_HEARING -> status = VRCG_HSTATUS_NONE;
            default -> {
                return null;
            }
        }
        return status;
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        String[] properties = {"HearingStatus"};
        Object[] oldproperties = getPropertyValues(properties);
        if (!setStatusProperties())
            return false;
        return propertiesHaveBeenChanged(properties, oldproperties);
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        return ret.length() > 0 ? ret : switch (getStatus()) {
            case VRCG_SUE_START_HEARING_FREE -> "VoiceRecognition Start Hearing Free";
            case VRCG_SUE_START_HEARING_SENTENCE -> "VoiceRecognition Start Hearing Sentence";
            case VRCG_SUE_START_HEARING_WORD -> "VoiceRecognition Start Hearing Word";
            case VRCG_SUE_START_HEARING_YESNO -> "VoiceRecognition Start Hearing Yes / No";
            case VRCG_SUE_STOP_HEARING -> "VoiceRecognition Stop Hearing";
            default -> "Unknown SoundRecorder Status Change: " + getStatus();
        };
    }
}
