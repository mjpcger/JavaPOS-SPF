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

package de.gmxhome.conrad.jpos.jpos_base.lights;

import de.gmxhome.conrad.jpos.jpos_base.JposBaseInterface;
import jpos.JposException;

/**
 * Interface for methods that implement property setter and method calls for the Lights device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Lights.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface LightsInterface extends JposBaseInterface {
    /**
     * Final part of SwitchOff method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>lightNumber is between 1 and MaxLights.</li>
     * </ul>
     *
     * @param lightNumber   light number between 1 and MaxLights.
     * @throws JposException If an error occurs.
     */
    public void switchOff(int lightNumber) throws JposException;

    /**
     * Final part of SwitchOn method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>lightNumber is between 1 and MaxLights,</li>
     *     <li>CapColor is COLOR_PRIMARY or color is one of the colors specified by CapColor,</li>
     *     <li>CapAlarm is ALARM_NOALARM or alarm is one of the alarms specified by CapAlarm.</li>
     * </ul>
     *
     * @param lightNumber   light number between 1 and MaxLights.
     * @param blinkOnCycle  If blinking, light-on time in milliseconds.
     * @param blinkOffCycle If blinking, light-off time in milliseconds.
     * @param color         Light color as specified by CapColor.
     * @param alarm         Alarm value as specified by CapAlarm.
     * @throws JposException If an error occurs.
     */
    public void switchOn(int lightNumber, int blinkOnCycle, int blinkOffCycle, int color, int alarm) throws JposException;
}
