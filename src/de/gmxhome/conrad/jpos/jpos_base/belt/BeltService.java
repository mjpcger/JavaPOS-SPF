/*
 * Copyright 2020 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.belt;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.services.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.BeltConst.*;
import static jpos.JposConst.*;

/**
 * Belt service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class BeltService extends JposBase implements BeltService116 {
    /**
     * Instance of a class implementing the BeltInterface for belt specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public BeltInterface BeltInterface;

    private final BeltProperties Data;

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public BeltService(BeltProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public boolean getCapAutoStopBackward() throws JposException {
        checkOpened();
        logGet("CapAutoStopBackward");
        return Data.CapAutoStopBackward;
    }

    @Override
    public boolean getCapAutoStopBackwardItemCount() throws JposException {
        checkOpened();
        logGet("CapAutoStopBackwardItemCount");
        return Data.CapAutoStopBackwardItemCount;
    }

    @Override
    public boolean getCapAutoStopForward() throws JposException {
        checkOpened();
        logGet("CapAutoStopForward");
        return Data.CapAutoStopForward;
    }

    @Override
    public boolean getCapAutoStopForwardItemCount() throws JposException {
        checkOpened();
        logGet("CapAutoStopForwardItemCount");
        return Data.CapAutoStopForwardItemCount;
    }

    @Override
    public boolean getCapLightBarrierBackward() throws JposException {
        checkOpened();
        logGet("CapLightBarrierBackward");
        return Data.CapLightBarrierBackward;
    }

    @Override
    public boolean getCapLightBarrierForward() throws JposException {
        checkOpened();
        logGet("CapLightBarrierForward");
        return Data.CapLightBarrierForward;
    }

    @Override
    public boolean getCapMoveBackward() throws JposException {
        checkOpened();
        logGet("CapMoveBackward");
        return Data.CapMoveBackward;
    }

    @Override
    public boolean getCapRealTimeData() throws JposException {
        return false;
    }

    @Override
    public boolean getCapSecurityFlapBackward() throws JposException {
        checkOpened();
        logGet("CapSecurityFlapBackward");
        return Data.CapSecurityFlapBackward;
    }

    @Override
    public boolean getCapSecurityFlapForward() throws JposException {
        checkOpened();
        logGet("CapSecurityFlapForward");
        return Data.CapSecurityFlapForward;
    }

    @Override
    public int getCapSpeedStepsBackward() throws JposException {
        checkOpened();
        logGet("CapSpeedStepsBackward");
        return Data.CapSpeedStepsBackward;
    }

    @Override
    public int getCapSpeedStepsForward() throws JposException {
        checkOpened();
        logGet("CapSpeedStepsForward");
        return Data.CapSpeedStepsForward;
    }

    @Override
    public boolean getAutoStopBackward() throws JposException {
        checkOpened();
        logGet("AutoStopBackward");
        return Data.AutoStopBackward;
    }

    @Override
    public int getAutoStopBackwardDelayTime() throws JposException {
        checkOpened();
        logGet("AutoStopBackwardDelayTime");
        return Data.AutoStopBackwardDelayTime;
    }

    @Override
    public int getAutoStopBackwardItemCount() throws JposException {
        checkOpened();
        logGet("AutoStopBackwardItemCount");
        return Data.AutoStopBackwardItemCount;
    }

    @Override
    public boolean getAutoStopForward() throws JposException {
        checkOpened();
        logGet("AutoStopForward");
        return Data.AutoStopForward;
    }

    @Override
    public int getAutoStopForwardDelayTime() throws JposException {
        checkOpened();
        logGet("AutoStopForwardDelayTime");
        return Data.AutoStopForwardDelayTime;
    }

    @Override
    public int getAutoStopForwardItemCount() throws JposException {
        checkOpened();
        logGet("AutoStopForwardItemCount");
        return Data.AutoStopForwardItemCount;
    }

    @Override
    public boolean getLightBarrierBackwardInterrupted() throws JposException {
        checkEnabled();
        logGet("LightBarrierBackwardInterrupted");
        return Data.LightBarrierBackwardInterrupted;
    }

    @Override
    public boolean getLightBarrierForwardInterrupted() throws JposException {
        checkEnabled();
        logGet("LightBarrierForwardInterrupted");
        return Data.LightBarrierForwardInterrupted;
    }

    @Override
    public int getMotionStatus() throws JposException {
        checkEnabled();
        logGet("MotionStatus");
        return Data.MotionStatus;
    }

    @Override
    public boolean getSecurityFlapBackwardOpened() throws JposException {
        checkEnabled();
        logGet("SecurityFlapBackwardOpened");
        return Data.SecurityFlapBackwardOpened;
    }

    @Override
    public boolean getSecurityFlapForwardOpened() throws JposException {
        checkEnabled();
        logGet("SecurityFlapForwardOpened");
        return Data.SecurityFlapForwardOpened;
    }

    @Override
    public void setAutoStopBackward(boolean b) throws JposException {
        logPreSet("AutoStopBackward");
        checkOpened();
        check(!Data.CapAutoStopBackward && b, JPOS_E_ILLEGAL, "Device does not support AutoStopBackward");
        checkNoChangedOrClaimed(Data.AutoStopBackward, b);
        BeltInterface.autoStopBackward(b);
        logSet("AutoStopBackward");

    }

    @Override
    public void setAutoStopBackwardDelayTime(int i) throws JposException {
        logPreSet("AutoStopBackwardDelayTime");
        checkOpened();
        if (Data.CapAutoStopBackward) {
            check(i < 0 && i != JPOS_FOREVER, JPOS_E_ILLEGAL, "Device does not support negative AutoStopBackwardDelayTime");
            checkNoChangedOrClaimed(Data.AutoStopBackwardDelayTime, i);
            BeltInterface.autoStopBackwardDelayTime(i);
        }
        logSet("AutoStopBackwardDelayTime");
    }

    @Override
    public void setAutoStopForward(boolean b) throws JposException {
        logPreSet("AutoStopForward");
        checkOpened();
        check(!Data.CapAutoStopForward, JPOS_E_ILLEGAL, "Device does not support AutoStopForward");
        checkNoChangedOrClaimed(Data.AutoStopForward, b);
        BeltInterface.autoStopForward(b);
        logSet("AutoStopForward");
    }

    @Override
    public void setAutoStopForwardDelayTime(int i) throws JposException {
        logPreSet("AutoStopForwardDelayTime");
        checkOpened();
        if (Data.CapAutoStopForward) {
            check(i < 0 && i != JPOS_FOREVER, JPOS_E_ILLEGAL, "Device does not support negative AutoStopForwardDelayTime");
            checkNoChangedOrClaimed(Data.AutoStopForwardDelayTime, i);
            BeltInterface.autoStopForwardDelayTime(i);
        }
        logSet("AutoStopForwardDelayTime");
    }

    @Override
    public void adjustItemCount(int i, int i1) throws JposException {
        logPreCall("AdjustItemCount", removeOuterArraySpecifier(new Object[]{i, i1}, Device.MaxArrayStringElements));
        checkEnabled();
        checkMember(i, new long[]{BELT_AIC_BACKWARD, BELT_AIC_FORWARD}, JPOS_E_ILLEGAL, "Invalid direction: " + i);
        check(!Data.CapAutoStopBackwardItemCount && i == BELT_AIC_BACKWARD, JPOS_E_ILLEGAL, "Unsupported direction: " + i);
        check(!Data.CapAutoStopForwardItemCount && i == BELT_AIC_FORWARD, JPOS_E_ILLEGAL, "Unsupported direction: " + i);
        BeltInterface.adjustItemCount(i, i1);
        logCall("adjustItemCount");
    }

    @Override
    public void moveBackward(int i) throws JposException {
        logPreCall("MoveBackward", removeOuterArraySpecifier(new Object[]{i}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapMoveBackward, JPOS_E_ILLEGAL, "Not supported");
        check(i <= 0 || i > Data.CapSpeedStepsBackward, JPOS_E_ILLEGAL, "Speed out of range: " + i);
        BeltInterface.moveBackward(i);
        logCall("MoveBackward");
    }

    @Override
    public void moveForward(int i) throws JposException {
        logPreCall("MoveForward", removeOuterArraySpecifier(new Object[]{i}, Device.MaxArrayStringElements));
        checkEnabled();
        check(i <= 0 || i > Data.CapSpeedStepsForward, JPOS_E_ILLEGAL, "Speed out of range: " + i);
        BeltInterface.moveForward(i);
        logCall("MoveForward");
    }

    @Override
    public void resetBelt() throws JposException {
        logPreCall("ResetBelt");
        checkEnabled();
        BeltInterface.resetBelt();
        logCall("ResetBelt");
    }

    @Override
    public void resetItemCount(int i) throws JposException {
        logPreCall("ResetItemCount", removeOuterArraySpecifier(new Object[]{i}, Device.MaxArrayStringElements));
        checkEnabled();
        checkMember(i, new long[]{BELT_RIC_BACKWARD, BELT_RIC_FORWARD}, JPOS_E_ILLEGAL, "Invalid direction: " + i);
        check(!Data.CapAutoStopBackwardItemCount && i == BELT_RIC_BACKWARD, JPOS_E_ILLEGAL, "Unsupported direction: " + i);
        check(!Data.CapAutoStopForwardItemCount && i == BELT_RIC_FORWARD, JPOS_E_ILLEGAL, "Unsupported direction: " + i);
        BeltInterface.resetItemCount(i);
        logCall("ResetItemCount");
    }

    @Override
    public void stopBelt() throws JposException {
        logPreCall("StopBelt");
        checkEnabled();
        BeltInterface.stopBelt();
        logCall("StopBelt");
    }
}
