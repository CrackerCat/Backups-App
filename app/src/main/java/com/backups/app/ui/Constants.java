package com.backups.app.ui;

import com.backups.app.R;

public class Constants {
  // FAButtons
  public static final int SEARCH_BUTTON = 0;
  public static final int BACKUP_BUTTON = 1;

  public static final int[][] sAppListFragmentActionLayouts =
      new int[][] {{R.id.main_search_label, R.id.main_search_button}};

  public static final int sAppListFragmentTotalActions =
      sAppListFragmentActionLayouts.length;

  public static final int[][] sAppQueueFragmentActionLayouts =
      new int[][] {{R.id.app_queue_search_label, R.id.app_queue_search_button}};

  public static final int sAppQueueFragmentTotalActions =
      sAppQueueFragmentActionLayouts.length;
  //

  // Tabs
  public static final int APPLIST = 0;
  public static final int APPQUEUE = 1;
  //
}
