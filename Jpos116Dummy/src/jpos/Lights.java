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

public class Lights extends BaseJposControl implements JposConst, LightsControl116 {
    protected LightsService112 service112;
    protected LightsService113 service113;
    protected LightsService114 service114;
    protected LightsService115 service115;
    protected LightsService116 service116;
    protected Vector directIOListeners;
    protected Vector statusUpdateListeners;

    public Lights() {
        deviceControlDescription = "JavaPOS Lights Dummy Control";
        deviceControlVersion = deviceVersion115 + 1000;
        directIOListeners = new Vector();
        statusUpdateListeners = new Vector();
    }

    protected class Callbacks implements EventCallbacks {

        @Override
        public void fireDataEvent(DataEvent dataEvent) {
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
            return Lights.this;
        }
    }

    @Override
    protected EventCallbacks createEventCallbacks() {
        return new Callbacks();
    }

    @Override
    protected void setDeviceService(BaseService baseService, int i) throws JposException {
        if (baseService == null) {
            service112 = service113 = service114 = service115 = service116 = null;
        } else {
            int version = 12;
            try {
                service112 = (LightsService112) baseService; version++;
                service113 = (LightsService113) baseService; version++;
                service114 = (LightsService114) baseService; version++;
                service115 = (LightsService115) baseService; version++;
                service116 = (LightsService116) baseService; version++;
            } catch (Exception e) {
                if (i >= version * 1000 + 1000000)
                    throw new JposException(JPOS_E_NOSERVICE, "LightsService11" + version + " not fully implemented");
            }
        }
    }

    @Override
    public void addDirectIOListener(DirectIOListener directIOListener) {
        synchronized(directIOListeners)
        {
            directIOListeners.addElement(directIOListener);
        }
    }

    @Override
    public void removeDirectIOListener(DirectIOListener directIOListener) {
        synchronized(directIOListeners)
        {
            directIOListeners.removeElement(directIOListener);
        }
    }

    @Override
    public void addStatusUpdateListener(StatusUpdateListener statusUpdateListener) {
        synchronized(statusUpdateListeners)
        {
            statusUpdateListeners.addElement(statusUpdateListener);
        }
    }

    @Override
    public void removeStatusUpdateListener(StatusUpdateListener statusUpdateListener) {
        synchronized(statusUpdateListeners)
        {
            statusUpdateListeners.removeElement(statusUpdateListener);
        }
    }


    @Override
    public int getCapAlarm() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((LightsService112)service).getCapAlarm();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public boolean getCapBlink() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((LightsService112)service).getCapBlink();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public int getCapColor() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((LightsService112)service).getCapColor();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public boolean getCapCompareFirmwareVersion() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((LightsService112)service).getCapCompareFirmwareVersion();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public int getCapPowerReporting() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((LightsService112)service).getCapPowerReporting();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public boolean getCapStatisticsReporting() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((LightsService112)service).getCapStatisticsReporting();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public boolean getCapUpdateFirmware() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((LightsService112)service).getCapUpdateFirmware();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public boolean getCapUpdateStatistics() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((LightsService112)service).getCapUpdateStatistics();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public int getMaxLights() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((LightsService112)service).getMaxLights();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public int getPowerNotify() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((LightsService112)service).getPowerNotify();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public void setPowerNotify(int i) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((LightsService112)service).setPowerNotify(i);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public int getPowerState() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((LightsService112)service).getPowerState();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public void compareFirmwareVersion(String s, int[] ints) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((LightsService112)service).compareFirmwareVersion(s, ints);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public void resetStatistics(String s) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((LightsService112)service).resetStatistics(s);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public void retrieveStatistics(String[] strings) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((LightsService112)service).retrieveStatistics(strings);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public void switchOff(int i) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((LightsService112)service).switchOff(i);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public void switchOn(int i, int i1, int i2, int i3, int i4) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((LightsService112)service).switchOn(i, i1, i2, i3, i4);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public void updateFirmware(String s) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((LightsService112)service).updateFirmware(s);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public void updateStatistics(String s) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((LightsService112)service).updateStatistics(s);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public int getCapPattern() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((LightsService116)service).getCapPattern();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public void switchOnMultiple(String lightNumbers, int blinkOnCycle, int blinkOffCycle, int color, int alarm) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((LightsService116)service).switchOnMultiple(lightNumbers, blinkOnCycle, blinkOffCycle, color, alarm);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public void switchOnPattern(int pattern, int alarm) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((LightsService116)service).switchOnPattern(pattern, alarm);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public void switchOffPattern() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((LightsService116)service).switchOffPattern();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }
}
