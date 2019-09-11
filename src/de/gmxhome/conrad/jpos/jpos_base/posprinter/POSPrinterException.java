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

import jpos.*;

/**
 * Printer exception class. Holds printer specific values to be passed to the application.
 */
public class POSPrinterException extends JposException {
    /**
     * Printer station. In case of asynchronous processing, this value will be passed to the ErrorStation property.
     */
    public int Station;

    /**
     * Error level. In case of asynchronous processing, this value will be passed to the ErrorLevel property.
     */
    public int Level;

    /**
     * Creates printer specific exception.
     * @param error     Error code, see JposException.
     * @param extension Extended error code, see JposException.
     * @param message   Error message, see JposException.
     * @param e         Source exception or null, see JposException.
     * @param station   Error station, see UPOS specification of property ErrorStation.
     * @param level     Error level, see UPOS specification of property ErrorLevel.
     */
    public POSPrinterException(int error, int extension, String message, java.lang.Exception e, int station, int level) {
        super(error, extension, message, e);
        Station = station;
        Level = level;
    }
}
