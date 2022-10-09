/*
 * Copyright 2022 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.electronicvaluerw;
import jpos.JposException;

/**
 * Output request executor for ElectronicValueRW method CashDeposit.
 */
public class CashDeposit extends CheckCard {
    /**
     * Get amount of money.
     * @return Amount of money.
     */
    public long getAmount() { return Amount; }
    private long Amount;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param data           Property set of device service.
     * @param sequenceNumber Sequence number for approval.
     * @param amount         Amount of money for charge.
     * @param timeout        The maximum waiting time (in milliseconds) until the response is received from the ElectronicValueRW device.
     */
    public CashDeposit(ElectronicValueRWProperties data, int sequenceNumber, long amount, int timeout) {
        super(data, sequenceNumber, timeout);
        Amount = amount;
    }

    @Override
    public void invoke() throws JposException {
        ((ElectronicValueRWService)Props.EventSource).ElectronicValueRW.cashDeposit(this);
    }
}
