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
 * Output request executor for FiscalPrinter methods PrintRecTotal.
 */
public class PrintRecTotal extends SimpleAmountOutputRequest {
    private long Payment;

    /**
     * FiscalPrinter method PrintRecTotal parameter payment, see UPOS specification.
     * @return PrintRecTotal parameter <i>payment</i>.
     */
    public long getPayment() {
        return Payment;
    }

    private String Description;

    /**
     * FiscalPrinter method parameter description, see method PrintRecTotal.
     * @return parameter <i>description</i>.
     */
    public String getDescription() {
        return Description;
    }

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props         Property set of device service.
     * @param total         Application computed receipt total.
     * @param payment       Amount of payment tendered.
     * @param description   Text description of the payment or the index of a predefined payment description.
     */
    public PrintRecTotal(FiscalPrinterProperties props, long total, long payment, String description) {
        super(props, total);
        Payment = payment;
        Description = description;
    }

    @Override
    public void invokeMethod() throws JposException {
        ((FiscalPrinterProperties)Props).printRecTotal(this);
    }
}
