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

/**
 * GestureControl service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class GestureControlService extends JposBase implements GestureControlService116 {
    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public GestureControlService(JposCommonProperties props, JposDevice device) {
        super(props, device);
    }

    /**
     * Instance of a class implementing the GestureControlInterface for gesture control specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public GestureControlInterface GestureControl;

    @Override
    public String getAutoMode() throws JposException {
        return null;
    }

    @Override
    public void setAutoMode(String s) throws JposException {

    }

    @Override
    public String getAutoModeList() throws JposException {
        return null;
    }

    @Override
    public String getCapAssociatedHardTotalsDevice() throws JposException {
        return null;
    }

    @Override
    public boolean getCapMotion() throws JposException {
        return false;
    }

    @Override
    public boolean getCapMotionCreation() throws JposException {
        return false;
    }

    @Override
    public boolean getCapPose() throws JposException {
        return false;
    }

    @Override
    public boolean getCapPoseCreation() throws JposException {
        return false;
    }

    @Override
    public int getCapStorage() throws JposException {
        return 0;
    }

    @Override
    public String getJointList() throws JposException {
        return null;
    }

    @Override
    public String getMotionList() throws JposException {
        return null;
    }

    @Override
    public boolean getPoseCreationMode() throws JposException {
        return false;
    }

    @Override
    public void setPoseCreationMode(boolean b) throws JposException {

    }

    @Override
    public String getPoseList() throws JposException {
        return null;
    }

    @Override
    public int getStorage() throws JposException {
        return 0;
    }

    @Override
    public void setStorage(int i) throws JposException {

    }

    @Override
    public void createMotion(String s, String s1) throws JposException {

    }

    @Override
    public void createPose(String s, int i) throws JposException {

    }

    @Override
    public void getPosition(String s, int[] ints) throws JposException {

    }

    @Override
    public void setPosition(String s, int i, boolean b) throws JposException {

    }

    @Override
    public void setSpeed(String s, int i) throws JposException {

    }

    @Override
    public void startMotion(String s) throws JposException {

    }

    @Override
    public void startPose(String s) throws JposException {

    }

    @Override
    public void stopControl(int i) throws JposException {

    }
}
