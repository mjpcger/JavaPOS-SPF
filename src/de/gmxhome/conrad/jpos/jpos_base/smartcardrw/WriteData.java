/*
 * Copyright 2023 Martin Conrad
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
 *
 */

package de.gmxhome.conrad.jpos.jpos_base.smartcardrw;

import de.gmxhome.conrad.jpos.jpos_base.JposOutputRequest;
import jpos.JposException;

/**
 * Output request executor for SmartCardRW method WriteData.
 */
public class WriteData extends JposOutputRequest {
    private final int Count, Action;
    private final String Data;

    /**
     * SmartCardRW method WriteData parameter action, see UPOS specification.
     * @return WriteData parameter action.
     */
    public int getAction() {
        return Action;
    }

    /**
     * SmartCardRW method WriteData parameter count, see UPOS specification.
     * @return WriteData parameter count.
     */
    public int getCount() {
        return Count;
    }

    /**
     * SmartCardRW method WriteData parameter data, see UPOS specification.
     * @return WriteData parameter data.
     */
    public String getData() {
        return Data;
    }

    /**
     * Constructor, stores given parameters for later use of WriteData operation.
     * @param props         Property set of device service.
     * @param action        Starting offset for write operation.
     * @param count         Number of bytes to be written.
     * @param data          Data to be written.
     */
    public WriteData(SmartCardRWProperties props, int action, int count, String data) {
        super(props);
        Action = action;
        Count = count;
        Data = data;
    }

    @Override
    public void invoke() throws JposException {
        ((SmartCardRWService)Props.EventSource).SmartCardRW.writeData(this);
    }
}
