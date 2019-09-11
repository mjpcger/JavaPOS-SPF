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
import jpos.services.MSRService114;

import java.util.Arrays;

/**
 * MSR service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class MSRService extends JposBase implements MSRService114 {
    /**
     * Instance of a class implementing the MSRInterface for magnetic stripe reader specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public MSRInterface MSRInterface;

    private MSRProperties Data;

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public MSRService(MSRProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public String getCapCardAuthentication() throws JposException {
        checkOpened();
        logGet("CapCardAuthentication");
        return Data.CapCardAuthentication;
    }

    @Override
    public int getCapDataEncryption() throws JposException {
        checkOpened();
        logGet("CapDataEncryption");
        return Data.CapDataEncryption;
    }

    @Override
    public int getCapDeviceAuthentication() throws JposException {
        checkOpened();
        logGet("CapDeviceAuthentication");
        return Data.CapDeviceAuthentication;
    }

    @Override
    public boolean getCapTrackDataMasking() throws JposException {
        checkOpened();
        logGet("CapTrackDataMasking");
        return Data.CapTrackDataMasking;
    }

    @Override
    public int getCapWritableTracks() throws JposException {
        Device.check(Props.State == JposConst.JPOS_S_CLOSED, JposConst.JPOS_E_CLOSED, "Device not opened");
        logGet("CapWritableTracks");
        return Data.CapWritableTracks;
    }

    @Override
    public boolean getCapTransmitSentinels() throws JposException {
        checkOpened();
        logGet("CapTransmitSentinels");
        return Data.CapTransmitSentinels;
    }

    @Override
    public boolean getCapISO() throws JposException {
        checkOpened();
        logGet("CapISO");
        return Data.CapISO;
    }

    @Override
    public boolean getCapJISOne() throws JposException {
        checkOpened();
        logGet("CapJISOne");
        return Data.CapJISOne;
    }

    @Override
    public boolean getCapJISTwo() throws JposException {
        checkOpened();
        logGet("CapJISTwo");
        return Data.CapJISTwo;
    }

    @Override
    public byte[] getAdditionalSecurityInformation() throws JposException {
        checkOpened();
        logGet("AdditionalSecurityInformation");
        return Arrays.copyOf(Data.AdditionalSecurityInformation, Data.AdditionalSecurityInformation.length);
    }

    @Override
    public byte[] getCardAuthenticationData() throws JposException {
        checkOpened();
        logGet("CardAuthenticationData");
        return Arrays.copyOf(Data.CardAuthenticationData, Data.CardAuthenticationData.length);
    }

    @Override
    public int getCardAuthenticationDataLength() throws JposException {
        checkOpened();
        logGet("CardAuthenticationDataLength");
        return Data.CardAuthenticationDataLength;
    }

    @Override
    public String getCardPropertyList() throws JposException {
        checkOpened();
        logGet("CardPropertyList");
        return Data.CardPropertyList;
    }

    @Override
    public String getCardType() throws JposException {
        checkOpened();
        logGet("CardType");
        return Data.CardType;
    }

    @Override
    public String getCardTypeList() throws JposException {
        checkOpened();
        logGet("CardTypeList");
        return Data.CardTypeList;
    }

    @Override
    public int getDataEncryptionAlgorithm() throws JposException {
        checkOpened();
        logGet("DataEncryptionAlgorithm");
        return Data.DataEncryptionAlgorithm;
    }

    @Override
    public void setDataEncryptionAlgorithm(int i) throws JposException {
        logPreSet("DataEncryptionAlgorithm");
        checkEnabled();
        Device.check((Data.CapDataEncryption & i) != i, JposConst.JPOS_E_ILLEGAL, "Invalid data encryption: " + i);
        MSRInterface.dataEncryptionAlgorithm(i);
        logSet("DataEncryptionAlgorithm");
    }

    @Override
    public boolean getDeviceAuthenticated() throws JposException {
        checkOpened();
        logGet("DeviceAuthenticated");
        return Data.DeviceAuthenticated;
    }

    @Override
    public int getDeviceAuthenticationProtocol() throws JposException {
        checkOpened();
        logGet("DeviceAuthenticationProtocol");
        return Data.DeviceAuthenticationProtocol;
    }

    @Override
    public byte[] getTrack1EncryptedData() throws JposException {
        checkOpened();
        logGet("Track1EncryptedData");
        return Arrays.copyOf(Data.Track1EncryptedData, Data.Track1EncryptedData.length);
    }

    @Override
    public int getTrack1EncryptedDataLength() throws JposException {
        checkOpened();
        logGet("Track1EncryptedDataLength");
        return Data.Track1EncryptedDataLength;
    }

    @Override
    public byte[] getTrack2EncryptedData() throws JposException {
        checkOpened();
        logGet("Track2EncryptedData");
        return Arrays.copyOf(Data.Track2EncryptedData, Data.Track2EncryptedData.length);
    }

    @Override
    public int getTrack2EncryptedDataLength() throws JposException {
        checkOpened();
        logGet("Track2EncryptedDataLength");
        return Data.Track2EncryptedDataLength;
    }

    @Override
    public byte[] getTrack3EncryptedData() throws JposException {
        checkOpened();
        logGet("Track3EncryptedData");
        return Arrays.copyOf(Data.Track3EncryptedData, Data.Track3EncryptedData.length);
    }

    @Override
    public int getTrack3EncryptedDataLength() throws JposException {
        checkOpened();
        logGet("Track3EncryptedDataLength");
        return Data.Track3EncryptedDataLength;
    }

    @Override
    public byte[] getTrack4EncryptedData() throws JposException {
        checkOpened();
        logGet("Track4EncryptedData");
        return Arrays.copyOf(Data.Track4EncryptedData, Data.Track4EncryptedData.length);
    }

    @Override
    public int getTrack4EncryptedDataLength() throws JposException {
        checkOpened();
        logGet("Track4EncryptedDataLength");
        return Data.Track4EncryptedDataLength;
    }

    @Override
    public String getWriteCardType() throws JposException {
        checkOpened();
        logGet("WriteCardType");
        return Data.WriteCardType;
    }

    @Override
    public void setWriteCardType(String s) throws JposException {
        logPreSet("WriteCardType");
        checkOpened();
        Device.check(!Device.member(s, Data.CardTypeList.split(",")), JposConst.JPOS_E_ILLEGAL, "Invalid card type: "+ s);
        MSRInterface.writeCardType(s);
        logSet("WriteCardType");
    }

    @Override
    public int getEncodingMaxLength() throws JposException {
        checkOpened();
        logGet("EncodingMaxLength");
        return Data.EncodingMaxLength;
    }

    @Override
    public int getTracksToWrite() throws JposException {
        checkOpened();
        logGet("TracksToWrite");
        return Data.TracksToWrite;
    }

    @Override
    public void setTracksToWrite(int i) throws JposException {
        logPreSet("TracksToWrite");
        checkOpened();
        Device.check((Data.CapWritableTracks & i) != i, JposConst.JPOS_E_ILLEGAL, "Invalid track selection: " + i);
        MSRInterface.tracksToWrite(i);
        logSet("TracksToWrite");
    }

    @Override
    public byte[] getTrack4Data() throws JposException {
        checkOpened();
        logGet("Track4Data");
        return Arrays.copyOf(Data.Track4Data, Data.Track4Data.length);
    }

    @Override
    public boolean getTransmitSentinels() throws JposException {
        checkOpened();
        logGet("TransmitSentinels");
        return Data.TransmitSentinels;
    }

    @Override
    public void setTransmitSentinels(boolean b) throws JposException {
        logPreSet("TransmitSentinels");
        checkOpened();
        Device.check(!Data.CapTransmitSentinels && b, JposConst.JPOS_E_ILLEGAL, "Sentinel transmission not supported");
        MSRInterface.transmitSentinels(b);
        logSet("TransmitSentinels");
    }

    @Override
    public String getAccountNumber() throws JposException {
        checkOpened();
        logGet("AccountNumber");
        return Data.AccountNumber;
    }

    @Override
    public boolean getDecodeData() throws JposException {
        checkOpened();
        logGet("DecodeData");
        return Data.DecodeData;
    }

    @Override
    public void setDecodeData(boolean b) throws JposException {
        logPreSet("DecodeData");
        checkOpened();
        MSRInterface.decodeData(b);
        logSet("DecodeData");
    }

    @Override
    public int getErrorReportingType() throws JposException {
        checkOpened();
        logGet("ErrorReportingType");
        return Data.ErrorReportingType;
    }

    @Override
    public void setErrorReportingType(int i) throws JposException {
        logPreSet("ErrorReportingType");
        checkOpened();
        Device.check(Data.CapDataEncryption == MSRConst.MSR_DE_NONE, JposConst.JPOS_E_ILLEGAL, "Data encryption not supported");
        Device.checkMember(i, new long[]{MSRConst.MSR_ERT_CARD, MSRConst.MSR_ERT_TRACK}, JposConst.JPOS_E_ILLEGAL, "Invalid error reporting: " + i);
        MSRInterface.errorReportingType(i);
        logSet("ErrorReportingType");
    }

    @Override
    public String getExpirationDate() throws JposException {
        checkOpened();
        logGet("ExpirationDate");
        return Data.ExpirationDate;
    }

    @Override
    public String getFirstName() throws JposException {
        checkOpened();
        logGet("FirstName");
        return Data.FirstName;
    }

    @Override
    public String getMiddleInitial() throws JposException {
        checkOpened();
        logGet("MiddleInitial");
        return Data.MiddleInitial;
    }

    @Override
    public boolean getParseDecodeData() throws JposException {
        checkOpened();
        logGet("ParseDecodeData");
        return Data.ParseDecodeData;
    }

    @Override
    public void setParseDecodeData(boolean b) throws JposException {
        logPreSet("ParseDecodeData");
        checkOpened();
        MSRInterface.parseDecodeData(b);
        logSet("ParseDecodeData");
    }

    @Override
    public String getServiceCode() throws JposException {
        checkOpened();
        logGet("ServiceCode");
        return Data.ServiceCode;
    }

    @Override
    public String getSuffix() throws JposException {
        checkOpened();
        logGet("Suffix");
        return Data.Suffix;
    }

    @Override
    public String getSurname() throws JposException {
        checkOpened();
        logGet("Surname");
        return Data.Surname;
    }

    @Override
    public String getTitle() throws JposException {
        checkOpened();
        logGet("Title");
        return Data.Title;
    }

    @Override
    public byte[] getTrack1Data() throws JposException {
        checkOpened();
        logGet("Track1Data");
        return Arrays.copyOf(Data.Track1Data, Data.Track1Data.length);
    }

    @Override
    public byte[] getTrack1DiscretionaryData() throws JposException {
        checkOpened();
        logGet("Track1DiscretionaryData");
        return Arrays.copyOf(Data.Track1DiscretionaryData, Data.Track1DiscretionaryData.length);
    }

    @Override
    public byte[] getTrack2Data() throws JposException {
        checkOpened();
        logGet("Track2Data");
        return Arrays.copyOf(Data.Track2Data, Data.Track2Data.length);
    }

    @Override
    public byte[] getTrack2DiscretionaryData() throws JposException {
        checkOpened();
        logGet("Track2DiscretionaryData");
        return Arrays.copyOf(Data.Track2DiscretionaryData, Data.Track2DiscretionaryData.length);
    }

    @Override
    public byte[] getTrack3Data() throws JposException {
        checkOpened();
        logGet("Track3Data");
        return Arrays.copyOf(Data.Track3Data, Data.Track3Data.length);
    }

    @Override
    public int getTracksToRead() throws JposException {
        checkOpened();
        logGet("TracksToRead");
        return Data.TracksToRead;
    }

    @Override
    public void setTracksToRead(int i) throws JposException {
        logPreSet("TracksToRead");
        checkOpened();
        Device.check((MSRConst.MSR_TR_1_2_3_4 & i) != i, JposConst.JPOS_E_ILLEGAL, "Invalid track selection: " + i);
        MSRInterface.tracksToRead(i);
        logSet("TracksToRead");
    }

    @Override
    public void clearInputProperties() throws JposException {
        MSRInterface.clearInput();
    }

    @Override
    public void authenticateDevice(byte[] response) throws JposException {
        logPreCall("AuthenticateDevice", "" + response);
        checkEnabled();
        Device.check(Data.CapDeviceAuthentication == MSRConst.MSR_DA_NOT_SUPPORTED, JposConst.JPOS_E_ILLEGAL, "Authentication not supported");
        MSRInterface.authenticateDevice(response);
        logCall("AuthenticateDevice");
    }

    @Override
    public void deauthenticateDevice(byte[] response) throws JposException {
        logPreCall("DeauthenticateDevice", "" + response);
        checkEnabled();
        Device.check(Data.CapDeviceAuthentication == MSRConst.MSR_DA_NOT_SUPPORTED, JposConst.JPOS_E_ILLEGAL, "Authentication not supported");
        MSRInterface.deauthenticateDevice(response);
        logCall("DeauthenticateDevice");
    }

    @Override
    public void retrieveCardProperty(String name, String[] value) throws JposException {
        logPreCall("RetrieveCardProperty", name + ", ...");
        checkEnabled();
        Device.check(!Device.member(name, Data.CardPropertyList.split(",")), JposConst.JPOS_E_ILLEGAL, "Invalid card property: " + name);
        Device.check(Data.CapDeviceAuthentication == MSRConst.MSR_DA_NOT_SUPPORTED, JposConst.JPOS_E_ILLEGAL, "Authentication not supported");
        Device.check(value == null || value.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid value array");
        MSRInterface.retrieveCardProperty(name, value);
        logCall("RetrieveCardProperty", name + ", " + value[0]);
    }

    @Override
    public void retrieveDeviceAuthenticationData(byte[][] challenge) throws JposException {
        logPreCall("RetrieveDeviceAuthenticationData", "" + challenge[0]);
        checkEnabled();
        Device.check(Data.CapDeviceAuthentication == MSRConst.MSR_DA_NOT_SUPPORTED, JposConst.JPOS_E_ILLEGAL, "Authentication not supported");
        Device.check(challenge == null || challenge.length != 1, JposConst.JPOS_E_ILLEGAL, "Invalid challenge array");
        MSRInterface.retrieveDeviceAuthenticationData(challenge);
        logCall("RetrieveDeviceAuthenticationData", "" + challenge[0]);
    }

    @Override
    public void retrieveDeviceAuthenticationData(byte[] challenge) throws JposException {
        MSRInterface.retrieveDeviceAuthenticationData(new byte[][]{challenge});
    }

    @Override
    public void updateKey(String key, String keyName) throws JposException {
        logPreCall("UpdateKey", key + ", " + keyName);
        checkEnabled();
        Device.check(Data.CapDeviceAuthentication == MSRConst.MSR_DA_NOT_SUPPORTED, JposConst.JPOS_E_ILLEGAL, "Authentication not supported");
        MSRInterface.updateKey(key, keyName);
        logCall("UpdateKey");
    }

    @Override
    public void writeTracks(byte[][] data, int timeout) throws JposException {
        String trackdata = "";
        for (byte[] track : data) {
            Device.check(data == null, JposConst.JPOS_E_ILLEGAL, "Illegal track");
            trackdata = trackdata + ", " + track;
        }
        logPreCall("WriteTracks", "{" + trackdata.substring(1) + " }, " + timeout);
        checkEnabled();
        Device.check(Data.CapWritableTracks == MSRConst.MSR_TR_NONE, JposConst.JPOS_E_ILLEGAL, "Write tracks not supported");
        Device.check(Data.TracksToWrite == MSRConst.MSR_TR_NONE, JposConst.JPOS_E_FAILURE, "Selected tracks cannot be written: " + Data.TracksToWrite);
        int[] trackspec = new int[]{MSRConst.MSR_TR_1, MSRConst.MSR_TR_2, MSRConst.MSR_TR_3, MSRConst.MSR_TR_4};
        for (int checkindex = 0; checkindex < trackspec.length; checkindex++) {
            if ((trackspec[checkindex] & Data.TracksToWrite) == 0) {
                Device.check(data[checkindex].length > 0, JposConst.JPOS_E_ILLEGAL, "Trackdata specified for non-writable track " + (checkindex + 1));
            } else {
                Device.check(data[checkindex].length > Data.EncodingMaxLength, JposConst.JPOS_E_ILLEGAL, "Data too long for track " + (checkindex + 1));
            }
        }
        MSRInterface.writeTracks(data, timeout);
        logCall("WriteTracks");
    }
}
