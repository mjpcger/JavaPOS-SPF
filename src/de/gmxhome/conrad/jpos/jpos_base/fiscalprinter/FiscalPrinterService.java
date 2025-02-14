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
import jpos.services.*;

import java.math.*;
import java.text.*;
import java.util.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.FiscalPrinterConst.*;
import static jpos.JposConst.*;

/**
 * FiscalPrinter service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 * <br>This service supports the following properties in jpos.xml in addition to the properties listed in JposBaseDevice:
 * <ul>
 *     <li>UseEnumeratedValues: If true, string representations contain a decimal point, where trailing zeroes up to and inclusive
 *     the decimal point can be stripped. Examples for a currency value of 123.4: "123.4", "123.4000". If false, string
 *     representations contain the 64-bit integer value used internally to store a currency value. This value does not
 *     contain a decimal point. Example for a currency value of 123.4: "1234000", for 0.19: "1900". The default is true.
 *     </li>
 * </ul>
 * This property will only be used if the service factory passes the jpos entries to the addDevice method.
 * If the deprecated addDevice method is used, the service will not consider this property.
 */
public class FiscalPrinterService extends JposBase implements FiscalPrinterService116 {
    /**
     * Instance of a class implementing the FiscalPrinterInterface for fiscal printer specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public FiscalPrinterInterface FiscalPrinterInterface;

    private final FiscalPrinterProperties Data;

    private void checkBusySync() throws JposException {
        check(Data.State == JPOS_S_BUSY && !Data.AsyncMode, JPOS_E_BUSY, "Output in progress");
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
        return Data.FiscalReceiptStation == FPTR_RS_RECEIPT ?
                FPTR_S_JOURNAL_RECEIPT :
                FPTR_S_SLIP | FPTR_S_JOURNAL;
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
        checkext(Data.CapReservedWord && !Data.ReservedWord.equals("") && index >= 0, JPOS_EFPTR_BAD_ITEM_DESCRIPTION, valuename + " contains reserved word");
    }

    /**
     * Convert a string containing a currency value into a currency value (a long with 4 implicit decimals).
     * Class BigDecimal will be used for conversion
     * @param value Value to be converted.
     * @param name  Name of the value, used if value is invalid.
     * @return Currency value if value could be converted.
     * @throws JposException with error code E_ILLEGAL if value cannot be converted or if value is out of range.
     */
    public long stringToCurrency(String value, String name) throws JposException {
        return stringToCurrency(value, name, false).longValue();
    }

    /**
     * Convert a string containing a currency or percentage value into a Number value (a long or int with 4 implicit decimals).
     * Class BigDecimal will be used for conversion
     * @param value Value to be converted.
     * @param name  Name of the value, used if value is invalid.
     * @param percent If true, return value is percent value, otherwise fixed amount.
     * @return Number value if value could be converted.
     * @throws JposException with error code E_ILLEGAL if value cannot be converted or if value is out of range.
     */
    public Number stringToCurrency(String value, String name, boolean percent) throws JposException {
        Number retval;
        if (value.equals("")) {
            retval = percent ? 0 : 0L;
        }
        else if (percent) {
            try {
                retval = Integer.parseInt(value);
                if (retval.intValue() <= 99) {  // We assume there is no vat rate or discount of 0.0099 % or less.
                    retval = retval.intValue() * 10000;
                }
            } catch (NumberFormatException | ArithmeticException ignore) {
                try {
                    retval = new BigDecimal(value).scaleByPowerOfTen(4).intValueExact();
                } catch(NumberFormatException | ArithmeticException e) {
                    throw new JposException(JPOS_E_ILLEGAL, 0, "Invalid " + name + ": " + value);
                }
            }
        } else {
            try {
                if (Data.CurrencyStringWithDecimalPoint) {
                    retval = new BigDecimal(value).scaleByPowerOfTen(4).longValueExact();
                } else
                    retval = Long.parseLong(value);
            } catch (NumberFormatException | ArithmeticException e) {
                throw new JposException(JPOS_E_ILLEGAL, 0, "Invalid " + name + ": " + value);
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
            check(Props.State == JPOS_S_BUSY, JPOS_E_BUSY, "Device is busy");
            checkCoverPaper(station);
        }
    }

    /**
     * Checks whether the cover is closed and paper present on all selected stations.
     * @param station   Selected station(s), any bit-wise combination of S_JOURNAL, S_RECEIPT and S_SLIP.
     * @throws JposException If AsyncMode is false and device busy, cover open or paper not present.
     */
    public void checkCoverPaper(int station) throws JposException {
        checkext(Data.CapCoverSensor && Data.CoverOpen, JPOS_EFPTR_COVER_OPEN, "Device cover open");
        if ((station & FPTR_S_SLIP) != 0) {
            checkext(Data.CapSlpEmptySensor && Data.SlpEmpty, JPOS_EFPTR_SLP_EMPTY, "No slip paper");
        }
        if ((station & FPTR_S_RECEIPT) != 0) {
            checkext(Data.CapRecEmptySensor && Data.RecEmpty, JPOS_EFPTR_REC_EMPTY, "No receipt paper");
        }
        if ((station & FPTR_S_JOURNAL) != 0) {
            checkext(Data.CapJrnEmptySensor && Data.JrnEmpty, JPOS_EFPTR_JRN_EMPTY, "No journal paper");
        }
    }

    /**
     * True if package adjustments may have the same (or a sub-set of) adjustment types as item adjustments. False if
     * only FPTR_AT_DISCOUNT and FPTR_AT_SURCHARGE are valid package adjustment types, as specified in the UPOS specification.
     */
    @Deprecated
    public Boolean AllowItemAdjustmentTypesInPackageAdjustment = false;

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

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param ignored Will not be used. Previous versions specified the value of Data.CurrencyStringWithDecimalPoint here.
     * @param device Device implementation object.
     */
    public FiscalPrinterService(FiscalPrinterProperties props, JposDevice device, boolean ignored) {
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
        check(!Data.CapAdditionalHeader, JPOS_E_ILLEGAL, "Invalid property 'AdditionalHeader'");
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
        check(!Data.CapAdditionalTrailer, JPOS_E_ILLEGAL, "Invalid property 'AdditionalTrailer'");
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
    public void setChangeDue(String changeDue) throws JposException {
        logPreSet("ChangeDue");
        if (changeDue == null)
            changeDue = "";
        checkOpened();
        check(!Data.CapChangeDue, JPOS_E_ILLEGAL, "Invalid property 'ChangeDue'");
        checkNoChangedOrClaimed(Data.ChangeDue, changeDue);
        FiscalPrinterInterface.changeDue(changeDue);
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
        check(!Data.CapCheckTotal, JPOS_E_ILLEGAL, "Invalid property 'CheckTotal'");
        checkNoChangedOrClaimed(Data.CheckTotal, check);
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
        long[] allowed = { FPTR_CID_FIRST, FPTR_CID_SECOND, FPTR_CID_SINGLE };
        logPreSet("ContractorId");
        checkOpened();
        check(!Data.CapMultiContractor, JPOS_E_ILLEGAL, "Changing 'ContractorId' invalid");
        checkMember(id, allowed,JPOS_E_ILLEGAL, "Invalid contractor ID: " + id);
        checkNoChangedOrClaimed(Data.ContractorId, id);
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
        long[] allowed = {
                FPTR_DT_CONF, FPTR_DT_EOD, FPTR_DT_RESET, FPTR_DT_RTC, FPTR_DT_VAT, FPTR_DT_START,
                FPTR_DT_TICKET_START, FPTR_DT_TICKET_END
        };
        logPreSet("DateType");
        checkOpened();
        checkMember(type, allowed, JPOS_E_ILLEGAL, "Illegal date specifier: " + type);
        checkNoChangedOrClaimed(Data.DateType, type);
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

    private Map<Integer, Map<String, int[]>> ValidVatRates = null;

    private void updateValidVatRates() {
        try {
            String[] data = {""};
            getData(FPTR_GD_VAT_ID_LIST, null, data);
            Map<Integer, Map<String, int[]>> validRates = new HashMap<>();
            for (String vatentry : data[0].split(";")) {
                String[] vatinfo = vatentry.split(":");
                if (vatinfo.length == 3) {
                    int id = Integer.parseInt(vatinfo[0].replaceAll(" ", ""));
                    String name = vatinfo[2];
                    String[] optargs = vatinfo[1].replaceAll(" ", "").split(",");
                    Map<String, int[]> rateinfo = new HashMap<>();
                    if (optargs.length == 1 && optargs[0].equals(""))
                        rateinfo.put(name, null);
                    else {
                        int[] args = new int[optargs.length];
                        for (int i = 0; i < optargs.length; i++)
                            args[i] = Integer.parseInt(optargs[i]);
                        rateinfo.put(name, args);
                    }
                    validRates.put(id, rateinfo);
                }
            }
            ValidVatRates = validRates;
        } catch (Exception ignore) {
            ValidVatRates = null;
        }
    }

    @Override
    public void setDeviceEnabled(boolean yes) throws JposException {
        super.setDeviceEnabled(yes);
        if (yes && Data.CapHasVatTable)
            updateValidVatRates();
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
        check(!Data.CapDuplicateReceipt, JPOS_E_ILLEGAL, "Changing 'DuplicateReceipt' invalid");
        checkNoChangedOrClaimed(Data.DuplicateReceipt, yes);
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
        long[] allowed = { FPTR_RS_RECEIPT, Data.CapFiscalReceiptStation ? FPTR_RS_SLIP : FPTR_RS_RECEIPT };
        logPreSet("FiscalReceiptStation");
        checkOpened();
        checkMember(station, allowed, JPOS_E_ILLEGAL, "Invalid station: " + station);
        checkext(Data.PrinterState != FPTR_PS_MONITOR && Data.PrinterState != FPTR_PS_LOCKED, JPOS_EFPTR_WRONG_STATE, "Neither locked nor in monitor state");
        check(station == FPTR_RS_SLIP && !Data.CapSlpPresent, JPOS_E_ILLEGAL, "No slip station");
        check(station == FPTR_RS_RECEIPT && !Data.CapRecPresent, JPOS_E_ILLEGAL, "No receipt station");
        checkNoChangedOrClaimed(Data.FiscalReceiptStation, station);
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
        long[] allowed = {
                FPTR_RT_CASH_IN, FPTR_RT_CASH_OUT, FPTR_RT_GENERIC, FPTR_RT_SALES, FPTR_RT_SERVICE,
                FPTR_RT_SIMPLE_INVOICE, FPTR_RT_REFUND
        };
        logPreSet("FiscalReceiptType");
        checkOpened();
        check(!Data.CapFiscalReceiptType, JPOS_E_ILLEGAL, "Invalid property 'FiscalReceiptType'");
        checkext(Data.PrinterState != FPTR_PS_MONITOR, JPOS_EFPTR_WRONG_STATE, "Not in monitor state");
        checkMember(type, allowed, JPOS_E_ILLEGAL, "Invalid receipt type: " + type);
        checkNoChangedOrClaimed(Data.FiscalReceiptType, type);
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
        long[] allowed = {
                FPTR_MT_ADVANCE, FPTR_MT_ADVANCE_PAID, FPTR_MT_AMOUNT_TO_BE_PAID, FPTR_MT_AMOUNT_TO_BE_PAID_BACK,
                FPTR_MT_CARD, FPTR_MT_CARD_NUMBER, FPTR_MT_CARD_TYPE, FPTR_MT_CASH, FPTR_MT_CASHIER,
                FPTR_MT_CASH_REGISTER_NUMBER, FPTR_MT_CHANGE, FPTR_MT_CHEQUE, FPTR_MT_CLIENT_NUMBER,
                FPTR_MT_CLIENT_SIGNATURE, FPTR_MT_COUNTER_STATE, FPTR_MT_CREDIT_CARD, FPTR_MT_CURRENCY,
                FPTR_MT_CURRENCY_VALUE, FPTR_MT_DEPOSIT, FPTR_MT_DEPOSIT_RETURNED, FPTR_MT_DOT_LINE,
                FPTR_MT_DRIVER_NUMB, FPTR_MT_EMPTY_LINE, FPTR_MT_FREE_TEXT, FPTR_MT_FREE_TEXT_WITH_DAY_LIMIT,
                FPTR_MT_GIVEN_DISCOUNT, FPTR_MT_LOCAL_CREDIT, FPTR_MT_MILEAGE_KM, FPTR_MT_NOTE, FPTR_MT_PAID,
                FPTR_MT_PAY_IN, FPTR_MT_POINT_GRANTED, FPTR_MT_POINTS_BONUS, FPTR_MT_POINTS_RECEIPT,
                FPTR_MT_POINTS_TOTAL, FPTR_MT_PROFITED, FPTR_MT_RATE, FPTR_MT_REGISTER_NUMB, FPTR_MT_SHIFT_NUMBER,
                FPTR_MT_STATE_OF_AN_ACCOUNT, FPTR_MT_SUBSCRIPTION, FPTR_MT_TABLE, FPTR_MT_THANK_YOU_FOR_LOYALTY,
                FPTR_MT_TRANSACTION_NUMB, FPTR_MT_VALID_TO, FPTR_MT_VOUCHER, FPTR_MT_VOUCHER_PAID,
                FPTR_MT_VOUCHER_VALUE, FPTR_MT_WITH_DISCOUNT, FPTR_MT_WITHOUT_UPLIFT
        };
        logPreSet("MessageType");
        checkOpened();
        checkMember(type, allowed, JPOS_E_ILLEGAL, "Invalid message type: " + type);
        checkNoChangedOrClaimed(Data.MessageType, type);
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
        long[] allowed = { FPTR_PS_FISCAL_RECEIPT, FPTR_PS_FISCAL_RECEIPT_TOTAL };
        logPreSet("PostLine");
        checkEnabled();
        check(!Data.CapPostPreLine, JPOS_E_ILLEGAL, "Post lines not supported");
        checkext(!member(Data.PrinterState, allowed), JPOS_EFPTR_WRONG_STATE, "Not in receipt or receipt total state");
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
        check(!Data.CapPostPreLine, JPOS_E_ILLEGAL, "Pre lines not supported");
        checkext(Data.PrinterState != FPTR_PS_FISCAL_RECEIPT, JPOS_EFPTR_WRONG_STATE, "Not in receipt state");
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
        long[] allowed = { FPTR_SS_FULL_LENGTH, FPTR_SS_VALIDATION };
        logPreSet("SlipSelection");
        checkEnabled();
        check(!Data.CapSlpPresent, JPOS_E_ILLEGAL, "Slip station not supported");
        checkMember(type, allowed, JPOS_E_ILLEGAL, "Invalid document type: " + type);
        check(type == FPTR_SS_VALIDATION && !Data.CapSlpValidation, JPOS_E_ILLEGAL, "Validation not supported");
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
        long[] allowed = { FPTR_TT_DOCUMENT, FPTR_TT_DAY, FPTR_TT_RECEIPT, FPTR_TT_GRAND };
        logPreSet("TotalizerType");
        checkEnabled();
        check(!Data.CapTotalizerType, JPOS_E_ILLEGAL, "Invalid property 'TotalizerType'");
        checkMember(type, allowed, JPOS_E_ILLEGAL, "Invalid totalizer type: " + type);
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
        logPreCall("BeginFiscalDocument", removeOuterArraySpecifier(new Object[]{documentAmount}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapSlpPresent || !Data.CapSlpFiscalDocument, JPOS_E_ILLEGAL, "Fiscal document printing not supported");
        checkext(Data.PrinterState != FPTR_PS_MONITOR, JPOS_EFPTR_WRONG_STATE, "Cannot change to fiscal document state");
        FiscalPrinterInterface.beginFiscalDocument(documentAmount);
        logCall("BeginFiscalDocument");
    }

    @Override
    public void beginFiscalReceipt(boolean printHeader) throws JposException {
        logPreCall("BeginFiscalReceipt", removeOuterArraySpecifier(new Object[]{printHeader}, Device.MaxArrayStringElements));
        checkEnabled();
        checkext(Data.PrinterState != FPTR_PS_MONITOR, JPOS_EFPTR_WRONG_STATE, "Cannot change to fiscal receipt state");
        FiscalPrinterInterface.beginFiscalReceipt(printHeader);
        logCall("BeginFiscalReceipt");
    }

    @Override
    public void beginFixedOutput(int station, int documentType) throws JposException {
        logPreCall("BeginFixedOutput", removeOuterArraySpecifier(new Object[]{station, documentType}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapFixedOutput, JPOS_E_ILLEGAL, "Non-fiscal fixed text printing not supported");
        checkext(Data.PrinterState != FPTR_PS_MONITOR, JPOS_EFPTR_WRONG_STATE, "Cannot change to non-fiscal document state");
        switch (station) {
            case FPTR_S_RECEIPT:
                check(!Data.CapRecPresent, JPOS_E_ILLEGAL, "Unsupported station: Receipt");
                break;
            case FPTR_S_SLIP:
                check(!Data.CapSlpPresent, JPOS_E_ILLEGAL, "Unsupported station: Slip");
                break;
            default:
                throw new JposException(JPOS_E_ILLEGAL, "Invalid station: " + station);
        }
        FiscalPrinterInterface.beginFixedOutput(station, documentType);
        logCall("BeginFixedOutput");
    }

    @Override
    public void beginInsertion(int timeout) throws JposException {
        logPreCall("BeginInsertion", removeOuterArraySpecifier(new Object[]{timeout}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapSlpPresent, JPOS_E_ILLEGAL, "Slip station not supported");
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout value: " + timeout);
        checkext(Data.PrinterState == FPTR_PS_LOCKED, JPOS_EFPTR_WRONG_STATE, "Device locked");
        FiscalPrinterInterface.beginInsertion(timeout);
        logCall("BeginInsertion");
    }

    @Override
    public void beginItemList(int vatID) throws JposException {
        logPreCall("BeginItemList", removeOuterArraySpecifier(new Object[]{vatID}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapItemList, JPOS_E_ILLEGAL, "Non-fiscal item list printing not supported");
        check(ValidVatRates != null && !ValidVatRates.containsKey(vatID), JPOS_E_ILLEGAL, "Invalid VAT id: " + vatID);
        checkext(Data.PrinterState != FPTR_PS_MONITOR, JPOS_EFPTR_WRONG_STATE, "Cannot change to item list state");
        FiscalPrinterInterface.beginItemList(vatID);
        logCall("BeginItemList");
    }

    @Override
    public void beginNonFiscal() throws JposException {
        logPreCall("BeginNonFiscal");
        checkEnabled();
        check(!Data.CapNonFiscalMode, JPOS_E_ILLEGAL, "Non-fiscal text printing not supported");
        checkext(Data.PrinterState != FPTR_PS_MONITOR, JPOS_EFPTR_WRONG_STATE, "Cannot change to non-fiscal text printing state");
        FiscalPrinterInterface.beginNonFiscal();
        logCall("BeginNonFiscal");
    }

    @Override
    public void beginRemoval(int timeout) throws JposException {
        logPreCall("BeginRemoval", removeOuterArraySpecifier(new Object[]{timeout}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapSlpPresent, JPOS_E_ILLEGAL, "Slip station not supported");
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout value: " + timeout);
        checkext(Data.PrinterState == FPTR_PS_LOCKED, JPOS_EFPTR_WRONG_STATE, "Device locked");
        FiscalPrinterInterface.beginRemoval(timeout);
        logCall("BeginRemoval");
    }

    @Override
    public void beginTraining() throws JposException {
        logPreCall("BeginTraining");
        checkEnabled();
        check(!Data.CapTrainingMode, JPOS_E_ILLEGAL, "Training mode not supported");
        checkext(Data.TrainingModeActive, JPOS_EFPTR_WRONG_STATE, "Device just in training mode");
        checkext(Data.PrinterState != FPTR_PS_MONITOR, JPOS_EFPTR_WRONG_STATE, "Cannot change to training mode");
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
        check(!Data.CapSlpFiscalDocument, JPOS_E_ILLEGAL, "Fiscal document printing not supported");
        checkext(Data.PrinterState != FPTR_PS_FISCAL_DOCUMENT, JPOS_EFPTR_WRONG_STATE, "Invalid printing state: " + Data.PrinterState);
        FiscalPrinterInterface.endFiscalDocument();
        logCall("EndFiscalDocument");
    }

    @Override
    public void endFiscalReceipt(boolean printHeader) throws JposException {
        long[] allowed = { FPTR_PS_FISCAL_RECEIPT, FPTR_PS_FISCAL_RECEIPT_ENDING };
        logPreCall("EndFiscalReceipt", removeOuterArraySpecifier(new Object[]{printHeader}, Device.MaxArrayStringElements));
        checkEnabled();
        checkext(!member(Data.PrinterState, allowed), JPOS_EFPTR_WRONG_STATE, "Invalid printing state: " + Data.PrinterState);
        FiscalPrinterInterface.endFiscalReceipt(printHeader);
        logCall("EndFiscalReceipt");
    }

    @Override
    public void endFixedOutput() throws JposException {
        logPreCall("EndFixedOutput");
        checkEnabled();
        check(!Data.CapFixedOutput, JPOS_E_ILLEGAL, "Non-fiscal fixed text printing not supported");
        checkext(Data.PrinterState != FPTR_PS_FIXED_OUTPUT, JPOS_EFPTR_WRONG_STATE, "Invalid printing state: " + Data.PrinterState);
        FiscalPrinterInterface.endFixedOutput();
        logCall("EndFixedOutput");
    }

    @Override
    public void endInsertion() throws JposException {
        logPreCall("EndInsertion");
        checkEnabled();
        check(!Data.CapSlpPresent, JPOS_E_ILLEGAL, "Slip station not supported");
        checkext(Data.PrinterState == FPTR_PS_LOCKED, JPOS_EFPTR_WRONG_STATE, "Device locked");
        FiscalPrinterInterface.endInsertion();
        logCall("EndInsertion");
    }

    @Override
    public void endItemList() throws JposException {
        logPreCall("EndItemList");
        checkEnabled();
        check(!Data.CapItemList, JPOS_E_ILLEGAL, "Non-fiscal item validation printing not supported");
        checkext(Data.PrinterState != FPTR_PS_ITEM_LIST, JPOS_EFPTR_WRONG_STATE, "Invalid printing state: " + Data.PrinterState);
        FiscalPrinterInterface.endItemList();
        logCall("EndItemList");
    }

    @Override
    public void endNonFiscal() throws JposException {
        logPreCall("EndNonFiscal");
        checkEnabled();
        check(!Data.CapNonFiscalMode, JPOS_E_ILLEGAL, "Non-fiscal text printing not supported");
        checkext(Data.PrinterState != FPTR_PS_NONFISCAL, JPOS_EFPTR_WRONG_STATE, "Invalid printing state: " + Data.PrinterState);
        FiscalPrinterInterface.endNonFiscal();
        logCall("EndNonFiscal");
    }

    @Override
    public void endRemoval() throws JposException {
        logPreCall("EndRemoval");
        checkEnabled();
        check(!Data.CapSlpPresent, JPOS_E_ILLEGAL, "Slip station not supported");
        checkext(Data.PrinterState == FPTR_PS_LOCKED, JPOS_EFPTR_WRONG_STATE, "Device locked");
        FiscalPrinterInterface.endRemoval();
        logCall("EndRemoval");
    }

    @Override
    public void endTraining() throws JposException {
        logPreCall("EndTraining");
        checkEnabled();
        check(!Data.CapTrainingMode, JPOS_E_ILLEGAL, "Training mode not supported");
        checkext(!Data.TrainingModeActive, JPOS_EFPTR_WRONG_STATE, "Device not in training mode");
        checkext(Data.PrinterState != FPTR_PS_MONITOR, JPOS_EFPTR_WRONG_STATE, "Cannot disable training mode");
        FiscalPrinterInterface.endTraining();
        logCall("EndTraining");
    }

    @Override
    public void getData(int dataItem, int[] optArgs, String[] data) throws JposException {
        logPreCall("GetData", removeOuterArraySpecifier(new Object[]{dataItem, optArgs, "..."}, Device.MaxArrayStringElements));
        long[] needOptArgs = { FPTR_GD_TENDER, FPTR_GD_LINECOUNT, FPTR_GD_DESCRIPTION_LENGTH };
        long[] allowedstr = { FPTR_GD_FIRMWARE, FPTR_GD_PRINTER_ID, FPTR_GD_TENDER, FPTR_GD_VAT_ID_LIST};
        long[] allowedint = {
                FPTR_GD_MID_VOID, FPTR_GD_RECEIPT_NUMBER, FPTR_GD_NUMB_CONFIG_BLOCK, FPTR_GD_NUMB_CURRENCY_BLOCK,
                FPTR_GD_NUMB_HDR_BLOCK, FPTR_GD_NUMB_RESET_BLOCK, FPTR_GD_NUMB_VAT_BLOCK, FPTR_GD_FISCAL_DOC,
                FPTR_GD_FISCAL_DOC_VOID, FPTR_GD_FISCAL_REC, FPTR_GD_FISCAL_REC_VOID, FPTR_GD_NONFISCAL_DOC,
                FPTR_GD_NONFISCAL_DOC_VOID, FPTR_GD_NONFISCAL_REC, FPTR_GD_RESTART, FPTR_GD_SIMP_INVOICE,
                FPTR_GD_Z_REPORT, FPTR_GD_LINECOUNT, FPTR_GD_DESCRIPTION_LENGTH
        };
        long[] allowedlong = {
                FPTR_GD_CURRENT_TOTAL, FPTR_GD_DAILY_TOTAL, FPTR_GD_GRAND_TOTAL, FPTR_GD_NOT_PAID, FPTR_GD_REFUND,
                FPTR_GD_REFUND_VOID
        };
        check(member(dataItem, needOptArgs) && (optArgs == null || optArgs.length != 1), JPOS_E_ILLEGAL, "optArgs not int[1]");
        check(data == null || data.length != 1, JPOS_E_ILLEGAL, "data not String[1]");
        checkEnabled();
        checkBusySync();
        if (member(dataItem, allowedstr))
            FiscalPrinterInterface.getData(dataItem, optArgs, data);
        else if (member(dataItem, allowedint)) {
            int[] intdata = {1};
            FiscalPrinterInterface.getData(dataItem, optArgs, intdata);
            data[0] = Integer.toString(intdata[0]);
        } else if (member(dataItem, allowedlong)) {
            long[] longdata = {1};
            FiscalPrinterInterface.getData(dataItem, optArgs, longdata);
            data[0] = Data.CurrencyStringWithDecimalPoint
                    ? new BigDecimal(longdata[0]).scaleByPowerOfTen(-4).stripTrailingZeros().toPlainString()
                    : Long.toString(longdata[0]);
        } else
            throw new JposException(JPOS_E_ILLEGAL, "Data item invalid: " + dataItem);
        logCall("GetData", removeOuterArraySpecifier(new Object[]{dataItem, optArgs, data}, Device.MaxArrayStringElements));
    }

    @Override
    public void getDate(String[] date) throws JposException {
        logPreCall("GetDate");
        checkEnabled();
        check(date == null, JPOS_E_ILLEGAL, "Unexpected null pointer argument");
        check(date.length != 1, JPOS_E_ILLEGAL, "Bad dimension of argument pointer");
        FiscalPrinterInterface.getDate(date);
        logCall("GetDate", removeOuterArraySpecifier(new Object[]{date[0]}, Device.MaxArrayStringElements));
    }

    @Override
    public void getTotalizer(int vatID, int optArgs, String[] data) throws JposException {
        logPreCall("GetTotalizer", removeOuterArraySpecifier(new Object[]{vatID, optArgs, "..."}, Device.MaxArrayStringElements));
        long[] allowed = {
                FPTR_GT_GROSS, FPTR_GT_NET, FPTR_GT_DISCOUNT, FPTR_GT_DISCOUNT_VOID, FPTR_GT_ITEM, FPTR_GT_ITEM_VOID,
                FPTR_GT_NOT_PAID, FPTR_GT_REFUND, FPTR_GT_REFUND_VOID, FPTR_GT_SUBTOTAL_DISCOUNT,
                FPTR_GT_SUBTOTAL_DISCOUNT_VOID, FPTR_GT_SUBTOTAL_SURCHARGES, FPTR_GT_SUBTOTAL_SURCHARGES_VOID,
                FPTR_GT_SURCHARGE, FPTR_GT_SURCHARGE_VOID, FPTR_GT_VAT, FPTR_GT_VAT_CATEGORY
        };
        checkEnabled();
        check(ValidVatRates != null && !ValidVatRates.containsKey(vatID), JPOS_E_ILLEGAL, "Invalid VAT id: " + vatID);
        check(data == null, JPOS_E_ILLEGAL, "Unexpected null pointer argument");
        check(data.length != 1, JPOS_E_ILLEGAL, "Bad dimension of argument pointer");
        checkMember(optArgs, allowed, JPOS_E_ILLEGAL, "Totalizer invalid: " + optArgs);
        long[] longdata = {1};
        FiscalPrinterInterface.getTotalizer(vatID, optArgs, longdata);
        data[0] = Data.CurrencyStringWithDecimalPoint
                ? new BigDecimal(longdata[0]).scaleByPowerOfTen(-4).stripTrailingZeros().toPlainString()
                : Long.toString(longdata[0]);
        logCall("GetTotalizer", removeOuterArraySpecifier(new Object[]{vatID, optArgs, data[0]}, Device.MaxArrayStringElements));
    }

    @Override
    public void getVatEntry(int vatID, int optArgs, int[] vatRate) throws JposException {
        logPreCall("GetVatEntry", removeOuterArraySpecifier(new Object[]{vatID, optArgs, "..."}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapHasVatTable, JPOS_E_ILLEGAL, "No VAT table");
        if (ValidVatRates != null) {
            check(!ValidVatRates.containsKey(vatID), JPOS_E_ILLEGAL, "Invalid VAT id: " + vatID);
            for (int[] arglist : ValidVatRates.get(vatID).values()) {
                if (arglist == null)
                    break;
                boolean error = true;
                for (int opt : arglist) {
                    if (opt == vatID) {
                        error = false;
                        break;
                    }
                }
                check(error, JPOS_E_ILLEGAL, "Invalid VAT ID: " + vatID);
            }
        }
        check(vatRate == null, JPOS_E_ILLEGAL, "Unexpected null pointer argument");
        check(vatRate.length != 1, JPOS_E_ILLEGAL, "Bad dimension of argument pointer");
        FiscalPrinterInterface.getVatEntry(vatID, optArgs, vatRate);
        logCall("GetVatEntry", removeOuterArraySpecifier(new Object[]{vatID, optArgs, vatRate[0]}, Device.MaxArrayStringElements));
    }

    @Override
    public void printDuplicateReceipt() throws JposException {
        logPreCall("PrintDuplicateReceipt");
        checkEnabled();
        check(!Data.CapDuplicateReceipt, JPOS_E_ILLEGAL, "Duplicate receipt not supported");
        checkext(Data.PrinterState != FPTR_PS_MONITOR, JPOS_EFPTR_WRONG_STATE, "Device not in monitor state");
        check(Data.State == JPOS_S_BUSY, JPOS_E_BUSY, "Output in progress");
        FiscalPrinterInterface.printDuplicateReceipt();
        logCall("PrintDuplicateReceipt");
    }

    @Override
    public void printPeriodicTotalsReport(String date1, String date2) throws JposException {
        logPreCall("PrintPeriodicTotalsReport", removeOuterArraySpecifier(new Object[]{date1, date2}, Device.MaxArrayStringElements));
        check(date1 == null || date2 == null, JPOS_E_ILLEGAL, "Starting date and ending date must not be null");
        checkEnabled();
        checkext(Data.PrinterState != FPTR_PS_MONITOR, JPOS_EFPTR_WRONG_STATE, "Device not in monitor state");
        SimpleDateFormat format = new SimpleDateFormat("ddMMyyyyHHmm");
        format.setLenient(false);
        Date start = format.parse(date1, new ParsePosition(0));
        Date end = format.parse(date2, new ParsePosition(0));
        check(start == null || date1.length() != format.toPattern().length(), JPOS_E_ILLEGAL, "Starting date invalid: " + date1);
        check(end == null || date2.length() != format.toPattern().length(), JPOS_E_ILLEGAL, "Ending date invalid: " + date2);
        check(start.compareTo(end) > 0, JPOS_E_ILLEGAL, "Starting date must not be after ending date");
        FiscalPrinterInterface.printPeriodicTotalsReport(date1, date2);
        logCall("PrintPeriodicTotalsReport");
    }

    @Override
    public void printPowerLossReport() throws JposException {
        logPreCall("PrintPowerLossReport");
        checkEnabled();
        check(!Data.CapPowerLossReport, JPOS_E_ILLEGAL, "Duplicate receipt not supported");
        checkext(Data.PrinterState != FPTR_PS_MONITOR, JPOS_EFPTR_WRONG_STATE, "Device not in monitor state");
        FiscalPrinterInterface.printPowerLossReport();
        logCall("PrintPowerLossReport");
    }

    @Override
    public void printRecVoidItem(String description, long price, int quantity, int adjustmentType, long adjustment, int vatInfo) throws JposException {
        logPreCall("PrintRecVoidItem", removeOuterArraySpecifier(new Object[]{description, price, quantity, adjustmentType, adjustment, vatInfo}, Device.MaxArrayStringElements));
        Data.checkForDeprecation(1011000, "Deprecated method, use PrintRecItemVoid and PrintRecItemAdjustmentVoid instead.");
        long[] allowedType = { FPTR_RT_SALES, FPTR_RT_SERVICE, FPTR_RT_SIMPLE_INVOICE, FPTR_RT_REFUND };
        long[] allowedamount = { FPTR_AT_AMOUNT_DISCOUNT, FPTR_AT_AMOUNT_SURCHARGE, FPTR_AT_COUPON_AMOUNT_DISCOUNT };
        long[] allowedpercent = { FPTR_AT_PERCENTAGE_DISCOUNT, FPTR_AT_PERCENTAGE_SURCHARGE, FPTR_AT_COUPON_PERCENTAGE_DISCOUNT };
        long[] allowedpositive = { FPTR_AT_AMOUNT_SURCHARGE, FPTR_AT_PERCENTAGE_SURCHARGE };
        check(description == null, JPOS_E_ILLEGAL, "Description must not be null");
        checkEnabled();
        checkext(Data.PrinterState != FPTR_PS_FISCAL_RECEIPT, JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        checkMember(Data.FiscalReceiptType, allowedType, JPOS_E_ILLEGAL, "Not a sale receipt");
        check(!member(adjustmentType, allowedamount) && !member(adjustmentType, allowedpercent), JPOS_E_ILLEGAL, "Invalid adjustment type: " + adjustmentType);
        check(!Data.CapAmountAdjustment && member(adjustmentType, allowedamount), JPOS_E_ILLEGAL, "Invalid adjustment type: " + adjustmentType);
        check(!Data.CapPercentAdjustment && member(adjustmentType, allowedpercent), JPOS_E_ILLEGAL, "Invalid adjustment type: " + adjustmentType);
        check(!Data.CapPositiveAdjustment && !member(adjustmentType, allowedpositive), JPOS_E_ILLEGAL, "Invalid adjustment type: " + adjustmentType);
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(description, "description");
        checkext(price < 0, JPOS_EFPTR_BAD_ITEM_AMOUNT, "price must be >= 0");
        checkext(quantity < 0, JPOS_EFPTR_BAD_ITEM_QUANTITY, "quantity must be >= 0");
        checkext(adjustment < 0, JPOS_EFPTR_BAD_PRICE, "adjustment must be >= 0");
        check(ValidVatRates != null && !ValidVatRates.containsKey(vatInfo), JPOS_E_ILLEGAL, "Invalid VAT id: " + vatInfo);
        callIt(FiscalPrinterInterface.printRecVoidItem(description, price, quantity, adjustmentType, adjustment, vatInfo), "PrintRecVoidItem");
    }

    @Override
    public void printReport(int reportType, String startNum, String endNum) throws JposException {
        logPreCall("PrintReport", removeOuterArraySpecifier(new Object[]{reportType, startNum, endNum}, Device.MaxArrayStringElements));
        long[] allowed = { FPTR_RT_ORDINAL, FPTR_RT_DATE, FPTR_RT_EOD_ORDINAL };
        check(startNum == null || endNum == null, JPOS_E_ILLEGAL, "Starting and final record must not be null");
        checkEnabled();
        checkext(Data.PrinterState != FPTR_PS_MONITOR, JPOS_EFPTR_WRONG_STATE, "Device not in monitor state");
        checkMember(reportType, allowed, JPOS_E_ILLEGAL, "Invalid report type: " + reportType);
        if (reportType == FPTR_RT_DATE) {
            SimpleDateFormat format = new SimpleDateFormat("ddMMyyyyHHmm");
            format.setLenient(false);
            Date start = format.parse(startNum, new ParsePosition(0));
            check(start == null || startNum.length() != format.toPattern().length(), JPOS_E_ILLEGAL, "Starting date invalid: " + startNum);
            Date end = format.parse(endNum, new ParsePosition(0));
            check(end == null || endNum.length() != format.toPattern().length(), JPOS_E_ILLEGAL, "Ending date invalid: " + endNum);
            check(start.compareTo(end) > 0, JPOS_E_ILLEGAL, "Starting date must not be after ending date");
        }
        else {
            try {
                int start = Integer.parseInt(startNum);
                int end = Integer.parseInt(endNum);
                check(start > end && end != 0, JPOS_E_ILLEGAL, "Starting record must not be greater than final record");
            } catch (NumberFormatException e) {
                throw new JposException(JPOS_E_ILLEGAL, "Starting and final record must be specified as numerical values");
            }
        }
        FiscalPrinterInterface.printReport(reportType, startNum, endNum);
        logCall("PrintReport");
    }

    @Override
    public void printXReport() throws JposException {
        logPreCall("PrintXReport");
        checkEnabled();
        check(!Data.CapXReport, JPOS_E_ILLEGAL, "X report not supported");
        checkext(Data.PrinterState != FPTR_PS_MONITOR, JPOS_EFPTR_WRONG_STATE, "Device not in monitor state");
        FiscalPrinterInterface.printXReport();
        logCall("PrintXReport");
    }

    @Override
    public void printZReport() throws JposException {
        logPreCall("PrintZReport");
        checkEnabled();
        checkext(Data.PrinterState != FPTR_PS_MONITOR, JPOS_EFPTR_WRONG_STATE, "Device not in monitor state");
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
        logPreCall("SetCurrency", removeOuterArraySpecifier(new Object[]{newCurrency}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapSetCurrency, JPOS_E_ILLEGAL, "Changing currency not supported");
        check(Data.DayOpened, JPOS_E_ILLEGAL, "Fiscal day open");
        checkext(Data.ActualCurrency == FPTR_AC_EUR, JPOS_EFPTR_WRONG_STATE, "Currency just changed to EUR");
        check(newCurrency != FPTR_SC_EURO, JPOS_E_ILLEGAL, "New currency not supported: " + newCurrency);
        FiscalPrinterInterface.setCurrency(newCurrency);
        logCall("SetCurrency");
    }

    @Override
    public void setDate(String date) throws JposException {
        logPreCall("SetDate", removeOuterArraySpecifier(new Object[]{date}, Device.MaxArrayStringElements));
        check(date == null, JPOS_E_ILLEGAL, "Date must not be null");
        checkEnabled();
        check(Data.DayOpened, JPOS_E_ILLEGAL, "Day open");
        SimpleDateFormat format = new SimpleDateFormat("ddMMyyyyHHmm");
        format.setLenient(false);
        Date start = format.parse(date, new ParsePosition(0));
        checkext(start == null || date.length() != format.toPattern().length(), JPOS_EFPTR_BAD_DATE, "Date invalid: " + date);
        FiscalPrinterInterface.setDate(date);
        logCall("SetDate");
    }

    @Override
    public void setHeaderLine(int lineNumber, String text, boolean doubleWidth) throws JposException {
        logPreCall("SetHeaderLine", removeOuterArraySpecifier(new Object[]{lineNumber, text, doubleWidth}, Device.MaxArrayStringElements));
        check(text == null, JPOS_E_ILLEGAL, "Text must not be null");
        checkEnabled();
        check(!Data.CapSetHeader, JPOS_E_ILLEGAL, "Setting header line not supported");
        check(Data.DayOpened, JPOS_E_ILLEGAL, "Day open");
        checkRange(lineNumber, 1, Data.NumHeaderLines, JPOS_E_ILLEGAL, "Line number out of range: " + lineNumber);
        checkext(Data.CapReservedWord && text.lastIndexOf(Data.ReservedWord) >= 0, JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Line contains reserved word");
        FiscalPrinterInterface.setHeaderLine(lineNumber, text, doubleWidth);
        logCall("SetHeaderLine");
    }

    @Override
    public void setPOSID(String POSID, String cashierID) throws JposException {
        logPreCall("SetPOSID", removeOuterArraySpecifier(new Object[]{POSID, cashierID}, Device.MaxArrayStringElements));
        check(POSID == null || cashierID == null, JPOS_E_ILLEGAL, "POSID and cashierID must not be null");
        checkEnabled();
        check(!Data.CapSetPOSID, JPOS_E_ILLEGAL, "Setting pos ID not supported");
        check(Data.DayOpened, JPOS_E_ILLEGAL, "Day open");
        checkReserved(POSID, "POSID");
        checkReserved(cashierID, "cashierID");
        FiscalPrinterInterface.setPOSID(POSID, cashierID);
        logCall("SetPOSID");
    }

    @Override
    public void setStoreFiscalID(String ID) throws JposException {
        logPreCall("SetStoreFiscalID", removeOuterArraySpecifier(new Object[]{ID}, Device.MaxArrayStringElements));
        check(ID == null, JPOS_E_ILLEGAL, "ID must not be null");
        checkEnabled();
        check(!Data.CapSetStoreFiscalID, JPOS_E_ILLEGAL, "Setting store fiscal ID not supported");
        check(Data.DayOpened, JPOS_E_ILLEGAL, "Day open");
        FiscalPrinterInterface.setStoreFiscalID(ID);
        logCall("SetStoreFiscalID");
    }

    @Override
    public void setTrailerLine(int lineNumber, String text, boolean doubleWidth) throws JposException {
        logPreCall("SetTrailerLine", removeOuterArraySpecifier(new Object[]{lineNumber, text, doubleWidth}, Device.MaxArrayStringElements));
        check(text == null, JPOS_E_ILLEGAL, "Text must not be null");
        checkEnabled();
        check(!Data.CapSetTrailer, JPOS_E_ILLEGAL, "Setting trailer line not supported");
        check(Data.DayOpened, JPOS_E_ILLEGAL, "Day open");
        checkRange(lineNumber, 1, Data.NumTrailerLines, JPOS_E_ILLEGAL, "Line number out of range: " + lineNumber);
        checkext(Data.CapReservedWord && text.lastIndexOf(Data.ReservedWord) >= 0, JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Line contains reserved word");
        FiscalPrinterInterface.setTrailerLine(lineNumber, text, doubleWidth);
        logCall("SetTrailerLine");
    }

    @Override
    public void setVatTable() throws JposException {
        logPreCall("SetVatTable");
        checkEnabled();
        check(!Data.CapHasVatTable, JPOS_E_ILLEGAL, "VAT tables not supported");
        check(!Data.CapSetVatTable, JPOS_E_ILLEGAL, "Setting VAT table not supported");
        check(Data.DayOpened, JPOS_E_ILLEGAL, "Day open");
        FiscalPrinterInterface.setVatTable();
        updateValidVatRates();
        logCall("SetVatTable");
    }

    /**
     * Sets the value of a specific VAT class in the VAT table. See UPOS specification, chapter Fiscal Printer -
     * Methods - setVatValue. In this implementation, vatValue must be either a percentage value (0 or a value between 0.01
     * and 99.9999, either specified with decimal point or with 4 implicit digits) or an integer and a percent value,
     * separated by comma. Examples for vatValue are "0", "0,0", "1,0", "19", "55000", "1,5.5".
     * <br>If vatValue is an integer and a percentage value separated by comma, the integer specifies the <i>optArgs</i>
     * argument that must be passed to <i>getVatEntry</i> together with vatID to retrieve the vat rate after the vat table
     * has been set.
     *
     * @param vatID    Index of the vat table entry to be set.
     * @param vatValue Tax value specifier, percentage value or integer and percentage value, separated by comma.
     * @throws JposException If an error occurs
     */
    @Override
    public void setVatValue(int vatID, String vatValue) throws JposException {
        logPreCall("SetVatValue", removeOuterArraySpecifier(new Object[]{vatID, vatValue}, Device.MaxArrayStringElements));
        check(vatValue == null, JPOS_E_ILLEGAL, "vatValue must not be null");
        checkEnabled();
        check(!Data.CapHasVatTable, JPOS_E_ILLEGAL, "VAT tables not supported");
        check(!Data.CapSetVatTable, JPOS_E_ILLEGAL, "Setting trailer line not supported");
        check(Data.DayOpened, JPOS_E_ILLEGAL, "Day open");
        if (vatValue.length() > 0 && vatValue.charAt(vatValue.length() - 1) == '%')
            vatValue = vatValue.substring(0, vatValue.length() - 1);
        String[] vatEntry = vatValue.split(",");
        check(vatEntry.length > 2, JPOS_E_ILLEGAL, "Invalid vatValue: " + vatValue);
        int[] values = new int[vatEntry.length];
        values[0] = stringToCurrency(vatEntry[vatEntry.length - 1], "Percentage Value", true).intValue();
        check(values[0] > 999999, JPOS_E_ILLEGAL, "Percentage value too big: " + vatEntry[vatEntry.length - 1]);
        if (values.length == 2) {
            try {
                values[1] = Integer.parseInt(vatEntry[0]);
            } catch (NumberFormatException e) {
                throw new JposException(JPOS_E_ILLEGAL, "Invalid integer part of vatValue: " + vatEntry[0]);
            }
            FiscalPrinterInterface.setVatValue(vatID, values[1], values[0]);

        } else {
            FiscalPrinterInterface.setVatValue(vatID, values[0]);
        }
        logCall("SetVatValue");
    }

    @Override
    public void verifyItem(String itemName, int vatID) throws JposException {
        logPreCall("VerifyItem", removeOuterArraySpecifier(new Object[]{itemName, vatID}, Device.MaxArrayStringElements));
        check(itemName == null, JPOS_E_ILLEGAL, "itemName must not be null");
        checkEnabled();
        check(!Data.CapHasVatTable, JPOS_E_ILLEGAL, "VAT tables not supported");
        check(!Data.CapItemList, JPOS_E_ILLEGAL, "Item list not supported");
        checkext(Data.PrinterState != FPTR_PS_ITEM_LIST, JPOS_EFPTR_WRONG_STATE, "Not in item list");
        checkext(Data.CapReservedWord && itemName.lastIndexOf(Data.ReservedWord) >= 0, JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Item name contains reserved word");
        check(ValidVatRates != null && !ValidVatRates.containsKey(vatID), JPOS_E_ILLEGAL, "Invalid VAT id: " + vatID);
        FiscalPrinterInterface.verifyItem(itemName, vatID);
        logCall("VerifyItem");
    }

    /*
    Possibly asynchronous methods
     */

    @Override
    public void printFiscalDocumentLine(String documentLine) throws JposException {
        logPreCall("PrintFiscalDocumentLine", removeOuterArraySpecifier(new Object[]{documentLine}, Device.MaxArrayStringElements));
        check(documentLine == null, JPOS_E_ILLEGAL, "DocumentLine must not be null");
        checkEnabled();
        check(!Data.CapSlpFiscalDocument, JPOS_E_ILLEGAL, "Fiscal document printing not supported");
        checkext(Data.PrinterState != FPTR_PS_FISCAL_DOCUMENT, JPOS_EFPTR_WRONG_STATE, "Not in fiscal document state");
        ifSyncCheckBusyCoverPaper(FPTR_S_SLIP);
        callIt(FiscalPrinterInterface.printFiscalDocumentLine(documentLine), "PrintFiscalDocumentLine");
    }

    @Override
    public void printFixedOutput(int documentType, int lineNumber, String data) throws JposException {
        logPreCall("PrintFixedOutput", removeOuterArraySpecifier(new Object[]{documentType, lineNumber, data}, Device.MaxArrayStringElements));
        check(data == null, JPOS_E_ILLEGAL, "Data must not be null");
        checkEnabled();
        check(!Data.CapFixedOutput, JPOS_E_ILLEGAL, "Fixed output printing not supported");
        checkext(Data.PrinterState != FPTR_PS_FIXED_OUTPUT, JPOS_EFPTR_WRONG_STATE, "Not in fixed output state");
        check(!Props.AsyncMode && Props.State == JPOS_S_BUSY, JPOS_E_BUSY, "Device is busy");
        callIt(FiscalPrinterInterface.printFixedOutput(documentType, lineNumber, data), "PrintFixedOutput");
    }

    @Override
    public void printNormal(int station, String data) throws JposException {
        logPreCall("PrintNormal", removeOuterArraySpecifier(new Object[]{station, data}, Device.MaxArrayStringElements));
        long[][] allowed = {
                { FPTR_S_JOURNAL, FPTR_S_RECEIPT, FPTR_S_SLIP },    // possible print stations
                { Data.CapJrnPresent ? 1 : 0, Data.CapRecPresent ? 1 : 0, Data.CapSlpPresent ? 1 : 0 },    // corresponding presence capabilities
                { -1, FPTR_RS_RECEIPT, FPTR_RS_SLIP }
        };
        long[] allowedFiscal = { FPTR_RT_GENERIC, FPTR_RT_CASH_IN, FPTR_RT_CASH_OUT };
        check(data == null, JPOS_E_ILLEGAL, "Data must not be null");
        checkEnabled();
        int stationindex;
        for (stationindex = 0; stationindex < allowed[0].length; stationindex++) {
            if (station == allowed[0][stationindex])
                break;
        }
        check(stationindex == allowed[0].length, JPOS_E_ILLEGAL, "Invalid station: " + station);
        if (Data.CapFiscalReceiptType && member(Data.FiscalReceiptType, allowedFiscal) && Data.PrinterState == FPTR_PS_FISCAL_RECEIPT) {
            check(Data.FiscalReceiptStation != allowed[2][stationindex], JPOS_E_ILLEGAL, "Station does not match receipt station: " + station);
        }
        else {
            checkext(Data.PrinterState != FPTR_PS_NONFISCAL, JPOS_EFPTR_WRONG_STATE, "Not in fixed output state");
            check(allowed[1][stationindex] == 0, JPOS_E_ILLEGAL, "Station does not exist: " + station);
        }
        ifSyncCheckBusyCoverPaper(station);
        callIt(FiscalPrinterInterface.printNormal(station, data), "PrintNormal");
    }

    @Override
    public void printRecCash(long amount) throws JposException {
        logPreCall("PrintRecCash", removeOuterArraySpecifier(new Object[]{amount}, Device.MaxArrayStringElements));
        long[] allowed = { FPTR_RT_CASH_IN, FPTR_RT_CASH_OUT };
        checkEnabled();
        check(!Data.CapFiscalReceiptType, JPOS_E_ILLEGAL, "Cash in / out not supported");
        checkext(Data.PrinterState != FPTR_PS_FISCAL_RECEIPT, JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        checkMember(Data.FiscalReceiptType, allowed, JPOS_E_ILLEGAL, "Not in cash in / cash out receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        check(amount <= 0, JPOS_E_ILLEGAL, "Amount of cash in / out must be > 0");
        check(truncUnusedDecimals(amount) != amount, JPOS_E_ILLEGAL, "Amount contains fractions of smallest cash units");
        callIt(FiscalPrinterInterface.printRecCash(amount), "PrintRecCash");
    }

    @Override
    public void printRecItem(String description, long price, int quantity, int vatInfo, long unitPrice, String unitName) throws JposException {
        logPreCall("PrintRecItem", removeOuterArraySpecifier(new Object[]{description, price, quantity, vatInfo, unitPrice, unitName}, Device.MaxArrayStringElements));
        long[] allowed = { FPTR_RT_SALES, FPTR_RT_SERVICE, FPTR_RT_SIMPLE_INVOICE, FPTR_RT_REFUND };
        check(description == null, JPOS_E_ILLEGAL, "Description must not be null");
        if (unitName == null)
            unitName = "";
        checkEnabled();
        checkext(Data.PrinterState != FPTR_PS_FISCAL_RECEIPT, JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        checkMember(Data.FiscalReceiptType, allowed, JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(description, "description");
        checkext(price < 0, JPOS_EFPTR_BAD_ITEM_AMOUNT, "price must be >= 0");
        checkext(quantity < 0, JPOS_EFPTR_BAD_ITEM_QUANTITY, "quantity must be >= 0");
        check(ValidVatRates != null && !ValidVatRates.containsKey(vatInfo), JPOS_E_ILLEGAL, "Invalid VAT id: " + vatInfo);
        checkext(unitPrice < 0, JPOS_EFPTR_BAD_PRICE, "unitPrice must be >= 0");
        checkReserved(unitName, "unitName");
        callIt(FiscalPrinterInterface.printRecItem(description, price, quantity, vatInfo, unitPrice, unitName), "PrintRecItem");
    }

    @Override
    public void printRecItemAdjustment(int adjustmentType, String description, long amount, int vatInfo) throws JposException {
        logPreCall("PrintRecItemAdjustment", removeOuterArraySpecifier(new Object[]{adjustmentType, description, amount, vatInfo}, Device.MaxArrayStringElements));
        long[] allowed = { FPTR_RT_SALES, FPTR_RT_SERVICE, FPTR_RT_SIMPLE_INVOICE, FPTR_RT_REFUND };
        long[] allowedamount = { FPTR_AT_AMOUNT_DISCOUNT, FPTR_AT_AMOUNT_SURCHARGE, FPTR_AT_COUPON_AMOUNT_DISCOUNT };
        long[] allowedpercent = { FPTR_AT_PERCENTAGE_DISCOUNT, FPTR_AT_PERCENTAGE_SURCHARGE, FPTR_AT_COUPON_PERCENTAGE_DISCOUNT };
        long[] allowedpositive = { FPTR_AT_AMOUNT_SURCHARGE, FPTR_AT_PERCENTAGE_SURCHARGE };
        check(description == null, JPOS_E_ILLEGAL, "Description must not be null");
        checkEnabled();
        checkext(Data.PrinterState != FPTR_PS_FISCAL_RECEIPT, JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        checkMember(Data.FiscalReceiptType, allowed, JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        check(!member(adjustmentType, allowedamount) && !member(adjustmentType, allowedpercent), JPOS_E_ILLEGAL, "Invalid adjustment type: " + adjustmentType);
        check(!Data.CapAmountAdjustment && member(adjustmentType, allowedamount), JPOS_E_ILLEGAL, "Unsupported adjustment: " + adjustmentType);
        check(!Data.CapPercentAdjustment && member(adjustmentType, allowedpercent), JPOS_E_ILLEGAL, "Unsupported adjustment: " + adjustmentType);
        check(!Data.CapPositiveAdjustment && member(adjustmentType, allowedpositive), JPOS_E_ILLEGAL, "Unsupported adjustment: " + adjustmentType);
        checkReserved(description, "description");
        checkext(amount <= 0, JPOS_EFPTR_BAD_ITEM_AMOUNT, "amount must be > 0");
        check(ValidVatRates != null && !ValidVatRates.containsKey(vatInfo), JPOS_E_ILLEGAL, "Invalid VAT id: " + vatInfo);
        callIt(FiscalPrinterInterface.printRecItemAdjustment(adjustmentType, description, amount, vatInfo), "PrintRecItemAdjustment");
    }

    @Override
    public void printRecItemAdjustmentVoid(int adjustmentType, String description, long amount, int vatInfo) throws JposException {
        logPreCall("PrintRecItemAdjustmentVoid", removeOuterArraySpecifier(new Object[]{adjustmentType, description, amount, vatInfo}, Device.MaxArrayStringElements));
        long[] allowed = { FPTR_RT_SALES, FPTR_RT_SERVICE, FPTR_RT_SIMPLE_INVOICE, FPTR_RT_REFUND };
        long[] allowedamount = { FPTR_AT_AMOUNT_DISCOUNT, FPTR_AT_AMOUNT_SURCHARGE, FPTR_AT_COUPON_AMOUNT_DISCOUNT };
        long[] allowedpercent = { FPTR_AT_PERCENTAGE_DISCOUNT, FPTR_AT_PERCENTAGE_SURCHARGE, FPTR_AT_COUPON_PERCENTAGE_DISCOUNT };
        long[] allowedpositive = { FPTR_AT_AMOUNT_SURCHARGE, FPTR_AT_PERCENTAGE_SURCHARGE };
        check(description == null, JPOS_E_ILLEGAL, "Description must not be null");
        checkEnabled();
        checkext(Data.PrinterState != FPTR_PS_FISCAL_RECEIPT, JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        checkMember(Data.FiscalReceiptType, allowed, JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        check(!member(adjustmentType, allowedamount) && !member(adjustmentType, allowedpercent), JPOS_E_ILLEGAL, "Invalid adjustment type: " + adjustmentType);
        check(!Data.CapAmountAdjustment && member(adjustmentType, allowedamount), JPOS_E_ILLEGAL, "Unsupported adjustment: " + adjustmentType);
        check(!Data.CapPercentAdjustment && member(adjustmentType, allowedpercent), JPOS_E_ILLEGAL, "Unsupported adjustment: " + adjustmentType);
        check(!Data.CapPositiveAdjustment && member(adjustmentType, allowedpositive), JPOS_E_ILLEGAL, "Unsupported adjustment: " + adjustmentType);
        checkReserved(description, "description");
        checkext(amount <= 0, JPOS_EFPTR_BAD_ITEM_AMOUNT, "amount must be > 0");
        check(ValidVatRates != null && !ValidVatRates.containsKey(vatInfo), JPOS_E_ILLEGAL, "Invalid VAT id: " + vatInfo);
        callIt(FiscalPrinterInterface.printRecItemAdjustmentVoid(adjustmentType, description, amount, vatInfo), "PrintRecItemAdjustmentVoid");
    }

    @Override
    public void printRecItemFuel(String description, long price, int quantity, int vatInfo, long unitPrice, String unitName, long specialTax, String specialTaxName) throws JposException {
        logPreCall("PrintRecItemFuel", removeOuterArraySpecifier(new Object[]{description, price, quantity, vatInfo, unitPrice, unitName, specialTax, specialTaxName}, Device.MaxArrayStringElements));
        long[] allowed = { FPTR_RT_SALES, FPTR_RT_SERVICE, FPTR_RT_SIMPLE_INVOICE, FPTR_RT_REFUND };
        check(description == null, JPOS_E_ILLEGAL, "Description must not be null");
        if (unitName == null)
            unitName = "";
        if (specialTaxName == null)
            specialTaxName = "";
        checkEnabled();
        checkext(Data.PrinterState != FPTR_PS_FISCAL_RECEIPT, JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        checkMember(Data.FiscalReceiptType, allowed, JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(description, "description");
        checkext(price < 0, JPOS_EFPTR_BAD_ITEM_AMOUNT, "price must be >= 0");
        checkext(quantity < 0, JPOS_EFPTR_BAD_ITEM_QUANTITY, "quantity must be >= 0");
        check(ValidVatRates != null && !ValidVatRates.containsKey(vatInfo), JPOS_E_ILLEGAL, "Invalid VAT id: " + vatInfo);
        checkext(unitPrice < 0, JPOS_EFPTR_BAD_PRICE, "unitPrice must be >= 0");
        check(specialTax < 0, JPOS_E_ILLEGAL, "specialTax must be >= 0");
        checkReserved(unitName, "unitName");
        checkReserved(specialTaxName, "specialTaxName");
        callIt(FiscalPrinterInterface.printRecItemFuel(description, price, quantity, vatInfo, unitPrice, unitName, specialTax, specialTaxName), "PrintRecItemFuel");
    }

    @Override
    public void printRecItemFuelVoid(String description, long price, int vatInfo, long specialTax) throws JposException {
        logPreCall("PrintRecItemFuelVoid", removeOuterArraySpecifier(new Object[]{description, price, vatInfo, specialTax}, Device.MaxArrayStringElements));
        long[] allowed = { FPTR_RT_SALES, FPTR_RT_SERVICE, FPTR_RT_SIMPLE_INVOICE, FPTR_RT_REFUND };
        check(description == null, JPOS_E_ILLEGAL, "Description must not be null");
        checkEnabled();
        checkext(Data.PrinterState != FPTR_PS_FISCAL_RECEIPT, JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        checkMember(Data.FiscalReceiptType, allowed, JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(description, "description");
        checkext(price < 0, JPOS_EFPTR_BAD_ITEM_AMOUNT, "price must be >= 0");
        check(ValidVatRates != null && !ValidVatRates.containsKey(vatInfo), JPOS_E_ILLEGAL, "Invalid VAT id: " + vatInfo);
        check(specialTax < 0, JPOS_E_ILLEGAL, "specialTax must be >= 0");
        callIt(FiscalPrinterInterface.printRecItemFuelVoid(description, price, vatInfo, specialTax), "PrintRecItemFuelVoid");
    }

    @Override
    public void printRecItemRefund(String description, long amount, int quantity, int vatInfo, long unitAmount, String unitName) throws JposException {
        logPreCall("PrintRecItemRefund", removeOuterArraySpecifier(new Object[]{description, amount, quantity, vatInfo, unitAmount, unitName}, Device.MaxArrayStringElements));
        long[] allowed = { FPTR_RT_SALES, FPTR_RT_SERVICE, FPTR_RT_REFUND };
        check(description == null, JPOS_E_ILLEGAL, "Description must not be null");
        if (unitName == null)
            unitName = "";
        checkEnabled();
        checkext(Data.PrinterState != FPTR_PS_FISCAL_RECEIPT, JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        checkMember(Data.FiscalReceiptType, allowed, JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(description, "description");
        checkext(amount < 0, JPOS_EFPTR_BAD_ITEM_AMOUNT, "price must be >= 0");
        checkext(quantity < 0, JPOS_EFPTR_BAD_ITEM_QUANTITY, "quantity must be >= 0");
        check(ValidVatRates != null && !ValidVatRates.containsKey(vatInfo), JPOS_E_ILLEGAL, "Invalid VAT id: " + vatInfo);
        checkext(unitAmount < 0, JPOS_EFPTR_BAD_PRICE, "unitPrice must be >= 0");
        checkReserved(unitName, "unitName");
        callIt(FiscalPrinterInterface.printRecItemRefund(description, amount, quantity, vatInfo, unitAmount, unitName), "PrintRecItemRefund");
    }

    @Override
    public void printRecItemRefundVoid(String description, long amount, int quantity, int vatInfo, long unitAmount, String unitName) throws JposException {
        logPreCall("PrintRecItemRefundVoid", removeOuterArraySpecifier(new Object[]{description, amount, quantity, vatInfo, unitAmount, unitName}, Device.MaxArrayStringElements));
        long[] allowed = { FPTR_RT_SALES, FPTR_RT_SERVICE, FPTR_RT_REFUND };
        check(description == null, JPOS_E_ILLEGAL, "Description must not be null");
        if (unitName == null)
            unitName = "";
        checkEnabled();
        checkext(Data.PrinterState != FPTR_PS_FISCAL_RECEIPT, JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        checkMember(Data.FiscalReceiptType, allowed, JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(description, "description");
        checkext(amount < 0, JPOS_EFPTR_BAD_ITEM_AMOUNT, "price must be >= 0");
        checkext(quantity < 0, JPOS_EFPTR_BAD_ITEM_QUANTITY, "quantity must be >= 0");
        check(ValidVatRates != null && !ValidVatRates.containsKey(vatInfo), JPOS_E_ILLEGAL, "Invalid VAT id: " + vatInfo);
        checkext(unitAmount < 0, JPOS_EFPTR_BAD_PRICE, "unitPrice must be >= 0");
        checkReserved(unitName, "unitName");
        callIt(FiscalPrinterInterface.printRecItemRefundVoid(description, amount, quantity, vatInfo, unitAmount, unitName), "PrintRecItemRefundVoid");
    }

    @Override
    public void printRecItemVoid(String description, long price, int quantity, int vatInfo, long unitPrice, String unitName) throws JposException {
        logPreCall("PrintRecItemVoid", removeOuterArraySpecifier(new Object[]{description, price, quantity, vatInfo, unitPrice, unitName}, Device.MaxArrayStringElements));
        long[] allowed = { FPTR_RT_SALES, FPTR_RT_SERVICE, FPTR_RT_SIMPLE_INVOICE, FPTR_RT_REFUND };
        check(description == null, JPOS_E_ILLEGAL, "Description and unitName must not be null");
        if (unitName == null)
            unitName = "";
        checkEnabled();
        checkext(Data.PrinterState != FPTR_PS_FISCAL_RECEIPT, JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        checkMember(Data.FiscalReceiptType, allowed, JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(description, "description");
        checkext(price < 0, JPOS_EFPTR_BAD_ITEM_AMOUNT, "price must be >= 0");
        checkext(quantity < 0, JPOS_EFPTR_BAD_ITEM_QUANTITY, "quantity must be >= 0");
        check(ValidVatRates != null && !ValidVatRates.containsKey(vatInfo), JPOS_E_ILLEGAL, "Invalid VAT id: " + vatInfo);
        checkext(unitPrice < 0, JPOS_EFPTR_BAD_PRICE, "unitPrice must be >= 0");
        checkReserved(unitName, "unitName");
        callIt(FiscalPrinterInterface.printRecItemVoid(description, price, quantity, vatInfo, unitPrice, unitName), "PrintRecItemVoid");
    }

    @Override
    public void printRecMessage(String message) throws JposException {
        logPreCall("PrintRecMessage", removeOuterArraySpecifier(new Object[]{message}, Device.MaxArrayStringElements));
        long[] allowed = { FPTR_RT_SALES, FPTR_RT_SERVICE, FPTR_RT_SIMPLE_INVOICE, FPTR_RT_REFUND };
        long[] allowedState = { FPTR_PS_FISCAL_RECEIPT, FPTR_PS_FISCAL_RECEIPT_TOTAL, FPTR_PS_FISCAL_RECEIPT_ENDING };
        check(message == null, JposConst.JPOS_E_ILLEGAL, "Message must not be null");
        checkEnabled();
        check(!Data.CapAdditionalLines, JposConst.JPOS_E_ILLEGAL, "Additional lines not supported");
        checkext(!member(Data.PrinterState, allowedState), JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(message, "message");
        if (!Data.AsyncMode) {
            checkext(message.length() > Data.MessageLength, JPOS_EFPTR_BAD_ITEM_DESCRIPTION, "Message too long");
        }
        callIt(FiscalPrinterInterface.printRecMessage(message), "PrintRecMessage");
    }

    @Override
    public void printRecNotPaid(String description, long amount) throws JposException {
        logPreCall("PrintRecNotPaid", removeOuterArraySpecifier(new Object[]{description, amount}, Device.MaxArrayStringElements));
        long[] allowed = { FPTR_RT_SALES, FPTR_RT_SERVICE, FPTR_RT_SIMPLE_INVOICE, FPTR_RT_REFUND };
        long[] allowedState = { FPTR_PS_FISCAL_RECEIPT, FPTR_PS_FISCAL_RECEIPT_TOTAL };
        check(description == null, JposConst.JPOS_E_ILLEGAL, "Description must not be null");
        checkEnabled();
        check(!Data.CapReceiptNotPaid, JposConst.JPOS_E_ILLEGAL, "Receipt not paid not supported");
        checkext(!member(Data.PrinterState, allowedState), JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(description, "description");
        check(amount <= 0, JposConst.JPOS_E_ILLEGAL, "Amount <= 0");
        callIt(FiscalPrinterInterface.printRecNotPaid(description, amount), "PrintRecNotPaid");
    }

    @Override
    public void printRecPackageAdjustment(int adjustmentType, String description, String vatAdjustment) throws JposException {
        logPreCall("PrintRecPackageAdjustment", removeOuterArraySpecifier(new Object[]{adjustmentType, description, vatAdjustment}, Device.MaxArrayStringElements));
        long[] allowed = { FPTR_RT_SALES, FPTR_RT_SERVICE, FPTR_RT_SIMPLE_INVOICE, FPTR_RT_REFUND };
        long[] allowedType = AllowItemAdjustmentTypesInPackageAdjustment ? new long[]{
                FPTR_AT_AMOUNT_DISCOUNT, FPTR_AT_AMOUNT_SURCHARGE, FPTR_AT_COUPON_AMOUNT_DISCOUNT,
                FPTR_AT_PERCENTAGE_DISCOUNT, FPTR_AT_PERCENTAGE_SURCHARGE, FPTR_AT_COUPON_PERCENTAGE_DISCOUNT
        } : new long[]{FPTR_AT_DISCOUNT, FPTR_AT_SURCHARGE};
        check(vatAdjustment == null || description == null, JposConst.JPOS_E_ILLEGAL, "description and vatAdjustment must not be null");
        checkEnabled();
        check(!Data.CapPackageAdjustment, JposConst.JPOS_E_ILLEGAL, "Package Adjustment not supported");
        checkext(Data.PrinterState != FPTR_PS_FISCAL_RECEIPT, JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        checkMember(adjustmentType, allowedType, JposConst.JPOS_E_ILLEGAL, "Adjustment not supported: " + adjustmentType);
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(description, "description");
        String[] adjustments = vatAdjustment.split(";");
        check(adjustments.length > Data.NumVatRates, JposConst.JPOS_E_ILLEGAL, "Bad number of pairs of VAT ID and amount");
        Map<Integer, Number> vatAdjustments = new HashMap<>();
        vatAdjustment = getAndModifyVatAdjustments(adjustmentType, vatAdjustment, vatAdjustments);
        callIt(FiscalPrinterInterface.printRecPackageAdjustment(adjustmentType, description, vatAdjustment, vatAdjustments), "PrintRecPackageAdjustment");
    }

    @Override
    public void printRecPackageAdjustVoid(int adjustmentType, String vatAdjustment) throws JposException {
        logPreCall("PrintRecPackageAdjustVoid", removeOuterArraySpecifier(new Object[]{adjustmentType, vatAdjustment}, Device.MaxArrayStringElements));
        long[] allowed = { FPTR_RT_SALES, FPTR_RT_SERVICE, FPTR_RT_SIMPLE_INVOICE, FPTR_RT_REFUND };
        long[] allowedType = AllowItemAdjustmentTypesInPackageAdjustment ? new long[]{
                FPTR_AT_AMOUNT_DISCOUNT, FPTR_AT_AMOUNT_SURCHARGE, FPTR_AT_COUPON_AMOUNT_DISCOUNT,
                FPTR_AT_PERCENTAGE_DISCOUNT, FPTR_AT_PERCENTAGE_SURCHARGE, FPTR_AT_COUPON_PERCENTAGE_DISCOUNT
        } : new long[]{FPTR_AT_DISCOUNT, FPTR_AT_SURCHARGE};
        check(vatAdjustment == null, JposConst.JPOS_E_ILLEGAL, "vatAdjustment must not be null");
        checkEnabled();
        check(!Data.CapPackageAdjustment, JposConst.JPOS_E_ILLEGAL, "Package Adjustment not supported");
        checkext(Data.PrinterState != FPTR_PS_FISCAL_RECEIPT, JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        checkMember(adjustmentType, allowedType, JposConst.JPOS_E_ILLEGAL, "Adjustment not supported: " + adjustmentType);
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        Map<Integer, Number> vatAdjustments = new HashMap<>();
        vatAdjustment = getAndModifyVatAdjustments(adjustmentType, vatAdjustment, vatAdjustments);
        callIt(FiscalPrinterInterface.printRecPackageAdjustVoid(adjustmentType, vatAdjustment, vatAdjustments), "PrintRecPackageAdjustVoid");
    }

    private String getAndModifyVatAdjustments(int adjustmentType, String vatAdjustment, Map<Integer, Number> vatAdjustments) throws JposException {
        long[] percentTypes = { FPTR_AT_PERCENTAGE_DISCOUNT, FPTR_AT_PERCENTAGE_SURCHARGE, FPTR_AT_COUPON_PERCENTAGE_DISCOUNT };
        String[] adjustments = vatAdjustment.split(";");
        check(adjustments.length > Data.NumVatRates, JposConst.JPOS_E_ILLEGAL, "Bad number of pairs of VAT ID and amount");
        boolean percent = member(adjustmentType, percentTypes);
        StringBuilder vatAdjustmentBuilder = new StringBuilder();
        for (String adjustment : adjustments) {
            String[] value = adjustment.replaceAll(" ", "").split(",");
            check(value.length != 2, JposConst.JPOS_E_ILLEGAL, "Mal-formatted vatAdjustment parameter");
            try {
                int vatid = Integer.parseInt(value[0]);
                check(ValidVatRates != null && !ValidVatRates.containsKey(vatid), JPOS_E_ILLEGAL, "Invalid VAT id: " + vatid);
                boolean percentvalue = value[1].charAt(value[1].length() - 1) == '%';
                if (percentvalue)
                    value[1] = value[1].substring(0, value[1].length() - 1);
                Number amount = percent || percentvalue ? stringToCurrency(value[1], "percentage for VAT ID " + value[0], true)
                        : (Data.CurrencyStringWithDecimalPoint ? new BigDecimal(value[1]).scaleByPowerOfTen(4).longValueExact() : Long.parseLong(value[1]));
                check(vatAdjustments.containsKey(vatid), JposConst.JPOS_E_ILLEGAL, "VatID specified twice: " + value[0]);
                vatAdjustments.put(vatid, amount);
                vatAdjustmentBuilder.append(";").append(vatid).append(",").append(new BigDecimal(amount.longValue()).scaleByPowerOfTen(-4).stripTrailingZeros().toPlainString());
                if (percentvalue)
                    vatAdjustmentBuilder.append("%");
            } catch (NumberFormatException | ArithmeticException e) {
                throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid adjustment " + adjustment + ": " + e.getMessage(), e);
            }
        }
        vatAdjustment = vatAdjustmentBuilder.toString();
        return vatAdjustment.length() > 0 ? vatAdjustment.substring(1) : vatAdjustment;
    }

    @Override
    public void printRecRefund(String description, long amount, int vatInfo) throws JposException {
        logPreCall("PrintRecRefund", removeOuterArraySpecifier(new Object[]{description, amount, vatInfo}, Device.MaxArrayStringElements));
        long[] allowed = { FPTR_RT_SALES, FPTR_RT_SERVICE, FPTR_RT_REFUND };
        check(description == null, JposConst.JPOS_E_ILLEGAL, "Description must not be null");
        checkEnabled();
        checkext(Data.PrinterState != FPTR_PS_FISCAL_RECEIPT, JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(description, "description");
        checkext(amount < 0, JPOS_EFPTR_BAD_ITEM_AMOUNT, "price must be >= 0");
        check(ValidVatRates != null && !ValidVatRates.containsKey(vatInfo), JPOS_E_ILLEGAL, "Invalid VAT id: " + vatInfo);
        callIt(FiscalPrinterInterface.printRecRefund(description, amount, vatInfo), "PrintRecRefund");
    }

    @Override
    public void printRecRefundVoid(String description, long amount, int vatInfo) throws JposException {
        logPreCall("PrintRecRefundVoid", removeOuterArraySpecifier(new Object[]{description, amount, vatInfo}, Device.MaxArrayStringElements));
        long[] allowed = { FPTR_RT_SALES, FPTR_RT_SERVICE, FPTR_RT_REFUND };
        check(description == null, JposConst.JPOS_E_ILLEGAL, "Description must not be null");
        checkEnabled();
        checkext(Data.PrinterState != FPTR_PS_FISCAL_RECEIPT, JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(description, "description");
        checkext(amount < 0, JPOS_EFPTR_BAD_ITEM_AMOUNT, "price must be >= 0");
        check(ValidVatRates != null && !ValidVatRates.containsKey(vatInfo), JPOS_E_ILLEGAL, "Invalid VAT id: " + vatInfo);
        callIt(FiscalPrinterInterface.printRecRefundVoid(description, amount, vatInfo), "PrintRecRefundVoid");
    }

    @Override
    public void printRecSubtotal(long amount) throws JposException {
        logPreCall("PrintRecSubtotal", removeOuterArraySpecifier(new Object[]{amount}, Device.MaxArrayStringElements));
        long[] allowed = { FPTR_RT_SALES, FPTR_RT_SERVICE, FPTR_RT_SIMPLE_INVOICE, FPTR_RT_REFUND };
        checkEnabled();
        checkext(Data.PrinterState != FPTR_PS_FISCAL_RECEIPT, JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        check(amount < 0, JposConst.JPOS_E_ILLEGAL, "amount < 0");
        callIt(FiscalPrinterInterface.printRecSubtotal(amount), "PrintRecSubtotal");
    }

    @Override
    public void printRecSubtotalAdjustment(int adjustmentType, String description, long amount) throws JposException {
        logPreCall("PrintRecSubtotalAdjustment", removeOuterArraySpecifier(new Object[]{adjustmentType, description, amount}, Device.MaxArrayStringElements));
        long[] allowed = { FPTR_RT_SALES, FPTR_RT_SERVICE, FPTR_RT_SIMPLE_INVOICE, FPTR_RT_REFUND };
        long[] allowedamount = { FPTR_AT_AMOUNT_DISCOUNT, FPTR_AT_AMOUNT_SURCHARGE, FPTR_AT_COUPON_AMOUNT_DISCOUNT };
        long[] allowedpercent = { FPTR_AT_PERCENTAGE_DISCOUNT, FPTR_AT_PERCENTAGE_SURCHARGE, FPTR_AT_COUPON_PERCENTAGE_DISCOUNT };
        long[] allowedpositive = { FPTR_AT_AMOUNT_SURCHARGE, FPTR_AT_PERCENTAGE_SURCHARGE };
        check(description == null, JposConst.JPOS_E_ILLEGAL, "Description must not be null");
        checkEnabled();
        checkext(Data.PrinterState != FPTR_PS_FISCAL_RECEIPT, JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        check(!member(adjustmentType, allowedamount) && !member(adjustmentType, allowedpercent), JposConst.JPOS_E_ILLEGAL, "Invalid adjustment type: " + adjustmentType);
        check(!Data.CapSubAmountAdjustment && member(adjustmentType, allowedamount), JposConst.JPOS_E_ILLEGAL, "Unsupported adjustment: " + adjustmentType);
        check(!Data.CapSubPercentAdjustment && member(adjustmentType, allowedpercent), JposConst.JPOS_E_ILLEGAL, "Unsupported adjustment: " + adjustmentType);
        check(!Data.CapPositiveSubtotalAdjustment && member(adjustmentType, allowedpositive), JposConst.JPOS_E_ILLEGAL, "Unsupported adjustment: " + adjustmentType);
        checkReserved(description, "description");
        checkext(amount <= 0, JPOS_EFPTR_BAD_ITEM_AMOUNT, "amount must be > 0");
        callIt(FiscalPrinterInterface.printRecSubtotalAdjustment(adjustmentType, description, amount), "PrintRecSubtotalAdjustment");
    }

    @Override
    public void printRecSubtotalAdjustVoid(int adjustmentType, long amount) throws JposException {
        logPreCall("PrintRecSubtotalAdjustVoid", removeOuterArraySpecifier(new Object[]{adjustmentType, amount}, Device.MaxArrayStringElements));
        long[] allowed = { FPTR_RT_SALES, FPTR_RT_SERVICE, FPTR_RT_SIMPLE_INVOICE, FPTR_RT_REFUND };
        long[] allowedamount = { FPTR_AT_AMOUNT_DISCOUNT, FPTR_AT_AMOUNT_SURCHARGE };
        long[] allowedpercent = { FPTR_AT_PERCENTAGE_DISCOUNT, FPTR_AT_PERCENTAGE_SURCHARGE };
        long[] allowedpositive = { FPTR_AT_AMOUNT_SURCHARGE, FPTR_AT_PERCENTAGE_SURCHARGE };
        checkEnabled();
        checkext(Data.PrinterState != FPTR_PS_FISCAL_RECEIPT, JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt state");
        checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        check(!member(adjustmentType, allowedamount) && !member(adjustmentType, allowedpercent), JposConst.JPOS_E_ILLEGAL, "Invalid adjustment type: " + adjustmentType);
        check(!Data.CapSubAmountAdjustment && member(adjustmentType, allowedamount), JposConst.JPOS_E_ILLEGAL, "Unsupported adjustment: " + adjustmentType);
        check(!Data.CapSubPercentAdjustment && member(adjustmentType, allowedpercent), JposConst.JPOS_E_ILLEGAL, "Unsupported adjustment: " + adjustmentType);
        check(!Data.CapPositiveSubtotalAdjustment && member(adjustmentType, allowedpositive), JposConst.JPOS_E_ILLEGAL, "Unsupported adjustment: " + adjustmentType);
        checkext(amount <= 0, JPOS_EFPTR_BAD_ITEM_AMOUNT, "amount must be > 0");
        callIt(FiscalPrinterInterface.printRecSubtotalAdjustVoid(adjustmentType, amount), "PrintRecSubtotalAdjustVoid");
    }

    @Override
    public void printRecTaxID(String taxId) throws JposException {
        logPreCall("PrintRecTaxID", removeOuterArraySpecifier(new Object[]{taxId}, Device.MaxArrayStringElements));
        long[] allowed = { FPTR_RT_SALES, FPTR_RT_SERVICE, FPTR_RT_SIMPLE_INVOICE, FPTR_RT_REFUND };
        check(taxId == null, JposConst.JPOS_E_ILLEGAL, "Tax-ID must not be null");
        checkEnabled();
        checkext(Data.PrinterState != FPTR_PS_FISCAL_RECEIPT_ENDING, JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt ending state");
        checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(taxId, "taxId");
        callIt(FiscalPrinterInterface.printRecTaxID(taxId), "PrintRecTaxID");
    }

    @Override
    public void printRecTotal(long total, long payment, String description) throws JposException {
        logPreCall("PrintRecTotal", removeOuterArraySpecifier(new Object[]{total, payment, description}, Device.MaxArrayStringElements));
        long[] allowed = { FPTR_RT_SALES, FPTR_RT_SERVICE, FPTR_RT_SIMPLE_INVOICE, FPTR_RT_REFUND };
        long[] allowedstate = { FPTR_PS_FISCAL_RECEIPT, FPTR_PS_FISCAL_RECEIPT_TOTAL };
        check(description == null, JposConst.JPOS_E_ILLEGAL, "Tax-ID must not be null");
        checkEnabled();
        checkext(!member(Data.PrinterState, allowedstate), JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt or fiscal receipt total ending state");
        checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        check(total < 0, JposConst.JPOS_E_ILLEGAL, "Total < 0");
        check(payment < 0, JposConst.JPOS_E_ILLEGAL, "Payment < 0");
        if (Data.CapPredefinedPaymentLines) {
            String[] payments = Data.PredefinedPaymentLines.split(",");
            boolean found = false;
            for (String placeholder : payments) {
                if (placeholder.equals(description)) {
                    found = true;
                    break;
                }
            }
            check(!found, JposConst.JPOS_E_ILLEGAL, "Invalid payment ID: " + description);
        }
        else
            checkReserved(description, "description");
        callIt(FiscalPrinterInterface.printRecTotal(total, payment, description), "PrintRecTotal");
    }

    @Override
    public void printRecVoid(String description) throws JposException {
        logPreCall("PrintRecVoid", removeOuterArraySpecifier(new Object[]{description}, Device.MaxArrayStringElements));
        long[] allowed = { FPTR_RT_SALES, FPTR_RT_SERVICE, FPTR_RT_SIMPLE_INVOICE, FPTR_RT_REFUND };
        long[] allowedstate = { FPTR_PS_FISCAL_RECEIPT, FPTR_PS_FISCAL_RECEIPT_TOTAL };
        check(description == null, JposConst.JPOS_E_ILLEGAL, "Tax-ID must not be null");
        checkEnabled();
        checkext(!member(Data.PrinterState, allowedstate), JPOS_EFPTR_WRONG_STATE, "Not in fiscal receipt or fiscal receipt total state");
        checkMember(Data.FiscalReceiptType, allowed, JposConst.JPOS_E_ILLEGAL, "Not a sale receipt");
        ifSyncCheckBusyCoverPaper(getFiscalStation());
        checkReserved(description, "description");
        callIt(FiscalPrinterInterface.printRecVoid(description), "PrintRecVoid");
    }
}
