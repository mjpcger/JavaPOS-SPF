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

package de.gmxhome.conrad.jpos.jpos_base.toneindicator;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

import static jpos.ToneIndicatorConst.*;

/**
 * Class containing the tone indicator specific properties, their default values and default implementations of
 * ToneIndicatorInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Tone Indicator.
 */
public class ToneIndicatorProperties extends JposCommonProperties implements ToneIndicatorInterface {
    /**
     * UPOS property CapMelody. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapMelody = 0;

    /**
     * UPOS property CapPitch. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapPitch = false;

    /**
     * UPOS property CapVolume. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapVolume = false;

    /**
     * UPOS property InterToneWait.
     */
    public int InterToneWait;

    /**
     * UPOS property MelodyType.
     */
    public int MelodyType;

    /**
     * UPOS property MelodyVolume.
     */
    public int MelodyVolume;

    /**
     * UPOS property Tone1Duration.
     */
    public int Tone1Duration;

    /**
     * UPOS property Tone1Pitch.
     */
    public int Tone1Pitch;

    /**
     * UPOS property Tone1Volume.
     */
    public int Tone1Volume;

    /**
     * UPOS property Tone2Duration.
     */
    public int Tone2Duration;

    /**
     * UPOS property Tone2Pitch.
     */
    public int Tone2Pitch;

    /**
     * UPOS property Tone2Volume.
     */
    public int Tone2Volume;

    /**
     * Constructor. Sets ExclusiveUse to ExclusiveAllowed to match the ToneIndicator device model.
     *
     * @param dev Device index
     */
    public ToneIndicatorProperties(int dev) {
        super(dev);
        ExclusiveUse = ExclusiveAllowed;
    }

    @Override
    public void initOnOpen() {
        super.initOnOpen();
    }

    @Override
    public boolean initOnFirstEnable() {
        if (!super.initOnFirstEnable()) {
            InterToneWait = 0;
            MelodyType = TONE_MT_NONE;
            MelodyVolume = 100;
            Tone1Duration = 0;
            Tone1Pitch = 0;
            Tone1Volume = 100;
            Tone2Duration = 0;
            Tone2Pitch = 0;
            Tone2Volume = 100;
            return false;
        }
        return true;
    }

    @Override
    public void melodyType(int type) throws JposException {
        MelodyType = type;
    }

    @Override
    public void melodyVolume(int volume) throws JposException {
        MelodyVolume = volume;
    }

    @Override
    public void interToneWait(int delay) throws JposException {
        InterToneWait = delay;
    }

    @Override
    public void tone1Duration(int duration) throws JposException {
        Tone1Duration = duration;
    }

    @Override
    public void tone1Pitch(int type) throws JposException {
        Tone1Pitch = type;
    }

    @Override
    public void tone1Volume(int type) throws JposException {
        Tone1Volume = type;
    }

    @Override
    public void tone2Duration(int type) throws JposException {
        Tone2Duration = type;
    }

    @Override
    public void tone2Pitch(int type) throws JposException {
        Tone2Pitch = type;
    }

    @Override
    public void tone2Volume(int type) throws JposException {
        Tone2Volume = type;
    }

    @Override
    public Sound sound(int numberOfCycles, int interSoundWait) throws JposException {
        return new Sound(this, numberOfCycles, interSoundWait);
    }

    @Override
    public void sound(Sound request) throws JposException {
    }
}
