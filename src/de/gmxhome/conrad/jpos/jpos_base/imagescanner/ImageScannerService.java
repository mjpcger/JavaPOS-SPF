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
import jpos.*;
import jpos.services.*;

import java.util.Arrays;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.ImageScannerConst.*;
import static jpos.JposConst.*;
import static net.bplaced.conrad.log4jpos.Level.*;

/**
 * ImageScanner service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class ImageScannerService extends JposBase implements ImageScannerService116 {
    /**
     * Instance of a class implementing the ImageScannerInterface for image scanner specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public ImageScannerInterface ImageScanner;

    private final ImageScannerProperties Data;
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
    public boolean getAutoDisable() throws JposException {
        checkOpened();
        Device.log(DEBUG, Props.LogicalName + ": AutoDisable: " + Data.StoredAutoDisable);
        return Data.StoredAutoDisable;
    }

    @Override
    public void setAutoDisable(boolean b) throws JposException {
        logPreSet("AutoDisable");
        checkOpened();
        ImageScanner.autoDisable(b);
        Device.log(INFO, Props.LogicalName + ": AutoDisable <- " + Data.StoredAutoDisable);
    }

    @Override
    public boolean getCapAim() throws JposException {
        checkOpened();
        logGet("CapAim");
        return Data.CapAim;
    }

    @Override
    public boolean getCapDecodeData() throws JposException {
        checkOpened();
        logGet("CapDecodeData");
        return Data.CapDecodeData;
    }

    @Override
    public boolean getCapHostTriggered() throws JposException {
        checkOpened();
        logGet("CapHostTriggered");
        return Data.CapHostTriggered;
    }

    @Override
    public boolean getCapIlluminate() throws JposException {
        checkOpened();
        logGet("CapIlluminate");
        return Data.CapIlluminate;
    }

    @Override
    public boolean getCapImageData() throws JposException {
        checkOpened();
        logGet("CapImageData");
        return Data.CapImageData;
    }

    @Override
    public boolean getCapImageQuality() throws JposException {
        checkOpened();
        logGet("CapImageQuality");
        return Data.CapImageQuality;
    }

    @Override
    public boolean getCapVideoData() throws JposException {
        checkOpened();
        logGet("CapVideoData");
        return Data.CapVideoData;
    }

    @Override
    public boolean getAimMode() throws JposException {
        checkOpened();
        logGet("AimMode");
        return Data.AimMode;
    }

    @Override
    public int getBitsPerPixel() throws JposException {
        checkOpened();
        logGet("BitsPerPixel");
        check(Data.BitsPerPixel == null, JPOS_E_ILLEGAL, "BitsPerPixel not available");
        return Data.BitsPerPixel;
    }

    @Override
    public byte[] getFrameData() throws JposException {
        checkOpened();
        logGet("FrameData");
        check(Data.FrameData == null, JPOS_E_ILLEGAL, "FrameData not available");
        return Arrays.copyOf(Data.FrameData, Data.FrameData.length);
    }

    @Override
    public int getFrameType() throws JposException {
        checkOpened();
        logGet("FrameType");
        check(Data.FrameType == null, JPOS_E_ILLEGAL, "FrameType not available");
        return Data.FrameType;
    }

    @Override
    public boolean getIlluminateMode() throws JposException {
        checkOpened();
        logGet("IlluminateMode");
        check(Data.IlluminateMode == null, JPOS_E_ILLEGAL, "IlluminateMode not available");
        return Data.IlluminateMode;
    }

    @Override
    public int getImageHeight() throws JposException {
        checkOpened();
        logGet("ImageHeight");
        check(Data.ImageHeight == null, JPOS_E_ILLEGAL, "ImageHeight not available");
        return Data.ImageHeight;
    }

    @Override
    public int getImageLength() throws JposException {
        checkOpened();
        logGet("ImageLength");
        check(Data.ImageLength == null, JPOS_E_ILLEGAL, "ImageLength not available");
        return Data.ImageLength;
    }

    @Override
    public int getImageMode() throws JposException {
        checkOpened();
        logGet("ImageMode");
        return Data.ImageMode;
    }

    @Override
    public int getImageQuality() throws JposException {
        checkOpened();
        logGet("ImageQuality");
        return Data.ImageQuality;
    }

    @Override
    public int getImageType() throws JposException {
        checkOpened();
        logGet("ImageType");
        return Data.ImageType;
    }

    @Override
    public int getImageWidth() throws JposException {
        checkOpened();
        logGet("ImageWidth");
        check(Data.ImageWidth == null, JPOS_E_ILLEGAL, "ImageWidth not available");
        return Data.ImageWidth;
    }

    @Override
    public int getVideoCount() throws JposException {
        checkOpened();
        logGet("VideoCount");
        return Data.VideoCount;
    }

    @Override
    public int getVideoRate() throws JposException {
        checkOpened();
        logGet("VideoRate");
        return Data.VideoRate;
    }

    @Override
    public void setAimMode(boolean aimMode) throws JposException {
        logPreSet("AimMode");
        checkOpened();
        check(!Data.CapAim && aimMode != Data.AimMode, JPOS_E_ILLEGAL, "AimMode must not be changed");
        checkNoChangedOrClaimed(Data.AimMode, aimMode);
        ImageScanner.aimMode(aimMode);
        logSet("AimMode");
    }

    @Override
    public void setIlluminateMode(boolean illuminateMode) throws JposException {
        logPreSet("IlluminateMode");
        checkOpened();
        check(!Data.CapIlluminate && illuminateMode != Data.IlluminateMode, JPOS_E_ILLEGAL, "IlluminateMode must not be changed");
        checkNoChangedOrClaimed(Data.IlluminateMode, illuminateMode);
        ImageScanner.illuminateMode(illuminateMode);
        logSet("IlluminateMode");
    }

    @Override
    public void setImageMode(int imageMode) throws JposException {
        logPreSet("ImageMode");
        checkOpened();
        if (imageMode == IMG_DECODE_ONLY)
            check(!Data.CapDecodeData, JPOS_E_ILLEGAL, "Unsupported ImageMode: " + imageMode);
        else if (imageMode == IMG_STILL_ONLY)
            check(!Data.CapImageData, JPOS_E_ILLEGAL, "Unsupported ImageMode: " + imageMode);
        else if (imageMode == IMG_STILL_DECODE)
            check(!Data.CapDecodeData || !Data.CapImageData, JPOS_E_ILLEGAL, "Unsupported ImageMode: " + imageMode);
        else if (imageMode == IMG_VIDEO_DECODE)
            check(!Data.CapDecodeData || !Data.CapVideoData, JPOS_E_ILLEGAL, "Unsupported ImageMode: " + imageMode);
        else if (imageMode == IMG_VIDEO_STILL)
            check(!Data.CapImageData || !Data.CapVideoData, JPOS_E_ILLEGAL, "Unsupported ImageMode: " + imageMode);
        else if (imageMode == IMG_ALL)
            check(!Data.CapDecodeData || !Data.CapImageData || !Data.CapVideoData, JPOS_E_ILLEGAL, "Unsupported ImageMode: " + imageMode);
        else
            throw new JposException(JPOS_E_ILLEGAL, "Invalid ImageMode: " + imageMode);
        checkNoChangedOrClaimed(Data.ImageMode, imageMode);
        ImageScanner.imageMode(imageMode);
        logSet("ImageMode");
    }

    @Override
    public void setImageQuality(int imageQuality) throws JposException {
        long[] valid = { IMG_QUAL_LOW, IMG_QUAL_MED, IMG_QUAL_HIGH };
        logPreSet("ImageQuality");
        checkOpened();
        checkMember(imageQuality, valid, JPOS_E_ILLEGAL, "Invalid ImageQuality: " + imageQuality);
        check(!Data.CapImageQuality && Data.ImageQuality != imageQuality, JPOS_E_ILLEGAL, "Illegal ImageQuality: " + imageQuality);
        checkNoChangedOrClaimed(Data.ImageQuality, imageQuality);
        ImageScanner.imageQuality(imageQuality);
        logSet("ImageQuality");
    }

    @Override
    public void setVideoCount(int videoCount) throws JposException {
        logPreSet("VideoCount");
        checkOpened();
        check(videoCount < 0, JPOS_E_ILLEGAL, "Invalid VideoCount: " + videoCount);
        checkNoChangedOrClaimed(Data.VideoCount, videoCount);
        ImageScanner.videoCount(videoCount);
        logSet("VideoCount");
    }

    @Override
    public void setVideoRate(int videoRate) throws JposException {
        logPreSet("VideoRate");
        checkOpened();
        check(videoRate < 0, JPOS_E_ILLEGAL, "Invalid VideoRate: " + videoRate);
        checkNoChangedOrClaimed(Data.VideoRate, videoRate);
        ImageScanner.videoRate(videoRate);
        logSet("VideoRate");
    }

    @Override
    public void startSession() throws JposException {
        logPreCall("StartSession");
        checkEnabled();
        check(!Data.CapHostTriggered, JPOS_E_ILLEGAL, "Not host triggered");
        ImageScanner.startSession();
        logCall("StartSession");
    }

    @Override
    public void stopSession() throws JposException {
        logPreCall("StopSession");
        checkEnabled();
        check(!Data.CapHostTriggered, JPOS_E_ILLEGAL, "Not host triggered");
        ImageScanner.stopSession();
        logCall("StopSession");
    }
}
