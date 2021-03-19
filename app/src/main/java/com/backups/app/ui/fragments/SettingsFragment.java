package com.backups.app.ui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import com.backups.app.R;

public class SettingsFragment extends PreferenceFragmentCompat
    implements SharedPreferences.OnSharedPreferenceChangeListener {

  private static final int sShowSystemAppsPrefId = 1;
  private static final int sAppThemePrefId = 2;

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.preferences, rootKey);

    loadPreferences();

    PreferenceManager.getDefaultSharedPreferences(requireActivity())
        .registerOnSharedPreferenceChangeListener(this);
  }

  private void loadPreferences() {
    PreferenceScreen preferenceScreen = getPreferenceScreen();
    SharedPreferences sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(requireActivity());

    for (int i = 0, total = preferenceScreen.getPreferenceCount(); i < total;
         ++i) {
      Preference preference = preferenceScreen.getPreference(i);

      if (i == sShowSystemAppsPrefId || i == sAppThemePrefId) {
        boolean value =
            sharedPreferences.getBoolean(preference.getKey(), false);
        ((SwitchPreference)preference).setChecked(value);
      }
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    PreferenceManager.getDefaultSharedPreferences(requireActivity())
        .registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    PreferenceManager.getDefaultSharedPreferences(requireActivity())
        .unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                        String key) {
    Preference preference = findPreference(key);
    if (preference != null) {
      if (preference.getOrder() == sAppThemePrefId) {
        boolean value = sharedPreferences.getBoolean(key, false);

        String themeString;

        if (!value) {
          themeString = getResources().getString(R.string.light_theme);
        } else {
          themeString = getResources().getString(R.string.dark_theme);
        }

        preference.setSummary(themeString);
      }
    }
  }
}