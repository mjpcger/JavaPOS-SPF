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

import java.lang.reflect.Method;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;

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
        return addDevice(index, dev, CurrentEntry);
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
        validateJposConfiguration(props, dev, dev.ClaimedFiscalPrinter, entry);
        FiscalPrinterService service = (FiscalPrinterService)(props.EventSource = new FiscalPrinterService(props, dev));
        dev.changeDefaults(props);
        if (props.DeviceServiceVersion < 1014000) {
            try {   // printRecVoidItem deprecated since 1.11 implies support must be present until version 1.13
                Method voidItem = props.getClass().getMethod("printRecVoidItem", PrintRecVoidItem.class);
                Class<?> defaultImpl = Class.forName("de.gmxhome.conrad.jpos.jpos_base.fiscalprinter.FiscalPrinterProperties");
                check(voidItem.getDeclaringClass() == defaultImpl, 0, "");
            } catch (Exception e) {
                throw new JposException(JPOS_E_NOSERVICE, "Method printRecVoidItem not implemented", e);
            }
        }
        props.addProperties(dev.FiscalPrinters);
        service.DeviceInterface = service.FiscalPrinterInterface = props;
        return service;
    }
}
