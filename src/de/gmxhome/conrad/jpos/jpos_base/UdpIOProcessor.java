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

import jpos.JposConst;
import jpos.JposException;
import org.apache.log4j.Level;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
public class UdpIOProcessor extends UniqueIOProcessor implements Runnable {
    private int TargetPort = 0;
    private InetAddress TargetIP = null;
    private int SourcePort = 0;
    private InetAddress SourceIP = null;
    private int OwnPort = 0;
    private DatagramChannel Socket = null;
    private int MaxDataSize = 0;

    private Thread TheReader = null;
    private SyncObject ReadWaiter = null;
    private SyncObject ContinueWaiter = null;
    private List<Object> InputData = new ArrayList<Object>();
    private boolean HighWaterWaiting = false;

    private static class Frame {
        byte[] Data;
        InetSocketAddress Source;

        Frame(byte[]data, SocketAddress source) {
            Data = data;
            Source = (InetSocketAddress) source;
        }
    }
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
        try {
            SourceIP = TargetIP = InetAddress.getByName("0.0.0.0");
            SourcePort = TargetPort = 0;
            if (ownport < 0 || ownport > 0xffff)
                throw new JposException(JposConst.JPOS_E_ILLEGAL, IOProcessorError, "Invalid port: " + ownport);
            OwnPort = ownport;
        } catch (Exception e) {
            logerror("UdpIOProcessor", JposConst.JPOS_E_FAILURE, e);
        }
    }

    @Deprecated
    public UdpIOProcessor(JposDevice device, String addr) throws JposException {
        super(device, "0.0.0.0:0");
        try {
            SourceIP = InetAddress.getByName("0.0.0.0");
            SourcePort = 0;
        } catch (Exception e) {
            logerror("UdpIOProcessor", JposConst.JPOS_E_FAILURE, e);
        }
        setTarget(addr);
    }

    /**
     * Sets UDP specific communication parameter.
     * @param ownport           Own port address
     * @param maxdatasize       Maximum size of a frame. May be set to Integer.MAX_VALUE to use the limits given by
     *                          network configuration and hardware.
     * @throws JposException    If port address &lt; 0 or port address &gt; 65535
     */
    @Deprecated
    public void setParameters(int ownport, int maxdatasize) throws JposException {
        if (ownport < 0 || ownport > 0xffff)
            logerror("SetParam", JposConst.JPOS_E_ILLEGAL, "Invalid port: " + ownport + ", must be between 0 and 65535");
        OwnPort = ownport;
        MaxDataSize = maxdatasize;
    }

    @Override
    public int setTimeout(int timeout) {
        return Timeout = timeout > 0 ? timeout : (timeout == 0 ? 1 : Integer.MAX_VALUE);
    }

    @Override
    public String setTarget(String addr) throws JposException {
        String[] splitaddr = addr.split(":");
        if (splitaddr.length != 2)
            logerror("SetTarget", JposConst.JPOS_E_ILLEGAL, addr +" invalid: Format must be ip:port");
        int port;
        InetAddress address;
        try {
            if ((port = Integer.parseInt(splitaddr[1])) <= 0 || port > 0xffff)
                throw new JposException(JposConst.JPOS_E_ILLEGAL, IOProcessorError, splitaddr[1] + " invalid: Must be between 1 and 65535");
            address = InetAddress.getByName(splitaddr[0]);
            if (((Socket != null && Socket.socket().getLocalPort() == port) || port == OwnPort) &&
                    (address.equals(InetAddress.getByName("localhost"))))
                throw new JposException(JposConst.JPOS_E_ILLEGAL, 0, "Target cannot be own address");
            TargetIP = address;
            TargetPort = port;
            InitialPort = getTarget();
        } catch (Exception e) {
            logerror("SetTarget", JposConst.JPOS_E_FAILURE, e);
        }
        return TargetIP.toString() + ":" + TargetPort;
    }

    @Override
    public String getTarget() {
        return TargetIP.toString() + ":" + TargetPort;
    }

    @Override
    public String getSource() {
        return SourceIP.toString() + ":" + SourcePort;
    }

    @Override
    synchronized public void open(boolean noErrorLog) throws JposException {
        if (Socket != null) {
            Dev.log(Level.ERROR, LoggingPrefix + "Open error: Datagram socket just opened");
            throw new JposException(JposConst.JPOS_E_ILLEGAL, IOProcessorError, "Datagram socket just opened");
        }
        try {
            Socket = DatagramChannel.open();
            if (OwnPort != 0)
                Socket.socket().bind(new InetSocketAddress(OwnPort));
            else
                Socket.socket().bind(null);
            ReadWaiter = new SyncObject();
            ContinueWaiter = new SyncObject();
            InputData.clear();
            HighWaterWaiting = false;
            (TheReader = new Thread(this, "localhost:" + Socket.socket().getLocalPort() + " Reader")).start();
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
        if (buffer.length > MaxDataSize)
            logerror("Write", JposConst.JPOS_E_FAILURE, "Buffer too long: " + buffer.length);
        synchronized (WriteSynchronizer) {
            if (Socket == null)
                logerror("Write", JposConst.JPOS_E_ILLEGAL, "Socket not opened");
            try {
                if (Socket.socket().getReceiveBufferSize() < buffer.length)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Message too long: " + buffer.length);
                Socket.send(ByteBuffer.wrap(buffer), new InetSocketAddress(TargetIP, TargetPort));
            } catch (Exception e) {
                logerror("Write", JposConst.JPOS_E_FAILURE, e);
            }
            return super.write(buffer);
        }
    }

    private Frame read(int len, int timeout) throws JposException {
        if (ReadWaiter.suspend(timeout)) {
            Object data = InputData.get(0);
            InputData.remove(0);
            if (data instanceof JposException) {
                ContinueWaiter.signal();
                throw (JposException) data;
            } else {
                byte[] frame = ((Frame) data).Data;
                SourceIP = ((Frame) data).Source.getAddress();
                SourcePort = ((Frame) data).Source.getPort();
                if (HighWaterWaiting && InputData.size() < 500) {
                    HighWaterWaiting = false;
                    ContinueWaiter.signal();
                }
                return (Frame)data;
            }
        } else {
            return null;
        }
    }

    @Override
    public void run() {
        while (Socket != null) {
            Object readobj = null;
            SocketAddress address = null;
            boolean highwater = false;
            try {
                ByteBuffer data = ByteBuffer.wrap(new byte[Socket.socket().getReceiveBufferSize()]);
                if ((address = Socket.receive(data)) == null)
                    continue;
                readobj = new Frame(Arrays.copyOf(data.array(), data.position()), address);
            } catch (Exception e) {
                readobj = new JposException(JposConst.JPOS_E_FAILURE, e.getClass().getSimpleName() + ": " + e.getMessage(), e);
            }
            synchronized (ReadSynchronizer) {
                InputData.add(readobj);
                if (readobj instanceof Frame && InputData.size() >= 1000)
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
                    count += ((Frame) data).Data.length;
                }
            }
            LoggingData = String.valueOf(count).getBytes();
            return super.available();
        }
    }

    @Override
    public byte[] read(int count) throws JposException {
        synchronized(ReadSynchronizer) {
            if (count > MaxDataSize)
                count = MaxDataSize;
            Frame data = read(Integer.MAX_VALUE, Timeout);
            byte[] discardedData = null;
            if (data == null)
                LoggingData = new byte[0];
            else {
                if (data.Data.length <= count) {
                    LoggingData = data.Data;
                } else {
                    LoggingData = Arrays.copyOf(data.Data, count);
                    discardedData = Arrays.copyOfRange(data.Data, count, data.Data.length);
                }
                SourceIP = data.Source.getAddress();
                SourcePort = data.Source.getPort();
            }
            super.read(count);
            if (discardedData != null) {
                Dev.log(Level.TRACE, LoggingPrefix + "Discarded data (" + discardedData.length + ") bytes" + location(true) + ": " + toLogString(discardedData));
            }
            return LoggingData;
        }
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
