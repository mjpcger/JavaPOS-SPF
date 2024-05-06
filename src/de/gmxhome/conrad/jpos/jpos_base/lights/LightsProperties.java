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

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

import static jpos.LightsConst.*;

/**
 * Class containing the lights specific properties, their default values and default implementations of
 * LightsInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Lights.
 */
public class LightsProperties extends JposCommonProperties implements LightsInterface {
    /**
     * UPOS property CapAlarm. Default: ALARM_NOALARM. Can be overwritten by objects derived from JposDevice within
     * the changeDefaults method.
     */
    public int CapAlarm = LGT_ALARM_NOALARM;

    /**
     * UPOS property CapBlink. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapBlink = false;

    /**
     * UPOS property CapColor. Default: COLOR_PRIMARY. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int CapColor = LGT_COLOR_PRIMARY;

    /**
     * UPOS property MaxLights. Default: 1. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int MaxLights = 1;

    /**
     * UPOS property CapPattern. Default: LGT_PATTERN_NOPATTERN. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int CapPattern = LGT_PATTERN_NOPATTERN;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected LightsProperties(int dev) {
        super(dev);
        ExclusiveUse = ExclusiveYes;
        FlagWhenIdleStatusValue = -1;   // To avoid FlagWhenIdle handling for CASH_SUE_DRAWERCLOSED
    }

    @Override
    public void switchOff(int lightNumber) throws JposException {
    }

    @Override
    public void switchOn(int lightNumber, int blinkOnCycle, int blinkOffCycle, int color, int alarm) throws JposException {
    }

    /**
     * Final part of SwitchOn method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>lightNumbers consists of a comma separated list of numbers between 1 and MaxLights,</li>
     *     <li>CapColor is COLOR_PRIMARY or color is one of the colors specified by CapColor,</li>
     *     <li>CapAlarm is ALARM_NOALARM or alarm is one of the alarms specified by CapAlarm.</li>
     * </ul>
     *
     * @param lightNumbers  light numbers between 1 and MaxLights, separated by comma.
     * @param blinkOnCycle  If blinking, light-on time in milliseconds.
     * @param blinkOffCycle If blinking, light-off time in milliseconds.
     * @param color         Light color as specified by CapColor.
     * @param alarm         Alarm value as specified by CapAlarm.
     * @throws JposException If an error occurs.
     */
    @Override
    public void switchOnMultiple(String lightNumbers, int blinkOnCycle, int blinkOffCycle, int color, int alarm) throws JposException {
    }

    /**
     * Final part of SwitchOn method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Only pattern set in CapPattern are specified in pattern,</li>
     *     <li>CapAlarm is ALARM_NOALARM or alarm is one of the alarms specified by CapAlarm.</li>
     * </ul>
     *
     * @param pattern Lightning pattern to be switched on.
     * @param alarm   Alarm value as specified by CapAlarm.
     * @throws JposException If an error occurs.
     */
    @Override
    public void switchOnPattern(int pattern, int alarm) throws JposException {
    }

    /**
     * Final part of SwitchOn method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapPattern is not equal to LGT_PATTERN_NOPATTERN.</li>
     * </ul>
     *
     * @throws JposException If an error occurs.
     */
    @Override
    public void switchOffPattern() throws JposException {
    }
}
