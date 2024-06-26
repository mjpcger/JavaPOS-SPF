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

package de.gmxhome.conrad.jpos.jpos_base.soundplayer;

import de.gmxhome.conrad.jpos.jpos_base.JposBase;
import de.gmxhome.conrad.jpos.jpos_base.JposStatusUpdateEvent;

import static jpos.SoundPlayerConst.*;

/**
 * Status update event implementation for SoundPlayer devices.
 */
public class SoundPlayerStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Output ID of sound that generates this event.
     */
    public int OutputID;

    /**
     * Constructor, Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     * @param outputID Output ID of the sound that finished asynchronous processing.
     */
    public SoundPlayerStatusUpdateEvent(JposBase source, int state, int outputID) {
        super(source, state);
        OutputID = outputID;
    }
    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        SoundPlayerProperties data = (SoundPlayerProperties) ((JposBase) getSource()).Props;
        synchronized (this) {
            switch (getStatus()) {
                case SPLY_SUE_START_PLAY_SOUND -> {
                    synchronized (data.OutputIdListSync) {
                        data.OutputIDList = data.OutputIDList.length() == 0 ? Integer.toString(OutputID) : data.OutputIDList + "," + OutputID;
                    }
                }
                case SPLY_SUE_STOP_PLAY_SOUND -> {
                    synchronized (data.OutputIdListSync) {
                        String[] parts = data.OutputIDList.split(",");
                        StringBuilder newList = new StringBuilder();
                        for (String part : parts) {
                            if (OutputID != Integer.parseInt(part))
                                newList.append(',').append(part);
                        }
                        data.OutputIDList = newList.substring(1);
                    }
                }
                default -> {
                    return false;
                }
            }
        }
        ((SoundPlayerService) getSource()).logSet("OutputIDList");
        return true;
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        return ret.length() > 0 ? ret : switch (getStatus()) {
            case SPLY_SUE_START_PLAY_SOUND -> "SoundPlayer Start Play Sound, ID: " + OutputID;
            case SPLY_SUE_STOP_PLAY_SOUND -> "SoundPlayer Stop Play Sound, ID: " + OutputID;
            default -> "Unknown SoundPlayer Status Change: " + getStatus();
        };
    }
}
