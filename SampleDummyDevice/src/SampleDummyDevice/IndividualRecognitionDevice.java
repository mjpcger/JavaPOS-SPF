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

package SampleDummyDevice;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.individualrecognition.*;
import jpos.*;
import jpos.config.*;

import java.util.*;

import static javax.swing.JOptionPane.*;
import static jpos.JposConst.*;

/**
 * JposDevice based dummy implementation for JavaPOS IndividualRecognition device service implementation.
 * No real hardware. Recognition by operator interaction via OptionDialog boxes. Instead of image data, a unique pin
 * will be used for recognition.<br>
 * Supported configuration values for IndividualRecognition in jpos.xml can be used to set the individuals:<br>
 * <ul>
 *     <li>Individual<i>N</i>: Specifies individual data. Format: Comma delimited list of <i>name</i>=<i>value</i>
 *     pairs, where the following <i>name</i>s are supported:
 *     <ul>
 *         <li>Name: <i>value</i> must be the name of an individual. Mandatory pair.</li>
 *         <li>PIN: <i>value</i> must be a unique integer value &ge; 1000, used to identify the individual. Mandatory pair.</li>
 *         <li>Gender: <i>value</i> must be <b>male</b>, <b>female</b> or <b>diverse</b>. Optional pair.</li>
 *         <li>Age: <i>value</i> must be an integer value representing the individual's age in years. Optional pair.</li>
 *     </ul>
 *     <i>N</i> must be a consecutive number, starting at 1.
 *     </li>
 * </ul>
 * Property IndividualRecognitionFilter will contain one line per Individual<i>N</i> properties (line delimiter CR, LF
 * or CR LF). Each line has the following format: "ID:%n;%e,Gender:%g;%f,Age:%a;%b", where<ul>%n specifies the individual ID,<br>
 * %e specifies whether the individual shall be enabled,<br>%g specifies whether gender is recognizable,<br>%f specifies whether
 * gender recognition is enabled,<br>%a specifies whether age is recognizable and<br>%b specifies whether age recognition is
 * enabled.</ul>
 * Keep in mind: %a, %b, %e, %g and %f are placeholders for either the letter Y or N. Y means enabled or recognizable, N
 * means disabled or not recognizable. If gender or age is not recognizable, it cannot be enabled.<br>
 * <b>Example:</b><br>
 * Let us assume,the following properties have been specified in jpos.xml:
 * <ul>
 *     <li>name="Individual1" value="Name=John Doe,PIN=135792468,Gender=male,Age=28"</li>
 *     <li>name="Individual2" value="Name=Jane Doe,PIN=2468013579,Gender=female"</li>
 *     <li>name="Individual3" value="Name=Hue Doe,PIN=1234567890,Age=45"</li>
 * </ul>
 * Then the initial value of IndividualRecognitionFilter will be<ul>
 *     ID:1;Y,Gender:Y;Y,Age:Y;Y<br>
 *     ID:2;Y,Gender:Y;Y,Age:N;N<br>
 *     ID:3;Y,Gender:N;N,Age:Y;Y
 * </ul>
 */
public class IndividualRecognitionDevice extends JposDevice {
    protected IndividualRecognitionDevice(String id) {
        super(id);
        individualRecognitionInit(1);
        PhysicalDeviceDescription = "Dummy IndividualRecognition simulator";
        PhysicalDeviceName = "Dummy IndividualRecognition Simulator";
        CapPowerReporting = JPOS_PR_NONE;
    }

    private static class Individual {
        private final int ID;
        private String Name = null;
        private String PIN = null;
        private Integer Age = null;
        private String Gender = null;
        private boolean Enabled = true;
        private boolean AgeEnabled = false;
        private boolean GenderEnabled = false;

        Individual(int id) {
            ID = id;
        }

        void setName(String name) throws JposException {
            check(Name != null, JPOS_E_NOSERVICE, "Duplicate Name for ID " + ID);
            Name = name;
        }

        void setPIN(String pin) throws JposException{
            check(PIN != null, JPOS_E_NOSERVICE, "Duplicate PIN for ID " + ID);
            try {
                long val = Long.parseLong(pin);
                check (val < 1000, JPOS_E_NOSERVICE, "Pin too low for ID " + ID + ": " + pin);
                PIN = String.valueOf(val);
            } catch (NumberFormatException ignore) {
                throw new JposException(JPOS_E_NOSERVICE, "Invalid PIN for ID " + ID + ": " + pin);
            }
        }

        void setAge(String age) throws JposException {
            check(Age != null, JPOS_E_NOSERVICE, "Duplicate Age for ID " + ID);
            try {
                int val = Integer.parseInt(age);
                check (val < 0, JPOS_E_NOSERVICE, "Age too low for ID " + ID + ": " + age);
                Age = val;
                AgeEnabled = true;
            } catch (NumberFormatException ignore) {
                throw new JposException(JPOS_E_NOSERVICE, "Invalid Age for ID " + ID + ": " + age);
            }
        }

        void setGender(String gender) throws JposException {
            check(Gender != null, JPOS_E_NOSERVICE, "Duplicate Gender for ID " + ID);
            check(!member(gender, new String[]{"male", "female", "diverse"}), JPOS_E_NOSERVICE, "Invalid Gender for ID " + ID + ": " + gender);
            Gender = gender;
            GenderEnabled = true;
        }
    }

    @Override
    public void changeDefaults(IndividualRecognitionProperties p) {
        MyProperties props = (MyProperties) p;
        super.changeDefaults(p);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "Individual Recognition service for sample dummy device";
        StringBuilder caplist = new StringBuilder();
        StringBuilder recfilter = new StringBuilder();
        for (Individual current : props.Individuals) {
            caplist.append(String.format(",%d:%s", current.ID, current.Name));
            recfilter.append(String.format("\nID:%d;Y,Gender:%c;%c,Age:%c;%c",
                    current.ID, current.GenderEnabled ? 'Y' : 'N', current.GenderEnabled ? 'Y' : 'N',
                    current.AgeEnabled ? 'Y' : 'N', current.AgeEnabled ? 'Y' : 'N'));
        }
        props.CapIndividualList = caplist.substring(1);
        props.IndividualRecognitionFilter = recfilter.substring(1);
    }

    @Override
    public IndividualRecognitionProperties getIndividualRecognitionProperties(int index) {
        return new MyProperties();
    }

    private class MyProperties extends IndividualRecognitionProperties implements Runnable {
        private Individual[] Individuals = null;

        @Override
        public void checkProperties(JposEntry entries) throws JposException {
            super.checkProperties(entries);
            List<Individual> individuals = new ArrayList<>();
            for (int n = 1; true; n++) {
                Object o = entries.getPropertyValue("Individual" + n);
                if (o == null)
                    break;
                String[] individual = o.toString().split(",");
                Individual current = new Individual(n);
                for (String pair : individual) {
                    String[] namevalue = pair.split("=");
                    check(namevalue.length != 2, JPOS_E_NOSERVICE, "Invalid format: " + pair);
                    if ("Name".equals(namevalue[0]))
                        current.setName(namevalue[1]);
                    else if ("PIN".equals(namevalue[0]))
                        current.setPIN(namevalue[1]);
                    else if ("Gender".equals(namevalue[0]))
                        current.setGender(namevalue[1]);
                    else if ("Age".equals(namevalue[0]))
                        current.setAge(namevalue[1]);
                    else
                        throw new JposException(JPOS_E_NOSERVICE, "Invalid pair name: " + namevalue[0]);
                }
                check(current.Name == null, JPOS_E_NOSERVICE, "Name missing for ID " + n);
                check(current.PIN == null, JPOS_E_NOSERVICE, "PIN missing for ID " + n);
                individuals.add(current);
            }
            Individuals = individuals.toArray(new Individual[0]);
        }

        protected MyProperties() {
            super(0);
        }

        @Override
        public void claim(int timeout) throws JposException {
            super.claim(timeout);
            synchronized (Box) {
                if (!Finish) {
                    new Thread(this).start();
                } else // If just running: Do not finish.
                    Finish = false;
            }
        }

        @Override
        public void release() throws JposException {
            synchronized (Box) {
                Finish = true;
                Box.abortDialog();
            }
            super.release();
        }

        private boolean Finish = false;
        private final SynchronizedMessageBox Box = new SynchronizedMessageBox();

        @Override
        @SuppressWarnings("SynchronizeOnNonFinalField")
        public void run() {
            String title = "Dummy IndividualRecognition Device Simulator";
            String message = "Enter PIN, press OK when ready, ERASE to erase input and ABORT to abort:\n   ";
            String[] options = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "OK", "ERASE", "ABORT"};
            String pin = "";
            boolean done = false;
            while (true) {
                synchronized (Box) {
                    if (Finish) {
                        Finish = false;
                        break;
                    }
                }
                int result = Box.synchronizedConfirmationBox(message + pin.replaceAll("\\d", "*"),
                        title, options, "OK", INFORMATION_MESSAGE, JPOS_FOREVER);
                String errormessage = null;
                if (result >= 0) {
                    if (result <= 9) {
                        pin += result;
                        try {
                            Long.parseLong(pin);
                        } catch (Exception e) {
                            done = true;
                            errormessage = "PIN too long";
                        }
                    } else if (options[result].equals("ERASE"))
                        pin = "";
                    else if (options[result].equals("ABORT")) {
                        done = true;
                        errormessage = "Recognition Abort";
                    } else if (options[result].equals("OK"))
                        done = true;
                }
                if (done) {  // We have a result
                    if (DeviceEnabled) {
                        if (errormessage == null) {
                            int count = 0;
                            Individual recognized = null;
                            boolean[] enabled = null;
                            for (Individual current : Individuals) {
                                if (current.PIN.equals(pin)) {
                                    synchronized (Individuals) {
                                        recognized = current;
                                        enabled = new boolean[]{current.Enabled, current.GenderEnabled, current.AgeEnabled};
                                    }
                                    count = 0;
                                    break;
                                }
                                if (current.PIN.length() > pin.length() && pin.equals(current.PIN.substring(0, pin.length()))) {
                                    synchronized (Individuals) {
                                        recognized = current;
                                        enabled = new boolean[]{current.Enabled, current.GenderEnabled, current.AgeEnabled};
                                    }
                                    count++;
                                }
                            }
                            if (recognized == null || count > 1) {
                                errormessage = count > 0 ? "Too Many Candidates" : "No Match";
                            } else if (enabled[0]) {
                                String id = String.valueOf(recognized.ID);
                                String info = "";
                                if (enabled[1])
                                    info += ",Gender=" + recognized.Gender;
                                if (enabled[2])
                                    info += ",Age=" + recognized.Age;
                                if (info.length() > 0)
                                    info = info.substring(1);
                                try {
                                    if (IndividualRecognitionInformationIsInputProperty) {
                                        handleEvent(new IndividualRecognitionDataEvent(EventSource, 0, id, info));
                                    } else {
                                        if (!info.equals(IndividualRecognitionInformation)) {
                                            IndividualRecognitionInformation = info;
                                            EventSource.logSet("IndividualRecognitionInformation");
                                        }
                                        handleEvent(new IndividualRecognitionDataEvent(EventSource, 0, id, null));
                                    }
                                } catch (JposException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                errormessage = "Candidate disabled";
                            }
                        }
                        if (errormessage != null) {
                            try {
                                handleEvent(new IndividualRecognitionErrorEvent(EventSource, JPOS_E_NOEXIST, 0, null, null, errormessage));
                            } catch (JposException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    pin = "";
                    done = false;
                }
            }
        }

        @Override
        @SuppressWarnings("SynchronizeOnNonFinalField")
        public void individualRecognitionFilter(String filter) throws JposException {
            if (!IndividualRecognitionFilter.equals(filter)) {
                String[] filterlines = filter.replaceAll("\r\n", "\n").replaceAll("\r", "\n").split("\n");
                check(filterlines.length != Individuals.length, JPOS_E_ILLEGAL, "Invalid filter dimension: " + filterlines.length);
                String[] valid1 = {"N", "Y"}, valid0 = {"N"};
                for (int i = 0; i < Individuals.length; i++) {
                    // Line i must correspond to data in Individuals[i]
                    Individual act = Individuals[i];
                    // Split and check parts
                    String[] parts = filterlines[i].split(",");
                    check(parts.length != 3, JPOS_E_ILLEGAL, "Invalid parts dimension: " + parts.length);
                    // Split and check part 0 (ID)
                    String[] data = parts[0].split(":");
                    check(!"ID".equals(data[0]), JPOS_E_ILLEGAL, "No ID part: " + parts[0]);
                    check(data.length != 2, JPOS_E_ILLEGAL,"Invalid part dimension: " + data.length + "for part " + parts[0]);
                    // Split and check id and enablement of part 0
                    data = data[1].split(";");
                    check(data.length != 2, JPOS_E_ILLEGAL,"Invalid part condition dimension: " + data.length + "for part " + parts[0]);
                    check(!String.valueOf(act.ID).equals(data[0]), JPOS_E_ILLEGAL, "ID out of range: " + data[0]);
                    check(!member(data[1], valid1), JPOS_E_ILLEGAL, "Invalid enable value: " + data[1]);
                    // Split and check part 1 (Gender)
                    data = parts[1].split(":");
                    check(!"Gender".equals(data[0]), JPOS_E_ILLEGAL, "No Gender part: " + parts[0]);
                    check(data.length != 2, JPOS_E_ILLEGAL,"Invalid part dimension: " + data.length + "for part " + parts[0]);
                    // Split and check capability and enablement of part 1
                    data = data[1].split(";");
                    check(data.length != 2, JPOS_E_ILLEGAL,"Invalid part condition dimension: " + data.length + "for part " + parts[0]);
                    check(!data[0].equals(act.Gender == null ? "N" : "Y"), JPOS_E_ILLEGAL, "Invalid Gender capability: " + data[0]);
                    check(!member(data[1], act.Gender == null ? valid0 : valid1), JPOS_E_ILLEGAL, "Invalid Gender enablement value: " + data[1]);
                    // Split and check part 2 (Age)
                    data = parts[2].split(":");
                    check(!"Age".equals(data[0]), JPOS_E_ILLEGAL, "No Age part: " + parts[0]);
                    check(data.length != 2, JPOS_E_ILLEGAL,"Invalid part dimension: " + data.length + "for part " + parts[0]);
                    // Split and check capability and enablement of part 2
                    data = data[1].split(";");
                    check(data.length != 2, JPOS_E_ILLEGAL,"Invalid part condition dimension: " + data.length + "for part " + parts[0]);
                    check(!data[0].equals(act.Age == null ? "N" : "Y"), JPOS_E_ILLEGAL, "Invalid Age capability: " + data[0]);
                    check(!member(data[1], act.Age == null ? valid0 : valid1), JPOS_E_ILLEGAL, "Invalid Age enablement value: " + data[1]);
                }
                synchronized (Individuals) {
                    for (int i = 0; i < Individuals.length; i++) {
                        String[] parts = filterlines[i].split(",");
                        Individual current = Individuals[i];
                        current.Enabled = "Y".equals(parts[0].split(":")[1].split(";")[1]);
                        current.GenderEnabled = "Y".equals(parts[1].split(":")[1].split(";")[1]);
                        current.AgeEnabled = "Y".equals(parts[2].split(":")[1].split(";")[1]);
                    }
                }
                IndividualRecognitionFilter = filter;
                EventSource.logSet("IndividualRecognitionFilter");
            }
        }
    }
}
