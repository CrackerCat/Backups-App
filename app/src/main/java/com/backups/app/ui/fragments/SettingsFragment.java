package com.backups.app.ui.fragments;

import static com.backups.app.Constants.PRIMARY_STORAGE;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import com.backups.app.R;
import com.backups.app.data.viewmodels.BackupsViewModelFactory;
import com.backups.app.data.viewmodels.appqueue.AppQueueViewModel;

public final class SettingsFragment extends PreferenceFragmentCompat
    implements SharedPreferences.OnSharedPreferenceChangeListener {

  private final int outputDirectoryPrefId = 0;
  private final int appThemePrefId = 2;
  private AppQueueViewModel mAppQueueViewModel;

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.preferences, rootKey);

    final FragmentActivity parent = requireActivity();

    mAppQueueViewModel =
        new ViewModelProvider(this, new BackupsViewModelFactory(parent))
            .get(AppQueueViewModel.class);

    setupPreferences();

    PreferenceManager.getDefaultSharedPreferences(requireActivity())
        .registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onResume() {
    super.onResume();

    PreferenceManager.getDefaultSharedPreferences(requireActivity())
        .registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onPause() {
    PreferenceManager.getDefaultSharedPreferences(requireActivity())
        .unregisterOnSharedPreferenceChangeListener(this);

    super.onPause();
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                        String key) {
    final Preference preference = findPreference(key);

    if (preference != null) {
      final int preferenceId = preference.getOrder();

      if (preferenceId == outputDirectoryPrefId) {
        updateOutputDirectoryPreference(preference, sharedPreferences, key);

      } else if (preferenceId == appThemePrefId) {
        updateAppThemePreference(sharedPreferences, key, preference);
      }
    }
  }

  private void setupPreferences() {
    final PreferenceScreen preferenceScreen = getPreferenceScreen();

    final SharedPreferences sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(requireActivity());

    final int showSystemAppsPrefId = 1;

    for (int preferenceIndex = 0,
             preferenceCount = preferenceScreen.getPreferenceCount();
         preferenceIndex < preferenceCount; ++preferenceIndex) {

      final Preference preference =
          preferenceScreen.getPreference(preferenceIndex);

      if (preferenceIndex == outputDirectoryPrefId) {
        initializeOutputDirectoryPreferences(preference);

      } else if (preferenceIndex == showSystemAppsPrefId) {

        final SwitchPreference appThemePreference =
            (SwitchPreference)preference;

        setupShowSystemAppsPreference(sharedPreferences, appThemePreference);

      } else if (preferenceIndex == appThemePrefId) {

        final SwitchPreference appThemePreference =
            (SwitchPreference)preference;

        setupAppThemePreference(sharedPreferences, appThemePreference);
      }
    }
  }

  private void
  setupAppThemePreference(final SharedPreferences sharedPreferences,
                          final SwitchPreference appThemePreference) {
    final boolean value =
        sharedPreferences.getBoolean(appThemePreference.getKey(), false);

    appThemePreference.setSummary(updateThemeSummary(value));

    appThemePreference.setChecked(value);
  }

  private void
  setupShowSystemAppsPreference(final SharedPreferences sharedPreferences,
                                final SwitchPreference appThemePreference) {
    final boolean value =
        sharedPreferences.getBoolean(appThemePreference.getKey(), false);

    appThemePreference.setChecked(value);
  }

  private void
  initializeOutputDirectoryPreferences(final Preference preference) {
    final ListPreference outputDirectoryPreference = (ListPreference)preference;

    final Pair<String[], String[]> result =
        mAppQueueViewModel.getStorageEntryValues(
            getString(R.string.internal_storage_selection),
            getString(R.string.external_storage_selection));

    outputDirectoryPreference.setEntries(result.first);

    outputDirectoryPreference.setEntryValues(result.second);

    outputDirectoryPreference.setSummary(
        mAppQueueViewModel.getOutputDirectoryPath());
  }

  private void
  updateOutputDirectoryPreference(final Preference preference,
                                  final SharedPreferences sharedPreferences,
                                  final String key) {
    final ListPreference listPreference = (ListPreference)preference;

    final int value = Integer.parseInt(
        sharedPreferences.getString(key, PRIMARY_STORAGE + ""));

    mAppQueueViewModel.setStorageVolumeIndex(value);

    listPreference.setSummary(mAppQueueViewModel.getOutputDirectoryPath());
  }

  private String updateThemeSummary(boolean useDarkTheme) {
    Resources resources = getResources();

    String lightThemeSummary = resources.getString(R.string.light_theme);
    String darkThemeSummary = resources.getString(R.string.dark_theme);

    return (useDarkTheme ? darkThemeSummary : lightThemeSummary);
  }

  private void
  updateAppThemePreference(final SharedPreferences sharedPreferences,
                           final String key, final Preference preference) {
    boolean useDarkTheme = sharedPreferences.getBoolean(key, false);

    preference.setSummary(updateThemeSummary(useDarkTheme));
  }
}