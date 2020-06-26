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
import jpos.JposException;

/**
 * Class containing the bump bar specific properties, their default values and default implementations of
 * BumpBarInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Bump Bar.
 */
public class BumpBarProperties extends JposCommonProperties implements BumpBarInterface {
    /**
     * helper class, contains property values per bump bar unit. A fixed size array with 32 elements will be allocated
     * due to the limit of maximum 32 units. This is the bit count of an int value, and each bit of an int may
     * represent one bump bar unit in some methods and events.
     * Default values are placeholders for offline bump bars. Whenever a unit goes online, the service must fill the
     * unit specific values into the corresponding UnitProperties instance.
     */
    public class UnitProperties {
        /**
         * UPOS property AutoToneDuration.
         */
        public int AutoToneDuration = 0;

        /**
         * UPOS property AutoToneFrequency.
         */
        public int AutoToneFrequency = 0;

        /**
         * UPOS property CapTone.
         */
        public boolean CapTone = false;

        /**
         * UPOS property Keys.
         */
        public int Keys = 0;
    }

    /**
     * UPOS property AutoToneDuration.
     */
    public int AutoToneDuration = 0;

    /**
     * UPOS property AutoToneFrequency.
     */
    public int AutoToneFrequency = 0;

    /**
     * UPOS property CapTone.
     */
    public boolean CapTone = false;

    /**
     * UPOS property Keys.
     */
    public int Keys = 0;

    /**
     * Default value of Timeout property. Default: 1 second. Can be
     * overwritten by objects derived from JposDevice within the changeDefaults method.
     */
    public int TimeoutDef = 1000;

    /**
     * Unit specific properties, one per possible unit.
     */
    public UnitProperties[] Unit = new UnitProperties[32];

    /**
     * Copies all properties to the property set belonging to this object.
     */
    public void copyOut() {
        synchronized(Unit) {
            int index = unitsToFirstIndex(CurrentUnitID);
            Unit[index].Keys = Keys;
            Unit[index].AutoToneDuration = AutoToneDuration;
            Unit[index].AutoToneFrequency = AutoToneFrequency;
            Unit[index].CapTone = CapTone;
        }
    }

    /**
     * Retrieves all properties from the property set this object belongs to.
     */
    public void copyIn() {
        synchronized(Unit) {
            int index = unitsToFirstIndex(CurrentUnitID);
            if (AutoToneDuration != Unit[index].AutoToneDuration) {
                AutoToneDuration = Unit[index].AutoToneDuration;
                EventSource.logSet("AutoToneDuration");
            }
            if (AutoToneFrequency != Unit[index].AutoToneFrequency) {
                AutoToneFrequency = Unit[index].AutoToneFrequency;
                EventSource.logSet("AutoToneFrequency");
            }
            if (Keys != Unit[index].Keys) {
                Keys = Unit[index].Keys;
                EventSource.logSet("Keys");
            }
            if (CapTone != Unit[index].CapTone) {
                CapTone = Unit[index].CapTone;
                EventSource.logSet("CapTone");
            }
        }
    }

    /**
     * Returns the lowest index of a unit specified by the given bitmask.
     * @param units A bitmask specifying one or more bump bar units.
     * @return The lowest index where (units &amp; (1 &lt;&lt; index)) != 0.
     */
    public int unitsToFirstIndex(int units) {
        int i = -1;
        while (units != 0) {
            if ((units & (1 << ++i)) != 0)
                break;
        }
        return i;
    }

    /**
     * UPOS property Timeout.
     */
    public int Timeout;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected BumpBarProperties(int dev) {
        super(dev);
    }

    @Override
    public void initOnOpen() {
        super.initOnOpen();
        Timeout = TimeoutDef;
        for (int i = Unit.length; --i >= 0;)
            Unit[i] = new UnitProperties();
    }

    @Override
    public boolean initOnFirstEnable() {
        if (!super.initOnFirstEnable()) {
            CurrentUnitID = BumpBarConst.BB_UID_1;
            copyIn();
            UnitsOnline = 0;
            return false;
        }
        return true;
    }

    /*
     * Interface part of property set
     */

    @Override
    public JposOutputRequest newJposOutputRequest() {
        return new UnitOutputRequest(this, CurrentUnitID);
    }

    @Override
    public void autoToneDuration(int duration) throws JposException {
        AutoToneDuration = duration;
    }

    @Override
    public void autoToneFrequency(int frequency) throws JposException {
        AutoToneFrequency = frequency;
    }

    @Override
    public void currentUnitID(int unit) throws JposException {
        copyOut();
        CurrentUnitID = unit;
        copyIn();
    }

    @Override
    public void timeout(int milliseconds) throws JposException {
        Timeout = milliseconds;
    }

    @Override
    public void setKeyTranslation(int units, int scanCode, int logicalKey) throws JposException {
    }

    @Override
    public BumpBarSound bumpBarSound(int units, int frequency, int duration, int numberOfCycles, int interSoundWait) throws JposException {
        return new BumpBarSound(this, units, frequency, duration, numberOfCycles, interSoundWait);
    }

    @Override
    public void bumpBarSound(BumpBarSound request) throws JposException {
    }
}
