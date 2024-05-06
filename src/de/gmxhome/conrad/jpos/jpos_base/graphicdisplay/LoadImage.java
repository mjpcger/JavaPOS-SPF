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
 * Output request executor for GraphicDisplay method LoadImage.
 */
public class LoadImage extends JposOutputRequest {
    /**
     * Constructor. Stores given parameters for later use.
     * @param props     Property set of device service.
     * @param fileName  The file name of the image to be loaded.
     */
    public LoadImage(GraphicDisplayProperties props, String fileName) {
        super(props);
        FileName = fileName;
        ImageType = props.ImageType;
    }

    /**
     * Returns contents of ImageType property at the time this request has been created.
     * @return Contents of ImageType.
     */
    public String getImageType() {
        return ImageType;
    }
    private final String ImageType;

    /**
     * Returns contents of fileName parameter
     * @return Contents of FileName.
     */
    public String getFileName() {
        return FileName;
    }
    private final String FileName;

    @Override
    public void invoke() throws JposException {
        ((GraphicDisplayService)Props.EventSource).GraphicDisplay.loadImage(this);
    }
}
