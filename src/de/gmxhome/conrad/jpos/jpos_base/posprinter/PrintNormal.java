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
 * Output request executor for POSPrinter method PrintNormal.
 */
public class PrintNormal extends OutputPrintRequest {
    /**
     * POSPrinter method PrintNormal parameter station, see UPOS specification.
     * @return PrintNormal parameter <i>station</i>.
     */
    public int getStation() {
        return Station;
    }

    private final int Station;

    /**
     * POSPrinter method PrintNormal parameter data, converted to a List&lt;Object&gt; with POSPrinterService method outputDataParts.
     * @return List&lt;Object&gt; containing print data contained in PrintNormal parameter data.
     */
    public List<POSPrinterService.PrintDataPart> getData() {
        return Data;
    }

    private final List<POSPrinterService.PrintDataPart> Data;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param station The printer station to be used. May be either S_JOURNAL, S_RECEIPT or S_SLIP.
     * @param data The characters to be printed. May consist of printable characters, escape sequences,
     *            carriage returns (13 decimal), and line feeds (10 decimal).
     */
    public PrintNormal(JposCommonProperties props, int station, String data) {
        super(props);
        Station = station;
        Data = data == null ? new ArrayList<>() : ((POSPrinterService)props.EventSource).outputDataParts(data);
    }

    @Override
    public void invoke() throws JposException {
        POSPrinterService svc = (POSPrinterService)Props.EventSource;
        if (EndSync == null) {
            svc.extendedErrorCheck(getStation());
        }
        svc.POSPrinterInterface.printNormal(this);
    }
}
