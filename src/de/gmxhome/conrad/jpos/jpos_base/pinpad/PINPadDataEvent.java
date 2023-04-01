/*
 * Copyright 2023 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.pinpad;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;


/**
 * Data event implementation for PINPad devices.
 */
public class PINPadDataEvent extends JposDataEvent {
    private String AdditionalSecurityInformation, EncryptedPIN;

    /**
     * Since PINPadConst.PPAD_TIMEOUT is missing in current JavaPOS versions, this property can be used instead.
     * <br>Until this bug will be fixed, PPAD_TIMEOUT will be set to 3. When the bug will be fixed, PPAD_TIMEOUT will have
     * the value of PINPadConst.PPAD_TIMEOUT.
     */
    static final public int PPAD_TIMEOUT = getPPAD_TIMEOUT();

    private static int getPPAD_TIMEOUT() {
        int ret = 3;
        try {
            ret = (int)(PINPadConst.class.getField("PPAD_TIMEOUT").get(null));
        } catch (Exception e) {}
        return ret;
    }

    /**
     * Constructor. Parameters passed to base class unchanged.
     *
     * @param source                        Source, for services implemented with this framework, the
     *                                      (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state                         Status, see UPOS specification.
     * @param additionalSecurityInformation Additional security/encryption information.
     * @param encryptedPIN                  Value of the Encrypted PIN.
     */
    public PINPadDataEvent(JposBase source, int state, String additionalSecurityInformation, String encryptedPIN) {
        super(source, state);
        AdditionalSecurityInformation = additionalSecurityInformation;
        EncryptedPIN = encryptedPIN;
    }

    @Override
    public void setDataProperties() {
        PINPadService service = (PINPadService) getSource();
        PINPadProperties props = (PINPadProperties) (service.Props);
        if (!AdditionalSecurityInformation.equals(props.AdditionalSecurityInformation)) {
            props.AdditionalSecurityInformation = AdditionalSecurityInformation;
            service.logSet("AdditionalSecurityInformation");
        }
        if (!EncryptedPIN.equals(props.EncryptedPIN)) {
            props.EncryptedPIN = EncryptedPIN;
            service.logSet("EncryptedPIN");
        }
    }

    @Override
    public String toLogString() {
        int state = getStatus();
        String statestr;
        if (state == PINPadConst.PPAD_SUCCESS)
            statestr = "SUCCESS";
        else if (state == PINPadConst.PPAD_CANCEL)
            statestr = "CANCEL";
        else if (state == PPAD_TIMEOUT)
            statestr = "TIMEOUT";
        else
            statestr = String.valueOf(getStatus());
        return "Status: " + statestr + ", EncryptedPIN: " + EncryptedPIN + ", AdditionalSecurityInformation: " + AdditionalSecurityInformation;
    }
}
