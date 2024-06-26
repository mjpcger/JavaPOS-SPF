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
public class TakePhoto extends JposInputRequest {
    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props     Property set of device service.
     * @param fileName      Indicates the video target located on host, HardTotals device or both, depending on Storage property.
     * @param overWrite     Specifies whether the image file shall be overwritten if just present. If false, TakePhoto
     *                      will fail if the specified file just exists.
     * @param timeout       Specifies the recording timeout in milliseconds.
     */
    public TakePhoto(JposCommonProperties props, String fileName, boolean overWrite, int timeout) {
        super(props);
        FileName = fileName;
        OwerWrite = overWrite;
        Timeout = timeout;
    }

    /**
     * Image file name of image to be recorded.
     */
    public final String FileName;

    /**
     * Specifies the behavior when the specified file just exists.
     */
    public final boolean OwerWrite;

    /**
     * Timeout for recording the photo or FOREVER.
     */
    public final int Timeout;

    @Override
    public void invoke() throws JposException {
        ((VideoCaptureService)Props.EventSource).VideoCapture.takePhoto(this);
    }
}
