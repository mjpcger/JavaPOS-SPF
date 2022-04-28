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

import jpos.events.TransitionEvent;

/**
 * Transition event. The default implementation for event handling does not
 * handle instances of TransitionEvent, it handles only instances of JposTransitionEvent
 * instead.
 */
public class JposTransitionEvent extends TransitionEvent {
    /**
     * Constructor. Parameters passed to base class unchanged.
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param eventNumber The ID number of the asynchronous I/O device process condition
     * @param pData Additional information about appropriate response which is dependent upon the specific
     *              process condition.
     * @param pString Information about the specific event that has occurred.
     */
    public JposTransitionEvent(JposBase source, int eventNumber, int pData, String pString) {
        super(source, eventNumber, pData, pString);
    }

    /**
     * Generates string describing the transition event for logging purposes.
     * @return Describing string.
     */
    public String toLogString() {
        return Integer.toString(getEventNumber()) + "/" + getData() + "/" + getString();
    }

    /**
     * Returns property set bound to the event source.
     * @return Property set.
     */
    public JposCommonProperties getPropertySet() {
        return ((JposBase) getSource()).Props;
    }

    /**
     * Sets the Transition properties stored within the JposTransitionEvent into
     * the JposCommonProperties object stored in Source. The corresponding properties must be
     * defined within derived classes.
     */
    public void setTransitionProperties() {
    }

    boolean WriteProtected = false;

    @Override
    public void setData(int data) {
        if (!WriteProtected)
            super.setData(data);
    }

    @Override
    public void setString(String string) {
        if (!WriteProtected)
            super.setString(string);
    }
}
