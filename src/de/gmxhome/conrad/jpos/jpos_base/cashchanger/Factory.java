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

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * CashChanger part of Belt factory for JPOS devices using this framework.
 */
public class Factory extends JposDeviceFactory {
    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index CashChanger  property set index.
     * @param dev CashChanger implementation instance derived from JposDevice to be used by the service.
     * @return CashChangerService object.
     * @throws JposException If property set could not be retrieved.
     */
    public CashChangerService addDevice(int index, JposDevice dev) throws JposException {
        CashChangerService service;
        CashChangerProperties props = dev.getCashChangerProperties(index);
        dev.check(props == null, JposConst.JPOS_E_FAILURE, "Missing implementation of getCashChangerProperties()");
        service = (CashChangerService) (props.EventSource = new CashChangerService(props, dev));
        props.Device = dev;
        props.addProperties(dev.Belts);
        props.Claiming = dev.ClaimedBelt;
        dev.changeDefaults(props);
        dev.check(props.CurrencyCode == null, JposConst.JPOS_E_FAILURE, "Missing initialization of CurrencyCode property");
        dev.check(props.CurrencyCashList == null, JposConst.JPOS_E_FAILURE, "Missing initialization of CurrencyCashList property");
        dev.check(props.CurrencyCodeList == null, JposConst.JPOS_E_FAILURE, "Missing initialization of CurrencyCodeList property");
        dev.check(props.ExitCashList == null, JposConst.JPOS_E_FAILURE, "Missing initialization of ExitCashList property");
        dev.check(props.DepositCashList == null, JposConst.JPOS_E_FAILURE, "Missing initialization of DepositCashList property");
        dev.check(props.DepositCodeList == null, JposConst.JPOS_E_FAILURE, "Missing initialization of DepositCodeList property");
        dev.check(props.DepositCounts == null, JposConst.JPOS_E_FAILURE, "Missing initialization of DepositCounts property");
        dev.check(props.RealTimeDataEnabled == null, JposConst.JPOS_E_FAILURE, "Missing initialization of RealTimeDataEnabled property");
        service.DeviceInterface = service.CashChangerInterface = props;
        return service;
    }
}
