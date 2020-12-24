package com.backups.app;

import androidx.documentfile.provider.DocumentFile;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.backups.app.filehandling.APKFileOperations;

public class MainActivity extends AppCompatActivity {
    TextView mTextView;
    Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.main_tv);
        mButton = findViewById(R.id.main_btn);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        Uri newDirectoryUri;

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == APKFileOperations.CREATE_DIRECTORY_CODE) {
                if (resultData != null) {
                    newDirectoryUri = resultData.getData();
                    DocumentFile pickedDirectory = DocumentFile.fromTreeUri(this, newDirectoryUri);
                    DocumentFile newDirectory = pickedDirectory.createDirectory(APKFileOperations.OUTPUT_DIRECTORY);
                    mTextView.setText(String.format("Apk Backups Directory: %s/%s", pickedDirectory.getName(), newDirectory.getName()));
                }
            }
        }
    }

}