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

package de.gmxhome.conrad.jpos.jpos_base.coinacceptor;

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import jpos.CoinAcceptorConst;
import jpos.CoinDispenser;
import jpos.CoinDispenserConst;
import jpos.JposException;

/**
 * Class containing the coin acceptor specific properties, their default values and default implementations of
 * CoinAcceptorInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Coin Acceptor.
 */
public class CoinAcceptorProperties extends JposCommonProperties implements CoinAcceptorInterface {
    /**
     * UPOS property CapDiscrepancy. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapDiscrepancy = false;

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
     * UPOS property CurrencyCode. Default: null. Must be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String CurrencyCode = null;

    /**
     * UPOS property DepositAmount. Default: 0.
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
    public int DepositStatus = CoinAcceptorConst.CACC_STATUS_DEPOSIT_END;

    /**
     * Default value of DepositStatus property. Default: STATUS_DEPOSIT_END. Should be updated
     * before calling initOnEnable the first time.
     */
    public int DepositStatusDef = CoinAcceptorConst.CACC_STATUS_DEPOSIT_END;

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
     * UPOS property RealTimeDataEnabled. Default: false.
     */
    public boolean RealTimeDataEnabled = false;
    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected CoinAcceptorProperties(int dev) {
        super(dev);
        ExclusiveUse = ExclusiveYes;
    }

    @Override
    public void initOnEnable(boolean enable) {
        super.initOnEnable(enable);
        if (enable) {
            initOnFirstEnable();
            DepositStatus = DepositStatusDef;
            FullStatus = FullStatusDef;
        }
    }

    @Override
    public void currencyCode(String code) throws JposException {
        CurrencyCode = code;
    }

    @Override
    public void realTimeDataEnabled(boolean flag) throws JposException {
        RealTimeDataEnabled = flag;
    }

    @Override
    public void adjustCashCounts(String cashCounts) throws JposException {
    }

    @Override
    public void beginDeposit() throws JposException {
    }

    @Override
    public void endDeposit(int success) throws JposException {
    }

    @Override
    public void fixDeposit() throws JposException {
    }

    @Override
    public void pauseDeposit(int control) throws JposException {
    }

    @Override
    public void readCashCounts(String[] cashCounts, boolean[] discrepancy) throws JposException {
    }
}
