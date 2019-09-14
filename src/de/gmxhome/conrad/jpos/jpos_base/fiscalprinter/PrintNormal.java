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
 * Output request executor for FiscalPrinter method PrintNormal.
 */
public class PrintNormal extends OutputRequest {
    private int Station;

    /**
     * FiscalPrinter method PrintNormal parameter station, see UPOS specification.
     * @return PrintNormal parameter <i>station</i>.
     */
    public int getStation() {
        return Station;
    }

    private String Data;

    /**
     * FiscalPrinter method PrintNormal parameter data, see UPOS specification.
     * @return PrintNormal parameter <i>data</i>.
     */
    public String getData() {
        return Data;
    }

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props     Property set of device service.
     * @param station   The Fiscal Printer station to be used.
     * @param data      The characters to be printed.
     */
    public PrintNormal(FiscalPrinterProperties props, int station, String data) {
        super(props);
        Station = station;
        Data = data;
    }

    @Override
    public void invoke() throws JposException {
        FiscalPrinterService svc = (FiscalPrinterService)Props.EventSource;
        if (EndSync == null) {
            svc.checkCoverPaper(getStation());
        }
        svc.FiscalPrinterInterface.printNormal(this);
        super.invoke();
    }
}
