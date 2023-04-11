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

/**
 * Biometrics service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class BiometricsService extends JposBase implements BiometricsService115 {
    /**
     * Instance of a class implementing the BiometricsInterface for biometrics specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public BiometricsInterface Biometrics;

    private BiometricsProperties Data;
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
        Device.check(Data.CapSensorType == null, JposConst.JPOS_E_FAILURE, "CapSensorType not initialized by device");
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
        Device.check(Data.Algorithm == null, JposConst.JPOS_E_FAILURE, "Algorithm not initialized by device");
        logGet("Algorithm");
        return Data.Algorithm;
    }

    @Override
    public void setAlgorithm(int newAlgorithm) throws JposException {
        logPreSet("Algorithm");
        checkClaimed();
        Device.check(Data.DeviceEnabled, JposConst.JPOS_E_ILLEGAL, "Device enabled");
        Device.checkRange(newAlgorithm, 0, Data.AlgorithmList.split(",").length, JposConst.JPOS_E_ILLEGAL, "Algorithm out of range: " + newAlgorithm);
        Device.check(newAlgorithm > 0 && Data.AlgorithmList.length() == 0, JposConst.JPOS_E_ILLEGAL, "Only default algorithm allowed");
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
        Device.check(Data.BIR == null, JposConst.JPOS_E_ILLEGAL, "BIR currently not available");
        logGet("BIR");
        return Arrays.copyOf(Data.BIR, Data.BIR.length);
    }

    @Override
    public byte[] getRawSensorData() throws JposException {
        checkEnabled();
        Device.check(Data.RawSensorData == null, JposConst.JPOS_E_ILLEGAL, "RawSensorData currently not available");
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
        Device.check(newRealTimeDataEnabled && !Data.CapRealTimeData, JposConst.JPOS_E_ILLEGAL, "RealTimeData not supported");
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
        Device.check(!Data.validateSensorColor(newSensorColor), JposConst.JPOS_E_ILLEGAL, "SensorColor invalid: " + newSensorColor);
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
        Device.check(Data.SensorOrientation == null, JposConst.JPOS_E_ILLEGAL, "SensorOrientation not initialized by device");
        logGet("SensorOrientation");
        return Data.SensorOrientation;
    }

    @Override
    public void setSensorOrientation(int newSensorOrientation) throws JposException {
        logPreSet("SensorOrientation");
        checkClaimed();
        Device.check(Data.DeviceEnabled, JposConst.JPOS_E_ILLEGAL, "Device enabled");
        Device.check(!Data.validateSensorOrientation(newSensorOrientation), JposConst.JPOS_E_ILLEGAL, "SensorOrientation invalid: " + newSensorOrientation);
        Biometrics.sensorOrientation(newSensorOrientation);
        logSet("SensorOrientation");
    }

    @Override
    public int getSensorType() throws JposException {
        checkEnabled();
        Device.check(Data.SensorType == null, JposConst.JPOS_E_ILLEGAL, "SensorType not initialized by device");
        logGet("SensorType");
        return Data.SensorType;
    }

    @Override
    public void setSensorType(int newSensorType) throws JposException {
        logPreSet("SensorType");
        checkEnabled();
        Device.check(!Data.validateSensorType(newSensorType), JposConst.JPOS_E_ILLEGAL, "SensorType invalid: " + newSensorType);
        Biometrics.sensorType(newSensorType);
        logSet("SensorType");
    }

    @Override
    public int getSensorWidth() throws JposException {
        checkOpened();
        logGet("SensorWidth");
        return Data.SensorWidth;
    }

    String bytes2String(byte[] data) {
        String ret = "";
        for (byte date : data)
            ret += ", " + (int) date;
        return ret.length() == 0 ? "{}" : "{" + ret.substring(2) + "}";
    }

    String ints2String(int[] data) {
        String ret = "";
        if (data == null)
            return "null";
        for (int date : data)
            ret += ", " + ((long)date & (((long)1 << Integer.SIZE) - 1));
        return ret.length() == 0 ? "{}" : "{" + ret.substring(2) + "}";
    }

    Object[] bytes2String(byte[][] datas) {
        String ret = "";
        Boolean validBIR = true;
        for (byte[] data : datas) {
            if (data == null) {
                ret += ", null";
                validBIR = false;
            }
            else {
                if (!Data.checkBIRPurpose(data, null))
                    validBIR = false;
                ret += ", " + bytes2String(data);
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
        Device.check(!Data.CapTemplateAdaptation && !Data.isDataEmpty(referenceBIR, true), JposConst.JPOS_E_FAILURE, "Adaption of referenceBIR not supported");
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
        logPreCall("Identify", "" + maxFARRequested + ", " + maxFRRRequested + ", " + fARPrecedence + ", " +
                refBIR[0].toString() + ", ..., " + timeout);
        checkEnabled();
        Data.checkFARorFRRLimit(maxFARRequested, "maxFARRequested");
        Data.checkFARorFRRLimit(maxFRRRequested, "maxFRRRequested");
        Device.check(referenceBIRPopulation.length == 0, JposConst.JPOS_E_ILLEGAL, "No reference BIR");
        Device.check(!(Boolean)refBIR[1], JposConst.JPOS_E_ILLEGAL, "At least one invalid reference BIR");
        Device.check(candidateRanking == null || candidateRanking.length != 1, JposConst.JPOS_E_ILLEGAL,
                "Reference candidateRanking invalid, must be int[1][]");
        Device.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        Biometrics.identify(maxFARRequested, maxFRRRequested, fARPrecedence, referenceBIRPopulation, candidateRanking, timeout);
        if (candidateRanking[0] == null)
            candidateRanking[0] = new int[0];
        logCall("Identify", "...," + ints2String(candidateRanking[0]) + ", ...");
    }

    @Override
    public void identifyMatch(int maxFARRequested, int maxFRRRequested, boolean fARPrecedence, byte[] sampleBIR,
                              byte[][] referenceBIRPopulation, int[][] candidateRanking) throws JposException {
        if (referenceBIRPopulation == null)
            referenceBIRPopulation = new byte[0][];
        if (sampleBIR == null)
            sampleBIR = new byte[0];
        Object[] refBIR = bytes2String(referenceBIRPopulation);
        logPreCall("IdentifyMatch", "" + maxFARRequested + ", " + maxFRRRequested + ", " + fARPrecedence + ", " +
                bytes2String(sampleBIR) + ", " + refBIR[0].toString() + ", ...");
        checkEnabled();
        Data.checkFARorFRRLimit(maxFARRequested, "maxFARRequested");
        Data.checkFARorFRRLimit(maxFRRRequested, "maxFRRRequested");
        Device.check(Data.isDataEmpty(sampleBIR, true), JposConst.JPOS_E_ILLEGAL, "Empty sampleBIR");
        Device.check(referenceBIRPopulation.length == 0, JposConst.JPOS_E_ILLEGAL, "No reference BIR");
        Device.check(!(Boolean)refBIR[1], JposConst.JPOS_E_ILLEGAL, "At least one invalid reference BIR");
        Device.check(candidateRanking == null || candidateRanking.length != 1, JposConst.JPOS_E_ILLEGAL,
                "Reference candidateRanking invalid, must be int[1][]");
        Biometrics.identifyMatch(maxFARRequested, maxFRRRequested, fARPrecedence, sampleBIR, referenceBIRPopulation, candidateRanking);
        if (candidateRanking[0] == null)
            candidateRanking[0] = new int[0];
        logCall("IdentifyMatch", "..., " + ints2String(candidateRanking[0]));
    }

    @Override
    public void processPrematchData(byte[] sampleBIR, byte[] prematchDataBIR, byte[][] processedBIR) throws JposException {
        if (prematchDataBIR == null)
            prematchDataBIR = new byte[0];
        if (sampleBIR == null)
            sampleBIR = new byte[0];
        logPreCall("ProcessPrematchData", bytes2String(sampleBIR) + ", " + bytes2String(prematchDataBIR) + ", ...");
        checkEnabled();
        Device.check(!Data.CapPrematchData, JposConst.JPOS_E_ILLEGAL, "PrematchData not supported");
        Device.check(Data.isDataEmpty(sampleBIR, true), JposConst.JPOS_E_ILLEGAL, "Empty sampleBIR");
        Device.check(Data.isDataEmpty(prematchDataBIR, true), JposConst.JPOS_E_ILLEGAL, "Empty prematchDataBIR");
        Device.check(processedBIR == null || processedBIR.length != 1, JposConst.JPOS_E_ILLEGAL,
                "Reference processedBIR invalid, must be byte[1][]");
        Biometrics.processPrematchData(sampleBIR, prematchDataBIR, processedBIR);
        if (processedBIR[0] == null)
            processedBIR[0] = new byte[0];
        logCall("ProcessPrematchData", "..., " + bytes2String(processedBIR[0]));
    }

    @Override
    public void verify(int maxFARRequested, int maxFRRRequested, boolean fARPrecedence, byte[] referenceBIR,
                       byte[][] adaptedBIR, boolean[] result, int[] fARAchieved, int[] fRRAchieved, byte[][] payload,
                       int timeout) throws JposException {
        if (referenceBIR == null)
            referenceBIR = new byte[0];
        logPreCall("Verify", "" + maxFARRequested + ", " + maxFRRRequested + ", " + fARPrecedence + ", "
                + bytes2String(referenceBIR) + ", " + bytes2String(adaptedBIR) + ", ..., " + timeout);
        checkEnabled();
        Data.checkFARorFRRLimit(maxFARRequested, "maxFARRequested");
        Data.checkFARorFRRLimit(maxFRRRequested, "maxFRRRequested");
        Device.check(!Data.checkBIRPurpose(referenceBIR, null), JposConst.JPOS_E_ILLEGAL, "Invalid referenceBIR");
        Device.check(adaptedBIR == null || adaptedBIR.length != 1, JposConst.JPOS_E_ILLEGAL,
                "Reference adaptedBIR invalid, must be byte[1][]");
        Device.check(result == null || result.length != 1, JposConst.JPOS_E_ILLEGAL,
                "Reference result invalid, must be boolean[1]");
        Device.check(fARAchieved == null || fARAchieved.length != 1, JposConst.JPOS_E_ILLEGAL,
                "Reference FARAchieved invalid, must be int[1]");
        Device.check(fRRAchieved == null || fRRAchieved.length != 1, JposConst.JPOS_E_ILLEGAL,
                "Reference FRRAchieved invalid, must be int[1]");
        Device.check(payload == null || payload.length != 1, JposConst.JPOS_E_ILLEGAL,
                "Reference payload invalid, must be byte[1][]");
        Device.check(timeout < 0 && timeout != JposConst.JPOS_FOREVER, JposConst.JPOS_E_ILLEGAL, "Invalid timeout: " + timeout);
        Biometrics.verify(maxFARRequested, maxFRRRequested, fARPrecedence, referenceBIR, adaptedBIR, result, fARAchieved, fRRAchieved, payload, timeout);
        logCall("Verify", "..., " + bytes2String(adaptedBIR) + ", " + result[0] + ", " + fARAchieved[0] + ", " +
                fRRAchieved[0] + ", " + bytes2String(payload) + ",...");
    }

    @Override
    public void verifyMatch(int maxFARRequested, int maxFRRRequested, boolean fARPrecedence, byte[] sampleBIR,
                            byte[]referenceBIR, byte[][]adaptedBIR, boolean[]result, int[]fARAchieved, int[]fRRAchieved,
                            byte[][]payload) throws JposException {
        if (sampleBIR == null)
            sampleBIR = new byte[0];
        logPreCall("VerifyMatch", "" + maxFARRequested + ", " + maxFRRRequested + ", " + fARPrecedence + ", " +
                bytes2String(sampleBIR) + ", " + bytes2String(referenceBIR) + ", " + bytes2String(adaptedBIR) + ", ...");
        checkEnabled();
        Data.checkFARorFRRLimit(maxFARRequested, "maxFARRequested");
        Data.checkFARorFRRLimit(maxFRRRequested, "maxFRRRequested");
        Device.check(Data.isDataEmpty(sampleBIR, true), JposConst.JPOS_E_ILLEGAL, "Empty sampleBIR");
        Device.check(!Data.checkBIRPurpose(referenceBIR, null), JposConst.JPOS_E_ILLEGAL, "Invalid referenceBIR");
        Device.check(adaptedBIR == null || adaptedBIR.length != 1, JposConst.JPOS_E_ILLEGAL,
                "Reference adaptedBIR invalid, must be byte[1][]");
        Device.check(result == null || result.length != 1, JposConst.JPOS_E_ILLEGAL,
                "Reference result invalid, must be boolean[1]");
        Device.check(fARAchieved == null || fARAchieved.length != 1, JposConst.JPOS_E_ILLEGAL,
                "Reference FARAchieved invalid, must be int[1]");
        Device.check(fRRAchieved == null || fRRAchieved.length != 1, JposConst.JPOS_E_ILLEGAL,
                "Reference FRRAchieved invalid, must be int[1]");
        Device.check(payload == null || payload.length != 1, JposConst.JPOS_E_ILLEGAL,
                "Reference payload invalid, must be byte[1][]");
        Biometrics.verifyMatch(maxFARRequested, maxFRRRequested, fARPrecedence, sampleBIR, referenceBIR, adaptedBIR, result, fARAchieved, fRRAchieved, payload);
        logCall("VerifyMatch", "..., " + bytes2String(adaptedBIR) + ", " + result[0] + ", " + fARAchieved[0] + ", " +
                fRRAchieved[0] + ", " + bytes2String(payload));
    }
}
