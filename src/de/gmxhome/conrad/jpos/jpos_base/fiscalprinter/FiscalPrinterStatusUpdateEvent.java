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

package de.gmxhome.conrad.jpos.jpos_base.fiscalprinter;

import de.gmxhome.conrad.jpos.jpos_base.*;

import static jpos.FiscalPrinterConst.*;
import static jpos.JposConst.*;

/**
 * Status update event implementation for FiscalPrinter devices.
 */
public class FiscalPrinterStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Constructor, Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (fiscalprinter.)FiscalPrinterService object.
     * @param state  Status value, see UPOS specification, chapter Fiscal Printer - Events - StatusUpdateEvent.
     */
    public FiscalPrinterStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
    }

    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        FiscalPrinterProperties props = (FiscalPrinterProperties)getPropertySet();
        switch (getStatus()) {
            case FPTR_SUE_COVER_OK -> props.CoverOpen = false;
            case FPTR_SUE_COVER_OPEN -> props.CoverOpen = true;
            case FPTR_SUE_JRN_COVER_OK, FPTR_SUE_JRN_COVER_OPEN, FPTR_SUE_REC_COVER_OK, FPTR_SUE_REC_COVER_OPEN, FPTR_SUE_SLP_COVER_OK, FPTR_SUE_SLP_COVER_OPEN -> {
            }
            case FPTR_SUE_IDLE -> props.State = JPOS_S_IDLE;
            case FPTR_SUE_JRN_EMPTY, FPTR_SUE_SLP_EMPTY -> props.JrnEmpty = true;
            case FPTR_SUE_JRN_NEAREMPTY -> {
                props.JrnNearEnd = true;
                props.JrnEmpty = false;
            }
            case FPTR_SUE_JRN_PAPEROK, FPTR_SUE_SLP_PAPEROK -> props.JrnEmpty = props.JrnNearEnd = false;
            case FPTR_SUE_REC_EMPTY -> props.RecEmpty = true;
            case FPTR_SUE_REC_NEAREMPTY -> {
                props.RecNearEnd = true;
                props.RecEmpty = false;
            }
            case FPTR_SUE_REC_PAPEROK -> props.RecEmpty = props.RecNearEnd = false;
            case FPTR_SUE_SLP_NEAREMPTY -> props.JrnNearEnd = true;
            default -> { return false; }
        }
        props.signalWaiter();
        return true;
    }

    @Override
    public boolean checkStatusCorresponds() {
        FiscalPrinterProperties props = (FiscalPrinterProperties)getPropertySet();
        return super.checkStatusCorresponds() || switch (getStatus()) {
            case FPTR_SUE_JRN_COVER_OK, FPTR_SUE_REC_COVER_OK,
                    FPTR_SUE_SLP_COVER_OK, FPTR_SUE_COVER_OK ->
                    !props.CoverOpen;
            case FPTR_SUE_JRN_COVER_OPEN, FPTR_SUE_REC_COVER_OPEN,
                    FPTR_SUE_SLP_COVER_OPEN, FPTR_SUE_COVER_OPEN ->
                    props.CoverOpen;
            case FPTR_SUE_IDLE -> true;
            case FPTR_SUE_JRN_EMPTY, FPTR_SUE_SLP_EMPTY ->
                    props.JrnEmpty;
            case FPTR_SUE_JRN_NEAREMPTY -> props.JrnNearEnd && !props.JrnEmpty;
            case FPTR_SUE_JRN_PAPEROK, FPTR_SUE_SLP_PAPEROK ->
                    !props.JrnEmpty && !props.JrnNearEnd;
            case FPTR_SUE_REC_EMPTY -> props.RecEmpty;
            case FPTR_SUE_REC_NEAREMPTY -> props.RecNearEnd && !props.RecEmpty;
            case FPTR_SUE_REC_PAPEROK -> !props.RecEmpty && !props.RecNearEnd;
            case FPTR_SUE_SLP_NEAREMPTY -> props.JrnNearEnd;
            default -> false;
        };
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        String[] propnames = {
                "CoverOpen",
                "JrnEmpty",
                "JrnNearEnd",
                "RecEmpty",
                "RecNearEnd",
                "SlpEmpty",
                "SlpNearEnd"
        };
        Object[] oldvals = getPropertyValues(propnames);
        FiscalPrinterProperties props = (FiscalPrinterProperties)getPropertySet();
        int state = props.State;
        if (super.setAndCheckStatusProperties())
            return true;
        if (state != props.State)
            props.EventSource.logSet("State");
        return propertiesHaveBeenChanged(propnames, oldvals) || switch (getStatus()) {
            case FPTR_SUE_JRN_COVER_OK, FPTR_SUE_JRN_COVER_OPEN,
                    FPTR_SUE_REC_COVER_OK, FPTR_SUE_REC_COVER_OPEN,
                    FPTR_SUE_SLP_COVER_OK, FPTR_SUE_SLP_COVER_OPEN ->
                    true;
            default -> false;
        };
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        return ret.length() > 0 ? ret : switch (getStatus()) {
            case FPTR_SUE_COVER_OK -> "Cover OK";
            case FPTR_SUE_COVER_OPEN -> "Cover open";
            case FPTR_SUE_IDLE -> "Fiscal printer idle";
            case FPTR_SUE_JRN_COVER_OK -> "Journal cover OK";
            case FPTR_SUE_JRN_COVER_OPEN -> "Journal cover open";
            case FPTR_SUE_JRN_EMPTY -> "Journal paper empty";
            case FPTR_SUE_JRN_NEAREMPTY -> "Journal paper near end";
            case FPTR_SUE_JRN_PAPEROK -> "Journal paper OK";
            case FPTR_SUE_REC_COVER_OK -> "Receipt cover OK";
            case FPTR_SUE_REC_COVER_OPEN -> "Receipt cover open";
            case FPTR_SUE_REC_EMPTY -> "Receipt paper empty";
            case FPTR_SUE_REC_NEAREMPTY -> "Receipt paper near end";
            case FPTR_SUE_REC_PAPEROK -> "Receipt paper OK";
            case FPTR_SUE_SLP_COVER_OK -> "Slip cover OK";
            case FPTR_SUE_SLP_COVER_OPEN -> "Slip cover open";
            case FPTR_SUE_SLP_EMPTY -> "Slip paper empty";
            case FPTR_SUE_SLP_NEAREMPTY -> "Slip paper near end";
            case FPTR_SUE_SLP_PAPEROK -> "Slip paper OK";
            default -> "Unknown Status Change: " + getStatus();
        };
    }
}
