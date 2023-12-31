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
import jpos.JposConst;
import jpos.JposException;
import jpos.SoundPlayerConst;
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
    public SoundPlayerService(SoundPlayerProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    private final SoundPlayerProperties Data;

    /**
     * Instance of a class implementing the SoundPlayerInterface for sound player specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public SoundPlayerInterface SoundPlayer;

    @Override
    public String getCapAssociatedHardTotalsDevice() throws JposException {
        logGet("CapAssociatedHardTotalsDevice");
        checkOpened();
        return Data.CapAssociatedHardTotalsDevice;
    }

    @Override
    public boolean getCapMultiPlay() throws JposException {
        logGet("CapMultiPlay");
        checkOpened();
        return Data.CapMultiPlay;
    }

    @Override
    public String getCapSoundTypeList() throws JposException {
        logGet("CapSoundTypeList");
        checkOpened();
        return Data.CapSoundTypeList;
    }

    @Override
    public int getCapStorage() throws JposException {
        logGet("CapStorage");
        checkOpened();
        return Data.CapStorage;
    }

    @Override
    public boolean getCapVolume() throws JposException {
        logGet("CapVolume");
        checkOpened();
        return Data.CapVolume;
    }

    @Override
    public String getDeviceSoundList() throws JposException {
        logGet("DeviceSoundList");
        checkOpened();
        return Data.DeviceSoundList;
    }

    @Override
    public String getOutputIDList() throws JposException {
        logGet("OutputIDList(");
        checkEnabled();
        return Data.OutputIDList;
    }

    @Override
    public int getStorage() throws JposException {
        logGet("Storage");
        checkEnabled();
        return Data.Storage;
    }

    @Override
    public int getVolume() throws JposException {
        logGet("Volume");
        checkEnabled();
        return Data.Volume;
    }

    @Override
    public void setStorage(int storage) throws JposException {
        long valid[] = { SoundPlayerConst.SPLY_ST_HARDTOTALS, SoundPlayerConst.SPLY_ST_HOST };
        logPreSet("Storage");
        checkEnabled();
        JposDevice.checkMember(storage, valid, JposConst.JPOS_E_ILLEGAL, "Invalid storage selected: " + storage);
        JposDevice.check(Data.CapStorage == SoundPlayerConst.SPLY_CST_HOST_ONLY &&
                storage != SoundPlayerConst.SPLY_ST_HOST, JposConst.JPOS_E_ILLEGAL, "Unsupported storage selected: " + storage);
        JposDevice.check(Data.CapStorage == SoundPlayerConst.SPLY_CST_HARDTOTALS_ONLY &&
                storage != SoundPlayerConst.SPLY_ST_HARDTOTALS, JposConst.JPOS_E_ILLEGAL, "Unsupported storage selected: " + storage);
        JposDevice.check(Data.CapAssociatedHardTotalsDevice.length() == 0 && storage == SoundPlayerConst.SPLY_ST_HARDTOTALS,
                JposConst.JPOS_E_ILLEGAL, "No HardTotals device configured");
        SoundPlayer.storage(storage);
        logSet("Storage");
    }

    @Override
    public void setVolume(int volume) throws JposException {
        logPreSet("Volume");
        checkEnabled();
        JposDevice.check(!Data.CapVolume && volume != Data.Volume, JposConst.JPOS_E_ILLEGAL, "Unsupported volume selected: " + volume);
        JposDevice.check( volume < 0 || volume > 100, JposConst.JPOS_E_ILLEGAL, "Volume out of range 0 - 100: " + volume);
        SoundPlayer.volume(volume);
        logSet("Volume");
    }

    @Override
    public void playSound(String fileName, boolean loop) throws JposException {
        logPreCall("PlaySound", fileName + ", " + loop);
        String[] supported = Data.CapSoundTypeList.split(",");
        checkEnabled();
        if (!JposDevice.member(fileName, Data.DeviceSoundList.split(",")))
            JposDevice.check(!JposDevice.member(fileName.substring(fileName.lastIndexOf('.') + 1), supported),
                    JposConst.JPOS_E_ILLEGAL, "File type not one of {" + Data.CapSoundTypeList + "}");
        JposOutputRequest request = SoundPlayer.playSound(fileName, loop);
        if (request != null)
            request.enqueue();
        logAsyncCall("PlaySound");
    }

    @Override
    public void stopSound(int outputID) throws JposException {
        logPreCall("StopSound", "" + outputID);
        String[] supported = Data.OutputIDList.split(",");
        checkEnabled();
        JposDevice.check(!JposDevice.member(Integer.toString(outputID), supported),
                JposConst.JPOS_E_ILLEGAL, "outputID not one of {" + String.join(",", supported) + "}");
        SoundPlayer.stopSound(outputID);
        logCall("StopSound");
    }
}
