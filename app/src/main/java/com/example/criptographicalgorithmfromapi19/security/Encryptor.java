package com.example.criptographicalgorithmfromapi19.security;

import com.example.criptographicalgorithmfromapi19.binary.ByteHelper;
import com.example.criptographicalgorithmfromapi19.binary.set.PaddingBinBlockSet;
import com.example.criptographicalgorithmfromapi19.util.AlgUtils;
import com.example.criptographicalgorithmfromapi19.binary.set.BinBlockSet;
import com.example.criptographicalgorithmfromapi19.util.ProgressTracker;
import com.example.criptographicalgorithmfromapi19.ui.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;


public class Encryptor {
    private final static Logger logger = Logger.getLogger("ENCRYPT LOGGER -->>");

    @Deprecated
    public void encrypt(InputStream source, byte[] keyBin, int rounds, File destinationFolder,
                        String fileName, String fileExtension,
                        MainActivity mainActivity) throws IOException {
        if (source != null) {
            File file = new File(destinationFolder, fileName);
            Queue<Integer> progressCallbacks = new LinkedList<>();
            Runnable encryptionRunnable = new EncryptionRunnable(file, keyBin, rounds, fileExtension,
                    source, progressCallbacks);
            Runnable progressTracker = new ProgressTracker(progressCallbacks, mainActivity,
                    "Encrypting file...", "File encrypted!");

            startEncryption(encryptionRunnable, progressTracker);
        }
    }

    public void encrypt(InputStream source, byte[] keyBin, int rounds, boolean chained,
                        int encryptionDensity, File destinationFolder,
                        String fileName, String fileExtension,
                        MainActivity mainActivity) throws IOException {
        if (source != null) {
            File file = new File(destinationFolder, fileName);
            Queue<Integer> progressCallbacks = new LinkedList<>();
            Runnable progressTracker = new ProgressTracker(progressCallbacks, mainActivity,
                    "Encrypting file...", "File encrypted!");

            Runnable encryptionRunnable;
            if (chained) {
                encryptionRunnable = new ChainedEncryptionRunnable(file, keyBin, rounds,
                        encryptionDensity, fileExtension, source, progressCallbacks);
            }
            else {
                encryptionRunnable = new EncryptionRunnable(file, keyBin, rounds, fileExtension,
                        source, progressCallbacks);
            }

            startEncryption(encryptionRunnable, progressTracker);
        }
    }

    public void startEncryption(Runnable encryptionRunnable,
                         Runnable progressTracker) {
        Thread encryptionThread = new Thread(encryptionRunnable, "Encryption");
        Thread trackingThread = new Thread(progressTracker, "Progress Tracker");
        encryptionThread.start();
        trackingThread.start();
    }

    private static class EncryptionRunnable implements Runnable {
        private final InputStream source;
        private final File file;
        protected int rounds;
        protected byte[] keyBin;
        protected String fileExtension;
        protected Queue<Integer> progressCallbacks;
        protected BinBlockSet blockSet;
        protected double minProgress;

        EncryptionRunnable(File file, byte[] keyBin, int rounds,
                           String fileExtension, InputStream source,
                           Queue<Integer> progressCallbacks) throws IOException {
            this.file = file;
            this.rounds = rounds;
            this.keyBin = keyBin;
            this.fileExtension = fileExtension;
            this.progressCallbacks = progressCallbacks;
            this.source = source;
            this.blockSet = new PaddingBinBlockSet(source);
        }

        @Override
        public void run() {
            Thread.currentThread().setPriority(7);

            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                double progressIncrementation;
                progressIncrementation = AlgUtils.estimateProgressIncrPoint(blockSet.getBytesTotal());
                minProgress = 0;
                writeEncryptedFile(fileOut, progressIncrementation);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    source.close();
                } catch (IOException e) {
                    logger.severe("Error while encrypting: failed to close InputStream");
                    e.printStackTrace();
                }
                progressCallbacks.add(-1);
            }
        }

        protected void writeEncryptedFile(FileOutputStream fileOut,
                                          double progressIncrementation) throws IOException {
            while (blockSet.next()) {
                writeEncryptedBlock(blockSet.getBlock(), fileOut, progressIncrementation);
            }
            fileOut.write(fileExtension.getBytes());
        }

        protected byte[] writeEncryptedBlock(byte[] block,
                                             FileOutputStream fileOut,
                                             double progressIncrementation) throws IOException {
            byte[] encryptedBlock = encryptBlock(block, keyBin, rounds);
            writeBinBlock(encryptedBlock, fileOut, progressIncrementation);
            return encryptedBlock;
        }

        protected void writeBinBlock(byte[] block,
                                     FileOutputStream fileOut,
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

        protected byte[] encryptBlock(byte[] block, byte[] key, int rounds) {
            byte[] left = new byte[AlgUtils.MAIN_BLOCKS_WIDTH];
            byte[] right = new byte[AlgUtils.MAIN_BLOCKS_WIDTH];

            AlgUtils.divideIntoBlocks(block, left, right);
            for (; rounds != 0; rounds--){
                left = encodeLeft(left, key);
                right = encodeRight(right);

                byte[] oldLeft = left;
                left = AlgUtils.xorArrays(left, right);
                right = oldLeft;
            }

            return AlgUtils.concatBlocks(left, right);
        }

        private byte[] encodeLeft(byte[] left, byte[] key) {
            byte[] newLeft = AlgUtils.performSBlockPermutation(left, AlgUtils.S1, 4);
            return AlgUtils.xorArrays(newLeft, key);
        }

        private byte[] encodeRight(byte[] right) {
            byte[] newRight = AlgUtils.performSBlockPermutation(right, AlgUtils.S2, 5);
            return AlgUtils.mapOnP(newRight);
        }

    }   // EncryptionRunnable inner class end

    private static class ChainedEncryptionRunnable extends EncryptionRunnable {
        protected int encryptionDensity;

        /**
         * @param encryptionDensity the n in "encrypt every nth block"
         */
        ChainedEncryptionRunnable(File file, byte[] keyBin, int rounds, int encryptionDensity,
                                  String fileExtension, InputStream source,
                                  Queue<Integer> progressCallbacks) throws IOException {
            super(file, keyBin, rounds, fileExtension, source, progressCallbacks);
            this.encryptionDensity = encryptionDensity;
        }

        @Override
        protected void writeEncryptedFile(FileOutputStream fileOut,
                                          double progressIncrementation) throws IOException {
            byte[] prevBlock;
            int blocksUntilEncryption;

            // encrypt the first block
            if (blockSet.next()) {
                prevBlock = writeEncryptedBlock(blockSet.getBlock(), fileOut, progressIncrementation);
                blocksUntilEncryption = encryptionDensity;

                // encrypt the rest
                while (blockSet.next()) {
                    byte[] block = AlgUtils.xorArrays(blockSet.getBlock(), prevBlock);
                    if (blocksUntilEncryption == 1) {
                        prevBlock = writeEncryptedBlock(block, fileOut, progressIncrementation);
                        blocksUntilEncryption = encryptionDensity;
                    } else {
                        writeBinBlock(block, fileOut, progressIncrementation);
                        blocksUntilEncryption--;
                    }
                }
            }
            fileOut.write(fileExtension.getBytes());
        }
    }   // ChainedEncryptionRunnable inner class end

}   // Encryptor outer class end