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

package de.gmxhome.conrad.jpos.jpos_base.electronicjournal;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Interface for methods that implement property setter and method calls for the ElectronicJournal device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Electronic Journal.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface ElectronicJournalInterface extends JposBaseInterface {
    /**
     * Final part of setting Station. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>All bits set in station are set in CapStation as well.</li>
     * </ul>
     *
     * @param station Station for subsequent storing data into journal.
     * @throws JposException If an error occurs.
     */
    public void station(int station) throws JposException;

    /**
     * Final part of setting StorageEnabled. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapStorageEnabled is true or flag is true.</li>
     * </ul>
     *
     * @param flag New StorageEnabled value.
     * @throws JposException If an error occurs.
     */
    public void storageEnabled(boolean flag) throws JposException;

    /**
     * Final part of setting WaterMark. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapWaterMark is true or flag is false.</li>
     * </ul>
     *
     * @param flag New WaterMark value.
     * @throws JposException If an error occurs.
     */
    public void waterMark(boolean flag) throws JposException;

    /**
     * Final part of AddMarker method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapAddMarker is true.</li>
     * </ul>
     *
     * @param marker     Marker identifier.
     * @throws JposException    If an error occurs.
     */
    public void addMarker(String marker) throws JposException;

    /**
     * Validation part of EraseMedium method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapErasableMedium is true,</li>
     *     <li>CapMediumIsAvailable is false or MediumIsAvailable is true.</li>
     * </ul>
     *
     * @return EraseMedium object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public EraseMedium eraseMedium() throws JposException;

    /**
     * Final part of EraseMedium method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a EraseMedium object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by EraseMedium.
     * @throws JposException    If an error occurs.
     */
    public void eraseMedium(EraseMedium request) throws JposException;

    /**
     * Validation part of InitializeMedium method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapInitializeMedium is true,</li>
     *     <li>mediumID in not null.</li>
     * </ul>
     *
     * @param mediumID Medium identifier.
     * @return InitializeMedium object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public InitializeMedium initializeMedium(String mediumID) throws JposException;

    /**
     * Final part of InitializeMedium method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a InitializeMedium object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by InitializeMedium.
     * @throws JposException    If an error occurs.
     */
    public void initializeMedium(InitializeMedium request) throws JposException;

    /**
     * Validation part of PrintContent method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapPrintContent is true,</li>
     *     <li>Neither fromMarker nor toMarker is null.</li>
     * </ul>
     *
     * @param fromMarker Marker that marks start position of data to be printed.
     * @param toMarker   Marker that marks end position of data to be printed.
     * @return PrintContent object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintContent printContent(String fromMarker, String toMarker) throws JposException;

    /**
     * Final part of PrintContent method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintContent object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintContent.
     * @throws JposException    If an error occurs.
     */
    public void printContent(PrintContent request) throws JposException;

    /**
     * Validation part of PrintContentFile method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapPrintContentFile is true,</li>
     *     <li>fileName is not null.</li>
     * </ul>
     *
     * @param fileName Name of file that contains printing data.
     * @return PrintContentFile object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public PrintContentFile printContentFile(String fileName) throws JposException;

    /**
     * Final part of PrintContentFile method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a PrintContentFile object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by PrintContentFile.
     * @throws JposException    If an error occurs.
     */
    public void printContentFile(PrintContentFile request) throws JposException;

    /**
     * Validation part of QueryContent method. Can be overwritten within derived
     * classes, if necessary.
     * This method shall only perform additional validation. It will be called before the service buffers the
     * method call for synchronous or asynchronous execution.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>fileName is not null,</li>
     *     <li>Neither fromMarker nor toMarker is null.</li>
     * </ul>
     *
     * @param fileName      Filename to be used to store queried contents.
     * @param fromMarker    Name of lower bound marker.
     * @param toMarker      Name of upper bound marker.
     * @return QueryContent object for use in final part.
     * @throws JposException    If an error occurs.
     */
    public QueryContent queryContent(String fileName, String fromMarker, String toMarker) throws JposException;

    /**
     * Final part of QueryContent method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a QueryContent object. This method will be called
     * when the corresponding operation shall be performed, either synchronously or asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     *
     * @param request           Output request object returned by validation method that contains all parameters
     *                          to be used by QueryContent.
     * @throws JposException    If an error occurs.
     */
    public void queryContent(QueryContent request) throws JposException;

    /**
     * Final part of CancelPrintContent method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSuspendPrintContent is true,</li>
     *     <li>Suspended is true.</li>
     * </ul>
     *
     * @throws JposException    If an error occurs.
     */
    public void cancelPrintContent() throws JposException;

    /**
     * Final part of CancelQueryContent method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSuspendQueryContent is true,</li>
     *     <li>Suspended is true.</li>
     * </ul>
     *
     * @throws JposException    If an error occurs.
     */
    public void cancelQueryContent() throws JposException;

    /**
     * Final part of ResumePrintContent method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSuspendPrintContent is true,</li>
     *     <li>Suspended is true.</li>
     * </ul>
     *
     * @throws JposException    If an error occurs.
     */
    public void resumePrintContent() throws JposException;

    /**
     * Final part of ResumeQueryContent method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapSuspendQueryContent is true,</li>
     *     <li>Suspended is true.</li>
     * </ul>
     *
     * @throws JposException    If an error occurs.
     */
    public void resumeQueryContent() throws JposException;

    /**
     * Final part of RetrieveCurrentMarker method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>marker is a String array of length 1,</li>
     *     <li>CapRetrieveCurrentMarker is true,</li>
     *     <li>marterType is one of MT_SESSION_BEG, MT_SESSION_END, MT_DOCUMENT, MT_HEAD or MT_TAIL.</li>
     * </ul>
     *
     * @param markerType     Specifies the type of the queried current marker.
     * @param marker         Contains the return value, the implementation specific marker.
     * @throws JposException If an error occurs.
     */
    public void retrieveCurrentMarker(int markerType, String[] marker) throws JposException;

    /**
     * Final part of RetrieveMarker method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>marker is a String array with length 1,</li>
     *     <li>CapRetrieveMarker is true,</li>
     *     <li>markerType is one of MT_SESSION_BEG, MT_SESSION_END or MT_DOCUMENT,</li>
     *     <li>documentNumber is &ge; 0.</li>
     * </ul>
     *
     * @param markerType     Specifies the type of the queried marker.
     * @param sessionNumber  Contains the number of the session the marker is queried for.
     * @param documentNumber Contains the number of the document the marker is queried.
     * @param marker         Contains the return value, the implementation specific marker.
     * @throws JposException If an error occurs.
     */
    public void retrieveMarker(int markerType, int sessionNumber, int documentNumber, String[] marker) throws JposException;

    /**
     * Final part of RetrieveMarkerByDateTime method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Neither dateTime normarkerNumber is null,</li>
     *     <li>marker is a String array of length 1,</li>
     *     <li>CapRetrieveMarkerByDateTime is true,</li>
     *     <li>markerType is one of MT_SESSION_BEG, MT_SESSION_END or MT_DOCUMENT,</li>
     *     <li>dateTime consists of a valid date and - if present - a valid time,</li>
     *     <li>markerNumber is numeric, the value is &ge; 1,</li>
     * </ul>
     *
     * @param markerType     Specifies the type of the queried marker.
     * @param dateTime       The date-time period the marker is queried for.
     * @param markerNumber   The number of the marker which has to be queried.
     * @param marker         Contains the return value, the implementation specific marker.
     * @throws JposException If an error occurs.
     */
    public void retrieveMarkerByDateTime(int markerType, String dateTime, String markerNumber, String[] marker) throws JposException;

    /**
     * Final part of RetrieveMarkersDateTime method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>marker is not null,</li>
     *     <li>dateTime is a String array of length 1,</li>
     *     <li>CapRetrieveMarkersDateTime is true.</li>
     * </ul>
     *
     * @param marker         Specifies the marker for which the time has to be determined.
     * @param dateTime       Contains the return value, the date and time string of the given marker.
     * @throws JposException If an error occurs.
     */
    public void retrieveMarkersDateTime(String marker, String[] dateTime) throws JposException;

    /**
     * Final part of SuspendPrintContent method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>State is S_BUSY,</li>
     *     <li>CapSuspendPrintContent is true,</li>
     *     <li>Suspended is false.</li>
     * </ul>
     *
     * @throws JposException    If an error occurs.
     */
    public void suspendPrintContent() throws JposException;

    /**
     * Final part of SuspendQueryContent method. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>State is S_BUSY,</li>
     *     <li>CapSuspendQueryContent is true,</li>
     *     <li>Suspended is false.</li>
     * </ul>
     *
     * @throws JposException    If an error occurs.
     */
    public void suspendQueryContent() throws JposException;
}
