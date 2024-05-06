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

import static jpos.FiscalPrinterConst.*;

/**
 * Output request executor for FiscalPrinter method PrintFiscalDocumentLine.
 */
public class PrintFixedOutput extends OutputRequest {
    private int Station = FPTR_S_RECEIPT;

    /**
     * FiscalPrinter method BeginFixedOutput parameter station, see UPOS specification. Default:
     * FPTR_S_RECEIPT. If printer supports fixed output on slip paper and beginFixedOutput has been
     * called with station = FPTR_S_SLIP, the implementation class must explicitly call changeToSlip
     * to activate slipoutput.
     * @return Print station.
     */
    public int getStation() {
        return Station;
    }

    /**
     * Changes Station to FPTR_S_SLIP. Must be called before first call of invoke() whenever FPTR_S_SLIP
     * had been specified in the corresponding BeginFixedOutput method call.
     */
    public void changeToSlip() {
        Station = FPTR_S_SLIP;
    }

    private final int DocumentType;

    /**
     * FiscalPrinter method PrintFixedOutput parameter lineNumber, see UPOS specification.
     * @return PrintFixedOutput parameter <i>lineNumber</i>.
     */
    public int getDocumentType() {
        return DocumentType;
    }

    private final int LineNumber;

    /**
     * FiscalPrinter method PrintFixedOutput parameter documentLine, see UPOS specification.
     * @return PrintFixedOutput parameter <i>documentLine</i>.
     */
    public int getLineNumber() {
        return LineNumber;
    }

    private final String Data;

    /**
     * FiscalPrinter method PrintFixedOutput parameter data, see UPOS specification.
     * @return PrintFixedOutput parameter <i>data</i>.
     */
    public String getData() {
        return Data;
    }

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props         Property set of device service.
     * @param documentType  Identifier of a document stored in the Fiscal Printer.
     * @param lineNumber    Number of the line in the document to print.
     * @param data          String parameter for placement in printed line.
     */
    public PrintFixedOutput(FiscalPrinterProperties props, int documentType, int lineNumber, String data) {
        super(props);
        DocumentType = documentType;
        LineNumber = lineNumber;
        Data = data;
    }

    @Override
    public void invoke() throws JposException {
        FiscalPrinterService svc = (FiscalPrinterService)Props.EventSource;
        svc.checkCoverPaper(getStation());
        svc.FiscalPrinterInterface.printFixedOutput(this);
        super.invoke();
    }
}
