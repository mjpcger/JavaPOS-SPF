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

package de.gmxhome.conrad.jpos.jpos_base.biometrics;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.services.*;

import java.util.Arrays;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;

/**
 * Biometrics service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class BiometricsService extends JposBase implements BiometricsService116 {
    /**
     * Instance of a class implementing the BiometricsInterface for biometrics specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public BiometricsInterface Biometrics;

    private final BiometricsProperties Data;
    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public BiometricsService(BiometricsProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public boolean getCapPrematchData() throws JposException {
        checkOpened();
        logGet("CapPrematchData");
        return Data.CapPrematchData;
    }

    @Override
    public boolean getCapRawSensorData() throws JposException {
        checkOpened();
        logGet("CapRawSensorData");
        return Data.CapRawSensorData;
    }

    @Override
    public boolean getCapRealTimeData() throws JposException {
        checkOpened();
        logGet("CapRealTimeData");
        return Data.CapRealTimeData;
    }

    @Override
    public int getCapSensorColor() throws JposException {
        checkOpened();
        logGet("CapSensorColor");
        return Data.CapSensorColor;
    }

    @Override
    public int getCapSensorOrientation() throws JposException {
        checkOpened();
        logGet("CapSensorOrientation");
        return Data.CapSensorOrientation;
    }

    @Override
    public int getCapSensorType() throws JposException {
        checkEnabled();
        check(Data.CapSensorType == null, JPOS_E_FAILURE, "CapSensorType not initialized by device");
        logGet("CapSensorType");
        return Data.CapSensorType;
    }

    @Override
    public boolean getCapTemplateAdaptation() throws JposException {
        checkOpened();
        logGet("CapTemplateAdaptation");
        return Data.CapTemplateAdaptation;
    }

    @Override
    public int getAlgorithm() throws JposException {
        checkClaimed();
        check(Data.Algorithm == null, JPOS_E_FAILURE, "Algorithm not initialized by device");
        logGet("Algorithm");
        return Data.Algorithm;
    }

    @Override
    public void setAlgorithm(int newAlgorithm) throws JposException {
        logPreSet("Algorithm");
        checkClaimed();
        check(Data.DeviceEnabled, JPOS_E_ILLEGAL, "Device enabled");
        checkRange(newAlgorithm, 0, Data.AlgorithmList.split(",").length, JPOS_E_ILLEGAL, "Algorithm out of range: " + newAlgorithm);
        check(newAlgorithm > 0 && Data.AlgorithmList.length() == 0, JPOS_E_ILLEGAL, "Only default algorithm allowed");
        Biometrics.algorithm(newAlgorithm);
        logSet("Algorithm");
    }

    @Override
    public String getAlgorithmList() throws JposException {
        checkOpened();
        logGet("AlgorithmList");
        return Data.AlgorithmList;
    }

    @Override
    public byte[] getBIR() throws JposException {
        checkEnabled();
        check(Data.BIR == null, JPOS_E_ILLEGAL, "BIR currently not available");
        logGet("BIR");
        return Arrays.copyOf(Data.BIR, Data.BIR.length);
    }

    @Override
    public byte[] getRawSensorData() throws JposException {
        checkEnabled();
        check(Data.RawSensorData == null, JPOS_E_ILLEGAL, "RawSensorData currently not available");
        logGet("RawSensorData");
        return Arrays.copyOf(Data.RawSensorData, Data.RawSensorData.length);
    }

    @Override
    public boolean getRealTimeDataEnabled() throws JposException {
        checkOpened();
        logGet("RealTimeDataEnabled");
        return Data.RealTimeDataEnabled;
    }

    @Override
    public void setRealTimeDataEnabled(boolean newRealTimeDataEnabled) throws JposException {
        logPreSet("RealTimeDataEnabled");
        checkOpened();
        check(newRealTimeDataEnabled && !Data.CapRealTimeData, JPOS_E_ILLEGAL, "RealTimeData not supported");
        checkNoChangedOrClaimed(Data.RealTimeDataEnabled, newRealTimeDataEnabled);
        Biometrics.realTimeDataEnabled(newRealTimeDataEnabled);
        logSet("RealTimeDataEnabled");
    }

    @Override
    public int getSensorBPP() throws JposException {
        checkOpened();
        logGet("SensorBPP");
        return Data.SensorBPP;
    }

    @Override
    public int getSensorColor() throws JposException {
        checkOpened();
        logGet("SensorColor");
        return Data.SensorColor;
    }

    @Override
    public void setSensorColor(int newSensorColor) throws JposException {
        logPreSet("SensorColor");
        checkOpened();
        check(Data.validateSensorColor(newSensorColor), JPOS_E_ILLEGAL, "SensorColor invalid: " + newSensorColor);
        checkNoChangedOrClaimed(Data.SensorColor, newSensorColor);
        Biometrics.sensorColor(newSensorColor);
        logSet("SensorColor");
    }

    @Override
    public int getSensorHeight() throws JposException {
        checkOpened();
        logGet("SensorHeight");
        return Data.SensorHeight;
    }

    @Override
    public int getSensorOrientation() throws JposException {
        checkClaimed();
        check(Data.SensorOrientation == null, JPOS_E_ILLEGAL, "SensorOrientation not initialized by device");
        logGet("SensorOrientation");
        return Data.SensorOrientation;
    }

    @Override
    public void setSensorOrientation(int newSensorOrientation) throws JposException {
        logPreSet("SensorOrientation");
        checkClaimed();
        check(Data.DeviceEnabled, JPOS_E_ILLEGAL, "Device enabled");
        check(!Data.validateSensorOrientation(newSensorOrientation), JPOS_E_ILLEGAL, "SensorOrientation invalid: " + newSensorOrientation);
        Biometrics.sensorOrientation(newSensorOrientation);
        logSet("SensorOrientation");
    }

    @Override
    public int getSensorType() throws JposException {
        checkEnabled();
        check(Data.SensorType == null, JPOS_E_ILLEGAL, "SensorType not initialized by device");
        logGet("SensorType");
        return Data.SensorType;
    }

    @Override
    public void setSensorType(int newSensorType) throws JposException {
        logPreSet("SensorType");
        checkEnabled();
        check(!Data.validateSensorType(newSensorType), JPOS_E_ILLEGAL, "SensorType invalid: " + newSensorType);
        Biometrics.sensorType(newSensorType);
        logSet("SensorType");
    }

    @Override
    public int getSensorWidth() throws JposException {
        checkOpened();
        logGet("SensorWidth");
        return Data.SensorWidth;
    }

    private String bytes2String(byte[] data) {
        StringBuilder ret = new StringBuilder();
        for (byte date : data)
            ret.append(", ").append((int) date);
        return ret.length() == 0 ? "{}" : "{" + ret.substring(2) + "}";
    }

    private String ints2String(int[] data) {
        StringBuilder ret = new StringBuilder();
        if (data == null)
            return "null";
        for (int date : data)
            ret.append(", ").append((long) date & (((long) 1 << Integer.SIZE) - 1));
        return ret.length() == 0 ? "{}" : "{" + ret.substring(2) + "}";
    }

    private Object[] bytes2String(byte[][] datas) {
        StringBuilder ret = new StringBuilder();
        boolean validBIR = true;
        for (byte[] data : datas) {
            if (data == null) {
                ret.append(", null");
                validBIR = false;
            }
            else {
                if (!Data.checkBIRPurpose(data, null))
                    validBIR = false;
                ret.append(", ").append(bytes2String(data));
            }
        }
        return new Object[]{ret.length() == 0 ? "{}" : "{" + ret.substring(2) + "}", validBIR};
    }

    @Override
    public void beginEnrollCapture(byte[] referenceBIR, byte[] payload) throws JposException {
        if (referenceBIR == null)
            referenceBIR = new byte[0];
        if (payload == null)
            payload = new byte[0];
        logPreCall("BeginEnrollCapture", bytes2String(referenceBIR) + ", " + bytes2String(payload));
        check(!Data.CapTemplateAdaptation && !Data.isDataEmpty(referenceBIR, true), JPOS_E_FAILURE, "Adaption of referenceBIR not supported");
        checkEnabled();
        Biometrics.beginEnrollCapture(referenceBIR, payload);
        logCall("BeginEnrollCapture");
    }

    @Override
    public void beginVerifyCapture() throws JposException {
        logPreCall("BeginVerifyCapture");
        checkEnabled();
        Biometrics.beginVerifyCapture();
        logCall("BeginVerifyCapture");
    }

    @Override
    public void endCapture() throws JposException {
        logPreCall("EndCapture");
        checkEnabled();
        Biometrics.endCapture();
        logCall("EndCapture");
    }

    @Override
    public void identify(int maxFARRequested, int maxFRRRequested, boolean fARPrecedence, byte[][] referenceBIRPopulation,
                         int[][] candidateRanking, int timeout) throws JposException {
        if (referenceBIRPopulation == null)
            referenceBIRPopulation = new byte[0][];
        Object[] refBIR = bytes2String(referenceBIRPopulation);
        logPreCall("Identify", removeOuterArraySpecifier(new Object[]{
                maxFARRequested, maxFRRRequested, fARPrecedence,refBIR[0].toString(), "...", timeout }, Device.MaxArrayStringElements));
        checkEnabled();
        Data.checkFARorFRRLimit(maxFARRequested, "maxFARRequested");
        Data.checkFARorFRRLimit(maxFRRRequested, "maxFRRRequested");
        check(referenceBIRPopulation.length == 0, JPOS_E_ILLEGAL, "No reference BIR");
        check(!(Boolean)refBIR[1], JPOS_E_ILLEGAL, "At least one invalid reference BIR");
        check(candidateRanking == null || candidateRanking.length != 1, JPOS_E_ILLEGAL,
                "Reference candidateRanking invalid, must be int[1][]");
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        Biometrics.identify(maxFARRequested, maxFRRRequested, fARPrecedence, referenceBIRPopulation, candidateRanking, timeout);
        if (candidateRanking[0] == null)
            candidateRanking[0] = new int[0];
        logCall("Identify", removeOuterArraySpecifier(new Object[]{"...", ints2String(candidateRanking[0]), "..."}, Device.MaxArrayStringElements));
    }

    @Override
    public void identifyMatch(int maxFARRequested, int maxFRRRequested, boolean fARPrecedence, byte[] sampleBIR,
                              byte[][] referenceBIRPopulation, int[][] candidateRanking) throws JposException {
        if (referenceBIRPopulation == null)
            referenceBIRPopulation = new byte[0][];
        if (sampleBIR == null)
            sampleBIR = new byte[0];
        Object[] refBIR = bytes2String(referenceBIRPopulation);
        logPreCall("IdentifyMatch", removeOuterArraySpecifier(new Object[]{
                        maxFARRequested, maxFRRRequested, fARPrecedence, bytes2String(sampleBIR), refBIR[0].toString(), "..."
                }, Device.MaxArrayStringElements));
        checkEnabled();
        Data.checkFARorFRRLimit(maxFARRequested, "maxFARRequested");
        Data.checkFARorFRRLimit(maxFRRRequested, "maxFRRRequested");
        check(Data.isDataEmpty(sampleBIR, true), JPOS_E_ILLEGAL, "Empty sampleBIR");
        check(referenceBIRPopulation.length == 0, JPOS_E_ILLEGAL, "No reference BIR");
        check(!(Boolean)refBIR[1], JPOS_E_ILLEGAL, "At least one invalid reference BIR");
        check(candidateRanking == null || candidateRanking.length != 1, JPOS_E_ILLEGAL,
                "Reference candidateRanking invalid, must be int[1][]");
        Biometrics.identifyMatch(maxFARRequested, maxFRRRequested, fARPrecedence, sampleBIR, referenceBIRPopulation, candidateRanking);
        if (candidateRanking[0] == null)
            candidateRanking[0] = new int[0];
        logCall("IdentifyMatch", removeOuterArraySpecifier(new Object[]{"...", ints2String(candidateRanking[0])}, Device.MaxArrayStringElements));
    }

    @Override
    public void processPrematchData(byte[] sampleBIR, byte[] prematchDataBIR, byte[][] processedBIR) throws JposException {
        if (prematchDataBIR == null)
            prematchDataBIR = new byte[0];
        if (sampleBIR == null)
            sampleBIR = new byte[0];
        logPreCall("ProcessPrematchData", removeOuterArraySpecifier(new Object[]{
                bytes2String(sampleBIR), bytes2String(prematchDataBIR), "..."}, Device.MaxArrayStringElements));
        checkEnabled();
        check(!Data.CapPrematchData, JPOS_E_ILLEGAL, "PrematchData not supported");
        check(Data.isDataEmpty(sampleBIR, true), JPOS_E_ILLEGAL, "Empty sampleBIR");
        check(Data.isDataEmpty(prematchDataBIR, true), JPOS_E_ILLEGAL, "Empty prematchDataBIR");
        check(processedBIR == null || processedBIR.length != 1, JPOS_E_ILLEGAL,
                "Reference processedBIR invalid, must be byte[1][]");
        Biometrics.processPrematchData(sampleBIR, prematchDataBIR, processedBIR);
        if (processedBIR[0] == null)
            processedBIR[0] = new byte[0];
        logCall("ProcessPrematchData", removeOuterArraySpecifier(new Object[]{
                "...", bytes2String(processedBIR[0])}, Device.MaxArrayStringElements));
    }

    @Override
    public void verify(int maxFARRequested, int maxFRRRequested, boolean fARPrecedence, byte[] referenceBIR,
                       byte[][] adaptedBIR, boolean[] result, int[] fARAchieved, int[] fRRAchieved, byte[][] payload,
                       int timeout) throws JposException {
        if (referenceBIR == null)
            referenceBIR = new byte[0];
        logPreCall("Verify", removeOuterArraySpecifier(new Object[]{
                maxFARRequested, maxFRRRequested, fARPrecedence, bytes2String(referenceBIR),
                adaptedBIR == null ? null : bytes2String(adaptedBIR)[0], "...", timeout
        }, Device.MaxArrayStringElements));
        checkEnabled();
        Data.checkFARorFRRLimit(maxFARRequested, "maxFARRequested");
        Data.checkFARorFRRLimit(maxFRRRequested, "maxFRRRequested");
        check(!Data.checkBIRPurpose(referenceBIR, null), JPOS_E_ILLEGAL, "Invalid referenceBIR");
        check(adaptedBIR == null || adaptedBIR.length != 1, JPOS_E_ILLEGAL,
                "Reference adaptedBIR invalid, must be byte[1][]");
        check(result == null || result.length != 1, JPOS_E_ILLEGAL,
                "Reference result invalid, must be boolean[1]");
        check(fARAchieved == null || fARAchieved.length != 1, JPOS_E_ILLEGAL,
                "Reference FARAchieved invalid, must be int[1]");
        check(fRRAchieved == null || fRRAchieved.length != 1, JPOS_E_ILLEGAL,
                "Reference FRRAchieved invalid, must be int[1]");
        check(payload == null || payload.length != 1, JPOS_E_ILLEGAL,
                "Reference payload invalid, must be byte[1][]");
        check(timeout < 0 && timeout != JPOS_FOREVER, JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        Biometrics.verify(maxFARRequested, maxFRRRequested, fARPrecedence, referenceBIR, adaptedBIR, result, fARAchieved, fRRAchieved, payload, timeout);
        logCall("Verify", removeOuterArraySpecifier(new Object[]{
                "...", bytes2String(adaptedBIR)[0], result[0], fARAchieved[0], fRRAchieved[0], bytes2String(payload)[0], "..."
        }, Device.MaxArrayStringElements));
    }

    @Override
    public void verifyMatch(int maxFARRequested, int maxFRRRequested, boolean fARPrecedence, byte[] sampleBIR,
                            byte[]referenceBIR, byte[][]adaptedBIR, boolean[]result, int[]fARAchieved, int[]fRRAchieved,
                            byte[][]payload) throws JposException {
        if (sampleBIR == null)
            sampleBIR = new byte[0];
        if (referenceBIR == null)
            referenceBIR = new byte[0];
        logPreCall("VerifyMatch", removeOuterArraySpecifier(new Object[]{
                maxFARRequested, maxFRRRequested, fARPrecedence, bytes2String(sampleBIR), bytes2String(referenceBIR),
                adaptedBIR == null ? null : bytes2String(adaptedBIR)[0], "..."
        }, Device.MaxArrayStringElements));
        checkEnabled();
        Data.checkFARorFRRLimit(maxFARRequested, "maxFARRequested");
        Data.checkFARorFRRLimit(maxFRRRequested, "maxFRRRequested");
        check(Data.isDataEmpty(sampleBIR, true), JPOS_E_ILLEGAL, "Empty sampleBIR");
        check(!Data.checkBIRPurpose(referenceBIR, null), JPOS_E_ILLEGAL, "Invalid referenceBIR");
        check(adaptedBIR == null || adaptedBIR.length != 1, JPOS_E_ILLEGAL,
                "Reference adaptedBIR invalid, must be byte[1][]");
        check(result == null || result.length != 1, JPOS_E_ILLEGAL,
                "Reference result invalid, must be boolean[1]");
        check(fARAchieved == null || fARAchieved.length != 1, JPOS_E_ILLEGAL,
                "Reference FARAchieved invalid, must be int[1]");
        check(fRRAchieved == null || fRRAchieved.length != 1, JPOS_E_ILLEGAL,
                "Reference FRRAchieved invalid, must be int[1]");
        check(payload == null || payload.length != 1, JPOS_E_ILLEGAL,
                "Reference payload invalid, must be byte[1][]");
        Biometrics.verifyMatch(maxFARRequested, maxFRRRequested, fARPrecedence, sampleBIR, referenceBIR, adaptedBIR, result, fARAchieved, fRRAchieved, payload);
        logCall("VerifyMatch", removeOuterArraySpecifier(new Object[]{
                "...", bytes2String(adaptedBIR)[0], result[0], fARAchieved[0], fRRAchieved[0], bytes2String(payload)[0]
        }, Device.MaxArrayStringElements));
    }
}
