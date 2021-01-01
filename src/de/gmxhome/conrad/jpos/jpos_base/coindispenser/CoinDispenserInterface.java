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

package de.gmxhome.conrad.jpos.jpos_base.coindispenser;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Interface for methods that implement property setter and method calls for the CoinDispenser device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Coin Dispenser.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface CoinDispenserInterface extends JposBaseInterface {
    /**
     * Final part of AdjustCashCounts method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>cashCounts is a string consisting of comma separated pairs of double-point separated positive integer values,</li>
     *     <li>The second value of each pair is &gt; 0,</li>
     * </ul>
     *
     * @param cashCounts      The cashCounts parameter contains cash types and amounts to be initialized.
     * @throws JposException  If an error occurs.
     */
    public void adjustCashCounts(String cashCounts) throws JposException;

    /**
     * Final part of ReadCashCounts method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Both parameters are arrays with dimension 1.</li>
     * </ul>
     *
     * @param cashCounts  The cash count data is placed into cashCounts.
     * @param discrepancy If discrepancy is set to true by this method, then there is some cash which was not able
     *                    to be included in the counts reported in cashCounts. Otherwise it is set to false.
     * @throws JposException If an error occurs.
     */
    public void readCashCounts(String[] cashCounts, boolean[] discrepancy) throws JposException;

    /**
     * Final part of DispenseChange method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>The given amount is &gt; 0.</li>
     * </ul>
     *
     * @param amount    The amount parameter contains the amount of change to be dispensed.
     * @throws JposException  If an error occurs.
     */
    public void dispenseChange(int amount) throws JposException;
}
