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

package de.gmxhome.conrad.jpos.jpos_base.keylock;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Interface for methods that implement property setter and method calls for the Keylock device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Keylock.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface KeylockInterface extends JposBaseInterface {
    /**
     * Final part of WaitForKeylockChange method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>keyPosition is between 0 and PositionCount,</li>
     *     <li>timeout is &ge; 0 or JPOS_FOREVER.</li>
     * </ul>
     *
     * @param keyPosition   Requested keylock position.
     * @param timeout       Maximum number of milliseconds to wait for the keylock before returning control
     *                      back to the application.
     * @throws JposException If an error occurs.
     */
    public void waitForKeylockChange(int keyPosition, int timeout) throws JposException;
}
