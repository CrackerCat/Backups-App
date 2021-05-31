package com.backups.app.utils;

import android.content.Context;
import android.widget.Toast;
import androidx.core.util.Pair;

public class NotificationsUtils {
  private NotificationsUtils() {}

  public static boolean
  checkNotifyAndGetResult(Context context,
                          Result<Pair<Boolean, String>> checks) {
    Pair<Boolean, String> result = checks.onComplete();

    Toast.makeText(context, result.second, Toast.LENGTH_SHORT).show();

    return result.first;
  }

  public static void checkAndNotify(Context context, Result<String> checks) {
    String result = checks.onComplete();

    Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
  }
}
