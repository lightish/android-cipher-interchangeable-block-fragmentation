package com.example.criptographicalgorithmfromapi19.binary.set;

import com.example.criptographicalgorithmfromapi19.binary.ByteHelper;
import com.example.criptographicalgorithmfromapi19.util.AlgUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PaddingBinBlockSet extends BinBlockSet {
    protected byte[] block;
    protected final int bytesTotal;

    public PaddingBinBlockSet(InputStream inputStream) throws IOException {
        super(inputStream);

        bytesTotal = evalBytesTotal();
    }

    @Override
    public boolean next() throws IOException {
        byte[] bytes = new byte[BLOCK_BYTE_LENGTH];
        
        int bytesRead = stream.read(bytes, 0, BLOCK_BYTE_LENGTH);

        if (bytesRead < 1) {
            return false;
        }

        if (bytesRead == BLOCK_BYTE_LENGTH) {
            block = ByteHelper.toBitArray(bytes);
        } else if (bytesRead <= BYTES_SAFE_TO_WRITE) {
            int paddedBits = fillLastBlock(bytes, bytesRead, BITS_SAFE_TO_WRITE);
            AlgUtils.fillWithBinary(block, BITS_SAFE_TO_WRITE, paddedBits, PAD_INFO_LENGTH);
        } else {
            int paddedBits = fillLastBlock(bytes, bytesRead, BLOCK_BIN_LENGTH);
            byte[] lastBlock = new byte[BLOCK_BIN_LENGTH];
            AlgUtils.placeRandomBits(lastBlock, 0, BITS_SAFE_TO_WRITE);
            paddedBits += BLOCK_BIN_LENGTH;
            AlgUtils.fillWithBinary(lastBlock, BITS_SAFE_TO_WRITE, paddedBits, PAD_INFO_LENGTH);
            stream = new ByteArrayInputStream(lastBlock);
        }

        return true;
    }

    @Override
    public byte[] getBlock() {
        if (block == null)
            throw new IllegalStateException(
                    "Method 'next' has not been called");
        return block;
    }

    @Override
    public int getBytesTotal() {
        return bytesTotal;
    }

    private int evalBytesTotal() throws IOException {
        int estimateBytes = stream.available();
        //estimate padding
        int remainder = estimateBytes % BLOCK_BYTE_LENGTH;
        if (remainder == 0) {
            estimateBytes += BLOCK_BYTE_LENGTH;
        } else {
            estimateBytes += BLOCK_BYTE_LENGTH - remainder;
        }
        return estimateBytes;
    }

    protected void fillRemainedBytes(byte[] bytes, int remainder, int paddingTo) {
        block = ByteHelper.toBitBlock(bytes, BLOCK_BIN_LENGTH);
        AlgUtils.placeRandomBits(block, remainder, paddingTo);
    }

    protected int fillLastBlock(byte[] bytes, int bytesRead, int bound) {
        int remainder = bytesRead * 8;
        fillRemainedBytes(bytes, remainder, bound);
        return BLOCK_BIN_LENGTH - remainder;
    }
}