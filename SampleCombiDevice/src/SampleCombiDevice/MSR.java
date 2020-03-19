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

import de.gmxhome.conrad.jpos.jpos_base.SyncObject;
import de.gmxhome.conrad.jpos.jpos_base.msr.*;
import jpos.JposConst;
import jpos.JposException;
import jpos.MSRConst;

import javax.swing.*;
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
        if (!Dev.internalCheckHealth(this, level) && !externalCheckHealth(level)) {
            interactiveCheckHealth(level);
        }
        super.checkHealth(level);
    }

    private void interactiveCheckHealth(int level) {
        if (level == JposConst.JPOS_CH_INTERACTIVE) {
            int datacount = DataCount;
            int loopcount;
            String result;
            try {
                clearDataProperties();
                ((MSRService) EventSource).setFreezeEvents(true);
                if (!DataEventEnabled)
                    ((MSRService) EventSource).setDataEventEnabled(true);
                Dev.synchronizedMessageBox("Press OK, then swipe a card", "CheckHealth MSR", JOptionPane.INFORMATION_MESSAGE);
                for (loopcount = 0; loopcount < 100 && datacount == DataCount && (!Dev.DeviceIsOffline && !Dev.InIOError); loopcount++)
                    new SyncObject().suspend(100);
                result = (loopcount == 100 ? "Timed out" : (datacount < DataCount ? "OK" : "Error"));
                ((MSRService) EventSource).setFreezeEvents(false);
            } catch (JposException e) {
                result = "Error, " + e.getMessage();
            }
            Dev.synchronizedMessageBox("MSR check " + result + ".", "CheckHealth MSR",
                    (result.equals("OK") ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE));
            CheckHealthText = "Interactive check: " + result;
        }
    }

    private boolean externalCheckHealth(int level) {
        if (level == JposConst.JPOS_CH_EXTERNAL) {
            int datacount = DataCount;
            int loopcount;
            String result;
            try {
                clearDataProperties();
                ((MSRService) EventSource).setFreezeEvents(true);
                if (!DataEventEnabled)
                    ((MSRService) EventSource).setDataEventEnabled(true);
                for (loopcount = 0; loopcount < 100 && datacount == DataCount && (!Dev.DeviceIsOffline && !Dev.InIOError); loopcount++)
                    new SyncObject().suspend(100);
                result = (loopcount == 100 ? "Timed out" : (datacount < DataCount ? "OK" : "Error"));
                ((MSRService) EventSource).setFreezeEvents(false);
            } catch (JposException e) {
                result = "Error, " + e.getMessage();
            }
            CheckHealthText = "External check: " + result;
            return true;
        }
        return false;
    }

    @Override
    public void setDataProperties(Object o) {
        if (o instanceof Device.TrackData) {
            byte[][] tracks = ((Device.TrackData) o).Tracks;
            if (tracks.length == 3) {
                Track1Data = (TracksToRead & MSRConst.MSR_TR_1) != 0 ? storeData(tracks[0], 0x20, 0x3f) : new byte[0];
                Track2Data = (TracksToRead & MSRConst.MSR_TR_2) != 0 ? storeData(tracks[1], 0x30, 0xf) : new byte[0];
                Track3Data = (TracksToRead & MSRConst.MSR_TR_3) != 0 ? storeData(tracks[2], 0x30, 0xf) : new byte[0];
            }
        }
    }

    @Override
    public void transmitSentinels(boolean transmit) throws JposException {
        boolean previous = TransmitSentinels;
        super.transmitSentinels(transmit);
        if (previous != TransmitSentinels) {
            Track1Data = changeTrack(Track1Data, "Track1Data", previous, (byte)'&');
            Track2Data = changeTrack(Track2Data, "Track2Data", previous, (byte)';');
            Track3Data = changeTrack(Track3Data, "Track3Data", previous, (byte)';');
        }
    }

    @Override
    public void decodeData(boolean decode) throws JposException {
        boolean previous = DecodeData;
        super.decodeData(decode);
        if (previous != DecodeData) {
            Track1Data = codeTrack(Track1Data, "Track1Data", previous ? -0x20 : 0x20);
            Track2Data = codeTrack(Track2Data, "Track2Data", previous ? -0x30 : 0x30);
            Track3Data = codeTrack(Track3Data, "Track3Data", previous ? -0x30 : 0x30);
        }
    }

    private byte[] changeTrack(byte[] track, String name, boolean previous, byte start) {
        if (track.length > 0) {
            byte[] target;
            if (previous)
                track =  Arrays.copyOfRange(track, 1, track.length - 1);
            else {
                (target = new byte[track.length + 2])[0] = start;
                System.arraycopy(track, 0, target, 1, track.length);
                (track = target)[target.length - 1] = (byte)'?';
            }
            EventSource.logSet(name);
        }
        return track;
    }

    private byte[] codeTrack(byte[] data, String name, int delta) {
        if (data != null && data.length > 0) {
            for (int i = 0; i < data.length; i++)
                data[i] += delta;
            EventSource.logSet(name);
        }
        return data;
    }

    private byte[] storeData(byte[] source, int subtractor, int maxvalue) {
        if (!TransmitSentinels && source.length > 1)
            source = Arrays.copyOfRange(source, 1, source.length - 1);
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
