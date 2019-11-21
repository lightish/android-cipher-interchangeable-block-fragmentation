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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

public class Decryptor {

    public void decrypt(InputStream source, byte[] keyBin, int rounds,
                        File destinationFolder, String fileName,
                        MainActivity mainActivity) {

        if (source != null) {
            Queue<Integer> progressCallbacks = new LinkedList<>();

            Runnable decryptionRunnable = new DecryptionRunnable(source, rounds, keyBin,
                    destinationFolder, fileName, progressCallbacks);
            Runnable progressTracker = new ProgressTracker(progressCallbacks, mainActivity,
                    "Decrypting file...", "File decrypted!");

            Thread decryptionThread = new Thread(decryptionRunnable, "Decryption");
            Thread trackingThread = new Thread(progressTracker, "Progress Tracker");
            decryptionThread.start();
            trackingThread.start();
        }
    }

    private static class DecryptionRunnable implements Runnable {
        protected Logger logger = Logger.getLogger("DECRYPT LOGGER -->>");
        private InputStream source;
        private int rounds;
        private byte[] keyBin;
        private File destinationFolder;
        private String fileName;
        private Queue<Integer> progressCallbacks;

        public DecryptionRunnable(InputStream source, int rounds, byte[] keyBin,
                                  File destinationFolder, String fileName,
                                  Queue<Integer> progressCallbacks) {
            this.source = source;
            this.rounds = rounds;
            this.keyBin = keyBin;
            this.destinationFolder = destinationFolder;
            this.fileName = fileName;
            this.progressCallbacks = progressCallbacks;
        }

        @Override
        public void run() {
            Thread.currentThread().setPriority(7);
            File file = new File(destinationFolder, fileName + ".tmp");

            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                byte[] block;
                byte[] decryptedData;
                byte[] bytesToWrite;
                UnpaddingBinBlockSet blockSet = new UnpaddingBinBlockSet(source);
                double progressIncrementation =
                        AlgUtils.estimateProgressIncrPoint(blockSet.getBytesTotal());
                double minProgress = 0;

                while (blockSet.next()) {
                    block = blockSet.getBlock();
                    decryptedData = decryptBlock(block, keyBin, rounds);
                    bytesToWrite = ByteHelper.toByteArray(decryptedData);
                    fileOut.write(bytesToWrite);

                    minProgress += progressIncrementation;
                    if (minProgress >= 1) {
                        progressCallbacks.add((int) minProgress);
                        minProgress -= Math.floor(minProgress);
                    }
                }
                decryptLastBlock(blockSet, keyBin, rounds, fileOut);

                String extension = new String(blockSet.getRemainder());
                logger.info("extension bytes: " + Arrays.toString(blockSet.getRemainder()));
                logger.info("extension actual: ." + extension);
                File newFile = new File(destinationFolder, fileName + "." + extension);
                logger.info("file renamed: " + file.renameTo(newFile));
                logger.info(newFile.getAbsolutePath() + " is decrypted and saved");
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

        private byte[] decryptBlock(byte[] block, byte[] key, int rounds) {
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

        private void decryptLastBlock(UnpaddingBinBlockSet blockSet,
                                      byte[] key, int rounds,
                                      OutputStream fileOut) throws IOException {

            byte[] block = blockSet.getLast();
            byte[] decryptedData = decryptBlock(block, key, rounds);
            decryptedData = unpadBlock(decryptedData, 8);
            byte[] bytesToWrite = ByteHelper.toByteArray(decryptedData);
            fileOut.write(bytesToWrite);
        }

        byte[] unpadBlock(byte[] block, int paddingDigits) {
            int paddingBits = AlgUtils.binArray2Decimal(block, block.length - paddingDigits, paddingDigits);
            byte[] unpaddedBlock = new byte[block.length - paddingBits];
            System.arraycopy(block, 0, unpaddedBlock, 0, unpaddedBlock.length);
            return unpaddedBlock;
        }
    }

}   // Decryptor class end
