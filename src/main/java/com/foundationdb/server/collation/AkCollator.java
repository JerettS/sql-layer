/**
 * Copyright (C) 2009-2013 FoundationDB, LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.foundationdb.server.collation;

import com.foundationdb.server.PersistitKeyValueSource;
import com.foundationdb.server.types.value.ValueSource;
import com.persistit.Key;

public abstract class AkCollator {

    public static String getString(ValueSource valueSource, AkCollator collator) {
        if (valueSource.isNull())
            return null;
        else if (valueSource.canGetRawValue())
            return valueSource.getString();
        else if (valueSource.hasCacheValue()) {
            Object obj = valueSource.getObject();
            if (obj instanceof byte[]) {
                // Good enough for printing or encoding for hash comparison.
                // TODO: See comment on CStringKeyCoder.decodeKeySegment().
                byte[] bytes = (byte[])obj;
                assert (collator != null) : "encoded as bytes without collator";
                return collator.decodeSortKeyBytes(bytes, 0, bytes.length);
            }
            return (String) obj;
        }
        throw new AssertionError("no value");
    }

    private final String collatorName;

    private final String collatorScheme;

    private final int collationId;

    protected AkCollator(final String collatorName, final String collatorScheme, final int collationId) {
        this.collatorName = collatorName;
        this.collatorScheme = collatorScheme;
        this.collationId = collationId;
    }

    /**
     * @return true if this collator is capable of precisely recovering the key
     *         string from a key segment.
     */
    abstract public boolean isRecoverable();

    /**
     * Append a String to a Key
     * 
     * @param key
     * @param value
     */
    abstract public void append(Key key, String value);

    /**
     * Decode a String from a Key segment
     * 
     * @param key
     * @return the decoded String
     * @throws UnsupportedOperationException
     *             for collations in which a precise transformation is
     *             impossible. See {@link #isRecoverable()}.
     */
    abstract public String decode(Key key);

    /**
     * Compare two string values: Comparable<ValueSource>
     */
    final public int compare(ValueSource value1, ValueSource value2) {
        boolean persistit1 = value1 instanceof PersistitKeyValueSource;
        boolean persistit2 = value2 instanceof PersistitKeyValueSource;
        if (persistit1 && persistit2) {
            return ((PersistitKeyValueSource) value1).compare((PersistitKeyValueSource) value2);
        } else if (persistit1) {
            return ((PersistitKeyValueSource) value1).compare(this, getString(value2, this));
        } else if (persistit2) {
            return -((PersistitKeyValueSource) value2).compare(this, getString(value1, this));
        } else {
            String sValue1 = getString(value1, this);
            String sValue2 = getString(value2, this);
            if(sValue1 == null || sValue2 == null){
                if(sValue1 == sValue2)
                    return 0;
                return (sValue1 == null)? -1: 1;
            }
            return compare(getString(value1, this), getString(value2, this));
        }
    }

    /**
     * Compare two string objects: Comparable<String>
     */
    abstract public int compare(String string1, String string2);

    /**
     * @return whether the underlying collation scheme is case-sensitive
     */
    abstract public boolean isCaseSensitive();

    /**
     * Compute the hash of a String based on the sort keys produced by the
     * underlying collator. For example, if the collator is case-insensitive
     * then hash("ABC") == hash("abc").
     * 
     * @param string
     *            the String
     * @return the computed hash value
     * @throws NullPointerException
     *             if string is null
     */
    abstract public int hashCode(final String string);

    @Override
    public String toString() {
        return collatorName + "(" + collatorScheme + ")";
    }

    public int getCollationId() {
        return collationId;
    }

    public String getName() {
        return collatorName;
    }

    public String getScheme() {
        return collatorScheme;
    }

    /**
     * Construct the sort key bytes for the given String value. This method is
     * intended for use only by {@link CStringKeyCoder} and is therefore
     * package-private.
     * 
     * @param value
     *            the String
     * @return sort key bytes
     */
    abstract byte[] encodeSortKeyBytes(String value);

    /**
     * Recover a String value which may be approximate. For example The string
     * may be spelled with incorrect case and not correctly represent some
     * characters. This method is intended for use only by
     * {@link CStringKeyCoder} and is therefore package-private.
     * 
     * @param bytes
     *            the sort key bytes
     * @param index
     *            index within array of first sorted key byte
     * @param length
     *            number of sorted key bytes
     * @return the decoded String value
     * @throws UnsupportedOperationException
     *             if unable to decode sort keys
     */
    abstract String decodeSortKeyBytes(byte[] bytes, int index, int length);
}
