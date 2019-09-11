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
import jpos.*;

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
    public JposStatusUpdateEvent copyEvent(JposBase o) {
        return new FiscalPrinterStatusUpdateEvent(o, getStatus());
    }

    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        FiscalPrinterProperties props = (FiscalPrinterProperties)getPropertySet();
        switch (getStatus()) {
            case FiscalPrinterConst.FPTR_SUE_COVER_OK:
                props.CoverOpen = false;
                break;
            case FiscalPrinterConst.FPTR_SUE_COVER_OPEN:
                props.CoverOpen = true;
            case FiscalPrinterConst.FPTR_SUE_JRN_COVER_OK:
            case FiscalPrinterConst.FPTR_SUE_JRN_COVER_OPEN:
            case FiscalPrinterConst.FPTR_SUE_REC_COVER_OK:
            case FiscalPrinterConst.FPTR_SUE_REC_COVER_OPEN:
            case FiscalPrinterConst.FPTR_SUE_SLP_COVER_OK:
            case FiscalPrinterConst.FPTR_SUE_SLP_COVER_OPEN:
                break;
            case FiscalPrinterConst.FPTR_SUE_IDLE:
                props.State = JposConst.JPOS_S_IDLE;
                break;
            case FiscalPrinterConst.FPTR_SUE_JRN_EMPTY:
                props.JrnEmpty = true;
                break;
            case FiscalPrinterConst.FPTR_SUE_JRN_NEAREMPTY:
                props.JrnNearEnd = true;
                props.JrnEmpty = false;
                break;
            case FiscalPrinterConst.FPTR_SUE_JRN_PAPEROK:
                props.JrnEmpty = props.JrnNearEnd = false;
                break;
            case FiscalPrinterConst.FPTR_SUE_REC_EMPTY:
                props.RecEmpty = true;
                break;
            case FiscalPrinterConst.FPTR_SUE_REC_NEAREMPTY:
                props.RecNearEnd = true;
                props.RecEmpty = false;
                break;
            case FiscalPrinterConst.FPTR_SUE_REC_PAPEROK:
                props.RecEmpty = props.RecNearEnd = false;
                break;
            case FiscalPrinterConst.FPTR_SUE_SLP_EMPTY:
                props.JrnEmpty = true;
                break;
            case FiscalPrinterConst.FPTR_SUE_SLP_NEAREMPTY:
                props.JrnNearEnd = true;
                break;
            case FiscalPrinterConst.FPTR_SUE_SLP_PAPEROK:
                props.JrnEmpty = props.JrnNearEnd = false;
                break;
            default:
                return false;
        }
        props.signalWaiter();
        return true;
    }

    @Override
    public boolean checkStatusCorresponds() {
        if (super.checkStatusCorresponds())
            return true;
        FiscalPrinterProperties props = (FiscalPrinterProperties)getPropertySet();
        switch (getStatus()) {
            case FiscalPrinterConst.FPTR_SUE_JRN_COVER_OK:
            case FiscalPrinterConst.FPTR_SUE_REC_COVER_OK:
            case FiscalPrinterConst.FPTR_SUE_SLP_COVER_OK:
            case FiscalPrinterConst.FPTR_SUE_COVER_OK:
                return props.CoverOpen == false;
            case FiscalPrinterConst.FPTR_SUE_JRN_COVER_OPEN:
            case FiscalPrinterConst.FPTR_SUE_REC_COVER_OPEN:
            case FiscalPrinterConst.FPTR_SUE_SLP_COVER_OPEN:
            case FiscalPrinterConst.FPTR_SUE_COVER_OPEN:
                return props.CoverOpen == true;
            case FiscalPrinterConst.FPTR_SUE_IDLE:
                return true;
            case FiscalPrinterConst.FPTR_SUE_JRN_EMPTY:
                return props.JrnEmpty == true;
            case FiscalPrinterConst.FPTR_SUE_JRN_NEAREMPTY:
                return props.JrnNearEnd == true && props.JrnEmpty == false;
            case FiscalPrinterConst.FPTR_SUE_JRN_PAPEROK:
                return props.JrnEmpty == false && props.JrnNearEnd == false;
            case FiscalPrinterConst.FPTR_SUE_REC_EMPTY:
                return props.RecEmpty == true;
            case FiscalPrinterConst.FPTR_SUE_REC_NEAREMPTY:
                return props.RecNearEnd == true && props.RecEmpty == false;
            case FiscalPrinterConst.FPTR_SUE_REC_PAPEROK:
                return props.RecEmpty == false && props.RecNearEnd == false;
            case FiscalPrinterConst.FPTR_SUE_SLP_EMPTY:
                return props.JrnEmpty == true;
            case FiscalPrinterConst.FPTR_SUE_SLP_NEAREMPTY:
                return props.JrnNearEnd == true;
            case FiscalPrinterConst.FPTR_SUE_SLP_PAPEROK:
                return props.JrnEmpty == false && props.JrnNearEnd == false;
        }
        return false;
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        FiscalPrinterProperties props = (FiscalPrinterProperties)getPropertySet();
        int state = props.State;
        boolean cover = props.CoverOpen;
        boolean jend = props.JrnEmpty;
        boolean jlow = props.JrnNearEnd;
        boolean rend = props.RecEmpty;
        boolean rlow = props.RecNearEnd;
        boolean send = props.SlpEmpty;
        boolean slow = props.SlpNearEnd;
        if (super.setAndCheckStatusProperties())
            return true;
        if (state != props.State)
            props.EventSource.logSet("State");
        if (   cover != props.CoverOpen ||
                jend != props.JrnEmpty ||
                jlow != props.JrnNearEnd ||
                rend != props.RecEmpty ||
                rlow != props.RecNearEnd ||
                send != props.SlpEmpty ||
                slow != props.SlpNearEnd)
        {
            if (cover != props.CoverOpen)
                props.EventSource.logSet("CoverOpen");
            if (jend != props.JrnEmpty)
                props.EventSource.logSet("JrnEmpty");
            if (jlow != props.JrnNearEnd)
                props.EventSource.logSet("JrnNearEnd");
            if (rend != props.RecEmpty)
                props.EventSource.logSet("RecEmpty");
            if (rlow != props.RecNearEnd)
                props.EventSource.logSet("RecNearEnd");
            if (send != props.SlpEmpty)
                props.EventSource.logSet("SlpEmpty");
            if (slow != props.SlpNearEnd)
                props.EventSource.logSet("SlpNearEnd");
            return true;
        }
        switch (getStatus()) {
            case FiscalPrinterConst.FPTR_SUE_JRN_COVER_OK:
            case FiscalPrinterConst.FPTR_SUE_JRN_COVER_OPEN:
            case FiscalPrinterConst.FPTR_SUE_REC_COVER_OK:
            case FiscalPrinterConst.FPTR_SUE_REC_COVER_OPEN:
            case FiscalPrinterConst.FPTR_SUE_SLP_COVER_OK:
            case FiscalPrinterConst.FPTR_SUE_SLP_COVER_OPEN:
            case FiscalPrinterConst.FPTR_SUE_IDLE:
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
            case FiscalPrinterConst.FPTR_SUE_COVER_OK:
                return "Cover OK";
            case FiscalPrinterConst.FPTR_SUE_COVER_OPEN:
                return "Cover open";
            case FiscalPrinterConst.FPTR_SUE_IDLE:
                return "Fiscal printer idle";
            case FiscalPrinterConst.FPTR_SUE_JRN_COVER_OK:
                return "Journal cover OK";
            case FiscalPrinterConst.FPTR_SUE_JRN_COVER_OPEN:
                return "Journal cover open";
            case FiscalPrinterConst.FPTR_SUE_JRN_EMPTY:
                return "Journal paper empty";
            case FiscalPrinterConst.FPTR_SUE_JRN_NEAREMPTY:
                return "Journal paper near end";
            case FiscalPrinterConst.FPTR_SUE_JRN_PAPEROK:
                return "Journal paper OK";
            case FiscalPrinterConst.FPTR_SUE_REC_COVER_OK:
                return "Receipt cover OK";
            case FiscalPrinterConst.FPTR_SUE_REC_COVER_OPEN:
                return "Receipt cover open";
            case FiscalPrinterConst.FPTR_SUE_REC_EMPTY:
                return "Receipt paper empty";
            case FiscalPrinterConst.FPTR_SUE_REC_NEAREMPTY:
                return "Receipt paper near end";
            case FiscalPrinterConst.FPTR_SUE_REC_PAPEROK:
                return "Receipt paper OK";
            case FiscalPrinterConst.FPTR_SUE_SLP_COVER_OK:
                return "Slip cover OK";
            case FiscalPrinterConst.FPTR_SUE_SLP_COVER_OPEN:
                return "Slip cover open";
            case FiscalPrinterConst.FPTR_SUE_SLP_EMPTY:
                return "Slip paper empty";
            case FiscalPrinterConst.FPTR_SUE_SLP_NEAREMPTY:
                return "Slip paper near end";
            case FiscalPrinterConst.FPTR_SUE_SLP_PAPEROK:
                return "Slip paper OK";
        }
        return "Unknown Status Change: " + getStatus();
    }
}
