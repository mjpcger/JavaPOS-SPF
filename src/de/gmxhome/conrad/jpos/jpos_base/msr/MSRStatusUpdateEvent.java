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
            case MSR_SUE_DEVICE_AUTHENTICATED -> props.DeviceAuthenticated = true;
            case MSR_SUE_DEVICE_DEAUTHENTICATED -> props.DeviceAuthenticated = false;
            default -> {
                return false;
            }
        }
        props.signalWaiter();
        return true;
    }

    @Override
    public boolean checkStatusCorresponds() {
        MSRProperties props = (MSRProperties)getPropertySet();
        return super.checkStatusCorresponds()  || switch (getStatus()) {
            case MSR_SUE_DEVICE_AUTHENTICATED -> props.DeviceAuthenticated;
            case MSR_SUE_DEVICE_DEAUTHENTICATED -> !props.DeviceAuthenticated;
            default -> false;
        };
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
        return ret.length() > 0 ? ret : switch (getStatus()) {
            case MSR_SUE_DEVICE_AUTHENTICATED -> "Device Authenticated";
            case MSR_SUE_DEVICE_DEAUTHENTICATED -> "Device De-Authenticated";
            default -> "Unknown Status Change: " + getStatus();
        };
    }
}
