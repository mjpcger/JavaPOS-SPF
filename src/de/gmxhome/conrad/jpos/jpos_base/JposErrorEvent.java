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

import jpos.JposConst;
import jpos.JposException;
import jpos.events.ErrorEvent;

/**
 * Error event.
 * The default implementation for event handling does not handle instances of ErrorEvent, it handles only
 * instances of JposErrorEvent instead.
 */
public class JposErrorEvent extends ErrorEvent {
    /**
     * Contains the error message stored in a JposException passed to method createErrorEvent of a JposOutputRequest.
     * Contains an empty string as default otherwise.
     */
    public String Message = "";

    /**
     * Constructor. Parameters passed to base class unchanged.
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param errorcode ErrorCode, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     * @param extended ErrorCodeExtended, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     * @param locus ErrorLocus, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     */
    public JposErrorEvent(JposBase source, int errorcode, int extended, int locus) {
        super(source, errorcode, extended, locus, locus == JposConst.JPOS_EL_OUTPUT ? JposConst.JPOS_ER_RETRY : (locus == JposConst.JPOS_EL_INPUT ? JposConst.JPOS_ER_CLEAR : JposConst.JPOS_ER_CONTINUEINPUT));
    }

    /**
     * Constructor. Parameters passed to base class unchanged.
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param errorcode ErrorCode, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     * @param extended ErrorCodeExtended, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     * @param locus ErrorLocus, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     * @param message Error message from exception.
     */
    public JposErrorEvent(JposBase source, int errorcode, int extended, int locus, String message) {
        super(source, errorcode, extended, locus, locus == JposConst.JPOS_EL_OUTPUT ? JposConst.JPOS_ER_RETRY : (locus == JposConst.JPOS_EL_INPUT ? JposConst.JPOS_ER_CLEAR : JposConst.JPOS_ER_CONTINUEINPUT));
        Message = message;
    }

    /**
     * Sets the error properties stored within the JposErrorEvent into
     * the JposCommonProperties object stored in Source. The corresponding properties must be
     * defined within derived classes.
     */
    public void setErrorProperties() {
    }
    /**
     * Generates string describing the error event for logging purposes.
     * @return Describing string.
     */
    public String toLogString() {
        return Integer.toString(getErrorCode()) + "/" + getErrorCodeExtended() + "/" + getErrorLocus() + (Message.equals("") ? "" : "/" + Message);
    }

    /**
     * Returns property set bound to the event source.
     * @return Property set.
     */
    public JposCommonProperties getPropertySet() {
        return ((JposBase) getSource()).Props;
    }

    /**
     * Returns an input data event corresponding to the given event. Will be created automatically within input event
     * handler.
     * @return JposErrorEvent with locus input data.
     */
    public JposErrorEvent getInputDataErrorEvent() {
        return new JposErrorEvent((JposBase) getSource(), getErrorCode(), getErrorCodeExtended(), JposConst.JPOS_EL_INPUT_DATA);
    }

    /**
     * Clears all input data or buffered output requests, error and output complete events that belong to this error
     * event. Neither ClearInput nor ClearOutput should throw an exception because both operations should be implemented
     * as internal operations that do not need to throw an exception. However, if an implementation should throw a
     * JposException, it will be caught and ignored.
     */
    public void clear() {
        try {
            if (getErrorLocus() == JposConst.JPOS_EL_OUTPUT) {
                ((JposBase) getSource()).DeviceInterface.clearOutput();
            } else {
                ((JposBase) getSource()).DeviceInterface.clearInput();
            }
        } catch (JposException e) {
            e.printStackTrace();
        }
    }
}
