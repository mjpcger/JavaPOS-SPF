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

package de.gmxhome.conrad.jpos.jpos_base.cat;

import jpos.*;

/**
 * Output request executor for CAT method AuthorizePreSales.
 */
public class AuthorizePreSales extends AuthorizeCompletion {
    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param data           Property set of device service.
     * @param sequenceNumber Sequence number for approval.
     * @param amount         Purchase amount for approval.
     * @param taxOthers      Tax and other amounts for approval.
     * @param timeout        The maximum waiting time (in milliseconds) until the response is received from the CAT device.
     */
    public AuthorizePreSales(CATProperties data, int sequenceNumber, long amount, long taxOthers, int timeout) {
        super(data, sequenceNumber, amount, taxOthers, timeout);
    }

    @Override
    public void invoke() throws JposException {
        ((CATService)Props.EventSource).CatInterface.authorizePreSales(this);
    }
}
