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
import jpos.config.JposEntry;

import java.text.*;
import java.util.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static de.gmxhome.conrad.jpos.jpos_base.electronicvaluerw.ElectronicValueRWService.BoolTags;
import static de.gmxhome.conrad.jpos.jpos_base.electronicvaluerw.ElectronicValueRWService.CurrencyTags;
import static de.gmxhome.conrad.jpos.jpos_base.electronicvaluerw.ElectronicValueRWService.DateTimeTags;
import static de.gmxhome.conrad.jpos.jpos_base.electronicvaluerw.ElectronicValueRWService.EnumTagsValues;
import static de.gmxhome.conrad.jpos.jpos_base.electronicvaluerw.ElectronicValueRWService.NumberTags;
import static jpos.ElectronicValueRWConst.*;
import static jpos.JposConst.*;

/**
 * Class containing the electronic value reader / writer specific properties, their default values and default implementations of
 * ElectronicValueRWInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Electronic Value Reader / Writer.
 */
public class ElectronicValueRWProperties extends JposCommonProperties implements ElectronicValueRWInterface {
    /**
     * UPOS property AccountNumber.
     */
    public String AccountNumber;

    /**
     * UPOS property AdditionalSecurityInformation.
     */
    public String AdditionalSecurityInformation;

    /**
     * UPOS property Amount.
     */
    public long Amount;

    /**
     * UPOS property ApprovalCode.
     */
    public String ApprovalCode;

    /**
     * UPOS property Balance.
     */
    public long Balance;

    /**
     * UPOS property BalanceOfPoint.
     */
    public long BalanceOfPoint;

    /**
     * UPOS property CapActivateService. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapActivateService = false;

    /**
     * UPOS property CapAdditionalSecurityInformation. Default: true. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapAdditionalSecurityInformation = true;

    /**
     * UPOS property CapAddValue. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapAddValue = false;

    /**
     * UPOS property CapAuthorizeCompletion. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapAuthorizeCompletion = false;

    /**
     * UPOS property CapAuthorizePreSales. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapAuthorizePreSales = false;

    /**
     * UPOS property CapAuthorizeRefund. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapAuthorizeRefund = false;

    /**
     * UPOS property CapAuthorizeVoid. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapAuthorizeVoid = false;

    /**
     * UPOS property CapAuthorizeVoidPreSales. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapAuthorizeVoidPreSales = false;

    /**
     * UPOS property CapCancelValue. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapCancelValue = false;

    /**
     * UPOS property CapCardSensor. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapCardSensor = 0;

    /**
     * UPOS property CapCashDeposit. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapCashDeposit = false;

    /**
     * UPOS property CapCenterResultCode. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapCenterResultCode = false;

    /**
     * UPOS property CapCheckCard. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapCheckCard = false;

    /**
     * UPOS property CapDailyLog. Default: DL_NONE. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapDailyLog = ElectronicValueRWConst.EVRW_DL_NONE;

    /**
     * UPOS property CapDetectionControl. Default: CDC_RWCONTROL. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapDetectionControl = ElectronicValueRWConst.EVRW_CDC_RWCONTROL;

    /**
     * UPOS property CapElectronicMoney. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapElectronicMoney = false;

    /**
     * UPOS property CapEnumerateCardServices. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapEnumerateCardServices = false;

    /**
     * UPOS property CapIndirectTransactionLog. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapIndirectTransactionLog = false;

    /**
     * UPOS property CapInstallments. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapInstallments = false;

    /**
     * UPOS property CapLockTerminal. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapLockTerminal = false;

    /**
     * UPOS property CapLogStatus. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapLogStatus = false;

    /**
     * UPOS property CapMediumID. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapMediumID = false;

    /**
     * UPOS property CapMembershipCertificate. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapMembershipCertificate = false;

    /**
     * UPOS property CapPaymentDetail. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapPaymentDetail = false;

    /**
     * UPOS property CapPINDevice. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapPINDevice = false;

    /**
     * UPOS property CapPoint. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapPoint = false;

    /**
     * UPOS property CapSubtractValue. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapSubtractValue = false;

    /**
     * UPOS property CapTaxOthers. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapTaxOthers = false;

    /**
     * UPOS property CapTrainingMode. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapTrainingMode = false;

    /**
     * UPOS property CapTransaction. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapTransaction = false;

    /**
     * UPOS property CapTransactionLog. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapTransactionLog = false;

    /**
     * UPOS property CapTransactionNumber. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapTransactionNumber = false;

    /**
     * UPOS property CapUnlockTerminal. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapUnlockTerminal = false;

    /**
     * UPOS property CapUpdateKey. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapUpdateKey = false;

    /**
     * UPOS property CapVoucher. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapVoucher = false;

    /**
     * UPOS property CapWriteValue. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapWriteValue = false;

    /**
     * UPOS property CardCompanyID.
     */
    public String CardCompanyID;

    /**
     * UPOS property CardServiceList.
     */
    public String CardServiceList;

    /**
     * UPOS property CenterResultCode.
     */
    public String CenterResultCode;

    /**
     * UPOS property CurrentService.
     */
    public String CurrentService;

    /**
     * UPOS property DailyLog.
     */
    public String DailyLog;

    /**
     * UPOS property DetectionControl.
     */
    public boolean DetectionControl;

    /**
     * UPOS property DetectionStatus.
     */
    public int DetectionStatus;

    /**
     * UPOS property ExpirationDate.
     */
    public String ExpirationDate;

    /**
     * UPOS property LastUsedDate.
     */
    public String LastUsedDate = "";

    /**
     * UPOS property LogStatus. Default: null. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public Integer LogStatus = null;

    /**
     * UPOS property MediumID.
     */
    public String MediumID;

    /**
     * UPOS property PaymentCondition. Default: null. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public Integer PaymentCondition = null;

    /**
     * UPOS property PaymentDetail. Default: null. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String PaymentDetail = null;

    /**
     * UPOS property PaymentMedia.
     */
    public int PaymentMedia;

    /**
     * UPOS property PINEntry.
     */
    public int PINEntry;

    /**
     * UPOS property Point.
     */
    public long Point;

    /**
     * UPOS property ReaderWriterServiceList. Default: null. Must be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String ReaderWriterServiceList = null;

    /**
     * UPOS property SequenceNumber.
     */
    public int SequenceNumber;

    /**
     * UPOS property ServiceType.
     */
    public int ServiceType;

    /**
     * UPOS property SettledAmount.
     */
    public long SettledAmount;

    /**
     * UPOS property SettledPoint.
     */
    public long SettledPoint;

    /**
     * UPOS property SlipNumber.
     */
    public String SlipNumber;

    /**
     * UPOS property TrainingModeState.
     */
    public int TrainingModeState;

    /**
     * UPOS property TransactionLog.
     */
    public String TransactionLog;

    /**
     * UPOS property TransactionNumber.
     */
    public String TransactionNumber;

    /**
     * UPOS property TransactionType.
     */
    public int TransactionType;

    /**
     * UPOS property VoucherID.
     */
    public String VoucherID;

    /**
     * UPOS property VoucherIDList.
     */
    public String VoucherIDList;

    /**
     * Tag names and values set by setParameterInformation. Will be initialized to an empty Map during claim and by
     * invocation of method clearParameterInformation. Deprecated because replaced by TypedParameters.
     */
    @Deprecated
    public Map<String, String> Parameters;

    /**
     * Tag names and values set by setParameterInformation. Will be initialized to an empty Map during claim and by
     * invocation of method clearParameterInformation.
     */
    public TypeSafeStringMap TypedParameters;

    /**
     * Tag names and values hold for method retrieveResultInformation. Must be initialized to an empty Map whenever
     * the service has read a new card. Automatic initialized to an empty Map by this framework will occur during
     * successful claim and endDetection. This implies that initialization by the service is not necessary during
     * the first card reading after claim and whenever the card detection process has been controlled by methods
     * beginDetection and endDetection.
     */
    @Deprecated
    public Map<String, String> Results;

    /**
     * Tag names and values hold for method retrieveResultInformation. Must be initialized to an empty Map whenever
     * the service has read a new card. Automatic initialized to an empty Map by this framework will occur during
     * successful claim and endDetection. This implies that initialization by the service is not necessary during
     * the first card reading after claim and whenever the card detection process has been controlled by methods
     * beginDetection and endDetection.
     */
    public TypeSafeStringMap TypedResults;

    /**
     * Specifies whether enumerated values will be passed by value (true) or by name (false). Default is true.
     */
    public boolean UseEnumeratedValues = true;

    /**
     * Specifies whether enumeration values will be checked against the list of enumerations specified in the corresponding
     * UPOS specification (true) or not (false). In the latter case, any value can be passed for an enumeration value
     * and the specific service implementation must decide whether the given value shall be supported. Default is true.
     */
    public boolean StrongEnumerationCheck = true;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected ElectronicValueRWProperties(int dev) {
        super(dev);
        ExclusiveUse = ExclusiveYes;
    }

    @Override
    public void checkProperties(JposEntry entry) throws JposException {
        super.checkProperties(entry);
        Object o = entry.getPropertyValue("UseEnumeratedValues");
        if (o != null)
            UseEnumeratedValues = Boolean.parseBoolean(o.toString());
        o = entry.getPropertyValue("StrongEnumerationCheck");
        if (o != null)
            StrongEnumerationCheck = Boolean.parseBoolean(o.toString());
    }

    @Override
    public void initOnOpen() {
        super.initOnOpen();
        AccountNumber = "";
        AdditionalSecurityInformation = "";
        Amount = 0;
        ApprovalCode = "";
        Balance = 0;
        BalanceOfPoint = 0;
        CardCompanyID = "";
        CardServiceList = "";
        CenterResultCode = "";
        CurrentService = "";
        DailyLog = "";
        DetectionControl = false;
        DetectionStatus = EVRW_DS_NOCARD;
        ExpirationDate = "";
        LastUsedDate = "";
        MediumID = "";
        PaymentDetail = "";
        PaymentMedia = EVRW_MEDIA_UNSPECIFIED;
        PINEntry = EVRW_PIN_ENTRY_UNKNOWN;
        Point = 0;
        SequenceNumber = 0;
        ServiceType = EVRW_ST_UNSPECIFIED;
        SettledAmount = 0;
        SettledPoint = 0;
        SlipNumber = "";
        TrainingModeState = EVRW_TM_UNKNOWN;
        TransactionLog = "";
        TransactionNumber = "";
        TransactionType = 0;
        VoucherID = "";
        VoucherIDList = "";
    }

    @Override
    @SuppressWarnings("SynchronizeOnNonFinalField")
    public void initOnClaim() {
        super.initOnClaim();
        synchronized (TypedParameters) {
            TypedParameters.clear();
        }
        synchronized (TypedResults) {
            TypedResults.clear();
        }
    }

    /**
     * Method to verify whether the format of a value matches the format specified for the given tag. See the UPOS
     * specification, chapter 15.5.31 retrieveResultInformation Method for the general layout.
     * @param tag       tag name.
     * @param value     value to be checked.
     * @return          JposException if value is invalid for tag, null if format of value matches the specification.
     */
    public JposException checkTagValueFormat(String tag, String value) {
        try {
            if (!checkCurrencyTag(tag, value) && !checkBoolenTag(tag, value) && !checkNumberTag(tag, value)
                    && !checkDateTimeTag(tag, value) && !checkEnumTag(tag, value)) {
                checkStringTag(tag, value);
            }
            return null;
        } catch (JposException e) {
            return e;
        }
    }

    private boolean checkCurrencyTag(String tag, String value) throws JposException {
        for(String name : CurrencyTags) {
            if (name.equals(tag)) {
                try {
                    Long.parseLong(value);
                    return true;
                } catch (NumberFormatException e) {
                    throw new JposException(JPOS_E_ILLEGAL, "Bad format for tag name " + tag + ": " + value, e);
                }
            }
        }
        return false;
    }

    private boolean checkEnumTag(String tag, String value) throws JposException {
        for (int i = 0; i < EnumTagsValues.length; i += 2) {
            if (EnumTagsValues[0].equals(tag)) {
                for (Object val : (Object[])EnumTagsValues[i + 1]) {
                    if (val.equals(value) || (val instanceof Integer && Integer.toString((int) val).equals(value)))
                        return true;
                }
                throw new JposException(JPOS_E_ILLEGAL, "Bad format for tag name " + tag + ": " + value);
            }
        }
        return false;
    }

    private boolean checkBoolenTag(String tag, String value) throws JposException {
        for(String name : BoolTags) {
            if (name.equals(tag)) {
                if (value.equals("True") || value.equals("False")) {
                    return true;
                } else {
                    throw new JposException(JPOS_E_ILLEGAL, "Bad format for tag name " + tag + ": " + value);
                }
            }
        }
        return false;
    }

    private boolean checkNumberTag(String tag, String value) throws JposException {
        for(String name : NumberTags) {
            if (name.equals(tag)) {
                try {
                    Integer.parseInt(value);
                    return true;
                } catch (NumberFormatException e) {
                    throw new JposException(JPOS_E_ILLEGAL, "Bad format for tag name " + tag + ": " + value, e);
                }
            }
        }
        return false;
    }

    private boolean checkDateTimeTag(String tag, String value) throws JposException {
        for(String name : DateTimeTags) {
            if (name.equals(tag)) {
                try {
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(value);
                    return true;
                } catch (ParseException e) {
                    throw new JposException(JPOS_E_ILLEGAL, "Bad format for tag name " + tag + ": " + value, e);
                }
            }
        }
        return false;
    }

    @SuppressWarnings("UnusedReturnValue")
    private boolean checkStringTag(String tag, String value) throws JposException {
        if (tag.equals("SetttledVoucherID") || tag.equals("SettledVoucherID") || tag.equals("VoucherID")) {
            if (value.length() > 0) {
                String[] parts = value.split(":");
                int i;
                try {
                    i = Integer.parseInt(parts[1]);
                    check(i <= 0 || parts.length != 2, JPOS_E_ILLEGAL, "Bad format for tag name " + tag + ": " + value);
                    return true;
                } catch (Exception e) {
                    throw new JposException(JPOS_E_ILLEGAL, "Bad format for tag name " + tag + ": " + value, e);
                }
            }
        } else if (tag.equals("VoucherIDList")) {
            if (value.length() > 0) {
                String[] ids = value.split(",");
                for (String id : ids) {
                    String[] parts = id.split(":");
                    int i;
                    try {
                        i = Integer.parseInt(parts[1]);
                        check(i <= 0 || parts.length != 2, JPOS_E_ILLEGAL, "Bad format for tag name " + tag + ": " + value);
                        return true;
                    } catch (Exception e) {
                        throw new JposException(JPOS_E_ILLEGAL, "Bad format for tag name " + tag + ": " + value, e);
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void additionalSecurityInformation(String addInfo) throws JposException {
        AdditionalSecurityInformation = addInfo;
    }

    @Override
    public void amount(long amount) throws JposException {
        Amount = amount;
    }

    @Override
    public void approvalCode(String code) throws JposException {
        ApprovalCode = code;
    }

    @Override
    public void currentService(String service) throws JposException {
        CurrentService = service;
    }

    @Override
    public void detectionControl(boolean flag) throws JposException {
        DetectionControl = false;
    }

    @Override
    public void mediumID(String id) throws JposException {
        MediumID = id;
    }

    @Override
    public void paymentMedia(int media) throws JposException {
        PaymentMedia = media;
    }

    @Override
    public void PINEntry(int value) throws JposException {
        PINEntry = value;
    }

    @Override
    public void point(long count) throws JposException {
        Point = count;
    }

    @Override
    public void trainingModeState(int state) throws JposException {
        TrainingModeState = state;
    }

    @Override
    public void voucherID(String id) throws JposException {
        VoucherID = id;
    }

    @Override
    public void voucherIDList(String ids) throws JposException {
        VoucherIDList = ids;
    }

    @Override
    public void beginDetection(int type, int timeout) throws JposException {

    }

    @Override
    public void beginRemoval(int timeout) throws JposException {

    }

    @Override
    public void captureCard() throws JposException {

    }

    @SuppressWarnings("SynchronizeOnNonFinalField")
    @Override
    public void clearParameterInformation() throws JposException {
        synchronized (TypedParameters) {
            TypedParameters.clear();
        }
    }

    @Override
    public void endDetection() throws JposException {

    }

    @Override
    public void endRemoval() throws JposException {

    }

    @Override
    public void enumerateCardServices() throws JposException {

    }

    @SuppressWarnings({"SynchronizeOnNonFinalField", "deprecation"})
    @Override
    public void retrieveResultInformation(String name, String[] value) throws JposException {
        synchronized (TypedResults) {
            String o = TypedResults.get(name);
            value[0] = (o == null) ? "" : o;
        }
    }

    @Override
    @SuppressWarnings("SynchronizeOnNonFinalField")
    public void setParameterInformation(String name, String value) throws JposException {
        synchronized (TypedResults) {
            try {
                TypedResults.put(name, value);
            } catch (Exception e) {
                throw new JposException(JPOS_E_ILLEGAL, "Parameter error: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
            }
        }
    }

    private final Object[][] TagValueList = {
            {"PaymentCondition"},
            {
                    EVRW_TAG_PC_INSTALLMENT_1, EVRW_TAG_PC_INSTALLMENT_2, EVRW_TAG_PC_INSTALLMENT_3,
                    EVRW_TAG_PC_BONUS_1, EVRW_TAG_PC_BONUS_2, EVRW_TAG_PC_BONUS_3, EVRW_TAG_PC_BONUS_4, EVRW_TAG_PC_BONUS_5,
                    EVRW_TAG_PC_BONUS_COMBINATION_1, EVRW_TAG_PC_BONUS_COMBINATION_2,
                    EVRW_TAG_PC_BONUS_COMBINATION_3, EVRW_TAG_PC_BONUS_COMBINATION_4,
                    EVRW_TAG_PC_LUMP, EVRW_TAG_PC_REVOLVING
            }, {
                    EVRW_PAYMENT_INSTALLMENT_1, EVRW_PAYMENT_INSTALLMENT_2, EVRW_PAYMENT_INSTALLMENT_3,
                    EVRW_PAYMENT_BONUS_1, EVRW_PAYMENT_BONUS_2, EVRW_PAYMENT_BONUS_3, EVRW_PAYMENT_BONUS_4, EVRW_PAYMENT_BONUS_5,
                    EVRW_PAYMENT_BONUS_COMBINATION_1, EVRW_PAYMENT_BONUS_COMBINATION_2,
                    EVRW_PAYMENT_BONUS_COMBINATION_3, EVRW_PAYMENT_BONUS_COMBINATION_4,
                    EVRW_PAYMENT_LUMP, EVRW_PAYMENT_REVOLVING
            }, {"TransactionType"},
            {
                    EVRW_TAG_TT_RETURN, EVRW_TAG_TT_SUBTRACT, EVRW_TAG_TT_CANCEL_SALES,
                    EVRW_TAG_TT_ADD, EVRW_TAG_TT_COMPLETION, EVRW_TAG_TT_PRE_SALES
            }, {
                    EVRW_TRANSACTION_REFUND, EVRW_TRANSACTION_SALES, EVRW_TRANSACTION_VOID,
                    EVRW_TRANSACTION_CASHDEPOSIT, EVRW_TRANSACTION_COMPLETION, EVRW_TRANSACTION_PRESALES
            }
    };

    /**
     * Convert property value into tag value.
     * @param name      Name of the parameter tag, e.g. "PaymentCondition"
     * @param value     Property value.
     * @return The corresponding tag value, null if no corresponding tag value exists or value is invalid.
     */
    public Integer getEnumTagFromPropertyValue(String name, int value) {
        for (int i = 0; i < TagValueList.length; i += 3) {
            if (name.equals(TagValueList[i][0].toString())) {
                for (int j = 0; j < TagValueList[i + 2].length && j < TagValueList[i + 1].length; j++) {
                    if (TagValueList[i + 2][j].equals(value)) {
                        return (Integer)(TagValueList[i + 1][j]);
                    }
                }
                return null;
            }
        }
        return null;
    }

    /**
     * Convert parameter tag value into corresponding property value.
     * @param name     Name of the parameter tag, e.g. "PaymentCondition"
     * @param value    Enumerated number.
     * @return The corresponding property value, null if no corresponding property value exists or if tag or value is invalid.
     */
    @Deprecated
    public Integer getPropertyValueFromEnumTag(String name, int value) {
        for (int i = 0; i < TagValueList.length; i += 3) {
            if (name.equals(TagValueList[i][0].toString())) {
                for (int j = 0; j < TagValueList[i + 1].length && j < TagValueList[i + 2].length; j++) {
                    if (TagValueList[i + 1][j].equals(value)) {
                        return (Integer)(TagValueList[i + 2][j]);
                    }
                }
                return null;
            }
        }
        return null;
    }

    /**
     * Convert parameter tag value into corresponding property value. Deprecated.
     * @param name      Name of the parameter tag, e.g. "PaymentCondition"
     * @param valstr    String representing the corresponding Enumerated number.
     * @return The corresponding property value, null if no corresponding property value exists or if tag or valstr is invalid.
     */
    @Deprecated
    public Integer getPropertyValueFromEnumTag(String name, String valstr) {
        TypeSafeStringMap converter = TypedResults.emptyClone();
        Integer value;
        try {
            converter.put(name, valstr);
            value = converter.getEnumerated(name);
        } catch (Exception e) {
            return null;
        }
        return getPropertyValueFromEnumTag(name, value);
    }

    @Override
    public AccessDailyLog accessDailyLog(int sequenceNumber, int type, int timeout) throws JposException {
        return new AccessDailyLog(this, sequenceNumber, type, timeout);
    }

    @Override
    public void accessDailyLog(AccessDailyLog request) throws JposException {
    }

    @Override
    public AccessData accessData(int dataType, int[] data, Object[] obj) throws JposException {
        return new AccessData(this, dataType, data, obj);
    }

    @Override
    public void accessData(AccessData request) throws JposException {
    }

    @Override
    public AccessLog accessLog(int sequenceNumber, int type, int timeout) throws JposException {
        return new AccessLog(this, sequenceNumber, type, timeout);
    }

    @Override
    public void accessLog(AccessLog request) throws JposException {
    }

    @Override
    public ActivateEVService activateEVService(int[] data, Object[] obj) throws JposException {
        return new ActivateEVService(this, data,obj);
    }

    @Override
    public void activateEVService(ActivateEVService request) throws JposException {
    }

    @Override
    public ActivateService activateService(int[] data, Object[] obj) throws JposException {
        return new ActivateService(this, data, obj);
    }

    @Override
    public void activateService(ActivateService request) throws JposException {
    }

    @Override
    public AddValue addValue(int sequenceNumber, int timeout) throws JposException {
        return new AddValue(this, sequenceNumber, timeout);
    }

    @Override
    public void addValue(AddValue request) throws JposException {
    }

    @Override
    public AuthorizeCompletion authorizeCompletion(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
        return new AuthorizeCompletion(this, sequenceNumber, amount, taxOthers, timeout);
    }

    @Override
    public void authorizeCompletion(AuthorizeCompletion request) throws JposException {
    }

    @Override
    public AuthorizePreSales authorizePreSales(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
        return new AuthorizePreSales(this, sequenceNumber, amount, taxOthers, timeout);
    }

    @Override
    public void authorizePreSales(AuthorizePreSales request) throws JposException {
    }

    @Override
    public AuthorizeRefund authorizeRefund(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
        return new AuthorizeRefund(this, sequenceNumber, amount, taxOthers, timeout);
    }

    @Override
    public void authorizeRefund(AuthorizeRefund request) throws JposException {
    }

    @Override
    public AuthorizeSales authorizeSales(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
        return new AuthorizeSales(this, sequenceNumber, amount, taxOthers, timeout);
    }

    @Override
    public void authorizeSales(AuthorizeSales request) throws JposException {
    }

    @Override
    public AuthorizeVoid authorizeVoid(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
        return new AuthorizeVoid(this, sequenceNumber, amount, taxOthers, timeout);
    }

    @Override
    public void authorizeVoid(AuthorizeVoid request) throws JposException {
    }

    @Override
    public AuthorizeVoidPreSales authorizeVoidPreSales(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
        return new AuthorizeVoidPreSales(this, sequenceNumber, amount, taxOthers, timeout);
    }

    @Override
    public void authorizeVoidPreSales(AuthorizeVoidPreSales request) throws JposException {
    }

    @Override
    public CancelValue cancelValue(int sequenceNumber, int timeout) throws JposException {
        return new CancelValue(this, sequenceNumber, timeout);
    }

    @Override
    public void cancelValue(CancelValue request) throws JposException {
    }

    @Override
    public CashDeposit cashDeposit(int sequenceNumber, long amount, int timeout) throws JposException {
        return new CashDeposit(this, sequenceNumber, amount, timeout);
    }

    @Override
    public void cashDeposit(CashDeposit request) throws JposException {
    }

    @Override
    public CheckCard checkCard(int sequenceNumber, int timeout) throws JposException {
        return new CheckCard(this, sequenceNumber, timeout);
    }

    @Override
    public void checkCard(CheckCard request) throws JposException {
    }

    @Override
    public CheckServiceRegistrationToMedium checkServiceRegistrationToMedium(int sequenceNumber, int timeout) throws JposException {
        return new CheckServiceRegistrationToMedium(this, sequenceNumber, timeout);
    }

    @Override
    public void checkServiceRegistrationToMedium(CheckServiceRegistrationToMedium request) throws JposException {
    }

    @Override
    public CloseDailyEVService closeDailyEVService(int[] data, Object[] obj) throws JposException {
        return new CloseDailyEVService(this, data, obj);
    }

    @Override
    public void closeDailyEVService(CloseDailyEVService request) throws JposException {
    }

    @Override
    public DeactivateEVService deactivateEVService(int[] data, Object[] obj) throws JposException {
        return new DeactivateEVService(this, data, obj);
    }

    @Override
    public void deactivateEVService(DeactivateEVService request) throws JposException {
    }

    @Override
    public LockTerminal lockTerminal() throws JposException {
        return new LockTerminal(this);
    }

    @Override
    public void lockTerminal(LockTerminal request) throws JposException {
    }

    @Override
    public OpenDailyEVService openDailyEVService(int[] data, Object[] obj) throws JposException {
        return new OpenDailyEVService(this, data, obj);
    }

    @Override
    public void openDailyEVService(OpenDailyEVService request) throws JposException {
    }

    @Override
    public QueryLastSuccessfulTransactionResult queryLastSuccessfulTransactionResult() throws JposException {
        return new QueryLastSuccessfulTransactionResult(this);
    }

    @Override
    public void queryLastSuccessfulTransactionResult(QueryLastSuccessfulTransactionResult request) throws JposException {
    }

    @Override
    public ReadValue readValue(int sequenceNumber, int timeout) throws JposException {
        return new ReadValue(this, sequenceNumber, timeout);
    }

    @Override
    public void readValue(ReadValue request) throws JposException {
    }

    @Override
    public RegisterServiceToMedium registerServiceToMedium(int sequenceNumber, int timeout) throws JposException {
        return new RegisterServiceToMedium(this, sequenceNumber, timeout);
    }

    @Override
    public void registerServiceToMedium(RegisterServiceToMedium request) throws JposException {
    }

    @Override
    public SubtractValue subtractValue(int sequenceNumber, int timeout) throws JposException {
        return new SubtractValue(this, sequenceNumber, timeout);
    }

    @Override
    public void subtractValue(SubtractValue request) throws JposException {
    }

    @Override
    public TransactionAccess transactionAccess(int control) throws JposException {
        return new TransactionAccess(this, control);
    }

    @Override
    public void transactionAccess(TransactionAccess request) throws JposException {
    }

    @Override
    public UnlockTerminal unlockTerminal() throws JposException {
        return new UnlockTerminal(this);
    }

    @Override
    public void unlockTerminal(UnlockTerminal request) throws JposException {
    }

    @Override
    public UnregisterServiceToMedium unregisterServiceToMedium(int sequenceNumber, int timeout) throws JposException {
        return new UnregisterServiceToMedium(this, sequenceNumber, timeout);
    }

    @Override
    public void unregisterServiceToMedium(UnregisterServiceToMedium request) throws JposException {
    }

    @Override
    public UpdateData updateData(int dataType, int[] data, Object[] obj) throws JposException {
        return new UpdateData(this, dataType, data, obj);
    }

    @Override
    public void updateData(UpdateData request) throws JposException {
    }

    @Override
    public UpdateKey updateKey(int[] data, Object[] obj) throws JposException {
        return new UpdateKey(this, data, obj);
    }

    @Override
    public void updateKey(UpdateKey request) throws JposException {
    }

    @Override
    public WriteValue writeValue(int sequenceNumber, int timeout) throws JposException {
        return new WriteValue(this, sequenceNumber, timeout);
    }

    @Override
    public void writeValue(WriteValue request) throws JposException {
    }
}
