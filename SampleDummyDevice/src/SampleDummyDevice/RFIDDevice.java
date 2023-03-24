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
import de.gmxhome.conrad.jpos.jpos_base.rfidscanner.*;
import jpos.*;
import jpos.config.JposEntry;

import javax.swing.*;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * JposDevice based dummy implementation for JavaPOS RFIDScanner device service implementation.
 * No real hardware. All read data with dummy values, operator interaction via OptionDialog boxes.<br>
 * Supported configuration values for SmartCardRW in jpos.xml can be used to set the corresponding property values:
 * <ul>
 *     <li>CapContinuousRead: Can be TRUE or FALSE. Default is FALSE.</li>
 *     <li>CapDisableTag: Can be TRUE or FALSE. Default is FALSE.</li>
 *     <li>CapLockTag: Can be TRUE or FALSE. Default is FALSE.</li>
 *     <li>CapMultipleProtocols: Can be set to any hexadecimal value between 0 and 7FFFFFFF.</li>
 *     <li>CapReadTimer: Can be TRUE or FALSE. Default is FALSE.</li>
 *     <li>CapWriteTag: Can be set to "RFID_CWT_NONE", "RFID_CWT_ID", "RFID_CWT_USERDATA" or "RFID_CWT_ALL".</li>
 *     <li>RFIDScannerFileName: Name of the file that consists of RFID label specifications as specified below.</li>
 * </ul>
 * This simulator uses a file to receive the tags to be read. One line per read, at the end restart at line one. Line
 * separator can be CR, LF, CR LF or LF CR.
 * <br>Each line consists of pairs of hexadecimal values, where the first value of each pair represent a tag ID and the
 * following value the corresponding userdata. Values are separated by spaces. An example:
 * <br>00000001 123456789abcdef0 0000002 4142434445464748
 * <br>00000002 3031323334353637 0000011 6162636465666768 00000055 5555555500000000
 * <br>A file containing the above values would have the following effect:
 * <ul>
 *     <li>The simulator will read two different labels, one with 2 IDs and one with 3 IDs.</li>
 *     <li>The length of each tagID is 4 byte, data length has a maximum of 8 byte per tag.</li>
 *     <li>The first tagID if the first label is byte[]{ 0,0,0,1}, the userdata byte[]{0x12, 0x34,0x56,0x78,0x9A,0xBC,0xDE,0xF0},</li>
 *     <li>The last tagID of the last label is byte[]{0,0,0,'U'}, the userdata byte[]{'U','U','U','U',0,0,0,0}.</li>
 *     <li>After the second read, the simulator reads the first label again.</li>
 * </ul>
 */
public class RFIDDevice extends JposDevice implements Runnable {
    private ArrayList<byte[][][]> Labels = new ArrayList<>();
    private int LabelIndex = 0;
    static final private int IDIdx = 0;             // Index of ID
    static final private int DataIdx = 1;           // Index of UserData
    static final private int ProtoIdx = 2;          // Index of Protocol
    static final private int TagLocked = 0x40;      // Flag for locked (unwritable) tag
    static final private int TagDisabled = 0x80;    // Flag for disabled (unaccessible) tag
    static final private int TagProtoMask = 0x1f;   // Mask for protocol bit number

    protected RFIDDevice(String id) throws JposException {
        super(id);
        rFIDScannerInit(1);
        PhysicalDeviceDescription = "Dummy RFIDScanner simulator";
        PhysicalDeviceName = "Dummy RFIDScanner Simulator";
        CapPowerReporting = JposConst.JPOS_PR_NONE;
        try (RandomAccessFile file = new RandomAccessFile(id, "r")) {
            byte[] data = new byte[(int)file.length()];
            file.readFully(data);
            String[] document = new String(data).replace("\r", "\n").replace("\n\n", "\n").split("\n");
            for (String line : document) {
                String[] values = line.split(" ");
                check((values.length & 1) != 0, JposConst.JPOS_E_FAILURE, "Invalid read label: " + line);
                byte[][][] label = new byte[values.length / 2][][];
                for (int i = 0; i < values.length; i += 2) {
                    label[i >> 1] = new byte[3][];
                    label[i >> 1][IDIdx] = hexStringToByteArray(values[i]);
                    label[i >> 1][DataIdx] = hexStringToByteArray(values[i + 1]);
                }
                Labels.add(label);
            }

        } catch (IOException e) {
            throw new JposException(JposConst.JPOS_E_FAILURE, e.getMessage(), e);
        }
        check(Labels.size() == 0, JposConst.JPOS_E_ILLEGAL, "No label in " + id);
    }

    private byte[] hexStringToByteArray(String s) throws JposException {
        String data = s.toUpperCase();
        byte[] result = new byte[data.length()];
        int i, j = 0;
        for (i = 0; i < result.length && j < data.length(); i++) {
            byte b = 0;
            for (int k = 0; k < 2 && j < data.length(); k++, j++) {
                int c = data.charAt(j);
                byte v = (byte) "0123456789ABCDEF".indexOf(c);
                if (v < 0) {
                    throw new JposException(JposConst.JPOS_E_FAILURE, "No hexadecimal value: " + data);
                }
                b = (byte) (b * 0x10 + v);
            }
            result[i] = b;
        }
        return Arrays.copyOf(result, i);
    }

    private String[][] Capabilities = {
            {"CapContinuousRead", PropTypeBool, "TRUE", "FALSE"},
            {"CapDisableTag", PropTypeBool, "TRUE", "FALSE"},
            {"CapLockTag", PropTypeBool, "TRUE", "FALSE"},
            {"CapReadTimer", PropTypeBool, "TRUE", "FALSE"},
            {"CapWriteTag", PropTypeSym, "RFID_CWT_NONE", "RFID_CWT_ID", "RFID_CWT_USERDATA", "RFID_CWT_ALL"},
            {"CapMultipleProtocols", PropTypeHex}
    };
    static private final int PropName = 0;          // Index of property name
    static private final int PropType = 1;          // Index of property type
    static private final int PropValueBase = 2;     // Index of first allowed value as String
    static private final String PropTypeBool = "0"; // Property type Boolean, allowed values true and false
    static private final String PropTypeSym = "1";  // Property type symbol, allowed values static properties of class RFIDScannerConst
    static private final String PropTypeHex = "2";  // Property type hexadecimal, allowed any hexadecimal integer value

    private Map<String, String[]> LastEntries = new HashMap<>();

    @Override
    public void checkProperties(JposEntry entry) throws JposException {
        super.checkProperties(entry);
        Object o = null;
        int val;
        for (String[] capability : Capabilities) {
            o = entry.getPropertyValue(capability[PropName]);
            if (o != null) {
                if (capability[PropType].equals(PropTypeHex)) {
                    try {
                        long lval = Long.parseLong(o.toString(), 16);
                        check(lval < 0 || lval > Integer.MAX_VALUE, JposConst.JPOS_E_ILLEGAL, "Invalid value for property " + capability[PropName] + ": " + o.toString());
                        o = String.valueOf((int) lval);
                    } catch (NumberFormatException e) {
                        throw new JposException(JposConst.JPOS_E_NOSERVICE, "Invalid property: " + capability[PropName] + " - " + o.toString() + ": " + e.getMessage(), e);
                    }
                } else {
                    int j;
                    for (j = PropValueBase; j < capability.length; j++) {
                        if (capability[j].equals(o.toString().toUpperCase()))
                            break;
                    }
                    check(j == capability.length, JposConst.JPOS_E_ILLEGAL, "Invalid value for property " + capability[PropName] + ": " + o.toString());
                }
                LastEntries.put(capability[PropName], new String[]{ capability[PropType], o.toString()});
            }
        }
    }

    @Override
    public void changeDefaults(RFIDScannerProperties props) {
        for (String capa : LastEntries.keySet()) {
            try {
                String[] attr = LastEntries.get(capa);
                Object value = null;
                if (attr[0].equals(PropTypeBool))
                    value = attr[1].toUpperCase().equals("TRUE");
                else if (attr[0].equals(PropTypeHex))
                    value = (int) Long.parseLong(attr[1]);
                else
                    value = RFIDScannerConst.class.getField(attr[1]).get(null);
                props.getClass().getField(capa).set(props, value);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public RFIDScannerProperties getRFIDScannerProperties(int index) {
        return new SampleProperties();
    }

    private Thread TheRunner;
    private enum Status { idle, gotTags, errorTags };
    private enum Operation { read, lock, disable, writeId, writeData, noop };
    private RFIDDevice.Status ReaderState = RFIDDevice.Status.idle;
    private boolean ToBeFinished = false;
    private SynchronizedMessageBox TheBox = null;
    private int Cmd = 0, Start = 0, Length = Integer.MAX_VALUE;
    private byte[] FilterID = null, Filtermask = null;
    private SyncObject DataEventWaiter = null;
    private Object[] OperationOK = null;    // must be array with length 3
    private Operation CurrentOp = Operation.noop;
    private long LastReadEventTime;
    // The selected status changes
    static final private int IdleToFailed = 0;
    static final private int IdleToPreasent = 1;
    static final private int PresentToIdleOK = 0;
    static final private int PresentToPresent = 1;
    static final private int PresentToFailed = 2;
    static final private int PresentToIdleNOK = 3;
    static final private int FailedToIdle = 0;
    static final private int FailedToPresent = 1;
    static final private int FailedToFailed = 2;

    @Override
    public void run() {
        String title = "Dummy RFIDScanner Simulator";
        String message;
        String[] options;
        TheBox = new SynchronizedMessageBox();
        while (RFIDScanners[0].size() > 0 && !ToBeFinished) {
            switch (ReaderState) {
                case idle:
                    message = "Present an RFID label for operation. Select an option when ready";
                    options = new String[]{"Label Failed", "Label Present"};
                    if (TheBox.synchronizedConfirmationBox(message, title, options, options[1], JOptionPane.INFORMATION_MESSAGE, JposConst.JPOS_FOREVER) < 0)
                        continue;
                    ReaderState = handleReadResult();
                    break;
                case gotTags:
                    message = "Got Label: " + toString(Labels.get(LabelIndex)) + "Select option for the label";
                    options = new String[]{"OK, Finish", "OK, Continue", "Error, Retry", "Error, Give Up"};
                    if (TheBox.synchronizedConfirmationBox(message, title, options, options[0], JOptionPane.INFORMATION_MESSAGE, JposConst.JPOS_FOREVER) < 0)
                        continue;
                    if ((ReaderState = handleLabelResult()) == Status.idle)
                        LabelIndex = LabelIndex < Labels.size() - 1 ? LabelIndex + 1 : 0;
                    break;
                case errorTags:
                    message = "Label present but not readable. Select option";
                    options = new String[]{"Finish", "Retry OK", "Retry Failed"};
                    if (TheBox.synchronizedConfirmationBox(message, title, options, options[0], JOptionPane.INFORMATION_MESSAGE, JposConst.JPOS_FOREVER) < 0)
                        continue;
                    ReaderState = handleErrorResult();
                    break;
            }
        }
    }

    private String toString(byte[][][] label) {
        String result = "\n";
        for (int i = 0; i < label.length; i++) {
            if ((label[i][ProtoIdx][0] & TagDisabled) == 0) {
                String line = "ID: ";
                for (byte c : label[i][IDIdx])
                    line += String.format("%02X", c & 0xff);
                line += ", Data: ";
                for (byte c : label[i][DataIdx])
                    line += String.format("%02X", c & 0xff);
                if (line.length() > 200)
                    line = line.substring(0, 200) + "...";
                result += line + "\n";
            }
        }
        return result;
    }

    private Status handleReadResult() {
        Status ret = TheBox.Result == IdleToPreasent ? Status.gotTags : Status.errorTags;
        synchronized (TheRunner) {
            RFIDScannerProperties props = ClaimedRFIDScanner[0];
            switch (CurrentOp) {
                case read:
                    finishRead(TheBox.Result != IdleToFailed);
            }
        }
        return ret;
    }

    private Status handleErrorResult() {
        Status ret = TheBox.Result == FailedToIdle ? Status.idle : (TheBox.Result == FailedToFailed ? Status.errorTags : Status.gotTags);
        synchronized (TheRunner) {
            RFIDScannerProperties props = ClaimedRFIDScanner[0];
            switch (CurrentOp) {
                case read:
                    finishRead(TheBox.Result == FailedToPresent);
            }
        }
        return ret;
    }

    private Status handleLabelResult() {
        Status ret = (TheBox.Result == PresentToIdleOK || TheBox.Result == PresentToIdleNOK) ? Status.idle : (TheBox.Result == PresentToPresent ? Status.gotTags : Status.errorTags);
        synchronized (TheRunner) {
            RFIDScannerProperties props = ClaimedRFIDScanner[0];
            switch (CurrentOp) {
                case lock:
                    finishLock();
                    break;
                case disable:
                    finishDisable();
                    break;
                case writeData:
                    finishWrite();
                    break;
                case writeId:
                    finishID();
            }
        }
        return ret;
    }

    private void finishID() {
        if (TheBox.Result < PresentToFailed) {
            byte[][][] data = Labels.get(LabelIndex);
            OperationOK[0] = JposConst.JPOS_E_NOEXIST;
            OperationOK[1] = "No matching source ID";
            for (byte[][]tag : data) {
                if (Arrays.equals(tag[IDIdx], FilterID)) {
                    if ((tag[ProtoIdx][0] & ~TagProtoMask) == 0){
                        tag[IDIdx] = Filtermask;
                        OperationOK[0] = null;
                    } else {
                        OperationOK[0] = JposConst.JPOS_E_ILLEGAL;
                        OperationOK[1] = "Source ID " + ((tag[ProtoIdx][0] & TagLocked) != 0 ? "locked" : "disabled");
                    }
                    break;
                }
            }
        } else {
            OperationOK[0] = JposConst.JPOS_E_FAILURE;
            OperationOK[1] = "Source ID change error";
        }
        CurrentOp = Operation.noop;
        DataEventWaiter.signal();
        DataEventWaiter = null;
    }

    private void finishWrite() {
        if (TheBox.Result < PresentToFailed) {
            byte[][][] data = Labels.get(LabelIndex);
            OperationOK[0] = JposConst.JPOS_E_NOEXIST;
            OperationOK[1] = "No matching tag ID";
            for (byte[][]tag : data) {
                if (Arrays.equals(tag[IDIdx], FilterID)) {
                    if ((tag[ProtoIdx][0] & ~TagProtoMask) == 0){
                        if (Start + Filtermask.length > tag[DataIdx].length)
                            tag[DataIdx] = Arrays.copyOf(tag[DataIdx], Start + Filtermask.length);
                        System.arraycopy(Filtermask, 0, tag[DataIdx], Start, Filtermask.length);
                        OperationOK[0] = null;
                    } else {
                        OperationOK[0] = JposConst.JPOS_E_ILLEGAL;
                        OperationOK[1] = "Tag ID " + ((tag[ProtoIdx][0] & TagLocked) != 0 ? "locked" : "disabled");
                    }
                    break;
                }
            }
        } else {
            OperationOK[0] = JposConst.JPOS_E_FAILURE;
            OperationOK[1] = "Tag data write error";
        }
        CurrentOp = Operation.noop;
        DataEventWaiter.signal();
        DataEventWaiter = null;
    }

    private void finishDisable() {
        if (TheBox.Result < PresentToFailed) {
            byte[][][] data = Labels.get(LabelIndex);
            OperationOK[0] = JposConst.JPOS_E_NOEXIST;
            OperationOK[1] = "No matching tag ID";
            for (byte[][]tag : data) {
                if (Arrays.equals(tag[IDIdx], FilterID)) {
                    if ((tag[ProtoIdx][0] & ~TagProtoMask) == 0) {
                        tag[ProtoIdx][0] |= TagDisabled;
                        OperationOK[0] = null;
                    } else {
                        OperationOK[1] = JposConst.JPOS_E_ILLEGAL;
                        OperationOK[1] = "Tag ID " + ((tag[ProtoIdx][0] & TagLocked) != 0 ? "locked" : "disabled");
                    }
                    break;
                }
            }
        } else {
            OperationOK[0] = JposConst.JPOS_E_FAILURE;
            OperationOK[1] = "Tag access error";
        }
        CurrentOp = Operation.noop;
        DataEventWaiter.signal();
        DataEventWaiter = null;
    }

    private void finishLock() {
        if (TheBox.Result < PresentToFailed) {
            byte[][][] data = Labels.get(LabelIndex);
            OperationOK[0] = JposConst.JPOS_E_NOEXIST;
            OperationOK[1] = "No matching tag ID";
            for (byte[][]tag : data) {
                if (Arrays.equals(tag[IDIdx], FilterID)) {
                    if ((tag[ProtoIdx][0] & ~TagProtoMask) == 0) {
                        tag[ProtoIdx][0] |= TagLocked;
                        OperationOK[0] = null;
                    } else {
                        OperationOK[1] = JposConst.JPOS_E_ILLEGAL;
                        OperationOK[1] = "Tag ID " + ((tag[ProtoIdx][0] & TagLocked) != 0 ? "locked" : "disabled");
                    }
                    break;
                }
            }
        } else {
            OperationOK[0] = JposConst.JPOS_E_FAILURE;
            OperationOK[1] = "Tag access error";
        }
        CurrentOp = Operation.noop;
        DataEventWaiter.signal();
        DataEventWaiter = null;
    }

    private void finishRead(boolean ok) {
        SampleProperties props = (SampleProperties)ClaimedRFIDScanner[0];
        ArrayList<RFIDScannerTagData> tags = new ArrayList<>();
        long now = System.currentTimeMillis();
        if (!props.ContinuousReadMode || now - LastReadEventTime >= props.ReadTimerInterval) {
            if (ok) {
                byte[][][] data = Labels.get(LabelIndex);
                for (byte[][] tag : data) {
                    if ((tag[ProtoIdx][0] & TagDisabled) == 0) {
                        if (((1 << (tag[ProtoIdx][0] & TagProtoMask)) & props.ProtocolMask) != 0 ||
                                (props.ProtocolMask & RFIDScannerConst.RFID_PR_ALL) != 0) {
                            byte[] id = tag[IDIdx].length == FilterID.length ? getMaskedData(tag[IDIdx]) : new byte[0];
                            if (Arrays.equals(id, FilterID)) {
                                id = (Cmd & RFIDScannerConst.RFID_RT_ID) != 0 ? tag[IDIdx] : new byte[0];
                                byte[] tagdata = new byte[0];
                                if ((Cmd & RFIDScannerConst.RFID_RT_FULLUSERDATA) != 0)
                                    tagdata = tag[DataIdx];
                                else if ((Cmd & RFIDScannerConst.RFID_RT_PARTIALUSERDATA) != 0) {
                                    if (Start + Length <= tag[DataIdx].length)
                                        tagdata = Arrays.copyOfRange(tag[DataIdx], Start, Start + Length);
                                    else if (Start < tag[DataIdx].length)
                                        tagdata = Arrays.copyOfRange(tag[DataIdx], Start, tag[DataIdx].length - Start);
                                }
                                tags.add(new RFIDScannerTagData(id, tagdata, 1 << (tag[ProtoIdx][0] & TagProtoMask)));
                            }
                        }
                    }
                }
            }
            if (tags.size() > 0) {
                try {
                    handleEvent(new RFIDScannerDataEvent(props.EventSource, 0, tags));
                } catch (JposException e) {
                }
            }
            if (DataEventWaiter != null) {
                CurrentOp = Operation.noop;
                Filtermask = FilterID = null;
                if (ok) {
                    if (tags.size() > 0)
                        OperationOK[0] = null;
                    else {
                        OperationOK[0] = JposConst.JPOS_E_NOEXIST;
                        OperationOK[1] = "No tags matching the filter found";
                    }
                } else {
                    OperationOK[0] = JposConst.JPOS_E_FAILURE;
                    OperationOK[1] = "Could not read RFID tags";
                }
                DataEventWaiter.signal();
                DataEventWaiter = null;
            } else if (tags.size() == 0) {
                try {
                    if (!ok)
                        handleEvent(new RFIDScannerErrorEvent(props.EventSource, JposConst.JPOS_E_FAILURE, 0, JposConst.JPOS_EL_INPUT, "RFID read error"));
                    else
                        handleEvent(new RFIDScannerErrorEvent(props.EventSource, JposConst.JPOS_E_NOEXIST, 0, JposConst.JPOS_EL_INPUT, "No ID match"));
                } catch (JposException e) {
                }
            }
        }
    }

    private byte[] getMaskedData(byte[] id) {
        byte[] result = new byte[id.length];
        for (int i = 0; i < result.length; i++)
            result[i] = (byte)(id[i] & Filtermask[i]);
        return result;
    }

    private class SampleProperties extends RFIDScannerProperties {
        protected SampleProperties() {
            super(0);
        }

        @Override
        synchronized public void open() throws JposException {
            check(CapMultipleProtocols == 0, JposConst.JPOS_E_ILLEGAL, "Invalid CapMultipleProtocols: 0");
            if (Labels.get(0)[0][ProtoIdx] == null) {
                byte index = 0;
                for (byte[][][] data : Labels) {
                    for (byte[][] tag : data) {
                        while (true) {
                            if ((1 << index) > CapMultipleProtocols)
                                index = 0;
                            if (((1 << index) & CapMultipleProtocols) != 0)
                                break;
                        }
                        tag[ProtoIdx] = new byte[]{index++};
                    }
                }
            }
            super.open();
        }

        @Override
        public synchronized void initOnClaim() {
            super.initOnClaim();
            if (TheRunner == null) {
                TheRunner = new Thread(RFIDDevice.this);
                TheRunner.setName("SCRWDeviceRunner");
                TheRunner.start();
            }
        }

        @Override
        synchronized public void close() throws JposException {
            if (RFIDScanners[0].size() == 1) {
                ToBeFinished = true;
                SynchronizedMessageBox box = TheBox;
                if (box != null)
                    box.abortDialog();
            }
        }

        @Override
        public StartReadTags startReadTags(int cmd, byte[] filterID, byte[] filtermask, int start, int length, byte[] password) throws JposException {
            synchronized (TheRunner) {
                Cmd = cmd;
                Filtermask = Arrays.copyOf(filtermask, filtermask.length);
                FilterID = getMaskedData(filterID);
                Start = start;
                Length = length;
                CurrentOp = Operation.read;
                LastReadEventTime = System.currentTimeMillis() - ReadTimerInterval;
                if (ReaderState == Status.gotTags)
                    finishRead(true);
            }
            super.startReadTags(null);
            return null;
        }

        @Override
        public void stopReadTags(byte[] password) throws JposException {
            synchronized (TheRunner) {
                Filtermask = FilterID = null;
                CurrentOp = Operation.noop;
            }
            ContinuousReadMode = false;
            EventSource.logSet("ContinuousReadMode");
        }

        @Override
        public ReadTags readTags(int cmd, byte[] filterID, byte[] filtermask, int start, int length, int timeout, byte[] password) throws JposException {
            return new ReadTags(this, cmd, filterID, filtermask, start, length, timeout, password);
        }

        @Override
        public void readTags(ReadTags request) throws JposException {
            SyncObject waiter;
            Object[] result = {null, null, true};
            synchronized (TheRunner) {
                check(CurrentOp != Operation.noop, JposConst.JPOS_E_ILLEGAL, "Device busy");
                Cmd = request.getCmd();
                Filtermask = request.getFiltermask();
                FilterID = getMaskedData(request.getFilterID());
                Start = request.getStart();
                Length = request.getLength();
                DataEventWaiter = waiter = new SyncObject();
                OperationOK = result;
                CurrentOp = Operation.read;
                if (ReaderState == Status.gotTags)
                    finishRead(true);
            }
            result[2] = waiter.suspend(request.getTimeout() == JposConst.JPOS_FOREVER ? SyncObject.INFINITE : request.getTimeout());
            checkResult(result);
        }

        @Override
        public DisableTag disableTag(byte[] tagID, int timeout, byte[] password) throws JposException {
            return new DisableTag(this, tagID, timeout, password);
        }

        @Override
        public void disableTag(DisableTag request) throws JposException {
            SyncObject waiter;
            Object[] result = {null, null, true};
            synchronized (TheRunner) {
                check(CurrentOp != Operation.noop, JposConst.JPOS_E_ILLEGAL, "Device busy");
                FilterID = request.getTagID();
                DataEventWaiter = waiter = new SyncObject();
                OperationOK = result;
                CurrentOp = Operation.disable;
            }
            result[2] = waiter.suspend(request.getTimeout() == JposConst.JPOS_FOREVER ? SyncObject.INFINITE : request.getTimeout());
            checkResult(result);
        }

        @Override
        public LockTag lockTag(byte[] tagID, int timeout, byte[] password) throws JposException {
            return new LockTag(this, tagID, timeout, password);
        }

        @Override
        public void lockTag(LockTag request) throws JposException {
            SyncObject waiter;
            Object[] result = {null, null, true};
            synchronized (TheRunner) {
                check(CurrentOp != Operation.noop, JposConst.JPOS_E_ILLEGAL, "Device busy");
                FilterID = request.getTagID();
                DataEventWaiter = waiter = new SyncObject();
                OperationOK = result;
                CurrentOp = Operation.lock;
            }
            result[2] = waiter.suspend(request.getTimeout() == JposConst.JPOS_FOREVER ? SyncObject.INFINITE : request.getTimeout());
            checkResult(result);
        }

        @Override
        public WriteTagData writeTagData(byte[] tagID, byte[] userdata, int start, int timeout, byte[] password) throws JposException {
            return new WriteTagData(this, tagID, userdata, start, timeout, password);
        }

        @Override
        public void writeTagData(WriteTagData request) throws JposException {
            SyncObject waiter;
            Object[] result = {null, null, true};
            synchronized (TheRunner) {
                check(CurrentOp != Operation.noop, JposConst.JPOS_E_ILLEGAL, "Device busy");
                FilterID = request.getTagID();
                Filtermask = request.getUserData();
                Start = request.getStart();
                DataEventWaiter = waiter = new SyncObject();
                OperationOK = result;
                CurrentOp = Operation.writeData;
            }
            result[2] = waiter.suspend(request.getTimeout() == JposConst.JPOS_FOREVER ? SyncObject.INFINITE : request.getTimeout());
            checkResult(result);
        }

        @Override
        public WriteTagID writeTagID(byte[] sourceID, byte[] destID, int timeout, byte[] password) throws JposException {
            return new WriteTagID(this, sourceID, destID, timeout, password);
        }

        @Override
        public void writeTagID(WriteTagID request) throws JposException {
            SyncObject waiter;
            Object[] result = {null, null, true};
            synchronized (TheRunner) {
                check(CurrentOp != Operation.noop, JposConst.JPOS_E_ILLEGAL, "Device busy");
                FilterID = request.getTagID();
                Filtermask = request.getDestID();
                DataEventWaiter = waiter = new SyncObject();
                OperationOK = result;
                CurrentOp = Operation.writeId;
            }
            result[2] = waiter.suspend(request.getTimeout() == JposConst.JPOS_FOREVER ? SyncObject.INFINITE : request.getTimeout());
            checkResult(result);
        }

        /**
         * Check result of operation. result specifies how the operation finished:
         * <ul>
         *     <li>If result[2] is false, the operation timed out and a JposException with error code E_TIMEOUT will be
         *     thrown. If the operation was currently in progress, it will be removed from the server thread. The
         *     contents of result[0] and result[1] will be ignored.</li>
         *     <li>If result[2] is true, the operation result depends on the result[0]. If result[0] is null, the
         *     operation was successful. Otherwise, result[0] is an Integer value containing the error code and result[1]
         *     is a String containing the error message of a JposException to be throws.</li>
         * </ul>
         * @param result        Result array as passed to TheRunner via property OperationOK. Array length must be three
         *                      or above.
         * @throws JposException    Throws JposException if result[0] != null or result[2] = false.
         */
        private void checkResult(Object[] result) throws JposException {
            if (!(Boolean)result[2]) {
                synchronized (TheRunner) {
                    if (OperationOK == result) {
                        CurrentOp = Operation.noop;
                        Filtermask = FilterID = null;
                        DataEventWaiter = null;
                        OperationOK = null;
                    } else
                        result[2] = true;   // Operation finished in the meantime
                }
            }
            check(!(Boolean)result[2], JposConst.JPOS_E_TIMEOUT, "");
            if (result[0] != null)
                throw new JposException((Integer) result[0], (String) result[1]);
        }
    }
}
