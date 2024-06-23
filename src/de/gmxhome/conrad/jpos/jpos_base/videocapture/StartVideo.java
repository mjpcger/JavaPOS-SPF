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
import jpos.JposException;

/**
 * Input request executor for VideoCapture method StartVideo.
 */
public class StartVideo extends JposInputRequest {
    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param fileName The name of the video file to be recorded.
     * @param overWrite Specifies whether the file shall be overwritten in case it just exists.
     * @param recordingTime Maximum recording time in seconds.
     */
    public StartVideo(JposCommonProperties props, String fileName, boolean overWrite, int recordingTime) {
        super(props);
        FileName = fileName;
        OwerWrite = overWrite;
        RecordingTime = recordingTime;
    }

    /**
     * Video file name of video to be recorded.
     */
    public final String FileName;

    /**
     * Specifies the behavior when the specified file just exists.
     */
    public final boolean OwerWrite;

    /**
     * Time for recording in seconds or FOREVER.
     */
    public final int RecordingTime;

    @Override
    public void invoke() throws JposException {
        ((VideoCaptureService)Props.EventSource).VideoCapture.startVideo(this);
    }
}
