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

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Concurrent output request executor for GraphicDisplay method PlayVideo. Keep in mind: With the default implementation
 * of method invokeConcurrentMethod in JposBaseDevice, concurrent output requests will be executed sequentially, as all
 * other output requests.
 */
public class PlayVideo extends JposOutputRequest {
    /**
     * Constructor. Stores given parameters for later use.
     * @param props       Property set of device service.
     * @param fileName    The file name of the video to be played.
     * @param loop        Specified whether play back shall loop.
     */
    public PlayVideo(GraphicDisplayProperties props, String fileName, boolean loop) {
        super(props);
        FileName = fileName;
        VideoType = props.VideoType;
        Loop = loop;
    }

    /**
     * Returns contents of VideoType property at the time this request has been created.
     * @return Contents of VideoType.
     */
    public String getVideoType() {
        return VideoType;
    }
    private final String VideoType;

    /**
     * Returns contents of fileName parameter
     * @return Contents of FileName.
     */
    public String getFileName() {
        return FileName;
    }
    private final String FileName;

    /**
     * Returns contents of loop parameter
     * @return Contents of Loop.
     */
    public boolean getLoop() {
        return Loop;
    }
    private final boolean Loop;

    @Override
    public void invoke() throws JposException {
        ((GraphicDisplayService)Props.EventSource).GraphicDisplay.playVideo(this);
    }
}
