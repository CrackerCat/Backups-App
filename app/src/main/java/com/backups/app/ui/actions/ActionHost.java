package com.backups.app.ui.actions;

public interface ActionHost {
  boolean isActionAvailable(final int actionID);
  boolean isActionAvailable(final int actionSet, final int actionID);

  void makeActionAvailable(final int actionID, final boolean flag);
  void makeActionAvailable(final int actionSet, final int actionID,
                           final boolean flag);
}
