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

import java.io.*;
import java.nio.*;
import java.util.*;

import static javax.swing.JOptionPane.*;
import static jpos.HardTotalsConst.*;
import static jpos.JposConst.*;

/**
 * Base of a JposDevice based implementation of JavaPOS HardTotals device service implementation for the sample device
 * based on the RandomAccessFile class. Error detection is not supported by this sample.
 * <p>Here a full list of all device specific properties that can be changed via jpos.xml:
 * <ul>
 *     <li>DevIndex: Positive integer between 0 and MaxTotals - 1, specifying one part of the hard totals file.
 *     Default: 0</li>
 *     <li>HardTotalsFileName: Path to the disk file that contains the HardTotals. Stored in class variable ID.</li>
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

    private Integer[] HardTotalFileSizes;               // Capacity of all HardTotals
    private Boolean[] SingleFileOnlys;                  // specifies whether HardTotals supports only one single file
    private byte[][] FileBuffers;                       // Buffer for HardTotals contents
    private long[] HardTotalOffsets;                    // Offset of HardTotals representation disk file
    private int NextHandle = 0;                         // Next file handle to try when creating a file
    private List<DirEntry>[] Directories;               // Holds directory for each HardTotals device
    private Map<Integer, DirEntry>[] Handles;           // Holds handles of all files per HardTotals device

    static private class DirEntry {
        String Name;        // File name
        int    Offset;      // Offset within file buffer
        int    Size;        // Size of file
        int    Handle;      // File handle
    }
    static private final int INTSIZE = Integer.SIZE / Byte.SIZE;    // Size of file or end-of-data (EOD) mark
    static private final int NAMESIZE = 10;                         // Maximum size of file name
    static private final int HANDLELIMIT = 0x4000;                  // Handle limit


    /**
     * The device implementation. See parent for further details.
     * @param id  Device ID, interface to HardTotals device. Name of file representing the hard totals hardware in
     *            this implementation.
     * @throws JposException if id cannot be opened or has bad format.
     */
    protected Device(String id) throws JposException {
        super(id);
        PhysicalDeviceDescription = "HardTotals device simulator";
        PhysicalDeviceName = "HardTotals Device Simulator";
        CapPowerReporting = JPOS_PR_NONE;
        initHardTotals(id);
    }

    private String getName(byte[] buffer, int offset) {
        int j = offset;
        for (int i = 0; i < Device.NAMESIZE && buffer[j] != 0; i++)
            j++;
        return new String(buffer, offset, j - offset);
    }

    private void putName(byte[] buffer, int offset, int maxlen, String name) {
        int j = offset;
        for (int i = 0; i < name.length() && i < maxlen; i++)
            buffer[j++] = (byte)name.charAt(i);
        while (j - offset < maxlen)
            buffer[j++] = 0;
    }

    synchronized private void initHardTotals(String id) throws JposException {
        try (RandomAccessFile file = new RandomAccessFile(id, "r")) {
            file.seek(0);
            MaxTotals = file.readInt();
            HardTotalFileSizes = new Integer[MaxTotals];
            SingleFileOnlys = new Boolean[MaxTotals];
            HardTotalOffsets = new long[MaxTotals];
            Directories = getArrayOf(MaxTotals);
            Handles = getArrayOf(MaxTotals);
            FileBuffers = new byte[MaxTotals][];
            for (int i = 0; i < MaxTotals; i++) {
                HardTotalFileSizes[i] = file.readInt();
                SingleFileOnlys[i] = file.readBoolean();
                Directories[i] = new ArrayList<>();
                Handles[i] = new HashMap<>();
            }
            HardTotalOffsets[0] = file.getFilePointer();
            for (int i = 1; i < MaxTotals; i++) {
                HardTotalOffsets[i] = HardTotalOffsets[i - 1] + HardTotalFileSizes[i - 1];
            }
            for (int i = 0; i < MaxTotals; i++) {
                byte[] buffer = FileBuffers[i] = new byte[HardTotalFileSizes[i]];
                file.readFully(buffer);
                int offset = 0;
                while(offset < HardTotalFileSizes[i]) {
                    DirEntry entry = new DirEntry();
                    if ((entry.Size = ByteBuffer.wrap(buffer, offset, INTSIZE).getInt()) == 0)
                        break;
                    if (SingleFileOnlys[i]) {
                        entry.Offset = offset + INTSIZE;
                        entry.Handle = ++NextHandle;
                        entry.Name = "";
                        Directories[i].add(entry);
                        Handles[i].put(entry.Handle, entry);
                        break;
                    } else {
                        entry.Offset = offset + INTSIZE + NAMESIZE;
                        entry.Handle = ++NextHandle;
                        entry.Name = getName(buffer, offset + INTSIZE);
                        Directories[i].add(entry);
                        Handles[i].put(entry.Handle, entry);
                    }
                    offset = entry.Offset + entry.Size;
                }
            }
            check(file.length() != HardTotalOffsets[MaxTotals - 1] + HardTotalFileSizes[MaxTotals - 1], JPOS_E_FAILURE, "Inconsistant file format: " + id);
            hardTotalsInit(MaxTotals);
        } catch (FileNotFoundException e) {
            MaxTotals = null;
            HardTotalFileSizes = null;
        } catch (IOException e) {
            throw new JposException(JPOS_E_FAILURE, e.getMessage(), e);
        }
    }

    synchronized private void createHardTotals(String id) throws JposException {
        if (HardTotalOffsets == null) {
            try (RandomAccessFile ignored = new RandomAccessFile(id, "r")) {
                throw new JposException(JPOS_E_FAILURE, "Unexpected HardTotals file present: " + id);
            } catch (IOException e) {
                try (RandomAccessFile file = new RandomAccessFile(id, "rws")){
                   ;
                    file.seek(0);
                    file.writeInt(MaxTotals);
                    for (int i = 0; i < MaxTotals; i++) {
                        file.writeInt(HardTotalFileSizes[i] != null ? HardTotalFileSizes[i] : (HardTotalFileSizes[i] = 0x8000));
                        file.writeBoolean(SingleFileOnlys[i] != null ? SingleFileOnlys[i] : (SingleFileOnlys[i] = false));
                    }
                    Directories = getArrayOf(MaxTotals);
                    Handles = getArrayOf(MaxTotals);
                    HardTotalOffsets = new long[MaxTotals];
                    FileBuffers = new byte[MaxTotals][];
                    for (int i = 0; i < MaxTotals; i++) {
                        Directories[i] = new ArrayList<>();
                        Handles[i] = new HashMap<>();
                        HardTotalOffsets[i] = file.getFilePointer();
                        ByteBuffer.wrap(FileBuffers[i] = new byte[HardTotalFileSizes[i]], 0, INTSIZE).putInt(0);
                        file.write(FileBuffers[i]);
                    }
                } catch (IOException ee) {
                    throw new JposException(JPOS_E_FAILURE, ee.getMessage(), ee);
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
            if (MaxTotals == null) {
                if ((o = entry.getPropertyValue("MaxTotals")) != null && Integer.parseInt(o.toString()) > 0)
                    MaxTotals = Integer.parseInt(o.toString());
                else
                    MaxTotals = index + 1;
                hardTotalsInit(MaxTotals);
            }
            check(index >= MaxTotals, JPOS_E_FAILURE, "DevIndex out of range: " + index + " >= " + MaxTotals);
            if (HardTotalFileSizes == null) {
                HardTotalFileSizes = new Integer[MaxTotals];
                SingleFileOnlys = new Boolean[MaxTotals];
            }
            if (SingleFileOnlys[index] == null) {
                if ((o = entry.getPropertyValue("SingleFileOnly")) != null)
                    SingleFileOnlys[index] = Boolean.parseBoolean(o.toString());
                else
                    SingleFileOnlys[index] = false;
            }
            if (HardTotalFileSizes[index] == null) {
                int minimum = SingleFileOnlys[index] ? INTSIZE : INTSIZE + NAMESIZE + INTSIZE;
                if ((o = entry.getPropertyValue("MaxHardTotalFileSize")) != null && Integer.parseInt(o.toString()) > minimum)
                    HardTotalFileSizes[index] = Integer.parseInt(o.toString());
                else
                    HardTotalFileSizes[index] = 0x8000;
            }
        } catch (JposException e) {
            throw e;
        } catch (Exception e) {
            throw new JposException(JPOS_E_ILLEGAL, "Invalid JPOS property", e);
        }
    }

    @Override
    public void changeDefaults(HardTotalsProperties props) {
        super.changeDefaults(props);
        props.DeviceServiceVersion += 1;
        props.DeviceServiceDescription = "Hard totals service for sample device";
        props.CapSingleFile = SingleFileOnlys[props.Index];
    }

    @Override
    public HardTotalsProperties getHardTotalsProperties(int index) {
        return new SampleHardTotalsProperties(index);
    }

    private class SampleHardTotalsProperties extends HardTotalsProperties {
        protected SampleHardTotalsProperties(int dev) {
            super(dev);
        }

        private int HardTotalFileSize;            // Capacity of HardTotals
        private byte[] FileBuffer;                // Buffer for HardTotals contents
        private long HardTotalOffset;             // Offset of HardTotals representation in disk file
        private List<DirEntry> Directory;         // Holds directory
        private Map<Integer, DirEntry> Files;     // Holds file handles
        private int RealFreeData;                 // Really free data, can become negative

        @Override
        public void checkHealth(int level) throws JposException {
            String result = " checkHealth: OK";
            String typestr = "Internal";
            if (level == JPOS_CH_EXTERNAL)
                typestr = "External";
            else if (level == JPOS_CH_INTERACTIVE) {
                typestr = "Interactive";
                synchronizedMessageBox("CheckHealth result:\n" + typestr + result, "CheckHealth", INFORMATION_MESSAGE);
            }
            CheckHealthText = typestr + result;
        }

        @Override
        public void deviceEnabled(boolean enable) throws JposException {
            if (enable && !FirstEnableHappened) {
                createHardTotals(ID);
                Directory = Directories[Index];
                Files = Handles[Index];
                FileBuffer = FileBuffers[Index];
                HardTotalOffset = HardTotalOffsets[Index];
                HardTotalFileSize = HardTotalFileSizes[Index];
            }
            super.deviceEnabled(enable);
        }

        @Override
        public void initOnEnable(boolean enable) {
            super.initOnEnable(enable);
            NumberOfFiles = Directory.size();
            EventSource.logSet("NumberOfFiles");
            if (CapSingleFile) {    // Capacity reduced by file size
                TotalsSize = RealFreeData = HardTotalFileSize - INTSIZE; // Capacity reduced by size (4 byte int)
                for (DirEntry entry : Directory) {
                    RealFreeData -= entry.Size;
                }
            } else {    // Capacity reduced by directory size (4 byte length, 10 byte name) and EOD mark (4 byte 0)
                TotalsSize = RealFreeData = HardTotalFileSize - INTSIZE - NAMESIZE - INTSIZE;
                for (DirEntry entry : Directory) {
                    RealFreeData -= INTSIZE + NAMESIZE + entry.Size;    // Reduce by file size + directory size (4 byte length, 10 byte length)
                }
            }
            FreeData = Math.max(RealFreeData, 0);
            EventSource.logSet("TotalsSize");
            EventSource.logSet("FreeData");
        }

        @Override
        public void create(String fileName, int[] hTotalsFile, int size, boolean errorDetection) throws JposException {
            synchronized (Device.this) {
                List<DirEntry> entries = Directory;
                for (DirEntry e : entries)
                    check(e.Name.equals(fileName), JPOS_E_EXISTS, "File exists: " + fileName);
                checkext(size > RealFreeData, JPOS_ETOT_NOROOM, "File too large for " + fileName + ": " + size);
                DirEntry entry = new DirEntry();
                entry.Size = size;
                entry.Name = fileName;
                byte[] buffer = new byte[(CapSingleFile ? 0 : NAMESIZE) + INTSIZE];
                ByteBuffer.wrap(buffer).putInt(entry.Size);
                putName(buffer, INTSIZE, buffer.length - INTSIZE, fileName);
                entry.Offset = buffer.length;
                int offset;
                if (entries.size() == 0) {
                    offset = 0;
                } else {
                    DirEntry last = entries.get(entries.size() - 1);
                    entry.Offset += (offset = last.Offset + last.Size);  // CapSingleFile IS false
                }
                buffer = Arrays.copyOf(buffer, buffer.length + entry.Size + (CapSingleFile ? 0 : INTSIZE));
                try (RandomAccessFile file = new RandomAccessFile(ID, "rws")) {
                    file.seek(HardTotalOffset + offset);
                    file.write(buffer);
                } catch (IOException e) {
                    throw new JposException(JPOS_E_FAILURE, e.getMessage(), e);
                }
                System.arraycopy(buffer, 0, FileBuffer, offset, buffer.length);
                entry.Handle = ++NextHandle;
                while (Files.containsKey(entry.Handle))
                    entry.Handle++;
                if ((NextHandle = entry.Handle) > HANDLELIMIT)
                    NextHandle -= HANDLELIMIT;
                Directory.add(entry);
                Files.put(entry.Handle, entry);
                RealFreeData -= buffer.length - (INTSIZE);
                FreeData = Math.max(RealFreeData, 0);
                EventSource.logSet("FreeData");
                NumberOfFiles++;
                EventSource.logSet("NumberOfFiles");
                hTotalsFile[0] = entry.Handle;
            }
        }

        @Override
        public void rename(int handle, String fileName) throws JposException {
            synchronized (Device.this) {
                DirEntry e = null;
                for (DirEntry ce : Directory) {
                    check(ce.Name.equals(fileName) && ce.Handle != handle, JPOS_E_EXISTS, "Duplicate file name: " + fileName);
                    if (ce.Handle == handle)
                        e = ce;
                }
                check(e == null, JPOS_E_NOEXIST, "Invalid file handle");
                if (!e.Name.equals(fileName)) {
                    byte[] name = new byte[NAMESIZE];
                    putName(name, 0, NAMESIZE, fileName);
                    try (RandomAccessFile file = new RandomAccessFile(ID, "rws")) {
                        file.seek(HardTotalOffset + e.Offset - NAMESIZE);
                        file.write(name);
                    } catch (IOException ee) {
                        throw new JposException(JPOS_E_FAILURE, ee.getMessage(), ee);
                    }
                    System.arraycopy(name, 0, FileBuffer, e.Offset - NAMESIZE, NAMESIZE);
                    e.Name = fileName;
                }
            }
        }

        @Override
        public void delete(String fileName) throws JposException {
            synchronized (Device.this) {
                DirEntry entry = null;
                int index;
                for (index = 0; index < Directory.size(); index++) {
                    if (Directory.get(index).Name.equals(fileName)) {
                        entry = Directory.get(index);
                        break;
                    }
                }
                check(entry == null, JPOS_E_NOEXIST, "File does not exist: " + fileName);
                try (RandomAccessFile file = new RandomAccessFile(ID, "rws")) {
                    int startTo;
                    int startFrom;
                    int endFrom;
                    byte[] buffer;
                    if (CapSingleFile) {
                        buffer = new byte[INTSIZE];
                        ByteBuffer.wrap(buffer).putInt(0);
                        startFrom = startTo = 0;
                        endFrom = buffer.length;
                    } else {
                        DirEntry last = Directory.get(Directory.size() - 1);
                        startTo = entry.Offset - NAMESIZE - INTSIZE;
                        buffer = FileBuffer;
                        startFrom = entry.Offset + entry.Size;
                        endFrom = last.Offset + last.Size + INTSIZE;
                    }
                    file.seek(HardTotalOffset + startTo);
                    file.write(buffer, startFrom, endFrom - startFrom);
                    System.arraycopy(buffer, startFrom, FileBuffer, startTo, endFrom - startFrom);
                    while (++index < Directory.size())
                        Directory.get(index).Offset -= entry.Size + INTSIZE + NAMESIZE;
                    Directory.remove(entry);
                    Files.remove(entry.Handle);
                    FreeData = RealFreeData += entry.Size + (CapSingleFile ? 0 : NAMESIZE + INTSIZE);
                    EventSource.logSet("FreeData");
                    NumberOfFiles--;
                    EventSource.logSet("NumberOfFiles");
                } catch (IOException e) {
                    throw new JposException(JPOS_E_FAILURE, e.getMessage(), e);
                }
            }
        }

        @Override
        public void findByIndex(int index, String[] fileName) throws JposException {
            synchronized (Device.this) {
                checkRange(index, 0, Directory.size() - 1, JPOS_E_ILLEGAL, "Invalid index: " + index);
                fileName[0] = Directory.get(index).Name;
            }
        }

        @Override
        public void find(String fileName, int[] hTotalsFile, int[] size) throws JposException {
            synchronized (Device.this) {
                for (DirEntry e : Directory) {
                    if (e.Name.equals(fileName)) {
                        hTotalsFile[0] = e.Handle;
                        size[0] = e.Size;
                        return;
                    }
                }
                throw new JposException(JPOS_E_NOEXIST, "File not found: " + fileName);
            }
        }

        @Override
        public void read(int hTotalsFile, byte[] data, int offset, int count, List<ChangeRequest> transaction) throws JposException {
            synchronized (Device.this) {
                for (DirEntry e : Directory) {
                    if (e.Handle == hTotalsFile) {
                        byte[] buffer = new byte[count];
                        check(offset + count > e.Size, JPOS_E_ILLEGAL, "Read out of file size");
                        System.arraycopy(FileBuffer, e.Offset + offset, buffer, 0, count);
                        for (ChangeRequest cr : transaction) {
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
                                        System.arraycopy(req.getData(), from, buffer, to, Math.min(len1, len2));
                                    }
                                }
                            }
                        }
                        System.arraycopy(buffer, 0, data, 0, count);
                        return;
                    }
                }
                throw new JposException(JPOS_E_ILLEGAL, "Bad handle: " + hTotalsFile);
            }
        }

        @Override
        public SetAll setAll(int hTotalsFile, byte value) throws JposException {
            synchronized (Device.this) {
                check(!Files.containsKey(hTotalsFile), JPOS_E_ILLEGAL, "Bad handle: " + hTotalsFile);
                return super.setAll(hTotalsFile, value);
            }
        }

        @Override
        public void setAll(SetAll request) throws JposException {
            synchronized (Device.this) {
                DirEntry e = Files.get(request.getHTotalsFile());
                check(e == null, JPOS_E_ILLEGAL, "Handle no longer valid: " + request.getHTotalsFile());
                byte[] buffer = new byte[e.Size];
                Arrays.fill(buffer, request.getValue());
                try (RandomAccessFile file = new RandomAccessFile(ID, "rws");) {
                    file.seek(HardTotalOffset + e.Offset);
                    file.write(buffer);
                } catch (IOException ee) {
                    throw new JposException(JPOS_E_FAILURE, ee.getMessage(), ee);
                }
                System.arraycopy(buffer, 0, FileBuffer, e.Offset, e.Size);
            }
        }

        @Override
        public Write write(int hTotalsFile, byte[] data, int offset, int count) throws JposException {
            synchronized (Device.this) {
                DirEntry e = Files.get(hTotalsFile);
                check(e == null, JPOS_E_ILLEGAL, "Bad handle: " + hTotalsFile);
                check(offset + count > e.Size, JPOS_E_ILLEGAL, "Write out of range");
                return super.write(hTotalsFile, data, offset, count);
            }
        }

        @Override
        public void write(Write request) throws JposException {
            synchronized (Device.this) {
                DirEntry e = Files.get(request.getHTotalsFile());
                check(e == null, JPOS_E_ILLEGAL, "Bad handle: " + request.getHTotalsFile());
                try (RandomAccessFile file = new RandomAccessFile(ID, "rws")) {
                    file.seek(HardTotalOffset + e.Offset + request.getOffset());
                    file.write(request.getData(), 0, request.getCount());
                } catch (IOException ee) {
                    throw new JposException(JPOS_E_FAILURE, ee.getMessage(), ee);
                }
                System.arraycopy(request.getData(), 0, FileBuffer, e.Offset + request.getOffset(), request.getCount());
            }
        }

        @Override
        public void commitTrans() throws JposException {
            synchronized (Device.this) {
                byte[] savedBuffer = Arrays.copyOf(FileBuffer, FileBuffer.length);
                for (ChangeRequest cr : Transaction) {                              // Perform everything in RAM
                    DirEntry e = Files.get(cr.getHTotalsFile());
                    if(e != null) {         // File has not been deleted in the meantime
                        if (cr instanceof SetAll) {
                            byte[] data = new byte[e.Size];
                            Arrays.fill(data, ((SetAll) cr).getValue());
                            System.arraycopy(data, 0, FileBuffer, e.Offset, e.Size);
                        } else if (cr instanceof Write) {
                            Write wr = (Write) cr;
                            System.arraycopy(wr.getData(), 0, FileBuffer, e.Offset + wr.getOffset(), wr.getCount());
                        }
                    }
                }
                try (RandomAccessFile file = new RandomAccessFile(ID, "rws")) {   // Now do the I/O
                    file.seek(HardTotalOffset);
                    file.write(FileBuffer);
                } catch (IOException ee) {
                    System.arraycopy(savedBuffer, 0, FileBuffer, 0, FileBuffer.length);
                    throw new JposException(JPOS_E_FAILURE, ee.getMessage(), ee);
                }                                                                   // Cleanup
                super.commitTrans();
            }
        }
    }
}
