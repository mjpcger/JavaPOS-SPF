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
import jpos.services.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;
import static jpos.POSPowerConst.*;

/**
 * POSPower service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class POSPowerService extends JposBase implements POSPowerService116 {
    /**
     * Instance of a class implementing the POSPowerInterface for POS power device specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public POSPowerInterface POSPowerInterface;

    private final POSPowerProperties Data;

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public POSPowerService(POSPowerProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public boolean getCapBatteryCapacityRemaining() throws JposException {
        checkOpened();
        logGet("CapBatteryCapacityRemaining");
        return Data.CapBatteryCapacityRemaining;
    }

    @Override
    public boolean getCapFanAlarm() throws JposException {
        checkOpened();
        logGet("CapFanAlarm");
        return Data.CapFanAlarm;
    }

    @Override
    public boolean getCapHeatAlarm() throws JposException {
        checkOpened();
        logGet("CapHeatAlarm");
        return Data.CapHeatAlarm;
    }

    @Override
    public boolean getCapQuickCharge() throws JposException {
        checkOpened();
        logGet("CapQuickCharge");
        return Data.CapQuickCharge;
    }

    @Override
    public boolean getCapRestartPOS() throws JposException {
        checkOpened();
        logGet("CapRestartPOS");
        return Data.CapRestartPOS;
    }

    @Override
    public boolean getCapShutdownPOS() throws JposException {
        checkOpened();
        logGet("CapShutdownPOS");
        return Data.CapShutdownPOS;
    }

    @Override
    public boolean getCapStandbyPOS() throws JposException {
        checkOpened();
        logGet("CapStandbyPOS");
        return Data.CapStandbyPOS;
    }

    @Override
    public boolean getCapSuspendPOS() throws JposException {
        checkOpened();
        logGet("CapSuspendPOS");
        return Data.CapSuspendPOS;
    }

    @Override
    public int getCapUPSChargeState() throws JposException {
        checkOpened();
        logGet("CapUPSChargeState");
        return Data.CapUPSChargeState;
    }

    @Override
    public boolean getCapVariableBatteryCriticallyLowThreshold() throws JposException {
        checkOpened();
        logGet("CapVariableBatteryCriticallyLowThreshold");
        return Data.CapVariableBatteryCriticallyLowThreshold;
    }

    @Override
    public boolean getCapVariableBatteryLowThreshold() throws JposException {
        checkOpened();
        logGet("CapVariableBatteryLowThreshold");
        return Data.CapVariableBatteryLowThreshold;
    }

    @Override
    public int getBatteryCapacityRemaining() throws JposException {
        checkOpened();
        logGet("BatteryCapacityRemaining");
        return Data.BatteryCapacityRemaining;
    }

    @Override
    public int getBatteryCriticallyLowThreshold() throws JposException {
        checkOpened();
        logGet("BatteryCriticallyLowThreshold");
        return Data.BatteryCriticallyLowThreshold;
    }

    @Override
    public void setBatteryCriticallyLowThreshold(int threshold) throws JposException {
        logPreSet("BatteryCriticallyLowThreshold");
        checkOpened();
        check(threshold <= 0 || threshold > 99, JPOS_E_ILLEGAL, "BatteryCriticallyLowThreshold must be between 0 and 100: " + threshold);
        POSPowerInterface.batteryCriticallyLowThreshold(threshold);
        logSet("BatteryCriticallyLowThreshold");
    }

    @Override
    public int getBatteryLowThreshold() throws JposException {
        checkOpened();
        logGet("BatteryLowThreshold");
        return Data.BatteryLowThreshold;
    }

    @Override
    public void setBatteryLowThreshold(int threshold) throws JposException {
        logPreSet("BatteryLowThreshold");
        checkOpened();
        check(threshold <= 0 || threshold > 99, JPOS_E_ILLEGAL, "BatteryLowThreshold must be between 0 and 99: " + threshold);
        POSPowerInterface.batteryLowThreshold(threshold);
        logSet("Timeout");
    }

    @Override
    public int getEnforcedShutdownDelayTime() throws JposException {
        checkOpened();
        logGet("EnforcedShutdownDelayTime");
        return Data.EnforcedShutdownDelayTime;
    }

    @Override
    public void setEnforcedShutdownDelayTime(int delay) throws JposException {
        logPreSet("EnforcedShutdownDelayTime");
        checkEnabled();
        check(delay < 0, JPOS_E_ILLEGAL, "Enforced shutdown delay time must be >= 0: " + delay);
        POSPowerInterface.enforcedShutdownDelayTime(delay);
        logSet("EnforcedShutdownDelayTime");
    }

    @Override
    public int getPowerFailDelayTime() throws JposException {
        checkOpened();
        logGet("PowerFailDelayTime");
        return Data.PowerFailDelayTime;
    }

    @Override
    public int getPowerSource() throws JposException {
        checkOpened();
        logGet("PowerSource");
        return Data.PowerSource;
    }

    @Override
    public boolean getQuickChargeMode() throws JposException {
        checkOpened();
        logGet("QuickChargeMode");
        return Data.QuickChargeMode;
    }

    @Override
    public int getQuickChargeTime() throws JposException {
        checkOpened();
        logGet("QuickChargeTime");
        return Data.QuickChargeTime;
    }

    @Override
    public int getUPSChargeState() throws JposException {
        checkEnabled();
        logGet("UPSChargeState");
        return Data.UPSChargeState;
    }

    @Override
    public void restartPOS() throws JposException {
        logPreCall("RestartPOS");
        checkEnabled();
        check(!Data.CapRestartPOS, JPOS_E_ILLEGAL, "RestartPOS not supported");
        POSPowerInterface.restartPOS();
        logCall("RestartPOS");

    }

    @Override
    public void shutdownPOS() throws JposException {
        logPreCall("ShutdownPOS");
        checkEnabled();
        check(!Data.CapShutdownPOS, JPOS_E_ILLEGAL, "ShutdownPOS not supported");
        POSPowerInterface.shutdownPOS();
        logCall("ShutdownPOS");

    }

    @Override
    public void standbyPOS(int reason) throws JposException {
        logPreCall("StandbyPOS", removeOuterArraySpecifier(new Object[]{reason}, Device.MaxArrayStringElements));
        long[] allowed = { PWR_REASON_REQUEST, PWR_REASON_ALLOW, PWR_REASON_DENY };
        checkEnabled();
        checkMember(reason, allowed, JPOS_E_ILLEGAL, "Unsupported reason: " + reason);
        check(!Data.CapStandbyPOS && reason == allowed[0], JPOS_E_ILLEGAL, "Request standby not supported");
        POSPowerInterface.standbyPOS(reason);
        logCall("StandbyPOS");
    }

    @Override
    public void suspendPOS(int reason) throws JposException {
        logPreCall("SuspendPOS", removeOuterArraySpecifier(new Object[]{reason}, Device.MaxArrayStringElements));
        long[] allowed = { PWR_REASON_REQUEST, PWR_REASON_ALLOW, PWR_REASON_DENY };
        checkEnabled();
        checkMember(reason, allowed, JPOS_E_ILLEGAL, "Unsupported reason: " + reason);
        check(!Data.CapSuspendPOS && reason == allowed[0], JPOS_E_ILLEGAL, "Request suspend not supported");
        POSPowerInterface.suspendPOS(reason);
        logCall("SuspendPOS");
    }

    @Override
    public int getBatteryCapacityRemainingInSeconds() throws JposException {
        checkOpened();
        logGet("BatteryCapacityRemainingInSeconds");
        return Data.BatteryCapacityRemainingInSeconds;
    }

    @Override
    public int getBatteryCriticallyLowThresholdInSeconds() throws JposException {
        checkOpened();
        logGet("BatteryCriticallyLowThresholdInSeconds");
        return Data.BatteryCriticallyLowThresholdInSeconds;
    }

    @Override
    public int getBatteryLowThresholdInSeconds() throws JposException {
        checkOpened();
        logGet("BatteryLowThresholdInSeconds");
        return Data.BatteryLowThresholdInSeconds;
    }

    @Override
    public boolean getCapBatteryCapacityRemainingInSeconds() throws JposException {
        checkOpened();
        logGet("CapBatteryCapacityRemaining");
        return Data.CapBatteryCapacityRemainingInSeconds;
    }

    @Override
    public boolean getCapChargeTime() throws JposException {
        checkOpened();
        logGet("CapChargeTime");
        return Data.CapChargeTime;
    }

    @Override
    public boolean getCapVariableBatteryCriticallyLowThresholdInSeconds() throws JposException {
        checkOpened();
        logGet("CapVariableBatteryCriticallyLowThresholdInSeconds");
        return Data.CapVariableBatteryCriticallyLowThresholdInSeconds;
    }

    @Override
    public boolean getCapVariableBatteryLowThresholdInSeconds() throws JposException {
        checkOpened();
        logGet("CapVariableBatteryLowThresholdInSeconds");
        return Data.CapVariableBatteryLowThresholdInSeconds;
    }

    @Override
    public int getChargeTime() throws JposException {
        checkOpened();
        logGet("ChargeTime");
        return Data.ChargeTime;
    }

    @Override
    public void setBatteryCriticallyLowThresholdInSeconds(int seconds) throws JposException {
        logPreSet("BatteryCriticallyLowThresholdInSeconds");
        checkEnabled();
        check(!Data.CapVariableBatteryCriticallyLowThresholdInSeconds, JPOS_E_ILLEGAL, "No support for critical low threshold in seconds");
        check(seconds < 0, JPOS_E_ILLEGAL, "Critical low threshold in seconds must be >= 0: " + seconds);
        POSPowerInterface.setBatteryCriticallyLowThresholdInSeconds(seconds);
        logSet("BatteryCriticallyLowThresholdInSeconds");
    }

    @Override
    public void setBatteryLowThresholdInSeconds(int seconds) throws JposException {
        logPreSet("BatteryLowThresholdInSeconds");
        checkEnabled();
        check(!Data.CapVariableBatteryLowThresholdInSeconds, JPOS_E_ILLEGAL, "No support for low threshold in seconds");
        check(seconds < 0, JPOS_E_ILLEGAL, "Low threshold in seconds must be >= 0: " + seconds);
        POSPowerInterface.setBatteryLowThresholdInSeconds(seconds);
        logSet("BatteryLowThresholdInSeconds");
    }
}
