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

import de.gmxhome.conrad.jpos.jpos_base.JposBase;
import de.gmxhome.conrad.jpos.jpos_base.JposStatusUpdateEvent;
import jpos.BeltConst;

import java.util.Arrays;

/**
 * Status update event implementation for Belt devices.
 */
public class BeltStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Constructor, Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (belt.)BeltService object.
     * @param state  Status, see UPOS specification, chapter Belt - Events - StatusUpdateEvent.
     */
    public BeltStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
    }

    @Override
    public JposStatusUpdateEvent copyEvent(JposBase o) {
        return new BeltStatusUpdateEvent(o, getStatus());
    }

    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        BeltProperties props = (BeltProperties)getPropertySet();
        switch (getStatus()) {
            case BeltConst.BELT_SUE_AUTO_STOP:
            case BeltConst.BELT_SUE_EMERGENCY_STOP:
            case BeltConst.BELT_SUE_SAFETY_STOP:
            case BeltConst.BELT_SUE_TIMEOUT_STOP:
                props.MotionStatus = BeltConst.BELT_MT_STOPPED;
                return true;
            case BeltConst.BELT_SUE_MOTOR_FUSE_DEFECT:
                props.MotionStatus = BeltConst.BELT_MT_MOTOR_FAULT;
                return true;
            case BeltConst.BELT_SUE_MOTOR_OVERHEATING:
                props.MotionStatus = BeltConst.BELT_MT_MOTOR_FAULT;
                return true;
            case BeltConst.BELT_SUE_LIGHT_BARRIER_BACKWARD_INTERRUPTED:
                props.LightBarrierBackwardInterrupted = true;
                return true;
            case BeltConst.BELT_SUE_LIGHT_BARRIER_BACKWARD_OK:
                props.LightBarrierBackwardInterrupted = false;
                return true;
            case BeltConst.BELT_SUE_LIGHT_BARRIER_FORWARD_INTERRUPTED:
                props.LightBarrierForwardInterrupted = true;
                return true;
            case BeltConst.BELT_SUE_LIGHT_BARRIER_FORWARD_OK:
                props.LightBarrierForwardInterrupted = false;
                return true;
            case BeltConst.BELT_SUE_SECURITY_FLAP_BACKWARD_CLOSED:
                props.SecurityFlapBackwardOpened = false;
                return true;
            case BeltConst.BELT_SUE_SECURITY_FLAP_BACKWARD_OPENED:
                props.SecurityFlapBackwardOpened = true;
                return true;
            case BeltConst.BELT_SUE_SECURITY_FLAP_FORWARD_CLOSED:
                props.SecurityFlapForwardOpened = false;
                return true;
            case BeltConst.BELT_SUE_SECURITY_FLAP_FORWARD_OPENED:
                props.SecurityFlapForwardOpened = true;
                return true;
        }
        return false;
    }

    @Override
    public boolean checkStatusCorresponds() {
        if (super.checkStatusCorresponds())
            return true;
        BeltProperties props = (BeltProperties)getPropertySet();
        switch (getStatus()) {
            case BeltConst.BELT_SUE_AUTO_STOP:
            case BeltConst.BELT_SUE_SAFETY_STOP:
            case BeltConst.BELT_SUE_TIMEOUT_STOP:
                return props.MotionStatus == BeltConst.BELT_MT_STOPPED;
            case BeltConst.BELT_SUE_EMERGENCY_STOP:
                return props.MotionStatus == BeltConst.BELT_MT_EMERGENCY;
            case BeltConst.BELT_SUE_MOTOR_FUSE_DEFECT:
            case BeltConst.BELT_SUE_MOTOR_OVERHEATING:
                return props.MotionStatus == BeltConst.BELT_MT_MOTOR_FAULT;
            case BeltConst.BELT_SUE_LIGHT_BARRIER_BACKWARD_INTERRUPTED:
                return props.LightBarrierBackwardInterrupted == true;
            case BeltConst.BELT_SUE_LIGHT_BARRIER_BACKWARD_OK:
                return props.LightBarrierBackwardInterrupted == false;
            case BeltConst.BELT_SUE_LIGHT_BARRIER_FORWARD_INTERRUPTED:
                return props.LightBarrierForwardInterrupted == true;
            case BeltConst.BELT_SUE_LIGHT_BARRIER_FORWARD_OK:
                return props.LightBarrierForwardInterrupted == false;
            case BeltConst.BELT_SUE_SECURITY_FLAP_BACKWARD_CLOSED:
                return props.SecurityFlapBackwardOpened == false;
            case BeltConst.BELT_SUE_SECURITY_FLAP_BACKWARD_OPENED:
                return props.SecurityFlapBackwardOpened == true;
            case BeltConst.BELT_SUE_SECURITY_FLAP_FORWARD_CLOSED:
                return props.SecurityFlapForwardOpened == false;
            case BeltConst.BELT_SUE_SECURITY_FLAP_FORWARD_OPENED:
                return props.SecurityFlapForwardOpened == true;
        }
        return false;
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        String[] propnames = {
                "LightBarrierBackwardInterrupted",
                "LightBarrierForwardInterrupted",
                "SecurityFlapBackwardOpened",
                "SecurityFlapBackwardOpened",
                "MotionStatus"
        };
        Object[] oldvals = getPropertyValues(propnames);
        if (super.setAndCheckStatusProperties())
            return true;
        return propertiesHaveBeenChanged(propnames, oldvals);
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        if (ret.length() > 0)
            return ret;
        switch (getStatus()) {
            case BeltConst.BELT_SUE_AUTO_STOP:
                return "Belt Auto Stop";
            case BeltConst.BELT_SUE_EMERGENCY_STOP:
                return "Belt Emergency Stop";
            case BeltConst.BELT_SUE_SAFETY_STOP:
                return "Belt Safety Stop";
            case BeltConst.BELT_SUE_TIMEOUT_STOP:
                return "Belt Motor Timeout Stop";
            case BeltConst.BELT_SUE_LIGHT_BARRIER_BACKWARD_INTERRUPTED:
                return "Belt Backward Light Barrier Interrupted";
            case BeltConst.BELT_SUE_LIGHT_BARRIER_BACKWARD_OK:
                return "Belt Backward Light Barrier Free";
            case BeltConst.BELT_SUE_LIGHT_BARRIER_FORWARD_INTERRUPTED:
                return "Belt Forward Light Barrier Interrupted";
            case BeltConst.BELT_SUE_LIGHT_BARRIER_FORWARD_OK:
                return "Belt Forward Light Barrier Free";
            case BeltConst.BELT_SUE_MOTOR_FUSE_DEFECT:
                return "Belt Motor Fuse Defect";
            case BeltConst.BELT_SUE_MOTOR_OVERHEATING:
                return "Belt Motor Overheating";
            case BeltConst.BELT_SUE_SECURITY_FLAP_BACKWARD_CLOSED:
                return "Belt Backward Security Flap Closed";
            case BeltConst.BELT_SUE_SECURITY_FLAP_BACKWARD_OPENED:
                return "Belt Backward Security Flap Open";
            case BeltConst.BELT_SUE_SECURITY_FLAP_FORWARD_CLOSED:
                return "Belt Forward Security Flap Closed";
            case BeltConst.BELT_SUE_SECURITY_FLAP_FORWARD_OPENED:
                return "Belt Forward Security Flap Open";
        }
        return "Unknown Belt Status Change: " + getStatus();
    }
}
