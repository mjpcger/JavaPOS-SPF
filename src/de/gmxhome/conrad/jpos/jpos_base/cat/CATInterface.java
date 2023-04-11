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

/**
 * Interface for methods that implement property setter and method calls for the CAT device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter CAT - Credit
 * Authorization Terminal.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface CATInterface extends JposBaseInterface {
    /**
     * Final part of setting AdditionalSecurityInformation. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>CapAdditionalSecurityInformation is true,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or addInfo equals the previous value of AdditionalSecurityInformation.</li>
     * </ul>
     *
     * @param addInfo AdditionalSecurityInformation for subsequent storing data into journal.
     * @throws JposException If an error occurs.
     */
    public void additionalSecurityInformation(String addInfo) throws JposException;

    /**
     * Final part of setting PaymentMedia. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>media is one of MEDIA_UNSPECIFIED, MEDIA_CREDIT, MEDIA_DEBIT or MEDIA_ELECTRONIC_MONEY,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or media equals the previous value of PaymentMedia.</li>
     * </ul>
     *
     * @param media PaymentMedia for subsequent storing data into journal.
     * @throws JposException If an error occurs.
     */
    public void paymentMedia(int media) throws JposException;

    /**
     * Final part of setting TrainingMode. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>CapTrainingMode is true or flag is false,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or flag equals the previous value of TrainingMode.</li>
     * </ul>
     *
     * @param flag TrainingMode for subsequent storing data into journal.
     * @throws JposException If an error occurs.
     */
    public void trainingMode(boolean flag) throws JposException;

    /**
     * Validation part of AccessDailyLog method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Device is not busy,</li>
     *     <li>type is one of DL_REPORTING or DL_SETTLEMENT and is contained in CapDailyLog,</li>
     *     <li>timeout is JPOS_FOREVER or &ge; 0.</li>
     * </ul>
     *
     * @param sequenceNumber    Sequence number, any value.
     * @param type              DL_REPORTING or DL_SETTLEMENT.
     * @param timeout           operation timeout.
     * @return                  AccessDailyLog object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public AccessDailyLog accessDailyLog(int sequenceNumber, int type, int timeout) throws JposException;

    /**
     * Final part of AccessDailyLog method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a AccessDailyLog object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by AccessDailyLog.
     * @throws JposException    If an error occurs.
     */
    public void accessDailyLog(AccessDailyLog request) throws JposException;

    /**
     * Validation part of AuthorizeCompletion method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Device is not busy,</li>
     *     <li>CapAuthorizeCompletion is true,</li>
     *     <li>amount &gt; 0, taxOthers $ge; 0,</li>
     *     <li>timeout is JPOS_FOREVER or &ge; 0.</li>
     * </ul>
     *
     * @param sequenceNumber    Sequence number, any value.
     * @param amount            Purchase amount for approval.
     * @param taxOthers         Tax and other amounts for approval.
     * @param timeout           operation timeout.
     * @return                  AuthorizeCompletion object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public AuthorizeCompletion authorizeCompletion(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException;

    /**
     * Final part of AuthorizeCompletion method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a AuthorizeCompletion object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by AuthorizeCompletion.
     * @throws JposException    If an error occurs.
     */
    public void authorizeCompletion(AuthorizeCompletion request) throws JposException;

    /**
     * Validation part of AuthorizePreSales method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Device is not busy,</li>
     *     <li>CapAuthorizePreSales is true,</li>
     *     <li>amount &gt; 0, taxOthers $ge; 0,</li>
     *     <li>timeout is JPOS_FOREVER or &ge; 0.</li>
     * </ul>
     *
     * @param sequenceNumber    Sequence number, any value.
     * @param amount            Purchase amount for approval.
     * @param taxOthers         Tax and other amounts for approval.
     * @param timeout           operation timeout.
     * @return                  AuthorizePreSales object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public AuthorizePreSales authorizePreSales(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException;

    /**
     * Final part of AuthorizePreSales method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a AuthorizePreSales object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by AuthorizePreSales.
     * @throws JposException    If an error occurs.
     */
    public void authorizePreSales(AuthorizePreSales request) throws JposException;

    /**
     * Validation part of AuthorizeRefund method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Device is not busy,</li>
     *     <li>CapAuthorizeRefund is true,</li>
     *     <li>amount &gt; 0, taxOthers $ge; 0,</li>
     *     <li>timeout is JPOS_FOREVER or &ge; 0.</li>
     * </ul>
     *
     * @param sequenceNumber    Sequence number, any value.
     * @param amount            Purchase amount for approval.
     * @param taxOthers         Tax and other amounts for approval.
     * @param timeout           operation timeout.
     * @return                  AuthorizeRefund object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public AuthorizeRefund authorizeRefund(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException;

    /**
     * Final part of AuthorizeRefund method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a AuthorizeRefund object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by AuthorizeRefund.
     * @throws JposException    If an error occurs.
     */
    public void authorizeRefund(AuthorizeRefund request) throws JposException;

    /**
     * Validation part of AuthorizeSales method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Device is not busy,</li>
     *     <li>amount &gt; 0, taxOthers $ge; 0,</li>
     *     <li>timeout is JPOS_FOREVER or &ge; 0.</li>
     * </ul>
     *
     * @param sequenceNumber    Sequence number, any value.
     * @param amount            Purchase amount for approval.
     * @param taxOthers         Tax and other amounts for approval.
     * @param timeout           operation timeout.
     * @return                  AuthorizeSales object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public AuthorizeSales authorizeSales(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException;

    /**
     * Final part of AuthorizeSales method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a AuthorizeSales object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by AuthorizeSales.
     * @throws JposException    If an error occurs.
     */
    public void authorizeSales(AuthorizeSales request) throws JposException;

    /**
     * Validation part of AuthorizeVoid method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Device is not busy,</li>
     *     <li>CapAuthorizeVoid is true,</li>
     *     <li>amount &gt; 0, taxOthers $ge; 0,</li>
     *     <li>timeout is JPOS_FOREVER or &ge; 0.</li>
     * </ul>
     *
     * @param sequenceNumber    Sequence number, any value.
     * @param amount            Purchase amount for approval.
     * @param taxOthers         Tax and other amounts for approval.
     * @param timeout           operation timeout.
     * @return                  AuthorizeVoid object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public AuthorizeVoid authorizeVoid(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException;

    /**
     * Final part of AuthorizeVoid method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a AuthorizeVoid object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by AuthorizeVoid.
     * @throws JposException    If an error occurs.
     */
    public void authorizeVoid(AuthorizeVoid request) throws JposException;

    /**
     * Validation part of AuthorizeVoidPreSales method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Device is not busy,</li>
     *     <li>CapAuthorizeVoidPreSales is true,</li>
     *     <li>amount &gt; 0, taxOthers $ge; 0,</li>
     *     <li>timeout is JPOS_FOREVER or &ge; 0.</li>
     * </ul>
     *
     * @param sequenceNumber    Sequence number, any value.
     * @param amount            Purchase amount for approval.
     * @param taxOthers         Tax and other amounts for approval.
     * @param timeout           operation timeout.
     * @return                  AuthorizeVoidPreSales object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public AuthorizeVoidPreSales authorizeVoidPreSales(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException;

    /**
     * Final part of AuthorizeVoidPreSales method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a AuthorizeVoidPreSales object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters to be
     *                          used by AuthorizeVoidPreSales.
     * @throws JposException    If an error occurs.
     */
    public void authorizeVoidPreSales(AuthorizeVoidPreSales request) throws JposException;

    /**
     * Validation part of CashDeposit method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Device is not busy,</li>
     *     <li>CapCashDeposit is true,</li>
     *     <li>amount &gt; 0,</li>
     *     <li>timeout is JPOS_FOREVER or &ge; 0.</li>
     * </ul>
     *
     * @param sequenceNumber    Sequence number, any value.
     * @param amount            Purchase amount for approval.
     * @param timeout           operation timeout.
     * @return                  CashDeposit object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public CashDeposit cashDeposit(int sequenceNumber, long amount, int timeout) throws JposException;

    /**
     * Final part of CashDeposit method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a CashDeposit object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by CashDeposit.
     * @throws JposException    If an error occurs.
     */
    public void cashDeposit(CashDeposit request) throws JposException;

    /**
     * Validation part of CheckCard method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Device is not busy,</li>
     *     <li>CapCheckCard is true,</li>
     *     <li>timeout is JPOS_FOREVER or &ge; 0.</li>
     * </ul>
     *
     * @param sequenceNumber    Sequence number, any value.
     * @param timeout           operation timeout.
     * @return                  CheckCard object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public CheckCard checkCard(int sequenceNumber, int timeout) throws JposException;

    /**
     * Final part of CheckCard method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a CheckCard object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by CheckCard.
     * @throws JposException    If an error occurs.
     */
    public void checkCard(CheckCard request) throws JposException;

    /**
     * Validation part of LockTerminal method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Device is not busy,</li>
     *     <li>CapLockTerminal is true,</li>
     *     <li>timeout is JPOS_FOREVER or &ge; 0.</li>
     * </ul>
     *
     * @return                  LockTerminal object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public LockTerminal lockTerminal() throws JposException;

    /**
     * Final part of LockTerminal method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a LockTerminal object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method to be used by LockTerminal.
     * @throws JposException    If an error occurs.
     */
    public void lockTerminal(LockTerminal request) throws JposException;

    /**
     * Validation part of UnlockTerminal method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Device is not busy,</li>
     *     <li>CapUnlockTerminal is true,</li>
     *     <li>timeout is JPOS_FOREVER or &ge; 0.</li>
     * </ul>
     *
     * @return                  UnlockTerminal object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public UnlockTerminal unlockTerminal() throws JposException;

    /**
     * Final part of UnlockTerminal method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a UnlockTerminal object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method to be used by UnlockTerminal.
     * @throws JposException    If an error occurs.
     */
    public void unlockTerminal(UnlockTerminal request) throws JposException;
}
