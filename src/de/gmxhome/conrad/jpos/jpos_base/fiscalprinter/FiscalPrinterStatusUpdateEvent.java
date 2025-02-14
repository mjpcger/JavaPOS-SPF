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
        case FPTR_SUE_COVER_OK:
            props.CoverOpen = false;
            break;
        case FPTR_SUE_COVER_OPEN:
            props.CoverOpen = true;
            break;
        case FPTR_SUE_JRN_COVER_OK:
        case FPTR_SUE_JRN_COVER_OPEN:
        case FPTR_SUE_REC_COVER_OK:
        case FPTR_SUE_REC_COVER_OPEN:
        case FPTR_SUE_SLP_COVER_OK:
        case FPTR_SUE_SLP_COVER_OPEN:
            break;
        case FPTR_SUE_IDLE:
            props.State = JPOS_S_IDLE;
            break;
        case FPTR_SUE_JRN_EMPTY:
        case FPTR_SUE_SLP_EMPTY:
            props.JrnEmpty = true;
            break;
        case FPTR_SUE_JRN_NEAREMPTY:
            props.JrnNearEnd = true;
            props.JrnEmpty = false;
            break;
        case FPTR_SUE_JRN_PAPEROK:
        case FPTR_SUE_SLP_PAPEROK:
            props.JrnEmpty = props.JrnNearEnd = false;
            break;
        case FPTR_SUE_REC_EMPTY:
            props.RecEmpty = true;
            break;
        case FPTR_SUE_REC_NEAREMPTY:
            props.RecNearEnd = true;
            props.RecEmpty = false;
            break;
        case FPTR_SUE_REC_PAPEROK:
            props.RecEmpty = props.RecNearEnd = false;
            break;
        case FPTR_SUE_SLP_NEAREMPTY:
            props.JrnNearEnd = true;
            break;
        default:
            return false;
        }
        props.signalWaiter();
        return true;
    }

    @Override
    public boolean checkStatusCorresponds() {
        FiscalPrinterProperties props = (FiscalPrinterProperties)getPropertySet();
        if (super.checkStatusCorresponds())
            return true;
        switch (getStatus()) {
        case FPTR_SUE_JRN_COVER_OK:
        case FPTR_SUE_REC_COVER_OK:
        case FPTR_SUE_SLP_COVER_OK:
        case FPTR_SUE_COVER_OK:
            return !props.CoverOpen;
        case FPTR_SUE_JRN_COVER_OPEN:
        case FPTR_SUE_REC_COVER_OPEN:
        case FPTR_SUE_SLP_COVER_OPEN:
        case FPTR_SUE_COVER_OPEN:
            return props.CoverOpen;
        case FPTR_SUE_IDLE:
            return true;
        case FPTR_SUE_JRN_EMPTY:
        case FPTR_SUE_SLP_EMPTY:
            return props.JrnEmpty;
        case FPTR_SUE_JRN_NEAREMPTY:
            return props.JrnNearEnd && !props.JrnEmpty;
        case FPTR_SUE_JRN_PAPEROK:
        case FPTR_SUE_SLP_PAPEROK:
            return !props.JrnEmpty && !props.JrnNearEnd;
        case FPTR_SUE_REC_EMPTY:
            return props.RecEmpty;
        case FPTR_SUE_REC_NEAREMPTY:
            return props.RecNearEnd && !props.RecEmpty;
        case FPTR_SUE_REC_PAPEROK:
            return !props.RecEmpty && !props.RecNearEnd;
        case FPTR_SUE_SLP_NEAREMPTY:
            return props.JrnNearEnd;
        }
        return false;
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
        if (propertiesHaveBeenChanged(propnames, oldvals))
            return true;
        switch (getStatus()) {
        case FPTR_SUE_JRN_COVER_OK:
        case FPTR_SUE_JRN_COVER_OPEN:
        case FPTR_SUE_REC_COVER_OK:
        case FPTR_SUE_REC_COVER_OPEN:
        case FPTR_SUE_SLP_COVER_OK:
        case FPTR_SUE_SLP_COVER_OPEN:
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
        case FPTR_SUE_COVER_OK:
            return "Cover OK";
        case FPTR_SUE_COVER_OPEN:
            return "Cover open";
        case FPTR_SUE_IDLE:
            return "Fiscal printer idle";
        case FPTR_SUE_JRN_COVER_OK:
            return "Journal cover OK";
        case FPTR_SUE_JRN_COVER_OPEN:
            return "Journal cover open";
        case FPTR_SUE_JRN_EMPTY:
            return "Journal paper empty";
        case FPTR_SUE_JRN_NEAREMPTY:
            return "Journal paper near end";
        case FPTR_SUE_JRN_PAPEROK:
            return "Journal paper OK";
        case FPTR_SUE_REC_COVER_OK:
            return "Receipt cover OK";
        case FPTR_SUE_REC_COVER_OPEN:
            return "Receipt cover open";
        case FPTR_SUE_REC_EMPTY:
            return "Receipt paper empty";
        case FPTR_SUE_REC_NEAREMPTY:
            return "Receipt paper near end";
        case FPTR_SUE_REC_PAPEROK:
            return "Receipt paper OK";
        case FPTR_SUE_SLP_COVER_OK:
            return "Slip cover OK";
        case FPTR_SUE_SLP_COVER_OPEN:
            return "Slip cover open";
        case FPTR_SUE_SLP_EMPTY:
            return "Slip paper empty";
        case FPTR_SUE_SLP_NEAREMPTY:
            return "Slip paper near end";
        case FPTR_SUE_SLP_PAPEROK:
            return "Slip paper OK";
        }
        return "Unknown Status Change: " + getStatus();
    }
}
