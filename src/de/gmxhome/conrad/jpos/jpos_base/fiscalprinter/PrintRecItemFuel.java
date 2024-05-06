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
 * Output request executor for FiscalPrinter method PrintRecItemFuel.
 */
public class PrintRecItemFuel extends PrintRecItem {
    private final long SpecialTax;

    /**
     * FiscalPrinter method PrintRecItemFuel parameter specialTax, see UPOS specification.
     * @return PrintRecItemFuel parameter <i>specialTax</i>.
     */
    public long getSpecialTax() {
        return SpecialTax;
    }

    private final String SpecialTaxName;

    /**
     * FiscalPrinter method PrintRecItemFuel parameter specialTaxName, see UPOS specification.
     * @return PrintRecItemFuel parameter <i>specialTaxName</i>.
     */
    public String getSpecialTaxName() {
        return SpecialTaxName;
    }

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props             Property set of device service.
     * @param description       Text describing the fuel product.
     * @param price             Price of the fuel item.
     * @param quantity          Number of items.
     * @param vatInfo           VAT rate identifier or amount.
     * @param unitPrice         Price of the fuel item per volume.
     * @param unitName          Name of the volume unit.
     * @param specialTax        Special tax amount, e.g., road tax.
     * @param specialTaxName    Name of the special tax.
     */
    public PrintRecItemFuel(FiscalPrinterProperties props, String description, long price, int quantity, int vatInfo, long unitPrice, String unitName, long specialTax, String specialTaxName) {
        super(props, description, price, quantity, vatInfo, unitPrice, unitName);
        SpecialTax = specialTax;
        SpecialTaxName = specialTaxName;
    }

    @Override
    public void invokeMethod() throws JposException {
        FiscalPrinterService svc = (FiscalPrinterService)Props.EventSource;
        if (EndSync == null) {
            svc.checkCoverPaper(svc.getFiscalStation());
        }
        svc.FiscalPrinterInterface.printRecItemFuel(this);
    }
}
