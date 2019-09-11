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

package de.gmxhome.conrad.jpos.jpos_base.remoteorderdisplay;

import jpos.*;

/**
 * Output request executor for RemoteOrderDisplay method DisplayData.
 */
public class DisplayData extends PositionBase {
    /**
     * Retrieves parameter attribute of method DisplayData. See UPOS specification for further information.
     * @return  Value of method parameter attribute.
     */
    public int getAttributes() {
        return Attributes;
    }
    private int Attributes;

    /**
     * Retrieves parameter data of method DisplayData. See UPOS specification for further information.
     * @return  Value of method parameter data.
     */
    public String getData() {
        return Data;
    }
    private String Data;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param units  Units where status has been changed.
     * @param row    (Upper) Row where operation shall start.
     * @param column (Left) Column where operation shall start.
     * @param attribute VGA-like attribute parameter, see UPOS specification, chapter Remote Order Display - General
     *                      Information - Model.
     * @param data    Text to be displayed.
     */
    public DisplayData(RemoteOrderDisplayProperties props, int units, int row, int column, int attribute, String data) {
        super(props, units, row, column);
        Attributes = attribute;
        Data = data;
    }

    @Override
    public void invoke() throws JposException {
        ((RemoteOrderDisplayService)Props.EventSource).RemoteOrderDisplayInterface.displayData(this);
    }
}
