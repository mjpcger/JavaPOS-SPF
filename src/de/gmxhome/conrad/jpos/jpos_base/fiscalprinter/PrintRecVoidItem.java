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
 * Output request executor for FiscalPrinter method PrintRecVoidItem.
 */
public class PrintRecVoidItem extends SimpleItemOutputRequest {
    private int Quantity;

    /**
     * FiscalPrinter method PrintRecVoidItem parameter quantity, see UPOS specification.
     * @return PrintRecVoidItem parameter <i>quantity</i>.
     */
    public int getQuantity() {
        return Quantity;
    }

    private int AdjustmentType;

    /**
     * FiscalPrinter method parameter adjustmentType, see method PrintRecVoidItem.
     * @return parameter <i>adjustmentType</i>.
     */
    public int getAdjustmentType() {
        return AdjustmentType;
    }

    private long Adjustment;

    /**
     * FiscalPrinter method PrintRecVoidItem parameter adjustment, see UPOS specification.
     * @return PrintRecVoidItem parameter <i>adjustment</i>.
     */
    public long getAdjustment() {
        return Adjustment;
    }

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param description Text describing the item sold.
     * @param price       Price of the item.
     * @param quantity    Number of items.
     * @param adjustmentType  Type of adjustment.
     * @param adjustment  Amount or percent amount (in case of percent adjustment).
     * @param vatInfo     VAT rate identifier or amount.
     */
    public PrintRecVoidItem(FiscalPrinterProperties props, String description, long price, int quantity, int adjustmentType, long adjustment, int vatInfo) {
        super(props, description, price, vatInfo);
        Quantity = quantity;
        AdjustmentType = adjustmentType;
        Adjustment = adjustment;
    }

    @Override
    public void invokeMethod() throws JposException {
        ((FiscalPrinterProperties)Props).printRecVoidItem(this);
    }
}
