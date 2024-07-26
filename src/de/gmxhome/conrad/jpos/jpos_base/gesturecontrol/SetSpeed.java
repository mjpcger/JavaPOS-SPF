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
import de.gmxhome.conrad.jpos.jpos_base.JposOutputRequest;
import jpos.JposException;

/**
 * Output request executor for GestureControl method SetSpeed.
 */
public class SetSpeed extends JposOutputRequest {
    /**
     * Speed information in comma separated list.
     */
    public final String SpeedList;

    /**
     * Device control time in seconds.
     */
    public final int Time;

    /**
     * Constructor. Stores given parameters for later use.
     * @param props     Property set of device service.
     * @param speedList Speed information in comma separated list.
     * @param time      Device control time in seconds.
     */
    public SetSpeed(JposCommonProperties props, String speedList, int time) {
        super(props);
        SpeedList = speedList;
        Time = time;
    }

    @Override
    public void invoke() throws JposException {
        ((GestureControlService) Props.EventSource).GestureControl.setSpeed(this);
    }
}
