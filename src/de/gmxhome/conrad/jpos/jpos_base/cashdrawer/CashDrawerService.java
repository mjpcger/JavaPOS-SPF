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

package de.gmxhome.conrad.jpos.jpos_base.cashdrawer;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.services.CashDrawerService114;

/**
 * CashDrawer service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class CashDrawerService extends JposBase implements CashDrawerService114, Runnable {
    /**
     * Instance of a class implementing the CashDrawerInterface for cash drawer specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public CashDrawerInterface CashDrawerInterface;

    private CashDrawerProperties Data;
    private MySoundPlayer Sound;

    /**
     * Constructor. Stores property set and device implementation object.
     *
     * @param props  Device service property set
     * @param device Device implementation object
     */
    public CashDrawerService(CashDrawerProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public boolean getCapStatusMultiDrawerDetect() throws JposException {
        checkOpened();
        logGet("CapStatusMultiDrawerDetect");
        return Data.CapStatusMultiDrawerDetect;
    }

    @Override
    public boolean getCapStatus() throws JposException {
        checkOpened();
        logGet("CapStatus");
        return Data.CapStatus;
    }

    @Override
    public boolean getDrawerOpened() throws JposException {
        checkEnabled();
        logGet("DrawerOpened");
        return Data.DrawerOpened;
    }

    @Override
    public void openDrawer() throws JposException {
        logPreCall("OpenDrawer");
        checkEnabledUnclaimed();
        CashDrawerInterface.openDrawer();
        logCall("OpenDrawer");
    }

    private class MySoundPlayer extends SoundPlayer {
        private int BeepTimeout;
        private int BeepFrequency;
        private int BeepDuration;
        private int BeepDelay;

        MySoundPlayer(int beepTimeout, int beepFrequency, int beepDuration, int beepDelay) {
            super(Data.LogicalName);
            BeepTimeout = beepTimeout;
            BeepFrequency = beepFrequency;
            BeepDuration = beepDuration;
            BeepDelay = beepDelay;
        }
    }

    @Override
    public void waitForDrawerClose(int beepTimeout, int beepFrequency, int beepDuration, int beepDelay) throws JposException {
        logPreCall("WaitForDrawerClose", "" + beepTimeout + ", " + beepFrequency + ", " + beepDuration + ", " + beepDelay);
        checkEnabledUnclaimed();
        if (!Data.CapStatus)
            return;
        Device.check(beepTimeout < 0 && beepTimeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_CLOSED, "Negative beep timeout");
        Device.checkRange(beepFrequency, 10, 24000 , JposConst.JPOS_E_CLOSED, "beep frequency out of range: " + beepFrequency);
        Device.check(beepDuration < 0 && beepDuration != JposConst.JPOS_FOREVER, JposConst.JPOS_E_CLOSED, "Negative beep duration");
        Device.check(beepDelay < 0 && beepDelay != JposConst.JPOS_FOREVER, JposConst.JPOS_E_CLOSED, "Negative beep delay");
        CashDrawerInterface.waitForDrawerClose(beepTimeout, beepFrequency, beepDuration, beepDelay);
        if (Data.DrawerOpened) {
            Sound = new MySoundPlayer(beepTimeout, beepFrequency, beepDuration, beepDelay);
            new Thread(this, Data.LogicalName + ".WaitForDrawerCloseHandler").start();
            try {
                CashDrawerInterface.waitForDrawerClose();
            } finally {
                synchronized (this) {
                    if (Sound != null) {
                        Sound.clear();
                        Sound = null;
                    }
                }
            }
        }
        logCall("WaitForDrawerClose");
    }

    /**
     * Beeper thread: Waits at least 10 milliseconds, then starts beeping as requested until stopped. To stop the
     * thread, call Sound.clear() and set Sound = null.
     */
    @Override
    public void run() {
        try {
            if (Sound.BeepTimeout != JposConst.JPOS_FOREVER) {
                Thread.sleep(Sound.BeepTimeout > 10 ? Sound.BeepTimeout : 10);
                while (true) {
                    Sound.startSound(Sound.BeepFrequency, Sound.BeepDuration, Device.DrawerBeepVolume);
                    Sound.waitFinished();
                    Thread.sleep(Sound.BeepDelay);
                }
            }
        }
        catch (Exception e) {}
    }
}
