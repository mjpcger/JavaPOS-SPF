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

package SampleFiscalPrinter;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.linedisplay.*;
import jpos.JposConst;
import jpos.JposException;
import jpos.LineDisplayConst;

import static SampleFiscalPrinter.SampleFiscalPrinter.*;

import javax.swing.*;

/**
 * Class implementing the JposDisplayInterface for the sample fiscal printer.
 */
class SampleFiscalPrinterDisplay extends LineDisplayProperties implements SampleFiscalPrinterStatusUpdater {
    private SampleFiscalPrinter Dev;

    private char Lines[][];

    @Override
    public void updateState(boolean notused) {
        char[] state = Dev.getCurrentState();
        if (PowerNotify == JposConst.JPOS_PN_ENABLED) {
            int value = state.length <= DRAWER ? JposConst.JPOS_PS_OFF_OFFLINE : JposConst.JPOS_PS_ONLINE;
            new JposStatusUpdateEvent(EventSource, value).setAndCheckStatusProperties();
        }
    }

    private class Coordinates {
        int Row;
        int Column;
        Coordinates(int row, int column) {
            Row = row;
            Column = column;
        }
    }

    /**
     * Constructor.
     * @param index     Device index.
     * @param dev Instance of SampleFiscalPrinter this object belongs to.
     */
    SampleFiscalPrinterDisplay(int index, SampleFiscalPrinter dev) {
        super(index);
        Dev = dev;
    }

    @Override
    public void open() throws JposException {
        super.open();
        Lines = new char[DeviceRows][DeviceColumns];
        clearIfNotLess(0, 0);
    }

    @Override
    public void deviceEnabled(boolean enable) throws JposException {
        super.deviceEnabled(enable);
        Dev.updateStates(this, enable);
    }

    @Override
    public void claim(int timeout) throws JposException {
        Dev.startPolling();
        super.claim(timeout);
    }

    @Override
    public void release() throws JposException {
        super.release();
        Dev.stopPolling();
    }

    @Override
    public void checkHealth(int level) throws JposException {
        if (level == JposConst.JPOS_CH_INTERNAL) {
            CheckHealthText = "Internal CheckHealth: OK";
            return;
        }
        if (level == JposConst.JPOS_CH_INTERACTIVE) {
            Dev.synchronizedMessageBox("Press OK to start health test.", "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
        }
        CheckHealthText = (level == JposConst.JPOS_CH_EXTERNAL ? "Externel" : "Interactive") + " CheckHealth: ";
        try {
            ((LineDisplayService) EventSource).clearText();
            ((LineDisplayService) EventSource).displayTextAt(1, 3, "CheckHealth: OK!", LineDisplayConst.DISP_DT_NORMAL);
            CheckHealthText += "OK";
        } catch (JposException e) {
            CheckHealthText += "Failed, " + e.getMessage();
        }
        super.checkHealth(level);
    }

    @Override
    public void clearText() throws JposException {
        CursorRow = CursorColumn = 0;
        clearIfNotLess(0, 0);
        refreshWindow(0);
        super.clearText();
    }

    @Override
    public DisplayText displayText(String data, int attribute) throws JposException {
        DisplayText request = super.displayText(data, attribute);
        request.AdditionalData = new Coordinates(CursorRow, CursorColumn);
        return request;
    }

    @Override
    public void displayText(DisplayText request) throws  JposException {
        Coordinates coordinates = (Coordinates)request.AdditionalData;
        LineDisplayService.DisplayDataPart[] parts = request.getData();
        for(LineDisplayService.DisplayDataPart o : parts) {
            if (o instanceof LineDisplayService.DisplayData){
                String data = ((LineDisplayService.DisplayData) o).getData();
                for (int i = 0; i < data.length(); i++) {
                    processChar(coordinates, data.charAt(i));
                }
            }
            else if (o instanceof LineDisplayService.ControlChar)
                processChar(coordinates, ((LineDisplayService.ControlChar)o).getControlCharacter());
        }
        CursorRow = coordinates.Row;
        CursorColumn = coordinates.Column;
        refreshWindow(0);
    }

    @Override
    public void refreshWindow(int index) throws JposException {
        String[] cmd = new String[]{"display", "", ""};
        for (int line = 0; line < Lines.length; line++) {
            cmd[1] = Integer.toString(line + 1);
            cmd[2] = new String(Lines[line]);
            String[] resp = Dev.sendrecv(cmd);
            Dev.check(resp == null || resp.length < 1, JposConst.JPOS_E_FAILURE, "Communication error");
            Dev.check(resp[0].charAt(0) != SUCCESS, JposConst.JPOS_E_FAILURE, resp.length != 3 ? "Unknown error" : "Error " + resp[1] + " [" + resp[2] + "]");
        }
    }

    @Override
    public void scrollText(int direction, int units) throws JposException {
        switch (direction) {
            case LineDisplayConst.DISP_ST_UP:
                scrollUp(units);
                break;
            case LineDisplayConst.DISP_ST_DOWN:
                scrollDown(units);
                break;
            case LineDisplayConst.DISP_ST_LEFT:
                scrollLeft(units);
                break;
            case LineDisplayConst.DISP_ST_RIGHT:
                scrollRight(units);
        }
        refreshWindow(0);
    }

    private void scrollRight(int units) {
        if (!clearIfNotLess(units, DeviceColumns)) {
            for (int l = DeviceColumns - 1; l >= units; l--) {
                for (int i = 0; i < DeviceRows; i++) {
                    Lines[i][l] = Lines[i][l - units];
                }
            }
            while (--units >= 0) {
                for (int i = 0; i < DeviceRows; i++) {
                    Lines[i][units] = ' ';
                }
            }
        }
    }

    private void scrollLeft(int units) {
        if (!clearIfNotLess(units, DeviceColumns)) {
            for (int l = units; l < DeviceColumns; l++) {
                for (int i = 0; i < DeviceRows; i++) {
                    Lines[i][l - units] = Lines[i][l];
                }
            }
            do {
                for (int i = 0; i < DeviceRows; i++) {
                    Lines[i][DeviceColumns - units] = ' ';
                }
            } while (--units > 0);
        }
    }

    private void scrollDown(int units) {
        if (!clearIfNotLess(units, DeviceRows)) {
            for (int l = DeviceRows - 1; l >= units; l--) {
                for (int i = 0; i < DeviceColumns; i++) {
                    Lines[l][i] = Lines[l - units][i];
                }
            }
            while (--units >= 0) {
                for (int i = 0; i < DeviceColumns; i++) {
                    Lines[units][i] = ' ';
                }
            }
        }
    }

    private void scrollUp(int units) {
        if (!clearIfNotLess(units, DeviceRows)) {
            for (int l = units; l < DeviceRows; l++) {
                for (int i = 0; i < DeviceColumns; i++) {
                    Lines[l - units][i] = Lines[l][i];
                }
            }
            do {
                for (int i = 0; i < DeviceColumns; i++) {
                    Lines[DeviceRows - units][i] = ' ';
                }
            } while (--units > 0);
        }
        return;
    }

    private boolean clearIfNotLess(int units, int dimension) {
        if (units >= dimension) {
            for (char[] line : Lines) {
                for (int i = 0; i < line.length; i++)
                    line[i] = ' ';
            }
            return true;
        }
        return false;
    }

    private void processChar(Coordinates coordinates, char c) {
        if (c == '\r') {
            coordinates.Column = 0;
            return;
        }
        if (c == '\n' || coordinates.Column == DeviceColumns) {
            if (coordinates.Row < DeviceRows - 1) {
                coordinates.Row++;
                coordinates.Column = 0;
            } else {
                scrollUp(1);
            }
            if (c == '\n')
                return;
        }
        Lines[coordinates.Row][coordinates.Column] = c;
        coordinates.Column++;
    }
}
