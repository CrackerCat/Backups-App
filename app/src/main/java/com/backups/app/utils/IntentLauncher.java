package com.backups.app.utils;

import android.content.Intent;
import android.net.Uri;
import androidx.fragment.app.FragmentActivity;

public final class IntentLauncher {
  private IntentLauncher() {}

  public static void launchWebPage(final FragmentActivity activity,
                                   final String url) {
    Uri webpage = Uri.parse(url);

    Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
    if (intent.resolveActivity(activity.getPackageManager()) != null) {
      activity.startActivity(intent);
    }
  }
  public static void composeEmail(final FragmentActivity activity,
                                  final String[] addresses,
                                  final String subject, final String body) {
    Intent intent = new Intent(Intent.ACTION_SENDTO);
    intent.setData(Uri.parse("mailto:")); // only email apps should handle this

    intent.putExtra(Intent.EXTRA_EMAIL, addresses);
    intent.putExtra(Intent.EXTRA_SUBJECT, subject);
    intent.putExtra(Intent.EXTRA_TEXT, body);

    if (intent.resolveActivity(activity.getPackageManager()) != null) {
      activity.startActivity(intent);
    }
  }

  public static void composeShareableMessage(FragmentActivity activity,
                                             final String title,
                                             final String body,
                                             final String[] links) {
    Intent sendIntent = new Intent();
    sendIntent.setAction(Intent.ACTION_SEND);

    sendIntent.putExtra(Intent.EXTRA_TITLE, title);

    sendIntent.putExtra(Intent.EXTRA_TEXT,
                        TextUtils.composeParagraph(body, links));

    sendIntent.setType("text/plain");

    Intent shareIntent = Intent.createChooser(sendIntent, null);
    activity.startActivity(shareIntent);
  }
}
