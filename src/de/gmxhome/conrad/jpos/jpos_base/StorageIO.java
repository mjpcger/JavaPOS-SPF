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

package de.gmxhome.conrad.jpos.jpos_base;

import jpos.*;

import java.io.*;
import java.lang.reflect.*;
import java.nio.channels.*;
import java.util.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposBaseDevice.*;
import static jpos.JposConst.*;

/**
 * This class provides methods to write data to a data target or read data from a data source. Both, data source and data
 * target can be either a file in the file system or a file on a HardTotals device or both. While a data source is always either
 * a file system file or a file on a HardTotals device, data target can be a file system file and a file on a HardTotals
 * device (in that case, identical data will be written to the file system and the HardTotals device).
 */
@SuppressWarnings("unused")
public class StorageIO {
    /**
     * Constructor for storage operations.
     * @param storage    HardTotals device and / or file system path. In case of a HardTotals device object, it is the
     *                   object to be used as storage. It must be opened and enabled.<br>
     *                   If it is a String object, it contains the path name of the file within the file system.<br>
     *                   For StorageIO objects used as data target, an Object[2], where the first object is an instance
     *                   of a HardTotals device to be used as storage and the second object is a String containing the
     *                   path name of the storage within the file system to be used is also valid.
     * @param errorCodeDeviceFull Optional integer, specifying the extended error code to be used in case of device full
     *                   conditions. If not specified, method setStorageData signals device full condition via return
     *                   code false. Otherwise, setStoragedata will throw a JposException with error code E_EXTENDED and
     *                   the given error code in case of device full condition and returns true otherwise.
     * @throws JposException If storage is invalid.
     */
    public StorageIO(Object storage, int... errorCodeDeviceFull) throws JposException {
        check(storage == null, JPOS_E_ILLEGAL, "No source device specified");
        if (storage instanceof HardTotals)
            Totals = (HardTotals) storage;
        else if (storage instanceof String) {
            File f = new File(Path = storage.toString());
            check(!f.exists() || !f.isDirectory(), JPOS_E_ILLEGAL, "Invalid folder: " + Path);
        } else if (storage instanceof Object[] && Array.getLength(storage) == 2 &&
                Array.get(storage, 0) instanceof HardTotals && Array.get(storage, 1) instanceof String) {
            Totals = (HardTotals) Array.get(storage, 0);
            File f = new File(Path = Array.get(storage, 1).toString());
            check(!f.exists() || !f.isDirectory(), JPOS_E_ILLEGAL, "Invalid folder: " + Path);
        } else
            throw new JposException(JPOS_E_ILLEGAL, "Invalid data type for source: " + storage.getClass().getSimpleName());
        check(errorCodeDeviceFull.length > 1, JPOS_E_ILLEGAL, "Too many error codes");
        ExtendedErrorCode = errorCodeDeviceFull.length == 0 ? null : errorCodeDeviceFull[0];
    }

    private HardTotals Totals = null;
    private String Path = null;
    /**
     * TheFile can have of the following types:<ul>
     *     <li>FileInputStream: In case of file system data source.</li>
     *     <li>int[]: In case of HardTotals source.</li>
     *     <li>Object[]: In case of data target:<ul>
     *         <li>Object[0]: FileOutputStream or null for file system target. If null, only HardTotals target.</li>
     *         <li>Object[1]: int[] containing size and position. In case of HartTotals target, it contains a handle
     *         value, too.</li>
     *     </ul>
     *     </li>
     * </ul>
     */
    private Object TheFile = null;
    private final static int STREAM_OBJECT = 0;      // Index of Stream object in Object[] if TheFile is Object[]
    private final static int HARD_TOTAL_OBJECT = 1;   // Index of HardTotals int[] in Object[] if TheFile is Object[]
    private final static int FILE_SIZE = 0;          // Index of file size in int[] if TheFile is int[] or within Object[HardTotalObject]
    private final static int FILE_OFFSET = 1;        // Index of file offset in int[]
    private final static int FILE_HANDLE = 2;        // Index of file handle in int[] in case of HardTotals

    private final Integer ExtendedErrorCode;        // Initialized in constructor, for storage full error code.

    /**
     * Retrieves all data of a given file from storage device.
     * @param fileName   Source file name.<br>
     *                   For HardTotals devices, fileName will be ignored if CapSingleFile is true
     *                   (in this case, the empty string will be used as file name). If CapSingleFile is false, the
     *                   directory part of the file name will be removed and a maximum of 10 characters of the remainder
     *                   will be used as file name. It must consist of ASCII characters only.<br>
     *                   For the file system, fileName must match the rules for file names specific to the corresponding
     *                   file system and operating system.
     * @param hardTotals Optional parameter, can be set to true to force read from HardTotals device if the specified
     *                   file is present on both data sources.
     * @return The contents of the storage file specified by the parameters.
     * @throws JposException If an error occurs.
     */
    public byte[] getStorageData(String fileName, boolean... hardTotals) throws JposException {
        check(hardTotals.length > 1, JPOS_E_ILLEGAL, "Too many conditions");
        return hardTotals.length == 1 ? getStorageData(fileName, Integer.MAX_VALUE, hardTotals[0])
                : getStorageData(fileName, Integer.MAX_VALUE);
    }

    /**
     * Retrieves data from storage device. Source file remains open until end of file will be reached during data
     * retrieval. To close the source file explicitly, length can be set to -1.
     * @param fileName   Source file name.<br>
     *                   For HardTotals devices, fileName will be ignored if CapSingleFile is true
     *                   (in this case, the empty string will be used as file name). If CapSingleFile is false, the
     *                   directory part of the file name will be removed and a maximum of 10 characters of the remainder
     *                   will be used as file name. It must consist of ASCII characters only.<br>
     *                   For the file system, fileName must match the rules for file names specific to the corresponding
     *                   file system and operating system.<br>
     *                   If null, data will be retrieved from the file specified in a previous getStorageData call. It
     *                   will always be retrieved starting at the position of the first previously unread byte.
     * @param length     Maximum number of bytes to be read.
     * @param hardTotals Optional parameter, can be set to true to force read from HardTotals device if the specified
     *                   file is present on both data sources.
     * @return The contents of the storage file part specified by the parameters.
     * @throws JposException If an error occurs.
     */
    public byte[] getStorageData(String fileName, int length, boolean... hardTotals) throws JposException {
        if ((fileName = checkFileName(fileName)) == null)
            return getDataPart(length);
        check(hardTotals.length > 1, JPOS_E_ILLEGAL, "Too many conditions");
        byte[] data = {};
        if (Totals != null && (Path == null || (hardTotals.length != 0 && hardTotals[0]))) {
            data = getDataFromHardTotals(fileName, length);
        } else if (Path != null) {
            data = getDataFromFileSystem(fileName, length);
        }
        return data;
    }

    /**
     * Retrieve size of the currently open file, if any. Can be used in combination with getStorageData to retrieve the
     * total amount of data in the data store after getting a part of it.
     * @return The size of the storage file, if just open, 0 otherwise.
     */
    public long getOpenFileSize() {
        long ret = 0;
        if (TheFile instanceof FileInputStream) {
            FileInputStream in = (FileInputStream) TheFile;
            try {
                ret = in.getChannel().size();
            } catch (IOException e) {
                e.printStackTrace();
                TheFile = null;
                try {
                    in.close();
                } catch (IOException ee) {
                    ee.printStackTrace();
                }
            }
        } else if (TheFile instanceof int[]) {
            ret = ((int[])TheFile)[FILE_SIZE];
        } else if (TheFile instanceof Object[]) {
            ret = ((int[])((Object[])TheFile)[HARD_TOTAL_OBJECT])[FILE_SIZE];
        }
        return ret;
    }

    /**
     * Gets free space on the storage.
     * @param fileName Optional parameter: If specified, the name of a file. If present, the size of this file will be
     *                 added to the free space of the storage.
     * @return Free capacity of the storage, in byte. If greater than MAX_VALUE, MAX_VALUE will be returned.
     * @throws JposException If an error occurs.
     */
    public int getAvailableSpace(String... fileName) throws JposException {
        check(fileName.length > 1, JPOS_E_ILLEGAL, "Too many file names");
        int htspace = Integer.MAX_VALUE;
        long fsspace = Integer.MAX_VALUE;
        if (Totals != null) {
            htspace = Totals.getFreeData();
            if (fileName.length == 1 && fileName[0] != null) {
                try {
                    int[] handle = {0}, size = {0};
                    Totals.find(getHardTotalsFileName(fileName[0]), handle, size);
                    htspace += size[0];
                } catch (JposException ignored) {
                }
            }
        }
        if (Path != null) {
            fsspace = new File(Path).getFreeSpace();
            if (fileName.length == 1 && fileName[0] != null) {
                File f = new File(Path, fileName[0]);
                fsspace += f.length();
            }
        }
        return htspace < fsspace ? htspace : (int) fsspace;
    }

    /**
     * Writes data to storage device. If specified file is just present, it will be overwritten.
     * @param fileName   Source file name.<br>
     *                   For HardTotals devices, fileName will be ignored if CapSingleFile is true
     *                   (in this case, the empty string will be used as file name). If CapSingleFile is false, the
     *                   directory part of the file name will be removed and a maximum of 10 characters of the remainder
     *                   will be used as file name. It must consist of ASCII characters only.<br>
     *                   For the file system, fileName must match the rules for file names specific to the corresponding
     *                   file system and operating system.
     * @param data       Data to be written to the storage.
     * @return true if successful, false if not enough free space is available. In the latter case, the caller should
     *         throw a JposException with ErrorCode E_EXTENDED and the device class specific value E..._NOROOM.
     * @throws JposException If an error occurs.
     */
    public boolean setStorageData(String fileName, byte[] data) throws JposException {
        return setStorageData(fileName, data, data.length) == 0;
    }

    /**
     * Writes data to storage device. If specified file is just present, it will be overwritten.
     * @param fileName   Source file name.<br>
     *                   For HardTotals devices, fileName will be ignored if CapSingleFile is true
     *                   (in this case, the empty string will be used as file name). If CapSingleFile is false, the
     *                   directory part of the file name will be removed and a maximum of 10 characters of the remainder
     *                   will be used as file name. It must consist of ASCII characters only.<br>
     *                   For the file system, fileName must match the rules for file names specific to the corresponding
     *                   file system and operating system.<br>
     *                   To append data to a previously created file with a size greater than the data.length, specify
     *                   null as fileName. In this case, data consists of the next data.length bytes to be written
     *                   sequentially to the target until the previously specified file size will be reached.
     * @param data       Data to be written to the storage. If fileName equals null, the previously created file can be
     *                   closed by specifying a byte array of size 0.
     * @param size       Size of the target file to be created. Contents will be ignored if fileName
     *                   is null.
     * @return Number of bytes between current write position and end of file, -1 if not enough space is available on
     *         target
     * @throws JposException If an error occurs.
     */
    public int setStorageData(String fileName, byte[] data, int size) throws JposException {
        if (data == null)
            data = new byte[0];
        if ((fileName = checkFileName(fileName)) == null)
            return setPartData(data);
        check(data.length > size, JPOS_E_ILLEGAL, "End of target reached");
        if (Totals != null) {
            if (setDataForHardTotals(fileName, data, size)) return -1;
        }
        if (Path != null) {
            if (setDataForFileSystem(fileName, data, size)) return -1;
        }
        return size - data.length;
    }

    /**
     * Checks whether a storage file is present.
     * @param fileName   File name. If <i>device</i> is null or true, it must consist of up to <i>N</i> ASCII characters,
     *                   optionally followed by a dot and further characters. <i>N</i> is 0 if property <i>CapSingleFile</i>
     *                   of the HardTotals device is true, otherwise 10. If present, all characters behind the dot and
     *                   the dot will be ignored in case of HardTotals files.
     * @return true if a storage file is just present, false if not. If the storage is on file system and on a HardTotals
     *         device and the file is only present on one of these storages, null will be returned.
     * @throws JposException If an error occurred.
     */
    public Boolean checkFileExists(String fileName) throws JposException {
        Boolean htpresent = null, fspresent = null;
        if (fileName == null)
            fileName = "";
        if (Totals != null) {
            String htname = getHardTotalsFileName(fileName);
            int[] handle = {0}, size = {0};
            try {
                Totals.find(htname, handle, size);
                htpresent = true;
            } catch (JposException e) {
                if (e.getErrorCode() != JPOS_E_NOEXIST)
                    throw e;
                htpresent = false;
            }
        }
        if (Path != null) {
            File f = new File(Path, fileName);
            fspresent = f.exists() && f.isFile();
        }
        if (Totals != null && Path != null)
            return fspresent != htpresent ? null : fspresent;
        return htpresent != null ? htpresent : fspresent;
    }

    /**
     * Returns the underlaying storage specifier: In case of a HardTotals device, this is an instance of the corresponding
     * HardTotals device. In case if a file system storage, it is the path to the storage folder as String.
     * @param hardtotal Optional argument, only relevant if the storage is in both, file system and HardTotals device.
     *                  Specifies which storage specifier shall be returned. If true, the HardTotals device instance,
     *                  otherwise the folder path. Default is true
     * @return  File system path or HardTotals instance, depending on storage type and hardtotals parameter.
     * @throws JposException If hardtotals has been specified more than once.
     */
    public Object getStorageObject(boolean... hardtotal) throws JposException {
        check(hardtotal.length > 1, JPOS_E_ILLEGAL, "Wrong number of arguments: hardtotals");
        if (Totals != null && (Path == null || hardtotal.length == 0 || hardtotal[0]))
            return Totals;
        return Path;
    }

    /**
     * Retrieves the names of all files within the storage. If storage is located on HardTotals and file system, only
     * files present in HardTotals device and on file system are listed.
     * @return Names of files that can be opened on the specified storage.
     * @throws JposException In case of an error.
     */
    public String[] getFileNames() throws JposException {
        String[] results = null;
        if (Path != null) {
            results = getFilesFromFileSystem();
        }
        if (Totals != null) {
            if (results == null) {
                results = getFilesFromHardTotals();
            } else {
                results = extractHardTotalsFilesFromFileSystemFileList(results);
            }
        }
        return results;
    }

    private byte[] getDataFromFileSystem(String fileName, int length) throws JposException {
        byte[] data;
        File f = new File(Path, fileName);
        check(!f.exists() || !f.isFile(), JPOS_E_ILLEGAL, "Invalid file: " + fileName);
        long size = f.length();
        data = new byte[(int)(length < size ? length : size)];
        try {
            FileInputStream in = new FileInputStream(f);
            int pos = 0;
            try {
                int len;
                for (pos = 0; pos < data.length; pos += len) {
                    if ((len = in.read(data, pos, data.length - pos)) < 0) {
                        break;
                    }
                }
            } catch (IOException e) {
                in.close();
                throw e;
            }
            if (pos == length)
                TheFile = in;
            else
                in.close();
            check(pos < data.length, JPOS_E_FAILURE, "File read error before");
        } catch (IOException e) {
            throw new JposException(JPOS_E_FAILURE, e.getMessage(), e);
        }
        return data;
    }

    private byte[] getDataFromHardTotals(String fileName, int length) throws JposException {
        String htname = getHardTotalsFileName(fileName);
        int[] handle = {0}, size = {0};
        Totals.find(htname, handle, size);
        byte[] data = new byte[Math.min(size[0], length)];
        Totals.read(handle[0], data, 0, data.length);
        if (data.length == length)
            TheFile = new int[]{size[0], length, handle[0]};
        return data;
    }

    private byte[] getDataPart(int length) throws JposException {
        byte[] data = {};
        if (length == -1) {
            checkFileName("");
        } else if (TheFile instanceof FileInputStream) {
            data = getPartFromFileSystem(length);
        } else if (TheFile instanceof int[]) {
            data = getPartFromHardTotals(length);
        } else
            throw new JposException (JPOS_E_FAILURE, "Cannot retrieve data from target");
        return data;
    }

    private byte[] getPartFromHardTotals(int length) throws JposException {
        int handle = ((int[])TheFile)[FILE_HANDLE];
        int size = ((int[])TheFile)[FILE_SIZE];
        int offset = ((int[])TheFile)[FILE_OFFSET];
        byte[] data = {};
        try {
            byte[] buffer = new byte[size > length + offset ? length : size - offset];
            Totals.read(handle, buffer, offset, buffer.length);
            data = buffer;
            ((int[])TheFile)[FILE_OFFSET] += data.length;
        } finally {
            if (data.length < length)
                TheFile = null;
        }
        return data;
    }

    private byte[] getPartFromFileSystem(int length) throws JposException {
        FileInputStream in = (FileInputStream) TheFile;
        byte[] data = {};
        IOException ex = null;
        int pos = 0;
        try {
            FileChannel fc = in.getChannel();
            long size = fc.size() - fc.position();
            data = new byte[(int) (size > length ? length : size)];
            int len;
            for (pos = 0; pos < data.length; pos += len) {
                if ((len = in.read(data, pos, data.length - pos)) < 0)
                    break;
            }
        } catch (IOException e) {
            ex = e;
        }
        if (pos < length || ex != null) {
            TheFile = null;
            try {
                in.close();
            } catch (IOException e) {
                if (ex == null)
                    ex = e;
                else
                    e.printStackTrace();
            }
            if (ex != null)
                throw  new JposException(JPOS_E_FAILURE, ex.getMessage(), ex);
        }
        return data;
    }

    private String checkFileName(String name) throws JposException {
        if (name == null) {
            if (TheFile == null)
                name = "";
        } else if (TheFile != null) {
            try {
                if (TheFile instanceof FileInputStream)
                    ((FileInputStream) TheFile).close();
                else if (TheFile instanceof Object[] && ((Object[])TheFile)[STREAM_OBJECT] instanceof FileOutputStream)
                    ((FileOutputStream)(((Object[])TheFile)[STREAM_OBJECT])).close();
            } catch (IOException e) {
                throw new JposException(JPOS_E_FAILURE, e.getMessage(), e);
            } finally {
                TheFile = null;
            }
        }
        return name;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private boolean setDataForFileSystem(String fileName, byte[] data, int size) throws JposException {
        File f = new File(Path, fileName);
        if (f.exists() && f.isFile() && f.length() > data.length)
            f.delete();
        long free = new File(Path).getFreeSpace();
        if (free < data.length) {
            if (ExtendedErrorCode != null)
                throw new JposException(JPOS_E_EXTENDED, ExtendedErrorCode, "Not enough space: " +
                        free + " < " + data.length);
            return true;
        }
        FileOutputStream ostr = null;
        try {
            ostr = new FileOutputStream(f, false);
            ostr.write(data);
            if (data.length < size) {
                if (TheFile == null)
                    TheFile = new Object[]{ostr, new int[]{size, data.length}};
                else
                    ((Object[]) TheFile)[STREAM_OBJECT] = ostr;
            } else
                ostr.close();
        } catch (IOException e) {
            if (ostr != null)
                try {
                    ostr.close();
                } catch (IOException ee) {
                    ee.printStackTrace();
                }
            throw new JposException(JPOS_E_FAILURE, e.getMessage(), e);
        }
        return false;
    }

    private boolean setDataForHardTotals(String fileName, byte[] data, int size) throws JposException {
        int[] handle = {0}, oldsize = {0};
        String htname = getHardTotalsFileName(fileName);
        try {
            Totals.find(htname, handle, oldsize);
            if (oldsize[0] != size && oldsize[0] + Totals.getFreeData() >= size)
                Totals.delete(htname);
        } catch (JposException e) {
            e.printStackTrace();
        }
        if (oldsize[0] != size) {
            int free = Totals.getFreeData();
            if (free < size) {
                if (ExtendedErrorCode != null)
                    throw new JposException(JPOS_E_EXTENDED, ExtendedErrorCode, "Not enough space: " +
                            free + " < " + size);
                return true;
            }
            Totals.create(htname, handle, size, false);
        }
        Totals.write(handle[0], data, 0, data.length);
        if (data.length < size) {
            if (TheFile == null)
                TheFile = new Object[]{null, new int[]{size, data.length, handle[0]}};
            else
                ((Object[]) TheFile)[HARD_TOTAL_OBJECT] = new int[]{size, data.length, handle[0]};
        }
        return false;
    }

    private int setPartData(byte[] data)  throws JposException{
        if (data.length == 0) {
            checkFileName("");
            return 0;
        }
        check(!(TheFile instanceof Object[] || TheFile instanceof int[]), JPOS_E_ILLEGAL, "Cannot store data in source");
        FileOutputStream ostr = (FileOutputStream) Array.get(TheFile, STREAM_OBJECT);
        int[] fileparams = (int[]) Array.get(TheFile, HARD_TOTAL_OBJECT);
        if (fileparams[FILE_SIZE] > fileparams[FILE_OFFSET]) {
            int maxlen = fileparams[FILE_SIZE] - fileparams[FILE_OFFSET];
            int count = 0;
            if (ostr != null)
                count = setPartInFileSystem(ostr, data, maxlen);
            if (fileparams.length > FILE_HANDLE)
                count = setPartInHardTotals(fileparams, data, maxlen);
            fileparams[FILE_OFFSET] += count;
        }
        if (fileparams[FILE_SIZE] == fileparams[FILE_OFFSET]) {
            if (ostr != null)
                try {
                    ostr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            TheFile = null;
        }
        return fileparams[0] - fileparams[1];
    }

    private int setPartInFileSystem(FileOutputStream ostr, byte[] data, int maxlen) throws JposException {
        int bytecount = Math.min(maxlen, data.length);
        try {
            ostr.write(data, 0, bytecount);
        } catch (IOException e) {
            throw new JposException(JPOS_E_FAILURE, e.getMessage(), e);
        }
        return bytecount;
    }

    private int setPartInHardTotals(int[] fileparams, byte[] data, int maxlen) throws JposException {
        int bytecount = Math.min(maxlen, data.length);
        Totals.write(fileparams[FILE_HANDLE], data, fileparams[FILE_OFFSET], bytecount);
        return bytecount;
    }

    private String[] extractHardTotalsFilesFromFileSystemFileList(String[] results) throws JposException {
        Map<String, Integer> found = new HashMap<>();
        for (String result : results) {
            int[] handle = {0}, size = {0};
            String htname = getHardTotalsFileName(result);
            if (!found.containsKey(htname)) {
                try {
                    Totals.find(htname, handle, size);
                    found.put(htname, 0);
                } catch (JposException ignored) {}
            }
        }
        results = new String[found.size()];
        int i = 0;
        for (String name : found.keySet()) {
            results[i++] = name;
        }
        return results;
    }

    private String[] getFilesFromHardTotals() throws JposException {
        String[] results;
        results = new String[Totals.getNumberOfFiles()];
        for (int i = 0; i < Totals.getNumberOfFiles(); i++) {
            String[] name = {""};
            Totals.findByIndex(i, name);
            results[i] = name[0];
        }
        return results;
    }

    private String[] getFilesFromFileSystem() {
        String[] results;
        File[] allresults = new File(Path).listFiles();
        int filecount = 0;
        if (allresults != null) {
            for (int i = 0; i < allresults.length; i++) {
                if (allresults[i].exists() && allresults[i].isFile())
                    allresults[filecount++] = allresults[i];
            }
        }
        results = new String[filecount];
        for(int i = 0; i < filecount; i++) {
            results[i] = allresults[i].getName();
        }
        return results;
    }

    private String getHardTotalsFileName(String fileName) throws JposException {
        String hf;
        if (Totals.getCapSingleFile())
            hf = "";
        else {
            hf = fileName.length() > 10 ? fileName.substring(0, 10) : fileName;
            check(hf.length() != hf.getBytes().length, JPOS_E_ILLEGAL, "StorageIO: Invalid File Name: " + fileName);
        }
        return hf;
    }
}
