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

import jpos.*;
import jpos.config.*;
import jpos.events.*;

import java.lang.reflect.Constructor;
import java.util.*;

import jpos.services.*;
import net.bplaced.conrad.log4jpos.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties.ExclusiveNo;
import static de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties.ExclusiveYes;
import static de.gmxhome.conrad.jpos.jpos_base.JposDeviceFactory.CurrentEntry;
import static de.gmxhome.conrad.jpos.jpos_base.SyncObject.INFINITE;
import static jpos.JposConst.*;
import static net.bplaced.conrad.log4jpos.Level.*;

/**
 * Base class for device driver implementation based on JPOS framework.<br>
 * Implements event handling, methods for plausibility checks, properties
 * that are common for all device instances, logging methods and some general
 * helper methods.<br>
 * Device specific properties will be specified in property sets stored in
 * device type specific classes named <i>DeviceType</i>Properties or derived
 * classes.<p>
 * Property getters are implemented directly within the
 * corresponding <i>DeviceType</i>Service class.<br>
 * Final parts of setters and methods have been implemented in classes that implement <i>DeviceType</i>Interface,
 * an interface derived from interface JposBaseInterface.
 * A Default implementation implementing basic functionality that is always necessary and that can be
 * used for derived classes has been made within the <i>DeviceType</i>Properties classes.<br>
 * Methods that can me performed asynchronously are split into two parts: A validation part returning a method specific
 * object derived from class <i>MethodName</i> (where
 * <i>methodName</i> is the name of the corresponding method) and the real final part with the same method name, but
 * with only the object returned by the corresponding validation part as parameter.<br>
 * Common properties, methods and setters have been defined within <i>JposCommonProperties</i> which implements JposBaseInterface.
 * An instance of a <i>DeviceType</i>Interface as well as the corresponding JposBaseInterface will be stored in each
 * property set in the member variables DeviceInterface and <i>DeviceType</i>Interface.<br>
 * The specific interface methods and setters will be
 * called by the corresponding setter or method implementation of the corresponding
 * <i>DeviceType</i>Service class after all plausibility checks have been performed. Therefore, only
 * physical device specific checks must be performed within these methods.<p>
 * Setter implementations have the same names as the corresponding UPOS property.
 * One parameter, the new property value, will be passed.<br>
 * Method implementations have the same name as the corresponding UPOS method. In case
 * of methods that can only performed synchronously, they have the same parameters as the
 * corresponding UPOS method. Methods that can be performed asynchronously as well are only for validation and return
 * an object derived from the class with the name of the UPOS method on success (such classes are always derived from
 * JposOutputRequest). The real final part must be implemented
 * in a method with the same name but with the object returned by the validation part as only parameter.
 * Keep in mind that the time this method is called is always the time
 * at the processing shall start. This is typically within the output request worker
 * thread.
 * <p>Here a full list of all properties, common for all device classes, that can be changed via jpos.xml:
 * <ul>
 *     <li>AllowAlwaysSetProperties: For all exclusive-use devices where UPOS specifies properties with read-write access
 *     before the device has been opened, This property specifies whether write access will be suppressed by the service
 *     (false) or not (true) until the device has been claimed. The default must be set within the constructor of a
 *     device derived from JposDevice.
 *     <br>However, even if set, the individual service implementation can decide to throw a JposException whenever the
 *     application tries to change a writable property until the device has been claimed, but only if not set, the service
 *     method <i>checkOpenNotClaimed</i> will throw the JposException without calling the device specific set method.</li>
 *     <li>DrawerBeepVolume: For all CashDrawer devices, a beep volume between 0 and 127 can be specified. Default is
 *     implementation specific.</li>
 *     <li>LoggerName: Name of the logger. See Log4j specification for more details.</li>
 *     <li>LoggerFormat: Layout pattern, if not set, SimpleLayout will be used. See Log4j specification for more details.</li>
 *     <li>LogFilePath: Path of log file.</li>
 *     <li>LogFilePattern: If set, file pattern for DailyRollingFileAppender, otherwise FileAppender will be used.
 *     See Log4j specification for more details.</li>
 *     <li>LogLevel: One of <b>all</b>, <b>trace</b>, <b>debug</b>, <b>info</b>, <b>warn</b>, <b>error</b>, <b>fatal</b>
 *     or <b>off</b>. Specifies the logging level. See Log4pos specification for more details.</li>
 *     <li>MaxArrayStringElements: Specifies the maximum number of elements of an object to be logged that are fully logged.
 *     If an object to be logged has more elements, the remaining elements will be logged as "...".</li>
 *     <li>MaximumConfirmationEventWaitingTime: Specifies the maximum time an event callback may block event handling.
 *     Default: FOREVER. <b>KEEP IN MIND:</b> Setting this property to a different value leads to a service that does
 *     not fully fulfill the requirements of the UPOS specification: Running more than one event handler callback
 *     becomes possible.</li>
 *     <li>SerialIOAdapterClass: Name of the SerialIOAdapter class. No default, must be set if serial communication shall
 *     be used. The following adapter classes have been implemented:
 *     <ul>
 *         <li>de.gmxhome.conrad.JNAWindows.JnaSerial: Serial IO implementation using WIN32 API via JNA.</li>
 *         <li>de.gmxhome.conrad.JNALinux.JnaLinuxSerial: Serial IO implementation using Linux API via JNA.</li>
 *         <li>de.gmxhome.conrad.jSerialComm.JSCSerial: Serial IO implementation using jSerialComm framework.</li>
 *         <li>de.gmxhome.conrad.jSSC.JSSCSerial: Serial IO implementation using jSSC framework.</li>
 *     </ul>
 *     </li>
 *     <li>StrictFIFOEventHandling: If true, all events will be delivered in the same sequence as they have been fired.
 *     If false, data end input error events can be bypassed by other events as long as DataEventEnabled and
 *     FreezeEvents are false. Default is false.</li>
 * </ul>
 */
public class JposBaseDevice {
    /*
     * Properties and default values that are common for all UPOS devices represented by a single
     * physical device.
     */
    /**
     * UPOS property PhysicalDeviceDescription. Default: "Default implementation for any device". For more details, see UPOS specification, chapter Common Properties,
     * Methods and Events.
     */
    public String PhysicalDeviceDescription = "Default implementation for any device";

    /**
     * UPOS property PhysicalDeviceName. Default: "Any Device". For more details, see UPOS specification, chapter Common Properties,
     * Methods and Events.
     */
    public String PhysicalDeviceName = "Any Device";

    /**
     * UPOS property CapCompareFirmwareVersion. Default: false. For more details, see UPOS specification, chapter Common Properties,
     * Methods and Events.
     */
    public boolean CapCompareFirmwareVersion = false;

    /**
     * UPOS property CapUpdateFirmware. Default: false. For more details, see UPOS specification, chapter Common Properties,
     * Methods and Events.
     */
    public boolean CapUpdateFirmware = false;

    /**
     * UPOS property CapPowerReporting. Default: PR_NONE. For more details, see UPOS specification, chapter Common Properties,
     * Methods and Events.
     */
    public int CapPowerReporting = JPOS_PR_NONE;

    /**
     * Device identifier, specifies the interface the device is connected. For example, COM port name
     * or IP address, (COM1, 192.168.1.150, ...).
     */
    protected String ID;

    /**
     * Logger object.
     */
    public Logger Log;

    /**
     * Logger name.
     */
    public String LoggerName;

    /**
     * Path of log file.
     */
    public String LogFilePath;

    /**
     * File pattern of log file. Example: ".YYYY-MM"
     */
    public String LogFilePattern;

    /**
     * Logging level. Example: Level.DEBUG, see Log4j specification
     */
    public Level LogLevel;

    /**
     * Format string for logging line. See Log4j specification
     */
    public String LoggerFormat;

    /**
     * Maximum number of array elements used for its string representation. Default: 100. Can be changed via jpos.xml.
     */
    public int MaxArrayStringElements = 100;

    /**
     * List holds all outstanding output requests.
     */
    public List<JposOutputRequest> PendingCommands = new ArrayList<>();

    /**
     * Flag that signals whether the processor for asynchronous commands is running. Used for synchronization purposes.
     */
    public final JposOutputRequest.JposRequestThread[] AsyncProcessorRunning = {null};

    /**
     * Currently executed output request, if concurrent asynchronous requests are not supported.
     */
    public JposOutputRequest CurrentCommand;

    /**
     * Invokes a JposRequestThread for handling the specified request. If an immediate class has been specified (not null),
     * it specifies a high priority class. Requests of a high priority class will be inserted before the first request
     * with normal priority (immediate requests). All other requests will be appended at the end of the request queue.<br>
     * After insertion, the request handler thread will be created and started if it is currently not running.
     * @param request   Request to be added. If null, only handler thread invocation, if necessary.
     * @param immediate If specified, class with high priority.
     */
    public void invokeRequestThread(JposOutputRequest request, Class<?> immediate) {
        if (immediate != null) {
            int index;
            for (index = 0; index < PendingCommands.size(); index++) {
                if (!immediate.isInstance(PendingCommands.get(index)))
                    break;
            }
            PendingCommands.add(index, request);
        } else
            PendingCommands.add(request);
        if (AsyncProcessorRunning[0] == null) {
            (AsyncProcessorRunning[0] = new JposOutputRequest.JposRequestThread(request, request)).start();
        }
    }

    /**
     * Method with checks whether the device supports concurrent processing for the given request. This default implementation
     * returns always false. You must override this method in derived classes to allow specific requests to be invoked
     * concurrently.
     *
     * @param request Request to be checked.
     * @return  true if the request can be processed concurrently. Otherwise, false will be returned if synchronization
     * with other requests of the device is necessary. If only serialization with other non-concurrent requests of the
     * same service is necessary, null can be returned.
     */
    public Boolean concurrentProcessingSupported(JposOutputRequest request) {
        return false;
    }

    /**
     * Starts a new thread for concurrent asynchronous processing and adds it to the CurrentCommands list of the
     * property set the request belongs to.
     * @param request   Request to be invoked.
     */
    public void createConcurrentRequestThread(JposOutputRequest request) {
        request.Props.CurrentCommands.add(request);
        RequestRunner runner = new RequestRunner(request);
        new JposOutputRequest.JposRequestThread(runner, request).start();
    }

    /**
     * Retrieves JposOutputRequest belonging to a RequestRunner object or null, if the given Runnable is no RequestRunner
     * @param runner    Runnable, almost always a RequestRunner.
     * @return          runner.Request, if runner is an instance of RequestRunner, otherwise null.
     */
    public JposOutputRequest getRequestRunnersRequest(Runnable runner) {
        return runner instanceof RequestRunner ? ((RequestRunner) runner).Request : null;
    }

    /**
     * Class for concurrent asynchronous output processing support.
     */
    class RequestRunner implements Runnable {
        /**
         * Request to be handled by the thread using this Runnable.
         */
        JposOutputRequest Request;

        /**
         * Specifies whether the request must be serialised within the service specifies by its property set (true)
         * or can run independent of all other requests.
         */
        boolean Serialized;

        /**
         * Thread to be used to perform an input or output request (asynchronously). Used whenever a device supports
         * concurrent asynchronous processing.
         * @param request request to be executed using this thread.
         */
        RequestRunner(JposOutputRequest request) {
            Request = request;
            Serialized = concurrentProcessingSupported(request) == null;
            if (Serialized) {
                if (request.Props.SerializedRequestRunner == null)
                    request.Props.SerializedRequestRunner = this;
                else
                    request.Props.SerializedRequests.add(this);
            }
        }

        @Override
        public void run() {
            if (Serialized) {
                boolean wait;
                synchronized (AsyncProcessorRunning) {
                    wait = Request.Props.SerializedRequestRunner != this;
                }
                if (wait)
                    Request.Waiting.suspend(INFINITE);
            }
            if (Request.Abort == null)
                Request.catchedInvocation();
            boolean processed = Request.finishAsyncProcessing();
            synchronized (AsyncProcessorRunning) {
                if (processed)
                    Request.Props.CurrentCommands.remove(Request);
                if (Request.Props.SerializedRequestRunner == this) {
                    if (Request.Props.SerializedRequests.size() > 0) {
                        Request.Props.SerializedRequestRunner = Request.Props.SerializedRequests.get(0);
                        getRequestRunnersRequest(Request.Props.SerializedRequestRunner).Waiting.signal();
                        Request.Props.SerializedRequests.remove(0);
                    } else
                        Request.Props.SerializedRequestRunner = null;
                }
            }
        }
    }

    /**
     * Constructor. Initialize ID. Derived classes must allocate list of list of property sets
     * for all supported device types.
     *
     * @param id Device ID, typically a unique identifier, like the COM port, e.g. "COM1"
     */
    protected JposBaseDevice(String id) {
        ID = id;
    }

    /**
     * Get count of device type specific property sets stored within a device driver.
     *
     * @param device List of list to be processed
     * @return No. of non-null property set objects stored within list of list
     */
    public static int getCount(List<JposCommonProperties>[] device) {
        int count = 0;

        for (List<JposCommonProperties> jposCommonProperties : device) {
            for (JposCommonProperties jposCommonProperty : jposCommonProperties) {
                if (jposCommonProperty != null) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Show message dialog within swing thread + synchronization. Useful in interactive CheckHealth methods
     *
     * @param message     see JoptionPane.showMessageBox
     * @param title       see JoptionPane.showMessageBox
     * @param messageType see JoptionPane.showMessageBox
     */
    static public void synchronizedMessageBox(final String message, final String title, final int messageType) {
        SynchronizedMessageBox box = new SynchronizedMessageBox();
        box.synchronizedConfirmationBox(message, title, null, null, messageType, JPOS_FOREVER);
    }

    /**
     * Checks whether the given condition it met. If so, throws
     * a JposException with the given error code and error description
     *
     * @param condition Error condition
     * @param err       Jpos error code
     * @param errtxt    Corresponding error text
     * @throws JposException Will be thrown if condition is true
     */
    static public void check(boolean condition, int err, String errtxt) throws JposException {
        if (condition) {
            throw new JposException(err, errtxt);
        }
    }

    /**
     * Checks whether the given condition it met. If so, throws
     * a JposException with JPOS_E_EXTENDED and the given extended error code and error description
     *
     * @param condition Error condition
     * @param ext       Jpos extended error code
     * @param errtxt    Corresponding error text
     * @throws JposException Will be thrown if condition is true
     */
    static public void checkext(boolean condition, int ext, String errtxt) throws JposException {
        if (condition) {
            throw new JposException(JPOS_E_EXTENDED, ext, errtxt);
        }
    }

    /**
     * Checks whether the given value is member of the given set of allowed values.
     * Throws JposException if given value does not match any value in allowed value set.
     *
     * @param value         Value to be checked
     * @param allowedValues Set of allowed values
     * @param err           JPOS error code
     * @param errtxt        Error text
     * @throws JposException will be thrown if value does not match any value in allowedValues
     */
    static public void checkMember(long value, long[] allowedValues, int err, String errtxt) throws JposException {
        if (!member(value, allowedValues))
            throw new JposException(err, errtxt);
    }

    /**
     * Checks whether the given value is member of the given set of allowed values.
     *
     * @param value         Value to be checked
     * @param allowedValues Set of allowed values
     * @return true if value is containaed in allowedValues
     */
    static public boolean member(long value, long[] allowedValues) {
        for (long val : allowedValues) {
            if (val == value)
                return true;
        }
        return false;
    }

    /**
     * Checks whether the given value is member of the given set of allowed values.
     *
     * @param value         Value to be checked
     * @param allowedValues Set of allowed values
     * @return true if value is containaed in allowedValues
     */
    public static boolean member(String value, String[] allowedValues) {
        for (String val : allowedValues) {
            if (val.equals(value))
                return true;
        }
        return false;
    }

    /**
     * Converts string array that contains integer values into long[].
     *
     * @param stringarray String array containing long integer values.
     * @return Array of long values containing the values stored in stringarray.
     */
    static public long[] stringArrayToLongArray(String[] stringarray) {
        long[] result = new long[stringarray.length];
        for (int i = result.length - 1; i >= 0; i--) {
            result[i] = Integer.parseInt(stringarray[i]);
        }
        return result;
    }

    /**
     * Checks whether the given value is out of range. Throws JposException if
     * given value is not between minimum and maximum value.
     *
     * @param value  Value to be checked
     * @param min    Minimum allowed value
     * @param max    Maximum allowed value
     * @param err    JPOS error code
     * @param errtxt Error text
     * @throws JposException will be thrown if not min &le; value &le; max
     */
    static public void checkRange(long value, long min, long max, int err, String errtxt) throws JposException {
        if (value < min || value > max)
            throw new JposException(err, errtxt);
    }

    /**
     * Delay current thread for the given time period.
     *
     * @param millis Number of milliseconds the thread shall be delayed.
     */
    @SuppressWarnings("BusyWait")
    static public void delay(int millis) {
        long startingTime = System.currentTimeMillis();
        long deltaTime;
        while ((deltaTime = System.currentTimeMillis() - startingTime) < millis) {
            try {
                Thread.sleep(millis - deltaTime);
            } catch (InterruptedException ignored) {}
        }
    }

    // Object used for synchronization purposes.
    private final Byte[] LoggerSync = {0};

    /**
     * Performs logging. Includes automatic logger initialization whenever necessary and possible.
     * However, if logger intialization is not possible due to missing entries in jpos.xml, no logging
     * will be made.
     *
     * @param loglevel Logging loglevel, any Level object from log4j, log4jpos or java.util.logging
     * @param message  Message to be logged
     */
    public void log(Object loglevel, String message) {
        Level level;
        level = loglevel instanceof Level ? (Level) loglevel : getLog4posLevel(loglevel);
        synchronized (LoggerSync) {
            if (Log == null && LogLevel != OFF) {
                Log = Logger.getLogger(LoggerName);
                Log.setLevel(LogLevel);
                try {
                    Layout layout;
                    if (LoggerFormat != null) {
                        layout = new PatternLayout(LoggerFormat);
                    } else {
                        layout = new SimpleLayout();
                    }
                    if (LogFilePattern != null) {
                        Log.addAppender(new DailyRollingFileAppender(layout, LogFilePath, LogFilePattern));
                    } else {
                        Log.addAppender(new FileAppender(layout, LogFilePath));
                    }
                } catch (Exception e) {
                    Log = null;
                }
            }
        }
        if (Log != null) {
            Log.log(level, message);
        }
    }

    // Convert log4j-Level and java.util.logging-Level to log4pos-Level
    private Level getLog4posLevel(Object loglevel) {
        if (loglevel instanceof java.util.logging.Level) {
            int level = ((java.util.logging.Level)loglevel).intValue();
            if (level == java.util.logging.Level.ALL.intValue())
                return ALL;
            if (level <= java.util.logging.Level.FINE.intValue())
                return TRACE;
            if (level <= java.util.logging.Level.CONFIG.intValue())
                return DEBUG;
            if (level <= java.util.logging.Level.INFO.intValue())
                return INFO;
            if (level <= java.util.logging.Level.WARNING.intValue())
                return WARN;
            if (level <= java.util.logging.Level.SEVERE.intValue())
                return ERROR;
            if (level <= java.util.logging.Level.SEVERE.intValue() + 100)
                return FATAL;
            return OFF;
        }
        String classname = loglevel.getClass().getName();
        if (classname.equals("org.apache.log4j.Level")) {
            if (loglevel.toString().equals("FATAL"))
                return FATAL;
            if (loglevel.toString().equals("ERROR"))
                return ERROR;
            if (loglevel.toString().equals("WARN"))
                return WARN;
            if (loglevel.toString().equals("INFO"))
                return INFO;
            if (loglevel.toString().equals("DEBUG"))
                return DEBUG;
            if (loglevel.toString().equals("TRACE"))
                return TRACE;
            if (loglevel.toString().equals("ALL"))
                return ALL;
            return OFF;
        }
        return null;
    }

    /**
     * Volume for drawer beep. Valid values are from 0 to 127. Will be initialized with an Integer object only for
     * devices that support at least one CashDrawer device.
     * Deprecated, the corresponding property in a device class specific property set will be set within the device
     * class specific checkProperties method called by the addDevice method of the specific factory class.
     */
    @Deprecated
    public Integer DrawerBeepVolume = null;

    /**
     * Temporary storage for jpos.xml property AllowAlwaysSetProperties. Specifies how the service instance shall handle
     * write access to writable properties while the device has not been claimed for exclusive-use devices:
     * <br>If true, setting such properties will remain possible and it is up to the service how to handle the new value.
     * Since no physical access to the device is allowed until the device has been claimed, the service can buffer the
     * new value for later use after a successful call of method claim.
     * <br>If false, only read access will be possible until the device has been claimed. If the application tries to
     * change a writable property, a JposException will be thrown with error code E_NOTCLAIMED.
     * The default value is true. Method <i>checkProperties</i> will be used to change it to the value specified in
     * jpos.xml, if specified, and method <i>changeDefaults</i> will be used to copy this value to the corresponding
     * property set and to reset it back to its default.
     * Deprecated. Use the corresponding property within the device class specific property set object.
     */
    @Deprecated
    public boolean AllowAlwaysSetProperties = true;

    /**
     * Holds the JavaPOS control version as specified in jpos.xml. Can be used in changeDefaults method of the corresponding
     * logical device. Default: null (not set). Will be reset to null within method changeDefaults of JposDevice. Every
     * service implementation that likes to use that value must save it within its own changeDefaults method before calling
     * super.changeDefaults.<br>
     * Deprecated. Use DeviceServiceVersion instead.
     */
    @Deprecated
    public Integer JposVersion = null;

    /**
     * Name of the SerialIOAdapter class. No default, must be set in device specific implementation if serial
     * communication shall be supported.
     */
    public static String SerialIOAdapterClass = null;

    /**
     * Maximun number of milliseconds the event handling is delayed after delivery of events that need confirmation.
     * Default is JPOS_FOREVER (unlimited number of seconds). Deprecated: Use MaximumConfirmationEventWaitingTime from
     * property set instead.
     */
    @Deprecated
    public int MaximumConfirmationEventWaitingTime = JPOS_FOREVER;

    /**
     * Specifies whether event handling strictly confirms the UPOS specification or if data and input error events can
     * be delivered delayed while DataEventEnabled is false. Default is false (data and input error events do not block
     * other events while DataEventEnabled is false). Deprecated: Use StrictFIFOEventHandling from property set instead.
     */
    @Deprecated
    public boolean StrictFIFOEventHandling = false;

    /**
     * Checks whether a JposEntry belongs to a predefined property value an if so,
     * sets the corresponding driver value
     *
     * @param entry Entry to be checked, contains value to be set
     * @throws JposException if a property value is invalid
     */
    @SuppressWarnings("deprecation")
    public void checkProperties(JposEntry entry) throws JposException {
        try {
            Object o;
            if ((o = entry.getPropertyValue("LoggerName")) != null)
                LoggerName = o.toString();
            if ((o = entry.getPropertyValue("LoggerFormat")) != null)
                LoggerFormat = o.toString();
            if ((o = entry.getPropertyValue("LogFilePath")) != null)
                LogFilePath = o.toString();
            if ((o = entry.getPropertyValue("LogFilePattern")) != null)
                LogFilePattern = o.toString();
            if ((o = entry.getPropertyValue("LogLevel")) != null) {
                if (o.toString().equalsIgnoreCase("all"))
                    LogLevel = ALL;
                else if (o.toString().equalsIgnoreCase("trace"))
                    LogLevel = TRACE;
                else if (o.toString().equalsIgnoreCase("debug"))
                    LogLevel = DEBUG;
                else if (o.toString().equalsIgnoreCase("info"))
                    LogLevel = INFO;
                else if (o.toString().equalsIgnoreCase("warn"))
                    LogLevel = WARN;
                else if (o.toString().equalsIgnoreCase("error"))
                    LogLevel = ERROR;
                else if (o.toString().equalsIgnoreCase("fatal"))
                    LogLevel = FATAL;
                else if (o.toString().equalsIgnoreCase("off"))
                    LogLevel = OFF;
                else
                    throw new JposException(JPOS_E_ILLEGAL, "Invalid warning level");
            }
            int val;
            if ((o = entry.getPropertyValue("MaxArrayStringElements")) != null && (val = Integer.parseInt(o.toString())) > 0)
                MaxArrayStringElements = val;
            if ((o = entry.getPropertyValue("SerialIOAdapterClass")) != null) {
                if (SerialIOAdapterClass == null) {
                    try {
                        Constructor<?> constructor = Class.forName(o.toString()).getConstructor();
                        Object adapter = constructor.newInstance();
                        if (adapter instanceof SerialIOAdapter)
                            SerialIOAdapterClass = o.toString();
                    } catch (Exception ignore) {}
                    check(SerialIOAdapterClass == null, JPOS_E_NOSERVICE, "No SerialIOAdapter class: " + o);
                }
                else if (!SerialIOAdapterClass.equals(o.toString()))
                    throw new JposException(JPOS_E_NOSERVICE, "SerialIOAdapterClass of different device entries of a physical device must match");
            }
            CurrentEntry = entry;
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            throw new JposException(JPOS_E_ILLEGAL, "Invalid JPOS property", e);
        }
    }


    /**
     * Enqueues or fires data event. If FreezeEvent is false, the event will be fired immediately. Otherwise, it will
     * be enqueued and fired after FreezeEvents will be reset. In addition, properties DeviceEnabled, DataEventEnabled
     * and DataCount will be adjusted as described in the UPOS specification
     *
     * @param event Event to be fired.
     * @throws JposException In case of an error during deviceEnable(false) due to AutoDisable = true.
     */
    public void handleEvent(JposDataEvent event) throws JposException {
        JposCommonProperties props = event.getPropertySet();
        synchronized (props.EventList) {
            if (props.DeviceEnabled) {
                log(DEBUG, props.LogicalName + ": Buffer Data Event: [" + event.toLogString() + "]");
                props.EventList.add(event);
                props.DataCount++;
                props.EventSource.logSet("DataCount");
                processEventList(props);
                if (props.AutoDisable) {
                    props.EventSource.setDeviceEnabled(false);
                }
            }
        }
    }

    /**
     * Enqueues or fires error event. If FreezeEvent is false, the event will be fired immediately. Otherwise, it will
     * be enqueued and fired after FreezeEvents will be reset.
     *
     * @param event Event to be fired
     * @throws JposException If error occurs during event handling
     */
    public void handleEvent(JposErrorEvent event) throws JposException {
        JposCommonProperties props = event.getPropertySet();
        synchronized (props.EventList) {
            if (props.DeviceEnabled) {
                props.State = JPOS_S_ERROR;
                if (event.getErrorLocus() == JPOS_EL_INPUT && props.DataCount > 0) {
                    JposEvent ev = null;
                    for (int i = 0; i < props.EventList.size(); ++i) {
                        ev = props.EventList.get(i);
                        if (ev instanceof JposDataEvent) {
                            props.EventList.add(i, event.getInputDataErrorEvent());
                            break;
                        }
                    }
                    if (!(ev instanceof JposDataEvent) && props.DataEventList.get(0) instanceof JposDataEvent) {
                        props.DataEventList.add(0, event.getInputDataErrorEvent());
                    }
                }
                props.EventList.add(event);
                log(DEBUG, props.LogicalName + ": Buffer Error Event: [" + event.toLogString() + "]");
                processEventList(props);
            }
        }
    }

    /**
     * Enqueues or fires status update event. If FreezeEvent is false, the event will be fired immediately. Otherwise, it will
     * be enqueued and fired after FreezeEvents will be reset. In case of sharable devices, the event will be fired to all
     * devices controls that enabled the same physical device.
     *
     * @param event Event to be fired
     * @throws JposException If error occurs during event handling
     */
    @SuppressWarnings("SynchronizeOnNonFinalField ")
    public void handleEvent(JposStatusUpdateEvent event) throws JposException {
        JposCommonProperties props = event.getPropertySet();
        synchronized (props.DevProps) {
            for (int j = 0; j < props.DevProps.size(); j++) {
                JposCommonProperties dev = props.DevProps.get(j);
                synchronized (dev.EventList) {
                    JposStatusUpdateEvent ev;
                    ev = (dev == props) ? event : event.copyEvent((JposBase) event.getSource());
                    if (!ev.block()) {
                        ev.setAndCheckStatusProperties();
                        if (dev.DeviceEnabled) {
                            dev.EventList.add(ev);
                            log(DEBUG, dev.LogicalName + ": Buffer StatusUpdateEvent: [" + ev.toLogString() + "]");
                            processEventList(dev);
                        }
                    }
                }
            }
        }
    }

    /**
     * Enqueues or fires output complete event. If FreezeEvent is false, the event will be fired immediately. Otherwise, it will
     * be enqueued and fired after FreezeEvents will be reset.
     *
     * @param event Event to be fired
     * @throws JposException If error occurs during event handling
     */
    public void handleEvent(JposOutputCompleteEvent event) throws JposException {
        JposCommonProperties props = event.getPropertySet();
        synchronized (props.EventList) {
            if (props.DeviceEnabled) {
                props.EventList.add(event);
                log(DEBUG, props.LogicalName + ": Buffer OutputCompleteEvent: [" + event.toLogString() + "]");
                processEventList(props);
            }
        }
    }

    /**
     * Enqueues or fires transition event. If FreezeEvent is false, the event will be fired immediately. Otherwise, it will
     * be enqueued and fired after FreezeEvents will be reset.
     *
     * @param event Event to be fired
     * @throws JposException If error occurs during event handling
     */
    public void handleEvent(JposTransitionEvent event) throws JposException {
        JposCommonProperties props = event.getPropertySet();
        synchronized (props.EventList) {
            if (props.DeviceEnabled) {
                props.EventList.add(event);
                log(DEBUG, props.LogicalName + ": Buffer TransitionEvent: [" + event.toLogString() + "]");
                processEventList(props);
            }
        }
    }

    /**
     * Enqueues or fires direct IO event. If FreezeEvent is false, the event will be fired immediately. Otherwise, it will
     * be enqueued and fired after FreezeEvents will be reset.
     *
     * @param event Event to be fired
     * @throws JposException If error occurs during event handling
     */
    public void handleEvent(JposDirectIOEvent event) throws JposException {
        JposCommonProperties props = event.getPropertySet();
        synchronized (props.EventList) {
            if (props.DeviceEnabled) {
                props.EventList.add(event);
                log(DEBUG, props.LogicalName + ": Buffer DirectIOEvent: [" + event.toLogString() + "]");
                processEventList(props);
            }
        }
    }

    private final Map<List<JposCommonProperties>, List<SyncObject>> StatusWaitingObjects = new HashMap<>();

    /**
     * Prepares device for later signalling status waits via signalStatusWaits method. Ensures that only those waiting
     * instances will be signalled that are still waiting during preparation. In case of situations where status
     * lookup and status changing commands can be invoked in different threads, this method should be called before
     * invoking the status lookup to ensure that an invocation of signalStatusWaits after the lookup will only signal
     * those threads that started waiting before the lookup started.
     *
     * @param propertylist list of property sets to be used for SyncObject lookup.
     */
    @SuppressWarnings("SynchronizeOnNonFinalField")
    public void prepareSignalStatusWaits(List<JposCommonProperties> propertylist) {
        List<SyncObject> waitingObjects;
        if (StatusWaitingObjects.containsKey(propertylist))
            (waitingObjects = StatusWaitingObjects.get(propertylist)).clear();
        else
            StatusWaitingObjects.put(propertylist, waitingObjects = new ArrayList<>(0));
        for(JposCommonProperties props : propertylist) {
            synchronized(props.EventSource.Props) {
                SyncObject obj = props.retrieveWaiter();
                if (obj != null) {
                    waitingObjects.add(obj);
                }
            }
        }
    }

    /**
     * Signals SyncObject objects attached to any property set bound to the device. If prepareSignalStatusWaits has been
     * invoked for the same property set list previously, only those objects will be signalled that have been attached
     * before the last call to prepareSignalStatusWaits. Otherwise, all attached objects will be signalled.
     *
     * @param propertylist list of property sets to be used for SyncObject lookup.
     */
    public void signalStatusWaits(List<JposCommonProperties> propertylist) {
        if (StatusWaitingObjects.containsKey(propertylist)) {
            List<SyncObject> toBeSignalled = StatusWaitingObjects.get(propertylist);
            for (SyncObject waiter : toBeSignalled) {
                waiter.signal();
            }
        }
        else {
            for (JposCommonProperties props : propertylist) {
                props.signalWaiter();
            }
        }
    }

    /**
     * Sets PowerState property to its current value and fires status update event, passing this state to the application.
     *
     * @param dev Device property set
     * @throws JposException If an error ocurs during event firing
     */
    public void handlePowerStateOnEnable(JposCommonProperties dev) throws JposException {
        int state;
        switch (dev.PowerState) {
        case JPOS_PS_OFF:
            state = JPOS_SUE_POWER_OFF;
            break;
        case JPOS_PS_OFFLINE:
            state = JPOS_SUE_POWER_OFFLINE;
            break;
        case JPOS_PS_OFF_OFFLINE:
            state = JPOS_SUE_POWER_OFF_OFFLINE;
            break;
        case JPOS_PS_ONLINE:
            state = JPOS_SUE_POWER_ONLINE;
            break;
        default:
            return;
        }
        JposStatusUpdateEvent event;
        event = new JposStatusUpdateEvent(dev.EventSource, state);
        synchronized (dev.EventList) {
            dev.EventList.add(event);
            log(DEBUG, dev.LogicalName + ": Buffer StatusUpdateEvent: [" + event.toLogString() + "]");
            processEventList(dev);
        }
    }

    /**
     * Process the event queue. Fires notification events (except data events) and buffers confirmation events in
     * confirmation event queue while FreezeEvents = false
     */
    class EventFirer extends Thread {
        private final JposCommonProperties Props;

        /**
         * Constructor for service thread that fires events when for the device instance that holds the given property set.
         * @param dev   Property set of the device instance.
         * @throws JposException If thread creation fails.
         */
        EventFirer(JposCommonProperties dev) throws JposException {
            super();
            Props = dev;
            setName("EventFirer");
            Props.EventProcessor = this;
            try {
                start();
            }
            catch (Exception e) {
                throw new JposException(JPOS_E_FAILURE, e.getMessage(), e);
            }
        }

        @Override
        public void run() {
            while(true) {
                JposStatusUpdateEvent stevent = null;
                JposDirectIOEvent dioevent = null;
                JposTransitionEvent trevent = null;
                JposErrorEvent errevent = null;
                JposOutputCompleteEvent ocevent = null;
                JposDataEvent devent = null;
                synchronized (Props.EventList) {
                    Props.EventProcessor = null;
                    if (Props.FreezeEvents || (Props.EventList.size() == 0 && (!Props.DataEventEnabled || Props.DataEventList.size() == 0)))
                        break;
                    Props.EventProcessor = this;
                    JposEvent event;
                    if (Props.DataEventEnabled && Props.DataEventList.size() > 0) {
                        event = Props.DataEventList.get(0);
                        Props.DataEventList.remove(0);
                    } else {
                        event = Props.EventList.get(0);
                        Props.EventList.remove(0);
                    }
                    if (event instanceof JposTransitionEvent) {
                        (trevent = (JposTransitionEvent) event).setTransitionProperties();
                    } else if (event instanceof JposDirectIOEvent) {
                        (dioevent = (JposDirectIOEvent) event).setDirectIOProperties();
                    } else if (event instanceof JposStatusUpdateEvent) {
                        (stevent = (JposStatusUpdateEvent) event).setLateProperties();
                    } else if (event instanceof JposOutputCompleteEvent) {
                        (ocevent = (JposOutputCompleteEvent) event).setOutputCompleteProperties();
                    } else if (event instanceof JposErrorEvent) {
                        if (((JposErrorEvent) event).getErrorLocus() == JPOS_EL_OUTPUT || Props.DataEventEnabled)
                            (errevent = (JposErrorEvent) event).setErrorProperties();
                        else if (Props.StrictFIFOEventHandling) {
                            Props.EventList.add(0, event);
                            Props.EventProcessor = null;
                            break;
                        } else
                            Props.DataEventList.add(event);
                    } else if (event instanceof JposDataEvent) {
                        if (Props.DataEventEnabled)
                            (devent = (JposDataEvent) event).setDataProperties();
                        else if (Props.StrictFIFOEventHandling) {
                            Props.EventList.add(0, event);
                            Props.EventProcessor = null;
                            break;
                        } else
                            Props.DataEventList.add(event);
                    }
                }
                if (dioevent != null) {
                    final SyncObject waiter = new SyncObject();
                    final JposDirectIOEvent dioev = dioevent;
                    new Thread(() -> {
                        try {
                            Props.EventCB.fireDirectIOEvent(dioev);
                            log(DEBUG, Props.LogicalName + ": Fire Transition Event: [" + dioev.toLogString() + "]");
                            postDirectIOProcessing(dioev);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        } finally {
                            waiter.signal();
                        }
                    }, "DirectIOEventRunner").start();
                    waiter.suspend(Props.MaximumConfirmationEventWaitingTime);
                }
                else if (trevent != null) {
                    final SyncObject waiter = new SyncObject();
                    final JposTransitionEvent tev = trevent;
                    new Thread(() -> {
                        try {
                            if (Props.EventCB instanceof EventCallbacks2) {
                                ((EventCallbacks2) Props.EventCB).fireTransitionEvent(tev);
                                log(DEBUG, Props.LogicalName + ": Fire Transition Event: [" + tev.toLogString() + "]");
                            } else {
                                log(DEBUG, Props.LogicalName + ": Transition Event: [" + tev.toLogString() + "]: Unsupported");
                            }
                            postTransitionProcessing(tev);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        } finally {
                            waiter.signal();
                        }
                    }, "TransitionEventRunner").start();
                    waiter.suspend(Props.MaximumConfirmationEventWaitingTime);
                } else if (errevent != null) {
                    final SyncObject waiter = new SyncObject();
                    final JposErrorEvent errev = errevent;
                    if (errevent.getErrorLocus() == JPOS_EL_OUTPUT) {
                        new Thread(() -> {
                            try {
                                synchronized (AsyncProcessorRunning) {
                                    Props.EventCB.fireErrorEvent(errev);
                                    log(DEBUG, Props.LogicalName + ": Fire Error Event: [" + errev.toLogString() + "]");
                                    if (errev.getErrorResponse() == JPOS_ER_CLEAR) {
                                        errev.clear();
                                    }
                                }
                                if (errev.getErrorResponse() == JPOS_ER_RETRY) {
                                    try {   // retryInput should never throw an exception!
                                        Props.EventSource.DeviceInterface.retryOutput();
                                    } catch (JposException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                            } finally {
                                waiter.signal();
                            }
                        }, "OutputErrorEventRunner").start();
                        waiter.suspend(Props.MaximumConfirmationEventWaitingTime);
                    } else {
                        new Thread(() -> {
                            try {
                                synchronized (AsyncProcessorRunning) {
                                    Props.EventCB.fireErrorEvent(errev);
                                    log(DEBUG, Props.LogicalName + ": Fire Error Event: [" + errev.toLogString() + "]");
                                    if (errev.getErrorResponse() == JPOS_ER_CLEAR) {
                                        errev.clear();
                                    }
                                }
                                if (errev.getErrorResponse() == JPOS_ER_RETRY) {
                                    try {   // retryInput should never throw an exception!
                                        Props.EventSource.DeviceInterface.retryInput();
                                    } catch (JposException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                            } finally {
                                waiter.signal();
                            }
                        }, "InputErrorEventRunner").start();
                        waiter.suspend(Props.MaximumConfirmationEventWaitingTime);
                    }
                } else if (ocevent != null) {
                    try {
                        Props.EventCB.fireOutputCompleteEvent(ocevent);
                    }  catch (Throwable e) {
                        e.printStackTrace();
                    }
                    log(DEBUG, Props.LogicalName + ": Fire Buffered Output Complete Event: [" + ocevent.toLogString() + "]");
                } else if (stevent != null) {
                    try {
                        Props.EventCB.fireStatusUpdateEvent(stevent);
                    }  catch (Throwable e) {
                        e.printStackTrace();
                    }
                    log(DEBUG, Props.LogicalName + ": Fire Buffered Status Update Event: [" + stevent.toLogString() + "]");
                } else if (devent != null) {
                    try {
                        Props.DataCount--;
                        Props.EventSource.logSet("DataCount");
                        Props.EventSource.setDataEventEnabled(false);
                        Props.EventCB.fireDataEvent(devent);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    log(DEBUG, Props.LogicalName + ": Fire Data Event: [" + devent.toLogString() + "]");
                }
            }
        }
    }

    /**
     * Process the event queue. Fires all notification events except data events while
     * FreezeEvents = false.
     *
     * @param dev Property set to be used for event processing
     * @throws JposException If <i>EventFirer</i> object cannot be created.
     */
    protected void processEventList(JposCommonProperties dev) throws JposException {
        synchronized (dev.EventList) {
            if (dev.EventProcessor == null && !dev.FreezeEvents && (dev.EventList.size() > 0 || (dev.DataEventEnabled && dev.DataEventList.size() > 0))) {
                new EventFirer(dev);
            }
        }
    }

    /**
     * Retrieves TransitionEvent after application could modify pData and pString. Allows the device service to continue
     * further processing as requested.
     * @param trevent JposTransitionEvent after return from application callback. null if the device control does not
     *                support transition events.
     */
    public void postTransitionProcessing(JposTransitionEvent trevent) {
        if (trevent != null) {
            trevent.WriteProtected = true;
            if (trevent instanceof JposTransitionWaitingEvent)
                ((JposTransitionWaitingEvent) trevent).getWaiter().signal();
        }
    }

    /**
     * Retrieves DirectIOEvent after application could modify data and obj. Allows the device service to continue
     * further processing as requested.
     * @param dioevent JposDirectIOEvent after return from application callback.
     */
    public void postDirectIOProcessing(JposDirectIOEvent dioevent) {
        if (dioevent != null) {
            dioevent.WriteProtected = true;
            if (dioevent instanceof JposDirectIOWaitingEvent)
                ((JposDirectIOWaitingEvent) dioevent).getWaiter().signal();
        }
    }

    /**
     * This method removes the given property set from the corresponding property set list and removes the device
     * from the list of all devices, if no further property set is present in any other property set list.
     * @param props Property set to be removed from the corresponding property set list.
     * @return      true if the device has been removed from the list of all devices, otherwise false
     * @throws JposException    Source is JposDeviceFactory.deleteDevice. Fails if device cannot be found in list of all
     *                          devices.
     */
    public boolean removePropertySet(JposCommonProperties props) throws JposException {
        props.EventSource.DeviceInterface.removeFromPropertySetList();
        if (noOfPropertySets() == 0) {
            JposDeviceFactory.deleteDevice(ID);
            props.EventSource.logCall("DeleteInstance probably");
            synchronized(LoggerSync) {
                if (Log != null) {
                    Log.removeAllAppenders();
                    Log = null;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Method that returns the number of property sets hold by this device. It is the sum of all property sets, created
     * for each open of any device class handled by this device.<br>
     * This method is only a dummy, overwritten in class JposDevice.
     * @return Number of property sets bound to this device.
     */
    public int noOfPropertySets() {
        return 0;
    }

    /**
     * Retrieves a claiming device from a device claimer array. Should be used to access the property set of any currently
     * claiming device.
     * @param claimedDevices Array of claiming devices. One of ClaimedCashDrawer, ClaimedKeylock, ...
     * @param index Index of property set within the array. Must be between 0 and the length of claimedDevices.
     * @return Propetry set of claiming device or null if device[index] has not been claimed or if index is out of range.
     */
    public JposCommonProperties getClaimingInstance(JposCommonProperties[] claimedDevices, int index) {
        JposCommonProperties[][] claimedDevicesArray = {claimedDevices};
        synchronized(claimedDevicesArray[0]) {
            return index >= 0 && index < claimedDevices.length ? claimedDevices[index] : null;
        }
    }

    /**
     * Retrieves a device instance from the list of all instances bound to a specific device.
     * @param propertysets  List of list of property sets bound to one of the devices of same device type.
     * @param deviceindex   Index of the requested device.
     * @param propertysetindex Index of the requested property set.
     * @return Requested property set or null if deviceindex or propertysetindex is out of range.
     */
    public JposCommonProperties getPropertySetInstance(List<JposCommonProperties>[] propertysets, int deviceindex, int propertysetindex) {
        if (deviceindex >= 0 && deviceindex < propertysets.length) {
            synchronized(propertysets[deviceindex]) {
                if (propertysetindex >= 0 && propertysetindex < propertysets[deviceindex].size())
                    return propertysets[deviceindex].get(propertysetindex);
            }
        }
        return null;
    }
}