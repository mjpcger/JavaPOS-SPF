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

package de.gmxhome.conrad.jpos.jpos_base.checkscanner;

import de.gmxhome.conrad.jpos.jpos_base.JposBase;
import de.gmxhome.conrad.jpos.jpos_base.JposStatusUpdateEvent;

import static jpos.CheckScannerConst.*;

/**
 * Status update event implementation for CheckScanner devices.
 */
public class CheckScannerStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Constructor, Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     */
    public CheckScannerStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
    }

    @Override
    public boolean checkStatusCorresponds() {
        return super.checkStatusCorresponds() || getStatus() == CHK_SUE_SCANCOMPLETE;
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        return ret.length() > 0 ? ret:
                (getStatus() == CHK_SUE_SCANCOMPLETE ? "CheckScanner scan complete" : "Unknown CheckScanner Status Change: " + getStatus());
    }
}
