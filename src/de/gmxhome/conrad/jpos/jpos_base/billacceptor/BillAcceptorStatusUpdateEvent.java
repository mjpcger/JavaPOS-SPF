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

package de.gmxhome.conrad.jpos.jpos_base.billacceptor;

import de.gmxhome.conrad.jpos.jpos_base.JposBase;
import de.gmxhome.conrad.jpos.jpos_base.JposStatusUpdateEvent;

import static jpos.BillAcceptorConst.*;

/**
 * Status update event implementation for BillAcceptor devices.
 */
public class BillAcceptorStatusUpdateEvent extends JposStatusUpdateEvent {
    private final int DepositState;

    /**
     * Constructor, Parameters source and state passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     */
    public BillAcceptorStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
        DepositState = BACC_STATUS_DEPOSIT_END;
    }

    /**
     * Constructor, Parameters source and state passed to base class unchanged.
     * Parameter depositState can be used to pass the value for DepositStatus that differs from
     * STATUS_DEPOSIT_END in case of STATUS_JAMOK.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     * @param depositState Value to be set in DepositStatus.
     */
    public BillAcceptorStatusUpdateEvent(JposBase source, int state, int depositState) {
        super(source, state);
        DepositState = state == BACC_STATUS_JAMOK ? depositState : BACC_STATUS_DEPOSIT_END;
    }

    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        BillAcceptorProperties props = (BillAcceptorProperties)getPropertySet();
        switch (getStatus()) {
            case BACC_STATUS_FULL -> props.FullStatus = BACC_STATUS_FULL;
            case BACC_STATUS_NEARFULL -> props.FullStatus = BACC_STATUS_NEARFULL;
            case BACC_STATUS_FULLOK -> props.FullStatus = BACC_STATUS_OK;
            case BACC_STATUS_JAM -> props.DepositStatus = BACC_STATUS_DEPOSIT_JAM;
            case BACC_STATUS_JAMOK -> props.DepositStatus = DepositState;
            default -> {
                return false;
            }
        }
        return  true;
    }

    @Override
    public boolean checkStatusCorresponds() {
        BillAcceptorProperties props = (BillAcceptorProperties)getPropertySet();
        return super.checkStatusCorresponds() || switch (getStatus()) {
            case BACC_STATUS_FULL, BACC_STATUS_NEARFULL -> props.FullStatus == getStatus();
            case BACC_STATUS_FULLOK -> props.FullStatus == BACC_STATUS_OK;
            case BACC_STATUS_JAM -> props.DepositStatus == BACC_STATUS_DEPOSIT_JAM;
            case BACC_STATUS_JAMOK -> props.DepositStatus == DepositState;
            default -> false;
        };
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        String[] propnames = { "DepositStatus", "FullStatus" };
        Object[] oldvals = getPropertyValues(propnames);
        return super.setAndCheckStatusProperties() || propertiesHaveBeenChanged(propnames, oldvals);
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        return ret.length() > 0 ? ret : switch (getStatus()) {
            case BACC_STATUS_FULL -> "BillAcceptor Slot Full";
            case BACC_STATUS_NEARFULL -> "BillAcceptor Slot Nearly Full";
            case BACC_STATUS_FULLOK -> "BillAcceptor Slot Under Limit";
            case BACC_STATUS_JAM -> "BillAcceptor Status Jam";
            case BACC_STATUS_JAMOK -> "BillAcceptor Status No Jam";
            default -> "Unknown BillAcceptor Status Change: " + getStatus();
        };
    }
}
