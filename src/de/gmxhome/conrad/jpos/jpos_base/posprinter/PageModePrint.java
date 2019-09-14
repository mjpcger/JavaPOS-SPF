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
 * Output request executor for POSPrinter method PageModePrint.
 */
public class PageModePrint extends OutputRequest {
    /**
     * POSPrinter method PageModePrint parameter control, see UPOS specification.
     * @return PageModePrint parameter <i>control</i>.
     */
    public int getControl() {
        return Control;
    }

    private int Control;

    /**
     * List holds all outstanding output requests.
     */
    private List<OutputRequest> PageModeCommands = new ArrayList<OutputRequest>();

    /**
     * Adds an output request to the request queue.
     * @param request Request to be enqueued.
     * @throws JposException if request is null (specifying synchronous method implementation).
     */
    public synchronized void addMethod(OutputRequest request) throws JposException {
        Props.Device.check(request == null, JposConst.JPOS_E_FAILURE, "Pagemode not supported for synchronous implementation");
        PageModeCommands.add(request);
    }

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param control Page Mode control. One of PM_PAGE_MODE, PM_PRINT_SAVE, PM_NORMAL or PM_CANCEL.
     */
    public PageModePrint(JposCommonProperties props, int control) {
        super(props);
        Control = control;
    }

    @Override
    public void invoke() throws JposException {
        POSPrinterService svc = (POSPrinterService)Props.EventSource;
        if (EndSync == null) {
            svc.extendedErrorCheck(((POSPrinterProperties)Props).PageModeStation);
        }
        svc.POSPrinterInterface.pageModePrint(this);
        for (OutputRequest request : PageModeCommands) {
            Device.check (Abort != null, JposConst.JPOS_E_FAILURE, "Page mode interrupted");
            request.invoke();
        }
    }
}
