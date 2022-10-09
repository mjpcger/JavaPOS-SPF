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
 *
 */

package de.gmxhome.conrad.jpos.jpos_base.electronicvaluerw;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.ElectronicValueRW;
import jpos.ElectronicValueRWConst;
import jpos.JposConst;
import jpos.JposException;

import java.util.ArrayList;
import java.util.List;

/**
 * Output request executor for ElectronicValueRW method
 */
public class TransactionAccess extends OutputRequest {
    /**
     * Returns copy of control parameter (The transaction control)
     * @return The transaction control value.
     */
    public int getControl() {
        return Control;
    }
    private int Control;

    /**
     * List holds all outstanding output requests.
     */
    List<JposOutputRequest> TransactionCommands = new ArrayList<JposOutputRequest>();

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
     * @param props       Property set of device service.
     * @param control           The transaction control.
     */
    public TransactionAccess(ElectronicValueRWProperties props, int control) {
        super(props);
        Control = control;
    }

    @Override
    public void invoke() throws JposException {
        ElectronicValueRWService svc = (ElectronicValueRWService)Props.EventSource;
        if (EndSync == null) {
            JposDevice.check(((ElectronicValueRWProperties)svc.Props).DetectionStatus == ElectronicValueRWConst.EVRW_DS_NOCARD, JposConst.JPOS_E_FAILURE, "No card present");
        }
        svc.ElectronicValueRW.transactionAccess(this);
        for (JposOutputRequest request : TransactionCommands) {
            Device.check (Abort != null, JposConst.JPOS_E_FAILURE, "Transaction interrupted");
            request.invoke();
        }
    }
}
