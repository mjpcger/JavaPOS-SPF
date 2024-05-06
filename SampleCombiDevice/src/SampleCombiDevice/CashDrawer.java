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
import de.gmxhome.conrad.jpos.jpos_base.cashdrawer.*;
import jpos.*;
import static SampleCombiDevice.Device.*;
import static de.gmxhome.conrad.jpos.jpos_base.SyncObject.INFINITE;
import static javax.swing.JOptionPane.*;
import static jpos.JposConst.*;
/**
 * Class implementing the CashDrawerInterface for the sample combi device.
 */
public class CashDrawer extends CashDrawerProperties {
    private final Device Dev;
    private static final byte[] CmdDrawerOpen = {'D','O'};

    /**
     * Constructor. Gets instance of Device to be used as communication object. Device index for
     * sample is always 0.
     * @param dev Instance of Device this object belongs to.
     */
    public CashDrawer(Device dev) {
        super(0);
        Dev = dev;
    }

    @Override
    public void deviceEnabled(boolean enable) throws JposException {
        super.deviceEnabled(enable);
        Dev.updateCommonStates(this, enable);
        Dev.updateDrawerStates(this, enable);
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
        if (Dev.internalCheckHealth(this, level)) {
            try {
                do {
                    if (level == JPOS_CH_EXTERNAL) {
                        CheckHealthText = "External check: Error";
                        ((CashDrawerService) EventSource).openDrawer();
                        new SyncObject().suspend(200);
                        ((CashDrawerService) EventSource).waitForDrawerClose(5000, 500, 300, 2000);
                        CheckHealthText = "External check: OK";
                        break;
                    }
                } while (drawerCheckHealthInteractive());
            } catch (JposException e) {
                CheckHealthText += ", " + e.getMessage();
            }
        }
        super.checkHealth(level);
    }

    private boolean drawerCheckHealthInteractive() throws JposException {
        SyncObject waiter = new SyncObject();
        CheckHealthText = "Interactive check: Error";
        synchronizedMessageBox("Press OK to open the drawer", "CheckHealth Drawer", INFORMATION_MESSAGE);
        try {
            ((CashDrawerService) EventSource).openDrawer();
        } catch (JposException e) {
            CheckHealthText += ", " + e.getMessage();
            synchronizedMessageBox("Drawer could not be opened:\n" + e.getMessage(), "CheckHealth Drawer", ERROR_MESSAGE);
            return true;
        }
        for (int i = 0; i < 10 && !DrawerOpened; i++) {
            waiter.suspend(200);
            if (Dev.DrawerIsOpen)
                break;
        }
        if (!Dev.DrawerIsOpen) {
            synchronizedMessageBox("Drawer could not be opened", "CheckHealth Drawer", ERROR_MESSAGE);
            return true;
        }
        synchronizedMessageBox("Close drawer to finish drawer check", "CheckHealth Drawer", INFORMATION_MESSAGE);
        waiter.suspend(200);
        if (Dev.DrawerIsOpen) {
            synchronizedMessageBox("Drawer could not be closed", "CheckHealth Drawer", ERROR_MESSAGE);
            return true;
        }
        synchronizedMessageBox("Drawer check finished successfully", "CheckHealth Drawer", INFORMATION_MESSAGE);
        CheckHealthText = "Interactive check: OK";
        return false;
    }

    @Override
    public void openDrawer() throws JposException {
        int retry = 0;
        do {
            Dev.sendCommand(CmdDrawerOpen, NoResponse);
            if (Dev.sendCommand(CmdStatusRequest, RespFromStatus) == 0)
                continue;
            super.openDrawer();
            return;
        } while (++retry < Dev.MaxRetry);
        throw new JposException(JPOS_E_ILLEGAL, "No response on drawer open request");
    }

    @Override
    public void waitForDrawerClose() throws JposException {
        attachWaiter();
        while (DrawerOpened && !Dev.DeviceIsOffline && DeviceEnabled) {
            waitWaiter(INFINITE);
        }
        releaseWaiter();
        check(Dev.DeviceIsOffline, JPOS_E_OFFLINE, "Device offline");
        check(!DeviceEnabled, JPOS_E_ILLEGAL, "Device not enabled");
        super.waitForDrawerClose();
    }
}
