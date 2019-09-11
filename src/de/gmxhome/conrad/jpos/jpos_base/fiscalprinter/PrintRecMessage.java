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
 * Output request executor for FiscalPrinter method PrintRecMessage.
 */
public class PrintRecMessage extends OutputRequest {
    private int MessageType;

    /**
     * FiscalPrinter property MessageType at time of validation, see UPOS specification.
     * @return Value of property MessageType during validation.
     */
    public int getMessageType() {
        return MessageType;
    }

    private String Message;

    /**
     * FiscalPrinter method PrintRecMessage parameter message, see UPOS specification.
     * @return PrintRecMessage parameter <i>message</i>.
     */
    public String getMessage() {
        return Message;
    }
    /**
     * Constructor. Stores given parameters for later use.
     *
     * @param props Property set of device service.
     * @param message Text message to print.
     */
    public PrintRecMessage(FiscalPrinterProperties props, String message) {
        super(props);
        Message = message;
        MessageType = props.MessageType;
    }

    @Override
    public void invoke() throws JposException {
        ((FiscalPrinterProperties)Props).printRecMessage(this);
        super.invoke();
    }
}
