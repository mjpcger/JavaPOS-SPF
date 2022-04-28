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

import jpos.events.DirectIOEvent;

/**
 * Direct IO event. The default implementation for event handling does not
 * handle instances of DirectIOEvent, it handles only instances of JposDirectIOEvent
 * instead.
 */
public class JposDirectIOEvent extends DirectIOEvent {
    /**
     * Constructor. Parameters passed to base class unchanged.
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param eventNumber Event number, vendor specific
     * @param data Data, additional vendor specific data
     * @param obj Object, additional data container, vendor specific
     */
    public JposDirectIOEvent(JposBase source, int eventNumber, int data, Object obj) {
        super(source, eventNumber, data, obj);
    }

    /**
     * Generates string describing the direct IO event for logging purposes. Since a meaningful
     * interpretation of getObject() is driver specific, any object derived from JposDevice shoud
     * not fire JposDirectIOEvents, it should fire specific events derived from JposDirectIOEvent
     * instead. Such derived classes should overwrite toLogString to add a meaningful interpretation
     * of getObject().
     * @return Describing string.
     */
    public String toLogString() {
        return Integer.toString(getEventNumber()) + "/" + getData();
    }

    /**
     * Returns property set bound to the event source.
     * @return Property set.
     */
    public JposCommonProperties getPropertySet() {
        return ((JposBase) getSource()).Props;
    }

    /**
     * Sets the directIO properties stored within the JposDirectIOEvent into
     * the JposCommonProperties object stored in Source. The corresponding properties must be
     * defined within derived classes.
     */
    public void setDirectIOProperties() {
        this.setData(1);
    }

    boolean WriteProtected = false;

    @Override
    public void setData(int data) {
        if (!WriteProtected)
            super.setData(data);
    }

    @Override
    public void setObject(Object obj) {
        if (!WriteProtected)
            super.setObject(obj);
    }
}
