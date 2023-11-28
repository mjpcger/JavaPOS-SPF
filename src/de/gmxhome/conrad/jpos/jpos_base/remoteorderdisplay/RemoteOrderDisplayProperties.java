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
import jpos.events.JposEvent;
import net.bplaced.conrad.log4jpos.Level;

/**
 * Class containing the remote order display specific properties, their default values and default implementations of
 * RemoteOrderDisplayInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Remote Order Display.
 */
public class RemoteOrderDisplayProperties extends JposCommonProperties implements RemoteOrderDisplayInterface {
    /**
     * helper class, contains property values per display unit. A fixed size array with 32 elements will be allocated
     * due to the limit of maximum 32 display units. This is the bit count of an int value, and each bit of an int may
     * represent one display unit in some methods and events.
     * Default values are placeholders for offline displays. Whenever a display goes online, the service must fill the
     * display specific values into the corresponding UnitProperties instance.
     */
    public class UnitProperties {
        /**
         * UPOS property AutoToneDuration.
         */
        public int AutoToneDuration = 0;

        /**
         * UPOS property AutoToneFrequency.
         */
        public int AutoToneFrequency = 0;

        /**
         * UPOS property CapSelectCharacterSet.
         */
        public boolean CapSelectCharacterSet = false;

        /**
         * UPOS property CapTone.
         */
        public boolean CapTone = false;

        /**
         * UPOS property CapTouch.
         */
        public boolean CapTouch = false;

        /**
         * UPOS property CharacterSet.
         */
        public int CharacterSet = CharacterSetDef;

        /**
         * UPOS property CharacterSetList.
         */
        public String CharacterSetList = "";

        /**
         * UPOS property Clocks.
         */
        public int Clocks = 0;

        /**
         * UPOS property VideoMode.
         */
        public int VideoMode = VideoModeDef;

        /**
         * UPOS property VideoModesList.
         */
        public String VideoModesList = VideoModesListDef;

        /**
         * UPOS property VideoSaveBuffers.
         */
        public int VideoSaveBuffers = 0;
    }

    /**
     * UPOS property AutoToneDuration.
     */
    public int AutoToneDuration;

    /**
     * UPOS property AutoToneFrequency.
     */
    public int AutoToneFrequency;

    /**
     * UPOS property CapSelectCharacterSet.
     */
    public boolean CapSelectCharacterSet;

    /**
     * UPOS property CapTone.
     */
    public boolean CapTone;

    /**
     * UPOS property CapTouch.
     */
    public boolean CapTouch;

    /**
     * UPOS property CharacterSet.
     */
    public int CharacterSet;

    /**
     * UPOS property CharacterSetList.
     */
    public String CharacterSetList;

    /**
     * UPOS property Clocks.
     */
    public int Clocks;

    /**
     * UPOS property VideoMode.
     */
    public int VideoMode;

    /**
     * UPOS property VideoModesList.
     */
    public String VideoModesList;

    /**
     * UPOS property VideoSaveBuffers.
     */
    public int VideoSaveBuffers;

    /**
     * Unit specific properties, one per possible unit.
     */
    public UnitProperties[] Unit = new UnitProperties[32];

    /**
     * Copies all properties to the property set belonging to this object.
     */
    public void copyOut() {
        synchronized(Unit) {
            int index = unitsToFirstIndex(CurrentUnitID);
            Unit[index].VideoMode = VideoMode;
            Unit[index].AutoToneDuration = AutoToneDuration;
            Unit[index].AutoToneFrequency = AutoToneFrequency;
            Unit[index].CapSelectCharacterSet = CapSelectCharacterSet;
            Unit[index].CapTone = CapTone;
            Unit[index].CapTouch = CapTouch;
            Unit[index].CharacterSet = CharacterSet;
            Unit[index].CharacterSetList = CharacterSetList;
            Unit[index].Clocks = Clocks;
            Unit[index].VideoMode = VideoMode;
            Unit[index].VideoModesList = VideoModesList;
            Unit[index].VideoSaveBuffers = VideoSaveBuffers;
        }
    }

    /**
     * Retrieves all properties from the property set this object belongs to.
     */
    public void copyIn() {
        synchronized(Unit) {
            int index = unitsToFirstIndex(CurrentUnitID);
            if (AutoToneDuration != Unit[index].AutoToneDuration) {
                AutoToneDuration = Unit[index].AutoToneDuration;
                EventSource.logSet("AutoToneDuration");
            }
            if (AutoToneFrequency != Unit[index].AutoToneFrequency) {
                AutoToneFrequency = Unit[index].AutoToneFrequency;
                EventSource.logSet("AutoToneFrequency");
            }
            if (CapSelectCharacterSet != Unit[index].CapSelectCharacterSet) {
                CapSelectCharacterSet = Unit[index].CapSelectCharacterSet;
                EventSource.logSet("CapSelectCharacterSet");
            }
            if (CapTone != Unit[index].CapTone) {
                CapTone = Unit[index].CapTone;
                EventSource.logSet("CapTone");
            }
            if (CapTouch != Unit[index].CapTouch) {
                CapTouch = Unit[index].CapTouch;
                EventSource.logSet("CapTouch");
            }
            if (CharacterSet != Unit[index].CharacterSet) {
                CharacterSet = Unit[index].CharacterSet;
                EventSource.logSet("CharacterSet");
            }
            if (CharacterSetList != Unit[index].CharacterSetList) {
                CharacterSetList = Unit[index].CharacterSetList;
                EventSource.logSet("CharacterSetList");
            }
            if (Clocks != Unit[index].Clocks) {
                Clocks = Unit[index].Clocks;
                EventSource.logSet("Clocks");
            }
            if (VideoMode != Unit[index].VideoMode) {
                VideoMode = Unit[index].VideoMode;
                EventSource.logSet("VideoMode");
            }
            if (VideoModesList != Unit[index].VideoModesList) {
                VideoModesList = Unit[index].VideoModesList;
                EventSource.logSet("VideoModesList");
            }
            if (VideoSaveBuffers != Unit[index].VideoSaveBuffers) {
                VideoSaveBuffers = Unit[index].VideoSaveBuffers;
                EventSource.logSet("VideoSaveBuffers");
            }
        }
    }

    /**
     * Default value of CharacterSet property for offline units. Default: RemoteOrderDisplayConst.ROD_CS_ASCII. Can be
     * overwritten by objects derived from JposDevice within the changeDefaults method.
     */
    public int CharacterSetDef = RemoteOrderDisplayConst.ROD_CS_ASCII;

    /**
     * Default value of VideoMode property for offline units. Default: 0. Can be
     * overwritten by objects derived from JposDevice within the changeDefaults method.
     */
    public int VideoModeDef = 0;

    /**
     * Default value of VideoModesList property for offline units. Default: "0:0x0x0M". Can be
     * overwritten by objects derived from JposDevice within the changeDefaults method.
     */
    public String VideoModesListDef = "0:0x0x0M";

    /**
     * Default value of CapMapCharacterSet property. Default: true. Can be
     * overwritten by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapMapCharacterSetDef = true;

    /**
     * Default value of EventType property. Default: 0 (no data events). Can be
     * overwritten by objects derived from JposDevice within the changeDefaults method.
     */
    public int EventTypeDef = 0;

    /**
     * Default value of CapMapCharacterSet property. Default: true. Can be
     * overwritten by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean MapCharacterSetDef = true;

    /**
     * Default value of Timeout property. Default: 1000 (1 second). Can be
     * overwritten by objects derived from JposDevice within the changeDefaults method.
     */
    public int TimeoutDef = 1000;

    /**
     * Default value of SystemVideoSaveBuffers property. Default: 0. Can be
     * overwritten by objects derived from JposDevice within the changeDefaults method.
     */
    public int SystemVideoSaveBuffersDef = 0;

    /**
     * Default value of SystemClocks property. Default: 0. Can be
     * overwritten by objects derived from JposDevice within the changeDefaults method.
     */
    public int SystemClocksDef = 0;

    /**
     * UPOS property CapMapCharacterSet.
     */
    public boolean CapMapCharacterSet;

    /**
     * Default value of CapTransaction property. Default: true. Can be
     * overwritten by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapTransactionDef = true;

    /**
     * UPOS property CapTransaction.
     */
    public boolean CapTransaction;

    /**
     * Returns the lowest index of a unit specified by the given bitmask.
     * @param units A bitmask specifying one or more display units.
     * @return The lowest index where (units &amp; (1 &lt;&lt; index)) != 0.
     */
    public int unitsToFirstIndex(int units) {
        int i = -1;
        while (units != 0) {
            if ((units & (1 << ++i)) != 0)
                break;
        }
        return i;
    }

    /**
     * UPOS property EventType.
     */
    public int EventType;

    /**
     * UPOS property MapCharacterSet.
     */
    public boolean MapCharacterSet;

    /**
     * UPOS property SystemClocks.
     */
    public int SystemClocks;

    /**
     * UPOS property SystemVideoSaveBuffers.
     */
    public int SystemVideoSaveBuffers;

    /**
     * UPOS property Timeout.
     */
    public int Timeout;

    /**
     * UPOS property UnitsOnline.
     */
    public int UnitsOnline;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    public RemoteOrderDisplayProperties(int dev) {
        super(dev);
        FlagWhenIdleStatusValue = RemoteOrderDisplayService.ROD_SUE_IDLE;
    }

    @Override
    public void initOnOpen() {
        super.initOnOpen();
        CapMapCharacterSet = CapMapCharacterSetDef;
        CapTransaction = CapTransactionDef;
        EventType = EventTypeDef;
        MapCharacterSet = MapCharacterSetDef;
        Timeout = TimeoutDef;
        for (int i = Unit.length; --i >= 0;)
            Unit[i] = new UnitProperties();
    }

    @Override
    public boolean initOnFirstEnable() {
        if (!super.initOnFirstEnable()) {
            CurrentUnitID = RemoteOrderDisplayConst.ROD_UID_1;
            copyIn();
            SystemClocks = SystemClocksDef;
            SystemVideoSaveBuffers = SystemVideoSaveBuffersDef;
            UnitsOnline = 0;
            return false;
        }
        return true;
    }

    /*
     * Interface part of property set
     */

    @Override
    public JposOutputRequest newJposOutputRequest() {
        return new UnitOutputRequest(this, CurrentUnitID);
    }

    @Override
    public void autoToneDuration(int duration) throws JposException {
        AutoToneDuration = duration;
    }

    @Override
    public void autoToneFrequency(int frequency) throws JposException {
        AutoToneFrequency = frequency;
    }

    @Override
    public void currentUnitID(int unit) throws JposException {
        copyOut();
        CurrentUnitID = unit;
        copyIn();
    }

    @Override
    public void eventType(int type) throws JposException {
        EventType = type;
    }

    @Override
    public void mapCharacterSet(boolean map) throws JposException {
        MapCharacterSet = map;
    }

    @Override
    public void timeout(int milliseconds) throws JposException {
        Timeout = milliseconds;
    }

    @Override
    public void videoMode(int mode) throws JposException {
        VideoMode = mode;
    }

    @Override
    public void controlClock(int units, int function, int clockid, int hour, int minute, int second, int row, int column, int attribute, int mode) throws JposException {
    }

    @Override
    public void controlCursor(int units, int function) throws JposException {
    }

    @Override
    public void freeVideoRegion(int units, int bufferId) throws JposException {
    }

    @Override
    public void resetVideo(int units) throws JposException {
    }

    @Override
    public void selectChararacterSet(int units, int characterSet) throws JposException {
    }

    @Override
    public void setCursor(int units, int row, int column) throws JposException {
    }

    @Override
    public ClearVideo clearVideo(int units, int attribute) throws JposException {
        return new ClearVideo(this, units, attribute);
    }

    @Override
    public void clearVideo(ClearVideo request) throws JposException {
    }

    @Override
    public ClearVideoRegion clearVideoRegion(int units, int row, int column, int height, int width, int attribute) throws JposException  {
        return new ClearVideoRegion(this, units, row, column, height, width, attribute);
    }

    @Override
    public void clearVideoRegion(ClearVideoRegion request) throws JposException {
    }

    @Override
    public CopyVideoRegion copyVideoRegion(int units, int row, int column, int height, int width, int targetRow, int targetColumn) throws JposException {
        return new CopyVideoRegion(this, units, row, column, height, width, targetRow, targetColumn);
    }

    @Override
    public void copyVideoRegion(CopyVideoRegion request) throws JposException {
    }

    @Override
    public DisplayData displayData(int units, int row, int column, int attribute, String data) throws JposException {
        return new DisplayData(this, units, row, column, attribute, data);
    }

    @Override
    public void displayData(DisplayData request) throws JposException {
    }

    @Override
    public DrawBox drawBox(int units, int row, int column, int height, int width, int attribute, int bordertype) throws JposException {
        return new DrawBox(this, units, row, column, height, width, attribute, bordertype);
    }

    @Override
    public void drawBox(DrawBox request) throws JposException {
    }

    @Override
    public RestoreVideoRegion restoreVideoRegion(int units, int targetRow, int targetColumn, int bufferId) throws JposException {
        return new RestoreVideoRegion(this, units, targetRow, targetColumn, bufferId);
    }

    @Override
    public void restoreVideoRegion(RestoreVideoRegion request) throws JposException {
    }

    @Override
    public SaveVideoRegion saveVideoRegion(int units, int row, int column, int height, int width, int bufferId) throws JposException {
        return new SaveVideoRegion(this, units, row, column, height, width, bufferId);
    }

    @Override
    public void saveVideoRegion(SaveVideoRegion request) throws JposException {
    }

    @Override
    public TransactionDisplay transactionDisplay(int units, int function) throws JposException {
        return new TransactionDisplay(this, units, function);
    }

    @Override
    public void transactionDisplay(TransactionDisplay request) throws JposException {
    }

    @Override
    public UpdateVideoRegionAttribute updateVideoRegionAttribute(int units, int function, int row, int column, int height, int width, int attribute) throws JposException {
        return new UpdateVideoRegionAttribute(this, units, function, row, column, height, width, attribute);
    }

    @Override
    public void updateVideoRegionAttribute(UpdateVideoRegionAttribute request) throws JposException {
    }

    @Override
    public VideoSound videoSound(int units, int frequency, int duration, int numberOfCycles, int interSoundWait) throws JposException {
        return new VideoSound(this, units, frequency, duration, numberOfCycles, interSoundWait);
    }

    @Override
    public void videoSound(VideoSound request) throws JposException {
    }

}
