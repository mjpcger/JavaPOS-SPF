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
import jpos.events.JposEvent;

import java.util.*;

/**
 * Class containing the PIN pad specific properties, their default values and default implementations of
 * PINPadInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter PIN Pad.
 */
public class PINPadProperties extends JposCommonProperties implements PINPadInterface {
    /**
     * UPOS property AccountNumber. Default: an empty string.
     */
    public String AccountNumber = "";
    /**
     * UPOS property AdditionalSecurityInformation. Default: an empty string.
     */
    public String AdditionalSecurityInformation = "";
    /**
     * UPOS property Amount. Default: 0.
     */
    public long Amount = 0;
    /**
     * UPOS property AvailableLanguagesList. Default: an empty string.
     */
    public String AvailableLanguagesList = "";
    /**
     * UPOS property AvailablePromptsList. Default: an empty string.
     */
    public String AvailablePromptsList = "";
    /**
     * UPOS property CapDisplay. Default: DISP_NONE. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int CapDisplay = PINPadConst.PPAD_DISP_NONE;
    /**
     * UPOS property CapKeyboard. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapKeyboard = false;
    /**
     * UPOS property CapLanguage. Default: LANG_NONE. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int CapLanguage = PINPadConst.PPAD_LANG_NONE;
    /**
     * UPOS property CapMACCalculation. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapMACCalculation = false;
    /**
     * UPOS property CapTone. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapTone = false;
    /**
     * UPOS property EncryptedPIN. Default: an empty string.
     */
    public String EncryptedPIN = "";
    /**
     * UPOS property MaximumPINLength. Must be set by objects derived from JposDevice within method changeDefaults.
     */
    public Integer MaximumPINLength = null;
    /**
     * UPOS property MinimumPINLength. Must be set by objects derived from JposDevice within method changeDefaults.
     */
    public Integer MinimumPINLength = null;
    /**
     * UPOS property MerchantID. Default: an empty string.
     */
    public String MerchantID = "";
    /**
     * UPOS property PINEntryEnabled. Default: false.
     */
    public boolean PINEntryEnabled = false;
    /**
     * UPOS property Prompt. Must be set by objects derived from JposDevice within method changeDefaults if .
     */
    public Integer Prompt = null;
    /**
     * UPOS property PromptLanguage. Default: an empty string.
     */
    public String PromptLanguage = "";
    /**
     * UPOS property TerminalID. Default: an empty string.
     */
    public String TerminalID = "";
    /**
     * UPOS property Track1Data. Default: an empty byte array.
     */
    public byte[] Track1Data = new byte[0];
    /**
     * UPOS property Track2Data. Default: an empty byte array.
     */
    public byte[] Track2Data = new byte[0];
    /**
     * UPOS property Track3Data. Default: an empty byte array.
     */
    public byte[] Track3Data = new byte[0];
    /**
     * UPOS property Track4Data. Default: an empty byte array.
     */
    public byte[] Track4Data = new byte[0];
    /**
     * UPOS property TransactionType. Default: null. Must be overwritten by application before calling BeginEFTTransaction.
     */
    public Integer TransactionType = null;

    /**
     * Internal property, contains all valid PINPadSystem parameters of method BeginEFTTransaction. The default should
     * contain all strings listed in UPOS specification for method BeginEFTTransaction, separated by comma.
     */
    public String SupportedPINPadSystems = "M/S,DUKPT,APACS40,AS2805,HGEPOS,JDEBIT2";

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected PINPadProperties(int dev) {
        super(dev);
        ExclusiveUse = ExclusiveYes;
    }

    @Override
    public void clearDataProperties() {
        super.clearDataProperties();
        if (AdditionalSecurityInformation.length() != 0) {
            AdditionalSecurityInformation = "";
            EventSource.logSet("AdditionalSecurityInformation");
        }
        if (EncryptedPIN.length() != 0) {
            EncryptedPIN = "";
            EventSource.logSet("EncryptedPIN");
        }
    }

    @Override
    public void setAccountNumber(String accountNumber) throws JposException {
        AccountNumber = accountNumber;
    }

    @Override
    public void setAmount(long amount) throws JposException {
        Amount = amount;
    }

    @Override
    public void setMaximumPINLength(int maximumPINLength) throws JposException {
        MaximumPINLength = maximumPINLength;
    }

    @Override
    public void setMerchantID(String merchantID) throws JposException {
        MerchantID = merchantID;
    }

    @Override
    public void setMinimumPINLength(int minimumPINLength) throws JposException {
        MinimumPINLength = minimumPINLength;
    }

    @Override
    public void setPrompt(int prompt) throws JposException {
        Prompt = prompt;
    }

    @Override
    public void setPromptLanguage(String promptLanguage) throws JposException {
        PromptLanguage = promptLanguage;
    }

    @Override
    public void setTerminalID(String terminalID) throws JposException {
        TerminalID = terminalID;
    }

    @Override
    public void setTrack1Data(byte[] track1Data) throws JposException {
        Track1Data = Arrays.copyOf(track1Data, track1Data.length);
    }

    @Override
    public void setTrack2Data(byte[] track2Data) throws JposException {
        Track2Data = Arrays.copyOf(track2Data, track2Data.length);
    }

    @Override
    public void setTrack3Data(byte[] track3Data) throws JposException {
        Track3Data = Arrays.copyOf(track3Data, track3Data.length);
    }

    @Override
    public void setTrack4Data(byte[] track4Data) throws JposException {
        Track4Data = Arrays.copyOf(track4Data, track4Data.length);
    }

    @Override
    public void setTransactionType(int transactionType) throws JposException {
        TransactionType = transactionType;
    }

    @Override
    public void beginEFTTransaction(String pinPadSystem, int transactionHost) throws JposException {
    }

    @Override
    public void computeMAC(String inMsg, String[] outMsg) throws JposException {
    }

    @Override
    public void enablePINEntry() throws JposException {
        PINEntryEnabled = true;
        EventSource.logSet("PINEntryEnabled");
    }

    @Override
    public void endEFTTransaction(int completionCode) throws JposException {
    }

    @Override
    public void updateKey(int keyNum, String key) throws JposException {
    }

    @Override
    public void verifyMAC(String message) throws JposException {
    }

    /**
     * Getter for DataEventList for clean synchronization between firing data or error events and setting property
     * PINEntryEnabled to the correct value.
     */
    protected List<JposEvent> getDataEventList() {
        return DataEventList;
    }
}
