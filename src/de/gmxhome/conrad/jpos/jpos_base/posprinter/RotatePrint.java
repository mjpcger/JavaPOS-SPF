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

import java.util.ArrayList;
import java.util.List;

/**
 * Output request executor for POSPrinter method RotatePrint.
 */
public class RotatePrint extends OutputRequest {
    /**
     * POSPrinter method RotatePrint parameter station, see UPOS specification.
     * @return RotatePrint parameter <i>station</i>.
     */
    public int getStation() {
        return Station;
    }

    private int Station;

    /**
     * POSPrinter method RotatePrint parameter rotation, see UPOS specification.
     * @return RotatePrint parameter <i>rotation</i>.
     */
    public int getRotation() {
        return Rotation;
    }

    private int Rotation;

    /**
     * List holds all outstanding output requests.
     */
    public List<OutputRequest> SidewaysCommands = new ArrayList<OutputRequest>();

    /**
     * Adds an output request to the request queue.
     * @param request Request to be enqueued.
     * @throws JposException if request is null (specifying synchronous method implementation).
     */
    public synchronized void addMethod(OutputRequest request) throws JposException {
        Props.Device.check(request == null, JposConst.JPOS_E_FAILURE, "Rotate print mode not supported for synchronous implementation");
        SidewaysCommands.add(request);
    }

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param station The printer station to be used. May be either S_RECEIPT or S_SLIP.
     * @param rotation Direction of rotation. One of RP_RIGHT90, RP_LEFT90 or RP_ROTATE180, optionally ORed with one of
     *                 RP_BARCODE or RP_BITMAP, or RP_NORMAL.
     */
    public RotatePrint(JposCommonProperties props, int station, int rotation) {
        super(props);
        Station = station;
        Rotation = rotation;
    }

    @Override
    public void invoke() throws JposException {
        POSPrinterService svc = (POSPrinterService)Props.EventSource;
        if (EndSync == null) {
            svc.extendedErrorCheck(getStation());
        }
        svc.POSPrinterInterface.rotatePrint(this);
        for (OutputRequest request : SidewaysCommands) {
            Device.check (Abort != null, JposConst.JPOS_E_FAILURE, "Rotate print interrupted");
            request.invoke();
        }
    }
}
