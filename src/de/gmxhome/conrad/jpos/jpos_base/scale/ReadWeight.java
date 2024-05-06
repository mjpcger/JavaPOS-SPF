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
import jpos.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;

/**
 * Input request executor for Scale method ReadWeight.
 */
public class ReadWeight extends JposInputRequest {
    /**
     * Weight from scale after successful invocation.
     */
    public int WeightData;

    /**
     * Value for scale property SalesPrice, see UPOS specification. Sales price from scale.
     */
    public long SalesPrice;

    /**
     * Scale method ReadWeight parameter timeout, see UPOS specification.
     * @return timeout for operation. FOREVER for unlimited wait.
     */
    public int getTimeout() {
        return Timeout;
    }
    private final int Timeout;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props         Property set of device service.
     * @param weightData    Preset for  returned value for the weight measured by the scale.
     * @param timeout       The number of milliseconds to wait for a settled weight before failing the method.
     */
    public ReadWeight(JposCommonProperties props, int weightData, int timeout) {
        super(props);
        WeightData = weightData;
        Timeout = timeout;
        SalesPrice = 0;
    }

    @Override
    public void invoke() throws JposException {
        check(Timeout < 0  && Timeout != JPOS_FOREVER && EndSync != null, JPOS_E_ILLEGAL, "Invalid timeout: " + Timeout);
        ((ScaleService)Props.EventSource).ScaleInterface.readWeight(this);
        if (EndSync == null) {
            Props.Device.handleEvent(new ScaleDataEvent(Props.EventSource, WeightData, -1, SalesPrice, -1));
        }
    }
}
