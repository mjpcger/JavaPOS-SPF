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

import jpos.JposException;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static jpos.JposConst.*;

/**
 * Class to process UDP communication. Client communication means communication with one UDP socket, initiated by this
 * instance. Server socket communication adds the ability to communicate with several UDP sockets, initiated by this
 * socket or any other socket. Includes functionality for automatic data logging. Implementation is based on Java class
 * <i>DatagramChannel</i>.<br>
 * Keep in mind: To allow correct message processing, the implementation has been made as follows:
 * <ul>
 *     <li> After open, before the first message has been received, getSource() will return 0.0.0.0:0.</li>
 *     <li> After the first message has been received, getSource() will always return the address of the sender of the
 *     last read message. After calling flush(), getSource() will return 0.0.0.0:0 again.</li>
 *     <li> In case of a multi-threaded application, ensure to synchronize read() and the corresponding getSource()
 *     call to ensure that the message will be assigned to the correct source.</li>
 *     <li> Before sending the first message, setTarget() must be used to set the target IP and port.</li>
 *     <li> If a response to an incoming message shall be sent, you can use setTarget(getSource) as long as no other
 *     message has been read.</li>
 *     <li> The target set by setTarget() is valid as long as another target will be set. In case of a multi-threaded
 *     application, ensure to synchronize setTarget() and the corresponding write() call to ensure that the message sent
 *     by write receives the correct target.</li>
 * </ul>
 */
public class UdpIOProcessor extends UdpBaseIOProcessor {
    /**
     * Stores JposDevice of derived IO processors and own port number. The device will
     * be used for logging. The port number is the local port to be used to communicate with other sockets. If zero,
     * a random port will be used.
     *
     * @param device  Device that uses the processor. Processor uses logging of device
     *                to produce logging entries.
     * @param ownport Local port to be used by this socket.
     * @throws jpos.JposException Will not be thrown.
     */
    public UdpIOProcessor(JposDevice device, int ownport) throws JposException {
        super(device, "0.0.0.0:0");
        OpenedText = "opened";
        try {
            TargetIP = new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 0);
            SourceIP = new InetSocketAddress(TargetIP.getAddress(), TargetIP.getPort());
            if (ownport < 0 || ownport > 0xffff)
                throw new JposException(JPOS_E_ILLEGAL, IOProcessorError, "Invalid port: " + ownport);
            OwnPort = ownport;
            LoggingPrefix = "";
        } catch (Exception e) {
            logerror(getClass().getSimpleName(), JPOS_E_FAILURE, e);
        }
    }

    /**
     * Sets target address for further write operations.
     * @param addr communication target, e.g. 127.0.0.1:23456 for IPv4 and [12:34:56:78:9a:bc:de:f0]:23456
     *             for IPv6.
     * @return Target in its internal String representation.
     * @throws JposException If an error occurs.
     */
    @Override
    public String setTarget(String addr) throws JposException {
        String[] splitaddr = addr.split(":");
        if (splitaddr.length != 2) {
            int idx = 0;
            if (addr.charAt(0) != '[' || (idx = addr.indexOf(']')) < 0 || addr.indexOf(']',idx + 1) >= 0
                    || (splitaddr = addr.substring(idx).split(":")).length != 2 || !splitaddr[0].equals("]")) {
                logerror("UdpClientIOProcessor", JPOS_E_ILLEGAL, addr + " invalid: Format must be ip:port");
            } else {
                splitaddr[0] = addr.substring(1, idx);
            }
        }
        int port;
        InetAddress address;
        try {
            if ((port = Integer.parseInt(splitaddr[1])) <= 0 || port > 0xffff)
                throw new JposException(JPOS_E_ILLEGAL, IOProcessorError, splitaddr[1] + " invalid: Must be between 1 and 65535");
            address = InetAddress.getByName(splitaddr[0]);
            if (((Socket != null && Socket.socket().getLocalPort() == port) || port == OwnPort) &&
                    (address.equals(InetAddress.getByName("localhost"))))
                throw new JposException(JPOS_E_ILLEGAL, 0, "Target cannot be own address");
            TargetIP = new InetSocketAddress(address, port);
        } catch (Exception e) {
            logerror("SetTarget", JPOS_E_FAILURE, e);
        }
        return getTarget();
    }

    @Override
    public String getTarget() {
        return getAddress(TargetIP.getAddress(), TargetIP.getPort());
    }

    @Override
    public String getSource() {
        return getAddress(SourceIP.getAddress(), SourceIP.getPort());
    }

    private String getAddress(InetAddress addr, int port) {
        String result = addr.getHostAddress();
        if (addr instanceof Inet6Address)
            result = "[" + result + "]";
        return result + ":" + port;
    }

    @Override
    String initAfterBind() {
        LoggingPrefix = "localhost:" + Socket.socket().getLocalPort() +": ";
        return "localhost:" + Socket.socket().getLocalPort() + " Reader";
    }

    @Override
    void send(ByteBuffer buffer) throws IOException {
        Socket.send(buffer, TargetIP);
    }

    Object getFrameOrException(ByteBuffer data) throws IOException {
            InetSocketAddress address = (InetSocketAddress)  Socket.receive(data);
            if (address == null)
                return null;
            return new Frame(Arrays.copyOf(data.array(), data.position()), address);
    }
}
