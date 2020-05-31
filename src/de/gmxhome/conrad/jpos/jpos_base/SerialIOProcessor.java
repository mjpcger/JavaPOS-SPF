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

import static de.gmxhome.conrad.jpos.jpos_base.JposBaseDevice.SerialIOAdapterClass;

/**
 * Class to process serial communication. Current implementation uses jSSC implementation
 * for serial communication. Includes functionality for automatic data logging.
 */
public class SerialIOProcessor extends UniqueIOProcessor {
    static Constructor NewSerialPort;

    static {
        if (SerialIOAdapterClass == null) {
            SerialIOAdapterClass = System.getProperty("java.version").split("\\.")[0].equals("1") ?
                    "de.gmxhome.conrad.jSSC.JSSCSerial" :
                    "de.gmxhome.conrad.jSerialComm.JSCSerial";
        }
        try {
            NewSerialPort = Class.forName(SerialIOAdapterClass).getConstructor();
        } catch (ClassNotFoundException e) {
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Baudrate constant for 1200 baud.
     */
    public final static int BAUDRATE_1200 = SerialIOAdapter.B_1200;
    /**
     * Baudrate constant for 4800 baud.
     */
    public final static int BAUDRATE_4800 = SerialIOAdapter.B_4800;
    /**
     * Baudrate constant for 9600 baud.
     */
    public final static int BAUDRATE_9600 = SerialIOAdapter.B_9600;
    /**
     * Baudrate constant for 19200 baud.
     */
    public final static int BAUDRATE_19200 = SerialIOAdapter.B_19200;
    /**
     * Baudrate constant for 38400 baud.
     */
    public final static int BAUDRATE_38400 = SerialIOAdapter.B_38400;
    /**
     * Baudrate constant for 57600 baud.
     */
    public final static int BAUDRATE_57600 = SerialIOAdapter.B_57600;
    /**
     * Baudrate constant for 115200 baud.
     */
    public final static int BAUDRATE_115200 = SerialIOAdapter.B_115200;
    /**
     * Baudrate constant for 128000 baud.
     */
    public final static int BAUDRATE_128000 = SerialIOAdapter.B_128000;
    /**
     * Baudrate constant for 256000 baud.
     */
    public final static int BAUDRATE_256000 = SerialIOAdapter.B_256000;

    /**
     * Baudrate to be used. Default: BAUDRATE_9600.
     */
    protected int Baudrate = BAUDRATE_9600;

    /**
     * Parity constant for no parity.
     */
    public final static int PARITY_NONE = SerialIOAdapter.P_NO;
    /**
     * Parity constant for odd parity.
     */
    public final static int PARITY_ODD = SerialIOAdapter.P_ODD;
    /**
     * Parity constant for even parity.
     */
    public final static int PARITY_EVEN = SerialIOAdapter.P_EVEN;
    /**
     * Parity constant for mark parity.
     */
    public final static int PARITY_MARK = SerialIOAdapter.P_MARK;
    /**
     * Parity constant for space parity.
     */
    public final static int PARITY_SPACE = SerialIOAdapter.P_SPACE;

    /**
     * Parity to be used. Default: PARITY_NONE.
     */
    protected int Parity = PARITY_NONE;

    /**
     * Constant for 7 bit data size.
     */
    public final static int DATABITS_7 = SerialIOAdapter.D_7;
    /**
     * Constant for 8 bit data size.
     */
    public final static int DATABITS_8 = SerialIOAdapter.D_8;

    /**
     * Data bits to be used. Default: DATABITS_8.
     */
    protected int Databits = DATABITS_8;

    /**
     * Constant for serial communication using 1 stop bit.
     */
    public final static int STOPBITS_1 = SerialIOAdapter.S_1;
    /**
     * Constant for serial communication using 2 stop bits.
     */
    public final static int STOPBITS_2 = SerialIOAdapter.S_2;

    /**
     * Stop bits to be used. Default: STOPBITS_2.
     */
    protected int Stopbits = STOPBITS_2;

    /**
     * jSSC implementation: jSSC simple serial connector used for serial communication.
     */
    SerialIOAdapter SerialIOExecutor;

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
                SerialIOExecutor.write(buffer);
                return super.write(buffer);
            } catch (IOException e) {
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
            logerror("Available", JposConst.JPOS_E_ILLEGAL, "Port not open");
        try {
            count = SerialIOExecutor.available();
        } catch (IOException e) {
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
                    result = SerialIOExecutor.read(count, timeout);
                    LoggingData = result;
                    return super.read(count);
                } catch (IOException e) {
                    logerror("Read", JposConst.JPOS_E_FAILURE, e);
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
            logerror("Flush", JposConst.JPOS_E_ILLEGAL, "Port not open error");
        try {
            SerialIOExecutor.flush();
            super.flush();
        } catch (IOException e) {
            logerror("Flush", JposConst.JPOS_E_FAILURE, e);
        }
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
        Object error = null;
        do {
            if (SerialIOExecutor != null) {
                error = "Port just open";
                break;
            }
            if (NewSerialPort == null) {
                error = "No SerialIOAdapter available";
                break;
            }
            try {
                SerialIOExecutor = (SerialIOAdapter) NewSerialPort.newInstance();
                SerialIOExecutor.open(Port);
                if (ParametersSet)
                    SerialIOExecutor.setParameters(Baudrate, Databits, Stopbits, Parity);
            } catch (Exception e) {
                error = e;
                break;
            }
            super.open(noErrorLog);
            return;
        }
        while (false);
        if (noErrorLog) {
            if (error instanceof SerialIOAdapter.NotFoundException)
                throw new JposException(JposConst.JPOS_E_NOEXIST, ((Exception)error).getMessage());
            else if (error instanceof Exception)
                throw new JposException(JposConst.JPOS_E_FAILURE, ((Exception)error).getMessage());
            else
                throw new JposException(JposConst.JPOS_E_ILLEGAL, error.toString());
        }
        if (error instanceof SerialIOAdapter.NotFoundException)
            logerror("Open", JposConst.JPOS_E_NOEXIST, (Exception)error);
        else if (error instanceof Exception)
            logerror("Open", JposConst.JPOS_E_FAILURE, (Exception)error);
        else
            logerror("Open", JposConst.JPOS_E_ILLEGAL, error.toString());
    }

    @Override
    /**
     * Close the port
     * @throws JposException If an IO error occurs
     */
    public void close() throws JposException {
        if (SerialIOExecutor == null)
            logerror("Close", JposConst.JPOS_E_ILLEGAL, "Not opened");
        try {
                SerialIOExecutor.close();
            super.close();
        } catch (Exception e) {
            logerror("Close", JposConst.JPOS_E_FAILURE, e);
        } finally {
            SerialIOExecutor = null;
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
        SerialIOAdapter dummyexec = null;
        try {
            dummyexec = (SerialIOAdapter)NewSerialPort.newInstance();
        } catch (Exception e) {
            logerror("SetParameters", JposConst.JPOS_E_FAILURE, e);
        }
        int[][] validrates = dummyexec.getCommunicationConstants(SerialIOAdapter.T_BAUD);
        int[][] validdatabits = dummyexec.getCommunicationConstants(SerialIOAdapter.T_DATA);
        int[][] validstopbits = dummyexec.getCommunicationConstants(SerialIOAdapter.T_STOP);
        int[][] validparities = dummyexec.getCommunicationConstants(SerialIOAdapter.T_PARITY);
        Baudrate = validate(baudrate, validrates, "baud rate");
        Databits = validate(databits, validdatabits, "data size");
        Stopbits = validate(stopbits, validstopbits, "number of stop bits");
        Parity = validate(parity, validparities, "parity type");
        ParametersSet = true;
        if (SerialIOExecutor != null) {
            try {
                    SerialIOExecutor.setParameters(Baudrate, Databits, Stopbits, Parity);
            } catch (IOException e) {
                logerror("SetParameters", JposConst.JPOS_E_ILLEGAL, e);
            }
        }
        log(Level.DEBUG, LoggingPrefix + "SetParameters(" + Baudrate + ", " + Databits + ", " + Stopbits + ", " + Parity + ") successful.");
    }

    private int validate(int value, int[][] validvalues, String valuename) throws JposException {
        if (validvalues != null) {
            for (int[] valid : validvalues) {
                if (valid.length == 2 && value == valid[0]) {
                    return valid[1];
                }
            }
        }
        logerror("SetParameters", JposConst.JPOS_E_ILLEGAL, "Unsupported " + valuename + ": " + value);
        return 0;   // will never be reached.
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
        return SerialIOExecutor != null && SerialIOExecutor.exits(Target);
    }
}
