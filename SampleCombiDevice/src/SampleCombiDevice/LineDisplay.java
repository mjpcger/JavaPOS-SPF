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

package SampleCombiDevice;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.linedisplay.*;
import jpos.*;

import javax.swing.*;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import static SampleCombiDevice.Device.*;
import static javax.swing.JOptionPane.*;
import static jpos.LineDisplayConst.*;
import static jpos.JposConst.*;

/**
 * Class implementing the LineDisplayInterface for the sample combi device.
 */
public class LineDisplay extends LineDisplayProperties {
    private final Device Dev;
    // Command for text output
    private static final byte CmdTextOutPrefix = 'T';
    private static final int TextLinePos = 1;
    private static final int TextLengthPos = 2;
    private static final int TextStartPos = 4;

    /**
     * Constructor. Gets instance of Device to be used as communication object. Device index for
     * sample is always 0.
     * @param dev Instance of Device this object belongs to.
     */
    public LineDisplay(Device dev) {
        super(0);
        Dev = dev;
    }

    @Override
    public void deviceEnabled(boolean enable) throws JposException {
        super.deviceEnabled(enable);
        Dev.updateCommonStates(this, enable);
    }

    @Override
    public void claim(int timeout) throws JposException {
        Dev.startCommunication();

        super.claim(timeout);
    }

    @Override
    public void release() throws JposException {
        super.release();
        Dev.stopCommunication();
    }

    @Override
    public void checkHealth(int level) throws JposException {
        if (Dev.internalCheckHealth(this, level)) {
            try {
                ((LineDisplayService) EventSource).clearText();
                ((LineDisplayService) EventSource).displayTextAt(1, 3, "CheckHealth: OK!", DISP_DT_NORMAL);
                CheckHealthText = "OK";
            } catch (JposException e) {
                CheckHealthText = "Failed, " + e.getMessage();
            }
            if (level == JPOS_CH_INTERACTIVE)
                synchronizedMessageBox("LineDisplay check " + CheckHealthText + ".", "CheckHealth LineDisplay",
                        (CheckHealthText.equals("OK") ? INFORMATION_MESSAGE : ERROR_MESSAGE));
            CheckHealthText = (level == JPOS_CH_EXTERNAL ? "Externel" : "Internal") + " CheckHealth: " + CheckHealthText;
        }
        super.checkHealth(level);
    }

    @Override
    public void clearText() throws JposException {
        int row, column;
        for (row = 0; row < Dev.DisplayContents.length; row++) {
            for (column = 0; column < Dev.DisplayContents[row].length; column++) {
                Dev.DisplayAttributes[row][column] = Dev.NormalChar;
                Dev.DisplayContents[row][column] = ' ';
            }
        }
        CursorRow = CursorColumn = 0;
        refreshWindow(0);
        super.clearText();
    }

    private String Encoding;

    @Override
    public void characterSet(int charset) throws JposException {
        if (charset != CharacterSet) {
            Dev.CpChanged = false;
        }
        super.characterSet(charset);
    }

    @Override
    public void deviceBrightness(int b) throws JposException {
        super.deviceBrightness(b);
        refreshWindow(0);
    }

    @Override
    public void scrollText(int direction, int units) throws JposException {
        boolean otherDirection = false;
        switch (direction) {
            case DISP_ST_DOWN:
                otherDirection = true;
            case DISP_ST_UP:
                scrollVertical(units, otherDirection);
                break;
            case DISP_ST_LEFT:
                otherDirection = true;
            case DISP_ST_RIGHT:
                scrollHorizontal(units, otherDirection);
                break;
        }
        super.scrollText(direction, units);
    }

    @Override
    public DisplayText displayText(String text, int attribute) throws JposException {
        DisplayText request = super.displayText(text, attribute);
        LineDisplayService.DisplayDataPart[] data = request.getData();
        request.AdditionalData = new Device.DisplayCoordinates(CursorRow, CursorColumn, CursorUpdate);
        if (CursorUpdate && InterCharacterWait > 0 && MarqueeType == DISP_MT_NONE) {
            // We update the coordinates here
            ((SampleCombiDevice.Device.DisplayCoordinates)request.AdditionalData).Update = false;
            for (Object o : data) {
                if (o instanceof LineDisplayService.DisplayData) {
                    CursorColumn += ((LineDisplayService.DisplayData) o).getData().length();
                    while (CursorColumn > Columns) {
                        CursorColumn -= Columns;
                        if (CursorRow < Rows - 1)
                            CursorRow++;
                    }
                }
                else if (o instanceof LineDisplayService.ControlChar) {
                    CursorColumn = 0;
                    if (CursorRow < Rows - 1 && ((LineDisplayService.ControlChar) o).getControlCharacter() == '\n')
                        CursorRow++;
                }
            }
        }
        return request;
    }

    private final SyncObject InterCharacterWaiter = new SyncObject();

    @Override
    public void interCharacterWait(int b) throws JposException {
        int prev = InterCharacterWait;
        super.interCharacterWait(b);
        if (prev != b && b == 0)
            InterCharacterWaiter.signal();
    }

    @Override
    public void displayText(DisplayText request) throws  JposException {
        char attribute = Dev.NormalChar;
        SampleCombiDevice.Device.DisplayCoordinates coordinates = (SampleCombiDevice.Device.DisplayCoordinates)request.AdditionalData;
        InterCharacterWaiter.reset();
        for(Object o : request.getData()) {
            char blinkReverseChar = 'a';
            if (o instanceof LineDisplayService.DisplayData){
                String data = ((LineDisplayService.DisplayData) o).getData();
                for (int i = 0; i < data.length(); i++) {
                    processChar(coordinates, data.charAt(i), attribute);
                    if (InterCharacterWait > 0 && MarqueeType == DISP_MT_NONE) {
                        refreshWindow(0);
                        InterCharacterWaiter.suspend(InterCharacterWait);
                    }
                }
            }
            else if (o instanceof LineDisplayService.ControlChar)
                processChar(coordinates, ((LineDisplayService.ControlChar) o).getControlCharacter(), attribute);
            else if (o instanceof LineDisplayService.EscNormalize)
                attribute = Dev.NormalChar;
            else if (o instanceof LineDisplayService.EscSimple && attribute != blinkReverseChar) {
                LineDisplayService.EscSimple esc = (LineDisplayService.EscSimple) o;
                // Character attribute values
                char reverseChar = 'r';
                char blinkChar = 'b';
                if ((esc.getBlinking() && (esc.getReverse() || attribute == reverseChar)) || (esc.getReverse() && attribute == blinkChar))
                    attribute = blinkReverseChar;
                else
                    attribute = esc.getReverse() ? reverseChar : blinkChar;
            }
        }
        if (coordinates.Update) {
            CursorRow = coordinates.Line;
            CursorColumn = coordinates.Column;
        }
        refreshWindow(0);
    }

    private void processChar(SampleCombiDevice.Device.DisplayCoordinates coordinates, char c, char attribute) {
        if (c == '\r') {
            coordinates.Column = 0;
            return;
        }
        if (c == '\n' || coordinates.Column == 20) {
            coordinates.Column = 0;
            if (coordinates.Line == 0) {
                coordinates.Line++;
            } else {
                for (int i = 0; i < 20; i++) {
                    Dev.DisplayContents[0][i] = Dev.DisplayContents[1][i];
                    Dev.DisplayContents[1][i] = ' ';
                    Dev.DisplayAttributes[0][i] = Dev.DisplayAttributes[1][i];
                    Dev.DisplayAttributes[1][i] = Dev.NormalChar;
                }
            }
            if (c == '\n')
                return;
        }
        if (!MapCharacterSet && CharacterSet != DISP_CS_UNICODE) {
            try {
                c = new String(new byte[]{(byte)(c & 0xff)}, Encoding).charAt(0);
            } catch (UnsupportedEncodingException ignored) {}
        }
        Dev.DisplayContents[coordinates.Line][coordinates.Column] = c;
        Dev.DisplayAttributes[coordinates.Line][coordinates.Column] = attribute;
        coordinates.Column++;
    }

    private void sendTextLine(String linestr, char row) throws JposException {
        byte[] line;
        if (!Dev.CpChanged)
            setDeviceCodepage(CharacterSet);
        try {
            line = linestr.getBytes(Encoding);
        } catch (UnsupportedEncodingException e) {
            throw new JposException(JPOS_E_ILLEGAL, e.getMessage(), e);
        }
        byte[] buffer = Arrays.copyOf(new byte[]{CmdTextOutPrefix}, line.length + TextStartPos);
        System.arraycopy(line,0, buffer, TextStartPos, line.length);
        buffer[TextLinePos] = (byte)row;
        for (int i = TextStartPos, len = linestr.length(); --i >= TextLengthPos; len /= 10)
            buffer[i] = (byte)(len % 10 + '0');
        Dev.sendCommand(buffer, NoResponse);
    }

    private void setDeviceCodepage(int charset) throws JposException {
        String[] encodings = {"UTF-8", "ASCII", "cp437", "cp1252"};
        int[][] pairs = {
                {'0', DISP_CS_UNICODE},
                {'1', DISP_CS_ASCII},
                {'2', 437},
                {'3', 1252}
        };
        for (int[] pair : pairs) {
            if (charset == pair[1]) {
                byte[] buffer = {'C', (byte) pair[0]};
                Dev.CpChanged = false;
                Dev.sendCommand(buffer, RespFromDisplay);
                check(!Dev.CpChanged, JPOS_E_ILLEGAL, "Character set could not be changed to " + pair[1]);
                super.characterSet(charset);
                Encoding = encodings[pair[0] - '0'];
                return;
            }
        }
        throw new JposException(JPOS_E_ILLEGAL, "Unsupported character set: " + charset);
    }

    @Override
    public void refreshWindow(int index) throws JposException {
        if (DeviceBrightness < 50) {
            sendTextLine(" ", '0');
            sendTextLine(" ", '1');
        } else {
            sendTextLine(new String(Dev.DisplayContents[0]) + new String(Dev.DisplayAttributes[0]), '0');
            sendTextLine(new String(Dev.DisplayContents[1]) + new String(Dev.DisplayAttributes[1]), '1');
        }
        super.refreshWindow(index);
    }

    private void scrollHorizontal(int units, boolean otherDirection) throws JposException {
        if (units >= 20) {
            clearText();
            return;
        }
        if (units > 0) {
            int i;
            for (i = 0; i < 20 - units; i++) {
                if (otherDirection) {
                    Dev.DisplayContents[0][i] = Dev.DisplayContents[0][i + units];
                    Dev.DisplayContents[1][i] = Dev.DisplayContents[1][i + units];
                    Dev.DisplayAttributes[0][i] = Dev.DisplayAttributes[0][i + units];
                    Dev.DisplayAttributes[1][i] = Dev.DisplayAttributes[1][i + units];
                } else {
                    Dev.DisplayContents[0][19 - i] = Dev.DisplayContents[0][19 - i - units];
                    Dev.DisplayContents[1][19 - i] = Dev.DisplayContents[1][19 - i - units];
                    Dev.DisplayAttributes[0][19 - i] = Dev.DisplayAttributes[0][19 - i - units];
                    Dev.DisplayAttributes[1][19 - i] = Dev.DisplayAttributes[1][19 - i - units];
                }
            }
            while (i < 20) {
                if (otherDirection) {
                    Dev.DisplayContents[0][i] = Dev.DisplayContents[1][i] = ' ';
                    Dev.DisplayAttributes[0][i] = Dev.DisplayAttributes[1][i] = Dev.NormalChar;
                }
                else {
                    Dev.DisplayContents[0][19 - i] = Dev.DisplayContents[1][19 - i] = ' ';
                    Dev.DisplayAttributes[0][19 - i] = Dev.DisplayAttributes[1][19 - i] = Dev.NormalChar;
                }
                i++;
            }
            refreshWindow(0);
        }
    }

    private void scrollVertical(int units, boolean otherDirection) throws JposException {
        if (units >= 2) {
            clearText();
            return;
        }
        if (units == 1) {
            for (int i = 0; i < 20; i++) {
                if (otherDirection) {
                    Dev.DisplayContents[1][i] = Dev.DisplayContents[0][i];
                    Dev.DisplayContents[0][i] = ' ';
                    Dev.DisplayAttributes[1][i] = Dev.DisplayAttributes[0][i];
                    Dev.DisplayAttributes[0][i] = Dev.NormalChar;
                } else {
                    Dev.DisplayContents[0][i] = Dev.DisplayContents[1][i];
                    Dev.DisplayContents[1][i] = ' ';
                    Dev.DisplayAttributes[0][i] = Dev.DisplayAttributes[1][i];
                    Dev.DisplayAttributes[1][i] = Dev.NormalChar;
                }
            }
            refreshWindow(0);
        }
    }
}
