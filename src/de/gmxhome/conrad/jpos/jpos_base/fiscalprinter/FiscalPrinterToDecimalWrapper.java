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

package de.gmxhome.conrad.jpos.jpos_base.fiscalprinter;

import jpos.FiscalPrinterConst;
import jpos.JposConst;
import jpos.JposException;
import jpos.config.JposEntry;
import jpos.config.simple.SimpleEntry;
import jpos.loader.JposServiceInstance;
import jpos.loader.JposServiceInstanceFactory;
import jpos.services.*;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Wrapper class as interface between applications and services which use different kinds of currency-to-string
 * conversion.
 * No wrapper is necessary for applications which use the same conversion as the unwrapped service: Given a value of
 * 123.4567, stored in a long with implicit 4 decimal, two conversions are possible: Conversion to a decimal string
 * with decimal point ("123.4567") or conversion to a string which represents the internal long value ("1234567").
 * <br>If both, application and service, use the same type of conversion, no wrapper is necessary.
 * <br>If the application used the internal value representation, but the service works with decimal string with decimal
 * point, this wrapper class can be used to translate between both representations on-the-fly.
 * <br>If the application uses decimal strings with decimal point but the service uses the internal value representation,
 * use the FiscalPrinterToIntegerWrapper class.
 * <br><br>Configuration is as follows:
 * The name of the wrapper class must be configured within the <i>factoryClass</i> attribute of the <i>creation</i> tag
 * present in the JposEntry tag of jpos.xml. The name of the service class must be specified
 * in the <i>serviceClass</i> attribute of the same <i>creation</i> tag.
 * <br>The name of the class factory of the service class must be specified in the <i>value</i> attribute of a <i>prop</i>
 * tag with <b>JavaPOS_SPF_WrappedClassFactory</b> in the corresponding <i>name</i> attribute.
 * Furthermore, to specify the position of an optional percent character in the amount of a package adjustment, use
 * &lt;prop name="JavaPOS_SPF_TrailingPercent" value="false"/&gt; to allow a leading percent character. Default is to
 * allow a trailing percent character.
 */
public class FiscalPrinterToDecimalWrapper implements JposServiceInstanceFactory {
    private static class Wrapper {
        JposServiceInstanceFactory Factory = null;
        JposEntry Entries = new SimpleEntry();
        boolean TrailingPercent = true;
    }
    static final Map<String, Wrapper> Wrappers = new HashMap<>();
    @Override
    public JposServiceInstance createInstance(String name, JposEntry jposEntry) throws JposException {
        Exception errorException;
        Wrapper wrapper;
        synchronized (Wrappers) {
            wrapper = Wrappers.get(name);
        }
        while (wrapper == null) {
            wrapper = new Wrapper();
            String classes = null;
            for (Iterator<JposEntry.Prop> iter = jposEntry.getProps(); iter.hasNext(); ) {
                JposEntry.Prop prop = iter.next();
                if (prop.getName().equals(JposEntry.DEVICE_CATEGORY_PROP_NAME) && !prop.getValue().toString().equals("FiscalPrinter"))
                    throw new JposException(JposConst.JPOS_E_NOSERVICE, "Bad Device Category");
                else if (prop.getName().equals("JavaPOS_SPF_WrappedClassFactory")) {
                    classes = prop.getValue().toString();
                    continue;       // consume the wrapper entry
                } else if (prop.getName().equals("JavaPOS_SPF_TrailingPercent")) {
                    wrapper.TrailingPercent = Boolean.parseBoolean(prop.getValue().toString());
                    continue;       // consume the wrapper entry
                }
                wrapper.Entries.add(prop);
            }
            if (classes == null)
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Wrapped Service Not Specified");
            wrapper.Entries.modifyPropertyValue(JposEntry.SI_FACTORY_CLASS_PROP_NAME, classes);
            try {
                wrapper.Factory = (JposServiceInstanceFactory) Class.forName(classes).getConstructor().newInstance();
                synchronized (Wrappers) {
                    if (Wrappers.containsKey(name))
                        wrapper = Wrappers.get(name);
                    else
                        Wrappers.put(name, wrapper);
                }
                break;
            } catch (NumberFormatException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
                errorException = e;
            }
            throw new JposException(JposConst.JPOS_E_NOSERVICE, "FactoryClassInstantiationError: " + errorException.getMessage(), errorException);
        }
        JposServiceInstance srv = wrapper.Factory.createInstance(name, wrapper.Entries);
        if (!(srv instanceof FiscalPrinterService13))
            throw new JposException(JposConst.JPOS_E_NOSERVICE, "Bad Device Class:" + srv.getClass().getSimpleName());
        return new WrapperService((FiscalPrinterService13) srv, wrapper.TrailingPercent);
    }

    static private class WrapperService implements FiscalPrinterService115 {
        /**
         * Constructor for use as wrapper class.
         * @param service         Instance of the wrapped service class.
         * @param trailingPercent true if no or trailing percent in amount of package adjustment methods is accepted, false
         *                        if leading percent character is allowed.
         */
        public WrapperService(FiscalPrinterService13 service, boolean trailingPercent) {
            Service = service;
            TrailingPercent = trailingPercent;
        }

        private FiscalPrinterService13 Service;
        private boolean TrailingPercent;

        @Override
        public void printRecItemRefund(String s, long l, int i, int i1, long l1, String s1) throws JposException {
            if (!(Service instanceof FiscalPrinterService112))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.12");
            ((FiscalPrinterService112) Service).printRecItemRefund(s, l, i, i1, l1, s1);
        }

        @Override
        public void printRecItemRefundVoid(String s, long l, int i, int i1, long l1, String s1) throws JposException {
            if (!(Service instanceof FiscalPrinterService112))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.12");
            ((FiscalPrinterService112) Service).printRecItemRefundVoid(s, l, i, i1, l1, s1);
        }

        @Override
        public boolean getCapPositiveSubtotalAdjustment() throws JposException {
            if (!(Service instanceof FiscalPrinterService111))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.11");
            return ((FiscalPrinterService111) Service).getCapPositiveSubtotalAdjustment();
        }

        @Override
        public void printRecItemAdjustmentVoid(int i, String s, long l, int i1) throws JposException {
            if (!(Service instanceof FiscalPrinterService111))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.11");
            ((FiscalPrinterService111) Service).printRecItemAdjustmentVoid(i, s, l, i1);
        }

        @Override
        public void printRecItemVoid(String s, long l, int i, int i1, long l1, String s1) throws JposException {
            if (!(Service instanceof FiscalPrinterService111))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.11");
            ((FiscalPrinterService111) Service).printRecItemVoid(s, l, i, i1, l1, s1);
        }

        @Override
        public boolean getCapCompareFirmwareVersion() throws JposException {
            if (!(Service instanceof FiscalPrinterService19))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.9");
            return ((FiscalPrinterService19) Service).getCapCompareFirmwareVersion();
        }

        @Override
        public boolean getCapUpdateFirmware() throws JposException {
            if (!(Service instanceof FiscalPrinterService19))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.9");
            return ((FiscalPrinterService19) Service).getCapUpdateFirmware();
        }

        @Override
        public void compareFirmwareVersion(String s, int[] ints) throws JposException {
            if (!(Service instanceof FiscalPrinterService19))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.9");
            ((FiscalPrinterService19) Service).compareFirmwareVersion(s, ints);
        }

        @Override
        public void updateFirmware(String s) throws JposException {
            if (!(Service instanceof FiscalPrinterService19))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.9");
            ((FiscalPrinterService19) Service).updateFirmware(s);
        }

        @Override
        public boolean getCapStatisticsReporting() throws JposException {
            if (!(Service instanceof FiscalPrinterService18))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.8");
            return ((FiscalPrinterService18) Service).getCapStatisticsReporting();
        }

        @Override
        public boolean getCapUpdateStatistics() throws JposException {
            if (!(Service instanceof FiscalPrinterService18))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.8");
            return ((FiscalPrinterService18) Service).getCapUpdateStatistics();
        }

        @Override
        public void resetStatistics(String s) throws JposException {
            if (!(Service instanceof FiscalPrinterService18))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.8");
            ((FiscalPrinterService18) Service).resetStatistics(s);
        }

        @Override
        public void retrieveStatistics(String[] strings) throws JposException {
            if (!(Service instanceof FiscalPrinterService18))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.8");
            ((FiscalPrinterService18) Service).retrieveStatistics(strings);
        }

        @Override
        public void updateStatistics(String s) throws JposException {
            if (!(Service instanceof FiscalPrinterService18))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.8");
            ((FiscalPrinterService18) Service).updateStatistics(s);
        }

        @Override
        public int getAmountDecimalPlaces() throws JposException {
            if (!(Service instanceof FiscalPrinterService17))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.7");
            return ((FiscalPrinterService17) Service).getAmountDecimalPlaces();
        }

        @Override
        public boolean getCapAdditionalHeader() throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            return ((FiscalPrinterService16) Service).getCapAdditionalHeader();
        }

        @Override
        public boolean getCapAdditionalTrailer() throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            return ((FiscalPrinterService16) Service).getCapAdditionalTrailer();
        }

        @Override
        public boolean getCapChangeDue() throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            return ((FiscalPrinterService16) Service).getCapChangeDue();
        }

        @Override
        public boolean getCapEmptyReceiptIsVoidable() throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            return ((FiscalPrinterService16) Service).getCapEmptyReceiptIsVoidable();
        }

        @Override
        public boolean getCapFiscalReceiptStation() throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            return ((FiscalPrinterService16) Service).getCapFiscalReceiptStation();
        }

        @Override
        public boolean getCapFiscalReceiptType() throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            return ((FiscalPrinterService16) Service).getCapFiscalReceiptType();
        }

        @Override
        public boolean getCapMultiContractor() throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            return ((FiscalPrinterService16) Service).getCapMultiContractor();
        }

        @Override
        public boolean getCapOnlyVoidLastItem() throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            return ((FiscalPrinterService16) Service).getCapOnlyVoidLastItem();
        }

        @Override
        public boolean getCapPackageAdjustment() throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            return ((FiscalPrinterService16) Service).getCapPackageAdjustment();
        }

        @Override
        public boolean getCapPostPreLine() throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            return ((FiscalPrinterService16) Service).getCapPostPreLine();
        }

        @Override
        public boolean getCapSetCurrency() throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            return ((FiscalPrinterService16) Service).getCapSetCurrency();
        }

        @Override
        public boolean getCapTotalizerType() throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            return ((FiscalPrinterService16) Service).getCapTotalizerType();
        }

        @Override
        public int getActualCurrency() throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            return ((FiscalPrinterService16) Service).getActualCurrency();
        }

        @Override
        public String getAdditionalHeader() throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            return ((FiscalPrinterService16) Service).getAdditionalHeader();
        }

        @Override
        public void setAdditionalHeader(String s) throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            ((FiscalPrinterService16) Service).setAdditionalHeader(s);
        }

        @Override
        public String getAdditionalTrailer() throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            return ((FiscalPrinterService16) Service).getAdditionalTrailer();
        }

        @Override
        public void setAdditionalTrailer(String s) throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            ((FiscalPrinterService16) Service).setAdditionalTrailer(s);
        }

        @Override
        public String getChangeDue() throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            return ((FiscalPrinterService16) Service).getChangeDue();
        }

        @Override
        public void setChangeDue(String s) throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            ((FiscalPrinterService16) Service).setChangeDue(s);
        }

        @Override
        public int getContractorId() throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            return ((FiscalPrinterService16) Service).getContractorId();
        }

        @Override
        public void setContractorId(int i) throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            ((FiscalPrinterService16) Service).setContractorId(i);
        }

        @Override
        public int getDateType() throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            return ((FiscalPrinterService16) Service).getDateType();
        }

        @Override
        public void setDateType(int i) throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            ((FiscalPrinterService16) Service).setDateType(i);
        }

        @Override
        public int getFiscalReceiptStation() throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            return ((FiscalPrinterService16) Service).getFiscalReceiptStation();
        }

        @Override
        public void setFiscalReceiptStation(int i) throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            ((FiscalPrinterService16) Service).setFiscalReceiptStation(i);
        }

        @Override
        public int getFiscalReceiptType() throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            return ((FiscalPrinterService16) Service).getFiscalReceiptType();
        }

        @Override
        public void setFiscalReceiptType(int i) throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            ((FiscalPrinterService16) Service).setFiscalReceiptType(i);
        }

        @Override
        public int getMessageType() throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            return ((FiscalPrinterService16) Service).getMessageType();
        }

        @Override
        public void setMessageType(int i) throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            ((FiscalPrinterService16) Service).setMessageType(i);
        }

        @Override
        public String getPostLine() throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            return ((FiscalPrinterService16) Service).getPostLine();
        }

        @Override
        public void setPostLine(String s) throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            ((FiscalPrinterService16) Service).setPostLine(s);
        }

        @Override
        public String getPreLine() throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            return ((FiscalPrinterService16) Service).getPreLine();
        }

        @Override
        public void setPreLine(String s) throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            ((FiscalPrinterService16) Service).setPreLine(s);
        }

        @Override
        public int getTotalizerType() throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            return ((FiscalPrinterService16) Service).getTotalizerType();
        }

        @Override
        public void setTotalizerType(int i) throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            ((FiscalPrinterService16) Service).setTotalizerType(i);
        }

        @Override
        public void printRecCash(long l) throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            ((FiscalPrinterService16) Service).printRecCash(l);
        }

        @Override
        public void printRecItemFuel(String s, long l, int i, int i1, long l1, String s1, long l2, String s2) throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            ((FiscalPrinterService16) Service).printRecItemFuel(s, l, i, i1, l1, s1, l2, s2);
        }

        @Override
        public void printRecItemFuelVoid(String s, long l, int i, long l1) throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            ((FiscalPrinterService16) Service).printRecItemFuelVoid(s, l, i, l1);
        }

        @Override
        public void printRecPackageAdjustVoid(int adjustmentType, String vatAdjustment) throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            vatAdjustment = handleVatAdjustment(vatAdjustment);
            ((FiscalPrinterService16) Service).printRecPackageAdjustVoid(adjustmentType, vatAdjustment);
        }

        @Override
        public void printRecPackageAdjustment(int adjustmentType, String description, String vatAdjustment) throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            vatAdjustment = handleVatAdjustment(vatAdjustment);
            ((FiscalPrinterService16) Service).printRecPackageAdjustment(adjustmentType, description, vatAdjustment);
        }

        @Override
        public void printRecRefundVoid(String s, long l, int i) throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            ((FiscalPrinterService16) Service).printRecRefundVoid(s, l, i);
        }

        @Override
        public void printRecSubtotalAdjustVoid(int i, long l) throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            ((FiscalPrinterService16) Service).printRecSubtotalAdjustVoid(i, l);
        }

        @Override
        public void printRecTaxID(String s) throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            ((FiscalPrinterService16) Service).printRecTaxID(s);
        }

        @Override
        public void setCurrency(int i) throws JposException {
            if (!(Service instanceof FiscalPrinterService16))
                throw new JposException(JposConst.JPOS_E_NOSERVICE, "Service does not support version 1.6");
            ((FiscalPrinterService16) Service).setCurrency(i);
        }

        private String handleVatAdjustment(String vatAdjustment) throws JposException {
            String[] adjustments = vatAdjustment.split(";");
            String result = "";
            for (String pair : adjustments) {
                String[] args = pair.split(",");
                try {
                    String[] parts = args[1].split("%", -1);
                    if (parts.length == 1)
                        args[1] = new BigDecimal(Long.parseLong(args[1])).scaleByPowerOfTen(-4).stripTrailingZeros().toPlainString();
                    else if (parts.length == 2 && (parts[0].length() == 0 || parts[1].length() == 0)) {
                        args[1] = new BigDecimal(Long.parseLong(parts[parts[0].length() == 0 ? 1 : 0])).scaleByPowerOfTen(-4).stripTrailingZeros().toPlainString();
                        args[1] = TrailingPercent ? args[1] + "%" : "%" + args[1];
                    }
                    // No change for unknown format: more than 1 '%' character, characters before and behind '%' character
                } catch (Exception e) {
                    throw new JposException(JposConst.JPOS_E_ILLEGAL, "Invalid amount argument: " + e.getMessage(), e);
                }
                result = result + ";" + args[0] + "," + args[1];
            }
            return result.substring(1);
        }

        @Override
        public boolean getCapAdditionalLines() throws JposException {
            return Service.getCapAdditionalLines();
        }

        @Override
        public boolean getCapAmountAdjustment() throws JposException {
            return Service.getCapAmountAdjustment();
        }

        @Override
        public boolean getCapAmountNotPaid() throws JposException {
            return Service.getCapAmountNotPaid();
        }

        @Override
        public boolean getCapCheckTotal() throws JposException {
            return Service.getCapCheckTotal();
        }

        @Override
        public boolean getCapCoverSensor() throws JposException {
            return Service.getCapCoverSensor();
        }

        @Override
        public boolean getCapDoubleWidth() throws JposException {
            return Service.getCapDoubleWidth();
        }

        @Override
        public boolean getCapDuplicateReceipt() throws JposException {
            return Service.getCapDuplicateReceipt();
        }

        @Override
        public boolean getCapFixedOutput() throws JposException {
            return Service.getCapFixedOutput();
        }

        @Override
        public boolean getCapHasVatTable() throws JposException {
            return Service.getCapHasVatTable();
        }

        @Override
        public boolean getCapIndependentHeader() throws JposException {
            return Service.getCapIndependentHeader();
        }

        @Override
        public boolean getCapItemList() throws JposException {
            return Service.getCapItemList();
        }

        @Override
        public boolean getCapJrnEmptySensor() throws JposException {
            return Service.getCapJrnEmptySensor();
        }

        @Override
        public boolean getCapJrnNearEndSensor() throws JposException {
            return Service.getCapJrnNearEndSensor();
        }

        @Override
        public boolean getCapJrnPresent() throws JposException {
            return Service.getCapJrnPresent();
        }

        @Override
        public boolean getCapNonFiscalMode() throws JposException {
            return Service.getCapNonFiscalMode();
        }

        @Override
        public boolean getCapOrderAdjustmentFirst() throws JposException {
            return Service.getCapOrderAdjustmentFirst();
        }

        @Override
        public boolean getCapPercentAdjustment() throws JposException {
            return Service.getCapPercentAdjustment();
        }

        @Override
        public boolean getCapPositiveAdjustment() throws JposException {
            return Service.getCapPositiveAdjustment();
        }

        @Override
        public boolean getCapPowerLossReport() throws JposException {
            return Service.getCapPowerLossReport();
        }

        @Override
        public int getCapPowerReporting() throws JposException {
            return Service.getCapPowerReporting();
        }

        @Override
        public boolean getCapPredefinedPaymentLines() throws JposException {
            return Service.getCapPredefinedPaymentLines();
        }

        @Override
        public boolean getCapRecEmptySensor() throws JposException {
            return Service.getCapRecEmptySensor();
        }

        @Override
        public boolean getCapRecNearEndSensor() throws JposException {
            return Service.getCapRecNearEndSensor();
        }

        @Override
        public boolean getCapRecPresent() throws JposException {
            return Service.getCapRecPresent();
        }

        @Override
        public boolean getCapReceiptNotPaid() throws JposException {
            return Service.getCapReceiptNotPaid();
        }

        @Override
        public boolean getCapRemainingFiscalMemory() throws JposException {
            return Service.getCapRemainingFiscalMemory();
        }

        @Override
        public boolean getCapReservedWord() throws JposException {
            return Service.getCapReservedWord();
        }

        @Override
        public boolean getCapSetHeader() throws JposException {
            return Service.getCapSetHeader();
        }

        @Override
        public boolean getCapSetPOSID() throws JposException {
            return Service.getCapSetPOSID();
        }

        @Override
        public boolean getCapSetStoreFiscalID() throws JposException {
            return Service.getCapSetStoreFiscalID();
        }

        @Override
        public boolean getCapSetTrailer() throws JposException {
            return Service.getCapSetTrailer();
        }

        @Override
        public boolean getCapSetVatTable() throws JposException {
            return Service.getCapSetVatTable();
        }

        @Override
        public boolean getCapSlpEmptySensor() throws JposException {
            return Service.getCapSlpEmptySensor();
        }

        @Override
        public boolean getCapSlpFiscalDocument() throws JposException {
            return Service.getCapSlpFiscalDocument();
        }

        @Override
        public boolean getCapSlpFullSlip() throws JposException {
            return Service.getCapSlpFullSlip();
        }

        @Override
        public boolean getCapSlpNearEndSensor() throws JposException {
            return Service.getCapSlpNearEndSensor();
        }

        @Override
        public boolean getCapSlpPresent() throws JposException {
            return Service.getCapSlpPresent();
        }

        @Override
        public boolean getCapSlpValidation() throws JposException {
            return Service.getCapSlpValidation();
        }

        @Override
        public boolean getCapSubAmountAdjustment() throws JposException {
            return Service.getCapSubAmountAdjustment();
        }

        @Override
        public boolean getCapSubPercentAdjustment() throws JposException {
            return Service.getCapSubPercentAdjustment();
        }

        @Override
        public boolean getCapSubtotal() throws JposException {
            return Service.getCapSubtotal();
        }

        @Override
        public boolean getCapTrainingMode() throws JposException {
            return Service.getCapTrainingMode();
        }

        @Override
        public boolean getCapValidateJournal() throws JposException {
            return Service.getCapValidateJournal();
        }

        @Override
        public boolean getCapXReport() throws JposException {
            return Service.getCapXReport();
        }

        @Override
        public int getAmountDecimalPlace() throws JposException {
            return Service.getAmountDecimalPlace();
        }

        @Override
        public boolean getAsyncMode() throws JposException {
            return Service.getAsyncMode();
        }

        @Override
        public void setAsyncMode(boolean b) throws JposException {
            Service.setAsyncMode(b);
        }

        @Override
        public boolean getCheckTotal() throws JposException {
            return Service.getCheckTotal();
        }

        @Override
        public void setCheckTotal(boolean b) throws JposException {
            Service.setCheckTotal(b);
        }

        @Override
        public int getCountryCode() throws JposException {
            return Service.getCountryCode();
        }

        @Override
        public boolean getCoverOpen() throws JposException {
            return Service.getCoverOpen();
        }

        @Override
        public boolean getDayOpened() throws JposException {
            return Service.getDayOpened();
        }

        @Override
        public int getDescriptionLength() throws JposException {
            return Service.getDescriptionLength();
        }

        @Override
        public boolean getDuplicateReceipt() throws JposException {
            return Service.getDuplicateReceipt();
        }

        @Override
        public void setDuplicateReceipt(boolean b) throws JposException {
            Service.setDuplicateReceipt(b);
        }

        @Override
        public int getErrorLevel() throws JposException {
            return Service.getErrorLevel();
        }

        @Override
        public int getErrorOutID() throws JposException {
            return Service.getErrorOutID();
        }

        @Override
        public int getErrorState() throws JposException {
            return Service.getErrorState();
        }

        @Override
        public int getErrorStation() throws JposException {
            return Service.getErrorStation();
        }

        @Override
        public String getErrorString() throws JposException {
            return Service.getErrorString();
        }

        @Override
        public boolean getFlagWhenIdle() throws JposException {
            return Service.getFlagWhenIdle();
        }

        @Override
        public void setFlagWhenIdle(boolean b) throws JposException {
            Service.setFlagWhenIdle(b);
        }

        @Override
        public boolean getJrnEmpty() throws JposException {
            return Service.getJrnEmpty();
        }

        @Override
        public boolean getJrnNearEnd() throws JposException {
            return Service.getJrnNearEnd();
        }

        @Override
        public int getMessageLength() throws JposException {
            return Service.getMessageLength();
        }

        @Override
        public int getNumHeaderLines() throws JposException {
            return Service.getNumHeaderLines();
        }

        @Override
        public int getNumTrailerLines() throws JposException {
            return Service.getNumTrailerLines();
        }

        @Override
        public int getNumVatRates() throws JposException {
            return Service.getNumVatRates();
        }

        @Override
        public int getOutputID() throws JposException {
            return Service.getOutputID();
        }

        @Override
        public int getPowerNotify() throws JposException {
            return Service.getPowerNotify();
        }

        @Override
        public void setPowerNotify(int i) throws JposException {
            Service.setPowerNotify(i);
        }

        @Override
        public int getPowerState() throws JposException {
            return Service.getPowerState();
        }

        @Override
        public String getPredefinedPaymentLines() throws JposException {
            return Service.getPredefinedPaymentLines();
        }

        @Override
        public int getPrinterState() throws JposException {
            return Service.getPrinterState();
        }

        @Override
        public int getQuantityDecimalPlaces() throws JposException {
            return Service.getQuantityDecimalPlaces();
        }

        @Override
        public int getQuantityLength() throws JposException {
            return Service.getQuantityLength();
        }

        @Override
        public boolean getRecEmpty() throws JposException {
            return Service.getRecEmpty();
        }

        @Override
        public boolean getRecNearEnd() throws JposException {
            return Service.getRecNearEnd();
        }

        @Override
        public int getRemainingFiscalMemory() throws JposException {
            return Service.getRemainingFiscalMemory();
        }

        @Override
        public String getReservedWord() throws JposException {
            return Service.getReservedWord();
        }

        @Override
        public int getSlipSelection() throws JposException {
            return Service.getSlipSelection();
        }

        @Override
        public void setSlipSelection(int i) throws JposException {
            Service.setSlipSelection(i);
        }

        @Override
        public boolean getSlpEmpty() throws JposException {
            return Service.getSlpEmpty();
        }

        @Override
        public boolean getSlpNearEnd() throws JposException {
            return Service.getSlpNearEnd();
        }

        @Override
        public boolean getTrainingModeActive() throws JposException {
            return Service.getTrainingModeActive();
        }

        @Override
        public void beginFiscalDocument(int i) throws JposException {
            Service.beginFiscalDocument(i);
        }

        @Override
        public void beginFiscalReceipt(boolean b) throws JposException {
            Service.beginFiscalReceipt(b);
        }

        @Override
        public void beginFixedOutput(int i, int i1) throws JposException {
            Service.beginFixedOutput(i, i1);
        }

        @Override
        public void beginInsertion(int i) throws JposException {
            Service.beginInsertion(i);
        }

        @Override
        public void beginItemList(int i) throws JposException {
            Service.beginItemList(i);
        }

        @Override
        public void beginNonFiscal() throws JposException {
            Service.beginNonFiscal();
        }

        @Override
        public void beginRemoval(int i) throws JposException {
            Service.beginRemoval(i);
        }

        @Override
        public void beginTraining() throws JposException {
            Service.beginTraining();
        }

        @Override
        public void clearError() throws JposException {
            Service.clearError();
        }

        @Override
        public void clearOutput() throws JposException {
            Service.clearOutput();
        }

        @Override
        public void endFiscalDocument() throws JposException {
            Service.endFiscalDocument();
        }

        @Override
        public void endFiscalReceipt(boolean b) throws JposException {
            Service.endFiscalReceipt(b);
        }

        @Override
        public void endFixedOutput() throws JposException {
            Service.endFixedOutput();
        }

        @Override
        public void endInsertion() throws JposException {
            Service.endInsertion();
        }

        @Override
        public void endItemList() throws JposException {
            Service.endItemList();
        }

        @Override
        public void endNonFiscal() throws JposException {
            Service.endNonFiscal();
        }

        @Override
        public void endRemoval() throws JposException {
            Service.endRemoval();
        }

        @Override
        public void endTraining() throws JposException {
            Service.endTraining();
        }

        @Override
        public void getData(int dataItem, int[] ints, String[] data) throws JposException {
            Service.getData(dataItem, ints, data);
            // If conversion is necessary depends on dataItem and optArgs:
            switch (dataItem) {
                case FiscalPrinterConst.FPTR_GD_CURRENT_TOTAL:
                case FiscalPrinterConst.FPTR_GD_DAILY_TOTAL:
                case FiscalPrinterConst.FPTR_GD_GRAND_TOTAL:
                case FiscalPrinterConst.FPTR_GD_NOT_PAID:
                case FiscalPrinterConst.FPTR_GD_REFUND:
                case FiscalPrinterConst.FPTR_GD_REFUND_VOID:
                    try {
                        data[0] = Long.toString(new BigDecimal(data[0]).scaleByPowerOfTen(4).longValueExact());
                    } catch (Exception e) {
                    }    // Return unchanged data in case of data format error or overflow
            }
        }

        @Override
        public void getDate(String[] strings) throws JposException {
            Service.getDate(strings);
        }

        @Override
        public void getTotalizer(int i, int i1, String[] data) throws JposException {
            Service.getTotalizer(i, i1, data);
            // All totalizers must be converted from decimal string with dot to integer format.
            try {
                data[0] = Long.toString(new BigDecimal(data[0]).scaleByPowerOfTen(4).longValueExact());
            } catch (Exception e) {
            }    // Return unchanged data in case of data format error or overflow
        }

        @Override
        public void getVatEntry(int i, int i1, int[] ints) throws JposException {
            Service.getVatEntry(i, i1, ints);
        }

        @Override
        public void printDuplicateReceipt() throws JposException {
            Service.printDuplicateReceipt();
        }

        @Override
        public void printFiscalDocumentLine(String s) throws JposException {
            Service.printFiscalDocumentLine(s);
        }

        @Override
        public void printFixedOutput(int i, int i1, String s) throws JposException {
            Service.printFixedOutput(i, i1, s);
        }

        @Override
        public void printNormal(int i, String s) throws JposException {
            Service.printNormal(i, s);
        }

        @Override
        public void printPeriodicTotalsReport(String s, String s1) throws JposException {
            Service.printPeriodicTotalsReport(s, s1);
        }

        @Override
        public void printPowerLossReport() throws JposException {
            Service.printPowerLossReport();
        }

        @Override
        public void printRecItem(String s, long l, int i, int i1, long l1, String s1) throws JposException {
            Service.printRecItem(s, l, i, i1, l1, s1);
        }

        @Override
        public void printRecItemAdjustment(int i, String s, long l, int i1) throws JposException {
            Service.printRecItemAdjustment(i, s, l, i1);
        }

        @Override
        public void printRecMessage(String s) throws JposException {
            Service.printRecMessage(s);
        }

        @Override
        public void printRecNotPaid(String s, long l) throws JposException {
            Service.printRecNotPaid(s, l);
        }

        @Override
        public void printRecRefund(String s, long l, int i) throws JposException {
            Service.printRecRefund(s, l, i);
        }

        @Override
        public void printRecSubtotal(long l) throws JposException {
            Service.printRecSubtotal(l);
        }

        @Override
        public void printRecSubtotalAdjustment(int i, String s, long l) throws JposException {
            Service.printRecSubtotalAdjustment(i, s, l);
        }

        @Override
        public void printRecTotal(long l, long l1, String s) throws JposException {
            Service.printRecTotal(l, l1, s);
        }

        @Override
        public void printRecVoid(String s) throws JposException {
            Service.printRecVoid(s);
        }

        @Override
        public void printRecVoidItem(String s, long l, int i, int i1, long l1, int i2) throws JposException {
            Service.printRecVoidItem(s, l, i, i1, l1, i2);
        }

        @Override
        public void printReport(int i, String s, String s1) throws JposException {
            Service.printReport(i, s, s1);
        }

        @Override
        public void printXReport() throws JposException {
            Service.printXReport();
        }

        @Override
        public void printZReport() throws JposException {
            Service.printZReport();
        }

        @Override
        public void resetPrinter() throws JposException {
            Service.resetPrinter();
        }

        @Override
        public void setDate(String s) throws JposException {
            Service.setDate(s);
        }

        @Override
        public void setHeaderLine(int i, String s, boolean b) throws JposException {
            Service.setHeaderLine(i, s, b);
        }

        @Override
        public void setPOSID(String s, String s1) throws JposException {
            Service.setPOSID(s, s1);
        }

        @Override
        public void setStoreFiscalID(String s) throws JposException {
            Service.setStoreFiscalID(s);
        }

        @Override
        public void setTrailerLine(int i, String s, boolean b) throws JposException {
            Service.setTrailerLine(i, s, b);
        }

        @Override
        public void setVatTable() throws JposException {
            Service.setVatTable();
        }

        @Override
        public void setVatValue(int i, String s) throws JposException {
            Service.setVatValue(i, s);
        }

        @Override
        public void verifyItem(String s, int i) throws JposException {
            Service.verifyItem(s, i);
        }

        @Override
        public void deleteInstance() throws JposException {
            Service.deleteInstance();
        }

        @Override
        public String getCheckHealthText() throws JposException {
            return Service.getCheckHealthText();
        }

        @Override
        public boolean getClaimed() throws JposException {
            return Service.getClaimed();
        }

        @Override
        public boolean getDeviceEnabled() throws JposException {
            return Service.getDeviceEnabled();
        }

        @Override
        public void setDeviceEnabled(boolean b) throws JposException {
            Service.setDeviceEnabled(b);
        }

        @Override
        public String getDeviceServiceDescription() throws JposException {
            return Service.getDeviceServiceDescription();
        }

        @Override
        public int getDeviceServiceVersion() throws JposException {
            return Service.getDeviceServiceVersion();
        }

        @Override
        public boolean getFreezeEvents() throws JposException {
            return Service.getFreezeEvents();
        }

        @Override
        public void setFreezeEvents(boolean b) throws JposException {
            Service.setFreezeEvents(b);
        }

        @Override
        public String getPhysicalDeviceDescription() throws JposException {
            return Service.getPhysicalDeviceDescription();
        }

        @Override
        public String getPhysicalDeviceName() throws JposException {
            return Service.getPhysicalDeviceName();
        }

        @Override
        public int getState() throws JposException {
            return Service.getState();
        }

        @Override
        public void claim(int i) throws JposException {
            Service.claim(i);
        }

        @Override
        public void close() throws JposException {
            Service.close();
        }

        @Override
        public void checkHealth(int i) throws JposException {
            Service.checkHealth(i);
        }

        @Override
        public void directIO(int i, int[] ints, Object o) throws JposException {
            Service.directIO(i, ints, o);
        }

        @Override
        public void open(String s, EventCallbacks eventCallbacks) throws JposException {
            Service.open(s, eventCallbacks);
        }

        @Override
        public void release() throws JposException {
            Service.release();
        }
    }
}
