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

package de.gmxhome.conrad.jpos.jpos_base.belt;

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import jpos.BeltConst;
import jpos.JposException;

/**
 * Class containing the belt specific properties, their default values and default implementations of
 * BeltInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Belt.
 */
public class BeltProperties extends JposCommonProperties implements BeltInterface {
    /**
     * UPOS property AutoStopBackward. Default: false.
     */
    public boolean AutoStopBackward = false;

    /**
     * UPOS property AutoStopBackwardDelayTime. Default: 0.
     */
    public int AutoStopBackwardDelayTime = 0;

    /**
     * UPOS property AutoStopBackwardItemCount. Default: 0.
     */
    public int AutoStopBackwardItemCount = 0;

    /**
     * UPOS property AutoStopForward. Default: false.
     */
    public boolean AutoStopForward = false;

    /**
     * UPOS property AutoStopForwardDelayTime. Default: 0.
     */
    public int AutoStopForwardDelayTime = 0;

    /**
     * UPOS property AutoStopBackwardItemCount. Default: 0.
     */
    public int AutoStopForwardItemCount = 0;

    /**
     * UPOS property CapAutoStopBackward. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapAutoStopBackward = false;

    /**
     * UPOS property CapAutoStopBackwardItemCount. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapAutoStopBackwardItemCount = false;

    /**
     * UPOS property CapAutoStopForward. Default: true. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapAutoStopForward = true;

    /**
     * UPOS property CapAutoStopForwardItemCount. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapAutoStopForwardItemCount = false;

    /**
     * UPOS property CapLightBarrierBackward. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapLightBarrierBackward = false;

    /**
     * UPOS property CapLightBarrierForward. Default: true. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapLightBarrierForward = true;

    /**
     * UPOS property CapMoveBackward. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapMoveBackward = false;

    /**
     * UPOS property CapSecurityFlapBackward. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSecurityFlapBackward = false;

    /**
     * UPOS property CapSecurityFlapForward. Default: true. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSecurityFlapForward = true;

    /**
     * UPOS property CapSpeedStepsBackward. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapSpeedStepsBackward = 0;

    /**
     * UPOS property CapSpeedStepsForward. Default: 1. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapSpeedStepsForward = 1;

    /**
     * UPOS property LightBarrierBackwardInterrupted. Default: false. Must be
     * initialized and kept current while device is enabled.
     */
    public boolean LightBarrierBackwardInterrupted = false;

    /**
     * UPOS property LightBarrierForwardInterrupted. Default: false. Must be
     * initialized and kept current while device is enabled.
     */
    public boolean LightBarrierForwardInterrupted = false;

    /**
     * UPOS property MotionStatus. Default: MT_STOPPED.  Must be
     * initialized and kept current while device is enabled.
     */
    public int MotionStatus = BeltConst.BELT_MT_STOPPED;

    /**
     * UPOS property SecurityFlapBackwardOpened. Default: false.  Must be
     * initialized and kept current while device is enabled.
     */
    public boolean SecurityFlapBackwardOpened = false;

    /**
     * UPOS property SecurityFlapForwardOpened. Default: false.  Must be
     * initialized and kept current while device is enabled.
     */
    public boolean SecurityFlapForwardOpened = false;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected BeltProperties(int dev) {
        super(dev);
        ExclusiveUse = ExclusiveYes;
    }

    @Override
    public void autoStopBackward(boolean flag) {
        AutoStopBackward = flag;
    }

    @Override
    public void autoStopBackwardDelayTime(int delay) throws JposException {
        AutoStopBackwardDelayTime = delay;
    }

    @Override
    public void autoStopForward(boolean flag) throws JposException {
        AutoStopForward = flag;
    }

    @Override
    public void autoStopForwardDelayTime(int delay) throws JposException {
        AutoStopForwardDelayTime = delay;
    }

    /**
     * The default implementation increments the corresponding property, AutoStopBackwardItemCount or
     * AutoStopForwardItemCount, by the specified count.
     * @param direction   Affected direction, one of RIC_BACKWARD and RIC_FORWARD.
     * @param count       Item count to be added to the specified auto-stop property.
     * @throws JposException If an error occurs.
     */
    @Override
    public void adjustItemCount(int direction, int count) throws JposException {
        if (direction == BeltConst.BELT_AIC_BACKWARD)
            AutoStopBackwardItemCount += count;
        else
            AutoStopForwardItemCount += count;
    }

    @Override
    public void moveBackward(int speed) throws JposException {
    }

    @Override
    public void moveForward(int speed) throws JposException {
    }

    @Override
    public void resetBelt() throws JposException {
    }

    /**
     * The default implementation sets the corresponding property, AutoStopBackwardItemCount or
     * AutoStopForwardItemCount, to 0.
     * @param direction   Affected direction, one of RIC_BACKWARD and RIC_FORWARD.
     * @throws JposException If an error occurs.
     */
    @Override
    public void resetItemCount(int direction) throws JposException {
        if (direction == BeltConst.BELT_AIC_BACKWARD)
            AutoStopBackwardItemCount = 0;
        else
            AutoStopForwardItemCount = 0;
    }

    @Override
    public void stopBelt() throws JposException {
    }
}
