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

package de.gmxhome.conrad.jpos.jpos_base.billacceptor;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.config.JposEntry;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;

/**
 * General part of BillAcceptor factory for JPOS devices using this framework.
 */
public class Factory extends JposDeviceFactory {
    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index BillAcceptor  property set index.
     * @param dev BillAcceptor implementation instance derived from JposDevice to be used by the service.
     * @param entry Property list from jpos configuration.
     * @return BillAcceptorService object.
     * @throws JposException If property set could not be retrieved.
     */
    public BillAcceptorService addDevice(int index, JposDevice dev, JposEntry entry) throws JposException {
        BillAcceptorProperties props = dev.getBillAcceptorProperties(index);
        validateJposConfiguration(props, dev, dev.ClaimedBillAcceptor, entry);
        BillAcceptorService service = (BillAcceptorService) (props.EventSource = new BillAcceptorService(props, dev));
        dev.changeDefaults(props);
        check(props.CurrencyCode == null, JPOS_E_NOSERVICE, "Missing initialization of CurrencyCode property");
        check(props.DepositCashList == null, JPOS_E_NOSERVICE, "Missing initialization of DepositCashList property");
        check(props.DepositCodeList == null, JPOS_E_NOSERVICE, "Missing initialization of DepositCodeList property");
        check(props.DepositCounts == null, JPOS_E_NOSERVICE, "Missing initialization of DepositCounts property");
        props.addProperties(dev.BillAcceptors);
        service.DeviceInterface = service.BillAcceptorInterface = props;
        return service;
    }

    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index BillAcceptor  property set index.
     * @param dev BillAcceptor implementation instance derived from JposDevice to be used by the service.
     * @return BillAcceptorService object.
     * @throws JposException If property set could not be retrieved.
     */
    @Deprecated
    public BillAcceptorService addDevice(int index, JposDevice dev) throws JposException {
        return addDevice(index, dev, CurrentEntry);
    }
}
