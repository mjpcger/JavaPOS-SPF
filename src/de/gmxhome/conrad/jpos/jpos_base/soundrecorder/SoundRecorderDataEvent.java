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

package de.gmxhome.conrad.jpos.jpos_base.soundrecorder;

import de.gmxhome.conrad.jpos.jpos_base.JposBase;
import de.gmxhome.conrad.jpos.jpos_base.JposDataEvent;

/**
 * Data event implementation for SoundRecorder devices.
 */
public class SoundRecorderDataEvent extends JposDataEvent {
    /**
     * Constructor. Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     * @param data   Sound data to be stored in property SoundData during event processing.
     */
    public SoundRecorderDataEvent(JposBase source, int state, byte[] data) {
        super(source, state);
        SoundData = data;
    }

    /**
     * Holds the sound data to be stored in property SoundData.
     */
    public final byte[] SoundData;

    @Override
    public void setDataProperties() {
        ((SoundRecorderProperties)getPropertySet()).SoundData = SoundData;
    }
}
