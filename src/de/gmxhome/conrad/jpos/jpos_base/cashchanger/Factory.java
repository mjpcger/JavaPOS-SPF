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
 * General part of CashChanger factory for JPOS devices using this framework.
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
        JposDevice.check(props == null, JposConst.JPOS_E_FAILURE, "Missing implementation of getCashChangerProperties()");
        service = (CashChangerService) (props.EventSource = new CashChangerService(props, dev));
        props.Device = dev;
        props.Claiming = dev.ClaimedCashChanger;
        dev.changeDefaults(props);
        JposDevice.check(props.CurrencyCode == null, JposConst.JPOS_E_FAILURE, "Missing initialization of CurrencyCode property");
        JposDevice.check(props.CurrencyCashList == null, JposConst.JPOS_E_FAILURE, "Missing initialization of CurrencyCashList property");
        JposDevice.check(props.CurrencyCodeList == null, JposConst.JPOS_E_FAILURE, "Missing initialization of CurrencyCodeList property");
        JposDevice.check(props.ExitCashList == null, JposConst.JPOS_E_FAILURE, "Missing initialization of ExitCashList property");
        JposDevice.check(props.DepositCashList == null, JposConst.JPOS_E_FAILURE, "Missing initialization of DepositCashList property");
        JposDevice.check(props.DepositCodeList == null, JposConst.JPOS_E_FAILURE, "Missing initialization of DepositCodeList property");
        JposDevice.check(props.DepositCounts == null, JposConst.JPOS_E_FAILURE, "Missing initialization of DepositCounts property");
        JposDevice.check(props.RealTimeDataEnabled == null, JposConst.JPOS_E_FAILURE, "Missing initialization of RealTimeDataEnabled property");
        props.addProperties(dev.CashChangers);
        service.DeviceInterface = service.CashChangerInterface = props;
        return service;
    }
}
