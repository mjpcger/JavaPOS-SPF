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

package de.gmxhome.conrad.jpos.jpos_base.rfidscanner;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Interface for methods that implement property setter and method calls for the RFIDScanner device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter RFID Scanner.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 * <br>No final parts have been specified for methods FirstTag, NextTag and PreviousTag because these methods
 * will be handled completely by the service. A specific service implementation must only fill all tag data into
 * an RFIDScannerDataEvent an fire that event, The remainder will be handled via standard event handling and via
 * standard method implementations within the RFIDScannerService object.
 */
public interface RFIDScannerInterface extends JposBaseInterface {
    /**
     * Final part of setting ProtocolMask. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is claimed,</li>
     *     <li>Mask contains only bits that are set in CapMultipleProtocols as well.</li>
     * </ul>
     *
     * @param mask Bit pattern wherein each bit signifies one predefined RFID tag protocol.
     * @throws JposException If an error occurs.
     */
    public void protocolMask(int mask) throws JposException;

    /**
     * Final part of setting ReadTimerInterval. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is claimed,</li>
     *     <li>CapReadTimer is true,</li>
     *     <li>ContinuousReadMode is false,</li>
     *     <li>interval is not negative. </li>
     * </ul>
     *
     * @param interval The minimum time interval between tag reads in milliseconds.
     * @throws JposException If an error occurs.
     */
    public void readTimerInterval(int interval) throws JposException;

    /**
     * Final part of StopReadTags method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>ContinuousReadMode is true.</li>
     * </ul>
     * After successful completion, ContinuousReadMode must be reset to false.
     *
     * @param password Authorized key for reader, if needed, null or zero length binary otherwise.
     * @throws JposException If an error occurs.
     */
    public void stopReadTags(byte[] password) throws JposException;

    /**
     * Final part of StartReadTags method. Can be overwritten in derived class, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapContinuousRead is true,</li>
     *     <li>ContinuousReadMode is false,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>filterID and filtermask have the same length,</li>
     *     <li>cmd is one of RT_ID, RT_FULLUSERDATA, RT_PARTIALUSERDATA, RT_ID_FULLUSERDATA or RT_ID_PARTIALUSERDATA,</li>
     *     <li>If cmd is one of RT_PARTIALUSERDATA or RT_ID_PARTIALUSERDATA, start and length are positive.</li>
     * </ul>
     * Since the UPOS specification specifies StartReadMode as asynchronous method, this method must only start
     * polling and return after setting ContinuousReadMode to true.
     *
     * @param cmd        Read command, specifies what has to be read.
     * @param filterID   Holds a bit pattern to be AND’ed with filtermask to specify which tags shall be read.
     * @param filtermask Mask for filterID and tag ID, a tag will be read whenever the tag ID AND'ed with filtermask
     *                   is equal to filterID AND'ed with filtermask.
     * @param start      In case of partial user data read, start specifies the zero-based position within user data
     *                   where read shall start.
     * @param length     In case of partial user data read, length specifies the number of bytes to be read.
     * @param password Authorized key for reader, if needed, null or zero length binary otherwise.
     * @return StartReadTags object for use in final part.
     * @throws JposException If an error occurs.
     */
    public StartReadTags startReadTags(int cmd, byte[] filterID, byte[] filtermask, int start, int length, byte[] password) throws JposException;


    /**
     * Validation part of ReadTags method. Can be overwritten in derived class, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>ContinuousReadMode is false,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>filterID and filtermask have the same length,</li>
     *     <li>cmd is one of RT_ID, RT_FULLUSERDATA, RT_PARTIALUSERDATA, RT_ID_FULLUSERDATA or RT_ID_PARTIALUSERDATA,</li>
     *     <li>If cmd is one of RT_PARTIALUSERDATA or RT_ID_PARTIALUSERDATA, start and length are positive,</li>
     *     <li>timeout is positive or FOREVER.</li>
     * </ul>
     * Since the UPOS specification specifies StartReadMode as asynchronous method, this method must only start
     * polling and return after setting ContinuousReadMode to true.
     *
     * @param cmd        Read command, specifies what has to be read.
     * @param filterID   Holds a bit pattern to be AND’ed with filtermask to specify which tags shall be read.
     * @param filtermask Mask for filterID and tag ID, a tag will be read whenever the tag ID AND'ed with filtermask
     *                   is equal to filterID AND'ed with filtermask.
     * @param start      In case of partial user data read, start specifies the zero-based position within user data
     *                   where read shall start.
     * @param length     In case of partial user data read, length specifies the number of bytes to be read.
     * @param timeout    Allowed execution time, in milliseconds or FOREVER for unlimited execution time.
     * @param password   Authorized key for reader, if needed, null or zero length binary otherwise.
     * @return ReadTags object for use in final part.
     * @throws JposException If an error occurs.
     */
    public ReadTags readTags(int cmd, byte[] filterID, byte[] filtermask, int start, int length, int timeout, byte[] password) throws JposException;

    /**
     * Validation part of DisableTag method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapDisableTag is true,</li>
     *     <li>ContinuousReadMode is false,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>timeout is positive or FOREVER.</li>
     * </ul>
     *
     * @param tagID    Tag ID to be processed.
     * @param timeout  Allowed execution time, in milliseconds.
     * @param password Authorized key for reader that might be required, zero length if not needed.
     * @return DisableTag object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public DisableTag disableTag(byte[] tagID, int timeout, byte[] password) throws JposException;

    /**
     * Validation part of LockTag method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapLockTag is true,</li>
     *     <li>ContinuousReadMode is false,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>timeout is positive or FOREVER.</li>
     * </ul>
     *
     * @param tagID    Tag ID to be processed.
     * @param timeout  Allowed execution time, in milliseconds.
     * @param password Authorized key for reader that might be required, zero length if not needed.
     * @return LockTag object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public LockTag lockTag(byte[] tagID, int timeout, byte[] password) throws JposException;

    /**
     * Validation part of WriteTagData method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapWriteTag is one of CWT_ALL or CWT_USERDATA,</li>
     *     <li>ContinuousReadMode is false,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>start is positive,</li>
     *     <li>timeout is positive or FOREVER.</li>
     * </ul>
     *
     * @param tagID    Tag ID to be processed.
     * @param userdata Data to be written.
     * @param start    Zero-based position within the tags UserData field to begin writing.
     * @param timeout  Allowed execution time, in milliseconds.
     * @param password Authorized key for reader that might be required, zero length if not needed.
     * @return WriteTagData object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public WriteTagData writeTagData(byte[] tagID, byte[] userdata, int start, int timeout, byte[] password) throws JposException;

    /**
     * Validation part of DisableTag method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapWriteTag is one of CWT_ALL or CWT_ID,</li>
     *     <li>ContinuousReadMode is false,</li>
     *     <li>State is S_IDLE,</li>
     *     <li>timeout is positive or FOREVER.</li>
     * </ul>
     *
     * @param sourceID Original Tag ID to be processed.
     * @param destID   New ID of the tag.
     * @param timeout  Allowed execution time, in milliseconds.
     * @param password Authorized key for reader that might be required, zero length if not needed.
     * @return WriteTagID object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public WriteTagID writeTagID(byte[] sourceID, byte[] destID, int timeout, byte[] password) throws JposException;

    /**
     * Final part of DisableTag method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a DisableTag object. This method
     * will be called when the corresponding operation shall be performed.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by DisableTag.
     * @throws JposException    If an error occurs.
     */
    public void disableTag(DisableTag request) throws JposException;

    /**
     * Final part of LockTag method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a LockTag object. This method
     * will be called when the corresponding operation shall be performed.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by LockTag.
     * @throws JposException    If an error occurs.
     */
    public void lockTag(LockTag request) throws JposException;

    /**
     * Final part of WriteTagData method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a WriteTagData object. This method
     * will be called when the corresponding operation shall be performed.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by WriteTagData.
     * @throws JposException    If an error occurs.
     */
    public void writeTagData(WriteTagData request) throws JposException;

    /**
     * Final part of WriteTagID method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a WriteTagID object. This method
     * will be called when the corresponding operation shall be performed.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by WriteTagID.
     * @throws JposException    If an error occurs.
     */
    public void writeTagID(WriteTagID request) throws JposException;


    /**
     * Final part of ReadTags method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a ReadTags object. This method
     * will be called when the corresponding operation shall be performed.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Input request object returned by validation method that contains all parameters
     *                          to be used by ReadTags.
     * @throws JposException    If an error occurs.
     */
    public void readTags(ReadTags request) throws JposException;

    /**
     * Final part of StartReadTags method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a StartReadTags object. This method
     * will be called when the corresponding operation shall be performed.
     * All plausibility checks have been made before, only runtime errors can occur.
     *
     * @param request           Input request object returned by validation method that contains all parameters
     *                          to be used by StartReadTags.
     * @throws JposException    If an error occurs.
     */
    public void startReadTags(StartReadTags request) throws JposException;
}
