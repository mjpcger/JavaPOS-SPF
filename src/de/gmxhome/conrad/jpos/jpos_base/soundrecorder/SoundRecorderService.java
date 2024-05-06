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
import jpos.*;
import jpos.services.*;

import java.util.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;
import static jpos.SoundRecorderConst.*;

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
        check(!Data.CapChannel && !Data.Channel.equals(channel), JPOS_E_ILLEGAL, "Changing channel not supported");
        check(!member(channel, Data.ChannelList.split(",")), JPOS_E_ILLEGAL, "Invalid channel: " + channel);
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
        check(!Data.CapRecordingLevel && Data.RecordingLevel != recordingLevel, JPOS_E_ILLEGAL, "Changing channel not supported");
        check(recordingLevel < 0 || recordingLevel > 100, JPOS_E_ILLEGAL, "Invalid recording level: " + recordingLevel);
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
        check(!Data.CapSamplingRate && !Data.SamplingRate.equals(samplingRate), JPOS_E_ILLEGAL, "Changing sampling rate not supported");
        check(!member(samplingRate, Data.SamplingRateList.split(",")), JPOS_E_ILLEGAL, "Invalid sampling rate: " + samplingRate);
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
        check(!Data.CapSoundType && !Data.SoundType.equals(soundType), JPOS_E_ILLEGAL, "Changing sampling rate not supported");
        check(!member(soundType, Data.SoundTypeList.split(",")), JPOS_E_ILLEGAL, "Invalid sound type: " + soundType);
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
        long[] valid = {SREC_ST_HOST, SREC_ST_HARDTOTALS, SREC_ST_HOST_HARDTOTALS};
        check(Data.CapStorage == SREC_CST_HOST_ONLY && storage != SREC_ST_HOST, JPOS_E_ILLEGAL, "Invalid storage: " + storage);
        check(Data.CapStorage == SREC_CST_HARDTOTALS_ONLY && storage != SREC_ST_HARDTOTALS, JPOS_E_ILLEGAL, "Invalid storage: " + storage);
        checkMember(storage, valid, JPOS_E_ILLEGAL, "Invalid storage: " + storage);
        SoundRecorder.storage(storage);
        logSet("Storage");
    }

    @Override
    public void startRecording(String fileName, boolean overWrite, int recordingTime) throws JposException {
        logPreCall("StartRecording", removeOuterArraySpecifier(new Object[]{fileName, overWrite, recordingTime}, Device.MaxArrayStringElements));
        checkEnabled();
        check(Data.AsyncInputActive, JPOS_E_BUSY, "Just recording other sound");
        check(recordingTime <= 0 && recordingTime != JPOS_FOREVER,
                JPOS_E_ILLEGAL, "Invalid recording time: " + recordingTime);
        StartRecording request = SoundRecorder.startRecording(fileName, overWrite, recordingTime);
        if (request != null)
            request.enqueue();
        logCall("StartRecording");
    }

    @Override
    public void stopRecording() throws JposException {
        logPreCall("StopRecording");
        checkEnabled();
        check(!Data.AsyncInputActive, JPOS_E_ILLEGAL, "Recording not active");
        SoundRecorder.stopRecording();
        logCall("StopRecording");
    }
}
