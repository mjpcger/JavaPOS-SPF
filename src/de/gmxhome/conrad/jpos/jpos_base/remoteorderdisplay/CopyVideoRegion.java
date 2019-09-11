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
 * Output request executor for RemoteOrderDisplay method CopyVideoRegion.
 */
public class CopyVideoRegion extends AreaBase {
    /**
     * Retrieves parameter targetRow of remote order display method. See UPOS specification of the specific method for further
     * information.
     * @return  Value of method parameter targetRow.
     */
    public int getTargetRow() {
        return TargetRow;
    }
    private int TargetRow;

    /**
     * Retrieves parameter targetColumn of remote order display method. See UPOS specification of the specific method for further
     * information.
     * @return  Value of method parameter targetColumn.
     */
    public int getTargetColumn() {
        return TargetColumn;
    }
    private int TargetColumn;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param data Property set of device service.
     * @param units        Bitwise mask indicating which video unit(s) to operate on.
     * @param row          Upper row of the specified region.
     * @param column       Left column of the specified region.
     * @param height       Height of the specified region.
     * @param width        Width of the specified region.
     * @param targetRow    Upper row of target location.
     * @param targetColumn Left column of target location.
     */
    public CopyVideoRegion(RemoteOrderDisplayProperties data, int units, int row, int column, int height, int width, int targetRow, int targetColumn) {
        super(data, units, row, column, height, width);
        TargetRow = targetRow;
        TargetColumn = targetColumn;
    }

    @Override
    public void invoke() throws JposException {
        ((RemoteOrderDisplayService)Props.EventSource).RemoteOrderDisplayInterface.copyVideoRegion(this);
    }
}
