package com.backups.app.ui.actions;

public interface IPresenter {
  void addActions(IAction[] actions);

  void swapActions(int setId);

  void available(int actionID, boolean flag);

  void available(int actionSet, int actionID, boolean flag);

  boolean isActionAvailable(int actionID);

  boolean isActionAvailable(int actionSet, int actionID);

  void present();

  void hideActions();

  int totalAvailableActions();

  int totalAvailableActionSets();
}
