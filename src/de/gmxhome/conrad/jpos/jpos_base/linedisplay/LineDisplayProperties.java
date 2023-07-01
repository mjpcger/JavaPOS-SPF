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
 * Class containing the display specific properties, their default values and default implementations of
 * LineDisplayInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Line Display.
 */
public class    LineDisplayProperties extends JposCommonProperties implements LineDisplayInterface {
    /**
     * UPOS property CapBitmap. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapBitmap = false;

    /**
     * UPOS property CapScreenMode. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapScreenMode = false;

    /**
     * UPOS property CapMapCharacterSet. Default: true. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapMapCharacterSet = true;

    /**
     * UPOS property CapBlinkRate. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapBlinkRate = false;

    /**
     * UPOS property CapCursorType. Default: DISP_CCT_NONE. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapCursorType = LineDisplayConst.DISP_CCT_NONE;

    /**
     * UPOS property CapCustomGlyph. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapCustomGlyph = false;

    /**
     * UPOS property CapReadBack. Default: DISP_CRB_NONE. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapReadBack = LineDisplayConst.DISP_CRB_NONE;

    /**
     * UPOS property CapReverse. Default: DISP_CR_NONE. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapReverse = LineDisplayConst.DISP_CR_NONE;

    /**
     * UPOS property CapBlink. Default: DISP_CB_NOBLINK. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapBlink = LineDisplayConst.DISP_CB_NOBLINK;

    /**
     * UPOS property CapBrightness. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapBrightness = false;

    /**
     * UPOS property CapCharacterSet. Default: DISP_CCS_ASCII. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapCharacterSet = LineDisplayConst.DISP_CCS_ASCII;

    /**
     * UPOS property CapDescriptors. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapDescriptors = false;

    /**
     * UPOS property CapHMarquee. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapHMarquee = false;

    /**
     * UPOS property CapICharWait. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapICharWait = false;

    /**
     * UPOS property CapVMarquee. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapVMarquee = false;

    /**
     * UPOS property MapCharacterSet. Default: Default will be true if
     * CapMapCharacterSet is true for jpos versions 1.7 and above, false
     * otherwise. Can be overwritten by objects derived from JposDevice
     * within the changeDefaults method.
     */

    public Boolean MapCharacterSet = null;

    /**
     * UPOS property MaximumX. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int MaximumX = 0;

    /**
     * UPOS property MaximumY. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int MaximumY = 0;

    /**
     * UPOS property ScreenMode. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int ScreenMode = 0;

    /**
     * UPOS property ScreenModeList. Default: "2x20". Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String ScreenModeList = "2x20";

    /**
     * UPOS property BlinkRate. Default: DISP_CCT_NONE. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int BlinkRate = LineDisplayConst.DISP_CCT_NONE;

    /**
     * UPOS property CursorType. Default: DISP_CT_NONE. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CursorType = LineDisplayConst.DISP_CT_NONE;

    /**
     * UPOS property CustomGlyphList. Default: "". Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String CustomGlyphList = "";

    /**
     * UPOS property GlyphHeight. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int GlyphHeight = 0;

    /**
     * UPOS property GlyphWidth. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int GlyphWidth = 0;

    /**
     * Default value of CharacterSet property. Default: DISP_CS_ASCII. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CharacterSetDef = LineDisplayConst.DISP_CS_ASCII;

    /**
     * UPOS property CharacterSet.
     */
    public int CharacterSet;

    /**
     * UPOS property CharacterSetList. Default: "998" (DISP_CS_ASCII). Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String CharacterSetList = "998";

    /**
     * UPOS property Columns.
     */
    public int Columns;

    /**
     * UPOS property CurrentWindow.
     */
    public int CurrentWindow;

    /**
     * UPOS property CursorColumn.
     */
    public int CursorColumn;

    /**
     * UPOS property CursorRow.
     */
    public int CursorRow;

    /**
     * UPOS property CursorUpdate.
     */
    public boolean CursorUpdate;

    /**
     * UPOS property DeviceBrightness.
     */
    public int DeviceBrightness;

    /**
     * UPOS property DeviceColumns. Default: 20. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int DeviceColumns = 20;

    /**
     * UPOS property DeviceDescriptors. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int DeviceDescriptors = 0;

    /**
     * UPOS property BlinkRate. Default: 2. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int DeviceRows = 2;

    /**
     * UPOS property DeviceWindows. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int DeviceWindows = 0;

    /**
     * UPOS property InterCharacterWait.
     */
    public int InterCharacterWait;

    /**
     * UPOS property MarqueeFormat. Default: DISP_MF_WALK. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int MarqueeFormat = LineDisplayConst.DISP_MF_WALK;

    /**
     * UPOS property MarqueeRepeatWait.
     */
    public int MarqueeRepeatWait;

    /**
     * UPOS property MarqueeType.
     */
    public int MarqueeType;

    /**
     * UPOS property MarqueeUnitWait.
     */
    public int MarqueeUnitWait;

    /**
     * UPOS property Rows.
     */
    public int Rows;

    /**
     * Constructor. Sets ExclusiveUse to ExclusiveYes to match the LineDisplay device model.
     *
     * @param dev Device index
     */
    public LineDisplayProperties(int dev) {
        super(dev);
    }

    @Override
    public void initOnOpen() {
        super.initOnOpen();
        MarqueeType = LineDisplayConst.DISP_MT_NONE;
        CurrentWindow = 0;
        CursorColumn = 0;
        CursorRow = 0;
        CursorUpdate = true;
        InterCharacterWait = 0;
        MarqueeRepeatWait = 0;
        MarqueeUnitWait = 0;
        Columns = DeviceColumns;
        Rows = DeviceRows;
        if (DeviceServiceVersion < 1007000)
            MapCharacterSet = false;
        else if (MapCharacterSet == null)
            MapCharacterSet = CapMapCharacterSet;
    }

    @Override
    public boolean initOnFirstEnable() {
        if (!super.initOnFirstEnable()) {
            CharacterSet = CharacterSetDef;
            DeviceBrightness = 100;
            return false;
        }
        return true;
    }

    @Override
    public void mapCharacterSet(boolean b) throws JposException {
        MapCharacterSet = b;
    }

    @Override
    public void screenMode(int b) throws JposException {
        ScreenMode = b;
    }

    @Override
    public void blinkRate(int rate) throws JposException {
        BlinkRate = rate;
    }

    @Override
    public void cursorType(int type) throws JposException {
        CursorType = type;
    }

    @Override
    public void characterSet(int charset) throws JposException {
        CharacterSet = charset;
    }

    @Override
    public void currentWindow(int windowNo) throws JposException {
        CurrentWindow = windowNo;
    }

    @Override
    public void cursorColumn(int column) throws JposException {
        CursorColumn = column;
    }

    @Override
    public void cursorRow(int row) throws JposException {
        CursorRow = row;
    }

    @Override
    public void cursorUpdate(boolean flag) throws JposException {
        CursorUpdate = flag;
    }

    @Override
    public void deviceBrightness(int brightness) throws JposException {
        DeviceBrightness = brightness;
    }

    @Override
    public void interCharacterWait(int millisec) throws JposException {
        InterCharacterWait = millisec;
    }

    @Override
    public void marqueeFormat(int format) throws JposException {
        MarqueeFormat = format;
    }

    @Override
    public void marqueeRepeatWait(int millisec) throws JposException {
        MarqueeRepeatWait = millisec;
    }

    @Override
    public void marqueeType(int type) throws JposException {
        MarqueeType = type;
    }

    @Override
    public void marqueeUnitWait(int millisec) throws JposException {
        MarqueeUnitWait = millisec;
    }

    @Override
    public void displayBitmap(String fileName, int width, int alignmentX, int alignmentY) throws JposException {
    }

    @Override
    public void setBitmap(int bitmapNumber, String fileName, int width, int alignmentX, int alignmentY) throws JposException {
    }

    @Override
    public void defineGlyph(int glyphCode, byte[] glyph) throws JposException {
    }

    @Override
    public void readCharacterAtCursor(int[] cursorData) throws JposException {
    }

    @Override
    public void clearDescriptors() throws JposException {
    }

    @Override
    public void clearText() throws JposException {
    }

    @Override
    public void createWindow(int viewportRow, int viewportColumn, int viewportHeight, int viewportWidth, int windowHeight, int windowWidth) throws JposException {
        Rows = windowHeight;
        Columns = windowWidth;
        CursorRow = CursorColumn = 0;
        CursorType = LineDisplayConst.DISP_CT_NONE;
        CursorUpdate = true;
        MarqueeType = LineDisplayConst.DISP_MT_NONE;
        MarqueeFormat = LineDisplayConst.DISP_MF_WALK;
        MarqueeUnitWait = MarqueeRepeatWait = InterCharacterWait = 0;
    }

    @Override
    public void destroyWindow() throws JposException {
    }

    @Override
    public DisplayText displayText(String data, int attribute) throws JposException {
        return new DisplayText(this, data, attribute);
    }

    @Override
    public void displayText(DisplayText request) throws JposException {
    }

    @Override
    public void refreshWindow(int window) throws JposException {
    }

    @Override
    public void scrollText(int direction, int units) throws JposException {
    }

    @Override
    public void setDescriptor(int descriptor, int attribute) throws JposException {
    }
}
