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

package de.gmxhome.conrad.jpos.jpos_base.scanner;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Interface for methods that implement property setter and method calls for the Scanner device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Scanner (Bar
 * Code Reader).
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface ScannerInterface extends JposBaseInterface {
    /**
     * Final part of setting scannerDecodeData. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed.</li>
     * </ul>
     *
     * @param b New DecodeData value
     * @throws JposException If an error occurs
     */
    public void decodeData(boolean b) throws JposException;
}
