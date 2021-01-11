package com.backups.app;


import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.backups.app.fileoperations.APKFileOperations;

public class PermissionsHandler {
    protected static boolean checkExternalFilesystemPermissions() {
        return false;
    }

    protected static void promptUserForPermissions() {

    }

    protected static void askForExternalFilesystemPermissions(AppCompatActivity activity) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        activity.startActivityForResult(intent, APKFileOperations.CREATE_DIRECTORY_CODE);

    }
}
