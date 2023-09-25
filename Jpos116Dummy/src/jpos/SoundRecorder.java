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

package jpos;

import jpos.events.*;
import jpos.services.*;
import java.util.Vector;

public class SoundRecorder extends BaseJposControl implements JposConst, SoundRecorderControl116 {
    protected Vector directIOListeners;
    protected Vector statusUpdateListeners;
    protected Vector dataListeners;
    protected Vector errorListeners;
    public SoundRecorder() {
        deviceControlDescription = "JavaPOS GestureControl Dummy Control";
        deviceControlVersion = deviceVersion115 + 1000;
        directIOListeners = new Vector();
        statusUpdateListeners = new Vector();
        dataListeners = new Vector();
        errorListeners = new Vector();
    }
    protected class Callbacks implements EventCallbacks {

        @Override
        public void fireDataEvent(DataEvent dataEvent) {
            synchronized (dataListeners) {
                for (Object listener : dataListeners)
                    ((DataListener) listener).dataOccurred(dataEvent);
            }
        }

        @Override
        public void fireDirectIOEvent(DirectIOEvent directIOEvent) {
            synchronized (directIOListeners) {
                for (Object listener : directIOListeners)
                    ((DirectIOListener) listener).directIOOccurred(directIOEvent);
            }
        }

        @Override
        public void fireErrorEvent(ErrorEvent errorEvent) {
            synchronized (errorListeners) {
                for (Object listener : errorListeners)
                    ((ErrorListener) listener).errorOccurred(errorEvent);
            }
        }

        @Override
        public void fireOutputCompleteEvent(OutputCompleteEvent outputCompleteEvent) {
        }

        @Override
        public void fireStatusUpdateEvent(StatusUpdateEvent statusUpdateEvent) {
            synchronized (statusUpdateListeners) {
                for (Object listener : statusUpdateListeners)
                    ((StatusUpdateListener)listener).statusUpdateOccurred(statusUpdateEvent);
            }
        }

        @Override
        public BaseControl getEventSource() {
            return SoundRecorder.this;
        }
    }
    @Override
    protected EventCallbacks createEventCallbacks() {
        return new Callbacks();
    }

    @Override
    protected void setDeviceService(BaseService baseService, int i) throws JposException {
        if (baseService == null) {
            service = null;
        } else {
            int version = 16;
            try {
                service = (SoundRecorderService116) baseService; version++;
            } catch (Exception e) {
                if (i >= version * 1000 + 1000000)
                    throw new JposException(JPOS_E_NOSERVICE, "GestureControlService1" + version + " not fully implemented", e);
            }
        }
    }

    @Override
    public boolean getAutoDisable() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getAutoDisable();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void setAutoDisable(boolean var1) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((SoundRecorderService116)service).setAutoDisable(var1);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public boolean getCapCompareFirmwareVersion() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getCapCompareFirmwareVersion();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public int getCapPowerReporting() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getCapPowerReporting();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public boolean getCapStatisticsReporting() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getCapStatisticsReporting();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public boolean getCapUpdateFirmware() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getCapUpdateFirmware();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public boolean getCapUpdateStatistics() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getCapUpdateStatistics();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public int getDataCount() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getDataCount();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public boolean getDataEventEnabled() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getDataEventEnabled();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void setDataEventEnabled(boolean var1) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((SoundRecorderService116)service).setDataEventEnabled(var1);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public int getPowerNotify() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getPowerNotify();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void setPowerNotify(int var1) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((SoundRecorderService116)service).setPowerNotify(var1);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public int getPowerState() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getPowerState();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void clearInput() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((SoundRecorderService116)service).clearInput();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void clearInputProperties() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((SoundRecorderService116)service).clearInputProperties();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void compareFirmwareVersion(String var1, int[] var2) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((SoundRecorderService116)service).compareFirmwareVersion(var1, var2);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void resetStatistics(String var1) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((SoundRecorderService116)service).resetStatistics(var1);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void retrieveStatistics(String[] var1) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((SoundRecorderService116)service).retrieveStatistics(var1);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void updateFirmware(String var1) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((SoundRecorderService116)service).updateFirmware(var1);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void updateStatistics(String var1) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((SoundRecorderService116)service).updateStatistics(var1);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public String getCapAssociatedHardTotalsDevice() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getCapAssociatedHardTotalsDevice();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public boolean getCapChannel() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getCapChannel();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public boolean getCapRecordingLevel() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getCapRecordingLevel();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public boolean getCapSamplingRate() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getCapSamplingRate();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public boolean getCapSoundType() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getCapSoundType();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public int getCapStorage() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getCapStorage();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public String getChannel() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getChannel();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void setChannel(String var1) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((SoundRecorderService116)service).setChannel(var1);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public String getChannelList() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getChannelList();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public int getRecordingLevel() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getRecordingLevel();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void setRecordingLevel(int var1) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((SoundRecorderService116)service).setRecordingLevel(var1);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public int getRemainingRecordingTimeInSec() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getRemainingRecordingTimeInSec();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public String getSamplingRate() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getSamplingRate();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void setSamplingRate(String var1) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((SoundRecorderService116)service).setSamplingRate(var1);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public String getSamplingRateList() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getSamplingRateList();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public byte[] getSoundData() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getSoundData();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public String getSoundType() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getSoundType();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void setSoundType(String var1) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((SoundRecorderService116)service).setSoundType(var1);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public String getSoundTypeList() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getSoundTypeList();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public int getStorage() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((SoundRecorderService116)service).getStorage();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void setStorage(int var1) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((SoundRecorderService116)service).setStorage(var1);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void startRecording(String fileName, boolean overwrite, int recordingTime) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((SoundRecorderService116)service).startRecording(fileName, overwrite, recordingTime);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void stopRecording() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((SoundRecorderService116)service).stopRecording();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void addDataListener(DataListener l) {
        synchronized (dataListeners) {
            dataListeners.addElement(l);
        }
    }

    @Override
    public void removeDataListener(DataListener l) {
        synchronized (dataListeners) {
            dataListeners.removeElement(l);
        }
    }

    @Override
    public void addDirectIOListener(DirectIOListener l) {
        synchronized(directIOListeners)
        {
            directIOListeners.addElement(l);
        }
    }

    @Override
    public void removeDirectIOListener(DirectIOListener l) {
        synchronized(directIOListeners)
        {
            directIOListeners.removeElement(l);
        }
    }

    @Override
    public void addErrorListener(ErrorListener l) {
        synchronized (errorListeners) {
            errorListeners.addElement(l);
        }
    }

    @Override
    public void removeErrorListener(ErrorListener l) {
        synchronized (errorListeners) {
            errorListeners.removeElement(l);
        }
    }

    @Override
    public void addStatusUpdateListener(StatusUpdateListener l) {
        synchronized (statusUpdateListeners) {
            statusUpdateListeners.addElement(l);
        }
    }

    @Override
    public void removeStatusUpdateListener(StatusUpdateListener l) {
        synchronized(statusUpdateListeners)
        {
            statusUpdateListeners.removeElement(l);
        }
    }
}
