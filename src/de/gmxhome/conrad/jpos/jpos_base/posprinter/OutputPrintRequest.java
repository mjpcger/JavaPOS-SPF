/*
 * Copyright 2018 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.posprinter;

import de.gmxhome.conrad.jpos.jpos_base.*;

/**
 * Output print request class for printers.
 */
public class OutputPrintRequest extends OutputRequest {
    private boolean SynchronousPrinting;

    /**
     * Gets synchronous printing flag. This flag is set in PrintImmediate method and if the following
     * conditions are met:
     * <ul>
     *     <li>AsyncMode is false,</li>
     *     <li>Sideways printing is not active,</li>
     *     <li>The printer is not in page mode,</li>
     *     <li>The printer is outside of a transaction.</li>
     * </ul>
     * The implementation of the corresponding methods shall check this flag to verify whether the output
     * request data must be completely printed to be successful. In other words, if SynchronousPrinting is
     * true, request data must be completely printed. No print data may remain in a printer buffer. See
     * UPOS specification, end of chapter POS Printer - General Information - Model.
     *
     * @return True if request shall be performed completely before printing method returns.
     */
    public boolean getSynchronousPrinting() {
        return SynchronousPrinting;
    }

    /**
     * Sets synchronous printing flag if the given request is an OutputPrintRequest and AsyncMode is false or
     * request is a PrintImmediate request that is alwasy synchronously.
     *
     * @param request Request to be modified if the conditions are met.
     */
    static void setSynchronousPrinting(OutputRequest request) {
        if ((request instanceof OutputPrintRequest && !request.Props.AsyncMode) || request instanceof PrintImmediate)
            ((OutputPrintRequest)request).SynchronousPrinting = true;
    }

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     */
    public OutputPrintRequest(JposCommonProperties props) {
        super(props);
        SynchronousPrinting = false;
    }
}
