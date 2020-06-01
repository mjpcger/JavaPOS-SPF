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

package de.gmxhome.conrad.JNAWindows;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.*;
import com.sun.jna.win32.W32APIOptions;
import de.gmxhome.conrad.jpos.jpos_base.SerialIOAdapter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Implementation of SerialIOAdapter using native Win32 OS calls via JNA.
 */
public class JnaSerial implements SerialIOAdapter {

    private static interface Kernel32Ext extends Kernel32 {
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

    private static Kernel32Ext Kernel32Lib;

    static {
        try {
            Kernel32Lib = null;
            if (System.getProperty("os.name").startsWith("Windows")) {
                Kernel32Lib = (Kernel32Ext) Native.load("kernel32", Kernel32Ext.class, W32APIOptions.DEFAULT_OPTIONS);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private String Port;
    private WinNT.HANDLE DeviceHandle;

    /**
     * Constructor. Creates communication adapter.
     */
    public JnaSerial() {
        Port = null;
    }

    @Override
    synchronized public void open(String port) throws IOException {
        checkOpened(false);
        Port = port;
        WinNT.HANDLE hd = Kernel32Lib.CreateFile("\\\\.\\" + Port,
                WinNT.GENERIC_READ|WinNT.GENERIC_WRITE, 0, null, WinNT.OPEN_EXISTING, WinNT.FILE_FLAG_OVERLAPPED, null);
        if (hd.equals(WinBase.INVALID_HANDLE_VALUE)) {
            int error = Kernel32Lib.GetLastError();
            if (error == WinError.ERROR_FILE_NOT_FOUND)
                throw new FileNotFoundException("Got error code " + error);
            throw new IOException("Got error code " + error);
        }
        DeviceHandle = hd;
   }

    @Override
    public void close() throws IOException {
        WinNT.HANDLE hd = checkOpened(true);
        if (!Kernel32Lib.CloseHandle(hd)) {
            DeviceHandle = null;    // What shall we do else?
            throw new IOException("Closing port failed with error code " + Kernel32Lib.GetLastError());
        }
    }

    @Override
    public void setParameters(int baudrate, int databits, int stopbits, int parity) throws IOException {
        WinNT.HANDLE hd = checkOpened(true);
        WinBase.DCB dcb = new WinBase.DCB();
        if (!Kernel32Lib.GetCommState(hd, dcb))
            throw new IOException("GetCommState returns error code " + Kernel32Lib.GetLastError());
        dcb.BaudRate.setValue(baudrate);
        dcb.ByteSize.setValue(databits);
        dcb.StopBits.setValue(stopbits);
        dcb.Parity.setValue(parity);
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
            throw new IOException("SetCommState returned error code " + Kernel32Lib.GetLastError());
        setTimeouts(0);
    }

    @Override
    synchronized public int available() throws IOException {
        WinNT.HANDLE hd = checkOpened(true);
        IntByReference errors = new IntByReference(0);
        WinBaseExt.COMSTAT stat = new WinBaseExt.COMSTAT();
        if (!Kernel32Lib.ClearCommError(hd, errors, stat))
            throw new IOException("ClearCommError returned with error code " + Kernel32Lib.GetLastError());
        return stat.cbInQue;
    }

    @Override
    public byte[] read(int count, int timeout) throws IOException {
        WinNT.HANDLE hd = checkOpened(true);
        IntByReference receicedBytes = new IntByReference(-1);
        Memory received = new Memory(count);
        setTimeouts(timeout);
        WinBase.OVERLAPPED ov = new WinBase.OVERLAPPED();
        ov.writeField("hEvent", Kernel32Lib.CreateEvent(null, true, false, null));
        if (ov.hEvent == null)
            throw new IOException("Read event creation returned error code " + Kernel32Lib.GetLastError());
        try {
            if (!(Kernel32Lib.ReadFile(hd, received.getByteBuffer(0, count), count, null, ov))) {
                int code = Kernel32Lib.GetLastError();
                if (code != WinError.ERROR_IO_PENDING)
                    throw new IOException("Read returned error code " + code);
            }
            if (!Kernel32Lib.GetOverlappedResult(hd, ov, receicedBytes, true))
                throw new IOException("GetOverlappedResult returned error code " + Kernel32Lib.GetLastError());
        } finally {
            Kernel32Lib.CloseHandle(ov.hEvent);
        }
        if (receicedBytes.getValue() < 0 )
            throw new IOException("GetOverlappedResult returned invalid count " + receicedBytes.getValue());
        return received.getByteArray(0, receicedBytes.getValue());
    }

    @Override
    synchronized public void write(byte[] buffer) throws IOException {
        WinNT.HANDLE hd = checkOpened(true);
        int actpos, actlen;
        Memory memory = new Memory(actlen = buffer.length);
        memory.write(actpos = 0, buffer, 0, actlen);
        IntByReference sentBytes = new IntByReference(-1);
        do {
            sentBytes.setValue(actlen);
            WinBase.OVERLAPPED ov = new WinBase.OVERLAPPED();
            ov.writeField("hEvent", Kernel32Lib.CreateEvent(null, true, false, null));
            if (ov.hEvent == null)
                throw new IOException("Write event creation error " + Kernel32Lib.GetLastError());
            try {
                if (!Kernel32Lib.WriteFile(hd, memory.getByteBuffer(actpos, actlen), actlen, null, ov)) {
                    int code = Kernel32Lib.GetLastError();
                    if (code != WinError.ERROR_IO_PENDING)
                        throw new IOException("WriteFile returned error code " + Kernel32Lib.GetLastError());
                }
                if (!Kernel32Lib.GetOverlappedResult(hd, ov, sentBytes, true))
                    throw new IOException("GetOverlappedResult returned error code " + Kernel32Lib.GetLastError());
            } finally {
                Kernel32Lib.CloseHandle(ov.hEvent);
            }
            if (sentBytes.getValue() > 0) {
                actpos += sentBytes.getValue();
                if ((actlen = buffer.length - actpos) == 0)
                    return;
            } else
                throw new IOException("Write channel blocked, " + buffer.length + " bytes remaining");
        } while (true);
    }

    @Override
    synchronized public void flush() throws IOException {
        WinNT.HANDLE hd = checkOpened(true);
        if (!Kernel32Lib.PurgeComm(hd, Kernel32Ext.PURGE_RXABORT|Kernel32Ext.PURGE_TXABORT))
            throw new IOException("PurgeComm results with error code " + Kernel32Lib.GetLastError());
    }

    @Override
    public boolean exits(String port) {
        WinNT.HANDLE hd = Kernel32Lib.CreateFile("\\\\.\\" + port,
                WinNT.GENERIC_READ|WinNT.GENERIC_WRITE, 0, null, WinNT.OPEN_EXISTING, 0, null);
        if (WinBase.INVALID_HANDLE_VALUE.equals(hd))
            return Kernel32Lib.GetLastError() != WinError.ERROR_FILE_NOT_FOUND;
        Kernel32Lib.CloseHandle(hd);
        return true;
    }

    private WinNT.HANDLE checkOpened(boolean opened) throws IOException {
        if (Kernel32Lib == null)
            throw new IOException("JNA instance Kernel32 not available");
        if ((DeviceHandle == null) == opened)
            throw new IOException("Port " + (opened ? "not opened" : "just opened"));
        return DeviceHandle;
    }

    private void setTimeouts(int timeout) throws IOException {
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
            throw new IOException("SetCommTimeouts returned error code " + Kernel32Lib.GetLastError());
    }

    @Override
    public int[][]getCommunicationConstants(int constantType) {
        int[][][] validvalues = {
                {   // T_BAUD
                        {B_1200, WinBase.CBR_1200},
                        {B_4800, WinBase.CBR_4800},
                        {B_9600, WinBase.CBR_9600},
                        {B_19200, WinBase.CBR_19200},
                        {B_38400, WinBase.CBR_38400},
                        {B_128000, WinBase.CBR_128000},
                        {B_256000, WinBase.CBR_256000}
                },
                {   // T_DATA
                        {D_7, 7},
                        {D_8, 8}
                },
                {   // T_STOP
                        {S_1, WinBase.ONE5STOPBITS},
                        {S_2, WinBase.TWOSTOPBITS}
                },
                {   // T_PARITY
                        {P_NO, WinBase.NOPARITY},
                        {P_ODD, WinBase.ODDPARITY},
                        {P_EVEN, WinBase.EVENPARITY},
                        {P_MARK, WinBase.MARKPARITY},
                        {P_SPACE, WinBase.SPACEPARITY}
                }
        };
        if (constantType >= 0 && constantType < validvalues.length)
            return validvalues[constantType];
        return null;
    }
}
