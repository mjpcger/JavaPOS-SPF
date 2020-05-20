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
import org.apache.log4j.Level;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.Arrays;

/**
 * Class to process serial communication. Current implementation uses jSSC implementation
 * for serial communication. Includes functionality for automatic data logging.
 */
public class SerialIOProcessor extends UniqueIOProcessor {
    private static boolean JsscSerialPort = false;
    static Constructor NewSerialPort = null;
    static Method OpenPort = null;
    static Method ClosePort = null;
    static Method SetParams = null;
    static Method GetInputBufferBytesCount = null;
    static Method ReadBytes = null;
    static Method ReadBytesWithTimeout = null;
    static Method WriteBytes = null;
    static Method PurgePort = null;
    static Method GetExceptionType = null;
    static String TYPE_PORT_NOT_FOUND = null;
    private static Integer PurgeMode = null;

    private static boolean JSerialComSerialPort = false;
    static Method GetCommPort = null;
    static Method SetComPortParameters = null;
    static Method BytesAvailable = null;
    static Method SetComPortTimeouts = null;
    private static Integer TimeoutMode = null;

    private static boolean JnaSerialSerialPort = false;

    static {
        Class theclass = null;
        Exception ex = null;
        if (System.getProperty("java.version").split("\\.")[0].equals("1")) {
            try {
                theclass = Class.forName("jssc.SerialPort");
                hBAUDRATE_1200 = theclass.getField("BAUDRATE_1200").getInt(null);
                hBAUDRATE_4800 = theclass.getField("BAUDRATE_4800").getInt(null);
                hBAUDRATE_9600 = theclass.getField("BAUDRATE_9600").getInt(null);
                hBAUDRATE_19200 = theclass.getField("BAUDRATE_19200").getInt(null);
                hBAUDRATE_38400 = theclass.getField("BAUDRATE_38400").getInt(null);
                hBAUDRATE_57600 = theclass.getField("BAUDRATE_57600").getInt(null);
                hBAUDRATE_115200 = theclass.getField("BAUDRATE_115200").getInt(null);
                hBAUDRATE_128000 = theclass.getField("BAUDRATE_128000").getInt(null);
                hBAUDRATE_256000 = theclass.getField("BAUDRATE_256000").getInt(null);
                hPARITY_NONE = theclass.getField("PARITY_NONE").getInt(null);
                hPARITY_ODD = theclass.getField("PARITY_ODD").getInt(null);
                hPARITY_EVEN = theclass.getField("PARITY_EVEN").getInt(null);
                hPARITY_MARK = theclass.getField("PARITY_MARK").getInt(null);
                hPARITY_SPACE = theclass.getField("PARITY_SPACE").getInt(null);
                hDATABITS_7 = theclass.getField("DATABITS_7").getInt(null);
                hDATABITS_8 = theclass.getField("DATABITS_8").getInt(null);
                hSTOPBITS_1 = theclass.getField("STOPBITS_1").getInt(null);
                hSTOPBITS_2 = theclass.getField("STOPBITS_2").getInt(null);
                PurgeMode = theclass.getField("PURGE_RXABORT").getInt(null) | theclass.getField("PURGE_TXABORT").getInt(null);

                NewSerialPort = theclass.getConstructor(String.class);
                OpenPort = theclass.getMethod("openPort");
                SetParams = theclass.getMethod("setParams", Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
                ClosePort = theclass.getMethod("closePort");
                WriteBytes = theclass.getMethod("writeBytes", byte[].class);
                PurgePort = theclass.getMethod("purgePort", Integer.TYPE);
                GetInputBufferBytesCount = theclass.getMethod("getInputBufferBytesCount");
                ReadBytes = theclass.getMethod("readBytes", Integer.TYPE);
                ReadBytesWithTimeout = theclass.getMethod("readBytes", Integer.TYPE, Integer.TYPE);

                GetExceptionType = Class.forName("jssc.SerialPortException").getMethod("getExceptionType");
                TYPE_PORT_NOT_FOUND = (String) Class.forName("jssc.SerialPortException").getField("TYPE_PORT_NOT_FOUND").get(null);
                JsscSerialPort = true;
                System.console().printf("Use jSSC for serial communication");
            } catch (ClassNotFoundException e) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!JsscSerialPort) {
            try {
                theclass = Class.forName("com.fazecast.jSerialComm.SerialPort");
                hBAUDRATE_1200 = 1200;
                hBAUDRATE_4800 = 4800;
                hBAUDRATE_9600 = 9600;
                hBAUDRATE_19200 = 19200;
                hBAUDRATE_38400 = 38400;
                hBAUDRATE_57600 = 57600;
                hBAUDRATE_115200 = 115200;
                hBAUDRATE_128000 = 128000;
                hBAUDRATE_256000 = 256000;
                hPARITY_NONE = theclass.getField("NO_PARITY").getInt(null);
                hPARITY_ODD = theclass.getField("ODD_PARITY").getInt(null);
                hPARITY_EVEN = theclass.getField("EVEN_PARITY").getInt(null);
                hPARITY_MARK = theclass.getField("MARK_PARITY").getInt(null);
                hPARITY_SPACE = theclass.getField("SPACE_PARITY").getInt(null);
                hDATABITS_7 = 7;
                hDATABITS_8 = 8;
                hSTOPBITS_1 = theclass.getField("ONE_STOP_BIT").getInt(null);
                hSTOPBITS_2 = theclass.getField("TWO_STOP_BITS").getInt(null);
                TimeoutMode = theclass.getField("TIMEOUT_WRITE_BLOCKING").getInt(null) | theclass.getField("TIMEOUT_READ_BLOCKING").getInt(null);

                GetCommPort = theclass.getMethod("getCommPort", String.class);
                OpenPort = theclass.getMethod("openPort");
                ClosePort = theclass.getMethod("closePort");
                SetComPortParameters = theclass.getMethod("setComPortParameters", Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
                BytesAvailable = theclass.getMethod("bytesAvailable");
                ReadBytes = theclass.getMethod("readBytes", byte[].class, Long.TYPE);
                WriteBytes = theclass.getMethod("writeBytes", byte[].class, Long.TYPE, Long.TYPE);
                SetComPortTimeouts = theclass.getMethod("setComPortTimeouts", Integer.TYPE, Integer.TYPE, Integer.TYPE);
                JSerialComSerialPort = true;
                System.console().printf("Use jSerialComm for serial communication");
            } catch (ClassNotFoundException e) {
                ex = e;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!JsscSerialPort && !JSerialComSerialPort) {
            try {
                new JnaSerial();
                hBAUDRATE_1200 = 1200;
                hBAUDRATE_4800 = 4800;
                hBAUDRATE_9600 = 9600;
                hBAUDRATE_19200 = 19200;
                hBAUDRATE_38400 = 38400;
                hBAUDRATE_57600 = null;
                hBAUDRATE_115200 = null;
                hBAUDRATE_128000 = 128000;
                hBAUDRATE_256000 = 256000;
                hPARITY_NONE = JnaSerial.P_NO;
                hPARITY_ODD = JnaSerial.P_ODD;
                hPARITY_EVEN = JnaSerial.P_EVEN;
                hPARITY_MARK = JnaSerial.P_MARK;
                hPARITY_SPACE = JnaSerial.P_SPACE;
                hDATABITS_7 = 7;
                hDATABITS_8 = 8;
                hSTOPBITS_1 = 1;
                hSTOPBITS_2 = 2;
                JnaSerialSerialPort = true;
            } catch (JposException e) {
                ex.printStackTrace();
            }
        }
    }

    // Baudrate constants
    private static Integer hBAUDRATE_1200;
    private static Integer hBAUDRATE_4800;
    private static Integer hBAUDRATE_9600;
    private static Integer hBAUDRATE_19200;
    private static Integer hBAUDRATE_38400;
    private static Integer hBAUDRATE_57600;
    private static Integer hBAUDRATE_115200;
    private static Integer hBAUDRATE_128000;
    private static Integer hBAUDRATE_256000;

    /**
     * Baudrate constant for 1200 baud.
     */
    public final static int BAUDRATE_1200 = 1200;
    /**
     * Baudrate constant for 4800 baud.
     */
    public final static int BAUDRATE_4800 = 4800;
    /**
     * Baudrate constant for 9600 baud.
     */
    public final static int BAUDRATE_9600 = 9600;
    /**
     * Baudrate constant for 19200 baud.
     */
    public final static int BAUDRATE_19200 = 19200;
    /**
     * Baudrate constant for 38400 baud.
     */
    public final static int BAUDRATE_38400 = 38400;
    /**
     * Baudrate constant for 57600 baud.
     */
    public final static int BAUDRATE_57600 = 57600;
    /**
     * Baudrate constant for 115200 baud.
     */
    public final static int BAUDRATE_115200 = 115200;
    /**
     * Baudrate constant for 128000 baud.
     */
    public final static int BAUDRATE_128000 = 128000;
    /**
     * Baudrate constant for 256000 baud.
     */
    public final static int BAUDRATE_256000 = 256000;

    /**
     * Baudrate to be used. Default: BAUDRATE_9600.
     */
    protected int Baudrate = BAUDRATE_9600;

    // Parity constants
    private static int hPARITY_NONE;
    private static int hPARITY_ODD;
    private static int hPARITY_EVEN;
    private static int hPARITY_MARK;
    private static int hPARITY_SPACE;

    /**
     * Parity constant for no parity.
     */
    public final static int PARITY_NONE = 0;
    /**
     * Parity constant for odd parity.
     */
    public final static int PARITY_ODD = 1;
    /**
     * Parity constant for even parity.
     */
    public final static int PARITY_EVEN = 2;
    /**
     * Parity constant for mark parity.
     */
    public final static int PARITY_MARK = 3;
    /**
     * Parity constant for space parity.
     */
    public final static int PARITY_SPACE = 4;

    /**
     * Parity to be used. Default: PARITY_NONE.
     */
    protected int Parity = hPARITY_NONE;

    // Bit size constants
    private static int hDATABITS_7;
    private static int hDATABITS_8;

    /**
     * Constant for 7 bit data size.
     */
    public final static int DATABITS_7 = 7;
    /**
     * Constant for 8 bit data size.
     */
    public final static int DATABITS_8 = 8;

    /**
     * Data bits to be used. Default: DATABITS_8.
     */
    protected int Databits = DATABITS_8;

    //Stop bits constants
    private static int hSTOPBITS_1;
    private static int hSTOPBITS_2;

    /**
     * Constant for serial communication using 1 stop bit.
     */
    public final static int STOPBITS_1 = 1;
    /**
     * Constant for serial communication using 2 stop bits.
     */
    public final static int STOPBITS_2 = 2;

    /**
     * Stop bits to be used. Default: STOPBITS_2.
     */
    protected int Stopbits = STOPBITS_2;

    /**
     * jSSC implementation: jSSC simple serial connector used for serial communication.
     */
    Object SerialIOExecutor;

    // For parameter setting:
    private boolean ParametersSet = false;

    private void log(Level level, String message) {
        Dev.log(level, message);
    }

    /**
     * Creates simple serial connector for given port
     *
     * @param usingDevice Device that uses the proceessor. Processor uses logging of device
     *                    to produce logging entries
     * @param portName    Name of port. Example: COM1
     * @throws JposException Will not be thrown in case of serial communication.
     */
    public SerialIOProcessor(JposDevice usingDevice, String portName) throws JposException {
        super(usingDevice, portName);
    }

    @Override
    /**
     * write byte buffer to serial port
     * @param buffer Buffer to be written
     * @return length written
     * @throws JposException In case of an IO error
     */
    public int write(byte[] buffer) throws JposException {
        synchronized (WriteSynchronizer) {
            if (SerialIOExecutor == null)
                logerror("Write", JposConst.JPOS_E_ILLEGAL, "Port not open");
            try {
                if (JsscSerialPort) {
                    writeJssc(buffer);
                } else if (JSerialComSerialPort) {
                    writeJSerialCom(buffer);
                } else if (JnaSerialSerialPort) {
                    ((JnaSerial) SerialIOExecutor).write(buffer);
                } else
                    logerror("Write", JposConst.JPOS_E_FAILURE, "No implementation for serial I/O");
                return super.write(buffer);
            } catch (JposException e) {
                throw e;
            } catch (Exception e) {
                if (e instanceof InvocationTargetException)
                    e = (Exception) ((InvocationTargetException) e).getTargetException();
                return logerror("Write", JposConst.JPOS_E_FAILURE, e);
            }
        }
    }

    private void writeJSerialCom(byte[] buffer) throws IllegalAccessException, InvocationTargetException, IOException {
        int sent = 0;
        while (sent < buffer.length) {
            int partlen = (Integer) WriteBytes.invoke(SerialIOExecutor, buffer, buffer.length - sent, sent);
            if (partlen <= 0)
                throw new IOException("Cannot write to port " + Port);
            sent += partlen;
        }
    }

    private void writeJssc(byte[] buffer) throws IllegalAccessException, InvocationTargetException, JposException {
        if (!(Boolean) WriteBytes.invoke(SerialIOExecutor, buffer))
            logerror("Write", JposConst.JPOS_E_FAILURE, "Write error");
    }

    @Override
    /**
     * Returns no. of bytes available in input buffer
     * @return Input buffer byte count
     * @throws JposException In case of an IO error
     */
    public int available() throws JposException {
        int count = 0;
        if (SerialIOExecutor == null)
            logerror("Available", JposConst.JPOS_E_ILLEGAL, "Port not open");
        try {
            if (JsscSerialPort)
                count = (Integer) GetInputBufferBytesCount.invoke(SerialIOExecutor);
            else if (JSerialComSerialPort)
                count = (Integer) BytesAvailable.invoke(SerialIOExecutor);
            else if (JnaSerialSerialPort)
                count = ((JnaSerial) SerialIOExecutor).available();
            else
                logerror("Available", JposConst.JPOS_E_FAILURE, "No implementation for serial I/O");
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            if (e instanceof InvocationTargetException)
                e = (Exception) ((InvocationTargetException) e).getTargetException();
            logerror("Available", JposConst.JPOS_E_FAILURE, e);
        }
        if (count >= 0) {
            LoggingData = String.valueOf(count).getBytes();
            return super.available();
        }
        return logerror("Available", JposConst.JPOS_E_FAILURE, "Bad connector");
    }

    @Override
    /**
     * Reads count bytes from input buffer
     * @param count No. of bytes to be read
     * @return byte[] containing received bytes. In case of timeout, less than count
     * bytes will be returned.
     * @throws JposException In case of an IO error
     */
    public byte[] read(int count) throws JposException {
        synchronized (ReadSynchronizer) {
            boolean retry = false;
            int timeout = Timeout;
            if (SerialIOExecutor == null)
                logerror("Read", JposConst.JPOS_E_ILLEGAL, "Port not open");
            byte[] result = new byte[count];
            if (count == 0)
                return result;
            while (true) {
                try {
                    if (JsscSerialPort) {
                        result = readJssc(count, timeout, result);
                    } else if (JSerialComSerialPort) {
                        result = readJSerialCom(count, timeout, result);
                    } else if (JnaSerialSerialPort) {
                        result = ((JnaSerial) SerialIOExecutor).read(count, timeout);
                    } else
                        logerror("Read", JposConst.JPOS_E_FAILURE, "No implementation for serial I/O");
                    LoggingData = result;
                    return super.read(count);
                } catch (JposException e) {
                    throw e;
                } catch (Exception e) {
                    if (e instanceof InvocationTargetException)
                        e = (Exception) ((InvocationTargetException) e).getTargetException();
                    if (e.getClass().getSimpleName().equals("SerialPortException"))
                        logerror("Read", JposConst.JPOS_E_FAILURE, e);
                    else if (e.getClass().getSimpleName().equals("SerialPortTimeoutException")) {
                        if (!retry && (count = available()) > 0) {
                            timeout = 0;
                            retry = true;
                            continue;
                        }
                    }
                }
                break;
            }
            log(Level.TRACE, LoggingPrefix + "Input timeout");
            return new byte[0];
        }
    }

    private byte[] readJSerialCom(int count, int timeout, byte[] result) throws IllegalAccessException, InvocationTargetException, IOException {
        int len = (Integer) BytesAvailable.invoke(SerialIOExecutor);
        if (len < 0)
            throw new IOException("Port not open error on port " + Port);
        else if (len < count) {
            if (!(Boolean) SetComPortTimeouts.invoke(SerialIOExecutor, TimeoutMode, timeout == 0 ? 1 : timeout, 0))
                throw new IOException("Timeout cannot be set on port " + Port);
        }
        if ((len = (Integer) ReadBytes.invoke(SerialIOExecutor, result, count)) < 0)
            throw new IOException("Read error on port " + Port);
        result = Arrays.copyOf(result, len);
        return result;
    }

    private byte[] readJssc(int count, int timeout, byte[] result) throws InvocationTargetException, IllegalAccessException {
        int len = (Integer) GetInputBufferBytesCount.invoke(SerialIOExecutor);
        if (len > 0)
            result = (byte[]) ReadBytes.invoke(SerialIOExecutor, len <= count ? len : count);
        else {
            result = (byte[]) ReadBytesWithTimeout.invoke(SerialIOExecutor, 1, timeout);
            if (result.length == 1 && count > 1 && (len = (Integer) GetInputBufferBytesCount.invoke(SerialIOExecutor)) > 0) {
                byte[] part = (byte[]) ReadBytes.invoke(SerialIOExecutor, len < count ? len : count - 1);
                result = Arrays.copyOf(result, len + 1);
                System.arraycopy(part, 0, result, 1, part.length);
            }
        }
        return result;
    }

    @Override
    /**
     * Flushes input and output buffer
     * @throws JposException In case of an IO error
     */
    public void flush() throws JposException {
        if (SerialIOExecutor == null)
            logerror("Flush", JposConst.JPOS_E_ILLEGAL, "Port not open error");
        try {
            if (JsscSerialPort)
                PurgePort.invoke(SerialIOExecutor, PurgeMode);
            if (JSerialComSerialPort)
                flushJSerialCom();
            else if (JnaSerialSerialPort) {
                ((JnaSerial) SerialIOExecutor).flush();
            } else
                logerror("Flush", JposConst.JPOS_E_FAILURE, "No implementation for serial I/O");
            super.flush();
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            if (e instanceof InvocationTargetException)
                e = (Exception) ((InvocationTargetException) e).getTargetException();
            logerror("Flush", JposConst.JPOS_E_FAILURE, e);
        }
    }

    private void flushJSerialCom() throws IllegalAccessException, InvocationTargetException, IOException {
        // Not supported, but we can empty the input buffer
        int count;
        if (!(Boolean) SetComPortTimeouts.invoke(SerialIOExecutor, TimeoutMode, 1, 0))
            throw new IOException("Port not open error for " + Port);
        while ((count = (Integer) BytesAvailable.invoke(SerialIOExecutor)) > 0) {
            byte[] buffer = new byte[count + 100];
            int read = (Integer) ReadBytes.invoke(SerialIOExecutor, buffer, buffer.length);
            if (read < 0)
                break;
        }
        if (count < 0)
            throw new IOException("Access error for port " + Port);
    }

    /**
     * Opens the port. No error logging occurs in error case to avoid a flood of error messages in error cases.
     *
     * @throws JposException if an IO error occurs
     */
    public void open() throws JposException {
        open(true);
    }

    @Override
    /**
     * Opens the port
     * @param  noErrorLog  if set, no logging occurs in error case to avoid a flood
     *                     of error messages.
     * @throws JposException if an IO error occurs
     */
    public void open(boolean noErrorLog) throws JposException {
        if (SerialIOExecutor != null)
            logerror("Open", JposConst.JPOS_E_ILLEGAL, "Port just open");
        if (JsscSerialPort)
            SerialIOExecutor = openJssc(noErrorLog);
        else if (JSerialComSerialPort)
            SerialIOExecutor = openJSerialCom(noErrorLog);
        else if (JnaSerialSerialPort)
            SerialIOExecutor = openJna(noErrorLog);
        else
            logerror("Open", JposConst.JPOS_E_FAILURE, "No implementation for serial I/O");
        super.open(noErrorLog);
    }

    private Object openJna(boolean noErrorLog) throws JposException {
        try {
            JnaSerial obj = new JnaSerial();
            obj.open(Port);
            if (ParametersSet)
                obj.setParameters(Baudrate, Databits, Stopbits, Parity);
            return obj;
        } catch (JposException e) {
            if (!noErrorLog)
                Dev.log(Level.ERROR, LoggingPrefix + "Open error: " + e.getMessage());
            throw e;
        }
    }

    private Object openJSerialCom(boolean noErrorLog) throws JposException {
        Object portObj;
        try {
            portObj = GetCommPort.invoke(null, Port);
            boolean success = true;
            if (ParametersSet)
                success = (Boolean) SetComPortParameters.invoke(portObj, Baudrate, Databits, Stopbits, Parity);
            if (!success || !(Boolean) OpenPort.invoke(portObj))
                throw new IOException((success ? "Error opening " : "Error setting parameters for ") + Port);
            return portObj;
        } catch (Exception e) {
            if (e instanceof InvocationTargetException)
                e = (Exception) ((InvocationTargetException) e).getTargetException();
            if (!noErrorLog)
                log(Level.ERROR, LoggingPrefix + "Open error: " + e.getMessage());
            throw new JposException(e.getClass().getSimpleName().equals("SerialPortInvalidPortException") ?
                    JposConst.JPOS_E_NOEXIST : JposConst.JPOS_E_OFFLINE, IOProcessorError, e.getMessage(), e);
        }
    }

    private Object openJssc(boolean noErrorLog) throws JposException {
        Object portObj = null;
        boolean opened = false;
        try {
            portObj = NewSerialPort.newInstance(Port);
            OpenPort.invoke(portObj);
            if (opened = ParametersSet)
                SetParams.invoke(portObj, Baudrate, Databits, Stopbits, Parity);
            return portObj;
        } catch (Exception e) {
            if (e instanceof InvocationTargetException)
                e = (Exception) ((InvocationTargetException) e).getTargetException();
            if (!noErrorLog)
                log(Level.ERROR, LoggingPrefix + "Open error: " + e.getMessage());
            if (opened) {
                try {
                    ClosePort.invoke(portObj);
                } catch (Exception ex) {}
            }
            if (e.getClass().getSimpleName().equals("SerialPortException")) {
                try {
                    if (GetExceptionType.invoke(e).equals(TYPE_PORT_NOT_FOUND)) {
                        throw new JposException(JposConst.JPOS_E_NOEXIST, IOProcessorError, e.getMessage(), e);
                    }
                } catch (Exception ex) {}
            }
            throw new JposException(JposConst.JPOS_E_OFFLINE, IOProcessorError, e.getMessage(), e);
        }
    }

    @Override
    /**
     * Close the port
     * @throws JposException If an IO error occurs
     */
    public void close() throws JposException {
        if (SerialIOExecutor == null)
            logerror("Close", JposConst.JPOS_E_ILLEGAL, "Duplicate close error");
        try {
            if (JsscSerialPort)
                ClosePort.invoke(SerialIOExecutor);
            else if (JSerialComSerialPort)
                closeJSerialCom();
            else if (JnaSerialSerialPort)
                ((JnaSerial) SerialIOExecutor).close();
            else
                logerror("Close", JposConst.JPOS_E_FAILURE, "No implementation for serial I/O");
            super.close();
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            if (e instanceof InvocationTargetException)
                e = (Exception) ((InvocationTargetException) e).getTargetException();
            logerror("Close", JposConst.JPOS_E_FAILURE, e);
        } finally {
            SerialIOExecutor = null;
        }
    }

    private void closeJSerialCom() throws IllegalAccessException, InvocationTargetException, JposException {
        if (!(Boolean) ClosePort.invoke(SerialIOExecutor))
            logerror("Close", JposConst.JPOS_E_FAILURE, "Closing port failed");
    }

    /**
     * Sets communication parameters for serial communication (RS232)
     * @param baudrate  Baud rate, one of the predefined baudrate values.
     *                  Default BAUDRATE_9600
     * @param databits  Bits per byte, one of the predefined values.
     *                  Default DATABITS_8.
     * @param stopbits  Stop bits to be used when sending data, one of the prededined
     *                  values. Default STOPBITS_2.
     * @param parity    Parity to be used, one of the predefined values.
     *                  Default PARITY_NONE.
     * @throws JposException In case of any IO error
     */
    public void setParameters(int baudrate, int databits, int stopbits, int parity) throws JposException {
        Integer[][] validrates = {
                {BAUDRATE_1200, hBAUDRATE_1200},
                {BAUDRATE_4800, hBAUDRATE_4800},
                {BAUDRATE_9600, hBAUDRATE_9600},
                {BAUDRATE_19200, hBAUDRATE_19200},
                {BAUDRATE_38400, hBAUDRATE_38400},
                {BAUDRATE_57600, hBAUDRATE_57600},
                {BAUDRATE_115200, hBAUDRATE_115200},
                {BAUDRATE_128000, hBAUDRATE_128000},
                {BAUDRATE_256000, hBAUDRATE_256000}
        };
        Integer[][] validdatabits = {{DATABITS_7, hDATABITS_7}, {DATABITS_8, hDATABITS_8}};
        Integer[][] validstopbits = {{STOPBITS_1, hSTOPBITS_1}, {STOPBITS_2, hSTOPBITS_2}};
        Integer[][] validparities = {
                {PARITY_NONE, hPARITY_NONE},
                {PARITY_ODD, hPARITY_ODD},
                {PARITY_EVEN, hPARITY_EVEN},
                {PARITY_MARK, hPARITY_MARK},
                {PARITY_SPACE, hPARITY_SPACE}
        };
        Baudrate = validate(baudrate, validrates, "baud rate");
        Databits = validate(databits, validdatabits, "data size");
        Stopbits = validate(stopbits, validstopbits, "number of stop bits");
        Parity = validate(parity, validparities, "parity type");
        ParametersSet = true;
        if (SerialIOExecutor != null) {
            try {
                if (JsscSerialPort) {
                    SetParams.invoke(SerialIOExecutor, Baudrate, Databits, Stopbits, Parity);
                }
                else if (JSerialComSerialPort) {
                    setParametersJSerialCom();
                }
                else if (JnaSerialSerialPort) {
                    ((JnaSerial) SerialIOExecutor).setParameters(Baudrate, Databits, Stopbits, Parity);
                }
                else
                    logerror("SetParameters", JposConst.JPOS_E_FAILURE, "No implementation for serial I/O");
            } catch (JposException e) {
                throw e;
            } catch (Exception e) {
                if (e instanceof InvocationTargetException)
                    e = (Exception) ((InvocationTargetException)e).getTargetException();
                logerror("SetParameters", JposConst.JPOS_E_ILLEGAL, e);
            }
        }
        log(Level.DEBUG, LoggingPrefix + "Set parameters: " + Baudrate + "/" + Databits + "/" + Stopbits + "/" + Parity);
    }

    private int validate(int value, Integer[][] validvalues, String valuename) throws JposException {
        for (Integer[] valid : validvalues) {
            if (value == valid[0]) {
                if (valid[1] != null)
                    return valid[1];
                break;
            }
        }
        logerror("SetParameters", JposConst.JPOS_E_ILLEGAL, "Unsupported " + valuename + ": " + value);
        return 0;   // will never be reached.
    }

    private void setParametersJSerialCom() throws IllegalAccessException, InvocationTargetException, IOException {
        if (!(Boolean) SetComPortParameters.invoke(SerialIOExecutor, Baudrate, Databits, Stopbits, Parity))
            throw new IOException("Error setting parameters for " + Port);
    }

    @Override
    public String setTarget(String target) throws JposException {
        if (!target.equals(Port))
            logerror("SetTarget", JposConst.JPOS_E_ILLEGAL, "Target must match " + Port);
        return Target;
    }

    /**
     * Checks whether the serial port (still) exists. This is especially useful in case of virtual COM ports generated
     * by USB devices.
     * @return true if the port exists (even if it is accessible), false if it does not exist.
     */
    public boolean exists() {
        if (JsscSerialPort)
            return existsJssc();
        else if (JSerialComSerialPort)
            return existsJSerialCom();
        else if (JnaSerialSerialPort)
            return ((JnaSerial) SerialIOExecutor).exits(Target);
        return false;
    }

    private boolean existsJSerialCom() {
        try {
            Object checker = GetCommPort.invoke(null, Target);
        } catch (Exception e) {
            if (e instanceof InvocationTargetException &&
                    ((InvocationTargetException)e).getTargetException().getClass().getSimpleName().equals("SerialPortInvalidPortException"))
                return false;
        }
        return true;
    }

    private boolean existsJssc() {
        try {
            Object checker = NewSerialPort.newInstance(Target);
            OpenPort.invoke(checker);
            SetParams.invoke(checker, Baudrate, Databits, Stopbits, Parity);
            ClosePort.invoke(checker);
        } catch (Exception e) {
            if (e instanceof InvocationTargetException)
                e = (Exception) ((InvocationTargetException)e).getTargetException();
            try {
                if (e.getClass().getSimpleName().equals("SerialPortException") &&
                        GetExceptionType.invoke(e).equals(TYPE_PORT_NOT_FOUND))
                    return false;
            } catch (Exception ex) {
            }
        }
        return true;
    }
}
