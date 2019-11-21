package com.example.criptographicalgorithmfromapi19.util;
import java.util.Random;
final public class AlgUtils {
    /**
     * Количество елементов в левом и правом блоках.
     */
    public final static int MAIN_BLOCKS_WIDTH;
    /**
     * Массив высоконелинейных в чевтеричном смысле S-блоков
     */
    public final static int[][] S1;
    /**
     * Массив S-блоков, удовлетворяющих СЛК высшего порядка
     */
    public final static int[][] S2;
    /**
     * Map of replacements for right main blocks
     */
    final static int[] P;
    public static void divideIntoBlocks(byte[] source, byte[] leftFragment, byte[] rightFragment) {
        System.arraycopy(source, 0, leftFragment, 0, MAIN_BLOCKS_WIDTH);
        System.arraycopy(source, leftFragment.length, rightFragment, 0, MAIN_BLOCKS_WIDTH);
    }
    public static byte[] concatBlocks(byte[] leftFragment, byte[] rightFragment) {
        byte[] newArr = new byte[leftFragment.length + rightFragment.length];

        System.arraycopy(leftFragment, 0, newArr, 0, leftFragment.length);
        System.arraycopy(rightFragment, 0, newArr, leftFragment.length, rightFragment.length);

        return newArr;
    }
    /**
     * Binary-to-decimal conversion for passed amount of digits in array from passed index
     */
    public static int binArray2Decimal(byte[] bin, int from, int digits) {
        int i = 0;
        int decimal = 0;
        while (i < digits) {
            decimal |= bin[from + i++] << digits - i;
        }
        return decimal;
    }
    /**
     * Fills passed amount of next spots in array
     * starting from passed index with binary representation of passed decimal
     */
    public static void fillWithBinary(byte[] arr, int from, int decimal, int digits) {
        int i = 0;
        while (i < digits) {
            arr[from + i++] = (byte) ((decimal >> (digits - i)) & 1);
        }
    }
    /**
     * @param bound is exclusive
     */
    public static void placeRandomBits(byte[] arr, int from, int bound) {
        Random random = new Random();
        for (; from < bound; from++) {
            arr[from] = (byte) random.nextInt(2);
        }
    }
    /**
     * Makes an array as a result of XOR of two parameter arrays.
     * Arrays must be the same length
     */
    public static byte[] xorArrays(byte[] arr1, byte[] arr2) {
        byte[] result = new byte[arr1.length];

        for (int i = 0; i < arr1.length; i++) {
            result[i] = (byte) (arr1[i] ^ arr2[i]);
        }
        return result;
    }
    public static byte[] performSBlockPermutation(byte[] initBlock, int[][] SBlock, int fragmentBinWidth) {
        byte[] newBlock = new byte[MAIN_BLOCKS_WIDTH];

        for (int i = 0, k = 0; i < MAIN_BLOCKS_WIDTH; i += fragmentBinWidth, k++) {
            int fragment = binArray2Decimal(initBlock, i, fragmentBinWidth);
            int newFragment = SBlock[k][fragment];
            fillWithBinary(newBlock, i, newFragment, fragmentBinWidth);
        }
        return newBlock;
    }
    public static byte[] performReverseSBlockPermutation(byte[] initBlock, int[][] SBlock, int fragmentBinWidth) {
        byte[] newBlock = new byte[MAIN_BLOCKS_WIDTH];

        for (int i = 0, k = 0; i < MAIN_BLOCKS_WIDTH; i += fragmentBinWidth, k++) {
            int fragment = binArray2Decimal(initBlock, i, fragmentBinWidth);
            int newFragment = findInUnsortedArray(SBlock[k], fragment);
            fillWithBinary(newBlock, i, newFragment, fragmentBinWidth);
        }
        return newBlock;
    }
    /**
     * Replacing values from array according to array P with replacement indexes.
     * Parameter must be the same length as P
     */
    public static byte[] mapOnP(byte[] array) {
        byte[] result = new byte[array.length];

        for (int i = 0; i < array.length; i++) {
            result[P[i]] = array[i];
        }
        return result;
    }
    public static byte[] reverseMapFromP(byte[] array) {
        byte[] result = new byte[array.length];

        for (byte i = 0; i < array.length; i++) {
            result[findInUnsortedArray(P, i)] = array[i];
        }
        return result;
    }
    public static byte findInUnsortedArray(int[] arr, int key) {
        for (byte i = 0; i < arr.length; i++) {
            if (arr[i] == key)
                return i;
        }
        return -1;
    }
    public static double estimateProgressIncrPoint(int bytesTotal) {
        double blocksTotal = bytesTotal / 30;
        return 100 / blocksTotal;
    }

    static {
        MAIN_BLOCKS_WIDTH = 120;
        S1 = new int[][]
                {
                        {12,   13,   4,   5,   14,   7,   9,    0,   15,   6,   11,   10,   8,   1,   3,   2},
                        {15,   13,   4,   5,   12,   7,   9,    0,   14,   6,   11,   10,   8,   1,   3,   2},
                        {12,   14,   4,   5,   15,   7,   10,   0,   13,   6,   11,   9,    8,   1,   3,   2},
                        {15,   14,   4,   5,   13,   7,   10,   0,   12,   6,   11,   9,    8,   1,   3,   2},
                        {14,   13,   4,   5,   12,   7,   11,   0,   15,   6,   9,    10,   8,   1,   3,   2},
                        {13,   14,   4,   5,   12,   7,   11,   0,   15,   6,   10,   9,    8,   1,   3,   2},
                        {15,   14,   4,   5,   12,   7,   11,   0,   13,   6,   10,   9,    8,   1,   3,   2},
                        {12,   15,   4,   6,   14,   5,   9,    0,   13,   7,   11,   10,   8,   1,   3,   2},
                        {14,   15,   4,   6,   12,   7,   9,    0,   13,   5,   11,   10,   8,   1,   3,   2},
                        {13,   14,   4,   6,   15,   7,   10,   0,   12,   5,   9,    11,   8,   1,   3,   2},
                        {12,   15,   4,   6,   14,   7,   11,   0,   13,   5,   9,    10,   8,   1,   3,   2},
                        {13,   15,   4,   6,   12,   7,   11,   0,   14,   5,   9,    10,   8,   1,   3,   2},
                        {14,   12,   4,   7,   13,   5,   10,   0,   15,   6,   9,    11,   8,   1,   3,   2},
                        {12,   13,   4,   7,   14,   5,   11,   0,   15,   6,   9,    10,   8,   1,   3,   2},
                        {15,   13,   4,   7,   12,   5,   11,   0,   14,   6,   9,    10,   8,   1,   3,   2},
                        {12,   14,   5,   4,   13,   6,   9,    0,   15,   7,   11,   10,   8,   1,   3,   2},
                        {12,   13,   5,   4,   14,   7,   9,    0,   15,   6,   10,   11,   8,   1,   3,   2},
                        {15,   13,   5,   4,   12,   7,   9,    0,   14,   6,   10,   11,   8,   1,   3,   2},
                        {14,   15,   5,   4,   12,   7,   11,   0,   13,   6,   10,   9,    8,   1,   3,   2},
                        {14,   15,   5,   6,   13,   4,   9,    0,   12,   7,   10,   11,   8,   1,   3,   2},
                        {12,   14,   5,   6,   15,   4,   9,    0,   13,   7,   11,   10,   8,   1,   3,   2},
                        {15,   14,   5,   6,   13,   4,   9,    0,   12,   7,   11,   10,   8,   1,   3,   2},
                        {12,   13,   5,   6,   15,   4,   11,   0,   14,   7,   10,   9,    8,   1,   3,   2},
                        {14,   15,   5,   6,   12,   7,   11,   0,   13,   4,   10,   9,    8,   1,   3,   2},
                        {13,   12,   6,   4,   14,   5,   9,    0,   15,   7,   10,   11,   8,   1,   3,   2},
                        {12,   13,   6,   4,   14,   5,   9,    0,   15,   7,   11,   10,   8,   1,   3,   2},
                        {15,   13,   6,   4,   12,   5,   9,    0,   14,   7,   11,   10,   8,   1,   3,   2},
                        {12,   14,   6,   4,   15,   5,   10,   0,   13,   7,   11,   9,    8,   1,   3,   2},
                        {15,   14,   6,   4,   13,   5,   10,   0,   12,   7,   11,   9,    8,   1,   3,   2},
                        {14,   13,   6,   4,   12,   5,   11,   0,   15,   7,   9,    10,   8,   1,   3,   2}
                };
        S2 = new int[][]
                {
                        {0,   30,   2,  28,   4,  26,  25,   7,   8,   9,  10,  11,  19,  18,  14,  15,  16,  17,  13,  12,  20,  21,  22,  23,  24,   6,   5,  27,   3,  29,   1,  31},
                        {0,   14,  18,  28,   4,  10,   9,   7,   8,  25,  26,  11,  19,   2,  30,  15,  16,   1,  29,  12,  20,   5,   6,  23,  24,  22,  21,  27,   3,  13,  17,  31},
                        {0,   14,   2,  12,  20,  26,   9,   7,   8,  25,  10,  27,   3,  18,  30,  15,  16,   1,  13,  28,   4,  21,   6,  23,  24,  22,   5,  11,  19,  29,  17,  31},
                        {0,   30,  18,  12,  20,  10,  25,   7,   8,   9,  26,  27,   3,   2,  14,  15,  16,  17,  29,  28,   4,   5,  22,  23,  24,   6,  21,  11,  19,  13,   1,  31},
                        {0,   14,   2,  12,   4,  10,  25,  23,  24,   9,  26,  11,   3,  18,  30,  15,  16,   1,  13,  28,  20,   5,  22,   7,   8,   6,  21,  27,  19,  29,  17,  31},
                        {0,   30,  18,  12,   4,  26,   9,  23,  24,  25,  10,  11,   3,   2,  14,  15,  16,  17,  29,  28,  20,  21,   6,   7,   8,  22,   5,  27,  19,  13,   1,  31},
                        {0,   30,   2,  28,  20,  10,   9,  23,  24,  25,  26,  27,  19,  18,  14,  15,  16,  17,  13,  12,   4,   5,   6,   7,   8,  22,  21,  11,   3,  29,   1,  31},
                        {0,   14,  18,  28,  20,  26,  25,  23,  24,   9,  10,  27,  19,   2,  30,  15,  16,   1,  29,  12,   4,  21,  22,   7,   8,   6,   5,  11,   3,  13,  17,  31},
                        {16,  14,  18,  12,  20,  10,   9,  23,  24,  25,  26,  27,   3,   2,  30,  31,   0,   1,  29,  28,   4,   5,   6,   7,   8,  22,  21,  11,  19,  13,  17,  15},
                        {16,  30,   2,  12,  20,  26,  25,  23,  24,   9,  10,  27,   3,  18,  14,  31,   0,  17,  13,  28,   4,  21,  22,   7,   8,   6,   5,  11,  19,  29,   1,  15},
                        {16,  30,  18,  28,   4,  10,  25,  23,  24,   9,  26,  11,  19,   2,  14,  31,   0,  17,  29,  12,  20,   5,  22,   7,   8,   6,  21,  27,   3,  13,   1,  15},
                        {16,  14,   2,  28,   4,  26,   9,  23,  24,  25,  10,  11,  19,  18,  30,  31,   0,   1,  13,  12,  20,  21,   6,   7,   8,  22,   5,  27,   3,  29,  17,  15},
                        {16,  30,  18,  28,  20,  26,   9,   7,   8,  25,  10,  27,  19,   2,  14,  31,   0,  17,  29,  12,   4,  21,   6,  23,  24,  22,   5,  11,   3,  13,   1,  15},
                        {16,  14,   2,  28,  20,  10,  25,   7,   8,   9,  26,  27,  19,  18,  30,  31,   0,   1,  13,  12,   4,   5,  22,  23,  24,   6,  21,  11,   3,  29,  17,  15},
                        {16,  14,  18,  12,   4,  26,  25,   7,   8,   9,  10,  11,   3,   2,  30,  31,   0,   1,  29,  28,  20,  21,  22,  23,  24,   6,   5,  27,  19,  13,  17,  15},
                        {8,   22,  18,  28,  20,  10,   1,  15,  24,  25,   2,  19,  27,  26,  14,  31,   0,  17,   5,   4,  12,  29,   6,   7,  16,  30,  21,  11,   3,  13,   9,  23},
                        {8,   6,   2,  28,   4,  10,   1,  31,  24,   9,  18,  19,  11,  26,  14,  15,  16,  17,   5,  20,  12,  13,  22,   7,   0,  30,  21,  27,   3,  29,  25,  23},
                        {8,   22,  18,  28,   4,  26,  17,  31,  24,  25,   2,  19,  11,  10,  30,  15,  16,   1,  21,  20,  12,  29,   6,   7,   0,  14,   5,  27,   3,  13,   9,  23},
                        {8,   22,   2,  12,  20,  10,  17,  31,  24,  25,  18,   3,  27,  26,  30,  15,  16,   1,   5,   4,  28,  13,   6,   7,   0,  14,  21,  11,  19,  29,   9,  23},
                        {8,   6,  18,  12,  20,  26,   1,  31,  24,   9,   2,   3,  27,  10,  14,  15,  16,  17,  21,   4,  28,  29,  22,   7,   0,  30,   5,  11,  19,  13,  25,  23},
                        {24,  6,  18,  28,  20,  10,  17,  31,   8,   9,   2,  19,  27,  26,  30,  15,  16,   1,   5,   4,  12,  29,  22,  23,   0,  14,  21,  11,   3,  13,  25,   7},
                        {24,  22,   2,  28,  20,  26,   1,  31,   8,  25,  18,  19,  27,  10,  14,  15,  16,  17,  21,   4,  12,  13,   6,  23,   0,  30,   5,  11,   3,  29,   9,   7},
                        {24,  22,  18,  12,   4,  10,   1,  31,   8,  25,   2,   3,  11,  26,  14,  15,  16,  17,   5,  20,  28,  29,   6,  23,   0,  30,  21,  27,  19,  13,   9,   7},
                        {24,  6,   2,  12,   4,  26,  17,  31,   8,   9,  18,   3,  11,  10,  30,  15,  16,   1,  21,  20,  28,  13,  22,  23,   0,  14,   5,  27,  19,  29,  25,   7}
                };
        P = new int[]
                {91, 46, 90, 87, 36, 102, 44, 31, 25, 63, 70, 108, 14, 72, 82, 78, 39, 20, 52, 114,
                        92, 43, 65, 80, 6, 16, 42, 24, 32, 85, 50, 23, 11, 94, 62, 47, 79, 77, 10,
                        106, 73, 81, 22, 109, 1, 119, 98, 19, 56, 61, 4, 30, 103, 66, 27, 34, 99,
                        100, 117, 37, 64, 60, 76, 113, 57, 29, 68, 7, 96, 95, 38, 40, 33, 26, 97,
                        105, 75, 13, 88, 8, 15, 53, 2, 0, 111, 45, 89, 58, 51, 112, 59, 86, 9, 28,
                        49, 21, 5, 17, 74, 104, 83, 118, 54, 41, 93, 18, 3, 116, 110, 55, 35, 107,
                        69, 12, 67, 115, 84, 48, 101, 71};
    }
}
