/*
 * Copyright 2022 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.electronicvaluerw;
import jpos.JposException;

/**
 * Output request executor for ElectronicValueRW method AccessDailyLog.
 */
public class AccessDailyLog extends CheckCard {
    /**
     * Get type of daily log.
     * @return Type of daily log.
     */
    public int getType() { return Type; }
    private final int Type;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param data           Property set of device service.
     * @param sequenceNumber Sequence number to get daily log.
     * @param type           Specify whether the daily log is intermediate total or final total and erase.
     * @param timeout        The maximum waiting time (in milliseconds) until the response is received from the ElectronicValueRW device.
     */
    public AccessDailyLog(ElectronicValueRWProperties data, int sequenceNumber, int type, int timeout) {
        super(data, sequenceNumber, timeout);
        Type = type;
    }

    @Override
    public void invoke() throws JposException {
        ((ElectronicValueRWService)Props.EventSource).ElectronicValueRW.accessDailyLog(this);
    }
}
