/*
 * Copyright 2020 Martin Conrad
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

package SampleUdpDevice;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.config.JposEntry;

import static jpos.JposConst.*;
import static net.bplaced.conrad.log4jpos.Level.*;

/**
 * JposDevice based implementation base for JavaPOS device service implementations for the
 * sample device implemented in SampleUdpDevice.tcl. It implements methods, properties and interfaces that
 * can be used for server class implementations.<p>
 * All frames have variable length and start with a SUBDEV:Function where SUBDEV is a sub-device specific id (e.g.
 * DRAWER) and Function the name of a sub-device specific function to be processed. The corresponding response starts
 * with the same SUBDEV:Function pair.<br> Communication will be made with UDP sockets.
 * Multiple commands can be sent in a single frame, separated by comma (,). The corresponding responses will
 * be sent in one frame, separated by comma as well.<p>
 * Here a full list of all device specific properties that can be changed via jpos.xml:
 * <ul>
 *     <li>MaxRetry: Specifies the maximum number of retries. Default: 2.</li>
 *     <li>OwnPort: Integer value between 0 and 65535 specifying the UDP port used for communication with the device
 *     simulator. Default: 0 (for random port number selected by operating system).</li>
 *     <li>PollDelay: Minimum time between status requests, in milliseconds. Status requests will be used to monitor the
 *     device state. Default: 200.</li>
 *     <li>Port: Operating system specific name of the UDP address to be used for
 *     communication with the device simulator. Names are of the form IPv4:port, where IPv4 is the IP address of the
 *     device and port its UDP port.</li>
 *     <li>RequestTimeout: Maximum time, in milliseconds, between sending a command to the simulator and getting the
 *     first byte of its response. Default: 200.</li>
 *     <li>UseClientIO: Specifies whether UdpClientIOProcessor or UdpIOProcessor shall be used for communication. If
 *     true, UdpClientIOProcessor will be used, otherwise UdpIOProcessor. Default: true.</li>
 * </ul>
 */
public class Device extends JposDevice implements Runnable {
    // General purpose objects
    private int OwnPort = 0;
    private int PollDelay = 200;
    private boolean UseClientIO = true;
    private UniqueIOProcessor Target = null;
    private final int[] OpenCount = { 0 };
    private JposCommonProperties StartPollingWaiter = null;
    private ThreadHandler StateWatcher;

    /**
     * Offline flag, simple boolean that shows whether the last request has been responded correctly (false) or not (true).
     */
    boolean Offline = true;

    /**
     * Synchronization object for internal thread synchronization.
     */
    SyncObject PollWaiter = new SyncObject();

    /**
     * Timeout in milliseconds for a response from the device. Default 500 milliseconds.
     */
    int RequestTimeout = 500;

    /**
     * Maximum number of retrys in case of a timeout after sending a request to the device. Default 2.
     */
    int MaxRetry = 2;

    /**
     * Constructor. id specifies the server to be connected in format host:port.
     *
     * @param id IPv4 target address and port
     */
    protected Device(String id) {
        super(id);
        PhysicalDeviceDescription = "UDP device simulator";
        PhysicalDeviceName = "UDP device Simulator";
        CapPowerReporting = JPOS_PR_STANDARD;
    }

    @Override
    public void checkProperties(JposEntry entry) throws JposException {
        super.checkProperties(entry);
        try {
            Object o;
            if ((o = entry.getPropertyValue("OwnPort")) != null) {
                if ((OwnPort = Integer.parseInt(o.toString())) < 0 || OwnPort >= 0xffff)
                    throw new JposException(JPOS_E_ILLEGAL, "Invalid source port.");
            }
            if ((o = entry.getPropertyValue("PollDelay")) != null) {
                if ((PollDelay = Integer.parseInt(o.toString())) <= 0)
                    throw new JposException(JPOS_E_ILLEGAL, "Invalid poll delay: " + PollDelay);
            }
            if ((o = entry.getPropertyValue("RequestTimeout")) != null) {
                if ((RequestTimeout = Integer.parseInt(o.toString())) <= 0)
                    throw new JposException(JPOS_E_ILLEGAL, "Invalid request timeout: " + RequestTimeout);
            }
            if ((o = entry.getPropertyValue("MaxRetry")) != null) {
                if ((MaxRetry = Integer.parseInt(o.toString())) <= 0)
                    throw new JposException(JPOS_E_ILLEGAL, "Invalid maximum retry count: " + MaxRetry);
            }
            if ((o = entry.getPropertyValue("UseClientIO")) != null) {
                UseClientIO = Boolean.parseBoolean(o.toString());
            }
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            throw new JposException(JPOS_E_ILLEGAL, "Invalid JPOS property", e);
        }
    }

    private JposException initPort() {
        try {
            if (UseClientIO)
                ((UdpClientIOProcessor) (Target = new UdpClientIOProcessor(this, ID))).setParam(OwnPort);
            else
                ((UdpIOProcessor) (Target = new UdpIOProcessor(this, OwnPort))).setTarget(ID);
            Target.open(Offline);
            Offline = false;
        } catch (JposException e) {
            Target = null;
            return e;
        }
        return null;
    }

    @SuppressWarnings("UnusedReturnValue")
    private JposException closePort() {
        JposException e = null;
        if (Target != null) {
            for (int i = 0; i < 2; i++) {
                try {
                    Target.close();
                } catch (JposException ee) {
                    e = ee;
                }
            }
            Target = null;
        }
        return e;
    }

    private boolean connectionOffline() {
        if (Target == null) {
            JposException e = initPort();
            if (e != null)
                return Offline = true;
        }
        return false;
    }

    /**
     * Class used to check whether command and return parameters are correct.
     */
    protected class CommonSubDeviceToolset {
        /**
         * Checks whether response is a valid response for command
         * @param command Both parts of a command.
         * @param response Both parts of a response, first parts of command and response must be equal.
         * @return Return values specify the result of validation. The following values are supported:
         * <ul>
         *     <li>1: The response is a valid response.</li>
         *     <li>0: This ReturnValueChecker cannot validate this command.</li>
         *     <li>-1: The response is an invalid or unknown value.</li>
         *     <li>-2: The command is invalid.</li>
         * </ul>
         */
        public int check(String[] command, String[] response) {
            if (response.length != 2)
                return -1;
            if (!response[0].equals(command[0]))
                return response[0].equals("INVALID") ? -2 : -1;
            return response[1].matches("Inv!.*") ? -2 : 0;
        }

        /**
         * Checks whether the second part of a command and the corresponding response are formally correct.
         * @param command   Right request part (part right of ":").
         * @param response  Right part of response (part right of ":").
         * @param opportunities List of functions supported by a specific sub-device.
         * @return Length of fuction name if valid, otherwise 0.
         */
        public int checkValidCommand(String command, String response, String[] opportunities) {
            int clen = command.length();
            int rlen = response.length();
            for (String opportunity : opportunities) {
                int olen = opportunity.length();
                if (olen <= clen && olen <= rlen) {
                    if (opportunity.equals(command.substring(0, olen)) && opportunity.equals(response.substring(0, olen)))
                        return olen;
                }
            }
            return 0;
        }

        private boolean Oldoffline;

        /**
         * Saves the current state of a sub-device before a status request. The saved values will usually be compared with
         * the new status after the request. If different, one or more events may be thrown.
         * @param commands  One-dimensional array of commands to for status update. Commands to update the specific sub-
         *                  command must be appended on return.
         */
        public void saveCurrentStatusInformation(String[][] commands) {
            Oldoffline = Offline;
        }

        /**
         * Handles status changes.
         * @param resp Parameter strings as returned by the status update commands given by saveCurrentStatusInformation.
         *             The strings are in the same sequence as the commands.
         */
        public void setNewStatusInformation(String[] resp) {}

        /**
         * Method to be called after status changes have been stored to perform event handling.
         */
        public void statusUpdateEventProcessing() {
            if (Oldoffline && !Offline) {
                statusPowerOnlineProcessing();
            }
            if (!Offline) {
                statusUpdateProcessing();
            }
            if (Offline && !Oldoffline) {
                statusPowerOfflineProcessing();
            }
        }

        /**
         * Method to be overwritten. Will be called if device is online to perform event handling.
         */
        public void statusUpdateProcessing() {}

        /**
         * Method to be overwritten. Will be called to generate power online event for sub-device before other
         * StatusUpdateEvents will be thrown.
         * @return The previous power state.
         */
        public boolean statusPowerOnlineProcessing() {
            boolean ret = Oldoffline;
            Oldoffline = Offline;
            return ret;
        }

        /**
         * Method to be overwritten. Will be called to generate power offline event for sub-device after all other
         * StatusUpdateEvents, have been thrown.
         */
        public void statusPowerOfflineProcessing() {}
    }

    /**
     * List of ReturnValueCheckers to be used by a specific device implementation for the UDP sample
     */
    public CommonSubDeviceToolset[] Toolsets = {};

    /**
     * Sends a single command to the device and returns the response on success. In case of a recoverable error,
     * maximum MaxRetry to send the command will be made.
     *
     * @param command Command to be sent to the device.
     * @return        Return parameters of the response on success, otherwise null.
     */
    protected String sendResp(String command) {
        String[] result = sendResp(new String[]{command});
        return result == null ? null : result[0];
    }

    /**
     * Sends multiple commands to the device and returns the responses on success. In case of a recoverable error,
     * maximum MaxRetry to send the commands will be made.<br>
     * On success, the response values will be returned within a String array of the same length as the array of
     * commands passed as method parameter.
     *
     * @param commandParts Array of commands to be sent to the device.
     * @return             Array of return parameters of the corresponding responses on success, otherwise null.
     */
    @SuppressWarnings("ThrowableInstanceNeverThrown ")
    protected synchronized String[] sendResp(String[] commandParts) {
        if (connectionOffline())
            return null;
        try {
            StringBuilder commands = new StringBuilder();
            String[] result = new String[commandParts.length];
            for (String part : commandParts) {
                commands.append(",").append(part);
            }
            byte[] responses;
            byte[] request = commands.substring(1).getBytes();
            Target.flush();
            for (int count = 0; count < MaxRetry; count++) {
                Target.write(request);
                long starttime = System.currentTimeMillis();
                long acttime = starttime;
                do {
                    Target.setTimeout((int)(RequestTimeout - (acttime - starttime)));
                    responses = Target.read(500);
                    if (responses.length > 0 && (UseClientIO || Target.getSource().equals(Target.getTarget()))) {
                        String[] responseParts = new String(responses).split(",");
                        if (commandParts.length == responseParts.length) {
                            int checkresult = 1;
                            for (int index = 0; index < commandParts.length && checkresult == 1; index++) {
                                String[] respparts = responseParts[index].split(":");
                                String[] cmdparts = commandParts[index].split(":");
                                for (CommonSubDeviceToolset check : Toolsets) {
                                    if ((checkresult = check.check(cmdparts, respparts)) != 0) {
                                        if (checkresult == 1)
                                            result[index] = respparts[1];
                                        break;
                                    }
                                }
                                result[index] = respparts[1];
                            }
                            if (checkresult == 1)
                                return result;
                        }
                    }
                } while ((acttime = System.currentTimeMillis()) - starttime < RequestTimeout);
            }
        } catch (JposException e) {
            log(TRACE, ID + ": IO error: " + e.getMessage());
        }
        Offline = true;
        closePort();
        return null;
    }

    @Override
    public void run() {
        try {
            while (!StateWatcher.ToBeFinished) {
                String[][] commands = {new String[0]};
                for (CommonSubDeviceToolset set : Toolsets) {
                    set.saveCurrentStatusInformation(commands);
                }
                String[] resps = sendResp(commands[0]);
                for (CommonSubDeviceToolset set : Toolsets) {
                    if (resps != null)
                        set.setNewStatusInformation(resps);
                    set.statusUpdateEventProcessing();
                }
                if (StartPollingWaiter != null) {
                    StartPollingWaiter.signalWaiter();
                    StartPollingWaiter = null;
                }
                PollWaiter.suspend(PollDelay);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to start status updating thread if started for the first time.
     * @param props Property set of the device that starts polling
     * @return      Start count, 1 if updating thread has been started and a value &gt; 1 if the thread is just running.
     */
    @SuppressWarnings("UnusedReturnValue")
    int startPolling(JposCommonProperties props) {
        synchronized (OpenCount) {
            if (OpenCount[0] == 0) {
                PollWaiter = new SyncObject();
                (StartPollingWaiter = props).attachWaiter();
                (StateWatcher = new ThreadHandler(ID + "/StatusUpdater", this)).start();
                OpenCount[0] = 1;
                props.waitWaiter((long)MaxRetry * RequestTimeout * 3);
                props.releaseWaiter();
            }
            else
                OpenCount[0] = OpenCount[0] + 1;
            return OpenCount[0];
        }
    }

    /**
     * Method to stop status updating thread if stopped as many times as previously started.
     * @return      Start count, 0 if updating thread has been stopped and a value &gt; 1 if the thread is just running.
     */
    @SuppressWarnings({"UnusedReturnValue", "ThrowableInstanceNeverThrown"})
    int stopPolling() {
        synchronized(OpenCount) {
            if (OpenCount[0] == 1) {
                StateWatcher.ToBeFinished = true;
                PollWaiter.signal();
                StateWatcher.waitFinished();
                StartPollingWaiter = null;
                closePort();
            }
            if (OpenCount[0] > 0)
                OpenCount[0] = OpenCount[0] - 1;
            return OpenCount[0];
        }
    }
}
