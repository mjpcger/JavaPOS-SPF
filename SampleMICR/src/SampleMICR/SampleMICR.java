/*
Copyright 2018 Martin Conrad
<p>
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
<p>
http://www.apache.org/licenses/LICENSE-2.0
<p>
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package SampleMICR;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.micr.*;
import jpos.JposConst;
import jpos.JposException;
import jpos.MICRConst;
import jpos.config.JposEntry;
import org.apache.log4j.Level;

/**
 * Implementation of a JposDevice based implementation of a MICR driver that becomes
 * a JavaPOS MICR service in combination with the JposMICR class
 */
public class SampleMICR extends JposDevice implements Runnable {
    /**
     * IO processor to be used for communication with scale.
     */
    UniqueIOProcessor Target = null;

    private int OwnPort = 0;
    private int CharacterTimeout = 20;
    private int MinClaimTimeout = 200;
    private int PollDelay = 500;
    private String SubstituteCharacters = "tao-";

    private boolean ToBeFinished;
    private int Offline = JposConst.JPOS_SUE_POWER_OFF_OFFLINE;
    private Thread CommandProcessor;
    private SyncObject PollWaiter = new SyncObject();

    /**
     * Constructor. id specifies either the COM port to be used or the server to be connected in format host:port
     *
     * @param id COM port or IP target address and port
     */
    protected SampleMICR(String id) {
        super(id);
        mICRInit(1);
        PhysicalDeviceDescription = "MICR simulator";
        PhysicalDeviceName = "MICR Simulator";
        CapPowerReporting = JposConst.JPOS_PR_STANDARD;
    }

    @Override
    public void checkProperties(JposEntry entry) throws JposException {
        super.checkProperties(entry);
        try {
            Object o;
            if ((o = entry.getPropertyValue("OwnPort")) != null) {
                if ((OwnPort = Integer.parseInt(o.toString())) < 0 || OwnPort >= 0xffff)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid source port.");
            }
            if ((o = entry.getPropertyValue("CharacterTimeout")) != null) {
                if ((CharacterTimeout = Integer.parseInt(o.toString())) <= 0)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid character timeout: " + CharacterTimeout);
            }
            if ((o = entry.getPropertyValue("MinClaimTimeout")) != null) {
                if ((MinClaimTimeout = Integer.parseInt(o.toString())) < 0)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid minimum claim timeout: " + MinClaimTimeout);
            }
            if ((o = entry.getPropertyValue("PollDelay")) != null) {
                if ((PollDelay = Integer.parseInt(o.toString())) <= 0)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid poll delay: " + PollDelay);
            }
            if ((o = entry.getPropertyValue("SubstituteCharacters")) != null) {
                if ((SubstituteCharacters = o.toString()).length() != 4)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid substitute characters: " + SubstituteCharacters);
            }
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property", e);
        }
    }

    @Override
    public void changeDefaults(MICRProperties props) {
        props.CapValidationDevice = false;
        props.DeviceServiceDescription = "MICR service for smple MICR simulator";
        CapPowerReporting = JposConst.JPOS_PR_STANDARD;
        props.DeviceServiceVersion = 1014001;
    }

    /**
     * Port initialization.
     *
     * @return In case of initialization error, the exception. Otherwise null.
     */
    private JposException initPort() {
        try {
            ((TcpClientIOProcessor) (Target = new TcpClientIOProcessor(this, ID))).setParam(OwnPort, PollDelay);
            Target.open(Offline == JposConst.JPOS_SUE_POWER_OFF_OFFLINE);
            Offline = JposConst.JPOS_SUE_POWER_ONLINE;
        } catch (JposException e) {
            Target = null;
            return e;
        }
        return null;
    }

    /**
     * Closes the port
     *
     * @return In case of an IO error, the corresponding exception. Otherwise null
     */
    private JposException closePort() {
        JposException e = null;
        if (Target != null) {
            for (int i = 0; i < 2; i++) {
                try {
                    Target.close();
                } catch (JposException ee) {
                    e = ee;
                }
            }
            Target = null;
        }
        return e;
    }

    /**
     * Sends a command to MICR device. The sample device does not generate a response.
     *
     * @param command Command to be sent. Must be "I" (Insert cheque) or "R" (Release cheque )
     * @return true on success, false if I/O error occurred.
     */
    synchronized private boolean sendCommand(String command) {
        if (connectionOffline())
            return false;
        try {
            byte[] request = command.getBytes();
            Target.write(request);
            return true;
        } catch (JposException e) {
            log(Level.TRACE, ID + ": IO error: " + e.getMessage());
        }
        Offline = JposConst.JPOS_SUE_POWER_OFF_OFFLINE;
        closePort();
        return false;
    }

    private boolean connectionOffline() {
        if (Target == null) {
            JposException e = initPort();
            if (e != null) {
                Offline = JposConst.JPOS_SUE_POWER_OFF_OFFLINE;
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves data from MICR device. Waits PollTimeout msec for data, if data are present, CharacterTimeout msec for
     * next character. '\n' is the frame delimiter, if received, all previously received characters will be returned.
     *
     * @return MICR data on success, empty string in case of a timeout condition, null in case of I/O error.
     */
    private String recvData() {
        UniqueIOProcessor proc;
        String resp = "";
        synchronized (this) {
            if (connectionOffline())
                return null;
            proc = Target;
        }
        for (int timeout = PollDelay; ; timeout = CharacterTimeout) {
            proc.setTimeout(timeout);
            try {
                byte[] part = proc.read(1);
                if (part.length == 0)
                    return "";
                if (part[0] == '\n')
                    return resp;
                resp = resp + new String(part);
            } catch (JposException e) {
                Offline = JposConst.JPOS_SUE_POWER_OFF_OFFLINE;
                closePort();
                return null;
            }
        }
    }

    /**
     * Thread main, used for MICR data retrieval.
     */
    @Override
    public void run() {
        MICRProperties props = (MICRProperties)getClaimingInstance(ClaimedMICR, 0);
        while (!ToBeFinished) {
            String data = recvData();
            if (props.DeviceEnabled) {
                if ((props.PowerState == JposConst.JPOS_PS_ONLINE) != (Offline == JposConst.JPOS_SUE_POWER_ONLINE)) {
                    try {
                        handleEvent(new JposStatusUpdateEvent(props.EventSource, Offline));
                    } catch (JposException e) {
                        e.printStackTrace();
                    }
                }
                if (data != null && !data.equals("")) {
                    sendEvent(data, props.EventSource);
                }
            }
            if (data == null)
                PollWaiter.suspend(PollDelay);
        }
    }

    private String[] strip(String[] parts) {
        String[] res;
        int start;
        for (start = 0; start < parts.length && parts[start].length() == 0; start++)
            ;
        res = new String[parts.length - start];
        for (int i = parts.length - 1; i >= start; i--)
            res[i - start] = parts[i];
        return res;
    }

    private void sendEvent(String data, JposBase service) {
        data = data.replace('<', SubstituteCharacters.charAt(0)).replace(';', SubstituteCharacters.charAt(2));
        String[] parts = strip(data.split(SubstituteCharacters.substring(0, 1)));
        String[][] subpart = new String[parts.length][];
        String[][][] value = new String[parts.length][][];
        for (int i = 0; i < parts.length; i++) {
            subpart[i] = strip(parts[i].split(SubstituteCharacters.substring(2, 3)));
            value[i] = new String[subpart[i].length][];
            for (int j = 0; j < subpart[i].length; j++) {
                value[i][j] = strip(subpart[i][j].split(" "));
            }
        }
        Data[] props = new Data[1];
        boolean error;
        error = getJposMICRData(data, parts, subpart, value, props);
        try {
            if (error) {
                if (props[0] == null)
                    handleEvent(new MICRErrorEvent(service, JposConst.JPOS_E_EXTENDED, MICRConst.JPOS_EMICR_NODATA, new Data("", "", "", MICRConst.MICR_CT_UNKNOWN, MICRConst.MICR_CC_UNKNOWN, "", "", "", "")));
                else
                    handleEvent(new MICRErrorEvent(service, JposConst.JPOS_E_EXTENDED, MICRConst.JPOS_EMICR_BADSIZE, props[0]));
            }
            else
                handleEvent(new MICRDataEvent(service, 0, props[0]));
        } catch (JposException e) {
            e.printStackTrace();
        }
    }

    private boolean getJposMICRData(String data, String[] parts, String[][] subpart, String[][][] value, Data[] props) {
        boolean error;
        String serial, account, transit, bank;
        if (error = (parts.length != 2 || subpart[0].length != 1 || subpart[1].length != 2)) {
            props[0] = null;
        } else {
            if (error = (value[0][0].length == 0)) {
                transit = bank = "";
            }
            else {
                transit = subpart[0][0];
                bank =  (error = (transit.length() < 8)) ? "" : transit.substring(4, 8);
            }
            if (value[1][0].length == 1) {
                account = value[1][0][0];
            }
            else {
                account = "";
                error = true;
            }
            if (value[1][1].length == 1) {
                serial = value[1][1][0];
            }
            else {
                serial = "";
                error = true;
            }
            props[0] = new Data(account,"", bank, MICRConst.MICR_CT_UNKNOWN, MICRConst.MICR_CC_UNKNOWN, "", data, serial, transit);
        }
        return error;
    }

    @Override
    public MICRProperties getMICRProperties(int index) {
        return new SampleMICRAccessor(index);
    }
    /**
     * The sample MICR accessor implementation class.
     */
    public class SampleMICRAccessor extends MICRProperties {
        private SampleMICRAccessor(int index) {
            super(index);
        }

        @Override
        public void claim(int timeout) throws JposException {
            JposException e = initPort();
            if (e != null)
                throw e;
            super.claim(timeout);
            ToBeFinished = false;
            (CommandProcessor = new Thread(SampleMICR.this)).start();
        }

        @Override
        public void release() throws JposException {
            ToBeFinished = true;
            synchronized (SampleMICR.this) {
                if (Target != null)
                    closePort();
                else
                    PollWaiter.signal();
            }
            while (true) {
                try {
                    CommandProcessor.join();
                    break;
                } catch (Exception e) {}
            }
            super.release();
        }

        @Override
        public void deviceEnabled(boolean enable) throws JposException {
            super.deviceEnabled(enable);
            if (enable) {
                handleEvent(new JposStatusUpdateEvent(EventSource, Offline));
            }
            else if (PowerNotify == JposConst.JPOS_PN_ENABLED){
                PowerState = JposConst.JPOS_PS_UNKNOWN;
                EventSource.logSet("PowerState");
            }
        }

        @Override
        public void checkHealth(int level) throws JposException {
            if (level == JposConst.JPOS_CH_INTERNAL) {
                CheckHealthText = "Internal CheckHealth: ";
                CheckHealthText += Offline == JposConst.JPOS_SUE_POWER_OFF_OFFLINE ? "Failed" : "OK";
            }
            else {
                CheckHealthText = level == JposConst.JPOS_CH_EXTERNAL ? "External" : "Interactive";
                CheckHealthText += " Checkhealth: Failed (Not supported)";
            }
            EventSource.logSet("CheckHealthText");
            super.checkHealth(level);
        }

        @Override
        public void beginInsertion(int timeout) throws JposException {
            long starttime = System.currentTimeMillis();
            do {
                if (!connectionOffline()) {
                    return;
                }
                new SyncObject().suspend(PollDelay);
            } while (timeout == JposConst.JPOS_FOREVER || System.currentTimeMillis() - starttime < timeout);
            check(true, JposConst.JPOS_E_FAILURE, "No connection to MICR device");
        }

        @Override
        public void endInsertion() throws JposException {
            check(!sendCommand("I"), JposConst.JPOS_E_FAILURE, "No connection to MICR device");
        }

        @Override
        public void beginRemoval(int timeout) throws JposException {
            long starttime = System.currentTimeMillis();
            do {
                if (sendCommand("R"))
                    return;
                new SyncObject().suspend(PollDelay);
            } while (System.currentTimeMillis() - starttime < timeout);
            check(true, JposConst.JPOS_E_FAILURE, "No connection to MICR device");
        }

        @Override
        public void directIO(int command, int[] data, Object obj) throws JposException {
            if (command == 1) {
                EventSource.setDataEventEnabled(true);
            }
        }
    }
}
