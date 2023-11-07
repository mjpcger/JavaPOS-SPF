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

package de.gmxhome.conrad.jpos.jpos_base;

import jpos.JposConst;
import jpos.JposException;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to invoke an input method synchronously or asynchronously, depending on AsyncMode property. Does neither change
 * OutputID nor fires OutputCompleteEvents.<br>
 * Classes that use JposInputRequests should rewrite newJposOutputRequest(), the JposOutputRequest class factory in
 * the property set class, to return an instance of JposInputRequest instead to avoid wrong request handling.
 */
public class JposInputRequest extends JposOutputRequest {
    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     */
    public JposInputRequest(JposCommonProperties props) {
        super(props);
    }

    @Override
    public JposErrorEvent createErrorEvent(JposException ex) {
        return new JposErrorEvent(Props.EventSource, ex.getErrorCode(), ex.getErrorCodeExtended(), JposConst.JPOS_EL_INPUT, ex.getMessage());
    }

    @Override
    public JposOutputCompleteEvent createOutputEvent() {
        return null;
    }

    @Override
    public void enqueue() throws JposException {
        synchronized (Device.AsyncProcessorRunning) {
            OutputID = -1;
            if (Props.State == JposConst.JPOS_S_ERROR) {
                Props.SuspendedCommands.add(this);
            }
            else {
                Device.PendingCommands.add(this);
                if (Device.AsyncProcessorRunning[0] == null) {
                    (Device.AsyncProcessorRunning[0] = new JposRequestThread(Device)).start();
                }
            }
        }
    }

    @Override
    public void reactivate(boolean queries) {
        synchronized (Device.AsyncProcessorRunning) {
            int i = 0;
            while (Props.SuspendedCommands.size() > 0) {
                JposOutputRequest request = Props.SuspendedCommands.get(0);
                if ((request instanceof JposInputRequest) == queries) {
                    if (!queries && Props.State != JposConst.JPOS_S_BUSY) {
                        Props.State = JposConst.JPOS_S_BUSY;
                        Props.EventSource.logSet("State");
                    }
                    Device.PendingCommands.add(Props.SuspendedCommands.get(i));
                    Props.SuspendedCommands.remove(i);
                }
                else
                    i++;
            }
            if (Device.PendingCommands.size() > 0 && Device.AsyncProcessorRunning[0] == null) {
                (Device.AsyncProcessorRunning[0] = new JposRequestThread(this)).start();
            }
        }
    }
}
