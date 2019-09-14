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
 * Output request executor for FiscalPrinter method PrintFiscalDocumentLine.
 */
public class PrintFiscalDocumentLine extends OutputRequest {
    private String DocumentLine;

    /**
     * FiscalPrinter method PrintFiscalDocumentLine parameter documentLine, see UPOS specification.
     * @return PrintFiscalDocumentLine parameter <i>documentLine</i>.
     */
    public String getDocumentLine() {
        return DocumentLine;
    }

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param documentLine  String to be printed on fiscal slip.
     */
    public PrintFiscalDocumentLine(FiscalPrinterProperties props, String documentLine) {
        super(props);
        DocumentLine = documentLine;
    }

    @Override
    public void invoke() throws JposException {
        FiscalPrinterService svc = (FiscalPrinterService)Props.EventSource;
        if (EndSync == null) {
            svc.checkCoverPaper(FiscalPrinterConst.FPTR_S_SLIP);
        }
        svc.FiscalPrinterInterface.printFiscalDocumentLine(this);
        super.invoke();
    }
}
