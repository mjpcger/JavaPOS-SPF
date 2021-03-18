/*
 * Copyright 2021 Martin Conrad
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

package SampleUdpDevice;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.biometrics.*;
import jpos.BiometricsConst;
import jpos.JposConst;
import jpos.JposException;
import jpos.config.JposEntry;

import javax.swing.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * JposDevice based implementation of a JavaPOS Biometrics device service implementation for the
 * sample device implemented in SampleUdpDevice.tcl.
 * <p>The simulator supports the following biometrics commands:
 *   <ul>
 *     <li> 'BIO:StartN': Starts input, where N specifies whether user name and password shall be entered (0) or only
 *     the password shall be re-entered (1). Sends back 'BIO:Start0' if Biometrics is just in input state, otherwise
 *     'BIO:Start1'.</li>
 *     <li> 'BIO:Check': Checks input state. Sends back 'BIO:Check0' if still in input and 'BIO:Check1' if input has
 *     been finished.</li>
 *     <li> 'BIO:Get': Retrieves input data. Sends back 'BIO:GetK', where K is a space separated list of decimal
 *     Unicode values representing user name and password. 0 will be the delimiter between user name and password. An
 *     exampe: If the user name is 'john' and the password 'doe', the simulator sends
 *     'BIO:Get106 111 104 110 0 100 111 101'.</li>
 *   </ul>
 * All frames have variable length. Communication will be made with UDP sockets.
 * <br> Multiple commands can be sent in a single frame, separated by comma (,). The corresponding responses will
 * be sent in one frame, separated by comma as well.<p>
 * Here a full list of all Biometrics device specific properties that can be changed via jpos.xml:
 * <ul>
 *     <li>BIRFormatIDOwner: The owner component of the BIR format ID in the range 0 - 65535. Default: 54321. Keep in
 *     mind that this is not a real format owner as provided by the International Biometric Industry Association.</li>
 *     <li>BIRFormatIDType: The type component of the BIR format ID in the range 0 - 65535. Default: 1.</li>
 *     <li>MaxEnrollmentEntries: The maximum number of password captures allowed for successful identification. Default:
 *     5. Identification is successful, if two successive password entries match.</li>
 *     <li>FRRUserOnly: The False Reject Rate if user matches but password is wrong. Default: 10.</li>
 *     <li>FARUserOnly: The False Accept Rate if user matches but password is wrong. Default: 90.</li>
 * </ul>
 * The format of the BDB (Biometric Data Block) used by this simulation is as follows:
 * <ul>
 *     <li>The BDB starts with the BDB length (4 byte), followed by</li>
 *     <li>Hash codes(12 byte) that cover the password, followed by</li>
 *     <li>length (4 byte) of the user name, followed by</li>
 *     <li>length (4 byte) of payload from application, followed by</li>
 *     <li>user name (UTF8 encoded, length as specified before), followed by</li>
 *     <li>application payload (length specified before).</li>
 * </ul>
 * Length fields within the BDB are in binary little-endian format.
 */
public class Biometrics extends Device {
    // General purpose objects

    private static final int BIRHeaderVersion = 1;
    private int BIRFormatIDOwner = 54321;
    private int BIRFormatIDType = 1;

    private boolean InProcessing = false;
    private boolean Enrollment;
    private byte[][] CapturePayload = {null};
    private byte[] LastCaptured = null;
    private boolean CapturesMatched = false;
    private int MaxEnrollmentEntries = 5;
    private int EnrollmentEntryCount;
    private int FRRUserOnly = 10;
    private int FARUserOnly = 90;

    protected Biometrics(String id) {
        super(id);
        biometricsInit(1);
        PhysicalDeviceDescription = "UDP device simulator, Biometrics part";
        PhysicalDeviceName = "UDP Biometrics";
        CapPowerReporting = JposConst.JPOS_PR_STANDARD;
        Toolsets = new CommonSubDeviceToolset[]{new BiometricsSubDeviceToolset()};
    }

    @Override
    public void checkProperties(JposEntry entry) throws JposException {
        super.checkProperties(entry);
        try {
            Object o;
            if ((o = entry.getPropertyValue("BIRFormatIDOwner")) != null) {
                if ((BIRFormatIDOwner = Integer.parseInt(o.toString())) < 0 || BIRFormatIDOwner >= 1 << Short.SIZE)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid BIR format ID owner: " + BIRFormatIDOwner);
            }
            if ((o = entry.getPropertyValue("BIRFormatIDType")) != null) {
                if ((BIRFormatIDType = Integer.parseInt(o.toString())) < 0 || BIRFormatIDType >= 1 << Short.SIZE)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid BIR format ID type: " + BIRFormatIDType);
            }
            if ((o = entry.getPropertyValue("MaxEnrollmentEntries")) != null) {
                if ((MaxEnrollmentEntries = Integer.parseInt(o.toString())) < 2)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid maximum enrollment entries: " + MaxEnrollmentEntries);
            }
            if ((o = entry.getPropertyValue("FRRUserOnly")) != null) {
                if ((FRRUserOnly = Integer.parseInt(o.toString())) < 0 || FRRUserOnly > 100)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid false reject rate for password mismatch: " + FRRUserOnly);
            }
            if ((o = entry.getPropertyValue("FARUserOnly")) != null) {
                if ((FARUserOnly = Integer.parseInt(o.toString())) < 0 || FARUserOnly > 100)
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid false accept rate for password mismatch: " + FARUserOnly);
            }
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property", e);
        }
    }

    @Override
    public void changeDefaults(BiometricsProperties props) {
        props.DeviceServiceDescription = "Biometrics service for sample UDP device simulator";
        props.DeviceServiceVersion = 1014001;
        props.AlgorithmList = "PasswordMatch";
        props.SensorBPP = 0;
        props.SensorHeight = 0;
        props.SensorWidth = 0;
        props.SensorTypeDef = BiometricsConst.BIO_ST_PASSWORD;
    }

    private class BiometricsSubDeviceToolset extends CommonSubDeviceToolset {
        @Override
        public int check(String[] command, String[] response) {
            int rc = super.check(command, response);
            if (0 == rc && command[0].equals("BIO")) {
                String[] validcommands = {
                        "Start",
                        "Cancel",
                        "Check",
                        "Get"
                };
                int prefix = checkValidCommand(command[1], response[1], validcommands);
                if (prefix != 0) {
                    response[1] = response[1].substring(prefix);
                    if (!command[1].substring(0, prefix).equals("Get")) {
                        rc =  response[1].matches("[01]") ? 1 : -1;
                    } else {
                        rc = 1;
                        int count = 0;
                        for (String code : response[1].split(" ")) {
                            int unicode;
                            try {
                                unicode = Integer.parseInt(code);
                            } catch (Exception e) {
                                unicode = -1;
                            }
                            if (unicode == 0 && count == 0)
                                count++;
                            else if (unicode == 0 || !Character.isDefined(unicode)) {
                                rc = -1;
                                break;
                            }
                        }
                    }
                } else
                    rc = -2;
            }
            return rc;
        }

        private boolean PreviousPowerState;
        private boolean OldInProcessing;
        private int FirstCommand;

        @Override
        public void saveCurrentStatusInformation(String[][] commands) {
            super.saveCurrentStatusInformation(null);
            OldInProcessing = InProcessing;
            prepareSignalStatusWaits(Biometricss[0]);
            commands[0] = Arrays.copyOf(commands[0], (FirstCommand = commands[0].length) + 1);
            commands[0][FirstCommand] = "BIO:Check";
        }

        @Override
        public void setNewStatusInformation(String[] resps) {
            super.setNewStatusInformation(resps);
            InProcessing = resps[FirstCommand].equals("0");
        }

        @Override
        public void statusUpdateProcessing() {
            if (OldInProcessing != InProcessing) {
                boolean wakeup = false;
                boolean failed = false;
                try {
                    JposCommonProperties props = getPropertySetInstance(Biometricss, 0, 0);
                    if (props != null) {
                        byte[] bdb = new byte[0];
                        if (!InProcessing && !PreviousPowerState) {
                            String data = sendResp("BIO:Get");
                            if (data != null)
                                bdb = getBDB(data);
                        }
                        if (failed = PreviousPowerState && !InProcessing)
                            handleEvent(new BiometricsStatusUpdateEvent(props.EventSource, BiometricsConst.BIO_SUE_FAILED_READ));
                        else if (InProcessing) {
                            if (wakeup = !Enrollment || LastCaptured == null)
                                handleEvent(new BiometricsStatusUpdateEvent(props.EventSource, BiometricsConst.BIO_SUE_SENSOR_READY));
                        }
                        else if (bdb == null) {
                            handleEvent(new BiometricsStatusUpdateEvent(props.EventSource, BiometricsConst.BIO_SUE_FAILED_READ));
                            CapturesMatched = false;
                        }
                        else {
                            if (Enrollment) {
                                if (bdb.length > 0 && !(wakeup = CapturesMatched = Arrays.equals(LastCaptured, bdb)))
                                    LastCaptured = bdb;
                                if (!wakeup) {
                                    if (++EnrollmentEntryCount < MaxEnrollmentEntries) {
                                        if (failed = sendResp("BIO:Start1") == null) {
                                            EnrollmentEntryCount = MaxEnrollmentEntries;
                                        }
                                    } else
                                        failed = true;
                                }
                            } else {
                                if (!(failed = bdb.length <= 0))
                                    LastCaptured = bdb;
                                wakeup = true;
                            }
                            if (failed)
                                handleEvent(new BiometricsStatusUpdateEvent(props.EventSource, BiometricsConst.BIO_SUE_FAILED_READ));
                            else if (wakeup)
                                handleEvent(new BiometricsStatusUpdateEvent(props.EventSource, BiometricsConst.BIO_SUE_SENSOR_COMPLETE));
                        }
                    }
                } catch (JposException e) {
                }
                if (wakeup || failed)
                    signalStatusWaits(Biometricss[0]);
            }
            PreviousPowerState = Offline;
        }

        @Override
        public boolean statusPowerOnlineProcessing() {
            JposCommonProperties props = getPropertySetInstance(Biometricss, 0, 0);
            if (props != null) {
                try {
                    handleEvent(new BiometricsStatusUpdateEvent(props.EventSource, JposConst.JPOS_SUE_POWER_ONLINE));
                } catch (JposException e) {}
            }
            return PreviousPowerState = super.statusPowerOnlineProcessing();
        }

        @Override
        public void statusPowerOfflineProcessing() {
            JposCommonProperties props = getPropertySetInstance(Biometricss, 0, 0);
            if (props != null) {
                try {
                    handleEvent(new BiometricsStatusUpdateEvent(props.EventSource, JposConst.JPOS_SUE_POWER_OFF_OFFLINE));
                } catch (JposException e) {}
            }
            signalStatusWaits(Biometricss[0]);
        }
    }

    private static final int INTSIZE = Integer.SIZE / Byte.SIZE;
    private static final int BDBSIZE = 0;
    private static final int BDBHASH1 = INTSIZE * 1;
    private static final int BDBHASH2 = INTSIZE * 2;
    private static final int BDBHASH3 = INTSIZE * 3;
    private static final int BDBDATALEN = INTSIZE * 4;
    private static final int BDBPAYLOADLEN = INTSIZE * 5;
    private static final int BDBDATA = INTSIZE * 6;

    /**
     * Creates simulator specific Biometric Data Block from user / password data retrieved by device
     * @param data Parameters returned from Get command (unicode code points of user and password, delimited with 0).
     * @return BDB [Layout: size(4), hashes(12), user length(4), payload length(4), user (UTF8), payload].
     */
    private byte[] getBDB(String data) {
        String[] dataStrings = {"", ""};
        int index = 0;
        for (String codestr : data.split(" ")) {
            if (codestr.equals("0"))
                index++;
            else
                dataStrings[index] += new String(Character.toChars(Integer.parseInt(codestr)));
        }
        synchronized (CapturePayload) {
            if (CapturePayload[0] == null)
                return null;
            else if (dataStrings[0].equals("") || dataStrings[1].equals(""))
                return new byte[0];
            else {
                byte[] user = dataStrings[0].getBytes();
                byte[] bdb = new byte[BDBDATA + user.length + CapturePayload[0].length];
                ByteBuffer buffer = ByteBuffer.wrap(bdb);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                buffer.putInt(BDBSIZE, bdb.length);
                buffer.putInt(BDBHASH1, data.hashCode());
                buffer.putInt(BDBHASH2, dataStrings[0].hashCode());
                buffer.putInt(BDBHASH3, dataStrings[1].hashCode());
                buffer.putInt(BDBDATALEN, user.length);
                buffer.putInt(BDBPAYLOADLEN, CapturePayload[0].length);
                System.arraycopy(user, 0, bdb, BDBDATA, user.length);
                System.arraycopy(CapturePayload[0], 0, bdb, BDBDATA + user.length, CapturePayload[0].length);
                buffer.clear();
                return bdb;
            }
        }
    }

    /**
     * Checks whether two biometric data blocks specify the same person
     * @param bdb1 First BDB
     * @param bdb2 Second BDB
     * @return true if both BDB belong to the same person, false otherwise. Returns null if hash differs but users are equal.
     */
    Boolean isEqual(byte[] bdb1, byte[]bdb2) {
        ByteBuffer buff1 = ByteBuffer.wrap(bdb1);
        ByteBuffer buff2 = ByteBuffer.wrap(bdb2);
        buff1.order(ByteOrder.LITTLE_ENDIAN);
        buff2.order(ByteOrder.LITTLE_ENDIAN);
        if (bdb1.length > BDBDATA && bdb2.length > BDBDATA) {
            boolean result = buff1.getInt(BDBHASH1) == buff2.getInt(BDBHASH1) &&
                    buff1.getInt(BDBHASH2) == buff2.getInt(BDBHASH2) &&
                    buff1.getInt(BDBHASH3) == buff2.getInt(BDBHASH3);
            int length = buff1.getInt(BDBDATALEN);
            if (length == buff2.getInt(BDBDATALEN) && bdb1.length >= BDBDATA + length && bdb2.length >= BDBDATA + length) {
                if (Arrays.equals(Arrays.copyOfRange(bdb1, BDBDATA, BDBDATA + length), Arrays.copyOfRange(bdb2, BDBDATA, BDBDATA + length)))
                    return result ? true : null;
            }
        }
        return false;
    }

    private class SampleProperties extends BiometricsProperties {
        private  SampleProperties() {
            super(0);
        }

        @Override
        public void handlePowerStateOnEnable() throws JposException {
            boolean offline;
            synchronized(Device) {
                offline = Offline;
            }
            handleEvent(new BiometricsStatusUpdateEvent(EventSource, offline ? JposConst.JPOS_SUE_POWER_OFF_OFFLINE : JposConst.JPOS_SUE_POWER_ONLINE));
        }

        @Override
        public void claim(int timeout) throws JposException {
            startPolling(this);
            if (Offline && PowerNotify == JposConst.JPOS_PN_DISABLED) {
                stopPolling();
                throw new JposException(JposConst.JPOS_E_OFFLINE, "Communication with device disrupted");
            }
            super.claim(timeout);
        }

        @Override
        public void deviceEnabled(boolean enable) throws JposException {
            if (InProcessing) {
                doItWaitState("BIO:Cancel", "1", true);
            }
            super.deviceEnabled(enable);
        }

        @Override
        public void release() throws JposException {
            super.release();
            stopPolling();
        }

        @Override
        public void checkHealth(int level) throws JposException {
            String how = level == JposConst.JPOS_CH_INTERNAL ? "Internal" : (level == JposConst.JPOS_CH_EXTERNAL ? "External" : "Interactive");
            if (Offline)
                CheckHealthText = how + " Checkhealth: Offline";
            else {
                CheckHealthText = how + " Checkhealth: OK";
            }
            if (level == JposConst.JPOS_CH_INTERACTIVE)
                synchronizedMessageBox("CheckHealth result:\n" + CheckHealthText, "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
        }

        private String doItWaitState(String command, String responseMask, boolean throwException) throws JposException {
            attachWaiter();
            String data = sendResp(command);
            if(data != null && data.matches(responseMask)) {
                PollWaiter.signal();
                waitWaiter(RequestTimeout * MaxRetry);
            } else if (throwException){
                releaseWaiter();
                throw new JposException(JposConst.JPOS_E_FAILURE, "Invalid capture request");
            }
            releaseWaiter();
            return data;
        }

        @Override
        public void beginEnrollCapture(byte[] referenceBIR, byte[] payload) throws JposException {
            check(Offline, JposConst.JPOS_E_OFFLINE, "Device offline");
            check(InProcessing, JposConst.JPOS_E_ILLEGAL, "Capture active");
            doItWaitState("BIO:Start0", "1", true);
            Enrollment = true;
            LastCaptured = null;
            CapturePayload[0] = payload;
            CapturesMatched = false;
            EnrollmentEntryCount = 0;
        }

        @Override
        public void beginVerifyCapture() throws JposException {
            check(Offline, JposConst.JPOS_E_OFFLINE, "Device offline");
            check(InProcessing, JposConst.JPOS_E_ILLEGAL, "Capture active");
            doItWaitState("BIO:Start0", "1", true);
            Enrollment = false;
            CapturePayload[0] = new byte[0];
        }

        @Override
        public void endCapture() throws JposException {
            check(Offline, JposConst.JPOS_E_OFFLINE, "Device offline");
            check(CapturePayload[0] == null, JposConst.JPOS_E_ILLEGAL, "Capture not started");
            String data = "0";
            if (InProcessing) {
                synchronized (CapturePayload) {
                    CapturePayload[0] = null;
                }
                data = doItWaitState("BIO:Cancel", "1", false);
                check(InProcessing, JposConst.JPOS_E_FAILURE, "Stop capture failed");
            }
            BiometricInformationRecord result = new BiometricInformationRecord();
            if (data.equals("0")) {
                if (LastCaptured.length > 0 && (!Enrollment || CapturesMatched)) {
                    result.setHeaderVersion(BIRHeaderVersion);
                    result.setBIRDataType(BiometricInformationRecord.DATA_TYPE_PROCESSED);
                    result.setFormatIDOwner(BIRFormatIDOwner);
                    result.setFormatIDType(BIRFormatIDType);
                    result.setQuality(100);
                    result.setPurpose(Enrollment ? BiometricInformationRecord.PURPOSE_ENROLL : BiometricInformationRecord.PURPOSE_ENROLL_FOR_VERIFICATION);
                    result.setBiometricType(BiometricInformationRecord.BIOMETRIC_TYPE_PASSWORD);
                    result.setBiometricDataBlock(LastCaptured);
                }
            }
            BIR = result.getBytes();
            EventSource.logSet("BIR");
            Enrollment = false;
            CapturePayload[0] = null;
            LastCaptured = null;
        }

        @Override
        public void identify(int maxFARRequested, int maxFRRRequested, boolean fARPrecedence, byte[][] referenceBIRPopulation, int[][] candidateRanking, int timeout) throws JposException {
            beginVerifyCapture();
            attachWaiter();
            long tio = timeout == JposConst.JPOS_FOREVER ? Long.MAX_VALUE : (timeout == 0 ? 1 : timeout);
            long starttime = System.currentTimeMillis();
            for (long deltatime = 0; deltatime < tio && InProcessing; deltatime = System.currentTimeMillis() - starttime) {
                waitWaiter(tio - deltatime);
            }
            releaseWaiter();
            if (InProcessing) {
                String data = doItWaitState("BIO:Cancel", "1", false);
                check(data == null, JposConst.JPOS_E_FAILURE, "Stop identify failed");
                throw new JposException(JposConst.JPOS_E_TIMEOUT, "Identify timed out");
            }
            endCapture();
            check(isDataEmpty(BIR, true), JposConst.JPOS_E_TIMEOUT, "No non-empty BIR within time limit");
            identifyMatch(maxFARRequested, maxFRRRequested, fARPrecedence, BIR, referenceBIRPopulation, candidateRanking);
        }

        @Override
        public void identifyMatch(int maxFARRequested, int maxFRRRequested, boolean fARPrecedence, byte[] sampleBIR, byte[][] referenceBIRPopulation, int[][] candidateRanking) throws JposException {
            // Ignore maxFARRequested, maxFRRRequested and fARPrecedence because password match is binary (0% or 100%).
            BiometricInformationRecord source = new BiometricInformationRecord(sampleBIR);
            long[] allowed = {
                    BiometricInformationRecord.PURPOSE_ENROLL,
                    BiometricInformationRecord.PURPOSE_ENROLL_FOR_VERIFICATION
            };
            checkMember(source.getPurpose(), allowed, JposConst.JPOS_E_ILLEGAL, "Invalid purpose: " + source.getPurpose());
            int[][] candidates = {new int[referenceBIRPopulation.length], new int[referenceBIRPopulation.length]};
            byte[] bdb = source.getBiometricDataBlock();
            int[] index = {0, 0};
            for (int i = 0; i < referenceBIRPopulation.length; i++) {
                Boolean result = isEqual(bdb, new BiometricInformationRecord(referenceBIRPopulation[i]).getBiometricDataBlock());
                if (result == null || result) {
                    if (result == null) {
                        if (maxFRRRequested == 0  || fARPrecedence) {
                            if (maxFARRequested > FARUserOnly)
                                candidates[1][index[1]++] = i;
                        } else if (maxFRRRequested > FRRUserOnly)
                            candidates[1][index[1]++] = i;
                    } else
                        candidates[0][index[0]++] = i;
                }
            }
            candidateRanking[0] = Arrays.copyOf(candidates[0], index[0] + index[1]);
            if (index[1] > 0)
                System.arraycopy(candidates[1], 0, candidateRanking[0], index[0], index[1]);
        }

        @Override
        public void verify(int maxFARRequested, int maxFRRRequested, boolean fARPrecedence, byte[] referenceBIR, byte[][] adaptedBIR, boolean[] result, int[] fARAchieved, int[] fRRAchieved, byte[][] payload, int timeout) throws JposException {
            beginEnrollCapture(referenceBIR, payload[0]);
            attachWaiter();
            long tio = timeout == JposConst.JPOS_FOREVER ? Long.MAX_VALUE : (timeout == 0 ? 1 : timeout);
            long starttime = System.currentTimeMillis();
            for (long deltatime = 0; deltatime < tio && InProcessing; deltatime = System.currentTimeMillis() - starttime) {
                waitWaiter(tio - deltatime);
            }
            releaseWaiter();
            if (InProcessing) {
                String data = doItWaitState("BIO:Cancel", "1", false);
                check(data == null, JposConst.JPOS_E_FAILURE, "Stop verify failed");
                throw new JposException(JposConst.JPOS_E_TIMEOUT, "Verify timed out");
            }
            endCapture();
            check(isDataEmpty(BIR, true), JposConst.JPOS_E_TIMEOUT, "No non-empty BIR within time limit");
            verifyMatch(maxFARRequested, maxFRRRequested, fARPrecedence, BIR, referenceBIR, adaptedBIR, result, fARAchieved, fRRAchieved, payload);
        }

        @Override
        public void verifyMatch(int maxFARRequested, int maxFRRRequested, boolean fARPrecedence, byte[] sampleBIR, byte[] referenceBIR, byte[][] adaptedBIR, boolean[] result, int[] fARAchieved, int[] fRRAchieved, byte[][] payload) throws JposException {
            // Ignore maxFARRequested, maxFRRRequested and fARPrecedence because password match is binary (0% or 100%).
            // Ignore adaptedBIR and payload because template adaption is not supported.
            // FAR and FRR are always 0 for user/password identification.
            BiometricInformationRecord source = new BiometricInformationRecord(sampleBIR);
            long[] allowed = {
                    BiometricInformationRecord.PURPOSE_ENROLL,
                    BiometricInformationRecord.PURPOSE_ENROLL_FOR_VERIFICATION
            };
            checkMember(source.getPurpose(), allowed, JposConst.JPOS_E_ILLEGAL, "Invalid purpose: " + source.getPurpose());
            BiometricInformationRecord reference = new BiometricInformationRecord(referenceBIR);
            check(reference.getPurpose() != BiometricInformationRecord.PURPOSE_ENROLL, JposConst.JPOS_E_ILLEGAL, "Invalid referenceBIR");
            byte[] bdb = source.getBiometricDataBlock();
            Boolean match = isEqual(bdb, reference.getBiometricDataBlock());
            if (match == null) {
                fARAchieved[0] = FARUserOnly;
                fRRAchieved[0] = FRRUserOnly;
                if (maxFRRRequested == 0 || fARPrecedence) {
                    if (maxFARRequested < FARUserOnly)
                        match = false;
                } else if (maxFRRRequested < FRRUserOnly)
                    match = false;
            } else if (match)
                fARAchieved[0] = fRRAchieved[0] = 0;
            else
                fARAchieved[0] = fRRAchieved[0] = 100;
            if (result[0] = match == null || match) {
                ByteBuffer buffer = ByteBuffer.wrap(reference.getBiometricDataBlock());
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                int length = buffer.getInt(BDBPAYLOADLEN);
                int payloadoffset = BDBDATA + buffer.getInt(BDBDATALEN);
                payload[0] = Arrays.copyOfRange(buffer.array(), payloadoffset, payloadoffset + length);
            } else {
                payload[0] = new byte[0];
            }
        }
    }

    @Override
    public BiometricsProperties getBiometricsProperties(int index) {
        return new SampleProperties();
    }
}
