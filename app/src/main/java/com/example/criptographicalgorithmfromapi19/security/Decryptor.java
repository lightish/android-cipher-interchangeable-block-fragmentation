package com.example.criptographicalgorithmfromapi19.security;

import com.example.criptographicalgorithmfromapi19.binary.ByteHelper;
import com.example.criptographicalgorithmfromapi19.binary.set.UnpaddingBinBlockSet;
import com.example.criptographicalgorithmfromapi19.ui.MainActivity;
import com.example.criptographicalgorithmfromapi19.util.AlgUtils;
import com.example.criptographicalgorithmfromapi19.util.ProgressTracker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

public class Decryptor {

    @Deprecated
    public void decrypt(InputStream source, byte[] keyBin, int rounds,
                        File destinationFolder, String fileName,
                        MainActivity mainActivity) throws IOException {
        if (source != null) {
            Queue<Integer> progressCallbacks = new LinkedList<>();

            Runnable decryptionRunnable = new DecryptionRunnable(source, rounds, keyBin,
                    destinationFolder, fileName, progressCallbacks);
            Runnable progressTracker = new ProgressTracker(progressCallbacks, mainActivity,
                    "Decrypting file...", "File decrypted!");

            startDecryption(decryptionRunnable, progressTracker);
        }
    }

    public void decrypt(InputStream source, byte[] keyBin, int rounds, boolean chained,
                        int encryptionDensity, File destinationFolder, String fileName,
                        MainActivity mainActivity) throws IOException {
        if (source != null) {
            Queue<Integer> progressCallbacks = new LinkedList<>();
            Runnable progressTracker = new ProgressTracker(progressCallbacks, mainActivity,
                    "Decrypting file...", "File decrypted!");

            Runnable decryptionRunnable;
            if (chained) {
                decryptionRunnable = new ChainedDecryptionRunnable(source, rounds, keyBin,
                        encryptionDensity, destinationFolder, fileName, progressCallbacks);
            } else {
                decryptionRunnable = new DecryptionRunnable(source, rounds, keyBin,
                        destinationFolder, fileName, progressCallbacks);
            }

            startDecryption(decryptionRunnable, progressTracker);
        }
    }

    public void startDecryption(Runnable decryptionRunnable,
                                Runnable progressTracker) {
        Thread decryptionThread = new Thread(decryptionRunnable, "Encryption");
        Thread trackingThread = new Thread(progressTracker, "Progress Tracker");
        decryptionThread.start();
        trackingThread.start();
    }

    private static class DecryptionRunnable implements Runnable {
        protected Logger logger = Logger.getLogger("DECRYPT LOGGER -->>");
        protected InputStream source;
        protected int rounds;
        protected byte[] keyBin;
        protected File destinationFolder;
        protected String fileName;
        protected Queue<Integer> progressCallbacks;
        protected double minProgress;
        protected UnpaddingBinBlockSet blockSet;

        public DecryptionRunnable(InputStream source, int rounds, byte[] keyBin,
                                  File destinationFolder, String fileName,
                                  Queue<Integer> progressCallbacks) throws IOException {
            this.source = source;
            this.rounds = rounds;
            this.keyBin = keyBin;
            this.destinationFolder = destinationFolder;
            this.fileName = fileName;
            this.progressCallbacks = progressCallbacks;
            this.blockSet = new UnpaddingBinBlockSet(source);
        }

        @Override
        public void run() {
            Thread.currentThread().setPriority(7);
            File file = new File(destinationFolder, fileName + ".tmp");

            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                double progressIncrementation;
                progressIncrementation = AlgUtils.estimateProgressIncrPoint(blockSet.getBytesTotal());
                minProgress = 0;
                writeDecryptedFile(fileOut, progressIncrementation);
                finalizeFileName(file);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                try {
                    source.close();

                } catch (IOException e) {
                    logger.info("Error while encrypting: failed to close InputStream");
                    e.printStackTrace();
                }
                progressCallbacks.add(-1);
            }
        }

        protected void writeDecryptedFile(FileOutputStream fileOut,
                                          double progressIncrementation) throws IOException {
            while (blockSet.next()) {
                writeDecryptedBlock(blockSet.getBlock(), fileOut, progressIncrementation);
            }
            byte[] lastBlock = decryptLastBlock(blockSet.getLast());
            writeBinBlock(lastBlock, fileOut, progressIncrementation);
        }

        protected void finalizeFileName(File file) {
            String extension = new String(blockSet.getRemainder());
            File newFile = new File(destinationFolder, fileName + "." + extension);
            boolean fileRenamed = file.renameTo(newFile);
            if (!fileRenamed) {
                logger.warning("Couldn't rename the file to " + newFile.getName() +
                        "\nNew absolute path requested: " + newFile.getAbsolutePath());
            }
            logger.info(file.getAbsolutePath() + " is decrypted and saved");
        }

        protected void writeDecryptedBlock(byte[] block,
                                             FileOutputStream fileOut,
                                             double progressIncrementation) throws IOException {
            byte[] decryptedBlock = decryptBlock(block, keyBin, rounds);
            writeBinBlock(decryptedBlock, fileOut, progressIncrementation);
        }

        protected void writeBinBlock(byte[] block,
                                     OutputStream fileOut,
                                     double progressIncrementation) throws IOException {
            byte[] bytesToWrite = ByteHelper.toByteArray(block);
            fileOut.write(bytesToWrite);
            reportProgress(progressIncrementation);
        }

        protected void reportProgress(double progressIncrementation) {
            minProgress += progressIncrementation;
            if (minProgress >= 1) {
                int progressToReport = (int) minProgress;
                progressCallbacks.add(progressToReport);
                minProgress -= progressToReport;
            }
        }

        protected byte[] decryptBlock(byte[] block, byte[] key, int rounds) {
            byte[] left = new byte[AlgUtils.MAIN_BLOCKS_WIDTH];
            byte[] right = new byte[AlgUtils.MAIN_BLOCKS_WIDTH];

            AlgUtils.divideIntoBlocks(block, left, right);
            logger.info("start decrypting next block");
            for (; rounds != 0; rounds--){
                byte[] oldLeft = right;
                right = AlgUtils.xorArrays(left, right);
                left = oldLeft;

                left = decodeLeft(left, key);
                right = decodeRight(right);
            }

            return AlgUtils.concatBlocks(left, right);
        }

        private byte[] decodeLeft(byte[] left, byte[] key) {
            byte[] newLeft = AlgUtils.xorArrays(left, key);
            return AlgUtils.performReverseSBlockPermutation(newLeft, AlgUtils.S1, 4);
        }

        private byte[] decodeRight(byte[] right) {
            byte[] newRight = AlgUtils.reverseMapFromP(right);
            return AlgUtils.performReverseSBlockPermutation(newRight, AlgUtils.S2, 5);
        }

        protected byte[] decryptLastBlock(byte[] block) {
            byte[] decryptedData = decryptBlock(block, keyBin, rounds);
            return unpadBlock(decryptedData, 8);
        }

        byte[] unpadBlock(byte[] block, int paddingDigits) {
            int paddingBits = AlgUtils.binArray2Decimal(block, block.length - paddingDigits, paddingDigits);
            byte[] unpaddedBlock = new byte[block.length - paddingBits];
            System.arraycopy(block, 0, unpaddedBlock, 0, unpaddedBlock.length);
            return unpaddedBlock;
        }
    }   // DecryptionRunnable inner class end

    private static class ChainedDecryptionRunnable extends DecryptionRunnable {
        protected int encryptionDensity;

        ChainedDecryptionRunnable(InputStream source, int rounds, byte[] keyBin, int encryptionDensity,
                                  File destinationFolder, String fileName,
                                  Queue<Integer> progressCallbacks) throws IOException {
            super(source, rounds, keyBin, destinationFolder, fileName, progressCallbacks);
            this.encryptionDensity = encryptionDensity;
        }

        @Override
        protected void writeDecryptedFile(FileOutputStream fileOut,
                                          double progressIncrementation) throws IOException {
            byte[] prevBlock;
            int blocksUntilEncrypted = encryptionDensity;

            if (blockSet.next()) {
                byte[] encryptedBlock = blockSet.getBlock();
                writeDecryptedBlock(encryptedBlock, fileOut, progressIncrementation);
                prevBlock = encryptedBlock;

                while (blockSet.next()) {
                    byte[] boundBlock;

                    encryptedBlock = blockSet.getBlock();
                    if (blocksUntilEncrypted == 1) {
                        boundBlock = decryptBlock(encryptedBlock, keyBin, rounds);
                        blocksUntilEncrypted = encryptionDensity;
                    } else {
                        boundBlock = encryptedBlock;
                        blocksUntilEncrypted--;
                    }

                    byte[] plaintextBlock = AlgUtils.xorArrays(boundBlock, prevBlock);
                    writeBinBlock(plaintextBlock, fileOut, progressIncrementation);
                    prevBlock = encryptedBlock;
                }
                byte[] lastBlock = blockSet.getLast();
                byte[] boundBlock;
                if (blocksUntilEncrypted == 1) {
                    boundBlock = decryptBlock(lastBlock, keyBin, rounds);
                } else {
                    boundBlock = lastBlock;
                }
                byte[] plaintextPaddedBlock = AlgUtils.xorArrays(boundBlock, prevBlock);
                byte[] plaintextBlock = unpadBlock(plaintextPaddedBlock, 8);
                writeBinBlock(plaintextBlock, fileOut, progressIncrementation);
            }
        }
    }   // ChainedDecryptionRunnable inner class end

}   // Decryptor class end
