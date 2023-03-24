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

import de.gmxhome.conrad.jpos.jpos_base.*;

import java.util.Arrays;

/**
 * Base class for output request executor for RFIDScanner method DisableTag, LockTag, WriteTagData and WriteTagID.
 */
public class RFIDRequest extends JposOutputRequest {
    /**
     * RFIDScanner method parameter tagID or sourceID, see UPOS specification.
     * @return Parameter tagID or sourceID.
     */
    public byte[] getTagID() {
        return Arrays.copyOf(TagID, TagID.length);
    }

    /**
     * RFIDScanner method parameter password, see UPOS specification.
     * @return Parameter password.
     */
    public byte[] getPassword() {
        return Arrays.copyOf(Password, Password.length);
    }

    /**
     * RFIDScanner method parameter timeout, see UPOS specification.
     * @return Parameter timeout.
     */
    public int getTimeout() {
        return Timeout;
    }

    private byte[] TagID;
    private byte[] Password;
    private int Timeout;

    /**
     * Constructor, stores given parameters for later use of WriteData operation.
     * @param props        Property set of device service.
     * @param tagID        Tag ID to be processed.
     * @param timeout      Allowed execution time, in milliseconds.
     * @param password     Authorized key for reader that might be required, zero length if not needed.
     */
    public RFIDRequest(RFIDScannerProperties props, byte[] tagID, int timeout, byte[] password) {
        super(props);
        TagID = Arrays.copyOf(tagID, tagID.length);
        Timeout = timeout;
        Password = Arrays.copyOf(password, password.length);
    }
}
