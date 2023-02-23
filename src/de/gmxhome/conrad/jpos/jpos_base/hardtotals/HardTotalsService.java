/*
 * Copyright 2021 Martin Conrad
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

package de.gmxhome.conrad.jpos.jpos_base.hardtotals;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;
import jpos.services.*;
import jpos.util.JposProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HardTotals service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
public class HardTotalsService extends JposBase implements HardTotalsService115 {
    /**
     * Instance of a class implementing the HardTotalsInterface for hard totals specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public HardTotalsInterface HardTotals;

    private HardTotalsProperties Data;

    /**
     * Constructor. Stores given property set and device implementation object.
     *
     * @param props  Property set.
     * @param device Device implementation object.
     */
    public HardTotalsService(HardTotalsProperties props, JposDevice device) {
        super(props, device);
        Data = props;
    }

    @Override
    public void deleteInstance() throws JposException {
        super.deleteInstance();
        synchronized (Factory.ClaimedHardTotals) {
            if (Device.getCount(Device.HardTotalss) == 0)
                Factory.ClaimedHardTotals.remove(Device);
        }
    }

    // Returns null if not claimed, true if claimed by this instance, false if claimed by other instance.
    private Boolean myHandle(int handle) {
        synchronized (Props.Claiming) {
            HardTotalsProperties props = Factory.ClaimedHardTotals.get(Device)[Data.Index].get(handle);
            if (props != null) {
                return props == Props;
            }
        }
        return null;
    }

    @Override
    public JposCommonProperties startClaiming(SyncObject waiter) {
        JposCommonProperties props;
        Map<Integer,HardTotalsProperties> theMap = Factory.ClaimedHardTotals.get(Device)[Data.Index];
        synchronized (Props.Claiming) {
            if ((props = Props.Claiming[Props.Index]) == null && theMap.size() == 0) {
                Props.Claiming[Props.Index] = Props;
                return null;
            } else {
                if (props == null)
                    props = theMap.values().iterator().next();
                props.ClaimWaiters.add(waiter);
            }
        }
        return props;
    }

    /**
     * Starts claiming of file. Checks whether device and file can be claimed. If so, stores property set in property Claiming,
     * which is a reservation for successful claim operation. Otherwise stores the given SyncObject in the claiming object.
     * The claiming object will signal the object after successful release or releaseFile operation.
     * @param handle    Handle of file to be claimed.
     * @param waiter    SyncObject the caller can use to wait in case of no success.
     * @return          null on success, property set of the instance that claimed the device or file.
     */
    public JposCommonProperties startClaiming(int handle, SyncObject waiter) {
        JposCommonProperties props;
        Map<Integer,HardTotalsProperties> theMap = Factory.ClaimedHardTotals.get(Device)[Data.Index];
        synchronized (Props.Claiming) {
            if ((props = Props.Claiming[Props.Index]) == null && !theMap.containsKey(handle)) {
                theMap.put(handle, Data);
                return null;
            } else {
                if (props == null)
                    props = theMap.get(handle);
                props.ClaimWaiters.add(waiter);
            }
        }
        return props;
    }

    /**
     * Signals release after claimFile. Should be called whenever a claimed file becomes unclaimed to wake up any other
     * waiting instances.
     *
     * @param handle    File handle of file to be released.
     */
    public void signalRelease(int handle) {
        synchronized (Props.Claiming) {
            Factory.ClaimedHardTotals.get(Device)[Data.Index].remove(Props);
        }
        for (SyncObject waiter : Props.ClaimWaiters)
            waiter.signal();
        Props.ClaimWaiters.clear();
    }

    @Override
    public boolean getCapErrorDetection() throws JposException {
        logGet("CapErrorDetection");
        checkOpened();
        return Data.CapErrorDetection;
    }

    @Override
    public boolean getCapSingleFile() throws JposException {
        logGet("CapSingleFile");
        checkOpened();
        return Data.CapSingleFile;
    }

    @Override
    public boolean getCapTransactions() throws JposException {
        logGet("CapTransactions");
        checkOpened();
        return Data.CapTransactions;
    }

    @Override
    public int getFreeData() throws JposException {
        logGet("FreeData");
        checkEnabled();
        JposDevice.check(Data.FreeData == null, JposConst.JPOS_E_FAILURE, "Implementation error, FreeData not initialized");
        return Data.FreeData;
    }

    @Override
    public int getNumberOfFiles() throws JposException {
        logGet("NumberOfFiles");
        checkEnabled();
        JposDevice.check(Data.NumberOfFiles == null, JposConst.JPOS_E_FAILURE, "Implementation error, NumberOfFiles not initialized");
        return Data.NumberOfFiles;
    }

    @Override
    public int getTotalsSize() throws JposException {
        logGet("TotalsSize");
        checkEnabled();
        JposDevice.check(Data.TotalsSize == null, JposConst.JPOS_E_FAILURE, "Implementation error, TotalsSize not initialized");
        return Data.TotalsSize;
    }

    @Override
    public boolean getTransactionInProgress() throws JposException {
        logGet("TransactionInProgress");
        checkOpened();
        return Data.TransactionInProgress;
    }

    @Override
    public void close() throws JposException {
        super.close();
        Data.Transaction.clear();
    }

    @Override
    public void beginTrans() throws JposException {
        logPreCall("BeginTrans");
        checkEnabled();
        JposDevice.check(!Data.CapTransactions, JposConst.JPOS_E_ILLEGAL, "Transactions not supported by service");
        JposDevice.check(Data.TransactionInProgress, JposConst.JPOS_E_ILLEGAL, "Transaction just in progress");
        HardTotals.beginTrans();
        Data.Transaction = new ArrayList<ChangeRequest>();
        logCall("BeginTrans");
    }

    @Override
    public void claimFile(int hTotalsFile, int timeout) throws JposException {
        logPreCall("ClaimFile", "" + hTotalsFile + ", " + timeout);
        checkEnabled();
        Boolean res = myHandle(hTotalsFile);
        JposDevice.check(res != null && res, JposConst.JPOS_E_CLAIMED, "Hard total file claimed");
        JposDevice.check(timeout != JposConst.JPOS_FOREVER && timeout < 0, JposConst.JPOS_E_ILLEGAL, "Invalid timeout value");
        long start = System.currentTimeMillis();
        JposCommonProperties props;
        while (true) {
            SyncObject waiter = new SyncObject();
            props = startClaiming(hTotalsFile, waiter);
            if (props == null)
                break;
            if (timeout == JposConst.JPOS_FOREVER || (timeout > 0 && System.currentTimeMillis() - start < timeout)) {
                waiter.suspend(timeout == JposConst.JPOS_FOREVER ? SyncObject.INFINITE : timeout);
            } else
                throw new JposException(JposConst.JPOS_E_TIMEOUT, "ClaimFile timed out");
        }
        try {
            HardTotals.claimFile(hTotalsFile, timeout);
        } catch (JposException e) {
            signalRelease(hTotalsFile);
            throw e;
        }
        logCall("ClaimFile");
    }

    @Override
    public void commitTrans() throws JposException {
        logPreCall("CommitTrans");
        checkEnabled();
        JposDevice.check(!Data.CapTransactions, JposConst.JPOS_E_ILLEGAL, "Transactions not supported by service");
        JposDevice.check(!Data.TransactionInProgress, JposConst.JPOS_E_ILLEGAL, "Transaction has not been started");
        JposCommonProperties props = Props.getClaimingInstance();
        JposDevice.check(props != null && props != Data, JposConst.JPOS_E_CLAIMED, "Device claimed");
        for (ChangeRequest cr : Data.Transaction) {
            Boolean res = myHandle(cr.getHTotalsFile());
            JposDevice.check(res != null && !res, JposConst.JPOS_E_CLAIMED, "Hard total file claimed");
        }
        HardTotals.commitTrans(Data.Transaction);
        Data.Transaction.clear();
        logCall("CommitTrans");
    }

    @Override
    public void create(String fileName, int[] hTotalsFile, int size, boolean errorDetection) throws JposException {
        if (fileName == null)
            fileName = "";
        logPreCall("Create", fileName + ", [], " + size + ", " + errorDetection);
        checkEnabled();
        JposDevice.check(Data.CapSingleFile && fileName.length() > 0, JposConst.JPOS_E_ILLEGAL, "Filename not empty");
        JposDevice.check(fileName.length() > 10, JposConst.JPOS_E_ILLEGAL, "Filename too long");
        JposDevice.check(invalidCharacters(fileName), JposConst.JPOS_E_ILLEGAL, "Filename " + fileName + "contains non-ASCII characters");
        JposDevice.check(hTotalsFile == null || hTotalsFile.length != 1, JposConst.JPOS_E_ILLEGAL, "Bad file handle type, must be int[1]");
        JposDevice.check(size < 0, JposConst.JPOS_E_ILLEGAL, "Invalid file size: " + size);
        JposCommonProperties props = Props.getClaimingInstance();
        JposDevice.check(props != null && props != Data, JposConst.JPOS_E_CLAIMED, "Device claimed");
        HardTotals.create(fileName, hTotalsFile, size, errorDetection);
        logCall("Create", "" + hTotalsFile[0]);
    }

    private boolean invalidCharacters(String fileName) {
        for(int i = 0; i < fileName.length(); i++) {
            char c = fileName.charAt(i);
            if (c < 0x20 || c > 0x7f)
                return true;
        }
        return false;
    }

    @Override
    public void delete(String fileName) throws JposException {
        if (fileName == null)
            fileName = "";
        logPreCall("Delete", fileName);
        checkEnabled();
        JposDevice.check(Data.CapSingleFile && fileName.length() > 0, JposConst.JPOS_E_ILLEGAL, "Filename not empty");
        JposDevice.check(fileName.length() > 10, JposConst.JPOS_E_ILLEGAL, "Filename too long");
        JposDevice.check(invalidCharacters(fileName), JposConst.JPOS_E_ILLEGAL, "Filename " + fileName + "contains non-ASCII characters");
        JposCommonProperties props = Props.getClaimingInstance();
        JposDevice.check(props != null && props != Data, JposConst.JPOS_E_CLAIMED, "Device claimed");
        HardTotals.delete(fileName);
        logCall("Delete");
    }

    @Override
    public void find(String fileName, int[] hTotalsFile, int[] size) throws JposException {
        if (fileName == null)
            fileName = "";
        logPreCall("Find", fileName + ", [], []");
        checkEnabled();
        JposDevice.check(Data.CapSingleFile && fileName.length() > 0, JposConst.JPOS_E_ILLEGAL, "Filename not empty");
        JposDevice.check(fileName.length() > 10, JposConst.JPOS_E_ILLEGAL, "Filename too long");
        JposDevice.check(invalidCharacters(fileName), JposConst.JPOS_E_ILLEGAL, "Filename " + fileName + "contains non-ASCII characters");
        JposDevice.check(hTotalsFile == null || hTotalsFile.length != 1, JposConst.JPOS_E_ILLEGAL, "Bad file handle type, must be int[1]");
        JposDevice.check(size == null || size.length != 1, JposConst.JPOS_E_ILLEGAL, "Bad size type, must be int[1]");
        HardTotals.find(fileName, hTotalsFile, size);
        logCall("Find", "" + hTotalsFile[0] + ", " + size[0]);
    }

    @Override
    public void findByIndex(int index, String[] fileName) throws JposException {
        logPreCall("FindByIndex", "" + index + ", [], []");
        checkEnabled();
        JposDevice.check(fileName == null || fileName.length != 1, JposConst.JPOS_E_ILLEGAL, "Bad fileName type, must be String[1]");
        JposDevice.check(index < 0 || index >= Data.NumberOfFiles, JposConst.JPOS_E_ILLEGAL, "Index out of range");
        HardTotals.findByIndex(index, fileName);
        logCall("FindByIndex", fileName[0]);
    }

    @Override
    public void read(int hTotalsFile, byte[] data, int offset, int count) throws JposException {
        if (data == null)
            data = new byte[0];
        logPreCall("Read", "" + hTotalsFile + ", [], " + offset + ", " + count);
        checkEnabled();
        JposDevice.check(data == null, JposConst.JPOS_E_ILLEGAL, "Bad data type, must be byte[]");
        JposDevice.check(offset < 0, JposConst.JPOS_E_ILLEGAL, "Invalid offset: " + count);
        JposDevice.check(count < 0, JposConst.JPOS_E_ILLEGAL, "Invalid count: " + count);
        JposDevice.check(offset + count > Data.TotalsSize, JposConst.JPOS_E_ILLEGAL, "Invalid read position");
        JposDevice.check(data.length < count, JposConst.JPOS_E_ILLEGAL, "Data buffer too small: " + data.length);
        JposCommonProperties props = Props.getClaimingInstance();
        JposDevice.check(props != null && props != Data, JposConst.JPOS_E_CLAIMED, "Device claimed");
        for (ChangeRequest cr : Data.Transaction) {
            Boolean res = myHandle(cr.getHTotalsFile());
            JposDevice.check(res != null && !res, JposConst.JPOS_E_CLAIMED, "Hard total file claimed");
        }
        HardTotals.read(hTotalsFile, data, offset, count, Data.Transaction);
        logCall("Read", "" + data.toString());
    }

    @Override
    public void recalculateValidationData(int hTotalsFile) throws JposException {
        logPreCall("RecalculateValidationData", "" + hTotalsFile);
        checkEnabled();
        JposCommonProperties props = Props.getClaimingInstance();
        JposDevice.check(props != null && props != Data, JposConst.JPOS_E_CLAIMED, "Device claimed");
        Boolean res = myHandle(hTotalsFile);
        JposDevice.check(res != null && !res, JposConst.JPOS_E_CLAIMED, "Hard total file claimed");
        HardTotals.recalculateValidationData(hTotalsFile);
        logCall("RecalculateValidationData");
    }

    @Override
    public void releaseFile(int hTotalsFile) throws JposException {
        logPreCall("ReleaseFile", "" + hTotalsFile);
        checkEnabled();
        Boolean res = myHandle(hTotalsFile);
        JposDevice.check(res == null || !res, JposConst.JPOS_E_NOTCLAIMED, "Hard total file not claimed");
        HardTotals.releaseFile(hTotalsFile);
        signalRelease(hTotalsFile);
        logCall("ReleaseFile");
    }

    @Override
    public void rename(int hTotalsFile, String fileName) throws JposException {
        if (fileName == null)
            fileName = "";
        logPreCall("Rename", "" + hTotalsFile + ", " + fileName);
        checkEnabled();
        JposDevice.check(Data.CapSingleFile && fileName.length() > 0, JposConst.JPOS_E_ILLEGAL, "Filename not empty");
        JposDevice.check(fileName.length() > 10, JposConst.JPOS_E_ILLEGAL, "Filename too long");
        JposDevice.check(invalidCharacters(fileName), JposConst.JPOS_E_ILLEGAL, "Filename " + fileName + "contains non-ASCII characters");
        HardTotals.rename(hTotalsFile, fileName);
        logCall("Rename");
    }

    @Override
    public void rollback() throws JposException {
        logPreCall("Rollback");
        checkEnabled();
        JposDevice.check(!Data.CapTransactions, JposConst.JPOS_E_ILLEGAL, "Transactions not supported by service");
        JposDevice.check(!Data.TransactionInProgress, JposConst.JPOS_E_ILLEGAL, "Transaction has not been started");
        HardTotals.rollback();
        Data.Transaction.clear();
        logCall("Rollback");
    }

    @Override
    public void setAll(int hTotalsFile, byte value) throws JposException {
        logPreCall("SetAll", "" + hTotalsFile + ", " + value);
        checkEnabled();
        JposCommonProperties props = Props.getClaimingInstance();
        JposDevice.check(props != null && props != Data, JposConst.JPOS_E_CLAIMED, "Device claimed");
        Boolean res = myHandle(hTotalsFile);
        JposDevice.check(res != null && !res, JposConst.JPOS_E_CLAIMED, "Hard total file claimed");
        SetAll request = HardTotals.setAll(hTotalsFile, value);
        if (Data.TransactionInProgress) {
            Data.Transaction.add(request);
        }
        else {
            request.invoke();
        }
        logCall("SetAll");
    }

    @Override
    public void validateData(int hTotalsFile) throws JposException {
        logPreCall("ValidateData", "" + hTotalsFile);
        checkEnabled();
        JposCommonProperties props = Props.getClaimingInstance();
        JposDevice.check(props != null && props != Data, JposConst.JPOS_E_CLAIMED, "Device claimed");
        Boolean res = myHandle(hTotalsFile);
        JposDevice.check(res != null && !res, JposConst.JPOS_E_CLAIMED, "Hard total file claimed");
        HardTotals.validateData(hTotalsFile);
        logCall("ValidateData");
    }

    @Override
    public void write(int hTotalsFile, byte[] data, int offset, int count) throws JposException {
        if (data == null)
            data = new byte[0];
        logPreCall("Write", "" + hTotalsFile + ", " + data.toString() + ", " + offset + ", " + count);
        checkEnabled();
        JposDevice.check(data == null, JposConst.JPOS_E_ILLEGAL, "Bad data type, must be byte[]");
        JposDevice.check(offset < 0, JposConst.JPOS_E_ILLEGAL, "Invalid offset: " + count);
        JposDevice.check(count < 0, JposConst.JPOS_E_ILLEGAL, "Invalid count: " + count);
        JposDevice.check(offset + count > Data.TotalsSize, JposConst.JPOS_E_ILLEGAL, "Invalid read position");
        JposDevice.check(data.length < count, JposConst.JPOS_E_ILLEGAL, "Data buffer too small: " + data.length);
        JposCommonProperties props = Props.getClaimingInstance();
        JposDevice.check(props != null && props != Data, JposConst.JPOS_E_CLAIMED, "Device claimed");
        Boolean res = myHandle(hTotalsFile);
        JposDevice.check(res != null && !res, JposConst.JPOS_E_CLAIMED, "Hard total file claimed");
        Write request = HardTotals.write(hTotalsFile, data, offset, count);
        if (Data.TransactionInProgress) {
            Data.Transaction.add(request);
        }
        else {
            request.invoke();
        }
        logCall("Write", "" + data.toString());
    }
}
