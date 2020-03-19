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

import de.gmxhome.conrad.jpos.jpos_base.keylock.*;

import jpos.*;

import javax.swing.*;

import static SampleCombiDevice.Device.*;

/**
 * Class implementing the KeylockInterface for the sample combi device.
 * External and interactive Checkhealth might be implemented in a later version.
 */
public class Keylock extends KeylockProperties {
    private Device Dev;

    /**
     * Constructor. Gets index of Keylock to be used and an instance of Device to be used as communication object.
     * @param index Property set used by this KeylockInterface implementation.
     * @param dev Instance of Device this object belongs to.
     */
    public Keylock(int index, Device dev) {
        super(index);
        Dev = dev;
    }

    @Override
    public void deviceEnabled(boolean enable) throws JposException {
        super.deviceEnabled(enable);
        Dev.updateCommonStates(this, enable);
        Dev.updateKeylockStates(this, enable);
        if (!enable)
            signalWaiter();
    }

    @Override
    public void open() throws JposException {
        Dev.startCommunication();
        super.open();
    }

    @Override
    public void close() throws JposException {
        super.close();
        Dev.stopCommunication();
    }

    @Override
    public void checkHealth(int level) throws JposException {
        if (!Dev.internalCheckHealth(this, level) && !externalCheckHealth(level))
            interactiveCheckHealth(level);
        super.checkHealth(level);
    }

    private void interactiveCheckHealth(int level) {
        if (level == JposConst.JPOS_CH_INTERACTIVE) {
            if (Dev.DeviceIsOffline || Dev.InIOError) {
                CheckHealthText = "Interactive check: Error";
                Dev.synchronizedMessageBox("Keylock not operational.", "CheckHealth Drawer", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    Dev.synchronizedMessageBox("Press OK, then change the key lock", "CheckHealth Keylock", JOptionPane.INFORMATION_MESSAGE);
                    CheckHealthText = "Interactive check: Error";
                    ((KeylockService) EventSource).waitForKeylockChange(KeylockConst.LOCK_KP_ANY, 10000);
                    CheckHealthText = "Interactive check: OK";
                    Dev.synchronizedMessageBox("Keylock change successful.", "CheckHealth Drawer", JOptionPane.INFORMATION_MESSAGE);
                } catch (JposException e) {
                    if (e.getErrorCode() == JposConst.JPOS_E_TIMEOUT) {
                        CheckHealthText = "Interactive check: Timed out";
                        Dev.synchronizedMessageBox("Keylock change timed out.", "CheckHealth Drawer", JOptionPane.ERROR_MESSAGE);
                    } else {
                        Dev.synchronizedMessageBox("Error occurred during wait for keylock change:\n"
                                + e.getMessage(), "CheckHealth Drawer", JOptionPane.ERROR_MESSAGE);
                        CheckHealthText += ", " + e.getMessage();
                    }
                }
            }
        }
    }

    private boolean externalCheckHealth(int level) {
        if (level == JposConst.JPOS_CH_EXTERNAL) {
            CheckHealthText = "External check: Error";
            if (!Dev.DeviceIsOffline && !Dev.InIOError) {
                try {
                    ((KeylockService) EventSource).waitForKeylockChange(KeylockConst.LOCK_KP_ANY, 10000);
                    CheckHealthText = "External check: OK";
                } catch (JposException e) {
                    if (e.getErrorCode() == JposConst.JPOS_E_TIMEOUT)
                        CheckHealthText = "External check: Timed out";
                    else
                        CheckHealthText += ", " + e.getMessage();
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void waitForKeylockChange(int pos, int timeout) throws JposException {
        long startTime = System.currentTimeMillis();
        long occurredTime = 0;
        long tio = Dev.timeoutToLong(timeout);
        if (pos != KeyPosition || pos == KeylockConst.LOCK_KP_ANY) {
            attachWaiter();
            while ((tio < 0 || occurredTime < tio) && waitWaiter(tio - occurredTime) && DeviceEnabled) {
                if (pos == KeylockConst.LOCK_KP_ANY || pos == KeyPosition || Dev.DeviceIsOffline) {
                    occurredTime = tio - 1;
                    break;
                }
                occurredTime = tio >= 0 ? System.currentTimeMillis() - startTime : 0;
            }
            releaseWaiter();
            Dev.check(occurredTime >= tio && tio != JposConst.JPOS_FOREVER, JposConst.JPOS_E_TIMEOUT, "No keylock change");
            Dev.check(Dev.DeviceIsOffline, JposConst.JPOS_E_OFFLINE, "Device offline");
            check(!DeviceEnabled, JposConst.JPOS_E_ILLEGAL, "Device not enabled");
        }
        super.waitForKeylockChange(pos, timeout);
    }
}
