/*
 * Copyright 2020 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.signaturecapture;

import de.gmxhome.conrad.jpos.jpos_base.JposBaseInterface;
import jpos.JposException;

/**
 * Interface for methods that implement property setter and method calls for the SignatureCature device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Signature Cature.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface SignatureCaptureInterface extends JposBaseInterface {
    /**
     * Final part of setting RealTimeDataEnabled. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has been opened,</li>
     *     <li>The new property value is false or CapRealTimeData is true,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or flag equals the previous value of RealTimeDataEnabled.</li>
     * </ul>
     *
     * @param flag     New property value.
     * @throws JposException If an error occurs.
     */
    public void realTimeDataEnabled(boolean flag) throws JposException;

    /**
     * Final part of BeginCapture method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     *
     * @param formName          Identifier for signature capture form.
     * @throws JposException    If an error occurs.
     */
    public void beginCapture(String formName) throws JposException;

    /**
     * Final part of EndCapture method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     *
     * @throws JposException    If an error occurs.
     */
    public void endCapture() throws JposException;
}
