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

import de.gmxhome.conrad.jpos.jpos_base.JposBase;
import de.gmxhome.conrad.jpos.jpos_base.JposDataEvent;
import de.gmxhome.conrad.jpos.jpos_base.JposStatusUpdateEvent;

import static jpos.GestureControlConst.GCTL_SUE_START_MOTION;
import static jpos.GestureControlConst.GCTL_SUE_STOP_MOTION;

/**
 * Status update event implementation for GestureControl devices.
 */
@SuppressWarnings("unused")
public class GestureControlStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Constructor, Parameters eventSource and status passed to base class unchanged.Parameter what specifies which
     * method generated the event.
     * @param eventSource Event source.
     * @param status      Status value, see UPOS specification.
     */
    public GestureControlStatusUpdateEvent(JposBase eventSource, int status) {
        super(eventSource, status);
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        if (ret.length() > 0)
            return ret;
        switch (getStatus()) {
        case GCTL_SUE_START_MOTION:
            return "GestureControl Start Motion";
        case GCTL_SUE_STOP_MOTION:
            return "GestureControl Stop Motion";
        }
        return "Unknown SpeechSynthesis Status Change: " + getStatus();
    }
}
