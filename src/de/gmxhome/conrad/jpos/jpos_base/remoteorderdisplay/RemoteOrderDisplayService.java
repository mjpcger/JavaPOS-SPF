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
import jpos.services.RemoteOrderDisplayService114;

/**
 * RemoteOrderDisplay service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class RemoteOrderDisplayService extends JposBase implements RemoteOrderDisplayService114 {
    private RemoteOrderDisplayProperties Data;

    private static final long[] validUnitIDs = new long[] {
            RemoteOrderDisplayConst.ROD_UID_1, RemoteOrderDisplayConst.ROD_UID_2,
            RemoteOrderDisplayConst.ROD_UID_3, RemoteOrderDisplayConst.ROD_UID_4,
            RemoteOrderDisplayConst.ROD_UID_5, RemoteOrderDisplayConst.ROD_UID_6,
            RemoteOrderDisplayConst.ROD_UID_7, RemoteOrderDisplayConst.ROD_UID_8,
            RemoteOrderDisplayConst.ROD_UID_9, RemoteOrderDisplayConst.ROD_UID_10,
            RemoteOrderDisplayConst.ROD_UID_11, RemoteOrderDisplayConst.ROD_UID_12,
            RemoteOrderDisplayConst.ROD_UID_13, RemoteOrderDisplayConst.ROD_UID_14,
            RemoteOrderDisplayConst.ROD_UID_15, RemoteOrderDisplayConst.ROD_UID_16,
            RemoteOrderDisplayConst.ROD_UID_17, RemoteOrderDisplayConst.ROD_UID_18,
            RemoteOrderDisplayConst.ROD_UID_19, RemoteOrderDisplayConst.ROD_UID_20,
            RemoteOrderDisplayConst.ROD_UID_21, RemoteOrderDisplayConst.ROD_UID_22,
            RemoteOrderDisplayConst.ROD_UID_23, RemoteOrderDisplayConst.ROD_UID_24,
            RemoteOrderDisplayConst.ROD_UID_25, RemoteOrderDisplayConst.ROD_UID_26,
            RemoteOrderDisplayConst.ROD_UID_27, RemoteOrderDisplayConst.ROD_UID_28,
            RemoteOrderDisplayConst.ROD_UID_29, RemoteOrderDisplayConst.ROD_UID_30,
            RemoteOrderDisplayConst.ROD_UID_31, RemoteOrderDisplayConst.ROD_UID_32
    };
    private static final long[] validEventTypes = new long[] {
            0,
            RemoteOrderDisplayConst.ROD_DE_TOUCH_UP,
            RemoteOrderDisplayConst.ROD_DE_TOUCH_DOWN,
            RemoteOrderDisplayConst.ROD_DE_TOUCH_UP|RemoteOrderDisplayConst.ROD_DE_TOUCH_DOWN,
            RemoteOrderDisplayConst.ROD_DE_TOUCH_MOVE,
            RemoteOrderDisplayConst.ROD_DE_TOUCH_MOVE|RemoteOrderDisplayConst.ROD_DE_TOUCH_UP,
            RemoteOrderDisplayConst.ROD_DE_TOUCH_MOVE|RemoteOrderDisplayConst.ROD_DE_TOUCH_DOWN,
            RemoteOrderDisplayConst.ROD_DE_TOUCH_MOVE|RemoteOrderDisplayConst.ROD_DE_TOUCH_UP|RemoteOrderDisplayConst.ROD_DE_TOUCH_DOWN
    };

    /**
     * The transaction command, if a transaction is in progress.
     */
    TransactionDisplay TransactionCommand = null;

    /**
     * Instance of a class implementing the RemoteOrderDisplayInterface for remote order display specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public RemoteOrderDisplayInterface RemoteOrderDisplayInterface;

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public RemoteOrderDisplayService(RemoteOrderDisplayProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public int getAutoToneDuration() throws JposException {
        checkFirstEnabled();
        logGet("AutoToneDuration");
        return Data.AutoToneDuration;
    }

    @Override
    public void setAutoToneDuration(int i) throws JposException {
        logPreSet("AutoToneDuration");
        checkOnline(Data.CurrentUnitID);
        check(i < 0 || (i > 0 && !Data.CapTone), Data.CurrentUnitID, JposConst.JPOS_E_ILLEGAL, 0, "AutoToneDuration " + i + " invalid for unit " + Data.unitsToFirstIndex(Data.CurrentUnitID));
        RemoteOrderDisplayInterface.autoToneDuration(i);
        logSet("AutoToneDuration");
    }

    @Override
    public int getAutoToneFrequency() throws JposException {
        checkFirstEnabled();
        logGet("AutoToneFrequency");
        return Data.AutoToneFrequency;
    }

    @Override
    public void setAutoToneFrequency(int i) throws JposException {
        logPreSet("AutoToneFrequency");
        checkOnline(Data.CurrentUnitID);
        check(i < 0, Data.CurrentUnitID, JposConst.JPOS_E_ILLEGAL, 0, "AutoToneFrequency " + i + " invalid for unit " + Data.unitsToFirstIndex(Data.CurrentUnitID));
        RemoteOrderDisplayInterface.autoToneFrequency(i);
        logSet("AutoToneFrequency");
    }

    @Override
    public boolean getCapMapCharacterSet() throws JposException {
        checkOpened();
        logGet("CapMapCharacterSet");
        return Data.CapMapCharacterSet;
    }

    @Override
    public boolean getCapSelectCharacterSet() throws JposException {
        checkOpened();
        logGet("CapSelectCharacterSet");
        return Data.CapSelectCharacterSet;
    }

    @Override
    public boolean getCapTone() throws JposException {
        checkFirstEnabled();
        logGet("CapTone");
        return Data.CapTone;
    }

    @Override
    public boolean getCapTouch() throws JposException {
        checkFirstEnabled();
        logGet("CapTouch");
        return Data.CapTouch;
    }

    @Override
    public boolean getCapTransaction() throws JposException {
        checkOpened();
        logGet("CapTransaction");
        return Data.CapTransaction;
    }

    @Override
    public int getCharacterSet() throws JposException {
        checkFirstEnabled();
        logGet("CharacterSet");
        return Data.CharacterSet;
    }

    @Override
    public String getCharacterSetList() throws JposException {
        checkFirstEnabled();
        logGet("CharacterSetList");
        return Data.CharacterSetList;
    }

    @Override
    public int getClocks() throws JposException {
        checkFirstEnabled();
        logGet("Clocks");
        return Data.Clocks;
    }

    @Override
    public int getCurrentUnitID() throws JposException {
        checkFirstEnabled();
        logGet("CurrentUnitID");
        return Data.CurrentUnitID;
    }

    @Override
    public void setCurrentUnitID(int i) throws JposException {
        logPreSet("CurrentUnitID");
        checkEnabled();
        check(!Device.member(i, validUnitIDs), 0, JposConst.JPOS_E_ILLEGAL, 0, "" + i + " is not a valid unit ID");
        RemoteOrderDisplayInterface.currentUnitID(i);
        logSet("CurrentUnitID");
    }

    @Override
    public int getDataCount() throws JposException {
        checkOpened();
        logGet("DataCount");
        return Data.DataCount;
    }

    @Override
    public String getErrorString() throws JposException {
        checkOpened();
        logGet("ErrorString");
        return Data.ErrorString;
    }

    @Override
    public int getErrorUnits() throws JposException {
        checkOpened();
        logGet("ErrorUnits");
        return Data.ErrorUnits;
    }

    @Override
    public String getEventString() throws JposException {
        checkOpened();
        logGet("EventString");
        return Data.EventString;
    }

    @Override
    public int getEventType() throws JposException {
        checkOpened();
        logGet("EventType");
        return Data.EventType;
    }

    @Override
    public void setEventType(int i) throws JposException {
        logPreSet("EventType");
        checkOpened();
        check(!Device.member(i, validEventTypes), Data.CurrentUnitID, JposConst.JPOS_E_ILLEGAL, 0, "EventType " + i + " invalid");
        RemoteOrderDisplayInterface.eventType(i);
        logSet("EventType");
    }

    @Override
    public int getEventUnitID() throws JposException {
        checkOpened();
        logGet("EventUnitID");
        return Data.EventUnitID;
    }

    @Override
    public int getEventUnits() throws JposException {
        checkOpened();
        logGet("EventUnits");
        return Data.EventUnits;
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
        check(b && !Data.CapMapCharacterSet, -1, JposConst.JPOS_E_ILLEGAL, 0, "MapCharacterSet " + b + " invalid");
        RemoteOrderDisplayInterface.mapCharacterSet(b);
        logSet("MapCharacterSet");
    }

    @Override
    public int getSystemClocks() throws JposException {
        checkFirstEnabled();
        logGet("SystemClocks");
        return Data.SystemClocks;
    }

    @Override
    public int getSystemVideoSaveBuffers() throws JposException {
        checkFirstEnabled();
        logGet("SystemVideoSaveBuffers");
        return Data.SystemVideoSaveBuffers;
    }

    @Override
    public int getTimeout() throws JposException {
        checkOpened();
        logGet("Timeout");
        return Data.Timeout;
    }

    @Override
    public void setTimeout(int i) throws JposException {
        logPreSet("Timeout");
        checkOpened();
        check(i < 0, -1, JposConst.JPOS_E_ILLEGAL, 0, "Timeout " + i + " invalid");
        RemoteOrderDisplayInterface.timeout(i);
        logSet("Timeout");
    }

    @Override
    public int getUnitsOnline() throws JposException {
        checkFirstEnabled();
        logGet("UnitsOnline");
        return Data.UnitsOnline;
    }

    @Override
    public int getVideoDataCount() throws JposException {
        checkOpened();
        RemoteOrderDisplayInterface.videoDataCount();
        logGet("VideoDataCount");
        return Data.VideoDataCount;
    }

    @Override
    public int getVideoMode() throws JposException {
        checkFirstEnabled();
        logGet("VideoMode");
        return Data.VideoMode;
    }

    @Override
    public void setVideoMode(int i) throws JposException {
        logPreSet("VideoMode");
        checkOnline(Data.CurrentUnitID);
        String[] modi = Data.VideoModesList.split(",");
        long[] validModes = new long[modi.length];
        for (int j = 0; j < modi.length; j++) {
            validModes[j] = Long.parseLong(modi[j].split(":")[0]);
        }
        check(Device.member(i , validModes), Data.CurrentUnitID, JposConst.JPOS_E_ILLEGAL, 0, "VideoMode " + i + " invalid for unit " + Data.unitsToFirstIndex(Data.CurrentUnitID));
        RemoteOrderDisplayInterface.videoMode(i);
        logSet("VideoMode");
    }

    @Override
    public String getVideoModesList() throws JposException {
        checkFirstEnabled();
        logGet("VideoModesList");
        return Data.VideoModesList;
    }

    @Override
    public int getVideoSaveBuffers() throws JposException {
        checkFirstEnabled();
        logGet("VideoSaveBuffers");
        return Data.VideoSaveBuffers;
    }

    @Override
    public void clearInput() throws JposException {
        super.clearInput();
        Data.clearErrorProperties();
        Data.clearDataProperties();
    }

    @Override
    public void clearOutput() throws JposException {
        super.clearOutput();
        Data.clearOutputErrorProperties();
        if (TransactionCommand != null && (TransactionCommand.getUnits() & Data.CurrentUnitID) != 0)
            TransactionCommand = null;
    }

    private long[] validFunctions = new long[] {
            RemoteOrderDisplayConst.ROD_CLK_PAUSE, RemoteOrderDisplayConst.ROD_CLK_START,
            RemoteOrderDisplayConst.ROD_CLK_RESUME, RemoteOrderDisplayConst.ROD_CLK_STOP,
            RemoteOrderDisplayConst.ROD_CLK_MOVE
    };

    private long[] initFunktions = new long[] {
            RemoteOrderDisplayConst.ROD_CLK_START, RemoteOrderDisplayConst.ROD_CLK_MOVE
    };

    private long[] validClockModes = new long[] {
            RemoteOrderDisplayConst.ROD_CLK_SHORT, RemoteOrderDisplayConst.ROD_CLK_NORMAL,
            RemoteOrderDisplayConst.ROD_CLK_12_LONG, RemoteOrderDisplayConst.ROD_CLK_24_LONG
    };

    private int validateCoordinates(int bits, int height, int width) {
        int result = 0;
        if (height < 0 || width < 0)
            return bits;
        while (bits != 0) {
            int index = Data.unitsToFirstIndex(bits);
            String[] modi = Data.Unit[index].VideoModesList.split(",");
            bits &= ~(1 << index);
            for (String mode : modi) {
                String[] values = mode.split(":");
                if (Integer.parseInt(values[0]) == Data.Unit[index].VideoMode) {
                    String[] limits = values[1].split("x");
                    if (height > Integer.parseInt(limits[0]) || width > Integer.parseInt(limits[1]))
                        result |= 1 << index;
                    break;
                }
            }
        }
        return result;
    }

    private int validateClockID(int bits, int id) {
        int result = 0;
        if (id < 1)
            return bits;
        while (bits != 0) {
            int index = Data.unitsToFirstIndex(bits);
            bits &= ~(1 << index);
            if (Data.Unit[index].Clocks < id)
                result |= 1 << index;
        }
        return result;
    }

    /**
     * Check condition and if true, sets error properties and throws JposException.
     * @param condition Error condition
     * @param units     Units to be filled in ErrorUnits
     * @param error     Error code
     * @param ext       Extended error code
     * @param message   Error message, same message for ErrorString and JposException
     * @throws JposException If Error condition is true
     */
    public void check(boolean condition, int units, int error, int ext, String message) throws JposException {
        if (condition) {
            Data.ErrorUnits = units;
            logSet("ErrorUnits");
            Data.ErrorString = message;
            logSet("ErrorString");
            throw new JposException(error, ext, message);
        }
    }

    /**
     * If in synchronous mode, check condition and if true, sets error properties and throws JposException.
     * @param condition Error condition
     * @param units     Units to be filled in ErrorUnits
     * @param error     Error code
     * @param ext       Extended error code
     * @param message   Error message, same message for ErrorString and JposException
     * @throws JposException If Error condition is true
     */
    public void checkSync(boolean condition, int units, int error, int ext, String message) throws JposException {
        if (!Data.AsyncMode && TransactionCommand == null)
            check(condition, units, error, ext, message);
    }

    @Override
    public void controlClock(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9) throws JposException {
        logPreCall("ControlClock", "" + i + ", " + i1 + ", " + i2 + ", " + i3 + ", " + i4 + ", " + i5 + ", " + i6 + ", " + i7 + ", " + i8 + ", " + i9);
        checkOnline(i);
        checkDeviceIdle(i);
        try {
            int errbits;
            check(!Device.member(i1, validFunctions), i, JposConst.JPOS_E_ILLEGAL, 0, "Invalid function: " + i1);
            errbits = validateClockID(i, i2);
            check(errbits != 0, errbits, JposConst.JPOS_E_ILLEGAL, 0, "Invalid clock id: " + i2);
            check(i1 == RemoteOrderDisplayConst.ROD_CLK_START && (i3 < 0 || i3 > 23), i, JposConst.JPOS_E_ILLEGAL, 0, "Hour out of range: " + i3);
            check(i1 == RemoteOrderDisplayConst.ROD_CLK_START && (i4 < 0 || i4 > 59), i, JposConst.JPOS_E_ILLEGAL, 0, "Minute out of range: " + i4);
            check(i1 == RemoteOrderDisplayConst.ROD_CLK_START && (i5 < 0 || i5 > 59), i, JposConst.JPOS_E_ILLEGAL, 0, "Second out of range: " + i5);
            check(i1 == RemoteOrderDisplayConst.ROD_CLK_START && (i8 < 0 || i8 > 255), i, JposConst.JPOS_E_ILLEGAL, 0, "Attribute out of range: " + i8);
            errbits = validateCoordinates(i, i6, i7);
            check(Device.member(i1, initFunktions) && errbits != 0, errbits, JposConst.JPOS_E_ILLEGAL, 0, "Row and / or column too big for units: " + errbits);
            check(i1 == RemoteOrderDisplayConst.ROD_CLK_START && !Device.member(i9, validClockModes), i, JposConst.JPOS_E_ILLEGAL, 0, "Invalid clock mode: " + i9);
            RemoteOrderDisplayInterface.controlClock(i, i1, i2, i3, i4, i5, i6, i7, i8, i9);
        } finally {
            setDeviceIdle();
        }
        logCall("ControlClock");
    }

    private static final long[] validCursorFunctions = new long[]{
            RemoteOrderDisplayConst.ROD_CRS_LINE, RemoteOrderDisplayConst.ROD_CRS_LINE_BLINK, RemoteOrderDisplayConst.ROD_CRS_BLOCK,
            RemoteOrderDisplayConst.ROD_CRS_BLOCK_BLINK, RemoteOrderDisplayConst.ROD_CRS_OFF
    };

    @Override
    public void controlCursor(int i, int i1) throws JposException {
        logPreCall("ControlCursor", "" + i + ", " + i1);
        checkOnline(i);
        checkDeviceIdle(i);
        try {
            check(!Device.member(i1, validCursorFunctions), i, JposConst.JPOS_E_ILLEGAL, 0, "Invalid function: " + i1);
            RemoteOrderDisplayInterface.controlCursor(i, i1);
        } finally {
            setDeviceIdle();
        }
        logCall("ControlCursor");
    }

    private int validateBufferID(int units, int id) {
        int result = 0;
        while (units != 0) {
            int index = Data.unitsToFirstIndex(units);
            units &= ~(1 << index);
            if (Data.Unit[index].VideoSaveBuffers < id || id < 1)
                result |= 1 << index;
        }
        return result;
    }

    @Override
    public void freeVideoRegion(int i, int i1) throws JposException {
        logPreCall("FreeVideoRegion", "" + i + ", " + i1);
        checkOnline(i);
        int errunits = validateBufferID(i, i1);
        check(errunits != 0, errunits, JposConst.JPOS_E_ILLEGAL, 0, "BufferID " + i1 + " invalid for units " + i);
        RemoteOrderDisplayInterface.freeVideoRegion(i, i1);
        logCall("FreeVideoRegion");
    }

    @Override
    public void resetVideo(int i) throws JposException {
        logPreCall("ResetVideo", "" + i);
        checkOnline(i);
        checkDeviceIdle(i);
        try {
            RemoteOrderDisplayInterface.resetVideo(i);
        } finally {
            setDeviceIdle();
        }
        logCall("ResetVideo");
    }

    private int validateCharacterSet(int units, int cs) {
        int result = 0;
        while (units != 0) {
            int index = Data.unitsToFirstIndex(units);
            units &= ~(1 << index);
            if (!Data.Unit[index].CapSelectCharacterSet || !Device.member(cs, Device.stringArrayToLongArray(Data.Unit[index].CharacterSetList.split(","))))
                result |= 1 << index;
        }
        return result;
    }

    @Override
    public void selectChararacterSet(int i, int i1) throws JposException {
        logPreCall("SelectChararacterSet", "" + i + ", " + i1);
        checkOnline(i);
        checkDeviceIdle(i);
        try {
            int errunits = validateCharacterSet(i, i1);
            check(errunits != 0, errunits, JposConst.JPOS_E_ILLEGAL, 0, "Cannot select character set " + i1 + " for units " + i);
            RemoteOrderDisplayInterface.selectChararacterSet(i, i1);
        } finally {
            setDeviceIdle();
        }
        logCall("SelectChararacterSet");
    }

    @Override
    public void setCursor(int i, int i1, int i2) throws JposException {
        logPreCall("SetCursor", "" + i + ", " + i1 + ", " + i2);
        checkOnline(i);
        checkDeviceIdle(i);
        try {
            int errunits = validateCoordinates(i, i1, i2);
            check(errunits != 0, errunits, JposConst.JPOS_E_ILLEGAL, 0, "Row and / or column too big for units: " + errunits);
            RemoteOrderDisplayInterface.setCursor(i, i1, i2);
        } finally {
            setDeviceIdle();
        }
        logCall("SetCursor");
    }

    private void doItTrans(OutputRequest request, String what) throws JposException {
        if (TransactionCommand != null)
            TransactionCommand.addMethod(request);
        else  if (!callNowOrLater(request)) {
            logCall(what);
            return;
        }
        logAsyncCall(what);
    }

    @Override
    public void clearVideo(int i, int i1) throws JposException {
        logPreCall("ClearVideo", "" + i + ", " + i1);
        checkSyncOnline(i);
        check(i1 < 0 || i1 > 0xff, i, JposConst.JPOS_E_ILLEGAL, 0, "Illegal attribute value: " + i1);
        doItTrans(RemoteOrderDisplayInterface.clearVideo(i, i1), "ClearVideo");
    }

    @Override
    public void clearVideoRegion(int i, int i1, int i2, int i3, int i4, int i5) throws JposException {
        logPreCall("ClearVideoRegion", "" + i + ", " + i1 + ", " + i2 + ", " + i3 + ", " + i4 + ", " + i5);
        checkSyncOnline(i);
        check(i1 < 0, i, JposConst.JPOS_E_ILLEGAL, 0, "Row of region invalid: " + i1);
        check(i2 < 0, i, JposConst.JPOS_E_ILLEGAL, 0, "Column of region invalid: " + i2);
        check(i3 < 1, i, JposConst.JPOS_E_ILLEGAL, 0, "Height of region invalid: " + i3);
        check(i4 < 1, i, JposConst.JPOS_E_ILLEGAL, 0, "Width of region invalid: " + i4);
        int errorunits = validateCoordinates(i, i1, i2);                            // upper left corner invalid
        errorunits |= validateCoordinates(i, i1 + i3 - 1, i2 + i4 - 1); // lower right corner invalid
        checkSync(errorunits != 0, errorunits, JposConst.JPOS_E_ILLEGAL, 0, "Illegal region for units specified by " + errorunits);
        check(i5 < 0 || i5 > 0xff, i, JposConst.JPOS_E_ILLEGAL, 0, "Illegal attribute value: " + i5);
        doItTrans(RemoteOrderDisplayInterface.clearVideoRegion(i, i1, i2, i3, i4, i5), "ClearVideoRegion");
    }

    @Override
    public void copyVideoRegion(int i, int i1, int i2, int i3, int i4, int i5, int i6) throws JposException {
        logPreCall("CopyVideoRegion", "" + i + ", " + i1 + ", " + i2 + ", " + i3 + ", " + i4 + ", " + i5 + ", " + i6);
        checkSyncOnline(i);
        check(i1 < 0, i, JposConst.JPOS_E_ILLEGAL, 0, "Row of region invalid: " + i1);
        check(i2 < 0, i, JposConst.JPOS_E_ILLEGAL, 0, "Column of region invalid: " + i2);
        check(i3 < 1, i, JposConst.JPOS_E_ILLEGAL, 0, "Height of region invalid: " + i3);
        check(i4 < 1, i, JposConst.JPOS_E_ILLEGAL, 0, "Width of region invalid: " + i4);
        check(i5 < 0, i, JposConst.JPOS_E_ILLEGAL, 0, "TargetRow of region invalid: " + i5);
        check(i6 < 0, i, JposConst.JPOS_E_ILLEGAL, 0, "TargetColumn of region invalid: " + i6);
        int errorunits = validateCoordinates(i, i1, i2);                            // upper left source corner invalid
        errorunits |= validateCoordinates(i, i1 + i3 - 1, i2 + i4 - 1); // lower right source corner invalid
        errorunits |= validateCoordinates(i, i5 + i3 - 1, i6 + i4 - 1); // lower right target corner invalid
        checkSync(errorunits != 0, errorunits, JposConst.JPOS_E_ILLEGAL, 0, "Illegal region for units specified by " + errorunits);
        doItTrans(RemoteOrderDisplayInterface.copyVideoRegion(i, i1, i2, i3, i4, i5, i6), "CopyVideoRegion");
    }

    @Override
    public void displayData(int i, int i1, int i2, int i3, String s) throws JposException {
        logPreCall("DisplayData", "" + i + ", " + i1 + ", " + i2 + ", " + i3 + ", \"" + s + "\"");
        checkSyncOnline(i);
        check(i1 < 0, i, JposConst.JPOS_E_ILLEGAL, 0, "Row of region invalid: " + i1);
        check(i2 < 0, i, JposConst.JPOS_E_ILLEGAL, 0, "Column of region invalid: " + i2);
        int errorunits = validateCoordinates(i, i1, i2);                            // upper left source corner invalid
        checkSync(errorunits != 0, errorunits, JposConst.JPOS_E_ILLEGAL, 0, "Illegal region for units specified by " + errorunits);
        check(i3 < 0 || i3 > 0xff, i, JposConst.JPOS_E_ILLEGAL, 0, "Illegal attribute value: " + i3);
        doItTrans(RemoteOrderDisplayInterface.displayData(i, i1, i2, i3, s), "DisplayData");
    }

    private long[] validBorderType = new long[]{
            RemoteOrderDisplayConst.ROD_BDR_SINGLE, RemoteOrderDisplayConst.ROD_BDR_DOUBLE, RemoteOrderDisplayConst.ROD_BDR_SOLID
    };

    @Override
    public void drawBox(int i, int i1, int i2, int i3, int i4, int i5, int i6) throws JposException {
        logPreCall("DrawBox", "" + i + ", " + i1 + ", " + i2 + ", " + i3 + ", " + i4 + ", " + i5 + ", " + i6);
        checkSyncOnline(i);
        check(i1 < 0, i, JposConst.JPOS_E_ILLEGAL, 0, "Row of region invalid: " + i1);
        check(i2 < 0, i, JposConst.JPOS_E_ILLEGAL, 0, "Column of region invalid: " + i2);
        check(i3 < 1, i, JposConst.JPOS_E_ILLEGAL, 0, "Height of region invalid: " + i3);
        check(i4 < 1, i, JposConst.JPOS_E_ILLEGAL, 0, "Width of region invalid: " + i4);
        int errorunits = validateCoordinates(i, i1, i2);                            // upper left corner invalid
        errorunits |= validateCoordinates(i, i1 + i3 - 1, i2 + i4 - 1); // lower right corner invalid
        checkSync(errorunits != 0, errorunits, JposConst.JPOS_E_ILLEGAL, 0, "Illegal region for units specified by " + errorunits);
        check(i5 < 0 || i5 > 0xff, i, JposConst.JPOS_E_ILLEGAL, 0, "Illegal attribute value: " + i5);
        check(!Device.member(i6, validBorderType), i, JposConst.JPOS_E_ILLEGAL, 0, "Illegal border type: " + i6);
        doItTrans(RemoteOrderDisplayInterface.drawBox(i, i1, i2, i3, i4, i5, i6), "DrawBox");
    }

    @Override
    public void restoreVideoRegion(int i, int i1, int i2, int i3) throws JposException {
        logPreCall("RestoreVideoRegion", "" + i + ", " + i1 + ", " + i2 + ", " + i3);
        checkSyncOnline(i);
        check(i1 < 0, i, JposConst.JPOS_E_ILLEGAL, 0, "Row of region invalid: " + i1);
        check(i2 < 0, i, JposConst.JPOS_E_ILLEGAL, 0, "Column of region invalid: " + i2);
        int errorunits = validateCoordinates(i, i1, i2);                            // upper left source corner invalid
        checkSync(errorunits != 0, errorunits, JposConst.JPOS_E_ILLEGAL, 0, "Illegal region for units specified by " + errorunits);
        check(i3 < 1, i, JposConst.JPOS_E_ILLEGAL, 0, "Illegal buffer ID: " + i3);
        errorunits = validateBufferID(i, i3);
        checkSync(errorunits != 0, errorunits, JposConst.JPOS_E_ILLEGAL, 0, "Illegal buffer ID " + i3 + " for units specified by " + errorunits);
        doItTrans(RemoteOrderDisplayInterface.restoreVideoRegion(i, i1, i2, i3), "RestoreVideoRegion");
    }

    @Override
    public void saveVideoRegion(int i, int i1, int i2, int i3, int i4, int i5) throws JposException {
        logPreCall("SaveVideoRegion", "" + i + ", " + i1 + ", " + i2 + ", " + i3 + ", " + i4 + ", " + i5);
        checkSyncOnline(i);
        check(i1 < 0, i, JposConst.JPOS_E_ILLEGAL, 0, "Row of region invalid: " + i1);
        check(i2 < 0, i, JposConst.JPOS_E_ILLEGAL, 0, "Column of region invalid: " + i2);
        check(i3 < 1, i, JposConst.JPOS_E_ILLEGAL, 0, "Height of region invalid: " + i3);
        check(i4 < 1, i, JposConst.JPOS_E_ILLEGAL, 0, "Width of region invalid: " + i4);
        int errorunits = validateCoordinates(i, i1, i2);                            // upper left corner invalid
        errorunits |= validateCoordinates(i, i1 + i3 - 1, i2 + i4 - 1); // lower right corner invalid
        checkSync(errorunits != 0, errorunits, JposConst.JPOS_E_ILLEGAL, 0, "Illegal region for units specified by " + errorunits);
        check(i5 < 1, i, JposConst.JPOS_E_ILLEGAL, 0, "Illegal buffer ID: " + i5);
        errorunits = validateBufferID(i, i5);
        checkSync(errorunits != 0, errorunits, JposConst.JPOS_E_ILLEGAL, 0, "Illegal buffer ID " + i5 + " for units specified by " + errorunits);
        doItTrans(RemoteOrderDisplayInterface.saveVideoRegion(i, i1, i2, i3, i4, i5), "SaveVideoRegion");
    }

    private static final long[] validTransactionType = new long[] {
            RemoteOrderDisplayConst.ROD_TD_TRANSACTION, RemoteOrderDisplayConst.ROD_TD_NORMAL
    };

    @Override
    public void transactionDisplay(int i, int i1) throws JposException {
        logPreCall("TransactionDisplay", "" + i + ", " + i1);
        TransactionDisplay request;
        checkEnabled();
        check(!Device.member(i1, validTransactionType), i, JposConst.JPOS_E_ILLEGAL, 0, "Illegal transaction type: " + i1);
        if (i1 == RemoteOrderDisplayConst.ROD_TD_TRANSACTION) {
            check(TransactionCommand != null, i, JposConst.JPOS_E_ILLEGAL, 0, "Transaction in progress");
            TransactionCommand = request = RemoteOrderDisplayInterface.transactionDisplay(i, i1);
        }
        else {
            check(TransactionCommand == null, i, JposConst.JPOS_E_ILLEGAL, 0, "Transaction not in progress");
            request = RemoteOrderDisplayInterface.transactionDisplay(i, i1);
            TransactionCommand.addMethod(request);
            request = TransactionCommand;
            TransactionCommand = null;
            checkSyncOnline(i);
            if (!callNowOrLater(request)) {
                logCall("TransactionDisplay");
                return;
            }
        }
        logAsyncCall("TransactionDisplay");
    }

    private static final long[] validAttributeFunction = new long[]{
            RemoteOrderDisplayConst.ROD_UA_SET, RemoteOrderDisplayConst.ROD_UA_INTENSITY_ON,
            RemoteOrderDisplayConst.ROD_UA_INTENSITY_OFF, RemoteOrderDisplayConst.ROD_UA_REVERSE_ON,
            RemoteOrderDisplayConst.ROD_UA_REVERSE_OFF, RemoteOrderDisplayConst.ROD_UA_BLINK_ON,
            RemoteOrderDisplayConst.ROD_UA_BLINK_OFF
    };

    @Override
    public void updateVideoRegionAttribute(int i, int i1, int i2, int i3, int i4, int i5, int i6) throws JposException {
        logPreCall("UpdateVideoRegionAttribute", "" + i + ", " + i1 + ", " + i2 + ", " + i3 + ", " + i4 + ", " + i5 + ", " + i6);
        checkSyncOnline(i);
        check(!Device.member(i1, validAttributeFunction), i, JposConst.JPOS_E_ILLEGAL, 0, "Invalid attribute command: " + i1);
        check(i2 < 0, i, JposConst.JPOS_E_ILLEGAL, 0, "Row of region invalid: " + i2);
        check(i3 < 0, i, JposConst.JPOS_E_ILLEGAL, 0, "Column of region invalid: " + i3);
        check(i4 < 1, i, JposConst.JPOS_E_ILLEGAL, 0, "Height of region invalid: " + i4);
        check(i5 < 1, i, JposConst.JPOS_E_ILLEGAL, 0, "Width of region invalid: " + i5);
        int errorunits = validateCoordinates(i, i2, i3);                            // upper left corner invalid
        errorunits |= validateCoordinates(i, i2 + i4 - 1, i3 + i5 - 1); // lower right corner invalid
        checkSync(errorunits != 0, errorunits, JposConst.JPOS_E_ILLEGAL, 0, "Illegal region for units specified by " + errorunits);
        check(i1 == RemoteOrderDisplayConst.ROD_UA_SET && (i6 < 0 || i6 > 0xff), i, JposConst.JPOS_E_ILLEGAL, 0, "Illegal attribute value: " + i6);
        doItTrans(RemoteOrderDisplayInterface.updateVideoRegionAttribute(i, i1, i2, i3, i4, i5, i6), "UpdateVideoRegionAttribute");
    }

    private int validateTone(int bits) {
        int result = 0;
        while (bits != 0) {
            int index = Data.unitsToFirstIndex(bits);
            bits &= ~(1 << index);
            if (!Data.Unit[index].CapTone)
                result |= 1 << index;
        }
        return result;
    }

    @Override
    public void videoSound(int i, int i1, int i2, int i3, int i4) throws JposException {
        logPreCall("VideoSound", "" + i + ", " + i1 + ", " + i2 + ", " + i3 + ", " + i4);
        checkSyncOnline(i);
        check(i1 < 0, i, JposConst.JPOS_E_ILLEGAL, 0, "Frequency invalid: " + i1);
        check(i2 < 0, i, JposConst.JPOS_E_ILLEGAL, 0, "Duration invalid: " + i2);
        check(i3 != JposConst.JPOS_FOREVER && i3 < 0, i, JposConst.JPOS_E_ILLEGAL, 0, "Invalid cycle count: " + i3);
        check(i4 < 0, i, JposConst.JPOS_E_ILLEGAL, 0, "InterSoundWait invalid: " + i4);
        check(i2 + i4 < 0, i, JposConst.JPOS_E_ILLEGAL, 0, "Sound cylcle time invalid: " + (i2 + i4));
        int errunits = validateTone(i);
        checkSync(errunits != 0, errunits, JposConst.JPOS_E_FAILURE, 0, "Selected units do not support video sound: " + errunits);
        doItTrans(RemoteOrderDisplayInterface.videoSound(i, i1, i2, i3, i4), "VideoSound");
    }

    @Override
    public void checkFirstEnabled() throws JposException {
        checkOpened();
        check(!Props.FirstEnableHappened, 0, JposConst.JPOS_E_ILLEGAL, 0, "Device never enabled");
    }

    @Override
    public void checkOpened() throws JposException {
        check(Props.State == JposConst.JPOS_S_CLOSED, 0, JposConst.JPOS_E_CLOSED, 0, "Device not opened");
    }

    @Override
    public void checkEnabled() throws JposException {
        checkOpened();
        JposCommonProperties claimer = Props.getClaimingInstance();
        check(!Props.Claimed, 0,  claimer == null ? JposConst.JPOS_E_NOTCLAIMED : JposConst.JPOS_E_CLAIMED, 0, "Device not claimed");
        check(!Props.DeviceEnabled, 0, JposConst.JPOS_E_DISABLED, 0, "Device not enabled");
    }

    private void checkOnline(int units) throws JposException {
        checkEnabled();
        check((units & ~Data.UnitsOnline) != 0, units & ~Data.UnitsOnline, JposConst.JPOS_E_OFFLINE, 0, "Display units specified by " + (units & ~Data.UnitsOnline) + " Offline");
    }

    private void checkSyncOnline(int units) throws JposException {
        checkEnabled();
        if (!Data.AsyncMode && TransactionCommand == null)
            check((~Data.UnitsOnline & units) != 0, ~Data.UnitsOnline & units, JposConst.JPOS_E_OFFLINE, 0, "Display units specified by " + (units & ~Data.UnitsOnline) + " offline");
    }

    private void checkDeviceIdle(int units) throws JposException {
        synchronized (Device.AsyncProcessorRunning) {
            check(Data.State != JposConst.JPOS_S_IDLE, units, JposConst.JPOS_E_BUSY, 0, "Device is not idle: " + Data.State);
            Data.State = JposConst.JPOS_S_BUSY;
            logSet("State");
        }
    }

    private void setDeviceIdle() {
        synchronized (Device.AsyncProcessorRunning) {
            Data.State = JposConst.JPOS_S_IDLE;
            logSet("State");
        }
    }
}
