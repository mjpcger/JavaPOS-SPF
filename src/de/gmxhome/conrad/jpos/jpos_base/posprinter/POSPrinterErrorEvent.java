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

package de.gmxhome.conrad.jpos.jpos_base.posprinter;

import de.gmxhome.conrad.jpos.jpos_base.*;

import static jpos.JposConst.*;
import static jpos.POSPrinterConst.*;

/**
 * POSPrinter error event class. Holds print station,error level and error message.
 */
public class POSPrinterErrorEvent extends JposErrorEvent {
    /**
     * Printer station. In case of asynchronous processing, this value will be passed to the ErrorStation property.
     */
    public final int Station;

    /**
     * Error level. In case of asynchronous processing, this value will be passed to the ErrorLevel property.
     */
    public final int Level;

    /**
     * Error message. In case of asynchronous processing, this value will be passed to ErrorString property.
     */
    public final String Text;

    /**
     * Constructor. Parameters passed to base class unchanged.
     *
     * @param source   Source, for services implemented with this framework, the (posprinter.)POSPrinterService object.
     * @param errorcode Error code.
     * @param extended  Extended errorcode.
     * @param level     Error level. Value to be stored in property ErrorLevel.
     * @param station   Error station. Value to be stored in property ErrorStation.
     * @param text      Error description. Value to be stored in property ErrorString.
     */
    public POSPrinterErrorEvent(JposBase source, int errorcode, int extended, int station, int level, String text) {
        super(source, errorcode, extended, JPOS_EL_OUTPUT, text);
        Station = station;
        Level = level;
        Text = text;
    }

    @Override
    public void setErrorProperties() {
        POSPrinterProperties data = (POSPrinterProperties) getPropertySet();

        if ((data.ErrorLevel = Level) != PTR_EL_NONE) {
            data.ErrorStation = Station;
            data.EventSource.logSet("ErrorStation");
            data.ErrorString = Text;
            data.EventSource.logSet("ErrorString");
        }
    }

    @Override
    public String toLogString() {
        return super.toLogString() + "/" + Level + "/" + Station + "/" + Text;
    }
}
