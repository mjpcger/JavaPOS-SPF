/*
 * Copyright 2022 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.electronicvaluerw;

import de.gmxhome.conrad.jpos.jpos_base.JposOutputRequest;
import de.gmxhome.conrad.jpos.jpos_base.cat.CATProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Base output request class for ElectronicValueRW devices. Holds a copy of all properties and parameters from the time where
 * the request has been created.
 */
public class OutputRequest extends JposOutputRequest {
    /**
     * Returns contents of AdditionalSecurityInformation when the output request has been generated.
     * @return Contents of AdditionalSecurityInformation.
     */
    public String getAdditionalSecurityInformation() {
        return AdditionalSecurityInformation;
    }
    private String AdditionalSecurityInformation;

    /**
     * Returns contents of PaymentMedia when the output request has been generated.
     * @return Contents of PaymentMedia.
     */
    public int getPaymentMedia() {
        return PaymentMedia;
    }
    private int PaymentMedia;

    /**
     * Returns contents of TrainingModeState when the output request has been generated.
     * @return Contents of TrainingMode.
     */
    public int getTrainingMode() {
        return TrainingMode;
    }
    private int TrainingMode;

    /**
     * Returns contents of CurrentService when the output request has been generated.
     * @return Contents of CurrentService.
     */
    public String getCurrentService() {
        return CurrentService;
    }
    private String CurrentService;

    /**
     * Returns contents of ApprovalCode when the output request has been generated.
     * @return Contents of ApprovalCode.
     */
    public String getApprovalCode() {
        return ApprovalCode;
    }
    private String ApprovalCode;

    /**
     * Returns contents of PINEntry when the output request has been generated.
     * @return Contents of PINEntry.
     */
    public int getPINEntry() {
        return PINEntry;
    }
    private int PINEntry;

    /**
     * Returns contents of ServiceType when the output request has been generated.
     * @return Contents of ServiceType.
     */
    public int getServiceType() {
        return ServiceType;
    }
    private int ServiceType;

    /**
     * Get parameter set by setParameterInformation before request has been created.
     * @param key Key of parameter to be queried, e.g. "Amount".
     * @return parameter value, null if parameter has not been set.
     */
    public String getParameter(String key) {
        return Parameters.get(key);
    }
    private Map<String, String> Parameters = new HashMap<String, String>();

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     */
    public OutputRequest(ElectronicValueRWProperties props) {
        super(props);
        AdditionalSecurityInformation = props.AdditionalSecurityInformation;
        ApprovalCode = props.ApprovalCode;
        CurrentService = props.CurrentService;
        PaymentMedia = props.PaymentMedia;
        PINEntry = props.PINEntry;
        ServiceType = props.ServiceType;
        TrainingMode = props.TrainingModeState;
        synchronized ((props.Parameters)) {
            Parameters.putAll(props.Parameters);
        }
        if (!Parameters.containsKey("Amount") && props.Amount != 0)
            Parameters.put("Amount", String.valueOf(props.Amount));
        if (!Parameters.containsKey("MediumID") && props.MediumID.length() > 0)
            Parameters.put("MediumID", props.MediumID);
        if (!Parameters.containsKey("Point") && props.Point != 0)
            Parameters.put("Point", String.valueOf(props.Point));
        if (!Parameters.containsKey("VoucherID") && props.VoucherID.length() > 0)
            Parameters.put("VoucherID", props.VoucherID);
        if (!Parameters.containsKey("VoucherIDList") && props.VoucherIDList.length() > 0)
            Parameters.put("VoucherIDList", props.VoucherIDList);
    }
}
