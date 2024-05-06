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

import static jpos.CoinDispenserConst.*;

/**
 * Class containing the CoinDispenser specific properties, their default values and default implementations of
 * CoinDispenserInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Coin Dispenser.
 */
public class CoinDispenserProperties extends JposCommonProperties implements CoinDispenserInterface {
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
     * UPOS property DispenserStatus.
     */
    public int DispenserStatus = 0;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    public CoinDispenserProperties(int dev) {
        super(dev);
    }

    @Override
    public void initOnOpen() {
        super.initOnOpen();
    }

    @Override
    public void initOnEnable(boolean enable) {
        super.initOnEnable(enable);
        DispenserStatus = COIN_STATUS_OK;
    }

    @Override
    public void adjustCashCounts(String cashCounts) throws JposException {
    }

    @Override
    public void readCashCounts(String[] cashCounts, boolean[] discrepancy) throws JposException {
    }

    @Override
    public void dispenseChange(int amount) throws JposException {
    }
}
