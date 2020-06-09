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

package de.gmxhome.conrad.JNALinux;

import com.sun.jna.Structure;
import de.gmxhome.conrad.jpos.jpos_base.SerialIOAdapter;
import com.sun.jna.Native;
import com.sun.jna.platform.linux.*;

import java.io.*;
import java.util.Arrays;
import java.util.Properties;

/**
 * Implementation of SerialIOAdapter using native Linux calls via JNA. Can be used for all similar operating systems as
 * long as the arguments of open, read, write, close, poll and system match the interface.
 */
public class JnaLinuxSerial implements SerialIOAdapter {
    /**
     * Interface for some I/O relevant functions provided by Linux and other Unix-like operating systems.
     */
    public static interface LibCExt extends LibC {
        /**
         * Open a file or device.
         * @param name  File or device name
         * @param mode  Access mode
         * @return      A positive value as a file descriptor for use in subsequent OS calls or -1 to report an error.
         */
        public int open(String name, int mode);

        /**
         * Access mode value for read-only access.
         */
        public static final int O_RDONLY = 0;

        /**
         * Access mode value for write-only access.
         */
        public static final int O_WRONLY = 1;

        /**
         * Access mode for read and write access.
         */
        public static final int O_RDWR = 2;

        /**
         * Read data from file or device.
         * @param fd    File descriptor from previous open call.
         * @param buffer Data buffer to be filled.
         * @param count Maximum number of bytes to read.
         * @return  Number of bytes read. 0 in case of timeout, -1 in error case.
         */
        public int read(int fd, byte[] buffer, int count);

        /**
         * Write data to file or device.
         * @param fd    File descriptor from previous open call.
         * @param buffer Data buffer to be writte.
         * @param count Number of bytes to be written.
         * @return  Number of bytes written or -1 in error cases.
         */
        public int write(int fd, byte[] buffer, int count);

        /**
         * Close a file or device
         * @param fd    File descriptor from previous open call.
         * @return      0 on success, -1 in error case.
         */
        public int close(int fd);

        /**
         * Checks whether files or devices are ready for reading or writing.
         * @param fds       Array of pollfd structures, each specifying whether the file or device belonging to its file
         *                  descriptor shall be checked for reading or writing without blocking.
         * @param count     Number of pollfd structures to be used.
         * @param timeout   Maximum number of milliseconds to wait before giving up.
         * @return  Number of file descriptors that can be used for at least one of the requested operations without
         *          blocking, 0 in case of a timeout, -1 if an error occurred.
         */
        public int poll(pollfd[] fds, int count, int timeout);

        /**
         * Structure pollfd, to be used in OS call poll().
         */
        @Structure.FieldOrder({"fd", "events", "revents"})
        public static class pollfd extends Structure {
            /**
             * Input field, must contain a file descriptor returned by a previous open call. A negative value makes
             * this pollfd entry invalid. The poll() system calls ignores invalid pollfd entries.
             */
            public int fd;

            /**
             * Input field, must contain a bitwise combination of POLLIN and POLLOUT.
             */
            public short events;

            /**
             * Output field, contains a bitwise combination of POLLIN, POLLOUT, POLLERR, POLLNVAL and perhaps other less
             * relevant values. Specifies the condition that is fulfilled by the corresponding file descriptor.
             */
            public short revents;
        }

        /**
         * Bit value for poll request for non-blocking input. If set in pollfd property events, it specifies that the
         * file descriptor shall be checked for the ability to read without blocking. If set in pollfd property revents,
         * it specifies that the next read will not block.
         */
        public static final short POLLIN = 1;

        /**
         * Bit value for poll request for non-blocking output. If set in pollfd property events, it specifies that the
         * file descriptor shall be checked for the ability to write without blocking. If set in pollfd property
         * revents, it specifies that the next write will not block.
         */
        public static final short POLLOUT = 4;

        /**
         * Bit value for poll request to signal an error condition on the specified file descriptor. Whenever set in
         * pollfd property revents, it specifies an error condition on the corresponding file descriptor.
         */
        public static final short POLLERR = 8;

        /**
         * Bit value for poll request to signal that the specified file descriptor is invalid. Whenever set in
         * pollfd property revents, the corresponding file descriptor has been closed in the meantime (or has not been
         * opened previously).
         */
        public static final short POLLNVAL = 32;

        /**
         * Run a shell command and returns its exit code.
         * @param command   Any command as it can be specified in a shell (/bin/sh).
         * @return The exit code of the command.
         */
        public int system(String command);
    }

    private static LibCExt LibCLib;

    static {
        LibCLib = null;
        try {
            if (!System.getProperty("os.name").startsWith("Windows")) {
                LibCLib = (LibCExt) Native.load("c", LibCExt.class);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private String Port = null;
    private int Filedesc = -1;

    /**
     * Constructor. Creates communication adapter.
     */
    public JnaLinuxSerial() {
        Port = null;
        Buffer = InputBuffers[CurrentBuffer = 0];
    }

    @Override
    synchronized public void open(String port) throws IOException {
        checkOpened(false);
        Port = port;
        Filedesc = LibCLib.open(Port, LibCExt.O_RDWR);
        if (Filedesc < 0)
            throw new IOException("Open error");
    }

    @Override
    public void close() throws IOException {
        int fd = checkOpened(true);
        IOException e = null;
        if (LibCLib.close(fd) < 0)
            e = new IOException("Close error");
        Filedesc = -1;
        if (e != null)
            throw e;
    }

    @Override
    public void setParameters(int baudrate, int databits, int stopbits, int parity) throws IOException {
        checkOpened(true);
        if (baudrate < 0 || baudrate >= ValidBaudrates.length)
            throw new IOException("Baudrate out of range: " + baudrate);
        if (databits < 0 || databits >= ValidDatasizes.length)
            throw new IOException("Data size out of range: " + databits);
        if (stopbits < 0 || stopbits >= ValidStopbits.length)
            throw new IOException("Stop bits out of range: " + stopbits);
        if (parity < 0 || parity >= ValidParities.length)
            throw new IOException("Parity out of range: " + parity);
        String sttycmd = SttyCommandPrefix + " " + Baudrates[ValidBaudrates[baudrate][1]];
        sttycmd += " " + Datasize[ValidDatasizes[databits][1]] + " " + Stopbits[ValidStopbits[stopbits][1]];
        int result = LibCLib.system(sttycmd += " " + Parities[ValidParities[parity][1]] + " < " + Port);
        if (result != 0)
            throw new IOException("Cannot set communication paremeters, '" + sttycmd + "' result code: " + result);
    }

    private byte[] Buffer;
    private int    BufferCount = 0;
    private int    CurrentBuffer;
    private byte[][] InputBuffers = { new byte[1000], new byte[1000] };

    @Override
    synchronized public int available() throws IOException {
        int fd = checkOpened(true);
        if (BufferCount == Buffer.length)
            return Buffer.length;
        LibCExt.pollfd[] fds = { new LibCExt.pollfd() };
        fds[0].fd = fd;
        fds[0].events = LibCExt.POLLIN;
        int ret = LibCLib.poll(fds, fds.length, 0);
        if (ret > 0 && (fds[0].revents & LibCExt.POLLIN) != 0) {
            byte[] buffer = new byte[Buffer.length - BufferCount];
            ret = LibCLib.read(fd, buffer, buffer.length);
            if (ret > 0) {
                System.arraycopy(buffer, 0, Buffer, BufferCount, ret);
                BufferCount += ret;
            }
        }
        else if (ret > 0 && (fds[0].revents & LibCExt.POLLERR) != 0)
            ret = -1;   // poll signalled an error
        if (BufferCount > 0)
            return BufferCount;
        if (ret < 0)
            throw new IOException("Poll or read error");
        return 0;
    }

    @Override
    public byte[] read(int count, int timeout) throws IOException {
        if (count < 0)
            throw new IOException("Invalid read count: " + count);
        int fd;
        synchronized (this) {
            if (available() > 0) {
                byte[] ret = Arrays.copyOf(Buffer, count < BufferCount ? count : BufferCount);
                if (ret.length < BufferCount) {
                    CurrentBuffer = 1 - CurrentBuffer;
                    System.arraycopy(Buffer, ret.length, InputBuffers[CurrentBuffer], 0, BufferCount -= ret.length);
                    Buffer = InputBuffers[CurrentBuffer];
                } else
                    BufferCount = 0;
                return ret;
            }
            fd = Filedesc;
        }
        byte[] buffer = new byte[count];
        LibCExt.pollfd[] fds = { new LibCExt.pollfd() };
        fds[0].fd = fd;
        fds[0].events = LibCExt.POLLIN;
        int ret = LibCLib.poll(fds, fds.length, timeout);
        if (ret > 0 && (fds[0].revents & LibCExt.POLLIN) != 0) {
            ret = LibCLib.read(fd, buffer, buffer.length);
            if (ret > 0)
                return ret == buffer.length ? buffer : Arrays.copyOf(buffer, ret);
            else if (ret < 0)
                throw new IOException("Read error");
        }
        else if (ret < 0 || (fds[0].revents & LibCExt.POLLERR) != 0)
            throw new IOException("Poll (read) error");
        return new byte[0];
    }

    @Override
    synchronized public void write(byte[] buffer) throws IOException {
        int fd = checkOpened(true);
        while (buffer.length > 0) {
            int ret = LibCLib.write(fd, buffer, buffer.length);
            if (ret <= 0)
                throw new IOException("Write error");
            buffer = Arrays.copyOfRange(buffer, ret, buffer.length);
        }
    }

    @Override
    public void flush() throws IOException {
        int count;
        while ((count = available()) > 0)
            read(count, 0);
    }

    @Override
    public boolean exits(String port) {
        int fd = LibCLib.open(port, LibCExt.O_RDONLY);
        if (fd >= 0) {
            LibCLib.close(fd);
            return true;
        }
        return false;
    }

    private static String[] Baudrates = {"1200", "4800", "9600", "19200", "38400", "57600", "115200"};
    private static String[] Datasize = {"cs7", "cs8"};
    private static String[] Stopbits = {"-cstopb", "cstopb"};
    private static String[] Parities = {"-parenb", "parenb parodd", "parenb -parodd"};
    private static String   SttyCommandPrefix = "stty raw -echo";
    private static int[][] ValidBaudrates = {
            {B_1200, 0},
            {B_4800, 1},
            {B_9600, 2},
            {B_19200, 3},
            {B_38400, 4},
            {B_57600, 5},
            {B_115200, 6},
            {B_128000, 7},
            {B_256000, 8}
    };
    private static int[][] ValidDatasizes = {
            {D_7, 0},
            {D_8, 1}
    };
    private static int[][] ValidStopbits = {
            {S_1, 0},
            {S_2, 1}
    };
    private static int[][] ValidParities = {
            {P_NO, 0},
            {P_ODD, 1},
            {P_EVEN, 2},
            {P_MARK, 3},
            {P_SPACE, 4}
    };

    static {
        Properties set = new Properties();
        try {
            set.load(new BufferedInputStream(new FileInputStream(new File(JnaLinuxSerial.class.getName() + ".properties"))));
            Baudrates = new String[]{
                    set.getProperty("BD1200", "1200"),
                    set.getProperty("BD4800", "4800"),
                    set.getProperty("BD9600", "9600"),
                    set.getProperty("BD19200", "19200"),
                    set.getProperty("BD38400", "38400"),
                    set.getProperty("BD57600", "57600"),
                    set.getProperty("BD115200", "115200"),
                    set.getProperty("BD128000", ""),
                    set.getProperty("BD256000", "")
            };
            Datasize = new String[]{
                    set.getProperty("CS7", "cs7"),
                    set.getProperty("CS8", "cs8")
            };
            Stopbits = new String[]{
                    set.getProperty("SB1", "-cstopb"),
                    set.getProperty("SB2", "cstopb")
            };
            Parities = new String[]{
                    set.getProperty("PARNO", "-parenb"),
                    set.getProperty("PARODD", "parenb parodd"),
                    set.getProperty("PAREVEN", "parenb -parodd"),
                    set.getProperty("PARMARK", ""),
                    set.getProperty("PARSPACE", "")
            };
            SttyCommandPrefix = set.getProperty("STTYPREFIX", "stty raw -echo");
        } catch (IOException e) {
            e.printStackTrace();
        }
        ValidBaudrates = cleanValidValues(ValidBaudrates, Baudrates);
        ValidDatasizes = cleanValidValues(ValidDatasizes, Datasize);
        ValidStopbits = cleanValidValues(ValidStopbits, Stopbits);
        ValidParities = cleanValidValues(ValidParities, Parities);
    }

    private static int[][] cleanValidValues(int[][] valuetupels, String[] strings) {
        int count = 0;
        for(int i = 0; i < strings.length; i++) {
            if (!strings[i].equals("")) {
                valuetupels[count++] = valuetupels[i];
            }
        }
        return Arrays.copyOf(valuetupels, count);
    }

    @Override
    public int[][] getCommunicationConstants(int constantType) {
        int[][][]validvalues = {
                ValidBaudrates, ValidDatasizes, ValidStopbits, ValidParities
        };
        if (constantType >= 0 && constantType < validvalues.length)
            return validvalues[constantType];
        return null;
    }

    private int checkOpened(boolean opened) throws IOException {
        if (LibCLib == null)
            throw new IOException("JNA instance libc.a not available");
        if ((Filedesc < 0) == opened)
            throw new IOException("Port " + (opened ? "not opened" : "just opened"));
        return Filedesc;
    }
}
