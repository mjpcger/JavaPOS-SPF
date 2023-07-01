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

package de.gmxhome.conrad.jpos.jpos_base.electronicvaluerw;

import de.gmxhome.conrad.jpos.jpos_base.JposDevice;

import java.lang.reflect.*;
import java.math.*;
import java.security.InvalidParameterException;
import java.text.*;
import java.util.*;

/**
 * Implementation of a type-safe map of strings using a string key. Has been implemented to allow implementations of
 * ElectronicValueRW methods setParameterInformation and retrieveResultInformation.
 * <br>Internally, one global map named Types must be filled with <i>tag</i> / <i>type</i> entries before the first use of a
 * TypeSafeStringMap object, where the <i>tag</i> specifies a unique String that identifies a parameter and <i>type</i>
 * is one of String.class (for Datetime values), Long.class (for currency values), Integer.class (for Number values),
 * Boolean.class (for Boolean values) or int[] (for Enumerated values, contains all valid integer values). See the UPOS
 * specification, chapter 15.5.31, for a description of the UPOS types Datetime, Currency, Number, Boolean and Enumerated.
 * A special case are tags of type String. They must not be specified, they are the default.
 * <br>The given values will be stored as String (String and Datetime values), Long (Currency values), Integer (Number
 * and Enumerated values) or Boolean (Boolean values) objects. Method put and putAll convert the value string to the
 * type specified by the tag (the key). If conversion fails, an exception (NullPointerException, ArithmeticException,
 * ParseException) will be thrown.
 * <br>When retrieved via get, the value will be converted back to a String representing the same value. Keep in mind
 * that the string representation can be different to the value used in the put method (truncated zeroes, missing sign,...),
 * but the represented value itself should be the same.
 * <br>Usage of get and put in a service implementation remains possible, but it is recommended to use the typed methods
 * instead (getString, getDateTime, getCurrency, getNumber, getBoolean, getEnumerated or getObject instead of get,
 * putString, putDatetime, putCurrency, putNumber, putBoolean or putEnumerated instead of put) to set or retrieve values
 * directly without conversion. However, in case of a type mismatch, these methods throw a RuntimeException.
 */
public class TypeSafeStringMap implements Map<String,String>, Cloneable {
    private HashMap<String,Object> Me;
    private static Map<String,Object> Types = new HashMap<>();
    private boolean UseEnumeratedValues, StrongEnumerationCheck;

    private class TSSEntry implements Entry<String,String> {
        String Key, Value;

        TSSEntry(String key, String value) {
            Key = key;
            Value = value;
        }
        @Override
        public String getKey() {
            return Key;
        }

        @Override
        public String getValue() {
            return Value;
        }

        @Override
        public String setValue(String value) {
            String ret = Value;
            Value = value;
            return ret;
        }
    }

    static private class SIVals {
        String Key;
        int Value;
        SIVals(String key, int value) {
            Key = key;
            Value = value;
        }
    }

    /**
     * Constructor for type safe String map. Should only be used by service implementations. EVRW implementations derived
     * from JposDevice or ElectronicValueRWProperties should use method emptyClone of TypesResults or TypedParameters which
     * are properties of the ElectronicValueRWProperties used.
     *
     * @param useEnumeratedValues If true, integer value of Enumerated values will be used, Otherwise the name of the
     *                            constant in ElectronicValueRWConst.
     * @param strongEnumerationCheck If true, setting Enumerated values fails if the value to be set does not match one
     *                               of the predefined values. If false, any numeric value will be accepted.
     */
    TypeSafeStringMap(boolean useEnumeratedValues, boolean strongEnumerationCheck) {
        UseEnumeratedValues = useEnumeratedValues;
        StrongEnumerationCheck = strongEnumerationCheck;
        Me = new HashMap<>();
    }


    /**
     * Create an empty TypeSafeStringMap with the same internal structure as the calling object. This ensures that
     * Enumerated values will be handled the same way.
     * @return New empty TypeSafeStringMap object with identical properties.
     */
    public TypeSafeStringMap emptyClone() {
        return new TypeSafeStringMap(UseEnumeratedValues, StrongEnumerationCheck);
    }

    @Override
    public TypeSafeStringMap clone() {
        TypeSafeStringMap ret = emptyClone();
        ret.Me = (HashMap<String, Object>) Me.clone();
        return ret;
    }

    /**
     * Add tag name and corresponding type information. type information will be specified via the type argument:
     * <ul>
     *     <li>type == null: tag is of type String.</li>
     *     <li>type == String.class: tag is of type Datetime, a String representing date and time in a format that can
     *     be parsed with the SimpleDateFormat "yyyy-MM-dd'T'HH:mm:ss.SSSXXX".</li>
     *     <li>type == Boolean.class: tag is of type Boolean, a value of "true" or "false".</li>
     *     <li>type == Integer.class: tag is of type Numeric, a 32-bit integer value.</li>
     *     <li>type == Long.class: tag is of type Currency, a 64-bit integer value which represents a decimal value with
     *     implicit 4 decimals. A value of 1234567 represents a Currency value of 123.4567.</li>
     *     <li>type instanceof int[]: tag is of type Enumerated and type is an array that contains all documented valid
     *     values.</li>
     *     <li>type instanceof java.lang.reflect.Field[]: tag is of type Enumerated and type is an array of Field
     *     objects which correspond to the ElectronicValueRWConst constants allowed for that tag.</li>
     * </ul>
     * @param key   Name of tag with a length &gt; 0.
     * @param type  Object representing the type of the tag.
     * @throws RuntimeException if type is invalid or invalid Enumerator constants have been specified.
     */
    public static void addTagType(String key, Object type) {
        if (key.length() > 0) {
            if (type != null) {
                if (type instanceof Field[]) {
                    Field[] constants = (Field[])type;
                    SIVals[] validvalues = new SIVals[constants.length];
                    for (int i = 0; i < constants.length; i++) {
                        try {
                            validvalues[i] = new SIVals(constants[i].getName(), constants[i].getInt(null));
                        } catch (Exception e) {
                            String msg = e.getClass().getSimpleName() + (e.getMessage() == null ? "" : e.getMessage());
                            throw new RuntimeException(msg);
                        }
                    }
                    type = validvalues;
                } else if (!(type instanceof int[] || type == String.class || type == Boolean.class || type == Integer.class || type == Long.class))
                    throw new RuntimeException("Bad type identifier: " + type.getClass().getSimpleName());
                Types.put(key, type);
            }
        }
    }

    /**
     * Retrieve the value of a given tag as String if the tag is of type String.
     * @param key Name of the tag.
     * @return  Tag value or null if not set.
     * @throws RuntimeException If the tag has not type String.
     */
    public String getString(String key) {
        Object o = Types.get(key);
        if (o != null)
            throw new RuntimeException("Invalid type for tag " + key + ": String");
        return (String) Me.get(o.toString());
    }

    /**
     * Retrieve the value of a given tag as Integer if the tag is of type Number.
     * @param key Name of the tag.
     * @return  Tag value or null if not set.
     * @throws RuntimeException If the tag has not type Number.
     */
    public Integer getNumber(String key) {
        Object o = Types.get(key);
        if (o != Integer.class)
            throw new RuntimeException("Invalid type for tag " + key + ": Number");
        return (Integer) Me.get(key);
    }

    /**
     * Retrieve the value of a given tag as Long if the tag is of type Currency.
     * @param key Name of the tag.
     * @return  Tag value or null if not set.
     * @throws RuntimeException If the tag has not type Currency.
     */
    public Long getCurrency(String key) {
        Object o = Types.get(key);
        if (o != Long.class)
            throw new RuntimeException("Invalid type for tag " + key + ": Currency");
        return (Long) Me.get(key);
    }

    /**
     * Retrieve the value of a given tag as String if the tag is of type Datetime.
     * @param key Name of the tag.
     * @return  Tag value or null if not set.
     * @throws RuntimeException If the tag is not of type Datetime.
     */
    public String getDatetime(String key) {
        Object o = Types.get(key);
        if (o != String.class)
            throw new RuntimeException("Invalid type for tag " + key + ": Datetime");
        return (String) Me.get(key);
    }

    /**
     * Retrieve the value of a given tag as Boolean if the tag is of type Boolean.
     * @param key Name of the tag.
     * @return  Tag value or null if not set.
     * @throws RuntimeException If the tag has not type Boolean.
     */
    public Boolean getBoolean(String key) {
        Object o = Types.get(key);
        if (o != Boolean.class)
            throw new RuntimeException("Invalid type for tag " + key + ": Boolean");
        return (Boolean) Me.get(key);
    }

    /**
     * Retrieve the value of a given tag as Integer if the tag is of type Enumerated.
     * @param key Name of the tag.
     * @return  Tag value or null if not set.
     * @throws RuntimeException If the tag has not type Enumerated.
     */
    public Integer getEnumerated(String key) {
        Object o = Types.get(key);
        if (!(o instanceof int[] || o instanceof Map))
            throw new RuntimeException("Invalid type for tag " + key + ": Enumerated");
        return (Integer) Me.get(key);
    }

    /**
     * Retrieve the value of a given tag as Object if the tag value has been set.
     * @param key Name of the tag.
     * @return  Tag value or null if not set.
     */
    public Object getObject(String key) {
        return Me.get(key);
    }

    /**
     * Sets the value of tag named by key to the value specified by value. If the specified tag is not of type String,
     * a NullPointerException will be thrown.
     * @param key   Tag name.
     * @param value Tag value.
     * @return  Previous value or null if tag values has not been set previously.
     * @throws RuntimeException if the tag is not of type String.
     */
    public String putString(String key, String value) {
        Object o = Types.get(key.toString());
        if (o != null)
            throw new RuntimeException("Invalid type for tag " + key + ": String");
        return (String)Me.put(key, value.toString());
    }

    /**
     * Sets the value of tag named by key to the value specified by value. If the specified tag is not of type Number,
     * a NullPointerException will be thrown.
     * @param key   Tag name.
     * @param value Tag value.
     * @return  Previous value or null if tag values has not been set previously.
     * @throws RuntimeException if the tag is not of type Number.
     */
    public Integer putNumber(String key, int value) {
        Object o = Types.get(key.toString());
        if (o != Integer.class)
            throw new RuntimeException("Invalid type for tag " + key + ": Number");
        return (Integer) Me.put(key, value);
    }

    /**
     * Sets the value of tag named by key to the value specified by value. If the specified tag is not of type Currency,
     * a NullPointerException will be thrown.
     * @param key   Tag name.
     * @param value Tag value.
     * @return  Previous value or null if tag values has not been set previously.
     * @throws RuntimeException if the tag is not of type Currency.
     */
    public Long putCurrency(String key, long value) {
        Object o = Types.get(key.toString());
        if (o != Long.class)
            throw new RuntimeException("Invalid type for tag " + key + ": Currency");
        return (Long) Me.put(key, value);
    }

    /**
     * Sets the value of tag named by key to the value specified by value. If the specified tag is not of type Datetime,
     * a NullPointerException will be thrown.
     * @param key   Tag name.
     * @param value Tag value. Must be a valid Datetime string
     * @return  Previous value or null if tag values has not been set previously.
     * @throws RuntimeException if the tag is not of type Datetime.
     * @throws ParseException if value has invalid format.
     */
    public String putDatetime(String key, String value) throws ParseException {
        Object o = Types.get(key.toString());
        if (o != String.class)
            throw new RuntimeException("Invalid type for tag " + key + ": Datetime");
        Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(value);
        return (String) Me.put(key, value.toString());
    }

    /**
     * Sets the value of tag named by key to the value specified by value. If the specified tag is not of type Boolean,
     * a NullPointerException will be thrown.
     * @param key   Tag name.
     * @param value Tag value.
     * @return  Previous value or null if tag values has not been set previously.
     * @throws RuntimeException if the tag has not type Boolean.
     */
    public Boolean putBoolean(String key, boolean value) {
        Object o = Types.get(key.toString());
        if (o != Boolean.class)
            throw new RuntimeException("Invalid type for tag " + key + ": Boolean");
        return (Boolean) Me.put(key, value);
    }

    /**
     * Sets the value of tag named by key to the value specified by value. If the specified tag is not of type Enumerated,
     * a NullPointerException will be thrown.
     * @param key   Tag name.
     * @param value Tag value.
     * @return  Previous value or null if tag values has not been set previously.
     * @throws RuntimeException if the tag is not of type Enumerated, InvalidParameterException if value is not a valid
     *                              enumeration value.
     */
    public Integer putEnumerated(String key, int value) {
        Object o = Types.get(key.toString());
        if (o instanceof int[]) {
            int[] valid = (int[]) o;
            for (int i = 0; i < valid.length; i++) {
                if (value == valid[i])
                    return (Integer) Me.put(key, value);
            }
        } else if (o instanceof SIVals[]) {
            SIVals[] valid = (SIVals[]) o;
            for (int i = 0; i < valid.length; i++) {
                if (value == valid[i].Value)
                    return (Integer) Me.put(key, value);
            }
        } else
            throw new RuntimeException("Invalid type for tag " + key + ": Enumerated");
        if (StrongEnumerationCheck)
            throw new InvalidParameterException("Invalid value for tag " + key + ": " + value);
        return (Integer) Me.put(key, value);
    }

    @Override
    public int size() {
        return Me.size();
    }

    @Override
    public boolean isEmpty() {
        return Me.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return Me.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    @Deprecated
    public String get(Object key) {
        if (((String)key).length() >= 0) {
            Object val = Me.get(key);
            return getString(key, val);
        }
        return null;
    }

    private String getString(Object key, Object val) {
        if (val == null)
            return null;
        else if (val instanceof String)     // Datetime or String
            return (String) val;
        else if (val instanceof Boolean)    // Boolean: "true" or "false"
            return Boolean.toString((Boolean) val);
        else if (val instanceof Long)       // Currency: Value wit 4 implicit decimals, converted with decimal point
            return new BigDecimal((Long) val).scaleByPowerOfTen(-4).stripTrailingZeros().toPlainString();
        else {                              // Number or Enumerated
            if (UseEnumeratedValues)
                return Integer.toString((Integer) val);
            Object type = Types.get(key);
            if (type == null)
                return Integer.toString((Integer) val);
            else if (type instanceof SIVals[]) { // Enumerated with named elements
                for (SIVals def : (SIVals[])type) {
                    if (def.Value == (Integer) val)
                        return def.Key;
                }
            }
            return Integer.toString((Integer) val);
        }
    }

    @Override
    @Deprecated
    public String put(String key, String value) {
        if (((String)key).length() >= 0) {
            String ret = get(key);
            Object type = Types.get(key);
            if (type == null || value == null) {        // String type
                if (value == null)
                    Me.remove(key);
                else
                    Me.put(key, value);
            } else {
                try {
                    if (type == String.class) {         // Datetime
                        Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(value);
                        Me.put(key, value);
                    } else if (type == Boolean.class) { // Boolen
                        value = value.toLowerCase();
                        boolean val;
                        if (!(val = value.equals("true")) && !value.equals("false"))
                            val = ((String)null).length() == 0; // Generate NullPointerException
                        Me.put(key, val);
                    } else if (type == Integer.class) { // Number
                        Me.put(key, Integer.parseInt(value));
                    } else if (type == Long.class) {    // Currency
                        Me.put(key, new BigDecimal(value).scaleByPowerOfTen(4).longValueExact());
                    } else if (type instanceof int[]) { // Enumerated, no symbols
                        int val = Integer.parseInt(value);
                        if (StrongEnumerationCheck) {
                            boolean valid = false;
                            for (int v : (int[]) type) {
                                if (valid = v == val)
                                    break;
                            }
                            if (!valid)
                                val = ((Integer)null).intValue(); // Generate NullPointerException
                        }
                        Me.put(key, val);
                    } else {    // SIVals[]              // Enumerated, symbols
                        Integer val = null;
                        if (UseEnumeratedValues) {
                            int v = Integer.parseInt(value);
                            if (StrongEnumerationCheck) {
                                for (SIVals fld : (SIVals[]) type) {
                                    if (fld.Value == v) {
                                        val = v;
                                        break;
                                    }
                                }
                            } else
                                val = v;
                        } else {
                            for (SIVals fld : (SIVals[]) type) {
                                if (value.equals(fld.Key)) {
                                    val = fld.Value;
                                    break;
                                }
                            }
                            if (!StrongEnumerationCheck && val == null)
                                val = Integer.parseInt(value);
                        }
                        Me.put(key, val.intValue());
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Bad format for tag " + key + ": " + value);
                }
            }
            return ret;
        }
        return null;
    }

    @Override
    public String remove(Object key) {
        Object val = Me.remove(key);
        return getString(key, val);
    }

    @Override
    @Deprecated
    public void putAll(Map<? extends String, ? extends String> m) {
        for (Entry<? extends String, ? extends String> entry : m.entrySet())
            put(entry.getKey(), entry.getValue());
    }

    @Override
    public void clear() {
        Me.clear();
    }

    @Override
    public Set<String> keySet() {
        return Me.keySet();
    }

    @Override
    @Deprecated
    public Collection<String> values() {
        ArrayList<String> ret = new ArrayList<>(Me.size());
        for (Entry<String,Object> entry : Me.entrySet()) {
            ret.add(getString(entry.getKey(), entry.getValue()));
        }
        return ret;
    }

    /**
     * Retrieve a collection that contains all values present in the map, where the values will be stored in their generic
     * type.
     * @return The collection.
     */
    public Collection<Object> typedValues() {
        return Me.values();
    }

    @Override
    @Deprecated
    public Set<Entry<String, String>> entrySet() {
        Set<Entry<String, String>> ret = new HashSet<Entry<String,String>>(Me.size());
        for (Entry<String,Object> source : Me.entrySet())
            ret.add(new TSSEntry(source.getKey(), getString(source.getKey(), source.getValue())));
        return ret;
    }

    /**
     * Retrieves a set of all key / value pairs present in the map, where the values will be stored in their generic type.
     * @return The set.
     */
    public Set<Entry<String,Object>> typedEntrySet() {
        return Me.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Map ) ? entrySet().equals(((Map)o).entrySet()) : false;
    }

    @Override
    public int hashCode() {
        return Me.hashCode() + Types.hashCode();
    }
}
