/*
 * Copyright 2024 Martin Conrad
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

import jpos.JposException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;

import static de.gmxhome.conrad.jpos.jpos_base.SyncObject.INFINITE;
import static jpos.JposConst.JPOS_E_FAILURE;
import static jpos.JposConst.JPOS_E_ILLEGAL;
import static net.bplaced.conrad.log4jpos.Level.ERROR;

public abstract class UdpBaseIOProcessor extends UniqueIOProcessor implements Runnable {
    /**
     * Stores JposDevice and port of derived IO processors. The device will
     * be used for logging while the port specifies the communication object.
     *
     * @param dev  Device that uses the processor. Processor uses logging of device
     *             to produce logging entries
     * @param port Communication object, e.g. COM3 or 127.0.0.1:23456
     * @throws JposException If port does not specify a valid communication object
     */
    public UdpBaseIOProcessor(JposDevice dev, String port) throws JposException {
        super(dev, port);
    }

    /**
     * Source socket address for last received frame.
     */
    protected InetSocketAddress SourceIP = null;

    /**
     * Target socket address for next frame to be sent.
     */
    protected InetSocketAddress TargetIP = null;

    /**
     * Own socket port, if 0, the system will select a free port automatically.
     */
    protected int OwnPort = 0;

    /**
     * The DatagramChannel used for communication, will be set in method open() and released in close().
     */
    protected DatagramChannel Socket = null;

    private Thread TheReader = null;
    private SyncObject ReadWaiter = null;
    private SyncObject ContinueWaiter = null;
    private final List<Object> InputData = new ArrayList<>();
    private boolean HighWaterWaiting = false;

    /**
     * Internal class, encapsulating a received frame with source address.
     */
    protected static class Frame {
        /**
         * Frame received from source
         */
        byte[] Data;

        /**
         * Source socket address.
         */
        InetSocketAddress Addr;

        /**
         * Constructor, initializes Data and Addr.
         * @param data   Frame data.
         * @param source Source address.
         */
        Frame(byte[]data, SocketAddress source) {
            Data = data;
            Addr = (InetSocketAddress) source;
        }
    }

    @Override
    public int setTimeout(int timeout) {
        return Timeout = timeout > 0 ? timeout : (timeout == 0 ? 1 : Integer.MAX_VALUE);
    }

    /**
     * String showing the socket state after method call open. Should be something like "connected" or "opened".
     */
    String OpenedText;

    /**
     * Performs further initialization after bind. For client sockets, this should be the connect, for server sockets,
     * the logging prefix should be set.
     * @return The name of the reader thread.
     * @throws IOException If an I/O error ocurred during further initialization (connect).
     */
    abstract String initAfterBind() throws IOException;

    @Override
    synchronized public void open(boolean noErrorLog) throws JposException {
        if (Socket != null) {
            Dev.log(ERROR, LoggingPrefix + "Open error: Datagram socket just " + OpenedText);
            throw new JposException(JPOS_E_ILLEGAL, IOProcessorError, "Datagram socket just " + OpenedText);
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
            (TheReader = new Thread(this, initAfterBind())).start();
        } catch (Exception e) {
            String message = e.getClass().getSimpleName() + ": " + e.getMessage();
            if (noErrorLog)
                throw new JposException(JPOS_E_ILLEGAL, IOProcessorError, message, e);
            logerror("Open", JPOS_E_ILLEGAL, e);
        }
    }

    @Override
    synchronized public void close() throws JposException {
        if (Socket == null) {
            Dev.log(ERROR, LoggingPrefix + "Close error: Socket just closed");
            throw new JposException(JPOS_E_ILLEGAL, IOProcessorError, "Socket just closed");
        }
        try {
            Socket.close();
        } catch (Exception e) {
            logerror("Close", JPOS_E_FAILURE, e);
        }
        Socket = null;
        LoggingPrefix = "";
        ContinueWaiter.signal();
        try {
            TheReader.join();
        } catch (InterruptedException ignored) {}
        TheReader = null;
        super.close();
    }

    /**
     * Encapsulated socket write or send method call
     * @param buffer    Buffer to be written.
     * @throws IOException If an error occurs.
     */
    abstract void send(ByteBuffer buffer) throws IOException;

    @Override
    public int write(byte[] buffer) throws JposException {
        synchronized (WriteSynchronizer) {
            if (Socket == null)
                logerror("Write", JPOS_E_ILLEGAL, "Socket not " + OpenedText);
            try {
                if (Socket.socket().getSendBufferSize() < buffer.length)
                    throw new JposException(JPOS_E_ILLEGAL, "Message too long: " + buffer.length);
                send(ByteBuffer.wrap(buffer));
            } catch (IOException e) {
                logerror("Write", JPOS_E_FAILURE, e);
            }
            return super.write(buffer);
        }
    }

    private final int[] FlushSync = {0};

    /**
     * Encapsulated socket read or receive call. Returns null if no frames are present. Otherwise, a Frame object with
     * frame data ans the source address, either from receive or TargetIP (if connected client socket).
     * @param data ByteBuffer to be used for reading.
     * @return Frame containing frame data and source address or null.
     * @throws IOException If an I/O error occurred during receive.
     */
    abstract Object getFrameOrException(ByteBuffer data) throws IOException;

    @Override
    public void run() {
        while (Socket != null) {
            Object readobj;
            boolean highwater = false;
            try {
                ByteBuffer data = ByteBuffer.wrap(new byte[Socket.socket().getReceiveBufferSize()]);
                readobj = getFrameOrException(data);
                if (readobj == null)
                    continue;
            } catch (Exception e) {
                readobj = new JposException(JPOS_E_FAILURE, e.getClass().getSimpleName() + ": " + e.getMessage(), e);
            }
            synchronized (FlushSync) {
                synchronized (ReadSynchronizer) {
                    InputData.add(readobj);
                    if (readobj instanceof Frame && InputData.size() >= 1000)
                        highwater = HighWaterWaiting = true;
                    ReadWaiter.signal();
                }
                if (readobj instanceof JposException || highwater)
                    ContinueWaiter.suspend(INFINITE);
            }
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
                LoggingData = ((Frame) data).Data;
                SourceIP = ((Frame) data).Addr;
            }
        } else
            LoggingData = new byte[0];
        return super.read(count);
    }

    @Override
    public void flush() throws JposException {
        boolean callSuper = true;
        while (true) {
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
                if (callSuper) {
                    super.flush();
                    callSuper = false;
                }
            }
            synchronized (FlushSync) {
                if (InputData.size() == 0) {
                    break;
                }
            }
        }
    }
}
