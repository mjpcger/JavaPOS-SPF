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

import de.gmxhome.conrad.jpos.jpos_base.JposBase;
import de.gmxhome.conrad.jpos.jpos_base.JposDevice;
import jpos.JposConst;
import jpos.JposException;
import jpos.LightsConst;
import jpos.services.LightsService114;

/**
 * Lights service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class LightsService extends JposBase implements LightsService114 {
    /**
     * Instance of a class implementing the LightsInterface for lights specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public LightsInterface LightsInterface;

    private LightsProperties Data;

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public LightsService(LightsProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public int getCapAlarm() throws JposException {
        checkOpened();
        logGet("CapAlarm");
        return Data.CapAlarm;
    }

    @Override
    public boolean getCapBlink() throws JposException {
        checkOpened();
        logGet("CapBlink");
        return Data.CapBlink;
    }

    @Override
    public int getCapColor() throws JposException {
        checkOpened();
        logGet("CapColor");
        return Data.CapColor;
    }

    @Override
    public int getMaxLights() throws JposException {
        checkOpened();
        logGet("MaxLights");
        return Data.MaxLights;
    }

    @Override
    public void switchOff(int lightNumber) throws JposException {
        logPreCall("SwitchOff", "" + lightNumber);
        checkEnabled();
        Device.check(lightNumber < 1 || lightNumber > Data.MaxLights, JposConst.JPOS_E_ILLEGAL, "Light number out of range: " + lightNumber);
        LightsInterface.switchOff(lightNumber);
        logCall("SwitchOff");
    }

    @Override
    public void switchOn(int lightNumber, int blinkOnCycle, int blinkOffCycle, int color, int alarm) throws JposException {
        long[] allowedcolors = {
                LightsConst.LGT_COLOR_PRIMARY,
                LightsConst.LGT_COLOR_CUSTOM1,
                LightsConst.LGT_COLOR_CUSTOM2,
                LightsConst.LGT_COLOR_CUSTOM3,
                LightsConst.LGT_COLOR_CUSTOM4,
                LightsConst.LGT_COLOR_CUSTOM5
        };
        long[] allowedalarms = {
                LightsConst.LGT_ALARM_NOALARM,
                LightsConst.LGT_ALARM_SLOW,
                LightsConst.LGT_ALARM_MEDIUM,
                LightsConst.LGT_ALARM_FAST,
                LightsConst.LGT_ALARM_CUSTOM1,
                LightsConst.LGT_ALARM_CUSTOM2
        };
        logPreCall("SwitchOn", "" + lightNumber + ", " + blinkOnCycle + ", " + blinkOffCycle + ", " + color + ", " + alarm);
        checkEnabled();
        Device.check(lightNumber < 1 || lightNumber > Data.MaxLights, JposConst.JPOS_E_ILLEGAL, "Light number out of range: " + lightNumber);
        Device.check(Data.CapColor != LightsConst.LGT_COLOR_PRIMARY && (!Device.member(color, allowedcolors) ||
                (color & ~Data.CapColor) != 0), JposConst.JPOS_E_ILLEGAL, "Invalid color: " + color);
        Device.check(Data.CapAlarm != LightsConst.LGT_ALARM_NOALARM && (!Device.member(alarm, allowedalarms) ||
                (alarm & ~Data.CapAlarm) != 0), JposConst.JPOS_E_ILLEGAL, "Invalid alarm: " + alarm);
        LightsInterface.switchOn(lightNumber, blinkOnCycle, blinkOffCycle, color, alarm);
        logCall("SwitchOn");
    }
}
