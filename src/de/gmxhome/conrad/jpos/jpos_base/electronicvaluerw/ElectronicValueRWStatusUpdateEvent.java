/*
 * Copyright 2022 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.electronicvaluerw;

import de.gmxhome.conrad.jpos.jpos_base.*;

import static jpos.ElectronicValueRWConst.*;

/**
 * Status update event implementation for ElectronicValueRW devices.
 */
public class ElectronicValueRWStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Constructor, Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (electronicvaluerw.)ElectronicValueRWService object.
     * @param state  Status, see UPOS specification.
     */
    public ElectronicValueRWStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
    }

    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        ElectronicValueRWProperties props = (ElectronicValueRWProperties)getPropertySet();
        int status = getStatus();
        switch (status) {
            case EVRW_SUE_LS_OK -> props.LogStatus = EVRW_LS_OK;
            case EVRW_SUE_LS_NEARFULL -> props.LogStatus = EVRW_LS_NEARFULL;
            case EVRW_SUE_LS_FULL -> props.LogStatus = EVRW_LS_FULL;
            case EVRW_SUE_DS_NOCARD -> props.LogStatus = EVRW_DS_NOCARD;
            case EVRW_SUE_DS_DETECTED -> props.LogStatus = EVRW_DS_DETECTED;
            case EVRW_SUE_DS_ENTERED -> props.LogStatus = EVRW_DS_ENTERED;
            case EVRW_SUE_DS_CAPTURED -> props.LogStatus = EVRW_DS_CAPTURED;
            default -> {
                return false;
            }
        };
        return true;
    }

    @Override
    public boolean checkStatusCorresponds() {
        ElectronicValueRWProperties props = (ElectronicValueRWProperties)getPropertySet();
        return super.checkStatusCorresponds() || switch (getStatus()) {
            case EVRW_SUE_LS_OK -> props.LogStatus == EVRW_LS_OK;
            case EVRW_SUE_LS_NEARFULL -> props.LogStatus == EVRW_LS_NEARFULL;
            case EVRW_SUE_LS_FULL -> props.LogStatus == EVRW_LS_FULL;
            case EVRW_SUE_DS_NOCARD -> props.LogStatus == EVRW_DS_NOCARD;
            case EVRW_SUE_DS_DETECTED -> props.LogStatus == EVRW_DS_DETECTED;
            case EVRW_SUE_DS_ENTERED -> props.LogStatus == EVRW_DS_ENTERED;
            case EVRW_SUE_DS_CAPTURED -> props.LogStatus == EVRW_DS_CAPTURED;
            default -> false;
        };
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        String[] names = {"LogStatus", "DetectionStatus"};
        Object[] values = getPropertyValues(names);
        return super.setAndCheckStatusProperties() || propertiesHaveBeenChanged(names, values);
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        return ret.length() > 0 ? ret : switch (getStatus()) {
            case EVRW_SUE_LS_OK -> "ElectronicValueRW Dealing Log OK";
            case EVRW_SUE_LS_NEARFULL -> "ElectronicValueRW Dealing Log Nearly Full";
            case EVRW_SUE_LS_FULL -> "ElectronicValueRW Dealing Log Full";
            case EVRW_SUE_DS_NOCARD -> "ElectronicValueRW Detected NOCARD";
            case EVRW_SUE_DS_DETECTED -> "ElectronicValueRW Detected Card";
            case EVRW_SUE_DS_ENTERED -> "ElectronicValueRW Detected Card Entered";
            case EVRW_SUE_DS_CAPTURED -> "ElectronicValueRW Detected Card Captured";
            default -> "Unknown ElectronicValueRW Status Change: " + getStatus();
        };
    }
}
