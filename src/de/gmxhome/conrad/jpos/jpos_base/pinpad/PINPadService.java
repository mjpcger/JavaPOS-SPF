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
import jpos.JposException;
import jpos.services.PINPadService114;

/**
 * PINPad service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class PINPadService extends JposBase implements PINPadService114 {
    /**
     * Instance of a class implementing the PINPadInterface for PIN pad specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public PINPadInterface PINPad;

    private PINPadProperties Data;
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
    public byte[] getTrack4Data() throws JposException {
        return new byte[0];
    }

    @Override
    public void setTrack4Data(byte[] bytes) throws JposException {

    }

    @Override
    public int getCapDisplay() throws JposException {
        return 0;
    }

    @Override
    public int getCapLanguage() throws JposException {
        return 0;
    }

    @Override
    public boolean getCapKeyboard() throws JposException {
        return false;
    }

    @Override
    public boolean getCapMACCalculation() throws JposException {
        return false;
    }

    @Override
    public boolean getCapTone() throws JposException {
        return false;
    }

    @Override
    public String getAccountNumber() throws JposException {
        return null;
    }

    @Override
    public void setAccountNumber(String s) throws JposException {

    }

    @Override
    public String getAdditionalSecurityInformation() throws JposException {
        return null;
    }

    @Override
    public long getAmount() throws JposException {
        return 0;
    }

    @Override
    public void setAmount(long l) throws JposException {

    }

    @Override
    public String getAvailableLanguagesList() throws JposException {
        return null;
    }

    @Override
    public String getAvailablePromptsList() throws JposException {
        return null;
    }

    @Override
    public String getEncryptedPIN() throws JposException {
        return null;
    }

    @Override
    public int getMaximumPINLength() throws JposException {
        return 0;
    }

    @Override
    public void setMaximumPINLength(int i) throws JposException {

    }

    @Override
    public String getMerchantID() throws JposException {
        return null;
    }

    @Override
    public void setMerchantID(String s) throws JposException {

    }

    @Override
    public int getMinimumPINLength() throws JposException {
        return 0;
    }

    @Override
    public void setMinimumPINLength(int i) throws JposException {

    }

    @Override
    public boolean getPINEntryEnabled() throws JposException {
        return false;
    }

    @Override
    public int getPrompt() throws JposException {
        return 0;
    }

    @Override
    public void setPrompt(int i) throws JposException {

    }

    @Override
    public String getPromptLanguage() throws JposException {
        return null;
    }

    @Override
    public void setPromptLanguage(String s) throws JposException {

    }

    @Override
    public String getTerminalID() throws JposException {
        return null;
    }

    @Override
    public void setTerminalID(String s) throws JposException {

    }

    @Override
    public byte[] getTrack1Data() throws JposException {
        return new byte[0];
    }

    @Override
    public void setTrack1Data(byte[] bytes) throws JposException {

    }

    @Override
    public byte[] getTrack2Data() throws JposException {
        return new byte[0];
    }

    @Override
    public void setTrack2Data(byte[] bytes) throws JposException {

    }

    @Override
    public byte[] getTrack3Data() throws JposException {
        return new byte[0];
    }

    @Override
    public void setTrack3Data(byte[] bytes) throws JposException {

    }

    @Override
    public int getTransactionType() throws JposException {
        return 0;
    }

    @Override
    public void setTransactionType(int i) throws JposException {

    }

    @Override
    public void beginEFTTransaction(String s, int i) throws JposException {

    }

    @Override
    public void computeMAC(String s, String[] strings) throws JposException {

    }

    @Override
    public void enablePINEntry() throws JposException {

    }

    @Override
    public void endEFTTransaction(int i) throws JposException {

    }

    @Override
    public void updateKey(int i, String s) throws JposException {

    }

    @Override
    public void verifyMAC(String s) throws JposException {

    }
}
