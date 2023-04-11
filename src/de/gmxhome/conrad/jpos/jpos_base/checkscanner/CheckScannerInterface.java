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

package de.gmxhome.conrad.jpos.jpos_base.checkscanner;

import de.gmxhome.conrad.jpos.jpos_base.JposBaseInterface;
import jpos.JposException;

/**
 * Interface for methods that implement property setter and method calls for the CheckScanner device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Check Scanner.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface CheckScannerInterface extends JposBaseInterface {
    /**
     * Checks whether mandatory properties can be set to plausible values due to specific capability values. If so,
     * these properties will be set to the corresponding values. Otherwise, these properties must be initialized
     * either in the Device's changeDefaults or checkProperties method.<br>
     * <b>A call of this method in a CheckScanner device factory after the call of the Device's checkProperties method
     * is mandatory</b>.
     * @throws JposException If one of the mandatory properties has not been set previously.
     */
    public void checkMandatoryProperties() throws JposException;

    /**
     * Final part of setting Color. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>color is one of CL_MONO, CL_GRAYSCALE, CL_16, CL_256 or CL_FULL,</li>
     *     <li>the corresponding bit in CapColor (CCL_MONO, ...) is set.</li>
     * </ul>
     *
     * @param color New value for Color property.
     * @throws JposException If an error occurs.
     */
    public void color(int color) throws JposException;

    /**
     * Final part of setting ConcurrentMICR. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>CapMICRDevice is false or CapConcurrentMICR is true or concurrentMICR is false,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or concurrentMICR equals the previous value of ConcurrentMICR.</li>
     * </ul>
     *
     * @param concurrentMICR New value for ConcurrentMICR property.
     * @throws JposException If an error occurs.
     */
    public void concurrentMICR(boolean concurrentMICR) throws JposException;

    /**
     * Final part of setting Contrast. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>0 &le; contrast %le; 100 or contrast = AUTOMATIC_CONTRAST and CapAutoContrast = true.</li>
     * </ul>
     *
     * @param contrast New value for Contrast property.
     * @throws JposException If an error occurs.
     */
    public void contrast(int contrast) throws JposException;

    /**
     * Final part of setting DocumentHeight. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>documentHeight is not negative,</li>
     *     <li>documentHeight in combination with MapMode does not specify a height above the initial DocumentHeight
     *     value,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or documentHeight equals the previous value of DocumentHeight.</li>
     * </ul>
     *
     * @param documentHeight New value for DocumentHeight property.
     * @throws JposException If an error occurs.
     */
    public void documentHeight(int documentHeight) throws JposException;

    /**
     * Final part of setting DocumentWidth. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>documentWidth is not negative,</li>
     *     <li>documentWidth in combination with MapMode does not specify a width above the initial DocumentWidth
     *     value,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or documentWidth equals the previous value of DocumentWidth.</li>
     * </ul>
     *
     * @param documentWidth New value for DocumentWidth property.
     * @throws JposException If an error occurs.
     */
    public void documentWidth(int documentWidth) throws JposException;

    /**
     * Final part of setting FileID. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>fileID is not null,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or fileID equals the previous value of FileID.</li>
     * </ul>
     *
     * @param fileID New value for FileID property.
     * @throws JposException If an error occurs.
     */
    public void fileID(String fileID) throws JposException;

    /**
     * Final part of setting FileIndex. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or fileIndex equals the previous value of FileIndex.</li>
     * </ul>
     *
     * @param fileIndex New value for FileIndex property.
     * @throws JposException If an error occurs.
     */
    public void fileIndex(int fileIndex) throws JposException;

    /**
     * Final part of setting ImageFormat. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>imageFormat is one of IF_NATIVE, IF_TIFF, IF_BMP, IF_JPEG, or IF_GIF,</li>
     *     <li>the corresponding bit in CapImageFormat (CIF_NATIVE, ...) is set,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or imageFormat equals the previous value of ImageFormat.</li>
     * </ul>
     *
     * @param imageFormat New value for ImageFormat property.
     * @throws JposException If an error occurs.
     */
    public void imageFormat(int imageFormat) throws JposException;

    /**
     * Final part of setting ImageTagData. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>imageTagData is not null,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or imageTagData equals the previous value of ImageTagData.</li>
     * </ul>
     *
     * @param imageTagData New value for ImageTagData property.
     * @throws JposException If an error occurs.
     */
    public void imageTagData(String imageTagData) throws JposException;

    /**
     * Final part of setting MapMode. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>mapMode is one of MM_DOTS, MM_TWIPS, MM_ENGLISH or MM_METRIC,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or mapMode equals the previous value of MapMode.</li>
     * </ul>
     *
     * @param mapMode New value for MapMode property.
     * @throws JposException If an error occurs.
     */
    public void mapMode(int mapMode) throws JposException;

    /**
     * Final part of setting Quality. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>quality is one of the values stored in the QualityList property,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or quality equals the previous value of Quality.</li>
     * </ul>
     *
     * @param quality New value for Quality property.
     * @throws JposException If an error occurs.
     */
    public void quality(int quality) throws JposException;

    /**
     * Final part of BeginInsertion method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>timeout is positive or FOREVER.</li>
     * </ul>
     * If the service enters insertion mode successfully after BeginInsertion failed, e.g. due to a
     * timeout condition during delayed asynchronous insertion handling, property InsertionMode must
     * be set to true by the specific service implementation to avoid EndInsertion to fail.
     *
     * @param timeout   Maximum time in milliseconds BeginInsertion delays execution.
     * @throws JposException    If an error occurs or in case of a timeout.
     */
    public void beginInsertion(int timeout) throws JposException;

    /**
     * Final part of BeginRemoval method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>timeout is positive or FOREVER.</li>
     * </ul>
     * If the service enters removal mode successfully after BeginRemoval failed, e.g. due to a
     * timeout condition during delayed asynchronous removal handling, property RemovalMode must
     * be set to true by the specific service implementation to avoid EndRemoval to fail.
     *
     * @param timeout   Maximum time in milliseconds BeginRemoval delays execution.
     * @throws JposException    If an error occurs or in case of a timeout.
     */
    public void beginRemoval(int timeout) throws JposException;

    /**
     * Final part of ClearImage method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>by is one of CLR_ALL, CLR_BY_FILEID, CLR_BY_FILEINDEX or CLR_BY_IMAGETAGDATA.</li>
     * </ul>
     *
     * @param by    One of CLR_ALL, CLR_BY_FILEID, CLR_BY_FILEINDEX or CLR_BY_IMAGETAGDATA, specifies which properties
     *              will be used to access the device's image storage.
     * @throws JposException    If an error occurs.
     */
    public void clearImage(int by) throws JposException;

    /**
     * Final part of DefineCropArea method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>cropAreaID equals CROP_AREA_RESET_ALL or <ul>
     *         <li>CapDefineCropArea is true,</li>
     *         <li>x between 0 and DocumentWidth,</li>
     *         <li>y between 0 and DocumentHeight,</li>
     *         <li>cx between 0 and DocumentWidth - x and</li>
     *         <li>cy between 0 and DocumentHeight - y.</li>
     *     </ul>Remark: If cx equals CROP_AREA_RIGHT or cy equals CROP_AREA_BOTTOM, the service computes the
     *     corresponding real cx or cy value from the corresponding image dimensions using the current MapMode.</li>
     * </ul>
     *
     * @param cropAreaID    Crop area identifier to be used.
     * @param x             Starting x coordinate of the cropping area.
     * @param y             Starting y coordinate of the cropping area.
     * @param cx            Value to be added to x coordinate to compute the ending x coordinate of the cropping area.
     * @param cy            Value to be added to y coordinate to compute the ending y coordinate of the cropping area.
     * @throws JposException  If an error occurs.
     */
    public void defineCropArea(int cropAreaID, int x, int y, int cx, int cy) throws JposException;

    /**
     * Final part of EndInsertion method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Device entered insertion mode previously.</li>
     * </ul>
     *
     * @throws JposException    If an error occurs.
     */
    public void endInsertion() throws JposException;

    /**
     * Final part of EndRemoval method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Device entered removal mode previously.</li>
     * </ul>
     *
     * @throws JposException    If an error occurs.
     */
    public void endRemoval() throws JposException;

    /**
     * Final part of RetrieveImage method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>cropAreaID  equals CROP_AREA_ENTIRE_IMAGE or CapDefineCropArea is true,</li>
     *     <li>Device is neither in insertion nor in removal mode (no outstanding EndInsertion or EndRemoval).</li>
     * </ul>
     *
     * @param cropAreaID    Crop area ID as specified in DefineCropArea or CROP_AREA_ENTIRE_IMAGE.
     * @throws JposException    If an error occurs.
     */
    public void retrieveImage(int cropAreaID) throws JposException;

    /**
     * Final part of RetrieveMemory method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapStoreImageFiles is true,</li>
     *     <li>by equals LOCATE_BY_FILEID and FileID is a non-empty string or</li>
     *     <li>by equals LOCATE_BY_FILEINDEX or</li>
     *     <li>by equals LOCATE_BY_IMAGETAGDATA, CapImageTagData is true and ImageTagData is a non-empty string.</li>
     * </ul>
     *
     * @param by    One of LOCATE_BY_FILEID, LOCATE_BY_FILEINDEX or LOCATE_BY_IMAGETAGDATA.
     * @throws JposException    If an error occurs.
     */
    public void retrieveMemory(int by) throws JposException;

    /**
     * Final part of StoreImage method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapStoreImageFiles is true,</li>
     *     <li>CapDefineCropArea is true or cropAreaID equals CROP_AREA_ENTIRE_IMAGE.</li>
     * </ul>
     *
     * @param cropAreaID    Crop area ID as specified in DefineCropArea or CROP_AREA_ENTIRE_IMAGE.
     * @throws JposException If an error occurs.
     */
    public void storeImage(int cropAreaID) throws JposException;
}
