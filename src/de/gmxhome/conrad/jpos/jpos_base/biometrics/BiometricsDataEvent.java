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

package de.gmxhome.conrad.jpos.jpos_base.biometrics;

import de.gmxhome.conrad.jpos.jpos_base.*;

import java.util.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.BiometricsConst.*;

/**
 * Data event implementation for Biometric devices.
 */
public class BiometricsDataEvent extends JposDataEvent {
    /**
     * New contents for property BIR. For details, see UPOS specification.
     */
    public final byte[] BIR;

    /**
     * New contents for property RawSensorData. For details, see UPOS specification.
     */
    public final byte[] RawSensorData;

    /**
     * Constructor. Parameters passed to base class unchanged.
     *
     * @param source  Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state   Status, see UPOS specification.
     * @param bir     Value to be filled in property BIR before event delivery, see UPOS specification. If null, BIR
     *                will remain unchanged.
     * @param rawData Value to be filled in property RawSensorData before event delivery, see UPOS specification. if null,
     *                RawSensorData will remain unchanged.
     */
    public BiometricsDataEvent(JposBase source, int state, byte[] bir, byte[] rawData) {
        super(source, state);
        BIR = bir == null ? null : Arrays.copyOf(bir, bir.length);
        RawSensorData = rawData == null ? null : Arrays.copyOf(rawData, rawData.length);
    }

    @Override
    public void setDataProperties() {
        super.setDataProperties();
        BiometricsProperties props = (BiometricsProperties) getPropertySet();
        if (BIR != null) {
            props.BIR = Arrays.copyOf(BIR, BIR.length);
            props.EventSource.logSet("BIR");
        }
        if (RawSensorData != null) {
            props.RawSensorData = Arrays.copyOf(RawSensorData, RawSensorData.length);
            props.EventSource.logSet("RawSensorData");
        }
    }

    @Override
    public String toLogString() {
        String ret;
        if (member(getStatus(), new long[]{BIO_DATA_ENROLL, BIO_DATA_VERIFY}))
            ret = "Status: " + (getStatus() == BIO_DATA_ENROLL ? "DATA_ENROLL" : "DATA_VERIFY");
        else
            ret = super.toLogString();
        return ret;
    }
}
