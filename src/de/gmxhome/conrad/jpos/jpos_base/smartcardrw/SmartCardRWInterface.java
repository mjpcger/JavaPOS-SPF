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

package de.gmxhome.conrad.jpos.jpos_base.smartcardrw;

import de.gmxhome.conrad.jpos.jpos_base.JposBaseInterface;
import de.gmxhome.conrad.jpos.jpos_base.JposOutputRequest;
import jpos.JposException;

/**
 * Interface for methods that implement property setter and method calls for the SmartCardRW device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Smart Card Reader / Writer.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface SmartCardRWInterface extends JposBaseInterface {
    /**
     * Final part of setting InterfaceMode. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>mode is one of MODE_TRANS, MODE_BLOCK, MODE_APDU or MODE_XML,</li>
     *     <li>The corresponding capability bit in CapInterfaceMode, CMODE_TRANS, CMODE_BLOCK, CMODE_APDU or MODE_XML,
     *     is set, too.</li>
     * </ul>
     *
     * @param mode AdditionalSecurityInformation for subsequent storing data into journal.
     * @throws JposException If an error occurs.
     */
    void interfaceMode(int mode) throws JposException;

    /**
     * Final part of setting IsoEmvMode. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>mode is one of MODE_ISO or MODE_EMV,</li>
     *     <li>The corresponding capability bit in CapIsoEmvMode, CMODE_ISO or CMODE_EMV, is set, too.</li>
     * </ul>
     *
     * @param mode Indicates the message modes the SCR/W shall use.
     * @throws JposException If an error occurs.
     */
    void isoEmvMode(int mode) throws JposException;

    /**
     * Final part of setting SCSlot. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Only one bit has been set in slots,</li>
     *     <li>The bit set in slots is set in CapSCSlots as well.</li>
     * </ul>
     *
     * @param slot New current slot.
     * @throws JposException If an error occurs.
     */
    void sCSlot(int slot) throws JposException;

    /**
     * Final part of BeginInsertion method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>timeout is FOREVER or &ge; 0.</li>
     * </ul>
     * @param timeout The number of milliseconds before failing the method.
     * @throws JposException If an error occurs.
     */
    void beginInsertion(int timeout) throws JposException;

    /**
     * Final part of BeginRemoval method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>timeout is FOREVER or &ge; 0.</li>
     * </ul>
     * @param timeout The number of milliseconds before failing the method.
     * @throws JposException If an error occurs.
     */
    void beginRemoval(int timeout) throws JposException;

    /**
     * Final part of EndInsertion method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     * @throws JposException If an error occurs.
     */
    void endInsertion() throws JposException;

    /**
     * Final part of EndRemoval method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     * @throws JposException If an error occurs.
     */
    void endRemoval() throws JposException;

    /**
     * Final part of ReadData method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>count and data are arrays with length one,</li>
     *     <li>action is one of READ_DATA, READ_PROGRAM, EXECUTE_AND_READ_DATAor XML_READ_BLOCK_DATA.</li>
     * </ul>
     * @param action Indicates the type of processing of the data.
     * @param count  The total number of data bytes that have been returned.
     * @param data   The data that is returned.
     * @throws JposException If an error occurs.
     */
    void readData(int action, int[] count, String[] data) throws JposException;

    /**
     * Validation part of WriteData method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>action is one of STORE_DATA, STORE_PROGRAM, EXECUTE_DATA, XML_BLOCK_DATA, SECURITY_FUSE or RESET,</li>
     *     <li>count is &gt; 0.</li>
     * </ul>
     *
     * @param action Indicates the type of processing of the data.
     * @param count  The total number of data bytes that shall be sent.
     * @param data   The data to be sent.
     * @throws JposException    If an error occurs.
     * @return WriteData object for use in final part.
     */
    WriteData writeData(int action, int count, String data) throws JposException;

    /**
     * Final part of WriteData method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a WriteData object. This method
     * will be called when the corresponding operation shall be performed, either synchronously during commit.
     * All plausibility checks have been made before, only runtime errors can occur.
     * <br>The default implementation should be called within derived methods to ensure that the property
     * TransactionInProgress is updated as expected.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by WriteData.
     * @throws JposException    If an error occurs.
     */
    void writeData(WriteData request) throws JposException;
}
