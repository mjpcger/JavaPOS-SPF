/*
 * Copyright 2019 Martin Conrad
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
 */

package de.gmxhome.conrad.jpos.jpos_base.scanner;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Class containing the scanner specific properties, their default values and default implementations of
 * ScannerInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Scanner (Bar Code
 * Reader).
 */
public class ScannerProperties extends JposCommonProperties implements ScannerInterface {
    /**
     * Default value of ScanData property. Default: Zero-length byte array. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public byte[] ScanDataDef = new byte[0];
    /**
     * UPOS property DecodeData.
     */
    public boolean DecodeData;
    /**
     * UPOS property ScanData.
     */
    public byte[] ScanData;
    /**
     * UPOS property ScanDataLabel.
     */
    public byte[] ScanDataLabel;
    /**
     * UPOS property ScanDataType.
     */
    public int ScanDataType;

    /**
     * Constructor. Sets ExclusiveUse to ExclusiveYes to match the ScannerInterface device model.
     *
     * @param dev Device index
     */
    public ScannerProperties(int dev) {
        super(dev);
    }

    @Override
    public void initOnOpen() {
        super.initOnOpen();
        DecodeData = false;
        clearDataProperties();
    }

    @Override
    public void clearDataProperties() {
        super.clearDataProperties();
        ScanData = ScanDataLabel = ScanDataDef;
        ScanDataType = ScannerConst.SCAN_SDT_UNKNOWN;

    }

    @Override
    public void decodeData(boolean flag) throws JposException {
        DecodeData = flag;
    }
}
