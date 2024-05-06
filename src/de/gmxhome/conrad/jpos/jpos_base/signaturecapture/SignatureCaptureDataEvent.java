/*
 * Copyright 2020 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.signaturecapture;

import de.gmxhome.conrad.jpos.jpos_base.*;

import java.awt.*;
import java.util.Arrays;

/**
 * Data event implementation for SignatureCapture devices.
 */
public class SignatureCaptureDataEvent extends JposDataEvent {
    /**
     * Holds the RawData property value to be stored before firing the event.
     */
    final byte[] RawData;

    /**
     * Holds the PointArray property value to be stored before firing the event.
     */
    final Point[] PointArray;

    /**
     * Constructor. Parameters passed to base class unchanged.
     * @param source Source, for services implemented with this framework, the (signaturecapture.)SignatureCaptureService object.
     * @param state Status, see UPOS specification, chapter Signature Capture - Events - DataEvent.
     * @param rawData Value to be stored in property ScanData.
     * @param pointArray Value to be stored in property ScanDataLabel.
     */
    public SignatureCaptureDataEvent(JposBase source, int state, byte[] rawData, Point[] pointArray) {
        super(source, state);
        if (state == 0) {
            RawData = new byte[0];
            PointArray = new Point[0];
        } else {
            RawData = rawData != null ? Arrays.copyOf(rawData, rawData.length) : new byte[0];
            PointArray = pointArray != null ? Arrays.copyOf(pointArray, pointArray.length) : new Point[0];
        }
    }

    @Override
    public void setDataProperties() {
        super.setDataProperties();
        SignatureCaptureProperties dev = (SignatureCaptureProperties) getPropertySet();
        dev.RawData = RawData;
        dev.PointArray = PointArray;
    }
}
