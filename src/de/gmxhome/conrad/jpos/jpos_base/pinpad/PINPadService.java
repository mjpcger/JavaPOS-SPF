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

import java.util.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;
import static jpos.PINPadConst.*;

/**
 * PINPad service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class PINPadService extends JposBase implements PINPadService116 {
    /**
     * Instance of a class implementing the PINPadInterface for PIN pad specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public PINPadInterface PINPad;

    private final PINPadProperties Data;
    private boolean EFTTransactionStarted = false;

    /**
     * Reset EFT transaction, to be called whenever the PINPad needs to be reset, for example after claim or timeout.
     * The default implementation resets PINEntryEnabled and the service property EFTTransactionStarted and clears the
     * data event queue.
     * <br>This method returns true if a pending transaction has been reset, false if no transaction was pending.
     *
     * @return True if EFT transaction has been reset, false if PINpad was still idle.
     * @throws JposException If an error occurred
     */
    public boolean resetEFTTransaction() throws JposException{
        boolean res = EFTTransactionStarted;
        synchronized (Data.getDataEventList()) {
            if (Data.PINEntryEnabled) {
                Data.PINEntryEnabled = false;
                logSet("PINEntryEnabled");
            }
            Data.getDataEventList().clear();
        }
        EFTTransactionStarted = false;
        return res;
    }

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
        if (accountNumber == null)
            accountNumber = "";
        checkOpened();
        check(EFTTransactionStarted, JPOS_E_ILLEGAL, "EFT Transaction in Progress");
        checkNoChangedOrClaimed(Data.AccountNumber, accountNumber);
        PINPad.setAccountNumber(accountNumber);
        logSet("AccountNumber");
    }

    @Override
    public void setAmount(long amount) throws JposException {
        logPreSet("AccountNumber");
        checkOpened();
        check(EFTTransactionStarted, JPOS_E_ILLEGAL, "EFT Transaction in Progress");
        check(amount < 0, JPOS_E_ILLEGAL, "Negative Amount invalid");
        checkNoChangedOrClaimed(Data.Amount, amount);
        PINPad.setAmount(amount);
        logSet("AccountNumber");
    }

    @Override
    public void setMaximumPINLength(int maximumPINLength) throws JposException {
        logPreSet("MaximumPINLength");
        checkOpened();
        check(Data.PINEntryEnabled, JPOS_E_ILLEGAL, "MaximumPINLength cannot be changed while PIN entry enabled");
        check(maximumPINLength <= 0, JPOS_E_ILLEGAL, "MaximumPINLength must be greater than zero");
        checkNoChangedOrClaimed(Data.MaximumPINLength, maximumPINLength);
        PINPad.setMaximumPINLength(maximumPINLength);
        logSet("MaximumPINLength");
    }

    @Override
    public void setMerchantID(String merchantID) throws JposException {
        logPreSet("MerchantID");
        if(merchantID == null)
            merchantID = "";
        checkOpened();
        check(EFTTransactionStarted, JPOS_E_ILLEGAL, "EFT Transaction in Progress");
        checkNoChangedOrClaimed(Data.MerchantID, merchantID);
        PINPad.setMerchantID(merchantID);
        logSet("MerchantID");
    }

    @Override
    public void setMinimumPINLength(int minimumPINLength) throws JposException {
        logPreSet("MinimumPINLength");
        checkOpened();
        check(Data.PINEntryEnabled, JPOS_E_ILLEGAL, "MinimumPINLength cannot be changed while PIN entry enabled");
        check(minimumPINLength < 0, JPOS_E_ILLEGAL, "MinimumPINLength must be a positive value");
        checkNoChangedOrClaimed(Data.MinimumPINLength, minimumPINLength);
        PINPad.setMinimumPINLength(minimumPINLength);
        logSet("MinimumPINLength");
    }

    @Override
    public void setPrompt(int prompt) throws JposException {
        long[] valid = { PPAD_DISP_PINRESTRICTED, PPAD_DISP_RESTRICTED_LIST, PPAD_DISP_RESTRICTED_ORDER };
        logPreSet("Prompt");
        checkOpened();
        checkMember(Data.CapDisplay,valid, JPOS_E_ILLEGAL, "Setting Prompt invalid for display type " + Data.CapDisplay);
        check(Data.CapDisplay == PPAD_DISP_PINRESTRICTED && !Data.PINEntryEnabled,
                JPOS_E_ILLEGAL, "Setting Prompt invalid while PIN entry not enabled for display type " + Data.CapDisplay);
        String[] supported = Data.AvailablePromptsList.split(",");
        for (String s : supported) {
            if (prompt == Integer.parseInt(s)) {
                checkNoChangedOrClaimed(Data.Prompt, prompt);
                PINPad.setPrompt(prompt);
                logSet("Prompt");
                return;
            }
        }
        throw new JposException(JPOS_E_ILLEGAL, "Prompt not supported: " + prompt);
    }

    @Override
    public void setPromptLanguage(String promptLanguage) throws JposException {
        logPreSet("PromptLanguage");
        if (promptLanguage == null)
            promptLanguage = "";
        checkOpened();
        check(Data.CapLanguage == PPAD_LANG_NONE, JPOS_E_ILLEGAL, "No language support");
        check(Data.CapLanguage == PPAD_LANG_ONE && !Data.PromptLanguage.equals(promptLanguage),
                JPOS_E_ILLEGAL, "Changing language not supported");
        check(Data.CapLanguage == PPAD_LANG_PINRESTRICTED && Data.PINEntryEnabled, JPOS_E_BUSY,
                "Changing language not supported during PIN entry");
        String[] prompts = Data.AvailableLanguagesList.split(";");
        for (String promptstr : prompts) {
            String[] language = promptstr.split(",");
            if (promptstr.equals(promptLanguage) || (language.length > 0 && language[0].equals(promptLanguage))) {
                checkNoChangedOrClaimed(Data.PromptLanguage, promptLanguage);
                PINPad.setPromptLanguage(promptLanguage);
                logSet("PromptLanguage");
                return;
            }
        }
        throw new JposException(JPOS_E_ILLEGAL, "Prompt language not supported: " + promptLanguage);
    }

    @Override
    public void setTerminalID(String terminalID) throws JposException {
        logPreSet("TerminalID");
        if (terminalID == null)
            terminalID = "";
        checkOpened();
        check(EFTTransactionStarted, JPOS_E_ILLEGAL, "EFT Transaction in Progress");
        checkNoChangedOrClaimed(Data.TerminalID, terminalID);
        PINPad.setTerminalID(terminalID);
        logSet("TerminalID");
    }

    @Override
    public void setTrack1Data(byte[] track1Data) throws JposException {
        logPreSet("Track1Data");
        if (track1Data == null)
            track1Data = new byte[0];
        checkOpened();
        check(EFTTransactionStarted, JPOS_E_ILLEGAL, "EFT Transaction in Progress");
        checkNoChangedOrClaimed(Data.Track1Data, track1Data);
        PINPad.setTrack1Data(track1Data);
        logSet("Track1Data");
    }

    @Override
    public void setTrack2Data(byte[] track2Data) throws JposException {
        logPreSet("Track2Data");
        if (track2Data == null)
            track2Data = new byte[0];
        checkOpened();
        check(EFTTransactionStarted, JPOS_E_ILLEGAL, "EFT Transaction in Progress");
        checkNoChangedOrClaimed(Data.Track2Data, track2Data);
        PINPad.setTrack2Data(track2Data);
        logSet("Track2Data");
    }

    @Override
    public void setTrack3Data(byte[] track3Data) throws JposException {
        logPreSet("Track3Data");
        if (track3Data == null)
            track3Data = new byte[0];
        checkOpened();
        check(EFTTransactionStarted, JPOS_E_ILLEGAL, "EFT Transaction in Progress");
        checkNoChangedOrClaimed(Data.Track3Data, track3Data);
        PINPad.setTrack3Data(track3Data);
        logSet("Track3Data");
    }

    @Override
    public void setTrack4Data(byte[] track4Data) throws JposException {
        logPreSet("Track4Data");
        if (track4Data == null)
            track4Data = new byte[0];
        checkOpened();
        check(EFTTransactionStarted, JPOS_E_ILLEGAL, "EFT Transaction in Progress");
        checkNoChangedOrClaimed(Data.Track4Data, track4Data);
        PINPad.setTrack4Data(track4Data);
        logSet("Track4Data");
    }

    @Override
    public void setTransactionType(int transactionType) throws JposException {
        long[] valid = { PPAD_TRANS_DEBIT, PPAD_TRANS_CREDIT, PPAD_TRANS_INQ, PPAD_TRANS_RECONCILE, PPAD_TRANS_ADMIN };
        logPreSet("TransactionType");
        checkOpened();
        check(EFTTransactionStarted, JPOS_E_ILLEGAL, "EFT Transaction in Progress");
        checkMember(transactionType, valid, JPOS_E_ILLEGAL, "Invalid transaction type: " + transactionType);
        checkNoChangedOrClaimed(Data.TransactionType, transactionType);
        PINPad.setTransactionType(transactionType);
        logSet("TransactionType");
    }

    @Override
    public void beginEFTTransaction(String pinPadSystem, int transactionHost) throws JposException {
        logPreCall("BeginEFTTransaction", removeOuterArraySpecifier(new Object[]{pinPadSystem, transactionHost}, Device.MaxArrayStringElements));
        if (pinPadSystem == null)
            pinPadSystem = "";
        checkEnabled();
        check(EFTTransactionStarted, JPOS_E_BUSY, "EFT Transaction in Progress");
        check(!member(pinPadSystem, Data.SupportedPINPadSystems.split(",")),
                JPOS_E_ILLEGAL, "Unsupported PINPad system: " + pinPadSystem);
        PINPad.beginEFTTransaction(pinPadSystem, transactionHost);
        logCall("BeginEFTTransaction");
        EFTTransactionStarted = true;
    }

    @Override
    public void computeMAC(String inMsg, String[] outMsg) throws JposException {
        logPreCall("ComputeMAC", removeOuterArraySpecifier(new Object[]{inMsg, outMsg}, Device.MaxArrayStringElements));
        if (inMsg == null)
            inMsg = "";
        if (outMsg == null)
            outMsg = new String[0];
        checkEnabled();
        check(!EFTTransactionStarted, JPOS_E_DISABLED, "EFT Transaction not started");
        check(!Data.CapMACCalculation, JPOS_E_ILLEGAL, "MAC calculation not supported");
        check(Data.PINEntryEnabled, JPOS_E_BUSY, "PIN entry active");
        check(outMsg.length != 1, JPOS_E_ILLEGAL, "Type of outMsg must be String[1]");
        PINPad.computeMAC(inMsg, outMsg);
        logCall("ComputeMAC", removeOuterArraySpecifier(new Object[]{outMsg[0]}, Device.MaxArrayStringElements));
    }

    @Override
    public void enablePINEntry() throws JposException {
        logPreCall("EnablePINEntry");
        checkEnabled();
        check(!EFTTransactionStarted, JPOS_E_DISABLED, "EFT Transaction not started");
        check(Data.PINEntryEnabled, JPOS_E_ILLEGAL, "PIN entry active");
        check(Data.MaximumPINLength < Data.MinimumPINLength, JPOS_E_ILLEGAL, "Invalid PIN length limits");
        PINPad.enablePINEntry();
        logCall("EnablePINEntry");
    }

    @Override
    public void endEFTTransaction(int completionCode) throws JposException {
        logPreCall("EndEFTTransaction", removeOuterArraySpecifier(new Object[]{completionCode}, Device.MaxArrayStringElements));
        long[] valid = { PPAD_EFT_NORMAL, PPAD_EFT_ABNORMAL };
        checkEnabled();
        check(!EFTTransactionStarted, JPOS_E_DISABLED, "EFT Transaction not started");
        checkMember(completionCode, valid, JPOS_E_ILLEGAL, "Invalid completion code: " + completionCode);
        PINPad.endEFTTransaction(completionCode);
        logCall("EndEFTTransaction");
        EFTTransactionStarted = false;
    }

    @Override
    public void updateKey(int keyNum, String key) throws JposException {
        logPreCall("UpdateKey", removeOuterArraySpecifier(new Object[]{keyNum, key}, Device.MaxArrayStringElements));
        if (key == null)
            key = "";
        checkEnabled();
        check(!EFTTransactionStarted, JPOS_E_DISABLED, "EFT Transaction not started");
        check((key.length() & 1) != 0, JPOS_E_ILLEGAL, "Must be even number of hexadecimal digits");
        for (int i = key.length() - 1; i >= 0; --i) {
            check("0123456789ABCDEFabcdef".indexOf(key.charAt(i)) < 0, JPOS_E_ILLEGAL, "Invalid key: " + key);
        }
        PINPad.updateKey(keyNum, key.toUpperCase());
        logCall("UpdateKey");
    }

    @Override
    public void verifyMAC(String message) throws JposException {
        logPreCall("VerifyMAC", removeOuterArraySpecifier(new Object[]{message}, Device.MaxArrayStringElements));
        if (message == null)
            message = "";
        checkEnabled();
        check(!EFTTransactionStarted, JPOS_E_DISABLED, "EFT Transaction not started");
        check(!Data.CapMACCalculation, JPOS_E_ILLEGAL, "MAC calculation not supported");
        check(Data.PINEntryEnabled, JPOS_E_BUSY, "PIN entry active");
        PINPad.verifyMAC(message);
        logCall("VerifyMAC");
    }
}
