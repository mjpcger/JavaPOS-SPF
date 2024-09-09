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

import java.util.List;

import static jpos.GestureControlConst.GCTL_SUE_START_MOTION;
import static jpos.GestureControlConst.GCTL_SUE_STOP_MOTION;
import static de.gmxhome.conrad.jpos.jpos_base.gesturecontrol.GestureControlProperties.*;

/**
 * Output request executor for GestureControl method SetPosition.
 */
public class SetPosition extends JposOutputRequest {
    /**
     * Position information in comma separated list.
     */
    public final List<JointParameter> PositionList;

    /**
     * Device control completion time in seconds.
     */
    public final int Time;

    /**
     * Position information contain absolute values if true, otherwise relative values.
     */
    public final boolean Absolute;

    /**
     * Constructor. Stores given parameters for later use.
     * @param props        Property set of device service.
     * @param positionList Position information in comma separated list.
     * @param time         Device control completion time in seconds.
     * @param absolute     Position information contain absolute values if true, otherwise relative values.
     */
    public SetPosition(JposCommonProperties props, List<JointParameter> positionList, int time, boolean absolute) {
        super(props);
        PositionList = positionList;
        Time = time;
        Absolute = absolute;
    }

    @Override
    public void invoke() throws JposException {
        Device.handleEvent(new GestureControlStatusUpdateEvent(Props.EventSource, GCTL_SUE_START_MOTION));
        try {
            ((GestureControlService) Props.EventSource).GestureControl.setPosition(this);
        } finally {
            Device.handleEvent(new GestureControlStatusUpdateEvent(Props.EventSource, GCTL_SUE_STOP_MOTION));
        }
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
