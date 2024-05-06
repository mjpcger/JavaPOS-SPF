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

/**
 * Output request executor for POSPrinter method PrintBitmap.
 */
public class PrintBitmap extends OutputRequest {
    /**
     * POSPrinter method PrintBitmap parameter station, see UPOS specification.
     * @return PrintBitmap parameter <i>station</i>.
     */
    public int getStation() {
        return Station;
    }

    private final int Station;

    /**
     * POSPrinter method PrintBitmap parameter fileName, see UPOS specification.
     * @return PrintBitmap parameter <i>fileName</i>.
     */
    public String getFileName() {
        return FileName;
    }

    private final String FileName;

    /**
     * POSPrinter method PrintBitmap parameter width, see UPOS specification.
     * @return PrintBitmap parameter <i>width</i>.
     */
    public int getWidth() {
        return Width;
    }

    private final int Width;

    /**
     * POSPrinter method PrintBitmap parameter alignment, see UPOS specification.
     * @return PrintBitmap parameter <i>alignment</i>.
     */
    public int getAlignment() {
        return Alignment;
    }

    private final int Alignment;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param station The printer station to be used. May be either S_RECEIPT or S_SLIP.
     * @param fileName File name or URL of bitmap file. Various file formats may be supported, such as bmp, gif, or jpeg files.
     * @param width Printed width of the bitmap to be performed. Values are BM_ASIS or the bitmap width expressed in the
     *             unit of measure given by MapMode.
     * @param alignment Placement of the bitmap. One of BC_LEFT, BC_CENTER, BC_RIGHT or any other positive value as
     *                  distance from left-most position in units of measure given by MapMode.
     */
    public PrintBitmap(JposCommonProperties props, int station, String fileName, int width, int alignment) {
        super(props);
        Station = station;
        FileName = fileName == null ? "" : fileName;
        Width = width;
        Alignment = alignment;
    }

    @Override
    public void invoke() throws JposException {
        POSPrinterService svc = (POSPrinterService)Props.EventSource;
        if (EndSync == null) {
            svc.extendedErrorCheck(getStation());
        }
        svc.POSPrinterInterface.printBitmap(this);
    }
}
