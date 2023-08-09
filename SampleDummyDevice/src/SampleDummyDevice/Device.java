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

package SampleDummyDevice;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.checkscanner.*;
import de.gmxhome.conrad.jpos.jpos_base.imagescanner.*;
import jpos.*;
import jpos.config.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JposDevice based dummy implementation, currently for JavaPOS CheckScanner and ImageScanner device service implementation.
 * No real hardware. Scanned images always with dummy values, operator interaction via OptionDialog boxes.<br>
 * Supported configuration values for CheckScanner in jpos.xml are:
 * <ul>
 *     <li>ContrastCheck: The value of a CheckScanner's Contrast property. Must be a value between 0 and 100, default: 50.</li>
 *     <li>MaxCropAreasCheck: The maximum number of crop areas a CheckScanner supports, default: 3.</li>
 *     <li>MaxHeightCheck: The maximum height of a CheckScanner scan in (0.001 inch) units, default: 3000 (3 inch).</li>
 *     <li>MaxWidthCheck: The maximum width of a CheckScanner scan in (0.001 inch) units, default: 6000 (6 inch).</li>
 * </ul>
 * In this simulator implementation, image data for CheckScanner will be returned in a byte array with one bit per dot,
 * starting with the left-most column, with the bit values of the first row in the MSB of the first byte
 * of each row. Depending on the Quality property which can be set to 100 or 300, each column consists of 300
 * or 900 dots stored in 38 or 113 bytes, therefore a full scan with 600 0o 1800 colums consists of 22800 or
 * 203400 byte. <b>Surprise:</b> Since this simulator cannot really scan a check, all dot values will always
 * be 0 (white).<br>
 * Supported configuration values for ImageScanner in jpos.xml are:
 * <ul>
 *     <li>HeightImage: The value of a ImageScanner's image height. Must be a value above 0, default: 500.</li>
 *     <li>WidthImage: The value of a ImageScanner's image width. Must be a value above 0, default: 500.</li>
 *     <li>BitsPerPixelImage: The number of bits per pixel. Must be a value above 0, default: 8.</li>
 *     <li>AimModeImage: Specifies whether an aiming spot will be simulated, default: true.</li>
 *     <li>IlluminateModeImage: Specifies whether illumination will be simulated, default: true.</li>
 *     <li>SessionTimeoutImage: Specifies the maximum session length. Zero means CapHostTriggered becomes false, default:
 *          FOREVER (-1).</li>
 *     <li>ImageType: Specifies the value of ImageType set before data event delivery, one of BMP, JPEG, GIF, PNG or TIFF.
 *          Default: JPEG</li>
 * </ul>
 * In this simulator implementation, image data for ImageScanner will be returned in a byte array with BitsPerPixelImage
 * bit per dot, Depending on the Quality property which can be set to QUAL_LOW, QUAL_MED or QUAL_HIGH, the size
 * of the byte array will be about 1/3, 2/3 or 3/3 or width * height * bits per pixel / 8. But
 * <b>Surprise:</b> Since this simulator cannot really scan an image, all byte values will be 0. This implies that the
 * frame data structure is not as it should be based on the ImageType property.
 */
public class Device extends JposDevice {
    private JposCommonProperties[] ClaimedDevices;
    /**
     * The device implementation. See parent for further details.
     * @param id  Device ID, not used by implementation.
     */
    protected Device(String id) {
        super(id);
        checkScannerInit(1);
        imageScannerInit(1);
        ClaimedDevices = new JposCommonProperties[]{
                null,
                null
        };
        PhysicalDeviceDescription = "Dummy device simulator";
        PhysicalDeviceName = "Dummy Device Simulator";
        CapPowerReporting = JposConst.JPOS_PR_NONE;
    }

    private void checkClaimedByAnyInstance() throws JposException {
        synchronized (ClaimedDevices) {
            for (JposCommonProperties claimed : ClaimedDevices) {
                JposDevice.check(claimed != null, JposConst.JPOS_E_CLAIMED, "Other simulator instance is just claimed");
            }
        }
    }

    @Override
    public void checkProperties(JposEntry entry) throws JposException {
        super.checkProperties(entry);
        try {
            checkCheckScannerProperties(entry);
            checkImageScannerProperties(entry);
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property", e);
        }
    }

    private static interface ConfirmWaiterFinalizer {
        void finish(String what, int result);
    }

    private class ConfirmationWaiter extends Thread {
        private SyncObject Sync;
        private String OperationName;
        private Map<String, Integer> Defaults = new HashMap<String, Integer>();
        private ConfirmWaiterFinalizer Finalizer;
        /**
         * Confirmation result. Is null while confirmation dialog has not been finished. If not null, it holds
         * the index of the selected option, starting at zero. In case of timeout or no selection, it holds the number of
         * options. In case of a stop via abortDialog, it holds -1.
         */
        Integer Result = null;

        /**
         * Constructor for threads that allow waiting for
         * @param name  Thread name
         * @param operation Identifier for operation, one of "DoCheckScan" or "DoImageScan".
         * @param sync  SyncObject, to be signalled whenever the thread has been finished.
         * @param finalizer Finalizer, its finish method will be called after confirmation.
         */
        private ConfirmationWaiter(String name, String operation, SyncObject sync, ConfirmWaiterFinalizer finalizer) {
            setName(name);
            OperationName = operation;
            Sync = sync;
            Finalizer = finalizer;
        }

        private SynchronizedMessageBox Box = new SynchronizedMessageBox();

        @Override
        public void run() {
            if (OperationName.equals("DoCheckScan")) {
                Integer index = Defaults.get(OperationName);
                String defOption = index == null || index < 0 || index >= CheckScannerOptions.length ? null : CheckScannerOptions[index];
                Box.synchronizedConfirmationBox("Scan Simulation Successful?", "Check Scanner", CheckScannerOptions, defOption, JOptionPane.QUESTION_MESSAGE, JposConst.JPOS_FOREVER);
                if (Defaults.containsKey(OperationName))
                    Defaults.remove(OperationName);
                if (Result >= 0 && Result < CheckScannerOptions.length)
                    Defaults.put(OperationName, Result);
            } else if (OperationName.equals("DoImageScan")) {
                Integer index = Defaults.get(OperationName);
                String defOption = index == null || index < 0 || index >= ImageScannerOptions.length ? null : ImageScannerOptions[index];
                Box.synchronizedConfirmationBox("Scan Simulation Successful?", "Image Scanner", ImageScannerOptions, defOption, JOptionPane.QUESTION_MESSAGE, SessionTimeoutImage);
                if (Defaults.containsKey(OperationName))
                    Defaults.remove(OperationName);
                if (Result >= 0 && Result < ImageScannerOptions.length)
                    Defaults.put(OperationName, Result);
            }
            Finalizer.finish(OperationName, Result);
        }
    }

    private ConfirmationWaiter startAsyncOperation(String name, String operation, SyncObject sync, ConfirmWaiterFinalizer finalizer) throws JposException {
        while (sync.suspend(0))
            ;
        ConfirmationWaiter ret = new ConfirmationWaiter(name, operation, sync, finalizer);
        ret.start();
        return ret;
    }

    /*
    CheckScanner Part
     */

    private final static int ClaimedCheckScannerIndex = 0;

    private int MaxHeightCheck = 3000;
    private int MaxWidthCheck = 6000;
    private int ContrastCheck = 50;
    private int MaxCropAreasCheck = 3;

    static private final String[] CheckScannerOptions = {"Yes", "No"};
    static private final int CheckScannerYes = 0;
    static private final int CheckScannerNo = 1;

    private void checkCheckScannerProperties(JposEntry entry) {
        Object o;
        int val;
        if ((o = entry.getPropertyValue("MaxHeightCheck")) != null && (val = Integer.parseInt(o.toString())) > 0)
            MaxHeightCheck = val;
        if ((o = entry.getPropertyValue("MaxWidthCheck")) != null && (val = Integer.parseInt(o.toString())) > 0)
            MaxWidthCheck = val;
        if ((o = entry.getPropertyValue("MaxCropAreasCheck")) != null && (val = Integer.parseInt(o.toString())) > 0)
            MaxCropAreasCheck = val;
        if ((o = entry.getPropertyValue("ContrastCheck")) != null && (val = Integer.parseInt(o.toString())) >= 0 && val <= 100)
            ContrastCheck = val;
    }

    @Override
    public void changeDefaults(CheckScannerProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "Check scanner service for sample dummy device";
        props.DocumentHeightDef = MaxHeightCheck;
        props.DocumentWidthDef = MaxWidthCheck;
        props.CapColor = CheckScannerConst.CHK_CCL_MONO;
        props.CapImageFormat = CheckScannerConst.CHK_CIF_NATIVE;
        props.CapDefineCropArea = true;
        props.ContrastDef = ContrastCheck;    // UPOS specification: Shall be user configurable.
        props.MaxCropAreas = MaxCropAreasCheck;
        props.Quality = 100;
        props.QualityList = "100,300";
    }

    @Override
    public CheckScannerProperties getCheckScannerProperties(int index) {
        return new SampleCheckScannerProperties();
    }

    private class SampleCheckScannerProperties extends CheckScannerProperties implements ConfirmWaiterFinalizer {
        protected SampleCheckScannerProperties() {
            super(0);
            try {
                defineCropArea(CheckScannerConst.CHK_CROP_AREA_RESET_ALL, 0, 0, 0, 0);
            } catch (JposException e) {}
        }

        private ConfirmationWaiter TheWaiter = null;
        private SyncObject SyncObj = new SyncObject();
        Map<Integer, int[]> CropAreas = new HashMap();
        int ScanQuality = 0;

        @Override
        public void claim(int timeout) throws JposException {
            checkClaimedByAnyInstance();
            super.claim(timeout);
            synchronized (ClaimedDevices) {
                ClaimedDevices[ClaimedCheckScannerIndex] = this;
            }
        }

        @Override
        public void release() throws JposException {
            super.release();
            synchronized (ClaimedDevices) {
                ClaimedDevices[ClaimedCheckScannerIndex] = null;
            }
        }

        @Override
        public void deviceEnabled(boolean enable) throws JposException {
            if (enable && !AutoDisable) {
                resetWaiter();
            }
            super.deviceEnabled(enable);
        }

        @Override
        public void beginInsertion(int timeout) throws JposException {
            if (Inserted != null)
                throw new JposException(JposConst.JPOS_E_ILLEGAL, Inserted ? "Check scanned" : "Removal not finished");
            synchronized (SyncObj) {
                if (TheWaiter == null) {
                    TheWaiter = startAsyncOperation(ID + ":CheckScanConfirmWaiter", "DoCheckScan", SyncObj, this);
                }
                ScanQuality = Quality;
            }
            if (!SyncObj.suspend(timeout == JposConst.JPOS_FOREVER ? SyncObject.INFINITE : timeout)) {
                InsertionMode = true;
                throw new JposException(JposConst.JPOS_E_TIMEOUT, "Timeout BeginInsertion");
            }
            check(TheWaiter.Result != CheckScannerYes, JposConst.JPOS_E_FAILURE, "Check Insertion Failed");
        }

        Boolean Inserted = null;

        @Override
        public void endInsertion() throws JposException {
            Integer result = null;
            synchronized (SyncObj) {
                result = TheWaiter.Result;
            }
            if (result == null) {
                SyncObj.suspend(SyncObject.INFINITE);
            }
            if (TheWaiter.Result != CheckScannerYes) {
                resetWaiter();
                throw new JposException(CheckScannerConst.JPOS_ECHK_NOCHECK, "No check available");
            }
            handleEvent(new CheckScannerStatusUpdateEvent(EventSource, CheckScannerConst.CHK_SUE_SCANCOMPLETE));
            Inserted = true;
        }

        private void resetWaiter() throws JposException{
            ConfirmationWaiter waiter = null;
            synchronized (SyncObj) {
                if (TheWaiter != null && TheWaiter.Result == null) {
                    waiter = TheWaiter;
                }
                TheWaiter = null;
            }
            if (waiter != null)
                waiter.Box.abortDialog();
            SyncObj.signal();
            while (SyncObj.suspend(0))
                continue;
        }

        @Override
        public void beginRemoval(int timeout) throws JposException {
            checkext(Inserted == null, CheckScannerConst.JPOS_ECHK_NOCHECK, "No check available");
            Inserted = false;
            resetWaiter();
        }

        @Override
        public void endRemoval() throws JposException {
            checkext(Inserted == null, CheckScannerConst.JPOS_ECHK_NOCHECK, "No check available");
            checkext(Inserted, CheckScannerConst.JPOS_ECHK_CHECK, "Check still available");
            Inserted = null;
        }

        @Override
        public void retrieveImage(int cropAreaID) throws JposException {
            int[] area = CropAreas.get(cropAreaID);
            check(area == null, JposConst.JPOS_E_ILLEGAL, "Invalid cropAreaID: " + cropAreaID);
            checkext(TheWaiter == null || TheWaiter.Result == null || TheWaiter.Result != CheckScannerYes, CheckScannerConst.JPOS_ECHK_NOCHECK, "No check available");
            int factor = getMM_Factor(CheckScannerConst.CHK_MM_ENGLISH);
            // Only cx and cy are relevant for image size
            int scanheight = ((area[3] * ScanQuality + (factor >> 2)) / factor + Byte.SIZE - 1) / Byte.SIZE;
            int scanwidth = (area[2] * ScanQuality + (factor >> 2)) / factor;
            byte[] data = new byte[scanheight * scanwidth];
            // Currently, graphic not filled with any contents. Therefore, x and y are irrelevant.
            handleEvent(new CheckScannerDataEvent(EventSource, 0, 0, data, null, null, "", "", null));
        }

        @Override
        public void defineCropArea(int cropAreaID, int x, int y, int cx, int cy) throws JposException {
            // Store area coordinates in MM_ENGLISH
            if (cropAreaID == CheckScannerConst.CHK_CROP_AREA_RESET_ALL) {
                CropAreas.clear();
                if (CropAreaCount > 0) {
                    CropAreaCount = 0;
                    EventSource.logSet("CropAreaCount");
                }
                CropAreas.put(CheckScannerConst.CHK_CROP_AREA_ENTIRE_IMAGE, new int[]{0, 0, MaxWidthCheck - 1, MaxHeightCheck - 1});
            } else  if (cropAreaID != CheckScannerConst.CHK_CROP_AREA_ENTIRE_IMAGE) {
                check(CropAreaCount == MaxCropAreas, JposConst.JPOS_E_ILLEGAL, "Maximum number of crop areas reached");
                if (CropAreas.containsKey(cropAreaID))
                    CropAreas.remove(cropAreaID);
                int[] factors = new int[]{ getMM_Factor(MapMode), getMM_Factor(CheckScannerConst.CHK_MM_ENGLISH) };
                CropAreas.put(cropAreaID, new int[]{
                        (x * factors[1] + (factors[0] >> 1)) / factors[0],
                        (y * factors[1] + (factors[0] >> 1)) / factors[0],
                        (cx * factors[1] + (factors[0] >> 1)) / factors[0],
                        (cy * factors[1] + (factors[0] >> 1)) / factors[0]
                });
                if (CropAreaCount < CropAreas.size() - 1) {
                    CropAreaCount = CropAreas.size() - 1;
                    EventSource.logSet("CropAreaCount");
                }
            }
        }

        @Override
        public void finish(String what, int result) {
            SyncObj.signal();
        }
    }

    /*
    ImageScanner Part
     */

    private final static int ClaimedImageScannerIndex = 1;

    private int HeightImage = 500;
    private int WidthImage = 500;
    private int BitsPerPixelImage = 8;
    private boolean IlluminateModeImage = true;
    private boolean AimModeImage = true;
    private int SessionTimeoutImage = JposConst.JPOS_FOREVER;
    private int TypeImage = ImageScannerConst.IMG_TYP_JPEG;
    static private final String[] ImageScannerOptions = {"Yes", "No"};
    static private final int ImageScannerYes = 0;
    static private final int ImageScannerNo = 1;
    static private final int ImageScannerTimeout = 2;
    static private final int ImageScannerAbort = -1;

    private void checkImageScannerProperties(JposEntry entry) {
        Object o;
        int val;
        if ((o = entry.getPropertyValue("HeightImage")) != null && (val = Integer.parseInt(o.toString())) > 0)
            HeightImage = val;
        if ((o = entry.getPropertyValue("WidthImage")) != null && (val = Integer.parseInt(o.toString())) > 0)
            WidthImage = val;
        if ((o = entry.getPropertyValue("BitsPerPixelImage")) != null && (val = Integer.parseInt(o.toString())) > 0)
            BitsPerPixelImage = val;
        if ((o = entry.getPropertyValue("SessionTimeoutImage")) != null && ((val = Integer.parseInt(o.toString())) >= 0 || val == JposConst.JPOS_FOREVER))
            SessionTimeoutImage = val;
        if ((o = entry.getPropertyValue("IlluminateModeImage")) != null && (val = Boolean.parseBoolean(o.toString()) ? 1 : -1) != 0)
            IlluminateModeImage = val > 0;
        if ((o = entry.getPropertyValue("AimModeImage")) != null && (val = Boolean.parseBoolean(o.toString()) ? 1 : -1) != 0)
            AimModeImage = val > 0;
        if ((o = entry.getPropertyValue("ImageType")) != null) {
            Object[][] validpairs = {
                    {"BMP", ImageScannerConst.IMG_TYP_BMP},
                    {"JPEG", ImageScannerConst.IMG_TYP_JPEG},
                    {"GIF", ImageScannerConst.IMG_TYP_GIF},
                    {"PNG", ImageScannerConst.IMG_TYP_PNG},
                    {"TIFF", ImageScannerConst.IMG_TYP_TIFF}
            };
            Object value = null;
            for (Object[] pair : validpairs) {
                if (pair[0].equals(o.toString().toUpperCase()))
                    value = pair[1];
            }
            TypeImage = (Integer)value;
        }
    }

    @Override
    public void changeDefaults(ImageScannerProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "Image scanner service for sample dummy device";
        props.AimMode = AimModeImage;
        props.CapHostTriggered = SessionTimeoutImage != 0;
        props.CapIlluminate = true;     // Simulation: No change in processing
        props.CapImageData = true;      // No Video Simulation, no decode that needs hydra device
        props.CapImageQuality = true;   // Simulation: Length of dummy data
        props.IlluminateMode = IlluminateModeImage;
    }

    @Override
    public ImageScannerProperties getImageScannerProperties(int index) {
        return new SampleImageScannerProperties();
    }

    private class SampleImageScannerProperties extends ImageScannerProperties implements ConfirmWaiterFinalizer {
        private boolean InSession = false;
        private ConfirmationWaiter TheWaiter = null;
        private SyncObject SyncObj = new SyncObject();

        protected SampleImageScannerProperties() {
            super(0);
        }

        @Override
        public void claim(int timeout) throws JposException {
            checkClaimedByAnyInstance();
            super.claim(timeout);
            synchronized (ClaimedDevices) {
                ClaimedDevices[ClaimedImageScannerIndex] = this;
            }
        }

        @Override
        public void release() throws JposException {
            super.release();
            synchronized (ClaimedDevices) {
                ClaimedDevices[ClaimedImageScannerIndex] = null;
            }
        }

        @Override
        public void deviceEnabled(boolean enable) throws JposException {
            super.deviceEnabled(enable);
            if (!enable)
                stopSession();
            else if (!CapHostTriggered)
                startSession();
        }

        @Override
        public void startSession() throws JposException {
            synchronized (SyncObj) {
                if (!InSession) {
                    InSession = true;
                    TheWaiter = startAsyncOperation(ID + ":ImageScanConfirmWaiter", "DoImageScan", SyncObj, this);
                }
            }
        }

        @Override
        public void stopSession() throws JposException {
            ConfirmationWaiter waiter = null;
            synchronized (SyncObj) {
                if (InSession) {
                    InSession = false;
                    waiter = TheWaiter;
                    TheWaiter = null;
                }
            }
            if (waiter != null)
                waiter.Box.abortDialog();
        }

        @Override
        public void retryInput() throws JposException {
            super.retryInput();
            startSession();
        }

        @Override
        public void clearInput() throws JposException {
            stopSession();
            super.clearInput();
        }

        @Override
        public void finish(String what, int result) {
            if (ImageMode == ImageScannerConst.IMG_STILL_ONLY) {
                try {
                    synchronized (SyncObj) {
                        InSession = false;
                        TheWaiter = null;
                    }
                    switch (result) {
                        case ImageScannerYes:
                            byte[] framedata = new byte[HeightImage * WidthImage * ((BitsPerPixelImage + Byte.SIZE - 1) / Byte.SIZE) * ImageQuality / 3];
                            handleEvent(new ImageScannerDataEvent(EventSource, 0, framedata, BitsPerPixelImage,
                                    ImageScannerConst.IMG_FRAME_STILL, HeightImage, WidthImage, framedata.length, TypeImage));
                            break;
                        case ImageScannerNo:
                            handleEvent(new JposErrorEvent(EventSource, JposConst.JPOS_E_FAILURE, 0, JposConst.JPOS_EL_INPUT, "Simulated image Scan failed"));
                            break;
                        case ImageScannerTimeout:
                            handleEvent(new JposErrorEvent(EventSource, JposConst.JPOS_E_TIMEOUT, 0, JposConst.JPOS_EL_INPUT, "Session timed out"));
                        case ImageScannerAbort:
                            break;
                        default:
                            handleEvent(new JposErrorEvent(EventSource, JposConst.JPOS_E_ILLEGAL, 0, JposConst.JPOS_EL_INPUT, "Unexpected session end"));
                    }
                } catch (JposException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
