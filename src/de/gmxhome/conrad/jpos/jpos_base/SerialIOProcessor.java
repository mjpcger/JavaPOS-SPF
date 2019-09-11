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
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;
import org.apache.log4j.Level;

import java.util.Arrays;

/**
 * Class to process serial communication. Current implementation uses jSSC implementation
 * for serial communication. Includes functionality for automatic data logging.
 */
public class SerialIOProcessor extends UniqueIOProcessor {
    // Baudrate constants
    public static final int BAUDRATE_1200 = SerialPort.BAUDRATE_1200;
    public static final int BAUDRATE_4800 = SerialPort.BAUDRATE_4800;
    public static final int BAUDRATE_9600 = SerialPort.BAUDRATE_9600;
    public static final int BAUDRATE_19200 = SerialPort.BAUDRATE_19200;
    public static final int BAUDRATE_38400 = SerialPort.BAUDRATE_38400;
    public static final int BAUDRATE_57600 = SerialPort.BAUDRATE_57600;
    public static final int BAUDRATE_115200 = SerialPort.BAUDRATE_115200;
    public static final int BAUDRATE_128000 = SerialPort.BAUDRATE_128000;
    public static final int BAUDRATE_256000 = SerialPort.BAUDRATE_256000;

    /**
     * Baudrate to be used. Default: BAUDRATE_9600.
     */
    protected int Baudrate = BAUDRATE_9600;

    // Parity constants
    public static final int PARITY_NONE = SerialPort.PARITY_NONE;
    public static final int PARITY_ODD = SerialPort.PARITY_ODD;
    public static final int PARITY_EVEN = SerialPort.PARITY_EVEN;
    public static final int PARITY_MARK = SerialPort.PARITY_MARK;
    public static final int PARITY_SPACE = SerialPort.PARITY_SPACE;

    /**
     * Parity to be used. Default: PARITY_NONE.
     */
    protected int Parity = PARITY_NONE;

    // Bit size constants
    public static final int DATABITS_7 = SerialPort.DATABITS_7;
    public static final int DATABITS_8 = SerialPort.DATABITS_8;

    /**
     * Data bits to be used. Default: DATABITS_8.
     */
    protected int Databits = DATABITS_8;

    //Stop bits constants
    public static final int STOPBITS_1 = SerialPort.STOPBITS_1;
    public static final int STOPBITS_2 = SerialPort.STOPBITS_2;

    /**
     * Stop bits to be used. Default: STOPBITS_2.
     */
    protected int Stopbits = STOPBITS_2;

    /**
     * jSSC implementation: jSSC simple serial connector used for serial communication.
     */
    SerialPort SerialIOExecutor;

    // For parameter setting:
    private boolean ParametersSet = false;

    private void log(Level level, String message) {
        Dev.log(level, message);
    }
    /**
     * Creates simple serial connector for given port
     * @param usingDevice Device that uses the proceessor. Processor uses logging of device
     *                    to produce logging entries
     * @param portName Name of port. Example: COM1
     * @throws JposException Will not be thrown in case of serial communication.
     */
    public SerialIOProcessor(JposDevice usingDevice, String portName) throws JposException {
        super(usingDevice, portName);
        Source = Target = portName;
    }

    @Override
    /**
     * write byte buffer to serial port
     * @param buffer Buffer to be written
     * @return length written
     * @throws JposException In case of an IO error
     */
    public int write(byte[] buffer) throws JposException {
        synchronized(WriteSynchronizer) {
            if (SerialIOExecutor == null)
                logerror("Write", JposConst.JPOS_E_ILLEGAL, "Port not open for port " + Port);
            try {
                if (!SerialIOExecutor.writeBytes(buffer))
                    logerror("Write", JposConst.JPOS_E_FAILURE, "Error on port " + Port);
                return super.write(buffer);
            } catch (SerialPortException e) {
                return logerror("Write", JposConst.JPOS_E_FAILURE, e);
            }
        }
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
            logerror("Available", JposConst.JPOS_E_ILLEGAL, "Port not open for port " + Port);
       try {
            count = SerialIOExecutor.getInputBufferBytesCount();
        } catch (SerialPortException e) {
            logerror("Available", JposConst.JPOS_E_FAILURE, e);
        }
        if (count >= 0) {
            LoggingData = String.valueOf(count).getBytes();
            return super.available();
        }
        return logerror("Available", JposConst.JPOS_E_FAILURE, "Bad connector on port " + Port);
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
        synchronized(ReadSynchronizer) {
            boolean retry = false;
            int timeout = Timeout;
            if (SerialIOExecutor == null)
                logerror("Read", JposConst.JPOS_E_ILLEGAL, "Port not open for port " + Port);
            byte[] result;
            if (count == 0)
                return new byte[0];
            while (true) {
                try {
                    int len = SerialIOExecutor.getInputBufferBytesCount();
                    if (len > 0)
                        result = SerialIOExecutor.readBytes(len <= count ? len : count, 1);
                    else {
                        result = SerialIOExecutor.readBytes(1, timeout);
                        if (result.length == 1 && count > 1 && (len = SerialIOExecutor.getInputBufferBytesCount()) > 0) {
                            byte[] part = SerialIOExecutor.readBytes(len < count ? len : count - 1);
                            result = Arrays.copyOf(result, len + result.length);
                            System.arraycopy(part, 0, result, 1, part.length);
                        }
                    }
                    LoggingData = result;
                    return super.read(count);
                } catch (SerialPortException e) {
                    logerror("Read", JposConst.JPOS_E_FAILURE, e);
                } catch (SerialPortTimeoutException e) {
                    if (!retry && (count = available()) > 0) {
                        timeout = 0;
                        retry = true;
                        continue;
                    }
                }
                break;
            }
            log(Level.TRACE, LoggingPrefix + "Input timeout");
            return new byte[0];
        }
    }

    @Override
    /**
     * Flushes input and output buffer
     * @throws JposException In case of an IO error
     */
    public void flush() throws JposException {
        if (SerialIOExecutor == null)
            logerror("Flush", JposConst.JPOS_E_ILLEGAL, "Port not open error for port " + Port);
        try {
            SerialIOExecutor.purgePort(SerialPort.PURGE_RXABORT|SerialPort.PURGE_TXABORT);
            super.flush();
        } catch (SerialPortException e) {
            logerror("Flush", JposConst.JPOS_E_FAILURE, e);
        }
    }

    /**
     * Opens the port. No error logging occurs in error case to avoid a flood of error messages in error cases.
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
        try {
            SerialIOExecutor = new SerialPort(Port);
            SerialIOExecutor.openPort();
            if (ParametersSet)
                SerialIOExecutor.setParams(Baudrate, Databits, Stopbits, Parity);
            super.open(noErrorLog);
        } catch (SerialPortException e) {
            if (!noErrorLog)
                log(Level.ERROR, LoggingPrefix + "Open error: " + e.getMessage());
            if (e.getExceptionType().equals(SerialPortException.TYPE_PORT_NOT_FOUND))
                throw new JposException(JposConst.JPOS_E_NOEXIST, IOProcessorError, e.getMessage(), e);
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
            SerialIOExecutor.closePort();
            SerialIOExecutor = null;
            super.close();
        } catch (SerialPortException e) {
            SerialIOExecutor = null;
            logerror("Close", JposConst.JPOS_E_FAILURE, e);
        }
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
        Baudrate = baudrate;
        Databits = databits;
        Stopbits = stopbits;
        Parity = parity;
        ParametersSet = true;
        if (SerialIOExecutor != null) {
            try {
                SerialIOExecutor.setParams(Baudrate, Databits, Stopbits, Parity);
            } catch (SerialPortException e) {
                logerror("SetParameters", JposConst.JPOS_E_ILLEGAL, e);
            }
        }
        log(Level.DEBUG, LoggingPrefix + "Set parameters: " + Baudrate + "/" + Databits + "/" + Stopbits + "/" + Parity);
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
        SerialPort checker = new SerialPort(Target);
        try {
            checker.openPort();
            checker.setParams(Baudrate, Databits, Stopbits, Parity);
            checker.closePort();
        } catch (SerialPortException e) {
            if (e.getExceptionType().equals(SerialPortException.TYPE_PORT_NOT_FOUND))
                return false;
        }
        return true;
    }
}
