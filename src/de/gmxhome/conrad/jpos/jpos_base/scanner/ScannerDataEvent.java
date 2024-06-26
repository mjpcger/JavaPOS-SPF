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

/**
 * Data event implementation for Scanner devices.
 */
public class ScannerDataEvent extends JposDataEvent {
    /**
     * Holds the ScanData property value to be stored before firing the event.
     */
    final byte[] Data;
    /**
     * Holds the ScanDataLabel property value to be stored before firing the event.
     */
    final byte[] Label;
    /**
     * Holds the ScanDataType property value to be stored before firing the event.
     */
    final int Type;

    /**
     * Constructor. Parameters passed to base class unchanged.
     * @param source Source, for services implemented with this framework, the (scanner.)ScannerService object.
     * @param state Status, see UPOS specification, chapter Scanner (Bar Code Reader) - Events - DataEvent.
     * @param data Value to be stored in property ScanData.
     * @param label Value to be stored in property ScanDataLabel.
     * @param type Value to be stored in property ScanDataType.
     */
    public ScannerDataEvent(JposBase source, int state, byte[] data, byte[] label, int type) {
        super(source, state);
        Data = data;
        Label = label;
        Type = type;
    }

    @Override
    public void setDataProperties() {
        super.setDataProperties();
        ScannerProperties dev = (ScannerProperties) getPropertySet();
        dev.ScanDataType = Type;
        dev.EventSource.logSet("ScanDataType");
        dev.ScanData = Data;
        dev.EventSource.logSet("ScanData");
        dev.ScanDataLabel = Label;
        dev.EventSource.logSet("ScanDataLabel");
    }
}
