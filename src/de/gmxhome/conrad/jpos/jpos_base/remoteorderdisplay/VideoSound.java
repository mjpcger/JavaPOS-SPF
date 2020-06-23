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

package de.gmxhome.conrad.jpos.jpos_base.remoteorderdisplay;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Output request executor for RemoteOrderDisplay method VideoSound.
 */
public class VideoSound extends UnitOutputRequest {
    /**
     * Retrieves parameter function of method VideoSound. See UPOS specification for further information.
     * @return  Value of method parameter frequency.
     */
    public int getFrequency() {
        return Frequency;
    }

    private int Frequency;

    /**
     * Retrieves parameter duration of method VideoSound. See UPOS specification for further information.
     * @return  Value of method parameter duration.
     */
    public int getDuration() {
        return Duration;
    }

    private int Duration;

    /**
     * Retrieves parameter numberOfCycles of method VideoSound. See UPOS specification for further information.
     * @return  Value of method parameter numberOfCycles.
     */
    public int getNumberOfCycles() {
        return NumberOfCycles;
    }

    private int NumberOfCycles;

    /**
     * Retrieves parameter function of method VideoSound. See UPOS specification for further information.
     * @return  Value of method parameter interSoundWait.
     */
    public int getInterSoundWait() {
        return InterSoundWait;
    }

    private int InterSoundWait;

    /**
     * Constructor. Stores given parameters for later use.
     * @param props             Property set of device service.
     * @param units             Bitwise mask indicating which video unit(s) to operate on.
     * @param frequency         Tone frequency in Hertz.
     * @param duration          Tone duration in milliseconds.
     * @param numberOfCycles    Number of cycles to generate tone.
     * @param interSoundWait    Delay between sounds, in milliseconds.
     */
    public VideoSound(JposCommonProperties props, int units, int frequency, int duration, int numberOfCycles, int interSoundWait) {
        super(props, units);
    }

    @Override
    public void invoke() throws JposException {
        RemoteOrderDisplayService svc = (RemoteOrderDisplayService) Props.EventSource;
        if (EndSync == null) {
            checkUnitsOnline();
            int errunits = svc.validateTone(getUnits());
            svc.check(errunits != 0, errunits, JposConst.JPOS_E_FAILURE, 0, "Selected units do not support video sound: " + errunits, EndSync != null);
        }
        svc.RemoteOrderDisplayInterface.videoSound(this);
    }
}
