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
import jpos.JposException;
import jpos.services.CheckScannerService114;

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
        return false;
    }

    @Override
    public boolean getCapContrast() throws JposException {
        return false;
    }

    @Override
    public int getContrast() throws JposException {
        return 0;
    }

    @Override
    public void setContrast(int i) throws JposException {

    }

    @Override
    public boolean getCapAutoGenerateFileID() throws JposException {
        return false;
    }

    @Override
    public boolean getCapAutoGenerateImageTagData() throws JposException {
        return false;
    }

    @Override
    public boolean getCapAutoSize() throws JposException {
        return false;
    }

    @Override
    public int getCapColor() throws JposException {
        return 0;
    }

    @Override
    public boolean getCapConcurrentMICR() throws JposException {
        return false;
    }

    @Override
    public boolean getCapDefineCropArea() throws JposException {
        return false;
    }

    @Override
    public int getCapImageFormat() throws JposException {
        return 0;
    }

    @Override
    public boolean getCapImageTagData() throws JposException {
        return false;
    }

    @Override
    public boolean getCapMICRDevice() throws JposException {
        return false;
    }

    @Override
    public boolean getCapStoreImageFiles() throws JposException {
        return false;
    }

    @Override
    public boolean getCapValidationDevice() throws JposException {
        return false;
    }

    @Override
    public int getColor() throws JposException {
        return 0;
    }

    @Override
    public void setColor(int i) throws JposException {

    }

    @Override
    public boolean getConcurrentMICR() throws JposException {
        return false;
    }

    @Override
    public void setConcurrentMICR(boolean b) throws JposException {

    }

    @Override
    public int getCropAreaCount() throws JposException {
        return 0;
    }

    @Override
    public int getDocumentHeight() throws JposException {
        return 0;
    }

    @Override
    public void setDocumentHeight(int i) throws JposException {

    }

    @Override
    public int getDocumentWidth() throws JposException {
        return 0;
    }

    @Override
    public void setDocumentWidth(int i) throws JposException {

    }

    @Override
    public String getFileID() throws JposException {
        return null;
    }

    @Override
    public void setFileID(String s) throws JposException {

    }

    @Override
    public int getFileIndex() throws JposException {
        return 0;
    }

    @Override
    public void setFileIndex(int i) throws JposException {

    }

    @Override
    public byte[] getImageData() throws JposException {
        return new byte[0];
    }

    @Override
    public int getImageFormat() throws JposException {
        return 0;
    }

    @Override
    public void setImageFormat(int i) throws JposException {

    }

    @Override
    public int getImageMemoryStatus() throws JposException {
        return 0;
    }

    @Override
    public String getImageTagData() throws JposException {
        return null;
    }

    @Override
    public void setImageTagData(String s) throws JposException {

    }

    @Override
    public int getMapMode() throws JposException {
        return 0;
    }

    @Override
    public void setMapMode(int i) throws JposException {

    }

    @Override
    public int getMaxCropAreas() throws JposException {
        return 0;
    }

    @Override
    public int getQuality() throws JposException {
        return 0;
    }

    @Override
    public void setQuality(int i) throws JposException {

    }

    @Override
    public String getQualityList() throws JposException {
        return null;
    }

    @Override
    public int getRemainingImagesEstimate() throws JposException {
        return 0;
    }

    @Override
    public void beginInsertion(int i) throws JposException {

    }

    @Override
    public void beginRemoval(int i) throws JposException {

    }

    @Override
    public void clearImage(int i) throws JposException {

    }

    @Override
    public void defineCropArea(int i, int i1, int i2, int i3, int i4) throws JposException {

    }

    @Override
    public void endInsertion() throws JposException {

    }

    @Override
    public void endRemoval() throws JposException {

    }

    @Override
    public void retrieveImage(int i) throws JposException {

    }

    @Override
    public void retrieveMemory(int i) throws JposException {

    }

    @Override
    public void storeImage(int i) throws JposException {

    }
}
