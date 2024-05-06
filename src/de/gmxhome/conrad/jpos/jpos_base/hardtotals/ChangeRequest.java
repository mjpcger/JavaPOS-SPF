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

import de.gmxhome.conrad.jpos.jpos_base.*;

/**
 * Base class for output request executor for HardTotals method SetAll or Write.
 */
public class ChangeRequest extends JposOutputRequest {
    /**
     * HardTotals method Write or SetAll parameter hTotalsFile, see UPOS specification.
     * @return Write or SetAll parameter hTotalsFile.
     */
    public int getHTotalsFile() {
        return HTotalsFile;
    }
    private final int HTotalsFile;

    /**
     * Constructor, stores given parameters for later use of SetAll or Write operation.
     * @param props         Property set of device service.
     * @param hTotalsFile   Handle of a totals file.
     */
    public ChangeRequest(HardTotalsProperties props, int hTotalsFile) {
        super(props);
        HTotalsFile = hTotalsFile;
    }
}
