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
 * Output request executor for ElectronicJournal method PrintContentFile.
 */
public class PrintContentFile extends JposOutputRequest {
    /**
     * ElectronicJournal method PrintContentFile parameter fileName, see UPOS specification.
     * @return Filename to be used as journal source.
     */
    public String getFileName() {
        return FileName;
    }
    private String FileName;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param fileName Name of file that contains printing data.
     */
    public PrintContentFile(JposCommonProperties props, String fileName) {
        super(props);
        FileName = fileName;
    }

    @Override
    public void invoke() throws JposException {
        ((ElectronicJournalService)Props.EventSource).ElectronicJournalInterface.printContentFile(this);
    }
}
