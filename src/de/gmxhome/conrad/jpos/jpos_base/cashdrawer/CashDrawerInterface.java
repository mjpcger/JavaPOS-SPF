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
     * Since the CashDrawerService class performs beeping directly via the underlaying standardized
     * sound system via javax.sound, the parameters beepTimeout, beepDuration, beepFrequency and beepDelay
     * will not be forwarded to the final part.<br>
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device is enabled,</li>
     *     <li>CapStatus is true,</li>
     *     <li>The parameters beepTimeout, beepDuration and beepDelay of the calling
     *         method are positive or FOREVER,</li>
     *     <li>The parameter beepFrequency is between 10 and 24000.</li>
     * </ul>
     * since the parameters beepTimeout, beepFrequency, beepDuration and beepDelay will not be passed to
     * this method because sound generation will be performed via the sound system, it must simply wait
     * the drawer state changes to closed and return immediately after that change.<br>
     * Keep also in mind that the beep duration should be at least 1/8 seconds (125 milliseconds) to ensure
     * that it can be heard, values above 1/4 second (250 milliseconds) are recommended.
     *
     * @throws JposException If an error occurs.
     */
    public void waitForDrawerClose() throws JposException;
}
