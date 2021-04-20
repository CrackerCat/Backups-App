package com.backups.app;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.backups.app.data.ApkListViewModel;
import com.backups.app.data.AppQueueViewModel;
import com.backups.app.data.BackupProgress;
import com.backups.app.data.BackupsViewModelFactory;
import com.backups.app.ui.actions.ActionPresenter;
import com.backups.app.ui.actions.ActionSetMaker;
import com.backups.app.ui.adapters.TabAdapter;
import com.backups.app.ui.fragments.AppListFragment;
import com.backups.app.ui.fragments.AppQueueFragment;
import com.backups.app.ui.fragments.SearchDialogFragment;
import com.backups.app.ui.fragments.SettingsFragment;
import com.backups.app.utils.NotificationsUtils;
import com.backups.app.utils.PackageNameUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import static com.backups.app.ui.Constants.APP_LIST;
import static com.backups.app.ui.Constants.APP_QUEUE;
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

  private TextView mBackupCounterTextView;
  private TabAdapter mTabAdapter;
  private TabLayout mTabLayout;
  private ViewPager2 mViewPager;
  private ActionPresenter mActionPresenter;
  private final SearchDialogFragment mAppSearchDialogFragment =
      new SearchDialogFragment();

  private static String sShowSystemAppsKey;
  private static String sAppThemeKey;

  private static boolean sInitializeValues = true;
  private static String sAppTabName;
  private static String sQueueTabName;
  private static String sSettingsTabName;
  private static String sStartingBackupMessage;
  private static String sBackupErrorMessage;
  private static String sBackupCreatedMessage;
  private static String sFetchingAPKDataMessage;
  private static String sInsufficientStorageMessage;
  private static String sNoBackupsSelectedMessage;
  private static String sStorageLocationNotPresentMessage;
  private static String sBackupInProgressMessage;

  private static class SettingsKeys {
    public boolean showSystemApps;
    public boolean useDarkTheme;
  }

  private final SettingsKeys sSettings = new SettingsKeys();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (sInitializeValues) {
      initializeStringValues();
    }

    loadSettings();

    mAppQueueViewModel =
        new ViewModelProvider(this, new BackupsViewModelFactory(this))
            .get(AppQueueViewModel.class);

    mApkListViewModel = new ViewModelProvider(this).get(ApkListViewModel.class);

    boolean hasNotScannedForAppsYet =
        mApkListViewModel.getApkListLiveData().getValue() == null;
    if (hasNotScannedForAppsYet) {
      mApkListViewModel.fetchInstalledApps(getPackageManager(),
                                           sSettings.showSystemApps);
    }

    initializeViews();

    // else branch is used to restore backup counter string after
    // Activity is created
    mAppQueueViewModel.getAppQueueLiveData().observe(this, queue -> {
      if (getLifecycle().getCurrentState() != Lifecycle.State.STARTED) {
        updateBackupCountView(queue.size());
      } else {
        mBackupCounterTextView.setText(
            mAppQueueViewModel.getBackupCountLabel());
      }
    });

    mAppQueueViewModel.getBackupProgressLiveData().observe(
        this, this::handleBackupProgress);

    initializeFAButton();

    initializeTabLayout();
  }

  private void initializeStringValues() {
    Resources resources = getResources();

    sStartingBackupMessage =
        resources.getString(R.string.commencing_backup_message);

    sBackupErrorMessage =
        resources.getString(R.string.unable_to_create_backup_message);

    sBackupCreatedMessage =
        resources.getString(R.string.backup_created_message);

    sFetchingAPKDataMessage =
        resources.getString(R.string.fetching_data_message);

    sInsufficientStorageMessage =
        resources.getString(R.string.insufficient_storage_message);

    sNoBackupsSelectedMessage =
        resources.getString(R.string.no_backups_selected_message);

    sStorageLocationNotPresentMessage =
        resources.getString(R.string.storage_volume_not_present);

    sBackupInProgressMessage =
        resources.getString(R.string.backup_in_progress_message);

    sShowSystemAppsKey = resources.getString(R.string.show_system_apps_key);
    sAppThemeKey = resources.getString(R.string.current_theme_key);

    sAppTabName = resources.getString(R.string.apps_tab_name);
    sQueueTabName = resources.getString(R.string.queue_tab_name);
    sSettingsTabName = resources.getString(R.string.settings_tab_name);

    sInitializeValues = false;
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
    mBackupCounterTextView = findViewById(R.id.main_backup_count_label);

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

  private void handleBackupProgress(BackupProgress callback) {
    BackupProgress.ProgressState state = callback.state;

    boolean finished = state.equals(BackupProgress.ProgressState.ERROR) ||
                       state.equals(BackupProgress.ProgressState.FINISHED);

    if (finished) {
      int totalApps = mAppQueueViewModel.getSelectedApps().size();

      updateBackupCountView(totalApps);

      PackageNameUtils.resetCountFor(callback.backupName);

      Toast
          .makeText(
              MainActivity.this,
              String.format((state.equals(BackupProgress.ProgressState.ERROR)
                                 ? sBackupErrorMessage
                                 : sBackupCreatedMessage),
                            callback.backupName),
              Toast.LENGTH_SHORT)
          .show();
    }
  }

  private Pair<Boolean, String> startPreBackupChecks() {
    boolean isBackupInProgress = mAppQueueViewModel.isBackupInProgress();
    boolean doesNotHaveSufficientStorage =
        !mAppQueueViewModel.hasSufficientStorage();

    String outputMessage;

    boolean canStartBackup = false;

    // TODO: Implement setting for choosing where backups are outputted to
    // TODO: Added check to make sure storage volume is available

    if (isBackupInProgress) {
      outputMessage = sBackupInProgressMessage;
    } else if (doesNotHaveSufficientStorage) {
      outputMessage = sInsufficientStorageMessage;
    } else {
      outputMessage = String.format(
          sStartingBackupMessage, mAppQueueViewModel.getSelectedApps().size());

      canStartBackup = true;
    }

    return (new Pair<>(canStartBackup, outputMessage));
  }

  private String startPreBackupActionChecks() {
    String outputMessage = "";

    boolean hasNotScannedForApps =
        mApkListViewModel.getApkListLiveData().getValue() == null;
    boolean hasNoBackups = !mAppQueueViewModel.hasBackups();

    if (hasNotScannedForApps) {
      outputMessage = sFetchingAPKDataMessage;
    } else if (hasNoBackups) {
      outputMessage = sNoBackupsSelectedMessage;
    }

    return outputMessage;
  }

  private ActionSetMaker.CallBackSetup initializeAppListFragmentActions() {
    return (position, action) -> {
      if (position == SEARCH_BUTTON) {

        action.assignCallBacks(
            v
            -> {
              mAppSearchDialogFragment.setDataSetID(
                  SearchDialogFragment.DataSet.APP_LIST);

              mAppSearchDialogFragment.show(
                  getSupportFragmentManager(),
                  (mAppSearchDialogFragment.getClass().getSimpleName()));
            },
            v
            -> NotificationsUtils.checkAndNotify(
                MainActivity.this, this::startPreBackupActionChecks));
      }
    };
  }

  private ActionSetMaker.CallBackSetup initializeAppQueueFragmentActions() {
    return (position, action) -> {
      if (position == SEARCH_BUTTON) {

        action.assignCallBacks(
            v
            -> {
              mAppSearchDialogFragment.setDataSetID(
                  SearchDialogFragment.DataSet.APP_QUEUE);

              mAppSearchDialogFragment.show(
                  getSupportFragmentManager(),
                  (mAppSearchDialogFragment.getClass().getSimpleName()));
            },
            v
            -> NotificationsUtils.checkAndNotify(
                MainActivity.this, this::startPreBackupActionChecks));

      } else if (position == BACKUP_BUTTON) {

        action.assignCallBacks(
            v
            -> {
              if (NotificationsUtils.checkNotifyAndGetResult(
                      MainActivity.this, this::startPreBackupChecks)) {

                makeAppListActionsAvailable(false);

                makeAppQueueActionsAvailable(false);

                mAppQueueViewModel.startBackup();
              }
            },
            v
            -> NotificationsUtils.checkAndNotify(
                MainActivity.this, this::startPreBackupActionChecks));
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

    mActionPresenter.swapActions(APP_LIST);
    mActionPresenter.present();
  }

  private void updateBackupCountView(int selectedItems) {
    String backupCountLabel = "";

    if (selectedItems != 0) {
      String quantityString = getResources().getQuantityString(
          R.plurals.amount_of_backups, selectedItems);

      backupCountLabel = String.format(quantityString, selectedItems);
    }

    mBackupCounterTextView.setText(backupCountLabel);

    if (!backupCountLabel.isEmpty()) {
      mAppQueueViewModel.setBackupCountLabel(backupCountLabel);
    }
  }

  private void makeAppListActionsAvailable(boolean conditionMet) {
    mActionPresenter.available(APP_LIST, SEARCH_BUTTON, conditionMet);
  }

  private void makeAppQueueActionsAvailable(boolean conditionMet) {
    mActionPresenter.available(APP_QUEUE, SEARCH_BUTTON, conditionMet);
    mActionPresenter.available(APP_QUEUE, BACKUP_BUTTON, conditionMet);
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

    boolean makeActionsAvailable = mAppQueueViewModel.hasBackups() &&
                                   !mAppQueueViewModel.isBackupInProgress();

    if (position == APP_LIST) {
      mActionPresenter.swapActions(position);

      makeAppListActionsAvailable(makeActionsAvailable);
    } else if (position == APP_QUEUE) {
      mActionPresenter.swapActions(position);

      makeAppQueueActionsAvailable(makeActionsAvailable);
    }
  }

  @Override
  public void onTabUnselected(TabLayout.Tab tab) {}

  @Override
  public void onTabReselected(TabLayout.Tab tab) {}

  @Override
  protected void onResume() {
    PreferenceManager.getDefaultSharedPreferences(this)
        .registerOnSharedPreferenceChangeListener(this);

    super.onResume();
  }

  @Override
  protected void onStop() {
    PreferenceManager.getDefaultSharedPreferences(this)
        .unregisterOnSharedPreferenceChangeListener(this);

    super.onStop();
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                        String key) {
    if (key.equals(sShowSystemAppsKey)) {
      boolean choice = sharedPreferences.getBoolean(key, false);
      sSettings.showSystemApps = choice;

      mActionPresenter.available(APP_LIST, SEARCH_BUTTON, false);
      mApkListViewModel.fetchInstalledApps(getPackageManager(), choice);
    }
  }
}
