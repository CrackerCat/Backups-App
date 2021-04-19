package com.backups.app.data;

import android.content.Context;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class BackupsViewModelFactory implements ViewModelProvider.Factory {
  private final Context mContext;

  public BackupsViewModelFactory(Context context) { mContext = context; }

  @Override
  public <T extends ViewModel> T create(Class<T> modelClass) {
    return (T) new AppQueueViewModel(mContext);
  }
}