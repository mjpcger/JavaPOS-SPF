/*
 * Copyright 2022 Martin Conrad
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

package SampleElectronicValueRW;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.electronicvaluerw.*;
import jpos.*;
import jpos.config.*;
import jpos.events.TransitionEvent;
import net.bplaced.conrad.log4jpos.*;

import java.util.*;

import static jpos.ElectronicValueRWConst.EVRW_TE_CONFIRM_DEVICE_DATA;

/**
 * Base of a JposDevice based implementation of JavaPOS ElectronicValueRW device service implementations for the
 * sample device implemented in SampleCAT.tcl. The sample device simulates EFT payment transactions without ticket
 * printing (the simulator sends ticket and display data to its target).<br>
 * Supported features are:
 * <ul>
 *     <li>Sale, refund and void.</li>
 *     <li>Start transaction before amount to approve is known.</li>
 *     <li>Confirmation in case of signature based approval.</li>
 *     <li>Lock / unlock terminal.</li>
 *     <li>Card issuer, transaction number, transaction date, tip, approved amount, approval result, terminal result,
 *         masked card number (last 4 digit readable), expiration date sent in case of successful operation.</li>
 *     <li>Display output will be used to generate specific TransitionEvent events.</li>
 *     <li>Ticket layout will be passed to the application via TransitionEvent events.</li>
 *     <li>In synchronous mode, passing display data to the application and operator confirmation in case of signature
 *     based authorizations will not be possible. The service will expect a positive confirmation whenever needed.</li>
 * </ul>
 * Display data will be provided via TransitionEvent with EventNumber TE_CONFIRM_DEVICE_DATA. pData will be set to the
 * line number and pString to the corresponding display line. AdditionalSecurityInformation remains unchanged.In
 * synchronous mode, display data will not be provided.<br>
 * Ticket data will be passed directly via property AdditionalSecurityInformation, set directly before a TransitionEvent
 * with EventNumber TE_CONFIRM_DEVICE_DATA with pData = 0 and empty pString will be fired.<br>
 * AdditionalSecurityInformation is of the form<blockquote>
 *     <i>sequenceNo</i> STX <i>count</i> STX <i>ticket data</i><br>
 * </blockquote>
 * where  <i>sequenceNo</i> is the sequence number of the current operation, <i>count</i> specifies the number of
 * tickets to be printed (if two, one ticket must be signed by the customer and verified by the user) and <i>ticket
 * data</i>specifies the ticket data to be printed, with LF (0Ah) as line separator. STX (02h) will be used as delimiter
 * between <i>count</i>, <i>sequenceNo</i> and <i>ticket data</i>.<br>
 * In case of a signature based operation, a TransitionEvent with EventNumber EVRW_TE_CONFIRM_CANCEL will be fired. The
 * application must change the event property pData to 1 to cancel the operation or to 0 to finish the operation
 * successfully.<br>
 * In case of synchronous operation, no TransitionEvent event will be fired, but AdditionalSecurityInformation will be
 * used to pass TicketData as described above before the authorization method returns. In case of <i>count</i> = 2,
 * validation based on the ticket data must be performed afterwards. If validation fails, the transaction must be
 * voided.<br>
 * Here a full list of all device specific properties that can be changed via jpos.xml:
 * <ul>
 *     <li>CharacterTimeout: Positive integer value, specifying the maximum delay between bytes that belong to the same
 *     frame. Default value: 50 milliseconds.</li>
 *     <li>ClientPort: Integer value between 0 and 65535 specifying the TCP port used for communication with the device
 *     simulator. Default: 0 (for random port number selected by operating system).</li>
 *     <li>MinClaimTimeout: Minimum timeout in milliseconds used by method Claim to ensure correct working. Must be a
 *     positive value. If this value is too small, Claim might throw a JposException even if everything is OK if the
 *     specified timeout is less than or equal to MinClaimTimeout. Default: 100.</li>
 *     <li>Port: The IPv4 address of the device. Must always be specified and not empty. Notation: address:port, where
 *     address is a IPv4 address and port the TCP port of the device.</li>
 *     <li>RequestTimeout: Maximum time the service object waits for the reception of a response frame after sending a
 *     request to the target, in milliseconds. Default: 1000.</li>
 *     <li>TicketWidth: Length of one ticket line passed vie TransitionEvent. Must be a value between 28 and 99
 *     (inclusive). Default: 32.</li>
 * </ul>
 */
public class Device extends JposDevice implements Runnable {
    /**
     * Sample device specific class. The device uses the following commands:
     * <ul>
     *     <li>p%d\3			Set print line width. Parameters: Line width (must be &ge; 28).</li>
     *     <li>l%d\3			Lock or unlock terminal. Parameters: 0: unlock, 1: lock</li>
     *     <li>b\3				Begin transaction. s, v or r must follow.</li>
     *     <li>s%f\3			Set sale amount. Parameters: Amount.</li>
     *     <li>c%d\2%d\3		Commit operation. Parameters: No. of transaction to be committed, result (0: Verification
     *                          error, 1: Signature verified). Mandatory after sign-based sale operations.</li>
     *     <li>r%f\3			Set return amount. Parameters: Amount.</li>
     *     <li>v%d\3			Void transaction. Parameters: No. of transaction to be voided.</li>
     *     <li>a\3				Abort operation.</li>
     * </ul>
     * In addition, the device sends the following responses:
     * <ul>
     *     <li>L%d\3										Lock terminal. Parameters: Result code (0: OK, 4: just locked).</li>
     *     <li>U%d\3										Unlock terminal. Parameters: Result code (0: OK, 4: just unlocked).</li>
     *     <li>B%d\3										Begin transaction. Parameters: Result code (0: OK, 4: just locked,
     *                                                      6: waiting for commit, 7: authorization active).</li>
     *     <li>E%d\3										End. Parameters: Result code (0: OK, 3: Abort, 4: locked, 5: no
     *                                                      transaction, 6: wait for commit, 7: other operation active,
     *                                                      8: invalid transaction).</li>
     *     <li>E%d\2%s\2%s\2%s\2%s\2%s\2%s\2%s\2%s\2%s\3 	End processing. Parameters: Result code (0: OK, 1: wait for commit,
     *                                                      2: Error), Result code (0: no error), approval result (0: OK,
     *                                                      1111: check, else error), balance, tip (included in balance),
     *                                                      card issuer (abbreviation, see IssuerList), card no (last 4 digits),
     *                                                      expiration date, transaction number, transaction time (format
     *                                                      YYYYmmddTHHMMSS).</li>
     * </ul>
     * The device sends the following status messages:
     * <ul>
     *     <li>D%d\2%s\3					Display line. Parameters: line no (0-3), contents (UTF-8).</li>
     *     <li>P%d\2%s\3					Print ticket. Parameters: count (1-2), ticket data (UTF-8), may contain line feeds.</li>
     * </ul>
     * The contents of the following display lines will be passed via property pString of TransitionEvent events with
     * EventNumber = TE_NOTIFY_BUSY:
     * <ul>
     *     <li>Line 2, Verified</li>
     *     <li>Line 2, Verification failed</li>
     *     <li>Line 2, Remove Card</li>
     *     <li>Line 2, Enter PIN</li>
     *     <li>Line 2, Abort By User</li>
     *     <li>Line 2, Card locked</li>
     *     <li>Line 2, Retain Card</li>
     *     <li>Line 2, Card Error</li>
     *     <li>Line 2, Approval Error</li>
     *     <li>Line 2, With <i>Issuer</i></li>
     *     <li>Line 2, Invalid Expiration Date</li>
     *     <li>Line 2, Card Expired</li>
     *     <li>Line 2, Card Unknown</li>
     *     <li>Line 2, No Or Unknown Card</li>
     *     <li>Line 2, Swipe Card</li>
     *     <li>Line 2, Invalid transaction</li>
     *     <li>Line 2, Abort by Cashier</li>
     * </ul>
     * The contents of the following display lines will be passed via property pString of TransitionEvent events with
     * EventNumber TE_NOTIFY_INVALID_OPERATION or TE_NOTIFY_COMPLETE:
     *     <li>Line 3, *** ABORTED ***</li>
     *     <li>Line 3, *** SUCCESS ***</li>
     *     <li>Line 3, *** READY ***</li>
     *     <li>Line 3, *** LOCKED ***</li>
     * </ul>
     * The device will be connected via TCP.
     */

    /**
     * Maximum time between request and response.
     */
    int RequestTimeout = 1000;

    /**
     * Minimum claim timeout. To be used whenever timeout in claim() method is too small to
     * guarantee correct work of service, even if everything is OK.
     */
    int MinClaimTimeout = 100;

    /**
     * Length of print line.
     */
    int TicketWidth = 32;

    /**
     * Status watcher instance.
     */
    Thread StateWatcher = null;

    /**
     * Flag showing that status watcher shall be stopped.
     */
    boolean ToBeFinished;

    /**
     * Flag showing the current communication state.
     */
    boolean InIOError = false;

    /**
     * Long representation of a CURRENCY value of 1. Since a CURRENCY is specified as a long with implicit 4 decimals,
     * this value is always 10000.
     */
    static final long CURRENCYFACTOR = 10000;

    static private final int MinTicketWidth = 28;      // Minimum print line length.
    static private final int MaxTicketWidth = 99;      // Maximum print line length.
    private int ClientPort = 0;                         // Default: OS generated random port
    private int CharacterTimeout = 50;                  // Default: Service specific value for maximum delay between bytes belonging
                                                        // to the same frame
    private TcpClientIOProcessor OutStream = null;

    private String TicketData = null;
    private boolean Synchronous = false;

    private class TransitionWithConfirm extends  ElectronicValueRWTransitionEvent {
        SyncObject Waiter;
        TransitionWithConfirm(JposBase obj, int evn, int pd, String ps) {
            super(obj, evn, pd, ps);
            Waiter = new SyncObject();
        }
    }

    private int  Result = 0;
    private int  SequenceNumber = 0;

    // Further control characters used by the terminal simulator

    static private final byte STX = 2;
    static private final byte ETX = 3;
    static private final byte LF = 10;

    static private final byte SOH = 1;                  // Start of header, used in transaction log file.

    static private final String UTF8 = "UTF-8";         // Used for string conversion

    static private Object[][] Line3Params = {
            {ElectronicValueRWConst.EVRW_TE_NOTIFY_INVALID_OPERATION, "\\** ABORTED \\**"},
            {ElectronicValueRWConst.EVRW_TE_NOTIFY_COMPLETE, "\\** SUCCESS \\**"},
            {ElectronicValueRWConst.EVRW_TE_NOTIFY_COMPLETE, "\\** READY \\**"},
            {ElectronicValueRWConst.EVRW_TE_NOTIFY_COMPLETE, "\\** LOCKED \\**"}
    };

    static private final int ErrCardAuthentication = 2;     // Value for pData in case of invalid operation

    /**
     * Constructor. ID is the network address of the EFT (as ElectronicValueRW).
     * @param id    Network address of the display controller.
     */
    protected Device(String id) {
        super(id);
        electronicValueRWInit(1);
        PhysicalDeviceDescription = "ElectronicValueRW simulator for TCP";
        PhysicalDeviceName = "ElectronicValueRW Simulator";
        CapPowerReporting = JposConst.JPOS_PR_STANDARD;
    }

    @Override
    public void checkProperties(JposEntry entry) throws JposException {
        super.checkProperties(entry);
        try {
            Object o;
            int value;
            if ((o = entry.getPropertyValue("CharacterTimeout")) != null && (value = Integer.parseInt(o.toString())) > 0)
                CharacterTimeout = value;
            if ((o = entry.getPropertyValue("ClientPort")) != null && (value = Integer.parseInt(o.toString())) >= 0 && value <= 0xffff)
                ClientPort = value;
            if ((o = entry.getPropertyValue("MinClaimTimeout")) != null && (value = Integer.parseInt(o.toString())) >= 0)
                MinClaimTimeout = value;
            if ((o = entry.getPropertyValue("RequestTimeout")) != null && (value = Integer.parseInt(o.toString())) > 0)
                RequestTimeout = value;
            if ((o = entry.getPropertyValue("TicketWidth")) != null && (value = Integer.parseInt(o.toString())) >= 0 && MinTicketWidth <= value && value <= MaxTicketWidth)
                TicketWidth = value;
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property", e);
        }
    }

    @Override
    public void changeDefaults(ElectronicValueRWProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "ElectronicValueRW service for sample EFT";
        props.CapAuthorizeRefund = true;
        props.CapAuthorizeVoid = true;
        props.CapCenterResultCode = true;
        props.CapPINDevice = true;
        props.CapAdditionalSecurityInformation = true;
        props.ReaderWriterServiceList="EFT";
    }

    private class StreamReader extends Thread {
        /**
         * If not 0, the first byte of the frame we are waiting for.
         */
        char ResultHead = 0;

        private SyncObject FrameReader = new SyncObject();
        private String TheFrame = null;
        final private int BIGTIMEOUT = 1000000000;  // One billion milliseconds

        StreamReader(String name) {
            super(name);
            start();
        }

        @Override
        public void run() {
            TcpClientIOProcessor stream = OutStream;
            byte[] frame = new byte[0];
            while (stream != null) {
                try {
                    stream.setTimeout(frame.length == 0 ? BIGTIMEOUT : CharacterTimeout);
                    byte[] part1 = stream.read(1);
                    if (part1.length > 0) {
                        int count = stream.available();
                        byte[] part2 = count > 0 ? stream.read(count) : new byte[0];
                        byte[] data = Arrays.copyOf(frame, frame.length + part1.length + part2.length);
                        System.arraycopy(part1, 0, data, frame.length, part1.length);
                        System.arraycopy(part2, 0, data, frame.length + part1.length, part2.length);
                        for (int i = frame.length; i < data.length; i++) {
                            if (data[i] == ETX) {
                                processFrame(new String(Arrays.copyOf(data, i), UTF8));
                                data = Arrays.copyOfRange(data, i + 1, data.length);
                                i = -1;
                            }
                        }
                        frame = data;
                    }
                    else if (frame.length > 0) {
                        log(Level.DEBUG, ID + ": Incomplete frame discarded.");
                        frame = new byte[0];
                    }
                } catch (JposException e) {
                    closePort();
                    FrameReader.signal();
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            ReadThread = null;
        }

        private void processFrame(String frame) {
            synchronized(this) {
                if (frame.charAt(0) == ResultHead) {
                    TheFrame = frame;
                    FrameReader.signal();
                    return;
                }
            }
            String logtext = null;
            switch (frame.charAt(0)) {
                case 'D':   // Display text
                {
                    ElectronicValueRWProperties props = (ElectronicValueRWProperties) getClaimingInstance(ClaimedElectronicValueRW, 0);
                    String[] parts = frame.split("\2");
                    String data = parts.length >= 2 ? parts[1] : "";
                    int line = Integer.parseInt(parts[0].substring(1));
                    System.out.println("Display line " + line + ": " + data);
                    if (props != null && props.State == JposConst.JPOS_S_BUSY) {
                        if (!Synchronous) {
                            try {
                                handleEvent(new JposTransitionEvent(props.EventSource, EVRW_TE_CONFIRM_DEVICE_DATA, line + 1, data));
                            } catch (JposException e) {
                                e.printStackTrace();
                            }
                        }
                        if (line == 3) {
                            try {
                                for (Object[] params : Line3Params) {
                                    if (data.matches(params[1].toString())) {
                                        if (Synchronous) {
                                            if (TicketData != null && !TicketData.equals(props.AdditionalSecurityInformation)) {
                                                props.AdditionalSecurityInformation = TicketData;
                                                props.EventSource.logSet("AdditionalSecurityInformation");
                                            }
                                        } else {
                                            handleEvent(new JposTransitionEvent(props.EventSource, (Integer) (params[0]), 0, data));
                                        }
                                    }
                                }
                            } catch (JposException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                }
                case 'P':   // Ticket data
                {
                    ElectronicValueRWProperties props = (ElectronicValueRWProperties) getClaimingInstance(ClaimedElectronicValueRW, 0);
                    if(props != null && props.State == JposConst.JPOS_S_BUSY) {
                        String[] parts = frame.split("\2");
                        String ticket = parts.length >= 2 ? parts[1] : "";
                        int count = Integer.parseInt(parts[0].substring(1));
                        TicketData = "" + SequenceNumber + "\2" + count + "\2" + ticket;
                        if (!Synchronous) {
                            try {
                                handleEvent(new ElectronicValueRWTransitionEvent(props.EventSource,
                                        EVRW_TE_CONFIRM_DEVICE_DATA, 0, "", TicketData));
                            } catch (JposException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                }
                case 'L':   // Lock response
                    logtext = "lock";
                case 'U':   // Unlock response
                    if (logtext == null)
                        logtext = "unlock";
                case 'B':   // Begin transaction response
                    if (logtext == null)
                        logtext = "transaction commit";
                case 'E':   // Command end sequence
                    if (logtext == null)
                        logtext = "authorization end";
                    System.out.println("Unexpected " + logtext + " response, will be ignored.");
            }
        }

        /**
         * Sets response frame type for later reading. Should be set immediately before the corresponding command
         * will be sent.
         * @param frametype Header byte of expected command
         */
        void prepareReadResponse(char frametype) {
            ResultHead = frametype;
        }

        /**
         * Retrieves response read by the StreamReader. On timeout, send cancel command.
         * @return             The response or empty string in case of timeout or error.
         */
        String readFrame(int timeout) {
            if (ResultHead == 0)
                return "";
            TcpClientIOProcessor stream;
            if (!FrameReader.suspend(timeout) && (stream = OutStream) != null) {
                try {
                    stream.write(new byte[]{'a', ETX});
                } catch (JposException e) {}
            }
            synchronized(this) {
                String response = TheFrame;
                TheFrame = null;
                ResultHead = 0;
                return response == null ? "" : response;
            }
        }
    }

    /**
     * Cesar's encryption: Simply add keyValue to each character for encryption. For decryption, pass -keyValue.
     * @param s         String to be encrypted or decrypted.
     * @param keyValue  Encryption key.
     * @return The encrypted text.
     */
    public static String caesar(String s, int keyValue) {
        char[] data = s.toCharArray();
        for (int i = data.length - 1; i >= 0; --i)
            data[i] += keyValue;
        return data.toString();
    }

    private StreamReader ReadThread;

    private JposException initPort() {
        try {
            OutStream = new TcpClientIOProcessor(this, ID);
            OutStream.setParam(ClientPort);
            OutStream.open(InIOError);
            InIOError = false;
            ReadThread = new StreamReader("StreamReader_" + ID);
        } catch (JposException e) {
            OutStream = null;
            return e;
        }
        return null;
    }

    private JposException closePort() {
        JposException e = null;
        if (OutStream != null) {
            try {
                OutStream.close();
            } catch (JposException ee) {
                e = ee;
            }
            OutStream = null;
        }
        return e;
    }

    private Object sendRecv(String command, char resptype, int timeout) {   // Method to perform any command, Keep in mind that commands normally generate no response.
        TcpClientIOProcessor stream;
        StreamReader reader;
        synchronized(this) {
            if (OutStream == null) {
                JposException e = initPort();
                if (e != null) {
                    return e;
                }
            }
            stream = OutStream;
            reader = ReadThread;
        }
        try {
            byte[] request = (command + "\3").getBytes(UTF8);
            reader.prepareReadResponse(resptype);
            stream.write(request);
            return resptype == 0 ? "" : reader.readFrame(timeout);
        } catch (Exception e) {
            log(Level.TRACE, ID + ": IO error: " + e.getMessage());
            closePort();
            InIOError = true;
            return e instanceof JposException ? (JposException)e : new JposException(JposConst.JPOS_E_ILLEGAL, UniqueIOProcessor.IOProcessorError, e.getMessage());
        }
    }

    private Object sendAbort() {     // Method to send an abort command without waiting for the response.
        TcpClientIOProcessor stream;
        synchronized(this) {
            if (OutStream == null) {
                JposException e = initPort();
                if (e != null) {
                    return e;
                }
            }
            stream = OutStream;
        }
        try {
            byte[] request = ("a\3").getBytes(UTF8);
            stream.write(request);
            return "";
        } catch (Exception e) {
            log(Level.TRACE, ID + ": IO error: " + e.getMessage());
            closePort();
            InIOError = true;
            return e instanceof JposException ? (JposException)e : new JposException(JposConst.JPOS_E_ILLEGAL, UniqueIOProcessor.IOProcessorError, e.getMessage());
        }
    }

    private void setPrintWidth(int width){      // Internal command to set ticket line width.
        Object o = sendRecv(String.format("p%d", width), '\0', 0);
    }

    private void lock(boolean lock, int timeout) throws JposException {     // Internal command to lock or unlock the terminal.
        Object o = sendRecv(lock ? "l1" : "l0", lock ? 'L' : 'U', timeout);
        if (o instanceof JposException)
            throw (JposException) o;
        String resp = (String) o;
        if (resp.length() == 0)
            throw new JposException(JposConst.JPOS_E_TIMEOUT, 0, "No valid response within " + timeout + " milliseconds");
        // This function fails only if the terminal is just in the requested state. Therefore, errors will be ignored.
    }

    /**
     * Begin transaction. Should be called after starting receipt to allow card swipe for fast payment. Amount and
     * transaction type (sale, refund or void) will be sent when authorization will be required. Alternatively, the
     * terminal can be unlocked or transaction can be cancelled.
     * @param request   AuthorizeCompletion request to be handled..
     * @throws JposException    Communication error or terminal in wrong state.
     */
    private void beginAuthorization (AuthorizeCompletion request) throws JposException {
        Synchronous = request.EndSync != null;
        TicketData = null;
        Object o = sendRecv("b", 'B', request.getTimeout());
        if (o instanceof JposException)
            throw (JposException) o;
        String resp = (String) o;
        if (resp.length() == 0)
            throw new JposException(JposConst.JPOS_E_TIMEOUT, 0, "No valid response within " + request.getTimeout() + " milliseconds");
        switch (Result = Integer.parseInt(resp.substring(1))) {
            case 4:
                throw new JposException(JposConst.JPOS_E_EXTENDED, ElectronicValueRWConst.JPOS_EEVRW_RESET, "Terminal locked");
            case 6:
                throw new JposException(JposConst.JPOS_E_EXTENDED, ElectronicValueRWConst.JPOS_EEVRW_COMMANDERROR, "Confirmation requested");
            case 7:
                throw new JposException(JposConst.JPOS_E_EXTENDED, ElectronicValueRWConst.JPOS_EEVRW_COMMANDERROR, "Authorization just activated");
        }
        SequenceNumber = request.getSequenceNumber();
    }

    private void abort() throws JposException {     // Abort a transaction.
        Object o = sendAbort();
        if (o instanceof JposException)
            throw (JposException) o;
    }

    @Override
    public void postTransitionProcessing(JposTransitionEvent trevent) {
        if (trevent instanceof TransitionWithConfirm)
            ((TransitionWithConfirm)trevent).Waiter.signal();
        super.postTransitionProcessing(trevent);
    }

    /**
     * Transaction confirmation. Will be requested after signature-based sale authorization.
     * @param transno   Transaction number of corresponding sale transaction.
     * @param timeout   Timeout for answer-back.
     * @throws JposException    Communication error or terminal in wrong state.
     */
    private void confirm(int transno, int timeout) throws JposException {
        ElectronicValueRWProperties props = (ElectronicValueRWProperties) getClaimingInstance(ClaimedElectronicValueRW, 0);
        check(props == null, JposConst.JPOS_E_NOTCLAIMED, "Terminal not caimed");
        boolean committed = true;
        long acttime = System.currentTimeMillis();
        if (!Synchronous) {
            TransitionWithConfirm ev = new TransitionWithConfirm(props.EventSource, ElectronicValueRWConst.EVRW_TE_CONFIRM_CANCEL, 0, "Verify Signature");
            handleEvent(ev);
            committed = ev.Waiter.suspend(timeout) || ev.getData() == 0;
        }
        if (timeout > 0) {
            timeout -= System.currentTimeMillis() - acttime;
            if (timeout < RequestTimeout)
                timeout = RequestTimeout;
        }
        Object o = sendRecv(String.format("c%d\2%d", transno, committed ? 1 : 0), 'E', timeout);
        if (o instanceof JposException)
            throw (JposException) o;
        String resp = (String) o;
        if (resp.length() == 0)
            throw new JposException(JposConst.JPOS_E_TIMEOUT, 0, "No valid response within " + timeout + " milliseconds");
        switch (Result = Integer.parseInt(resp.substring(1))) {
            case 4: // Device locked:
                throw new JposException(JposConst.JPOS_E_EXTENDED, ElectronicValueRWConst.JPOS_EEVRW_RESET, "Terminal locked");
            case 5: // Not in transaction: Ignore.
                throw new JposException(JposConst.JPOS_E_EXTENDED, ElectronicValueRWConst.JPOS_EEVRW_COMMANDERROR, "Not in transaction");
            case 7: // No confirmation requested.
                throw new JposException(JposConst.JPOS_E_EXTENDED, ElectronicValueRWConst.JPOS_EEVRW_COMMANDERROR, "No confirmation requested");
            case 8: // Invalid transaction number:
                throw new JposException(JposConst.JPOS_E_ILLEGAL, Result, "Invalid transaction number: " + transno);
        }
    }

    /**
     * Sale or refund operation. Allowed after successful transaction start.
     * @param request   AuthorizeSale or AuthorizeRefund.
     * @throws JposException    Communication error or terminal in wrong state.
     */
    private void sale(AuthorizeCompletion request) throws JposException {
        Object o = sendRecv((request instanceof AuthorizeRefund ? "r" : "s") +
                (double) (request.getAmount() + request.getTaxOthers()) / 10000.0, 'E', request.getTimeout());
        if (o instanceof JposException)
            throw (JposException) o;
        String resp = (String) o;
        if (resp.length() == 0)
            throw new JposException(JposConst.JPOS_E_TIMEOUT, 0, "No valid response within " + request.getTimeout() + " milliseconds");
        int codeEndIndex = resp.indexOf(STX);
        switch (Result = Integer.parseInt(resp.substring(1, codeEndIndex > 0 ? codeEndIndex : resp.length()))) {
            case 4: // Device locked:
                throw new JposException(JposConst.JPOS_E_EXTENDED, ElectronicValueRWConst.JPOS_EEVRW_RESET, "Terminal locked");
            case 5: // Not in transaction: Ignore.
                throw new JposException(JposConst.JPOS_E_EXTENDED, ElectronicValueRWConst.JPOS_EEVRW_COMMANDERROR, "Not in transaction");
            case 6: // Waiting for commit.
                throw new JposException(JposConst.JPOS_E_EXTENDED, ElectronicValueRWConst.JPOS_EEVRW_COMMANDERROR, "Waiting for commit");
            case 7: // No confirmation requested.
                throw new JposException(JposConst.JPOS_E_EXTENDED, ElectronicValueRWConst.JPOS_EEVRW_COMMANDERROR, "Authorization active");
            case 3: // Operation abort confirmed
                throw new JposException(JposConst.JPOS_E_EXTENDED, ElectronicValueRWConst.JPOS_EEVRW_COMMANDERROR, "Authorization aborted");
            case 0: // Transaction OK
            case 1: // Wait for commit
            case 2: // Authorization failure
                setTransactionProperties(request, resp);
        }
    }

    /**
     * Void operation. Allowed after successful transaction start.
     * @param request   Timeout for answer-back.
     * @throws JposException    Communication error or terminal in wrong state.
     */
    private void rollback(AuthorizeVoid request) throws JposException {
        ElectronicValueRWProperties props = (ElectronicValueRWProperties) getClaimingInstance(ClaimedElectronicValueRW, 0);
        check(props == null, JposConst.JPOS_E_NOTCLAIMED, "Terminal not caimed");
        try {
            Integer.parseInt(props.ApprovalCode);
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid ApprovalCode: " + props.ApprovalCode);
        }
        Object o = sendRecv("v" + props.ApprovalCode, 'E', request.getTimeout());
        if (o instanceof JposException)
            throw (JposException) o;
        String resp = (String) o;
        if (resp.length() == 0)
            throw new JposException(JposConst.JPOS_E_TIMEOUT, 0, "No valid response within " + request.getTimeout() + " milliseconds");
        int codeEndIndex = resp.indexOf(STX);
        switch (Result = Integer.parseInt(resp.substring(1, codeEndIndex > 0 ? codeEndIndex : resp.length()))) {
            case 4: // Device locked:
                throw new JposException(JposConst.JPOS_E_EXTENDED, ElectronicValueRWConst.JPOS_EEVRW_RESET, "Terminal locked");
            case 5: // Not in transaction: Ignore.
                throw new JposException(JposConst.JPOS_E_EXTENDED, ElectronicValueRWConst.JPOS_EEVRW_COMMANDERROR, "Not in transaction");
            case 6: // Waiting for commit.
                throw new JposException(JposConst.JPOS_E_EXTENDED, ElectronicValueRWConst.JPOS_EEVRW_COMMANDERROR, "Waiting for commit");
            case 7: // No confirmation requested.
                throw new JposException(JposConst.JPOS_E_EXTENDED, ElectronicValueRWConst.JPOS_EEVRW_COMMANDERROR, "Authorization active");
            case 8: // No confirmation requested.
                throw new JposException(JposConst.JPOS_E_ILLEGAL, Result, "Invalid approval code: " + props.ApprovalCode);
            case 0: // Transaction OK
            case 1: // Wait for commit
            case 2: // Authorization failure
                setTransactionProperties(request, resp);
        }
    }

    /**
     * Sets ElectronicValueRW properties to the appropriate values returned by the device. Use the following mapping:
     * <br> - CenterResultCode: approval result,
     * <br> - Balance: Amount (inclusive tip, if added),
     * <br> - CardCompanyID: Card issuer,
     * <br> - AccountNumber: Card number (4 last digits left unchanged),
     * <br> - ExpirationDate: from 4-digit expiration date,
     * <br> - ApprovalCode: transaction number,
     * <br> - SlipNumber: Transaction date / time.
     *
     * @param request  AuthorizeSale, AuthorizeRefund or AuthorizeVoid object
     * @param resp  Response string as received by sendRecv().
     * @throws JposException If response doesnot contain the expected fields or non-numeric amount.
     */
    private void setTransactionProperties(AuthorizeCompletion request, String resp) throws JposException {
        String[] params = resp.substring(resp.indexOf('\2') + 1).split("\2");
        if (params.length >= 9) {
            ElectronicValueRWProperties props = (ElectronicValueRWProperties)getClaimingInstance(ClaimedElectronicValueRW, 0);
            synchronized (props.Results) {
                props.CenterResultCode = params[1];
                String name = "", value = "";
                try {
                    props.Results.put(name = "TaxOthers", String.valueOf((long) (Double.parseDouble(value = params[3]) * 10000)));
                    props.Results.put(name = "Balance", String.valueOf((long) (props.Balance = (long) (Double.parseDouble(value = params[2]) * 10000))));
                } catch (Exception e) {
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, JposConst.JPOS_E_ILLEGAL, "Illegal " + name + ": " + value);
                }
                props.CardCompanyID = params[4];
                props.ApprovalCode = params[7];
                handleTransactionDateTimeExpDateCardNo(params, props);
                props.PaymentMedia = ElectronicValueRWConst.EVRW_MEDIA_CREDIT;
                props.PaymentCondition = ElectronicValueRWConst.EVRW_PAYMENT_DEBIT;
                props.Results.put("PaymentCondition", props.getEnumTagFromPropertyValue("PaymentCondition", props.PaymentCondition));
                if (request instanceof AuthorizeSales)
                    props.TransactionType = ElectronicValueRWConst.EVRW_TRANSACTION_SALES;
                else if (request instanceof AuthorizeRefund)
                    props.TransactionType = ElectronicValueRWConst.EVRW_TRANSACTION_REFUND;
                else if (request instanceof AuthorizeVoid)
                    props.TransactionType = ElectronicValueRWConst.EVRW_TRANSACTION_VOID;
                String tag = props.getEnumTagFromPropertyValue("TransactionType", props.TransactionType);
                props.Results.put("TransactionType", tag != null ? tag :
                        Integer.toString(ElectronicValueRWConst.EVRW_TAG_TT_CANCEL_SALES));
            }
            checkErrorCondition(params);
        }
        else
            throw new JposException(JposConst.JPOS_E_EXTENDED, ElectronicValueRWConst.JPOS_EEVRW_COMMANDERROR, "Invalid response: " + resp);
    }

    private void handleTransactionDateTimeExpDateCardNo(String[] params, ElectronicValueRWProperties props) {
        String[] LastDayOfMonth = { "31", "", "31", "30", "31", "30", "31", "31", "30", "31", "30", "31"};
        String LastDay = LastDayOfMonth[Integer.valueOf(params[6].substring(0, 2)) - 1];
        // This routine works only until year 2099 (Only optimists believe UPOS 1.15 survives until 2099)
        if (LastDay.length() == 0)
            LastDay = Integer.valueOf(params[6].substring(2,4)) % 4 == 0 ? "29" : "28";
        props.Results.put("AccountNumber", props.AccountNumber = "XXXXXXXXXXXX" + params[5]);
        props.Results.put("ExpirationDate", "20" + params[6].substring(2, 4) + "-" + params[6].substring(0, 2) + "-" + LastDay + "T23:59:59.999");
        props.ExpirationDate = "20" + params[6].substring(2,4) + params[6].substring(0,2) + LastDay;
        props.Results.put("EVRWDateTime", params[8].substring(0,4) + "-" + params[8].substring(4,6) + "-" +
                params[8].substring(6,11) + ":" + params[8].substring(11, 13) + ":" + params[8].substring(13) + ".000");
        props.SlipNumber = params[8].substring(0, 8) + params[8].substring(9);
    }

    private void checkErrorCondition(String[] params) throws JposException {
        switch (Integer.parseInt(params[0])) {
            case 100:   // Abort by user
                throw new JposException(JposConst.JPOS_E_EXTENDED, ElectronicValueRWConst.JPOS_EEVRW_RESET, "Aborted by customer");
            case 101:   // Card locked
                throw new JposException(JposConst.JPOS_E_EXTENDED, ElectronicValueRWConst.JPOS_EEVRW_CENTERERROR, "Card locked");
            case 102:   // Retain card
                throw new JposException(JposConst.JPOS_E_EXTENDED, ElectronicValueRWConst.JPOS_EEVRW_CENTERERROR, "Retain card");
            case 103:   // Card error
                throw new JposException(JposConst.JPOS_E_EXTENDED, ElectronicValueRWConst.JPOS_EEVRW_CENTERERROR, "Card error");
            case 104:   // Approval error
                throw new JposException(JposConst.JPOS_E_EXTENDED, ElectronicValueRWConst.JPOS_EEVRW_CENTERERROR, "Approval error");
        }
    }

    @Override
    public void run() {
        UniqueIOProcessor oldstream = null;
        UniqueIOProcessor currentStream = null;
        try {
            oldstream = new UniqueIOProcessor(this, "");
        } catch (JposException e) {}
        JposCommonProperties props;
        while (!ToBeFinished) {
            StreamReader reader;
            synchronized (this) {
                if (OutStream == null)
                    initPort();
                currentStream = OutStream;
                reader = ReadThread;
            }
            if (oldstream != currentStream) {
                oldstream = currentStream;
                if ((props = getClaimingInstance(ClaimedElectronicValueRW, 0)) != null) {
                    try {
                        handleEvent(new JposStatusUpdateEvent(props.EventSource, oldstream == null ? JposConst.JPOS_SUE_POWER_OFF_OFFLINE : JposConst.JPOS_SUE_POWER_ONLINE));
                    } catch (JposException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (reader != null) {
                setPrintWidth(TicketWidth);
                try {
                    reader.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    reader = null;
                }
            }
            if (reader == null) {
                new SyncObject().suspend(CharacterTimeout);
            }
        }
    }

    @Override
    public ElectronicValueRWProperties getElectronicValueRWProperties(int index) {
        return new ElectronicValueRW();
    }

    /**
     *  Sample device specific accessor class.
     */
    private class ElectronicValueRW extends ElectronicValueRWProperties {
        protected ElectronicValueRW() {
            super(0);
        }

        @Override
        public void claim(int timeout) throws JposException {
            super.claim(timeout);
            if (StateWatcher == null) {
                StateWatcher = new Thread(Device.this, "StateWatcher");
                StateWatcher.start();
            }
            setPrintWidth(TicketWidth);
            if (InIOError) {
                release();
                throw new JposException(JposConst.JPOS_E_NOHARDWARE, "EVRW not detected");
            }
        }

        @Override
        public void release() throws JposException {
            ToBeFinished = true;
            synchronized (Device.this) {
                closePort();
            }
            while (ToBeFinished) {
                try {
                    StateWatcher.join();
                } catch (Exception e) {
                }
                break;
            }
            StateWatcher = null;
            PowerState = JposConst.JPOS_PS_UNKNOWN;
            EventSource.logSet("PowerState");
            InIOError = false;
            super.release();
        }

        @Override
        public void checkHealth(int level) throws JposException {
            CheckHealthText = InIOError ? "Internal CheckHealth: OFFLINE" : "Internal CheckHealth: OK";
            EventSource.logSet("CheckHealthText");
        }

        @Override
        public void deviceEnabled(boolean enable) throws JposException {
            lock(!enable, RequestTimeout);
            super.deviceEnabled(enable);
        }

        @Override
        public void handlePowerStateOnEnable() throws JposException {
            synchronized (Device.this) {
                int old = PowerState;
                if (old != (PowerState = OutStream == null ? JposConst.JPOS_PS_OFF_OFFLINE : JposConst.JPOS_PS_ONLINE))
                    EventSource.logSet("PowerState");
            }
            super.handlePowerStateOnEnable();
        }

        @Override
        public AuthorizeSales authorizeSales(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
            // AdditionalSecurityInformation must be an integer value, containing the transaction number generated by
            // the device.
            check(ServiceType != ElectronicValueRWConst.EVRW_ST_CAT, JposConst.JPOS_E_ILLEGAL, "No Service selected");
            return super.authorizeSales(sequenceNumber, amount, taxOthers, timeout);
        }

        @Override
        public void authorizeSales(AuthorizeSales request) throws JposException {
            int timeout = request.getTimeout() == JposConst.JPOS_FOREVER ? Integer.MAX_VALUE : request.getTimeout();
            long starttime = System.currentTimeMillis();
            beginAuthorization(request);
            long deltatime = System.currentTimeMillis() - starttime;
            if (deltatime < timeout) {
                sale(request);
                deltatime = System.currentTimeMillis() - starttime;
                if (Result == 1 && deltatime < timeout) {
                    try {
                        confirm(Integer.parseInt(ApprovalCode), (int) (timeout - deltatime));
                    } catch (NumberFormatException e) {
                        throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid ApprovalCode: " + ApprovalCode, e);
                    }
                }
            }
        }

        @Override
        public AuthorizeVoid authorizeVoid(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
            // ApprovalCode must match the approval code returned by original operation.
            check(ServiceType != ElectronicValueRWConst.EVRW_ST_CAT, JposConst.JPOS_E_ILLEGAL, "No Service selected");
            check("".equals(ApprovalCode), JposConst.JPOS_E_ILLEGAL, "ApprovalCode missing");
            return super.authorizeVoid(sequenceNumber, amount, taxOthers, timeout);
        }

        @Override
        public void authorizeVoid(AuthorizeVoid request) throws JposException {
            int timeout = request.getTimeout() == JposConst.JPOS_FOREVER ? Integer.MAX_VALUE : request.getTimeout();
            long starttime = System.currentTimeMillis();
            beginAuthorization(request);
            long deltatime = System.currentTimeMillis() - starttime;
            if (deltatime < timeout) {
                rollback(request);
            }
        }

        @Override
        public AuthorizeRefund authorizeRefund(int sequenceNumber, long amount, long taxOthers, int timeout) throws JposException {
            // AdditionalSecurityInformation must be an integer value, containing the transaction number generated by
            // the device.
            check(ServiceType != ElectronicValueRWConst.EVRW_ST_CAT, JposConst.JPOS_E_ILLEGAL, "No Service selected");
            return super.authorizeRefund(sequenceNumber, amount, taxOthers, timeout);
        }

        @Override
        public void authorizeRefund(AuthorizeRefund request) throws JposException {
            int timeout = request.getTimeout() == JposConst.JPOS_FOREVER ? Integer.MAX_VALUE : request.getTimeout();
            long starttime = System.currentTimeMillis();
            beginAuthorization(request);
            long deltatime = System.currentTimeMillis() - starttime;
            if (deltatime < timeout) {
                sale(request);
            }
        }

        @Override
        public void clearOutput() throws JposException {
            abort();
            super.clearOutput();
        }

        @Override
        public void currentService(String service) throws JposException {
            if (!service.equals(ServiceType)) {
                if (ReaderWriterServiceList.equals(service)) {  // So easy only if ReaderWriterServiceList has only one element
                    super.currentService(service);
                    ServiceType = ElectronicValueRWConst.EVRW_ST_CAT;
                    TrainingModeState = ElectronicValueRWConst.EVRW_TM_FALSE;
                    PINEntry = ElectronicValueRWConst.EVRW_PIN_ENTRY_INTERNAL;
                } else {
                    ServiceType = ElectronicValueRWConst.EVRW_ST_UNSPECIFIED;
                    TrainingModeState = ElectronicValueRWConst.EVRW_TM_UNKNOWN;
                    PINEntry = ElectronicValueRWConst.EVRW_PIN_ENTRY_UNKNOWN;
                }
                EventSource.logSet("ServiceType");
                EventSource.logSet("TrainingModeState");
                EventSource.logSet("PINEntry");
            }
        }

        @Override
        public void PINEntry(int value) throws JposException {
            // If service = EFT, only internal PINEntry supported. If service not set, PINEntry is unknown.
            check(CurrentService.length() == 0 && value != ElectronicValueRWConst.EVRW_PIN_ENTRY_UNKNOWN, JposConst.JPOS_E_ILLEGAL, "No service, PINEntry value illegal: " + value);
            check(CurrentService.equals(ReaderWriterServiceList) && value != ElectronicValueRWConst.EVRW_PIN_ENTRY_INTERNAL, JposConst.JPOS_E_ILLEGAL, "PINEntry value " + value + " not supported for service " + ReaderWriterServiceList);
            super.PINEntry(value);
        }
    }
}
