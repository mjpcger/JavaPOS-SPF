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
 * Output request executor for FiscalPrinter method PrintRecSubtotalAdjustVoid and executor base for FiscalPrinterService method
 * PrintRecSubtotalAdjustment.
 */
public class PrintRecSubtotalAdjustVoid extends SimpleAmountOutputRequest {
    private int AdjustmentType;

    /**
     * FiscalPrinter method parameter vatInfo, see method .
     * @return parameter <i>vatInfo</i>.
     */
    public int getAdjustmentType() {
        return AdjustmentType;
    }

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props             Property set of device service.
     * @param adjustmentType    Type of adjustment.
     * @param amount            Amount or percent amount (in case of percent adjustment).
     */
    public PrintRecSubtotalAdjustVoid(FiscalPrinterProperties props, int adjustmentType, long amount) {
        super(props, amount);
        AdjustmentType = adjustmentType;
    }

    @Override
    public void invokeMethod() throws JposException {
        ((FiscalPrinterProperties)Props).printRecSubtotalAdjustVoid(this);
    }
}
