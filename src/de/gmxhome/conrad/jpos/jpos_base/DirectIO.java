/*
 * Copyright 2018 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base;

import jpos.*;

/**
 * Output request executor for common method DirectIO.
 */
public class DirectIO extends JposOutputRequest {
    /**
     * Common method DirectIO parameter command, see UPOS specification.
     * @return Parameter command.
     */
    public int getCommand() {
        return Command;
    }
    private final int Command;
    private final int[] Data;

    /**
     * Common method DirectIO parameter data, see UPOS specification.
     */
    public final int Datum;

    /**
     * Common method DirectIO parameter object, see UPOS specification.
     */
    public final Object Object;

    /**
     * Constructor. Stores given parameters for later use.
     * @param props     Property set of device service.
     * @param command   Command number, see UPOS specification.
     * @param datum     Integer value contained in data array, see UPOS specification.
     * @param object    Additional data, see UPOS specification.
     */
    public DirectIO(JposCommonProperties props, int command, int[] datum, Object object) {
        super(props);
        Command = command;
        Datum = (Data = datum)[0];
        Object = object;
    }

    /**
     * Constructor. Stores given parameters for later use. Since this version does not store the original parameter
     * <i>data</i> as passed by the application (this would be an int[1]), any change of that value will not be passed
     * back to the application at the end of the invoke method. Therefore, this constructor is deprecated now. You
     * should use the other constructor instead or (better) use the default implementation of method directIO in
     * JposCommonProperties for creation within an implementation of the validation or final part for a specific device
     * service.
     * @param props     Property set of device service.
     * @param command   Command number, see UPOS specification.
     * @param datum     Integer value contained in data array, see UPOS specification.
     * @param object    Additional data, see UPOS specification.
     */
    @Deprecated
    public DirectIO(JposCommonProperties props, int command, int datum, Object object) {
        super(props);
        Command = command;
        Datum = datum;
        Object = object;
        Data = null;
    }
    @Override
    public JposOutputCompleteEvent createOutputEvent() {
        return null;
    }

    @Override
    public void invoke() throws JposException {
        Props.EventSource.DeviceInterface.directIO(this);
        if (Data != null)
            Data[0] = Datum;
    }
}
