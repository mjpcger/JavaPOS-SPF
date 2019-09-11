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

package de.gmxhome.conrad.jpos.jpos_base.poskeyboard;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Interface for methods that implement property setter and method calls for the POSKeyboard device category.
 * For details about properties, methods and method parameters, see UPOS specification, chapter POS Keyboard.
 * Further details about error handling can be found in introduction - Device Behavior Models - Errors.
 */
public interface POSKeyboardInterface extends JposBaseInterface {
    /**
     * Final part of setting EventTypes. Can be overwritten within derived classes, if necessary.
     * This method will be called only if the following plausibility checks lead to a positive result:
     * <ul>
     *     <li>Device has not been closed,</li>
     *     <li>CapKeyUp is true: type is KBD_ET_DOWN or KBD_ET_DOWN_UP,</li>
     *     <li>CapKeyUp is false: type is KBD_ET_DOWN.</li>
     * </ul>
     *
     * @param type New EventTypes value
     * @throws JposException If an error occurs during enable or disable
     */
    public void eventTypes(int type) throws JposException;
}
