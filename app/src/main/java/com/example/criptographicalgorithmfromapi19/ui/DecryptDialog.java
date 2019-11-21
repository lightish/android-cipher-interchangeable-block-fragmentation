package com.example.criptographicalgorithmfromapi19.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.criptographicalgorithmfromapi19.R;
import com.example.criptographicalgorithmfromapi19.binary.ByteHelper;
import com.example.criptographicalgorithmfromapi19.security.Decryptor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class DecryptDialog extends AppCompatDialogFragment {
    private int rounds;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.decryption_dialog, null);

        builder.setView(view)
                .setTitle("Decrypt and save")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Bundle arguments = getArguments();
                        EditText fileNameET = view.findViewById(R.id.file_name);
                        fileNameET.clearFocus();

                        if (fileNameET.getText() != null) {
                            String fileName = fileNameET.getText().toString().trim();
                            if (fileName.length() > 0) {

                                File appFilePath = (File) arguments.getSerializable("appFilePath");
                                Uri targetFileUri = arguments.getParcelable("targetFileUri");
                                byte[] keyByte = arguments.getByteArray("keyByte");
                                Context context = getContext();

                                try {
                                    InputStream fileIS = context.getContentResolver()
                                            .openInputStream(targetFileUri);
                                    onConfirm(keyByte, fileName, appFilePath, fileIS);

                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                    Toast.makeText(context, "Couldn't find the file",
                                            Toast.LENGTH_SHORT).show();

                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(context, "Output system error",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                });


        return builder.create();
    }

    private void onConfirm(byte[] keyByte, String fileName, File appFilePath,
                           InputStream sourceBin) throws IOException {

        if (keyByte != null && keyByte.length != 0) {
            byte[] keyBin = separateKeyFromRounds(keyByte);

            Decryptor decryptor = new Decryptor();
            decryptor.decrypt(sourceBin, keyBin, rounds, appFilePath, fileName,
                    (MainActivity) getActivity());
        }
    }

    private byte[] separateKeyFromRounds(byte[] keyByte) {
        rounds = keyByte[15];

        byte[] keyBin = ByteHelper.toBitArray(Arrays.copyOf(keyByte, 15));

        return keyBin;
    }
}
