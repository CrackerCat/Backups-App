package com.backups.app.utils;

public interface Callback<T> {
  void onComplete(T result);
}