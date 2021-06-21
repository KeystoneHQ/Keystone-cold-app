package com.keystone.coinlib.coins.polkadot.scale;

import com.keystone.coinlib.coins.polkadot.scale.writer.*;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Optional;

public class ScaleCodecWriter implements Closeable {

    public static final CompactUIntWriter COMPACT_UINT = new CompactUIntWriter();
    public static final CompactBigIntWriter COMPACT_BIGINT = new CompactBigIntWriter();
    public static final UInt16Writer UINT16 = new UInt16Writer();
    public static final UInt32Writer UINT32 = new UInt32Writer();
    public static final UInt128Writer UINT128 = new UInt128Writer();
    public static final UInt64Writer UINT64 = new UInt64Writer();
    public static final ULong32Writer ULONG32 = new ULong32Writer();
    public static final BoolWriter BOOL = new BoolWriter();
    public static final BoolOptionalWriter BOOL_OPT = new BoolOptionalWriter();

    private final ByteArrayOutputStream out;

    public ScaleCodecWriter(ByteArrayOutputStream out) {
        this.out = out;
    }
    
    public byte[] toByteArray() {
        return out.toByteArray();
    }

    public void writeBIntCompact(BigInteger value) throws IOException {
        COMPACT_BIGINT.write(this, value);
    }

    public void writeLIntCompact(Long value) throws IOException {
        this.writeBIntCompact(BigInteger.valueOf(value));
    }

    public void writeUint256(byte[] value) throws IOException {
        if (value.length != 32) {
            throw new IllegalArgumentException("Value must be 32 byte array");
        }
        writeByteArray(value);
    }

    public void writeByteArray(byte[] value) throws IOException {
        out.write(value, 0, value.length);
    }

    public void writeAsList(byte[] value) throws IOException {
        writeCompact(value.length);
        out.write(value, 0, value.length);
    }

    /**
     * Write the byte into output stream as-is directly, the input is supposed to be already encoded
     *
     * @param b byte to write
     * @throws IOException if failed to write
     */
    public void directWrite(int b) throws IOException {
        out.write(b);
    }

    /**
     * Write the bytes into output stream as-is directly, the input is supposed to be already encoded
     *
     * @param b bytes to write
     * @param off offset
     * @param len length
     * @throws IOException if failed to write
     */
    public void directWrite(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    public <T> void write(ScaleWriter<T> writer, T value) throws IOException {
        writer.write(this, value);
    }

    public void writeByte(int value) throws IOException {
        directWrite(value);
    }

    public void writeByte(byte value) throws IOException {
        directWrite(value);
    }

    public void writeUint16(int value) throws IOException {
        UINT16.write(this, value);
    }

    public void writeUint32(int value) throws IOException {
        UINT32.write(this, value);
    }

    public void writeUint32(long value) throws IOException {
        ULONG32.write(this, value);
    }

    public void writeUint128(BigInteger value) throws IOException {
        UINT128.write(this, value);
    }

    public void writeUint64(BigInteger value) throws IOException {
        UINT64.write(this, value);
    }

    public void writeCompact(int value) throws IOException {
        COMPACT_UINT.write(this, value);
    }

    public void writeBoolean(boolean flag) throws IOException {
        BOOL.write(this, flag);
    }

    @SuppressWarnings("unchecked")
    public <T> void writeOptional(ScaleWriter<T> writer, T value) throws IOException {
        if (writer instanceof BoolOptionalWriter || writer instanceof BoolWriter) {
            BOOL_OPT.write(this, (Optional<Boolean>) Optional.ofNullable(value));
        } else {
            if (value == null) {
                BOOL.write(this, false);
            } else {
                BOOL.write(this, true);
                writer.write(this, value);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> void writeOptional(ScaleWriter<T> writer, Optional<T> value) throws IOException {
        if (writer instanceof BoolOptionalWriter || writer instanceof BoolWriter) {
            BOOL_OPT.write(this, (Optional<Boolean>) value);
        } else {
            if (!value.isPresent()) {
                BOOL.write(this, false);
            } else {
                BOOL.write(this, true);
                writer.write(this, value.get());
            }
        }
    }
}
