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
import jpos.*;
import org.apache.log4j.Level;

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
            case MotionSensorConst.MOTION_M_ABSENT:
                props.Motion = false;
                props.signalWaiter();
                break;
            case MotionSensorConst.MOTION_M_PRESENT:
                props.Motion = true;
                props.signalWaiter();
        }
    }

    @Override
    public boolean checkStatusCorresponds() {
        if (super.checkStatusCorresponds())
            return true;
        MotionSensorProperties props = (MotionSensorProperties)getPropertySet();
        switch (getStatus()) {
            case MotionSensorConst.MOTION_M_ABSENT:
                return props.Motion == false;
            case MotionSensorConst.MOTION_M_PRESENT:
                return props.Motion == true;
        }
        return false;
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
        if (ret.length() > 0)
            return ret;
        switch (getStatus()) {
            case MotionSensorConst.MOTION_M_PRESENT:
                return "Motion detected";
            case MotionSensorConst.MOTION_M_ABSENT:
                return "No motion detected";
        }
        return "Unknown Status Change: " + getStatus();
    }

    @Override
    public long handleDelay() {
        if (getStatus() == MotionSensorConst.MOTION_M_ABSENT)
            return ((MotionSensorProperties)getPropertySet()).Timeout;
        else if (getStatus() == MotionSensorConst.MOTION_M_PRESENT)
            return CANCEL_ONLY;
        return 0;
    }
}
