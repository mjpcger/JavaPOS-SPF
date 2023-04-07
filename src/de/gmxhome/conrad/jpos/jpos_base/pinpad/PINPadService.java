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

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.services.*;

import java.util.Arrays;

/**
 * PINPad service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class PINPadService extends JposBase implements PINPadService115 {
    /**
     * Instance of a class implementing the PINPadInterface for PIN pad specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public PINPadInterface PINPad;

    private PINPadProperties Data;
    private boolean EFTTransactionStarted = false;

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public PINPadService(PINPadProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    /**
     * Sets or resets the DeviceEnabled property.
     * Disabling of Pin Pad devices must be blocked as long as an EFT transaction has not been finished.
     * @param enabled   New value of property DeviceEnabled.
     * @throws JposException    If an EFT transaction is in progress or in case of other errors as described within
     *                          the UPOS specification.
     */
    @Override
    public void setDeviceEnabled(boolean enabled) throws JposException {
        if (!enabled && EFTTransactionStarted) {
            logPreSet("DeviceEnabled");
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Disable Device Invalid in EFT Transaction");
        }
        super.setDeviceEnabled(enabled);
    }

    @Override
    public int getCapDisplay() throws JposException {
        logGet("CapDisplay");
        checkOpened();
        return Data.CapDisplay;
    }

    @Override
    public int getCapLanguage() throws JposException {
        logGet("CapLanguage");
        checkOpened();
        return Data.CapLanguage;
    }

    @Override
    public boolean getCapKeyboard() throws JposException {
        logGet("CapKeyboard");
        checkOpened();
        return Data.CapKeyboard;
    }

    @Override
    public boolean getCapMACCalculation() throws JposException {
        logGet("CapMACCalculation");
        checkOpened();
        return Data.CapMACCalculation;
    }

    @Override
    public boolean getCapTone() throws JposException {
        logGet("CapTone");
        checkOpened();
        return Data.CapTone;
    }

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
    public String getAvailableLanguagesList() throws JposException {
        logGet("AvailableLanguagesList");
        checkOpened();
        return Data.AvailableLanguagesList;
    }

    @Override
    public String getAvailablePromptsList() throws JposException {
        logGet("AvailablePromptsList");
        checkOpened();
        return Data.AvailablePromptsList;
    }

    @Override
    public String getEncryptedPIN() throws JposException {
        logGet("EncryptedPIN");
        checkOpened();
        return Data.EncryptedPIN;
    }

    @Override
    public int getMaximumPINLength() throws JposException {
        logGet("MaximumPINLength");
        checkOpened();
        return Data.MaximumPINLength;
    }

    @Override
    public String getMerchantID() throws JposException {
        logGet("MerchantID");
        checkOpened();
        return Data.MerchantID;
    }

    @Override
    public int getMinimumPINLength() throws JposException {
        logGet("MinimumPINLength");
        checkOpened();
        return Data.MinimumPINLength;
    }

    @Override
    public boolean getPINEntryEnabled() throws JposException {
        logGet("PINEntryEnabled");
        checkOpened();
        return Data.PINEntryEnabled;
    }

    @Override
    public int getPrompt() throws JposException {
        logGet("Prompt");
        checkOpened();
        return Data.Prompt;
    }

    @Override
    public String getPromptLanguage() throws JposException {
        logGet("PromptLanguage");
        checkOpened();
        return Data.PromptLanguage;
    }

    @Override
    public String getTerminalID() throws JposException {
        logGet("TerminalID");
        checkOpened();
        return Data.TerminalID;
    }

    @Override
    public byte[] getTrack1Data() throws JposException {
        logGet("Track1Data");
        checkOpened();
        return Arrays.copyOf(Data.Track1Data, Data.Track1Data.length);
    }

    @Override
    public byte[] getTrack2Data() throws JposException {
        logGet("Track2Data");
        checkOpened();
        return Arrays.copyOf(Data.Track2Data, Data.Track2Data.length);
    }

    @Override
    public byte[] getTrack3Data() throws JposException {
        logGet("Track3Data");
        checkOpened();
        return Arrays.copyOf(Data.Track3Data, Data.Track3Data.length);
    }

    @Override
    public byte[] getTrack4Data() throws JposException {
        logGet("Track4Data");
        checkOpened();
        return Arrays.copyOf(Data.Track4Data, Data.Track4Data.length);
    }

    @Override
    public int getTransactionType() throws JposException {
        logGet("TransactionType");
        checkOpened();
        return Data.TransactionType == null ? 0 : Data.TransactionType;
    }

    @Override
    public void setAccountNumber(String accountNumber) throws JposException {
        logPreSet("AccountNumber");
        checkOpened();
        JposDevice.check(EFTTransactionStarted, JposConst.JPOS_E_ILLEGAL, "EFT Transaction in Progress");
        PINPad.setAccountNumber(accountNumber);
        logSet("AccountNumber");
    }

    @Override
    public void setAmount(long amount) throws JposException {
        logPreSet("AccountNumber");
        checkOpened();
        JposDevice.check(EFTTransactionStarted, JposConst.JPOS_E_ILLEGAL, "EFT Transaction in Progress");
        JposDevice.check(amount < 0, JposConst.JPOS_E_ILLEGAL, "Negative Amount invalid");
        PINPad.setAmount(amount);
        logSet("AccountNumber");
    }

    @Override
    public void setMaximumPINLength(int maximumPINLength) throws JposException {
        logPreSet("MaximumPINLength");
        checkOpened();
        JposDevice.check(Data.PINEntryEnabled, JposConst.JPOS_E_ILLEGAL, "MaximumPINLength cannot be changed while PIN entry enabled");
        JposDevice.check(maximumPINLength <= 0, JposConst.JPOS_E_ILLEGAL, "MaximumPINLength must be greater than zero");
        PINPad.setMaximumPINLength(maximumPINLength);
        logSet("MaximumPINLength");
    }

    @Override
    public void setMerchantID(String merchantID) throws JposException {
        logPreSet("MerchantID");
        checkOpened();
        JposDevice.check(EFTTransactionStarted, JposConst.JPOS_E_ILLEGAL, "EFT Transaction in Progress");
        PINPad.setMerchantID(merchantID);
        logSet("MerchantID");
    }

    @Override
    public void setMinimumPINLength(int minimumPINLength) throws JposException {
        logPreSet("MinimumPINLength");
        checkOpened();
        JposDevice.check(Data.PINEntryEnabled, JposConst.JPOS_E_ILLEGAL, "MinimumPINLength cannot be changed while PIN entry enabled");
        JposDevice.check(minimumPINLength < 0, JposConst.JPOS_E_ILLEGAL, "MinimumPINLength must be a positive value");
        PINPad.setMinimumPINLength(minimumPINLength);
        logSet("MinimumPINLength");
    }

    @Override
    public void setPrompt(int prompt) throws JposException {
        long[] valid = {
                PINPadConst.PPAD_DISP_PINRESTRICTED,
                PINPadConst.PPAD_DISP_RESTRICTED_LIST,
                PINPadConst.PPAD_DISP_RESTRICTED_ORDER
        };
        logPreSet("Prompt");
        checkOpened();
        JposDevice.checkMember(Data.CapDisplay,valid, JposConst.JPOS_E_ILLEGAL, "Setting Prompt invalid for display type " + Data.CapDisplay);
        JposDevice.check(Data.CapDisplay == PINPadConst.PPAD_DISP_PINRESTRICTED && !Data.PINEntryEnabled,
                JposConst.JPOS_E_ILLEGAL, "Setting Prompt invalid while PIN entry not enabled for display type " + Data.CapDisplay);
        String[] supported = Data.AvailablePromptsList.split(",");
        for (String s : supported) {
            if (prompt == Integer.parseInt(s)) {
                PINPad.setPrompt(prompt);
                logSet("Prompt");
                return;
            }
        }
        throw new JposException(JposConst.JPOS_E_ILLEGAL, "Prompt not supported: " + prompt);
    }

    @Override
    public void setPromptLanguage(String promptLanguage) throws JposException {
        logPreSet("PromptLanguage");
        checkOpened();
        JposDevice.check(Data.CapLanguage == PINPadConst.PPAD_LANG_NONE, JposConst.JPOS_E_ILLEGAL, "No language support");
        JposDevice.check(Data.CapLanguage == PINPadConst.PPAD_LANG_ONE && !Data.PromptLanguage.equals(promptLanguage),
                JposConst.JPOS_E_ILLEGAL, "Changing language not supported");
        JposDevice.check(Data.CapLanguage == PINPadConst.PPAD_LANG_PINRESTRICTED && Data.PINEntryEnabled, JposConst.JPOS_E_BUSY,
                "Changing language not supported during PIN entry");
        String[] prompts = Data.AvailableLanguagesList.split(";");
        for (String promptstr : prompts) {
            String[] language = promptstr.split(",");
            if (promptstr.equals(promptLanguage) || (language.length > 0 && language[0].equals(promptLanguage))) {
                PINPad.setPromptLanguage(promptLanguage);
                logSet("PromptLanguage");
                return;
            }
        }
        throw new JposException(JposConst.JPOS_E_ILLEGAL, "Prompt language not supported: " + promptLanguage);
    }

    @Override
    public void setTerminalID(String terminalID) throws JposException {
        logPreSet("TerminalID");
        checkOpened();
        JposDevice.check(EFTTransactionStarted, JposConst.JPOS_E_ILLEGAL, "EFT Transaction in Progress");
        PINPad.setTerminalID(terminalID);
        logSet("TerminalID");
    }

    @Override
    public void setTrack1Data(byte[] track1Data) throws JposException {
        logPreSet("Track1Data");
        checkOpened();
        JposDevice.check(EFTTransactionStarted, JposConst.JPOS_E_ILLEGAL, "EFT Transaction in Progress");
        PINPad.setTrack1Data(track1Data);
        logSet("Track1Data");
    }

    @Override
    public void setTrack2Data(byte[] track2Data) throws JposException {
        logPreSet("Track2Data");
        checkOpened();
        JposDevice.check(EFTTransactionStarted, JposConst.JPOS_E_ILLEGAL, "EFT Transaction in Progress");
        PINPad.setTrack2Data(track2Data);
        logSet("Track2Data");
    }

    @Override
    public void setTrack3Data(byte[] track3Data) throws JposException {
        logPreSet("Track3Data");
        checkOpened();
        JposDevice.check(EFTTransactionStarted, JposConst.JPOS_E_ILLEGAL, "EFT Transaction in Progress");
        PINPad.setTrack3Data(track3Data);
        logSet("Track3Data");
    }

    @Override
    public void setTrack4Data(byte[] track4Data) throws JposException {
        logPreSet("Track4Data");
        checkOpened();
        JposDevice.check(EFTTransactionStarted, JposConst.JPOS_E_ILLEGAL, "EFT Transaction in Progress");
        PINPad.setTrack4Data(track4Data);
        logSet("Track4Data");
    }

    @Override
    public void setTransactionType(int transactionType) throws JposException {
        long[] valid = {
                PINPadConst.PPAD_TRANS_DEBIT,
                PINPadConst.PPAD_TRANS_CREDIT,
                PINPadConst.PPAD_TRANS_INQ,
                PINPadConst.PPAD_TRANS_RECONCILE,
                PINPadConst.PPAD_TRANS_ADMIN
        };
        logPreSet("TransactionType");
        checkOpened();
        JposDevice.check(EFTTransactionStarted, JposConst.JPOS_E_ILLEGAL, "EFT Transaction in Progress");
        JposDevice.checkMember(transactionType, valid, JposConst.JPOS_E_ILLEGAL, "Invalid transaction type: " + transactionType);
        PINPad.setTransactionType(transactionType);
        logSet("TransactionType");
    }

    @Override
    public void beginEFTTransaction(String pinPadSystem, int transactionHost) throws JposException {
        if (pinPadSystem == null)
            pinPadSystem = "";
        logPreCall("BeginEFTTransaction", pinPadSystem + ", " + transactionHost);
        checkEnabled();
        JposDevice.check(EFTTransactionStarted, JposConst.JPOS_E_BUSY, "EFT Transaction in Progress");
        JposDevice.check(!JposDevice.member(pinPadSystem, Data.SupportedPINPadSystems.split(",")),
                JposConst.JPOS_E_ILLEGAL, "Unsupported PINPad system: " + pinPadSystem);
        PINPad.beginEFTTransaction(pinPadSystem, transactionHost);
        logCall("BeginEFTTransaction");
        EFTTransactionStarted = true;
    }

    @Override
    public void computeMAC(String inMsg, String[] outMsg) throws JposException {
        if (inMsg == null)
            inMsg = "";
        if (outMsg == null)
            outMsg = new String[0];
        logPreCall("ComputeMAC", inMsg);
        checkEnabled();
        JposDevice.check(!EFTTransactionStarted, JposConst.JPOS_E_DISABLED, "EFT Transaction not started");
        JposDevice.check(!Data.CapMACCalculation, JposConst.JPOS_E_ILLEGAL, "MAC calculation not supported");
        JposDevice.check(Data.PINEntryEnabled, JposConst.JPOS_E_BUSY, "PIN entry active");
        JposDevice.check(outMsg.length != 1, JposConst.JPOS_E_ILLEGAL, "Type of outMsg must be String[1]");
        PINPad.computeMAC(inMsg, outMsg);
        if (outMsg[0] == null)
            logCall("ComputeMAC", "null");
        else
            logCall("ComputeMAC", outMsg[0]);
    }

    @Override
    public void enablePINEntry() throws JposException {
        logPreCall("EnablePINEntry");
        checkEnabled();
        JposDevice.check(!EFTTransactionStarted, JposConst.JPOS_E_DISABLED, "EFT Transaction not started");
        JposDevice.check(Data.PINEntryEnabled, JposConst.JPOS_E_ILLEGAL, "PIN entry active");
        JposDevice.check(Data.MaximumPINLength < Data.MinimumPINLength, JposConst.JPOS_E_ILLEGAL, "Invalid PIN length limits");
        PINPad.enablePINEntry();
        logCall("EnablePINEntry");
    }

    @Override
    public void endEFTTransaction(int completionCode) throws JposException {
        long[] valid = {
                PINPadConst.PPAD_EFT_NORMAL, PINPadConst.PPAD_EFT_ABNORMAL
        };
        logPreCall("EndEFTTransaction", "" + completionCode);
        checkEnabled();
        JposDevice.check(!EFTTransactionStarted, JposConst.JPOS_E_DISABLED, "EFT Transaction not started");
        JposDevice.checkMember(completionCode, valid, JposConst.JPOS_E_ILLEGAL, "Invalid completion code: " + completionCode);
        PINPad.endEFTTransaction(completionCode);
        logCall("EndEFTTransaction");
        EFTTransactionStarted = false;
    }

    @Override
    public void updateKey(int keyNum, String key) throws JposException {
        if (key == null)
            key = "";
        logPreCall("UpdateKey", "" + keyNum + ", " + key);
        checkEnabled();
        JposDevice.check(!EFTTransactionStarted, JposConst.JPOS_E_DISABLED, "EFT Transaction not started");
        JposDevice.check((key.length() & 1) != 0, JposConst.JPOS_E_ILLEGAL, "Must be even number of hexadecimal digits");
        for (int i = key.length() - 1; i >= 0; --i) {
            JposDevice.check("0123456789ABCDEFabcdef".indexOf(key.charAt(i)) < 0, JposConst.JPOS_E_ILLEGAL, "Invalid key: " + key);
        }
        PINPad.updateKey(keyNum, key.toUpperCase());
        logCall("UpdateKey");
    }

    @Override
    public void verifyMAC(String message) throws JposException {
        if (message == null)
            message = "";
        logPreCall("VerifyMAC", message);
        checkEnabled();
        JposDevice.check(!EFTTransactionStarted, JposConst.JPOS_E_DISABLED, "EFT Transaction not started");
        JposDevice.check(!Data.CapMACCalculation, JposConst.JPOS_E_ILLEGAL, "MAC calculation not supported");
        JposDevice.check(Data.PINEntryEnabled, JposConst.JPOS_E_BUSY, "PIN entry active");
        PINPad.verifyMAC(message);
        logCall("VerifyMAC");
    }
}
