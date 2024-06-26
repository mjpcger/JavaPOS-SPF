/*
 * Copyright 2020 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.pospower;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

import static jpos.POSPowerConst.*;

/**
 * Class containing the POS Power specific properties, their default values and default implementations of
 * POSPowerInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter POS Power.
 */
public class POSPowerProperties extends JposCommonProperties implements POSPowerInterface {
    /**
     * UPOS property CapBatteryCapacityRemaining. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapBatteryCapacityRemaining = false;

    /**
     * UPOS property CapFanAlarm. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapFanAlarm = false;

    /**
     * UPOS property CapHeatAlarm. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapHeatAlarm = false;

    /**
     * UPOS property CapQuickCharge. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapQuickCharge = false;

    /**
     * UPOS property CapRestartPOS. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapRestartPOS = false;

    /**
     * UPOS property CapShutdownPOS. Default: true. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapShutdownPOS = true;

    /**
     * UPOS property CapStandbyPOS. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapStandbyPOS = false;

    /**
     * UPOS property CapSuspendPOS. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapSuspendPOS = false;

    /**
     * UPOS property CapUPSChargeState. Default: 0. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int CapUPSChargeState = 0;

    /**
     * UPOS property CapVariableBatteryCriticallyLowThreshold. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapVariableBatteryCriticallyLowThreshold = false;

    /**
     * UPOS property CapVariableBatteryLowThreshold. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapVariableBatteryLowThreshold = false;

    /**
     * UPOS property BatteryCapacityRemaining. Default: 0. Should be set to an initial value in the open method if
     * CapBatteryCapacityRemaining is true.
     */
    public int BatteryCapacityRemaining = 0;

    /**
     * UPOS property BatteryCriticallyLowThreshold. Default: 0. Should be overwritten by objects derived from JposDevice within the
     * open method if CapVariableBatteryCriticallyLowThreshold is true.
     */
    public int BatteryCriticallyLowThreshold = 0;

    /**
     * UPOS property BatteryLowThreshold. Default: 0. Should be overwritten by objects derived from JposDevice within the
     * open method if CapVariableBatteryLowThreshold is true.
     */
    public int BatteryLowThreshold = 0;

    /**
     * UPOS property EnforcedShutdownDelayTime. Default: 0. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public int EnforcedShutdownDelayTime = 0;

    /**
     * UPOS property PowerFailDelayTime. Default: 0. Can be initialized by objects derived from JposDevice within
     * the changeDefaults or open method.
     */
    public int PowerFailDelayTime = 0;

    /**
     * UPOS property PowerSource. Default: SOURCE_NA. Should be initialized by objects derived from JposDevice within the
     * open method.
     */
    public int PowerSource = PWR_SOURCE_NA;

    /**
     * UPOS property QuickChargeMode. Default: false. Should be overwritten by objects derived from JposDevice within the
     * open method if CapQuickCharge is true.
     */
    public boolean QuickChargeMode = false;

    /**
     * UPOS property QuickChargeTime. Default: 0. Should be overwritten by objects derived from JposDevice within the
     * open method if CapQuickCharge is true.
     */
    public int QuickChargeTime = 0;

    /**
     * UPOS property UPSChargeState. Default: 0. Must be overwritten by objects derived from JposDevice whenever
     * DeviceEnabled will be set to true if CapUPSChargeState is not 0.
     */
    public int UPSChargeState = 0;

    /**
     * UPOS property BatteryCapacityRemainingInSeconds. Default: 0. Should be overwritten by objects derived from JposDevice within the
     * open method if CapBatteryCapacityRemainingInSeconds is true.
     */
    public int BatteryCapacityRemainingInSeconds = 0;

    /**
     * UPOS property BatteryCriticallyLowThresholdInSeconds. Default: 0. Should be overwritten by objects derived from JposDevice within the
     * open method if CapVariableBatteryCriticallyLowThresholdInSeconds is true.
     */
    public int BatteryCriticallyLowThresholdInSeconds = 0;

    /**
     * UPOS property BatteryLowThresholdInSeconds. Default: 0. Should be overwritten by objects derived from JposDevice within the
     * open method if CapVariableBatteryLowThresholdInSeconds is true.
     */
    public int BatteryLowThresholdInSeconds = 0;

    /**
     * UPOS property CapBatteryCapacityRemainingInSeconds. Default: false. Should be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapBatteryCapacityRemainingInSeconds = false;

    /**
     * UPOS property CapChargeTime. Default: false. Should be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapChargeTime = false;

    /**
     * UPOS property CapVariableBatteryCriticallyLowThresholdInSeconds. Default: false. Should be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapVariableBatteryCriticallyLowThresholdInSeconds = false;

    /**
     * UPOS property CapVariableBatteryLowThresholdInSeconds. Default: false. Should be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapVariableBatteryLowThresholdInSeconds = false;

    /**
     * UPOS property ChargeTime. Default: 0. Should be overwritten by objects derived from JposDevice within the
     * open method if CapChargeTime is true.
     */
    public int ChargeTime = 0;

    /**
     * Constructor. Sets ExclusiveUse to ExclusiveAllowed to match the POSPower device model.
     *
     * @param dev Device index
     */
    protected POSPowerProperties(int dev) {
        super(dev);
        ExclusiveUse = ExclusiveAllowed;
    }

    @Override
    public void batteryCriticallyLowThreshold(int threshold) throws JposException {
        BatteryCriticallyLowThreshold = threshold;
    }

    @Override
    public void batteryLowThreshold(int threshold) throws JposException {
        BatteryLowThreshold = threshold;
    }

    @Override
    public void setBatteryCriticallyLowThresholdInSeconds(int seconds) throws JposException {
        BatteryCriticallyLowThresholdInSeconds = seconds;
    }

    @Override
    public void setBatteryLowThresholdInSeconds(int seconds) throws JposException {
        BatteryLowThresholdInSeconds = seconds;
    }

    @Override
    public void enforcedShutdownDelayTime(int delay) throws JposException {
        EnforcedShutdownDelayTime = delay;
    }

    @Override
    public void restartPOS() throws JposException {
    }

    @Override
    public void shutdownPOS() throws JposException {
    }

    @Override
    public void standbyPOS(int reason) throws JposException {
    }

    @Override
    public void suspendPOS(int reason) throws JposException {
    }
}
