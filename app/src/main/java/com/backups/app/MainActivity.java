package com.backups.app;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.backups.app.data.ApkListViewModel;
import com.backups.app.data.AppQueueViewModel;
import com.backups.app.ui.actions.ActionPresenter;
import com.backups.app.ui.actions.ActionSetMaker;
import com.backups.app.ui.adapters.TabAdapter;
import com.backups.app.ui.fragments.AppListFragment;
import com.backups.app.ui.fragments.AppQueueFragment;
import com.backups.app.ui.fragments.SearchDialogFragment;
import com.backups.app.ui.fragments.SettingsFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import static com.backups.app.ui.Constants.APPLIST;
import static com.backups.app.ui.Constants.APPQUEUE;
import static com.backups.app.ui.Constants.BACKUP_BUTTON;
import static com.backups.app.ui.Constants.SEARCH_BUTTON;
import static com.backups.app.ui.Constants.sAppListFragmentActionLayouts;
import static com.backups.app.ui.Constants.sAppQueueFragmentActionLayouts;

public class MainActivity extends AppCompatActivity
    implements ActionPresenter.IActionAvailability,
               TabLayout.OnTabSelectedListener,
               SharedPreferences.OnSharedPreferenceChangeListener {
  private ApkListViewModel mApkListViewModel;
  private AppQueueViewModel mAppQueueViewModel;

  private TextView mBackupCounterView;
  private TabAdapter mTabAdapter;
  private TabLayout mTabLayout;
  private ViewPager2 mViewPager;
  private ActionPresenter mActionPresenter;
  private final SearchDialogFragment mAppSearchDialogFragment =
      new SearchDialogFragment();

  private final static SettingsKeys sSettings = new SettingsKeys();
  private static String sShowSystemAppsKey;
  private static String sAppThemeKey;
  private static String sAppTabName;
  private static String sQueueTabName;
  private static String sSettingsTabName;
  private static boolean sInitializeValues = true;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    initializeStringValues();

    loadSettings();

    mAppQueueViewModel =
        new ViewModelProvider(this).get(AppQueueViewModel.class);

    mApkListViewModel = new ViewModelProvider(this).get(ApkListViewModel.class);

    if (mApkListViewModel.getApkListLiveData().getValue() == null) {
      mApkListViewModel.fetchInstalledApps(getPackageManager(),
                                           sSettings.showSystemApps);
    }

    initializeViews();

    mAppQueueViewModel.getAppQueueLiveData().observe(this, selection -> {
      if (getLifecycle().getCurrentState() != Lifecycle.State.STARTED) {
        updateBackupCountView(selection.size());
      } else {
        mBackupCounterView.setText(mAppQueueViewModel.getBackupCountLabel());
      }
    });

    initializeFAButton();
    initializeTabLayout();
  }

  private void initializeStringValues() {
    if (sInitializeValues) {
      Resources resources = getResources();

      sShowSystemAppsKey = resources.getString(R.string.show_system_apps_key);
      sAppThemeKey = resources.getString(R.string.current_theme_key);

      sAppTabName = resources.getString(R.string.apps_tab_name);
      sQueueTabName = resources.getString(R.string.queue_tab_name);
      sSettingsTabName = resources.getString(R.string.settings_tab_name);

      sInitializeValues = false;
    }
  }

  private void loadSettings() {
    SharedPreferences sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(this);

    sSettings.showSystemApps =
        sharedPreferences.getBoolean(sShowSystemAppsKey, false);
    sSettings.useDarkTheme = sharedPreferences.getBoolean(sAppThemeKey, false);
  }

  private void addTabs(TabAdapter tabAdapter) {
    tabAdapter.addTab(sAppTabName, new AppListFragment());
    tabAdapter.addTab(sQueueTabName, new AppQueueFragment());
    tabAdapter.addTab(sSettingsTabName, new SettingsFragment());
  }

  private void initializeViews() {
    mBackupCounterView = findViewById(R.id.main_backup_count_label);

    mTabLayout = findViewById(R.id.main_tab_layout);
    mViewPager = findViewById(R.id.main_pager);
  }

  private void initializeTabLayout() {
    mTabAdapter = new TabAdapter(this);
    mTabLayout.addOnTabSelectedListener(this);

    addTabs(mTabAdapter);

    mViewPager.setAdapter(mTabAdapter);

    new TabLayoutMediator(
        mTabLayout, mViewPager,
        (tab, position) -> tab.setText(mTabAdapter.getTabName(position)))
        .attach();
  }

  private ActionSetMaker.CallBackSetup initializeAppListFragmentActions() {
    return (position, action) -> {
      if (position == SEARCH_BUTTON) {
        action.assignCallBacks(
            v
            -> {
              mAppSearchDialogFragment.setDataSetID(APPLIST);
              mAppSearchDialogFragment.show(
                  getSupportFragmentManager(),
                  (mAppSearchDialogFragment.getClass().getSimpleName()));
            },
            v
            -> Toast
                   .makeText(MainActivity.this, R.string.fetching_data_message,
                             Toast.LENGTH_SHORT)
                   .show());
      }
    };
  }

  private ActionSetMaker.CallBackSetup initializeAppQueueFragmentActions() {
    return (position, action) -> {
      if (position == SEARCH_BUTTON) {
        action.assignCallBacks(
            v
            -> {
              mAppSearchDialogFragment.setDataSetID(APPQUEUE);
              mAppSearchDialogFragment.show(
                  getSupportFragmentManager(),
                  (mAppSearchDialogFragment.getClass().getSimpleName()));
            },
            v
            -> Toast
                   .makeText(MainActivity.this, R.string.fetching_data_message,
                             Toast.LENGTH_SHORT)
                   .show());
      } else if (position == BACKUP_BUTTON) {
        action.assignCallBacks(
            v
            -> Toast
                   .makeText(MainActivity.this, R.string.fetching_data_message,
                             Toast.LENGTH_SHORT)
                   .show(),
            v
            -> Toast
                   .makeText(MainActivity.this, R.string.fetching_data_message,
                             Toast.LENGTH_SHORT)
                   .show());
      }
    };
  }

  private void initializeFAButton() {
    mActionPresenter =
        new ActionPresenter(this, R.id.main_floating_action_button);

    mActionPresenter.addActions(ActionSetMaker.makeActionSet(
        mActionPresenter, this, sAppListFragmentActionLayouts,
        initializeAppListFragmentActions()));

    mActionPresenter.addActions(ActionSetMaker.makeActionSet(
        mActionPresenter, this, sAppQueueFragmentActionLayouts,
        initializeAppQueueFragmentActions()));

    mActionPresenter.swapActions(APPLIST);
    mActionPresenter.present();
  }

  private void updateBackupCountView(int selectedItems) {
    String quantityString = getResources().getQuantityString(
        R.plurals.amount_of_backups, selectedItems);

    String backupCountLabel = String.format(quantityString, selectedItems);
    mBackupCounterView.setText(backupCountLabel);

    mAppQueueViewModel.setBackupCountLabel(backupCountLabel);
  }

  @Override
  public int totalAvailableActions() {
    return mActionPresenter.totalAvailableActions();
  }

  @Override
  public int totalAvailableActionSets() {
    return mActionPresenter.totalAvailableActionSets();
  }

  @Override
  public void makeActionAvailable(int actionID, boolean flag) {
    mActionPresenter.available(actionID, flag);
  }

  @Override
  public void makeActionAvailable(int actionSet, int actionID, boolean flag) {
    mActionPresenter.available(actionSet, actionID, flag);
  }

  @Override
  public void onTabSelected(TabLayout.Tab tab) {
    int position = tab.getPosition();

    if (position == APPLIST) {
      mActionPresenter.swapActions(position);
    } else if (position == APPQUEUE) {
      mActionPresenter.swapActions(position);

      if (!mActionPresenter.isActionAvailable(SEARCH_BUTTON)) {
        if (mAppQueueViewModel.hasBackups()) {
          mActionPresenter.available(SEARCH_BUTTON, true);
        }
      }
    }
  }

  @Override
  public void onTabUnselected(TabLayout.Tab tab) {}

  @Override
  public void onTabReselected(TabLayout.Tab tab) {}

  @Override
  protected void onResume() {
    super.onResume();
    PreferenceManager.getDefaultSharedPreferences(this)
        .registerOnSharedPreferenceChangeListener(this);
  }

  @Override
  protected void onStop() {
    super.onStop();
    PreferenceManager.getDefaultSharedPreferences(this)
        .unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                        String key) {
    if (key.equals(sShowSystemAppsKey)) {
      boolean choice = sharedPreferences.getBoolean(key, false);
      sSettings.showSystemApps = choice;

      mActionPresenter.available(APPLIST, SEARCH_BUTTON, false);
      mApkListViewModel.fetchInstalledApps(getPackageManager(), choice);
    }
  }
}
