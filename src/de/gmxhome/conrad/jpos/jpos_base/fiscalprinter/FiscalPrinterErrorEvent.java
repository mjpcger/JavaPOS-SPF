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

package de.gmxhome.conrad.jpos.jpos_base.fiscalprinter;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.JposConst;

/**
 * FiscalPrinter error events, holds additional parameters.
 */
public class FiscalPrinterErrorEvent extends JposErrorEvent {
    /**
     * Getter for error level.
     * @return New value for ErrorLevel property.
     */
    public int getLevel() {
        return Level;
    }
    private int Level;

    /**
     * Getter for error output ID.
     * @return  New value for ErrorOutID property.
     */
    public int getOutputID() {
        return OutputID;
    }
    private int OutputID;

    /**
     * Getter for error state.
     * @return New value for ErrorState property.
     */
    public int getState() {
        return State;
    }
    private int State;

    /**
     * Getter for error station.
     * @return New value for ErrorStation property.
     */
    public int getStation() {
        return Station;
    }
    private int Station;

    /**
     * Getter for error message.
     * @return New value for ErrorString property.
     */
    public String getMessage() {
        return Message;
    }
    private String Message;

    /**
     * Constructor. Parameters are:
     *
     * @param source    Source, for services implemented with this framework, the (fiscalprinter.)FiscalPrinterService object.
     * @param errorcode Error code. See UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     * @param extended  Extended errorcode. See UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     * @param level     Error level, one of EL_RECOVERABLE, EL_FATAL or EL_BLOCKED. To be stored in property ErrorLevel.
     * @param outputID  Output ID of the asynchronous command that generated the error. To be stored in property ErrorOutID.
     * @param state     Printer state. To be stored in property ErrorState.
     * @param station   Station or stations that were printing when an error was detected. To be stored in property ErrorStation.
     * @param message   Error message. To be stored in property ErrorString.
     */
    public FiscalPrinterErrorEvent(JposBase source, int errorcode, int extended, int level, int outputID, int state, int station, String message) {
        super(source, errorcode, extended, JposConst.JPOS_EL_OUTPUT);
        Level = level;
        OutputID = outputID;
        State = state;
        Station = station;
        Message = message;
    }

    @Override
    public void setErrorProperties() {
        FiscalPrinterProperties data = (FiscalPrinterProperties) getPropertySet();

        if (Level != data.ErrorLevel) {
            data.ErrorLevel = Level;
            data.EventSource.logSet("ErrorLevel");
        }
        if (data.ErrorOutID != OutputID) {
            data.ErrorOutID = OutputID;
            data.EventSource.logSet("ErrorOutID");
        }
        if (data.ErrorState != State) {
            data.ErrorState = State;
            data.EventSource.logSet("ErrorState");
        }
        if (data.ErrorStation != Station) {
            data.ErrorStation = Station;
            data.EventSource.logSet("ErrorStation");
        }
        if (!data.ErrorString.equals(Message)) {
            data.ErrorString = Message;
            data.EventSource.logSet("ErrorString");
        }
    }

    @Override
    public String toLogString() {
        return super.toLogString() + "/" + Level + "/" + OutputID + "/" + State + "/" + Station + "/" + Message;
    }
}
