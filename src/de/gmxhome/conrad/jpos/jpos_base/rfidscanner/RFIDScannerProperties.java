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

package de.gmxhome.conrad.jpos.jpos_base.rfidscanner;

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;

/**
 * Class containing the RFID scanner specific properties, their default values and default implementations of
 * RFIDScannerInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter RFID Scanner.
 */
public class RFIDScannerProperties extends JposCommonProperties implements RFIDScannerInterface {
    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected RFIDScannerProperties(int dev) {
        super(dev);
        ExclusiveUse = ExclusiveYes;
    }
}
