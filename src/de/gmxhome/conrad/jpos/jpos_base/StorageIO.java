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

/**
 * This class provides methods to write data to a data target or read data from a data source. Both, data source and data
 * target can be either a file in the file system or a file on a HardTotals device or both. While a data source is always either
 * a file system file or a file on a HardTotals device, data target can be a file system file and a file on a HardTotals
 * device (in that case, identical data will be written to the file system and the HardTotals device).
 */
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
        JposDevice.check(storage == null, JposConst.JPOS_E_ILLEGAL, "No source device specified");
        if (storage instanceof HardTotals)
            totals = (HardTotals) storage;
        else if (storage instanceof String) {
            File f = new File(path = storage.toString());
            JposDevice.check(!f.exists() || !f.isDirectory(), JposConst.JPOS_E_ILLEGAL, "Invalid folder: " + path);
        } else if (storage instanceof Object[] && Array.getLength(storage) == 2 &&
                Array.get(storage, 0) instanceof HardTotals && Array.get(storage, 1) instanceof String) {
            totals = (HardTotals) Array.get(storage, 0);
            File f = new File(path = Array.get(storage, 1).toString());
            JposDevice.check(!f.exists() || !f.isDirectory(), JposConst.JPOS_E_ILLEGAL, "Invalid folder: " + path);
        } else
            throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid data type for source: " + storage.getClass().getSimpleName());
        JposDevice.check(errorCodeDeviceFull.length > 1, JposConst.JPOS_E_ILLEGAL, "Too many error codes");
        ExtendedErrorCode = errorCodeDeviceFull.length == 0 ? null : errorCodeDeviceFull[0];
    }

    private HardTotals totals = null;
    private String path = null;

    final private Integer ExtendedErrorCode;

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
        JposDevice.check(hardTotals.length > 1, JposConst.JPOS_E_ILLEGAL, "Too many conditions");
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
        JposDevice.check(hardTotals.length > 1, JposConst.JPOS_E_ILLEGAL, "Too many conditions");
        byte[] data = {};
        if (totals != null && (path == null || (hardTotals.length != 0 && hardTotals[0]))) {
            data = getDataFromHardTotals(fileName, length, data);
        } else if (path != null && (totals == null || hardTotals.length == 0 || !hardTotals[0])) {
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
                TheFile = null;
                try {
                    in.close();
                } catch (IOException ee) {}
            }
        } else if (TheFile instanceof int[][]) {
            ret = ((int[][])TheFile)[1][0];
        } else if (TheFile instanceof Object[]) {
            ret = ((int[])((Object[])TheFile)[1])[0];
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
        JposDevice.check(fileName.length > 1, JposConst.JPOS_E_ILLEGAL, "Too many file names");
        int htspace = Integer.MAX_VALUE;
        long fsspace = Integer.MAX_VALUE;
        if (totals != null) {
            htspace = totals.getFreeData();
            if (fileName.length == 1 && fileName[0] != null)
            try {
                int[] handle = {0}, size = {0};
                totals.find(getHardTotalsFileName(fileName[0]), handle, size);
                htspace += size[0];
            } catch (JposException e) {}
        }
        if (path != null) {
            fsspace = new File(path).getFreeSpace();
            if (fileName.length == 1 && fileName[0] != null) {
                File f = new File(path, fileName[0]);
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
        JposDevice.check(data.length > size, JposConst.JPOS_E_ILLEGAL, "End of target reached");
        if (totals != null) {
            if (setDataForHardTotals(fileName, data, size)) return -1;
        }
        if (path != null) {
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
        if (totals != null) {
            String htname = getHardTotalsFileName(fileName);
            int[] handle = {0}, size = {0};
            try {
                totals.find(htname, handle, size);
                htpresent = true;
            } catch (JposException e) {
                if (e.getErrorCode() != JposConst.JPOS_E_NOEXIST)
                    throw e;
                htpresent = false;
            }
        }
        if (path != null) {
            File f = new File(path, fileName);
            fspresent = f.exists() && f.isFile();
        }
        if (totals != null && path != null)
            return fspresent != htpresent ? null : fspresent;
        return htpresent != null ? htpresent : fspresent;
    }

    /**
     * Retrieves the names of all files within the storage. If storage is located on HardTotals and file system, only
     * files present in HardTotals device and on file system are listed.
     * @return Names of files that can be opened on the specified storage.
     * @throws JposException In case of an error.
     */
    public String[] getFileNames() throws JposException {
        String[] results = null;
        if (path != null) {
            results = getFilesFromFileSystem();
        }
        if (totals != null) {
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
        File f = new File(path, fileName);
        JposDevice.check(!f.exists() || !f.isFile(), JposConst.JPOS_E_ILLEGAL, "Invalid file: " + fileName);
        long size = f.length();
        data = new byte[(int)(length < size ? length : size)];
        try {
            FileInputStream in = new FileInputStream(f);
            int pos = 0;
            try {
                int len;
                for (pos = 0; pos < data.length; pos += len)
                    len = in.read(data, pos, data.length - pos);
            } catch (IOException e) {
                data = Arrays.copyOf(data, pos);
                in.close();
            }
            if (data.length == length)
                TheFile = in;
            else
                in.close();
        } catch (IOException e) {
            throw new JposException(JposConst.JPOS_E_FAILURE, e.getMessage(), e);
        }
        return data;
    }

    private byte[] getDataFromHardTotals(String fileName, int length, byte[] data) throws JposException {
        String htname = getHardTotalsFileName(fileName);
        int[] handle = {0}, size = {0};
        totals.find(htname, handle, size);
        data = new byte[size[0] > length ? length : size[0]];
        try {
            totals.read(handle[0], data, 0, data.length);
        } catch (JposException ignore) {
            data = new byte[0];
        }
        if (data.length == length)
            TheFile = new int[][]{ handle, new int[]{size[0], length} };
        return data;
    }

    private byte[] getDataPart(int length) throws JposException {
        byte[] data = {};
        if (TheFile instanceof FileInputStream) {
            data = getPartFromFileSystem(length);
        } else if (TheFile instanceof int[][]) {
            data = getPartFromHardTotals(length);
        } else
            throw new JposException (JposConst.JPOS_E_FAILURE, "Cannot retrieve data from target");
        return data;
    }

    private byte[] getPartFromHardTotals(int length) {
        int handle = ((int[][])TheFile)[0][0];
        int size = ((int[][])TheFile)[1][0];
        int offset = ((int[][])TheFile)[1][1];
        byte[] data = new byte[size > length + offset ? length : size - offset];
        try {
            totals.read(handle, data, offset, data.length);
            ((int[][])TheFile)[1][1] += data.length;
        } catch (JposException e) {
            data = new byte[0];
        }
        if (data.length < length)
            TheFile = null;
        return data;
    }

    private byte[] getPartFromFileSystem(int length) {
        FileInputStream in = (FileInputStream) TheFile;
        byte[] data;
        try {
            FileChannel fc = in.getChannel();
            long size = fc.size() - fc.position();
            data = new byte[(int) (size > length ? length : size)];
            int pos;
            int len;
            for (pos = 0; pos < data.length; pos += len)
                len = in.read(data, pos, data.length - pos);
        } catch (IOException e) {
            data = new byte[0];
        }
        if (data.length < length) {
            TheFile = null;
            try {
                in.close();
            } catch (IOException e) {}
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
                else if (TheFile instanceof FileOutputStream)
                    ((FileOutputStream) TheFile).close();
                else if (TheFile instanceof Object[] && ((Object[])TheFile)[0] instanceof FileOutputStream)
                    ((FileOutputStream)(((Object[])TheFile)[0])).close();
            } catch (IOException e) {
                throw new JposException(JposConst.JPOS_E_FAILURE, e.getMessage(), e);
            } finally {
                TheFile = null;
            }
        }
        return name;
    }

    private Object TheFile = null;

    private boolean setDataForFileSystem(String fileName, byte[] data, int size) throws JposException {
        File f = new File(path, fileName);
        if (f.exists() && f.isFile() && f.length() > data.length)
            f.delete();
        long free = new File(path).getFreeSpace();
        if (free < data.length) {
            if (ExtendedErrorCode != null)
                throw new JposException(JposConst.JPOS_E_EXTENDED, ExtendedErrorCode, "Not enough space: " +
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
                    ((Object[]) TheFile)[0] = ostr;
            }
        } catch (IOException e) {
            if (ostr != null)
                try {
                    ostr.close();
                } catch (IOException ee) {}
            throw new JposException(JposConst.JPOS_E_FAILURE, e.getMessage(), e);
        }
        return false;
    }

    private boolean setDataForHardTotals(String fileName, byte[] data, int size) throws JposException {
        int[] handle = {0}, oldsize = {0};
        String htname = getHardTotalsFileName(fileName);
        try {
            totals.find(htname, handle, oldsize);
            if (oldsize[0] != size && oldsize[0] + totals.getFreeData() >= size)
                totals.delete(htname);
        } catch (JposException e) {}
        if (oldsize[0] != size) {
            int free = totals.getFreeData();
            if (free < size) {
                if (ExtendedErrorCode != null)
                    throw new JposException(JposConst.JPOS_E_EXTENDED, ExtendedErrorCode, "Not enough space: " +
                            free + " < " + size);
                return true;
            }
            totals.create(htname, handle, size, false);
        }
        totals.write(handle[0], data, 0, data.length);
        if (data.length < size) {
            if (TheFile == null)
                TheFile = new Object[]{null, new int[]{size, data.length, handle[0]}};
            else
                ((Object[]) TheFile)[1] = new int[]{size, data.length, handle[0]};
        }
        return false;
    }

    private int setPartData(byte[] data)  throws JposException{
        JposDevice.check(!(TheFile instanceof Object[]), JposConst.JPOS_E_ILLEGAL, "Cannot store data in source");
        FileOutputStream ostr = (FileOutputStream) Array.get(TheFile, 0);
        int[] fileparams = (int[]) Array.get(TheFile, 1);
        if (fileparams[0] > fileparams[1]) {
            int maxlen = fileparams[0] - fileparams[1];
            int count = 0;
            if (ostr != null)
                count = setPartInFileSystem(ostr, fileparams, data, maxlen);
            if (fileparams.length == 3)
                count = setPartInHardTotals(fileparams, data, maxlen);
            fileparams[1] += count;
        }
        if (fileparams[0] == fileparams[1]) {
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

    private int setPartInFileSystem(FileOutputStream ostr, int[] fileparams, byte[] data, int maxlen) throws JposException {
        int bytecount = maxlen > data.length ? data.length : maxlen;
        try {
            ostr.write(data, 0, bytecount);
        } catch (IOException e) {
            throw new JposException(JposConst.JPOS_E_FAILURE, e.getMessage(), e);
        }
        return bytecount;
    }

    private int setPartInHardTotals(int[] fileparams, byte[] data, int maxlen) throws JposException {
        int bytecount = maxlen > data.length ? data.length : maxlen;
        totals.write(fileparams[2], data, fileparams[1], bytecount);
        return bytecount;
    }

    private String[] extractHardTotalsFilesFromFileSystemFileList(String[] results) throws JposException {
        Map<String, Integer> found = new HashMap<>();
        for (int i = 0; i < results.length; i++) {
            int[] handle = {0}, size = {0};
            String htname = getHardTotalsFileName(results[i]);
            if (!found.containsKey(htname)) {
                try {
                    totals.find(htname, handle, size);
                    found.put(htname, 0);
                } catch (JposException e) {}
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
        results = new String[totals.getNumberOfFiles()];
        for (int i = 0; i < totals.getNumberOfFiles(); i++) {
            String[] name = {""};
            totals.findByIndex(i, name);
            results[i] = name[0];
        }
        return results;
    }

    private String[] getFilesFromFileSystem() {
        String[] results;
        File[] allresults = new File(path).listFiles();
        int filecount = 0;
        for  (int i = 0; i < allresults.length; i++) {
            if (allresults[i].exists() && allresults[i].isFile())
                allresults[filecount++] = allresults[i];
        }
        results = new String[filecount];
        for(int i = 0; i < filecount; i++) {
            results[i] = allresults[i].getName();
        }
        return results;
    }

    private String getHardTotalsFileName(String fileName) throws JposException {
        String hf;
        if (totals.getCapSingleFile())
            hf = "";
        else {
            hf = fileName.length() > 10 ? fileName.substring(0, 10) : fileName;
            JposDevice.check(hf.length() != hf.getBytes().length, JposConst.JPOS_E_ILLEGAL, "StorageIO: Invalid File Name: " + fileName);
        }
        return hf;
    }
}
