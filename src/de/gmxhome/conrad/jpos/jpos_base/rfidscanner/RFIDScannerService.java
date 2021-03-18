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
import jpos.JposException;
import jpos.services.RFIDScannerService114;

/**
 * RFIDScanner service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class RFIDScannerService extends JposBase implements RFIDScannerService114 {
    /**
     * Instance of a class implementing the RFIDScannerInterface for RFID scanner specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public RFIDScannerInterface RFIDScanner;

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
        return false;
    }

    @Override
    public boolean getCapDisableTag() throws JposException {
        return false;
    }

    @Override
    public boolean getCapLockTag() throws JposException {
        return false;
    }

    @Override
    public int getCapMultipleProtocols() throws JposException {
        return 0;
    }

    @Override
    public boolean getCapReadTimer() throws JposException {
        return false;
    }

    @Override
    public boolean getCapRealTimeData() throws JposException {
        return false;
    }

    @Override
    public int getCapWriteTag() throws JposException {
        return 0;
    }

    @Override
    public boolean getContinuousReadMode() throws JposException {
        return false;
    }

    @Override
    public byte[] getCurrentTagID() throws JposException {
        return new byte[0];
    }

    @Override
    public int getCurrentTagProtocol() throws JposException {
        return 0;
    }

    @Override
    public byte[] getCurrentTagUserData() throws JposException {
        return new byte[0];
    }

    @Override
    public int getProtocolMask() throws JposException {
        return 0;
    }

    @Override
    public void setProtocolMask(int i) throws JposException {

    }

    @Override
    public int getReadTimerInterval() throws JposException {
        return 0;
    }

    @Override
    public void setReadTimerInterval(int i) throws JposException {

    }

    @Override
    public int getTagCount() throws JposException {
        return 0;
    }

    @Override
    public void disableTag(byte[] bytes, int i, byte[] bytes1) throws JposException {

    }

    @Override
    public void firstTag() throws JposException {

    }

    @Override
    public void lockTag(byte[] bytes, int i, byte[] bytes1) throws JposException {

    }

    @Override
    public void nextTag() throws JposException {

    }

    @Override
    public void previousTag() throws JposException {

    }

    @Override
    public void readTags(int i, byte[] bytes, byte[] bytes1, int i1, int i2, int i3, byte[] bytes2) throws JposException {

    }

    @Override
    public void startReadTags(int i, byte[] bytes, byte[] bytes1, int i1, int i2, byte[] bytes2) throws JposException {

    }

    @Override
    public void stopReadTags(byte[] bytes) throws JposException {

    }

    @Override
    public void writeTagData(byte[] bytes, byte[] bytes1, int i, int i1, byte[] bytes2) throws JposException {

    }

    @Override
    public void writeTagID(byte[] bytes, byte[] bytes1, int i, byte[] bytes2) throws JposException {

    }
}
