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
 * Output request executor for RemoteOrderDisplay method SaveVideoRegion.
 */
public class SaveVideoRegion extends AreaBase {
    /**
     * Retrieves parameter bufferId of method ClearVideoRegion. See UPOS specification for further information.
     * @return  Value of method parameter bufferId.
     */
    public int getBufferId() {
        return BufferId;
    }
    private final int BufferId;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param units    Bitwise mask indicating which video unit(s) to operate on.
     * @param row      Upper row of the specified region.
     * @param column   Left column of the specified region.
     * @param height   Height of the specified region.
     * @param width    Width of the specified region.
     * @param bufferId Number identifying the video buffer to use.
     */
    public SaveVideoRegion(JposCommonProperties props, int units, int row, int column, int height, int width, int bufferId) {
        super(props, units, row, column, height, width);
        BufferId = bufferId;
    }

    @Override
    public void invoke() throws JposException {
        RemoteOrderDisplayService svc = (RemoteOrderDisplayService) Props.EventSource;
        if (EndSync == null) {
            checkUnitsOnline();
            checkAreaValid();
            int errorunits = svc.validateBufferID(getUnits(), getBufferId());
            svc.check(errorunits != 0, errorunits, JPOS_E_ILLEGAL, 0, "Illegal buffer ID " + getBufferId() + " for units specified by " + errorunits, EndSync != null);
        }
        svc.RemoteOrderDisplayInterface.saveVideoRegion(this);
    }
}
