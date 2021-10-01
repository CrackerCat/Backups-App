package com.backups.app.data.viewmodels.appqueue;

import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

final class AppQueueEntryHelper {
  private final Map<Integer, Integer> mEntryTable = new Hashtable<>();

  public String computeDuplicateBackupName(final int appHash,
                                           final String appName) {
    String formattedName = null;

    if (mEntryTable.containsKey(appHash)) {
      Integer count = mEntryTable.get(appHash);

      if (count != null) {
        mEntryTable.put(appHash, ++count);

        formattedName =
            String.format(Locale.getDefault(), "%s (%d)", appName, count);
      }

    } else {
      mEntryTable.put(appHash, 0);
    }

    return formattedName;
  }

  public void decrementCounterFor(final int appHash) {
    Integer count = mEntryTable.get(appHash);

    if (count != null) {
      mEntryTable.put(appHash, --count);
    }
  }

  public void resetBackupCounter(final int appHash) {
    mEntryTable.remove(appHash);
  }

  public void clearRegisteredEntries() { mEntryTable.clear(); }
}
