package com.backups.app;

import static com.backups.app.ui.Constants.APPLIST;
import static com.backups.app.ui.Constants.APPQUEUE;
import static com.backups.app.ui.Constants.BACKUP_BUTTON;
import static com.backups.app.ui.Constants.SEARCH_BUTTON;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import com.backups.app.data.ApkListViewModel;
import com.backups.app.data.AppQueueViewModel;
import com.backups.app.data.BackupCreator;
import com.backups.app.permissionshandler.PermissionsHandler;
import com.backups.app.ui.actions.ActionPresenter;
import com.backups.app.ui.actions.ActionSetHolder;
import com.backups.app.ui.actions.IAction;
import com.backups.app.ui.adapters.TabAdapter;
import com.backups.app.ui.fragments.AppListFragment;
import com.backups.app.ui.fragments.AppQueueFragment;
import com.backups.app.ui.fragments.SearchDialogFragment;
import com.backups.app.ui.fragments.SettingsFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity
    extends AppCompatActivity implements ActionPresenter.IActionAvailability,
                                         TabLayout.OnTabSelectedListener {
  private AppQueueViewModel mAppQueueViewModel;

  private TextView mBackupCounterView;
  private TabAdapter mTabAdapter;
  private TabLayout mTabLayout;
  private ViewPager2 mViewPager;

  private ActionSetHolder mActionSetHolder;
  private ActionPresenter mActionPresenter;

  private final SearchDialogFragment mAppSearchDialogFragment =
      new SearchDialogFragment();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ApkListViewModel apkListViewModel =
        new ViewModelProvider(this).get(ApkListViewModel.class);
    mAppQueueViewModel =
        new ViewModelProvider(this).get(AppQueueViewModel.class);
    apkListViewModel.fetchInstalledApps(getPackageManager(), true);

    initializeViews();

    mAppQueueViewModel.getAppQueue().observe(this, selection -> {
      if (getLifecycle().getCurrentState() != Lifecycle.State.STARTED) {
        updateBackupCountView(selection.size());
      } else {
        mBackupCounterView.setText(mAppQueueViewModel.getBackupCountLabel());
      }
    });

    initializeFAButton();
    initializeTabLayout();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode,
                                  Intent resultData) {
    super.onActivityResult(requestCode, resultCode, resultData);

    Uri newDirectoryUri;

    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == PermissionsHandler.CREATE_DIRECTORY_CODE) {
        if (resultData != null) {
          newDirectoryUri = resultData.getData();
          DocumentFile pickedDirectory =
              DocumentFile.fromTreeUri(this, newDirectoryUri);
          if (pickedDirectory != null) {
            pickedDirectory.createDirectory(BackupCreator.getOutputDirectory());
          }
        }
      }
    }
  }

  private void addTabs(TabAdapter tabAdapter) {
    tabAdapter.addTab(getString(R.string.apps_tab_name), new AppListFragment());
    tabAdapter.addTab(getString(R.string.queue_tab_name),
                      new AppQueueFragment());
    tabAdapter.addTab(getString(R.string.settings_tab_name),
                      new SettingsFragment());
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

  private void initializeAppListFragmentActions() {
    IAction[] actionButtons = mActionSetHolder.getActionSet(APPLIST);

    for (int action = 0, total = actionButtons.length; action < total;
         ++action) {
      if (action == SEARCH_BUTTON) {
        IAction searchButton = actionButtons[action];
        searchButton.assignActiveCallback(v -> {
          mAppSearchDialogFragment.setDataSetID(APPLIST);
          mAppSearchDialogFragment.show(
              getSupportFragmentManager(),
              (mAppSearchDialogFragment.getClass().getSimpleName()));
        });
        searchButton.assignInactiveCallback(
            v
            -> Toast
                   .makeText(this, R.string.fetching_data_message,
                             Toast.LENGTH_SHORT)
                   .show());
      }
    }
  }

  private void initializeAppQueueFragmentActions() {
    IAction[] actionButtons = mActionSetHolder.getActionSet(APPQUEUE);

    for (int action = 0, total = actionButtons.length; action < total;
         ++action) {
      if (action == SEARCH_BUTTON) {
        IAction searchButton = actionButtons[action];
        searchButton.assignActiveCallback(v -> {
          mAppSearchDialogFragment.setDataSetID(APPQUEUE);
          mAppSearchDialogFragment.show(
              getSupportFragmentManager(),
              (mAppSearchDialogFragment.getClass().getSimpleName()));
        });
        searchButton.assignInactiveCallback(
            v
            -> Toast
                   .makeText(this, R.string.fetching_data_message,
                             Toast.LENGTH_SHORT)
                   .show());
      } else if (action == BACKUP_BUTTON) {
        IAction backupButton = actionButtons[action];
        backupButton.assignActiveCallback(
            v
            -> Toast
                   .makeText(this, R.string.fetching_data_message,
                             Toast.LENGTH_SHORT)
                   .show());

        backupButton.assignInactiveCallback(
            v
            -> Toast
                   .makeText(this, R.string.fetching_data_message,
                             Toast.LENGTH_SHORT)
                   .show());
      }
    }
  }

  private void initializeFAButton() {
    mActionPresenter =
        new ActionPresenter(this, R.id.main_floating_action_button);

    mActionSetHolder = new ActionSetHolder(mActionPresenter, this);

    initializeAppListFragmentActions();
    initializeAppQueueFragmentActions();

    mActionPresenter.addActions(mActionSetHolder.getActionSet(APPLIST));
    mActionPresenter.addActions(mActionSetHolder.getActionSet(APPQUEUE));

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
}
