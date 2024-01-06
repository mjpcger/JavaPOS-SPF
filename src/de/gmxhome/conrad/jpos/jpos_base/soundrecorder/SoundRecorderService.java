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

package de.gmxhome.conrad.jpos.jpos_base.soundrecorder;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.soundplayer.SoundPlayerInterface;
import jpos.JposConst;
import jpos.JposException;
import jpos.SoundRecorder;
import jpos.SoundRecorderConst;
import jpos.services.SoundRecorderService116;

import java.io.File;
import java.util.Arrays;

/**
 * SoundRecorder service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class SoundRecorderService extends JposBase implements SoundRecorderService116 {
    private final SoundRecorderProperties Data;

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public SoundRecorderService(SoundRecorderProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    /**
     * Instance of a class implementing the SoundRecorderInterface for sound recorder specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public SoundRecorderInterface SoundRecorder;

    @Override
    public String getCapAssociatedHardTotalsDevice() throws JposException {
        logGet("CapAssociatedHardTotalsDevice");
        checkOpened();
        return Data.CapAssociatedHardTotalsDevice;
    }

    @Override
    public boolean getCapChannel() throws JposException {
        logGet("CapChannel");
        checkOpened();
        return Data.CapChannel;
    }

    @Override
    public boolean getCapRecordingLevel() throws JposException {
        logGet("CapRecordingLevel");
        checkOpened();
        return Data.CapRecordingLevel;
    }

    @Override
    public boolean getCapSamplingRate() throws JposException {
        logGet("CapSamplingRate");
        checkOpened();
        return Data.CapSamplingRate;
    }

    @Override
    public boolean getCapSoundType() throws JposException {
        logGet("CapSoundType");
        checkOpened();
        return Data.CapSoundType;
    }

    @Override
    public int getCapStorage() throws JposException {
        logGet("CapStorage");
        checkOpened();
        return Data.CapStorage;
    }

    @Override
    public String getChannel() throws JposException {
        logGet("Channel");
        checkEnabled();
        return Data.Channel;
    }

    @Override
    public void setChannel(String channel) throws JposException {
        logPreSet("Channel");
        checkEnabled();
        JposDevice.check(!Data.CapChannel && !Data.Channel.equals(channel), JposConst.JPOS_E_ILLEGAL, "Changing channel not supported");
        JposDevice.check(!JposDevice.member(channel, Data.ChannelList.split(",")), JposConst.JPOS_E_ILLEGAL, "Invalid channel: " + channel);
        SoundRecorder.channel(channel);
        logSet("Channel");
    }

    @Override
    public String getChannelList() throws JposException {
        logGet("ChannelList");
        checkOpened();
        return Data.ChannelList;
    }

    @Override
    public int getRecordingLevel() throws JposException {
        logGet("RecordingLevel");
        checkEnabled();
        return Data.RecordingLevel;
    }

    @Override
    public void setRecordingLevel(int recordingLevel) throws JposException {
        logPreSet("RecordingLevel");
        checkEnabled();
        JposDevice.check(!Data.CapRecordingLevel && Data.RecordingLevel != recordingLevel, JposConst.JPOS_E_ILLEGAL, "Changing channel not supported");
        JposDevice.check(recordingLevel < 0 || recordingLevel > 100, JposConst.JPOS_E_ILLEGAL, "Invalid recording level: " + recordingLevel);
        SoundRecorder.recordingLevel(recordingLevel);
        logSet("RecordingLevel");
    }

    @Override
    public int getRemainingRecordingTimeInSec() throws JposException {
        logGet("RemainingRecordingTimeInSec");
        checkEnabled();
        return Data.RemainingRecordingTimeInSec;
    }

    @Override
    public String getSamplingRate() throws JposException {
        logGet("SamplingRate");
        checkEnabled();
        return Data.SamplingRate;
    }

    @Override
    public void setSamplingRate(String samplingRate) throws JposException {
        logPreSet("SamplingRate");
        checkEnabled();
        JposDevice.check(!Data.CapSamplingRate && !Data.SamplingRate.equals(samplingRate), JposConst.JPOS_E_ILLEGAL, "Changing sampling rate not supported");
        JposDevice.check(!JposDevice.member(samplingRate, Data.SamplingRateList.split(",")), JposConst.JPOS_E_ILLEGAL, "Invalid sampling rate: " + samplingRate);
        SoundRecorder.samplingRate(samplingRate);
        logSet("SamplingRate");
    }

    @Override
    public String getSamplingRateList() throws JposException {
        logGet("SamplingRateList");
        checkOpened();
        return Data.SamplingRateList;
    }

    @Override
    public byte[] getSoundData() throws JposException {
        logGet("SoundData");
        checkOpened();
        synchronized (Data) {
            return Arrays.copyOf(Data.SoundData, Data.SoundData.length);
        }
    }

    @Override
    public String getSoundType() throws JposException {
        logGet("SoundType");
        checkEnabled();
        return Data.SoundType;
    }

    @Override
    public void setSoundType(String soundType) throws JposException {
        logPreSet("SoundType");
        checkEnabled();
        JposDevice.check(!Data.CapSoundType && !Data.SoundType.equals(soundType), JposConst.JPOS_E_ILLEGAL, "Changing sampling rate not supported");
        JposDevice.check(!JposDevice.member(soundType, Data.SoundTypeList.split(",")), JposConst.JPOS_E_ILLEGAL, "Invalid sound type: " + soundType);
        SoundRecorder.soundType(soundType);
        logSet("SoundType");
    }

    @Override
    public String getSoundTypeList() throws JposException {
        logGet("SoundTypeList");
        checkOpened();
        return Data.SoundTypeList;
    }

    @Override
    public int getStorage() throws JposException {
        logGet("Storage");
        checkEnabled();
        return Data.Storage;
    }

    @Override
    public void setStorage(int storage) throws JposException {
        logPreSet("Storage");
        checkEnabled();
        long[] valid = {SoundRecorderConst.SREC_ST_HOST, SoundRecorderConst.SREC_ST_HARDTOTALS, SoundRecorderConst.SREC_ST_HOST_HARDTOTALS};
        JposDevice.check(Data.CapStorage == SoundRecorderConst.SREC_CST_HOST_ONLY && storage != SoundRecorderConst.SREC_ST_HOST, JposConst.JPOS_E_ILLEGAL, "Invalid storage: " + storage);
        JposDevice.check(Data.CapStorage == SoundRecorderConst.SREC_CST_HARDTOTALS_ONLY && storage != SoundRecorderConst.SREC_ST_HARDTOTALS, JposConst.JPOS_E_ILLEGAL, "Invalid storage: " + storage);
        JposDevice.checkMember(storage, valid, JposConst.JPOS_E_ILLEGAL, "Invalid storage: " + storage);
        SoundRecorder.storage(storage);
        logSet("Storage");
    }

    @Override
    public void startRecording(String fileName, boolean overWrite, int recordingTime) throws JposException {
        logPreCall("StartRecording", fileName + ", " + overWrite + ", " + recordingTime);
        checkEnabled();
        JposDevice.check(Data.AsyncInputActive, JposConst.JPOS_E_BUSY, "Just recording other sound");
        File f = new File(fileName);
        JposDevice.check(f.exists() && !overWrite, JposConst.JPOS_E_EXISTS, "Just present: " + fileName);
        JposDevice.check(f.exists() && !f.isFile(), JposConst.JPOS_E_FAILURE, "No regular file: " + fileName);
        JposDevice.check(recordingTime <= 0 && recordingTime != JposConst.JPOS_FOREVER,
                JposConst.JPOS_E_ILLEGAL, "Invalid recording time: " + recordingTime);
        StartRecording request = SoundRecorder.startRecording(fileName, overWrite, recordingTime);
        if (request != null)
            request.enqueue();
        logAsyncCall("StartRecording");
    }

    /**
     * Specifies whether a video is currently recording.
     */
    public boolean VideoRecording = false;

    @Override
    public void stopRecording() throws JposException {
        logPreCall("StopRecording");
        checkEnabled();
        SoundRecorder.stopRecording();
        logAsyncCall("StopRecording");
    }
}
