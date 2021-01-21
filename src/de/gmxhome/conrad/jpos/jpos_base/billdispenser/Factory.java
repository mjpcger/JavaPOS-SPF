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

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * General part of BillDispenser factory for JPOS devices using this framework.
 */
public class Factory extends JposDeviceFactory {
    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index BillDispenser  property set index.
     * @param dev BillDispenser implementation instance derived from JposDevice to be used by the service.
     * @return BillDispenserService object.
     * @throws JposException If property set could not be retrieved.
     */
    public BillDispenserService addDevice(int index, JposDevice dev) throws JposException {
        BillDispenserService service;
        BillDispenserProperties props = dev.getBillDispenserProperties(index);
        dev.check(props == null, JposConst.JPOS_E_FAILURE, "Missing implementation of getBillDispenserProperties()");
        service = (BillDispenserService) (props.EventSource = new BillDispenserService(props, dev));
        props.Device = dev;
        props.addProperties(dev.BillDispensers);
        props.Claiming = dev.ClaimedBillDispenser;
        dev.changeDefaults(props);
        dev.check(props.CurrencyCode == null, JposConst.JPOS_E_FAILURE, "Missing initialization of CurrencyCode property");
        dev.check(props.CurrencyCashList == null, JposConst.JPOS_E_FAILURE, "Missing initialization of CurrencyCashList property");
        dev.check(props.CurrencyCodeList == null, JposConst.JPOS_E_FAILURE, "Missing initialization of CurrencyCodeList property");
        dev.check(props.ExitCashList == null, JposConst.JPOS_E_FAILURE, "Missing initialization of ExitCashList property");
        service.DeviceInterface = service.BillDispenserInterface = props;
        return service;
    }
}
