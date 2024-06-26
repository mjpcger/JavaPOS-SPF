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

import jpos.JposException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import static jpos.JposConst.*;
import static net.bplaced.conrad.log4jpos.Level.*;

/**
 * Class to process TCP client communication. Includes functionality for automatic
 * data logging. Implementation is based on Java class <i>Socket</i>.
 */
public class TcpClientIOProcessor extends UniqueIOProcessor {
    /**
     * Stores JposDevice and tcp address of derived IO processors. The device will
     * be used for logging while the tcp address specifies the communication object.
     *
     * @param dev  Device that uses the proceessor. Processor uses loging of device
     *             to produce logging entries
     * @param addr Communication object, e.g. 127.0.0.1:23456 for IPv4 and [12:34:56:78:9a:bc:de:f0]:23456
     *             for IPv6.
     * @throws JposException If addr if not a valid tcp address in format IP:port.
     */
    public TcpClientIOProcessor(JposDevice dev, String addr) throws JposException {
        super(dev, addr);
        String[] splitaddr = addr.split(":");
        if (splitaddr.length != 2) {
            int idx = 0;
            if (addr.charAt(0) != '[' || (idx = addr.indexOf(']')) < 0 || addr.indexOf(']',idx + 1) >= 0
                    || (splitaddr = addr.substring(idx).split(":")).length != 2 || !splitaddr[0].equals("]")) {
                logerror("TcpClientIOProcessor", JPOS_E_ILLEGAL, addr + " invalid: Format must be ip:port");
            } else {
                splitaddr[0] = addr.substring(1, idx);
            }
        }
        int port;
        try {
            if ((port = Integer.parseInt(splitaddr[1])) <= 0 || port > 0xffff)
                throw new JposException(JPOS_E_ILLEGAL, IOProcessorError, splitaddr[1] + " invalid: Must be between 1 and 65535");
            TargetIP = InetAddress.getByName(splitaddr[0]);
            TargetPort = port;
            InitialPort = getTarget();
        } catch (Exception e) {
            logerror("TcpClientIOProcessor", JPOS_E_FAILURE, e);
        }
    }

    /**
     * Connect timeout. Default: 1.5 seconds.
     */
    int ConnectTimeout = 1500;

    /**
     * Target IP, converted from Target.
     */
    InetAddress TargetIP;

    /**
     * Target port, converted from Target.
     */
    int TargetPort;

    /**
     * Own TCP port, can be set to allow better filtering by firewall. If zero, a
     * random port will be used.
     */
    int OwnPort = 0;

    /**
     * Sets TCP specific communication parameter.
     * @param ownport   Own port address
     * @throws JposException    If port address &lt; 0 or port address &gt; 65535
     */
    public void setParam(int ownport) throws JposException {
        if (ownport < 0 || ownport > 0xffff)
            logerror("SetParam", JPOS_E_ILLEGAL, "Invalid port: " + ownport + ", must be between 0 and 65535");
        OwnPort = ownport;
    }

    /**
     * Sets TCP specific communication parameter.
     * @param ownport   Own port address
     * @param connectTimeout  Connect timeout in milliseconds. Must be &gt; 0
     * @throws JposException    If port address &lt; 0 or port address &gt; 65535
     */
    public void setParam(int ownport, int connectTimeout) throws JposException {
        if (connectTimeout <= 0)
            logerror("SetParam", JPOS_E_ILLEGAL, "Invalid connect timeout: " + connectTimeout + ", must be > 0");
        setParam(ownport);
        ConnectTimeout = connectTimeout;
    }

    @Override
    public String setTarget(String target) throws JposException {
        if (!target.equals(Port))
            logerror("SetTarget", JPOS_E_ILLEGAL, "Target must match " + Port);
        return Target;
    }

    @Override
    public int write(byte[] buffer) throws JposException {
        synchronized (WriteSynchronizer) {
            if (Sock == null)
                logerror("Write", JPOS_E_ILLEGAL, "Socket not connected");
            try {
                Sock.getOutputStream().write(buffer);
            } catch (IOException e) {
                logerror("Write", JPOS_E_FAILURE, e);
            }
            return super.write(buffer);
        }
    }

    @Override
    public int available() throws JposException {
        if (Sock == null)
            logerror("Available", JPOS_E_ILLEGAL, "Socket not connected");
        try {
            LoggingData = String.valueOf(Sock.getInputStream().available()).getBytes();
        } catch (IOException e) {
            logerror("Available", JPOS_E_FAILURE, e);
        }
        return super.available();
    }

    @Override
    public byte[] read(int count) throws JposException {
        synchronized(ReadSynchronizer) {
            if (Sock == null)
                logerror("Read", JPOS_E_ILLEGAL, "Socket not connected");
            try {
                Sock.setSoTimeout(Timeout);
                long start = System.currentTimeMillis();
                int len = Sock.getInputStream().read(LoggingData = new byte[count]);
                if (len > 0 || (Timeout >= 0 && System.currentTimeMillis() - start >= Timeout))
                    LoggingData = len > 0 ? Arrays.copyOf(LoggingData, len) : new byte[0];
                else
                    logerror("Read", JPOS_E_FAILURE, "Bad socket: Timeout not working ");
            } catch (SocketTimeoutException e) {
                LoggingData = new byte[0];
            } catch (IOException e) {
                logerror("Read", JPOS_E_FAILURE, e);
            }
            return super.read(count);
        }
    }

    @Override
    @SuppressWarnings("unused")
    public void flush() throws JposException {
        synchronized(ReadSynchronizer) {
            if (Sock == null)
                logerror("Flush", JPOS_E_ILLEGAL, "Socket not connected");
            try {
                Sock.setSoTimeout(1);
                int count;
                while ((count = Sock.getInputStream().available()) > 0) {
                    int got = Sock.getInputStream().read(new byte[count]);
                }
            } catch (IOException e) {
                logerror("Flush", JPOS_E_FAILURE, e);
            }
            super.flush();
        }
    }

    /**
     * Socket bound to this IO processor.
     */
    Socket Sock = null;

    @Override
    public void open(boolean noErrorLog) throws JposException {
        if (Sock != null) {
            Dev.log(ERROR, LoggingPrefix + "Open error: Socket just connected");
            throw new JposException(JPOS_E_ILLEGAL, IOProcessorError, "Socket just connected");
        }
        try {
            Sock = new Socket();
            if (OwnPort != 0)
                Sock.bind(new InetSocketAddress((InetAddress) null, OwnPort));
            Sock.connect(new InetSocketAddress(TargetIP, TargetPort), ConnectTimeout);
            super.open(noErrorLog);
        } catch (Exception e) {
            if (noErrorLog)
                throw new JposException(JPOS_E_ILLEGAL, IOProcessorError, e.getMessage(), e);
            logerror("Open", JPOS_E_ILLEGAL, e);
        }
    }

    @Override
    public void close() throws JposException {
        if (Sock == null) {
            Dev.log(ERROR, LoggingPrefix + "Close error: Socket just closed");
            throw new JposException(JPOS_E_ILLEGAL, IOProcessorError, "Socket just closed");
        }
        try {
            Sock.close();
        } catch (IOException e) {
            logerror("Close", JPOS_E_FAILURE, e);
        }
        Sock = null;
        super.close();
    }
}
