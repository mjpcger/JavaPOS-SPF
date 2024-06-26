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

package de.gmxhome.conrad.jpos.jpos_base.coinacceptor;

import de.gmxhome.conrad.jpos.jpos_base.JposBase;
import de.gmxhome.conrad.jpos.jpos_base.JposStatusUpdateEvent;

import static jpos.CoinAcceptorConst.*;

/**
 * Status update event implementation for CoinAcceptor devices.
 */
public class CoinAcceptorStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * New value for DepositStatus property
     */
    private final int DepositState;

    /**
     * Constructor, Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     */
    public CoinAcceptorStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
        DepositState = CACC_STATUS_DEPOSIT_END;
    }

    /**
     * Constructor, Parameters source and state passed to base class unchanged.
     * Parameter depositState can be used to pass the value for DepositStatus that differs from
     * STATUS_DEPOSIT_END in case of STATUS_JAMOK.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     * @param depositState Value to be set in DepositStatus if state is STATUS_JAMOK.
     */
    public CoinAcceptorStatusUpdateEvent(JposBase source, int state, int depositState) {
        super(source, state);
        DepositState = depositState;
    }

    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        CoinAcceptorProperties props = (CoinAcceptorProperties) getPropertySet();
        int state = getStatus();
        switch (state) {
            case CACC_STATUS_FULL, CACC_STATUS_NEARFULL -> props.FullStatus = state;
            case CACC_STATUS_FULLOK -> props.FullStatus = CACC_STATUS_OK;
            case CACC_STATUS_JAM -> props.DepositStatus = CACC_STATUS_JAM;
            case CACC_STATUS_JAMOK -> props.DepositStatus = DepositState;
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean checkStatusCorresponds() {
        CoinAcceptorProperties props = (CoinAcceptorProperties)getPropertySet();
        return super.checkStatusCorresponds() || switch (getStatus()) {
            case CACC_STATUS_FULL, CACC_STATUS_NEARFULL ->
                    props.FullStatus == getStatus();
            case CACC_STATUS_FULLOK -> props.FullStatus == CACC_STATUS_OK;
            case CACC_STATUS_JAM -> props.DepositStatus == getStatus();
            case CACC_STATUS_JAMOK -> props.DepositStatus == DepositState;
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
            case CACC_STATUS_FULL -> "CoinAcceptor Slot Full";
            case CACC_STATUS_NEARFULL -> "CoinAcceptor Slot Nearly Full";
            case CACC_STATUS_FULLOK -> "CoinAcceptor Slot Under Limit";
            case CACC_STATUS_JAM -> "CoinAcceptor Status Jam";
            case CACC_STATUS_JAMOK -> "CoinAcceptor Status No Jam";
            default -> "Unknown CoinAcceptor Status Change: " + getStatus();
        };
    }
}
