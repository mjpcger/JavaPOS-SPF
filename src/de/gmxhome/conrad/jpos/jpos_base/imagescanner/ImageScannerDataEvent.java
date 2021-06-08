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

import de.gmxhome.conrad.jpos.jpos_base.JposBase;
import de.gmxhome.conrad.jpos.jpos_base.JposDataEvent;

import java.util.Arrays;

/**
 * Data event implementation for ImageScanner devices.
 */
public class ImageScannerDataEvent extends JposDataEvent {
    /**
     * New contents of FrameData property. For details, see UPOS specification.
     */
    public byte[] FrameData;

    /**
     * New contents of BitsPerPixel property. For details, see UPOS specification.
     */
    public int BitsPerPixel;

    /**
     * New contents of FrameType property. For details, see UPOS specification.
     */
    public int FrameType;

    /**
     * New contents of ImageHeight property. For details, see UPOS specification.
     */
    public int ImageHeight;

    /**
     * New contents of ImageWidth property. For details, see UPOS specification.
     */
    public int ImageWidth;

    /**
     * New contents of ImageLength property. For details, see UPOS specification.
     */
    public int ImageLength;

    /**
     * New contents of ImageType property. For details, see UPOS specification.
     */
    public int ImageType;

    /**
     * Constructor. Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     * @param bitsPerPixel Holds the value to be stored in BitsPerPixel property.
     * @param frameData    Holds the value to be stored in FrameData property.
     * @param frameType    Holds the value to be stored in FrameType property.
     * @param imageHeight  Holds the value to be stored in ImageHeight property.
     * @param imageWidth   Holds the value to be stored in ImageWidth property.
     * @param imageLength  Holds the value to be stored in ImageLength property.
     * @param imageType    Holds the value to be stored in ImageType property.
     */
    public ImageScannerDataEvent(JposBase source, int state, byte[] frameData, int bitsPerPixel, int frameType, int imageHeight, int imageWidth, int imageLength, int imageType) {
        super(source, state);
        FrameData = Arrays.copyOf(frameData, frameData.length);
        BitsPerPixel = bitsPerPixel;
        FrameType = frameType;
        ImageHeight = imageHeight;
        ImageWidth = imageWidth;
        ImageLength =imageLength;
        ImageType = imageType;
    }

    @Override
    public void setDataProperties() {
        super.setDataProperties();
        ImageScannerProperties props = (ImageScannerProperties) getPropertySet();
        if (!Arrays.equals(FrameData, props.FrameData)) {
            props.FrameData = Arrays.copyOf(FrameData, FrameData.length);
            props.EventSource.logSet("FrameData");
        }
        if (props.BitsPerPixel == null || props.BitsPerPixel != BitsPerPixel) {
            props.BitsPerPixel = BitsPerPixel;
            props.EventSource.logSet("BitsPerPixel");
        }
        if (props.FrameType == null || props.FrameType != FrameType) {
            props.FrameType = FrameType;
            props.EventSource.logSet("FrameType");
        }
        if (props.ImageHeight == null || props.ImageHeight != ImageHeight) {
            props.ImageHeight = ImageHeight;
            props.EventSource.logSet("ImageHeight");
        }
        if (props.ImageWidth == null || props.ImageWidth != ImageWidth) {
            props.ImageWidth = ImageWidth;
            props.EventSource.logSet("ImageWidth");
        }
        if (props.ImageLength == null || props.ImageLength != ImageLength) {
            props.ImageLength = ImageLength;
            props.EventSource.logSet("ImageLength");
        }
        if (props.ImageType == null || props.ImageType != ImageType) {
            props.ImageType = ImageType;
            props.EventSource.logSet("ImageType");
        }
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        ret += ", BitsPerPixel: " + BitsPerPixel;
        ret += ", ImageData: [" + FrameData.length + " byte]";
        ret += ", FrameType: " + FrameType;
        ret += ", ImageHeight: " + ImageHeight;
        ret += ", ImageWidth: " + ImageWidth;
        ret += ", ImageLength: " + ImageLength;
        ret += ", ImageType: " + ImageType;
        return ret;
    }
}
