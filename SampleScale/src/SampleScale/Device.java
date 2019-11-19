package SampleScale;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.micr.Data;
import de.gmxhome.conrad.jpos.jpos_base.scale.*;
import jpos.JposConst;
import jpos.JposException;
import jpos.ScaleConst;
import jpos.config.JposEntry;
import org.apache.log4j.Level;

import javax.swing.*;
import java.nio.charset.Charset;

/**
 * Implementation of a JposDevice based implementation of a scales driver that becomes
 * a JavaPOS Scales service in combination with the JposScale class
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
        try {
            Target = new TcpClientIOProcessor(this, id);
            OwnPort = 0;   // Default: Random port
        } catch (JposException e) {
        }
        scaleInit(1);
        PhysicalDeviceDescription = "Scales Dialog 02 / 04 simulator";
        PhysicalDeviceName = "Scales Simulator";
        CapPowerReporting = JposConst.JPOS_PR_STANDARD;
    }

    /**
     * Maximum no. of retries before service gives up communication.
     */
    int MaxRetry = 0;

    /**
     * In case of a TCP connection, values above 0 specify the source port number. Zero results in an random source port.
     */
    int OwnPort;

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
    int WeightUnit = ScaleConst.SCAL_WU_KILOGRAM;

    private boolean InIOError = false;
    private String ScaleText = "";
    private Charset AsciiCoder = Charset.availableCharsets().get("US-ASCII");

    @Override
    public void checkProperties(JposEntry entry) throws JposException {
        super.checkProperties(entry);
        try {
            Object o;
            if ((o = entry.getPropertyValue("OwnPort")) != null) {
                if (Target != null) {
                    if ((OwnPort = Integer.parseInt(o.toString())) < 0 || OwnPort >= 0xffff)
                        throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid source port.");
                }
                else
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid property for Scale Dialog 04.");
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
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property", e);
        }
    }

    @Override
    public void changeDefaults(ScaleProperties props) {
        props.CapPriceCalculating = true;
        props.CapDisplayText = true;
        props.CapDisplay = true;
        props.CapTareWeight = true;
        props.MaxDisplayTextChars = 13;
        props.MaximumWeight = MaximumWeight;
        props.TareWeightDef = DefaultTara;
        props.WeightUnit = WeightUnit;
        props.DeviceServiceDescription = "Scales service for Scales Dialog 02/04 simulator";
        CapPowerReporting = JposConst.JPOS_PR_STANDARD;
        props.DeviceServiceVersion = 1014001;
    }

    private Thread CommandProcessor;
    private SyncObject SignalStatusUpdated = null;
    private SyncObject WaitCommand = new SyncObject();
    private boolean ToBeFinished;
    private int Offline = JposConst.JPOS_PS_UNKNOWN;
    /**
     * Thread main, used for status check loop while device is enabled.
     */
    @Override
    public void run() {
        Target = null;
        int offline = Offline;
        while (!ToBeFinished) {
            long timeval = System.currentTimeMillis();
            String response = sendCommand("\2" + "08\3", (MaxRetry + 1) * (RequestTimeout + CharacterTimeout) + 70);
            JposCommonProperties props = getClaimingInstance(ClaimedScale, 0);
            if (!ToBeFinished && offline != Offline && Offline == JposConst.JPOS_PS_ONLINE) {
                try {
                    handleEvent(new JposStatusUpdateEvent(props.EventSource, JposConst.JPOS_SUE_POWER_ONLINE));
                } catch (JposException e) {
                }
                offline = Offline;
            }
            synchronized(CommandProcessor) {
                if (SignalStatusUpdated != null) {
                    SignalStatusUpdated.signal();
                    SignalStatusUpdated = null;
                }
            }
            if (!ToBeFinished && offline != Offline) {
                try {
                    handleEvent(new JposStatusUpdateEvent(props.EventSource, JposConst.JPOS_SUE_POWER_OFF_OFFLINE));
                } catch (JposException e) {
                }
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
        SampleScaleAccessor() {
            super(0);
        }

        @Override
        public void deviceEnabled(boolean enable) throws JposException {
            if (enable) {
                int timeout = (MaxRetry + 2) * RequestTimeout;
                SyncObject waiter = SignalStatusUpdated = new SyncObject();
                ToBeFinished = false;
                (CommandProcessor = new Thread(SampleScale.Device.this)).start();
                CommandProcessor.setName(LogicalName +".StatusHandler");
                waiter.suspend(timeout);
            }
            else {
                ToBeFinished = true;
                WaitCommand.signal();
                while (ToBeFinished) {
                    try {
                        CommandProcessor.join();
                    } catch (Exception e) {}
                    break;
                }
                JposException e = closePort(true);
                Offline = JposConst.JPOS_PS_UNKNOWN;
                InIOError = false;
            }
            super.deviceEnabled(enable);
        }

        @Override
        public void handlePowerStateOnEnable() throws JposException {
            PowerState = SignalStatusUpdated != null || InIOError ? JposConst.JPOS_PS_OFF_OFFLINE : JposConst.JPOS_PS_ONLINE;
            super.handlePowerStateOnEnable();
        }

        @Override
        public void checkHealth(int level) throws JposException {
            if (level == JposConst.JPOS_CH_INTERNAL) {
                CheckHealthText = "Internal CheckHealth: ";
                CheckHealthText += InIOError || member(PowerState, new long[]{JposConst.JPOS_PS_OFFLINE, JposConst.JPOS_PS_OFF, JposConst.JPOS_PS_OFF_OFFLINE}) ? "Failed" : "OK";
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
            int[] weight = new int[1];
            int msgtype = JOptionPane.INFORMATION_MESSAGE;
            CheckHealthText = level == JposConst.JPOS_CH_EXTERNAL ? "External CheckHealth: " : "Interactive CheckHealth: ";
            try {
                if (async)
                    asyncMode(false);
                UnitPrice = 123400;  // 12.34
                srv.displayText("WEIGHING");
                if (level == JposConst.JPOS_CH_INTERACTIVE)
                    synchronizedMessageBox("Put something on scale", "CheckHealth Scale", msgtype);
                srv.readWeight(weight, level == JposConst.JPOS_CH_EXTERNAL ? Integer.MAX_VALUE : MaxRetry * RequestTimeout);
                CheckHealthText += "OK";
            } catch (JposException e) {
                CheckHealthText += "Failed (" +e.getMessage() + ")";
                msgtype = JOptionPane.ERROR_MESSAGE;
            }
            try {
                if (UnitPrice != price || ScaleText != text) {
                    UnitPrice = price;
                    displayText(text);
                }
                if (async)
                    asyncMode(async);
            } catch (JposException e) {}
            if (level == JposConst.JPOS_CH_INTERACTIVE)
                synchronizedMessageBox(CheckHealthText, "CheckHealth Scale", msgtype);
        }

        @Override
        public void tareWeight(int weight) throws JposException {
            if (weight >= 5000)
                throw new JposException(JposConst.JPOS_E_ILLEGAL, "Tare too high");
            String request = String.format("\2%02d\33%06d\33%04d\3", 3, UnitPrice / 100, weight);
            sendSetupCommand(request);
            super.tareWeight(weight);
        }

        private void sendSetupCommand(String request) throws JposException {
            String response = sendCommand(request, Integer.MAX_VALUE);
            if (response == null)
                throw new JposException(JposConst.JPOS_E_OFFLINE, "Communication error");
            if (!response.equals("\6")) {
                if (response.equals("\15"))
                    throw new JposException(JposConst.JPOS_E_FAILURE, "Negative acknowledge");
                if (response.length() == 7 && response.charAt(0) == STX && response.charAt(3) == ESC && response.charAt(6) == ETX &&
                        response.substring(1, 3).equals("09")) {
                    int reason = Integer.parseInt(response.substring(4, 6), 10);
                    if (reason != 0)
                        throw new JposException(JposConst.JPOS_E_FAILURE, "Scale in error state: " + reason);
                }
                throw new JposException(JposConst.JPOS_E_FAILURE, "Invalid response");
            }
        }

        @Override
        public void unitPrice(long price) throws JposException {
            if (price >= 100000000)
                throw new JposException(JposConst.JPOS_E_ILLEGAL, "Unit price too high");
            String request = String.format("\2%02d\33%06d\33\3", 1, price / 100);
            sendSetupCommand(request);
            super.unitPrice(price);
        }

        @Override
        public void zeroValid(boolean b) throws JposException {
            if (b)
                throw new JposException(JposConst.JPOS_E_ILLEGAL, 0, "Valid zero weight not supported by sample scale");
        }

        @Override
        public void displayText(String description) throws JposException {
            super.displayText(description);
            String request = String.format("\2%02d\33%06d\33%-13s\3", 4, UnitPrice / 100, description);
            sendSetupCommand(request);
            ScaleText = description;
        }

        @Override
        public void readWeight(int[] weight, int timeout) throws JposException {
            check(State == JposConst.JPOS_S_ERROR, JposConst.JPOS_E_BUSY, "Device busy (in error)");
            new ReadWeight(this, weight[0], timeout).throwRequest();
        }

        @Override
        public void readWeight(ReadWeight request) throws JposException {
            int timeout = request.getTimeout() == JposConst.JPOS_FOREVER ? Integer.MAX_VALUE : request.getTimeout();
            if (timeout < (MaxRetry + 2) * RequestTimeout)
                timeout = (MaxRetry + 2) * RequestTimeout;
            long startTime = System.currentTimeMillis();
            while (true) {
                String response = sendCommand("\5", timeout);
                try {
                    Device.check(response.length() != 7 && response.length() != 26, JposConst.JPOS_E_FAILURE, "Bad frame size");
                    if (response.length() == 7 && response.charAt(0) == STX && response.charAt(6) == ETX && response.charAt(3) == ESC && response.substring(1, 3).equals("09")) {
                        int state = Integer.parseInt(response.substring(4, 6), 10);
                        switch (state) {
                            case 20:    // Still in motion: Do it again (no remove of finalizer)
                            case 30:    // Weight less than minimum (zero weight)
                            case 31:    // Scale less than zero
                                Device.check(System.currentTimeMillis() - startTime > timeout, JposConst.JPOS_E_TIMEOUT, "No valid weight within time limit");
                                break;
                            case 21:    // Not in motion since last weighing
                                throw new JposException(JposConst.JPOS_E_EXTENDED, ScaleConst.JPOS_ESCAL_SAME_WEIGHT, "Not in motion since last weighing");
                            case 22:    // No price calculation (unit price 0), should never occur
                                throw new JposException(JposConst.JPOS_E_ILLEGAL, 0, "UnitPrice has not been set");
                            case 32:    // Scale is overloaded
                                throw new JposException(JposConst.JPOS_E_EXTENDED, ScaleConst.JPOS_ESCAL_OVERWEIGHT, "Scale overloaded");
                            default:
                                throw new JposException(JposConst.JPOS_E_FAILURE, 0, "Unknown scale status: " + state);
                        }
                        new SyncObject().suspend(PollDelay);
                    } else if (response.length() == 26 && response.charAt(0) == STX && response.charAt(3) == ESC && response.charAt(5) == ESC &&
                            response.charAt(11) == ESC && response.charAt(18) == ESC && response.charAt(25) == ETX &&
                            response.substring(1, 3).equals("02") && response.charAt(4) == 0x33) {
                        Device.check(Long.parseLong(response.substring(12, 18), 10) * 100 != UnitPrice, JposConst.JPOS_E_FAILURE, "Unexpected unit price");
                        request.WeightData = Integer.parseInt(response.substring(6, 11), 10);
                        request.SalesPrice = Long.parseLong(response.substring(19, 25), 10) * 100;
                        return;
                    } else
                        throw new JposException(JposConst.JPOS_E_FAILURE, 0, "Invalid frame structure");
                } catch (NumberFormatException e) {
                    Device.check(System.currentTimeMillis() - startTime < timeout, JposConst.JPOS_E_TIMEOUT, "No valid weight within time limit");
                } catch (NullPointerException e) {
                    throw new JposException(JposConst.JPOS_E_FAILURE, 0, "Offline");
                }
            }
        }

        @Override
        public void doPriceCalculating(int[] weightData, int[] tare, long[] unitPrice, long[] unitPriceX, int[] weightUnitX, int[] weightNumeratorX, int[] weightDenominatorX, long[] price, int timeout) throws JposException {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Parameter values for unitPriceX, weightUnitX, weightNumeratorX and weightDenominatorX are not available.");
        }
    }

    /**
     * Port initialization.
     * @return In case of initialization error, the exception. Otherwise null.
     */
    private JposException initPort() {
        try {
            try {
                ((TcpClientIOProcessor)(Target = new TcpClientIOProcessor(this, ID))).setParam(OwnPort);
            } catch (JposException e) {
                ((SerialIOProcessor)(Target = new SerialIOProcessor(this, ID))).setParameters(SerialIOProcessor.BAUDRATE_4800, SerialIOProcessor.DATABITS_7, SerialIOProcessor.STOPBITS_1, SerialIOProcessor.PARITY_ODD);
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
    private static final byte NAK = 025;
    private static final byte STX = 2;
    private static final byte ESC = 033;
    private static final byte ETX = 3;
    private static final byte ENQ = 5;
    private static final byte EOT = 4;

    synchronized private String sendCommand(String command, int timeout) {
        String resp;
        long starttime = System.currentTimeMillis();
        if (Target == null) {
            JposException e = initPort();
            if (e != null) {
                Offline = JposConst.JPOS_PS_OFF_OFFLINE;
                return null;
            }
        }
        try {
            for (int retry = 0; !ToBeFinished && System.currentTimeMillis() - starttime <= timeout && retry <= MaxRetry; retry++) {
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
                            Offline = JposConst.JPOS_PS_ONLINE;
                            return new String(part);
                        case STX:   // Request orderly finished, record type 02 or 09 follows
                            resp = new String(part);
                            Target.setTimeout(CharacterTimeout);
                            part = Target.read(request[1] == ENQ ? 25 : 6);
                            if (part.length >= 6 && part[0] == '0' && part[2] == ESC && part[part.length - 1] == ETX) {
                                Target.write(new byte[]{EOT});
                                Offline = JposConst.JPOS_PS_ONLINE;
                                return resp + new String(part);
                            }
                    }
                }
                Target.write(new byte[]{EOT});
                if (!exists())
                    throw new JposException(JposConst.JPOS_E_NOHARDWARE, "Connection to scale lost");
            }
        } catch (JposException e) {
            log(Level.TRACE, getClaimingInstance(ClaimedScale, 0).LogicalName + ": IO error: " + e.getMessage());
        }
        Offline = JposConst.JPOS_PS_OFF_OFFLINE;
        closePort(false);
        InIOError = true;
        return null;
    }
}
