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

package de.gmxhome.conrad.jpos.jpos_base.smartcardrw;

import de.gmxhome.conrad.jpos.jpos_base.JposBase;
import de.gmxhome.conrad.jpos.jpos_base.JposStatusUpdateEvent;
import jpos.SmartCardRWConst;

/**
 * Status update event implementation for SmartCardRW devices.
 */
public class SmartCardRWStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Constructor, Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     */
    public SmartCardRWStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
    }

    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        SmartCardRWProperties props = (SmartCardRWProperties)getPropertySet();
        switch (getStatus()) {
            case SmartCardRWConst.SC_SUE_NO_CARD:
                props.signalWaiter();
                return true;
            case SmartCardRWConst.SC_SUE_CARD_PRESENT:
                props.signalWaiter();
                return true;
        }
        return false;
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        if (ret.length() > 0)
            return ret;
        switch (getStatus()) {
            case SmartCardRWConst.SC_SUE_NO_CARD:
                return "No Card";
            case SmartCardRWConst.SC_SUE_CARD_PRESENT:
                return "Card Present";
        }
        return "Unknown Status Change: " + getStatus();
    }
}
