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

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.JposException;
import jpos.services.IndividualRecognitionService116;

/**
 * IndividualRecognition service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class IndividualRecognitionService extends JposBase implements IndividualRecognitionService116 {
    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public IndividualRecognitionService(JposCommonProperties props, JposDevice device) {
        super(props, device);
    }

    /**
     * Instance of a class implementing the IndividualRecognitionInterface for individual recognition specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public IndividualRecognitionInterface IndividualRecognition;

    @Override
    public String getCapIndividualList() throws JposException {
        return null;
    }

    @Override
    public String getIndividualIDs() throws JposException {
        return null;
    }

    @Override
    public String getIndividualRecognitionFilter() throws JposException {
        return null;
    }

    @Override
    public void setIndividualRecognitionFilter(String s) throws JposException {

    }

    @Override
    public String getIndividualRecognitionInformation() throws JposException {
        return null;
    }
}
