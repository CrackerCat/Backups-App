package com.backups.app.ui.fragments;

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
import com.backups.app.data.repositories.BackupRepository;
import com.backups.app.data.viewmodels.BackupsViewModel;
import com.backups.app.data.viewmodels.BackupsViewModelFactory;

public class SettingsFragment extends PreferenceFragmentCompat
    implements SharedPreferences.OnSharedPreferenceChangeListener {

  private final int outputDirectoryPrefId = 0;
  private final int appThemePrefId = 2;

  private BackupsViewModel mBackupsViewModel;

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.preferences, rootKey);

    FragmentActivity parent = requireActivity();

    mBackupsViewModel =
        new ViewModelProvider(parent, new BackupsViewModelFactory(parent))
            .get(BackupsViewModel.class);

    loadPreferences();

    PreferenceManager.getDefaultSharedPreferences(requireActivity())
        .registerOnSharedPreferenceChangeListener(this);
  }

  private void loadPreferences() {
    PreferenceScreen preferenceScreen = getPreferenceScreen();

    SharedPreferences sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(requireActivity());

    int showSystemAppsPrefId = 1;

    for (int i = 0, total = preferenceScreen.getPreferenceCount(); i < total;
         ++i) {
      Preference preference = preferenceScreen.getPreference(i);

      if (i == outputDirectoryPrefId) {

        initializeOutputDirectoryPreferences(
            preference, mBackupsViewModel.getAvailableStorageVolumes());

      } else if (i == showSystemAppsPrefId) {

        boolean value =
            sharedPreferences.getBoolean(preference.getKey(), false);

        ((SwitchPreference)preference).setChecked(value);

      } else if (i == appThemePrefId) {

        boolean value =
            sharedPreferences.getBoolean(preference.getKey(), false);

        preference.setSummary(updateThemeSummary(value));

        ((SwitchPreference)preference).setChecked(value);
      }
    }
  }

  private Pair<String[], String[]> makeStorageEntryValues(int total) {
    Resources resources = getResources();
    String formatString =
        resources.getString(R.string.external_storage_selection);

    String[] entries = new String[total];
    String[] values = new String[total];

    entries[BackupRepository.sPrimaryStorage] =
        resources.getString(R.string.internal_storage_selection);

    values[BackupRepository.sPrimaryStorage] =
        BackupRepository.sPrimaryStorage + "";

    boolean foundExternalDrives = total != 1;
    if (foundExternalDrives) {
      // i = 1 to account for only external volumes
      for (int i = 1; i < total; ++i) {
        entries[i] = String.format(formatString, i);
        values[i] = i + "";
      }
    }

    return new Pair<>(entries, values);
  }

  private void initializeOutputDirectoryPreferences(final Preference preference,
                                                    final int totalEntries) {
    ListPreference outputDirectoryPreference = (ListPreference)preference;

    Pair<String[], String[]> result = makeStorageEntryValues(totalEntries);

    outputDirectoryPreference.setEntries(result.first);

    outputDirectoryPreference.setEntryValues(result.second);

    outputDirectoryPreference.setSummary(
        mBackupsViewModel.getStorageVolumePath());
  }

  private void
  updateOutputDirectoryPreference(final Preference preference,
                                  final SharedPreferences sharedPreferences,
                                  final String key) {
    final ListPreference listPreference = (ListPreference)preference;

    final int value = Integer.parseInt(sharedPreferences.getString(
        key, BackupRepository.sPrimaryStorage + ""));

    mBackupsViewModel.setStorageVolumeIndex(value);

    listPreference.setSummary(mBackupsViewModel.getStorageVolumePath());
  }

  private String updateThemeSummary(boolean useDarkTheme) {
    Resources resources = getResources();

    String lightThemeSummary = resources.getString(R.string.light_theme);
    String darkThemeSummary = resources.getString(R.string.dark_theme);

    return (useDarkTheme ? darkThemeSummary : lightThemeSummary);
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
    Preference preference = findPreference(key);
    if (preference != null) {
      final int preferenceId = preference.getOrder();

      if (preferenceId == outputDirectoryPrefId) {

        updateOutputDirectoryPreference(preference, sharedPreferences, key);

      } else if (preferenceId == appThemePrefId) {

        boolean useDarkTheme = sharedPreferences.getBoolean(key, false);

        preference.setSummary(updateThemeSummary(useDarkTheme));
      }
    }
  }
}