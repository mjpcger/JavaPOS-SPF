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

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import jpos.JposException;

import static jpos.VideoCaptureConst.*;

/**
 * Class containing the video capture specific properties, their default values and default implementations of
 * VideoCaptureInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Video Capture.
 */
public class VideoCaptureProperties extends JposCommonProperties implements VideoCaptureInterface {
    /**
     * UPOS property AutoExposure. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean AutoExposure = false;

    /**
     * UPOS property AutoFocus. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean AutoFocus = false;

    /**
     * UPOS property AutoGain. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean AutoGain = false;

    /**
     * UPOS property AutoWhiteBalance. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean AutoWhiteBalance = false;

    /**
     * UPOS property Brightness. Default: 50. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int Brightness = 50;

    /**
     * UPOS property CapAssociatedHardTotalsDevice. Default: an empty string. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public String CapAssociatedHardTotalsDevice = "";

    /**
     * UPOS property CapAutoExposure. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapAutoExposure = false;

    /**
     * UPOS property CapAutoFocus. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapAutoFocus = false;

    /**
     * UPOS property CapAutoGain. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapAutoGain = false;

    /**
     * UPOS property CapAutoWhiteBalance. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapAutoWhiteBalance = false;

    /**
     * UPOS property CapBrightness. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapBrightness = false;

    /**
     * UPOS property CapContrast. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapContrast = false;

    /**
     * UPOS property CapExposure. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapExposure = false;

    /**
     * UPOS property CapGain. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapGain = false;

    /**
     * UPOS property CapHorizontalFlip. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapHorizontalFlip = false;

    /**
     * UPOS property CapHue. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapHue = false;

    /**
     * UPOS property CapPhoto. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapPhoto = false;

    /**
     * UPOS property CapPhotoColorSpace. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapPhotoColorSpace = false;

    /**
     * UPOS property CapPhotoFrameRate. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapPhotoFrameRate = false;

    /**
     * UPOS property CapPhotoResolution. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapPhotoResolution = false;

    /**
     * UPOS property CapPhotoType. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapPhotoType = false;

    /**
     * UPOS property CapSaturation. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapSaturation = false;

    /**
     * UPOS property CapStorage. Default: null. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method to CST_HARDTOTALS_ONLY, CST_HOST_ONLY or CST_ALL.<br>
     * If not overwritten, it will be set in initOnOpen to CST_HOST_ONLY if CapAssociatedHardTotalsDevice is an empty
     * string, otherwise to CST_ALL.
     */
    public Integer CapStorage = null;

    /**
     * UPOS property CapVerticalFlip. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapVerticalFlip = false;

    /**
     * UPOS property CapVideo. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapVideo = false;

    /**
     * UPOS property CapVideoColorSpace. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapVideoColorSpace = false;

    /**
     * UPOS property CapVideoFrameRate. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapVideoFrameRate = false;

    /**
     * UPOS property CapVideoResolution. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapVideoResolution = false;

    /**
     * UPOS property CapVideoType. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapVideoType = false;

    /**
     * UPOS property Contrast. Default: 50. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int Contrast = 50;

    /**
     * UPOS property Exposure. Default: 50. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int Exposure = 50;

    /**
     * UPOS property Gain. Default: 50. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int Gain = 50;

    /**
     * UPOS property HorizontalFlip. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean HorizontalFlip = false;

    /**
     * UPOS property Hue. Default: 50. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int Hue = 50;

    /**
     * UPOS property PhotoColorSpace. Default: null. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.<br>
     * If not set otherwise, the default becomes the first entry of PhotoColorSpaceList in initOnOpen.
     */
    public String PhotoColorSpace = null;

    /**
     * UPOS property PhotoColorSpaceList. Default: an empty string. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public String PhotoColorSpaceList = "";

    /**
     * UPOS property PhotoFrameRate. Default: 1. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int PhotoFrameRate = 1;

    /**
     * UPOS property PhotoMaxFrameRate. Default: 1. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int PhotoMaxFrameRate = 1;

    /**
     * UPOS property PhotoResolution. Default: null. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.<br>
     * If not set otherwise, the default becomes the first entry of PhotoResolutionList in initOnOpen.
     */
    public String PhotoResolution = null;

    /**
     * UPOS property PhotoResolutionList. Default: an empty string. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public String PhotoResolutionList = "";

    /**
     * UPOS property PhotoType. Default: null. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.<br>
     * If not set otherwise, the default becomes the first entry of PhotoTypeList in initOnOpen.
     */
    public String PhotoType = null;

    /**
     * UPOS property PhotoTypeList. Default: an empty string. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public String PhotoTypeList = "";

    /**
     * UPOS property RemainingRecordingTimeInSec. Default: 0. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.<br>
     * Before each read of RemainingRecordingTimeInSec, the service calls updateRemainingRecordingTimeInSec() to update
     * the value if it is neither 0 nor FOREVER.
     */
    public int RemainingRecordingTimeInSec = 0;

    /**
     * UPOS property Saturation. Default: 50. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int Saturation = 50;

    /**
     * UPOS property Storage. Default: null. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.<br>
     * If not set otherwise, it will be set to ST_HARDTOTALS of CapStorage is CST_HARDTOTALS_ONLY and to ST_HOST
     * otherwise in initOnOpen.
     */
    public Integer Storage = null;

    /**
     * UPOS property VerticalFlip. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean VerticalFlip = false;

    /**
     * UPOS property VideoCaptureMode. Default: null. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.<br>
     * If not set otherwise, it will be set to VCMODE_VIDEO if CapVideo is true, otherwise to VCMODE_PHOTO in
     * initOnOpen. Keep in mind: If both, CapVideo and CapPhoto are false after changeDefaults has been called, the
     * open method will throw a JposException with ErrorCode = E_NOSERVICE.
     */
    public Integer VideoCaptureMode = null;

    /**
     * UPOS property VideoColorSpace. Default: null. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.<br>
     * If not set otherwise, the default becomes the first entry of VideoColorSpaceList in initOnOpen.
     */
    public String VideoColorSpace = null;

    /**
     * UPOS property VideoColorSpaceList. Default: an empty string. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public String VideoColorSpaceList = "";

    /**
     * UPOS property VideoFrameRate. Default: 30. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int VideoFrameRate = 30;

    /**
     * UPOS property VideoMaxFrameRate. Default: 30. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int VideoMaxFrameRate = 30;

    /**
     * UPOS property VideoResolution. Default: null. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.<br>
     * If not set otherwise, the default becomes the first entry of VideoResolutionList in initOnOpen.
     */
    public String VideoResolution = null;

    /**
     * UPOS property VideoResolutionList. Default: an empty string. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public String VideoResolutionList = "";

    /**
     * UPOS property VideoType. Default: null. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.<br>
     * If not set otherwise, the default becomes the first entry of VideoTypeList in initOnOpen.
     */
    public String VideoType = null;

    /**
     * UPOS property VideoTypeList. Default: an empty string. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public String VideoTypeList = "";

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected VideoCaptureProperties(int dev) {
        super(dev);
    }

    @Override
    public void initOnOpen() {
        super.initOnOpen();
        DataEventEnabled = true;
        if (CapStorage == null)
            CapStorage = CapAssociatedHardTotalsDevice.length() == 0 ?
                    VCAP_CST_HOST_ONLY : VCAP_CST_ALL;
        if (PhotoColorSpace == null)
            PhotoColorSpace = PhotoColorSpaceList.split(",")[0];
        if (PhotoResolution == null)
            PhotoResolution = PhotoResolutionList.split(",")[0];
        if (PhotoType == null)
            PhotoType = PhotoTypeList.split(",")[0];
        if (Storage == null)
            Storage = CapStorage == VCAP_CST_HARDTOTALS_ONLY ?
                    VCAP_ST_HARDTOTALS : VCAP_ST_HOST;
        if (VideoCaptureMode == null)
            VideoCaptureMode = CapVideo ? VCAP_VCMODE_VIDEO : VCAP_VCMODE_PHOTO;
        if (VideoColorSpace == null)
            VideoColorSpace = VideoColorSpaceList.split(",")[0];
        if (VideoResolution == null)
            VideoResolution = VideoResolutionList.split(",")[0];
        if (VideoType == null)
            VideoType = VideoTypeList.split(",")[0];
    }

    @Override
    public void initOnEnable(boolean enable) {
        if (enable) {
            RecordingTimeInSec = RemainingRecordingTimeInSec = 0;
            RecordingStartTime = System.currentTimeMillis();
        }
    }

    private long RecordingStartTime;    // Recording start system time in milliseconds.
    private int  RecordingTimeInSec;    // Recording time as given in recording start method.

    public void updateRemainingRecordingTimeInSec() {
        int deltaInSeconds = (int) ((System.currentTimeMillis() - RecordingStartTime) / 1000);
        RemainingRecordingTimeInSec = Math.max(RecordingTimeInSec - deltaInSeconds, 0);
    }

    @Override
    public void autoExposure(boolean flag) throws JposException {
        AutoExposure = flag;
    }

    @Override
    public void autoFocus(boolean flag) throws JposException {
        AutoFocus = flag;
    }

    @Override
    public void autoGain(boolean flag) throws JposException {
        AutoGain = flag;
    }

    @Override
    public void autoWhiteBalance(boolean flag) throws JposException {
        AutoWhiteBalance = flag;
    }

    @Override
    public void brightness(int brightness) throws JposException {
        Brightness = brightness;
    }

    @Override
    public void contrast(int contrast) throws JposException {
        Contrast = contrast;
    }

    @Override
    public void exposure(int exposure) throws JposException {
        Exposure = exposure;
    }

    @Override
    public void gain(int gain) throws JposException {
        Gain = gain;
    }

    @Override
    public void horizontalFlip(boolean flag) throws JposException {
        HorizontalFlip = flag;
    }

    @Override
    public void hue(int hue) throws JposException {
        Hue = hue;
    }

    @Override
    public void photoColorSpace(String photoColorSpace) throws JposException {
        PhotoColorSpace = photoColorSpace;
    }

    @Override
    public void photoFrameRate(int photoFrameRate) throws JposException {
        PhotoFrameRate = photoFrameRate;
    }

    @Override
    public void photoResolution(String photoResolution) throws JposException {
        PhotoResolution = photoResolution;
    }

    @Override
    public void photoType(String photoType) throws JposException {
        PhotoType = photoType;
    }

    @Override
    public void saturation(int saturation) throws JposException {
        Saturation = saturation;
    }

    @Override
    public void storage(int storage) throws JposException {
        Storage = storage;
    }

    @Override
    public void verticalFlip(boolean verticalFlip) throws JposException {
        VerticalFlip = verticalFlip;
    }

    @Override
    public void videoCaptureMode(int videoCaptureMode) throws JposException {
        VideoCaptureMode = videoCaptureMode;
    }

    @Override
    public void videoColorSpace(String videoColorSpace) throws JposException {
        VideoColorSpace = videoColorSpace;
    }

    @Override
    public void videoFrameRate(int videoFrameRate) throws JposException {
        VideoFrameRate = videoFrameRate;
    }

    @Override
    public void videoResolution(String videoResolution) throws JposException {
        VideoResolution = videoResolution;
    }

    @Override
    public void videoType(String videoType) throws JposException {
        VideoType = videoType;
    }

    @Override
    public StartVideo startVideo(String fileName, boolean overWrite, int recordingTime) throws JposException {
        return new StartVideo(this, fileName, overWrite, recordingTime);
    }

    @Override
    public void startVideo(StartVideo startVideo) throws JposException {
    }

    @Override
    public void stopVideo() throws JposException {
    }

    @Override
    public TakePhoto takePhoto(String fileName, boolean overWrite, int timeout) throws JposException {
        return new TakePhoto(this, fileName, overWrite, timeout);
    }

    @Override
    public void takePhoto(TakePhoto takePhoto) throws JposException {
    }
}
