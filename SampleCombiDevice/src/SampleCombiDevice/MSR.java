/*
 * Copyright 2018 Martin Conrad
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

package SampleCombiDevice;

import de.gmxhome.conrad.jpos.jpos_base.msr.*;
import jpos.JposException;

import java.util.Arrays;

/**
 * Class implementing the MSRInterface for the sample combi device.
 * External and interactive Checkhealth might be implemented in a later version.
 */
public class MSR extends MSRProperties {
    private Device Dev;

    /**
     * Constructor. Gets instance of Device to be used as communication object. Device index for
     * sample is always 0.
     * @param dev Instance of Device this object belongs to.
     */
    public MSR(Device dev) {
        super(0);
        Dev = dev;
    }

    @Override
    public void claim(int timeout) throws JposException {
        Dev.startCommunication();
        super.claim(timeout);
    }

    @Override
    public void release() throws JposException {
        super.release();
        Dev.stopCommunication();
    }

    @Override
    public void deviceEnabled(boolean enable) throws JposException {
        super.deviceEnabled(enable);
        Dev.updateCommonStates(this, enable);
    }

    @Override
    public void checkHealth(int level) throws JposException {
        if (Dev.internalCheckHealth(this, level))
            return;
        // TOBEIMPLEMENTED
        super.checkHealth(level);
    }

    @Override
    public void setDataProperties(Object o) {
        if (o instanceof Device.TrackData) {
            byte[][] tracks = ((Device.TrackData) o).Tracks;
            if (tracks.length == 3) {
                Track1Data = storeData(tracks[0], 0x20, 0x3f);
                Track2Data = storeData(tracks[1], 0x30, 0xf);
                Track3Data = storeData(tracks[2], 0x30, 0xf);
            }
        }
    }
    private byte[] storeData(byte[] source, int subtractor, int maxvalue) {
        if (!TransmitSentinels && source.length > 1)
            source = Arrays.copyOfRange(source, 1, source.length);
        if (DecodeData)
            return source;
        byte[] target = new byte[source.length];
        int i = target.length;
        while (--i >= 0) {
            if ((target[i] = (byte)(source[i] - subtractor)) < 0 || target[i] > maxvalue)
                break;
        }
        return i < 0 ? target : source;
    }
}
