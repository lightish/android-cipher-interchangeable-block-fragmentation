package com.example.criptographicalgorithmfromapi19.binary.set;
import java.io.IOException;
import java.io.InputStream;
public abstract class BinBlockSet {
    protected static final int BLOCK_BYTE_LENGTH = 30;
    protected static final int BLOCK_BIN_LENGTH = 240;
    protected static final int BYTES_SAFE_TO_WRITE = 29;
    protected static final int BITS_SAFE_TO_WRITE = 232;
    protected static final int PAD_INFO_LENGTH = 8;
    protected InputStream stream;
    BinBlockSet(InputStream inputStream) {
        stream = inputStream;
    }
    public abstract boolean next() throws IOException;
    public abstract byte[] getBlock();
    public abstract int getBytesTotal() throws IOException;
}
