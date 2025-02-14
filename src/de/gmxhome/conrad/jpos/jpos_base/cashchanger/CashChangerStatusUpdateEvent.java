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
        default:
            return false;
        case CHAN_STATUS_EMPTYOK:
            props.DeviceStatus = CHAN_STATUS_EMPTYOK;
            break;
        case CHAN_STATUS_FULLOK:
            props.FullStatus = CHAN_STATUS_OK;
            break;
        case CHAN_STATUS_FULL:
        case CHAN_STATUS_NEARFULL:
            props.FullStatus = state;
            break;
        case CHAN_STATUS_EMPTY:
        case CHAN_STATUS_NEAREMPTY: {
                if (!InDepositOperation)
                    props.DeviceStatus = state;
            }
            break;
        case CHAN_STATUS_JAM: {
                if (InDepositOperation)
                    props.DepositStatus = CHAN_STATUS_DEPOSIT_JAM;
                else
                    props.DeviceStatus = CHAN_STATUS_JAM;
            }
            break;
        case CHAN_STATUS_JAMOK: {
                if (InDepositOperation)
                    props.DepositStatus = State;
                else
                    props.DeviceStatus = State;
            }
            break;
        case CHAN_STATUS_ASYNC: {
                props.AsyncResultCode = Exception == null ? JPOS_SUCCESS : Exception.getErrorCode();
                props.AsyncResultCodeExtended = Exception == null ? 0 : Exception.getErrorCodeExtended();
            }
        }
        return true;
    }

    @Override
    public boolean checkStatusCorresponds() {
        CashChangerProperties props = (CashChangerProperties) getPropertySet();
        if (super.checkStatusCorresponds())
            return true;
        switch (getStatus()) {
        case CHAN_STATUS_EMPTY:
        case CHAN_STATUS_NEAREMPTY:
            return props.DeviceStatus == getStatus();
        case CHAN_STATUS_EMPTYOK:
            return Objects.equals(props.DeviceStatus, State);
        case CHAN_STATUS_JAM:
            return InDepositOperation ? props.DepositStatus == CHAN_STATUS_DEPOSIT_JAM : props.DeviceStatus == getStatus();
        case CHAN_STATUS_JAMOK:
            return InDepositOperation ? Objects.equals(props.DepositStatus, State) : Objects.equals(props.DeviceStatus, State);
        case CHAN_STATUS_FULL:
        case CHAN_STATUS_NEARFULL:
            return props.FullStatus == getStatus();
        case CHAN_STATUS_FULLOK:
            return props.FullStatus == CHAN_STATUS_OK;
        case CHAN_STATUS_ASYNC:
            return props.AsyncResultCode == (Exception == null ? JPOS_SUCCESS : Exception.getErrorCode()) &&
                    props.AsyncResultCodeExtended == (Exception == null ? 0 : Exception.getErrorCodeExtended());
        }
        return false;
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
        if (ret.length() > 0)
            return ret;
        switch (getStatus()) {
        case CHAN_STATUS_EMPTY:
            return "CashChanger Slot Empty";
        case CHAN_STATUS_NEAREMPTY:
            return "CashChanger Slot Nearly Empty";
        case CHAN_STATUS_EMPTYOK:
            return "CashChanger Slots OK";
        case CHAN_STATUS_JAM:
            return "CashChanger Jam";
        case CHAN_STATUS_JAMOK:
            return "CashChanger No Jam";
        case CHAN_STATUS_FULL:
            return "CashChanger Slot Full";
        case CHAN_STATUS_NEARFULL:
            return "CashChanger Slot Nearly Full";
        case CHAN_STATUS_FULLOK:
            return "CashChanger Slots Under Limit";
        case CHAN_STATUS_ASYNC:
            return "CashChanger Async Method Finished";
    }
        return "Unknown CashChanger Status Change: " + getStatus();
    }
}
