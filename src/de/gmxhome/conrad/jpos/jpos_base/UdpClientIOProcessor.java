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
 * Class to process client UDP communication. Client communication means communication with one UDP socket, initiated by
 * this instance. Includes functionality for automatic data logging. Implementation is based on Java class
 * <i>DatagramChannel</i>.
 */
public class UdpClientIOProcessor extends UdpBaseIOProcessor {
    /**
     * Stores JposDevice and port of derived IO processors. The device will
     * be used for logging while the port specifies the communication object.
     *
     * @param dev  Device that uses the processor. Processor uses logging of device
     *             to produce logging entries
     * @param addr Communication object, e.g. 127.0.0.1:23456 for IPv4 and [12:34:56:78:9a:bc:de:f0]:23456
     *             for IPv6.
     * @throws JposException If port does not specify a valid communication object
     */
    public UdpClientIOProcessor(JposDevice dev, String addr) throws JposException {
        super(dev, addr);
        OpenedText = "connected";
        String[] splitaddr = addr.split(":");
        if (splitaddr.length != 2) {
            int idx = 0;
            if (addr.charAt(0) != '[' || (idx = addr.indexOf(']')) < 0 || addr.indexOf(']',idx + 1) >= 0
                    || (splitaddr = addr.substring(idx).split(":")).length != 2 || !splitaddr[0].equals("]")) {
                logerror(getClass().getSimpleName(), JPOS_E_ILLEGAL, addr + " invalid: Format must be ip:port");
            } else {
                splitaddr[0] = addr.substring(1, idx);
            }
        }
        int port;
        try {
            if ((port = Integer.parseInt(splitaddr[1])) <= 0 || port > 0xffff)
                throw new JposException(JPOS_E_ILLEGAL, IOProcessorError, splitaddr[1] + " invalid: Must be between 1 and 65535");
            TargetIP = new InetSocketAddress(InetAddress.getByName(splitaddr[0]), port);
            InitialPort = getTarget();
        } catch (Exception e) {
            logerror(getClass().getSimpleName(), JPOS_E_FAILURE, e);
        }
    }

    @Override
    public String setTarget(String target) throws JposException {
        if (!target.equals(Port))
            logerror("SetTarget", JPOS_E_ILLEGAL, "Target must match " + Port);
        return Target;
    }

    /**
     * Sets UDP specific communication parameter.
     * @param ownport   Own port address
     * @throws JposException    If port address &lt; 0 or port address &gt; 65535
     */
    public void setParam(int ownport) throws JposException {
        if (ownport < 0 || ownport > 0xffff)
            logerror("SetParam", JPOS_E_ILLEGAL, "Invalid port: " + ownport + ", must be between 0 and 65535");
        OwnPort = ownport;
    }

    @Override
    String initAfterBind() throws IOException {
        Socket.connect(TargetIP);
        return Port + " Reader";
    }

    @Override
    void send(ByteBuffer buffer) throws IOException {
        Socket.write(buffer);
    }

    Object getFrameOrException(ByteBuffer data) throws IOException {
        int count = Socket.read(data);
        if (count <= 0)
            return null;
        return new Frame(Arrays.copyOf(data.array(), count), TargetIP);
    }
}
