/*
 * Copyright 2023 Martin Conrad
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
 *
 */

package de.gmxhome.conrad.jpos.jpos_base.soundplayer;

import de.gmxhome.conrad.jpos.jpos_base.JposOutputRequest;
import jpos.JposException;

/**
 * Output request executor for SoundPlayer method PlaySound.
 */
public class PlaySound extends JposOutputRequest {
    /**
     * Audio file name or sound ID of sound to be played.
     * @return Audio file name or sound ID.
     */
    public String getFileName() { return FileName; }
    private final String FileName;

    /**
     * Specifies whether loop playback shall be performed.
     * @return true for loop playback.
     */
    public boolean getLoop() { return Loop; }
    private final boolean Loop;
    /**
     * Constructor. Stores given parameters for later use.
     * @param props       Property set of device service.
     * @param fileName    The file name to be played.
     * @param loop .      Specifies whether sound will be replayed whenever ready.
     */
    public PlaySound(SoundPlayerProperties props, String fileName, boolean loop) {
        super(props);
        FileName = fileName;
        Loop = loop;
    }

    @Override
    public void invoke() throws JposException {
        ((SoundPlayerService) Props.EventSource).SoundPlayer.playSound(this);
    }
}
