package SampleScale;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.scale.*;
import jpos.*;
import jpos.config.JposEntry;

import java.nio.charset.Charset;
import java.util.Objects;

import static de.gmxhome.conrad.jpos.jpos_base.SerialIOProcessor.*;
import static javax.swing.JOptionPane.*;
import static jpos.JposConst.*;
import static jpos.ScaleConst.*;
import static net.bplaced.conrad.log4jpos.Level.*;

/**
 * JposDevice based implementation of a JavaPOS Scale device service implementation for the
 * sample device implemented in SampleScale.tcl.
 * <p>This is an implementation based on the Scales Dialog 02 / 04.
 * <p>Here a full list of all device specific properties that can be changed via jpos.xml:
 * <ul>
 *     <li>Baudrate: Baud rate of the communication device. Must be 2400 or 4800, if set. Default: 2400.
 *     <br>This property may only be set if the communication with the device shall be made via serial port.</li>
 *     <li>CharacterTimeout: Positive integer value, specifying the maximum delay between bytes that belong to the same
 *     frame. Default value: 20 milliseconds.</li>
 *     <li>DefaultTara: Default tare value. Default: 2. If the scale shall use any other default, you can set it here.</li>
 *     <li>MaximumWeight: Maximum weight supported by the scale. Default: 5000. Any higher weight results in an
 *     overweight condition.</li>
 *     <li>MaxRetry: Specifies the maximum number of retries. Should be &gt; 0 only for RS232 (real COM ports)
 *     where characters can become lost or corrupted on the communication line. Default: 0.</li>
 *     <li>OwnPort: Integer value between 0 and 65535 specifying the TCP port used for communication with the device
 *     simulator. Default: 0 (for random port number selected by operating system).
 *     <br>This property may only be set if the communication with the device shall be made via TCP.</li>
 *     <li>PollDelay: Minimum time between status requests, in milliseconds. Status requests will be used to monitor the
 *     device state. Default: 500.</li>
 *     <li>RequestTimeout: Maximum time, in milliseconds, between sending a command to the simulator and getting the
 *     first byte of its response. Default: 2000.</li>
 *     <li>Target: Operating system specific name of the serial communication port (e.g. RS232, Usb2Serial,
 *     Bluetooth...) or the TCP address to be used for
 *     communication with the device simulator. In case of RS232, names look typically like COM2 or /dev/ttyS1. In
 *     case of TCP, names are of the form IPv4:port, where IPv4 is the IP address of the device and port its TCP port.</li>
 * </ul>
 */
public class Device extends JposDevice implements Runnable {
    /**
     * IO processor to be used for communication with scale.
     */
    UniqueIOProcessor Target = null;
    /**
     * Constructor. id specifies either the COM port to be used or the server to be connected in format host:port
     *
     * @param id COM port or IP target address and port
     */
    protected Device(String id) {
        super(id);
        scaleInit(1);
        PhysicalDeviceDescription = "Scales Dialog 02 / 04 simulator";
        PhysicalDeviceName = "Scales Simulator";
        CapPowerReporting = JPOS_PR_STANDARD;
    }

    /**
     * Maximum no. of retries before service gives up communication.
     */
    int MaxRetry = 0;

    /**
     * In case of a serial connection, baud rates 2400 (Scales Dialog 02) or 4800 (Scales Dialog 04) are valid.
     */
    Integer Baudrate = BAUDRATE_2400;
    /**
     * In case of a TCP connection, values above 0 specify the source port number. Zero results in an random source port.
     */
    Integer OwnPort = null;

    /**
     * Timeout for getting a response after sending a request.
     */
    int RequestTimeout = 1000;

    /**
     * Timeout for getting the next character of a multi-character frame.
     */
    int CharacterTimeout = 20;

    /**
     * Delay between unforced status requests used to verify operational state of the scale.
     */
    int PollDelay = 500;

    /**
     * Maximum weight supported by the scale. Any higher weight results in an overweight condition.
     */
    int MaximumWeight = 5000;

    /**
     * Default tare value. If the scale shall use any other default, you can set it here.
     */
    int DefaultTara = 2;

    /**
     * Weight unit of the scale. Currently, only supported value is SCAL_WU_KILOGRAM.
     */
    int WeightUnit = SCAL_WU_KILOGRAM;

    private boolean InIOError = false;
    private String ScaleText = "";
    private final Charset AsciiCoder = Charset.availableCharsets().get("US-ASCII");

    @Override
    @SuppressWarnings("resource")
    public void checkProperties(JposEntry entry) throws JposException {
        super.checkProperties(entry);
        try {
            new TcpClientIOProcessor(this, ID);
            OwnPort = 0;   // Default: Random port
        } catch (JposException ignored) {}
        try {
            Object o;
            if ((o = entry.getPropertyValue("OwnPort")) != null) {
                if (OwnPort != null) {
                    if ((OwnPort = Integer.parseInt(o.toString())) < 0 || OwnPort >= 0xffff)
                        throw new JposException(JPOS_E_ILLEGAL, "Invalid source port.");
                }
                else
                    throw new JposException(JPOS_E_ILLEGAL, "Invalid property for Scale Dialog 04.");
            }
            if ((o = entry.getPropertyValue("Baudrate")) != null) {
                Baudrate = Integer.parseInt(o.toString());
                if (OwnPort != null)
                    throw new JposException(JPOS_E_ILLEGAL, "Invalid JPOS property: Baudrate");
            }
            if ((o = entry.getPropertyValue("RequestTimeout")) != null) {
                RequestTimeout = Integer.parseInt(o.toString());
            }
            if ((o = entry.getPropertyValue("CharacterTimeout")) != null) {
                CharacterTimeout = Integer.parseInt(o.toString());
            }
            if ((o = entry.getPropertyValue("MaxRetry")) != null) {
                MaxRetry = Integer.parseInt(o.toString());
            }
            if ((o = entry.getPropertyValue("PollDelay")) != null) {
                PollDelay = Integer.parseInt(o.toString());
            }
            if ((o = entry.getPropertyValue("MaximumWeight")) != null) {
                MaximumWeight = (int) Double.parseDouble(o.toString()) * 1000;
            }
            if ((o = entry.getPropertyValue("DefaultTara")) != null) {
                DefaultTara = (int)Double.parseDouble(o.toString()) * 1000;
            }
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            throw new JposException(JPOS_E_ILLEGAL, "Invalid JPOS property", e);
        }
    }

    @Override
    public void changeDefaults(ScaleProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.CapPriceCalculating = true;
        props.CapDisplayText = true;
        props.CapDisplay = true;
        props.CapTareWeight = true;
        props.MaxDisplayTextChars = 13;
        props.MaximumWeight = MaximumWeight;
        props.TareWeightDef = DefaultTara;
        props.WeightUnit = WeightUnit;
        props.DeviceServiceDescription = "Scales service for Scales Dialog 02/04 simulator";
    }

    private ThreadHandler CommandProcessor;
    private SyncObject SignalStatusUpdated = null;
    private final SyncObject WaitCommand = new SyncObject();
    private int Offline = JPOS_PS_UNKNOWN;
    /**
     * Thread main, used for status check loop while device is enabled.
     */
    @Override
    @SuppressWarnings("SynchronizeOnNonFinalField")
    public void run() {
        Target = null;
        int offline = Offline;
        while (!CommandProcessor.ToBeFinished) {
            long timeval = System.currentTimeMillis();
            sendCommand("\2" + "08\3", (MaxRetry + 1) * (RequestTimeout + CharacterTimeout) + 70);
            JposCommonProperties props = getClaimingInstance(ClaimedScale, 0);
            if (!CommandProcessor.ToBeFinished && offline != Offline && Offline == JPOS_PS_ONLINE) {
                try {
                    handleEvent(new JposStatusUpdateEvent(props.EventSource, JPOS_SUE_POWER_ONLINE));
                } catch (JposException ignored) {}
                offline = Offline;
            }
            synchronized(CommandProcessor) {
                if (SignalStatusUpdated != null) {
                    SignalStatusUpdated.signal();
                    SignalStatusUpdated = null;
                }
            }
            if (!CommandProcessor.ToBeFinished && offline != Offline) {
                try {
                    handleEvent(new JposStatusUpdateEvent(props.EventSource, JPOS_SUE_POWER_OFF_OFFLINE));
                } catch (JposException ignored) {}
                offline = Offline;
            }
            timeval = System.currentTimeMillis() - timeval;
            if (timeval < PollDelay)
                WaitCommand.suspend(PollDelay - timeval);
        }
    }

    @Override
    public ScaleProperties getScaleProperties(int index) {
        return new SampleScaleAccessor();
    }

    /**
     * The sample scale interface implementation class. Implementation as inner class only recommended for small
     * classes. However, using inner classes makes things sometimes easier.
     */
    public class SampleScaleAccessor extends ScaleProperties {
        /**
         * Creates the scale accessor instance.
         */
        SampleScaleAccessor() {
            super(0);
        }

        @Override
        @SuppressWarnings("ThrowableInstanceNeverThrown")
        public void deviceEnabled(boolean enable) throws JposException {
            if (enable) {
                int timeout = (MaxRetry + 2) * RequestTimeout;
                SyncObject waiter = SignalStatusUpdated = new SyncObject();
                (CommandProcessor = new ThreadHandler(LogicalName +".StatusHandler", Device.this)).start();
                waiter.suspend(timeout);
            }
            else {
                CommandProcessor.ToBeFinished = true;
                WaitCommand.signal();
                CommandProcessor.waitFinished();
                closePort(true);
                Offline = JPOS_PS_UNKNOWN;
                InIOError = false;
            }
            super.deviceEnabled(enable);
        }

        @Override
        public void handlePowerStateOnEnable() throws JposException {
            PowerState = SignalStatusUpdated != null || InIOError ? JPOS_PS_OFF_OFFLINE : JPOS_PS_ONLINE;
            super.handlePowerStateOnEnable();
        }

        @Override
        public void checkHealth(int level) throws JposException {
            if (level == JPOS_CH_INTERNAL) {
                CheckHealthText = "Internal CheckHealth: ";
                CheckHealthText += InIOError || member(PowerState, new long[]{JPOS_PS_OFFLINE, JPOS_PS_OFF, JPOS_PS_OFF_OFFLINE}) ? "Failed" : "OK";
            }
            else {
                nonInternalCheckHealth(level);
            }
        }

        private void nonInternalCheckHealth(int level) {
            ScaleService srv = (ScaleService)EventSource;
            long price = UnitPrice;
            String text = ScaleText;
            boolean async = AsyncMode;
            int[] weight = {0};
            int msgtype = INFORMATION_MESSAGE;
            CheckHealthText = level == JPOS_CH_EXTERNAL ? "External CheckHealth: " : "Interactive CheckHealth: ";
            try {
                if (async)
                    asyncMode(false);
                UnitPrice = 123400;  // 12.34
                srv.displayText("WEIGHING");
                if (level == JPOS_CH_INTERACTIVE)
                    synchronizedMessageBox("Put something on scale", "CheckHealth Scale", msgtype);
                srv.readWeight(weight, level == JPOS_CH_EXTERNAL ? Integer.MAX_VALUE : MaxRetry * RequestTimeout);
                CheckHealthText += "OK";
            } catch (JposException e) {
                CheckHealthText += "Failed (" +e.getMessage() + ")";
                msgtype = ERROR_MESSAGE;
            }
            try {
                if (UnitPrice != price || !Objects.equals(ScaleText, text)) {
                    UnitPrice = price;
                    displayText(text);
                }
                if (async)
                    asyncMode(true);
            } catch (JposException ignored) {}
            if (level == JPOS_CH_INTERACTIVE)
                synchronizedMessageBox(CheckHealthText, "CheckHealth Scale", msgtype);
        }

        @Override
        public void tareWeight(int weight) throws JposException {
            if (weight >= 5000)
                throw new JposException(JPOS_E_ILLEGAL, "Tare too high");
            String request = String.format("\2%02d\33%06d\33%04d\3", 3, UnitPrice / 100, weight);
            sendSetupCommand(request);
            super.tareWeight(weight);
        }

        private void sendSetupCommand(String request) throws JposException {
            String response = sendCommand(request, Integer.MAX_VALUE);
            if (response == null)
                throw new JposException(JPOS_E_OFFLINE, "Communication error");
            if (!response.equals("\6")) {
                if (response.equals("\15"))
                    throw new JposException(JPOS_E_FAILURE, "Negative acknowledge");
                if (response.length() == 7 && response.charAt(6) == ETX && response.startsWith(String.format("%c09%c", STX, ESC))) {
                    int reason = Integer.parseInt(response.substring(4, 6), 10);
                    if (reason != 0)
                        throw new JposException(JPOS_E_FAILURE, "Scale in error state: " + reason);
                }
                throw new JposException(JPOS_E_FAILURE, "Invalid response");
            }
        }

        @Override
        public void unitPrice(long price) throws JposException {
            if (price >= 100000000)
                throw new JposException(JPOS_E_ILLEGAL, "Unit price too high");
            String request = String.format("\2%02d\33%06d\33\3", 1, price / 100);
            sendSetupCommand(request);
            super.unitPrice(price);
        }

        @Override
        public void zeroValid(boolean b) throws JposException {
            if (b)
                throw new JposException(JPOS_E_ILLEGAL, 0, "Valid zero weight not supported by sample scale");
        }

        @Override
        public void displayText(String description) throws JposException {
            super.displayText(description);
            String request = String.format("\2%02d\33%06d\33%-13s\3", 4, UnitPrice / 100, description);
            sendSetupCommand(request);
            ScaleText = description;
        }

        @Override
        public ReadWeight readWeight(int[] weight, int timeout) throws JposException {
            check(State == JPOS_S_ERROR, JPOS_E_BUSY, "Device busy (in error)");
            return super.readWeight(weight, timeout);
        }

        @Override
        public void readWeight(ReadWeight request) throws JposException {
            int timeout = request.getTimeout() == JPOS_FOREVER ? Integer.MAX_VALUE : request.getTimeout();
            if (timeout < (MaxRetry + 2) * RequestTimeout)
                timeout = (MaxRetry + 2) * RequestTimeout;
            long startTime = System.currentTimeMillis();
            while (true) {
                String response = sendCommand("\5", timeout);
                try {
                    assert response != null;
                    check(response.length() != 7 && response.length() != 26, JPOS_E_FAILURE, "Bad frame size");
                    if (response.length() == 7 && response.charAt(6) == ETX && response.startsWith(String.format("%c09%c", STX, ESC))) {
                        int state = Integer.parseInt(response.substring(4, 6), 10);
                        switch (state) {    // Still in motion: Do it again (no remove of finalizer)
                            // Weight less than minimum (zero weight)
                            case 20, 30, 31 ->    // Scale less than zero
                                    check(System.currentTimeMillis() - startTime > timeout, JPOS_E_TIMEOUT, "No valid weight within time limit");
                            case 21 ->    // Not in motion since last weighing
                                    throw new JposException(JPOS_E_EXTENDED, JPOS_ESCAL_SAME_WEIGHT, "Not in motion since last weighing");
                            case 22 ->    // No price calculation (unit price 0), should never occur
                                    throw new JposException(JPOS_E_ILLEGAL, 0, "UnitPrice has not been set");
                            case 32 ->    // Scale is overloaded
                                    throw new JposException(JPOS_E_EXTENDED, JPOS_ESCAL_OVERWEIGHT, "Scale overloaded");
                            default ->
                                    throw new JposException(JPOS_E_FAILURE, 0, "Unknown scale status: " + state);
                        }
                        new SyncObject().suspend(PollDelay);
                    } else if (response.length() == 26 && response.startsWith(String.format("%c02%c3%c", STX, ESC, ESC)) &&
                            response.charAt(11) == ESC && response.charAt(18) == ESC && response.charAt(25) == ETX) {
                        check(Long.parseLong(response.substring(12, 18), 10) * 100 != UnitPrice, JPOS_E_FAILURE, "Unexpected unit price");
                        request.WeightData = Integer.parseInt(response.substring(6, 11), 10);
                        request.SalesPrice = Long.parseLong(response.substring(19, 25), 10) * 100;
                        return;
                    } else
                        throw new JposException(JPOS_E_FAILURE, 0, "Invalid frame structure");
                } catch (NumberFormatException e) {
                    check(System.currentTimeMillis() - startTime < timeout, JPOS_E_TIMEOUT, "No valid weight within time limit");
                } catch (NullPointerException e) {
                    throw new JposException(JPOS_E_FAILURE, 0, "Offline");
                }
            }
        }

        @Override
        public DoPriceCalculating doPriceCalculating(int[] weightData, int[] tare, long[] unitPrice, long[] unitPriceX, int[] weightUnitX, int[] weightNumeratorX, int[] weightDenominatorX, long[] price, int timeout) throws JposException {
            throw new JposException(JPOS_E_ILLEGAL, "Parameter values for unitPriceX, weightUnitX, weightNumeratorX and weightDenominatorX are not available.");
        }
    }

    /**
     * Port initialization.
     * @return In case of initialization error, the exception. Otherwise null.
     */
    private JposException initPort() {
        try {
            if (OwnPort != null) {
                ((TcpClientIOProcessor)(Target = new TcpClientIOProcessor(this, ID))).setParam(OwnPort);
            }
            else {
                ((SerialIOProcessor)(Target = new SerialIOProcessor(this, ID))).setParameters(Baudrate, DATABITS_7, STOPBITS_1, PARITY_ODD);
            }
            Target.open(InIOError);
            InIOError = false;
        } catch (JposException e) {
            Target = null;
            return e;
        }
        return null;
    }

    /**
     * Closes the port
     * @param doFlush Specifies whether the output stream shall be flushed befor close.
     * @return In case of an IO error, the corresponding exception. Otherwise null
     */
    @SuppressWarnings("UnusedReturnValue")
    private JposException closePort(boolean doFlush) {
        JposException e = null;
        if (Target != null) {
            for (int i = 0; i < 2; i++) {
                try {
                    switch (i) {
                        case 0:
                            if (doFlush)
                                Target.flush();
                            i++;
                        case 1:
                            Target.close();
                    }
                } catch (JposException ee) {
                    e = ee;
                }
            }
            Target = null;
        }
        return e;
    }

    /**
     * Checks whether the target is still present. Keep in mind: If target is connected via TCP, any read or write
     * will result in an exception in case of disconnect. In case of a COM port, it depends on the USB driver whether
     * a read or write will generate an exception in case of a disconnect of any Usb2Serial adapter.
     * @return true if no disconnect could be detected, false otherwise.
     */
    private boolean exists() {
        if (Target == null)
            return false;
        if (Target instanceof TcpClientIOProcessor)
            return true;
        return ((SerialIOProcessor) Target).exists();
    }

    /*
     Control characters used in scale dialog 04
     */
    private static final byte ACK = 6;
    private static final byte NAK = 0x15;
    private static final byte STX = 2;
    private static final byte ESC = 0x1b;
    private static final byte ETX = 3;
    private static final byte ENQ = 5;
    private static final byte EOT = 4;

    @SuppressWarnings("ThrowableInstanceNeverThrown")
    synchronized private String sendCommand(String command, int timeout) {
        String resp;
        long starttime = System.currentTimeMillis();
        if (Target == null) {
            JposException e = initPort();
            if (e != null) {
                Offline = JPOS_PS_OFF_OFFLINE;
                return null;
            }
        }
        try {
            for (int retry = 0; !CommandProcessor.ToBeFinished && System.currentTimeMillis() - starttime <= timeout && retry <= MaxRetry; retry++) {
                byte[] request = ("\4" + command).getBytes(AsciiCoder);
                Target.flush();
                Target.write(request);
                Target.setTimeout(RequestTimeout);
                byte[] part = Target.read(1);
                if (part.length != 0) {
                    switch (part[0]) {
                        case NAK:   // Request state if not state request
                            if (request.length < 4 || request[3] != '8') {
                                --retry;
                                command = "\2" + "08\3";
                                continue;
                            }
                        case ACK:   // Request orderly finished
                            Target.write(new byte[]{EOT});
                            Offline = JPOS_PS_ONLINE;
                            return new String(part);
                        case STX:   // Request orderly finished, record type 02 or 09 follows
                            resp = new String(part);
                            Target.setTimeout(CharacterTimeout);
                            part = Target.read(request[1] == ENQ ? 25 : 6);
                            if (part.length >= 6 && part[0] == '0' && part[2] == ESC && part[part.length - 1] == ETX) {
                                Target.write(new byte[]{EOT});
                                Offline = JPOS_PS_ONLINE;
                                return resp + new String(part);
                            }
                    }
                }
                Target.write(new byte[]{EOT});
                if (!exists())
                    throw new JposException(JPOS_E_NOHARDWARE, "Connection to scale lost");
            }
        } catch (JposException e) {
            log(TRACE, getClaimingInstance(ClaimedScale, 0).LogicalName + ": IO error: " + e.getMessage());
        }
        Offline = JPOS_PS_OFF_OFFLINE;
        closePort(false);
        InIOError = true;
        return null;
    }
}
