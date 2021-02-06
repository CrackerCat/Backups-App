package com.backups.app.ui.actions;

import android.view.View;

public interface IAction {
    void assignCallback(View.OnClickListener callback);

    void inactive();

    void availability(boolean flag);

    void display(boolean flag);

    boolean canBeDisplayed();
}
