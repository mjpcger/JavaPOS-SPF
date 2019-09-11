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
 * Output request executor for RemoteOrderDisplay method DrawBox.
 */
public class DrawBox extends ClearVideoRegion {
    /**
     * Retrieves parameter bordertype of method DrawBox. See UPOS specification for further information.
     * @return  Value of method parameter bordertype.
     */
    public int getBordertype() {
        return Bordertype;
    }
    private int Bordertype;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param data Property set of device service.
     * @param units     Bitwise mask indicating which video unit(s) to operate on.
     * @param row       Upper row of the specified region.
     * @param column    Left column of the specified region.
     * @param height    Height of the specified region.
     * @param width     Width of the specified region.
     * @param attribute VGA-like attribute parameter, see UPOS specification, chapter Remote Order Display - General
     *                      Information - Model.
     * @param bordertype The border type to be drawn.
     */
    public DrawBox(RemoteOrderDisplayProperties data, int units, int row, int column, int height, int width, int attribute, int bordertype) {
        super(data, units, row, column, height, width, attribute);
        Bordertype = bordertype;
    }

    @Override
    public void invoke() throws JposException {
        ((RemoteOrderDisplayService)Props.EventSource).RemoteOrderDisplayInterface.drawBox(this);
    }
}
