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

package de.gmxhome.conrad.jpos.jpos_base.linedisplay;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.linedisplay.*;
import jpos.*;

import java.util.Arrays;

/**
 * Output request executor for LineDisplay method DisplayText.
 */
public class DisplayText extends JposOutputRequest {
    /**
     * LineDisplay property CurrentWindow, see UPOS specification.
     * @return LineDisplayService property <i>CurrentWindow</i>.
     */
    public int getWindow() {
        return Window;
    }

    private int Window;

    /**
     * LineDisplay method DisplayText parameter data, converted to an array of Object with LineDisplayService method outputDataParts.
     * @return Array of LineDisplayService.DisplayDataPart objects representing display data and attributes contained
     * in DisplayText parameter data and attribute.
     */
    public LineDisplayService.DisplayDataPart[] getData() {
        return Arrays.copyOf(Data, Data.length);
    }

    private LineDisplayService.DisplayDataPart[] Data;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param data  Display data, see UPOS specification, method DisplayText.
     * @param attribute Text attributes, see UPOS specification, method DisplayText.
     */
    public DisplayText(LineDisplayProperties props, String data, int attribute) {
        super(props);
        Data = ((LineDisplayService)props.EventSource).outputDataParts(data, attribute);
        Window = props.CurrentWindow;
    }

    @Override
    public void run() {
        JposOutputRequest current;
        while ((current = dequeue()) != null) {
            try {
                current.invoke();
            } catch (JposException e) {}
            current.finished();
            if (current.EndSync != null) {
                synchronized (Device.AsyncProcessorRunning) {
                    if (Device.PendingCommands.size() == 0 && current.Props.SuspendedCommands.size() == 0) {
                        current.Props.State = JposConst.JPOS_S_IDLE;
                        current.Props.EventSource.logSet("State");
                    }
                }
                current.EndSync.signal();
            } else {
                if (current.OutputID == current.Props.OutputID) {
                    synchronized (Device.AsyncProcessorRunning) {
                        current.Props.State = JposConst.JPOS_S_IDLE;
                        current.Props.EventSource.logSet("State");
                    }
                }
            }
        }
    }

    @Override
    public void invoke() throws JposException {
        ((LineDisplayService)Props.EventSource).LineDisplayInterface.displayText(this);
    }
}
