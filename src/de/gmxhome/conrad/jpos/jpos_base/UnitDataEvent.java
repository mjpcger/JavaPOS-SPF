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
 * Data event implementation for subsystem unit devices.
 */
public class UnitDataEvent extends JposDataEvent {
    /**
     * Subsystem unit that forced the event.
     */
    public int Unit;

    /**
     * Constructor. Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (<i>class</i>.)<i>Class</i>Service object.
     * @param state Status,  see UPOS specification, chapter <i>Class</i> - Events - DataEvent.
     * @param unit event source.
     */
    public UnitDataEvent(JposBase source, int state, int unit) {
        super(source, state);
        Unit = unit;
    }

    @Override
    public void setDataProperties() {
        super.setDataProperties();
        JposCommonProperties dev = getPropertySet();
        dev.EventUnitID = Unit;
        dev.EventSource.logSet("EventUnitID");
    }

    @Override
    public String toLogString() {
        return "Unit: " + Unit + ", " + super.getStatus();
    }

}
