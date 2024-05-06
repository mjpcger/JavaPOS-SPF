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
import jpos.*;
import jpos.services.*;

import java.util.Objects;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;
import static jpos.VideoCaptureConst.*;

/**
 * VideoCapture service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class VideoCaptureService extends JposBase implements VideoCaptureService116 {
    private final VideoCaptureProperties Data;
    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public VideoCaptureService(VideoCaptureProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    /**
     * Instance of a class implementing the VideoCaptureInterface for video capture specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public VideoCaptureInterface VideoCapture;

    @Override
    public boolean getAutoExposure() throws JposException {
        logGet("AutoExposure");
        checkEnabled();
        return Data.AutoExposure;
    }

    @Override
    public void setAutoExposure(boolean flag) throws JposException {
        logPreSet("AutoExposure");
        checkEnabled();
        check(flag != Data.AutoExposure && !Data.CapAutoExposure, JPOS_E_ILLEGAL, "Changing AutoExposure not supported");
        VideoCapture.autoExposure(flag);
        logSet("AutoExposure");
    }

    @Override
    public boolean getAutoFocus() throws JposException {
        logGet("AutoFocus");
        checkEnabled();
        return Data.AutoFocus;
    }

    @Override
    public void setAutoFocus(boolean flag) throws JposException {
        logPreSet("AutoFocus");
        checkEnabled();
        check(flag != Data.AutoFocus && !Data.CapAutoFocus, JPOS_E_ILLEGAL, "Changing AutoFocus not supported");
        VideoCapture.autoFocus(flag);
        logSet("AutoFocus");
    }

    @Override
    public boolean getAutoGain() throws JposException {
        logGet("AutoGain");
        checkEnabled();
        return Data.AutoGain;
    }

    @Override
    public void setAutoGain(boolean flag) throws JposException {
        logPreSet("AutoGain");
        checkEnabled();
        check(flag != Data.AutoGain && !Data.CapAutoGain, JPOS_E_ILLEGAL, "Changing AutoGain not supported");
        VideoCapture.autoGain(flag);
        logSet("AutoGain");
    }

    @Override
    public boolean getAutoWhiteBalance() throws JposException {
        logGet("AutoWhiteBalance");
        checkEnabled();
        return Data.AutoWhiteBalance;
    }

    @Override
    public void setAutoWhiteBalance(boolean flag) throws JposException {
        logPreSet("AutoWhiteBalance");
        checkEnabled();
        check(flag != Data.AutoWhiteBalance && !Data.CapAutoWhiteBalance, JPOS_E_ILLEGAL, "Changing AutoWhiteBalance not supported");
        VideoCapture.autoWhiteBalance(flag);
        logSet("AutoWhiteBalance");
    }

    @Override
    public int getBrightness() throws JposException {
        logGet("Brightness");
        checkEnabled();
        return Data.Brightness;
    }

    @Override
    public void setBrightness(int brightness) throws JposException {
        logPreSet("Brightness");
        checkEnabled();
        check(brightness != Data.Brightness && !Data.CapBrightness, JPOS_E_ILLEGAL, "Changing Brightness not supported");
        check(brightness < 0 || brightness > 100, JPOS_E_ILLEGAL, "Invalid brighness: " + brightness);
        VideoCapture.brightness(brightness);
        logSet("Brightness");
    }

    @Override
    public String getCapAssociatedHardTotalsDevice() throws JposException {
        logGet("CapAssociatedHardTotalsDevice");
        checkOpened();
        return Data.CapAssociatedHardTotalsDevice;
    }

    @Override
    public boolean getCapAutoExposure() throws JposException {
        logGet("CapAutoExposure");
        checkOpened();
        return Data.CapAutoExposure;
    }

    @Override
    public boolean getCapAutoFocus() throws JposException {
        logGet("CapAutoFocus");
        checkOpened();
        return Data.CapAutoFocus;
    }

    @Override
    public boolean getCapAutoGain() throws JposException {
        logGet("CapAutoGain");
        checkOpened();
        return Data.CapAutoGain;
    }

    @Override
    public boolean getCapAutoWhiteBalance() throws JposException {
        logGet("CapAutoWhiteBalance");
        checkOpened();
        return Data.CapAutoWhiteBalance;
    }

    @Override
    public boolean getCapBrightness() throws JposException {
        logGet("CapBrightness");
        checkOpened();
        return Data.CapBrightness;
    }

    @Override
    public boolean getCapContrast() throws JposException {
        logGet("CapContrast");
        checkOpened();
        return Data.CapContrast;
    }

    @Override
    public boolean getCapExposure() throws JposException {
        logGet("CapExposure");
        checkOpened();
        return Data.CapExposure;
    }

    @Override
    public boolean getCapGain() throws JposException {
        logGet("CapGain");
        checkOpened();
        return Data.CapGain;
    }

    @Override
    public boolean getCapHorizontalFlip() throws JposException {
        logGet("CapHorizontalFlip");
        checkOpened();
        return Data.CapHorizontalFlip;
    }

    @Override
    public boolean getCapHue() throws JposException {
        logGet("CapHue");
        checkOpened();
        return Data.CapHue;
    }

    @Override
    public boolean getCapPhoto() throws JposException {
        logGet("CapPhoto");
        checkOpened();
        return Data.CapPhoto;
    }

    @Override
    public boolean getCapPhotoColorSpace() throws JposException {
        logGet("CapPhotoColorSpace");
        checkOpened();
        return Data.CapPhotoColorSpace;
    }

    @Override
    public boolean getCapPhotoFrameRate() throws JposException {
        logGet("CapPhotoFrameRate");
        checkOpened();
        return Data.CapPhotoFrameRate;
    }

    @Override
    public boolean getCapPhotoResolution() throws JposException {
        logGet("CapPhotoResolution");
        checkOpened();
        return Data.CapPhotoResolution;
    }

    @Override
    public boolean getCapPhotoType() throws JposException {
        logGet("CapPhotoType");
        checkOpened();
        return Data.CapPhotoType;
    }

    @Override
    public boolean getCapSaturation() throws JposException {
        logGet("CapSaturation");
        checkOpened();
        return Data.CapSaturation;
    }

    @Override
    public int getCapStorage() throws JposException {
        logGet("CapStorage");
        checkOpened();
        return Data.CapStorage;
    }

    @Override
    public boolean getCapVerticalFlip() throws JposException {
        logGet("CapVerticalFlip");
        checkOpened();
        return Data.CapVerticalFlip;
    }

    @Override
    public boolean getCapVideo() throws JposException {
        logGet("CapVideo");
        checkOpened();
        return Data.CapVideo;
    }

    @Override
    public boolean getCapVideoColorSpace() throws JposException {
        logGet("CapVideoColorSpace");
        checkOpened();
        return Data.CapVideoColorSpace;
    }

    @Override
    public boolean getCapVideoFrameRate() throws JposException {
        logGet("CapVideoFrameRate");
        checkOpened();
        return Data.CapVideoFrameRate;
    }

    @Override
    public boolean getCapVideoResolution() throws JposException {
        logGet("CapVideoResolution");
        checkOpened();
        return Data.CapVideoResolution;
    }

    @Override
    public boolean getCapVideoType() throws JposException {
        logGet("CapVideoType");
        checkOpened();
        return Data.CapVideoType;
    }

    @Override
    public int getContrast() throws JposException {
        logGet("Contrast");
        checkEnabled();
        return Data.Contrast;
    }

    @Override
    public void setContrast(int contrast) throws JposException {
        logPreSet("Contrast");
        checkEnabled();
        check(contrast != Data.Contrast && !Data.CapContrast, JPOS_E_ILLEGAL, "Changing Contrast not supported");
        check(contrast < 0 || contrast > 100, JPOS_E_ILLEGAL, "Invalid contrast: " + contrast);
        VideoCapture.contrast(contrast);
        logSet("Contrast");
    }

    @Override
    public int getExposure() throws JposException {
        logGet("Exposure");
        checkEnabled();
        return Data.Exposure;
    }

    @Override
    public void setExposure(int exposure) throws JposException {
        logPreSet("Exposure");
        checkEnabled();
        check(exposure != Data.Exposure && !Data.CapExposure, JPOS_E_ILLEGAL, "Changing Exposure not supported");
        check(exposure < 0 || exposure > 100, JPOS_E_ILLEGAL, "Invalid exposure: " + exposure);
        VideoCapture.exposure(exposure);
        logSet("Exposure");
    }

    @Override
    public int getGain() throws JposException {
        logGet("Gain");
        checkEnabled();
        return Data.Gain;
    }

    @Override
    public void setGain(int gain) throws JposException {
        logPreSet("Gain");
        checkEnabled();
        check(gain != Data.Gain && !Data.CapGain, JPOS_E_ILLEGAL, "Changing Gain not supported");
        check(gain < 0 || gain > 100, JPOS_E_ILLEGAL, "Invalid gain: " + gain);
        VideoCapture.gain(gain);
        logSet("Gain");
    }

    @Override
    public boolean getHorizontalFlip() throws JposException {
        logGet("HorizontalFlip");
        checkEnabled();
        return Data.HorizontalFlip;
    }

    @Override
    public void setHorizontalFlip(boolean flag) throws JposException {
        logPreSet("HorizontalFlip");
        checkEnabled();
        check(flag != Data.HorizontalFlip && !Data.CapHorizontalFlip, JPOS_E_ILLEGAL, "Changing HorizontalFlip not supported");
        VideoCapture.horizontalFlip(flag);
        logSet("HorizontalFlip");
    }

    @Override
    public int getHue() throws JposException {
        logGet("Hue");
        checkEnabled();
        return Data.Hue;
    }

    @Override
    public void setHue(int hue) throws JposException {
        logPreSet("Hue");
        checkEnabled();
        check(hue != Data.Hue && !Data.CapHue, JPOS_E_ILLEGAL, "Changing Hue not supported");
        check(hue < 0 || hue > 100, JPOS_E_ILLEGAL, "Invalid hue: " + hue);
        VideoCapture.hue(hue);
        logSet("Hue");
    }

    @Override
    public String getPhotoColorSpace() throws JposException {
        logGet("PhotoColorSpace");
        checkEnabled();
        return Data.PhotoColorSpace;
    }

    @Override
    public void setPhotoColorSpace(String photoColorSpace) throws JposException {
        logPreSet("PhotoColorSpace");
        checkEnabled();
        check(!Data.CapPhoto, JPOS_E_ILLEGAL, "Photo function not supported");
        check(!Objects.equals(photoColorSpace, Data.PhotoColorSpace) && !Data.CapPhotoColorSpace, JPOS_E_ILLEGAL, "Changing PhotoColorSpace not supported");
        check(!member(photoColorSpace, Data.PhotoColorSpaceList.split(",")), JPOS_E_ILLEGAL, "Invalid photoColorSpace: " + photoColorSpace);
        VideoCapture.photoColorSpace(photoColorSpace);
        logSet("PhotoColorSpace");
    }

    @Override
    public String getPhotoColorSpaceList() throws JposException {
        logGet("PhotoColorSpaceList");
        checkOpened();
        return Data.PhotoColorSpaceList;
    }

    @Override
    public int getPhotoFrameRate() throws JposException {
        logGet("PhotoFrameRate");
        checkEnabled();
        return Data.PhotoFrameRate;
    }

    @Override
    public void setPhotoFrameRate(int photoFrameRate) throws JposException {
        logPreSet("PhotoFrameRate");
        checkEnabled();
        check(!Data.CapPhoto, JPOS_E_ILLEGAL, "Photo function not supported");
        check(photoFrameRate != Data.PhotoFrameRate && !Data.CapPhotoFrameRate, JPOS_E_ILLEGAL, "Changing PhotoFrameRate not supported");
        check(photoFrameRate < 1 || photoFrameRate > Data.PhotoMaxFrameRate, JPOS_E_ILLEGAL, "Invalid PhotoFrameRate: " + photoFrameRate);
        VideoCapture.photoFrameRate(photoFrameRate);
        logSet("PhotoFrameRate");
    }

    @Override
    public int getPhotoMaxFrameRate() throws JposException {
        logGet("PhotoMaxFrameRate");
        checkOpened();
        return Data.PhotoMaxFrameRate;
    }

    @Override
    public String getPhotoResolution() throws JposException {
        logGet("PhotoResolution");
        checkEnabled();
        return Data.PhotoResolution;
    }

    @Override
    public void setPhotoResolution(String photoResolution) throws JposException {
        logPreSet("PhotoResolution");
        checkEnabled();
        check(!Data.CapPhoto, JPOS_E_ILLEGAL, "Photo function not supported");
        check(!Objects.equals(photoResolution, Data.PhotoResolution) && !Data.CapPhotoResolution, JPOS_E_ILLEGAL, "Changing PhotoResolution not supported");
        check(!member(photoResolution, Data.PhotoResolutionList.split(",")), JPOS_E_ILLEGAL, "Invalid PhotoResolution: " + photoResolution);
        VideoCapture.photoResolution(photoResolution);
        logSet("PhotoResolution");
    }

    @Override
    public String getPhotoResolutionList() throws JposException {
        logGet("PhotoResolutionList");
        checkOpened();
        return Data.PhotoResolutionList;
    }

    @Override
    public String getPhotoType() throws JposException {
        logGet("PhotoType");
        checkEnabled();
        return Data.PhotoType;
    }

    @Override
    public void setPhotoType(String photoType) throws JposException {
        logPreSet("PhotoType");
        checkEnabled();
        check(!Data.CapPhoto, JPOS_E_ILLEGAL, "Photo function not supported");
        check(!Objects.equals(photoType, Data.PhotoType) && !Data.CapPhotoType, JPOS_E_ILLEGAL, "Changing PhotoType not supported");
        check(!member(photoType, Data.PhotoTypeList.split(",")), JPOS_E_ILLEGAL, "Invalid PhotoType: " + photoType);
        VideoCapture.photoType(photoType);
        logSet("PhotoType");
    }

    @Override
    public String getPhotoTypeList() throws JposException {
        logGet("PhotoTypeList");
        checkOpened();
        return Data.PhotoTypeList;
    }

    @Override
    public int getRemainingRecordingTimeInSec() throws JposException {
        if (Data.DeviceEnabled && Data.RemainingRecordingTimeInSec > 0)
            Data.updateRemainingRecordingTimeInSec();
        logGet("RemainingRecordingTimeInSec");
        checkEnabled();
        return Data.RemainingRecordingTimeInSec;
    }

    @Override
    public int getSaturation() throws JposException {
        logGet("Saturation");
        checkEnabled();
        return Data.Saturation;
    }

    @Override
    public void setSaturation(int saturation) throws JposException {
        logPreSet("Saturation");
        checkEnabled();
        check(saturation != Data.Saturation && !Data.CapSaturation, JPOS_E_ILLEGAL, "Changing Saturation not supported");
        check(saturation < 0 || saturation > 100, JPOS_E_ILLEGAL, "Invalid Saturation: " + saturation);
        VideoCapture.saturation(saturation);
        logSet("Saturation");
    }

    @Override
    public int getStorage() throws JposException {
        logGet("Storage");
        checkEnabled();
        return Data.Storage;
    }

    @Override
    public void setStorage(int storage) throws JposException {
        logPreSet("Storage");
        checkEnabled();
        long[] valid = { VCAP_ST_HOST, VCAP_ST_HARDTOTALS, VCAP_ST_HOST_HARDTOTALS };
        checkMember(storage, valid, JPOS_E_ILLEGAL, "Invalid Storage: " + storage);
        check(Data.CapStorage == VCAP_CST_HARDTOTALS_ONLY &&
                storage != VCAP_ST_HARDTOTALS, JPOS_E_ILLEGAL, "Storage not ST_HARDTOTALS");
        check(Data.CapStorage == VCAP_CST_HOST_ONLY &&
                storage != VCAP_ST_HOST, JPOS_E_ILLEGAL, "Storage not ST_HOST");
        VideoCapture.storage(storage);
        logSet("Storage");
    }

    @Override
    public boolean getVerticalFlip() throws JposException {
        logGet("VerticalFlip");
        checkEnabled();
        return Data.VerticalFlip;
    }

    @Override
    public void setVerticalFlip(boolean verticalFlip) throws JposException {
        logPreSet("VerticalFlip");
        checkEnabled();
        check(verticalFlip != Data.VerticalFlip && !Data.CapVerticalFlip, JPOS_E_ILLEGAL, "Changing VerticalFlip not supported");
        VideoCapture.verticalFlip(verticalFlip);
        logSet("VerticalFlip");
    }

    @Override
    public int getVideoCaptureMode() throws JposException {
        logGet("VideoCaptureMode");
        checkEnabled();
        return Data.VideoCaptureMode;
    }

    @Override
    public void setVideoCaptureMode(int videoCaptureMode) throws JposException {
        logPreSet("VideoCaptureMode");
        checkEnabled();
        long[] valid = { VCAP_VCMODE_PHOTO, VCAP_VCMODE_VIDEO };
        checkMember(videoCaptureMode, valid, JPOS_E_ILLEGAL, "Invalid VideoCaptureMode");
        check(!Data.CapPhoto && videoCaptureMode == VCAP_VCMODE_PHOTO,
                JPOS_E_ILLEGAL, "VCMODE_PHOTO not supported");
        check(!Data.CapVideo && videoCaptureMode == VCAP_VCMODE_VIDEO,
                JPOS_E_ILLEGAL, "VCMODE_VIDEO not supported");
        VideoCapture.videoCaptureMode(videoCaptureMode);
        logSet("VideoCaptureMode");
    }

    @Override
    public String getVideoColorSpace() throws JposException {
        logGet("VideoColorSpace");
        checkEnabled();
        return Data.VideoColorSpace;
    }

    @Override
    public void setVideoColorSpace(String videoColorSpace) throws JposException {
        logPreSet("VideoColorSpace");
        checkEnabled();
        check(!Data.CapVideo, JPOS_E_ILLEGAL, "Video function not supported");
        check(!Objects.equals(videoColorSpace, Data.VideoColorSpace) && !Data.CapVideoColorSpace, JPOS_E_ILLEGAL, "Changing VideoColorSpace not supported");
        check(!member(videoColorSpace, Data.VideoColorSpaceList.split(",")), JPOS_E_ILLEGAL, "Invalid VideoColorSpace: " + videoColorSpace);
        VideoCapture.videoColorSpace(videoColorSpace);
        logSet("VideoColorSpace");
    }

    @Override
    public String getVideoColorSpaceList() throws JposException {
        logGet("VideoColorSpaceList");
        checkOpened();
        return Data.VideoColorSpaceList;
    }

    @Override
    public int getVideoFrameRate() throws JposException {
        logGet("VideoFrameRate");
        checkEnabled();
        return Data.VideoFrameRate;
    }

    @Override
    public void setVideoFrameRate(int videoFrameRate) throws JposException {
        logPreSet("VideoFrameRate");
        checkEnabled();
        check(!Data.CapVideo, JPOS_E_ILLEGAL, "Video function not supported");
        check(videoFrameRate != Data.VideoFrameRate && !Data.CapVideoFrameRate, JPOS_E_ILLEGAL, "Changing VideoFrameRate not supported");
        check(videoFrameRate < 1 || videoFrameRate > Data.VideoMaxFrameRate, JPOS_E_ILLEGAL, "Invalid PhotoFrameRate: " + videoFrameRate);
        VideoCapture.videoFrameRate(videoFrameRate);
        logSet("VideoFrameRate");
    }

    @Override
    public int getVideoMaxFrameRate() throws JposException {
        logGet("VideoMaxFrameRate");
        checkOpened();
        return Data.VideoMaxFrameRate;
    }

    @Override
    public String getVideoResolution() throws JposException {
        logGet("VideoResolution");
        checkEnabled();
        return Data.VideoResolution;
    }

    @Override
    public void setVideoResolution(String videoResolution) throws JposException {
        logPreSet("VideoResolution");
        checkEnabled();
        check(!Data.CapVideo, JPOS_E_ILLEGAL, "Video function not supported");
        check(!Objects.equals(videoResolution, Data.VideoResolution) && !Data.CapVideoResolution, JPOS_E_ILLEGAL, "Changing VideoResolution not supported");
        check(!member(videoResolution, Data.VideoResolutionList.split(",")), JPOS_E_ILLEGAL, "Invalid VideoResolution: " + videoResolution);
        VideoCapture.videoResolution(videoResolution);
        logSet("VideoResolution");
    }

    @Override
    public String getVideoResolutionList() throws JposException {
        logGet("VideoResolutionList");
        checkOpened();
        return Data.VideoResolutionList;
    }

    @Override
    public String getVideoType() throws JposException {
        logGet("VideoType");
        checkEnabled();
        return Data.VideoType;
    }

    @Override
    public void setVideoType(String videoType) throws JposException {
        logPreSet("VideoType");
        checkEnabled();
        check(!Data.CapVideo, JPOS_E_ILLEGAL, "Video function not supported");
        check(!Objects.equals(videoType, Data.VideoType) && !Data.CapVideoType, JPOS_E_ILLEGAL, "Changing VideoType not supported");
        check(!member(videoType, Data.VideoTypeList.split(",")), JPOS_E_ILLEGAL, "Invalid VideoType: " + videoType);
        VideoCapture.videoType(videoType);
        logSet("VideoType");
    }

    @Override
    public String getVideoTypeList() throws JposException {
        logGet("VideoTypeList");
        checkOpened();
        return Data.VideoTypeList;
    }

    @Override
    public void startVideo(String fileName, boolean overWrite, int recordingTime) throws JposException {
        logPreCall("StartVideo", removeOuterArraySpecifier(new Object[]{fileName, overWrite, recordingTime}, Device.MaxArrayStringElements));
        checkEnabled();
        check(Data.VideoCaptureMode != VCAP_VCMODE_VIDEO, JPOS_E_ILLEGAL,
                "Cannot capture video in photo mode");
        check(Data.AsyncInputActive, JPOS_E_BUSY, "Just recording other video");
        check(recordingTime <= 0 && recordingTime != JPOS_FOREVER,
                JPOS_E_ILLEGAL, "Invalid recording time: " + recordingTime);
        StartVideo request = VideoCapture.startVideo(fileName, overWrite, recordingTime);
        if (request != null)
            request.enqueue();
        logCall("StartVideo");
    }

    @Override
    public void stopVideo() throws JposException {
        logPreCall("StopVideo");
        checkEnabled();
        check(Data.VideoCaptureMode != VCAP_VCMODE_VIDEO, JPOS_E_ILLEGAL,
                "Cannot capture video in photo mode");
        check(!Data.AsyncInputActive, JPOS_E_ILLEGAL, "Recording not active");
        VideoCapture.stopVideo();
        logCall("StopVideo");
    }

    @Override
    public void takePhoto(String fileName, boolean overWrite, int timeout) throws JposException {
        logPreCall("TakePhoto", removeOuterArraySpecifier(new Object[]{fileName, overWrite, timeout}, Device.MaxArrayStringElements));
        checkEnabled();
        check(Data.VideoCaptureMode != VCAP_VCMODE_PHOTO, JPOS_E_ILLEGAL,
                "Cannot take photo in video mode");
        check(Data.AsyncInputActive, JPOS_E_BUSY, "Just taking other photo");
        check(timeout <= 0 && timeout != JPOS_FOREVER,
                JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        TakePhoto request = VideoCapture.takePhoto(fileName, overWrite, timeout);
        if (request != null)
            request.enqueue();
        logCall("TakePhoto");
    }
}
