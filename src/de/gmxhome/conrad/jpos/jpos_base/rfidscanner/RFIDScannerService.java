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

package de.gmxhome.conrad.jpos.jpos_base.rfidscanner;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.services.*;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * RFIDScanner service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class RFIDScannerService extends JposBase implements RFIDScannerService115 {
    /**
     * Instance of a class implementing the RFIDScannerInterface for RFID scanner specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public RFIDScannerInterface RFIDScanner;
    /**
     * Internally used list of tag data of a label that match the filter given by ReadTags or StartReadTags. Filled from
     * DataEvent whenever delivered or cleared by ErrorEvent with ErrorLocus EL_INPUT.
     */
    ArrayList<RFIDScannerTagData> CurrentLabelData = new ArrayList<>();
    /**
     * Internally used index to current tag in CurrentLabelData. Initialized whenever DataEvent is delivered, updated
     * whenever firstTag, nextTag or previousTag is called. -1 if not initialized, otherwise a value between 0 and
     * TagCount.
     */
    int CurrentTagIndex = -1;

    private RFIDScannerProperties Data;
    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public RFIDScannerService(RFIDScannerProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public boolean getCapContinuousRead() throws JposException {
        logGet("CapContinuousRead");
        checkOpened();
        return Data.CapContinuousRead;
    }

    @Override
    public boolean getCapDisableTag() throws JposException {
        logGet("CapDisableTag");
        checkOpened();
        return Data.CapDisableTag;
    }

    @Override
    public boolean getCapLockTag() throws JposException {
        logGet("CapLockTag");
        checkOpened();
        return Data.CapLockTag;
    }

    @Override
    public int getCapMultipleProtocols() throws JposException {
        logGet("MultipleProtocols");
        checkOpened();
        return Data.CapMultipleProtocols;
    }

    @Override
    public boolean getCapReadTimer() throws JposException {
        logGet("CapReadTimer");
        checkOpened();
        return Data.CapReadTimer;
    }

    @Override
    public boolean getCapRealTimeData() throws JposException {
        return false;
    }

    @Override
    public int getCapWriteTag() throws JposException {
        logGet("CapWriteTag");
        checkOpened();
        return Data.CapWriteTag;
    }

    @Override
    public boolean getContinuousReadMode() throws JposException {
        logGet("ContinuousReadMode");
        checkOpened();
        return Data.ContinuousReadMode;
    }

    @Override
    public byte[] getCurrentTagID() throws JposException {
        logGet("CurrentTagID");
        checkOpened();
        synchronized (CurrentLabelData) {
            JposDevice.check(Data.TagCount == 0, JposConst.JPOS_E_ILLEGAL, "No tag available");
            return Arrays.copyOf(Data.CurrentTagID, Data.CurrentTagID.length);
        }
    }

    @Override
    public int getCurrentTagProtocol() throws JposException {
        logGet("CurrentTagProtocol");
        checkOpened();
        synchronized (CurrentLabelData) {
            JposDevice.check(Data.TagCount == 0, JposConst.JPOS_E_ILLEGAL, "No tag available");
            return Data.CurrentTagProtocol;
        }
    }

    @Override
    public byte[] getCurrentTagUserData() throws JposException {
        logGet("CurrentTagUserData");
        checkOpened();
        synchronized (CurrentLabelData) {
            JposDevice.check(Data.TagCount == 0, JposConst.JPOS_E_ILLEGAL, "No tag available");
            return Arrays.copyOf(Data.CurrentTagUserData, Data.CurrentTagUserData.length);
        }
    }

    @Override
    public int getProtocolMask() throws JposException {
        logGet("ProtocolMask");
        checkClaimed();
        return Data.ProtocolMask;
    }

    @Override
    public int getReadTimerInterval() throws JposException {
        logGet("ReadTimerInterval");
        checkClaimed();
        return Data.ReadTimerInterval;
    }

    @Override
    public int getTagCount() throws JposException {
        logGet("TagCount");
        checkOpened();
        return Data.TagCount;
    }

    @Override
    public void setProtocolMask(int mask) throws JposException {
        logPreSet("ProtocolMask");
        checkClaimed();
        if ((mask & RFIDScannerConst.RFID_PR_ALL) == 0)
            JposDevice.check((mask & ~Data.CapMultipleProtocols) != 0, JposConst.JPOS_E_ILLEGAL, "Invalid Protocol Mask: " + Integer.toHexString(mask));
        RFIDScanner.protocolMask(mask);
        logSet("ProtocolMask");
    }

    @Override
    public void setReadTimerInterval(int interval) throws JposException {
        logPreSet("ProtocolMask");
        checkClaimed();
        JposDevice.check(!Data.CapReadTimer, JposConst.JPOS_E_ILLEGAL, "Setting read timer interval not supported");
        JposDevice.check(Data.ContinuousReadMode, JposConst.JPOS_E_ILLEGAL, "Setting read timer interval not possible in continuous read mode");
        JposDevice.check(interval < 0, JposConst.JPOS_E_ILLEGAL, "Read timer interval must not be negative: " + interval);
        RFIDScanner.readTimerInterval(interval);
        logSet("ProtocolMask");
    }

    /**
     * Sets the current tag properties to the tag pointed to by the given index in the list of tags read, provided by the
     * last delivered RFIDScannerDataEvent event.
     * @param index Index within the tag list, must be a value between 0 and TagCount - 1.
     * @throws JposException if index is out of range.
     */
    public void setCurrentTagData(int index) throws JposException {
        synchronized (CurrentLabelData) {
            JposDevice.check(index < 0 || index >= CurrentLabelData.size(), JposConst.JPOS_E_ILLEGAL,
                    "No tag data " + (index < 0 ? "before first tag" : "after last tag") + " available");
            RFIDScannerTagData tagData = CurrentLabelData.get(CurrentTagIndex = index);
            if (Data.CurrentTagProtocol != tagData.getTagProtocol()) {
                Data.CurrentTagProtocol = tagData.getTagProtocol();
                logSet("CurrentTagProtocol");
            }
            byte[] bytes = tagData.getTagID();
            if (!Arrays.equals(bytes, Data.CurrentTagID)) {
                Data.CurrentTagID = bytes;
                logSet("CurrentTagID");
            }
            bytes = tagData.getTagUserData();
            if (!Arrays.equals(bytes, Data.CurrentTagUserData)) {
                Data.CurrentTagUserData = bytes;
                logSet("CurrentTagUserData");
            }
        }
    }

    @Override
    public void firstTag() throws JposException {
        logPreCall("FirstTag");
        checkOpened();
        JposDevice.check(Data.TagCount == 0, JposConst.JPOS_E_ILLEGAL, "No tags available");
        setCurrentTagData(0);
        logCall("FirstTag");
    }

    @Override
    public void nextTag() throws JposException {
        logPreCall("NextTag");
        checkOpened();
        JposDevice.check(Data.TagCount == 0, JposConst.JPOS_E_ILLEGAL, "No tags available");
        setCurrentTagData(CurrentTagIndex + 1);
        logCall("NextTag");
    }

    @Override
    public void previousTag() throws JposException {
        logPreCall("PreviousTag");
        checkOpened();
        JposDevice.check(Data.TagCount == 0, JposConst.JPOS_E_ILLEGAL, "No tags available");
        setCurrentTagData(CurrentTagIndex - 1);
        logCall("PreviousTag");
    }

    private long[] validCmds = {
            RFIDScannerConst.RFID_RT_ID,
            RFIDScannerConst.RFID_RT_FULLUSERDATA,
            RFIDScannerConst.RFID_RT_PARTIALUSERDATA,
            RFIDScannerConst.RFID_RT_ID_FULLUSERDATA,
            RFIDScannerConst.RFID_RT_ID_PARTIALUSERDATA
    };
    private long[] relevantStartLengthCmds = {
            RFIDScannerConst.RFID_RT_ID_PARTIALUSERDATA,
            RFIDScannerConst.RFID_RT_PARTIALUSERDATA
    };

    @Override
    public void startReadTags(int cmd, byte[] filterID, byte[] filtermask, int start, int length, byte[] password) throws JposException {
        if (password == null)
            password = new byte[0];
        if (filterID == null)
            filterID = new byte[0];
        if (filtermask == null)
            filtermask = new byte[0];
        logPreCall("StartReadTags", "" + cmd + ", " + filterID.toString() + ", " + filtermask.toString() + ", " + start + ", " + length + ", " + password.toString());
        checkEnabled();
        JposDevice.check(!Data.CapContinuousRead, JposConst.JPOS_E_ILLEGAL, "Continuous read mode not supported");
        JposDevice.check(Data.ContinuousReadMode, JposConst.JPOS_E_ILLEGAL, "Continuous read mode just active");
        JposDevice.check(Data.State != JposConst.JPOS_S_IDLE, JposConst.JPOS_E_ILLEGAL, "Device is busy");
        JposDevice.check(filterID.length != filtermask.length, JposConst.JPOS_E_ILLEGAL, "Length of filter ID does not match length of filter mask");
        JposDevice.checkMember(cmd, validCmds, JposConst.JPOS_E_ILLEGAL, "Invalid cmd: " + cmd);
        if (cmd == RFIDScannerConst.RFID_RT_PARTIALUSERDATA || cmd == RFIDScannerConst.RFID_RT_ID_PARTIALUSERDATA) {
            JposDevice.check(start < 0 && JposDevice.member(cmd, relevantStartLengthCmds), JposConst.JPOS_E_ILLEGAL, "Invalid starting position: " + start);
            JposDevice.check(length < 0 && JposDevice.member(cmd, relevantStartLengthCmds), JposConst.JPOS_E_ILLEGAL, "Invalid read length: " + length);
        }
        callNowOrLater(RFIDScanner.startReadTags(cmd, filterID, filtermask, start, length, password));
        logAsyncCall("StartReadTags");
    }

    @Override
    public void stopReadTags(byte[] password) throws JposException {
        if (password == null)
            password = new byte[0];
        logPreCall("StopReadTags", password.toString());
        checkEnabled();
        JposDevice.check(!Data.ContinuousReadMode, JposConst.JPOS_E_ILLEGAL, "Not in continuous read mode");
        RFIDScanner.stopReadTags(password);
        logCall("StopReadTags");
    }

    @Override
    public void readTags(int cmd, byte[] filterID, byte[] filtermask, int start, int length, int timeout, byte[] password) throws JposException {
        if (password == null)
            password = new byte[0];
        if (filterID == null)
            filterID = new byte[0];
        if (filtermask == null)
            filtermask = new byte[0];
        logPreCall("ReadTags", "" + cmd + ", " + filterID.toString() + ", " + filtermask.toString() + ", " + start + ", " + length + ", " + timeout + ", " + password.toString());
        checkEnabled();
        JposDevice.check(Data.ContinuousReadMode, JposConst.JPOS_E_ILLEGAL, "Device in continuous read mode");
        JposDevice.check(Data.State != JposConst.JPOS_S_IDLE, JposConst.JPOS_E_ILLEGAL, "Device is busy");
        JposDevice.check(filterID.length != filtermask.length, JposConst.JPOS_E_ILLEGAL, "Length of filter ID does not match length of filter mask");
        JposDevice.checkMember(cmd, validCmds, JposConst.JPOS_E_ILLEGAL, "Invalid cmd: " + cmd);
        JposDevice.check(start < 0 && JposDevice.member(cmd, relevantStartLengthCmds), JposConst.JPOS_E_ILLEGAL, "Invalid starting position: " + start);
        JposDevice.check(length < 0 && JposDevice.member(cmd, relevantStartLengthCmds), JposConst.JPOS_E_ILLEGAL, "Invalid read length: " + length);
        JposDevice.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout value: " + timeout);
        callNowOrLater(RFIDScanner.readTags(cmd, filterID, filtermask, start, length, timeout, password));
        logAsyncCall("ReadTags");
    }

    @Override
    public void disableTag(byte[] tagID, int timeout, byte[] password) throws JposException {
        if (tagID == null)
            tagID = new byte[0];
        if (password == null)
            password = new byte[0];
        logPreCall("DisableTag", tagID.toString() + ", " + timeout + ", " + password.toString());
        checkEnabled();
        JposDevice.check(!Data.CapDisableTag, JposConst.JPOS_E_ILLEGAL, "Disable tag not supported");
        JposDevice.check(Data.ContinuousReadMode, JposConst.JPOS_E_ILLEGAL, "Device in continuous read mode");
        JposDevice.check(Data.State != JposConst.JPOS_S_IDLE, JposConst.JPOS_E_ILLEGAL, "Device is busy");
        JposDevice.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout value: " + timeout);
        callNowOrLater(RFIDScanner.disableTag(tagID, timeout, password));
        logAsyncCall("DisableTag");
    }

    @Override
    public void lockTag(byte[] tagID, int timeout, byte[] password) throws JposException {
        if (tagID == null)
            tagID = new byte[0];
        if (password == null)
            password = new byte[0];
        logPreCall("LockTag", tagID.toString() + ", " + timeout + ", " + password.toString());
        checkEnabled();
        JposDevice.check(!Data.CapLockTag, JposConst.JPOS_E_ILLEGAL, "Lock tag not supported");
        JposDevice.check(Data.ContinuousReadMode, JposConst.JPOS_E_ILLEGAL, "Device in continuous read mode");
        JposDevice.check(Data.State != JposConst.JPOS_S_IDLE, JposConst.JPOS_E_ILLEGAL, "Device is busy");
        JposDevice.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout value: " + timeout);
        callNowOrLater(RFIDScanner.lockTag(tagID, timeout, password));
        logAsyncCall("LockTag");
    }

    @Override
    public void writeTagData(byte[] tagID, byte[] userdata, int start, int timeout, byte[] password) throws JposException {
        long[] valid = {RFIDScannerConst.RFID_CWT_ALL, RFIDScannerConst.RFID_CWT_USERDATA};
        if (tagID == null)
            tagID = new byte[0];
        if (password == null)
            password = new byte[0];
        if (userdata == null)
            userdata = new byte[0];
        logPreCall("WriteTagData", tagID.toString() + ", " + userdata.toString() + ", " + start + ", " + timeout + ", " + password.toString());
        checkEnabled();
        JposDevice.checkMember(Data.CapWriteTag, valid, JposConst.JPOS_E_ILLEGAL, "Device does not support writing user data");
        JposDevice.check(Data.ContinuousReadMode, JposConst.JPOS_E_ILLEGAL, "Device in continuous read mode");
        JposDevice.check(Data.State != JposConst.JPOS_S_IDLE, JposConst.JPOS_E_ILLEGAL, "Device is busy");
        JposDevice.check(start < 0, JposConst.JPOS_E_ILLEGAL, "Invalid starting position: " + start);
        JposDevice.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout value: " + timeout);
        callNowOrLater(RFIDScanner.writeTagData(tagID, userdata, start, timeout, password));
        logAsyncCall("WriteTagData");
    }

    @Override
    public void writeTagID(byte[] sourceID, byte[] destID, int timeout, byte[] password) throws JposException {
        long[] valid = {RFIDScannerConst.RFID_CWT_ALL, RFIDScannerConst.RFID_CWT_ID};
        if (sourceID == null)
            sourceID = new byte[0];
        if (password == null)
            password = new byte[0];
        if (destID == null)
            destID = new byte[0];
        logPreCall("WriteTagID", sourceID.toString() + ", " + destID.toString() + ", " + timeout + ", " + password.toString());
        checkEnabled();
        JposDevice.checkMember(Data.CapWriteTag, valid, JposConst.JPOS_E_ILLEGAL, "Device does not support writing tag ID");
        JposDevice.check(Data.ContinuousReadMode, JposConst.JPOS_E_ILLEGAL, "Device in continuous read mode");
        JposDevice.check(Data.State != JposConst.JPOS_S_IDLE, JposConst.JPOS_E_ILLEGAL, "Device is busy");
        JposDevice.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout value: " + timeout);
        callNowOrLater(RFIDScanner.writeTagID(sourceID, destID, timeout, password));
        logAsyncCall("WriteTagID");
    }
}
