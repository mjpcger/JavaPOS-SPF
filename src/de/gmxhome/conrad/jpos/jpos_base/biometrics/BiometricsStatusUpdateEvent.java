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

import de.gmxhome.conrad.jpos.jpos_base.JposBase;
import de.gmxhome.conrad.jpos.jpos_base.JposStatusUpdateEvent;

import static jpos.BiometricsConst.*;

/**
 * Status update event implementation for Biometric devices.
 */
public class BiometricsStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Constructor, Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     */
    public BiometricsStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
    }

    @Override
    public boolean checkStatusCorresponds() {
        return super.checkStatusCorresponds() || switch (getStatus()) {
            case BIO_SUE_RAW_DATA, BIO_SUE_MOVE_LEFT,
                    BIO_SUE_MOVE_RIGHT, BIO_SUE_MOVE_DOWN,
                    BIO_SUE_MOVE_UP, BIO_SUE_MOVE_CLOSER,
                    BIO_SUE_MOVE_AWAY, BIO_SUE_MOVE_BACKWARD,
                    BIO_SUE_MOVE_FORWARD, BIO_SUE_MOVE_SLOWER,
                    BIO_SUE_MOVE_FASTER, BIO_SUE_SENSOR_DIRTY,
                    BIO_SUE_FAILED_READ, BIO_SUE_SENSOR_READY,
                    BIO_SUE_SENSOR_COMPLETE ->
                    true;
            default -> false;
        };
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        return ret.length() > 0 ? ret : switch (getStatus()) {
            case BIO_SUE_RAW_DATA -> "Biometrics raw data";
            case BIO_SUE_MOVE_LEFT -> "Biometrics move left";
            case BIO_SUE_MOVE_RIGHT -> "Biometrics move right";
            case BIO_SUE_MOVE_DOWN -> "Biometrics move down";
            case BIO_SUE_MOVE_UP -> "Biometrics move up";
            case BIO_SUE_MOVE_CLOSER -> "Biometrics move closer";
            case BIO_SUE_MOVE_AWAY -> "Biometrics move away";
            case BIO_SUE_MOVE_BACKWARD -> "Biometrics move backward";
            case BIO_SUE_MOVE_FORWARD -> "Biometrics move forward";
            case BIO_SUE_MOVE_SLOWER -> "Biometrics move slower";
            case BIO_SUE_MOVE_FASTER -> "Biometrics move faster";
            case BIO_SUE_SENSOR_DIRTY -> "Biometrics sensor dirty";
            case BIO_SUE_FAILED_READ -> "Biometrics failed read";
            case BIO_SUE_SENSOR_READY -> "Biometrics sensor ready";
            case BIO_SUE_SENSOR_COMPLETE -> "Biometrics sensor complete";
            default -> "Unknown Biometrics Status Change: " + getStatus();
        };
    }
}
