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

import de.gmxhome.conrad.jpos.jpos_base.JposBaseInterface;
import jpos.JposException;

/**
 * Interface for methods that implement property setter and method calls for the Biometrics device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Biometrics.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface BiometricsInterface extends JposBaseInterface {
    /**
     * Final part of setting Algorithm. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is claimed,</li>
     *     <li>Device is not enabled,</li>
     *     <li>newAlgorithm is between 0 and the number of algorithms specified in AlgorithmList.</li>
     * </ul>
     *
     * @param newAlgorithm New value for Algorithm property.
     * @throws JposException If an error occurs.
     */
    void algorithm(int newAlgorithm) throws JposException;

    /**
     * Final part of setting RealTimeDataEnabled. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>CapRealTimeData is true or newRealTimeData is false,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or newRealTimeDataEnabled equals the previous value of
     *     RealTimeDataEnabled.</li>
     * </ul>
     *
     * @param newRealTimeDataEnabled New value for RealTimeDataEnabled property.
     * @throws JposException If an error occurs.
     */
    void realTimeDataEnabled(boolean newRealTimeDataEnabled) throws JposException;

    /**
     * Final part of setting SensorColor. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is open,</li>
     *     <li>newSensorColor is one of SC_MONO, SC_GRAYSCALE, SC_16, SC_256 or SC_FULL,</li>
     *     <li>the corresponding bit in CapSensorColor is set,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or newSensorColor equals the previous value of SensorColor.</li>
     * </ul>
     *
     * @param newSensorColor New value for SensorColor property.
     * @throws JposException If an error occurs.
     */
    public void sensorColor(int newSensorColor) throws JposException;

    /**
     * Final part of setting SensorOrientation. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is claimed,</li>
     *     <li>Device is not enabled,</li>
     *     <li>newSensorOrientation is one of SO_NORMAL, SO_RIGHT, SO_INVERTED or SO_LEFT,</li>
     *     <li>the corresponding bit in CapSensorOrientation is set.</li>
     * </ul>
     *
     * @param newSensorOrientation New value for SensorOrientation property.
     * @throws JposException If an error occurs.
     */
    public void sensorOrientation(int newSensorOrientation) throws JposException;

    /**
     * Final part of setting SensorType. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>newSensorType is one of ST_FACIAL_FEATURES, ST_VOICE, ST_FINGERPRINT, ST_IRIS, ST_RETINA,
     *     ST_HAND_GEOMETRY, ST_SIGNATURE_DYNAMICS, ST_KEYSTROKE_DYNAMICS, ST_LIP_MOVEMENT, ST_THERMAL_FACE_IMAGE,
     *     ST_THERMAL_HAND_IMAGE, ST_GAIT or ST_PASSWORD,</li>
     *     <li>the corresponding bit in CapSensorType is set.</li>
     * </ul>
     *
     * @param newSensorType New value for SensorType property.
     * @throws JposException If an error occurs.
     */
    public void sensorType(int newSensorType) throws JposException;

    /**
     * Final part of BeginEnrollCapture method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>referenceBIR and payload are not null (null arguments will be passed as byte[0]),</li>
     *     <li>CapTemplateAdaptation or isEmpty(referenceBIR, true) is true.</li>
     * </ul>
     *
     * @param referenceBIR Optional BIR to be adapted (updated).
     * @param payload      Data that will be stored by the BSP.
     * @throws JposException If an error occurs.
     */
    public void beginEnrollCapture(byte[] referenceBIR, byte[] payload) throws JposException;

    /**
     * Final part of BeginVerifyCapture method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     *
     * @throws JposException If an error occurs.
     */
    public void beginVerifyCapture() throws JposException;

    /**
     * Final part of EndCapture method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     *
     * @throws JposException If an error occurs.
     */
    public void endCapture() throws JposException;

    /**
     * Final part of Identify method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>maxFARRequested and maxFRRRequested have been checked with checkFARorFRRLimit,</li>
     *     <li>timeout is positive or FOREVER,</li>
     *     <li>referenceBIRPopulation has a length &gt; 0,</li>
     *     <li>each referenceBIR in referenceBIRPopulation is valid (checkBIRPurpose(referenceBIR, null) is true),</li>
     *     <li>candidateRanking is a valid reference (non-null int[1][]).</li>
     * </ul>
     * Even if FAR and FRR are specified to be percentage values in the UPOS specification, the corresponding values
     * have been specified as probability value in 1/Integer.MAX_VALUE units in the BioAPI.
     *
     * @param maxFARRequested        The requested FAR criterion for successful verification.
     * @param maxFRRRequested        The requested FRR criterion for successful verification.
     * @param fARPrecedence          If both criteria are provided, this parameter indicates which takes precedence.
     * @param referenceBIRPopulation An array of BIRs against which the Identify match is performed.
     * @param candidateRanking       Array reference of BIR indices from the referenceBIRPopulation listed in rank order.
     * @param timeout                Timeout for successful biometric capture.
     * @throws JposException If an error occurs.
     */
    public void identify(int maxFARRequested, int maxFRRRequested, boolean fARPrecedence, byte[][] referenceBIRPopulation, int[][] candidateRanking, int timeout) throws JposException;

    /**
     * Final part of IdentifyMatch method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>maxFARRequested and maxFRRRequested have been checked with checkFARorFRRLimit,</li>
     *     <li>sampleBIR is not empty (isDataEmpty(sampleBIR, true) is false),</li>
     *     <li>referenceBIRPopulation has a length &gt; 0,</li>
     *     <li>each referenceBIR in referenceBIRPopulation is valid (checkBIRPurpose(referenceBIR, null) is true),</li>
     *     <li>candidateRanking is a valid reference (non-null int[1][]).</li>
     * </ul>
     *
     * @param maxFARRequested        The requested FAR criterion for successful verification.
     * @param maxFRRRequested        The requested FRR criterion for successful verification.
     * @param fARPrecedence          If both criteria are provided, this parameter indicates which takes precedence.
     * @param sampleBIR              The BIR to be identified.
     * @param referenceBIRPopulation An array of BIRs against which the Identify match is performed.
     * @param candidateRanking       Array reference of BIR indices from the referenceBIRPopulation listed in rank order.
     * @throws JposException If an error occurs.
     */
    public void identifyMatch(int maxFARRequested, int maxFRRRequested, boolean fARPrecedence, byte[] sampleBIR, byte[][] referenceBIRPopulation, int[][] candidateRanking) throws JposException;

    /**
     * Final part of ProcessPrematchData method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>sampleBIR and prematchDataBIR are not empty (isEmpty(sampleBIR, true) and isEmpty(prematchDataBIR, true)
     *     are false),</li>
     *     <li>processedBIR is a valid reference (non-null byte[1][]),</li>
     *     <li>CapPrematchData is true.</li>
     * </ul>
     *
     * @param sampleBIR       BIR to be processed.
     * @param prematchDataBIR BIR containing prematch data.
     * @param processedBIR    Reference for the new constructed BIR.
     * @throws JposException If an error occurs.
     */
    public void processPrematchData(byte[] sampleBIR, byte[] prematchDataBIR, byte[][] processedBIR) throws JposException;

    /**
     * Final part of Verify method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>maxFARRequested and maxFRRRequested have been checked with checkFARorFRRLimit,</li>
     *     <li>timeout is positive or FOREVER,</li>
     *     <li>referenceBIR is valid (checkBIRPurpose(referenceBIR, null) is true),</li>
     *     <li>adaptedBIR is a valid reference (non-null byte[1][]),</li>
     *     <li>result is a valid reference (non-null boolean[1]),</li>
     *     <li>fARAchieved and fRRAchieved are a valid references (non-null int[1]),</li>
     *     <li>payload is a valid reference (non-null byte[1][]),</li>
     *     <li>adaptedBIR is a valid reference (non-null byte[1][]),</li>
     *     <li>candidateRanking is a valid reference (non-null int[1][]).</li>
     * </ul>
     *
     * @param maxFARRequested        The requested FAR criterion for successful verification.
     * @param maxFRRRequested        The requested FRR criterion for successful verification.
     * @param fARPrecedence          If both criteria are provided, this parameter indicates which takes precedence.
     * @param referenceBIR           The BIR to be verified against.
     * @param adaptedBIR             Reference of the adapted BIR.
     * @param result                 Reference of the result.
     * @param fARAchieved            Reference to FAR Value indicating the closeness of the match.
     * @param fRRAchieved            Reference to FRR Value indicating the closeness of the match.
     * @param payload                Reference to optional payload.
     * @param timeout                Timeout for successful verification.
     * @throws JposException If an error occurs.
     */
    public void verify(int maxFARRequested, int maxFRRRequested, boolean fARPrecedence, byte[] referenceBIR, byte[][] adaptedBIR, boolean[] result, int[] fARAchieved, int[] fRRAchieved, byte[][] payload, int timeout) throws JposException;

    /**
     * Final part of VerifyMatch method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>maxFARRequested and maxFRRRequested have been checked with checkFARorFRRLimit,</li>
     *     <li>timeout is positive or FOREVER,</li>
     *     <li>sampleBIR is not empty (isDataEmpty(sampleBIR, true) is false),</li>
     *     <li>referenceBIR is valid (checkBIRPurpose(referenceBIR, null) is true),</li>
     *     <li>adaptedBIR is a valid reference (non-null byte[1][]),</li>
     *     <li>result is a valid reference (non-null boolean[1]),</li>
     *     <li>fARAchieved and fRRAchieved are a valid references (non-null int[1]),</li>
     *     <li>payload is a valid reference (non-null byte[1][]),</li>
     *     <li>adaptedBIR is a valid reference (non-null byte[1][]),</li>
     *     <li>candidateRanking is a valid reference (non-null int[1][]).</li>
     * </ul>
     *
     * @param maxFARRequested        The requested FAR criterion for successful verification.
     * @param maxFRRRequested        The requested FRR criterion for successful verification.
     * @param fARPrecedence          If both criteria are provided, this parameter indicates which takes precedence.
     * @param sampleBIR              The BIR to be identified.
     * @param referenceBIR           The BIR to be verified against.
     * @param adaptedBIR             Reference of the adapted BIR.
     * @param result                 Reference of the result.
     * @param fARAchieved            Reference to FAR Value indicating the closeness of the match.
     * @param fRRAchieved            Reference to FRR Value indicating the closeness of the match.
     * @param payload                Reference to optional payload.
     * @throws JposException If an error occurs.
     */
    public void verifyMatch(int maxFARRequested, int maxFRRRequested, boolean fARPrecedence, byte[] sampleBIR, byte[] referenceBIR, byte[][] adaptedBIR, boolean[] result, int[] fARAchieved, int[] fRRAchieved, byte[][] payload) throws JposException;

    /**
     * Checks whether a FAR or FRR limit is valid. Negative FAR or FRR limits are invalid. Depending on the interpretation
     * of FAR and FRR values as specified in the UPOS specification (percentage values) or as used in the BioAPI reference
     * implementation (probability in units of 1/Integer.MAX_VALUE), it can be necessary to override this method within
     * the implementation class. The default implementation is interpretation as specified in UPOS specification, throwing
     * a JposException if limit is less than zero or greater than 100 (percentage values).
     * @param limit FAR or FRR limit to be checked.
     * @param name  limit name (maxFARRequested or maxFRRRequested).
     * @throws JposException If limit is out of range, at least if negative.
     */
    public void checkFARorFRRLimit(int limit, String name) throws JposException;

    /**
     * Checks if a BIR is valid for a given purpose.
     * @param bir    The BIR to be checked.
     * @param verify Must be set to true to check for validity for verification, to false for validity check for
     *               identification or to null for validity check for use as reference BIR.
     * @return true if the BIR is valid for the specified purpose, false otherwise.
     */
    public boolean checkBIRPurpose(byte[] bir, Boolean verify);

    /**
     * Checks whether a given BIR or payload is empty. In the default implementation, empty means the given data is null
     * or all bytes of data are zero. In addition, if isBIR is true, data is also empty if data.length is not greater
     * than the BIR header length (16) or the Length field of the BIR is equal to the header length.
     * @param data  The BIR or payload to be checked.
     * @param isBIR True in case of a BIR, false for payload value.
     * @return true if data is empty, false otherwise.
     */
    public boolean isDataEmpty(byte[] data, boolean isBIR);
}
