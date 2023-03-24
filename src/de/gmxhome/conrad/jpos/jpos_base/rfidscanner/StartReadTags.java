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

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import jpos.JposConst;
import jpos.JposException;

/**
 * Input request executor for RFIDScanner method ReadTags and StartReadTags.
 */
public class StartReadTags extends ReadTags {
    /**
     * Constructor. Stores given parameters for later use in method StartReadTags.
     *
     * @param props      Property set of device service.
     * @param cmd        Read command, specifies what has to be read.
     * @param filterID   Holds a bit pattern to be AND’ed with filtermask to specify which tags shall be read.
     * @param filtermask Mask for filterID and tag ID, a tag will be read whenever the tag ID AND'ed with filtermask
     *                   is equal to filterID AND'ed with filtermask.
     * @param start      In case of partial user data read, start specifies the zero-based position within user data
     *                   where read shall start.
     * @param length     In case of partial user data read, length specifies the number of bytes to be read.
     * @param password   Authorized key for reader that might be required.
     */
    public StartReadTags(JposCommonProperties props, int cmd, byte[] filterID, byte[] filtermask, int start, int length, byte[] password) {
        super(props, cmd, filterID, filtermask, start, length, JposConst.JPOS_FOREVER, password);
    }

    @Override
    public void invoke() throws JposException {
        ((RFIDScannerService)Props.EventSource).RFIDScanner.startReadTags(this);
    }
}
