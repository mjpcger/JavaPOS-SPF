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

package de.gmxhome.conrad.jpos.jpos_base.billdispenser;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.JposException;

import static jpos.BillDispenserConst.*;

/**
 * Output request executor for BillDispenser method DispenseCash.
 */
public class DispenseCash extends JposInputRequest {
    /**
     *  Get cashCounts parameter passed to DispenseCash method.
     * @return CashCounts parameter.
     */
    public String getCashCounts() {
        return CashCounts;
    }
    private final String CashCounts;

    /**
     * Gets CurrentExit property at the time when the request has been created.
     * @return  CurrentExit property.
     */
    public int getCurrentExit() {
        return CurrentExit;
    }
    private final int CurrentExit;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props      Property set of device service.
     * @param cashCounts The cashCounts parameter contains the dispensing cash units and counts.
     */
    public DispenseCash(JposCommonProperties props, String cashCounts) {
        super(props);
        CashCounts = cashCounts;
        CurrentExit = ((BillDispenserProperties)props).CurrentExit;
    }

    @Override
    public void invoke() throws JposException {
        ((BillDispenserService)Props.EventSource).BillDispenserInterface.dispenseCash(this);
    }

    private JposException TheException = null;

    @Override
    public JposErrorEvent createErrorEvent(JposException ex) {
        TheException = ex;
        return null;
    }

    @Override
    public JposStatusUpdateEvent createIdleEvent() {
        return new BillDispenserStatusUpdateEvent(Props.EventSource, BDSP_STATUS_ASYNC, TheException);
    }
}