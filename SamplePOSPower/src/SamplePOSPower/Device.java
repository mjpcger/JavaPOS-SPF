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

package SamplePOSPower;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.pospower.*;
import jpos.*;
import jpos.config.JposEntry;

import javax.swing.*;
import java.io.IOException;

/**
 * JposDevice based implementation of JavaPOS POSPower device service implementation for the battery power devices in
 * Windows laptops, notebooks and tablets.
 * <p>This implementation uses the following WIN32 API functions:
 * <ul>
 *     <li>GetSystemPowerStatus, </li>
 *     <li>SetSuspendState</li>
 * </ul>and the Windows program shutdown.exe.
 * <p>Here a full list of all device specific properties that can be changed via jpos.xml:
 * <ul>
 *     <li>PollDelay: Minimum time between status requests, in milliseconds. Status requests will be used to monitor the
 *     device state. Default: 1000.</li>
 *     <li>SecondsToFinish: Number of seconds between shutdown activation and shutdown. Will be used with command line
 *     parameter "-t" of the shutdown command. Default: 1.</li>
 * </ul>
 */
public class Device extends JposDevice implements Runnable {
    private static interface Kernel32Ext extends Kernel32 {
        public boolean GetSystemPowerStatus(SYSTEM_POWER_STATUS lpSystemPowerStatus);

        @Structure.FieldOrder({"ACLineStatus", "BatteryFlag", "BatteryLifePercent", "SystemStatusFlag", "BatteryLifeTime", "BatteryFullLifeTime"})
        public static class SYSTEM_POWER_STATUS extends Structure {
            public byte ACLineStatus;
            public byte BatteryFlag;
            public byte BatteryLifePercent;
            public byte SystemStatusFlag;
            public int BatteryLifeTime;
            public int BatteryFullLifeTime;
        }
    }

    private static interface PowrProf extends StdCallLibrary {
        public boolean SetSuspendState(boolean hibernate, boolean force, boolean wakeupEventsDisabled);
    }

    private static final byte ACLineStatusOffline = 0;
    private static final byte ACLineStatusOnline = 1;
    private static final byte ACLineStatusUnknown = ~0;

    private static final byte BatteryFlagHigh = 1;
    private static final byte BatteryFlagLow = 2;
    private static final byte BatteryFlagCritical = 4;
    private static final byte BatteryFlagCharging = 8;
    private static final byte BatteryFlagNoBattery = -128;
    private static final byte BatteryFlagUnknownState = ~0;

    private static final byte BatteryLifePercentUnknown = ~0;

    private static final byte SystemStatusFlagBatterySaverOn = 1;

    static Kernel32Ext  Kernel32Lib;
    static PowrProf PowrProfLib;

    static {
        try {
            PowrProfLib = null;
            Kernel32Lib = null;
            if (System.getProperty("os.name").startsWith("Windows")) {
                PowrProfLib = (PowrProf) Native.load("PowrProf", PowrProf.class, W32APIOptions.DEFAULT_OPTIONS);
                Kernel32Lib = (Kernel32Ext) Native.load("kernel32", Kernel32Ext.class, W32APIOptions.DEFAULT_OPTIONS);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static boolean ToBeFinished = false;
    private boolean Operational = true;
    private static Integer PollDelay = null;
    private static int getPollDelay() {
        return PollDelay == null ? 1000 : PollDelay;
    }
    private static Integer SecondsToFinish = null;
    private static int getSecondsToFinish() {
        return SecondsToFinish == null ? 1 : SecondsToFinish;
    }
    private static SyncObject PollWaiter = new SyncObject();
    private static SyncObject StartWaiter = null;
    private Thread Poller = null;

    private static int RemainingBatteryCapacity = BatteryLifePercentUnknown;
    private static int POSPowerSource = POSPowerConst.PWR_SOURCE_NA;
    private static byte BatteryFlags = ~0;
    private static int ShutdownDelay;
    private static long NoACTick = 0;

    private static int OpenCount = 0;

    private long[] AllowedLow = { 0, 33 };      // Low threshold 33%, see Win32 documentation.
    private long[] AllowedCritical = { 0, 5 };  // Critical low threshold 5%, see Win32 documentation.

    public Device(String id) {
        super(id);
        pOSPowerInit(1);
        PhysicalDeviceDescription = "POSPower device simulator for Windows";
        PhysicalDeviceName = "POSPower Device Simulator";
        CapPowerReporting = JposConst.JPOS_PR_NONE;
    }

    @Override
    public void checkProperties(JposEntry entry) throws JposException {
        super.checkProperties(entry);
        try {
            Object o;
            int val;
            if ((o = entry.getPropertyValue("PollDelay")) != null && (PollDelay == null || Integer.parseInt(o.toString()) < PollDelay))
                PollDelay = Integer.parseInt(o.toString());
            if ((o = entry.getPropertyValue("SecondsToFinish")) != null && (SecondsToFinish == null || Integer.parseInt(o.toString()) > SecondsToFinish))
                SecondsToFinish = Integer.parseInt(o.toString());
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property", e);
        }
    }

    @Override
    public void run() {
        while (!ToBeFinished) {
            try {
                int oldcapacity = RemainingBatteryCapacity;
                int oldsource = POSPowerSource;
                byte oldflags = BatteryFlags;
                Kernel32Ext.SYSTEM_POWER_STATUS powerstate = new Kernel32Ext.SYSTEM_POWER_STATUS();
                updateBatteryCapacityAndPowerSource(powerstate);
                Operational = true;
                checkShutDown();
                handleStates(oldcapacity, oldsource, oldflags);
            } catch (Exception e) {
                e.printStackTrace();
                Operational = false;
            } finally {
                if (StartWaiter != null) {
                    StartWaiter.signal();
                    StartWaiter = null;
                }
                PollWaiter.suspend(getPollDelay());
            }
        }
    }

    private void checkShutDown() throws JposException {
        synchronized (Poller) {
            if (NoACTick != 0 && ShutdownDelay != 0 && System.currentTimeMillis() - NoACTick > ShutdownDelay) {
                NoACTick = 0;
                doShutdown("s");
                JposCommonProperties props = getPropertySetInstance(POSPowers, 0, 0);
                if (props != null)
                    handleEvent(new POSPowerStatusUpdateEvent(props.EventSource, POSPowerConst.PWR_SUE_SHUTDOWN));
            }
        }
    }

    private void doShutdown(String s) {
        Process process;
        try {
            if ("s".equals(s) || "r".equals(s))
                process = Runtime.getRuntime().exec("shutdown.exe -" + s + " -t " + getSecondsToFinish());
            else
                return;
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateBatteryCapacityAndPowerSource(Kernel32Ext.SYSTEM_POWER_STATUS powerstate) throws IOException {
        if (!Kernel32Lib.GetSystemPowerStatus(powerstate))
            throw new IOException("GetSystemPowerStatus error " + Kernel32Lib.GetLastError());
        synchronized (Poller) {
            if (powerstate.BatteryFlag == BatteryFlagUnknownState || (powerstate.BatteryFlag & BatteryFlagNoBattery) != 0) {
                RemainingBatteryCapacity = 0;
                POSPowerSource = POSPowerConst.PWR_SOURCE_NA;
                BatteryFlags = BatteryFlagCritical;
            } else {
                RemainingBatteryCapacity = powerstate.BatteryLifePercent;
                POSPowerSource = POSPowerConst.PWR_SOURCE_NA;
                switch (powerstate.ACLineStatus) {
                    case ACLineStatusOffline:
                        POSPowerSource = POSPowerConst.PWR_SOURCE_BATTERY;
                        if (NoACTick == 0)
                            NoACTick = System.currentTimeMillis();
                        break;
                    case ACLineStatusOnline:
                        POSPowerSource = POSPowerConst.PWR_SOURCE_AC;
                        NoACTick = 0;
                        break;
                }
                BatteryFlags = (byte) (powerstate.BatteryFlag & (BatteryFlagCritical & BatteryFlagLow));
            }
        }
    }

    private void handleStates(int oldcapacity, int oldsource, byte oldflags) throws JposException {
        JposCommonProperties props = getPropertySetInstance(POSPowers, 0, 0);
        if (props != null) {
            if (oldsource != POSPowerSource)
                handleEvent(new POSPowerStatusUpdateEvent(props.EventSource, POSPowerConst.PWR_SUE_PWR_SOURCE, POSPowerSource));
            if (oldcapacity != RemainingBatteryCapacity)
                handleEvent(new POSPowerStatusUpdateEvent(props.EventSource, POSPowerConst.PWR_SUE_BAT_CAPACITY_REMAINING, RemainingBatteryCapacity));
            if (oldflags != BatteryFlags) {
                if (((BatteryFlags & ~oldflags) & BatteryFlagCritical) != 0)
                    handleEvent(new POSPowerStatusUpdateEvent(props.EventSource, POSPowerConst.PWR_SUE_BAT_CRITICAL, 0));
                else if (((BatteryFlags & ~oldflags) & BatteryFlagLow) != 0)
                    handleEvent(new POSPowerStatusUpdateEvent(props.EventSource, POSPowerConst.PWR_SUE_BAT_LOW, 0));
            }
        }
    }

    @Override
    public void changeDefaults(POSPowerProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "POS Power service for sample Windows PC";
        props.CapBatteryCapacityRemaining = true;
        props.CapRestartPOS = true;
        props.CapSuspendPOS = true;
        props.CapStandbyPOS = true;
    }

    private int getModifiedOpenCount(boolean increment) {
        OpenCount += increment ? 1 : -1;
        return OpenCount;
    }

    @Override
    public POSPowerProperties getPOSPowerProperties(int index) {
        return new SamplePOSPowerProperties();
    }

    private class SamplePOSPowerProperties extends POSPowerProperties {
        SamplePOSPowerProperties() {
            super(0);
        }

        @Override
        public void initOnOpen() {
            super.initOnOpen();
            synchronized (Poller) {
                BatteryCapacityRemaining = RemainingBatteryCapacity;
                PowerSource = POSPowerSource;
            }
            BatteryLowThreshold = (byte)AllowedLow[1];
            BatteryCriticallyLowThreshold = (byte)AllowedCritical[1];
            EnforcedShutdownDelayTime = 0;
        }

        @Override
        public void open() throws JposException {
            int count;
            synchronized (Device) {
                if ((count = getModifiedOpenCount(true)) == 1) {
                    SyncObject waiter = StartWaiter = new SyncObject();
                    ToBeFinished = false;
                    Poller = new Thread((Device)Device);
                    Poller.setName("POSPower");
                    Poller.start();
                    waiter.suspend(SyncObject.INFINITE);
                }
            }
            check(!Operational, JposConst.JPOS_E_NOSERVICE, "Device not working");
            super.open();
        }

        @Override
        public void close() throws JposException {
            int count;
            synchronized (Device) {
                if ((count = getModifiedOpenCount(false)) == 0) {
                    ToBeFinished = true;
                    PollWaiter.signal();
                    try {
                        Poller.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            super.close();
        }

        @Override
        public void checkHealth(int level) throws JposException {
            String result = " checkHealth: " + (Operational ? "OK" : "Not operational");
            String typestr = "Internal";
            if (level == JposConst.JPOS_CH_EXTERNAL)
                typestr = "External";
            else if (level == JposConst.JPOS_CH_INTERACTIVE) {
                typestr = "Interactive";
                synchronizedMessageBox("CheckHealth result:\n" + typestr + result, "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
            }
            CheckHealthText = typestr + result;
        }

        @Override
        public void batteryLowThreshold(int threshold) throws JposException {
            checkMember(threshold, AllowedLow, JposConst.JPOS_E_ILLEGAL, "BatteryLowThreshold must be 0 or " + AllowedLow[1]);
            super.batteryLowThreshold(threshold);
        }

        @Override
        public void batteryCriticallyLowThreshold(int threshold) throws JposException {
            checkMember(threshold, AllowedCritical, JposConst.JPOS_E_ILLEGAL, "BatteryCriticallyLowThreshold must be 0 or " + AllowedCritical[1]);
            super.batteryCriticallyLowThreshold(threshold);
        }

        @Override
        public void enforcedShutdownDelayTime(int delay) throws JposException {
            synchronized (Device) {
                super.enforcedShutdownDelayTime(delay);
                int mindelay = Integer.MAX_VALUE;
                for (JposCommonProperties props : POSPowers[0]) {
                    int current = ((POSPowerProperties) props).EnforcedShutdownDelayTime;
                    if (current > 0 && current < mindelay)
                        mindelay = current;
                }
                if (mindelay == Integer.MAX_VALUE)
                    mindelay = 0;
                synchronized (Poller) {
                    ShutdownDelay = mindelay;
                }
            }
        }

        @Override
        public void restartPOS() throws JposException {
            doShutdown("r");
        }

        @Override
        public void shutdownPOS() throws JposException {
            doShutdown("s");
        }

        @Override
        public void standbyPOS(int reason) throws JposException {
            if (reason == POSPowerConst.PWR_REASON_REQUEST)
                PowrProfLib.SetSuspendState(false, true, false);
            else
                throw new JposException(JposConst.JPOS_E_ILLEGAL, "Not in current context (no suspend status update event)");
                        }

@Override
public void suspendPOS(int reason) throws JposException {
        if (reason == POSPowerConst.PWR_REASON_REQUEST)
        PowrProfLib.SetSuspendState(true, true, true);
        else
        throw new JposException(JposConst.JPOS_E_ILLEGAL, "Not in current context (no suspend status update event)");
        }
        }
        }
