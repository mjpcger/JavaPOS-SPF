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
     * Retrieves data from storage device.
     * @param fileName   Source file name. For HardTotals devices, it must consist of up to <i>N</i> ASCII characters,
     *                   optionally followed by a dot and further characters. <i>N</i> is 0 if property <i>CapSingleFile</i>
     *                   of the HardTotals device is true, otherwise 10. If present, all characters behind the dot and
     *                   the dot itself will be ignored in case of HardTotals files.
     * @return The contents of the storage file specified by the parameters.
     * @throws JposException If an error occurs.
     */
    public byte[] getStorageData(String fileName, boolean... hardTotals) throws JposException {
        if (fileName == null)
            fileName = "";
        JposDevice.check(hardTotals.length > 1, JposConst.JPOS_E_ILLEGAL, "Too many conditions");
        byte[] data = null;
        if (totals != null && (path == null || (hardTotals.length != 0 && hardTotals[0]))) {
            String htname = getHardTotalsFileName(fileName);
            int[] handle = {0}, size = {0};
            totals.find(htname, handle, size);
            data = new byte[size[0]];
            totals.read(handle[0], data, 0, data.length);
        } else if (path != null && (totals == null || hardTotals.length == 0 || !hardTotals[0])) {
            File f = new File(path, fileName);
            if (f.exists() && f.isFile() && f.length() <= Integer.MAX_VALUE)
                data = new byte[(int)f.length()];
            try (FileInputStream in = new FileInputStream(f)) {
                int len;
                for (int pos = 0; pos < data.length; pos += len) {
                    len = in.read(data, pos, data.length - pos);
                }
            } catch (IOException e) {
                throw new JposException(JposConst.JPOS_E_FAILURE, e.getMessage(), e);
            }
        }
        return data;
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
     * @param fileName   Source file name. For HardTotals devices, it must consist of up to <i>N</i> ASCII characters,
     *                   optionally followed by a dot and further characters. <i>N</i> is 0 if property <i>CapSingleFile</i>
     *                   of the HardTotals device is true, otherwise 10. If present, all characters behind the dot and
     *                   the dot itself will be ignored in case of HardTotals files.
     * @param data       Data to be written to the storage.
     * @return true if successful, false if not enough free space is available. In the latter case, the caller should
     *         throw a JposException with ErrorCode E_EXTENDED and the device class specific value E..._NOROOM.
     * @throws JposException If an error occurs.
     */
    public boolean setStorageData(String fileName, byte[] data) throws JposException {
        if (fileName == null)
            fileName = "";
        if (data == null)
            data = new byte[0];
        int[] handle = {0}, size = {0};
        String htname = null;
        if (totals != null) {
            htname = getHardTotalsFileName(fileName);
            try {
                totals.find(htname, handle, size);
                if (size[0] != data.length && size[0] + totals.getFreeData() >= data.length)
                    totals.delete(htname);
            } catch (JposException e) {}
            if (size[0] != data.length) {
                int free = totals.getFreeData();
                if (free < data.length) {
                    if (ExtendedErrorCode != null)
                        throw new JposException(JposConst.JPOS_E_EXTENDED, ExtendedErrorCode, "Not enough space: " +
                            free + " < " + data.length);
                    return false;
                }
                totals.create(htname, handle, data.length, false);
            }
            totals.write(handle[0], data, 0, data.length);
        }
        if (path != null) {
            File f = new File(path, fileName);
            if (f.exists() && f.isFile() && f.length() > data.length)
                f.delete();
            long free = new File(path).getFreeSpace();
            if (free < data.length) {
                if (ExtendedErrorCode != null)
                    throw new JposException(JposConst.JPOS_E_EXTENDED, ExtendedErrorCode, "Not enough space: " +
                            free + " < " + data.length);
                return false;
            }
            try (FileOutputStream ostr = new FileOutputStream(f, false)) {
                ostr.write(data);
            } catch (IOException e) {
                throw new JposException(JposConst.JPOS_E_FAILURE, e.getMessage(), e);
            }
        }
        return true;
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
