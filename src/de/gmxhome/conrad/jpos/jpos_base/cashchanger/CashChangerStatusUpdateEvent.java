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

import de.gmxhome.conrad.jpos.jpos_base.JposBase;
import de.gmxhome.conrad.jpos.jpos_base.JposDevice;
import de.gmxhome.conrad.jpos.jpos_base.JposStatusUpdateEvent;
import jpos.CashChangerConst;
import jpos.JposConst;
import jpos.JposException;

import java.util.Arrays;

/**
 *
 */
public class CashChangerStatusUpdateEvent extends JposStatusUpdateEvent {
    private JposException Exception = null;
    private Integer State = null;
    private static final long[] OutOfDepositStates = {CashChangerConst.CHAN_STATUS_DEPOSIT_END, CashChangerConst.CHAN_STATUS_DEPOSIT_NONE};
    private Boolean InDepositOperation = null;

    /**
     * Constructor, Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     */
    public CashChangerStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
        InDepositOperation = JposDevice.member(((CashChangerProperties) getPropertySet()).DepositStatus, OutOfDepositStates);
        State = InDepositOperation ? CashChangerConst.CHAN_STATUS_DEPOSIT_END : CashChangerConst.CHAN_STATUS_OK;
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
        InDepositOperation = JposDevice.member(((CashChangerProperties) getPropertySet()).DepositStatus, OutOfDepositStates);
        State = state;
    }

    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        CashChangerProperties props = (CashChangerProperties) getPropertySet();
        switch (getStatus()) {
            case CashChangerConst.CHAN_STATUS_EMPTY:
            case CashChangerConst.CHAN_STATUS_NEAREMPTY:
                if (InDepositOperation) {
                    props.DeviceStatus = getStatus();
                    return true;
                }
            case CashChangerConst.CHAN_STATUS_EMPTYOK:
                props.DeviceStatus = State;
                return true;
            case CashChangerConst.CHAN_STATUS_JAM:
                if (InDepositOperation)
                    props.DepositStatus = CashChangerConst.CHAN_STATUS_DEPOSIT_JAM;
                else
                    props.DeviceStatus = getStatus();
                return true;
            case CashChangerConst.CHAN_STATUS_JAMOK:
                if (InDepositOperation)
                    props.DepositStatus = State;
                else
                    props.DeviceStatus = State;
                return true;
            case CashChangerConst.CHAN_STATUS_FULL:
            case CashChangerConst.CHAN_STATUS_NEARFULL:
                props.FullStatus = getStatus();
                return true;
            case CashChangerConst.CHAN_STATUS_FULLOK:
                props.FullStatus = CashChangerConst.CHAN_STATUS_OK;
                return true;
            case CashChangerConst.CHAN_STATUS_ASYNC:
                props.AsyncResultCode = Exception == null ? JposConst.JPOS_SUCCESS : Exception.getErrorCode();
                props.AsyncResultCodeExtended = Exception == null ? 0 : Exception.getErrorCodeExtended();
                return true;
        }
        return false;
    }

    @Override
    public boolean checkStatusCorresponds() {
        if (super.checkStatusCorresponds())
            return true;
        CashChangerProperties props = (CashChangerProperties) getPropertySet();
        switch (getStatus()) {
            case CashChangerConst.CHAN_STATUS_EMPTY:
            case CashChangerConst.CHAN_STATUS_NEAREMPTY:
                return props.DeviceStatus == getStatus();
            case CashChangerConst.CHAN_STATUS_EMPTYOK:
                return props.DeviceStatus == State;
            case CashChangerConst.CHAN_STATUS_JAM:
                if (InDepositOperation)
                    return props.DepositStatus == CashChangerConst.CHAN_STATUS_DEPOSIT_JAM;
                return props.DeviceStatus == getStatus();
            case CashChangerConst.CHAN_STATUS_JAMOK:
                if (InDepositOperation)
                    return props.DepositStatus == State;
                return props.DeviceStatus == State;
            case CashChangerConst.CHAN_STATUS_FULL:
            case CashChangerConst.CHAN_STATUS_NEARFULL:
                return props.FullStatus == getStatus();
            case CashChangerConst.CHAN_STATUS_FULLOK:
                return props.FullStatus == CashChangerConst.CHAN_STATUS_OK;
            case CashChangerConst.CHAN_STATUS_ASYNC:
                return props.AsyncResultCode == (Exception == null ? JposConst.JPOS_SUCCESS : Exception.getErrorCode()) &&
                       props.AsyncResultCodeExtended == (Exception == null ? 0 : Exception.getErrorCodeExtended());
        }
        return false;
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        String[] propnames = {
                "AsyncResultCode",
                "AsyncResultCodeExtended",
                "DeviceStatus",
                "DepositStatus",
                "FullStatus"
        };
        Object[] oldvals = getPropertyValues(propnames);
        if (super.setAndCheckStatusProperties())
            return true;
        return propertiesHaveBeenChanged(propnames, oldvals);
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        if (ret.length() > 0)
            return ret;
        switch (getStatus()) {
            case CashChangerConst.CHAN_STATUS_EMPTY:
                return "CashChanger Slot Empty";
            case CashChangerConst.CHAN_STATUS_NEAREMPTY:
                return "CashChanger Slot Nearly Empty";
            case CashChangerConst.CHAN_STATUS_EMPTYOK:
                return "CashChanger Slots OK";
            case CashChangerConst.CHAN_STATUS_JAM:
                return "CashChanger Jam";
            case CashChangerConst.CHAN_STATUS_JAMOK:
                return "CashChanger No Jam";
            case CashChangerConst.CHAN_STATUS_FULL:
                return "CashChanger Slot Full";
            case CashChangerConst.CHAN_STATUS_NEARFULL:
                return "CashChanger Slot Nearly Full";
            case CashChangerConst.CHAN_STATUS_FULLOK:
                return "CashChanger Slots Under Limit";
            case CashChangerConst.CHAN_STATUS_ASYNC:
                return "CashChanger Async Method Finished";
        }
        return "Unknown CashChanger Status Change: " + getStatus();
    }
}
