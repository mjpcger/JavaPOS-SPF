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
 * Interface for methods that implement property setter and method calls for the Scale device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Scale.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface ScaleInterface extends JposBaseInterface {
    /**
     * Final part of setting StatusNotify. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapStatusUpdate is true,</li>
     *     <li>The new status value is one of the predefined values.</li>
     * </ul>
     *
     * @param i New StatusNotify value
     * @throws JposException If an error occurs
     */
    public void statusNotify(int i) throws JposException;

    /**
     * Final part of setting TareWeight. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>The new tare value is between 0 and MaximumWeight.</li>
     * </ul>
     *
     * @param i New TareWeight value
     * @throws JposException If an error occurs
     */
    public void tareWeight(int i) throws JposException;

    /**
     * Final part of setting UnitPrice. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>New price is not negative.</li>
     * </ul>
     *
     * @param l New UnitPrice value
     * @throws JposException If an error occurs
     */
    public void unitPrice(long l) throws JposException;

    /**
     * Final part of setting ZeroValid. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     *
     * @param b New ZeroValid value
     * @throws JposException If an error occurs
     */
    public void zeroValid(boolean b) throws JposException;

    /**
     * Final part of DisplayText method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapDisplayText is true,</li>
     *     <li>The new text is not null and its length is not longer than MaxDisplayTextChars.</li>
     * </ul>
     *
     * @param data    The string of characters to display.
     * @throws JposException    See UPOS specification, method DisplayText.
     */
    public void displayText(String data) throws JposException;

    /**
     * Validation part of DoPriceCalculating method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>The device state is not S_BUSY,</li>
     *     <li>The dimension of the given array parameters is 1,</li>
     *     <li>In case of AsyncMode = false: The timeout is &ge; 0 or JPOS_FOREVER.</li>
     * </ul>
     * If AsyncMode = true, setting weightData[0], tare[0], unitPrice[0] and price[0] to 0 will not be necessary
     * because this will be done within the calling method.<p>
     * Since the UPOS specification is very unclear for this method, further
     * checks might be necessary and must be implemented within this method in derived classes:
     * <ul>
     *     <li>CapPriceCalculation check,</li>
     *     <li>CapSetUnitPriceWithWeightUnit check,</li>
     *     <li>Check whether SetUnitPriceWithWeightUnit has been called previously.</li>
     * </ul>
     * <br>In addition, this method must return the current values for initPriceX, weightUnitX,
     * weightNumeratorX and weightDenominatorX as set via method SetUnitPriceWithWeightUnit.
     * <br>A service may perform the full weight process inside this method, but this is not recommended, especially
     * in asynchronous operation. The recommended functionality is to perform only validation in this method and
     * to perform weighing inside the final part. If implemented the latter way, it must throw a JposException with
     * ErrorCode = 0 to signal successful operation and optional an additional data object, derived from Exception,
     * passed as original exception.
     *
     * @param weightData         The value for the net weight in the price calculation algorithm.
     * @param tare               The value used to determine the item net weight in the price calculation algorithm.
     * @param unitPrice          The cost per measurement unit that is used in the price calcuation algorithm.
     * @param unitPriceX         See UPOS specification, chapter Scale - Methods - doPriceCalculating Method.
     * @param weightUnitX        See UPOS specification, chapter Scale - Methods - doPriceCalculating Method.
     * @param weightNumeratorX   See UPOS specification, chapter Scale - Methods - doPriceCalculating Method.
     * @param weightDenominatorX See UPOS specification, chapter Scale - Methods - doPriceCalculating Method.
     * @param price              The calculated monetary value for the item on the scale.
     * @param timeout            The number of milliseconds to wait for a settled weight before failing the method.
     * @throws JposException    See UPOS specification, method DoPriceCalculating. To signal successful validation,
     *                          property ErrorCode will be 0.
     */
    public void doPriceCalculating(int[] weightData, int[] tare, long[] unitPrice, long[] unitPriceX, int[] weightUnitX, int[] weightNumeratorX, int[] weightDenominatorX, long[] price, int timeout) throws JposException;

    /**
     * Final part of DoPriceCalculating method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a DoPriceCalculating object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     * <br>In case of asynchronous processing, the following additional checks have been made before as well:
     * <ul>
     *     <li>Timeout &ge; 0 or JPOS_FOREVER.</li>
     * </ul>
     * <br>This method will only be called if the validation method threw a JposException with ErrorCode = 0.
     *
     * @param request           Input request object that contains all parameters to be used by DoPriceCalculating.
     * @throws JposException    If an error occurs.
     */
    public void doPriceCalculating(DoPriceCalculating request) throws JposException;

    /**
     * Final part of FreezeValue method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapFreezeValue is true,</li>
     *     <li>The given item is a bitwise combination of SCAL_SFR_MANUAL_TARE, SCAL_SFR_PERCENT_TARE,
     *         SCAL_SFR_WEIGHTED_TARE and SCAL_SFR_UNITPRICE.</li>
     * </ul>
     *
     * @param item    The bitwise value setting the state of the selected parameter item(s).
     * @param freeze  Specifies behavior after readWeight method finished. See UPOS specification, chapter Scale
     *                - Methods - doPriceCalculating Method.
     * @throws JposException    See UPOS specification, method FreezeValue.
     */
    public void freezeValue(int item, boolean freeze) throws JposException;

    /**
     * Validation part of ReadLiveWeightWithTare method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapReadLiveWeightWithTare is true,</li>
     *     <li>Device state is not S_BUSY,</li>
     *     <li>The dimension of weightData and tare is 1,</li>
     *     <li>If AsyncMode = false: timeout &ge; 0 or JPOS_FOREVER.</li>
     * </ul>
     * If AsyncMode = true, setting weightData[0] and tare[0] to 0 will not be necessary
     * because this will be done within the calling method.
     * <br>A service may perform the full weight process inside this method, but this is not recommended, especially
     * in asynchronous operation. The recommended functionality is to perform only validation in this method and
     * to perform weighing inside the final part. If implemented the latter way, it must throw a JposException with
     * ErrorCode = 0 to signal successful operation and optional an additional data object, derived from Exception,
     * passed as original exception.
     *
     * @param weightData    On return, net weight calculated by the scale.
     * @param tare          On return, tare weight used to calculate the net weight.
     * @param timeout       Number of milliseconds to wait for a settled weight before failing the method.
     * @throws JposException    See UPOS specification, method ReadLiveWeightWithTare. To signal successful validation,
     *                          property ErrorCode will be 0.
     */
    public void readLiveWeightWithTare(int[] weightData, int[] tare, int timeout) throws JposException;

    /**
     * Final part of ReadLiveWeightWithTare method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a ReadLiveWeightWithTare object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     * <br>In case of asynchronous processing, the following additional checks have been made before as well:
     * <ul>
     *     <li>Timeout &ge; 0 or JPOS_FOREVER.</li>
     * </ul>
     * <br>This method will only be called if the validation method threw a JposException with ErrorCode = 0.
     *
     * @param request           Output request object that contains all parameters to be used by ReadLiveWeightWithTare.
     * @throws JposException    If an error occurs.
     */
    public void readLiveWeightWithTare(ReadLiveWeightWithTare request) throws JposException;

    /**
     * Validation part of ReadWeight method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Device state is not S_BUSY,</li>
     *     <li>The dimension of weightData is 1,</li>
     *     <li>If AsyncMode = false: timeout &ge; 0 or JPOS_FOREVER.</li>
     * </ul>
     * If AsyncMode = true, setting weightData[0] to 0 will not be necessary
     * because this will be done within the calling method.
     * <br>A service may perform the full weight process inside this method, but this is not recommended, especially
     * in asynchronous operation. The recommended functionality is to perform only validation in this method and
     * to perform weighing inside the final part. If implemented the latter way, it must throw a JposException with
     * ErrorCode = 0 to signal successful operation and optional an additional data object, derived from Exception,
     * passed as original exception.
     *
     * @param weightData The weight measured by the scale.
     * @param timeout    The number of milliseconds to wait for a settled weight before failing the method.
     * @throws JposException    See UPOS specification, method ReadWeight. To signal successful validation,
     *                          property ErrorCode will be 0.
     */
    public void readWeight(int[] weightData, int timeout) throws JposException;

    /**
     * Final part of ReadWeight method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a ReadWeight object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     * <br>In case of asynchronous processing, the following additional checks have been made before as well:
     * <ul>
     *     <li>Timeout &ge; 0 or JPOS_FOREVER.</li>
     * </ul>
     * <br>This method will only be called if the validation method threw a JposException with ErrorCode = 0.
     *
     * @param request           Output request object that contains all parameters to be used by ReadWeight.
     * @throws JposException    If an error occurs.
     */
    public void readWeight(ReadWeight request) throws JposException;

    /**
     * Final part of SetPriceCalculationMode method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSetPriceCalculationMode is true,</li>
     *     <li>The new mode is one of SCAL_PCM_PRICE_LABELING, SCAL_PCM_SELF_SERVICE or SCAL_PCM_OPERATOR.</li>
     * </ul>
     *
     * @param mode    The operation functionality selected for the scale.
     * @throws JposException    See UPOS specification, method SetPriceCalculationMode.
     */
    public void setPriceCalculationMode(int mode) throws JposException;

    /**
     * Final part of SetSpecialTare method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSpecialTare is true,</li>
     *     <li>The new mode value is one of SCAL_SST_DEFAULT, SCAL_SST_MANUAL, SCAL_SST_PERCENT or SCAL_SST_WEIGHTED,</li>
     *     <li>The new tare specifier is &ge; 0,</li>
     *     <li>In case of SCAL_SST_DEFAULT and SCAL_SST_MANUAL: The new tare is &le; MaximumWeight.</li>
     * </ul>
     *
     * @param mode    Select the tare mode that is to be modified.
     * @param data    Provides additional information specific to the mode.
     * @throws JposException    See UPOS specification, method SetSpecialTare.
     */
    public void setSpecialTare(int mode, int data) throws JposException;

    /**
     * Final part of SetTarePriority method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapTarePriority is true,</li>
     *     <li>The new priority is one of SCAL_STP_FIRST or SCAL_STP_NONE.</li>
     * </ul>
     *
     * @param priority The sequence in which a tare value is used when determining the net weight.
     * @throws JposException    See UPOS specification, method SetTarePriority.
     */
    public void setTarePriority(int priority) throws JposException;

    /**
     * Final part of SetUnitPriceWithWeightUnit method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSetUnitPriceWithWeightUnit is true,</li>
     *     <li>The new weight unit is one of SCAL_WU_GRAM, SCAL_WU_KILOGRAM, SCAL_WU_OUNCE or SCAL_WU_POUND,</li>
     *     <li>The new unitPrice is &ge; 0,</li>
     *     <li>weightNumerator and weightDenominator are &ge;0, both.</li>
     * </ul>
     *
     * @param unitPrice         The cost per unit price as calculated by this method.
     * @param weightUnit        The value representing the new unit of weight that differs from the default value
     *                          for the scale.
     * @param weightNumerator   The dividend which is the weight value based on the current unit weight.
     * @param weightDenominator The divisor which is the weight value based on the new unit weight.
     * @throws JposException    See UPOS specification, method SetUnitPriceWithWeightUnit.
     */
    public void setUnitPriceWithWeightUnit(long unitPrice, int weightUnit, int weightNumerator, int weightDenominator) throws JposException;

    /**
     * Final part of ZeroScale method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapZeroScale is true,</li>
     *     <li>Device state is not S_BUSY.</li>
     * </ul>
     *
     * @throws JposException    See UPOS specification, method ZeroScale.
     */
    public void zeroScale() throws JposException;
}
