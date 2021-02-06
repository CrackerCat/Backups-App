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

    public ActionButton createAction(final FragmentActivity activity, final ActionButtonConfig config) {
        ActionButton action = new ActionButton(activity, config.getLayoutIDS(), config.isActive());
        action.assignCallback(config.getClickListener());
        return action;
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
            mCurrentActions = mActionSets.get(mActionSets.size());
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
                    for (IAction action : mCurrentActions) {
                        action.display(true);
                    }
                    mIsShowingActions = true;
                } else {
                    for (IAction action : mCurrentActions) {
                        action.display(false);
                    }
                    mIsShowingActions = false;
                }
            });
        }
    }
}
