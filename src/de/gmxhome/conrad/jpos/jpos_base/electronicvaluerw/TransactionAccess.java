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
import jpos.*;

import java.util.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.ElectronicValueRWConst.*;
import static jpos.JposConst.*;

/**
 * Output request executor for ElectronicValueRW method TransactionAccess.
 */
public class TransactionAccess extends OutputRequest {
    /**
     * Returns copy of control parameter (The transaction control)
     * @return The transaction control value.
     */
    public int getControl() {
        return Control;
    }
    private final int Control;

    /**
     * List holds all outstanding output requests.
     */
    List<JposOutputRequest> TransactionCommands = new ArrayList<>();

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
            check(((ElectronicValueRWProperties)svc.Props).DetectionStatus == EVRW_DS_NOCARD, JPOS_E_FAILURE, "No card present");
        }
        svc.ElectronicValueRW.transactionAccess(this);
        for (JposOutputRequest request : TransactionCommands) {
            check (Abort != null, JPOS_E_FAILURE, "Transaction interrupted");
            request.invoke();
        }
    }
}
