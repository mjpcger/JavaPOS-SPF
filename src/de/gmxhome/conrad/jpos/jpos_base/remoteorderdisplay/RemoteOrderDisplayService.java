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
import jpos.services.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;
import static jpos.RemoteOrderDisplayConst.*;
import static net.bplaced.conrad.log4jpos.Level.*;

/**
 * RemoteOrderDisplay service implementation. For more details about getter, setter and method implementations,
 * see JposBase.<br>
 * Special handling has been added to method DirectIO: Due to the fact that UPOS does not support a FlagWhenIdle
 * property for remote order displays, two commands have been added to the UPOS standard. See the description of
 * method DirectIO for details.
 */
public class RemoteOrderDisplayService extends JposBase implements RemoteOrderDisplayService116 {
    private final RemoteOrderDisplayProperties Data;

    private static final long[] validUnitIDs = {
            ROD_UID_1, ROD_UID_2, ROD_UID_3, ROD_UID_4, ROD_UID_5, ROD_UID_6, ROD_UID_7, ROD_UID_8,
            ROD_UID_9, ROD_UID_10, ROD_UID_11, ROD_UID_12, ROD_UID_13, ROD_UID_14, ROD_UID_15, ROD_UID_16,
            ROD_UID_17, ROD_UID_18, ROD_UID_19, ROD_UID_20, ROD_UID_21, ROD_UID_22, ROD_UID_23, ROD_UID_24,
            ROD_UID_25, ROD_UID_26, ROD_UID_27, ROD_UID_28, ROD_UID_29, ROD_UID_30, ROD_UID_31, ROD_UID_32
    };
    private static final long[] validEventTypes = {
            0, ROD_DE_TOUCH_UP, ROD_DE_TOUCH_DOWN, ROD_DE_TOUCH_UP|ROD_DE_TOUCH_DOWN, ROD_DE_TOUCH_MOVE,
            ROD_DE_TOUCH_MOVE|ROD_DE_TOUCH_UP, ROD_DE_TOUCH_MOVE|ROD_DE_TOUCH_DOWN,
            ROD_DE_TOUCH_MOVE|ROD_DE_TOUCH_UP|ROD_DE_TOUCH_DOWN
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
    public boolean getAsyncMode() throws JposException {
        checkEnabled();
        logGet("AsyncMode");
        return Props.AsyncMode;
    }

    @Override
    public void setAsyncMode(boolean b) throws JposException {
        logPreSet("AsyncMode");
        checkEnabled();
        DeviceInterface.asyncMode(b);
        logSet("AsyncMode");
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
        check(i < 0, Data.CurrentUnitID, JPOS_E_ILLEGAL, 0, "AutoToneDuration " + i + " invalid for unit " + Data.unitsToFirstIndex(Data.CurrentUnitID));
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
        check(i < 0, Data.CurrentUnitID, JPOS_E_ILLEGAL, 0, "AutoToneFrequency " + i + " invalid for unit " + Data.unitsToFirstIndex(Data.CurrentUnitID));
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
        check(!member(i, validUnitIDs), 0, JPOS_E_ILLEGAL, 0, i + " is not a valid unit ID");
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
        check(!member(i, validEventTypes), Data.CurrentUnitID, JPOS_E_ILLEGAL, 0, "EventType " + i + " invalid");
        checkNoChangedOrClaimed(Data.EventType, i);
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
        check(b && !Data.CapMapCharacterSet, -1, JPOS_E_ILLEGAL, 0, "MapCharacterSet " + b + " invalid");
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
        check(i < 0, -1, JPOS_E_ILLEGAL, 0, "Timeout " + i + " invalid");
        checkNoChangedOrClaimed(Data.Timeout, i);
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
        int count = RemoteOrderDisplayInterface.unitDataCount();
        Device.log(DEBUG, Props.LogicalName + ": VideoDataCount: " + count);
        return count;
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
        check(member(i , validModes), Data.CurrentUnitID, JPOS_E_ILLEGAL, 0, "VideoMode " + i + " invalid for unit " + Data.unitsToFirstIndex(Data.CurrentUnitID));
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

    private final long[] validFunctions = { ROD_CLK_PAUSE, ROD_CLK_START, ROD_CLK_RESUME, ROD_CLK_STOP, ROD_CLK_MOVE };

    private final long[] initFunktions = { ROD_CLK_START, ROD_CLK_MOVE };

    private final long[] validClockModes = { ROD_CLK_SHORT, ROD_CLK_NORMAL, ROD_CLK_12_LONG, ROD_CLK_24_LONG };

    /**
     * Checks whether the given coordinates are valid for the specified units.
     * @param bits      Unit to be checked.
     * @param row       Character row of coordinate.
     * @param column    Character column of coordinate.
     * @return Bit mask, 1 for every requested unit where row and column are not valid coordinates.
     */
    public int validateCoordinates(int bits, int row, int column) {
        int result = 0;
        if (row < 0 || column < 0)
            return bits;
        while (bits != 0) {
            int index = Data.unitsToFirstIndex(bits);
            String[] modi = Data.Unit[index].VideoModesList.split(",");
            bits &= ~(1 << index);
            for (String mode : modi) {
                String[] values = mode.split(":");
                if (Integer.parseInt(values[0]) == Data.Unit[index].VideoMode) {
                    String[] limits = values[1].split("x");
                    if (row >= Integer.parseInt(limits[0]) || column >= Integer.parseInt(limits[1]))
                        result |= 1 << index;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Checks if id is a valid clock id for all units specified by bits.
     * @param bits  Bit mask specifying all requested units.
     * @param id    Clock ID to be checked.
     * @return  Bit mask, 1 for every requested unit where id is not a valid clock ID.
     */
    public int validateClockID(int bits, int id) {
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
    public void controlClock(int Units, int function, int clockid, int hour, int minute, int second, int row, int column, int attribute, int mode) throws JposException {
        logPreCall("ControlClock", removeOuterArraySpecifier(new Object[]{Units, function, clockid, hour, minute, second, row, column, attribute, mode}, Device.MaxArrayStringElements));
        checkOnline(Units);
        checkDeviceIdle(Units);
        try {
            int errbits;
            check(!member(function, validFunctions), Units, JPOS_E_ILLEGAL, 0, "Invalid function: " + function);
            errbits = validateClockID(Units, clockid);
            check(errbits != 0, errbits, JPOS_E_ILLEGAL, 0, "Invalid clock id: " + clockid);
            check(function == ROD_CLK_START && (hour < 0 || hour > 23), Units, JPOS_E_ILLEGAL, 0, "Hour out of range: " + hour);
            check(function == ROD_CLK_START && (minute < 0 || minute > 59), Units, JPOS_E_ILLEGAL, 0, "Minute out of range: " + minute);
            check(function == ROD_CLK_START && (second < 0 || second > 59), Units, JPOS_E_ILLEGAL, 0, "Second out of range: " + second);
            check(function == ROD_CLK_START && (attribute < 0 || attribute > 255), Units, JPOS_E_ILLEGAL, 0, "Attribute out of range: " + attribute);
            errbits = validateCoordinates(Units, row, column);
            check(member(function, initFunktions) && errbits != 0, errbits, JPOS_E_ILLEGAL, 0, "Row and / or column too big for units: " + errbits);
            check(function == ROD_CLK_START && !member(mode, validClockModes), Units, JPOS_E_ILLEGAL, 0, "Invalid clock mode: " + mode);
            RemoteOrderDisplayInterface.controlClock(Units, function, clockid, hour, minute, second, row, column, attribute, mode);
        } finally {
            setDeviceIdle();
        }
        logCall("ControlClock");
    }

    private static final long[] validCursorFunctions = { ROD_CRS_LINE, ROD_CRS_LINE_BLINK, ROD_CRS_BLOCK, ROD_CRS_BLOCK_BLINK, ROD_CRS_OFF };

    @Override
    public void controlCursor(int units, int function) throws JposException {
        logPreCall("ControlCursor", removeOuterArraySpecifier(new Object[]{units, function}, Device.MaxArrayStringElements));
        checkOnline(units);
        checkDeviceIdle(units);
        try {
            check(!member(function, validCursorFunctions), units, JPOS_E_ILLEGAL, 0, "Invalid function: " + function);
            RemoteOrderDisplayInterface.controlCursor(units, function);
        } finally {
            setDeviceIdle();
        }
        logCall("ControlCursor");
    }

    /**
     * Checks if id is a valid buffer id for all specified units.
     * @param units Bit mask specifying all requested units.
     * @param id    Buffer ID to be checked.
     * @return  Bit mask, 1 for every requested unit where id is not a valid buffer ID.
     */
    public int validateBufferID(int units, int id) {
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
    public void freeVideoRegion(int units, int bufferId) throws JposException {
        logPreCall("FreeVideoRegion", removeOuterArraySpecifier(new Object[]{units, bufferId}, Device.MaxArrayStringElements));
        checkOnline(units);
        int errunits = validateBufferID(units, bufferId);
        check(errunits != 0, errunits, JPOS_E_ILLEGAL, 0, "BufferID " + bufferId + " invalid for units " + units);
        RemoteOrderDisplayInterface.freeVideoRegion(units, bufferId);
        logCall("FreeVideoRegion");
    }

    @Override
    public void resetVideo(int units) throws JposException {
        logPreCall("ResetVideo", removeOuterArraySpecifier(new Object[]{units}, Device.MaxArrayStringElements));
        checkOnline(units);
        checkDeviceIdle(units);
        try {
            RemoteOrderDisplayInterface.resetVideo(units);
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
            if ((!Data.Unit[index].CapSelectCharacterSet && Data.Unit[index].CharacterSet != cs) || !member(cs, stringArrayToLongArray(Data.Unit[index].CharacterSetList.split(","))))
                result |= 1 << index;
        }
        return result;
    }

    @Override
    public void selectChararacterSet(int units, int characterSet) throws JposException {
        logPreCall("SelectChararacterSet", removeOuterArraySpecifier(new Object[]{units, characterSet}, Device.MaxArrayStringElements));
        checkOnline(units);
        checkDeviceIdle(units);
        try {
            int errunits = validateCharacterSet(units, characterSet);
            check(errunits != 0, errunits, JPOS_E_ILLEGAL, 0, "Cannot select character set " + characterSet + " for units " + units);
            RemoteOrderDisplayInterface.selectChararacterSet(units, characterSet);
        } finally {
            setDeviceIdle();
        }
        logCall("SelectChararacterSet");
    }

    @Override
    public void setCursor(int units, int row, int column) throws JposException {
        logPreCall("SetCursor", removeOuterArraySpecifier(new Object[]{units, row, column}, Device.MaxArrayStringElements));
        checkOnline(units);
        checkDeviceIdle(units);
        try {
            int errunits = validateCoordinates(units, row, column);
            check(errunits != 0, errunits, JPOS_E_ILLEGAL, 0, "Row and / or column too big for units: " + errunits);
            RemoteOrderDisplayInterface.setCursor(units, row, column);
        } finally {
            setDeviceIdle();
        }
        logCall("SetCursor");
    }

    private void doItTrans(UnitOutputRequest request, String what) throws JposException {
        if (TransactionCommand != null)
            TransactionCommand.addMethod(request);
        else  if (!callNowOrLater(request)) {
            logCall(what);
            return;
        }
        logAsyncCall(what);
    }

    @Override
    public void clearVideo(int units, int attribute) throws JposException {
        logPreCall("ClearVideo", removeOuterArraySpecifier(new Object[]{units, attribute}, Device.MaxArrayStringElements));
        checkSyncOnline(units);
        check(attribute < 0 || attribute > 0xff, units, JPOS_E_ILLEGAL, 0, "Illegal attribute value: " + attribute);
        doItTrans(RemoteOrderDisplayInterface.clearVideo(units, attribute), "ClearVideo");
    }

    @Override
    public void clearVideoRegion(int units, int row, int column, int height, int width, int attribute) throws JposException {
        logPreCall("ClearVideoRegion", removeOuterArraySpecifier(new Object[]{units, row, column, height, width, attribute}, Device.MaxArrayStringElements));
        checkSyncOnline(units);
        check(row < 0, units, JPOS_E_ILLEGAL, 0, "Row of region invalid: " + row);
        check(column < 0, units, JPOS_E_ILLEGAL, 0, "Column of region invalid: " + column);
        check(height < 1, units, JPOS_E_ILLEGAL, 0, "Height of region invalid: " + height);
        check(width < 1, units, JPOS_E_ILLEGAL, 0, "Width of region invalid: " + width);
        int errorunits = validateCoordinates(units, row, column);                            // upper left corner invalid
        errorunits |= validateCoordinates(units, row + height - 1, column + width - 1); // lower right corner invalid
        checkSync(errorunits != 0, errorunits, JPOS_E_ILLEGAL, 0, "Illegal region for units specified by " + errorunits);
        check(attribute < 0 || attribute > 0xff, units, JPOS_E_ILLEGAL, 0, "Illegal attribute value: " + attribute);
        doItTrans(RemoteOrderDisplayInterface.clearVideoRegion(units, row, column, height, width, attribute), "ClearVideoRegion");
    }

    @Override
    public void copyVideoRegion(int units, int row, int column, int height, int width, int targetRow, int targetColumn) throws JposException {
        logPreCall("CopyVideoRegion", removeOuterArraySpecifier(new Object[]{units, row, column, height, width, targetRow, targetColumn}, Device.MaxArrayStringElements));
        checkSyncOnline(units);
        check(row < 0, units, JPOS_E_ILLEGAL, 0, "Row of region invalid: " + row);
        check(column < 0, units, JPOS_E_ILLEGAL, 0, "Column of region invalid: " + column);
        check(height < 1, units, JPOS_E_ILLEGAL, 0, "Height of region invalid: " + height);
        check(width < 1, units, JPOS_E_ILLEGAL, 0, "Width of region invalid: " + width);
        check(targetRow < 0, units, JPOS_E_ILLEGAL, 0, "TargetRow of region invalid: " + targetRow);
        check(targetColumn < 0, units, JPOS_E_ILLEGAL, 0, "TargetColumn of region invalid: " + targetColumn);
        int errorunits = validateCoordinates(units, row, column);                            // upper left source corner invalid
        errorunits |= validateCoordinates(units, row + height - 1, column + width - 1); // lower right source corner invalid
        errorunits |= validateCoordinates(units, targetRow + height - 1, targetColumn + width - 1); // lower right target corner invalid
        checkSync(errorunits != 0, errorunits, JPOS_E_ILLEGAL, 0, "Illegal region for units specified by " + errorunits);
        doItTrans(RemoteOrderDisplayInterface.copyVideoRegion(units, row, column, height, width, targetRow, targetColumn), "CopyVideoRegion");
    }

    @Override
    public void displayData(int units, int row, int column, int attribute, String data) throws JposException {
        logPreCall("DisplayData", removeOuterArraySpecifier(new Object[]{units, row, column, attribute, data}, Device.MaxArrayStringElements));
        checkSyncOnline(units);
        check(row < 0, units, JPOS_E_ILLEGAL, 0, "Row of region invalid: " + row);
        check(column < 0, units, JPOS_E_ILLEGAL, 0, "Column of region invalid: " + column);
        int errorunits = validateCoordinates(units, row, column);                            // upper left source corner invalid
        checkSync(errorunits != 0, errorunits, JPOS_E_ILLEGAL, 0, "Illegal region for units specified by " + errorunits);
        check(attribute < 0 || attribute > 0xff, units, JPOS_E_ILLEGAL, 0, "Illegal attribute value: " + attribute);
        doItTrans(RemoteOrderDisplayInterface.displayData(units, row, column, attribute, data), "DisplayData");
    }

    private final long[] validBorderType = { ROD_BDR_SINGLE, ROD_BDR_DOUBLE, ROD_BDR_SOLID };

    @Override
    public void drawBox(int units, int row, int column, int height, int width, int attribute, int borderType) throws JposException {
        logPreCall("DrawBox", removeOuterArraySpecifier(new Object[]{units, row, column, height, width, attribute, borderType}, Device.MaxArrayStringElements));
        checkSyncOnline(units);
        check(row < 0, units, JPOS_E_ILLEGAL, 0, "Row of region invalid: " + row);
        check(column < 0, units, JPOS_E_ILLEGAL, 0, "Column of region invalid: " + column);
        check(height < 1, units, JPOS_E_ILLEGAL, 0, "Height of region invalid: " + height);
        check(width < 1, units, JPOS_E_ILLEGAL, 0, "Width of region invalid: " + width);
        int errorunits = validateCoordinates(units, row, column);                            // upper left corner invalid
        errorunits |= validateCoordinates(units, row + height - 1, column + width - 1); // lower right corner invalid
        checkSync(errorunits != 0, errorunits, JPOS_E_ILLEGAL, 0, "Illegal region for units specified by " + errorunits);
        check(attribute < 0 || attribute > 0xff, units, JPOS_E_ILLEGAL, 0, "Illegal attribute value: " + attribute);
        check(!member(borderType, validBorderType), units, JPOS_E_ILLEGAL, 0, "Illegal border type: " + borderType);
        doItTrans(RemoteOrderDisplayInterface.drawBox(units, row, column, height, width, attribute, borderType), "DrawBox");
    }

    @Override
    public void restoreVideoRegion(int units, int targetRow, int targetColumn, int bufferId) throws JposException {
        logPreCall("RestoreVideoRegion", removeOuterArraySpecifier(new Object[]{units, targetRow, targetColumn, bufferId}, Device.MaxArrayStringElements));
        checkSyncOnline(units);
        check(targetRow < 0, units, JPOS_E_ILLEGAL, 0, "Row of region invalid: " + targetRow);
        check(targetColumn < 0, units, JPOS_E_ILLEGAL, 0, "Column of region invalid: " + targetColumn);
        int errorunits = validateCoordinates(units, targetRow, targetColumn);                            // upper left source corner invalid
        checkSync(errorunits != 0, errorunits, JPOS_E_ILLEGAL, 0, "Illegal region for units specified by " + errorunits);
        check(bufferId < 1, units, JPOS_E_ILLEGAL, 0, "Illegal buffer ID: " + bufferId);
        errorunits = validateBufferID(units, bufferId);
        checkSync(errorunits != 0, errorunits, JPOS_E_ILLEGAL, 0, "Illegal buffer ID " + bufferId + " for units specified by " + errorunits);
        doItTrans(RemoteOrderDisplayInterface.restoreVideoRegion(units, targetRow, targetColumn, bufferId), "RestoreVideoRegion");
    }

    @Override
    public void saveVideoRegion(int units, int row, int column, int height, int width, int bufferId) throws JposException {
        logPreCall("SaveVideoRegion", removeOuterArraySpecifier(new Object[]{units, row, column, height, width, bufferId}, Device.MaxArrayStringElements));
        checkSyncOnline(units);
        check(row < 0, units, JPOS_E_ILLEGAL, 0, "Row of region invalid: " + row);
        check(column < 0, units, JPOS_E_ILLEGAL, 0, "Column of region invalid: " + column);
        check(height < 1, units, JPOS_E_ILLEGAL, 0, "Height of region invalid: " + height);
        check(width < 1, units, JPOS_E_ILLEGAL, 0, "Width of region invalid: " + width);
        int errorunits = validateCoordinates(units, row, column);                            // upper left corner invalid
        errorunits |= validateCoordinates(units, row + height - 1, column + width - 1); // lower right corner invalid
        checkSync(errorunits != 0, errorunits, JPOS_E_ILLEGAL, 0, "Illegal region for units specified by " + errorunits);
        check(bufferId < 1, units, JPOS_E_ILLEGAL, 0, "Illegal buffer ID: " + bufferId);
        errorunits = validateBufferID(units, bufferId);
        checkSync(errorunits != 0, errorunits, JPOS_E_ILLEGAL, 0, "Illegal buffer ID " + bufferId + " for units specified by " + errorunits);
        doItTrans(RemoteOrderDisplayInterface.saveVideoRegion(units, row, column, height, width, bufferId), "SaveVideoRegion");
    }

    private static final long[] validTransactionType = { ROD_TD_TRANSACTION, ROD_TD_NORMAL };

    @Override
    public void transactionDisplay(int units, int function) throws JposException {
        logPreCall("TransactionDisplay", removeOuterArraySpecifier(new Object[]{units, function}, Device.MaxArrayStringElements));
        TransactionDisplay request;
        checkEnabled();
        check(!member(function, validTransactionType), units, JPOS_E_ILLEGAL, 0, "Illegal transaction type: " + function);
        if (function == ROD_TD_TRANSACTION) {
            check(TransactionCommand != null, units, JPOS_E_ILLEGAL, 0, "Transaction in progress");
            TransactionCommand = request = RemoteOrderDisplayInterface.transactionDisplay(units, function);
        }
        else {
            check(TransactionCommand == null, units, JPOS_E_ILLEGAL, 0, "Transaction not in progress");
            request = RemoteOrderDisplayInterface.transactionDisplay(units, function);
            TransactionCommand.addMethod(request);
            request = TransactionCommand;
            TransactionCommand = null;
            checkSyncOnline(units);
            if (!callNowOrLater(request)) {
                logCall("TransactionDisplay");
                return;
            }
        }
        logAsyncCall("TransactionDisplay");
    }

    private static final long[] validAttributeFunction = {
            ROD_UA_SET, ROD_UA_INTENSITY_ON, ROD_UA_INTENSITY_OFF, ROD_UA_REVERSE_ON, ROD_UA_REVERSE_OFF,
            ROD_UA_BLINK_ON, ROD_UA_BLINK_OFF
    };

    @Override
    public void updateVideoRegionAttribute(int units, int function, int row, int column, int height, int width, int attribute) throws JposException {
        logPreCall("UpdateVideoRegionAttribute", removeOuterArraySpecifier(new Object[]{units, function, row, column, height, width, attribute}, Device.MaxArrayStringElements));
        checkSyncOnline(units);
        check(!member(function, validAttributeFunction), units, JPOS_E_ILLEGAL, 0, "Invalid attribute command: " + function);
        check(row < 0, units, JPOS_E_ILLEGAL, 0, "Row of region invalid: " + row);
        check(column < 0, units, JPOS_E_ILLEGAL, 0, "Column of region invalid: " + column);
        check(height < 1, units, JPOS_E_ILLEGAL, 0, "Height of region invalid: " + height);
        check(width < 1, units, JPOS_E_ILLEGAL, 0, "Width of region invalid: " + width);
        int errorunits = validateCoordinates(units, row, column);                            // upper left corner invalid
        errorunits |= validateCoordinates(units, row + height - 1, column + width - 1); // lower right corner invalid
        checkSync(errorunits != 0, errorunits, JPOS_E_ILLEGAL, 0, "Illegal region for units specified by " + errorunits);
        check(function == ROD_UA_SET && (attribute < 0 || attribute > 0xff), units, JPOS_E_ILLEGAL, 0, "Illegal attribute value: " + attribute);
        doItTrans(RemoteOrderDisplayInterface.updateVideoRegionAttribute(units, function, row, column, height, width, attribute), "UpdateVideoRegionAttribute");
    }

    /**
     * Checks if CapTone is true for all units specified by bits.
     * @param bits  Bit mask specifying all units to be checked.
     * @return Bit mask, 1 for every requested unit where CapTone is false.
     */
    public int validateTone(int bits) {
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
    public void videoSound(int units, int frequency, int duration, int NumberOfCycles, int interSountWait) throws JposException {
        logPreCall("VideoSound", removeOuterArraySpecifier(new Object[]{units, frequency, duration, NumberOfCycles, interSountWait}, Device.MaxArrayStringElements));
        checkSyncOnline(units);
        check(frequency < 0, units, JPOS_E_ILLEGAL, 0, "Frequency invalid: " + frequency);
        check(duration < 0, units, JPOS_E_ILLEGAL, 0, "Duration invalid: " + duration);
        check(NumberOfCycles != JPOS_FOREVER && NumberOfCycles < 0, units, JPOS_E_ILLEGAL, 0, "Invalid cycle count: " + NumberOfCycles);
        check(interSountWait < 0, units, JPOS_E_ILLEGAL, 0, "InterSoundWait invalid: " + interSountWait);
        check(duration + interSountWait < 0, units, JPOS_E_ILLEGAL, 0, "Sound cylcle time invalid: " + (duration + interSountWait));
        int errunits = validateTone(units);
        checkSync(errunits != 0, errunits, JPOS_E_FAILURE, 0, "Selected units do not support video sound: " + errunits);
        doItTrans(RemoteOrderDisplayInterface.videoSound(units, frequency, duration, NumberOfCycles, interSountWait), "VideoSound");
    }

    @Override
    public void checkFirstEnabled() throws JposException {
        checkOpened();
        check(!Props.FirstEnableHappened, 0, JPOS_E_ILLEGAL, 0, "Device never enabled");
    }

    @Override
    public void checkOpened() throws JposException {
        check(Props.State == JPOS_S_CLOSED, 0, JPOS_E_CLOSED, 0, "Device not opened");
    }

    @Override
    public void checkEnabled() throws JposException {
        checkOpened();
        JposCommonProperties claimer = Props.getClaimingInstance();
        check(!Props.Claimed, 0,  claimer == null ? JPOS_E_NOTCLAIMED : JPOS_E_CLAIMED, 0, "Device not claimed");
        check(!Props.DeviceEnabled, 0, JPOS_E_DISABLED, 0, "Device not enabled");
    }

    private void checkOnline(int units) throws JposException {
        checkEnabled();
        check((units & ~Data.UnitsOnline) != 0, units & ~Data.UnitsOnline, JPOS_E_OFFLINE, 0, "Display units specified by " + (units & ~Data.UnitsOnline) + " Offline");
    }

    private void checkSyncOnline(int units) throws JposException {
        checkEnabled();
        if (!Data.AsyncMode && TransactionCommand == null)
            check((~Data.UnitsOnline & units) != 0, ~Data.UnitsOnline & units, JPOS_E_OFFLINE, 0, "Display units specified by " + (units & ~Data.UnitsOnline) + " offline");
    }

    private void checkDeviceIdle(int units) throws JposException {
        synchronized (Device.AsyncProcessorRunning) {
            check(Data.State != JPOS_S_IDLE, units, JPOS_E_BUSY, 0, "Device is not idle: " + Data.State);
            Data.State = JPOS_S_BUSY;
            logSet("State");
        }
    }

    private void setDeviceIdle() {
        synchronized (Device.AsyncProcessorRunning) {
            Data.State = JPOS_S_IDLE;
            logSet("State");
        }
    }
}
