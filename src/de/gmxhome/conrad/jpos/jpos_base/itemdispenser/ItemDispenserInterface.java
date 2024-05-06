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

package de.gmxhome.conrad.jpos.jpos_base.itemdispenser;

import de.gmxhome.conrad.jpos.jpos_base.JposBaseInterface;
import jpos.JposException;

/**
 * Interface for methods that implement property setter and method calls for the ItemDispenser device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Item Dispenser.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface ItemDispenserInterface extends JposBaseInterface {
    /**
     * Final part of AdjustItemCount method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>itemCount is a positive value,</li>
     *     <li>slotNumber is between 1 and MaxSlots.</li>
     * </ul>
     *
     * @param itemCount     Number of items currently in the specified slot.
     * @param slotNumber    Slot number.
     * @throws JposException If an error occurs.
     */
    public void adjustItemCount(int itemCount, int slotNumber) throws JposException;

    /**
     * Final part of DispenseItem method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>numItem is not null and has length 1,</li>
     *     <li>slotNumber is between 1 and MaxSlots.</li>
     * </ul>
     *
     * @param numItem       Number of items to be dispensed from given slot.
     * @param slotNumber    Slot number to be used to dispense item(s).
     * @throws JposException If an error occurs.
     */
    public void dispenseItem(int[] numItem, int slotNumber) throws JposException;

    /**
     * Final part of ReadItemCount method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>itemCount is not null and has length 1,</li>
     *     <li>itemCount[0] &gt; 0,</li>
     *     <li>slotNumber is between 1 and MaxSlots.</li>
     * </ul>
     *
     * @param itemCount     Target for item count of the specified slot.
     * @param slotNumber    Slot number to be used to read slot specific item count.
     * @throws JposException If an error occurs.
     */
    public void readItemCount(int[] itemCount, int slotNumber) throws JposException;
}
