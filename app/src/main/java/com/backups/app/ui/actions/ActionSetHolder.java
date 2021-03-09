package com.backups.app.ui.actions;

import static com.backups.app.ui.Constants.APPLIST;
import static com.backups.app.ui.Constants.APPQUEUE;
import static com.backups.app.ui.Constants.sAppListFragmentActionLayouts;
import static com.backups.app.ui.Constants.sAppListFragmentTotalActions;
import static com.backups.app.ui.Constants.sAppQueueFragmentActionLayouts;
import static com.backups.app.ui.Constants.sAppQueueFragmentTotalActions;

import androidx.fragment.app.FragmentActivity;

public class ActionSetHolder {
  private final IPresenter mPresenter;
  private final FragmentActivity mParentActivity;
  private final IAction[] mAppListFragmentActions;
  private final IAction[] mAppQueueFragmentActions;

  public ActionSetHolder(IPresenter presenter,
                         FragmentActivity parentActivity) {
    mPresenter = presenter;
    mParentActivity = parentActivity;

    mAppListFragmentActions = new IAction[sAppListFragmentTotalActions];
    mAppQueueFragmentActions = new IAction[sAppQueueFragmentTotalActions];

    initializeAppListActions();
    initializeAppQueueActions();
  }

  private void initializeAppListActions() {
    for (int i = 0; i < sAppListFragmentTotalActions; i++) {
      mAppListFragmentActions[i] = new ActionButton(
          mPresenter, mParentActivity, sAppListFragmentActionLayouts[i], false);
    }
  }
  private void initializeAppQueueActions() {
    for (int i = 0; i < sAppQueueFragmentTotalActions; i++) {
      mAppQueueFragmentActions[i] =
          new ActionButton(mPresenter, mParentActivity,
                           sAppQueueFragmentActionLayouts[i], false);
    }
  }

  public IAction[] getActionSet(int set) {
    IAction[] result = null;

    if (set == APPLIST) {
      result = mAppListFragmentActions;
    } else if (set == APPQUEUE) {
      result = mAppQueueFragmentActions;
    }

    return result;
  }
}
