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
 *
 */

package de.gmxhome.conrad.jpos.jpos_base;

import jpos.JposConst;
import jpos.JposException;
import org.apache.log4j.Level;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class to process client UDP communication. Client communication means communication with one UDP socket, initiated by
 * this instance. Includes functionality for automatic data logging. Implementation is based on Java class
 * <i>DatagramChannel</i>.
 */
public class UdpClientIOProcessor extends UniqueIOProcessor implements Runnable {
    private int TargetPort = 0;
    private InetAddress TargetIP = null;
    private int OwnPort = 0;
    private DatagramChannel Socket = null;

    private Thread TheReader = null;
    private SyncObject ReadWaiter = null;
    private SyncObject ContinueWaiter = null;
    private List<Object> InputData = new ArrayList<Object>();
    private boolean HighWaterWaiting = false;

    /**
     * Stores JposDevice and port of derived IO processors. The device will
     * be used for logging while the port specifies the communication object.
     *
     * @param dev  Device that uses the processor. Processor uses logging of device
     *             to produce logging entries
     * @param addr Communication object, e.g. 127.0.0.1:23456
     * @throws JposException If port does not specify a valid communication object
     */
    public UdpClientIOProcessor(JposDevice dev, String addr) throws JposException {
        super(dev, addr);
        String[] splitaddr = addr.split(":");
        if (splitaddr.length != 2)
            logerror("UdpClientIOProcessor", JposConst.JPOS_E_ILLEGAL, addr +" invalid: Format must be ip:port");
        int port;
        try {
            if ((port = Integer.parseInt(splitaddr[1])) <= 0 || port > 0xffff)
                throw new JposException(JposConst.JPOS_E_ILLEGAL, IOProcessorError, splitaddr[1] + " invalid: Must be between 1 and 65535");
            TargetIP = InetAddress.getByName(splitaddr[0]);
            TargetPort = port;
            InitialPort = getTarget();
        } catch (Exception e) {
            logerror("UdpClientIOProcessor", JposConst.JPOS_E_FAILURE, e);
        }
    }

    /**
     * Sets UDP specific communication parameter.
     * @param ownport   Own port address
     * @throws JposException    If port address &lt; 0 or port address &gt; 65535
     */
    public void setParam(int ownport) throws JposException {
        if (ownport < 0 || ownport > 0xffff)
            logerror("SetParam", JposConst.JPOS_E_ILLEGAL, "Invalid port: " + ownport + ", must be between 0 and 65535");
        OwnPort = ownport;
    }

    @Override
    public int setTimeout(int timeout) {
        return Timeout = timeout > 0 ? timeout : (timeout == 0 ? 1 : Integer.MAX_VALUE);
    }

    @Override
    public String setTarget(String target) throws JposException {
        if (!target.equals(Port))
            logerror("SetTarget", JposConst.JPOS_E_ILLEGAL, "Target must match " + Port);
        return Target;
    }

    @Override
    synchronized public void open(boolean noErrorLog) throws JposException {
        if (Socket != null) {
            Dev.log(Level.ERROR, LoggingPrefix + "Open error: Datagram socket just connected");
            throw new JposException(JposConst.JPOS_E_ILLEGAL, IOProcessorError, "Datagram socket just connected");
        }
        try {
            Socket = DatagramChannel.open();
            if (OwnPort != 0)
                Socket.socket().bind(new InetSocketAddress(OwnPort));
            else
                Socket.socket().bind(null);
            Socket.connect(new InetSocketAddress(TargetIP, TargetPort));
            ReadWaiter = new SyncObject();
            ContinueWaiter = new SyncObject();
            InputData.clear();
            HighWaterWaiting = false;
            (TheReader = new Thread(this, Port + " Reader")).start();
        } catch (Exception e) {
            String message = e.getClass().getSimpleName() + ": " + e.getMessage();
            Dev.log(Level.ERROR, LoggingPrefix + "Open error: " + message);
            throw new JposException(JposConst.JPOS_E_ILLEGAL, IOProcessorError, message, e);
        }
    }

    @Override
    synchronized public void close() throws JposException {
        if (Socket == null) {
            Dev.log(Level.ERROR, LoggingPrefix + "Close error: Socket just closed");
            throw new JposException(JposConst.JPOS_E_ILLEGAL, IOProcessorError, "Socket just closed");
        }
        try {
            Socket.close();
        } catch (Exception e) {
            logerror("Close", JposConst.JPOS_E_FAILURE, e);
        }
        Socket = null;
        ContinueWaiter.signal();
        try {
            TheReader.join();
        } catch (InterruptedException e) {}
        TheReader = null;
        super.close();
    }

    @Override
    public int write(byte[] buffer) throws JposException {
        synchronized (WriteSynchronizer) {
            if (Socket == null)
                logerror("Write", JposConst.JPOS_E_ILLEGAL, "Socket not connected");
            try {
                Socket.write(ByteBuffer.wrap(buffer));
            } catch (Exception e) {
                logerror("Write", JposConst.JPOS_E_FAILURE, e);
            }
            return super.write(buffer);
        }
    }

    @Override
    public void run() {
        while (Socket != null) {
            Object readobj = null;
            int count = -1;
            boolean highwater = false;
            try {
                ByteBuffer data = ByteBuffer.wrap(new byte[Socket.socket().getReceiveBufferSize()]);
                if ((count = Socket.read(data)) <= 0)
                    continue;
                readobj = Arrays.copyOf(data.array(), count);
            } catch (Exception e) {
                readobj = new JposException(JposConst.JPOS_E_FAILURE, e.getClass().getSimpleName() + ": " + e.getMessage(), e);
            }
            synchronized (ReadSynchronizer) {
                InputData.add(readobj);
                if (readobj instanceof byte[] && InputData.size() >= 1000)
                    highwater = HighWaterWaiting = true;
                ReadWaiter.signal();
            }
            if (readobj instanceof JposException || highwater)
                ContinueWaiter.suspend(SyncObject.INFINITE);
        }
    }

    @Override
    public int available() throws JposException {
        synchronized(ReadSynchronizer) {
            int count = 0;
            for (Object data : InputData) {
                if (data instanceof JposException) {
                    if (count == 0) {
                        InputData.clear();
                        ReadWaiter = new SyncObject();
                        ContinueWaiter.signal();
                        throw (JposException) data;
                    }
                    else
                        break;
                }
                else {
                    count += ((byte[]) data).length;
                }
            }
            LoggingData = String.valueOf(count).getBytes();
            return super.available();
        }
    }

    @Override
    public byte[] read(int count) throws JposException {
        if (ReadWaiter.suspend(Timeout)) {
            synchronized (ReadSynchronizer) {
                Object data = InputData.get(0);
                InputData.remove(0);
                if (data instanceof JposException) {
                    ContinueWaiter.signal();
                    throw (JposException) data;
                }
                if (HighWaterWaiting && InputData.size() < 500) {
                    HighWaterWaiting = false;
                    ContinueWaiter.signal();
                }
                LoggingData = (byte[]) data;
            }
        } else
            LoggingData = new byte[0];
        return super.read(count);

    }

    @Override
    public void flush() throws JposException {
        synchronized (ReadSynchronizer) {
            if (InputData.size() > 0) {
                Object e = InputData.get(InputData.size() - 1);
                InputData.clear();
                ReadWaiter = new SyncObject();
                if (e instanceof JposException || HighWaterWaiting) {
                    HighWaterWaiting = false;
                    ContinueWaiter.signal();
                    if (e instanceof JposException)
                        throw (JposException) e;
                }
            }
            super.flush();
        }
    }
}
