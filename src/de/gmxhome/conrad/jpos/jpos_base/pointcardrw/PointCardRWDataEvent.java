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

import de.gmxhome.conrad.jpos.jpos_base.*;

import java.util.Arrays;

/**
 * Data event implementation for PointCardRW devices.
 */
public class PointCardRWDataEvent extends JposDataEvent {
    /**
     * Array containing 6 Strings with track data. Empty strings if a track has not been read.
     */
    public final String[] Tracks;

    /**
     * Array containing the state values of the 6 tracks to be stored in ReadState1 and ReadState2.
     */
    public final Integer[] State;

    /**
     * Array containing the length in bytes of the 6 tracks to be stored in RecvLength1 and RecvLength2.
     */
    public final Integer[] Length;

    /**
     * Constructor.
     *
     * @param source      Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param tracks      Array containing track data read for all possible 6 tracks.
     * @param statelength Arrays containing states of tracks read and optional lengths of tracks in bytes. If lengths are
     *                    not specified, length will be computed from the string lengths stored in tracks.
     */
    public PointCardRWDataEvent(JposBase source, String[] tracks, Integer[]... statelength) {
        super(source, 0);
        PointCardRWProperties props = (PointCardRWProperties) source.Props;
        Tracks = Arrays.copyOf(tracks, props.TrackData.length);
        State = Arrays.copyOf(statelength[0], Tracks.length);
        if (statelength.length > 1)
            Length = Arrays.copyOf(statelength[1], Tracks.length);
        else {
            Length = new Integer[State.length];
            for (int i = 0; i < Length.length; i++)
                Length[i] = Tracks[i].length();
        }
    }

    @Override
    public void setDataProperties() {
        super.setDataProperties();
        PointCardRWProperties props = (PointCardRWProperties) getPropertySet();
        boolean changed = !Arrays.equals(props.TrackData, Tracks);
        props.TrackData = Arrays.copyOf(Tracks, props.TrackData.length);
        if (changed)
            props.EventSource.logSet("TrackData");
        Integer[] val = Arrays.copyOf(props.ReadState, props.ReadState.length);
        for (int i = 0; i < State.length; i++)
            props.setReadState(i + 1, State[i]);
        if (!Arrays.equals(val, props.ReadState))
            props.EventSource.logSet("ReadState");
        val = Arrays.copyOf(props.RecvLength, props.RecvLength.length);
        for (int i = 0; i < Length.length; i++)
            props.setRecvLength(i + 1, Length[i]);
        if (!Arrays.equals(val, props.RecvLength))
            props.EventSource.logSet("RecvLength");
    }
}
