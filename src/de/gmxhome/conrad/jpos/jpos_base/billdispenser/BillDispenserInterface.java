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

package de.gmxhome.conrad.jpos.jpos_base.billdispenser;

import de.gmxhome.conrad.jpos.jpos_base.JposBaseInterface;
import jpos.JposException;

/**
 * Interface for methods that implement property setter and method calls for the BillDispenser device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Bill Dispenser.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface BillDispenserInterface  extends JposBaseInterface {
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
     * Final part of readCashCounts method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>cashCounts is not null and has dimension 1,</li>
     *     <li>discrepancy is not null and has dimension 1.</li>
     * </ul>
     *
     * @param cashCounts  The cash count data is placed into cashCounts.
     * @param discrepancy Specifies whether there is some cash which could not to be included in the counts reported.
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
     *     <li>cashCounts start with ";" to indicate adjustment of bills,</li>
     *     <li>the remainder of cashCounts consists of a comma separated, not empty list of integer value pairs
     *     separated by double-point.</li>
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
}
