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

import jpos.JposConst;
import jpos.JposException;
import jpos.services.BaseService;
import jpos.services.EventCallbacks;
import net.bplaced.conrad.log4jpos.Level;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;

import static de.gmxhome.conrad.jpos.jpos_base.JposOutputRequest.JposRequestThread.ActiveMessageBoxes;

/**
 * Base class for all UPOS device services using this framework. Each service owns a
 * driver object derived from JposDevice and a device class specific property set
 * derived from JposCommonProperties.<p> Whenever accessing a property or method, the
 * service performs all plausibility checks that are possible without detailed knowledge
 * about a specific physical device. Whenever all parameters are plausible and a method
 * or setter call is generally allowed, a corresponding method of a class that implements the device specific interface
 * be called. This is almost always the correspondong property property set class which implements the corresponding
 * default methods. <p>These methods have the same name as the method or property name
 * originally called.<br>Interface methods corresponding to JavaPOS properties have one parameter -
 * the new property value. <br>Interface methods corresponding to JavaPOS methods that are always
 * be called synchronously have the same parameters as the original method.
 * <br>For JavaPOS methods that can be called asynchronously, two property set methods are available:
 * <ul>
 * <li>A validation method that has the same parameters as the original method. This method must return an object of a
 * method specific class derived from JposOutputRequest with the method's name.</li>
 * <li>An executor method that has one parameter - the object returned by the corresponding validation method.</li>
 * </ul>
 */
public class JposBase implements BaseService {
    /**
     * Property set that forms the device service together with class JposDevice and the JposBaseInterface, normally
     * implemented within the property set.
     */
    public JposCommonProperties Props;

    /**
     * Device object that holds the service implementation. Only used to access device object
     * directly instead of indirectly via the stored property set.
     */
    public JposDevice Device;

    /**
     * Instance of a class implementing the JposBaseInterface for common getter and method calls bound to the property
     * set. Almost always the same object as Props.
     */
    public JposBaseInterface DeviceInterface;

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public JposBase(JposCommonProperties props, JposDevice device) {
        Props = props;
        Device = device;
        MaximumConfirmationEventWaitingTime = device.MaximumConfirmationEventWaitingTime;
        StrictFIFOEventHandling = device.StrictFIFOEventHandling;
        device.StrictFIFOEventHandling = false;
        device.MaximumConfirmationEventWaitingTime = JposConst.JPOS_FOREVER;
    }

    /**
     * Maximum time in milliseconds an event callback may block event processing (default: FOREVER). Only conforms to
     * UPOS specification if FOREVER. lower values can lead to concurrent processing of event coroutines.
     */
    int MaximumConfirmationEventWaitingTime;

    /**
     * Specifies whether event handling conforms strictly to UPOS specification (all events handled in a first-in-first-out
     * mammer) or not (data and input error can be bypassed by other events as long as DataEventEnabled is false). Since
     * strict UPOS conformance is impractical, the default is false.
     */
    boolean StrictFIFOEventHandling;

    /**
     * Deletes the service instance. Called to perform cleanup operations.
     * @throws JposException If close() failed or if device cannot be found in list of all devices.
     */
    public void deleteInstance() throws JposException {
        logPreCall("DeleteInstance");
        if (Props.State != JposConst.JPOS_S_CLOSED)
            close();
        Device.removePropertySet(Props);
        synchronized (ActiveMessageBoxes) {
            for (SynchronizedMessageBox msg : ActiveMessageBoxes)
                msg.abortDialog();
        }
        synchronized(Device.AsyncProcessorRunning) {
            Device.PendingCommands.clear();
        }
        Props.EventSource = null;
        Props.Device = null;
        Props = null;
        Device = null;
        DeviceInterface = null;
    }

    /**
     * Generates logging message for the named property.
     * @param propertyName Name of the property.
     */
    public void logGet(String propertyName) {
        logGet(Props, propertyName);
    }

    /**
     * Generates logging message for the named property of the given object.
     * @param obj Object that holds the requested property.
     * @param propertyName Name of the property.
     */
    public void logGet(Object obj, String propertyName) {
        try {
            Device.log(Level.DEBUG, Props.LogicalName + ": " + propertyName + ": " + getPropertyString(obj, propertyName));
        } catch (Exception e) {
            Device.log(Level.DEBUG, Props.LogicalName + ": Cannot access property " + propertyName);
        }
    }

    /**
     * Returns a property value of an object as String, using its getter method. Usually, simply the toString method
     * of the specified object will be used to retrieve its string representation. If the property is an array, the
     * string representations of all elements stored within the array will be concatenated separated by comma. Keep
     * in mind that this might be confusing if the string representation of an element itself contains a comma, and
     * that at least MaxArrayStringElements will be retrieved. If an array has more than MaxArrayStringElements
     * elements, further elements will be represented by "...".
     * @param obj           Object that contains the requested property.
     * @param propertyName  Name of the requested property.
     * @return              String representation of the property.
     * @throws Exception    Getter not available.
     */
    public String getPropertyString(Object obj, String propertyName) throws Exception {
        Field  property;
        try {
            property = obj.getClass().getField(propertyName);
        } catch (Exception e) {
            (property = obj.getClass().getDeclaredField(propertyName)).setAccessible(true);
        }
        return removeOuterArraySpecifier(property.get(obj), Device.MaxArrayStringElements);
    }

    /**
     * Removes outer array specifiers '{' and '}' from string representation of an object created via deepToString. This
     * means, if a String returned by deepToString starts with '{' end ends with '}', both, the first and the last
     * character will be removed from the string.
     * @param object The object for which the String representation shall be returned.
     * @param maxlen Maximum array length, if object or a component of object is an array.
     * @return  String representation of object, in case of an array without the starting and final '{' and '}'.
     */
    public String removeOuterArraySpecifier(Object object, int maxlen) {
        String arraystring = deepToString(object, maxlen);
        if (arraystring.length() > 1 && arraystring.charAt(0) == '{' && arraystring.charAt(arraystring.length() - 1) == '}')
            return arraystring.substring(1, arraystring.length() - 1);
        return arraystring;
    }

    /**
     * Converts object to String. Uses method toString to convert the object to a String, but in case of Array objects,
     * a comma separated list of the toString results of the first <i>maxlen</i> elements of the array will be returned
     * instead.
     * @param value  The object to be converted to a string.
     * @param maxlen Maximum number of array elements converted to String. If an array consists of more than maxlen
     *               elements, the remaining elements will be represented by "...".
     * @return The String representation of value.
     */
    public String deepToString(Object value, int maxlen) {
        if (value == null)
            return "[null]";
        else if (!value.getClass().isArray())
            return value.toString();
        StringBuilder valueString = new StringBuilder("{");
        int max = Array.getLength(value);
        int limit = Math.min(max, maxlen);
        for (int i = 0; i < limit; i++) {
            Object component = Array.get(value, i);
            String componentString = deepToString(component, maxlen);
            if (0 != i)
                valueString.append(",");
            valueString.append(componentString);
        }
        if (limit < max)
            valueString.append(",...}");
        else
            valueString.append("}");
        return valueString.toString();
    }

    /**
     * Generates logging message before setting named property.
     * @param propertyName Name of property to be set.
     */
    public void logPreSet(String propertyName) {
        Device.log(Level.DEBUG, Props.LogicalName + ": Enter set" + propertyName + "...");
    }

    /**
     * Generates logging message after named property has been set
     * @param propertyName Name of property to be set.
     */
    public void logSet(String propertyName) {
        try {
            Device.log(Level.INFO, Props.LogicalName + ": " + propertyName + " <- " + getPropertyString(Props, propertyName));
        } catch (Exception e) {
            Device.log(Level.INFO, Props.LogicalName + ": Cannot access property " + propertyName);
        }
    }

    /**
     * Generates logging message before named method will be called.
     * @param method Method name.
     * @param args String specifying the parameters passed to the method.
     */
    public void logPreCall(String method, String args) {
        Device.log(Level.DEBUG, Props.LogicalName + ": Enter " + method + "(" + args + ")...");
    }

    /**
     * Generates logging message before named method will be called. Version that suppresses parameter dump.
     * @param method Method name.
     */
    public void logPreCall(String method) {
        Device.log(Level.DEBUG, Props.LogicalName + ": Enter " + method + "()...");
    }

    /**
     * Generate logging message after successful method call.
     * @param method Method name.
     * @param args   empty string or comma separated list of arguments.
     */
    public void logCall(String method, String args) {
        Device.log(Level.INFO, Props.LogicalName + ": " + method + "(" + args + ") successful.");
    }

    /**
     * Generate logging message after successful method call. Version without return parameter list.
     * @param method Method name.
     */
    public void logCall(String method) {
        Device.log(Level.INFO, Props.LogicalName + ": " + method + " successful.");
    }

    /**
     * Generate logging message after successful enqueueing an asynchronous method call.
     * @param method Method name.
     */
    public void logAsyncCall(String method) {
        Device.log(Level.INFO, Props.LogicalName + ": " + method + " enqueued successfully.");
    }

    /**
     * Call method bound to a specific output request synchronously or asynchronously, depending on AsyncMode property.
     * @param request JposOutputRequest to be enqueued
     * @return true if corresponding method will be called asynchronously, false in case of synchronous operation.
     * @throws JposException If an error occurs during synchronous operation.
     */
    public boolean callNowOrLater(JposOutputRequest request) throws JposException {
        if (request != null) {
            if (Props.AsyncMode) {
                request.enqueue();
                return true;
            }
            request.enqueueSynchronous();
            if (request.Exception != null)
                throw request.Exception;
        }
        return false;
    }

    /**
     * Get common property AutoDisable, see UPOS specification
     * @return property value
     * @throws JposException See UPOS specification, property AutoDisable
     */
    public boolean getAutoDisable() throws JposException {
        logGet("AutoDisable");
        checkOpened();
        return Props.AutoDisable;
    }

    /**
     * Set common property AutoDisable, see UPOS specification
     * @param b New property value
     * @throws JposException See UPOS specification, property AutoDisable
     */
    public void setAutoDisable(boolean b) throws JposException {
        logPreSet("AutoDisable");
        checkOpened();
        DeviceInterface.autoDisable(b);
        logSet("AutoDisable");
    }

    /**
     * Get common property DataCount, see UPOS specification
     * @return property value
     * @throws JposException See UPOS specification, property DataCount
     */
    public int getDataCount() throws JposException {
        logGet("DataCount");
        checkOpened();
        return Props.DataCount;
    }

    /**
     * Get common property DataEventEnabled, see UPOS specification
     * @return property value
     * @throws JposException See UPOS specification, property DataEventEnabled
     */
    public boolean getDataEventEnabled() throws JposException {
        logGet("DataEventEnabled");
        checkOpened();
        return Props.DataEventEnabled;
    }

    /**
     * Set common property DataEventEnabled, see UPOS specification
     * @param b New property value
     * @throws JposException See UPOS specification, property DataEventEnabled
     */
    public void setDataEventEnabled(boolean b) throws JposException {
        logPreSet("DataEventEnabled");
        checkOpened();
        DeviceInterface.dataEventEnabled(b);
        logSet("DataEventEnabled");
    }

    /**
     * Get common property CapCompareFirmwareVersion, see UPOS specification
     * @return property value
     * @throws JposException See UPOS specification, property CapCompareFirmwareVersion
     */
    public boolean getCapCompareFirmwareVersion() throws JposException {
        logGet(Device,"CapCompareFirmwareVersion");
        return Device.CapCompareFirmwareVersion;
    }

    /**
     * Get common property CapUpdateFirmware, see UPOS specification
     * @return property value
     * @throws JposException See UPOS specification, property CapUpdateFirmware
     */
    public boolean getCapUpdateFirmware() throws JposException {
        logGet(Device,"CapUpdateFirmware");
        return Device.CapUpdateFirmware;
    }

    /**
     * Get common property CapStatisticsReporting, see UPOS specification
     * @return property value
     * @throws JposException See UPOS specification, property CapStatisticsReporting
     */
    public boolean getCapStatisticsReporting() throws JposException {
        checkOpened();
        logGet("CapStatisticsReporting");
        return Props.CapStatisticsReporting;
    }

    /**
     * Get common property CapUpdateStatistics, see UPOS specification
     * @return property value
     * @throws JposException See UPOS specification, property CapUpdateStatistics
     */
    public boolean getCapUpdateStatistics() throws JposException {
        logGet("CapUpdateStatistics");
        checkOpened();
        return Props.CapUpdateStatistics;
    }

    /**
     * Get common property CapPowerReporting, see UPOS specification
     * @return property value
     * @throws JposException See UPOS specification, property CapPowerReporting
     */
    public int getCapPowerReporting() throws JposException {
        logGet(Device, "CapPowerReporting");
        checkOpened();
        return Device.CapPowerReporting;
    }

    /**
     * Get common property PowerNotify, see UPOS specification
     * @return property value
     * @throws JposException See UPOS specification, property PowerNotify
     */
    public int getPowerNotify() throws JposException {
        logGet("PowerNotify");
        checkOpened();
        return Props.PowerNotify;
    }

    /**
     * Set common property PowerNotify, see UPOS specification
     * @param powerNotify New property value
     * @throws JposException See UPOS specification, property PowerNotify
     */
    public void setPowerNotify(int powerNotify) throws JposException {
        logPreSet("PowerNotify");
        checkOpened();
        Device.check(Props.DeviceEnabled, JposConst.JPOS_E_ILLEGAL, "Device just enabled");
        Device.check(Props.Device.CapPowerReporting == JposConst.JPOS_PR_NONE && powerNotify != JposConst.JPOS_PN_DISABLED, JposConst.JPOS_E_ILLEGAL, "PowerReporting not supported");
        Device.checkMember(powerNotify, new long[]{JposConst.JPOS_PN_DISABLED, JposConst.JPOS_PN_ENABLED}, JposConst.JPOS_E_ILLEGAL, "Illegal value for PowerNotify");
        DeviceInterface.powerNotify(powerNotify);
        if (powerNotify == JposConst.JPOS_PN_DISABLED && Props.PowerState != JposConst.JPOS_PS_UNKNOWN) {
            Props.PowerState = JposConst.JPOS_PS_UNKNOWN;
            logSet("PowerState");
        }
        logSet("PowerNotify");
    }

    /**
     * Get common property CapCompareFirmwareVersion, see UPOS specification
     * @return property value
     * @throws JposException See UPOS specification, property
     */
    public int getPowerState() throws JposException {
        logGet("PowerState");
        checkOpened();
        return Props.PowerState;
    }

    /**
     * Get common property AsyncMode, see UPOS specification
     * @return property value
     * @throws JposException See UPOS specification, property
     */
    public boolean getAsyncMode() throws JposException {
        logGet("AsyncMode");
        checkOpened();
        return Props.AsyncMode;
    }

    /**
     * Get common property AsyncMode, see UPOS specification
     * @param b New property value
     * @throws JposException See UPOS specification, property PowerNotify
     */
    public void setAsyncMode(boolean b) throws JposException {
        logPreSet("AsyncMode");
        checkOpened();
        DeviceInterface.asyncMode(b);
        logSet("AsyncMode");
    }

    /**
     * Checks whether the device is enabled. If not, throws a JposException with an error code and message that describe
     * the state of the device.
     * @throws JposException Will be thrown whenever the device is not enabled.
     */
    public void checkEnabled() throws JposException {
        checkClaimed();
        Device.check(!Props.DeviceEnabled, JposConst.JPOS_E_DISABLED, "Device not enabled");
    }

    /**
     * Checks whether the device is enabled and not busy. If not, throws a JposException with an error code and message
     * that describe the state of the device.
     * @throws JposException Will be thrown whenever the device is not enabled or busy.
     */
    public void checkBusy() throws JposException {
        checkEnabled();
        Device.check(Props.State == JposConst.JPOS_S_BUSY, JposConst.JPOS_E_BUSY, "Device is busy");
    }

    /**
     * Checks whether the device is enabled and not claimed by another instance. If not, throws a JposException with an
     * error code and message that describe the state of the device.
     * @throws JposException Will be thrown whenever the device is not enabled.
     */
    public void checkEnabledUnclaimed() throws JposException {
        checkEnabled();
        JposCommonProperties claimer = Props.getClaimingInstance();
        Device.check(claimer != null && claimer != Props, JposConst.JPOS_E_CLAIMED, "Device claimed by other instance");
    }

    /**
     * Checks whether the device has just been enabled once. If not, throws a JposException with an error code and message
     * that describe the state of the device.
     * @throws JposException Will be thrown whenever the device has never been enabled.
     */
    public void checkFirstEnabled() throws JposException {
        checkOpened();
        Device.check(!Props.FirstEnableHappened, JposConst.JPOS_E_ILLEGAL, "Device never enabled");
    }

    /**
     * Checks whether the device has been claimed. If not, a JposException will be thrown with the corresponding error
     * code.
     * @throws JposException Will be thrown whenever the device has not been claimed.
     */
    public void checkClaimed() throws JposException {
        checkOpened();
        JposCommonProperties claiming = Props.getClaimingInstance();
        if (Props.ExclusiveUse == JposCommonProperties.ExclusiveYes) {
            JposDevice.check(claiming == null, JposConst.JPOS_E_NOTCLAIMED, "Device not claimed");
            JposDevice.check(claiming != Props, JposConst.JPOS_E_CLAIMED, "Device claimed by other instance");
        }
    }

    /**
     * Checks whether the device has been opened. If not, a JposException will be thrown with the corresponding error
     * code.
     * @throws JposException Will be thrown whenever the device has not been opened or has been closed.
     */
    public void checkOpened() throws JposException {
        Device.check(Props.State == JposConst.JPOS_S_CLOSED, JposConst.JPOS_E_CLOSED, "Device not opened");
    }

    /**
     * Checks within property set methods whether old and new values are equal or the device has been claimed. If the
     * device is an exclusive-use device and AllowAlwaysSetProperties has been set to false via jpos.xml, a
     * JposException will be thrown.
     *
     * @param oldval Property value to be changed.
     * @param newval New property value.
     * @throws JposException Will be thrown whenever the throwing condition is met.
     */
    public void checkNoChangedOrClaimed(Object oldval, Object newval) throws JposException {
        boolean change;
        if (oldval == null)
            change = newval != null;
        else if (oldval.getClass().isArray())
            change = !Arrays.equals((Object[]) oldval, (Object[]) newval);
        else
            change = !oldval.equals(newval);
        if (change && !Device.AllowAlwaysSetProperties && Props.ExclusiveUse == JposCommonProperties.ExclusiveYes)
            Device.check(!Props.Claimed, JposConst.JPOS_E_NOTCLAIMED, "Device not claimed");
    }

    /**
     * Check method for device classes that support a subsystem of up to 32 units.
     * Checks condition and if true, sets error properties and throws JposException. This method may only be used if
     * it is absolutely clear that the check is made within a synchronous UPOS method. If this is not the case, use
     * method <i>check</i>with additional parameter <i>synchrone</i>. This is almost always the case in methods that
     * have one parameter derived from JposOutputRequest. You can check whether property <i>EndSync</i> of the request
     * equals null (asynchronous call) or not (synchronous call).
     * @param condition Error condition.
     * @param units     Units to be filled in ErrorUnits.
     * @param error     Error code.
     * @param ext       Extended error code.
     * @param message   Error message, same message for ErrorString and JposException.
     * @throws JposException If Error condition is true.
     */
    public void check(boolean condition, int units, int error, int ext, String message) throws JposException {
        check(condition, units, error, ext, message, true);
    }

    /**
     * Check method for device classes that support a subsystem of up to 32 units.
     * Checks condition and if true, sets error properties and throws JposException.
     * @param condition Error condition.
     * @param units     Units to be filled in ErrorUnits.
     * @param error     Error code.
     * @param ext       Extended error code.
     * @param message   Error message, same message for ErrorString and JposException.
     * @param synchrone True if method has been called synchronously, false otherwise.
     * @throws JposException If Error condition is true.
     */
    public void check(boolean condition, int units, int error, int ext, String message, boolean synchrone) throws JposException {
        if (condition) {
            if (synchrone) {
                Props.ErrorUnits = units;
                logSet("ErrorUnits");
                Props.ErrorString = message;
                logSet("ErrorString");
            }
            else {
                Props.EventUnits = units;
                logSet("EventUnits");
                Props.EventString = message;
                logSet("EventString");
            }
            throw new JposException(error, ext, message);
        }
    }

    /**
     * Check method for device classes that support a subsystem of up to 32 units.
     * Check condition and if true, sets error properties and throws JposException. If the error cause
     * @param cause     The Exception that caused the error. A value of null can be passed if no error occurred.
     * @param units     Units to be filled in ErrorUnits.
     * @param error     Error code.
     * @param ext       Extended error code.
     * @param synchrone True if method has been called synchronously, false otherwise.
     * @throws JposException If Error condition is true.
     */
    public void check(Exception cause, int units, int error, int ext, boolean synchrone) throws JposException {
        if (cause != null) {
            if (synchrone) {
                Props.ErrorUnits = units;
                logSet("ErrorUnits");
                Props.ErrorString = cause.getMessage();
                logSet("ErrorString");
            }
            else {
                Props.EventUnits = units;
                logSet("EventUnits");
                Props.EventString = cause.getMessage();
                logSet("EventString");
            }
            if (cause instanceof JposException)
                throw (JposException) cause;
            throw new JposException(error, ext, cause.getMessage(), cause);
        }
    }

    /**
     * Get common property OutputID, see UPOS specification
     * @return property value
     * @throws JposException See UPOS specification, property
     */
    public int getOutputID() throws JposException {
        checkEnabled();
        logGet("OutputID");
        return Props.OutputID;
    }


    /**
     * Common method compareFirmwareVersion, see UPOS specification
     * @param firmwareFileName See UPOS specification, method compareFirmwareVersion
     * @param result See UPOS specification, method compareFirmwareVersion
     * @throws JposException See UPOS specification, method compareFirmwareVersion
     */
    public void compareFirmwareVersion(String firmwareFileName, int[] result) throws JposException {
        logPreCall("compareFirmwareVersion");
        checkEnabled();
        Device.check(!Device.CapCompareFirmwareVersion, JposConst.JPOS_E_ILLEGAL, "Device does not support compare firmware version");
        Device.check(firmwareFileName == null, JposConst.JPOS_E_ILLEGAL, "Missing firmwareFileName");
        Device.check(result == null || result.length <= 0, JposConst.JPOS_E_ILLEGAL, "Missing result");
        DeviceInterface.compareFirmwareVersion(firmwareFileName, result);
        logCall("compareFirmwareVersion", firmwareFileName + ", " + result[0]);
    }

    /**
     * Common method updateFirmware, see UPOS specification
     * @param firmwareFileName See UPOS specification, method updateFirmware
     * @throws JposException See UPOS specification, method updateFirmware
     */
    public void updateFirmware(String firmwareFileName) throws JposException {
        logPreCall("UpdateFirmware", firmwareFileName == null ? "" : firmwareFileName);
        checkEnabled();
        Device.check(!Device.CapUpdateFirmware, JposConst.JPOS_E_ILLEGAL, "Device does not support update firmware");
        Device.check(firmwareFileName == null, JposConst.JPOS_E_ILLEGAL, "Missing firmwareFileName");
        UpdateFirmware request = DeviceInterface.updateFirmware(firmwareFileName);
        if (request != null) {
            request.enqueue();
            logAsyncCall("UpdateFirmware");
        }
        else
            logCall("UpdateFirmware");
    }

    /**
     * Common method resetStatistics, see UPOS specification
     * @param statisticsBuffer See UPOS specification, method resetStatistics
     * @throws JposException See UPOS specification, method resetStatistics
     */
    public void resetStatistics(String statisticsBuffer) throws JposException {
        if (statisticsBuffer == null)
            statisticsBuffer = "";
        logPreCall("ResetStatistics");
        checkEnabled();
        Device.check(!Props.CapUpdateStatistics || !Props.CapStatisticsReporting, JposConst.JPOS_E_ILLEGAL, "Device does not support resetting statistics");
        DeviceInterface.resetStatistics(statisticsBuffer);
        logPreCall("ResetStatistics", statisticsBuffer);
    }

    /**
     * Common method retrieveStatistics, see UPOS specification
     * @param statisticsBuffer See UPOS specification, method retrieveStatistics
     * @throws JposException See UPOS specification, method retrieveStatistics
     */
    public void retrieveStatistics(String[] statisticsBuffer) throws JposException {
        if (statisticsBuffer != null && statisticsBuffer[0] == null)
            statisticsBuffer[0] = "";
        logPreCall("RetrieveStatistics", statisticsBuffer == null ? "" : statisticsBuffer[0]);
        checkEnabled();
        Device.check(!Props.CapStatisticsReporting, JposConst.JPOS_E_ILLEGAL, "Device does not support retrieving statistics");
        Device.check(statisticsBuffer == null, JposConst.JPOS_E_ILLEGAL, "Missing statisticsBuffer");
        DeviceInterface.retrieveStatistics(statisticsBuffer);
        logCall("RetrieveStatistics", statisticsBuffer[0]);
    }

    /**
     * Common method updateStatistics, see UPOS specification
     * @param statisticsBuffer See UPOS specification, method updateStatistics
     * @throws JposException See UPOS specification, method updateStatistics
     */
    public void updateStatistics(String statisticsBuffer) throws JposException {
        if (statisticsBuffer == null)
            statisticsBuffer = "";
        logPreCall("UpdateStatistics", statisticsBuffer);
        checkEnabled();
        Device.check(!Props.CapUpdateStatistics || !Props.CapStatisticsReporting, JposConst.JPOS_E_ILLEGAL, "Device does not support updating statistics");
        DeviceInterface.updateStatistics(statisticsBuffer);
        logCall("UpdateStatistics");
    }

    /**
     * Common method clearOutput, se UPOS specification
     * @throws JposException See UPOS specification, method clearOutput
     */
    public void clearOutput() throws JposException {
        logPreCall("ClearOutput");
        checkClaimed();
        DeviceInterface.clearOutput();
        logCall("ClearOutput");
        logSet("State");
    }

    /**
     * Common method clearInput, see UPOS specification
     * @throws JposException see UPOS specification
     */
    public void clearInput() throws JposException {
        logPreCall("ClearInput");
        checkClaimed();
        DeviceInterface.clearInput();
        logCall("ClearInput");
    }

    /**
     * Common method clearInputProperties, see UPOS specification
     * @throws JposException see UPOS specification
     */
    public void clearInputProperties() throws JposException {
        logPreCall("ClearInputProperties");
        checkClaimed();
        Props.clearDataProperties();
        logCall("ClearInputProperties");
    }

    @Override
    public String getCheckHealthText() throws JposException {
        Device.check(Props.CheckHealthText == null, JposConst.JPOS_E_CLOSED, "Device not opened");
        logGet("CheckHealthText");
        return Props.CheckHealthText;
    }

    @Override
    public boolean getClaimed() throws JposException {
        checkOpened();
        logGet("Claimed");
        return Props.Claimed;
    }

    @Override
    public boolean getDeviceEnabled() throws JposException {
        checkOpened();
        logGet("DeviceEnabled");
        return Props.DeviceEnabled;
    }

    @Override
    public void setDeviceEnabled(boolean enable) throws JposException {
        logPreSet("DeviceEnabled");
        checkClaimed();
        Device.check(!Props.DeviceEnabled && !enable, JposConst.JPOS_E_DISABLED, "Device just disabled");
        Device.check(Props.DeviceEnabled && enable, JposConst.JPOS_E_ILLEGAL, "Device just enabled");
        DeviceInterface.deviceEnabled(enable);
        logSet("DeviceEnabled");
        if (Props.PowerNotify == JposConst.JPOS_PN_ENABLED) {
            if (enable)
                DeviceInterface.handlePowerStateOnEnable();
            else {
                Props.PowerState = JposConst.JPOS_PS_UNKNOWN;
                logSet("PowerState");
            }
        }
    }

    @Override
    public String getDeviceServiceDescription() throws JposException {
        Device.check(Props.DeviceServiceDescription == null, JposConst.JPOS_E_CLOSED, "Device not opened");
        logGet("DeviceServiceDescription");
        return Props.DeviceServiceDescription;
    }

    @Override
    public int getDeviceServiceVersion() throws JposException {
        checkOpened();
        logGet("DeviceServiceVersion");
        return Props.DeviceServiceVersion;
    }

    @Override
    public boolean getFreezeEvents() throws JposException {
        checkOpened();
        logGet("FreezeEvents");
        return Props.FreezeEvents;
    }

    @Override
    public void setFreezeEvents(boolean freezeEvents) throws JposException {
        logPreSet("FreezeEvents");
        checkOpened();
        DeviceInterface.freezeEvents(freezeEvents);
        logSet("FreezeEvents");
    }

    @Override
    public String getPhysicalDeviceDescription() throws JposException {
        logGet(Device, "PhysicalDeviceDescription");
        return Device.PhysicalDeviceDescription;
    }

    @Override
    public String getPhysicalDeviceName() throws JposException {
        logGet(Device, "PhysicalDeviceName");
        return Device.PhysicalDeviceName;
    }

    @Override
    public int getState() throws JposException {
        logGet("State");
        return Props.State;
    }

    @Override
    public void claim(int timeout) throws JposException {
        logPreCall("Claim", "" + timeout);
        checkOpened();
        JposDevice.check(Props.Claimed, JposConst.JPOS_E_CLAIMED, "Device just claimed");
        JposDevice.check(Props.ExclusiveUse == JposCommonProperties.ExclusiveNo, JposConst.JPOS_E_ILLEGAL, "Device always shareable");
        JposDevice.check(timeout != JposConst.JPOS_FOREVER && timeout < 0, JposConst.JPOS_E_ILLEGAL, "Invalid timeout value");
        long start = System.currentTimeMillis();
        JposCommonProperties props;
        while (true) {
            SyncObject waiter = new SyncObject();
            props = startClaiming(waiter);
            if (props == null) break;
            if (props != null) {
                if (timeout == JposConst.JPOS_FOREVER || (timeout > 0 && System.currentTimeMillis() - start < timeout)) {
                    waiter.suspend(timeout == JposConst.JPOS_FOREVER ? SyncObject.INFINITE : timeout);
                } else
                    throw new JposException(JposConst.JPOS_E_TIMEOUT, "Claim timed out");
            }
        }
        try {
            DeviceInterface.claim(timeout);
        } catch (JposException e) {
            signalRelease();
            throw e;
        }
        logCall("Claim");
    }

    /**
     * Starts claiming. Should be called whenever a device tries to claim a device.
     * <br>Returns null, if the device is not claimed. Otherwise, the given SyncObject instance will be
     * inserted into the ClaimWaiters list of the claiming instance. Since this object will be signalled
     * during release or close of that instance, it should be used to wait before retrying start claiming.
     * @param waiter    SyncOject to be used for synchronization with currently claiming instance, if any.
     * @return          null on success, property set of currently claiming instance otherwise.
     */
    public JposCommonProperties startClaiming(SyncObject waiter) {
        JposCommonProperties props;
        synchronized (Props.Claiming) {
            if ((props = Props.Claiming[Props.Index]) == null) {
                Props.Claiming[Props.Index] = Props;
                return null;
            } else {
                props.ClaimWaiters.add(waiter);
            }
        }
        return props;
    }

    /**
     * Signals release after claim. Should be called whenever a claimed device becomes unclaimed to wake up any other
     * waiting instances.
     */
    public void signalRelease() {
        synchronized (Props.Claiming) {
            Props.Claiming[Props.Index] = null;
        }
        for (SyncObject waiter : Props.ClaimWaiters)
            waiter.signal();
        Props.ClaimWaiters.clear();
    }

    @Override
    public void close() throws JposException {
        logPreCall("Close");
        if (Props.DeviceEnabled)
            setDeviceEnabled(false);
        if (Props.Claimed)
            release();
        DeviceInterface.close();
        logCall("Close");
    }

    @Override
    public void checkHealth(int level) throws JposException {
        logPreCall("CheckHealth", "" + level);
        checkEnabled();
        DeviceInterface.checkHealth(level);
        logCall("CheckHealth");
    }

    @Override
    public void directIO(int command, int[] data, Object object) throws JposException {
        logPreCall("DirectIO", removeOuterArraySpecifier(new Object[]{command, data, object}, Device.MaxArrayStringElements));
        JposDevice.check(data != null && data.length != 1, JposConst.JPOS_E_ILLEGAL, "Data invalid, must be int[1]: " + deepToString(data, 5));
        DirectIO request = DeviceInterface.directIO(command, data, object);
        if (request != null) {
            request.enqueue();
            logAsyncCall("DirectIO");
            return;
        }
        logCall("DirectIO", removeOuterArraySpecifier(new Object[]{command, data, object}, Device.MaxArrayStringElements));
    }

    @Override
    public void open(String logicalName, EventCallbacks eventCallbacks) throws JposException {
        Device.log(Level.DEBUG, logicalName + ": Enter Open()...");
        Props.LogicalName = logicalName;
        Props.EventCB = eventCallbacks;
        try {
            DeviceInterface.open();
        } catch(Exception e) {
            deleteInstance();
            if (e instanceof JposException)
                throw e;
            throw new JposException(JposConst.JPOS_E_NOSERVICE, e.getMessage(), e);
        }
        logCall("Open");
    }

    @Override
    public void release() throws JposException {
        logPreCall("Release");
        checkOpened();
        Device.check(!Props.Claimed, JposConst.JPOS_E_NOTCLAIMED, "Device not claimed");
        if (Props.DeviceEnabled && Props.ExclusiveUse == JposCommonProperties.ExclusiveYes)
            setDeviceEnabled(false);
        DeviceInterface.release();
        signalRelease();
        logCall("Release");
    }
}
