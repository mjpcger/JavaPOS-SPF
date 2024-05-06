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
import jpos.config.JposEntry;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;

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
    @Deprecated
    public BillDispenserService addDevice(int index, JposDevice dev) throws JposException {
        return addDevice(index, dev, CurrentEntry);
    }

    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index BillDispenser  property set index.
     * @param dev BillDispenser implementation instance derived from JposDevice to be used by the service.
     * @param entry Property list from jpos configuration.
     * @return BillDispenserService object.
     * @throws JposException If property set could not be retrieved.
     */
    public BillDispenserService addDevice(int index, JposDevice dev, JposEntry entry) throws JposException {
        BillDispenserProperties props = dev.getBillDispenserProperties(index);
        validateJposConfiguration(props, dev, dev.ClaimedBillDispenser, entry);
        BillDispenserService service = (BillDispenserService) (props.EventSource = new BillDispenserService(props, dev));
        dev.changeDefaults(props);
        check(props.CurrencyCode == null, JPOS_E_NOSERVICE, "Missing initialization of CurrencyCode property");
        check(props.CurrencyCashList == null, JPOS_E_NOSERVICE, "Missing initialization of CurrencyCashList property");
        check(props.CurrencyCodeList == null, JPOS_E_NOSERVICE, "Missing initialization of CurrencyCodeList property");
        check(props.ExitCashList == null, JPOS_E_NOSERVICE, "Missing initialization of ExitCashList property");
        props.addProperties(dev.BillDispensers);
        service.DeviceInterface = service.BillDispenserInterface = props;
        return service;
    }
}
