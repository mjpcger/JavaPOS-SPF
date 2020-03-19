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

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.toneindicator.*;
import jpos.*;

import javax.swing.*;

/**
 * Class implementing the POSKeyboardInterface for the sample combi device.
 * External and interactive Checkhealth might be implemented in a later version.
 */
public class ToneIndicator extends ToneIndicatorProperties {
    private Device Dev;

    /**
     * Constructor. Gets instance of Device to be used as communication object. Device index for
     * sample is always 0.
     * @param dev Instance of Device this object belongs to.
     */
    public ToneIndicator(Device dev) {
        super(0);
        Dev = dev;
    }

    @Override
    public void deviceEnabled(boolean enable) throws JposException {
        super.deviceEnabled(enable);
        Dev.updateCommonStates(this, enable);
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
        if (!Dev.internalCheckHealth(this, level) || !externalCheckHealth(level)) {
            interactiveCheckHealth(level);
        }
        super.checkHealth(level);
    }

    private void interactiveCheckHealth(int level) {
        if (level == JposConst.JPOS_CH_INTERACTIVE) {
            String result;
            try {
                ((ToneIndicatorService) EventSource).soundImmediate();
                result = "OK";
            } catch (JposException e) {
                result = "Error, " + e.getMessage();
            }
            Dev.synchronizedMessageBox("ToneIndicator check " + result + ".", "CheckHealth ToneIndicator",
                    (result.equals("OK") ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE));
            CheckHealthText = "Interactive check: " + result;
        }
    }

    private boolean externalCheckHealth(int level) {
        if (level == JposConst.JPOS_CH_EXTERNAL) {
            String result;
            try {
                ((ToneIndicatorService) EventSource).soundImmediate();
                result = "OK";
            } catch (JposException e) {
                result = "Error, " + e.getMessage();
            }
            CheckHealthText = "External check: " + result;
            return true;
        }
        return false;
    }

    @Override
    public void sound(Sound request) throws JposException {
        if ((Tone1Duration | Tone2Duration) != 0) {
            while (request.Count == JposConst.JPOS_FOREVER || request.Count-- > 0) {
                if (soundAndDelay(request, Tone1Duration, InterToneWait))
                    break;
                if (soundAndDelay(request, Tone2Duration, request.Delay))
                    break;
            }
        }
    }

    private boolean soundAndDelay(JposOutputRequest request, int duration, int delay) throws JposException {
        ToneIndicatorProperties props = (ToneIndicatorProperties)request.Props;
        if (request.Abort != null) {
            JposCommonProperties claimer = props.getClaimingInstance();
            if (request.EndSync != null && claimer != null && claimer != props)
                throw new JposException(JposConst.JPOS_E_CLAIMED, "Claimed by other instance");
            return true;
        }
        if (duration > 0) {
            Dev.sendCommand(Dev.CmdBeepOn, Dev.NoResponse);
            request.Waiting.suspend(duration);
            if (request.Abort != null)
                return true;
            Dev.sendCommand(Dev.CmdBeepOff, Dev.NoResponse);
            if (request.Abort != null)
                return true;
        }
        if (delay > 0) {
            request.Waiting.suspend(delay);
        }
        return false;
    }
}
