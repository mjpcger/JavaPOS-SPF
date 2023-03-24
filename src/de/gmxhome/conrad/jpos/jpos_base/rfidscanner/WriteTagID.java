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

import jpos.JposException;

import java.util.Arrays;

/**
 * Output request executor for RFIDScanner method WriteTagID.
 */
public class WriteTagID extends RFIDRequest {
    /**
     * RFIDScanner method WriteTagID parameter destID, see UPOS specification.
     * @return Parameter destID.
     */
    public byte[] getDestID() {
        return Arrays.copyOf(DestID, DestID.length);
    }

    private byte[] DestID;

    /**
     * Constructor, stores given parameters for later use of WriteData operation.
     *
     * @param props    Property set of device service.
     * @param sourceID Original Tag ID to be processed.
     * @param destID   New ID of the tag.
     * @param timeout  Allowed execution time, in milliseconds.
     * @param password Authorized key for reader that might be required, zero length if not needed.
     */
    public WriteTagID(RFIDScannerProperties props, byte[] sourceID, byte[] destID, int timeout, byte[] password) {
        super(props, sourceID, timeout, password);
        DestID = Arrays.copyOf(destID, destID.length);
    }

    @Override
    public void invoke() throws JposException {
        ((RFIDScannerService)Props.EventSource).RFIDScanner.writeTagID(this);
    }
}
