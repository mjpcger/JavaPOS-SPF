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
 * Output request executor for FiscalPrinter method PrintRecItem, output request base for several PrintRec... methods
 * with quantity and price data.
 */
public class PrintRecItem extends SimpleItemOutputRequest {
    private int Quantity;

    /**
     * FiscalPrinter method parameter quantity, see UPOS specification.
     * @return PrintRecItem parameter <i>quantity</i>.
     */
    public int getQuantity() {
        return Quantity;
    }

    private long UnitPrice;

    /**
     * FiscalPrinter method parameter unitPrice, see UPOS specification.
     * @return PrintRecItem parameter <i>unitPrice</i>.
     */
    public long getUnitPrice() {
        return UnitPrice;
    }

    private String UnitName;

    /**
     * FiscalPrinter method parameter unitName, see UPOS specification.
     * @return PrintRecItem parameter <i>unitName</i>.
     */
    public String getUnitName() {
        return UnitName;
    }

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param description Text describing the item sold.
     * @param price       Price of the item.
     * @param quantity    Number of items.
     * @param vatInfo     VAT rate identifier or amount.
     * @param unitPrice   Price of each item.
     * @param unitName    Name of the unit.
     */
    public PrintRecItem(FiscalPrinterProperties props, String description, long price, int quantity, int vatInfo, long unitPrice, String unitName) {
        super(props, description, price, vatInfo);
        Quantity = quantity;
        UnitPrice = unitPrice;
        UnitName = unitName;
    }

    @Override
    public void invokeMethod() throws JposException {
        ((FiscalPrinterProperties)Props).printRecItem(this);
    }
}
