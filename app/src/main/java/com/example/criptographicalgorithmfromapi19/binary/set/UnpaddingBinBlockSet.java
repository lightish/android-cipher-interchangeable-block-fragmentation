package com.example.criptographicalgorithmfromapi19.binary.set;

import com.example.criptographicalgorithmfromapi19.binary.ByteHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class UnpaddingBinBlockSet extends BinBlockSet {
    protected byte[] thisBlock;
    protected byte[] nextBlock;
    protected final int bytesTotal;

    public UnpaddingBinBlockSet(InputStream inputStream) throws IOException {
        super(inputStream);

        bytesTotal = evalBytesTotal();
        readNextBlock();
    }

    @Override
    public boolean next() throws IOException {
        thisBlock = ByteHelper.toBitArray(nextBlock);
        int bytesRead = readNextBlock();

        return bytesRead == BLOCK_BYTE_LENGTH;
    }

    private int readNextBlock() throws IOException {
        byte[] bytes = new byte[BLOCK_BYTE_LENGTH];
        int bytesRead = stream.read(bytes, 0, BLOCK_BYTE_LENGTH);
        if (bytesRead > 0) {
            nextBlock = bytes;
        }
        return bytesRead;
    }

    @Override
    public byte[] getBlock() {
        return thisBlock;
    }

    @Override
    public int getBytesTotal() {
        return bytesTotal;
    }

    private int evalBytesTotal() throws IOException {
        int estimateBytes = stream.available();
        //don't include extension
        estimateBytes -= estimateBytes % BLOCK_BYTE_LENGTH;
        return estimateBytes;
    }

    public byte[] getLast() {
        System.out.println("Last block: " + thisBlock);
        return thisBlock;
    }

    public byte[] getRemainder() {
        trimRemainder();
        return nextBlock;
    }

    private void trimRemainder() {
        int lastNonZero = 0;
        for (int i = 0; i < nextBlock.length; i++) {
            if (nextBlock[i] != 0)
                lastNonZero = i;
        }
        nextBlock = Arrays.copyOfRange(nextBlock, 0, lastNonZero +1);
    }
}
