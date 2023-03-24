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

package de.gmxhome.conrad.jpos.jpos_base.rfidscanner;

import de.gmxhome.conrad.jpos.jpos_base.JposBase;
import de.gmxhome.conrad.jpos.jpos_base.JposErrorEvent;

/**
 * Error event implementation for RFIDScanner devices. Extends JposErrorEvent for RFIDScanner input errors: Even if no
 * error properties exist, data properties (CurrentTagID, CurrentTagUserData, CurrentTagProtocol and TagCount) will be
 * cleared.
 */
public class RFIDScannerErrorEvent extends JposErrorEvent {
    /**
     * Constructor. Parameters passed to base class unchanged.
     *
     * @param source    Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param errorcode ErrorCode, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     * @param extended  ErrorCodeExtended, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     * @param locus     ErrorLocus, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     */
    public RFIDScannerErrorEvent(JposBase source, int errorcode, int extended, int locus) {
        super(source, errorcode, extended, locus);
    }

    /**
     * Constructor. Parameters passed to base class unchanged.
     *
     * @param source    Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param errorcode ErrorCode, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     * @param extended  ErrorCodeExtended, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     * @param locus     ErrorLocus, see UPOS specification, chapter Common Properties, Methods, and Events - Events - ErrorEvent.
     * @param message   Error message from exception.
     */
    public RFIDScannerErrorEvent(JposBase source, int errorcode, int extended, int locus, String message) {
        super(source, errorcode, extended, locus, message);
    }

    @Override
    public void setErrorProperties() {
        super.setErrorProperties();
        ((RFIDScannerService)getPropertySet().EventSource).Props.clearDataProperties();
    }
}
