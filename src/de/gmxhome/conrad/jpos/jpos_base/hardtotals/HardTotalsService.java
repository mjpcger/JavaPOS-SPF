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

import java.util.*;

import static de.gmxhome.conrad.jpos.jpos_base.SyncObject.INFINITE;
import static jpos.JposConst.*;

/**
 * HardTotals service implementation. For more details about getter, setter and method implementations,
 * see JposBase.
 */
@SuppressWarnings("SynchronizeOnNonFinalField")
public class HardTotalsService extends JposBase implements HardTotalsService116 {
    /**
     * Instance of a class implementing the HardTotalsInterface for hard totals specific setter and method calls bound
     * to the property set. Almost always the same object as Data.
     */
    public HardTotalsInterface HardTotals;

    private final HardTotalsProperties Data;

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
        JposDevice dev = Device;
        super.deleteInstance();
        synchronized (Factory.ClaimedHardTotals) {
            if (JposBaseDevice.getCount(dev.HardTotalss) == 0) {
                Factory.ClaimedHardTotals.remove(dev);
            }
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
            Factory.ClaimedHardTotals.get(Device)[Data.Index].remove(handle);
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
        check(Data.FreeData == null, JPOS_E_FAILURE, "Implementation error, FreeData not initialized");
        return Data.FreeData;
    }

    @Override
    public int getNumberOfFiles() throws JposException {
        logGet("NumberOfFiles");
        checkEnabled();
        check(Data.NumberOfFiles == null, JPOS_E_FAILURE, "Implementation error, NumberOfFiles not initialized");
        return Data.NumberOfFiles;
    }

    @Override
    public int getTotalsSize() throws JposException {
        logGet("TotalsSize");
        checkEnabled();
        check(Data.TotalsSize == null, JPOS_E_FAILURE, "Implementation error, TotalsSize not initialized");
        return Data.TotalsSize;
    }

    @Override
    public boolean getTransactionInProgress() throws JposException {
        logGet("TransactionInProgress");
        checkOpened();
        return Data.TransactionInProgress;
    }

    @Override
    public void beginTrans() throws JposException {
        logPreCall("BeginTrans");
        checkEnabled();
        check(!Data.CapTransactions, JPOS_E_ILLEGAL, "Transactions not supported by service");
        check(Data.TransactionInProgress, JPOS_E_ILLEGAL, "Transaction just in progress");
        HardTotals.beginTrans();
        synchronized (Device.HardTotalss[Data.Index]) {
            Data.Transaction.clear();
        }
        logCall("BeginTrans");
    }

    @Override
    public void claimFile(int hTotalsFile, int timeout) throws JposException {
        logPreCall("ClaimFile", removeOuterArraySpecifier(new Object[]{hTotalsFile, timeout}, Device.MaxArrayStringElements));
        checkEnabledUnclaimed();
        Boolean res = myHandle(hTotalsFile);
        check(res != null && res, JPOS_E_CLAIMED, "Hard total file claimed");
        check(res != null, JPOS_E_CLAIMED, "Hard total file claimed by other instance");
        check(timeout != JPOS_FOREVER && timeout < 0, JPOS_E_ILLEGAL, "Invalid timeout value");
        long start = System.currentTimeMillis();
        JposCommonProperties props;
        while (true) {
            SyncObject waiter = new SyncObject();
            props = startClaiming(hTotalsFile, waiter);
            if (props == null)
                break;
            if (timeout == JPOS_FOREVER || (timeout > 0 && System.currentTimeMillis() - start < timeout)) {
                waiter.suspend(timeout == JPOS_FOREVER ? INFINITE : timeout);
            } else
                throw new JposException(JPOS_E_TIMEOUT, "ClaimFile timed out");
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
        checkEnabledUnclaimed();
        check(!Data.CapTransactions, JPOS_E_ILLEGAL, "Transactions not supported by service");
        check(!Data.TransactionInProgress, JPOS_E_ILLEGAL, "Transaction has not been started");
        synchronized (Device.HardTotalss[Data.Index]) {
            for (ChangeRequest cr : Data.Transaction) {
                Boolean res = myHandle(cr.getHTotalsFile());
                check(res != null && !res, JPOS_E_CLAIMED, "Hard total file claimed");
            }
        }
        HardTotals.commitTrans();
        synchronized (Device.HardTotalss[Data.Index]) {
            Data.Transaction.clear();
        }
        logCall("CommitTrans");
    }

    @Override
    public void create(String fileName, int[] hTotalsFile, int size, boolean errorDetection) throws JposException {
        logPreCall("Create", removeOuterArraySpecifier(new Object[]{fileName, "...", size, errorDetection}, Device.MaxArrayStringElements));
        if (fileName == null)
            fileName = "";
        checkEnabledUnclaimed();
        check(Data.CapSingleFile && fileName.length() > 0, JPOS_E_ILLEGAL, "Filename not empty");
        check(fileName.length() > 10, JPOS_E_ILLEGAL, "Filename too long");
        check(invalidCharacters(fileName), JPOS_E_ILLEGAL, "Filename " + fileName + "contains non-ASCII characters");
        check(hTotalsFile == null || hTotalsFile.length != 1, JPOS_E_ILLEGAL, "Bad file handle type, must be int[1]");
        check(size < 0, JPOS_E_ILLEGAL, "Invalid file size: " + size);
        HardTotals.create(fileName, hTotalsFile, size, errorDetection);
        logCall("Create", removeOuterArraySpecifier(new Object[]{"...", hTotalsFile[0], "..."}, Device.MaxArrayStringElements));
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
        logPreCall("Delete", removeOuterArraySpecifier(new Object[]{fileName}, Device.MaxArrayStringElements));
        if (fileName == null)
            fileName = "";
        checkEnabledUnclaimed();
        check(Data.CapSingleFile && fileName.length() > 0, JPOS_E_ILLEGAL, "Filename not empty");
        check(fileName.length() > 10, JPOS_E_ILLEGAL, "Filename too long");
        check(invalidCharacters(fileName), JPOS_E_ILLEGAL, "Filename " + fileName + "contains non-ASCII characters");
        HardTotals.delete(fileName);
        logCall("Delete");
    }

    @Override
    public void find(String fileName, int[] hTotalsFile, int[] size) throws JposException {
        logPreCall("Find", removeOuterArraySpecifier(new Object[]{fileName, "..."}, Device.MaxArrayStringElements));
        if (fileName == null)
            fileName = "";
        checkEnabledUnclaimed();
        check(Data.CapSingleFile && fileName.length() > 0, JPOS_E_ILLEGAL, "Filename not empty");
        check(fileName.length() > 10, JPOS_E_ILLEGAL, "Filename too long");
        check(invalidCharacters(fileName), JPOS_E_ILLEGAL, "Filename " + fileName + "contains non-ASCII characters");
        check(hTotalsFile == null || hTotalsFile.length != 1, JPOS_E_ILLEGAL, "Bad file handle type, must be int[1]");
        check(size == null || size.length != 1, JPOS_E_ILLEGAL, "Bad size type, must be int[1]");
        HardTotals.find(fileName, hTotalsFile, size);
        logCall("Find", removeOuterArraySpecifier(new Object[]{"...", hTotalsFile[0], size[0]}, Device.MaxArrayStringElements));
    }

    @Override
    public void findByIndex(int index, String[] fileName) throws JposException {
        logPreCall("FindByIndex", removeOuterArraySpecifier(new Object[]{index, "..."}, Device.MaxArrayStringElements));
        checkEnabled();
        check(fileName == null || fileName.length != 1, JPOS_E_ILLEGAL, "Bad fileName type, must be String[1]");
        check(index < 0 || index >= Data.NumberOfFiles, JPOS_E_ILLEGAL, "Index out of range");
        HardTotals.findByIndex(index, fileName);
        logCall("FindByIndex", removeOuterArraySpecifier(new Object[]{"...", fileName[0]}, Device.MaxArrayStringElements));
    }

    @Override
    public void read(int hTotalsFile, byte[] data, int offset, int count) throws JposException {
        logPreCall("Read", removeOuterArraySpecifier(new Object[]{hTotalsFile, "...", offset, count}, Device.MaxArrayStringElements));
        if (data == null)
            data = new byte[0];
        checkEnabledUnclaimed();
        check(offset < 0, JPOS_E_ILLEGAL, "Invalid offset: " + count);
        check(count < 0, JPOS_E_ILLEGAL, "Invalid count: " + count);
        check(offset + count > Data.TotalsSize, JPOS_E_ILLEGAL, "Invalid read position");
        check(data.length < count, JPOS_E_ILLEGAL, "Data buffer too small: " + data.length);
        Boolean res = myHandle(hTotalsFile);
        check(res != null && !res, JPOS_E_CLAIMED, "Hard total file claimed");
        List<ChangeRequest> openChanges = new ArrayList<>();
        synchronized (Device.HardTotalss[Data.Index]) {
            for (JposCommonProperties cp : Device.HardTotalss[Data.Index]) {
                HardTotalsProperties currentprops = (HardTotalsProperties) cp;
                for (ChangeRequest cr : currentprops.Transaction) {
                    if (cr.getHTotalsFile() == hTotalsFile)
                        openChanges.add(cr);
                }
            }
        }
        HardTotals.read(hTotalsFile, data, offset, count, openChanges);
        openChanges.clear();
        logCall("Read", removeOuterArraySpecifier(new Object[]{"...", data, "..."}, Device.MaxArrayStringElements));
    }

    @Override
    public void recalculateValidationData(int hTotalsFile) throws JposException {
        logPreCall("RecalculateValidationData", removeOuterArraySpecifier(new Object[]{hTotalsFile}, Device.MaxArrayStringElements));
        checkEnabledUnclaimed();
        Boolean res = myHandle(hTotalsFile);
        check(res != null && !res, JPOS_E_CLAIMED, "Hard total file claimed");
        check(!Data.CapErrorDetection, JPOS_E_ILLEGAL, "Error detection not supported");
        HardTotals.recalculateValidationData(hTotalsFile);
        logCall("RecalculateValidationData");
    }

    @Override
    public void releaseFile(int hTotalsFile) throws JposException {
        logPreCall("ReleaseFile", removeOuterArraySpecifier(new Object[]{hTotalsFile}, Device.MaxArrayStringElements));
        checkEnabledUnclaimed();
        Boolean res = myHandle(hTotalsFile);
        check(res == null, JPOS_E_NOTCLAIMED, "Hard total file not claimed");
        check(!res, JPOS_E_CLAIMED, "Hard total file claimed by other instance");
        HardTotals.releaseFile(hTotalsFile);
        signalRelease(hTotalsFile);
        logCall("ReleaseFile");
    }

    @Override
    public void rename(int hTotalsFile, String fileName) throws JposException {
        logPreCall("Rename", removeOuterArraySpecifier(new Object[]{hTotalsFile, fileName}, Device.MaxArrayStringElements));
        if (fileName == null)
            fileName = "";
        checkEnabledUnclaimed();
        check(Data.CapSingleFile && fileName.length() > 0, JPOS_E_ILLEGAL, "Filename not empty");
        check(fileName.length() > 10, JPOS_E_ILLEGAL, "Filename too long");
        check(invalidCharacters(fileName), JPOS_E_ILLEGAL, "Filename " + fileName + "contains non-ASCII characters");
        Boolean res = myHandle(hTotalsFile);
        check(res != null && !res, JPOS_E_CLAIMED, "Hard total file claimed");
        HardTotals.rename(hTotalsFile, fileName);
        logCall("Rename");
    }

    @Override
    public void rollback() throws JposException {
        logPreCall("Rollback");
        checkEnabled();
        check(!Data.CapTransactions, JPOS_E_ILLEGAL, "Transactions not supported by service");
        check(!Data.TransactionInProgress, JPOS_E_ILLEGAL, "Transaction has not been started");
        HardTotals.rollback();
        synchronized (Device.HardTotalss[Data.Index]) {
            Data.Transaction.clear();
        }
        logCall("Rollback");
    }

    @Override
    public void setAll(int hTotalsFile, byte value) throws JposException {
        logPreCall("SetAll", removeOuterArraySpecifier(new Object[]{hTotalsFile, value}, Device.MaxArrayStringElements));
        checkEnabledUnclaimed();
        Boolean res = myHandle(hTotalsFile);
        check(res != null && !res, JPOS_E_CLAIMED, "Hard total file claimed");
        SetAll request = HardTotals.setAll(hTotalsFile, value);
        if (Data.TransactionInProgress) {
            synchronized (Device.HardTotalss[Data.Index]) {
                Data.Transaction.add(request);
            }
        }
        else {
            request.invoke();
        }
        logCall("SetAll");
    }

    @Override
    public void validateData(int hTotalsFile) throws JposException {
        logPreCall("ValidateData", removeOuterArraySpecifier(new Object[]{hTotalsFile}, Device.MaxArrayStringElements));
        checkEnabledUnclaimed();
        Boolean res = myHandle(hTotalsFile);
        check(res != null && !res, JPOS_E_CLAIMED, "Hard total file claimed");
        check(!Data.CapErrorDetection, JPOS_E_ILLEGAL, "Error detection not supported");
        HardTotals.validateData(hTotalsFile);
        logCall("ValidateData");
    }

    @Override
    public void write(int hTotalsFile, byte[] data, int offset, int count) throws JposException {
        logPreCall("Write", removeOuterArraySpecifier(new Object[]{hTotalsFile, data, offset, count}, Device.MaxArrayStringElements));
        checkEnabledUnclaimed();
        check(data == null, JPOS_E_ILLEGAL, "Bad data type, must be byte[]");
        check(offset < 0, JPOS_E_ILLEGAL, "Invalid offset: " + count);
        check(count < 0, JPOS_E_ILLEGAL, "Invalid count: " + count);
        check(offset + count > Data.TotalsSize, JPOS_E_ILLEGAL, "Invalid read position");
        check(data.length < count, JPOS_E_ILLEGAL, "Data buffer too small: " + data.length);
        Boolean res = myHandle(hTotalsFile);
        check(res != null && !res, JPOS_E_CLAIMED, "Hard total file claimed");
        Write request = HardTotals.write(hTotalsFile, data, offset, count);
        if (Data.TransactionInProgress) {
            synchronized (Device.HardTotalss[Data.Index]) {
                Data.Transaction.add(request);
            }
        }
        else {
            request.invoke();
        }
        logCall("Write", removeOuterArraySpecifier(new Object[]{"...", data, "..."}, Device.MaxArrayStringElements));
    }
}
