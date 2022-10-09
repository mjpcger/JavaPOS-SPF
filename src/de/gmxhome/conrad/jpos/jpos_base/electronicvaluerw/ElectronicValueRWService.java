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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ElectronicValueRW service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class ElectronicValueRWService extends JposBase implements ElectronicValueRWService115 {
    /**
     * Instance of a class implementing the ElectronicValueRWInterface for electronic value reader / writer specific
     * setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public ElectronicValueRWInterface ElectronicValueRW;

    private ElectronicValueRWProperties Data;

    private TransactionAccess TransactionCommand = null;

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public ElectronicValueRWService(ElectronicValueRWProperties props, JposDevice device) {
        super(props, device);
        Data = props;
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
        JposDevice.check(Data.LogStatus == null, JposConst.JPOS_E_ILLEGAL, "LogStatus not available");
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
        JposDevice.check(Data.PaymentCondition == null, JposConst.JPOS_E_ILLEGAL, "PaymentCondition not available");
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
        JposDevice.check(Data.ReaderWriterServiceList == null, JposConst.JPOS_E_FAILURE, "Implementation error: ReaderWriterServiceList not initialized");
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
        JposDevice.check(!Data.CapAdditionalSecurityInformation, JposConst.JPOS_E_ILLEGAL, "AdditionalSecurityInformation not supported by service");
        if (s == null)
            s = "";
        ElectronicValueRW.additionalSecurityInformation(s);
        logSet("AdditionalSecurityInformation");
    }

    @Override
    public void setAmount(long l) throws JposException {
        logPreSet("Amount");
        checkOpened();
        ElectronicValueRW.amount(l);
        logSet("Amount");
    }

    @Override
    public void setApprovalCode(String s) throws JposException {
        logPreSet("ApprovalCode");
        checkOpened();
        if (s == null)
            s = "";
        ElectronicValueRW.approvalCode(s);
        logSet("ApprovalCode");
    }

    @Override
    public void setCurrentService(String s) throws JposException {
        logPreSet("CurrentService");
        checkOpened();
        if (s == null)
            s = "";
        if (s.length() > 0) {
            JposDevice.check(Data.ReaderWriterServiceList == null, JposConst.JPOS_E_FAILURE, "Implementation error: ReaderWriterServiceList not initialized");
            String[] services = Data.ReaderWriterServiceList.split(",");
            boolean missing = true;
            for (String service : services) {
                if (s.equals(service)) {
                    missing = false;
                    break;
                }
            }
            JposDevice.check(missing, JposConst.JPOS_E_ILLEGAL, "Service " + s + " not supported");
        }
        ElectronicValueRW.currentService(s);
        logSet("CurrentService");
    }

    @Override
    public void setDetectionControl(boolean b) throws JposException {
        logPreSet("DetectionControl");
        checkOpened();
        JposDevice.check((Data.CapDetectionControl & ElectronicValueRWConst.EVRW_CDC_APPLICATIONCONTROL) == 0 && b, JposConst.JPOS_E_ILLEGAL, "Application control for card handling not supported by service");
        ElectronicValueRW.detectionControl(b);
        logSet("DetectionControl");
    }

    @Override
    public void setMediumID(String s) throws JposException {
        logPreSet("MediumID");
        checkOpened();
        if (s == null)
            s = "";
        ElectronicValueRW.mediumID(s);
        logSet("MediumID");
    }

    @Override
    public void setPaymentMedia(int i) throws JposException {
        long[] validvalues = {
                ElectronicValueRWConst.EVRW_MEDIA_UNSPECIFIED,
                ElectronicValueRWConst.EVRW_MEDIA_CREDIT,
                ElectronicValueRWConst.EVRW_MEDIA_DEBIT,
                ElectronicValueRWConst.EVRW_MEDIA_ELECTRONIC_MONEY
        };
        logPreSet("PaymentMedia");
        checkOpened();
        JposDevice.checkMember(i, validvalues, JposConst.JPOS_E_ILLEGAL, "Invalid PaymentMedia: " + i);
        ElectronicValueRW.paymentMedia(i);
        logSet("PaymentMedia");
    }

    @Override
    public void setPINEntry(int i) throws JposException {
        long[] validvalues = {
                ElectronicValueRWConst.EVRW_PIN_ENTRY_NONE,
                ElectronicValueRWConst.EVRW_PIN_ENTRY_EXTERNAL,
                ElectronicValueRWConst.EVRW_PIN_ENTRY_INTERNAL,
                ElectronicValueRWConst.EVRW_PIN_ENTRY_UNKNOWN
        };
        logPreSet("PINEntry");
        checkOpened();
        JposDevice.checkMember(i, validvalues, JposConst.JPOS_E_ILLEGAL, "Invalid PINEntry: " + i);
        ElectronicValueRW.PINEntry(i);
        logSet("PINEntry");
    }

    @Override
    public void setPoint(long l) throws JposException {
        logPreSet("Point");
        checkOpened();
        ElectronicValueRW.point(l);
        logSet("Point");
    }

    @Override
    public void setTrainingModeState(int i) throws JposException {
        long[] validvalues = {
                ElectronicValueRWConst.EVRW_TM_FALSE,
                ElectronicValueRWConst.EVRW_TM_TRUE,
                ElectronicValueRWConst.EVRW_TM_UNKNOWN
        };
        logPreSet("TrainingModeState");
        checkOpened();
        JposDevice.check(!Data.CapTrainingMode && i == ElectronicValueRWConst.EVRW_TM_TRUE, JposConst.JPOS_E_ILLEGAL, "TrainingModeState cannot be set to TM_TRUE");
        JposDevice.checkMember(i, validvalues, JposConst.JPOS_E_ILLEGAL, "Invalid TrainingModeState: " + i);
        ElectronicValueRW.trainingModeState(i);
        logSet("TrainingModeState");
    }

    @Override
    public void setVoucherID(String s) throws JposException {
        logPreSet("VoucherID");
        checkOpened();
        if (s == null)
            s = "";
        if (s.length() > 0) {
            String[] parts = s.split(":");
            JposDevice.check(parts.length != 2, JposConst.JPOS_E_ILLEGAL, "Invalid VoucherID format: " + s);
            int i;
            try {
                i = Integer.valueOf(parts[1]);
            } catch (Exception e) {
                throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid VoucherID format: " + s);
            }
            JposDevice.check(i <= 0, JposConst.JPOS_E_ILLEGAL, "Invalid VoucherID format: " + s);
        }
        ElectronicValueRW.voucherID(s);
        logSet("VoucherID");
    }

    @Override
    public void setVoucherIDList(String s) throws JposException {
        logPreSet("VoucherIDList");
        checkOpened();
        if (s == null)
            s = "";
        if (s.length() > 0) {
            String[] ids = s.split(",");
            for (String id : ids) {
                String[] parts = id.split(":");
                JposDevice.check(parts.length != 2, JposConst.JPOS_E_ILLEGAL, "Invalid VoucherID format: " + s);
                int i;
                try {
                    i = Integer.valueOf(parts[1]);
                } catch (Exception e) {
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid VoucherID format: " + s);
                }
                JposDevice.check(i <= 0, JposConst.JPOS_E_ILLEGAL, "Invalid VoucherID format: " + s);
            }
        }
        ElectronicValueRW.voucherIDList(s);
        logSet("VoucherIDList");
    }

    /*
        We continue with methods running always synchronously
     */

    @Override
    public void beginDetection(int type, int timeout) throws JposException {
        long[] validvalues = { ElectronicValueRWConst.EVRW_BD_ANY, ElectronicValueRWConst.EVRW_BD_SPECIFIC};
        logPreCall("BeginDetection", "" + type + ", " + timeout);
        checkEnabled();
        JposDevice.check(Data.State != JposConst.JPOS_S_IDLE, JposConst.JPOS_E_BUSY, "Device busy");
        JposDevice.check(!Data.DetectionControl, JposConst.JPOS_E_BUSY, "Card detection by application disabled");
        JposDevice.checkMember(type, validvalues, JposConst.JPOS_E_ILLEGAL, "Invalid type: " + type);
        JposDevice.check(timeout != JposConst.JPOS_FOREVER && timeout < 0,JposConst.JPOS_E_ILLEGAL, "Invalid timeout value: " + timeout);
        ElectronicValueRW.beginDetection(type, timeout);
        logCall("BeginDetection");
    }

    @Override
    public void beginRemoval(int timeout) throws JposException {
        logPreCall("BeginRemoval", "" + timeout);
        checkEnabled();
        JposDevice.check(Data.State != JposConst.JPOS_S_IDLE, JposConst.JPOS_E_BUSY, "Device busy");
        JposDevice.check(timeout != JposConst.JPOS_FOREVER && timeout < 0,JposConst.JPOS_E_ILLEGAL, "Invalid timeout value: " + timeout);
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
        JposDevice.check(!Data.DetectionControl, JposConst.JPOS_E_BUSY, "Card detection by application disabled");
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

    @Override
    public void retrieveResultInformation(String s, String[] strings) throws JposException {
        logPreCall("RetrieveResultInformation", s);
        checkEnabled();
        ElectronicValueRW.retrieveResultInformation(s, strings);
        logCall("RetrieveResultInformation", s + ", " + strings[0]);
    }

    @Override
    public void setParameterInformation(String s, String s1) throws JposException {
        logPreCall("SetParameterInformation");
        checkEnabled();
        JposException e = Data.checkTagValueFormat(s, s1);
        if (e != null)
            throw e;
        ElectronicValueRW.setParameterInformation(s, s1);
        logCall("SetParameterInformation");
    }

    /*
        We continue with methods that potentially run asynchronously
     */

    @Override
    public void accessDailyLog(int sequenceNumber, int type, int timeout) throws JposException {
        long[] validvalues = {ElectronicValueRWConst.EVRW_DL_REPORTING, ElectronicValueRWConst.EVRW_DL_SETTLEMENT};

        logPreCall("AccessDailyLog", "" + sequenceNumber + ", " + type + ", " + timeout);
        checkBusy();
        JposDevice.checkMember(type, validvalues, JposConst.JPOS_E_ILLEGAL, "Invalid log type: " + type);
        JposDevice.check((type & ~Data.CapDailyLog) != 0, JposConst.JPOS_E_ILLEGAL, "Invalid log type: " + type);
        JposDevice.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.accessDailyLog(sequenceNumber, type, timeout), "AccessDailyLog");
    }

    @Override
    public void accessData(int dataType, int[] data, Object[] obj) throws JposException {
        String intsStr = ", (null)";
        String objectsStr = ", (null)";
        long[] validvalues = {ElectronicValueRWConst.EVRW_AD_KEY, ElectronicValueRWConst.EVRW_AD_NEGATIVE_LIST, ElectronicValueRWConst.EVRW_AD_OTHERS};

        if (data != null)
            intsStr = data.length == 0 ? ", (no int)" : (data.length == 1 ? ", " + data[0] : ", (multiple int)");
        if (obj != null)
            objectsStr = obj.length == 0 ? ", (no Object)" : (obj.length == 1 ? (obj[0] == null ? ", (null)" : obj[0].toString()) : ", (multiple Object)");
        logPreCall("AccessData", "" + dataType + intsStr + objectsStr);
        checkBusy();
        JposDevice.checkMember(dataType, validvalues, JposConst.JPOS_E_ILLEGAL, "Invalid dataType: " + dataType);
        JposDevice.check(data == null || data.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid data");
        JposDevice.check(obj == null || obj.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid obj");
        callIt(ElectronicValueRW.accessData(dataType, data, obj), "AccessData");
    }

    @Override
    public void accessLog(int sequenceNumber, int type, int timeout) throws JposException {
        long[] validvalues = {ElectronicValueRWConst.EVRW_AL_REPORTING, ElectronicValueRWConst.EVRW_AL_SETTLEMENT};

        logPreCall("AccessLog", "" + sequenceNumber + ", " + type + ", " + timeout);
        checkBusy();
        JposDevice.checkMember(type, validvalues, JposConst.JPOS_E_ILLEGAL, "Invalid type: " + type);
        JposDevice.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.accessLog(sequenceNumber, type, timeout), "AccessLog");
    }

    @Override
    public void activateEVService(int[] data, Object[] obj) throws JposException {
        String intsStr = "(null)";
        String objectsStr = ", (null)";

        if (data != null)
            intsStr = data.length == 0 ? "(no int)" : (data.length == 1 ? "" + data[0] : "(multiple int)");
        if (obj != null)
            objectsStr = obj.length == 0 ? ", (no Object)" : (obj.length == 1 ? (obj[0] == null ? ", (null)" : obj[0].toString()) : ", (multiple Object)");
        logPreCall("ActivateEVService", intsStr + objectsStr);
        checkBusy();
        JposDevice.check(data == null || data.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid data");
        JposDevice.check(obj == null || obj.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid obj");
        callIt(ElectronicValueRW.activateEVService(data, obj), "ActivateEVService");
    }

    @Override
    public void activateService(int[] data, Object[] obj) throws JposException {
        String intsStr = "(null)";
        String objectsStr = ", (null)";

        if (data != null)
            intsStr = data.length == 0 ? "(no int)" : (data.length == 1 ? "" + data[0] : "(multiple int)");
        if (obj != null)
            objectsStr = obj.length == 0 ? ", (no Object)" : (obj.length == 1 ? (obj[0] == null ? ", (null)" : obj[0].toString()) : ", (multiple Object)");
        logPreCall("ActivateService", intsStr + objectsStr);
        checkBusy();
        JposDevice.check(!Data.CapActivateService, JposConst.JPOS_E_ILLEGAL, "ActivateService not supported");
        JposDevice.check(data == null || data.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid data");
        JposDevice.check(obj == null || obj.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid obj");
        callIt(ElectronicValueRW.activateService(data, obj), "ActivateService");
    }

    @Override
    public void addValue(int sequenceNumber, int timeout) throws JposException {
        logPreCall("AddValue", "" + sequenceNumber + ", " + timeout);
        checkBusy();
        JposDevice.check(!Data.CapAddValue, JposConst.JPOS_E_ILLEGAL, "AddValue not supported");
        JposDevice.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.addValue(sequenceNumber, timeout), "AddValue");
    }

    @Override
    public void authorizeCompletion(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
        logPreCall("AuthorizeCompletion", "" + sequenceNumber + ", " + amount + ", " + taxOthers + ", " + timeout);
        checkBusy();
        Device.check(!Data.CapAuthorizeCompletion, JposConst.JPOS_E_ILLEGAL, "AuthorizeCompletion not supported");
        Device.check(amount <= 0, JposConst.JPOS_E_ILLEGAL, "Invalid amount: " + amount);
        Device.check(taxOthers < 0, JposConst.JPOS_E_ILLEGAL, "Invalid taxOthers: " + taxOthers);
        Device.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.authorizeCompletion(sequenceNumber, amount, taxOthers, timeout), "AuthorizeCompletion");
    }

    @Override
    public void authorizePreSales(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
        logPreCall("AuthorizePreSales", "" + sequenceNumber + ", " + amount + ", " + taxOthers + ", " + timeout);
        checkBusy();
        Device.check(!Data.CapAuthorizePreSales, JposConst.JPOS_E_ILLEGAL, "AuthorizePreSales not supported");
        Device.check(amount <= 0, JposConst.JPOS_E_ILLEGAL, "Invalid amount: " + amount);
        Device.check(taxOthers < 0, JposConst.JPOS_E_ILLEGAL, "Invalid taxOthers: " + taxOthers);
        Device.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.authorizePreSales(sequenceNumber, amount, taxOthers, timeout), "AuthorizePreSales");
    }

    @Override
    public void authorizeRefund(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
        logPreCall("AuthorizeRefund", "" + sequenceNumber + ", " + amount + ", " + taxOthers + ", " + timeout);
        checkBusy();
        Device.check(!Data.CapAuthorizeRefund, JposConst.JPOS_E_ILLEGAL, "AuthorizeRefund not supported");
        Device.check(amount <= 0, JposConst.JPOS_E_ILLEGAL, "Invalid amount: " + amount);
        Device.check(taxOthers < 0, JposConst.JPOS_E_ILLEGAL, "Invalid taxOthers: " + taxOthers);
        Device.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.authorizeRefund(sequenceNumber, amount, taxOthers, timeout), "AuthorizeRefund");
    }

    @Override
    public void authorizeSales(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
        logPreCall("AuthorizeSales", "" + sequenceNumber + ", " + amount + ", " + taxOthers + ", " + timeout);
        checkBusy();
        Device.check(amount <= 0, JposConst.JPOS_E_ILLEGAL, "Invalid amount: " + amount);
        Device.check(taxOthers < 0, JposConst.JPOS_E_ILLEGAL, "Invalid taxOthers: " + taxOthers);
        Device.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.authorizeSales(sequenceNumber, amount, taxOthers, timeout), "AuthorizeSales");
    }

    @Override
    public void authorizeVoid(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
        logPreCall("AuthorizeVoid", "" + sequenceNumber + ", " + amount + ", " + taxOthers + ", " + timeout);
        checkBusy();
        Device.check(!Data.CapAuthorizeVoid, JposConst.JPOS_E_ILLEGAL, "AuthorizeVoid not supported");
        Device.check(amount <= 0, JposConst.JPOS_E_ILLEGAL, "Invalid amount: " + amount);
        Device.check(taxOthers < 0, JposConst.JPOS_E_ILLEGAL, "Invalid taxOthers: " + taxOthers);
        Device.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.authorizeVoid(sequenceNumber, amount, taxOthers, timeout), "AuthorizeVoid");
    }

    @Override
    public void authorizeVoidPreSales(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
        logPreCall("AuthorizeVoidPreSales", "" + sequenceNumber + ", " + amount + ", " + taxOthers + ", " + timeout);
        checkBusy();
        Device.check(!Data.CapAuthorizeVoidPreSales, JposConst.JPOS_E_ILLEGAL, "AuthorizeVoidPreSales not supported");
        Device.check(amount <= 0, JposConst.JPOS_E_ILLEGAL, "Invalid amount: " + amount);
        Device.check(taxOthers < 0, JposConst.JPOS_E_ILLEGAL, "Invalid taxOthers: " + taxOthers);
        Device.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.authorizeVoidPreSales(sequenceNumber, amount, taxOthers, timeout), "AuthorizeVoidPreSales");
    }

    @Override
    public void cancelValue(int sequenceNumber, int timeout) throws JposException {
        logPreCall("CancelValue", "" + sequenceNumber + ", " + timeout);
        checkBusy();
        JposDevice.check(!Data.CapCancelValue, JposConst.JPOS_E_ILLEGAL, "CancelValue not supported");
        JposDevice.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.cancelValue(sequenceNumber, timeout), "CancelValue");
    }

    @Override
    public void cashDeposit(int sequenceNumber, long amount, int timeout) throws JposException {
        logPreCall("CashDeposit", "" + sequenceNumber + ", " + amount + ", " + timeout);
        checkBusy();
        Device.check(!Data.CapCashDeposit, JposConst.JPOS_E_ILLEGAL, "CashDeposit not supported");
        Device.check(amount <= 0, JposConst.JPOS_E_ILLEGAL, "Invalid amount: " + amount);
        Device.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.cashDeposit(sequenceNumber, amount, timeout), "CashDeposit");
    }

    @Override
    public void checkCard(int sequenceNumber, int timeout) throws JposException {
        logPreCall("CheckCard", "" + sequenceNumber + ", " + timeout);
        checkBusy();
        Device.check(!Data.CapCheckCard, JposConst.JPOS_E_ILLEGAL, "CheckCard not supported");
        Device.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.checkCard(sequenceNumber, timeout), "CheckCard");
    }

    @Override
    public void checkServiceRegistrationToMedium(int sequenceNumber, int timeout) throws JposException {
        logPreCall("CheckServiceRegistrationToMedium", "" + sequenceNumber + ", " + timeout);
        checkBusy();
        JposDevice.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.checkServiceRegistrationToMedium(sequenceNumber, timeout), "CheckServiceRegistrationToMedium");
    }

    @Override
    public void closeDailyEVService(int[] data, Object[] obj) throws JposException {
        String intsStr = "(null)";
        String objectsStr = ", (null)";

        if (data != null)
            intsStr = data.length == 0 ? "(no int)" : (data.length == 1 ? "" + data[0] : "(multiple int)");
        if (obj != null)
            objectsStr = obj.length == 0 ? ", (no Object)" : (obj.length == 1 ? (obj[0] == null ? ", (null)" : obj[0].toString()) : ", (multiple Object)");
        logPreCall("CloseDailyEVService", intsStr + objectsStr);
        checkBusy();
        JposDevice.check(data == null || data.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid data");
        JposDevice.check(obj == null || obj.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid obj");
        callIt(ElectronicValueRW.closeDailyEVService(data, obj), "CloseDailyEVService");
    }

    @Override
    public void deactivateEVService(int[] data, Object[] obj) throws JposException {
        String intsStr = "(null)";
        String objectsStr = ", (null)";

        if (data != null)
            intsStr = data.length == 0 ? "(no int)" : (data.length == 1 ? "" + data[0] : "(multiple int)");
        if (obj != null)
            objectsStr = obj.length == 0 ? ", (no Object)" : (obj.length == 1 ? (obj[0] == null ? ", (null)" : obj[0].toString()) : ", (multiple Object)");
        logPreCall("DeactivateEVService", intsStr + objectsStr);
        checkBusy();
        JposDevice.check(data == null || data.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid data");
        JposDevice.check(obj == null || obj.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid obj");
        callIt(ElectronicValueRW.deactivateEVService(data, obj), "DeactivateEVService");
    }

    @Override
    public void lockTerminal() throws JposException {
        logPreCall("LockTerminal");
        checkBusy();
        Device.check(!Data.CapLockTerminal, JposConst.JPOS_E_ILLEGAL, "LockTerminal not supported");
        callIt(ElectronicValueRW.lockTerminal(), "LockTerminal");
    }

    @Override
    public void openDailyEVService(int[] data, Object[] obj) throws JposException {
        String intsStr = "(null)";
        String objectsStr = ", (null)";

        if (data != null)
            intsStr = data.length == 0 ? "(no int)" : (data.length == 1 ? "" + data[0] : "(multiple int)");
        if (obj != null)
            objectsStr = obj.length == 0 ? ", (no Object)" : (obj.length == 1 ? (obj[0] == null ? ", (null)" : obj[0].toString()) : ", (multiple Object)");
        logPreCall("UpdateKey", intsStr + objectsStr);
        checkBusy();
        JposDevice.check(data == null || data.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid data");
        JposDevice.check(obj == null || obj.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid obj");
        callIt(ElectronicValueRW.updateKey(data, obj), "UpdateKey");
    }

    @Override
    public void queryLastSuccessfulTransactionResult() throws JposException {
        logPreCall("QueryLastSuccessfulTransactionResult");
        checkEnabled();
        JposDevice.check(!Data.CapSubtractValue, JposConst.JPOS_E_ILLEGAL, "CancelValue not supported");
        callIt(ElectronicValueRW.queryLastSuccessfulTransactionResult(), "QueryLastSuccessfulTransactionResult");
    }

    @Override
    public void readValue(int sequenceNumber, int timeout) throws JposException {
        logPreCall("ReadValue", "" + sequenceNumber + ", " + timeout);
        checkBusy();
        JposDevice.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.readValue(sequenceNumber, timeout), "ReadValue");
    }

    @Override
    public void registerServiceToMedium(int sequenceNumber, int timeout) throws JposException {
        logPreCall("RegisterServiceToMedium", "" + sequenceNumber + ", " + timeout);
        checkBusy();
        JposDevice.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.registerServiceToMedium(sequenceNumber, timeout), "RegisterServiceToMedium");
    }

    @Override
    public void subtractValue(int sequenceNumber, int timeout) throws JposException {
        logPreCall("SubtractValue", "" + sequenceNumber + ", " + timeout);
        checkBusy();
        JposDevice.check(!Data.CapSubtractValue, JposConst.JPOS_E_ILLEGAL, "CancelValue not supported");
        JposDevice.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.subtractValue(sequenceNumber, timeout), "SubtractValue");
    }

    @Override
    public void transactionAccess(int control) throws JposException {
        long[] validvalues = {ElectronicValueRWConst.EVRW_TA_TRANSACTION, ElectronicValueRWConst.EVRW_TA_NORMAL};

        logPreCall("TransactionAccess", "" + control);
        checkBusy();
        JposDevice.check(!Data.CapTransaction, JposConst.JPOS_E_ILLEGAL, "TransactionAccess not supported");
        JposDevice.checkMember(control, validvalues, JposConst.JPOS_E_ILLEGAL, "Invalid control value: " + control);
        JposDevice.check(control == ElectronicValueRWConst.EVRW_TA_TRANSACTION && TransactionCommand != null, JposConst.JPOS_E_ILLEGAL, "Starting transaction within transaction not supported");
        JposDevice.check(control == ElectronicValueRWConst.EVRW_TA_NORMAL && TransactionCommand == null, JposConst.JPOS_E_ILLEGAL, "Ending transaction from outside a transaction not possible");
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
        Device.check(!Data.CapUnlockTerminal, JposConst.JPOS_E_ILLEGAL, "UnlockTerminal not supported");
        callIt(ElectronicValueRW.unlockTerminal(), "UnlockTerminal");
    }

    @Override
    public void unregisterServiceToMedium(int sequenceNumber, int timeout) throws JposException {
        logPreCall("UnregisterServiceToMedium", "" + sequenceNumber + ", " + timeout);
        checkBusy();
        JposDevice.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(ElectronicValueRW.unregisterServiceToMedium(sequenceNumber, timeout), "UnregisterServiceToMedium");
    }

    @Override
    public void updateData(int dataType, int[] data, Object[] obj) throws JposException {
        String intsStr = ", (null)";
        String objectsStr = ", (null)";
        long[] validvalues = {ElectronicValueRWConst.EVRW_AD_KEY, ElectronicValueRWConst.EVRW_AD_NEGATIVE_LIST, ElectronicValueRWConst.EVRW_AD_OTHERS};

        if (data != null)
            intsStr = data.length == 0 ? ", (no int)" : (data.length == 1 ? ", " + data[0] : ", (multiple int)");
        if (obj != null)
            objectsStr = obj.length == 0 ? ", (no Object)" : (obj.length == 1 ? (obj[0] == null ? ", (null)" : obj[0].toString()) : ", (multiple Object)");
        logPreCall("UpdateData", "" + dataType + intsStr + objectsStr);
        checkBusy();
        JposDevice.checkMember(dataType, validvalues, JposConst.JPOS_E_ILLEGAL, "Invalid dataType: " + dataType);
        JposDevice.check(data == null || data.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid data");
        JposDevice.check(obj == null || obj.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid obj");
        callIt(ElectronicValueRW.updateData(dataType, data, obj), "UpdateData");
    }

    @Override
    public void updateKey(int[] data, Object[] obj) throws JposException {
        String intsStr = "(null)";
        String objectsStr = ", (null)";

        if (data != null)
            intsStr = data.length == 0 ? "(no int)" : (data.length == 1 ? "" + data[0] : "(multiple int)");
        if (obj != null)
            objectsStr = obj.length == 0 ? ", (no Object)" : (obj.length == 1 ? (obj[0] == null ? ", (null)" : obj[0].toString()) : ", (multiple Object)");
        logPreCall("UpdateKey", intsStr + objectsStr);
        checkBusy();
        JposDevice.check(data == null || data.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid data");
        JposDevice.check(obj == null || obj.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid obj");
        callIt(ElectronicValueRW.updateKey(data, obj), "UpdateKey");
    }

    @Override
    public void writeValue(int sequenceNumber, int timeout) throws JposException {
        logPreCall("WriteValue", "" + sequenceNumber + ", " + timeout);
        checkBusy();
        JposDevice.check(!Data.CapWriteValue, JposConst.JPOS_E_ILLEGAL, "WriteValue not supported");
        JposDevice.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
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
