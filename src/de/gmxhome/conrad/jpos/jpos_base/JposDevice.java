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

import de.gmxhome.conrad.jpos.jpos_base.bumpbar.BumpBarProperties;
import de.gmxhome.conrad.jpos.jpos_base.cashdrawer.CashDrawerProperties;
import de.gmxhome.conrad.jpos.jpos_base.gate.GateProperties;
import de.gmxhome.conrad.jpos.jpos_base.cat.CATProperties;
import de.gmxhome.conrad.jpos.jpos_base.coindispenser.CoinDispenserProperties;
import de.gmxhome.conrad.jpos.jpos_base.electronicjournal.ElectronicJournalProperties;
import de.gmxhome.conrad.jpos.jpos_base.fiscalprinter.FiscalPrinterProperties;
import de.gmxhome.conrad.jpos.jpos_base.itemdispenser.ItemDispenserProperties;
import de.gmxhome.conrad.jpos.jpos_base.keylock.KeylockProperties;
import de.gmxhome.conrad.jpos.jpos_base.lights.LightsProperties;
import de.gmxhome.conrad.jpos.jpos_base.linedisplay.LineDisplayProperties;
import de.gmxhome.conrad.jpos.jpos_base.micr.MICRProperties;
import de.gmxhome.conrad.jpos.jpos_base.motionsensor.MotionSensorProperties;
import de.gmxhome.conrad.jpos.jpos_base.msr.MSRProperties;
import de.gmxhome.conrad.jpos.jpos_base.poskeyboard.POSKeyboardProperties;
import de.gmxhome.conrad.jpos.jpos_base.posprinter.POSPrinterProperties;
import de.gmxhome.conrad.jpos.jpos_base.remoteorderdisplay.RemoteOrderDisplayProperties;
import de.gmxhome.conrad.jpos.jpos_base.scale.ScaleProperties;
import de.gmxhome.conrad.jpos.jpos_base.scanner.ScannerProperties;
import de.gmxhome.conrad.jpos.jpos_base.signaturecapture.SignatureCaptureProperties;
import de.gmxhome.conrad.jpos.jpos_base.toneindicator.ToneIndicatorProperties;
import jpos.FiscalPrinterConst;
import jpos.SignatureCapture;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that contains all device specific implementation interfaces to be derived by device service implementations.
 * For each <i>DeviceClass</i>, it provides
 * <ul>
 *     <li>an adjustment in method noOfPropertySets that counts all property sets bound to the <i>DeviceClass</i>s
 *     array,</li>
 *     <li>an array of property set lists with name <i>DeviceClass</i>s,</li>
 *     <li>an array for claimed property sets with name Claimed<i>DeviceClass</i>,</li>
 *     <li>a method <i>deviceClass</i>Init which initializes <i>DeviceClass</i>s to hold the correct number of lists,</li>
 *     <li>a method changeDefaults(<i>DeviceClass</i>Proterties) which must be oferwritten in derived classes to
 *         set the correct property defaults and</li>
 *     <li>a class factory get<i>DeviceClass</i>Properties(index) which returns a device specific property set to be
 *         added to <i>DeviceClass</i>s[index].</li>
 * </ul>
 */
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

    @Override
    public int noOfPropertySets() {
        return super.noOfPropertySets() +
                getCount(BumpBars) +
                getCount(CashDrawers) +
                getCount(CATs) +
                getCount(CoinDispensers) +
                getCount(ElectronicJournals) +
                getCount(FiscalPrinters) +
                getCount(Gates) +
                getCount(ItemDispensers) +
                getCount(Keylocks) +
                getCount(Lightss) +
                getCount(LineDisplays) +
                getCount(MICRs) +
                getCount(MotionSensors) +
                getCount(MSRs) +
                getCount(POSKeyboards) +
                getCount(POSPrinters) +
                getCount(RemoteOrderDisplays) +
                getCount(Scales) +
                getCount(Scanners) +
                getCount(SignatureCaptures) +
                getCount(ToneIndicators);
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
    public List<JposCommonProperties>[] BumpBars = (List<JposCommonProperties>[])new List[0];

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
     * @param maxBumpBar Maximum number of cash drawers that can be controlled by the physical device
     */
    protected void bumpBarInit(int maxBumpBar) {
        if (BumpBars.length == 0 && maxBumpBar > 0) {
            ClaimedBumpBar = new BumpBarProperties[maxBumpBar];
            BumpBars = (List<JposCommonProperties>[])new List[maxBumpBar];
            for (int i = 0; i < maxBumpBar; i++) {
                BumpBars[i] = new ArrayList<JposCommonProperties>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support bump bar services.
     *
     * @param props Property set for setting the property defaults
     */
    public void changeDefaults(BumpBarProperties props) {
    }

    /**
     * Returns device implementation of BumpBarProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of BumpBarProperties that matches the requirements of the corresponding device service.
     */
    public BumpBarProperties getBumpBarProperties(int index) {
        return null;
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
    public List<JposCommonProperties>[] CashDrawers = (List<JposCommonProperties>[])new List[0];

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
    protected void cashDrawerInit(int maxDrawer) {
        if (CashDrawers.length == 0 && maxDrawer > 0) {
            ClaimedCashDrawer = new CashDrawerProperties[maxDrawer];
            CashDrawers = (List<JposCommonProperties>[])new List[maxDrawer];
            for (int i = 0; i < maxDrawer; i++) {
                CashDrawers[i] = new ArrayList<JposCommonProperties>(0);
            }
            DrawerBeepVolume = 100; // The default volume
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support cash drawer services.
     *
     * @param props Property set for setting the property defaults
     */
    public void changeDefaults(CashDrawerProperties props) {
    }

    /**
     * Returns device implementation of CashDrawerProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of CashDrawerProperties that matches the requirements of the corresponding device service.
     */
    public CashDrawerProperties getCashDrawerProperties(int index) {
        return null;
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
    public List<JposCommonProperties>[] Keylocks = ((List<JposCommonProperties>[])new List[0]);

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
            Keylocks = (List<JposCommonProperties>[])new List[maxKeylock];
            for (int i = 0; i < maxKeylock; i++) {
                Keylocks[i] = new ArrayList<JposCommonProperties>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support keylock services.
     *
     * @param props Property set for setting the propperty defaults
     */
    public void changeDefaults(KeylockProperties props) {
    }

    /**
     * Returns device implementation of KeylockProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of KeylockProperties that matches the requirements of the corresponding device service.
     */
    public KeylockProperties getKeylockProperties(int index) {
        return null;
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
    public List<JposCommonProperties>[] POSKeyboards = (List<JposCommonProperties>[])new List[0];

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
            POSKeyboards = (List<JposCommonProperties>[])new List[maxKeyboard];
            for (int i = 0; i < maxKeyboard; i++) {
                POSKeyboards[i] = new ArrayList<JposCommonProperties>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support POS keyboard services.
     *
     * @param props Property set for setting the propperty defaults
     */
    public void changeDefaults(POSKeyboardProperties props) {
    }

    /**
     * Returns device implementation of POSKeyboardProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of POSKeyboardProperties that matches the requirements of the corresponding device service.
     */
    public POSKeyboardProperties getPOSKeyboardProperties(int index) {
        return null;
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
    public List<JposCommonProperties>[] Scanners = (List<JposCommonProperties>[])new List[0];

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
            Scanners = (List<JposCommonProperties>[])new List[maxScanner];
            for (int i = 0; i < maxScanner; i++) {
                Scanners[i] = new ArrayList<JposCommonProperties>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support scanner services.
     *
     * @param props Property set for setting the propperty defaults
     */
    public void changeDefaults(ScannerProperties props) {
    }

    /**
     * Returns device implementation of ScannerProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of ScannerProperties that matches the requirements of the corresponding device service.
     */
    public ScannerProperties getScannerProperties(int index) {
        return null;
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
    public List<JposCommonProperties>[] ToneIndicators = (List<JposCommonProperties>[])new List[0];

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
            ToneIndicators = (List<JposCommonProperties>[])new List[maxToneIndicator];
            for (int i = 0; i < maxToneIndicator; i++) {
                ToneIndicators[i] = new ArrayList<JposCommonProperties>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support tone indicator services.
     *
     * @param props Property set for setting the property defaults
     */
    public void changeDefaults(ToneIndicatorProperties props) {
    }

    /**
     * Returns device implementation of ToneIndicatorProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of ToneIndicatorProperties that matches the requirements of the corresponding device service.
     */
    public ToneIndicatorProperties getToneIndicatorProperties(int index) {
        return null;
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
    public List<JposCommonProperties>[] LineDisplays = (List<JposCommonProperties>[])new List[0];

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
            LineDisplays = (List<JposCommonProperties>[])new List[maxDisplay];
            for (int i = 0; i < maxDisplay; i++) {
                LineDisplays[i] = new ArrayList<JposCommonProperties>(0);
            }
        }
    }

    /**
     * Change defaults of properties. Must be implemented within derived classed that support line display services.
     *
     * @param props Property set for setting the propperty defaults
     */
    public void changeDefaults(LineDisplayProperties props) {
    }

    /**
     * Returns device implementation of LineDisplayProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of LineDisplayProperties that matches the requirements of the corresponding device service.
     */
    public LineDisplayProperties getLineDisplayProperties(int index) {
        return null;
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
    public List<JposCommonProperties>[] MSRs = (List<JposCommonProperties>[])new List[0];

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
    public void changeDefaults(MSRProperties props) {
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
            MSRs = (List<JposCommonProperties>[])new List[maxMsr];
            for (int i = 0; i < maxMsr; i++) {
                MSRs[i] = new ArrayList<JposCommonProperties>(0);
            }
        }
    }

    /**
     * Returns device implementation of MSRProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of MSRProperties that matches the requirements of the corresponding device service.
     */
    public MSRProperties getMSRProperties(int index) {
        return null;
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
    public List<JposCommonProperties>[] Scales = (List<JposCommonProperties>[])new List[0];

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
    public void changeDefaults(ScaleProperties props) {
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
            Scales = (List<JposCommonProperties>[])new List[maxScales];
            for (int i = 0; i < maxScales; i++) {
                Scales[i] = new ArrayList<JposCommonProperties>(0);
            }
        }
    }

    /**
     * Returns device implementation of ScaleProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of ScaleProperties that matches the requirements of the corresponding device service.
     */
    public ScaleProperties getScaleProperties(int index) {
        return null;
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
    public List<JposCommonProperties>[] CoinDispensers = (List<JposCommonProperties>[])new List[0];

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
    public void changeDefaults(CoinDispenserProperties props) {
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
            CoinDispensers = (List<JposCommonProperties>[])new List[maxCoinDispenser];
            for (int i = 0; i < maxCoinDispenser; i++) {
                CoinDispensers[i] = new ArrayList<JposCommonProperties>(0);
            }
        }
    }

    /**
     * Returns device implementation of CoinDispenserProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of CoinDispenserProperties that matches the requirements of the corresponding device service.
     */
    public CoinDispenserProperties getCoinDispenserProperties(int index) {
        return null;
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
    public List<JposCommonProperties>[] POSPrinters = (List<JposCommonProperties>[])new List[0];

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
    public void changeDefaults(POSPrinterProperties props) {
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
            POSPrinters = (List<JposCommonProperties>[])new List[maxPrinters];
            for (int i = 0; i < maxPrinters; i++) {
                POSPrinters[i] = new ArrayList<JposCommonProperties>(0);
            }
        }
    }

    /**
     * Returns device implementation of POSPrinterProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of POSPrinterProperties that matches the requirements of the corresponding device service.
     */
    public POSPrinterProperties getPOSPrinterProperties(int index) {
        return null;
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
    public List<JposCommonProperties>[] RemoteOrderDisplays = (List<JposCommonProperties>[])new List[0];

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
    public void changeDefaults(RemoteOrderDisplayProperties props) {
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
            RemoteOrderDisplays = (List<JposCommonProperties>[])new List[maxRODisplay];
            for (int i = 0; i < maxRODisplay; i++) {
                RemoteOrderDisplays[i] = new ArrayList<JposCommonProperties>(0);
            }
        }
    }

    /**
     * Returns device implementation of RemoteOrderDisplayProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of RemoteOrderDisplayProperties that matches the requirements of the corresponding device service.
     */
    public RemoteOrderDisplayProperties getRemoteOrderDisplayProperties(int index) {
        return null;
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
    public List<JposCommonProperties>[] CATs = (List<JposCommonProperties>[])new List[0];

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
    public void changeDefaults(CATProperties props) {
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
            CATs = (List<JposCommonProperties>[])new List[maxCAT];
            for (int i = 0; i < maxCAT; i++) {
                CATs[i] = new ArrayList<JposCommonProperties>(0);
            }
        }
    }

    /**
     * Returns device implementation of CATProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of CATProperties that matches the requirements of the corresponding device service.
     */
    public CATProperties getCATProperties(int index) {
        return null;
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
    public List<JposCommonProperties>[] ElectronicJournals = (List<JposCommonProperties>[])new List[0];

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
    public void changeDefaults(ElectronicJournalProperties props) {
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
            ElectronicJournals = (List<JposCommonProperties>[])new List[maxElectronicJournal];
            for (int i = 0; i < maxElectronicJournal; i++) {
                ElectronicJournals[i] = new ArrayList<JposCommonProperties>(0);
            }
        }
    }

    /**
     * Returns device implementation of ElectronicJournalProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of ElectronicJournalProperties that matches the requirements of the corresponding device service.
     */
    public ElectronicJournalProperties getElectronicJournalProperties(int index) {
        return null;
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
    public List<JposCommonProperties>[] MICRs = (List<JposCommonProperties>[])new List[0];

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
    public void changeDefaults(MICRProperties props) {
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
            MICRs = (List<JposCommonProperties>[])new List[maxMICR];
            for (int i = 0; i < maxMICR; i++) {
                MICRs[i] = new ArrayList<JposCommonProperties>(0);
            }
        }
    }

    /**
     * Returns device implementation of MICRProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of MICRProperties that matches the requirements of the corresponding device service.
     */
    public MICRProperties getMICRProperties(int index) {
        return null;
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
    public List<JposCommonProperties>[] FiscalPrinters = (List<JposCommonProperties>[])new List[0];

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
    public void changeDefaults(FiscalPrinterProperties props) {
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
            FiscalPrinters = (List<JposCommonProperties>[])new List[maxFiscalPrinter];
            for (int i = 0; i < maxFiscalPrinter; i++) {
                FiscalPrinters[i] = new ArrayList<JposCommonProperties>(0);
            }
            FPTR_AT_DISCOUNT = FiscalPrinterConst.FPTR_AT_AMOUNT_DISCOUNT;      // Default for package discount
            FPTR_AT_SURCHARGE = FiscalPrinterConst.FPTR_AT_AMOUNT_SURCHARGE;    // Default for package surcharge
        }
    }

    /**
     * Returns device implementation of FiscalPrinterProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of FiscalPrinterProperties that matches the requirements of the corresponding device service.
     */
    public FiscalPrinterProperties getFiscalPrinterProperties(int index) {
        return null;
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
    public List<JposCommonProperties>[] Lightss = (List<JposCommonProperties>[])new List[0];

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
    public void changeDefaults(LightsProperties props) {
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
            Lightss = (List<JposCommonProperties>[])new List[maxLights];
            for (int i = 0; i < maxLights; i++) {
                Lightss[i] = new ArrayList<JposCommonProperties>(0);
            }
        }
    }

    /**
     * Returns device implementation of LightsProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of LightsProperties that matches the requirements of the corresponding device service.
     */
    public LightsProperties getLightsProperties(int index) {
        return null;
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
    public List<JposCommonProperties>[] MotionSensors = (List<JposCommonProperties>[])new List[0];

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
    public void changeDefaults(MotionSensorProperties props) {
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
            MotionSensors = (List<JposCommonProperties>[])new List[maxMotionSensor];
            for (int i = 0; i < maxMotionSensor; i++) {
                MotionSensors[i] = new ArrayList<JposCommonProperties>(0);
            }
        }
    }

    /**
     * Returns device implementation of MotionSensorProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of MotionSensorProperties that matches the requirements of the corresponding device service.
     */
    public MotionSensorProperties getMotionSensorProperties(int index) {
        return null;
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
    public List<JposCommonProperties>[] Gates = (List<JposCommonProperties>[])new List[0];

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
    public void changeDefaults(GateProperties props) {
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
            Gates = (List<JposCommonProperties>[])new List[maxGate];
            for (int i = 0; i < maxGate; i++) {
                Gates[i] = new ArrayList<JposCommonProperties>(0);
            }
        }
    }

    /**
     * Returns device implementation of GateProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of GateProperties that matches the requirements of the corresponding device service.
     */
    public GateProperties getGateProperties(int index) {
        return null;
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
    public List<JposCommonProperties>[] ItemDispensers = (List<JposCommonProperties>[])new List[0];

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
    public void changeDefaults(ItemDispenserProperties props) {
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
            ItemDispensers = (List<JposCommonProperties>[])new List[maxItemDispenser];
            for (int i = 0; i < maxItemDispenser; i++) {
                ItemDispensers[i] = new ArrayList<JposCommonProperties>(0);
            }
        }
    }

    /**
     * Returns device implementation of ItemDispenserProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of ItemDispenserProperties that matches the requirements of the corresponding device service.
     */
    public ItemDispenserProperties getItemDispenserProperties(int index) {
        return null;
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
    public List<JposCommonProperties>[] SignatureCaptures = (List<JposCommonProperties>[])new List[0];

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
    public void changeDefaults(SignatureCaptureProperties props) {
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
            SignatureCaptures = (List<JposCommonProperties>[])new List[maxSignatureCapture];
            for (int i = 0; i < maxSignatureCapture; i++) {
                SignatureCaptures[i] = new ArrayList<JposCommonProperties>(0);
            }
        }
    }

    /**
     * Returns device implementation of SignatureCaptureProperties.
     *
     * @param index Device index, see constructor of JposCommonProperties.
     * @return Instance of SignatureCaptureProperties that matches the requirements of the corresponding device service.
     */
    public SignatureCaptureProperties getSignatureCaptureProperties(int index) {
        return null;
    }
}
