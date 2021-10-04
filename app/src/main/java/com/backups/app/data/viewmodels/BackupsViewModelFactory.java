package com.backups.app.data.viewmodels;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.backups.app.data.viewmodels.appqueue.AppQueueViewModel;

public final class BackupsViewModelFactory
    implements ViewModelProvider.Factory {
  private final Context mContext;

  public BackupsViewModelFactory(Context context) { mContext = context; }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      return (T) new AppQueueViewModel(mContext);
  }
}