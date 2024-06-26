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
import jpos.services.*;

import java.util.ArrayList;
import java.util.List;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;
import static jpos.PointCardRWConst.*;
import static net.bplaced.conrad.log4jpos.Level.*;

/**
 * PointCardRW service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class PointCardRWService extends JposBase implements PointCardRWService116 {
    /**
     * Instance of a class implementing the PointCardRWInterface for point card reader / writer specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public PointCardRWInterface PointCardRW;

    private final PointCardRWProperties Data;
    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public PointCardRWService(PointCardRWProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public boolean getCapMapCharacterSet() throws JposException {
        checkOpened();
        logGet("CapMapCharacterSet");
        return Data.CapMapCharacterSet;
    }

    @Override
    public boolean getMapCharacterSet() throws JposException {
        checkOpened();
        logGet("MapCharacterSet");
        return Data.MapCharacterSet;
    }


    @Override
    public boolean getCapBold() throws JposException {
        checkOpened();
        logGet("CapBold");
        return Data.CapBold;
    }

    @Override
    public int getCapCardEntranceSensor() throws JposException {
        checkOpened();
        logGet("CapCardEntranceSensor");
        return Data.CapCardEntranceSensor ? JPOS_TRUE : JPOS_FALSE;
    }

    @Override
    public int getCapCharacterSet() throws JposException {
        checkOpened();
        logGet("CapCharacterSet");
        return Data.CapCharacterSet;
    }

    @Override
    public boolean getCapCleanCard() throws JposException {
        checkOpened();
        logGet("CapCleanCard");
        return Data.CapCleanCard;
    }

    @Override
    public boolean getCapClearPrint() throws JposException {
        checkOpened();
        logGet("CapClearPrint");
        return Data.CapClearPrint;
    }

    @Override
    public boolean getCapDhigh() throws JposException {
        checkOpened();
        logGet("CapDhigh");
        return Data.CapDhigh;
    }

    @Override
    public boolean getCapDwide() throws JposException {
        checkOpened();
        logGet("CapDwide");
        return Data.CapDwide;
    }

    @Override
    public boolean getCapDwideDhigh() throws JposException {
        checkOpened();
        logGet("CapDwideDhigh");
        return Data.CapDwideDhigh;
    }

    @Override
    public boolean getCapItalic() throws JposException {
        checkOpened();
        logGet("CapItalic");
        return Data.CapItalic;
    }

    @Override
    public boolean getCapLeft90() throws JposException {
        checkOpened();
        logGet("CapLeft90");
        return Data.CapLeft90;
    }

    @Override
    public boolean getCapPrint() throws JposException {
        checkOpened();
        logGet("CapPrint");
        return Data.CapPrint;
    }

    @Override
    public boolean getCapPrintMode() throws JposException {
        checkOpened();
        logGet("CapPrintMode");
        return Data.CapPrintMode;
    }

    @Override
    public boolean getCapRight90() throws JposException {
        checkOpened();
        logGet("CapRight90");
        return Data.CapRight90;
    }

    @Override
    public boolean getCapRotate180() throws JposException {
        checkOpened();
        logGet("CapRotate180");
        return Data.CapRotate180;
    }

    @Override
    public int getCapTracksToRead() throws JposException {
        checkOpened();
        logGet("CapTracksToRead");
        return Data.CapTracksToRead;
    }

    @Override
    public int getCapTracksToWrite() throws JposException {
        checkOpened();
        logGet("CapTracksToWrite");
        return Data.CapTracksToWrite;
    }

    @Override
    public int getCardState() throws JposException {
        checkOpened();
        logGet("CardState");
        return Data.CardState;
    }

    @Override
    public int getCharacterSet() throws JposException {
        checkEnabled();
        logGet("CharacterSet");
        return Data.CharacterSet;
    }

    @Override
    public String getCharacterSetList() throws JposException {
        checkOpened();
        logGet("CharacterSetList");
        return Data.CharacterSetList;
    }

    @Override
    public String getFontTypeFaceList() throws JposException {
        checkOpened();
        logGet("FontTypeFaceList");
        return Data.FontTypeFaceList;
    }

    @Override
    public int getLineChars() throws JposException {
        checkEnabled();
        logGet("LineChars");
        return Data.LineChars;
    }

    @Override
    public String getLineCharsList() throws JposException {
        checkOpened();
        logGet("LineCharsList");
        return Data.LineCharsList;
    }

    @Override
    public int getLineHeight() throws JposException {
        checkEnabled();
        logGet("LineHeight");
        return Data.LineHeight;
    }

    @Override
    public int getLineSpacing() throws JposException {
        checkEnabled();
        logGet("LineSpacing");
        return Data.LineSpacing;
    }

    @Override
    public int getLineWidth() throws JposException {
        checkEnabled();
        logGet("LineWidth");
        return Data.LineWidth;
    }

    @Override
    public int getMapMode() throws JposException {
        checkEnabled();
        logGet("MapMode");
        return Data.MapMode;
    }

    @Override
    public int getMaxLines() throws JposException {
        checkEnabled();
        logGet("MaxLines");
        return Data.MaxLines;
    }

    @Override
    public int getPrintHeight() throws JposException {
        checkOpened();
        logGet("PrintHeight");
        return Data.PrintHeight;
    }

    @Override
    public int getReadState1() throws JposException {
        checkOpened();
        logGet(Data.ReadState,0, "ReadState%d", 1);
        return Data.ReadState[0];
    }

    @Override
    public int getReadState2() throws JposException {
        checkOpened();
        logGet(Data.ReadState,1, "ReadState%d", 2);
        return Data.ReadState[1];
    }

    @Override
    public int getRecvLength1() throws JposException {
        checkEnabled();
        logGet(Data.RecvLength,0, "RecvLength%d", 1);
        return Data.RecvLength[0];
    }

    @Override
    public int getRecvLength2() throws JposException {
        checkEnabled();
        logGet(Data.RecvLength,1, "RecvLength%d", 2);
        return Data.RecvLength[1];
    }

    @Override
    public int getSidewaysMaxChars() throws JposException {
        checkEnabled();
        logGet("SidewaysMaxChars");
        return Data.SidewaysMaxChars;
    }

    @Override
    public int getSidewaysMaxLines() throws JposException {
        checkEnabled();
        logGet("SidewaysMaxLines");
        return Data.SidewaysMaxLines;
    }

    @Override
    public int getTracksToRead() throws JposException {
        checkEnabled();
        logGet("TracksToRead");
        return Data.TracksToRead;
    }

    @Override
    public int getTracksToWrite() throws JposException {
        checkEnabled();
        logGet("TracksToWrite");
        return Data.TracksToWrite;
    }

    @Override
    public String getTrack1Data() throws JposException {
        checkOpened();
        logGet(Data.TrackData,0, "Track%dData", 1);
        return Data.TrackData[0];
    }

    @Override
    public String getTrack2Data() throws JposException {
        checkOpened();
        logGet(Data.TrackData,1, "Track%dData", 2);
        return Data.TrackData[1];
    }

    @Override
    public String getTrack3Data() throws JposException {
        checkOpened();
        logGet(Data.TrackData,2, "Track%dData", 3);
        return Data.TrackData[2];
    }

    @Override
    public String getTrack4Data() throws JposException {
        checkOpened();
        logGet(Data.TrackData,3, "Track%dData", 4);
        return Data.TrackData[3];
    }

    @Override
    public String getTrack5Data() throws JposException {
        checkOpened();
        logGet(Data.TrackData,4, "Track%dData", 5);
        return Data.TrackData[4];
    }

    @Override
    public String getTrack6Data() throws JposException {
        checkOpened();
        logGet(Data.TrackData,5, "Track%dData", 6);
        return Data.TrackData[5];
    }

    @Override
    public int getWriteState1() throws JposException {
        checkOpened();
        logGet(Data.WriteState,0, "WriteState%d", 1);
        return Data.WriteState[0];
    }

    @Override
    public int getWriteState2() throws JposException {
        checkOpened();
        logGet(Data.WriteState,1, "WriteState%d", 2);
        return Data.WriteState[1];
    }

    @Override
    public String getWrite1Data() throws JposException {
        checkOpened();
        logGet(Data.WriteData,0, "Write%dData", 1);
        return Data.WriteData[0];
    }

    @Override
    public String getWrite2Data() throws JposException {
        checkOpened();
        logGet(Data.WriteData,1, "Write%dData", 2);
        return Data.WriteData[1];
    }

    @Override
    public String getWrite3Data() throws JposException {
        checkOpened();
        logGet(Data.WriteData,2, "Write%dData", 3);
        return Data.WriteData[2];
    }

    @Override
    public String getWrite4Data() throws JposException {
        checkOpened();
        logGet(Data.WriteData,3, "Write%dData", 4);
        return Data.WriteData[3];
    }

    @Override
    public String getWrite5Data() throws JposException {
        checkOpened();
        logGet(Data.WriteData,4, "Write%dData", 5);
        return Data.WriteData[4];
    }

    @Override
    public String getWrite6Data() throws JposException {
        checkOpened();
        logGet(Data.WriteData,5, "Write%dData", 6);
        return Data.WriteData[5];
    }

    @Override
    public void setCharacterSet(int code) throws JposException {
        logPreSet("CharacterSet");
        checkEnabled();
        check(!Data.CapPrint, JPOS_E_ILLEGAL, "Printing not supported");
        long[] allowed;
        try {
            allowed = stringArrayToLongArray(Data.CharacterSetList.split(","));
        } catch (Exception e) {
            throw new JposException(JPOS_E_ILLEGAL, "CharacterSetList invalid");
        }
        checkMember(code, allowed, JPOS_E_ILLEGAL, "Invalid character set: " + code);
        PointCardRW.characterSet(code);
        logSet("CharacterSet");
    }

    @Override
    public void setLineChars(int chars) throws JposException {
        logPreSet("LineChars");
        checkEnabled();
        check(!Data.CapPrint, JPOS_E_ILLEGAL, "Printing not supported");
        long[] allowed;
        try {
            allowed = stringArrayToLongArray(Data.LineCharsList.split(","));
        } catch (Exception e) {
            throw new JposException(JPOS_E_ILLEGAL, "LineCharsList invalid");
        }
        for(long val : allowed) {
            if (chars <= val && chars > 0) {
                PointCardRW.lineChars(chars);
                logSet("LineChars");
                return;
            }
        }
        throw new JposException(JPOS_E_ILLEGAL, "Invalid LineChars: " + chars);
    }

    @Override
    public void setLineHeight(int height) throws JposException {
        logPreSet("LineHeight");
        checkEnabled();
        check(!Data.CapPrint, JPOS_E_ILLEGAL, "Printing not supported");
        check(height <= 0, JPOS_E_ILLEGAL, "Height invalid: " + height);
        PointCardRW.lineHeight(height);
        logSet("LineHeight");
    }

    @Override
    public void setLineSpacing(int spacing) throws JposException {
        logPreSet("LineSpacing");
        checkEnabled();
        check(!Data.CapPrint, JPOS_E_ILLEGAL, "Printing not supported");
        check(spacing < 0, JPOS_E_ILLEGAL, "Spacing invalid: " + spacing);
        PointCardRW.lineSpacing(spacing);
        logSet("LineSpacing");
    }

    @Override
    public void setMapCharacterSet(boolean flag) throws JposException {
        logPreSet("MapCharacterSet");
        checkEnabled();
        check(!Data.CapPrint, JPOS_E_ILLEGAL, "Printing not supported");
        check(!Data.CapMapCharacterSet && flag, JPOS_E_ILLEGAL, "Mapping character set not supported");
        PointCardRW.mapCharacterSet(flag);
        logSet("MapCharacterSet");
    }

    @Override
    public void setMapMode(int mode) throws JposException {
        long[] valid = { PCRW_MM_DOTS, PCRW_MM_TWIPS, PCRW_MM_ENGLISH, PCRW_MM_METRIC };
        logPreSet("MapMode");
        checkEnabled();
        checkMember(mode, valid, JPOS_E_ILLEGAL, "Invalid MapMode value: " + mode);
        PointCardRW.mapMode(mode);
        logSet("MapMode");
    }

    @Override
    public void setTracksToRead(int tracks) throws JposException {
        logPreSet("TracksToRead");
        checkEnabled();
        int invalid = tracks & ~Data.CapTracksToRead;
        check(invalid != 0, JPOS_E_ILLEGAL, "Invalid tracks: " + Integer.toHexString(invalid));
        PointCardRW.tracksToRead(tracks);
        logSet("TracksToRead");
    }

    @Override
    public void setTracksToWrite(int tracks) throws JposException {
        logPreSet("TracksToWrite");
        checkEnabled();
        int invalid = tracks & ~Data.CapTracksToWrite;
        check(invalid != 0, JPOS_E_ILLEGAL, "Invalid tracks: " + Integer.toHexString(invalid));
        PointCardRW.tracksToWrite(tracks);
        logSet("TracksToWrite");
    }

    @Override
    public void setWrite1Data(String trackdata) throws JposException {
        logPreSet("Write1Data");
        checkOpened();
        check((Data.TracksToWrite & PCRW_TRACK1) == 0, JPOS_E_ILLEGAL, "Writing track 1 disabled");
        PointCardRW.write1Data(trackdata);
        logSet(Data.WriteData, 0, "Write%dData", 1);
    }

    @Override
    public void setWrite2Data(String trackdata) throws JposException {
        logPreSet("Write2Data");
        checkOpened();
        check((Data.TracksToWrite & PCRW_TRACK2) == 0, JPOS_E_ILLEGAL, "Writing track 2 disabled");
        PointCardRW.write2Data(trackdata);
        logSet(Data.WriteData, 1, "Write%dData", 2);
    }

    @Override
    public void setWrite3Data(String trackdata) throws JposException {
        logPreSet("Write3Data");
        checkOpened();
        check((Data.TracksToWrite & PCRW_TRACK3) == 0, JPOS_E_ILLEGAL, "Writing track 3 disabled");
        PointCardRW.write3Data(trackdata);
        logSet(Data.WriteData, 2, "Write%dData", 3);
    }

    @Override
    public void setWrite4Data(String trackdata) throws JposException {
        logPreSet("Write4Data");
        checkOpened();
        check((Data.TracksToWrite & PCRW_TRACK4) == 0, JPOS_E_ILLEGAL, "Writing track 4 disabled");
        PointCardRW.write4Data(trackdata);
        logSet(Data.WriteData, 3, "Write%dData", 4);
    }

    @Override
    public void setWrite5Data(String trackdata) throws JposException {
        logPreSet("Write5Data");
        checkOpened();
        check((Data.TracksToWrite & PCRW_TRACK5) == 0, JPOS_E_ILLEGAL, "Writing track 5 disabled");
        PointCardRW.write5Data(trackdata);
        logSet(Data.WriteData, 4, "Write%dData", 5);
    }

    @Override
    public void setWrite6Data(String trackdata) throws JposException {
        logPreSet("Write6Data");
        checkOpened();
        check((Data.TracksToWrite & PCRW_TRACK6) == 0, JPOS_E_ILLEGAL, "Writing track 6 disabled");
        PointCardRW.write6Data(trackdata);
        logSet(Data.WriteData, 5, "Write%dData", 6);
    }

    @Override
    public void beginInsertion(int timeout) throws JposException {
        logPreCall("BeginInsertion", removeOuterArraySpecifier(new Object[]{timeout}, Device.MaxArrayStringElements));
        checkEnabled();
        check(Props.State == JPOS_S_BUSY, JPOS_E_BUSY, "Asynchronous output in progress");
        check(timeout < 1 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        PointCardRW.beginInsertion(timeout);
        logCall("BeginInsertion");
    }

    @Override
    public void beginRemoval(int timeout) throws JposException {
        logPreCall("BeginRemoval", removeOuterArraySpecifier(new Object[]{timeout}, Device.MaxArrayStringElements));
        checkEnabled();
        check(Props.State == JPOS_S_BUSY, JPOS_E_BUSY, "Asynchronous output in progress");
        check(timeout < 1 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        PointCardRW.beginRemoval(timeout);
        logCall("BeginRemoval");
    }

    @Override
    public void cleanCard() throws JposException {
        logPreCall("CleanCard");
        checkEnabled();
        check(!Data.CapCleanCard, JPOS_E_ILLEGAL, "Card cleaning not supported");
        PointCardRW.cleanCard();
        logCall("CleanCard");
    }

    @Override
    public void clearPrintWrite(int kind, int hposition, int vposition, int width, int height) throws JposException {
        logPreCall("ClearPrintWrite", removeOuterArraySpecifier(new Object[]{kind, hposition, vposition, width, height}, Device.MaxArrayStringElements));
        checkEnabled();
        check(Props.State == JPOS_S_BUSY, JPOS_E_BUSY, "Asynchronous output in progress");
        check(kind < 1 || kind > 3, JPOS_E_ILLEGAL, "Invalid kind: " + kind);
        if ((kind & 1) != 0) {
            check(!Data.CapClearPrint, JPOS_E_ILLEGAL, "Clearing print area not supported");
            check(hposition < 0, JPOS_E_ILLEGAL, "Invalid horizontal position: " + hposition);
            check(vposition < 0, JPOS_E_ILLEGAL, "Invalid vertical position: " + vposition);
            check(width < -1, JPOS_E_ILLEGAL, "Invalid width: " + width);
            check(height < -1, JPOS_E_ILLEGAL, "Invalid hheight: " + height);
        }
        PointCardRW.clearPrintWrite(kind, hposition, vposition, width, height);
        logCall("ClearPrintWrite");
    }

    @Override
    public void endInsertion() throws JposException {
        logPreCall("EndInsertion");
        checkEnabled();
        PointCardRW.endInsertion();
        logCall("EndInsertion");
    }

    @Override
    public void endRemoval() throws JposException {
        logPreCall("EndRemoval");
        checkEnabled();
        PointCardRW.endRemoval();
        logCall("EndRemoval");
    }

    @Override
    public void rotatePrint(int rotation) throws JposException {
        logPreCall("RotatePrint", removeOuterArraySpecifier(new Object[]{rotation}, Device.MaxArrayStringElements));
        long[] allowed = { PCRW_RP_NORMAL, PCRW_RP_RIGHT90, PCRW_RP_LEFT90, PCRW_RP_ROTATE180 };
        boolean[] correspondingCapability = { true, Data.CapRight90, Data.CapLeft90, Data.CapRotate180 };
        checkEnabled();
        check(Props.State == JPOS_S_BUSY, JPOS_E_BUSY, "Asynchronous output in progress");
        check(!Data.CapPrint, JPOS_E_ILLEGAL, "Printing not supported");;
        checkMember(rotation, allowed, JPOS_E_ILLEGAL, "Invalid rotation: " + rotation);
        for (int i = 1; i < allowed.length; ++i)
            check(rotation == allowed[i] && !correspondingCapability[i], JPOS_E_ILLEGAL, "Invalid rotation: " + rotation);
        PointCardRW.rotatePrint(rotation);
        logCall("RotatePrint");
    }

    @Override
    public void validateData(String data) throws JposException {
        logPreCall("ValidateData", removeOuterArraySpecifier(new Object[]{data}, Device.MaxArrayStringElements));
        if (data == null)
            data = "";
        checkEnabled();
        check(!Data.CapPrint, JPOS_E_FAILURE, "Printing not supported");
        try {
            PointCardRW.validateData(data);
        } catch (JposException e) {
            if (e.getErrorCode() != 0)
                throw e;
            return;
        }
        plausibilityCheckData(outputDataParts(data));
        logCall("ValidateData");
    }

    @Override
    public void printWrite(int kind, int hposition, int vposition, String data) throws JposException {
        logPreCall("PrintWrite", removeOuterArraySpecifier(new Object[]{kind, hposition, vposition, data}, Device.MaxArrayStringElements));
        checkEnabled();
        check(kind < 1 || kind > 3, JPOS_E_ILLEGAL, "Invalid kind: " + kind);
        if ((kind & 1) != 0) {
            check(!Data.CapPrint, JPOS_E_ILLEGAL, "Printing on card not supported");
            check(hposition < 0, JPOS_E_ILLEGAL, "Invalid horizontal position: " + hposition);
            check(vposition < 0, JPOS_E_ILLEGAL, "Invalid vertical position: " + vposition);
        }
        if ((kind & 2) != 0) {
            check((Data.TracksToWrite & ~Data.CapTracksToWrite) != 0, JPOS_E_ILLEGAL, "Cannot write invalid tracks");
            Object[] tocheck = {
                    PCRW_TRACK1, Data.WriteData[0], 1,
                    PCRW_TRACK2, Data.WriteData[1], 2,
                    PCRW_TRACK3, Data.WriteData[2], 3,
                    PCRW_TRACK4, Data.WriteData[3], 4,
                    PCRW_TRACK5, Data.WriteData[4], 5,
                    PCRW_TRACK6, Data.WriteData[5], 6
            };
            for (int i = 0; i < tocheck.length; i += 3) {
                check(((int)tocheck[i] & Data.TracksToWrite) != 0 && tocheck[i + 1].equals(""), JPOS_E_ILLEGAL, "No data for track " + tocheck[i + 2].toString());
            }
        }
        if (callNowOrLater(PointCardRW.printWrite(kind, hposition, vposition, data)))
            logAsyncCall("PrintWrite");
        else
            logCall("PrintWrite");
    }

    /**
     * Helper class used to control parsed output data.
     */
    public abstract static class PrintDataPart {
        /**
         * Used to perform full validation of the print data. To do this, relevant capabilities will be checked and
         * the corresponding method of the PointCardRWInterface used by the given PointCardRWService will be called.
         * @param srv     PointCardRWService to be used for additional validation.
         * @throws JposException If not precisely supported with ErrorCode E_ILLEGAL, if not supported and no workaround
         *                       is possible, with ErrorCode E_FAILURE.
         */
        abstract void validate(PointCardRWService srv) throws JposException;
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
        void validate(PointCardRWService srv) throws JposException {
            validateData(srv);
        }

        /**
         * Used to perform additional validation of the print data, if print output occurs. To do this,
         * simply the corresponding method of the PointCardRWInterface used by the given PointCardRWService will be called.
         *
         * @param srv PointCardRWService to be used for additional validation.
         * @throws JposException If not precisely supported with ErrorCode E_ILLEGAL, if not supported and no workaround
         *                       is possible, with ErrorCode E_FAILURE.
         */
        void validateData(PointCardRWService srv) throws JposException {
            srv.PointCardRW.validateData(this);
        }
    }

    /**
     * Class describing unknown escape sequence.
     */
    public static class EscUnknown extends PrintDataPart {
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
         * @param present   Initial value for ValuePresent.
         */
        public EscUnknown(int type, int subtype, int value, boolean present) {
            Esc = type;
            Subtype = subtype;
            Value = value;
            ValuePresent = present;
        }

        @Override
        void validate(PointCardRWService srv) throws JposException {
            validateData(srv);
        }

        /**
         * Used to perform additional validation of the print data, if print output occurs. To do this,
         * simply the corresponding method of the PointCardRWInterface used by the given PointCardRWService will be called.
         *
         * @param srv PointCardRWService to be used for additional validation.
         * @throws JposException If not precisely supported with ErrorCode E_ILLEGAL, if not supported and no workaround
         *                       is possible, with ErrorCode E_FAILURE.
         */
        void validateData(PointCardRWService srv) throws JposException {
            srv.PointCardRW.validateData(this);
        }
    }

    /**
     * Class describing embedded escape sequences ESC|[*]#E.
     */
    public static class EscEmbedded extends PrintDataPart {
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
         * @param escdata           If value is a data length, the corresponding data. Otherwise null.
         * @return      An EscEmbedded object, if the sequence is a well-formed embedded sequence, otherwise obj.
         */
        public static PrintDataPart getEscEmbedded(PrintDataPart obj, int type, int subtype, String escdata) {
            if (type == 'E' && subtype == 0 && escdata != null) {
                EscEmbedded esc = new EscEmbedded();
                esc.Data = escdata;
                return esc;
            }
            return obj;
        }

        @Override
        void validate(PointCardRWService srv) throws JposException {
            validateData(srv);
        }

        /**
         * Used to perform additional validation of the print data, if print output occurs. To do this,
         * simply the corresponding method of the PointCardRWInterface used by the given PointCardRWService will be called.
         *
         * @param srv PointCardRWService to be used for additional validation.
         * @throws JposException If not precisely supported with ErrorCode E_ILLEGAL, if not supported and no workaround
         *                       is possible, with ErrorCode E_FAILURE.
         */
        void validateData(PointCardRWService srv) throws JposException {
            srv.PointCardRW.validateData(this);
        }
    }

    /**
     * Class describing font typeface selection escape sequences ESC|#fT.
     */
    public static class EscFontTypeface extends PrintDataPart {
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
         * @param valueispresent    The present flag (see EscUnknown, property ValuePresent).
         * @return      An EscFontTypeface object, if the sequence is a well-formed font typeface sequence, otherwise obj.
         */
        public static PrintDataPart getEscFontTypeface(PrintDataPart obj, int type, int subtype, int value, boolean valueispresent) {
            if (type == 'T' && subtype == 'f' && valueispresent) {
                EscFontTypeface esc = new EscFontTypeface();
                esc.TypefaceIndex = value;
                return esc;
            }
            return obj;
        }

        @Override
        void validate(PointCardRWService srv) throws JposException {
            int fontcount = srv.Data.FontTypeFaceList.split(",").length;
            check (TypefaceIndex > fontcount, JPOS_E_FAILURE, "Invalid font type face: " + TypefaceIndex);
            validateData(srv);
        }

        /**
         * Used to perform additional validation of the print data, if print output occurs. To do this,
         * simply the corresponding method of the PointCardRWInterface used by the given PointCardRWService will be called.
         *
         * @param srv PointCardRWService to be used for additional validation.
         * @throws JposException If not precisely supported with ErrorCode E_ILLEGAL, if not supported and no workaround
         *                       is possible, with ErrorCode E_FAILURE.
         */
        void validateData(PointCardRWService srv) throws JposException {
            srv.PointCardRW.validateData(this);
        }
    }

    /**
     * Class describing alignment escape sequences ESC|xA, where x is one of c or r.
     */
    public static class EscAlignment extends PrintDataPart {
        /**
         * One of the alignment values, BC_LEFT, BC_CENTER or BC_RIGHT.
         * @return Alignment.
         */
        public int getAlignment() {
            return Alignment;
        }
        private int Alignment;

        /**
         * Constant for alignment at the right side.
         */
        public final static int RIGHT = 1;

        /**
         * Constant for centered alignment.
         */
        public final static int CENTERED = 2;

        private EscAlignment() {
        }

        /**
         * Checks whether the specified esc sequence parameters form an alignment sequence. If so, it returns an EscAlignment object.
         * If not, the object given as first parameter will be returned.
         * @param obj               An object containing corresponding sequence parameters or null.
         * @param type              The sequence type (see EscUnknown, property Esc).
         * @param subtype           The subtype (see EscUnknown, property Subtype).
         * @param valueispresent    The present flag (see EscUnknown, property ValuePresent).
         * @return      An EscAlignment object, if the sequence is a well-formed alignment sequence, otherwise obj.
         */
        public static PrintDataPart getEscAlignment(PrintDataPart obj, int type, int subtype, boolean valueispresent) {
            if (type == 'A' && !valueispresent) {
                EscAlignment esc = new EscAlignment();
                if (subtype == 'r')
                    esc.Alignment = RIGHT;
                else if (subtype == 'c')
                    esc.Alignment = CENTERED;
                else
                    return obj;
                return esc;
            }
            return obj;
        }

        @Override
        void validate(PointCardRWService srv) throws JposException {
            validateData(srv);
        }

        /**
         * Used to perform additional validation of the print data. To do this,
         * simply the corresponding method of the PointCardRWInterface used by the given PointCardRWService will be called.
         *
         * @param srv PointCardRWService to be used for additional validation.
         * @throws JposException If not precisely supported with ErrorCode E_ILLEGAL, if not supported and no workaround
         *                       is possible, with ErrorCode E_FAILURE.
         */
        void validateData(PointCardRWService srv) throws JposException {
            srv.PointCardRW.validateData(this);
        }
    }

    /**
     * Class describing normalize escape sequences ESC|N.
     */
    public static class EscNormalize extends PrintDataPart {
        private EscNormalize() {
        }

        /**
         * Checks whether the specified esc sequence parameters form a normalize sequence. If so, it returns an EscNormalize object.
         * If not, the object given as first parameter will be returned.
         * @param obj               An object containing corresponding sequence parameters or null.
         * @param type              The sequence type (see EscUnknown, property Esc).
         * @param subtype           The subtype (see EscUnknown, property Subtype).
         * @param valueispresent    The present flag (see EscUnknown, property ValuePresent).
         * @return      An EscNormalize object, if the sequence is a well-formed normalize sequence, otherwise obj.
         */
        public static PrintDataPart getEscNormalize(PrintDataPart obj, int type, int subtype, boolean valueispresent) {
            if (type == 'N' && subtype == 0 && !valueispresent)
                return new EscNormalize();
            return obj;
        }

        @Override
        void validate(PointCardRWService srv) throws JposException {
            validateData(srv);
        }

        /**
         * Used to perform additional validation of the print data, if print output occurs. To do this,
         * simply the corresponding method of the PointCardRWInterface used by the given PointCardRWService will be called.
         *
         * @param srv PointCardRWService to be used for additional validation.
         * @throws JposException If not precisely supported with ErrorCode E_ILLEGAL, if not supported and no workaround
         *                       is possible, with ErrorCode E_FAILURE.
         */
        void validateData(PointCardRWService srv) throws JposException {
            srv.PointCardRW.validateData(this);
        }
    }

    /**
     * Class describing simple attribute setting escape sequences ESC|xC, where x is one of b, i or rv.
     */
    public static class EscSimple extends PrintDataPart {
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

        private EscSimple() {
        }

        /**
         * Checks whether the specified esc sequence parameters form a simple attribute sequence. If so, it returns an EscSimple object.
         * If not, the object given as first parameter will be returned. Simple attributes are bolt, italic, reverse,
         * subscript and superscript.
         * @param obj               An object containing corresponding sequence parameters or null.
         * @param type              The sequence type (see EscUnknown, property Esc).
         * @param subtype           The subtype (see EscUnknown, property Subtype).
         * @param valueispresent    The present flag (see EscUnknown, property ValuePresent).
         * @return      An EscSimple object, if the sequence is a well-formed simple attribute sequence, otherwise obj.
         */
        public static PrintDataPart getEscSimple(PrintDataPart obj, int type, int subtype, boolean valueispresent) {
            if (type == 'C' && !valueispresent) {
                EscSimple esc = new EscSimple();
                esc.Bold = subtype == 'b';
                esc.Italic = subtype == 'i';
                esc.Reverse = subtype == ('r' * 1000) + 'v';
                if (esc.Bold || esc.Italic || esc.Reverse)
                    return esc;
            }
            return obj;
        }

        @Override
        void validate(PointCardRWService srv) throws JposException {
            check(Italic && !srv.Data.CapItalic, JPOS_E_FAILURE, "Italic printing not supported");
            check(Bold  && !srv.Data.CapBold, JPOS_E_FAILURE, "Bold printing not supported");
            validateData(srv);
        }

        /**
         * Used to perform additional validation of the print data, if print output occurs. To do this,
         * simply the corresponding method of the PointCardRWInterface used by the given PointCardRWService will be called.
         *
         * @param srv PointCardRWService to be used for additional validation.
         * @throws JposException If not precisely supported with ErrorCode E_ILLEGAL, if not supported and no workaround
         *                       is possible, with ErrorCode E_FAILURE.
         */
        void validateData(PointCardRWService srv) throws JposException {
            srv.PointCardRW.validateData(this);
        }
    }

    /**
     * Class describing line escape sequences ESC|[!][#]xC, where x is one of u or st.
     */
    public static class EscUnderline extends PrintDataPart {
        /**
         * Thickness of line in dots. -1 means a service specific default thickness, 0 switches line mode off.
         * @return Line thickness.
         */
        public int getThickness() {
            return Thickness;
        }
        private int Thickness;

        private EscUnderline() {
        }

        /**
         * Checks whether the specified esc sequence parameters form a line attribute sequence. If so, it returns an EscLine object.
         * If not, the object given as first parameter will be returned. Line attributes are underline and strike-through.
         * @param obj               An object containing corresponding sequence parameters or null.
         * @param type              The sequence type (see EscUnknown, property Esc).
         * @param subtype           The subtype (see EscUnknown, property Subtype).
         * @param value             The value (see EscUnknown, property Value).
         * @param valueispresent    The present flag (see EscUnknown, property ValuePresent).
         * @return      An EscLine object, if the sequence is a well-formed line attribute sequence, otherwise obj.
         */
        public static PrintDataPart getEscUnderline(PrintDataPart obj, int type, int subtype, int value, boolean valueispresent) {
            if (type == 'C' && subtype == 'u') {
                EscUnderline esc = new EscUnderline();
                esc.Thickness = valueispresent ? value : -1;
                return esc;
            }
            return obj;
        }

        @Override
        void validate(PointCardRWService srv) throws JposException {
            validateData(srv);
        }

        /**
         * Used to perform additional validation of the print data, if print output occurs. To do this,
         * simply the corresponding method of the PointCardRWInterface used by the given PointCardRWService will be called.
         *
         * @param srv PointCardRWService to be used for additional validation.
         * @throws JposException If not precisely supported with ErrorCode E_ILLEGAL, if not supported and no workaround
         *                       is possible, with ErrorCode E_FAILURE.
         */
        void validateData(PointCardRWService srv) throws JposException {
            srv.PointCardRW.validateData(this);
        }
    }

    /**
     * Class describing scaling escape sequences ESC|[#][x]C, where x is one of h or v.
     */
    public static class EscScale extends PrintDataPart {
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
         * @param valueispresent    The present flag (see EscUnknown, property ValuePresent).
         * @return      An EscScale object, if the sequence is a well-formed scaling sequence, otherwise obj.
         */
        public static PrintDataPart getEscScale(PrintDataPart obj, int type, int subtype, int value, boolean valueispresent) {
            if (type == 'C' && valueispresent) {
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
        void validate(PointCardRWService srv) throws JposException {
            check(ScaleValue >= 2 && ScaleVertical && ScaleHorizontal && !srv.Data.CapDwideDhigh, JPOS_E_FAILURE, "Double size printing not supported");
            check(ScaleValue >= 2 && ScaleVertical && !srv.Data.CapDhigh, JPOS_E_FAILURE, "Double high printing not supported");
            check(ScaleValue >= 2 && ScaleHorizontal && !srv.Data.CapDwide, JPOS_E_FAILURE, "Double wide printing not supported");
            validateData(srv);
        }

        /**
         * Used to perform additional validation of the print data, if print output occurs. To do this,
         * simply the corresponding method of the PointCardRWInterface used by the given PointCardRWService will be called.
         *
         * @param srv PointCardRWService to be used for additional validation.
         * @throws JposException If not precisely supported with ErrorCode E_ILLEGAL, if not supported and no workaround
         *                       is possible, with ErrorCode E_FAILURE.
         */
        void validateData(PointCardRWService srv) throws JposException {
            srv.PointCardRW.validateData(this);
        }
    }

    /*
     * Print data processing
     */

    /**
     * Parses output data for escape sequences. See UPOS specification for PointCardRW,
     * chapter <i>Data Characters and Escape Sequences</i>. Returns list of objects that describe all parts of the output
     * string. These objects can be used by validate and print functions to check print data and to generate generic output
     * data.
     * Possible objects in list have one of the following types:
     * <ul>
     *     <li>PrintData       -   Class containing character strings with printable characters only. See PrintData for details.</li>
     *     <li>EscNormalize    -   Class containing information about details of a normalize command. See EscNormalize for details.</li>
     *     <li>EscEmbedded     -   Class containing information about details of a embedded data command. See EscEmbedded for details.</li>
     *     <li>EscFontTypeface -   Class containing information about details of a font typeface command. See EscFontTypeface for details.</li>
     *     <li>EscAlignment    -   Class containing information about details of an alignment command. See EscAlignment for details.</li>
     *     <li>EscScale        -   Class containing information about details of a scale command. See EscScale for details.</li>
     *     <li>EscSimple       -   Class containing information about details of a simple attribute command. See EscSimple for details.</li>
     *     <li>EscLine         -   Class containing information about details of an added line command. See EscLine for details.</li>
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
                boolean valueispresent = false;
                for (index = 0; (temp = data.charAt(index) - '0') >= 0 && temp <= 9; index++) {
                    value = value * 10 + temp;
                    valueispresent = true;
                }
                data = data.substring(index);
                int subtype = 0;
                for (index = 0; (temp = data.charAt(index)) >= 'a' && temp <= 'z'; index++) {
                    subtype = subtype * 1000 + temp;
                }
                String escdata = getEscData(data, index, value, temp);
                data = data.substring(index + escdata.length() + 1);
                out.add(getEscObj(temp, subtype, value, escdata, valueispresent));
            }
            if (data.length() > 0)
                outputPrintableParts(data, out);
        } catch (IndexOutOfBoundsException e) {
            out.add(new EscUnknown(0, 0, 0, false));
        }
        return out;
    }

    private void outputPrintableParts(String data, List<PrintDataPart> out) {
        if (data.length() > 0)
            out.add(new PrintData(data, Data.MapCharacterSet, Data.CharacterSet));
    }

    private String getEscData(String data, int index, int value, int temp) {
        String escdata;
        if ("E".indexOf(temp) == 0) {
            escdata = data.substring(index + 1, value + index + 1);
        }
        else {
            escdata = null;
        }
        return escdata;
    }

    private PrintDataPart getEscObj(int temp, int subtype, int value, String escdata, boolean valueispresent) {
        PrintDataPart ret;
        boolean notnull = ((ret = EscNormalize.getEscNormalize(null, temp, subtype, valueispresent)) != null ||
                (ret = EscEmbedded.getEscEmbedded(null, temp, subtype, escdata)) != null ||
                (ret = EscFontTypeface.getEscFontTypeface(null, temp, subtype, value, valueispresent)) != null ||
                (ret = EscAlignment.getEscAlignment(null, temp, subtype, valueispresent)) != null ||
                (ret = EscScale.getEscScale(null, temp, subtype, value, valueispresent)) != null ||
                (ret = EscSimple.getEscSimple(null, temp, subtype, valueispresent)) != null ||
                (ret = EscUnderline.getEscUnderline(null, temp, subtype, value, valueispresent)) != null);
        return notnull ? ret : new EscUnknown(temp, subtype, value, valueispresent);
    }

    /*
     * Extended validation handling.
     */

    /**
     * Checks whether the given string holds data that cannot be printed precisely on slip as expected. See UPOS
     * method ValidateData for details.<br>
     * this method checks only the general ability for those features that can be checked via capabilitys or other
     * property values. More detailed checks must be performed by device specific service implementations.
     *
     * @param data Data to be checked. See UPOS method ValidateData for more details.
     * @throws JposException See UPOS specification of method ValidateData. Error code can be E_ILLEGAL or E_FAILURE.
     */
    public void plausibilityCheckData(List<PrintDataPart> data) throws JposException {
        check(!Data.CapPrint, JPOS_E_FAILURE, "Printing not supported");
        for (PrintDataPart obj : data) {
            obj.validate(this);
        }
    }

    /**
     * Generates logging message for internal property array representing a range of UPOS properties.
     * @param obj      The array that contains the property range.
     * @param index    The index of the array to be logged.
     * @param mask     The UPOS property name mask, e.g for property Track1 ... Track3 this can be Track%d.
     * @param modifier The value to be formatted. An example: For mask = Track%d and modifier 2, the resulting property
     *                 name would be Track2.
     */
    public void logGet(Object[] obj, int index, String mask, int modifier) {
        Device.log(DEBUG, Props.LogicalName + ": " + String.format(mask, modifier) + ": " + obj[index].toString());
    }

    /**
     * Generates logging message for internal property array representing a range of UPOS properties.
     * @param obj      The array that contains the property range.
     * @param index    The index of the array to be logged.
     * @param mask     The UPOS property name mask, e.g for property Track1 ... Track3 this can be Track%d.
     * @param modifier The value to be formatted. An example: For mask = Track%d and modifier 2, the resulting property
     *                 name would be Track2.
     */
    public void logSet(Object[] obj, int index, String mask, int modifier) {
        Device.log(INFO, Props.LogicalName + ": " + String.format(mask, modifier) + " <- " + obj[index].toString());
    }
}
