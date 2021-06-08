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

import de.gmxhome.conrad.jpos.jpos_base.JposBase;
import de.gmxhome.conrad.jpos.jpos_base.JposDataEvent;
import jpos.CheckScannerConst;

import java.util.Arrays;

/**
 * Data event implementation for CheckScanner devices.
 */
public class CheckScannerDataEvent extends JposDataEvent {
    /**
     * New contents of FileID property. Must be set if CapAutoGenerateFileID is true. For details, see UPOS specification.
     */
    public String FileID;

    /**
     * New contents of FileIndex property. For details, see UPOS specification.
     */
    public int FileIndex;

    /**
     * New contents of ImageData property. For details, see UPOS specification.
     */
    public byte[] ImageData;

    /**
     * New contents of ImageTagData property. Must be set if CapAutoGenerateImageTagData is true. For details, see UPOS specification.
     */
    public String ImageTagData;

    /**
     * New document height in dot units. See Quality for details.
     */
    public Integer DocumentHeight;

    /**
     * New document width in dot units. See Quality for details.
     */
    public Integer DocumentWidth;

    /**
     * Resolution of the scan image in DPI. For example, if Quality equals 320 and the scan image is 3 x 6 inch,
     * DocumentHeight must be equal to 960 and DocumentWidth equal to 1920.
     */
    public Integer Quality;

    /**
     * Constructor. Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     * @param fileIndex         Value to be set in property FileIndex before this DataEvent will be delivered.
     * @param imageData         Value to be set in property ImageData before this DataEvent will be delivered.
     * @param documentHeight    Document height in dots, used to compute property DocumentHeight before this DataEvent
     *                          will be delivered.
     * @param documentWidth     Document width in dots, used to compute property DocumentWidth before this DataEvent
     *                          will be delivered.
     * @param fileID            Value to be set in property FileID before this DataEvent will be delivered.
     * @param imageTagData      Value to be set in property ImageTagData before this DataEvent will be delivered.
     * @param quality           Value to be set in property Quality before this DataEvent will be delivered.
     */
    public CheckScannerDataEvent(JposBase source, int state, int fileIndex, byte[] imageData, Integer documentHeight,
                                 Integer documentWidth, String fileID, String imageTagData, Integer quality) {
        super(source, state);
        Quality = quality;
        FileID = fileID;
        ImageTagData = imageTagData;
        FileIndex = fileIndex;
        ImageData = Arrays.copyOf(imageData, imageData.length);
        DocumentHeight = documentHeight;
        DocumentWidth = documentWidth;
    }

    @Override
    public void setDataProperties() {
        super.setDataProperties();
        CheckScannerProperties props = (CheckScannerProperties) getPropertySet();
        if (!Arrays.equals(ImageData, props.ImageData)) {
            props.ImageData = Arrays.copyOf(ImageData, ImageData.length);
            props.EventSource.logSet("ImageData");
        }
        if (props.FileIndex != FileIndex) {
            props.FileIndex = FileIndex;
            props.EventSource.logSet("FileIndex");
        }
        if (props.CapAutoGenerateFileID && FileID != null && !FileID.equals(props.FileID)) {
            props.FileID = FileID;
            props.EventSource.logSet("FileID");
        }
        if (props.CapAutoGenerateImageTagData && ImageTagData != null && !ImageTagData.equals(props.ImageTagData)) {
            props.ImageTagData = ImageTagData;
            props.EventSource.logSet("ImageTagData");
        }
        if (props.CapAutoSize && Quality != null && DocumentHeight != null && DocumentWidth != null) {
            for (int[] pair : props.getMM_Factors()) {
                if (props.MapMode == pair[0]) {
                    props.DocumentHeight = (DocumentHeight * pair[1] + (pair[1] >> 1)) / Quality;
                    props.EventSource.logSet("DocumentHeight");
                    props.DocumentWidth = (DocumentWidth * pair[1] + (pair[1] >> 1)) / Quality;
                    props.EventSource.logSet("DocumentWidth");
                    break;
                }
            }
        }
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        ret += ", FileIndex: " + FileIndex;
        if (FileID != null && !FileID.equals(""))
            ret += ", FileID: \"" + FileID + "\"";
        ret += ", ImageData: [" + ImageData.length + " byte]";
        if (DocumentWidth != null && DocumentHeight != null && Quality != null)
            ret += ", size (dots): " + DocumentWidth + "x" + DocumentHeight;
        return ret;
    }
}
