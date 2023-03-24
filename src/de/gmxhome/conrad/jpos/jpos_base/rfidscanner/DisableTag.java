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

/**
 * Output request executor for RFIDScanner method DisableTag.
 */
public class DisableTag extends RFIDRequest {
    /**
     * Constructor, stores given parameters for later use of WriteData operation.
     *
     * @param props    Property set of device service.
     * @param tagID    Tag ID to be processed.
     * @param timeout  Allowed execution time, in milliseconds.
     * @param password Authorized key for reader that might be required, zero length if not needed.
     */
    public DisableTag(RFIDScannerProperties props, byte[] tagID, int timeout, byte[] password) {
        super(props, tagID, timeout, password);
    }

    @Override
    public void invoke() throws JposException {
        ((RFIDScannerService)Props.EventSource).RFIDScanner.disableTag(this);
    }
}
