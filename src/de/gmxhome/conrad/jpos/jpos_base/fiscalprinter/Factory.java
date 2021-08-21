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

import java.lang.reflect.Constructor;

/**
 * General part of FiscalPrinterService factory for JPOS devices using this framework.
 */
public class Factory extends JposDeviceFactory {
    private static class MyConstants implements FiscalPrinterConst {}

    /**
     * Perform basic initialization of given device and property set. Links property
     * set and driver to each other and sets driver specific property defaults.
     * @param index FiscalPrinter property set index.
     * @param dev   FiscalPrinter implementation instance derived from JposDevice to be used by the service.
     * @return FiscalPrinterService object.
     * @throws JposException If property set could not be retrieved.
     */
    public FiscalPrinterService addDevice(int index, JposDevice dev) throws JposException {
        FiscalPrinterProperties props = dev.getFiscalPrinterProperties(index);
        dev.check(props == null, JposConst.JPOS_E_FAILURE, "Missing implementation of getFiscalPrinterProperties()");
        FiscalPrinterService service = null;
        try {
            MyConstants.class.getField("FPTR_CC_GERMANY").getInt(null);
            Constructor[] constructors = Class.forName("de.gmxhome.conrad.jpos.jpos_base.fiscalprinter.FiscalPrinterServiceX").getConstructors();
            service = (FiscalPrinterService) constructors[0].newInstance(props, dev);

        } catch (Throwable e) {}
        service = (FiscalPrinterService)(props.EventSource = service == null ? new FiscalPrinterService(props, dev) : service);
        props.Device = dev;
        props.addProperties(dev.FiscalPrinters);
        props.Claiming = dev.ClaimedFiscalPrinter;
        dev.changeDefaults(props);
        service.DeviceInterface = service.FiscalPrinterInterface = props;
        return service;
    }
}
