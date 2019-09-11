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

package de.gmxhome.conrad.jpos.jpos_base.msr;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Class containing the MSR specific properties, their default values and default implementations of
 * MSRInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter MSR - Magnetic Stripe
 * Reader.
 */
public class MSRProperties extends JposCommonProperties implements MSRInterface {
    /**
     * UPOS property CapCardAuthentication. Default: "". Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String CapCardAuthentication = "";

    /**
     * UPOS property CapDataEncryption. Default: MSRConst.MSR_DE_NONE. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapDataEncryption = MSRConst.MSR_DE_NONE;

    /**
     * UPOS property CapDeviceAuthentication. Default: MSRConst.MSR_DA_NOT_SUPPORTED. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapDeviceAuthentication = MSRConst.MSR_DA_NOT_SUPPORTED;

    /**
     * UPOS property CapTrackDataMasking. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapTrackDataMasking = false;

    /**
     * UPOS property CapWritableTracks. Default: MSRConst.MSR_TR_NONE. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapWritableTracks = MSRConst.MSR_TR_NONE;

    /**
     * UPOS property CapTransmitSentinels. Default: true. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapTransmitSentinels = true;

    /**
     * UPOS property CapISO. Default: "". Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapISO = false;

    /**
     * UPOS property CapJISOne. Default: "". Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapJISOne = false;

    /**
     * UPOS property CapJISTwo. Default: "". Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapJISTwo = false;

    /**
     * UPOS property AdditionalSecurityInformation.
     */
    public byte[] AdditionalSecurityInformation;

    /**
     * UPOS property CardAuthenticationData.
     */
    public byte[] CardAuthenticationData;

    /**
     * UPOS property CardAuthenticationDataLength.
     */
    public int CardAuthenticationDataLength;

    /**
     * UPOS property CardPropertyList.
     */
    public String CardPropertyList;

    /**
     * UPOS property CardType.
     */
    public String CardType;

    /**
     * UPOS property CardTypeList. Default: "". Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String CardTypeList = "";

    /**
     * UPOS property DataEncryptionAlgorithm. Default: "". Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int DataEncryptionAlgorithm = MSRConst.MSR_DE_NONE;

    /**
     * UPOS property DeviceAuthenticated.
     */
    public boolean DeviceAuthenticated;

    /**
     * UPOS property DeviceAuthenticationProtocol. Default: "". Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int DeviceAuthenticationProtocol = MSRConst.MSR_AP_NONE;

    /**
     * UPOS property Track1EncryptedData.
     */
    public byte[] Track1EncryptedData;

    /**
     * UPOS property Track1EncryptedDataLength.
     */
    public int Track1EncryptedDataLength;

    /**
     * UPOS property Track2EncryptedData.
     */
    public byte[] Track2EncryptedData;

    /**
     * UPOS property Track2EncryptedDataLength.
     */
    public int Track2EncryptedDataLength;

    /**
     * UPOS property Track3EncryptedData.
     */
    public byte[] Track3EncryptedData;

    /**
     * UPOS property Track3EncryptedDataLength.
     */
    public int Track3EncryptedDataLength;

    /**
     * UPOS property Track4EncryptedData.
     */
    public byte[] Track4EncryptedData;

    /**
     * UPOS property Track4EncryptedDataLength.
     */
    public int Track4EncryptedDataLength;

    /**
     * UPOS property WriteCardType.
     */
    public String WriteCardType;

    /**
     * UPOS property EncodingMaxLength.
     */
    public int EncodingMaxLength;

    /**
     * UPOS property TracksToWrite.
     */
    public int TracksToWrite;

    /**
     * UPOS property Track1Data.
     */
    public byte[] Track1Data;

    /**
     * UPOS property Track2Data.
     */
    public byte[] Track2Data;

    /**
     * UPOS property Track3Data.
     */
    public byte[] Track3Data;

    /**
     * UPOS property Track4Data.
     */
    public byte[] Track4Data;

    /**
     * UPOS property TransmitSentinels.
     */
    public boolean TransmitSentinels;

    /**
     * UPOS property AccountNumber.
     */
    public String AccountNumber;

    /**
     * UPOS property DecodeData.
     */
    public boolean DecodeData;

    /**
     * UPOS property ErrorReportingType.
     */
    public int ErrorReportingType;

    /**
     * UPOS property ExpirationDate.
     */
    public String ExpirationDate;

    /**
     * UPOS property FirstName.
     */
    public String FirstName;

    /**
     * UPOS property MiddleInitial.
     */
    public String MiddleInitial;

    /**
     * UPOS property ParseDecodeData.
     */
    public boolean ParseDecodeData;

    /**
     * UPOS property ServiceCode.
     */
    public String ServiceCode;

    /**
     * UPOS property Suffix.
     */
    public String Suffix;

    /**
     * UPOS property Surname.
     */
    public String Surname;

    /**
     * UPOS property Title.
     */
    public String Title;

    /**
     * UPOS property Track1DiscretionaryData.
     */
    public byte[] Track1DiscretionaryData;

    /**
     * UPOS property Track2DiscretionaryData.
     */
    public byte[] Track2DiscretionaryData;

    /**
     * UPOS property TracksToRead.
     */
    public int TracksToRead;

    /**
     * Constructor. Sets ExclusiveUse to ExclusiveYes to match the MSR device model.
     *
     * @param dev Device index
     */
    public MSRProperties(int dev) {
        super(dev);
    }

    @Override
    public void initOnOpen() {
        super.initOnOpen();
        CardPropertyList = "";
        CardType = "";
        WriteCardType = "BANK";
        EncodingMaxLength = 0;
        TracksToWrite = MSRConst.MSR_TR_NONE;
        TransmitSentinels = false;
        DecodeData = true;
        ErrorReportingType = MSRConst.MSR_ERT_CARD;
        ParseDecodeData = true;
        TracksToRead = MSRConst.MSR_TR_1_2_3;
        // The following fields will be filled to hold default values that never will be filled
        // when using the defaults
        AdditionalSecurityInformation = new byte[0];
        CardAuthenticationData = new byte[0];
        CardAuthenticationDataLength = 0;
        Track1Data = new byte[0];
        Track1DiscretionaryData = new byte[0];
        Track1EncryptedData = new byte[0];
        Track1EncryptedDataLength = 0;  // Not specified whether this is correct
        Track2Data = new byte[0];
        Track2DiscretionaryData = new byte[0];
        Track2EncryptedData = new byte[0];
        Track2EncryptedDataLength = 0;  // Not specified whether this is correct
        Track3Data = new byte[0];
        Track3EncryptedData = new byte[0];
        Track3EncryptedDataLength = 0;  // Not specified whether this is correct
        Track4Data = new byte[0];
        Track4EncryptedData = new byte[0];
        Track4EncryptedDataLength = 0;  // Not specified whether this is correct
        AccountNumber = "";
        ExpirationDate = "";
        FirstName = "";
        MiddleInitial = "";
        ServiceCode = "";
        Suffix = "";
        Surname = "";
        Title = "";
    }

    @Override
    public void initOnEnable(boolean enable) {
        if (enable) {
            super.initOnEnable(enable);
            DeviceAuthenticated = false;
        }
    }

    @Override
    public void setDataProperties(Object tracks) {
    }

    @Override
    public void dataEncryptionAlgorithm(int b) throws JposException {
        DataEncryptionAlgorithm = b;
    }

    @Override
    public void writeCardType(String b) throws JposException {
        WriteCardType = b;
    }

    @Override
    public void tracksToWrite(int b) throws JposException {
        TracksToWrite = b;
    }

    @Override
    public void transmitSentinels(boolean b) throws JposException {
        TransmitSentinels = b;
    }

    @Override
    public void decodeData(boolean b) throws JposException {
        if ((DecodeData = b) == false) {
            ParseDecodeData = false;
            EventSource.logSet("ParseDecodeData");
        }
    }

    @Override
    public void errorReportingType(int b) throws JposException {
        ErrorReportingType = b;
    }

    @Override
    public void parseDecodeData(boolean b) throws JposException {
        if (ParseDecodeData = b) {
            DecodeData = true;
            EventSource.logSet("DecodeData");
        }
    }

    @Override
    public void tracksToRead(int b) throws JposException {
        TracksToRead = b;
    }

    @Override
    public void authenticateDevice(byte[] response) throws JposException {
    }

    @Override
    public void deauthenticateDevice(byte[] response) throws JposException {
    }

    @Override
    public void retrieveCardProperty(String name, String[] value) throws JposException {
    }

    @Override
    public void retrieveDeviceAuthenticationData(byte[][] challenge) throws JposException {
    }

    @Override
    public void updateKey(String key, String keyName) throws JposException {
    }

    @Override
    public void writeTracks(byte[][] data, int timeout) throws JposException {
    }
}
