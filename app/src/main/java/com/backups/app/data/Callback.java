package com.backups.app.data;

public interface Callback<T> {
  void onComplete(T result);
}
