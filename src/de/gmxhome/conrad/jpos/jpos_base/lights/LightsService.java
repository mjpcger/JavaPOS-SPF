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
import jpos.services.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;
import static jpos.LightsConst.*;

/**
 * Lights service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class LightsService extends JposBase implements LightsService116 {
    /**
     * Instance of a class implementing the LightsInterface for lights specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public LightsInterface LightsInterface;

    private final LightsProperties Data;

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
    public int getCapPattern() throws JposException {
        try {
            checkOpened();
            logGet("CapPattern");
            return Data.CapPattern;
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unexpected exception from service", e);
        }
    }

    @Override
    public void switchOff(int lightNumber) throws JposException {
        logPreCall("SwitchOff", removeOuterArraySpecifier(new Object[]{lightNumber}, Device.MaxArrayStringElements));
        checkEnabled();
        check(lightNumber < 1 || lightNumber > Data.MaxLights, JPOS_E_ILLEGAL, "Light number out of range: " + lightNumber);
        LightsInterface.switchOff(lightNumber);
        logCall("SwitchOff");
    }

    private final long[] allowedcolors = {
            LGT_COLOR_PRIMARY, LGT_COLOR_CUSTOM1, LGT_COLOR_CUSTOM2,
            LGT_COLOR_CUSTOM3, LGT_COLOR_CUSTOM4, LGT_COLOR_CUSTOM5
    };
    private final long[] allowedalarms = {
            LGT_ALARM_NOALARM, LGT_ALARM_SLOW, LGT_ALARM_MEDIUM,
            LGT_ALARM_FAST, LGT_ALARM_CUSTOM1, LGT_ALARM_CUSTOM2
    };

    @Override
    public void switchOn(int lightNumber, int blinkOnCycle, int blinkOffCycle, int color, int alarm) throws JposException {
        logPreCall("SwitchOn", removeOuterArraySpecifier(new Object[]{lightNumber, blinkOnCycle, blinkOffCycle, color, alarm}, Device.MaxArrayStringElements));
        checkEnabled();
        check(lightNumber < 1 || lightNumber > Data.MaxLights, JPOS_E_ILLEGAL, "Light number out of range: " + lightNumber);
        check(Data.CapColor != LGT_COLOR_PRIMARY && (!member(color, allowedcolors) ||
                (color & ~Data.CapColor) != 0), JPOS_E_ILLEGAL, "Invalid color: " + color);
        check(Data.CapAlarm != LGT_ALARM_NOALARM && (!member(alarm, allowedalarms) ||
                (alarm & ~Data.CapAlarm) != 0), JPOS_E_ILLEGAL, "Invalid alarm: " + alarm);
        LightsInterface.switchOn(lightNumber, blinkOnCycle, blinkOffCycle, color, alarm);
        logCall("SwitchOn");
    }

    @Override
    public void switchOnMultiple(String lightNumbers, int blinkOnCycle, int blinkOffCycle, int color, int alarm) throws JposException {
        logPreCall("SwitchOnMultiple", removeOuterArraySpecifier(new Object[]{lightNumbers, blinkOnCycle, blinkOffCycle, color, alarm}, Device.MaxArrayStringElements));
        if (lightNumbers == null)
            lightNumbers = "";
        checkEnabled();
        check(Data.CapColor != LGT_COLOR_PRIMARY && (!member(color, allowedcolors) ||
                (color & ~Data.CapColor) != 0), JPOS_E_ILLEGAL, "Invalid color: " + color);
        check(Data.CapAlarm != LGT_ALARM_NOALARM && (!member(alarm, allowedalarms) ||
                (alarm & ~Data.CapAlarm) != 0), JPOS_E_ILLEGAL, "Invalid alarm: " + alarm);
        String[] numbers = lightNumbers.split(",");
        try {
            for (String number : numbers) {
                int no = Integer.parseInt(number);
                check(no <= 0 || no > Data.MaxLights, JPOS_E_ILLEGAL, "Invalid light no: " + no);
            }
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            throw new JposException(JPOS_E_ILLEGAL, "Bad lightNumbers: " + lightNumbers, e);
        }
        LightsInterface.switchOnMultiple(lightNumbers, blinkOnCycle, blinkOffCycle, color, alarm);
        logCall("SwitchOnMultiple");
    }

    @Override
    public void switchOnPattern(int pattern, int alarm) throws JposException {
        long[] valid = {
                LGT_PATTERN_CUSTOM1, LGT_PATTERN_CUSTOM2, LGT_PATTERN_CUSTOM3, LGT_PATTERN_CUSTOM4,
                LGT_PATTERN_CUSTOM5, LGT_PATTERN_CUSTOM6, LGT_PATTERN_CUSTOM7, LGT_PATTERN_CUSTOM8,
                LGT_PATTERN_CUSTOM9, LGT_PATTERN_CUSTOM10, LGT_PATTERN_CUSTOM11, LGT_PATTERN_CUSTOM12,
                LGT_PATTERN_CUSTOM13, LGT_PATTERN_CUSTOM14, LGT_PATTERN_CUSTOM15, LGT_PATTERN_CUSTOM16,
                LGT_PATTERN_CUSTOM17, LGT_PATTERN_CUSTOM18, LGT_PATTERN_CUSTOM19, LGT_PATTERN_CUSTOM20,
                LGT_PATTERN_CUSTOM21, LGT_PATTERN_CUSTOM22, LGT_PATTERN_CUSTOM23, LGT_PATTERN_CUSTOM24,
                LGT_PATTERN_CUSTOM25, LGT_PATTERN_CUSTOM26, LGT_PATTERN_CUSTOM27, LGT_PATTERN_CUSTOM28,
                LGT_PATTERN_CUSTOM29, LGT_PATTERN_CUSTOM30, LGT_PATTERN_CUSTOM31, LGT_PATTERN_CUSTOM32
        };
        logPreCall("SwitchOnPattern", removeOuterArraySpecifier(new Object[]{pattern, alarm}, Device.MaxArrayStringElements));
        checkEnabled();
        checkMember(pattern, valid, JPOS_E_ILLEGAL, "Invalid pattern: " + Integer.toHexString(pattern));
        check((~Data.CapPattern & pattern) != 0, JPOS_E_ILLEGAL, "Unsupported pattern: " + Integer.toHexString(pattern));
        check(Data.CapAlarm != LGT_ALARM_NOALARM && (!member(alarm, allowedalarms) ||
                (alarm & ~Data.CapAlarm) != 0), JPOS_E_ILLEGAL, "Invalid alarm: " + alarm);
        LightsInterface.switchOnPattern(pattern, alarm);
        logCall("SwitchOnPattern");
    }

    @Override
    public void switchOffPattern() throws JposException {
        logPreCall("SwitchOffPattern");
        checkEnabled();
        check(Data.CapPattern == LGT_PATTERN_NOPATTERN, JPOS_E_ILLEGAL, "Pattern not supported");
        LightsInterface.switchOffPattern();
        logCall("SwitchOffPattern");
    }
}
