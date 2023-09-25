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

public class VoiceRecognition extends BaseJposControl implements JposConst, VoiceRecognitionControl116 {
    protected Vector directIOListeners;
    protected Vector statusUpdateListeners;
    protected Vector dataListeners;
    protected Vector errorListeners;
    public VoiceRecognition() {
        deviceControlDescription = "JavaPOS VoiceRecognition Dummy Control";
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
            return VoiceRecognition.this;
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
                service = (VoiceRecognitionService116) baseService; version++;
            } catch (Exception e) {
                if (i >= version * 1000 + 1000000)
                    throw new JposException(JPOS_E_NOSERVICE, "VoiceRecognitionService1" + version + " not fully implemented", e);
            }
        }
    }

    @Override
    public boolean getAutoDisable() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((VoiceRecognitionService116)service).getAutoDisable();
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
            ((VoiceRecognitionService116)service).setAutoDisable(var1);
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
            return ((VoiceRecognitionService116)service).getCapCompareFirmwareVersion();
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
            return ((VoiceRecognitionService116)service).getCapPowerReporting();
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
            return ((VoiceRecognitionService116)service).getCapStatisticsReporting();
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
            return ((VoiceRecognitionService116)service).getCapUpdateFirmware();
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
            return ((VoiceRecognitionService116)service).getCapUpdateStatistics();
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
            return ((VoiceRecognitionService116)service).getDataCount();
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
            return ((VoiceRecognitionService116)service).getDataEventEnabled();
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
            ((VoiceRecognitionService116)service).setDataEventEnabled(var1);
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
            return ((VoiceRecognitionService116)service).getPowerNotify();
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
            ((VoiceRecognitionService116)service).setPowerNotify(var1);
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
            return ((VoiceRecognitionService116)service).getPowerState();
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
            ((VoiceRecognitionService116)service).clearInput();
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
            ((VoiceRecognitionService116)service).clearInputProperties();
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
            ((VoiceRecognitionService116)service).compareFirmwareVersion(var1, var2);
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
            ((VoiceRecognitionService116)service).resetStatistics(var1);
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
            ((VoiceRecognitionService116)service).retrieveStatistics(var1);
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
            ((VoiceRecognitionService116)service).updateFirmware(var1);
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
            ((VoiceRecognitionService116)service).updateStatistics(var1);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public boolean getCapLanguage() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((VoiceRecognitionService116)service).getCapLanguage();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public String getHearingDataPattern() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((VoiceRecognitionService116)service).getHearingDataPattern();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public String getHearingDataWord() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((VoiceRecognitionService116)service).getHearingDataWord();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public String getHearingDataWordList() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((VoiceRecognitionService116)service).getHearingDataWordList();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public int getHearingResult() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((VoiceRecognitionService116)service).getHearingResult();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public int getHearingStatus() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((VoiceRecognitionService116)service).getHearingStatus();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public String getLanguageList() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((VoiceRecognitionService116)service).getLanguageList();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void startHearingFree(String language) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((VoiceRecognitionService116)service).startHearingFree(language);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void startHearingSentence(String language, String wordList, String patternList) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((VoiceRecognitionService116)service).startHearingSentence(language, wordList, patternList);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void startHearingWord(String language, String wordList) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((VoiceRecognitionService116)service).startHearingWord(language, wordList);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void startHearingYesNo(String language) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((VoiceRecognitionService116)service).startHearingYesNo(language);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void stopHearing() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((VoiceRecognitionService116)service).stopHearing();
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
