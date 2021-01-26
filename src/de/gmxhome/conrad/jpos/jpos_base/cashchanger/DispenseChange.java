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

package de.gmxhome.conrad.jpos.jpos_base.cashchanger;

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import de.gmxhome.conrad.jpos.jpos_base.JposErrorEvent;
import de.gmxhome.conrad.jpos.jpos_base.JposOutputRequest;
import de.gmxhome.conrad.jpos.jpos_base.JposStatusUpdateEvent;
import jpos.CashChangerConst;
import jpos.JposConst;
import jpos.JposException;

/**
 * Output request executor for CashChanger method DispenseChange.
 */
public class DispenseChange extends JposOutputRequest {
    /**
     *  Get amount parameter passed to DispenseChange method.
     * @return CashCounts parameter.
     */
    public int getAmount() {
        return Amount;
    }
    private int Amount;

    /**
     * Gets CurrentExit property at the time when the request has been created.
     * @return  CurrentExit property.
     */
    public int getCurrentExit() {
        return CurrentExit;
    }
    private int CurrentExit;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props  Property set of device service.
     * @param amount The amount parameter contains the amount of change to be dispensed.
     */
    public DispenseChange(JposCommonProperties props, int amount) {
        super(props);
        Amount = amount;
        CurrentExit = ((CashChangerProperties)props).CurrentExit;
    }

    @Override
    public void invoke() throws JposException {
        ((CashChangerService)Props.EventSource).CashChangerInterface.dispenseChange(this);
    }

    private JposException TheException = null;

    @Override
    public JposErrorEvent createErrorEvent(JposException ex) {
        TheException = ex;
        return null;
    }

    @Override
    public JposStatusUpdateEvent createIdleEvent() {
        return new CashChangerStatusUpdateEvent(Props.EventSource, CashChangerConst.CHAN_STATUS_ASYNC, TheException);
    }
}
