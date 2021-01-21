/*
 * Copyright 2020 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.cashchanger;

import de.gmxhome.conrad.jpos.jpos_base.JposBaseInterface;
import de.gmxhome.conrad.jpos.jpos_base.JposOutputRequest;
import jpos.JposException;

/**
 * Interface for methods that implement property setter and method calls for the CashChanger device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Cash Changer.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface CashChangerInterface  extends JposBaseInterface {
    /**
     * Final part of setting CurrencyCode. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>code is one of the values specified in CurrencyCodeList.</li>
     * </ul>
     *
     * @param code New value for CurrencyCode property.
     * @throws JposException If an error occurs.
     */
    public void currencyCode(String code) throws JposException;

    /**
     * Final part of setting CurrentExit. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>i is neither less than one nor greater than DeviceExits.</li>
     * </ul>
     *
     * @param i New value for CurrentExit property.
     * @throws JposException If an error occurs.
     */
    public void currentExit(int i) throws JposException;

    /**
     * Final part of setting CurrentService. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>i is neither less than zero nor greater than ServiceCount,</li>
     *     <li>i is one of the index values specified in ServiceIndex.</li>
     * </ul>
     *
     * @param i New value for CurrentService property.
     * @throws JposException If an error occurs.
     */
    public void currentService(int i) throws JposException;

    /**
     * Final part of setting RealTimeDataEnabled. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapRealTimeData is true or flag is false.</li>
     * </ul>
     *
     * @param flag New value for RealTimeDataEnabled property.
     * @throws JposException If an error occurs.
     */
    public void realTimeDataEnabled(boolean flag) throws JposException;


    /**
     * Final part of AdjustCashCounts method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>cashCounts consists of a comma separated, not empty list of integer value pairs
     *     separated by double-point,</li>
     *     <li>one pair may start with ";" to indicate the first bill adjustment.</li>
     * </ul>
     *
     * @param cashCounts  The cashCounts parameter contains cash types and amounts to be initialized.
     * @throws JposException If an error occurs.
     */
    public void adjustCashCounts(String cashCounts) throws JposException;

    /**
     * Final part of BeginDeposit method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>DepositStatus is STATUS_DEPOSIT_END. </li>
     * </ul>
     *
     * @throws JposException If an error occurs.
     */
    public void beginDeposit() throws JposException;

    /**
     * Final part of EndDeposit method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>DepositStatus is STATUS_DEPOSIT_COUNT, </li>
     *     <li>success is DEPOSIT_CHANGE, DEPOSIT_NOCHANGE or DEPOSIT_REPAY.</li>
     * </ul>
     *
     * @param success  The success parameter holds the value of how to deal with the cash that was
     * deposited. Must be DEPOSIT_COMPLETE.
     * @throws JposException If an error occurs.
     */
    public void endDeposit(int success) throws JposException;

    /**
     * Final part of FixDeposit method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>DepositStatus is STATUS_DEPOSIT_START. </li>
     * </ul>
     *
     * @throws JposException If an error occurs.
     */
    public void fixDeposit() throws JposException;

    /**
     * Final part of PauseDeposit method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapPauseDeposit is true, </li>
     *     <li>control is DEPOSIT_PAUSE or DEPOSIT_RESTART.</li>
     * </ul>
     *
     * @param control The control parameter contains one of DEPOSIT_PAUSE and DEPOSIT_RESTART.
     * @throws JposException If an error occurs.
     */
    public void pauseDeposit(int control) throws JposException;

    /**
     * Final part of ReadCashCounts method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>cashCounts and discrepancy are arrays with length one.</li>
     * </ul>
     *
     * @param cashCounts  The cash count data is placed into cashCounts.
     * @param discrepancy Reports whether not all counts could be updated as requested.
     * @throws JposException If an error occurs.
     */
    public void readCashCounts(String[] cashCounts, boolean[] discrepancy) throws JposException;

    /**
     * Validation part of DispenseCash method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>all previous DispenseCash operations have been finished,</li>
     *     <li>the remainder of cashCounts consists of a comma separated, not empty list of integer value pairs
     *     separated by double-point;</li>
     *     <li>one pair may start with ";" to indicate the first bill dispense.</li>
     * </ul>
     *
     * @param cashCounts        The cashCounts parameter contains the dispensing cash units and counts.
     * @return                  DispenseCash object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public DispenseCash dispenseCash(String cashCounts) throws JposException;

    /**
     * Final part of DispenseCash method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a DispenseCash object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by DispenseCash.
     * @throws JposException    If an error occurs.
     */
    public void dispenseCash(DispenseCash request) throws JposException;

    /**
     * Validation part of DispenseChange method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>all previous DispenseCash and DispenseChange operations have been finished,</li>
     *     <li>amount is greater than zero.</li>
     * </ul>
     *
     * @param amount        The amount parameter contains the amount of change to be dispensed.
     * @return                  DispenseChange object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public DispenseChange dispenseChange(int amount) throws JposException;

    /**
     * Final part of DispenseChange method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a DispenseChange object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by DispenseChange.
     * @throws JposException    If an error occurs.
     */
    public void dispenseChange(DispenseChange request) throws JposException;
}
