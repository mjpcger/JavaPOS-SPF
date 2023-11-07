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

import de.gmxhome.conrad.jpos.jpos_base.JposBaseInterface;
import jpos.JposException;

/**
 * Interface for methods that implement property setter and method calls for the GestureControl device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Gesture Control.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface GraphicDisplayInterface extends JposBaseInterface {
    /**
     * Final part of setBrightness method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>brightness is between 0 and 100.</li>
     * </ul>
     * @param brightness Brightness level in percent.
     * @throws JposException If an error occurs.
     */
    void brightness(int brightness) throws JposException;

    /**
     * Final part of setDisplayMode method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>displayMode is one of DMODE_HIDDEN, DMODE_IMAGE_FIT, DMODE_IMAGE_FILL, DMODE_IMAGE_CENTER,
     *                 DMODE_VIDEO_NORMAL, GDSP_DMODE_VIDEO_FULL or GDSP_DMODE_WEB.</li>
     * </ul>
     * @param displayMode Critical battery low power level in percent.
     * @throws JposException If an error occurs.
     */
    void displayMode(int displayMode) throws JposException;

    /**
     * Final part of setImageType method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapImageType is true or imageType is equal to the current imageType value,</li>
     *     <li>imageType is one of the values listed in property ImageTypeList.</li>
     * </ul>
     * @param imageType Critical battery low power level in percent.
     * @throws JposException If an error occurs.
     */
    void imageType(String imageType) throws JposException;

    /**
     * Final part of setStorage method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>storage is one of ST_HOST or ST_HARDTOTALS,</li>
     *     <li>If CapStorage is CST_HOST_ONLY, storage equals ST_HOST,</li>
     *     <li>If CapStorage is CST_HARDTOTALS_ONLY, storage equals ST_HARDTOTALS.</li>
     * </ul>
     * Keep in mind: Even if the value ST_HOST_HARDTOTALS has been specified for property Storage within the UPOS
     * specification, this value is senseless because reading from both sources at the same time is
     * not practical.
     * @param storage Video or image storage specifier.
     * @throws JposException If an error occurs.
     */
    void storage(int storage) throws JposException;

    /**
     * Final part of setVideoType method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapVideoType is true or videoType is equal to the current VideoType property value,</li>
     *     <li>videoType is one of the values listed in VideoTypeList.</li>
     * </ul>
     * @param videoType Critical battery low power level in percent.
     * @throws JposException If an error occurs.
     */
    void videoType(String videoType) throws JposException;

    /**
     * Final part of setVolume method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>volume is between 0 and 100.</li>
     * </ul>
     * @param volume Volume power level in percent.
     * @throws JposException If an error occurs.
     */
    void volume(int volume) throws JposException;

    /**
     * final part of CancelURLLoading method. Can be overwritten within derived
     * classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>URL loading indicated by StatusUpdateEvents is active.</li>
     * </ul>
     * Remark: Even if described as asynchronous method, this method will be performed synchronously. It forces URL
     * loading cancellation and returns immediately. The service must ensure that the loading process is subsequently
     * terminated and the corresponding events will be generated.
     *
     * @throws JposException    If an error occurs.
     */
    public void cancelURLLoading() throws JposException;

    /**
     * final part of StopVideo method. Can be overwritten within derived
     * classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Video playing indicated by StatusUpdateEvents is active.</li>
     * </ul>
     * Remark: Even if described as asynchronous method, this method will be performed synchronously. It forces URL
     * loading cancellation and returns immediately. The service must ensure that the loading process is subsequently
     * terminated and the corresponding events will be generated.
     *
     * @throws JposException    If an error occurs.
     */
    public void stopVideo() throws JposException;

    /**
     * Validation part of GoURLBack method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     *
     * @return                  GoURLBack object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public GoURLBack goURLBack() throws JposException;

    /**
     * Final part of GoURLBack method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a GoURLBack object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method to be used by GoURLBack.
     * @throws JposException    If an error occurs.
     */
    public void goURLBack(GoURLBack request) throws JposException;

    /**
     * Validation part of GoURLForward method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     *
     * @return                  GoURLForward object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public GoURLForward goURLForward() throws JposException;

    /**
     * Final part of GoURLForward method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a GoURLForward object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method to be used by GoURLForward.
     * @throws JposException    If an error occurs.
     */
    public void goURLForward(GoURLForward request) throws JposException;

    /**
     * Validation part of LoadImage method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapImageType is true and ImageTypeList is not an empty string,</li>
     *     <li>DisplayMode is DMODE_IMAGE_FIT, DMODE_IMAGE_FILL or DMODE_IMAGE_CENTER.</li>
     * </ul>
     *
     * @param fileName          The file name of the image to be loaded.
     * @return                  LoadImage object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public LoadImage loadImage(String fileName) throws JposException;

    /**
     * Final part of LoadImage method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a LoadImage object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method to be used by LoadImage.
     * @throws JposException    If an error occurs.
     */
    public void loadImage(LoadImage request) throws JposException;

    /**
     * Validation part of LoadURL method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>url is neither an empty string nor null.</li>
     * </ul>
     *
     * @param url               The URL of the web page to be loaded.
     * @return                  LoadURL object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public LoadURL loadURL(String url) throws JposException;

    /**
     * Final part of LoadURL method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a LoadURL object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method to be used by LoadURL.
     * @throws JposException    If an error occurs.
     */
    public void loadURL(LoadURL request) throws JposException;

    /**
     * Validation part of PlayVideo method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapVideoType is true and VideoTypeList is not an empty string,</li>
     *     <li>DisplayMode is DMODE_VIDEO_NORMAL or DMODE_VIDEO_FULL.</li>
     * </ul>
     *
     * @param fileName          The file name of the video to be played.
     * @param loop              Specified whether play back shall loop.
     * @return                  PlayVideo object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PlayVideo playVideo(String fileName, boolean loop) throws JposException;

    /**
     * Final part of PlayVideo method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PlayVideo object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method to be used by PlayVideo.
     * @throws JposException    If an error occurs.
     */
    public void playVideo(PlayVideo request) throws JposException;

    /**
     * Validation part of UpdateURLPage method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     * </ul>
     *
     * @return                  UpdateURLPage object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public UpdateURLPage updateURLPage() throws JposException;

    /**
     * Final part of UpdateURLPage method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a UpdateURLPage object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method to be used by UpdateURLPage.
     * @throws JposException    If an error occurs.
     */
    public void updateURLPage(UpdateURLPage request) throws JposException;
}
