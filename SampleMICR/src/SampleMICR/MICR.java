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

package SampleMICR;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.micr.*;
import jpos.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposBaseDevice.*;
import static jpos.JposConst.*;

/**
 * Class derived from MICRProperties that implements the MICRInterface for the sample magnetic ink character
 * recognition reader.
 * The following commands of the sample MICR simulator will be used:
 * <ul>
 *     <li><i><b>I</b></i>: The command to open the cheque slot for MICR reading,</li>
 *     <li><i><b>R</b></i>: The command to close the cheque slot for MICR reading.</li>
 * </ul>
 * In case of a successful read, the cheque slot will be released automatically. In this case, MICR data
 * in simplified E13B format will be retrieved by the communication handler:<br>
 *     &lt; ttttttttt &lt; aaaaaaaaaaaaaaa ; ssss LF<br>
 * where
 * <ul>
 *     <li><i><b>&lt;</b></i>: The Transit character. Will be replaced by <b><i>t</i></b> in property RawData
 *     due to UPOS convention,</li>
 *     <li><i>ttttttttt</i>: The left justified maximum 9 digit transit number,</li>
 *     <li><i>aaaaaaaaaaaaaaa</i>: The right justified maximum 15 digit account number, filled with spaces,</li>
 *     <li><i><b>;</b></i>: The On-Us character. Will be replaced by <b><i>o</i></b> in property RawData due to
 *     UPOS convention,</li>
 *     <li><i>ssss</i>: The 4 digit special information field, for example a cheque count,</li>
 *     <li>LF: A newline character (0Ah), used as frame terminator.</li>
 * </ul>
 */
public class MICR extends MICRProperties {
    private final SampleMICR.Device Dev;

    /**
     * The constructor. Gets the Device object that implements the device communication as parameter. The
     * device index used by the sample device is always 0.
     * @param dev Instance of Device this object belongs to.
     */
    public MICR(SampleMICR.Device dev) {
        super(0);
        Dev = dev;
    }
    @Override
    public void claim(int timeout) throws JposException {
        JposException e = Dev.initPort();
        if (timeout < Dev.MinClaimTimeout)
            timeout = Dev.MinClaimTimeout;
        if (e != null)
            throw e;
        super.claim(timeout);
        (Dev.CommandProcessor = new ThreadHandler("MICRCommandProcessor", Dev)).start();
    }

    @Override
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    public void release() throws JposException {
        Dev.CommandProcessor.ToBeFinished = true;
        if (Dev.Target != null)
            Dev.closePort();
        else
            Dev.PollWaiter.signal();
        Dev.CommandProcessor.waitFinished();
        super.release();
    }

    @Override
    public void handlePowerStateOnEnable() throws JposException {
        PowerState = Dev.Offline == JPOS_SUE_POWER_OFF_OFFLINE ? JPOS_PS_OFF_OFFLINE : JPOS_PS_ONLINE;
        super.handlePowerStateOnEnable();
    }

    @Override
    public void checkHealth(int level) throws JposException {
        if (level == JPOS_CH_INTERNAL) {
            CheckHealthText = "Internal CheckHealth: ";
            CheckHealthText += Dev.Offline == JPOS_SUE_POWER_OFF_OFFLINE ? "Failed" : "OK";
        }
        else {
            CheckHealthText = level == JPOS_CH_EXTERNAL ? "External" : "Interactive";
            CheckHealthText += " Checkhealth: Failed (Not supported)";
        }
        EventSource.logSet("CheckHealthText");
        super.checkHealth(level);
    }

    @Override
    public void beginInsertion(int timeout) throws JposException {
        long starttime = System.currentTimeMillis();
        do {
            if (!Dev.connectionOffline()) {
                return;
            }
            new SyncObject().suspend(Dev.PollDelay);
        } while (timeout == JPOS_FOREVER || System.currentTimeMillis() - starttime < timeout);
        throw new JposException(JPOS_E_FAILURE, "No connection to MICR device");
    }

    @Override
    public void endInsertion() throws JposException {
        check(!Dev.sendCommand("I"), JPOS_E_FAILURE, "No connection to MICR device");
    }

    @Override
    public void beginRemoval(int timeout) throws JposException {
        long starttime = System.currentTimeMillis();
        do {
            if (Dev.sendCommand("R"))
                return;
            new SyncObject().suspend(Dev.PollDelay);
        } while (System.currentTimeMillis() - starttime < timeout);
        throw new JposException(JPOS_E_FAILURE, "No connection to MICR device");
    }
}
