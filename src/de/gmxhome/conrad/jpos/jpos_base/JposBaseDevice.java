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
import jpos.config.JposEntry;
import jpos.events.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.*;
import javax.swing.*;

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
 * where the processing shall start. This is typically within the output request worker
 * thread.
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
    public int CapPowerReporting = JposConst.JPOS_PR_NONE;
    ;

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
     * List holds all outstanding output requests.
     */
    public List<JposOutputRequest> PendingCommands = new ArrayList<JposOutputRequest>();

    /**
     * Flag that signals whether the processor for asynchronous commands is running. Used for synchronization purposes.
     */
    public Boolean[] AsyncProcessorRunning = new Boolean[]{false};

    /**
     * Currently executed output request.
     */
    public JposOutputRequest CurrentCommand;

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
    int getCount(List<JposCommonProperties>[] device) {
        int count = 0;

        for (int i = 0; i < device.length; i++) {
            for (int j = 0; j < device[i].size(); j++) {
                if (device[i].get(j) != null) {
                    count++;
                }
            }
        }
        return count;
    }

    // private helper variables
    private boolean DialogReady;    // Helper variable for synchronized message box handling

    /**
     * Show message dialog within swing thread + synchronization. Useful in interactive CheckHealth methods
     *
     * @param message     see JoptionPane.showMessageBox
     * @param title       see JoptionPane.showMessageBox
     * @param messageType see JoptionPane.showMessageBox
     */
    public void synchronizedMessageBox(final String message, final String title, final int messageType) {
        DialogReady = false;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null, message, title, messageType);
                dialogReady(true);
            }
        });
        while (!dialogReady(false)) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }
    }

    /**
     * Set or queries DialogReady value.
     *
     * @param set if true, sets DialogReady = true, else don't change its value
     * @return value of DialogReady
     */
    private synchronized boolean dialogReady(boolean set) {
        if (set)
            DialogReady = true;
        return DialogReady;
    }

    /**
     * Checks whether the given condition it met. If not, throws
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
     * Checks whether the given condition it met. If not, throws
     * a JposException with JPOS_E_EXTENDED and the given extended error code and error description
     *
     * @param condition Error condition
     * @param ext       Jpos extended error code
     * @param errtxt    Corresponding error text
     * @throws JposException Will be thrown if condition is true
     */
    static public void checkext(boolean condition, int ext, String errtxt) throws JposException {
        if (condition) {
            throw new JposException(JposConst.JPOS_E_EXTENDED, ext, errtxt);
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
    static public void delay(int millis) {
        long startingTime = System.currentTimeMillis();
        long deltaTime;
        while ((deltaTime = System.currentTimeMillis() - startingTime) < millis) {
            try {
                Thread.sleep(millis - deltaTime);
            } catch (InterruptedException e) {
            }
        }
    }

    // Object used for synchronization purposes.
    private Byte[] LoggerSync = new Byte[]{(byte) 0};

    /**
     * Performs logging. Includes automatic logger initialization whenever necessary and possible.
     * However, if logger intialization is not possible due to missing entries in jpos.xml, no logging
     * will be made.
     *
     * @param loglevel Logging level, see Log4j specification
     * @param message  Message to be logged
     */
    public void log(Level loglevel, String message) {
        synchronized (LoggerSync) {
            if (Log == null && LogLevel != Level.OFF) {
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
            Log.log(loglevel, message);
        }
    }

    /**
     * Volume for drawer beep. Valid values are from 0 to 127. Will be initialized with an Integer object only for
     * devices that support at least one CashDrawer device.
     */
    public Integer DrawerBeepVolume = null;

    /**
     * Holds the JavaPOS control version. Must be unique for all JavaPOS devices supported by a device implementation.
     * For example, if an implementation for a device supports logical devices for a POSPrinter and two CashDrawers,
     * the version must be the same for all logical devices (and it must be specified for all devices).
     */
    public Integer JposVersion = null;

    /**
     * Checks whether a JposEntry belongs to a predefined property value an if so,
     * sets the corresponding driver value
     *
     * @param entry Entry to be checked, contains value to be set
     * @throws JposException if a property value is invalid
     */
    public void checkProperties(JposEntry entry) throws JposException {
        try {
            Object o;
            if ((o = entry.getPropertyValue("jposVersion")) != null) {
                String[] versionparts = o.toString().split("\\.");
                int version = 0;
                for (int i = 0; i < 3; i++) {
                    version *= 1000;
                    if (i < versionparts.length) {
                        int component = Integer.parseInt(versionparts[i]);
                        checkRange(component, i == 0 ? 1 : 0, 999, JposConst.JPOS_E_ILLEGAL, "Bad JposVersion: " + o.toString());
                        version += Integer.parseInt(versionparts[i]);
                    }
                }
                if (JposVersion == null) {
                    JposVersion = version;
                }
                else if (JposVersion != version)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "JposVersions not unique");
            }
            if ((o = entry.getPropertyValue("LoggerName")) != null)
                LoggerName = o.toString();
            if ((o = entry.getPropertyValue("LoggerFormat")) != null)
                LoggerFormat = o.toString();
            if ((o = entry.getPropertyValue("LogFilePath")) != null)
                LogFilePath = o.toString();
            if ((o = entry.getPropertyValue("LogFilePattern")) != null)
                LogFilePattern = o.toString();
            if ((o = entry.getPropertyValue("LogLevel")) != null) {
                if (o.toString().toLowerCase().equals("all"))
                    LogLevel = Level.ALL;
                else if (o.toString().toLowerCase().equals("trace"))
                    LogLevel = Level.TRACE;
                else if (o.toString().toLowerCase().equals("debug"))
                    LogLevel = Level.DEBUG;
                else if (o.toString().toLowerCase().equals("info"))
                    LogLevel = Level.INFO;
                else if (o.toString().toLowerCase().equals("warn"))
                    LogLevel = Level.WARN;
                else if (o.toString().toLowerCase().equals("error"))
                    LogLevel = Level.ERROR;
                else if (o.toString().toLowerCase().equals("fatal"))
                    LogLevel = Level.FATAL;
                else if (o.toString().toLowerCase().equals("off"))
                    LogLevel = Level.OFF;
                else
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid warning level");
            }
            // Device class specific entries, common for all implementations
            if (DrawerBeepVolume != null && (o = entry.getPropertyValue("DrawerBeepVolume")) != null) {
                DrawerBeepVolume = Integer.parseInt(o.toString());
                if (DrawerBeepVolume < 0 || DrawerBeepVolume > 127)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Drawer beep value not between 0 and 127: " + DrawerBeepVolume);
            }
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property", e);
        }
    }


    /**
     * Enqueues or fires data event. If FreezeEvent is false, the event will be fired immediately. Otherwise it will
     * be enqueued and fired after FreezeEvents will be reset. In addition, properties DeviceEnabled, DataEventEnabled
     * and DataCount will be adjusted as described in the UPOS specification
     *
     * @param event Event to be fired.
     * @throws JposException In case of an error during deviceEnable(false) due to AutoDisable = true.
     */
    public void handleEvent(JposDataEvent event) throws JposException {
        JposCommonProperties props = event.getPropertySet();
        synchronized (props.DataEventList) {
            if (props.DeviceEnabled) {
                if (props.AutoDisable) {
                    props.EventSource.setDeviceEnabled(false);
                }
                log(Level.DEBUG, props.LogicalName + ": Buffer Data Event: [" + event.toLogString() + "]");
                props.DataEventList.add(event);
                props.DataCount++;
                props.EventSource.logSet("DataCount");
                processDataEventList(props);
            }
        }
    }

    /**
     * Enqueues or fires error event. If FreezeEvent is false, the event will be fired immediately. Otherwise it will
     * be enqueued and fired after FreezeEvents will be reset.
     *
     * @param event Event to be fired
     * @throws JposException If error occurs during event handling
     */
    public void handleEvent(JposErrorEvent event) throws JposException {
        JposCommonProperties props = event.getPropertySet();
        if (event.getErrorLocus() != JposConst.JPOS_EL_OUTPUT)
            synchronized (props.DataEventList) {
                if (props.DeviceEnabled) {
                    props.State = JposConst.JPOS_S_ERROR;
                    if (event.getErrorLocus() == JposConst.JPOS_EL_INPUT && props.DataEventList.size() > 0 && props.DataEventList.get(0) instanceof JposDataEvent) {
                        props.DataEventList.add(0, event.getInputDataErrorEvent());
                    }
                    props.DataEventList.add(event);
                    log(Level.DEBUG, props.LogicalName + ": Buffer Error Event: [" + event.toLogString() + "]");
                    processDataEventList(props);
                }
            }
        else
            synchronized (props.ErrorEventList) {
                if (props.DeviceEnabled) {
                    props.State = JposConst.JPOS_S_ERROR;
                    props.ErrorEventList.add(event);
                    log(Level.DEBUG, props.LogicalName + ": Buffer Error Event: [" + event.toLogString() + "]");
                    processErrorEventList(props);
                }
            }
    }


    /**
     * Enqueues or fires status update event. If FreezeEvent is false, the event will be fired immediately. Otherwise it will
     * be enqueued and fired after FreezeEvents will be reset. In case of sharable devices, the event will be fired to all
     * devices controls that enabled the same physical device.
     *
     * @param event Event to be fired
     * @throws JposException If error occurs during event handling
     */
    public void handleEvent(JposStatusUpdateEvent event) throws JposException {
        JposCommonProperties props = event.getPropertySet();
        synchronized (props.DevProps) {
            for (int j = 0; j < props.DevProps.size(); j++) {
                JposCommonProperties dev = props.DevProps.get(j);
                synchronized (dev.EventList) {
                    if (dev.DeviceEnabled) {
                        JposStatusUpdateEvent ev;
                        ev = (dev == props) ? event : event.copyEvent((JposBase)event.getSource());
                        if (!ev.block()) {
                            ev.setAndCheckStatusProperties();
                            dev.EventList.add(ev);
                            log(Level.DEBUG, dev.LogicalName + ": Buffer StatusUpdateEvent: [" + ev.toLogString() + "]");
                            processEventList(dev);
                        }
                    }
                }
            }
        }
    }

    /**
     * Enqueues or fires output complete event. If FreezeEvent is false, the event will be fired immediately. Otherwise it will
     * be enqueued and fired after FreezeEvents will be reset.
     *
     * @param event Event to be fired
     * @throws JposException If error occurs during event handling
     */
    public void handleEvent(JposOutputCompleteEvent event) throws JposException {
        JposCommonProperties props = event.getPropertySet();
        synchronized (props.ErrorEventList) {
            props.ErrorEventList.add(event);
            log(Level.DEBUG, props.LogicalName + ": Buffer OutputCompleteEvent: [" + event.toLogString() + "]");
            processErrorEventList(props);
        }
    }

    /**
     * Enqueues or fires direct IO event. If FreezeEvent is false, the event will be fired immediately. Otherwise it will
     * be enqueued and fired after FreezeEvents will be reset.
     *
     * @param event Event to be fired
     * @throws JposException If error occurs during event handling
     */
    public void handleEvent(JposDirectIOEvent event) throws JposException {
        JposCommonProperties props = event.getPropertySet();
        synchronized (props.DirectIOEventList) {
            props.DirectIOEventList.add(event);
            log(Level.DEBUG, props.LogicalName + ": Buffer DirectIOEvent: [" + event.toLogString() + "]");
            processDirectIOEventList(props);
        }
    }

    private Map<List<JposCommonProperties>, List<SyncObject>> StatusWaitingObjects = new HashMap();

    /**
     * Prepares device for later signalling status waits via signalStatusWaits method. Ensures that only those waiting
     * instances will be signalled that are still waiting during preparation. In case of situations where status
     * lookup and status changing commands can be invoked in different threads, this method should be called before
     * invoking the status lookup to ensure that an invocation of signalStatusWaits after the lookup will only signal
     * those threads that started waiting before the lookup started.
     *
     * @param propertylist list of property sets to be used for SyncObject lookup.
     */
    public void prepareSignalStatusWaits(List<JposCommonProperties> propertylist) {
        List<SyncObject> waitingObjects;
        if (StatusWaitingObjects.containsKey(propertylist))
            (waitingObjects = StatusWaitingObjects.get(propertylist)).clear();
        else
            StatusWaitingObjects.put(propertylist, waitingObjects = new ArrayList(0));
        for(JposCommonProperties props : propertylist) {
            synchronized(props) {
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
            case JposConst.JPOS_PS_OFF:
                state = JposConst.JPOS_SUE_POWER_OFF;
                break;
            case JposConst.JPOS_PS_OFFLINE:
                state = JposConst.JPOS_SUE_POWER_OFFLINE;
                break;
            case JposConst.JPOS_PS_OFF_OFFLINE:
                state = JposConst.JPOS_SUE_POWER_OFF_OFFLINE;
                break;
            case JposConst.JPOS_PS_ONLINE:
                state = JposConst.JPOS_SUE_POWER_ONLINE;
                break;
            default:
                return;
        }
        JposStatusUpdateEvent event;
        event = new JposStatusUpdateEvent(dev.EventSource, state);
        synchronized (dev.EventList) {
            dev.EventList.add(event);
            log(Level.DEBUG, dev.LogicalName + ": Buffer StatusUpdateEvent: [" + event.toLogString() + "]");
            processEventList(dev);
        }
    }

    /**
     * Process the event queue. Fires StatusUpdateEvent events while
     * FreezeEvents = false
     */
    class EventFirer extends Thread {
        private JposCommonProperties Props;
        EventFirer(JposCommonProperties dev) throws JposException {
            super();
            Props = dev;
            setName("EventFirer");
            Props.EventProcessor = this;
            try {
                start();
            }
            catch (Exception e) {
                throw new JposException(JposConst.JPOS_E_FAILURE, e.getMessage(), e);
            }
        }

        @Override
        public void run() {
            JposStatusUpdateEvent stevent = null;
            JposDirectIOEvent dioevent = null;
            while(true) {
                synchronized (Props.EventList) {
                    Props.EventProcessor = null;
                    if (Props.FreezeEvents || Props.EventList.size() == 0)
                        break;
                    Props.EventProcessor = this;
                    JposEvent event = Props.EventList.get(0);
                    Props.EventList.remove(0);
                    if (event instanceof JposDirectIOEvent)
                        (dioevent = (JposDirectIOEvent)event).setDirectIOProperties();
                    else if (event instanceof JposStatusUpdateEvent) {
                        (stevent = (JposStatusUpdateEvent)event).setLateProperties();
                    }
                }
                if (dioevent != null) {
                    Props.EventCB.fireDirectIOEvent(dioevent);
                    log(Level.DEBUG, Props.LogicalName + ": Fire Buffered Direct IO Event: [" + dioevent.toLogString() + "]");
                }
                else if (stevent != null) {
                    Props.EventCB.fireStatusUpdateEvent(stevent);
                    log(Level.DEBUG, Props.LogicalName + ": Fire Buffered Status Update Event: [" + stevent.toLogString() + "]");
                }
            }
        }
    }

    /**
     * Process the event queue. Fires DataEvent and ErrorEvent events while
     * FreezeEvents = false and DataEventEnabled = true
     *
     * @param dev Property set to be used for event processing
     * @throws JposException If <i>EventFirer</i> object cannot be created.
     */
    protected void processEventList(JposCommonProperties dev) throws JposException {
        if (dev.EventProcessor == null && !dev.FreezeEvents && dev.EventList.size() > 0) {
            new EventFirer(dev);
        }
    }

    /**
     * Process the data event queue. Fires DataEvent and ErrorEvent events while
     * FreezeEvents = false and DataEventEnabled = true
     */
    class DataEventFirer extends Thread {
        private JposCommonProperties Props;
        DataEventFirer(JposCommonProperties dev) throws JposException {
            Props = dev;
            setName("DataEventFirer");
            dev.DataEventProcessor = this;
            try {
                start();
            }
            catch (Exception e) {
                throw new JposException(JposConst.JPOS_E_FAILURE, e.getMessage(), e);
            }
        }

        @Override
        public void run() {
            JposErrorEvent errevent = null;
            JposDataEvent dataevent = null;
            JposDirectIOEvent dioevent = null;
            while (true) {
                synchronized (Props.DataEventList) {
                    Props.DataEventProcessor = null;
                    if (Props.FreezeEvents || Props.DataEventList.size() == 0)
                        break;
                    JposEvent event = Props.DataEventList.get(0);
                    if (event instanceof JposDataEvent) {
                        if (!Props.DataEventEnabled)
                            break;
                        (dataevent = (JposDataEvent) event).setDataProperties();
                    }
                    if (event instanceof JposErrorEvent)
                        (errevent = (JposErrorEvent) event).setErrorProperties();
                    else if (event instanceof JposDirectIOEvent)
                        (dioevent = (JposDirectIOEvent) event).setDirectIOProperties();
                    Props.DataEventList.remove(0);
                    Props.DataEventProcessor = this;
                }
                if (dataevent != null) {
                    try {
                        Props.EventSource.setDataEventEnabled(false);
                        Props.DataCount--;
                        Props.EventSource.logSet("DataCount");
                        Props.EventCB.fireDataEvent(dataevent);
                        log(Level.DEBUG, Props.LogicalName + ": Fire Data Event: [" + dataevent.toLogString() + "]");
                    } catch (JposException e) {
                        e.printStackTrace();
                    }
                } else if (errevent != null) {
                    synchronized (AsyncProcessorRunning) {
                        Props.EventCB.fireErrorEvent(errevent);
                        log(Level.DEBUG, Props.LogicalName + ": Fire Error Event: [" + errevent.toLogString() + "]");
                        if (errevent.getErrorResponse() == JposConst.JPOS_ER_CLEAR) {
                            errevent.clear();
                        }
                    }
                    if (errevent.getErrorResponse() == JposConst.JPOS_ER_RETRY) {
                        try {   // retryInput should never throw an exception!
                            Props.EventSource.DeviceInterface.retryInput();
                        } catch (JposException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (dioevent != null) {
                    Props.EventCB.fireDirectIOEvent(dioevent);
                    log(Level.DEBUG, Props.LogicalName + ": Fire Buffered Direct IO Event: [" + dioevent.toLogString() + "]");
                }
            }
        }
    }

    /**
     * Starts processing the data event queue. If the DataEventList is not empty, creates DataEventFirer that fires
     * events until the DataEventList becomes empty. Runs always in synchronized context.
     *
     * @param dev Property set for data event processing.
     * @throws JposException If <i>DataEventFirer</i> object cannot be created.
     */
    protected void processDataEventList(JposCommonProperties dev) throws JposException {
        if (dev.DataEventProcessor == null && !dev.FreezeEvents && dev.DataEventEnabled && dev.DataEventList.size() > 0) {
            new DataEventFirer(dev);
        }
    }

    /**
     * Process the error event queue. Fires OutputCompleteEvent and ErrorEvent events while
     * FreezeEvents = false
     */
    class ErrorEventFirer extends Thread {
        private JposCommonProperties Props;
        ErrorEventFirer(JposCommonProperties dev) throws JposException {
            Props = dev;
            setName("ErrorEventFirer");
            dev.ErrorEventProcessor = this;
            try {
                start();
            }
            catch (Exception e) {
                throw new JposException(JposConst.JPOS_E_FAILURE, e.getMessage(), e);
            }
        }

        @Override
        public void run() {
            JposErrorEvent errevent = null;
            JposOutputCompleteEvent ocevent = null;
            JposDirectIOEvent dioevent = null;
            while (true) {
                synchronized (Props.ErrorEventList) {
                    Props.ErrorEventProcessor = null;
                    if (Props.FreezeEvents || Props.ErrorEventList.size() == 0)
                        break;
                    Props.ErrorEventProcessor = this;
                    JposEvent event = Props.ErrorEventList.get(0);
                    Props.ErrorEventList.remove(0);
                    if (event instanceof JposOutputCompleteEvent)
                        (ocevent = (JposOutputCompleteEvent) event).setOutputCompleteProperties();
                    if (event instanceof JposErrorEvent)
                        (errevent = (JposErrorEvent) event).setErrorProperties();
                    else if (event instanceof JposDirectIOEvent)
                        (dioevent = (JposDirectIOEvent) event).setDirectIOProperties();
                }
                if (ocevent != null) {
                    Props.EventCB.fireOutputCompleteEvent(ocevent);
                    log(Level.DEBUG, Props.LogicalName + ": Fire Output Complete Event: [" + ocevent.toLogString() + "]");
                } else if (errevent != null) {
                    synchronized (AsyncProcessorRunning) {
                        Props.EventCB.fireErrorEvent(errevent);
                        log(Level.DEBUG, Props.LogicalName + ": Fire Error Event: [" + errevent.toLogString() + "]");
                        if (errevent.getErrorResponse() == JposConst.JPOS_ER_CLEAR) {
                            errevent.clear();
                        }
                    }
                    if (errevent.getErrorResponse() == JposConst.JPOS_ER_RETRY) {
                        try {   // retryInput should never throw an exception!
                            Props.EventSource.DeviceInterface.retryOutput();
                        } catch (JposException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (dioevent != null) {
                    Props.EventCB.fireDirectIOEvent(dioevent);
                    log(Level.DEBUG, Props.LogicalName + ": Fire Buffered Direct IO Event: [" + dioevent.toLogString() + "]");
                }
            }
        }
    }

    /**
     * Process the output complete and error event queue. Fires OutputCompleteEvent or ErrorEvent events while
     * FreezeEvents = false. This kind of processing will be performed directly by the JposOutputRequest handler
     * because asynchronous processing must be synchronized with the corresponding event handling.
     *
     * @param dev Property set to be used for output error event processing
     * @throws JposException If <i>ErrorEventFirer</i> object cannot be created.
     */
    protected void processErrorEventList(JposCommonProperties dev) throws JposException {
        if (dev.ErrorEventProcessor == null && !dev.FreezeEvents && dev.ErrorEventList.size() > 0) {
            new ErrorEventFirer(dev);
        }
    }

    private void processDirectIOEventList(JposCommonProperties props) throws JposException {
        if (props.DirectIOEventList == props.EventList)
            processEventList(props);
        else if (props.DirectIOEventList == props.ErrorEventList)
            processErrorEventList(props);
        else if (props.DirectIOEventList == props.DataEventList)
            processDataEventList(props);
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

    /*
     * Common implementation that must be modified for each device type added.
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
        synchronized(claimedDevices) {
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