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
import jpos.*;

/**
 * Output request executor for ElectronicJournal method InitializeMedium.
 */
public class InitializeMedium extends JposOutputRequest {
    /**
     * ElectronicJournal method InitializeMedium parameter mediumID, see UPOS specification.
     * @return Medium Identifier.
     */
    public String getMediumID() {
        return MediumID;
    }

    private final String MediumID;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param mediumID Medium identifier.
     */
    public InitializeMedium(JposCommonProperties props, String mediumID) {
        super(props);
        MediumID = mediumID;
    }

    @Override
    public void invoke() throws JposException {
        ((ElectronicJournalService)Props.EventSource).ElectronicJournalInterface.initializeMedium(this);
    }
}
