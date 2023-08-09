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

/**
 * Class holding values for MICR data in data and error events
 */
public class Data {
    /**
     * Creates a data object that can hold MICR data to be stored before MICRDataEvent or MICRErrorEvent processing starts.
     * @param account       Value for AccountNumber, see UPOS specification.
     * @param amount        Value for Amount, see UPOS specification.
     * @param bank          Value for BankNumber, see UPOS specification.
     * @param checktype     Value for CheckType, see UPOS specification.
     * @param country       Value for CountryCode, see UPOS specification.
     * @param epc           Value for EPC, see UPOS specification.
     * @param rawdata       Value for RawData, see UPOS specification.
     * @param serial        Value for SerialNumber, see UPOS specification.
     * @param transit       Value for TransitNumber, see UPOS specification.
     */
    public Data(String account, String amount, String bank, int checktype, int country, String epc, String rawdata, String serial, String transit) {
        AccountNumber = account;
        Amount = amount;
        BankNumber = bank;
        CheckType = checktype;
        CountryCode = country;
        EPC = epc;
        RawData = rawdata;
        SerialNumber = serial;
        TransitNumber = transit;
    }

    /**
     * Set device instance properties from properties of the device implementation.
     * @param props Property set of claiming device instance.
     */
    public void setDataProperties (MICRProperties props) {
        props.AccountNumber = AccountNumber;
        props.Amount = Amount;
        props.BankNumber = BankNumber;
        props.CheckType = CheckType;
        props.CountryCode = CountryCode;
        props.EPC = EPC;
        props.RawData = RawData;
        props.SerialNumber = SerialNumber;
        props.TransitNumber = TransitNumber;
    }

    /**
     * Contents of MICR property AccountNumber. Will be set before data or error event will be fired. See UPOS specification:
     * This account number will not include a check serial number if a check serial number is able to be separately parsed,
     * even if the check serial number is embedded in the account number portion of the 'On Us' field.
     */
    public String AccountNumber;

    /**
     * Contents of MICR property Amount. Will be set before data or error event will be fired. See UPOS specification:
     * The amount field on a check consists of ten digits bordered by Amount symbols. All non space digits will be represented
     * in the test string including leading 0’s.
     */
    public String Amount;

    /**
     * Contents of MICR property BankNumber. Will be set before data or error event will be fired. See UPOS specification:
     * The bank number is contained in digits 5 through 8 of the transit field.
     */
    public String BankNumber;

    /**
     * Contents of MICR property CheckType. Will be set before data or error event will be fired. See UPOS specification:
     * Holds the type of check parsed from the most recently read MICR data, one of CT_PERSONAL, CT_BUSINESS or CT_UNKNOWN.
     */
    public int CheckType;

    /**
     * Contents of MICR property CountryCode. Will be set before data or error event will be fired. See UPOS specification:
     * Holds the country of origin of the check parsed from the most recently read MICR data. Or, if the country cannot
     * be determined, indicates the check font, legal values are CC_USA, CC_CANADA, CC_MEXICO, CC_UNKNOWN, CC_CMC7 or
     * CC_OTHER.
     */
    public int CountryCode;

    /**
     * Contents of MICR property EPC. Will be set before data or error event will be fired. See UPOS specification:
     * Holds the Extended Processing Code ("EPC") field parsed from the most recently read MICR data. It will contain a
     * single character 0 though 9 if the field is present
     */
    public String EPC;

    /**
     * Contents of MICR property RawData. Will be set before data or error event will be fired. See UPOS specification:
     * Holds the MICR data from the most recent MICRService read. It contains any of the MICR characters with appropriate
     * substitution to represent non-ASCII characters (see "MICR Character Substitution", in UPOS specification).
     */
    public String RawData;

    /**
     * Contents of MICR property SerialNumber. Will be set before data or error event will be fired. See UPOS specification:
     * Holds the serial number of the check parsed from the most recently read MICR data.
     */
    public String SerialNumber;

    /**
     * Contents of MICR property TransitNumber. Will be set before data or error event will be fired. See UPOS specification:
     * Holds the transit field of the check parsed from the most recently read MICR data. It consists of all the characters
     * read between the 'Transit' symbols on the check. It is a nine character string
     */
    public String TransitNumber;
}
