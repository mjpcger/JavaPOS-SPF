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
import jpos.*;

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
            case ElectronicValueRWConst.EVRW_SUE_LS_OK:
                props.LogStatus = ElectronicValueRWConst.EVRW_LS_OK;
                return true;
            case ElectronicValueRWConst.EVRW_SUE_LS_NEARFULL:
                props.LogStatus = ElectronicValueRWConst.EVRW_LS_NEARFULL;
                return true;
            case ElectronicValueRWConst.EVRW_SUE_LS_FULL:
                props.LogStatus = ElectronicValueRWConst.EVRW_LS_FULL;
                return true;
            case ElectronicValueRWConst.EVRW_SUE_DS_NOCARD:
                props.LogStatus = ElectronicValueRWConst.EVRW_DS_NOCARD;
                return true;
            case ElectronicValueRWConst.EVRW_SUE_DS_DETECTED:
                props.LogStatus = ElectronicValueRWConst.EVRW_DS_DETECTED;
                return true;
            case ElectronicValueRWConst.EVRW_SUE_DS_ENTERED:
                props.LogStatus = ElectronicValueRWConst.EVRW_DS_ENTERED;
                return true;
            case ElectronicValueRWConst.EVRW_SUE_DS_CAPTURED:
                props.LogStatus = ElectronicValueRWConst.EVRW_DS_CAPTURED;
                return true;
        }
        return false;
    }

    @Override
    public boolean checkStatusCorresponds() {
        if (super.checkStatusCorresponds())
            return true;
        ElectronicValueRWProperties props = (ElectronicValueRWProperties)getPropertySet();
        int status = getStatus();
        switch (status) {
            case ElectronicValueRWConst.EVRW_SUE_LS_OK:
                return props.LogStatus == ElectronicValueRWConst.EVRW_LS_OK;
            case ElectronicValueRWConst.EVRW_SUE_LS_NEARFULL:
                return props.LogStatus == ElectronicValueRWConst.EVRW_LS_NEARFULL;
            case ElectronicValueRWConst.EVRW_SUE_LS_FULL:
                return props.LogStatus == ElectronicValueRWConst.EVRW_LS_FULL;
            case ElectronicValueRWConst.EVRW_SUE_DS_NOCARD:
                return props.LogStatus == ElectronicValueRWConst.EVRW_DS_NOCARD;
            case ElectronicValueRWConst.EVRW_SUE_DS_DETECTED:
                return props.LogStatus == ElectronicValueRWConst.EVRW_DS_DETECTED;
            case ElectronicValueRWConst.EVRW_SUE_DS_ENTERED:
                return props.LogStatus == ElectronicValueRWConst.EVRW_DS_ENTERED;
            case ElectronicValueRWConst.EVRW_SUE_DS_CAPTURED:
                return props.LogStatus == ElectronicValueRWConst.EVRW_DS_CAPTURED;
        }
        return false;
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        String[] names = {"LogStatus", "DetectionStatus"};
        Object[] values = getPropertyValues(names);
        if (super.setAndCheckStatusProperties())
            return true;
        return propertiesHaveBeenChanged(names, values);
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        if (ret.length() > 0)
            return ret;
        switch (getStatus()) {
            case ElectronicValueRWConst.EVRW_SUE_LS_OK:
                return "ElectronicValueRW Dealing Log OK";
            case ElectronicValueRWConst.EVRW_SUE_LS_NEARFULL:
                return "ElectronicValueRW Dealing Log Nearly Full";
            case ElectronicValueRWConst.EVRW_SUE_LS_FULL:
                return "ElectronicValueRW Dealing Log Full";
            case ElectronicValueRWConst.EVRW_SUE_DS_NOCARD:
                return "ElectronicValueRW Detected NOCARD";
            case ElectronicValueRWConst.EVRW_SUE_DS_DETECTED:
                return "ElectronicValueRW Detected Card";
            case ElectronicValueRWConst.EVRW_SUE_DS_ENTERED:
                return "ElectronicValueRW Detected Card Entered";
            case ElectronicValueRWConst.EVRW_SUE_DS_CAPTURED:
                return "ElectronicValueRW Detected Card Captured";
        }
        return "Unknown ElectronicValueRW Status Change: " + getStatus();
    }
}
