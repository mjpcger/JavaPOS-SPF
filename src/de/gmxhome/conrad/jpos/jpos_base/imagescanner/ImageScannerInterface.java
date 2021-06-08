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

import de.gmxhome.conrad.jpos.jpos_base.JposBaseInterface;
import jpos.JposException;

/**
 * Interface for methods that implement property setter and method calls for the ImageScanner device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Image Scanner.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface ImageScannerInterface extends JposBaseInterface {
    /**
     * Final part of setting AimMode. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>CapAim is true or aimMode equals the previous property value.</li>
     * </ul>
     *
     * @param aimMode New value for AimMode property.
     * @throws JposException If an error occurs.
     */
    void aimMode(boolean aimMode) throws JposException;

    /**
     * Final part of setting IlluminateMode. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>CapIlluminate is true or illuminateMode equals the previous property value.</li>
     * </ul>
     *
     * @param illuminateMode New value for IlluminateMode property.
     * @throws JposException If an error occurs.
     */
    void illuminateMode(boolean illuminateMode) throws JposException;

    /**
     * Final part of setting ImageMode. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>imageMode is one of DECODE_ONLY, STILL_ONLY, STILL_DECODE, VIDEO_DECODE, VIDEO_STILL or ALL,</li>
     *     <li>imageMode matches the capabilities CapDecodeData, CapImageData and CapVideoData.</li>
     * </ul>
     *
     * @param imageMode New value for ImageMode property.
     * @throws JposException If an error occurs.
     */
    void imageMode(int imageMode) throws JposException;

    /**
     * Final part of setting ImageQuality. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>imageQuality is one of QUAL_LOW, QUAL_MED or QUAL_HIGH,</li>
     *     <li>if CapImageQuality is false, imageQuality equals the previous property value.</li>
     * </ul>
     *
     * @param imageQuality New value for ImageQuality property.
     * @throws JposException If an error occurs.
     */
    void imageQuality(int imageQuality) throws JposException;

    /**
     * Final part of setting VideoCount. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>videoRate is a positive value.</li>
     * </ul>
     *
     * @param videoCount New value for VideoCount property.
     * @throws JposException If an error occurs.
     */
    void videoCount(int videoCount) throws JposException;

    /**
     * Final part of setting VideoRate. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>videoRate is a positive value.</li>
     * </ul>
     *
     * @param videoRate New value for VideoRate property.
     * @throws JposException If an error occurs.
     */
    void videoRate(int videoRate) throws JposException;

    /**
     * Final part of StartSession method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapHostTriggered is true.</li>
     * </ul>
     *
     * @throws JposException    If an error occurs or in case of a timeout.
     */
    void startSession() throws JposException;

    /**
     * Final part of StopSession method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapHostTriggered is true.</li>
     * </ul>
     *
     * @throws JposException    If an error occurs or in case of a timeout.
     */
    void stopSession() throws JposException;
}
