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

public class POSPower extends BaseJposControl implements JposConst, POSPowerControl116 {
    protected POSPowerService15 service15;
    protected POSPowerService16 service16;
    protected POSPowerService17 service17;
    protected POSPowerService18 service18;
    protected POSPowerService19 service19;
    protected POSPowerService110 service110;
    protected POSPowerService111 service111;
    protected POSPowerService112 service112;
    protected POSPowerService113 service113;
    protected POSPowerService114 service114;
    protected POSPowerService115 service115;
    protected POSPowerService116 service116;
    protected Vector directIOListeners;
    protected Vector statusUpdateListeners;

    private static final int v116 = deviceVersion115 + 1000;

    public POSPower() {
        deviceControlDescription = "JavaPOS POSPower Dummy Control";
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
            return POSPower.this;
        }
    }

    @Override
    protected EventCallbacks createEventCallbacks() {
        return new Callbacks();
    }

    @Override
    protected void setDeviceService(BaseService baseService, int i) throws JposException {
        if (baseService == null) {
            service15 = service16 = service17 = service18 = service19 = service110 = service111 = service112 = service113 = service114 = service115 = null;service116 = null;
        } else {
            int version = 5;
            try {
                service15  = (POSPowerService15) baseService; version++;
                service16  = (POSPowerService16) baseService; version++;
                service17  = (POSPowerService17) baseService; version++;
                service18  = (POSPowerService18) baseService; version++;
                service19  = (POSPowerService19) baseService; version++;
                service110 = (POSPowerService110) baseService; version++;
                service111 = (POSPowerService111) baseService; version++;
                service112 = (POSPowerService112) baseService; version++;
                service113 = (POSPowerService113) baseService; version++;
                service114 = (POSPowerService114) baseService; version++;
                service115 = (POSPowerService115) baseService; version++;
                service116 = (POSPowerService116) baseService; version++;
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
    public int getBatteryCapacityRemainingInSeconds() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService116)service).getBatteryCapacityRemainingInSeconds();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < v116 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.16");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public int getBatteryCriticallyLowThresholdInSeconds() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService116)service).getBatteryCriticallyLowThresholdInSeconds();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < v116 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.16");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public void setBatteryCriticallyLowThresholdInSeconds(int seconds) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((POSPowerService116)service).setBatteryCriticallyLowThresholdInSeconds(seconds);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < v116 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.16");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public int getBatteryLowThresholdInSeconds() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService116)service).getBatteryLowThresholdInSeconds();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < v116 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.16");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public void setBatteryLowThresholdInSeconds(int seconds) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((POSPowerService116)service).setBatteryLowThresholdInSeconds(seconds);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < v116 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.16");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public boolean getCapBatteryCapacityRemainingInSeconds() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService116)service).getCapBatteryCapacityRemainingInSeconds();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < v116 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.16");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public boolean getCapChargeTime() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService116)service).getCapChargeTime();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < v116 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.16");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public boolean getCapVariableBatteryCriticallyLowThresholdInSeconds() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService116)service).getCapVariableBatteryCriticallyLowThresholdInSeconds();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < v116 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.16");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public boolean getCapVariableBatteryLowThresholdInSeconds() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService116)service).getCapVariableBatteryLowThresholdInSeconds();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < v116 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.16");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public int getChargeTime() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService116)service).getChargeTime();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < v116 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.16");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public boolean getCapBatteryCapacityRemaining() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService19)service).getCapBatteryCapacityRemaining();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < deviceVersion19 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.9");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public boolean getCapCompareFirmwareVersion() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService19)service).getCapCompareFirmwareVersion();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < deviceVersion19 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.9");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public boolean getCapRestartPOS() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService19)service).getCapRestartPOS();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < deviceVersion19 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.9");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public boolean getCapStandbyPOS() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService19)service).getCapStandbyPOS();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < deviceVersion19 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.9");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public boolean getCapSuspendPOS() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService19)service).getCapSuspendPOS();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < deviceVersion19 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.9");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public boolean getCapUpdateFirmware() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService19)service).getCapUpdateFirmware();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < deviceVersion19 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.9");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public boolean getCapVariableBatteryCriticallyLowThreshold() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService19)service).getCapVariableBatteryCriticallyLowThreshold();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < deviceVersion19 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.9");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public boolean getCapVariableBatteryLowThreshold() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService19)service).getCapVariableBatteryLowThreshold();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < deviceVersion19 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.9");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public int getBatteryCapacityRemaining() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService19)service).getBatteryCapacityRemaining();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < deviceVersion19 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.9");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public int getBatteryCriticallyLowThreshold() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService19)service).getBatteryCriticallyLowThreshold();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < deviceVersion19 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.9");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public void setBatteryCriticallyLowThreshold(int i) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((POSPowerService19)service).setBatteryCriticallyLowThreshold(i);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < deviceVersion19 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.9");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public int getBatteryLowThreshold() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService19)service).getBatteryLowThreshold();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < deviceVersion19 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.9");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public void setBatteryLowThreshold(int i) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((POSPowerService19)service).setBatteryLowThreshold(i);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < deviceVersion19 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.9");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public int getPowerSource() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService19)service).getPowerSource();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < deviceVersion19 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.9");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public void compareFirmwareVersion(String s, int[] ints) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((POSPowerService19)service).compareFirmwareVersion(s, ints);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < deviceVersion19 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.9");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public void restartPOS() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((POSPowerService19)service).restartPOS();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < deviceVersion19 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.9");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public void standbyPOS(int i) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((POSPowerService19)service).standbyPOS(i);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < deviceVersion19 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.9");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public void suspendPOS(int i) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((POSPowerService19)service).suspendPOS(i);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < deviceVersion19 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.9");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public void updateFirmware(String s) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((POSPowerService19)service).updateFirmware(s);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < deviceVersion19 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.9");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public boolean getCapStatisticsReporting() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService19)service).getCapStatisticsReporting();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < deviceVersion19 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.9");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public boolean getCapUpdateStatistics() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService19)service).getCapUpdateStatistics();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < deviceVersion19 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.9");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public void resetStatistics(String s) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((POSPowerService19)service).resetStatistics(s);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < deviceVersion19 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.9");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public void retrieveStatistics(String[] strings) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((POSPowerService19)service).retrieveStatistics(strings);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < deviceVersion19 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.9");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public void updateStatistics(String s) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((POSPowerService19)service).updateStatistics(s);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            if (serviceVersion < deviceVersion19 )
                throw new JposException(JPOS_E_NOSERVICE, "Service does not support version 1.9");
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public boolean getCapFanAlarm() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService15)service).getCapFanAlarm();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public boolean getCapHeatAlarm() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService15)service).getCapHeatAlarm();
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
            return ((POSPowerService15)service).getCapPowerReporting();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public boolean getCapQuickCharge() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService15)service).getCapQuickCharge();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public boolean getCapShutdownPOS() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService15)service).getCapShutdownPOS();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public int getCapUPSChargeState() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService15)service).getCapUPSChargeState();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public int getEnforcedShutdownDelayTime() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService15)service).getEnforcedShutdownDelayTime();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public void setEnforcedShutdownDelayTime(int i) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((POSPowerService15)service).setEnforcedShutdownDelayTime(i);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public int getPowerFailDelayTime() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService15)service).getPowerFailDelayTime();
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
            return ((POSPowerService15)service).getPowerNotify();
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
            ((POSPowerService15)service).setPowerNotify(i);
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
            return ((POSPowerService15)service).getPowerState();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public boolean getQuickChargeMode() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService15)service).getQuickChargeMode();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public int getQuickChargeTime() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService15)service).getQuickChargeTime();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public int getUPSChargeState() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((POSPowerService15)service).getUPSChargeState();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }

    @Override
    public void shutdownPOS() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((POSPowerService15)service).shutdownPOS();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service");
        }
    }
}
