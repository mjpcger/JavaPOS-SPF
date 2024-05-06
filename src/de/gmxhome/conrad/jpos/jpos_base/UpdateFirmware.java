/*
 * Copyright 2018 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base;

import jpos.JposException;

import static de.gmxhome.conrad.jpos.jpos_base.JposBaseDevice.*;
import static jpos.JposConst.*;

/**
 * Output request executor for common method UpdateFirmware.
 */
public class UpdateFirmware extends JposOutputRequest {
    /**
     * Common method UpdateFirmware parameter firmwareFileName, see UPOS specification.
     * @return Parameter firmwareFileName.
     */
    public String getFirmwareFileName() {
        return FirmwareFileName;
    }
    private final String FirmwareFileName;

    private final long[] AllowedResult = {
            JPOS_SUE_UF_COMPLETE,
            JPOS_SUE_UF_COMPLETE_DEV_NOT_RESTORED,
            JPOS_SUE_UF_FAILED_DEV_OK,
            JPOS_SUE_UF_FAILED_DEV_UNRECOVERABLE,
            JPOS_SUE_UF_FAILED_DEV_NEEDS_FIRMWARE,
            JPOS_SUE_UF_FAILED_DEV_UNKNOWN
    };
    /**
     * Sets result of firmware update for the case that updateFirmware throws a JposException. Must be one of
     * SUE_UF_COMPLETE_DEV_NOT_RESTORED, SUE_UF_FAILED_DEV_OK, SUE_UF_FAILED_DEV_UNRECOVERABLE,
     * SUE_UF_FAILED_DEV_NEEDS_FIRMWARE or SUE_UF_FAILED_DEV_UNKNOWN. Default is SUE_UF_FAILED_DEV_UNKNOWN.
     * <br>If updateFirmware throws a JposException with ErrorCodeExtended set to one of the allowed values, Result
     * will be set to ErrorCodeExtended internally.
     * <br>If updateFirmware ends normally, Result will be set to SUE_UF_COMPLETE internally.
     * @param result Result value as explained above.
     * @throws JposException if result is invalid.
     */
    public void setResult(int result) throws JposException {
        checkMember(result, AllowedResult, JPOS_E_ILLEGAL, "Invalid UpdateFirmware result: " + result);
        Result = result;
    }
    private int Result;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props         Property set of device service.
     * @param firmwareFileName Name of a firmware file or a container file, see UPOS specification.
     */
    public UpdateFirmware(JposCommonProperties props, String firmwareFileName) {
        super(props);
        FirmwareFileName = firmwareFileName;
    }

    @Override
    public JposOutputCompleteEvent createOutputEvent() {
        return null;
    }

    @Override
    public void invoke() throws JposException {
        try {
            Result = JPOS_SUE_UF_FAILED_DEV_UNKNOWN;
            Props.EventSource.DeviceInterface.updateFirmware(this);
            Result = JPOS_SUE_UF_COMPLETE;
        } catch (JposException e) {
            if (member(e.getErrorCodeExtended(), AllowedResult)) {
                Result = e.getErrorCodeExtended();
            }
        }
        Device.handleEvent(new JposStatusUpdateEvent(Props.EventSource, Result));
    }
}
