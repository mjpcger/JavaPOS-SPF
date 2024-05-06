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
import jpos.*;

import java.util.*;

import static jpos.JposConst.*;

/**
 * Input request executor for RFIDScanner method ReadTags and StartReadTags.
 */
public class ReadTags extends JposInputRequest {
    /**
     * RFIDScanner method parameter cmd, see UPOS specification.
     * @return Parameter start.
     */
    public int getCmd() {
        return Cmd;
    }

    /**
     * RFIDScanner method parameter filterID, see UPOS specification.
     * @return Parameter password.
     */
    public byte[] getFilterID() {
        return Arrays.copyOf(FilterID, FilterID.length);
    }

    /**
     * RFIDScanner method parameter filtermask, see UPOS specification.
     * @return Parameter password.
     */
    public byte[] getFiltermask() {
        return Arrays.copyOf(Filtermask, Filtermask.length);
    }

    /**
     * RFIDScanner method parameter start, see UPOS specification.
     * @return Parameter start.
     */
    public int getStart() {
        return Start;
    }

    /**
     * RFIDScanner method parameter length, see UPOS specification.
     * @return Parameter start.
     */
    public int getLength() {
        return Length;
    }

    /**
     * RFIDScanner method ReadTags parameter timeout, see UPOS specification.
     * @return Parameter timeout.
     */
    public int getTimeout() {
        return Timeout;
    }

    /**
     * RFIDScanner method parameter password, see UPOS specification.
     * @return Parameter password.
     */
    public byte[] getPassword() {
        return Arrays.copyOf(Password, Password.length);
    }

    private final byte[] Password, FilterID, Filtermask;
    private final int Timeout, Cmd, Start, Length;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param cmd        Read command, specifies what has to be read.
     * @param filterID   Holds a bit pattern to be AND’ed with filtermask to specify which tags shall be read.
     * @param filtermask Mask for filterID and tag ID, a tag will be read whenever the tag ID AND'ed with filtermask
     *                   is equal to filterID AND'ed with filtermask.
     * @param start      In case of partial user data read, start specifies the zero-based position within user data
     *                   where read shall start.
     * @param length     In case of partial user data read, length specifies the number of bytes to be read.
     * @param timeout    Allowed execution time, in milliseconds or FOREVER for unlimited execution time.
     * @param password   Authorized key for reader that might be required.
     */
    public ReadTags(JposCommonProperties props, int cmd, byte[] filterID, byte[] filtermask, int start, int length, int timeout, byte[] password) {
        super(props);
        Cmd = cmd;
        FilterID = Arrays.copyOf(filterID, filterID.length);
        Filtermask = Arrays.copyOf(filtermask, filtermask.length);
        Start = start;
        Length = length;
        Timeout = timeout;
        Password = Arrays.copyOf(password, password.length);
    }

    @Override
    public JposErrorEvent createErrorEvent(JposException ex) {
        return new RFIDScannerErrorEvent(Props.EventSource, ex.getErrorCode(), ex.getErrorCodeExtended(), JPOS_EL_INPUT, ex.getMessage());
    }

    @Override
    public void invoke() throws JposException {
        ((RFIDScannerService)Props.EventSource).RFIDScanner.readTags(this);
    }
}
