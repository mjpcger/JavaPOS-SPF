/*
 * Copyright 2024 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.gesturecontrol;

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import de.gmxhome.conrad.jpos.jpos_base.JposOutputCompleteEvent;
import de.gmxhome.conrad.jpos.jpos_base.JposOutputRequest;
import jpos.JposException;

/**
 * Output request executor for GestureControl method StartMotion.
 */
public class StartMotion extends JposOutputRequest {
    /**
     * Name of the motion file or motion ID to start.
     */
    public final String FileName;

    /**
     * Constructor. Stores given parameters for later use.
     * @param props     Property set of device service.
     * @param fileName  Name of the motion file or motion ID to start.
     */
    public StartMotion(JposCommonProperties props, String fileName) {
        super(props);
        FileName = fileName;
    }

    @Override
    public void invoke() throws JposException {
        ((GestureControlService) Props.EventSource).GestureControl.startMotion(this);
    }

    @Override
    public JposOutputCompleteEvent createOutputEvent() {
        for(Integer id : ((GestureControlService) Props.EventSource).OutputIDs) {
            if (id == OutputID) {
                ((GestureControlService) Props.EventSource).OutputIDs.remove(id);
                break;
            }
        }
        return super.createOutputEvent();
    }
}
