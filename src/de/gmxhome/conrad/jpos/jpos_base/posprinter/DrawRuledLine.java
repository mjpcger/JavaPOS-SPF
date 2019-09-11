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
 * Output request executor for POSPrinter method DrawRuledLine.
 */
public class DrawRuledLine extends OutputRequest {
    /**
     * POSPrinter method DrawRuledLine parameter station, see UPOS specification.
     * @return DrawRuledLine parameter <i>station</i>.
     */
    public int getStation() {
        return Station;
    }

    private int Station;

    /**
     * POSPrinter method DrawRuledLine parameter positionList, see UPOS specification.
     * @return DrawRuledLine parameter <i>positionList</i>.
     */
    public String getPositionList() {
        return PositionList;
    }

    private String PositionList;

    /**
     * POSPrinter method DrawRuledLine parameter lineDirection, see UPOS specification.
     * @return DrawRuledLine parameter <i>direction</i>.
     */
    public int getDirection() {
        return Direction;
    }

    private int Direction;

    /**
     * POSPrinter method DrawRuledLine parameter lineWidth, see UPOS specification.
     * @return DrawRuledLine parameter <i>width</i>.
     */
    public int getWidth() {
        return Width;
    }

    private int Width;

    /**
     * POSPrinter method DrawRuledLine parameter lineStyle, see UPOS specification.
     * @return DrawRuledLine parameter <i>style</i>.
     */
    public int getStyle() {
        return Style;
    }

    private int Style;

    /**
     * POSPrinter method DrawRuledLine parameter lineColor, see UPOS specification.
     * @return DrawRuledLine parameter <i>color</i>.
     */
    public int getColor() {
        return Color;
    }

    private int Color;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param station The printer station to be used. May be either S_RECEIPT or S_SLIP.
     * @param positionList Position parameters for the ruled line.
     * @param direction Direction of ruled line. One of RL_HORIZONTAL or RL_VERTICAL.
     * @param width Width of the ruled line. The unit of thickness is one dot.
     * @param style How the printed ruled line appears. One of LS_SINGLE_SOLID_LINE, LS_DOUBLE_SOLID_LINE, LS_BROKEN_LINE or LS_CHAIN_LINE.
     * @param color Color of the ruled line. One of the custom color values, see UPOS specification for ESC|[#]rC.
     */
    public DrawRuledLine(JposCommonProperties props, int station, String positionList, int direction, int width, int style, int color) {
        super(props);
        Station = station;
        PositionList = positionList == null ? "" : positionList;
        Direction = direction;
        Width = width;
        Style = style;
        Color = color;
    }

    @Override
    public void invoke() throws JposException {
        ((POSPrinterService)Props.EventSource).POSPrinterInterface.drawRuledLine(this);
    }
}
