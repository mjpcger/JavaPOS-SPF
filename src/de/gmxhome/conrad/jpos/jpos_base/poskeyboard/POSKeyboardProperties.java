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
 * Class containing the keyboard specific properties, their default values and default implementations of
 * POSKeyboardInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter POS Keyboard.
 */
public class POSKeyboardProperties extends JposCommonProperties implements POSKeyboardInterface {
    /**
     * UPOS property CapKeyUp. Default: false. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapKeyUp = false;

    /**
     * UPOS property EventTypes. Default: POSKeyboardConst.KBD_ET_DOWN. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public int EventTypes = POSKeyboardConst.KBD_ET_DOWN;

    /**
     * UPOS property POSKeyData.
     */
    public int POSKeyData;

    /**
     * UPOS property POSKeyEventType.
     */
    public int POSKeyEventType;

    /**
     * Constructor. Sets ExclusiveUse to ExclusiveYes to match the POSKeyboard device model.
     *
     * @param dev Device index
     */
    public POSKeyboardProperties(int dev) {
        super(dev);
        ExclusiveUse = ExclusiveYes;
    }

    @Override
    public void initOnOpen() {
        super.initOnOpen();
    }

    @Override
    public void clearDataProperties() {
        super.clearDataProperties();
        POSKeyData = 0;
        POSKeyEventType = 0;
    }

    @Override
    public void eventTypes(int type) throws JposException {
        EventTypes = type;
    }
}