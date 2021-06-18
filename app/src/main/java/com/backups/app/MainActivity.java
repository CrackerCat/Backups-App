package com.backups.app;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.backups.app.data.pojos.APKFile;
import com.backups.app.data.pojos.BackupProgress;
import com.backups.app.data.pojos.PreferenceKeys;
import com.backups.app.data.repositories.BackupRepository;
import com.backups.app.data.viewmodels.ApkListViewModel;
import com.backups.app.data.viewmodels.AppQueueViewModel;
import com.backups.app.data.viewmodels.BackupsViewModel;
import com.backups.app.data.viewmodels.BackupsViewModelFactory;
import com.backups.app.data.viewmodels.DataEvent;
import com.backups.app.data.viewmodels.ItemSelectionState;
import com.backups.app.ui.actionbuttonactions.AppListButtonActions;
import com.backups.app.ui.actionbuttonactions.AppQueueButtonActions;
import com.backups.app.ui.actionbuttonactions.SettingsFragmentButtonActions;
import com.backups.app.ui.actions.ActionPresenter;
import com.backups.app.ui.actions.ActionSetMaker;
import com.backups.app.ui.actions.IActionButtonMethods;
import com.backups.app.ui.actions.IDefaultAction;
import com.backups.app.ui.actions.IPresenter;
import com.backups.app.ui.adapters.TabAdapter;
import com.backups.app.ui.fragments.AppListFragment;
import com.backups.app.ui.fragments.AppQueueFragment;
import com.backups.app.ui.fragments.SettingsFragment;
import com.backups.app.utils.PackageNameUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;
import java.util.Locale;

import static com.backups.app.ui.Constants.APP_LIST;
import static com.backups.app.ui.Constants.APP_QUEUE;
import static com.backups.app.ui.Constants.BACKUP_BUTTON;
import static com.backups.app.ui.Constants.ITEM_SELECTION_BUTTON;
import static com.backups.app.ui.Constants.SEARCH_BUTTON;
import static com.backups.app.ui.Constants.SETTINGS;
import static com.backups.app.ui.Constants.sItemSelectionFMT;

public final class MainActivity extends AppCompatActivity
    implements TabLayout.OnTabSelectedListener,
               SharedPreferences.OnSharedPreferenceChangeListener {

  private static boolean sInitializeValues = true;

  private static String sOutputDirectoryKey;
  private static String sShowSystemAppsKey;
  private static String sAppThemeKey;
  private static String sItemSelectionCountSuffix;
  private static String sBackupErrorMessage;

  private final PreferenceKeys mPreferenceKeys = new PreferenceKeys();

  private ApkListViewModel mApkListViewModel;
  private AppQueueViewModel mAppQueueViewModel;
  private BackupsViewModel mBackupsViewModel;

  private TextView mAppNameTextView;
  private TextView mBackupCounterTextView;

  private Button mCancelSelectionButton;
  private TextView mItemSelectionCountTextView;
  private CheckBox mSelectAllItemsButton;
  private ConstraintLayout mAppQueueItemSelectionView;

  private TabAdapter mTabAdapter;
  private TabLayout mTabLayout;
  private ViewPager2 mViewPager;
  private IPresenter mActionPresenter;

  private IActionButtonMethods mAppListActions;
  private IActionButtonMethods mAppQueueButtonActions;
  private IActionButtonMethods mSettingsFragmentActions;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (sInitializeValues) {
      initializeStringValues();
    }

    loadSettings();

    useDarkTheme(mPreferenceKeys.useDarkTheme);

    initializeViewModels();

    verifyOutputDirectory();

    boolean hasNotScannedForAppsYet =
        mApkListViewModel.getApkListLiveData().getValue() == null;

    if (hasNotScannedForAppsYet) {
      mApkListViewModel.fetchInstalledApps(getPackageManager(),
                                           mPreferenceKeys.showSystemApps);
    }

    initializeViews();

    mAppListActions = new AppListButtonActions(this);

    mAppQueueButtonActions = new AppQueueButtonActions(this, mPreferenceKeys);

    mSettingsFragmentActions = new SettingsFragmentButtonActions(this);

    initializeFAButton();

    restoreViews();

    registerObservers();

    initializeTabLayout();
  }

  private void initializeStringValues() {
    Resources resources = getResources();

    sOutputDirectoryKey = resources.getString(R.string.output_directory_key);
    sShowSystemAppsKey = resources.getString(R.string.show_system_apps_key);
    sAppThemeKey = resources.getString(R.string.current_theme_key);

    sItemSelectionCountSuffix =
        resources.getString(R.string.app_queue_selected_items_fmt);

    sBackupErrorMessage =
        resources.getString(R.string.unable_to_create_backup_message);

    sInitializeValues = false;
  }

  private void initializeViewModels() {
    mApkListViewModel = new ViewModelProvider(this).get(ApkListViewModel.class);

    mAppQueueViewModel =
        new ViewModelProvider(this).get(AppQueueViewModel.class);

    mBackupsViewModel =
        new ViewModelProvider(this, new BackupsViewModelFactory(this))
            .get(BackupsViewModel.class);
  }

  private void loadSettings() {
    SharedPreferences sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(this);

    mPreferenceKeys.outputDirectory =
        Integer.parseInt(sharedPreferences.getString(
            sOutputDirectoryKey, BackupRepository.sPrimaryStorage + ""));

    mPreferenceKeys.showSystemApps =
        sharedPreferences.getBoolean(sShowSystemAppsKey, false);

    mPreferenceKeys.useDarkTheme =
        sharedPreferences.getBoolean(sAppThemeKey, false);
  }

  private void useDarkTheme(final boolean use) {
    AppCompatDelegate.setDefaultNightMode(
        use ? AppCompatDelegate.MODE_NIGHT_YES
            : AppCompatDelegate.MODE_NIGHT_NO);
  }

  private void verifyOutputDirectory() {
    if (mPreferenceKeys.outputDirectory != BackupRepository.sPrimaryStorage) {
      boolean isVolumeAvailable = mBackupsViewModel.setStorageVolumeIndex(
          mPreferenceKeys.outputDirectory);

      if (isVolumeAvailable) {
        final int storageVolumeIndex =
            mBackupsViewModel.getCurrentStorageVolumeIndex();

        mPreferenceKeys.outputDirectory = storageVolumeIndex;

        PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
            .putString(sOutputDirectoryKey, String.valueOf(storageVolumeIndex))
            .apply();
      }
    }
  }

  private void addTabs(TabAdapter tabAdapter) {
    Resources resources = getResources();
    String appTabName = resources.getString(R.string.apps_tab_name);
    String queueTabName = resources.getString(R.string.queue_tab_name);
    String settingsTabName = resources.getString(R.string.settings_tab_name);

    tabAdapter.addTab(appTabName, new AppListFragment());
    tabAdapter.addTab(queueTabName, new AppQueueFragment());
    tabAdapter.addTab(settingsTabName, new SettingsFragment());
  }

  private void initializeViews() {
    mAppNameTextView = findViewById(R.id.main_app_name_header);

    mBackupCounterTextView = findViewById(R.id.main_backup_count_label);

    mCancelSelectionButton = findViewById(R.id.app_queue_cancel_selection_bt);

    mItemSelectionCountTextView =
        findViewById(R.id.app_queue_selection_count_tv);

    mSelectAllItemsButton = findViewById(R.id.app_queue_select_all_cb);

    mAppQueueItemSelectionView = findViewById(R.id.app_queue_item_sv);

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

  private void restoreViews() {
    if (!mAppQueueViewModel.getBackupCountLabel().isEmpty()) {
      mBackupCounterTextView.setText(mAppQueueViewModel.getBackupCountLabel());
    }

    if (mAppQueueViewModel.hasSelectedItems()) {
      mItemSelectionCountTextView.setText(String.format(
          Locale.getDefault(), sItemSelectionFMT,
          mAppQueueViewModel.getSelectionSize(), sItemSelectionCountSuffix));

      mActionPresenter.available(APP_QUEUE, ITEM_SELECTION_BUTTON, true);
    }

    if (mAppQueueViewModel.hasAutomaticallySelectedAll()) {
      mSelectAllItemsButton.setChecked(true);
    }

    mCancelSelectionButton.setOnClickListener(v -> {
      if (mAppQueueViewModel.getCurrentSelectionState().equals(
              ItemSelectionState.SELECTION_STARTED)) {
        mAppQueueViewModel.clearSelection();

        mAppQueueViewModel.setItemSelectionStateTo(
            ItemSelectionState.SELECTION_ENDED);
      }
    });

    mSelectAllItemsButton.setOnClickListener(v -> {
      if (mSelectAllItemsButton.isChecked()) {

        if (!mAppQueueViewModel.hasAutomaticallySelectedAll() &&
            !mAppQueueViewModel.hasManuallySelectedAll()) {

          mAppQueueViewModel.selectAll();
        } else {
          mSelectAllItemsButton.setChecked(false);
        }

      } else {
        mAppQueueViewModel.clearSelection();
      }
    });
  }

  private void registerObservers() {
    /* Any checks against DataEvent/ItemSelectionState.NONE are used only
   to prevent the callback from being handled during Activity recreation
   which is not needed
   */
    mApkListViewModel.getApkListLiveData().observe(this, apkFiles -> {
      if (apkFiles != null && !apkFiles.isEmpty()) {
        makeAppListActionsAvailable(true);
      }
    });

    mBackupsViewModel.getBackupProgressLiveData().observe(
        this, backupProgress -> {
          boolean backupStarted = !backupProgress.getState().equals(
              BackupProgress.ProgressState.NONE);

          if (backupStarted) {
            handleBackupProgress(backupProgress);
          }
        });

    mAppQueueViewModel.getDataEventLiveData().observe(this, dataEvent -> {
      if (!mAppQueueViewModel.getLastDataEvent().equals(DataEvent.NONE)) {
        handleDataEvents(dataEvent);
      }
    });

    mAppQueueViewModel.getSelectionStateLiveData().observe(
        this, itemSelectionState -> {
          if (!mAppQueueViewModel.getCurrentSelectionState().equals(
                  ItemSelectionState.NONE)) {
            handleItemSelectionState(itemSelectionState);
          }
        });
  }

  private void handleBackupProgress(BackupProgress progress) {
    BackupProgress.ProgressState state = progress.getState();

    if (state.equals(BackupProgress.ProgressState.BEGAN)) {
      makeAppListActionsAvailable(false);

      makeAppQueueActionsAvailable(false);

    } else if (state.equals(BackupProgress.ProgressState.ERROR) ||
               state.equals(BackupProgress.ProgressState.FINISHED)) {
      int totalAppsLeft = mAppQueueViewModel.getAppsInQueue().size();

      final String backupName = progress.getBackupName();

      PackageNameUtils.resetCountFor(backupName);

      updateBackupCountView(totalAppsLeft);

      if (state.equals(BackupProgress.ProgressState.ERROR)) {
        Toast
            .makeText(MainActivity.this,
                      String.format(sBackupErrorMessage, backupName),
                      Toast.LENGTH_SHORT)
            .show();
      }

    } else if (state.equals(BackupProgress.ProgressState.ENDED)) {
      mBackupsViewModel.isBackupInProgress(false);

      makeAppListActionsAvailable(true);
    }
  }

  private String startActionButtonChecks() {
    String outputMessage = null;

    boolean hasNotScannedForApps =
        mApkListViewModel.getApkListLiveData().getValue() == null;

    boolean isBackupInProgress = mBackupsViewModel.isBackupInProgress();

    boolean hasNoBackups = mAppQueueViewModel.doesNotHaveBackups();

    Resources resources = getResources();

    if (hasNotScannedForApps) {
      outputMessage = resources.getString(R.string.fetching_data_message);
    } else if (isBackupInProgress) {
      outputMessage = resources.getString(R.string.backup_in_progress_message);
    } else if (hasNoBackups) {
      outputMessage = resources.getString(R.string.no_backups_selected_message);
    }

    return outputMessage;
  }

  private void runActionButtonChecks() {
    final String outputMessage = startActionButtonChecks();

    if (outputMessage != null) {
      Toast.makeText(MainActivity.this, outputMessage, Toast.LENGTH_SHORT)
          .show();
    }
  }

  private void handleDataEvents(DataEvent dataEvent) {
    if (dataEvent.equals(DataEvent.ITEM_ADDED_TO_QUEUE)) {

      List<APKFile> backupsInQueue = mAppQueueViewModel.getAppsInQueue();

      updateBackupCountView(backupsInQueue.size());

      final APKFile recentlyAdded =
          backupsInQueue.get(backupsInQueue.size() - 1);

      mBackupsViewModel.incrementBackupSize(recentlyAdded.getAppSize());

    } else if (dataEvent.equals(DataEvent.ITEM_SELECTED) ||
               dataEvent.equals(DataEvent.ALL_ITEMS_SELECTED)) {

      mItemSelectionCountTextView.setText(String.format(
          Locale.getDefault(), sItemSelectionFMT,
          mAppQueueViewModel.getSelectionSize(), sItemSelectionCountSuffix));

      if (dataEvent.equals(DataEvent.ITEM_SELECTED)) {
        if (mAppQueueViewModel.getSelectionSize() ==
            mAppQueueViewModel.getAppsInQueue().size()) {

          mAppQueueViewModel.hasManuallySelectedAll(true);
        }
      }

      if (!mActionPresenter.isActionAvailable(APP_QUEUE,
                                              ITEM_SELECTION_BUTTON)) {

        mActionPresenter.available(APP_QUEUE, ITEM_SELECTION_BUTTON, true);
      }
    } else if (dataEvent.equals(DataEvent.ITEM_DESELECTED) ||
               dataEvent.equals(DataEvent.ALL_ITEMS_DESELECTED)) {

      int size = mAppQueueViewModel.getSelectionSize();

      if (size != 0) {
        mItemSelectionCountTextView.setText(
            String.format(Locale.getDefault(), sItemSelectionFMT, size,
                          sItemSelectionCountSuffix));
      } else {
        mItemSelectionCountTextView.setText(sItemSelectionCountSuffix);
      }

      if (mAppQueueViewModel.hasManuallySelectedAll()) {

        mAppQueueViewModel.hasManuallySelectedAll(false);

      } else if (mAppQueueViewModel.hasAutomaticallySelectedAll()) {

        mAppQueueViewModel.hasAutomaticallySelectedAll(false);

        mSelectAllItemsButton.setChecked(false);
      }

      if (!mAppQueueViewModel.hasSelectedItems()) {
        mActionPresenter.available(APP_QUEUE, ITEM_SELECTION_BUTTON, false);
      }
    } else if (dataEvent.equals(DataEvent.ITEMS_REMOVED_FROM_QUEUE) ||
               dataEvent.equals(DataEvent.ITEMS_REMOVED_FROM_SELECTION)) {

      if (mAppQueueViewModel.doesNotHaveBackups()) {
        makeAppQueueActionsAvailable(false);
      }
    }
  }

  private void handleItemSelectionState(final ItemSelectionState state) {
    if (state.equals(ItemSelectionState.SELECTION_STARTED)) {
      hideWelcomeView(true);

      hideAppQueueItemSelectionView(false);

    } else if (state.equals(ItemSelectionState.SELECTION_ENDED)) {
      hideWelcomeView(false);

      hideAppQueueItemSelectionView(true);

      updateBackupCountView(mAppQueueViewModel.getAppsInQueue().size());

      if (mSelectAllItemsButton.isChecked()) {
        mSelectAllItemsButton.setChecked(false);
      }

      mItemSelectionCountTextView.setText(sItemSelectionCountSuffix);

      mActionPresenter.available(APP_QUEUE, ITEM_SELECTION_BUTTON, false);
    }
  }

  private int[] getFloatingActionButtonColors() {
    Resources resources = getResources();

    int[] activeColors = new int[2];

    final int activeColor = 0;
    final int inActiveColor = 1;

    activeColors[activeColor] = resources.getColor(R.color.secondaryDarkColor);

    activeColors[inActiveColor] =
        (mPreferenceKeys.useDarkTheme
             ? resources.getColor(R.color.primaryLightColor)
             : resources.getColor(R.color.primaryDarkColor));

    return activeColors;
  }

  private void initializeFAButton() {
    final int[][] APP_LIST_FRAGMENT_ACTION_LAYOUTS =
        new int[][] {{R.id.main_search_label, R.id.main_search_button}};

    final int[][] APP_QUEUE_FRAGMENT_ACTION_LAYOUTS = new int[][] {
        {R.id.app_queue_search_label, R.id.app_queue_search_button},
        {R.id.app_queue_backup_label, R.id.app_queue_backup_button},
        {R.id.app_queue_item_selection_label,
         R.id.app_queue_item_selection_button}};

    final int[][] SETTINGS_FRAGMENT_ACTION_LAYOUTS = new int[][] {
        {R.id.about_us_section_label, R.id.about_us_section_button},
        {R.id.rate_app_label, R.id.rate_app_button},
        {R.id.share_app_label, R.id.share_app_button}};

    final int[] activeColors = getFloatingActionButtonColors();

    mActionPresenter =
        new ActionPresenter(this, R.id.main_floating_action_button);

    IDefaultAction defaultInactiveAction = this::runActionButtonChecks;

    mActionPresenter.addActions(ActionSetMaker.makeActionSet(
        mActionPresenter, this, APP_LIST_FRAGMENT_ACTION_LAYOUTS, activeColors,
        defaultInactiveAction, mAppListActions::initializeActionButtonActions));

    mActionPresenter.addActions(ActionSetMaker.makeActionSet(
        mActionPresenter, this, APP_QUEUE_FRAGMENT_ACTION_LAYOUTS, activeColors,
        defaultInactiveAction,
        mAppQueueButtonActions::initializeActionButtonActions));

    mActionPresenter.addActions(ActionSetMaker.makeActionSet(
        mActionPresenter, this, SETTINGS_FRAGMENT_ACTION_LAYOUTS, activeColors,
        defaultInactiveAction,
        mSettingsFragmentActions::initializeActionButtonActions));

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

    mAppQueueViewModel.setBackupCountLabel(backupCountLabel);
  }

  private void makeAppListActionsAvailable(boolean conditionMet) {
    mActionPresenter.available(APP_LIST, SEARCH_BUTTON, conditionMet);
  }

  private void makeAppQueueActionsAvailable(boolean conditionMet) {
    mActionPresenter.available(APP_QUEUE, SEARCH_BUTTON, conditionMet);
    mActionPresenter.available(APP_QUEUE, BACKUP_BUTTON, conditionMet);
  }

  private void hideWelcomeView(final boolean hide) {
    if (hide) {
      mBackupCounterTextView.setVisibility(View.INVISIBLE);
      mAppNameTextView.setVisibility(View.INVISIBLE);
    } else {
      mAppNameTextView.setVisibility(View.VISIBLE);
      mBackupCounterTextView.setVisibility(View.VISIBLE);
    }
  }

  private void hideAppQueueItemSelectionView(final boolean hide) {
    if (hide) {
      mAppQueueItemSelectionView.setVisibility(View.INVISIBLE);
    } else {
      mAppQueueItemSelectionView.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public void onTabSelected(TabLayout.Tab tab) {
    int position = tab.getPosition();

    if (position == APP_LIST || position == SETTINGS) {
      mActionPresenter.swapActions(position);
    } else if (position == APP_QUEUE) {
      mActionPresenter.swapActions(position);

      boolean hasBackups = !mAppQueueViewModel.doesNotHaveBackups();
      if (hasBackups) {
        makeAppQueueActionsAvailable(true);
      }
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

  public void resetQueue() {
    mAppQueueViewModel.emptyQueue();

    mAppQueueViewModel.setItemSelectionStateTo(
        ItemSelectionState.SELECTION_ENDED);
  }

  private void showSystemApps(final boolean choice) {
    if (!mBackupsViewModel.isBackupInProgress()) {
      mActionPresenter.available(APP_LIST, SEARCH_BUTTON, false);

      if (!mAppQueueViewModel.doesNotHaveBackups()) {
        resetQueue();
      }

      mApkListViewModel.pushNullList();

      mApkListViewModel.fetchInstalledApps(getPackageManager(), choice);
    } else {
      Toast
          .makeText(
              this,
              getResources().getString(R.string.backup_in_progress_message_alt),
              Toast.LENGTH_SHORT)
          .show();
    }
  }

  private void switchTheme(final boolean yesOrNo) {
    if (!mBackupsViewModel.isBackupInProgress()) {
      useDarkTheme(yesOrNo);
    } else {
      Toast
          .makeText(
              this,
              getResources().getString(R.string.backup_in_progress_message_alt),
              Toast.LENGTH_SHORT)
          .show();
    }
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                        String key) {
    if (key.equals(sShowSystemAppsKey)) {
      showSystemApps((mPreferenceKeys.showSystemApps =
                          sharedPreferences.getBoolean(key, false)));
    } else if (key.equals(sAppThemeKey)) {
      switchTheme((mPreferenceKeys.useDarkTheme =
                       sharedPreferences.getBoolean(key, false)));
    }
  }
}
