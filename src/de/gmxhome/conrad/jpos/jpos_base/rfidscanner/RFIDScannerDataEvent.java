/*
 * Copyright 2023 Martin Conrad
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
 *
 */

package de.gmxhome.conrad.jpos.jpos_base.rfidscanner;

import de.gmxhome.conrad.jpos.jpos_base.JposBase;
import de.gmxhome.conrad.jpos.jpos_base.JposDataEvent;
import jpos.JposException;
import jpos.RFIDScanner;

import java.util.ArrayList;
import java.util.List;

/**
 * Data event implementation for RFIDScanner devices.
 */
public class RFIDScannerDataEvent extends JposDataEvent {
    List<RFIDScannerTagData> ScannedTags;

    /**
     * Constructor. Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     * @param tags   List containing data of all tags read that match the filter given by ReadTags or StartReadTags.
     */
    public RFIDScannerDataEvent(JposBase source, int state, List<RFIDScannerTagData> tags) {
        super(source, state);
        ScannedTags = new ArrayList<>();
        for (RFIDScannerTagData tag : tags)
            ScannedTags.add(tag);
    }

    @Override
    public void setDataProperties() {
        RFIDScannerService service = (RFIDScannerService)getSource();
        RFIDScannerProperties data = (RFIDScannerProperties)service.Props;
        synchronized (service.CurrentLabelData) {
            service.CurrentLabelData.clear();
            for (RFIDScannerTagData tagData : ScannedTags) {
                service.CurrentLabelData.add(tagData);
            }
            if (data.TagCount != ScannedTags.size()) {
                data.TagCount = service.CurrentLabelData.size();
                data.EventSource.logSet("TagCount");
            }
            if (data.TagCount > 0) {
                try {
                    service.setCurrentTagData(0);
                } catch (JposException e) {}    // Never occurs because TagCount > 0 and
            }
        }
    }
}
