/*
 * Copyright 2020 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base;

/**
 * Subsystem units complete event class. Holds unit and error message.
 */
public class UnitOutputCompleteEvent extends JposOutputCompleteEvent {
    /**
     * Target units of the corresponding output request.
     */
    public int Units;

    /**
     * Constructor. Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (<i>class</i>.)<i>Class</i>Service object.
     * @param id OutputID, see JposOutputCompleteEvent
     * @param units Units where output has been completed.
     */
    public UnitOutputCompleteEvent(JposBase source, int id, int units) {
        super(source, id);
        Units = units;
    }

    @Override
    public void setOutputCompleteProperties() {
        JposCommonProperties props = getPropertySet();
        props.EventUnits = Units;
        props.EventSource.logSet("EventUnits");
    }

    @Override
    public String toLogString() {
        return super.toLogString() + "/" + Units;
    }

}
