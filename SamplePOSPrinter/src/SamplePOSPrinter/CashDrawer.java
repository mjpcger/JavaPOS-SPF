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

package SamplePOSPrinter;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.cashdrawer.*;
import jpos.*;

import javax.swing.*;
import java.awt.*;

/**
 * Class implementing the CashDrawerInterface for the sample pos printer.
 */
class CashDrawer extends CashDrawerProperties {
    private SamplePOSPrinter.Device Dev;

    /**
     * Constructor. Gets instance of Device to be used as communication object. Device index for
     * sample is always 0.
     * @param dev Instance of Device this object belongs to.
     */
    CashDrawer(SamplePOSPrinter.Device dev) {
        super(0);
        Dev = dev;
    }

    @Override
    public void deviceEnabled(boolean enable) throws JposException {
        super.deviceEnabled(enable);
        updateStates(enable);
    }

    private void updateStates(boolean enable) {
        Dev.updateCommonStates(this, enable);
        if (enable) {
            DrawerOpened = Dev.DrawerOpen;
        }
    }

    @Override
    public void open() throws JposException {
        initOnOpen();
        Dev.startCommunication((int) SyncObject.INFINITE);
        State = JposConst.JPOS_S_IDLE;
    }

    @Override
    public void close() throws JposException {
        super.close();
        Dev.stopCommunication();
    }

    @Override
    public void checkHealth(int level) throws JposException {
        String healthError = "";

        if (level == JposConst.JPOS_CH_INTERACTIVE) {
            Dev.synchronizedMessageBox("Press OK to start health test.", "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
        }
        try {
            if (level != JposConst.JPOS_CH_INTERNAL) {
                if (level == JposConst.JPOS_CH_INTERACTIVE) {
                    healthError = "Interactive CheckHealth: ";
                    ((CashDrawerService)EventSource).openDrawer();
                    if (DrawerOpened) {
                        healthError += "Opened ";
                        Dev.synchronizedMessageBox("Close drawer and Press OK to stop health test.", "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
                        healthError += DrawerOpened ? "Failed " : "Closed ";
                    }
                    else
                        healthError += "Failed ";
                } else {
                    healthError = "External CheckHealth: ";
                    ((CashDrawerService)EventSource).openDrawer();
                    healthError += "Opened ";
                    ((CashDrawerService)EventSource).waitForDrawerClose(5000, 500, 450, 200);
                    healthError += "Closed ";
                }
            }
            else
                healthError = "Internal CheckHealth: ";
        } catch (JposException e) {
            healthError += "Failed ";
        }
        CheckHealthText = healthError + (healthError.matches(".*Fail.*") ? "ERROR." : "OK.");
        EventSource.logSet("CheckHealthText");
        super.checkHealth(level);
    }


    @Override
    public void openDrawer() throws JposException {
        if (!Dev.Online)
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "CashDrawer not accessible");
        Dev.sendCommand(Dev.CmdDrawerOpen);
        Dev.PollWaiter.signal();
        Dev.waitStatusChange(true, Dev.RequestTimeout);
        DrawerOpened = Dev.DrawerOpen;
        super.openDrawer();
    }

    @Override
    public void waitForDrawerClose(int beepTimeout, int beepFrequency, int beepDuration, int beepDelay) throws JposException {
        int timeout = beepTimeout;
        boolean beeping = false;

        if (Dev.DrawerOpen) {
            while (!Dev.waitStatusChange(false, timeout)) {
                if (beeping) {
                    timeout = beepDelay;
                } else {
                    timeout = beepDuration;
                }
                if (beeping = !beeping) {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        }
        super.waitForDrawerClose(beepTimeout, beepFrequency, beepDuration, beepDelay);
    }
}
