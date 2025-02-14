/*
 * Copyright 2019 Martin Conrad
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
 */

package de.gmxhome.conrad.jpos.jpos_base.cashdrawer;

import de.gmxhome.conrad.jpos.jpos_base.*;

import static jpos.CashDrawerConst.*;

/**
 * Status update event implementation for CashDrawer devices.
 */
public class CashDrawerStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Constructor, see JposStatusUpdateEvent
     *
     * @param source Source, for services implemented with this framework, the (cashdrawer.)CashDrawerService object.
     * @param state  New status value, see UPOS specification, chapter Cash Drawer - Events - StatusUpdateEvent.
     */
    public CashDrawerStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
    }

    @Override
    public JposStatusUpdateEvent copyEvent(JposBase o) {
        return new CashDrawerStatusUpdateEvent(o, getStatus());
    }

    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        CashDrawerProperties props = (CashDrawerProperties)getPropertySet();
        switch (getStatus()) {
            case CASH_SUE_DRAWERCLOSED:
            case CASH_SUE_DRAWEROPEN:
                props.DrawerOpened = getStatus() == CASH_SUE_DRAWEROPEN;
                props.signalWaiter();
                return true;
        }
        return false;
    }

    @Override
    public boolean checkStatusCorresponds() {
        CashDrawerProperties props = (CashDrawerProperties)getPropertySet();
        if (super.checkStatusCorresponds())
            return true;
        switch (getStatus()) {
        case CASH_SUE_DRAWERCLOSED:
        case CASH_SUE_DRAWEROPEN:
            return props.DrawerOpened == (getStatus() == CASH_SUE_DRAWEROPEN);
        }
        return false;
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        CashDrawerProperties props = (CashDrawerProperties)getPropertySet();
        boolean status = props.DrawerOpened;
        if (super.setAndCheckStatusProperties())
            return true;
        if (status != props.DrawerOpened) {
            props.EventSource.logSet("DrawerOpened");
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
        case CASH_SUE_DRAWERCLOSED:
            return  "CashDrawer Closed";
        case CASH_SUE_DRAWEROPEN:
            return  "CashDrawer Opened";
        }
        return  "Unknown Status Change: " + getStatus();
    }
}
