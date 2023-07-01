/*
 * Copyright 2021 Martin Conrad
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
 *
 */

package de.gmxhome.conrad.jpos.jpos_base.pointcardrw;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.JposException;

/**
 * Interface for methods that implement property setter and method calls for the PointCardRW device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Point Card Reader / Writer.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface PointCardRWInterface extends JposBaseInterface {
    /**
     * Final part of setting CharacterSet. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapPrint is true,</li>
     *     <li>code equals one of the values specified in CharacterSetList.</li>
     * </ul>
     *
     * @param code New CharacterSet value
     * @throws JposException If an error occurs
     */
    void characterSet(int code) throws JposException;

    /**
     * Final part of setting LineChars. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapPrint is true,</li>
     *     <li>chars is above zero but not above the maximum of all values specified in LineCharsList.</li>
     * </ul>
     *
     * @param chars New LineChars value
     * @throws JposException If an error occurs
     */
    void lineChars(int chars) throws JposException;

    /**
     * Final part of setting LineHeight. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapPrint is true,</li>
     *     <li>height is above zero.</li>
     * </ul>
     *
     * @param height New LineHeight value
     * @throws JposException If an error occurs
     */
    void lineHeight(int height) throws JposException;

    /**
     * Final part of setting LineSpacing. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapPrint is true,</li>
     *     <li>spacing is above zero.</li>
     * </ul>
     *
     * @param spacing New LineSpacing value
     * @throws JposException If an error occurs
     */
    void lineSpacing(int spacing) throws JposException;

    /**
     * Final part of setting MapCharacterSet. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapPrint is true,</li>
     *     <li>CapMapCharacterSet is true or flag is false.</li>
     * </ul>
     *
     * @param flag New MapCharacterSet value
     * @throws JposException If an error occurs
     */
    void mapCharacterSet(boolean flag) throws JposException;

    /**
     * Final part of setting MapMode. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>mode is one of MM_DOTS, MM_TWIPS, MM_ENGLISH and MM_METRIC.</li>
     * </ul>
     * Keep in mind: Even if MapMode can be set to one of the predefined values, it will be ignored if
     * CapPrintMode is false.
     *
     * @param mode New MapMode value
     * @throws JposException If an error occurs
     */
    void mapMode(int mode) throws JposException;

    /**
     * Final part of setting TracksToRead. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Only tracks that have been specified in CapTracksToRead are specified in tracks.</li>
     * </ul>
     *
     * @param tracks New TracksToRead value
     * @throws JposException If an error occurs
     */
    void tracksToRead(int tracks) throws JposException;

    /**
     * Final part of setting TracksToWrite. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Only tracks that have been specified in CapTracksToWrite are specified in tracks.</li>
     * </ul>
     *
     * @param tracks New TracksToWrite value
     * @throws JposException If an error occurs
     */
    void tracksToWrite(int tracks) throws JposException;

    /**
     * Final part of setting Write1Data. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Bit PCRW_TRACK1 is set in TracksToWrite.</li>
     * </ul>
     *
     * @param trackdata New Write1Data value
     * @throws JposException If an error occurs
     */
    void write1Data(String trackdata) throws JposException;

    /**
     * Final part of setting Write2Data. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Bit PCRW_TRACK2 is set in TracksToWrite.</li>
     * </ul>
     *
     * @param trackdata New Write2Data value
     * @throws JposException If an error occurs
     */
    void write2Data(String trackdata) throws JposException;

    /**
     * Final part of setting Write3Data. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Bit PCRW_TRACK3 is set in TracksToWrite.</li>
     * </ul>
     *
     * @param trackdata New Write3Data value
     * @throws JposException If an error occurs
     */
    void write3Data(String trackdata) throws JposException;

    /**
     * Final part of setting Write4Data. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Bit PCRW_TRACK4 is set in TracksToWrite.</li>
     * </ul>
     *
     * @param trackdata New Write4Data value
     * @throws JposException If an error occurs
     */
    void write4Data(String trackdata) throws JposException;

    /**
     * Final part of setting Write5Data. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Bit PCRW_TRACK5 is set in TracksToWrite.</li>
     * </ul>
     *
     * @param trackdata New Write5Data value
     * @throws JposException If an error occurs
     */
    void write5Data(String trackdata) throws JposException;

    /**
     * Final part of setting Write6Data. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Bit PCRW_TRACK6 is set in TracksToWrite.</li>
     * </ul>
     *
     * @param trackdata New Write6Data value
     * @throws JposException If an error occurs
     */
    void write6Data(String trackdata) throws JposException;

    /**
     * Final part of BeginInsertion method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>No asynchronous output is in progress,</li>
     *     <li>timeout is above zero or FOREVER.</li>
     * </ul>
     *
     * @param timeout Maximum time to wait for card insertion.
     * @throws JposException If an error occurs.
     */
    void beginInsertion(int timeout) throws JposException;

    /**
     * Final part of BeginRemoval method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>No asynchronous output is in progress,</li>
     *     <li>timeout is above zero or FOREVER.</li>
     * </ul>
     *
     * @param timeout Maximum time to wait for card insertion.
     * @throws JposException If an error occurs.
     */
    void beginRemoval(int timeout) throws JposException;

    /**
     * Final part of DestroyWindow method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapCleanCard is true.</li>
     * </ul>
     *
     * @throws JposException If an error occurs.
     */
    void cleanCard() throws JposException;

    /**
     * Final part of ClearPrintWrite method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>No asynchronous output is in progress,</li>
     *     <li>kind is 1, 2 or 3,</li>
     *     <li>if kind is 1 or 3:<ul>
     *         <li>CapClearPrint is true,</li>
     *         <li>hposition and vposition are positive values,</li>
     *         <li>width and height are above -2.</li>
     *     </ul>
     *     </li>
     * </ul>
     *
     * @param kind      Parts of the point card that will be cleared. See UPOS specification.
     * @param hposition The horizontal start position for erasing the printing area.
     * @param vposition The vertical start position for erasing the printing area.
     * @param width     The width used for erasing the printing area.
     * @param height    The height used for erasing the printing area.
     * @throws JposException If an error occurs.
     */
    void clearPrintWrite(int kind, int hposition, int vposition, int width, int height) throws JposException;

    /**
     * Final part of EndInsertion method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     *
     * @throws JposException If an error occurs.
     */
    void endInsertion() throws JposException;

    /**
     * Final part of EndRemoval method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     *     <li></li>
     * </ul>
     *
     * @throws JposException If an error occurs.
     */
    void endRemoval() throws JposException;

    /**
     * Final part of RotatePrint method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapPrint is true,</li>
     *     <li>rotation is one of RP_NORMAL, RP_RIGHT90, RP_LEFT90 and RP_ROTATE180,</li>
     *     <li>The corresponding capability, CapRight90, CapLeft90 or CapRotate180, is true.</li>
     * </ul>
     *
     * @param rotation Requested rotation.
     * @throws JposException If an error occurs.
     */
    void rotatePrint(int rotation) throws JposException;

    /**
     * Final part of DestroyWindow method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>No asynchronous output is in progress,</li>
     *     <li>CapPrint is true.</li>
     * </ul>
     * This method can throw a JposException with ErrorCode = 0 to signal that full validity check has token place.
     * If it does not throw a JposException, method PointCardRWService.outputDataParts will be used to parse data and
     * split it into a list of PointCardRWService.PrintDataPart objects. Afterwards, method
     * PointCardRWService.plausibilityCheckData will be used to check the validity of all objects returned.
     * <br>The service developer has the choice:
     * <ul>
     *     <li>data can be parsed to check whether whether parts are valid or contain
     *     conditions that require to throw a JposException with ErrorCode E_ILLEGAL or E_FAILURE and if not, to throw a
     *     JposException with ErrorCode 0. In this case, the other validateData methods should not be overriden.</li>
     *     <li>This method performs nothing. In that case, validation occurs for each escape sequence via the corresponding
     *     validateData method, and the other validation routines must be implemented if the default behavior do not
     *     match the needs.</li>
     * </ul>
     * @param data Print data to be checked for validity.
     * @throws JposException If an error occurs.
     */
    void validateData(String data) throws JposException;

    /**
     * Validation part of PrintWrite method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>kind is 1, 2 or 3,</li>
     *     <li>if kind is 1 or 3:<ul>
     *         <li>CapPrint is true,</li>
     *         <li>hposition and vposition are positive values.</li>
     *     </ul>
     *     </li>
     *     <li>if kind is 2 or 3:
     *     <ul>
     *         <li>Property TracksToWrite contains only tracks that are contained in CapTracksToWrite as well,</li>
     *         <li>The corresponding Write<i>x</i>Data properties are not empty strings.</li>
     *     </ul>
     *     </li>
     * </ul>
     *
     * @param kind      Parts of the point card that will be written or printed. See UPOS specification.
     * @param hposition The horizontal start position for printing.
     * @param vposition The vertical start position for printing.
     * @param data      Print data.
     * @throws JposException If an error occurs.
     * @return PrintWrite object for use in final part.
     */
    PrintWrite printWrite(int kind, int hposition, int vposition, String data) throws JposException;

    /**
     * Final part of PrintWrite method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintWrite object. This method will be performed asynchronously.
     * All plausibility checks have been made before, only runtime errors can occur.
     * <br>If this method performs a write operation, it must set the write status of each track via method setWriteState
     * of the give request to ensure that property WriteState1 and WriteState2 will be set correctly in error case.
     * Otherwise, the status of all tracks specified via TracksToWrite will be set to SUCCESS on success and to E_FAILURE
     * on failure.
     *
     * @param request   Output request object returned by validation method that contains all parameters
     *                  to be used by PrintWrite.
     * @throws JposException If an error occurs.
     */
    void printWrite(PrintWrite request) throws JposException;

    /**
     * Validate unknown sequence. Default is no support. If a device supports further sequences, this
     * method must be overwritten.
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>CapPrint is true.</li>
     * </ul>
     *
     * @param printData           EscUnknown object containing the sequence attributes.
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    void validateData(PointCardRWService.EscUnknown printData) throws JposException;

    /**
     * Validate printable character sequence. ESC sequences have been filtered out. Default behavior is that
     * all other characters with character code &le; 0x20 are invalid. If a service should support more characters,
     * this method must be overwritten.<br>
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>CapPrint is true.</li>
     * </ul>
     * @param printData      Data to be printed.
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    void validateData(PointCardRWService.PrintData printData) throws JposException;

    /**
     * Validate embedded data sequence. For details, see UPOS specification of ESC|#E. Default behavior is that
     * the given sequence is valid.
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>CapPrint is true.</li>
     * </ul>
     *
     * @param escEmbedded       EscEmbedded object containing sequence attributes.
     *
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    void validateData(PointCardRWService.EscEmbedded escEmbedded) throws JposException;

    /**
     * Validate font typeface selection sequence. For details, see UPOS specification of ESC|#fT. Default behavior is
     * to support all type fonts specified in FontTypefaceList.
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>CapPrint is true,</li>
     *     <li>EscFontTypeface attribute is between 0 and the number of type face names specified in FontTypefaceList.</li>
     * </ul>
     *
     * @param escFontTypeface       EscFontTypeface object containing the sequence attributes.
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    void validateData(PointCardRWService.EscFontTypeface escFontTypeface) throws JposException;

    /**
     * Validate alignment sequence. For details, see UPOS specification of ESC|xA. Default behavior is that the
     * service supports centered and right aligned text.
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>CapPrint is true.</li>
     * </ul>
     *
     * @param escAlignment       EscAlignment object that contains the sequence attributes.
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    void validateData(PointCardRWService.EscAlignment escAlignment) throws JposException;

    /**
     * Validate normalize escape sequence. For details, see UPOS specification of ESC|N. Should always be valid.
     * Can be overwritten by service implementations.
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>CapPrint is true.</li>
     * </ul>
     *
     * @param escNormalize           EscNormalize object containing the sequence attributes.
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    void validateData(PointCardRWService.EscNormalize escNormalize) throws JposException;

    /**
     * Validate attribute setting sequences. For details, see UPOS specification for ESC|bC, ESC|iC and ESC|rvC.
     * The default behavior is that reverse video is not supported. If a printer supports these attributes, this
     * method must be overwritten.<br>
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>CapPrint is true,</li>
     *     <li>If EscSimple attribute Bold is true, CapBold is true,</li>
     *     <li>If EscSimple attribute Italic is true, CapItalic is true.</li>
     * </ul>
     *
     * @param escSimple           Object holding data of simple esc sequence
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    void validateData(PointCardRWService.EscSimple escSimple) throws JposException;

    /**
     * Validate combined underline printing sequence. For details, see UPOS specification for ESC|[#]uC.
     * Default behavior is support for thickness of 1 dot for underline only. If a service
     * does not support underline or supports another thickness, this method must be overwritten.
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>CapPrint is true.</li>
     * </ul>
     *
     * @param escUnderline           EscUnderline object containing the sequence attributes.
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    void validateData(PointCardRWService.EscUnderline escUnderline) throws JposException;

    /**
     * Validate print scale sequence. For details, see UPOS specification for ESC|xC, ESC|#hC or ESC|#vC. Default behavior
     * is only support for scale = 1 and 2 if the corresponding capabilities for width and height of the specified station
     * are true. If a service supports more scales, this method must be overwritten.
     * The following plausibility checks will be made before this method will be called:
     * <ul>
     *     <li>CapPrint is true.</li>
     *     <li>If EscScale attribute ScaleValue is above 1, CapDhigh, CapDwide or CapDwideDhigh is true if the
     *         corresponding EscScale attribute(s) ScaleHorizontal and / or ScaleVertical is / are true.</li>
     * </ul>
     *
     * @param escScale           EscScale object that holds the sequence specific attributes.
     * @throws JposException    For details, see UPOS method ValidateData.
     */
    void validateData(PointCardRWService.EscScale escScale) throws JposException;
}
