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

package jpos.services;

import jpos.JposException;

public interface POSPowerService116 extends POSPowerService115 {
    int getBatteryCapacityRemainingInSeconds() throws JposException;

    int getBatteryCriticallyLowThresholdInSeconds() throws JposException;

    void setBatteryCriticallyLowThresholdInSeconds(int seconds) throws JposException;

    int getBatteryLowThresholdInSeconds() throws JposException;

    void setBatteryLowThresholdInSeconds(int seconds) throws JposException;

    boolean getCapBatteryCapacityRemainingInSeconds() throws JposException;

    boolean getCapChargeTime() throws JposException;

    boolean getCapVariableBatteryCriticallyLowThresholdInSeconds() throws JposException;

    boolean getCapVariableBatteryLowThresholdInSeconds() throws JposException;

    int getChargeTime() throws JposException;
}
