/*
 * Copyright 2024 Martin Conrad
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

package SampleDummyDevice;

import de.gmxhome.conrad.jpos.jpos_base.JposDevice;
import de.gmxhome.conrad.jpos.jpos_base.JposOutputRequest;
import de.gmxhome.conrad.jpos.jpos_base.gate.GateStatusUpdateEvent;
import de.gmxhome.conrad.jpos.jpos_base.gesturecontrol.*;
import jpos.JposException;
import jpos.config.JposEntry;

import static jpos.GestureControlConst.GCTL_SUE_START_MOTION;
import static jpos.JposConst.JPOS_E_NOSERVICE;
import static jpos.JposConst.JPOS_PR_NONE;

/**
 * JposDevice based dummy implementation for JavaPOS GestureControl device service implementation.
 * No real hardware. No real method implementations. Only for implementation demonstration purposes.<br>
 * Methods SetPosition and SetSpeed use only the time parameter as a timeout for method processinf.<br>
 * Methods StartMotion and StartPose use only the jpos.xml parameter MethodTimeout (default: 5) as a method timeout in
 * seconds.<br>
 * Supported configuration values for GestureControl in jpos.xml can be used to set the corresponding property values:
 * <ul>
 *     <li>MethodTimeout: Timeout for methods StartPose and StartMotion in seconds, type must be Integer. Default: 5.<br>
 * </ul>
 * <b>Remark:</b> This sample does not support usage of HardTotals devices. Check the sample implementations of the
 * SampleVideoAudioDevice implementation.
 */
public class GestureControlDevice extends JposDevice {
    /**
     * The device implementation. See parent for further details.
     * @param id  Device ID, not used by implementation.
     */
    protected GestureControlDevice(String id) {
        super(id);
        gestureControlInit(1);
        PhysicalDeviceDescription = "Dummy GestureControl simulator";
        PhysicalDeviceName = "Dummy GestureControl Simulator";
        CapPowerReporting = JPOS_PR_NONE;
    }

    @Override
    public void changeDefaults(GestureControlProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "Gesture control service for sample dummy device";
        // Set your defaults here
        props.CapMotion = props.CapMotionCreation = props.CapPose = props.CapPoseCreation = true;
        props.AutoModeList = "Auto";
        // Real implementations should initialize JointList, MotionList and PoseList here as well.
    }
    @Override
    public GestureControlProperties getGestureControlProperties(int index) {
        return new SampleProperties();
    }

    private class SampleProperties extends GestureControlProperties {
        private Integer MethodTimeout = 5;

        protected SampleProperties() {
            super(0);
        }

        @Override
        public void checkProperties(JposEntry entries) throws JposException {
            if (entries.getPropertyValue("MethodTimeout") instanceof Integer timeout) {
                check(timeout <= 0, JPOS_E_NOSERVICE, "MethodTimeout must be > 0");
                MethodTimeout = timeout;
            }
        }

        @Override
        public void createMotion(String fileName, String poseList) throws JposException {
            // First fileName and poseList must be checked for correctness because of poor service checks:
            // Here instructions to fill the motion file should be added.
            super.createMotion(fileName, poseList);
        }

        @Override
        public void createPose(String fileName, int time) throws JposException {
            // fileName must be checked for validity, current pose - as set by method SetPosition -
            // must be stored within the file.
            super.createPose(fileName, time);
        }

        @Override
        public JposOutputRequest setPosition(String positionList, int time, boolean absolute) throws JposException {
            // positionList should be checked for correctness here.
            return super.setPosition(positionList, time, absolute);
        }

        @Override
        public void setPosition(SetPosition request) throws JposException {
            // No real processing, waiting only request.Time seconds or until stop
            request.Waiting.suspend((request.Time > 0 ? request.Time : MethodTimeout) * 1000);
        }

        @Override
        public JposOutputRequest setSpeed(String speedList, int time) throws JposException {
            // speedList should be checked for correctness here
            return new SetSpeed(this, speedList, time);
        }

        @Override
        public void setSpeed(SetSpeed request) throws JposException {
            // No real processing, waiting only request.Time seconds or until stop
            request.Waiting.suspend((request.Time > 0 ? request.Time : MethodTimeout) * 1000);
        }

        @Override
        public JposOutputRequest startMotion(String fileName) throws JposException {
            // File should be checked for presence and - if possible - whether it is really a motion file
            return new StartMotion(this, fileName);
        }

        @Override
        public void startMotion(StartMotion request) throws JposException {
            handleEvent(new GestureControlStatusUpdateEvent(EventSource, GCTL_SUE_START_MOTION));
            try {
                // No real processing, waiting only MethodTimeout seconds or until stop
                request.Waiting.suspend(MethodTimeout * 1000);
            } finally {
                handleEvent(new GestureControlStatusUpdateEvent(EventSource, GCTL_SUE_START_MOTION));
            }
        }

        @Override
        public JposOutputRequest startPose(String fileName) throws JposException {
            // File should be checked for presence and - if possible - whether it is really a pose file
            return new StartPose(this, fileName);
        }

        @Override
        public void startPose(StartPose request) throws JposException {
            // No real processing, waiting only MethodTimeout seconds or until stop
            request.Waiting.suspend(MethodTimeout * 1000);
        }

        @Override
        public void getPosition(String jointID, int[] position) throws JposException {
            // Get the position of what has been specified by jointID and store it in position[0]. In this dummy: Always 1
            position[0] = 1;
        }

        @Override
        public void stopControl(JposOutputRequest request, boolean abort) {
            // The request is the one that shall be stopped
            if (abort)
                request.abortCommand(true);
        }
    }
}
