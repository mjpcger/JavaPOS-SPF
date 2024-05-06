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

/**
 * Output request base for FiscalPrinter methods that use description, amount and vatinfo arguments.
 */
class SimpleItemOutputRequest extends SimpleAmountOutputRequest {
    private final String Description;

    /**
     * FiscalPrinter method parameter description, see specific method.
     * @return parameter <i>description</i>.
     */
    public String getDescription() {
        return Description;
    }

    private final int VatInfo;

    /**
     * FiscalPrinter method parameter vatInfo, see specific method.
     * @return parameter <i>vatInfo</i>.
     */
    public int getVatInfo() {
        return VatInfo;
    }

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props         Property set of device service.
     * @param description   Item or discount description.
     * @param amount        Amount or percent amount (in case of percent adjustment)
     * @param vatInfo       VAT rate identifier or amount.
     */
    public SimpleItemOutputRequest(FiscalPrinterProperties props, String description, long amount, int vatInfo) {
        super(props, amount);
        Description = description;
        VatInfo = vatInfo;
    }
}
