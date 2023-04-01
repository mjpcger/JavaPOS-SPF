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

/**
 * General part of BillAcceptor factory for JPOS devices using this framework.
 */
public class Factory extends JposDeviceFactory {
    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index BillAcceptor  property set index.
     * @param dev BillAcceptor implementation instance derived from JposDevice to be used by the service.
     * @return BillAcceptorService object.
     * @throws JposException If property set could not be retrieved.
     */
    public BillAcceptorService addDevice(int index, JposDevice dev) throws JposException {
        BillAcceptorService service;
        BillAcceptorProperties props = dev.getBillAcceptorProperties(index);
        JposDevice.check(props == null, JposConst.JPOS_E_FAILURE, "Missing implementation of getBillAcceptorProperties()");
        service = (BillAcceptorService) (props.EventSource = new BillAcceptorService(props, dev));
        props.Device = dev;
        props.Claiming = dev.ClaimedBillAcceptor;
        dev.changeDefaults(props);
        JposDevice.check(props.CurrencyCode == null, JposConst.JPOS_E_FAILURE, "Missing initialization of CurrencyCode property");
        JposDevice.check(props.DepositCashList == null, JposConst.JPOS_E_FAILURE, "Missing initialization of DepositCashList property");
        JposDevice.check(props.DepositCodeList == null, JposConst.JPOS_E_FAILURE, "Missing initialization of DepositCodeList property");
        JposDevice.check(props.DepositCounts == null, JposConst.JPOS_E_FAILURE, "Missing initialization of DepositCounts property");
        props.addProperties(dev.BillAcceptors);
        service.DeviceInterface = service.BillAcceptorInterface = props;
        return service;
    }
}
