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

import de.gmxhome.conrad.jpos.jpos_base.JposBaseDevice;
import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import jpos.CheckScannerConst;
import jpos.JposConst;
import jpos.JposException;

/**
 * Class containing the check scanner specific properties, their default values and default implementations of
 * CheckScannerInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Check Scanner.
 */
public class CheckScannerProperties extends JposCommonProperties implements CheckScannerInterface {
    /**
     * This property will be used internally to verify whether BeginInsertion and EndInsertion are valid operations.
     * It will be initialized to false during device enable.
     */
    public boolean InsertionMode = false;

    /**
     * This property will be used internally to verify whether BeginRemoval and EndRemoval are valid operations.
     * It will be initialized to false during device enable.
     */
    public boolean RemovalMode = false;

    /**
     * UPOS property CapAutoContrast. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public boolean CapAutoContrast = false;

    /**
     * UPOS property CapAutoGenerateFileID. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public boolean CapAutoGenerateFileID = false;

    /**
     * UPOS property CapAutoGenerateImageTagData. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public boolean CapAutoGenerateImageTagData = false;

    /**
     * UPOS property CapAutoSize. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public boolean CapAutoSize = false;

    /**
     * UPOS property CapColor. Default: CCL_MONO. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public int CapColor = CheckScannerConst.CHK_CCL_MONO;

    /**
     * UPOS property CapConcurrentMICR. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public boolean CapConcurrentMICR = false;

    /**
     * UPOS property CapContrast. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public boolean CapContrast = false;

    /**
     * UPOS property CapDefineCropArea. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public boolean CapDefineCropArea = false;

    /**
     * UPOS property CapImageFormat. Default: CIF_NATIVE. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public int CapImageFormat = CheckScannerConst.CHK_CIF_NATIVE;

    /**
     * UPOS property CapImageTagData. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public boolean CapImageTagData = false;

    /**
     * UPOS property CapMICRDevice. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public boolean CapMICRDevice = false;

    /**
     * UPOS property CapStoreImageFiles. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public boolean CapStoreImageFiles = false;

    /**
     * UPOS property CapValidationDevice. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public boolean CapValidationDevice = false;

    /**
     * UPOS property Color. Default: null. Must be overwritten by objects derived from
     *JposDevice within the changeDefaults or checkProperties method if CapColor specifies more than one color.
     */
    public Integer Color = null;

    /**
     * UPOS property ConcurrentMICR. Default: null. Must be overwritten by objects derived from JposDevice within
     * the changeDefaults or checkProperties method if CapMICRDevice and CapConcurrentMICR are true.
     */
    public Boolean ConcurrentMICR = null;

    /**
     * Default value of Contrast property. Default: null. Can be updated
     * before calling initOnEnable the first time if CapContrast is true and CapAutoContrast is false.
     */
    public Integer ContrastDef = null;

    /**
     * UPOS property Contrast. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public Integer Contrast;

    /**
     * UPOS property CropAreaCount. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public int CropAreaCount = 0;

    /**
     * Default for UPOS property DocumentHeight. Default: null. Must be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     * Keep in mind that the initial value must be given in 0.001 inch units by default. Will
     * also be used in ClearDataProperties method if CapAutoSize is true to restore DocumentHeight.
     */
    public Integer DocumentHeightDef = null;

    /**
     * UPOS property DocumentHeight.
     */
    public int DocumentHeight;

    /**
     * Default for UPOS property DocumentWidth. Default: null. Must be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     * Keep in mind that the initial value must be given in 0.001 inch units by default. Will
     * also be used in ClearDataProperties method if CapAutoSize is true to restore DocumentWidth.
     */
    public Integer DocumentWidthDef = null;

    /**
     * UPOS property DocumentWidth.
     */
    public int DocumentWidth;

    /**
     * UPOS property FileID. Default: An empty string.
     */
    public String FileID = "";

    /**
     * UPOS property FileIndex.
     */
    public int FileIndex;

    /**
     * UPOS property ImageData. Default: A zero-length byte array.
     */
    public byte[] ImageData = new byte[0];

    /**
     * UPOS property ImageFormat. Default: null. If CapImageFormat specifies more than one format, it must be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public Integer ImageFormat = null;

    /**
     * Default value of ImageMemoryStatus property. Default: IMS_OK. Should be updated
     * before calling initOnClaim.
     */
    public int ImageMemoryStatusDef = CheckScannerConst.CHK_IMS_OK;

    /**
     * UPOS property ImageMemoryStatus. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public int ImageMemoryStatus = 0;

    /**
     * UPOS property ImageTagData. Default: An empty string. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public String ImageTagData = "";

    /**
     * UPOS property MapMode. Default: MM_ENGLISH. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     * If overwritten, DocumentHeight and DocumentWidth must be given in the corresponding units.
     */
    public int MapMode = CheckScannerConst.CHK_MM_ENGLISH;

    /**
     * UPOS property MaxCropAreas. Default: null. Must be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public Integer MaxCropAreas = null;

    /**
     * UPOS property Quality. Default: null. Must be overwritten by objects derived from JposDevice within the
     * changeDefaults or checkProperties method if QualityList contains more than one resolution.
     */
    public Integer Quality = null;

    /**
     * UPOS property QualityList. Default: null. Must be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public String QualityList = null;

    /**
     * UPOS property RemainingImagesEstimate. Default: null. Must be overwritten
     * by objects derived from JposDevice within the changeDefaults or checkProperties method.
     */
    public Integer RemainingImagesEstimate = null;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected CheckScannerProperties(int dev) {
        super(dev);
        ExclusiveUse = ExclusiveYes;
    }

    @Override
    public void initOnOpen() {
        super.initOnOpen();
        FileIndex = 0;
    }

    @Override
    public void initOnClaim() {
        super.initOnClaim();
        ImageMemoryStatus = ImageMemoryStatusDef;
    }

    @Override
    public boolean initOnFirstEnable() {
        if (!super.initOnFirstEnable()) {
            Contrast = CapAutoContrast ? CheckScannerConst.CHK_AUTOMATIC_CONTRAST : (ContrastDef == null ? 50 : ContrastDef);
            if (MapMode != CheckScannerConst.CHK_MM_ENGLISH) {
                long[][] mappings = {
                        {CheckScannerConst.CHK_MM_METRIC, 2540},    // 2540 * 0.01 mm = 1 inch
                        {CheckScannerConst.CHK_MM_TWIPS, 1440},     // 1440 * 1 twips = 1 inch
                        {CheckScannerConst.CHK_MM_DOTS, Quality}
                };
                for (long[] mapping : mappings) {
                    if (MapMode == mapping[0]) {
                        DocumentWidth = (int)((DocumentWidth * 1000 + 500) / mapping[1]);
                        DocumentHeight = (int)((DocumentHeight * 1000 + 500) / mapping[1]);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void initOnEnable(boolean enable) {
        super.initOnEnable(enable);
        InsertionMode = RemovalMode = false;
    }

    @Override
    public void checkMandatoryProperties() throws JposException {
        int[][] colors = {
                {CheckScannerConst.CHK_CCL_MONO, CheckScannerConst.CHK_CL_MONO},
                {CheckScannerConst.CHK_CCL_GRAYSCALE, CheckScannerConst.CHK_CL_GRAYSCALE},
                {CheckScannerConst.CHK_CCL_16, CheckScannerConst.CHK_CL_16},
                {CheckScannerConst.CHK_CCL_256, CheckScannerConst.CHK_CL_256},
                {CheckScannerConst.CHK_CCL_FULL, CheckScannerConst.CHK_CL_FULL}

        };
        int[][] formats = {
                {CheckScannerConst.CHK_CIF_NATIVE, CheckScannerConst.CHK_IF_NATIVE},
                {CheckScannerConst.CHK_CIF_TIFF, CheckScannerConst.CHK_IF_TIFF},
                {CheckScannerConst.CHK_CIF_BMP, CheckScannerConst.CHK_IF_BMP},
                {CheckScannerConst.CHK_CIF_JPEG, CheckScannerConst.CHK_IF_JPEG},
                {CheckScannerConst.CHK_CIF_GIF, CheckScannerConst.CHK_IF_GIF}
        };
        if (Color == null) {
            for (int[] color : colors) {
                if (color[0] == CapColor) {
                    Color = color[1];
                    break;
                }
            }
            Device.check(Color == null, JposConst.JPOS_E_NOSERVICE, "Color property not specified");
        }
        if (ConcurrentMICR == null) {
            Device.check(CapConcurrentMICR && CapMICRDevice, JposConst.JPOS_E_NOSERVICE, "ConcurrentMICR property not specified");
            ConcurrentMICR = false;
        }
        Device.check(DocumentHeightDef == null, JposConst.JPOS_E_NOSERVICE, "DocumentHeight default not specified");
        Device.check(DocumentWidthDef == null, JposConst.JPOS_E_NOSERVICE, "DocumentWidth default not specified");
        DocumentWidth = DocumentWidthDef;
        DocumentHeight = DocumentHeightDef;
        if (ImageFormat == null) {
            for (int[] format : formats) {
                if (format[0] == CapImageFormat) {
                    ImageFormat = format[1];
                }
            }
            Device.check(ImageFormat == null, JposConst.JPOS_E_NOSERVICE, "ImageFormat property not specified");
        }
        if (MaxCropAreas == null) {
            Device.check(CapDefineCropArea, JposConst.JPOS_E_NOSERVICE, "MaxCropAreas property not specified");
            MaxCropAreas = 0;
        }
        try {
            long[] qualities = JposBaseDevice.stringArrayToLongArray(QualityList.split(","));
            if (Quality == null) {
                Device.check(qualities.length != 1, JposConst.JPOS_E_NOSERVICE, "Quality property not specified");
                Quality = (int) qualities[0];
            }
        } catch (NullPointerException e) {
            throw new JposException(JposConst.JPOS_E_NOSERVICE, "QualityList property not specified");
        } catch (NumberFormatException e) {
            throw new JposException(JposConst.JPOS_E_NOSERVICE, "QualityList property is invalid: " + QualityList);
        }
        if (RemainingImagesEstimate == null) {
            Device.check(CapStoreImageFiles, JposConst.JPOS_E_NOSERVICE, "RemainingImagesEstimate property not specified");
            RemainingImagesEstimate = 0;
        }
    }

    @Override
    public void color(int color) throws JposException {
        Color = color;
    }

    @Override
    public void concurrentMICR(boolean concurrentMICR) throws JposException {
        ConcurrentMICR = concurrentMICR;
    }

    @Override
    public void contrast(int contrast) throws JposException {
        Contrast = contrast;
    }

    @Override
    public void documentHeight(int documentHeight) throws JposException {
        DocumentHeight = documentHeight;
    }

    @Override
    public void documentWidth(int documentWidth) throws JposException {
        DocumentWidth = documentWidth;
    }

    @Override
    public void fileID(String fileID) throws JposException {
        FileID = fileID;
    }

    @Override
    public void fileIndex(int fileIndex) throws JposException {
        FileIndex = fileIndex;
    }

    @Override
    public void imageFormat(int imageFormat) throws JposException {
        ImageFormat = imageFormat;
    }

    @Override
    public void imageTagData(String imageTagData) throws JposException {
        ImageTagData = imageTagData;
    }

    /**
     * Returns array containing the MapMode values and the corresponding units per inch.
     * @return Array containing value pairs for MM_DOTS, MM_TWIPS, MM_ENGLISH and MM_METRIC.
     */
    int[][] getMM_Factors() {
        int[][] mm_Factors = {
                {CheckScannerConst.CHK_MM_DOTS, Quality},
                {CheckScannerConst.CHK_MM_TWIPS, 1440},
                {CheckScannerConst.CHK_MM_ENGLISH, 1000},
                {CheckScannerConst.CHK_MM_METRIC, 254},
        };
        return mm_Factors;
    }

    @Override
    public void mapMode(int mapMode) throws JposException {
        if (mapMode != MapMode) {
            long newfactor = 0;
            for (int[] fac : getMM_Factors()) {
                if (fac[0] == mapMode) {
                    newfactor = fac[1];
                    break;
                }
            }
            for (int[] fac : getMM_Factors()) {
                if (fac[0] == MapMode) {
                    DocumentWidth = (int)((DocumentWidth * newfactor + (fac[1] >> 1)) / fac[1]);
                    EventSource.logSet("DocumentWidth");
                    DocumentHeight = (int)((DocumentHeight * newfactor + (fac[1] >> 1)) / fac[1]);
                    EventSource.logSet("DocumentHeight");
                    break;
                }
            }
        }
        MapMode = mapMode;
    }

    @Override
    public void quality(int quality) throws JposException {
        if (MapMode == CheckScannerConst.CHK_MM_DOTS) {
            DocumentWidth = (int)((DocumentWidth * quality + (Quality >> 1)) / Quality);
            EventSource.logSet("DocumentWidth");
            DocumentHeight = (int)((DocumentHeight * quality + (Quality >> 1)) / Quality);
            EventSource.logSet("DocumentHeight");
        }
        Quality = quality;
    }

    @Override
    public void beginInsertion(int timeout) throws JposException {
    }

    @Override
    public void beginRemoval(int timeout) throws JposException {
    }

    @Override
    public void clearImage(int by) throws JposException {
    }

    @Override
    public void defineCropArea(int cropAreaID, int x, int y, int cx, int cy) throws JposException {
    }

    @Override
    public void endInsertion() throws JposException {
    }

    @Override
    public void endRemoval() throws JposException {
    }

    @Override
    public void retrieveImage(int cropAreaID) throws JposException {
    }

    @Override
    public void retrieveMemory(int by) throws JposException {
    }

    @Override
    public void storeImage(int cropAreaID) throws JposException {
    }

    @Override
    public void clearDataProperties() {
        super.clearDataProperties();
        if (ImageData.length > 0) {
            ImageData = new byte[0];
            EventSource.logSet("ImageData");
        }
        if (CapAutoGenerateFileID && !"".equals(FileID)) {
            FileID = "";
            EventSource.logSet("FileID");
        }
        if (CapAutoGenerateImageTagData && !"".equals(ImageTagData)) {
            ImageTagData = "";
            EventSource.logSet("ImageTagData");
        }
        if (CapAutoSize) {
            for (int[] fac : getMM_Factors()) {
                if (fac[0] == MapMode) {
                    int[] olddim = {DocumentWidth, DocumentHeight};
                    DocumentWidth = (DocumentWidthDef * fac[1] + (fac[1] >> 1)) / 1000;
                    if (DocumentWidth != olddim[0])
                        EventSource.logSet("DocumentWidth");
                    DocumentHeight = (DocumentHeightDef * fac[1] + (fac[1] >> 1)) / 1000;
                    if (DocumentHeight != olddim[1])
                        EventSource.logSet("DocumentHeight");
                    break;
                }
            }
        }
    }
}
