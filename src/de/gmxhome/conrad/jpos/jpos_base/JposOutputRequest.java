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
        while (Waiting.suspend(0));
        Exception = null;
    }
    /**
     * Force command to abort processing. Waits until command has been finished.
     */
    public void abortCommand() {
        SyncObject waiter = new SyncObject();
        synchronized(this) {
            Abort = waiter;
            if (Finished)
                waiter = null;
            else
                Waiting.signal();
        }
        if (waiter != null) {
            waiter.suspend(SyncObject.INFINITE);
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
     * Puts output request into queue of pending commands. If not active,
     * a new request processor will be activated.
     * @throws JposException if device is just in error state
     */
    public void enqueue() throws JposException {
        int state;
        synchronized (Device.AsyncProcessorRunning) {
            state = Props.State;
            if (state == JposConst.JPOS_S_IDLE)
                Props.State = JposConst.JPOS_S_BUSY;
            OutputID = (Props.OutputID = (Props.OutputID + 1) % Integer.MAX_VALUE);
            Props.EventSource.logSet("OutputID");
            if (state == JposConst.JPOS_S_ERROR) {
                Props.SuspendedCommands.add(this);
            }
            else {
                Device.PendingCommands.add(this);
                if (!Device.AsyncProcessorRunning[0]) {
                    Device.AsyncProcessorRunning[0] = true;
                    Thread handler = new Thread(new JposOutputRequest(Device));
                    handler.setName("OutputRequestExecutor");
                    handler.start();
                }
            }
        }
        if (state != Props.State)
            Props.EventSource.logSet("State");
    }

    /**
     * Enqueue request for synchronous processing. No JposEvent handling, but exception
     * handling will take place.
     * @throws JposException JposException thrown within command execution thread.
     */
    public void enqueueSynchronous() throws JposException {
        EndSync = new SyncObject();
        enqueue();
        EndSync.suspend(SyncObject.INFINITE);
        JposException ex = Exception;
        synchronized (Device.AsyncProcessorRunning) {
            reset();
            if (Device.PendingCommands.size() == 0 && Props.State == JposConst.JPOS_S_BUSY) {
                Props.State = ex == null ? JposConst.JPOS_S_IDLE : JposConst.JPOS_S_ERROR;
                Props.EventSource.logSet("State");
            }
        }
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
                Device.AsyncProcessorRunning[0] = false;
            Device.CurrentCommand = result;
        }
        return result;
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
    public void clearInput() {}

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
        clearAll();
    }

    /**
     * Removes all pending and suspended output requests belonging to the property set of the command.
     */
    public void clearAll() {
        int i = 0;
        JposOutputRequest current;
        synchronized (Device.AsyncProcessorRunning) {
            Props.SuspendedCommands.clear();
            while (i < Device.PendingCommands.size()) {
                if (Device.PendingCommands.get(i).Props == Props) {
                    Device.PendingCommands.remove(i);
                } else {
                    i++;
                }
            }
            current = Device.CurrentCommand;
        }
        if (current != null && current.Props == Props) {
            current.abortCommand();
        }
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
            count += Props.SuspendedCommands.size();
        }
        return count;
    }

    /**
     * Moves the all output requests belonging to the same property set to the
     * suspended command list of the property set.
     * @param current If true, the current command will be added to suspended command list as well.
     */
    void suspend(boolean current) {
        int i = 0;
        int state;
        synchronized (Device.AsyncProcessorRunning) {
            state = Props.State;
            if (current) {
                JposOutputRequest request = Device.CurrentCommand;
                Props.SuspendedCommands.add(request);
                Device.CurrentCommand = null;
                request.reset();
                Props.State = JposConst.JPOS_S_ERROR;
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
        reactivate();
    }

    /**
     * Reactivate previously suspended requests: Add them to PendingCommands and start handler thread.
     */
    public void reactivate() {
        synchronized (Device.AsyncProcessorRunning) {
            while (Props.SuspendedCommands.size() > 0) {
                JposOutputRequest current = Props.SuspendedCommands.get(0);
                if (!(current instanceof JposInputRequest) && Props.State != JposConst.JPOS_S_BUSY) {
                    Props.State = JposConst.JPOS_S_BUSY;
                    Props.EventSource.logSet("State");
                }
                Device.PendingCommands.add(current);
                Props.SuspendedCommands.remove(0);
            }
            if (Device.PendingCommands.size() > 0 && !Device.AsyncProcessorRunning[0]) {
                Device.AsyncProcessorRunning[0] = true;
                new Thread(this).start();
            }
        }
    }

    @Override
    public void run() {
        JposOutputRequest current;
        JposOutputRequest thelastone = null;
        while ((current = dequeue()) != null) {
            try {
                current.invoke();
            } catch (JposException e) {
                current.Exception = e;
            } catch (Exception e) {
                current.Exception = new JposException(JposConst.JPOS_E_FAILURE, e.getMessage(), e);
                e.printStackTrace();
            }
            if (current.EndSync != null) {
                synchronized (Device.AsyncProcessorRunning) {
                    current.finished();
                    if (Device.PendingCommands.size() == 0 && current.Props.SuspendedCommands.size() == 0) {
                        current.Props.State = JposConst.JPOS_S_IDLE;
                        current.Props.EventSource.logSet("State");
                    }
                }
                current.EndSync.signal();
            } else {
                try {
                    if (current.Exception != null) {
                        JposErrorEvent event = current.createErrorEvent(current.Exception);
                        if (event == null) {
                            synchronized (Device.AsyncProcessorRunning) {
                                current.finished();
                                current.Props.FlagWhenIdle = true;
                                current.Props.EventSource.logSet("FlagWhenIdle");
                                thelastone = current;
                            }
                        } else {
                            current.suspend(true);
                            Device.handleEvent(event);
                        }
                    } else {
                        synchronized (Device.AsyncProcessorRunning) {
                            current.finished();
                            JposOutputCompleteEvent ocevent = current.createOutputEvent();
                            if (ocevent != null) {
                                Device.handleEvent(ocevent);
                            }
                            if (current.OutputID == current.Props.OutputID) {
                                if (current.Props.State == JposConst.JPOS_S_BUSY) {
                                    current.Props.State = JposConst.JPOS_S_IDLE;
                                    current.Props.EventSource.logSet("State");
                                }
                            }
                            thelastone = current;
                        }
                    }
                } catch (JposException e1) {
                }
            }
        }
        checkIdle(thelastone);
    }

    private void checkIdle(JposOutputRequest thelastone) {
        if (thelastone != null) {
            if (thelastone.Props.State == JposConst.JPOS_S_BUSY) {
                thelastone.Props.State = JposConst.JPOS_S_IDLE;
                thelastone.Props.EventSource.logSet("State");
            }
            if (thelastone.Props.FlagWhenIdle) {
                thelastone.Props.FlagWhenIdle = false;
                thelastone.Props.EventSource.logSet("FlagWhenIdle");
                JposStatusUpdateEvent event;
                synchronized (Device.AsyncProcessorRunning) {
                    JposOutputRequest savedCommand = Device.CurrentCommand;
                    event = (Device.CurrentCommand = thelastone).createIdleEvent();
                    Device.CurrentCommand = savedCommand;
                }
                try {
                    Device.handleEvent(event);
                } catch (JposException e) {
                }
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
        return new JposErrorEvent(Props.EventSource, ex.getErrorCode(), ex.getErrorCodeExtended(), JposConst.JPOS_EL_OUTPUT, ex.getMessage());
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
        return new JposStatusUpdateEvent(Props.EventSource, Props.FlagWhenIdleStatusValue);
    }

    /**
     * Helper class for methods where support for asynchronous operation has been added. Since such final parts of a
     * method typically cannot return an object derived from JposOutputRequest because of their method signature, such
     * methods throw an OkException after successful validation. The service can catch and retrieve the JposOutputRequest
     * object from the OkException and invoke the real final part synchronously or asynchronously as usual.
     */
    public class OkException extends JposException {
        /**
         * get JposOutputRequest this exception belongs to.
         * @return The request object.
         */
        public JposOutputRequest getOutputRequest() {
            return JposOutputRequest.this;
        }

        /**
         * The constructor. Sets error code 0.
         */
        public OkException() {
            super(0);
        }
    }

    /**
     * Creates and throws an OkException that contains its superclass as JposOutputRequest.
     * @throws JposException    OkException containing output request for final synchronous or asynchronous processing.
     */
    public void throwRequest() throws JposException {
        throw new OkException();
    }
}
