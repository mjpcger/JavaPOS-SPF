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

package de.gmxhome.conrad.jpos.jpos_base.cat;

import de.gmxhome.conrad.jpos.jpos_base.*;

/**
 * Output request class for credit authorization terminals
 */
public class OutputRequest extends JposOutputRequest {
    /**
     * Returns contents of AdditionalSecurityInformation when the output request has been generated.
     * @return Contents of AdditionalSecurityInformation.
     */
    public String getAdditionalSecurityInformation() {
        return AdditionalSecurityInformation;
    }
    private final String AdditionalSecurityInformation;

    /**
     * Returns contents of PaymentMedia when the output request has been generated.
     * @return Contents of PaymentMedia.
     */
    public int getPaymentMedia() {
        return PaymentMedia;
    }
    private final int PaymentMedia;

    /**
     * Returns contents of TrainingMode when the output request has been generated.
     * @return Contents of TrainingMode.
     */
    public boolean getTrainingMode() {
        return TrainingMode;
    }
    private final boolean TrainingMode;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     */
    public OutputRequest(CATProperties props) {
        super(props);
        AdditionalSecurityInformation = props.AdditionalSecurityInformation;
        PaymentMedia = props.PaymentMedia;
        TrainingMode = props.TrainingMode;
    }
}
