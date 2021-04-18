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
import jpos.CheckScannerConst;
import jpos.JposConst;
import jpos.JposException;
import jpos.services.CheckScannerService114;

import java.util.Arrays;

/**
 * CheckScanner service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class CheckScannerService extends JposBase implements CheckScannerService114 {
    /**
     * Instance of a class implementing the CheckScannerInterface for check scanner specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public CheckScannerInterface CheckScanner;

    private CheckScannerProperties Data;
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
        Device.check(Data.Contrast == null, JposConst.JPOS_E_ILLEGAL, "Property Contrast invalid");
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
                {CheckScannerConst.CHK_CL_MONO, CheckScannerConst.CHK_CCL_MONO},
                {CheckScannerConst.CHK_CL_GRAYSCALE, CheckScannerConst.CHK_CCL_GRAYSCALE},
                {CheckScannerConst.CHK_CL_16, CheckScannerConst.CHK_CCL_16},
                {CheckScannerConst.CHK_CL_256, CheckScannerConst.CHK_CCL_256},
                {CheckScannerConst.CHK_CL_FULL, CheckScannerConst.CHK_CCL_FULL}
        };
        logPreSet("Color");
        checkOpened();
        for (int[] colorrow : colors) {
            if (colorrow[0] == color) {
                Device.check((Data.CapColor & colorrow[1]) == 0, JposConst.JPOS_E_ILLEGAL, "Unsupported color value: " + color);
                CheckScanner.color(color);
                logSet("Color");
                return;
            }
        }
        throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid color value: " + color);
    }

    @Override
    public void setConcurrentMICR(boolean concurrentMICR) throws JposException {
        logPreSet("ConcurrentMICR");
        checkOpened();
        Device.check(Data.CapMICRDevice && !Data.CapConcurrentMICR && concurrentMICR, JposConst.JPOS_E_ILLEGAL, "Setting ConcurrentMICR invalid");
        CheckScanner.concurrentMICR(concurrentMICR);
        logSet("ConcurrentMICR");
    }

    @Override
    public void setContrast(int contrast) throws JposException {
        logPreSet("Contrast");
        checkEnabled();
        if (contrast == CheckScannerConst.CHK_AUTOMATIC_CONTRAST) {
            Device.check(!Data.CapAutoContrast, JposConst.JPOS_E_ILLEGAL, "Invalid Contrast value: " + contrast);
        }
        else {
            Device.check(100 < contrast || contrast < 0, JposConst.JPOS_E_ILLEGAL, "Contrast out of range: " + contrast);
        }
        CheckScanner.contrast(contrast);
        logSet("Contrast");
    }

    @Override
    public void setDocumentHeight(int documentHeight) throws JposException {
        logPreSet("DocumentHeight");
        checkOpened();
        Device.check(documentHeight < 0, JposConst.JPOS_E_ILLEGAL, "DocumentHeight must be a positive value");
        CheckScanner.documentHeight(documentHeight);
        logSet("DocumentHeight");
    }

    @Override
    public void setDocumentWidth(int documentWidth) throws JposException {
        logPreSet("DocumentWidth");
        checkOpened();
        Device.check(documentWidth < 0, JposConst.JPOS_E_ILLEGAL, "DocumentWidth must be a positive value");
        CheckScanner.documentWidth(documentWidth);
        logSet("DocumentWidth");
    }

    @Override
    public void setFileID(String fileID) throws JposException {
        if (fileID == null)
            fileID = "";
        logPreSet("FileID");
        checkOpened();
        CheckScanner.fileID(fileID);
        logSet("FileID");
    }

    @Override
    public void setFileIndex(int fileIndex) throws JposException {
        logPreSet("FileIndex");
        checkOpened();
        CheckScanner.fileIndex(fileIndex);
        logSet("FileIndex");
    }

    @Override
    public void setImageFormat(int imageFormat) throws JposException {
        int[][] formats = {
                {CheckScannerConst.CHK_IF_NATIVE, CheckScannerConst.CHK_CIF_NATIVE},
                {CheckScannerConst.CHK_IF_TIFF, CheckScannerConst.CHK_CIF_TIFF},
                {CheckScannerConst.CHK_IF_BMP, CheckScannerConst.CHK_CIF_BMP},
                {CheckScannerConst.CHK_IF_JPEG, CheckScannerConst.CHK_CIF_JPEG},
                {CheckScannerConst.CHK_IF_GIF, CheckScannerConst.CHK_CIF_GIF}
        };
        logPreSet("ImageFormat");
        checkOpened();
        for (int[] format : formats) {
            if (imageFormat == format[0]) {
                Device.check((Data.CapImageFormat & format[1]) == 0, JposConst.JPOS_E_ILLEGAL, "Unsupported ImageFormat value: " + imageFormat);
                CheckScanner.imageFormat(imageFormat);
                logSet("ImageFormat");
                return;
            }
        }
        throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid ImageFormat property value: " + imageFormat);
    }

    @Override
    public void setImageTagData(String imageTagData) throws JposException {
        if (imageTagData == null)
            imageTagData = "";
        logPreSet("ImageTagData");
        checkOpened();
        CheckScanner.imageTagData(imageTagData);
        logSet("ImageTagData");
    }

    @Override
    public void setMapMode(int mapMode) throws JposException {
        long[] modes = {
                CheckScannerConst.CHK_MM_DOTS,
                CheckScannerConst.CHK_MM_TWIPS,
                CheckScannerConst.CHK_MM_ENGLISH,
                CheckScannerConst.CHK_MM_METRIC
        };
        logPreSet("MapMode");
        checkOpened();
        Device.checkMember(mapMode, modes, JposConst.JPOS_E_ILLEGAL, "Illegal MapMode value: " + mapMode);
        CheckScanner.mapMode(mapMode);
        logSet("MapMode");
    }

    @Override
    public void setQuality(int quality) throws JposException {
        logPreSet("Quality");
        checkOpened();
        Device.checkMember(quality, Device.stringArrayToLongArray(Data.QualityList.split(",")), JposConst.JPOS_E_ILLEGAL, "Invalid Quality value: " + quality);
        CheckScanner.quality(quality);
        logSet("Quality");
    }

    @Override
    public void beginInsertion(int timeout) throws JposException {
        logPreCall("BeginInsertion", "" + timeout);
        checkEnabled();
        JposDevice.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout value: " + timeout);
        CheckScanner.beginInsertion(timeout);
        logCall("BeginInsertion");
    }

    @Override
    public void beginRemoval(int timeout) throws JposException {
        logPreCall("BeginRemoval", "" + timeout);
        checkEnabled();
        Device.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout value: " + timeout);
        CheckScanner.beginRemoval(timeout);
        logCall("BeginRemoval");
    }

    @Override
    public void clearImage(int by) throws JposException {
        long[] validby = {
                CheckScannerConst.CHK_CLR_ALL,
                CheckScannerConst.CHK_CLR_BY_FILEID,
                CheckScannerConst.CHK_CLR_BY_FILEINDEX,
                CheckScannerConst.CHK_CLR_BY_IMAGETAGDATA
        };
        logPreCall("ClearImage", "" + by);
        checkEnabled();
        JposDevice.checkMember(by, validby, JposConst.JPOS_E_ILLEGAL, "Invalid by value: " + by);
        CheckScanner.clearImage(by);
        logCall("ClearImage");
    }

    @Override
    public void defineCropArea(int cropAreaID, int x, int y, int cx, int cy) throws JposException {
        logPreCall("DefineCropArea", "" + x + ", " + y + ", " + cx + ", " + cy);
        checkEnabled();
        if (cropAreaID != CheckScannerConst.CHK_CROP_AREA_RESET_ALL) {
            JposDevice.check(!Data.CapDefineCropArea, JposConst.JPOS_E_ILLEGAL, "Crop areas not supported");
            JposDevice.check(x < 0 || x >= Data.DocumentWidth, JposConst.JPOS_E_ILLEGAL, "X coordinate invalid: " + x);
            JposDevice.check(y < 0 || y >= Data.DocumentHeight, JposConst.JPOS_E_ILLEGAL, "Y coordinate invalid: " + y);
            JposDevice.check(cx < 0 && cx != CheckScannerConst.CHK_CROP_AREA_RIGHT, JposConst.JPOS_E_ILLEGAL, "CX negative");
            JposDevice.check(cy < 0 && cy != CheckScannerConst.CHK_CROP_AREA_BOTTOM, JposConst.JPOS_E_ILLEGAL, "CY negative");
            if (x + cx > Data.DocumentWidth || cx == CheckScannerConst.CHK_CROP_AREA_RIGHT)
                cx = Data.DocumentWidth - x;
            if (y + cy > Data.DocumentHeight || cy == CheckScannerConst.CHK_CROP_AREA_BOTTOM)
                cy = Data.DocumentHeight - y;
        }
        CheckScanner.defineCropArea(cropAreaID, x, y, cx, cy);
        logCall("DefineCropArea");
    }

    @Override
    public void endInsertion() throws JposException {
        logPreCall("EndInsertion");
        checkEnabled();
        CheckScanner.endInsertion();
        logCall("EndInsertion");
    }

    @Override
    public void endRemoval() throws JposException {
        logPreCall("EndRemoval");
        checkEnabled();
        CheckScanner.endRemoval();
        logCall("EndRemoval");
    }

    @Override
    public void retrieveImage(int cropAreaID) throws JposException {
        logPreCall("RetrieveImage", "" + cropAreaID);
        checkEnabled();
        JposDevice.check(!Data.CapDefineCropArea && cropAreaID != CheckScannerConst.CHK_CROP_AREA_ENTIRE_IMAGE, JposConst.JPOS_E_ILLEGAL, "Invalid Crop area: " + cropAreaID);
        CheckScanner.retrieveImage(cropAreaID);
        logCall("RetrieveImage");
    }

    @Override
    public void retrieveMemory(int by) throws JposException {
        logPreCall("RetrieveMemory", "" + by);
        Object[][] allowedValue = {
                {
                        CheckScannerConst.CHK_LOCATE_BY_FILEID,
                        Data.FileID != null && !Data.FileID.equals(""),
                        "Missing FileID"
                },
                {
                        CheckScannerConst.CHK_LOCATE_BY_FILEINDEX,
                        true},
                {
                        CheckScannerConst.CHK_LOCATE_BY_IMAGETAGDATA,
                        Data.CapImageTagData && Data.ImageTagData != null && !Data.ImageTagData.equals(""),
                        (Data.CapImageTagData ? "No ImageTagData support" : "Missing ImageTagData")}
        };
        checkEnabled();
        for (Object[] pair : allowedValue) {
            if (by == (Integer) pair[0]) {
                if(!(Boolean)pair[1])
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, pair[2].toString());
                CheckScanner.retrieveMemory(by);
                logCall("RetrieveMemory");
                return;
            }
        }
        throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid by: " + by);
    }

    @Override
    public void storeImage(int cropAreaID) throws JposException {
        logPreCall("StoreImage", "" + cropAreaID);
        checkEnabled();
        JposDevice.check(Data.CapStoreImageFiles, JposConst.JPOS_E_ILLEGAL, "StoreImage not supported");
        JposDevice.check(!Data.CapDefineCropArea && cropAreaID != CheckScannerConst.CHK_CROP_AREA_ENTIRE_IMAGE, JposConst.JPOS_E_ILLEGAL, "Invalid Crop area: " + cropAreaID);
        CheckScanner.storeImage(cropAreaID);
        logCall("StoreImage");
    }
}
