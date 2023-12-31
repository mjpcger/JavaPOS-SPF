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
import jpos.SoundPlayerConst;

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
                case SoundPlayerConst.SPLY_SUE_START_PLAY_SOUND:
                    data.OutputIDList = data.OutputIDList.length() == 0 ? Integer.toString(OutputID) : data.OutputIDList + "," + OutputID;
                    break;
                case SoundPlayerConst.SPLY_SUE_STOP_PLAY_SOUND:
                    String[] parts = data.OutputIDList.split(",");
                    data.OutputIDList = "";
                    for (String part : parts) {
                        if (OutputID != Integer.parseInt(part))
                            data.OutputIDList += (data.OutputIDList.length() > 0 ? "," : "") + part;
                    }
                    break;
                default:
                    return false;
            }
        }
        ((SoundPlayerService) getSource()).logSet("OutputIDList");
        return true;
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        if (ret.length() > 0)
            return ret;
        switch (getStatus()) {
            case SoundPlayerConst.SPLY_SUE_START_PLAY_SOUND:
                return "SoundPlayer Start Play Sound, ID: " + OutputID;
            case SoundPlayerConst.SPLY_SUE_STOP_PLAY_SOUND:
                return "SoundPlayer Stop Play Sound, ID: " + OutputID;
        }
        return "Unknown SoundPlayer Status Change: " + getStatus();
    }
}
