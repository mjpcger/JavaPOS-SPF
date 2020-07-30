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

package de.gmxhome.conrad.jpos.jpos_base;

import jpos.JposException;

/**
 * Interface for methods that implement common property setter and common method calls. Each class that implements this
 * interface must have access to the property set used by the interface function. Since almost always the property set
 * class implements the interface, this is no real restriction.
 */
public interface JposBaseInterface {
    /**
     * Removes property set from device specific property set list.
     */
    public void removeFromPropertySetList();

    /**
     * Final part of setting DeviceEnabled. Can be overwritten within derived classes. Performs initOnEnable method of
     * corresponding property set in addition to setting DeviceEnabled.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>ExclusiveUse equals ExclusiveYes: Device is claimed,</li>
     *     <li>DeviceEnabled != enabled.</li>
     * </ul>
     *
     * @param enable True to enable, false to disable
     * @throws JposException If an error occurs during enable or disable
     */
    public void deviceEnabled(boolean enable) throws JposException;

    /**
     * Final part of setting FreezeEvents. Can be overwritten within derived classes, if necessary. Calls processEventList
     * and processDataEventList in addition to setting FreezeEvents.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed.</li>
     * </ul>
     *
     * @param freezeEvents False to enable, true to disable event delivery
     * @throws JposException If an error occurs during enable or disable
     */
    public void freezeEvents(boolean freezeEvents) throws JposException;

    /**
     * Final part of setting PowerNotify. Can be overwritten within derived classes.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>Device is not enabled,</li>
     *     <li>powerNotify is PN_DISABLED or PN_ENABLED,</li>
     *     <li>CapPowerReporting is not PR_NONE or powerNotify is PN_DISABLED.</li>
     * </ul>
     * If powerNotify equals PN_DISABLED, PowerState will be set to PS_UNKNOWN after this method
     * returns.
     *
     * @param powerNotify New property value
     * @throws JposException If an error occurs while setting the property
     */
    public void powerNotify(int powerNotify) throws JposException;

    /**
     * Final part of setting AutoDisable. Can be overwritten within derived classes.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed.</li>
     * </ul>
     *
     * @param b New property value
     * @throws JposException If an error occurs while setting the property
     */
    public void autoDisable(boolean b) throws JposException;

    /**
     * Final part of setting AsyncMode. Can be overwritten within derived classes.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled.</li>
     * </ul>
     *
     * @param b New property value
     * @throws JposException If an error occurs while setting the property.
     */
    public void asyncMode(boolean b) throws JposException;

    /**
     * Final part of setting DataEventEnabled. Can be overwritten within derived classes. Calls processDataEventList in
     * addition to setting DataEventEnabled
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed.</li>
     * </ul>
     *
     * @param b New property value
     * @throws JposException If an error occurs while setting the property DataEventEnabled.
     */
    public void dataEventEnabled(boolean b) throws JposException;

    /**
     * Final part of setting FlagWhenIdle. Can be overwritten within derived classes. Setting it to true
     * will be processed only if State is neither S_ERROR nor S_BUSY. Otherwise, a StatusUpdateEvent with
     * the device type specific FlagWhenIdle status value will be generated and FlagWhenIdle remains false.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has been enabled.</li>
     * </ul>
     *
     * @param b New property value
     * @throws JposException If an error occurs while setting the property FlagWhenIdle.
     */
    public void flagWhenIdle(boolean b) throws JposException;

    /**
     * Final part of setting claim. Can be overwritten within derived classes.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>The device is not claimed,</li>
     *     <li>ExclusiveUse dos not equal ExclusiveNo,</li>
     *     <li>timeout is negative not equal to FOREVER.</li>
     * </ul>
     *
     * @param timeout see UPOS specification, method Claim
     * @throws JposException If an error occurs while claiming the device
     */
    public void claim(int timeout) throws JposException;

    /**
     * Final part of close method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is neither enabled nor claimed.</li>
     * </ul>
     *
     * @throws JposException See UPOS specification, method Close
     */
    public void close() throws JposException;

    /**
     * Final part of CheckHealth method. Can be overwritten in derived class, if necessary. Keep in mind that dvice class
     * specific checks (e.g. check of Claimed or DeviceEnabled) must be done within derived classes.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is neither enabled nor claimed.</li>
     * </ul>
     *
     * @param level See UPOS specification, method CheckHealth
     * @throws JposException See UPOS specification, method CheckHealth
     */
    public void checkHealth(int level) throws JposException;

    /**
     * Final or validation part of DirectIO method. Can be overwritten in derived class, if necessary.
     * This method will be called whenever the service's directIO method will be called.
     * All checks, if necessary, must be implemented within the derived service implementation.
     * <br>In case of validation only (asynchronous mode), a DirectIO object must be be created with
     * command, data[0] and object as parameters. Last action in validation must be to call its throwRequest
     * method.
     *
     * @param command See UPOS specification, method DirectIO
     * @param data    See UPOS specification, method DirectIO
     * @param object  See UPOS specification, method DirectIO
     * @throws JposException See UPOS specification, method DirectIO.
     */
    public void directIO(int command, int[] data, Object object) throws JposException;

    /**
     * Final part of DirectIO method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via a DirectIO object. This method will be called
     * when the corresponding operation shall be performed asynchronously. All plausibility
     * checks have been made before, only runtime errors can occur.
     * <br>This method will only be called if the validation method threw a JposException with ErrorCode = 0.
     *
     * @param request           Output request object that contains all parameters to be used by ReadWeight.
     * @throws JposException    If an error occurs.
     */
    public void directIO(DirectIO request) throws JposException;

    /**
     * Final part of open method. Can be overwritten in derived class, if necessary. sets <b>State</b> property and
     * calls method initOnOpen of object specified by <b>dev</b>.
     * This method will be called whenever the service's open method will be called.
     * All checks, if necessary, must be implemented within the derived service implementation.
     *
     * @throws JposException See UPOS specification, method Open
     */
    public void open() throws JposException;

    /**
     * Final part of release method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is claimed,</li>
     *     <li>ExclusiveUse equals ExclusiveYes: Device is not enabled.</li>
     * </ul>
     *
     * @throws JposException See UPOS specification, method Release
     */
    public void release() throws JposException;

    /**
     * Clear input processing: Clear data event list, clear data event count, set State to idle.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>ExclusiveUse equals ExclusiveYes: Device is claimed.</li>
     * </ul>
     *
     * @throws JposException See UPOS specification, method ClearInput
     */
    public void clearInput() throws JposException;

    /**
     * Class factory for JposOutputRequests.
     * @return A new instance of JposOutputRequest or a derived class.
     */
    public JposOutputRequest newJposOutputRequest();

    /**
     * Clear output processing. Removes all OutputCompleteEvents and ErrorEvents from event queue and
     * sets State to S_IDLE. Clears all outstanding commands of the given property set as well.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>ExclusiveUse equals ExclusiveYes: Device is claimed.</li>
     * </ul>
     *
     * @throws JposException See UPOS specification, method ClearOutput
     */
    public void clearOutput() throws JposException;

    /**
     * Final part of CompareFirmwareVersion method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapCompareFirmwareVersion is true.</li>
     * </ul>
     *
     * @param firmwareFileName See UPOS specification, method CompareFirmwareVersion.
     * @param result           See UPOS specification, method CompareFirmwareVersion.
     * @throws JposException See UPOS specification, method CompareFirmwareVersion
     */
    public void compareFirmwareVersion(String firmwareFileName, int[] result) throws JposException;

    /**
     * Final or validation part of UpdateFirmware method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapUpdateFirmware is true.</li>
     * </ul>
     * In case of validation only (asynchronous mode), an UpdateFirmware object must be created with
     * firmwareFileName as parameters. Last action in validation must be to call its throwRequest method.
     * Otherwise, the service must implement the necessary functionality to perform the firmware update
     * in background.
     *
     * @param firmwareFileName See UPOS specification, method UpdateFirmware
     * @throws JposException See UPOS specification, method UpdateFirmware
     */
    public void updateFirmware(String firmwareFileName) throws JposException;

    /**
     * Final part of UpdateFirmware method. Can be overwritten within derived classes, if necessary.
     * The parameters of the method will be passed via an UpdateFirmware object. All plausibility
     * checks have been made before, only runtime errors can occur.
     * <br>The result of this method will always be provided by the framework via StatusUpdateEvent.
     * Its Status property can be set as follows:
     * <ul>
     *     <li>If the method ends normally, Status will be SUE_UF_COMPLETE.</li>
     *     <li>If the method ends with a JposException with ErrorCodeExtended set to SUE_UF_COMPLETE,
     *         SUE_UF_COMPLETE_DEV_NOT_RESTORED, SUE_UF_FAILED_DEV_OK, SUE_UF_FAILED_DEV_UNRECOVERABLE,
     *         SUE_UF_FAILED_DEV_NEEDS_FIRMWARE or SUE_UF_FAILED_DEV_UNKNOWN, Status will be set to
     *         ErrorCodeExtended.
     *     </li>
     *     <li>In case of any other exception, Status will be set to the value stored in the Result
     *         property of request. The default is SUE_UF_FAILED_DEV_UNKNOWN, but can be changed via
     *         method setResult of request.
     *     </li>
     * </ul>
     *
     * @param request           Output request object that contains all parameters to be used by ReadWeight.
     * @throws JposException    If an error occurs.
     */
    public void updateFirmware(UpdateFirmware request) throws JposException;

    /**
     * Final part of ResetStatistics method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapUpdateStatistics and CapStatisticsReporting are true.</li>
     * </ul>
     *
     * @param statisticsBuffer See UPOS specification, method ResetStatistics
     * @throws JposException See UPOS specification, method ResetStatistics
     */
    public void resetStatistics(String statisticsBuffer) throws JposException;

    /**
     * Final part of RetrieveStatistics method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapStatisticsReporting are true,</li>
     *     <li>statisticsBuffer is not null.</li>
     * </ul>
     *
     * @param statisticsBuffer See UPOS specification, method RetrieveStatistics
     * @throws JposException See UPOS specification, method RetrieveStatistics
     */
    public void retrieveStatistics(String[] statisticsBuffer) throws JposException;

    /**
     * Final part of UpdateStatistics method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapUpdateStatistics and CapStatisticsReporting are true.</li>
     * </ul>
     *
     * @param statisticsBuffer See UPOS specification, method UpdateStatistics
     * @throws JposException See UPOS specification, method UpdateStatistics
     */
    public void updateStatistics(String statisticsBuffer) throws JposException;

    /**
     * Initiates input retrieval. Must be overwritten in devices that support retrying input operation.
     *
     * @throws JposException Should never be thrown. Instead, the retryInput implementation should generate
     * an ErrorEvent whenever retryInput fails. The application should set ErrorResponse of that ErrorEvent
     * after some unsuccessful retries.
     */
    public void retryInput() throws JposException;

    /**
     * Initiates output retrieval. Must be overwritten in devices that support retrying output operations.
     *
     * @throws JposException Should never be thrown. Since retryOutput should only re-add the previously suspended
     * requests to the request queue and - if necessary - restart the JposOutputRequest handler, a JposException
     * should not be necessary.
     */
    public void retryOutput() throws JposException;

    /**
     * Will be called whenever DeviceEnabled will be set to true if power notification is enabled
     * (PowerNotify = PN_ENABLED). Should update the PowerState property. Further processing like firing a
     * StatusUpdateEvent is not necessary, this will be made by the framework automatically.
     *
     * @throws JposException If an error occurs.
     */
    public void handlePowerStateOnEnable() throws JposException;

    /**
     * Counts enqueued data events of subsystem unit specified by CurrentUnitID.
     * @return Number of enqueued data events from subsystem unit specified by CurrentUnitID.
     */
    public int unitDataCount();
}
