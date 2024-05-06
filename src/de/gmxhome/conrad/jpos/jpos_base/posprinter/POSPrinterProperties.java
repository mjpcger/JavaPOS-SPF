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

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;
import static jpos.POSPrinterConst.*;

/**
 * Class containing the POSPrinter specific properties, their default values and default implementations of
 * POSPrinterInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter POS Printer.
 */
public class POSPrinterProperties extends JposCommonProperties implements POSPrinterInterface {
    /**
     * This property will be used internally to verify whether BeginInsertion and EndInsertion are valid operations.
     * It will be initialized to false during device enable.
     */
    public boolean InsertionMode = false;

    /**
     * This property will be used internally to verify whether BeginRemoval and EndRemoval are valid operations.
     * It will be initialized to false during device enable.
     */
    public boolean RemovalMode = false;

    /**
     * UPOS property CapCharacterSet. Default: PTR_CCS_ASCII. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapCharacterSet = PTR_CCS_ASCII;

    /**
     * UPOS property CapConcurrentJrnRec. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapConcurrentJrnRec = false;

    /**
     * UPOS property CapConcurrentJrnSlp. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapConcurrentJrnSlp = false;

    /**
     * UPOS property CapConcurrentPageMode. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapConcurrentPageMode = false;

    /**
     * UPOS property CapConcurrentRecSlp. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapConcurrentRecSlp = false;

    /**
     * UPOS property CapCoverSensor . Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapCoverSensor = false;

    /**
     * UPOS property CapJrn2Color. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapJrn2Color = false;

    /**
     * UPOS property CapJrnBold. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapJrnBold = false;

    /**
     * UPOS property CapJrnCartridgeSensor. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapJrnCartridgeSensor = 0;

    /**
     * UPOS property CapJrnColor. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapJrnColor = 0;

    /**
     * UPOS property CapJrnDhigh. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapJrnDhigh = false;

    /**
     * UPOS property CapJrnDwide. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapJrnDwide = false;

    /**
     * UPOS property CapJrnDwideDhigh. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapJrnDwideDhigh = false;

    /**
     * UPOS property CapJrnEmptySensor. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapJrnEmptySensor = false;

    /**
     * UPOS property CapJrnItalic. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapJrnItalic = false;

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
     * UPOS property CapJrnUnderline. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapJrnUnderline = false;

    /**
     * UPOS property CapMapCharacterSet. Default: true. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapMapCharacterSet = true;

    /**
     * UPOS property CapRec2Color. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRec2Color = false;

    /**
     * UPOS property CapRecBarCode. Default: true. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRecBarCode = true;

    /**
     * UPOS property CapRecBitmap. Default: true. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRecBitmap = true;

    /**
     * UPOS property CapRecBold. Default: true. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRecBold = true;

    /**
     * UPOS property CapRecCartridgeSensor. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapRecCartridgeSensor = 0;

    /**
     * UPOS property CapRecColor. Default: COLOR_PRIMARY. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapRecColor = PTR_COLOR_PRIMARY;

    /**
     * UPOS property CapRecDhigh. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRecDhigh = false;

    /**
     * UPOS property CapRecDwide. Default: true. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRecDwide = true;

    /**
     * UPOS property CapRecDwideDhigh. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRecDwideDhigh = false;

    /**
     * UPOS property CapRecEmptySensor. Default: true. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRecEmptySensor = true;

    /**
     * UPOS property CapRecItalic. Default: true. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRecItalic = true;

    /**
     * UPOS property CapRecLeft90. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRecLeft90 = false;

    /**
     * UPOS property CapRecMarkFeed. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapRecMarkFeed = 0;

    /**
     * UPOS property CapRecNearEndSensor. Default: true. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRecNearEndSensor = true;

    /**
     * UPOS property CapRecPageMode. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRecPageMode = false;

    /**
     * UPOS property CapRecPapercut. Default: true. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRecPapercut = true;

    /**
     * UPOS property CapRecPresent. Default: true. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRecPresent = true;

    /**
     * UPOS property CapRecRight90. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRecRight90 = false;

    /**
     * UPOS property CapRecRotate180. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRecRotate180 = false;

    /**
     * UPOS property CapRecRuledLine. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapRecRuledLine = 0;

    /**
     * UPOS property CapRecStamp. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRecStamp = false;

    /**
     * UPOS property CapRecUnderline. Default: true. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRecUnderline = true;

    /**
     * UPOS property CapSlp2Color. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSlp2Color = false;

    /**
     * UPOS property CapSlpBarCode. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSlpBarCode = false;

    /**
     * UPOS property CapSlpBitmap. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSlpBitmap = false;

    /**
     * UPOS property CapSlpBold. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSlpBold = false;

    /**
     * UPOS property CapSlpBothSidesPrint. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSlpBothSidesPrint = false;

    /**
     * UPOS property CapSlpCartridgeSensor. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapSlpCartridgeSensor = 0;

    /**
     * UPOS property CapSlpColor. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapSlpColor = 0;

    /**
     * UPOS property CapSlpDhigh. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSlpDhigh = false;

    /**
     * UPOS property CapSlpDwide. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSlpDwide = false;

    /**
     * UPOS property CapSlpDwideDhigh. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSlpDwideDhigh = false;

    /**
     * UPOS property CapSlpEmptySensor. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSlpEmptySensor = false;

    /**
     * UPOS property CapSlpFullslip. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSlpFullslip = false;

    /**
     * UPOS property CapSlpItalic. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSlpItalic = false;

    /**
     * UPOS property CapSlpLeft90. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSlpLeft90 = false;

    /**
     * UPOS property CapSlpNearEndSensor. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSlpNearEndSensor = false;

    /**
     * UPOS property CapSlpPageMode. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSlpPageMode = false;

    /**
     * UPOS property CapSlpPresent. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSlpPresent = false;

    /**
     * UPOS property CapSlpRight90. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSlpRight90 = false;

    /**
     * UPOS property CapSlpRotate180. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSlpRotate180 = false;

    /**
     * UPOS property CapSlpRuledLine. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapSlpRuledLine = 0;

    /**
     * UPOS property CapSlpUnderline. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSlpUnderline = false;

    /**
     * UPOS property CapTransaction. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapTransaction = false;

    /**
     * UPOS property CartridgeNotify.
     */
    public int CartridgeNotify;

    /**
     * Default value of CharacterSet property. Default: PTR_CS_ASCII. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CharacterSetDef = PTR_CS_ASCII;

    /**
     * UPOS property CharacterSet.
     */
    public int CharacterSet;

    /**
     * UPOS property CharacterSetList. Default: "998" (PTR_CS_ASCII). Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String CharacterSetList = Integer.toString(PTR_CS_ASCII);

    /**
     * UPOS property CoverOpen.
     */
    public boolean CoverOpen;

    /**
     * UPOS property ErrorLevel.
     */
    public int ErrorLevel;

    /**
     * UPOS property ErrorStation.
     */
    public int ErrorStation;

    /**
     * UPOS property ErrorString.
     */
    public String ErrorString;

    /**
     * UPOS property FontTypefaceList. Default: "". Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String FontTypefaceList = "";

    /**
     * UPOS property JrnCartridgeState.
     */
    public int JrnCartridgeState;

    /**
     * Default value of JrnCurrentCartridge property. Default: 0. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int JrnCurrentCartridgeDef = 0;

    /**
     * UPOS property JrnCurrentCartridge. For more details, see UPOS specification.
     */
    public int JrnCurrentCartridge;

    /**
     * UPOS property JrnEmpty.
     */
    public boolean JrnEmpty;

    /**
     * UPOS property JrnLetterQuality.
     */
    public boolean JrnLetterQuality;

    /**
     * UPOS property JrnLineChars.
     */
    public int JrnLineChars;

    /**
     * UPOS property JrnLineCharsList property. Default: "". Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method. If not empty,
     * the first specified value is the default for JrnLineChars, too.
     */
    public String JrnLineCharsList = "";

    /**
     * Default value of JrnLineHeight property. Default: 0. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int JrnLineHeightDef = 0;

    /**
     * UPOS property JrnLineHeight.
     */
    public int JrnLineHeight;

    /**
     * Default value of JrnLineSpacing property. Default: 0. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int JrnLineSpacingDef = 0;

    /**
     * UPOS property JrnLineSpacing.
     */
    public int JrnLineSpacing;

    /**
     * Default value of JrnLineWidth property. Default: 0. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int JrnLineWidthDef = 0;

    /**
     * UPOS property JrnLineWidth.
     */
    public int JrnLineWidth;

    /**
     * UPOS property JrnNearEnd.
     */
    public boolean JrnNearEnd;

    /**
     * UPOS property MapCharacterSet. Default will be true if CapMapCharacterSet
     * is true for jpos versions 1.7 and above, false otherwise.
     */
    public Boolean MapCharacterSet = null;

    /**
     * UPOS property MapMode.
     */
    public int MapMode;

    /**
     * UPOS property PageModeArea.
     */
    public String PageModeArea;

    /**
     * UPOS property PageModeArea.
     */
    public int PageModeDescriptor;

    /**
     * UPOS property PageModeHorizontalPosition.
     */
    public int PageModeHorizontalPosition;

    /**
     * UPOS property PageModeArea.
     */
    public String PageModePrintArea;

    /**
     * UPOS property PageModePrintDirection.
     */

    public int PageModePrintDirection;

    /**
     * UPOS property PageModeStation.
     */
    public int PageModeStation;

    /**
     * UPOS property PageModeStation.
     */
    public int PageModeVerticalPosition;

    /**
     * UPOS property RecBarCodeRotationList. Default: "0". Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String RecBarCodeRotationList = "0";

    /**
     * UPOS property RecBitmapRotationList. Default: "0". Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String RecBitmapRotationList = "0";

    /**
     * UPOS property RecCartridgeState.
     */
    public int RecCartridgeState;

    /**
     * Default value of RecCurrentCartridge property. Default: PTR_COLOR_PRIMARY. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int RecCurrentCartridgeDef = PTR_COLOR_PRIMARY;

    /**
     * UPOS property RecCurrentCartridge.
     */
    public int RecCurrentCartridge;

    /**
     * UPOS property RecEmpty.
     */
    public boolean RecEmpty;

    /**
     * UPOS property RecLetterQuality.
     */
    public boolean RecLetterQuality;

    /**
     * UPOS property RecLineChars.
     */
    public int RecLineChars;

    /**
     * UPOS property RecLineCharsList property. Default: "". Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method. If not empty,
     * the first value is the default for RecLineChars, too.
     */
    public String RecLineCharsList = "";

    /**
     * Default value of RecLineHeight property. Default: 0. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int RecLineHeightDef = 0;

    /**
     * UPOS property RecLineHeight.
     */
    public int RecLineHeight;

    /**
     * Default value of RecLineSpacing property. Default: 0. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int RecLineSpacingDef = 0;

    /**
     * UPOS property RecLineSpacing.
     */
    public int RecLineSpacing;

    /**
     * Default value of RecLinesToPaperCut property. Default: 0. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int RecLinesToPaperCutDef = 0;

    /**
     * UPOS property RecLinesToPaperCut.
     */
    public int RecLinesToPaperCut;

    /**
     * Default value of RecLineWidth property. Default: 0. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int RecLineWidthDef = 0;

    /**
     * UPOS property RecLineWidth.
     */
    public int RecLineWidth;

    /**
     * UPOS property RecNearEnd.
     */
    public boolean RecNearEnd;

    /**
     * Default value of RecSidewaysMaxChars property. Default: 0. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int RecSidewaysMaxCharsDef = 0;

    /**
     * UPOS property RecSidewaysMaxChars.
     */
    public int RecSidewaysMaxChars;

    /**
     * Default value of RecSidewaysMaxLines property. Default: 0. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int RecSidewaysMaxLinesDef = 0;

    /**
     * UPOS property RecSidewaysMaxLines.
     */
    public int RecSidewaysMaxLines;

    /**
     * UPOS property RotateSpecial.
     */
    public int RotateSpecial;

    /**
     * UPOS property SlpBarCodeRotationList. Default: "". Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String SlpBarCodeRotationList = "";

    /**
     * UPOS property SlpBitmapRotationList. Default: "". Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String SlpBitmapRotationList = "";

    /**
     * UPOS property SlpCartridgeState.
     */
    public int SlpCartridgeState;

    /**
     * Default value of SlpCurrentCartridge property. Default: 0. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int SlpCurrentCartridgeDef = 0;

    /**
     * UPOS property SlpCurrentCartridge.
     */
    public int SlpCurrentCartridge;

    /**
     * UPOS property SlpEmpty.
     */
    public boolean SlpEmpty;

    /**
     * UPOS property SlpLetterQuality.
     */
    public boolean SlpLetterQuality;

    /**
     * UPOS property SlpLineChars.
     */
    public int SlpLineChars;

    /**
     * UPOS property SlpLineCharsList property. Default: "". Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method. If no empty,
     * the first value is the default for RecLineChars, too.
     */
    public String SlpLineCharsList = "";

    /**
     * Default value of SlpLineHeight property. Default: 0. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int SlpLineHeightDef = 0;

    /**
     * UPOS property SlpLineHeight.
     */
    public int SlpLineHeight;

    /**
     * Default value of SlpLinesNearEndToEnd property. Default: 0. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int SlpLinesNearEndToEndDef = 0;

    /**
     * UPOS property SlpLinesNearEndToEnd.
     */
    public int SlpLinesNearEndToEnd;

    /**
     * Default value of SlpLineSpacing property. Default: 0. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int SlpLineSpacingDef = 0;

    /**
     * UPOS property SlpLineSpacing.
     */
    public int SlpLineSpacing;

    /**
     * Default value of SlpLineWidth property. Default: 0. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int SlpLineWidthDef = 0;

    /**
     * UPOS property SlpLineWidth.
     */
    public int SlpLineWidth;

    /**
     * Default value of SlpMaxLines property. Default: 0. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method if
     * CapSlpFullslip is false.
     */
    public int SlpMaxLinesDef = 0;

    /**
     * UPOS property SlpMaxLines.
     */
    public int SlpMaxLines;

    /**
     * UPOS property SlpNearEnd.
     */
    public boolean SlpNearEnd;

    /**
     * Default value of SlpPrintSide property. Default: PTR_PS_UNKNOWN. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int SlpPrintSideDef = PTR_PS_UNKNOWN;

    /**
     * UPOS property SlpPrintSide.
     */
    public int SlpPrintSide;

    /**
     * Default value of SlpSidewaysMaxChars property. Default: 0. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int SlpSidewaysMaxCharsDef = 0;

    /**
     * UPOS property SlpSidewaysMaxChars.
     */
    public int SlpSidewaysMaxChars;

    /**
     * Default value of SlpSidewaysMaxLines property. Default: 0. Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int SlpSidewaysMaxLinesDef = 0;

    /**
     * UPOS property SlpSidewaysMaxLines.
     */
    public int SlpSidewaysMaxLines;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    public POSPrinterProperties(int dev) {
        super(dev);
        FlagWhenIdleStatusValue = PTR_SUE_IDLE;
    }

    @Override
    public void initOnOpen() {
        super.initOnOpen();
        CartridgeNotify = PTR_CN_DISABLED;
        ErrorLevel = PTR_EL_NONE;
        JrnCartridgeState = PTR_CART_UNKNOWN;
        PageModeStation = 0;
        RecCartridgeState = PTR_CART_UNKNOWN;
        RotateSpecial = PTR_RP_NORMAL;
        SlpCartridgeState = PTR_CART_UNKNOWN;
        if (DeviceServiceVersion < 1007000)
            MapCharacterSet = false;
        else if (MapCharacterSet == null)
            MapCharacterSet = CapMapCharacterSet;
    }

    @Override
    public boolean initOnFirstEnable() {
        if (!super.initOnFirstEnable()) {
            CharacterSet = CharacterSetDef;
            JrnCurrentCartridge = JrnCurrentCartridgeDef;
            JrnLetterQuality = false;
            JrnLineChars = JrnLineCharsList.length() > 0 ? (int)stringArrayToLongArray(JrnLineCharsList.split(","))[0] : 0;
            JrnLineHeight = JrnLineHeightDef;
            JrnLineSpacing = JrnLineSpacingDef;
            JrnLineWidth = JrnLineWidthDef;
            MapMode = PTR_MM_DOTS;
            PageModePrintDirection = PTR_PD_LEFT_TO_RIGHT;
            RecCurrentCartridge = RecCurrentCartridgeDef;
            RecLetterQuality = false;
            RecLineChars = RecLineCharsList.length() > 0 ? (int)stringArrayToLongArray(RecLineCharsList.split(","))[0] : 0;
            RecLineHeight = RecLineHeightDef;
            RecLineSpacing = RecLineSpacingDef;
            RecLinesToPaperCut = RecLinesToPaperCutDef;
            RecLineWidth = RecLineWidthDef;
            RecSidewaysMaxChars = RecSidewaysMaxCharsDef;
            RecSidewaysMaxLines = RecSidewaysMaxLinesDef;
            SlpCurrentCartridge = SlpCurrentCartridgeDef;
            SlpLetterQuality = false;
            SlpLineChars = SlpLineCharsList.length() > 0 ? (int)stringArrayToLongArray(SlpLineCharsList.split(","))[0] : 0;
            SlpLineHeight = SlpLineHeightDef;
            SlpLinesNearEndToEnd = SlpLinesNearEndToEndDef;
            SlpLineSpacing = SlpLineSpacingDef;
            SlpLineWidth = SlpLineWidthDef;
            SlpMaxLines = CapSlpFullslip ? 0 : SlpMaxLinesDef;
            SlpSidewaysMaxChars = SlpSidewaysMaxCharsDef;
            SlpSidewaysMaxLines = SlpSidewaysMaxLinesDef;
            return false;
        }
        return true;
    }

    @Override
    public void initOnEnable(boolean enable) {
        super.initOnEnable(enable);
        if (enable) {
            ErrorString = "";
            JrnEmpty = false;
            JrnNearEnd = false;
            RecEmpty = false;
            RecNearEnd = false;
            SlpEmpty = false;
            SlpNearEnd = false;
            SlpPrintSide = SlpPrintSideDef;
            InsertionMode = false;
            RemovalMode = false;
        }
    }

    @Override
    public void clearErrorProperties() {
        super.clearErrorProperties();
        ErrorLevel = PTR_EL_NONE;
        ErrorStation = 0;
        ErrorString = "";
    }

    @Override
    public JposOutputRequest newJposOutputRequest() {
        return new OutputRequest(this);
    }

    @Override
    public void cartridgeNotify(int notify) throws JposException {
        CartridgeNotify = notify;
    }

    @Override
    public void characterSet(int i) throws JposException {
        CharacterSet = i;
    }

    @Override
    public void jrnCurrentCartridge(int i) throws JposException {
        JrnCurrentCartridge = i;
    }

    @Override
    public void jrnLetterQuality(boolean i) throws JposException {
        JrnLetterQuality = i;
    }

    @Override
    public void jrnLineChars(int i) throws JposException {
        JrnLineChars = i;
    }

    @Override
    public void jrnLineHeight(int i) throws JposException {
        JrnLineHeight = i;
    }

    @Override
    public void jrnLineSpacing(int i) throws JposException {
        JrnLineSpacing = i;
    }

    @Override
    public void mapCharacterSet(boolean i) throws JposException {
        MapCharacterSet = i;
    }

    @Override
    public void mapMode(int i) throws JposException {
        MapMode = i;
    }

    @Override
    public void pageModeHorizontalPosition(int i) throws JposException {
        PageModeHorizontalPosition = i;
    }

    @Override
    public void pageModePrintArea(String i) throws JposException {
        PageModePrintArea = i;
    }

    @Override
    public void pageModePrintDirection(int i) throws JposException {
        PageModePrintDirection = i;
    }

    @Override
    public void pageModeStation(int i) throws JposException {
        PageModeStation = i;
    }

    @Override
    public void pageModeVerticalPosition(int i) throws JposException {
        PageModeVerticalPosition = i;
    }

    @Override
    public void recCurrentCartridge(int i) throws JposException {
        RecCurrentCartridge = i;
    }

    @Override
    public void recLetterQuality(boolean i) throws JposException {
        RecLetterQuality = i;
    }

    @Override
    public void recLineChars(int i) throws JposException {
        RecLineChars = i;
    }

    @Override
    public void recLineHeight(int i) throws JposException {
        RecLineHeight = i;
    }

    @Override
    public void recLineSpacing(int i) throws JposException {
        RecLineSpacing = i;
    }

    @Override
    public void rotateSpecial(int special) throws JposException {
        RotateSpecial = special;
    }

    @Override
    public void slpCurrentCartridge(int i) throws JposException {
        SlpCurrentCartridge = i;
    }

    @Override
    public void slpLetterQuality(boolean i) throws JposException {
        SlpLetterQuality = i;
    }

    @Override
    public void slpLineChars(int i) throws JposException {
        SlpLineChars = i;
    }

    @Override
    public void slpLineHeight(int i) throws JposException {
        SlpLineHeight = i;
    }

    @Override
    public void slpLineSpacing(int i) throws JposException {
        SlpLineSpacing = i;
    }

    @Override
    public void beginInsertion(int timeout) throws JposException {
    }

    @Override
    public void beginRemoval(int timeout) throws JposException {
    }

    @Override
    public void changePrintSide(int side) throws JposException {
    }

    @Override
    public void clearPrintArea() throws JposException {
    }

    @Override
    public void endInsertion() throws JposException {
    }

    @Override
    public void endRemoval() throws JposException {
    }

    @Override
    public PrintImmediate printImmediate(int station, String data) throws JposException {
        return new PrintImmediate(this, station, data);
    }

    @Override
    public void setBitmap(int bitmapNumber, int station, String fileName, int width, int alignment) throws JposException {
    }

    @Override
    public void setLogo(int location, String data) throws JposException {
    }

    @Override
    public void validateData(int station, String data) throws JposException {
    }

    @Override
    @Deprecated
    public void validateData(int station, POSPrinterService.PrintDataPart data) throws JposException {
        data.validateData((POSPrinterService)EventSource, station);
    }

    @Override
    public void validateData(int station, POSPrinterService.PrintData data) throws JposException {
        for (int i = 0; i < data.getPrintData().length(); i++) {
            int actchar = data.getPrintData().charAt(i);
            check(actchar < ' ', JPOS_E_FAILURE, "Invalid character: " + actchar);
        }
    }

    @Override
    public void validateData(int station, POSPrinterService.ControlChar ctrl) throws JposException {
    }

    @Override
    public void validateData(int station, POSPrinterService.EscCut esc) throws JposException {
        check(esc.getPercent() != 0 && esc.getPercent() != 100, JPOS_E_ILLEGAL, "Percentage not supported: " + esc.getPercent());
    }

    @Override
    public void validateData(int station, POSPrinterService.EscRuledLine esc) throws JposException {
        throw new JposException(JPOS_E_FAILURE, "Ruled line not supported");
    }

    @Override
    public void validateData(int station, POSPrinterService.EscNormalize esc) throws JposException {
    }

    @Override
    public void validateData(int station, POSPrinterService.EscLogo esc) throws JposException {
    }

    @Override
    public POSPrinterService.PrintDataPart[] getLogoData(boolean top) {
        return new POSPrinterService.PrintDataPart[0];
    }

    @Override
    public void validateData(int station, POSPrinterService.EscStamp esc) throws JposException {
    }

    @Override
    public void validateData(int station, POSPrinterService.EscBitmap esc) throws JposException {
    }

    @Override
    public void validateData(int station, POSPrinterService.EscFeed esc) throws JposException {
    }

    @Override
    public void validateData(int station, POSPrinterService.EscEmbedded esc) throws JposException {
    }

    @Override
    public void validateData(int station, POSPrinterService.EscBarcode esc) throws JposException {
    }

    @Override
    public void validateData(int station, POSPrinterService.EscFontTypeface esc) throws JposException {
    }

    @Override
    public void validateData(int station, POSPrinterService.EscAlignment esc) throws JposException {
    }

    @Override
    public void validateData(int station, POSPrinterService.EscScale esc) throws JposException {
    }

    @Override
    public void validateData(int station, POSPrinterService.EscSimple esc) throws JposException {
        check(!esc.getActivate(), JPOS_E_ILLEGAL, "Resetting attribute not supported");
        check(esc.getReverse(), JPOS_E_ILLEGAL, "Reverse video printing not supported");
        check(esc.getSubscript(), JPOS_E_ILLEGAL, "Subscript printing not supported");
        check(esc.getSuperscript(), JPOS_E_ILLEGAL, "Superscript printing not supported");
    }

    @Override
    public void validateData(int station, POSPrinterService.EscLine esc) throws JposException {
        check(esc.getThickness() != 0 && !esc.getUnderline(), JPOS_E_FAILURE, "Strike-through not supported");
        check(esc.getThickness() > 1, JPOS_E_ILLEGAL, "Thickness not supported: " + esc.getThickness());
    }

    @Override
    public void validateData(int station, POSPrinterService.EscColor esc) throws JposException {
    }

    @Override
    public void validateData(int station, POSPrinterService.EscShade esc) throws JposException {
        check(esc.getPercentage() > 0, JPOS_E_ILLEGAL, "Shading value not supported for: " + esc.getPercentage());
    }

    @Override
    public void validateData(int station, POSPrinterService.EscUnknown esc) throws JposException {
        throw new JposException(JPOS_E_FAILURE, "Unknown escape sequence not supported");
    }

    @Override
    public CutPaper cutPaper(int percentage) throws JposException {
        return new CutPaper(this, percentage);
    }

    @Override
    public void cutPaper(CutPaper request) throws JposException {
    }

    @Override
    public DrawRuledLine drawRuledLine(int station, String positionList, int lineDirection, int lineWidth, int lineStyle, int lineColor) throws JposException {
        return new DrawRuledLine(this, station, positionList, lineDirection, lineWidth, lineStyle, lineColor);
    }

    @Override
    public void drawRuledLine(DrawRuledLine request) throws JposException {
    }

    @Override
    public MarkFeed markFeed(int type) throws JposException {
        return new MarkFeed(this, type);
    }

    @Override
    public void markFeed(MarkFeed request) throws JposException {
    }

    @Override
    public PrintBarCode printBarCode(int station, String data, int symbology, int height, int width, int alignment, int textPosition) throws JposException {
        return new PrintBarCode(this, station, data, symbology, height, width, alignment, textPosition);
    }

    @Override
    public void printBarCode(PrintBarCode request) throws JposException {
    }

    @Override
    public PrintBitmap printBitmap(int station, String fileName, int width, int alignment) throws JposException {
        return new PrintBitmap(this, station, fileName, width, alignment);
    }

    @Override
    public void printBitmap(PrintBitmap request) throws JposException {
    }

    @Override
    public PrintMemoryBitmap printMemoryBitmap(int station, byte[] data, int type, int width, int alignment) throws JposException {
        return new PrintMemoryBitmap(this, station, data, type, width, alignment);
    }

    @Override
    public void printMemoryBitmap(PrintMemoryBitmap request) throws JposException {
    }

    @Override
    public PrintNormal printNormal(int station, String data) throws JposException {
        return new PrintNormal(this, station, data);
    }

    @Override
    public void printNormal(PrintNormal request) throws JposException {
    }

    @Override
    public PrintTwoNormal printTwoNormal(int stations, String data1, String data2) throws JposException {
        return new PrintTwoNormal(this, stations, data1, data2);
    }

    @Override
    public void printTwoNormal(PrintTwoNormal request) throws JposException {
    }

    @Override
    public TransactionPrint transactionPrint(int station, int control) throws JposException {
        return new TransactionPrint(this, station, control);
    }

    @Override
    public void transactionPrint(TransactionPrint request) throws JposException {
    }

    @Override
    public RotatePrint rotatePrint(int station, int rotation) throws JposException {
        return new RotatePrint(this, station, rotation);
    }

    @Override
    public void rotatePrint(RotatePrint request) throws JposException {
    }

    @Override
    public PageModePrint pageModePrint(int control) throws JposException {
        return new PageModePrint(this, control);
    }

    @Override
    public void pageModePrint(PageModePrint request) throws JposException {
    }
}
