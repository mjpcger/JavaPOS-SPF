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

package de.gmxhome.conrad.jpos.jpos_base.posprinter;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Output request class for printers.
 */
public class OutputRequest extends JposOutputRequest {
    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     */
    public OutputRequest(JposCommonProperties props) {
        super(props);
    }

    @Override
    public JposErrorEvent createErrorEvent(JposException ex) {
        if (ex instanceof POSPrinterException e) {
            return new POSPrinterErrorEvent(Device.CurrentCommand.Props.EventSource, e.getErrorCode(), e.getErrorCodeExtended(), e.Station, e.Level, e.getMessage());
        }
        return super.createErrorEvent(ex);
    }

    @Override
    public JposStatusUpdateEvent createIdleEvent() {
        return new POSPrinterStatusUpdateEvent(Device.CurrentCommand.Props.EventSource, Device.CurrentCommand.Props.FlagWhenIdleStatusValue);
    }
}
