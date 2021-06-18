package com.backups.app.ui.actions;

import androidx.annotation.LayoutRes;
import androidx.fragment.app.FragmentActivity;

public final class ActionSetMaker {
  private ActionSetMaker() {}

  public interface CallBackSetup {
    void setup(int position, final IActionButton action,
               final IDefaultAction defaultChecks);
  }

  public static IActionButton[] makeActionSet(
      final IPresenter presenter, final FragmentActivity parentActivity,
      @LayoutRes final int[][] layoutIDs, final int[] colors,
      final IDefaultAction defaultAction, final CallBackSetup callBackSetup) {
    boolean canSetup =
        layoutIDs != null && presenter != null && parentActivity != null;
    if (canSetup) {
      int total = layoutIDs.length;

      IActionButton[] actions = new IActionButton[total];

      for (int i = 0; i < total; ++i) {
        IActionButton action = new ActionButton(presenter, parentActivity,
                                                layoutIDs[i], colors, false);

        callBackSetup.setup(i, action, defaultAction);

        actions[i] = action;
      }
      return actions;
    }
    return null;
  }
}
