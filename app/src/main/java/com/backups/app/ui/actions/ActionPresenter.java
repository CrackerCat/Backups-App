package com.backups.app.ui.actions;

import androidx.annotation.IdRes;
import androidx.fragment.app.FragmentActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;

public final class ActionPresenter implements IPresenter {

  private boolean mIsPresenting = false;
  private boolean mIsShowingActions = false;
  private IAction[] mCurrentActions = null;
  private final FloatingActionButton mParentButton;
  private final ArrayList<IAction[]> mActionSets = new ArrayList<>();

  public ActionPresenter(final FragmentActivity view, final @IdRes int uiID) {
    mParentButton = view.findViewById(uiID);
  }

  @Override
  public void addActions(IAction[] actions) {
    if (actions != null) {
      for (IAction action : actions) {
        if (!action.canBeDisplayed()) {
          return;
        }
      }
      mActionSets.add(actions);
    }
  }

  @Override
  public void swapActions(final int setId) {
    hideActions();

    if (setId == IPresenter.NO_ACTIONS) {
      mCurrentActions = null;
    } else {
      mCurrentActions = mActionSets.get(setId);
    }
  }

  @Override
  public void available(final int action, final boolean flag) {
    boolean canBeMadeAvailable = mCurrentActions != null && action >= 0 &&
                                 action < mCurrentActions.length;
    if (canBeMadeAvailable) {
      if (!mCurrentActions[action].getAvailability()) {
        (mCurrentActions[action]).setAvailability(flag);
      }
    }
  }

  @Override
  public void available(final int actionSet, final int actionID,
                        final boolean flag) {
    IAction[] set = mActionSets.get(actionSet);

    if (set != null) {
      boolean canBeMadeAvailable = actionID >= 0 && actionID < set.length;

      if (canBeMadeAvailable) {
        set[actionID].setAvailability(flag);
      }
    }
  }

  @Override
  public boolean isActionAvailable(final int actionID) {
    boolean exists = actionID >= 0 && mCurrentActions != null &&
                     actionID < mCurrentActions.length;

    if (exists) {
      return mCurrentActions[actionID].getAvailability();
    }

    return false;
  }

  @Override
  public boolean isActionAvailable(final int actionSet, final int actionID) {
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
        if (mCurrentActions != null) {
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
