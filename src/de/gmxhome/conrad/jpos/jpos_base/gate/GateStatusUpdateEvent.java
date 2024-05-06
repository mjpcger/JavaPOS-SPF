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

package de.gmxhome.conrad.jpos.jpos_base.gate;

import de.gmxhome.conrad.jpos.jpos_base.*;

import static jpos.GateConst.*;

/**
 * Status update event implementation for Gate devices.
 */
public class GateStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Constructor, see JposStatusUpdateEvent
     *
     * @param source Source, for services implemented with this framework, the (gate.)GateService object.
     * @param state  New status value, see UPOS specification, chapter Gate - Events - StatusUpdateEvent.
     */
    public GateStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
    }

    @Override
    public JposStatusUpdateEvent copyEvent(JposBase o) {
        return new GateStatusUpdateEvent(o, getStatus());
    }

    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        GateProperties props = (GateProperties)getPropertySet();
        switch (getStatus()) {
            case GATE_SUE_CLOSED -> props.GateStatus = GATE_GS_CLOSED;
            case GATE_SUE_OPEN -> props.GateStatus = GATE_GS_OPEN;
            case GATE_SUE_BLOCKED -> props.GateStatus = GATE_GS_BLOCKED;
            case GATE_SUE_MALFUNCTION -> props.GateStatus = GATE_GS_MALFUNCTION;
            default -> {
                return false;
            }
        }
        props.signalWaiter();
        return true;
    }

    @Override
    public boolean checkStatusCorresponds() {
        GateProperties props = (GateProperties)getPropertySet();
        return super.checkStatusCorresponds() || switch (getStatus()) {
            case GATE_SUE_CLOSED -> props.GateStatus == GATE_GS_CLOSED;
            case GATE_SUE_OPEN -> props.GateStatus == GATE_GS_OPEN;
            case GATE_SUE_BLOCKED -> props.GateStatus == GATE_GS_BLOCKED;
            case GATE_SUE_MALFUNCTION -> props.GateStatus == GATE_GS_MALFUNCTION;
            default -> false;
        };
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        GateProperties props = (GateProperties)getPropertySet();
        int status = props.GateStatus;
        if (super.setAndCheckStatusProperties())
            return true;
        if (status != props.GateStatus) {
            props.EventSource.logSet("GateStatus");
            return true;
        }
        return false;
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        return ret.length() > 0 ? ret : switch (getStatus()) {
            case GATE_SUE_CLOSED -> "Gate Closed";
            case GATE_SUE_OPEN -> "Gate Open";
            case GATE_SUE_BLOCKED -> "Gate Blocked";
            case GATE_SUE_MALFUNCTION -> "Gate Malfunction";
            default -> "Unknown Status Change: " + getStatus();
        };
    }
}
