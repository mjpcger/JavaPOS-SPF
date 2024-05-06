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
            case BELT_SUE_AUTO_STOP, BELT_SUE_EMERGENCY_STOP, BELT_SUE_SAFETY_STOP, BELT_SUE_TIMEOUT_STOP -> props.MotionStatus = BELT_MT_STOPPED;
            case BELT_SUE_MOTOR_FUSE_DEFECT, BELT_SUE_MOTOR_OVERHEATING -> props.MotionStatus = BELT_MT_MOTOR_FAULT;
            case BELT_SUE_LIGHT_BARRIER_BACKWARD_INTERRUPTED -> props.LightBarrierBackwardInterrupted = true;
            case BELT_SUE_LIGHT_BARRIER_BACKWARD_OK -> props.LightBarrierBackwardInterrupted = false;
            case BELT_SUE_LIGHT_BARRIER_FORWARD_INTERRUPTED -> props.LightBarrierForwardInterrupted = true;
            case BELT_SUE_LIGHT_BARRIER_FORWARD_OK -> props.LightBarrierForwardInterrupted = false;
            case BELT_SUE_SECURITY_FLAP_BACKWARD_CLOSED -> props.SecurityFlapBackwardOpened = false;
            case BELT_SUE_SECURITY_FLAP_BACKWARD_OPENED -> props.SecurityFlapBackwardOpened = true;
            case BELT_SUE_SECURITY_FLAP_FORWARD_CLOSED -> props.SecurityFlapForwardOpened = false;
            case BELT_SUE_SECURITY_FLAP_FORWARD_OPENED -> props.SecurityFlapForwardOpened = true;
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean checkStatusCorresponds() {
        BeltProperties props = (BeltProperties)getPropertySet();
        return super.checkStatusCorresponds() || switch (getStatus()) {
            case BELT_SUE_AUTO_STOP, BELT_SUE_SAFETY_STOP, BELT_SUE_TIMEOUT_STOP -> props.MotionStatus == BELT_MT_STOPPED;
            case BELT_SUE_EMERGENCY_STOP -> props.MotionStatus == BELT_MT_EMERGENCY;
            case BELT_SUE_MOTOR_FUSE_DEFECT, BELT_SUE_MOTOR_OVERHEATING -> props.MotionStatus == BELT_MT_MOTOR_FAULT;
            case BELT_SUE_LIGHT_BARRIER_BACKWARD_INTERRUPTED -> props.LightBarrierBackwardInterrupted;
            case BELT_SUE_LIGHT_BARRIER_BACKWARD_OK -> !props.LightBarrierBackwardInterrupted;
            case BELT_SUE_LIGHT_BARRIER_FORWARD_INTERRUPTED -> props.LightBarrierForwardInterrupted;
            case BELT_SUE_LIGHT_BARRIER_FORWARD_OK -> !props.LightBarrierForwardInterrupted;
            case BELT_SUE_SECURITY_FLAP_BACKWARD_CLOSED -> !props.SecurityFlapBackwardOpened;
            case BELT_SUE_SECURITY_FLAP_BACKWARD_OPENED -> props.SecurityFlapBackwardOpened;
            case BELT_SUE_SECURITY_FLAP_FORWARD_CLOSED -> !props.SecurityFlapForwardOpened;
            case BELT_SUE_SECURITY_FLAP_FORWARD_OPENED -> props.SecurityFlapForwardOpened;
            default -> false;
        };
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
        return ret.length() > 0 ? ret : switch (getStatus()) {
            case BELT_SUE_AUTO_STOP -> "Belt Auto Stop";
            case BELT_SUE_EMERGENCY_STOP -> "Belt Emergency Stop";
            case BELT_SUE_SAFETY_STOP -> "Belt Safety Stop";
            case BELT_SUE_TIMEOUT_STOP -> "Belt Motor Timeout Stop";
            case BELT_SUE_LIGHT_BARRIER_BACKWARD_INTERRUPTED -> "Belt Backward Light Barrier Interrupted";
            case BELT_SUE_LIGHT_BARRIER_BACKWARD_OK -> "Belt Backward Light Barrier Free";
            case BELT_SUE_LIGHT_BARRIER_FORWARD_INTERRUPTED -> "Belt Forward Light Barrier Interrupted";
            case BELT_SUE_LIGHT_BARRIER_FORWARD_OK -> "Belt Forward Light Barrier Free";
            case BELT_SUE_MOTOR_FUSE_DEFECT -> "Belt Motor Fuse Defect";
            case BELT_SUE_MOTOR_OVERHEATING -> "Belt Motor Overheating";
            case BELT_SUE_SECURITY_FLAP_BACKWARD_CLOSED -> "Belt Backward Security Flap Closed";
            case BELT_SUE_SECURITY_FLAP_BACKWARD_OPENED -> "Belt Backward Security Flap Open";
            case BELT_SUE_SECURITY_FLAP_FORWARD_CLOSED -> "Belt Forward Security Flap Closed";
            case BELT_SUE_SECURITY_FLAP_FORWARD_OPENED -> "Belt Forward Security Flap Open";
            default -> "Unknown Belt Status Change: " + getStatus();
        };
    }
}
