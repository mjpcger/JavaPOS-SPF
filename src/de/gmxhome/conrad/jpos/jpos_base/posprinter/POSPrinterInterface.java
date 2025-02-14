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

package de.gmxhome.conrad.jpos.jpos_base.posprinter;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Interface for methods that implement property setter and method calls for the POSPrinter device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter POS Printer.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface POSPrinterInterface extends JposBaseInterface {
    /**
     * Final part of setting CartridgeNotify. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is disabled,</li>
     *     <li>CapJrnCartridgeSensor, CapRecCartridgeSensor or CapSlpCartridgeSensor is not 0,</li>
     *     <li>The new value is one of the predefined values,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or notify equals the previous value of CartridgeNotify.</li>
     * </ul>
     *
     * @param notify New CartridgeNotify value
     * @throws JposException If an error occurs
     */
    public void cartridgeNotify(int notify) throws JposException;

    /**
     * Final part of setting CharacterSet. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapJrnCartridgeSensor, CapRecCartridgeSensor or CapSlpCartridgeSensor is not 0,</li>
     *     <li>The new value is one of the values specified in CharacterSetList.</li>
     * </ul>
     *
     * @param i New character set value
     * @throws JposException If an error occurs
     */
    public void characterSet(int i) throws JposException;

    /**
     * Final part of setting JrnCurrentCartridge. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapJrnPresent is true: New value is 0,</li>
     *     <li>CapJrnPresent is false: New value is one of the cartridge values specified in CapJrnColor.</li>
     * </ul>
     *
     * @param i New color value
     * @throws JposException If an error occurs
     */
    public void jrnCurrentCartridge(int i) throws JposException;

    /**
     * Final part of setting JrnLetterQuality. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapJrnPresent is true.</li>
     * </ul>
     *
     * @param i New JrnLetterQuality value
     * @throws JposException If an error occurs
     */
    public void jrnLetterQuality(boolean i) throws JposException;

    /**
     * Final part of setting JrnLineChars. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapJrnPresent is true,</li>
     *     <li>The new value is &ge; 0 and &le; the maximum value specified in JrnLineCharsList.</li>
     * </ul>
     *
     * @param i New JrnLineChars value
     * @throws JposException If an error occurs
     */
    public void jrnLineChars(int i) throws JposException;

    /**
     * Final part of setting JrnLineHeight. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapJrnPresent is true.</li>
     * </ul>
     *
     * @param i New JrnLineHeight value
     * @throws JposException If an error occurs
     */
    public void jrnLineHeight(int i) throws JposException;

    /**
     * Final part of setting JrnLineSpacing. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapJrnPresent is true.</li>
     * </ul>
     *
     * @param i New JrnLineSpacing value
     * @throws JposException If an error occurs
     */
    public void jrnLineSpacing(int i) throws JposException;

    /**
     * Final part of setting MapCharacterSet. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapMapCharacterSet is true or the new value is false.</li>
     * </ul>
     *
     * @param i New MapCharacterSet value
     * @throws JposException If an error occurs
     */
    public void mapCharacterSet(boolean i) throws JposException;

    /**
     * Final part of setting MapMode. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>The new value is one of MM_DOTS, MM_TWIPS, MM_ENGLISH or MM_METRIC.</li>
     * </ul>
     *
     * @param i New MapMode value
     * @throws JposException If an error occurs
     */
    public void mapMode(int i) throws JposException;

    /**
     * Final part of setting PageModeHorizontalPosition. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PageModeStation is not 0.</li>
     * </ul>
     *
     * @param i New PageModeHorizontalPosition value
     * @throws JposException If an error occurs
     */
    public void pageModeHorizontalPosition(int i) throws JposException;

    /**
     * Final part of setting PageModePrintArea. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PageModeStation is not 0,</li>
     *     <li>The new value consists of 4 non-negative values,</li>
     *     <li>The area does not exceed the paper width specified by RecLineWidth or SlpLineWidth,</li>
     *     <li>If PageModeStation = S_SLIP and SlpMaxLines &gt; 0, the area does not exceed the paper height specified by
     * SlpLineHeight and SlpMaxLines.</li>
     * </ul>
     *
     * @param i New PageModePrintArea value
     * @throws JposException If an error occurs
     */
    public void pageModePrintArea(String i) throws JposException;

    /**
     * Final part of setting PageModePrintDirection. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PageModeStation is not 0,</li>
     *     <li>The new value is one of PD_TOP_TO_BOTTOM, PD_BOTTOM_TO_TOP, PD_RIGHT_TO_LEFT or PD_LEFT_TO_RIGHT.</li>
     * </ul>
     *
     * @param i New PageModePrintDirection value
     * @throws JposException If an error occurs
     */
    public void pageModePrintDirection(int i) throws JposException;

    /**
     * Final part of setting PageModeStation. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>New PageModeStation value is S_RECEIPT or S_SLIP,</li>
     *     <li>The selected station supports page mode (CapRecPageMode or CapSlpPageMode is true).</li>
     * </ul>
     *
     * @param i New PageModeStation value
     * @throws JposException If an error occurs
     */
    public void pageModeStation(int i) throws JposException;

    /**
     * Final part of setting PageModeVerticalPosition. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PageModeStation is not 0.</li>
     * </ul>
     *
     * @param i New PageModeVerticalPosition value
     * @throws JposException If an error occurs
     */
    public void pageModeVerticalPosition(int i) throws JposException;

    /**
     * Final part of setting RecCurrentCartridge. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapRecPresent is true: New value is 0,</li>
     *     <li>CapRecPresent is false: New value is one of the cartridge values specified in CapRecColor.</li>
     * </ul>
     *
     * @param i New color value
     * @throws JposException If an error occurs
     */
    public void recCurrentCartridge(int i) throws JposException;

    /**
     * Final part of setting RecLetterQuality. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapRecPresent is true.</li>
     * </ul>
     *
     * @param i New RecLetterQuality value
     * @throws JposException If an error occurs
     */
    public void recLetterQuality(boolean i) throws JposException;

    /**
     * Final part of setting RecLineChars. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapRecPresent is true,</li>
     *     <li>The new value is &ge; 0 and &le; the maximum value specified in RecLineCharsList.</li>
     * </ul>
     *
     * @param i New RecLineChars value
     * @throws JposException If an error occurs
     */
    public void recLineChars(int i) throws JposException;

    /**
     * Final part of setting RecLineHeight. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapRecPresent is true.</li>
     * </ul>
     *
     * @param i New RecLineHeight value
     * @throws JposException If an error occurs
     */
    public void recLineHeight(int i) throws JposException;

    /**
     * Final part of setting RecLineSpacing. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapRecPresent is true.</li>
     * </ul>
     *
     * @param i New RecLineSpacing value
     * @throws JposException If an error occurs
     */
    public void recLineSpacing(int i) throws JposException;

    /**
     * Final part of setting RotateSpecial. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is not closed,</li>
     *     <li>CapRecBarCode or CapSlpBarCode is true,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or special equals the previous value of RotateSpecial.</li>
     * </ul>
     *
     * @param special New RotateSpecial value
     * @throws JposException If an error occurs
     */
    public void rotateSpecial(int special) throws JposException;

    /**
     * Final part of setting SlpCurrentCartridge. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSlpPresent is true: New value is 0,</li>
     *     <li>CapSlpPresent is false: New value is one of the cartridge values specified in CapSlpColor.</li>
     * </ul>
     *
     * @param i New color value
     * @throws JposException If an error occurs
     */
    public void slpCurrentCartridge(int i) throws JposException;

    /**
     * Final part of setting SlpLetterQuality. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSlpPresent is true.</li>
     * </ul>
     *
     * @param i New SlpLetterQuality value
     * @throws JposException If an error occurs
     */
    public void slpLetterQuality(boolean i) throws JposException;

    /**
     * Final part of setting SlpLineChars. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSlpPresent is true,</li>
     *     <li>The new value is &ge; 0 and &le; the maximum value specified in SlpLineCharsList.</li>
     * </ul>
     *
     * @param i New SlpLineChars value
     * @throws JposException If an error occurs
     */
    public void slpLineChars(int i) throws JposException;

    /**
     * Final part of setting SlpLineHeight. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSlpPresent is true.</li>
     * </ul>
     *
     * @param i New SlpLineHeight value
     * @throws JposException If an error occurs
     */
    public void slpLineHeight(int i) throws JposException;

    /**
     * Final part of setting SlpLineSpacing. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSlpPresent is true.</li>
     * </ul>
     *
     * @param i New SlpLineSpacing value
     * @throws JposException If an error occurs
     */
    public void slpLineSpacing(int i) throws JposException;

    /**
     * Final part of BeginInsertion method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSlpPresent is true,</li>
     *     <li>Asynchronous output or error handling is in progress,</li>
     *     <li>The timeout is &ge; 0 or JPOS_FOREVER.</li>
     * </ul>
     * If the service enters insertion mode successfully after BeginInsertion failed, e.g. due to a
     * timeout condition during delayed asynchronous insertion handling, property InsertionMode must
     * be set to true by the specific service implementation to avoid EndInsertion to fail.
     *
     * @param timeout    See UPOS specification, method BeginInsertion.
     * @throws JposException    See UPOS specification, method DisplayText.
     */
    public void beginInsertion(int timeout) throws JposException;

    /**
     * Final part of BeginRemoval method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSlpPresent is true,</li>
     *     <li>Asynchronous output or error handling is not in progress,</li>
     *     <li>The timeout is &ge; 0 or JPOS_FOREVER.</li>
     * </ul>
     * If the service enters removal mode successfully after BeginRemoval failed, e.g. due to a
     * timeout condition during delayed asynchronous removal handling, property RemovalMode must
     * be set to true by the specific service implementation to avoid EndRemoval to fail.
     *
     * @param timeout    See UPOS specification, method BeginRemoval.
     * @throws JposException    See UPOS specification, method DisplayText.
     */
    public void beginRemoval(int timeout) throws JposException;

    /**
     * Final part of ChangePrintSide method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSlpPresent and CapSlpBothSidesPrint are true,</li>
     *     <li>Asynchronous output or error handling is not in progress,</li>
     *     <li>The new side value is PS_OPPOSITE, PS_SIDE1 or PS_SIDE2.</li>
     * </ul>
     *
     * @param side    See UPOS specification, method ChangePrintSide.
     * @throws JposException    See UPOS specification, method DisplayText.
     */
    public void changePrintSide(int side) throws JposException;

    /**
     * Final part of ClearPrintArea method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PageModeStation in not 0.</li>
     * </ul>
     *
     * @throws JposException    See UPOS specification, method ClearPrintArea.
     */
    public void clearPrintArea() throws JposException;

    /**
     * Final part of EndInsertion method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSlpPresent is true,</li>
     *     <li>Device is not in insertion mode,</li>
     *     <li>Asynchronous output or error handling is not in progress.</li>
     * </ul>
     *
     * @throws JposException    See UPOS specification, method EndInsertion.
     */
    public void endInsertion() throws JposException;

    /**
     * Final part of EndRemoval method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSlpPresent is true,</li>
     *     <li>Device is not in removal mode,</li>
     *     <li>Asynchronous output or error handling is not in progress.</li>
     * </ul>
     *
     * @throws JposException    See UPOS specification, method EndRemoval.
     */
    public void endRemoval() throws JposException;

    /**
     * Validation part of PrintImmediate method. For details, see UPOS specification. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous (high-priority) execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Selected station is S_JOURNAL, S_RECEIPT or S_SLIP,</li>
     *     <li>The cover is closed, paper present, head ready and cartridge neither empty nor removed.</li>
     * </ul>
     * If the service supports page mode but does not support immediate printing in page mode, this method
     * must be overwritten. In this case, it must throw E_ILLEGAL if called in page mode.
     * Keep in mind: Since PrintImmediate prints always synchronously, but page mode printing will start after
     * leaving page mode, PrintImmediate prints outside page mode before page mode printing starts. This is also
     * the case in sideways print mode.
     *
     * @param station           See UPOS specification, method PrintImmediate.
     * @param data              Print data. See UPOS specification, method PrintImmediate.
     * @return PrintImmediate object for use in final part.
     * @throws JposException    See UPOS specification, method PrintImmediate.
     */
    public PrintImmediate printImmediate(int station, String data) throws JposException;

    /**
     * Final part of SetBitmap method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>bitmapNumber is between 1 and 20,</li>
     *     <li>station is S_RECEIPT or S_SLIP,</li>
     *     <li>selected station and bitmap printing on selected station supported,</li>
     *     <li>fileName is not null,</li>
     *     <li>width is BM_ASIS or &gt; 0,</li>
     *     <li>alignment is BM_LEFT, BM_CENTER, BM_RIGHT or &ge; 0.</li>
     * </ul>
     *
     * @param bitmapNumber      See UPOS specification, method SetBitmap.
     * @param station           See UPOS specification, method SetBitmap.
     * @param fileName          See UPOS specification, method SetBitmap.
     * @param width             See UPOS specification, method SetBitmap.
     * @param alignment         See UPOS specification, method SetBitmap.
     * @throws JposException    See UPOS specification, method SetBitmap.
     */
    public void setBitmap(int bitmapNumber, int station, String fileName, int width, int alignment) throws JposException;

    /**
     * Final part of SetLogo method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Selected location is L_TOP or L_BOTTOM,</li>
     *     <li>data is not null.</li>
     * </ul>
     *
     * @param location          See UPOS specification, method SetLogo.
     * @param data              See UPOS specification, method SetLogo.
     * @throws JposException    See UPOS specification, method SetLogo.
     */
    public void setLogo(int location, String data) throws JposException;

    /**
     * Final part of ValidateData method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>station is S_JOURNAL, S_RECEIPT or S_SLIP.</li>
     * </ul>
     * This method will be called before data will be checked using the methods validateData with POSPrinterService.PrintDataPart
     * objects as second parameter. Since these methods should
     * check everything that must be checked, it should not be necessary to overwrite this method. However, if a
     * service overwrites this method, it may throw a JposException with error code 0 to signal that successful
     * validation has been completed and calling validateData with POSPrinterService.PrintDataPart objects representing data shall
     * be suppressed. Otherwise, this method can be used to initialize the implementation object to handle dependencies
     * between all following data parts.
     *
     * @param station           See UPOS specification, method ValidateData.
     * @param data              Print data. See UPOS specification, method ValidateData.
     * @throws JposException    See UPOS specification, method ValidateData.
     */
    public void validateData(int station, String data) throws JposException;

    /**
     * Validate character or escape sequence. Usually, this method simply calls the validation method of the given
     * PrintDataPart object.<br>
     * This method is deprecaded now. It is recommended to use the validateData method of data instead.
     * @param station   POSPrinter station, one of S_JOURNAL, S_RECEIPT or S_SLIP.
     * @param data      Any object derived from PrintDataPart.
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    @Deprecated
    public void validateData(int station, POSPrinterService.PrintDataPart data) throws JposException;

    /**
     * Validate printable character sequence. ESC sequences, CR and LF have been filtered out. Default behavior is that
     * all other characters with character code &le; 0x20 are invalid. If a service should support more characters,
     * this method must be overwritten.<br>
     * Keep in mind that data may contain an empty string. In that case, it marks the end of printable data in
     * cases where the last character was a CR to support post-validation of CR.
     * @param station   POSPrinter station, one of S_JOURNAL, S_RECEIPT or S_SLIP.
     * @param data      Data to be printed.
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    public void validateData(int station, POSPrinterService.PrintData data) throws JposException;

    /**
     * Validate control character. One of CR or LF. Default behavior is that both characters are valid. CR will be
     * passed to ValidateData only once for any sequence of multiple CR in the original data stream and only if LF
     * is neither the previous nor the following character. However, even if the printer supports CR, CR might be
     * valid behind or in case of a following escape sequence.
     * @param station   POSPrinter station, one of S_JOURNAL, S_RECEIPT or S_SLIP.
     * @param ctrl      ControlChar object, representing CR or LF.
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    public void validateData(int station, POSPrinterService.ControlChar ctrl) throws JposException;

    /**
     * Validate cut sequence. For details, see UPOS specification of ESC|[#][x]P. Default behavior is that a service
     * supports only full cut and no cut (CP_FULLCUT and 0 percent). If a service supports more values, this method
     * must be overwritten.
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>The printer station is valid,</li>
     *     <li>Capability CapXxxPapercut is true,</li>
     *     <li>EscCut attribute Stamp is false or CapXxxStamp is true.</li>
     * </ul>
     *
     * @param station   S_RECEIPT.
     * @param esc       EscCut object containing sequence attributes.
     *
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    public void validateData(int station, POSPrinterService.EscCut esc) throws JposException;

    /**
     * Validate ruled line escape sequence. For details, see UPOS specification of ESC|*#dL. All parameters have been checked
     * for plausibility before entering this method as described for each parameter. Can be overwritten by service implementations.
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>The printer station is valid,</li>
     *     <li>The LineDirection attribute is valid in combination with the corresponding capability CapXxxRuledLine,</li>
     *     <li>The LineWidth attribute is &gt; 0,</li>
     *     <li>The LineStyle attribute is one of LS_SINGLE_SOLID_LINE, LS_DOUBLE_SOLID_LINE, LS_BROKEN_LINE or LS_CHAIN_LINE,</li>
     *     <li>The LineColor attribute is one of the colors represented by the bits set in the corresponding CapXxxColor
     *         capability,</li>
     *     <li>The PositionList attribute conforms to the UPOS specification, method DrawRuledLine. Values have been checked
     *         against 0 and XxxLineWidth.</li>
     * </ul>
     * The default implementation is that ruled lines are not supported. If a service supports more values, this method
     * must be overwritten.
     *
     * @param station       A valid printer station, S_RECEIPT, S_JOURNAL or S_SLIP.
     * @param esc           EscRuledLine object containing the sequence attributes.
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    public void validateData(int station, POSPrinterService.EscRuledLine esc) throws JposException;

    /**
     * Validate normalize escape sequence. For details, see UPOS specification of ESC|N. Should always be valid.
     * Can be overwritten by service implementations.
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>The printer station is valid.</li>
     * </ul>
     *
     * @param station       A valid printer station, S_RECEIPT, S_JOURNAL or S_SLIP.
     * @param esc           EscNormalize object containing the sequence attributes.
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    public void validateData(int station, POSPrinterService.EscNormalize esc) throws JposException;

    /**
     * Validate logo sequence. For details, see UPOS specification of ESC|xL. The default behavior is to assume that
     * requests to print a logo are always valid.
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>The printer station is valid.</li>
     * </ul>
     *
     * @param station   A valid printer station, S_RECEIPT, S_JOURNAL or S_SLIP.
     * @param esc       EscLogo object containing sequence attributes.
     *
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    public void validateData(int station, POSPrinterService.EscLogo esc) throws JposException;

    /**
     * Validate stamp sequence. For details, see UPOS specification of ESC|sL. The corresponding
     * capability (CapRecStamp) has been checked and station is S_RECEIPT. The default behavior is to assume that
     * requests to stamp are always valid.
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>The printer station is valid,</li>
     *     <li>CapXxxStanp is true.</li>
     * </ul>
     *
     * @param station   A valid printer station, S_RECEIPT, S_JOURNAL or S_SLIP.
     * @param esc       EscStamp object containing the sequemnce attributes.
     *
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    public void validateData(int station, POSPrinterService.EscStamp esc) throws JposException;

    /**
     * Validate bitmap sequence. For details, see UPOS specification of ESC|#B. Default behavior is that all bitmap
     * numbers are valid if the corresponding capability for the specified printer station is true.
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>The printer station is valid,</li>
     *     <li>CapXxxBitmap is true,</li>
     *     <li>EscBitmap attribute Number is &gt; 0 and &le; 20.</li>
     * </ul>
     *
     * @param station   A valid printer station, S_RECEIPT or S_SLIP.
     * @param esc       EscBitmap object containing the sequence attributes.
     *
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    public void validateData(int station, POSPrinterService.EscBitmap esc) throws JposException;

    /**
     * Validate feed sequence. For details, see UPOS specification of ESC|[#]xF. Default behavior is that all count
     * values are valid.
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>The printer station is valid.</li>
     * </ul>
     *
     * @param station   A valid printer station, S_RECEIPT, S_JOURNAL or S_SLIP.
     * @param esc       EscFeed object containing the sequence attributes.
     *
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    public void validateData(int station, POSPrinterService.EscFeed esc) throws JposException;

    /**
     * Validate embedded data sequence. For details, see UPOS specification of ESC|[*]#E. Default behavior is that
     * the given sequence is valid.
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>The printer station is valid.</li>
     * </ul>
     *
     * @param station   A valid printer station, S_RECEIPT, S_JOURNAL or S_SLIP.
     * @param esc       EscEmbedded object containing sequence attributes.
     *
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    public void validateData(int station, POSPrinterService.EscEmbedded esc) throws JposException;

    /**
     * Validate barcode sequence. For details, see UPOS specification of ESC|[*]#R. A service implementation should
     * check the validity of symbology, height and text position and whether all character in code can be printed
     * using the selected symbology. For example, if symbology is BCS_UPCA, the code must consist only of numeric
     * characters, the length must be 6, 7, 11 or 12 and in case of 7 or 12, the last digit must be a valid check digit.
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>The printer station is valid,</li>
     *     <li>CapXxxBarCode is true,</li>
     *     <li>EscBarcode attribute Symbology is &ge; BCS_UPCA (which is the smallest valid symbology value),</li>
     *     <li>EscBarcode attribute Height is &gt; 0,</li>
     *     <li>EscBarcode attribute Width is &gt; 0 and &le; XxxLineWidth,</li>
     *     <li>EscCarcode attribute TextPosition is one of BC_TEXT_ABOVE, BC_TEXT_BELOW or BC_TEXT_NONE.</li>
     * </ul>
     *
     * @param station   A valid printer station, S_RECEIPT or S_SLIP.
     * @param esc       EscBarcode object containing sequence attributes.
     *
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    public void validateData(int station, POSPrinterService.EscBarcode esc) throws JposException;

    /**
     * Validate font typeface selection sequence. For details, see UPOS specification of ESC|#fT. Default behavior is
     * to support all type fonts specified in FontTypefaceList for all printer stations.
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>The printer station is valid,</li>
     *     <li>EscFontTypeface attribute is between 0 and the number of type face names specified in FontTypefaceList.</li>
     * </ul>
     *
     * @param station   A valid printer station, S_RECEIPT, S_JOURNAL or S_SLIP.
     * @param esc       EscFontTypeface object containing the sequence attributes.
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    public void validateData(int station, POSPrinterService.EscFontTypeface esc) throws JposException;

    /**
     * Validate alignment sequence. For details, see UPOS specification of ESC|xA. Default behavior is that the
     * service supports left, centered and right aligned text.
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>The printer station is valid.</li>
     * </ul>
     *
     * @param station   A valid printer station, S_RECEIPT, S_JOURNAL or S_SLIP.
     * @param esc       EscAlignment object that contains the sequence attributes.
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    public void validateData(int station, POSPrinterService.EscAlignment esc) throws JposException;

    /**
     * Validate print size sequence. For details, see UPOS specification for ESC|xC, ESC|#hC or ESC|#vC. Default behavior
     * is only support for scale = 1 and 2 if the corresponding capabilities for width and height of the specified station
     * are true. If a service supports more scales, this method must be overwritten.
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>The printer station is valid,</li>
     *     <li>If EscScale attribute ScaleValue is 2, CapXxxDhigh, CapXxxDwide or CapXxxDwideDhigh is true if the
     *         corresponding EscScale attribute(s) ScaleHorizontal and / or ScaleVertical is / are true.</li>
     * </ul>
     *
     * @param station       Valid print station
     * @param esc           EscScale object that holds the sequence specific attributes.
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    public void validateData(int station, POSPrinterService.EscScale esc) throws JposException;

    /**
     * Validate attribute setting sequences. For details, see UPOS specification for ESC|[!]bC, ESC|[!]iC, ESC|[!]rvC,
     * ESC|[!]tbC and ESC|[!]tpC. The default behavior is that reverse video, superscript and subscript printing is
     * not supported and resetting an attribute is not supported, too. If a printer supports these attributes, this
     * method must be overwritten.<br>
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>The printer station is valid,</li>
     *     <li>If EscSimple attributes Bold and Activate are true, CapXxxBold is true,</li>
     *     <li>If EscSimple attributes Italic and Activate are true, CapXxxItalic is true.</li>
     * </ul>
     *
     * @param station       Print station, in case of bold ant italic validated.
     * @param esc           Object holding data of simple esc sequence
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    public void validateData(int station, POSPrinterService.EscSimple esc) throws JposException;

    /**
     * Validate combined line printing sequence. For details, see UPOS specification for ESC|[!][#]uC and ESC|[!][#]stC.
     * Default behavior is no support for strike-through and thickness of 1 dot for underline is supported. If a service
     * supports strike-through or another thickness, this method must be overwritten.
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>The printer station is valid,</li>
     *     <li>If EscLine attribute Underline and Activate are true, CapXxxUnderline is true.</li>
     * </ul>
     *
     * @param station       Valid print station
     * @param esc           EscLine object containing the sequence attributes.
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    public void validateData(int station, POSPrinterService.EscLine esc) throws JposException;

    /**
     * Validate color sequences. For details, see UPOS specification for ESC|[#]rC and ESC|[#]fC. Default behavior is
     * that the service supports all colors specified in the 2Color capability of the specified printer station, but
     * no RGB color settings. If a service supports RGB colors, this method must be overwritten.
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>The printer station is valid,</li>
     *     <li>If EscColor attribute Rgb is true, attribute Color has a decimal value of the form rrrgggbbb, where rrr,
     *         ggg and bbb are values between 0 and 255 (inclusive),</li>
     *     <li>If EscColor attribute Rgbis false, attribute Color is one of the color values that form CapXxx2Color.</li>
     * </ul>
     *
     * @param station       Valid printer station
     * @param esc           EscColor object containing the sequence attributes.
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    public void validateData(int station, POSPrinterService.EscColor esc) throws JposException;

    /**
     * Validate shading sequence. For details, see UPOS specification of ESC|[#]sC. Default is no support of shading.
     * Therefore all values other then -1 (default) and 0 are not supported. If a service supports shading, this
     * method must be overwritten.
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>The printer station is valid.</li>
     * </ul>
     *
     * @param station       POSPrinter station.
     * @param esc           EscShade object containing the sequence attributes.
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    public void validateData(int station, POSPrinterService.EscShade esc) throws JposException;

    /**
     * Validate unknown sequence. Default is no support. If a device supports further sequences, this
     * method must be overwritten.
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>The printer station is valid.</li>
     * </ul>
     *
     * @param station       POSPrinter station.
     * @param esc           EscUnknown object containing the sequence attributes.
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    public void validateData(int station, POSPrinterService.EscUnknown esc) throws JposException;

    /**
     * This method will be called whenever print data contain a logo print sequence ESC|tL or ESC|bL. Since it is
     * possible that logos can be changed between the print call and execution of methods for asynchronous print, e.g.
     * when transaction printing will be used, data describing the logo must be retrieved by POSPrinterService whenever
     * an EscLogo object is generated.
     * Every POSPrinter service implementation must overwrite this method because only the specific service stores information about
     * the logo data. However, logo data are of type POSPrinterService.PrintDataPart[],
     * where POSPrinterService.PrintDataPart is one of the objects supported by validateData(int, POSPrinterService.PrintDataPart).
     * As default, this method returns an empty list of objects.
     * @param top   Specifies which logo data shall be filled, true for top logo, false for bottom logo.
     * @return      POSPrinterService.PrintDataPart array containing logo data. Array is empty if the requested logo has not been set.
     */
    public POSPrinterService.PrintDataPart[] getLogoData(boolean top);

    /**
     * Validation part of CutPaper method. For details, see UPOS specification. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapRecPresent and CapRecPapercut are true,</li>
     *     <li>If AsyncMode is false: State is S_IDLE, paper present, cover closed and if present, cartridge is operational.</li>
     * </ul>
     *
     * @param percentage    See UPOS specification, method CutPaper.
     * @return CutPaper object for use in final part.
     * @throws JposException    For details, see UPOS method CutPaper.
     */
    public CutPaper cutPaper(int percentage) throws JposException;

    /**
     * Final part of CutPaper method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a CutPaper object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     * <br>In case of asynchronous processing, the following additional checks have been made before invocation:
     * <ul>
     *     <li>Receipt paper is present.</li>
     *     <li>Cover is closed.</li>
     *     <li>If present, cartridge is operational.</li>
     * </ul>
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by CutPaper.
     * @throws JposException    For details, see UPOS method CutPaper.
     */
    public void cutPaper(CutPaper request) throws JposException;

    /**
     * Validation part of DrawRuledLine method. For details, see UPOS specification. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>If AsyncMode is false: State is S_IDLE, paper present, cover closed and if present, cartridge is operational.</li>
     *     <li>neither transaction mode nor vertical print mode or page mode is active for the specified station,</li>
     *     <li>station, lineDirection, lineWidth, lineStyle, lineColor and positionList have been validated with
     *         validateRuledLine.</li>
     * </ul>
     *
     * @param station       POSPrinter station.
     * @param positionList  Position list.
     * @param lineDirection Line direction.
     * @param lineWidth     Line width in dots.
     * @param lineStyle     Line stype.
     * @param lineColor     Line color.
     * @return DrawRuledLine object for use in final part.
     * @throws JposException    For details, see UPOS method DrawRuledLine.
     */
    public DrawRuledLine drawRuledLine(int station, String positionList, int lineDirection, int lineWidth, int lineStyle, int lineColor) throws JposException;

    /**
     * Final part of DrawRuledLine method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a DrawRuledLine object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     * <br>In case of asynchronous processing, the following additional checks have been made before invocation:
     * <ul>
     *     <li>The station specified by request.getStation() is present.</li>
     *     <li>Cover is closed.</li>
     *     <li>If present, cartridge is operational.</li>
     * </ul>
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by DrawRuledLine.
     * @throws JposException    For details, see UPOS method DrawRuledLine.
     */
    public void drawRuledLine(DrawRuledLine request) throws JposException;

    /**
     * Validation part of MarkFeed method. For details, see UPOS specification. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>neither transaction mode nor vertical print mode or page mode is active,</li>
     *     <li>CapRecPresent is true and CapRecMarkFeed is not 0,</li>
     *     <li>type is MF_TO_TAKEUP, MF_TO_CUTTER, MF_TO_CURRENT_TOF or MF_TO_NEXT_TOF,</li>
     *     <li>CapRecMarkFeed ANDed with type is non-zero,</li>
     *     <li>If AsyncMode is false: State is S_IDLE, paper present, cover closed and if present, cartridge is operational.</li>
     * </ul>
     *
     * @param type  type of mark sensed paper handling. See UPOS specification for method MarkFeed
     * @return MarkFeed object for use in final part.
     * @throws JposException    For details, see UPOS method MarkFeed.
     */
    public MarkFeed markFeed(int type) throws JposException;

    /**
     * Final part of MarkFeed method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a MarkFeed object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     * <br>In case of asynchronous processing, the following additional checks have been made before invocation:
     * <ul>
     *     <li>Receipt paper is present.</li>
     *     <li>Cover is closed.</li>
     *     <li>If present, cartridge is operational.</li>
     * </ul>
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by MarkFeed.
     * @throws JposException    For details, see UPOS method MarkFeed.
     */
    public void markFeed(MarkFeed request) throws JposException;

    /**
     * Validation part of PrintBarCode method. For details, see UPOS specification. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>If AsyncMode is false: State is S_IDLE, paper present, cover closed and if present, cartridge is operational,</li>
     *     <li>station, data, symbology, height, width, alignment and textPosition have been validated with validateData.</li>
     * </ul>
     * If a service supports upside down printing, this method must be overwritten. It must at least be checked
     * whether the service is inside upside down printing mode without activated rotated barcode printing.
     *
     * @param station       Print station, see UPOS method PrintBarCode.
     * @param data          String to be bar coded, see UPOS method PrintBarCode.
     * @param symbology     Symbol type to be used, see UPOS method PrintBarCode.
     * @param height        Bar code height, see UPOS method PrintBarCode.
     * @param width         Bar code width, see UPOS method PrintBarCode.
     * @param alignment     Bar code alignment, see UPOS method PrintBarCode.
     * @param textPosition  HRI text position, see UPOS method PrintBarCode.
     * @return PrintBarCode object for use in final part.
     * @throws JposException    For details, see UPOS method PrintBarCode.
     */
    public PrintBarCode printBarCode(int station, String data, int symbology, int height, int width, int alignment, int textPosition) throws JposException;

    /**
     * Final part of PrintBarCode method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintBarCode object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     * <br>In case of asynchronous processing, the following additional checks have been made before invocation:
     * <ul>
     *     <li>The station specified by request.getStation() is present.</li>
     *     <li>Cover is closed.</li>
     *     <li>If present, cartridge is operational.</li>
     * </ul>
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by PrintBarCode.
     * @throws JposException    For details, see UPOS method PrintBarCode.
     */
    public void printBarCode(PrintBarCode request) throws JposException;

    /**
     * Validation part of PrintBitmap method. For details, see UPOS specification. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>If AsyncMode is false: State is S_IDLE, paper present, cover closed and if present, cartridge is operational,</li>
     *     <li>station is valid (S_RECEIPT or S_SLIP and CapXxxBitmap and CapXxxPresent are true),</li>
     *     <li>fileName is not null,</li>
     *     <li>width is BM_ASIS or a value &gt; 0,</li>
     *     <li>alignment is one of BM_LEFT, BM_CENTER, BM_RIGHT or a positive value.</li>
     * </ul>
     * If a service supports bitmap printing, this method must be overwritten. It must at least be check
     * whether the fileName points to a valid bitmap file. In addition, if the service supports upside down printing,
     * it must check whether the service is inside upside down printing mode without activated rotated bitmap printing.
     *
     * @param station       Print station, see UPOS method PrintBitmap.
     * @param fileName      Bitmap file name, see UPOS method PrintBitmap.
     * @param width         Width, see UPOS method PrintBitmap.
     * @param alignment     Alignment, see UPOS method PrintBitmap.
     * @return PrintBitmap object for use in final part.
     * @throws JposException    For details, see UPOS method PrintBitmap.
     */
    public PrintBitmap printBitmap(int station, String fileName, int width, int alignment) throws JposException;

    /**
     * Final part of PrintBitmap method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintBitmap object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     * <br>In case of asynchronous processing, the following additional checks have been made before invocation:
     * <ul>
     *     <li>The station specified by request.getStation() is present.</li>
     *     <li>Cover is closed.</li>
     *     <li>If present, cartridge is operational.</li>
     * </ul>
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by PrintBitmap.
     * @throws JposException    For details, see UPOS method PrintBitmap.
     */
    public void printBitmap(PrintBitmap request) throws JposException;

    /**
     * Validation part of PrintMemoryBitmap method. For details, see UPOS specification. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>If AsyncMode is false: State is S_IDLE, paper present, cover closed and if present, cartridge is operational,</li>
     *     <li>station is valid (S_RECEIPT or S_SLIP and CapXxxBitmap and CapXxxPresent are true),</li>
     *     <li>data is not null,</li>
     *     <li>type is one of BMT_BMP, BMT_JPEG or BMT_GIF,</li>
     *     <li>width is BM_ASIS or a value &gt; 0,</li>
     *     <li>alignment is one of BM_LEFT, BM_CENTER, BM_RIGHT or a positive value.</li>
     * </ul>
     * If a service supports bitmap printing, this method must be overwritten. It must at least be check
     * whether data is a byte array that matches the given bitmap format. In addition, if the service supports
     * upside down printing, it must check whether the service is inside upside down printing mode without
     * activated rotated bitmap printing.
     *
     * @param station       Print station, see UPOS method PrintMemoryBitmap.
     * @param data          Bitmap data, see UPOS method PrintBitmap.
     * @param type          Bitmap format, see UPOS method PrintBitmap.
     * @param width         Width, see UPOS method PrintMemoryBitmap.
     * @param alignment     Alignment, see UPOS method PrintMemoryBitmap.
     * @return PrintMemoryBitmap object for use in final part.
     * @throws JposException    For details, see UPOS method PrintMemoryBitmap.
     */
    public PrintMemoryBitmap printMemoryBitmap(int station, byte[] data, int type, int width, int alignment) throws JposException;

    /**
     * Final part of PrintMemoryBitmap method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintMemoryBitmap object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     * <br>In case of asynchronous processing, the following additional checks have been made before invocation:
     * <ul>
     *     <li>The station specified by request.getStation() is present.</li>
     *     <li>Cover is closed.</li>
     *     <li>If present, cartridge is operational.</li>
     * </ul>
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by PrintMemoryBitmap.
     * @throws JposException    For details, see UPOS method PrintMemoryBitmap.
     */
    public void printMemoryBitmap(PrintMemoryBitmap request) throws JposException;

    /**
     * Validation part of PrintNormal method. For details, see UPOS specification. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>If AsyncMode is false: State is S_IDLE, paper present, cover closed and if present, cartridge is operational,</li>
     *     <li>station is valid (S_JOURNAL, S_RECEIPT or S_SLIP and the corresponding CapXxxPresent is true),</li>
     *     <li>data is not null.</li>
     * </ul>
     *
     * @param station   Print station, see UPOS method PrintNormal.
     * @param data      Print data. See UPOS method PrintNormal.
     * @return PrintNormal object for use in final part.
     * @throws JposException    For details, see UPOS method PrintNormal.
     */
    public PrintNormal printNormal(int station, String data) throws JposException;

    /**
     * Final part of PrintNormal method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintNormal object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     * <br>In case of asynchronous processing, the following additional checks have been made before invocation:
     * <ul>
     *     <li>The station specified by request.getStation() is present.</li>
     *     <li>Cover is closed.</li>
     *     <li>If present, cartridge is operational.</li>
     * </ul>
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by PrintNormal.
     * @throws JposException    For details, see UPOS method PrintNormal.
     */
    public void printNormal(PrintNormal request) throws JposException;

    /**
     * Validation part of PrintTwoNormal method. For details, see UPOS specification. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>stations is valid (S_JOURNAL_RECEIPT, S_JOURNAL_SLIP, S_RECEIPT_SLIP, TWO_RECEIPT_JOURNAL, TWO_SLIP_JOURNAL
     *         or TWO_SLIP_RECEIPT and the corresponding CapConcurrentXxxYyy property is true),</li>
     *     <li>data1 and data2 are not null,</li>
     *     <li>Non of the two print stations is in page mode, sideways print mode or transaction mode,</li>
     *     <li>If AsyncMode is false: State is S_IDLE, paper present, cover closed and if present, cartridge is operational
     *         for both stations.</li>
     * </ul>
     *
     * @param stations  Print stations, see UPOS method PrintTwoNormal.
     * @param data1     Print data for first or both stations, see UPOS method PrintTwoNormal.
     * @param data2     Print data for second station or empty string, see UPOS method PrintTwoNormal.
     * @return PrintTwoNormal object for use in final part.
     * @throws JposException    For details, see UPOS method PrintTwoNormal.
     */
    public PrintTwoNormal printTwoNormal(int stations, String data1, String data2) throws JposException;

    /**
     * Final part of PrintTwoNormal method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintTwoNormal object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     * <br>In case of asynchronous processing, the following additional checks have been made before invocation:
     * <ul>
     *     <li>The stations specified by request.getStation() are present.</li>
     *     <li>Covers are closed.</li>
     *     <li>If present, cartridges are operational.</li>
     * </ul>
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by PrintTwoNormal.
     * @throws JposException    For details, see UPOS method PrintTwoNormal.
     */
    public void printTwoNormal(PrintTwoNormal request) throws JposException;

    /**
     * Validation part of TransactionPrint method. For details, see UPOS specification. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>station is valid (S_JOURNAL, S_RECEIPT or S_SLIP),</li>
     *     <li>control is one of TP_NORMAL or TP_TRANSACTION,</li>
     *     <li>control is TP_NORMAL if and only if the service is in transaction mode,</li>
     *     <li>If AsyncMode is false: State is S_IDLE, paper present, cover closed and if present, cartridge is operational
     *         for both stations.</li>
     * </ul>
     *
     * @param station           Print station, see UPOS method TransactionPrint.
     * @param control           Control, see UPOS method TransactionPrint.
     * @return TransactionPrint object for use in final part.
     * @throws JposException    For details, see UPOS method TransactionPrint.
     */
    public TransactionPrint transactionPrint(int station, int control) throws JposException;

    /**
     * Final part of TransactionPrint method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a TransactionPrint object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     * <br>In case of asynchronous processing, the following additional checks have been made before invocation:
     * <ul>
     *     <li>The station specified by request.getStation() is present.</li>
     *     <li>Cover is closed.</li>
     *     <li>If present, cartridge is operational.</li>
     * </ul>
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by TransactionPrint.
     * @throws JposException    For details, see UPOS method TransactionPrint.
     */
    public void transactionPrint(TransactionPrint request) throws JposException;

    /**
     * Validation part of RotatePrint method. For details, see UPOS specification. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>station is valid (S_RECEIPT or S_SLIP),</li>
     *     <li>rotation contains one of the rotation values (RP_RIGHT90, RP_LEFT90 or RP_ROTATE180) or is RP_NORMAL,</li>
     *     <li>If not RP_NORMAL: The capability corresponding to station (CapXxxRight90, CapXxxLeft90, CapRotate180)
     *         is true and the station is not in sideways printing mode,</li>
     *     <li>If AsyncMode is false: State is S_IDLE, paper present, cover closed and if present, cartridge is operational
     *         for both stations.</li>
     * </ul>
     *
     * @param station       Print station, see UPOS method RotatePrint.
     * @param rotation      Rotation, see UPOS method RotatePrint.
     * @return RotatePrint object for use in final part.
     * @throws JposException    For details, see UPOS method RotatePrint.
     */
    public RotatePrint rotatePrint(int station, int rotation) throws JposException;

    /**
     * Final part of RotatePrint method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a RotatePrint object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     * <br>In case of asynchronous processing, the following additional checks have been made before invocation:
     * <ul>
     *     <li>The station specified by request.getStation() is present.</li>
     *     <li>Cover is closed.</li>
     *     <li>If present, cartridge is operational.</li>
     * </ul>
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by RotatePrint.
     * @throws JposException    For details, see UPOS method RotatePrint.
     */
    public void rotatePrint(RotatePrint request) throws JposException;

    /**
     * Validation part of PageModePrint method. For details, see UPOS specification. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>PageModeStation is valid (S_RECEIPT or S_SLIP and the correcponding capability CapXxxPageMode is true),</li>
     *     <li>control is one of PM_PAGE_MODE, PM_PRINT_SAVE, PM_NORMAL or PM_CANCEL,</li>
     *     <li>If PM_PAGE_MODE: The station is not in page mode,</li>
     *     <li>If not PM_PAGE_MODE: The station is in page mode,</li>
     *     <li>If AsyncMode is false: State is S_IDLE, paper present, cover closed and if present, cartridge is operational
     *         for both stations.</li>
     * </ul>
     *
     * @param control   Control, see UPOS method PageModePrint.
     * @return PageModePrint object for use in final part.
     * @throws JposException    For details, see UPOS method PageModePrint.
     */
    public PageModePrint pageModePrint(int control) throws JposException;

    /**
     * Final part of PageModePrint method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PageModePrint object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     * <br>In case of asynchronous processing, the following additional checks have been made before invocation:
     * <ul>
     *     <li>The station specified by PageModeStation is present.</li>
     *     <li>Cover is closed.</li>
     *     <li>If present, cartridge is operational.</li>
     * </ul>
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by PageModePrint.
     * @throws JposException    For details, see UPOS method PageModePrint.
     */
    public void pageModePrint(PageModePrint request) throws JposException;
}
