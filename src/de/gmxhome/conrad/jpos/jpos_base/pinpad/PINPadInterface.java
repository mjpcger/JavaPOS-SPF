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

package de.gmxhome.conrad.jpos.jpos_base.pinpad;

import de.gmxhome.conrad.jpos.jpos_base.JposBaseInterface;
import jpos.JposException;

/**
 * Interface for methods that implement property setter and method calls for the PINPad device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter PIN Pad.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface PINPadInterface extends JposBaseInterface {
    /**
     * Final part of setting AccountNumber. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is opened,</li>
     *     <li>EFT transaction has not been started.</li>
     * </ul>
     *
     * @param accountNumber Account number to be used for the current EFT transaction.
     * @throws JposException If an error occurs.
     */
    public void setAccountNumber(String accountNumber) throws JposException;

    /**
     * Final part of setting Amount. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is opened,</li>
     *     <li>EFT transaction has not been started,</li>
     *     <li>amount is not negative.</li>
     * </ul>
     *
     * @param amount Amount of the current EFT transaction.
     * @throws JposException If an error occurs.
     */
    public void setAmount(long amount) throws JposException;

    /**
     * Final part of setting MaximumPINLength. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is opened,</li>
     *     <li>Property PINEntryEnabled is false,</li>
     *     <li>maximumPINLength is greater than zero.</li>
     * </ul>
     *
     * @param maximumPINLength Maximum acceptable number of digits in a PIN.
     * @throws JposException If an error occurs.
     */
    public void setMaximumPINLength(int maximumPINLength) throws JposException;

    /**
     * Final part of setting MerchantID. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is opened,</li>
     *     <li>EFT transaction has not been started.</li>
     * </ul>
     *
     * @param merchantID Merchant ID, as known by the EFT Transaction Host.
     * @throws JposException If an error occurs.
     */
    public void setMerchantID(String merchantID) throws JposException;

    /**
     * Final part of setting MinimumPINLength. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is opened,</li>
     *     <li>Property PINEntryEnabled is false,</li>
     *     <li>minimumPINLength is a positive value.</li>
     * </ul>
     *
     * @param minimumPINLength Minimum acceptable number of digits in a PIN.
     * @throws JposException If an error occurs.
     */
    public void setMinimumPINLength(int minimumPINLength) throws JposException;

    /**
     * Final part of setting Prompt. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is opened,</li>
     *     <li>CapDisplay is one of DISP_PINRESTRICTED, DISP_RESTRICTED_LIST or DISP_RESTRICTED_ORDER,</li>
     *     <li>If CapDisplay is DISP_PINRESTRICTED, PINEntryEnabled is true,</li>
     *     <li>prompt equals one of the values specified in property AvailablePromptsList.</li>
     * </ul>
     *
     * @param prompt Identifies a predefined message to be displayed on the PIN Pad.
     * @throws JposException If an error occurs.
     */
    public void setPrompt(int prompt) throws JposException;

    /**
     * Final part of setting PromptLanguage. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is opened,</li>
     *     <li>CapLanguage is not LANG_NONE,</li>
     *     <li>If CapLanguage is LANG_ONE, promptLanguage equals the default value of PromptLanguage,</li>
     *     <li>If CapLanguage is LANG_PINRESTRICTED, PINEntryEnabled is false,</li>
     *     <li>promptLanguage specifies one of the languages specified in property AvailableLanguagesList.</li>
     * </ul>
     * For example, if AvailableLanguagesList contains the language specification "EN,US", both, "EN,US" and "EN"
     * would be valid language specifications, but "US" would be invalid because "US" specifies a country, not a language.
     * However, if the service supports multiple English variants, e.eg "EN,US" and "EN,UK", it is up to the service which
     * variant will be used if only "EN" will be specified.
     *
     * @param promptLanguage Holds the “language definition” for the message to be displayed.
     * @throws JposException If an error occurs.
     */
    public void setPromptLanguage(String promptLanguage) throws JposException;

    /**
     * Final part of setting TerminalID. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is opened,</li>
     *     <li>EFT transaction has not been started.</li>
     * </ul>
     *
     * @param terminalID Terminal ID, as known by the EFT Transaction Host.
     * @throws JposException If an error occurs.
     */
    public void setTerminalID(String terminalID) throws JposException;

    /**
     * Final part of setting Track1Data. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is opened,</li>
     *     <li>EFT transaction has not been started.</li>
     * </ul>
     *
     * @param track1Data Decoded track 1 data from the previous card swipe or an empty array.
     * @throws JposException If an error occurs.
     */
    public void setTrack1Data(byte[] track1Data) throws JposException;

    /**
     * Final part of setting Track2Data. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is opened,</li>
     *     <li>EFT transaction has not been started.</li>
     * </ul>
     *
     * @param track2Data Decoded track 2 data from the previous card swipe or an empty array.
     * @throws JposException If an error occurs.
     */
    public void setTrack2Data(byte[] track2Data) throws JposException;

    /**
     * Final part of setting Track3Data. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is opened,</li>
     *     <li>EFT transaction has not been started.</li>
     * </ul>
     *
     * @param track3Data Decoded track 3 data from the previous card swipe or an empty array.
     * @throws JposException If an error occurs.
     */
    public void setTrack3Data(byte[] track3Data) throws JposException;

    /**
     * Final part of setting Track4Data. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is opened,</li>
     *     <li>EFT transaction has not been started.</li>
     * </ul>
     *
     * @param track4Data Decoded track 4 (JIS-II) data from the previous card swipe or an empty array.
     * @throws JposException If an error occurs.
     */
    public void setTrack4Data(byte[] track4Data) throws JposException;

    /**
     * Final part of setting TransactionType. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is opened,</li>
     *     <li>EFT transaction has not been started,</li>
     *     <li>transactionType is one of TRANS_DEBIT, TRANS_CREDIT, TRANS_INQ, TRANS_RECONCILE or TRANS_ADMIN.</li>
     * </ul>
     *
     * @param transactionType Type of the current EFT Transaction.
     * @throws JposException If an error occurs.
     */
    public void setTransactionType(int transactionType) throws JposException;

    /**
     * Final part of BeginEFTTransaction method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>EFT transaction has not been started,</li>
     *     <li>pinPadSystem equals one of the predefined values, see PINPadProperties, property SupportedPINPadSystems.</li>
     * </ul>
     *
     * @param pinPadSystem    Name of the desired PIN Pad Management System.
     * @param transactionHost Identifier of an EFT transaction host to be used for this transaction.
     * @throws JposException If an error occurs.
     */
    public void beginEFTTransaction(String pinPadSystem, int transactionHost) throws JposException;

    /**
     * Final part of ComputeMAC method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>EFT transaction has been started,</li>
     *     <li>CapMACCalculation is true,</li>
     *     <li>PINEntryEnabled is false,</li>
     *     <li>inMsg is not null,</li>
     *     <li>outMsg is a String array with length 1.</li>
     * </ul>
     *
     * @param inMsg  The message that the application intends to send to an EFT Transaction.
     * @param outMsg Reformatted message that may actually be transmitted to an EFT Transaction Host.
     * @throws JposException If an error occurs.
     */
    public void computeMAC(String inMsg, String[] outMsg) throws JposException;

    /**
     * Final part of EnablePINEntry method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>EFT transaction has been started,</li>
     *     <li>PINEntryEnabled is false.</li>
     * </ul>
     *
     * @throws JposException If an error occurs.
     */
    public void enablePINEntry() throws JposException;

    /**
     * Final part of EndEFTTransaction method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>EFT transaction has been started,</li>
     *     <li>completionCode is EFT_NORMAL or EFT_ABNORMAL.</li>
     * </ul>
     *
     * @param completionCode The completion code.
     * @throws JposException If an error occurs.
     */
    public void endEFTTransaction(int completionCode) throws JposException;

    /**
     * Final part of UpdateKey method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>EFT transaction has been started,</li>
     *     <li>key consists of an even number of hexadecimal digits (0-9 and A-F).</li>
     * </ul>
     *
     * @param keyNum A key number.
     * @param key    A Hex-ASCII value for a new key.
     * @throws JposException If an error occurs.
     */
    public void updateKey(int keyNum, String key) throws JposException;

    /**
     * Final part of VerifyMAC method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapMACCalculation is true,</li>
     *     <li>EFT transaction has been started,</li>
     *     <li>PINEntryEnabled is false,</li>
     *     <li>message is not null.</li>
     * </ul>
     *
     * @param message A message received from an EFT Transaction Host.
     * @throws JposException If an error occurs.
     */
    public void verifyMAC(String message) throws JposException;
}
