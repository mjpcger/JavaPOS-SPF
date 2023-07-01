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

import de.gmxhome.conrad.jpos.jpos_base.JposDevice;
import jpos.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Output request executor for FiscalPrinter method PrintRecPackageAdjustVoid and executor base for FiscalPrinterService method
 * PrintRecPackageAdjustment.
 */
public class PrintRecPackageAdjustVoid extends PrePostOutputRequest {
    private int AdjustmentType;

    /**
     * FiscalPrinter method parameter adjustmentType, see method PrintRecPackageAdjustVoid.
     * @return parameter <i>adjustmentType</i>.
     */
    public int getAdjustmentType() {
        return AdjustmentType;
    }

    private String VatAdjustment;

    /**
     * FiscalPrinter method parameter vatAdjustment, see method PrintRecPackageAdjustVoid.
     * @return parameter <i>vatAdjustment</i>.
     */
    @Deprecated
    public String getVatAdjustment() {
        return VatAdjustment;
    }

    private Map.Entry<Integer,Number>[] ParsedParameters;

    /**
     * Returns number of VAT ID and adjustment amount pairs specified within VatAdjustment. Even if VatAdjustment is still
     * available, use getVatIDAmountCount, getVatID and getAdjustmentAmount instead to retrieve the adjustments per vat.
     * @return Number of pairs. If zero, deprecated constructor has been used to create this object and getVatAdjustment
     *         must be parsed to get vat IDs and amounts.
     */
    public int getVatIDAmountCount() {
        return ParsedParameters.length;
    }

    /**
     * Retrieves vat id of the specified pair.
     * @param i Pair index, a value between zero and getVatIDAmountCount() - 1.
     * @return  Vat ID component of the specified pair.
     * @throws JposException If the index is outside the valid range.
     */
    public int getVatID(int i) throws JposException {
        JposDevice.check(i < 0 || i >= ParsedParameters.length, JposConst.JPOS_E_FAILURE, "Index out of bound: " + i);
        return ParsedParameters[i].getKey();
    }

    /**
     * Retrieves adjustment amount of the specified pair.
     * @param i Pair index, a value between zero and getVatIDAmountCount() - 1.
     * @return  adjustment amount component of the specified pair.
     * @throws JposException If the index is outside the valid range.
     */
    public Number getAjustmentAmount(int i) throws JposException {
        JposDevice.check(i < 0 || i >= ParsedParameters.length, JposConst.JPOS_E_FAILURE, "Index out of bound: " + i);
        return ParsedParameters[i].getValue();
    }

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props             Property set of device service.
     * @param adjustmentType    Type of adjustment.
     * @param vatAdjustment     String containing a list of adjustment(s) to be voided for different VAT(s).
     */
    public PrintRecPackageAdjustVoid(FiscalPrinterProperties props, int adjustmentType, String vatAdjustment) {
        super(props);
        AdjustmentType = adjustmentType;
        VatAdjustment = vatAdjustment;
        ParsedParameters = new Map.Entry[0];
    }

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props             Property set of device service.
     * @param adjustmentType    Type of adjustment.
     * @param vatAdjustment     String containing a list of adjustment(s) to be voided for different VAT(s).
     * @param parsedAdjustments parsed vatAdjustment, contains the adjustment amounts with the corresponding vat id as key
     *                          specified in vatAdjustment.
     */
    public PrintRecPackageAdjustVoid(FiscalPrinterProperties props, int adjustmentType, String vatAdjustment, Map<Integer, Number> parsedAdjustments) {
        super(props);
        AdjustmentType = adjustmentType;
        VatAdjustment = vatAdjustment;
        ParsedParameters = (Map.Entry<Integer, Number>[]) parsedAdjustments.entrySet().toArray();
    }

    @Override
    public void invokeMethod() throws JposException {
        FiscalPrinterService svc = (FiscalPrinterService)Props.EventSource;
        if (EndSync == null) {
            svc.checkCoverPaper(svc.getFiscalStation());
        }
        svc.FiscalPrinterInterface.printRecPackageAdjustVoid(this);
    }
}
