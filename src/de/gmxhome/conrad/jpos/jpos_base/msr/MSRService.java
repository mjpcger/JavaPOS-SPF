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
import jpos.services.*;

import java.util.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;
import static jpos.MSRConst.*;

/**
 * MSR service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class MSRService extends JposBase implements MSRService116 {
    /**
     * Instance of a class implementing the MSRInterface for magnetic stripe reader specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public MSRInterface MSRInterface;

    private final MSRProperties Data;

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
        check(Props.State == JPOS_S_CLOSED, JPOS_E_CLOSED, "Device not opened");
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
        check((Data.CapDataEncryption & i) != i, JPOS_E_ILLEGAL, "Invalid data encryption: " + i);
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
        if (s == null)
            s = "";
        checkOpened();
        check(!member(s, Data.CardTypeList.split(",")), JPOS_E_ILLEGAL, "Invalid card type: "+ s);
        checkNoChangedOrClaimed(Data.WriteCardType, s);
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
        check((Data.CapWritableTracks & i) != i, JPOS_E_ILLEGAL, "Invalid track selection: " + i);
        checkNoChangedOrClaimed(Data.TracksToWrite, i);
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
        check(!Data.CapTransmitSentinels && b, JPOS_E_ILLEGAL, "Sentinel transmission not supported");
        checkNoChangedOrClaimed(Data.TransmitSentinels, b);
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
        checkNoChangedOrClaimed(Data.DecodeData, b);
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
        checkMember(i, new long[]{MSR_ERT_CARD, MSR_ERT_TRACK}, JPOS_E_ILLEGAL, "Invalid error reporting: " + i);
        checkNoChangedOrClaimed(Data.ErrorReportingType, i);
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
        checkNoChangedOrClaimed(Data.ParseDecodeData, b);
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
        check((MSR_TR_1_2_3_4 & i) != i, JPOS_E_ILLEGAL, "Invalid track selection: " + i);
        checkNoChangedOrClaimed(Data.TracksToRead, i);
        MSRInterface.tracksToRead(i);
        logSet("TracksToRead");
    }

    @Override
    public void clearInputProperties() throws JposException {
        MSRInterface.clearInput();
    }

    @Override
    public void authenticateDevice(byte[] response) throws JposException {
        logPreCall("AuthenticateDevice", removeOuterArraySpecifier(new Object[]{response}, Device.MaxArrayStringElements));
        checkEnabled();
        check(Data.CapDeviceAuthentication == MSR_DA_NOT_SUPPORTED, JPOS_E_ILLEGAL, "Authentication not supported");
        MSRInterface.authenticateDevice(response);
        logCall("AuthenticateDevice");
    }

    @Override
    public void deauthenticateDevice(byte[] response) throws JposException {
        logPreCall("DeauthenticateDevice", removeOuterArraySpecifier(new Object[]{response}, Device.MaxArrayStringElements));
        checkEnabled();
        check(Data.CapDeviceAuthentication == MSR_DA_NOT_SUPPORTED, JPOS_E_ILLEGAL, "Authentication not supported");
        MSRInterface.deauthenticateDevice(response);
        logCall("DeauthenticateDevice");
    }

    @Override
    public void retrieveCardProperty(String name, String[] value) throws JposException {
        logPreCall("RetrieveCardProperty", name + ", ...");
        checkEnabled();
        check(!member(name, Data.CardPropertyList.split(",")), JPOS_E_ILLEGAL, "Invalid card property: " + name);
        check(Data.CapDeviceAuthentication == MSR_DA_NOT_SUPPORTED, JPOS_E_ILLEGAL, "Authentication not supported");
        check(value == null || value.length != 1, JPOS_E_ILLEGAL, "Invalid value array");
        MSRInterface.retrieveCardProperty(name, value);
        logCall("RetrieveCardProperty", name + ", " + value[0]);
    }

    @Override
    public void retrieveDeviceAuthenticationData(byte[][] challenge) throws JposException {
        logPreCall("RetrieveDeviceAuthenticationData", removeOuterArraySpecifier(new Object[]{challenge}, Device.MaxArrayStringElements));
        checkEnabled();
        check(Data.CapDeviceAuthentication == MSR_DA_NOT_SUPPORTED, JPOS_E_ILLEGAL, "Authentication not supported");
        check(challenge == null || challenge.length != 1, JPOS_E_ILLEGAL, "Invalid challenge array");
        MSRInterface.retrieveDeviceAuthenticationData(challenge);
        logCall("RetrieveDeviceAuthenticationData", removeOuterArraySpecifier(new Object[]{challenge[0]}, Device.MaxArrayStringElements));
    }

    @Override
    public void retrieveDeviceAuthenticationData(byte[] challenge) throws JposException {
        MSRInterface.retrieveDeviceAuthenticationData(new byte[][]{challenge});
    }

    @Override
    public void updateKey(String key, String keyName) throws JposException {
        logPreCall("UpdateKey", key + ", " + keyName);
        checkEnabled();
        check(Data.CapDeviceAuthentication == MSR_DA_NOT_SUPPORTED, JPOS_E_ILLEGAL, "Authentication not supported");
        MSRInterface.updateKey(key, keyName);
        logCall("UpdateKey");
    }

    @Override
    public void writeTracks(byte[][] data, int timeout) throws JposException {
        logPreCall("WriteTracks", removeOuterArraySpecifier(new Object[]{data, timeout}, Device.MaxArrayStringElements));
        checkEnabled();
        check(Data.CapWritableTracks == MSR_TR_NONE, JPOS_E_ILLEGAL, "Write tracks not supported");
        check(Data.TracksToWrite == MSR_TR_NONE, JPOS_E_FAILURE, "Selected tracks cannot be written: " + Data.TracksToWrite);
        check(data == null || data.length != 4, JPOS_E_ILLEGAL, "Invalid track data");
        int[] trackspec = {MSR_TR_1, MSR_TR_2, MSR_TR_3, MSR_TR_4};
        for (int checkindex = 0; checkindex < trackspec.length; checkindex++) {
            check(data[checkindex] == null, JPOS_E_ILLEGAL, "Invalid track data");
            if ((trackspec[checkindex] & Data.TracksToWrite) == 0) {
                check(data[checkindex].length > 0, JPOS_E_ILLEGAL, "Trackdata specified for non-writable track " + (checkindex + 1));
            } else {
                check(data[checkindex].length > Data.EncodingMaxLength, JPOS_E_ILLEGAL, "Data too long for track " + (checkindex + 1));
            }
        }
        MSRInterface.writeTracks(data, timeout);
        logCall("WriteTracks");
    }
}
