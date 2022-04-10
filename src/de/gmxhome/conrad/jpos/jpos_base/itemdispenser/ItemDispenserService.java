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

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.services.*;

/**
 * ItemDispenser service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class ItemDispenserService extends JposBase implements ItemDispenserService115 {
    /**
     * Instance of a class implementing the ItemDispenserInterface for item dispenser specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public ItemDispenserInterface ItemDispenserInterface;

    private ItemDispenserProperties Data;

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public ItemDispenserService(ItemDispenserProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public boolean getCapEmptySensor() throws JposException {
        checkOpened();
        logGet("CapEmptySensor");
        return Data.CapEmptySensor;
    }

    @Override
    public boolean getCapIndividualSlotStatus() throws JposException {
        checkOpened();
        logGet("CapIndividualSlotStatus");
        return Data.CapIndividualSlotStatus;
    }

    @Override
    public boolean getCapJamSensor() throws JposException {
        checkOpened();
        logGet("CapJamSensor");
        return Data.CapJamSensor;
    }

    @Override
    public boolean getCapNearEmptySensor() throws JposException {
        checkOpened();
        logGet("CapNearEmptySensor");
        return Data.CapNearEmptySensor;
    }

    @Override
    public int getDispenserStatus() throws JposException {
        checkEnabled();
        logGet("DispenserStatus");
        return Data.DispenserStatus;
    }

    @Override
    public int getMaxSlots() throws JposException {
        checkOpened();
        logGet("MaxSlots");
        return Data.MaxSlots;
    }

    @Override
    public void adjustItemCount(int itemCount, int slotNumber) throws JposException {
        logPreCall("AdjustItemCount", "" + itemCount + ", " + slotNumber);
        checkEnabled();
        Device.check(itemCount < 0, JposConst.JPOS_E_ILLEGAL, "Item count negative: " + itemCount);
        Device.check(slotNumber < 1 || slotNumber > Data.MaxSlots, JposConst.JPOS_E_ILLEGAL, "slotNumber invalid: " + slotNumber);
        ItemDispenserInterface.adjustItemCount(itemCount, slotNumber);
        logCall("AdjustItemCount");
    }

    @Override
    public void dispenseItem(int[] numItem, int slotNumber) throws JposException {
        try {
            logPreCall("DispenseItem", "" + numItem[0] + ", " + slotNumber);
        } catch (NullPointerException e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "numItem must not be null");
        }
        checkEnabled();
        Device.check(numItem.length != 1, JposConst.JPOS_E_ILLEGAL, "numItem Bad dimension: " + numItem.length);
        Device.check(slotNumber < 1 || slotNumber > Data.MaxSlots, JposConst.JPOS_E_ILLEGAL, "slotNumber invalid: " + slotNumber);
        ItemDispenserInterface.dispenseItem(numItem, slotNumber);
        logCall("DispenseItem", "" + numItem[0]);
    }

    @Override
    public void readItemCount(int[] itemCount, int slotNumber) throws JposException {
        try {
            if (itemCount[0] > 0 || true)
                logPreCall("ReadItemCount", "" + slotNumber);
        } catch (NullPointerException e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "numItem must not be null");
        }
        checkEnabled();
        Device.check(itemCount.length != 1, JposConst.JPOS_E_ILLEGAL, "numItem Bad dimension: " + itemCount.length);
        Device.check(slotNumber < 1 || slotNumber > Data.MaxSlots, JposConst.JPOS_E_ILLEGAL, "slotNumber invalid: " + slotNumber);
        ItemDispenserInterface.readItemCount(itemCount, slotNumber);
        logCall("ReadItemCount", "" + itemCount[0]);
    }
}
