package com.example.criptographicalgorithmfromapi19.io;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public final class IOHelper {

    public static void save(Context context, File destinationFolder, String fileName, byte[] ... dataInBytes) {
        File file = new File(destinationFolder, fileName);

        try (FileOutputStream fileOut = new FileOutputStream(file)) {

            for (byte[] data : dataInBytes) {
                fileOut.write(data);
            }

            Toast.makeText(context,
                    "File " + fileName + " saved to " + destinationFolder.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context, "Couldn't save this file. Check read/write permissions", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Output system error", Toast.LENGTH_SHORT).show();
        }

    }
}
