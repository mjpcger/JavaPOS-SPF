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

package de.gmxhome.conrad.jpos.jpos_base.scale;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Status update event implementation for Scale devices.
 */
public class ScaleStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Constructor, Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (scale.)ScaleService object.
     * @param state Status,  see UPOS specification, chapter Scale - Events - StatusUpdateEvent.
     */
    public ScaleStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
    }

    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        ScaleProperties props = (ScaleProperties)getPropertySet();
        switch (getStatus()) {
            default:
                if (getStatus() != ScaleConst.SCAL_SUE_WEIGHT_UNDERWEIGHT)
                    break;
            case ScaleConst.SCAL_SUE_STABLE_WEIGHT:
            case ScaleConst.SCAL_SUE_WEIGHT_UNSTABLE:
            case ScaleConst.SCAL_SUE_WEIGHT_ZERO:
            case ScaleConst.SCAL_SUE_WEIGHT_OVERWEIGHT:
            case ScaleConst.SCAL_SUE_NOT_READY:
            case ScaleConst.SCAL_SUE_WEIGHT_UNDER_ZERO:
                props.signalWaiter();
                return true;
        }
        return false;
    }

    @Override
    public boolean checkStatusCorresponds() {
        if (super.checkStatusCorresponds())
            return true;
        switch (getStatus()) {
            default:
                if (getStatus() != ScaleConst.SCAL_SUE_WEIGHT_UNDERWEIGHT)
                    break;
            case ScaleConst.SCAL_SUE_STABLE_WEIGHT:
            case ScaleConst.SCAL_SUE_WEIGHT_UNSTABLE:
            case ScaleConst.SCAL_SUE_WEIGHT_ZERO:
            case ScaleConst.SCAL_SUE_WEIGHT_OVERWEIGHT:
            case ScaleConst.SCAL_SUE_NOT_READY:
            case ScaleConst.SCAL_SUE_WEIGHT_UNDER_ZERO:
                return true;
        }
        return false;
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        ScaleProperties props = (ScaleProperties)getPropertySet();
        if (super.setAndCheckStatusProperties())
            return true;
        switch (getStatus()) {
            default:
                if (getStatus() != ScaleConst.SCAL_SUE_WEIGHT_UNDERWEIGHT)
                    break;
            case ScaleConst.SCAL_SUE_STABLE_WEIGHT:
            case ScaleConst.SCAL_SUE_WEIGHT_UNSTABLE:
            case ScaleConst.SCAL_SUE_WEIGHT_ZERO:
            case ScaleConst.SCAL_SUE_WEIGHT_OVERWEIGHT:
            case ScaleConst.SCAL_SUE_NOT_READY:
            case ScaleConst.SCAL_SUE_WEIGHT_UNDER_ZERO:
                return true;
        }
        return false;
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        if (ret.length() > 0)
            return ret;
        switch (getStatus()) {
            case ScaleConst.SCAL_SUE_STABLE_WEIGHT:
                return "Weight stable";
            case ScaleConst.SCAL_SUE_WEIGHT_UNSTABLE:
                return "Weight unstable";
            case ScaleConst.SCAL_SUE_WEIGHT_ZERO:
                return "Weight zero";
            case ScaleConst.SCAL_SUE_WEIGHT_OVERWEIGHT:
                return "Weight too high";
            case ScaleConst.SCAL_SUE_NOT_READY:
                return "Scale not ready";
            case ScaleConst.SCAL_SUE_WEIGHT_UNDER_ZERO:
                return "Weight under zero";
            default:
                if (getStatus() == ScaleConst.SCAL_SUE_WEIGHT_UNDERWEIGHT)
                    return "Under weight";
        }
        return "Unknown Status Change: "+ getStatus();
    }
}
