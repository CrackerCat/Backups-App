package com.backups.app.ui.actions;

import androidx.annotation.LayoutRes;
import androidx.fragment.app.FragmentActivity;

public final class ActionButtonsConfig {
  public IPresenter presenter;
  public FragmentActivity parentActivity;
  public @LayoutRes int[][] layoutIDs;
  public int[] colors;
  public boolean isActive;

  public ActionButtonsConfig(final IPresenter presenter,
                             final FragmentActivity parentActivity,
                             final int[][] layoutIDs, final int[] colors,
                             final boolean isActive) {
    this.presenter = presenter;
    this.parentActivity = parentActivity;
    this.layoutIDs = layoutIDs;
    this.colors = colors;
    this.isActive = isActive;
  }
}
