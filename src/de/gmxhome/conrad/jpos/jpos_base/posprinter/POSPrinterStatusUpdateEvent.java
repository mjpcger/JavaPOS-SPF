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

import static jpos.JposConst.*;
import static jpos.POSPrinterConst.*;

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
            case PTR_SUE_COVER_OK:
                props.CoverOpen = false;
                props.signalWaiter();
                return true;
            case PTR_SUE_COVER_OPEN:
                props.CoverOpen = true;
            case PTR_SUE_JRN_COVER_OK:
            case PTR_SUE_JRN_COVER_OPEN:
            case PTR_SUE_REC_COVER_OK:
            case PTR_SUE_REC_COVER_OPEN:
            case PTR_SUE_SLP_COVER_OK:
            case PTR_SUE_SLP_COVER_OPEN:
                props.signalWaiter();
                return true;
            case PTR_SUE_IDLE:
                props.State = JPOS_S_IDLE;
                props.signalWaiter();
                return true;
            case PTR_SUE_JRN_CARTDRIGE_OK:
                props.JrnCartridgeState = PTR_CART_OK;
                props.signalWaiter();
                return true;
            case PTR_SUE_JRN_CARTRIDGE_EMPTY:
                props.JrnCartridgeState = PTR_CART_EMPTY;
                props.signalWaiter();
                return true;
            case PTR_SUE_JRN_CARTRIDGE_NEAREMPTY:
                props.JrnCartridgeState = PTR_CART_NEAREND;
                props.signalWaiter();
                return true;
            case PTR_SUE_JRN_EMPTY:
                props.JrnEmpty = true;
                props.signalWaiter();
                return true;
            case PTR_SUE_JRN_HEAD_CLEANING:
                props.JrnCartridgeState = PTR_CART_CLEANING;
                props.signalWaiter();
                return true;
            case PTR_SUE_JRN_NEAREMPTY:
                props.JrnNearEnd = true;
                props.JrnEmpty = false;
                props.signalWaiter();
                return true;
            case PTR_SUE_JRN_PAPEROK:
                props.JrnEmpty = props.JrnNearEnd = false;
                props.signalWaiter();
                return true;
            case PTR_SUE_REC_CARTDRIGE_OK:
                props.RecCartridgeState = PTR_CART_OK;
                props.signalWaiter();
                return true;
            case PTR_SUE_REC_CARTRIDGE_EMPTY:
                props.RecCartridgeState = PTR_CART_EMPTY;
                props.signalWaiter();
                return true;
            case PTR_SUE_REC_CARTRIDGE_NEAREMPTY:
                props.RecCartridgeState = PTR_CART_NEAREND;
                props.signalWaiter();
                return true;
            case PTR_SUE_REC_EMPTY:
                props.RecEmpty = true;
                props.signalWaiter();
                return true;
            case PTR_SUE_REC_HEAD_CLEANING:
                props.RecCartridgeState = PTR_CART_CLEANING;
                props.signalWaiter();
                return true;
            case PTR_SUE_REC_NEAREMPTY:
                props.RecNearEnd = true;
                props.RecEmpty = false;
                props.signalWaiter();
                return true;
            case PTR_SUE_REC_PAPEROK:
                props.RecEmpty = props.RecNearEnd = false;
                props.signalWaiter();
                return true;
            case PTR_SUE_SLP_CARTDRIGE_OK:
                props.SlpCartridgeState = PTR_CART_OK;
                props.signalWaiter();
                return true;
            case PTR_SUE_SLP_CARTRIDGE_EMPTY:
                props.SlpCartridgeState = PTR_CART_EMPTY;
                props.signalWaiter();
                return true;
            case PTR_SUE_SLP_CARTRIDGE_NEAREMPTY:
                props.SlpCartridgeState = PTR_CART_NEAREND;
                props.signalWaiter();
                return true;
            case PTR_SUE_SLP_EMPTY:
                props.SlpEmpty = true;
                props.signalWaiter();
                return true;
            case PTR_SUE_SLP_HEAD_CLEANING:
                props.SlpCartridgeState = PTR_CART_CLEANING;
                props.signalWaiter();
                return true;
            case PTR_SUE_SLP_NEAREMPTY:
                props.SlpNearEnd = true;
                props.signalWaiter();
                return true;
            case PTR_SUE_SLP_PAPEROK:
                props.SlpEmpty = props.SlpNearEnd = false;
                props.signalWaiter();
                return true;
        }
        return false;
    }

    @Override
    public boolean checkStatusCorresponds() {
        POSPrinterProperties props = (POSPrinterProperties)getPropertySet();
        return super.checkStatusCorresponds() || switch (getStatus()) {
            case PTR_SUE_COVER_OK -> !props.CoverOpen;
            case PTR_SUE_COVER_OPEN -> props.CoverOpen;
            case PTR_SUE_JRN_COVER_OK, PTR_SUE_JRN_COVER_OPEN,
                    PTR_SUE_REC_COVER_OK, PTR_SUE_REC_COVER_OPEN,
                    PTR_SUE_SLP_COVER_OK, PTR_SUE_SLP_COVER_OPEN ->
                    true;
            case PTR_SUE_IDLE -> props.State == JPOS_S_IDLE;
            case PTR_SUE_JRN_CARTDRIGE_OK -> props.JrnCartridgeState == PTR_CART_OK;
            case PTR_SUE_JRN_CARTRIDGE_EMPTY ->
                    props.JrnCartridgeState == PTR_CART_EMPTY;
            case PTR_SUE_JRN_CARTRIDGE_NEAREMPTY ->
                    props.JrnCartridgeState == PTR_CART_NEAREND;
            case PTR_SUE_JRN_EMPTY -> props.JrnEmpty;
            case PTR_SUE_JRN_HEAD_CLEANING ->
                    props.JrnCartridgeState == PTR_CART_CLEANING;
            case PTR_SUE_JRN_NEAREMPTY -> props.JrnNearEnd && !props.JrnEmpty;
            case PTR_SUE_JRN_PAPEROK -> !props.JrnEmpty && !props.JrnNearEnd;
            case PTR_SUE_REC_CARTDRIGE_OK -> props.RecCartridgeState == PTR_CART_OK;
            case PTR_SUE_REC_CARTRIDGE_EMPTY ->
                    props.RecCartridgeState == PTR_CART_EMPTY;
            case PTR_SUE_REC_CARTRIDGE_NEAREMPTY ->
                    props.RecCartridgeState == PTR_CART_NEAREND;
            case PTR_SUE_REC_EMPTY -> props.RecEmpty;
            case PTR_SUE_REC_HEAD_CLEANING ->
                    props.RecCartridgeState == PTR_CART_CLEANING;
            case PTR_SUE_REC_NEAREMPTY -> props.RecNearEnd && !props.RecEmpty;
            case PTR_SUE_REC_PAPEROK -> !props.RecEmpty && !props.RecNearEnd;
            case PTR_SUE_SLP_CARTDRIGE_OK -> props.SlpCartridgeState == PTR_CART_OK;
            case PTR_SUE_SLP_CARTRIDGE_EMPTY ->
                    props.SlpCartridgeState == PTR_CART_EMPTY;
            case PTR_SUE_SLP_CARTRIDGE_NEAREMPTY ->
                    props.SlpCartridgeState == PTR_CART_NEAREND;
            case PTR_SUE_SLP_EMPTY -> props.SlpEmpty;
            case PTR_SUE_SLP_HEAD_CLEANING ->
                    props.SlpCartridgeState == PTR_CART_CLEANING;
            case PTR_SUE_SLP_NEAREMPTY -> props.SlpNearEnd;
            case PTR_SUE_SLP_PAPEROK -> !props.SlpEmpty && !props.SlpNearEnd;
            default -> false;
        };
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
        return propertiesHaveBeenChanged(propnames, oldvals) || switch (getStatus()) {
            case PTR_SUE_JRN_COVER_OK, PTR_SUE_JRN_COVER_OPEN,
                    PTR_SUE_REC_COVER_OK, PTR_SUE_REC_COVER_OPEN,
                    PTR_SUE_SLP_COVER_OK, PTR_SUE_SLP_COVER_OPEN ->
                    true;
            default -> false;
        };
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        return ret.length() > 0 ? ret : switch (getStatus()) {
            case PTR_SUE_COVER_OK -> "Cover OK";
            case PTR_SUE_COVER_OPEN -> "Cover Open";
            case PTR_SUE_JRN_CARTDRIGE_OK -> "Journal Cartridge OK";
            case PTR_SUE_JRN_CARTRIDGE_EMPTY -> "Journal Cartridge Empty";
            case PTR_SUE_JRN_CARTRIDGE_NEAREMPTY -> "Journal Cartridge Nearly Empty";
            case PTR_SUE_JRN_COVER_OK -> "Journal Cover OK";
            case PTR_SUE_JRN_COVER_OPEN -> "Journal Cover Open";
            case PTR_SUE_JRN_EMPTY -> "Journal Paper empty";
            case PTR_SUE_JRN_HEAD_CLEANING -> "Journal Head Cleaning";
            case PTR_SUE_JRN_NEAREMPTY -> "Journal Paper Nearly Empty";
            case PTR_SUE_JRN_PAPEROK -> "Journal Paper OK";
            case PTR_SUE_REC_CARTDRIGE_OK -> "Receipt Cartridge OK";
            case PTR_SUE_REC_CARTRIDGE_EMPTY -> "Receipt Cartridge Empty";
            case PTR_SUE_REC_CARTRIDGE_NEAREMPTY -> "Receipt Cartridge Nearly Empty";
            case PTR_SUE_REC_COVER_OK -> "Receipt Cover OK";
            case PTR_SUE_REC_COVER_OPEN -> "Receipt Cover Open";
            case PTR_SUE_REC_EMPTY -> "Receipt Paper Empty";
            case PTR_SUE_REC_HEAD_CLEANING -> "Receipt Head Cleaning";
            case PTR_SUE_REC_NEAREMPTY -> "Receipt Paper Nearly Empty";
            case PTR_SUE_REC_PAPEROK -> "Receipt Paper OK";
            case PTR_SUE_SLP_CARTDRIGE_OK -> "Slip Cartridge OK";
            case PTR_SUE_SLP_CARTRIDGE_EMPTY -> "Slip Cartridge Empty";
            case PTR_SUE_SLP_CARTRIDGE_NEAREMPTY -> "Slip Cartridge Nearly Empty";
            case PTR_SUE_SLP_COVER_OK -> "Slip Cover OK";
            case PTR_SUE_SLP_COVER_OPEN -> "Slip Cover Open";
            case PTR_SUE_SLP_EMPTY -> "Slip Paper Empty";
            case PTR_SUE_SLP_HEAD_CLEANING -> "Slip Head Cleaning";
            case PTR_SUE_SLP_NEAREMPTY -> "Slip Paper Nearly Empty";
            case PTR_SUE_SLP_PAPEROK -> "Slip Paper OK";
            default -> "Unknown Status Change: " + getStatus();
        };
    }
}
