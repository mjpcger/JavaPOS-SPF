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

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import jpos.JposException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import static de.gmxhome.conrad.jpos.jpos_base.JposBaseDevice.*;
import static jpos.BiometricsConst.*;
import static jpos.JposConst.*;

/**
 * Class containing the biometrics specific properties, their default values and default implementations of
 * BiometricsInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Biometrics.
 */
public class BiometricsProperties extends JposCommonProperties implements BiometricsInterface {
    /**
     * UPOS property CapPrematchData. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapPrematchData = false;

    /**
     * UPOS property CapRawSensorData. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRawSensorData = false;

    /**
     * UPOS property CapRealTimeData. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapRealTimeData = false;

    /**
     * UPOS property CapSensorColor. Default: CSC_MONO. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapSensorColor = BIO_CSC_MONO;

    /**
     * UPOS property CapSensorOrientation. Default: CSO_NORMAL. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int CapSensorOrientation = BIO_CSO_NORMAL;

    /**
     * Default value of CapSensorType property. Default: CST_PASSWORD. Should be updated
     * before calling initOnEnable the first time.
     */
    public int CapSensorTypeDef = BIO_CST_PASSWORD;

    /**
     * UPOS property CapSensorType. Default: null. Must be overwritten
     * by objects derived from JposDevice at least when the device is enabled the first time.
     */
    public Integer CapSensorType = null;

    /**
     * UPOS property CapTemplateAdaptation. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapTemplateAdaptation = false;

    /**
     * Default value of Algorithm property. Default: 0. Should be updated
     * before calling initOnClaim the first time.
     */
    public int AlgorithmDef = 0;

    /**
     * UPOS property Algorithm. Default: null. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public Integer Algorithm = null;

    /**
     * UPOS property AlgorithmList. Default: "". Should be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String AlgorithmList = "";

    /**
     * UPOS property BIR. Default: null.
     */
    public byte[] BIR = null;

    /**
     * UPOS property RawSensorData. Default: null.
     */
    public byte[] RawSensorData = null;

    /**
     * UPOS property RealTimeDataEnabled. Set to false in initOnOpen method..
     */
    public boolean RealTimeDataEnabled;

    /**
     * UPOS property SensorBPP. Default: null. Must be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public Integer SensorBPP = null;

    /**
     * UPOS property SensorColor. Default: SC_MONO. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method, must be overwritten if CapSensorColor
     * does not contain CSC_MONO.
     */
    public int SensorColor = BIO_SC_MONO;

    /**
     * UPOS property SensorHeight. Default: null. Must be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public Integer SensorHeight = null;

    /**
     * Default value of SensorOrientation property. Default: SC_MONO. Should be updated
     * before calling initOnClaim the first time.  Must be overwritten if CapSensorOrientation
     * does not contain CSO_NORMAL.
     */
    public int SensorOrientationDef = BIO_SO_NORMAL;

    /**
     * UPOS property SensorOrientation.
     */
    public Integer SensorOrientation = null;

    /**
     * Default value of SensorType property. Default: null. Must be overwritten before any call to method initOnClaim.
     */
    public Integer SensorTypeDef = null;

    /**
     * UPOS property SensorType. Default: null. Must be overwritten
     * by objects derived from JposDevice at least when the device is enabled the first time.
     */
    public Integer SensorType = null;

    /**
     * UPOS property SensorWidth. Default: null. Must be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public Integer SensorWidth = null;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected BiometricsProperties(int dev) {
        super(dev);
        ExclusiveUse = ExclusiveYes;
    }

    @Override
    public void initOnOpen() {
        super.initOnOpen();
        RealTimeDataEnabled = false;
    }

    @Override
    public void initOnClaim() {
        super.initOnClaim();
        if (Algorithm == null && AlgorithmList.split(",").length >= AlgorithmDef && AlgorithmDef >= 0)
            Algorithm = AlgorithmDef;
        if (SensorOrientation == null && validateSensorOrientation(SensorOrientationDef))
            SensorOrientation = SensorOrientationDef;
    }

    /**
     * Validation of SensorOrientation property. Checks whether a given value is allowed due to the value of
     * capability CapSensorOrientation.
     * @param value SensorOrientation value to be checked
     * @return      true if value is valid, false otherwise.
     */
    boolean validateSensorOrientation(int value) {
        int[] validValues = { BIO_SO_NORMAL, BIO_SO_RIGHT, BIO_SO_INVERTED, BIO_SO_LEFT };
        int[] correspondingCaptureBits = { BIO_CSO_NORMAL, BIO_CSO_RIGHT, BIO_CSO_INVERTED, BIO_CSO_LEFT };
        for (int i = 0; i < validValues.length; i++) {
            if (value == validValues[i]) {
                return (CapSensorOrientation & correspondingCaptureBits[i]) != 0;
            }
        }
        return false;
    }

    @Override
    public boolean initOnFirstEnable() {
        if (!super.initOnFirstEnable()) {
            CapSensorType = CapSensorTypeDef;
            if (SensorTypeDef != null)
                SensorType = SensorTypeDef;
            return true;
        }
        return false;
    }

    /**
     * Validation of SensorType property. Checks whether a given value is allowed due to the value of
     * capability CapSensorType.
     * @param value SensorType value to be checked
     * @return      true if value is valid, false otherwise.
     */
    boolean validateSensorType(int value) {
        int[] validValues = {
                BIO_ST_FACIAL_FEATURES, BIO_ST_VOICE, BIO_ST_FINGERPRINT, BIO_ST_IRIS, BIO_ST_RETINA,
                BIO_ST_HAND_GEOMETRY, BIO_ST_SIGNATURE_DYNAMICS, BIO_ST_KEYSTROKE_DYNAMICS, BIO_ST_LIP_MOVEMENT,
                BIO_ST_THERMAL_FACE_IMAGE, BIO_ST_THERMAL_HAND_IMAGE, BIO_ST_GAIT, BIO_ST_PASSWORD
        };
        int[] correspondingCaptureBits = {
                BIO_CST_FACIAL_FEATURES, BIO_CST_VOICE, BIO_CST_FINGERPRINT, BIO_CST_IRIS, BIO_CST_RETINA,
                BIO_CST_HAND_GEOMETRY, BIO_CST_SIGNATURE_DYNAMICS, BIO_CST_KEYSTROKE_DYNAMICS, BIO_CST_LIP_MOVEMENT,
                BIO_CST_THERMAL_FACE_IMAGE, BIO_CST_THERMAL_HAND_IMAGE, BIO_CST_GAIT, BIO_CST_PASSWORD
        };
        for (int i = 0; i < validValues.length; i++) {
            if (value == validValues[i]) {
                return (CapSensorType & correspondingCaptureBits[i]) != 0;
            }
        }
        return false;
    }

    /**
     * Validation of SensorColor property. Checks whether a given value is allowed due to the value of
     * capability CapSensorColor.
     * @param value SensorColor value to be checked
     * @return      false if value is valid, true otherwise.
     */
    boolean validateSensorColor(int value) {
        int[] validValues = { BIO_SC_MONO, BIO_SC_GRAYSCALE, BIO_SC_16, BIO_SC_256, BIO_SC_FULL };
        int[] correspondingCaptureBits = { BIO_CSC_MONO, BIO_CSC_GRAYSCALE, BIO_CSC_16, BIO_CSC_256, BIO_CSC_FULL };
        for (int i = 0; i < validValues.length; i++) {
            if (value == validValues[i]) {
                return (CapSensorColor & correspondingCaptureBits[i]) == 0;
            }
        }
        return true;
    }

    @Override
    public void algorithm(int newAlgorithm) throws JposException {
        Algorithm = newAlgorithm;
    }

    @Override
    public void realTimeDataEnabled(boolean newRealTimeDataEnabled) throws JposException {
        RealTimeDataEnabled = newRealTimeDataEnabled;
    }

    @Override
    public void sensorColor(int newSensorColor) throws JposException {
        SensorColor = newSensorColor;
    }

    @Override
    public void sensorOrientation(int newSensorOrientation) throws JposException {
        SensorOrientation = newSensorOrientation;
    }

    @Override
    public void sensorType(int newSensorType) throws JposException {
        SensorType = newSensorType;
    }

    @Override
    public void beginEnrollCapture(byte[] referenceBIR, byte[] payload) throws JposException {

    }

    @Override
    public void beginVerifyCapture() throws JposException {

    }

    @Override
    public void endCapture() throws JposException {

    }

    @Override
    public void identify(int maxFARRequested, int maxFRRRequested, boolean fARPrecedence, byte[][] referenceBIRPopulation, int[][] candidateRanking, int timeout) throws JposException {

    }

    @Override
    public void identifyMatch(int maxFARRequested, int maxFRRRequested, boolean fARPrecedence, byte[] sampleBIR, byte[][] referenceBIRPopulation, int[][] candidateRanking) throws JposException {

    }

    @Override
    public void processPrematchData(byte[] sampleBIR, byte[] prematchDataBIR, byte[][] processedBIR) throws JposException {

    }

    @Override
    public void verify(int maxFARRequested, int maxFRRRequested, boolean fARPrecedence, byte[] referenceBIR, byte[][] adaptedBIR, boolean[] result, int[] fARAchieved, int[] fRRAchieved, byte[][] payload, int timeout) throws JposException {

    }

    @Override
    public void verifyMatch(int maxFARRequested, int maxFRRRequested, boolean fARPrecedence, byte[] sampleBIR, byte[] referenceBIR, byte[][] adaptedBIR, boolean[] result, int[] fARAchieved, int[] fRRAchieved, byte[][] payload) throws JposException {

    }

    @Override
    public void checkFARorFRRLimit(int limit, String name) throws JposException {
        checkRange(limit, 0, 100, JPOS_E_ILLEGAL, name + " out of range: " + limit);
    }

    @Override
    public boolean checkBIRPurpose(byte[] bir, Boolean verify) {
        if (bir == null)
            return false;
        return switch (new BiometricInformationRecord(bir).getPurpose()) {
            case BiometricInformationRecord.PURPOSE_ENROLL -> true;
            case BiometricInformationRecord.PURPOSE_ENROLL_FOR_VERIFICATION -> verify != null && verify;
            case BiometricInformationRecord.PURPOSE_ENROLL_FOR_IDENTIFICATION -> verify != null && !verify;
            default -> false;
        };
    }

    @Override
    public boolean isDataEmpty(byte[] data, boolean isBIR) {
        if (data == null || Arrays.equals(data, Arrays.copyOf(new byte[0], data.length)))
            return true;
        return isBIR && (data.length < BiometricInformationRecord.BiometricDataBlockOffset ||
                new BiometricInformationRecord(data).getLength() == BiometricInformationRecord.BiometricDataBlockOffset);
    }

    /**
     * Helper class for Biometric Information Record (BIR) interpretation. Based on the BioAPI 1.1 reference
     * implementation on Github.
     */
    public static class BiometricInformationRecord {
        private byte[] TheBIR;
        private ByteBuffer Buffer;

        /**
         * Constructor. Creates a BIR from byte array.
         * @param bir Byte array representing the BIR.
         */
        public BiometricInformationRecord(byte[] bir) {
            TheBIR = Arrays.copyOf(bir, Math.max(bir.length, BiometricDataBlockOffset));
            wrapBuffer();
        }

        /**
         * Default constructor. Creates empty BIR.
         */
        public BiometricInformationRecord() {
            TheBIR = Arrays.copyOf(new byte[4], BiometricDataBlockOffset);
            wrapBuffer();
            try {
                setLength(BiometricDataBlockOffset);
            } catch (JposException ignored) {}
        }

        private void wrapBuffer() {
            Buffer = ByteBuffer.wrap(TheBIR);
            Buffer.order(ByteOrder.LITTLE_ENDIAN);
        }

        /**
         * Extracts byte array containing the representation of the BIR as byte array, as required by UPOS Biometrics.
         * @return Byte array interpretation of this object.
         */
        public byte[] getBytes() {
            return Arrays.copyOf(TheBIR, TheBIR.length);
        }

        /**
         * Offset of the Biometric Data Block (BDB), equals to the BIR header length.
         */
        public static final int BiometricDataBlockOffset = 16;

        /**
         * Offset of the Length field of the BIR header. Length will be stored in little endian format.
         */
        private static final int LengthOffset = 0;

        /**
         * Offset of the Header Version field of the BIR header.
         */
        private static final int HeaderVersionOffset = 4;

        /**
         * Offset of the BIR Data Type field of the BIR header.
         */
        private static final int BIRDataTypeOffset = 5;

        /**
         * Offset of the Owner field of the Format ID component of the BIR header. Owner will be stored in little endian
         * format.
         */
        private static final int FormatIDOwnerOffset = 6;

        /**
         * Offset of the Type field of the Format ID component of the BIR header. Type will be stored in little endian
         * format.
         */
        private static final int FormatIDTypeOffset = 8;

        /**
         * Offset of the Quality field of the BIR header.
         */
        private static final int QualityOffset = 10;

        /**
         * Offset of the Purpose field of the BIR header.
         */
        private static final int PurposeOffset = 11;

        /**
         * Offset of the Biometric Type field of the BIR header. Biometric Type will be stored in little endian format.
         */
        private static final int BiometricTypeOffset = 12;

        /**
         * Length getter.
         * @return  Length of BIR (without optional signature).
         */
        public int getLength() {
            return Buffer.getInt(LengthOffset);
        }

        /**
         * Length setter. Must be at least the header size.
         * @param length New value for Length field.
         * @throws JposException if length is less than the BIR header length (16).
         */
        public void setLength(int length) throws JposException {
            if (length < BiometricDataBlockOffset)
                throw new JposException(JPOS_E_FAILURE, "Insufficient BIR length");
            Buffer.putInt(LengthOffset, length);
        }

        /**
         * Header Version getter.
         * @return Header version.
         */
        public int getHeaderVersion() {
            return (int)TheBIR[HeaderVersionOffset] & 0xff;
        }

        /**
         * Header Version setter.
         * @param version        New version value. Must be between zero and 0xff (255).
         * @throws JposException If version is out of range.
         */
        public void setHeaderVersion(int version) throws JposException {
            if (version < Byte.MIN_VALUE)
                throw new JposException(JPOS_E_FAILURE, "Header Version out of range: " + version);
            TheBIR[HeaderVersionOffset] = (byte)(version & 0xff);
        }

        /**
         * Constant for BIR data type conforming to BioAPI type RAW.
         */
        public final static int DATA_TYPE_RAW = 1;

        /**
         * Constant for BIR data type conforming to BioAPI type INTERMEDIATE.
         */
        public final static int DATA_TYPE_INTERMEDIATE = 2;

        /**
         * Constant for BIR data type conforming to BioAPI type PROCESSED.
         */
        public final static int DATA_TYPE_PROCESSED = 4;

        /**
         * Constant for BIR data type conforming to BioAPI type ENCRYPTED.
         */
        public final static int DATA_TYPE_ENCRYPTED = 16;

        /**
         * Constant for BIR data type conforming to BioAPI type SIGNED.
         */
        public final static int DATA_TYPE_SIGNED = 32;
        /**
         * BIR Data Type getter.
         * @return BIR data type.
         */
        public int getBIRDataType() {
            return (int)TheBIR[BIRDataTypeOffset] & 0xff;
        }

        /**
         * BIR Data Type setter.
         * @param birDataType    New birDataType value. Must be between zero and 0xff (255).
         * @throws JposException If birDataType is out of range.
         */
        public void setBIRDataType(int birDataType) throws JposException {
            if (birDataType < Byte.MIN_VALUE)
                throw new JposException(JPOS_E_FAILURE, "BIR Data Type out of range: " + birDataType);
            TheBIR[BIRDataTypeOffset] = (byte)(birDataType & 0xff);
        }

        /**
         * Format ID component Owner getter.
         * @return Format ID component Owner.
         */
        public int getFormatIDOwner() {
            return Buffer.getShort(FormatIDOwnerOffset);
        }

        /**
         * Format ID component Owner setter. Must be between zero and 0xffff.
         * @param owner New value for Length field.
         * @throws     JposException if owner is out of range.
         */
        public void setFormatIDOwner(int owner) throws JposException {
            if (owner < Short.MIN_VALUE)
                throw new JposException(JPOS_E_FAILURE, "Format ID component Owner out of range: " + owner);
            Buffer.putShort(FormatIDOwnerOffset, (short) owner);
        }

        /**
         * Format ID component Type getter.
         * @return Format ID component Type.
         */
        public int getFormatIDType() {
            return Buffer.getShort(FormatIDTypeOffset);
        }

        /**
         * Format ID component Type setter. Must be between zero and 0xffff.
         * @param type New value for Length field.
         * @throws     JposException if type is out of range.
         */
        public void setFormatIDType(int type) throws JposException {
            if (type < Short.MIN_VALUE)
                throw new JposException(JPOS_E_FAILURE, "Format ID component Type out of range: " + type);
            Buffer.putShort(FormatIDTypeOffset, (short)type);
        }

        /**
         * Constant for BIR quality conforming to BioAPI type Quality.
         */
        public final static int QUALITY_NOT_SET = 0xff;

        /**
         * Constant for BIR quality conforming to BioAPI type Quality.
         */
        public final static int QUALITY_NOT_SUPPORTED = 0xfe;

        /**
         * Quality getter.
         * @return Quality.
         */
        public int getQuality() {
            return (int)TheBIR[QualityOffset] & 0xff;
        }

        /**
         * Quality setter.
         * @param quality    New quality value. Must be between -100 (lowest quality) and 100 (highest quality) or
         *                   one of QUALITY_NOT_SET or QUALITY_NOT_SUPPORTED.
         * @throws JposException If quality is out of range.
         */
        public void setQuality(int quality) throws JposException {
            if (quality < Byte.MIN_VALUE || quality > (1 << Byte.SIZE))
                throw new JposException(JPOS_E_FAILURE, "Quality out of range: " + quality);
            TheBIR[QualityOffset] = (byte)(quality & 0xff);
        }

        /**
         * Constant for BIR data type conforming to BioAPI purpose VERIFY.
         */
        public final static int PURPOSE_VERIFY = 1;

        /**
         * Constant for BIR data type conforming to BioAPI purpose IDENTIFY.
         */
        public final static int PURPOSE_IDENTIFY = 2;

        /**
         * Constant for BIR data type conforming to BioAPI purpose ENROLL.
         */
        public final static int PURPOSE_ENROLL = 3;

        /**
         * Constant for BIR data type conforming to BioAPI purpose ENROLL_FOR_VERIFICATION_ONLY.
         */
        public final static int PURPOSE_ENROLL_FOR_VERIFICATION = 4;

        /**
         * Constant for BIR data type conforming to BioAPI purpose ENROLL_FOR_IDENTIFICATION_ONLY.
         */
        public final static int PURPOSE_ENROLL_FOR_IDENTIFICATION = 5;

        /**
         * Constant for BIR data type conforming to BioAPI purpose AUDIT.
         */
        public final static int PURPOSE_AUDIT = 6;

        /**
         * Purpose getter.
         * @return Purpose.
         */
        public int getPurpose() {
            return (int)TheBIR[PurposeOffset] & 0xff;
        }

        /**
         * Purpose setter.
         * @param purpose    New purpose value. Must be between zero and 0xff (255).
         * @throws JposException If purpose is out of range.
         */
        public void setPurpose(int purpose) throws JposException {
            if (purpose < Byte.MIN_VALUE)
                throw new JposException(JPOS_E_FAILURE, "Purpose out of range: " + purpose);
            TheBIR[PurposeOffset] = (byte)(purpose & 0xff);
        }

        /**
         * Constant for biometric type conforming to BioAPI auth factor MULTIPLE.
         */
        public final static int BIOMETRIC_TYPE_MULTIPLE = 1;

        /**
         * Constant for biometric type conforming to BioAPI auth factor FACIAL_FEATURES.
         */
        public final static int BIOMETRIC_TYPE_FACIAL_FEATURES = 2;

        /**
         * Constant for biometric type conforming to BioAPI auth factor VOICE.
         */
        public final static int BIOMETRIC_TYPE_VOICE = 4;

        /**
         * Constant for biometric type conforming to BioAPI auth factor FINGERPRINT.
         */
        public final static int BIOMETRIC_TYPE_FINGERPRINT = 8;

        /**
         * Constant for biometric type conforming to BioAPI auth factor IRIS.
         */
        public final static int BIOMETRIC_TYPE_IRIS = 16;

        /**
         * Constant for biometric type conforming to BioAPI auth factor RETINA.
         */
        public final static int BIOMETRIC_TYPE_RETINA = 32;

        /**
         * Constant for biometric type conforming to BioAPI auth factor HAND_GEOMETRY.
         */
        public final static int BIOMETRIC_TYPE_HAND_GEOMETRY = 64;

        /**
         * Constant for biometric type conforming to BioAPI auth factor SIGNATURE_DYNAMICS.
         */
        public final static int BIOMETRIC_TYPE_SIGNATURE_DYNAMICS = 128;

        /**
         * Constant for biometric type conforming to BioAPI auth factor KEYSTOKE_DYNAMICS.
         */
        public final static int BIOMETRIC_TYPE_KEYSTOKE_DYNAMICS = 256;

        /**
         * Constant for biometric type conforming to BioAPI auth factor LIP_MOVEMENT.
         */
        public final static int BIOMETRIC_TYPE_LIP_MOVEMENT = 512;

        /**
         * Constant for biometric type conforming to BioAPI auth factor THERMAL_FACE_IMAGE.
         */
        public final static int BIOMETRIC_TYPE_THERMAL_FACE_IMAGE = 1024;

        /**
         * Constant for biometric type conforming to BioAPI auth factor THERMAL_HAND_IMAGE.
         */
        public final static int BIOMETRIC_TYPE_THERMAL_HAND_IMAGE = 2048;

        /**
         * Constant for biometric type conforming to BioAPI auth factor GAIT.
         */
        public final static int BIOMETRIC_TYPE_GAIT = 4096;

        /**
         * Constant for biometric type conforming to BioAPI auth factor PASSWORD.
         */
        public final static int BIOMETRIC_TYPE_PASSWORD = 8192;

        /**
         * Biometric Type getter.
         * @return Biometric type.
         */
        public int getBiometricType() {
            return Buffer.getInt(BiometricTypeOffset);
        }

        /**
         * Biometric Type setter.
         * @param biometricType New value for Length field.
         */
        public void setBiometricType(int biometricType) {
            Buffer.putInt(BiometricTypeOffset, biometricType);
        }

        /**
         * Retrieves the biometric data block.
         * @return The biometric data block, if the BIR contains a biometric data block, null if the BIR is empty
         *         or if the Length field of the BIR is invalid (less than header size, greater than BIR size).
         */
        public byte[] getBiometricDataBlock() {
            int size =  getLength() - BiometricDataBlockOffset;
            if (getLength() <= 0 || TheBIR.length < size + BiometricDataBlockOffset)
                return null;
            return Arrays.copyOfRange(TheBIR, BiometricDataBlockOffset, BiometricDataBlockOffset + size);
        }

        /**
         * Sets the biometric data block
         * @param bdb Biometric data block
         */
        public void setBiometricDataBlock(byte[] bdb) {
            TheBIR = Arrays.copyOf(TheBIR, BiometricDataBlockOffset + bdb.length);
            System.arraycopy(bdb, 0, TheBIR, BiometricDataBlockOffset, bdb.length);
            wrapBuffer();
            try {
                setLength(TheBIR.length);
            } catch (JposException ignored) {}
        }

        /**
         * Add signature to biometric properties.
         * @param signature Any device specific signature data.
         */
        public void addSignature(byte[] signature) {
            TheBIR = Arrays.copyOf(TheBIR, getLength() + signature.length);
            System.arraycopy(signature, 0, TheBIR, getLength(), signature.length);
            wrapBuffer();
        }
    }
}
