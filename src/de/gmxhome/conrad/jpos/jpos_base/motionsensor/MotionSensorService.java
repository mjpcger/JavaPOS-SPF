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

package de.gmxhome.conrad.jpos.jpos_base.motionsensor;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.services.*;

/**
 * MotionSensor service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class MotionSensorService extends JposBase implements MotionSensorService115 {
    /**
     * Instance of a class implementing the MotionSensorInterface for motion sensor specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public MotionSensorInterface MotionSensorInterface;

    private MotionSensorProperties Data;

    /**
     * Constructor. Stores property set and device implementation object.
     *
     * @param props  Device service property set
     * @param device Device implementation object
     */
    public MotionSensorService(MotionSensorProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public boolean getMotion() throws JposException {
        checkEnabled();
        logGet("Motion");
        return Data.Motion;
    }

    @Override
    public int getTimeout() throws JposException {
        checkEnabled();
        logGet("Timeout");
        return Data.Timeout;
    }

    @Override
    public void setTimeout(int timeout) throws JposException {
        logPreSet("Timeout");
        checkEnabled();
        Device.check(timeout <= 0, JposConst.JPOS_E_ILLEGAL, "Motion timeout must be > 0");
        MotionSensorInterface.timeout(timeout);
        logSet("Timeout");
    }

    @Override
    public void waitForMotion(int timeout) throws JposException {
        logPreCall("waitForMotion", "" + timeout);
        checkEnabled();
        Device.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_CLOSED, "Negative timeout");
        MotionSensorInterface.waitForMotion(timeout);
        logCall("waitForMotion");
    }
}
