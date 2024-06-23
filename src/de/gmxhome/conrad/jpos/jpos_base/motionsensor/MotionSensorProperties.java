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

/**
 * Class containing the motion sensor specific properties, their default values and default implementations of
 * MotionSensorInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Motion Sensor.
 */
public class MotionSensorProperties extends JposCommonProperties implements MotionSensorInterface {
    /**
     * UPOS property Motion. Must be overwritten
     * by derived objects within the initOnEnable method.
     */
    public boolean Motion = false;

    /**
     * UPOS property Timeout.
     */
    public int Timeout = 0;

    /**
     * StatusUpdateEvent to be delivered Timeout milliseconds after it has been fired.
     */
    public MotionSensorStatusUpdateEvent BufferedAbsent;

    /**
     * Synchronization object used by MotionSensor to wait until Timeout milliseconds have passed.
     */
    public SyncObject TimeoutWaiter = new SyncObject();

    /**
     * Constructor. Sets ExclusiveUse to ExclusiveNo to match the MotionSensor device model.
     *
     * @param dev Device index
     */
    public MotionSensorProperties(int dev)
    {
        super(dev);
        ExclusiveUse = ExclusiveNo;
    }

    @Override
    public void initOnOpen() {
        super.initOnOpen();
    }

    @Override
    public void timeout(int timeout) throws JposException {
        Timeout = timeout;
    }

    @Override
    public void waitForMotion(int timeout) throws JposException {
    }
}
