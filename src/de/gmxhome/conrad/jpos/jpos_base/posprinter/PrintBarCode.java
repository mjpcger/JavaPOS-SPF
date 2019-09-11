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
 * Output request executor for POSPrinter method PrintBarCode.
 */
public class PrintBarCode extends OutputRequest {
    /**
     * POSPrinter method PrintBarCode parameter station, see UPOS specification.
     * @return PrintBarCode parameter <i>station</i>.
     */
    public int getStation() {
        return Station;
    }

    private int Station;

    /**
     * POSPrinter method PrintBarCode parameter data, see UPOS specification.
     * @return PrintBarCode parameter <i>data</i>.
     */
    public String getData() {
        return Data;
    }

    private String Data;

    /**
     * POSPrinter method PrintBarCode parameter symbology, see UPOS specification.
     * @return PrintBarCode parameter <i>symbology</i>.
     */
    public int getSymbology() {
        return Symbology;
    }

    private int Symbology;

    /**
     * POSPrinter method PrintBarCode parameter height, see UPOS specification.
     * @return PrintBarCode parameter <i>height</i>.
     */
    public int getHeight() {
        return Height;
    }

    private int Height;

    /**
     * POSPrinter method PrintBarCode parameter width, see UPOS specification.
     * @return PrintBarCode parameter <i>width</i>.
     */
    public int getWidth() {
        return Width;
    }

    private int Width;

    /**
     * POSPrinter method PrintBarCode parameter alignment, see UPOS specification.
     * @return PrintBarCode parameter <i>alignment</i>.
     */
    public int getAlignment() {
        return Alignment;
    }

    private int Alignment;

    /**
     * POSPrinter method PrintBarCode parameter textPosition, see UPOS specification.
     * @return PrintBarCode parameter <i>textPosition</i>.
     */
    public int getTextPosition() {
        return TextPosition;
    }

    private int TextPosition;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param station The printer station to be used. May be either S_RECEIPT or S_SLIP.
     * @param data Character string to be bar coded.
     * @param symbology Bar code symbol type to use. See UPOS specification for method PrintBarCode for possible values.
     * @param height Bar code height. Expressed in the unit of measure given by MapMode.
     * @param width Bar code width. Expressed in the unit of measure given by MapMode.
     * @param alignment Placement of the bar code. One of BC_LEFT, BC_CENTER, BC_RIGHT or any other positive value as
     *                  distance from left-most position in units of measure given by MapMode.
     * @param textPosition Placement of the readable character string. One of BC_TEXT_NONE, BC_TEXT_ABOVE or BC_TEXT_BELOW.
     */
    public PrintBarCode(JposCommonProperties props, int station, String data, int symbology, int height, int width, int alignment, int textPosition) {
        super(props);
        Station = station;
        Data = data == null ? "" : data;
        Symbology = symbology;
        Height = height;
        Width = width;
        Alignment = alignment;
        TextPosition = textPosition;
    }

    @Override
    public void invoke() throws JposException {
        ((POSPrinterService)Props.EventSource).POSPrinterInterface.printBarCode(this);
    }
}
