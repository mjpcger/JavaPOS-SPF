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

package de.gmxhome.conrad.jpos.jpos_base.itemdispenser;

import de.gmxhome.conrad.jpos.jpos_base.JposBase;
import de.gmxhome.conrad.jpos.jpos_base.JposStatusUpdateEvent;
import jpos.ItemDispenserConst;

/**
 * Status update event implementation for ItemDispenser devices.
 */
public class ItemDispenserStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Constructor, Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (itemdispenser.)ItemDispenserService object.
     * @param state  Status, see UPOS specification, chapter Item Dispenser - Events - StatusUpdateEvent.
     */
    public ItemDispenserStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
    }

    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        ItemDispenserProperties props = (ItemDispenserProperties)getPropertySet();
        switch (getStatus()) {
            case ItemDispenserConst.ITEM_SUE_EMPTY:
                props.DispenserStatus = ItemDispenserConst.ITEM_DS_EMPTY;
                props.signalWaiter();
                return true;
            case ItemDispenserConst.ITEM_SUE_NEAREMPTY:
                props.DispenserStatus = ItemDispenserConst.ITEM_DS_NEAREMPTY;
                props.signalWaiter();
                return true;
            case ItemDispenserConst.ITEM_SUE_OK:
                props.DispenserStatus = ItemDispenserConst.ITEM_DS_OK;
                props.signalWaiter();
                return true;
            case ItemDispenserConst.ITEM_SUE_JAM:
                props.DispenserStatus = ItemDispenserConst.ITEM_DS_JAM;
                props.signalWaiter();
                return true;
        }
        return false;
    }

    @Override
    public boolean checkStatusCorresponds() {
        if (super.checkStatusCorresponds())
            return true;
        ItemDispenserProperties props = (ItemDispenserProperties)getPropertySet();
        switch (getStatus()) {
            case ItemDispenserConst.ITEM_SUE_EMPTY:
                return props.DispenserStatus == ItemDispenserConst.ITEM_DS_EMPTY;
            case ItemDispenserConst.ITEM_SUE_NEAREMPTY:
                return props.DispenserStatus == ItemDispenserConst.ITEM_DS_NEAREMPTY;
            case ItemDispenserConst.ITEM_SUE_OK:
                return props.DispenserStatus == ItemDispenserConst.ITEM_DS_OK;
            case ItemDispenserConst.ITEM_SUE_JAM:
                return props.DispenserStatus == ItemDispenserConst.ITEM_DS_JAM;
        }
        return false;
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        ItemDispenserProperties props = (ItemDispenserProperties)getPropertySet();
        int status = props.DispenserStatus;
        if (super.setAndCheckStatusProperties())
            return true;
        if (status != props.DispenserStatus) {
            props.EventSource.logSet("GateStatus");
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
            case ItemDispenserConst.ITEM_SUE_EMPTY:
                return "Item Dispenser Empty";
            case ItemDispenserConst.ITEM_SUE_NEAREMPTY:
                return "Item Dispenser Near Empty";
            case ItemDispenserConst.ITEM_SUE_OK:
                return "Item Dispenser OK";
            case ItemDispenserConst.ITEM_SUE_JAM:
                return "Item Dispenser Jam";
        }
        return "Unknown Status Change: " + getStatus();
    }
}
