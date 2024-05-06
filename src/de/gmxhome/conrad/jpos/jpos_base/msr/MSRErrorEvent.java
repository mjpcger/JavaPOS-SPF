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

import static jpos.JposConst.*;

/**
 * MSR error events, work partly similar to data events, depending on error level
 */
public class MSRErrorEvent extends JposErrorEvent {
    /**
     * Object that contains all data that are necessary to fill data fields
     * before event is really fired. Used when ErrorReportingType = MSR_ERT_TRACK.
     */
    public final Object TrackData;

    /**
     * Constructor. Parameters passed to base class unchanged.
     *
     * @param source  Source, for services implemented with this framework, the (msr.)MSRService object.
     * @param errorcode Error code.
     * @param extended  Extended errorcode.
     * @param data track data object
     */
    public MSRErrorEvent(JposBase source, int errorcode, int extended, Object data) {
        super(source, errorcode, extended, JPOS_EL_INPUT);
        TrackData = data;
    }

    @Override
    public void setErrorProperties() {
        MSRProperties props = (MSRProperties)getPropertySet();
        super.setErrorProperties();
        ((MSRService)props.EventSource).MSRInterface.setDataProperties(TrackData);
    }

    @Override
    public String toLogString() {
        return super.toLogString() + ", Trackdata: " + TrackData.toString();
    }
}
