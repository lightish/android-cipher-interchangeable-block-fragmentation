package com.example.criptographicalgorithmfromapi19.binary;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ByteHelper {

    public static byte[] getFileByte(Context context, Uri filePath) {
        byte[] data = null;

        try (InputStream fileIS = context.getContentResolver().openInputStream(filePath)) {
            data = toByteArray(fileIS);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context, "Couldn't find the file", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Output system error", Toast.LENGTH_SHORT).show();
        }

        return data;
    }

    public static byte[] getFileBin(Context context, Uri filePath) {
        return toBitArray(getFileByte(context, filePath));
    }

    public static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }

        return os.toByteArray();
    }

    public static byte[] toBitArray(byte[] bytes) {
        byte[] bits = new byte[bytes.length * 8];

        for (int i = 0; i < bytes.length * 8; i++) {
            if ((bytes[i / 8] & (1 << (7 - (i % 8)))) > 0) {
                bits[i] = 1;
            }
        }

        return bits;
    }
    public static byte[] toBitBlock(byte[] bytes, int blockLength) {
        byte[] bits = new byte[blockLength];
        int bitsNum = bytes.length * 8;

        for (int i = 0; i < bitsNum; i++) {

            if ((bytes[i / 8] & (1 << (7 - (i % 8)))) > 0) {

                bits[i] = 1;
            }
        }

        return bits;
    }
    public static byte[] toByteArray(byte[] bits) {
        byte[] results = new byte[(bits.length + 7) / 8];
        int byteValue = 0;
        int index;

        for (index = 0; index < bits.length; index++) {

            byteValue = (byteValue << 1) | bits[index];

            if (index % 8 == 7) {
                results[index / 8] = (byte) byteValue;
            }
        }

        if (index % 8 != 0) {
            results[index / 8] = (byte) (byteValue << (8 - (index % 8)));
        }

        return results;
    }
}
