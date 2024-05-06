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

import static jpos.PointCardRWConst.*;

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
        PointCardRWProperties props = (PointCardRWProperties) getPropertySet();
        return super.setStatusProperties() || switch (getStatus()) {
            case PCRW_SUE_NOCARD -> extracted(props, PCRW_STATE_NOCARD);
            case PCRW_SUE_REMAINING -> extracted(props, PCRW_STATE_REMAINING);
            case PCRW_SUE_INRW -> extracted(props, PCRW_STATE_INRW);
            default -> false;
        };
    }

    private static boolean extracted(PointCardRWProperties props, int value) {
        int oldval = props.CardState;
        if (oldval != (props.CardState = value))
            props.EventSource.logSet("CardState");
        return true;
    }

    @Override
    public boolean checkStatusCorresponds() {
        PointCardRWProperties props = (PointCardRWProperties) getPropertySet();
         return super.checkStatusCorresponds() || switch (getStatus()) {
            case PCRW_SUE_NOCARD -> props.CardState == PCRW_STATE_NOCARD;
            case PCRW_SUE_REMAINING -> props.CardState == PCRW_STATE_REMAINING;
            case PCRW_SUE_INRW -> props.CardState == PCRW_STATE_INRW;
            default -> false;
        };
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        return ret.length() > 0 ? ret : switch (getStatus()) {
            case PCRW_SUE_NOCARD -> "No card present";
            case PCRW_SUE_REMAINING -> "Card in entrance";
            case PCRW_SUE_INRW -> "Card in device";
            default -> "Unknown Status Change: " + getStatus();
        };
    }
}
