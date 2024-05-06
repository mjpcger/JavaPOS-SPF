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

import java.util.Arrays;

/**
 * Output request executor for POSPrinter method PrintMemoryBitmap.
 */
public class PrintMemoryBitmap extends OutputRequest {
    /**
     * POSPrinter method PrintMemoryBitmap parameter station, see UPOS specification.
     * @return PrintMemoryBitmap parameter <i>station</i>.
     */
    public int getStation() {
        return Station;
    }

    private final int Station;

    /**
     * POSPrinter method PrintMemoryBitmap parameter data, see UPOS specification.
     * @return PrintMemoryBitmap parameter <i>data</i>.
     */
    public byte[] getData() {
        return Arrays.copyOf(Data, Data.length);
    }

    private final byte[] Data;

    /**
     * POSPrinter method PrintMemoryBitmap parameter type, see UPOS specification.
     * @return PrintMemoryBitmap parameter <i>type</i>.
     */
    public int getType() {
        return Type;
    }

    private final int Type;

    /**
     * POSPrinter method PrintMemoryBitmap parameter width, see UPOS specification.
     * @return PrintMemoryBitmap parameter <i>width</i>.
     */
    public int getWidth() {
        return Width;
    }

    private final int Width;

    /**
     * POSPrinter method PrintMemoryBitmap parameter alignment, see UPOS specification.
     * @return PrintMemoryBitmap parameter <i>alignment</i>.
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
     * @param data Memory byte array representation of the bitmap.
     * @param type Various bitmap formats may be supported, such as bmp, gif, or jpeg files. One of BMT_BMP, BMT_JPEG or BMT_GIF.
     * @param width Printed width of the bitmap to be performed. Values are BM_ASIS or the bitmap width expressed in the
     *             unit of measure given by MapMode.
     * @param alignment Placement of the bitmap. One of BC_LEFT, BC_CENTER, BC_RIGHT or any other positive value as
     *                  distance from left-most position in units of measure given by MapMode.
     */
    public PrintMemoryBitmap(JposCommonProperties props, int station, byte[] data, int type, int width, int alignment) {
        super(props);
        Station = station;
        Data = data == null ? new byte[0] : Arrays.copyOf(data, data.length);
        Type = type;
        Width = width;
        Alignment = alignment;
    }

    @Override
    public void invoke() throws JposException {
        POSPrinterService svc = (POSPrinterService)Props.EventSource;
        if (EndSync == null) {
            svc.extendedErrorCheck(getStation());
        }
        svc.POSPrinterInterface.printMemoryBitmap(this);
    }
}
