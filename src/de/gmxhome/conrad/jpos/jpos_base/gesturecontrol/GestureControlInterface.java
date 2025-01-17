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

import de.gmxhome.conrad.jpos.jpos_base.JposBaseInterface;
import de.gmxhome.conrad.jpos.jpos_base.JposOutputRequest;
import jpos.JposException;

import java.util.List;
import static de.gmxhome.conrad.jpos.jpos_base.gesturecontrol.GestureControlProperties.*;

/**
 * Interface for methods that implement property setter and method calls for the GestureControl device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Gesture Control.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.<br>
 * Reduced plausibility checks, most parts of validation must be made within specific device implementations. However,
 * the automatism for logging, event handling and asynchronous processing can be used as usual.
 */
public interface GestureControlInterface extends JposBaseInterface {
    /**
     * Final part of setAutoMode method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>mode is one of the modes listed in AutoModeList.</li>
     * </ul>
     * @param mode New value for AutoMode.
     * @throws JposException If an error occurs.
     */
    void autoMode(String mode) throws JposException;

    /**
     * Final part of setPoseCreationMode method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapPoseCreation is true.</li>
     * </ul>
     * @param mode New value for PoseCreationMode.
     * @throws JposException If an error occurs.
     */
    void poseCreationMode(boolean mode) throws JposException;

    /**
     * Final part of setStorage method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>storage is one of ST_HARDTOTALS, ST_HOST or ST_HOST_HARDTOTALS,</li>
     *     <li>if CapStorage is CST_HARDTOTALS_ONLY, storage is ST_HARDTOTALS,</li>
     *     <li>if CapStorage is CST_HOST_ONLY, storage is ST_HOST.</li>
     * </ul>
     * @param storage new Storage value.
     * @throws JposException If an error occurs.
     */
    void storage(int storage) throws JposException;

    /**
     * final part of CreateMotion method. Can be overwritten within derived
     * classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapMotionCreation is true,</li>
     *     <li>fileName is not null,</li>
     *     <li>If Storage is not ST_HARDTOTALS, fileName is not empty,</li>
     *     <li>Otherwise, fileName matches the requirements for fileName as specified for HardTotals devices (&lt; 10
     *     characters in range 0x20 - 0x7f, empty if CapSingleFile of HardTotals device is true),</li>
     *     <li>poseList is neither null nor empty,</li>
     *     <li>poseList does not contain any whitespace character,</li>
     *     <li>All elements specified in poseList are not empty.</li>
     * </ul>
     *
     * @param fileName Name of motion file to be created.
     * @param poseList Comma separated list of pose information.
     * @throws JposException    If an error occurs.
     */
    void createMotion(String fileName, String poseList) throws JposException;

    /**
     * final part of CreatePose method. Can be overwritten within derived
     * classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapMotionCreation is true,</li>
     *     <li>PoseCreationMode is true,</li>
     *     <li>fileName is not null,</li>
     *     <li>If Storage is not ST_HARDTOTALS, fileName is not empty,</li>
     *     <li>Otherwise, fileName matches the requirements for fileName as specified for HardTotals devices (&lt; 10
     *     characters in range 0x20 - 0x7f, empty if CapSingleFile of HardTotals device is true),</li>
     *     <li>time is a positive value or FOREVER.</li>
     * </ul>
     *
     * @param fileName Name of pose file to be created.
     * @param time     Time to reach the pose position in seconds.
     * @throws JposException    If an error occurs.
     */
    void createPose(String fileName, int time) throws JposException;

    /**
     * final part of GetPosition method. Can be overwritten within derived
     * classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>jointID is present in JointList,</li>
     *     <li>Dimension of position is 1.</li>
     * </ul>
     *
     * @param jointID jointID values as specified in property JointList.
     * @param position Target for position associated with jointID.
     * @throws JposException    If an error occurs.
     */
    void getPosition(String jointID, int[] position) throws JposException;

    /**
     * Validation part of SetPosition method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>positionList neither empty nor null,</li>
     *     <li>whitespace characters have been removed from positionList,</li>
     *     <li>positionList contains only JointIDs present in JointList,</li>
     *     <li>position values are integer values between -100 and 100 for jointIDs with position range availability
     *     value 1, </li>
     *     <li>time is a positive value or FOREVER.</li>
     * </ul>
     *
     * @param positionList List of Position information from comma-separated list. See GestureControlProperties.JointParameter
     *                     for detail.
     * @param time         Specify time to complete operation in seconds. See UPOS specification for details.
     * @param absolute     Specifies whether position is absolute or relative. See UPOS specification for details.
     * @return                  SetPosition object for use in final part.
     * @throws JposException    If an error occurs.
     */
    JposOutputRequest setPosition(List<JointParameter> positionList, int time, boolean absolute) throws JposException;

    /**
     * Final part of SetPosition method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a SetPosition object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method to be used by SetPosition.
     * @throws JposException    If an error occurs.
     */
    void setPosition(SetPosition request) throws JposException;

    /**
     * Validation part of SetSpeed method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>speedList is not empty,</li>
     *     <li>time is a positive value or FOREVER.</li>
     * </ul>
     *
     * @param speedList List of speed information from comma separated list. See GestureControlProperties.JointParameter
     *                  for details.
     * @param time      Time to control device in second. See UPOS 1.16 specification for additional information.
     * @return                  SetSpeed object for use in final part.
     * @throws JposException    If an error occurs.
     */
    JposOutputRequest setSpeed(List<JointParameter> speedList, int time) throws JposException;

    /**
     * Final part of SetSpeed method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a SetSpeed object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method to be used by SetSpeed.
     * @throws JposException    If an error occurs.
     */
    void setSpeed(SetSpeed request) throws JposException;

    /**
     * Validation part of StartMotion method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>fileName is neither null,nor empty.</li>
     * </ul>
     *
     * @param fileName Name of motion file or motion ID present in MotionList property.
     * @return                  StartMotion object for use in final part.
     * @throws JposException    If an error occurs.
     */
    JposOutputRequest startMotion(String fileName) throws JposException;

    /**
     * Final part of StartMotion method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a StartMotion object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method to be used by StartMotion.
     * @throws JposException    If an error occurs.
     */
    void startMotion(StartMotion request) throws JposException;

    /**
     * Validation part of StartPose method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>fileName is neither null,nor empty.</li>
     * </ul>
     *
     * @param fileName Name of pose file or pose ID present in PoseList property.
     * @return                  StartPose object for use in final part.
     * @throws JposException    If an error occurs.
     */
    JposOutputRequest startPose(String fileName) throws JposException;

    /**
     * Final part of StartPose method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a StartPose object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method to be used by StartPose.
     * @throws JposException    If an error occurs.
     */
    void startPose(StartPose request) throws JposException;

    /**
     * final part of StopControl method. Can be overwritten within derived
     * classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>outputID has been previously assigned to an uncompleted asynchronous operation.</li>
     * </ul>
     *
     * @param outputID OutputID of the method call to be terminated.
     * @throws JposException    If an error occurs.
     */
    void stopControl(int outputID) throws JposException;
}
