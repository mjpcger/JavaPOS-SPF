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

package de.gmxhome.conrad.jpos.jpos_base.coindispenser;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

import static jpos.CoinDispenserConst.*;

/**
 * Status update event implementation for CoinDispenser devices.
 */
public class CoinDispenserStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Constructor, Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (coindispenser.)CoinDispenserService object.
     * @param state  New status value, see UPOS specification, chapter Coin Dispenser - Events - StatusUpdateEvent.
     */
    public CoinDispenserStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
    }

    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        CoinDispenserProperties props = (CoinDispenserProperties) getPropertySet();
        int status = getStatus();
        switch (status) {
        default:
            return false;
        case COIN_STATUS_OK:
        case COIN_STATUS_EMPTY:
        case COIN_STATUS_NEAREMPTY:
        case COIN_STATUS_JAM:
            props.DispenserStatus = status;
        }
        return true;
    }

    @Override
    public boolean checkStatusCorresponds() {
        CoinDispenserProperties props = (CoinDispenserProperties) getPropertySet();
        int status = getStatus();
        if (!super.checkStatusCorresponds()) {
            switch (status) {
            case COIN_STATUS_OK:
            case COIN_STATUS_EMPTY:
            case COIN_STATUS_NEAREMPTY:
            case COIN_STATUS_JAM:
                return props.DispenserStatus == status;
            default:
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        CoinDispenserProperties props = (CoinDispenserProperties)getPropertySet();
        int status = props.DispenserStatus;
        if (super.setAndCheckStatusProperties())
            return true;
        if (status != props.DispenserStatus) {
            props.EventSource.logSet("DispenserStatus");
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
        case COIN_STATUS_OK:
            return "Coin Dispenser OK";
        case COIN_STATUS_EMPTY:
            return "Coin Dispenser Empty";
        case COIN_STATUS_NEAREMPTY:
            return "Coin Dispenser Near Empty";
        case COIN_STATUS_JAM:
            return "Coin Dispenser Jam";
        }
        return "Unknown Coin Dispenser Status Change: " + getStatus();
    }
}
