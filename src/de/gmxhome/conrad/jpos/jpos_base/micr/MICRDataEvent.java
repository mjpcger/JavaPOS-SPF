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

package de.gmxhome.conrad.jpos.jpos_base.micr;

import de.gmxhome.conrad.jpos.jpos_base.*;

/**
 * Data event implementation for MICR devices.
 */
public class MICRDataEvent extends JposDataEvent {
    /**
     * Data containing values for data fields belonging to this data event.
     */
    Data Data;

    /**
     * Constructor. Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (micr.)MICRService object.
     * @param state Status, see UPOS specification.
     * @param data Data belonging to this event.
     */
    public MICRDataEvent(JposBase source, int state, Data data) {
        super(source, state);
        Data = data;
    }

    @Override
    public void setDataProperties() {
        super.setDataProperties();
        Data.setDataProperties((MICRProperties) getPropertySet());
    }
}
