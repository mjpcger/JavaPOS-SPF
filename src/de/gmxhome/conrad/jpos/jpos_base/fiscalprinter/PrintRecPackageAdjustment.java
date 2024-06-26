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

import java.util.Map;

/**
 * Output request executor for FiscalPrinter method PrintRecPackageAdjustment.
 */
public class PrintRecPackageAdjustment extends PrintRecPackageAdjustVoid {
    private final String Description;

    /**
     * FiscalPrinter method parameter description, see method PrintRecPackageAdjustment.
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
     * @param description    Text describing the adjustment.
     * @param vatAdjustment  String containing a list of adjustment(s) to be voided for different VAT(s).
     */
    public PrintRecPackageAdjustment(FiscalPrinterProperties props, int adjustmentType, String description, String vatAdjustment) {
        super(props, adjustmentType, vatAdjustment);
        Description = description;
    }

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props          Property set of device service.
     * @param adjustmentType Type of adjustment.
     * @param description    Text describing the adjustment.
     * @param vatAdjustment  String containing a list of adjustment(s) to be voided for different VAT(s).
     * @param parsedAdjustments parsed vatAdjustment, contains the adjustment amounts with the corresponding vat id as key
     *                          specified in vatAdjustment.
     */
    public PrintRecPackageAdjustment(FiscalPrinterProperties props, int adjustmentType, String description, String vatAdjustment, Map<Integer, Number> parsedAdjustments) {
        super(props, adjustmentType, vatAdjustment, parsedAdjustments);
        Description = description;
    }

    @Override
    public void invokeMethod() throws JposException {
        FiscalPrinterService svc = (FiscalPrinterService)Props.EventSource;
        if (EndSync == null) {
            svc.checkCoverPaper(svc.getFiscalStation());
        }
        svc.FiscalPrinterInterface.printRecPackageAdjustment(this);
    }
}
