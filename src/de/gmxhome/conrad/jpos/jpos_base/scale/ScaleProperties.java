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
 * Class containing the scales specific properties, their default values and default implementations of
 * ScaleInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Scale.
 */
public class ScaleProperties extends JposCommonProperties implements ScaleInterface {
    /**
     * UPOS property CapDisplay. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapDisplay = false;

    /**
     * UPOS property CapDisplayText. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapDisplayText = false;

    /**
     * UPOS property CapFreezeValue. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapFreezeValue = false;

    /**
     * UPOS property CapPriceCalculating. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapPriceCalculating = false;

    /**
     * UPOS property CapReadLiveWeightWithTare. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapReadLiveWeightWithTare = false;

    /**
     * UPOS property CapSetPriceCalculationMode. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSetPriceCalculationMode = false;

    /**
     * UPOS property CapSetUnitPriceWithWeightUnit. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSetUnitPriceWithWeightUnit = false;

    /**
     * UPOS property CapSpecialTare. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSpecialTare = false;

    /**
     * UPOS property CapStatusUpdate. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapStatusUpdate = false;

    /**
     * UPOS property CapTarePriority. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapTarePriority = false;

    /**
     * UPOS property CapTareWeight. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapTareWeight = false;

    /**
     * UPOS property CapZeroScale. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapZeroScale = false;

    /**
     * UPOS property MaxDisplayTextChars. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int MaxDisplayTextChars = 0;

    /**
     * UPOS property MaximumWeight. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int MaximumWeight = 0;

    /**
     * UPOS property MinimumWeight. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int MinimumWeight = 0;

    /**
     * Default value of TareWeight property. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int TareWeightDef = 0;

    /**
     * UPOS property WeightUnit. Default: ScaleConst.SCAL_WU_KILOGRAM. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int WeightUnit = ScaleConst.SCAL_WU_KILOGRAM;

    /**
     * UPOS property SalesPrice.
     */
    public long SalesPrice;

    /**
     * UPOS property ScaleLiveWeight.
     */
    public int ScaleLiveWeight;

    /**
     * UPOS property StatusNotify.
     */
    public int StatusNotify;

    /**
     * UPOS property TareWeight.
     */
    public int TareWeight;

    /**
     * UPOS property UnitPrice.
     */
    public long UnitPrice;

    /**
     * UPOS property ZeroValid.
     */
    public boolean ZeroValid;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    public ScaleProperties(int dev) {
        super(dev);
    }

    @Override
    public void initOnOpen() {
        super.initOnOpen();
        StatusNotify = ScaleConst.SCAL_SN_DISABLED;
        ZeroValid = false;
        ScaleService srv = (ScaleService)EventSource;
    }

    @Override
    public boolean initOnFirstEnable() {
        if (!super.initOnFirstEnable()) {
            SalesPrice = 0;
            ScaleLiveWeight = 0;
            TareWeight = TareWeightDef;
            UnitPrice = 0;
            return false;
        }
        return true;
    }

    @Override
    public void statusNotify(int i) throws JposException {
        StatusNotify = i;
    }

    @Override
    public void tareWeight(int i) throws JposException {
        TareWeight = i;
    }

    @Override
    public void unitPrice(long l) throws JposException {
        UnitPrice = l;
    }

    @Override
    public void zeroValid(boolean b) throws JposException {
        ZeroValid = b;
    }

    @Override
    public void displayText(String data) throws JposException {
    }

    @Override
    public DoPriceCalculating doPriceCalculating(int[] weightData, int[] tare, long[] unitPrice, long[] unitPriceX, int[] weightUnitX, int[] weightNumeratorX, int[] weightDenominatorX, long[] price, int timeout) throws JposException {
        return new DoPriceCalculating(this, weightData[0], tare[0], unitPrice[0], unitPriceX[0], weightUnitX[0], weightNumeratorX[0], weightDenominatorX[0], price[0], timeout);
    }

    @Override
    public void doPriceCalculating(DoPriceCalculating request) throws JposException {
    }

    @Override
    public void freezeValue(int item, boolean freeze) throws JposException {
    }

    @Override
    public ReadLiveWeightWithTare readLiveWeightWithTare(int[] weightData, int[] tare, int timeout) throws JposException {
        return new ReadLiveWeightWithTare(this, weightData[0], tare[0], timeout);
    }

    @Override
    public void readLiveWeightWithTare(ReadLiveWeightWithTare request) throws JposException {
    }

    @Override
    public ReadWeight readWeight(int[] weightData, int timeout) throws JposException {
        return new ReadWeight(this, weightData[0], timeout);
    }

    @Override
    public void readWeight(ReadWeight request) throws JposException {
    }

    @Override
    public void setPriceCalculationMode(int mode) throws JposException {
    }

    @Override
    public void setSpecialTare(int mode, int data) throws JposException {
    }

    @Override
    public void setTarePriority(int priority) throws JposException {
    }

    @Override
    public void setUnitPriceWithWeightUnit(long unitPrice, int weightUnit, int weightNumerator, int weightDenominator) throws JposException {
    }

    @Override
    public void zeroScale() throws JposException {
    }
}