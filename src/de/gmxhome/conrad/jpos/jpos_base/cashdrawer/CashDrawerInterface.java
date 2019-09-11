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

package de.gmxhome.conrad.jpos.jpos_base.cashdrawer;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Interface for methods that implement property setter and method calls for the CashDrawer device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Cash Drawer.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface CashDrawerInterface extends JposBaseInterface {
    /**
     * Final part of OpenDrawer method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>Device has not been claimed by other instance.</li>
     * </ul>
     *
     * @throws JposException If an error occurs.
     */
    public void openDrawer() throws JposException;

    /**
     * Final part of WaitForDrawerClose method. Can be overwritten in derived class, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapStatus is true,</li>
     *     <li>beepTimeout, beepDuration and beepDelay are positive values,</li>
     *     <li>beepFrequency is between 10 and 40000 (this is more than the human audible range).</li>
     * </ul>
     *
     * @param beepTimeout   Number of milliseconds to wait before starting an alert beeper.
     * @param beepFrequency Audio frequency of the alert beeper in hertz.
     * @param beepDuration  Number of milliseconds that the beep tone will be sounded.
     * @param beepDelay     Number of milliseconds between the sounding of beeper tones.
     * @throws JposException If an error occurs.
     */
    public void waitForDrawerClose(int beepTimeout, int beepFrequency, int beepDuration, int beepDelay) throws JposException;
}
