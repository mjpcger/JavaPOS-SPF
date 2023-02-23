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

package SampleHardTotals;

import de.gmxhome.conrad.jpos.jpos_base.*;
import de.gmxhome.conrad.jpos.jpos_base.hardtotals.*;
import jpos.*;
import jpos.config.JposEntry;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.Array;
import java.nio.*;
import java.util.*;

/**
 * Base of a JposDevice based implementation of JavaPOS HardTotals device service implementation for the sample device
 * based on the RandomAccessFile class. Error detection is not supported by this sample.
 * <p>Here a full list of all device specific properties that can be changed via jpos.xml:
 * <ul>
 *     <li>DevIndex: Positive integer between 0 and MaxTotals - 1, specifying one part of the hard totals file.
 *     Default: 0</li>
 *     <li>HardTotalsFileName: Path to the disk file that contains the HardTotals.</li>
 *     <li>MaxHardTotalFileSize: Maximum size of each of the MaxTotals Hard totals. Default 32768 (0x8000).</li>
 *     <li>MaxTotals: Maximum number of hard totals. Limited by memory and disk space because each hard total
 *     requires MaxHardTotalFileSize bytes in both, RAM and disk. Default: DevIndex + 1.</li>
 *     <li>SingleFileOnly: Must be true if only one file shall be supported per hard total, otherwise false. Default: false.</li>
 * </ul>
 * Keep in mind that values can only be set once. once the hard totals file has been created (that will be made after
 * first enable), these properties cannot be modified any longer.
 * The structure of the HardTotals file is as follows:
 * <ul>
 *     <li>MaxTotals: 32-bit integer value.</li>
 *     <li>MaxTotals times:
 *          <ul>
 *              <li>MaxHardTotalFileSize: 32-bit integer value, size of the corresponding HardTotals device,</li>
 *              <li>SingleFileOnly: 8-bit boolean value, specifies whether the corresponding HardTotals device supports
 *              only one single file.</li>
 *          </ul>
 *     </li>
 *     <li>MaxTotals times: N bytes representing the corresponding HardTotals, where N is the corresponding
 *     MaxHardTotalFileSize value.</li>
 * </ul>
 * The structure of a HardTotals device where SingleFileOnly is true:
 * <ul>
 *     <li>Size: 32-bit integer value, the size of the created file or 0 if file has not been created,</li>
 *     <li>Contents: Size byte contents of the hard totals file, if it exists.</li>
 *     <li>Undefined: N - Size - 4 byte random values, where N is the corresponding MaxHardTotalFileSize value.</li>
 * </ul>
 * The structure of a HardTotals device where SingleFileOnly is false:
 * <ul>
 *     <li>For NumberOfFiles: <ul>
 *         <li>Size: 32-bit integer value, the size of a file,</li>
 *         <li>Name: 10 byte file name,</li>
 *         <li>Contents: Size byte contents.</li>
 *     </ul></li>
 *     <li>At the end: 32-bit integer 0 as end of information mark.</li>
 *     <li>All remaining bytes of a HardTotals device contain random values.</li>
 * </ul>
 */
public class Device extends JposDevice {
    /**
     * Maximum number of hard totals. Initialized to the corresponding entry in jpos.xml of the entry used when the object
     * will be created. Default is the value of the DevIndex property of the same entry. The default value of DevIndex is 0.
     */
    Integer MaxTotals;

    private Integer[] MaxHardTotalFileSize;
    private Boolean[] SingleFileOnly;
    private byte[][] FileBuffer;
    private long[] HardTotalOffset;
    private int NextHandle = 0;
    private List<ChangeRequest>[] OpenChanges;
    private List<DirEntry>[] Directory;
    static private class DirEntry {
        String Name;        // File name
        int    Offset;      // Offset within file buffer
        int    Size;        // Size of file
        int    Handle;      // File handle
    }


    protected Device(String id) throws JposException {
        super(id);
        PhysicalDeviceDescription = "HardTotals device simulator";
        PhysicalDeviceName = "HardTotals Device Simulator";
        CapPowerReporting = JposConst.JPOS_PR_NONE;
        initHardTotals(id);
    }

    synchronized private void initHardTotals(String id) throws JposException {
        try {
            RandomAccessFile file = new RandomAccessFile(id, "r");
            file.seek(0);
            MaxTotals = file.readInt();
            MaxHardTotalFileSize = new Integer[MaxTotals];
            SingleFileOnly = new Boolean[MaxTotals];
            HardTotalOffset = new long[MaxTotals];
            OpenChanges = (List<ChangeRequest>[])new List[MaxTotals];
            Directory = (List<DirEntry>[])new List[MaxTotals];
            FileBuffer = new byte[MaxTotals][];
            for (int i = 0; i < MaxTotals; i++) {
                MaxHardTotalFileSize[i] = file.readInt();
                SingleFileOnly[i] = file.readBoolean();
                OpenChanges[i] = new ArrayList<ChangeRequest>();
                Directory[i] = new ArrayList<DirEntry>();
            }
            HardTotalOffset[0] = file.getFilePointer();
            for (int i = 1; i < MaxTotals; i++) {
                HardTotalOffset[i] = HardTotalOffset[i - 1] + MaxHardTotalFileSize[i - 1];
            }
            for (int i = 0; i < MaxTotals; i++) {
                byte[] buffer = FileBuffer[i] = new byte[MaxHardTotalFileSize[i]];
                file.readFully(buffer);
                int offset = 0;
                while(offset < MaxHardTotalFileSize[i]) {
                    DirEntry entry = new DirEntry();
                    entry.Size = ByteBuffer.wrap(buffer, offset, Integer.SIZE / Byte.SIZE).getInt();
                    if (SingleFileOnly[i]) {
                        entry.Offset = offset + Integer.SIZE / Byte.SIZE;
                        entry.Handle = ++NextHandle;
                        entry.Name = "";
                        Directory[i].add(entry);
                        break;
                    } else {
                        entry.Offset = offset + Integer.SIZE / Byte.SIZE + 10;
                        entry.Handle = ++NextHandle;
                        int j;
                        for (j = entry.Offset - 10; j < entry.Offset; j++) {
                            if (buffer[j] == 0)
                                break;
                        }
                        entry.Name = new String(buffer, entry.Offset - 10, j);
                        Directory[i].add(entry);
                    }
                    offset = entry.Offset + entry.Size;
                }
            }
            check(file.length() != HardTotalOffset[MaxTotals - 1] + MaxHardTotalFileSize[MaxTotals - 1], JposConst.JPOS_E_FAILURE, "Inconsistant file format: " + id);
        } catch (FileNotFoundException e) {
            MaxTotals = null;
            MaxHardTotalFileSize = null;
        } catch (IOException e) {
            throw new JposException(JposConst.JPOS_E_FAILURE, e.getMessage(), e);
        }
    }

    synchronized private void createHardTotals(String id) throws JposException {
        if (HardTotalOffset == null) {
            try {
                RandomAccessFile file = new RandomAccessFile(id, "r");
                throw new JposException(JposConst.JPOS_E_FAILURE, "Unexpected HardTotals file present: " + id);
            } catch (IOException e) {
                try {
                    RandomAccessFile file = new RandomAccessFile(id, "rws");
                    file.seek(0);
                    file.writeInt(MaxTotals);
                    for (int i = 0; i < MaxTotals; i++) {
                        file.writeInt(MaxHardTotalFileSize[i]);
                        file.writeBoolean(SingleFileOnly[i]);
                    }
                    OpenChanges = (List<ChangeRequest>[])new List[MaxTotals];
                    Directory = (List<DirEntry>[])new List[MaxTotals];
                    HardTotalOffset = new long[MaxTotals];
                    FileBuffer = new byte[MaxTotals][];
                    for (int i = 0; i < MaxTotals; i++) {
                        OpenChanges[i] = new ArrayList<ChangeRequest>();
                        Directory[i] = new ArrayList<DirEntry>();
                        HardTotalOffset[i] = file.getFilePointer();
                        ByteBuffer.wrap(FileBuffer[i] = new byte[MaxHardTotalFileSize[i]], 0, Integer.SIZE / Byte.SIZE).putInt(0);
                        file.write(FileBuffer[i]);
                    }
                } catch (IOException ee) {
                    throw new JposException(JposConst.JPOS_E_FAILURE, ee.getMessage(), ee);
                }
            }
        }
    }

    /**
     * Checks whether a JposEntry belongs to a predefined property value an if so,
     * sets the corresponding driver value for device global properties or for a specific hard totals device.
     * This is the first method called after creation of a Device instance. Therefore, all variables and properties
     * set within this method can be used in all other methods except the constructor.
     *
     * @param entry Entry to be checked, contains value to be set
     * @param index Index of the specific device.
     * @throws JposException if a property value is invalid
     */
    public void checkProperties(JposEntry entry, int index) throws JposException {
        super.checkProperties(entry);
        try {
            Object o;
            int val;
            if (MaxTotals == null) {
                if ((o = entry.getPropertyValue("MaxTotals")) != null && Integer.parseInt(o.toString()) > 0)
                    MaxTotals = Integer.parseInt(o.toString());
                else
                    MaxTotals = index + 1;
                hardTotalsInit(MaxTotals);
            }
            check(index >= MaxTotals, JposConst.JPOS_E_FAILURE, "DevIndex out of range: " + index + " >= " + MaxTotals);
            if (MaxHardTotalFileSize == null) {
                MaxHardTotalFileSize = new Integer[MaxTotals];
                SingleFileOnly = new Boolean[MaxTotals];
            }
            if (MaxHardTotalFileSize[index] == null) {
                if ((o = entry.getPropertyValue("MaxHardTotalFileSize")) != null && Integer.parseInt(o.toString()) > 4)
                    MaxHardTotalFileSize[index] = Integer.parseInt(o.toString());
                else
                    MaxHardTotalFileSize[index] = 0x8000;
            }
            if (SingleFileOnly[index] == null) {
                if ((o = entry.getPropertyValue("SingleFileOnly")) != null)
                    SingleFileOnly[index] = Boolean.parseBoolean(o.toString());
                else
                    SingleFileOnly[index] = false;
            }
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid JPOS property", e);
        }
    }

    @Override
    public void changeDefaults(HardTotalsProperties props) {
        props.CapSingleFile = SingleFileOnly[props.Index];
    }

    @Override
    public HardTotalsProperties getHardTotalsProperties(int index) {
        return new SampleHardTotalsProperties(index);
    }

    private class SampleHardTotalsProperties extends HardTotalsProperties {
        protected SampleHardTotalsProperties(int dev) {
            super(dev);
        }

        @Override
        public void checkHealth(int level) throws JposException {
            String result = " checkHealth: OK";
            String typestr = "Internal";
            if (level == JposConst.JPOS_CH_EXTERNAL)
                typestr = "External";
            else if (level == JposConst.JPOS_CH_INTERACTIVE) {
                typestr = "Interactive";
                synchronizedMessageBox("CheckHealth result:\n" + typestr + result, "CheckHealth", JOptionPane.INFORMATION_MESSAGE);
            }
            CheckHealthText = typestr + result;
        }

        private Map<Integer, DirEntry> Files;

        @Override
        public void deviceEnabled(boolean enable) throws JposException {
            createHardTotals(ID);
            for (DirEntry e : Directory[Index]) {
                Files.put(e.Handle, e);
            }
            if (FileBuffer == null) {
                FileBuffer = new byte[MaxTotals][];
                try {
                    RandomAccessFile file = new RandomAccessFile(ID, "r");
                    file.seek(HardTotalOffset[0]);
                    for (int i = 0; i < MaxTotals; i++) {
                        file.read(FileBuffer[i] = new byte[MaxHardTotalFileSize[i]]);
                    }
                } catch (IOException e) {
                    throw new JposException(JposConst.JPOS_E_FAILURE, e.getMessage(), e);
                }
            }
            super.deviceEnabled(enable);
        }

        @Override
        public void initOnEnable(boolean enable) {
            super.initOnEnable(enable);
            NumberOfFiles = Directory[Index].size();
            EventSource.logSet("NumberOfFiles");
            if (CapSingleFile) {
                TotalsSize = FreeData = MaxHardTotalFileSize[Index] - 4; // Capacity reduced by size (4 byte int)
                for (DirEntry entry : Directory[Index]) {
                    FreeData -= entry.Size;
                }
            } else {
                TotalsSize = FreeData = MaxHardTotalFileSize[Index] - 18;// Capacity reduced by directory size (4 byte length, 10 byte name) and EOD mark (4 byte 0)
                for (DirEntry entry : Directory[Index]) {
                    FreeData -= 14 + entry.Size;            // Reduce by file size + directory size (4 byte length, 10 byte length)
                }
            }
            EventSource.logSet("TotalsSize");
            EventSource.logSet("FreeData");
        }

        @Override
        synchronized public void create(String fileName, int[] hTotalsFile, int size, boolean errorDetection) throws JposException {
            List<DirEntry> entries = Directory[Index];
            for (DirEntry e : entries)
                check (e.Name.equals(fileName), JposConst.JPOS_E_EXISTS, "File exists: " + fileName);
            checkext (size > FreeData, HardTotalsConst.JPOS_ETOT_NOROOM, "File too large for " + fileName + ": " + size);
            DirEntry entry = new DirEntry();
            entry.Size = size;
            entry.Name = fileName;
            byte[] buffer = new byte[(CapSingleFile ? 0 : 10) + Integer.SIZE / Byte.SIZE];
            ByteBuffer.wrap(buffer).putInt(entry.Size);
            int j = Integer.SIZE / Byte.SIZE;
            for (int i = 0; i < fileName.length(); i++)
                buffer[j++] = (byte)fileName.indexOf(i);
            while(j < buffer.length)
                buffer[j++] = 0;
            entry.Offset = buffer.length;
            int offset;
            if (entries.size() == 0) {
                offset = 0;
            } else {
                DirEntry last = entries.get(entries.size() - 1);
                offset = last.Offset + last.Size;
                entry.Offset = offset + 10 + Integer.SIZE / Byte.SIZE;  // CapSingleFile must be false
            }
            buffer = Arrays.copyOf(buffer, buffer.length + entry.Size + (CapSingleFile ? 0 : Integer.SIZE / Byte.SIZE));
            try {
                RandomAccessFile file = new RandomAccessFile(ID, "rws");
                file.seek(HardTotalOffset[Index] + offset);
                file.write(buffer);
            } catch (IOException e) {
                throw new JposException(JposConst.JPOS_E_FAILURE, e.getMessage(), e);
            }
            System.arraycopy(buffer, 0, FileBuffer[Index], offset, buffer.length);
            entry.Handle = ++NextHandle;
            while (Files.containsKey(entry.Handle))
                entry.Handle++;
            if ((NextHandle = entry.Handle) > 0x4000)
                NextHandle -= 0X4000;
            Directory[Index].add(entry);
            Files.put(entry.Handle, entry);
            FreeData -= buffer.length - Integer.SIZE / Byte.SIZE;
            EventSource.logSet("FreeData");
            NumberOfFiles++;
            EventSource.logSet("NumberOfFiles");
            hTotalsFile[0] = entry.Handle;
        }

        @Override
        synchronized public void rename(int handle, String fileName) throws JposException {
            DirEntry e = Files.get(handle);
            check(e == null, JposConst.JPOS_E_NOEXIST, "Invalid file handle");
            if (!e.Name.equals(fileName)) {
                byte[] name = new byte[10];
                int j = -1;
                while (++j < fileName.length())
                    name[j] = (byte) fileName.charAt(j);
                while (j < name.length)
                    name[j++] = 0;
                try {
                    RandomAccessFile file = new RandomAccessFile(ID, "rws");
                    file.seek(HardTotalOffset[Index] + e.Offset - 10);
                    file.write(name);
                } catch (IOException ee) {
                    throw new JposException(JposConst.JPOS_E_FAILURE, ee.getMessage(), ee);
                }
                System.arraycopy(name, 0, FileBuffer[Index], e.Offset - 10, 10);
                e.Name = fileName;
            }
        }

        @Override
        synchronized public void delete(String fileName) throws JposException {
            DirEntry entry = null;
            int index;
            for ( index = 0; index < Directory[Index].size(); index++) {
                if (Directory[Index].get(index).Name.equals(fileName)) {
                    entry = Directory[Index].get(index);
                    break;
                }
            }
            check(entry == null, JposConst.JPOS_E_NOEXIST, "File does not exist: " + fileName);
            try {
                RandomAccessFile file = new RandomAccessFile(ID, "rws");
                int startTo;
                int startFrom;
                int endFrom;
                byte[] buffer;
                if (CapSingleFile) {
                    buffer = new byte[Integer.SIZE / Byte.SIZE];
                    ByteBuffer.wrap(buffer).putInt(0);
                    startFrom = startTo = 0;
                    endFrom = buffer.length;
                } else {
                    DirEntry last = Directory[Index].get(Directory[Index].size() - 1);
                    startTo = entry.Offset - 10 - Integer.SIZE / Byte.SIZE;
                    buffer = FileBuffer[Index];
                    startFrom = entry.Offset + entry.Size;
                    endFrom = last.Offset + last.Size + Integer.SIZE / Byte.SIZE;
                }
                file.seek(HardTotalOffset[Index] + startTo);
                file.write(buffer, startFrom, endFrom);
                System.arraycopy(buffer, startFrom,FileBuffer[Index], startTo, endFrom - startFrom);
                while (++index < Directory[Index].size())
                    Directory[Index].get(index).Offset -= entry.Size + Integer.SIZE / Byte.SIZE + 10;
                Directory[Index].remove(entry);
                Files.remove(entry);
                FreeData += entry.Size + 10 + Integer.SIZE / Byte.SIZE;
                EventSource.logSet("FreeData");
                NumberOfFiles--;
                EventSource.logSet("NumberOfFiles");
                for (int i = 0; i < OpenChanges[Index].size(); i++) {
                    if (OpenChanges[Index].get(i).getHTotalsFile() == entry.Handle) {
                        OpenChanges[Index].remove(i--);
                    }
                }
                for (int i = 0; i < Transaction.size(); i++) {
                    if (Transaction.get(i).getHTotalsFile() == entry.Handle) {
                        Transaction.remove(i--);
                    }
                }
            } catch (IOException e) {
                throw new JposException(JposConst.JPOS_E_FAILURE, e.getMessage(), e);
            }
        }

        @Override
        synchronized public void findByIndex(int index, String[] fileName) throws JposException {
            checkRange(index, 0, Directory[Index].size() -1, JposConst.JPOS_E_ILLEGAL, "Invalid index: " + index);
            fileName[0] = Directory[Index].get(index).Name;
        }

        @Override
        synchronized  public void find(String fileName, int[] hTotalsFile, int[] size) throws JposException {
            for (DirEntry e : Directory[Index]) {
                if (e.Name.equals(fileName)) {
                    hTotalsFile[0] = e.Handle;
                    size[0] = e.Size;
                    return;
                }
            }
            throw new JposException(JposConst.JPOS_E_NOEXIST, "File not found: " + fileName);
        }

        @Override
        synchronized public void read(int hTotalsFile, byte[] data, int offset, int count, List<ChangeRequest> transaction) throws JposException {
            for(DirEntry e : Directory[Index]) {
                if (e.Handle == hTotalsFile) {
                    byte[] buffer = new byte[count];
                    check(offset + count > e.Size, JposConst.JPOS_E_ILLEGAL, "Read out of file size");
                    System.arraycopy(FileBuffer[Index], e.Offset + offset, buffer, 0, count);
                    for (ChangeRequest cr : OpenChanges[Index]) {
                        if (cr.getHTotalsFile() == e.Handle) {
                            if (cr instanceof SetAll) {
                                SetAll req = (SetAll) cr;
                                Arrays.fill(buffer, req.getValue());
                            } else if (cr instanceof Write) {
                                Write req = (Write) cr;
                                if (req.getOffset() + req.getCount() >= offset && req.getOffset() < offset + count) {
                                    int to = req.getOffset() <= offset ? 0 : req.getOffset() - offset;
                                    int from = req.getOffset() < offset ? offset - req.getOffset() : 0;
                                    int len1 = req.getCount() - from;
                                    int len2 = count - to;
                                    System.arraycopy(req.getData(), from, buffer, to, len1 < len2 ? len1 : len2);
                                }
                            }
                        }
                    }
                    System.arraycopy(buffer, 0, data, 0, count);
                    return;
                }
            }
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Bad handle: " + hTotalsFile);
        }

        @Override
        synchronized public SetAll setAll(int hTotalsFile, byte value) throws JposException {
            check(!Files.containsKey(hTotalsFile), JposConst.JPOS_E_ILLEGAL, "Bad handle: " + hTotalsFile);
            return super.setAll(hTotalsFile, value);
        }

        @Override
        synchronized public void setAll(SetAll request) throws JposException {
            DirEntry e  = Files.get(request.getHTotalsFile());
            check(e == null, JposConst.JPOS_E_ILLEGAL, "Handle no longer valid: " + request.getHTotalsFile());
            byte[] buffer = new byte[e.Size];
            Arrays.fill(buffer, request.getValue());
            try {
                RandomAccessFile file = new RandomAccessFile(ID, "rws");
                file.seek(HardTotalOffset[Index] + e.Offset);
                file.write(buffer);
            } catch (IOException ee) {
                throw new JposException(JposConst.JPOS_E_FAILURE, ee.getMessage(), ee);
            }
            System.arraycopy(buffer, 0, FileBuffer[Index], e.Offset, e.Size);
        }

        @Override
        synchronized public Write write(int hTotalsFile, byte[] data, int offset, int count) throws JposException {
            DirEntry e = Files.get(hTotalsFile);
            check(e == null, JposConst.JPOS_E_ILLEGAL, "Bad handle: " + hTotalsFile);
            check(offset + count > e.Size, JposConst.JPOS_E_ILLEGAL, "Write out of range");
            return super.write(hTotalsFile, data, offset, count);
        }

        @Override
        synchronized public void write(Write request) throws JposException {
            DirEntry e = Files.get(request.getHTotalsFile());
            check(e == null, JposConst.JPOS_E_ILLEGAL, "Bad handle: " + request.getHTotalsFile());
            try {
                RandomAccessFile file = new RandomAccessFile(ID, "rws");
                file.seek(HardTotalOffset[Index] + e.Offset + request.getOffset());
                file.write(request.getData(), 0, request.getCount());
            } catch (IOException ee) {
                throw new JposException(JposConst.JPOS_E_FAILURE, ee.getMessage(), ee);
            }
            System.arraycopy(request.getData(), 0, FileBuffer[Index], e.Offset + request.getOffset(), request.getCount());
        }

        @Override
        synchronized public void rollback() throws JposException {
            for (ChangeRequest cr : Transaction) {
                OpenChanges[Index].remove(cr);
            }
            super.rollback();
        }

        @Override
        synchronized public void close() throws JposException {
            if (TransactionInProgress)
                rollback();
            super.close();
        }

        @Override
        synchronized public void commitTrans(List<ChangeRequest> transaction) throws JposException {
            byte[] buffer = Arrays.copyOf(FileBuffer[Index], FileBuffer[Index].length);
            for (ChangeRequest cr : transaction) {                              // Perform everything in RAM
                DirEntry e = Files.get(cr.getHTotalsFile());
                check(e == null, JposConst.JPOS_E_ILLEGAL, "Commit failed because handle became illegal: " + cr.getHTotalsFile());
                if (cr instanceof SetAll) {
                    byte[] data = new byte[e.Size];
                    Arrays.fill(data, ((SetAll)cr).getValue());
                    System.arraycopy(data, 0, buffer, e.Offset, e.Size);
                } else if (cr instanceof Write) {
                    Write wr = (Write) cr;
                    System.arraycopy(wr.getData(), 0, buffer, e.Offset + wr.getOffset(), wr.getCount());
                }
            }
            try {                                                               // Now do the I/O
                RandomAccessFile file = new RandomAccessFile(ID, "rws");
                file.seek(HardTotalOffset[Index]);
                file.write(buffer);
            } catch (IOException ee) {
                throw new JposException(JposConst.JPOS_E_FAILURE, ee.getMessage(), ee);
            }                                                                   // Cleanup
            FileBuffer[Index] = buffer;
            for (ChangeRequest cr : transaction) {
                OpenChanges[Index].remove(cr);
            }
            TransactionInProgress = false;
            EventSource.logSet("TransactionInProgress");
        }
    }
}
