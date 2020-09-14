/*
 * Copyright 2020 Martin Conrad
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

package SampleUdpDevice;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.cashdrawer.*;
import jpos.CashDrawerConst;
import jpos.JposConst;
import jpos.JposException;
import jpos.config.JposEntry;
import org.apache.log4j.Level;

import javax.swing.*;

/**
 * Implementation of a JposDevice based implementation for a sample implemented in SampleUdpDevice.tcl that becomes
 * a JavaPOS CashDrawer device services in combination with the CashDrawerServer class.
 * The simulator supports the following commands:<ul>
 *     <li/> 'S': Status request. Sends back '0' if drawer is closed, otherwise '1'.
 *     <li/> 'O': Opens the drawer. Sends back the new status which is always '1'.
 * </ul>
 * All frames have a length of 1 byte. Communication will be made with UDP sockets.<br>
 * Jpos property <i>Port</i> will be used to specify the target socket (ip:port).<br>
 * Jpos property <i>OwnPort</i> can be used to specify the port used by the simulator. If not specified, a random port
 * will be used.<br>
 * Jpos property <i>UseClientIO</i> can be set to false to force use of UdpIOProcessor. If not specified or true,
 * UdpClientIOProcessor will be used.
 */
public class Device extends JposDevice implements Runnable {
    /**
     * Constructor. id specifies the server to be connected in format host:port.
     *
     * @param id IPv4 target address and port
     */
    protected Device(String id) {
        super(id);
        cashDrawerInit(1);
        PhysicalDeviceDescription = "UDP device simulator";
        PhysicalDeviceName = "UDP device Simulator";
        CapPowerReporting = JposConst.JPOS_PR_STANDARD;
    }

    private int OwnPort = 0;
    private int PollDelay = 200;
    private int RequestTimeout = 100;
    private int MaxRetry = 2;
    private boolean UseClientIO = true;

    @Override
    public void checkProperties(JposEntry entry) throws JposException {
        super.checkProperties(entry);
        try {
            Object o;
            if ((o = entry.getPropertyValue("OwnPort")) != null) {
                if ((OwnPort = Integer.parseInt(o.toString())) < 0 || OwnPort >= 0xffff)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid source port.");
            }
            if ((o = entry.getPropertyValue("PollDelay")) != null) {
                if ((PollDelay = Integer.parseInt(o.toString())) <= 0)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid poll delay: " + PollDelay);
            }
            if ((o = entry.getPropertyValue("RequestTimeout")) != null) {
                if ((RequestTimeout = Integer.parseInt(o.toString())) <= 0)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid request timeout: " + RequestTimeout);
            }
            if ((o = entry.getPropertyValue("MaxRetry")) != null) {
                if ((MaxRetry = Integer.parseInt(o.toString())) <= 0)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid maximum retry count: " + MaxRetry);
            }
            if ((o = entry.getPropertyValue("UseClientIO")) != null) {
                UseClientIO = Boolean.parseBoolean(o.toString());
            }
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property", e);
        }
    }

    @Override
    public void changeDefaults(CashDrawerProperties props) {
        props.DeviceServiceDescription = "CashDrawer service for sample UDP device simulator";
        props.DeviceServiceVersion = 1014001;
    }

    private UniqueIOProcessor Target = null;
    private boolean Offline = true;

    private JposException initPort() {
        try {
            if (UseClientIO)
                ((UdpClientIOProcessor) (Target = new UdpClientIOProcessor(this, ID))).setParam(OwnPort);
            else {
                ((UdpIOProcessor) (Target = new UdpIOProcessor(this))).setParameters(OwnPort, Integer.MAX_VALUE);
                Target.setTarget(ID);
            }
            Target.open(Offline);
            Offline = false;
        } catch (JposException e) {
            Target = null;
            return e;
        }
        return null;
    }

    private JposException closePort() {
        JposException e = null;
        if (Target != null) {
            for (int i = 0; i < 2; i++) {
                try {
                    Target.close();
                } catch (JposException ee) {
                    e = ee;
                }
            }
            Target = null;
        }
        return e;
    }

    private boolean connectionOffline() {
        if (Target == null) {
            JposException e = initPort();
            if (e != null)
                return Offline = true;
        }
        return false;
    }

    private synchronized String sendResp(String command) {
        if (connectionOffline())
            return null;
        try {
            byte[] request = command.getBytes();
            Target.flush();
            Target.setTimeout(RequestTimeout);
            for (int count = 0; count < MaxRetry; count++) {
                Target.write(request);
                byte[] response = Target.read(2);
                if (response.length == 1 && (response[0] == '1' || response[0] == '0'))
                    return new String(response);
            }
        } catch (JposException e) {
            log(Level.TRACE, ID + ": IO error: " + e.getMessage());
        }
        Offline = true;
        closePort();
        return null;
    }

    private boolean DrawerOpen = false;
    private boolean ToBeFinished = false;
    private SyncObject PollWaiter = new SyncObject();

    @Override
    public void run() {
        while (!ToBeFinished) {
            boolean oldoffline = Offline;
            boolean oldopen = DrawerOpen;
            prepareSignalStatusWaits(CashDrawers[0]);
            String resp = sendResp("S");
            if (resp != null)
                DrawerOpen = resp.charAt(0) == '1';
            if (oldoffline && oldoffline != Offline)
                sendPowerStateEvents();
            if (oldopen != DrawerOpen && !Offline) {
                sendDrawerStateEvent();
                signalStatusWaits(CashDrawers[0]);
            }
            if (Offline && oldoffline != Offline) {
                sendPowerStateEvents();
                signalStatusWaits(CashDrawers[0]);
            }
            if (StartPollingWaiter != null) {
                StartPollingWaiter.signalWaiter();
                StartPollingWaiter = null;
            }
            PollWaiter.suspend(PollDelay);
        }
    }

    private JposException sendDrawerStateEvent() {
        try {
            JposCommonProperties props = getPropertySetInstance(CashDrawers, 0, 0);
            if (props != null) {
                int state = DrawerOpen ? CashDrawerConst.CASH_SUE_DRAWEROPEN : CashDrawerConst.CASH_SUE_DRAWERCLOSED;
                handleEvent(new CashDrawerStatusUpdateEvent(props.EventSource, state));
            }
        } catch (JposException e) {
            return e;
        } catch (IndexOutOfBoundsException e) {}
        return null;
    }

    private void sendPowerStateEvents() {
        int state = Offline ? JposConst.JPOS_SUE_POWER_OFF_OFFLINE : JposConst.JPOS_SUE_POWER_ONLINE;
        try {
            JposCommonProperties props = getPropertySetInstance(CashDrawers, 0, 0);
            if (props != null) {
                handleEvent(new CashDrawerStatusUpdateEvent(props.EventSource, state));
            }
        } catch (JposException e) {}
    }

    private int[] OpenCount = { 0 };

    private synchronized int changeOpenCount(int value) {
        OpenCount[0] += value;
        return OpenCount[0];
    }

    private JposCommonProperties StartPollingWaiter = null;
    private Thread StateWatcher;

    // Method to start communication
    private int startPolling(JposCommonProperties props) {
        synchronized (OpenCount) {
            if (OpenCount[0] == 0) {
                ToBeFinished = false;
                PollWaiter = new SyncObject();
                (StartPollingWaiter = props).attachWaiter();
                (StateWatcher = new Thread(this)).start();
                StateWatcher.setName(ID + "/StatusUpdater");
                OpenCount[0] = 1;
                props.waitWaiter(MaxRetry * RequestTimeout * 3);
                props.releaseWaiter();
            }
            else
                OpenCount[0] = OpenCount[0] + 1;
            return OpenCount[0];
        }
    }

    // Method to stop communication
    private int stopPolling() {
        synchronized(OpenCount) {
            if (OpenCount[0] == 1) {
                ToBeFinished = true;
                PollWaiter.signal();
                while (true) {
                    try {
                        StateWatcher.join();
                        break;
                    } catch (InterruptedException e) {}
                }
                StartPollingWaiter = null;
                closePort();
            }
            if (OpenCount[0] > 0)
                OpenCount[0] = OpenCount[0] - 1;
            return OpenCount[0];
        }
    }

    class SampleUdpDeviceCashDrawerAccessor extends CashDrawerProperties {
        /**
         * Constructor. Uses device index 0 implicitly because sample implementation supports only one cash drawer.
         */
        public SampleUdpDeviceCashDrawerAccessor() {
            super(0);
        }

        @Override
        public void initOnEnable(boolean enable) {
            if (enable) {
                DrawerOpened = DrawerOpen;
            }
        }

        @Override
        public void handlePowerStateOnEnable() throws JposException {
            handleEvent(new JposStatusUpdateEvent(EventSource, Offline ? JposConst.JPOS_SUE_POWER_OFF_OFFLINE : JposConst.JPOS_SUE_POWER_ONLINE));
        }

        @Override
        public void deviceEnabled(boolean enable) throws JposException {
            if (enable) {
                if (!Claimed) {
                    startPolling(this);
                    if (Offline && PowerNotify == JposConst.JPOS_PN_DISABLED) {
                        stopPolling();
                        throw new JposException(JposConst.JPOS_E_OFFLINE, "Communication with device disrupted");
                    }
                }
            } else {
                if (!Claimed)
                    stopPolling();
                signalWaiter();
            }
            super.deviceEnabled(enable);
        }

        @Override
        public void claim(int timeout) throws JposException {
            startPolling(this);
            if (Offline && PowerNotify == JposConst.JPOS_PN_DISABLED) {
                stopPolling();
                throw new JposException(JposConst.JPOS_E_OFFLINE, "Communication with device disrupted");
            }
            super.claim(timeout);
        }

        @Override
        public void release() throws JposException {
            super.release();
            stopPolling();
        }

        @Override
        public void checkHealth(int level) throws JposException {
            String how = level == JposConst.JPOS_CH_INTERNAL ? "Internal" : (level == JposConst.JPOS_CH_EXTERNAL ? "External" : "Interactive");
            if (Offline)
                CheckHealthText = how + " Checkhealth: Offline";
            else {
                CheckHealthText = how + " Checkhealth: OK";
                if (level != JposConst.JPOS_CH_INTERNAL) {
                    boolean interactive;
                    if (interactive = (level == JposConst.JPOS_CH_INTERACTIVE))
                        synchronizedMessageBox("Press OK to start health test.", "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
                    try {
                        openDrawer();
                        if (!DrawerOpened)
                            CheckHealthText = how + "Checkhealth: Drawer open failed";
                    } catch (JposException e) {
                        CheckHealthText = how + "Checkhealth: Offline";
                    }
                    if (interactive)
                        synchronizedMessageBox("CheckHealth result:\n" + CheckHealthText, "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }

        @Override
        public void openDrawer() throws JposException {
            attachWaiter();
            String state = sendResp("O");
            if (!DrawerOpened) {
                PollWaiter.signal();
                waitWaiter(RequestTimeout * MaxRetry);
            }
            releaseWaiter();
            check(state == null, JposConst.JPOS_E_OFFLINE, "Communication failure");
            super.openDrawer();
        }

        @Override
        public void waitForDrawerClose() throws JposException {
            attachWaiter();
            while (DrawerOpen && !Offline && DeviceEnabled)
                waitWaiter(SyncObject.INFINITE);
            releaseWaiter();
            check(Offline, JposConst.JPOS_E_OFFLINE, "Device offline");
            check(!DeviceEnabled, JposConst.JPOS_E_ILLEGAL, "Device not enabled");
            super.waitForDrawerClose();
        }
    }

    @Override
    public CashDrawerProperties getCashDrawerProperties(int index) {
        return new SampleUdpDeviceCashDrawerAccessor();
    }
}
