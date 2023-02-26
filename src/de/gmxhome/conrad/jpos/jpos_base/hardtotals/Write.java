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

package de.gmxhome.conrad.jpos.jpos_base.hardtotals;

import jpos.JposException;

import java.util.Arrays;

/**
 * Output request executor for HardTotals method Write.
 */
public class Write extends ChangeRequest {
    /**
     * HardTotals method Write parameter data, see UPOS specification.
     * @return Write parameter data.
     */
    public byte[] getData() {
        return Data;
    }
    private byte[] Data;

    /**
     * HardTotals method Write parameter offset, see UPOS specification.
     * @return Write parameter offset.
     */
    public int getOffset() {
        return Offset;
    }
    private int Offset;

    /**
     * HardTotals method Write parameter count, see UPOS specification.
     * @return Write parameter count.
     */
    public int getCount() {
        return Count;
    }
    private int Count;

    /**
     * Constructor, stores given parameters for later use of Write operation.
     * @param props         Property set of device service.
     * @param hTotalsFile   Handle of a totals file.
     * @param data          Data to be written.
     * @param offset        Starting offset for write operation.
     * @param count         Number of bytes to be written.
     */
    public Write(HardTotalsProperties props, int hTotalsFile, byte[] data, int offset, int count) {
        super(props, hTotalsFile);
        Data = Arrays.copyOf(data, data.length);
        Offset = offset;
        Count = count;
    }

    @Override
    public void invoke() throws JposException {
        ((HardTotalsService) Props.EventSource).HardTotals.write(this);
    }
}
