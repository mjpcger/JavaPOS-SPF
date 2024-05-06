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

import static jpos.ScaleConst.*;

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
            case SCAL_SUE_STABLE_WEIGHT, SCAL_SUE_WEIGHT_UNSTABLE, SCAL_SUE_WEIGHT_ZERO, SCAL_SUE_WEIGHT_OVERWEIGHT,
                    SCAL_SUE_NOT_READY, SCAL_SUE_WEIGHT_UNDER_ZERO, SCAL_SUE_WEIGHT_UNDERWEIGHT -> {
                props.signalWaiter();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean checkStatusCorresponds() {
        return  super.checkStatusCorresponds() || switch (getStatus()) {
            case SCAL_SUE_WEIGHT_UNDERWEIGHT, SCAL_SUE_STABLE_WEIGHT, SCAL_SUE_WEIGHT_UNSTABLE, SCAL_SUE_WEIGHT_ZERO,
                    SCAL_SUE_WEIGHT_OVERWEIGHT, SCAL_SUE_NOT_READY, SCAL_SUE_WEIGHT_UNDER_ZERO ->
                    true;
            default -> false;
        };
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        return super.setAndCheckStatusProperties() || switch (getStatus()) {
            case SCAL_SUE_WEIGHT_UNDERWEIGHT, SCAL_SUE_STABLE_WEIGHT, SCAL_SUE_WEIGHT_UNSTABLE,
                    SCAL_SUE_WEIGHT_ZERO, SCAL_SUE_WEIGHT_OVERWEIGHT, SCAL_SUE_NOT_READY, SCAL_SUE_WEIGHT_UNDER_ZERO ->
                    true;
            default -> false;
        };
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        return ret.length() > 0 ?  ret : switch (getStatus()) {
            case SCAL_SUE_STABLE_WEIGHT -> "Weight stable";
            case SCAL_SUE_WEIGHT_UNSTABLE -> "Weight unstable";
            case SCAL_SUE_WEIGHT_ZERO -> "Weight zero";
            case SCAL_SUE_WEIGHT_OVERWEIGHT -> "Weight too high";
            case SCAL_SUE_NOT_READY -> "Scale not ready";
            case SCAL_SUE_WEIGHT_UNDER_ZERO -> "Weight under zero";
            case SCAL_SUE_WEIGHT_UNDERWEIGHT -> "Under weight";
            default -> "Unknown Status Change: " + getStatus();
        };
    }
}
