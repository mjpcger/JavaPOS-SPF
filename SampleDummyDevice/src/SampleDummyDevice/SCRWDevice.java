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

package SampleDummyDevice;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.smartcardrw.*;
import jpos.*;
import jpos.config.*;

import javax.swing.*;
import java.util.*;

/**
 * JposDevice based dummy implementation for JavaPOS SmartCardRW device service implementation.
 * No real hardware. All read data with dummy values, operator interaction via OptionDialog boxes.<br>
 * Supported configuration values for SmartCardRW in jpos.xml can be used to set the corresponding property values:
 * <ul>
 *     <li>CapCardErrorDetection: Can be TRUE or FALSE. Default is FALSE.</li>
 *     <li>CapInterfaceMode: Can be set to "SC_CMODE_TRANS", "SC_CMODE_BLOCK", "SC_CMODE_APDU" or "SC_CMODE_XML".</li>
 *     <li>CapIsoEmvMode: Can be set to "SC_CMODE_ISO" or "SC_CMODE_EMV".</li>
 *     <li>CapSCPresentSensor: Can be set to any hexadecimal value between 0 and FFFFFFFF.</li>
 *     <li>CapSCSlots: Can be set to any hexadecimal value between 0 and FFFFFFFF.</li>
 *     <li>TransmissionProtocol: Can be set to "SC_CTRANS_PROTOCOL_T0" or "SC_CTRANS_PROTOCOL_T1".</li>
 * </ul>
 * In addition, the following values in jpos.xml can be used to setup specific behavior:
 * <ul>
 *     <li>CardReadyDelay: Maximum time between card insertion and card being ready. Default: 1000 (one second), </li>
 *     <li>InTransactionDelay: Maximum time between last read or write until transaction becomes completed. Default: 1000 (one second).</li>
 * </ul>
 */
public class SCRWDevice extends JposDevice implements Runnable {
    private int CardReadyDelay = 1000;
    private int InTransactionDelay = 1000;

    protected SCRWDevice(String id) {
        super(id);
        smartCardRWInit(1);
        PhysicalDeviceDescription = "Dummy SmardCardRW simulator";
        PhysicalDeviceName = "Dummy SmardCardRW Simulator";
        CapPowerReporting = JposConst.JPOS_PR_NONE;
    }

    private String[][] Capabilities = {
            {"CapCardErrorDetection", "0", "TRUE", "FALSE"},
            {"CapInterfaceMode", "1", "SC_CMODE_TRANS", "SC_CMODE_BLOCK", "SC_CMODE_APDU", "SC_CMODE_XML"},
            {"CapIsoEmvMode", "1", "SC_CMODE_ISO", "SC_CMODE_EMV"},
            {"CapSCPresentSensor", "2"},
            {"CapSCSlots", "2"},
            {"TransmissionProtocol", "1", "SC_CTRANS_PROTOCOL_T0", "SC_CTRANS_PROTOCOL_T1"}
    };

    private Map<String, String[]> LastEntries = new HashMap<>();

    @Override
    public void checkProperties(JposEntry entry) throws JposException {
        super.checkProperties(entry);
        Object o = entry.getPropertyValue("InTransactionDelay");
        int val;
        if (o != null && (val = Integer.parseInt(o.toString())) >= 0 )
            InTransactionDelay = val;
        if ((o = entry.getPropertyValue("CardReadyDelay")) != null && (val = Integer.parseInt(o.toString())) >= 0)
            CardReadyDelay = val;
        for (String[] capa : Capabilities) {
            o = entry.getPropertyValue(capa[0]);
            if (o != null) {
                if (capa[1].equals("2")) {
                    long lval = Long.parseLong(o.toString(), 16);
                    check (lval < 0 || lval > 0xffffffffl, JposConst.JPOS_E_ILLEGAL, "Invalid value for property " + capa[0] + ": " + o.toString());
                    o = String.valueOf((int)lval);
                } else {
                    int j;
                    for (j = 2; j < capa.length; j++) {
                        if (capa[j].equals(o.toString().toUpperCase()))
                            break;
                    }
                    check(j == capa.length, JposConst.JPOS_E_ILLEGAL, "Invalid value for property " + capa[0] + ": " + o.toString());
                }
                LastEntries.put(capa[0], new String[]{ capa[1], o.toString()});
            }
        }
    }

    @Override
    public void changeDefaults(SmartCardRWProperties props) {
        props.AsyncMode = true;
        props.CapTransmissionProtocol = SmartCardRWConst.SC_CTRANS_PROTOCOL_T0;
        props.TransmissionProtocol = SmartCardRWConst.SC_TRANS_PROTOCOL_T0;
        for (String capa : LastEntries.keySet()) {
            try {
                String[] attr = LastEntries.get(capa);
                Object value = null;
                if (attr[0].equals("0"))
                    value = attr[1].toUpperCase().equals("TRUE");
                else if (attr[0].equals("2"))
                    value = (int) Long.parseLong(attr[1]);
                else
                    value = JposConst.class.getField(attr[1]).get(null);
                props.getClass().getField(capa).set(props, value);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public SmartCardRWProperties getSmartCardRWProperties(int index) {
        return new SampleProperties(index);
    }

    private Thread TheRunner;
    private enum Status { idle, gotCard, cardReadable, cardAborted, cardRemovable };
    private Status ReaderState = Status.idle;
    private SyncObject WaitCardInserted = null, WaitCardRemoved = null;
    private boolean CardInserted = false;
    private long LastActionTime;
    private boolean ToBeFinished = false;
    SynchronizedMessageBox TheBox;

    @Override
    public void run() {
        String title = "Dummy SmardCardRW Simulator";
        String message;
        String[] options;
        while (SmartCardRWs[0].size() > 0 && !ToBeFinished) {
            SmartCardRWProperties props;
            TheBox = new SynchronizedMessageBox();
            switch (ReaderState) {
                case idle: {
                    message = "A card must be inserted. Press 'Card Inserted' when ready.";
                    options = new String[]{"Card Inserted"};
                    TheBox.synchronizedConfirmationBox(message,title, options, options[0], JOptionPane.INFORMATION_MESSAGE, JposConst.JPOS_FOREVER);
                    synchronized (ReaderState) {
                        ReaderState = Status.gotCard;
                        if (WaitCardInserted != null) {
                            WaitCardInserted.signal();
                            WaitCardInserted = null;
                        }
                        props = getProperties();
                    }
                    if (props != null) {
                        try {
                            handleEvent(new SmartCardRWStatusUpdateEvent(props.EventSource, SmartCardRWConst.SC_SUE_CARD_PRESENT));
                        } catch (JposException ignore) {}
                    }
                    continue;
                }
                case gotCard: {
                    message = "The card you inserted must become readable. This will happen in " + CardReadyDelay + "milliseconds, but can be changed via option button.";
                    options = new String[]{"Ready for reading", "Card not readable"};
                    TheBox.synchronizedConfirmationBox(message, title, options, options[1], JOptionPane.INFORMATION_MESSAGE, CardReadyDelay);
                    JposErrorEvent errev = null;
                    JposDataEvent dataev = null;
                    synchronized (ReaderState) {
                        props = getProperties();
                        if (TheBox.Result != 0) {
                            ReaderState =  Status.cardRemovable;
                            if (props.CapCardErrorDetection)
                                errev = new JposErrorEvent(props.EventSource, JposConst.JPOS_E_EXTENDED, SmartCardRWConst.JPOS_ESC_TORN, JposConst.JPOS_EL_INPUT, "Card unexpectedly removed");
                            else
                                errev = new JposErrorEvent(props.EventSource, JposConst.JPOS_E_FAILURE, 0, JposConst.JPOS_EL_INPUT, "Card unexpectedly removed");
                        } else {
                            ReaderState =  Status.cardReadable;
                            LastActionTime = System.currentTimeMillis();
                            dataev = new JposDataEvent(props.EventSource, 0);
                            props.TransactionInProgress = true;
                        }
                    }
                    try {
                        if (errev != null)
                            handleEvent(errev);
                        else if (dataev != null)
                            handleEvent(dataev);
                    } catch (JposException ignore) {}
                    continue;
                }
                case cardReadable: {
                    message = "The card is now readable and writeable....";
                    options = new String[]{"Finish Operation", "AbortOperation"};
                    int timeout = (int)(CardReadyDelay - (System.currentTimeMillis() - LastActionTime));
                    TheBox.synchronizedConfirmationBox(message, title, options, options[1], JOptionPane.INFORMATION_MESSAGE, timeout <= 0 ? 1 : timeout);
                    if (System.currentTimeMillis() - LastActionTime < CardReadyDelay && (TheBox.Result < 0 || TheBox.Result >= options.length))
                        continue;
                    JposStatusUpdateEvent suev = null;
                    synchronized (ReaderState) {
                        (props = getProperties()).TransactionInProgress = false;
                        if(TheBox.Result == 0) {
                            ReaderState =  Status.cardRemovable;
                        } else {
                            ReaderState = Status.idle;
                            suev = new SmartCardRWStatusUpdateEvent(props.EventSource, SmartCardRWConst.SC_SUE_NO_CARD);
                        }
                    }
                    if (suev != null) {
                        try {
                            handleEvent(suev);
                        } catch (JposException ignore) {}
                    }
                    continue;
                }
                case cardAborted:
                case cardRemovable: {
                    message = "Operation has been finished or aborted. Press 'Card Removed' to continue.";
                    options = new String[]{"Card Removed"};
                    TheBox.synchronizedConfirmationBox(message,title, options, options[0], JOptionPane.INFORMATION_MESSAGE, JposConst.JPOS_FOREVER);
                    synchronized (ReaderState) {
                        ReaderState = Status.idle;
                        if (WaitCardRemoved != null) {
                            WaitCardRemoved.signal();
                            WaitCardRemoved = null;
                        }
                        props = getProperties();
                    }
                    if (props != null) {
                        try {
                            handleEvent(new SmartCardRWStatusUpdateEvent(props.EventSource, SmartCardRWConst.SC_SUE_NO_CARD));
                        } catch (JposException ignore) {}
                    }
                    continue;
                }
            }
        }
    }

    private SmartCardRWProperties getProperties() {
        SmartCardRWProperties props;
        props = ClaimedSmartCardRW[0];
        if (props != null && !props.DeviceEnabled)
            props = null;
        return props;
    }

    private class SampleProperties extends SmartCardRWProperties {
        protected SampleProperties(int dev) {
            super(dev);
        }

        @Override
        synchronized public void initOnEnable(boolean enable) {
            super.initOnEnable(enable);
            if (TheRunner == null) {
                TheRunner = new Thread(SCRWDevice.this);
                TheRunner.setName("SCRWDeviceRunner");
                TheRunner.start();
            }
        }

        @Override
        public void deviceEnabled(boolean enable) throws JposException {
            super.deviceEnabled(enable);
            if (enable) {
                synchronized(ReaderState) {
                    handleEvent(new SmartCardRWStatusUpdateEvent(EventSource,
                            ReaderState == Status.idle ? SmartCardRWConst.SC_SUE_NO_CARD : SmartCardRWConst.SC_SUE_CARD_PRESENT));
                }
            } else {
                CardInserted = false;
            }
        }

        @Override
        synchronized public void close() throws JposException {
            if (SmartCardRWs[0].size() == 1) {
                ToBeFinished = true;
                SynchronizedMessageBox box = TheBox;
                if (box != null)
                    box.abortDialog();
            }
        }

        @Override
        public void beginInsertion(int timeout) throws JposException {
            if(!CardInserted) {
                SyncObject waiter = null;
                synchronized (ReaderState) {
                    if (ReaderState == Status.idle) {
                        waiter = WaitCardInserted = new SyncObject();
                    }
                }
                if (waiter != null)
                    waiter.suspend(timeout == JposConst.JPOS_FOREVER ? SyncObject.INFINITE : timeout);
                synchronized (ReaderState) {
                    check(ReaderState == Status.idle && waiter != null, JposConst.JPOS_E_TIMEOUT, "Card still not present");
                }
            }
        }

        @Override
        public void endInsertion() throws JposException {
            if (!CardInserted) {
                JposDataEvent dataev = null;
                JposErrorEvent errev = null;
                synchronized (ReaderState) {
                    if (ReaderState == Status.cardReadable)
                        dataev = new JposDataEvent(EventSource, 0);
                    else if (ReaderState != Status.idle) {
                        if (CapCardErrorDetection)
                            errev = new JposErrorEvent(EventSource, JposConst.JPOS_E_EXTENDED, SmartCardRWConst.JPOS_ESC_TORN, JposConst.JPOS_EL_INPUT, "Unexpected card removal");
                        else
                            errev = new JposErrorEvent(EventSource, JposConst.JPOS_E_FAILURE, 0, JposConst.JPOS_EL_INPUT, "Unexpected card error");
                    } else
                        throw new JposException(JposConst.JPOS_E_FAILURE, "Card not present");
                    CardInserted = true;
                }
                if (errev != null)
                    handleEvent(errev);
                else if (dataev != null)
                    handleEvent(dataev);
            }
        }

        @Override
        public void beginRemoval(int timeout) throws JposException {
            if(CardInserted) {
                SyncObject waiter = null;
                synchronized (ReaderState) {
                    if (ReaderState != Status.idle) {
                        waiter = WaitCardRemoved = new SyncObject();
                    }
                }
                if (waiter != null)
                    waiter.suspend(timeout == JposConst.JPOS_FOREVER ? SyncObject.INFINITE : timeout);
                synchronized (ReaderState) {
                    check(ReaderState != Status.idle && waiter != null, JposConst.JPOS_E_TIMEOUT, "Card still present");
                }
            }
        }

        @Override
        public void endRemoval() throws JposException {
            if (CardInserted) {
                synchronized (ReaderState) {
                    if (ReaderState != Status.idle)
                        throw new JposException(JposConst.JPOS_E_FAILURE, "Card still present");
                    CardInserted = false;
                }
            }
        }

        @Override
        public WriteData writeData(int action, int count, String data) throws JposException {
            synchronized (ReaderState) {
                check(ReaderState != Status.cardReadable, JposConst.JPOS_E_FAILURE, "Card not processable");
                byte[] bytes = data.getBytes();
                check(bytes.length < count, JposConst.JPOS_E_ILLEGAL, "Too few characters given: " + data);
                String cmd = new String(bytes, 0, count);
                for (int i = 0; i < cmd.length(); i++) {
                    check(cmd.charAt(i) != data.charAt(i), JposConst.JPOS_E_ILLEGAL, "Invalid command format: " + cmd);
                }
            }
            return super.writeData(action, count, data);
        }

        @Override
        public void writeData(WriteData request) throws JposException {
            super.writeData(request);
            LastActionTime = System.currentTimeMillis();
        }

        @Override
        public void readData(int action, int[] count, String[] data) throws JposException {
            synchronized (ReaderState) {
                check(ReaderState != Status.cardRemovable, JposConst.JPOS_E_FAILURE, "Card not processable");
                data[0] = String.valueOf(LastActionTime = System.currentTimeMillis());
                count[0] = data[0].length();
            }
        }
    }
}
