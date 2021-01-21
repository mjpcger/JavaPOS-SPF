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

package de.gmxhome.conrad.jpos.jpos_base.cat;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Status update event implementation for CAT devices.
 */
public class CATStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Constructor, Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (cat.)CATService object.
     * @param state  New status value, see UPOS specification, chapter CAT - Credit Authorization Terminal - Events - StatusUpdateEvent.
     */
    public CATStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
    }

    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        CATProperties props = (CATProperties)getPropertySet();
        int status = getStatus();
        switch (status) {
            case CATConst.CAT_LOGSTATUS_OK:
            case CATConst.CAT_LOGSTATUS_NEARFULL:
            case CATConst.CAT_LOGSTATUS_FULL:
                props.LogStatus = status;
                return true;
        }
        return false;
    }

    @Override
    public boolean checkStatusCorresponds() {
        if (super.checkStatusCorresponds())
            return true;
        CATProperties props = (CATProperties)getPropertySet();
        int status = getStatus();
        switch (status) {
            case CATConst.CAT_LOGSTATUS_OK:
            case CATConst.CAT_LOGSTATUS_NEARFULL:
            case CATConst.CAT_LOGSTATUS_FULL:
                return props.LogStatus == status;
        }
        return false;
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        CATProperties props = (CATProperties)getPropertySet();
        int status = props.LogStatus;
        if (super.setAndCheckStatusProperties())
            return true;
        if (status != props.LogStatus) {
            props.EventSource.logSet("LogStatus");
            return true;
        }
        return false;
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        if (ret.length() > 0)
            return ret;
        switch (getStatus()) {
            case CATConst.CAT_LOGSTATUS_OK:
                return "CAT Dealing Log OK";
            case CATConst.CAT_LOGSTATUS_NEARFULL:
                return "CAT Dealing Log Nearly Full";
            case CATConst.CAT_LOGSTATUS_FULL:
                return "CAT Dealing Log Full";
        }
        return "Unknown CAT Status Change: " + getStatus();
    }
}
