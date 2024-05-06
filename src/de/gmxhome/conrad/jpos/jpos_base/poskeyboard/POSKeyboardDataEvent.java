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

/**
 * Data event implementation for POSKeyboard devices.
 */
public class POSKeyboardDataEvent extends JposDataEvent {
    /**
     * Holds the POSKeyData property value to be stored before firing the event.
     */
    final int Data;
    /**
     * Holds the POSKeyEventType property value to be stored before firing the event.
     */
    final int EventType;

    /**
     * Constructor. Parameters passed to base class unchanged.
     *
     * @param source   Source, for services implemented with this framework, the (poskeyboard.)POSKeyboardService object.
     * @param state    Status, see UPOS specification, chapter POS Keyboard - Events - DataEvent.
     * @param data Value to be stored in property POSKeyData.
     * @param type Value to be stored in property POSKeyEventType.
     */
    public POSKeyboardDataEvent(JposBase source, int state, int data, int type) {
        super(source, state);
        Data = data;
        EventType = type;
    }

    @Override
    public void setDataProperties() {
        super.setDataProperties();
        POSKeyboardProperties dev = (POSKeyboardProperties) getPropertySet();
        dev.POSKeyData = Data;
        dev.EventSource.logSet("POSKeyData");
        dev.POSKeyEventType = EventType;
        dev.EventSource.logSet("POSKeyEventType");
    }
}
