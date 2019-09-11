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
 * Interface for methods that implement property setter and method calls for the FiscalPrinter device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Fiscal Printer.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface FiscalPrinterInterface extends JposBaseInterface {
    /**
     * Final part of setting AdditionalHeader. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapAdditionalHeader is true.</li>
     * </ul>
     *
     * @param header Additional header line to be printer in header of next receipt.
     * @throws JposException If an error occurs.
     */
    public void additionalHeader(String header) throws JposException;

    /**
     * Final part of setting AdditionalTrailer. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapAdditionalTrailer is true.</li>
     * </ul>
     *
     * @param trailer Additional header line to be printer in header of next receipt.
     * @throws JposException If an error occurs.
     */
    public void additionalTrailer(String trailer) throws JposException;

    /**
     * Final part of setting ChangeDue. Must be overwritten within derived classes, if CapChangeDue is true.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is opened,</li>
     *     <li>CapChangeDue is true.</li>
     * </ul>
     * cashreturn must at least be checked for valid length.
     *
     * @param cashreturn Text for cash return.
     * @throws JposException If an error occurs.
     */
    public void changeDue(String cashreturn) throws JposException;

    /**
     * Final part of setting CheckTotal. Can be overwritten within derived classes, if CapCheckTotal is true.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is opened,</li>
     *     <li>CapCheckTotal is true.</li>
     * </ul>
     *
     * @param check true if application and printer total must match, false otherwise.
     * @throws JposException If an error occurs.
     */
    public void checkTotal(boolean check) throws JposException;

    /**
     * Final part of setting ContractorId. Can be overwritten within derived classes, if CapMultiContractor is true.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapMultiContractor is true,</li>
     *     <li>id is one of CID_FIRST, CID_SECOND or CID_SINGLE.</li>
     * </ul>
     *
     * @param id One of CID_FIRST, CID_SECOND or CID_SINGLE.
     * @throws JposException If an error occurs.
     */
    public void contractorId(int id) throws JposException;

    /**
     * Final part of setting DateType.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>type is one of DT_CONF, DT_EOD, DT_RESET, DT_RTC, DT_VAT or DT_START.</li>
     * </ul>
     *
     * @param type One of DT_CONF, DT_EOD, DT_RESET, DT_RTC, DT_VAT or DT_START.
     * @throws JposException If an error occurs.
     */
    public void dateType(int type) throws JposException;

    /**
     * Final part of setting DuplicateReceipt.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>CapDuplicateReceipt is true.</li>
     * </ul>
     *
     * @param yes true if service can store printer commands for generation of fiscal receipt duplication.
     * @throws JposException If an error occurs.
     */
    public void duplicateReceipt(boolean yes) throws JposException;

    /**
     * Final part of setting FiscalReceiptStation.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>CapFiscalReceiptStation is true,</li>
     *     <li>Printer is in monitoring state,</li>
     *     <li>station is one of RS_RECEIPT or RS_SLIP.</li>
     *     <li>if CapSlpPresent is false, station is RS_RECEIPT.</li>
     * </ul>
     *
     * @param station One of RS_RECEIPT or RS_SLIP.
     * @throws JposException If an error occurs.
     */
    public void fiscalReceiptStation(int station) throws JposException;

    /**
     * Final part of setting FiscalReceiptType.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>CapFiscalReceiptType is true,</li>
     *     <li>Printer is in monitoring state,</li>
     *     <li>type is one of RT_CASH_IN, RT_CASH_OUT, RT_GENERIC, RT_SALES, RT_SERVICE, SIMPLE_INVOICE or RT_REFUND.</li>
     * </ul>
     *
     * @param type One of RT_CASH_IN, RT_CASH_OUT, RT_GENERIC, RT_SALES, RT_SERVICE, SIMPLE_INVOICE or RT_REFUND.
     * @throws JposException If an error occurs.
     */
    public void fiscalReceiptType(int type) throws JposException;

    /**
     * Final part of setting MessageType.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>type is one of the values listed in the UPOS specification for property MessageType.</li>
     * </ul>
     *
     * @param type One of the values listed in the UPOS specification for property MessageType.
     * @throws JposException If an error occurs.
     */
    public void messageType(int type) throws JposException;

    /**
     * Final part of setting PostLine.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapPostPreLine is true,</li>
     *     <li>Printer is in fiscal receipt state.</li>
     * </ul>
     *
     * @param text Text to be printed after next item line.
     * @throws JposException If an error occurs.
     */
    public void postLine(String text) throws JposException;

    /**
     * Final part of setting PreLine.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapPostPreLine is true,</li>
     *     <li>Printer is in fiscal receipt state.</li>
     * </ul>
     *
     * @param text Text to be printed before next item line.
     * @throws JposException If an error occurs.
     */
    public void preLine(String text) throws JposException;

    /**
     * Final part of setting SlipSelection.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSlpPresent is true,</li>
     *     <li>type is one of SS_FULL_LENGTH or SS_VALIDATION.</li>
     *     <li>If CapSlpValidation is false, type is SS_FULL_LENGTH.</li>
     * </ul>
     *
     * @param type One of SS_FULL_LENGTH or SS_VALIDATION.
     * @throws JposException If an error occurs.
     */
    public void slipSelection(int type) throws JposException;

    /**
     * Final part of setting TotalizerType.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapTotalizerType is true,</li>
     *     <li>type is one of TT_DOCUMENT, TT_DAY, TT_RECEIPT or TT_GRAND.</li>
     * </ul>
     *
     * @param type One of TT_DOCUMENT, TT_DAY, TT_RECEIPT or TT_GRAND.
     * @throws JposException If an error occurs.
     */
    public void totalizerType(int type) throws JposException;

    /**
     * Final part of BeginFiscalDocument method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSlpPresent as well as CapSlpFiscalDocument is true,</li>
     *     <li>PrinterState is PS_MONITOR (not in any other kind of document).</li>
     * </ul>
     * The default implementation should be called within derived methods to ensure that the properties
     * DayOpened and PrinterState are updated as expected.
     *
     * @param documentAmount    Amount of document to be stored by the Fiscal Printer.
     * @throws JposException    If an error occurs.
     */
    public void beginFiscalDocument(int documentAmount) throws JposException;

    /**
     * Final part of BeginFiscalReceipt method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PrinterState is PS_MONITOR (not in any other kind of document).</li>
     * </ul>
     * The default implementation should be called within derived methods to ensure that the properties
     * DayOpened and PrinterState are updated as expected.
     *
     * @param printHeader       Indicates if the header lines are to be printed at this time.
     * @throws JposException    If an error occurs.
     */
    public void beginFiscalReceipt(boolean printHeader) throws JposException;

    /**
     * Final part of BeginFixedOutput method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapFixedOutput is true,</li>
     *     <li>PrinterState is PS_MONITOR (not in any other kind of document),</li>
     *     <li>station is one of S_RECEIPT or S_SLIP,</li>
     *     <li>the corresponding station is present.</li>
     * </ul>
     * The default implementation should be called within derived methods to ensure that the property
     * PrinterState is updated as expected.
     *
     * @param station           The Fiscal Printer station to be used.
     * @param documentType      Identifier of a document stored in the Fiscal Printer.
     * @throws JposException    If an error occurs.
     */
    public void beginFixedOutput(int station, int documentType) throws JposException;

    /**
     * Final part of BeginInsertion method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSlpPresent is true,</li>
     *     <li>timeout is &ge; 0 or FOREVER,</li>
     *     <li>PrinterState is not PS_LOCKED.</li>
     * </ul>
     *
     * @param timeout           The timeout parameter gives the number of milliseconds.
     * @throws JposException    If an error occurs.
     */
    public void beginInsertion(int timeout) throws JposException;

    /**
     * Final part of BeginItemList method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapItemList is true,</li>
     *     <li>PrinterState is PS_MONITOR (not in any other kind of document).</li>
     * </ul>
     * vatID will not be checked because it has no predefined allowed value range. Valid values are specified
     * by each service vendor individually. Therefore vatID must be checked within each individual service
     * implementation.
     * <br>The default implementation should be called within derived methods to ensure that the property
     * PrinterState is updated as expected.
     *
     * @param vatID             Vat identifier for reporting.
     * @throws JposException    If an error occurs.
     */
    public void beginItemList(int vatID) throws JposException;

    /**
     * Final part of BeginNonFiscal method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapNonFiscalMode is true,</li>
     *     <li>PrinterState is PS_MONITOR (not in any other kind of document).</li>
     * </ul>
     * The default implementation should be called within derived methods to ensure that the property
     * PrinterState is updated as expected.
     *
     * @throws JposException    If an error occurs.
     */
    public void beginNonFiscal() throws JposException;

    /**
     * Final part of BeginRemoval method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSlpPresent is true,</li>
     *     <li>timeout is &ge; 0 or FOREVER,</li>
     *     <li>PrinterState is not PS_LOCKED.</li>
     * </ul>
     *
     * @param timeout           The timeout parameter gives the number of milliseconds.
     * @throws JposException    If an error occurs.
     */
    public void beginRemoval(int timeout) throws JposException;

    /**
     * Final part of BeginTraining method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapTrainingMode is true,</li>
     *     <li>PrinterState is PS_MONITOR (not in any other kind of document).</li>
     * </ul>
     * The default implementation should be called within derived methods to ensure that the property
     * TrainingModeActive is updated as expected.
     *
     * @throws JposException    If an error occurs.
     */
    public void beginTraining() throws JposException;

    /**
     * Final part of ClearError method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     *
     * @throws JposException    If an error occurs.
     */
    public void clearError() throws JposException;

    /**
     * Final part of EndFiscalDocument method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSlpFiscalDocument is true,</li>
     *     <li>PrinterState is PS_FISCAL_DOCUMENT.</li>
     * </ul>
     * The default implementation should be called within derived methods to ensure that the property
     * PrinterState is updated as expected.
     *
     * @throws JposException    If an error occurs.
     */
    public void endFiscalDocument() throws JposException;

    /**
     * Final part of EndFiscalReceipt method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PrinterState is PS_FISCAL_RECEIPT_ENDING or PS_FISCAL_RECEIPT.</li>
     * </ul>
     * The default implementation can be called within derived methods to ensure that the properties
     * PrinterState and DuplicateReceipt are updated as expected.
     * <br>If called from PS_FISCAL_RECEIPT, this method must throw an exception after the first item has just been
     * sold. In this case, PrintRecVoid must be called explicitly.
     *
     * @param printHeader      Indicates if the header lines of the following receipt are to be printed at this time.
     * @throws JposException   If an error occurs.
     */
    public void endFiscalReceipt(boolean printHeader) throws JposException;

    /**
     * Final part of EndFixedOutput method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapFixedOutput is true,</li>
     *     <li>PrinterState is PS_FIXED_OUTPUT.</li>
     * </ul>
     * The default implementation should be called within derived methods to ensure that the property
     * PrinterState is updated as expected.
     *
     * @throws JposException    If an error occurs.
     */
    public void endFixedOutput() throws JposException;

    /**
     * Final part of EndInsertion method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSlpPresent is true,</li>
     *     <li>PrinterState is not PS_LOCKED.</li>
     * </ul>
     *
     * @throws JposException    If an error occurs.
     */
    public void endInsertion() throws JposException;

    /**
     * Final part of EndItemList method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapItemList is true,</li>
     *     <li>PrinterState is PS_ITEM_LIST.</li>
     * </ul>
     * The default implementation should be called within derived methods to ensure that the property
     * PrinterState is updated as expected.
     *
     * @throws JposException    If an error occurs.
     */
    public void endItemList() throws JposException;

    /**
     * Final part of EndNonFiscal method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapNonFiscalMode is true,</li>
     *     <li>PrinterState is PS_NONFISCAL.</li>
     * </ul>
     * The default implementation should be called within derived methods to ensure that the property
     * PrinterState is updated as expected.
     *
     * @throws JposException    If an error occurs.
     */
    public void endNonFiscal() throws JposException;

    /**
     * Final part of EndRemoval method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSlpPresent is true,</li>
     *     <li>PrinterState is not PS_LOCKED.</li>
     * </ul>
     *
     * @throws JposException    If an error occurs.
     */
    public void endRemoval() throws JposException;

    /**
     * Final part of EndTraining method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapTrainingMode is true,</li>
     *     <li>TrainingModeActive is true.</li>
     * </ul>
     * The default implementation should be called within derived methods to ensure that the property
     * PrinterState is updated as expected.
     *
     * @throws JposException    If an error occurs.
     */
    public void endTraining() throws JposException;

    /**
     * Final part of GetData method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>AsyncMode is true or State is not S_BUSY,</li>
     *     <li>optArgs and data are not null and are of length 1,</li>
     *     <li>dataItem is one of the GD_ values as described for method GetData.</li>
     * </ul>
     *
     * @param  dataItem         The specific data item to retrieve.
     * @param  optArgs          For some dataItem this additional argument is used for further targeting.
     * @param  data             Character string to hold the data retrieved.
     * @throws JposException    If an error occurs.
     */
    public void getData(int dataItem, int[] optArgs, String[] data) throws JposException;

    /**
     * Final part of GetDate method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>date is not null and is of length 1.</li>
     * </ul>
     *
     * @param  date             Date and time returned as a string.
     * @throws JposException    If an error occurs.
     */
    public void getDate(String[] date) throws JposException;

    /**
     * Final part of GetTotalizer method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>data is not null and is of length 1,</li>
     *     <li>optArgs is one of the GT_ values as described for method GetTotalizer.</li>
     * </ul>
     *
     * @param  vatID            VAT identifier of the required totalizer.
     * @param  optArgs          Specifies the required totalizer.
     * @param  data             Totalizer returned as a string.
     * @throws JposException    If an error occurs.
     */
    public void getTotalizer(int vatID, int optArgs, String[] data) throws JposException;

    /**
     * Final part of GetVatEntry method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapHasVatTable is true.</li>
     *     <li>vatRate is not null and is of length 1,</li>
     * </ul>
     *
     * @param  vatID            VAT identifier of the required rate.
     * @param  optArgs          For some countries, this additional argument may be needed.
     * @param  vatRate          The rate associated with the VAT identifier.
     * @throws JposException    If an error occurs.
     */
    public void getVatEntry(int vatID, int optArgs, int[] vatRate) throws JposException;

    /**
     * Final part of PrintDuplicateReceipt method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapDuplicateReceipt is true,</li>
     *     <li>PrinterState is PS_MONITOR,</li>
     *     <li>If AsyncMode is false: State is not S_BUSY.</li>
     * </ul>
     * Since the UPOS specification does not clearly specify whether property DuplicateReceipt must be true,
     * it can be checked by the specific implementation if this should be necessary.
     * <br>The default implementation should be called within derived methods to ensure that the property
     * DuplicateReceipt is updated as expected.
     *
     * @throws JposException    If an error occurs.
     */
    public void printDuplicateReceipt() throws JposException;

    /**
     * Final part of PrintPeriodicTotalsReport method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PrinterState is PS_MONITOR,</li>
     *     <li>data1 and data2 are not null,</li>
     *     <li>both, date1 and date2, are well-formatted date strings as specified in the UPOS specification,</li>
     *     <li>date2 does not specify any date before date1.</li>
     * </ul>
     *
     * @param  date1            Starting date of report to print.
     * @param  date2            Ending date of report to print.
     * @throws JposException    If an error occurs.
     */
    public void printPeriodicTotalsReport(String date1, String date2) throws JposException;

    /**
     * Final part of PrintPowerLossReport method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapPowerLossReport is true,</li>
     *     <li>PrinterState is PS_MONITOR.</li>
     * </ul>
     *
     * @throws JposException    If an error occurs.
     */
    public void printPowerLossReport() throws JposException;

    /**
     * Final part of PrintReport method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PrinterState is PS_MONITOR,</li>
     *     <li>reportType is one of RT_ORDINAL, RT_DATE or RT_EOD_ORDINAL,</li>
     *     <li>if reportType == RT_DATE: startNum and endNum are date values as specified in the UPOS specification and
     * startNum specifies a date equal to or before endNum.</li>
     *     <li>if reportType != RT_DATE: startNum and endNum are numerical values and endNum is zero or startNum &le;
     * endNum.</li>
     * </ul>
     *
     * @param reportType        The kind of report to print.
     * @param  startNum         Starting record in fiscal printer.
     * @param  endNum           Final record in fiscal printer.
     * @throws JposException    If an error occurs.
     */
    public void printReport(int reportType, String startNum, String endNum) throws JposException;

    /**
     * Final part of PrintXReport method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapXReport is true,</li>
     *     <li>PrinterState is PS_MONITOR.</li>
     * </ul>
     *
     * @throws JposException    If an error occurs.
     */
    public void printXReport() throws JposException;

    /**
     * Final part of PrintZReport method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PrinterState is PS_MONITOR.</li>
     * </ul>
     * The default implementation should be called within derived methods to ensure that the property
     * DayOpened is updated as expected.
     *
     * @throws JposException    If an error occurs.
     */
    public void printZReport() throws JposException;

    /**
     * Final part of ResetPrinter method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     * The default implementation should be called within derived methods to ensure that the properties
     * PrinterState, TrainingModeActive and DuplicateReceipt are updated as expected.
     *
     * @throws JposException    If an error occurs.
     */
    public void resetPrinter() throws JposException;

    /**
     * Final part of SetCurrency method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSetCurrency is true,</li>
     *     <li>DayOpened is false,</li>
     *     <li>newCurrency is one of the SC_ values (currently only SC_EURO).</li>
     *     <li>ActualCurrency is not the corresponding AC_value (currently: not AC_EUR).</li>
     * </ul>
     * The default implementation should be called within derived methods to ensure that the property
     * ActualCurrency is updated as expected.
     *
     * @param newCurrency       The new currency.
     * @throws JposException    If an error occurs.
     */
    public void setCurrency(int newCurrency) throws JposException;

    /**
     * Final part of SetDate method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>DayOpened is false,</li>
     *     <li>date is a valid date string as specified in the UPOS specification vor method SetDate.</li>
     * </ul>
     *
     * @param date              Date and time as a string.
     * @throws JposException    If an error occurs.
     */
    public void setDate(String date) throws JposException;

    /**
     * Final part of SetHeaderLine method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSetHeader is true,</li>
     *     <li>DayOpened is false,</li>
     *     <li>lineNumber is between 1 and NumHeaderLines,</li>
     *     <li>if CapReservedWord is true, text does not contain the word stored in property ReservedWord.</li>
     * </ul>
     * Any derived method should check whether the text length is valid in combination with doubleWidth. The
     * UPOS specification does not specify how a service shall behave if doubleWidth is true and CapDoubleWidth is
     * false.
     *
     * @param lineNumber        Line number of the header line to set.
     * @param text              Text to which to set the header line.
     * @param doubleWidth       Print this line in double wide characters.
     * @throws JposException    If an error occurs.
     */
    public void setHeaderLine(int lineNumber, String text, boolean doubleWidth) throws JposException;

    /**
     * Final part of SetPOSID method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSetPOSID is true,</li>
     *     <li>DayOpened is false,</li>
     *     <li>if CapReservedWord is true, neither POSID nor cashierID contains the word stored in property ReservedWord.</li>
     * </ul>
     * Any derived method should check whether length and format of POSID and cashierID are valid.
     *
     * @param POSID             Identifier for the POS system.
     * @param cashierID         Identifier of the current cashier.
     * @throws JposException    If an error occurs.
     */
    public void setPOSID(String POSID, String cashierID) throws JposException;

    /**
     * Final part of SetStoreFiscalID method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSetStoreFiscalID is true,</li>
     *     <li>DayOpened is false.</li>
     * </ul>
     * Any derived method should check whether length and format of ID are valid.
     *
     * @param ID                Fiscal identifier.
     * @throws JposException    If an error occurs.
     */
    public void setStoreFiscalID(String ID) throws JposException;

    /**
     * Final part of SetTrailerLine method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSetTrailer is true,</li>
     *     <li>DayOpened is false,</li>
     *     <li>lineNumber is between 1 and NumTrailerLines,</li>
     *     <li>if CapReservedWord is true, text does not contain the word stored in property ReservedWord.</li>
     * </ul>
     * Any derived method should check whether the text length is valid in combination with doubleWidth. The
     * UPOS specification does not specify how a service shall behave if doubleWidth is true and CapDoubleWidth is
     * false.
     *
     * @param lineNumber        Line number of the trailer line to set.
     * @param text              Text to which to set the trailer line.
     * @param doubleWidth       Print this line in double wide characters.
     * @throws JposException    If an error occurs.
     */
    public void setTrailerLine(int lineNumber, String text, boolean doubleWidth) throws JposException;

    /**
     * Final part of SetVatTable method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapHasVatTable is true,</li>
     *     <li>CapSetVatTable is true,</li>
     *     <li>DayOpened is false.</li>
     * </ul>
     *
     * @throws JposException    If an error occurs.
     */
    public void setVatTable() throws JposException;

    /**
     * Final part of SetVatValue method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapHasVatTable is true,</li>
     *     <li>CapSetVatTable is true,</li>
     *     <li>DayOpened is false,</li>
     *     <li>vatValue is an value &le; 999999, representing a percent value &le; 99.9999.</li>
     * </ul>
     * <b>Attention:</b> Each service implementation that supports setting VAT values must perform additional
     * checks for vatID and vatValue:
     * <ul>
     *     <li>vatID must be one of NumVatRate values. However, UPOS does not specify the upper or lower border of
     * vatID. For example, if NumVatRate is 4, valid values for vatID might be 0 - 3, 1 - 4, '1' - '4', 'A' - 'D',
     * the ASCII code of any character in "ABZN" ...</li>
     *     <li>Since UPOS does not define the format of a percentage string, vatValue will be computed from the original
     * string parameter as follows: If it is an integer value, it will be used unchanged if the value is &ge; 100 and
     * the value will be multiplied by 10000 if the value is &lt; 100. Otherwise, it will be converted into a BigDecimal,
     * multiplied by 10000, checked to fit into a long and passed as vatValue.</li>
     * </ul>
     *
     * @param vatID             Index of the VAT table entry to set.
     * @param vatValue          Tax value as a percentage.
     * @throws JposException    If an error occurs.
     */
    public void setVatValue(int vatID, long vatValue) throws JposException;

    /**
     * Final part of VerifyItem method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapHasVatTable is true,</li>
     *     <li>CapItemList is true,</li>
     *     <li>PrinterState is PS_ITEM_LIST,</li>
     *     <li>If CapReservedWord is true: itemName does not contain the contents of property ReservedWord.</li>
     * </ul>
     * <b>Attention:</b> Each service implementation that supports verifying items must perform additional
     * checks for itemName and vatID:
     * <ul>
     *     <li>itemName may not contain invalid characters and must not exceed a specific length.</li>
     *     <li>vatID must be one of NumVatRate values. However, UPOS does not specify the upper or lower border of
     *         vatID. For example, if NumVatRate is 4, valid values for vatID might be 0 - 3, 1 - 4, '1' - '4', 'A' - 'D',
     *         the ASCII code of any character in "ABZN" ...
     *     </li>
     * </ul>
     *
     * @param itemName          Item to be verified.
     * @param vatID             VAT identifier of the item.
     * @throws JposException    If an error occurs.
     */
    public void verifyItem(String itemName, int vatID) throws JposException;

    /**
     * Validation part of PrintFiscalDocumentLine method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSlpFiscalDocument is true,</li>
     *     <li>PrinterState is PS_FISCAL_DOCUMENT,</li>
     *     <li>documentLine is not null.</li>
     * </ul>
     * In addition, If AsyncMode is false:
     * <ul>
     *     <li>State is not S_BUSY,</li>
     *     <li>CapCoverSensor is false or CoverOpen is false,</li>
     *     <li>CapSlpEmptySensor is false or SlpEmpty is false.</li>
     * </ul>
     *
     * @param documentLine      Line to be printed on fiscal slip.
     * @return PrintFiscalDocumentLine object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintFiscalDocumentLine printFiscalDocumentLine(String documentLine) throws JposException;

    /**
     * Final part of PrintFiscalDocumentLine method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintFiscalDocumentLine object. This method
     * will be called when the corresponding operation shall be performed, either synchronously or asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters to
     *                          be used by PrintFiscalDocumentLine.
     * @throws JposException    If an error occurs.
     */
    public void printFiscalDocumentLine(PrintFiscalDocumentLine request) throws JposException;

    /**
     * Validation part of PrintFixedOutput method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapFixedOutput is true,</li>
     *     <li>PrinterState is PS_FIXED_OUTPUT,</li>
     *     <li>data is not null.</li>
     * </ul>
     * In addition, If AsyncMode is false:
     * <ul>
     *     <li>State is not S_BUSY,</li>
     *     <li>CapCoverSensor is false or CoverOpen is false,</li>
     *     <li>CapRecEmptySensor is false or RecEmpty is false.</li>
     * </ul>
     *
     * @param documentType  Identifier of a document stored in the Fiscal Printer.
     * @param lineNumber    Number of the line in the document to print.
     * @param data          String parameter for placement in printed line.
     * @return PrintFixedOutput object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintFixedOutput printFixedOutput(int documentType, int lineNumber, String data) throws JposException;

    /**
     * Final part of PrintFixedOutput method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintFixedOutput object. This method
     * will be called when the corresponding operation shall be performed, either synchronously or asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintFixedOutput.
     * @throws JposException    If an error occurs.
     */
    public void printFixedOutput(PrintFixedOutput request) throws JposException;

    /**
     * Validation part of PrintNormal method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PrinterState is PS_NONFISCAL, or PrinterState is PS_FISCAL_RECEIPT and FiscalReceiptType is RT_GENERIC,
     * RT_CASH_IN or RT_CASH_OUT,</li>
     *     <li>station is one of S_JOURNAL, S_RECEIPT or S_SLIP,</li>
     *     <li>The corresponding presence property (CapJrnPresent, CapRecPresent or CapSlpPresent) is true,</li>
     *     <li>data is not null.</li>
     * </ul>
     * If PrinterState is FiscalReceiptType, station must logically match property FiscalReceiptStation.
     * <br>If AsyncMode is false:
     * <ul>
     *     <li>State is not S_BUSY,</li>
     *     <li>CapCoverSensor is false or CoverOpen is false,</li>
     *     <li>the corresponding empty paper sensor is not present or its property value (JrnEmpty, RecEmpty or
     * SlpEmpty) is false.</li>
     * </ul>
     *
     * @param station       The Fiscal Printer station to be used.
     * @param data          The characters to be printed.
     * @return PrintNormal object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintNormal printNormal(int station, String data) throws JposException;

    /**
     * Final part of PrintNormal method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintNormal object. This method
     * will be called when the corresponding operation shall be performed, either synchronously or asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintNormal.
     * @throws JposException    If an error occurs.
     */
    public void printNormal(PrintNormal request) throws JposException;

    /**
     * Validation part of PrintRecCash method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PrinterState is PS_FISCAL_RECEIPT,</li>
     *     <li>CapFiscalReceiptType is true,</li>
     *     <li>FiscalReceiptType is RT_CASH_IN or RT_CASH_OUT,</li>
     *     <li>amount is &gt; 0.</li>
     *     <li>amount is a multiple of smallest cash units (e.g. a multiple of 100 for Euro).</li>
     * </ul>
     * In addition, If AsyncMode is false:
     * <ul>
     *     <li>State is not S_BUSY,</li>
     *     <li>CapCoverSensor is false or CoverOpen is false,</li>
     *     <li>the empty paper sensors of the journal and the station specified by FiscalReceiptStation are not present
     * or their property values (JrnEmpty and RecEmpty or SlpEmpty) are false.</li>
     * </ul>
     *
     * @param amount            Amount to be incremented or decremented.
     * @return PrintRecCash object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintRecCash printRecCash(long amount) throws JposException;

    /**
     * Final part of PrintRecCash method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintRecCash object. This method
     * will be called when the corresponding operation shall be performed, either synchronously or asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintRecCash.
     * @throws JposException    If an error occurs.
     */
    public void printRecCash(PrintRecCash request) throws JposException;

    /**
     * Validation part of PrintRecItem method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PrinterState is PS_FISCAL_RECEIPT,</li>
     *     <li>CapFiscalReceiptType is false or FiscalReceiptType is RT_SALES, RT_SERVICE, RT_SIMPLE_INVOICE or RT_REFUND,</li>
     *     <li>amount quantity and price are &ge; 0,</li>
     *     <li>description and unitName are not null and do not contain the reserved word, if any.</li>
     * </ul>
     * In addition, If AsyncMode is false:
     * <ul>
     *     <li>State is not S_BUSY,</li>
     *     <li>CapCoverSensor is false or CoverOpen is false,</li>
     *     <li>the empty paper sensors of the journal and the station specified by FiscalReceiptStation are not present
     * or their property values (JrnEmpty and RecEmpty or SlpEmpty) are false.</li>
     * </ul>
     * The implementation should sets properties PreLine and PostLine to an empty string. Even if the UPOS specification
     * tells that this shall be done after the command has been executed, this should be done here because the contents
     * of these properties are buffered in the PrePostOutputRequest and the application should have
     * the opportunity to set these properties for further print requests after a print request has been enqueued
     * for asynchronous processing.
     * <br>For synchronous processing, this difference should not be relevant. However, keep in mind that PreLine
     * and PostLine must be reset to an empty string at least at the end of the final part of PrintRecItem.
     *
     * @param description       Text describing the item sold.
     * @param price             Price of the line item.
     * @param quantity          Number of items. If zero, a single item is assumed.
     * @param vatInfo           VAT rate identifier or amount.
     * @param unitPrice         Price of each item.
     * @param unitName          Name of the unit.
     * @return PrintRecItem object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintRecItem printRecItem(String description, long price, int quantity, int vatInfo, long unitPrice, String unitName) throws JposException;

    /**
     * Final part of PrintRecItem method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintRecItem object. This method
     * will be called when the corresponding operation shall be performed, either synchronously or asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintRecItem.
     * @throws JposException    If an error occurs.
     */
    public void printRecItem(PrintRecItem request) throws JposException;

    /**
     * Validation part of PrintRecItemAdjustment method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PrinterState is PS_FISCAL_RECEIPT,</li>
     *     <li>CapFiscalReceiptType is false or FiscalReceiptType is RT_SALES, RT_SERVICE, RT_SIMPLE_INVOICE or RT_REFUND,</li>
     *     <li>adjustmentType is one of AT_PERCENTAGE_DISCOUNT, AT_PERCENTAGE_SURCHARGE, AT_COUPON_PERCENTAGE_DISCOUNT,
     * AT_AMOUNT_DISCOUNT, AT_AMOUNT_SURCHARGE or AT_COUPON_AMOUNT_DISCOUNT,</li>
     *     <li>if CapAmountAdjustment is false, adjustmentType is one of AT_PERCENTAGE_DISCOUNT, AT_PERCENTAGE_SURCHARGE
     * or AT_COUPON_PERCENTAGE_DISCOUNT,</li>
     *     <li>if CapPercentAdjustment is false, adjustmentType is one of AT_AMOUNT_DISCOUNT, AT_AMOUNT_SURCHARGE or
     * AT_COUPON_AMOUNT_DISCOUNT,</li>
     *     <li>if CapPositiveAdjustment is false, adjustmentType is one of AT_AMOUNT_DISCOUNT, AT_COUPON_AMOUNT_DISCOUNT,
     * AT_PERCENTAGE_DISCOUNT, AT_PERCENTAGE_SURCHARGE or AT_COUPON_PERCENTAGE_DISCOUNT,</li>
     *     <li>amount &gt; 0,</li>
     *     <li>description is not null and do not contain the reserved word, if any.</li>
     * </ul>
     * In addition, If AsyncMode is false:
     * <ul>
     *     <li>State is not S_BUSY,</li>
     *     <li>CapCoverSensor is false or CoverOpen is false,</li>
     *     <li>the empty paper sensors of the journal and the station specified by FiscalReceiptStation are not present
     * or their property values (JrnEmpty and RecEmpty or SlpEmpty) are false.</li>
     * </ul>
     * The implementation should sets properties PreLine to an empty string. Even if the UPOS specification
     * tells that this shall be done after the command has been executed, this should be done here because the contents
     * of these properties are buffered in the PrePostOutputRequest and the application should have
     * the opportunity to set these properties for further print requests after a print request has been enqueued
     * for asynchronous processing.
     * <br>For synchronous processing, this difference should not be relevant. However, keep in mind that PreLine
     * must be reset to an empty string at least at the end of the final part of PrintRecItemAdjustment.
     *
     * @param adjustmentType    Type of adjustment.
     * @param description       Text describing the item sold.
     * @param amount            Number of items. If zero, a single item is assumed.
     * @param vatInfo           VAT rate identifier or amount.
     * @return PrintRecItemAdjustment object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintRecItemAdjustment printRecItemAdjustment(int adjustmentType, String description, long amount, int vatInfo) throws JposException;

    /**
     * Final part of PrintRecItemAdjustment method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintRecItemAdjustment object. This method
     * will be called when the corresponding operation shall be performed, either synchronously or asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintRecItemAdjustment.
     * @throws JposException    If an error occurs.
     */
    public void printRecItemAdjustment(PrintRecItemAdjustment request) throws JposException;

    /**
     * Validation part of PrintRecItemAdjustmentVoid method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PrinterState is PS_FISCAL_RECEIPT,</li>
     *     <li>CapFiscalReceiptType is false or FiscalReceiptType is RT_SALES, RT_SERVICE, RT_SIMPLE_INVOICE or RT_REFUND,</li>
     *     <li>adjustmentType is one of AT_PERCENTAGE_DISCOUNT, AT_PERCENTAGE_SURCHARGE, AT_COUPON_PERCENTAGE_DISCOUNT,
     * AT_AMOUNT_DISCOUNT, AT_AMOUNT_SURCHARGE or AT_COUPON_AMOUNT_DISCOUNT,</li>
     *     <li>if CapAmountAdjustment is false, adjustmentType is one of AT_PERCENTAGE_DISCOUNT, AT_PERCENTAGE_SURCHARGE
     * or AT_COUPON_PERCENTAGE_DISCOUNT,</li>
     *     <li>if CapPercentAdjustment is false, adjustmentType is one of AT_AMOUNT_DISCOUNT, AT_AMOUNT_SURCHARGE or
     * AT_COUPON_AMOUNT_DISCOUNT,</li>
     *     <li>if CapPositiveAdjustment is false, adjustmentType is one of AT_AMOUNT_DISCOUNT, AT_COUPON_AMOUNT_DISCOUNT,
     * AT_PERCENTAGE_DISCOUNT, AT_PERCENTAGE_SURCHARGE or AT_COUPON_PERCENTAGE_DISCOUNT,</li>
     *     <li>amount &gt; 0,</li>
     *     <li>description is not null and do not contain the reserved word, if any.</li>
     * </ul>
     * In addition, If AsyncMode is false:
     * <ul>
     *     <li>State is not S_BUSY,</li>
     *     <li>CapCoverSensor is false or CoverOpen is false,</li>
     *     <li>the empty paper sensors of the journal and the station specified by FiscalReceiptStation are not present
     * or their property values (JrnEmpty and RecEmpty or SlpEmpty) are false.</li>
     * </ul>
     * The implementation should sets properties PreLine to an empty string. Even if the UPOS specification
     * tells that this shall be done after the command has been executed, this should be done here because the contents
     * of these properties are buffered in the PrePostOutputRequest and the application should have
     * the opportunity to set these properties for further print requests after a print request has been enqueued
     * for asynchronous processing.
     * <br>For synchronous processing, this difference should not be relevant. However, keep in mind that PreLine
     * must be reset to an empty string at least at the end of the final part of PrintRecItemAdjustmentVoid.
     *
     * @param adjustmentType    Type of adjustment.
     * @param description       Text describing the item sold.
     * @param amount            Number of items. If zero, a single item is assumed.
     * @param vatInfo           VAT rate identifier or amount.
     * @return PrintRecItemAdjustmentVoid object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintRecItemAdjustmentVoid printRecItemAdjustmentVoid(int adjustmentType, String description, long amount, int vatInfo) throws JposException;

    /**
     * Final part of PrintRecItemAdjustmentVoid method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintRecItemAdjustmentVoid object. This method
     * will be called when the corresponding operation shall be performed, either synchronously or asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintRecItemAdjustmentVoid.
     * @throws JposException    If an error occurs.
     */
    public void printRecItemAdjustmentVoid(PrintRecItemAdjustmentVoid request) throws JposException;

    /**
     * Validation part of PrintRecItemFuel method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PrinterState is PS_FISCAL_RECEIPT,</li>
     *     <li>CapFiscalReceiptType is false or FiscalReceiptType is RT_SALES, RT_SERVICE, RT_SIMPLE_INVOICE or RT_REFUND,</li>
     *     <li>amount, quantity, price and specialTax are &ge; 0,</li>
     *     <li>description, unitName and specialTaxName are not null and do not contain the reserved word, if any.</li>
     * </ul>
     * In addition, If AsyncMode is false:
     * <ul>
     *     <li>State is not S_BUSY,</li>
     *     <li>CapCoverSensor is false or CoverOpen is false,</li>
     *     <li>the empty paper sensors of the journal and the station specified by FiscalReceiptStation are not present
     * or their property values (JrnEmpty and RecEmpty or SlpEmpty) are false.</li>
     * </ul>
     * FiscalPrinter specific implementations should set properties PreLine and PostLine as expected. The UPOS specification
     * does not tells anything about handling of PreLine and PostLine in PrintRecItemFuel, therefore
     * handling is vendor specific.
     * <br>It might be a good solution to set Preline and / or PostLine to an empty string whenever they should be
     * printed by the service and to let them unchanged otherwise.
     *
     * @param description       Text describing the item sold.
     * @param price             Price of the line item.
     * @param quantity          Number of items. If zero, a single item is assumed.
     * @param vatInfo           VAT rate identifier or amount.
     * @param unitPrice         Price of each item.
     * @param unitName          Name of the unit.
     * @param specialTax        Special tax amount, e.g., road tax.
     * @param specialTaxName    Name of the special tax.
     * @return PrintRecItemFuel object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintRecItemFuel printRecItemFuel(String description, long price, int quantity, int vatInfo, long unitPrice, String unitName, long specialTax, String specialTaxName) throws JposException;

    /**
     * Final part of PrintRecItemFuel method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintRecItemFuel object. This method
     * will be called when the corresponding operation shall be performed, either synchronously or asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintRecItemFuel.
     * @throws JposException    If an error occurs.
     */
    public void printRecItemFuel(PrintRecItemFuel request) throws JposException;

    /**
     * Validation part of PrintRecItemFuelVoid method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PrinterState is PS_FISCAL_RECEIPT,</li>
     *     <li>CapFiscalReceiptType is false or FiscalReceiptType is RT_SALES, RT_SERVICE, RT_SIMPLE_INVOICE or RT_REFUND,</li>
     *     <li>price and specialTax are &ge; 0,</li>
     *     <li>description is not null and does not contain the reserved word, if any.</li>
     * </ul>
     * In addition, If AsyncMode is false:
     * <ul>
     *     <li>State is not S_BUSY,</li>
     *     <li>CapCoverSensor is false or CoverOpen is false,</li>
     *     <li>the empty paper sensors of the journal and the station specified by FiscalReceiptStation are not present
     * or their property values (JrnEmpty and RecEmpty or SlpEmpty) are false.</li>
     * </ul>
     * FiscalPrinter specific implementations should set properties PreLine and PostLine as expected. The UPOS specification
     * does not tells anything about handling of PreLine and PostLine in PrintRecItemFuelVoid, therefore
     * handling is vendor specific.
     * <br>It might be a good solution to set Preline and / or PostLine to an empty string whenever they should be
     * printed by the service and to let them unchanged otherwise.
     *
     * @param description       Text describing the item sold.
     * @param price             Price of the line item.
     * @param vatInfo           VAT rate identifier or amount.
     * @param specialTax        Special tax amount, e.g., road tax.
     * @return PrintRecItemFuelVoid object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintRecItemFuelVoid printRecItemFuelVoid(String description, long price, int vatInfo, long specialTax) throws JposException;

    /**
     * Final part of PrintRecItemFuelVoid method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintRecItemFuelVoid object. This method
     * will be called when the corresponding operation shall be performed, either synchronously or asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintRecItemFuelVoid.
     * @throws JposException    If an error occurs.
     */
    public void printRecItemFuelVoid(PrintRecItemFuelVoid request) throws JposException;

    /**
     * Validation part of PrintRecItemVoid method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PrinterState is PS_FISCAL_RECEIPT,</li>
     *     <li>CapFiscalReceiptType is false or FiscalReceiptType is RT_SALES, RT_SERVICE, RT_SIMPLE_INVOICE or RT_REFUND,</li>
     *     <li>amount, quantity and price are &ge; 0,</li>
     *     <li>description and unitName are not null and do not contain the reserved word, if any.</li>
     * </ul>
     * In addition, If AsyncMode is false:
     * <ul>
     *     <li>State is not S_BUSY,</li>
     *     <li>CapCoverSensor is false or CoverOpen is false,</li>
     *     <li>the empty paper sensors of the journal and the station specified by FiscalReceiptStation are not present
     * or their property values (JrnEmpty and RecEmpty or SlpEmpty) are false.</li>
     * </ul>
     * The implementation should sets properties PreLine and PostLine to an empty string. Even if the UPOS specification
     * tells that this shall be done after the command has been executed, this should be done here because the contents
     * of these properties are buffered in the PrePostOutputRequest and the application should have
     * the opportunity to set these properties for further print requests after a print request has been enqueued
     * for asynchronous processing.
     * <br>For synchronous processing, this difference should not be relevant. However, keep in mind that PreLine
     * and PostLine must be reset to an empty string at least at the end of the final part of PrintRecItemVoid.
     *
     * @param description       Text describing the item sold.
     * @param price             Price of the line item.
     * @param quantity          Number of items. If zero, a single item is assumed.
     * @param vatInfo           VAT rate identifier or amount.
     * @param unitPrice         Price of each item.
     * @param unitName          Name of the unit.
     * @return PrintRecItemVoid object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintRecItemVoid printRecItemVoid(String description, long price, int quantity, int vatInfo, long unitPrice, String unitName) throws JposException;

    /**
     * Final part of PrintRecItemVoid method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintRecItemVoid object. This method
     * will be called when the corresponding operation shall be performed, either synchronously or asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintRecItemVoid.
     * @throws JposException    If an error occurs.
     */
    public void printRecItemVoid(PrintRecItemVoid request) throws JposException;

    /**
     * Validation part of PrintRecItemRefund method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PrinterState is PS_FISCAL_RECEIPT,</li>
     *     <li>CapFiscalReceiptType is false or FiscalReceiptType is RT_SALES, RT_SERVICE or RT_REFUND,</li>
     *     <li>amount, quantity and price are &ge; 0,</li>
     *     <li>description and unitName are not null and do not contain the reserved word, if any.</li>
     * </ul>
     * In addition, If AsyncMode is false:
     * <ul>
     *     <li>State is not S_BUSY,</li>
     *     <li>CapCoverSensor is false or CoverOpen is false,</li>
     *     <li>the empty paper sensors of the journal and the station specified by FiscalReceiptStation are not present
     * or their property values (JrnEmpty and RecEmpty or SlpEmpty) are false.</li>
     * </ul>
     * The implementation should sets property PreLine to an empty string. Even if the UPOS specification
     * tells that this shall be done after the command has been executed, this should be done here because the contents
     * of this properties is buffered in the PrePostOutputRequest and the application should have
     * the opportunity to set this property for further print requests after a print request has been enqueued
     * for asynchronous processing.
     * <br>For synchronous processing, this difference should not be relevant. However, keep in mind that PreLine
     * must be reset to an empty string at least at the end of the final part of PrintRecItemRefund.
     *
     * @param description       Text describing the item sold.
     * @param amount            Price of the line item.
     * @param quantity          Number of items. If zero, a single item is assumed.
     * @param vatInfo           VAT rate identifier or amount.
     * @param unitAmount        Price of each item.
     * @param unitName          Name of the unit.
     * @return PrintRecItemRefund object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintRecItemRefund printRecItemRefund(String description, long amount, int quantity, int vatInfo, long unitAmount, String unitName) throws JposException;

    /**
     * Final part of PrintRecItemRefund method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintRecItemRefund object. This method
     * will be called when the corresponding operation shall be performed, either synchronously or asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintRecItemRefund.
     * @throws JposException    If an error occurs.
     */
    public void printRecItemRefund(PrintRecItemRefund request) throws JposException;

    /**
     * Validation part of PrintRecItemRefundVoid method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PrinterState is PS_FISCAL_RECEIPT,</li>
     *     <li>CapFiscalReceiptType is false or FiscalReceiptType is RT_SALES, RT_SERVICE or RT_REFUND,</li>
     *     <li>amount, quantity and price are &ge; 0,</li>
     *     <li>description and unitName are not null and do not contain the reserved word, if any.</li>
     * </ul>
     * In addition, If AsyncMode is false:
     * <ul>
     *     <li>State is not S_BUSY,</li>
     *     <li>CapCoverSensor is false or CoverOpen is false,</li>
     *     <li>the empty paper sensors of the journal and the station specified by FiscalReceiptStation are not present
     * or their property values (JrnEmpty and RecEmpty or SlpEmpty) are false.</li>
     * </ul>
     *
     * @param description       Text describing the item sold.
     * @param amount            Price of the line item.
     * @param quantity          Number of items. If zero, a single item is assumed.
     * @param vatInfo           VAT rate identifier or amount.
     * @param unitAmount        Price of each item.
     * @param unitName          Name of the unit.
     * @return PrintRecItemRefundVoid object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintRecItemRefundVoid printRecItemRefundVoid(String description, long amount, int quantity, int vatInfo, long unitAmount, String unitName) throws JposException;

    /**
     * Final part of PrintRecItemRefundVoid method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintRecItemRefundVoid object. This method
     * will be called when the corresponding operation shall be performed, either synchronously or asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintRecItemRefundVoid.
     * @throws JposException    If an error occurs.
     */
    public void printRecItemRefundVoid(PrintRecItemRefundVoid request) throws JposException;

    /**
     * Validation part of PrintRecMessage method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapAdditionalLines is true,</li>
     *     <li>PrinterState is PS_FISCAL_RECEIPT,</li>
     *     <li>CapFiscalReceiptType is false or FiscalReceiptType is RT_SALES, RT_SERVICE, RT_SIMPLE_INVOICE or RT_REFUND,</li>
     *     <li>amount is &ge; 0,</li>
     *     <li>description is not null and do not contain the reserved word, if any.</li>
     * </ul>
     * In addition, If AsyncMode is false:
     * <ul>
     *     <li>State is not S_BUSY,</li>
     *     <li>CapCoverSensor is false or CoverOpen is false,</li>
     *     <li>the empty paper sensors of the journal and the station specified by FiscalReceiptStation are not present
     * or their property values (JrnEmpty and RecEmpty or SlpEmpty) are false,</li>
     *     <li>description in not longer than MessageLength property specifies.</li>
     * </ul>
     *
     * @param message           Text message to print.
     * @return PrintRecMessage object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintRecMessage printRecMessage(String message) throws JposException;

    /**
     * Final part of PrintRecMessage method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintRecMessage object. This method
     * will be called when the corresponding operation shall be performed, either synchronously or asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintRecMessage.
     * @throws JposException    If an error occurs.
     */
    public void printRecMessage(PrintRecMessage request) throws JposException;

    /**
     * Validation part of PrintRecNotPaid method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapReceiptNotPaid is true,</li>
     *     <li>PrinterState is PS_FISCAL_RECEIPT or PS_FISCAL_RECEIPT_TOTAL,</li>
     *     <li>CapFiscalReceiptType is false or FiscalReceiptType is RT_SALES, RT_SERVICE, RT_SIMPLE_INVOICE or RT_REFUND,</li>
     *     <li>amount is &gt; 0,</li>
     *     <li>description is not null and does not contain the reserved word, if any.</li>
     * </ul>
     * In addition, If AsyncMode is false:
     * <ul>
     *     <li>State is not S_BUSY,</li>
     *     <li>CapCoverSensor is false or CoverOpen is false,</li>
     *     <li>the empty paper sensors of the journal and the station specified by FiscalReceiptStation are not present
     * or their property values (JrnEmpty and RecEmpty or SlpEmpty) are false.</li>
     * </ul>
     *
     * @param description       Text describing the not paid amount.
     * @param amount            Amount not paid.
     * @return PrintRecNotPaid object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintRecNotPaid printRecNotPaid(String description, long amount) throws JposException;

    /**
     * Final part of PrintRecNotPaid method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintRecNotPaid object. This method
     * will be called when the corresponding operation shall be performed, either synchronously or asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     * <br>The default implementation should be called within derived methods to ensure that the property
     * PrinterState is updated as expected.
     *
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintRecNotPaid.
     * @throws JposException    If an error occurs.
     */
    public void printRecNotPaid(PrintRecNotPaid request) throws JposException;

    /**
     * Validation part of PrintRecPackageAdjustment method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapPackageAdjustment is true,</li>
     *     <li>PrinterState is PS_FISCAL_RECEIPT,</li>
     *     <li>CapFiscalReceiptType is false or FiscalReceiptType is RT_SALES, RT_SERVICE, RT_SIMPLE_INVOICE or RT_REFUND,</li>
     *     <li>adjustmentType is one of AT_AMOUNT_DISCOUNT, AT_AMOUNT_SURCHARGE, AT_PERCENTAGE_DISCOUNT, AT_PERCENTAGE_SURCHARGE,
     * AT_COUPON_AMOUNT_DISCOUNT or AT_COUPON_PERCENTAGE_DISCOUNT,</li>
     *     <li>description is not null and does not contain the reserved word, if any,</li>
     *     <li>vatAdjustment is not null and consists of no more than NumVatRates value pairs, each consisting of two values:
     * A long and a long or a decimal number with maximum 4 decimals.</li>
     * </ul>
     * In addition, If AsyncMode is false:
     * <ul>
     *     <li>State is not S_BUSY,</li>
     *     <li>CapCoverSensor is false or CoverOpen is false,</li>
     *     <li>the empty paper sensors of the journal and the station specified by FiscalReceiptStation are not present
     * or their property values (JrnEmpty and RecEmpty or SlpEmpty) are false.</li>
     * </ul>
     * The implementation should sets property PreLine to an empty string. Even if the UPOS specification
     * tells that this shall be done after the command has been executed, this should be done here because the contents
     * of this properties is buffered in the PrePostOutputRequest and the application should have
     * the opportunity to set this property for further print requests after a print request has been enqueued
     * for asynchronous processing.
     * <br>For synchronous processing, this difference should not be relevant. However, keep in mind that PreLine
     * must be reset to an empty string at least at the end of the final part of PrintRecPackageAdjustment.
     *
     * @param adjustmentType    Type of adjustment.
     * @param description       Text describing the adjustment.
     * @param vatAdjustment     String containing a list of adjustment(s) to be voided for different VAT(s). See
     *                          UPOS method PrintRecPackageAdjustment.
     * @return PrintRecPackageAdjustment object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintRecPackageAdjustment printRecPackageAdjustment(int adjustmentType, String description, String vatAdjustment) throws JposException;

    /**
     * Final part of PrintRecPackageAdjustment method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintRecPackageAdjustment object. This method
     * will be called when the corresponding operation shall be performed, either synchronously or asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintRecPackageAdjustment.
     * @throws JposException    If an error occurs.
     */
    public void printRecPackageAdjustment(PrintRecPackageAdjustment request) throws JposException;

    /**
     * Validation part of PrintRecPackageAdjustVoid method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PrinterState is PS_FISCAL_RECEIPT,</li>
     *     <li>CapFiscalReceiptType is false or FiscalReceiptType is RT_SALES, RT_SERVICE, RT_SIMPLE_INVOICE or RT_REFUND,</li>
     *     <li>adjustmentType is one of AT_AMOUNT_DISCOUNT, AT_AMOUNT_SURCHARGE, AT_PERCENTAGE_DISCOUNT, AT_PERCENTAGE_SURCHARGE,
     * AT_COUPON_AMOUNT_DISCOUNT or AT_COUPON_PERCENTAGE_DISCOUNT,</li>
     *     <li>vatAdjustment is not null and consists of no more than NumVatRates value pairs, each consisting of two values:
     * A long and a long or a decimal number with maximum 4 decimals.</li>
     * </ul>
     * In addition, If AsyncMode is false:
     * <ul>
     *     <li>State is not S_BUSY,</li>
     *     <li>CapCoverSensor is false or CoverOpen is false,</li>
     *     <li>the empty paper sensors of the journal and the station specified by FiscalReceiptStation are not present
     * or their property values (JrnEmpty and RecEmpty or SlpEmpty) are false.</li>
     * </ul>
     * The implementation should sets property PreLine to an empty string. Even if the UPOS specification
     * tells that this shall be done after the command has been executed, this should be done here because the contents
     * of this properties is buffered in the PrePostOutputRequest and the application should have
     * the opportunity to set this property for further print requests after a print request has been enqueued
     * for asynchronous processing.
     * <br>For synchronous processing, this difference should not be relevant. However, keep in mind that PreLine
     * must be reset to an empty string at least at the end of the final part of PrintRecPackageAdjustVoid.
     *
     * @param adjustmentType    Type of adjustment.
     * @param vatAdjustment     String containing a list of adjustment(s) to be voided for different VAT(s). See
     *                          UPOS method PrintRecPackageAdjustVoid.
     * @return PrintRecPackageAdjustVoid object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintRecPackageAdjustVoid printRecPackageAdjustVoid(int adjustmentType, String vatAdjustment) throws JposException;

    /**
     * Final part of PrintRecPackageAdjustVoid method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintRecPackageAdjustVoid object. This method
     * will be called when the corresponding operation shall be performed, either synchronously or asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintRecPackageAdjustVoid.
     * @throws JposException    If an error occurs.
     */
    public void printRecPackageAdjustVoid(PrintRecPackageAdjustVoid request) throws JposException;

    /**
     * Validation part of PrintRecRefund method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PrinterState is PS_FISCAL_RECEIPT,</li>
     *     <li>CapFiscalReceiptType is false or FiscalReceiptType is RT_SALES, RT_SERVICE or RT_REFUND,</li>
     *     <li>amount is &ge; 0,</li>
     *     <li>description is not null and do not contain the reserved word, if any.</li>
     * </ul>
     * In addition, If AsyncMode is false:
     * <ul>
     *     <li>State is not S_BUSY,</li>
     *     <li>CapCoverSensor is false or CoverOpen is false,</li>
     *     <li>the empty paper sensors of the journal and the station specified by FiscalReceiptStation are not present
     * or their property values (JrnEmpty and RecEmpty or SlpEmpty) are false.</li>
     * </ul>
     * The implementation should sets property PreLine to an empty string. Even if the UPOS specification
     * tells that this shall be done after the command has been executed, this should be done here because the contents
     * of this properties is buffered in the PrePostOutputRequest and the application should have
     * the opportunity to set this property for further print requests after a print request has been enqueued
     * for asynchronous processing.
     * <br>For synchronous processing, this difference should not be relevant. However, keep in mind that PreLine
     * must be reset to an empty string at least at the end of the final part of PrintRecRefund.
     *
     * @param description       Text describing the item sold.
     * @param amount            Price of the line item.
     * @param vatInfo           VAT rate identifier or amount.
     * @return PrintRecRefund object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintRecRefund printRecRefund(String description, long amount, int vatInfo) throws JposException;

    /**
     * Final part of PrintRecRefund method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintRecRefund object. This method
     * will be called when the corresponding operation shall be performed, either synchronously or asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintRecRefund.
     * @throws JposException    If an error occurs.
     */
    public void printRecRefund(PrintRecRefund request) throws JposException;

    /**
     * Validation part of PrintRecRefundVoid method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PrinterState is PS_FISCAL_RECEIPT,</li>
     *     <li>CapFiscalReceiptType is false or FiscalReceiptType is RT_SALES, RT_SERVICE or RT_REFUND,</li>
     *     <li>amount is &ge; 0,</li>
     *     <li>description is not null and do not contain the reserved word, if any.</li>
     * </ul>
     * In addition, If AsyncMode is false:
     * <ul>
     *     <li>State is not S_BUSY,</li>
     *     <li>CapCoverSensor is false or CoverOpen is false,</li>
     *     <li>the empty paper sensors of the journal and the station specified by FiscalReceiptStation are not present
     * or their property values (JrnEmpty and RecEmpty or SlpEmpty) are false.</li>
     * </ul>
     *
     * @param description       Text describing the item sold.
     * @param amount            Price of the line item.
     * @param vatInfo           VAT rate identifier or amount.
     * @return PrintRecRefundVoid object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintRecRefundVoid printRecRefundVoid(String description, long amount, int vatInfo) throws JposException;

    /**
     * Final part of PrintRecRefundVoid method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintRecRefundVoid object. This method
     * will be called when the corresponding operation shall be performed, either synchronously or asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintRecRefundVoid.
     * @throws JposException    If an error occurs.
     */
    public void printRecRefundVoid(PrintRecRefundVoid request) throws JposException;

    /**
     * Validation part of PrintRecSubtotal method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PrinterState is PS_FISCAL_RECEIPT,</li>
     *     <li>CapFiscalReceiptType is false or FiscalReceiptType is RT_SALES, RT_SERVICE, RT_SIMPLE_INVOICE or RT_REFUND,</li>
     *     <li>amount is &ge; 0.</li>
     * </ul>
     * In addition, If AsyncMode is false:
     * <ul>
     *     <li>State is not S_BUSY,</li>
     *     <li>CapCoverSensor is false or CoverOpen is false,</li>
     *     <li>the empty paper sensors of the journal and the station specified by FiscalReceiptStation are not present
     * or their property values (JrnEmpty and RecEmpty or SlpEmpty) are false.</li>
     * </ul>
     * The implementation should sets property PostLine to an empty string. Even if the UPOS specification
     * tells that this shall be done after the command has been executed, this should be done here because the contents
     * of this properties is buffered in the PrePostOutputRequest and the application should have
     * the opportunity to set this property for further print requests after a print request has been enqueued
     * for asynchronous processing.
     * <br>For synchronous processing, this difference should not be relevant. However, keep in mind that PostLine
     * must be reset to an empty string at least at the end of the final part of PrintRecSubtotal.
     *
     * @param amount            Price of the line item.
     * @return PrintRecSubtotal object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintRecSubtotal printRecSubtotal(long amount) throws JposException;

    /**
     * Final part of PrintRecSubtotal method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintRecSubtotal object. This method
     * will be called when the corresponding operation shall be performed, either synchronously or asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintRecSubtotal.
     * @throws JposException    If an error occurs.
     */
    public void printRecSubtotal(PrintRecSubtotal request) throws JposException;

    /**
     * Validation part of PrintRecSubtotalAdjustment method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PrinterState is PS_FISCAL_RECEIPT,</li>
     *     <li>CapFiscalReceiptType is false or FiscalReceiptType is RT_SALES, RT_SERVICE, RT_SIMPLE_INVOICE or RT_REFUND,</li>
     *     <li>adjustmentType is one of AT_AMOUNT_DISCOUNT, AT_AMOUNT_SURCHARGE, AT_PERCENTAGE_DISCOUNT, AT_PERCENTAGE_SURCHARGE,
     * AT_COUPON_AMOUNT_DISCOUNT or AT_COUPON_PERCENTAGE_DISCOUNT,</li>
     *     <li>CapSubAmountAdjustment is true or adjustmentType is neither AT_AMOUNT_DISCOUNT nor AT_AMOUNT_SURCHARGE
     * or AT_COUPON_AMOUNT_DISCOUNT,</li>
     *     <li>CapSubPercentAdjustment is true or adjustmentType is neither AT_PERCENTAGE_DISCOUNT nor AT_PERCENTAGE_SURCHARGE
     * or AT_COUPON_PERCENTAGE_DISCOUNT,</li>
     *     <li>CapPositiveSubtotalAdjustment is true or adjustmentType is neither AT_AMOUNT_SURCHARGE nor AT_PERCENTAGE_SURCHARGE,</li>
     *     <li>description is not null and does not contain the reserver word, if any.</li>
     *     <li>amount is &ge; 0.</li>
     * </ul>
     * In addition, If AsyncMode is false:
     * <ul>
     *     <li>State is not S_BUSY,</li>
     *     <li>CapCoverSensor is false or CoverOpen is false,</li>
     *     <li>the empty paper sensors of the journal and the station specified by FiscalReceiptStation are not present
     * or their property values (JrnEmpty and RecEmpty or SlpEmpty) are false.</li>
     * </ul>
     * The implementation should sets property PreLine to an empty string. Even if the UPOS specification
     * tells that this shall be done after the command has been executed, this should be done here because the contents
     * of this properties is buffered in the PrePostOutputRequest and the application should have
     * the opportunity to set this property for further print requests after a print request has been enqueued
     * for asynchronous processing.
     * <br>For synchronous processing, this difference should not be relevant. However, keep in mind that PreLine
     * must be reset to an empty string at least at the end of the final part of PrintRecSubtotalAdjustment.
     *
     * @param adjustmentType    Type of adjustment.
     * @param description       Text describing the discount or surcharge.
     * @param amount            Amount or percent amount (in case of percent adjustment).
     * @return PrintRecSubtotalAdjustment object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintRecSubtotalAdjustment printRecSubtotalAdjustment(int adjustmentType, String description, long amount) throws JposException;

    /**
     * Final part of PrintRecSubtotalAdjustment method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintRecSubtotalAdjustment object. This method
     * will be called when the corresponding operation shall be performed, either synchronously or asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintRecSubtotalAdjustment.
     * @throws JposException    If an error occurs.
     */
    public void printRecSubtotalAdjustment(PrintRecSubtotalAdjustment request) throws JposException;

    /**
     * Validation part of PrintRecSubtotalAdjustVoid method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PrinterState is PS_FISCAL_RECEIPT,</li>
     *     <li>CapFiscalReceiptType is false or FiscalReceiptType is RT_SALES, RT_SERVICE, RT_SIMPLE_INVOICE or RT_REFUND,</li>
     *     <li>adjustmentType is one of AT_AMOUNT_DISCOUNT, AT_AMOUNT_SURCHARGE, AT_PERCENTAGE_DISCOUNT, AT_PERCENTAGE_SURCHARGE,
     * AT_COUPON_AMOUNT_DISCOUNT or AT_COUPON_PERCENTAGE_DISCOUNT,</li>
     *     <li>CapSubAmountAdjustment is true or adjustmentType is neither AT_AMOUNT_DISCOUNT nor AT_AMOUNT_SURCHARGE
     * or AT_COUPON_AMOUNT_DISCOUNT,</li>
     *     <li>CapSubPercentAdjustment is true or adjustmentType is neither AT_PERCENTAGE_DISCOUNT nor AT_PERCENTAGE_SURCHARGE
     * or AT_COUPON_PERCENTAGE_DISCOUNT,</li>
     *     <li>CapPositiveSubtotalAdjustment is true or adjustmentType is neither AT_AMOUNT_SURCHARGE nor AT_PERCENTAGE_SURCHARGE,</li>
     *     <li>amount is &ge; 0.</li>
     * </ul>
     * In addition, If AsyncMode is false:
     * <ul>
     *     <li>State is not S_BUSY,</li>
     *     <li>CapCoverSensor is false or CoverOpen is false,</li>
     *     <li>the empty paper sensors of the journal and the station specified by FiscalReceiptStation are not present
     * or their property values (JrnEmpty and RecEmpty or SlpEmpty) are false.</li>
     * </ul>
     * The implementation should sets property PreLine to an empty string. Even if the UPOS specification
     * tells that this shall be done after the command has been executed, this should be done here because the contents
     * of this properties is buffered in the PrePostOutputRequest and the application should have
     * the opportunity to set this property for further print requests after a print request has been enqueued
     * for asynchronous processing.
     * <br>For synchronous processing, this difference should not be relevant. However, keep in mind that PreLine
     * must be reset to an empty string at least at the end of the final part of PrintRecSubtotalAdjustVoid.
     *
     * @param adjustmentType    Type of adjustment.
     * @param amount            Amount or percent amount (in case of percent adjustment).
     * @return PrintRecSubtotalAdjustVoid object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintRecSubtotalAdjustVoid printRecSubtotalAdjustVoid(int adjustmentType, long amount) throws JposException;

    /**
     * Final part of PrintRecSubtotalAdjustVoid method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintRecSubtotalAdjustVoid object. This method
     * will be called when the corresponding operation shall be performed, either synchronously or asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintRecSubtotalAdjustVoid.
     * @throws JposException    If an error occurs.
     */
    public void printRecSubtotalAdjustVoid(PrintRecSubtotalAdjustVoid request) throws JposException;

    /**
     * Validation part of PrintRecTaxID method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PrinterState is PS_FISCAL_RECEIPT_ENDING,</li>
     *     <li>CapFiscalReceiptType is false or FiscalReceiptType is RT_SALES, RT_SERVICE, RT_SIMPLE_INVOICE or RT_REFUND,</li>
     * </ul>
     * In addition, If AsyncMode is false:
     * <ul>
     *     <li>State is not S_BUSY,</li>
     *     <li>CapCoverSensor is false or CoverOpen is false,</li>
     *     <li>the empty paper sensors of the journal and the station specified by FiscalReceiptStation are not present
     * or their property values (JrnEmpty and RecEmpty or SlpEmpty) are false.</li>
     * </ul>
     *
     * @param taxId            Customer identification with identification characters and tax number.
     * @return PrintRecTaxID object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintRecTaxID printRecTaxID(String taxId) throws JposException;

    /**
     * Final part of PrintRecTaxID method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintRecTaxID object. This method
     * will be called when the corresponding operation shall be performed, either synchronously or asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintRecTaxID.
     * @throws JposException    If an error occurs.
     */
    public void printRecTaxID(PrintRecTaxID request) throws JposException;

    /**
     * Validation part of PrintRecTotal method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PrinterState is PS_FISCAL_RECEIPT or PS_FISCAL_RECEIPT_TOTAL,</li>
     *     <li>CapFiscalReceiptType is false or FiscalReceiptType is RT_SALES, RT_SERVICE, RT_SIMPLE_INVOICE or RT_REFUND,</li>
     *     <li>total is &ge; 0,</li>
     *     <li>payment is &ge; 0.</li>
     * </ul>
     * In addition, if CapPredefinedPaymentLines is true, description equals one of the words stored in the
     * PredefinedPaymentLines property (a comma separated list of payment description placeholders).
     * <br>In addition, If AsyncMode is false:
     * <ul>
     *     <li>State is not S_BUSY,</li>
     *     <li>CapCoverSensor is false or CoverOpen is false,</li>
     *     <li>the empty paper sensors of the journal and the station specified by FiscalReceiptStation are not present
     * or their property values (JrnEmpty and RecEmpty or SlpEmpty) are false.</li>
     * </ul>
     * The implementation should sets property PostLine to an empty string. Even if the UPOS specification
     * tells that this shall be done after the command has been executed, this should be done here because the contents
     * of this properties is buffered in the PrePostOutputRequest and the application should have
     * the opportunity to set this property for further print requests after a print request has been enqueued
     * for asynchronous processing.
     * <br>For synchronous processing, this difference should not be relevant. However, keep in mind that PostLine
     * must be reset to an empty string at least at the end of the final part of PrintRecTotal.
     *
     * @param total         Application computed receipt total.
     * @param payment       Amount of payment tendered.
     * @param description   Text description of the payment or the index of a predefined payment description. See
     *                      UPOS method PrintRecTotal.
     * @return PrintRecTotal object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintRecTotal printRecTotal(long total, long payment, String description) throws JposException;

    /**
     * Final part of PrintRecTotal method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintRecTotal object. This method
     * will be called when the corresponding operation shall be performed, either synchronously or asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintRecTotal.
     * @throws JposException    If an error occurs.
     */
    public void printRecTotal(PrintRecTotal request) throws JposException;

    /**
     * Validation part of PrintRecVoid method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PrinterState is PS_FISCAL_RECEIPT or PS_FISCAL_RECEIPT_TOTAL,</li>
     *     <li>CapFiscalReceiptType is false or FiscalReceiptType is RT_SALES, RT_SERVICE, RT_SIMPLE_INVOICE or RT_REFUND,</li>
     *     <li>description is not null and does not contain the reserved word, if any.</li>
     * </ul>
     * In addition, If AsyncMode is false:
     * <ul>
     *     <li>State is not S_BUSY,</li>
     *     <li>CapCoverSensor is false or CoverOpen is false,</li>
     *     <li>the empty paper sensors of the journal and the station specified by FiscalReceiptStation are not present
     * or their property values (JrnEmpty and RecEmpty or SlpEmpty) are false.</li>
     * </ul>
     *
     * @param description   Text describing the void.
     * @return PrintRecVoid object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintRecVoid printRecVoid(String description) throws JposException;

    /**
     * Final part of PrintRecVoid method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintRecVoid object. This method
     * will be called when the corresponding operation shall be performed, either synchronously or asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     * <br>The default implementation should be called within derived methods to ensure that the property
     * PrinterState is updated as expected.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintRecVoid.
     * @throws JposException    If an error occurs.
     */
    public void printRecVoid(PrintRecVoid request) throws JposException;

    /**
     * Validation part of PrintRecVoidItem method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>AllowDeprecatedMethods is true.</li>
     *     <li>Device is enabled,</li>
     *     <li>PrinterState is PS_FISCAL_RECEIPT,</li>
     *     <li>CapFiscalReceiptType is false or FiscalReceiptType is RT_SALES, RT_SERVICE, RT_SIMPLE_INVOICE or RT_REFUND,</li>
     *     <li>adjustmentType is one of AT_PERCENTAGE_DISCOUNT, AT_PERCENTAGE_SURCHARGE, AT_COUPON_PERCENTAGE_DISCOUNT,
     * AT_AMOUNT_DISCOUNT, AT_AMOUNT_SURCHARGE or AT_COUPON_AMOUNT_DISCOUNT,</li>
     *     <li>if CapAmountAdjustment is false, adjustmentType is one of AT_PERCENTAGE_DISCOUNT, AT_PERCENTAGE_SURCHARGE
     * or AT_COUPON_PERCENTAGE_DISCOUNT,</li>
     *     <li>if CapPercentAdjustment is false, adjustmentType is one of AT_AMOUNT_DISCOUNT, AT_AMOUNT_SURCHARGE or
     * AT_COUPON_AMOUNT_DISCOUNT,</li>
     *     <li>if CapPositiveAdjustment is false, adjustmentType is one of AT_AMOUNT_DISCOUNT, AT_COUPON_AMOUNT_DISCOUNT,
     * AT_PERCENTAGE_DISCOUNT, AT_PERCENTAGE_SURCHARGE or AT_COUPON_PERCENTAGE_DISCOUNT,</li>
     *     <li>adjustment, quantity and price are &ge; 0,</li>
     *     <li>description is not null and does not contain the reserved word, if any.</li>
     * </ul>
     * In addition, If AsyncMode is false:
     * <ul>
     *     <li>State is not S_BUSY,</li>
     *     <li>CapCoverSensor is false or CoverOpen is false,</li>
     *     <li>the empty paper sensors of the journal and the station specified by FiscalReceiptStation are not present
     * or their property values (JrnEmpty and RecEmpty or SlpEmpty) are false.</li>
     * </ul>
     * The implementation should sets properties PreLine and PostLine to an empty string. Even if the UPOS specification
     * tells that this shall be done after the command has been executed, this should be done here because the contents
     * of these properties are buffered in the PrePostOutputRequest and the application should have
     * the opportunity to set these properties for further print requests after a print request has been enqueued
     * for asynchronous processing.
     * <br>For synchronous processing, this difference should not be relevant. However, keep in mind that PreLine
     * and PostLine must be reset to an empty string at least at the end of the final part of PrintRecItem.
     *
     * @param description       Text describing the item sold.
     * @param price             Price of the line item.
     * @param quantity          Number of items. If zero, a single item is assumed.
     * @param adjustmentType    Type of adjustment.
     * @param adjustment        Amount of the adjustment (discount or surcharge).
     * @param vatInfo           VAT rate identifier or amount.
     * @return PrintRecItem object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintRecVoidItem printRecVoidItem(String description, long price, int quantity, int adjustmentType, long adjustment, int vatInfo) throws JposException;

    /**
     * Final part of PrintRecVoidItem method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintRecVoidItem object. This method
     * will be called when the corresponding operation shall be performed, either synchronously or asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     * <br>The default implementation should be called within derived methods to ensure that the property
     * PrinterState is updated as expected.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintRecVoid.
     * @throws JposException    If an error occurs.
     */
    public void printRecVoidItem(PrintRecVoidItem request) throws JposException;
}
