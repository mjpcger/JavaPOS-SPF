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
 */

package de.gmxhome.conrad.jpos.jpos_base;

import jpos.JposConst;
import jpos.JposException;

/**
 * Output request class for subsystem unit devices.
 */
public class UnitOutputRequest extends JposOutputRequest {
    /**
     * Retrieves parameter units of all subsystem unit device methods using OutputRequest for processing.
     * See UPOS specification of the specific method for further information.
     * @return Value of method parameter units.
     */
    public int getUnits() {
        return Units;
    };

    private int Units;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param units Units where status has been changed.
     */
    public UnitOutputRequest(JposCommonProperties props, int units) {
        super(props);
        Units = units;
    }

    @Override
    public JposErrorEvent createErrorEvent(JposException ex) {
        UnitOutputRequest current = (UnitOutputRequest)Device.CurrentCommand;
        return new UnitOutputErrorEvent(current.Props.EventSource, ex.getErrorCode(), ex.getErrorCodeExtended(), current.Units, ex.getMessage());
    }

    @Override
    public void clearAll() {
        UnitOutputRequest current;
        synchronized (Device.AsyncProcessorRunning) {
            for (int i = 0; i < Props.SuspendedCommands.size();) {
                current = (UnitOutputRequest)Props.SuspendedCommands.get(i);
                if ((current.Units & Units) != 0) {
                    Props.SuspendedCommands.remove(i);
                } else {
                    ++i;
                }
            }
            for (int i = 0; i < Device.PendingCommands.size();) {
                current = (UnitOutputRequest)Device.PendingCommands.get(i);
                if (current.Props == Props && (current.Units & Units) != 0) {
                    Device.PendingCommands.remove(i);
                } else {
                    i++;
                }
            }
        }
        current = (UnitOutputRequest)Device.CurrentCommand;
        if (current != null && current.Props == Props && (current.Units & Units) != 0)
            current.abortCommand();
    }

    /**
     * Checks whether all units specified by Units are online.
     * @throws JposException If not all specified units are online.
     */
    protected void checkUnitsOnline() throws JposException {
        JposBase svc = (JposBase) Props.EventSource;
        svc.check((~Props.UnitsOnline & getUnits()) != 0, ~Props.UnitsOnline & getUnits(), JposConst.JPOS_E_OFFLINE, 0, "Display units specified by " + (getUnits() & ~Props.UnitsOnline) + " offline", EndSync != null);
    }

}
