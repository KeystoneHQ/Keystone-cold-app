package com.keystone.coinlib.coins.polkadot.scale.reader;

import com.keystone.coinlib.coins.polkadot.scale.ScaleCodecReader;
import com.keystone.coinlib.coins.polkadot.scale.ScaleReader;

/**
 * Read a Java enum value. The reader reads a one byte and returns a Enum value which Ordinal value is equal to it.
 * <br>
 * If you need to read an enumeration with assigned value, i.e. Rust style enum, you should use {@link UnionReader} instead.
 *
 * @param <T> type of Enum
 * @see UnionReader
 */
public class EnumReader<T extends Enum<?>> implements ScaleReader<T> {

    private final T[] values;

    /**
     * Define reader by specifying list of possible values. In most of the cases it would be:
     * <code>new EnumReader(MyEnum.values()</code>
     *
     * @param values list of enum values
     */
    public EnumReader(T[] values) {
        if (values == null) {
            throw new NullPointerException("List of enums is null");
        }
        if (values.length == 0) {
            throw new IllegalArgumentException("List of enums is empty");
        }
        this.values = values;
    }

    @Override
    public T read(ScaleCodecReader rdr) {
        int id = rdr.readUByte();
        for (T t: values) {
            if (t.ordinal() == id) {
                return t;
            }
        }
        throw new IllegalStateException("Unknown enum value: " + id);
    }
}
