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
 * Output request executor for POSPrinter method PrintTwoNormal.
 */
public class PrintTwoNormal extends OutputRequest {
    /**
     * POSPrinter method PrintTwoNormal parameter station, see UPOS specification.
     * @return PrintTwoNormal parameter <i>station</i>.
     */
    public int getStation() {
        return Station;
    }

    private int Station;

    /**
     * POSPrinter method PrintTwoNormal parameter data1, converted to a List&lt;Object&gt; with POSPrinterService method outputDataParts.
     * @return List&lt;Object&gt; containing print data contained in PrintTwoNormal parameter data1.
     */
    public List<POSPrinterService.PrintDataPart> getData1() {
        return Data1;
    }

    private List<POSPrinterService.PrintDataPart> Data1;

    /**
     * POSPrinter method PrintTwoNormal parameter data2, converted to a List&lt;Object&gt; with POSPrinterService method outputDataParts.
     * @return List&lt;Object&gt; containing print data contained in PrintTwoNormal parameter data2.
     */
    public List<POSPrinterService.PrintDataPart> getData2() {
        return Data2;
    }

    private List<POSPrinterService.PrintDataPart> Data2;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param station The printer station to be used. May one of S_JOURNAL_RECEIPT, S_JOURNAL_SLIP, S_RECEIPT_SLIP,
     *                TWO_RECEIPT_JOURNAL, TWO_SLIP_JOURNAL or TWO_SLIP_RECEIPT.
     * @param data1 The characters to be printed on the first printer station. May consist of printable characters,
     *             escape sequences, carriage returns (13 decimal), and line feeds (10 decimal). The characters must all
     *             fit on one printed line, so that the printer may attempt to print on both stations simultaneously.
     * @param data2 The characters to be printed on the second station. Restrictions are the same as for data1. If
     *             this string is the empty string (""), then print the same data as data1. On some printers, using
     *             this format may give additional increased print performance.
     */
    public PrintTwoNormal(JposCommonProperties props, int station, String data1, String data2) {
        super(props);
        Station = station;
        Data1 = data1 == null ? new ArrayList<POSPrinterService.PrintDataPart>() : ((POSPrinterService)props.EventSource).outputDataParts(data1);
        Data2 = data2 == null ? new ArrayList<POSPrinterService.PrintDataPart>() : ((POSPrinterService)props.EventSource).outputDataParts(data2);
    }

    @Override
    public void invoke() throws JposException {
        ((POSPrinterService)Props.EventSource).POSPrinterInterface.printTwoNormal(this);
    }
}
