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

import static jpos.JposConst.*;

/**
 * Output request executor for RemoteOrderDisplay method RestoreVideoRegion.
 */
public class RestoreVideoRegion extends UnitOutputRequest {
    /**
     * Retrieves parameter targetRow of method RestoreVideoRegion. See UPOS specification for further information.
     * @return  Value of method parameter targetRow.
     */
    public int getTargetRow() {
        return TargetRow;
    }
    private final int TargetRow;

    /**
     * Retrieves parameter targetColumn of method RestoreVideoRegion. See UPOS specification for further information.
     * @return  Value of method parameter targetColumn.
     */
    public int getTargetColumn() {
        return TargetColumn;
    }
    private final int TargetColumn;

    /**
     * Retrieves parameter bufferId of method RestoreVideoRegion. See UPOS specification for further information.
     * @return  Value of method parameter bufferId.
     */
    public int getBufferId() {
        return BufferId;
    }
    private final int BufferId;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param data Property set of device service.
     * @param units        Bitwise mask indicating which video unit(s) to operate on.
     * @param targetRow    Upper row of target location.
     * @param targetColumn Left column of target location.
     * @param bufferId     Number identifying the source video buffer to use.
     */
    public RestoreVideoRegion(RemoteOrderDisplayProperties data, int units, int targetRow, int targetColumn, int bufferId) {
        super(data, units);
        TargetRow = targetRow;
        TargetColumn = targetColumn;
        BufferId = bufferId;
    }

    @Override
    public void invoke() throws JposException {
        RemoteOrderDisplayService svc = (RemoteOrderDisplayService) Props.EventSource;
        if (EndSync == null) {
            checkUnitsOnline();
            int errorunits = svc.validateCoordinates(getUnits(), getTargetRow(), getTargetColumn());
            svc.check(errorunits != 0, errorunits, JPOS_E_ILLEGAL, 0, "Illegal region for units specified by " + errorunits, EndSync != null);
            errorunits = svc.validateBufferID(getUnits(), getBufferId());
            svc.check(errorunits != 0, errorunits, JPOS_E_ILLEGAL, 0, "Illegal buffer ID " + getBufferId() + " for units specified by " + errorunits, EndSync != null);
        }
        svc.RemoteOrderDisplayInterface.restoreVideoRegion(this);
    }
}
