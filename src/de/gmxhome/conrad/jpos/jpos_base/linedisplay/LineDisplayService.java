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
import jpos.services.*;

import java.util.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;
import static jpos.LineDisplayConst.*;

/**
 * LineDisplay service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class LineDisplayService extends JposBase implements LineDisplayService116{
    /**
     * Instance of a class implementing the LineDisplayInterface for line display specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public LineDisplayInterface LineDisplayInterface;

    private final LineDisplayProperties Data;

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public LineDisplayService(LineDisplayProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public boolean getCapBitmap() throws JposException {
        checkOpened();
        logGet("CapBitmap");
        return Data.CapBitmap;
    }

    @Override
    public boolean getCapScreenMode() throws JposException {
        checkOpened();
        logGet("CapScreenMode");
        return Data.CapScreenMode;
    }

    @Override
    public boolean getCapMapCharacterSet() throws JposException {
        checkOpened();
        logGet("CapMapCharacterSet");
        return Data.CapMapCharacterSet;
    }

    @Override
    public boolean getCapBlinkRate() throws JposException {
        checkOpened();
        logGet("CapBlinkRate");
        return Data.CapBlinkRate;
    }

    @Override
    public int getCapCursorType() throws JposException {
        checkOpened();
        logGet("CapCursorType");
        return Data.CapCursorType;
    }

    @Override
    public boolean getCapCustomGlyph() throws JposException {
        checkOpened();
        logGet("CapCustomGlyph");
        return Data.CapCustomGlyph;
    }

    @Override
    public int getCapReadBack() throws JposException {
        checkOpened();
        logGet("CapReadBack");
        return Data.CapReadBack;
    }

    @Override
    public int getCapReverse() throws JposException {
        checkOpened();
        logGet("CapReverse");
        return Data.CapReverse;
    }

    @Override
    public int getCapBlink() throws JposException {
        checkOpened();
        logGet("CapBlink");
        return Data.CapBlink;
    }

    @Override
    public boolean getCapBrightness() throws JposException {
        checkOpened();
        logGet("CapBrightness");
        return Data.CapBrightness;
    }

    @Override
    public int getCapCharacterSet() throws JposException {
        checkOpened();
        logGet("CapCharacterSet");
        return Data.CapCharacterSet;
    }

    @Override
    public boolean getCapDescriptors() throws JposException {
        checkOpened();
        logGet("CapDescriptors");
        return Data.CapDescriptors;
    }

    @Override
    public boolean getCapHMarquee() throws JposException {
        checkOpened();
        logGet("CapHMarquee");
        return Data.CapHMarquee;
    }

    @Override
    public boolean getCapICharWait() throws JposException {
        checkOpened();
        logGet("CapICharWait");
        return Data.CapICharWait;
    }

    @Override
    public boolean getCapVMarquee() throws JposException {
        checkOpened();
        logGet("CapVMarquee");
        return Data.CapVMarquee;
    }

    @Override
    public boolean getMapCharacterSet() throws JposException {
        checkOpened();
        logGet("MapCharacterSet");
        return Data.MapCharacterSet;
    }

    @Override
    public void setMapCharacterSet(boolean b) throws JposException {
        logPreSet("MapCharacterSet");
        checkOpened();
        check(!Data.CapMapCharacterSet && b, JPOS_E_ILLEGAL, "Character set mapping not supported");
        LineDisplayInterface.mapCharacterSet(b);
        logSet("MapCharacterSet");
    }

    @Override
    public int getMaximumX() throws JposException {
        checkOpened();
        logGet("MaximumX");
        return Data.MaximumX;
    }

    @Override
    public int getMaximumY() throws JposException {
        checkOpened();
        logGet("MaximumY");
        return Data.MaximumY;
    }

    @Override
    public int getScreenMode() throws JposException {
        checkOpened();
        logGet("ScreenMode");
        return Data.ScreenMode;
    }

    @Override
    public void setScreenMode(int i) throws JposException {
        logPreSet("ScreenMode");
        checkClaimed();
        check(Props.DeviceEnabled, JPOS_E_ILLEGAL, "Device enabled");
        check(!Data.CapScreenMode && i != 0, JPOS_E_ILLEGAL, "Screen mode out of range");
        checkRange(i, 0, Data.ScreenModeList.split(",").length, JPOS_E_ILLEGAL, "Screen mode out of range");
        LineDisplayInterface.screenMode(i);
        logSet("ScreenMode");
    }

    @Override
    public String getScreenModeList() throws JposException {
        checkOpened();
        logGet("ScreenModeList");
        return Data.ScreenModeList;
    }

    @Override
    public int getBlinkRate() throws JposException {
        checkOpened();
        logGet("BlinkRate");
        return Data.BlinkRate;
    }

    @Override
    public void setBlinkRate(int i) throws JposException {
        logPreSet("BlinkRate");
        checkOpened();
        check(!Data.CapBlinkRate, JPOS_E_ILLEGAL, "Blink rate setting not supported");
        check(i <= 0, JPOS_E_ILLEGAL, "Illegal blink rate: " + i);
        checkNoChangedOrClaimed(Data.BlinkRate, i);
        LineDisplayInterface.blinkRate(i);
        logSet("BlinkRate");
    }

    @Override
    public int getCursorType() throws JposException {
        checkOpened();
        logGet("CursorType");
        return Data.CursorType;
    }

    @Override
    public void setCursorType(int i) throws JposException {
        logPreSet("CursorType");
        checkOpened();
        check(Data.CapCursorType == DISP_CCT_NONE || Data.CapCursorType == DISP_CCT_FIXED, JPOS_E_ILLEGAL, "Cursor type cannot be set");
        check((i & DISP_CT_BLINK) != 0 && (Data.CapCursorType & DISP_CCT_BLINK) == 0, JPOS_E_ILLEGAL, "Blinking cursor not supported");
        switch (i & ~DISP_CT_BLINK ) {
            case DISP_CT_NONE -> throw new JposException(JPOS_E_ILLEGAL, "Cursor type not supported");
            case DISP_CT_BLOCK -> check((Data.CapCursorType & DISP_CCT_BLOCK) == 0, JPOS_E_ILLEGAL, "Cursor type not supported");
            case DISP_CT_HALFBLOCK -> check((Data.CapCursorType & DISP_CCT_HALFBLOCK) == 0, JPOS_E_ILLEGAL, "Cursor type not supported");
            case DISP_CT_UNDERLINE -> check((Data.CapCursorType & DISP_CCT_UNDERLINE) == 0, JPOS_E_ILLEGAL, "Cursor type not supported");
            case DISP_CT_REVERSE -> check((Data.CapCursorType & DISP_CCT_REVERSE) == 0, JPOS_E_ILLEGAL, "Cursor type not supported");
            case DISP_CT_OTHER -> check((Data.CapCursorType & DISP_CCT_OTHER) == 0, JPOS_E_ILLEGAL, "Cursor type not supported");
            default -> throw new JposException(JPOS_E_ILLEGAL, "Invalid cursor type: " + i);
        }
        checkNoChangedOrClaimed(Data.CursorType, i);
        LineDisplayInterface.cursorType(i);
        logSet("CursorType");
    }

    @Override
    public String getCustomGlyphList() throws JposException {
        checkOpened();
        logGet("CustomGlyphList");
        return Data.CustomGlyphList;
    }

    @Override
    public int getGlyphHeight() throws JposException {
        checkOpened();
        logGet("GlyphHeight");
        return Data.GlyphHeight;
    }

    @Override
    public int getGlyphWidth() throws JposException {
        checkOpened();
        logGet("GlyphWidth");
        return Data.GlyphWidth;
    }

    @Override
    public int getCharacterSet() throws JposException {
        checkOpened();
        logGet("CharacterSet");
        return Data.CharacterSet;
    }

    @Override
    public void setCharacterSet(int i) throws JposException {
        logPreSet("CharacterSet");
        checkOpened();
        checkMember(i, stringArrayToLongArray(Data.CharacterSetList.split(",")), JPOS_E_ILLEGAL, "Invalid Character set: " + i);
        checkNoChangedOrClaimed(Data.CharacterSet, i);
        LineDisplayInterface.characterSet(i);
        logSet("CharacterSet");
    }

    @Override
    public String getCharacterSetList() throws JposException {
        checkOpened();
        logGet("CharacterSetList");
        return Data.CharacterSetList;
    }

    @Override
    public int getColumns() throws JposException {
        checkOpened();
        logGet("Columns");
        return Data.Columns;
    }

    @Override
    public int getCurrentWindow() throws JposException {
        checkOpened();
        logGet("CurrentWindow");
        return Data.CurrentWindow;
    }

    @Override
    public void setCurrentWindow(int i) throws JposException {
        logPreSet("CurrentWindow");
        checkOpened();
        check(i < 0 || Data.DeviceWindows < i, JPOS_E_ILLEGAL, "Current windows out of range: " + i);
        checkNoChangedOrClaimed(Data.CurrentWindow, i);
        LineDisplayInterface.currentWindow(i);
        logSet("CurrentWindow");
    }

    @Override
    public int getCursorColumn() throws JposException {
        checkOpened();
        logGet("CursorColumn");
        return Data.CursorColumn;
    }

    @Override
    public void setCursorColumn(int i) throws JposException {
        logPreSet("CursorColumn");
        checkOpened();
        check(Data.Columns < i || i < 0, JPOS_E_ILLEGAL, "Cursor column out of range: " + i);
        checkNoChangedOrClaimed(Data.CursorColumn, i);
        LineDisplayInterface.cursorColumn(i);
        logSet("CursorColumn");
    }

    @Override
    public int getCursorRow() throws JposException {
        checkOpened();
        logGet("CursorRow");
        return Data.CursorRow;
    }

    @Override
    public void setCursorRow(int i) throws JposException {
        logPreSet("CursorRow");
        checkOpened();
        check(Data.Rows <= i || i < 0, JPOS_E_ILLEGAL, "Cursor row out of range:" + i);
        checkNoChangedOrClaimed(Data.CursorRow, i);
        LineDisplayInterface.cursorRow(i);
        logSet("CursorRow");
    }

    @Override
    public boolean getCursorUpdate() throws JposException {
        checkOpened();
        logGet("CursorUpdate");
        return Data.CursorUpdate;
    }

    @Override
    public void setCursorUpdate(boolean b) throws JposException {
        logPreSet("CursorUpdate");
        checkOpened();
        checkNoChangedOrClaimed(Data.CursorUpdate, b);
        LineDisplayInterface.cursorUpdate(b);
        logSet("CursorUpdate");
    }

    @Override
    public int getDeviceBrightness() throws JposException {
        checkOpened();
        logGet("DeviceBrightness");
        return Data.DeviceBrightness;
    }

    @Override
    public void setDeviceBrightness(int i) throws JposException {
        logPreSet("DeviceBrightness");
        checkOpened();
        check(i < 0 || i > 100, JPOS_E_ILLEGAL, "Invalid device brightness: " + i);
        checkNoChangedOrClaimed(Data.DeviceBrightness, i);
        LineDisplayInterface.deviceBrightness(i);
        logSet("DeviceBrightness");
    }

    @Override
    public int getDeviceColumns() throws JposException {
        checkOpened();
        logGet("DeviceColumns");
        return Data.DeviceColumns;
    }

    @Override
    public int getDeviceDescriptors() throws JposException {
        checkOpened();
        logGet("DeviceDescriptors");
        return Data.DeviceDescriptors;
    }

    @Override
    public int getDeviceRows() throws JposException {
        checkOpened();
        logGet("DeviceRows");
        return Data.DeviceRows;
    }

    @Override
    public int getDeviceWindows() throws JposException {
        checkOpened();
        logGet("DeviceWindows");
        return Data.DeviceWindows;
    }

    @Override
    public int getInterCharacterWait() throws JposException {
        checkOpened();
        logGet("InterCharacterWait");
        return Data.InterCharacterWait;
    }

    @Override
    public void setInterCharacterWait(int i) throws JposException {
        logPreSet("InterCharacterWait");
        checkOpened();
        check(!Data.CapICharWait && i > 0, JPOS_E_ILLEGAL, "Inter-character wait not supported");
        check(i < 0, JPOS_E_ILLEGAL, "Invalid waiting time: " + i);
        checkNoChangedOrClaimed(Data.InterCharacterWait, i);
        LineDisplayInterface.interCharacterWait(i);
        logSet("InterCharacterWait");
    }

    @Override
    public int getMarqueeFormat() throws JposException {
        checkOpened();
        logGet("MarqueeFormat");
        return Data.MarqueeFormat;
    }

    @Override
    public void setMarqueeFormat(int i) throws JposException {
        logPreSet("MarqueeFormat");
        checkOpened();
        check(Data.CurrentWindow == 0, JPOS_E_ILLEGAL, "No marquee format for device window");
        checkMember(i, new long[]{ DISP_MF_WALK, DISP_MF_PLACE }, JPOS_E_ILLEGAL, "Invalid marquee format: " + i);
        checkNoChangedOrClaimed(Data.MarqueeFormat, i);
        LineDisplayInterface.marqueeFormat(i);
        logSet("MarqueeFormat");
    }

    @Override
    public int getMarqueeRepeatWait() throws JposException {
        checkOpened();
        logGet("MarqueeRepeatWait");
        return Data.MarqueeRepeatWait;
    }

    @Override
    public void setMarqueeRepeatWait(int i) throws JposException {
        logPreSet("MarqueeRepeatWait");
        checkOpened();
        check(i < 0, JPOS_E_ILLEGAL, "Invalid marquee repeat wait: " + i);
        checkNoChangedOrClaimed(Data.MarqueeRepeatWait, i);
        LineDisplayInterface.marqueeRepeatWait(i);
        logSet("MarqueeRepeatWait");
    }

    @Override
    public int getMarqueeType() throws JposException {
        checkOpened();
        logGet("MarqueeType");
        return Data.MarqueeType;
    }

    @Override
    public void setMarqueeType(int i) throws JposException {
        logPreSet("MarqueeType");
        checkOpened();
        check(Data.CurrentWindow == 0, JPOS_E_CLOSED, "Invalid marquee type for device window");
        check(!Data.CapVMarquee && !Data.CapHMarquee && i != DISP_MT_NONE, JPOS_E_ILLEGAL, "Marquee not supported");
        check(!Data.CapHMarquee && !member(i, new long[]{DISP_MT_NONE, DISP_MT_INIT, DISP_MT_UP, DISP_MT_DOWN}), JPOS_E_ILLEGAL, "Invalid marquee type: " + i);
        check(!Data.CapVMarquee && !member(i, new long[]{DISP_MT_NONE, DISP_MT_INIT, DISP_MT_LEFT, DISP_MT_RIGHT}), JPOS_E_ILLEGAL, "Invalid marquee type: " + i);
        check(!Data.CapHMarquee && !member(i, new long[]{DISP_MT_NONE, DISP_MT_INIT, DISP_MT_UP, DISP_MT_DOWN, DISP_MT_LEFT, DISP_MT_RIGHT}), JPOS_E_ILLEGAL, "Invalid marquee type: " + i);
        checkNoChangedOrClaimed(Data.MarqueeType, i);
        LineDisplayInterface.marqueeType(i);
        logSet("MarqueeType");
    }

    @Override
    public int getMarqueeUnitWait() throws JposException {
        checkOpened();
        logGet("MarqueeUnitWait");
        return Data.MarqueeUnitWait;
    }

    @Override
    public void setMarqueeUnitWait(int i) throws JposException {
        logPreSet("MarqueeUnitWait");
        checkOpened();
        check(Data.CurrentWindow == 0, JPOS_E_ILLEGAL, "Marque not supported for device window");
        check(i < 0, JPOS_E_ILLEGAL, "Invalid marquee unit wait: " + i);
        checkNoChangedOrClaimed(Data.MarqueeUnitWait, i);
        LineDisplayInterface.marqueeUnitWait(i);
        logSet("MarqueeUnitWait");
    }

    @Override
    public int getRows() throws JposException {
        checkOpened();
        logGet("Rows");
        return Data.Rows;
    }

    @Override
    public void displayBitmap(String fileName, int width, int alignmentX, int alignmentY) throws JposException {
        logPreCall("DisplayBitmap", removeOuterArraySpecifier(new Object[]{fileName, width, alignmentX, alignmentY}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapBitmap, JPOS_E_ILLEGAL, "Bitmaps not supported");
        check(fileName == null || fileName.length() == 0, JPOS_E_ILLEGAL, "Empty filename");
        check(width <= 0 && width != DISP_BM_ASIS, JPOS_E_ILLEGAL, "Bitmap width out of range: " + width);
        checkMember(alignmentX, new long[]{DISP_BM_LEFT, DISP_BM_CENTER, DISP_BM_RIGHT}, JPOS_E_ILLEGAL, "Bitmaps X alignment not supported: " + alignmentX);
        checkMember(alignmentY, new long[]{DISP_BM_TOP, DISP_BM_CENTER, DISP_BM_BOTTOM}, JPOS_E_ILLEGAL, "Bitmaps Y alignment not supported: " + alignmentY);
        LineDisplayInterface.displayBitmap(fileName, width, alignmentX, alignmentY);
        logCall("DisplayBitmap");
    }

    @Override
    public void setBitmap(int bitmapNumber, String fileName, int width, int alignmentX, int alignmentY) throws JposException {
        logPreCall("SetBitmap", removeOuterArraySpecifier(new Object[]{bitmapNumber, fileName, width, alignmentX, alignmentY}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapBitmap, JPOS_E_ILLEGAL, "Bitmaps not supported");
        checkRange(bitmapNumber, 1, 100, JPOS_E_ILLEGAL, "Invalid bitmap number: " + bitmapNumber);
        check(fileName == null || fileName.length() == 0, JPOS_E_ILLEGAL, "Empty filename");
        check(width <= 0 && width != DISP_BM_ASIS, JPOS_E_ILLEGAL, "Bitmap width out of range: " + width);
        checkMember(alignmentX, new long[]{DISP_BM_LEFT, DISP_BM_CENTER, DISP_BM_RIGHT}, JPOS_E_ILLEGAL, "Bitmaps X alignment not supported: " + alignmentX);
        checkMember(alignmentY, new long[]{DISP_BM_TOP, DISP_BM_CENTER, DISP_BM_BOTTOM}, JPOS_E_ILLEGAL, "Bitmaps Y alignment not supported: " + alignmentY);
        LineDisplayInterface.setBitmap(bitmapNumber, fileName, width, alignmentX, alignmentY);
        logCall("SetBitmap");
    }

    @Override
    public void defineGlyph(int glyphCode, byte[] glyph) throws JposException {
        logPreCall("DefineGlyph", removeOuterArraySpecifier(new Object[]{glyphCode, glyph}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapCustomGlyph, JPOS_E_ILLEGAL, "Glyphs not supported");
        check(glyph == null || glyph.length < Data.GlyphHeight * (Data.GlyphWidth + 7) / 8, JPOS_E_ILLEGAL, "Too few glyphs data");
        String[] codes = Data.CustomGlyphList.split(",");
        boolean valid = false;
        for (String s : codes) {
            String[] vals = s.split("-");
            if (vals.length == 1 && Integer.parseInt(vals[0], 16) == glyphCode) {
                valid = true;
                break;
            }
            else if (vals.length == 2 && Integer.parseInt(vals[0], 16) <= glyphCode && Integer.parseInt(vals[1], 16) >= glyphCode) {
                valid = true;
                break;
            }
        }
        check(!valid, JPOS_E_ILLEGAL, "Invalid glyphcode: " + glyphCode);
        LineDisplayInterface.defineGlyph(glyphCode, glyph);
        logCall("DefineGlyph");
    }

    @Override
    public void readCharacterAtCursor(int[] cursorData) throws JposException {
        logPreCall("ReadCharacterAtCursor");
        checkEnabled();
        check(Data.CapReadBack == DISP_CRB_NONE, JPOS_E_ILLEGAL, "Read back not supported");
        check(cursorData == null || cursorData.length != 1, JPOS_E_ILLEGAL, "CursorData must be of type int[1]");
        LineDisplayInterface.readCharacterAtCursor(cursorData);
        logCall("ReadCharacterAtCursor", removeOuterArraySpecifier(new Object[]{cursorData[0]}, Device.MaxArrayStringElements));
    }

    @Override
    public void clearDescriptors() throws JposException {
        logPreCall("ClearDescriptors");
        checkEnabled();
        check(!Data.CapDescriptors, JPOS_E_ILLEGAL, "Descriptors not supported");
        LineDisplayInterface.clearDescriptors();
        logCall("ClearDescriptors");
    }

    @Override
    public void clearText() throws JposException {
        logPreCall("ClearText");
        checkEnabled();
        checkMember(Data.MarqueeType, new long[]{DISP_MT_NONE, DISP_MT_INIT}, JPOS_E_ILLEGAL, "Not supported in marquee mode");
        if (Data.InterCharacterWait != 0) {
            // Clear outstanding asynchronous requests first
            LineDisplayInterface.clearOutput();
        }
        LineDisplayInterface.clearText();
        logCall("ClearText");
    }

    @Override
    public void createWindow(int viewportRow, int viewportColumn, int viewportHeight, int viewportWidth, int windowHeight, int windowWidth) throws JposException {
        logPreCall("CreateWindow", removeOuterArraySpecifier(new Object[]{viewportRow, viewportColumn, viewportHeight, viewportWidth, windowHeight, windowWidth}, Device.MaxArrayStringElements));
        checkEnabled();
        check(Data.DeviceWindows == 0, JPOS_E_ILLEGAL, "Create window not supported");
        check(viewportColumn < 0 || viewportRow < 0 || viewportColumn >= Data.DeviceColumns || viewportRow >= Data.DeviceRows || viewportHeight <= 0 || viewportWidth <= 0 || windowHeight <= 0 || windowWidth <= 0, JPOS_E_ILLEGAL, "Illegal parameter");
        check(windowHeight < viewportHeight || windowWidth < viewportWidth || (windowHeight != viewportHeight && windowWidth != viewportWidth), JPOS_E_ILLEGAL, "Viewport too large");
        check(viewportHeight + viewportRow > Data.DeviceRows || viewportWidth + viewportColumn > Data.DeviceColumns, JPOS_E_ILLEGAL, "Window outside device window");
        LineDisplayInterface.createWindow(viewportRow, viewportColumn, viewportHeight, viewportWidth, windowHeight, windowWidth);
        logCall("CreateWindow");
    }

    @Override
    public void destroyWindow() throws JposException {
        logPreCall("DestroyWindow");
        checkEnabled();
        check(Data.CurrentWindow == 0, JPOS_E_ILLEGAL, "Window 0 cannot be destroyed");
        LineDisplayInterface.destroyWindow();
        logCall("DestroyWindow");
    }

    @Override
    public void displayText(String data, int attribute) throws JposException {
        logPreCall("DisplayText", removeOuterArraySpecifier(new Object[]{data, attribute}, Device.MaxArrayStringElements));
        checkEnabled();
        if (data == null)
            data = "";
        checkMember(attribute, new long[]{DISP_DT_NORMAL, DISP_DT_REVERSE, DISP_DT_BLINK, DISP_DT_BLINK_REVERSE, }, JPOS_E_ILLEGAL, "Attribute invalid: " + attribute);
        checkMember(Data.MarqueeType, new long[]{DISP_MT_NONE, DISP_MT_INIT}, JPOS_E_ILLEGAL, "Not supported in marquee mode");
        DisplayText request = LineDisplayInterface.displayText(data, attribute);
        if (Data.InterCharacterWait > 0 && Data.MarqueeType == DISP_MT_NONE){
            request.enqueue();
            logAsyncCall("DisplayText");
            return;
        }
        else if (request != null) {
            request.enqueueSynchronous();
            if (request.Exception != null)
                throw request.Exception;
        }
        logCall("DisplayText");
    }

    @Override
    public void displayTextAt(int row, int column, String data, int attribute) throws JposException {
        logPreCall("DisplayTextAt", removeOuterArraySpecifier(new Object[]{row, column, data, attribute}, Device.MaxArrayStringElements));
        checkEnabled();
        if (data == null)
            data = "";
        check(Data.Rows <= row || row < 0, JPOS_E_ILLEGAL, "Cursor row out of range:" + row);
        LineDisplayInterface.cursorRow(row);
        check(Data.Columns < column || column < 0, JPOS_E_ILLEGAL, "Cursor column out of range: " + column);
        LineDisplayInterface.cursorColumn(column);
        checkMember(attribute, new long[]{DISP_DT_NORMAL, DISP_DT_REVERSE, DISP_DT_BLINK, DISP_DT_BLINK_REVERSE, }, JPOS_E_ILLEGAL, "Attribute invalid: " + attribute);
        checkMember(Data.MarqueeType, new long[]{DISP_MT_NONE, DISP_MT_INIT}, JPOS_E_ILLEGAL, "Not supported in marquee mode");
        DisplayText request = LineDisplayInterface.displayText(data, attribute);
        if (Data.InterCharacterWait > 0 && Data.MarqueeType == DISP_MT_NONE){
            request.enqueue();
            logAsyncCall("DisplayTextAt");
            return;
        }
        else if (request != null) {
            request.enqueueSynchronous();
            if (request.Exception != null)
                throw request.Exception;
        }
        logCall("DisplayTextAt");
    }

    @Override
    public void refreshWindow(int window) throws JposException {
        logPreCall("RefreshWindow", removeOuterArraySpecifier(new Object[]{window}, Device.MaxArrayStringElements));
        checkEnabled();
        check(Data.DeviceWindows < window || window < 0, JPOS_E_ILLEGAL, "Invalid window: " + window);
        checkMember(Data.MarqueeType, new long[]{DISP_MT_NONE, DISP_MT_INIT}, JPOS_E_ILLEGAL, "Not supported in marquee mode");
        LineDisplayInterface.refreshWindow(window);
        logCall("RefreshWindow");
    }

    @Override
    public void scrollText(int direction, int units) throws JposException {
        logPreCall("ScrollText", removeOuterArraySpecifier(new Object[]{direction, units}, Device.MaxArrayStringElements));
        checkEnabled();
        check(Data.InterCharacterWait != 0 || Data.MarqueeType != DISP_MT_NONE, JPOS_E_ILLEGAL, "Scroll text not supported in marquee or teletype mode");
        checkMember(direction, new long[]{DISP_ST_UP, DISP_ST_DOWN, DISP_ST_LEFT, DISP_ST_RIGHT}, JPOS_E_ILLEGAL, "Illegal direction: "+ direction);
        check(units < 0, JPOS_E_ILLEGAL, "Scrolling negative units not supported");
        LineDisplayInterface.scrollText(direction, units);
        logCall("ScrollText");
    }

    @Override
    public void setDescriptor(int descriptor, int attribute) throws JposException {
        logPreCall("SetDescriptor", removeOuterArraySpecifier(new Object[]{descriptor, attribute}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapDescriptors, JPOS_E_ILLEGAL, "Descriptors not supported");
        check(descriptor < 0 || descriptor >= Data.DeviceDescriptors, JPOS_E_ILLEGAL, "Invalid descriptor: " + descriptor);
        checkMember(attribute, new long[]{DISP_SD_ON, DISP_SD_OFF, DISP_SD_BLINK}, JPOS_E_ILLEGAL, "Invalid attribute: " + attribute);
        LineDisplayInterface.setDescriptor(descriptor, attribute);
        logCall("SetDescriptor");
    }

    /*
     * LineDisplay data processing
     */

    /**
     * Parses output data for escape sequences and valid control characters CR and LF. See UPOS specification for DisplayText,
     * chapter <i>Data Characters and Escape Sequences</i>. Returns list of objects that describe all parts of the output
     * string. These objects can be used by display functions to check display data and to generate generic output
     * data.
     * Possible objects in list have one of the following types:
     * <ul>
     *     <li>DisplayData    -    Class containing character strings with printable characters only. See PrintData for details.</li>
     *     <li>ControlChar    -    Control character object containing CR or LF.</li>
     *     <li>EscNormalize   -    Class containing information about details of a normalize command. See EscNormalize for details.</li>
     *     <li>EscBitmap      -    Class containing information about details of a bitmap command. See EscBitmap for details.</li>
     *     <li>EscSimple      -    Class containing information about details of a simple attribute command. See EscSimple for details.</li>
     *     <li>EscUnknown     -    Class containing information about details of an unknown escape sequence.</li>
     * </ul>
     *
     * @param data Character string to be displayed. May contain CR, LF and ESC sequences as described in the UPOS specification.
     * @param attribute Text attribute to be used. See UPOS description.
     * @return array of objects that describe all parts of data.
     */
    DisplayDataPart[] outputDataParts(String data, int attribute) {
        List<DisplayDataPart> out = new ArrayList<>();
        int index;
        try {
            while ((index = data.indexOf("\33|")) >= 0) {
                outputDisplayableParts(data.substring(0, index), out);
                data = data.substring(index + 2);
                int temp;
                int value = -1;
                for (index = 0; (temp = data.charAt(index) - '0') >= 0 && temp <= 9; ++index) {
                    value = value < 0 ? temp : value * 10 + temp;
                }
                data = data.substring(index);
                int subtype = 0;
                for (index = 0; (temp = data.charAt(index)) >= 'a' && temp <= 'z'; index++) {
                    subtype = subtype * 1000 + temp;
                }
                data = data.substring(index + 1);
                out.add(getEscObj(temp, subtype, value));
            }
            if (data.length() > 0)
                outputDisplayableParts(data, out);
        } catch (IndexOutOfBoundsException e) {
            out.add(new EscUnknown(0, 0, 0));
        }
        out.add(0, attribute == DISP_DT_NORMAL ? new LineDisplayService.EscNormalize() : new LineDisplayService.EscSimple((attribute & DISP_DT_BLINK) != 0, (attribute & DISP_DT_REVERSE) != 0));
        return out.toArray(new DisplayDataPart[0]);
    }

    private void outputDisplayableParts(String data, List<DisplayDataPart> out) {
        for (int i = 0; i < data.length(); i++) {
            int actchar = data.charAt(i);
            if (actchar == '\12' || actchar == '\15') {
                if (i > 0)
                    out.add(new DisplayData(data.substring(0, i), Data.MapCharacterSet, Data.CharacterSet));
                data = data.substring(i + 1);
                out.add(new ControlChar((char) actchar));
                i = -1;
            }
        }
        if (data.length() > 0)
            out.add(new DisplayData(data, Data.MapCharacterSet, Data.CharacterSet));
    }

    private DisplayDataPart getEscObj(int temp, int subtype, int value) {
        DisplayDataPart ret;
        boolean notnull = ((ret = EscNormalize.getEscNormalize(null, temp, subtype, value)) != null ||
                (ret = EscBitmap.getEscBitmap(null, temp, subtype, value)) != null ||
                (ret = EscSimple.getEscSimple(null, temp, subtype, value)) != null);
        return notnull ? ret : new EscUnknown(temp, subtype, value);
    }

    public abstract static class DisplayDataPart {}

    /**
     * Class describing control characters in display data.
     */
    public static class ControlChar extends DisplayDataPart {
        /**
         * Returns control character to be handled. One of CR or LF.
         * @return '\n' or '\r'.
         */
        public char getControlCharacter() {
            return ControlCharacter;
        }
        private final char ControlCharacter;

        /**
         * Constructor
         * @param control Control character to be displayed. Only CR and LF are valid control characters.
         */
        public ControlChar(char control) {
            ControlCharacter = control;
        }
    }

    /**
     * Class describing displayable part of display data.
     */
    public static class DisplayData extends DisplayDataPart {
        /**
         * Returns data to be displayed.
         * @return Display data.
         */
        public String getData() {
            return Data;
        }
        private final String Data;

        /**
         * Returns whether the service maps data.
         * @return  If true, Data contains unmapped data and the service must perform character conversion, if necessary.
         *          If false, Data contains mapped data and the service does not need to perform conversion (Data will
         *          be copied character-to-byte into the output buffer).
         */
        public boolean getServiceIsMapping() {
            return ServiceIsMapping;
        }
        private final boolean ServiceIsMapping;

        /**
         * Returns character set to be used for output.
         * @return Character set.
         */
        public int getCharacterSet() {
            return CharacterSet;
        }
        private final int CharacterSet;

        /**
         * Constructor.
         * @param data      Print data.
         * @param mapping   Character mapping by service (true) or by application (false).
         * @param charset   Character set to be used during print operation for data.
         */
        public DisplayData(String data, boolean mapping, int charset) {
            Data = data;
            ServiceIsMapping = mapping;
            CharacterSet = charset;
        }
    }

    /**
     * Class describing bitmap escape sequences ESC|#B.
     */
    public static class EscBitmap extends DisplayDataPart {
        /**
         * Returns bitmap number, corresponding to bitmap number used in SetBitmap method.
         * @return Bitmap number.
         */
        public int getNumber() {
            return Number;
        }
        private int Number;

        private EscBitmap() {}

        /**
         * Checks whether the specified esc sequence parameters form a bitmap sequence. If so, it returns an EscBitmap object.
         * If not, the object given as first parameter will be returned.
         * @param obj               An object containing corresponding sequence parameters or null.
         * @param type              The sequence type (see EscUnknown, property Esc).
         * @param subtype           The subtype (see EscUnknown, property Subtype).
         * @param value             The value (see EscUnknown, property Value).
         * @return      An EscBitmap object, if the sequence is a well-formed bitmap sequence, otherwise obj.
         */
        static public DisplayDataPart getEscBitmap(DisplayDataPart obj, int type, int subtype, int value) {
            if (type == 'B' && subtype == 0 && value >= 0) {
                EscBitmap esc = new EscBitmap();
                esc.Number = value;
                return esc;
            }
            return obj;
        }
    }

    /**
     * Class describing unknown escape sequence.
     */
    public static class EscUnknown extends DisplayDataPart {
        /**
         * Returns capital character that marks the end of the escape sequence.
         * @return Character between A and Z.
         */
        public int getEsc() {
            return Esc;
        }
        private final int Esc;

        /**
         * Returns value that contains the lower-case characters between value and upper-case character that marks the end
         * of the sequence. The codes of the lower-case characters are the digits of Subtype in base1000 representation,
         * e.g. if the lower-case characters between value and upper-case character are "abc", Subtype will be
         * (('a' * 1000) + 'b') * 1000 + 'c'.
         * @return Lowe-case character sequence before end character as described above.
         */
        public int getSubtype() {
            return Subtype;
        }
        private final int Subtype;

        /**
         * Returns value in ESC sequence, in any. -1 if no value is present.
         * @return Value.
         */
        public int getValue() {
            return Value;
        }
        private final int Value;

        /**
         * Constructor.
         * @param type      Initial value for Esc.
         * @param subtype   Initial value for Subtype.
         * @param value     Initial value for Value.
         */
        public EscUnknown(int type, int subtype, int value) {
            Esc = type;
            Subtype = subtype;
            Value = value;
        }
    }

    /**
     * Class describing normalize escape sequences ESC|N.
     */
    static public class EscNormalize extends DisplayDataPart {
        /**
         * Constructor.
         */
        public EscNormalize() {
        }

        /**
         * Checks whether the specified esc sequence parameters form a normalize sequence. If so, it returns an EscNormalize object.
         * If not, the object given as first parameter will be returned.
         * @param obj               An object containing corresponding sequence parameters or null.
         * @param type              The sequence type (see EscUnknown, property Esc).
         * @param subtype           The subtype (see EscUnknown, property Subtype).
         * @param value             The value (see EscUnknown, property Value).
         * @return      An EscNormalize object, if the sequence is a well-formed normalize sequence, otherwise obj.
         */
        static public DisplayDataPart getEscNormalize(DisplayDataPart obj, int type, int subtype, int value) {
            if (type == 'N' && subtype == 0 && value == -1)
                return new EscNormalize();
            return obj;
        }
    }

    /**
     * Class describing simple attribute setting escape sequences ESC|[!]xC, where x is one of b, i, rv, tb or tp.
     */
    static public class EscSimple extends DisplayDataPart {
        /**
         * Returns true in case of blinking attribute, otherwise false.
         * @return Blink attribute.
         */
        public boolean getBlinking() {
            return Blinking;
        }
        private boolean Blinking;

        /**
         * Returns true in case of reverse video attribute, otherwise false.
         * @return Reverse attribute.
         */
        public boolean getReverse() {
            return Reverse;
        }
        private boolean Reverse;

        private EscSimple() {
        }

        /**
         * Constructor. Used to create first list object whenever DisplayText attribute is not DT_NORMAL.
         * @param blinking  If set, following characters shall be displayed in blinking mode.
         * @param reverse   If set, following characters shall be displayed in reverse video mode
         */
        public EscSimple(boolean blinking, boolean reverse) {
            Blinking = blinking;
            Reverse = reverse;
        }

        /**
         * Checks whether the specified esc sequence parameters form a simple attribute sequence. If so, it returns an EscSimple object.
         * If not, the object given as first parameter will be returned. Simple attributes are bolt, italic, reverse,
         * subscript and superscript.
         * @param obj               An object containing corresponding sequence parameters or null.
         * @param type              The sequence type (see EscUnknown, property Esc).
         * @param subtype           The subtype (see EscUnknown, property Subtype). -1 for blinking and reverse video together.
         * @param value             The value (see EscUnknown, property Value).
         * @return      An EscSimple object, if the sequence is a well-formed simple attribute sequence, otherwise obj.
         */
        static public DisplayDataPart getEscSimple(DisplayDataPart obj, int type, int subtype, int value) {
            if (type == 'C' && value == -1) {
                EscSimple esc = new EscSimple();
                esc.Blinking = subtype == 'k';
                esc.Reverse = subtype == ('r' * 1000) + 'v';
                if (esc.Blinking || esc.Reverse)
                    return esc;
            }
            return obj;
        }
    }
}
