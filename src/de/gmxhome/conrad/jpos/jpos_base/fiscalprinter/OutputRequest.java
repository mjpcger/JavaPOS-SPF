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

import static jpos.FiscalPrinterConst.*;

/**
 * Output request class for fiscal printers.
 */
public class OutputRequest extends JposOutputRequest {
    /**
     * Fiscal Printer error station. This value will be passed to the ErrorStation property in error case during asynchronous
     * processing. Default value: The same station as specified by FiscalReceiptStation.
     */
    public int Station;

    /**
     * Error level. This value will be passed to the ErrorLevel property in error case during asynchronous processing.
     * Default value: EL_RECOVERABLE.
     */
    public int Level;

    /**
     * Constructor. Stores default values for Level and Station for later use.
     *
     * @param props Property set of device service.
     */
    public OutputRequest(FiscalPrinterProperties props) {
        super(props);
        Level = FPTR_EL_RECOVERABLE;
        Station = props.CapFiscalReceiptStation && props.FiscalReceiptStation == FPTR_RS_SLIP ?
                FPTR_S_SLIP : FPTR_S_RECEIPT;
    }

    /**
     * Error event creator. Level and Station will be taken from this object and
     * State from PrinterState of the property set unless the given exception is a FiscalPrinterException.
     * In the latter case, Level, Station and State will be taken from the exception.
     * OutputID will always be taken from this object.
     * @param ex JposException which is the originator of an error event.
     * @return FiscalPrinterErrorEvent with values from exception and corresponding
     */
    @Override
    public JposErrorEvent createErrorEvent(JposException ex) {
        FiscalPrinterProperties props = (FiscalPrinterProperties) Props;
        if (ex instanceof FiscalPrinterException fex) {
            return new FiscalPrinterErrorEvent(Props.EventSource, ex.getErrorCode(), ex.getErrorCodeExtended(), fex.Level, OutputID, fex.State, fex.Station, ex.getMessage());
        }
        return new FiscalPrinterErrorEvent(Props.EventSource, ex.getErrorCode(), ex.getErrorCodeExtended(), Level, OutputID, props.PrinterState, Station, ex.getMessage());
    }

    @Override
    public JposStatusUpdateEvent createIdleEvent() {
        return new FiscalPrinterStatusUpdateEvent(Device.CurrentCommand.Props.EventSource, Device.CurrentCommand.Props.FlagWhenIdleStatusValue);
    }

    /**
     * This function should be called whenever a printing method has been invoked. Therefore, it is the perfect place to
     * signal processing has finished to implicitly waiting synchronous methods.
     *
     * @throws JposException If the invoked method throws an exception.
     */
    @Override
    public void invoke() throws JposException {
        synchronized (Device.AsyncProcessorRunning) {
            SyncObject waiter = ((FiscalPrinterProperties) Props).IdleWaiter;
            if (Device.PendingCommands.size() == 0 && Device.CurrentCommand.Props.SuspendedCommands.size() == 0 && waiter != null) {
                ((FiscalPrinterProperties) Props).IdleWaiter = null;
                waiter.signal();
            }
        }
    }

    /**
     * Sets SyncObject to be signalled when device becomes idle.
     * @param dev   Device to be used.
     * @return      SyncObject to wait for idle, if not idle. null if device is idle.
     */
    @SuppressWarnings("SynchronizeOnNonFinalField")
    static public SyncObject setIdleWaiter(JposDevice dev) {
        synchronized (dev.AsyncProcessorRunning) {
            synchronized(dev.ClaimedFiscalPrinter) {
                if (dev.PendingCommands.size() != 0 ||
                        dev.ClaimedFiscalPrinter[0].SuspendedCommands.size() != 0 ||
                        dev.ClaimedFiscalPrinter[0].SuspendedConcurrentCommands.size() != 0) {
                    return dev.ClaimedFiscalPrinter[0].IdleWaiter = new SyncObject();
                }
            }
        }
        return null;
    }
}
