/*
 * Copyright 2019 Martin Conrad
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

import de.gmxhome.conrad.jpos.jpos_base.belt.BeltProperties;
import de.gmxhome.conrad.jpos.jpos_base.billacceptor.BillAcceptorProperties;
import de.gmxhome.conrad.jpos.jpos_base.billdispenser.BillDispenserProperties;
import de.gmxhome.conrad.jpos.jpos_base.biometrics.BiometricsProperties;
import de.gmxhome.conrad.jpos.jpos_base.bumpbar.BumpBarProperties;
import de.gmxhome.conrad.jpos.jpos_base.cashchanger.CashChangerProperties;
import de.gmxhome.conrad.jpos.jpos_base.cashdrawer.CashDrawerProperties;
import de.gmxhome.conrad.jpos.jpos_base.checkscanner.CheckScannerProperties;
import de.gmxhome.conrad.jpos.jpos_base.coinacceptor.CoinAcceptorProperties;
import de.gmxhome.conrad.jpos.jpos_base.devicemonitor.DeviceMonitorProperties;
import de.gmxhome.conrad.jpos.jpos_base.electronicvaluerw.ElectronicValueRWProperties;
import de.gmxhome.conrad.jpos.jpos_base.gate.GateProperties;
import de.gmxhome.conrad.jpos.jpos_base.cat.CATProperties;
import de.gmxhome.conrad.jpos.jpos_base.coindispenser.CoinDispenserProperties;
import de.gmxhome.conrad.jpos.jpos_base.electronicjournal.ElectronicJournalProperties;
import de.gmxhome.conrad.jpos.jpos_base.fiscalprinter.FiscalPrinterProperties;
import de.gmxhome.conrad.jpos.jpos_base.gesturecontrol.GestureControlProperties;
import de.gmxhome.conrad.jpos.jpos_base.graphicdisplay.GraphicDisplayProperties;
import de.gmxhome.conrad.jpos.jpos_base.hardtotals.HardTotalsProperties;
import de.gmxhome.conrad.jpos.jpos_base.imagescanner.ImageScannerProperties;
import de.gmxhome.conrad.jpos.jpos_base.individualrecognition.IndividualRecognitionProperties;
import de.gmxhome.conrad.jpos.jpos_base.itemdispenser.ItemDispenserProperties;
import de.gmxhome.conrad.jpos.jpos_base.keylock.KeylockProperties;
import de.gmxhome.conrad.jpos.jpos_base.lights.LightsProperties;
import de.gmxhome.conrad.jpos.jpos_base.linedisplay.LineDisplayProperties;
import de.gmxhome.conrad.jpos.jpos_base.micr.MICRProperties;
import de.gmxhome.conrad.jpos.jpos_base.motionsensor.MotionSensorProperties;
import de.gmxhome.conrad.jpos.jpos_base.msr.MSRProperties;
import de.gmxhome.conrad.jpos.jpos_base.pinpad.PINPadProperties;
import de.gmxhome.conrad.jpos.jpos_base.pointcardrw.PointCardRWProperties;
import de.gmxhome.conrad.jpos.jpos_base.poskeyboard.POSKeyboardProperties;
import de.gmxhome.conrad.jpos.jpos_base.pospower.POSPowerProperties;
import de.gmxhome.conrad.jpos.jpos_base.posprinter.POSPrinterProperties;
import de.gmxhome.conrad.jpos.jpos_base.remoteorderdisplay.RemoteOrderDisplayProperties;
import de.gmxhome.conrad.jpos.jpos_base.rfidscanner.RFIDScannerProperties;
import de.gmxhome.conrad.jpos.jpos_base.scale.ScaleProperties;
import de.gmxhome.conrad.jpos.jpos_base.scanner.ScannerProperties;
import de.gmxhome.conrad.jpos.jpos_base.signaturecapture.SignatureCaptureProperties;
import de.gmxhome.conrad.jpos.jpos_base.smartcardrw.SmartCardRWProperties;
import de.gmxhome.conrad.jpos.jpos_base.soundplayer.SoundPlayerProperties;
import de.gmxhome.conrad.jpos.jpos_base.soundrecorder.SoundRecorderProperties;
import de.gmxhome.conrad.jpos.jpos_base.speechsynthesis.SpeechSynthesisProperties;
import de.gmxhome.conrad.jpos.jpos_base.toneindicator.ToneIndicatorProperties;
import de.gmxhome.conrad.jpos.jpos_base.videocapture.VideoCaptureProperties;
import de.gmxhome.conrad.jpos.jpos_base.voicerecognition.VoiceRecognitionProperties;
import jpos.JposException;

import java.util.*;

import static jpos.JposConst.JPOS_E_NOSERVICE;

/**
 * Class that contains all device specific implementation interfaces to be derived by device service implementations.
 * For each <i>DeviceClass</i>, it provides
 * <ul>
 *     <li>an adjustment in method noOfPropertySets that counts all property sets bound to the <i>DeviceClass</i>s
 *     array,</li>
 *     <li>an array of property set lists with name <i>DeviceClass</i>s,</li>
 *     <li>an array for claimed property sets with name Claimed<i>DeviceClass</i>,</li>
 *     <li>a method <i>deviceClass</i>Init which initializes <i>DeviceClass</i>s to hold the correct number of lists,</li>
 *     <li>a method changeDefaults(<i>DeviceClass</i>Proterties) which must be overwritten in derived classes to
 *         set the correct property defaults and</li>
 *     <li>a class factory get<i>DeviceClass</i>Properties(index) which returns a device specific property set to be
 *         added to <i>DeviceClass</i>s[index].</li>
 * </ul>
 */
@SuppressWarnings("unused")
public class JposDevice extends JposBaseDevice {
    /**
     * Constructor. Initialize ID. Derived classes must allocate list of list of property sets
     * for all supported device types.
     *
     * @param id Device ID, typically a unique identifier, like the COM port, e.g. "COM1"
     */
    protected JposDevice(String id) {
        super(id);
    }

    /**
     * Helper method to create arrays of Maps or lists
     * @param count   Minimum Dimension of the array.
     * @param entries Zero or more instances of an object of the type to be stored within the array.
     * @return  Array of dimension &ge; count, with entries as first element(s), if any.
     * @param <E> Class of elements of the array.
     */
    @SafeVarargs
    public static <E> E[] getArrayOf(int count, E... entries) {
        if (count <= entries.length)
            return entries;
        return Arrays.copyOf(entries, count);
    }

    @Override
    public int noOfPropertySets() {
        return super.noOfPropertySets() +
                getCount(Belts) +
                getCount(BillAcceptors) +
                getCount(BillDispensers) +
                getCount(Biometricss) +
                getCount(BumpBars) +
                getCount(CashChangers) +
                getCount(CashDrawers) +
                getCount(CATs) +
                getCount(CheckScanners) +
                getCount(CoinAcceptors) +
                getCount(CoinDispensers) +
                getCount(DeviceMonitors) +
                getCount(ElectronicJournals) +
                getCount(ElectronicValueRWs) +
                getCount(FiscalPrinters) +
                getCount(Gates) +
                getCount(GestureControls) +
                getCount(GraphicDisplays) +
                getCount(HardTotalss) +
                getCount(ImageScanners) +
                getCount(IndividualRecognitions) +
                getCount(ItemDispensers) +
                getCount(Keylocks) +
                getCount(Lightss) +
                getCount(LineDisplays) +
                getCount(MICRs) +
                getCount(MotionSensors) +
                getCount(MSRs) +
                getCount(PINPads) +
                getCount(PointCardRWs) +
                getCount(POSKeyboards) +
                getCount(POSPowers) +
                getCount(POSPrinters) +
                getCount(RemoteOrderDisplays) +
                getCount(RFIDScanners) +
                getCount(Scales) +
                getCount(Scanners) +
                getCount(SignatureCaptures) +
                getCount(SmartCardRWs) +
                getCount(SoundPlayers) +
                getCount(SoundRecorders) +
                getCount(SpeechSynthesiss) +
                getCount(ToneIndicators) +
                getCount(VideoCaptures) +
                getCount(VoiceRecognitions);
    }

    /*
     * Belt specific implementations
     */

    /**
     * Set of Set of property sets for Belt devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of belts the device service supports. Each
     * list element contains a list of all property sets owned by BeltService
     * objects belonging to the same belt.
     */
    public List<JposCommonProperties>[] Belts = getArrayOf(0);

    /**
     * Array of belt property sets, one element for each belt the device service
     * supports. Whenever a belt device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public BeltProperties[] ClaimedBelt;

    /**
     * Allocate device specific property set list for belt devices. One list must be allocated for each belt
     * the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxBelt Maximum number of belts that can be controlled by the physical device
     */
    protected void beltInit(int maxBelt) {
        if (Belts.length == 0 && maxBelt > 0) {
            ClaimedBelt = new BeltProperties[maxBelt];
            Belts = getArrayOf(maxBelt);
            for (int i = 0; i < maxBelt; i++) {
                Belts[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support belt services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(BeltProperties props) {
        JposVersion = null;
    }

    /**
     * Returns device implementation of BeltProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of BeltProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public BeltProperties getBeltProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * BillAcceptor specific implementations
     */

    /**
     * Set of Set of property sets for BillAcceptor devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of bill acceptors the device service supports. Each
     * list element contains a list of all property sets owned by BillAcceptorService
     * objects belonging to the same bill acceptor.
     */
    public List<JposCommonProperties>[] BillAcceptors = getArrayOf(0);

    /**
     * Array of bill acceptor property sets, one element for each bill acceptor the device service
     * supports. Whenever a bill acceptor device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public BillAcceptorProperties[] ClaimedBillAcceptor;

    /**
     * Allocate device specific property set list for bill acceptor devices. One list must be allocated for each bill
     * acceptors the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxBillAcceptor Maximum number of bill acceptors that can be controlled by the physical device
     */
    protected void billAcceptorInit(int maxBillAcceptor) {
        if (BillAcceptors.length == 0 && maxBillAcceptor > 0) {
            ClaimedBillAcceptor = new BillAcceptorProperties[maxBillAcceptor];
            BillAcceptors = getArrayOf(maxBillAcceptor);
            for (int i = 0; i < maxBillAcceptor; i++) {
                BillAcceptors[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support bill acceptor services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(BillAcceptorProperties props) {
        JposVersion = null;
    }

    /**
     * Returns device implementation of BillAcceptorProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of BillAcceptorProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public BillAcceptorProperties getBillAcceptorProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * BillDispenser specific implementations
     */

    /**
     * Set of Set of property sets for BillDispenser devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of bill dispensers the device service supports. Each
     * list element contains a list of all property sets owned by BillDispenserService
     * objects belonging to the same bill dispenser.
     */
    public List<JposCommonProperties>[] BillDispensers = getArrayOf(0);

    /**
     * Array of bill dispenser property sets, one element for each bill dispenser the device service
     * supports. Whenever a bill dispenser device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public BillDispenserProperties[] ClaimedBillDispenser;

    /**
     * Allocate device specific property set list for bill dispenser devices. One list must be allocated for each bill dispenser
     * the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxBillDispenser Maximum number of bill dispensers that can be controlled by the physical device
     */
    protected void billDispenserInit(int maxBillDispenser) {
        if (BillDispensers.length == 0 && maxBillDispenser > 0) {
            ClaimedBillDispenser = new BillDispenserProperties[maxBillDispenser];
            BillDispensers = getArrayOf(maxBillDispenser);
            for (int i = 0; i < maxBillDispenser; i++) {
                BillDispensers[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support bill dispenser services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(BillDispenserProperties props) {
        JposVersion = null;
    }

    /**
     * Returns device implementation of BillDispenserProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of BillDispenserProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public BillDispenserProperties getBillDispenserProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * Biometrics specific implementations
     */

    /**
     * Set of Set of property sets for Biometrics devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of biometrics instances the device service supports. Each
     * list element contains a list of all property sets owned by BiometricsService
     * objects belonging to the same biometrics instance.
     */
    public List<JposCommonProperties>[] Biometricss = getArrayOf(0);

    /**
     * Array of biometrics property sets, one element for each biometrics instance the device service
     * supports. Whenever a biometrics device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public BiometricsProperties[] ClaimedBiometrics;

    /**
     * Allocate device specific property set list for biometrics devices. One list must be allocated for each biometrics
     * instance the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxBiometrics Maximum number of biometrics that can be controlled by the physical device
     */
    protected void biometricsInit(int maxBiometrics) {
        if (Biometricss.length == 0 && maxBiometrics > 0) {
            ClaimedBiometrics = new BiometricsProperties[maxBiometrics];
            Biometricss = getArrayOf(maxBiometrics);
            for (int i = 0; i < maxBiometrics; i++) {
                Biometricss[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support biometrics services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(BiometricsProperties props) {
        JposVersion = null;
    }

    /**
     * Returns device implementation of BiometricsProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of BiometricsProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public BiometricsProperties getBiometricsProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * BumpBar specific implementations
     */

    /**
     * Set of Set of property sets for BumpBar devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of bump bars the device service supports. Each
     * list element contains a list of all property sets owned by BumpBarService
     * objects belonging to the same bump bar.
     */
    public List<JposCommonProperties>[] BumpBars = getArrayOf(0);

    /**
     * Array of bump bar property sets, one element for each bump bar the device service
     * supports. Whenever a bump bar device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public BumpBarProperties[] ClaimedBumpBar;

    /**
     * Allocate device specific property set list for bump bar devices. One list must be allocated for each bump
     * bar the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxBumpBar Maximum number of bump bars that can be controlled by the physical device
     */
    protected void bumpBarInit(int maxBumpBar) {
        if (BumpBars.length == 0 && maxBumpBar > 0) {
            ClaimedBumpBar = new BumpBarProperties[maxBumpBar];
            BumpBars = getArrayOf(maxBumpBar);
            for (int i = 0; i < maxBumpBar; i++) {
                BumpBars[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support bump bar services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(BumpBarProperties props) {
        JposVersion = null;
        props.ClearOutputOnDeviceDisable = true;
    }

    /**
     * Returns device implementation of BumpBarProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of BumpBarProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public BumpBarProperties getBumpBarProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * CashChanger specific implementations
     */

    /**
     * Set of Set of property sets for CashChanger devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of cash changers the device service supports. Each
     * list element contains a list of all property sets owned by CashChangerService
     * objects belonging to the same cash changer.
     */
    public List<JposCommonProperties>[] CashChangers = getArrayOf(0);

    /**
     * Array of cash changer property sets, one element for each cash changer the device service
     * supports. Whenever a cash changer device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public CashChangerProperties[] ClaimedCashChanger;

    /**
     * Allocate device specific property set list for cash changer devices. One list must be allocated for each cash
     * changer the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxCashChanger Maximum number of cash changers that can be controlled by the physical device
     */
    protected void cashChangerInit(int maxCashChanger) {
        if (CashChangers.length == 0 && maxCashChanger > 0) {
            ClaimedCashChanger = new CashChangerProperties[maxCashChanger];
            CashChangers = getArrayOf(maxCashChanger);
            for (int i = 0; i < maxCashChanger; i++) {
                CashChangers[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support cash changer services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(CashChangerProperties props) {
        JposVersion = null;
    }

    /**
     * Returns device implementation of CashChangerProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of CashChangerProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public CashChangerProperties getCashChangerProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * CashDrawer specific implementations
     */

    /**
     * Set of Set of property sets for CashDrawer devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of cash drawer the device service supports. Each
     * list element contains a list of all property sets owned by CashDrawerService
     * objects belonging to the same cash drawer.
     */
    public List<JposCommonProperties>[] CashDrawers = getArrayOf(0);

    /**
     * Array of drawer property sets, one element for each cash drawer the device service
     * supports. Whenever a cash drawer device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public CashDrawerProperties[] ClaimedCashDrawer;

    /**
     * Allocate device specific property set list for cash drawer devices. One list must be allocated for each cash
     * drawer the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxDrawer Maximum number of cash drawers that can be controlled by the physical device
     */
    @SuppressWarnings("deprecation")
    protected void cashDrawerInit(int maxDrawer) {
        if (CashDrawers.length == 0 && maxDrawer > 0) {
            ClaimedCashDrawer = new CashDrawerProperties[maxDrawer];
            CashDrawers = getArrayOf(maxDrawer);
            for (int i = 0; i < maxDrawer; i++) {
                CashDrawers[i] = new ArrayList<>(0);
            }
            DrawerBeepVolume = 100; // The default volume
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support cash drawer services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(CashDrawerProperties props) {
        DrawerBeepVolume = props.DrawerBeepVolume;
        JposVersion = null;
    }

    /**
     * Returns device implementation of CashDrawerProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of CashDrawerProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public CashDrawerProperties getCashDrawerProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * CheckScanner specific implementations
     */

    /**
     * Set of Set of property sets for CheckScanner devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of check scanner the device service supports. Each
     * list element contains a list of all property sets owned by CheckScannerService
     * objects belonging to the same check scanner.
     */
    public List<JposCommonProperties>[] CheckScanners = getArrayOf(0);

    /**
     * Array of check scanner property sets, one element for each check scanner the device service
     * supports. Whenever a check scanner device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public CheckScannerProperties[] ClaimedCheckScanner;

    /**
     * Allocate device specific property set list for check scanner devices. One list must be allocated for each check
     * scanner the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxCheckScanner Maximum number of check scanners that can be controlled by the physical device
     */
    protected void checkScannerInit(int maxCheckScanner) {
        if (CheckScanners.length == 0 && maxCheckScanner > 0) {
            ClaimedCheckScanner = new CheckScannerProperties[maxCheckScanner];
            CheckScanners = getArrayOf(maxCheckScanner);
            for (int i = 0; i < maxCheckScanner; i++) {
                CheckScanners[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support check scanner services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(CheckScannerProperties props) {
        JposVersion = null;
    }

    /**
     * Returns device implementation of CheckScannerProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of CheckScannerProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public CheckScannerProperties getCheckScannerProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * CoinAcceptor specific implementations
     */

    /**
     * Set of Set of property sets for CoinAcceptor devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of coin acceptors the device service supports. Each
     * list element contains a list of all property sets owned by CoinAcceptorService
     * objects belonging to the same coin acceptor.
     */
    public List<JposCommonProperties>[] CoinAcceptors = getArrayOf(0);

    /**
     * Array of coin acceptor property sets, one element for each coin acceptor the device service
     * supports. Whenever a coin acceptor device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public CoinAcceptorProperties[] ClaimedCoinAcceptor;

    /**
     * Allocate device specific property set list for coin acceptor devices. One list must be allocated for each coin
     * acceptor the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxCoinAcceptor Maximum number of coin acceptors that can be controlled by the physical device
     */
    protected void coinAcceptorInit(int maxCoinAcceptor) {
        if (CoinAcceptors.length == 0 && maxCoinAcceptor > 0) {
            ClaimedCoinAcceptor = new CoinAcceptorProperties[maxCoinAcceptor];
            CoinAcceptors = getArrayOf(maxCoinAcceptor);
            for (int i = 0; i < maxCoinAcceptor; i++) {
                CoinAcceptors[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support coin acceptor services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(CoinAcceptorProperties props) {
        JposVersion = null;
    }

    /**
     * Returns device implementation of CoinAcceptorProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of CoinAcceptorProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public CoinAcceptorProperties getCoinAcceptorProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * CAT specific implementations
     */

    /**
     * Set of Set of property sets for CAT devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of credit authorization terminals the service supports. Each
     * list element contains a list of all property sets owned by CATService
     * objects belonging to the same credit authorization terminal.
     */
    public List<JposCommonProperties>[] CATs = getArrayOf(0);

    /**
     * Array of electronic journal property sets, one element for each credit authorization terminal the device service
     * supports. Whenever a credit authorization terminal device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public CATProperties[] ClaimedCAT;

    /**
     * Change defaults of properties. Must be implemented within derived classed that support credit authorization terminal services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(CATProperties props) {
        JposVersion = null;
        props.ClearOutputOnDeviceDisable = true;
    }

    /**
     * Allocate device specific property set list for credit authorization terminals. One list must be allocated for each
     * credit authorization terminal the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxCAT Maximum number of credit authorization terminals that can be controlled by the physical device
     */
    protected void cATInit(int maxCAT) {
        if (CATs.length == 0 && maxCAT > 0) {
            ClaimedCAT = new CATProperties[maxCAT];
            CATs = getArrayOf(maxCAT);
            for (int i = 0; i < maxCAT; i++) {
                CATs[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Returns device implementation of CATProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of CATProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public CATProperties getCATProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * CoinDispenser specific implementations
     */

    /**
     * Set of Set of property sets for CoinDispenser devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of coin dispensers the device service supports. Each
     * list element contains a list of all property sets owned by CoinDispenserService
     * objects belonging to the same coin dispenser.
     */
    public List<JposCommonProperties>[] CoinDispensers = getArrayOf(0);

    /**
     * Array of coin dispenser property sets, one element for each coin dispenser the device service
     * supports. Whenever a coin dispenser device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public CoinDispenserProperties[] ClaimedCoinDispenser;

    /**
     * Change defaults of properties. Must be implemented within derived classed that support coin dispenser services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(CoinDispenserProperties props) {
        JposVersion = null;
    }

    /**
     * Allocate device specific property set list for coin dispensers. One list must be allocated for each
     * coin dispenser the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxCoinDispenser Maximum number of coin dispensers that can be controlled by the physical device
     */
    protected void coinDispenserInit(int maxCoinDispenser) {
        if (CoinDispensers.length == 0 && maxCoinDispenser > 0) {
            ClaimedCoinDispenser = new CoinDispenserProperties[maxCoinDispenser];
            CoinDispensers = getArrayOf(maxCoinDispenser);
            for (int i = 0; i < maxCoinDispenser; i++) {
                CoinDispensers[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Returns device implementation of CoinDispenserProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of CoinDispenserProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public CoinDispenserProperties getCoinDispenserProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * DeviceMonitor specific implementations
     */

    /**
     * Set of Set of property sets for DeviceMonitor devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of device monitors the device service supports. Each
     * list element contains a list of all property sets owned by DeviceMonitorService
     * objects belonging to the same device monitor.
     */
    public List<JposCommonProperties>[] DeviceMonitors = getArrayOf(0);

    /**
     * Array of device monitor property sets, one element for each device monitor the device service
     * supports. Whenever a device monitor device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public DeviceMonitorProperties[] ClaimedDeviceMonitor;

    /**
     * Allocate device specific property set list for device monitor devices. One list must be allocated for each device
     * monitor the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxDeviceMonitor Maximum number of device monitors that can be controlled by the physical device
     */
    protected void deviceMonitorInit(int maxDeviceMonitor) {
        if (DeviceMonitors.length == 0 && maxDeviceMonitor > 0) {
            ClaimedDeviceMonitor = new DeviceMonitorProperties[maxDeviceMonitor];
            DeviceMonitors = getArrayOf(maxDeviceMonitor);
            for (int i = 0; i < maxDeviceMonitor; i++) {
                DeviceMonitors[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support device monitor services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(DeviceMonitorProperties props) {
        JposVersion = null;
    }

    /**
     * Returns device implementation of DeviceMonitorProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of DeviceMonitorProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public DeviceMonitorProperties getDeviceMonitorProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * ElectronicJournal specific implementations
     */

    /**
     * Set of Set of property sets for ElectronicJournal devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of electronic journal devices the service supports. Each
     * list element contains a list of all property sets owned by ElectronicJournalService
     * objects belonging to the same electronic journal.
     */
    public List<JposCommonProperties>[] ElectronicJournals = getArrayOf(0);

    /**
     * Array of electronic journal property sets, one element for each electronic journal the device service
     * supports. Whenever an electronic journal device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public ElectronicJournalProperties[] ClaimedElectronicJournal;

    /**
     * Change defaults of properties. Must be implemented within derived classed that support electronic journal services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(ElectronicJournalProperties props) {
        JposVersion = null;
        props.ClearOutputOnDeviceDisable = true;
    }

    /**
     * Allocate device specific property set list for electronic journal devices. One list must be allocated for each
     * electronic journal the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxElectronicJournal Maximum number of electronic journal devices that can be controlled by the physical device
     */
    protected void electronicJournalInit(int maxElectronicJournal) {
        if (ElectronicJournals.length == 0 && maxElectronicJournal > 0) {
            ClaimedElectronicJournal = new ElectronicJournalProperties[maxElectronicJournal];
            ElectronicJournals = getArrayOf(maxElectronicJournal);
            for (int i = 0; i < maxElectronicJournal; i++) {
                ElectronicJournals[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Returns device implementation of ElectronicJournalProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of ElectronicJournalProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public ElectronicJournalProperties getElectronicJournalProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * ElectronicValueRW specific implementations
     */

    /**
     * Set of Set of property sets for ElectronicValueRW devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of electronic value reader / writer devices the service supports. Each
     * list element contains a list of all property sets owned by ElectronicValueRWService
     * objects belonging to the same electronic value reader / writer.
     */
    public List<JposCommonProperties>[] ElectronicValueRWs = getArrayOf(0);

    /**
     * Array of electronic value reader / writer property sets, one element for each electronic value reader / writer
     * the device service supports. Whenever an electronic value reader / writer device will be claimed, the
     * corresponding property set will be stored within this array.
     */
    public ElectronicValueRWProperties[] ClaimedElectronicValueRW;

    /**
     * Change defaults of properties. Must be implemented within derived classed that support electronic value reader /
     * writer services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(ElectronicValueRWProperties props) {
        JposVersion = null;
        props.ClearOutputOnDeviceDisable = true;
    }

    /**
     * Allocate device specific property set list for electronic value reader / writer devices. One list must be
     * allocated for each electronic value reader / writer the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxElectronicValueRW Maximum number of electronic value reader / writer devices that can be controlled by
     *                             the physical device
     */
    protected void electronicValueRWInit(int maxElectronicValueRW) {
        if (ElectronicValueRWs.length == 0 && maxElectronicValueRW > 0) {
            ClaimedElectronicValueRW = new ElectronicValueRWProperties[maxElectronicValueRW];
            ElectronicValueRWs = getArrayOf(maxElectronicValueRW);
            for (int i = 0; i < maxElectronicValueRW; i++) {
                ElectronicValueRWs[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Returns device implementation of ElectronicValueRWProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of ElectronicValueRWProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public ElectronicValueRWProperties getElectronicValueRWProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * FiscalPrinter specific implementations
     */

    /**
     * Set of Set of property sets for FiscalPrinter devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of fiscal printers the service supports. Each
     * list element contains a list of all property sets owned by FiscalPrinterService
     * objects belonging to the same fiscal printer.
     */
    public List<JposCommonProperties>[] FiscalPrinters = getArrayOf(0);

    /**
     * Array of fiscal printer property sets, one element for each fiscal printer the device service
     * supports. Whenever a fiscal printer device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public FiscalPrinterProperties[] ClaimedFiscalPrinter;

    /**
     * Change defaults of properties. Must be implemented within derived classed that support fiscal printer services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(FiscalPrinterProperties props) {
        JposVersion = null;
        props.ClearOutputOnDeviceDisable = true;
    }

    /**
     * Allocate device specific property set list for fiscal printers. One list must be allocated for each
     * fiscal printer the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxFiscalPrinter Maximum number of fiscal printers that can be controlled by the physical device
     */
    protected void fiscalPrinterInit(int maxFiscalPrinter) {
        if (FiscalPrinters.length == 0 && maxFiscalPrinter > 0) {
            ClaimedFiscalPrinter = new FiscalPrinterProperties[maxFiscalPrinter];
            FiscalPrinters = getArrayOf(maxFiscalPrinter);
            for (int i = 0; i < maxFiscalPrinter; i++) {
                FiscalPrinters[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Returns device implementation of FiscalPrinterProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of FiscalPrinterProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public FiscalPrinterProperties getFiscalPrinterProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * Gate specific implementations
     */

    /**
     * Set of Set of property sets for Gate devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of gates the service supports. Each
     * list element contains a list of all property sets owned by GateService
     * objects belonging to the same gate.
     */
    public List<JposCommonProperties>[] Gates = getArrayOf(0);

    /**
     * Array of gate property sets, one element for each gate the device service
     * supports. Whenever a gate device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public GateProperties[] ClaimedGate;

    /**
     * Change defaults of properties. Must be implemented within derived classed that support gate services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(GateProperties props) {
        JposVersion = null;
    }

    /**
     * Allocate device specific property set list for gates. One list must be allocated for each
     * gate the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxGate Maximum number of gates that can be controlled by the physical device
     */
    protected void gateInit(int maxGate) {
        if (Gates.length == 0 && maxGate > 0) {
            ClaimedGate = new GateProperties[maxGate];
            Gates = getArrayOf(maxGate);
            for (int i = 0; i < maxGate; i++) {
                Gates[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Returns device implementation of GateProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of GateProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public GateProperties getGateProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * GestureControl specific implementations
     */

    /**
     * Set of Set of property sets for GestureControl devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of gesture controls the device service supports. Each
     * list element contains a list of all property sets owned by GestureControlService
     * objects belonging to the same gesture control.
     */
    public List<JposCommonProperties>[] GestureControls = getArrayOf(0);

    /**
     * Array of gesture control property sets, one element for each gesture control the device service
     * supports. Whenever a gesture control device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public GestureControlProperties[] ClaimedGestureControl;

    /**
     * Allocate device specific property set list for gesture control devices. One list must be allocated for each gesture
     * control the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxGestureControl Maximum number of gesture controls that can be controlled by the physical device
     */
    protected void gestureControlInit(int maxGestureControl) {
        if (GestureControls.length == 0 && maxGestureControl > 0) {
            ClaimedGestureControl = new GestureControlProperties[maxGestureControl];
            GestureControls = getArrayOf(maxGestureControl);
            for (int i = 0; i < maxGestureControl; i++) {
                GestureControls[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support gesture control services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(GestureControlProperties props) {
        JposVersion = null;
        props.ClearOutputOnDeviceDisable = true;
    }

    /**
     * Returns device implementation of GestureControlProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of GestureControlProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public GestureControlProperties getGestureControlProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * GraphicDisplay specific implementations
     */

    /**
     * Set of Set of property sets for GraphicDisplay devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of graphic displays the device service supports. Each
     * list element contains a list of all property sets owned by GraphicDisplayService
     * objects belonging to the same graphic display.
     */
    public List<JposCommonProperties>[] GraphicDisplays = getArrayOf(0);

    /**
     * Array of graphic display property sets, one element for each graphic display the device service
     * supports. Whenever a graphic display device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public GraphicDisplayProperties[] ClaimedGraphicDisplay;

    /**
     * Allocate device specific property set list for graphic display devices. One list must be allocated for each graphic
     * display the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxGraphicDisplay Maximum number of graphic displays that can be controlled by the physical device
     */
    protected void graphicDisplayInit(int maxGraphicDisplay) {
        if (GraphicDisplays.length == 0 && maxGraphicDisplay > 0) {
            ClaimedGraphicDisplay = new GraphicDisplayProperties[maxGraphicDisplay];
            GraphicDisplays = getArrayOf(maxGraphicDisplay);
            for (int i = 0; i < maxGraphicDisplay; i++) {
                GraphicDisplays[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support graphic display services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(GraphicDisplayProperties props) {
        JposVersion = null;
        props.ClearOutputOnDeviceDisable = true;
    }

    /**
     * Returns device implementation of GraphicDisplayProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of GraphicDisplayProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public GraphicDisplayProperties getGraphicDisplayProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * HardTotals specific implementations
     */

    /**
     * Set of Set of property sets for HardTotals devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of hard totals instances the service supports. Each
     * list element contains a list of all property sets owned by HardTotalsService
     * objects belonging to the same hard totals instance.
     */
    public List<JposCommonProperties>[] HardTotalss = getArrayOf(0);

    /**
     * Array of hard totals property sets, one element for each hard totals instance the device service
     * supports. Whenever a hard totals device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public HardTotalsProperties[] ClaimedHardTotals;

    /**
     * Change defaults of properties. Must be implemented within derived classed that support hard totals services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(HardTotalsProperties props) {
        JposVersion = null;
    }

    /**
     * Allocate device specific property set list for hard totals instances. One list must be allocated for each
     * hard totals instance the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxHardTotals Maximum number of hard totals instances that can be controlled by the physical device
     */
    protected void hardTotalsInit(int maxHardTotals) {
        if (HardTotalss.length == 0 && maxHardTotals > 0) {
            ClaimedHardTotals = new HardTotalsProperties[maxHardTotals];
            HardTotalss = getArrayOf(maxHardTotals);
            for (int i = 0; i < maxHardTotals; i++) {
                HardTotalss[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Returns device implementation of HardTotalsProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of HardTotalsProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public HardTotalsProperties getHardTotalsProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * ImageScanner specific implementations
     */

    /**
     * Set of Set of property sets for ImageScanner devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of image scanners the service supports. Each
     * list element contains a list of all property sets owned by ImageScannerService
     * objects belonging to the same image scanner.
     */
    public List<JposCommonProperties>[] ImageScanners = getArrayOf(0);

    /**
     * Array of image scanner property sets, one element for each image scanner the device service
     * supports. Whenever a image scanner device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public ImageScannerProperties[] ClaimedImageScanner;

    /**
     * Change defaults of properties. Must be implemented within derived classed that support image scanner services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(ImageScannerProperties props) {
        JposVersion = null;
    }

    /**
     * Allocate device specific property set list for image scanners. One list must be allocated for each
     * image scanner the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxImageScanner Maximum number of image scanners that can be controlled by the physical device
     */
    protected void imageScannerInit(int maxImageScanner) {
        if (ImageScanners.length == 0 && maxImageScanner > 0) {
            ClaimedImageScanner = new ImageScannerProperties[maxImageScanner];
            ImageScanners = getArrayOf(maxImageScanner);
            for (int i = 0; i < maxImageScanner; i++) {
                ImageScanners[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Returns device implementation of ImageScannerProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of ImageScannerProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public ImageScannerProperties getImageScannerProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * IndividualRecognition specific implementations
     */

    /**
     * Set of Set of property sets for IndividualRecognition devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of individual recognitions the device service supports. Each
     * list element contains a list of all property sets owned by IndividualRecognitionService
     * objects belonging to the same individual recognition.
     */
    public List<JposCommonProperties>[] IndividualRecognitions = getArrayOf(0);

    /**
     * Array of individual recognition property sets, one element for each individual recognition the device service
     * supports. Whenever a individual recognition device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public IndividualRecognitionProperties[] ClaimedIndividualRecognition;

    /**
     * Allocate device specific property set list for individual recognition devices. One list must be allocated for each
     * individual recognition the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxIndividualRecognition Maximum number of individual recognitions that can be controlled by the physical device
     */
    protected void individualRecognitionInit(int maxIndividualRecognition) {
        if (IndividualRecognitions.length == 0 && maxIndividualRecognition > 0) {
            ClaimedIndividualRecognition = new IndividualRecognitionProperties[maxIndividualRecognition];
            IndividualRecognitions = getArrayOf(maxIndividualRecognition);
            for (int i = 0; i < maxIndividualRecognition; i++) {
                IndividualRecognitions[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support individual recognition
     * services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(IndividualRecognitionProperties props) {
        JposVersion = null;
    }

    /**
     * Returns device implementation of IndividualRecognitionProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of IndividualRecognitionProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public IndividualRecognitionProperties getIndividualRecognitionProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * ItemDispenser specific implementations
     */

    /**
     * Set of Set of property sets for ItemDispenser devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of item dispensers the service supports. Each
     * list element contains a list of all property sets owned by ItemDispenserService
     * objects belonging to the same item dispenser.
     */
    public List<JposCommonProperties>[] ItemDispensers = getArrayOf(0);

    /**
     * Array of item dispenser property sets, one element for each item dispenser the device service
     * supports. Whenever an item dispenser device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public ItemDispenserProperties[] ClaimedItemDispenser;

    /**
     * Change defaults of properties. Must be implemented within derived classed that support item dispenser services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(ItemDispenserProperties props) {
        JposVersion = null;
    }

    /**
     * Allocate device specific property set list for item dispensers. One list must be allocated for each
     * item dispenser the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxItemDispenser Maximum number of item dispensers that can be controlled by the physical device
     */
    protected void itemDispenserInit(int maxItemDispenser) {
        if (ItemDispensers.length == 0 && maxItemDispenser > 0) {
            ClaimedItemDispenser = new ItemDispenserProperties[maxItemDispenser];
            ItemDispensers = getArrayOf(maxItemDispenser);
            for (int i = 0; i < maxItemDispenser; i++) {
                ItemDispensers[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Returns device implementation of ItemDispenserProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of ItemDispenserProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public ItemDispenserProperties getItemDispenserProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * Keylock specific implementations
     */

    /**
     * Set of Set of property sets for Keylock devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of key locks the device service supports. Each
     * list element contains a list of all property sets owned by KeylockService
     * objects belonging to the same key lock.
     */
    public List<JposCommonProperties>[] Keylocks = getArrayOf(0);

    /**
     * Array of key lock property sets, one element for each key lock the device service
     * supports. Whenever a key lock device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public KeylockProperties[] ClaimedKeylock;

    /**
     * Allocate device specific property set list for keylock devices. One list must be allocated for each keylock the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxKeylock Maximum number of keylocks that can be controlled by the physical device
     */
    protected void keylockInit(int maxKeylock) {
        if (Keylocks.length == 0 && maxKeylock > 0) {
            ClaimedKeylock = new KeylockProperties[maxKeylock];
            Keylocks = getArrayOf(maxKeylock);
            for (int i = 0; i < maxKeylock; i++) {
                Keylocks[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support keylock services.
     *
     * @param props Property set for setting the propperty defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(KeylockProperties props) {
        JposVersion = null;
    }

    /**
     * Returns device implementation of KeylockProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of KeylockProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public KeylockProperties getKeylockProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * Lights specific implementations
     */

    /**
     * Set of Set of property sets for Lights devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of lights the service supports. Each
     * list element contains a list of all property sets owned by LightsService
     * objects belonging to the same lights.
     */
    public List<JposCommonProperties>[] Lightss = getArrayOf(0);

    /**
     * Array of lights property sets, one element for each lights the device service
     * supports. Whenever a lights device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public LightsProperties[] ClaimedLights;

    /**
     * Change defaults of properties. Must be implemented within derived classed that support lights services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(LightsProperties props) {
        JposVersion = null;
    }

    /**
     * Allocate device specific property set list for lights. One list must be allocated for each
     * lights device the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxLights Maximum number of lights that can be controlled by the physical device
     */
    protected void lightsInit(int maxLights) {
        if (Lightss.length == 0 && maxLights > 0) {
            ClaimedLights = new LightsProperties[maxLights];
            Lightss = getArrayOf(maxLights);
            for (int i = 0; i < maxLights; i++) {
                Lightss[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Returns device implementation of LightsProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of LightsProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public LightsProperties getLightsProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * LineDisplay specific implementations
     */

    /**
     * Set of Set of property sets for LineDisplay devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of line displays the device service supports. Each
     * list element contains a list of all property sets owned by LineDisplayService
     * objects belonging to the same line display.
     */
    public List<JposCommonProperties>[] LineDisplays = getArrayOf(0);

    /**
     * Array of line display property sets, one element for each line display the device service
     * supports. Whenever a line display device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public LineDisplayProperties[] ClaimedLineDisplay;

    /**
     * Allocate device specific property set list for line display devices. One list must be allocated for each line display the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxDisplay Maximum number of line displays that can be controlled by the physical device
     */
    protected void lineDisplayInit(int maxDisplay) {
        if (LineDisplays.length == 0 && maxDisplay > 0) {
            ClaimedLineDisplay = new LineDisplayProperties[maxDisplay];
            LineDisplays = getArrayOf(maxDisplay);
            for (int i = 0; i < maxDisplay; i++) {
                LineDisplays[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support line display services.
     *
     * @param props Property set for setting the propperty defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(LineDisplayProperties props) {
        JposVersion = null;
    }

    /**
     * Returns device implementation of LineDisplayProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of LineDisplayProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public LineDisplayProperties getLineDisplayProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * MICR specific implementations
     */

    /**
     * Set of Set of property sets for MICR devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of magnetic ink character recognition readers the service supports. Each
     * list element contains a list of all property sets owned by MICRService
     * objects belonging to the same magnetic ink character recognition reader.
     */
    public List<JposCommonProperties>[] MICRs = getArrayOf(0);

    /**
     * Array of electronic journal property sets, one element for each magnetic ink character recognition reader the device service
     * supports. Whenever a magnetic ink character recognition reader device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public MICRProperties[] ClaimedMICR;

    /**
     * Change defaults of properties. Must be implemented within derived classed that support magnetic ink character recognition reader services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(MICRProperties props) {
        JposVersion = null;
    }

    /**
     * Allocate device specific property set list for magnetic inc character recognition reader (micr) devices. One list
     * must be allocated for each micr device the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxMICR Maximum number of micr devices that can be controlled by the physical device
     */
    protected void mICRInit(int maxMICR) {
        if (MICRs.length == 0 && maxMICR > 0) {
            ClaimedMICR = new MICRProperties[maxMICR];
            MICRs = getArrayOf(maxMICR);
            for (int i = 0; i < maxMICR; i++) {
                MICRs[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Returns device implementation of MICRProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of MICRProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public MICRProperties getMICRProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * MotionSensor specific implementations
     */

    /**
     * Set of Set of property sets for MotionSensor devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of motion sensors the service supports. Each
     * list element contains a list of all property sets owned by MotionSensorService
     * objects belonging to the same motion sensor.
     */
    public List<JposCommonProperties>[] MotionSensors = getArrayOf(0);

    /**
     * Array of motion sensor property sets, one element for each motion sensor the device service
     * supports. Whenever a motion sensor device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public MotionSensorProperties[] ClaimedMotionSensor;

    /**
     * Change defaults of properties. Must be implemented within derived classed that support motion sensor services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(MotionSensorProperties props) {
        JposVersion = null;
    }

    /**
     * Allocate device specific property set list for motion sensors. One list must be allocated for each
     * motion sensor the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxMotionSensor Maximum number of motion sensors that can be controlled by the physical device
     */
    protected void motionSensorInit(int maxMotionSensor) {
        if (MotionSensors.length == 0 && maxMotionSensor > 0) {
            ClaimedMotionSensor = new MotionSensorProperties[maxMotionSensor];
            MotionSensors = getArrayOf(maxMotionSensor);
            for (int i = 0; i < maxMotionSensor; i++) {
                MotionSensors[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Returns device implementation of MotionSensorProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of MotionSensorProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public MotionSensorProperties getMotionSensorProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * MSR specific implementations
     */

    /**
     * Set of Set of property sets for MSR devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of magnetic stripe readers the device service supports. Each
     * list element contains a list of all property sets owned by MSRService
     * objects belonging to the same magnetic stripe reader.
     */
    public List<JposCommonProperties>[] MSRs = getArrayOf(0);

    /**
     * Array of msr property sets, one element for each MSR the device service
     * supports. Whenever a msr device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public MSRProperties[] ClaimedMSR;

    /**
     * Change defaults of properties. Must be implemented within derived classed that support magnetic stripe reader services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(MSRProperties props) {
        JposVersion = null;
    }

    /**
     * Allocate device specific property set list for magnetic stripe reader (msr) devices. One list must be allocated
     * for each msr the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxMsr Maximum number of magnetic stripe readers that can be controlled by the physical device
     */
    protected void mSRInit(int maxMsr) {
        if (MSRs.length == 0 && maxMsr > 0) {
            ClaimedMSR = new MSRProperties[maxMsr];
            MSRs = getArrayOf(maxMsr);
            for (int i = 0; i < maxMsr; i++) {
                MSRs[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Returns device implementation of MSRProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of MSRProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public MSRProperties getMSRProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * PINPad specific implementations
     */

    /**
     * Set of Set of property sets for PINPad devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of PIN pads the service supports. Each
     * list element contains a list of all property sets owned by PINPadService
     * objects belonging to the same PIN pad.
     */
    public List<JposCommonProperties>[] PINPads = getArrayOf(0);

    /**
     * Array of PIN pad property sets, one element for each PIN pad the device service
     * supports. Whenever a PIN pad device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public PINPadProperties[] ClaimedPINPad;

    /**
     * Change defaults of properties. Must be implemented within derived classed that support PIN pad services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(PINPadProperties props) {
        JposVersion = null;
    }

    /**
     * Allocate device specific property set list for PIN pads. One list must be allocated for each
     * PIN pad the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxPINPad Maximum number of PIN pads that can be controlled by the physical device
     */
    protected void pINPadInit(int maxPINPad) {
        if (PINPads.length == 0 && maxPINPad > 0) {
            ClaimedPINPad = new PINPadProperties[maxPINPad];
            PINPads = getArrayOf(maxPINPad);
            for (int i = 0; i < maxPINPad; i++) {
                PINPads[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Returns device implementation of PINPadProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of PINPadProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public PINPadProperties getPINPadProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * PointCardRW specific implementations
     */

    /**
     * Set of Set of property sets for PointCardRW devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of point card reader / writers the service supports. Each
     * list element contains a list of all property sets owned by PointCardRWService
     * objects belonging to the same point card reader / writer.
     */
    public List<JposCommonProperties>[] PointCardRWs = getArrayOf(0);

    /**
     * Array of point card reader / writer property sets, one element for each point card reader / writer the device service
     * supports. Whenever a point card reader / writer device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public PointCardRWProperties[] ClaimedPointCardRW;

    /**
     * Change defaults of properties. Must be implemented within derived classed that support point card reader / writer services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(PointCardRWProperties props) {
        JposVersion = null;
        props.ClearOutputOnDeviceDisable = true;
    }

    /**
     * Allocate device specific property set list for point card reader / writers. One list must be allocated for each
     * point card reader / writer the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxPointCardRW Maximum number of point card reader / writers that can be controlled by the physical device
     */
    protected void pointCardRWInit(int maxPointCardRW) {
        if (PointCardRWs.length == 0 && maxPointCardRW > 0) {
            ClaimedPointCardRW = new PointCardRWProperties[maxPointCardRW];
            PointCardRWs = getArrayOf(maxPointCardRW);
            for (int i = 0; i < maxPointCardRW; i++) {
                PointCardRWs[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Returns device implementation of PointCardRWProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of PointCardRWProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public PointCardRWProperties getPointCardRWProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * POSKeyboard specific implementations
     */

    /**
     * Set of Set of property sets for POSKeyboard devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of keyboards the device service supports. Each
     * list element contains a list of all property sets owned by POSKeyboardService
     * objects belonging to the same keyboard.
     */
    public List<JposCommonProperties>[] POSKeyboards = getArrayOf(0);

    /**
     * Array of keyboard property sets, one element for each POS keyboard the device service
     * supports. Whenever a keyboard device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public POSKeyboardProperties[] ClaimedPOSKeyboard;

    /**
     * Allocate device specific property set list for POS keyboard devices. One list must be allocated for each POS keyboard the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxKeyboard Maximum number of POS keyboards that can be controlled by the physical device
     */
    protected void pOSKeyboardInit(int maxKeyboard) {
        if (POSKeyboards.length == 0 && maxKeyboard > 0) {
            ClaimedPOSKeyboard = new POSKeyboardProperties[maxKeyboard];
            POSKeyboards = getArrayOf(maxKeyboard);
            for (int i = 0; i < maxKeyboard; i++) {
                POSKeyboards[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support POS keyboard services.
     *
     * @param props Property set for setting the propperty defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(POSKeyboardProperties props) {
        JposVersion = null;
    }

    /**
     * Returns device implementation of POSKeyboardProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of POSKeyboardProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public POSKeyboardProperties getPOSKeyboardProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * POSPower specific implementations
     */

    /**
     * Set of Set of property sets for POSPower devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of POS power devices  the device service supports. Each
     * list element contains a list of all property sets owned by POSPowerService
     * objects belonging to the same POS power device.
     */
    public List<JposCommonProperties>[] POSPowers = getArrayOf(0);

    /**
     * Array of POS power property sets, one element for each POS power device the device service
     * supports. Whenever a POS power device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public POSPowerProperties[] ClaimedPOSPower;

    /**
     * Allocate device specific property set list for POS power devices. One list must be allocated for each POS power
     * device the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxPower Maximum number of POS power devices that can be controlled by the physical device
     */
    protected void pOSPowerInit(int maxPower) {
        if (POSPowers.length == 0 && maxPower > 0) {
            ClaimedPOSPower = new POSPowerProperties[maxPower];
            POSPowers = getArrayOf(maxPower);
            for (int i = 0; i < maxPower; i++) {
                POSPowers[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support POS power services.
     *
     * @param props Property set for setting the propperty defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(POSPowerProperties props) {
        JposVersion = null;
    }

    /**
     * Returns device implementation of POSPowerProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of POSPowerProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public POSPowerProperties getPOSPowerProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * POSPrinter specific implementations
     */

    /**
     * Set of Set of property sets for POSPrinter devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of POS printers the device service supports. Each
     * list element contains a list of all property sets owned by POSPrinterService
     * objects belonging to the same POS printer.
     */
    public List<JposCommonProperties>[] POSPrinters = getArrayOf(0);

    /**
     * Array of POS printer property sets, one element for each POS printer the device service
     * supports. Whenever a POS printer device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public POSPrinterProperties[] ClaimedPOSPrinter;

    /**
     * Change defaults of properties. Must be implemented within derived classed that support POS printer services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(POSPrinterProperties props) {
        JposVersion = null;
        props.ClearOutputOnDeviceDisable = true;
    }

    /**
     * Allocate device specific property set list for POS printers. One list must be allocated for each
     * POS printer the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxPrinters Maximum number of POS printers that can be controlled by the physical device
     */
    protected void pOSPrinterInit(int maxPrinters) {
        if (POSPrinters.length == 0 && maxPrinters > 0) {
            ClaimedPOSPrinter = new POSPrinterProperties[maxPrinters];
            POSPrinters = getArrayOf(maxPrinters);
            for (int i = 0; i < maxPrinters; i++) {
                POSPrinters[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Returns device implementation of POSPrinterProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of POSPrinterProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public POSPrinterProperties getPOSPrinterProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * RemoteOrderDisplay specific implementations
     */

    /**
     * Set of Set of property sets for RemoteOrderDisplay devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of remote order displays the service supports. Each
     * list element contains a list of all property sets owned by RemoteOrderDisplayService
     * objects belonging to the same remote order display.
     */
    public List<JposCommonProperties>[] RemoteOrderDisplays = getArrayOf(0);

    /**
     * Array of remote order display property sets, one element for each remote order display the device service
     * supports. Whenever a remote order display device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public RemoteOrderDisplayProperties[] ClaimedRemoteOrderDisplay;

    /**
     * Change defaults of properties. Must be implemented within derived classed that support remote order display services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(RemoteOrderDisplayProperties props) {
        JposVersion = null;
        props.ClearOutputOnDeviceDisable = true;
    }

    /**
     * Allocate device specific property set list for remote order display devices. One list must be allocated for each
     * remote order display the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxRODisplay Maximum number of remote order displays that can be controlled by the physical device
     */
    protected void remoteOrderDisplayInit(int maxRODisplay) {
        if (RemoteOrderDisplays.length == 0 && maxRODisplay > 0) {
            ClaimedRemoteOrderDisplay = new RemoteOrderDisplayProperties[maxRODisplay];
            RemoteOrderDisplays = getArrayOf(maxRODisplay);
            for (int i = 0; i < maxRODisplay; i++) {
                RemoteOrderDisplays[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Returns device implementation of RemoteOrderDisplayProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of RemoteOrderDisplayProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public RemoteOrderDisplayProperties getRemoteOrderDisplayProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * RFIDScanner specific implementations
     */

    /**
     * Set of Set of property sets for RFIDScanner devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of RFID scanners the service supports. Each
     * list element contains a list of all property sets owned by RFIDScannerService
     * objects belonging to the same RFID scanner.
     */
    public List<JposCommonProperties>[] RFIDScanners = getArrayOf(0);

    /**
     * Array of RFID scanner property sets, one element for each RFID scanner the device service
     * supports. Whenever a RFID scanner device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public RFIDScannerProperties[] ClaimedRFIDScanner;

    /**
     * Change defaults of properties. Must be implemented within derived classed that support RFID scanner services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(RFIDScannerProperties props) {
        JposVersion = null;
        props.ClearOutputOnDeviceDisable = true;
    }

    /**
     * Allocate device specific property set list for RFID scanners. One list must be allocated for each
     * RFID scanner the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxRFIDScanner Maximum number of RFID scanners that can be controlled by the physical device
     */
    protected void rFIDScannerInit(int maxRFIDScanner) {
        if (RFIDScanners.length == 0 && maxRFIDScanner > 0) {
            ClaimedRFIDScanner = new RFIDScannerProperties[maxRFIDScanner];
            RFIDScanners = getArrayOf(maxRFIDScanner);
            for (int i = 0; i < maxRFIDScanner; i++) {
                RFIDScanners[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Returns device implementation of RFIDScannerProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of RFIDScannerProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public RFIDScannerProperties getRFIDScannerProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * Scale specific implementations
     */

    /**
     * Set of Set of property sets for Scale devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of scales the device service supports. Each
     * list element contains a list of all property sets owned by ScaleService
     * objects belonging to the same scale.
     */
    public List<JposCommonProperties>[] Scales = getArrayOf(0);

    /**
     * Array of Scale property sets, one element for each scale the device service
     * supports. Whenever a scale device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public ScaleProperties[] ClaimedScale;

    /**
     * Change defaults of properties. Must be implemented within derived classed that support scale services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(ScaleProperties props) {
        JposVersion = null;
    }

    /**
     * Allocate device specific property set list for scale devices. One list must be allocated for each
     * scale the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxScales Maximum number of scales that can be controlled by the physical device
     */
    protected void scaleInit(int maxScales) {
        if (Scales.length == 0 && maxScales > 0) {
            ClaimedScale = new ScaleProperties[maxScales];
            Scales = getArrayOf(maxScales);
            for (int i = 0; i < maxScales; i++) {
                Scales[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Returns device implementation of ScaleProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of ScaleProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public ScaleProperties getScaleProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * Scanner specific implementations
     */

    /**
     * Set of Set of property sets for Scanner devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of scanners the device service supports. Each
     * list element contains a list of all property sets owned by ScannerService
     * objects belonging to the same scanner.
     */
    public List<JposCommonProperties>[] Scanners = getArrayOf(0);

    /**
     * Array of scanner property sets, one element for each scanner the device service
     * supports. Whenever a scanner device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public ScannerProperties[] ClaimedScanner;

    /**
     * Allocate device specific property set list for scanner devices. One list must be allocated for each scanner the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxScanner Maximum number of scanners that can be controlled by the physical device
     */
    protected void scannerInit(int maxScanner) {
        if (Scanners.length == 0 && maxScanner > 0) {
            ClaimedScanner = new ScannerProperties[maxScanner];
            Scanners = getArrayOf(maxScanner);
            for (int i = 0; i < maxScanner; i++) {
                Scanners[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support scanner services.
     *
     * @param props Property set for setting the propperty defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(ScannerProperties props) {
        JposVersion = null;
    }

    /**
     * Returns device implementation of ScannerProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of ScannerProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public ScannerProperties getScannerProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * SignatureCapture specific implementations
     */

    /**
     * Set of Set of property sets for SignatureCapture devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of signature capture devices the service supports. Each
     * list element contains a list of all property sets owned by SignatureCaptureService
     * objects belonging to the same signature capture device.
     */
    public List<JposCommonProperties>[] SignatureCaptures = getArrayOf(0);

    /**
     * Array of signature capture property sets, one element for each signature capture device the device service
     * supports. Whenever a signature capture device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public SignatureCaptureProperties[] ClaimedSignatureCapture;

    /**
     * Change defaults of properties. Must be implemented within derived classed that support signature capture services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(SignatureCaptureProperties props) {
        JposVersion = null;
    }

    /**
     * Allocate device specific property set list for signature capture devices. One list must be allocated for each
     * signature capture device the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxSignatureCapture Maximum number of signature capture devices that can be controlled by the physical device
     */
    protected void signatureCaptureInit(int maxSignatureCapture) {
        if (SignatureCaptures.length == 0 && maxSignatureCapture > 0) {
            ClaimedSignatureCapture = new SignatureCaptureProperties[maxSignatureCapture];
            SignatureCaptures = getArrayOf(maxSignatureCapture);
            for (int i = 0; i < maxSignatureCapture; i++) {
                SignatureCaptures[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Returns device implementation of SignatureCaptureProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of SignatureCaptureProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public SignatureCaptureProperties getSignatureCaptureProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * SmartCardRW specific implementations
     */

    /**
     * Set of Set of property sets for SmartCardRW devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of smart card reader / writers the service supports. Each
     * list element contains a list of all property sets owned by SmartCardRWService
     * objects belonging to the same smart card reader / writer.
     */
    public List<JposCommonProperties>[] SmartCardRWs = getArrayOf(0);

    /**
     * Array of smart card reader / writer property sets, one element for each smart card reader / writer the device service
     * supports. Whenever a smart card reader / writer device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public SmartCardRWProperties[] ClaimedSmartCardRW;

    /**
     * Change defaults of properties. Must be implemented within derived classed that support smart card reader / writer services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(SmartCardRWProperties props) {
        JposVersion = null;
        props.ClearOutputOnDeviceDisable = true;
    }

    /**
     * Allocate device specific property set list for smart card reader / writers. One list must be allocated for each
     * smart card reader / writer the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxSmartCardRW Maximum number of smart card reader / writers that can be controlled by the physical device
     */
    protected void smartCardRWInit(int maxSmartCardRW) {
        if (SmartCardRWs.length == 0 && maxSmartCardRW > 0) {
            ClaimedSmartCardRW = new SmartCardRWProperties[maxSmartCardRW];
            SmartCardRWs = getArrayOf(maxSmartCardRW);
            for (int i = 0; i < maxSmartCardRW; i++) {
                SmartCardRWs[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Returns device implementation of SmartCardRWProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of SmartCardRWProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public SmartCardRWProperties getSmartCardRWProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * SoundPlayer specific implementations
     */

    /**
     * Set of Set of property sets for SoundPlayer devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of sound players the device service supports. Each
     * list element contains a list of all property sets owned by SoundPlayerService
     * objects belonging to the same sound player.
     */
    public List<JposCommonProperties>[] SoundPlayers = getArrayOf(0);

    /**
     * Array of sound player property sets, one element for each sound player the device service
     * supports. Whenever a sound player device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public SoundPlayerProperties[] ClaimedSoundPlayer;

    /**
     * Allocate device specific property set list for sound player devices. One list must be allocated for each sound player
     * the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxSoundPlayer Maximum number of sound players that can be controlled by the physical device
     */
    protected void soundPlayerInit(int maxSoundPlayer) {
        if (SoundPlayers.length == 0 && maxSoundPlayer > 0) {
            ClaimedSoundPlayer = new SoundPlayerProperties[maxSoundPlayer];
            SoundPlayers = getArrayOf(maxSoundPlayer);
            for (int i = 0; i < maxSoundPlayer; i++) {
                SoundPlayers[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support sound player services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(SoundPlayerProperties props) {
        JposVersion = null;
        props.ClearOutputOnDeviceDisable = true;
    }

    /**
     * Returns device implementation of SoundPlayerProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of SoundPlayerProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public SoundPlayerProperties getSoundPlayerProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * SoundRecorder specific implementations
     */

    /**
     * Set of Set of property sets for SoundRecorder devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of sound recorders the device service supports. Each
     * list element contains a list of all property sets owned by SoundRecorderService
     * objects belonging to the same sound recorder.
     */
    public List<JposCommonProperties>[] SoundRecorders = getArrayOf(0);

    /**
     * Array of sound recorder property sets, one element for each sound recorder the device service
     * supports. Whenever a sound recorder device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public SoundRecorderProperties[] ClaimedSoundRecorder;

    /**
     * Allocate device specific property set list for sound recorder devices. One list must be allocated for each sound
     * recorder the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxSoundRecorder Maximum number of sound recorders that can be controlled by the physical device
     */
    protected void soundRecorderInit(int maxSoundRecorder) {
        if (SoundRecorders.length == 0 && maxSoundRecorder > 0) {
            ClaimedSoundRecorder = new SoundRecorderProperties[maxSoundRecorder];
            SoundRecorders = getArrayOf(maxSoundRecorder);
            for (int i = 0; i < maxSoundRecorder; i++) {
                SoundRecorders[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support sound recorder services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(SoundRecorderProperties props) {
        JposVersion = null;
    }

    /**
     * Returns device implementation of SoundRecorderProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of SoundRecorderProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public SoundRecorderProperties getSoundRecorderProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * SpeechSynthesis specific implementations
     */

    /**
     * Set of Set of property sets for SpeechSynthesis devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of speech synthesises the device service supports. Each
     * list element contains a list of all property sets owned by SpeechSynthesisService
     * objects belonging to the same speech synthesis.
     */
    public List<JposCommonProperties>[] SpeechSynthesiss = getArrayOf(0);

    /**
     * Array of speech synthesis property sets, one element for each speech synthesis the device service
     * supports. Whenever a speech synthesis device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public SpeechSynthesisProperties[] ClaimedSpeechSynthesis;

    /**
     * Allocate device specific property set list for speech synthesis devices. One list must be allocated for each speech
     * synthesis the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxSpeechSynthesis Maximum number of speech synthesiss that can be controlled by the physical device
     */
    protected void speechSynthesisInit(int maxSpeechSynthesis) {
        if (SpeechSynthesiss.length == 0 && maxSpeechSynthesis > 0) {
            ClaimedSpeechSynthesis = new SpeechSynthesisProperties[maxSpeechSynthesis];
            SpeechSynthesiss = getArrayOf(maxSpeechSynthesis);
            for (int i = 0; i < maxSpeechSynthesis; i++) {
                SpeechSynthesiss[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support speech synthesis services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(SpeechSynthesisProperties props) {
        JposVersion = null;
        props.ClearOutputOnDeviceDisable = true;
    }

    /**
     * Returns device implementation of SpeechSynthesisProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of SpeechSynthesisProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public SpeechSynthesisProperties getSpeechSynthesisProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * ToneIndicator specific implemenmtations
     */

    /**
     * Set of Set of property sets for ToneIndicator devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of tone indicators the device service supports. Each
     * list element contains a list of all property sets owned by ToneIndicatorService
     * objects belonging to the same tone indicator.
     */
    public List<JposCommonProperties>[] ToneIndicators = getArrayOf(0);

    /**
     * Array of tone indicator property sets, one element for each tone indicator the device service
     * supports. Whenever a tone indicator device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public ToneIndicatorProperties[] ClaimedToneIndicator;

    /**
     * Allocate device specific property set list for tone indicator devices. One list must be allocated for each tone indicator the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxToneIndicator Maximum number of tone indicators that can be controlled by the physical device
     */
    protected void toneIndicatorInit(int maxToneIndicator) {
        if (ToneIndicators.length == 0 && maxToneIndicator > 0) {
            ClaimedToneIndicator = new ToneIndicatorProperties[maxToneIndicator];
            ToneIndicators = getArrayOf(maxToneIndicator);
            for (int i = 0; i < maxToneIndicator; i++) {
                ToneIndicators[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support tone indicator services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(ToneIndicatorProperties props) {
        JposVersion = null;
        props.ClearOutputOnDeviceDisable = true;
    }

    /**
     * Returns device implementation of ToneIndicatorProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of ToneIndicatorProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public ToneIndicatorProperties getToneIndicatorProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * VideoCapture specific implementations
     */

    /**
     * Set of Set of property sets for VideoCapture devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of video captures the device service supports. Each
     * list element contains a list of all property sets owned by VideoCaptureService
     * objects belonging to the same video capture.
     */
    public List<JposCommonProperties>[] VideoCaptures = getArrayOf(0);

    /**
     * Array of video capture property sets, one element for each video capture the device service
     * supports. Whenever a video capture device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public VideoCaptureProperties[] ClaimedVideoCapture;

    /**
     * Allocate device specific property set list for video capture devices. One list must be allocated for each video
     * capture the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxVideoCapture Maximum number of video captures that can be controlled by the physical device
     */
    protected void videoCaptureInit(int maxVideoCapture) {
        if (VideoCaptures.length == 0 && maxVideoCapture > 0) {
            ClaimedVideoCapture = new VideoCaptureProperties[maxVideoCapture];
            VideoCaptures = getArrayOf(maxVideoCapture);
            for (int i = 0; i < maxVideoCapture; i++) {
                VideoCaptures[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support video capture services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(VideoCaptureProperties props) {
        JposVersion = null;
    }

    /**
     * Returns device implementation of VideoCaptureProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of VideoCaptureProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public VideoCaptureProperties getVideoCaptureProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }

    /*
     * VoiceRecognition specific implementations
     */

    /**
     * Set of Set of property sets for VoiceRecognition devices. The size of the list
     * will be specified by the device service implementation (default is 0) and is
     * identical to the number of voice recognitions the device service supports. Each
     * list element contains a list of all property sets owned by VoiceRecognitionService
     * objects belonging to the same voice recognition.
     */
    public List<JposCommonProperties>[] VoiceRecognitions = getArrayOf(0);

    /**
     * Array of voice recognition property sets, one element for each voice recognition the device service
     * supports. Whenever a voice recognition device will be claimed, the corresponding property set will
     * be stored within this array.
     */
    public VoiceRecognitionProperties[] ClaimedVoiceRecognition;

    /**
     * Allocate device specific property set list for voice recognition devices. One list must be allocated for each voice recognition
     * the driver supports.
     * Must be called within constructor of derived classes.
     *
     * @param maxVoiceRecognition Maximum number of voice recognitions that can be controlled by the physical device
     */
    protected void voiceRecognitionInit(int maxVoiceRecognition) {
        if (VoiceRecognitions.length == 0 && maxVoiceRecognition > 0) {
            ClaimedVoiceRecognition = new VoiceRecognitionProperties[maxVoiceRecognition];
            VoiceRecognitions = getArrayOf(maxVoiceRecognition);
            for (int i = 0; i < maxVoiceRecognition; i++) {
                VoiceRecognitions[i] = new ArrayList<>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support voice recognition services.
     *
     * @param props Property set for setting the property defaults
     */
    @SuppressWarnings("deprecation")
    public void changeDefaults(VoiceRecognitionProperties props) {
        JposVersion = null;
    }

    /**
     * Returns device implementation of VoiceRecognitionProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of VoiceRecognitionProperties that matches the requirements of the corresponding device service.
     * @throws JposException If individual implementation of this method is missing.
     */
    public VoiceRecognitionProperties getVoiceRecognitionProperties(int index) throws JposException {
        throw new JposException(JPOS_E_NOSERVICE, "Missing implementation of getBumpBarProperties()");
    }
}
