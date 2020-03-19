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

import de.gmxhome.conrad.jpos.jpos_base.SyncObject;
import de.gmxhome.conrad.jpos.jpos_base.poskeyboard.*;
import jpos.*;

import javax.swing.*;

/**
 * Class implementing the POSKeyboardInterface for the sample combi device.
 * External and interactive Checkhealth might be implemented in a later version.
 */
public class POSKeyboard extends POSKeyboardProperties {
    private Device Dev;

    /**
     * Constructor. Gets instance of Device to be used as communication object. Device index for
     * sample is always 0.
     * @param dev Instance of Device this object belongs to.
     */
    public POSKeyboard(Device dev) {
        super(0);
        Dev = dev;
    }

    @Override
    public void claim(int timeout) throws JposException {
        Dev.startCommunication();
        super.claim(timeout);
    }

    @Override
    public void release() throws JposException {
        super.release();
        Dev.stopCommunication();
    }

    @Override
    public void deviceEnabled(boolean enable) throws JposException {
        super.deviceEnabled(enable);
        Dev.updateCommonStates(this, enable);
    }

    @Override
    public void checkHealth(int level) throws JposException {
        if (!Dev.internalCheckHealth(this, level) && !externalCheckHealth(level)) {
            interactiveCheckHealth(level);
        }
        super.checkHealth(level);
    }

    private void interactiveCheckHealth(int level) {
        if (level == JposConst.JPOS_CH_INTERACTIVE) {
            String result;
            int datacount = DataCount;
            int loopcount;
            try {
                ((POSKeyboardService) EventSource).setFreezeEvents(true);
                if (!DataEventEnabled)
                    ((POSKeyboardService) EventSource).setDataEventEnabled(true);
                Dev.synchronizedMessageBox("Press OK, then press any key", "CheckHealth POSKeyboard", JOptionPane.INFORMATION_MESSAGE);
                for (loopcount = 0; loopcount < 100 && datacount == DataCount && (!Dev.DeviceIsOffline && !Dev.InIOError); loopcount++)
                    new SyncObject().suspend(100);
                result = (loopcount == 100 ? "Timed out" : (datacount < DataCount ? "OK" : "Error"));
                ((POSKeyboardService) EventSource).setFreezeEvents(false);
            } catch (JposException e) {
                result = "Error, " + e.getMessage();
            }
            Dev.synchronizedMessageBox("Keyboard check " + result + ".", "CheckHealth POSKeyboard",
                    (result.equals("OK") ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE));
            CheckHealthText = "Interactive check: " + result;
        }
    }

    private boolean externalCheckHealth(int level) {
        if (level == JposConst.JPOS_CH_EXTERNAL) {
            int datacount = DataCount;
            int loopcount;
            try {
                ((POSKeyboardService) EventSource).setFreezeEvents(true);
                if (!DataEventEnabled)
                    ((POSKeyboardService) EventSource).setDataEventEnabled(true);
                for (loopcount = 0; loopcount < 100 && datacount == DataCount && (!Dev.DeviceIsOffline && !Dev.InIOError); loopcount++)
                    new SyncObject().suspend(100);
                CheckHealthText = "External check: " + (loopcount == 100 ? "Timed out" : (datacount < DataCount ? "OK" : "Error"));
                ((POSKeyboardService) EventSource).setFreezeEvents(false);
            } catch (JposException e) {
                CheckHealthText = "External check: Error, " + e.getMessage();
            }
            return true;
        }
        return false;
    }
}
