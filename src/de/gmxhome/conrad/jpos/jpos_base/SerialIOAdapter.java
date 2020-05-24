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
 */

package de.gmxhome.conrad.jpos.jpos_base;

import java.io.IOException;

/**
 * Wrapper interface that provides the interface used by SerialIOProcessor, to be implemented for different
 * implementations for serial communication, such as jSSC, jSerialComm or others.<br>
 * Each SerialIOAdapter is a very simple class that holds the absolute minimum of functionality to support serial IO.
 * It contains at least a default constructor.
 */
public interface SerialIOAdapter {
    /**
     * Baudrate constant for 1200 baud.
     */
    public final static int B_1200 = 1200;
    /**
     * Baudrate constant for 4800 baud.
     */
    public final static int B_4800 = 4800;
    /**
     * Baudrate constant for 9600 baud.
     */
    public final static int B_9600 = 9600;
    /**
     * Baudrate constant for 19200 baud.
     */
    public final static int B_19200 = 19200;
    /**
     * Baudrate constant for 38400 baud.
     */
    public final static int B_38400 = 38400;
    /**
     * Baudrate constant for 57600 baud.
     */
    public final static int B_57600 = 57600;
    /**
     * Baudrate constant for 115200 baud.
     */
    public final static int B_115200 = 115200;
    /**
     * Baudrate constant for 128000 baud.
     */
    public final static int B_128000 = 128000;
    /**
     * Baudrate constant for 256000 baud.
     */
    public final static int B_256000 = 256000;

    /**
     * Constant for 7 bit data size.
     */
    public final static int D_7 = 7;
    /**
     * Constant for 8 bit data size.
     */
    public final static int D_8 = 8;

    /**
     * Constant for serial communication using 1 stop bit.
     */
    public final static int S_1 = 1;
    /**
     * Constant for serial communication using 2 stop bits.
     */
    public final static int S_2 = 2;

    /**
     * Constant for no parity.
     */
    public static final int P_NO = 0;
    /**
     * Constant for odd parity.
     */
    public static final int P_ODD = 1;
    /**
     * Constant for even parity.
     */
    public static final int P_EVEN = 2;
    /**
     * Constant for mark parity.
     */
    public static final int P_MARK = 3;
    /**
     * Constant for space parity.
     */
    public static final int P_SPACE = 4;
    /**
     * Constant for requesting valid baud rate constants.
     */
    public static final int T_BAUD = 0;
    /**
     * Constant for requesting valid baud rate constants.
     */
    public static final int T_DATA = 1;
    /**
     * Constant for requesting valid baud rate constants.
     */
    public static final int T_STOP = 2;
    /**
     * Constant for requesting valid baud rate constants.
     */
    public static final int T_PARITY = 3;

    /**
     * Current baud rate. Set to null as long as it has not been set to a valid baud rate.
     */
    public Integer Baudrate = null;

    /**
     * Opens a port for serial IO.
     * @param port  Port. e.g. COM2
     * @throws IOException If the port does not exist or in case of IO errors.
     */
    public void open(String port) throws IOException;

    /**
     * Close the communication port.
     * @throws IOException In case of IO error.
     */
    public void close() throws IOException;

    /**
     * Set communication parameters for speed, data size and frame structure.<br>baudrate specifies the baud rate,
     * databits the number of data bits, stopbits the number of stop bits per data unit and parity the kind of parity bit,
     * if parity shall be used. Keep in mind that these constants can vary between adapter implementations. The application
     * should use method getCommunicationConstants to retrieve the valid constant values and the corresponding adapter
     * independent values.
     * @param baudrate Baud rate.
     * @param databits Bits per date unit.
     * @param stopbits Number of stop bits to be used.
     * @param parity   Parity constant.
     * @throws IOException If an IO error occurs of if a parameter is invalid.
     */
    public void setParameters(int baudrate, int databits, int stopbits, int parity) throws IOException;

    /**
     * Returns the number of received bytes that can be read without blocking.
     * @return Number of bytes just in device input buffer.
     * @throws IOException In case of an IO error
     */
    public int available() throws IOException;

    /**
     * Reads data from a communication port. If no data are available, read blocks until data are available or the
     * specified timeout has been reached.
     * @param count Maximum number of bytes to be read.
     * @param timeout Maximum time to wait for data in milliseconds.
     * @return byte array holding all available data bytes
     * @throws IOException In case of an IO error.
     */
    public byte[] read(int count, int timeout) throws IOException;

    /**
     * Write the given byte buffer to the com port.
     * @param buffer    Buffer to be written
     * @throws IOException    If the port has not been opened or in case of an IO error.
     */
    public void write(byte[] buffer) throws IOException;

    /**
     * Flushed input and output buffers.
     * @throws IOException In case of an IO error.
     */
    public void flush() throws IOException;

    /**
     * Checks whether a port still exists. In case of serial communication via Bluetooth or USB, ports can go lost.
     * @param port  Port to be checked.
     * @return true if the port still exists, false otherwise-
     */
    public boolean exits(String port);

    /**
     * Retrieves an array of integer pairs specifying all valid parameters for the given constant type. constantType
     * can be one of T_BAUD, T_DATA, T_STOP or T_PARITY. The method returns an array of integer pairs where the first
     * element specifies the constant value to be passed to the setParameters method of the SerialIOProcessor and the
     * second element specifies the corresponding value to be passed to the setParameters method of the specific
     * SerialIOAdapter implementation.
     * @param constantType Type of constants to be requested.
     * @return Array of integer pairs containing the adapter independent and adapter specific constants of the requested
     *         type. null if constantType is invalid.
     */
    public int[][]getCommunicationConstants(int constantType);

    /**
     * Special exception to be used when the port is not available.
     */
    public static class NotFoundException extends IOException {
        /**
         * Constructor.
         * @param message   Message of exception.
         * @param exception Cause of exception.
         */
        public NotFoundException(String message, Throwable exception) {
            super(message, exception);
        }
    }
}
