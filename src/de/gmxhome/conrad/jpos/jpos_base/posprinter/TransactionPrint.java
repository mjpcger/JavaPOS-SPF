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

import java.util.ArrayList;
import java.util.List;

/**
 * Output request executor for POSPrinter method TransactionPrint.
 */
public class TransactionPrint extends OutputRequest {
    /**
     * POSPrinter method TransactionPrint parameter station, see UPOS specification.
     * @return TransactionPrint parameter <i>station</i>.
     */
    public int getStation() {
        return Station;
    }

    private int Station;

    /**
     * POSPrinter method TransactionPrint parameter control, see UPOS specification.
     * @return TransactionPrint parameter <i>control</i>.
     */
    public int getControl() {
        return Control;
    }

    private int Control;

    /**
     * List holds all outstanding output requests.
     */
    List<OutputRequest> TransactionCommands = new ArrayList<OutputRequest>();

    /**
     * Adds an output request to the request queue.
     * @param request Request to be enqueued.
     * @throws JposException if request is null (specifying synchronous method implementation).
     */
    synchronized void addMethod(OutputRequest request) throws JposException {
        Props.Device.check(request == null, JposConst.JPOS_E_FAILURE, "Transaction mode not supported for synchronous implementation");
        TransactionCommands.add(request);
    }

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param station The printer station to be used. May be either S_RECEIPT or S_SLIP.
     * @param control Transaction control. One of TP_TRANSACTION and TP_NORMAL.
     */
    public TransactionPrint(JposCommonProperties props, int station, int control) {
        super(props);
        Station = station;
        Control = control;
    }

    @Override
    public void invoke() throws JposException {
        POSPrinterService svc = (POSPrinterService)Props.EventSource;
        if (EndSync == null) {
            svc.extendedErrorCheck(getStation());
        }
        svc.POSPrinterInterface.transactionPrint(this);
        for (OutputRequest request : TransactionCommands) {
            Device.check (Abort != null, JposConst.JPOS_E_FAILURE, "Transaction interrupted");
            request.invoke();
        }
    }
}
