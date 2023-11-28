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

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import de.gmxhome.conrad.jpos.jpos_base.JposDevice;
import jpos.ImageScannerConst;
import jpos.JposException;

/**
 * Class containing the image scanner specific properties, their default values and default implementations of
 * ImageScannerInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Image Scanner.
 */
public class ImageScannerProperties extends JposCommonProperties implements ImageScannerInterface {
    /**
     * Copy of AutoDisable property. This is the copy set and returned by AutoDisable getter and setter.
     * However, since data event based standard handling must be replace by non-standard handling in case of
     * video mode, the AutoDisabled property itself will be set to false in video mode by the framework.
     * <br>Since access to AutoDisable will always be made via getter and setter, the application does not
     * recognize this automatism, it sees always StoredAutoDisable instead of AutoDisable.
     */
    public boolean StoredAutoDisable = false;
    /**
     * UPOS property CapAim. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public boolean CapAim = false;

    /**
     * UPOS property CapDecodeData. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public boolean CapDecodeData = false;

    /**
     * UPOS property CapHostTriggered. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public boolean CapHostTriggered = false;

    /**
     * UPOS property CapIlluminate. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public boolean CapIlluminate = false;

    /**
     * UPOS property CapImageData. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public boolean CapImageData = false;

    /**
     * UPOS property CapImageQuality. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public boolean CapImageQuality = false;

    /**
     * UPOS property CapVideoData. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public boolean CapVideoData = false;

    /**
     * UPOS property AimMode. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public boolean AimMode = false;

    /**
     * UPOS property BitsPerPixel. Default: null.
     */
    public Integer BitsPerPixel = null;

    /**
     * UPOS property FrameData. Default: null.
     */
    public byte[] FrameData = null;

    /**
     * UPOS property FrameType. Default: null.
     */
    public Integer FrameType = null;

    /**
     * UPOS property IlluminateMode. Default: null.
     */
    public Boolean IlluminateMode = null;

    /**
     * UPOS property ImageHeight. Default: null.
     */
    public Integer ImageHeight = null;

    /**
     * UPOS property ImageLength. Default: null.
     */
    public Integer ImageLength = null;

    /**
     * UPOS property ImageMode. Default: STILL_ONLY.
     */
    public int ImageMode = ImageScannerConst.IMG_STILL_ONLY;

    /**
     * UPOS property ImageQuality. Default: QUAL_HIGH.
     */
    public int ImageQuality = ImageScannerConst.IMG_QUAL_HIGH;

    /**
     * UPOS property ImageType. Default: null.
     */
    public Integer ImageType = null;

    /**
     * UPOS property ImageWidth. Default: null.
     */
    public Integer ImageWidth = null;

    /**
     * UPOS property VideoCount. Default: 15.
     */
    public int VideoCount = 15;

    /**
     * UPOS property VideoRate. Default: 30.
     */
    public int VideoRate = 30;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected ImageScannerProperties(int dev) {
        super(dev);
        ExclusiveUse = ExclusiveYes;
    }

    @Override
    public void clearDataProperties() {
        super.clearDataProperties();
        if (FrameData != null) {
            FrameData = null;
            EventSource.logSet("FrameData");
        }
        if (BitsPerPixel != null) {
            BitsPerPixel = null;
            EventSource.logSet("BitsPerPixel");
        }
        if (FrameType != null) {
            FrameType = null;
            EventSource.logSet("FrameType");
        }
        if (ImageHeight != null) {
            ImageHeight = null;
            EventSource.logSet("ImageHeight");
        }
        if (ImageLength != null) {
            ImageLength = null;
            EventSource.logSet("ImageLength");
        }
        if (ImageType != null) {
            ImageType = null;
            EventSource.logSet("ImageType");
        }
        if (ImageWidth != null) {
            ImageWidth = null;
            EventSource.logSet("ImageWidth");
        }
    }

    private long VideoModes[] = {ImageScannerConst.IMG_VIDEO_DECODE, ImageScannerConst.IMG_VIDEO_STILL, ImageScannerConst.IMG_ALL};

    @Override
    public void autoDisable(boolean enable) throws JposException {
        AutoDisable = JposDevice.member(ImageMode, VideoModes) ? false : (StoredAutoDisable = enable);
    }

    @Override
    public void aimMode(boolean aimMode) throws JposException {
        AimMode = aimMode;
    }

    @Override
    public void illuminateMode(boolean illuminateMode) throws JposException {
        IlluminateMode = illuminateMode;
    }

    @Override
    public void imageMode(int imageMode) throws JposException {
        AutoDisable = JposDevice.member(ImageMode = imageMode, VideoModes) ? false : StoredAutoDisable;
    }

    @Override
    public void imageQuality(int imageQuality) throws JposException {
        ImageQuality = imageQuality;
    }

    @Override
    public void videoCount(int videoCount) throws JposException {
        VideoCount = videoCount;
    }

    @Override
    public void videoRate(int videoRate) throws JposException {
        VideoRate = videoRate;
    }

    @Override
    public void startSession() throws JposException {
    }

    @Override
    public void stopSession() throws JposException {
    }
}
