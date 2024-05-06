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

package de.gmxhome.conrad.jSSC;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jssc.*;

import java.io.*;
import java.util.*;

import static jssc.SerialPort.*;

/**
 * Implementation of SerialIOAdapter using jSSC framework.
 */
public class JSSCSerial implements SerialIOAdapter {
    private SerialPort SerialIOExecutor;
    private final String Port;
    private final static int PurgeMode = PURGE_RXABORT|PURGE_TXABORT;

    /**
     * Constructor. Creates communication adapter.
     * @throws IOException If the operating system is not supported by JnaSerial.
     */
    public JSSCSerial() throws IOException {
        Port = null;
        SerialIOExecutor = null;
        try {
            Class.forName("jssc.SerialPort");
        } catch (Exception e) {
            throw new IOException("Framework jSSC not available", e);
        }
    }

    @Override
    public void open(String port) throws IOException {
        checkOpened(false);
        SerialPort portObj;
        try {
            portObj = new SerialPort(Port);
            portObj.openPort();
            SerialIOExecutor = portObj;
        } catch (SerialPortException e) {
            if (e.getExceptionType().equals(SerialPortException.TYPE_PORT_NOT_FOUND))
                throw new NotFoundException(e.getMessage(), e);
            else
                throw new IOException(e.getMessage(), e);
        } catch (Throwable e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public void close() throws IOException {
        checkOpened(true);
        try {
            SerialIOExecutor.closePort();
        } catch (SerialPortException e) {
            throw new IOException(e.getMessage(), e);
        } finally {
            SerialIOExecutor = null;
        }
    }

    @Override
    public void setParameters(int baudrate, int databits, int stopbits, int parity) throws IOException {
        checkOpened(true);
        try {
            SerialIOExecutor.setParams(baudrate, databits, stopbits, parity);
        } catch (SerialPortException e) {
            throw new IOException(e.getMessage(), e);
        }

    }

    @Override
    public int available() throws IOException {
        checkOpened(true);
        try {
            return SerialIOExecutor.getInputBufferBytesCount();
        } catch (SerialPortException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public byte[] read(int count, int timeout) throws IOException {
        checkOpened(true);
        byte[] result;
        int len = 0;
        try {
            len = SerialIOExecutor.getInputBufferBytesCount();
            if (len > 0)
                result = SerialIOExecutor.readBytes(Math.min(len, count));
            else {
                result = SerialIOExecutor.readBytes(1, timeout);
                if (result.length == 1 && count > 1 && (len = SerialIOExecutor.getInputBufferBytesCount()) > 0) {
                    byte[] part = SerialIOExecutor.readBytes(len < count ? len : count - 1);
                    result = Arrays.copyOf(result, len + 1);
                    System.arraycopy(part, 0, result, 1, part.length);
                }
            }
            return result;
        } catch (SerialPortTimeoutException e) {
            return new byte[0];
        } catch (SerialPortException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public void write(byte[] buffer) throws IOException {
        checkOpened(true);
        try {
            if (!SerialIOExecutor.writeBytes(buffer))
                throw new IOException("Write error");
        } catch (SerialPortException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public void flush() throws IOException {
        checkOpened(true);
        try {
            SerialIOExecutor.purgePort(PurgeMode);
        } catch (SerialPortException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public boolean exits(String port) {
        try {
            SerialPort checker = new SerialPort(port);
            checker.openPort();
            try {
                checker.closePort();
            } catch(Throwable e) {
                e.printStackTrace();
            }
            return true;
        } catch (SerialPortException e) {
            if (!e.getExceptionType().equals(SerialPortException.TYPE_PORT_NOT_FOUND))
                return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int[][] getCommunicationConstants(int constantType) {
        int[][][] validvalues = {
                {   // T_BAUD
                        {B_1200, BAUDRATE_1200},
                        {B_4800, BAUDRATE_4800},
                        {B_9600, BAUDRATE_9600},
                        {B_19200, BAUDRATE_19200},
                        {B_38400, BAUDRATE_38400},
                        {B_57600, BAUDRATE_57600},
                        {B_115200, BAUDRATE_115200},
                        {B_128000, BAUDRATE_128000},
                        {B_256000, BAUDRATE_256000}
                },
                {   // T_DATA
                        {D_7, DATABITS_7},
                        {D_8, DATABITS_8}
                },
                {   // T_STOP
                        {S_1, STOPBITS_1},
                        {S_2, STOPBITS_2}
                },
                {   // T_PARITY
                        {P_NO, PARITY_NONE},
                        {P_ODD, PARITY_ODD},
                        {P_EVEN, PARITY_EVEN},
                        {P_MARK, PARITY_MARK},
                        {P_SPACE, PARITY_SPACE}
                }
        };
        if (constantType >= 0 && constantType < validvalues.length)
            return validvalues[constantType];
        return null;
    }

    private void checkOpened(boolean opened) throws IOException {
        if ((SerialIOExecutor == null) == opened)
            throw new IOException("Port " + (opened ? "not opened" : "just opened"));
    }
}
