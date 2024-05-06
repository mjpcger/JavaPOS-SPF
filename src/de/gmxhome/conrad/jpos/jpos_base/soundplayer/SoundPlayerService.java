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
import jpos.*;
import jpos.services.*;

import java.io.File;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;
import static jpos.SoundPlayerConst.*;

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
        synchronized (Data.OutputIdListSync) {
            return Data.OutputIDList;
        }
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
        long[] valid = { SPLY_ST_HARDTOTALS, SPLY_ST_HOST };
        logPreSet("Storage");
        checkEnabled();
        checkMember(storage, valid, JPOS_E_ILLEGAL, "Invalid storage selected: " + storage);
        check(Data.CapStorage == SPLY_CST_HOST_ONLY &&
                storage != SPLY_ST_HOST, JPOS_E_ILLEGAL, "Unsupported storage selected: " + storage);
        check(Data.CapStorage == SPLY_CST_HARDTOTALS_ONLY &&
                storage != SPLY_ST_HARDTOTALS, JPOS_E_ILLEGAL, "Unsupported storage selected: " + storage);
        check(Data.CapAssociatedHardTotalsDevice.length() == 0 && storage == SPLY_ST_HARDTOTALS,
                JPOS_E_ILLEGAL, "No HardTotals device configured");
        SoundPlayer.storage(storage);
        logSet("Storage");
    }

    @Override
    public void setVolume(int volume) throws JposException {
        logPreSet("Volume");
        checkEnabled();
        check(!Data.CapVolume && volume != Data.Volume, JPOS_E_ILLEGAL, "Unsupported volume selected: " + volume);
        check( volume < 0 || volume > 100, JPOS_E_ILLEGAL, "Volume out of range 0 - 100: " + volume);
        SoundPlayer.volume(volume);
        logSet("Volume");
    }

    @Override
    public void playSound(String fileName, boolean loop) throws JposException {
        logPreCall("PlaySound", removeOuterArraySpecifier(new Object[]{fileName, loop}, Device.MaxArrayStringElements));
        String[] supported = Data.CapSoundTypeList.toLowerCase().split(",");
        checkEnabled();
        if (!member(fileName, Data.DeviceSoundList.split(","))) {
            String name = new File(fileName).getName();
            int dotpos = name.lastIndexOf('.');
            check(dotpos >= 0 && !member(name.substring(dotpos + 1), supported),
                    JPOS_E_ILLEGAL, "File type not one of {" + Data.CapSoundTypeList + "}");
        }
        JposOutputRequest request = SoundPlayer.playSound(fileName, loop);
        if (request != null)
            request.enqueue();
        logAsyncCall("PlaySound");
    }

    @Override
    public void stopSound(int outputID) throws JposException {
        logPreCall("StopSound", removeOuterArraySpecifier(new Object[]{outputID}, Device.MaxArrayStringElements));
        synchronized (Data.OutputIdListSync) {
            String[] supported = Data.OutputIDList.split(",");
            checkEnabled();
            check(!member(Integer.toString(outputID), supported),
                    JPOS_E_ILLEGAL, "outputID not one of {" + String.join(",", supported) + "}");
        }
        SoundPlayer.stopSound(outputID);
        logCall("StopSound");
    }
}
