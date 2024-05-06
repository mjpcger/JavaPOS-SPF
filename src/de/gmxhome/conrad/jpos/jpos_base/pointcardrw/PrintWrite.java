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

package de.gmxhome.conrad.jpos.jpos_base.pointcardrw;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

import java.util.*;

import static jpos.JposConst.*;
import static jpos.PointCardRWConst.*;

public class PrintWrite extends JposOutputRequest {
    /**
     * PointCardRW method PrintWrite parameter kind, see UPOS specification.
     * @return PrintWrite parameter <i>kind</i>.
     */
    public int getKind() {
        return Kind;
    }
    private final int Kind;

    /**
     * PointCardRW method PrintWrite parameter hposition, see UPOS specification.
     * @return PrintWrite parameter <i>hposition</i>.
     */
    public int getHPosition() {
        return HPosition;
    }
    private final int HPosition;

    /**
     * PointCardRW method PrintWrite parameter vposition, see UPOS specification.
     * @return PrintWrite parameter <i>vposition</i>.
     */
    public int getVPosition() {
        return VPosition;
    }
    private final int VPosition;

    /**
     * Get PointCardRW property Write<i>trackno</i>Data (1 &le; trackno &le; 6) contents when the method
     * PrintWrite has been invoked.
     * @param trackno Track number.
     * @return Contents of Write<i>trackno</i>Date property at the time when WriteData has been called from application.
     */
    public String getWriteTrackData(int trackno) {
        if (--trackno < 0 || trackno >= WriteTrackData.length)
            return null;
        return WriteTrackData[trackno];
    }
    private final String[] WriteTrackData;

    /**
     * Get PointCardRW property TracksToWrite contents when method PrintWrite has been invoked.
     * @return Contents of TracksToWrite property at the time when WriteData has been called from application.
     */
    public  int getTracksToWrite() {
        return TracksToWrite;
    }
    private final int TracksToWrite;

    /**
     * PointCardRW method PrintWrite parameter data, converted to a List&lt;Object&gt; with PointCardRWService method outputDataParts.
     * @return List&lt;Object&gt; containing parsed print data contained in PrintWrite parameter data.
     */
    public List<PointCardRWService.PrintDataPart> getData() {
        return Data;
    }
    private final List<PointCardRWService.PrintDataPart> Data;

    /**
     * Sets WriteStates, one value per track, must be set during write track operations. Will be used to set WriteState
     * properties of property set either via OutputCompleteEvent or ErrorEvent.
     * @param track Track of status, must be between 1 and 6.
     * @param value Status value for the specified track, should be one of the predefined values.
     */
    public void setWriteState(int track, int value) {
        WriteStates[track - 1] = value;
    }
    private final Integer[] WriteStates;

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param kind      Parts of the point card that will be written or printed. See UPOS specification.
     * @param hposition The horizontal start position for printing.
     * @param vposition The vertical start position for printing.
     * @param data      Print data.
     */
    public PrintWrite(PointCardRWProperties props, int kind, int hposition, int vposition, String data) {
        super(props);
        Kind = kind;
        HPosition = hposition;
        VPosition = vposition;
        Data = ((PointCardRWService)props.EventSource).outputDataParts(data);
        TracksToWrite = props.TracksToWrite;
        WriteTrackData = Arrays.copyOf(props.WriteData, props.WriteData.length);
        WriteStates = initWriteStates();
    }

    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param kind      Parts of the point card that will be written or printed. See UPOS specification.
     * @param hposition The horizontal start position for printing.
     * @param vposition The vertical start position for printing.
     * @param data      Print data.
     */
    public PrintWrite(PointCardRWProperties props, int kind, int hposition, int vposition, List<PointCardRWService.PrintDataPart> data) {
        super(props);
        Kind = kind;
        HPosition = hposition;
        VPosition = vposition;
        Data = data;
        TracksToWrite = props.TracksToWrite;
        WriteTrackData = Arrays.copyOf(props.WriteData, props.WriteData.length);
        WriteStates = initWriteStates();
    }

    @Override
    public void invoke() throws JposException {
        PointCardRWService svc = (PointCardRWService)Props.EventSource;
        svc.PointCardRW.printWrite(this);
    }

    @Override
    public JposErrorEvent createErrorEvent(JposException ex) {
        return new PointCardRWErrorEvent(Props.EventSource, ex.getErrorCode(), ex.getErrorCodeExtended(), ex.getMessage(), WriteStates);
    }

    @Override
    public JposOutputCompleteEvent createOutputEvent() {
        if (WriteStates != null) {
            for (int i = 0; i < WriteStates.length; i++) {
                if (WriteStates[i] != null)
                    WriteStates[i] = JPOS_SUCCESS;
            }
        }
        return new PointCardRWOutputCompleteEvent(Props.EventSource, OutputID, WriteStates);
    }

    private Integer[] initWriteStates() {
        if ((Kind & 2) != 0) {
            return new Integer[] {
                    (TracksToWrite & PCRW_TRACK1) == 0 ? null : JPOS_E_FAILURE,
                    (TracksToWrite & PCRW_TRACK2) == 0 ? null : JPOS_E_FAILURE,
                    (TracksToWrite & PCRW_TRACK3) == 0 ? null : JPOS_E_FAILURE,
                    (TracksToWrite & PCRW_TRACK4) == 0 ? null : JPOS_E_FAILURE,
                    (TracksToWrite & PCRW_TRACK5) == 0 ? null : JPOS_E_FAILURE,
                    (TracksToWrite & PCRW_TRACK6) == 0 ? null : JPOS_E_FAILURE
           };
        }
        return null;
    }
}
