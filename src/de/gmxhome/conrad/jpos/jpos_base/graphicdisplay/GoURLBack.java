/*
 * Copyright 2023 Martin Conrad
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
 *
 */

package de.gmxhome.conrad.jpos.jpos_base.graphicdisplay;

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import de.gmxhome.conrad.jpos.jpos_base.JposOutputRequest;
import jpos.JposException;

/**
 * Output request executor for GraphicDisplay method GoURLBack.
 */
public class GoURLBack extends JposOutputRequest {
    /**
     * Constructor. Stores given parameters for later use.
     * @param props       Property set of device service.
     */
    public GoURLBack(JposCommonProperties props) {
        super(props);
    }

    @Override
    public void invoke() throws JposException {
        ((GraphicDisplayService)Props.EventSource).GraphicDisplay.goURLBack(this);
    }
}
