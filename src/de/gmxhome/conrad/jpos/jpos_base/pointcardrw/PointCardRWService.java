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

package de.gmxhome.conrad.jpos.jpos_base.pointcardrw;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.JposException;
import jpos.services.PointCardRWService114;

/**
 * PointCardRW service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class PointCardRWService extends JposBase implements PointCardRWService114 {
    /**
     * Instance of a class implementing the PointCardRWInterface for point card reader / writer specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public PointCardRWInterface PointCardRW;

    private PointCardRWProperties Data;
    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public PointCardRWService(PointCardRWProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public boolean getCapMapCharacterSet() throws JposException {
        return false;
    }

    @Override
    public boolean getMapCharacterSet() throws JposException {
        return false;
    }

    @Override
    public void setMapCharacterSet(boolean b) throws JposException {

    }

    @Override
    public boolean getCapBold() throws JposException {
        return false;
    }

    @Override
    public int getCapCardEntranceSensor() throws JposException {
        return 0;
    }

    @Override
    public int getCapCharacterSet() throws JposException {
        return 0;
    }

    @Override
    public boolean getCapCleanCard() throws JposException {
        return false;
    }

    @Override
    public boolean getCapClearPrint() throws JposException {
        return false;
    }

    @Override
    public boolean getCapDhigh() throws JposException {
        return false;
    }

    @Override
    public boolean getCapDwide() throws JposException {
        return false;
    }

    @Override
    public boolean getCapDwideDhigh() throws JposException {
        return false;
    }

    @Override
    public boolean getCapItalic() throws JposException {
        return false;
    }

    @Override
    public boolean getCapLeft90() throws JposException {
        return false;
    }

    @Override
    public boolean getCapPrint() throws JposException {
        return false;
    }

    @Override
    public boolean getCapPrintMode() throws JposException {
        return false;
    }

    @Override
    public boolean getCapRight90() throws JposException {
        return false;
    }

    @Override
    public boolean getCapRotate180() throws JposException {
        return false;
    }

    @Override
    public int getCapTracksToRead() throws JposException {
        return 0;
    }

    @Override
    public int getCapTracksToWrite() throws JposException {
        return 0;
    }

    @Override
    public int getCardState() throws JposException {
        return 0;
    }

    @Override
    public int getCharacterSet() throws JposException {
        return 0;
    }

    @Override
    public void setCharacterSet(int i) throws JposException {

    }

    @Override
    public String getCharacterSetList() throws JposException {
        return null;
    }

    @Override
    public String getFontTypeFaceList() throws JposException {
        return null;
    }

    @Override
    public int getLineChars() throws JposException {
        return 0;
    }

    @Override
    public void setLineChars(int i) throws JposException {

    }

    @Override
    public String getLineCharsList() throws JposException {
        return null;
    }

    @Override
    public int getLineHeight() throws JposException {
        return 0;
    }

    @Override
    public void setLineHeight(int i) throws JposException {

    }

    @Override
    public int getLineSpacing() throws JposException {
        return 0;
    }

    @Override
    public void setLineSpacing(int i) throws JposException {

    }

    @Override
    public int getLineWidth() throws JposException {
        return 0;
    }

    @Override
    public int getMapMode() throws JposException {
        return 0;
    }

    @Override
    public void setMapMode(int i) throws JposException {

    }

    @Override
    public int getMaxLines() throws JposException {
        return 0;
    }

    @Override
    public int getPrintHeight() throws JposException {
        return 0;
    }

    @Override
    public int getReadState1() throws JposException {
        return 0;
    }

    @Override
    public int getReadState2() throws JposException {
        return 0;
    }

    @Override
    public int getRecvLength1() throws JposException {
        return 0;
    }

    @Override
    public int getRecvLength2() throws JposException {
        return 0;
    }

    @Override
    public int getSidewaysMaxChars() throws JposException {
        return 0;
    }

    @Override
    public int getSidewaysMaxLines() throws JposException {
        return 0;
    }

    @Override
    public int getTracksToRead() throws JposException {
        return 0;
    }

    @Override
    public void setTracksToRead(int i) throws JposException {

    }

    @Override
    public int getTracksToWrite() throws JposException {
        return 0;
    }

    @Override
    public void setTracksToWrite(int i) throws JposException {

    }

    @Override
    public String getTrack1Data() throws JposException {
        return null;
    }

    @Override
    public String getTrack2Data() throws JposException {
        return null;
    }

    @Override
    public String getTrack3Data() throws JposException {
        return null;
    }

    @Override
    public String getTrack4Data() throws JposException {
        return null;
    }

    @Override
    public String getTrack5Data() throws JposException {
        return null;
    }

    @Override
    public String getTrack6Data() throws JposException {
        return null;
    }

    @Override
    public int getWriteState1() throws JposException {
        return 0;
    }

    @Override
    public int getWriteState2() throws JposException {
        return 0;
    }

    @Override
    public String getWrite1Data() throws JposException {
        return null;
    }

    @Override
    public void setWrite1Data(String s) throws JposException {

    }

    @Override
    public String getWrite2Data() throws JposException {
        return null;
    }

    @Override
    public void setWrite2Data(String s) throws JposException {

    }

    @Override
    public String getWrite3Data() throws JposException {
        return null;
    }

    @Override
    public void setWrite3Data(String s) throws JposException {

    }

    @Override
    public String getWrite4Data() throws JposException {
        return null;
    }

    @Override
    public void setWrite4Data(String s) throws JposException {

    }

    @Override
    public String getWrite5Data() throws JposException {
        return null;
    }

    @Override
    public void setWrite5Data(String s) throws JposException {

    }

    @Override
    public String getWrite6Data() throws JposException {
        return null;
    }

    @Override
    public void setWrite6Data(String s) throws JposException {

    }

    @Override
    public void beginInsertion(int i) throws JposException {

    }

    @Override
    public void beginRemoval(int i) throws JposException {

    }

    @Override
    public void cleanCard() throws JposException {

    }

    @Override
    public void clearPrintWrite(int i, int i1, int i2, int i3, int i4) throws JposException {

    }

    @Override
    public void endInsertion() throws JposException {

    }

    @Override
    public void endRemoval() throws JposException {

    }

    @Override
    public void printWrite(int i, int i1, int i2, String s) throws JposException {

    }

    @Override
    public void rotatePrint(int i) throws JposException {

    }

    @Override
    public void validateData(String s) throws JposException {

    }
}
