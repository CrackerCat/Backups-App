package com.backups.app.ui.actions;

import androidx.annotation.IdRes;
import androidx.fragment.app.FragmentActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.HashMap;

public final class ActionPresenter implements IPresentor {

  private final FloatingActionButton mParentButton;
  private final HashMap<Integer, IAction[]> mActionSets = new HashMap<>();
  private boolean mIsPresenting = false;
  private boolean mIsShowingActions = false;
  private IAction[] mCurrentActions = null;

  public ActionPresenter(final FragmentActivity view, final @IdRes int uiID) {
    mParentButton = view.findViewById(uiID);
  }

  public interface IActionAvailability {
    int totalAvailableActions();
    void makeActionAvailable(int actionID, boolean flag);
  }

  @Override
  public void addActions(IAction[] actions) {
    if (actions != null) {
      for (IAction action : actions) {
        if (!action.canBeDisplayed()) {
          return;
        }
      }

      mActionSets.put((mActionSets.size() + 1), actions);
      mCurrentActions = actions;
    }
  }

  @Override
  public void swapActions(int setId) {
    mCurrentActions = mActionSets.get(setId);
  }

  @Override
  public void available(int action, boolean flag) {
    if (action >= 0 && action < mCurrentActions.length) {
      (mCurrentActions[action]).availability(flag);
    }
  }

  @Override
  public void present() {
    if (!mIsPresenting && mParentButton != null) {

      mIsPresenting = true;

      mParentButton.setOnClickListener(view -> {
        if (!mIsShowingActions) {
          mIsShowingActions = true;
          for (IAction action : mCurrentActions) {
            action.display(mIsShowingActions);
          }
        } else {
          mIsShowingActions = false;
          for (IAction action : mCurrentActions) {
            action.display(mIsShowingActions);
          }
        }
      });
    }
  }

  @Override
  public void hideActions() {
    if (mCurrentActions != null && mCurrentActions.length != 0) {
      mIsShowingActions = false;
      for (IAction action : mCurrentActions) {
        action.display(mIsShowingActions);
      }
    }
  }

  @Override
  public int totalAvailableActions() {
    return (mCurrentActions != null ? mCurrentActions.length : 0);
  }
}
