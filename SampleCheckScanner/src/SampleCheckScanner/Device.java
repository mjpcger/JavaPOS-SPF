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

package SampleCheckScanner;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.checkscanner.CheckScannerDataEvent;
import de.gmxhome.conrad.jpos.jpos_base.checkscanner.CheckScannerProperties;
import de.gmxhome.conrad.jpos.jpos_base.checkscanner.CheckScannerStatusUpdateEvent;
import jpos.*;
import jpos.config.*;

import javax.swing.*;
import java.awt.*;

/**
 * JposDevice based dummy implementation of JavaPOS CheckScanner device service implementation.
 * No real hardware. Scanned images always with dummy values, operator interaction via message boxes.
 */
public class Device extends JposDevice implements Runnable {
    protected Device(String id) {
        super(id);
        checkScannerInit(1);
        PhysicalDeviceDescription = "CheckScanner device simulator";
        PhysicalDeviceName = "CheckScanner Device Simulator";
        CapPowerReporting = JposConst.JPOS_PR_NONE;
    }

    @Override
    public void checkProperties(JposEntry entry) throws JposException {
        super.checkProperties(entry);
        try {
            Object o;
            int val;
            if ((o = entry.getPropertyValue("MaxHeight")) != null && (val = Integer.parseInt(o.toString())) > 0)
                MaxHeight = val;
            if ((o = entry.getPropertyValue("MaxWidth")) != null && (val = Integer.parseInt(o.toString())) > 0)
                MaxWidth = val;
            if ((o = entry.getPropertyValue("Contrast")) != null && (val = Integer.parseInt(o.toString())) >= 0 && val <= 100)
                Contrast = val;
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property", e);
        }
    }

    private int MaxHeight = 1000;
    private int MaxWidth = 2000;
    private int Contrast = 50;

    @Override
    public void changeDefaults(CheckScannerProperties props) {
        props.DocumentHeightDef = MaxHeight;
        props.DocumentWidthDef = MaxWidth;
        props.CapColor = CheckScannerConst.CHK_CCL_MONO;
        props.CapImageFormat = CheckScannerConst.CHK_CIF_NATIVE;
        props.ContrastDef = Contrast;    // UPOS specification: Shall be user configurable.
        props.Quality = 100;
        props.QualityList = "100,300";
    }

    @Override
    public CheckScannerProperties getCheckScannerProperties(int index) {
        return new SampleCheckScannerProperties();
    }

    @Override
    public void run() {
        if (OperationName.equals("DoTheScan")) {
            synchronizedConfirmationBox("Scan Simulation Successful?", "Check Scanner", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        }
        synchronized (IsRunning) {
            IsRunning[0] = false;
            prepareSignalStatusWaits(CheckScanners[0]);
            signalStatusWaits(CheckScanners[0]);
        }
    }

    private boolean[] IsRunning = {false};
    private String OperationName;
    private boolean[] DialogReady = {false};
    private Integer DialogResult = null;

    private int synchronizedConfirmationBox(final String message, final String title, final int optionType, final int messageType) {
        DialogReady[0] = false;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DialogResult = JOptionPane.showConfirmDialog(null, message, title, optionType, messageType);
                dialogReady(true);
            }
        });

        while(!dialogReady(false)) {
            try {
                Thread.sleep(100L);
            } catch (Exception var5) {
            }
        }
        return DialogResult;
    }

    private boolean dialogReady(boolean set) {
        synchronized (DialogReady) {
            if (set) {
                DialogReady[0] = true;
            }
            return DialogReady[0];
        }
    }

    private void startAsyncOperation(String name) throws JposException {
        synchronized (IsRunning) {
            check(IsRunning[0], JposConst.JPOS_E_FAILURE, "Operation " + OperationName + " is running");
            OperationName = name;
            IsRunning[0] = true;
            Thread runner = new Thread(this);
            runner.setName(ID + ":" + name);
            runner.start();
        }
    }

    private class SampleCheckScannerProperties extends CheckScannerProperties {
        protected SampleCheckScannerProperties() {
            super(0);
        }

        @Override
        public void beginInsertion(int timeout) throws JposException {
            synchronized (IsRunning) {
                if (!IsRunning[0])
                    startAsyncOperation("DoTheScan");
                attachWaiter();
            }
            boolean waitResult = waitWaiter(timeout == JposConst.JPOS_FOREVER ? SyncObject.INFINITE : timeout);
            releaseWaiter();
            if (!waitResult) {
                InsertionMode = true;
                throw new JposException(JposConst.JPOS_E_TIMEOUT, "Timeout BeginInsertion");
            }
            check(DialogResult != JOptionPane.OK_OPTION, JposConst.JPOS_E_FAILURE, "Check Insertion Failed");
        }

        Boolean Inserted = null;

        @Override
        public void endInsertion() throws JposException {
            boolean waitResult = false;
            synchronized (IsRunning) {
                if (IsRunning[0]) {
                    attachWaiter();
                    waitResult = true;
                }
            }
            if (waitResult) {
                waitWaiter(SyncObject.INFINITE);
                releaseWaiter();
            }
            checkext(DialogResult != JOptionPane.OK_OPTION, CheckScannerConst.JPOS_ECHK_NOCHECK, "No check available");
            handleEvent(new CheckScannerStatusUpdateEvent(EventSource, CheckScannerConst.CHK_SUE_SCANCOMPLETE));
            Inserted = true;
        }

        @Override
        public void beginRemoval(int timeout) throws JposException {
            checkext(Inserted == null, CheckScannerConst.JPOS_ECHK_NOCHECK, "No check available");
            Inserted = false;
        }

        @Override
        public void endRemoval() throws JposException {
            checkext(Inserted == null, CheckScannerConst.JPOS_ECHK_NOCHECK, "No check available");
            checkext(Inserted, CheckScannerConst.JPOS_ECHK_CHECK, "Check still available");
            Inserted = null;
        }

        @Override
        public void retrieveImage(int id) throws JposException {
            checkext(DialogResult == null || DialogResult != JOptionPane.OK_OPTION, CheckScannerConst.JPOS_ECHK_NOCHECK, "No check available");
            byte[] data = new byte[((MaxHeight + Byte.SIZE - 1) / Byte.SIZE) * MaxWidth];   // One bit per dot, vertical orientation
            // Currently, graphic not filled with any contents
            handleEvent(new CheckScannerDataEvent(EventSource, 0, 0, data, null, null, "", "", null));
            DialogResult = null;
        }
    }
}
