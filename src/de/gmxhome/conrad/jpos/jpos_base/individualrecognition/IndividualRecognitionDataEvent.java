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
import jpos.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;

/**
 * Data event implementation for IndividualRecognition devices.
 */
@SuppressWarnings("unused")
public class IndividualRecognitionDataEvent extends JposDataEvent {
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
     * Constructor. Parameters source and state will be passed to base class unchanged. Parameter data contains all new
     * input property values. One or two values must be specified: The first value becomes the new value of property
     * IndividualIDs after event delivery and - if given - the second value becomes the new value of property
     * IndividualRecognitionInformation.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     * @param data   One or two new values for properties IndividualIDs and (optional) IndividualRecognitionInformation.
     * @throws JposException If no or more than two data parameters have been passed.
     */
    public IndividualRecognitionDataEvent(JposBase source, int state, String... data) throws JposException {
        super(source, state);
        check(data.length == 0 || data.length > 2, JPOS_E_FAILURE, "Invalid number of property values: " + data.length);
        IDs = data[0] == null ? "" : data[0];
        Information = data.length == 1 ? null : data[1];
    }

    @Override
    public void setDataProperties() {
        super.setDataProperties();
        IndividualRecognitionProperties data = (IndividualRecognitionProperties) getPropertySet();
        if (!data.IndividualIDs.equals(IDs)) {
            data.IndividualIDs = IDs;
            data.EventSource.logSet("IndividualIDs");
        }
        if (Information != null && !data.IndividualRecognitionInformation.equals(Information)) {
            data.IndividualRecognitionInformation = Information;
            data.EventSource.logSet("IndividualRecognitionInformation");
        }
    }
}
