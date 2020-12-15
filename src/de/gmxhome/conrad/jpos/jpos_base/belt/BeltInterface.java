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

package de.gmxhome.conrad.jpos.jpos_base.belt;

import de.gmxhome.conrad.jpos.jpos_base.JposBaseInterface;
import jpos.JposException;

/**
 * Interface for methods that implement property setter and method calls for the Belt device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Belt.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface BeltInterface extends JposBaseInterface {
    /**
     * Final part of setting AutoStopBackward. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>CapAutoStopBackward is true or flag is false.</li>
     * </ul>
     *
     * @param flag New value for AutoStopBackward property.
     * @throws JposException If an error occurs.
     */
    public void autoStopBackward(boolean flag) throws JposException;

    /**
     * Final part of setting AutoStopBackwardDelayTime. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>CapAutoStopBackward is true,</li>
     *     <li>delay is FOREVER or positive.</li>
     * </ul>
     *
     * @param delay Time delay in milliseconds for automatic stop in backward direction.
     * @throws JposException If an error occurs.
     */
    public void autoStopBackwardDelayTime(int delay) throws JposException;

    /**
     * Final part of setting AutoStopForward. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>CapAutoStopForward is true or flag is false.</li>
     * </ul>
     *
     * @param flag New value for AutoStopForward property.
     * @throws JposException If an error occurs.
     */
    public void autoStopForward(boolean flag) throws JposException;

    /**
     * Final part of setting AutoStopForwardDelayTime. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>CapAutoStopForward is true,</li>
     *     <li>delay is FOREVER or positive.</li>
     * </ul>
     *
     * @param delay Time delay in milliseconds for automatic stop in forward direction.
     * @throws JposException If an error occurs.
     */
    public void autoStopForwardDelayTime(int delay) throws JposException;

    /**
     * Final part of AdjustItemCount method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Specified direction is AIC_BACKWARD or AIC_FORWARD,</li>
     *     <li>If direction is AIC_FORWARD, CapAutoStopForwardItemCount is true,</li>
     *     <li>If direction is AIC_BACKWARD, CapAutoStopBackwardItemCount is true.</li>
     * </ul>
     *
     * @param direction   Affected direction, one of AIC_BACKWARD and AIC_FORWARD.
     * @param count       Number of items to be added to current item count. Max be positive or negative.
     * @throws JposException If an error occurs.
     */
    public void adjustItemCount(int direction, int count) throws JposException;

    /**
     * Final part of MoveBackward method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapMoveBackward is true,</li>
     *     <li>Specified speed is between 1 and CapSpeedStepsBackward.</li>
     * </ul>
     *
     * @param speed   New belt speed.
     * @throws JposException If an error occurs.
     */
    public void moveBackward(int speed) throws JposException;

    /**
     * Final part of MoveForward method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Specified speed is between 1 and CapSpeedStepsForward.</li>
     * </ul>
     *
     * @param speed   New belt speed.
     * @throws JposException If an error occurs.
     */
    public void moveForward(int speed) throws JposException;

    /**
     * Final part of ResetBelt method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     *
     * @throws JposException If an error occurs.
     */
    public void resetBelt() throws JposException;

    /**
     * Final part of ResetItemCount method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Specified direction is RIC_BACKWARD or RIC_FORWARD,</li>
     *     <li>If direction is RIC_FORWARD, CapAutoStopForwardItemCount is true,</li>
     *     <li>If direction is RIC_BACKWARD, CapAutoStopBackwardItemCount is true.</li>
     * </ul>
     *
     * @param direction   Affected direction, one of RIC_BACKWARD and RIC_FORWARD.
     * @throws JposException If an error occurs.
     */
    public void resetItemCount(int direction) throws JposException;

    /**
     * Final part of StopBelt method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     *
     * @throws JposException If an error occurs.
     */
    public void stopBelt() throws JposException;
}
