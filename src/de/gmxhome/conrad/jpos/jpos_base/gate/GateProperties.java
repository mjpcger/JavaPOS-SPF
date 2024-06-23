/*
 * Copyright 2020 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.gate;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

/**
 * Class containing the gate specific properties, their default values and default implementations of
 * GateInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Gate.
 */
public class GateProperties extends JposCommonProperties implements GateInterface {
    /**
     * UPOS property CapStatus. Default: true. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapGateStatus = true;

    /**
     * UPOS property GateStatus. Must be overwritten in the individual initOnEnable method.
     */
    public int GateStatus = 0;

    /**
     * Constructor. Sets ExclusiveUse to ExclusiveAllowed to match the Gate device model.
     *
     * @param dev Device index
     */
    public GateProperties(int dev)
    {
        super(dev);
        ExclusiveUse = ExclusiveAllowed;
    }

    @Override
    public void initOnOpen() {
        super.initOnOpen();
    }

    @Override
    public void openGate() throws JposException {
    }

    @Override
    public void waitForGateClose(int timeout) throws JposException {
    }
}
