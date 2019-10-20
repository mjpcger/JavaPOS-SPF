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
        if (Dev.internalCheckHealth(this, level))
            return;
        // TOBEIMPLEMENTED
        super.checkHealth(level);
    }

    @Override
    public void waitForKeylockChange(int pos, int timeout) throws JposException {
        long startTime = System.currentTimeMillis();
        long occurredTime = 0;
        long tio = Dev.timeoutToLong(timeout);
        if (pos != KeyPosition || pos == KeylockConst.LOCK_KP_ANY) {
            attachWaiter();
            while (occurredTime < tio && waitWaiter(tio - occurredTime)) {
                if (pos == KeylockConst.LOCK_KP_ANY || pos == KeyPosition) {
                    occurredTime = tio - 1;
                    break;
                }
                occurredTime = System.currentTimeMillis() - startTime;
            }
            releaseWaiter();
            if (occurredTime == tio)
                throw new JposException(JposConst.JPOS_E_TIMEOUT, "No keylock change");
        }
        super.waitForKeylockChange(pos, timeout);
    }
}
