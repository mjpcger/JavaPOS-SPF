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

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.JposException;
import jpos.services.SoundPlayerService116;

/**
 * SoundPlayer service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class SoundPlayerService extends JposBase implements SoundPlayerService116 {
    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public SoundPlayerService(JposCommonProperties props, JposDevice device) {
        super(props, device);
    }

    /**
     * Instance of a class implementing the SoundPlayerInterface for tone indicator specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public SoundPlayerInterface SoundPlayer;

    @Override
    public String getCapAssociatedHardTotalsDevice() throws JposException {
        return null;
    }

    @Override
    public boolean getCapMultiPlay() throws JposException {
        return false;
    }

    @Override
    public String getCapSoundTypeList() throws JposException {
        return null;
    }

    @Override
    public int getCapStorage() throws JposException {
        return 0;
    }

    @Override
    public boolean getCapVolume() throws JposException {
        return false;
    }

    @Override
    public String getDeviceSoundList() throws JposException {
        return null;
    }

    @Override
    public String getOutputIDList() throws JposException {
        return null;
    }

    @Override
    public int getStorage() throws JposException {
        return 0;
    }

    @Override
    public void setStorage(int i) throws JposException {

    }

    @Override
    public int getVolume() throws JposException {
        return 0;
    }

    @Override
    public void setVolume(int i) throws JposException {

    }

    @Override
    public void playSound(String s, boolean b) throws JposException {

    }

    @Override
    public void stopSound(int i) throws JposException {

    }
}
