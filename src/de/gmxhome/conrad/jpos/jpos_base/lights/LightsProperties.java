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

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import jpos.JposException;
import jpos.LightsConst;

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
    public int CapAlarm = LightsConst.LGT_ALARM_NOALARM;

    /**
     * UPOS property CapBlink. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapBlink = false;

    /**
     * UPOS property CapColor. Default: COLOR_PRIMARY. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int CapColor = LightsConst.LGT_COLOR_PRIMARY;

    /**
     * UPOS property MaxLights. Default: 1. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int MaxLights = 1;

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
}
