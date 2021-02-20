package com.backups.app.data;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

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

  public void resetRepeatedCountTable() {
    Set<Map.Entry<String, Integer>> pairs = sTimesTable.entrySet();

    for (Map.Entry<String, Integer> pair : pairs) {
      pair.setValue(0);
    }
  }

  public static String computeRepeatedBackupName(String name) {
    String formattedName = null;
    if (sTimesTable.containsKey(name)) {
      int count = sTimesTable.get(name);

      int updatedCount = count + 1;
      sTimesTable.put(name, updatedCount);

      sBuffer.append(
          String.format(sRepeatedBackupFormatterString, name, updatedCount));

      formattedName = sBuffer.toString();
      sBuffer.delete(0, formattedName.length());
    } else {
      int initialValue = 0;
      sTimesTable.put(name, initialValue);
    }
    return formattedName;
  }
}
