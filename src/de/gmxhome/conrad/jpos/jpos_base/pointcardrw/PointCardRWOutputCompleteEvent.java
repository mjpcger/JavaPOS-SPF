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
import de.gmxhome.conrad.jpos.jpos_base.JposOutputCompleteEvent;

import java.util.Arrays;

public class PointCardRWOutputCompleteEvent extends JposOutputCompleteEvent {
    /**
     * Array containing the state values of the 6 tracks to be stored in WriteState1 and WriteState2, one value per track.
     */
    public Integer[] State;

    /**
     * Constructor. Parameters passed to base class unchanged.
     *
     * @param source     Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param id         OutputID, see UPOS specification, chapter Common Properties, Methods,and Events - Events - OutputCompleteEvent.
     * @param writestate New values for the corresponding WriteState properties WriteState1 and WriteState2, one state value per track.
     */
    public PointCardRWOutputCompleteEvent(JposBase source, int id, Integer[] writestate) {
        super(source, id);
        State = writestate != null ? Arrays.copyOf(writestate, ((PointCardRWProperties)((PointCardRWService)source).Props).WriteData.length) : null;
    }

    @Override
    public void setOutputCompleteProperties() {
        PointCardRWProperties props = (PointCardRWProperties) getPropertySet();
        super.setOutputCompleteProperties();
        if (State != null) {
            Integer[] val = Arrays.copyOf(props.WriteState, props.WriteState.length);
            for (int i = 0; i < State.length; i++) {
                if (State[i] != null)
                    props.setWriteState(i + 1, State[i]);
            }
            if (!Arrays.equals(props.WriteState, val))
                props.EventSource.logSet("WriteState");
        }
    }
}
