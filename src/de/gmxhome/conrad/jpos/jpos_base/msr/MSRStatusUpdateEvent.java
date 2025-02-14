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

package de.gmxhome.conrad.jpos.jpos_base.msr;

import de.gmxhome.conrad.jpos.jpos_base.*;

import static jpos.MSRConst.*;

/**
 * Status update event implementation for MSR devices.
 */
public class MSRStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Constructor, see JposStatusUpdateEvent
     *
     * @param source    Source, for services implemented with this framework, the (msr.)MSRService object.
     * @param state     Status, see UPOS specification, chapter MSR - Magnetic Stripe Reader - Events - StatusUpdateEvent.
     */
    public MSRStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
    }

    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        MSRProperties props = (MSRProperties)getPropertySet();
        switch (getStatus()) {
        case MSR_SUE_DEVICE_AUTHENTICATED:
            props.DeviceAuthenticated = true;
            break;
        case MSR_SUE_DEVICE_DEAUTHENTICATED:
            props.DeviceAuthenticated = false;
            break;
        default:
            return false;
        }
        props.signalWaiter();
        return true;
    }

    @Override
    public boolean checkStatusCorresponds() {
        if (super.checkStatusCorresponds())
            return true;
        MSRProperties props = (MSRProperties)getPropertySet();
        switch (getStatus()) {
        case MSR_SUE_DEVICE_AUTHENTICATED:
            return props.DeviceAuthenticated;
        case MSR_SUE_DEVICE_DEAUTHENTICATED:
            return !props.DeviceAuthenticated;
        }
        return false;
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        MSRProperties props = (MSRProperties)getPropertySet();
        boolean status = props.DeviceAuthenticated;
        if (super.setAndCheckStatusProperties())
            return true;
        if (status != props.DeviceAuthenticated) {
            props.EventSource.logSet("DeviceAuthenticated");
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
        case MSR_SUE_DEVICE_AUTHENTICATED:
            return "Device Authenticated";
        case MSR_SUE_DEVICE_DEAUTHENTICATED:
            return "Device De-Authenticated";
        }
        return "Unknown Status Change: " + getStatus();
    }
}
