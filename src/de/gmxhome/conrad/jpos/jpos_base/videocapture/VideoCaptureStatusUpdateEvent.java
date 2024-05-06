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

package de.gmxhome.conrad.jpos.jpos_base.videocapture;

import de.gmxhome.conrad.jpos.jpos_base.*;

import static jpos.VideoCaptureConst.*;

/**
 * Status update event implementation for VideoCapture devices.
 */
public class VideoCaptureStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Constructor, Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     */
    public VideoCaptureStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        if (ret.length() > 0)
            return ret;
        return switch (getStatus()) {
            case VCAP_SUE_START_PHOTO -> "VideoCapture Start Taking Photo";
            case VCAP_SUE_END_PHOTO -> "VideoCapture End Taking Photo";
            case VCAP_SUE_START_VIDEO -> "VideoCapture Start Vidoe Recording";
            case VCAP_SUE_STOP_VIDEO -> "VideoCapture Stop Video Recording";
            default -> "Unknown VideoCapture Status Change: " + getStatus();
        };
    }
}
