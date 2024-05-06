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

/**
 * Data event implementation for Scale devices.
 */
public class ScaleDataEvent extends JposDataEvent {
    /**
     * Unit price updated by DoPriceCalculation or 0 if not supported
     */
    public final long UnitPrice;

    /**
     * Price set of readWeight or DoPriceCalculation.
     */
    public final long Price;

    /**
     * Tara updated of ReadLifeWeightWithTare and DoPriceCalculation or 0 if CapTareWeight is false.
     */
    public final int TareWeight;

    /**
     * Constructor. Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (scale.)ScaleService object.
     * @param weight Status, in case of Scale the weight, see UPOS specification.
     * @param tara   Tare weight used during last scale operation. In negative, properties ScaleLifeWeight,
     *               TareWeight and UnitPrice will not be updated.
     * @param price  Computed price of last scale operation.
     * @param unitprice  Unit price used during last scale operation. If negative, property UnitPrice will
     *                   not be updated.
     */
    public ScaleDataEvent(JposBase source, int weight, int tara, long price, long unitprice) {
        super(source, weight);
        UnitPrice = unitprice;
        Price = price;
        TareWeight = tara;
    }

    @Override
    public void setDataProperties() {
        super.setDataProperties();
        ScaleProperties dev = (ScaleProperties) getPropertySet();
        dev.SalesPrice = Price;
        dev.EventSource.logSet("SalesPrice");
        if (TareWeight >= 0) {
            if (UnitPrice >= 0) {
                dev.UnitPrice = UnitPrice;
                dev.EventSource.logSet("UnitPrice");
            }
            dev.ScaleLiveWeight = getStatus();
            dev.EventSource.logSet("ScaleLiveWeight");
            dev.TareWeight = TareWeight;
            dev.EventSource.logSet("TareWeight");
        }
    }
}
