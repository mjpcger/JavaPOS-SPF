/*
 * Copyright 2022 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.electronicvaluerw;

import de.gmxhome.conrad.jpos.jpos_base.*;

/**
 * Transition event implementation for ElectronicValueRW devices.
 */
public class ElectronicValueRWTransitionEvent extends JposTransitionEvent {
    /**
     * Value for Balance property to be set before firing transition event with EventNumber TE_CONFIRM_REMAINDER_SUBTRACTION.
     * If null (the default), Balance property will not be changed by this event.
     */
    protected final Long Balance;

    /**
     * Value for AdditionalSecurityInformation property to be set before firing transition event with EventNumber
     * TE_CONFIRM_DEVICE_DATA, TE_CONFIRM_SEARCH_TABLE or TE_CONFIRM_AUTHORIZE.
     * If null (the default), AdditionalSecurityInformation property will not be changed by this event.
     */
    protected final String AdditionalSecurityInformation;

    /**
     * Constructor. Parameters passed to base class unchanged.
     *
     * @param source      Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param eventNumber The ID number of the asynchronous I/O device process condition
     * @param pData       Additional information about appropriate response which is dependent upon the specific
     *                    process condition.
     * @param pString     Information about the specific event that has occurred.
     */
    public ElectronicValueRWTransitionEvent(JposBase source, int eventNumber, int pData, String pString) {
        super(source, eventNumber, pData, pString);
        Balance = null;
        AdditionalSecurityInformation = null;
    }

    /**
     * Constructor. Parameters passed to base class unchanged.
     *
     * @param source      Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param eventNumber The ID number of the asynchronous I/O device process condition
     * @param pData       Additional information about appropriate response which is dependent upon the specific
     *                    process condition.
     * @param pString     Information about the specific event that has occurred.
     * @param balance     Information to be stored in property Balance.
     */
    public ElectronicValueRWTransitionEvent(JposBase source, int eventNumber, int pData, String pString, long balance) {
        super(source, eventNumber, pData, pString);
        Balance = balance;
        AdditionalSecurityInformation = null;
    }

    /**
     * Constructor. Parameters passed to base class unchanged.
     *
     * @param source      Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param eventNumber The ID number of the asynchronous I/O device process condition
     * @param pData       Additional information about appropriate response which is dependent upon the specific
     *                    process condition.
     * @param pString     Information about the specific event that has occurred.
     * @param additionalSecurityInformation Information to be stored in property AdditionalSecurityInformation.
     */
    public ElectronicValueRWTransitionEvent(JposBase source, int eventNumber, int pData, String pString, String additionalSecurityInformation) {
        super(source, eventNumber, pData, pString);
        Balance = null;
        AdditionalSecurityInformation = additionalSecurityInformation;
    }

    @Override
    public void setTransitionProperties() {
        ElectronicValueRWProperties props = (ElectronicValueRWProperties)getPropertySet();
        if (Balance != null && Balance != props.Balance) {
            props.Balance = Balance;
            props.EventSource.logSet("Balance");
        }
        if (AdditionalSecurityInformation != null && !AdditionalSecurityInformation.equals(props.AdditionalSecurityInformation)) {
            props.AdditionalSecurityInformation = AdditionalSecurityInformation;
            props.EventSource.logSet("AdditionalSecurityInformation");
        }
    }
}
