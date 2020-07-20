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

package de.gmxhome.conrad.jpos.jpos_base.bumpbar;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.BumpBarConst;
import jpos.JposConst;
import jpos.JposException;
import jpos.services.BumpBarService114;
import org.apache.log4j.Level;

/**
 * BumpBar service implementation. For more details about getter, setter and method implementations,
 * see JposBase.<br>
 * Special handling has been added to method DirectIO: Due to the fact that UPOS does not support a FlagWhenIdle
 * property for remote order displays, two commands have been added to the UPOS standard. See the description of
 * method DirectIO for details.
 */
public class BumpBarService extends JposBase implements BumpBarService114 {
    private BumpBarProperties Data;

    private static final long[] validUnitIDs = new long[] {
            BumpBarConst.BB_UID_1, BumpBarConst.BB_UID_2, BumpBarConst.BB_UID_3, BumpBarConst.BB_UID_4,
            BumpBarConst.BB_UID_5, BumpBarConst.BB_UID_6, BumpBarConst.BB_UID_7, BumpBarConst.BB_UID_8,
            BumpBarConst.BB_UID_9, BumpBarConst.BB_UID_10, BumpBarConst.BB_UID_11, BumpBarConst.BB_UID_12,
            BumpBarConst.BB_UID_13, BumpBarConst.BB_UID_14, BumpBarConst.BB_UID_15, BumpBarConst.BB_UID_16,
            BumpBarConst.BB_UID_17, BumpBarConst.BB_UID_18, BumpBarConst.BB_UID_19, BumpBarConst.BB_UID_20,
            BumpBarConst.BB_UID_21, BumpBarConst.BB_UID_22, BumpBarConst.BB_UID_23, BumpBarConst.BB_UID_24,
            BumpBarConst.BB_UID_25, BumpBarConst.BB_UID_26, BumpBarConst.BB_UID_27, BumpBarConst.BB_UID_28,
            BumpBarConst.BB_UID_29, BumpBarConst.BB_UID_30, BumpBarConst.BB_UID_31, BumpBarConst.BB_UID_32
    };

    /**
     * Instance of a class implementing the BumpBarInterface for bump bar specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public BumpBarInterface BumpBarInterface;

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public BumpBarService(BumpBarProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    /**
     * Checks if CapTone is true for all units specified by bits.
     * @param bits  Bit mask specifying all units to be checked.
     * @return Bit mask, 1 for every requested unit where CapTone is false.
     */
    public int validateTone(int bits) {
        int result = 0;
        while (bits != 0) {
            int index = Data.unitsToFirstIndex(bits);
            bits &= ~(1 << index);
            if (!Data.Unit[index].CapTone)
                result |= 1 << index;
        }
        return result;
    }

    @Override
    public boolean getAsyncMode() throws JposException {
        checkEnabled();
        logGet("AsyncMode");
        return Props.AsyncMode;
    }

    @Override
    public void setAsyncMode(boolean b) throws JposException {
        logPreSet("AsyncMode");
        checkEnabled();
        DeviceInterface.asyncMode(b);
        logSet("AsyncMode");
    }

    @Override
    public boolean getCapTone() throws JposException {
        checkFirstEnabled();
        logGet("CapTone");
        return Data.CapTone;
    }

    @Override
    public int getAutoToneDuration() throws JposException {
        checkFirstEnabled();
        logGet("AutoToneDuration");
        return Data.AutoToneDuration;
    }

    @Override
    public void setAutoToneDuration(int i) throws JposException {
        logPreSet("AutoToneDuration");
        checkOnline(Data.CurrentUnitID);
        check(i < 0, Data.CurrentUnitID, JposConst.JPOS_E_ILLEGAL, 0, "AutoToneDuration " + i + " invalid for unit " + Data.unitsToFirstIndex(Data.CurrentUnitID));
        BumpBarInterface.autoToneDuration(i);
        logSet("AutoToneDuration");
    }

    @Override
    public int getAutoToneFrequency() throws JposException {
        checkFirstEnabled();
        logGet("AutoToneFrequency");
        return Data.AutoToneFrequency;
    }

    @Override
    public void setAutoToneFrequency(int i) throws JposException {
        logPreSet("AutoToneFrequency");
        checkOnline(Data.CurrentUnitID);
        check(i < 0, Data.CurrentUnitID, JposConst.JPOS_E_ILLEGAL, 0, "AutoToneFrequency " + i + " invalid for unit " + Data.unitsToFirstIndex(Data.CurrentUnitID));
        BumpBarInterface.autoToneFrequency(i);
        logSet("AutoToneFrequency");
    }

    @Override
    public int getBumpBarDataCount() throws JposException {
        checkOpened();
        int count = BumpBarInterface.unitDataCount();
        Device.log(Level.DEBUG, Props.LogicalName + ": VideoDataCount: " + count);
        return count;
    }

    @Override
    public int getCurrentUnitID() throws JposException {
        checkFirstEnabled();
        logGet("CurrentUnitID");
        return Data.CurrentUnitID;
    }

    @Override
    public void setCurrentUnitID(int i) throws JposException {
        logPreSet("CurrentUnitID");
        checkEnabled();
        check(!Device.member(i, validUnitIDs), 0, JposConst.JPOS_E_ILLEGAL, 0, "" + i + " is not a valid unit ID");
        BumpBarInterface.currentUnitID(i);
        logSet("CurrentUnitID");
    }

    @Override
    public String getErrorString() throws JposException {
        checkOpened();
        logGet("ErrorString");
        return Data.ErrorString;
    }

    @Override
    public int getErrorUnits() throws JposException {
        checkOpened();
        logGet("ErrorUnits");
        return Data.ErrorUnits;
    }

    @Override
    public String getEventString() throws JposException {
        checkOpened();
        logGet("EventString");
        return Data.EventString;
    }

    @Override
    public int getEventUnitID() throws JposException {
        checkOpened();
        logGet("EventUnitID");
        return Data.EventUnitID;
    }

    @Override
    public int getEventUnits() throws JposException {
        checkOpened();
        logGet("EventUnits");
        return Data.EventUnits;
    }

    @Override
    public int getKeys() throws JposException {
        checkOpened();
        logGet("Keys");
        return Data.Keys;
    }

    @Override
    public int getTimeout() throws JposException {
        checkOpened();
        logGet("Timeout");
        return Data.Timeout;
    }

    @Override
    public void setTimeout(int i) throws JposException {
        logPreSet("Timeout");
        checkOpened();
        check(i < 0, -1, JposConst.JPOS_E_ILLEGAL, 0, "Timeout " + i + " invalid");
        BumpBarInterface.timeout(i);
        logSet("Timeout");
    }

    @Override
    public int getUnitsOnline() throws JposException {
        checkFirstEnabled();
        logGet("UnitsOnline");
        return Data.UnitsOnline;
    }

    @Override
    public void bumpBarSound(int units, int frequency, int duration, int numberOfCycles, int interSoundWait) throws JposException {
        logPreCall("BumpBarSound", "" + units + ", " + frequency + ", " + duration + ", " + numberOfCycles + ", " + interSoundWait);
        check(units == 0, units, JposConst.JPOS_E_ILLEGAL, 0, "No unit specified");
        checkOnline(units);
        check(frequency < 0, units, JposConst.JPOS_E_ILLEGAL, 0, "Frequency invalid: " + frequency);
        check(duration < 0, units, JposConst.JPOS_E_ILLEGAL, 0, "Duration invalid: " + duration);
        check(!Data.AsyncMode && numberOfCycles == JposConst.JPOS_FOREVER, units, JposConst.JPOS_E_ILLEGAL, 0, "Invalid cycle count: " + numberOfCycles);
        check(numberOfCycles != JposConst.JPOS_FOREVER && numberOfCycles <= 0, units, JposConst.JPOS_E_ILLEGAL, 0, "Invalid cycle count: " + numberOfCycles);
        check(interSoundWait < 0, units, JposConst.JPOS_E_ILLEGAL, 0, "InterSoundWait invalid: " + interSoundWait);
        int errunits = validateTone(units);
        check(errunits != 0, errunits, JposConst.JPOS_E_FAILURE, 0, "Selected units do not support video sound: " + errunits);
        if (!callNowOrLater(BumpBarInterface.bumpBarSound(units, frequency, duration, numberOfCycles, interSoundWait))) {
            logCall("BumpBarSound");
            return;
        }
        logAsyncCall("BumpBarSound");
    }

    @Override
    public void setKeyTranslation(int units, int scanCode, int logicalKey) throws JposException {
        logPreCall("SetKeyTranslation", "" + units + ", " + scanCode + ", " + logicalKey);
        checkOnline(units);
        check(units == 0, units, JposConst.JPOS_E_ILLEGAL, 0, "No unit specified");
        check(scanCode < 0 || scanCode > 255, units, JposConst.JPOS_E_ILLEGAL, 0, "ScanCode out of range: " + scanCode);
        check(logicalKey < 0 || logicalKey > 255, units, JposConst.JPOS_E_ILLEGAL, 0, "LogicalKey out of range: " + logicalKey);
        BumpBarInterface.setKeyTranslation(units, scanCode, logicalKey);
        logCall("SetKeyTranslation");
}

    private void checkOnline(int units) throws JposException {
        checkEnabled();
        check((units & ~Data.UnitsOnline) != 0, units & ~Data.UnitsOnline, JposConst.JPOS_E_OFFLINE, 0, "Units specified by " + (units & ~Data.UnitsOnline) + " Offline");
    }
}
