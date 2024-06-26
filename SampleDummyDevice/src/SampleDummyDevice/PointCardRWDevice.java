/*
 * Copyright 2023 Martin Conrad
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

package SampleDummyDevice;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.pointcardrw.*;
import jpos.*;
import jpos.config.JposEntry;

import java.util.*;

import static javax.swing.JOptionPane.*;
import static jpos.JposConst.*;
import static jpos.PointCardRWConst.*;
import static net.bplaced.conrad.log4jpos.Level.*;

/**
 * JposDevice based dummy implementation for JavaPOS PointCardRW device service implementation.
 * No real hardware. All read data with dummy values, operator interaction via OptionDialog boxes.<br>
 * Supported configuration values for PointCardRW in jpos.xml can be used to set the corresponding property values:
 * <ul>
 *     <li>CapCardEntranceSensor: Valid values are TRUE and FALSE default is FALSE.</li>
 *     <li>CapCleanCard: Valid values are TRUE and FALSE, default is TRUE.</li>
 *     <li>CapClearPrint: Valid values are TRUE and FALSE, default is TRUE.</li>
 *     <li>CapPrint: Valid values are TRUE and FALSE, default is TRUE.</li>
 * </ul>
 * In addition, the following device specific properties can be set. If different values are specified for multiple
 * instances of PointCardRWDevice, the settings of the instance that had been opened first will be valid for all
 * instances. The other settings will be ignored. The following device specific settings can be configured via jpos.xml:
 * <ul>
 *     <li>LineCount: Height of printable area of the card, valid only if CapPrint is TRUE. Default is 4.</li>
 *     <li>LineLength: Width of printable area of the card, valid only if CapPrint is TRUE. Default is 16.</li>
 *     <li>RemovalTimeout: Maximum time for card removal in milliseconds or FOREVER, default 5000.</li>
 *     <li>StatusToReadReady: Maximum time in milliseconds to become ready after card entrance sensor detected the card.
 *     Only used if CapCardEntranceSensor is TRUE. Default: 500 milliseconds.</li>
 *     <li>WriteDuration: Time in milliseconds the simulator needs for printWrite method. Default: 500 milliseconds.</li>
 * </ul>
 * <b>SPECIAL REMARKS:</b> This sample does not implement any really existing PointCardRW system and shall not be
 * used in any really existing cash register application.
 */
@SuppressWarnings("unused")
public class PointCardRWDevice extends JposDevice implements Runnable {
    /**
     * The device implementation. See parent for further details.
     * @param id  Device ID, not used by implementation.
     */
    protected PointCardRWDevice(String id) {
        super(id);
        pointCardRWInit(1);
        PhysicalDeviceDescription = "Dummy PointCardRW simulator";
        PhysicalDeviceName = "Dummy PointCardRW Simulator";
        CapPowerReporting = JPOS_PR_NONE;
    }

    static private final int PropName = 0;          // Index of property name
    static private final int PropType = 1;          // Index of property type
    static private final int PropValueBase = 2;     // Index of first allowed value as String
    static private final String PropTypeBool = "0"; // Property type Boolean, allowed values true and false
    static private final String PropTypeSym = "1";  // Property type symbol, allowed values static properties of class RFIDScannerConst
    static private final String PropTypeInt = "2";  // Property type Integer, allowed values as specified starting at PropValueBase.

    private Integer StatusToReadReady;
    private Integer LineCount;
    private Integer LineLength;
    private Integer RemovalTimeout;
    private Integer WriteDuration;

    private final String[] InternalProperties = { "StatusToReadReady", "LineCount", "LineLength", "RemovalTimeout", "WriteDuration" };
    private final int[]    InternalDefaults   = {         500,            4,              16,             5000,          500};

    private final String[][] Capabilities = {
            {"CapCardEntranceSensor", PropTypeBool, "TRUE", "FALSE"},
            {"CapCleanCard", PropTypeBool, "TRUE", "FALSE"},
            {"CapClearPrint", PropTypeBool, "TRUE", "FALSE"},
            {"CapPrint", PropTypeBool, "TRUE", "FALSE"},
            {"MaxLines", PropTypeInt, "1", "2", "3", "4", "5", "6", "7", "8"},
            {"LineChars", PropTypeInt, "2", "4", "6", "8", "10", "12", "14", "16", "18", "20", "22", "24", "26", "28", "30", "32"}
    };
    private final Map<String, String[]> LastEntries = new HashMap<>();
    private String LogicalName = null;

    @Override
    public void checkProperties(JposEntry entry) throws JposException {
        super.checkProperties(entry);
        Object o = entry.getPropertyValue("logicalName");
        LogicalName = o.toString();
        for (String[] capability : Capabilities) {
            o = entry.getPropertyValue(capability[PropName]);
            if (o != null) {
                int j;
                for (j = PropValueBase; j < capability.length; j++) {
                    if (capability[j].equals(o.toString().toUpperCase()))
                        break;
                }
                check(j == capability.length, JPOS_E_ILLEGAL, "Invalid value for property " + capability[PropName] + ": " + o.toString());
                LastEntries.put(capability[PropName], new String[]{capability[PropType], o.toString()});
            }
        }
        for (int i = 0; i < InternalProperties.length; i++) {
            try {
                o = entry.getPropertyValue(InternalProperties[i]);
                getClass().getDeclaredField(InternalProperties[i]).set(this, o == null ? InternalDefaults[i] : Integer.parseInt(o.toString()));
            } catch (Exception ignore) {
                log(WARN, LogicalName + ": Cannot set property " + InternalProperties[i]);
            }
        }
    }

    @Override
    public void changeDefaults(PointCardRWProperties p) {
        p.LogicalName = LogicalName;
        SampleProperties props = (PointCardRWDevice.SampleProperties) p;
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "PIN pad service for sample dummy device";
        props.LineChars = LineLength;
        props.MaxLines = LineCount;
        props.CapCleanCard = true;
        props.CapClearPrint = true;
        props.CapPrint = true;
        props.WriteDuration = WriteDuration;
        for (String capa : LastEntries.keySet()) {
            try {
                String[] attr = LastEntries.get(capa);
                Object value;
                if (attr[0].equals(PropTypeBool))
                    value = attr[1].equalsIgnoreCase("TRUE");
                else if (attr[0].equals(PropTypeInt))
                    value = Integer.parseInt(attr[1]);
                else
                    value = PointCardRWConst.class.getField(attr[1]).get(null);
                props.getClass().getField(capa).set(props, value);
            } catch (Exception ignored) {
                props.Device.log(WARN, props.LogicalName + ": Cannot set property " + capa + " to " + LastEntries.get(capa)[1]);
            }
        }
        props.CardState = PCRW_SUE_STATE_NOCARD;
        if (props.CapPrint) {
            props.CharacterSetList = "";
            StringBuilder charlist = new StringBuilder();
            for (long code : CodePages)
                charlist.append(code).append(",");
            charlist.append(props.CharacterSet = PCRW_CS_ASCII);
            props.CharacterSetList = charlist.toString();
            props.LineCharsList = String.valueOf(props.LineChars);
            props.SidewaysMaxChars =
            props.SidewaysMaxLines = 0;
            props.LineHeight = 1;
            props.LineSpacing = 1;
            props.LineWidth = LineLength;
            props.PrintHeight = 1;
       }
        TheCards = new Card[]{
                new Card("Card w. Track 1, 2, 3", PCRW_TRACK1, PCRW_TRACK2, PCRW_TRACK3),
                new Card("Card w. Track 1, 3, 5", PCRW_TRACK1, PCRW_TRACK3, PCRW_TRACK5),
                new Card("Card w. Track 1, 2, 4, 6", PCRW_TRACK1, PCRW_TRACK2, PCRW_TRACK4, PCRW_TRACK6),
                new Card("Card w. Track 1, 2, 3; 2 Def.", PCRW_TRACK1, -PCRW_TRACK2, PCRW_TRACK3),
                new Card("Card w. Track 2, 3, 6; Track Def.", -PCRW_TRACK2, -PCRW_TRACK3, -PCRW_TRACK6),
                new Card("Cleaning Card")
        };
    }

    /*
    Character encoding. Supported are only 7-bit ASCII with country specific characters for some countries,
    conforming with ISO 646.
     */

    // Strings containing valid characters corresponding with country specific character sets. Start with characters
    // for code 20hand up to code 7Eh. Characters with code 0 - 1Fh and 7Fh are control characters and are equal
    // for all code pages.
    static private final String[] EncodedCharacters = {
            " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~",    // US
            " !\"£$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}¯",    // UK
            " !\"#$%&'()*+,-./0123456789:;<=>?§ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÜ^_`abcdefghijklmnopqrstuvwxyzäöüß",     // DE
            " !\"#¤%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÅ^_`abcdefghijklmnopqrstuvwxyzäöå‾",     // FI/SE
            " !\"#$%&'()*+,-./0123456789:;<=>?ÄABCDEFGHIJKLMNOPQRSTUVWXYZÆØÅÜ_äabcdefghijklmnopqrstuvwxyzæøåü",     // DK/NO
            " !\"£$%&'()*+,-./0123456789:;<=>?àABCDEFGHIJKLMNOPQRSTUVWXYZ°ç§^_`abcdefghijklmnopqrstuvwxyzéùè¨",     // FR
            " !\"£$%&'()*+,-./0123456789:;<=>?§ABCDEFGHIJKLMNOPQRSTUVWXYZ°çé^_ùabcdefghijklmnopqrstuvwxyzàòèì",     // IT
            " !\"£$%&'()*+,-./0123456789:;<=>?§ABCDEFGHIJKLMNOPQRSTUVWXYZ¡Ñ¿^_`abcdefghijklmnopqrstuvwxyz°ñç~",     // ES
            " !\"#$%&'()*+,-./0123456789:;<=>?§ABCDEFGHIJKLMNOPQRSTUVWXYZÃÇÕ^_`abcdefghijklmnopqrstuvwxyzãçõ°"      // PT
    };

    // All valid control characters, invalid replaced by DEL (7fh).
    static private final String ValidControlCharacters = "\177\177\177\177\177\177\177\177\177\177\177\177\177\177\177\177\177\177\177\177\177\177\177\177\177\177\177\33\177\177\177\177";

    // Internal code pages. Kepp in mind: 100 (CP_US) equals US-ASCII
    static private final int CP_BASE = 101;
    static private final int CP_US = CP_BASE;
    static private final int CP_UK = CP_BASE + 1;
    static private final int CP_DE = CP_BASE + 2;
    static private final int CP_SE = CP_BASE + 3;
    static private final int CP_DK = CP_BASE + 4;
    static private final int CP_FR = CP_BASE + 5;
    static private final int CP_IT = CP_BASE + 6;
    static private final int CP_ES = CP_BASE + 7;
    static private final int CP_PT = CP_BASE + 8;

    // The only valid control character is ESC (1Ch).
    static private final long[] CodePages = {CP_US,CP_UK,CP_DE,CP_SE,CP_DK,CP_FR,CP_IT,CP_ES,CP_PT,};

    // invalid characters will be converted to DEL (7Fh). Invalid codepage returns empty array.
    static private byte[] toBytes(int codepage, String source, boolean mapping) {
        if (codepage == PCRW_CS_ASCII || !mapping)
            codepage = CP_US;
        if (!member(codepage, CodePages))
            return new byte[0];
        byte[] target = new byte[source.length()];
        for (int i = 0; i < source.length(); i++) {
            int code = source.charAt(i);
            int c = (ValidControlCharacters + EncodedCharacters[codepage - CP_BASE]).indexOf(code);
            target[i] = (byte)(c < 0 ? (0x20 > code ? code : 0x7f) : c);
        }
        return target;
    }

    // Checks if invalid bytes have been passed
    static private boolean allBytesValid(byte[] source) {
        if (source != null) {
            for (byte b : source) {
                if (b == 0x7f)
                    return false;
            }
        }
        return true;
    }

    // Convert byte array back to String. Returns empty string if codepage is invalid
    private static String fromBytes(int codepage, byte[] source) {
        if (codepage == PCRW_CS_ASCII)
            codepage = CP_US;
        if (!member(codepage, CodePages) || source == null)
            return "";
        char[] target = new char[source.length];
        for (int i = 0; i < source.length; i++) {
            target[i] = source[i] == (byte)0x7f ? (char)0x2014 : (ValidControlCharacters + EncodedCharacters[codepage - CP_BASE]).charAt(source[i] & 0xff);
        }
        return new String(target);
    }

    /*
    Helper class representing some cards. Supported are 5 cards:
    Card 1 with tracks 1,2,3
    card 2 with tracks 1, 3, 5
    Card 3 with tracks 1, 2, 4, 6
    card 4 with tracks 1,2,3, track 2 defective
    card 5 completely defective
     */

    private class Card {
        Map<Integer, byte[]> AvailableTracks = new HashMap<>();
        byte[][] PrintArea;

        String Label;
        int Readable = 0;
        int Writable = 0;
        int Defective = 0;
        // Specify supported tracks (PCRW_TRACK1...), defective tracks negative
        Card(String label, int... tracks) {
            Label = label;
            for (int key : tracks) {
                int abskey = Math.abs(key);
                AvailableTracks.put(abskey, key > 0 ? ("Track " + (int)(Math.log(key) / Math.log(2) + 1.5)).getBytes() : null);
                Writable |= abskey;
                if (key != abskey)
                    Defective |= abskey;
            }
            Readable = Writable;
            byte[] line = new byte[LineLength];
            Arrays.fill(line, (byte)' ');
            PrintArea = new byte[LineCount][];
            for (int i = 0; i < LineCount; i++)
                PrintArea[i] = Arrays.copyOf(line, line.length);
        }
    }

    private Card[] TheCards;
    private Card CurrentCard = null;

    private enum State {
        Idle,
        Insertion,
        CardInitializing,
        TrackReading,
        CardPresent,
        Removal,
    };
    private final State[] Status = {State.Idle};
    private SyncObject StatusChangeWaiter = null;
    private final SynchronizedMessageBox TheBox = new SynchronizedMessageBox();
    private Thread Handler = null;
    @Override
    public void run() {
        String title = "Dummy PointCardRW Simulator";
        String[] options;
        StringBuilder message;
        SampleProperties props;
        long insertionstart = 0L;
        while (Thread.currentThread().getName().length() > 0) {
            try {
                switch (Status[0]) {
                    case Idle:
                        TheBox.synchronizedConfirmationBox("Reader / Writer Idle", title, new String[0], null, PLAIN_MESSAGE, JPOS_FOREVER);
                        break;
                    case Insertion: {
                        message = new StringBuilder("Select card for insertion:");
                        StringBuilder opts = new StringBuilder();
                        for (int i = 0; i < TheCards.length; i++) {
                            message.append("\nCard ").append(i + 1).append(": ").append(TheCards[i].Label);
                            opts.append(", Card ").append(i + 1);
                        }
                        options = opts.substring(1).split(",");
                        int res = TheBox.synchronizedConfirmationBox(message.toString(), title, options, options[0], PLAIN_MESSAGE, JPOS_FOREVER);
                        if (res >= 0 && res < TheCards.length) {
                            CurrentCard = TheCards[res];
                            synchronized (Status) {
                                props = (SampleProperties) getClaimingInstance(ClaimedPointCardRW, 0);
                                if (props != null) {
                                    setCardCapabilities(props);
                                }
                                if (props != null && props.CapCardEntranceSensor && props.DeviceEnabled) {
                                    Status[0] = State.CardInitializing;
                                    handleEvent(new PointCardRWStatusUpdateEvent(props.EventSource, PCRW_SUE_REMAINING));
                                } else {
                                    Status[0] = State.CardPresent;
                                }
                                signalStatusChanged();
                            }
                        }
                    }
                    break;
                    case CardInitializing: {
                        int value = (int) (System.currentTimeMillis() - insertionstart);
                        boolean changeState = value >= StatusToReadReady;
                        if (!changeState) {
                            options = new String[]{"OK"};
                            value = TheBox.synchronizedConfirmationBox("Press OK when insertion is ready for " + CurrentCard.Label,
                                    title, options, options[0], PLAIN_MESSAGE, StatusToReadReady - value);
                            changeState = value == 0;
                        }
                        if (changeState) {
                            synchronized (Status) {
                                Status[0] = State.CardPresent;
                                signalStatusChanged();
                            }
                        }
                    }
                    break;
                    case TrackReading: {
                        props = (SampleProperties) getClaimingInstance(ClaimedPointCardRW, 0);
                        if (props != null && props.DeviceEnabled) {
                            if (props.CapCardEntranceSensor)
                                handleEvent(new PointCardRWStatusUpdateEvent(props.EventSource, PCRW_SUE_INRW));
                            if (props.TracksToRead != 0) {
                                if ((props.TracksToRead & CurrentCard.Readable) == props.TracksToRead && (props.TracksToRead & CurrentCard.Defective) == 0)
                                    handleEvent(new PointCardRWDataEvent(props.EventSource, getTracks(props.CharacterSet, props.TracksToRead), getState(props.TracksToRead)));
                                else
                                    handleEvent(new PointCardRWErrorEvent(props.EventSource, JPOS_E_FAILURE, 0, getTracks(props.CharacterSet, props.TracksToRead), getState(props.TracksToRead)));
                            }
                        }
                        synchronized (Status) {
                            Status[0] = State.CardPresent;
                            signalStatusChanged();
                        }
                    }
                    case CardPresent: {
                        props = (SampleProperties) getClaimingInstance(ClaimedPointCardRW, 0);
                        message = new StringBuilder(CurrentCard.Label);
                        for (int i = 0; i < Trackkeys.length; i++) {
                            if (CurrentCard.AvailableTracks.containsKey(Trackkeys[i])) {
                                message.append("\nTrack ").append(i + 1).append(": »").append(fromBytes(CP_US, CurrentCard.AvailableTracks.get(Trackkeys[i]))).append("«");
                            }
                        }
                        if (props != null && props.CapPrint) {
                            message.append("\n\nPrint Area:");
                            for (int i = 0; i < CurrentCard.PrintArea.length; i++) {
                                message.append("\n").append(fromBytes(props.CharacterSet, Arrays.copyOf(CurrentCard.PrintArea[i], props.LineChars)));
                            }
                        }
                        TheBox.synchronizedConfirmationBox(message.toString(), title, new String[0], null, PLAIN_MESSAGE, JPOS_FOREVER);
                    }
                    break;
                    case Removal: {
                        props = (SampleProperties) getClaimingInstance(ClaimedPointCardRW, 0);
                        if (props != null && props.CapCardEntranceSensor)
                            handleEvent(new PointCardRWStatusUpdateEvent(props.EventSource, PCRW_SUE_REMAINING));
                        message = new StringBuilder("Card removal, press OK when finished");
                        TheBox.synchronizedConfirmationBox(message.toString(), title, new String[]{"OK"}, "OK", PLAIN_MESSAGE, RemovalTimeout);
                        synchronized (Status) {
                            Status[0] = State.Idle;
                            props = (SampleProperties) getClaimingInstance(ClaimedPointCardRW, 0);
                            if (props != null)
                                setCardCapabilityDefaults(props);
                            signalStatusChanged();
                        }
                        if (props != null && props.CapCardEntranceSensor)
                            handleEvent(new PointCardRWStatusUpdateEvent(props.EventSource, PCRW_SUE_NOCARD));
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private void setCardCapabilities(SampleProperties props) {
        if (props.CapTracksToRead != CurrentCard.Readable) {
            props.CapTracksToRead = CurrentCard.Readable;
            props.EventSource.logSet("CapTracksToRead");
        }
        if (props.CapTracksToWrite != CurrentCard.Writable) {
            props.CapTracksToWrite = CurrentCard.Writable;
            props.EventSource.logSet("CapTracksToWrite");
        }
        if ((~props.CapTracksToRead & props.TracksToRead) != 0) {
            props.TracksToRead &= props.CapTracksToRead;
            props.EventSource.logSet("TracksToRead");
        }
        if ((~props.CapTracksToWrite & props.TracksToWrite) != 0) {
            props.TracksToWrite &= props.CapTracksToWrite;
            props.EventSource.logSet("TracksToWrite");
        }
    }

    private void setCardCapabilityDefaults(SampleProperties props) {
        if (props.CapTracksToRead != 0) {
            props.CapTracksToRead = 0;
            props.EventSource.logSet("CapTracksToRead");
        }
        if (props.CapTracksToWrite != 0) {
            props.CapTracksToWrite = 0;
            props.EventSource.logSet("CapTracksToWrite");
        }
        if (props.TracksToRead != 0) {
            props.TracksToRead = 0;
            props.EventSource.logSet("TracksToRead");
        }
        if (props.TracksToWrite != 0) {
            props.TracksToWrite  = 0;
            props.EventSource.logSet("TracksToWrite");
        }
    }

    private void signalStatusChanged() {
        if (StatusChangeWaiter != null) {
            StatusChangeWaiter.signal();
            StatusChangeWaiter = null;
        }
    }
    private final int[] Trackkeys = {PCRW_TRACK1, PCRW_TRACK2, PCRW_TRACK3, PCRW_TRACK4, PCRW_TRACK5, PCRW_TRACK6};

    private Integer[] getState(int toread) {
        Integer[] state = new Integer[Trackkeys.length];
        for (int i = 0; i < Trackkeys.length; i++) {
            if ((toread & Trackkeys[i]) != 0) {
                if ((CurrentCard.Defective & Trackkeys[i]) != 0)
                    state[i] = JPOS_EPCRW_LRC;
                else
                    state[i] = JPOS_SUCCESS;
            } else {
                state[i] = JPOS_EPCRW_ENCODE;
            }
        }
        return state;
    }

    private String[] getTracks(int codepage, int toread) {
        String[] tracks = new String[Trackkeys.length];
        for (int i = 0; i < Trackkeys.length; i++) {
            if (CurrentCard.AvailableTracks.containsKey(Trackkeys[i]) && (CurrentCard.Defective & Trackkeys[i]) == 0 && (toread & Trackkeys[i]) != 0) {
                tracks[i] = fromBytes(codepage == PCRW_CS_ASCII ? CP_US : codepage, CurrentCard.AvailableTracks.get(Trackkeys[i]));
            } else {
                tracks[i] = "";
            }
        }
        return tracks;
    }

    @Override
    public PointCardRWProperties getPointCardRWProperties(int id) {
        return new SampleProperties();
    }

    private class SampleProperties extends PointCardRWProperties {
        protected SampleProperties() {
            super(0);
        }

        @Override
        @SuppressWarnings("SynchronizeOnNonFinalField")
        public void open() throws JposException {
            super.open();
            synchronized (Device) {
                if (Handler == null) {
                    Handler = new Thread(PointCardRWDevice.this);
                    Handler.setName("PointCardRWDeviceHandler");
                    Handler.start();
                }
            }
        }

        @Override
        @SuppressWarnings("SynchronizeOnNonFinalField")
        public void close() throws JposException {
            super.close();
            Thread handler = null;
            synchronized (Device) {
                if (PointCardRWs[0].size() == 1 && (handler = Handler) != null) {
                    Handler.setName("");
                    TheBox.abortDialog();
                }
            }
            try {
                if (handler != null)
                    handler.join();
            } catch (InterruptedException ignored) {}
        }

        @Override
        public void deviceEnabled(boolean flag) throws JposException {
            super.deviceEnabled(flag);
            synchronized (Status) {
                if (flag) {
                    if (Status[0] != PointCardRWDevice.State.Idle && Status[0] != PointCardRWDevice.State.Insertion) {
                        setCardCapabilities(this);
                    } else {
                        setCardCapabilityDefaults(this);
                    }
                    Inserting = Removing = false;
                    if (CapCardEntranceSensor) {
                        if (Status[0] == PointCardRWDevice.State.Idle || Status[0] == PointCardRWDevice.State.Insertion) {
                            if (CardState != PCRW_STATE_NOCARD) {
                                CardState = PCRW_STATE_NOCARD;
                                EventSource.logSet("CardState");
                            }
                        } else if (Status[0] == PointCardRWDevice.State.TrackReading || Status[0] == PointCardRWDevice.State.CardPresent) {
                            if (CardState != PCRW_STATE_INRW) {
                                CardState = PCRW_STATE_INRW;
                                EventSource.logSet("CardState");
                            }
                        } else if (CardState != PCRW_STATE_REMAINING) {
                            CardState = PCRW_STATE_REMAINING;
                            EventSource.logSet("CardState");
                        }
                    }
                    if (Status[0] == PointCardRWDevice.State.CardPresent)
                        new PointCardRWErrorEvent(EventSource, JPOS_E_FAILURE, 0, getTracks(CharacterSet, TracksToRead), getState(TracksToRead)).setErrorProperties();
                }
            }
        }

        @Override
        public void lineHeight(int count) throws JposException {
            // ignore: LineHeight cannot be changed.
        }

        @Override
        public void lineSpacing(int count) throws JposException {
            // ignore: LineSpacing cannot be changed.
        }

        private boolean Inserting = false, Removing = false;
        @Override
        public void beginInsertion(int timeout) throws JposException {
            if (timeout == JPOS_FOREVER)
                timeout = Integer.MAX_VALUE;
            check(Status[0] == PointCardRWDevice.State.Removal || Removing, JPOS_E_ILLEGAL, "Card removal active");
            SyncObject waiter = null;
            Inserting = true;
            long starttime = System.currentTimeMillis();
            while (true) {
                synchronized (Status) {
                    if (Status[0] == PointCardRWDevice.State.Idle) {
                        Status[0] = PointCardRWDevice.State.Insertion;
                        waiter = StatusChangeWaiter = new SyncObject();
                        TheBox.abortDialog();
                    } else if (Status[0] == PointCardRWDevice.State.Insertion || Status[0] == PointCardRWDevice.State.CardInitializing)
                        waiter = StatusChangeWaiter = new SyncObject();
                    else
                        break;
                }
                long synctio = timeout - (System.currentTimeMillis() - starttime);
                check(synctio < 0, JPOS_E_TIMEOUT, "Insertion timeout");
                waiter.suspend(timeout);
            }
        }

        @Override
        public void endInsertion() throws JposException {
            check(!Inserting, JPOS_E_ILLEGAL, "Not Inserting");
            synchronized (Status) {
                if (Status[0] != PointCardRWDevice.State.CardPresent) {
                    if (Status[0] != PointCardRWDevice.State.Removal) {
                        Status[0] = PointCardRWDevice.State.Removal;
                        TheBox.abortDialog();
                    }
                    throw new JposException(JPOS_E_FAILURE, "Card not present");
                } else {
                    Inserting = false;
                    Status[0] = PointCardRWDevice.State.TrackReading;
                    TheBox.abortDialog();
                }
            }
        }

        @Override
        public void beginRemoval(int timeout) throws JposException {
            check(Inserting, JPOS_E_ILLEGAL, "Insertion active");
            SyncObject waiter;
            timeout = timeout == JPOS_FOREVER ? Integer.MAX_VALUE : timeout;
            long starttime = System.currentTimeMillis();
            Removing = true;
            while (true) {
                synchronized (Status) {
                    if (Status[0] == PointCardRWDevice.State.CardPresent || Status[0] == PointCardRWDevice.State.TrackReading) {
                        Status[0] = PointCardRWDevice.State.Removal;
                        TheBox.abortDialog();
                    }
                    check(Status[0] != PointCardRWDevice.State.Removal && Status[0] != PointCardRWDevice.State.Idle, JPOS_E_ILLEGAL, "Not in removal");
                    waiter = Status[0] == PointCardRWDevice.State.Removal ? (StatusChangeWaiter = new SyncObject()) : null;
                }
                if (waiter != null) {
                    long tio = timeout - (System.currentTimeMillis() - starttime);
                    check(tio < 0, JPOS_E_TIMEOUT, "Removal timeout");
                    waiter.suspend(tio);
                } else
                    break;
            }
        }

        @Override
        public void endRemoval() throws JposException {
            check(!Removing, JPOS_E_ILLEGAL, "Not Inserting");
            synchronized (Status) {
                check(Status[0] == PointCardRWDevice.State.Removal, JPOS_E_FAILURE, "Card still present");
            }
            Removing = false;
        }

        @Override
        public void cleanCard() throws JposException {
            check(Inserting, JPOS_E_ILLEGAL, "Insertion not completed");
            check(Removing, JPOS_E_ILLEGAL, "Removal not completed");
            synchronized (Status) {
                checkext(Status[0] != PointCardRWDevice.State.CardPresent, JPOS_E_FAILURE, "No cleaning card present");
                check(!CurrentCard.AvailableTracks.isEmpty(), JPOS_E_FAILURE, "No cleaning card present");
                new SyncObject().suspend(500);
            }
        }

        @Override
        public void clearPrintWrite(int kind, int hpos, int vpos, int width, int height) throws JposException {
            check(Inserting, JPOS_E_ILLEGAL, "Insertion not completed");
            synchronized (Status) {
                checkext(Status[0] != PointCardRWDevice.State.CardPresent, JPOS_EPCRW_NOCARD, "No card available");
            }
            JposException ex = null;
            if ((kind & 1) != 0) {
                if (width == -1)
                    width = LineChars - hpos;
                if (height == -1)
                    height = MaxLines - height;
                check(hpos + width > LineChars || vpos + height > MaxLines, JPOS_E_ILLEGAL, "Clear area out of range");
                for (int h = 0; h < height; h++) {
                    for (int w = 0; w < width; w++)
                        CurrentCard.PrintArea[h + vpos][w + hpos] = ' ';
                }
            }
            if ((kind & 2) != 0) {
                for (Integer key : Trackkeys) {
                    if ((key & CurrentCard.Defective) != 0) {
                        if (ex == null)
                            ex = new JposException(JPOS_E_EXTENDED, JPOS_EPCRW_WRITE, "Track could not be written");
                    } else if ((CurrentCard.Readable & key) != 0) {
                        CurrentCard.AvailableTracks.replace(key, null);
                        CurrentCard.Readable &= ~key;
                    }
                }
            }
            TheBox.abortDialog();
            if (ex != null)
                throw ex;
        }

        @Override
        public PrintWrite printWrite(int kind, int hposition, int vposition, String data) throws JposException {
            check(Inserting, JPOS_E_ILLEGAL, "Insertion not completed");
            Card current = CurrentCard;
            checkext(current == null, JPOS_EPCRW_NOCARD, "No card present");
            PrintWrite ret = null;
            if ((kind & 1) != 0) {
                PointCardRWService srv = (PointCardRWService)EventSource;
                List<PointCardRWService.PrintDataPart> parts = srv.outputDataParts(data);
                int count = 0;
                for(PointCardRWService.PrintDataPart part : parts) {
                    if (part instanceof PointCardRWService.PrintData pd) {
                        count += pd.getPrintData().length();
                    }
                }
                checkext(hposition + count >= LineChars || vposition >= MaxLines, JPOS_EPCRW_PRINTER, "Print out of print area");
                ret = new PrintWrite(this, kind, hposition, vposition, parts);
            }
            if ((kind & 2) != 0) {
                check((~current.Writable & TracksToWrite) != 0, JPOS_E_FAILURE, "Bad tracks selected for writing");
                for (int i = 0; i < Trackkeys.length; i++) {
                    if ((TracksToWrite & Trackkeys[i]) != 0)
                        check(WriteData[i].length() == 0, JPOS_E_ILLEGAL, "Write" + (i + 1) + "Date not set");
                }
            }
            return ret == null ? new PrintWrite(this, kind, hposition, vposition, data) : ret;
        }

        int WriteDuration;

        @Override
        public void printWrite(PrintWrite req) throws JposException {
            req.Waiting.suspend(WriteDuration);
            Card current = CurrentCard;
            checkext(current == null, JPOS_EPCRW_NOCARD, "Card removed");
            checkext(req.Abort != null, JPOS_EPCRW_RELEASE, "Command aborted");
            if ((req.getKind() & 1) != 0) {
                StringBuilder data = new StringBuilder();
                PointCardRWService.PrintData lastpart = null;
                for(PointCardRWService.PrintDataPart part : req.getData()) {
                    if (part instanceof PointCardRWService.PrintData)
                        data.append((lastpart = (PointCardRWService.PrintData) part).getPrintData());
                }
                int codepage = lastpart == null ? PCRW_CS_ASCII : lastpart.getCharacterSet();
                boolean mapping = lastpart == null ? MapCharacterSet : lastpart.getServiceIsMapping();
                byte[] bytes = toBytes(codepage, data.toString(), mapping);
                System.arraycopy(bytes, 0, current.PrintArea[req.getVPosition()], req.getHPosition(), bytes.length);
            }
            int error = JPOS_SUCCESS;
            if ((req.getKind() & 2) != 0) {
                for (int i = 0; i < Trackkeys.length; i++) {
                    if ((Trackkeys[i] & req.getTracksToWrite()) != 0) {
                        if ((Trackkeys[i] & current.Defective) != 0) {
                            req.setWriteState(i + 1, JPOS_EPCRW_VERIFY);
                            if (error != JPOS_SUCCESS)
                                error = JPOS_EPCRW_VERIFY;
                        }
                        else {
                            current.AvailableTracks.replace(Trackkeys[i], toBytes(CP_US, req.getWriteTrackData(i + 1), false));
                            if (!allBytesValid(current.AvailableTracks.get(Trackkeys[i]))) {
                                req.setWriteState(i + 1, JPOS_EPCRW_ENCODE);
                                if (error != JPOS_EPCRW_VERIFY)
                                    error = JPOS_EPCRW_ENCODE;
                            }
                            else
                                req.setWriteState(i + 1, JPOS_SUCCESS);
                        }
                    }
                }
            }
            TheBox.abortDialog();
            checkext(error != JPOS_SUCCESS, JPOS_EPCRW_WRITE, "Track write error");
        }

        @Override
        public void validateData(PointCardRWService.PrintData printData)  throws JposException {
            for (int i = printData.getPrintData().length() - 1; i >= 0; --i) {
                int code;
                check((code = printData.getPrintData().charAt(i)) < 0x20, JPOS_E_FAILURE, "Unsupported control character");
                if (!MapCharacterSet) {
                    check(code > 0x7e, JPOS_E_FAILURE, "Unsupported character");
                } else {
                    int cpindex = CharacterSet == PCRW_CS_ASCII ? 0 : CharacterSet - CP_BASE;
                    check(EncodedCharacters[cpindex].indexOf(code) < 0, JPOS_E_FAILURE, "Unsupported character");
                }
            }
        }

        @Override
        public void validateData(PointCardRWService.EscEmbedded escEmbedded)  throws JposException {
            check(escEmbedded.getData().length() > 0, JPOS_E_FAILURE, "Embedded sequence not supported");
        }

        @Override
        public void validateData(PointCardRWService.EscFontTypeface escFontTypeface)  throws JposException {
            check(escFontTypeface.getTypefaceIndex() > 1, JPOS_E_ILLEGAL, "Font not supported");
        }

        @Override
        public void validateData(PointCardRWService.EscAlignment escAlignment)  throws JposException {
            throw new JposException(JPOS_E_ILLEGAL, "Alignment not supported");
        }

        @Override
        public void validateData(PointCardRWService.EscSimple escSimple)  throws JposException {
            check(escSimple.getReverse(), JPOS_E_ILLEGAL, "Reverse video not supported");
            check(escSimple.getBold(), JPOS_E_ILLEGAL, "Bold not supported");
            check(escSimple.getItalic(), JPOS_E_ILLEGAL, "Italic not supported");
        }

        @Override
        public void validateData(PointCardRWService.EscUnderline escUnderline)  throws JposException {
            int thickness = escUnderline.getThickness();
            check(thickness != 0, JPOS_E_ILLEGAL, "Thickness for underline not supported: " + thickness);
        }

        @Override
        public void validateData(PointCardRWService.EscScale escScale)  throws JposException {
            int scale = escScale.getScaleValue();
            check(scale != 1, JPOS_E_ILLEGAL, "Scale not supported: " + scale);
        }
    }
}
