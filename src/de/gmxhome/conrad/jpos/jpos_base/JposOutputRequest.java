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

import java.util.*;

import static de.gmxhome.conrad.jpos.jpos_base.SyncObject.INFINITE;
import static jpos.JposConst.*;

/**
 * Class to invoke a method asynchronously. Contains all method parameters, a JposDevice
 * object and a property set as
 * properties and an invoke method that calls the corresponding method.<br>
 * For each method that can be invoked asynchronously, one class must be derived that
 * overwrites the invoke method.<br>
 * To abort a command, call method abortCommand from a different thread. Method abortCommand sets
 * the Abort SyncObject that can be used by a running command to check whether it shall
 * finish.
 * The Run method implements the command processor.
 */
public class JposOutputRequest implements Runnable {
    /**
     * Property set of device service that started asynchronous processing.
     */
    public JposCommonProperties Props;
    /**
     * Device implementation of device service that started asynchronous processing.
     */
    protected JposDevice Device;
    /**
     * OutputID of the asynchronous command.
     */
    public int OutputID;

    /**
     * If set, command shall be aborted as soon as possible. Use abortCommand() to force
     * command abort.
     */
    public SyncObject Abort = null;

    /**
     * For synchronous processing, this object can be used to signal termination of the
     * command invocation.
     */
    public SyncObject EndSync = null;

    /**
     * If true, the command has been finished. Must be set to true whenever command processing
     * finishes.
     */
    protected boolean Finished = false;

    /**
     * Object to be used whenever the command must sleep some time. Will be signaled by aborting
     * thread to slow down waiting time.
     */
    public SyncObject Waiting = new SyncObject();

    /**
     * If set, the exception that terminated command execution. Otherwise, the command terminated
     * normally.
     */
    public JposException Exception = null;

    /**
     * Additional request data. It may contain any request specific data at the time the method was invoked that must be
     * buffered until asynchronous request processing really starts. This might be specific status values or other
     * device specific data.
     */
    public Object AdditionalData = null;

    /**
     * Resets the object to a pre-run status,
     */
    synchronized public void reset() {
        if (Abort != null)
            Abort.signal();
        Abort = null;
        EndSync = null;
        Finished = false;
        Waiting.reset();
        Exception = null;
    }

    /**
     * Force command to abort processing. Waits until command has been finished.
     */
    public void abortCommand() {
        abortCommand(false);
    }

    /**
     * Force command to abort processing. Waits until command has been finished.
     * @param noEvents  If true, OutputCompleteEvents and ErrorEvents will be suppressed.
     */
    public void abortCommand(boolean noEvents) {
        if (noEvents)
            NoEvents = true;
        SyncObject waiter = new SyncObject();
        synchronized(this) {
            Abort = waiter;
            if (Finished)
                waiter = null;
            else
                Waiting.signal();
        }
        if (waiter != null) {
            waiter.suspend(INFINITE);
        }
    }

    /**
     * Must be called at the end of each invoke method. If another thread is waiting for
     * abort completion or termination, this thread will be woken up.
     */
    public synchronized void finished() {
        Finished = true;
        if (Abort != null)
            Abort.signal();
    }

    /**
     * Constructor for internal use. Generates dummy request to be used only to create a request executor thread.
     * @param device Device implementation object.
     */
    public JposOutputRequest(JposDevice device) {
        Props = null;
        Device = device;
    }

    /**
     * Constructor. Stores given parameters for later use.
     * @param props Property set of device service.
     */
    public JposOutputRequest(JposCommonProperties props) {
        Props = props;
        Device = props.Device;
    }

    /**
     * Invokes the command. Must be implemented in derived class. Calls finished()
     * to wake up potentially waiting threads.
     * @throws JposException JposException thrown by the command to be executed.
     */
    public void invoke() throws JposException {
    }

    /**
     * Helper thread for asynchronous method processing. One thread per device, even if the device supports multiple
     * device classes. Will not be used for methods that support concurrent execution, see JposBaseDevice -
     * createRequestThread.
     */
    static public class JposRequestThread extends Thread {
        /**
         * List containing all active message box objects.
         */
        public static final List<SynchronizedMessageBox> ActiveMessageBoxes = new ArrayList<>();

        /**
         * Constructor, creates asynchronous request handler thread for the specified request using the specified request
         * runner.
         * @param runner   Runnable to be used for request handling.
         * @param req      Request to be handled.
         */
        public JposRequestThread(Runnable runner, JposOutputRequest req) {
            super(runner);
            String name = runner == req ? req.Device.ID : req.Props.LogicalName;
            if (! (runner instanceof  JposOutputRequest)) {
                name += "." + req.getClass().getSimpleName();
                if (req.OutputID != 0)
                    name += "." + req.OutputID;
            }
            setName(name  + ".AsyncRequestExecutor");
        }
    }

    /**
     * Puts output request into queue of pending commands. If not active,
     * a new request processor will be activated.
     * @throws JposException if device is just in error state
     */
    public void enqueue() throws JposException {
        int state;
        synchronized (Device.AsyncProcessorRunning) {
            state = Props.State;
            if (EndSync == null) {
                if (state == JPOS_S_IDLE)
                    Props.State = JPOS_S_BUSY;
                OutputID = (Props.OutputID = (Props.OutputID + 1) % Integer.MAX_VALUE);
                Props.EventSource.logSet("OutputID");
            }
            Boolean concurrent = Device.concurrentProcessingSupported(this);
            if (concurrent == null || concurrent) {
                if (state == JPOS_S_ERROR)
                    Props.SuspendedConcurrentCommands.add(this);
                else
                    Device.createConcurrentRequestThread(this);
            } else if (state == JPOS_S_ERROR)
                Props.SuspendedCommands.add(this);
            else
                Device.invokeRequestThread(this, null);
        }
        if (state != Props.State)
            Props.EventSource.logSet("State");
    }

    /**
     * Enqueue request for synchronous processing. No JposErrorEvent handling, but exception
     * handling will take place.
     * @throws JposException JposException thrown within command execution thread.
     */
    public void enqueueSynchronous() throws JposException {
        EndSync = new SyncObject();
        enqueue();
        EndSync.suspend(INFINITE);
        JposException ex = Exception;
        reset();
        if (ex != null)
            throw ex;
    }

    /**
     * Removes output request from queue of pending commands.
     * @return First output request in request queue, null if request queue is empty.
     */
    protected JposOutputRequest dequeue() {
        JposOutputRequest result = null;
        synchronized (Device.AsyncProcessorRunning) {
            if (Device.PendingCommands.size() > 0) {
                result = Device.PendingCommands.get(0);
                Device.PendingCommands.remove(0);
            } else
                Device.AsyncProcessorRunning[0] = null;
            Device.CurrentCommand = result;
        }
        return result;
    }

    /**
     * Checks whether the current request is the only active request belonging to the same property set. Keep in mind:
     * Synchronization will be necessary to avoid concurrent access to lists Device.PendingCommands and
     * Device.CurrentCommands!
     * @return false if another request is active or enqueued, true otherwise.
     */
    private boolean lastRequest() {
        for (JposOutputRequest req : Device.PendingCommands) {
            if (req.Props == Props)
                return false;
        }
        if (Props.CurrentCommands != null) {
            for (JposOutputRequest req : Props.CurrentCommands) {
                if (req != this)
                    return false;
            }
        }
        return Props.SuspendedCommands.size() == 0 && Props.SuspendedConcurrentCommands.size() == 0;
    }

    /**
     * Removes all pending and suspended input requests belonging to the property set of the command. Clears only those
     * requests that are really input requests:
     * <ul>
     *     <li>Have no own OutputID,</li>
     *     <li>Generate no OutputCompleteEvent when finished,</li>
     *     <li>Generate error events with locus EL_INPUT.</li>
     * </ul>
     * This base implementation expects that no output request fulfills these requirements and therefore simply
     * makes nothing.
     */
    public void clearInput() {
        int i = 0;
        List<JposOutputRequest> current = new ArrayList<>();
        JposOutputRequest request;
        int state = Props.State;
        synchronized (Device.AsyncProcessorRunning) {
            while (i < Props.SuspendedCommands.size()) {
                if (Props.SuspendedCommands.get(i) instanceof JposInputRequest) {
                    Props.SuspendedCommands.remove(i);
                } else {
                    i++;
                }
            }
            i = 0;
            while (i < Props.SuspendedConcurrentCommands.size()) {
                if (Props.SuspendedConcurrentCommands.get(i) instanceof JposInputRequest) {
                    Props.SuspendedConcurrentCommands.remove(i);
                } else {
                    i++;
                }
            }
            i = 0;
            while (i < Device.PendingCommands.size()) {
                if ((request = Device.PendingCommands.get(i)).Props == Props && request instanceof JposInputRequest) {
                    Device.PendingCommands.remove(i);
                } else {
                    i++;
                }
            }
            if (Device.CurrentCommand instanceof JposInputRequest)
                current.add(Device.CurrentCommand);
            if (Props.CurrentCommands != null) {
                for (JposOutputRequest req : Props.CurrentCommands) {
                    if (req instanceof JposInputRequest)
                        current.add(req);
                }
            }
            if (current.size() == 0) {
                Props.AsyncInputActive = false;
                if (Props.State == JPOS_S_ERROR && Props.SuspendedCommands.size() + Props.SuspendedConcurrentCommands.size() == 0)
                    Props.State = JPOS_S_IDLE;
            }
        }
        if (state != Props.State)
            Props.EventSource.logSet("State");
        for (JposOutputRequest req : current)
            req.abortCommand();
    }

    /**
     * Removes all pending and suspended output requests belonging to the property set of the command. Clears only those
     * requests that are really output requests:
     * <ul>
     *     <li>Have an own OutputID,</li>
     *     <li>Generate an OutputCompleteEvent when finished,</li>
     *     <li>Generate error events with locus EL_OUTPUT.</li>
     * </ul>
     * This base implementation expects that all output requests fulfill these requirements and therefore simply
     * invokes clearAll.
     */
    public void clearOutput() {
        int i = 0;
        List<JposOutputRequest> current = new ArrayList<>();
        int state = Props.State;
        synchronized (Device.AsyncProcessorRunning) {
            while (i < Props.SuspendedCommands.size()) {
                if (!(Props.SuspendedCommands.get(i) instanceof JposInputRequest)) {
                    Props.SuspendedCommands.remove(i);
                } else {
                    i++;
                }
            }
            i = 0;
            while (i < Props.SuspendedConcurrentCommands.size()) {
                if (!(Props.SuspendedConcurrentCommands.get(i) instanceof JposInputRequest)) {
                    Props.SuspendedConcurrentCommands.remove(i);
                } else {
                    i++;
                }
            }
            i = 0;
            while (i < Device.PendingCommands.size()) {
                JposOutputRequest req;
                if ((req = Device.PendingCommands.get(i)).Props == Props && !(req instanceof JposInputRequest)) {
                    Device.PendingCommands.remove(i);
                } else {
                    i++;
                }
            }
            if (!(Device.CurrentCommand instanceof JposInputRequest)) {
                JposOutputRequest req = Device.CurrentCommand;
                if (req != null && req.Props == Props)
                    current.add(Device.CurrentCommand);
            }
            if (Props.CurrentCommands != null) {
                for (JposOutputRequest request : Props.CurrentCommands) {
                    if (!(request instanceof JposInputRequest) && request != null)
                        current.add(request);
                }
            }
            if (current.size() == 0) {
                Props.State = Props.SuspendedCommands.size() + Props.SuspendedConcurrentCommands.size() == 0 ?
                        JPOS_S_IDLE : JPOS_S_ERROR;
            }
        }
        if (state != Props.State)
            Props.EventSource.logSet("State");
        for (JposOutputRequest req : current)
            req.abortCommand(true);
    }

    /**
     * Removes all pending and suspended output requests belonging to the property set of the command.
     */
    public void clearAll() {
        int i = 0;
        List<JposOutputRequest> current = new ArrayList<>();
        int state = Props.State;
        synchronized (Device.AsyncProcessorRunning) {
            Props.SuspendedCommands.clear();
            Props.SuspendedConcurrentCommands.clear();
            while (i < Device.PendingCommands.size()) {
                if (Device.PendingCommands.get(i).Props == Props) {
                    Device.PendingCommands.remove(i);
                } else {
                    i++;
                }
            }
            if (Device.CurrentCommand != null && Device.CurrentCommand.Props == Props) {
                current.add(Device.CurrentCommand);
            }
            if (Props.CurrentCommands != null) {
                current.addAll(Props.CurrentCommands);
            }
            if (current.size() == 0) {
                Props.State = JPOS_S_IDLE;
                Props.AsyncInputActive = false;
            }
        }
        if (state != Props.State)
            Props.EventSource.logSet("State");
        for (JposOutputRequest req : current)
            req.abortCommand(!(req instanceof JposInputRequest));
    }

    /**
     * Computes number of all commands enqueued or suspended.
     * @return Computed amount.
     */
    public int countCommands() {
        int count = 0;
        synchronized (Device.AsyncProcessorRunning) {
            for (JposOutputRequest command : Device.PendingCommands) {
                if (command.Props == Props)
                    count++;
            }
            JposOutputRequest current = Device.CurrentCommand;
            if (current != null && current.Props == Props)
                count++;
            if (Props.CurrentCommands != null) {
                count += Props.CurrentCommands.size();
            }
            count += Props.SuspendedCommands.size() + Props.SuspendedConcurrentCommands.size();
        }
        return count;
    }

    /**
     * Moves the all output requests belonging to the same property set to the
     * suspended command list of the property set.
     */
    private void suspend() {
        int i = 0;
        int state;
        synchronized (Device.AsyncProcessorRunning) {
            state = Props.State;
            JposOutputRequest request = Device.CurrentCommand;
            if (request != null && request.Props == Props) {
                Props.SuspendedCommands.add(request);
                Device.CurrentCommand = null;
                request.reset();
                Props.State = JPOS_S_ERROR;
            }
            if (Props.CurrentCommands != null) {
                while (Props.CurrentCommands.size() > 0) {
                    request = Props.CurrentCommands.get(0);
                    Props.SuspendedConcurrentCommands.add(request);
                    Props.CurrentCommands.remove(request);
                    if (request == this)
                        request.reset();
                    else
                        request.abortCommand(true);
                    Props.State = JPOS_S_ERROR;
                }
            }
            while (i < Device.PendingCommands.size()) {
                if (Device.PendingCommands.get(i).Props == Props) {
                    Props.SuspendedCommands.add(Device.PendingCommands.get(i));
                    Device.PendingCommands.remove(i);
                } else {
                    i++;
                }
            }
        }
        if (state != Props.State)
            Props.EventSource.logSet("State");
    }

    /**
     * Reactivate only those commands that are or are not derived from JposInputRequest. This default implementation
     * expects that no request has been derived from JposInputRequest.
     * @param input If true, only JposInputRequests will be reactivated. Otherwise only all other
     *              JposOutputRequests will be reactivated.
     */
    public void reactivate(boolean input) {
        new JposInputRequest(Props).reactivate(input);
    }

    /**
     * Reactivate previously suspended requests: Add them to PendingCommands and start handler thread.
     */
    public void reactivate() {
        synchronized (Device.AsyncProcessorRunning) {
            while (Props.SuspendedCommands.size() > 0) {
                JposOutputRequest current = Props.SuspendedCommands.get(0);
                if (!(current instanceof JposInputRequest) && Props.State != JPOS_S_BUSY) {
                    Props.State = JPOS_S_BUSY;
                    Props.EventSource.logSet("State");
                }
                Props.SuspendedCommands.remove(0);
                Device.invokeRequestThread(current, null);
            }
            while (Props.SuspendedConcurrentCommands.size() > 0) {
                JposOutputRequest current = Props.SuspendedConcurrentCommands.get(0);
                if (!(current instanceof JposInputRequest) && Props.State != JPOS_S_BUSY) {
                    Props.State = JPOS_S_BUSY;
                    Props.EventSource.logSet("State");
                }
                Props.SuspendedConcurrentCommands.remove(0);
                Device.createConcurrentRequestThread(current);
            }
        }
    }

    @Override
    public void run() {
        JposOutputRequest current;
        while ((current = dequeue()) != null) {
            current.catchedInvocation();
            if (current.EndSync != null) {
                synchronized (Device.AsyncProcessorRunning) {
                    current.finished();
                    transitionToIdle();
                }
                current.EndSync.signal();
            } else
                current.finishAsyncProcessing();
        }
    }

    /**
     * Catches exceptions thrown by method invoke() and stores it in property Exception for further processing by
     * asynchronous request handlers.
     */
    public void catchedInvocation() {
        try {
            invoke();
        } catch (JposException e) {
            Exception = e;
        } catch (Throwable e) {
            Exception = new JposException(JPOS_E_FAILURE, e.getMessage(), e instanceof Exception ?
                    (Exception) e : new Exception(e));
            e.printStackTrace();
        }
    }

    /**
     * Specifies whether OutputCompleteEvent or ErrorEvent shall be suppressed after command finalization. Will be
     * set in current commands whenever they will be discarded due to ClearOutput or ClearInput processing.
     */
    boolean NoEvents = false;

    /**
     * Finishs asynchronous processing of a request.
     * @return true if request has been processed, false if suspended.
     */
    public boolean finishAsyncProcessing() {
        boolean processed = false;
        try {
            if (Abort != null)
                Abort.signal();
            if (NoEvents) {
                NoEvents = false;
                reset();
                processed = true;
            } else if (Exception != null) {
                JposErrorEvent event = createErrorEvent(Exception);
                if (event == null) {
                    synchronized (Device.AsyncProcessorRunning) {
                        finished();
                        Props.FlagWhenIdle = true;
                        Props.EventSource.logSet("FlagWhenIdle");
                        processed = true;
                    }
                } else {
                    suspend();
                    Device.handleEvent(event);
                }
            } else {
                synchronized (Device.AsyncProcessorRunning) {
                    finished();
                    JposOutputCompleteEvent ocevent = createOutputEvent();
                    if (ocevent != null) {
                        Device.handleEvent(ocevent);
                    }
                    processed = true;
                }
            }
        } catch (Throwable e1) {
            e1.printStackTrace();
            if (!(e1 instanceof JposException))
                processed = true;
        }
        if (processed)
            transitionToIdle();
        return processed;
    }

    private void transitionToIdle() {
        JposStatusUpdateEvent event = null;
        synchronized (Device.AsyncProcessorRunning) {
            if (lastRequest()) {
                if (Props.State == JPOS_S_BUSY) {
                    Props.State = JPOS_S_IDLE;
                    Props.EventSource.logSet("State");
                }
                if (Props.FlagWhenIdle) {
                    Props.FlagWhenIdle = false;
                    Props.EventSource.logSet("FlagWhenIdle");
                    synchronized (Device.AsyncProcessorRunning) {
                        JposOutputRequest savedCommand = Device.CurrentCommand;
                        try {
                            event = (Device.CurrentCommand = this).createIdleEvent();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        Device.CurrentCommand = savedCommand;
                    }
                }
            }
        }
        if (event != null) {
            try {
                Device.handleEvent(event);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Factory for error events generated from JposExceptions. Must be overwritten whenever a device specific error
     * event shall be created. For example, in case of cash printer methods, this method should return a
     * POSPrinterErrorEvent (which is an object derived from JposErrorEvent) that contains additional values to be
     * stored in printer properties before the event will be fired.<br>
     * If a device supports result code properties instead of error events, this method must return null.
     * The result codes should be buffered for a later call of the createIdleEvent method which must create a
     * device specific StatusUpdateEvent which contains the buffered values.<br>
     * If null will be returned instead of a JposErrorEvent, it will enforce special request handling instead:<ul>
     *     <li>Instead of suspending the request, it will be finished.</li>
     *     <li>The idle flag will be set.</li>
     * </ul>
     * Devices handling asynchronous requests this way, must not buffer more than one request at once. They must end
     * with throwing a StatusUpdateEvent with a specific end-of-request status value instead. This event must be
     * returned by the createIdleEvent method of a device specific class derived from JposOutputRequest.
     *
     * @param ex JposException which is the originator of an error event.
     * @return  The resulting error event.
     */
    public JposErrorEvent createErrorEvent(JposException ex) {
        return new JposErrorEvent(Props.EventSource, ex.getErrorCode(), ex.getErrorCodeExtended(), JPOS_EL_OUTPUT, ex.getMessage());
    }

    /**
     * Factory for output complete events. Must be overwritten whenever a device specific output complete event
     * shall be created.
     * @return  The resulting output complete event or null if no output complete event shall be enqueued.
     */
    public JposOutputCompleteEvent createOutputEvent() {
        return new JposOutputCompleteEvent(Props.EventSource, OutputID);
    }

    /**
     * Factory for status update event with FlagWhenIdle status value. Must be overwritten whenever a device specific
     * status update event shall be created.<br>
     * If the createErrorEvent method has been overwritten with a method that returns an error event with
     * source = null to enforce special error handling via result properties instead of error events, createIdleEvent
     * must be overwritten as well.
     * 
     * @return  The resulting status update event.
     */
    public JposStatusUpdateEvent createIdleEvent() {
        return Props.FlagWhenIdleStatusValue == null ? null :
                new JposStatusUpdateEvent(Props.EventSource, Props.FlagWhenIdleStatusValue);
    }
}
