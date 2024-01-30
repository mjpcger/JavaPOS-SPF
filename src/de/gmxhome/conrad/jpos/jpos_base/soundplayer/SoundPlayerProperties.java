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

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import jpos.JposException;
import jpos.SoundPlayerConst;

/**
 * Class containing the sound player specific properties, their default values and default implementations of
 * SoundPlayerInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Sound Player.
 */
public class SoundPlayerProperties extends JposCommonProperties implements SoundPlayerInterface {
    /**
     * UPOS property CapAssociatedHardTotalsDevice. Default: an empty string. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public String CapAssociatedHardTotalsDevice = "";

    /**
     * UPOS property CapMultiPlay. Will be set in initOnOpen to true if property CurrentCommands of JposBaseDevice is not
     * null, otherwise to false.
     */
    public Boolean CapMultiPlay = false;

    /**
     * UPOS property CapSoundTypeList. Default: an empty string. Must be overwritten by objects derived from JposDevice within the
     * changeDefaults method to match at least one supported sound type.
     */
    public String CapSoundTypeList = "";

    /**
     * UPOS property CapStorage. Default: CST_HOST_ONLY. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int CapStorage = SoundPlayerConst.SPLY_CST_HOST_ONLY;

    /**
     * UPOS property CapVolume. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapVolume = false;

    /**
     * UPOS property DeviceSoundList. Default: an empty string. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public String DeviceSoundList = "";

    /**
     * UPOS property OutputIDList. Default: an empty string. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public String OutputIDList = "";

    /**
     * Every assess to OutputIDList must be synchronized via OutputIdListSync.
     */
    public String[] OutputIdListSync = { OutputIDList };

    /**
     * UPOS property Storage. Default: an empty string. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int Storage = SoundPlayerConst.SPLY_ST_HOST;

    /**
     * UPOS property Volume. Default: 0. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int Volume = 0;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected SoundPlayerProperties(int dev) {
        super(dev);
        DeviceServiceVersion = 1016000;
    }

    @Override
    public void initOnOpen() {
        CapMultiPlay = Device.CurrentCommands != null;
        AsyncMode = true;
    }

    @Override
    public void storage(int storage) throws JposException {
        Storage = storage;
    }

    @Override
    public void volume(int volume) throws JposException {
        Volume = volume;
    }

    @Override
    public PlaySound playSound(String fileName, boolean loop) throws JposException {
        return new PlaySound(this, fileName, loop);
    }

    @Override
    public void playSound(PlaySound request) throws JposException {
    }

    @Override
    public void stopSound(int outputID) throws JposException {
    }
}
