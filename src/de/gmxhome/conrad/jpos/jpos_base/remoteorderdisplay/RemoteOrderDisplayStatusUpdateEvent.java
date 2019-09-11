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

package de.gmxhome.conrad.jpos.jpos_base.remoteorderdisplay;

import de.gmxhome.conrad.jpos.jpos_base.*;

/**
 * Status update event implementation for RemoteOrderDisplay devices.
 */
public class RemoteOrderDisplayStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Target units of the corresponding status change.
     */
    public int Units;

    /**
     * Constructor, Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (remoteorderdisplay.)RemoteOrderDisplayService object.
     * @param state Status,  see UPOS specification, chapter Remote Order Display - Events - StatusUpdateEvent.
     * @param units Units where status has been changed.
     */
    public RemoteOrderDisplayStatusUpdateEvent(JposBase source, int state, int units) {
        super(source, state);
        Units = units;
    }

    @Override
    public JposStatusUpdateEvent copyEvent(JposBase o) {
        return new RemoteOrderDisplayStatusUpdateEvent(o, getStatus(), Units);
    }

    @Override
    public boolean setStatusProperties() {
        RemoteOrderDisplayProperties data = (RemoteOrderDisplayProperties) getPropertySet();
        data.EventUnits = Units;
        data.EventSource.logSet("EventUnits");
        return super.setStatusProperties();
    }

    @Override
    public boolean checkStatusCorresponds() {
        if (super.checkStatusCorresponds())
            return true;
        return false;
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        RemoteOrderDisplayProperties props = (RemoteOrderDisplayProperties)getPropertySet();
        int units = props.EventUnits;
        boolean rc = super.setAndCheckStatusProperties();
        if (units != props.EventUnits) {
            props.EventSource.logSet("EventUnits");
            return true;
        }
        return rc;
    }
}
