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

import jpos.JposException;

import java.util.List;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;

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
        return new JposErrorEvent(Props.EventSource, ex.getErrorCode(), ex.getErrorCodeExtended(), JPOS_EL_INPUT, ex.getMessage());
    }

    @Override
    public JposOutputCompleteEvent createOutputEvent() {
        return null;
    }

    @Override
    public void enqueue() throws JposException {
        synchronized (Device.AsyncProcessorRunning) {
            OutputID = -1;
            if (EndSync == null)
                Props.AsyncInputActive = true;
            Boolean concurrent = Device.concurrentProcessingSupported(this);
            if (concurrent == null || concurrent) {
                if (Props.State == JPOS_S_ERROR)
                    Props.SuspendedConcurrentCommands.add(this);
                else
                    Device.createConcurrentRequestThread(this);
            } else if (Props.State == JPOS_S_ERROR)
                Props.SuspendedCommands.add(this);
            else
                Device.invokeRequestThread(this, null);
        }
    }

    @Override
    public boolean finishAsyncProcessing() {
        boolean processed = super.finishAsyncProcessing();
        synchronized (Device.AsyncProcessorRunning) {
            if (Device.CurrentCommand != this && Device.CurrentCommand instanceof JposInputRequest && Device.CurrentCommand.Props == Props)
                return processed;
            for (List<JposOutputRequest> requests : getArrayOf(0,
                    Device.PendingCommands, Props.SuspendedCommands, Props.SuspendedConcurrentCommands, Props.CurrentCommands)) {
                if (requests != null) {
                    for (JposOutputRequest request : requests) {
                        if (request != this && request instanceof JposInputRequest && request.Props == Props)
                            return processed;
                    }
                }
            }
            Props.AsyncInputActive = false;
        }
        return processed;
    }

    @Override
    public void reactivate(boolean queries) {
        synchronized (Device.AsyncProcessorRunning) {
            int i = 0;
            while (Props.SuspendedCommands.size() > 0) {
                JposOutputRequest request = Props.SuspendedCommands.get(i);
                if ((request instanceof JposInputRequest) == queries) {
                    if (!queries && Props.State != JPOS_S_BUSY) {
                        Props.State = JPOS_S_BUSY;
                        Props.EventSource.logSet("State");
                    }
                    Props.SuspendedCommands.remove(i);
                    Device.invokeRequestThread(request, null);
                }
                else
                    i++;
            }
            i = 0;
            while (Props.SuspendedConcurrentCommands.size() > 0) {
                JposOutputRequest request = Props.SuspendedConcurrentCommands.get(0);
                if ((request instanceof JposInputRequest) == queries) {
                    if (!queries && Props.State != JPOS_S_BUSY) {
                        Props.State = JPOS_S_BUSY;
                        Props.EventSource.logSet("State");
                    }
                    Device.createConcurrentRequestThread(request);
                    Props.SuspendedConcurrentCommands.remove(i);
                }
                else
                    i++;
            }
        }
    }
}
