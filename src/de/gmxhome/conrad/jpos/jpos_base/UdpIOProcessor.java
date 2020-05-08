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

import java.net.*;
import java.util.Arrays;

/**
 * Class to process UDP communication. Includes functionality for automatic
 * data logging. Implementation is based on Java class <i>DatagramSocket</i>.
 */
public class UdpIOProcessor extends UniqueIOProcessor {
    private JposDevice Dev;
    private int MaxDataSize;
    private int OwnPort;
    private boolean ParamSet = false;
    private InetAddress CurrentTargetIp, SourceIp;
    private int CurrentTargetPort, SourcePort;
    private int Timeout = Integer.MAX_VALUE;
    private DatagramSocket Socket;
    private DatagramPacket ReadPacket;
    /**
     * ExtendedErrorCode value. Indicates target cannot be the own socket.
     */
    public final static int ErrorToMyself = IOProcessorError + 1;

    /**
     * Stores JposDevice and tcp address of derived IO processors. The device will
     * be used for logging while the tcp address specifies the communication object.
     * @param device            JposDevice that uses this IOProcessor instance.
     * @param addr              Target address in format ip:port.
     * @throws JposException    if the target address is invalid. Must be in format ip:port, where ip is an IPv4
     *                          address and port a udp port number.
     */
    public UdpIOProcessor(JposDevice device, String addr) throws JposException {
        super(device, addr);
        Dev = device;
        String[] t = addr.split(":");
        if (t.length != 2) {
            logerror("UdpIOProcessor", JposConst.JPOS_E_ILLEGAL, "Invalid UDP target");
        }
        try {
            int port = Integer.parseInt(t[1]);
            if (port <= 0 || port > 0xfffe) {
                logerror("UdpIOProcessor", JposConst.JPOS_E_ILLEGAL, "Port out of range: " + port);
            }
            SourceIp = InetAddress.getByName("0.0.0.0");
            SourcePort = 0;
            CurrentTargetIp = InetAddress.getByName(t[0]);
            CurrentTargetPort = port;
            Source = Target = InitialPort = getTarget();
        } catch (Exception e) {
            logerror("UdpIOProcessor", JposConst.JPOS_E_ILLEGAL, e);
        }
    }

    @Override
    public int write(byte[] buffer) throws JposException {
        if (Socket == null) {
            logerror("write", JposConst.JPOS_E_ILLEGAL, "Connection not opened");
        }
        try {
            Socket.send(new DatagramPacket(buffer, buffer.length, CurrentTargetIp, CurrentTargetPort));
        } catch (PortUnreachableException e) {
            Dev.log(Level.ERROR,  LoggingPrefix + "write: Port unreachable: " + CurrentTargetIp.getHostAddress() + ":" + CurrentTargetPort);
            return 0;
        } catch (Exception e) {
            logerror("write", JposConst.JPOS_E_ILLEGAL, e);
        }
        return super.write(buffer);
    }

    @Override
    public int available() throws JposException {
        if (Socket == null) {
            logerror("available", JposConst.JPOS_E_ILLEGAL, "Connection not opened");
        }
        byte[] count = new byte[]{'0'};
        for (boolean inerror = false; ReadPacket == null; inerror = true) {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[MaxDataSize], MaxDataSize);
                Socket.setSoTimeout(1);
                Socket.receive(packet);
                count = String.valueOf((ReadPacket = packet).getData().length).getBytes();
            } catch (SocketTimeoutException e) {
            } catch (Exception e) {
                if (inerror) {
                    logerror("available", JposConst.JPOS_E_ILLEGAL, e);
                }
                continue;
            }
            break;
        }
        return super.available();
    }

    @Override
    public byte[] read(int count) throws JposException {
        if (Socket == null) {
            logerror("read", JposConst.JPOS_E_ILLEGAL, "Connection not opened");
        }
        long starttime = System.currentTimeMillis();
        long currenttime = starttime;
        byte[] rc = new byte[]{};
        for (boolean inerror = false; ReadPacket == null && currenttime - starttime < Timeout; inerror = true) {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[MaxDataSize], MaxDataSize);
                Socket.setSoTimeout(Timeout - (int)(currenttime - starttime));
                Socket.receive(packet);
                ReadPacket = packet;
            } catch (SocketTimeoutException e) {
                break;
            } catch (Exception e) {
                currenttime = System.currentTimeMillis();
                if (inerror) {
                    logerror("read", JposConst.JPOS_E_ILLEGAL, e);
                }
                continue;
            }
            break;
        }
        if (ReadPacket != null) {
            SourceIp = ReadPacket.getAddress();
            SourcePort = ReadPacket.getPort();
            rc = Arrays.copyOf(ReadPacket.getData(), ReadPacket.getLength());
            ReadPacket = null;
        }
        if (count < rc.length) {
            LoggingData = Arrays.copyOf(rc, count);
            byte[] remaining = Arrays.copyOfRange(rc, count, rc.length);
            rc = super.read(count);
            Dev.log(Level.TRACE, LoggingPrefix + "Read discarded " + remaining.length + " bytes" + location(true) + ": " + toLogString(remaining));
            return rc;
        }
        LoggingData = rc;
        return super.read(count);
    }

    @Override
    public void flush() throws JposException {
        super.flush();
        int len;
        while ((len = available()) > 0)
            read(len);
    }

    @Override
    public void open(boolean noErrorLog) throws JposException {
        if (!ParamSet) {
            logerror("open", JposConst.JPOS_E_ILLEGAL, "No parameters set");
        }
        try {
            Socket = OwnPort != 0 ? new DatagramSocket(OwnPort) : new DatagramSocket();
        } catch (Exception e) {
            logerror("open", JposConst.JPOS_E_ILLEGAL, e);
        }
        super.open(noErrorLog);
    }

    @Override
    public void close() throws JposException {
        Socket.close();
        Socket = null;
        ReadPacket = null;
        super.close();
    }

    /**
     * Set UDP specific parameters. Must be called before opening the IO processor.
     * @param ownport       Own port number between 1 and 65534
     * @param maxdatasize   Maximum message size, any value &lt; 0
     * @throws JposException If parameters invalid or socket not initialized.
     */
    public void setParameters(int ownport, int maxdatasize) throws JposException {
        if (Socket != null) {
            logerror("setParameters", JposConst.JPOS_E_ILLEGAL, "Interface initialized");
        }
        if (ownport < 0 || ownport > 0xfffe)
            logerror("setParameters", JposConst.JPOS_E_ILLEGAL, "Port out of range: " + ownport);
        if (maxdatasize <= 0)
            logerror("setParameters", JposConst.JPOS_E_ILLEGAL, "Maximum data size must be > 0");
        OwnPort = ownport;
        MaxDataSize = maxdatasize;
        ParamSet = true;
    }

    @Override
    public int setTimeout(int timeout) {
        return Timeout = timeout > 0 ? timeout : (timeout == 0 ? 1 : Integer.MAX_VALUE);
    }

    @Override
    public String setTarget(String target) throws JposException {
        if (Socket == null) {
            logerror("setTarget", JposConst.JPOS_E_ILLEGAL, "Interface not initialized");
        }
        String[] t = target.split(":");
        if (t.length != 2) {
            logerror("setTarget", JposConst.JPOS_E_ILLEGAL, "Invalid UDP target");
        }
        try {
            int port = Integer.parseInt(t[1]);
            if (port <= 0 || port > 0xfffe) {
                logerror("setTarget", JposConst.JPOS_E_ILLEGAL, "Port out of range: " + port);
            }
            InetAddress addr = InetAddress.getByName(t[0]);
            if (addr.getHostAddress().equals(Socket.getLocalAddress().getHostAddress()) && port == Socket.getLocalPort()) {
                logerror("setTarget", new JposException(JposConst.JPOS_E_ILLEGAL, ErrorToMyself, "Cannot target to own address"));
            }
            CurrentTargetIp =addr;
            CurrentTargetPort = port;
        } catch (Exception e) {
            logerror("setTarget", JposConst.JPOS_E_ILLEGAL, e);
        }
        return Target = getTarget();
    }

    @Override
    public String getSource() {
        return SourceIp.getHostAddress() + ":" + Integer.toString(SourcePort);
    }

    @Override
    public String getTarget() {
        return CurrentTargetIp.getHostAddress() + ":" + Integer.toString(CurrentTargetPort);
    }
}
