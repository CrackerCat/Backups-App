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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.Pair;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.backups.app.data.pojos.BackupProgress;
import com.backups.app.data.repositories.BackupRepository;
import com.backups.app.data.viewmodels.ApkListViewModel;
import com.backups.app.data.viewmodels.AppQueueViewModel;
import com.backups.app.data.viewmodels.BackupsViewModelFactory;
import com.backups.app.data.viewmodels.DataEvent;
import com.backups.app.data.viewmodels.ItemSelectionState;
import com.backups.app.ui.actions.ActionHost;
import com.backups.app.ui.actions.ActionPresenter;
import com.backups.app.ui.actions.ActionSetMaker;
import com.backups.app.ui.actions.IAction;
import com.backups.app.ui.actions.IPresenter;
import com.backups.app.ui.adapters.TabAdapter;
import com.backups.app.ui.fragments.AboutUsDialogFragment;
import com.backups.app.ui.fragments.AppListFragment;
import com.backups.app.ui.fragments.AppQueueFragment;
import com.backups.app.ui.fragments.SearchDialogFragment;
import com.backups.app.ui.fragments.SettingsFragment;
import com.backups.app.utils.NotificationsUtils;
import com.backups.app.utils.PackageNameUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Locale;

import static com.backups.app.ui.Constants.ABOUT_US_SECTION_BUTTON;
import static com.backups.app.ui.Constants.APP_LIST;
import static com.backups.app.ui.Constants.APP_QUEUE;
import static com.backups.app.ui.Constants.BACKUP_BUTTON;
import static com.backups.app.ui.Constants.ITEM_SELECTION_BUTTON;
import static com.backups.app.ui.Constants.RATE_APP_BUTTON;
import static com.backups.app.ui.Constants.SEARCH_BUTTON;
import static com.backups.app.ui.Constants.SETTINGS;
import static com.backups.app.ui.Constants.SHARE_APP_BUTTON;
import static com.backups.app.ui.Constants.sItemSelectionFMT;

public class MainActivity extends AppCompatActivity
    implements ActionHost, TabLayout.OnTabSelectedListener,
               SharedPreferences.OnSharedPreferenceChangeListener {

  private static class SettingsKeys {
    public int outputDirectory;
    public boolean showSystemApps;
    public boolean useDarkTheme;
  }

  private static boolean sInitializeValues = true;

  private static String sOutputDirectoryKey;
  private static String sShowSystemAppsKey;
  private static String sAppThemeKey;
  private static String sItemSelectionCountSuffix;
  private static String sBackupErrorMessage;

  private final int[][] APP_LIST_FRAGMENT_ACTION_LAYOUTS =
      new int[][] {{R.id.main_search_label, R.id.main_search_button}};

  private final int[][] APP_QUEUE_FRAGMENT_ACTION_LAYOUTS =
      new int[][] {{R.id.app_queue_search_label, R.id.app_queue_search_button},
                   {R.id.app_queue_backup_label, R.id.app_queue_backup_button},
                   {R.id.app_queue_item_selection_label,
                    R.id.app_queue_item_selection_button}};

  private static final int[][] SETTINGS_FRAGMENT_ACTION_LAYOUTS =
      new int[][] {{R.id.about_us_section_label, R.id.about_us_section_button},
                   {R.id.rate_app_label, R.id.rate_app_button},
                   {R.id.share_app_label, R.id.share_app_button}};

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

    verifyOutputDirectory();

    boolean hasNotScannedForAppsYet =
        mApkListViewModel.getApkListLiveData().getValue() == null;
    if (hasNotScannedForAppsYet) {
      mApkListViewModel.fetchInstalledApps(getPackageManager(),
                                           sSettings.showSystemApps);
    }

    initializeViews();

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

  private void loadSettings() {
    SharedPreferences sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(this);

    sSettings.outputDirectory = Integer.parseInt(sharedPreferences.getString(
        sOutputDirectoryKey, BackupRepository.sPrimaryStorage + ""));

    sSettings.showSystemApps =
        sharedPreferences.getBoolean(sShowSystemAppsKey, false);

    sSettings.useDarkTheme = sharedPreferences.getBoolean(sAppThemeKey, false);
  }

  private void verifyOutputDirectory() {
    if (sSettings.outputDirectory != BackupRepository.sPrimaryStorage) {
      boolean isVolumeAvailable =
          mAppQueueViewModel.setStorageVolumeIndex(sSettings.outputDirectory);

      if (isVolumeAvailable) {
        final int storageVolumeIndex =
            mAppQueueViewModel.getCurrentStorageVolumeIndex();

        sSettings.outputDirectory = storageVolumeIndex;

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
    mAppQueueViewModel.getBackupProgressLiveData().observe(
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

    boolean finished = state.equals(BackupProgress.ProgressState.ERROR) ||
                       state.equals(BackupProgress.ProgressState.FINISHED);

    if (finished) {
      int totalApps = mAppQueueViewModel.getAppsInQueue().size();

      if (totalApps == 0) {
        mAppQueueViewModel.isBackupInProgress(false);

        if (sSettings.showSystemApps) {
          showSystemApps(true);
        }
      }

      updateBackupCountView(totalApps);

      final String backupName = progress.getBackupName();

      PackageNameUtils.resetCountFor(backupName);

      if (state.equals(BackupProgress.ProgressState.ERROR)) {
        Toast
            .makeText(MainActivity.this,
                      String.format(sBackupErrorMessage, backupName),
                      Toast.LENGTH_SHORT)
            .show();
      }
    }
  }

  private void interruptSelectionState() {
    if (mAppQueueViewModel.getCurrentSelectionState().equals(
            ItemSelectionState.SELECTION_STARTED)) {

      if (mAppQueueViewModel.hasSelectedItems()) {
        mAppQueueViewModel.clearSelection();

        mAppQueueViewModel.setItemSelectionStateTo(
            ItemSelectionState.SELECTION_ENDED);
      }
    }
  }

  private Pair<Boolean, String> startPreBackupChecks() {
    boolean isBackupInProgress = mAppQueueViewModel.isBackupInProgress();

    boolean doesNotHaveSufficientStorage =
        !mAppQueueViewModel.hasSufficientStorage();

    String outputMessage;

    boolean canStartBackup = false;

    Resources resources = getResources();

    if (isBackupInProgress) {
      outputMessage = resources.getString(R.string.backup_in_progress_message);
    } else if (doesNotHaveSufficientStorage) {
      outputMessage =
          resources.getString(R.string.insufficient_storage_message);
    } else {
      String startingBackupMessage =
          resources.getString(R.string.commencing_backup_message);

      outputMessage = String.format(startingBackupMessage,
                                    mAppQueueViewModel.getAppsInQueue().size());

      canStartBackup = true;
    }

    return (new Pair<>(canStartBackup, outputMessage));
  }

  private String startPreBackupActionChecks() {
    String outputMessage = null;

    boolean hasNotScannedForApps =
        mApkListViewModel.getApkListLiveData().getValue() == null;

    boolean isBackupInProgress = mAppQueueViewModel.isBackupInProgress();

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

  private void runPreBackupActionChecks() {
    final String outputMessage = startPreBackupActionChecks();

    if (outputMessage != null) {
      Toast.makeText(MainActivity.this, outputMessage, Toast.LENGTH_SHORT)
          .show();
    }
  }

  private void handleDataEvents(DataEvent dataEvent) {
    if (dataEvent.equals(DataEvent.ITEM_ADDED_TO_QUEUE)) {

      updateBackupCountView(mAppQueueViewModel.getAppsInQueue().size());

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

  private void initializeAppListFragmentActions(final int position,
                                                final IAction action) {
    if (position == SEARCH_BUTTON) {

      action.assignCallBacks(v -> {
        SearchDialogFragment appSearchDialog = new SearchDialogFragment();

        appSearchDialog.setDataSetID(SearchDialogFragment.DataSet.APP_LIST);

        appSearchDialog.show(getSupportFragmentManager(),
                             (appSearchDialog.getClass().getSimpleName()));
      }, v -> runPreBackupActionChecks());
    }
  }

  private void initializeAppQueueFragmentActions(final int position,
                                                 final IAction action) {
    if (position == SEARCH_BUTTON) {

      action.assignCallBacks(v -> {
        SearchDialogFragment appSearchDialog = new SearchDialogFragment();

        appSearchDialog.setDataSetID(SearchDialogFragment.DataSet.APP_QUEUE);

        appSearchDialog.show(getSupportFragmentManager(),
                             (appSearchDialog.getClass().getSimpleName()));
      }, v -> runPreBackupActionChecks());

    } else if (position == BACKUP_BUTTON) {
      action.assignCallBacks(v -> {
        if (NotificationsUtils.checkNotifyAndGetResult(
                MainActivity.this, this::startPreBackupChecks)) {
          interruptSelectionState();

          makeAppListActionsAvailable(false);

          makeAppQueueActionsAvailable(false);

          mAppQueueViewModel.startBackup();
        }
      }, v -> runPreBackupActionChecks());

    } else if (position == ITEM_SELECTION_BUTTON) {
      action.assignCallBacks(
          v
          -> {
            if (mAppQueueViewModel.hasSelectedItems()) {
              mAppQueueViewModel.clearAndEmptySelection();
            }
          },
          v -> {
            boolean backupNotInProgress =
                !mAppQueueViewModel.isBackupInProgress();
            boolean canStartSelection =
                !mAppQueueViewModel.doesNotHaveBackups() && backupNotInProgress;

            if (canStartSelection) {
              if (!mAppQueueViewModel.getCurrentSelectionState().equals(
                      ItemSelectionState.SELECTION_STARTED)) {

                mAppQueueViewModel.setItemSelectionStateTo(
                    ItemSelectionState.SELECTION_STARTED);
              }
            } else {
              if (backupNotInProgress) {
                Toast
                    .makeText(
                        MainActivity.this,
                        getResources().getString(R.string.no_apps_in_queue),
                        Toast.LENGTH_SHORT)
                    .show();
              } else {
                Toast
                    .makeText(MainActivity.this,
                              getResources().getString(
                                  R.string.backup_in_progress_message_alt),
                              Toast.LENGTH_SHORT)
                    .show();
              }
            }
          });
    }
  }

  private void initializeSettingsFragmentActions(final int position,
                                                 final IAction action) {
    action.setAvailability(true);
    if (position == ABOUT_US_SECTION_BUTTON) {
      action.assignCallBacks(v -> {
        AboutUsDialogFragment aboutUsFragment = new AboutUsDialogFragment();

        aboutUsFragment.show(getSupportFragmentManager(),
                             (aboutUsFragment.getClass().getSimpleName()));
      }, v -> {});
    } else if (position == RATE_APP_BUTTON) {
      action.assignCallBacks(v -> {
        Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
      }, v -> {});
    } else if (position == SHARE_APP_BUTTON) {
      action.assignCallBacks(v -> {
        Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
      }, v -> {});
    }
  }

  private void initializeFAButton() {
    mActionPresenter =
        new ActionPresenter(this, R.id.main_floating_action_button);

    mActionPresenter.addActions(ActionSetMaker.makeActionSet(
        mActionPresenter, this, APP_LIST_FRAGMENT_ACTION_LAYOUTS,
        this::initializeAppListFragmentActions));

    mActionPresenter.addActions(ActionSetMaker.makeActionSet(
        mActionPresenter, this, APP_QUEUE_FRAGMENT_ACTION_LAYOUTS,
        this::initializeAppQueueFragmentActions));

    mActionPresenter.addActions(ActionSetMaker.makeActionSet(
        mActionPresenter, this, SETTINGS_FRAGMENT_ACTION_LAYOUTS,
        this::initializeSettingsFragmentActions));

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
  public boolean isActionAvailable(final int actionID) {
    return mActionPresenter.isActionAvailable(actionID);
  }

  @Override
  public boolean isActionAvailable(final int actionSet, final int actionID) {
    return mActionPresenter.isActionAvailable(actionSet, actionID);
  }

  @Override
  public void makeActionAvailable(final int actionID, final boolean flag) {
    mActionPresenter.available(actionID, flag);
  }

  @Override
  public void makeActionAvailable(final int actionSet, final int actionID,
                                  final boolean flag) {
    mActionPresenter.available(actionSet, actionID, flag);
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
    if (!mAppQueueViewModel.isBackupInProgress()) {
      mActionPresenter.available(APP_LIST, SEARCH_BUTTON, false);

      if (!mAppQueueViewModel.getAppsInQueue().isEmpty()) {
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

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                        String key) {
    if (key.equals(sShowSystemAppsKey)) {
      showSystemApps((sSettings.showSystemApps =
                          sharedPreferences.getBoolean(key, false)));
    }
  }
}
