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

package de.gmxhome.conrad.jpos.jpos_base.fiscalprinter;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.config.JposEntry;

import java.lang.reflect.Constructor;

/**
 * General part of FiscalPrinterService factory for JPOS devices using this framework.
 */
public class Factory extends JposDeviceFactory {
    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index FiscalPrinter property set index.
     * @param dev   FiscalPrinter implementation instance derived from JposDevice to be used by the service.
     * @return FiscalPrinterService object.
     * @throws JposException If property set could not be retrieved.
     */
    @Deprecated
    public FiscalPrinterService addDevice(int index, JposDevice dev) throws JposException {
        FiscalPrinterProperties props = dev.getFiscalPrinterProperties(index);
        JposDevice.check(props == null, JposConst.JPOS_E_FAILURE, "Missing implementation of getFiscalPrinterProperties()");
        FiscalPrinterService service = (FiscalPrinterService)(props.EventSource = new FiscalPrinterService(props, dev));
        props.Device = dev;
        props.Claiming = dev.ClaimedFiscalPrinter;
        dev.changeDefaults(props);
        props.addProperties(dev.FiscalPrinters);
        service.DeviceInterface = service.FiscalPrinterInterface = props;
        return service;
    }

    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index FiscalPrinter property set index.
     * @param dev   FiscalPrinter implementation instance derived from JposDevice to be used by the service.
     * @param entry Jpos entries, to be checked for FiscalPrinter specific properties.
     * @return FiscalPrinterService object.
     * @throws JposException If property set could not be retrieved.
     */
    public FiscalPrinterService addDevice(int index, JposDevice dev, JposEntry entry) throws JposException {
        FiscalPrinterProperties props = dev.getFiscalPrinterProperties(index);
        Object o = entry.getPropertyValue("CurrencyStringWithDecimalPoint");
        boolean currencyStringWithDecimalPoint = o != null ? Boolean.parseBoolean(o.toString()) : true;
        JposDevice.check(props == null, JposConst.JPOS_E_FAILURE, "Missing implementation of getFiscalPrinterProperties()");
        FiscalPrinterService service = (FiscalPrinterService)(props.EventSource = new FiscalPrinterService(props, dev, currencyStringWithDecimalPoint));
        props.Device = dev;
        props.Claiming = dev.ClaimedFiscalPrinter;
        dev.changeDefaults(props);
        props.addProperties(dev.FiscalPrinters);
        service.DeviceInterface = service.FiscalPrinterInterface = props;
        return service;
    }
}
