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

package de.gmxhome.conrad.jpos.jpos_base.hardtotals;

import de.gmxhome.conrad.jpos.jpos_base.JposBaseInterface;
import jpos.JposException;

import java.util.List;

/**
 * Interface for methods that implement property setter and method calls for the HardTotals device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Hard Totals.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface HardTotalsInterface extends JposBaseInterface {
    /**
     * Final part of BeginTrans method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapTransaction is true,</li>
     *     <li>TransactionInProgress is false.</li>
     * </ul>
     * Keep in mind that TransactionInProgress must be set to true after successful transaction begin. This can be done
     * using the implementation in class HardTotalsProperties.
     *
     * @throws JposException If an error occurs.
     */
    public void beginTrans() throws JposException;

    /**
     * Final part of ClaimFile method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Device has not been claimed previously,</li>
     *     <li>timeout is FOREVER or positive,</li>
     * </ul>
     * If the service must wait for a release from another instance and timeout is not FOREVER, timeout will be reduced
     * by the number of milliseconds the service must wait to get exclusive access. If the service cannot get
     * exclusive access, a JposException with error code E_TIMEOUT will be thrown without calling this method.
     *
     * @param hTotalsFile   Handle of a totals file.
     * @param timeout       The (reduced) time in milliseconds to wait for the file to become available.
     * @throws JposException If an error occurs.
     */
    public void claimFile(int hTotalsFile, int timeout)  throws JposException;

    /**
     * Final part of CommitTrans method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapTransactions is true,</li>
     *     <li>TransactionInProgress is true,</li>
     *     <li>Device has not been claimed by another instance,</li>
     *     <li>All files used in stored Write or SetAll operations have not been claimed by another instance.</li>
     * </ul>
     * The ChangeRequest instances to be processed can be found in property Transaction.
     * @throws JposException If an error occurs.
     */
    public void commitTrans() throws JposException;

    /**
     * Final part of Create method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>if CapSingleFile is true, fileName is an empty string,</li>
     *     <li>otherwise, fileName consists of no more than 10 ASCII characters (character codes between 0x20 and
     *          0x7f,</li>
     *     <li>hTotalsFile is an array with length = 1,</li>
     *     <li>size is a positive value,</li>
     *     <li>Device has not been claimed by another instance.</li>
     * </ul>
     *
     * @param fileName          Name of totals file.
     * @param hTotalsFile       No matter on call, handle to file on return.
     * @param size              Requested size of the file.
     * @param errorDetection    Error detection level.
     * @throws JposException If an error occurs.
     */
    public void create(String fileName, int[] hTotalsFile, int size, boolean errorDetection) throws JposException;

    /**
     * Final part of Delete method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>if CapSingleFile is true, fileName is an empty string,</li>
     *     <li>otherwise, fileName consists of no more than 10 ASCII characters (character codes between 0x20 and
     *          0x7f,</li>
     *     <li>Device has not been claimed by another instance.</li>
     * </ul>
     * @param fileName          Name of file to be deleted.
     * @throws JposException If an error occurs.
     */
    public void delete(String fileName) throws JposException;

    /**
     * Final part of Find method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>if CapSingleFile is true, fileName is an empty string,</li>
     *     <li>otherwise, fileName consists of no more than 10 ASCII characters (character codes between 0x20 and
     *          0x7f,</li>
     *     <li>hTotalsFile is an array with length = 1,</li>
     *     <li>size is an array with length = 1.</li>
     * </ul>
     *
     * @param fileName      Name of file to be found.
     * @param hTotalsFile   No matter on call, file handle on return.
     * @param size          No matter on call, file size on return.
     * @throws JposException If an error occurs.
     */
    public void find(String fileName, int[] hTotalsFile, int[] size) throws JposException;

    /**
     * Final part of FindByIndex method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>fileName is an array with length = 1,</li>
     *     <li>index is &ge; 0 and &lt; NumberOfFiles.</li>
     * </ul>
     *
     * @param index         File index between 0 and NumberOfFiles - 1.
     * @param fileName      No matter on call, file name on return.
     * @throws JposException If an error occurs.
     */
    public void findByIndex(int index, String[] fileName) throws JposException;

    /**
     * Final part of Read method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>offset &ge; 0, count &ge; 0 and length of data &ge; count,</li>
     *     <li>offset + count &le; TotalsSize,</li>
     *     <li>Neither the device nor the file referenced by hTotalsFile has been claimed by another instance.</li>
     * </ul>
     * The list of ChangeRequest objects passed via <i>transaction</i> consists of stored Write and SetAll requests
     * stored due to transaction processing for the given file. The contents of these objects can be used to modify data
     * read to get the expected results, if necessary. However, after a transaction ends in one instance of the service,
     * remaining operations of other transactions can lead to a different read result whenever a previously buffered
     * operation will be committed later, resulting in not being overwritten by the finished transaction.
     *
     * @param hTotalsFile   Handle of a totals file.
     * @param data          Data buffer for read.
     * @param offset        Starting offset for read operation.
     * @param count         Number of bytes to be read.
     * @param transaction   List of ChangeRequest objects stored within all current transactions. An empty list if
     *                      no transaction is in progress.
     * @throws JposException If an error occurs.
     */
    public void read(int hTotalsFile, byte[] data, int offset, int count, List<ChangeRequest> transaction) throws JposException;

    /**
     * Final part of RecalculateValidationData method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Device has not been claimed by other instance,</li>
     *     <li>The file referenced by hTotalsFile has not been claimed by other instance,</li>
     *     <li>Device supports advanced error detection.</li>
     * </ul>
     *
     * @param hTotalsFile   Handle of a totals file.
     * @throws JposException If an error occurs.
     */
    public void recalculateValidationData(int hTotalsFile) throws JposException;

    /**
     * Final part of ReleaseFile method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>The file referenced by hTotalsFile has been claimed.</li>
     * </ul>
     *
     * @param hTotalsFile   Handle of a totals file.
     * @throws JposException If an error occurs.
     */
    public void releaseFile(int hTotalsFile) throws JposException;

    /**
     * Final part of Rename method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>if CapSingleFile is true, fileName is an empty string,</li>
     *     <li>otherwise, fileName consists of no more than 10 ASCII characters (character codes between 0x20 and
     *          0x7f.</li>
     * </ul>
     *
     * @param hTotalsFile   Handle of a totals file.
     * @param fileName      New file name.
     * @throws JposException If an error occurs.
     */
    public void rename(int hTotalsFile, String fileName) throws JposException;

    /**
     * Final part of Rollback method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapTransactions and TransactionInProgress are true.</li>
     * </ul>
     *
     * @throws JposException If an error occurs.
     */
    public void rollback() throws JposException;

    /**
     * Final part of OpenGate method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Device has not been claimed by other instance,</li>
     *     <li>The file referenced by hTotalsFile has not been claimed by other instance,</li>
     *     <li>Device supports advanced error detection.</li>
     * </ul>
     *
     * @param hTotalsFile   Handle of a totals file.
     * @throws JposException If an error occurs.
     */
    public void validateData(int hTotalsFile) throws JposException;

    /**
     * Validation part of SetAll method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Device has not been claimed by another instance,</li>
     *     <li>The hard totals file specified by <i>hTotalsFile</i> has not been claimed by another instance.</li>
     * </ul>
     *
     * @param hTotalsFile   Handle of a totals file.
     * @param value         Value to set all locations to in totals file.
     * @return SetAll object for use in final part or in CommitTrans.
     * @throws JposException    If an error occurs.
     */
    public SetAll setAll(int hTotalsFile, byte value) throws JposException;

    /**
     * Final part of SetAll method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a SetAll object. This method
     * will be called when the corresponding operation shall be performed, either synchronously during commit.
     * All plausibility checks have been made before, only runtime errors can occur.
     * <br>The default implementation should be called within derived methods to ensure that the property
     * TransactionInProgress is updated as expected.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by SetAll.
     * @throws JposException    If an error occurs.
     */
    public void setAll(SetAll request) throws JposException;

    /**
     * Validation part of Write method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>The byte buffer consists of at least <i>count</i> bytes,</li>
     *     <li>Both, <i>offset</i> and <i>count</i>, are &ge; 0 and their sum is %le; TotalsSize,</li>
     *     <li>Device has not been claimed by another instance,</li>
     *     <li>The hard totals file specified by <i>hTotalsFile</i> has not been claimed by another instance.</li>
     * </ul>
     *
     * @param hTotalsFile   Handle of a totals file.
     * @param data          Data to be written.
     * @param offset        Starting offset for write operation.
     * @param count         Number of bytes to be written.
     * @return Write object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public Write write(int hTotalsFile, byte[] data, int offset, int count) throws JposException;

    /**
     * Final part of Write method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a Write object. This method
     * will be called when the corresponding operation shall be performed, either synchronously during commit.
     * All plausibility checks have been made before, only runtime errors can occur.
     * <br>The default implementation should be called within derived methods to ensure that the property
     * TransactionInProgress is updated as expected.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by Write.
     * @throws JposException    If an error occurs.
     */
    public void write(Write request) throws JposException;
}
