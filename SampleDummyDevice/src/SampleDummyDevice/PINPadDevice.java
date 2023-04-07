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
import de.gmxhome.conrad.jpos.jpos_base.pinpad.*;
import jpos.*;
import jpos.config.*;

import javax.swing.*;
import java.util.*;

/**
 * JposDevice based dummy implementation for JavaPOS PINPad device service implementation.
 * No real hardware. All read data with dummy values, operator interaction via OptionDialog boxes.<br>
 * Supported configuration values for PINPad in jpos.xml can be used to set the corresponding property values:
 * <ul>
 *     <li>CapMACCalculation: Can be TRUE or FALSE. Default is FALSE.</li>
 *     <li>CapDisplay: Can be set to "PPAD_DISP_UNRESTRICTED", "PPAD_DISP_PINRESTRICTED", "PPAD_DISP_RESTRICTED_LIST",
 *     "PPAD_DISP_RESTRICTED_ORDER" or "PPAD_DISP_NONE". Default is "PPAD_DISP_NONE"</li>
 *     <li>CapLanguage: Can be set to "PPAD_LANG_NONE", "PPAD_LANG_ONE", "PPAD_LANG_PINRESTRICTED" or
 *     "PPAD_LANG_UNRESTRICTED". Default is "PPAD_LANG_NONE"</li>
 *     <li>MaximumPINLength: Can be set to one of "1", "2", "3", "4", "5", "6", "7", "8" or "9". Default is "4".</li>
 *     <li>MinimumPINLength: Can be set to the same values as MaximumPINLength or to "0". Default is "4".
 *     MaximumPINLength must be greater or equal to MinimumPINLength.</li>
 *     <li>AllowAlwaysSetProperties: Can be TRUE or FALSE. Default is FALSE. If TRUE, the service will throw an exception
 *     if the application tries to change a writable property while the device has not been claimed.</li>
 * </ul>
 * If CapDisplay will be set to PPAD_DISP_UNRESTRICTED, CapLanguage will be set to PPAD_LANG_NONE and any configuration
 * option for CapLanguage will be ignored.
 * <br>Only one language will be supported: "EN,US".
 * <br>"Encryption is very simple: The fixed value as stored in AdditionalSecurityInformation will be added to the byte
 * code of every digit of the entered PIN.
 * <br>UpdateKey excepts any key of any length, as long as the key number is between 1 and 3.
 * <br><b>SPECIAL REMARKS:</b> This sample does not implement any really existing PINPad system and shall not be used in any
 * really existing cash register application. Therefore, is supports only one sample PINPad system to be specified in
 * the BeginEFTTransaction method: "SAMPLE".
 */
public class PINPadDevice extends JposDevice implements Runnable {
    protected PINPadDevice(String id) throws JposException {
        super(id);
        pINPadInit(1);
        PhysicalDeviceDescription = "Dummy PINPad simulator";
        PhysicalDeviceName = "Dummy PINPad Simulator";
        CapPowerReporting = JposConst.JPOS_PR_NONE;
    }

    private String[][] Capabilities = {
            {"CapMACCalculation", PropTypeBool, "TRUE", "FALSE"},
            {"CapDisplay", PropTypeSym, "PPAD_DISP_UNRESTRICTED", "PPAD_DISP_PINRESTRICTED", "PPAD_DISP_RESTRICTED_LIST", "PPAD_DISP_RESTRICTED_ORDER", "PPAD_DISP_NONE"},
            {"CapLanguage", PropTypeSym, "PPAD_LANG_NONE", "PPAD_LANG_ONE", "PPAD_LANG_PINRESTRICTED", "PPAD_LANG_UNRESTRICTED"},
            {"MaximumPINLength", PropTypeInt, "1", "2", "3", "4", "5", "6", "7", "8", "9"},
            {"MinimumPINLength", PropTypeInt, "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"}
    };
    static private final int PropName = 0;          // Index of property name
    static private final int PropType = 1;          // Index of property type
    static private final int PropValueBase = 2;     // Index of first allowed value as String
    static private final String PropTypeBool = "0"; // Property type Boolean, allowed values true and false
    static private final String PropTypeSym = "1";  // Property type symbol, allowed values static properties of class RFIDScannerConst
    static private final String PropTypeInt = "2";  // Property type Integer, allowed values as specified starting at PropValueBase.

    static private final String[] Prompts = {       // Strings that correspond to property Prompt
            "",
            "Enter PIN",            // PPAD_MSG_ENTERPIN
            "Please Wait",          // PPAD_MSG_PLEASEWAIT
            "",
            "Retries Exceeded",     // PPAD_MSG_RETRIESEXCEEDED
            "Approved",             // PPAD_MSG_APPROVED
            "Declined",             // PPAD_MSG_DECLINED
            "Cancelled",            // PPAD_MSG_CANCELED
            "",
            "Locked",               // PPAD_MSG_NOTREADY
            "Ready",                // PPAD_MSG_IDLE
            "Swipe Card",           // PPAD_MSG_SLIDE_CARD
            "Insert Card"           // PPAD_MSG_INSERTCARD
    };

    private String Message = Prompts[PINPadConst.PPAD_MSG_NOTREADY];

    private SynchronizedMessageBox TheBox = new SynchronizedMessageBox();
    private Thread TheThread = null;
    private Boolean ToBeFinished;
    private Map<PINPadProperties, Boolean> UsePrompt = new HashMap<>();

    private Map<String, String[]> LastEntries = new HashMap<>();

    @Override
    public void checkProperties(JposEntry entry) throws JposException {
        super.checkProperties(entry);
        Object o;
        int min = 4, max = 4;
        for (String[] capability : Capabilities) {
            o = entry.getPropertyValue(capability[PropName]);
            if (o != null) {
                int j;
                for (j = PropValueBase; j < capability.length; j++) {
                    if (capability[j].equals(o.toString().toUpperCase()))
                        break;
                }
                check(j == capability.length, JposConst.JPOS_E_ILLEGAL, "Invalid value for property " + capability[PropName] + ": " + o.toString());
                LastEntries.put(capability[PropName], new String[]{capability[PropType], o.toString()});
                if (capability[PropName].equals("MaximumPINLength"))
                    max = Integer.parseInt(o.toString());
                else if (capability[PropName].equals("MinimumPINLength"))
                    min = Integer.parseInt(o.toString());
            }
        }
        check(min > max, JposConst.JPOS_E_NOSERVICE, "Bad PIN length: " + min + " <= " + max + " ???");
    }

    @Override
    public void changeDefaults(PINPadProperties props) {
        props.MinimumPINLength = props.MaximumPINLength = 4;
        for (String capa : LastEntries.keySet()) {
            try {
                String[] attr = LastEntries.get(capa);
                Object value = null;
                if (attr[0].equals(PropTypeBool))
                    value = attr[1].toUpperCase().equals("TRUE");
                else if (attr[0].equals(PropTypeInt))
                    value = Integer.parseInt(attr[1]);
                else
                    value = PINPadConst.class.getField(attr[1]).get(null);
                props.getClass().getField(capa).set(props, value);
            } catch (Exception ignored) {}
        }
        if (props.CapDisplay == PINPadConst.PPAD_DISP_NONE || props.CapDisplay == PINPadConst.PPAD_DISP_UNRESTRICTED) {
            UsePrompt.put(props, false);
            props.CapLanguage = PINPadConst.PPAD_LANG_NONE;
        } else {
            UsePrompt.put(props, true);
            props.Prompt = PINPadConst.PPAD_MSG_NOTREADY;
            props.AvailableLanguagesList = props.CapLanguage == PINPadConst.PPAD_LANG_ONE ? "EN,US" : "EN,US;EN,UK";
            props.PromptLanguage = "EN,US";
            props.AvailablePromptsList = props.CapDisplay == PINPadConst.PPAD_DISP_PINRESTRICTED ? "2,4,5,6,7,9,10,11,12" : "1,2,4,5,6,7,9,10,11,12";
        }
        props.SupportedPINPadSystems = "SAMPLE";
    }

    @Override
    public void run() {
        long[] noPromptDisplays = {PINPadConst.PPAD_DISP_UNRESTRICTED, PINPadConst.PPAD_DISP_NONE};
        String title = "Dummy PINPad Simulator";
        String[] options;
        SampleProperties props;
        String pin = "";
        int shift = 'A' - '0';
        while (!ToBeFinished) {
            try {
                props = (SampleProperties) getClaimingInstance(ClaimedPINPad, 0);
                if (props != null && props.PINEntryEnabled) {
                    boolean noprompt = member(props.CapDisplay, noPromptDisplays);
                    String message = Message + "\n" + "*********".substring(0, pin.length()) + "_________".substring(pin.length(), props.MaximumPINLength);
                    options = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "OK", "Clear", "Cancel", "Error", "Timeout"};
                    int result = TheBox.synchronizedConfirmationBox(message, title, options, "OK", JOptionPane.INFORMATION_MESSAGE, JposConst.JPOS_FOREVER);
                    if (result >= 0) {
                        if (result < 10)
                            pin += options[result];
                        if (pin.length() == props.MaximumPINLength || (pin.length() >= props.MinimumPINLength && options[result].equals("OK"))) {
                            pinReadySendDataEvent(props, pin, shift, noprompt);
                        } else if (options[result].length() > 1){
                            pin = "";
                            if (!options[result].equals("Clear")) {
                                pinReadyWithErrorSendDataOrErrorEvent(options, props, noprompt, result);
                            }
                        }
                    }
                    shift = updateShift(props, shift);
                } else {
                    pin = "";
                    options = new String[0];
                    TheBox.synchronizedConfirmationBox(Message, title, options, null, JOptionPane.INFORMATION_MESSAGE, JposConst.JPOS_FOREVER);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private int updateShift(SampleProperties props, int shift) {
        if (!props.PINEntryEnabled) {
            if ((shift += 1) > 'Z' - '9')
                shift = 'A' - '0';
        }
        return shift;
    }

    private void pinReadyWithErrorSendDataOrErrorEvent(String[] options, SampleProperties props, boolean noprompt, int result) {
        try {
            if (noprompt)
                Message = Prompts[PINPadConst.PPAD_MSG_DECLINED];
            if (options[result].equals("Error")) {
                handleEvent(new JposErrorEvent(props.EventSource, JposConst.JPOS_E_FAILURE, 0, JposConst.JPOS_EL_INPUT, "An error occurred"));
            } else if (options[result].equals("OK")) {
                handleEvent(new JposErrorEvent(props.EventSource, JposConst.JPOS_E_ILLEGAL, 0, JposConst.JPOS_EL_INPUT, "Invalid PIN"));
            } else {
                if (noprompt)
                    Message = Prompts[PINPadConst.PPAD_MSG_CANCELED];
                handleEvent(new PINPadDataEvent(props.EventSource, options[result].equals("Cancel") ?
                        PINPadConst.PPAD_CANCEL : PINPadDataEvent.PPAD_TIMEOUT, "", ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        props.PINEntryEnabled = false;
        props.EventSource.logSet("PINEntryEnabled");
    }

    private void pinReadySendDataEvent(SampleProperties props, String pin, int shift, boolean noprompt) {
        String encryptedpin = encryptPin(pin, shift);
        try {
            if (noprompt)
                Message = Prompts[PINPadConst.PPAD_MSG_APPROVED];
            handleEvent(new PINPadDataEvent(props.EventSource, PINPadConst.PPAD_SUCCESS, encryptedpin.substring(0, 2), encryptedpin.substring(2)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        props.PINEntryEnabled = false;
        props.EventSource.logSet("PINEntryEnabled");
    }

    private String encryptPin(String pin, int key) {
        String result = Integer.toHexString(key);
        for (int i = 0; i < pin.length(); i++) {
            result += Integer.toHexString(pin.charAt(i) + key);
        }
        return result;
    }

    @Override
    public PINPadProperties getPINPadProperties(int index) {
        return new SampleProperties();
    }

    private class SampleProperties extends PINPadProperties {
        protected SampleProperties() {
            super(0);
        }

        @Override
        public void setPrompt(int prompt) throws JposException {
            super.setPrompt(prompt);
            if (Claimed) {
                Message = Prompts[prompt];
                TheBox.abortDialog();
            }
        }

        @Override
        public void open() throws JposException {
            super.open();
            synchronized(PINPadDevice.this) {
                if (TheThread == null) {
                    TheThread = new Thread(PINPadDevice.this);
                    TheThread.setName("PINPadSampleThread");
                    Message = UsePrompt.get(this) ? Prompts[Prompt] : Prompts[PINPadConst.PPAD_MSG_NOTREADY];
                    ToBeFinished = false;
                    TheThread.start();
                }
            }
        }

        @Override
        public void close() throws JposException {
            synchronized(PINPadDevice.this) {
                boolean stopthread = getCount(PINPads) == 1;
                super.close();
                if (stopthread) {
                    ToBeFinished = true;
                    TheBox.abortDialog();
                    try {
                        TheThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                TheThread = null;
            }
        }

        @Override
        public void claim(int timeout) throws JposException {
            super.claim(timeout);
            int prompt = UsePrompt.get(this) ? Prompt : PINPadConst.PPAD_MSG_IDLE;
            if (!Message.equals(Prompts[prompt])) {
                setPrompt(prompt);
            }
        }

        @Override
        public void release() throws JposException {
            super.release();
            if (!UsePrompt.get(this)) {
                setPrompt(PINPadConst.PPAD_MSG_NOTREADY);
            }
        }

        @Override
        public void beginEFTTransaction(String system, int host) throws JposException {
            super.beginEFTTransaction(system, host);
            synchronized(PINPadDevice.this) {
                if (!UsePrompt.get(this)) {
                    Message = Prompts[PINPadConst.PPAD_MSG_PLEASEWAIT];
                    TheBox.abortDialog();
                }
            }
        }

        @Override
        public void endEFTTransaction(int code) throws JposException {
            synchronized(PINPadDevice.this) {
                if (!UsePrompt.get(this)) {
                    Message = Prompts[PINPadConst.PPAD_MSG_IDLE];
                    TheBox.abortDialog();
                }
            }
            super.endEFTTransaction(code);
        }

        @Override
        public void enablePINEntry() throws JposException {
            synchronized(PINPadDevice.this) {
                if (!UsePrompt.get(this) || CapDisplay == PINPadConst.PPAD_DISP_PINRESTRICTED)
                    Message = Prompts[PINPadConst.PPAD_MSG_ENTERPIN];
                TheBox.abortDialog();
            }
            super.enablePINEntry();
        }

        @Override
        public void updateKey(int num, String key) throws JposException {
            check(num < 1 || num > 3, JposConst.JPOS_E_ILLEGAL, "Invalid key number");
            // Surprise: The simulator does not use a key.
            super.updateKey(num, key);
        }

        @Override
        public void computeMAC(String in, String[] out) throws JposException {
            // Dummy handling: Add hashCode (8 byte hex-ascii) to input.
            out[0] = in + Long.toString((0xffffffffl & (long)in.hashCode()), 16);
            out[0] = in + "00000000".substring(out[0].length()) + out[0];
        }

        @Override
        public void verifyMAC(String message) throws JposException {
            // Reverse of the above
            try {
                long storedhash = Long.parseLong(message.substring(message.length() - 8));
                long computedhash = 0xffffffffl & (long) message.substring(0, message.length() - 8).hashCode();
                check(computedhash != storedhash, JposConst.JPOS_E_FAILURE, "Wrong MAC");
            } catch (JposException e) {
                throw e;
            } catch (Exception e) {
                throw new JposException(JposConst.JPOS_E_FAILURE, "Invalid MAC: " + message);
            }
        }
    }
}
