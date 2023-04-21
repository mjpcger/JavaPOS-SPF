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

package de.gmxhome.conrad.jpos.jpos_base.pinpad;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.JposConst;

/**
 * Error event implementation for PINPad devices. For compatibility to previous version only.
 */
public class PINPadErrorEvent extends JposErrorEvent {
    /**
     * Constructor. Parameters passed to base class unchanged.
     *
     * @param source    Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param errorcode ErrorCode, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     * @param extended  ErrorCodeExtended, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     */
    public PINPadErrorEvent(JposBase source, int errorcode, int extended) {
        super(source, errorcode, extended, JposConst.JPOS_EL_INPUT);
    }

    /**
     * Constructor. Parameters passed to base class unchanged.
     *
     * @param source    Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param errorcode ErrorCode, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     * @param extended  ErrorCodeExtended, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     * @param message   Error message from exception.
     */
    public PINPadErrorEvent(JposBase source, int errorcode, int extended, String message) {
        super(source, errorcode, extended, JposConst.JPOS_EL_INPUT, message);
    }
}
