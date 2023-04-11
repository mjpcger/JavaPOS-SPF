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
 * Interface for methods that implement property setter and method calls for the MSR device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter MSR - Magnetic Stripe
 * Reader.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface MSRInterface extends JposBaseInterface {
    /**
     * Sets data properties according to the track data. This method will be called whenever
     * data properties must be filled before a data event will be fired.
     *
     * @param tracks Contents of track data.
     */
    public void setDataProperties(Object tracks);

    /**
     * Final part of setting DataEncryptionAlgorithm. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is claimed,</li>
     *     <li>Device is not enabled,</li>
     *     <li>The new algorith value matches CapDataEncryption.</li>
     * </ul>
     *
     * @param b New DataEncryptionAlgorithm value
     * @throws JposException If an error occurs during enable or disable
     */
    public void dataEncryptionAlgorithm(int b) throws JposException;

    /**
     * Final part of setting WriteCardType. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>The new card type matches one of the names specified in CardTypeList,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or type equals the previous value of WriteCardType.</li>
     * </ul>
     *
     * @param type New WriteCardType value
     * @throws JposException If an error occurs during enable or disable
     */
    public void writeCardType(String type) throws JposException;

    /**
     * Final part of setting TracksToWrite. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>The given value for tracks to write specifies only tracks present in CapWritableTracks,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or t2w equals the previous value of TracksToWrite.</li>
     * </ul>
     *
     * @param t2w New TracksToWrite value
     * @throws JposException If an error occurs during enable or disable
     */
    public void tracksToWrite(int t2w) throws JposException;

    /**
     * Final part of setting TransmitSentinels. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>CapTransmitSentinels is false: The new value is false as well,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or flag equals the previous value of TransmitSentinels.</li>
     * </ul>
     *
     * @param flag New TransmitSentinels value
     * @throws JposException If an error occurs during enable or disable
     */
    public void transmitSentinels(boolean flag) throws JposException;

    /**
     * Final part of setting DecodeData. Can be overwritten within derived classes, if necessary. If set to false,
     * ParseDecodeData will be set to false as well.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or flag equals the previous value of DecodeData.</li>
     * </ul>
     *
     * @param flag New DecodeData value
     * @throws JposException If an error occurs during enable or disable
     */
    public void decodeData(boolean flag) throws JposException;

    /**
     * Final part of setting ErrorReportingType. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>CapDataEncryption != MSR_DE_NONE,</li>
     *     <li>The new value is one of MSR_ERT_CARD or MSR_ERT_TRACK,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or type equals the previous value of ErrorReportingType.</li>
     * </ul>
     *
     * @param type New ErrorReportingType value
     * @throws JposException If an error occurs during enable or disable
     */
    public void errorReportingType(int type) throws JposException;

    /**
     * Final part of setting ParseDecodeData. Can be overwritten within derived classes, if necessary. If ParseDecodeData
     * becomes true, DecodeData will be set to rue as well.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or flag equals the previous value of ParseDecodeData.</li>
     * </ul>
     *
     * @param flag New ParseDecodeData value
     * @throws JposException If an error occurs during enable or disable
     */
    public void parseDecodeData(boolean flag) throws JposException;

    /**
     * Final part of setting TracksToRead. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>Track to be read specifies a combination of up to 4 tracks,</li>
     *     <li>internal property AllowAlwaysSetProperties is true or t2r equals the previous value of TracksToRead.</li>
     * </ul>
     *
     * @param t2r New TracksToRead value
     * @throws JposException If an error occurs during enable or disable
     */
    public void tracksToRead(int t2r) throws JposException;

    /**
     * Final part of AuthenticateDevice method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapDeviceAuthentication is not MSR_DA_NOT_SUPPORTED.</li>
     * </ul>
     *
     * @param response see UPOS specification, method AuthenticateDevice
     * @throws JposException See UPOS specification, method AuthenticateDevice
     */
    public void authenticateDevice(byte[] response) throws JposException;

    /**
     * Final part of DeauthenticateDevice method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapDeviceAuthentication is not MSR_DA_NOT_SUPPORTED.</li>
     * </ul>
     *
     * @param response see UPOS specification, method DeauthenticateDevice
     * @throws JposException See UPOS specification, method DeauthenticateDevice
     */
    public void deauthenticateDevice(byte[] response) throws JposException;

    /**
     * Final part of RetrieveCardProperty method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapDeviceAuthentication is not MSR_DA_NOT_SUPPORTED,</li>
     *     <li>The given name must match one of the names specified in CardPropertyList,</li>
     *     <li>The value argument is an array of length 1.</li>
     * </ul>
     *
     * @param name  see UPOS specification, method RetrieveCardProperty
     * @param value see UPOS specification, method RetrieveCardProperty
     * @throws JposException See UPOS specification, method RetrieveCardProperty
     */
    public void retrieveCardProperty(String name, String[] value) throws JposException;

    /**
     * Final part of RetrieveDeviceAuthenticationData method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapDeviceAuthentication is not MSR_DA_NOT_SUPPORTED,</li>
     *     <li>The challenge argument is an array of length 1.</li>
     * </ul>
     *
     * @param challenge see UPOS specification, method RetrieveDeviceAuthenticationData
     * @throws JposException See UPOS specification, method RetrieveDeviceAuthenticationData
     */
    public void retrieveDeviceAuthenticationData(byte[][] challenge) throws JposException;

    /**
     * Final part of UpdateKey method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapDeviceAuthentication is not MSR_DA_NOT_SUPPORTED.</li>
     * </ul>
     *
     * @param key     see UPOS specification, method UpdateKey
     * @param keyName see UPOS specification, method UpdateKey
     * @throws JposException See UPOS specification, method UpdateKey
     */
    public void updateKey(String key, String keyName) throws JposException;

    /**
     * Final part of WriteTracks method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Argument data is an array of 4 non-null byte arrays,</li>
     *     <li>CapWritableTracks is not MSR_TR_NONE,</li>
     *     <li>All non-writable tracks have been specified as zero-length byte array.</li>
     * </ul>
     *
     * @param data    see UPOS specification, method WriteTracks
     * @param timeout see UPOS specification, method WriteTracks
     * @throws JposException See UPOS specification, method WriteTracks
     */
    public void writeTracks(byte[][] data, int timeout) throws JposException;
}
