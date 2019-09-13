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

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import jpos.JposConst;
import jpos.JposException;

/**
 * Output request class for remote order display methods using starting row and column parameters.
 */
public class PositionBase extends OutputRequest {
    /**
     * Retrieves parameter row of remote order display method. See UPOS specification of the specific method for further
     * information.
     * @return  Value of method parameter row.
     */
    public int getRow() {
        return Row;
    }
    private int Row;

    /**
     * Retrieves parameter column of remote order display method. See UPOS specification of the specific method for further
     * information.
     * @return  Value of method parameter column.
     */
    public int getColumn() {
        return Column;
    }
    private int Column;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props  Property set of device service.
     * @param units  Units where status has been changed.
     * @param row    (Upper) Row where operation shall start.
     * @param column (Left) Column where operation shall start.
     */
    public PositionBase(JposCommonProperties props, int units, int row, int column) {
        super(props, units);
        Row = row;
        Column = column;
    }

    protected void checkPositionValid() throws JposException {
        RemoteOrderDisplayProperties data = (RemoteOrderDisplayProperties) (Props);
        RemoteOrderDisplayService svc = (RemoteOrderDisplayService) data.EventSource;
        int errorunits = svc.validateCoordinates(getUnits(), getRow(), getColumn());
        svc.check(errorunits != 0, errorunits, JposConst.JPOS_E_ILLEGAL, 0, "Illegal region for units specified by " + errorunits);
    }
}
