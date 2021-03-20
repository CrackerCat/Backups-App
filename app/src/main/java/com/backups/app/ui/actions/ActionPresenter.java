package com.backups.app.ui.actions;

import androidx.annotation.IdRes;
import androidx.fragment.app.FragmentActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.HashMap;

public final class ActionPresenter implements IPresenter {

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
    int totalAvailableActionSets();

    void makeActionAvailable(int actionID, boolean flag);
    void makeActionAvailable(int actionSet, int actionID, boolean flag);
  }

  @Override
  public void addActions(IAction[] actions) {
    if (actions != null) {
      for (IAction action : actions) {
        if (!action.canBeDisplayed()) {
          return;
        }
      }
      mActionSets.put((mActionSets.size()), actions);
    }
  }

  @Override
  public void swapActions(int setId) {
    hideActions();
    mCurrentActions = mActionSets.get(setId);
  }

  @Override
  public void available(int action, boolean flag) {
    boolean canBeMadeAvailable = action >= 0 && mCurrentActions != null &&
                                 action < mCurrentActions.length;
    if (canBeMadeAvailable) {
      if (!mCurrentActions[action].getAvailability()) {
        (mCurrentActions[action]).availability(flag);
      }
    }
  }

  @Override
  public void available(int actionSet, int actionID, boolean flag) {
    IAction[] set = mActionSets.get(actionSet);
    if (set != null) {
      boolean canBeMadeAvailable = actionID >= 0 && actionID < set.length &&
                                   !set[actionID].getAvailability();
      if (canBeMadeAvailable) {
        set[actionID].availability(flag);
      }
    }
  }

  @Override
  public boolean isActionAvailable(int actionID) {
    boolean exists = actionID >= 0 && mCurrentActions != null &&
                     actionID < mCurrentActions.length;

    if (exists) {
      return mCurrentActions[actionID].getAvailability();
    }

    return exists;
  }

  @Override
  public boolean isActionAvailable(int actionSet, int actionID) {
    IAction[] set = mActionSets.get(actionSet);
    if (set != null) {
      boolean exists = actionID >= 0 && actionID < set.length;
      if (exists) {
        return set[actionID].getAvailability();
      }
    }
    return false;
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
    if (mIsShowingActions) {
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

  @Override
  public int totalAvailableActionSets() {
    return mActionSets.size();
  }
}
