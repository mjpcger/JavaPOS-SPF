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

package de.gmxhome.conrad.jpos.jpos_base.pointcardrw;

import de.gmxhome.conrad.jpos.jpos_base.JposBase;
import de.gmxhome.conrad.jpos.jpos_base.JposErrorEvent;

import java.util.Arrays;

import static jpos.JposConst.*;

/**
 * Error event implementation for PointCardRW devices.
 */
public class PointCardRWErrorEvent extends JposErrorEvent {
    /**
     * Array containing 6 Strings with track data. Empty strings if a track has not been read.
     */
    public final String[] Tracks;

    /**
     * Array containing the state values of the 6 tracks to be stored in ReadState1 and ReadState2.
     */
    public final Integer[] State;

    /**
     * Array containing the length in bytes of the 6 tracks to be stored i RecvLength1 and RecvLength2.
     */
    public final Integer[] Length;

    /**
     * Constructor. Parameters passed to base class unchanged. For input error events, contains tracks read.
     *
     * @param source    Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param errorcode ErrorCode, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     * @param extended  ErrorCodeExtended, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     * @param tracks    Array containing track data of tracks 0-6, an empty string for unreadable tracks.
     * @param statelength Arrays containing states of tracks read and optional lengths of tracks in bytes. If lengths are
     *                    not specified, length will be computed from the string lengths stored in tracks.
     */
    public PointCardRWErrorEvent(JposBase source, int errorcode, int extended, String[] tracks, Integer[]... statelength) {
        super(source, errorcode, extended, JPOS_EL_INPUT);
        PointCardRWProperties props = (PointCardRWProperties) source.Props;
        Tracks = Arrays.copyOf(tracks, props.TrackData.length);
        State = Arrays.copyOf(statelength[0], Tracks.length);
        if (statelength.length > 1)
            Length = Arrays.copyOf(statelength[1], Tracks.length);
        else {
            Length = new Integer[Tracks.length];
            for (int i = 0; i < Length.length; i++)
                Length[i] = Tracks[i].length();
        }
    }

    /**
     * Constructor. Parameters passed to base class unchanged. For output error events, contains write states
     *
     * @param source    Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param errorcode ErrorCode, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     * @param extended  ErrorCodeExtended, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     * @param message   Error message from exception.
     * @param state     Status for written tracks, one value per track. null for unmodified tracks.
     */
    public PointCardRWErrorEvent(JposBase source, int errorcode, int extended, String message, Integer[]state) {
        super(source, errorcode, extended, JPOS_EL_OUTPUT, message);
        PointCardRWProperties props = (PointCardRWProperties) source.Props;
        State = Arrays.copyOf(state, props.WriteData.length);
        Tracks = null;
        Length = null;
    }

    @Override
    public void setErrorProperties() {
        PointCardRWProperties props = (PointCardRWProperties) getPropertySet();
        Integer[] val;
        if (Tracks != null && Length != null) {
            String[] previoustracks = props.TrackData;
            props.TrackData = Tracks;
            for(int i = 0; i < Tracks.length; i++) {
                if (props.TrackData[i] == null)
                    props.TrackData[i] = "";
                if (!props.TrackData[i].equals(previoustracks[i]))
                    ((PointCardRWService)getSource()).logSet(Tracks,i, "Track%dData", i + 1);
            }
            val = Arrays.copyOf(props.ReadState, props.ReadState.length);
            for (int i = 0; i < State.length; i++)
                props.setReadState(i + 1, State[i]);
            if (!Arrays.equals(val, props.ReadState))
                props.EventSource.logSet("ReadState");
            val = Arrays.copyOf(props.RecvLength, props.RecvLength.length);
            for (int i = 0; i < Length.length; i++)
                props.setRecvLength(i + 1, Length[i]);
            if (!Arrays.equals(val, props.RecvLength))
                props.EventSource.logSet("RecvLength");
        } else if (State != null){
            val = Arrays.copyOf(props.WriteState, props.WriteState.length);
            for (int i = 0; i < State.length; i++) {
                if (State[i] != null)
                    props.setWriteState(i + 1, State[i]);
            }
            if (!Arrays.equals(props.WriteState, val))
                props.EventSource.logSet("WriteState");
        }
    }
}
