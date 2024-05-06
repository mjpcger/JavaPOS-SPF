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
import jpos.JposException;

import static jpos.SoundRecorderConst.*;

/**
 * Class containing the sound recorder specific properties, their default values and default implementations of
 * SoundRecorderInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Sound Recorder.
 */
public class SoundRecorderProperties extends JposCommonProperties implements SoundRecorderInterface {
    /**
     * UPOS property CapAssociatedHardTotalsDevice. Default: an empty string. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public String CapAssociatedHardTotalsDevice = "";

    /**
     * UPOS property CapChannel. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapChannel = false;

    /**
     * UPOS property CapRecordingLevel. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapRecordingLevel = false;

    /**
     * UPOS property CapSamplingRate. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapSamplingRate = false;

    /**
     * UPOS property CapSoundType. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapSoundType = false;

    /**
     * UPOS property CapStorage. Default: CST_HOST_ONLY. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int CapStorage = SREC_CST_HOST_ONLY;

    /**
     * UPOS property Channel. Default: null. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method. If not set, Channel will be set to the first type listed in ChannelList within the
     * initOnOpen method.
     */
    public String Channel = null;

    /**
     * UPOS property ChannelList. Default: an empty string. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public String ChannelList = "";

    /**
     * UPOS property RecordingLevel. Default: 50. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int RecordingLevel = 50;

    /**
     * UPOS property RemainingRecordingTimeInSec. Will be set to 0 within the
     * InitOnEnable method.
     */
    public int RemainingRecordingTimeInSec;

    /**
     * UPOS property SamplingRate. Default: null. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method. If not set, SamplingRate will be set to the first type listed in SamplingRateList within the
     * initOnOpen method.
     */
    public String SamplingRate = null;

    /**
     * UPOS property SamplingRateList. Default: an empty string. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public String SamplingRateList = "";

    /**
     * UPOS property SoundData. Default: A zero-length byte array.
     */
    public byte[] SoundData = {};

    /**
     * UPOS property SoundType. Default: null. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method. If not set, SoundType will be set to the first type listed in SoundTypeList within the
     * initOnOpen method.
     */
    public String SoundType = null;

    /**
     * UPOS property SoundTypeList. Default: an empty string. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public String SoundTypeList = "";

    /**
     * UPOS property Storage. Default: an empty string. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int Storage = SREC_ST_HOST;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected SoundRecorderProperties(int dev) {
        super(dev);
}

    @Override
    public void initOnOpen() {
        super.initOnOpen();
        if (Channel == null)
            Channel = ChannelList.split(",")[0];
        if (SamplingRate == null)
            SamplingRate = SamplingRateList.split(",")[0];
        if (SoundType == null)
            SoundType = SoundTypeList.split(",")[0];
    }
    @Override
    public void initOnEnable(boolean flag) {
        RemainingRecordingTimeInSec = 0;
    }

    @Override
    public void clearDataProperties() {
        if (SoundData.length > 0) {
            SoundData = new byte[0];
            EventSource.logSet("SoundData");
        }
    }

    @Override
    public JposOutputRequest newJposOutputRequest() {
        return new JposInputRequest(this);
    }

    @Override
    public void channel(String channel) throws JposException {
        Channel = channel;
    }

    @Override
    public void recordingLevel(int recordingLevel) throws JposException {
        RecordingLevel = recordingLevel;
    }

    @Override
    public void samplingRate(String samplingRate) throws JposException {
        SamplingRate = samplingRate;
    }

    @Override
    public void soundType(String soundType) throws JposException {
        SoundType = soundType;
    }

    @Override
    public void storage(int storage) throws JposException {
        Storage = storage;
    }

    @Override
    public StartRecording startRecording(String fileName, boolean overWrite, int recordingTime) throws JposException {
        return new StartRecording(this, fileName, overWrite, recordingTime);
    }

    @Override
    public void startRecording(StartRecording request) throws JposException {
    }

    @Override
    public void stopRecording() throws JposException {
    }
}
