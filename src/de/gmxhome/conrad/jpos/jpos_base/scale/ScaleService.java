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
import jpos.services.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;
import static jpos.ScaleConst.*;

/**
 * Scale service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class ScaleService extends JposBase implements ScaleService116 {
    /**
     * Instance of a class implementing the ScaleInterface for scale specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public ScaleInterface ScaleInterface;

    private final ScaleProperties Data;

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
        check(!Data.CapStatusUpdate, JPOS_E_ILLEGAL, "Status notification not supported");
        check(Data.DeviceEnabled, JPOS_E_ILLEGAL, "Device enabled");
        checkMember(i, new long[]{SCAL_SN_ENABLED, SCAL_SN_DISABLED}, JPOS_E_ILLEGAL, "Invalid status notification value: " + i);
        checkNoChangedOrClaimed(Data.StatusNotify, i);
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
        check(i < 0 || i >= Data.MaximumWeight, JPOS_E_ILLEGAL, "Tare weight out of range");
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
        check(l < 0, JPOS_E_ILLEGAL, "Tare weight negative");
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
        checkNoChangedOrClaimed(Data.ZeroValid, b);
        ScaleInterface.zeroValid(b);
        logSet("ZeroValid");
    }

    @Override
    public void displayText(String data) throws JposException {
        logPreCall("DisplayText", removeOuterArraySpecifier(new Object[]{data}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapDisplayText, JPOS_E_ILLEGAL, "Method DisplayText not supported");
        check(data != null && data.length() > Data.MaxDisplayTextChars, JPOS_E_ILLEGAL, "Text too long: " + data);
        ScaleInterface.displayText(data);
        logCall("DisplayText");
    }

    @Override
    public void doPriceCalculating(int[] weightData, int[] tare, long[] unitPrice, long[] unitPriceX, int[] weightUnitX, int[] weightNumeratorX, int[] weightDenominatorX, long[] price, int timeout) throws JposException {
        logPreCall("DoPriceCalculating", removeOuterArraySpecifier(new Object[]{"...", timeout}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapSetUnitPriceWithWeightUnit, JPOS_E_ILLEGAL, "Method DoPriceCalculating not supported");
        check(Device.PendingCommands.size() > 0 || Device.CurrentCommand != null, JPOS_E_BUSY, "Device busy");
        check(timeout < 0 && timeout != JPOS_FOREVER && !Props.AsyncMode, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        check(weightData.length != 1, JPOS_E_ILLEGAL, "Invalid dimension of weightData");
        check(tare.length != 1, JPOS_E_ILLEGAL, "Invalid dimension of tare");
        check(unitPrice.length != 1, JPOS_E_ILLEGAL, "Invalid dimension of unitPrice");
        check(unitPriceX.length != 1, JPOS_E_ILLEGAL, "Invalid dimension of unitPriceX");
        check(weightUnitX.length != 1, JPOS_E_ILLEGAL, "Invalid dimension of weightUnitX");
        check(weightNumeratorX.length != 1, JPOS_E_ILLEGAL, "Invalid dimension of weightNumeratorX");
        check(weightDenominatorX.length != 1, JPOS_E_ILLEGAL, "Invalid dimension of weightDenominatorX");
        check(price.length != 1, JPOS_E_ILLEGAL, "Invalid dimension of price");
        DoPriceCalculating request = ScaleInterface.doPriceCalculating(weightData, tare, unitPrice, unitPriceX, weightUnitX, weightNumeratorX, weightDenominatorX, price, timeout);
        if (callNowOrLater(request)) {
            unitPrice[0] = price[0] = weightData[0] = tare[0] = 0;
            logAsyncCall("DoPriceCalculating(" + removeOuterArraySpecifier(new Object[]{0, 0, 0, unitPriceX[0],
                    weightUnitX[0], weightNumeratorX[0], weightDenominatorX[0], 0}, Device.MaxArrayStringElements) + ")");
        } else {
            Data.ScaleLiveWeight = weightData[0] = request.WeightData;
            logSet("ScaleLiveWeight");
            Data.TareWeight = tare[0] = request.Tare;
            logSet("TareWeight");
            Data.SalesPrice = request.SalesPrice;
            logSet("SalesPrice");
            Data.UnitPrice = unitPrice[0] = request.UnitPrice;
            logCall("DoPriceCalculating", removeOuterArraySpecifier(new Object[]{weightData[0], tare[0],
                    unitPrice[0], unitPriceX[0], weightUnitX[0], weightNumeratorX[0], weightDenominatorX[0], price[0]}, Device.MaxArrayStringElements));
        }
    }

    @Override
    public void freezeValue(int item, boolean freeze) throws JposException {
        logPreCall("FreezeValue", removeOuterArraySpecifier(new Object[]{item, freeze}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapFreezeValue, JPOS_E_ILLEGAL, "Method FreezeValue not supported");
        check((item & ~(SCAL_SFR_MANUAL_TARE|SCAL_SFR_PERCENT_TARE|SCAL_SFR_WEIGHTED_TARE|SCAL_SFR_UNITPRICE)) != 0, JPOS_E_ILLEGAL, "Invalid item: " + item);
        ScaleInterface.freezeValue(item, freeze);
        logCall("FreezeValue");
    }

    @Override
    public void readLiveWeightWithTare(int[] weightData, int[] tare, int timeout) throws JposException {
        logPreCall("ReadLiveWeightWithTare", removeOuterArraySpecifier(new Object[]{"...", timeout}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapReadLiveWeightWithTare, JPOS_E_ILLEGAL, "Method ReadLiveWeightWithTare not supported");
        check(Device.PendingCommands.size() > 0 || Device.CurrentCommand != null, JPOS_E_BUSY, "Device busy");
        check(timeout < 0 && timeout != JPOS_FOREVER && !Props.AsyncMode, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        check(weightData.length != 1, JPOS_E_ILLEGAL, "Invalid dimension of weightData");
        check(tare.length != 1, JPOS_E_ILLEGAL, "Invalid dimension of tare");
        ReadLiveWeightWithTare request = ScaleInterface.readLiveWeightWithTare(weightData, tare, timeout);
        if (callNowOrLater(request)) {
            weightData[0] = 0;
            tare[0] = 0;
            logAsyncCall("ReadLiveWeightWithTare(0, 0)");
        } else {
            Data.ScaleLiveWeight = weightData[0] = request.WeightData;
            logSet("ScaleLiveWeight");
            Data.TareWeight = tare[0] = request.Tare;
            logSet("TareWeight");
            Data.SalesPrice = request.SalesPrice;
            logSet("SalesPrice");
            logCall("ReadLiveWeightWithTare", removeOuterArraySpecifier(new Object[]{weightData[0], tare[0]}, Device.MaxArrayStringElements));
        }
    }

    @Override
    public void readWeight(int[] weightData, int timeout) throws JposException {
        logPreCall("ReadWeight", removeOuterArraySpecifier(new Object[]{"...", timeout}, Device.MaxArrayStringElements));
        checkEnabled();
        check(Device.PendingCommands.size() > 0 || Device.CurrentCommand != null, JPOS_E_BUSY, "Device busy");
        check(timeout < 0 && timeout != JPOS_FOREVER && !Props.AsyncMode, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        check(weightData.length != 1, JPOS_E_ILLEGAL, "Invalid dimension of weightData");
        ReadWeight request = ScaleInterface.readWeight(weightData, timeout);
        if (callNowOrLater(request)) {
            weightData[0] = 0;
            logAsyncCall("ReadWeight(0)");
        } else {
            weightData[0] = request.WeightData;
            Data.SalesPrice = request.SalesPrice;
            logSet("SalesPrice");
            logCall("ReadWeight", removeOuterArraySpecifier(new Object[]{weightData[0]}, Device.MaxArrayStringElements));
        }
    }

    @Override
    public void setPriceCalculationMode(int mode) throws JposException {
        logPreCall("SetPriceCalculationMode", removeOuterArraySpecifier(new Object[]{mode}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapSetPriceCalculationMode, JPOS_E_ILLEGAL, "Method SetPriceCalculationMode not supported");
        checkMember(mode, new long[]{SCAL_PCM_PRICE_LABELING, SCAL_PCM_SELF_SERVICE, SCAL_PCM_OPERATOR}, JPOS_E_ILLEGAL, "Invalid mode: " + mode);
        ScaleInterface.setPriceCalculationMode(mode);
        logCall("SetPriceCalculationMode");
    }

    @Override
    public void setSpecialTare(int mode, int data) throws JposException {
        logPreCall("SetSpecialTare", removeOuterArraySpecifier(new Object[]{mode, data}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapSpecialTare, JPOS_E_ILLEGAL, "Method SetSpecialTare not supported");
        checkMember(mode, new long[]{SCAL_SST_DEFAULT, SCAL_SST_MANUAL, SCAL_SST_PERCENT, SCAL_SST_WEIGHTED}, JPOS_E_ILLEGAL, "Invalid mode: " + mode);
        check(data < 0 && mode != SCAL_SST_WEIGHTED, JPOS_E_ILLEGAL, "Negative tare");
        check(data > Data.MaximumWeight && (mode == SCAL_SST_DEFAULT || mode == SCAL_SST_MANUAL), JPOS_E_ILLEGAL, "Tare too high: " + data);
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
        logPreCall("SetTarePriority", removeOuterArraySpecifier(new Object[]{priority}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapTarePriority, JPOS_E_ILLEGAL, "Method SetTarePriority not supported");
        checkMember(priority, new long[]{SCAL_STP_FIRST, SCAL_STP_NONE}, JPOS_E_ILLEGAL, "Invalid priority: " + priority);
        ScaleInterface.setTarePriority(priority);
        logCall("SetTarePriority");
    }

    @Override
    public void setTarePrioity(int priority) throws JposException {
        setTarePriority(priority);
    }

    @Override
    public void setUnitPriceWithWeightUnit(long unitPrice, int weightUnit, int weightNumerator, int weightDenominator) throws JposException {
        logPreCall("SetUnitPriceWithWeightUnit", removeOuterArraySpecifier(new Object[]{unitPrice, weightUnit, weightNumerator, weightDenominator}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapSetUnitPriceWithWeightUnit, JPOS_E_ILLEGAL, "Method SetUnitPriceWithWeightUnit not supported");
        checkMember(weightUnit, new long[]{SCAL_WU_GRAM, SCAL_WU_KILOGRAM, SCAL_WU_OUNCE, SCAL_WU_POUND}, JPOS_E_ILLEGAL, "Invalid weight unit: " + weightUnit);
        check(unitPrice < 0, JPOS_E_ILLEGAL, "Unit price invalid: " + unitPrice);
        check(weightNumerator < 0, JPOS_E_ILLEGAL, "Weight numerator invalid: " + weightNumerator);
        check(weightDenominator < 0, JPOS_E_ILLEGAL, "Weight denominator invalid: " + weightDenominator);
        ScaleInterface.setUnitPriceWithWeightUnit(unitPrice, weightUnit, weightNumerator, weightDenominator);
        logCall("SetUnitPriceWithWeightUnit");
    }

    @Override
    public void zeroScale() throws JposException {
        logPreCall("ZeroScale");
        checkEnabled();
        check(!Data.CapZeroScale, JPOS_E_ILLEGAL, "Method ZeroScale not supported");
        check(Device.PendingCommands.size() > 0 || Device.CurrentCommand != null, JPOS_E_BUSY, "Device busy");
        ScaleInterface.zeroScale();
        logCall("ZeroScale");
    }
}
