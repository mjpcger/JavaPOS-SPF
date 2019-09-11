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

/**
 * Output request class for remote order display methods using row, column, height and width parameters to specify a
 * video region.
 */
public class AreaBase extends PositionBase {
    /**
     * Retrieves parameter height of remote order display method. See UPOS specification of the specific method for further
     * information.
     * @return  Value of method parameter height.
     */
    public int getHeight() {
        return Height;
    }
    private int Height;

    /**
     * Retrieves parameter width of remote order display method. See UPOS specification of the specific method for further
     * information.
     * @return  Value of method parameter width.
     */
    public int getWidth() {
        return Width;
    }
    private int Width;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props  Property set of device service.
     * @param units  Units where status has been changed.
     * @param row    Upper row of the specified region.
     * @param column Left column of the specified region.
     * @param height Height of the specified region.
     * @param width  Width of the specified region.
     */
    public AreaBase(JposCommonProperties props, int units, int row, int column, int height, int width) {
        super(props, units, row, column);
        Height = height;
        Width = width;
    }
}
