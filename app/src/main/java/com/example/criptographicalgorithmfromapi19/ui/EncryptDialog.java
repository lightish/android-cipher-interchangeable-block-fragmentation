package com.example.criptographicalgorithmfromapi19.ui;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

import com.example.criptographicalgorithmfromapi19.R;
import com.example.criptographicalgorithmfromapi19.io.IOHelper;
import com.example.criptographicalgorithmfromapi19.security.Encryptor;
import com.example.criptographicalgorithmfromapi19.util.AlgUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.example.criptographicalgorithmfromapi19.binary.ByteHelper.toByteArray;

public class EncryptDialog extends AppCompatDialogFragment {

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.encryption_dialog, null);


        builder.setView(view)
                .setTitle("Encrypt")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Bundle arguments = getArguments();
                        File appFilePath = (File) arguments.getSerializable("appFilePath");
                        Uri targetFileUri = arguments.getParcelable("targetFileUri");
                        Context context = getContext();

                        String fileName = getFileName(view);
                        int rounds = getRoundsNumber(view);

                        if (rounds != -1) {
                            try {
                                InputStream fileIS = context.getContentResolver().openInputStream(targetFileUri);
                                onConfirm(fileIS, appFilePath, targetFileUri, fileName, rounds);

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
                });

        return builder.create();
    }

    private String getFileName(View view) {
        EditText fileName = view.findViewById(R.id.file_name);
        return fileName.getText().toString().trim();
    }

    private int getRoundsNumber(View view) {
        EditText rounds = view.findViewById(R.id.rounds);
        String input = rounds.getText().toString().trim();
        Integer roundsInteger;

        try {
            roundsInteger = Integer.decode(input);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(),
                    "Couldn't parse rounds number",
                    Toast.LENGTH_SHORT).show();
            return -1;
        }

        int roundsNumber = roundsInteger;
        if (roundsNumber < 1 || roundsNumber > 255) {
            Toast.makeText(getContext(),
                    "Number of rounds must be bigger then 0 and lower then 256",
                    Toast.LENGTH_SHORT).show();
            return -1;
        }
        return roundsNumber;
    }

    private void onConfirm(InputStream dataSource, File appFilePath,
                           Uri targetFileUri, String fileName, int rounds) throws IOException {

        Encryptor encryptor = new Encryptor();
        byte[] keyBin = makeKeyBin();
        if (fileName == null) {
            Toast.makeText(getContext(), "File name is empty. Saved file as New Encrypted File", Toast.LENGTH_SHORT).show();
            fileName = "New Encrypted File";
        }
        String fileExtension = getTargetFileExtension(targetFileUri);

        encryptor.encrypt(dataSource, keyBin, rounds,
                            appFilePath, fileName,
                            fileExtension, (MainActivity) getActivity());

        saveKey(keyBin, rounds, appFilePath, fileName);
    }

    private byte[] makeKeyBin() {
        byte[] keyBin = new byte[120];
        AlgUtils.placeRandomBits(keyBin, 0, 120);
        return keyBin;
    }

    private void saveKey(byte[] keyBin, int rounds, File appFilePath, String fileName) {
        byte[] keyByte = toByteArray(keyBin);
        IOHelper.save(getContext(), appFilePath, fileName + ".key",
                keyByte, new byte[]{(byte) rounds});
    }

    private String getTargetFileExtension(Uri targetFileUri) {
        String extension;

        if (targetFileUri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            final MimeTypeMap mime = MimeTypeMap.getSingleton();
            extension = mime.getExtensionFromMimeType(getContext().getContentResolver().getType(targetFileUri));
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters. This will avoid returning null values on file name with spaces and special characters.
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(targetFileUri.getPath())).toString());
        }

        return extension;
    }
}
