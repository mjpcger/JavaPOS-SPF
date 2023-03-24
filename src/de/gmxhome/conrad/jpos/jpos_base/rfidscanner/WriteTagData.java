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

package de.gmxhome.conrad.jpos.jpos_base.rfidscanner;

import de.gmxhome.conrad.jpos.jpos_base.JposDevice;
import jpos.JposException;

import java.util.Arrays;

/**
 * Output request executor for RFIDScanner method WriteTagData.
 */
public class WriteTagData extends RFIDRequest {
    /**
     * RFIDScanner method WriteTagData parameter userData, see UPOS specification.
     * @return Parameter userData.
     */
    public byte[] getUserData() {
        return Arrays.copyOf(UserData, UserData.length);
    }

    /**
     * RFIDScanner method WriteTagData parameter start, see UPOS specification.
     * @return Parameter start.
     */
    public int getStart() {
        return Start;
    }

    private byte[] UserData;
    private int Start;

    /**
     * Constructor, stores given parameters for later use of WriteData operation.
     *
     * @param props    Property set of device service.
     * @param tagID    Tag ID to be processed.
     * @param userData Data to be written.
     * @param start    Zero-based position within the tags UserData field to begin writing.
     * @param timeout  Allowed execution time, in milliseconds.
     * @param password Authorized key for reader that might be required, zero length if not needed.
     */
    public WriteTagData(RFIDScannerProperties props, byte[] tagID, byte[] userData, int start, int timeout, byte[] password) {
        super(props, tagID, timeout, password);
        UserData = Arrays.copyOf(userData, userData.length);
        Start = start;
    }

    @Override
    public void invoke() throws JposException {
        ((RFIDScannerService)Props.EventSource).RFIDScanner.writeTagData(this);
    }
}
