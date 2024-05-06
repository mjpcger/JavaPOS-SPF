/*
 * Copyright 2019 Martin Conrad
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
 */

package de.gmxhome.conrad.jpos.jpos_base.micr;

import de.gmxhome.conrad.jpos.jpos_base.*;
import jpos.*;

import static de.gmxhome.conrad.jpos.jpos_base.JposDevice.*;
import static jpos.JposConst.*;
import static jpos.MICRConst.*;

/**
 * Class containing the MICR specific properties, their default values and default implementations of
 * MICRInterface.
 * For details about properties, methods and method parameters, see UPOS specification, chapter MICR -
 * Magnetic Ink Character Recognition Reader.
 */
public class MICRProperties extends JposCommonProperties implements MICRInterface {
    /**
     * This property will be used internally to verify whether BeginInsertion and EndInsertion are valid operations.
     * It will be initialized to false during device enable.
     */
    public boolean InsertionMode = false;

    /**
     * This property will be used internally to verify whether BeginRemoval and EndRemoval are valid operations.
     * It will be initialized to false during device enable.
     */
    public boolean RemovalMode = false;

    /**
     * UPOS property AccountNumber.
     */
    public String AccountNumber;

    /**
     * UPOS property Amount.
     */
    public String Amount;

    /**
     * UPOS property BankNumber.
     */
    public String BankNumber;

    /**
     * UPOS property CapValidationDevice. Default: true. Can be overwritten
     * by objects derived from JposDevice within the changeDefaults method.
     */
    public boolean CapValidationDevice = true;

    /**
     * UPOS property CheckType.
     */
    public int CheckType;

    /**
     * UPOS property CountryCode.
     */
    public int CountryCode;

    /**
     * UPOS property EPC.
     */
    public String EPC;

    /**
     * UPOS property RawData.
     */
    public String RawData;

    /**
     * UPOS property SerialNumber.
     */
    public String SerialNumber;

    /**
     * UPOS property TransitNumber.
     */
    public String TransitNumber;

    /**
     * Constructor.
     *
     * @param dev Device index
     */
    public MICRProperties(int dev) {
        super(dev);
    }

    @Override
    public void initOnOpen() {
        super.initOnOpen();
        clearDataProperties();
    }

    @Override
    public void clearDataProperties() {
        AccountNumber = Amount = BankNumber = EPC = RawData = SerialNumber = TransitNumber = "";
        CheckType = CountryCode = 0;
    }

    /*
     * Methods that implement MICR property setter and MICR method calls
     */

    @Override
    public void checkNoData() throws JposException {
        checkext(CheckType == 0, JPOS_EMICR_NODATA, "Data not present");
    }

    @Override
    public void checkBusy() throws JposException {
        check(State != JPOS_S_IDLE, JPOS_E_BUSY, "Output in progress or error detected");
    }

    @Override
    public void beginInsertion(int timeout) throws JposException {
    }

    @Override
    public void beginRemoval(int timeout) throws JposException {
    }

    @Override
    public void endInsertion() throws JposException {
    }

    @Override
    public void endRemoval() throws JposException {
    }
}
