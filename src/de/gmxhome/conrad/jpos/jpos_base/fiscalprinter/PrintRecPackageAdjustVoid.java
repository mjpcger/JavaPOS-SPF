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
 * Output request executor for FiscalPrinter method PrintRecPackageAdjustVoid and executor base for FiscalPrinterService method
 * PrintRecPackageAdjustment.
 */
public class PrintRecPackageAdjustVoid extends PrePostOutputRequest {
    private int AdjustmentType;

    /**
     * FiscalPrinter method parameter adjustmentType, see method PrintRecPackageAdjustVoid.
     * @return parameter <i>adjustmentType</i>.
     */
    public int getAdjustmentType() {
        return AdjustmentType;
    }

    private String VatAdjustment;

    /**
     * FiscalPrinter method parameter vatAdjustment, see method PrintRecPackageAdjustVoid.
     * @return parameter <i>vatAdjustment</i>.
     */
    public String getVatAdjustment() {
        return VatAdjustment;
    }

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props             Property set of device service.
     * @param adjustmentType    Type of adjustment.
     * @param vatAdjustment     String containing a list of adjustment(s) to be voided for different VAT(s).
     */
    public PrintRecPackageAdjustVoid(FiscalPrinterProperties props, int adjustmentType, String vatAdjustment) {
        super(props);
        AdjustmentType = adjustmentType;
        VatAdjustment = vatAdjustment;
    }

    @Override
    public void invokeMethod() throws JposException {
        FiscalPrinterService svc = (FiscalPrinterService)Props.EventSource;
        if (EndSync == null) {
            svc.checkCoverPaper(svc.getFiscalStation());
        }
        svc.FiscalPrinterInterface.printRecPackageAdjustVoid(this);
    }
}
