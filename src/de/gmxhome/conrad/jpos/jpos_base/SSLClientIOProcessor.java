/*
 * Copyright 2023 Martin Conrad
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
 *
 */

package de.gmxhome.conrad.jpos.jpos_base;

import jpos.*;
import net.bplaced.conrad.log4jpos.*;
import javax.net.ssl.*;
import java.io.*;
import java.net.*;

/**
 * Class to process SSL/TLS client communication. Includes functionality for automatic
 * data logging. Implementation is based on JavaPOS-SPF class <i>TcpClientIOProcessor</i>
 * and uses the Javax classes <i>SSLSocket</i> and <i>SSLSocketFactory</i>.
 */
public class SSLClientIOProcessor extends TcpClientIOProcessor {
    private SSLSocketFactory Factory;

    /**
     * Stores JposDevice and tcp address of derived IO processors. The device will
     * be used for logging while the tcp address specifies the communication object.
     * An SSLSocketFactory can be provided to be used instead of the default factory
     * to allow use of specific authorization options.
     *
     * @param dev  Device that uses the proceessor. Processor uses loging of device
     *             to produce logging entries
     * @param addr Communication object, e.g. 127.0.0.1:23456
     * @param factory SSLSocketFactory to be used to create the SSL socket. If null,
     *                the default factory will be used.
     * @throws JposException If addr if not a valid tcp address in format IP:port.
     */
    public SSLClientIOProcessor(JposDevice dev, String addr, SSLSocketFactory factory) throws JposException {
        super(dev, addr);
        Factory = factory == null ? (SSLSocketFactory) SSLSocketFactory.getDefault() : factory;
    }

    @Override
    public void open(boolean noErrorLog) throws JposException {

        if (Sock != null) {
            Dev.log(Level.ERROR, LoggingPrefix + "Open error: Socket just connected");
            throw new JposException(JposConst.JPOS_E_ILLEGAL, IOProcessorError, "Socket just connected");
        }
        try {
            Sock = new Socket();
            if (OwnPort != 0)
                Sock.bind(new InetSocketAddress((InetAddress) null, OwnPort));
            Sock.connect(new InetSocketAddress(TargetIP, TargetPort), ConnectTimeout);
            Sock = Factory.createSocket(Sock, TargetIP.getHostAddress(), TargetPort, true);
            ((SSLSocket) Sock).startHandshake();
            // Special UniquiIOProcessor, only used for logging because super.super.open(...) does not work.
            new UniqueIOProcessor(Dev, Port).open(noErrorLog);
        } catch (Exception e) {
            if (Sock != null) {
                try {
                    Sock.close();
                } catch (Exception ex) {}
                Sock = null;
            }
            if (noErrorLog)
                throw new JposException(JposConst.JPOS_E_ILLEGAL, IOProcessorError, e.getMessage(), e);
            logerror("Open", JposConst.JPOS_E_ILLEGAL, e);
        }
    }
}
