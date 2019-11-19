/*
 * Copyright 2018 Martin Conrad
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

package SampleCombiDevice;

import de.gmxhome.conrad.jpos.jpos_base.scanner.*;
import jpos.*;

/**
 * Class implementing the POSKeyboardInterface for the sample combi device.
 * External and interactive Checkhealth might be implemented in a later version.
 */
public class Scanner extends ScannerProperties {
    private Device Dev;
    // Scanner commands
    private static byte[] CmdScannerEnable = {'R','E'};
    private static byte[] CmdScannerDisable = {'R','D'};


    /**
     * Constructor. Gets instance of Device to be used as communication object. Device index for
     * sample is always 0.
     * @param dev Instance of Device this object belongs to.
     */
    public Scanner(Device dev) {
        super(0);
        Dev = dev;
    }

    @Override
    public void claim(int timeout) throws JposException {
        Dev.startCommunication();
        if (DataEventEnabled)
            Dev.sendCommand(CmdScannerEnable, Dev.NoResponse);
        super.claim(timeout);
    }

    @Override
    public void release() throws JposException {
        if (DataEventEnabled)
            Dev.sendCommand(CmdScannerDisable, Dev.NoResponse);
        super.release();
        Dev.stopCommunication();
    }

    @Override
    public void checkHealth(int level) throws JposException {
        if (Dev.internalCheckHealth(this, level))
            return;
        // TOBEIMPLEMENTED
        super.checkHealth(level);
    }

    @Override
    public void dataEventEnabled(boolean enable) throws JposException {
        if (DataEventEnabled != enable)
            Dev.sendCommand(enable ? CmdScannerEnable : CmdScannerDisable, Dev.NoResponse);
        super.dataEventEnabled(enable);
    }
}
