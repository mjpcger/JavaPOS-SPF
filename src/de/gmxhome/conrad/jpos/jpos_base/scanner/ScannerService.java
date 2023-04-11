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
import jpos.services.*;

import java.util.Arrays;

/**
 * Scanner service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class ScannerService extends JposBase implements ScannerService115 {
    /**
     * Instance of a class implementing the ScannerInterface for scanner specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public ScannerInterface ScannerInterface;

    private ScannerProperties Data;

    /**
     * Constructor. Stores property set and device driver implementation
     *
     * @param props  Device service property set.
     * @param device Device driver implementation.
     */
    public ScannerService(ScannerProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public boolean getDecodeData() throws JposException {
        checkOpened();
        logGet("DecodeData");
        return Data.DecodeData;
    }

    @Override
    public void setDecodeData(boolean b) throws JposException {
        logPreSet("DecodeData");
        checkOpened();
        checkNoChangedOrClaimed(Data.DecodeData, b);
        ScannerInterface.decodeData(b);
        logSet("DecodeData");
    }

    @Override
    public byte[] getScanData() throws JposException {
        checkClaimed();
        logGet("ScanData");
        return Arrays.copyOf(Data.ScanData, Data.ScanData.length);
    }

    @Override
    public byte[] getScanDataLabel() throws JposException {
        checkClaimed();
        logGet("ScanDataLabel");
        return Arrays.copyOf(Data.ScanDataLabel, Data.ScanDataLabel.length);
    }

    @Override
    public int getScanDataType() throws JposException {
        checkClaimed();
        logGet("ScanDataType");
        return Data.ScanDataType;
    }
}
