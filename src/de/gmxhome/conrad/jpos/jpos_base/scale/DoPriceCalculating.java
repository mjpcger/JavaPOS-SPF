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
 * Input request executor for Scale method DoPriceCalculating.
 */
public class DoPriceCalculating extends ReadLiveWeightWithTare {
    private int WeightUnitX;

    /**
     * Scale method DoPriceCalculating parameter weightUnitX, see UPOS specification.
     * @return  weightUnitX preset passed to the message before calling invoke, weightUnitX from scale after successful
     *          invocation.
     */
    public int getWeightUnitX() {
        return WeightUnitX;
    }

    private int WeightNumeratorX;

    /**
     * Scale method DoPriceCalculating parameter weightNumeratorX, see UPOS specification.
     * @return  weightNumeratorX preset passed to the message before calling invoke, weightNumeratorX from scale
     *          after successful invocation.
     */
    public int getWeightNumeratorX() {
        return WeightNumeratorX;
    }

    private int WeightDenominatorX;

    /**
     * Scale method DoPriceCalculating parameter weightDenominatorX, see UPOS specification.
     * @return  weightDenominatorX preset passed to the message before calling invoke, weightDenominatorX from scale
     *          after successful invocation.
     */
    public int getWeightDenominatorX() {
        return WeightDenominatorX;
    }

    /**
     * UnitPrice unitPrice from scale.
     */
    public long UnitPrice;

    private long UnitPriceX;

    /**
     * Scale method DoPriceCalculating parameter unitPriceX, see UPOS specification.
     * @return  unitPriceX preset passed to the message before calling invoke, unitPriceX from scale
     *          after successful invocation.
     */
    public long getUnitPriceX() {
        return UnitPriceX;
    }

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props         Property set of device service.
     * @param weightData    Preset for  returned value for the weight measured by the scale.
     * @param tare               The value used to determine the item net weight in the price calculation algorithm.
     * @param unitPrice          The cost per measurement unit that is used in the price calcuation algorithm.
     * @param unitPriceX         See UPOS specification, chapter Scale - Methods - doPriceCalculating Method.
     * @param weightUnitX        See UPOS specification, chapter Scale - Methods - doPriceCalculating Method.
     * @param weightNumeratorX   See UPOS specification, chapter Scale - Methods - doPriceCalculating Method.
     * @param weightDenominatorX See UPOS specification, chapter Scale - Methods - doPriceCalculating Method.
     * @param price              The calculated monetary value for the item on the scale.
     * @param timeout       The number of milliseconds to wait for a settled weight before failing the method.
     */
    public DoPriceCalculating(JposCommonProperties props, int weightData, int tare, long unitPrice, long unitPriceX, int weightUnitX, int weightNumeratorX, int weightDenominatorX, long price, int timeout) {
        super(props, weightData, tare, timeout);
        UnitPrice = unitPrice;
        UnitPriceX = unitPriceX;
        WeightUnitX = weightUnitX;
        WeightNumeratorX = weightNumeratorX;
        WeightDenominatorX = weightDenominatorX;
        SalesPrice = price;
    }

    @Override
    public void invoke() throws JposException {
        Props.Device.check(getTimeout() < 0  && getTimeout() != JposConst.JPOS_FOREVER && Props.AsyncMode == false, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + getTimeout());
        ((ScaleService)Props.EventSource).ScaleInterface.doPriceCalculating(this);
        if (EndSync == null) {
            ScaleProperties data = (ScaleProperties) Props;
            Props.Device.handleEvent(new ScaleDataEvent(Props.EventSource, WeightData, Tare, SalesPrice, UnitPrice));
        }
    }
}
