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
import jpos.events.JposEvent;
import jpos.services.EventCallbacks;
import net.bplaced.conrad.log4jpos.Level;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Class containing common properties, property defaults, several values necessary for
 * common event handling and for standardized asynchronous method call processing and default implementations of
 * JposBaseInterface.
 * <br>For details about properties, methods and method parameters, see UPOS specification, chapter Common Properties,
 * Methods and Events.<p>
 * Default values of properties that will be initialized with a device specific value when method open will be called,
 * must be directly set during object construction. Default values for all other properties must be set within method
 * changeDefault in the device implementation class by setting the default value into a property with name
 * <i>PropertyName</i>Def. Whenever the corresponding property shall be initialized (e.g. during enable), the default
 * value will be copied to the property.
 */
public abstract class JposCommonProperties implements JposBaseInterface {
    /**
     * Instance of JposDevice implementation that has been bound to this property set.
     */
    public JposDevice Device;

    /**
     * UPOS property CapStatisticsReporting. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapStatisticsReporting = false;

    /**
     * UPOS property CapStatisticsReporting. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapUpdateStatistics = false;

    /**
     * UPOS property DeviceServiceDescription. Default: "Default service implementation". Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public String DeviceServiceDescription = "Default service implementation";

    /**
     * UPOS property DeviceServiceVersion. Default: 1014000. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int DeviceServiceVersion = 1014000;

    /**
     * UPOS property State.
     */
    public int State = JposConst.JPOS_S_CLOSED;

    /**
     * UPOS property AsyncMode.
     */
    public boolean AsyncMode;

    /**
     * UPOS property AutoDisable.
     */
    public boolean AutoDisable;

    /**
     * UPOS property FlagWhenIdle.
     */
    public boolean FlagWhenIdle;

    /**
     * UPOS property CheckHealthText.
     */
    public String CheckHealthText;

    /**
     * UPOS property Claimed.
     */
    public boolean Claimed;

    /**
     * UPOS property DataCount.
     */
    public int DataCount;

    /**
     * UPOS property DataEventEnabled.
     */
    public boolean DataEventEnabled;

    /**
     * UPOS property DeviceEnabled.
     */
    public boolean DeviceEnabled;

    /**
     * UPOS property FreezeEvents.
     */
    public boolean FreezeEvents;

    /**
     * UPOS property OutputID.
     */
    public int OutputID;

    /**
     * UPOS property PowerNotify.
     */
    public int PowerNotify;

    /**
     * UPOS property PowerState.
     */
    public int PowerState;

    /**
     * Logical device name. Passed by Open method.
     */
    public String LogicalName;

    /**
     * UPOS property CurrentUnitID. Must be initialized at least in initOnFirstEnable method of derived class, if
     * UsesSubsystemUnits is true.
     */
    public int CurrentUnitID;

    /**
     * UPOS property ErrorString.
     */
    public String ErrorString;

    /**
     * UPOS property ErrorUnits.
     */
    public int ErrorUnits;

    /**
     * UPOS property EventString.
     */
    public String EventString;

    /**
     * UPOS property EventUnitID.
     */
    public int EventUnitID;

    /**
     * UPOS property EventUnits.
     */
    public int EventUnits;

    /**
     * UPOS property UnitsOnline.
     */
    public int UnitsOnline;

    /**
     * Object for event callbacks. Passed by Open method.
     */
    public EventCallbacks EventCB;

    /**
     * Object to be used as source of Jpos events.
     */
    public JposBase EventSource;

    /**
     * Value of property AllowAlwaysSetProperties in jpos.xml for the specified device
     */
    public boolean AllowAlwaysSetProperties = true;

    /**
     * List containing SyncObject instances to be signalled after releasing a claimed object. Whenever a service
     * tries to claim a device which has been claimed before by another instance, if adds a SyncObject to this list
     * and waits until the object will be signalled. During release or close, all objects within this list will be
     * signalled to allow all waiting instances to try claiming again.
     */
    final public List<SyncObject> ClaimWaiters = new LinkedList<SyncObject>();

    /**
     * Event list, holds at least events until they can be fired. As long as DataEventEnabled = false,
     * data events and (input) error events will be put into DataEventList instead.
     */
    final public List<JposEvent> EventList = new LinkedList<JposEvent>();

    /**
     * Event list, holds all data and input error events until they can be fired.
     */
    protected final List<JposEvent> DataEventList = new LinkedList<JposEvent>();

    /**
     * List of all property sets sharing the same UPOS device.
     */
    public List<JposCommonProperties> DevProps = null;

    /**
     * Device index. Must be betwen 0 and the number of devices of the
     * device class that the driver supports minus one.
     */
    public int Index;

    /**
     * Value for ExclusiveUse. Must be set within derived classes that support only exclusive use.
     */
    static public final int ExclusiveYes = 1;      // Exclusive use device, claim must be used

    /**
     * Value for ExclusiveUse. Must be set within derived classes that are sharable but allow exclusive use.
     */
    static public final int ExclusiveAllowed = 0;  // Claim may be used for exclusive access

    /**
     * Value for ExclusiveUse. Must be set within derived classes that are sharable and do not allow exclusive use.
     */
    static public final int ExclusiveNo = -1;      // Shareable device, claim must not be used.

    /**
     * Specifies the device model the device supports. Default is ExclusiveYes.
     */
    public int ExclusiveUse = ExclusiveYes;

    /**
     * Specifies whether the device class supports use of subsystem units. If true, the service implements a device class
     * that supports the properties EventUnitID, EventUnits, EventText, ErrorUnits, ErrorText and UnitsOnline. Currently,
     * this restricts usage to device class BumpBar and RemoteOrderDisplay.
     */
    public boolean UsesSubsystemUnits = false;

    /**
     * Specifies whether first enable happened before.
     */
    public boolean FirstEnableHappened;

    /**
     * Array holding the property sets of the device service that claimed this device class.
     */
    public JposCommonProperties[] Claiming;

    /**
     * Holds the value to be set in StatusUpdateEvents fired due to FlagWhenIdle = true
     */
    public int FlagWhenIdleStatusValue = 0;

    /**
     * Flag that specifies whether the device supports deprecated methods in cases where UPOS specifies that a service
     * may throw an exception with error code E_DEPRECATED. This is the case whenever deprecation started more than 2
     * minor release numbers before the current UPOS release. Since this implementation is for UPOS release 1.14, this
     * affects methods that are deprecated since UPOS version 1.11 or earlier.
     */
    public boolean AllowDeprecatedMethods = false;

    /**
     * Synchronization object for delayed status update event firing.
     */
    SyncObject DelayedStatusUpdateEventWaiter = new SyncObject();

    /**
     * Delayed status update event for later firing.
     */
    DelayedStatusUpdateEvent BufferedEvent = null;

    /**
     * List holding asynchronous output requests whenever service is in error state.
     */
    public List<JposOutputRequest> SuspendedCommands = new ArrayList<JposOutputRequest>();

    /**
     * Currently executed output requests if the service supports concurrent method execution. If a service supports
     * concurrent method execution, it must set CurrentCommands to a non-null value.
     */
    public List<JposOutputRequest> CurrentCommands = null;

    /**
     * List holding asynchronous output requests which allow concurrent processing whenever service is in error state.
     */
    public List<JposOutputRequest> SuspendedConcurrentCommands = new ArrayList<JposOutputRequest>();

    private SyncObject StatusWaiter = null;

    /**
     * Runnable for serialized request processing.
     */
    public Runnable SerializedRequestRunner = null;

    /**
     * Output request runners waiting for service-specific serialization of asynchronous processing.
     */
    public List<Runnable> SerializedRequests = new LinkedList<>();

    /**
     * Event processor. Thread that fires status update events, ouitput complete events and direct IO and transition
     * events that are used for notification only.
     */
    JposBaseDevice.EventFirer EventProcessor = null;

    /**
     * State of asynchronous input processing. Will be set whenever asynchronous input request will be enqueued and
     * reset when the last input operation has been finished.
     */
    public boolean AsyncInputActive = false;


    /**
     * Constructor.
     * @param dev Device index
     */
    protected JposCommonProperties(int dev) {
        Index = dev;
        Claiming = null;
    }

    /**
     * Initialization of properties that must be initialized during open.
     */
    public void initOnOpen() {
        AutoDisable = false;
        DataCount = 0;
        Claimed = false;
        DeviceEnabled = false;
        DataEventEnabled = false;
        FreezeEvents = false;
        PowerState = JposConst.JPOS_PS_UNKNOWN;
        CheckHealthText = "";
        PowerNotify = JposConst.JPOS_PN_DISABLED;
        FlagWhenIdle = false;
        AsyncMode = false;
        ErrorString = "";
        ErrorUnits = 0;
        EventString = "";
        EventUnitID = 0;
        EventUnits = 0;
    }

    /**
     * Initialization of properties that must be initialized during deviceEnable.
     * @param enable True: initialize properties, false: do nothing
     */
    public void initOnEnable(boolean enable) {
        if (enable) {
            initOnFirstEnable();
        }
    }

    /**
     * Initialize properties that must be initialized whenever the device will be enabled the first time-
     * @return Returns true in case of first enable.
     */
    public boolean initOnFirstEnable() {
        boolean res = FirstEnableHappened;
        if (!res) {
            EventList.clear();
            DataEventList.clear();
            FirstEnableHappened = true;
            UnitsOnline = 0;
        }
        return res;
    }

    /**
     * Initialize properties that must be initialized whenever the device will be claimed.
     */
    public void initOnClaim() {}

    /**
     * Clear data properties. Performed during ClearInput and ClearInputProperties.
     */
    public void clearDataProperties() {}

    /**
     * Clear error properties. Performed at the end of any error handling.
     */
    public void clearErrorProperties() {
        EventString = "";
        EventUnits = 0;
    }

    /**
     * Clear output error properties. Performed at the end of any output error handling. Must only be overwritten
     * when different operation is necessary for input and output errors or if the device supports subsystem units.
     */
    public void clearOutputErrorProperties() {
        if (UsesSubsystemUnits) {
            ErrorString = "";
            ErrorUnits = 0;
        }
        else {
            clearErrorProperties();
        }
    }

    /**
     * Add this to the given array of property set lists. Index specifies
     * to which list it will be added.
     * @param props array of property set lists to be used
     */
    public void addProperties(List<JposCommonProperties>[] props) {
        synchronized (props[Index]) {
            if (!props[Index].contains(this)) {
                (DevProps = props[Index]).add(this);
            }
        }
    }

    /**
     * remove this from the list of property sets.
     */
    public void removeFromPropertySetList() {
        if (DevProps != null) {
            synchronized (DevProps) {
                if (DevProps.contains(this)) {
                    DevProps.remove(this);
                    DevProps = null;
                }
            }
        }
    }
    /**
     * Attach SyncObject for status synchronization. Will be attached when a method needs to wait for a status change.
     * After status changed, events derived from JposStatusUpdateEvents that support waiting for status change will
     * signal the SyncObject after updating the corresponding properties. After being woken up, the previously
     * waiting method should release the SyncObject as soon as possible.
     */
    synchronized public void attachWaiter() {
            StatusWaiter = new SyncObject();
    }

    /**
     * Release SyncObject for status synchronization. See attachWaiter for further details.
     */
    synchronized public void releaseWaiter() {
        StatusWaiter = null;
    }

    /**
     * Signals SyncObject for status synchronization.  See attachWaiter for further details.
     */
    synchronized public void signalWaiter() {
        if (StatusWaiter != null)
            StatusWaiter.signal();
    }

    /**
     * Suspend thread until SyncObject for status synchronization has been signalled.
     * See attachWaiter for further details.
     * @param timeout Maximum time to wait for status status change
     * @return true when signalled, false otherwise (timeout / no SyncObject present)
     */
    public boolean waitWaiter(long timeout) {
        if (StatusWaiter != null)
            return StatusWaiter.suspend(timeout);
        return false;
    }

    /**
     * Retrieves status waiter and resets it to null. The calling method must ensure that the status waiter
     * will be signalled later to avoid deadlock situations.
     * @return  attached SyncObject if attached, null otherwise.
     */
    synchronized SyncObject retrieveWaiter() {
        return StatusWaiter;
    }

    /**
     * Retrieves the property set of the service instance that claims the device.
     * @return property set of claiming instance, null if no instance claims the device.
     */
    public JposCommonProperties getClaimingInstance() {
        return Device.getClaimingInstance(Claiming, Index);
    }

    /*
     * Methods that implement common property setter and common method calls
     */

    @Override
    public void deviceEnabled(boolean enable) throws JposException {
        initOnEnable(enable);
        DeviceEnabled = enable;
    }

    @Override
    public void freezeEvents(boolean freezeEvents) throws JposException {
        if (!(FreezeEvents = freezeEvents)) {
            synchronized (EventList) {
                Device.processEventList(this);
            }
        }
    }

    @Override
    public void powerNotify(int powerNotify) throws JposException {
        PowerNotify = powerNotify;
    }

    @Override
    public void autoDisable(boolean b) throws JposException {
        AutoDisable = b;
    }

    @Override
    public void asyncMode(boolean b) throws JposException {
        AsyncMode = b;
    }

    @Override
    public void dataEventEnabled(boolean b) throws JposException {
        if (DataEventEnabled = b) {
            synchronized (EventList) {
                Device.processEventList(this);
            }
        }
    }

    @Override
    public void flagWhenIdle(boolean b) throws JposException {
        synchronized (Device.AsyncProcessorRunning) {
            if (FlagWhenIdle = b) {
                if (State == JposConst.JPOS_S_IDLE) {
                    Device.handleEvent(new JposStatusUpdateEvent(EventSource, FlagWhenIdleStatusValue));
                }
            }
        }
    }

    @Override
    public int unitDataCount() {
        int count = 0;
        synchronized(DataEventList) {
            for (JposEvent ev : DataEventList) {
                if (ev instanceof UnitDataEvent && ((UnitDataEvent)ev).Unit == CurrentUnitID) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public void claim(int timeout) throws JposException {
        initOnClaim();
        Claimed = true;
    }

    @Override
    public void close() throws JposException {
        State = JposConst.JPOS_S_CLOSED;
    }

    @Override
    public void checkHealth(int level) throws JposException {
    }

    @Override
    public DirectIO directIO(int command, int[] data, Object object) throws JposException {
        Method asyncdio = null;
        DirectIO retval = null;
        try {
            asyncdio = EventSource.DeviceInterface.getClass().getMethod("directIO", DirectIO.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        retval = new DirectIO(this, command, data, object);

        if (asyncdio == null || asyncdio.getDeclaringClass() == JposCommonProperties.class)
            return null;
        return retval;
    }

    @Override
    public void directIO(DirectIO request) throws JposException {
    }

    @Override
    public void open() throws JposException {
        initOnOpen();
        State = JposConst.JPOS_S_IDLE;
    }

    @Override
    public void release() throws JposException {
        Claimed = false;
        if (ExclusiveUse == JposCommonProperties.ExclusiveYes) {
            synchronized (EventList) {
                EventList.clear();
                DataEventList.clear();
                DataCount = 0;
            }
        }
        newJposOutputRequest().clearAll();
        if (State != JposConst.JPOS_S_IDLE) {
            State = JposConst.JPOS_S_IDLE;
            EventSource.logSet("State");
        }
        if (FlagWhenIdle) {
            FlagWhenIdle = false;
            EventSource.logSet("FlagWhenIdle");
        }
    }

    @Override
    public void clearInput() throws JposException {
        synchronized (EventList) {
            if (UsesSubsystemUnits) {
                for (List<JposEvent> list : new List[]{EventList, DataEventList}) {
                    for (int i = 0; i < list.size(); ) {
                        JposEvent ev = list.get(i);
                        i = conditionalDataEventRemoval(list, CurrentUnitID, i, ev);
                        i = conditionalInputErrorEventRemoval(list, CurrentUnitID, i, ev);
                    }
                }
            } else {
                DataEventList.clear();
                for (int i = 0; i < EventList.size();) {
                    JposEvent ev = EventList.get(i);
                    if (ev instanceof JposDataEvent)
                        EventList.remove(ev);
                    else if (ev instanceof JposErrorEvent && ((JposErrorEvent) ev).getErrorLocus() != JposConst.JPOS_EL_OUTPUT)
                        EventList.remove(ev);
                    else
                        i++;
                }
                DataCount = 0;
            }
            // See UPOS spec, chapter 2.5.3. clearInput Method: Clears all device input that has been
            // buffered. ... events that are enqueued ... are also cleared. This implies that clearInput must clear all
            // buffered data and input error events as well as other buffered input stuff. Therefore, it is good
            // practice to clear JposInputRequest objects from request buffers as well as follows:
            newJposOutputRequest().clearInput();
        }
        Device.processEventList(this);
        State = JposConst.JPOS_S_IDLE;
    }

    private int conditionalInputErrorEventRemoval(List<JposEvent> list, int bit, int i, JposEvent ev) {
        if (ev instanceof UnitInputErrorEvent) {
            UnitInputErrorEvent event = (UnitInputErrorEvent) ev;
            if ((event.Units & bit) != 0) {
                list.remove(i);
            }
            else {
                i++;
            }
        }
        return i;
    }

    private int conditionalDataEventRemoval(List<JposEvent> list, int bit, int i, JposEvent ev) {
        if (ev instanceof UnitDataEvent) {
            UnitDataEvent event = (UnitDataEvent) ev;
            if ((event.Unit & bit) != 0) {
                list.remove(i);
                DataCount--;
            }
            else {
                i++;
            }
        }
        return i;
    }

    @Override
    public void retryInput() throws JposException {
        State = JposConst.JPOS_S_IDLE;
        EventSource.logSet("State");
        clearErrorProperties();
        Device.log(Level.DEBUG, LogicalName + ": Enter Retry input...");
    }

    @Override
    public JposOutputRequest newJposOutputRequest() {
        return new JposOutputRequest(this);
    }

    @Override
    public void clearOutput() throws JposException {
        if (UsesSubsystemUnits) {
            synchronized(EventList) {
                for (int i = 0; i < EventList.size();) {
                    i = conditionalOutputErrorEventRemoval(CurrentUnitID, i);
                    i = conditionalOutputCompleteEventRemoval(CurrentUnitID, i);
                }
            }
            UnitOutputRequest checker = new UnitOutputRequest(this, CurrentUnitID);
            checker.clearOutput();
            int remainingCommands = checker.countCommands();
            if (State != JposConst.JPOS_S_IDLE && remainingCommands == 0) {
                State = JposConst.JPOS_S_IDLE;
                Device.log(Level.DEBUG, LogicalName + ": State <- " + JposConst.JPOS_S_IDLE);
            }
            else if (State != JposConst.JPOS_S_BUSY && remainingCommands != 0) {
                State = JposConst.JPOS_S_BUSY;
                Device.log(Level.DEBUG, LogicalName + ": State <- " + JposConst.JPOS_S_BUSY);
            }
            if (FlagWhenIdle && remainingCommands != 0) {
                FlagWhenIdle = false;
                Device.log(Level.DEBUG, LogicalName + ": FlagWhenIdle <- " + FlagWhenIdleStatusValue);
                Device.handleEvent(new JposStatusUpdateEvent(EventSource, FlagWhenIdleStatusValue));
            }
        }
        else {
            synchronized (EventList) {
                for (int i = 0; i < EventList.size();) {
                    JposEvent ev = EventList.get(i);
                    if (ev instanceof JposErrorEvent && ((JposErrorEvent) ev).getErrorLocus() == JposConst.JPOS_EL_OUTPUT)
                        EventList.remove(i);
                    else
                        ++i;
                }
            }
            newJposOutputRequest().clearOutput();
            if (State != JposConst.JPOS_S_IDLE) {
                State = JposConst.JPOS_S_IDLE;
                EventSource.logSet("State");
            }
            if (FlagWhenIdle) {
                FlagWhenIdle = false;
                EventSource.logSet("FlagWhenIdle");
                Device.handleEvent(new JposStatusUpdateEvent(EventSource, FlagWhenIdleStatusValue));
            }
            else
                Device.processEventList(this);
        }
    }

    private int conditionalOutputErrorEventRemoval(int bit, int i) {
        JposEvent event = EventList.get(i);
        if (event instanceof UnitOutputErrorEvent && (((UnitOutputErrorEvent) event).Units & bit) != 0)
            EventList.remove(i);
        else
            i++;
        return i;
    }

    private int conditionalOutputCompleteEventRemoval(int bit, int i) {
        JposEvent event = EventList.get(i);
        if (event instanceof UnitOutputCompleteEvent && (((UnitOutputCompleteEvent) event).Units & bit) != 0)
            EventList.remove(i);
        else
            i++;
        return i;
    }

    @Override
    public void retryOutput() throws JposException {
        new JposOutputRequest(this).reactivate();
        Device.log(Level.DEBUG, LogicalName + ": Enter Retry output...");
    }

    @Override
    public void compareFirmwareVersion(String firmwareFileName, int[] result) throws JposException {
    }

    @Override
    public UpdateFirmware updateFirmware(String firmwareFileName) throws JposException {
        return new UpdateFirmware(this, firmwareFileName);
    }

    @Override
    public void updateFirmware(UpdateFirmware request) throws JposException {
    }

    @Override
    public void resetStatistics(String statisticsBuffer) throws JposException {
    }

    @Override
    public void retrieveStatistics(String[] statisticsBuffer) throws JposException {
    }

    @Override
    public void updateStatistics(String statisticsBuffer) throws JposException {
    }

    @Override
    public void handlePowerStateOnEnable() throws JposException {
        Device.handlePowerStateOnEnable(this);
    }
}

