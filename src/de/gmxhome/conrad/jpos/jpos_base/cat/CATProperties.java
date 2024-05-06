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

import static jpos.CATConst.*;

/**
 * Class containing the CAT specific properties, their default values and default implementations of
 * CATInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter CAT - Credit
 * Authorization Terminal.
 */
public class CATProperties extends JposCommonProperties implements CATInterface {
    /**
     * UPOS property AccountNumber.
     */
    public String AccountNumber;

    /**
     * UPOS property AdditionalSecurityInformation. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String AdditionalSecurityInformation;

    /**
     * UPOS property ApprovalCode.
     */
    public String ApprovalCode;

    /**
     * UPOS property Balance. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public long Balance = 0;

    /**
     * UPOS property CapAdditionalSecurityInformation. Default: true. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapAdditionalSecurityInformation = true;

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
    public int CapDailyLog = CAT_DL_NONE;

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
     * UPOS property CapPaymentDetail. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapPaymentDetail = false;

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
     * UPOS property CardCompanyID.
     */
    public String CardCompanyID;

    /**
     * UPOS property CenterResultCode.
     */
    public String CenterResultCode;

    /**
     * UPOS property DailyLog. Default: "". Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String DailyLog = "";

    /**
     * UPOS property LogStatus. Default: LOGSTATUS_OK. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int LogStatus = CAT_LOGSTATUS_OK;

    /**
     * UPOS property PaymentCondition. Default: PAYMENT_DEBIT. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int PaymentCondition = CAT_PAYMENT_DEBIT;

    /**
     * UPOS property PaymentDetail.
     */
    public String PaymentDetail;

    /**
     * UPOS property PaymentMedia.
     */
    public int PaymentMedia;

    /**
     * UPOS property SequenceNumber.
     */
    public int SequenceNumber;

    /**
     * UPOS property SettledAmount. Default: 0. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public long SettledAmount = 0;

    /**
     * UPOS property SlipNumber.
     */
    public String SlipNumber;

    /**
     * UPOS property TrainingMode.
     */
    public boolean TrainingMode;

    /**
     * UPOS property TransactionNumber.
     */
    public String TransactionNumber;

    /**
     * UPOS property TransactionType.
     */
    public int TransactionType;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    public CATProperties(int dev) {
        super(dev);
    }

    @Override
    public void initOnOpen() {
        super.initOnOpen();
        AccountNumber = "";
        ApprovalCode = "";
        CardCompanyID = "";
        CenterResultCode = "";
        PaymentDetail = "";
        PaymentMedia = CAT_MEDIA_UNSPECIFIED;
        SequenceNumber = 0;
        SlipNumber = "";
        TrainingMode = false;
        TransactionNumber = "";
        TransactionType = 0;
    }

    @Override
    public void additionalSecurityInformation(String addInfo) throws JposException {
        AdditionalSecurityInformation = addInfo;
    }

    @Override
    public void paymentMedia(int media) throws JposException {
        PaymentMedia = media;
    }

    @Override
    public void trainingMode(boolean flag) throws JposException {
        TrainingMode = flag;
    }

    @Override
    public AccessDailyLog accessDailyLog(int sequenceNumber, int type, int timeout) throws JposException {
        return new AccessDailyLog(this, sequenceNumber, type, timeout);
    }

    @Override
    public void accessDailyLog(AccessDailyLog request) throws JposException {
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
    public LockTerminal lockTerminal() throws JposException {
        return new LockTerminal(this);
    }

    @Override
    public void lockTerminal(LockTerminal request) throws JposException {
    }

    @Override
    public UnlockTerminal unlockTerminal() throws JposException {
        return new UnlockTerminal(this);
    }

    @Override
    public void unlockTerminal(UnlockTerminal request) throws JposException {
    }
}
