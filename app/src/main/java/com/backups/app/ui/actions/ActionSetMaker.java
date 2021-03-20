package com.backups.app.ui.actions;

import androidx.annotation.LayoutRes;
import androidx.fragment.app.FragmentActivity;

public class ActionSetMaker {

  public interface CallBackSetup {
    void setup(int position, IAction action);
  }

  public static IAction[] makeActionSet(IPresenter presenter,
                                        FragmentActivity parentActivity,
                                        @LayoutRes int[][] layoutIDs,
                                        CallBackSetup callBackSetup) {
    boolean canSetup =
        layoutIDs != null && presenter != null && parentActivity != null;
    if (canSetup) {
      int total = layoutIDs.length;
      IAction[] actions = new IAction[total];
      for (int i = 0; i < total; ++i) {
        IAction action =
            new ActionButton(presenter, parentActivity, layoutIDs[i], false);
        callBackSetup.setup(i, action);
        actions[i] = action;
      }
      return actions;
    }
    return null;
  }
}
