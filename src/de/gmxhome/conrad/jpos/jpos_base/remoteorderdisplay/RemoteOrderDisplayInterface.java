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

package de.gmxhome.conrad.jpos.jpos_base.remoteorderdisplay;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Interface for methods that implement property setter and method calls for the RemoteOrderDisplay device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Remote Order Display.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface RemoteOrderDisplayInterface extends JposBaseInterface {
    /**
     * Final part of setting AutoToneDuration. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>The display specified by CurrentUnitID is online,</li>
     *     <li>The new property value is positive.</li>
     * </ul>
     *
     * @param duration New AutoToneDuration value
     * @throws JposException If an error occurs
     */
    public void autoToneDuration(int duration) throws JposException;

    /**
     * Final part of setting AutoToneFrequency. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>The display specified by CurrentUnitID is online,</li>
     *     <li>The new property value is positive.</li>
     * </ul>
     *
     * @param frequency New AutoToneFrequency value
     * @throws JposException If an error occurs
     */
    public void autoToneFrequency(int frequency) throws JposException;

    /**
     * Final part of setting CurrentUnitID. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>The new property value is one of UID_x, where 1 &le; x &le; 32.</li>
     * </ul>
     *
     * @param unit New CurrentUnitID value
     * @throws JposException If an error occurs
     */
    public void currentUnitID(int unit) throws JposException;

    /**
     * Final part of setting EventType. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has been opened,</li>
     *     <li>The new property value is a bitwise combination of DE_TOUCH_UP, DE_TOUCH_DOWN and DE_TOUCH_MOVE.</li>
     * </ul>
     * <br>-
     * <br>-
     *
     * @param type New EventType value
     * @throws JposException If an error occurs
     */
    public void eventType(int type) throws JposException;

    /**
     * Final part of setting MapCharacterSet. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has been opened,</li>
     *     <li>The new property value is false of CapMapCharacterSet is true.</li>
     * </ul>
     *
     * @param map New MapCharacterSet value
     * @throws JposException If an error occurs
     */
    public void mapCharacterSet(boolean map) throws JposException;

    /**
     * Final part of setting Timeout. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has been opened,</li>
     *     <li>The new property value is positive.</li>
     * </ul>
     *
     * @param milliseconds New Timeout value
     * @throws JposException If an error occurs
     */
    public void timeout(int milliseconds) throws JposException;

    /**
     * Final part of setting video mode. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>The device unit specified by CurrentUnitID is online,</li>
     *     <li>The new property value one of the modes specified by VideoModesList.</li>
     * </ul>
     *
     * @param mode New video mode value
     * @throws JposException If an error occurs
     */
    public void videoMode(int mode) throws JposException;

    /**
     * Final part of ControlClock method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Device is idle</li>
     *     <li>All displays specified by units are online,</li>
     *     <li>function is CLK_PAUSE, CLK_START, CLK_RESUME, CLK_STOP or CLK_MOVE,</li>
     *     <li>clockid is &gt; 0 and %le; the minimum Clocks value for all selected display units,</li>
     *     <li>if function = CLK_START, hour, minute and second are within the usual ranges (0-23, 0-59),</li>
     *     <li>if function = CLK_START, mode is one of CLK_SHORT, CLK_NORMAL, CLK_12_LONG or CLK_24_LONG,</li>
     *     <li>if function = CLK_START, attribute is between 0 and 255,</li>
     *     <li>if funktion is CLK_START or CLK_MOVE, row and column are valid start coordinates for all specified units.</li>
     * </ul>
     *
     * @param units     Bitwise mask indicating which video unit(s) to operate on.
     * @param function  The requested clock command.
     * @param clockid   The requested clock command.
     * @param hour      The initial hours for the clock display.
     * @param minute    The initial minutes for the clock display.
     * @param second    The initial seconds for the clock display.
     * @param row       The clock's row.
     * @param column    The clock's start column.
     * @param attribute VGA-like attribute parameter, see UPOS specification, chapter Remote Order Display - General
     *                  Information - Model.
     * @param mode      The type of clock to display.
     * @throws JposException    For details, see UPOS method ControlClock.
     */
    public void controlClock(int units, int function, int clockid, int hour, int minute, int second, int row, int column, int attribute, int mode) throws JposException;

    /**
     * Final part of ControlCursor method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Device is idle</li>
     *     <li>All displays specified by units are online,</li>
     *     <li>function is CRS_LINE, CRS_LINE_BLINK, RS_BLOCK, CRS_BLOCK_BLINK or CRS_OFF.</li>
     * </ul>
     *
     * @param units     Bitwise mask indicating which video unit(s) to operate on.
     * @param function  The cursor command, indicating the type of cursor to display.
     * @throws JposException    For details, see UPOS method ControlCursor.
     */
    public void controlCursor(int units, int function) throws JposException;

    /**
     * Final part of FreeVideoRegion method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>All displays specified by units are online,</li>
     *     <li>All displays specified by units fulfill the following condition: 1 &le; bufferId &le; VideoSaveBuffers.</li>
     * </ul>
     * Keep in mind: VideoSaveBuffers can differ for different units.
     *
     * @param units     Bitwise mask indicating which video unit(s) to operate on.
     * @param bufferId  Number identifying the video buffer to free.
     * @throws JposException    For details, see UPOS method FreeVideoRegion.
     */
    public void freeVideoRegion(int units, int bufferId) throws JposException;

    /**
     * Final part of ResetVideo method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Device is idle</li>
     *     <li>All displays specified by units are online.</li>
     * </ul>
     *
     * @param units     Bitwise mask indicating which video unit(s) to operate on.
     * @throws JposException    For details, see UPOS method ResetVideo.
     */
    public void resetVideo(int units) throws JposException;

    /**
     * Final part of SelectChararacterSet method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Device is idle</li>
     *     <li>All displays specified by units are online,</li>
     *     <li>For all displays specified by units property CapSelectCharacterSet is true,</li>
     *     <li>For all displays specified by units, property CharacterSetList contains characterSet .</li>
     * </ul>
     * Keep in mind: CapSelectCharacterSet and CharacterSetList can differ for different units.
     *
     * @param units         Bitwise mask indicating which video unit(s) to operate on.
     * @param characterSet  Contain the character set for displaying characters.
     * @throws JposException    For details, see UPOS method SelectChararacterSet.
     */
    public void selectChararacterSet(int units, int characterSet) throws JposException;

    /**
     * Final part of SetCursor method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Device is idle,</li>
     *     <li>All displays specified by units are online,</li>
     *     <li>For all displays specified by units, row and column are valid cursor coordinates.</li>
     * </ul>
     *
     * @param units     Bitwise mask indicating which video unit(s) to operate on.
     * @param row       Row to place the cursor on.
     * @param column    Column to place the cursor on.
     * @throws JposException    For details, see UPOS method SetCursor.
     */
    public void setCursor(int units, int row, int column) throws JposException;

    /**
     * Validation part of ClearVideo method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>0 &le; attribute &le; 0xff,</li>
     *     <li>If AsyncMode is false: All display units specified by units are online.</li>
     * </ul>
     *
     * @param units         Bitwise mask indicating which video unit(s) to operate on.
     * @param attribute     VGA-like attribute parameter, see UPOS specification, chapter Remote Order Display - General
     *                      Information - Model.
     * @return ClearVideo object for use in final part.
     * @throws JposException    For details, see UPOS method ClearVideo.
     */
    public ClearVideo clearVideo(int units, int attribute) throws JposException;

    /**
     * Final part of ClearVideo method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a ClearVideo object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before.<br>
     * In case of asynchronous processing, the following additional checks have been made before invocation:
     * <ul>
     *     <li>All display units specified by request.getUnits() are online.</li>
     * </ul>
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by ClearVideo.
     * @throws JposException    For details, see UPOS method ClearVideo.
     */
    public void clearVideo(ClearVideo request) throws JposException;

    /**
     * Validation part of ClearVideoRegion method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>row and column are positive values, height &ge; 1 and width &ge; 1.</li>
     *     <li>0 &le; attribute &le; 0xff.</li>
     * </ul>
     * If AsyncMode is false, the following conditions have been checked, too:
     * <ul>
     *     <li>All display units specified by units are online,</li>
     *     <li>row, column, height and width specify a valid region on all affected units.</li>
     * </ul>
     *
     * @param units     Bitwise mask indicating which video unit(s) to operate on.
     * @param row       Upper row of the specified region.
     * @param column    Left column of the specified region.
     * @param height    Height of the specified region.
     * @param width     Width of the specified region.
     * @param attribute VGA-like attribute parameter, see UPOS specification, chapter Remote Order Display - General
     *                  Information - Model.
     * @return ClearVideoRegion object for use in final part.
     * @throws JposException    For details, see UPOS method ClearVideoRegion.
     */
    public ClearVideoRegion clearVideoRegion(int units, int row, int column, int height, int width, int attribute) throws JposException ;

    /**
     * Final part of ClearVideoRegion method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a ClearVideoRegion object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.<br>
     * In case of asynchronous processing, the following additional checks have been made before invocation:
     * <ul>
     *     <li>All display units specified by request.getUnits() are online.</li>
     *     <li>request.getRow(), request.getColumn(), request.getHeight() and request.getWidth() specify a valid region
     *     on all affected units.</li>
     * </ul>
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by ClearVideoRegion.
     * @throws JposException    For details, see UPOS method ClearVideoRegion.
     */
    public void clearVideoRegion(ClearVideoRegion request) throws JposException;

    /**
     * Validation part of CopyVideoRegion method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>row, column, targetRow and targetColumn are positive values, height &ge; 1 and width &ge; 1.</li>
     * </ul>
     * If AsyncMode is false, the following conditions have been checked, too:
     * <ul>
     *     <li>All display units specified by units are online,</li>
     *     <li>row, column, height and width specify a valid region on all affected units,</li>
     *     <li>targetRow, targetColumn, height and width specify a valid region on all affected units.</li>
     * </ul>
     *
     * @param units        Bitwise mask indicating which video unit(s) to operate on.
     * @param row          Upper row of the specified region.
     * @param column       Left column of the specified region.
     * @param height       Height of the specified region.
     * @param width        Width of the specified region.
     * @param targetRow    Upper row of target location.
     * @param targetColumn Left column of target location.
     * @return CopyVideoRegion object for use in final part.
     * @throws JposException    For details, see UPOS method CopyVideoRegion.
     */
    public CopyVideoRegion copyVideoRegion(int units, int row, int column, int height, int width, int targetRow, int targetColumn) throws JposException;

    /**
     * Final part of CopyVideoRegion method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a CopyVideoRegion object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.<br>
     * In case of asynchronous processing, the following additional checks have been made before invocation:
     * <ul>
     *     <li>All display units specified by request.getUnits() are online.</li>
     *     <li>request.getRow(), request.getColumn(), request.getHeight() and request.getWidth() specify a valid region
     *     on all affected units.</li>
     *     <li>request.getTargetRow(), request.getTargetColumn(), request.getHeight() and request.getWidth() specify a
     *     valid region on all affected units.</li>
     * </ul>
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by CopyVideoRegion.
     * @throws JposException    For details, see UPOS method CopyVideoRegion.
     */
    public void copyVideoRegion(CopyVideoRegion request) throws JposException;

    /**
     * Validation part of DisplayData method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>row and column are positive values,</li>
     *     <li>0 &le; attribute &le; 0xff.</li>
     * </ul>
     * If AsyncMode is false, the following conditions have been checked, too:
     * <ul>
     *     <li>All display units specified by units are online,</li>
     *     <li>row and column specify a valid coordinate on all affected units.</li>
     * </ul>
     *
     * @param units  Units where status has been changed.
     * @param row    (Upper) Row where operation shall start.
     * @param column (Left) Column where operation shall start.
     * @param attribute VGA-like attribute parameter, see UPOS specification, chapter Remote Order Display - General
     *                Information - Model.
     * @param data    Text to be displayed.
     * @return DisplayData object for use in final part.
     * @throws JposException    For details, see UPOS method DisplayData.
     */
    public DisplayData displayData(int units, int row, int column, int attribute, String data) throws JposException;

    /**
     * Final part of DisplayData method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a DisplayData object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.<br>
     * In case of asynchronous processing, the following additional checks have been made before invocation:
     * <ul>
     *     <li>All display units specified by units are online,</li>
     *     <li>request.getRow() and request.getColumn() specify a valid coordinate on all affected units.</li>
     * </ul>
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by DisplayData.
     * @throws JposException    For details, see UPOS method DisplayData.
     */
    public void displayData(DisplayData request) throws JposException;

    /**
     * Validation part of DrawBox method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>row and column are positive values, height &ge; 1 and width &ge; 1,</li>
     *     <li>0 &le; attribute &le; 0xff,</li>
     *     <li>bordertype is one of BDR_SINGLE, BDR_DOUBLE or BDR_SOLID.</li>
     * </ul>
     * If AsyncMode is false, the following conditions have been checked, too:
     * <ul>
     *     <li>All display units specified by units are online,</li>
     *     <li>row, column, height and width specify a valid region on all affected units.</li>
     * </ul>
     *
     * @param units     Bitwise mask indicating which video unit(s) to operate on.
     * @param row       Upper row of the specified region.
     * @param column    Left column of the specified region.
     * @param height    Height of the specified region.
     * @param width     Width of the specified region.
     * @param attribute VGA-like attribute parameter, see UPOS specification, chapter Remote Order Display - General
     *                  Information - Model.
     * @param bordertype The border type to be drawn.
     * @return DrawBox object for use in final part.
     * @throws JposException    For details, see UPOS method ClearVideoRegion.
     */
    public DrawBox drawBox(int units, int row, int column, int height, int width, int attribute, int bordertype) throws JposException;

    /**
     * Final part of DrawBox method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a DrawBox object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.<br>
     * In case of asynchronous processing, the following additional checks have been made before invocation:
     * <ul>
     *     <li>All display units specified by request.getUnits() are online.</li>
     *     <li>request.getRow(), request.getColumn(), request.getHeight() and request.getWidth() specify a valid region
     *     on all affected units.</li>
     * </ul>
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by DrawBox.
     * @throws JposException    For details, see UPOS method DrawBox.
     */
    public void drawBox(DrawBox request) throws JposException;

    /**
     * Validation part of RestoreVideoRegion method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>targetRow and targetColumn are positive values,</li>
     *     <li>1 &le; bufferId.</li>
     * </ul>
     * If AsyncMode is false, the following conditions have been checked, too:
     * <ul>
     *     <li>All display units specified by units are online,</li>
     *     <li>row and column specify a valid coordinate on all affected units,</li>
     *     <li>bufferId &le; VideoSaveBuffers.</li>
     * </ul>
     *
     * @param units        Bitwise mask indicating which video unit(s) to operate on.
     * @param targetRow    Upper row of target location.
     * @param targetColumn Left column of target location.
     * @param bufferId     Number identifying the source video buffer to use.
     * @return RestoreVideoRegion object for use in final part.
     * @throws JposException    For details, see UPOS method RestoreVideoRegion.
     */
    public RestoreVideoRegion restoreVideoRegion(int units, int targetRow, int targetColumn, int bufferId) throws JposException;

    /**
     * Final part of RestoreVideoRegion method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a RestoreVideoRegion object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.<br>
     * In case of asynchronous processing, the following additional checks have been made before invocation:
     * <ul>
     *     <li>All display units specified by request.getUnits() are online.</li>
     *     <li>request.getTargetRow() and request.getTargetColumn() specify a valid coordinate on all affected units.</li>
     *     <li>request.getBufferId() &le; VideoSaveBuffers.</li>
     * </ul>
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by RestoreVideoRegion.
     * @throws JposException    For details, see UPOS method RestoreVideoRegion.
     */
    public void restoreVideoRegion(RestoreVideoRegion request) throws JposException;

    /**
     * Validation part of SaveVideoRegion method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>row and column are positive values, height &ge; 1 and width &ge; 1.</li>
     *     <li>1 &le; bufferId.</li>
     * </ul>
     * If AsyncMode is false, the following conditions have been checked, too:
     * <ul>
     *     <li>All display units specified by units are online,</li>
     *     <li>row, column, height and width specify a valid region on all affected units,</li>
     *     <li>bufferId &le; VideoSaveBuffers for all specified units.</li>
     * </ul>
     *
     * @param units    Bitwise mask indicating which video unit(s) to operate on.
     * @param row      Upper row of the specified region.
     * @param column   Left column of the specified region.
     * @param height   Height of the specified region.
     * @param width    Width of the specified region.
     * @param bufferId Number identifying the video buffer to use.
     * @return SaveVideoRegion object for use in final part.
     * @throws JposException    For details, see UPOS method SaveVideoRegion.
     */
    public SaveVideoRegion saveVideoRegion(int units, int row, int column, int height, int width, int bufferId) throws JposException;

    /**
     * Final part of SaveVideoRegion method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a SaveVideoRegion object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.<br>
     * In case of asynchronous processing, the following additional checks have been made before invocation:
     * <ul>
     *     <li>All display units specified by request.getUnits() are online.</li>
     *     <li>request.getRow(), request.getColumn(), request.getHeight() and request.getWidth() specify a valid region
     *     on all affected units.</li>
     *     <li>request.getBufferId() &le; VideoSaveBuffers.</li>
     * </ul>
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by SaveVideoRegion.
     * @throws JposException    For details, see UPOS method SaveVideoRegion.
     */
    public void saveVideoRegion(SaveVideoRegion request) throws JposException;

    /**
     * Validation part of TransactionDisplay method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>function is one of TD_TRANSACTION or TD_NORMAL.</li>
     * </ul>
     * If function is TD_TRANSACTION, the following condition has been checked as well:
     * <ul>
     *     <li>No transaction is active.</li>
     * </ul>
     * If function is TD_NORMAL, the following condition has been checked, too:
     * <ul>
     *     <li>A transaction is in progress.</li>
     *     <li>If AsyncMode is false, the following condition has been checked as well:<br>
     *         <b>-</b> All display units specified by units are online.</li>
     *
     * </ul>
     *
     * @param units     Bitwise mask indicating which video unit(s) to operate on.
     * @param function  Transaction control function.
     * @return TransactionDisplay object for use in final part.
     * @throws JposException    For details, see UPOS method TransactionDisplay.
     */
    public TransactionDisplay transactionDisplay(int units, int function) throws JposException;

    /**
     * Final part of TransactionDisplay method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a TransactionDisplay object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.<br>
     * In case of asynchronous processing, the following additional checks have been made before invocation:
     * <ul>
     *     <li>All display units specified by request.getUnits() are online.</li>
     * </ul>
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by TransactionDisplay.
     * @throws JposException    For details, see UPOS method TransactionDisplay.
     */
    public void transactionDisplay(TransactionDisplay request) throws JposException;

    /**
     * Validation part of UpdateVideoRegionAttribute method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>function is one of UA_SET, UA_INTENSITY_ON, UA_INTENSITY_OFF, UA_REVERSE_ON, UA_REVERSE_OFF, UA_BLINK_ON, or UA_BLINK_OFF,</li>
     *     <li>row and column are positive values, height &ge; 1 and width &ge; 1.</li>
     * </ul>
     * If function = UA_SET: 0 &le; attribute &le; 0xff.
     * <br>If AsyncMode is false, the following conditions have been checked, too:
     * <ul>
     *     <li>All display units specified by units are online,</li>
     *     <li>row, column, height and width specify a valid region on all affected units.</li>
     * </ul>
     *
     * @param units     Bitwise mask indicating which video unit(s) to operate on.
     * @param function  The attribute command.
     * @param row       Upper row of the specified region.
     * @param column    Left column of the specified region.
     * @param height    Height of the specified region.
     * @param width     Width of the specified region.
     * @param attribute VGA-like attribute parameter, see UPOS specification, chapter Remote Order Display - General
     *                  Information - Model.
     * @return UpdateVideoRegionAttribute object for use in final part.
     * @throws JposException    For details, see UPOS method UpdateVideoRegionAttribute.
     */
    public UpdateVideoRegionAttribute updateVideoRegionAttribute(int units, int function, int row, int column, int height, int width, int attribute) throws JposException;

    /**
     * Final part of UpdateVideoRegionAttribute method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a UpdateVideoRegionAttribute object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.<br>
     * In case of asynchronous processing, the following additional checks have been made before invocation:
     * <ul>
     *     <li>All display units specified by request.getUnits() are online.</li>
     *     <li>request.getRow(), request.getColumn(), request.getHeight() and request.getWidth() specify a valid region
     *     on all affected units.</li>
     * </ul>
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by UpdateVideoRegionAttribute.
     * @throws JposException    For details, see UPOS method UpdateVideoRegionAttribute.
     */
    public void updateVideoRegionAttribute(UpdateVideoRegionAttribute request) throws JposException;

    /**
     * Validation part of VideoSound method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     * </ul>
     * If AsyncMode is false, the following conditions have been checked, too:
     * <ul>
     *     <li>All display units specified by units are online,</li>
     *     <li>For all affected units, CapTone is true.</li>
     * </ul>
     *
     * @param units             Bitwise mask indicating which video unit(s) to operate on.
     * @param frequency         Tone frequency in Hertz.
     * @param duration          Tone duration in milliseconds.
     * @param numberOfCycles    Number of cycles to generate tone.
     * @param interSoundWait    Delay between sounds, in milliseconds.
     * @return VideoSound object for use in final part.
     * @throws JposException    For details, see UPOS method VideoSound.
     */
    public VideoSound videoSound(int units, int frequency, int duration, int numberOfCycles, int interSoundWait) throws JposException;

    /**
     * Final part of VideoSound method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a UpdateVideoRegionAttribute object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.<br>
     * In case of asynchronous processing, the following additional checks have been made before invocation:
     * <ul>
     *     <li>All display units specified by request.getUnits() are online.</li>
     *     <li>For all affected units, CapTone is true.</li>
     * </ul>
     *
     * @param request   Output request object returned by validation method that contains all parameters to be used by VideoSound.
     * @throws JposException    For details, see UPOS method VideoSound.
     */
    public void videoSound(VideoSound request) throws JposException;
}
