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

package de.gmxhome.conrad.jpos.jpos_base.micr;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Interface for methods that implement property setter and method calls for the MICR device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter MICR -
 * Magnetic Ink Character Recognition Reader.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface MICRInterface extends JposBaseInterface {
    /**
     * Checks whether MICR data are available for processing. The default implementation returns if CheckType &gt; 0.
     * Otherwise, a UPOS exception will be generated with error code E_EXTENDED and extender error code EMICR_NODATA.
     * The following checks have been made before this method will be invoked:
     * <ul>
     *     <li>The device has been claimed.</li>
     * </ul>
     *
     * @throws JposException    If MICR data cannot be retrieved.
     */
    public void checkNoData() throws JposException;

    /**
     * Checks whether the device is busy. Since MICRService often shares one physical device with a printer, busy means
     * MICR or printer of the device is busy. If checking property State is not enough, this method must be overwritten
     * in a derived class.
     * @throws JposException If device is busy orin error status.
     */
    public void checkBusy() throws JposException;

    /**
     * Final part of BeginInsertion method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>checkBusy did not throw an exception,</li>
     *     <li>The timeout is &ge; 0 or JPOS_FOREVER.</li>
     * </ul>
     * If the service enters insertion mode successfully after BeginInsertion failed, e.g. due to a
     * timeout condition during delayed asynchronous insertion handling, property InsertionMode must
     * be set to true by the specific service implementation to avoid EndInsertion to fail.
     *
     * @param timeout    See UPOS specification, method BeginInsertion.
     * @throws JposException    See UPOS specification, method DisplayText.
     */
    public void beginInsertion(int timeout) throws JposException;

    /**
     * Final part of BeginRemoval method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>checkBusy did not throw an exception,</li>
     *     <li>The timeout is &ge; 0 or JPOS_FOREVER.</li>
     * </ul>
     * If the service enters removal mode successfully after BeginRemoval failed, e.g. due to a
     * timeout condition during delayed asynchronous removal handling, property RemovalMode must
     * be set to true by the specific service implementation to avoid EndRemoval to fail.
     *
     * @param timeout    See UPOS specification, method BeginRemoval.
     * @throws JposException    See UPOS specification, method DisplayText.
     */
    public void beginRemoval(int timeout) throws JposException;

    /**
     * Final part of EndInsertion method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>checkBusy did not throw an exception,</li>
     *     <li>Device is not in insertion mode,</li>
     * </ul>
     *
     * @throws JposException    See UPOS specification, method EndInsertion.
     */
    public void endInsertion() throws JposException;

    /**
     * Final part of EndRemoval method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>checkBusy did not throw an exception,</li>
     *     <li>Device is not in removal mode,</li>
     * </ul>
     *
     * @throws JposException    See UPOS specification, method EndRemoval.
     */
    public void endRemoval() throws JposException;
}
