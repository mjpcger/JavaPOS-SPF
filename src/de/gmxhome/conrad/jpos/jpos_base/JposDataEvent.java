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

package de.gmxhome.conrad.jpos.jpos_base;

import jpos.events.DataEvent;

/**
 * Data event with event data storage and method to fill data properties.
 * The default implementation for event handling does not handle instances of DataEvent, it handles only
 * instances of JposDataEvent instead.
 */
public class JposDataEvent extends DataEvent {
    /**
     * Constructor. Parameters passed to base class unchanged.
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state Status, see UPOS specification.
     */
    public JposDataEvent(JposBase source, int state) {
        super(source, state);
    }

    /**
     * Sets the data properties stored within the JposDataEvent into
     * the JposCommonProperties object. The corresponding properties must be
     * defined within derived classes.
     */
    public void setDataProperties() {
    }

    /**
     * Generates string describing the data event for logging purposes.
     * @return Describing string.
     */
    public String toLogString() {
        return "Status: " + super.getStatus();
    }

    /**
     * Returns property set bound to the event source.
     * @return Property set.
     */
    public JposCommonProperties getPropertySet() {
        Object obj = getSource();
        return ((JposBase) obj).Props;
    }
}
