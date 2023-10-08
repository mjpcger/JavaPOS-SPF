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
import jpos.services.SoundRecorderService116;

/**
 * SoundRecorder service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class SoundRecorderService extends JposBase implements SoundRecorderService116 {
    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public SoundRecorderService(JposCommonProperties props, JposDevice device) {
        super(props, device);
    }

    /**
     * Instance of a class implementing the SoundRecorderInterface for tone indicator specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public SoundRecorderInterface SoundRecorder;

    @Override
    public String getCapAssociatedHardTotalsDevice() throws JposException {
        return null;
    }

    @Override
    public boolean getCapChannel() throws JposException {
        return false;
    }

    @Override
    public boolean getCapRecordingLevel() throws JposException {
        return false;
    }

    @Override
    public boolean getCapSamplingRate() throws JposException {
        return false;
    }

    @Override
    public boolean getCapSoundType() throws JposException {
        return false;
    }

    @Override
    public int getCapStorage() throws JposException {
        return 0;
    }

    @Override
    public String getChannel() throws JposException {
        return null;
    }

    @Override
    public void setChannel(String s) throws JposException {

    }

    @Override
    public String getChannelList() throws JposException {
        return null;
    }

    @Override
    public int getRecordingLevel() throws JposException {
        return 0;
    }

    @Override
    public void setRecordingLevel(int i) throws JposException {

    }

    @Override
    public int getRemainingRecordingTimeInSec() throws JposException {
        return 0;
    }

    @Override
    public String getSamplingRate() throws JposException {
        return null;
    }

    @Override
    public void setSamplingRate(String s) throws JposException {

    }

    @Override
    public String getSamplingRateList() throws JposException {
        return null;
    }

    @Override
    public byte[] getSoundData() throws JposException {
        return new byte[0];
    }

    @Override
    public String getSoundType() throws JposException {
        return null;
    }

    @Override
    public void setSoundType(String s) throws JposException {

    }

    @Override
    public String getSoundTypeList() throws JposException {
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
    public void startRecording(String s, boolean b, int i) throws JposException {

    }

    @Override
    public void stopRecording() throws JposException {

    }
}
