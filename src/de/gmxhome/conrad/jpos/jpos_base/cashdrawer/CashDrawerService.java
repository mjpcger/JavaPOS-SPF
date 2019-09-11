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
public class CashDrawerService extends JposBase implements CashDrawerService114 {
    /**
     * Instance of a class implementing the CashDrawerInterface for cash drawer specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public CashDrawerInterface CashDrawerInterface;

    private CashDrawerProperties Data;

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

    @Override
    public void waitForDrawerClose(int beepTimeout, int beepFrequency, int beepDuration, int beepDelay) throws JposException {
        logPreCall("WaitForDrawerClose", "" + beepTimeout + ", " + beepFrequency + ", " + beepDuration + ", " + beepDelay);
        checkEnabledUnclaimed();
        if (!Data.CapStatus)
            return;
        Device.check(beepTimeout < 0, JposConst.JPOS_E_CLOSED, "Negative beep timeout");
        Device.checkRange(beepFrequency, 10, 40000 , JposConst.JPOS_E_CLOSED, "beep frequency out of range: " + beepFrequency);
        Device.check(beepDuration < 0, JposConst.JPOS_E_CLOSED, "Negative beep duration");
        Device.check(beepDelay < 0, JposConst.JPOS_E_CLOSED, "Negative beep delay");
        CashDrawerInterface.waitForDrawerClose(beepTimeout, beepFrequency, beepDuration, beepDelay);
        logCall("WaitForDrawerClose");
    }
}
