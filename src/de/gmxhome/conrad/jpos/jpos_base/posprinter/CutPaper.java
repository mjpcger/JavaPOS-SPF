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

import static jpos.POSPrinterConst.*;

/**
 * Output request executor for POSPrinter method CutPaper.
 */
public class CutPaper extends OutputRequest {
    /**
     * POSPrinter method CutPaper parameter percentage, see UPOS specification.
     * @return CutPaper parameter <i>percentage</i>.
     */
    public int getPercentage() {
        return Percentage;
    }

    private final int Percentage;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param percentage The percentage of paper to cut.
     */
    public CutPaper(JposCommonProperties props, int percentage) {
        super(props);
        Percentage = percentage;
    }

    @Override
    public void invoke() throws JposException {
        POSPrinterService svc = (POSPrinterService)Props.EventSource;
        if (EndSync == null) {
            svc.extendedErrorCheck(PTR_S_RECEIPT);
        }
        svc.POSPrinterInterface.cutPaper(this);
    }
}
