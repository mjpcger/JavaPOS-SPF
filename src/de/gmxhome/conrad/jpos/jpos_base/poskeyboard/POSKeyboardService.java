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
import jpos.services.*;

import static jpos.JposConst.*;
import static jpos.POSKeyboardConst.*;

/**
 * POSKeyboard service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class POSKeyboardService extends JposBase implements POSKeyboardService116 {
    /**
     * Instance of a class implementing the POSKeyboardInterface for pos keyboard specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public POSKeyboardInterface POSKeyboardInterface;

    private final POSKeyboardProperties Data;

    /**
     * Constructor. Stores property set and device driver implementation
     *
     * @param props  Device service property set
     * @param device Device driver implementation
     */
    public POSKeyboardService(POSKeyboardProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public boolean getCapKeyUp() throws JposException {
        checkOpened();
        logGet("CapKeyUp");
        return Data.CapKeyUp;
    }

    @Override
    public int getEventTypes() throws JposException {
        checkOpened();
        logGet("EventTypes");
        return Data.EventTypes;
    }

    @Override
    public void setEventTypes(int type) throws JposException {
        logPreSet("EventTypes");
        checkOpened();
        check(!Data.CapKeyUp && type != KBD_ET_DOWN, JPOS_E_ILLEGAL, "No support for key up events");
        check(type != KBD_ET_DOWN && type != KBD_ET_DOWN_UP, JPOS_E_ILLEGAL, "Invalid event type: " + type);
        checkNoChangedOrClaimed(Data.EventTypes, type);
        POSKeyboardInterface.eventTypes(type);
        logSet("EventTypes");
    }

    @Override
    public int getPOSKeyData() throws JposException {
        checkClaimed();
        logGet("POSKeyData");
        return Data.POSKeyData;
    }

    @Override
    public int getPOSKeyEventType() throws JposException {
        checkClaimed();
        logGet("POSKeyEventType");
        return Data.POSKeyEventType;
    }
}
