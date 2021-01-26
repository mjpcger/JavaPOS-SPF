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

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import jpos.BillDispenserConst;
import jpos.JposException;

/**
 * Class containing the bill dispenser specific properties, their default values and default implementations of
 * BillDispenserInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Bill Dispenser.
 */
public class BillDispenserProperties extends JposCommonProperties implements BillDispenserInterface {
    /**
     * UPOS property AsyncResultCode. Default: STATUS_OK. Will be set when throwing a BillDispenserStatusUpdateEvent with
     * STATUS_ASYNC.
     */
    public int AsyncResultCode = BillDispenserConst.BDSP_STATUS_OK;

    /**
     * UPOS property AsyncResultCodeExtended. Default: 0. Will be set when throwing a BillDispenserStatusUpdateEvent
     * with STATUS_ASYNC.
     */
    public int AsyncResultCodeExtended = 0;

    /**
     * UPOS property CapDiscrepancy. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapDiscrepancy = false;

    /**
     * UPOS property CapEmptySensor. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapEmptySensor = false;

    /**
     * UPOS property CapJamSensor. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapJamSensor = false;

    /**
     * UPOS property CapNearEmptySensor. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapNearEmptySensor = false;

    /**
     * UPOS property CurrencyCashList. Default: false. Must be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String CurrencyCashList = null;

    /**
     * UPOS property CurrencyCode. Default: false. Must be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String CurrencyCode = null;

    /**
     * UPOS property CurrencyCodeList. Default: false. Must be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String CurrencyCodeList = null;

    /**
     * UPOS property CurrentExit. Default: 1. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CurrentExit = 1;

    /**
     * UPOS property DeviceExits. Default: 1. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int DeviceExits = 1;

    /**
     * UPOS property DeviceStatus.
     */
    public Integer DeviceStatus = null;

    /**
     * Default value of DeviceStatus property. Default: null. Must be updated
     * before calling initOnEnable the first time.
     */
    public Integer DeviceStatusDef = null;

    /**
     * UPOS property ExitCashList. Default: null. Must be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String ExitCashList = null;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected BillDispenserProperties(int dev) {
        super(dev);
        ExclusiveUse = ExclusiveYes;
    }

    @Override
    public void initOnEnable(boolean enable) {
        super.initOnEnable(enable);
        if (enable) {
            initOnFirstEnable();
            DeviceStatus = DeviceStatusDef;
        }
    }

    @Override
    public void currencyCode(String code) throws JposException {
        CurrencyCode = code;
    }

    @Override
    public void currentExit(int i) throws JposException {
        CurrentExit = i;
    }

    @Override
    public void adjustCashCounts(String cashCounts) throws JposException {
    }

    @Override
    public void readCashCounts(String[] cashCounts, boolean[] discrepancy) throws JposException {
    }

    @Override
    public DispenseCash dispenseCash(String cashCounts) throws JposException {
        return new DispenseCash(this, cashCounts);
    }

    @Override
    public void dispenseCash(DispenseCash request) throws JposException {
    }
}
