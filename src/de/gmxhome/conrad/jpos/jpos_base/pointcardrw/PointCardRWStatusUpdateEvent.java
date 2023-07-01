/*
 * Copyright 2023 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.pointcardrw;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Status update event implementation for PointCardRW devices.
 */
public class PointCardRWStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Constructor, Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     */
    public PointCardRWStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
    }

    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        PointCardRWProperties props = (PointCardRWProperties) getPropertySet();
        int val = props.CardState;
        switch (getStatus()) {
            case PointCardRWConst.PCRW_SUE_NOCARD:
                return extracted(props, PointCardRWConst.PCRW_STATE_NOCARD);
            case PointCardRWConst.PCRW_SUE_REMAINING:
                return extracted(props, PointCardRWConst.PCRW_STATE_REMAINING);
            case PointCardRWConst.PCRW_SUE_INRW:
                return extracted(props, PointCardRWConst.PCRW_STATE_INRW);
        }
        return false;
    }

    private static boolean extracted(PointCardRWProperties props, int value) {
        int oldval = props.CardState;
        if (oldval != (props.CardState = value))
            props.EventSource.logSet("CardState");
        return true;
    }

    @Override
    public boolean checkStatusCorresponds() {
        if (super.checkStatusCorresponds())
            return true;
        PointCardRWProperties props = (PointCardRWProperties) getPropertySet();
        switch (getStatus()) {
            case PointCardRWConst.PCRW_SUE_NOCARD:
                return (props.CardState == PointCardRWConst.PCRW_STATE_NOCARD);
            case PointCardRWConst.PCRW_SUE_REMAINING:
                return (props.CardState == PointCardRWConst.PCRW_STATE_REMAINING);
            case PointCardRWConst.PCRW_SUE_INRW:
                return (props.CardState == PointCardRWConst.PCRW_STATE_INRW);
        }
        return false;
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        if (ret.length() > 0)
            return ret;
        switch (getStatus()) {
            case PointCardRWConst.PCRW_SUE_NOCARD:
                return "No card present";
            case PointCardRWConst.PCRW_SUE_REMAINING:
                return "Card in entrance";
            case PointCardRWConst.PCRW_SUE_INRW:
                return "Card in device";
        }
        return "Unknown Status Change: "+ getStatus();
    }
}
