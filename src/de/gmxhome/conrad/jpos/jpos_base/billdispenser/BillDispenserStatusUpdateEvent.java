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

package de.gmxhome.conrad.jpos.jpos_base.billdispenser;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.JposException;

import static jpos.BillDispenserConst.*;
import static jpos.JposConst.*;

/**
 * Status update event implementation for BillDispenser devices.
 */
public class BillDispenserStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Exception will not be thrown, the exception properties ErrorCode and ErrorCodeExtended will be used to fill the
     * corresponding result code properties instead. If null, the result code properties will be set to the values reserved for
     * error-free processing.
     */
    private final JposException Exception;

    /**
     * Value to be set in property DeviceStatus, if specified within constructor.
     */
    private final int DeviceState;

    /**
     * Constructor, Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     */
    public BillDispenserStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
        Exception = null;
        DeviceState = 0;
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
    public BillDispenserStatusUpdateEvent(JposBase source, int state, JposException ex) {
        super(source, state);
        Exception = state == BDSP_STATUS_ASYNC ? ex : null;
        DeviceState = 0;
    }

    /**
     * Constructor, Parameters source and state passed to base class unchanged.
     * Parameter deviceState must be used to pass the value for DeviceStatus, depending on
     * the given status value.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     * @param deviceState Value to be set in DeviceStatus, in case of jam or slot condition change.
     */
    public BillDispenserStatusUpdateEvent(JposBase source, int state, int deviceState) {
        super(source, state);
        Exception = null;
        DeviceState = state != BDSP_STATUS_ASYNC ? deviceState : 0;
    }

    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        BillDispenserProperties props = (BillDispenserProperties)getPropertySet();
        if (getStatus() == BDSP_STATUS_ASYNC) {
            props.AsyncResultCode = Exception == null ? JPOS_SUCCESS : Exception.getErrorCode();
            props.AsyncResultCodeExtended = Exception == null ? 0 : Exception.getErrorCodeExtended();
            return true;
        }
        switch (getStatus()) {
        default:
            return false;
        case BDSP_STATUS_EMPTY:
        case BDSP_STATUS_NEAREMPTY:
        case BDSP_STATUS_EMPTYOK:
        case BDSP_STATUS_JAM:
        case BDSP_STATUS_JAMOK:
            props.DeviceStatus = DeviceState;
        }
        return true;
    }

    @Override
    public boolean checkStatusCorresponds() {
        BillDispenserProperties props = (BillDispenserProperties) getPropertySet();
        if (super.checkStatusCorresponds())
            return true;
        switch (getStatus()) {
        case BDSP_STATUS_EMPTY:
        case BDSP_STATUS_NEAREMPTY:
        case BDSP_STATUS_EMPTYOK:
        case BDSP_STATUS_JAM:
        case BDSP_STATUS_JAMOK:
            return props.DeviceStatus == DeviceState;
        case BDSP_STATUS_ASYNC:
            return props.AsyncResultCode == (Exception == null ? BDSP_STATUS_OK : Exception.getErrorCode()) &&
                    props.AsyncResultCodeExtended == (Exception == null ? 0 : Exception.getErrorCodeExtended());
        }
        return false;
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        String[] propnames = { "AsyncResultCode", "AsyncResultCodeExtended", "DeviceStatus" };
        Object[] oldvals = getPropertyValues(propnames);
        return super.setAndCheckStatusProperties() || propertiesHaveBeenChanged(propnames, oldvals);
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        if (ret.length() > 0)
            return ret;
        switch (getStatus()) {
            case BDSP_STATUS_EMPTY:
                return "BillDispenser One Slot Empty";
            case BDSP_STATUS_NEAREMPTY:
                return "BillDispenser One Slot Nearly Empty";
            case BDSP_STATUS_EMPTYOK:
                return "BillDispenser Slots Not Empty";
            case BDSP_STATUS_JAM:
                return "BillDispenser Jam Status";
            case BDSP_STATUS_JAMOK:
                return "BillDispenser No Jam Status";
            case BDSP_STATUS_ASYNC:
                return "BillDispenser Async Method Finished";
        }
        return "Unknown BillDispenser Status Change: " + getStatus();
    }
}
