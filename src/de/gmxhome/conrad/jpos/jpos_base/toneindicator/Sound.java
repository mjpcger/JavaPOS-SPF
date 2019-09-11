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

/**
 * Output request executor for ToneIndicator method Sound.
 */
public class Sound extends JposOutputRequest {
    /**
     * ToneIndicator method sound parameter numberOfCycles, see UPOS specification.
     */
    public int Count;

    /**
     * ToneIndicator method sound parameter interSoundWait, see UPOS specification.
     */
    public int Delay;

    /**
     * Constructor. Stores given parameters for later use. In addition to the base class, the UPOS parameters of
     * ToneIndicatorInterface method Sound must be passed.
     *
     * @param props Property set of device service.
     * @param count Number of times the tone shall be played, FOREVER means forever.
     * @param delay Delay between tones.
     */
    public Sound(ToneIndicatorProperties props, int count, int delay) {
        super(props);
        Count = count;
        Delay = delay;
    }

    @Override
    public void invoke() throws JposException {
        ((ToneIndicatorService)Props.EventSource).ToneIndicatorInterface.sound(this);
    }
}
