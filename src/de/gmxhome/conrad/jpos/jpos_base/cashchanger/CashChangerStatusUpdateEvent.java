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

package de.gmxhome.conrad.jpos.jpos_base.cashchanger;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

import java.util.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.CashChangerConst.*;
import static jpos.JposConst.*;

/**
 * Status update event implementation for CashChanger devices.
 */
public class CashChangerStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Exception will not be thrown, the exception properties ErrorCode and ErrorCodeExtended will be used to fill the
     * corresponding result code properties instead. If null, the result code properties will be set to the values reserved for
     * error-free processing.
     */
    private final JposException Exception;

    /**
     * Status value.
     */
    private final Integer State;

    /**
     * Array of possible status values when deposit is not in progress.
     */
    private static final long[] OutOfDepositStates = { CHAN_STATUS_DEPOSIT_END, CHAN_STATUS_DEPOSIT_NONE };

    /**
     * Flag to be used to check whether the devise is in a deposit operation or not. If this is not known,
     * InDepositOperation will be null.
     */
    private final Boolean InDepositOperation;

    /**
     * Constructor, Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     */
    public CashChangerStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
        InDepositOperation = !member(((CashChangerProperties) getPropertySet()).DepositStatus, OutOfDepositStates);
        State = InDepositOperation ? CHAN_STATUS_DEPOSIT_END : CHAN_STATUS_OK;
        Exception = null;
    }

    /**
     * Constructor, Parameters source and state passed to base class unchanged.
     * Parameter ex can be used by objects derived from JposOutputRequest to pass the results of the corresponding
     * request.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     * @param ex     JposException, used for STATUS_ASYNC in error case.
     */
    public CashChangerStatusUpdateEvent(JposBase source, int state, JposException ex) {
        super(source, state);
        Exception = ex;
        InDepositOperation = null;
        State = null;
    }

    /**
     * Constructor, Parameters source and status passed to base class unchanged.
     * Parameter state must be used to pass the value for DeviceStatus, DepositStatus or FullStatus, depending on
     * the given status value.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param status Status, see UPOS specification.
     * @param state  Value to be set in DeviceStatus or DepositStatus, depending on current DepositStatus value.
     */
    public CashChangerStatusUpdateEvent(JposBase source, int status, int state) {
        super(source, status);
        InDepositOperation = !member(((CashChangerProperties) getPropertySet()).DepositStatus, OutOfDepositStates);
        State = state;
        Exception = null;
    }

    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        CashChangerProperties props = (CashChangerProperties) getPropertySet();
        int state = getStatus();
        switch (state) {
            case CHAN_STATUS_EMPTYOK -> props.DeviceStatus = CHAN_STATUS_EMPTYOK;
            case CHAN_STATUS_FULLOK -> props.FullStatus = CHAN_STATUS_OK;
            case CHAN_STATUS_FULL, CHAN_STATUS_NEARFULL -> props.FullStatus = state;
            case CHAN_STATUS_EMPTY, CHAN_STATUS_NEAREMPTY -> {
                if (!InDepositOperation)
                    props.DeviceStatus = state;
            }
            case CHAN_STATUS_JAM -> {
                if (InDepositOperation)
                    props.DepositStatus = CHAN_STATUS_DEPOSIT_JAM;
                else
                    props.DeviceStatus = CHAN_STATUS_JAM;
            }
            case CHAN_STATUS_JAMOK -> {
                if (InDepositOperation)
                    props.DepositStatus = State;
                else
                    props.DeviceStatus = State;
            }
            case CHAN_STATUS_ASYNC -> {
                props.AsyncResultCode = Exception == null ? JPOS_SUCCESS : Exception.getErrorCode();
                props.AsyncResultCodeExtended = Exception == null ? 0 : Exception.getErrorCodeExtended();
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean checkStatusCorresponds() {
        CashChangerProperties props = (CashChangerProperties) getPropertySet();
        return super.checkStatusCorresponds() || switch (getStatus()) {
            case CHAN_STATUS_EMPTY, CHAN_STATUS_NEAREMPTY -> props.DeviceStatus == getStatus();
            case CHAN_STATUS_EMPTYOK -> Objects.equals(props.DeviceStatus, State);
            case CHAN_STATUS_JAM -> InDepositOperation ? props.DepositStatus == CHAN_STATUS_DEPOSIT_JAM : props.DeviceStatus == getStatus();
            case CHAN_STATUS_JAMOK -> InDepositOperation ? Objects.equals(props.DepositStatus, State) : Objects.equals(props.DeviceStatus, State);
            case CHAN_STATUS_FULL, CHAN_STATUS_NEARFULL -> props.FullStatus == getStatus();
            case CHAN_STATUS_FULLOK -> props.FullStatus == CHAN_STATUS_OK;
            case CHAN_STATUS_ASYNC -> props.AsyncResultCode == (Exception == null ? JPOS_SUCCESS : Exception.getErrorCode()) &&
                    props.AsyncResultCodeExtended == (Exception == null ? 0 : Exception.getErrorCodeExtended());
            default -> false;
        };
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        String[] propnames = { "AsyncResultCode", "AsyncResultCodeExtended", "DeviceStatus", "DepositStatus", "FullStatus" };
        Object[] oldvals = getPropertyValues(propnames);
        return super.setAndCheckStatusProperties() || propertiesHaveBeenChanged(propnames, oldvals);
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
         return ret.length() > 0 ? ret :switch (getStatus()) {
            case CHAN_STATUS_EMPTY -> "CashChanger Slot Empty";
            case CHAN_STATUS_NEAREMPTY -> "CashChanger Slot Nearly Empty";
            case CHAN_STATUS_EMPTYOK -> "CashChanger Slots OK";
            case CHAN_STATUS_JAM -> "CashChanger Jam";
            case CHAN_STATUS_JAMOK -> "CashChanger No Jam";
            case CHAN_STATUS_FULL -> "CashChanger Slot Full";
            case CHAN_STATUS_NEARFULL -> "CashChanger Slot Nearly Full";
            case CHAN_STATUS_FULLOK -> "CashChanger Slots Under Limit";
            case CHAN_STATUS_ASYNC -> "CashChanger Async Method Finished";
            default -> "Unknown CashChanger Status Change: " + getStatus();
        };
    }
    }
