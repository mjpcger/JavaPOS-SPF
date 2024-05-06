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
import jpos.config.JposEntry;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;

/**
 * General part of CashChanger factory for JPOS devices using this framework.
 */
public class Factory extends JposDeviceFactory {
    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index CashChanger  property set index.
     * @param dev CashChanger implementation instance derived from JposDevice to be used by the service.
     * @param entry Property list from jpos configuration.
     * @return CashChangerService object.
     * @throws JposException If property set could not be retrieved.
     */
    public CashChangerService addDevice(int index, JposDevice dev, JposEntry entry) throws JposException {
        CashChangerProperties props = dev.getCashChangerProperties(index);
        validateJposConfiguration(props, dev, dev.ClaimedCashChanger, entry);
        CashChangerService service = (CashChangerService) (props.EventSource = new CashChangerService(props, dev));
        dev.changeDefaults(props);
        check(props.CurrencyCode == null, JPOS_E_NOSERVICE, "Missing initialization of CurrencyCode property");
        check(props.CurrencyCashList == null, JPOS_E_NOSERVICE, "Missing initialization of CurrencyCashList property");
        check(props.CurrencyCodeList == null, JPOS_E_NOSERVICE, "Missing initialization of CurrencyCodeList property");
        check(props.ExitCashList == null, JPOS_E_NOSERVICE, "Missing initialization of ExitCashList property");
        check(props.DepositCashList == null, JPOS_E_NOSERVICE, "Missing initialization of DepositCashList property");
        check(props.DepositCodeList == null, JPOS_E_NOSERVICE, "Missing initialization of DepositCodeList property");
        check(props.DepositCounts == null, JPOS_E_NOSERVICE, "Missing initialization of DepositCounts property");
        check(props.RealTimeDataEnabled == null, JPOS_E_NOSERVICE, "Missing initialization of RealTimeDataEnabled property");
        props.addProperties(dev.CashChangers);
        service.DeviceInterface = service.CashChangerInterface = props;
        return service;
    }

    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index CashChanger  property set index.
     * @param dev CashChanger implementation instance derived from JposDevice to be used by the service.
     * @return CashChangerService object.
     * @throws JposException If property set could not be retrieved.
     */
    @Deprecated
    public CashChangerService addDevice(int index, JposDevice dev) throws JposException {
        return addDevice(index, dev, CurrentEntry);
    }
}
