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
import jpos.services.ScaleService114;

/**
 * Scale service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class ScaleService extends JposBase implements ScaleService114 {
    /**
     * Instance of a class implementing the ScaleInterface for scale specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public ScaleInterface ScaleInterface;

    private ScaleProperties Data;

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public ScaleService(ScaleProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public boolean getCapDisplay() throws JposException {
        checkOpened();
        logGet("CapDisplay");
        return Data.CapDisplay;
    }

    @Override
    public boolean getCapDisplayText() throws JposException {
        checkOpened();
        logGet("CapDisplayText");
        return Data.CapDisplayText;
    }

    @Override
    public boolean getCapFreezeValue() throws JposException {
        checkOpened();
        logGet("CapFreezeValue");
        return Data.CapFreezeValue;
    }

    @Override
    public boolean getCapPriceCalculating() throws JposException {
        checkOpened();
        logGet("CapPriceCalculating");
        return Data.CapPriceCalculating;
    }

    @Override
    public boolean getCapReadLiveWeightWithTare() throws JposException {
        checkOpened();
        logGet("CapReadLiveWeightWithTare");
        return Data.CapReadLiveWeightWithTare;
    }

    @Override
    public boolean getCapSetPriceCalculationMode() throws JposException {
        checkOpened();
        logGet("CapSetPriceCalculationMode");
        return Data.CapSetPriceCalculationMode;
    }

    @Override
    public boolean getCapSetUnitPriceWithWeightUnit() throws JposException {
        checkOpened();
        logGet("CapSetUnitPriceWithWeightUnit");
        return Data.CapSetUnitPriceWithWeightUnit;
    }

    @Override
    public boolean getCapSpecialTare() throws JposException {
        checkOpened();
        logGet("CapSpecialTare");
        return Data.CapSpecialTare;
    }

    @Override
    public boolean getCapStatusUpdate() throws JposException {
        checkOpened();
        logGet("CapStatusUpdate");
        return Data.CapStatusUpdate;
    }

    @Override
    public boolean getCapTarePriority() throws JposException {
        checkOpened();
        logGet("CapTarePriority");
        return Data.CapTarePriority;
    }

    @Override
    public boolean getCapTareWeight() throws JposException {
        checkOpened();
        logGet("CapTareWeight");
        return Data.CapTareWeight;
    }

    @Override
    public boolean getCapZeroScale() throws JposException {
        checkOpened();
        logGet("CapZeroScale");
        return Data.CapZeroScale;
    }

    @Override
    public int getMaxDisplayTextChars() throws JposException {
        checkOpened();
        logGet("MaxDisplayTextChars");
        return Data.MaxDisplayTextChars;
    }

    @Override
    public int getMaximumWeight() throws JposException {
        checkOpened();
        logGet("MaximumWeight");
        return Data.MaximumWeight;
    }

    @Override
    public int getMinimumWeight() throws JposException {
        checkOpened();
        logGet("MinimumWeight");
        return Data.MinimumWeight;
    }

    @Override
    public long getSalesPrice() throws JposException {
        checkFirstEnabled();
        logGet("SalesPrice");
        return Data.SalesPrice;
    }

    @Override
    public int getScaleLiveWeight() throws JposException {
        checkEnabled();
        logGet("ScaleLiveWeight");
        return Data.ScaleLiveWeight;
    }

    @Override
    public int getStatusNotify() throws JposException {
        checkOpened();
        logGet("StatusNotify");
        return Data.StatusNotify;
    }

    @Override
    public void setStatusNotify(int i) throws JposException {
        logPreSet("StatusNotify");
        checkOpened();
        Device.check(!Data.CapStatusUpdate, JposConst.JPOS_E_ILLEGAL, "Status notification not supported");
        Device.check(Data.DeviceEnabled, JposConst.JPOS_E_ILLEGAL, "Device enabled");
        Device.checkMember(i, new long[]{ScaleConst.SCAL_SN_ENABLED, ScaleConst.SCAL_SN_DISABLED}, JposConst.JPOS_E_ILLEGAL, "Invalid status notification value: " + i);
        ScaleInterface.statusNotify(i);
        logSet("StatusNotify");
    }

    @Override
    public int getTareWeight() throws JposException {
        checkEnabled();
        logGet("TareWeight");
        return Data.TareWeight;
    }

    @Override
    public void setTareWeight(int i) throws JposException {
        logPreSet("TareWeight");
        checkEnabled();
        Device.check(i < 0 || i >= Data.MaximumWeight, JposConst.JPOS_E_ILLEGAL, "Tare weight out of range");
        ScaleInterface.tareWeight(i);
        logSet("TareWeight");
    }

    @Override
    public long getUnitPrice() throws JposException {
        checkEnabled();
        logGet("UnitPrice");
        return Data.UnitPrice;
    }

    @Override
    public void setUnitPrice(long l) throws JposException {
        logPreSet("UnitPrice");
        checkEnabled();
        Device.check(l < 0, JposConst.JPOS_E_ILLEGAL, "Tare weight negative");
        ScaleInterface.unitPrice(l);
        logSet("UnitPrice");
    }

    @Override
    public int getWeightUnit() throws JposException {
        checkOpened();
        logGet("WeightUnit");
        return Data.WeightUnit;
    }

    @Override
    public boolean getZeroValid() throws JposException {
        checkOpened();
        logGet("ZeroValid");
        return Data.ZeroValid;
    }

    @Override
    public void setZeroValid(boolean b) throws JposException {
        logPreSet("UnitPrice");
        checkOpened();
        ScaleInterface.zeroValid(b);
        logSet("ZeroValid");
    }

    @Override
    public void displayText(String s) throws JposException {
        logPreCall("DisplayText", s);
        checkEnabled();
        Device.check(!Data.CapDisplayText, JposConst.JPOS_E_ILLEGAL, "Method DisplayText not supported");
        Device.check(s != null && s.length() > Data.MaxDisplayTextChars, JposConst.JPOS_E_ILLEGAL, "Text too long: " + s);
        ScaleInterface.displayText(s);
        logCall("DisplayText");
    }

    @Override
    public void doPriceCalculating(int[] weightData, int[] tare, long[] unitPrice, long[] unitPriceX, int[] weightUnitX, int[] weightNumeratorX, int[] weightDenominatorX, long[] price, int timeout) throws JposException {
        logPreCall("DoPriceCalculating", "" + timeout);
        checkEnabled();
        Device.check(!Data.CapSetUnitPriceWithWeightUnit, JposConst.JPOS_E_ILLEGAL, "Method DoPriceCalculating not supported");
        Device.check(Device.PendingCommands.size() > 0 || Device.CurrentCommand != null, JposConst.JPOS_E_BUSY, "Device busy");
        Device.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER && Props.AsyncMode == false, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        Device.check(weightData.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid dimension of weightData");
        Device.check(tare.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid dimension of tare");
        Device.check(unitPrice.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid dimension of unitPrice");
        Device.check(unitPriceX.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid dimension of unitPriceX");
        Device.check(weightUnitX.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid dimension of weightUnitX");
        Device.check(weightNumeratorX.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid dimension of weightNumeratorX");
        Device.check(weightDenominatorX.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid dimension of weightDenominatorX");
        Device.check(price.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid dimension of price");
        try {
            ScaleInterface.doPriceCalculating(weightData, tare, unitPrice, unitPriceX, weightUnitX, weightNumeratorX, weightDenominatorX, price, timeout);
            logCall("DoPriceCalculating", "" + weightData[0] + ", " + tare[0] + ", " + unitPrice[0] + ", " + unitPriceX[0] + ", " + weightUnitX[0] + ", " + weightNumeratorX[0] + ", " + weightDenominatorX[0] + ", " + price[0]);
        } catch (JposOutputRequest.OkException e) {
            Device.check(!(e.getOutputRequest() instanceof DoPriceCalculating), JposConst.JPOS_E_FAILURE, "Bad request from validation: " + e.getOutputRequest().getClass().getName());
            DoPriceCalculating request = (DoPriceCalculating) e.getOutputRequest();
            if (callNowOrLater(request)) {
                unitPrice[0] = price[0] = weightData[0] = tare[0] = 0;
                logAsyncCall("DoPriceCalculating(" + weightData[0] + ", " + tare[0] + ", " + unitPrice[0] + ", " + unitPriceX[0] + ", " + weightUnitX[0] + ", " + weightNumeratorX[0] + ", " + weightDenominatorX[0] + ", " + price[0] + ")");
            }
            else {
                Data.ScaleLiveWeight = weightData[0] = request.WeightData;
                logSet("ScaleLiveWeight");
                Data.TareWeight = tare[0] = request.Tare;
                logSet("TareWeight");
                Data.SalesPrice = request.SalesPrice;
                logSet("SalesPrice");
                Data.UnitPrice = unitPrice[0] = request.UnitPrice;
                logCall("DoPriceCalculating", "" + weightData[0] + ", " + tare[0] + ", " + unitPrice[0] + ", " + unitPriceX[0] + ", " + weightUnitX[0] + ", " + weightNumeratorX[0] + ", " + weightDenominatorX[0] + ", " + price[0]);
            }
        }
    }

    @Override
    public void freezeValue(int item, boolean freeze) throws JposException {
        logPreCall("FreezeValue", "" + item + ", " + freeze);
        checkEnabled();
        Device.check(!Data.CapFreezeValue, JposConst.JPOS_E_ILLEGAL, "Method FreezeValue not supported");
        Device.check((item & ~(ScaleConst.SCAL_SFR_MANUAL_TARE|ScaleConst.SCAL_SFR_PERCENT_TARE|ScaleConst.SCAL_SFR_WEIGHTED_TARE|ScaleConst.SCAL_SFR_UNITPRICE)) != 0, JposConst.JPOS_E_ILLEGAL, "Invalid item: " + item);
        ScaleInterface.freezeValue(item, freeze);
        logCall("FreezeValue");
    }

    @Override
    public void readLiveWeightWithTare(int[] weightData, int[] tare, int timeout) throws JposException {
        logPreCall("ReadLiveWeightWithTare", "" + timeout);
        checkEnabled();
        Device.check(!Data.CapReadLiveWeightWithTare, JposConst.JPOS_E_ILLEGAL, "Method ReadLiveWeightWithTare not supported");
        Device.check(Device.PendingCommands.size() > 0 || Device.CurrentCommand != null, JposConst.JPOS_E_BUSY, "Device busy");
        Device.check(timeout < 0  && timeout != JposConst.JPOS_FOREVER && Props.AsyncMode == false, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        Device.check(weightData.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid dimension of weightData");
        Device.check(tare.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid dimension of tare");
        try {
            ScaleInterface.readLiveWeightWithTare(weightData, tare, timeout);
            logCall("ReadLiveWeightWithTare", "" + weightData[0] + ", " + tare[0]);
        } catch (JposOutputRequest.OkException e) {
            Device.check(!(e.getOutputRequest() instanceof ReadLiveWeightWithTare), JposConst.JPOS_E_FAILURE, "Bad request from validation: " + e.getOutputRequest().getClass().getName());
            ReadLiveWeightWithTare request = (ReadLiveWeightWithTare) e.getOutputRequest();
            if (callNowOrLater(request)) {
                weightData[0] = 0;
                tare[0] = 0;
                logAsyncCall("ReadLiveWeightWithTare(0, 0)");
            }
            else {
                Data.ScaleLiveWeight = weightData[0] = request.WeightData;
                logSet("ScaleLiveWeight");
                Data.TareWeight = tare[0] = request.Tare;
                logSet("TareWeight");
                Data.SalesPrice = request.SalesPrice;
                logSet("SalesPrice");
                logCall("ReadLiveWeightWithTare", "" + weightData[0] + ", " + tare[0]);
            }
        }
    }

    @Override
    public void readWeight(int[] weightData, int timeout) throws JposException {
        logPreCall("ReadWeight", "" + timeout);
        checkEnabled();
        Device.check(Device.PendingCommands.size() > 0 || Device.CurrentCommand != null, JposConst.JPOS_E_BUSY, "Device busy");
        Device.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER && Props.AsyncMode == false, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        Device.check(weightData.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid dimension of weightData");
        try {
            ScaleInterface.readWeight(weightData, timeout);
            logCall("ReadWeight", "" + weightData[0]);
        } catch (JposOutputRequest.OkException e) {
            Device.check(!(e.getOutputRequest() instanceof ReadWeight), JposConst.JPOS_E_FAILURE, "Bad request from validation: " + e.getOutputRequest().getClass().getName());
            ReadWeight request = (ReadWeight) e.getOutputRequest();
            if (callNowOrLater(request)) {
                weightData[0] = 0;
                logAsyncCall("ReadWeight(0)");
            }
            else {
                weightData[0] = request.WeightData;
                Data.SalesPrice = request.SalesPrice;
                logSet("SalesPrice");
                logCall("ReadWeight", "" + weightData[0]);
            }
        }
    }

    @Override
    public void setPriceCalculationMode(int mode) throws JposException {
        logPreCall("SetPriceCalculationMode", "" + mode);
        checkEnabled();
        Device.check(!Data.CapSetPriceCalculationMode, JposConst.JPOS_E_ILLEGAL, "Method SetPriceCalculationMode not supported");
        Device.checkMember(mode, new long[]{ScaleConst.SCAL_PCM_PRICE_LABELING, ScaleConst.SCAL_PCM_SELF_SERVICE, ScaleConst.SCAL_PCM_OPERATOR}, JposConst.JPOS_E_ILLEGAL, "Invalid mode: " + mode);
        ScaleInterface.setPriceCalculationMode(mode);
        logCall("SetPriceCalculationMode");
    }

    @Override
    public void setSpecialTare(int mode, int data) throws JposException {
        logPreCall("SetSpecialTare", "" + mode + ", " + data);
        checkEnabled();
        Device.check(!Data.CapSpecialTare, JposConst.JPOS_E_ILLEGAL, "Method SetSpecialTare not supported");
        Device.checkMember(mode, new long[]{ScaleConst.SCAL_SST_DEFAULT, ScaleConst.SCAL_SST_MANUAL, ScaleConst.SCAL_SST_PERCENT, ScaleConst.SCAL_SST_WEIGHTED}, JposConst.JPOS_E_ILLEGAL, "Invalid mode: " + mode);
        Device.check(data < 0 && mode != ScaleConst.SCAL_SST_WEIGHTED, JposConst.JPOS_E_ILLEGAL, "Negative tare");
        Device.check(data > Data.MaximumWeight && (mode == ScaleConst.SCAL_SST_DEFAULT || mode == ScaleConst.SCAL_SST_MANUAL), JposConst.JPOS_E_ILLEGAL, "Tare too high: " + data);
        ScaleInterface.setSpecialTare(mode, data);
        logCall("SetSpecialTare");
    }

    /**
     * Same as set setTarePrioity. Will be called from setTarePrioity. Reason for this method is that setTarePrioity exists only
     * due to a spelling error.
     * @param priority Tare priority, see UPOS specification
     * @throws JposException If an error occurs
     */
    public void setTarePriority(int priority) throws JposException {
        logPreCall("SetTarePriority", "" + priority);
        checkEnabled();
        Device.check(!Data.CapTarePriority, JposConst.JPOS_E_ILLEGAL, "Method SetTarePriority not supported");
        Device.checkMember(priority, new long[]{ScaleConst.SCAL_STP_FIRST, ScaleConst.SCAL_STP_NONE}, JposConst.JPOS_E_ILLEGAL, "Invalid priority: " + priority);
        ScaleInterface.setTarePriority(priority);
        logCall("SetTarePriority");
    }

    @Override
    public void setTarePrioity(int priority) throws JposException {
        setTarePriority(priority);
    }

    @Override
    public void setUnitPriceWithWeightUnit(long unitPrice, int weightUnit, int weightNumerator, int weightDenominator) throws JposException {
        logPreCall("SetUnitPriceWithWeightUnit", "" + unitPrice + ", " + weightUnit + ", " + weightNumerator + ", " + weightDenominator);
        checkEnabled();
        Device.check(!Data.CapSetUnitPriceWithWeightUnit, JposConst.JPOS_E_ILLEGAL, "Method SetUnitPriceWithWeightUnit not supported");
        Device.checkMember(weightUnit, new long[]{ScaleConst.SCAL_WU_GRAM, ScaleConst.SCAL_WU_KILOGRAM, ScaleConst.SCAL_WU_OUNCE, ScaleConst.SCAL_WU_POUND}, JposConst.JPOS_E_ILLEGAL, "Invalid weight unit: " + weightUnit);
        Device.check(unitPrice < 0, JposConst.JPOS_E_ILLEGAL, "Unit price invalid: " + unitPrice);
        Device.check(weightNumerator < 0, JposConst.JPOS_E_ILLEGAL, "Weight numerator invalid: " + weightNumerator);
        Device.check(weightDenominator < 0, JposConst.JPOS_E_ILLEGAL, "Weight denominator invalid: " + weightDenominator);
        ScaleInterface.setUnitPriceWithWeightUnit(unitPrice, weightUnit, weightNumerator, weightDenominator);
        logCall("SetUnitPriceWithWeightUnit");
    }

    @Override
    public void zeroScale() throws JposException {
        logPreCall("ZeroScale");
        checkEnabled();
        Device.check(!Data.CapZeroScale, JposConst.JPOS_E_ILLEGAL, "Method ZeroScale not supported");
        Device.check(Device.PendingCommands.size() > 0 || Device.CurrentCommand != null, JposConst.JPOS_E_BUSY, "Device busy");
        ScaleInterface.zeroScale();
        logCall("ZeroScale");
    }
}
