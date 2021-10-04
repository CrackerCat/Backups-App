package com.backups.app.ui.actions;

public interface IPresenter {
  int NO_ACTIONS = -1;

  void addActions(IAction[] actions);

  void swapActions(final int setId);

  void available(final int actionID, final boolean flag);

  void available(final int actionSet, final int actionID, final boolean flag);

  boolean isActionAvailable(final int actionID);

  boolean isActionAvailable(final int actionSet, final int actionID);

  void present();

  void hideActions();

  int totalAvailableActions();

  int totalAvailableActionSets();
}
