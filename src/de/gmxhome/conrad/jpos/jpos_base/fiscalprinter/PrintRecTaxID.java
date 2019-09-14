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
 * Output request executor for FiscalPrinter method PrintRecTaxID.
 */
public class PrintRecTaxID extends OutputRequest {
    private String TaxId;

    /**
     * FiscalPrinter method PrintRecTaxID parameter taxId, see UPOS specification.
     * @return PrintRecTaxID parameter <i>taxId</i>.
     */
    public String getTaxId() {
        return TaxId;
    }
    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param taxId Customer identification with identification characters and tax number.
     */
    public PrintRecTaxID(FiscalPrinterProperties props, String taxId) {
        super(props);
        TaxId = taxId;
    }

    @Override
    public void invoke() throws JposException {
        FiscalPrinterService svc = (FiscalPrinterService)Props.EventSource;
        if (EndSync == null) {
            svc.checkCoverPaper(svc.getFiscalStation());
        }
        svc.FiscalPrinterInterface.printRecTaxID(this);
        super.invoke();
    }
}
