package com.backups.app;

import static com.backups.app.Constants.APP_LIST;
import static com.backups.app.Constants.APP_QUEUE;
import static com.backups.app.Constants.BACKUP_BUTTON;
import static com.backups.app.Constants.ITEM_SELECTION_BUTTON;
import static com.backups.app.Constants.ITEM_SELECTION_FMT;
import static com.backups.app.Constants.PRIMARY_STORAGE;
import static com.backups.app.Constants.SEARCH_BUTTON;
import static com.backups.app.Constants.SETTINGS;

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
import com.backups.app.data.events.DataEvent;
import com.backups.app.data.events.SelectionState;
import com.backups.app.data.pojos.BackupProgress;
import com.backups.app.data.viewmodels.BackupsViewModelFactory;
import com.backups.app.data.viewmodels.apklist.ApkListViewModel;
import com.backups.app.data.viewmodels.appqueue.AppQueueViewModel;
import com.backups.app.ui.actions.ActionButtonsConfig;
import com.backups.app.ui.actions.ActionPresenter;
import com.backups.app.ui.actions.ActionSetMaker;
import com.backups.app.ui.actions.IActionSetFunctionality;
import com.backups.app.ui.actions.IPresenter;
import com.backups.app.ui.actionsets.AppListActions;
import com.backups.app.ui.actionsets.AppQueueActions;
import com.backups.app.ui.actionsets.SettingsActions;
import com.backups.app.ui.adapters.TabAdapter;
import com.backups.app.ui.fragments.AppListFragment;
import com.backups.app.ui.fragments.AppQueueFragment;
import com.backups.app.ui.fragments.SettingsFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.Locale;

public final class MainActivity extends AppCompatActivity
    implements TabLayout.OnTabSelectedListener,
               SharedPreferences.OnSharedPreferenceChangeListener {

  private static final class SettingsKeys {
    public int outputDirectory;
    public boolean showSystemApps;
    public boolean useDarkTheme;
  }

  private final SettingsKeys mPreferenceKeys = new SettingsKeys();

  private ApkListViewModel mApkListViewModel;
  private AppQueueViewModel mAppQueueViewModel;

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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    initializeViewModels();

    loadSettings();

    switchThemes(mPreferenceKeys.useDarkTheme);

    verifyOutputDirectory();

    fetchApkData();

    initializeViews();

    initializeFAButton();

    initializeTabLayout();

    restoreViews();

    registerObservers();
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

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                        String key) {
    if (key.equals(getString(R.string.show_system_apps_key))) {
      showSystemApps((mPreferenceKeys.showSystemApps =
                          sharedPreferences.getBoolean(key, false)));

    } else if (key.equals(getString(R.string.current_theme_key))) {
      switchTheme((mPreferenceKeys.useDarkTheme =
                       sharedPreferences.getBoolean(key, false)));
    }
  }

  private void loadSettings() {
    SharedPreferences sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(this);

    mPreferenceKeys.outputDirectory =
        Integer.parseInt(sharedPreferences.getString(
            getString(R.string.output_directory_key), PRIMARY_STORAGE + ""));

    mPreferenceKeys.showSystemApps = sharedPreferences.getBoolean(
        getString(R.string.show_system_apps_key), false);

    mPreferenceKeys.useDarkTheme = sharedPreferences.getBoolean(
        getString(R.string.current_theme_key), false);
  }

  private void switchThemes(final boolean useDarkMode) {
    AppCompatDelegate.setDefaultNightMode(
        useDarkMode ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO);
  }

  private void verifyOutputDirectory() {
    if (mPreferenceKeys.outputDirectory != PRIMARY_STORAGE) {
      boolean isVolumeAvailable = mAppQueueViewModel.setStorageVolumeIndex(
          mPreferenceKeys.outputDirectory);

      if (isVolumeAvailable) {
        final int storageVolumeIndex =
            mAppQueueViewModel.getCurrentStorageVolumeIndex();

        mPreferenceKeys.outputDirectory = storageVolumeIndex;

        PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
            .putString(getString(R.string.output_directory_key),
                       String.valueOf(storageVolumeIndex))
            .apply();
      }
    }
  }

  private void initializeViewModels() {
    mAppQueueViewModel =
        new ViewModelProvider(this, new BackupsViewModelFactory(this))
            .get(AppQueueViewModel.class);

    mApkListViewModel = new ViewModelProvider(this).get(ApkListViewModel.class);
  }

  private void fetchApkData() {
    if (mApkListViewModel.hasNotScannedForApps()) {
      mApkListViewModel.fetchInstalledApps(getPackageManager(),
                                           mPreferenceKeys.showSystemApps);
    }
  }

  private void addTabs(final TabAdapter tabAdapter) {
    final Resources resources = getResources();

    String appTabName = resources.getString(R.string.apps_tab_name);

    String queueTabName = resources.getString(R.string.queue_tab_name);

    String settingsTabName = resources.getString(R.string.settings_tab_name);

    tabAdapter.addTab(appTabName, new AppListFragment());
    tabAdapter.addTab(queueTabName, new AppQueueFragment());
    tabAdapter.addTab(settingsTabName, new SettingsFragment());
  }

  private void initializeBackupSelectionViews() {
    mCancelSelectionButton = findViewById(R.id.app_queue_cancel_selection_bt);

    mItemSelectionCountTextView =
        findViewById(R.id.app_queue_selection_count_tv);

    mSelectAllItemsButton = findViewById(R.id.app_queue_select_all_cb);

    mAppQueueItemSelectionView = findViewById(R.id.app_queue_item_sv);
  }

  private void initializeViews() {
    mAppNameTextView = findViewById(R.id.main_app_name_header);

    mBackupCounterTextView = findViewById(R.id.main_backup_count_label);

    initializeBackupSelectionViews();

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

  private void restoreBackupCounterView() {
    if (!mAppQueueViewModel.getBackupCountLabel().isEmpty()) {
      mBackupCounterTextView.setText(mAppQueueViewModel.getBackupCountLabel());
    }

    if (mAppQueueViewModel.hasSelectedItems()) {
      mItemSelectionCountTextView.setText(
          String.format(Locale.getDefault(), ITEM_SELECTION_FMT,
                        mAppQueueViewModel.getSelectionSize(),
                        getString(R.string.app_queue_selected_items_fmt)));

      mActionPresenter.available(APP_QUEUE, ITEM_SELECTION_BUTTON, true);
    }
  }

  private void onCancelSelectionButtonPressed() {
    if (mAppQueueViewModel.getCurrentSelectionState() ==
        SelectionState.SELECTION_STARTED) {
      mAppQueueViewModel.clearSelection();

      mAppQueueViewModel.setItemSelectionStateTo(
          SelectionState.SELECTION_ENDED);
    }
  }

  private void onSelectAllButtonPressed() {
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
  }

  private void restoreBackupSelectionView() {
    if (mAppQueueViewModel.hasAutomaticallySelectedAll()) {
      mSelectAllItemsButton.setChecked(true);
    }

    mCancelSelectionButton.setOnClickListener(
        v -> onCancelSelectionButtonPressed());

    mSelectAllItemsButton.setOnClickListener(v -> onSelectAllButtonPressed());
  }

  private void restoreViews() {
    restoreBackupCounterView();

    restoreBackupSelectionView();
  }

  private void onBackupComplete() {
    final int totalAppsLeft = mAppQueueViewModel.getAppsInQueue().size();

    updateBackupCountView(totalAppsLeft);

    mAppQueueViewModel.isBackupInProgress(false);

    makeAppListActionsAvailable();

    if (totalAppsLeft == 0) {
      mAppQueueViewModel.isBackupInProgress(false);

      makeAppQueueActionsAvailable(false);

      makeAppListActionsAvailable();
    }
  }

  private void handleBackupProgress(final BackupProgress progress) {
    if (progress.finished()) {
      onBackupComplete();
    }
  }

  private String startPreBackupActionChecks() {
    String outputMessage = null;

    final Resources resources = getResources();

    if (mApkListViewModel.hasNotScannedForApps()) {
      outputMessage = resources.getString(R.string.fetching_data_message);

    } else if (mAppQueueViewModel.isBackupInProgress()) {
      outputMessage = resources.getString(R.string.backup_in_progress_message);

    } else if (mAppQueueViewModel.doesNotHaveBackups()) {
      outputMessage = resources.getString(R.string.no_backups_selected_message);
    }

    return outputMessage;
  }

  private void runActionButtonChecks() {
    final String outputMessage = startPreBackupActionChecks();

    if (outputMessage != null) {
      Toast.makeText(MainActivity.this, outputMessage, Toast.LENGTH_SHORT)
          .show();
    }
  }

  private void onBackupSelected(final DataEvent dataEvent) {
    final String updateSelectionCount =
        String.format(Locale.getDefault(), ITEM_SELECTION_FMT,
                      mAppQueueViewModel.getSelectionSize(),
                      getString(R.string.app_queue_selected_items_fmt));

    mItemSelectionCountTextView.setText(updateSelectionCount);

    if (dataEvent == DataEvent.ITEM_SELECTED) {
      if (mAppQueueViewModel.getSelectionSize() ==
          mAppQueueViewModel.getAppsInQueue().size()) {

        mAppQueueViewModel.hasManuallySelectedAll(true);
      }
    }

    if (!mActionPresenter.isActionAvailable(APP_QUEUE, ITEM_SELECTION_BUTTON)) {
      mActionPresenter.available(APP_QUEUE, ITEM_SELECTION_BUTTON, true);
    }
  }

  private void resetBackupItemSelectionCount(final int size) {
    if (size != 0) {
      final String updateSelectionCount =
          String.format(Locale.getDefault(), ITEM_SELECTION_FMT, size,
                        getString(R.string.app_queue_selected_items_fmt));

      mItemSelectionCountTextView.setText(updateSelectionCount);

    } else {
      mItemSelectionCountTextView.setText(
          getString(R.string.app_queue_selected_items_fmt));
    }
  }

  private void updateSelectAllButtonState() {
    if (mAppQueueViewModel.hasManuallySelectedAll()) {

      mAppQueueViewModel.hasManuallySelectedAll(false);

    } else if (mAppQueueViewModel.hasAutomaticallySelectedAll()) {

      mAppQueueViewModel.hasAutomaticallySelectedAll(false);

      mSelectAllItemsButton.setChecked(false);
    }
  }

  private void onBackupDeselected() {
    int size = mAppQueueViewModel.getSelectionSize();

    resetBackupItemSelectionCount(size);

    updateSelectAllButtonState();

    if (!mAppQueueViewModel.hasSelectedItems()) {
      mActionPresenter.available(APP_QUEUE, ITEM_SELECTION_BUTTON, false);
    }
  }

  private void handleDataEvents(DataEvent dataEvent) {
    if (dataEvent.equals(DataEvent.ITEM_ADDED_TO_QUEUE)) {

      updateBackupCountView(mAppQueueViewModel.getAppsInQueue().size());

    } else if (dataEvent == DataEvent.ITEM_SELECTED ||
               dataEvent == DataEvent.ALL_ITEMS_SELECTED) {

      onBackupSelected(dataEvent);

    } else if (dataEvent == DataEvent.ITEM_DESELECTED ||
               dataEvent == DataEvent.ALL_ITEMS_DESELECTED) {
      onBackupDeselected();

    } else if (dataEvent == DataEvent.ITEMS_REMOVED_FROM_QUEUE ||
               dataEvent == DataEvent.ITEMS_REMOVED_FROM_SELECTION) {

      if (mAppQueueViewModel.doesNotHaveBackups()) {
        makeAppQueueActionsAvailable(false);
      }
    }
  }

  private void onBackupSelectionStarted() {
    hideWelcomeView(true);

    hideAppQueueItemSelectionView(false);

    mActionPresenter.available(APP_QUEUE, SEARCH_BUTTON, false);
  }

  private void onBackupSelectionEnded() {
    hideAppQueueItemSelectionView(true);

    hideWelcomeView(false);

    updateBackupCountView(mAppQueueViewModel.getAppsInQueue().size());

    if (mSelectAllItemsButton.isChecked()) {
      mSelectAllItemsButton.setChecked(false);
    }

    mItemSelectionCountTextView.setText(
        getString(R.string.app_queue_selected_items_fmt));

    mActionPresenter.available(APP_QUEUE, SEARCH_BUTTON, true);

    mActionPresenter.available(APP_QUEUE, ITEM_SELECTION_BUTTON, false);
  }

  private void handleItemSelectionState(final SelectionState state) {
    if (state == SelectionState.SELECTION_STARTED) {
      onBackupSelectionStarted();

    } else if (state == SelectionState.SELECTION_ENDED) {
      onBackupSelectionEnded();
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

  private void initializeAppListFragmentActions(final int[] activeColors) {
    final int[][] appListFragmentActionLayouts =
        new int[][] {{R.id.main_search_label, R.id.main_search_button}};

    final ActionButtonsConfig actionButtonsConfig = new ActionButtonsConfig(
        mActionPresenter, MainActivity.this, appListFragmentActionLayouts,
        activeColors, false);

    IActionSetFunctionality appListActions =
        new AppListActions(MainActivity.this);

    mActionPresenter.addActions(ActionSetMaker.makeActionButtonSet(
        actionButtonsConfig, appListActions,
        MainActivity.this::runActionButtonChecks));
  }

  private void initializeAppQueueFragmentActions(final int[] activeColors) {
    final int[][] appQueueFragmentActionLayouts = new int[][] {
        {R.id.app_queue_search_label, R.id.app_queue_search_button},
        {R.id.app_queue_backup_label, R.id.app_queue_backup_button},
        {R.id.app_queue_item_selection_label,
         R.id.app_queue_item_selection_button}};

    final ActionButtonsConfig actionButtonsConfig = new ActionButtonsConfig(
        mActionPresenter, MainActivity.this, appQueueFragmentActionLayouts,
        activeColors, false);

    IActionSetFunctionality appQueueActions = new AppQueueActions(
        MainActivity.this, mAppQueueViewModel, mActionPresenter);

    mActionPresenter.addActions(ActionSetMaker.makeActionButtonSet(
        actionButtonsConfig, appQueueActions,
        MainActivity.this::runActionButtonChecks));
  }

  private void initializeSettingsFragmentActions(int[] activeColors) {
    final int[][] settingsFragmentActionLayouts = new int[][] {
        {R.id.about_us_section_label, R.id.about_us_section_button},
        {R.id.rate_app_label, R.id.rate_app_button},
        {R.id.share_app_label, R.id.share_app_button}};

    final ActionButtonsConfig actionButtonsConfig = new ActionButtonsConfig(
        mActionPresenter, MainActivity.this, settingsFragmentActionLayouts,
        activeColors, false);

    IActionSetFunctionality settingsActions =
        new SettingsActions(MainActivity.this);

    mActionPresenter.addActions(ActionSetMaker.makeActionButtonSet(
        actionButtonsConfig, settingsActions,
        MainActivity.this::runActionButtonChecks));
  }

  private void initializeFAButton() {
    final int[] activeColors = getFloatingActionButtonColors();

    mActionPresenter =
        new ActionPresenter(this, R.id.main_floating_action_button);

    initializeAppListFragmentActions(activeColors);

    initializeAppQueueFragmentActions(activeColors);

    initializeSettingsFragmentActions(activeColors);

    mActionPresenter.swapActions(APP_LIST);

    mActionPresenter.present();
  }

  private void registerObservers() {
    /* Any checks against DataEvent/ItemSelectionState.NONE are used only
   to prevent the callback from being handled during Activity recreation
   which is not needed
   */
    mApkListViewModel.getApkListLiveData().observe(this, apkFiles -> {
      if (apkFiles != null && !apkFiles.isEmpty()) {
        makeAppListActionsAvailable();
      }
    });

    mAppQueueViewModel.getBackupProgressLiveData().observe(
        this, backupProgress -> {
          if (backupProgress.getState() != BackupProgress.ProgressState.NONE) {
            handleBackupProgress(backupProgress);
          }
        });

    mAppQueueViewModel.getDataEventLiveData().observe(this, dataEvent -> {
      if (mAppQueueViewModel.getLastDataEvent() != DataEvent.NONE) {
        handleDataEvents(dataEvent);
      }
    });

    mAppQueueViewModel.getSelectionStateLiveData().observe(
        this, itemSelectionState -> {
          if (mAppQueueViewModel.getCurrentSelectionState() !=
              SelectionState.NONE) {
            handleItemSelectionState(itemSelectionState);
          }
        });
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

  private void makeAppListActionsAvailable() {
    mActionPresenter.available(APP_LIST, SEARCH_BUTTON, true);
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

  public void resetQueue() {
    mAppQueueViewModel.emptyQueue();

    mAppQueueViewModel.setItemSelectionStateTo(SelectionState.SELECTION_ENDED);
  }

  private void showSystemApps(final boolean choice) {
    if (!mAppQueueViewModel.isBackupInProgress()) {
      mActionPresenter.available(APP_LIST, SEARCH_BUTTON, false);

      if (!mAppQueueViewModel.doesNotHaveBackups()) {
        resetQueue();
      }

      mApkListViewModel.clearApkData();

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
    if (!mAppQueueViewModel.isBackupInProgress()) {
      switchThemes(yesOrNo);
    } else {
      Toast
          .makeText(
              this,
              getResources().getString(R.string.backup_in_progress_message_alt),
              Toast.LENGTH_SHORT)
          .show();
    }
  }
}