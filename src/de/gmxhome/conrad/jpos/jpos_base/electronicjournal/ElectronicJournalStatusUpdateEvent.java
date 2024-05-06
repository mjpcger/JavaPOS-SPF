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

package de.gmxhome.conrad.jpos.jpos_base.electronicjournal;

import de.gmxhome.conrad.jpos.jpos_base.*;

import static jpos.ElectronicJournalConst.*;
import static jpos.JposConst.*;

/**
 * Status update event implementation for ElectronicJournal devices.
 */
public class ElectronicJournalStatusUpdateEvent extends JposStatusUpdateEvent {
    /**
     * Constructor, Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (electronicjournal.)ElectronicJournalService object.
     * @param state  New status value, see UPOS specification, chapter Electronic Journal - Events - StatusUpdateEvent.
     */
    public ElectronicJournalStatusUpdateEvent(JposBase source, int state) {
        super(source, state);
    }

    @Override
    public boolean setStatusProperties() {
        if (super.setStatusProperties())
            return true;
        ElectronicJournalProperties props = (ElectronicJournalProperties)getPropertySet();
        switch (getStatus()) {
            case EJ_SUE_IDLE -> props.State = JPOS_S_IDLE;
            case EJ_SUE_SUSPENDED -> props.Suspended = true;
            case EJ_SUE_MEDIUM_FULL, EJ_SUE_MEDIUM_NEAR_FULL, EJ_SUE_MEDIUM_INSERTED, EJ_SUE_MEDIUM_REMOVED -> {
            }
            default -> {
                return false;
            }
        }
        props.signalWaiter();
        return true;
    }

    @Override
    public boolean checkStatusCorresponds() {
        ElectronicJournalProperties props = (ElectronicJournalProperties) getPropertySet();
        return super.checkStatusCorresponds() ||switch (getStatus()) {
            case EJ_SUE_SUSPENDED -> props.Suspended;
            case EJ_SUE_MEDIUM_FULL, EJ_SUE_MEDIUM_NEAR_FULL, EJ_SUE_MEDIUM_INSERTED, EJ_SUE_MEDIUM_REMOVED, EJ_SUE_IDLE -> true;
            default -> false;
        };
    }

    @Override
    public boolean setAndCheckStatusProperties() {
        String[] propnames = { "State", "Suspended" };
        Object[] oldvals = getPropertyValues(propnames);
        return super.setAndCheckStatusProperties() || propertiesHaveBeenChanged(propnames, oldvals) || switch (getStatus()) {
            case EJ_SUE_MEDIUM_FULL, EJ_SUE_MEDIUM_NEAR_FULL, EJ_SUE_MEDIUM_INSERTED, EJ_SUE_MEDIUM_REMOVED -> true;
            default -> false;
        };
    }

    @Override
    public String toLogString() {
        String ret = super.toLogString();
        return ret.length() > 0 ? ret : switch (getStatus()) {
            case EJ_SUE_IDLE -> "ElectronicJournal Idle";
            case EJ_SUE_SUSPENDED -> "ElectronicJournal suspended";
            case EJ_SUE_MEDIUM_FULL -> "ElectronicJournal medium full";
            case EJ_SUE_MEDIUM_NEAR_FULL -> "ElectronicJournal medium nearly full";
            case EJ_SUE_MEDIUM_INSERTED -> "ElectronicJournal medium inserted";
            case EJ_SUE_MEDIUM_REMOVED -> "ElectronicJournal medium removed";
            default -> "Unknown Status Change: " + getStatus();
        };
    }
}
