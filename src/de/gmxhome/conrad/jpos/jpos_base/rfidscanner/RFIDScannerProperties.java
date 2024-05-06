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

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import jpos.JposException;

import static jpos.RFIDScannerConst.*;

/**
 * Class containing the RFID scanner specific properties, their default values and default implementations of
 * RFIDScannerInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter RFID Scanner.
 * No methods for meth
 */
public class RFIDScannerProperties extends JposCommonProperties implements RFIDScannerInterface {
    /**
     * UPOS property CapContinuousRead. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapContinuousRead = false;
    /**
     * UPOS property CapDisableTag. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapDisableTag = false;
    /**
     * UPOS property CapLockTag. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapLockTag = false;
    /**
     * UPOS property CapMultipleProtocols. Default: 0. Must be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int CapMultipleProtocols = 0;
    /**
     * UPOS property CapReadTimer. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapReadTimer = false;
    /**
     * UPOS property CapWriteTag. Default: CWT_NONE. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int CapWriteTag = RFID_CWT_NONE;
    /**
     * UPOS property ContinuousReadMode. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean ContinuousReadMode = false;
    /**
     * UPOS property CurrentTagID. Default: null. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public byte[] CurrentTagID = null;
    /**
     * UPOS property CurrentTagProtocol. Default: 0. Must be overwritten by objects derived from JposDevice before
     * delivering a DataEvent.
     */
    public int CurrentTagProtocol = 0;
    /**
     * UPOS property CurrentTagUserData. Default: null. Must be overwritten by objects derived from JposDevice before
     * delivering a DataEvent.
     */
    public byte[] CurrentTagUserData = null;
    /**
     * UPOS property ProtocolMask. Default: Same as CapMultipleProtocols.
     */
    public int ProtocolMask;
    /**
     * UPOS property ReadTimerInterval. Default: 0.
     */
    public int ReadTimerInterval = 0;
    /**
     * UPOS property CapCardErrorDetection. Default: 0.
     */
    public int TagCount = 0;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected RFIDScannerProperties(int dev) {
        super(dev);
        ExclusiveUse = ExclusiveYes;
        AsyncMode = true;
    }

    @Override
    public void clearDataProperties() {
        super.clearDataProperties();
        if (CurrentTagProtocol != 0) {
            CurrentTagProtocol = 0;
            EventSource.logSet("CurrentTagProtocol");
        }
        if (CurrentTagID != null) {
            CurrentTagID = null;
            EventSource.logSet("CurrentTagID");
        }
        if (CurrentTagUserData != null) {
            CurrentTagUserData = null;
            EventSource.logSet("CurrentTagUserData");
        }
        if (TagCount != 0) {
            TagCount = 0;
            EventSource.logSet("TagCount");
        }
        ((RFIDScannerService)EventSource).CurrentLabelData.clear();
        ((RFIDScannerService)EventSource).CurrentTagIndex = -1;
    }

    @Override
    public void initOnOpen() {
        super.initOnOpen();
        ProtocolMask = CapMultipleProtocols;
        AsyncMode = true;
    }

    @Override
    public void protocolMask(int mask) throws JposException {
        ProtocolMask = mask;
    }

    @Override
    public void readTimerInterval(int interval) throws JposException {
        ReadTimerInterval = interval;
    }

    @Override
    public void stopReadTags(byte[] password) throws JposException {
        ContinuousReadMode = false;
        EventSource.logSet("ContinuousReadMode");
    }

    @Override
    public StartReadTags startReadTags(int cmd, byte[] filterID, byte[] filtermask, int start, int length, byte[] password) throws JposException {
        return new StartReadTags(this, cmd, filterID, filtermask, start, length, password);
    }

    @Override
    public ReadTags readTags(int cmd, byte[] filterID, byte[] filtermask, int start, int length, int timeout, byte[] password) throws JposException {
        return new ReadTags(this, cmd, filterID, filtermask, start, length, timeout, password);
    }

    @Override
    public DisableTag disableTag(byte[] tagID, int timeout, byte[] password) throws JposException {
        return new DisableTag(this, tagID, timeout, password);
    }

    @Override
    public LockTag lockTag(byte[] tagID, int timeout, byte[] password) throws JposException {
        return new LockTag(this, tagID, timeout, password);
    }

    @Override
    public WriteTagData writeTagData(byte[] tagID, byte[] userdata, int start, int timeout, byte[] password) throws JposException {
        return new WriteTagData(this, tagID, userdata, start, timeout, password);
    }

    @Override
    public WriteTagID writeTagID(byte[] sourceID, byte[] destID, int timeout, byte[] password) throws JposException {
        return new WriteTagID(this, sourceID, destID, timeout, password);
    }

    @Override
    public void disableTag(DisableTag request) throws JposException {

    }

    @Override
    public void lockTag(LockTag request) throws JposException {

    }

    @Override
    public void writeTagData(WriteTagData request) throws JposException {

    }

    @Override
    public void writeTagID(WriteTagID request) throws JposException {

    }

    @Override
    public void readTags(ReadTags request) throws JposException {

    }

    @Override
    public void startReadTags(StartReadTags request) throws JposException {
        ContinuousReadMode = true;
        EventSource.logSet("ContinuousReadMode");
    }
}
