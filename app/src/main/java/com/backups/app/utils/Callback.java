package com.backups.app.utils;

public interface Callback<T> {
  void invoke(T result);
}