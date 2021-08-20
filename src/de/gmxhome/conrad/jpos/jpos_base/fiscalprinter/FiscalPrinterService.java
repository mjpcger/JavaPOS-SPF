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

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.services.FiscalPrinterService115;

import java.math.BigDecimal;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * FiscalPrinter service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class FiscalPrinterService extends JposBase implements FiscalPrinterService115 {
    /**
     * Instance of a class implementing the FiscalPrinterInterface for fiscal printer specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public FiscalPrinterInterface FiscalPrinterInterface;

    private FiscalPrinterProperties Data;

    private void checkBusySync() throws JposException {
        Device.check(Data.State == JposConst.JPOS_S_BUSY && !Data.AsyncMode, JposConst.JPOS_E_BUSY, "Output in progress");
    }

    private long truncUnusedDecimals(long amount) {
        long factor = (new long[]{10000, 1000, 100, 10, 1})[Data.AmountDecimalPlaces];

        return (amount / factor) * factor;
    }

    /**
     * Retrieves print stations to be checked for for fiscal receipt.
     * @return  Stations to be checked, FiscalReceiptStation combined with journal station.
     */
    int getFiscalStation() {
        return Data.FiscalReceiptStation == FiscalPrinterConst.FPTR_RS_RECEIPT ?
                FiscalPrinterConst.FPTR_S_JOURNAL_RECEIPT :
                FiscalPrinterConst.FPTR_S_SLIP | FiscalPrinterConst.FPTR_S_JOURNAL;
    }

    private void callIt(OutputRequest request, String methodName) throws JposException {
        if (!callNowOrLater(request)) {
            logCall(methodName);
            return;
        }
        logAsyncCall(methodName);
    }

    /**
     * Check whether a text contains the reserved word, if CapReservedWord is true. If so, a JposException will be
     * thrown with error code E_EXTENDED and extended error code EFPTR_BAD_ITEM_DESCRIPTION.
     * @param text      Text to be checked
     * @param valuename Name of the text argument to be checked.
     * @throws JposException with E_EXTENDED and EFPTR_BAD_ITEM_DESCRIPTION if the text contains the reserved word.
     */
    public void checkReserved(String text, String valuename) throws JposException {
        int index = (" " + text.replaceAll("\u00A0", " ") + " ").indexOf(" " + Data.ReservedWord + " ");
        Device.checkext(Data.CapReservedWord && !Data.ReservedWord.equals("") && index >= 0, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_DESCRIPTION, valuename + " contains reserved word");
    }

    /**
     * Convert a string containing a numerical value into a currency value (a long with 4 implicit decimals).
     * Integer values will be returned unchanged (4 decimals will be assumed), other values will be converted using
     * BigDecimal
     * @param value Value to be converted.
     * @param name  Name of the value, used if value is invalid.
     * @return Currency value if value could be converted.
     * @throws JposException with error code E_ILLEGAL if value cannot be converted or if value is out of range.
     */
    public long stringToCurrency(String value, String name) throws JposException {
        long retval;
        if (value.equals("")) {
            retval = 0;
        }
        else {
            try {
                retval = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                try {
                    BigDecimal dvat = new BigDecimal(value);
                    Device.check(dvat.scale() < 0 || dvat.scale() > 4, JposConst.JPOS_E_ILLEGAL, "Invalid decimals for " + name + ": " + value);
                    dvat = dvat.multiply(new BigDecimal(10000));
                    Device.check(dvat.compareTo(new BigDecimal(Long.MIN_VALUE)) < 0, JposConst.JPOS_E_ILLEGAL, name + " too low: " + value);
                    Device.check(dvat.compareTo(new BigDecimal(Long.MAX_VALUE)) > 0, JposConst.JPOS_E_ILLEGAL, name + " too big: " + value);
                    retval = dvat.longValue();
                } catch (NumberFormatException ee) {
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, 0, "Invalid " + name + ": " + value);
                }
            }
        }
        return retval;
    }

    /**
     * If AsyncMode is false, checks whether the device is busy, cover closed and paper present on all selected stations.
     * @param station   Selected station(s), any bit-wise combination of S_JOURNAL, S_RECEIPT and S_SLIP.
     * @throws JposException If AsyncMode is false and device busy, cover open or paper not present.
     */
    public void ifSyncCheckBusyCoverPaper(int station) throws JposException {
        if (!Data.AsyncMode) {
            Device.check(Props.State == JposConst.JPOS_S_BUSY, JposConst.JPOS_E_BUSY, "Device is busy");
            checkCoverPaper(station);
        }
    }

    /**
     * Checks whether the cover is closed and paper present on all selected stations.
     * @param station   Selected station(s), any bit-wise combination of S_JOURNAL, S_RECEIPT and S_SLIP.
     * @throws JposException If AsyncMode is false and device busy, cover open or paper not present.
     */
    public void checkCoverPaper(int station) throws JposException {
        Device.checkext(Data.CapCoverSensor && Data.CoverOpen, FiscalPrinterConst.JPOS_EFPTR_COVER_OPEN, "Device cover open");
        if ((station & FiscalPrinterConst.FPTR_S_SLIP) != 0) {
            Device.checkext(Data.CapSlpEmptySensor && Data.SlpEmpty, FiscalPrinterConst.JPOS_EFPTR_SLP_EMPTY, "No slip paper");
        }
        if ((station & FiscalPrinterConst.FPTR_S_RECEIPT) != 0) {
            Device.checkext(Data.CapRecEmptySensor && Data.RecEmpty, FiscalPrinterConst.JPOS_EFPTR_REC_EMPTY, "No receipt paper");
        }
        if ((station & FiscalPrinterConst.FPTR_S_JOURNAL) != 0) {
            Device.checkext(Data.CapJrnEmptySensor && Data.JrnEmpty, FiscalPrinterConst.JPOS_EFPTR_JRN_EMPTY, "No journal paper");
        }
    }

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public FiscalPrinterService(FiscalPrinterProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    /*
     FiscalPrinterService Properties
     */

    @Override
    public int getActualCurrency() throws JposException {
        checkEnabled();
        logGet("ActualCurrency");
        return Data.ActualCurrency;
    }

    @Override
    public String getAdditionalHeader() throws JposException {
        checkEnabled();
        logGet("AdditionalHeader");
        return Data.AdditionalHeader;
    }

    @Override
    public void setAdditionalHeader(String s) throws JposException {
        logPreSet("AdditionalHeader");
        checkEnabled();
        Device.check(!Data.CapAdditionalHeader, JposConst.JPOS_E_ILLEGAL, "Invalid property 'AdditionalHeader'");
        FiscalPrinterInterface.additionalHeader(s);
        logSet("AdditionalHeader");
    }

    @Override
    public String getAdditionalTrailer() throws JposException {
        checkEnabled();
        logGet("AdditionalTrailer");
        return Data.AdditionalTrailer;
    }

    @Override
    public void setAdditionalTrailer(String trailer) throws JposException {
        logPreSet("AdditionalTrailer");
        checkEnabled();
        Device.check(!Data.CapAdditionalTrailer, JposConst.JPOS_E_ILLEGAL, "Invalid property 'AdditionalTrailer'");
        FiscalPrinterInterface.additionalTrailer(trailer);
        logSet("AdditionalTrailer");
    }

    @Override
    public int getAmountDecimalPlace() throws JposException {
        return getAmountDecimalPlaces();
    }

    @Override
    public int getAmountDecimalPlaces() throws JposException {
        checkEnabled();
        logGet("AmountDecimalPlaces");
        return Data.AmountDecimalPlaces;
    }

    @Override
    public boolean getCapAdditionalHeader() throws JposException {
        checkOpened();
        logGet("CapAdditionalHeader");
        return Data.CapAdditionalHeader;
    }

    @Override
    public boolean getCapAdditionalLines() throws JposException {
        checkOpened();
        logGet("CapAdditionalLines");
        return Data.CapAdditionalLines;
    }

    @Override
    public boolean getCapAdditionalTrailer() throws JposException {
        checkOpened();
        logGet("CapAdditionalTrailer");
        return Data.CapAdditionalTrailer;
    }

    @Override
    public boolean getCapAmountAdjustment() throws JposException {
        checkOpened();
        logGet("CapAmountAdjustment");
        return Data.CapAmountAdjustment;
    }

    @Override
    public boolean getCapAmountNotPaid() throws JposException {
        checkOpened();
        logGet("CapAmountNotPaid");
        return Data.CapAmountNotPaid;
    }

    @Override
    public boolean getCapChangeDue() throws JposException {
        checkOpened();
        logGet("CapChangeDue");
        return Data.CapChangeDue;
    }

    @Override
    public boolean getCapCheckTotal() throws JposException {
        checkOpened();
        logGet("CapCheckTotal");
        return Data.CapCheckTotal;
    }

    @Override
    public boolean getCapCoverSensor() throws JposException {
        checkOpened();
        logGet("CapCoverSensor");
        return Data.CapCoverSensor;
    }

    @Override
    public boolean getCapDoubleWidth() throws JposException {
        checkOpened();
        logGet("CapDoubleWidth");
        return Data.CapDoubleWidth;
    }

    @Override
    public boolean getCapDuplicateReceipt() throws JposException {
        checkOpened();
        logGet("CapDuplicateReceipt");
        return Data.CapDuplicateReceipt;
    }

    @Override
    public boolean getCapEmptyReceiptIsVoidable() throws JposException {
        checkOpened();
        logGet("CapEmptyReceiptIsVoidable");
        return Data.CapEmptyReceiptIsVoidable;
    }

    @Override
    public boolean getCapFiscalReceiptStation() throws JposException {
        checkOpened();
        logGet("CapFiscalReceiptStation");
        return Data.CapFiscalReceiptStation;
    }

    @Override
    public boolean getCapFiscalReceiptType() throws JposException {
        checkOpened();
        logGet("CapFiscalReceiptType");
        return Data.CapFiscalReceiptType;
    }

    @Override
    public boolean getCapFixedOutput() throws JposException {
        checkOpened();
        logGet("CapFixedOutput");
        return Data.CapFixedOutput;
    }

    @Override
    public boolean getCapHasVatTable() throws JposException {
        checkOpened();
        logGet("CapHasVatTable");
        return Data.CapHasVatTable;
    }

    @Override
    public boolean getCapIndependentHeader() throws JposException {
        checkOpened();
        logGet("CapIndependentHeader");
        return Data.CapIndependentHeader;
    }

    @Override
    public boolean getCapItemList() throws JposException {
        checkOpened();
        logGet("CapItemList");
        return Data.CapItemList;
    }

    @Override
    public boolean getCapJrnEmptySensor() throws JposException {
        checkOpened();
        logGet("CapJrnEmptySensor");
        return Data.CapJrnEmptySensor;
    }

    @Override
    public boolean getCapJrnNearEndSensor() throws JposException {
        checkOpened();
        logGet("CapJrnNearEndSensor");
        return Data.CapJrnNearEndSensor;
    }

    @Override
    public boolean getCapJrnPresent() throws JposException {
        checkOpened();
        logGet("CapJrnPresent");
        return Data.CapJrnPresent;
    }

    @Override
    public boolean getCapMultiContractor() throws JposException {
        checkOpened();
        logGet("CapMultiContractor");
        return Data.CapMultiContractor;
    }

    @Override
    public boolean getCapNonFiscalMode() throws JposException {
        checkOpened();
        logGet("CapNonFiscalMode");
        return Data.CapNonFiscalMode;
    }

    @Override
    public boolean getCapOnlyVoidLastItem() throws JposException {
        checkOpened();
        logGet("CapOnlyVoidLastItem");
        return Data.CapOnlyVoidLastItem;
    }

    @Override
    public boolean getCapOrderAdjustmentFirst() throws JposException {
        checkOpened();
        logGet("CapOrderAdjustmentFirst");
        return Data.CapOrderAdjustmentFirst;
    }

    @Override
    public boolean getCapPackageAdjustment() throws JposException {
        checkOpened();
        logGet("CapPackageAdjustment");
        return Data.CapPackageAdjustment;
    }

    @Override
    public boolean getCapPercentAdjustment() throws JposException {
        checkOpened();
        logGet("CapPercentAdjustment");
        return Data.CapPercentAdjustment;
    }

    @Override
    public boolean getCapPositiveAdjustment() throws JposException {
        checkOpened();
        logGet("CapPositiveAdjustment");
        return Data.CapPositiveAdjustment;
    }

    @Override
    public boolean getCapPositiveSubtotalAdjustment() throws JposException {
        checkOpened();
        logGet("CapPositiveSubtotalAdjustment");
        return Data.CapPositiveSubtotalAdjustment;
    }

    @Override
    public boolean getCapPostPreLine() throws JposException {
        checkOpened();
        logGet("CapPostPreLine");
        return Data.CapPostPreLine;
    }

    @Override
    public boolean getCapPowerLossReport() throws JposException {
        checkOpened();
        logGet("CapPowerLossReport");
        return Data.CapPowerLossReport;
    }

    @Override
    public boolean getCapPredefinedPaymentLines() throws JposException {
        checkOpened();
        logGet("CapPredefinedPaymentLines");
        return Data.CapPredefinedPaymentLines;
    }

    @Override
    public boolean getCapReceiptNotPaid() throws JposException {
        checkOpened();
        logGet("CapReceiptNotPaid");
        return Data.CapReceiptNotPaid;
    }

    @Override
    public boolean getCapRecEmptySensor() throws JposException {
        checkOpened();
        logGet("CapRecEmptySensor");
        return Data.CapRecEmptySensor;
    }

    @Override
    public boolean getCapRecNearEndSensor() throws JposException {
        checkOpened();
        logGet("CapRecNearEndSensor");
        return Data.CapRecNearEndSensor;
    }

    @Override
    public boolean getCapRecPresent() throws JposException {
        checkOpened();
        logGet("CapRecPresent");
        return Data.CapRecPresent;
    }

    @Override
    public boolean getCapRemainingFiscalMemory() throws JposException {
        checkOpened();
        logGet("CapRemainingFiscalMemory");
        return Data.CapRemainingFiscalMemory;
    }

    @Override
    public boolean getCapReservedWord() throws JposException {
        checkOpened();
        logGet("CapReservedWord");
        return Data.CapReservedWord;
    }

    @Override
    public boolean getCapSetCurrency() throws JposException {
        checkOpened();
        logGet("CapSetCurrency");
        return Data.CapSetCurrency;
    }

    @Override
    public boolean getCapSetHeader() throws JposException {
        checkOpened();
        logGet("CapSetHeader");
        return Data.CapSetHeader;
    }

    @Override
    public boolean getCapSetPOSID() throws JposException {
        checkOpened();
        logGet("CapSetPOSID");
        return Data.CapSetPOSID;
    }

    @Override
    public boolean getCapSetStoreFiscalID() throws JposException {
        checkOpened();
        logGet("CapSetStoreFiscalID");
        return Data.CapSetStoreFiscalID;
    }

    @Override
    public boolean getCapSetTrailer() throws JposException {
        checkOpened();
        logGet("CapSetTrailer");
        return Data.CapSetTrailer;
    }

    @Override
    public boolean getCapSetVatTable() throws JposException {
        checkOpened();
        logGet("CapSetVatTable");
        return Data.CapSetVatTable;
    }

    @Override
    public boolean getCapSlpEmptySensor() throws JposException {
        checkOpened();
        logGet("CapSlpEmptySensor");
        return Data.CapSlpEmptySensor;
    }

    @Override
    public boolean getCapSlpFiscalDocument() throws JposException {
        checkOpened();
        logGet("CapSlpFiscalDocument");
        return Data.CapSlpFiscalDocument;
    }

    @Override
    public boolean getCapSlpFullSlip() throws JposException {
        checkOpened();
        logGet("CapSlpFullSlip");
        return Data.CapSlpFullSlip;
    }

    @Override
    public boolean getCapSlpNearEndSensor() throws JposException {
        checkOpened();
        logGet("CapSlpNearEndSensor");
        return Data.CapSlpNearEndSensor;
    }

    @Override
    public boolean getCapSlpPresent() throws JposException {
        checkOpened();
        logGet("CapSlpPresent");
        return Data.CapSlpPresent;
    }

    @Override
    public boolean getCapSlpValidation() throws JposException {
        checkOpened();
        logGet("CapSlpValidation");
        return Data.CapSlpValidation;
    }

    @Override
    public boolean getCapSubAmountAdjustment() throws JposException {
        checkOpened();
        logGet("CapSubAmountAdjustment");
        return Data.CapSubAmountAdjustment;
    }

    @Override
    public boolean getCapSubPercentAdjustment() throws JposException {
        checkOpened();
        logGet("CapSubPercentAdjustment");
        return Data.CapSubPercentAdjustment;
    }

    @Override
    public boolean getCapSubtotal() throws JposException {
        checkOpened();
        logGet("CapSubtotal");
        return Data.CapSubtotal;
    }

    @Override
    public boolean getCapTotalizerType() throws JposException {
        checkOpened();
        logGet("CapTotalizerType");
        return Data.CapTotalizerType;
    }

    @Override
    public boolean getCapTrainingMode() throws JposException {
        checkOpened();
        logGet("CapTrainingMode");
        return Data.CapTrainingMode;
    }

    @Override
    public boolean getCapValidateJournal() throws JposException {
        checkOpened();
        logGet("CapValidateJournal");
        return Data.CapValidateJournal;
    }

    @Override
    public boolean getCapXReport() throws JposException {
        checkOpened();
        logGet("CapXReport");
        return Data.CapXReport;
    }

    @Override
    public String getChangeDue() throws JposException {
        checkOpened();
        logGet("ChangeDue");
        return Data.ChangeDue;
    }

    @Override
    public void setChangeDue(String cashreturn) throws JposException {
        logPreSet("ChangeDue");
        checkOpened();
        Device.check(!Data.CapChangeDue, JposConst.JPOS_E_ILLEGAL, "Invalid property 'ChangeDue'");
        FiscalPrinterInterface.changeDue(cashreturn);
        logSet("ChangeDue");
    }

    @Override
    public boolean getCheckTotal() throws JposException {
        checkOpened();
        logGet("CheckTotal");
        return Data.CheckTotal;
    }

    @Override
    public void setCheckTotal(boolean check) throws JposException {
        logPreSet("CheckTotal");
        checkOpened();
        Device.check(!Data.CapCheckTotal, JposConst.JPOS_E_ILLEGAL, "Invalid property 'CheckTotal'");
        FiscalPrinterInterface.checkTotal(check);
        logSet("CheckTotal");
    }

    @Override
    public int getContractorId() throws JposException {
        checkEnabled();
        logGet("ContractorId");
        return Data.ContractorId;
    }

    @Override
    public void setContractorId(int id) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_CID_FIRST,
                FiscalPrinterConst.FPTR_CID_SECOND,
                FiscalPrinterConst.FPTR_CID_SINGLE
        };
        logPreSet("ContractorId");
        checkOpened();
        Device.check(!Data.CapMultiContractor, JposConst.JPOS_E_ILLEGAL, "Changing 'ContractorId' invalid");
        Device.checkMember(id, allowed,JposConst.JPOS_E_ILLEGAL, "Invalid contractor ID: " + id);
        FiscalPrinterInterface.contractorId(id);
        logSet("ContractorId");
    }

    @Override
    public int getCountryCode() throws JposException {
        checkFirstEnabled();
        logGet("CountryCode");
        return Data.CountryCode;
    }

    @Override
    public boolean getCoverOpen() throws JposException {
        checkEnabled();
        logGet("CoverOpen");
        return Data.CoverOpen;
    }

    @Override
    public int getDateType() throws JposException {
        checkEnabled();
        logGet("DateType");
        return Data.DateType;
    }

    @Override
    public void setDateType(int type) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_DT_CONF,
                FiscalPrinterConst.FPTR_DT_EOD,
                FiscalPrinterConst.FPTR_DT_RESET,
                FiscalPrinterConst.FPTR_DT_RTC,
                FiscalPrinterConst.FPTR_DT_VAT,
                FiscalPrinterConst.FPTR_DT_START,
                FiscalPrinterConst.FPTR_DT_TICKET_START,
                FiscalPrinterConst.FPTR_DT_TICKET_END
        };
        logPreSet("DateType");
        checkOpened();
        Device.checkMember(type, allowed, JposConst.JPOS_E_ILLEGAL, "Illegal date specifier: " + type);
        FiscalPrinterInterface.dateType(type);
        logSet("DateType");
    }

    @Override
    public boolean getDayOpened() throws JposException {
        checkEnabled();
        logGet("DayOpened");
        return Data.DayOpened;
    }

    @Override
    public int getDescriptionLength() throws JposException {
        checkOpened();
        logGet("DescriptionLength");
        return Data.DescriptionLength;
    }

    @Override
    public boolean getDuplicateReceipt() throws JposException {
        checkOpened();
        logGet("DuplicateReceipt");
        return Data.DuplicateReceipt;
    }

    @Override
    public void setDuplicateReceipt(boolean yes) throws JposException {
        logPreSet("DuplicateReceipt");
        checkOpened();
        Device.check(!Data.CapDuplicateReceipt, JposConst.JPOS_E_ILLEGAL, "Changing 'DuplicateReceipt' invalid");
        FiscalPrinterInterface.duplicateReceipt(yes);
        logSet("DuplicateReceipt");
    }

    @Override
    public int getErrorLevel() throws JposException {
        checkOpened();
        logGet("ErrorLevel");
        return Data.ErrorLevel;
    }

    @Override
    public int getErrorOutID() throws JposException {
        checkOpened();
        logGet("ErrorOutID");
        return Data.ErrorOutID;
    }

    @Override
    public int getErrorState() throws JposException {
        checkOpened();
        logGet("ErrorState");
        return Data.ErrorState;
    }

    @Override
    public int getErrorStation() throws JposException {
        checkOpened();
        logGet("ErrorStation");
        return Data.ErrorStation;
    }

    @Override
    public String getErrorString() throws JposException {
        checkOpened();
        logGet("ErrorString");
        return Data.ErrorString;
    }

    @Override
    public int getFiscalReceiptStation() throws JposException {
        checkEnabled();
        logGet("FiscalReceiptStation");
        return Data.FiscalReceiptStation;
    }

    @Override
    public void setFiscalReceiptStation(int station) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_RS_RECEIPT,
                Data.CapFiscalReceiptStation ? FiscalPrinterConst.FPTR_RS_SLIP : FiscalPrinterConst.FPTR_RS_RECEIPT
        };
        logPreSet("FiscalReceiptStation");
        checkOpened();
        Device.checkMember(station, allowed, JposConst.JPOS_E_ILLEGAL, "Invalid station: " + station);
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_MONITOR && Data.PrinterState != FiscalPrinterConst.FPTR_PS_LOCKED, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Neither locked nor in monitor state");
        Device.check(station == FiscalPrinterConst.FPTR_RS_SLIP && !Data.CapSlpPresent, JposConst.JPOS_E_ILLEGAL, "No slip station");
        Device.check(station == FiscalPrinterConst.FPTR_RS_RECEIPT && !Data.CapRecPresent, JposConst.JPOS_E_ILLEGAL, "No receipt station");
        FiscalPrinterInterface.fiscalReceiptStation(station);
        logSet("FiscalReceiptStation");
    }

    @Override
    public int getFiscalReceiptType() throws JposException {
        checkEnabled();
        logGet("FiscalReceiptType");
        return Data.FiscalReceiptType;
    }

    @Override
    public void setFiscalReceiptType(int type) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_RT_CASH_IN,
                FiscalPrinterConst.FPTR_RT_CASH_OUT,
                FiscalPrinterConst.FPTR_RT_GENERIC,
                FiscalPrinterConst.FPTR_RT_SALES,
                FiscalPrinterConst.FPTR_RT_SERVICE,
                FiscalPrinterConst.FPTR_RT_SIMPLE_INVOICE,
                FiscalPrinterConst.FPTR_RT_REFUND
        };
        logPreSet("FiscalReceiptType");
        checkOpened();
        Device.check(!Data.CapFiscalReceiptType, JposConst.JPOS_E_ILLEGAL, "Invalid property 'FiscalReceiptType'");
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_MONITOR, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in monitor state");
        Device.checkMember(type, allowed, JposConst.JPOS_E_ILLEGAL, "Invalid receipt type: " + type);
        FiscalPrinterInterface.fiscalReceiptType(type);
        logSet("FiscalReceiptType");
    }

    @Override
    public boolean getFlagWhenIdle() throws JposException {
        checkOpened();
        logGet("FlagWhenIdle");
        return Data.FlagWhenIdle;
    }

    @Override
    public void setFlagWhenIdle(boolean b) throws JposException {
        logPreSet("FlagWhenIdle");
        checkOpened();
        FiscalPrinterInterface.flagWhenIdle(b);
        logSet("FlagWhenIdle");
    }

    @Override
    public boolean getJrnEmpty() throws JposException {
        checkEnabled();
        logGet("JrnEmpty");
        return Data.JrnEmpty;
    }

    @Override
    public boolean getJrnNearEnd() throws JposException {
        checkEnabled();
        logGet("JrnNearEnd");
        return Data.JrnNearEnd;
    }

    @Override
    public int getMessageLength() throws JposException {
        checkOpened();
        logGet("MessageLength");
        return Data.MessageLength;
    }

    @Override
    public int getMessageType() throws JposException {
        checkOpened();
        logGet("MessageType");
        return Data.MessageType;
    }

    @Override
    public void setMessageType(int type) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_MT_ADVANCE,
                FiscalPrinterConst.FPTR_MT_ADVANCE_PAID,
                FiscalPrinterConst.FPTR_MT_AMOUNT_TO_BE_PAID,
                FiscalPrinterConst.FPTR_MT_AMOUNT_TO_BE_PAID_BACK,
                FiscalPrinterConst.FPTR_MT_CARD,
                FiscalPrinterConst.FPTR_MT_CARD_NUMBER,
                FiscalPrinterConst.FPTR_MT_CARD_TYPE,
                FiscalPrinterConst.FPTR_MT_CASH,
                FiscalPrinterConst.FPTR_MT_CASHIER,
                FiscalPrinterConst.FPTR_MT_CASH_REGISTER_NUMBER,
                FiscalPrinterConst.FPTR_MT_CHANGE,
                FiscalPrinterConst.FPTR_MT_CHEQUE,
                FiscalPrinterConst.FPTR_MT_CLIENT_NUMBER,
                FiscalPrinterConst.FPTR_MT_CLIENT_SIGNATURE,
                FiscalPrinterConst.FPTR_MT_COUNTER_STATE,
                FiscalPrinterConst.FPTR_MT_CREDIT_CARD,
                FiscalPrinterConst.FPTR_MT_CURRENCY,
                FiscalPrinterConst.FPTR_MT_CURRENCY_VALUE,
                FiscalPrinterConst.FPTR_MT_DEPOSIT,
                FiscalPrinterConst.FPTR_MT_DEPOSIT_RETURNED,
                FiscalPrinterConst.FPTR_MT_DOT_LINE,
                FiscalPrinterConst.FPTR_MT_DRIVER_NUMB,
                FiscalPrinterConst.FPTR_MT_EMPTY_LINE,
                FiscalPrinterConst.FPTR_MT_FREE_TEXT,
                FiscalPrinterConst.FPTR_MT_FREE_TEXT_WITH_DAY_LIMIT,
                FiscalPrinterConst.FPTR_MT_GIVEN_DISCOUNT,
                FiscalPrinterConst.FPTR_MT_LOCAL_CREDIT,
                FiscalPrinterConst.FPTR_MT_MILEAGE_KM,
                FiscalPrinterConst.FPTR_MT_NOTE,
                FiscalPrinterConst.FPTR_MT_PAID,
                FiscalPrinterConst.FPTR_MT_PAY_IN,
                FiscalPrinterConst.FPTR_MT_POINT_GRANTED,
                FiscalPrinterConst.FPTR_MT_POINTS_BONUS,
                FiscalPrinterConst.FPTR_MT_POINTS_RECEIPT,
                FiscalPrinterConst.FPTR_MT_POINTS_TOTAL,
                FiscalPrinterConst.FPTR_MT_PROFITED,
                FiscalPrinterConst.FPTR_MT_RATE,
                FiscalPrinterConst.FPTR_MT_REGISTER_NUMB,
                FiscalPrinterConst.FPTR_MT_SHIFT_NUMBER,
                FiscalPrinterConst.FPTR_MT_STATE_OF_AN_ACCOUNT,
                FiscalPrinterConst.FPTR_MT_SUBSCRIPTION,
                FiscalPrinterConst.FPTR_MT_TABLE,
                FiscalPrinterConst.FPTR_MT_THANK_YOU_FOR_LOYALTY,
                FiscalPrinterConst.FPTR_MT_TRANSACTION_NUMB,
                FiscalPrinterConst.FPTR_MT_VALID_TO,
                FiscalPrinterConst.FPTR_MT_VOUCHER,
                FiscalPrinterConst.FPTR_MT_VOUCHER_PAID,
                FiscalPrinterConst.FPTR_MT_VOUCHER_VALUE,
                FiscalPrinterConst.FPTR_MT_WITH_DISCOUNT,
                FiscalPrinterConst.FPTR_MT_WITHOUT_UPLIFT
        };
        logPreSet("MessageType");
        checkOpened();
        Device.checkMember(type, allowed, JposConst.JPOS_E_ILLEGAL, "Invalid message type: " + type);
        FiscalPrinterInterface.messageType(type);
        logSet("MessageType");
    }

    @Override
    public int getNumHeaderLines() throws JposException {
        checkOpened();
        logGet("NumHeaderLines");
        return Data.NumHeaderLines;
    }

    @Override
    public int getNumTrailerLines() throws JposException {
        checkOpened();
        logGet("NumTrailerLines");
        return Data.NumTrailerLines;
    }

    @Override
    public int getNumVatRates() throws JposException {
        checkOpened();
        logGet("NumVatRates");
        return Data.NumVatRates;
    }

    @Override
    public String getPostLine() throws JposException {
        checkFirstEnabled();
        logGet("PostLine");
        return Data.PostLine;
    }

    @Override
    public void setPostLine(String text) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT,
                FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT_TOTAL
        };
        logPreSet("PostLine");
        checkEnabled();
        Device.check(!Data.CapPostPreLine, JposConst.JPOS_E_ILLEGAL, "Post lines not supported");
        Device.checkext(!Device.member(Data.PrinterState, allowed), FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in receipt or receipt total state");
        FiscalPrinterInterface.postLine(text == null ? "" : text);
        logSet("PostLine");
    }

    @Override
    public String getPredefinedPaymentLines() throws JposException {
        checkFirstEnabled();
        logGet("PredefinedPaymentLines");
        return Data.PredefinedPaymentLines;
    }

    @Override
    public String getPreLine() throws JposException {
        checkFirstEnabled();
        logGet("PreLine");
        return Data.PreLine;
    }

    @Override
    public void setPreLine(String text) throws JposException {
        logPreSet("PreLine");
        checkEnabled();
        Device.check(!Data.CapPostPreLine, JposConst.JPOS_E_ILLEGAL, "Pre lines not supported");
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in receipt state");
        FiscalPrinterInterface.preLine(text == null ? "" : text);
        logSet("PreLine");
    }

    @Override
    public int getPrinterState() throws JposException {
        checkFirstEnabled();
        logGet("PrinterState");
        return Data.PrinterState;
    }

    @Override
    public int getQuantityDecimalPlaces() throws JposException {
        checkFirstEnabled();
        logGet("QuantityDecimalPlaces");
        return Data.QuantityDecimalPlaces;
    }

    @Override
    public int getQuantityLength() throws JposException {
        checkFirstEnabled();
        logGet("QuantityLength");
        return Data.QuantityLength;
    }

    @Override
    public boolean getRecEmpty() throws JposException {
        checkEnabled();
        logGet("RecEmpty");
        return Data.RecEmpty;
    }

    @Override
    public boolean getRecNearEnd() throws JposException {
        checkEnabled();
        logGet("RecNearEnd");
        return Data.RecNearEnd;
    }

    @Override
    public int getRemainingFiscalMemory() throws JposException {
        checkEnabled();
        logGet("RemainingFiscalMemory");
        return Data.RemainingFiscalMemory;
    }

    @Override
    public String getReservedWord() throws JposException {
        checkOpened();
        logGet("ReservedWord");
        return Data.ReservedWord;
    }

    @Override
    public boolean getSlpEmpty() throws JposException {
        checkEnabled();
        logGet("SlpEmpty");
        return Data.SlpEmpty;
    }

    @Override
    public boolean getSlpNearEnd() throws JposException {
        checkEnabled();
        logGet("SlpNearEnd");
        return Data.SlpNearEnd;
    }

    @Override
    public int getSlipSelection() throws JposException {
        checkClaimed();
        logGet("SlipSelection");
        return Data.SlipSelection;
    }

    @Override
    public void setSlipSelection(int type) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_SS_FULL_LENGTH,
                FiscalPrinterConst.FPTR_SS_VALIDATION
        };
        logPreSet("SlipSelection");
        checkEnabled();
        Device.check(!Data.CapSlpPresent, JposConst.JPOS_E_ILLEGAL, "Slip station not supported");
        Device.checkMember(type, allowed, JposConst.JPOS_E_ILLEGAL, "Invalid document type: " + type);
        Device.check(type == FiscalPrinterConst.FPTR_SS_VALIDATION && !Data.CapSlpValidation, JposConst.JPOS_E_ILLEGAL, "Validation not supported");
        FiscalPrinterInterface.slipSelection(type);
        logSet("SlipSelection");
    }

    @Override
    public int getTotalizerType() throws JposException {
        checkEnabled();
        logGet("TotalizerType");
        return Data.TotalizerType;
    }

    @Override
    public void setTotalizerType(int type) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_TT_DOCUMENT,
                FiscalPrinterConst.FPTR_TT_DAY,
                FiscalPrinterConst.FPTR_TT_RECEIPT,
                FiscalPrinterConst.FPTR_TT_GRAND
        };
        logPreSet("TotalizerType");
        checkEnabled();
        Device.check(!Data.CapTotalizerType, JposConst.JPOS_E_ILLEGAL, "Invalid property 'TotalizerType'");
        Device.checkMember(type, allowed, JposConst.JPOS_E_ILLEGAL, "Invalid totalizer type: " + type);
        FiscalPrinterInterface.totalizerType(type);
        logSet("TotalizerType");
    }

    @Override
    public boolean getTrainingModeActive() throws JposException {
        checkEnabled();
        logGet("TrainingModeActive");
        return Data.TrainingModeActive;
    }

    /*
     Synchronous methods
     */

    @Override
    public void beginFiscalDocument(int documentAmount) throws JposException {
        logPreCall("BeginFiscalDocument", "" + documentAmount);
        checkEnabled();
        Device.check(!Data.CapSlpPresent || !Data.CapSlpFiscalDocument, JposConst.JPOS_E_ILLEGAL, "Fiscal document printing not supported");
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_MONITOR, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Cannot change to fiscal document state");
        FiscalPrinterInterface.beginFiscalDocument(documentAmount);
        logCall("BeginFiscalDocument");
    }

    @Override
    public void beginFiscalReceipt(boolean printHeader) throws JposException {
        logPreCall("BeginFiscalReceipt", "" + printHeader);
        checkEnabled();
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_MONITOR, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Cannot change to fiscal receipt state");
        FiscalPrinterInterface.beginFiscalReceipt(printHeader);
        logCall("BeginFiscalReceipt");
    }

    @Override
    public void beginFixedOutput(int station, int documentType) throws JposException {
        logPreCall("BeginFixedOutput", "" + station + ", " + documentType);
        checkEnabled();
        Device.check(!Data.CapFixedOutput, JposConst.JPOS_E_ILLEGAL, "Non-fiscal fixed text printing not supported");
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_MONITOR, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Cannot change to non-fiscal document state");
        switch (station) {
            case FiscalPrinterConst.FPTR_S_RECEIPT:
                Device.check(!Data.CapRecPresent, JposConst.JPOS_E_ILLEGAL, "Unsupported station: Receipt");
                break;
            case FiscalPrinterConst.FPTR_S_SLIP:
                Device.check(!Data.CapSlpPresent, JposConst.JPOS_E_ILLEGAL, "Unsupported station: Slip");
                break;
            default:
                throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid station: " + station);
        }
        FiscalPrinterInterface.beginFixedOutput(station, documentType);
        logCall("BeginFixedOutput");
    }

    @Override
    public void beginInsertion(int timeout) throws JposException {
        logPreCall("BeginInsertion", "" + timeout);
        checkEnabled();
        Device.check(!Data.CapSlpPresent, JposConst.JPOS_E_ILLEGAL, "Slip station not supported");
        Device.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout value: " + timeout);
        Device.checkext(Data.PrinterState == FiscalPrinterConst.FPTR_PS_LOCKED, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Device locked");
        FiscalPrinterInterface.beginInsertion(timeout);
        logCall("BeginInsertion");
    }

    @Override
    public void beginItemList(int vatID) throws JposException {
        logPreCall("BeginItemList", "" + vatID);
        checkEnabled();
        Device.check(!Data.CapItemList, JposConst.JPOS_E_ILLEGAL, "Non-fiscal item list printing not supported");
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_MONITOR, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Cannot change to item list state");
        FiscalPrinterInterface.beginItemList(vatID);
        logCall("BeginItemList");
    }

    @Override
    public void beginNonFiscal() throws JposException {
        logPreCall("BeginNonFiscal");
        checkEnabled();
        Device.check(!Data.CapNonFiscalMode, JposConst.JPOS_E_ILLEGAL, "Non-fiscal text printing not supported");
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_MONITOR, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Cannot change to non-fiscal text printing state");
        FiscalPrinterInterface.beginNonFiscal();
        logCall("BeginNonFiscal");
    }

    @Override
    public void beginRemoval(int timeout) throws JposException {
        logPreCall("BeginRemoval", "" + timeout);
        checkEnabled();
        Device.check(!Data.CapSlpPresent, JposConst.JPOS_E_ILLEGAL, "Slip station not supported");
        Device.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout value: " + timeout);
        Device.checkext(Data.PrinterState == FiscalPrinterConst.FPTR_PS_LOCKED, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Device locked");
        FiscalPrinterInterface.beginRemoval(timeout);
        logCall("BeginRemoval");
    }

    @Override
    public void beginTraining() throws JposException {
        logPreCall("BeginTraining");
        checkEnabled();
        Device.check(!Data.CapTrainingMode, JposConst.JPOS_E_ILLEGAL, "Training mode not supported");
        Device.checkext(Data.TrainingModeActive, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Device just in training mode");
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_MONITOR, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Cannot change to training mode");
        FiscalPrinterInterface.beginTraining();
        logCall("BeginTraining");
    }

    @Override
    public void clearError() throws JposException {
        logPreCall("ClearError");
        checkEnabled();
        FiscalPrinterInterface.clearError();
        logCall("ClearError");
    }

    @Override
    public void endFiscalDocument() throws JposException {
        logPreCall("EndFiscalDocument");
        checkEnabled();
        Device.check(!Data.CapSlpFiscalDocument, JposConst.JPOS_E_ILLEGAL, "Fiscal document printing not supported");
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_DOCUMENT, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Invalid printing state: " + Data.PrinterState);
        FiscalPrinterInterface.endFiscalDocument();
        logCall("EndFiscalDocument");
    }

    @Override
    public void endFiscalReceipt(boolean printHeader) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT,
                FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT_ENDING
        };
        logPreCall("EndFiscalReceipt");
        checkEnabled();
        Device.checkext(!Device.member(Data.PrinterState, allowed), FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Invalid printing state: " + Data.PrinterState);
        FiscalPrinterInterface.endFiscalReceipt(printHeader);
        logCall("EndFiscalReceipt");
    }

    @Override
    public void endFixedOutput() throws JposException {
        logPreCall("EndFixedOutput");
        checkEnabled();
        Device.check(!Data.CapFixedOutput, JposConst.JPOS_E_ILLEGAL, "Non-fiscal fixed text printing not supported");
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_FIXED_OUTPUT, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Invalid printing state: " + Data.PrinterState);
        FiscalPrinterInterface.endFixedOutput();
        logCall("EndFixedOutput");
    }

    @Override
    public void endInsertion() throws JposException {
        logPreCall("EndInsertion");
        checkEnabled();
        Device.check(!Data.CapSlpPresent, JposConst.JPOS_E_ILLEGAL, "Slip station not supported");
        Device.checkext(Data.PrinterState == FiscalPrinterConst.FPTR_PS_LOCKED, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Device locked");
        FiscalPrinterInterface.endInsertion();
        logCall("EndInsertion");
    }

    @Override
    public void endItemList() throws JposException {
        logPreCall("EndItemList");
        checkEnabled();
        Device.check(!Data.CapItemList, JposConst.JPOS_E_ILLEGAL, "Non-fiscal item validation printing not supported");
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_ITEM_LIST, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Invalid printing state: " + Data.PrinterState);
        FiscalPrinterInterface.endItemList();
        logCall("EndItemList");
    }

    @Override
    public void endNonFiscal() throws JposException {
        logPreCall("EndNonFiscal");
        checkEnabled();
        Device.check(!Data.CapNonFiscalMode, JposConst.JPOS_E_ILLEGAL, "Non-fiscal text printing not supported");
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_NONFISCAL, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Invalid printing state: " + Data.PrinterState);
        FiscalPrinterInterface.endNonFiscal();
        logCall("EndNonFiscal");
    }

    @Override
    public void endRemoval() throws JposException {
        logPreCall("EndRemoval");
        checkEnabled();
        Device.check(!Data.CapSlpPresent, JposConst.JPOS_E_ILLEGAL, "Slip station not supported");
        Device.checkext(Data.PrinterState == FiscalPrinterConst.FPTR_PS_LOCKED, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Device locked");
        FiscalPrinterInterface.endRemoval();
        logCall("EndRemoval");
    }

    @Override
    public void endTraining() throws JposException {
        logPreCall("EndTraining");
        checkEnabled();
        Device.check(!Data.CapTrainingMode, JposConst.JPOS_E_ILLEGAL, "Training mode not supported");
        Device.checkext(!Data.TrainingModeActive, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Device not in training mode");
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_MONITOR, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Cannot disable training mode");
        FiscalPrinterInterface.endTraining();
        logCall("EndTraining");
    }

    @Override
    public void getData(int dataItem, int[] optArgs, String[] data) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_GD_FIRMWARE,
                FiscalPrinterConst.FPTR_GD_PRINTER_ID,
                FiscalPrinterConst.FPTR_GD_CURRENT_TOTAL,
                FiscalPrinterConst.FPTR_GD_DAILY_TOTAL,
                FiscalPrinterConst.FPTR_GD_GRAND_TOTAL,
                FiscalPrinterConst.FPTR_GD_MID_VOID,
                FiscalPrinterConst.FPTR_GD_NOT_PAID,
                FiscalPrinterConst.FPTR_GD_RECEIPT_NUMBER,
                FiscalPrinterConst.FPTR_GD_REFUND,
                FiscalPrinterConst.FPTR_GD_REFUND_VOID,
                FiscalPrinterConst.FPTR_GD_NUMB_CONFIG_BLOCK,
                FiscalPrinterConst.FPTR_GD_NUMB_CURRENCY_BLOCK,
                FiscalPrinterConst.FPTR_GD_NUMB_HDR_BLOCK,
                FiscalPrinterConst.FPTR_GD_NUMB_RESET_BLOCK,
                FiscalPrinterConst.FPTR_GD_NUMB_VAT_BLOCK,
                FiscalPrinterConst.FPTR_GD_FISCAL_DOC,
                FiscalPrinterConst.FPTR_GD_FISCAL_DOC_VOID,
                FiscalPrinterConst.FPTR_GD_FISCAL_REC,
                FiscalPrinterConst.FPTR_GD_FISCAL_REC_VOID,
                FiscalPrinterConst.FPTR_GD_NONFISCAL_DOC,
                FiscalPrinterConst.FPTR_GD_NONFISCAL_DOC_VOID,
                FiscalPrinterConst.FPTR_GD_NONFISCAL_REC,
                FiscalPrinterConst.FPTR_GD_RESTART,
                FiscalPrinterConst.FPTR_GD_SIMP_INVOICE,
                FiscalPrinterConst.FPTR_GD_Z_REPORT,
                FiscalPrinterConst.FPTR_GD_TENDER,
                FiscalPrinterConst.FPTR_GD_LINECOUNT,
                FiscalPrinterConst.FPTR_GD_DESCRIPTION_LENGTH
        };
        Device.check(optArgs == null || data == null, JposConst.JPOS_E_ILLEGAL, "Unexpected null pointer argument");
        Device.check(optArgs.length * data.length != 1, JposConst.JPOS_E_ILLEGAL, "Bad dimension of argument pointer");
        logPreCall("GetData", "" + dataItem + ", " + optArgs[0]);
        checkEnabled();
        checkBusySync();
        Device.checkMember(dataItem, allowed, JposConst.JPOS_E_ILLEGAL, "Data item invalid: " + dataItem);
        FiscalPrinterInterface.getData(dataItem, optArgs, data);
        logCall("GetData", "" + dataItem + ", " + optArgs[0] + ", " + data[0]);
    }

    @Override
    public void getDate(String[] date) throws JposException {
        logPreCall("GetDate");
        checkEnabled();
        Device.check(date == null, JposConst.JPOS_E_ILLEGAL, "Unexpected null pointer argument");
        Device.check(date.length != 1, JposConst.JPOS_E_ILLEGAL, "Bad dimension of argument pointer");
        FiscalPrinterInterface.getDate(date);
        logCall("GetDate", "" + date[0]);
    }

    @Override
    public void getTotalizer(int vatID, int optArgs, String[] data) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_GT_GROSS,
                FiscalPrinterConst.FPTR_GT_NET,
                FiscalPrinterConst.FPTR_GT_DISCOUNT,
                FiscalPrinterConst.FPTR_GT_DISCOUNT_VOID,
                FiscalPrinterConst.FPTR_GT_ITEM,
                FiscalPrinterConst.FPTR_GT_ITEM_VOID,
                FiscalPrinterConst.FPTR_GT_NOT_PAID,
                FiscalPrinterConst.FPTR_GT_REFUND,
                FiscalPrinterConst.FPTR_GT_REFUND_VOID,
                FiscalPrinterConst.FPTR_GT_SUBTOTAL_DISCOUNT,
                FiscalPrinterConst.FPTR_GT_SUBTOTAL_DISCOUNT_VOID,
                FiscalPrinterConst.FPTR_GT_SUBTOTAL_SURCHARGES,
                FiscalPrinterConst.FPTR_GT_SUBTOTAL_SURCHARGES_VOID,
                FiscalPrinterConst.FPTR_GT_SURCHARGE,
                FiscalPrinterConst.FPTR_GT_SURCHARGE_VOID,
                FiscalPrinterConst.FPTR_GT_VAT,
                FiscalPrinterConst.FPTR_GT_VAT_CATEGORY
        };
        logPreCall("GetTotalizer", "" + vatID + ", " + optArgs);
        checkEnabled();
        Device.check(data == null, JposConst.JPOS_E_ILLEGAL, "Unexpected null pointer argument");
        Device.check(data.length != 1, JposConst.JPOS_E_ILLEGAL, "Bad dimension of argument pointer");
        Device.checkMember(optArgs, allowed, JposConst.JPOS_E_ILLEGAL, "Totalizer invalid: " + optArgs);
        FiscalPrinterInterface.getTotalizer(vatID, optArgs, data);
        logCall("GetTotalizer", "" + vatID + ", " + optArgs + ", " + data[0]);
    }

    @Override
    public void getVatEntry(int vatID, int optArgs, int[] vatRate) throws JposException {
        logPreCall("GetVatEntry", "" + vatID + ", " + optArgs);
        checkEnabled();
        Device.check(!Data.CapHasVatTable, JposConst.JPOS_E_ILLEGAL, "No VAT table");
        Device.check(vatRate == null, JposConst.JPOS_E_ILLEGAL, "Unexpected null pointer argument");
        Device.check(vatRate.length != 1, JposConst.JPOS_E_ILLEGAL, "Bad dimension of argument pointer");
        FiscalPrinterInterface.getVatEntry(vatID, optArgs, vatRate);
        logCall("GetVatEntry", "" + vatID + ", " + optArgs + ", " + vatRate[0]);
    }

    @Override
    public void printDuplicateReceipt() throws JposException {
        logPreCall("PrintDuplicateReceipt");
        checkEnabled();
        Device.check(!Data.CapDuplicateReceipt, JposConst.JPOS_E_ILLEGAL, "Duplicate receipt not supported");
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_MONITOR, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Device not in monitor state");
        Device.check(Data.State == JposConst.JPOS_S_BUSY, JposConst.JPOS_E_BUSY, "Output in progress");
        FiscalPrinterInterface.printDuplicateReceipt();
        logCall("PrintDuplicateReceipt");
    }

    @Override
    public void printPeriodicTotalsReport(String date1, String date2) throws JposException {
        Device.check(date1 == null || date2 == null, JposConst.JPOS_E_ILLEGAL, "Starting date and ending date must not be null");
        logPreCall("PrintPeriodicTotalsReport", date1 + ", " + date2);
        checkEnabled();
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_MONITOR, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Device not in monitor state");
        SimpleDateFormat format = new SimpleDateFormat("ddMMyyyyHHmm");
        format.setLenient(false);
        Date start = format.parse(date1, new ParsePosition(0));
        Date end = format.parse(date2, new ParsePosition(0));
        Device.check(start == null || date1.length() != format.toPattern().length(), JposConst.JPOS_E_ILLEGAL, "Starting date invalid: " + date1);
        Device.check(end == null || date2.length() != format.toPattern().length(), JposConst.JPOS_E_ILLEGAL, "Ending date invalid: " + date2);
        Device.check(start.compareTo(end) > 0, JposConst.JPOS_E_ILLEGAL, "Starting date must not be after ending date");
        FiscalPrinterInterface.printPeriodicTotalsReport(date1, date2);
        logCall("PrintPeriodicTotalsReport");
    }

    @Override
    public void printPowerLossReport() throws JposException {
        logPreCall("PrintPowerLossReport");
        checkEnabled();
        Device.check(!Data.CapPowerLossReport, JposConst.JPOS_E_ILLEGAL, "Duplicate receipt not supported");
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_MONITOR, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Device not in monitor state");
        FiscalPrinterInterface.printPowerLossReport();
        logCall("PrintPowerLossReport");
    }

    @Override
    public void printRecVoidItem(String description, long price, int quantity, int adjustmentType, long adjustment, int vatInfo) throws JposException {
        Device.check(!Data.AllowDeprecatedMethods, JposConst.JPOS_E_DEPRECATED, "Deprecated method, use PrintRecItemVoid and PrintRecItemAdjustmentVoid instead.");
        long[] allowedType = new long[]{
                FiscalPrinterConst.FPTR_RT_SALES,
                FiscalPrinterConst.FPTR_RT_SERVICE,
                FiscalPrinterConst.FPTR_RT_SIMPLE_INVOICE,
                FiscalPrinterConst.FPTR_RT_REFUND
        };
        long[] allowedamount = new long[]{
                FiscalPrinterConst.FPTR_AT_AMOUNT_DISCOUNT,
                FiscalPrinterConst.FPTR_AT_AMOUNT_SURCHARGE,
                FiscalPrinterConst.FPTR_AT_COUPON_AMOUNT_DISCOUNT
        };
        long[] allowedpercent = new long[]{
                FiscalPrinterConst.FPTR_AT_PERCENTAGE_DISCOUNT,
                FiscalPrinterConst.FPTR_AT_PERCENTAGE_SURCHARGE,
                FiscalPrinterConst.FPTR_AT_COUPON_PERCENTAGE_DISCOUNT
        };
        long[] allowedpositive = new long[]{
                FiscalPrinterConst.FPTR_AT_AMOUNT_SURCHARGE,
                FiscalPrinterConst.FPTR_AT_PERCENTAGE_SURCHARGE
        };
        Device.check(description == null, JposConst.JPOS_E_ILLEGAL, "Description must not be null");
        logPreCall("PrintRecVoidItem", description + ", " + price + ", " + quantity + ", " + adjustmentType + ", " + adjustment + ", " + vatInfo);
        checkEnabled();
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        Device.checkMember(Data.FiscalReceiptType, allowedType, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        Device.check(!Device.member(adjustmentType, allowedamount) && !Device.member(adjustmentType, allowedpercent), JposConst.JPOS_E_ILLEGAL, "Invalid adjustment type: " + adjustmentType);
        Device.check(!Data.CapAmountAdjustment && Device.member(adjustmentType, allowedamount), JposConst.JPOS_E_ILLEGAL, "Invalid adjustment type: " + adjustmentType);
        Device.check(!Data.CapPercentAdjustment && Device.member(adjustmentType, allowedpercent), JposConst.JPOS_E_ILLEGAL, "Invalid adjustment type: " + adjustmentType);
        Device.check(!Data.CapPositiveAdjustment && !Device.member(adjustmentType, allowedpositive), JposConst.JPOS_E_ILLEGAL, "Invalid adjustment type: " + adjustmentType);
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(description, "description");
        Device.checkext(price < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_AMOUNT, "price must be >= 0");
        Device.checkext(quantity < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_QUANTITY, "quantity must be >= 0");
        Device.checkext(adjustment < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_PRICE, "adjustment must be >= 0");
        callIt(FiscalPrinterInterface.printRecVoidItem(description, price, quantity, adjustmentType, adjustment, vatInfo), "PrintRecVoidItem");
    }

    @Override
    public void printReport(int reportType, String startNum, String endNum) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_RT_ORDINAL,
                FiscalPrinterConst.FPTR_RT_DATE,
                FiscalPrinterConst.FPTR_RT_EOD_ORDINAL
        };
        Device.check(startNum == null || endNum == null, JposConst.JPOS_E_ILLEGAL, "Starting and final record must not be null");
        logPreCall("PrintReport", "" +reportType + ", " + startNum + ", " + endNum);
        checkEnabled();
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_MONITOR, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Device not in monitor state");
        Device.checkMember(reportType, allowed, JposConst.JPOS_E_ILLEGAL, "Invalid report type: " + reportType);
        if (reportType == FiscalPrinterConst.FPTR_RT_DATE) {
            SimpleDateFormat format = new SimpleDateFormat("ddMMyyyyHHmm");
            format.setLenient(false);
            Date start = format.parse(startNum, new ParsePosition(0));
            Device.check(start == null || startNum.length() != format.toPattern().length(), JposConst.JPOS_E_ILLEGAL, "Starting date invalid: " + startNum);
            Date end = format.parse(endNum, new ParsePosition(0));
            Device.check(end == null || endNum.length() != format.toPattern().length(), JposConst.JPOS_E_ILLEGAL, "Ending date invalid: " + endNum);
            Device.check(start.compareTo(end) > 0, JposConst.JPOS_E_ILLEGAL, "Starting date must not be after ending date");
        }
        else {
            try {
                int start = Integer.parseInt(startNum);
                int end = Integer.parseInt(endNum);
                Device.check(start > end && end != 0, JposConst.JPOS_E_ILLEGAL, "Starting record must not be greater than final record");
            } catch (NumberFormatException e) {
                Device.check(true, JposConst.JPOS_E_ILLEGAL, "Starting and final record must be specified as numerical values");
            }
        }
        FiscalPrinterInterface.printReport(reportType, startNum, endNum);
        logCall("PrintReport");
    }

    @Override
    public void printXReport() throws JposException {
        logPreCall("PrintXReport");
        checkEnabled();
        Device.check(!Data.CapXReport, JposConst.JPOS_E_ILLEGAL, "X report not supported");
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_MONITOR, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Device not in monitor state");
        FiscalPrinterInterface.printXReport();
        logCall("PrintXReport");
    }

    @Override
    public void printZReport() throws JposException {
        logPreCall("PrintZReport");
        checkEnabled();
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_MONITOR, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Device not in monitor state");
        FiscalPrinterInterface.printZReport();
        logCall("PrintZReport");
    }

    @Override
    public void resetPrinter() throws JposException {
        logPreCall("ResetPrinter");
        checkEnabled();
        FiscalPrinterInterface.resetPrinter();
        logCall("ResetPrinter");
    }

    @Override
    public void setCurrency(int newCurrency) throws JposException {
        logPreCall("SetCurrency", "" + newCurrency);
        checkEnabled();
        Device.check(!Data.CapSetCurrency, JposConst.JPOS_E_ILLEGAL, "Changing currency not supported");
        Device.check(Data.DayOpened, JposConst.JPOS_E_ILLEGAL, "Fiscal day open");
        Device.checkext(Data.ActualCurrency == FiscalPrinterConst.FPTR_AC_EUR, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Currency just changed to EUR");
        Device.check(newCurrency != FiscalPrinterConst.FPTR_SC_EURO, JposConst.JPOS_E_ILLEGAL, "New currency not supported: " + newCurrency);
        FiscalPrinterInterface.setCurrency(newCurrency);
        logCall("SetCurrency");
    }

    @Override
    public void setDate(String date) throws JposException {
        Device.check(date == null, JposConst.JPOS_E_ILLEGAL, "Date must not be null");
        logPreCall("SetDate", date);
        checkEnabled();
        Device.check(Data.DayOpened, JposConst.JPOS_E_ILLEGAL, "Day open");
        SimpleDateFormat format = new SimpleDateFormat("ddMMyyyyHHmm");
        format.setLenient(false);
        Date start = format.parse(date, new ParsePosition(0));
        Device.checkext(start == null || date.length() != format.toPattern().length(), FiscalPrinterConst.JPOS_EFPTR_BAD_DATE, "Date invalid: " + date);
        FiscalPrinterInterface.setDate(date);
        logCall("SetDate");
    }

    @Override
    public void setHeaderLine(int lineNumber, String text, boolean doubleWidth) throws JposException {
        Device.check(text == null, JposConst.JPOS_E_ILLEGAL, "Text must not be null");
        logPreCall("SetHeaderLine", lineNumber + ", " + text + ", " + doubleWidth);
        checkEnabled();
        Device.check(!Data.CapSetHeader, JposConst.JPOS_E_ILLEGAL, "Setting header line not supported");
        Device.check(Data.DayOpened, JposConst.JPOS_E_ILLEGAL, "Day open");
        Device.checkRange(lineNumber, 1, Data.NumHeaderLines, JposConst.JPOS_E_ILLEGAL, "Line number out of range: " + lineNumber);
        Device.checkext(Data.CapReservedWord && text.lastIndexOf(Data.ReservedWord) >= 0, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Line contains reserved word");
        FiscalPrinterInterface.setHeaderLine(lineNumber, text, doubleWidth);
        logCall("SetHeaderLine");
    }

    @Override
    public void setPOSID(String POSID, String cashierID) throws JposException {
        Device.check(POSID == null || cashierID == null, JposConst.JPOS_E_ILLEGAL, "POSID and cashierID must not be null");
        logPreCall("SetPOSID", POSID + ", " + cashierID);
        checkEnabled();
        Device.check(!Data.CapSetPOSID, JposConst.JPOS_E_ILLEGAL, "Setting pos ID not supported");
        Device.check(Data.DayOpened, JposConst.JPOS_E_ILLEGAL, "Day open");
        checkReserved(POSID, "POSID");
        checkReserved(cashierID, "cashierID");
        FiscalPrinterInterface.setPOSID(POSID, cashierID);
        logCall("SetPOSID");
    }

    @Override
    public void setStoreFiscalID(String ID) throws JposException {
        Device.check(ID == null, JposConst.JPOS_E_ILLEGAL, "ID must not be null");
        logPreCall("SetStoreFiscalID", ID);
        checkEnabled();
        Device.check(!Data.CapSetStoreFiscalID, JposConst.JPOS_E_ILLEGAL, "Setting store fiscal ID not supported");
        Device.check(Data.DayOpened, JposConst.JPOS_E_ILLEGAL, "Day open");
        FiscalPrinterInterface.setStoreFiscalID(ID);
        logCall("SetStoreFiscalID");
    }

    @Override
    public void setTrailerLine(int lineNumber, String text, boolean doubleWidth) throws JposException {
        Device.check(text == null, JposConst.JPOS_E_ILLEGAL, "Text must not be null");
        logPreCall("SetTrailerLine", lineNumber + ", " + text + ", " + doubleWidth);
        checkEnabled();
        Device.check(!Data.CapSetTrailer, JposConst.JPOS_E_ILLEGAL, "Setting trailer line not supported");
        Device.check(Data.DayOpened, JposConst.JPOS_E_ILLEGAL, "Day open");
        Device.checkRange(lineNumber, 1, Data.NumTrailerLines, JposConst.JPOS_E_ILLEGAL, "Line number out of range: " + lineNumber);
        Device.checkext(Data.CapReservedWord && text.lastIndexOf(Data.ReservedWord) >= 0, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Line contains reserved word");
        FiscalPrinterInterface.setTrailerLine(lineNumber, text, doubleWidth);
        logCall("SetTrailerLine");
    }

    @Override
    public void setVatTable() throws JposException {
        logPreCall("SetVatTable");
        checkEnabled();
        Device.check(!Data.CapHasVatTable, JposConst.JPOS_E_ILLEGAL, "VAT tables not supported");
        Device.check(!Data.CapSetVatTable, JposConst.JPOS_E_ILLEGAL, "Setting VAT table not supported");
        Device.check(Data.DayOpened, JposConst.JPOS_E_ILLEGAL, "Day open");
        FiscalPrinterInterface.setVatTable();
        logCall("SetVatTable");
    }

    @Override
    public void setVatValue(int vatID, String vatValue) throws JposException {
        Device.check(vatValue == null, JposConst.JPOS_E_ILLEGAL, "vatValue must not be null");
        logPreCall("SetVatValue", vatID + ", " + vatValue);
        checkEnabled();
        Device.check(!Data.CapHasVatTable, JposConst.JPOS_E_ILLEGAL, "VAT tables not supported");
        Device.check(!Data.CapSetVatTable, JposConst.JPOS_E_ILLEGAL, "Setting trailer line not supported");
        Device.check(Data.DayOpened, JposConst.JPOS_E_ILLEGAL, "Day open");
        long vat = stringToCurrency(vatValue, "vatValue");
        Device.check(vat > 999999, JposConst.JPOS_E_ILLEGAL, "VAT value too big: " + vatValue);
        FiscalPrinterInterface.setVatValue(vatID, vat < 100 ? vat * 10000 : vat);
        logCall("SetVatValue");
    }

    @Override
    public void verifyItem(String itemName, int vatID) throws JposException {
        Device.check(itemName == null, JposConst.JPOS_E_ILLEGAL, "itemName must not be null");
        logPreCall("VerifyItem", itemName + ", " + vatID);
        checkEnabled();
        Device.check(!Data.CapHasVatTable, JposConst.JPOS_E_ILLEGAL, "VAT tables not supported");
        Device.check(!Data.CapItemList, JposConst.JPOS_E_ILLEGAL, "Item list not supported");
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_ITEM_LIST, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in item list");
        Device.checkext(Data.CapReservedWord && itemName.lastIndexOf(Data.ReservedWord) >= 0, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Item name contains reserved word");
        FiscalPrinterInterface.verifyItem(itemName, vatID);
        logCall("VerifyItem");
    }

    /*
    Possibly asynchronous methods
     */

    @Override
    public void printFiscalDocumentLine(String documentLine) throws JposException {
        Device.check(documentLine == null, JposConst.JPOS_E_ILLEGAL, "DocumentLine must not be null");
        logPreCall("PrintFiscalDocumentLine", documentLine);
        checkEnabled();
        Device.check(!Data.CapSlpFiscalDocument, JposConst.JPOS_E_ILLEGAL, "Fiscal document printing not supported");
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_DOCUMENT, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in fiscal document state");
        ifSyncCheckBusyCoverPaper(FiscalPrinterConst.FPTR_S_SLIP);
        callIt(FiscalPrinterInterface.printFiscalDocumentLine(documentLine), "PrintFiscalDocumentLine");
    }

    @Override
    public void printFixedOutput(int documentType, int lineNumber, String data) throws JposException {
        Device.check(data == null, JposConst.JPOS_E_ILLEGAL, "Data must not be null");
        logPreCall("PrintFixedOutput", documentType + ", " + lineNumber + ", \"" + data + "\"");
        checkEnabled();
        Device.check(!Data.CapFixedOutput, JposConst.JPOS_E_ILLEGAL, "Fixed output printing not supported");
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_FIXED_OUTPUT, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in fixed output state");
        Device.check(!Props.AsyncMode && Props.State == JposConst.JPOS_S_BUSY, JposConst.JPOS_E_BUSY, "Device is busy");
        callIt(FiscalPrinterInterface.printFixedOutput(documentType, lineNumber, data), "PrintFixedOutput");
    }

    @Override
    public void printNormal(int station, String data) throws JposException {
        long[][] allowed = new long[][]{
                new long[]{     // possible print stations
                        FiscalPrinterConst.FPTR_S_JOURNAL,
                        FiscalPrinterConst.FPTR_S_RECEIPT,
                        FiscalPrinterConst.FPTR_S_SLIP
                },
                new long[]{     // corresponding presence capabilities
                        Data.CapJrnPresent ? 1 : 0,
                        Data.CapRecPresent ? 1 : 0,
                        Data.CapSlpPresent ? 1 : 0
                },
                new long[]{
                        -1,
                        FiscalPrinterConst.FPTR_RS_RECEIPT,
                        FiscalPrinterConst.FPTR_RS_SLIP
                }
        };
        long[] allowedFiscal = new long[]{
                FiscalPrinterConst.FPTR_RT_GENERIC,
                FiscalPrinterConst.FPTR_RT_CASH_IN,
                FiscalPrinterConst.FPTR_RT_CASH_OUT
        };
        Device.check(data == null, JposConst.JPOS_E_ILLEGAL, "Data must not be null");
        logPreCall("PrintNormal", station + ", \"" + data + "\"");
        checkEnabled();
        int stationindex;
        for (stationindex = 0; stationindex < allowed[0].length; stationindex++) {
            if (station == allowed[0][stationindex])
                break;
        }
        Device.check(stationindex == allowed[0].length, JposConst.JPOS_E_ILLEGAL, "Invalid station: " + station);
        if (Data.CapFiscalReceiptType && Device.member(Data.FiscalReceiptType, allowedFiscal) && Data.PrinterState == FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT) {
            Device.check(Data.FiscalReceiptStation != allowed[2][stationindex], JposConst.JPOS_E_ILLEGAL, "Station does not match receipt station: " + station);
        }
        else {
            Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_NONFISCAL, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in fixed output state");
            Device.check(allowed[1][stationindex] == 0, JposConst.JPOS_E_ILLEGAL, "Station does not exist: " + station);
        }
        ifSyncCheckBusyCoverPaper(station);
        callIt(FiscalPrinterInterface.printNormal(station, data), "PrintNormal");
    }

    @Override
    public void printRecCash(long amount) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_RT_CASH_IN, FiscalPrinterConst.FPTR_RT_CASH_OUT
        };
        logPreCall("PrintRecCash", "" + amount);
        checkEnabled();
        Device.check(!Data.CapFiscalReceiptType, JposConst.JPOS_E_ILLEGAL, "Cash in / out not supported");
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        Device.checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not in cash in / cash out receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        Device.check(amount <= 0, JposConst.JPOS_E_ILLEGAL, "Amount of cash in / out must be > 0");
        Device.check(truncUnusedDecimals(amount) != amount, JposConst.JPOS_E_ILLEGAL, "Amount contains fractions of smallest cash units");
        callIt(FiscalPrinterInterface.printRecCash(amount), "PrintRecCash");
    }

    @Override
    public void printRecItem(String description, long price, int quantity, int vatInfo, long unitPrice, String unitName) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_RT_SALES,
                FiscalPrinterConst.FPTR_RT_SERVICE,
                FiscalPrinterConst.FPTR_RT_SIMPLE_INVOICE,
                FiscalPrinterConst.FPTR_RT_REFUND
        };
        Device.check(description == null, JposConst.JPOS_E_ILLEGAL, "Description must not be null");
        if (unitName == null)
            unitName = "";
        logPreCall("PrintRecItem", description + ", " + price + ", " + quantity + ", " + vatInfo + ", " + unitPrice + ", " + unitName);
        checkEnabled();
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        Device.checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(description, "description");
        Device.checkext(price < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_AMOUNT, "price must be >= 0");
        Device.checkext(quantity < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_QUANTITY, "quantity must be >= 0");
        Device.checkext(unitPrice < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_PRICE, "unitPrice must be >= 0");
        checkReserved(unitName, "unitName");
        callIt(FiscalPrinterInterface.printRecItem(description, price, quantity, vatInfo, unitPrice, unitName), "PrintRecItem");
    }

    @Override
    public void printRecItemAdjustment(int adjustmentType, String description, long amount, int vatInfo) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_RT_SALES,
                FiscalPrinterConst.FPTR_RT_SERVICE,
                FiscalPrinterConst.FPTR_RT_SIMPLE_INVOICE,
                FiscalPrinterConst.FPTR_RT_REFUND
        };
        long[] allowedamount = new long[]{
                FiscalPrinterConst.FPTR_AT_AMOUNT_DISCOUNT,
                FiscalPrinterConst.FPTR_AT_AMOUNT_SURCHARGE,
                FiscalPrinterConst.FPTR_AT_COUPON_AMOUNT_DISCOUNT
        };
        long[] allowedpercent = new long[]{
                FiscalPrinterConst.FPTR_AT_PERCENTAGE_DISCOUNT,
                FiscalPrinterConst.FPTR_AT_PERCENTAGE_SURCHARGE,
                FiscalPrinterConst.FPTR_AT_COUPON_PERCENTAGE_DISCOUNT
        };
        long[] allowedpositive = new long[]{
                FiscalPrinterConst.FPTR_AT_AMOUNT_SURCHARGE,
                FiscalPrinterConst.FPTR_AT_PERCENTAGE_SURCHARGE
        };
        Device.check(description == null, JposConst.JPOS_E_ILLEGAL, "Description must not be null");
        logPreCall("PrintRecItemAdjustment", description + ", " + amount + ", " + vatInfo);
        checkEnabled();
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        Device.checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        Device.check(!Device.member(adjustmentType, allowedamount) && !Device.member(adjustmentType, allowedpercent), JposConst.JPOS_E_ILLEGAL, "Invalid adjustment type: " + adjustmentType);
        Device.check(!Data.CapAmountAdjustment && Device.member(adjustmentType, allowedamount), JposConst.JPOS_E_ILLEGAL, "Unsupported adjustment: " + adjustmentType);
        Device.check(!Data.CapPercentAdjustment && Device.member(adjustmentType, allowedpercent), JposConst.JPOS_E_ILLEGAL, "Unsupported adjustment: " + adjustmentType);
        Device.check(!Data.CapPositiveAdjustment && Device.member(adjustmentType, allowedpositive), JposConst.JPOS_E_ILLEGAL, "Unsupported adjustment: " + adjustmentType);
        checkReserved(description, "description");
        Device.checkext(amount <= 0, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_AMOUNT, "amount must be > 0");
        callIt(FiscalPrinterInterface.printRecItemAdjustment(adjustmentType, description, amount, vatInfo), "PrintRecItemAdjustment");
    }

    @Override
    public void printRecItemAdjustmentVoid(int adjustmentType, String description, long amount, int vatInfo) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_RT_SALES,
                FiscalPrinterConst.FPTR_RT_SERVICE,
                FiscalPrinterConst.FPTR_RT_SIMPLE_INVOICE,
                FiscalPrinterConst.FPTR_RT_REFUND
        };
        long[] allowedamount = new long[]{
                FiscalPrinterConst.FPTR_AT_AMOUNT_DISCOUNT,
                FiscalPrinterConst.FPTR_AT_AMOUNT_SURCHARGE,
                FiscalPrinterConst.FPTR_AT_COUPON_AMOUNT_DISCOUNT
        };
        long[] allowedpercent = new long[]{
                FiscalPrinterConst.FPTR_AT_PERCENTAGE_DISCOUNT,
                FiscalPrinterConst.FPTR_AT_PERCENTAGE_SURCHARGE,
                FiscalPrinterConst.FPTR_AT_COUPON_PERCENTAGE_DISCOUNT
        };
        long[] allowedpositive = new long[]{
                FiscalPrinterConst.FPTR_AT_AMOUNT_SURCHARGE,
                FiscalPrinterConst.FPTR_AT_PERCENTAGE_SURCHARGE
        };
        Device.check(description == null, JposConst.JPOS_E_ILLEGAL, "Description must not be null");
        logPreCall("PrintRecItemAdjustmentVoid", description + ", " + amount + ", " + vatInfo);
        checkEnabled();
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        Device.checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        Device.check(!Device.member(adjustmentType, allowedamount) && !Device.member(adjustmentType, allowedpercent), JposConst.JPOS_E_ILLEGAL, "Invalid adjustment type: " + adjustmentType);
        Device.check(!Data.CapAmountAdjustment && Device.member(adjustmentType, allowedamount), JposConst.JPOS_E_ILLEGAL, "Unsupported adjustment: " + adjustmentType);
        Device.check(!Data.CapPercentAdjustment && Device.member(adjustmentType, allowedpercent), JposConst.JPOS_E_ILLEGAL, "Unsupported adjustment: " + adjustmentType);
        Device.check(!Data.CapPositiveAdjustment && Device.member(adjustmentType, allowedpositive), JposConst.JPOS_E_ILLEGAL, "Unsupported adjustment: " + adjustmentType);
        checkReserved(description, "description");
        Device.checkext(amount <= 0, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_AMOUNT, "amount must be > 0");
        callIt(FiscalPrinterInterface.printRecItemAdjustmentVoid(adjustmentType, description, amount, vatInfo), "PrintRecItemAdjustmentVoid");
    }

    @Override
    public void printRecItemFuel(String description, long price, int quantity, int vatInfo, long unitPrice, String unitName, long specialTax, String specialTaxName) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_RT_SALES,
                FiscalPrinterConst.FPTR_RT_SERVICE,
                FiscalPrinterConst.FPTR_RT_SIMPLE_INVOICE,
                FiscalPrinterConst.FPTR_RT_REFUND
        };
        Device.check(description == null, JposConst.JPOS_E_ILLEGAL, "Description must not be null");
        if (unitName == null)
            unitName = "";
        if (specialTaxName == null)
            specialTaxName = "";
        logPreCall("PrintRecItemFuel", description + ", " + price + ", " + quantity + ", " + vatInfo + ", " + unitPrice + ", " + unitName + ", " + specialTax + ", " + specialTaxName);
        checkEnabled();
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        Device.checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(description, "description");
        Device.checkext(price < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_AMOUNT, "price must be >= 0");
        Device.checkext(quantity < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_QUANTITY, "quantity must be >= 0");
        Device.checkext(unitPrice < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_PRICE, "unitPrice must be >= 0");
        Device.check(specialTax < 0, JposConst.JPOS_E_ILLEGAL, "specialTax must be >= 0");
        checkReserved(unitName, "unitName");
        checkReserved(specialTaxName, "specialTaxName");
        callIt(FiscalPrinterInterface.printRecItemFuel(description, price, quantity, vatInfo, unitPrice, unitName, specialTax, specialTaxName), "PrintRecItemFuel");
    }

    @Override
    public void printRecItemFuelVoid(String description, long price, int vatInfo, long specialTax) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_RT_SALES,
                FiscalPrinterConst.FPTR_RT_SERVICE,
                FiscalPrinterConst.FPTR_RT_SIMPLE_INVOICE,
                FiscalPrinterConst.FPTR_RT_REFUND
        };
        Device.check(description == null, JposConst.JPOS_E_ILLEGAL, "Description must not be null");
        logPreCall("PrintRecItemFuelVoid", description + ", " + price + ", " + vatInfo + ", " + specialTax);
        checkEnabled();
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        Device.checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(description, "description");
        Device.checkext(price < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_AMOUNT, "price must be >= 0");
        Device.check(specialTax < 0, JposConst.JPOS_E_ILLEGAL, "specialTax must be >= 0");
        callIt(FiscalPrinterInterface.printRecItemFuelVoid(description, price, vatInfo, specialTax), "PrintRecItemFuelVoid");
    }

    @Override
    public void printRecItemRefund(String description, long amount, int quantity, int vatInfo, long unitAmount, String unitName) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_RT_SALES,
                FiscalPrinterConst.FPTR_RT_SERVICE,
                FiscalPrinterConst.FPTR_RT_REFUND
        };
        Device.check(description == null, JposConst.JPOS_E_ILLEGAL, "Description must not be null");
        if (unitName == null)
            unitName = "";
        logPreCall("PrintRecItemRefund", description + ", " + amount + ", " + quantity + ", " + vatInfo + ", " + unitAmount + ", " + unitName);
        checkEnabled();
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        Device.checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(description, "description");
        Device.checkext(amount < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_AMOUNT, "price must be >= 0");
        Device.checkext(quantity < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_QUANTITY, "quantity must be >= 0");
        Device.checkext(unitAmount < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_PRICE, "unitPrice must be >= 0");
        checkReserved(unitName, "unitName");
        callIt(FiscalPrinterInterface.printRecItemRefund(description, amount, quantity, vatInfo, unitAmount, unitName), "PrintRecItemRefund");
    }

    @Override
    public void printRecItemRefundVoid(String description, long amount, int quantity, int vatInfo, long unitAmount, String unitName) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_RT_SALES,
                FiscalPrinterConst.FPTR_RT_SERVICE,
                FiscalPrinterConst.FPTR_RT_REFUND
        };
        Device.check(description == null, JposConst.JPOS_E_ILLEGAL, "Description must not be null");
        if (unitName == null)
            unitName = "";
        logPreCall("PrintRecItemRefundVoid", description + ", " + amount + ", " + quantity + ", " + vatInfo + ", " + unitAmount + ", " + unitName);
        checkEnabled();
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        Device.checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(description, "description");
        Device.checkext(amount < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_AMOUNT, "price must be >= 0");
        Device.checkext(quantity < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_QUANTITY, "quantity must be >= 0");
        Device.checkext(unitAmount < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_PRICE, "unitPrice must be >= 0");
        checkReserved(unitName, "unitName");
        callIt(FiscalPrinterInterface.printRecItemRefundVoid(description, amount, quantity, vatInfo, unitAmount, unitName), "PrintRecItemRefundVoid");
    }

    @Override
    public void printRecItemVoid(String description, long price, int quantity, int vatInfo, long unitPrice, String unitName) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_RT_SALES,
                FiscalPrinterConst.FPTR_RT_SERVICE,
                FiscalPrinterConst.FPTR_RT_SIMPLE_INVOICE,
                FiscalPrinterConst.FPTR_RT_REFUND
        };
        Device.check(description == null, JposConst.JPOS_E_ILLEGAL, "Description and unitName must not be null");
        if (unitName == null)
            unitName = "";
        logPreCall("PrintRecItemVoid", description + ", " + price + ", " + quantity + ", " + vatInfo + ", " + unitPrice + ", " + unitName);
        checkEnabled();
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        Device.checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(description, "description");
        Device.checkext(price < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_AMOUNT, "price must be >= 0");
        Device.checkext(quantity < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_QUANTITY, "quantity must be >= 0");
        Device.checkext(unitPrice < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_PRICE, "unitPrice must be >= 0");
        checkReserved(unitName, "unitName");
        callIt(FiscalPrinterInterface.printRecItemVoid(description, price, quantity, vatInfo, unitPrice, unitName), "PrintRecItemVoid");
    }

    @Override
    public void printRecMessage(String message) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_RT_SALES,
                FiscalPrinterConst.FPTR_RT_SERVICE,
                FiscalPrinterConst.FPTR_RT_SIMPLE_INVOICE,
                FiscalPrinterConst.FPTR_RT_REFUND
        };
        long[] allowedState = new long[]{
                FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT,
                FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT_TOTAL,
                FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT_ENDING
        };
        Device.check(message == null, JposConst.JPOS_E_ILLEGAL, "Message must not be null");
        logPreCall("PrintRecMessage", message);
        checkEnabled();
        Device.check(!Data.CapAdditionalLines, JposConst.JPOS_E_ILLEGAL, "Additional lines not supported");
        Device.checkext(!Device.member(Data.PrinterState, allowedState), FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        Device.checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(message, "message");
        if (!Data.AsyncMode) {
            Device.checkext(message.length() > Data.MessageLength, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Message too long");
        }
        callIt(FiscalPrinterInterface.printRecMessage(message), "PrintRecMessage");
    }

    @Override
    public void printRecNotPaid(String description, long amount) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_RT_SALES,
                FiscalPrinterConst.FPTR_RT_SERVICE,
                FiscalPrinterConst.FPTR_RT_SIMPLE_INVOICE,
                FiscalPrinterConst.FPTR_RT_REFUND
        };
        long[] allowedState = new long[]{
                FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT, FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT_TOTAL
        };
        Device.check(description == null, JposConst.JPOS_E_ILLEGAL, "Description must not be null");
        logPreCall("PrintRecNotPaid", description + ", " + amount);
        checkEnabled();
        Device.check(!Data.CapReceiptNotPaid, JposConst.JPOS_E_ILLEGAL, "Receipt not paid not supported");
        Device.checkext(!Device.member(Data.PrinterState, allowedState), FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        Device.checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(description, "description");
        Device.check(amount <= 0, JposConst.JPOS_E_ILLEGAL, "Amount <= 0");
        callIt(FiscalPrinterInterface.printRecNotPaid(description, amount), "PrintRecNotPaid");
    }

    @Override
    public void printRecPackageAdjustment(int adjustmentType, String description, String vatAdjustment) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_RT_SALES,
                FiscalPrinterConst.FPTR_RT_SERVICE,
                FiscalPrinterConst.FPTR_RT_SIMPLE_INVOICE,
                FiscalPrinterConst.FPTR_RT_REFUND
        };
        long[] allowedType = new long[]{
                Device.FPTR_AT_DISCOUNT,
                Device.FPTR_AT_SURCHARGE
        };
        Device.check(vatAdjustment == null || description == null, JposConst.JPOS_E_ILLEGAL, "description and vatAdjustment must not be null");
        logPreCall("PrintRecPackageAdjustment", adjustmentType + ", " + description + ", " + vatAdjustment);
        checkEnabled();
        Device.check(!Data.CapPackageAdjustment, JposConst.JPOS_E_ILLEGAL, "Package Adjustment not supported");
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        Device.checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        Device.checkMember(adjustmentType, allowedType, JposConst.JPOS_E_ILLEGAL, "Adjustment not supported: " + adjustmentType);
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(description, "description");
        String[] adjustments = vatAdjustment.split(";");
        Device.check(adjustments.length > Data.NumVatRates, JposConst.JPOS_E_ILLEGAL, "Bad number of pairs of VAT ID and amount");
        for (String adjustment : adjustments) {
            String[] value = adjustment.split(",");
            Device.check(value.length != 2, JposConst.JPOS_E_ILLEGAL, "Mal-formatted vatAdjustment parameter");
            try {
                Integer.parseInt(value[0]);
                stringToCurrency(value[1], "Amount");
            } catch (NumberFormatException e) {
                throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid vatID or invalid amount: " + e.getMessage(), e);
            }
        }
        checkReserved(description, "description");
        callIt(FiscalPrinterInterface.printRecPackageAdjustment(adjustmentType, description, vatAdjustment), "PrintRecPackageAdjustment");
    }

    @Override
    public void printRecPackageAdjustVoid(int adjustmentType, String vatAdjustment) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_RT_SALES,
                FiscalPrinterConst.FPTR_RT_SERVICE,
                FiscalPrinterConst.FPTR_RT_SIMPLE_INVOICE,
                FiscalPrinterConst.FPTR_RT_REFUND
        };
        long[] allowedType = new long[]{
                Device.FPTR_AT_DISCOUNT,
                Device.FPTR_AT_SURCHARGE
        };
        Device.check(vatAdjustment == null, JposConst.JPOS_E_ILLEGAL, "vatAdjustment must not be null");
        logPreCall("PrintRecPackageAdjustVoid", adjustmentType + ", " + vatAdjustment);
        checkEnabled();
        Device.check(!Data.CapPackageAdjustment, JposConst.JPOS_E_ILLEGAL, "Package Adjustment not supported");
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        Device.checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        Device.checkMember(adjustmentType, allowedType, JposConst.JPOS_E_ILLEGAL, "Adjustment not supported: " + adjustmentType);
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        String[] adjustments = vatAdjustment.split(";");
        Device.check(adjustments.length > Data.NumVatRates, JposConst.JPOS_E_ILLEGAL, "Bad number of pairs of VAT ID and amount");
        for (String adjustment : adjustments) {
            String[] value = adjustment.split(",");
            Device.check(value.length != 2, JposConst.JPOS_E_ILLEGAL, "Mal-formatted vatAdjustment parameter");
            try {
                Integer.parseInt(value[0]);
                stringToCurrency(value[1], "Amount");
            } catch (NumberFormatException e) {
                throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid vatID or invalid amount: " + e.getMessage(), e);
            }
        }
        callIt(FiscalPrinterInterface.printRecPackageAdjustVoid(adjustmentType, vatAdjustment), "PrintRecPackageAdjustVoid");
    }

    @Override
    public void printRecRefund(String description, long amount, int vatInfo) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_RT_SALES,
                FiscalPrinterConst.FPTR_RT_SERVICE,
                FiscalPrinterConst.FPTR_RT_REFUND
        };
        Device.check(description == null, JposConst.JPOS_E_ILLEGAL, "Description must not be null");
        logPreCall("PrintRecRefund", description + ", " + amount + ", " + vatInfo);
        checkEnabled();
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        Device.checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(description, "description");
        Device.checkext(amount < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_AMOUNT, "price must be >= 0");
        callIt(FiscalPrinterInterface.printRecRefund(description, amount, vatInfo), "PrintRecRefund");
    }

    @Override
    public void printRecRefundVoid(String description, long amount, int vatInfo) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_RT_SALES,
                FiscalPrinterConst.FPTR_RT_SERVICE,
                FiscalPrinterConst.FPTR_RT_REFUND
        };
        Device.check(description == null, JposConst.JPOS_E_ILLEGAL, "Description must not be null");
        logPreCall("PrintRecRefundVoid", description + ", " + amount + ", " + vatInfo);
        checkEnabled();
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        Device.checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(description, "description");
        Device.checkext(amount < 0, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_AMOUNT, "price must be >= 0");
        callIt(FiscalPrinterInterface.printRecRefundVoid(description, amount, vatInfo), "PrintRecRefundVoid");
    }

    @Override
    public void printRecSubtotal(long amount) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_RT_SALES,
                FiscalPrinterConst.FPTR_RT_SERVICE,
                FiscalPrinterConst.FPTR_RT_SIMPLE_INVOICE,
                FiscalPrinterConst.FPTR_RT_REFUND
        };
        logPreCall("PrintRecSubtotal", "" + amount);
        checkEnabled();
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        Device.checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        Device.check(amount < 0, JposConst.JPOS_E_ILLEGAL, "amount < 0");
        callIt(FiscalPrinterInterface.printRecSubtotal(amount), "PrintRecSubtotal");
    }

    @Override
    public void printRecSubtotalAdjustment(int adjustmentType, String description, long amount) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_RT_SALES,
                FiscalPrinterConst.FPTR_RT_SERVICE,
                FiscalPrinterConst.FPTR_RT_SIMPLE_INVOICE,
                FiscalPrinterConst.FPTR_RT_REFUND
        };
        long[] allowedamount = new long[]{
                FiscalPrinterConst.FPTR_AT_AMOUNT_DISCOUNT,
                FiscalPrinterConst.FPTR_AT_AMOUNT_SURCHARGE,
                FiscalPrinterConst.FPTR_AT_COUPON_AMOUNT_DISCOUNT
        };
        long[] allowedpercent = new long[]{
                FiscalPrinterConst.FPTR_AT_PERCENTAGE_DISCOUNT,
                FiscalPrinterConst.FPTR_AT_PERCENTAGE_SURCHARGE,
                FiscalPrinterConst.FPTR_AT_COUPON_PERCENTAGE_DISCOUNT
        };
        long[] allowedpositive = new long[]{
                FiscalPrinterConst.FPTR_AT_AMOUNT_SURCHARGE,
                FiscalPrinterConst.FPTR_AT_PERCENTAGE_SURCHARGE
        };
        Device.check(description == null, JposConst.JPOS_E_ILLEGAL, "Description must not be null");
        logPreCall("PrintRecSubtotalAdjustment", description + ", " + amount);
        checkEnabled();
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        Device.checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        Device.check(!Device.member(adjustmentType, allowedamount) && !Device.member(adjustmentType, allowedpercent), JposConst.JPOS_E_ILLEGAL, "Invalid adjustment type: " + adjustmentType);
        Device.check(!Data.CapSubAmountAdjustment && Device.member(adjustmentType, allowedamount), JposConst.JPOS_E_ILLEGAL, "Unsupported adjustment: " + adjustmentType);
        Device.check(!Data.CapSubPercentAdjustment && Device.member(adjustmentType, allowedpercent), JposConst.JPOS_E_ILLEGAL, "Unsupported adjustment: " + adjustmentType);
        Device.check(!Data.CapPositiveSubtotalAdjustment && Device.member(adjustmentType, allowedpositive), JposConst.JPOS_E_ILLEGAL, "Unsupported adjustment: " + adjustmentType);
        checkReserved(description, "description");
        Device.checkext(amount <= 0, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_AMOUNT, "amount must be > 0");
        callIt(FiscalPrinterInterface.printRecSubtotalAdjustment(adjustmentType, description, amount), "PrintRecSubtotalAdjustment");
    }

    @Override
    public void printRecSubtotalAdjustVoid(int adjustmentType, long amount) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_RT_SALES,
                FiscalPrinterConst.FPTR_RT_SERVICE,
                FiscalPrinterConst.FPTR_RT_SIMPLE_INVOICE,
                FiscalPrinterConst.FPTR_RT_REFUND
        };
        long[] allowedamount = new long[]{
                FiscalPrinterConst.FPTR_AT_AMOUNT_DISCOUNT,
                FiscalPrinterConst.FPTR_AT_AMOUNT_SURCHARGE
        };
        long[] allowedpercent = new long[]{
                FiscalPrinterConst.FPTR_AT_PERCENTAGE_DISCOUNT,
                FiscalPrinterConst.FPTR_AT_PERCENTAGE_SURCHARGE
        };
        long[] allowedpositive = new long[]{
                FiscalPrinterConst.FPTR_AT_AMOUNT_SURCHARGE,
                FiscalPrinterConst.FPTR_AT_PERCENTAGE_SURCHARGE
        };
        logPreCall("PrintRecSubtotalAdjustVoid", "" + amount);
        checkEnabled();
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        Device.checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        Device.check(!Device.member(adjustmentType, allowedamount) && !Device.member(adjustmentType, allowedpercent), JposConst.JPOS_E_ILLEGAL, "Invalid adjustment type: " + adjustmentType);
        Device.check(!Data.CapSubAmountAdjustment && Device.member(adjustmentType, allowedamount), JposConst.JPOS_E_ILLEGAL, "Unsupported adjustment: " + adjustmentType);
        Device.check(!Data.CapSubPercentAdjustment && Device.member(adjustmentType, allowedpercent), JposConst.JPOS_E_ILLEGAL, "Unsupported adjustment: " + adjustmentType);
        Device.check(!Data.CapPositiveSubtotalAdjustment && Device.member(adjustmentType, allowedpositive), JposConst.JPOS_E_ILLEGAL, "Unsupported adjustment: " + adjustmentType);
        Device.checkext(amount <= 0, FiscalPrinterConst.JPOS_EFPTR_BAD_ITEM_AMOUNT, "amount must be > 0");
        callIt(FiscalPrinterInterface.printRecSubtotalAdjustVoid(adjustmentType, amount), "PrintRecSubtotalAdjustVoid");
    }

    @Override
    public void printRecTaxID(String taxId) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_RT_SALES,
                FiscalPrinterConst.FPTR_RT_SERVICE,
                FiscalPrinterConst.FPTR_RT_SIMPLE_INVOICE,
                FiscalPrinterConst.FPTR_RT_REFUND
        };
        Device.check(taxId == null, JposConst.JPOS_E_ILLEGAL, "Tax-ID must not be null");
        logPreCall("PrintRecTaxID", taxId);
        checkEnabled();
        Device.checkext(Data.PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT_ENDING, FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt ending state");
        Device.checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(taxId, "taxId");
        callIt(FiscalPrinterInterface.printRecTaxID(taxId), "PrintRecTaxID");
    }

    @Override
    public void printRecTotal(long total, long payment, String description) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_RT_SALES,
                FiscalPrinterConst.FPTR_RT_SERVICE,
                FiscalPrinterConst.FPTR_RT_SIMPLE_INVOICE,
                FiscalPrinterConst.FPTR_RT_REFUND
        };
        long[] allowedstate = new long[]{
                FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT,
                FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT_TOTAL
        };
        Device.check(description == null, JposConst.JPOS_E_ILLEGAL, "Tax-ID must not be null");
        logPreCall("PrintRecTotal", description);
        checkEnabled();
        Device.checkext(!Device.member(Data.PrinterState, allowedstate), FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt or fiscal receipt total ending state");
        Device.checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        Device.check(total < 0, JposConst.JPOS_E_ILLEGAL, "Total < 0");
        Device.check(payment < 0, JposConst.JPOS_E_ILLEGAL, "Payment < 0");
        if (Data.CapPredefinedPaymentLines) {
            String[] payments = Data.PredefinedPaymentLines.split(",");
            boolean found = false;
            for (String placeholder : payments) {
                if (placeholder.equals(description)) {
                    found = true;
                    break;
                }
            }
            Device.check(!found, JposConst.JPOS_E_ILLEGAL, "Invalid payment ID: " + description);
        }
        else
            checkReserved(description, "description");
        callIt(FiscalPrinterInterface.printRecTotal(total, payment, description), "PrintRecTotal");
    }

    @Override
    public void printRecVoid(String description) throws JposException {
        long[] allowed = new long[]{
                FiscalPrinterConst.FPTR_RT_SALES,
                FiscalPrinterConst.FPTR_RT_SERVICE,
                FiscalPrinterConst.FPTR_RT_SIMPLE_INVOICE,
                FiscalPrinterConst.FPTR_RT_REFUND
        };
        long[] allowedstate = new long[]{
                FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT,
                FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT_TOTAL
        };
        Device.check(description == null, JposConst.JPOS_E_ILLEGAL, "Tax-ID must not be null");
        logPreCall("PrintRecVoid", description);
        checkEnabled();
        Device.checkext(!Device.member(Data.PrinterState, allowedstate), FiscalPrinterConst.JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt or fiscal receipt total state");
        Device.checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(description, "description");
        callIt(FiscalPrinterInterface.printRecVoid(description), "PrintRecVoid");
    }
}
