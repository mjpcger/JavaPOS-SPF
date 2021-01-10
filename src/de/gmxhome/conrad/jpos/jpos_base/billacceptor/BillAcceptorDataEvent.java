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

package de.gmxhome.conrad.jpos.jpos_base.billacceptor;

import de.gmxhome.conrad.jpos.jpos_base.JposBase;
import de.gmxhome.conrad.jpos.jpos_base.JposDataEvent;
import jpos.BillAcceptorConst;

/**
 * Data event implementation for BillAcceptor devices.
 */
public class BillAcceptorDataEvent extends JposDataEvent {
    /**
     * Holds the total of the cash accepted by the BillAcceptor. See UPOS specification, Chapter Bill Acceptor -
     * Properties - DepositCounts for details.
     */
    public String Counts;

    /**
     * The total amount of deposited cash. See UPOS specification, Chapter Bill Acceptor - Properties - DepositAmount
     * for details.
     */
    public int Amount;

    /**
     * Constructor. Parameters passed to base class unchanged.
     *
     * @param source Source, for services implemented with this framework, the (<i>deviceclass</i>.)<i>DeviceClass</i>Service object.
     * @param state  Status, see UPOS specification.
     * @param amount Value to be stored in property DepositAmount.
     * @param counts Value to be stored in property DepositCounts.
     */
    public BillAcceptorDataEvent(JposBase source, int state, int amount, String counts) {
        super(source, state);
        Amount = amount;
        Counts = counts;
    }

    @Override
    public void setDataProperties() {
        super.setDataProperties();
        BillAcceptorProperties props = (BillAcceptorProperties) getPropertySet();
        if (props.DepositStatus == BillAcceptorConst.BACC_STATUS_DEPOSIT_COUNT) {
            props.DepositAmount = Amount;
            props.DepositCounts = Counts;
        }
    }
}
