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
 * Output request executor for POSPrinter method MarkFeed.
 */
public class MarkFeed extends OutputRequest {
    /**
     * POSPrinter method MarkFeed parameter Type, see UPOS specification.
     * @return MarkFeed parameter <i>type</i>.
     */
    public int getType() {
        return Type;
    }

    private int Type;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param type  The type parameter indicates the type of mark sensed paper handling. Valid values are
     *              MF_TO_TAKEUP, MF_TO_CUTTER, MF_TO_CURRENT_TOF or MF_TO_NEXT_TOF.
     */
    public MarkFeed(JposCommonProperties props, int type) {
        super(props);
        Type = type;
    }

    @Override
    public void invoke() throws JposException {
        ((POSPrinterService)Props.EventSource).POSPrinterInterface.markFeed(this);
    }
}
