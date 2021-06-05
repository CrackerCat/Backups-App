package com.backups.app.utils;

public final class TextUtils {
  private TextUtils() {}

  public static String composeParagraph(final String introduction,
                                        final String[] additionalText) {
    if (additionalText != null) {
      if (additionalText.length != 0) {
        final StringBuilder paragraphBuilder = new StringBuilder(introduction);

        final String newLine = "\n";

        for (String sentence : additionalText) {
          paragraphBuilder.append(newLine).append(sentence).append(newLine);
        }

        return paragraphBuilder.toString();
      }
    }

    return introduction;
  }
}
