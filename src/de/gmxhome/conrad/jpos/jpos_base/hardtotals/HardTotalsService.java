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
    public boolean getCapErrorDetection() throws JposException {
        return false;
    }

    @Override
    public boolean getCapSingleFile() throws JposException {
        return false;
    }

    @Override
    public boolean getCapTransactions() throws JposException {
        return false;
    }

    @Override
    public int getFreeData() throws JposException {
        return 0;
    }

    @Override
    public int getNumberOfFiles() throws JposException {
        return 0;
    }

    @Override
    public int getTotalsSize() throws JposException {
        return 0;
    }

    @Override
    public boolean getTransactionInProgress() throws JposException {
        return false;
    }

    @Override
    public void beginTrans() throws JposException {

    }

    @Override
    public void claimFile(int i, int i1) throws JposException {

    }

    @Override
    public void commitTrans() throws JposException {

    }

    @Override
    public void create(String s, int[] ints, int i, boolean b) throws JposException {

    }

    @Override
    public void delete(String s) throws JposException {

    }

    @Override
    public void find(String s, int[] ints, int[] ints1) throws JposException {

    }

    @Override
    public void findByIndex(int i, String[] strings) throws JposException {

    }

    @Override
    public void read(int i, byte[] bytes, int i1, int i2) throws JposException {

    }

    @Override
    public void recalculateValidationData(int i) throws JposException {

    }

    @Override
    public void releaseFile(int i) throws JposException {

    }

    @Override
    public void rename(int i, String s) throws JposException {

    }

    @Override
    public void rollback() throws JposException {

    }

    @Override
    public void setAll(int i, byte b) throws JposException {

    }

    @Override
    public void validateData(int i) throws JposException {

    }

    @Override
    public void write(int i, byte[] bytes, int i1, int i2) throws JposException {

    }
}
