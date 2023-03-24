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
import de.gmxhome.conrad.jpos.jpos_base.JposErrorEvent;
import de.gmxhome.conrad.jpos.jpos_base.JposInputRequest;
import jpos.JposConst;
import jpos.JposException;

import java.util.Arrays;

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

    private byte[] Password, FilterID, Filtermask;
    private int Timeout, Cmd, Start, Length;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
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
        return new RFIDScannerErrorEvent(Props.EventSource, ex.getErrorCode(), ex.getErrorCodeExtended(), JposConst.JPOS_EL_INPUT, ex.getMessage());
    }

    @Override
    public void invoke() throws JposException {
        ((RFIDScannerService)Props.EventSource).RFIDScanner.readTags(this);
    }
}
