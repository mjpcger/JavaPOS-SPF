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

package de.gmxhome.conrad.jpos.jpos_base.fiscalprinter;

import jpos.*;

/**
 * Output request for FiscalPrinter methods PrintRecSubtotalAdjustment.
 */
public class PrintRecSubtotalAdjustment extends PrintRecSubtotalAdjustVoid {
    private final String Description;

    /**
     * FiscalPrinter method parameter description, see method PrintRecSubtotalAdjustment.
     * @return parameter <i>description</i>.
     */
    public String getDescription() {
        return Description;
    }

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props          Property set of device service.
     * @param adjustmentType Type of adjustment.
     * @param description    Text describing the discount or surcharge.
     * @param amount         Amount or percent amount (in case of percent adjustment)
     */
    public PrintRecSubtotalAdjustment(FiscalPrinterProperties props, int adjustmentType, String description, long amount) {
        super(props, adjustmentType, amount);
        Description = description;
    }

    @Override
    public void invokeMethod() throws JposException {
        FiscalPrinterService svc = (FiscalPrinterService)Props.EventSource;
        if (EndSync == null) {
            svc.checkCoverPaper(svc.getFiscalStation());
        }
        svc.FiscalPrinterInterface.printRecSubtotalAdjustment(this);
    }
}
