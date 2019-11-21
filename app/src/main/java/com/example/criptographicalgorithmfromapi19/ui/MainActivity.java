package com.example.criptographicalgorithmfromapi19.ui;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.criptographicalgorithmfromapi19.R;
import com.example.criptographicalgorithmfromapi19.binary.ByteHelper;
import java.io.File;

public class MainActivity extends AppCompatActivity {
    static final int CHOOSE_FILE = 1;
    static final int GET_KEY = 2;
    private Fragment progressTrackingFrag;
    private TextView curFileView;
    private File appFilePath;
    private Uri lastChosenFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        curFileView = findViewById(R.id.curFileView);
        Button loadBtn = findViewById(R.id.loadBtn);
        final Button encryptBtn = findViewById(R.id.encryptBtn);
        Button decryptBtn = findViewById(R.id.decryptBtn);
        progressTrackingFrag = getSupportFragmentManager().findFragmentById(R.id.progressTrackingFrag);

        hideFragment(progressTrackingFrag);
        setupAppFile();

        loadBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { performFileSearch(); }
        });

        encryptBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (lastChosenFileUri != null) { openEncryptDialog(); }
                else Toast.makeText(MainActivity.this,"Load file first", Toast.LENGTH_SHORT).show();
            }
        });

        decryptBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (lastChosenFileUri != null) getKeyAndDecrypt();
                else Toast.makeText(MainActivity.this,"Load file first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAppFile() {
        appFilePath = new File(Environment.getExternalStorageDirectory(), "appEncrypted");
        appFilePath.mkdir();
    }

    private void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(Uri.fromFile(appFilePath), "*/*");
        startActivityForResult(intent, CHOOSE_FILE);
    }

    private void getKeyAndDecrypt() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(Uri.fromFile(appFilePath), "*/*");

        startActivityForResult(intent, GET_KEY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case CHOOSE_FILE:
                if (resultCode == RESULT_OK) {
                    Uri newData = data.getData();

                    if (fileIsValid(newData)) {
                        lastChosenFileUri = newData;
                        curFileView.setText(lastChosenFileUri.getPath());
                    }
                }
            break;
            case GET_KEY:
                if (resultCode == RESULT_OK) {
                    Uri keyFileUri = data.getData();
                    if (keyFileUri != null) {
                        byte[] keyByte = ByteHelper.getFileByte(this, keyFileUri);
                        openDecryptDialog(keyByte);
                    }
                }
        }
    }

    private boolean fileIsValid(Uri fileUri) {
        return fileUri != null;
    }

    private void openEncryptDialog() {
        EncryptDialog cryptoDialog = new EncryptDialog();

        Bundle arguments = new Bundle();
        arguments.putSerializable("appFilePath", appFilePath);
        arguments.putParcelable("targetFileUri", lastChosenFileUri);

        cryptoDialog.setArguments(arguments);
        cryptoDialog.show(getSupportFragmentManager(), "Encrypt File");
    }

    private void openDecryptDialog(byte[] keyByte) {
        DecryptDialog cryptoDialog = new DecryptDialog();

        Bundle arguments = new Bundle();
        arguments.putSerializable("appFilePath", appFilePath);
        arguments.putParcelable("targetFileUri", lastChosenFileUri);
        arguments.putByteArray("keyByte", keyByte);

        cryptoDialog.setArguments(arguments);
        cryptoDialog.show(getSupportFragmentManager(), "Decrypt File");
    }

    private void hideFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().hide(fragment).commit();
    }

    public Fragment getProgressTrackingFragment() {
        return progressTrackingFrag;
    }
}
