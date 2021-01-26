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

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import jpos.CashChangerConst;
import jpos.JposException;

/**
 * Class containing the cash changer specific properties, their default values and default implementations of
 * CashChangerInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Cash Changer.
 */
public class CashChangerProperties extends JposCommonProperties implements CashChangerInterface {
    /**
     * UPOS property AsyncResultCode. Default: null. Will be set when throwing a BillDispenserStatusUpdateEvent with
     * STATUS_ASYNC.
     */
    public int AsyncResultCode = CashChangerConst.CHAN_STATUS_OK;

    /**
     * UPOS property AsyncResultCodeExtended. Default: null. Will be set when throwing a BillDispenserStatusUpdateEvent
     * with STATUS_ASYNC.
     */
    public int AsyncResultCodeExtended = 0;

    /**
     * UPOS property CapDeposit. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapDeposit = false;

    /**
     * UPOS property CapDepositDataEvent. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapDepositDataEvent = false;

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
     * UPOS property CapFullSensor. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapFullSensor = false;

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
     * UPOS property CapNearFullSensor. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapNearFullSensor = false;

    /**
     * UPOS property CapPauseDeposit. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapPauseDeposit = false;

    /**
     * UPOS property CapRealTimeData. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRealTimeData = false;

    /**
     * UPOS property CapRepayDeposit. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRepayDeposit = false;

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
     * UPOS property CurrentService. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CurrentService = 0;

    /**
     * UPOS property DepositAmount. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int DepositAmount = 0;

    /**
     * UPOS property DepositCashList. Default: null. Must be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String DepositCashList = null;

    /**
     * UPOS property DepositCodeList. Default: null. Must be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String DepositCodeList = null;

    /**
     * UPOS property DepositCounts. Default: null. Must be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String DepositCounts = null;

    /**
     * UPOS property DepositStatus.
     */
    public Integer DepositStatus = null;

    /**
     * Default value of DepositStatus property. Default: null. Must be updated
     * before calling initOnEnable the first time.
     */
    public Integer DepositStatusDef = null;

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
     * UPOS property FullStatus.
     */
    public Integer FullStatus = null;

    /**
     * Default value of FullStatus property. Default: null. Must be overwritten
     * before calling initOnEnable the first time.
     */
    public Integer FullStatusDef = null;

    /**
     * UPOS property RealTimeDataEnabled. Default: null. Must be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public Boolean RealTimeDataEnabled = null;

    /**
     * UPOS property ServiceCount. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int ServiceCount = 0;

    /**
     * UPOS property ServiceIndex. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int ServiceIndex = 0;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected CashChangerProperties(int dev) {
        super(dev);
        ExclusiveUse = ExclusiveYes;
    }

    @Override
    public void initOnEnable(boolean enable) {
        super.initOnEnable(enable);
        if (enable) {
            initOnFirstEnable();
            DeviceStatus = DeviceStatusDef;
            DepositStatus = DepositStatusDef;
            FullStatus = FullStatusDef;
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
    public void currentService(int i) throws JposException {
        CurrentService = i;
    }

    @Override
    public void realTimeDataEnabled(boolean flag) throws JposException {
        RealTimeDataEnabled = flag;
    }

    @Override
    public void adjustCashCounts(String cashCounts) throws JposException {
    }

    /**
     * Default implementation of BeginDeposit simply sets DepositStatus to STATUS_DEPOSIT_START.
     * @throws JposException never.
     */
    @Override
    public void beginDeposit() throws JposException {
        DepositStatus = CashChangerConst.CHAN_STATUS_DEPOSIT_START;
        EventSource.logSet("DepositStatus");
    }

    /**
     * Default implementation of EndDeposit simply sets DepositStatus to STATUS_DEPOSIT_END.
     * @throws JposException never.
     */
    @Override
    public void endDeposit(int success) throws JposException {
        DepositStatus = CashChangerConst.CHAN_STATUS_DEPOSIT_END;
        EventSource.logSet("DepositStatus");
    }

    /**
     * Default implementation of FixDeposit simply sets DepositStatus to STATUS_DEPOSIT_COUNT.
     * @throws JposException never.
     */
    @Override
    public void fixDeposit() throws JposException {
        DepositStatus = CashChangerConst.CHAN_STATUS_DEPOSIT_COUNT;
        EventSource.logSet("DepositStatus");
    }

    @Override
    public void pauseDeposit(int control) throws JposException {
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

    @Override
    public DispenseChange dispenseChange(int amount) throws JposException {
        return new DispenseChange(this, amount);
    }

    @Override
    public void dispenseChange(DispenseChange request) throws JposException {
    }
}
