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
import jpos.services.*;

import java.util.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;
import static jpos.POSPrinterConst.*;

/**
 * POSPrinter service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class POSPrinterService extends JposBase implements POSPrinterService116 {
    /**
     * Instance of a class implementing the POSPrinterInterface for pos printer specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public POSPrinterInterface POSPrinterInterface;

    private final POSPrinterProperties Data;

    private PageModePrint[] PagemodeCommand = new PageModePrint[3];

    private RotatePrint[] SidewaysCommand = new RotatePrint[3];

    private TransactionPrint[] TransactionCommand = new TransactionPrint[3];

    static private final long[] Cartridges = {
            PTR_COLOR_PRIMARY, PTR_COLOR_CUSTOM1, PTR_COLOR_CUSTOM2, PTR_COLOR_CUSTOM3, PTR_COLOR_CUSTOM4,
            PTR_COLOR_CUSTOM5, PTR_COLOR_CUSTOM6, PTR_COLOR_CYAN, PTR_COLOR_MAGENTA, PTR_COLOR_YELLOW
    };

    static private final long[] Metrics = { PTR_MM_DOTS, PTR_MM_TWIPS, PTR_MM_ENGLISH, PTR_MM_METRIC };

    static private final long[] PageModeHorizontalDirections = { PTR_PD_LEFT_TO_RIGHT, PTR_PD_RIGHT_TO_LEFT };

    static private final long[] PageModeVerticalDirections = { PTR_PD_BOTTOM_TO_TOP, PTR_PD_TOP_TO_BOTTOM };

    static private final long[] PrintSides = { PTR_PS_OPPOSITE, PTR_PS_SIDE1, PTR_PS_SIDE2 };

    static private final long[] SingleStations = { PTR_S_JOURNAL, PTR_S_RECEIPT, PTR_S_SLIP };

    static private final long[] Alignments = { PTR_BC_LEFT, PTR_BC_CENTER, PTR_BC_RIGHT };

    static private final long[] HRITextPositions = { PTR_BC_TEXT_ABOVE, PTR_BC_TEXT_BELOW, PTR_BC_TEXT_NONE };

    static private final long[] RuledLineStyles = { PTR_LS_SINGLE_SOLID_LINE, PTR_LS_DOUBLE_SOLID_LINE, PTR_LS_BROKEN_LINE, PTR_LS_CHAIN_LINE };

    static private final long[] MarkFeedTypes = { PTR_MF_TO_TAKEUP, PTR_MF_TO_CUTTER, PTR_MF_TO_CURRENT_TOF, PTR_MF_TO_NEXT_TOF };

    static private final long[] BitmapTypes = { PTR_BMT_BMP, PTR_BMT_JPEG, PTR_BMT_GIF };

    static private final long[] Rotations = { PTR_RP_NORMAL, PTR_RP_ROTATE180, PTR_RP_LEFT90, PTR_RP_RIGHT90 };

    static private final long[] PageModes = { PTR_PM_PAGE_MODE, PTR_PM_PRINT_SAVE, PTR_PM_NORMAL, PTR_PM_CANCEL };

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public POSPrinterService(POSPrinterProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public void clearOutput() throws JposException {
        super.clearOutput();
        Data.clearErrorProperties();
        TransactionCommand = null;
        SidewaysCommand = null;
        PagemodeCommand = null;
    }

    @Override
    public int getCapCharacterSet() throws JposException {
        checkOpened();
        logGet("CapCharacterSet");
        return Data.CapCharacterSet;
    }

    @Override
    public boolean getCapConcurrentJrnRec() throws JposException {
        checkOpened();
        logGet("CapConcurrentJrnRec");
        return Data.CapConcurrentJrnRec;
    }

    @Override
    public boolean getCapConcurrentJrnSlp() throws JposException {
        checkOpened();
        logGet("CapConcurrentJrnSlp");
        return Data.CapConcurrentJrnSlp;
    }

    @Override
    public boolean getCapConcurrentPageMode() throws JposException {
        checkOpened();
        logGet("CapConcurrentPageMode");
        return Data.CapConcurrentPageMode;
    }

    @Override
    public boolean getCapConcurrentRecSlp() throws JposException {
        checkOpened();
        logGet("CapConcurrentRecSlp");
        return Data.CapConcurrentRecSlp;
    }

    @Override
    public boolean getCapCoverSensor() throws JposException {
        checkOpened();
        logGet("CapCoverSensor");
        return Data.CapCoverSensor;
    }

    @Override
    public boolean getCapJrn2Color() throws JposException {
        checkOpened();
        logGet("CapJrn2Color");
        return Data.CapJrn2Color;
    }

    @Override
    public boolean getCapJrnBold() throws JposException {
        checkOpened();
        logGet("CapJrnBold");
        return Data.CapJrnBold;
    }

    @Override
    public int getCapJrnCartridgeSensor() throws JposException {
        checkOpened();
        logGet("CapCharacterSet");
        return Data.CapCharacterSet;
    }

    @Override
    public int getCapJrnColor() throws JposException {
        checkOpened();
        logGet("CapJrnColor");
        return Data.CapJrnColor;
    }

    @Override
    public boolean getCapJrnDhigh() throws JposException {
        checkOpened();
        logGet("CapJrnDhigh");
        return Data.CapJrnDhigh;
    }

    @Override
    public boolean getCapJrnDwide() throws JposException {
        checkOpened();
        logGet("CapJrnDwide");
        return Data.CapJrnDwide;
    }

    @Override
    public boolean getCapJrnDwideDhigh() throws JposException {
        checkOpened();
        logGet("CapJrnDwideDhigh");
        return Data.CapJrnDwideDhigh;
    }

    @Override
    public boolean getCapJrnEmptySensor() throws JposException {
        checkOpened();
        logGet("CapJrnEmptySensor");
        return Data.CapJrnEmptySensor;
    }

    @Override
    public boolean getCapJrnItalic() throws JposException {
        checkOpened();
        logGet("CapJrnItalic");
        return Data.CapJrnItalic;
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
    public boolean getCapJrnUnderline() throws JposException {
        checkOpened();
        logGet("CapJrnUnderline");
        return Data.CapJrnUnderline;
    }

    @Override
    public boolean getCapMapCharacterSet() throws JposException {
        checkOpened();
        logGet("CapMapCharacterSet");
        return Data.CapMapCharacterSet;
    }

    @Override
    public boolean getCapRec2Color() throws JposException {
        checkOpened();
        logGet("CapRec2Color");
        return Data.CapRec2Color;
    }

    @Override
    public boolean getCapRecBarCode() throws JposException {
        checkOpened();
        logGet("CapRecBarCode");
        return Data.CapRecBarCode;
    }

    @Override
    public boolean getCapRecBitmap() throws JposException {
        checkOpened();
        logGet("CapRecBitmap");
        return Data.CapRecBitmap;
    }

    @Override
    public boolean getCapRecBold() throws JposException {
        checkOpened();
        logGet("CapRecBold");
        return Data.CapRecBold;
    }

    @Override
    public int getCapRecCartridgeSensor() throws JposException {
        checkOpened();
        logGet("CapRecCartridgeSensor");
        return Data.CapRecCartridgeSensor;
    }

    @Override
    public int getCapRecColor() throws JposException {
        checkOpened();
        logGet("CapRecColor");
        return Data.CapRecColor;
    }

    @Override
    public boolean getCapRecDhigh() throws JposException {
        checkOpened();
        logGet("CapRecDhigh");
        return Data.CapRecDhigh;
    }

    @Override
    public boolean getCapRecDwide() throws JposException {
        checkOpened();
        logGet("CapRecDwide");
        return Data.CapRecDwide;
    }

    @Override
    public boolean getCapRecDwideDhigh() throws JposException {
        checkOpened();
        logGet("CapRecDwideDhigh");
        return Data.CapRecDwideDhigh;
    }

    @Override
    public boolean getCapRecEmptySensor() throws JposException {
        checkOpened();
        logGet("CapRecEmptySensor");
        return Data.CapRecEmptySensor;
    }

    @Override
    public boolean getCapRecItalic() throws JposException {
        checkOpened();
        logGet("CapRecItalic");
        return Data.CapRecItalic;
    }

    @Override
    public boolean getCapRecLeft90() throws JposException {
        checkOpened();
        logGet("CapRecLeft90");
        return Data.CapRecLeft90;
    }

    @Override
    public int getCapRecMarkFeed() throws JposException {
        checkOpened();
        logGet("CapRecMarkFeed");
        return Data.CapRecMarkFeed;
    }

    @Override
    public boolean getCapRecNearEndSensor() throws JposException {
        checkOpened();
        logGet("CapRecNearEndSensor");
        return Data.CapRecNearEndSensor;
    }

    @Override
    public boolean getCapRecPageMode() throws JposException {
        checkOpened();
        logGet("CapRecPageMode");
        return Data.CapRecPageMode;
    }

    @Override
    public boolean getCapRecPapercut() throws JposException {
        checkOpened();
        logGet("CapRecPapercut");
        return Data.CapRecPapercut;
    }

    @Override
    public boolean getCapRecPresent() throws JposException {
        checkOpened();
        logGet("CapRecPresent");
        return Data.CapRecPresent;
    }

    @Override
    public boolean getCapRecRight90() throws JposException {
        checkOpened();
        logGet("CapRecRight90");
        return Data.CapRecRight90;
    }

    @Override
    public boolean getCapRecRotate180() throws JposException {
        checkOpened();
        logGet("CapRecRotate180");
        return Data.CapRecRotate180;
    }

    @Override
    public int getCapRecRuledLine() throws JposException {
        checkOpened();
        logGet("CapRecRuledLine");
        return Data.CapRecRuledLine;
    }

    @Override
    public boolean getCapRecStamp() throws JposException {
        checkOpened();
        logGet("CapRecStamp");
        return Data.CapRecStamp;
    }

    @Override
    public boolean getCapRecUnderline() throws JposException {
        checkOpened();
        logGet("CapRecUnderline");
        return Data.CapRecUnderline;
    }

    @Override
    public boolean getCapSlp2Color() throws JposException {
        checkOpened();
        logGet("CapSlp2Color");
        return Data.CapSlp2Color;
    }

    @Override
    public boolean getCapSlpBarCode() throws JposException {
        checkOpened();
        logGet("CapSlpBarCode");
        return Data.CapSlpBarCode;
    }

    @Override
    public boolean getCapSlpBitmap() throws JposException {
        checkOpened();
        logGet("CapSlpBitmap");
        return Data.CapSlpBitmap;
    }

    @Override
    public boolean getCapSlpBold() throws JposException {
        checkOpened();
        logGet("CapSlpBold");
        return Data.CapSlpBold;
    }

    @Override
    public boolean getCapSlpBothSidesPrint() throws JposException {
        checkOpened();
        logGet("CapSlpBothSidesPrint");
        return Data.CapSlpBothSidesPrint;
    }

    @Override
    public int getCapSlpCartridgeSensor() throws JposException {
        checkOpened();
        logGet("CapSlpCartridgeSensor");
        return Data.CapSlpCartridgeSensor;
    }

    @Override
    public int getCapSlpColor() throws JposException {
        checkOpened();
        logGet("CapSlpColor");
        return Data.CapSlpColor;
    }

    @Override
    public boolean getCapSlpDhigh() throws JposException {
        checkOpened();
        logGet("CapSlpDhigh");
        return Data.CapSlpDhigh;
    }

    @Override
    public boolean getCapSlpDwide() throws JposException {
        checkOpened();
        logGet("CapSlpDwide");
        return Data.CapSlpDwide;
    }

    @Override
    public boolean getCapSlpDwideDhigh() throws JposException {
        checkOpened();
        logGet("CapSlpDwideDhigh");
        return Data.CapSlpDwideDhigh;
    }

    @Override
    public boolean getCapSlpEmptySensor() throws JposException {
        checkOpened();
        logGet("CapSlpEmptySensor");
        return Data.CapSlpEmptySensor;
    }

    @Override
    public boolean getCapSlpFullslip() throws JposException {
        checkOpened();
        logGet("CapSlpFullslip");
        return Data.CapSlpFullslip;
    }

    @Override
    public boolean getCapSlpItalic() throws JposException {
        checkOpened();
        logGet("CapSlpItalic");
        return Data.CapSlpItalic;
    }

    @Override
    public boolean getCapSlpLeft90() throws JposException {
        checkOpened();
        logGet("CapSlpLeft90");
        return Data.CapSlpLeft90;
    }

    @Override
    public boolean getCapSlpNearEndSensor() throws JposException {
        checkOpened();
        logGet("CapSlpNearEndSensor");
        return Data.CapSlpNearEndSensor;
    }

    @Override
    public boolean getCapSlpPageMode() throws JposException {
        checkOpened();
        logGet("CapSlpPageMode");
        return Data.CapSlpPageMode;
    }

    @Override
    public boolean getCapSlpPresent() throws JposException {
        checkOpened();
        logGet("CapSlpPresent");
        return Data.CapSlpPresent;
    }

    @Override
    public boolean getCapSlpRight90() throws JposException {
        checkOpened();
        logGet("CapSlpRight90");
        return Data.CapSlpRight90;
    }

    @Override
    public boolean getCapSlpRotate180() throws JposException {
        checkOpened();
        logGet("CapSlpRotate180");
        return Data.CapSlpRotate180;
    }

    @Override
    public int getCapSlpRuledLine() throws JposException {
        checkOpened();
        logGet("CapSlpRuledLine");
        return Data.CapSlpRuledLine;
    }

    @Override
    public boolean getCapSlpUnderline() throws JposException {
        checkOpened();
        logGet("CapSlpUnderline");
        return Data.CapSlpUnderline;
    }

    @Override
    public boolean getCapTransaction() throws JposException {
        checkOpened();
        logGet("CapTransaction");
        return Data.CapTransaction;
    }

    @Override
    public int getCartridgeNotify() throws JposException {
        checkOpened();
        logGet("CartridgeNotify");
        return Data.CartridgeNotify;
    }

    @Override
    public void setCartridgeNotify(int i) throws JposException {
        logPreSet("CartridgeNotify");
        checkOpened();
        check(Data.DeviceEnabled, JPOS_E_ILLEGAL, "Device enabled");
        check((Data.CapJrnCartridgeSensor | Data.CapRecCartridgeSensor | Data.CapSlpCartridgeSensor) == 0 && i != PTR_CN_DISABLED, JPOS_E_ILLEGAL, "No cartridge sensor notification");
        check(i != PTR_CN_DISABLED && i != PTR_CN_ENABLED, JPOS_E_ILLEGAL, "Invalid cartridge notification value: " + i);
        checkNoChangedOrClaimed(Data.CartridgeNotify, i);
        POSPrinterInterface.cartridgeNotify(i);
        logSet("CartridgeNotify");
    }

    @Override
    public int getCharacterSet() throws JposException {
        checkFirstEnabled();
        logGet("CharacterSet");
        return Data.CharacterSet;
    }

    @Override
    public void setCharacterSet(int i) throws JposException {
        logPreSet("CharacterSet");
        checkEnabled();
        check(!member(i, stringArrayToLongArray(Data.CharacterSetList.split(","))), JPOS_E_ILLEGAL, "Invalid character set: " + i);
        POSPrinterInterface.characterSet(i);
        logSet("CharacterSet");
    }

    @Override
    public String getCharacterSetList() throws JposException {
        checkOpened();
        logGet("CharacterSetList");
        return Data.CharacterSetList;
    }

    @Override
    public boolean getCoverOpen() throws JposException {
        checkEnabled();
        logGet("CoverOpen");
        return Data.CoverOpen;
    }

    @Override
    public int getErrorLevel() throws JposException {
        checkOpened();
        logGet("ErrorLevel");
        return Data.ErrorLevel;
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
    public boolean getFlagWhenIdle() throws JposException {
        checkOpened();
        logGet("FlagWhenIdle");
        return Data.FlagWhenIdle;
    }

    @Override
    public void setFlagWhenIdle(boolean b) throws JposException {
        logPreSet("FlagWhenIdle");
        checkOpened();
        POSPrinterInterface.flagWhenIdle(b);
        logSet("FlagWhenIdle");
    }

    @Override
    public String getFontTypefaceList() throws JposException {
        checkOpened();
        logGet("FontTypefaceList");
        return Data.FontTypefaceList;
    }

    @Override
    public int getJrnCartridgeState() throws JposException {
        checkEnabled();
        logGet("JrnCartridgeState");
        return Data.JrnCartridgeState;
    }

    @Override
    public int getJrnCurrentCartridge() throws JposException {
        checkFirstEnabled();
        logGet("JrnCurrentCartridge");
        return Data.JrnCurrentCartridge;
    }

    @Override
    public void setJrnCurrentCartridge(int i) throws JposException {
        logPreSet("JrnCurrentCartridge");
        checkEnabled();
        if (Data.CapJrnPresent) {
            checkMember(i, Cartridges, JPOS_E_ILLEGAL, "Invalid cartridge: " + i);
            check((i & Data.CapJrnColor) == 0, JPOS_E_ILLEGAL, "Unsupported color: " + 1);
        }
        else
            check(i != 0, JPOS_E_ILLEGAL, "Journal not present");
        POSPrinterInterface.jrnCurrentCartridge(i);
        logSet("JrnCurrentCartridge");
    }

    @Override
    public boolean getJrnEmpty() throws JposException {
        checkEnabled();
        logGet("JrnEmpty");
        return Data.JrnEmpty;
    }

    @Override
    public boolean getJrnLetterQuality() throws JposException {
        checkFirstEnabled();
        logGet("JrnLetterQuality");
        return Data.JrnLetterQuality;
    }

    @Override
    public void setJrnLetterQuality(boolean b) throws JposException {
        logPreSet("JrnLetterQuality");
        checkEnabled();
        check(!Data.CapJrnPresent, JPOS_E_ILLEGAL, "Journal not present");
        POSPrinterInterface.jrnLetterQuality(b);
        logSet("JrnLetterQuality");
    }

    @Override
    public int getJrnLineChars() throws JposException {
        checkFirstEnabled();
        logGet("JrnLineChars");
        return Data.JrnLineChars;
    }

    @Override
    public void setJrnLineChars(int i) throws JposException {
        logPreSet("JrnLineChars");
        checkEnabled();
        check(!Data.CapJrnPresent, JPOS_E_ILLEGAL, "Journal not present");
        check(i < 0 || i > max(stringArrayToLongArray(Data.JrnLineCharsList.split(","))), JPOS_E_ILLEGAL, "Value for JrnLineChars out of range: " + i);
        POSPrinterInterface.jrnLineChars(i);
        logSet("JrnLineChars");
    }

    @Override
    public String getJrnLineCharsList() throws JposException {
        checkOpened();
        logGet("JrnLineCharsList");
        return Data.JrnLineCharsList;
    }

    @Override
    public int getJrnLineHeight() throws JposException {
        checkFirstEnabled();
        logGet("JrnLineHeight");
        return Data.JrnLineHeight;
    }

    @Override
    public void setJrnLineHeight(int i) throws JposException {
        logPreSet("JrnLineHeight");
        checkEnabled();
        check(!Data.CapJrnPresent, JPOS_E_ILLEGAL, "Journal not present");
        POSPrinterInterface.jrnLineHeight(i);
        logSet("JrnLineHeight");
    }

    @Override
    public int getJrnLineSpacing() throws JposException {
        checkFirstEnabled();
        logGet("JrnLineSpacing");
        return Data.JrnLineSpacing;
    }

    @Override
    public void setJrnLineSpacing(int i) throws JposException {
        logPreSet("JrnLineSpacing");
        checkEnabled();
        check(!Data.CapJrnPresent, JPOS_E_ILLEGAL, "Journal not present");
        POSPrinterInterface.jrnLineSpacing(i);
        logSet("JrnLineSpacing");
    }

    @Override
    public int getJrnLineWidth() throws JposException {
        checkFirstEnabled();
        logGet("JrnLineWidth");
        return Data.JrnLineWidth;
    }

    @Override
    public boolean getJrnNearEnd() throws JposException {
        checkEnabled();
        logGet("JrnNearEnd");
        return Data.JrnNearEnd;
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
        checkEnabled();
        check(!Data.CapMapCharacterSet && b, JPOS_E_ILLEGAL, "Mapping character set not supported");
        POSPrinterInterface.mapCharacterSet(b);
        logSet("MapCharacterSet");
    }

    @Override
    public int getMapMode() throws JposException {
        checkFirstEnabled();
        logGet("MapMode");
        return Data.MapMode;
    }

    @Override
    public void setMapMode(int i) throws JposException {
        logPreSet("MapMode");
        checkEnabled();
        checkMember(i, Metrics, JPOS_E_ILLEGAL, "Invalid MapMode: " + i);
        POSPrinterInterface.mapMode(i);
        logSet("MapMode");
    }

    @Override
    public String getPageModeArea() throws JposException {
        checkOpened();
        if (Data.PageModeStation == 0)
            Data.PageModeArea = "";
        logGet("PageModeArea");
        return Data.PageModeArea;
    }

    @Override
    public int getPageModeDescriptor() throws JposException {
        checkOpened();
        if (Data.PageModeStation == 0)
            Data.PageModeDescriptor = 0;
        logGet("PageModeDescriptor");
        return Data.PageModeDescriptor;
    }

    @Override
    public int getPageModeHorizontalPosition() throws JposException {
        checkOpened();
        if (Data.PageModeStation == 0)
            Data.PageModeHorizontalPosition = 0;
        logGet("PageModeHorizontalPosition");
        return Data.PageModeHorizontalPosition;
    }

    @Override
    public void setPageModeHorizontalPosition(int i) throws JposException {
        logPreSet("PageModeHorizontalPosition");
        checkEnabled();
        check(Data.PageModeStation == 0, JPOS_E_ILLEGAL, "PageModeStation not selected");
        POSPrinterInterface.pageModeHorizontalPosition(i);
        logSet("PageModeHorizontalPosition");
    }

    @Override
    public String getPageModePrintArea() throws JposException {
        checkOpened();
        if (Data.PageModeStation == 0)
            Data.PageModePrintArea = "";
        logGet("PageModePrintArea");
        return Data.PageModePrintArea;
    }

    @Override
    public void setPageModePrintArea(String s) throws JposException {
        logPreSet("PageModePrintArea");
        checkEnabled();
        if (s == null)
            s = "";
        check(Data.PageModeStation == 0, JPOS_E_ILLEGAL, "PageModeStation not selected");
        long[] values = stringArrayToLongArray(s.split(","));
        long maxwidth = Data.PageModeStation == PTR_S_RECEIPT ? Data.RecLineWidth : Data.SlpLineWidth;
        check(values.length != 4, JPOS_E_ILLEGAL, "PageModePrintArea must consist of 4 comma separated integer numbers");
        check(values[0] < 0 || values[1] < 0 || values[2] < 0 ||values[3] < 0, JPOS_E_ILLEGAL, ("PageModePrintArea values must be positive numbers"));
        check(member((long)Data.PageModePrintDirection, PageModeHorizontalDirections) && values[0] + values[2] > maxwidth, JPOS_E_ILLEGAL, "PageModePrintArea width out of range");
        check(member((long)Data.PageModePrintDirection, PageModeVerticalDirections) && values[1] + values[3] > maxwidth, JPOS_E_ILLEGAL, "PageModePrintArea height out of range");
        if (Data.PageModeStation == PTR_S_SLIP && Data.SlpMaxLines > 0) {
            long maxheight = (long) Data.SlpLineHeight * Data.SlpMaxLines;
            check(member((long)Data.PageModePrintDirection, PageModeHorizontalDirections) && values[1] + values[3] > maxheight, JPOS_E_ILLEGAL, "PageModePrintArea height out of range");
            check(member((long)Data.PageModePrintDirection, PageModeVerticalDirections) && values[0] + values[2] > maxheight, JPOS_E_ILLEGAL, "PageModePrintArea width out of range");
        }
        POSPrinterInterface.pageModePrintArea(s);
        logSet("PageModePrintArea");
    }

    @Override
    public int getPageModePrintDirection() throws JposException {
        checkFirstEnabled();
        logGet("PageModePrintDirection");
        return Data.PageModePrintDirection;
    }

    @Override
    public void setPageModePrintDirection(int i) throws JposException {
        logPreSet("PageModePrintDirection");
        checkEnabled();
        check(Data.PageModeStation == 0, JPOS_E_ILLEGAL, "PageModeStation not selected");
        check(!member(i, PageModeVerticalDirections) && !member(i, PageModeHorizontalDirections), JPOS_E_ILLEGAL, "Invalid PageModePrintDirection: " + i);
        POSPrinterInterface.pageModePrintDirection(i);
        logSet("PageModePrintDirection");
    }

    @Override
    public int getPageModeStation() throws JposException {
        checkOpened();
        logGet("PageModeStation");
        return Data.PageModeStation;
    }

    @Override
    public void setPageModeStation(int i) throws JposException {
        logPreSet("PageModeStation");
        checkEnabled();
        checkMember(i, new long[]{PTR_S_RECEIPT, PTR_S_SLIP}, JPOS_E_ILLEGAL, "Invalid PageModeStation: " + i);
        check(i == PTR_S_RECEIPT && !Data.CapRecPageMode, JPOS_E_ILLEGAL, "No page mode support on receipt");
        check(i == PTR_S_SLIP && !Data.CapSlpPageMode, JPOS_E_ILLEGAL, "No page mode support on slip");
        POSPrinterInterface.pageModeStation(i);
        logSet("PageModeStation");
    }

    @Override
    public int getPageModeVerticalPosition() throws JposException {
        checkOpened();
        if (Data.PageModeStation == 0)
            Data.PageModeVerticalPosition = 0;
        logGet("PageModeVerticalPosition");
        return Data.PageModeVerticalPosition;
    }

    @Override
    public void setPageModeVerticalPosition(int i) throws JposException {
        logPreSet("PageModeVerticalPosition");
        checkEnabled();
        check(Data.PageModeStation == 0, JPOS_E_ILLEGAL, "PageModeStation not selected");
        POSPrinterInterface.pageModeVerticalPosition(i);
        logSet("PageModeVerticalPosition");
    }

    @Override
    public String getRecBarCodeRotationList() throws JposException {
        checkOpened();
        logGet("RecBarCodeRotationList");
        return Data.RecBarCodeRotationList;
    }

    @Override
    public String getRecBitmapRotationList() throws JposException {
        checkOpened();
        logGet("RecBitmapRotationList");
        return Data.RecBitmapRotationList;
    }

    @Override
    public int getRecCartridgeState() throws JposException {
        checkOpened();
        logGet("RecCartridgeState");
        return Data.RecCartridgeState;
    }

    @Override
    public int getRecCurrentCartridge() throws JposException {
        checkFirstEnabled();
        logGet("RecCurrentCartridge");
        return Data.RecCurrentCartridge;
    }

    @Override
    public void setRecCurrentCartridge(int i) throws JposException {
        logPreSet("RecCurrentCartridge");
        checkEnabled();
        if (Data.CapRecPresent) {
            checkMember(i, Cartridges, JPOS_E_ILLEGAL, "Invalid cartridge: " + i);
            check((i & Data.CapRecColor) == 0, JPOS_E_ILLEGAL, "Unsupported color: " + 1);
        }
        else
            check(i != 0, JPOS_E_ILLEGAL, "Receipt not present");
        POSPrinterInterface.recCurrentCartridge(i);
        logSet("RecCurrentCartridge");
    }

    @Override
    public boolean getRecEmpty() throws JposException {
        checkEnabled();
        logGet("RecEmpty");
        return Data.RecEmpty;
    }

    @Override
    public boolean getRecLetterQuality() throws JposException {
        checkFirstEnabled();
        logGet("RecLetterQuality");
        return Data.RecLetterQuality;
    }

    @Override
    public void setRecLetterQuality(boolean b) throws JposException {
        logPreSet("RecLetterQuality");
        checkEnabled();
        check(!Data.CapRecPresent, JPOS_E_ILLEGAL, "Receipt not present");
        POSPrinterInterface.recLetterQuality(b);
        logSet("RecLetterQuality");
    }

    @Override
    public int getRecLineChars() throws JposException {
        checkFirstEnabled();
        logGet("RecLineChars");
        return Data.RecLineChars;
    }

    @Override
    public void setRecLineChars(int i) throws JposException {
        logPreSet("RecLineChars");
        checkEnabled();
        check(!Data.CapRecPresent, JPOS_E_ILLEGAL, "Receipt not present");
        check(i < 0 || i > max(stringArrayToLongArray(Data.RecLineCharsList.split(","))), JPOS_E_ILLEGAL, "Value for RecLineChars out of range: " + i);
        POSPrinterInterface.recLineChars(i);
        logSet("RecLineChars");
    }

    @Override
    public String getRecLineCharsList() throws JposException {
        checkOpened();
        logGet("RecLineCharsList");
        return Data.RecLineCharsList;
    }

    @Override
    public int getRecLineHeight() throws JposException {
        checkFirstEnabled();
        logGet("RecLineHeight");
        return Data.RecLineHeight;
    }

    @Override
    public void setRecLineHeight(int i) throws JposException {
        logPreSet("RecLineHeight");
        checkEnabled();
        check(!Data.CapRecPresent, JPOS_E_ILLEGAL, "Receipt not present");
        POSPrinterInterface.recLineHeight(i);
        logSet("RecLineHeight");
    }

    @Override
    public int getRecLineSpacing() throws JposException {
        checkFirstEnabled();
        logGet("RecLineSpacing");
        return Data.RecLineSpacing;
    }

    @Override
    public void setRecLineSpacing(int i) throws JposException {
        logPreSet("RecLineSpacing");
        checkEnabled();
        check(!Data.CapRecPresent, JPOS_E_ILLEGAL, "Receipt not present");
        POSPrinterInterface.recLineSpacing(i);
        logSet("RecLineSpacing");
    }

    @Override
    public int getRecLinesToPaperCut() throws JposException {
        checkFirstEnabled();
        logGet("RecLinesToPaperCut");
        return Data.RecLinesToPaperCut;
    }

    @Override
    public int getRecLineWidth() throws JposException {
        checkFirstEnabled();
        logGet("RecLineWidth");
        return Data.RecLineWidth;
    }

    @Override
    public boolean getRecNearEnd() throws JposException {
        checkEnabled();
        logGet("RecNearEnd");
        return Data.RecNearEnd;
    }

    @Override
    public int getRecSidewaysMaxChars() throws JposException {
        checkFirstEnabled();
        logGet("RecSidewaysMaxChars");
        return Data.RecSidewaysMaxChars;
    }

    @Override
    public int getRecSidewaysMaxLines() throws JposException {
        checkFirstEnabled();
        logGet("RecSidewaysMaxLines");
        return Data.RecSidewaysMaxLines;
    }

    @Override
    public int getRotateSpecial() throws JposException {
        checkOpened();
        logGet("RotateSpecial");
        return Data.RotateSpecial;
    }

    @Override
    public void setRotateSpecial(int i) throws JposException {
        logPreSet("RotateSpecial");
        checkOpened();
        check(!Data.CapRecBarCode && !Data.CapSlpBarCode, JPOS_E_ILLEGAL, "Barcode not supported");
        checkNoChangedOrClaimed(Data.RotateSpecial, i);
        POSPrinterInterface.rotateSpecial(i);
        logSet("RotateSpecial");
    }

    @Override
    public String getSlpBarCodeRotationList() throws JposException {
        checkOpened();
        logGet("SlpBarCodeRotationList");
        return Data.SlpBarCodeRotationList;
    }

    @Override
    public String getSlpBitmapRotationList() throws JposException {
        checkOpened();
        logGet("SlpBitmapRotationList");
        return Data.SlpBitmapRotationList;
    }

    @Override
    public int getSlpCartridgeState() throws JposException {
        checkOpened();
        logGet("SlpCartridgeState");
        return Data.SlpCartridgeState;
    }

    @Override
    public int getSlpCurrentCartridge() throws JposException {
        checkFirstEnabled();
        logGet("SlpCurrentCartridge");
        return Data.SlpCurrentCartridge;
    }

    @Override
    public void setSlpCurrentCartridge(int i) throws JposException {
        logPreSet("SlpCurrentCartridge");
        checkEnabled();
        if (Data.CapSlpPresent) {
            checkMember(i, Cartridges, JPOS_E_ILLEGAL, "Invalid cartridge: " + i);
            check((i & Data.CapSlpColor) == 0, JPOS_E_ILLEGAL, "Unsupported color: " + 1);
        }
        else
            check(i != 0, JPOS_E_ILLEGAL, "Slip not present");
        POSPrinterInterface.slpCurrentCartridge(i);
        logSet("SlpCurrentCartridge");
    }

    @Override
    public boolean getSlpEmpty() throws JposException {
        checkEnabled();
        logGet("SlpEmpty");
        return Data.SlpEmpty;
    }

    @Override
    public boolean getSlpLetterQuality() throws JposException {
        checkFirstEnabled();
        logGet("SlpLetterQuality");
        return Data.SlpLetterQuality;
    }

    @Override
    public void setSlpLetterQuality(boolean b) throws JposException {
        logPreSet("SlpLetterQuality");
        checkEnabled();
        check(!Data.CapSlpPresent, JPOS_E_ILLEGAL, "Slip not present");
        POSPrinterInterface.slpLetterQuality(b);
        logSet("SlpLetterQuality");
    }

    @Override
    public int getSlpLineChars() throws JposException {
        checkFirstEnabled();
        logGet("SlpLineChars");
        return Data.SlpLineChars;
    }

    @Override
    public void setSlpLineChars(int i) throws JposException {
        logPreSet("SlpLineChars");
        checkEnabled();
        check(!Data.CapSlpPresent, JPOS_E_ILLEGAL, "Slip not present");
        check(i < 0 || i > max(stringArrayToLongArray(Data.SlpLineCharsList.split(","))), JPOS_E_ILLEGAL, "Value for SlpLineChars out of range: " + i);
        POSPrinterInterface.slpLineChars(i);
        logSet("SlpLineChars");
    }

    @Override
    public String getSlpLineCharsList() throws JposException {
        checkOpened();
        logGet("SlpLineCharsList");
        return Data.SlpLineCharsList;
    }

    @Override
    public int getSlpLineHeight() throws JposException {
        checkFirstEnabled();
        logGet("SlpLineHeight");
        return Data.SlpLineHeight;
    }

    @Override
    public void setSlpLineHeight(int i) throws JposException {
        logPreSet("SlpLineHeight");
        checkEnabled();
        check(!Data.CapSlpPresent, JPOS_E_ILLEGAL, "Slip not present");
        POSPrinterInterface.slpLineHeight(i);
        logSet("SlpLineHeight");
    }

    @Override
    public int getSlpLinesNearEndToEnd() throws JposException {
        checkFirstEnabled();
        logGet("SlpLinesNearEndToEnd");
        return Data.SlpLinesNearEndToEnd;
    }

    @Override
    public int getSlpLineSpacing() throws JposException {
        checkFirstEnabled();
        logGet("SlpLineSpacing");
        return Data.SlpLineSpacing;
    }

    @Override
    public void setSlpLineSpacing(int i) throws JposException {
        logPreSet("SlpLineSpacing");
        checkEnabled();
        check(!Data.CapSlpPresent, JPOS_E_ILLEGAL, "Slip not present");
        POSPrinterInterface.slpLineSpacing(i);
        logSet("SlpLineSpacing");
    }

    @Override
    public int getSlpLineWidth() throws JposException {
        checkFirstEnabled();
        logGet("SlpLineWidth");
        return Data.SlpLineWidth;
    }

    @Override
    public int getSlpMaxLines() throws JposException {
        checkFirstEnabled();
        logGet("SlpMaxLines");
        return Data.SlpMaxLines;
    }

    @Override
    public boolean getSlpNearEnd() throws JposException {
        checkEnabled();
        logGet("SlpNearEnd");
        return Data.SlpNearEnd;
    }

    @Override
    public int getSlpPrintSide() throws JposException {
        checkEnabled();
        logGet("SlpPrintSide");
        return Data.SlpPrintSide;
    }

    @Override
    public int getSlpSidewaysMaxChars() throws JposException {
        checkFirstEnabled();
        logGet("SlpSidewaysMaxChars");
        return Data.SlpSidewaysMaxChars;
    }

    @Override
    public int getSlpSidewaysMaxLines() throws JposException {
        checkFirstEnabled();
        logGet("SlpSidewaysMaxLines");
        return Data.SlpSidewaysMaxLines;
    }

    @Override
    public void beginInsertion(int timeout) throws JposException {
        logPreCall("BeginInsertion", removeOuterArraySpecifier(new Object[]{timeout}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapSlpPresent, JPOS_E_ILLEGAL, "No Slip station support");
        check(Props.State != JPOS_S_IDLE, JPOS_E_BUSY, "Output in progress or error detected");
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout value: " + timeout);
        POSPrinterInterface.beginInsertion(timeout);
        Data.InsertionMode = true;
        logCall("BeginInsertion");
    }

    @Override
    public void beginRemoval(int timeout) throws JposException {
        logPreCall("BeginRemoval", removeOuterArraySpecifier(new Object[]{timeout}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapSlpPresent, JPOS_E_ILLEGAL, "No Slip station support");
        check(Props.State != JPOS_S_IDLE, JPOS_E_BUSY, "Output in progress or error detected");
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout value: " + timeout);
        POSPrinterInterface.beginRemoval(timeout);
        Data.RemovalMode = true;
        logCall("BeginRemoval");
    }

    @Override
    public void changePrintSide(int side) throws JposException {
        logPreCall("ChangePrintSide", removeOuterArraySpecifier(new Object[]{side}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapSlpPresent || !Data.CapSlpBothSidesPrint, JPOS_E_ILLEGAL, "No support for both side slip printing");
        check(Props.State != JPOS_S_IDLE, JPOS_E_BUSY, "Output in progress or error detected");
        checkMember(side, PrintSides, JPOS_E_ILLEGAL, "Bad print side: " + side);
        POSPrinterInterface.changePrintSide(side);
        logCall("ChangePrintSide");
    }

    @Override
    public void clearPrintArea() throws JposException {
        logPreCall("ClearPrintArea");
        checkEnabled();
        check(Data.PageModeStation == 0, JPOS_E_ILLEGAL, "No page mode station selected");
        POSPrinterInterface.clearPrintArea();
        logCall("ClearPrintArea");
    }

    @Override
    public void endInsertion() throws JposException {
        logPreCall("EndInsertion");
        checkEnabled();
        check(!Data.CapSlpPresent, JPOS_E_ILLEGAL, "No Slip station support");
        check(!Data.InsertionMode, JPOS_E_ILLEGAL, "Not in insertion mode");
        check(Props.State != JPOS_S_IDLE, JPOS_E_BUSY, "Output in progress or error detected");
        POSPrinterInterface.endInsertion();
        logCall("EndInsertion");
    }

    @Override
    public void endRemoval() throws JposException {
        logPreCall("EndRemoval");
        checkEnabled();
        check(!Data.CapSlpPresent, JPOS_E_ILLEGAL, "No Slip station support");
        check(!Data.RemovalMode, JPOS_E_ILLEGAL, "Not in removal mode");
        check(Props.State != JPOS_S_IDLE, JPOS_E_BUSY, "Output in progress or error detected");
        POSPrinterInterface.endRemoval();
        Data.RemovalMode = false;
        logCall("EndRemoval");
    }

    @Override
    public void printImmediate(int station, String data) throws JposException {
        logPreCall("PrintImmediate", removeOuterArraySpecifier(new Object[]{station, data}, Device.MaxArrayStringElements));
        checkEnabled();
        checkStationPresent(station);
        extendedErrorCheck(station);
        PrintImmediate request = POSPrinterInterface.printImmediate(station, data == null ? "" : data);
        OutputPrintRequest.setSynchronousPrinting(request);
        request.enqueueSynchronous();
        if (request.Exception != null)
            throw request.Exception;
        logCall("PrintImmediate");
    }

    @Override
    public void setBitmap(int bitmapNumber, int station, String fileName, int width, int alignment) throws JposException {
        logPreCall("SetBitmap", removeOuterArraySpecifier(new Object[]{bitmapNumber, station, fileName, width, alignment}, Device.MaxArrayStringElements));
        checkEnabled();
        check(bitmapNumber < 1 || bitmapNumber > 20, JPOS_E_ILLEGAL, "BitmapNumber out of range: " + bitmapNumber);
        checkMember(station, new long[]{PTR_S_RECEIPT, PTR_S_SLIP}, JPOS_E_ILLEGAL, "Invalid station: " + station);
        check(station == PTR_S_RECEIPT && (!Data.CapRecBitmap || !Data.CapRecPresent), JPOS_E_ILLEGAL, "No bitmap support for receipt");
        check(station == PTR_S_SLIP && (!Data.CapSlpBitmap || !Data.CapSlpPresent), JPOS_E_ILLEGAL, "No bitmap support for slip");
        check(width != PTR_BM_ASIS && width <= 0, JPOS_E_ILLEGAL, "Invalid width: " + width);
        check(!member(alignment, new long[]{PTR_BM_LEFT, PTR_BM_CENTER, PTR_BM_RIGHT}) && alignment < 0, JPOS_E_ILLEGAL, "Invalid alignment: " + alignment);
        POSPrinterInterface.setBitmap(bitmapNumber, station, fileName == null ? "" : fileName, width, alignment);
        logCall("SetBitmap");
    }

    @Override
    public void setLogo(int location, String data) throws JposException {
        logPreCall("SetLogo", removeOuterArraySpecifier(new Object[]{location, data}, Device.MaxArrayStringElements));
        checkEnabled();
        checkMember(location, new long[]{PTR_L_TOP, PTR_L_BOTTOM}, JPOS_E_ILLEGAL, "Invalid location: " + location);
        POSPrinterInterface.setLogo(location, data == null ? "" : data);
        logCall("SetLogo");
    }

    @Override
    public void validateData(int station, String text) throws JposException {
        logPreCall("ValidateData", removeOuterArraySpecifier(new Object[]{station, text}, Device.MaxArrayStringElements));
        checkEnabled();
        try {
            POSPrinterInterface.validateData(station, text);
        } catch (JposException e) {
            if (e.getErrorCode() != 0)
                throw e;
            return;
        }
        List<PrintDataPart> data = text == null ? new ArrayList<>() : outputDataParts(text);
        switch (station) {
            case PTR_S_JOURNAL -> plausibilityCheckJournalData(data);
            case PTR_S_RECEIPT -> plausibilityCheckReceiptData(data);
            case PTR_S_SLIP -> plausibilityCheckSlipData(data);
            default -> throw new JposException(JPOS_E_NOEXIST, "Invalid station: " + station);
        }
        logCall("ValidateData");
    }

    @Override
    public void cutPaper(int percentage) throws JposException {
        logPreCall("CutPaper", removeOuterArraySpecifier(new Object[]{percentage}, Device.MaxArrayStringElements));
        int stationIndex = getStationIndex(PTR_S_RECEIPT);
        checkEnabled();
        check(!Data.CapRecPresent || !Data.CapRecPapercut, JPOS_E_ILLEGAL, "Cut paper not supported");
        check(PagemodeCommand[stationIndex] != null || SidewaysCommand[stationIndex] != null, JPOS_E_ILLEGAL, "Bad context for cut paper");
        extendedSynchronousErrorCheck(PTR_S_RECEIPT);
        doItTrans(stationIndex, POSPrinterInterface.cutPaper(percentage), "CutPaper");
    }

    @Override
    public void drawRuledLine(int station, String positionList, int lineDirection, int lineWidth, int lineStyle, int lineColor) throws JposException {
        logPreCall("DrawRuledLine", removeOuterArraySpecifier(new Object[]{station, positionList, lineDirection, lineWidth, lineStyle, lineColor}, Device.MaxArrayStringElements));
        String esc = "p" + (positionList == null ? "" : positionList) + "d" + lineDirection + "w" + lineWidth + "s" + lineStyle + "c" + lineColor;
        try {
            plausibilityCheckData(station, outputDataParts("\33|*" + esc.length() + "dL" + esc));
        } catch (JposException e) {
            check(e.getErrorCode() == JPOS_E_FAILURE, JPOS_E_ILLEGAL, e.getMessage());
            if (e.getErrorCode() != JPOS_E_ILLEGAL)
                throw e;
        }
        int stationIndex = getStationIndex(station);
        extendedSynchronousErrorCheck(station);
        check(SidewaysCommand[stationIndex] != null, JPOS_E_ILLEGAL, "No support for drawing ruled line when station is in sideways print mode");
        check(PagemodeCommand[stationIndex] != null, JPOS_E_ILLEGAL, "No support for drawing ruled line when station is in page mode");
        check(TransactionCommand[stationIndex] != null, JPOS_E_ILLEGAL, "No support for drawing ruled line when station is in transaction print mode");
        doIt(POSPrinterInterface.drawRuledLine(station, positionList == null ? "" : positionList, lineDirection, lineWidth, lineStyle, lineColor), "DrawRuledLine");
    }

    @Override
    public void markFeed(int type) throws JposException {
        logPreCall("MarkFeed", removeOuterArraySpecifier(new Object[]{type}, Device.MaxArrayStringElements));
        int stationIndex = getStationIndex(PTR_S_RECEIPT);
        checkEnabled();
        check(!Data.CapRecPresent || Data.CapRecMarkFeed == 0, JPOS_E_ILLEGAL, "Mark feed not supported");
        check((Data.CapRecMarkFeed & type) == 0 || !member(type, MarkFeedTypes), JPOS_E_ILLEGAL, "Invalid feed type: " + type);
        check(PagemodeCommand[stationIndex] != null || SidewaysCommand[stationIndex] != null || TransactionCommand[stationIndex] != null, JPOS_E_ILLEGAL, "Bad context for mark feed");
        extendedSynchronousErrorCheck(PTR_S_RECEIPT);
        doIt(POSPrinterInterface.markFeed(type), "MarkFeed");
    }

    @Override
    public void printBarCode(int station, String data, int symbology, int height, int width, int alignment, int textPosition) throws JposException {
        logPreCall("PrintBarCode", removeOuterArraySpecifier(new Object[]{station, data, symbology, height, width, alignment, textPosition}, Device.MaxArrayStringElements));
        String esc = "s" + symbology + "h" + height + "w" + width + "a" + alignment + "t" + textPosition + "d" + (data == null ? "" : data) + "e";
        try {
            plausibilityCheckData(station, outputDataParts("\33|*" + esc.length() + "R" + esc));
        } catch (JposException e) {
            check(e.getErrorCode() == JPOS_E_FAILURE, JPOS_E_ILLEGAL, e.getMessage());
            if (e.getErrorCode() != JPOS_E_ILLEGAL)
                throw e;
        }
        int stationIndex = getStationIndex(station);
        extendedSynchronousErrorCheck(station);
        doItSidewaysTransPagemode(stationIndex, POSPrinterInterface.printBarCode(station, data == null ? "" : data, symbology, height, width, alignment, textPosition), "PrintBarCode");
    }

    @Override
    public void printBitmap(int station, String fileName, int width, int alignment) throws JposException {
        logPreCall("PrintBitmap", removeOuterArraySpecifier(new Object[]{station, fileName, width, alignment}, Device.MaxArrayStringElements));
        checkEnabled();
        check(station != PTR_S_RECEIPT && station != PTR_S_SLIP, JPOS_E_ILLEGAL, "Invalid print station: " + station);
        check(station == PTR_S_RECEIPT && (!Data.CapRecPresent || !Data.CapRecBitmap), JPOS_E_ILLEGAL, "No bitmap printing support on receipt");
        check(station == PTR_S_SLIP && (!Data.CapSlpPresent || !Data.CapSlpBitmap), JPOS_E_ILLEGAL, "No bitmap printing support on slip");
        check(width != PTR_BM_ASIS && alignment <= 0, JPOS_E_ILLEGAL, "Invalid width: " + width);
        check(!member(alignment, Alignments) && alignment < 0, JPOS_E_ILLEGAL, "Invalid alignment: " + alignment);
        int stationIndex = getStationIndex(station);
        extendedSynchronousErrorCheck(station);
        doItSidewaysTransPagemode(stationIndex, POSPrinterInterface.printBitmap(station, fileName == null ? "" : fileName, width, alignment), "PrintBitmap");
    }

    @Override
    public void printMemoryBitmap(int station, byte[] data, int type, int width, int alignment) throws JposException {
        logPreCall("PrintMemoryBitmap", removeOuterArraySpecifier(new Object[]{station, data, type, width, alignment}, Device.MaxArrayStringElements));
        checkEnabled();
        check(station != PTR_S_RECEIPT && station != PTR_S_SLIP, JPOS_E_ILLEGAL, "Invalid print station: " + station);
        check(station == PTR_S_RECEIPT && (!Data.CapRecPresent || !Data.CapRecBitmap), JPOS_E_ILLEGAL, "No bitmap printing support on receipt");
        check(station == PTR_S_SLIP && (!Data.CapSlpPresent || !Data.CapSlpBitmap), JPOS_E_ILLEGAL, "No bitmap printing support on slip");
        checkext(!member(type, BitmapTypes), JPOS_EPTR_BADFORMAT, "Invalid bitmap format: " + type);
        check(width != PTR_BM_ASIS && width <= 0, JPOS_E_ILLEGAL, "Invalid width: " + width);
        check(!member(alignment, Alignments) && alignment < 0, JPOS_E_ILLEGAL, "Invalid alignment: " + alignment);
        int stationIndex = getStationIndex(station);
        extendedSynchronousErrorCheck(station);
        doItSidewaysTransPagemode(stationIndex, POSPrinterInterface.printMemoryBitmap(station, data == null ? new byte[0] : Arrays.copyOf(data, data.length), type, width, alignment), "PrintMemoryBitmap");
    }

    @Override
    public void printNormal(int station, String data) throws JposException {
        logPreCall("PrintNormal", removeOuterArraySpecifier(new Object[]{station, data}, Device.MaxArrayStringElements));
        checkEnabled();
        checkStationPresent(station);
        int stationIndex = getStationIndex(station);
        extendedSynchronousErrorCheck(station);
        doItSidewaysTransPagemode(stationIndex, POSPrinterInterface.printNormal(station, data), "PrintNormal");
    }

    @Override
    public void printTwoNormal(int stations, String data1, String data2) throws JposException {
        logPreCall("PrintTwoNormal", removeOuterArraySpecifier(new Object[]{stations, data1, data2}, Device.MaxArrayStringElements));
        checkEnabled();
        int[] stationIndex = new int[2];
        int[] station = new int[2];
        checkTwoStations(stations, stationIndex, station);
        extendedSynchronousErrorCheck(station[0]);
        extendedSynchronousErrorCheck(station[1]);
        doIt(POSPrinterInterface.printTwoNormal(stations, data1 == null ? "" : data1, data2 == null ? "" : data2), "PrintTwoNormal");
    }

    @Override
    public void pageModePrint(int control) throws JposException {
        logPreCall("PageModePrint", removeOuterArraySpecifier(new Object[]{control}, Device.MaxArrayStringElements));
        checkEnabled();
        int station = Data.PageModeStation;
        checkStationPresent(station);
        int stationIndex = getStationIndex(station);
        check(station == PTR_S_JOURNAL, JPOS_E_ILLEGAL, "No page mode on station: " + station);
        checkMember(control, PageModes, JPOS_E_ILLEGAL, "Invalid control: " + control);
        check(station == PTR_S_RECEIPT && !Data.CapRecPageMode, JPOS_E_ILLEGAL, "No page mode on receipt");
        check(station == PTR_S_SLIP && !Data.CapSlpPageMode, JPOS_E_ILLEGAL, "No page mode on slip");
        check(control == PTR_PM_PAGE_MODE && PagemodeCommand[stationIndex] != null, JPOS_E_ILLEGAL, "Station just in page mode");
        check(control != PTR_PM_PAGE_MODE && PagemodeCommand[stationIndex] == null, JPOS_E_ILLEGAL, "Station not in page mode");
        extendedSynchronousErrorCheck(station);
        PageModePrint request = POSPrinterInterface.pageModePrint(control);
        if (control == PTR_PM_CANCEL) {
            PagemodeCommand[stationIndex] = null;
        }
        else if (control == PTR_PM_PAGE_MODE) {
            PagemodeCommand[stationIndex] = request;
        }
        else {
            PagemodeCommand[stationIndex].addMethod(request);
            request = PagemodeCommand[stationIndex];
            if (control == PTR_PM_NORMAL)
                PagemodeCommand[stationIndex] = null;
            doItTrans(stationIndex, request, "PageModePrint");
            return;
        }
        logAsyncCall("PageModePrint");
    }

    @Override
    public void rotatePrint(int station, int rotation) throws JposException {
        logPreCall("RotatePrint", removeOuterArraySpecifier(new Object[]{station, rotation}, Device.MaxArrayStringElements));
        checkEnabled();
        checkStationPresent(station);
        int stationIndex = getStationIndex(station);
        check(station == PTR_S_JOURNAL, JPOS_E_ILLEGAL, "No rotation on station: " + station);
        int purerotation = rotation & ~(PTR_RP_BARCODE|PTR_RP_BITMAP);
        checkMember(purerotation, Rotations, JPOS_E_ILLEGAL, "Invalid rotation: " + rotation);
        check (station == PTR_S_RECEIPT && purerotation == PTR_RP_ROTATE180 && !Data.CapRecRotate180, JPOS_E_ILLEGAL, "Unsupported rotation: " + rotation);
        check (station == PTR_S_SLIP && purerotation == PTR_RP_ROTATE180 && !Data.CapSlpRotate180, JPOS_E_ILLEGAL, "Unsupported rotation: " + rotation);
        check (station == PTR_S_RECEIPT && purerotation == PTR_RP_RIGHT90 && !Data.CapRecRight90, JPOS_E_ILLEGAL, "Unsupported rotation: " + rotation);
        check (station == PTR_S_SLIP && purerotation == PTR_RP_RIGHT90 && !Data.CapSlpRight90, JPOS_E_ILLEGAL, "Unsupported rotation: " + rotation);
        check (station == PTR_S_RECEIPT && purerotation == PTR_RP_LEFT90 && !Data.CapRecLeft90, JPOS_E_ILLEGAL, "Unsupported rotation: " + rotation);
        check (station == PTR_S_SLIP && purerotation == PTR_RP_LEFT90 && !Data.CapSlpLeft90, JPOS_E_ILLEGAL, "Unsupported rotation: " + rotation);
        check(purerotation != PTR_RP_NORMAL && SidewaysCommand[stationIndex] != null, JPOS_E_ILLEGAL, "Other rotation just started for station: " + station);
        extendedSynchronousErrorCheck(station);
        RotatePrint request = POSPrinterInterface.rotatePrint(station, rotation);
        if (purerotation == PTR_RP_LEFT90 || purerotation == PTR_RP_RIGHT90) {
            SidewaysCommand[stationIndex] = request;
        } else{
            if (SidewaysCommand[stationIndex] != null) {
                SidewaysCommand[stationIndex].addMethod(request);
                request = SidewaysCommand[stationIndex];
                SidewaysCommand[stationIndex] = null;
            }
            doItTrans(stationIndex, request, "RotatePrint");
            return;
        }
        logAsyncCall("RotatePrint");
    }

    @Override
    public void transactionPrint(int station, int control) throws JposException {
        logPreCall("TransactionPrint", removeOuterArraySpecifier(new Object[]{station, control}, Device.MaxArrayStringElements));
        TransactionPrint request;
        checkEnabled();
        checkStationPresent(station);
        int stationIndex = getStationIndex(station);
        check(!Data.CapTransaction, JPOS_E_ILLEGAL, "Transaction print not supported");
        checkMember(control, new long[]{PTR_TP_NORMAL, PTR_TP_TRANSACTION}, JPOS_E_ILLEGAL, "Invalid control: " + control);
        if (control == PTR_TP_TRANSACTION) {
            check(TransactionCommand[stationIndex] != null, JPOS_E_ILLEGAL, "Transaction just in progress");
            extendedSynchronousErrorCheck(station);
            request = POSPrinterInterface.transactionPrint(station, control);
            TransactionCommand[stationIndex] = request;
        } else {
            check(TransactionCommand[stationIndex] == null, JPOS_E_ILLEGAL, "Transaction not started");
            extendedSynchronousErrorCheck(station);
            request = POSPrinterInterface.transactionPrint(station, control);
            TransactionCommand[stationIndex].addMethod(request);
            request = TransactionCommand[stationIndex];
            TransactionCommand[stationIndex] = null;
            doIt(request, "TransactionPrint");
            return;
        }
        logAsyncCall("TransactionPrint");
    }

    /**
     * Helper class used to control parsed output data.
     */
    public abstract static class PrintDataPart {
        /**
         * Used to perform full validation of the print data. To do this, relevant capabilities will be checked and
         * the corresponding method of the POSPrinterInterface used by the given POSPrinterService will be called.
         * @param srv     POSPrinterService to be used for additional validation.
         * @param station Printer station for which the validation shall be checked.
         * @throws JposException If not precisely supported with ErrorCode E_ILLEGAL, if not supported and no workaround
         *                       is possible, with ErrorCode E_FAILURE.
         */
        abstract public void validate(POSPrinterService srv, int station) throws JposException;

        /**
         * Used to perform additional validation of the print data, if output to the given station occurs. To do this,
         * simply the corresponding method of the POSPrinterInterface used by the given POSPrinterService will be called.
         * @param srv     POSPrinterService to be used for validation.
         * @param station Printer station used for validation.
         * @throws JposException If not precisely supported with ErrorCode E_ILLEGAL, if not supported and no workaround
         *                       is possible, with ErrorCode E_FAILURE.
         */
        abstract public void validateData(POSPrinterService srv, int station) throws JposException;
    }

    /**
     * Class describing control characters in print data.
     */
    public static class ControlChar extends PrintDataPart {
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
         * @param control Control character to be printed. Only CR and LF are valid control characters.
         */
        public ControlChar(char control) {
            ControlCharacter = control;
        }

        @Override
        @SuppressWarnings("null")
        public void validate(POSPrinterService srv, int station) throws JposException {
            int[] allowedFeatures = srv.getAllowed(station);
            check(allowedFeatures[CanStation] == 0, JPOS_E_FAILURE, "Print station not available");
            validateData(srv, station);
        }

        @Override
        public void validateData(POSPrinterService srv, int station) throws JposException {
            srv.POSPrinterInterface.validateData(station, this);
        }
    }

    /**
     * Class describing printable part of print data.
     */
    public static class PrintData extends PrintDataPart {
        /**
         * Returns data to be printed.
         * @return Print data.
         */
        public String getPrintData() {
            return PrintData;
        }
        private final String PrintData;

        /**
         * Returns whether PrintData needs mapping.
         * If true, PrintData contains unmapped data and the service must perform character conversion, if necessary.
         * If false, PrintData contains mapped data and the service does not need to perform conversion (PrintData will
         * be copied character-to-byte into the output buffer).
         * @return Mapping flag as described.
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
        public PrintData(String data, boolean mapping, int charset) {
            PrintData = data;
            ServiceIsMapping = mapping;
            CharacterSet = charset;
        }

        @Override
        @SuppressWarnings("null")
        public void validate(POSPrinterService srv, int station) throws JposException {
            int[] allowedFeatures = srv.getAllowed(station);
            check(allowedFeatures[CanStation] == 0, JPOS_E_FAILURE, "Print station not available");
            validateData(srv, station);
        }

        @Override
        public void validateData(POSPrinterService srv, int station) throws JposException {
            srv.POSPrinterInterface.validateData(station, this);
        }
    }

    /**
     * Class describing unknown escape sequence.
     */
    public static class EscUnknown extends PrintDataPart {
        /**
         * Returns data present after ESC|*... sequence. If no '*' follows '|', EscData is null.
         * @return Fixed length data belonging to escape sequence.
         */
        public String getEscData() {
            return EscData;
        }
        private final String EscData;

        /**
         * Returns capital characer that marks the end of the escape sequence.
         * @return Sequence end character.
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
         * @return Lower-case character sequence before sequence end character, formatted as described.
         */
        public int getSubtype() {
            return Subtype;
        }
        private final int Subtype;

        /**
         * Returns value in ESC sequence, in any. 0 if no value is present.
         * @return Sequence value.
         */
        public int getValue() {
            return Value;
        }
        private final int Value;

        /**
         * Returns whether '!' follows "ESC|".
         * @return Negation flag: true if ! follows ESC|
         */
        public boolean getNegated() {
            return Negated;
        }
        private final boolean Negated;

        /**
         * Specifies whether a positive integer value is part of the escape sequence.
         * @return true if escape sequence contains a value.
         */
        public boolean getValuePresent() {
            return ValuePresent;
        }
        private final boolean ValuePresent;

        /**
         * Constructor.
         * @param type      Initial value for Esc.
         * @param subtype   Initial value for Subtype.
         * @param value     Initial value for Value.
         * @param data      Initial value for EscData.
         * @param negated   Initial value for Negated.
         * @param present   Initial value for ValuePresent.
         */
        public EscUnknown(int type, int subtype, int value, String data, boolean negated, boolean present) {
            Esc = type;
            Subtype = subtype;
            Value = value;
            EscData = data;
            Negated = negated;
            ValuePresent = present;
        }

        @Override
        @SuppressWarnings("null")
        public void validate(POSPrinterService srv, int station) throws JposException {
            int[] allowedFeatures = srv.getAllowed(station);
            check(allowedFeatures[CanStation] == 0, JPOS_E_FAILURE, "Print station not available");
            validateData(srv, station);
        }

        @Override
        public void validateData(POSPrinterService srv, int station) throws JposException {
            srv.POSPrinterInterface.validateData(station, this);
        }
    }

    /**
     * Class describing cut escape sequences ESC|[#][x]P, where x is one of f or s.
     */
    public static class EscCut extends PrintDataPart {
        /**
         * Percentage of cut.
         * @return Percentage value.
         */
        public int getPercent() {
            return Percent;
        }
        private int Percent;

        /**
         * Specifies whether a feed of RecLinesToPaperCut lines shall be performed (if true).
         * @return true if feed shall be done.
         */
        public boolean getFeed() {
            return Feed;
        }
        private boolean Feed;

        /**
         * Specifies whether the stamp shall be pressed when cutting the paper.
         * @return true if stamp shall be pressed.
         */
        public boolean getStamp() {
            return Stamp;
        }
        private boolean Stamp;

        private EscCut() {}

        /**
         * Checks whether the specified cut sequence parameters form a stamp sequence. If so, it returns an EscCut object.
         * If not, the object given as first parameter will be returned.
         * @param obj               An object containing corresponding sequence parameters or null.
         * @param type              The sequence type (see EscUnknown, property Esc).
         * @param subtype           The subtype (see EscUnknown, property Subtype).
         * @param value             The value (see EscUnknown, property Value).
         * @param escdata           If value is a data length, the corresponding data. Otherwise null.
         * @param negated           The negation flag (see EscUnknown, property Negated).
         * @param valueispresent    The present flag (see EscUnknown, property ValuePresent).
         * @return      An EscCut object, if the sequence is a well-formed cut sequence, otherwise obj.
         */
        static public PrintDataPart getEscCut(PrintDataPart obj, int type, int subtype, int value, String escdata, boolean negated, boolean valueispresent) {
            if (type == 'P' && !negated && escdata == null) {
                EscCut esc = new EscCut();
                esc.Percent = valueispresent ? value : 100;
                esc.Stamp = subtype == 's';
                esc.Feed = subtype != 0;
                if (esc.Stamp || !esc.Feed || subtype == 'f')
                    return esc;
            }
            return obj;
        }

        @Override
        @SuppressWarnings("null")
        public void validate(POSPrinterService srv, int station) throws JposException {
            int[] allowedFeatures = srv.getAllowed(station);
            check(allowedFeatures[CanCut] == 0, JPOS_E_FAILURE, "Cut paper not supported");
            check(Stamp && allowedFeatures[CanStamp] == 0, JPOS_E_FAILURE, "Stamp paper not supported");
            validateData(srv, station);
        }

        @Override
        public void validateData(POSPrinterService srv, int station) throws JposException {
            srv.POSPrinterInterface.validateData(station, this);
        }
    }

    /**
     * Class describing stamp escape sequences ESC|sL.
     */
    static public class EscStamp extends PrintDataPart {
        private EscStamp() {}

        /**
         * Checks whether the specified esc sequence parameters form a stamp sequence. If so, it returns an EscStamp object.
         * If not, the object given as first parameter will be returned.
         * @param obj               An object containing corresponding sequence parameters or null.
         * @param type              The sequence type (see EscUnknown, property Esc).
         * @param subtype           The subtype (see EscUnknown, property Subtype).
         * @param ignored           The value (see EscUnknown, property Value), will be ignored.
         * @param escdata           If value is a data length, the corresponding data. Otherwise null.
         * @param negated           The negation flag (see EscUnknown, property Negated).
         * @param valueispresent    The present flag (see EscUnknown, property ValuePresent).
         * @return      An EscStamp object, if the sequence is a well-formed stamp sequence, otherwise obj.
         */
        static public PrintDataPart getEscStamp(PrintDataPart obj, int type, int subtype, int ignored, String escdata, boolean negated, boolean valueispresent) {
            if (type == 'L' && subtype == 's' && escdata == null && !negated && !valueispresent) {
                return new EscStamp();
            }
            return obj;
        }

        @Override
        @SuppressWarnings("null")
        public void validate(POSPrinterService srv, int station) throws JposException {
            int[] allowedFeatures = srv.getAllowed(station);
            check(allowedFeatures[CanStamp] == 0, JPOS_E_FAILURE, "Stamp paper not supported");
            validateData(srv, station);
        }

        @Override
        public void validateData(POSPrinterService srv, int station) throws JposException {
            srv.POSPrinterInterface.validateData(station, this);
        }
    }

    /**
     * Class describing logo escape sequences ESC|[x]L, where x is one of t or b.
     */
    static public class EscLogo extends PrintDataPart {
        /**
         * Data that describe the logo.
         * @return Array containing POSPrinterService.PrintDataPart objects describing the logo.
         */
        public PrintDataPart[] getLogoData() {
            return Arrays.copyOf(LogoData, LogoData.length);
        }
        private PrintDataPart[] LogoData;

        private EscLogo() {
        }

        /**
         * Checks whether the specified esc sequence parameters form a logo sequence. If so, it returns an EscLogo object.
         * If not, the object given as first parameter will be returned.
         * @param obj               An object containing corresponding sequence parameters or null.
         * @param type              The sequence type (see EscUnknown, property Esc).
         * @param subtype           The subtype (see EscUnknown, property Subtype).
         * @param ignored           The value (see EscUnknown, property Value). Will be ignored.
         * @param escdata           If value is a data length, the corresponding data. Otherwise null.
         * @param negated           The negation flag (see EscUnknown, property Negated).
         * @param valueispresent    The present flag (see EscUnknown, property ValuePresent).
         * @param printer           This object, used to retrieve logo data.
         * @return      An EscLogo object, if the sequence is a well-formed logo sequence, otherwise obj.
         */
        static public PrintDataPart getEscLogo(PrintDataPart obj, int type, int subtype, int ignored, String escdata, boolean negated, boolean valueispresent, POSPrinterService printer) {
            if (type == 'L' && escdata == null && !negated && !valueispresent) {
                EscLogo esc = new EscLogo();
                boolean top = subtype == 't';
                if (top || subtype == 'b') {
                    esc.LogoData = printer.POSPrinterInterface.getLogoData(top);
                    return esc;
                }
            }
            return obj;
        }

        @Override
        @SuppressWarnings("null")
        public void validate(POSPrinterService srv, int station) throws JposException {
            int[] allowedFeatures = srv.getAllowed(station);
            check(allowedFeatures[CanStation] == 0, JPOS_E_FAILURE, "Print station not available");
            validateData(srv, station);
        }

        @Override
        public void validateData(POSPrinterService srv, int station) throws JposException {
            srv.POSPrinterInterface.validateData(station, this);
        }
    }

    /**
     * Class describing bitmap escape sequences ESC|#B.
     */
    static public class EscBitmap extends PrintDataPart {
        /**
         * Bitmap number, corresponding to bitmap number used in SetBitmap method.
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
         * @param escdata           If value is a data length, the corresponding data. Otherwise null.
         * @param negated           The negation flag (see EscUnknown, property Negated).
         * @param valueispresent    The present flag (see EscUnknown, property ValuePresent).
         * @return      An EscBitmap object, if the sequence is a well-formed bitmap sequence, otherwise obj.
         */
        static public PrintDataPart getEscBitmap(PrintDataPart obj, int type, int subtype, int value, String escdata, boolean negated, boolean valueispresent) {
            if (type == 'B' && subtype == 0 && escdata == null && !negated && valueispresent) {
                EscBitmap esc = new EscBitmap();
                esc.Number = value;
                return esc;
            }
            return obj;
        }

        @Override
        @SuppressWarnings("null")
        public void validate(POSPrinterService srv, int station) throws JposException {
            int[] allowedFeatures = srv.getAllowed(station);
            check(allowedFeatures[CanBitmap] == 0, JPOS_E_FAILURE, "Print bitmap not supported");
            check(Number < 1 || Number > 20, JPOS_E_FAILURE, "Bitmap number invalid: " + Number);
            validateData(srv, station);

        }

        @Override
        public void validateData(POSPrinterService srv, int station) throws JposException {
            srv.POSPrinterInterface.validateData(station, this);
        }
    }

    /**
     * Class describing feed escape sequences ESC|[#]xF, where x is one of l, r or u.
     */
    static public class EscFeed extends PrintDataPart {
        /**
         * Returns whether feeding shall be in reverse direction.
         * @return If true, reverse feed, else normal feed.
         */
        public boolean getReverse() {
            return Reverse;
        }
        private boolean Reverse;

        /**
         * Returns how feeding shall be made.
         * @return if true, feed by minimum units, otherwise by lines.
         */
        public boolean getUnits() {
            return Units;
        }
        private boolean Units;

        /**
         * Lines or units to feed.
         * @return Feed count.
         */
        public int getCount() {
            return Count;
        }
        private int Count;

        /**
         * Mapping mode as stored in MapMode property
         * @return Mapping mode.
         */
        public int getMapMode() {
            return MapMode;
        }
        private int MapMode;

        private EscFeed() {
        }

        /**
         * Checks whether the specified esc sequence parameters form a feed sequence. If so, it returns an EscFeed object.
         * If not, the object given as first parameter will be returned.
         * @param obj               An object containing corresponding sequence parameters or null.
         * @param type              The sequence type (see EscUnknown, property Esc).
         * @param subtype           The subtype (see EscUnknown, property Subtype).
         * @param value             The value (see EscUnknown, property Value).
         * @param escdata           If value is a data length, the corresponding data. Otherwise null.
         * @param negated           The negation flag (see EscUnknown, property Negated).
         * @param valueispresent    The present flag (see EscUnknown, property ValuePresent).
         * @param mapmode           Contents of MapMode when the object has been created.
         * @return      An EscFeed object, if the sequence is a well-formed feed sequence, otherwise obj.
         */
        static public PrintDataPart getEscFeed(PrintDataPart obj, int type, int subtype, int value, String escdata, boolean negated, boolean valueispresent, int mapmode) {
            if (type == 'F' && !negated && escdata == null) {
                EscFeed esc = new EscFeed();
                esc.Count = valueispresent ? value : 1;
                esc.Reverse = subtype == 'r';
                esc.Units = subtype == 'u';
                esc.MapMode = mapmode;
                if (esc.Units || esc.Reverse || subtype == 'l')
                    return esc;
            }
            return obj;
        }

        @Override
        @SuppressWarnings("null")
        public void validate(POSPrinterService srv, int station) throws JposException {
            int[] allowedFeatures = srv.getAllowed(station);
            check(allowedFeatures[CanStation] == 0, JPOS_E_FAILURE, "Print station not available");
            validateData(srv, station);
        }

        @Override
        public void validateData(POSPrinterService srv, int station) throws JposException {
            srv.POSPrinterInterface.validateData(station, this);
        }
    }

    /**
     * Class describing embedded escape sequences ESC|[*]#E.
     */
    static public class EscEmbedded extends PrintDataPart {
        /**
         * Embedded data, data to be sent to the device unchanged.
         * @return Embedded data.
         */
        public String getData() {
            return Data;
        }
        private String Data;

        private EscEmbedded() {
        }

        /**
         * Checks whether the specified esc sequence parameters form an embedded sequence. If so, it returns an EscEmbedded object.
         * If not, the object given as first parameter will be returned.
         * @param obj               An object containing corresponding sequence parameters or null.
         * @param type              The sequence type (see EscUnknown, property Esc).
         * @param subtype           The subtype (see EscUnknown, property Subtype).
         * @param ignored           The value (see EscUnknown, property Value). Will be ignored.
         * @param escdata           If value is a data length, the corresponding data. Otherwise null.
         * @param negated           The negation flag (see EscUnknown, property Negated).
         * @param valueispresent    The present flag (see EscUnknown, property ValuePresent).
         * @return      An EscEmbedded object, if the sequence is a well-formed embedded sequence, otherwise obj.
         */
        static public PrintDataPart getEscEmbedded(PrintDataPart obj, int type, int subtype, int ignored, String escdata, boolean negated, boolean valueispresent) {
            if (type == 'E' && subtype == 0 && escdata != null && !negated && !valueispresent) {
                EscEmbedded esc = new EscEmbedded();
                esc.Data = escdata;
                return esc;
            }
            return obj;
        }

        @Override
        @SuppressWarnings("null")
        public void validate(POSPrinterService srv, int station) throws JposException {
            int[] allowedFeatures = srv.getAllowed(station);
            check(allowedFeatures[CanStation] == 0, JPOS_E_FAILURE, "Print station not available");
            validateData(srv, station);
        }

        @Override
        public void validateData(POSPrinterService srv, int station) throws JposException {
            srv.POSPrinterInterface.validateData(station, this);
        }
    }

    /**
     * Class describing font typeface selection escape sequences ESC|#fT.
     */
    static public class EscFontTypeface extends PrintDataPart {
        /**
         * Index of typeface to be selected.
         * @return Typeface index.
         */
        public int getTypefaceIndex() {
            return TypefaceIndex;
        }
        private int TypefaceIndex;

        private EscFontTypeface() {
        }

        /**
         * Checks whether the specified esc sequence parameters form a font typeface sequence. If so, it returns an EscFontTypeface object.
         * If not, the object given as first parameter will be returned.
         * @param obj               An object containing corresponding sequence parameters or null.
         * @param type              The sequence type (see EscUnknown, property Esc).
         * @param subtype           The subtype (see EscUnknown, property Subtype).
         * @param value             The value (see EscUnknown, property Value).
         * @param escdata           If value is a data length, the corresponding data. Otherwise null.
         * @param negated           The negation flag (see EscUnknown, property Negated).
         * @param valueispresent    The present flag (see EscUnknown, property ValuePresent).
         * @return      An EscFontTypeface object, if the sequence is a well-formed font typeface sequence, otherwise obj.
         */
        static public PrintDataPart getEscFontTypeface(PrintDataPart obj, int type, int subtype, int value, String escdata, boolean negated, boolean valueispresent) {
            if (type == 'T' && subtype == 'f' && escdata == null && !negated && valueispresent) {
                EscFontTypeface esc = new EscFontTypeface();
                esc.TypefaceIndex = value;
                return esc;
            }
            return obj;
        }

        @Override
        @SuppressWarnings("null")
        public void validate(POSPrinterService srv, int station) throws JposException {
            int[] allowedFeatures = srv.getAllowed(station);
            check(allowedFeatures[CanStation] == 0, JPOS_E_FAILURE, "Print station not available");
            check(allowedFeatures[FontCount] == 0, JPOS_E_FAILURE, "Select font not available");
            validateData(srv, station);
        }

        @Override
        public void validateData(POSPrinterService srv, int station) throws JposException {
            srv.POSPrinterInterface.validateData(station, this);
        }
    }

    /**
     * Class describing alignment escape sequences ESC|xA, where x is one of l, c or r.
     */
    static public class EscAlignment extends PrintDataPart {
        /**
         * One of the alignment values, BC_LEFT, BC_CENTER or BC_RIGHT.
         * @return Alignment.
         */
        public int getAlignment() {
            return Alignment;
        }
        private int Alignment;

        private EscAlignment() {
        }

        /**
         * Checks whether the specified esc sequence parameters form an alignment sequence. If so, it returns an EscAlignment object.
         * If not, the object given as first parameter will be returned.
         * @param obj               An object containing corresponding sequence parameters or null.
         * @param type              The sequence type (see EscUnknown, property Esc).
         * @param subtype           The subtype (see EscUnknown, property Subtype).
         * @param ignored           The value (see EscUnknown, property Value). Will be ignored.
         * @param escdata           If value is a data length, the corresponding data. Otherwise null.
         * @param negated           The negation flag (see EscUnknown, property Negated).
         * @param valueispresent    The present flag (see EscUnknown, property ValuePresent).
         * @return      An EscAlignment object, if the sequence is a well-formed alignment sequence, otherwise obj.
         */
        static public PrintDataPart getEscAlignment(PrintDataPart obj, int type, int subtype, int ignored, String escdata, boolean negated, boolean valueispresent) {
            if (type == 'A' && !negated && !valueispresent && escdata == null) {
                EscAlignment esc = new EscAlignment();
                if (subtype == 'l')
                    esc.Alignment = PTR_BC_LEFT;
                else if (subtype == 'r')
                    esc.Alignment = PTR_BC_RIGHT;
                else if (subtype == 'c')
                    esc.Alignment = PTR_BC_CENTER;
                else
                    return obj;
                return esc;
            }
            return obj;
        }

        @Override
        @SuppressWarnings("null")
        public void validate(POSPrinterService srv, int station) throws JposException {
            int[] allowedFeatures = srv.getAllowed(station);
            check(allowedFeatures[CanStation] == 0, JPOS_E_FAILURE, "Print station not available");
            validateData(srv, station);
        }

        @Override
        public void validateData(POSPrinterService srv, int station) throws JposException {
            srv.POSPrinterInterface.validateData(station, this);
        }
    }

    /**
     * Class describing normalize escape sequences ESC|N.
     */
    static public class EscNormalize extends PrintDataPart {
        private EscNormalize() {
        }

        /**
         * Checks whether the specified esc sequence parameters form a normalize sequence. If so, it returns an EscNormalize object.
         * If not, the object given as first parameter will be returned.
         * @param obj               An object containing corresponding sequence parameters or null.
         * @param type              The sequence type (see EscUnknown, property Esc).
         * @param subtype           The subtype (see EscUnknown, property Subtype).
         * @param ignored           The value (see EscUnknown, property Value). Will be ignored.
         * @param escdata           If value is a data length, the corresponding data. Otherwise null.
         * @param negated           The negation flag (see EscUnknown, property Negated).
         * @param valueispresent    The present flag (see EscUnknown, property ValuePresent).
         * @return      An EscNormalize object, if the sequence is a well-formed normalize sequence, otherwise obj.
         */
        static public PrintDataPart getEscNormalize(PrintDataPart obj, int type, int subtype, int ignored, String escdata, boolean negated, boolean valueispresent) {
            if (type == 'N' && subtype == 0 && escdata == null && !negated && !valueispresent)
                return new EscNormalize();
            return obj;
        }

        @Override
        @SuppressWarnings("null")
        public void validate(POSPrinterService srv, int station) throws JposException {
            int[] allowedFeatures = srv.getAllowed(station);
            check(allowedFeatures[CanStation] == 0, JPOS_E_FAILURE, "Print station not available");
            validateData(srv, station);
        }

        @Override
        public void validateData(POSPrinterService srv, int station) throws JposException {
            srv.POSPrinterInterface.validateData(station, this);
        }
    }

    /**
     * Class describing simple attribute setting escape sequences ESC|[!]xC, where x is one of b, i, rv, tb or tp.
     */
    static public class EscSimple extends PrintDataPart {
        /**
         * Returns whether the specified attribute shall be set or reset.
         * @return  true to set the attribute and false to reset the attribute.
         */
        public boolean getActivate() {
            return Activate;
        }
        private boolean Activate;

        /**
         * Returns whether attribute is bold.
         * @return True in case of bold attribute, otherwise false.
         */
        public boolean getBold() {
            return Bold;
        }
        private boolean Bold;

        /**
         * Returns whether attribute is italic.
         * @return True in case of italic attribute, otherwise false.
         */
        public boolean getItalic() {
            return Italic;
        }
        private boolean Italic;

        /**
         * Returns whether attribute is reverse.
         * @return True in case of reverse attribute, otherwise false.
         */
        public boolean getReverse() {
            return Reverse;
        }
        private boolean Reverse;

        /**
         * Returns whether attribute is subscript.
         * @return True in case of subscript attribute, otherwise false.
         */
        public boolean getSubscript() {
            return Subscript;
        }
        private boolean Subscript;

        /**
         * Returns whether attribute is superscript.
         * @return True in case of superscript attribute, otherwise false.
         */
        public boolean getSuperscript() {
            return Superscript;
        }
        private boolean Superscript;

        private EscSimple() {
        }

        /**
         * Checks whether the specified esc sequence parameters form a simple attribute sequence. If so, it returns an EscSimple object.
         * If not, the object given as first parameter will be returned. Simple attributes are bolt, italic, reverse,
         * subscript and superscript.
         * @param obj               An object containing corresponding sequence parameters or null.
         * @param type              The sequence type (see EscUnknown, property Esc).
         * @param subtype           The subtype (see EscUnknown, property Subtype).
         * @param ignored           The value (see EscUnknown, property Value). Will be ignored.
         * @param escdata           If value is a data length, the corresponding data. Otherwise null.
         * @param negated           The negation flag (see EscUnknown, property Negated).
         * @param valueispresent    The present flag (see EscUnknown, property ValuePresent).
         * @return      An EscSimple object, if the sequence is a well-formed simple attribute sequence, otherwise obj.
         */
        static public PrintDataPart getEscSimple(PrintDataPart obj, int type, int subtype, int ignored, String escdata, boolean negated, boolean valueispresent) {
            if (type == 'C' && !valueispresent && escdata == null) {
                EscSimple esc = new EscSimple();
                esc.Activate = !negated;
                esc.Bold = subtype == 'b';
                esc.Italic = subtype == 'i';
                esc.Reverse = subtype == ('r' * 1000) + 'v';
                esc.Subscript = subtype == ('t' * 1000) + 'b';
                esc.Superscript = subtype == ('t' * 1000) + 'p';
                if (esc.Bold || esc.Italic || esc.Reverse || esc.Subscript || esc.Superscript)
                    return esc;
            }
            return obj;
        }

        @Override
        @SuppressWarnings("null")
        public void validate(POSPrinterService srv, int station) throws JposException {
            int[] allowedFeatures = srv.getAllowed(station);
            check(allowedFeatures[CanStation] == 0, JPOS_E_FAILURE, "Print station not available");
            check(Italic && Activate && allowedFeatures[CanItalic] == 0, JPOS_E_FAILURE, "Italic printing not supported");
            check(Bold && Activate && allowedFeatures[CanBold] == 0, JPOS_E_FAILURE, "Bold printing not supported");
            validateData(srv, station);
        }

        @Override
        public void validateData(POSPrinterService srv, int station) throws JposException {
            srv.POSPrinterInterface.validateData(station, this);
        }
    }

    /**
     * Class describing line escape sequences ESC|[!][#]xC, where x is one of u or st.
     */
    static public class EscLine extends PrintDataPart {
        /**
         * Thickness of line in dots. -1 means a service specific default thickness, 0 switches line mode off.
         * @return Line thickness.
         */
        public int getThickness() {
            return Thickness;
        }
        private int Thickness;

        /**
         * Specifies whether the escape sequence controls underline mode (true) or strike-through mode (false).
         * @return true for underline, otherwise false.
         */
        public boolean getUnderline() {
            return Underline;
        }
        private boolean Underline;

        private EscLine() {
        }

        /**
         * Checks whether the specified esc sequence parameters form a line attribute sequence. If so, it returns an EscLine object.
         * If not, the object given as first parameter will be returned. Line attributes are underline and strike-through.
         * @param obj               An object containing corresponding sequence parameters or null.
         * @param type              The sequence type (see EscUnknown, property Esc).
         * @param subtype           The subtype (see EscUnknown, property Subtype).
         * @param value             The value (see EscUnknown, property Value).
         * @param escdata           If value is a data length, the corresponding data. Otherwise null.
         * @param negated           The negation flag (see EscUnknown, property Negated).
         * @param valueispresent    The present flag (see EscUnknown, property ValuePresent).
         * @return      An EscLine object, if the sequence is a well-formed line attribute sequence, otherwise obj.
         */
        static public PrintDataPart getEscLine(PrintDataPart obj, int type, int subtype, int value, String escdata, boolean negated, boolean valueispresent) {
            if (type == 'C' && escdata == null && (subtype == 'u' || subtype == 's' * 1000 + 't')) {
                EscLine esc = new EscLine();
                esc.Underline = subtype == 'u';
                esc.Thickness = negated ? 0 : valueispresent ? value : -1;
                return esc;
            }
            return obj;
        }

        @Override
        @SuppressWarnings("null")
        public void validate(POSPrinterService srv, int station) throws JposException {
            int[] allowedFeatures = srv.getAllowed(station);
            check(allowedFeatures[CanStation] == 0, JPOS_E_FAILURE, "Print station not available");
            check(Underline && allowedFeatures[CanUnderline] == 0 && getThickness() != 0, JPOS_E_FAILURE, "Underline not supported");
            validateData(srv, station);
        }

        @Override
        public void validateData(POSPrinterService srv, int station) throws JposException {
            srv.POSPrinterInterface.validateData(station, this);
        }
    }

    /**
     * Class describing color escape sequences ESC|[#]xC, where x is one of r or f.
     */
    static public class EscColor extends PrintDataPart {
        /**
         * Returns whether the color value is an RGB color value or a cartridge constant.
         * @return true in case of RGB color value, else false.
         */
        public boolean getRgb() {
            return Rgb;
        }
        private boolean Rgb;

        /**
         * Returns color value. In case of RGB, it must have the form rrrgggbbb, e.g. 255255255 for white. Otherwise, it must be one
         * of the predefined cartridge constants. Color is -1, if no color has been specified. In this case, the service
         * shall use primary color for RGB and secondary color otherwise.
         * @return Color value.
         */
        public int getColor() {
            return Color;
        }
        private int Color;

        private EscColor() {
        }

        /**
         * Checks whether the specified esc sequence parameters form a color sequence. If so, it returns an EscColor object.
         * If not, the object given as first parameter will be returned.
         * @param obj               An object containing corresponding sequence parameters or null.
         * @param type              The sequence type (see EscUnknown, property Esc).
         * @param subtype           The subtype (see EscUnknown, property Subtype).
         * @param value             The value (see EscUnknown, property Value).
         * @param escdata           If value is a data length, the corresponding data. Otherwise null.
         * @param negated           The negation flag (see EscUnknown, property Negated).
         * @param valueispresent    The present flag (see EscUnknown, property ValuePresent).
         * @return      An EscColor object, if the sequence is a well-formed color sequence, otherwise obj.
         */
        static public PrintDataPart getEscColor(PrintDataPart obj, int type, int subtype, int value, String escdata, boolean negated, boolean valueispresent) {
            if (type == 'C' && escdata == null && !negated) {
                EscColor esc = new EscColor();
                esc.Rgb = subtype == 'f';
                esc.Color = valueispresent ? value : -1;
                if (esc.Rgb || subtype == 'r')
                    return esc;
            }
            return obj;
        }

        @Override
        @SuppressWarnings("null")
        public void validate(POSPrinterService srv, int station) throws JposException {
            int[] allowedFeatures = srv.getAllowed(station);
            check(allowedFeatures[CanStation] == 0, JPOS_E_FAILURE, "Print station not available");
            check(Rgb && (allowedFeatures[Can2Color] & PTR_COLOR_FULL) == 0, JPOS_E_FAILURE, "RGB color not supported");
            check(Rgb && Color > 0 && (Color / 1000000 > 255 || (Color / 1000) % 1000 > 255 || Color % 1000 > 255), JPOS_E_FAILURE, "Bad RGB color: " + Color);
            check(!Rgb && Color > 0 && !member(Color, Cartridges), JPOS_E_FAILURE, "Invalid color value: " + Color);
            check(!Rgb && (allowedFeatures[Can2Color] & Color) == 0, JPOS_E_FAILURE, "Invalid color value: " + Color);
            validateData(srv, station);
        }

        @Override
        public void validateData(POSPrinterService srv, int station) throws JposException {
            srv.POSPrinterInterface.validateData(station, this);
        }
    }

    /**
     * Class describing scaling escape sequences ESC|[#][x]C, where x is one of h or v.
     */
    static public class EscScale extends PrintDataPart {
        /**
         * Scaling factor, multiple of normal size.
         * @return Scaling factor.
         */
        public int getScaleValue() {
            return ScaleValue;
        }
        private int ScaleValue;

        /**
         * Returns whether text shall be stretched in vertical direction.
         * @return  true in case of vertical stretching.
         */
        public boolean getScaleVertical() {
            return ScaleVertical;
        }
        private boolean ScaleVertical;

        /**
         * Returns whether text shall be stretched in horizontal direction.
         * @return  true in case of horizontal stretching.
         */
        public boolean getScaleHorizontal() {
            return ScaleHorizontal;
        }
        private boolean ScaleHorizontal;

        private EscScale() {
        }

        /**
         * Checks whether the specified esc sequence parameters form a scaling sequence. If so, it returns an EscScale object.
         * If not, the object given as first parameter will be returned.
         * @param obj               An object containing corresponding sequence parameters or null.
         * @param type              The sequence type (see EscUnknown, property Esc).
         * @param subtype           The subtype (see EscUnknown, property Subtype).
         * @param value             The value (see EscUnknown, property Value).
         * @param escdata           If value is a data length, the corresponding data. Otherwise null.
         * @param negated           The negation flag (see EscUnknown, property Negated).
         * @param valueispresent    The present flag (see EscUnknown, property ValuePresent).
         * @return      An EscScale object, if the sequence is a well-formed scaling sequence, otherwise obj.
         */
        static public PrintDataPart getEscScale(PrintDataPart obj, int type, int subtype, int value, String escdata, boolean negated, boolean valueispresent) {
            if (type == 'C' && valueispresent && !negated && escdata == null) {
                EscScale esc = new EscScale();
                switch (subtype) {
                    case 0 -> {
                        return getEscScaleForSubtypeZero(obj, value, esc);
                    }
                    case 'h' -> {
                        esc.ScaleHorizontal = true;
                        esc.ScaleVertical = false;
                        esc.ScaleValue = value;
                    }
                    case 'v' -> {
                        esc.ScaleHorizontal = false;
                        esc.ScaleVertical = true;
                        esc.ScaleValue = value;
                    }
                    default -> {
                        return obj;
                    }
                }
                return esc;
            }
            return obj;
        }

        private static PrintDataPart getEscScaleForSubtypeZero(PrintDataPart obj, int value, EscScale esc) {
            esc.ScaleValue = 2;
            switch (value) {
                case 1 -> {
                    esc.ScaleValue = 1;
                    esc.ScaleHorizontal = esc.ScaleVertical = false;
                }
                case 2 -> {
                    esc.ScaleHorizontal = true;
                    esc.ScaleVertical = false;
                }
                case 3 -> {
                    esc.ScaleHorizontal = false;
                    esc.ScaleVertical = true;
                }
                case 4 -> esc.ScaleHorizontal = esc.ScaleVertical = true;
                default -> {
                    return obj;
                }
            }
            return esc;
        }

        @Override
        @SuppressWarnings("null")
        public void validate(POSPrinterService srv, int station) throws JposException {
            int[] allowedFeatures = srv.getAllowed(station);
            check(allowedFeatures[CanStation] == 0, JPOS_E_FAILURE, "Print station not available");
            check(ScaleValue >= 2 && ScaleVertical && ScaleHorizontal && allowedFeatures[CanDWideHigh] == 0, JPOS_E_FAILURE, "Double size printing not supported");
            check(ScaleValue >= 2 && ScaleVertical && allowedFeatures[CanDHigh] == 0, JPOS_E_FAILURE, "Double high printing not supported");
            check(ScaleValue >= 2 && ScaleHorizontal && allowedFeatures[CanDWide] == 0, JPOS_E_FAILURE, "Double wide printing not supported");
            validateData(srv, station);
        }

        @Override
        public void validateData(POSPrinterService srv, int station) throws JposException {
            srv.POSPrinterInterface.validateData(station, this);
        }
    }

    /**
     * Class describing shading escape sequences ESC|[#]sC.
     */
    static public class EscShade extends PrintDataPart {
        /**
         * Shading percentage requested. A negative value forces device specific default shading.
         * @return Percentage value.
         */
        public int getPercentage() {
            return Percentage;
        }
        private int Percentage;

        private EscShade() {
        }

        /**
         * Checks whether the specified esc sequence parameters form a shade sequence. If so, it returns an EscShade object.
         * If not, the object given as first parameter will be returned.
         * @param obj               An object containing corresponding sequence parameters or null.
         * @param type              The sequence type (see EscUnknown, property Esc).
         * @param subtype           The subtype (see EscUnknown, property Subtype).
         * @param value             The value (see EscUnknown, property Value).
         * @param escdata           If value is a data length, the corresponding data. Otherwise null.
         * @param negated           The negation flag (see EscUnknown, property Negated).
         * @param valueispresent    The present flag (see EscUnknown, property ValuePresent).
         * @return      An EscShade object, if the sequence is a well-formed shade sequence, otherwise obj.
         */
        static public PrintDataPart getEscShade(PrintDataPart obj, int type, int subtype, int value, String escdata, boolean negated, boolean valueispresent) {
            if (type == 'C' && subtype == 's' && !negated && escdata == null) {
                EscShade esc = new EscShade();
                esc.Percentage = valueispresent ? value : -1;
                return esc;
            }
            return obj;
        }

        @Override
        @SuppressWarnings("null")
        public void validate(POSPrinterService srv, int station) throws JposException {
            int[] allowedFeatures = srv.getAllowed(station);
            check(allowedFeatures[CanStation] == 0, JPOS_E_FAILURE, "Print station not available");
            validateData(srv, station);
        }

        @Override
        public void validateData(POSPrinterService srv, int station) throws JposException {
            srv.POSPrinterInterface.validateData(station, this);
        }
    }

    /**
     * Class describing ruled line escape sequences ESC|*#dL.
     */
    static public class EscRuledLine extends PrintDataPart {
        /**
         * Returns line direction, see UPOS specification, method DrawRuledLine.
         * @return Line direction.
         */
        public int getLineDirection() {
            return LineDirection;
        }
        private int LineDirection;

        /**
         * Returns position list, see UPOS specification, method DrawRuledLine.
         * @return Position lkist.
         */
        public String getPositionList() {
            return PositionList;
        }
        private String PositionList;

        /**
         * Returns line width in dots.
         * @return Line width
         */
        public int getLineWidth() {
            return LineWidth;
        }
        private int LineWidth;

        /**
         * returns line style, see UPOS specification, method DrawRuledLine.
         * @return Line style.
         */
        public int getLineStyle() {
            return LineStyle;
        }
        private int LineStyle;

        /**
         * Returns line color, see UPOS specification, method DrawRuledLine.
         * @return Line color.
         */
        public int getLineColor() {
            return LineColor;
        }
        private int LineColor;

        private EscRuledLine() {}

        /**
         * Checks whether the specified esc sequence parameters form a ruled line sequence. If so, it returns an EscRuledLine object.
         * If not, the object given as first parameter will be returned.
         * @param obj               An object containing corresponding sequence parameters or null.
         * @param type              The sequence type (see EscUnknown, property Esc).
         * @param subtype           The subtype (see EscUnknown, property Subtype).
         * @param ignored           The value (see EscUnknown, property Value). Will be ignored.
         * @param escdata           If value is a data length, the corresponding data. Otherwise null.
         * @param negated           The negation flag (see EscUnknown, property Negated).
         * @param valueispresent    The present flag (see EscUnknown, property ValuePresent).
         * @return      An EscRuledLine object, if the sequence is a well-formed ruled line sequence, otherwise obj.
         */
        static public PrintDataPart getEscRuledLine(PrintDataPart obj, int type, int subtype, int ignored, String escdata, boolean negated, boolean valueispresent) {
            if (type == 'L' && subtype == 'd' && !negated && escdata != null && !valueispresent) {
                EscRuledLine esc = new EscRuledLine();
                try {
                    esc.PositionList = escdata.substring(escdata.indexOf('p') + 1, escdata.indexOf('d'));
                    esc.LineDirection = Integer.parseInt(escdata.substring(escdata.indexOf('d') + 1, escdata.indexOf('w')));
                    esc.LineWidth = Integer.parseInt(escdata.substring(escdata.indexOf('w') + 1, escdata.indexOf('s')));
                    esc.LineStyle = Integer.parseInt(escdata.substring(escdata.indexOf('s') + 1, escdata.indexOf('c')));
                    esc.LineColor = Integer.parseInt(escdata.substring(escdata.indexOf('c') + 1));
                    return esc;
                } catch (NumberFormatException ignored1) {}
            }
            return obj;
        }

        @Override
        @SuppressWarnings("null")
        public void validate(POSPrinterService srv, int station) throws JposException {
            int[] allowedFeatures = srv.getAllowed(station);
            check(allowedFeatures[CanStation] == 0, JPOS_E_FAILURE, "Print station not available");
            check(allowedFeatures[CanLine] == 0, JPOS_E_FAILURE, "Ruled lines not supported");
            check((LineDirection != PTR_RL_VERTICAL && LineDirection != PTR_RL_HORIZONTAL) || (LineDirection & allowedFeatures[CanLine]) == 0, JPOS_E_FAILURE, "Ruled line not supported for direction " + LineDirection);
            check(LineWidth <= 0, JPOS_E_FAILURE, "Ruled line width must be > 0: " + LineWidth);
            checkMember(LineStyle, RuledLineStyles, JPOS_E_FAILURE, "Invalid ruled line style: " + LineStyle);
            checkMember(LineColor, Cartridges, JPOS_E_FAILURE, "Invalid color value: " + LineColor);
            check((LineColor & allowedFeatures[CanColor]) == 0, JPOS_E_FAILURE, "Unsupported ruled line color value: " + LineColor);
            try {
                if (LineDirection == PTR_RL_VERTICAL) {
                    long[] values = stringArrayToLongArray(PositionList.split(","));
                    if (values.length == 0)
                        throw new JposException(JPOS_E_FAILURE, "Empty ruled line position list");
                    for (long i : values) {
                        check(i < 0 || i > allowedFeatures[LineWidth], JPOS_E_FAILURE, "Invalid ruled line position list: " + PositionList);
                    }
                } else {
                    String[] valuepairs = PositionList.split(";");
                    check(valuepairs.length == 0, JPOS_E_FAILURE, "Empty ruled line position list");
                    for (String s : valuepairs) {
                        long[] values = stringArrayToLongArray(s.split(","));
                        check(values.length != 2 || values[0] < 0 || values[0] + values[1] > allowedFeatures[LineWidth], JPOS_E_FAILURE, "Invalid ruled line position list: " + PositionList);
                    }
                }
            } catch (NumberFormatException e) {
                throw new JposException(JPOS_E_FAILURE, "Non-integer rule position");
            }
            validateData(srv, station);
        }

        @Override
        public void validateData(POSPrinterService srv, int station) throws JposException {
            srv.POSPrinterInterface.validateData(station, this);
        }
    }

    /**
     * Class describing barcode escape sequences ESC|[*]#R.
     */
    static public class EscBarcode extends PrintDataPart {
        /**
         * See parameter symbology, UPOS specification, method PrintBarCode.
         * @return Symbology.
         */
        public int getSymbology() {
            return Symbology;
        }
        private int Symbology;

        /**
         * See parameter height, UPOS specification, method PrintBarCode.
         * @return Height.
         */
        public int getHeight() {
            return Height;
        }
        private int Height;

        /**
         * See parameter width, UPOS specification, method PrintBarCode.
         * @return Width
         */
        public int getWidth() {
            return Width;
        }
        private int Width;

        /**
         * See parameter alignment, UPOS specification, method PrintBarCode.
         * @return Alignment.
         */
        public int getAlignment() {
            return Alignment;
        }
        private int Alignment;

        /**
         * See parameter textPosition, UPOS specification, method PrintBarCode.
         * @return Position of HRI-Text.
         */
        public int getTextPosition() {
            return TextPosition;
        }
        private int TextPosition;

        /**
         * Mapping mode as stored in MapMode property
         * @return Mapping mode.
         */
        public int getMapMode() {
            return MapMode;
        }
        private int MapMode;

        /**
         * See parameter data, UPOS specification, method PrintBarCode.
         * @return Bar code data.
         */
        public String getData() {
            return Data;
        }
        private String Data;

        private EscBarcode() {
        }

        /**
         * Checks whether the specified esc sequence parameters form a bar code sequence. If so, it returns an EscBarcode object.
         * If not, the object given as first parameter will be returned.
         * @param obj               An object containing corresponding sequence parameters or null.
         * @param type              The sequence type (see EscUnknown, property Esc).
         * @param subtype           The subtype (see EscUnknown, property Subtype).
         * @param ignored           The value (see EscUnknown, property Value). Will be ignored.
         * @param escdata           If value is a data length, the corresponding data. Otherwise null.
         * @param negated           The negation flag (see EscUnknown, property Negated).
         * @param valueispresent    The present flag (see EscUnknown, property ValuePresent).
         * @param mapmode           Contents of MapMode when the object has been created.
         * @return      An EscBarcode object, if the sequence is a well-formed bar code sequence, otherwise obj.
         */
        static public PrintDataPart getEscBarcode(PrintDataPart obj, int type, int subtype, int ignored, String escdata, boolean negated, boolean valueispresent, int mapmode) {
            if (type == 'R' && subtype == 0 && !negated && escdata != null && !valueispresent) {
                EscBarcode esc = new EscBarcode();
                try {
                    esc.Symbology = Integer.parseInt(escdata.substring(escdata.indexOf('s') + 1, escdata.indexOf('h')));
                    esc.Height = Integer.parseInt(escdata.substring(escdata.indexOf('h') + 1, escdata.indexOf('w')));
                    esc.Width = Integer.parseInt(escdata.substring(escdata.indexOf('w') + 1, escdata.indexOf('a')));
                    esc.Alignment = Integer.parseInt(escdata.substring(escdata.indexOf('a') + 1, escdata.indexOf('t')));
                    esc.TextPosition = Integer.parseInt(escdata.substring(escdata.indexOf('t') + 1, escdata.indexOf('d')));
                    esc.Data = escdata.substring(escdata.indexOf('d') + 1, escdata.indexOf('e'));
                    esc.MapMode = mapmode;
                    return esc;
                } catch (NumberFormatException ignored1) {}
            }
            return obj;
        }

        @Override
        @SuppressWarnings("null")
        public void validate(POSPrinterService srv, int station) throws JposException {
            int[] allowedFeatures = srv.getAllowed(station);
            check(allowedFeatures[CanStation] == 0, JPOS_E_FAILURE, "Print station not available");
            check(allowedFeatures[CanBarcode] == 0, JPOS_E_FAILURE, "Print barcode not supported");
            check(Symbology < PTR_BCS_UPCA, JPOS_E_FAILURE, "Invalid symbology: " + Symbology);
            check(Height <= 0, JPOS_E_FAILURE, "Invalid height: " + Height);
            check(Width <= 0 || Width > allowedFeatures[LineWidth], JPOS_E_FAILURE, "Invalid width: " + Width);
            check(Alignment < 0 && !member(Alignment, Alignments), JPOS_E_FAILURE, "Invalid alignment: " + Alignment);
            checkMember(TextPosition, HRITextPositions, JPOS_E_FAILURE, "Invalid HRI position: " + TextPosition);
            validateData(srv, station);
        }

        @Override
        public void validateData(POSPrinterService srv, int station) throws JposException {
            srv.POSPrinterInterface.validateData(station, this);
        }
    }

    /**
     * Helper function. Retrieves maximum value of a long array.
     * @param valarray The array.
     * @return Maximum value stored in valarray.
     */
    static public long max(long[] valarray) {
        long max = 0;
        for (long i : valarray) {
            if (i > max)
                max = i;
        }
        return max;
    }

    /*
     * Print data processing
     */

    /**
     * Parses output data for escape sequences and valid control characters CR and LF. See UPOS specification for POSPrinter,
     * chapter <i>Data Characters and Escape Sequences</i>. Returns list of objects that describe all parts of the output
     * string. These objects can be used by validate and print functions to check print data and to generate generic output
     * data.
     * Possible objects in list have one of the following types:
     * <ul>
     *     <li>PrintData       -   Class containing character strings with printable characters only. See PrintData for details.</li>
     *     <li>ControlChar     -   Class containing control character CR or LF. While LF should be always valid, CR can be invalid, depending
     *                             on printer capabilities.</li>
     *     <li>EscCut          -   Class containing information about details of a cut command. See EscCut for details.</li>
     *     <li>EscRuledLine    -   Class containing information about details of a ruled line command. See EscRuledLine for details.</li>
     *     <li>EscNormalize    -   Class containing information about details of a normalize command. See EscNormalize for details.</li>
     *     <li>EscLogo         -   Class containing information about details of a logo command. See EscLogo for details.</li>
     *     <li>EscStamp        -   Class containing information about details of a stamp command. See EscStamp for details.</li>
     *     <li>EscBitmap       -   Class containing information about details of a bitmap command. See EscBitmap for details.</li>
     *     <li>EscFeed         -   Class containing information about details of a feed command. See EscFeed for details.</li>
     *     <li>EscEmbedded     -   Class containing information about details of a embedded data command. See EscEmbedded for details.</li>
     *     <li>EscBarcode      -   Class containing information about details of a barcode command. See EscBarcode for details.</li>
     *     <li>EscFontTypeface -   Class containing information about details of a font typeface command. See EscFontTypeface for details.</li>
     *     <li>EscAlignment    -   Class containing information about details of an alignment command. See EscAlignment for details.</li>
     *     <li>EscScale        -   Class containing information about details of a scale command. See EscScale for details.</li>
     *     <li>EscSimple       -   Class containing information about details of a simple attribute command. See EscSimple for details.</li>
     *     <li>EscLine         -   Class containing information about details of an added line command. See EscLine for details.</li>
     *     <li>EscColor        -   Class containing information about details of a color setting command. See EscColor for details.</li>
     *     <li>EscShade        -   Class containing information about details of a shading command. See EscShade for details.</li>
     *     <li>EscUnknown      -   Class containing information about details of an unknown escape sequence.</li>
     * </ul>
     *
     * @param data Character string to be printed. May contain CR, LF and ESC sequences as described in the UPOS specification.
     * @return List of objects that describe all parts of data.
     */
    public List<PrintDataPart> outputDataParts(String data) {
        List<PrintDataPart> out = new ArrayList<>();
        int index;
        try {
            while ((index = data.indexOf("\33|")) >= 0) {
                outputPrintableParts(data.substring(0, index), out);
                data = data.substring(index + 2);
                int value = 0;
                int temp;
                boolean negated = data.charAt(0) == '!';
                boolean valueisdatalength = data.charAt(0) == '*';
                boolean valueispresent = false;
                for (index = (negated || valueisdatalength) ? 1 : 0; (temp = data.charAt(index) - '0') >= 0 && temp <= 9; index++) {
                    value = value * 10 + temp;
                    valueispresent = true;
                }
                data = data.substring(index);
                int subtype = 0;
                for (index = 0; (temp = data.charAt(index)) >= 'a' && temp <= 'z'; index++) {
                    subtype = subtype * 1000 + temp;
                }
                String escdata = getEscData(data, index, value, temp, valueisdatalength);
                data = data.substring(index + escdata.length() + 1);
                out.add(getEscObj(temp, subtype, value, valueisdatalength ? escdata : (String) null, negated, valueispresent));
            }
            if (data.length() > 0)
                outputPrintableParts(data, out);
            PrintDataPart o = out.get(out.size() - 1);
            if (o instanceof ControlChar && '\15' == ((ControlChar) o).getControlCharacter()) {
                out.add(new PrintData("", Data.MapCharacterSet, Data.CharacterSet));
            }
        } catch (IndexOutOfBoundsException e) {
            out.add(new EscUnknown(0, 0, 0, null, false, false));
        }
        return out;
    }

    private void outputPrintableParts(String data, List<PrintDataPart> out) {
        for (int i = 0; i < data.length(); i++) {
            int actchar = data.charAt(i);
            if (actchar == '\12' || actchar == '\15') {
                if (i > 0)
                    out.add(new PrintData(data.substring(0, i), Data.MapCharacterSet, Data.CharacterSet));
                data = data.substring(i + 1);
                if (actchar == '\15') {
                    while(data.length() > 0 && data.charAt(0) == '\15')
                        data = data.substring(1);
                    if (data.length() > 0 && data.charAt(0) != '\12')
                        out.add(new ControlChar((char)actchar));
                }
                else
                    out.add(new ControlChar((char) actchar));
                i = -1;
            }
        }
        if (data.length() > 0)
            out.add(new PrintData(data, Data.MapCharacterSet, Data.CharacterSet));
    }

    private String getEscData(String data, int index, int value, int temp, boolean valueisdatalength) {
        String escdata;
        if (valueisdatalength || "ER".indexOf(temp) >= 0) {
            escdata = data.substring(index + 1, value + index + 1);
        }
        else {
            escdata = "";
        }
        return escdata;
    }

    private PrintDataPart getEscObj(int temp, int subtype, int value, String escdata, boolean negated, boolean valueispresent) {
        PrintDataPart ret;
        boolean notnull = ((ret = EscCut.getEscCut(null, temp, subtype, value, escdata, negated, valueispresent)) != null ||
                (ret = EscRuledLine.getEscRuledLine(null, temp, subtype, value, escdata, negated, valueispresent)) != null ||
                (ret = EscNormalize.getEscNormalize(null, temp, subtype, value, escdata, negated, valueispresent)) != null ||
                (ret = EscLogo.getEscLogo(null, temp, subtype, value, escdata, negated, valueispresent, this)) != null ||
                (ret = EscStamp.getEscStamp(null, temp, subtype, value, escdata, negated, valueispresent)) != null ||
                (ret = EscBitmap.getEscBitmap(null, temp, subtype, value, escdata, negated, valueispresent)) != null ||
                (ret = EscFeed.getEscFeed(null, temp, subtype, value, escdata, negated, valueispresent, Data.MapMode)) != null ||
                (ret = EscEmbedded.getEscEmbedded(null, temp, subtype, value, escdata, negated, valueispresent)) != null ||
                (ret = EscBarcode.getEscBarcode(null, temp, subtype, value, escdata, negated, valueispresent, Data.MapMode)) != null ||
                (ret = EscFontTypeface.getEscFontTypeface(null, temp, subtype, value, escdata, negated, valueispresent)) != null ||
                (ret = EscAlignment.getEscAlignment(null, temp, subtype, value, escdata, negated, valueispresent)) != null ||
                (ret = EscScale.getEscScale(null, temp, subtype, value, escdata, negated, valueispresent)) != null ||
                (ret = EscSimple.getEscSimple(null, temp, subtype, value, escdata, negated, valueispresent)) != null ||
                (ret = EscLine.getEscLine(null, temp, subtype, value, escdata, negated, valueispresent)) != null ||
                (ret = EscColor.getEscColor(null, temp, subtype, value, escdata, negated, valueispresent)) != null ||
                (ret = EscShade.getEscShade(null, temp, subtype, value, escdata, negated, valueispresent)) != null);
        return notnull ? ret : new EscUnknown(temp, subtype, value, escdata, negated, valueispresent);
    }

    /*
     * Extended validation handling.
     */

    /**
     * The following values specify the indices for the integer array that specifies whether specific escape sequences
     * are valid. Used in validateData method.
     */
    private static final int Can2Color = 0;
    private static final int CanBold = 1;
    private static final int CanDHigh = 2;
    private static final int CanDWide = 3;
    private static final int CanDWideHigh = 4;
    private static final int CanItalic = 5;
    private static final int CanUnderline = 6;
    private static final int CanBarcode = 7;
    private static final int CanBitmap = 8;
    private static final int CanCut = 9;
    private static final int CanLine = 10;
    private static final int CanStamp = 11;
    private static final int FontCount = 12;
    private static final int CanColor = 13;
    private static final int LineWidth = 14;
    private static final int Station = 15;
    private static final int CanStation = 16;

    private int[] getAllowed(int station) {
        return switch (station) {
            case PTR_S_JOURNAL -> new int[]{
                    Data.CapJrn2Color ? 1 : 0,
                    Data.CapJrnBold ? 1 : 0,
                    Data.CapJrnDhigh ? 1 : 0,
                    Data.CapJrnDwide ? 1 : 0,
                    Data.CapJrnDwideDhigh ? 1 : 0,
                    Data.CapJrnItalic ? 1 : 0,
                    Data.CapJrnUnderline ? 1 : 0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    Data.FontTypefaceList.length() > 0 ? Data.FontTypefaceList.split(",").length : 0,
                    Data.CapJrnColor,
                    Data.JrnLineWidth,
                    PTR_S_JOURNAL,
                    Data.CapJrnPresent ? 1 : 0
            };
            case PTR_S_RECEIPT -> new int[]{
                    Data.CapRec2Color ? 1 : 0,
                    Data.CapRecBold ? 1 : 0,
                    Data.CapRecDhigh ? 1 : 0,
                    Data.CapRecDwide ? 1 : 0,
                    Data.CapRecDwideDhigh ? 1 : 0,
                    Data.CapRecItalic ? 1 : 0,
                    Data.CapRecUnderline ? 1 : 0,
                    Data.CapRecBarCode ? 1 : 0,
                    Data.CapRecBitmap ? 1 : 0,
                    Data.CapRecPapercut ? 1 : 0,
                    Data.CapRecRuledLine,
                    Data.CapRecStamp ? 1 : 0,
                    Data.FontTypefaceList.length() > 0 ? Data.FontTypefaceList.split(",").length : 0,
                    Data.CapRecColor,
                    Data.RecLineWidth,
                    PTR_S_RECEIPT,
                    Data.CapRecPresent ? 1 : 0
            };
            case PTR_S_SLIP -> new int[]{
                    Data.CapSlp2Color ? 1 : 0,
                    Data.CapSlpBold ? 1 : 0,
                    Data.CapSlpDhigh ? 1 : 0,
                    Data.CapSlpDwide ? 1 : 0,
                    Data.CapSlpDwideDhigh ? 1 : 0,
                    Data.CapSlpItalic ? 1 : 0,
                    Data.CapSlpUnderline ? 1 : 0,
                    Data.CapSlpBarCode ? 1 : 0,
                    Data.CapSlpBitmap ? 1 : 0,
                    0,
                    Data.CapSlpRuledLine,
                    0,
                    Data.FontTypefaceList.length() > 0 ? Data.FontTypefaceList.split(",").length : 0,
                    Data.CapSlpColor,
                    Data.SlpLineWidth,
                    PTR_S_SLIP,
                    Data.CapSlpPresent ? 1 : 0
            };
            default -> null;
        };
    }

    private void plausibilityCheckData(int station, List<PrintDataPart> data) throws JposException {
        String[] stationnames = {"Journal", "Receipt", "Slip"};
        int[] allowedFeatures = getAllowed(station);
        check (allowedFeatures == null, JPOS_E_FAILURE, "Invalid station: " + station);
        check(+allowedFeatures[CanStation] == 0, JPOS_E_FAILURE, stationnames[getStationIndex(station)] + " station not present");
        plausibilityCheckPrintData(allowedFeatures, data);
    }

    /**
     * Checks whether the given string holds data that cannot be printed precisely on slip as expected. See UPOS
     * method ValidateData for details.<br>
     * this method checks only the general ability for those features that can be checked via capabilitys or other
     * property values. More detailed checks must be performed by device specific service implementations.
     *
     * @param data Data to be checked. See UPOS method ValidateData for more details.
     * @throws JposException See UPOS specification of method ValidateData. Error code can be E_ILLEGAL or E_FAILURE.
     */
    public void plausibilityCheckSlipData(List<PrintDataPart> data) throws JposException {
        check(!Data.CapSlpPresent, JPOS_E_FAILURE, "Slip station not present");
        plausibilityCheckPrintData(getAllowed(PTR_S_SLIP), data);
    }

    /**
     * Checks whether the given string holds data that cannot be printed precisely on receipt as expected. See UPOS
     * method ValidateData for details.<br>
     * this method checks only the general ability for those features that can be checked via capabilitys or other
     * property values. More detailed checks must be performed by device specific service implementations.
     *
     * @param data Data to be checked. See UPOS method ValidateData for more details.
     * @throws JposException See UPOS specification of method ValidateData. Error code can be E_ILLEGAL or E_FAILURE.
     */
    public void plausibilityCheckReceiptData(List<PrintDataPart> data) throws JposException {
        check(!Data.CapRecPresent, JPOS_E_FAILURE, "Receipt station not present");
        plausibilityCheckPrintData(getAllowed(PTR_S_RECEIPT), data);
    }

    /**
     * Checks whether the given string holds data that cannot be printed precisely on journal as expected. See UPOS
     * method ValidateData for details.<br>
     * this method checks only the general ability for those features that can be checked via capabilities or other
     * property values. More detailed checks must be performed by device specific service implementations.
     *
     * @param data List of objects derived from PrintDataPart to be checked.
     * @throws JposException See UPOS specification of method ValidateData. Error code can be E_ILLEGAL or E_FAILURE.
     */
    public void plausibilityCheckJournalData(List<PrintDataPart> data) throws JposException {
        check(!Data.CapJrnPresent, JPOS_E_FAILURE, "Journal station not present");
        plausibilityCheckPrintData(getAllowed(PTR_S_JOURNAL), data);
    }

    private void plausibilityCheckPrintData(int[] allowedFeatures, List<PrintDataPart> out) throws JposException {
        for (PrintDataPart obj : out) {
            obj.validate(this, allowedFeatures[Station]);
        }
    }

    /*
     * Several checks for general use.
     */

    private void checkStationPresent(int station) throws JposException {
        checkMember(station, SingleStations, JPOS_E_ILLEGAL, "Invalid print station: " + station);
        check(station == PTR_S_JOURNAL && !Data.CapJrnPresent, JPOS_E_ILLEGAL, "No journal");
        check(station == PTR_S_RECEIPT && !Data.CapRecPresent, JPOS_E_ILLEGAL, "No receipt");
        check(station == PTR_S_SLIP && !Data.CapSlpPresent, JPOS_E_ILLEGAL, "No slip");
    }

    /**
     * Retrieves station constants and station indices from given stations constant specifying two stations. stations
     * specifies the stations of interest. On return, station will be filled with the corresponding singe-station
     * constants and stationIndex with a unique index in range 0 - 2.
     * @param stations      POSPrinter constant representing two stations, e.g. PTR_S_JOURNAL_RECEIPT.
     * @param stationIndex  Two-dimensional int array. Filled with internal station indices for both stations.
     * @param station       Two-dimensional int array. Filled with POSPrinter constant, e.g. PTR_S_JOURNAL, PTR_S_RECEIPT.
     * @throws JposException If concurrent printing is not supported for the specified stations by the printer, or if
     *                       one of the specified stations is in page mode, sideways print mode or transaction mode.
     */
    void checkTwoStations(int stations, int[] stationIndex, int[] station) throws JposException {
        switch (stations) {
            case PTR_S_JOURNAL_RECEIPT, PTR_TWO_RECEIPT_JOURNAL -> {
                check(!Data.CapConcurrentJrnRec, JPOS_E_ILLEGAL, "No concurrent printing on journal and receipt");
                stationIndex[0] = getStationIndex(station[0] = PTR_S_JOURNAL);
                stationIndex[1] = getStationIndex(station[1] = PTR_S_RECEIPT);
            }
            case PTR_S_JOURNAL_SLIP, PTR_TWO_SLIP_JOURNAL -> {
                check(!Data.CapConcurrentJrnSlp, JPOS_E_ILLEGAL, "No concurrent printing on journal and slip");
                stationIndex[0] = getStationIndex(station[0] = PTR_S_JOURNAL);
                stationIndex[1] = getStationIndex(station[1] = PTR_S_SLIP);
            }
            case PTR_S_RECEIPT_SLIP, PTR_TWO_SLIP_RECEIPT -> {
                check(!Data.CapConcurrentRecSlp, JPOS_E_ILLEGAL, "No concurrent printing on receipt and slip");
                stationIndex[0] = getStationIndex(station[0] = PTR_S_SLIP);
                stationIndex[1] = getStationIndex(station[1] = PTR_S_RECEIPT);
            }
            default -> throw new JposException(JPOS_E_ILLEGAL, "Invalid print stations: " + stations);
        }
        check(SidewaysCommand[station[0]] != null || SidewaysCommand[station[1]] != null, JPOS_E_ILLEGAL, "No support for printing to two stations when one station is in sideways print mode");
        check(PagemodeCommand[station[0]] != null || PagemodeCommand[station[1]] != null, JPOS_E_ILLEGAL, "No support for printing to two stations when one station is in page mode");
        check(TransactionCommand[station[0]] != null || TransactionCommand[station[1]] != null, JPOS_E_ILLEGAL, "No support for printing to two stations when one station is in transaction print mode");
    }

    private int getStationIndex(int station) {
        return station / 2;
    }

    /**
     * Checks whether the selected print station is operational.
     * @param station   Station to be checked
     * @throws JposException If station is not present or not operational.
     */
    public void extendedErrorCheck(int station) throws JposException {
        boolean[][] relevantconditions = {
                {Data.JrnEmpty, Data.JrnCartridgeState == PTR_CART_REMOVED, Data.JrnCartridgeState == PTR_CART_EMPTY, Data.JrnCartridgeState == PTR_CART_CLEANING },
                {Data.RecEmpty, Data.RecCartridgeState == PTR_CART_REMOVED, Data.RecCartridgeState == PTR_CART_EMPTY, Data.RecCartridgeState == PTR_CART_CLEANING },
                {Data.SlpEmpty, Data.SlpCartridgeState == PTR_CART_REMOVED, Data.SlpCartridgeState == PTR_CART_EMPTY, Data.SlpCartridgeState == PTR_CART_CLEANING }
        };
        int[][] exterrors = {
                {JPOS_EPTR_JRN_EMPTY, JPOS_EPTR_JRN_CARTRIDGE_REMOVED, JPOS_EPTR_JRN_CARTRIDGE_EMPTY, JPOS_EPTR_JRN_HEAD_CLEANING},
                {JPOS_EPTR_REC_EMPTY, JPOS_EPTR_REC_CARTRIDGE_REMOVED, JPOS_EPTR_REC_CARTRIDGE_EMPTY, JPOS_EPTR_REC_HEAD_CLEANING},
                {JPOS_EPTR_SLP_EMPTY, JPOS_EPTR_SLP_CARTRIDGE_REMOVED, JPOS_EPTR_SLP_CARTRIDGE_EMPTY, JPOS_EPTR_SLP_HEAD_CLEANING},
        };
        String[] errortexts = { "Paper empty", "Cartridge removed", "Cartridge empty", "Head cleaning" };
        check(Data.PowerNotify == JPOS_PN_ENABLED && Data.PowerState != JPOS_PS_ONLINE, JPOS_E_FAILURE, "POSPrinter not reachable");
        checkext(Data.CoverOpen, JPOS_EPTR_COVER_OPEN, "Cover open");
        for (int j = 0; j < relevantconditions.length; j++) {
            if (station == SingleStations[j]) {
                for (int k = 0; k < relevantconditions[j].length; k++) {
                    checkext(relevantconditions[j][k], exterrors[j][k], errortexts[k]);
                }
            }
        }
    }

    private void extendedSynchronousErrorCheck(int station) throws JposException {
        if (!Props.AsyncMode) {
            extendedErrorCheck(station);
            check(Props.State != JPOS_S_IDLE, JPOS_E_ILLEGAL, "POSPrinter busy");
        }
    }

    private void doIt(OutputRequest request, String what) throws JposException {
        OutputPrintRequest.setSynchronousPrinting(request);
        if (callNowOrLater(request))
            logAsyncCall(what);
        else
            logCall(what);
    }


    private void doItTrans(int stationIndex, OutputRequest request, String what) throws JposException {
        if (TransactionCommand[stationIndex] != null)
            TransactionCommand[stationIndex].addMethod(request);
        else if (!callNowOrLater(request)) {
            logCall(what);
            return;
        }
        logAsyncCall(what);
    }

    private void doItSidewaysTransPagemode(int stationIndex, OutputRequest request, String what) throws JposException {
        if (SidewaysCommand[stationIndex] != null)
            SidewaysCommand[stationIndex].addMethod(request);
        else if (TransactionCommand[stationIndex] != null)
            TransactionCommand[stationIndex].addMethod(request);
        else if (PagemodeCommand[stationIndex] != null)
            PagemodeCommand[stationIndex].addMethod(request);
        else {
            OutputPrintRequest.setSynchronousPrinting(request);
            if (!callNowOrLater(request)) {
                logCall(what);
                return;
            }
        }
        logAsyncCall(what);
    }
}
