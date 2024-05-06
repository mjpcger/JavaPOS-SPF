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

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.services.*;

import java.util.Arrays;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.CheckScannerConst.*;
import static jpos.JposConst.*;

/**
 * CheckScanner service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class CheckScannerService extends JposBase implements CheckScannerService116 {
    /**
     * Instance of a class implementing the CheckScannerInterface for check scanner specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public CheckScannerInterface CheckScanner;

    private final CheckScannerProperties Data;
    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public CheckScannerService(CheckScannerProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public boolean getCapAutoContrast() throws JposException {
        checkOpened();
        logGet("CapAutoContrast");
        return Data.CapAutoContrast;
    }

    @Override
    public boolean getCapAutoGenerateFileID() throws JposException {
        checkOpened();
        logGet("CapAutoGenerateFileID");
        return Data.CapAutoGenerateFileID;
    }

    @Override
    public boolean getCapAutoGenerateImageTagData() throws JposException {
        checkOpened();
        logGet("CapAutoGenerateImageTagData");
        return Data.CapAutoGenerateImageTagData;
    }

    @Override
    public boolean getCapAutoSize() throws JposException {
        checkOpened();
        logGet("CapAutoSize");
        return Data.CapAutoSize;
    }

    @Override
    public int getCapColor() throws JposException {
        checkOpened();
        logGet("CapColor");
        return Data.CapColor;
    }

    @Override
    public boolean getCapConcurrentMICR() throws JposException {
        checkOpened();
        logGet("CapConcurrentMICR");
        return Data.CapConcurrentMICR;
    }

    @Override
    public boolean getCapContrast() throws JposException {
        checkOpened();
        logGet("CapContrast");
        return Data.CapContrast;
    }

    @Override
    public boolean getCapDefineCropArea() throws JposException {
        checkOpened();
        logGet("CapDefineCropArea");
        return Data.CapDefineCropArea;
    }

    @Override
    public int getCapImageFormat() throws JposException {
        checkOpened();
        logGet("CapImageFormat");
        return Data.CapImageFormat;
    }

    @Override
    public boolean getCapImageTagData() throws JposException {
        checkOpened();
        logGet("CapImageTagData");
        return Data.CapImageTagData;
    }

    @Override
    public boolean getCapMICRDevice() throws JposException {
        checkOpened();
        logGet("CapMICRDevice");
        return Data.CapMICRDevice;
    }

    @Override
    public boolean getCapStoreImageFiles() throws JposException {
        checkOpened();
        logGet("CapStoreImageFiles");
        return Data.CapStoreImageFiles;
    }

    @Override
    public boolean getCapValidationDevice() throws JposException {
        checkOpened();
        logGet("CapValidationDevice");
        return Data.CapValidationDevice;
    }

    @Override
    public int getColor() throws JposException {
        checkOpened();
        logGet("Color");
        return Data.Color;
    }

    @Override
    public boolean getConcurrentMICR() throws JposException {
        checkOpened();
        logGet("ConcurrentMICR");
        return Data.ConcurrentMICR;
    }

    @Override
    public int getContrast() throws JposException {
        checkEnabled();
        check(Data.Contrast == null, JPOS_E_ILLEGAL, "Property Contrast invalid");
        logGet("Contrast");
        return Data.Contrast;
    }

    @Override
    public int getCropAreaCount() throws JposException {
        checkOpened();
        logGet("CropAreaCount");
        return Data.CropAreaCount;
    }

    @Override
    public int getDocumentHeight() throws JposException {
        checkOpened();
        logGet("DocumentHeight");
        return Data.DocumentHeight;
    }

    @Override
    public int getDocumentWidth() throws JposException {
        checkOpened();
        logGet("DocumentWidth");
        return Data.DocumentWidth;
    }

    @Override
    public String getFileID() throws JposException {
        checkOpened();
        logGet("FileID");
        return Data.FileID;
    }

    @Override
    public int getFileIndex() throws JposException {
        checkOpened();
        logGet("FileIndex");
        return Data.FileIndex;
    }

    @Override
    public byte[] getImageData() throws JposException {
        checkOpened();
        logGet("ImageData");
        return Arrays.copyOf(Data.ImageData, Data.ImageData.length);
    }

    @Override
    public int getImageFormat() throws JposException {
        checkOpened();
        logGet("ImageFormat");
        return Data.ImageFormat;
    }

    @Override
    public int getImageMemoryStatus() throws JposException {
        checkClaimed();
        logGet("ImageMemoryStatus");
        return Data.ImageMemoryStatus;
    }

    @Override
    public String getImageTagData() throws JposException {
        checkOpened();
        logGet("ImageTagData");
        return Data.ImageTagData;
    }

    @Override
    public int getMapMode() throws JposException {
        checkOpened();
        logGet("MapMode");
        return Data.MapMode;
    }

    @Override
    public int getMaxCropAreas() throws JposException {
        checkOpened();
        logGet("MaxCropAreas");
        return Data.MaxCropAreas;
    }

    @Override
    public int getQuality() throws JposException {
        checkOpened();
        logGet("Quality");
        return Data.Quality;
    }

    @Override
    public String getQualityList() throws JposException {
        checkOpened();
        logGet("QualityList");
        return Data.QualityList;
    }

    @Override
    public int getRemainingImagesEstimate() throws JposException {
        checkOpened();
        logGet("RemainingImagesEstimate");
        return Data.RemainingImagesEstimate;
    }

    @Override
    public void setColor(int color) throws JposException {
        int[][] colors = {
                {CHK_CL_MONO, CHK_CCL_MONO}, {CHK_CL_GRAYSCALE, CHK_CCL_GRAYSCALE},
                {CHK_CL_16, CHK_CCL_16}, {CHK_CL_256, CHK_CCL_256}, {CHK_CL_FULL, CHK_CCL_FULL}
        };
        logPreSet("Color");
        checkOpened();
        for (int[] colorrow : colors) {
            if (colorrow[0] == color) {
                check((Data.CapColor & colorrow[1]) == 0, JPOS_E_ILLEGAL, "Unsupported color value: " + color);
                CheckScanner.color(color);
                logSet("Color");
                return;
            }
        }
        throw new JposException(JPOS_E_ILLEGAL, "Invalid color value: " + color);
    }

    @Override
    public void setConcurrentMICR(boolean concurrentMICR) throws JposException {
        logPreSet("ConcurrentMICR");
        checkOpened();
        check(Data.CapMICRDevice && !Data.CapConcurrentMICR && concurrentMICR, JPOS_E_ILLEGAL, "Setting ConcurrentMICR invalid");
        checkNoChangedOrClaimed(Data.ConcurrentMICR, concurrentMICR);
        CheckScanner.concurrentMICR(concurrentMICR);
        logSet("ConcurrentMICR");
    }

    @Override
    public void setContrast(int contrast) throws JposException {
        logPreSet("Contrast");
        checkEnabled();
        if (contrast == CHK_AUTOMATIC_CONTRAST) {
            check(!Data.CapAutoContrast, JPOS_E_ILLEGAL, "Invalid Contrast value: " + contrast);
        }
        else {
            check(100 < contrast || contrast < 0, JPOS_E_ILLEGAL, "Contrast out of range: " + contrast);
        }
        CheckScanner.contrast(contrast);
        logSet("Contrast");
    }

    @Override
    public void setDocumentHeight(int documentHeight) throws JposException {
        logPreSet("DocumentHeight");
        checkOpened();
        check(documentHeight < 0, JPOS_E_ILLEGAL, "DocumentHeight must be a positive value");
        for (int[] pair : Data.getMM_Factors()) {
            if (Data.MapMode == pair[0]) {
                check((documentHeight * 1000 + (pair[1] >> 1)) / pair[1] > Data.DocumentHeightDef, JPOS_E_ILLEGAL, "Invalid height: " + documentHeight);
                break;
            }
        }
        checkNoChangedOrClaimed(Data.DocumentHeight, documentHeight);
        CheckScanner.documentHeight(documentHeight);
        logSet("DocumentHeight");
    }

    @Override
    public void setDocumentWidth(int documentWidth) throws JposException {
        logPreSet("DocumentWidth");
        checkOpened();
        check(documentWidth < 0, JPOS_E_ILLEGAL, "DocumentWidth must be a positive value");
        for (int[] pair : Data.getMM_Factors()) {
            if (Data.MapMode == pair[0]) {
                check((documentWidth * 1000 + (pair[1] >> 1)) / pair[1] > Data.DocumentWidthDef, JPOS_E_ILLEGAL, "Invalid width: " + documentWidth);
                break;
            }
        }
        checkNoChangedOrClaimed(Data.DocumentWidth, documentWidth);
        CheckScanner.documentWidth(documentWidth);
        logSet("DocumentWidth");
    }

    @Override
    public void setFileID(String fileID) throws JposException {
        if (fileID == null)
            fileID = "";
        logPreSet("FileID");
        checkOpened();
        checkNoChangedOrClaimed(Data.FileID, fileID);
        CheckScanner.fileID(fileID);
        logSet("FileID");
    }

    @Override
    public void setFileIndex(int fileIndex) throws JposException {
        logPreSet("FileIndex");
        checkOpened();
        checkNoChangedOrClaimed(Data.FileIndex, fileIndex);
        CheckScanner.fileIndex(fileIndex);
        logSet("FileIndex");
    }

    @Override
    public void setImageFormat(int imageFormat) throws JposException {
        int[][] formats = {
                {CHK_IF_NATIVE, CHK_CIF_NATIVE}, {CHK_IF_TIFF, CHK_CIF_TIFF}, {CHK_IF_BMP, CHK_CIF_BMP},
                {CHK_IF_JPEG, CHK_CIF_JPEG}, {CHK_IF_GIF, CHK_CIF_GIF}
        };
        logPreSet("ImageFormat");
        checkOpened();
        for (int[] format : formats) {
            if (imageFormat == format[0]) {
                check((Data.CapImageFormat & format[1]) == 0, JPOS_E_ILLEGAL, "Unsupported ImageFormat value: " + imageFormat);
                checkNoChangedOrClaimed(Data.ImageFormat, imageFormat);
                CheckScanner.imageFormat(imageFormat);
                logSet("ImageFormat");
                return;
            }
        }
        throw new JposException(JPOS_E_ILLEGAL, "Invalid ImageFormat property value: " + imageFormat);
    }

    @Override
    public void setImageTagData(String imageTagData) throws JposException {
        if (imageTagData == null)
            imageTagData = "";
        logPreSet("ImageTagData");
        checkOpened();
        checkNoChangedOrClaimed(Data.ImageTagData, imageTagData);
        CheckScanner.imageTagData(imageTagData);
        logSet("ImageTagData");
    }

    @Override
    public void setMapMode(int mapMode) throws JposException {
        long[] modes = { CHK_MM_DOTS, CHK_MM_TWIPS, CHK_MM_ENGLISH, CHK_MM_METRIC };
        logPreSet("MapMode");
        checkOpened();
        checkMember(mapMode, modes, JPOS_E_ILLEGAL, "Illegal MapMode value: " + mapMode);
        checkNoChangedOrClaimed(Data.MapMode, mapMode);
        CheckScanner.mapMode(mapMode);
        logSet("MapMode");
    }

    @Override
    public void setQuality(int quality) throws JposException {
        logPreSet("Quality");
        checkOpened();
        checkMember(quality, stringArrayToLongArray(Data.QualityList.split(",")), JPOS_E_ILLEGAL, "Invalid Quality value: " + quality);
        checkNoChangedOrClaimed(Data.Quality, quality);
        CheckScanner.quality(quality);
        logSet("Quality");
    }

    @Override
    public void beginInsertion(int timeout) throws JposException {
        logPreCall("BeginInsertion", removeOuterArraySpecifier(new Object[]{timeout}, Device.MaxArrayStringElements));
        checkEnabled();
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout value: " + timeout);
        CheckScanner.beginInsertion(timeout);
        Data.InsertionMode = true;
        logCall("BeginInsertion");
    }

    @Override
    public void beginRemoval(int timeout) throws JposException {
        logPreCall("BeginRemoval", removeOuterArraySpecifier(new Object[]{timeout}, Device.MaxArrayStringElements));
        checkEnabled();
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout value: " + timeout);
        CheckScanner.beginRemoval(timeout);
        Data.RemovalMode = true;
        logCall("BeginRemoval");
    }

    @Override
    public void clearImage(int by) throws JposException {
        long[] validby = { CHK_CLR_ALL, CHK_CLR_BY_FILEID, CHK_CLR_BY_FILEINDEX, CHK_CLR_BY_IMAGETAGDATA };
        logPreCall("ClearImage", removeOuterArraySpecifier(new Object[]{by}, Device.MaxArrayStringElements));
        checkEnabled();
        checkMember(by, validby, JPOS_E_ILLEGAL, "Invalid by value: " + by);
        CheckScanner.clearImage(by);
        logCall("ClearImage");
    }

    @Override
    public void defineCropArea(int cropAreaID, int x, int y, int cx, int cy) throws JposException {
        logPreCall("DefineCropArea", removeOuterArraySpecifier(new Object[]{cropAreaID, x, y, cx, cy}, Device.MaxArrayStringElements));
        checkEnabled();
        if (cropAreaID != CHK_CROP_AREA_RESET_ALL) {
            check(!Data.CapDefineCropArea, JPOS_E_ILLEGAL, "Crop areas not supported");
            check(x < 0 || x >= Data.DocumentWidth, JPOS_E_ILLEGAL, "X coordinate invalid: " + x);
            check(y < 0 || y >= Data.DocumentHeight, JPOS_E_ILLEGAL, "Y coordinate invalid: " + y);
            check(cx < 0 && cx != CHK_CROP_AREA_RIGHT, JPOS_E_ILLEGAL, "CX negative");
            check(cy < 0 && cy != CHK_CROP_AREA_BOTTOM, JPOS_E_ILLEGAL, "CY negative");
            if (x + cx > Data.DocumentWidth || cx == CHK_CROP_AREA_RIGHT)
                cx = Data.DocumentWidth - x;
            if (y + cy > Data.DocumentHeight || cy == CHK_CROP_AREA_BOTTOM)
                cy = Data.DocumentHeight - y;
        }
        CheckScanner.defineCropArea(cropAreaID, x, y, cx, cy);
        logCall("DefineCropArea");
    }

    @Override
    public void endInsertion() throws JposException {
        logPreCall("EndInsertion");
        checkEnabled();
        check(!Data.InsertionMode, JPOS_E_ILLEGAL, "Not in insertion mode");
        Data.InsertionMode = false;
        CheckScanner.endInsertion();
        logCall("EndInsertion");
    }

    @Override
    public void endRemoval() throws JposException {
        logPreCall("EndRemoval");
        checkEnabled();
        check(!Data.RemovalMode, JPOS_E_ILLEGAL, "Not in removal mode");
        Data.RemovalMode = false;
        CheckScanner.endRemoval();
        logCall("EndRemoval");
    }

    @Override
    public void retrieveImage(int cropAreaID) throws JposException {
        logPreCall("RetrieveImage", removeOuterArraySpecifier(new Object[]{cropAreaID}, Device.MaxArrayStringElements));
        checkEnabled();
        check(Data.InsertionMode || Data.RemovalMode, JPOS_E_FAILURE, "Bad device state (" + (Data.InsertionMode ? "insertion" : "removal") + " mode)");
        check(!Data.CapDefineCropArea && cropAreaID != CHK_CROP_AREA_ENTIRE_IMAGE, JPOS_E_ILLEGAL, "Invalid Crop area: " + cropAreaID);
        CheckScanner.retrieveImage(cropAreaID);
        logCall("RetrieveImage");
    }

    @Override
    public void retrieveMemory(int by) throws JposException {
        logPreCall("RetrieveMemory", removeOuterArraySpecifier(new Object[]{by}, Device.MaxArrayStringElements));
        Object[][] allowedValue = {
                { CHK_LOCATE_BY_FILEID, Data.FileID != null && !Data.FileID.equals(""), "Missing FileID" },
                { CHK_LOCATE_BY_FILEINDEX, true },
                {
                        CHK_LOCATE_BY_IMAGETAGDATA,
                        Data.CapImageTagData && Data.ImageTagData != null && !Data.ImageTagData.equals(""),
                        (Data.CapImageTagData ? "No ImageTagData support" : "Missing ImageTagData")
                }
        };
        checkEnabled();
        check(!Data.CapStoreImageFiles, JPOS_E_ILLEGAL, "No image file available");
        for (Object[] pair : allowedValue) {
            if (by == (Integer) pair[0]) {
                if(!(Boolean)pair[1])
                    throw new JposException(JPOS_E_ILLEGAL, pair[2].toString());
                CheckScanner.retrieveMemory(by);
                logCall("RetrieveMemory");
                return;
            }
        }
        throw new JposException(JPOS_E_ILLEGAL, "Invalid by: " + by);
    }

    @Override
    public void storeImage(int cropAreaID) throws JposException {
        logPreCall("StoreImage", removeOuterArraySpecifier(new Object[]{cropAreaID}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapStoreImageFiles, JPOS_E_ILLEGAL, "StoreImage not supported");
        check(!Data.CapDefineCropArea && cropAreaID != CHK_CROP_AREA_ENTIRE_IMAGE, JPOS_E_ILLEGAL, "Invalid Crop area: " + cropAreaID);
        CheckScanner.storeImage(cropAreaID);
        logCall("StoreImage");
    }
}
