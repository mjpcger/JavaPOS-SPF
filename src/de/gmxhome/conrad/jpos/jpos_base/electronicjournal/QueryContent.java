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
 * Input request executor for ElectronicJournal method QueryContent.
 */
public class QueryContent extends JposInputRequest {
    /**
     * ElectronicJournal method QueryContent parameter fileName, see UPOS specification.
     * @return Filename to be used to store queried contents.
     */
    public String getFileName() {
        return FileName;
    }
    private String FileName;

    /**
     * ElectronicJournal method QueryContent parameter fromMarker, see UPOS specification.
     * @return Name of lower bound marker.
     */
    public String getFromMarker() {
        return FromMarker;
    }
    private String FromMarker;

    /**
     * ElectronicJournal method QueryContent parameter toMarker, see UPOS specification.
     * @return Name of upper bound marker.
     */
    public String getToMarker() {
        return ToMarker;
    }
    private String ToMarker;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param fileName      Filename to be used to store queried contents.
     * @param fromMarker    Name of lower bound marker.
     * @param toMarker      Name of upper bound marker.
     */
    public QueryContent(JposCommonProperties props, String fileName, String fromMarker, String toMarker) {
        super(props);
        FileName = fileName;
        ToMarker = toMarker;
        FromMarker = fromMarker;
    }

    @Override
    public void invoke() throws JposException {
        ((ElectronicJournalService)Props.EventSource).ElectronicJournalInterface.queryContent(this);
    }
}
