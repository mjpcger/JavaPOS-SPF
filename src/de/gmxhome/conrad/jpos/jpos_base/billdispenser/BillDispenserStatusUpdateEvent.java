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
    private final JposException Exception;
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
            case BDSP_STATUS_EMPTY, BDSP_STATUS_NEAREMPTY, BDSP_STATUS_EMPTYOK, BDSP_STATUS_JAM, BDSP_STATUS_JAMOK ->
                    props.DeviceStatus = DeviceState;
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean checkStatusCorresponds() {
        BillDispenserProperties props = (BillDispenserProperties) getPropertySet();
        return super.checkStatusCorresponds() || switch (getStatus()) {
            case BDSP_STATUS_EMPTY, BDSP_STATUS_NEAREMPTY, BDSP_STATUS_EMPTYOK, BDSP_STATUS_JAM, BDSP_STATUS_JAMOK ->
                    props.DeviceStatus == DeviceState;
            case BDSP_STATUS_ASYNC ->
                    props.AsyncResultCode == (Exception == null ? BDSP_STATUS_OK : Exception.getErrorCode()) &&
                            props.AsyncResultCodeExtended == (Exception == null ? 0 : Exception.getErrorCodeExtended());
            default -> false;
        };
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
        return ret.length() > 0 ? ret : switch (getStatus()) {
            case BDSP_STATUS_EMPTY -> "BillDispenser One Slot Empty";
            case BDSP_STATUS_NEAREMPTY -> "BillDispenser One Slot Nearly Empty";
            case BDSP_STATUS_EMPTYOK -> "BillDispenser Slots Not Empty";
            case BDSP_STATUS_JAM -> "BillDispenser Jam Status";
            case BDSP_STATUS_JAMOK -> "BillDispenser No Jam Status";
            case BDSP_STATUS_ASYNC -> "BillDispenser Async Method Finished";
            default -> "Unknown BillDispenser Status Change: " + getStatus();
        };
    }
}
