/*
 * Copyright 2019 Martin Conrad
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
 */

package de.gmxhome.conrad.jpos.jpos_base.motionsensor;

import de.gmxhome.conrad.jpos.jpos_base.*;

import static jpos.MotionSensorConst.*;

/**
 * Status update event implementation for MotionSensor devices.
 */
public class MotionSensorStatusUpdateEvent extends DelayedStatusUpdateEvent {
    /**
     * Constructor, see JposStatusUpdateEvent
     *
     * @param source Source, for services implemented with this framework, the (motionsensor.)MotionSensorService object.
     * @param state  New status value, see UPOS specification, chapter Cash Drawer - Events - StatusUpdateEvent.
     */
    public MotionSensorStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
    }

    @Override
    public JposStatusUpdateEvent copyEvent(JposBase o) {
        return new MotionSensorStatusUpdateEvent(o, getStatus());
    }

    @Override
    public void setLateProperties() {
        super.setLateProperties();
        MotionSensorProperties props = (MotionSensorProperties)getPropertySet();
        switch (getStatus()) {
            case MOTION_M_ABSENT -> props.Motion = false;
            case MOTION_M_PRESENT -> props.Motion = true;
            default -> {
                return;
            }
        }
        props.signalWaiter();
    }

    @Override
    public boolean checkStatusCorresponds() {
        MotionSensorProperties props = (MotionSensorProperties)getPropertySet();
        return super.checkStatusCorresponds() || switch (getStatus()) {
            case MOTION_M_ABSENT -> !props.Motion;
            case MOTION_M_PRESENT -> props.Motion;
            default -> false;
        };
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        MotionSensorProperties props = (MotionSensorProperties)getPropertySet();
        boolean status = props.Motion;
        if (super.setAndCheckStatusProperties())
            return true;
        if (status != props.Motion) {
            props.EventSource.logSet("Motion");
            return true;
        }
        return false;
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        return ret.length() > 0 ? ret : switch (getStatus()) {
            case MOTION_M_PRESENT -> "Motion detected";
            case MOTION_M_ABSENT -> "No motion detected";
            default -> "Unknown Status Change: " + getStatus();
        };
    }

    @Override
    public long handleDelay() {
        if (getStatus() == MOTION_M_ABSENT)
            return ((MotionSensorProperties)getPropertySet()).Timeout;
        else if (getStatus() == MOTION_M_PRESENT)
            return CANCEL_ONLY;
        return 0;
    }
}
