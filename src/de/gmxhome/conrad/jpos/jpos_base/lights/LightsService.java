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
    public int getCapPattern() throws JposException {
        try {
            checkOpened();
            logGet("CapPattern");
            return Data.CapPattern;
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_FAILURE, "Unexpected exception from service", e);
        }
    }

    @Override
    public void switchOff(int lightNumber) throws JposException {
        logPreCall("SwitchOff", "" + lightNumber);
        checkEnabled();
        JposDevice.check(lightNumber < 1 || lightNumber > Data.MaxLights, JposConst.JPOS_E_ILLEGAL, "Light number out of range: " + lightNumber);
        LightsInterface.switchOff(lightNumber);
        logCall("SwitchOff");
    }

    private long[] allowedcolors = {
            LightsConst.LGT_COLOR_PRIMARY,
            LightsConst.LGT_COLOR_CUSTOM1,
            LightsConst.LGT_COLOR_CUSTOM2,
            LightsConst.LGT_COLOR_CUSTOM3,
            LightsConst.LGT_COLOR_CUSTOM4,
            LightsConst.LGT_COLOR_CUSTOM5
    };
    private long[] allowedalarms = {
            LightsConst.LGT_ALARM_NOALARM,
            LightsConst.LGT_ALARM_SLOW,
            LightsConst.LGT_ALARM_MEDIUM,
            LightsConst.LGT_ALARM_FAST,
            LightsConst.LGT_ALARM_CUSTOM1,
            LightsConst.LGT_ALARM_CUSTOM2
    };

    @Override
    public void switchOn(int lightNumber, int blinkOnCycle, int blinkOffCycle, int color, int alarm) throws JposException {
        logPreCall("SwitchOn", removeOuterArraySpecifier(new Object[]{lightNumber, blinkOnCycle, blinkOffCycle, color, alarm}, 5));
        checkEnabled();
        JposDevice.check(lightNumber < 1 || lightNumber > Data.MaxLights, JposConst.JPOS_E_ILLEGAL, "Light number out of range: " + lightNumber);
        JposDevice.check(Data.CapColor != LightsConst.LGT_COLOR_PRIMARY && (!Device.member(color, allowedcolors) ||
                (color & ~Data.CapColor) != 0), JposConst.JPOS_E_ILLEGAL, "Invalid color: " + color);
        JposDevice.check(Data.CapAlarm != LightsConst.LGT_ALARM_NOALARM && (!Device.member(alarm, allowedalarms) ||
                (alarm & ~Data.CapAlarm) != 0), JposConst.JPOS_E_ILLEGAL, "Invalid alarm: " + alarm);
        LightsInterface.switchOn(lightNumber, blinkOnCycle, blinkOffCycle, color, alarm);
        logCall("SwitchOn");
    }

    @Override
    public void switchOnMultiple(String lightNumbers, int blinkOnCycle, int blinkOffCycle, int color, int alarm) throws JposException {
        try {
            if (lightNumbers == null)
                lightNumbers = "";
            logPreCall("SwitchOnMultiple", removeOuterArraySpecifier(new Object[]{lightNumbers, blinkOnCycle, blinkOffCycle, color, alarm}, 5));
            checkEnabled();
            JposDevice.check(Data.CapColor != LightsConst.LGT_COLOR_PRIMARY && (!Device.member(color, allowedcolors) ||
                    (color & ~Data.CapColor) != 0), JposConst.JPOS_E_ILLEGAL, "Invalid color: " + color);
            JposDevice.check(Data.CapAlarm != LightsConst.LGT_ALARM_NOALARM && (!Device.member(alarm, allowedalarms) ||
                    (alarm & ~Data.CapAlarm) != 0), JposConst.JPOS_E_ILLEGAL, "Invalid alarm: " + alarm);
            String[] numbers = lightNumbers.split(",");
            try {
                for (String number : numbers) {
                    int no = Integer.parseInt(number);
                    JposDevice.check(no <= 0 || no > Data.MaxLights, JposConst.JPOS_E_ILLEGAL, "Invalid light no: " + no);
                }
            } catch (JposException e) {
                throw e;
            } catch (Exception e) {
                throw new JposException(JposConst.JPOS_E_ILLEGAL, "Bad lightNumbers: " + lightNumbers, e);
            }
            LightsInterface.switchOnMultiple(lightNumbers, blinkOnCycle, blinkOffCycle, color, alarm);
            logCall("SwitchOnMultiple");
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_FAILURE, "Unexpected exception from service", e);
        }
    }

    @Override
    public void switchOnPattern(int pattern, int alarm) throws JposException {
        long[] valid = {
                LightsConst.LGT_PATTERN_CUSTOM1,
                LightsConst.LGT_PATTERN_CUSTOM2,
                LightsConst.LGT_PATTERN_CUSTOM3,
                LightsConst.LGT_PATTERN_CUSTOM4,
                LightsConst.LGT_PATTERN_CUSTOM5,
                LightsConst.LGT_PATTERN_CUSTOM6,
                LightsConst.LGT_PATTERN_CUSTOM7,
                LightsConst.LGT_PATTERN_CUSTOM8,
                LightsConst.LGT_PATTERN_CUSTOM9,
                LightsConst.LGT_PATTERN_CUSTOM10,
                LightsConst.LGT_PATTERN_CUSTOM11,
                LightsConst.LGT_PATTERN_CUSTOM12,
                LightsConst.LGT_PATTERN_CUSTOM13,
                LightsConst.LGT_PATTERN_CUSTOM14,
                LightsConst.LGT_PATTERN_CUSTOM15,
                LightsConst.LGT_PATTERN_CUSTOM16,
                LightsConst.LGT_PATTERN_CUSTOM17,
                LightsConst.LGT_PATTERN_CUSTOM18,
                LightsConst.LGT_PATTERN_CUSTOM19,
                LightsConst.LGT_PATTERN_CUSTOM20,
                LightsConst.LGT_PATTERN_CUSTOM21,
                LightsConst.LGT_PATTERN_CUSTOM22,
                LightsConst.LGT_PATTERN_CUSTOM23,
                LightsConst.LGT_PATTERN_CUSTOM24,
                LightsConst.LGT_PATTERN_CUSTOM25,
                LightsConst.LGT_PATTERN_CUSTOM26,
                LightsConst.LGT_PATTERN_CUSTOM27,
                LightsConst.LGT_PATTERN_CUSTOM28,
                LightsConst.LGT_PATTERN_CUSTOM29,
                LightsConst.LGT_PATTERN_CUSTOM30,
                LightsConst.LGT_PATTERN_CUSTOM31,
                LightsConst.LGT_PATTERN_CUSTOM32
        };
        try {
        logPreCall("SwitchOnPattern", removeOuterArraySpecifier(new Object[]{pattern, alarm}, 2));
        checkEnabled();
        JposDevice.checkMember(pattern, valid, JposConst.JPOS_E_ILLEGAL, "Invalid pattern: " + Integer.toHexString(pattern));
        JposDevice.check((~Data.CapPattern & pattern) != 0, JposConst.JPOS_E_ILLEGAL, "Unsupported pattern: " + Integer.toHexString(pattern));
        JposDevice.check(Data.CapAlarm != LightsConst.LGT_ALARM_NOALARM && (!Device.member(alarm, allowedalarms) ||
                (alarm & ~Data.CapAlarm) != 0), JposConst.JPOS_E_ILLEGAL, "Invalid alarm: " + alarm);
        LightsInterface.switchOnPattern(pattern, alarm);
        logCall("SwitchOnPattern");
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_FAILURE, "Unexpected exception from service", e);
        }
    }

    @Override
    public void switchOffPattern() throws JposException {
        try{
        logPreCall("SwitchOffPattern");
        checkEnabled();
        JposDevice.check(Data.CapPattern == LightsConst.LGT_PATTERN_NOPATTERN, JposConst.JPOS_E_ILLEGAL, "Pattern not supported");
        LightsInterface.switchOffPattern();
        logCall("SwitchOffPattern");
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_FAILURE, "Unexpected exception from service", e);
        }
    }
}
