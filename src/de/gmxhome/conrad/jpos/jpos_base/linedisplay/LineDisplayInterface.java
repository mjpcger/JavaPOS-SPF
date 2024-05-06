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

package de.gmxhome.conrad.jpos.jpos_base.linedisplay;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Interface for methods that implement property setter and method calls for the LineDisplay device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Line Display.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface LineDisplayInterface extends JposBaseInterface {
    /**
     * Final part of setting MapCharacterSet. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>if CapMapCharacterSet is false: New value is false as well.</li>
     * </ul>
     *
     * @param b New MapCharacterSet value
     * @throws JposException If an error occurs during enable or disable
     */
    public void mapCharacterSet(boolean b) throws JposException;

    /**
     * Final part of setting ScreenMode. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is claimed,</li>
     *     <li>Device is not enabled,</li>
     *     <li>If CapScreenMode is false: New mode is 0 as well,</li>
     *     <li>If CapScreenMode is true: New mode is one of the values specified in ScreenModeList.</li>
     * </ul>
     *
     * @param b New ScreenMode value
     * @throws JposException If an error occurs during enable or disable
     */
    public void screenMode(int b) throws JposException;

    /**
     * Final part of setting BlinkRate. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>CapBlinkRate is true.</li>
     *     <li>New blink rate is &gt; 0,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or rate equals the previous value of BlinkRate.</li>
     * </ul>
     *
     * @param rate New BlinkRate value
     * @throws JposException If an error occurs during enable or disable
     */
    public void blinkRate(int rate) throws JposException;

    /**
     * Final part of setting CursorType. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>CapCursorType is neither DISP_CCT_NONE nor DISP_CCT_FIXED,</li>
     *     <li>Either CapCursorType bit DISP_CCT_BLINK is set or bit DISP_CT_BLINK is not set in new value,</li>
     *     <li>The new value is valid due to a match with the corresponding value in CapCursorType,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or type equals the previous value of CursorType.</li>
     * </ul>
     *
     * @param type New CursorType value
     * @throws JposException If an error occurs during enable or disable
     */
    public void cursorType(int type) throws JposException;

    /**
     * Final part of setting CharacterSet. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>The new value is one of the values specified in CharacterSetList,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or charset equals the previous value of CharacterSet.</li>
     * </ul>
     *
     * @param charset New CharacterSet value
     * @throws JposException If an error occurs during enable or disable
     */
    public void characterSet(int charset) throws JposException;

    /**
     * Final part of setting CurrentWindow. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>The new value is &ge; 0 and &le; DeviceWindows,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or windowNo equals the previous value of CurrentWindow.</li>
     * </ul>
     *
     * @param windowNo New CurrentWindow value
     * @throws JposException If an error occurs during enable or disable
     */
    public void currentWindow(int windowNo) throws JposException;

    /**
     * Final part of setting CursorColumn. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>The new value is &ge; 0 and &le; Columns,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or column equals the previous value of CursorColumn.</li>
     * </ul>
     *
     * @param column New CursorColumn value
     * @throws JposException If an error occurs during enable or disable
     */
    public void cursorColumn(int column) throws JposException;

    /**
     * Final part of setting CursorRow. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>The new value is &ge; 0 and &lt; Rows,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or row equals the previous value of CursorRow.</li>
     * </ul>
     *
     * @param row New CursorRow value
     * @throws JposException If an error occurs during enable or disable
     */
    public void cursorRow(int row) throws JposException;

    /**
     * Final part of setting CursorUpdate. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or flag equals the previous value of CursorUpdate.</li>
     * </ul>
     *
     * @param flag New CursorUpdate value
     * @throws JposException If an error occurs during enable or disable
     */
    public void cursorUpdate(boolean flag) throws JposException;

    /**
     * Final part of setting DeviceBrightness. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>The new value is &ge; 0 and &le; 100,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or brightness equals the previous value of DeviceBrightness.</li>
     * </ul>
     *
     * @param brightness New DeviceBrightness value
     * @throws JposException If an error occurs during enable or disable
     */
    public void deviceBrightness(int brightness) throws JposException;

    /**
     * Final part of setting InterCharacterWait. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>CapICharWait is false: The new value is 0,</li>
     *     <li>CapICharWait is true: The new value is &ge; 0,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or millisec equals the previous value of InterCharacterWait.</li>
     * </ul>
     *
     * @param millisec New InterCharacterWait value
     * @throws JposException If an error occurs during enable or disable
     */
    public void interCharacterWait(int millisec) throws JposException;

    /**
     * Final part of setting MarqueeFormat. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>CurrentWindow is not the device window,</li>
     *     <li>The new value is DISP_MF_WALK or DISP_MF_PLACE,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or format equals the previous value of MarqueeFormat.</li>
     * </ul>
     *
     * @param format New MarqueeFormat value
     * @throws JposException If an error occurs during enable or disable
     */
    public void marqueeFormat(int format) throws JposException;

    /**
     * Final part of setting MarqueeRepeatWait. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>The new value is &ge; 0,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or millisec equals the previous value of MarqueeRepeatWait.</li>
     * </ul>
     *
     * @param millisec New MarqueeRepeatWait value
     * @throws JposException If an error occurs during enable or disable
     */
    public void marqueeRepeatWait(int millisec) throws JposException;

    /**
     * Final part of setting MarqueeType. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>CurrentWindow is not the device window,</li>
     *     <li>CapVMarquee and CapHMarquee are false: New value is DISP_MT_NONE.</li>
     *     <li>Only CapVMarquee is false: New value is neither DISP_MT_UP nor DISP_MT_DOWN,</li>
     *     <li>Only CapHMarquee is false: New value is neither DISP_MT_LEFT nor DISP_MT_RIGHT,</li>
     *     <li>New value is DISP_MT_NONE, DISP_MT_INIT, DISP_MT_UP, DISP_MT_DOWN, DISP_MT_LEFT or DISP_MT_RIGHT,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or type equals the previous value of MarqueeType.</li>
     * </ul>
     *
     * @param type New MarqueeType value
     * @throws JposException If an error occurs during enable or disable
     */
    public void marqueeType(int type) throws JposException;

    /**
     * Final part of setting MarqueeUnitWait. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>CurrentWindow is not the device window,</li>
     *     <li>The new value is &ge; 0,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or millisec equals the previous value of MarqueeUnitWait.</li>
     * </ul>
     *
     * @param millisec New MarqueeUnitWait value
     * @throws JposException If an error occurs during enable or disable
     */
    public void marqueeUnitWait(int millisec) throws JposException;

    /**
     * Final part of DisplayBitmap method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapBitmap is true,</li>
     *     <li>fileName is neither null nor an empty string,</li>
     *     <li>width is &gt; 0 or DISP_BM_ASIS,</li>
     *     <li>alignmentX is DISP_BM_LEFT, DISP_BM_CENTER or DISP_BM_RIGHT,</li>
     *     <li>alignmentY is DISP_BM_TOP, DISP_BM_CENTER or DISP_BM_BOTTOM.</li>
     * </ul>
     *
     * @param fileName   File name or URL of bitmap file.
     * @param width      Width of the bitmap to be displayed.
     * @param alignmentX Horizontal placement of the bitmap.
     * @param alignmentY Vertical placement of the bitmap.
     * @throws JposException If an error occurs.
     */
    public void displayBitmap(String fileName, int width, int alignmentX, int alignmentY) throws JposException;

    /**
     * Final part of SetBitmap method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapBitmap is true,</li>
     *     <li>bitmapNumber is between 1 and 100,</li>
     *     <li>fileName is neither null nor an empty string,</li>
     *     <li>width is &gt; 0 or DISP_BM_ASIS,</li>
     *     <li>alignmentX is DISP_BM_LEFT, DISP_BM_CENTER or DISP_BM_RIGHT,</li>
     *     <li>alignmentY is DISP_BM_TOP, DISP_BM_CENTER or DISP_BM_BOTTOM.</li>
     * </ul>
     *
     * @param bitmapNumber The number to be assigned to this bitmap.
     * @param fileName     File name or URL of bitmap file.
     * @param width        Width of the bitmap to be displayed.
     * @param alignmentX   Horizontal placement of the bitmap.
     * @param alignmentY   Vertical placement of the bitmap.
     * @throws JposException If an error occurs.
     */
    public void setBitmap(int bitmapNumber, String fileName, int width, int alignmentX, int alignmentY) throws JposException;

    /**
     * Final part of DefineGlyph method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapCustomGlyph is true,</li>
     *     <li>glyphCode is one of the values specified in CustomGlyphList,</li>
     *     <li>glyph in a byte array with a length of at least GlyphHeight * (GlyphWidth + 7) / 8.</li>
     * </ul>
     *
     * @param glyphCode The character code to be defined.
     * @param glyph     Data bytes that define the glyph.
     * @throws JposException If an error occurs.
     */
    public void defineGlyph(int glyphCode, byte[] glyph) throws JposException;

    /**
     * Final part of ReadCharacterAtCursor method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapReadBack is true,</li>
     *     <li>cursorData is of type int[1].</li>
     * </ul>
     *
     * @param cursorData The character read from the display.
     * @throws JposException If an error occurs.
     */
    public void readCharacterAtCursor(int[] cursorData) throws JposException;

    /**
     * Final part of ClearDescriptors method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapDescriptors is true.</li>
     * </ul>
     *
     * @throws JposException If an error occurs.
     */
    public void clearDescriptors() throws JposException;

    /**
     * Final part of ClearText method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>MarqueeType is DISP_MT_NONE or DISP_MT_INIT.</li>
     * </ul>
     *
     * @throws JposException If an error occurs.
     */
    public void clearText() throws JposException;

    /**
     * Final part of CreateWindow method. Can be overwritten in derived class, if necessary. Initializes the following
     * properties: Rows, Columns, CursorRow, CursorType, CursorUpdate, MarqueeType, MarqueeFormat, MarqueeUnitWait.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>DeviceWindows is not 0,</li>
     *     <li>viewportRow is between 0 and DeviceRows - 1,</li>
     *     <li>viewportColumn is between 0 and DeviceColumns - 1,</li>
     *     <li>viewportHeight is between 0 and DeviceRows - viewportRow,</li>
     *     <li>viewportWidth is between 0 and DeviceColumns - viewportColumn,</li>
     *     <li>windowHeight is &ge; viewportHeight,</li>
     *     <li>windowWidth is &ge; viewportWidth,</li>
     *     <li>Either windowHeight = viewportHeight or windowWidth = viewportWidth.</li>
     * </ul>
     *
     * @param viewportRow    The viewport’s start device row.
     * @param viewportColumn The viewport’s start device column.
     * @param viewportHeight The number of device rows in the viewport.
     * @param viewportWidth  The number of device columns in the viewport.
     * @param windowHeight   The number of rows in the window.
     * @param windowWidth    The number of columns in the window.
     * @throws JposException If an error occurs.
     */
    public void createWindow(int viewportRow, int viewportColumn, int viewportHeight, int viewportWidth, int windowHeight, int windowWidth) throws JposException;

    /**
     * Final part of DestroyWindow method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CurrentWindow is not the device window.</li>
     * </ul>
     *
     * @throws JposException If an error occurs.
     */
    public void destroyWindow() throws JposException;

    /**
     * Validation part of DisplayText method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>data is not null,</li>
     *     <li>MarqueeType equals DISP_MT_NONE or DISP_MT_INIT.</li>
     * </ul>
     * In addition to validation, this method must update CursorRow and CursorColumn on success if InterCharacterWait
     * is not zero and MarqueeType is MT_NONE (teletype mode).
     *
     * @param data      Display data.
     * @param attribute Display attributes.
     * @throws JposException If an error occurs.
     * @return DisplayText object for use in final part.
     */
    public DisplayText displayText(String data, int attribute) throws JposException;

    /**
     * Final part of displayText method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a DisplayText object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously (if InterCharacterWait
     * is &gt; 0). All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request   Output request object returned by validation method that contains all parameters
     *                  to be used by DisplayText.
     * @throws JposException If an error occurs.
     */
    public void displayText(DisplayText request) throws JposException;

    /**
     * Final part of RefreshWindow method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>MarqueeType equals DISP_MT_NONE or DISP_MT_INIT,</li>
     *     <li>window is between 0 and DeviceWindows.</li>
     * </ul>
     *
     * @param window The window to be refreshed.
     * @throws JposException If an error occurs.
     */
    public void refreshWindow(int window) throws JposException;

    /**
     * Final part of ScrollText method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>MarqueeType equals DISP_MT_NONE,</li>
     *     <li>InterCharacterWait equals 0,</li>
     *     <li>direction is DISP_ST_UP, DISP_ST_DOWN, DISP_ST_LEFT or DISP_ST_RIGHT.</li>
     *     <li>units &ge; 0,</li>
     * </ul>
     *
     * @param direction Scrolling direction.
     * @param units     Number of columns or rows to scroll.
     * @throws JposException If an error occurs.
     */
    public void scrollText(int direction, int units) throws JposException;

    /**
     * Final part of SetDescriptor method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapDescriptors is true,</li>
     *     <li>descriptor is between 0 and DeviceDescriptors,</li>
     *     <li>attribute is DISP_SD_ON, DISP_SD_OFF or DISP_SD_BLINK.</li>
     * </ul>
     *
     * @param descriptor Descriptor to be changed.
     * @param attribute  Attribute for the descriptor.
     * @throws JposException If an error occurs.
     */
    public void setDescriptor(int descriptor, int attribute) throws JposException;
}
