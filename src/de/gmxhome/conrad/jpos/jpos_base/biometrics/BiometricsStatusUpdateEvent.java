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
import jpos.BiometricsConst;

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
        if (super.checkStatusCorresponds())
            return true;
        switch (getStatus()) {
            case BiometricsConst.BIO_SUE_RAW_DATA:
            case BiometricsConst.BIO_SUE_MOVE_LEFT:
            case BiometricsConst.BIO_SUE_MOVE_RIGHT:
            case BiometricsConst.BIO_SUE_MOVE_DOWN:
            case BiometricsConst.BIO_SUE_MOVE_UP:
            case BiometricsConst.BIO_SUE_MOVE_CLOSER:
            case BiometricsConst.BIO_SUE_MOVE_AWAY:
            case BiometricsConst.BIO_SUE_MOVE_BACKWARD:
            case BiometricsConst.BIO_SUE_MOVE_FORWARD:
            case BiometricsConst.BIO_SUE_MOVE_SLOWER:
            case BiometricsConst.BIO_SUE_MOVE_FASTER:
            case BiometricsConst.BIO_SUE_SENSOR_DIRTY:
            case BiometricsConst.BIO_SUE_FAILED_READ:
            case BiometricsConst.BIO_SUE_SENSOR_READY:
            case BiometricsConst.BIO_SUE_SENSOR_COMPLETE:
                return true;
        }
        return false;
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        if (ret.length() > 0)
            return ret;
        switch (getStatus()) {
            case BiometricsConst.BIO_SUE_RAW_DATA:
                return "Biometrics raw data";
            case BiometricsConst.BIO_SUE_MOVE_LEFT:
                return "Biometrics move left";
            case BiometricsConst.BIO_SUE_MOVE_RIGHT:
                return "Biometrics move right";
            case BiometricsConst.BIO_SUE_MOVE_DOWN:
                return "Biometrics move down";
            case BiometricsConst.BIO_SUE_MOVE_UP:
                return "Biometrics move up";
            case BiometricsConst.BIO_SUE_MOVE_CLOSER:
                return "Biometrics move closer";
            case BiometricsConst.BIO_SUE_MOVE_AWAY:
                return "Biometrics move away";
            case BiometricsConst.BIO_SUE_MOVE_BACKWARD:
                return "Biometrics move backward";
            case BiometricsConst.BIO_SUE_MOVE_FORWARD:
                return "Biometrics move forward";
            case BiometricsConst.BIO_SUE_MOVE_SLOWER:
                return "Biometrics move slower";
            case BiometricsConst.BIO_SUE_MOVE_FASTER:
                return "Biometrics move faster";
            case BiometricsConst.BIO_SUE_SENSOR_DIRTY:
                return "Biometrics sensor dirty";
            case BiometricsConst.BIO_SUE_FAILED_READ:
                return "Biometrics failed read";
            case BiometricsConst.BIO_SUE_SENSOR_READY:
                return "Biometrics sensor ready";
            case BiometricsConst.BIO_SUE_SENSOR_COMPLETE:
                return "Biometrics sensor complete";
        }
        return "Unknown Biometrics Status Change: " + getStatus();
    }
}
