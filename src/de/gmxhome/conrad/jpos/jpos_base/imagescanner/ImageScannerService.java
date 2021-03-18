/*
 * Copyright 2021 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.imagescanner;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.JposException;
import jpos.services.ImageScannerService114;

/**
 * ImageScanner service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class ImageScannerService extends JposBase implements ImageScannerService114 {
    /**
     * Instance of a class implementing the ImageScannerInterface for image scanner specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public ImageScannerInterface ImageScanner;

    private ImageScannerProperties Data;
    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public ImageScannerService(ImageScannerProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public boolean getCapAim() throws JposException {
        return false;
    }

    @Override
    public boolean getCapDecodeData() throws JposException {
        return false;
    }

    @Override
    public boolean getCapHostTriggered() throws JposException {
        return false;
    }

    @Override
    public boolean getCapIlluminate() throws JposException {
        return false;
    }

    @Override
    public boolean getCapImageData() throws JposException {
        return false;
    }

    @Override
    public boolean getCapImageQuality() throws JposException {
        return false;
    }

    @Override
    public boolean getCapVideoData() throws JposException {
        return false;
    }

    @Override
    public boolean getAimMode() throws JposException {
        return false;
    }

    @Override
    public void setAimMode(boolean b) throws JposException {

    }

    @Override
    public int getBitsPerPixel() throws JposException {
        return 0;
    }

    @Override
    public byte[] getFrameData() throws JposException {
        return new byte[0];
    }

    @Override
    public int getFrameType() throws JposException {
        return 0;
    }

    @Override
    public boolean getIlluminateMode() throws JposException {
        return false;
    }

    @Override
    public void setIlluminateMode(boolean b) throws JposException {

    }

    @Override
    public int getImageHeight() throws JposException {
        return 0;
    }

    @Override
    public int getImageLength() throws JposException {
        return 0;
    }

    @Override
    public int getImageMode() throws JposException {
        return 0;
    }

    @Override
    public void setImageMode(int i) throws JposException {

    }

    @Override
    public int getImageQuality() throws JposException {
        return 0;
    }

    @Override
    public void setImageQuality(int i) throws JposException {

    }

    @Override
    public int getImageType() throws JposException {
        return 0;
    }

    @Override
    public int getImageWidth() throws JposException {
        return 0;
    }

    @Override
    public int getVideoCount() throws JposException {
        return 0;
    }

    @Override
    public void setVideoCount(int i) throws JposException {

    }

    @Override
    public int getVideoRate() throws JposException {
        return 0;
    }

    @Override
    public void setVideoRate(int i) throws JposException {

    }

    @Override
    public void startSession() throws JposException {

    }

    @Override
    public void stopSession() throws JposException {

    }
}
