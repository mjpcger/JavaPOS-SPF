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
 * Output request executor for ElectronicJournal method PrintContent.
 */
public class PrintContent extends JposOutputRequest {
    /**
     * ElectronicJournal method PrintContent parameter fromMarker, see UPOS specification.
     * @return Name of lower bound marker.
     */
    public String getFromMarker() {
        return FromMarker;
    }
    private final String FromMarker;

    /**
     * ElectronicJournal method PrintContent parameter toMarker, see UPOS specification.
     * @return Name of upper bound marker.
     */
    public String getToMarker() {
        return ToMarker;
    }
    private final String ToMarker;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param fromMarker Marker that marks start position of data to be printed.
     * @param toMarker   Marker that marks end position of data to be printed.
     */
    public PrintContent(JposCommonProperties props, String fromMarker, String toMarker) {
        super(props);
        ToMarker = toMarker;
        FromMarker = fromMarker;
    }

    @Override
    public void invoke() throws JposException {
        ((ElectronicJournalService)Props.EventSource).ElectronicJournalInterface.printContent(this);
    }
}
