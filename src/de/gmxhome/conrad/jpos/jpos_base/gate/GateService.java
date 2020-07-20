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
 */

package de.gmxhome.conrad.jpos.jpos_base.gate;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.services.GateService114;

/**
 * Gate service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class GateService extends JposBase implements GateService114 {
    /**
     * Instance of a class implementing the GateInterface for gate specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public GateInterface GateInterface;

    private GateProperties Data;

    /**
     * Constructor. Stores property set and device implementation object.
     *
     * @param props  Device service property set
     * @param device Device implementation object
     */
    public GateService(GateProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public boolean getCapGateStatus() throws JposException {
        checkOpened();
        logGet("CapGateStatus");
        return Data.CapGateStatus;
    }

    @Override
    public boolean getCapRealTimeData() throws JposException {
        return false;   // Not specified in UPOS specification and therefore always false.
    }

    @Override
    public int getGetStatus() throws JposException {
        return getGateStatus();
    }

    public int getGateStatus() throws JposException {
        checkEnabled();
        logGet("GateStatus");
        return Data.GateStatus;
    }

    @Override
    public void openGate() throws JposException {
        logPreCall("OpenGate");
        checkEnabledUnclaimed();
        GateInterface.openGate();
        logCall("OpenGate");
    }

    @Override
    public void waitForGateClose(int timeout) throws JposException {
        logPreCall("WaitForGateClose", "" + timeout);
        checkEnabledUnclaimed();
        if (!Data.CapGateStatus)
            return;
        Device.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_CLOSED, "Negative timeout");
        GateInterface.waitForGateClose(timeout);
        logCall("WaitForGateClose");
    }
}
