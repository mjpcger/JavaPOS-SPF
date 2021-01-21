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

package de.gmxhome.conrad.jpos.jpos_base.posprinter;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Status update event implementation for POSPrinter devices.
 */
public class POSPrinterStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Constructor, Parameters passed to base class unchanged.
     *
     * @param source    Source, for services implemented with this framework, the (posprinter.)POSPrinterService object.
     * @param state     Status, see UPOS specification, chapter POS Printer - Events - StatusUpdateEvent.
     */
    public POSPrinterStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
    }

    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        POSPrinterProperties props = (POSPrinterProperties)getPropertySet();
        switch (getStatus()) {
            case POSPrinterConst.PTR_SUE_COVER_OK:
                props.CoverOpen = false;
                props.signalWaiter();
                return true;
            case POSPrinterConst.PTR_SUE_COVER_OPEN:
                props.CoverOpen = true;
            case POSPrinterConst.PTR_SUE_JRN_COVER_OK:
            case POSPrinterConst.PTR_SUE_JRN_COVER_OPEN:
            case POSPrinterConst.PTR_SUE_REC_COVER_OK:
            case POSPrinterConst.PTR_SUE_REC_COVER_OPEN:
            case POSPrinterConst.PTR_SUE_SLP_COVER_OK:
            case POSPrinterConst.PTR_SUE_SLP_COVER_OPEN:
                props.signalWaiter();
                return true;
            case POSPrinterConst.PTR_SUE_IDLE:
                props.State = JposConst.JPOS_S_IDLE;
                props.signalWaiter();
                return true;
            case POSPrinterConst.PTR_SUE_JRN_CARTDRIGE_OK:
                props.JrnCartridgeState = POSPrinterConst.PTR_CART_OK;
                props.signalWaiter();
                return true;
            case POSPrinterConst.PTR_SUE_JRN_CARTRIDGE_EMPTY:
                props.JrnCartridgeState = POSPrinterConst.PTR_CART_EMPTY;
                props.signalWaiter();
                return true;
            case POSPrinterConst.PTR_SUE_JRN_CARTRIDGE_NEAREMPTY:
                props.JrnCartridgeState = POSPrinterConst.PTR_CART_NEAREND;
                props.signalWaiter();
                return true;
            case POSPrinterConst.PTR_SUE_JRN_EMPTY:
                props.JrnEmpty = true;
                props.signalWaiter();
                return true;
            case POSPrinterConst.PTR_SUE_JRN_HEAD_CLEANING:
                props.JrnCartridgeState = POSPrinterConst.PTR_CART_CLEANING;
                props.signalWaiter();
                return true;
            case POSPrinterConst.PTR_SUE_JRN_NEAREMPTY:
                props.JrnNearEnd = true;
                props.JrnEmpty = false;
                props.signalWaiter();
                return true;
            case POSPrinterConst.PTR_SUE_JRN_PAPEROK:
                props.JrnEmpty = props.JrnNearEnd = false;
                props.signalWaiter();
                return true;
            case POSPrinterConst.PTR_SUE_REC_CARTDRIGE_OK:
                props.RecCartridgeState = POSPrinterConst.PTR_CART_OK;
                props.signalWaiter();
                return true;
            case POSPrinterConst.PTR_SUE_REC_CARTRIDGE_EMPTY:
                props.RecCartridgeState = POSPrinterConst.PTR_CART_EMPTY;
                props.signalWaiter();
                return true;
            case POSPrinterConst.PTR_SUE_REC_CARTRIDGE_NEAREMPTY:
                props.RecCartridgeState = POSPrinterConst.PTR_CART_NEAREND;
                props.signalWaiter();
                return true;
            case POSPrinterConst.PTR_SUE_REC_EMPTY:
                props.RecEmpty = true;
                props.signalWaiter();
                return true;
            case POSPrinterConst.PTR_SUE_REC_HEAD_CLEANING:
                props.RecCartridgeState = POSPrinterConst.PTR_CART_CLEANING;
                props.signalWaiter();
                return true;
            case POSPrinterConst.PTR_SUE_REC_NEAREMPTY:
                props.RecNearEnd = true;
                props.RecEmpty = false;
                props.signalWaiter();
                return true;
            case POSPrinterConst.PTR_SUE_REC_PAPEROK:
                props.RecEmpty = props.RecNearEnd = false;
                props.signalWaiter();
                return true;
            case POSPrinterConst.PTR_SUE_SLP_CARTDRIGE_OK:
                props.SlpCartridgeState = POSPrinterConst.PTR_CART_OK;
                props.signalWaiter();
                return true;
            case POSPrinterConst.PTR_SUE_SLP_CARTRIDGE_EMPTY:
                props.SlpCartridgeState = POSPrinterConst.PTR_CART_EMPTY;
                props.signalWaiter();
                return true;
            case POSPrinterConst.PTR_SUE_SLP_CARTRIDGE_NEAREMPTY:
                props.SlpCartridgeState = POSPrinterConst.PTR_CART_NEAREND;
                props.signalWaiter();
                return true;
            case POSPrinterConst.PTR_SUE_SLP_EMPTY:
                props.SlpEmpty = true;
                props.signalWaiter();
                return true;
            case POSPrinterConst.PTR_SUE_SLP_HEAD_CLEANING:
                props.SlpCartridgeState = POSPrinterConst.PTR_CART_CLEANING;
                props.signalWaiter();
                return true;
            case POSPrinterConst.PTR_SUE_SLP_NEAREMPTY:
                props.SlpNearEnd = true;
                props.signalWaiter();
                return true;
            case POSPrinterConst.PTR_SUE_SLP_PAPEROK:
                props.SlpEmpty = props.SlpNearEnd = false;
                props.signalWaiter();
                return true;
        }
        return false;
    }

    @Override
    public boolean checkStatusCorresponds() {
        if (super.checkStatusCorresponds())
            return true;
        POSPrinterProperties props = (POSPrinterProperties)getPropertySet();
        switch (getStatus()) {
            case POSPrinterConst.PTR_SUE_COVER_OK:
                return props.CoverOpen == false;
            case POSPrinterConst.PTR_SUE_COVER_OPEN:
                return props.CoverOpen == true;
            case POSPrinterConst.PTR_SUE_JRN_COVER_OK:
            case POSPrinterConst.PTR_SUE_JRN_COVER_OPEN:
            case POSPrinterConst.PTR_SUE_REC_COVER_OK:
            case POSPrinterConst.PTR_SUE_REC_COVER_OPEN:
            case POSPrinterConst.PTR_SUE_SLP_COVER_OK:
            case POSPrinterConst.PTR_SUE_SLP_COVER_OPEN:
                return true;
            case POSPrinterConst.PTR_SUE_IDLE:
                return props.State == JposConst.JPOS_S_IDLE;
            case POSPrinterConst.PTR_SUE_JRN_CARTDRIGE_OK:
                return props.JrnCartridgeState == POSPrinterConst.PTR_CART_OK;
            case POSPrinterConst.PTR_SUE_JRN_CARTRIDGE_EMPTY:
                return props.JrnCartridgeState == POSPrinterConst.PTR_CART_EMPTY;
            case POSPrinterConst.PTR_SUE_JRN_CARTRIDGE_NEAREMPTY:
                return props.JrnCartridgeState == POSPrinterConst.PTR_CART_NEAREND;
            case POSPrinterConst.PTR_SUE_JRN_EMPTY:
                return props.JrnEmpty == true;
            case POSPrinterConst.PTR_SUE_JRN_HEAD_CLEANING:
                return props.JrnCartridgeState == POSPrinterConst.PTR_CART_CLEANING;
            case POSPrinterConst.PTR_SUE_JRN_NEAREMPTY:
                return props.JrnNearEnd == true && props.JrnEmpty == false;
            case POSPrinterConst.PTR_SUE_JRN_PAPEROK:
                return props.JrnEmpty == false && props.JrnNearEnd == false;
            case POSPrinterConst.PTR_SUE_REC_CARTDRIGE_OK:
                return props.RecCartridgeState == POSPrinterConst.PTR_CART_OK;
            case POSPrinterConst.PTR_SUE_REC_CARTRIDGE_EMPTY:
                return props.RecCartridgeState == POSPrinterConst.PTR_CART_EMPTY;
            case POSPrinterConst.PTR_SUE_REC_CARTRIDGE_NEAREMPTY:
                return props.RecCartridgeState == POSPrinterConst.PTR_CART_NEAREND;
            case POSPrinterConst.PTR_SUE_REC_EMPTY:
                return props.RecEmpty == true;
            case POSPrinterConst.PTR_SUE_REC_HEAD_CLEANING:
                return props.RecCartridgeState == POSPrinterConst.PTR_CART_CLEANING;
            case POSPrinterConst.PTR_SUE_REC_NEAREMPTY:
                return props.RecNearEnd == true && props.RecEmpty == false;
            case POSPrinterConst.PTR_SUE_REC_PAPEROK:
                return props.RecEmpty == false && props.RecNearEnd == false;
            case POSPrinterConst.PTR_SUE_SLP_CARTDRIGE_OK:
                return props.SlpCartridgeState == POSPrinterConst.PTR_CART_OK;
            case POSPrinterConst.PTR_SUE_SLP_CARTRIDGE_EMPTY:
                return props.SlpCartridgeState == POSPrinterConst.PTR_CART_EMPTY;
            case POSPrinterConst.PTR_SUE_SLP_CARTRIDGE_NEAREMPTY:
                return props.SlpCartridgeState == POSPrinterConst.PTR_CART_NEAREND;
            case POSPrinterConst.PTR_SUE_SLP_EMPTY:
                return props.SlpEmpty == true;
            case POSPrinterConst.PTR_SUE_SLP_HEAD_CLEANING:
                return props.SlpCartridgeState == POSPrinterConst.PTR_CART_CLEANING;
            case POSPrinterConst.PTR_SUE_SLP_NEAREMPTY:
                return props.SlpNearEnd == true;
            case POSPrinterConst.PTR_SUE_SLP_PAPEROK:
                return props.SlpEmpty == false && props.SlpNearEnd == false;
        }
        return false;
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        POSPrinterProperties props = (POSPrinterProperties)getPropertySet();
        int state = props.State;
        String[] propnames = {
                "CoverOpen",
                "JrnEmpty",
                "JrnNearEnd",
                "JrnCartridgeState",
                "RecEmpty",
                "RecNearEnd",
                "RecCartridgeState",
                "SlpEmpty",
                "SlpNearEnd",
                "SlpCartridgeState"
        };
        Object[] oldvals = getPropertyValues(propnames);
        if (super.setAndCheckStatusProperties())
            return true;
        if (state != props.State)
            props.EventSource.logSet("State");
        if (propertiesHaveBeenChanged(propnames, oldvals))
            return true;
        switch (getStatus()) {
            case POSPrinterConst.PTR_SUE_JRN_COVER_OK:
            case POSPrinterConst.PTR_SUE_JRN_COVER_OPEN:
            case POSPrinterConst.PTR_SUE_REC_COVER_OK:
            case POSPrinterConst.PTR_SUE_REC_COVER_OPEN:
            case POSPrinterConst.PTR_SUE_SLP_COVER_OK:
            case POSPrinterConst.PTR_SUE_SLP_COVER_OPEN:
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
            case POSPrinterConst.PTR_SUE_COVER_OK:
                return "Cover OK";
            case POSPrinterConst.PTR_SUE_COVER_OPEN:
                return "Cover Open";
            case POSPrinterConst.PTR_SUE_JRN_CARTDRIGE_OK:
                return "Journal Cartridge OK";
            case POSPrinterConst.PTR_SUE_JRN_CARTRIDGE_EMPTY:
                return "Journal Cartridge Empty";
            case POSPrinterConst.PTR_SUE_JRN_CARTRIDGE_NEAREMPTY:
                return "Journal Cartridge Nearly Empty";
            case POSPrinterConst.PTR_SUE_JRN_COVER_OK:
                return "Journal Cover OK";
            case POSPrinterConst.PTR_SUE_JRN_COVER_OPEN:
                return "Journal Cover Open";
            case POSPrinterConst.PTR_SUE_JRN_EMPTY:
                return "Journal Paper empty";
            case POSPrinterConst.PTR_SUE_JRN_HEAD_CLEANING:
                return "Journal Head Cleaning";
            case POSPrinterConst.PTR_SUE_JRN_NEAREMPTY:
                return "Journal Paper Nearly Empty";
            case POSPrinterConst.PTR_SUE_JRN_PAPEROK:
                return "Journal Paper OK";
            case POSPrinterConst.PTR_SUE_REC_CARTDRIGE_OK:
                return "Receipt Cartridge OK";
            case POSPrinterConst.PTR_SUE_REC_CARTRIDGE_EMPTY:
                return "Receipt Cartridge Empty";
            case POSPrinterConst.PTR_SUE_REC_CARTRIDGE_NEAREMPTY:
                return "Receipt Cartridge Nearly Empty";
            case POSPrinterConst.PTR_SUE_REC_COVER_OK:
                return "Receipt Cover OK";
            case POSPrinterConst.PTR_SUE_REC_COVER_OPEN:
                return "Receipt Cover Open";
            case POSPrinterConst.PTR_SUE_REC_EMPTY:
                return "Receipt Paper Empty";
            case POSPrinterConst.PTR_SUE_REC_HEAD_CLEANING:
                return "Receipt Head Cleaning";
            case POSPrinterConst.PTR_SUE_REC_NEAREMPTY:
                return "Receipt Paper Nearly Empty";
            case POSPrinterConst.PTR_SUE_REC_PAPEROK:
                return "Receipt Paper OK";
            case POSPrinterConst.PTR_SUE_SLP_CARTDRIGE_OK:
                return "Slip Cartridge OK";
            case POSPrinterConst.PTR_SUE_SLP_CARTRIDGE_EMPTY:
                return "Slip Cartridge Empty";
            case POSPrinterConst.PTR_SUE_SLP_CARTRIDGE_NEAREMPTY:
                return "Slip Cartridge Nearly Empty";
            case POSPrinterConst.PTR_SUE_SLP_COVER_OK:
                return "Slip Cover OK";
            case POSPrinterConst.PTR_SUE_SLP_COVER_OPEN:
                return "Slip Cover Open";
            case POSPrinterConst.PTR_SUE_SLP_EMPTY:
                return "Slip Paper Empty";
            case POSPrinterConst.PTR_SUE_SLP_HEAD_CLEANING:
                return "Slip Head Cleaning";
            case POSPrinterConst.PTR_SUE_SLP_NEAREMPTY:
                return "Slip Paper Nearly Empty";
            case POSPrinterConst.PTR_SUE_SLP_PAPEROK:
                return "Slip Paper OK";
        }
        return "Unknown Status Change: " + getStatus();
    }
}
