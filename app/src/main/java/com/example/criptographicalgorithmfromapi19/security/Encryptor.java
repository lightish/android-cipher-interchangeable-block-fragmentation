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

    public void encrypt(InputStream source, byte[] keyBin, int rounds, File destinationFolder,
                        String fileName, String fileExtension,
                        MainActivity mainActivity) throws IOException {
        if (source != null) {
            File file = new File(destinationFolder, fileName);
            Queue<Integer> progressCallbacks = new LinkedList<>();

            Runnable encryptionRunnable =
                    new EncryptionRunnable(file, keyBin, rounds, fileExtension, source, progressCallbacks);
            Runnable progressTracker = new ProgressTracker(progressCallbacks, mainActivity,
                    "Encrypting file...", "File encrypted!");
            Thread encryptionThread = new Thread(encryptionRunnable, "Encryption");
            Thread trackingThread = new Thread(progressTracker, "Progress Tracker");
            encryptionThread.start();
            trackingThread.start();
        }
    }

    private static class EncryptionRunnable implements Runnable {
        private InputStream source;
        private File file;
        private int rounds;
        private byte[] keyBin;
        private String fileExtension;
        private Queue<Integer> progressCallbacks;
        private double progressIncrementation;
        private BinBlockSet blockSet;

        EncryptionRunnable(File file, byte[] keyBin, int rounds,
                                  String fileExtension, InputStream source,
                                  Queue<Integer> progressCallbacks) throws IOException {
            this.file = file;
            this.rounds = rounds;
            this.keyBin = keyBin;
            this.fileExtension = fileExtension;
            this.progressCallbacks = progressCallbacks;
            this.source = source;
            blockSet = new PaddingBinBlockSet(source);
            progressIncrementation = AlgUtils.estimateProgressIncrPoint(blockSet.getBytesTotal());
        }

        @Override
        public void run() {
            Thread.currentThread().setPriority(7);
            double minProgress = 0;
            byte[] block;

            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                while (blockSet.next()) {
                    block = blockSet.getBlock();
                    byte[] moreEncryptedData = encryptBlock(block, keyBin, rounds);
                    byte[] bytesToWrite = ByteHelper.toByteArray(moreEncryptedData);
                    fileOut.write(bytesToWrite);

                    minProgress += progressIncrementation;
                    if (minProgress >= 1) {
                        progressCallbacks.add((int) minProgress);
                        minProgress -= Math.floor(minProgress);
                    }
                }
                fileOut.write(fileExtension.getBytes());

            } catch (IOException e) {
                e.printStackTrace();

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

        private byte[] encryptBlock(byte[] block, byte[] key, int rounds) {
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

    }   // EncryptionThread inner class end

}   // Encryptor outer class end