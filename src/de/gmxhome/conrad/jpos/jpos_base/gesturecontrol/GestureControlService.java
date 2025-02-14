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

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.JposException;
import jpos.services.GestureControlService116;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static de.gmxhome.conrad.jpos.jpos_base.gesturecontrol.GestureControlProperties.*;
import static de.gmxhome.conrad.jpos.jpos_base.JposBaseDevice.member;
import static jpos.GestureControlConst.*;
import static jpos.JposConst.*;

/**
 * GestureControl service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class GestureControlService extends JposBase implements GestureControlService116 {
    private final GestureControlProperties Data;

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public GestureControlService(GestureControlProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    /**
     * Instance of a class implementing the GestureControlInterface for gesture control specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public GestureControlInterface GestureControl;

    @Override
    public String getAutoMode() throws JposException {
        checkEnabled();
        logGet("AutoMode");
        return Data.AutoMode;
    }

    @Override
    public void setAutoMode(String mode) throws JposException {
        logPreSet("AutoMode");
        if (mode == null)
            mode = "";
        checkEnabled();
        String[] allowed = Data.AutoModeList.split(",");
        check(mode.length() > 0 && !member(mode, allowed), JPOS_E_ILLEGAL, "Invalid mode: " + mode);
        GestureControl.autoMode(mode);
        logSet("AutoMode");
    }

    @Override
    public String getAutoModeList() throws JposException {
        checkOpened();
        logGet("AutoModeList");
        return Data.AutoModeList;
    }

    @Override
    public String getCapAssociatedHardTotalsDevice() throws JposException {
        checkOpened();
        logGet("CapAssociatedHardTotalsDevice");
        return Data.CapAssociatedHardTotalsDevice;
    }

    @Override
    public boolean getCapMotion() throws JposException {
        checkOpened();
        logGet("CapMotion");
        return Data.CapMotion;
    }

    @Override
    public boolean getCapMotionCreation() throws JposException {
        checkOpened();
        logGet("CapMotionCreation");
        return Data.CapMotionCreation;
    }

    @Override
    public boolean getCapPose() throws JposException {
        checkOpened();
        logGet("CapPose");
        return Data.CapPose;
    }

    @Override
    public boolean getCapPoseCreation() throws JposException {
        checkOpened();
        logGet("CapPoseCreation");
        return Data.CapPoseCreation;
    }

    @Override
    public int getCapStorage() throws JposException {
        checkOpened();
        logGet("CapStorage");
        return Data.CapStorage;
    }

    @Override
    public String getJointList() throws JposException {
        checkOpened();
        logGet("JointList");
        return Data.JointList;
    }

    @Override
    public String getMotionList() throws JposException {
        checkOpened();
        logGet("MotionList");
        return Data.MotionList;
    }

    @Override
    public boolean getPoseCreationMode() throws JposException {
        checkEnabled();
        logGet("PoseCreationMode");
        return Data.PoseCreationMode;
    }

    @Override
    public void setPoseCreationMode(boolean mode) throws JposException {
        logPreSet("PoseCreationMode");
        checkEnabled();
        check(!Data.CapPoseCreation && mode, JPOS_E_ILLEGAL, "Invalid mode: " + mode);
        GestureControl.poseCreationMode(mode);
        logSet("PoseCreationMode");
    }

    @Override
    public String getPoseList() throws JposException {
        checkOpened();
        logGet("PoseList");
        return Data.PoseList;
    }

    @Override
    public int getStorage() throws JposException {
        checkEnabled();
        logGet("CapAssociatedHardTotalsDevice");
        return Data.Storage;
    }

    @Override
    public void setStorage(int storage) throws JposException {
        logPreSet("Storage");
        checkEnabled();
        long[] valid = {GCTL_ST_HARDTOTALS, GCTL_ST_HOST, GCTL_ST_HOST_HARDTOTALS};
        boolean invalid = !member(storage, valid) ||
                Data.CapStorage == GCTL_CST_HARDTOTALS_ONLY && storage != GCTL_ST_HARDTOTALS ||
                Data.CapStorage == GCTL_CST_HOST_ONLY && storage != GCTL_ST_HOST;
        check(invalid, JPOS_E_ILLEGAL, "Invalid storage: " + storage);
        GestureControl.storage(storage);
        logSet("Storage");
    }

    List<Integer> OutputIDs = new ArrayList<>();

    @Override
    public void clearOutput() throws JposException {
        super.clearOutput();
        synchronized (Device.AsyncProcessorRunning) {
            OutputIDs.clear();
        }
    }

    private void checkFileName(String fileName) throws JposException {
        check(fileName == null, JPOS_E_ILLEGAL, "File name must not be null");
        if (Data.Storage != GCTL_ST_HOST) {
            byte[] fileAsBytes = fileName.getBytes(StandardCharsets.UTF_8);
            check(fileName.length() > 10, JPOS_E_ILLEGAL, "HardTotals file name too long (> 10");
            check(fileName.length() != fileAsBytes.length, JPOS_E_ILLEGAL, "Invalid character in fileName");
            for (byte b : fileAsBytes)
                check(b < ' ', JPOS_E_ILLEGAL, "Control character in fileName");
        } else
            check(fileName.length() == 0, JPOS_E_ILLEGAL, "File name must not be empty");
    }

    @Override
    public void createMotion(String fileName, String poseList) throws JposException {
        logPreCall("CreateMotion", removeOuterArraySpecifier(new Object[]{fileName, poseList}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapMotionCreation, JPOS_E_ILLEGAL, "Motion creation not supported");
        checkFileName(fileName);
        check(poseList == null || poseList.length() == 0, JPOS_E_ILLEGAL, "Empty pose list not supported");
        poseList = poseList.replaceAll("\\s", "");
        for (String pose : poseList.split(","))
            check(pose.length() == 0, JPOS_E_ILLEGAL, "Invalid pose list: " + poseList);
        GestureControl.createMotion(fileName, poseList);
        logCall("CreateMotion");
    }

    @Override
    public void createPose(String fileName, int time) throws JposException {
        logPreCall("CreatePose", removeOuterArraySpecifier(new Object[]{fileName, time}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapPoseCreation, JPOS_E_ILLEGAL, "Pose creation not supported");
        check(!Data.PoseCreationMode, JPOS_E_ILLEGAL, "Not in pose creation mode");
        checkFileName(fileName);
        check(time < 0 && time != JPOS_FOREVER, JPOS_E_ILLEGAL, "time must be a positive value or FOREVER");
        GestureControl.createPose(fileName, time);
        logCall("CreatePose");
    }

    @Override
    public void getPosition(String jointID, int[] position) throws JposException {
        logPreCall("GetPosition", removeOuterArraySpecifier(new Object[]{jointID, position}, Device.MaxArrayStringElements));
        checkEnabled();
        check(position == null || position.length != 1, JPOS_E_ILLEGAL, "position must be int[1]");
        check(jointID == null || jointID.equals(""), JPOS_E_ILLEGAL, "JointID empty");
        check(!Data.JointIDs.containsKey(jointID), JPOS_E_ILLEGAL, "Invalid jointID: " + jointID);
        check(!(Data.JointIDs.containsKey(jointID)), JPOS_E_ILLEGAL, "Invalid jointID: " + jointID);
        GestureControl.getPosition(jointID, position);
        logCall("GetPosition");
    }

    @Override
    public void setPosition(String positionList, int time, boolean absolute) throws JposException {
        logPreCall("SetPosition", removeOuterArraySpecifier(new Object[]{positionList, time, absolute}, Device.MaxArrayStringElements));
        checkEnabled();
        check(positionList == null || positionList.equals(""), JPOS_E_ILLEGAL, "Empty positionList");
        List<JointParameter> positions = new ArrayList<>();
        for (String position : positionList.replaceAll("\\s", "").split(",")) {
            String[] parts = position.split(":");
            check(parts.length != 2, JPOS_E_ILLEGAL, "Invalid position: " + position);
            check(!Data.JointIDs.containsKey(parts[0]), JPOS_E_ILLEGAL, "Invalid JointID: " + parts[0]);
            try {
                int value = Integer.parseInt(parts[1]);
                check(Data.JointIDs.get(parts[0]) && (value < -100 || 100 < value), JPOS_E_ILLEGAL, "Position out of range : " + position);
                positions.add(new JointParameter(parts[0], value));
            } catch (NumberFormatException e) {
                throw new JposException(JPOS_E_ILLEGAL, "Invalid");
            }
        }
        check(time < 0 && time != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid time: " + time);
        JposOutputRequest req;
        if ((req = GestureControl.setPosition(positions, time, absolute)) instanceof SetPosition)
            req.enqueue();
        OutputIDs.add(Data.OutputID);
        logAsyncCall("SetPosition");
    }

    @Override
    public void setSpeed(String speedList, int time) throws JposException {
        logPreCall("SetSpeed", removeOuterArraySpecifier(new Object[]{speedList, time}, Device.MaxArrayStringElements));
        checkEnabled();
        check(speedList == null || speedList.equals(""), JPOS_E_ILLEGAL, "Empty speedList");
        List<JointParameter> positions = new ArrayList<>();
        for(String entry : speedList.replaceAll("\\s", "").split(",")) {
            String[] parts = entry.split(":");
            check(parts.length != 2, JPOS_E_ILLEGAL, "Invalid speed entry: " + entry);
            check(!Data.JointIDs.containsKey(parts[0]), JPOS_E_ILLEGAL, "Invalid JointID: " + parts[0]);
            try {
                int value = Integer.parseInt(parts[1]);
                check(value < -100 || value > 100, JPOS_E_ILLEGAL, "Speed value out of range in " + entry);
                positions.add(new JointParameter(parts[0], value));
            } catch (NumberFormatException e) {
                throw new JposException(JPOS_E_ILLEGAL, "Invalid speed value in " + entry);
            }
        }
        check(time < 0 && time != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid time: " + time);
        JposOutputRequest req;
        if ((req = GestureControl.setSpeed(positions, time)) instanceof SetSpeed)
            req.enqueue();
        OutputIDs.add(Data.OutputID);
        logAsyncCall("SetSpeed");
    }

    @Override
    public void startMotion(String fileName) throws JposException {
        logPreCall("StartMotion", removeOuterArraySpecifier(new Object[]{fileName}, Device.MaxArrayStringElements));
        checkEnabled();
        check(fileName == null || fileName.equals(""), JPOS_E_ILLEGAL, "Filename must not be empty");
        JposOutputRequest req;
        if ((req = GestureControl.startMotion(fileName)) instanceof StartMotion)
            req.enqueue();
        OutputIDs.add(Data.OutputID);
        logAsyncCall("StartMotion");
    }

    @Override
    public void startPose(String fileName) throws JposException {
        logPreCall("StartPose", removeOuterArraySpecifier(new Object[]{fileName}, Device.MaxArrayStringElements));
        checkEnabled();
        check(fileName == null || fileName.equals(""), JPOS_E_ILLEGAL, "Filename must not be empty");
        JposOutputRequest req;
        if ((req = GestureControl.startPose(fileName)) instanceof StartPose)
            req.enqueue();
        OutputIDs.add(Data.OutputID);
        logAsyncCall("StartPose");
    }

    @Override
    public void stopControl(int outputID) throws JposException {
        logPreCall("StopControl", removeOuterArraySpecifier(new Object[]{outputID}, Device.MaxArrayStringElements));
        checkEnabled();
        synchronized (Device.AsyncProcessorRunning) {
            for (Integer id : OutputIDs) {
                if (id == outputID) {
                    GestureControl.stopControl(outputID);
                    OutputIDs.remove(id);
                    logCall("StopControl");
                    return;
                }
            }
            throw new JposException(JPOS_E_ILLEGAL, "Output request not running: " + outputID);
        }
    }

    /**
     * Checks whether property JointList matches the format as specified in UPOS specification:<ul>
     *     <li>Consists of entries separated by comma,</li>
     *     <li>Each entry starts with the JointID and a position range availability parameter of "0" or "1",
     *     separated by a doublepoint (:).</li>
     *     <li>Some examples for valid entries: <ul>
     *         <li>Joint01_Roll:1</li>
     *         <li>Wheel_Turn:0</li>
     *     </ul>.</li>
     *     <li>All JointIDs must be unique.</li>
     * </ul>
     * In addition, AutoModeList, MotionList and PoseList will be verified:<ul>
     *     <li>Consist of entries separated by comma,</li>
     *     <li>all entries must be unique.</li>
     * </ul>
     * Whitespace within AutoModeList, JointList, MotionList and PoseList will be removed.
     *
     * @param props Property containing the list properties to be verified.
     * @throws JposException, if one of the lists contain invalid entries.
     */
    public void validateListProperties(GestureControlProperties props) throws JposException {
        String[] entries = (props.MotionList = props.MotionList.replaceAll("\\s", "")).split(",");
        check(entries.length != new TreeSet<>(Arrays.asList(entries)).size(), JPOS_E_NOSERVICE, "Duplicate in MotionList: " + props.MotionList);
        entries = (props.AutoModeList = props.AutoModeList.replaceAll("\\s", "")).split(",");
        check(entries.length != new TreeSet<>(Arrays.asList(entries)).size(), JPOS_E_NOSERVICE, "Duplicate in AutoModeList: " + props.AutoModeList);
        entries = (props.PoseList = props.PoseList.replaceAll("\\s", "")).split(",");
        check(entries.length != new TreeSet<>(Arrays.asList(entries)).size(), JPOS_E_NOSERVICE, "Duplicate in PoseList: " + props.PoseList);
        if ((props.JointList = props.JointList.replaceAll("\\s", "")).length() > 0) {
            for (String entry : props.JointList.split(",")) {
                String[] parts = entry.split(":");
                check(parts.length != 2, JPOS_E_NOSERVICE, "Invalid entry in JointList: " + entry);
                check(Data.JointIDs.containsKey(parts[0]), JPOS_E_NOSERVICE, "Duplicate JointID in JointList: " + entry);
                boolean hasPositionRangeAvailability = parts[1].equals("1");
                check(!(parts[1].equals("0") || hasPositionRangeAvailability), JPOS_E_NOSERVICE,
                        "Invalid position range availability in JointID " + parts[0] + " of JointList: " + parts[1]);
                Data.JointIDs.put(parts[0], hasPositionRangeAvailability);
            }
        }
    }
}
