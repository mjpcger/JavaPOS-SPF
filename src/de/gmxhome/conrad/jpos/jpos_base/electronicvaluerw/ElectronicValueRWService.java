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

    @Override
    public boolean getCapPINDevice() throws JposException {
        return false;
    }

    @Override
    public boolean CapTrainingMode() throws JposException {
        return false;
    }

    @Override
    public int getPINEntry() throws JposException {
        return 0;
    }

    @Override
    public void setPINEntry(int i) throws JposException {

    }

    @Override
    public int getTrainingModeState() throws JposException {
        return 0;
    }

    @Override
    public void setTrainingModeState(int i) throws JposException {

    }

    @Override
    public void clearParameterInformation() throws JposException {

    }

    @Override
    public void queryLastSuccessfulTransactionResult() throws JposException {

    }

    @Override
    public boolean getCapAdditionalSecurityInformation() throws JposException {
        return false;
    }

    @Override
    public boolean getCapAuthorizeCompletion() throws JposException {
        return false;
    }

    @Override
    public boolean getCapAuthorizePreSales() throws JposException {
        return false;
    }

    @Override
    public boolean getCapAuthorizeRefund() throws JposException {
        return false;
    }

    @Override
    public boolean getCapAuthorizeVoid() throws JposException {
        return false;
    }

    @Override
    public boolean getCapAuthorizeVoidPreSales() throws JposException {
        return false;
    }

    @Override
    public boolean getCapCashDeposit() throws JposException {
        return false;
    }

    @Override
    public boolean getCapCenterResultCode() throws JposException {
        return false;
    }

    @Override
    public boolean getCapCheckCard() throws JposException {
        return false;
    }

    @Override
    public int getCapDailyLog() throws JposException {
        return 0;
    }

    @Override
    public boolean getCapInstallments() throws JposException {
        return false;
    }

    @Override
    public boolean getCapPaymentDetail() throws JposException {
        return false;
    }

    @Override
    public boolean getCapTaxOthers() throws JposException {
        return false;
    }

    @Override
    public boolean getCapTransactionNumber() throws JposException {
        return false;
    }

    @Override
    public boolean getCapMembershipCertificate() throws JposException {
        return false;
    }

    @Override
    public String getCardCompanyID() throws JposException {
        return null;
    }

    @Override
    public String getCenterResultCode() throws JposException {
        return null;
    }

    @Override
    public String getDailyLog() throws JposException {
        return null;
    }

    @Override
    public int getPaymentCondition() throws JposException {
        return 0;
    }

    @Override
    public String getPaymentDetail() throws JposException {
        return null;
    }

    @Override
    public int getPaymentMedia() throws JposException {
        return 0;
    }

    @Override
    public String getSlipNumber() throws JposException {
        return null;
    }

    @Override
    public String getTransactionNumber() throws JposException {
        return null;
    }

    @Override
    public int getTransactionType() throws JposException {
        return 0;
    }

    @Override
    public int getServiceType() throws JposException {
        return 0;
    }

    @Override
    public void accessDailyLog(int i, int i1, int i2) throws JposException {

    }

    @Override
    public void accessData(int i, int[] ints, Object[] objects) throws JposException {

    }

    @Override
    public void activateEVService(int[] ints, Object[] objects) throws JposException {

    }

    @Override
    public void authorizeCompletion(int i, long l, long l1, int i1) throws JposException {

    }

    @Override
    public void authorizePreSales(int i, long l, long l1, int i1) throws JposException {

    }

    @Override
    public void authorizeRefund(int i, long l, long l1, int i1) throws JposException {

    }

    @Override
    public void authorizeSales(int i, long l, long l1, int i1) throws JposException {

    }

    @Override
    public void authorizeVoid(int i, long l, long l1, int i1) throws JposException {

    }

    @Override
    public void authorizeVoidPreSales(int i, long l, long l1, int i1) throws JposException {

    }

    @Override
    public void cashDeposit(int i, long l, int i1) throws JposException {

    }

    @Override
    public void checkCard(int i, int i1) throws JposException {

    }

    @Override
    public void checkServiceRegistrationToMedium(int i, int i1) throws JposException {

    }

    @Override
    public void closeDailyEVService(int[] ints, Object[] objects) throws JposException {

    }

    @Override
    public void deactivateEVService(int[] ints, Object[] objects) throws JposException {

    }

    @Override
    public void openDailyEVService(int[] ints, Object[] objects) throws JposException {

    }

    @Override
    public void retrieveResultInformation(String s, String[] strings) throws JposException {

    }

    @Override
    public void unregisterServiceToMedium(int i, int i1) throws JposException {

    }

    @Override
    public void updateData(int i, int[] ints, Object[] objects) throws JposException {

    }

    @Override
    public void setParameterInformation(String s, String s1) throws JposException {

    }

    @Override
    public boolean getCapActivateService() throws JposException {
        return false;
    }

    @Override
    public boolean getCapAddValue() throws JposException {
        return false;
    }

    @Override
    public boolean getCapCancelValue() throws JposException {
        return false;
    }

    @Override
    public int getCapCardSensor() throws JposException {
        return 0;
    }

    @Override
    public int getCapDetectionControl() throws JposException {
        return 0;
    }

    @Override
    public boolean getCapElectronicMoney() throws JposException {
        return false;
    }

    @Override
    public boolean getCapEnumerateCardServices() throws JposException {
        return false;
    }

    @Override
    public boolean getCapIndirectTransactionLog() throws JposException {
        return false;
    }

    @Override
    public boolean getCapLockTerminal() throws JposException {
        return false;
    }

    @Override
    public boolean getCapLogStatus() throws JposException {
        return false;
    }

    @Override
    public boolean getCapMediumID() throws JposException {
        return false;
    }

    @Override
    public boolean getCapPoint() throws JposException {
        return false;
    }

    @Override
    public boolean getCapRealTimeData() throws JposException {
        return false;
    }

    @Override
    public boolean getCapSubtractValue() throws JposException {
        return false;
    }

    @Override
    public boolean getCapTransaction() throws JposException {
        return false;
    }

    @Override
    public boolean getCapTransactionLog() throws JposException {
        return false;
    }

    @Override
    public boolean getCapUnlockTerminal() throws JposException {
        return false;
    }

    @Override
    public boolean getCapUpdateKey() throws JposException {
        return false;
    }

    @Override
    public boolean getCapVoucher() throws JposException {
        return false;
    }

    @Override
    public boolean getCapWriteValue() throws JposException {
        return false;
    }

    @Override
    public String getAccountNumber() throws JposException {
        return null;
    }

    @Override
    public String getAdditionalSecurityInformation() throws JposException {
        return null;
    }

    @Override
    public void setAdditionalSecurityInformation(String s) throws JposException {

    }

    @Override
    public long getAmount() throws JposException {
        return 0;
    }

    @Override
    public void setAmount(long l) throws JposException {

    }

    @Override
    public String getApprovalCode() throws JposException {
        return null;
    }

    @Override
    public void setApprovalCode(String s) throws JposException {

    }

    @Override
    public long getBalance() throws JposException {
        return 0;
    }

    @Override
    public long getBalanceOfPoint() throws JposException {
        return 0;
    }

    @Override
    public String getCardServiceList() throws JposException {
        return null;
    }

    @Override
    public String getCurrentService() throws JposException {
        return null;
    }

    @Override
    public void setCurrentService(String s) throws JposException {

    }

    @Override
    public boolean getDetectionControl() throws JposException {
        return false;
    }

    @Override
    public void setDetectionControl(boolean b) throws JposException {

    }

    @Override
    public int getDetectionStatus() throws JposException {
        return 0;
    }

    @Override
    public String getExpirationDate() throws JposException {
        return null;
    }

    @Override
    public String getLastUsedDate() throws JposException {
        return null;
    }

    @Override
    public int getLogStatus() throws JposException {
        return 0;
    }

    @Override
    public String getMediumID() throws JposException {
        return null;
    }

    @Override
    public void setMediumID(String s) throws JposException {

    }

    @Override
    public long getPoint() throws JposException {
        return 0;
    }

    @Override
    public void setPoint(long l) throws JposException {

    }

    @Override
    public String getReaderWriterServiceList() throws JposException {
        return null;
    }

    @Override
    public int getSequenceNumber() throws JposException {
        return 0;
    }

    @Override
    public long getSettledAmount() throws JposException {
        return 0;
    }

    @Override
    public long getSettledPoint() throws JposException {
        return 0;
    }

    @Override
    public String getTransactionLog() throws JposException {
        return null;
    }

    @Override
    public String getVoucherID() throws JposException {
        return null;
    }

    @Override
    public void setVoucherID(String s) throws JposException {

    }

    @Override
    public String getVoucherIDList() throws JposException {
        return null;
    }

    @Override
    public void setVoucherIDList(String s) throws JposException {

    }

    @Override
    public void accessLog(int i, int i1, int i2) throws JposException {

    }

    @Override
    public void activateService(int[] ints, Object[] objects) throws JposException {

    }

    @Override
    public void addValue(int i, int i1) throws JposException {

    }

    @Override
    public void beginDetection(int i, int i1) throws JposException {

    }

    @Override
    public void beginRemoval(int i) throws JposException {

    }

    @Override
    public void cancelValue(int i, int i1) throws JposException {

    }

    @Override
    public void captureCard() throws JposException {

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

    @Override
    public void lockTerminal() throws JposException {

    }

    @Override
    public void readValue(int i, int i1) throws JposException {

    }

    @Override
    public void subtractValue(int i, int i1) throws JposException {

    }

    @Override
    public void transactionAccess(int i) throws JposException {

    }

    @Override
    public void unlockTerminal() throws JposException {

    }

    @Override
    public void updateKey(int[] ints, Object[] objects) throws JposException {

    }

    @Override
    public void writeValue(int i, int i1) throws JposException {

    }
}
