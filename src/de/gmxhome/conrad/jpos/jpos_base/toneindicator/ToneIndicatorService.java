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
import jpos.services.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;

/**
 * ToneIndicator service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class ToneIndicatorService extends JposBase implements ToneIndicatorService116 {
    /**
     * Instance of a class implementing the ToneIndicatorInterface for tone indicator specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public ToneIndicatorInterface ToneIndicatorInterface;

    private final ToneIndicatorProperties Data;

    /**
     * Constructor. Stores property set and device driver implementation
     *
     * @param props  Device service property set-
     * @param device Device driver implementation.
     */
    public ToneIndicatorService(ToneIndicatorProperties props, JposDevice device) {
        super(props, device);
        Data = props;
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
    public int getCapMelody() throws JposException {
        checkOpened();
        logGet("CapMelody");
        return Data.CapMelody;
    }

    @Override
    public int getMelodyType() throws JposException {
        checkFirstEnabled();
        logGet("MelodyType");
        return Data.MelodyType;
    }

    @Override
    public void setMelodyType(int i) throws JposException {
        logPreSet("MelodyType");
        checkEnabled();
        checkRange(i, 0, Data.CapMelody, JPOS_E_ILLEGAL, "Melody type out of range: " + i);
        ToneIndicatorInterface.melodyType(i);
        logSet("MelodyType");
    }

    @Override
    public int getMelodyVolume() throws JposException {
        checkFirstEnabled();
        logGet("MelodyVolume");
        return Data.MelodyVolume;
    }

    @Override
    public void setMelodyVolume(int i) throws JposException {
        logPreSet("MelodyVolume");
        checkEnabled();
        ToneIndicatorInterface.melodyVolume(i);
        logSet("MelodyVolume");
    }

    @Override
    public boolean getCapPitch() throws JposException {
        checkOpened();
        logGet("CapPitch");
        return Data.CapPitch;
    }

    @Override
    public boolean getCapVolume() throws JposException {
        checkOpened();
        logGet("CapVolume");
        return Data.CapVolume;
    }

    @Override
    public int getInterToneWait() throws JposException {
        checkFirstEnabled();
        logGet("InterToneWait");
        return Data.InterToneWait;
    }

    @Override
    public void setInterToneWait(int i) throws JposException {
        logPreSet("InterToneWait");
        checkEnabled();
        check(i < 0, JPOS_E_ILLEGAL, "Negative InterToneWait");
        ToneIndicatorInterface.interToneWait(i);
        logSet("InterToneWait");
    }

    @Override
    public int getTone1Duration() throws JposException {
        checkFirstEnabled();
        logGet("Tone1Duration");
        return Data.Tone1Duration;
    }

    @Override
    public void setTone1Duration(int i) throws JposException {
        logPreSet("Tone1Duration");
        checkEnabled();
        ToneIndicatorInterface.tone1Duration(i);
        logSet("Tone1Duration");
    }

    @Override
    public int getTone1Pitch() throws JposException {
        checkFirstEnabled();
        logGet("Tone1Pitch");
        return Data.Tone1Pitch;
    }

    @Override
    public void setTone1Pitch(int i) throws JposException {
        logPreSet("Tone1Pitch");
        checkEnabled();
        ToneIndicatorInterface.tone1Pitch(i);
        logSet("Tone1Pitch");
    }

    @Override
    public int getTone1Volume() throws JposException {
        checkFirstEnabled();
        logGet("Tone1Volume");
        return Data.Tone1Volume;
    }

    @Override
    public void setTone1Volume(int i) throws JposException {
        logPreSet("Tone1Volume");
        checkEnabled();
        ToneIndicatorInterface.tone1Volume(i);
        logSet("Tone1Volume");
    }

    @Override
    public int getTone2Duration() throws JposException {
        checkFirstEnabled();
        logGet("Tone2Duration");
        return Data.Tone2Duration;
    }

    @Override
    public void setTone2Duration(int i) throws JposException {
        logPreSet("Tone2Duration");
        checkEnabled();
        ToneIndicatorInterface.tone2Duration(i);
        logSet("Tone2Duration");
    }

    @Override
    public int getTone2Pitch() throws JposException {
        checkFirstEnabled();
        logGet("Tone2Pitch");
        return Data.Tone2Pitch;
    }

    @Override
    public void setTone2Pitch(int i) throws JposException {
        logPreSet("Tone2Pitch");
        checkEnabled();
        ToneIndicatorInterface.tone2Pitch(i);
        logSet("Tone2Pitch");
    }

    @Override
    public int getTone2Volume() throws JposException {
        checkFirstEnabled();
        logGet("Tone2Volume");
        return Data.Tone2Volume;
    }

    @Override
    public void setTone2Volume(int i) throws JposException {
        logPreSet("Tone2Volume");
        checkEnabled();
        ToneIndicatorInterface.tone2Volume(i);
        logSet("Tone2Volume");
    }

    @Override
    public void sound(int numberOfCycles, int interSoundWait) throws JposException {
        logPreCall("Sound", removeOuterArraySpecifier(new Object[]{numberOfCycles, interSoundWait}, Device.MaxArrayStringElements));
        checkEnabledUnclaimed();
        check(numberOfCycles <= 0 && numberOfCycles != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid numberOfCycles");
        check(numberOfCycles == JPOS_FOREVER && !Props.AsyncMode, JPOS_E_ILLEGAL, "Invalid numberOfCycles");
        check(interSoundWait < 0, JPOS_E_ILLEGAL, "Invalid interSoundWait specified");
        if (callNowOrLater(ToneIndicatorInterface.sound(numberOfCycles, interSoundWait)))
            logAsyncCall("Sound");
        else
            logCall("Sound");
    }

    @Override
    public void soundImmediate() throws JposException {
        logPreCall("SoundImmediate");
        checkEnabledUnclaimed();
        ToneIndicatorInterface.clearOutput();
        Sound request = ToneIndicatorInterface.sound(1, 1);
        if (request != null) {
            request.enqueueSynchronous();
            if (request.Exception != null)
                throw request.Exception;
        }
        logCall("SoundImmediate");
    }
}
