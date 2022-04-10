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

package SamplePOSPrinter;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.posprinter.*;
import jpos.*;
import net.bplaced.conrad.log4jpos.Level;

import java.nio.*;
import java.nio.charset.*;
import java.util.*;

/**
 * Class implementing the POSPrinterInterface for the sample pos printer.
 */
public class POSPrinter extends POSPrinterProperties {
    private SamplePOSPrinter.Device Dev;
    private static final int LineCharsListVals[] = {42, 56};
    private static final byte[] CodePages = { '6', '0', '6', '0', '1', '2', '3', '4', '5', '6' };    // Default encoding of Java is UFT-8
    private static final byte[] Fonts = { 'A', 'B' };
    private int CurrentCodePageIndex = 0;

    private final static byte LineFeed = '\12';

    // Byte arrays for printer commands.
    private final static byte[] CmdCut = {'\14'};
    private byte[] CmdBold = {'\33', 'b'};
    private byte[] CmdColor = {'\33', 'c'};
    private byte[] CmdFont = {'\33', 'f'};
    private byte[] CmdOrientation = {'\33', 'o'};
    private byte[] CmdCodepage = {'\33', 'p'};
    private byte[] CmdUnderline = {'\33', 'u'};

    // Matrix for unit computation
    private final int[][] FactorMatrix;

    /**
     * Constructor. Gets instance of Device to be used as communication object. Device index for
     * sample is always 0.
     * @param dev Instance of Device this object belongs to.
     */
    public POSPrinter(SamplePOSPrinter.Device dev) {
        super(0);
        Dev = dev;
        FactorMatrix = new int[][]{
                new int[]{POSPrinterConst.PTR_MM_DOTS, dev.LineWidth},  // LineWidth dots per line
                new int[]{POSPrinterConst.PTR_MM_METRIC, 8000},     // 8000/100 mm per line
                new int[]{POSPrinterConst.PTR_MM_ENGLISH, 3150},    // 3150/1000 inch per line
                new int[]{POSPrinterConst.PTR_MM_TWIPS, 4535},      // 4535/1440 inch per line
        };
    }

    @Override
    public void claim(int timeout) throws JposException {
        Dev.startCommunication(timeout);
        super.claim(timeout);
        setCurrentValues();
    }

    private void setCurrentValues() {
        int index;
        for (index = 0; index < Dev.CharSetListVals.length; index++) {
            if (CharacterSet == Dev.CharSetListVals[index]) {
                CurrentCodePageIndex = index;
                break;
            }
        }
        for (index = 0; index < LineCharsListVals.length; index++) {
            if (RecLineChars > LineCharsListVals[index]) {
                Dev.CurrentFontIndex = index;
                break;
            }
        }
    }


    @Override
    public void release() throws JposException {
        super.release();
        Dev.stopCommunication();
    }

    @Override
    public void deviceEnabled(boolean enable) throws JposException {
        SyncObject obj = Dev.StartWaiter;
        if (obj != null)
            obj.suspend(SyncObject.INFINITE);
        super.deviceEnabled(enable);
        updateStates(enable);
    }

    private void updateStates(boolean enable) {
        Dev.updateCommonStates(this, enable);
        if (enable) {
            CoverOpen = CoverOpen;
            RecEmpty = Dev.PaperState == Dev.PaperEnd;
            RecNearEnd = Dev.PaperState > Dev.PaperOk;
        }
    }

    private class PrinterState {
        int Cartridge = ((POSPrinterProperties)Dev.getClaimingInstance(Dev.ClaimedPOSPrinter, 0)).RecCurrentCartridge;
        int CharsetIndex = Dev.CurrentFontIndex;
        int CodeIndex = CurrentCodePageIndex;
    }

    @Override
    public void checkHealth(int level) throws JposException {
        CheckHealthText = "Interactive CheckHealth: ";
        switch (level) {
            case JposConst.JPOS_CH_INTERNAL:
                CheckHealthText = "Internal CheckHealth: OK.";
                break;
            case JposConst.JPOS_CH_EXTERNAL:
                CheckHealthText = "External CheckHealth: ";
            case JposConst.JPOS_CH_INTERACTIVE:
                try {
                    ((POSPrinterService) EventSource).printImmediate(POSPrinterConst.PTR_S_RECEIPT, "\12\33|cA" + CheckHealthText + "OK.\12\33|fP");
                    CheckHealthText += "OK.";
                } catch (JposException e) {
                    CheckHealthText += "Error: " + e.getMessage() + ".";
                }
        }
        Dev.log(Level.DEBUG, LogicalName + ": CheckHealthText <- " + CheckHealthText);
        super.checkHealth(level);
    }

    @Override
    public void mapMode(int i) throws JposException {
        if (i != MapMode) {
            super.mapMode(i);
            RecLineSpacing = fromDotScale(Dev.LineSpacings[Dev.CurrentFontIndex]);
            RecLineWidth = fromDotScale((Dev.LineWidth / Dev.CharWidths[Dev.CurrentFontIndex]) * RecLineChars);
            RecLineHeight = fromDotScale(Dev.LineHeights[Dev.CurrentFontIndex]);
            Dev.log(Level.DEBUG, LogicalName + ": RecLineSpacing <- " + RecLineSpacing);
            Dev.log(Level.DEBUG, LogicalName + ": RecLineWidth <- " + RecLineWidth);
            Dev.log(Level.DEBUG, LogicalName + ": RecLineHeight <- " + RecLineHeight);
        }
    }

    private int fromDotScale(int dotval, int mapmode) {
        for (int[] vector : FactorMatrix) {
            if (vector[0] == mapmode) {
                return dotval * vector[1] / Dev.LineWidth;
            }
        }
        return dotval;
    }

    private int fromDotScale(int dotval) {
        return fromDotScale(dotval, MapMode);
    }

    private int toDotScale(int mapval, int mapmode) {
        for (int[] vector : FactorMatrix) {
            if (vector[0] == mapmode) {
                return mapval * Dev.LineWidth / vector[1];
            }
        }
        return mapval;
    }

    @Override
    public void characterSet(int value) throws JposException {
        int newindex;
        for (newindex = 0; newindex < Dev.CharSetListVals.length; newindex++) {
            if (value == Dev.CharSetListVals[newindex]) {
                CurrentCodePageIndex = newindex;
                break;
            }
        }
        super.characterSet(value);
    }

    @Override
    public void recLineChars(int value) throws JposException {
        int newindex;
        for (newindex = 0; newindex < LineCharsListVals.length; newindex++) {
            if (LineCharsListVals[newindex] >= value)
                break;
        }
        if (newindex == LineCharsListVals.length)
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "RecLineChars too high: " + value + ", maximum: " + LineCharsListVals[newindex - 1]);
        if (newindex != Dev.CurrentFontIndex) {
            RecLineSpacing = fromDotScale(Dev.LineSpacings[Dev.CurrentFontIndex = newindex]);
            RecLineWidth = fromDotScale((Dev.LineWidth / Dev.CharWidths[Dev.CurrentFontIndex]) * RecLineChars);
            RecLineHeight = fromDotScale(Dev.LineHeights[Dev.CurrentFontIndex]);
            RecLinesToPaperCut = (Dev.KnifeOffset + Dev.LineSpacings[Dev.CurrentFontIndex] - 1) / Dev.LineSpacings[Dev.CurrentFontIndex];
            Dev.log(Level.DEBUG, LogicalName + ": RecLineSpacing <- " + RecLineSpacing);
            Dev.log(Level.DEBUG, LogicalName + ": RecLineWidth <- " + RecLineWidth);
            Dev.log(Level.DEBUG, LogicalName + ": RecLineHeight <- " + RecLineHeight);
            Dev.log(Level.DEBUG, LogicalName + ": RecLinesToPaperCut <- " + RecLinesToPaperCut);
        }
        super.recLineChars(Dev.AdjustLineChars ? LineCharsListVals[Dev.CurrentFontIndex] : value);
    }

    @Override
    public void recLineHeight(int i) throws JposException {
        super.recLineHeight(fromDotScale(Dev.LineHeights[Dev.CurrentFontIndex]));
    }

    @Override
    public void recLineSpacing(int i) throws JposException {
        super.recLineSpacing(fromDotScale(Dev.LineSpacings[Dev.CurrentFontIndex]));
    }

    private POSPrinterService.PrintDataPart[] TopLogoData;
    private POSPrinterService.PrintDataPart[] BottomLogoData;

    @Override
    public POSPrinterService.PrintDataPart[] getLogoData(boolean top) {
        return top ? TopLogoData : BottomLogoData;
    }

    @Override
    public void setLogo(int location, String data) throws JposException {
        try {
            ((POSPrinterService)EventSource).validateData(POSPrinterConst.PTR_S_RECEIPT, data);
        } catch (JposException e) {
            if (e.getErrorCode() == JposConst.JPOS_E_FAILURE)
                throw new JposException(JposConst.JPOS_E_ILLEGAL, e.getMessage(), e);
        }
        List<POSPrinterService.PrintDataPart> dataparts = ((POSPrinterService)EventSource).outputDataParts(data);
        Object o = null;
        for (int i = 0; i < dataparts.size(); i++) {
            o = dataparts.get(i);
            if (o instanceof POSPrinterService.EscLogo)
                throw new JposException(JposConst.JPOS_E_ILLEGAL, "Data contains logo escape sequence");
        }
        if (location == POSPrinterConst.PTR_L_TOP) {
            TopLogoData = dataparts.toArray(new POSPrinterService.PrintDataPart[0]);
        }
        else {
            BottomLogoData = dataparts.toArray(new POSPrinterService.PrintDataPart[0]);
        }
    }

    @Override
    public void validateData(int station, POSPrinterService.PrintData data) throws JposException {
        checkNextMustFeed();
        super.validateData(station, data);
        if (data.getServiceIsMapping() && data.getCharacterSet() != POSPrinterConst.PTR_CS_UNICODE) {
            Charset charset = data.getCharacterSet() == POSPrinterConst.PTR_CS_ANSI ? Charset.defaultCharset() : Charset.forName(getCharsetString(data));
            CharsetEncoder encoder = charset.newEncoder();
            encoder.onMalformedInput(CodingErrorAction.REPORT);
            encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
            try {
                encoder.encode(CharBuffer.wrap(data.getPrintData()));
            } catch (UnmappableCharacterException e) {
                throw new JposException(JposConst.JPOS_E_ILLEGAL, "Unmappable character", e);
            } catch (CharacterCodingException e) {
                throw new JposException(JposConst.JPOS_E_FAILURE, "Malformed input", e);
            }
        }
    }

    private void checkNextMustFeed() throws JposException {
        if (NextMustFeed) {
            NextMustFeed = false;
            throw new JposException(JposConst.JPOS_E_FAILURE, "Carriage return without line feed not supported");
        }
        LastHasFed = false;
    }

    private String getCharsetString(POSPrinterService.PrintData data) {
        return data.getCharacterSet() == POSPrinterConst.PTR_CS_ASCII ? "ASCII" : (data.getCharacterSet() == POSPrinterConst.PTR_CS_UNICODE || data.getCharacterSet() == Dev.CS_UTF8 ? "UTF-8" : "cp" + data.getCharacterSet());
    }

    private boolean LastHasFed = false;
    private boolean NextMustFeed = false;

    @Override
    public void validateData(int station, POSPrinterService.ControlChar ctrl) throws JposException {
        if (!LastHasFed) {
            if (ctrl.getControlCharacter() == '\15')
                NextMustFeed = true;
            else
                LastHasFed = true;
        }
    }

    @Override
    public void validateData(int station, POSPrinterService.EscLogo esc) throws JposException {
        POSPrinterService.PrintDataPart[] logo = esc.getLogoData();
        int i;
        for (i = 0; i < logo.length - i; i++) {
            validateData(station, logo[i]);
        }
        POSPrinterService.PrintDataPart o;
        if (i < logo.length && (!((o = logo[i]) instanceof POSPrinterService.PrintData) || ((POSPrinterService.PrintData) o).getPrintData().length() > 0)) {
            validateData(station, o);
        }
    }

    @Override
    public void validateData(int station, POSPrinterService.EscFeed esc) throws JposException {
        NextMustFeed = false;
        if (esc.getReverse()) {
            LastHasFed = false;
            throw new JposException(JposConst.JPOS_E_FAILURE, "Reverse feeding not supported");
        }
        int count = esc.getCount();
        if (esc.getUnits()) {
            count = toDotScale(esc.getCount(), esc.getMapMode());
            if (++count % Dev.LineSpacings[Dev.CurrentFontIndex] > 2) {
                LastHasFed = false;
                throw new JposException(JposConst.JPOS_E_ILLEGAL, "Unit feed not supported, feed to nearest line");
            }
            count /= Dev.LineSpacings[Dev.CurrentFontIndex];
        }
        if (count > 0)
            LastHasFed = true;
        else
            NextMustFeed = true;
    }

    @Override
    public void validateData(int station, POSPrinterService.EscEmbedded esc) throws JposException {
        checkNextMustFeed();
    }

    @Override
    public void validateData(int station, POSPrinterService.EscCut esc) throws JposException {
        super.validateData(station,esc);
    }

    private void checkInError() throws JposException {
        Dev.check(Dev.InIOError, JposConst.JPOS_E_FAILURE, "No connection to device");
        Dev.check(Dev.PrinterError, JposConst.JPOS_E_FAILURE, "POSPrinter not operational");
        Dev.checkext(CoverOpen, POSPrinterConst.JPOS_EPTR_COVER_OPEN, "POSPrinter cover open");
        Dev.checkext(Dev.PaperState == Dev.PaperEnd, POSPrinterConst.JPOS_EPTR_REC_EMPTY, "Paper end");
    }

    @Override
    public void cutPaper(CutPaper request) throws JposException {
        checkInError();
        new SyncObject().suspend(request.EndSync == null ? Dev.AsyncProcessingCommandDelay : 0);      // for testing
        Dev.sendCommand(CmdCut);
    }

    @Override
    public PrintNormal printNormal(int station, String data) throws JposException {
        PrintNormal request = super.printNormal(station, data);
        request.AdditionalData = new PrinterState();
        return request;
    }

    @Override
    public PrintImmediate printImmediate(int station, String data) throws JposException {
        PrintImmediate request = super.printImmediate(station, data);
        request.AdditionalData = new PrinterState();
        return request;
    }

    @Override
    public void printNormal(PrintNormal request) throws JposException {
        new SyncObject().suspend(request.EndSync == null ? Dev.AsyncProcessingCommandDelay : 0);      // for testing
        checkInError();
        List<POSPrinterService.PrintDataPart> dataparts = request.getData();
        boolean[] complete = new boolean[]{true};
        PrinterState printerstate = (PrinterState)request.AdditionalData;
        byte[] binarydata = getBytes(dataparts, printerstate, complete);
        Dev.check(request.getSynchronousPrinting() && !complete[0], JposConst.JPOS_E_ILLEGAL, "Completing printer output impossible");
        if (binarydata.length > 0) {
            byte[] cmdnormalize = Dev.getCmdNormalize(printerstate.Cartridge);
            if (Arrays.equals(cmdnormalize, Arrays.copyOf(binarydata, cmdnormalize.length)) || binarydata[0] == LineFeed) {
                Dev.sendCommand(binarydata);
            }
            else {
                byte[] tobesent = Arrays.copyOf(cmdnormalize, cmdnormalize.length + binarydata.length);
                System.arraycopy(binarydata, 0, tobesent, cmdnormalize.length, binarydata.length);
                Dev.sendCommand(tobesent);
            }
            if (request.getSynchronousPrinting()) {
                // In synchronous print mode, we must throw an exception if printer is in error state afterwards.
                SyncObject obj = Dev.StartWaiter = new SyncObject();
                Dev.PollWaiter.signal();
                obj.suspend(SyncObject.INFINITE);
                checkInError();
            }
        }
    }

    private byte[] getBytes(List<POSPrinterService.PrintDataPart> dataparts, PrinterState statusData, boolean[] complete) throws JposException {
        ByteBuffer[] parts = new ByteBuffer[dataparts.size()];
        int totalsize = 0;
        for (int i = 0; i < parts.length; i++) {
            POSPrinterService.PrintDataPart data = dataparts.get(i);
            if (data instanceof POSPrinterService.PrintData) {
                if ((parts[i] = getPrintData((POSPrinterService.PrintData) data, statusData)) != null && parts[i].limit() > 0) {
                    totalsize += parts[i].limit();
                    complete[0] = false;
                }
            }
            else if (data instanceof POSPrinterService.ControlChar) {
                if ((parts[i] = getNewline(statusData)) != null && parts[i].limit() > 0) {
                    totalsize += parts[i].limit();
                    complete[0] = true;
                }
            }
            else if (data instanceof POSPrinterService.EscCut) {
                if ((parts[i] = getCut((POSPrinterService.EscCut) data, statusData)) != null && parts[i].limit() > 0) {
                    totalsize += parts[i].limit();
                    complete[0] = true;
                }
            }
            else if (data instanceof POSPrinterService.EscNormalize) {
                if ((parts[i] = getNormalize(statusData)) != null && parts[i].limit() > 0) {
                    totalsize += parts[i].limit();
                    complete[0] = false;
                }
            }
            else if (data instanceof POSPrinterService.EscLogo) {
                if ((parts[i] = getLogo((POSPrinterService.EscLogo) data, statusData, complete)) != null && parts[i].limit() > 0)
                    totalsize += parts[i].limit();
            }
            else if (data instanceof POSPrinterService.EscFeed) {
                if ((parts[i] = getFeed((POSPrinterService.EscFeed) data, statusData)) != null && parts[i].limit() > 0) {
                    totalsize += parts[i].limit();
                    complete[0] = true;
                }
            }
            else if (data instanceof POSPrinterService.EscEmbedded) {
                if ((parts[i] = getEmbeddedBytes((POSPrinterService.EscEmbedded) data, complete)) != null && parts[i].limit() > 0)
                    totalsize += parts[i].limit();
            }
            else if (data instanceof POSPrinterService.EscAlignment) {
                if ((parts[i] = getAlignment((POSPrinterService.EscAlignment) data)) != null && parts[i].limit() > 0) {
                    totalsize += parts[i].limit();
                    complete[0] = false;
                }
            }
            else if (data instanceof POSPrinterService.EscSimple) {
                if ((parts[i] = getSimpleAttribute((POSPrinterService.EscSimple) data)) != null && parts[i].limit() > 0) {
                    totalsize += parts[i].limit();
                    complete[0] = false;
                }
            }
            else if (data instanceof POSPrinterService.EscLine) {
                if ((parts[i] = getUnderline((POSPrinterService.EscLine) data)) != null && parts[i].limit() > 0) {
                    totalsize += parts[i].limit();
                    complete[0] = false;
                }
            }
            else if (data instanceof POSPrinterService.EscColor) {
                if ((parts[i] = getColor((POSPrinterService.EscColor) data)) != null && parts[i].limit() > 0) {
                    totalsize += parts[i].limit();
                    complete[0] = false;
                }
            }
        }
        byte[] binarydata = new byte[totalsize];
        int pos = 0;
        for (ByteBuffer buffer : parts) {
            if (buffer != null) {
                System.arraycopy(buffer.array(), 0, binarydata, pos, buffer.limit());
                pos += buffer.limit();
            }
        }
        return binarydata;
    }

    private ByteBuffer getPrintData(POSPrinterService.PrintData data, PrinterState statusData) {
        ByteBuffer databuffer;
        if (data.getServiceIsMapping() || data.getCharacterSet() == POSPrinterConst.PTR_CS_UNICODE) {
            Charset charset = data.getCharacterSet() == POSPrinterConst.PTR_CS_ANSI ? Charset.defaultCharset() : Charset.forName(getCharsetString(data));
            CharsetEncoder encoder = charset.newEncoder();
            encoder.onMalformedInput(CodingErrorAction.IGNORE);
            encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
            encoder.replaceWith(new byte[]{' '});
            try {
                databuffer = encoder.encode(CharBuffer.wrap(data.getPrintData()));
            } catch (CharacterCodingException e) {
                databuffer = ByteBuffer.allocate(0);
            }
        }
        else {
            byte[] adddata = new byte[data.getPrintData().length()];
            for (int i = data.getPrintData().length() - 1; i >= 0; i--)
                adddata[i] = (byte)data.getPrintData().charAt(i);
            databuffer = ByteBuffer.wrap(adddata);
        }
        if (statusData.CodeIndex != Dev.LastUsedCodePageIndex || statusData.CharsetIndex != Dev.LastUsedFontIndex) {
            ByteBuffer newbuffer = ByteBuffer.allocate(databuffer.position(0).limit()
                    + (statusData.CodeIndex != Dev.LastUsedCodePageIndex ? CmdCodepage.length + 1 : 0)
                    + (statusData.CharsetIndex != Dev.LastUsedFontIndex ? CmdFont.length + 1 : 0));
            if (statusData.CodeIndex != Dev.LastUsedCodePageIndex)
                newbuffer.put(CmdCodepage).put(CodePages[Dev.LastUsedCodePageIndex = statusData.CodeIndex]);
            if (statusData.CharsetIndex != Dev.LastUsedFontIndex)
                newbuffer.put(CmdFont).put(Fonts[Dev.LastUsedFontIndex = statusData.CharsetIndex]);
            databuffer = newbuffer.put(databuffer);
        }
        return databuffer;
    }

    private ByteBuffer getNewline(PrinterState state) {
        byte[] cmdnormalize = Dev.getCmdNormalize(state.Cartridge);
        return ByteBuffer.allocate(cmdnormalize.length + 1).put(LineFeed).put(cmdnormalize);
    }

    private ByteBuffer getCut(POSPrinterService.EscCut data, PrinterState state) {
        POSPrinterService.EscCut cut = data;
        byte[] cmdnormalize = Dev.getCmdNormalize(state.Cartridge);
        if (cut.getFeed() || cut.getStamp()) {
            ByteBuffer retbuffer = ByteBuffer.allocate(RecLinesToPaperCut + 1 + cmdnormalize.length);
            Arrays.fill(retbuffer.array(), 0, RecLinesToPaperCut, LineFeed);
            retbuffer.position(RecLinesToPaperCut);
            return retbuffer.put(CmdCut).put(cmdnormalize);
        }
        return ByteBuffer.allocate(1 + cmdnormalize.length).put(CmdCut).put(cmdnormalize);
    }

    private ByteBuffer getNormalize(PrinterState state) {
        return ByteBuffer.wrap(Dev.getCmdNormalize(state.Cartridge));
    }

    private ByteBuffer getLogo(POSPrinterService.EscLogo data, PrinterState status, boolean[] complete) throws JposException {
        List<POSPrinterService.PrintDataPart> logodata = new ArrayList<POSPrinterService.PrintDataPart>();
        POSPrinterService.PrintDataPart[] source = data.getLogoData();
        for (POSPrinterService.PrintDataPart part : source)
            logodata.add(part);
        return ByteBuffer.wrap(getBytes(logodata, status, complete));
    }

    private ByteBuffer getFeed(POSPrinterService.EscFeed data, PrinterState state) {
        if (!data.getReverse()) {
            int count = data.getCount();
            if (data.getUnits()) {
                count = (toDotScale(count, data.getMapMode()) + Dev.LineSpacings[Dev.CurrentFontIndex] / 2) / Dev.LineSpacings[Dev.CurrentFontIndex];
            }
            if (count == 0)
                count++;
            byte[] cmdnormalize = Dev.getCmdNormalize(state.Cartridge);
            ByteBuffer retbuffer = ByteBuffer.allocate(count + cmdnormalize.length);
            Arrays.fill(retbuffer.array(), 0, count, LineFeed);
            retbuffer.position(count);
            return retbuffer.put(cmdnormalize);
        }
        return null;
    }

    private ByteBuffer getEmbeddedBytes(POSPrinterService.EscEmbedded data, boolean[] complete) {
        ByteBuffer newdata = ByteBuffer.allocate(data.getData().length());
        for (int i = data.getData().length() - 1; i >= 0; --i) {
            byte val = (byte) data.getData().charAt(i);
            newdata.put(val);
            complete[0] = val == LineFeed || val == CmdCut[0];
        }
        return newdata;
    }

    private ByteBuffer getAlignment(POSPrinterService.EscAlignment data) {
        return ByteBuffer.allocate(CmdOrientation.length + 1).put(CmdOrientation).put((byte)(data.getAlignment() == POSPrinterConst.PTR_BC_LEFT ? 'l' : (data.getAlignment() == POSPrinterConst.PTR_BC_CENTER ? 'c' : 'r')));
    }

    private ByteBuffer getSimpleAttribute(POSPrinterService.EscSimple data) {
        if (data.getBold()) {
            return ByteBuffer.allocate(CmdBold.length + 1).put(CmdBold).put((byte)(data.getActivate() ? '1' : '0'));
        }
        return null;
    }

    private ByteBuffer getUnderline(POSPrinterService.EscLine data) {
        if (data.getUnderline()) {
            return ByteBuffer.allocate(CmdUnderline.length + 1).put(CmdUnderline).put((byte)(data.getThickness() != 0 ? '1' : '0'));
        }
        return null;
    }

    private ByteBuffer getColor(POSPrinterService.EscColor data) {
        if (!data.getRgb()) {
            return ByteBuffer.allocate(CmdColor.length + 1).put(CmdColor).put((byte)(data.getColor() != POSPrinterConst.PTR_COLOR_PRIMARY ? '1' : '0'));
        }
        return null;
    }

    @Override
    public void transactionPrint(TransactionPrint request) throws JposException {
        if (request.getControl() == POSPrinterConst.PTR_TP_NORMAL) {
            SyncObject obj = Dev.StartWaiter = new SyncObject();
            Dev.PollWaiter.signal();
            obj.suspend(SyncObject.INFINITE);
            Dev.check(!Dev.Online, JposConst.JPOS_E_FAILURE, "Device off");
            Dev.check(Dev.PrinterError, JposConst.JPOS_E_FAILURE, "Device offline");
            Dev.checkext(CoverOpen, POSPrinterConst.JPOS_EPTR_COVER_OPEN, "Cover open");
            Dev.checkext(Dev.PaperState == Dev.PaperEnd, POSPrinterConst.JPOS_EPTR_REC_EMPTY, "Cover open");
        }
    }
}
