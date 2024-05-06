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

import static jpos.JposConst.*;

/**
 * ItemDispenser service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class ItemDispenserService extends JposBase implements ItemDispenserService116 {
    /**
     * Instance of a class implementing the ItemDispenserInterface for item dispenser specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public ItemDispenserInterface ItemDispenserInterface;

    private final ItemDispenserProperties Data;

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
        logPreCall("AdjustItemCount", removeOuterArraySpecifier(new Object[]{itemCount, slotNumber}, Device.MaxArrayStringElements));
        checkEnabled();
        check(itemCount < 0, JPOS_E_ILLEGAL, "Item count negative: " + itemCount);
        check(slotNumber < 1 || slotNumber > Data.MaxSlots, JPOS_E_ILLEGAL, "slotNumber invalid: " + slotNumber);
        ItemDispenserInterface.adjustItemCount(itemCount, slotNumber);
        logCall("AdjustItemCount");
    }

    @Override
    public void dispenseItem(int[] numItem, int slotNumber) throws JposException {
        logPreCall("DispenseItem", removeOuterArraySpecifier(new Object[]{numItem, slotNumber}, Device.MaxArrayStringElements));
        checkEnabled();
        check(numItem == null || numItem.length != 1, JPOS_E_ILLEGAL, "numItem invalid");
        check(numItem[0] <= 0, JPOS_E_ILLEGAL, "numItem <= 0");
        check(slotNumber < 1 || slotNumber > Data.MaxSlots, JPOS_E_ILLEGAL, "slotNumber invalid: " + slotNumber);
        ItemDispenserInterface.dispenseItem(numItem, slotNumber);
        logCall("DispenseItem", removeOuterArraySpecifier(new Object[]{numItem[0], slotNumber}, Device.MaxArrayStringElements));
    }

    @Override
    public void readItemCount(int[] itemCount, int slotNumber) throws JposException {
        logPreCall("ReadItemCount", removeOuterArraySpecifier(new Object[]{"...", slotNumber}, Device.MaxArrayStringElements));
        checkEnabled();
        check(itemCount == null || itemCount.length != 1, JPOS_E_ILLEGAL, "itemCount invalid");
        check(slotNumber < 1 || slotNumber > Data.MaxSlots, JPOS_E_ILLEGAL, "slotNumber invalid: " + slotNumber);
        ItemDispenserInterface.readItemCount(itemCount, slotNumber);
        logCall("ReadItemCount", removeOuterArraySpecifier(new Object[]{itemCount[0], slotNumber}, Device.MaxArrayStringElements));
    }
}
