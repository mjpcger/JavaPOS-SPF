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

package de.gmxhome.conrad.jSerialComm;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;
import de.gmxhome.conrad.jpos.jpos_base.SerialIOAdapter;

import java.io.IOException;
import java.util.Arrays;

import static com.fazecast.jSerialComm.SerialPort.*;

/**
 * Implementation of SerialIOAdapter using jSerialComm framework.
 */
public class JSCSerial implements SerialIOAdapter {
    private SerialPort SerialIOExecutor;
    private static final int TimeoutMode = TIMEOUT_WRITE_BLOCKING|TIMEOUT_READ_BLOCKING;

    /**
     * Constructor. Creates communication adapter.
     * @throws IOException If the operating system is not supported by JnaSerial.
     */
    public JSCSerial() {
        SerialIOExecutor = null;
    }

    @Override
    public void open(String port) throws IOException {
        checkOpened(false);
        SerialPort portObj;
        try {
            portObj = getCommPort(port);
            if (!portObj.openPort())
                throw new IOException("Open error");
            SerialIOExecutor = portObj;
        } catch (SerialPortInvalidPortException e) {
            throw new NotFoundException(e.getMessage(), e);
        } catch (Throwable e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public void close() throws IOException {
        checkOpened(true);
        if (!SerialIOExecutor.closePort())
            throw new IOException("Closing port error");
    }

    @Override
    public void setParameters(int baudrate, int databits, int stopbits, int parity) throws IOException {
        checkOpened(true);
        if (!SerialIOExecutor.setComPortParameters(baudrate, databits, stopbits, parity))
            throw new IOException("Parameters setting error");
    }

    @Override
    public int available() throws IOException {
        checkOpened(true);
        return SerialIOExecutor.bytesAvailable();
    }

    @Override
    public byte[] read(int count, int timeout) throws IOException {
        checkOpened(true);
        byte[] result = new byte[count];
        int len = SerialIOExecutor.bytesAvailable();
        if (len < 0)
            throw new IOException("Port not open error");
        else if (len < count) {
            if (!SerialIOExecutor.setComPortTimeouts(TimeoutMode, timeout == 0 ? 1 : timeout, 0))
                throw new IOException("Timeout cannot be set");
        }
        if ((len = SerialIOExecutor.readBytes(result, count)) < 0)
            throw new IOException("Read error");
        result = Arrays.copyOf(result, len);
        return result;
    }

    @Override
    public void write(byte[] buffer) throws IOException {
        checkOpened(true);
        int sent = 0;
        while (sent < buffer.length) {
            int partlen = SerialIOExecutor.writeBytes(buffer, buffer.length - sent, sent);
            if (partlen <= 0)
                throw new IOException("Write error");
            sent += partlen;
        }
    }

    @Override
    public void flush() throws IOException {
        // Not supported by jSerialComm, but we can empty the input buffer
        checkOpened(true);
        int count;
        if (!(Boolean) SerialIOExecutor.setComPortTimeouts(TimeoutMode, 1, 0))
            throw new IOException("Port not open error");
        while ((count = SerialIOExecutor.bytesAvailable()) > 0) {
            byte[] buffer = new byte[count + 100];
            int read = SerialIOExecutor.readBytes(buffer, buffer.length);
            if (read < 0)
                break;
        }
        if (count < 0)
            throw new IOException("Flush error");
    }

    @Override
    public boolean exits(String port) {
        try {
            return getCommPort(port) != null;
        } catch (SerialPortInvalidPortException e) {
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int[][]getCommunicationConstants(int constantType) {
        int[][][] validvalues = {
                {   // T_BAUD
                        {B_1200, 1200},
                        {B_4800, 4800},
                        {B_9600, 9600},
                        {B_19200, 19200},
                        {B_38400, 38400},
                        {B_57600, 57600},
                        {B_115200, 115200},
                        {B_128000, 128000},
                        {B_256000, 256000}
                },
                {   // T_DATA
                        {D_7, 7},
                        {D_8, 8}
                },
                {   // T_STOP
                        {S_1, ONE_STOP_BIT},
                        {S_2, TWO_STOP_BITS}
                },
                {   // T_PARITY
                        {P_NO, NO_PARITY},
                        {P_ODD, ODD_PARITY},
                        {P_EVEN, EVEN_PARITY},
                        {P_MARK, MARK_PARITY},
                        {P_SPACE, SPACE_PARITY}
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
