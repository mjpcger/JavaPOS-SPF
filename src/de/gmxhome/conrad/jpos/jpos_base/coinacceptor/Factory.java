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

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * General part of CoinAcceptor factory for JPOS devices using this framework.
 */
public class Factory extends JposDeviceFactory {
    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index CoinAcceptor  property set index.
     * @param dev CoinAcceptor implementation instance derived from JposDevice to be used by the service.
     * @return CoinAcceptorService object.
     * @throws JposException If property set could not be retrieved.
     */
    public CoinAcceptorService addDevice(int index, JposDevice dev) throws JposException {
        CoinAcceptorService service;
        CoinAcceptorProperties props = dev.getCoinAcceptorProperties(index);
        dev.check(props == null, JposConst.JPOS_E_FAILURE, "Missing implementation of getCoinAcceptorProperties()");
        service = (CoinAcceptorService) (props.EventSource = new CoinAcceptorService(props, dev));
        props.Device = dev;
        props.addProperties(dev.CoinAcceptors);
        props.Claiming = dev.ClaimedCoinAcceptor;
        dev.changeDefaults(props);
        dev.check(props.CurrencyCode == null, JposConst.JPOS_E_FAILURE, "Missing initialization of CurrencyCode property");
        dev.check(props.DepositCashList == null, JposConst.JPOS_E_FAILURE, "Missing initialization of DepositCashList property");
        dev.check(props.DepositCodeList == null, JposConst.JPOS_E_FAILURE, "Missing initialization of DepositCodeList property");
        dev.check(props.DepositCounts == null, JposConst.JPOS_E_FAILURE, "Missing initialization of DepositCounts property");
        dev.check(props.RealTimeDataEnabled == null, JposConst.JPOS_E_FAILURE, "Missing initialization of RealTimeDataEnabled property");
        service.DeviceInterface = service.CoinAcceptorInterface = props;
        return service;
    }
}
