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

public class GestureControl extends BaseJposControl implements JposConst, GestureControlControl116 {
    protected Vector directIOListeners;
    protected Vector statusUpdateListeners;
    protected Vector errorListeners;
    protected Vector outputCompleteListeners;
    public GestureControl() {
        deviceControlDescription = "JavaPOS GestureControl Dummy Control";
        deviceControlVersion = deviceVersion115 + 1000;
        directIOListeners = new Vector();
        statusUpdateListeners = new Vector();
        errorListeners = new Vector();
        outputCompleteListeners = new Vector();
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
            synchronized (errorListeners) {
                for (Object listener : errorListeners)
                    ((ErrorListener) listener).errorOccurred(errorEvent);
            }
        }

        @Override
        public void fireOutputCompleteEvent(OutputCompleteEvent outputCompleteEvent) {
            synchronized (outputCompleteListeners) {
                for (Object listener : outputCompleteListeners)
                    ((OutputCompleteListener) listener).outputCompleteOccurred(outputCompleteEvent);
            }
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
            return GestureControl.this;
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
                service = (GestureControlService116) baseService; version++;
            } catch (Exception e) {
                if (i >= version * 1000 + 1000000)
                    throw new JposException(JPOS_E_NOSERVICE, "GestureControlService1" + version + " not fully implemented", e);
            }
        }
    }

    @Override
    public boolean getCapCompareFirmwareVersion() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((GestureControlService116)service).getCapCompareFirmwareVersion();
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
            return ((GestureControlService116)service).getCapPowerReporting();
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
            return ((GestureControlService116)service).getCapStatisticsReporting();
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
            return ((GestureControlService116)service).getCapUpdateFirmware();
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
            return ((GestureControlService116)service).getCapUpdateStatistics();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public int getOutputID() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((GestureControlService116)service).getOutputID();
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
            return ((GestureControlService116)service).getPowerNotify();
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
            ((GestureControlService116)service).setPowerNotify(var1);
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
            return ((GestureControlService116)service).getPowerState();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void clearOutput() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((GestureControlService116)service).clearOutput();
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
            ((GestureControlService116)service).compareFirmwareVersion(var1, var2);
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
            ((GestureControlService116)service).resetStatistics(var1);
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
            ((GestureControlService116)service).retrieveStatistics(var1);
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
            ((GestureControlService116)service).updateFirmware(var1);
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
            ((GestureControlService116)service).updateStatistics(var1);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public String getAutoMode() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((GestureControlService116)service).getAutoMode();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void setAutoMode(String var1) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((GestureControlService116)service).setAutoMode(var1);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public String getAutoModeList() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((GestureControlService116)service).getAutoModeList();
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
            return ((GestureControlService116)service).getCapAssociatedHardTotalsDevice();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public boolean getCapMotion() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((GestureControlService116)service).getCapMotion();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public boolean getCapMotionCreation() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((GestureControlService116)service).getCapMotionCreation();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public boolean getCapPose() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((GestureControlService116)service).getCapPose();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public boolean getCapPoseCreation() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((GestureControlService116)service).getCapPoseCreation();
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
            return ((GestureControlService116)service).getCapStorage();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public String getJointList() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((GestureControlService116)service).getJointList();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public String getMotionList() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((GestureControlService116)service).getMotionList();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public boolean getPoseCreationMode() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((GestureControlService116)service).getPoseCreationMode();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void setPoseCreationMode(boolean var1) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((GestureControlService116)service).setPoseCreationMode(var1);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public String getPoseList() throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            return ((GestureControlService116)service).getPoseList();
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
            return ((GestureControlService116)service).getStorage();
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void createMotion(String fileName, String poseList) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((GestureControlService116)service).createMotion(fileName, poseList);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void createPose(String fileName, int time) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((GestureControlService116)service).createPose(fileName, time);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void getPosition(String jointID, int[] position) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((GestureControlService116)service).getPosition(jointID, position);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void setPosition(String positionList, int time, boolean absolute) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((GestureControlService116)service).setPosition(positionList, time, absolute);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void setSpeed(String speedList, int time) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((GestureControlService116)service).setSpeed(speedList, time);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void startMotion(String fileName) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((GestureControlService116)service).startMotion(fileName);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void startPose(String fileName) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((GestureControlService116)service).startPose(fileName);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
        }
    }

    @Override
    public void stopControl(int outputID) throws JposException {
        try {
            if (!bOpen)
                throw new JposException(JPOS_E_CLOSED, "Control not open");
            ((GestureControlService116)service).stopControl(outputID);
        } catch (JposException e) {
            throw e;
        } catch(Exception e) {
            throw new JposException(JPOS_E_FAILURE, "Unhandled exception from service", e);
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
    public void addOutputCompleteListener(OutputCompleteListener l) {
        synchronized (outputCompleteListeners) {
            outputCompleteListeners.addElement(l);
        }
    }

    @Override
    public void removeOutputCompleteListener(OutputCompleteListener l) {
        synchronized (outputCompleteListeners) {
            outputCompleteListeners.removeElement(l);
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
