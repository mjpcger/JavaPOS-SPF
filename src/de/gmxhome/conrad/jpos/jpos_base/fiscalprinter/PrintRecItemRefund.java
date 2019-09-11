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
 * Output request executor for FiscalPrinter method PrintRecItemRefund.
 */
public class PrintRecItemRefund extends PrintRecItem {
    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props       Property set of device service.
     * @param description Text describing the refund.
     * @param price       Text describing the refund.
     * @param quantity    Number of items.
     * @param vatInfo     VAT rate identifier or amount.
     * @param unitPrice   Amount of each refund item.
     * @param unitName    Name of the unit.
     */
    public PrintRecItemRefund(FiscalPrinterProperties props, String description, long price, int quantity, int vatInfo, long unitPrice, String unitName) {
        super(props, description, price, quantity, vatInfo, unitPrice, unitName);
    }

    @Override
    public void invokeMethod() throws JposException {
        ((FiscalPrinterProperties)Props).printRecItemRefund(this);
    }
}
