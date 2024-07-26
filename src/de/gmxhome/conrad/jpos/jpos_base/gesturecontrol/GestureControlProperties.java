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

package de.gmxhome.conrad.jpos.jpos_base.gesturecontrol;

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import de.gmxhome.conrad.jpos.jpos_base.JposOutputRequest;
import jpos.JposException;

import static jpos.GestureControlConst.*;

/**
 * Class containing the gesture control specific properties, their default values and default implementations of
 * GestureControlInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Gesture Control.
 */
public class GestureControlProperties extends JposCommonProperties implements GestureControlInterface {
    /**
     * UPOS property AutoMode. Default: An empty string. Must not be overwritten by objects derived from JposDevice within
     * the changeDefaults method.
     */
    public String AutoMode = "";

    /**
     * UPOS property AutoModeList. Default: An empty string. Can be overwritten by
     * objects derived from JposDevice within the changeDefaults method.
     */
    public String AutoModeList = "";

    /**
     * UPOS property CapAssociatedHardTotalsDevice. Default: An empty string. Must be overwritten by
     * objects derived from JposDevice within the changeDefaults method if CapStorage is not GCTL_CST_HOST_ONLY.
     */
    public String CapAssociatedHardTotalsDevice = "";

    /**
     * UPOS property CapMotion. Default: false. Can be overwritten by
     * objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapMotion = false;

    /**
     * UPOS property CapMotionCreation. Default: false. Can be overwritten by
     * objects derived from JposDevice within the changeDefaults method if CapMotion is true.
     */
    public boolean CapMotionCreation = false;

    /**
     * UPOS property CapPose. Default: false. Can be overwritten by
     * objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapPose = false;

    /**
     * UPOS property CapPoseCreation. Default: false. Can be overwritten by
     * objects derived from JposDevice within the changeDefaults method if CapPose is true.
     */
    public boolean CapPoseCreation = false;

    /**
     * UPOS property CapStorage. Default: GCTL_CST_HOST_ONLY. Can be overwritten by
     * objects derived from JposDevice within the changeDefaults method. If not GCTL_CST_HOST_ONLY,
     * CapAssociatedHardTotalsDevice must be overwritten with the HardTotals device name.
     */
    public int CapStorage = GCTL_CST_HOST_ONLY;

    /**
     * UPOS property JointList. Default: An empty string. Can be overwritten by
     * objects derived from JposDevice within the changeDefaults method.
     */
    public String JointList = "";

    /**
     * UPOS property MotionList. Default: An empty string. Coa be overwritten by
     * objects derived from JposDevice within the changeDefaults method.
     */
    public String MotionList = "";

    /**
     * UPOS property PoseCreationMode. Default: false. Will be set to false during first enable of the device.
     */
    public boolean PoseCreationMode = false;

    /**
     * UPOS property PoseList. Default: An empty string. Can be overwritten by
     * objects derived from JposDevice within the changeDefaults method.
     */
    public String PoseList = "";

    /**
     * UPOS property Storage. Default: GCTL_ST_HOST. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method. If changed, CapAssociatedHardTotalsDevice must be set to the name of the HardTotals device
     * used by the GestureControl service.
     */
    public int Storage = GCTL_ST_HOST;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected GestureControlProperties(int dev) {
        super(dev);
    }

    @Override
    public void initOnOpen() {
        super.initOnOpen();
        AutoMode = "";
        Storage = switch (CapStorage) {
            case GCTL_CST_HARDTOTALS_ONLY -> GCTL_CST_ALL;
            case GCTL_CST_HOST_ONLY -> GCTL_ST_HOST;
            default -> GCTL_ST_HOST_HARDTOTALS;
        };
    }
    @Override
    public boolean initOnFirstEnable() {
        if (super.initOnFirstEnable()) {
            PoseCreationMode = false;
            return true;
        }
        return false;
    }

    @Override
    public void autoMode(String mode) throws JposException {
        AutoMode = mode;
    }

    @Override
    public void poseCreationMode(boolean mode) throws JposException {
        PoseCreationMode = mode;
    }

    @Override
    public void storage(int storage) throws JposException {
        Storage = storage;
    }

    @Override
    public void createMotion(String fileName, String poseList) throws JposException {
    }

    @Override
    public void createPose(String fileName, int time) throws JposException {
    }

    @Override
    public void getPosition(String jointID, int[] position) throws JposException {
    }

    @Override
    public JposOutputRequest setPosition(String positionList, int time, boolean absolute) throws JposException {
        return new SetPosition(this, positionList, time, absolute);
    }

    @Override
    public void setPosition(SetPosition request) throws JposException {
    }

    @Override
    public JposOutputRequest setSpeed(String speedList, int time) throws JposException {
        return new SetSpeed(this, speedList, time);
    }

    @Override
    public void setSpeed(SetSpeed request) throws JposException {
    }

    @Override
    public JposOutputRequest startMotion(String fileName) throws JposException {
        return new StartMotion(this, fileName);
    }

    @Override
    public void startMotion(StartMotion request) throws JposException {
    }

    @Override
    public JposOutputRequest startPose(String fileName) throws JposException {
        return new StartPose(this, fileName);
    }

    @Override
    public void startPose(StartPose request) throws JposException {
    }

    @Override
    public void stopControl(JposOutputRequest request, boolean abort) {
    }
}
