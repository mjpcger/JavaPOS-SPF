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
 * Remote Order Display output error event class. Holds display unit and error message.
 */
public class RemoteOrderDisplayOutputErrorEvent extends JposErrorEvent {
    /**
     * Display units that forced the error. One bit per unit.
     */
    public int Units;

    /**
     * Constructor.
     * @param source    Source, for services implemented with this framework, the (remoteorderdisplay.)RemoteOrderDisplayService object.
     * @param errorcode Error code.
     * @param extended  Extended errorcode.
     * @param units     Display units that forced the event. Value to be stored in property ErrorUnits.
     * @param text      Error message. Value to be stored in property ErrorString.
     */
    public RemoteOrderDisplayOutputErrorEvent(JposBase source, int errorcode, int extended, int units, String text) {
        super(source, errorcode, extended, JposConst.JPOS_EL_OUTPUT, text);
        Units = units;
        Message = text;
    }

    RemoteOrderDisplayOutputErrorEvent(JposBase o, int errorcode, int extended, int locus, int units, String text) {
        super(o, errorcode, extended, locus, text);
        Units = units;
    }


    @Override
    public void setErrorProperties() {
        RemoteOrderDisplayProperties data = (RemoteOrderDisplayProperties) getPropertySet();

        data.EventUnits = Units;
        data.EventSource.logSet("EventUnits");
        data.EventString = Message;
        data.EventSource.logSet("EventString");
    }

    @Override
    public String toLogString() {
        return super.toLogString() + "/" + Units + "/" + Message;
    }

    @Override
    public void clear() {
        try {
            if (getErrorLocus() == JposConst.JPOS_EL_OUTPUT) {
                ((RemoteOrderDisplayService) getSource()).RemoteOrderDisplayInterface.clearOutput(Units);
            } else {
                ((RemoteOrderDisplayService) getSource()).RemoteOrderDisplayInterface.clearInput(Units);
            }
        } catch (JposException e) {
            e.printStackTrace();
        }
    }
}
