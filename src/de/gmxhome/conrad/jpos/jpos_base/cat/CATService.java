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
import jpos.*;
import jpos.services.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.CATConst.*;
import static jpos.JposConst.*;

/**
 * CAT service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class CATService extends JposBase implements CATService116 {
    /**
     * Instance of a class implementing the CATInterface for credit authorization terminal specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public CATInterface CatInterface;

    private final CATProperties Data;

    static private final long[] PaymentMedia = { CAT_MEDIA_UNSPECIFIED, CAT_MEDIA_CREDIT, CAT_MEDIA_DEBIT, CAT_MEDIA_ELECTRONIC_MONEY };

    static private final long[] DailyLogType = { CAT_DL_REPORTING, CAT_DL_SETTLEMENT };

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public CATService(CATProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public String getAccountNumber() throws JposException {
        checkOpened();
        logGet("AccountNumber");
        return Data.AccountNumber;
    }

    @Override
    public String getAdditionalSecurityInformation() throws JposException {
        checkOpened();
        logGet("AdditionalSecurityInformation");
        return Data.AdditionalSecurityInformation;
    }

    @Override
    public void setAdditionalSecurityInformation(String addInfo) throws JposException {
        logPreSet("AdditionalSecurityInformation");
        if (addInfo == null)
            addInfo = "";
        checkOpened();
        check(!Data.CapAdditionalSecurityInformation, JPOS_E_ILLEGAL, "Device does not support AdditionalSecurityInformation");
        checkNoChangedOrClaimed(Data.AdditionalSecurityInformation, addInfo);
        CatInterface.additionalSecurityInformation(addInfo);
        logSet("AdditionalSecurityInformation");
    }

    @Override
    public String getApprovalCode() throws JposException {
        checkOpened();
        logGet("ApprovalCode");
        return Data.ApprovalCode;
    }

    @Override
    public long getBalance() throws JposException {
        checkOpened();
        logGet("Balance");
        return Data.Balance;
    }

    @Override
    public boolean getCapAdditionalSecurityInformation() throws JposException {
        checkOpened();
        logGet("CapAdditionalSecurityInformation");
        return Data.CapAdditionalSecurityInformation;
    }

    @Override
    public boolean getCapAuthorizeCompletion() throws JposException {
        checkOpened();
        logGet("CapAuthorizeCompletion");
        return Data.CapAuthorizeCompletion;
    }

    @Override
    public boolean getCapAuthorizePreSales() throws JposException {
        checkOpened();
        logGet("CapAuthorizePreSales");
        return Data.CapAuthorizePreSales;
    }

    @Override
    public boolean getCapAuthorizeRefund() throws JposException {
        checkOpened();
        logGet("CapAuthorizeRefund");
        return Data.CapAuthorizeRefund;
    }

    @Override
    public boolean getCapAuthorizeVoid() throws JposException {
        checkOpened();
        logGet("CapAuthorizeVoid");
        return Data.CapAuthorizeVoid;
    }

    @Override
    public boolean getCapAuthorizeVoidPreSales() throws JposException {
        checkOpened();
        logGet("CapAuthorizeVoidPreSales");
        return Data.CapAuthorizeVoidPreSales;
    }

    @Override
    public boolean getCapCashDeposit() throws JposException {
        checkOpened();
        logGet("CapCashDeposit");
        return Data.CapCashDeposit;
    }

    @Override
    public boolean getCapCenterResultCode() throws JposException {
        checkOpened();
        logGet("CapCenterResultCode");
        return Data.CapCenterResultCode;
    }

    @Override
    public boolean getCapCheckCard() throws JposException {
        checkOpened();
        logGet("CapCheckCard");
        return Data.CapCheckCard;
    }

    @Override
    public int getCapDailyLog() throws JposException {
        checkOpened();
        logGet("CapDailyLog");
        return Data.CapDailyLog;
    }

    @Override
    public boolean getCapInstallments() throws JposException {
        checkOpened();
        logGet("CapInstallments");
        return Data.CapInstallments;
    }

    @Override
    public boolean getCapLockTerminal() throws JposException {
        checkOpened();
        logGet("CapLockTerminal");
        return Data.CapLockTerminal;
    }

    @Override
    public boolean getCapLogStatus() throws JposException {
        checkOpened();
        logGet("CapLogStatus");
        return Data.CapLogStatus;
    }

    @Override
    public boolean getCapPaymentDetail() throws JposException {
        checkOpened();
        logGet("CapPaymentDetail");
        return Data.CapPaymentDetail;
    }

    @Override
    public boolean getCapTaxOthers() throws JposException {
        checkOpened();
        logGet("CapTaxOthers");
        return Data.CapTaxOthers;
    }

    @Override
    public boolean getCapTrainingMode() throws JposException {
        checkOpened();
        logGet("CapTrainingMode");
        return Data.CapTrainingMode;
    }

    @Override
    public boolean getCapTransactionNumber() throws JposException {
        checkOpened();
        logGet("CapTransactionNumber");
        return Data.CapTransactionNumber;
    }

    @Override
    public boolean getCapUnlockTerminal() throws JposException {
        checkOpened();
        logGet("CapUnlockTerminal");
        return Data.CapUnlockTerminal;
    }

    @Override
    public String getCardCompanyID() throws JposException {
        checkOpened();
        logGet("CardCompanyID");
        return Data.CardCompanyID;
    }

    @Override
    public String getCenterResultCode() throws JposException {
        checkOpened();
        logGet("CenterResultCode");
        return Data.CenterResultCode;
    }

    @Override
    public String getDailyLog() throws JposException {
        checkOpened();
        logGet("DailyLog");
        return Data.DailyLog;
    }

    @Override
    public int getLogStatus() throws JposException {
        checkOpened();
        logGet("LogStatus");
        return Data.LogStatus;
    }

    @Override
    public int getPaymentCondition() throws JposException {
        checkOpened();
        logGet("PaymentCondition");
        return Data.PaymentCondition;
    }

    @Override
    public String getPaymentDetail() throws JposException {
        checkOpened();
        logGet("PaymentDetail");
        return Data.PaymentDetail;
    }

    @Override
    public int getPaymentMedia() throws JposException {
        checkOpened();
        logGet("PaymentMedia");
        return Data.PaymentMedia;
    }

    @Override
    public void setPaymentMedia(int media) throws JposException {
        logPreSet("PaymentMedia");
        checkOpened();
        checkMember(media, PaymentMedia, JPOS_E_ILLEGAL, "Invalid medium: " + media);
        checkNoChangedOrClaimed(Data.PaymentMedia, media);
        CatInterface.paymentMedia(media);
        logSet("PaymentMedia");
    }

    @Override
    public int getSequenceNumber() throws JposException {
        checkOpened();
        logGet("SequenceNumber");
        return Data.SequenceNumber;
    }

    @Override
    public long getSettledAmount() throws JposException {
        checkOpened();
        logGet("SettledAmount");
        return Data.SettledAmount;
    }

    @Override
    public String getSlipNumber() throws JposException {
        checkOpened();
        logGet("SlipNumber");
        return Data.SlipNumber;
    }

    @Override
    public boolean getTrainingMode() throws JposException {
        checkOpened();
        logGet("TrainingMode");
        return Data.TrainingMode;
    }

    @Override
    public void setTrainingMode(boolean flag) throws JposException {
        logPreSet("TrainingMode");
        checkOpened();
        check(!Data.CapTrainingMode && flag, JPOS_E_ILLEGAL, "Device does not support TrainingMode");
        checkNoChangedOrClaimed(Data.TrainingMode, flag);
        CatInterface.trainingMode(flag);
        logSet("TrainingMode");
    }

    @Override
    public String getTransactionNumber() throws JposException {
        checkOpened();
        logGet("TransactionNumber");
        return Data.TransactionNumber;
    }

    @Override
    public String getTransactionType() throws JposException {
        checkOpened();
        logGet("TransactionType");
        return String.valueOf(Data.TransactionType);
    }

    @Override
    public void accessDailyLog(int sequenceNumber, int type, int timeout) throws JposException {
        logPreCall("AccessDailyLog", removeOuterArraySpecifier(new Object[]{sequenceNumber, type, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        checkMember(type, DailyLogType, JPOS_E_ILLEGAL, "Invalid log type: " + type);
        check((type & ~Data.CapDailyLog) != 0, JPOS_E_ILLEGAL, "Invalid log type: " + type);
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(CatInterface.accessDailyLog(sequenceNumber, type, timeout), "AccessDailyLog");
    }

    @Override
    public void authorizeCompletion(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
        logPreCall("AuthorizeCompletion", removeOuterArraySpecifier(new Object[]{
                sequenceNumber, amount, taxOthers, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        check(!Data.CapAuthorizeCompletion, JPOS_E_ILLEGAL, "AuthorizeCompletion not supported");
        check(amount <= 0, JPOS_E_ILLEGAL, "Invalid amount: " + amount);
        check(taxOthers < 0, JPOS_E_ILLEGAL, "Invalid taxOthers: " + taxOthers);
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(CatInterface.authorizeCompletion(sequenceNumber, amount, taxOthers, timeout), "AuthorizeCompletion");
    }

    @Override
    public void authorizePreSales(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
        logPreCall("AuthorizePreSales", removeOuterArraySpecifier(new Object[]{
                sequenceNumber, amount, taxOthers, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        check(!Data.CapAuthorizePreSales, JPOS_E_ILLEGAL, "AuthorizePreSales not supported");
        check(amount <= 0, JPOS_E_ILLEGAL, "Invalid amount: " + amount);
        check(taxOthers < 0, JPOS_E_ILLEGAL, "Invalid taxOthers: " + taxOthers);
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(CatInterface.authorizePreSales(sequenceNumber, amount, taxOthers, timeout), "AuthorizePreSales");
    }

    @Override
    public void authorizeRefund(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
        logPreCall("AuthorizeRefund", removeOuterArraySpecifier(new Object[]{
                sequenceNumber, amount, taxOthers, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        check(!Data.CapAuthorizeRefund, JPOS_E_ILLEGAL, "AuthorizeRefund not supported");
        check(amount <= 0, JPOS_E_ILLEGAL, "Invalid amount: " + amount);
        check(taxOthers < 0, JPOS_E_ILLEGAL, "Invalid taxOthers: " + taxOthers);
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(CatInterface.authorizeRefund(sequenceNumber, amount, taxOthers, timeout), "AuthorizeRefund");
    }

    @Override
    public void authorizeSales(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
        logPreCall("AuthorizeSales", removeOuterArraySpecifier(new Object[]{
                sequenceNumber, amount, taxOthers, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        check(amount <= 0, JPOS_E_ILLEGAL, "Invalid amount: " + amount);
        check(taxOthers < 0, JPOS_E_ILLEGAL, "Invalid taxOthers: " + taxOthers);
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(CatInterface.authorizeSales(sequenceNumber, amount, taxOthers, timeout), "AuthorizeSales");
    }

    @Override
    public void authorizeVoid(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
        logPreCall("AuthorizeVoid", removeOuterArraySpecifier(new Object[]{
                sequenceNumber, amount, taxOthers, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        check(!Data.CapAuthorizeVoid, JPOS_E_ILLEGAL, "AuthorizeVoid not supported");
        check(amount <= 0, JPOS_E_ILLEGAL, "Invalid amount: " + amount);
        check(taxOthers < 0, JPOS_E_ILLEGAL, "Invalid taxOthers: " + taxOthers);
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(CatInterface.authorizeVoid(sequenceNumber, amount, taxOthers, timeout), "AuthorizeVoid");
    }

    @Override
    public void authorizeVoidPreSales(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
        logPreCall("AuthorizeVoidPreSales", removeOuterArraySpecifier(new Object[]{
                sequenceNumber, amount, taxOthers, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        check(!Data.CapAuthorizeVoidPreSales, JPOS_E_ILLEGAL, "AuthorizeVoidPreSales not supported");
        check(amount <= 0, JPOS_E_ILLEGAL, "Invalid amount: " + amount);
        check(taxOthers < 0, JPOS_E_ILLEGAL, "Invalid taxOthers: " + taxOthers);
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(CatInterface.authorizeVoidPreSales(sequenceNumber, amount, taxOthers, timeout), "AuthorizeVoidPreSales");
    }

    @Override
    public void cashDeposit(int sequenceNumber, long amount, int timeout) throws JposException {
        logPreCall("CashDeposit", removeOuterArraySpecifier(new Object[]{
                sequenceNumber, amount, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        check(!Data.CapCashDeposit, JPOS_E_ILLEGAL, "CashDeposit not supported");
        check(amount <= 0, JPOS_E_ILLEGAL, "Invalid amount: " + amount);
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(CatInterface.cashDeposit(sequenceNumber, amount, timeout), "CashDeposit");
    }

    @Override
    public void checkCard(int sequenceNumber, int timeout) throws JposException {
        logPreCall("CheckCard", removeOuterArraySpecifier(new Object[]{sequenceNumber, timeout}, Device.MaxArrayStringElements));
        checkBusy();
        check(!Data.CapCheckCard, JPOS_E_ILLEGAL, "CheckCard not supported");
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        callIt(CatInterface.checkCard(sequenceNumber, timeout), "CheckCard");
    }

    @Override
    public void lockTerminal() throws JposException {
        logPreCall("LockTerminal");
        checkBusy();
        check(!Data.CapLockTerminal, JPOS_E_ILLEGAL, "LockTerminal not supported");
        callIt(CatInterface.lockTerminal(), "LockTerminal");
    }

    @Override
    public void unlockTerminal() throws JposException {
        logPreCall("UnlockTerminal");
        checkBusy();
        check(!Data.CapUnlockTerminal, JPOS_E_ILLEGAL, "UnlockTerminal not supported");
        callIt(CatInterface.unlockTerminal(), "UnlockTerminal");
    }

    private void callIt(OutputRequest request, String name) throws JposException {
        if (callNowOrLater(request))
            logAsyncCall(name);
        else
            logCall(name);
    }
}
