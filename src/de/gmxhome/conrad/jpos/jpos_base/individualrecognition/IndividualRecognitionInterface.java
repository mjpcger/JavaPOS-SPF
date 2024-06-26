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

import de.gmxhome.conrad.jpos.jpos_base.JposBaseInterface;
import jpos.JposException;

/**
 * Interface for methods that implement property setter and method calls for the IndividualRecognition device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Individual Recognition.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface IndividualRecognitionInterface extends JposBaseInterface {
    /**
     * Final part of setting IndividualRecognitionFilter. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,/li>
     *     <li>individualRecognitionFilter is neither null nor an empty string.</li>
     * </ul>
     *
     * @param individualRecognitionFilter New value for IndividualRecognitionFilter property.
     * @throws JposException If an error occurs.
     */
    void individualRecognitionFilter(String individualRecognitionFilter) throws JposException;
}
