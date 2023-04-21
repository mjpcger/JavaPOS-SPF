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

/**
 * Class containing the FiscalPrinter specific properties, their default values and default implementations of
 * FiscalPrinterInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Fiscal Printer.
 */
public class FiscalPrinterProperties extends JposCommonProperties implements FiscalPrinterInterface {
    /**
     * Object to be used to wait until all asynchronous commands derived from OutputRequest
     * have been finished.
     */
    public SyncObject IdleWaiter = null;
    /**
     * Default value of ActualCurrency property. Default: AC_OTHER. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method and after a successful call to SetCurrency.
     */
    public int ActualCurrencyDef = FiscalPrinterConst.FPTR_AC_OTHER;

    /**
     * UPOS property AdditionalHeader.
     */
    public int ActualCurrency;

    /**
     * UPOS property AdditionalHeader.
     */
    public String AdditionalHeader;

    /**
     * UPOS property AdditionalTrailer.
     */
    public String AdditionalTrailer;

    /**
     * Default value of AmountDecimalPlaces property. Default: 2. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int AmountDecimalPlacesDef = 2;

    /**
     * UPOS property AmountDecimalPlaces.
     */
    public int AmountDecimalPlaces;

    /**
     * UPOS property CapAdditionalHeader. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapAdditionalHeader = false;

    /**
     * UPOS property CapAdditionalLines. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapAdditionalLines = false;

    /**
     * UPOS property CapAdditionalTrailer. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapAdditionalTrailer = false;

    /**
     * UPOS property CapAmountAdjustment. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapAmountAdjustment = false;

    /**
     * UPOS property CapAmountNotPaid. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapAmountNotPaid = false;

    /**
     * UPOS property CapChangeDue. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapChangeDue = false;

    /**
     * UPOS property CapCheckTotal. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapCheckTotal = false;

    /**
     * UPOS property CapCoverSensor. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapCoverSensor = false;

    /**
     * UPOS property CapDoubleWidth. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapDoubleWidth = false;

    /**
     * UPOS property CapDuplicateReceipt. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapDuplicateReceipt = false;

    /**
     * UPOS property CapEmptyReceiptIsVoidable. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapEmptyReceiptIsVoidable = false;

    /**
     * UPOS property CapFiscalReceiptStation. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapFiscalReceiptStation = false;

    /**
     * UPOS property CapFiscalReceiptType. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapFiscalReceiptType = false;

    /**
     * UPOS property CapFixedOutput. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapFixedOutput = false;

    /**
     * UPOS property CapHasVatTable. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapHasVatTable = false;

    /**
     * UPOS property CapIndependentHeader. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapIndependentHeader = false;

    /**
     * UPOS property CapItemList. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapItemList = false;

    /**
     * UPOS property CapJrnEmptySensor. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapJrnEmptySensor = false;

    /**
     * UPOS property CapJrnNearEndSensor. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapJrnNearEndSensor = false;

    /**
     * UPOS property CapJrnPresent. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapJrnPresent = false;

    /**
     * UPOS property CapMultiContractor. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapMultiContractor = false;

    /**
     * UPOS property CapNonFiscalMode. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapNonFiscalMode = false;

    /**
     * UPOS property CapOnlyVoidLastItem. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapOnlyVoidLastItem = false;

    /**
     * UPOS property CapOrderAdjustmentFirst. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapOrderAdjustmentFirst = false;

    /**
     * UPOS property CapPackageAdjustment. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapPackageAdjustment = false;

    /**
     * UPOS property CapPercentAdjustment. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapPercentAdjustment = false;

    /**
     * UPOS property CapPositiveAdjustment. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapPositiveAdjustment = false;

    /**
     * UPOS property CapPositiveSubtotalAdjustment. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapPositiveSubtotalAdjustment = false;

    /**
     * UPOS property CapPostPreLine. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapPostPreLine = false;

    /**
     * UPOS property CapPowerLossReport. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapPowerLossReport = false;

    /**
     * UPOS property CapPredefinedPaymentLines. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapPredefinedPaymentLines = false;

    /**
     * UPOS property CapReceiptNotPaid. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapReceiptNotPaid = false;

    /**
     * UPOS property CapRecEmptySensor. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRecEmptySensor = false;

    /**
     * UPOS property CapRecNearEndSensor. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRecNearEndSensor = false;

    /**
     * UPOS property CapRecPresent. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRecPresent = false;

    /**
     * UPOS property CapRemainingFiscalMemory. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRemainingFiscalMemory = false;

    /**
     * UPOS property CapReservedWord. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapReservedWord = false;

    /**
     * UPOS property CapSetCurrency. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSetCurrency = false;

    /**
     * UPOS property CapSetHeader. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSetHeader = false;

    /**
     * UPOS property CapSetPOSID. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSetPOSID = false;

    /**
     * UPOS property CapSetStoreFiscalID. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSetStoreFiscalID = false;

    /**
     * UPOS property CapSetTrailer. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSetTrailer = false;

    /**
     * UPOS property CapSetVatTable. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSetVatTable = false;

    /**
     * UPOS property CapSlpEmptySensor. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSlpEmptySensor = false;

    /**
     * UPOS property CapSlpFiscalDocument. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSlpFiscalDocument = false;

    /**
     * UPOS property CapSlpFullSlip. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSlpFullSlip = false;

    /**
     * UPOS property CapSlpNearEndSensor. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSlpNearEndSensor = false;

    /**
     * UPOS property CapSlpPresent. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSlpPresent = false;

    /**
     * UPOS property CapSlpValidation. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSlpValidation = false;

    /**
     * UPOS property CapSubAmountAdjustment. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSubAmountAdjustment = false;

    /**
     * UPOS property CapSubPercentAdjustment. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSubPercentAdjustment = false;

    /**
     * UPOS property CapSubtotal. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSubtotal = false;

    /**
     * UPOS property CapTotalizerType. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapTotalizerType = false;

    /**
     * UPOS property CapTrainingMode. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapTrainingMode = false;

    /**
     * UPOS property CapValidateJournal. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapValidateJournal = false;

    /**
     * UPOS property CapXReport. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapXReport = false;

    /**
     * UPOS property ChangeDue.
     */
    public String ChangeDue;

    /**
     * UPOS property CheckTotal.
     */
    public boolean CheckTotal;

    /**
     * UPOS property ContractorId.
     */
    public int ContractorId;

    /**
     * Default value of CountryCode property. Default: CC_OTHER. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CountryCodeDef = FiscalPrinterConst.FPTR_CC_OTHER;

    /**
     * UPOS property CountryCode.
     */
    public int CountryCode;

    /**
     * Default value of CoverOpen property. Default: false. Should be overwritten
     * by objects derived from JposDevice after each status change.
     */
    public boolean CoverOpenDef = false;

    /**
     * UPOS property CoverOpen.
     */
    public boolean CoverOpen;

    /**
     * UPOS property DateType.
     */
    public int DateType;

    /**
     * Default value of DayOpened property. Default: false. Should be overwritten
     * by objects derived from JposDevice after Z report and after first fiscal receipt after the last Z report
     */
    public boolean DayOpenedDef = false;

    /**
     * UPOS property DayOpened.
     */
    public boolean DayOpened;

    /**
     * UPOS property DescriptionLength. Default: 20. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int DescriptionLength = 20;

    /**
     * UPOS property DuplicateReceipt.
     */
    public boolean DuplicateReceipt;

    /**
     * UPOS property ErrorLevel.
     */
    public int ErrorLevel;

    /**
     * UPOS property ErrorOutID.
     */
    public int ErrorOutID;

    /**
     * UPOS property ErrorState.
     */
    public int ErrorState;

    /**
     * UPOS property ErrorStation.
     */
    public int ErrorStation;

    /**
     * UPOS property ErrorString.
     */
    public String ErrorString;

    /**
     * UPOS property FiscalReceiptStation.
     */
    public int FiscalReceiptStation;

    /**
     * UPOS property FiscalReceiptType.
     */
    public int FiscalReceiptType;

    /**
     * Default value of JrnEmpty property. Default: false. Should be overwritten
     * by objects derived from JposDevice after each status change.
     */
    public boolean JrnEmptyDef = false;

    /**
     * UPOS property JrnEmpty.
     */
    public boolean JrnEmpty;

    /**
     * Default value of JrnNearEnd property. Default: false. Should be overwritten
     * by objects derived from JposDevice after each status change.
     */
    public boolean JrnNearEndDef = false;

    /**
     * UPOS property JrnNearEnd.
     */
    public boolean JrnNearEnd;

    /**
     * Default value of MessageLength property. Default: 20. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method and after any change due to printer mode change.
     */
    public int MessageLengthDef = 20;

    /**
     * UPOS property MessageLength.
     */
    public int MessageLength;

    /**
     * UPOS property MessageType.
     */
    public int MessageType;

    /**
     * UPOS property NumHeaderLines. Default: 0. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int NumHeaderLines = 0;

    /**
     * UPOS property NumTrailerLines. Default: 0. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int NumTrailerLines = 0;

    /**
     * UPOS property NumVatRates. Default: 0. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int NumVatRates = 0;

    /**
     * UPOS property PostLine.
     */
    public String PostLine;

    /**
     * UPOS property PredefinedPaymentLines. Default: "". Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method if predefined payment lines are supported.
     */
    public String PredefinedPaymentLines = "";

    /**
     * UPOS property PreLine.
     */
    public String PreLine;

    /**
     * Default value of PrinterState property. Default: PS_MONITOR. Should be overwritten
     * by objects derived from JposDevice whenever the printer state has changed.
     */
    public int PrinterStateDef = FiscalPrinterConst.FPTR_PS_MONITOR;

    /**
     * UPOS property PrinterState.
     */
    public int PrinterState;
    /**
     * Default value of QuantityDecimalPlaces property. Default: 0. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int QuantityDecimalPlacesDef = 0;

    /**
     * UPOS property QuantityDecimalPlaces.
     */
    public int QuantityDecimalPlaces;

    /**
     * Default value of QuantityLength property. Default: 1. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int QuantityLengthDef = 1;

    /**
     * UPOS property QuantityLength.
     */
    public int QuantityLength;

    /**
     * Default value of RecEmpty property. Default: false. Should be overwritten
     * by objects derived from JposDevice whenever the status changes.
     */
    public boolean RecEmptyDef = false;

    /**
     * UPOS property RecEmpty.
     */
    public boolean RecEmpty;

    /**
     * Default value of RecNearEnd property. Default: false. Should be overwritten
     * by objects derived from JposDevice whenever the status changes.
     */
    public boolean RecNearEndDef = false;

    /**
     * UPOS property RecNearEnd.
     */
    public boolean RecNearEnd;

    /**
     * Default value of RemainingFiscalMemory property. Default: 0. Should be overwritten
     * by objects derived from JposDevice during device open and after each Z report.
     */
    public int RemainingFiscalMemoryDef = 0;

    /**
     * UPOS property RemainingFiscalMemory.
     */
    public int RemainingFiscalMemory;

    /**
     * UPOS property ReservedWord. Default: "". Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method if CapReservedWord is true.
     */
    public String ReservedWord = "";

    /**
     * Default value of SlpEmpty property. Default: false. Should be overwritten
     * by objects derived from JposDevice whenever the status changes.
     */
    public boolean SlpEmptyDef = false;

    /**
     * UPOS property SlpEmpty.
     */
    public boolean SlpEmpty;

    /**
     * Default value of SlpNearEnd property. Default: false. Should be overwritten
     * by objects derived from JposDevice whenever the status changes.
     */
    public boolean SlpNearEndDef = false;

    /**
     * UPOS property SlpNearEnd.
     */
    public boolean SlpNearEnd;

    /**
     * UPOS property SlipSelection.
     */
    public int SlipSelection;

    /**
     * UPOS property TotalizerType.
     */
    public int TotalizerType;

    /**
     * Default value of TrainingModeActive property. Default: false. Should be overwritten
     * by objects derived from JposDevice before first enable and after method calls BeginTraining and EndTraining.
     */
    public boolean TrainingModeActiveDef = false;

    /**
     * UPOS property TrainingModeActive.
     */
    public boolean TrainingModeActive;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    public FiscalPrinterProperties(int dev) {
        super(dev);
        DeviceServiceVersion = 1015000;
        FlagWhenIdleStatusValue = FiscalPrinterConst.FPTR_SUE_IDLE;
    }

    @Override
    public void initOnOpen() {
        super.initOnOpen();
        ChangeDue = "";
        CheckTotal = true;
        DuplicateReceipt = false;
        ErrorLevel = FiscalPrinterConst.FPTR_EL_NONE;
        ErrorOutID = 0;
        ErrorState = 0;
        ErrorStation = 0;
        ErrorString = "";
        MessageType = FiscalPrinterConst.FPTR_MT_FREE_TEXT;
        // Next line: Must be initialized here because relevant for ErrorStation
        FiscalReceiptStation = FiscalPrinterConst.FPTR_RS_RECEIPT;
    }

    @Override
    public boolean initOnFirstEnable() {
        if (!super.initOnFirstEnable()) {
            CountryCode = CountryCodeDef;
            PrinterState = PrinterStateDef;
            PostLine = "";
            PreLine = "";
            QuantityDecimalPlaces = QuantityDecimalPlacesDef;
            QuantityLength = QuantityLengthDef;
            TotalizerType = FiscalPrinterConst.FPTR_TT_DAY;
            TrainingModeActive = TrainingModeActiveDef;
            return false;
        }
        return true;
    }

    @Override
    public void initOnClaim() {
        SlipSelection = FiscalPrinterConst.FPTR_SS_FULL_LENGTH;
    }

    @Override
    public void initOnEnable(boolean enable) {
        super.initOnEnable(enable);
        if (enable) {
            ActualCurrency = ActualCurrencyDef;
            AdditionalHeader = "";
            AdditionalTrailer = "";
            AmountDecimalPlaces = AmountDecimalPlacesDef;
            ContractorId = FiscalPrinterConst.FPTR_CID_SINGLE;
            CoverOpen = CoverOpenDef;
            DateType = FiscalPrinterConst.FPTR_DT_RTC;
            DayOpened = DayOpenedDef;
            FiscalReceiptType = FiscalPrinterConst.FPTR_RT_SALES;
            JrnEmpty = JrnEmptyDef;
            JrnNearEnd = JrnNearEndDef;
            MessageLength = MessageLengthDef;
            RecEmpty = RecEmptyDef;
            RecNearEnd = RecNearEndDef;
            RemainingFiscalMemory = RemainingFiscalMemoryDef;
            SlpEmpty = SlpEmptyDef;
            SlpNearEnd = SlpNearEndDef;
        }
    }

    /*
     * Implementation of Interface functions
     */

    @Override
    public void clearOutput() throws JposException {
        super.clearOutput();
        synchronized(Device.AsyncProcessorRunning) {
            SyncObject obj = IdleWaiter;
            if (obj != null) {
                IdleWaiter = null;
                obj.signal();
            }
        }
    }

    @Override
    public void additionalHeader(String header) throws JposException {
        AdditionalHeader = header;
    }

    @Override
    public void additionalTrailer(String trailer) throws JposException {
        AdditionalTrailer = trailer;
    }

    @Override
    public void changeDue(String changeDue) throws JposException {
        ChangeDue = changeDue;
    }

    @Override
    public void checkTotal(boolean check) throws JposException {
        CheckTotal = check;
    }

    @Override
    public void contractorId(int id) throws JposException {
        ContractorId = id;
    }

    @Override
    public void dateType(int type) throws JposException {
        DateType = type;
    }

    @Override
    public void duplicateReceipt(boolean yes) throws JposException {
        DuplicateReceipt = yes;
    }

    @Override
    public void fiscalReceiptStation(int station) throws JposException {
        FiscalReceiptStation = station;
    }

    @Override
    public void fiscalReceiptType(int type) throws JposException {
        FiscalReceiptType = type;
    }

    @Override
    public void messageType(int type) throws JposException {
        MessageType = type;
    }

    @Override
    public void postLine(String text) throws JposException {
        PostLine = text;
    }

    @Override
    public void preLine(String text) throws JposException {
        PreLine = text;
    }

    @Override
    public void slipSelection(int type) throws JposException {
        SlipSelection = type;
    }

    @Override
    public void totalizerType(int type) throws JposException {
        TotalizerType = type;
    }

    @Override
    public void beginFiscalDocument(int documentAmount) throws JposException {
        DayOpened = true;
        EventSource.logSet("DayOpened");
        PrinterState = FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT;
        EventSource.logSet("PrinterState");
    }

    @Override
    public void beginFiscalReceipt(boolean printHeader) throws JposException {
        DayOpened = true;
        EventSource.logSet("DayOpened");
        PrinterState = FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT;
        EventSource.logSet("PrinterState");
    }

    @Override
    public void beginFixedOutput(int station, int documentType) throws JposException {
        PrinterState = FiscalPrinterConst.FPTR_PS_FIXED_OUTPUT;
        EventSource.logSet("PrinterState");
    }

    @Override
    public void beginInsertion(int timeout) throws JposException {
    }

    @Override
    public void beginItemList(int vatID) throws JposException {
        PrinterState = FiscalPrinterConst.FPTR_PS_ITEM_LIST;
        EventSource.logSet("PrinterState");
    }

    @Override
    public void beginNonFiscal() throws JposException {
        PrinterState = FiscalPrinterConst.FPTR_PS_NONFISCAL;
        EventSource.logSet("PrinterState");
    }

    @Override
    public void beginRemoval(int timeout) throws JposException {
    }

    @Override
    public void beginTraining() throws JposException {
        TrainingModeActive = true;
        EventSource.logSet("TrainingModeActive");
    }

    @Override
    public void clearError() throws JposException {
    }

    @Override
    public void endFiscalDocument() throws JposException {
        PrinterState = FiscalPrinterConst.FPTR_PS_MONITOR;
        EventSource.logSet("PrinterState");
    }

    @Override
    public void endFiscalReceipt(boolean printHeader) throws JposException {
        PrinterState = FiscalPrinterConst.FPTR_PS_MONITOR;
        EventSource.logSet("PrinterState");
        if (DuplicateReceipt) {
            DuplicateReceipt = false;
            EventSource.logSet("DuplicateReceipt");
        }
    }

    @Override
    public void endFixedOutput() throws JposException {
        PrinterState = FiscalPrinterConst.FPTR_PS_MONITOR;
        EventSource.logSet("PrinterState");
    }

    @Override
    public void endInsertion() throws JposException {
    }

    @Override
    public void endItemList() throws JposException {
        PrinterState = FiscalPrinterConst.FPTR_PS_MONITOR;
        EventSource.logSet("PrinterState");
    }

    @Override
    public void endNonFiscal() throws JposException {
        PrinterState = FiscalPrinterConst.FPTR_PS_MONITOR;
        EventSource.logSet("PrinterState");
    }

    @Override
    public void endRemoval() throws JposException {
    }

    @Override
    public void endTraining() throws JposException {
        TrainingModeActive = false;
        EventSource.logSet("TrainingModeActive");
    }

    @Override
    public void getData(int dataItem, int[] optArgs, String[] data) throws JposException {
    }

    @Override
    public void getDate(String[] date) throws JposException {
    }

    @Override
    public void getTotalizer(int vatID, int optArgs, String[] data) throws JposException {
    }

    @Override
    public void getVatEntry(int vatID, int optArgs, int[] vatRate) throws JposException {
    }

    @Override
    public void printDuplicateReceipt() throws JposException {
        if (DuplicateReceipt) {
            DuplicateReceipt = false;
            EventSource.logSet("DuplicateReceipt");
        }
    }

    @Override
    public void printPeriodicTotalsReport(String date1, String date2) throws JposException {
    }

    @Override
    public void printPowerLossReport() throws JposException {
    }

    @Override
    public void printReport(int reportType, String startNum, String endNum) throws JposException {
    }

    @Override
    public void printXReport() throws JposException {
    }

    @Override
    public void printZReport() throws JposException {
        if (DayOpened) {
            DayOpened = false;
            EventSource.logSet("DayOpened");
        }
    }

    @Override
    public void resetPrinter() throws JposException {
        if (PrinterState != FiscalPrinterConst.FPTR_PS_MONITOR) {
            PrinterState = FiscalPrinterConst.FPTR_PS_MONITOR;
            EventSource.logSet("PrinterState");
        }
        if (TrainingModeActive) {
            TrainingModeActive = false;
            EventSource.logSet("TrainingModeActive");
        }
        if (DuplicateReceipt) {
            DuplicateReceipt = false;
            EventSource.logSet("DuplicateReceipt");
        }
    }

    @Override
    public void setCurrency(int newCurrency) throws JposException {
        int[][] valuelinktable = new int[][]{
                new int[]{  // Valid values for newCurrency and corresponding value for property ActualCurrency.
                        FiscalPrinterConst.FPTR_SC_EURO, FiscalPrinterConst.FPTR_AC_EUR
                }
        };
        for (int[] valpair : valuelinktable) {
            if (newCurrency == valpair[0]) {
                if (ActualCurrency != valpair[1]) {
                    ActualCurrency = valpair[1];
                    EventSource.logSet("ActualCurrency");
                }
                break;
            }
        }
    }

    @Override
    public void setDate(String date) throws JposException {
    }

    @Override
    public void setHeaderLine(int lineNumber, String text, boolean doubleWidth) throws JposException {
    }

    @Override
    public void setPOSID(String POSID, String cashierID) throws JposException {
    }

    @Override
    public void setStoreFiscalID(String ID) throws JposException {
    }

    @Override
    public void setTrailerLine(int lineNumber, String text, boolean doubleWidth) throws JposException {
    }

    @Override
    public void setVatTable() throws JposException {
    }

    @Override
    public void setVatValue(int vatID, long vatValue) throws JposException {
    }

    @Override
    public void verifyItem(String itemName, int vatID) throws JposException {
    }

    @Override
    public PrintFiscalDocumentLine printFiscalDocumentLine(String documentLine) throws JposException {
        return new PrintFiscalDocumentLine(this, documentLine);
    }

    @Override
    public void printFiscalDocumentLine(PrintFiscalDocumentLine request) throws JposException {
    }

    @Override
    public PrintFixedOutput printFixedOutput(int documentType, int lineNumber, String data) throws JposException {
        return new PrintFixedOutput(this, documentType, lineNumber, data);
    }

    @Override
    public void printFixedOutput(PrintFixedOutput request) throws JposException {
    }

    @Override
    public PrintNormal printNormal(int station, String data) throws JposException {
        return new PrintNormal(this, station, data);
    }

    @Override
    public void printNormal(PrintNormal request) throws JposException {
    }

    @Override
    public PrintRecCash printRecCash(long amount) throws JposException {
        return new PrintRecCash(this, amount);
    }

    @Override
    public void printRecCash(PrintRecCash request) throws JposException {
    }

    @Override
    public PrintRecItem printRecItem(String description, long price, int quantity, int vatInfo, long unitPrice, String unitName) throws JposException {
        PrintRecItem request = new PrintRecItem(this, description, price, quantity, vatInfo, unitPrice, unitName);
        PostLine = PreLine = "";
        return request;
    }

    @Override
    public void printRecItem(PrintRecItem request) throws JposException {
    }

    @Override
    public PrintRecItemAdjustment printRecItemAdjustment(int adjustmentType, String description, long amount, int vatInfo) throws JposException {
        PrintRecItemAdjustment request = new PrintRecItemAdjustment(this, adjustmentType, description, amount, vatInfo);
        PreLine = "";
        return request;
    }

    @Override
    public void printRecItemAdjustment(PrintRecItemAdjustment request) throws JposException {
    }

    @Override
    public PrintRecItemAdjustmentVoid printRecItemAdjustmentVoid(int adjustmentType, String description, long amount, int vatInfo) throws JposException {
        PrintRecItemAdjustmentVoid request = new PrintRecItemAdjustmentVoid(this, adjustmentType, description, amount, vatInfo);
        PreLine = "";
        return request;
    }

    @Override
    public void printRecItemAdjustmentVoid(PrintRecItemAdjustmentVoid request) throws JposException {
    }

    @Override
    public PrintRecItemFuel printRecItemFuel(String description, long price, int quantity, int vatInfo, long unitPrice, String unitName, long specialTax, String specialTaxName) throws JposException {
        return new PrintRecItemFuel(this, description, price, quantity, vatInfo, unitPrice, unitName, specialTax, specialTaxName);
    }

    @Override
    public void printRecItemFuel(PrintRecItemFuel request) throws JposException {
    }

    @Override
    public PrintRecItemFuelVoid printRecItemFuelVoid(String description, long price, int vatInfo, long specialTax) throws JposException {
        return new PrintRecItemFuelVoid(this, description, price, vatInfo, specialTax);
    }

    @Override
    public void printRecItemFuelVoid(PrintRecItemFuelVoid request) throws JposException {
    }

    @Override
    public PrintRecItemVoid printRecItemVoid(String description, long price, int quantity, int vatInfo, long unitPrice, String unitName) throws JposException {
        PrintRecItemVoid request = new PrintRecItemVoid(this, description, price, quantity, vatInfo, unitPrice, unitName);
        PreLine = PostLine = "";
        return request;
    }

    @Override
    public void printRecItemVoid(PrintRecItemVoid request) throws JposException {
    }

    @Override
    public PrintRecItemRefund printRecItemRefund(String description, long amount, int quantity, int vatInfo, long unitAmount, String unitName) throws JposException {
        PrintRecItemRefund request = new PrintRecItemRefund(this, description, amount, quantity, vatInfo, unitAmount, unitName);
        PreLine = "";
        return request;
    }

    @Override
    public void printRecItemRefund(PrintRecItemRefund request) throws JposException {
    }

    @Override
    public PrintRecItemRefundVoid printRecItemRefundVoid(String description, long amount, int quantity, int vatInfo, long unitAmount, String unitName) throws JposException {
        return new PrintRecItemRefundVoid(this, description, amount, quantity, vatInfo, unitAmount, unitName);
    }

    @Override
    public void printRecItemRefundVoid(PrintRecItemRefundVoid request) throws JposException {
    }

    @Override
    public PrintRecMessage printRecMessage(String message) throws JposException {
        return new PrintRecMessage(this, message);
    }

    @Override
    public void printRecMessage(PrintRecMessage request) throws JposException {
    }

    @Override
    public PrintRecNotPaid printRecNotPaid(String description, long amount) throws JposException {
        return new PrintRecNotPaid(this, description, amount);
    }

    @Override
    public void printRecNotPaid(PrintRecNotPaid request) throws JposException {
        if (PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT_ENDING) {
            PrinterState = FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT_ENDING;
            EventSource.logSet("PrinterState");
        }
    }

    @Override
    public PrintRecPackageAdjustment printRecPackageAdjustment(int adjustmentType, String description, String vatAdjustment) throws JposException {
        PrintRecPackageAdjustment request = new PrintRecPackageAdjustment(this, adjustmentType, description, vatAdjustment);
        PreLine = "";
        return request;
    }

    @Override
    public void printRecPackageAdjustment(PrintRecPackageAdjustment request) throws JposException {
    }

    @Override
    public PrintRecPackageAdjustVoid printRecPackageAdjustVoid(int adjustmentType, String vatAdjustment) throws JposException {
        PrintRecPackageAdjustVoid request = new PrintRecPackageAdjustVoid(this, adjustmentType, vatAdjustment);
        PreLine = "";
        return request;
    }

    @Override
    public void printRecPackageAdjustVoid(PrintRecPackageAdjustVoid request) throws JposException {
    }

    @Override
    public PrintRecRefund printRecRefund(String description, long amount, int vatInfo) throws JposException {
        PrintRecRefund request = new PrintRecRefund(this, description, amount, vatInfo);
        PreLine = "";
        return null;
    }

    @Override
    public void printRecRefund(PrintRecRefund request) throws JposException {
    }

    @Override
    public PrintRecRefundVoid printRecRefundVoid(String description, long amount, int vatInfo) throws JposException {
        return new PrintRecRefundVoid(this, description, amount, vatInfo);
    }

    @Override
    public void printRecRefundVoid(PrintRecRefundVoid request) throws JposException {
    }

    @Override
    public PrintRecSubtotal printRecSubtotal(long amount) throws JposException {
        PrintRecSubtotal request = new PrintRecSubtotal(this, amount);
        PostLine = "";
        return request;
    }

    @Override
    public void printRecSubtotal(PrintRecSubtotal request) throws JposException {
    }

    @Override
    public PrintRecSubtotalAdjustment printRecSubtotalAdjustment(int adjustmentType, String description, long amount) throws JposException {
        PrintRecSubtotalAdjustment request = new PrintRecSubtotalAdjustment(this, adjustmentType, description, amount);
        PreLine = "";
        return request;
    }

    @Override
    public void printRecSubtotalAdjustment(PrintRecSubtotalAdjustment request) throws JposException {
    }

    @Override
    public PrintRecSubtotalAdjustVoid printRecSubtotalAdjustVoid(int adjustmentType, long amount) throws JposException {
        PrintRecSubtotalAdjustVoid request = new PrintRecSubtotalAdjustVoid(this, adjustmentType, amount);
        PreLine = "";
        return request;
    }

    @Override
    public void printRecSubtotalAdjustVoid(PrintRecSubtotalAdjustVoid request) throws JposException {
    }

    @Override
    public PrintRecTaxID printRecTaxID(String taxId) throws JposException {
        return new PrintRecTaxID(this, taxId);
    }

    @Override
    public void printRecTaxID(PrintRecTaxID request) throws JposException {
    }

    @Override
    public PrintRecTotal printRecTotal(long total, long payment, String description) throws JposException {
        PrintRecTotal request = new PrintRecTotal(this, total, payment, description);
        PostLine = "";
        return request;
    }

    @Override
    public void printRecTotal(PrintRecTotal request) throws JposException {
    }

    @Override
    public PrintRecVoid printRecVoid(String description) throws JposException {
        return new PrintRecVoid(this, description);
    }

    @Override
    public void printRecVoid(PrintRecVoid request) throws JposException {
        if (PrinterState != FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT_ENDING) {
            PrinterState = FiscalPrinterConst.FPTR_PS_FISCAL_RECEIPT_ENDING;
            EventSource.logSet("PrinterState");
        }
    }

    @Override
    public PrintRecVoidItem printRecVoidItem(String description, long price, int quantity, int adjustmentType, long adjustment, int vatInfo) throws JposException {
        return new PrintRecVoidItem(this, description, price, quantity, adjustmentType, adjustment, vatInfo);
    }

    @Override
    public void printRecVoidItem(PrintRecVoidItem request) throws JposException {
    }
}
