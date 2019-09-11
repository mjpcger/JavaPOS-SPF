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

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Output request executor for RemoteOrderDisplay method ClearVideoRegion and base for method CopyVideoRegion.
 */
public class ClearVideoRegion extends AreaBase {
    /**
     * Retrieves parameter attribute of method ClearVideoRegion. see UPOS specification, chapter Remote
     * Order Display - General Information - Model: Blinking flag, intensity flag, background and foreground color flags.
     * @return  Value of method parameter attribute.
     */
    public int getAttributes() {
        return Attributes;
    }
    private int Attributes;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param units     Bitwise mask indicating which video unit(s) to operate on.
     * @param row       Upper row of the specified region.
     * @param column    Left column of the specified region.
     * @param height    Height of the specified region.
     * @param width     Width of the specified region.
     * @param attribute VGA-like attribute parameter, see UPOS specification, chapter Remote Order Display - General
     *                      Information - Model.
     */
    public ClearVideoRegion(JposCommonProperties props, int units, int row, int column, int height, int width, int attribute) {
        super(props, units,row, column, height, width);
        Attributes = attribute;
    }

    @Override
    public void invoke() throws JposException {
        ((RemoteOrderDisplayService)Props.EventSource).RemoteOrderDisplayInterface.clearVideoRegion(this);
    }
}
