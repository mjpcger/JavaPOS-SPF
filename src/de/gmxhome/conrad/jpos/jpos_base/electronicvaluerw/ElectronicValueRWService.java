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

package de.gmxhome.conrad.jpos.jpos_base.electronicvaluerw;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.services.*;

import java.lang.reflect.*;
import java.util.Arrays;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.ElectronicValueRWConst.*;
import static jpos.JposConst.*;

/**
 * ElectronicValueRW service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 * <br>This service supports the following properties in jpos.xml in addition to the properties listed in JposBaseDevice:
 * <ul>
 *     <li>UseEnumeratedValues: If true, Enumerated values passed to <i>setParameterInformation</i> and returned
 *     from <i>retrieveResultInformation</i> are integer values, converted to strings. If false, these values are
 *     passed as symbols as specified in the UPOS specification. Default is true.</li>
 *     <li>StrongEnumerationCheck: If true, Enumeration values passed to <i>setParameterInformation</i>will be
 *     checked. If they do not match one of the predefined constants, an exception will be thrown. Otherwise, any
 *     number will be accepted. Default is true.</li>
 *
 * </ul>
 * These properties will only be used if the service factory passes the jpos entries to the addDevice method.
 * If the deprecated addDevice method is used, the service will not consider these properties.
 */
public class ElectronicValueRWService extends JposBase implements ElectronicValueRWService116 {
    /**
     * Instance of a class implementing the ElectronicValueRWInterface for electronic value reader / writer specific
     * setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public ElectronicValueRWInterface ElectronicValueRW;

    private final ElectronicValueRWProperties Data;

    private TransactionAccess TransactionCommand = null;

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    @SuppressWarnings("deprecation")
    public ElectronicValueRWService(ElectronicValueRWProperties props, JposDevice device) {
        super(props, device);
        Data = props;
        Data.Results = Data.TypedResults = new TypeSafeStringMap(Data.UseEnumeratedValues, Data.StrongEnumerationCheck);
        Data.Parameters = Data.TypedParameters = Data.TypedResults.emptyClone();
    }

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     * @param ignoredUseEnumeratedValues   Ignored. In previous versions, value for Data.UseEnumeratedValues.
     * @param ignoredStrongEnumerationCheck Ignored. in previous versions, value for Data.StrongEnumerationCheck
     */
    @SuppressWarnings("deprecation")
    public ElectronicValueRWService(ElectronicValueRWProperties props, JposDevice device, boolean ignoredUseEnumeratedValues, boolean ignoredStrongEnumerationCheck) {
        super(props, device);
        Data = props;
        Data.Results = Data.TypedResults = new TypeSafeStringMap(Data.UseEnumeratedValues, Data.StrongEnumerationCheck);
        Data.Parameters = Data.TypedParameters = Data.TypedResults.emptyClone();
    }

    /*
        We start with Capabilities
     */

    @Override
    public boolean getCapActivateService() throws JposException {
        logGet("CapActivateService");
        checkOpened();
        return Data.CapActivateService;
    }

    @Override
    public boolean getCapAdditionalSecurityInformation() throws JposException {
        logGet("CapAdditionalSecurityInformation");
        checkOpened();
        return Data.CapAdditionalSecurityInformation;
    }

    @Override
    public boolean getCapAddValue() throws JposException {
        logGet("CapAddValue");
        checkOpened();
        return Data.CapAddValue;
    }

    @Override
    public boolean getCapAuthorizeCompletion() throws JposException {
        logGet("CapAuthorizeCompletion");
        checkOpened();
        return Data.CapAuthorizeCompletion;
    }

    @Override
    public boolean getCapAuthorizePreSales() throws JposException {
        logGet("CapAuthorizePreSales");
        checkOpened();
        return Data.CapAuthorizePreSales;
    }

    @Override
    public boolean getCapAuthorizeRefund() throws JposException {
        logGet("CapAuthorizeRefund");
        checkOpened();
        return Data.CapAuthorizeRefund;
    }

    @Override
    public boolean getCapAuthorizeVoid() throws JposException {
        logGet("CapAuthorizeVoid");
        checkOpened();
        return Data.CapAuthorizeVoid;
    }

    @Override
    public boolean getCapAuthorizeVoidPreSales() throws JposException {
        logGet("CapAuthorizeVoidPreSales");
        checkOpened();
        return Data.CapAuthorizeVoidPreSales;
    }

    @Override
    public boolean getCapCancelValue() throws JposException {
        logGet("CapCancelValue");
        checkOpened();
        return Data.CapCancelValue;
    }

    @Override
    public int getCapCardSensor() throws JposException {
        logGet("CapCardSensor");
        checkOpened();
        return Data.CapCardSensor;
    }

    @Override
    public boolean getCapCashDeposit() throws JposException {
        logGet("CapCashDeposit");
        checkOpened();
        return Data.CapCashDeposit;
    }

    @Override
    public boolean getCapCenterResultCode() throws JposException {
        logGet("CapCenterResultCode");
        checkOpened();
        return Data.CapCenterResultCode;
    }

    @Override
    public boolean getCapCheckCard() throws JposException {
        logGet("CapCheckCard");
        checkOpened();
        return Data.CapCheckCard;
    }

    @Override
    public int getCapDailyLog() throws JposException {
        logGet("CapDailyLog");
        checkOpened();
        return Data.CapDailyLog;
    }

    @Override
    public int getCapDetectionControl() throws JposException {
        logGet("CapDetectionControl");
        checkOpened();
        return Data.CapDetectionControl;
    }

    @Override
    public boolean getCapElectronicMoney() throws JposException {
        logGet("CapElectronicMoney");
        checkOpened();
        return Data.CapElectronicMoney;
    }

    @Override
    public boolean getCapEnumerateCardServices() throws JposException {
        logGet("CapEnumerateCardServices");
        checkOpened();
        return Data.CapEnumerateCardServices;
    }

    @Override
    public boolean getCapIndirectTransactionLog() throws JposException {
        logGet("CapIndirectTransactionLog");
        checkOpened();
        return Data.CapIndirectTransactionLog;
    }

    @Override
    public boolean getCapInstallments() throws JposException {
        logGet("CapInstallments");
        checkOpened();
        return Data.CapInstallments;
    }

    @Override
    public boolean getCapLockTerminal() throws JposException {
        logGet("CapLockTerminal");
        checkOpened();
        return Data.CapLockTerminal;
    }

    @Override
    public boolean getCapLogStatus() throws JposException {
        logGet("CapLogStatus");
        checkOpened();
        return Data.CapLogStatus;
    }

    @Override
    public boolean getCapMediumID() throws JposException {
        logGet("CapMediumID");
        checkOpened();
        return Data.CapMediumID;
    }

    @Override
    public boolean getCapMembershipCertificate() throws JposException {
        logGet("CapMembershipCertificate");
        checkOpened();
        return Data.CapMembershipCertificate;
    }

    @Override
    public boolean getCapPaymentDetail() throws JposException {
        logGet("CapPaymentDetail");
        checkOpened();
        return Data.CapPaymentDetail;
    }

    @Override
    public boolean getCapPINDevice() throws JposException {
        logGet("CapPINDevice");
        checkOpened();
        return Data.CapPINDevice;
    }

    @Override
    public boolean getCapPoint() throws JposException {
        logGet("CapPoint");
        checkOpened();
        return Data.CapPoint;
    }

    @Override
    public boolean getCapRealTimeData() throws JposException {
        logGet("CapRealTimeData???");
        return false;
    }

    @Override
    public boolean getCapSubtractValue() throws JposException {
        logGet("CapSubtractValue");
        checkOpened();
        return Data.CapSubtractValue;
    }

    @Override
    public boolean getCapTaxOthers() throws JposException {
        logGet("CapTaxOthers");
        checkOpened();
        return Data.CapTaxOthers;
    }

    @Override
    public boolean CapTrainingMode() throws JposException {
        logGet("CapTrainingMode");
        checkOpened();
        return Data.CapTrainingMode;
    }

    @Override
    public boolean getCapTransaction() throws JposException {
        logGet("CapTransaction");
        checkOpened();
        return Data.CapTransaction;
    }

    @Override
    public boolean getCapTransactionLog() throws JposException {
        logGet("CapTransactionLog");
        checkOpened();
        return Data.CapTransactionLog;
    }

    @Override
    public boolean getCapTransactionNumber() throws JposException {
        logGet("CapTransactionNumber");
        checkOpened();
        return Data.CapTransactionNumber;
    }

    @Override
    public boolean getCapUnlockTerminal() throws JposException {
        logGet("CapUnlockTerminal");
        checkOpened();
        return Data.CapUnlockTerminal;
    }

    @Override
    public boolean getCapUpdateKey() throws JposException {
        logGet("CapUpdateKey");
        checkOpened();
        return Data.CapUpdateKey;
    }

    @Override
    public boolean getCapVoucher() throws JposException {
        logGet("CapVoucher");
        checkOpened();
        return Data.CapVoucher;
    }

    @Override
    public boolean getCapWriteValue() throws JposException {
        logGet("CapWriteValue");
        checkOpened();
        return Data.CapWriteValue;
    }

    /*
        We continue with Property get methods
     */

    @Override
    public String getAccountNumber() throws JposException {
        logGet("AccountNumber");
        checkOpened();
        return Data.AccountNumber;
    }

    @Override
    public String getAdditionalSecurityInformation() throws JposException {
        logGet("AdditionalSecurityInformation");
        checkOpened();
        return Data.AdditionalSecurityInformation;
    }

    @Override
    public long getAmount() throws JposException {
        logGet("Amount");
        checkOpened();
        return Data.Amount;
    }

    @Override
    public String getApprovalCode() throws JposException {
        logGet("ApprovalCode");
        checkOpened();
        return Data.ApprovalCode;
    }

    @Override
    public long getBalance() throws JposException {
        logGet("Balance");
        checkOpened();
        return Data.Balance;
    }

    @Override
    public long getBalanceOfPoint() throws JposException {
        logGet("BalanceOfPoint");
        checkOpened();
        return Data.BalanceOfPoint;
    }

    @Override
    public String getCardCompanyID() throws JposException {
        logGet("CardCompanyID");
        checkOpened();
        return Data.CardCompanyID;
    }

    @Override
    public String getCardServiceList() throws JposException {
        logGet("CardServiceList");
        checkOpened();
        return Data.CardServiceList;
    }

    @Override
    public String getCenterResultCode() throws JposException {
        logGet("CenterResultCode");
        checkOpened();
        return Data.CenterResultCode;
    }

    @Override
    public String getCurrentService() throws JposException {
        logGet("CurrentService");
        checkOpened();
        return Data.CurrentService;
    }

    @Override
    public String getDailyLog() throws JposException {
        logGet("DailyLog");
        checkOpened();
        return Data.DailyLog;
    }

    @Override
    public boolean getDetectionControl() throws JposException {
        logGet("DetectionControl");
        checkOpened();
        return Data.DetectionControl;
    }

    @Override
    public int getDetectionStatus() throws JposException {
        logGet("DetectionStatus");
        checkOpened();
        return Data.DetectionStatus;
    }

    @Override
    public String getExpirationDate() throws JposException {
        logGet("ExpirationDate");
        checkOpened();
        return Data.ExpirationDate;
    }

    @Override
    public String getLastUsedDate() throws JposException {
        logGet("LastUsedDate");
        checkOpened();
        return Data.LastUsedDate;
    }

    @Override
    public int getLogStatus() throws JposException {
        logGet("LogStatus");
        checkOpened();
        check(Data.LogStatus == null, JPOS_E_ILLEGAL, "LogStatus not available");
        return Data.LogStatus;
    }

    @Override
    public String getMediumID() throws JposException {
        logGet("MediumID");
        checkOpened();
        return Data.MediumID;
    }

    @Override
    public int getPaymentCondition() throws JposException {
        logGet("PaymentCondition");
        checkOpened();
        check(Data.PaymentCondition == null, JPOS_E_ILLEGAL, "PaymentCondition not available");
        return Data.PaymentCondition;
    }

    @Override
    public String getPaymentDetail() throws JposException {
        logGet("PaymentDetail");
        checkOpened();
        return Data.PaymentDetail;
    }

    @Override
    public int getPaymentMedia() throws JposException {
        logGet("PaymentMedia");
        checkOpened();
        return Data.PaymentMedia;
    }

    @Override
    public int getPINEntry() throws JposException {
        logGet("PINEntry");
        checkOpened();
        return Data.PINEntry;
    }

    @Override
    public long getPoint() throws JposException {
        logGet("Point");
        checkOpened();
        return Data.Point;
    }

    @Override
    public String getReaderWriterServiceList() throws JposException {
        logGet("ReaderWriterServiceList");
        checkOpened();
        check(Data.ReaderWriterServiceList == null, JPOS_E_FAILURE, "Implementation error: ReaderWriterServiceList not initialized");
        return Data.ReaderWriterServiceList;
    }

    @Override
    public int getSequenceNumber() throws JposException {
        logGet("SequenceNumber");
        checkOpened();
        return Data.SequenceNumber;
    }

    @Override
    public int getServiceType() throws JposException {
        logGet("ServiceType");
        checkOpened();
        return Data.ServiceType;
    }

    @Override
    public long getSettledAmount() throws JposException {
        logGet("SettledAmount");
        checkOpened();
        return Data.SettledAmount;
    }

    @Override
    public long getSettledPoint() throws JposException {
        logGet("SettledPoint");
        checkOpened();
        return Data.SettledPoint;
    }

    @Override
    public String getSlipNumber() throws JposException {
        logGet("SlipNumber");
        checkOpened();
        return Data.SlipNumber;
    }

    @Override
    public int getTrainingModeState() throws JposException {
        logGet("TrainingModeState");
        checkOpened();
        return Data.TrainingModeState;
    }

    @Override
    public String getTransactionLog() throws JposException {
        logGet("TransactionLog");
        checkOpened();
        return Data.TransactionLog;
    }

    @Override
    public String getTransactionNumber() throws JposException {
        logGet("TransactionNumber");
        checkOpened();
        return Data.TransactionNumber;
    }

    @Override
    public int getTransactionType() throws JposException {
        logGet("TransactionType");
        checkOpened();
        return Data.TransactionType;
    }

    @Override
    public String getVoucherID() throws JposException {
        logGet("VoucherID");
        checkOpened();
        return Data.VoucherID;
    }

    @Override
    public String getVoucherIDList() throws JposException {
        logGet("VoucherIDList");
        checkOpened();
        return Data.VoucherIDList;
    }

    /*
        We continue with Property set methods
     */

    @Override
    public void setAdditionalSecurityInformation(String s) throws JposException {
        logPreSet("AdditionalSecurityInformation");
        checkOpened();
        check(!Data.CapAdditionalSecurityInformation, JPOS_E_ILLEGAL, "AdditionalSecurityInformation not supported by service");
        if (s == null)
            s = "";
        checkNoChangedOrClaimed(Data.AdditionalSecurityInformation, s);
        ElectronicValueRW.additionalSecurityInformation(s);
        logSet("AdditionalSecurityInformation");
    }

    @Override
    public void setAmount(long l) throws JposException {
        logPreSet("Amount");
        checkOpened();
        checkNoChangedOrClaimed(Data.Amount, l);
        ElectronicValueRW.amount(l);
        logSet("Amount");
    }

    @Override
    public void setApprovalCode(String s) throws JposException {
        logPreSet("ApprovalCode");
        if (s == null)
            s = "";
        checkOpened();
        checkNoChangedOrClaimed(Data.ApprovalCode, s);
        ElectronicValueRW.approvalCode(s);
        logSet("ApprovalCode");
    }

    @Override
    public void setCurrentService(String s) throws JposException {
        logPreSet("CurrentService");
        if (s == null)
            s = "";
        checkOpened();
        if (s.length() > 0) {
            check(Data.ReaderWriterServiceList == null, JPOS_E_FAILURE, "Implementation error: ReaderWriterServiceList not initialized");
            String[] services = Data.ReaderWriterServiceList.split(",");
            boolean missing = true;
            for (String service : services) {
                if (s.equals(service)) {
                    missing = false;
                    break;
                }
            }
            check(missing, JPOS_E_ILLEGAL, "Service " + s + " not supported");
        }
        checkNoChangedOrClaimed(Data.CurrentService, s);
        ElectronicValueRW.currentService(s);
        logSet("CurrentService");
    }

    @Override
    public void setDetectionControl(boolean b) throws JposException {
        logPreSet("DetectionControl");
        checkOpened();
        check((Data.CapDetectionControl & EVRW_CDC_APPLICATIONCONTROL) == 0 && b, JPOS_E_ILLEGAL, "Application control for card handling not supported by service");
        checkNoChangedOrClaimed(Data.DetectionControl, b);
        ElectronicValueRW.detectionControl(b);
        logSet("DetectionControl");
    }

    @Override
    public void setMediumID(String s) throws JposException {
        logPreSet("MediumID");
        if (s == null)
            s = "";
        checkOpened();
        checkNoChangedOrClaimed(Data.MediumID, s);
        ElectronicValueRW.mediumID(s);
        logSet("MediumID");
    }

    @Override
    public void setPaymentMedia(int i) throws JposException {
        long[] validvalues = { EVRW_MEDIA_UNSPECIFIED, EVRW_MEDIA_CREDIT, EVRW_MEDIA_DEBIT, EVRW_MEDIA_ELECTRONIC_MONEY };
        logPreSet("PaymentMedia");
        checkOpened();
        checkMember(i, validvalues, JPOS_E_ILLEGAL, "Invalid PaymentMedia: " + i);
        checkNoChangedOrClaimed(Data.PaymentMedia, i);
        ElectronicValueRW.paymentMedia(i);
        logSet("PaymentMedia");
    }

    @Override
    public void setPINEntry(int i) throws JposException {
        long[] validvalues = { EVRW_PIN_ENTRY_NONE, EVRW_PIN_ENTRY_EXTERNAL, EVRW_PIN_ENTRY_INTERNAL, EVRW_PIN_ENTRY_UNKNOWN };
        logPreSet("PINEntry");
        checkOpened();
        checkMember(i, validvalues, JPOS_E_ILLEGAL, "Invalid PINEntry: " + i);
        checkNoChangedOrClaimed(Data.PINEntry, i);
        ElectronicValueRW.PINEntry(i);
        logSet("PINEntry");
    }

    @Override
    public void setPoint(long l) throws JposException {
        logPreSet("Point");
        checkOpened();
        checkNoChangedOrClaimed(Data.Point, l);
        ElectronicValueRW.point(l);
        logSet("Point");
    }

    @Override
    public void setTrainingModeState(int i) throws JposException {
        long[] validvalues = { EVRW_TM_FALSE, EVRW_TM_TRUE, EVRW_TM_UNKNOWN };
        logPreSet("TrainingModeState");
        checkOpened();
        check(!Data.CapTrainingMode && i == EVRW_TM_TRUE, JPOS_E_ILLEGAL, "TrainingModeState cannot be set to TM_TRUE");
        checkMember(i, validvalues, JPOS_E_ILLEGAL, "Invalid TrainingModeState: " + i);
        checkNoChangedOrClaimed(Data.TrainingModeState, i);
        ElectronicValueRW.trainingModeState(i);
        logSet("TrainingModeState");
    }

    @Override
    public void setVoucherID(String s) throws JposException {
        logPreSet("VoucherID");
        if (s == null)
            s = "";
        checkOpened();
        if (s.length() > 0) {
            String[] parts = s.split(":");
            check(parts.length != 2, JPOS_E_ILLEGAL, "Invalid VoucherID format: " + s);
            int i;
            try {
                i = Integer.parseInt(parts[1]);
            } catch (Exception e) {
                throw new JposException(JPOS_E_ILLEGAL, "Invalid VoucherID format: " + s);
            }
            check(i <= 0, JPOS_E_ILLEGAL, "Invalid VoucherID format: " + s);
        }
        checkNoChangedOrClaimed(Data.VoucherID, s);
        ElectronicValueRW.voucherID(s);
        logSet("VoucherID");
    }

    @Override
    public void setVoucherIDList(String s) throws JposException {
        logPreSet("VoucherIDList");
        if (s == null)
            s = "";
        checkOpened();
        if (s.length() > 0) {
            String[] ids = s.split(",");
            for (String id : ids) {
                String[] parts = id.split(":");
                check(parts.length != 2, JPOS_E_ILLEGAL, "Invalid VoucherID format: " + s);
                int i;
                try {
                    i = Integer.parseInt(parts[1]);
                } catch (Exception e) {
                    throw new JposException(JPOS_E_ILLEGAL, "Invalid VoucherID format: " + s);
                }
                check(i <= 0, JPOS_E_ILLEGAL, "Invalid VoucherID format: " + s);
            }
        }
        checkNoChangedOrClaimed(Data.VoucherIDList, s);
        ElectronicValueRW.voucherIDList(s);
        logSet("VoucherIDList");
    }

    /*
        We continue with methods running always synchronously
     */

    @Override
    public void beginDetection(int type, int timeout) throws JposException {
        long[] validvalues = { EVRW_BD_ANY, EVRW_BD_SPECIFIC};
        logPreCall("BeginDetection", removeOuterArraySpecifier(new Object[]{type, timeout}, Device.MaxArrayStringElements));
        checkEnabled();
        check(Data.State != JPOS_S_IDLE, JPOS_E_BUSY, "Device busy");
        check(!Data.DetectionControl, JPOS_E_BUSY, "Card detection by application disabled");
        checkMember(type, validvalues, JPOS_E_ILLEGAL, "Invalid type: " + type);
        check(timeout != JPOS_FOREVER && timeout < 0,JPOS_E_ILLEGAL, "Invalid timeout value: " + timeout);
        ElectronicValueRW.beginDetection(type, timeout);
        logCall("BeginDetection");
    }

    @Override
    public void beginRemoval(int timeout) throws JposException {
        logPreCall("BeginRemoval", removeOuterArraySpecifier(new Object[]{timeout}, Device.MaxArrayStringElements));
        checkEnabled();
        check(Data.State != JPOS_S_IDLE, JPOS_E_BUSY, "Device busy");
        check(timeout != JPOS_FOREVER && timeout < 0,JPOS_E_ILLEGAL, "Invalid timeout value: " + timeout);
        ElectronicValueRW.beginRemoval(timeout);
        logCall("BeginRemoval");
    }

    @Override
    public void captureCard() throws JposException {
        logPreCall("CaptureCard");
        checkEnabled();
        ElectronicValueRW.captureCard();
        logCall("CaptureCard");
    }

    @Override
    public void clearParameterInformation() throws JposException {
        logPreCall("ClearParameterInformation");
        checkEnabled();
        ElectronicValueRW.clearParameterInformation();
        logCall("ClearParameterInformation");
    }

    @Override
    public void endDetection() throws JposException {
        logPreCall("EndDetection");
        checkEnabled();
        check(!Data.DetectionControl, JPOS_E_BUSY, "Card detection by application disabled");
        ElectronicValueRW.endDetection();
        logCall("EndDetection");
    }

    @Override
    public void endRemoval() throws JposException {
        logPreCall("EndRemoval");
        checkEnabled();
        ElectronicValueRW.endRemoval();
        logCall("EndRemoval");
    }

    @Override
    public void enumerateCardServices() throws JposException {
        logPreCall("EnumerateCardServices");
        checkEnabled();
        ElectronicValueRW.enumerateCardServices();
        logCall("EnumerateCardServices");
    }

    /**
     * Array of tag names and names of corresponding enum values. Even number of elements. Odd elements have type
     * String and specify the name of an enumeration tag. The following even element is of type Object[] and contains
     * the names of the values that are valid for that tag for named tag values or the value itself for unnamed tag
     * values as Integer.
     */
    static final Object[] EnumTagsValues = {
            "AuthenticationStatus", new Object[]{ "EVRW_TAG_AS_AUTHENTICATED", "EVRW_TAG_AS_UNAUTHENTICATED" },
            "CancelTransactionType", new Object[]{ "EVRW_TAG_CTT_CANCEL", "EVRW_TAG_CTT_CHARGE", "EVRW_TAG_CTT_RETURN", "EVRW_TAG_CTT_SALES" },
            "ChargeMethod", new Object[]{ "EVRW_TAG_CM_CASH", "EVRW_TAG_CM_CREDIT", "EVRW_TAG_CM_POINT" },
            "NegativeInformationType", new Object[]{ "EVRW_TAG_NIT_ALL", "EVRW_TAG_NIT_UPDATED" },
            "PaymentCondition", new Object[]{
                    "EVRW_TAG_PC_INSTALLMENT_1", "EVRW_TAG_PC_INSTALLMENT_2", "EVRW_TAG_PC_INSTALLMENT_3",
                    "EVRW_TAG_PC_BONUS_1", "EVRW_TAG_PC_BONUS_2", "EVRW_TAG_PC_BONUS_3", "EVRW_TAG_PC_BONUS_4", "EVRW_TAG_PC_BONUS_5",
                    "EVRW_TAG_PC_BONUS_COMBINATION_1", "EVRW_TAG_PC_BONUS_COMBINATION_2", "EVRW_TAG_PC_BONUS_COMBINATION_3", "EVRW_TAG_PC_BONUS_COMBINATION_4",
                    "EVRW_TAG_PC_LUMP", "EVRW_TAG_PC_REVOLVING"
            },
            "PaymentMethod", new Object[]{ "EVRW_TAG_PM_COMBINED", "EVRW_TAG_PM_FULL_SETTLEMENT" },
            "PaymentMethodForPoint", new Object[]{ "EVRW_TAG_PMFP_CASH", "EVRW_TAG_PMFP_CREDIT", "EVRW_TAG_PMFP_EM", "EVRW_TAG_PMFP_OTHER" },
            "ResultOnSettlement", new Object[]{ "EVRW_TAG_ROS_NG", "EVRW_TAG_ROS_OK", "EVRW_TAG_ROS_UNKNOWN" },
            "SummaryTermType", new Object[]{ "EVRW_TAG_STT_1", "EVRW_TAG_STT_2", "EVRW_TAG_STT_3" },
            "TransactionType", new Object[]{
                    "EVRW_TAG_TT_CANCEL_CHARGE", "EVRW_TAG_TT_CANCEL_RETURN", "EVRW_TAG_TT_CANCEL_SALES", "EVRW_TAG_TT_GET_LOG",
                    "EVRW_TAG_TT_READ", "EVRW_TAG_TT_WRITE", "EVRW_TAG_TT_ADD", "EVRW_TAG_TT_SUBTRACT",
                    "EVRW_TAG_TT_RETURN", "EVRW_TAG_TT_COMPLETION", "EVRW_TAG_TT_PRE_SALES"
            },
            "VOIDorRETURN", new Object[]{ "EVRW_TAG_VR_VOID" , "EVRW_TAG_VR_RETURN" },
            "VoidTransactionType", new Object[]{ "EVRW_TAG_VTT_SALES" , "EVRW_TAG_VTT_CHARGE", "EVRW_TAG_VTT_CANCEL", "EVRW_TAG_VTT_RETURN" }
    };

    /**
     * Array of names of DateTime tags.
     */
    static final String[] DateTimeTags = {
            "AccessLogLastDateTime", "DateTime", "EndDateTime", "EVRWDataUpdateDateTime", "EVRWDateTime", "ExpirationDate",
            "KeyExpirationDateTime", "KeyUpdateDateTime", "LastUsedDateTime", "NegativeInformationUpdateDateTime", "POSDateTime", "StartDateTime"
    };

    /**
     * Array of names of currency tags.
     */
    static final String[] CurrencyTags = {
            "Amount", "AmountForPoint", "Balance", "BalanceOfPoint", "ChargeableAmount", "InsufficientAmount", "LastTimeBalance",
            "OtherAmount", "RequestedAutoChargeAmount", "SettledAmount", "SettledAutoChargeAmount", "SettledOther-Amount", "TaxOthers",
            "TotalAmountOfAddition", "TotalAmountOfSubtraction", "TotalAmountOfTransaction", "TotalAmountOfUncompletedAddition",
            "TotalAmountOfUncompletedSubtraction", "TotalAmountOfUncompletedVoid", "TotalAmountOfVoid"
    };

    /**
     * Array of names of boolean tags.
     */
    static final String[] BoolTags = {
            "AutoCharge", "ForceOnlineCheck", "LogCheck", "SignatureFlag", "SoundAssistFlag", "UILCDControl", "UILEDControl", "UISOUNDControl"
    };

    /**
     * Array of names of number tags.
     */
    static final String[] NumberTags = {
            "CardTransactionNumber", "ChargeableCount", "EffectiveDaysOfKey", "EndEVRWTransactionNumber", "EndPOSTransactionNumber",
            "EVRWID", "EVRWTransactionNumber", "MediumID", "ModuleID", "NumberOfAddition", "NumberOfEVRWTransactionLog", "NumberOfFreeEVRWTransactionLog",
            "NumberOfRecord", "NumberOfSentEVRWTransactionLog", "NumberOfSubtraction", "NumberOfTransaction", "NumberOfUncompletedAddition",
            "NumberOfUncompletedSubtraction", "NumberOfUncompletedVoid", "NumberOfVoid", "Point", "POSTransactionNumber", "RegistrableServiceCapacity",
            "ResponseCode1", "ResponseCode2", "RetryTimeout", "SettledPoint", "SettlementNumber", "StartEVRWTransactionNumber",
            "StartPOSTransactionNumber", "TouchTimeout"
    };

    static {
        for (int i = 0; i < EnumTagsValues.length; i += 2) {
            Object values = EnumTagsValues[i + 1];
            Object[] valueNames = (Object[]) values;
            Object[] fields = new Object[valueNames.length];
            int k = 0;
            for (int j = 0; j < fields.length; j++) {
                try {
                    if (valueNames[j] instanceof Integer)
                        fields[k++] = valueNames[j];
                    else
                        fields[k++] = ElectronicValueRWConst.class.getField(valueNames[j].toString());
                } catch (NoSuchFieldException | SecurityException ignored) {}
            }
            values = Arrays.copyOf(fields, k);
            TypeSafeStringMap.addTagType((String) EnumTagsValues[i], values);
        }
        for (String s : DateTimeTags)
            TypeSafeStringMap.addTagType(s, String.class);
        for (String s : CurrencyTags)
            TypeSafeStringMap.addTagType(s, Long.class);
        for (String s : BoolTags)
            TypeSafeStringMap.addTagType(s, Boolean.class);
        for (String s : NumberTags)
            TypeSafeStringMap.addTagType(s, Integer.class);
    }

    @Override
    public void retrieveResultInformation(String name, String[] value) throws JposException {
        logPreCall("RetrieveResultInformation", removeOuterArraySpecifier(new Object[]{name}, Device.MaxArrayStringElements));
        checkEnabled();
        check(name == null || name.length() == 0, JPOS_E_ILLEGAL, "No tag specified");
        check(value == null || value.length != 1, JPOS_E_ILLEGAL, "Value not String[1]");
        if ("SetttledVoucherID".equals(name))
            name = "SettledVoucherID";
        ElectronicValueRW.retrieveResultInformation(name, value);
        if (value[0] == null)
            value[0] = "";
        logCall("RetrieveResultInformation", name + ", " + value[0]);
    }

    @Override
    public void setParameterInformation(String name, String value) throws JposException {
        logPreCall("SetParameterInformation", removeOuterArraySpecifier(new Object[]{name, value}, Device.MaxArrayStringElements));
        if ("SetttledVoucherID".equals(name))
            name = "SettledVoucherID";
        checkEnabled();
        check(name == null || name.length() == 0, JPOS_E_ILLEGAL, "No tag specified");
        check(value == null, JPOS_E_ILLEGAL, "No value specified");
        ElectronicValueRW.setParameterInformation(name, value);
        logCall("SetParameterInformation");
    }

    /*
        We continue with methods that potentially run asynchronously
     */

    @Override
    public void accessDailyLog(int sequenceNumber, int type, int timeout) throws JposException {
        long[] validvalues = {EVRW_DL_REPORTING, EVRW_DL_SETTLEMENT};

        logPreCall("AccessDailyLog", removeOuterArraySpecifier(new Object[]{sequenceNumber, type, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        checkMember(type, validvalues, JPOS_E_ILLEGAL, "Invalid log type: " + type);
        check((type & ~Data.CapDailyLog) != 0, JPOS_E_ILLEGAL, "Invalid log type: " + type);
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.accessDailyLog(sequenceNumber, type, timeout), "AccessDailyLog");
    }

    @Override
    public void accessData(int dataType, int[] data, Object[] obj) throws JposException {
        long[] validvalues = {EVRW_AD_KEY, EVRW_AD_NEGATIVE_LIST, EVRW_AD_OTHERS};

        logPreCall("AccessData", removeOuterArraySpecifier(new Object[]{dataType, data, obj}, Device.MaxArrayStringElements));
        checkBusy();
        checkMember(dataType, validvalues, JPOS_E_ILLEGAL, "Invalid dataType: " + dataType);
        check(data == null || data.length != 1, JPOS_E_ILLEGAL, "Invalid data");
        check(obj == null || obj.length != 1, JPOS_E_ILLEGAL, "Invalid obj");
        callIt(ElectronicValueRW.accessData(dataType, data, obj), "AccessData");
    }

    @Override
    public void accessLog(int sequenceNumber, int type, int timeout) throws JposException {
        long[] validvalues = {EVRW_AL_REPORTING, EVRW_AL_SETTLEMENT};

        logPreCall("AccessLog", removeOuterArraySpecifier(new Object[]{sequenceNumber, type, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        checkMember(type, validvalues, JPOS_E_ILLEGAL, "Invalid type: " + type);
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.accessLog(sequenceNumber, type, timeout), "AccessLog");
    }

    @Override
    public void activateEVService(int[] data, Object[] obj) throws JposException {
        logPreCall("ActivateEVService", removeOuterArraySpecifier(new Object[]{data, obj}, Device.MaxArrayStringElements));
        checkBusy();
        check(data == null || data.length != 1, JPOS_E_ILLEGAL, "Invalid data");
        check(obj == null || obj.length != 1, JPOS_E_ILLEGAL, "Invalid obj");
        callIt(ElectronicValueRW.activateEVService(data, obj), "ActivateEVService");
    }

    @Override
    public void activateService(int[] data, Object[] obj) throws JposException {
        logPreCall("ActivateService", removeOuterArraySpecifier(new Object[]{data, obj}, Device.MaxArrayStringElements));
        checkBusy();
        check(!Data.CapActivateService, JPOS_E_ILLEGAL, "ActivateService not supported");
        check(data == null || data.length != 1, JPOS_E_ILLEGAL, "Invalid data");
        check(obj == null || obj.length != 1, JPOS_E_ILLEGAL, "Invalid obj");
        callIt(ElectronicValueRW.activateService(data, obj), "ActivateService");
    }

    @Override
    public void addValue(int sequenceNumber, int timeout) throws JposException {
        logPreCall("AddValue", removeOuterArraySpecifier(new Object[]{sequenceNumber, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        check(!Data.CapAddValue, JPOS_E_ILLEGAL, "AddValue not supported");
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.addValue(sequenceNumber, timeout), "AddValue");
    }

    @Override
    public void authorizeCompletion(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
        logPreCall("AuthorizeCompletion", removeOuterArraySpecifier(new Object[]{sequenceNumber, amount, taxOthers, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        check(!Data.CapAuthorizeCompletion, JPOS_E_ILLEGAL, "AuthorizeCompletion not supported");
        check(amount <= 0, JPOS_E_ILLEGAL, "Invalid amount: " + amount);
        check(taxOthers < 0, JPOS_E_ILLEGAL, "Invalid taxOthers: " + taxOthers);
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.authorizeCompletion(sequenceNumber, amount, taxOthers, timeout), "AuthorizeCompletion");
    }

    @Override
    public void authorizePreSales(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
        logPreCall("AuthorizePreSales", removeOuterArraySpecifier(new Object[]{sequenceNumber, amount, taxOthers, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        check(!Data.CapAuthorizePreSales, JPOS_E_ILLEGAL, "AuthorizePreSales not supported");
        check(amount <= 0, JPOS_E_ILLEGAL, "Invalid amount: " + amount);
        check(taxOthers < 0, JPOS_E_ILLEGAL, "Invalid taxOthers: " + taxOthers);
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.authorizePreSales(sequenceNumber, amount, taxOthers, timeout), "AuthorizePreSales");
    }

    @Override
    public void authorizeRefund(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
        logPreCall("AuthorizeRefund", removeOuterArraySpecifier(new Object[]{sequenceNumber, amount, taxOthers, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        check(!Data.CapAuthorizeRefund, JPOS_E_ILLEGAL, "AuthorizeRefund not supported");
        check(amount <= 0, JPOS_E_ILLEGAL, "Invalid amount: " + amount);
        check(taxOthers < 0, JPOS_E_ILLEGAL, "Invalid taxOthers: " + taxOthers);
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.authorizeRefund(sequenceNumber, amount, taxOthers, timeout), "AuthorizeRefund");
    }

    @Override
    public void authorizeSales(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
        logPreCall("AuthorizeSales", removeOuterArraySpecifier(new Object[]{sequenceNumber, amount, taxOthers, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        check(amount <= 0, JPOS_E_ILLEGAL, "Invalid amount: " + amount);
        check(taxOthers < 0, JPOS_E_ILLEGAL, "Invalid taxOthers: " + taxOthers);
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.authorizeSales(sequenceNumber, amount, taxOthers, timeout), "AuthorizeSales");
    }

    @Override
    public void authorizeVoid(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
        logPreCall("AuthorizeVoid", removeOuterArraySpecifier(new Object[]{sequenceNumber, amount, taxOthers, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        check(!Data.CapAuthorizeVoid, JPOS_E_ILLEGAL, "AuthorizeVoid not supported");
        check(amount <= 0, JPOS_E_ILLEGAL, "Invalid amount: " + amount);
        check(taxOthers < 0, JPOS_E_ILLEGAL, "Invalid taxOthers: " + taxOthers);
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.authorizeVoid(sequenceNumber, amount, taxOthers, timeout), "AuthorizeVoid");
    }

    @Override
    public void authorizeVoidPreSales(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
        logPreCall("AuthorizeVoidPreSales", removeOuterArraySpecifier(new Object[]{sequenceNumber, amount, taxOthers, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        check(!Data.CapAuthorizeVoidPreSales, JPOS_E_ILLEGAL, "AuthorizeVoidPreSales not supported");
        check(amount <= 0, JPOS_E_ILLEGAL, "Invalid amount: " + amount);
        check(taxOthers < 0, JPOS_E_ILLEGAL, "Invalid taxOthers: " + taxOthers);
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.authorizeVoidPreSales(sequenceNumber, amount, taxOthers, timeout), "AuthorizeVoidPreSales");
    }

    @Override
    public void cancelValue(int sequenceNumber, int timeout) throws JposException {
        logPreCall("CancelValue", removeOuterArraySpecifier(new Object[]{sequenceNumber, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        check(!Data.CapCancelValue, JPOS_E_ILLEGAL, "CancelValue not supported");
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.cancelValue(sequenceNumber, timeout), "CancelValue");
    }

    @Override
    public void cashDeposit(int sequenceNumber, long amount, int timeout) throws JposException {
        logPreCall("CashDeposit", removeOuterArraySpecifier(new Object[]{sequenceNumber, amount, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        check(!Data.CapCashDeposit, JPOS_E_ILLEGAL, "CashDeposit not supported");
        check(amount <= 0, JPOS_E_ILLEGAL, "Invalid amount: " + amount);
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.cashDeposit(sequenceNumber, amount, timeout), "CashDeposit");
    }

    @Override
    public void checkCard(int sequenceNumber, int timeout) throws JposException {
        logPreCall("CheckCard", removeOuterArraySpecifier(new Object[]{sequenceNumber, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        check(!Data.CapCheckCard, JPOS_E_ILLEGAL, "CheckCard not supported");
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.checkCard(sequenceNumber, timeout), "CheckCard");
    }

    @Override
    public void checkServiceRegistrationToMedium(int sequenceNumber, int timeout) throws JposException {
        logPreCall("CheckServiceRegistrationToMedium", removeOuterArraySpecifier(new Object[]{sequenceNumber, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.checkServiceRegistrationToMedium(sequenceNumber, timeout), "CheckServiceRegistrationToMedium");
    }

    @Override
    public void closeDailyEVService(int[] data, Object[] obj) throws JposException {
        logPreCall("CloseDailyEVService", removeOuterArraySpecifier(new Object[]{data, obj}, Device.MaxArrayStringElements));
        checkBusy();
        check(data == null || data.length != 1, JPOS_E_ILLEGAL, "Invalid data");
        check(obj == null || obj.length != 1, JPOS_E_ILLEGAL, "Invalid obj");
        callIt(ElectronicValueRW.closeDailyEVService(data, obj), "CloseDailyEVService");
    }

    @Override
    public void deactivateEVService(int[] data, Object[] obj) throws JposException {
        logPreCall("DeactivateEVService", removeOuterArraySpecifier(new Object[]{data, obj}, Device.MaxArrayStringElements));
        checkBusy();
        check(data == null || data.length != 1, JPOS_E_ILLEGAL, "Invalid data");
        check(obj == null || obj.length != 1, JPOS_E_ILLEGAL, "Invalid obj");
        callIt(ElectronicValueRW.deactivateEVService(data, obj), "DeactivateEVService");
    }

    @Override
    public void lockTerminal() throws JposException {
        logPreCall("LockTerminal");
        checkBusy();
        check(!Data.CapLockTerminal, JPOS_E_ILLEGAL, "LockTerminal not supported");
        callIt(ElectronicValueRW.lockTerminal(), "LockTerminal");
    }

    @Override
    public void openDailyEVService(int[] data, Object[] obj) throws JposException {
        logPreCall("UpdateKey", removeOuterArraySpecifier(new Object[]{data, obj}, Device.MaxArrayStringElements));
        checkBusy();
        check(data == null || data.length != 1, JPOS_E_ILLEGAL, "Invalid data");
        check(obj == null || obj.length != 1, JPOS_E_ILLEGAL, "Invalid obj");
        callIt(ElectronicValueRW.updateKey(data, obj), "UpdateKey");
    }

    @Override
    public void queryLastSuccessfulTransactionResult() throws JposException {
        logPreCall("QueryLastSuccessfulTransactionResult");
        checkEnabled();
        check(!Data.CapSubtractValue, JPOS_E_ILLEGAL, "CancelValue not supported");
        callIt(ElectronicValueRW.queryLastSuccessfulTransactionResult(), "QueryLastSuccessfulTransactionResult");
    }

    @Override
    public void readValue(int sequenceNumber, int timeout) throws JposException {
        logPreCall("ReadValue", removeOuterArraySpecifier(new Object[]{sequenceNumber, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.readValue(sequenceNumber, timeout), "ReadValue");
    }

    @Override
    public void registerServiceToMedium(int sequenceNumber, int timeout) throws JposException {
        logPreCall("RegisterServiceToMedium", removeOuterArraySpecifier(new Object[]{sequenceNumber, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.registerServiceToMedium(sequenceNumber, timeout), "RegisterServiceToMedium");
    }

    @Override
    public void subtractValue(int sequenceNumber, int timeout) throws JposException {
        logPreCall("SubtractValue", removeOuterArraySpecifier(new Object[]{sequenceNumber, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        check(!Data.CapSubtractValue, JPOS_E_ILLEGAL, "CancelValue not supported");
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.subtractValue(sequenceNumber, timeout), "SubtractValue");
    }

    @Override
    public void transactionAccess(int control) throws JposException {
        long[] validvalues = {EVRW_TA_TRANSACTION, EVRW_TA_NORMAL};

        logPreCall("TransactionAccess", removeOuterArraySpecifier(new Object[]{control}, Device.MaxArrayStringElements));
        checkBusy();
        check(!Data.CapTransaction, JPOS_E_ILLEGAL, "TransactionAccess not supported");
        checkMember(control, validvalues, JPOS_E_ILLEGAL, "Invalid control value: " + control);
        check(control == EVRW_TA_TRANSACTION && TransactionCommand != null, JPOS_E_ILLEGAL, "Starting transaction within transaction not supported");
        check(control == EVRW_TA_NORMAL && TransactionCommand == null, JPOS_E_ILLEGAL, "Ending transaction from outside a transaction not possible");
        TransactionAccess command = ElectronicValueRW.transactionAccess(control);
        if (TransactionCommand == null) {
            TransactionCommand = command;
            logCall("TransactionAccess");
        } else {
            callIt(command, "TransactionAccess");
        }
    }

    @Override
    public void unlockTerminal() throws JposException {
        logPreCall("UnlockTerminal");
        checkBusy();
        check(!Data.CapUnlockTerminal, JPOS_E_ILLEGAL, "UnlockTerminal not supported");
        callIt(ElectronicValueRW.unlockTerminal(), "UnlockTerminal");
    }

    @Override
    public void unregisterServiceToMedium(int sequenceNumber, int timeout) throws JposException {
        logPreCall("UnregisterServiceToMedium", removeOuterArraySpecifier(new Object[]{sequenceNumber, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.unregisterServiceToMedium(sequenceNumber, timeout), "UnregisterServiceToMedium");
    }

    @Override
    public void updateData(int dataType, int[] data, Object[] obj) throws JposException {
        long[] validvalues = {EVRW_AD_KEY, EVRW_AD_NEGATIVE_LIST, EVRW_AD_OTHERS};

        logPreCall("UpdateData", removeOuterArraySpecifier(new Object[]{dataType, data, obj}, Device.MaxArrayStringElements));
        checkBusy();
        checkMember(dataType, validvalues, JPOS_E_ILLEGAL, "Invalid dataType: " + dataType);
        check(data == null || data.length != 1, JPOS_E_ILLEGAL, "Invalid data");
        check(obj == null || obj.length != 1, JPOS_E_ILLEGAL, "Invalid obj");
        callIt(ElectronicValueRW.updateData(dataType, data, obj), "UpdateData");
    }

    @Override
    public void updateKey(int[] data, Object[] obj) throws JposException {
        logPreCall("UpdateKey", removeOuterArraySpecifier(new Object[]{data, obj}, Device.MaxArrayStringElements));
        checkBusy();
        check(data == null || data.length != 1, JPOS_E_ILLEGAL, "Invalid data");
        check(obj == null || obj.length != 1, JPOS_E_ILLEGAL, "Invalid obj");
        callIt(ElectronicValueRW.updateKey(data, obj), "UpdateKey");
    }

    @Override
    public void writeValue(int sequenceNumber, int timeout) throws JposException {
        logPreCall("WriteValue", removeOuterArraySpecifier(new Object[]{sequenceNumber, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        check(!Data.CapWriteValue, JPOS_E_ILLEGAL, "WriteValue not supported");
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.writeValue(sequenceNumber, timeout), "WriteValue");
    }

    /*
        We finish with internal helper methods
     */

    private void callIt(JposOutputRequest request, String name) throws JposException {
        if (TransactionCommand != null) {
            if (request instanceof ReadValue || request instanceof WriteValue || request instanceof AddValue ||
                request instanceof SubtractValue || request instanceof CancelValue || request instanceof TransactionAccess)
            {
                TransactionCommand.TransactionCommands.add(request);
                if (!(request instanceof TransactionAccess)) {
                    logCall(name);
                    return;
                }
                request = TransactionCommand;
                TransactionCommand = null;
            }
        }
        if (callNowOrLater(request))
            logAsyncCall(name);
        else
            logCall(name);
    }
}
