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

package de.gmxhome.conrad.jpos.jpos_base.fiscalprinter;

import jpos.*;

/**
 * Output request base for FiscalPrinter methods that use properties PreLine and / or  PostLine.
 */
class PrePostOutputRequest extends OutputRequest {
    private final String PostLine;

    /**
     * Returns contents of property PostLine at the time where this instance has been created.
     * @return Contents of PostLine during request creation.
     */
    public String getPostLine() {
        return PostLine;
    }
    private final String PreLine;

    /**
     * Returns contents of property PreLine at the time where this instance has been created.
     * @return Contents of PreLine during request creation.
     */
    public String getPreLine() {
        return PreLine;
    }

    /**
     * Constructor. Stores PreLine and PostLine for later use.
     *
     * @param props Property set of device service.
     */
    public PrePostOutputRequest(FiscalPrinterProperties props) {
        super(props);
        PreLine = props.PreLine;
        PostLine = props.PostLine;
    }

    @Override
    public void invoke() throws JposException {
        invokeMethod();
        super.invoke();
    }

    /**
     * Invokes the command. Must be implemented in derived class. Calls finished() to wake up potentially waiting
     * threads.
     * <br>Replacement for invoke in derived classes. Should neither call super.invoke nor super.invokeMethod in
     * derived classes because the calling method just calls super.invoke.
     * @throws JposException FiscalPrinterException thrown by the command to be executed.
     */
    public void invokeMethod() throws JposException {
    }
}
