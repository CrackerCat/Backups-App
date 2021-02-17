package com.backups.app.permissionshandler;

import android.content.Intent;
import androidx.fragment.app.FragmentActivity;

public class PermissionsHandler {
  public static final int CREATE_DIRECTORY_CODE = 51;

  protected static boolean checkExternalFilesystemPermissions() {
    return false;
  }

  protected static void promptUserForPermissions() {}

  protected static void askForExternalFilesystemPermissions(FragmentActivity activity) {
    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    activity.startActivityForResult(intent, CREATE_DIRECTORY_CODE);
  }
}
