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

package de.gmxhome.conrad.jpos.jpos_base.remoteorderdisplay;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

import java.util.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;

/**
 * Output request executor for RemoteOrderDisplay method TransactionDisplay.
 */
public class TransactionDisplay extends UnitOutputRequest {
    /**
     * Retrieves parameter row of method TransactionDisplay. See UPOS specification for further information.
     * @return  Value of method parameter function.
     */
    public int getFunction() {
        return Function;
    }

    private final int Function;

    /**
     * List holds all outstanding output requests.
     */
    final List<UnitOutputRequest> TransactionCommands = new ArrayList<>();

    /**
     * Adds an output request to the request queue.
     * @param request Request to be enqueued.
     * @throws JposException if request is null (specifying synchronous method implementation).
     */
    synchronized void addMethod(UnitOutputRequest request) throws JposException {
        check(request == null, JPOS_E_FAILURE, "Transaction mode not supported for synchronous implementation");
        TransactionCommands.add(request);
    }

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param units     Bitwise mask indicating which video unit(s) to operate on.
     * @param function  Transaction control function.
     */
    public TransactionDisplay(JposCommonProperties props, int units, int function) {
        super(props, units);
        Function = function;
    }

    @Override
    public void invoke() throws JposException {
        if (EndSync == null)
            checkUnitsOnline();
        ((RemoteOrderDisplayService)Props.EventSource).RemoteOrderDisplayInterface.transactionDisplay(this);
        for (UnitOutputRequest request : TransactionCommands) {
            check (Abort != null, JPOS_E_FAILURE, "Transaction interrupted");
            request.invoke();
        }
    }
}
