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
 * Output request executor for FiscalPrinter method PrintRecItemFuelVoid.
 */
public class PrintRecItemFuelVoid extends SimpleItemOutputRequest {
    private long SpecialTax;

    /**
     * FiscalPrinter method parameter specialTax, see method PrintRecItemFuelVoid.
     * @return parameter <i>specialTax</i>.
     */
    public long getSpecialTax() {
        return SpecialTax;
    }

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props       Property set of device service.
     * @param description Item or discount description.
     * @param amount      Amount or percent amount (in case of percent adjustment)
     * @param vatInfo     VAT rate identifier or amount.
     * @param specialTax  Special tax amount, e.g., road tax.
     */
    public PrintRecItemFuelVoid(FiscalPrinterProperties props, String description, long amount, int vatInfo, long specialTax) {
        super(props, description, amount, vatInfo);
        SpecialTax = specialTax;
    }

    @Override
    public void invokeMethod() throws JposException {
        ((FiscalPrinterProperties)Props).printRecItemFuelVoid(this);
    }
}
