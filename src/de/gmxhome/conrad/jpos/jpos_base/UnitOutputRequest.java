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

import jpos.*;

import java.util.*;

import static jpos.JposConst.*;

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

    private final int Units;

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
    public void clearOutput() {
        List<UnitOutputRequest> current = new ArrayList<>();
        UnitOutputRequest req;
        synchronized (Device.AsyncProcessorRunning) {
            for (int i = 0; i < Props.SuspendedCommands.size();) {
                req = (UnitOutputRequest)Props.SuspendedCommands.get(i);
                if ((req.Units & Units) != 0) {
                    Props.SuspendedCommands.remove(i);
                } else {
                    ++i;
                }
            }
            for (int i = 0; i < Props.SuspendedConcurrentCommands.size();) {
                req = (UnitOutputRequest)Props.SuspendedConcurrentCommands.get(i);
                if ((req.Units & Units) != 0) {
                    Props.SuspendedCommands.remove(i);
                } else {
                    ++i;
                }
            }
            for (int i = 0; i < Device.PendingCommands.size();) {
                if (Device.PendingCommands.get(i) instanceof UnitOutputRequest &&
                        (req = (UnitOutputRequest)Device.PendingCommands.get(i)).Props == Props && (req.Units & Units) != 0) {
                    Device.PendingCommands.remove(i);
                } else {
                    i++;
                }
            }
            if (Device.CurrentCommand instanceof UnitOutputRequest) {
                req = (UnitOutputRequest)Device.CurrentCommand;
                if (req.Props == Props && (req.Units & Units) != 0)
                    current.add(req);
            }
            if (Props.CurrentCommands != null) {
                for (JposOutputRequest request : Props.CurrentCommands) {
                    if (request instanceof UnitOutputRequest) {
                        req = (UnitOutputRequest) request;
                        if ((req.Units & Units) != 0)
                            current.add(req);
                    }
                }
            }
        }
        for (UnitOutputRequest request : current)
            request.abortCommand(true);
    }

    /**
     * Checks whether all units specified by Units are online.
     * @throws JposException If not all specified units are online.
     */
    protected void checkUnitsOnline() throws JposException {
        JposBase svc = (JposBase) Props.EventSource;
        svc.check((~Props.UnitsOnline & getUnits()) != 0, ~Props.UnitsOnline & getUnits(), JPOS_E_OFFLINE, 0, "Display units specified by " + (getUnits() & ~Props.UnitsOnline) + " offline", EndSync != null);
    }

}
