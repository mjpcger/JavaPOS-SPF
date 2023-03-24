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
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     */
    public StartReadTags(JposCommonProperties props, int cmd, byte[] filterID, byte[] filtermask, int start, int length, byte[] password) {
        super(props, cmd, filterID, filtermask, start, length, JposConst.JPOS_FOREVER, password);
    }

    @Override
    public void invoke() throws JposException {
        ((RFIDScannerService)Props.EventSource).RFIDScanner.startReadTags(this);
    }
}
