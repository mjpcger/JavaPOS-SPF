/*
 * Copyright 2024 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.individualrecognition;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.JposException;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;

@SuppressWarnings("unused")
public class IndividualRecognitionErrorEvent extends JposErrorEvent {
    /**
     * Holds the value to be stored in property IndividualIDs before event delivery.
     */
    public final String IDs;

    /**
     * Holds the value to be stored in property IndividualRecognitionInformation before event delivery. If null, property
     * IndividualRecognitionInformation will not be changed before event delivery.
     */
    public final String Information;

    /**
     * Constructor. Parameters source, errorcode and extended will be passed to base class unchanged. Parameter
     * data contains all new input values and optionally an error message text. Up to three values must be passed:
     * If passed and not null, the first value becomes the new value of property IndividualIDs after event delivery.
     * If passed and not null, the second value becomes the new value of property IndividualRecognitionInformation.
     * If a third value has been passed, it specifies an error message text.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param errorcode Error code, see UPOS specification.
     * @param extended  Extended error code, see UPOS specification.
     * @param data   One or two new values for properties IndividualIDs and (optional) IndividualRecognitionInformation.
     */
    public IndividualRecognitionErrorEvent(JposBase source, int errorcode, int extended, String... data) throws JposException {
        super(source, errorcode, extended, JPOS_EL_INPUT, data.length == 3 ? data[2] : "");
        check(data.length == 0 || data.length > 3, JPOS_E_FAILURE, "Invalid number of property values: " + data.length);
        check(data.length == 3 && data[2] == null, JPOS_E_FAILURE, "Error message must not be null");
        IDs = data[0];
        Information = data.length > 1 ? data[1] : null;
    }

    @Override
    public void setErrorProperties() {
        super.setErrorProperties();
        IndividualRecognitionProperties data = (IndividualRecognitionProperties) getPropertySet();
        if (IDs != null && !data.IndividualIDs.equals(IDs)) {
            data.IndividualIDs = IDs;
            data.EventSource.logSet("IndividualIDs");
        }
        if (Information != null && !data.IndividualRecognitionInformation.equals(Information)) {
            data.IndividualRecognitionInformation = Information;
            data.EventSource.logSet("IndividualRecognitionInformation");
        }
    }
}
