package com.backups.app.ui.actions;

import android.view.View;

public interface IAction {
  void assignActiveCallback(View.OnClickListener callback);

  void assignInactiveCallback(View.OnClickListener callback);

  void inactive();

  void active();

  void availability(boolean flag);

  void display(boolean flag);

  boolean canBeDisplayed();
}
