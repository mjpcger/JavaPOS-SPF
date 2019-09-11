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
 * Output request executor for FiscalPrinter method PrintRecItemAdjustment.
 */
public class PrintRecItemAdjustment extends SimpleItemOutputRequest {
    private int AdjustmentType;

    /**
     * FiscalPrinter method parameter adjustmentType, see method PrintRecItemAdjustment.
     * @return parameter <i>adjustmentType</i>.
     */
    public int getAdjustmentType() {
        return AdjustmentType;
    }

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props             Property set of device service.
     * @param adjustmentType    Type of adjustment.
     * @param description       Item or discount description.
     * @param amount            Amount or percent amount (in case of percent adjustment)
     * @param vatInfo           VAT rate identifier or amount.
     */
    public PrintRecItemAdjustment(FiscalPrinterProperties props, int adjustmentType, String description, long amount, int vatInfo) {
        super(props, description, amount, vatInfo);
        AdjustmentType = adjustmentType;
    }

    @Override
    public void invokeMethod() throws JposException {
        ((FiscalPrinterProperties)Props).printRecItemAdjustment(this);
    }
}
