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

package de.gmxhome.conrad.jpos.jpos_base.remoteorderdisplay;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Remote Order Display input error event class. Holds display unit and error message.
 */
public class RemoteOrderDisplayInputErrorEvent extends RemoteOrderDisplayOutputErrorEvent {
    /**
     * Constructor.
     * @param source    Source, for services implemented with this framework, the (remoteorderdisplay.)RemoteOrderDisplayService object.
     * @param errorcode Error code.
     * @param extended  Extended errorcode.
     * @param units     Display units that forced the event. Value to be stored in property ErrorUnits.
     * @param text      Error message. Value to be stored in property ErrorString.
     */
    public RemoteOrderDisplayInputErrorEvent(JposBase source, int errorcode, int extended, int units, String text) {
        super(source, errorcode, extended, JposConst.JPOS_EL_INPUT, units, text);
    }

    private RemoteOrderDisplayInputErrorEvent(JposBase o, int errorcode, int extended, int locus, int response, int units, String text) {
        super(o, errorcode, extended, locus, units, text);
    }

    @Override
    public void setErrorProperties() {
        RemoteOrderDisplayProperties data = (RemoteOrderDisplayProperties) getPropertySet();

        data.ErrorUnits = Units;
        data.EventSource.logSet("ErrorUnits");
        data.ErrorString = Message;
        data.EventSource.logSet("ErrorString");
    }

    @Override
    public JposErrorEvent getInputDataErrorEvent() {
        return new RemoteOrderDisplayInputErrorEvent((JposBase) getSource(), getErrorCode(), getErrorCodeExtended(), JposConst.JPOS_EL_INPUT, getErrorResponse(), Units, Message);
    }
}
