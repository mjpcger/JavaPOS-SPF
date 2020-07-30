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

package de.gmxhome.conrad.jpos.jpos_base.pospower;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.JposException;

/**
 * Interface for methods that implement property setter and method calls for the POSPower device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter POS Power.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface POSPowerInterface extends JposBaseInterface {
    /**
     * Final part of setBatteryCriticallyLowThreshold method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>threshold is between 0 and 99.</li>
     * </ul>
     * @param threshold Time delay for motion
     * @throws JposException If an error occurs.
     */
    void batteryCriticallyLowThreshold(int threshold) throws JposException;

    /**
     * Final part of setBatteryLowThreshold method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has been opened,</li>
     *     <li>threshold is between 0 and 99.</li>
     * </ul>
     * @param threshold Time delay for motion
     * @throws JposException If an error occurs.
     */
    void batteryLowThreshold(int threshold) throws JposException;

    /**
     * Final part of setEnforcedShutdownDelayTime method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has been opened,</li>
     *     <li>delay is &ge; 0.</li>
     * </ul>
     * @param delay Time delay for motion
     * @throws JposException If an error occurs.
     */
    void enforcedShutdownDelayTime(int delay) throws JposException;

    /**
     * Final part of restartPOS method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     *     <li>Device has not been claimed by another instance of this device.</li>
     *     <li>CapRestartPOS is true.</li>
     * </ul>
     *
     * @throws JposException If an error occurs.
     */
    void restartPOS() throws JposException;

    /**
     * Final part of shutdownPOS method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     *     <li>Device has not been claimed by another instance of this device.</li>
     *     <li>CapShutdownPOS is true.</li>
     * </ul>
     *
     * @throws JposException If an error occurs.
     */
    void shutdownPOS() throws JposException;

    /**
     * Final part of standbyPOS method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     *     <li>Device has not been claimed by another instance of this device,</li>
     *     <li>reason is one of REASON_REQUEST, REASON_ALLOW or REASON_DENY,</li>
     *     <li>CapStandbyPOS is true or reason &ne; REASON_REQUEST.</li>
     * </ul>
     *
     * @param reason   Reason for the call. See UPOS specification.
     * @throws JposException If an error occurs.
     */
    void standbyPOS(int reason) throws JposException;

    /**
     * Final part of suspendPOS method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     *     <li>Device has not been claimed by another instance of this device,</li>
     *     <li>reason is one of REASON_REQUEST, REASON_ALLOW or REASON_DENY,</li>
     *     <li>CapSuspendPOS is true or reason &ne; REASON_REQUEST.</li>
     * </ul>
     *
     * @param reason   Reason for the call. See UPOS specification.
     * @throws JposException If an error occurs.
     */
    void suspendPOS(int reason) throws JposException;
}
