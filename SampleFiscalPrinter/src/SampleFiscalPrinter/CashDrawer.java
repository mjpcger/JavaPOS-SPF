/*
 * Copyright 2019 Martin Conrad
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
 */

package SampleFiscalPrinter;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.cashdrawer.*;
import jpos.CashDrawerConst;
import jpos.JposConst;
import jpos.JposException;

import javax.swing.*;
import java.awt.*;

import static SampleFiscalPrinter.Device.*;

/**
 * Class implementing the CashDrawerInterface for the sample fiscal printer.
 */
class CashDrawer extends CashDrawerProperties implements StatusUpdater {
    private SampleFiscalPrinter.Device Dev;

    /**
     * Constructor. Gets instance of Device to be used as communication object. Device index
     * for sample is always 0.
     * @param dev Instance of Device this object belongs to.
     */
    CashDrawer(SampleFiscalPrinter.Device dev) {
        super(0);
        Dev = dev;
    }
    @Override
    public void deviceEnabled(boolean enable) throws JposException {
        super.deviceEnabled(enable);

        Dev.updateStates(this, enable);
    }

    @Override
    public void open() throws JposException {
        super.open();
        Dev.startPolling();
    }

    @Override
    public void close() throws JposException {
        super.close();
        Dev.stopPolling();
    }

    @Override
    public void checkHealth(int level) throws JposException {
        String healthError = "";

        if (level == JposConst.JPOS_CH_INTERNAL) {
            CheckHealthText = "Internal CheckHealth: OK";
            return;
        }
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
        String[] resp = Dev.sendrecv(new String[]{"openDrawer"});
        Dev.check(resp == null || resp.length < 1, JposConst.JPOS_E_FAILURE, "Communication error");
        Dev.check(resp[0].charAt(0) != SUCCESS, JposConst.JPOS_E_FAILURE, resp.length != 3 ? "Unknown printer error" : "Error " + resp[1] + " [" + resp[2] + "]");
        Dev.getStatusWaitObj().suspend(1000000);
    }

    @Override
    public void waitForDrawerClose(int beepTimeout, int beepFrequency, int beepDuration, int beepDelay) throws JposException {
        int timeout = beepTimeout;
        boolean beeping = false;
        SyncObject waiter = Dev.getDrawerClosedWaiter();
        char[] actstate = Dev.getCurrentState();

        if (actstate.length > DRAWER && actstate[DRAWER] != CLOSED) {
            while (!waiter.suspend(timeout)) {
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

    @Override
    public void updateState(boolean notused) {
        char[] state = Dev.getCurrentState();
        if (PowerNotify == JposConst.JPOS_PN_ENABLED) {
            int value = state.length <= DRAWER ? JposConst.JPOS_PS_OFF_OFFLINE : JposConst.JPOS_PS_ONLINE;
            new JposStatusUpdateEvent(EventSource, value).setAndCheckStatusProperties();
        }
        if (state.length > DRAWER) {
            int value = state.length >= DRAWER && state[DRAWER] == OPENED ? CashDrawerConst.CASH_SUE_DRAWEROPEN : CashDrawerConst.CASH_SUE_DRAWERCLOSED;
            new CashDrawerStatusUpdateEvent(EventSource, value).setAndCheckStatusProperties();
        }
    }
}
