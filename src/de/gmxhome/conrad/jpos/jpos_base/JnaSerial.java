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

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.*;
import com.sun.jna.win32.W32APIOptions;
import jpos.JposConst;
import jpos.JposException;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Class for serial communication via native OS calls using JNA. Currently only implemented for Windows
 */
public class JnaSerial {
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

    private static interface Kernel32Ext extends Kernel32 {
        Kernel32Ext INSTANCE = (Kernel32Ext) Native.load("kernel32", Kernel32Ext.class, W32APIOptions.DEFAULT_OPTIONS);

        boolean PurgeComm(HANDLE var1, int var2);

        static final int PURGE_TXABORT = 1;
        static final int PURGE_RXABORT = 2;
        static final int PURGE_TXCLEAR = 4;
        static final int PURGE_RXCLEAR = 8;

        boolean ClearCommError(HANDLE var1, IntByReference var2, WinBaseExt.COMSTAT var3);
        boolean WriteFile(HANDLE hd, ByteBuffer object, int length, IntByReference sent, OVERLAPPED ov);
        boolean ReadFile(HANDLE hd, ByteBuffer object, int length, IntByReference receiced, OVERLAPPED ov);
        boolean GetOverlappedResult(HANDLE hd, OVERLAPPED ov, IntByReference sentBytes, boolean b);
    }

    private interface WinBaseExt extends WinBase {

        @Structure.FieldOrder({"Flags", "cbInQue", "cbOutQue"})
        public static class COMSTAT extends Structure {
            public int Flags;
            public int cbInQue;
            public int cbOutQue;

            public COMSTAT() {}
        }
    }

    private Kernel32Ext Kernel32Lib = Kernel32Ext.INSTANCE;

    private String Port = null;
    private Integer Baudrate = null, Databits = null, Stopbits = null, Parity = null;
    private WinNT.HANDLE DeviceHandle;

    /**
     * Constructor. Creates communication adapter.
     * @throws JposException If the operating system is not supported by JnaSerial.
     */
    public JnaSerial() throws JposException {
        String os;
        if (!(os = System.getProperty("os.name")).startsWith("Windows")) {
            throw new JposException(JposConst.JPOS_E_NOEXIST, "No support for OS " + os);
        }
    }

    private WinNT.HANDLE checkOpened(boolean opened) throws JposException {
        if ((DeviceHandle == null) == opened)
            throw new JposException(JposConst.JPOS_E_FAILURE, Port + (opened ? " not opened" : " just opened"));
        return DeviceHandle;
    }

    /**
     * Write the given byte buffer to the com port.
     * @param buffer    Buffer to be written
     * @throws JposException    If the port has not been opened or in case of an IO error.
     */
    synchronized public void write(byte[] buffer) throws JposException {
        WinNT.HANDLE hd = checkOpened(true);
        int actpos, actlen;
        Memory memory = new Memory(actlen = buffer.length);
        memory.write(actpos = 0, buffer, 0, actlen);
        IntByReference sentBytes = new IntByReference(-1);
        do {
            sentBytes.setValue(actlen);
            //*
            WinBase.OVERLAPPED ov = new WinBase.OVERLAPPED();
            ov.writeField("hEvent", Kernel32Lib.CreateEvent(null, true, false, null));
            if (ov.hEvent == null)
                throw new JposException(JposConst.JPOS_E_FAILURE, "Write event creation error " + Kernel32Lib.GetLastError());
            try {
                if (!Kernel32Lib.WriteFile(hd, memory.getByteBuffer(actpos, actlen), actlen, null, ov))
                    throw new JposException(JposConst.JPOS_E_FAILURE, "WriteFile returned error code " + Kernel32Lib.GetLastError());
                if (Kernel32Lib.WaitForSingleObject(ov.hEvent, WinBase.INFINITE) != WinBase.WAIT_OBJECT_0)
                    throw new JposException(JposConst.JPOS_E_FAILURE, "WaitForSingleObject returned error code " + Kernel32Lib.GetLastError());
                if (!Kernel32Lib.GetOverlappedResult(hd, ov, sentBytes, true))
                    throw new JposException(JposConst.JPOS_E_FAILURE, "GetOverlappedResult returned error code " + Kernel32Lib.GetLastError());
            } finally {
                Kernel32Lib.CloseHandle(ov.hEvent);
            }
            /*/
            if (!Kernel32Lib.WriteFile(hd, memory.getByteBuffer(actpos, actlen), actlen, sentBytes, null))
                throw new JposException(JposConst.JPOS_E_FAILURE, "WriteFile returned error code " + Kernel32Lib.GetLastError());
            //*/
            if (sentBytes.getValue() > 0) {
                actpos += sentBytes.getValue();
                if ((actlen = buffer.length - actpos) == 0)
                    return;
            } else
                throw new JposException(JposConst.JPOS_E_FAILURE, "Write channel blocked, " + buffer.length + " bytes remaining");
        } while (true);
    }

    /**
     * Returns the number of received bytes that can be read without blocking.
     * @return Number of bytes just in device input buffer.
     * @throws JposException In case of an IO error
     */
    synchronized public int available() throws JposException {
        WinNT.HANDLE hd = checkOpened(true);
        IntByReference errors = new IntByReference(0);
        WinBaseExt.COMSTAT stat = new WinBaseExt.COMSTAT();
        if (!Kernel32Lib.ClearCommError(hd, errors, stat))
            throw new JposException(JposConst.JPOS_E_FAILURE, "ClearCommError returned with error code " + Kernel32Lib.GetLastError());
        return stat.cbInQue;
    }

    /**
     * Reads data from a communication port. If no data are available, read blocks until data are available or the
     * specified timeout has been reached.
     * @param count Maximum number of bytes to be read.
     * @param timeout Maximum time to wait for data in milliseconds.
     * @return byte array holding all available data bytes
     * @throws JposException In case of an IO error.
     */
    public byte[] read(int count, int timeout) throws JposException {
        WinNT.HANDLE hd = checkOpened(true);
        IntByReference receicedBytes = new IntByReference(-1);
        Memory received = new Memory(count);
        //*
        setTimeouts(timeout);
        WinBase.OVERLAPPED ov = new WinBase.OVERLAPPED();
        ov.writeField("hEvent", Kernel32Lib.CreateEvent(null, true, false, null));
        if (ov.hEvent == null)
            throw new JposException(JposConst.JPOS_E_FAILURE, "Read event creation returned error code " + Kernel32Lib.GetLastError());
        try {
            if (!(Kernel32Lib.ReadFile(hd, received.getByteBuffer(0, count), count, null, ov)))
                throw new JposException(JposConst.JPOS_E_FAILURE, "Read returned error code " + Kernel32Lib.GetLastError());
            if (Kernel32Lib.WaitForSingleObject(ov.hEvent, WinBase.INFINITE) != WinBase.WAIT_OBJECT_0)
                throw new JposException(JposConst.JPOS_E_FAILURE, "WaitForSingleObject returned error code " + Kernel32Lib.GetLastError());
            if (!Kernel32Lib.GetOverlappedResult(hd, ov, receicedBytes, true))
                throw new JposException(JposConst.JPOS_E_FAILURE, "GetOverlappedResult returned error code " + Kernel32Lib.GetLastError());
        } finally {
            Kernel32Lib.CloseHandle(ov.hEvent);
        }
        if (receicedBytes.getValue() < 0 )
            throw new JposException(JposConst.JPOS_E_FAILURE, "GetOverlappedResult returned invalid count " + receicedBytes.getValue());
        return received.getByteArray(0, receicedBytes.getValue());
        /*/
        while (true) {
            synchronized (this) {
                if (!(Kernel32Lib.ReadFile(hd, received.getByteBuffer(0, count), count, receicedBytes, null)))
                    throw new JposException(JposConst.JPOS_E_FAILURE, "Read returned error code " + Kernel32Lib.GetLastError());
            }
            if (receicedBytes.getValue() < 0 )
                throw new JposException(JposConst.JPOS_E_FAILURE, "Read returned invalid count " + receicedBytes.getValue());
            if (receicedBytes.getValue() > 0)
                return received.getByteArray(0, receicedBytes.getValue());
            if (timeout == 0)
                break;
            int tio = 10;
            timeout -= timeout < 0 ? 0 : (timeout >= 10 ? tio : (tio = timeout));
            new SyncObject().suspend(tio);
        }
        return new byte[0];
        //*/
    }

    /**
     * Flushed input and output buffers.
     * @throws JposException In case of an IO error.
     */
    synchronized public void flush() throws JposException {
        WinNT.HANDLE hd = checkOpened(true);
        if (!Kernel32Lib.PurgeComm(hd, Kernel32Ext.PURGE_RXABORT|Kernel32Ext.PURGE_TXABORT))
            throw new JposException(JposConst.JPOS_E_FAILURE, "PurgeComm results with error code " + Kernel32Lib.GetLastError());
    }

    /**
     * Opens a port for serial IO.
     * @param port  Port. e.g. COM2
     * @throws JposException If the port does not exist or in case of IO errors.
     */
    synchronized public void open(String port) throws JposException {
        checkOpened(false);
        Port = port;
        WinNT.HANDLE hd = Kernel32Lib.CreateFile("\\\\.\\" + Port,
                //*
                WinNT.GENERIC_READ|WinNT.GENERIC_WRITE, 0, null, WinNT.OPEN_EXISTING, 0, null);
                /*/
                WinNT.GENERIC_READ|WinNT.GENERIC_WRITE, 0, null, WinNT.OPEN_EXISTING, WinNT.FILE_FLAG_OVERLAPPED, null);
                //*/
        if (hd.equals(WinBase.INVALID_HANDLE_VALUE)) {
            int error = Kernel32Lib.GetLastError();
            boolean exists =  error == WinError.ERROR_FILE_NOT_FOUND;
            throw new JposException(exists ? JposConst.JPOS_E_NOEXIST : JposConst.JPOS_E_OFFLINE, "Got error code " + error);
        }
        DeviceHandle = hd;
        if (Baudrate != null && Parity != null && Databits != null && Stopbits != null) {
            try {
                setParameters(Baudrate, Databits, Stopbits, Parity);
            } catch (JposException e) {
                Kernel32Lib.CloseHandle(hd);
                DeviceHandle = null;
            }
        }
    }

    /**
     * Close the communication port.
     * @throws JposException In case of IO error.
     */
    public synchronized void close() throws JposException {
        WinNT.HANDLE hd = checkOpened(true);
        if (!Kernel32Lib.CloseHandle(hd)) {
            DeviceHandle = null;    // What shall we do else?
            throw new JposException(JposConst.JPOS_E_FAILURE, "Closing port failed with error code " + Kernel32Lib.GetLastError());
        }
    }

    /**
     * Set communication parameters. These are:
     * @param baudrate Baudrate: 1200, 4800, 9600, 19200, 38400, 57600, 115200, 128000 or 256000.
     * @param databits Bits per date unit: 7 or 8.
     * @param stopbits Number of stop bits to be used, 1 or 2.
     * @param parity   Parity, represented by 0 (no), 1 (odd), 2 (even), 3 (mark) or 4 (space).
     * @throws JposException If an IO error occurs of if a parameter is invalid.
     */
    synchronized public void setParameters(int baudrate, int databits, int stopbits, int parity) throws JposException {
        WinNT.HANDLE hd = checkOpened(true);
        checkAndSetBaudrate(baudrate);
        checkAndSetDatabits(databits);
        checkAndSetStopbits(stopbits);
        checkAndSetParity(parity);
        WinBase.DCB dcb = new WinBase.DCB();
        if (!Kernel32Lib.GetCommState(hd, dcb))
            throw new JposException(JposConst.JPOS_E_FAILURE, "GetCommState returns error code " + Kernel32Lib.GetLastError());
        dcb.BaudRate.setValue(Baudrate);
        dcb.ByteSize.setValue(Databits);
        dcb.StopBits.setValue(Stopbits);
        dcb.Parity.setValue(Parity);
        dcb.controllBits.setfRtsControl(WinBase.RTS_CONTROL_ENABLE);
        dcb.controllBits.setfDtrControl(WinBase.DTR_CONTROL_ENABLE);
        dcb.controllBits.setfOutxCtsFlow(false);
        dcb.controllBits.setfOutxDsrFlow(false);
        dcb.controllBits.setfDsrSensitivity(false);
        dcb.controllBits.setfTXContinueOnXoff(false);
        dcb.controllBits.setfOutX(false);
        dcb.controllBits.setfInX(false);
        dcb.controllBits.setfErrorChar(false);
        dcb.controllBits.setfNull(false);
        dcb.controllBits.setfAbortOnError(false);
        dcb.XonLim.setValue(2048);
        dcb.XoffLim.setValue(512);
        dcb.XonChar = 17;
        dcb.XoffChar = 19;
        if (!Kernel32Lib.SetCommState(hd, dcb))
            throw new JposException(JposConst.JPOS_E_FAILURE, "SetCommState returned error code " + Kernel32Lib.GetLastError());
        setTimeouts(0);
    }

    private void setTimeouts(int timeout) throws JposException {
        WinNT.HANDLE hd = checkOpened(true);
        WinBase.COMMTIMEOUTS tios = new WinBase.COMMTIMEOUTS();
        final long MAXDWORD = 0xffffffffl;
        tios.WriteTotalTimeoutConstant.setValue(0);
        tios.WriteTotalTimeoutMultiplier.setValue(0);
        if (timeout >= 0) {
            tios.ReadIntervalTimeout.setValue(MAXDWORD);
            tios.ReadTotalTimeoutConstant.setValue(timeout);
            tios.ReadTotalTimeoutMultiplier.setValue(timeout == 0 ? 0 : MAXDWORD);
        }
        else {
            tios.ReadIntervalTimeout.setValue(0);
            tios.ReadTotalTimeoutConstant.setValue(0);
            tios.ReadTotalTimeoutMultiplier.setValue(0);
        }
        if (!Kernel32Lib.SetCommTimeouts(hd, tios))
            throw new JposException(JposConst.JPOS_E_FAILURE, "SetCommTimeouts returned error code " + Kernel32Lib.GetLastError());
    }

    /**
     * Checks whether a port still exists. In case of serial communication via Bluetooth or USB, ports can go lost.
     * @param port  Port to be checked.
     * @return true if the port still exists, false otherwise-
     */
    public boolean exits(String port) {
        WinNT.HANDLE hd = Kernel32Lib.CreateFile("\\\\.\\" + port,
                WinNT.GENERIC_READ|WinNT.GENERIC_WRITE, 0, null, WinNT.OPEN_EXISTING, 0, null);
        if (WinBase.INVALID_HANDLE_VALUE.equals(hd))
            return Kernel32Lib.GetLastError() != WinError.ERROR_FILE_NOT_FOUND;
        Kernel32Lib.CloseHandle(hd);
        return true;
    }

    private void checkAndSetParity(int parity) throws JposException {
        switch (parity) {
            default:
                throw new JposException(JposConst.JPOS_E_ILLEGAL, "Unsupported parity: " + parity);
            case 0:
                Parity = WinBase.NOPARITY;
                break;
            case 1:
                Parity = WinBase.ODDPARITY;
                break;
            case 2:
                Parity = WinBase.EVENPARITY;
                break;
            case 3:
                Parity = WinBase.MARKPARITY;
                break;
            case 4:
                Parity = WinBase.SPACEPARITY;
        }
    }

    private void checkAndSetStopbits(int stopbits) throws JposException {
        if (stopbits == 1)
            Stopbits = WinBase.ONE5STOPBITS;
        else if (stopbits == 2)
            Stopbits = WinBase.TWOSTOPBITS;
        else
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Unsupported number of stop bits: " + stopbits);
    }

    private void checkAndSetDatabits(int databits) throws JposException {
        if (databits == 7 || databits == 8)
            Databits = databits;
        else
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Unsupported data size: " + databits);
    }

    private void checkAndSetBaudrate(int baudrate) throws JposException {
        switch (baudrate) {
            default:
                throw new JposException(JposConst.JPOS_E_ILLEGAL, "Unsupported baudrate: " + baudrate);
            case 1200:
                Baudrate = WinBase.CBR_1200;
                break;
            case 4800:
                Baudrate = WinBase.CBR_4800;
                break;
            case 9600:
                Baudrate = WinBase.CBR_9600;
                break;
            case 19200:
                Baudrate = WinBase.CBR_19200;
                break;
            case 38400:
                Baudrate = WinBase.CBR_38400;
                break;
            case 128000:
                Baudrate = WinBase.CBR_128000;
                break;
            case 256000:
                Baudrate = WinBase.CBR_256000;
                break;
        }
    }
}
