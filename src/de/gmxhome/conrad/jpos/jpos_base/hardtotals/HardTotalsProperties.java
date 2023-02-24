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

import de.gmxhome.conrad.jpos.jpos_base.JposCommonProperties;
import de.gmxhome.conrad.jpos.jpos_base.JposDevice;
import de.gmxhome.conrad.jpos.jpos_base.SyncObject;
import jpos.JposConst;
import jpos.JposException;

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing the hard totals specific properties, their default values and default implementations of
 * HardTotalsInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter Hard Totals.
 */
public class HardTotalsProperties extends JposCommonProperties implements HardTotalsInterface {
    /**
     * UPOS property CapErrorDetection. Default: false. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapErrorDetection = false;

    /**
     * UPOS property CapSingleFile. Default: true. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapSingleFile = true;

    /**
     * UPOS property CapTransactions. Default: true. Can be overwritten by objects derived from JposDevice within the
     * changeDefaults method.
     */
    public boolean CapTransactions = true;
    /**
     * UPOS property FreeData. Must be initialized in the individual initOnEnable method.
     */
    public Integer FreeData = null;

    /**
     * UPOS property NumberOfFiles. Must be initialized in the individual initOnEnable method.
     */
    public Integer NumberOfFiles = null;

    /**
     * UPOS property TotalsSize. Must be overwritten in the individual initOnEnable method. Value must be &ge; FreeData.
     */
    public Integer TotalsSize = null;

    /**
     * UPOS property TransactionInProgress. Default: false. Must not be overwritten within the changeDefaults method.
     */
    public boolean TransactionInProgress = false;

    /**
     * Buffered SetAll and Write requests, to be performed at transaction end.
     */
    public List<ChangeRequest> Transaction = new ArrayList<ChangeRequest>();

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    protected HardTotalsProperties(int dev) {
        super(dev);
        ExclusiveUse = ExclusiveAllowed;
    }

    private SyncObject HandleWaiter = new SyncObject();
    private List<HardTotalsProperties> WaitingForHandle = new ArrayList<HardTotalsProperties>();

    /**
     * Returns the number of currently claimed hard total files.
     * @return  Number of claimed files.
     */
    public int claimedHandles() {
        return Factory.ClaimedHardTotals.get(Device)[Index].size();
    }

    @Override
    public void close() throws JposException {
        if (TransactionInProgress)
            rollback();
        super.close();
    }

    @Override
    public void beginTrans() throws JposException {
        TransactionInProgress = true;
        EventSource.logSet("TransactionInProgress");
    }

    @Override
    public void claimFile(int hTotalsFile, int timeout) throws JposException {
    }

    @Override
    public void commitTrans(List<ChangeRequest> transaction) throws JposException {
        for (ChangeRequest cr : transaction) {
            cr.invoke();
        }
        TransactionInProgress = false;
        EventSource.logSet("TransactionInProgress");
        Transaction.clear();
    }

    @Override
    public void create(String fileName, int[] hTotalsFile, int size, boolean errorDetection) throws JposException {

    }

    @Override
    public void delete(String fileName) throws JposException {

    }

    @Override
    public void find(String fileName, int[] hTotalsFile, int[] size) throws JposException {

    }

    @Override
    public void findByIndex(int index, String[] fileName) throws JposException {

    }

    @Override
    public void read(int hTotalsFile, byte[] data, int offset, int count, List<ChangeRequest> transaction) throws JposException {

    }

    @Override
    public void recalculateValidationData(int hTotalsFile) throws JposException {

    }

    @Override
    public void releaseFile(int hTotalsFile) throws JposException {

    }

    @Override
    public SetAll setAll(int hTotalsFile, byte value) throws JposException {
        return new SetAll(this, hTotalsFile, value);
    }

    @Override
    public void setAll(SetAll request) throws JposException {
    }

    @Override
    public Write write(int hTotalsFile, byte[] data, int offset, int count) throws JposException {
        return new Write(this, hTotalsFile, data, offset, count);
    }

    @Override
    public void write(Write request) throws JposException {
    }

    @Override
    public void rename(int hTotalsFile, String fileName) throws JposException {
    }

    @Override
    public void rollback() throws JposException {
        TransactionInProgress = false;
        EventSource.logSet("TransactionInProgress");
        Transaction.clear();
    }

    @Override
    public void validateData(int hTotalsFile) throws JposException {
    }
}
