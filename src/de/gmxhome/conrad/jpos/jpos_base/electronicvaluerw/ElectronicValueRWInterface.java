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

import de.gmxhome.conrad.jpos.jpos_base.JposBaseInterface;
import jpos.JposException;

/**
 * Interface for methods that implement property setter and method calls for the ElectronicValueRW device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Electronic Value Reader / Writer.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface ElectronicValueRWInterface extends JposBaseInterface {
    /**
     * Final part of setting AdditionalSecurityInformation. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>CapAdditionalSecurityInformation is true,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or addInfo equals the previous value of AdditionalSecurityInformation.</li>
     * </ul>
     *
     * @param addInfo New value for AdditionalSecurityInformation property..
     * @throws JposException If an error occurs.
     */
    public void additionalSecurityInformation(String addInfo) throws JposException;

    /**
     * Final part of setting Amount. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or amount equals the previous value of Amount.</li>
     * </ul>
     *
     * @param amount Payment amount for electronic money operation.
     * @throws JposException If an error occurs.
     */
    public void amount(long amount) throws JposException;

    /**
     * Final part of setting ApprovalCode. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>code is not null,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or code equals the previous value of ApprovalCode.</li>
     * </ul>
     *
     * @param code New value for ApprovalCode property..
     * @throws JposException If an error occurs.
     */
    public void approvalCode(String code) throws JposException;

    /**
     * Final part of setting CurrentService. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>service is one of the sevices specified in property ReaderWriterServiceList or an
     *     empty string,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or service equals the previous value of CurrentService.</li>
     * </ul>
     *
     * @param service New service specifier.
     * @throws JposException If an error occurs.
     */
    public void currentService(String service) throws JposException;

    /**
     * Final part of setting DetectionControl. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>CDC_APPLICATIONCONTROL is set in CapDetectionControl or flag is false,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or flag equals the previous value of DetectionControl.</li>
     * </ul>
     *
     * @param flag New value for DetectionControl property..
     * @throws JposException If an error occurs.
     */
    public void detectionControl(boolean flag) throws JposException;

    /**
     * Final part of setting MediumID. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>id is not null,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or id equals the previous value of MediumID.</li>
     * </ul>
     *
     * @param id New value for MediumID property..
     * @throws JposException If an error occurs.
     */
    public void mediumID(String id) throws JposException;

    /**
     * Final part of setting PaymentMedia. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>media is one of MEDIA_UNSPECIFIED, MEDIA_CREDIT, MEDIA_DEBIT or MEDIA_ELECTRONIC_MONEY,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or media equals the previous value of PaymentMedia.</li>
     * </ul>
     *
     * @param media PaymentMedia for subsequent CAT service operations.
     * @throws JposException If an error occurs.
     */
    public void paymentMedia(int media) throws JposException;

    /**
     * Final part of setting PINEntry. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>value is one of PIN_ENTRY_NONE, PIN_ENTRY_EXTERNAL, PIN_ENTRY_INTERNAL or PIN_ENTRY_UNKNOWN,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or value equals the previous value of PINEntry.</li>
     * </ul>
     *
     * @param value New value for PINEntry property.
     * @throws JposException If an error occurs.
     */
    public void PINEntry(int value) throws JposException;

    /**
     * Final part of setting Point. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or count equals the previous value of Point.</li>
     * </ul>
     *
     * @param count New value for Point property.
     * @throws JposException If an error occurs.
     */
    public void point(long count) throws JposException;

    /**
     * Final part of setting TrainingModeState. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>CapTrainingMode is true or state is not TM_TRUE,</li>
     *     <li>state is one of TM_FALSE, TM_TRUE or TM_UNKNOWN,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or state equals the previous value of TrainingModeState.</li>
     * </ul>
     *
     * @param state TrainingModeState for subsequent operation.
     * @throws JposException If an error occurs.
     */
    public void trainingModeState(int state) throws JposException;

    /**
     * Final part of setting VoucherID. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>id is not null.</li>
     *     <li>id id has the form "ttt:cc", where ttt is a voucher/ticket identifier and cc the
     *     corresponding count &gt; 0,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or id equals the previous value of VoucherID.</li>
     * </ul>
     *
     * @param id New value for VoucherID property..
     * @throws JposException If an error occurs.
     */
    public void voucherID(String id) throws JposException;

    /**
     * Final part of setting VoucherIDList. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>id is not null,</li>
     *     <li>id id has the form "ttt1:cc1[,ttt2:cc2[,...]]", where ttt1 and tt2 are voucher/ticket
     *     identifiers, cc1 and cc2 the corresponding counts, where ttt2:cc2 is optional as well as
     *     further identifier:count pairs, represented by "...".</li>
     *     <li>internal property AllowAlwaysSetProperties is true or ids equals the previous value of VoucherIDList.</li>
     * </ul>
     *
     * @param ids New value for VoucherIDList property..
     * @throws JposException If an error occurs.
     */
    public void voucherIDList(String ids) throws JposException;

    /**
     * final part of BeginDetection method. Can be overwritten within derived
     * classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>DetectionControl is true,</li>
     *     <li>type is one of BD_ANY or BD_SPECIFIC,</li>
     *     <li>timeout is JPOS_FOREVER or &ge; 0.</li>
     * </ul>
     *
     * @param type              BD_ANY or BD_SPECIFIC.
     * @param timeout           operation timeout.
     * @throws JposException    If an error occurs.
     */
    public void beginDetection(int type, int timeout) throws JposException;

    /**
     * final part of BeginRemoval method. Can be overwritten within derived
     * classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>timeout is JPOS_FOREVER or &ge; 0.</li>
     * </ul>
     *
     * @param timeout           operation timeout.
     * @throws JposException    If an error occurs.
     */
    public void beginRemoval(int timeout) throws JposException;

    /**
     * final part of CaptureCard method. Can be overwritten within derived
     * classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     * </ul>
     *
     * @throws JposException    If an error occurs.
     */
    public void captureCard() throws JposException;

    /**
     * final part of ClearParameterInformation method. Can be overwritten within derived
     * classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     *
     * @throws JposException    If an error occurs.
     */
    public void clearParameterInformation() throws JposException;

    /**
     * final part of ClearParameterInformation method. Can be overwritten within derived
     * classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>DetectionControl is true.</li>
     * </ul>
     *
     * @throws JposException    If an error occurs.
     */
    public void endDetection() throws JposException;

    /**
     * final part of ClearParameterInformation method. Can be overwritten within derived
     * classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>DetectionControl is true.</li>
     * </ul>
     *
     * @throws JposException    If an error occurs.
     */
    public void endRemoval() throws JposException;

    /**
     * final part of EnumerateCardServices method. Can be overwritten within derived
     * classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     *
     * @throws JposException    If an error occurs.
     */
    public void enumerateCardServices() throws JposException;

    /**
     * final part of RetrieveResultInformation method. Can be overwritten within derived
     * classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is claimed,</li>
     *     <li>name is not null and value is an array with dimension 1.</li>
     * </ul>
     *
     * @param name  Tag name
     * @param value Array to be filled with tag value on return.
     * @throws JposException    If an error occurs.
     */
    public void retrieveResultInformation(String name, String[] value) throws JposException;

    /**
     * final part of SetParameterInformation method. Can be overwritten within derived
     * classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is claimed,</li>
     *     <li>name and value are not null,</li>
     *     <li>value matches the type specified in the UPOS specification for the given tag name. In case of an
     *     unknown tag name, value will not be checked.</li>
     * </ul>
     * Remarks:<ul>
     *     <li>Since the UPOS specification for tag VoidTransactionType and VOIDorRETURN is unclear, these tags won't be checked.</li>
     *     <li>Tag SettledVoucherID will be handled the same way as tag SetttledVoucherID.</li>
     *     <li>Tags SetttledVoucherID and VoucherID will be checked to match the same format as property VoucherID.</li>
     *     <li>Tag VoucherIDList will be checked to match the same format as property VoucherIDList.</li>
     * </ul>
     * @param name  Tag name
     * @param value Array to be filled with tag value on return.
     * @throws JposException    If an error occurs.
     */
    public void setParameterInformation(String name, String value) throws JposException;

    /**
     * Validation part of AccessDailyLog method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapDailyLog is true,</li>
     *     <li>State is S_IDLE,</li>
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
     * Final part of AccessData method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a AccessData object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by AccessData.
     * @throws JposException    If an error occurs.
     */
    public void accessDailyLog(AccessDailyLog request) throws JposException;

    /**
     * Validation part of AccessData method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>dataType is one of AD_KEY, AD_NEGATIVE_LIST or AD_OTHERS,</li>
     *     <li>data and type are non-null arrays with dimension 1.</li>
     * </ul>
     *
     * @param dataType    Data type.
     * @param data        vendor specific data.
     * @param obj         vendor specific Object.
     * @return            AccessData object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public AccessData accessData(int dataType, int[] data, Object[] obj) throws JposException;

    /**
     * Final part of AccessData method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a AccessData object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by AccessData.
     * @throws JposException    If an error occurs.
     */
    public void accessData(AccessData request) throws JposException;

    /**
     * Validation part of AccessLog method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>type is one of AL_REPORTING or AL_SETTLEMENT and is contained in CapDailyLog,</li>
     *     <li>timeout is JPOS_FOREVER or &ge; 0.</li>
     * </ul>
     *
     * @param sequenceNumber    Sequence number, any value.
     * @param type              AL_REPORTING or AL_SETTLEMENT.
     * @param timeout           operation timeout.
     * @return                  AccessLog object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public AccessLog accessLog(int sequenceNumber, int type, int timeout) throws JposException;

    /**
     * Final part of AccessLog method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a AccessLog object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by AccessLog.
     * @throws JposException    If an error occurs.
     */
    public void accessLog(AccessLog request) throws JposException;

    /**
     * Validation part of ActivateEVService method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>data and obj are arrays with dimension 0 or 1.</li>
     * </ul>
     *
     * @param data    Vendor specific.
     * @param obj     vendor specific.
     * @return        ActivateEVService object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public ActivateEVService activateEVService(int[] data, Object[] obj) throws JposException;

    /**
     * Final part of ActivateEVService method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a ActivateEVService object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by ActivateEVService.
     * @throws JposException    If an error occurs.
     */
    public void activateEVService(ActivateEVService request) throws JposException;

    /**
     * Validation part of ActivateService method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>CapActivateService is true,</li>
     *     <li>data and obj are arrays with dimension 0 or 1.</li>
     * </ul>
     *
     * @param data    Vendor specific.
     * @param obj     vendor specific.
     * @return                  ActivateService object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public ActivateService activateService(int[] data, Object[] obj) throws JposException;

    /**
     * Final part of ActivateService method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a ActivateService object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by ActivateService.
     * @throws JposException    If an error occurs.
     */
    public void activateService(ActivateService request) throws JposException;

    /**
     * Validation part of AddValue method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapAddValue is true,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>timeout is JPOS_FOREVER or &ge; 0.</li>
     * </ul>
     *
     * @param sequenceNumber    Sequence number, any value.
     * @param timeout           operation timeout.
     * @return                  AddValue object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public AddValue addValue(int sequenceNumber, int timeout) throws JposException;

    /**
     * Final part of AddValue method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a AddValue object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by AddValue.
     * @throws JposException    If an error occurs.
     */
    public void addValue(AddValue request) throws JposException;

    /**
     * Validation part of AuthorizeCompletion method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>State is S_IDLE,</li>
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
     *     <li>State is S_IDLE,</li>
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
     *     <li>State is S_IDLE,</li>
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
     *     <li>State is S_IDLE,</li>
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
     *     <li>State is S_IDLE,</li>
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
     *     <li>State is S_IDLE,</li>
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
     * Validation part of CancelValue method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>CapCancelValue is true,</li>
     *     <li>timeout is JPOS_FOREVER or &ge; 0.</li>
     * </ul>
     *
     * @param sequenceNumber    Sequence number, any value.
     * @param timeout           operation timeout.
     * @return                  CancelValue object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public CancelValue cancelValue(int sequenceNumber, int timeout) throws JposException;

    /**
     * Final part of CancelValue method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a CancelValue object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by CancelValue.
     * @throws JposException    If an error occurs.
     */
    public void cancelValue(CancelValue request) throws JposException;

    /**
     * Validation part of CashDeposit method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>State is S_IDLE,</li>
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
     *     <li>State is S_IDLE,</li>
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
     * Validation part of CheckServiceRegistrationToMedium method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>timeout is JPOS_FOREVER or &ge; 0.</li>
     * </ul>
     *
     * @param sequenceNumber    Sequence number, any value.
     * @param timeout           operation timeout.
     * @return                  CheckServiceRegistrationToMedium object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public CheckServiceRegistrationToMedium checkServiceRegistrationToMedium(int sequenceNumber, int timeout) throws JposException;

    /**
     * Final part of CheckServiceRegistrationToMedium method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a CheckCard object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by CheckServiceRegistrationToMedium.
     * @throws JposException    If an error occurs.
     */
    public void checkServiceRegistrationToMedium(CheckServiceRegistrationToMedium request) throws JposException;

    /**
     * Validation part of CloseDailyEVService method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>data and obj are arrays with dimension 0 or 1.</li>
     * </ul>
     *
     * @param data    Vendor specific.
     * @param obj     vendor specific.
     * @return                  CloseDailyEVService object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public CloseDailyEVService closeDailyEVService(int[] data, Object[] obj) throws JposException;

    /**
     * Final part of CloseDailyEVService method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a CheckCard object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by CloseDailyEVService.
     * @throws JposException    If an error occurs.
     */
    public void closeDailyEVService(CloseDailyEVService request) throws JposException;

    /**
     * Validation part of DeactivateEVService method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>data and obj are arrays with dimension 0 or 1.</li>
     * </ul>
     *
     * @param data    Vendor specific.
     * @param obj     vendor specific.
     * @return                  DeactivateEVService object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public DeactivateEVService deactivateEVService(int[] data, Object[] obj) throws JposException;

    /**
     * Final part of DeactivateEVService method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a CheckCard object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by DeactivateEVService.
     * @throws JposException    If an error occurs.
     */
    public void deactivateEVService(DeactivateEVService request) throws JposException;

    /**
     * Validation part of LockTerminal method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>CapLockTerminal is true.</li>
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
     * Validation part of OpenDailyEVService method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>data and obj are arrays with dimension 0 or 1.</li>
     * </ul>
     *
     * @param data    Vendor specific.
     * @param obj     vendor specific.
     * @return                  OpenDailyEVService object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public OpenDailyEVService openDailyEVService(int[]data, Object[]obj) throws JposException;

    /**
     * Final part of OpenDailyEVService method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a OpenDailyEVService object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method to be used by OpenDailyEVService.
     * @throws JposException    If an error occurs.
     */
    public void openDailyEVService(OpenDailyEVService request) throws JposException;

    /**
     * Validation part of QueryLastSuccessfulTransactionResult method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     *
     * @return                  QueryLastSuccessfulTransactionResult object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public QueryLastSuccessfulTransactionResult queryLastSuccessfulTransactionResult() throws JposException;

    /**
     * Final part of QueryLastSuccessfulTransactionResult method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a QueryLastSuccessfulTransactionResult object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method to be used by QueryLastSuccessfulTransactionResult.
     * @throws JposException    If an error occurs.
     */
    public void queryLastSuccessfulTransactionResult(QueryLastSuccessfulTransactionResult request) throws JposException;

    /**
     * Validation part of ReadValue method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>timeout is JPOS_FOREVER or &ge; 0.</li>
     * </ul>
     *
     * @param sequenceNumber    Sequence number, any value.
     * @param timeout           operation timeout.
     * @return                  ReadValue object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public ReadValue readValue(int sequenceNumber, int timeout) throws JposException;

    /**
     * Final part of ReadValue method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a ReadValue object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method to be used by ReadValue.
     * @throws JposException    If an error occurs.
     */
    public void readValue(ReadValue request) throws JposException;

    /**
     * Validation part of RegisterServiceToMedium method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>timeout is JPOS_FOREVER or &ge; 0.</li>
     * </ul>
     *
     * @param sequenceNumber    Sequence number, any value.
     * @param timeout           operation timeout.
     * @return                  RegisterServiceToMedium object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public RegisterServiceToMedium registerServiceToMedium(int sequenceNumber, int timeout) throws JposException;

    /**
     * Final part of RegisterServiceToMedium method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a RegisterServiceToMedium object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method to be used by RegisterServiceToMedium.
     * @throws JposException    If an error occurs.
     */
    public void registerServiceToMedium(RegisterServiceToMedium request) throws JposException;

    /**
     * Validation part of SubtractValue method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSubtractValue is true,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>timeout is JPOS_FOREVER or &ge; 0.</li>
     * </ul>
     *
     * @param sequenceNumber    Sequence number, any value.
     * @param timeout           operation timeout.
     * @return                  SubtractValue object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public SubtractValue subtractValue(int sequenceNumber, int timeout) throws JposException;

    /**
     * Final part of SubtractValue method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a SubtractValue object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method to be used by SubtractValue.
     * @throws JposException    If an error occurs.
     */
    public void subtractValue(SubtractValue request) throws JposException;

    /**
     * Validation part of TransactionAccess method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>CapTransaction is true,</li>
     *     <li>control is one of TA_TRANSACTION or TA_NORMAL,</li>
     *     <li>Either a transaction has not been started or control is TA_NORMAL.</li>
     * </ul>
     *
     * @param control           The transaction control.
     * @return                  TransactionAccess object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public TransactionAccess transactionAccess(int control) throws JposException;

    /**
     * Final part of TransactionAccess method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a TransactionAccess object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method to be used by TransactionAccess.
     * @throws JposException    If an error occurs.
     */
    public void transactionAccess(TransactionAccess request) throws JposException;

    /**
     * Validation part of UnlockTerminal method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>State is S_IDLE,</li>
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

    /**
     * Validation part of UnregisterServiceToMedium method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>timeout is JPOS_FOREVER or &ge; 0.</li>
     * </ul>
     *
     * @param sequenceNumber    Sequence number, any value.
     * @param timeout           operation timeout.
     * @return                  UnregisterServiceToMedium object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public UnregisterServiceToMedium unregisterServiceToMedium(int sequenceNumber, int timeout) throws JposException;

    /**
     * Final part of UnregisterServiceToMedium method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a UnregisterServiceToMedium object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method to be used by UnregisterServiceToMedium.
     * @throws JposException    If an error occurs.
     */
    public void unregisterServiceToMedium(UnregisterServiceToMedium request) throws JposException;

    /**
     * Validation part of UpdateData method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>dataType is one of AD_KEY, AD_NEGATIVE_LIST or AD_OTHERS,</li>
     *     <li>data and obj are arrays with dimension 0 or 1.</li>
     * </ul>
     *
     * @param dataType Type of data.
     * @param data    Vendor specific.
     * @param obj     vendor specific.
     * @return                  UpdateData object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public UpdateData updateData(int dataType, int[]data, Object[]obj) throws JposException;

    /**
     * Final part of UpdateData method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a UpdateData object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method to be used by UpdateData.
     * @throws JposException    If an error occurs.
     */
    public void updateData(UpdateData request) throws JposException;

    /**
     * Validation part of UpdateKey method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>data and obj are arrays with dimension 0 or 1.</li>
     * </ul>
     *
     * @param data    Vendor specific.
     * @param obj     vendor specific.
     * @return                  UpdateKey object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public UpdateKey updateKey(int[]data, Object[]obj) throws JposException;

    /**
     * Final part of UpdateKey method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a UpdateKey object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method to be used by UpdateKey.
     * @throws JposException    If an error occurs.
     */
    public void updateKey(UpdateKey request) throws JposException;

    /**
     * Validation part of WriteValue method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapWriteValue is true,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>timeout is JPOS_FOREVER or &ge; 0.</li>
     * </ul>
     *
     * @param sequenceNumber    Sequence number, any value.
     * @param timeout           operation timeout.
     * @return                  WriteValue object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public WriteValue writeValue(int sequenceNumber, int timeout) throws JposException;

    /**
     * Final part of WriteValue method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a WriteValue object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method to be used by WriteValue.
     * @throws JposException    If an error occurs.
     */
    public void writeValue(WriteValue request) throws JposException;
}
