/*
 * Copyright 2018 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.scale;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.JposConst;
import jpos.JposException;

/**
 * Input request executor for Scale method ReadLiveWeightWithTare.
 */
public class ReadLiveWeightWithTare extends ReadWeight {
    /**
     * Scale method ReadLiveWeightWithTare parameter tare, see UPOS specification.
     * Tare from scale after successful invocation.
     */
    public int Tare;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props         Property set of device service.
     * @param weightData    Preset for  returned value for the weight measured by the scale.
     * @param tare          The value used to calculate the net weight.
     * @param timeout       The number of milliseconds to wait for a settled weight before failing the method.
     */
    public ReadLiveWeightWithTare(JposCommonProperties props, int weightData, int tare, int timeout) {
        super(props, weightData, timeout);
        Tare = tare;
    }

    @Override
    public void invoke() throws JposException {
        Props.Device.check(getTimeout() < 0  && getTimeout() != JposConst.JPOS_FOREVER && Props.AsyncMode == false, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + getTimeout());
        ((ScaleService)Props.EventSource).ScaleInterface.readLiveWeightWithTare(this);
        if (EndSync == null) {
            ScaleProperties data = (ScaleProperties) Props;
            Props.Device.handleEvent(new ScaleDataEvent(Props.EventSource, WeightData, Tare, SalesPrice, -1));
        }
    }
}
