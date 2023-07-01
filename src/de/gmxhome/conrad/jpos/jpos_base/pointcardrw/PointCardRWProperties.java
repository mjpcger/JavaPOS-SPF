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
import jpos.*;

/**
 * Class containing the point card reader / writer specific properties, their default values and default implementations of
 * PointCardRWInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Point Card Reader / Writer.
 */
public class PointCardRWProperties extends JposCommonProperties implements PointCardRWInterface {
    /**
     * UPOS property CapBold. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapBold = false;

    /**
     * UPOS property CapCardEntranceSensor. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method. Keep in mind that this value will be automatically converted to JPOS_TRUE or JPOS_FALSE.
     */
    public boolean CapCardEntranceSensor = false;

    /**
     * UPOS property CapCharacterSet. Default: CCS_ASCII. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int CapCharacterSet = PointCardRWConst.PCRW_CCS_ASCII;

    /**
     * UPOS property CapCleanCard. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapCleanCard = false;

    /**
     * UPOS property CapClearPrint. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapClearPrint = false;

    /**
     * UPOS property CapDhigh. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapDhigh = false;

    /**
     * UPOS property CapDwide. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapDwide = false;

    /**
     * UPOS property CapDwideDhigh. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapDwideDhigh = false;

    /**
     * UPOS property CapItalic. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapItalic = false;

    /**
     * UPOS property CapLeft90. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapLeft90 = false;

    /**
     * UPOS property CapMapCharacterSet. Default: true. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapMapCharacterSet = true;

    /**
     * UPOS property CapPrint. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapPrint = false;

    /**
     * UPOS property CapPrintMode. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapPrintMode = false;

    /**
     * UPOS property CapRight90. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapRight90 = false;

    /**
     * UPOS property CapRotate180. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapRotate180 = false;

    /**
     * UPOS property CapTracksToRead. Default: 0. Should be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int CapTracksToRead = 0;

    /**
     * UPOS property CapTracksToWrite. Default: 0. Should be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int CapTracksToWrite = 0;

    /**
     * UPOS property CardState. Default: STATE_NOCARD. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int CardState = PointCardRWConst.PCRW_STATE_NOCARD;

    /**
     * UPOS property CharacterSet. Default: 0. Should be overwritten by objects derived from JposDevice within the
     * changeDefaults method if CapPrint is true.
     */
    public int CharacterSet = 0;

    /**
     * UPOS property CharacterSetList. Default: An empty string. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public String CharacterSetList = "";

    /**
     * UPOS property FontTypeFaceList. Default: An empty string. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public String FontTypeFaceList = "";

    /**
     * UPOS property LineChars. Default: 0. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int LineChars = 0;

    /**
     * UPOS property LineCharsList. Default: An empty string. Must be overwritten by objects derived from JposDevice within the
     * changeDefaults method if CapPrint is true.
     */
    public String LineCharsList = "";

    /**
     * UPOS property LineHeight. Default: 0. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int LineHeight = 0;

    /**
     * UPOS property LineSpacing. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int LineSpacing = 0;

    /**
     * UPOS property LineWidth. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int LineWidth = 0;

    /**
     * UPOS property MapCharacterSet. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean MapCharacterSet = false;

    /**
     * UPOS property MapMode. Default: MM_DOTS. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int MapMode = PointCardRWConst.PCRW_MM_DOTS;

    /**
     * UPOS property MaxLines. Default: 0. Must be overwritten by objects derived from JposDevice within the
     * changeDefaults method if CapPrint is true.
     */
    public int MaxLines = 0;

    /**
     * UPOS property PrintHeight. Default: 0. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int PrintHeight = 0;

    /**
     * UPOS properties ReadState1 and ReadState1. Default: { 0, 0 }. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public Integer[] ReadState = {0, 0};

    /**
     * UPOS property RecvLength1 and RecvLength2. Default: { 0, 0 }. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public Integer[] RecvLength = {0, 0};

    /**
     * UPOS property SidewaysMaxChars. Default: 0. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int SidewaysMaxChars = 0;

    /**
     * UPOS property SidewaysMaxLines. Default: 0. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int SidewaysMaxLines = 0;

    /**
     * UPOS property TracksToRead. Default: 0. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int TracksToRead = 0;

    /**
     * UPOS property TracksToWrite. Default: 0. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int TracksToWrite = 0;

    /**
     * Internal property TrackData. Contains the values of the UPOS propertier Track1Data, Track2Data, Track3Data,
     * Track4Data, Track5Data and Track6Data. TrackData[<i>N</i>-1] corresponds to Track<i>N</i>Data.
     */
    public String[] TrackData = {"", "", "", "", "", ""};

    /**
     * UPOS property WriteState1 and WriteState2. Default: { 0, 0 }. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public Integer[] WriteState = {0,0};

    /**
     * Internal property WriteData. Contains the values of the UPOS properties Write1Data, Write2Data, Write3Data,
     * Write4Data, Write5Data and Write6Data. WriteData[<i>N</i>-1] corresponds to Write<i>N</i>Data.
     */
    public String[] WriteData = {"", "", "", "", "", ""};

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected PointCardRWProperties(int dev) {
        super(dev);
        ExclusiveUse = ExclusiveYes;
        for (int i = 1; i <= 6; i++) {
            setReadState(i, JposConst.JPOS_E_FAILURE);
            setWriteState(i, JposConst.JPOS_E_FAILURE);
        }
    }

    /**
     * Set track specific byte-size information in integer array of size 2
     * @param property    Target int[2].
     * @param trackNumber Track number between 1 and 6.
     * @param value       Value to be set.
     */
    void setTrackProperty(Integer[] property, int trackNumber, int value) {
        if (trackNumber >= 1 && trackNumber <= 6) {
            int shift = (--trackNumber & 0x3) << 3;
            int index = trackNumber >> 2;
            property[index] = (property[index] & ~(0xff << shift)) | ((value & 0xff) << shift);
        }
    }

    /**
     * Return track specific byte-size information from integer array of size 2
     * @param property    Source int[2].
     * @param trackNumber Track number between 1 and 6.
     * @return  Byte-size value stored in source array.
     */
    int getTrackProperty(Integer[]property, int trackNumber) {
        if (trackNumber >= 1 && trackNumber <= 6) {
            int shift = (--trackNumber & 0x3) << 3;
            return (property[trackNumber >> 2] >> shift) & 0xff;
        }
        return 0;
    }

    /**
     * Set the state given for the specified track into property ReadState1 or ReadState2.
     * @param trackNumber   Number of the track
     * @param value         New state of the track specified by trackNumber
     */
    public void setReadState(int trackNumber, int value) {
        setTrackProperty(ReadState, trackNumber, value);
    }

    /**
     * get the state of the specified track from property ReadState1 or ReadState2.
     * @param trackNumber   Number of the track.
     * @return Read state of the specified track.
     */
    public int getReadState(int trackNumber) {
        return getTrackProperty(ReadState, trackNumber);
    }

    /**
     * Set the state given for the specified track into property WriteState1 or WriteState2.
     * @param trackNumber   Number of the track
     * @param value         New state of the track specified by trackNumber
     */
    public void setWriteState(int trackNumber, int value) {
        setTrackProperty(WriteState, trackNumber, value);
    }

    /**
     * get the state of the specified track from property WriteState1 or WriteState2.
     * @param trackNumber   Number of the track.
     * @return Write state of the specified track.
     */
    public int getWriteState(int trackNumber) {
        return getTrackProperty(WriteState, trackNumber);
    }

    /**
     * Set the length given for the specified track into property RecvLength1 or RecvLength2.
     * @param trackNumber   Number of the track
     * @param value         New state of the track specified by trackNumber
     */
    public void setRecvLength(int trackNumber, int value) {
        setTrackProperty(RecvLength, trackNumber, value);
    }

    /**
     * get the length of the specified track from property RecvLength1 or RecvLength2.
     * @param trackNumber   Number of the track.
     * @return Track length
     */
    public int getRecvLength(int trackNumber) {
        return getTrackProperty(RecvLength, trackNumber);
    }

    @Override
    public void characterSet(int code) throws JposException {
        CharacterSet = code;
    }

    @Override
    public void lineChars(int chars) throws JposException {
        LineChars = chars;
    }

    @Override
    public void lineHeight(int height) throws JposException {
        LineHeight = height;
    }

    @Override
    public void lineSpacing(int spacing) throws JposException {
        LineSpacing = spacing;
    }

    @Override
    public void mapCharacterSet(boolean flag) throws JposException {
        MapCharacterSet = flag;
    }

    @Override
    public void mapMode(int mode) throws JposException {
        MapMode = mode;
    }

    @Override
    public void tracksToRead(int tracks) throws JposException {
        TracksToRead = tracks;
    }

    @Override
    public void tracksToWrite(int tracks) throws JposException {
        TracksToWrite = tracks;
    }

    @Override
    public void write1Data(String trackdata) throws JposException {
        WriteData[0] = trackdata;
    }

    @Override
    public void write2Data(String trackdata) throws JposException {
        WriteData[1] = trackdata;
    }

    @Override
    public void write3Data(String trackdata) throws JposException {
        WriteData[2] = trackdata;
    }

    @Override
    public void write4Data(String trackdata) throws JposException {
        WriteData[3] = trackdata;
    }

    @Override
    public void write5Data(String trackdata) throws JposException {
        WriteData[4] = trackdata;
    }

    @Override
    public void write6Data(String trackdata) throws JposException {
        WriteData[5] = trackdata;
    }

    @Override
    public void beginInsertion(int i) throws JposException {

    }

    @Override
    public void beginRemoval(int timeout) throws JposException {

    }

    @Override
    public void cleanCard() throws JposException {

    }

    @Override
    public void clearPrintWrite(int kind, int hposition, int vposition, int width, int height) throws JposException {

    }

    @Override
    public void endInsertion() throws JposException {

    }

    @Override
    public void endRemoval() throws JposException {

    }

    @Override
    public void rotatePrint(int rotation) throws JposException {

    }

    @Override
    public void validateData(String data) throws JposException {

    }

    @Override
    public PrintWrite printWrite(int kind, int hposition, int vposition, String data) throws JposException {
        return new PrintWrite(this, kind, hposition, vposition, data);
    }

    @Override
    public void printWrite(PrintWrite request) throws JposException {

    }

    @Override
    public void validateData(PointCardRWService.EscUnknown printData)  throws JposException {
        throw new JposException(JposConst.JPOS_E_FAILURE, "Unsupported ESCsequence ending with " + (char)printData.getEsc());
    }

    @Override
    public void validateData(PointCardRWService.PrintData printData)  throws JposException {
        for (int i = printData.getPrintData().length() - 1; i >= 0; --i)
            JposDevice.check(printData.getPrintData().charAt(i) < 0x20, JposConst.JPOS_E_FAILURE, "Unsupported control character");
    }

    @Override
    public void validateData(PointCardRWService.EscEmbedded escEmbedded)  throws JposException {

    }

    @Override
    public void validateData(PointCardRWService.EscFontTypeface escFontTypeface)  throws JposException {

    }

    @Override
    public void validateData(PointCardRWService.EscAlignment escAlignment)  throws JposException {

    }

    @Override
    public void validateData(PointCardRWService.EscNormalize escNormalize)  throws JposException {

    }

    @Override
    public void validateData(PointCardRWService.EscSimple escSimple)  throws JposException {
        JposDevice.check(escSimple.getReverse(), JposConst.JPOS_E_ILLEGAL, "Reverse video not supported");
    }

    @Override
    public void validateData(PointCardRWService.EscUnderline escUnderline)  throws JposException {
        int thickness = escUnderline.getThickness();
        JposDevice.check(thickness != 1, JposConst.JPOS_E_ILLEGAL, "Thickness for underline not supported: " + thickness);
    }

    @Override
    public void validateData(PointCardRWService.EscScale escScale)  throws JposException {
        int scale = escScale.getScaleValue();
        JposDevice.check(scale > 2, JposConst.JPOS_E_ILLEGAL, "Scale not supported: " + scale);
    }
}
