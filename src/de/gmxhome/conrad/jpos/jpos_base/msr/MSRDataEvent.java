/*
 * Copyright 2019 Martin Conrad
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
 */

package de.gmxhome.conrad.jpos.jpos_base.msr;

import de.gmxhome.conrad.jpos.jpos_base.*;

/**
 * Data event implementation for MSR devices.
 */
public class MSRDataEvent extends JposDataEvent {
    /**
     * Object that contains all data that are necessary to fill data fields
     * before event is really fired.
     */
    public Object TrackData;
    /**
     * Constructor. Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (msr.)MSRService object.
     * @param state Status, see UPOS ErrorEvent.
     * @param trackData Object containing data to be filled in MSRInterface properties.
     */
    public MSRDataEvent(JposBase source, int state, Object trackData) {
        super(source, state);
        TrackData = trackData;
    }

    @Override
    public void setDataProperties() {
        MSRProperties props = (MSRProperties) getPropertySet();
        super.setDataProperties();
        ((MSRService)props.EventSource).MSRInterface.setDataProperties(TrackData);
    }

    @Override
    public String toLogString() {
        return "Status: " + String.format("%08X", getStatus()) + ", Trackdata: " + TrackData.toString();
    }
}
