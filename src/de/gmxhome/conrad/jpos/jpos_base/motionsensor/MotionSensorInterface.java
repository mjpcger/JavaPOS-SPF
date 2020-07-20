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
 */

package de.gmxhome.conrad.jpos.jpos_base.motionsensor;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Interface for methods that implement property setter and method calls for the MotionSensor device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Motion Sensor.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface MotionSensorInterface extends JposBaseInterface {
    /**
     * Final part of setTimeout method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>timeout is &gt; 0.</li>
     * </ul>
     * @param timeout Time delay for motion
     * @throws JposException If an error occurs.
     */
    public void timeout(int timeout) throws JposException;

    /**
     * Final part of WaitForMotion method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>timeout is a positive value or FOREVER.</li>
     * </ul>
     *
     * @param timeout   Number of milliseconds to wait before firing an exception with JPOS_E_TIMEOUT.
     * @throws JposException If an error or a timeout occurs.
     */
    public void waitForMotion(int timeout) throws JposException;
}
