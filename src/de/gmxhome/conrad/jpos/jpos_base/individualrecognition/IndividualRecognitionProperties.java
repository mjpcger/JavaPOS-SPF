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

package de.gmxhome.conrad.jpos.jpos_base.individualrecognition;

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import jpos.JposException;

/**
 * Class containing the individual recognition specific properties, their default values and default implementations of
 * IndividualRecognitionInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Individual Recognition.
 */
public class IndividualRecognitionProperties extends JposCommonProperties implements IndividualRecognitionInterface {
    /**
     * Internal property to be used for handling of property IndividualRecognitionInformation, default: false. If true,
     * IndividualRecognitionInformation will be handles as input property: It will be possible to set it before event
     * delivery and can be cleared via clearInputProperties. If false, clearInputProperties will not change property
     * IndividualRecognitionInformation.<br>
     * This property has been specified because the UPOS specification does not specify clearly whether property
     * IndividualRecognitionInformation is an input property or not.
     */
    public boolean IndividualRecognitionInformationIsInputProperty = false;
    /**
     * UPOS property CapIndividualList. Default: an empty string. Must be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public String CapIndividualList = "";

    /**
     * UPOS property IndividualIDs. Default: an empty string. Will be set before a DataEvent will be delivered. Can be
     * reset via method ClearInputProperties.
     */
    public String IndividualIDs = "";

    /**
     * UPOS property IndividualRecognitionFilter. Default: an empty string. Must be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public String IndividualRecognitionFilter = "";

    /**
     * UPOS property IndividualRecognitionInformation. Default: an empty string. Will be set before a DataEvent will be
     * delivered. Can be reset via method ClearInputProperties.
     */
    public String IndividualRecognitionInformation = "";

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected IndividualRecognitionProperties(int dev) {
        super(dev);
    }

    @Override
    public void individualRecognitionFilter(String individualRecognitionFilter) throws JposException {
        IndividualRecognitionFilter = individualRecognitionFilter;
    }

    @Override
    public void clearDataProperties() {
        if (!IndividualIDs.equals("")) {
            IndividualIDs = "";
            EventSource.logSet("IndividualIDs");
        }
        if (IndividualRecognitionInformationIsInputProperty && !IndividualRecognitionInformation.equals("")) {
            IndividualRecognitionInformation = "";
            EventSource.logSet("IndividualRecognitionInformation");
        }
    }
}
