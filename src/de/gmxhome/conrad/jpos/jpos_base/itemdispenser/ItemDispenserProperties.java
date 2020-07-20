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

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import jpos.JposException;

/**
 * Class containing the item dispenser specific properties, their default values and default implementations of
 * ItemDispenserInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Item Dispenser.
 */
public class ItemDispenserProperties extends JposCommonProperties implements ItemDispenserInterface {
    /**
     * UPOS property DispenserStatus. Must be overwritten
     * by derived objects within the initOnEnable method.
     */
    public int DispenserStatus = 0;

    /**
     * UPOS property CapEmptySensor. Default: false. Can be overwritten by objects derived from JposDevice within
     * the changeDefaults method.
     */
    public boolean CapEmptySensor = false;

    /**
     * UPOS property CapIndividualSlotStatus. Default: false. Can be overwritten by objects derived from JposDevice
     * within the changeDefaults method.
     */
    public boolean CapIndividualSlotStatus = false;

    /**
     * UPOS property CapJamSensor. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapJamSensor = false;

    /**
     * UPOS property CapNearEmptySensor. Default: false.  Can be overwritten by objects derived from JposDevice within
     * the changeDefaults method.
     */
    public boolean CapNearEmptySensor = false;

    /**
     * UPOS property MaxSlots. Default: 1.  Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int MaxSlots = 1;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected ItemDispenserProperties(int dev) {
        super(dev);
        ExclusiveUse = ExclusiveYes;
        FlagWhenIdleStatusValue = -1;   // To avoid FlagWhenIdle handling for CASH_SUE_DRAWERCLOSED
    }

    @Override
    public void adjustItemCount(int itemCount, int slotNumber) throws JposException {
    }

    @Override
    public void dispenseItem(int[] numItem, int slotNumber) throws JposException {
    }

    @Override
    public void readItemCount(int[] itemCount, int slotNumber) throws JposException {
    }
}
