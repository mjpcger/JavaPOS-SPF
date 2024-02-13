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

import de.gmxhome.conrad.jpos.jpos_base.JposBaseInterface;
import jpos.JposException;

/**
 * Interface for methods that implement property setter and method calls for the VideoCapture device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Video Capture.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface VideoCaptureInterface extends JposBaseInterface {
    /**
     * Update part for getting RemainingRecordingTimeInSec. Will be called before returning the corresponding property
     * value to application. Can be overwritten within derived class, if necessary. Default processing is to update
     * RemainingRecordingTimeInSec to the recording time as given in the start method, subtracted by the no. of seconds
     * since start method call.
     */
    public void updateRemainingRecordingTimeInSec() throws JposException;

    /**
     * Final part of setting AutoExposure. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapAutoExposure is true or flag equals the current value of AutoExposure.</li>
     * </ul>
     *
     * @param flag New AutoExposure value.
     * @throws JposException If an error occurs.
     */
    void autoExposure(boolean flag) throws JposException;

    /**
     * Final part of setting AutoFocus. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapAutoFocus is true or flag equals the current value of AutoFocus.</li>
     * </ul>
     *
     * @param flag New AutoFocus value.
     * @throws JposException If an error occurs.
     */
    void autoFocus(boolean flag) throws JposException;

    /**
     * Final part of setting AutoGain. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapAutoGain is true or flag equals the current value of AutoGain.</li>
     * </ul>
     *
     * @param flag New AutoGain value.
     * @throws JposException If an error occurs.
     */
    void autoGain(boolean flag) throws JposException;

    /**
     * Final part of setting AutoWhiteBalance. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapAutoWhiteBalance is true or flag equals the current value of AutoWhiteBalance.</li>
     * </ul>
     *
     * @param flag New AutoWhiteBalance value.
     * @throws JposException If an error occurs.
     */
    void autoWhiteBalance(boolean flag) throws JposException;

    /**
     * Final part of setting Brightness. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapBrightness is true or brightness equals the current value of Brightness,</li>
     *     <li>0 &le; brightness &le 100.</li>
     * </ul>
     *
     * @param brightness New brightness level between 0 and 100.
     * @throws JposException If an error occurs.
     */
    void brightness(int brightness) throws JposException;

    /**
     * Final part of setting Contrast. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapContrast is true or contrast equals the current value of Contrast,</li>
     *     <li>0 &le; contrast &le 100.</li>
     * </ul>
     *
     * @param contrast New contrast level between 0 and 100.
     * @throws JposException If an error occurs.
     */
    void contrast(int contrast) throws JposException;

    /**
     * Final part of setting Exposure. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapExposure is true or exposure equals the current value of Exposure,</li>
     *     <li>0 &le; exposure &le 100.</li>
     * </ul>
     *
     * @param exposure New exposure level between 0 and 100.
     * @throws JposException If an error occurs.
     */
    void exposure(int exposure) throws JposException;

    /**
     * Final part of setting Gain. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapGain is true or gain equals the current value of Gain,</li>
     *     <li>0 &le; gain &le 100.</li>
     * </ul>
     *
     * @param gain New gain level between 0 and 100.
     * @throws JposException If an error occurs.
     */
    void gain(int gain) throws JposException;

    /**
     * Final part of setting HorizontalFlip. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapHorizontalFlip is true or flag equals the current value of HorizontalFlip.</li>
     * </ul>
     *
     * @param flag New HorizontalFlip value.
     * @throws JposException If an error occurs.
     */
    void horizontalFlip(boolean flag) throws JposException;

    /**
     * Final part of setting Hue. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapHue is true or hue equals the current value of Hue,</li>
     *     <li>0 &le; hue &le 100.</li>
     * </ul>
     *
     * @param hue New hue level between 0 and 100.
     * @throws JposException If an error occurs.
     */
    void hue(int hue) throws JposException;

    /**
     * Final part of setting PhotoColorSpace. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapPhoto is true,</li>
     *     <li>CapPhotoColorSpace is true or photoColorSpace equals the current value of PhotoColorSpace,</li>
     *     <li>photoColorSpace is one of the values listed in PhotoColorSpaceList.</li>
     * </ul>
     *
     * @param photoColorSpace New photo color space value.
     * @throws JposException If an error occurs.
     */
    void photoColorSpace(String photoColorSpace) throws JposException;

    /**
     * Final part of setting PhotoFrameRate. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapPhoto is true,</li>
     *     <li>CapPhotoFrameRate is true or photoFrameRate equals the current value of PhotoFrameRate,</li>
     *     <li>1 &le; photoFrameRate &le PhotoMaxFrameRate.</li>
     * </ul>
     *
     * @param photoFrameRate New photo frame rate between 1 and PhotoMaxFrameRate.
     * @throws JposException If an error occurs.
     */
    void photoFrameRate(int photoFrameRate) throws JposException;

    /**
     * Final part of setting PhotoResolution. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapPhoto is true,</li>
     *     <li>CapPhotoResolution is true or photoResolution equals the current value of PhotoResolution,</li>
     *     <li>photoResolution is one of the values listed in PhotoResolutionList.</li>
     * </ul>
     *
     * @param photoResolution New photo resolution value.
     * @throws JposException If an error occurs.
     */
    void photoResolution(String photoResolution) throws JposException;

    /**
     * Final part of setting PhotoType. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapPhoto is true,</li>
     *     <li>CapPhotoType is true or photoType equals the current value of PhotoType,</li>
     *     <li>photoType is one of the values listed in PhotoTypeList.</li>
     * </ul>
     *
     * @param photoType New photo type value.
     * @throws JposException If an error occurs.
     */
    void photoType(String photoType) throws JposException;

    /**
     * Final part of setting Saturation. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSaturation is true or saturation equals the current value of Saturation,</li>
     *     <li>0 &le; saturation &le 100.</li>
     * </ul>
     *
     * @param saturation New saturation level between 0 and 100.
     * @throws JposException If an error occurs.
     */
    void saturation(int saturation) throws JposException;

    /**
     * Final part of setting Storage. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>storage is one of ST_HOST, ST_HARDTOTALS or ST_HOST_HARDTOTALS,</li>
     *     <li>if CapStorage is CST_HARDTOTALS_ONLY, storage is ST_HARDTOTALS,</li>
     *     <li>if CapStorage is CST_HOST_ONLY, storage is ST_HOST.</li>
     * </ul>
     *
     * @param storage New Storage value.
     * @throws JposException If an error occurs.
     */
    void storage(int storage) throws JposException;

    /**
     * Final part of setting VerticalFlip. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapVerticalFlip is true or verticalFlip equals the current value of VerticalFlip.</li>
     * </ul>
     *
     * @param verticalFlip New VerticalFlip value.
     * @throws JposException If an error occurs.
     */
    void verticalFlip(boolean verticalFlip) throws JposException;

    /**
     * Final part of setting VideoCaptureMode. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapVideo is true,</li>
     *     <li>videoCaptureMode is one of  VCMODE_PHOTO or VCMODE_VIDEO,</li>
     *     <li>if CapPhoto is false, videoCaptureMode is VCMODE_VIDEO,</li>
     *     <li>if CapVideo is false, videoCaptureMode is VCMODE_PHOTO.</li>
     * </ul>
     *
     * @param videoCaptureMode New video capture mode, VCMODE_PHOTO or VCMODE_VIDEO.
     * @throws JposException If an error occurs.
     */
    void videoCaptureMode(int videoCaptureMode) throws JposException;

    /**
     * Final part of setting VideoColorSpace. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapVideo is true,</li>
     *     <li>CapVideoColorSpace is true or videoColorSpace equals the current value of VideoColorSpace,</li>
     *     <li>videoColorSpace is one of the values listed in VideoColorSpaceList.</li>
     * </ul>
     *
     * @param videoColorSpace New video color space value.
     * @throws JposException If an error occurs.
     */
    void videoColorSpace(String videoColorSpace) throws JposException;

    /**
     * Final part of setting VideoFrameRate. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapVideo is true,</li>
     *     <li>CapVideoFrameRate is true or videoFrameRate equals the current value of VideoFrameRate,</li>
     *     <li>1 &le; videoFrameRate &le VideoMaxFrameRate.</li>
     * </ul>
     *
     * @param videoFrameRate New video frame rate between 1 and VideoMaxFrameRate.
     * @throws JposException If an error occurs.
     */
    void videoFrameRate(int videoFrameRate) throws JposException;

    /**
     * Final part of setting VideoResolution. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapVideo is true,</li>
     *     <li>CapVideoResolution is true or videoResolution equals the current value of VideoResolution,</li>
     *     <li>videoResolution is one of the values listed in VideoResolutionList.</li>
     * </ul>
     *
     * @param videoResolution New video resolution value.
     * @throws JposException If an error occurs.
     */
    void videoResolution(String videoResolution) throws JposException;

    /**
     * Final part of setting VideoType. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapVideo is true,</li>
     *     <li>CapVideoType is true or videoType equals the current value of VideoType,</li>
     *     <li>videoType is one of the values in VideoTypeList.</li>
     * </ul>
     *
     * @param videoType New video type value.
     * @throws JposException If an error occurs.
     */
    void videoType(String videoType) throws JposException;


    /**
     * Final part of StartVideo method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall perform additional validation and starts video capture. The video capturing functions will be
     * buffered for asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>VideoCaptureMode is VCMODE_VIDEO,</li>
     *     <li>No other video recording is active,</li>
     *     <li>recordingTime is &gt; 0 or FOREVER.</li>
     * </ul>
     *
     *
     * @param fileName      Indicates the video target located on host, HardTotals device or both, depending on Storage property.
     * @param overWrite     Specifies whether the sound file shall be overwritten if just present. If false, StartRecording
     *                      will fail if the specified file just exists.
     * @param recordingTime Specifies the recording time in seconds. If FOREVER, method StopRecording must be used to finish
     *                      the recording process.
     * @throws JposException    If an error occurs.
     * @return StartRecording object for use in final part.
     */
    StartVideo startVideo(String fileName, boolean overWrite, int recordingTime) throws JposException;

    /**
     * Final part of capturing video and storing video in specified location. Must be overwritten within derived classes.
     * The parameters of the method will be passed via a StartVideo object. This method
     * will be called asynchronously when capturing video has been started.
     *
     * @param request           Input request object returned by the startVideo finalization method that contains all
     *                          parameters passed to startVideo previously.
     * @throws JposException    If an error occurs.
     */
    void startVideo(StartVideo request) throws JposException;

    /**
     * Final part of StopVideo method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapStatus is true,</li>
     *     <li>VideoCaptureMode is VCMODE_VIDEO,</li>
     *     <li>Video recording is active.</li>
     * </ul>
     * If the deprecated waitForDrawerClose method (which must handle beeping itself) shall be used,
     * this method must throw a JposException with ErrorCode = 0.
     *
     * @throws JposException If an error occurs.
     */
    void stopVideo() throws JposException;

    /**
     * Final part of TakePhoto method. Can be overwritten within derived classes, if necessary.
     * This method shall perform additional validation and start taking photo. The functions to store the photo will be
     * buffered for asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>VideoCaptureMode is VCMODE_PHOTO,</li>
     *     <li>No other photo storing is active,</li>
     *     <li>timeout is &gt; 0 or FOREVER.</li>
     * </ul>
     *
     *
     * @param fileName      Indicates the video target located on host, HardTotals device or both, depending on Storage property.
     * @param overWrite     Specifies whether the sound file shall be overwritten if just present. If false, StartRecording
     *                      will fail if the specified file just exists.
     * @param timeout       Specifies the recording timeout in milliseconds.
     * @throws JposException    If an error occurs.
     * @return StartRecording object for use in final part.
     */
    TakePhoto takePhoto(String fileName, boolean overWrite, int timeout) throws JposException;

    /**
     * Final part of taking photo and storing photo in specified location. Must be overwritten within derived classes.
     * The parameters of the method will be passed via a TakePhoto object. This method
     * will be called asynchronously when the taking photo has been started.
     *
     * @param request           Input request object returned by the takePhoto finalization method that contains all
     *                          parameters passed to takePhoto previously..
     * @throws JposException    If an error occurs.
     */
    void takePhoto(TakePhoto request) throws JposException;
}
