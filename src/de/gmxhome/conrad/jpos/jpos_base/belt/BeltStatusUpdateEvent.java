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

import static jpos.BeltConst.*;

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
            case BELT_SUE_AUTO_STOP:
            case BELT_SUE_EMERGENCY_STOP:
            case BELT_SUE_SAFETY_STOP:
            case BELT_SUE_TIMEOUT_STOP:
                props.MotionStatus = BELT_MT_STOPPED;
                break;
            case BELT_SUE_MOTOR_FUSE_DEFECT:
            case BELT_SUE_MOTOR_OVERHEATING:
                props.MotionStatus = BELT_MT_MOTOR_FAULT;
                break;
            case BELT_SUE_LIGHT_BARRIER_BACKWARD_INTERRUPTED:
                props.LightBarrierBackwardInterrupted = true;
                break;
            case BELT_SUE_LIGHT_BARRIER_BACKWARD_OK:
                props.LightBarrierBackwardInterrupted = false;
                break;
            case BELT_SUE_LIGHT_BARRIER_FORWARD_INTERRUPTED:
                props.LightBarrierForwardInterrupted = true;
                break;
            case BELT_SUE_LIGHT_BARRIER_FORWARD_OK:
                props.LightBarrierForwardInterrupted = false;
                break;
            case BELT_SUE_SECURITY_FLAP_BACKWARD_CLOSED:
                props.SecurityFlapBackwardOpened = false;
                break;
            case BELT_SUE_SECURITY_FLAP_BACKWARD_OPENED:
                props.SecurityFlapBackwardOpened = true;
                break;
            case BELT_SUE_SECURITY_FLAP_FORWARD_CLOSED:
                props.SecurityFlapForwardOpened = false;
                break;
            case BELT_SUE_SECURITY_FLAP_FORWARD_OPENED:
                props.SecurityFlapForwardOpened = true;
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public boolean checkStatusCorresponds() {
        BeltProperties props = (BeltProperties)getPropertySet();
        if (super.checkStatusCorresponds())
            return true;
        switch (getStatus()) {
            case BELT_SUE_AUTO_STOP:
            case BELT_SUE_SAFETY_STOP:
            case BELT_SUE_TIMEOUT_STOP:
                return props.MotionStatus == BELT_MT_STOPPED;
            case BELT_SUE_EMERGENCY_STOP:
                return props.MotionStatus == BELT_MT_EMERGENCY;
            case BELT_SUE_MOTOR_FUSE_DEFECT:
            case BELT_SUE_MOTOR_OVERHEATING:
                return props.MotionStatus == BELT_MT_MOTOR_FAULT;
            case BELT_SUE_LIGHT_BARRIER_BACKWARD_INTERRUPTED:
                return props.LightBarrierBackwardInterrupted;
            case BELT_SUE_LIGHT_BARRIER_BACKWARD_OK:
                return !props.LightBarrierBackwardInterrupted;
            case BELT_SUE_LIGHT_BARRIER_FORWARD_INTERRUPTED:
                return props.LightBarrierForwardInterrupted;
            case BELT_SUE_LIGHT_BARRIER_FORWARD_OK:
                return !props.LightBarrierForwardInterrupted;
            case BELT_SUE_SECURITY_FLAP_BACKWARD_CLOSED:
                return !props.SecurityFlapBackwardOpened;
            case BELT_SUE_SECURITY_FLAP_BACKWARD_OPENED:
                return props.SecurityFlapBackwardOpened;
            case BELT_SUE_SECURITY_FLAP_FORWARD_CLOSED:
                return !props.SecurityFlapForwardOpened;
            case BELT_SUE_SECURITY_FLAP_FORWARD_OPENED:
                return props.SecurityFlapForwardOpened;
        };
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
        return super.setAndCheckStatusProperties() || propertiesHaveBeenChanged(propnames, oldvals);
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        if (ret.length() > 0)
            return ret;
        switch (getStatus()) {
            case BELT_SUE_AUTO_STOP:
                return "Belt Auto Stop";
            case BELT_SUE_EMERGENCY_STOP:
                return "Belt Emergency Stop";
            case BELT_SUE_SAFETY_STOP:
                return "Belt Safety Stop";
            case BELT_SUE_TIMEOUT_STOP:
                return "Belt Motor Timeout Stop";
            case BELT_SUE_LIGHT_BARRIER_BACKWARD_INTERRUPTED:
                return "Belt Backward Light Barrier Interrupted";
            case BELT_SUE_LIGHT_BARRIER_BACKWARD_OK:
                return "Belt Backward Light Barrier Free";
            case BELT_SUE_LIGHT_BARRIER_FORWARD_INTERRUPTED:
                return "Belt Forward Light Barrier Interrupted";
            case BELT_SUE_LIGHT_BARRIER_FORWARD_OK:
                return "Belt Forward Light Barrier Free";
            case BELT_SUE_MOTOR_FUSE_DEFECT:
                return "Belt Motor Fuse Defect";
            case BELT_SUE_MOTOR_OVERHEATING:
                return "Belt Motor Overheating";
            case BELT_SUE_SECURITY_FLAP_BACKWARD_CLOSED:
                return "Belt Backward Security Flap Closed";
            case BELT_SUE_SECURITY_FLAP_BACKWARD_OPENED:
                return "Belt Backward Security Flap Open";
            case BELT_SUE_SECURITY_FLAP_FORWARD_CLOSED:
                return "Belt Forward Security Flap Closed";
            case BELT_SUE_SECURITY_FLAP_FORWARD_OPENED:
                return "Belt Forward Security Flap Open";
        };
        return  "Unknown Belt Status Change: " + getStatus();
    }
}
