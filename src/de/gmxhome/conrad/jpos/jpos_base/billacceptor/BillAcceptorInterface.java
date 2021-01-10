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

package de.gmxhome.conrad.jpos.jpos_base.billacceptor;

import de.gmxhome.conrad.jpos.jpos_base.JposBaseInterface;
import jpos.JposException;

/**
 * Interface for methods that implement property setter and method calls for the BillAcceptor device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Bill Acceptor.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface BillAcceptorInterface  extends JposBaseInterface {
    /**
     * Final part of setting CurrencyCode. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>code is one of the values specified in DepositCodeList.</li>
     * </ul>
     *
     * @param code New value for CurrencyCode property.
     * @throws JposException If an error occurs.
     */
    public void currencyCode(String code) throws JposException;

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
     *     <li>cashCounts start with ";" to indicate adjustment of bills,</li>
     *     <li>the remainder of cashCounts consists of a comma separated, not empty list of integer value pairs
     *     separated by double-point.</li>
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
     *     <li>Device is enabled.</li>
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
     *     <li>success is DEPOSIT_COMPLETE.</li>
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
     *     <li>Device is enabled.</li>
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
}
