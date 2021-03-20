package com.backups.app.data;

import java.util.Hashtable;

public class PackageNameUtils {
  private static final String[] sPrefixes = {"com.", "org."};
  private static final String sRepeatedBackupFormatterString = "%s (%d)";
  private static final StringBuilder sBuffer = new StringBuilder();
  private static final Hashtable<String, Integer> sTimesTable =
      new Hashtable<>();

  public static boolean containsPackageNamePrefix(final String appName) {
    for (String prefix : sPrefixes) {
      if (appName.contains(prefix)) {
        return true;
      }
    }
    return false;
  }

  public static String extractHumanReadableName(final String appName) {
    int end = appName.length() - 1;

    while (appName.charAt(end) != '.') {
      --end;
    }

    int indexOfFirstLetter = end + 1;
    int indexAfterFirstLetter = indexOfFirstLetter + 1;

    char firstLetterOfName = appName.charAt(indexOfFirstLetter);

    if (Character.isUpperCase(firstLetterOfName)) {
      firstLetterOfName = Character.toUpperCase(firstLetterOfName);
    }

    return firstLetterOfName + appName.substring(indexAfterFirstLetter);
  }

  public static void resetCountFor(String appName) {
    if (sTimesTable.contains(appName)) {
      sTimesTable.put(appName, 0);
    }
  }

  public static String computeRepeatedBackupName(String appName) {
    String formattedName = null;

    if (sTimesTable.containsKey(appName)) {
      int count = sTimesTable.get(appName);

      ++count;
      sTimesTable.put(appName, count);

      sBuffer.append(
          String.format(sRepeatedBackupFormatterString, appName, count));

      formattedName = sBuffer.toString();
      sBuffer.delete(0, formattedName.length());
    } else {
      sTimesTable.put(appName, 0);
    }
    return formattedName;
  }
}
