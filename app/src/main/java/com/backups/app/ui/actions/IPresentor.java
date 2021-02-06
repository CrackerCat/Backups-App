package com.backups.app.ui.actions;

import java.util.List;

public interface IPresentor {
    void addActions(IAction[] actions);

    void swapActions(int setId);

    void available(int actionID, boolean flag);

    void present();
}
