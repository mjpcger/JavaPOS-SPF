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
import net.bplaced.conrad.log4jpos.Level;

/**
 * Output request executor for POSPrinter method PrintImmediate.
 */
public class PrintImmediate extends PrintNormal {
    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param station The printer station to be used. May be either S_JOURNAL, S_RECEIPT or S_SLIP.
     * @param data The characters to be printed. May consist of printable characters, escape sequences,
     *            carriage returns (13 decimal), and line feeds (10 decimal).
     */
    public PrintImmediate(JposCommonProperties props, int station, String data) {
        super(props, station, data);
    }

    @Override
    public void enqueue() throws JposException {
        int state;
        synchronized (Device.AsyncProcessorRunning) {
            state = Props.State;
            if (state == JposConst.JPOS_S_IDLE)
                Props.State = JposConst.JPOS_S_BUSY;
            OutputID = -1;
            int index = 0;
            while (Device.PendingCommands.size() > index) {
                if (!(Device.PendingCommands.get(index) instanceof PrintImmediate))
                    break;
            }
            Device.PendingCommands.add(index, this);
            if (Device.AsyncProcessorRunning[0] == null) {
                (Device.AsyncProcessorRunning[0] = new JposRequestThread(Device)).start();
            }
        }
        if (state != Props.State)
            Device.log(Level.DEBUG, Props.LogicalName + ": State <- " + Props.State);
    }
}
