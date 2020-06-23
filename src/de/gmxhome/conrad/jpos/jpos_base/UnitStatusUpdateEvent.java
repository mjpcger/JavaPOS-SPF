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
 * Status update event implementation for subsystem unit devices.
 */
public class UnitStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Target units of the corresponding status change.
     */
    public int Units;

    /**
     * Constructor, Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (<i>class</i>.)<i>Class</i>Service object.
     * @param state Status,  see UPOS specification, chapter <i>Class</i> - Events - StatusUpdateEvent.
     * @param units Units where status has been changed.
     */
    public UnitStatusUpdateEvent(JposBase source, int state, int units) {
        super(source, state);
        Units = units;
    }

    @Override
    public JposStatusUpdateEvent copyEvent(JposBase o) {
        return new UnitStatusUpdateEvent(o, getStatus(), Units);
    }

    @Override
    public void setLateProperties() {
        super.setLateProperties();
        JposCommonProperties data = getPropertySet();
        if (data.EventUnits != Units) {
            data.EventUnits = Units;
            data.EventSource.logSet("EventUnits");
        }
    }

    @Override
    public boolean checkStatusCorresponds() {
        return false;
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        super.setAndCheckStatusProperties();
        return true;
    }

}
